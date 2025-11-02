package dev.webfx.extras.fastpixelreaderwriter.spi.impl.teavm;

import dev.webfx.extras.fastpixelreaderwriter.FastPixelReaderWriter;
import dev.webfx.kit.mapper.peers.javafxgraphics.elemental2.html.ImageDataHelper;
import javafx.scene.image.Image;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.typedarrays.Uint8ClampedArray;

/**
 * @author Bruno Salmon
 */
public final class TeaVMFastPixelReaderWriter implements FastPixelReaderWriter {

    private static final int RED_OFFSET   = 0;
    private static final int GREEN_OFFSET = 1;
    private static final int BLUE_OFFSET  = 2;
    private static final int ALPHA_OFFSET = 3;

    private final Image image;
    private final Uint8ClampedArray data;
    private final int width;
    private final int maxIndex;
    private int index = -4;
    private boolean peerCanvasDirty;

    public TeaVMFastPixelReaderWriter(Image image) {
        this.image = image;
        width = (int) image.getWidth();
        // ImageDataHelper returns an elemental2.dom.ImageData, but it's basically a reference to the JS ImageData
        elemental2.dom.ImageData orCreateImageDataAssociatedWithImage = ImageDataHelper.getOrCreateImageDataAssociatedWithImage(image);
        // So we can cast it to the TeaVM JSO ImageData, because it's also basically a reference to the JS ImageData
        ImageData imageData = (ImageData) (Object) orCreateImageDataAssociatedWithImage;
        data = imageData.getData();
        maxIndex = imageData.getData().getLength() - 4;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public void goToPixel(int x, int y) {
        index = 4 * (y * width + x);
    }

    @Override
    public boolean gotToNextPixel() {
        if (index >= maxIndex)
            return false;
        index += 4;
        return true;
    }

    @Override
    public int getRed() {
        return data.get(index + RED_OFFSET);
    }

    @Override
    public int getGreen() {
        return data.get(index + GREEN_OFFSET);
    }

    @Override
    public int getBlue() {
        return data.get(index + BLUE_OFFSET);
    }

    @Override
    public int getOpacity() {
        return data.get(index + ALPHA_OFFSET);
    }

    @Override
    public void setRed(int red) {
        data.set(index + RED_OFFSET, red);
    }

    @Override
    public void setGreen(int green) {
        data.set(index + GREEN_OFFSET, green);
    }

    @Override
    public void setBlue(int blue) {
        data.set(index + BLUE_OFFSET, blue);
    }

    @Override
    public void setOpacity(int opacity) {
        data.set(index + ALPHA_OFFSET, opacity);
    }

    @Override
    public void writeCache() {
        image.setPeerCanvasDirty(true);
    }
}
