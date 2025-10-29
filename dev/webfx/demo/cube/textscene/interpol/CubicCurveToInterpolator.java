package dev.webfx.demo.cube.textscene.interpol;

import javafx.scene.shape.CubicCurveTo;

/**
 * @author Bruno Salmon
 */
public final class CubicCurveToInterpolator extends PathElementInterpolator<CubicCurveTo> {

    public CubicCurveToInterpolator(double x0, double y0, CubicCurveTo cubicCurveTo) {
        super(x0, y0, cubicCurveTo);
    }

    @Override
    public void movePointAtPercentage(double p) {
        movePointAtPercentage(p, x0, y0, e.getControlX1(), e.getControlY1(), e.getControlX2(), e.getControlY2(), e.getX(), e.getY());
    }

    private void movePointAtPercentage(double p, double x0, double y0, double c1x, double c1y, double c2x, double c2y, double x1, double y1) {
        x0  += (c1x - x0)  * p;
        c1x += (c2x - c1x) * p;
        c2x += (x1  - c2x) * p;
        x0  += (c1x - x0)  * p;
        c1x += (c2x - c1x) * p;
        y0  += (c1y - y0)  * p;
        c1y += (c2y - c1y) * p;
        c2y += (y1  - c2y) * p;
        y0  += (c1y - y0)  * p;
        c1y += (c2y - c1y) * p;
        px = x0 + (c1x - x0) * p;
        py = y0 + (c1y - y0) * p;
    }

}
