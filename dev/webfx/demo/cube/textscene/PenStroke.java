package dev.webfx.demo.cube.textscene;

import dev.webfx.demo.cube.textscene.interpol.*;
import dev.webfx.platform.console.Console;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class PenStroke implements CurveInterpolator {

    static final double PEN_STROKE_SPEED_PIXELS_PER_SECOND = 500;
    private final List<PathElementInterpolator<?>> interpolators;
    private final Path path;
    private final double length;
    private final double minX, maxX;
    private Word word;

    public PenStroke(List<PathElementInterpolator<?>> interpolators, Word previousWord) {
        this.interpolators = interpolators;
        path = createPath();
        double l = 0, thisMinX = 0, thisMaxX = 0;
        for (PathElementInterpolator<?> pei : interpolators) {
            path.getElements().add(pei.getPathElement());
            double peiMinX = pei.getMinX();
            double peiMaxX = pei.getMaxX();
            if (l == 0) {
                thisMinX = peiMinX;
                thisMaxX = peiMaxX;
            } else {
                if (peiMinX < thisMinX)
                    thisMinX = peiMinX;
                if (peiMaxX > thisMaxX)
                    thisMaxX = peiMaxX;
            }
            l += pei.getLength();
        }
        length = l;
        minX = thisMinX;
        maxX = thisMaxX;
        word = previousWord == null ? new Word(minX, maxX, length) : previousWord.closestWord(minX, maxX, length);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public double getMinX() {
        return minX;
    }

    @Override
    public double getMaxX() {
        return maxX;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    @Override
    public Point2D getPointAtPercentage(double p) {
        return getPointAtLength(p * length);
    }

    @Override
    public Point2D getPointAtLength(double length) {
        for (PathElementInterpolator<?> pei : interpolators) {
            double l = pei.getLength();
            if (l >= length)
                return pei.getPointAtLength(length);
            length -= l;
        }
        return null;
    }

    public static List<PenStroke> generatePenStrokesFromSvgText(String svg) {
        List<PenStroke> penStrokes = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String s : svg.split(" d=\"")) {
            if (!s.startsWith("M") && !s.startsWith("m"))
                continue;
            if (sb.length() > 0)
                sb.append(' ');
            sb.append(s, 0, s.indexOf("\""));
        }
        String allSvgPaths = sb.toString();
        List<PathElementInterpolator<?>> interpolators = new ArrayList<>();
        double x0 = 0, y0 = 0, x1 = 0, y1 = 0, c1x =0, c1y = 0, c2x = 0, c2y = 0, rx = 0, ry = 0, xAxisRotation = 0;
        boolean largeArcFlag = false, sweepFlag = false;
        int i = 0;
        Word previousWord = null;
        for (String svgPathToken : allSvgPaths.split("[MmLlHhVvCcQqZzAaSsTtXx]")) { // SsTtXx not yet supported
            if (i != 0 || !svgPathToken.trim().isEmpty()) {
                char c = allSvgPaths.charAt(i);
                double[] digits = Arrays.stream(svgPathToken.split("[\\s,]")).filter(s -> !s.isEmpty()).mapToDouble(Double::parseDouble).toArray();
                int p = 0;
                loop: do {
                    boolean isMoveTo = false, isLineTo = false, isCubicCurve = false, isQuadCurve = false, isArc = false;
                    switch (c) {
                        case 'M':
                            isMoveTo = true;
                            x1 = digits[p++];
                            y1 = digits[p++];
                            break;
                        case 'm':
                            isMoveTo = true;
                            x1 = x0 + digits[p++];
                            y1 = y0 + digits[p++];
                            break;
                        case 'L':
                            isLineTo = true;
                            x1 = digits[p++];
                            y1 = digits[p++];
                            break;
                        case 'l':
                            isLineTo = true;
                            x1 = x0 + digits[p++];
                            y1 = y0 + digits[p++];
                            break;
                        case 'H':
                            isLineTo = true;
                            x1 = digits[p++];
                            break;
                        case 'h':
                            isLineTo = true;
                            x1 = x0 + digits[p++];
                            break;
                        case 'V':
                            isLineTo = true;
                            y1 = digits[p++];
                            break;
                        case 'v':
                            isLineTo = true;
                            y1 = y0 + digits[p++];
                            break;
                        case 'C':
                            isCubicCurve = true;
                            c1x = digits[p++];
                            c1y = digits[p++];
                            c2x = digits[p++];
                            c2y = digits[p++];
                            x1  = digits[p++];
                            y1  = digits[p++];
                            break;
                        case 'c':
                            isCubicCurve = true;
                            c1x = x0 + digits[p++];
                            c1y = y0 + digits[p++];
                            c2x = x0 + digits[p++];
                            c2y = y0 + digits[p++];
                            x1  = x0 + digits[p++];
                            y1  = y0 + digits[p++];
                            break;
                        case 'Q':
                            isQuadCurve = true;
                            c1x = digits[p++];
                            c1y = digits[p++];
                            x1  = digits[p++];
                            y1  = digits[p++];
                            break;
                        case 'q':
                            isQuadCurve = true;
                            c1x = x0 + digits[p++];
                            c1y = y0 + digits[p++];
                            x1  = x0 + digits[p++];
                            y1  = y0 + digits[p++];
                            break;
                        case 'A':
                            isArc = true;
                            rx = digits[p++];
                            ry = digits[p++];
                            xAxisRotation = digits[p++];
                            largeArcFlag = (digits[p++] == 1);
                            sweepFlag = (digits[p++] == 1);
                            x1  = digits[p++];
                            y1  = digits[p++];
                            break;
                        case 'a':
                            isArc = true;
                            rx = digits[p++];
                            ry = digits[p++];
                            xAxisRotation = digits[p++];
                            largeArcFlag = (digits[p++] == 1);
                            sweepFlag = (digits[p++] == 1);
                            x1  = x0 + digits[p++];
                            y1  = y0 + digits[p++];
                            break;
                        case 'Z':
                        case 'z':
                            //path.getElements().add(new ClosePath());
                            break loop;
                        case ' ':
                            break loop;
                        default:
                            Console.log("Unsupported path element: " + c);
                            break loop;
                    }
                    if (isMoveTo) {
                        if (!interpolators.isEmpty()) {
                            PenStroke penStroke = new PenStroke(interpolators, previousWord);
                            penStrokes.add(penStroke);
                            interpolators = new ArrayList<>();
                            previousWord = penStroke.getWord();
                        }
                        interpolators.add(new MoveToInterpolator(new MoveTo(x1, y1)));
                    } else if (isLineTo)
                        interpolators.add(new LineToInterpolator(x0, y0, new LineTo(x1, y1)));
                    else if (isCubicCurve)
                        interpolators.add(new CubicCurveToInterpolator(x0, y0, new CubicCurveTo(c1x, c1y, c2x, c2y, x1, y1)));
                    else if (isQuadCurve)
                        interpolators.add(new QuadCurveToInterpolator(x0, y0, new QuadCurveTo(c1x, c1y, x1, y1)));
                    else if (isArc) {
                        interpolators.add(new ArcToInterpolator(x0, y0, new ArcTo(rx, ry, xAxisRotation, x1, y1, largeArcFlag, sweepFlag)));
                    }
                    x0 = x1;
                    y0 = y1;
                } while (p < digits.length);
                i += svgPathToken.length() + 1;
            }
        }
        if (!interpolators.isEmpty())
            penStrokes.add(new PenStroke(interpolators, previousWord));
        previousWord = null;
        for (PenStroke penStroke : penStrokes) {
            Word word = penStroke.getWord();
            if (word.getMergedIntoWord() != null)
                penStroke.setWord(word.getMergedIntoWord());
            else if (word != previousWord) {
                if (previousWord != null && previousWord.mergeWordIfCloseEnough(word))
                    penStroke.setWord(previousWord);
                else
                    previousWord = word;
            }
        }
        return penStrokes;
    }

    private static Path createPath() {
        Path path = new Path();
        path.setFill(Color.TRANSPARENT);
        path.setStrokeWidth(3);
        path.setManaged(false);
        path.setVisible(false);
        return path;
    }

}
