package dev.webfx.demo.cube.textscene;

import dev.webfx.extras.util.color.ColorSeries;
import dev.webfx.extras.util.color.Colors;
import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
final class Word {

    private static final double MIN_SPACE_BETWEEN_WORDS = 20;

    private static final ColorSeries COLOR_SERIES = Colors.createColorHueShiftSeries(Color.PURPLE, 60);

    private double minX;
    private double maxX;
    private double strokeLength;
    private Page page;
    private Word mergedIntoWord;

    private final Color color = COLOR_SERIES.nextColor();

    Word(double minX, double maxX, double strokeLength) {
        this.minX = minX;
        this.maxX = maxX;
        this.strokeLength = strokeLength;
    }

    double getMinX() {
        return minX;
    }

    double getMaxX() {
        return maxX;
    }

    double getWidth() {
        return maxX - minX;
    }

    public double getStrokeLength() {
        return strokeLength;
    }

    public Color getColor() {
        return color;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Word getMergedIntoWord() {
        return mergedIntoWord;
    }

    public void setMergedIntoWord(Word mergedIntoWord) {
        this.mergedIntoWord = mergedIntoWord;
    }

    boolean mergeWordIfCloseEnough(Word otherWord) {
        if (otherWord == this)
            return true;
        if (mergeIfCloseEnough(otherWord.minX, otherWord.maxX, otherWord.strokeLength)) {
            otherWord.setMergedIntoWord(this);
            return true;
        }
        return false;
    }

    boolean mergeIfCloseEnough(double minX, double maxX, double strokeLength) {
        if (distanceX(this.minX, this.maxX, minX, maxX) > MIN_SPACE_BETWEEN_WORDS)
            return false;
        if (maxX > this.maxX)
            this.maxX = maxX;
        if (minX < this.minX)
            this.minX = minX;
        this.strokeLength += strokeLength;
        return true;
    }

    Word closestWord(double minX, double maxX, double strokeLength) {
        if (mergeIfCloseEnough(minX, maxX, strokeLength))
            return this;
        return new Word(minX, maxX, strokeLength);
    }

    private static double distanceX(double minX1, double maxX1, double minX2, double maxX2) {
        if (minX2 > maxX1) // 2 after 1
            return minX2 - maxX1;
        if (maxX2 < minX1) // 2 before 1
            return minX1 - maxX2;
        return 0; // overlaps
    }

}
