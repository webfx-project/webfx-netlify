package dev.webfx.demo.cube.textscene.interpol;

import javafx.scene.shape.MoveTo;

/**
 * @author Bruno Salmon
 */
public final class MoveToInterpolator extends PathElementInterpolator<MoveTo> {

    public MoveToInterpolator(MoveTo moveTo) {
        super(moveTo);
        length = 0d;
        minX = maxX = e.getX();
    }

    @Override
    public void movePointAtPercentage(double p) {
        px = e.getX();
        py = e.getY();
    }

}
