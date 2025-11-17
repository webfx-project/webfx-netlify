package dev.webfx.lib.tracerframework;

import javafx.scene.paint.Color;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.worker.workerthread.ApplicationWorkerThreadWorkerBase;

/**
 * @author Bruno Salmon
 */
public interface PixelComputer {

    int getPlacesCount();

    int getFramesCount(int placeIndex);

    TracerThumbnail getPlaceThumbnail(int placeIndex);

    void setUsingWebAssembly(boolean usingWebAssembly);

    boolean isUsingWebAssembly();

    long getLastFrameIterations(); // TODO Find a way to remove this Mandelbrot specific method

    void initFrame(int canvasWidth, int canvasHeight, int placeIndex, int frameIndex);

    void endFrame();

    // Pixel color extractor from the line storage (once it has been computed)
    Color getPixelResultColor(int x, int y, Object linePixelResultStorage);

    // API when working with workers

    Class<? extends ApplicationWorkerThreadWorkerBase> getWorkerClass();

    ReadOnlyAstObject getLineWorkerParameters(int y, int n, boolean firstWorkerCall);

    Object getLinePixelResultStorage(Object workerResult);
}
