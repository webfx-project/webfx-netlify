package dev.webfx.extras.util.color;

import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public class ColorHueShifter implements ColorTransform {

    private final double hueShift;

    public ColorHueShifter() {
        this(20);
    }

    public ColorHueShifter(double hueShift) {
        this.hueShift = hueShift;
    }

    @Override
    public Color transformColor(Color color) {
        return color.deriveColor(hueShift, 1d, 1d, 1d);
    }
}
