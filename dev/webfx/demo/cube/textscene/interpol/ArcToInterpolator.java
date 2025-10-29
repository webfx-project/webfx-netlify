package dev.webfx.demo.cube.textscene.interpol;

import javafx.scene.shape.ArcTo;

/**
 * @author Bruno Salmon
 */
public class ArcToInterpolator extends PathElementInterpolator<ArcTo> {

    public ArcToInterpolator(double x0, double y0, ArcTo arcTo) {
        super(x0, y0, arcTo);
    }

    @Override
    public void movePointAtPercentage(double p) {
        // Calculate angle theta based on the percentage
        double theta = 2 * Math.PI * p;

        // Calculate the angle of the ellipse's major axis with the x-axis
        double phi = Math.toRadians(e.getXAxisRotation());

        // Calculate the coordinates of the point on the ellipse
        double rx = e.getRadiusX(), ry = e.getRadiusY();
        double x = rx * Math.cos(theta) * Math.cos(phi) - ry * Math.sin(theta) * Math.sin(phi);
        double y = rx * Math.cos(theta) * Math.sin(phi) + ry * Math.sin(theta) * Math.cos(phi);

        // Apply the transformation matrix to handle rotation and translation
        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);

        px = x0 + cosPhi * x - sinPhi * y;
        py = y0 + sinPhi * x + cosPhi * y;
    }
}
