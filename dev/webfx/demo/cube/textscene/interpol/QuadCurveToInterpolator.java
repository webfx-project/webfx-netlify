package dev.webfx.demo.cube.textscene.interpol;

import javafx.scene.shape.QuadCurveTo;

/**
 * @author Bruno Salmon
 */
public final class QuadCurveToInterpolator extends PathElementInterpolator<QuadCurveTo> {

    public QuadCurveToInterpolator(double x0, double y0, QuadCurveTo pathElement) {
        super(x0, y0, pathElement);
    }

    @Override
    public void movePointAtPercentage(double p) {
        movePointAtPercentage(p, x0, y0, e.getControlX(), e.getControlY(), e.getX(), e.getX());
    }

    private void movePointAtPercentage(double p, double x0, double y0, double cx, double cy, double x1, double y1) {
        double q = 1 - p;
        double q2 = q * q;
        double pq = q * p;
        double p2 = p * p;
        px = q2 * x0 + 2 * pq * cx + p2 * x1;
        py = q2 * y0 + 2 * pq * cy + p2 * y1;
    }

}
