package dev.webfx.demo.cube.textscene.interpol;

import javafx.geometry.Point2D;
import javafx.scene.shape.PathElement;

/**
 * @author Bruno Salmon
 */
public abstract class PathElementInterpolator<T extends PathElement> implements CurveInterpolator {

    private static final int INTERPOLATION_SEGMENTS_COUNT = 10; // Used for length, minX & maxX computation

    protected final double x0, y0;
    protected final T e;
    protected Double length;
    protected double minX, maxX;
    protected double px, py;

    public PathElementInterpolator(double x0, double y0, T pathElement) {
        this.x0 = x0;
        this.y0 = y0;
        this.e = pathElement;
    }

    public PathElementInterpolator(T pathElement) {
        this(0, 0, pathElement);
    }

    public T getPathElement() {
        return e;
    }

    @Override
    public double getMinX() {
        getLength(); // To force computation on first call
        return minX;
    }

    @Override
    public double getMaxX() {
        getLength(); // To force computation on first call
        return maxX;
    }

    public abstract void movePointAtPercentage(double p);

    public Point2D getPointAtPercentage(double p) {
        movePointAtPercentage(p);
        return new Point2D(px, py);
    }

    @Override
    public double getLength() {
        if (length == null) {
            movePointAtPercentage(0);
            minX = maxX = px;
            double sum = 0, x0 = px, y0 = py, x, y;
            for (int i = 1; i < INTERPOLATION_SEGMENTS_COUNT; i++) {
                movePointAtPercentage(((double) i) / INTERPOLATION_SEGMENTS_COUNT);
                updateMinMaxX();
                sum += lineLength(x0, y0, x = px, y = py);
                x0 = x; y0 = y;
            }
            length = sum;
        }
        return length;
    }

    private void updateMinMaxX() {
        if (px < minX)
            minX = px;
        if (px > maxX)
            maxX = px;
    }

    static double lineLength(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0, dy = y1 - y0;
        return Math.sqrt(dx * dx + dy * dy);
    }

}
