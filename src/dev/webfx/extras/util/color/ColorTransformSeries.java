package dev.webfx.extras.util.color;

import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public class ColorTransformSeries implements ColorSeries {

    private Color initialColor;
    private final ColorTransform colorTransform;
    private Color color;

    public ColorTransformSeries(Color initialColor, ColorTransform colorTransform) {
        this.initialColor = initialColor;
        this.colorTransform = colorTransform;
    }

    @Override
    public Color nextColor() {
        if (color == null)
            color = initialColor;
        else
            color = colorTransform.transformColor(color);
        return color;
    }

    public void reset() {
        color = null;
    }

    public void reset(Color initialColor) {
        this.initialColor = initialColor;
        color = null;
    }
}
