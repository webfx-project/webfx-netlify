package dev.webfx.lib.tracerframework;

import dev.webfx.platform.arch.Arch;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.worker.Worker;
import dev.webfx.platform.worker.mainthread.WorkerPool;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Bruno Salmon
 */
public final class TracerEngine {
    private boolean hdpi;
    private double outputScaleX;
    private double outputScaleY;

    private int width, height;
    private final Canvas canvas;
    private GraphicsContext ctx;
    private final PixelComputer pixelComputer;
    private int threadsCount = Math.max(2, Arch.availableProcessors());
    private int lastThreadsCount;
    private Runnable onFinished;
    private AnimationTimer animationTimer;
    private final AtomicInteger computingThreadsCount = new AtomicInteger();
    private boolean lastFrameHdpi;
    private boolean lastFrameUsedWebAssembly;
    private final WorkerPool webWorkerPool;
    private long t0, cumulatedComputationTime, lastFrameComputationTime;
    private int lastComputedLineIndex, readyLinesCount, nextLineToPaintIndex, startNumber;
    private LineComputationInfo[] computingLines, readyLines;
    private int placeIndex, frameIndex;

    public TracerEngine(Canvas canvas, PixelComputer pixelComputer) {
        this.canvas = canvas;
        this.pixelComputer = pixelComputer;
        Class<?> workerClass = pixelComputer.getWorkerClass();
        webWorkerPool = new WorkerPool(workerClass.getName());
    }

    public int getPlaceIndex() {
        return placeIndex;
    }

    public void setPlaceIndex(int placeIndex) {
        this.placeIndex = placeIndex;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public int getThreadsCount() {
        return threadsCount;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }

    public boolean wasLastFrameUsingWebAssembly() {
        return lastFrameUsedWebAssembly;
    }

    public boolean wasLastFrameHdpi() {
        return lastFrameHdpi;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public long getLastFrameComputationTime() {
        return lastFrameComputationTime;
    }

    public int getLastThreadsCount() {
        return lastThreadsCount;
    }

    public void setHdpi(boolean hdpi) {
        this.hdpi = hdpi;
    }

    public void start() {
        stop(); // Stopping any previous running computation eventually
        startNumber++;
        outputScaleX = hdpi ? Screen.getPrimary().getOutputScaleX() : 1;
        outputScaleY = hdpi ? Screen.getPrimary().getOutputScaleY() : 1;
        width  = (int) (canvas.getWidth()  * outputScaleX);
        height = (int) (canvas.getHeight() * outputScaleY);
        ctx = canvas.getGraphicsContext2D();
        if (computingLines == null || computingLines.length != height) {
            // Will contain the computing info of each line (ordered vertically)
            computingLines = new LineComputationInfo[height];
            // The same array but will be filled with computed lines ready to be paint (the order may differ depending on their computation time)
            readyLines     = new LineComputationInfo[height];
        }
        readyLinesCount = nextLineToPaintIndex = 0;
        cumulatedComputationTime = 0;
        pixelComputer.initFrame(width, height, placeIndex, frameIndex);
        t0 = System.nanoTime();
        computingThreadsCount.set(threadsCount);
        if (threadsCount > 0) { // Using background thread(s) for the computation
            lastComputedLineIndex = -1;
            // Starting computation jobs in the background
            for (int i = 1; i <= threadsCount; i++) // Starting the non-UI thread
                startComputingUsingWorker();
            // Using the UI thread just to paint ready lines on each animation frame
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    paintReadyLines();
                }
            };
        }
        animationTimer.start();
    }

    private void startComputingUsingWorker() {
        Worker worker = webWorkerPool.getWorker();
        // These 2 parameters can eventually be increased
        int messageQueueSize = 1;
        int numberOfLinesPerComputation = 1;
        int lineInfoSize = messageQueueSize * numberOfLinesPerComputation;
        LineComputationInfo[] lineComputationInfos = new LineComputationInfo[lineInfoSize];
        int[] receivedLineInfoIndex = { 0 };
        int[] completionIndex = { -1 };
        //int[] computedLineInfoIndex = { 0 };
        int workerStartNumber = startNumber;
        worker.setOnMessageHandler((data, transfer) -> { // one message per line
            // Checking the tracer hasn't been restarted with new parameters meanwhile
            if (workerStartNumber != startNumber) // If this is the case,
                return; // we don't continue this old stuff computation
            int receivedIndex = receivedLineInfoIndex[0];
            LineComputationInfo lineComputationInfo = lineComputationInfos[receivedIndex];
            lineComputationInfo.linePixelResultStorage = pixelComputer.getLinePixelResultStorage(data);
            addReadyToPaintLine(lineComputationInfo);
            receivedLineInfoIndex[0] = (receivedIndex + 1) % lineInfoSize;
            if (completionIndex[0] < 0 && (receivedIndex + 1) % numberOfLinesPerComputation == 0) {
                int computedIndex = (receivedIndex + 1 - numberOfLinesPerComputation) % lineInfoSize;
                int n = startComputingLineWorker(worker, lineComputationInfos, computedIndex, numberOfLinesPerComputation);
                if (n < numberOfLinesPerComputation) {
                    worker.terminate(); // Will actually put it back into the webWorker pool
                    completionIndex[0] = (computedIndex + n) % lineInfoSize;
                }
            }
            if (receivedLineInfoIndex[0] == completionIndex[0]) {
                logIfComplete();
            }
        });
        for (int i = 0; i < messageQueueSize; i++) {
            startComputingLineWorker(worker, lineComputationInfos, i * numberOfLinesPerComputation, numberOfLinesPerComputation);
        }
    }

    private int startComputingLineWorker(Worker webWorker, LineComputationInfo[] lineComputationInfos, int infoIndex, int numberOfLines) {
        boolean firstWorkerCall = infoIndex == 0 && lineComputationInfos[0] == null;
        int n = 0, cy = -1;
        while (n < numberOfLines) {
            int lineIndex = pickNextLineIndexToCompute();
            LineComputationInfo lineComputationInfo = lineComputationInfos[infoIndex + n] = getLineComputationInfo(lineIndex);
            if (lineComputationInfo == null)
                break;
            if (cy == -1)
                cy = lineComputationInfo.cy;
            n++;
        }
        if (n > 0) {
            ReadOnlyAstObject astParameters = pixelComputer.getLineWorkerParameters(cy, n, firstWorkerCall);
            webWorker.postMessage(AST.nativeObject(astParameters));
        }
        return n;
    }

    public void stop() {
        if (isRunning()) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    public boolean isRunning() {
        return animationTimer != null;
    }

    private void finish() {
        stop();
        pixelComputer.endFrame();
        lastFrameUsedWebAssembly = pixelComputer.isUsingWebAssembly();
        lastFrameHdpi = hdpi;
        if (onFinished != null)
            onFinished.run();
    }

    private void addReadyToPaintLine(LineComputationInfo readyToPaintLine) {
        synchronized (this) {
            readyLines[readyLinesCount++] = readyToPaintLine;
        }
    }

    private void paintReadyLines() { // Must be called by UI thread only
        while (nextLineToPaintIndex < readyLinesCount) {
            LineComputationInfo lci = readyLines[nextLineToPaintIndex];
            int cy = lci.cy;
            for (int cx = 0; cx < width; cx++) {
                Color pixelColor = pixelComputer.getPixelResultColor(cx, cy, lci.linePixelResultStorage);
                colorizePixel(cx, cy, pixelColor);
            }
            if (++nextLineToPaintIndex == height)
                finish();
        }
    }

    private void colorizePixel(int x, int y, Color pixelColor) {
        if (pixelColor != null) { // in multi-pass, null means unchanged color
            ctx.setFill(pixelColor);
            ctx.fillRect(x / outputScaleX, y / outputScaleY, 1 / outputScaleX, 1 / outputScaleY);
        }
    }

    private final static long MILLIS_IN_NANO = 1_000_000;

    private int pickNextLineIndexToCompute() { // Returning height means they have all been computed
        synchronized (this) {
            return ++lastComputedLineIndex;
        }
    }

    private LineComputationInfo getLineComputationInfo(int lineIndex) {
        if (lineIndex >= height)
            return null;
        LineComputationInfo lineComputationInfo = computingLines[lineIndex];
        if (lineComputationInfo == null) {
            computingLines[lineIndex] = lineComputationInfo = new LineComputationInfo();
            lineComputationInfo.cy = lineIndex;
        }
        return lineComputationInfo;
    }

    private void logIfComplete() {
        // End of the loop = end the job for this thread
        if (computingThreadsCount.decrementAndGet() <= 0) { // Was it the last thread to finish?
            // If yes, logging the computation time
            long totalTime = System.nanoTime() - t0;
            lastFrameComputationTime = totalTime / MILLIS_IN_NANO;
            lastThreadsCount = threadsCount;
            Console.log("Completed in " + lastFrameComputationTime + "ms (computation: " + cumulatedComputationTime / MILLIS_IN_NANO + "ms (" + 100 * cumulatedComputationTime / totalTime + "%) - UI: " + (totalTime - cumulatedComputationTime) / MILLIS_IN_NANO + "ms (" + 100 * (totalTime - cumulatedComputationTime) / totalTime + "%)");
        }
    }

}
