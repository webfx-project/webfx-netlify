package dev.webfx.extras.util.color;

import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public final class Colors {

    public static Color blendColors(Color fgc, Color bgc) {
        return blendColors(fgc, fgc.getOpacity(), bgc);
    }

    public static Color blendColors(Color fgc, double fgo, Color bgc) {
        return blendColors(fgc, fgo, bgc, bgc.getOpacity());
    }

    public static Color blendColors(Color fgc, double fgo, Color bgc, double bgo) {
        double opacity = 1 - (1 - fgo) * (1 - bgo);
        //if (r.A < 1.0e-6) return r; // Fully transparent -- R,G,B not important
        double red   = Math.min(1, fgc.getRed()   * fgo / opacity + bgc.getRed()   * bgo * (1 - fgo) / opacity);
        double green = Math.min(1, fgc.getGreen() * fgo / opacity + bgc.getGreen() * bgo * (1 - fgo) / opacity);
        double blue  = Math.min(1, fgc.getBlue()  * fgo / opacity + bgc.getBlue()  * bgo * (1 - fgo) / opacity);
        return Color.color(red, green, blue, opacity);
    }

    public static Color whitenColor(Color c, double o) {
        return Colors.blendColors(c, o, Color.WHITE);
    }

    public static Color blackenColor(Color c, double o) {
        return Colors.blendColors(c, o, Color.BLACK);
    }

    public static ColorTransformSeries createColorHueShiftSeries() {
        return createColorHueShiftSeries(Color.PURPLE);
    }

    public static ColorTransformSeries createColorHueShiftSeries(Color initialColor) {
        return createColorHueShiftSeries(initialColor, 20);
    }

    public static ColorTransformSeries createColorHueShiftSeries(Color initialColor, double hueShift) {
        return new ColorTransformSeries(initialColor, new ColorHueShifter(hueShift));
    }

}
