package dev.webfx.demo.cube.textscene;

import java.util.List;

/**
 * @author Bruno Salmon
 */
final class Page {

    private static final double MARGINS = 50;

    private final int index;
    private final double width;
    private Page nextPage;
    private Word firstWord, lastWord;
    private double textStrokeLength;

    public Page(int index, double width) {
        this.index = index;
        this.width = width;
    }

    public int getIndex() {
        return index;
    }

    public double getWidth() {
        return width;
    }

    public double getTextWidth() {
        return firstWord == null ? 0 : lastWord.getMaxX() - firstWord.getMinX();
    }

    public double getTextStrokeLength() {
        return textStrokeLength;
    }

    public Word getFirstWord() {
        return firstWord;
    }

    public Word getLastWord() {
        return lastWord;
    }

    public Page getNextPage() {
        return nextPage;
    }

    public void setNextPage(Page nextPage) {
        this.nextPage = nextPage;
    }

    boolean addWordIfFits(Word word) {
        if (firstWord != null && word.getMaxX() > getMaxX() - MARGINS)
            return false;
        addWord(word);
        return true;
    }

    void addWord(Word word) {
        if (firstWord == null)
            firstWord = word;
        lastWord = word;
        textStrokeLength += word.getStrokeLength();
        word.setPage(this);
    }

    double getDrawingDuration() {
        return getTextStrokeLength() / PenStroke.PEN_STROKE_SPEED_PIXELS_PER_SECOND;
    }

    double getMinX() {
        return getFirstWord().getMinX() - MARGINS;
    }

    double getMaxX() {
        return getMinX() + getWidth();
    }

    static void paginate(List<Word> words, double width) {
        Page[] page = { new Page(0, width) };
        words.forEach(word -> {
            Page p = page[0];
            if (!p.addWordIfFits(word)) {
                Page nextPage = new Page(p.getIndex() + 1, width);
                nextPage.addWord(word);
                p.setNextPage(nextPage);
                page[0] = nextPage;
            }
        });
    }

}
