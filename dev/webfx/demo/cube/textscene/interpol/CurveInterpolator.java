package dev.webfx.demo.cube.textscene.interpol;

import javafx.geometry.Point2D;

/**
 * @author Bruno Salmon
 */
public interface CurveInterpolator {

    double getLength();

    double getMinX();

    double getMaxX();

    Point2D getPointAtPercentage(double p);

    default Point2D getPointAtLength(double length) {
        return getPointAtPercentage(length / getLength());
    }

}
