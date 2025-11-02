package dev.webfx.extras.fastpixelreaderwriter.spi.impl.teavm.wasm;

import dev.webfx.extras.fastpixelreaderwriter.FastPixelReaderWriter;
import dev.webfx.kit.mapper.peers.javafxgraphics.elemental2.html.ImageDataHelper;
import javafx.scene.image.Image;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * @author Bruno Salmon
 */
public final class TeaVMWasmFastPixelReaderWriter implements FastPixelReaderWriter {

    private static final int RED_OFFSET   = 0;
    private static final int GREEN_OFFSET = 1;
    private static final int BLUE_OFFSET  = 2;
    private static final int ALPHA_OFFSET = 3;

    private final Image image;
    private final int width, height;
    private final Int8Array data;
    private byte[] cache;
    private int x = -1, y, cachePos;
    private boolean cachePosValid;
    private int newA, newR, newG, newB; // Used for writing

    public TeaVMWasmFastPixelReaderWriter(Image image) {
        this.image = image;
        width = (int) image.getWidth();
        height = (int) image.getHeight();
        // ImageDataHelper returns an elemental2.dom.ImageData, but it's basically a reference to the JS ImageData
        elemental2.dom.ImageData elemental2ImageData = ImageDataHelper.getOrCreateImageDataAssociatedWithImage(image);
        // So we can cast it to the TeaVM JSO ImageData, because it's also basically a reference to the JS ImageData
        ImageData teaVMImageData = (ImageData) (Object) elemental2ImageData;
        // Wrapping into an Int8Array for compatibility with Java byte[] cache
        data = new Int8Array(teaVMImageData.getData().getBuffer());
        cleanPixelChanges();
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public void goToPixel(int x, int y) {
        if (this.x != -1 && this.y < height)
            applyPixelChanges();
        this.x = x;
        this.y = y;
        invalidateCachePos();
    }

    @Override
    public boolean gotToNextPixel() {
        if (y >= height)
            return false;
        if (x != -1)
            applyPixelChanges();
        invalidateCachePos();
        if (++x >= width) {
            x = 0;
            if (++y >= height)
                return false;
        }
        return true;
    }

    private void invalidateCachePos() {
        cachePosValid = false;
    }

    private int getCachePos() {
        if (!cachePosValid) {
            cachePos = 4 * (x + y * width);
            cachePosValid = true;
        }
        return cachePos;
    }

    private void cleanPixelChanges() {
        newA = newR = newG = newB = -1;
    }

    private void applyPixelChanges() {
        if (cache != null && (newA != -1 || newR != -1 || newG != -1 || newB != -1)) {
            if (newA == -1)
                newA = getOpacity();
            if (newA == 0)
                newR = newG = newB = 0;
            else {
                if (newR == -1)
                    newR = getRed();
                if (newG == -1)
                    newG = getGreen();
                if (newB == -1)
                    newB = getBlue();
            }
            getCachePos();
            cache[cachePos + BLUE_OFFSET]  = (byte) newB;
            cache[cachePos + GREEN_OFFSET] = (byte) newG;
            cache[cachePos + RED_OFFSET]   = (byte) newR;
            cache[cachePos + ALPHA_OFFSET] = (byte) newA;
        }
        cleanPixelChanges();
    }

    private int getColor(int offset) {
        createCache(true);
        return cache[getCachePos() + offset] & 0xFF;
    }

    @Override
    public int getRed() {
        if (newR != -1)
            return newR;
        return getColor(RED_OFFSET);
    }

    @Override
    public int getGreen() {
        if (newG != -1)
            return newG;
        return getColor(GREEN_OFFSET);
    }

    @Override
    public int getBlue() {
        if (newB != -1)
            return newB;
        return getColor(BLUE_OFFSET);
    }

    @Override
    public int getOpacity() {
        if (newA != -1)
            return newA;
        return getColor(ALPHA_OFFSET);
    }

    @Override
    public void setRed(int red) {
        newR = red;
    }

    @Override
    public void setGreen(int green) {
        newG = green;
    }

    @Override
    public void setBlue(int blue) {
        newB = blue;
    }

    @Override
    public void setOpacity(int opacity) {
        newA = opacity;
    }

    @Override
    public boolean createCache(boolean copyImageData) {
        if (cache == null) {
            if (copyImageData)
                cache = data.copyToJavaArray();
            else
                cache = new byte[4 * width * height];
        }
        return true;
    }

    @Override
    public void writeCache() {
        if (cache != null) {
            data.set(cache);
            image.setPeerCanvasDirty(true);
        }
    }
}
