package dev.webfx.demo.cube.textscene.interpol;

import javafx.scene.shape.LineTo;

/**
 * @author Bruno Salmon
 */
public final class LineToInterpolator extends PathElementInterpolator<LineTo> {

    private final double x0, y0;

    public LineToInterpolator(double x0, double y0, LineTo lineTo) {
        super(lineTo);
        this.x0 = x0;
        this.y0 = y0;
        length = lineLength(x0, y0, e.getX(), e.getY());
        minX = Math.min(x0, e.getX());
        maxX = Math.max(x0, e.getX());
    }

    @Override
    public void movePointAtPercentage(double p) {
        px = x0 + p * (e.getX() - x0);
        py = y0 + p * (e.getY() - y0);
    }

}
