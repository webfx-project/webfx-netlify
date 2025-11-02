package dev.webfx.extras.fastpixelreaderwriter;

import dev.webfx.extras.fastpixelreaderwriter.spi.FastPixelReaderWriterProvider;
import dev.webfx.platform.service.SingleServiceProvider;
import javafx.scene.image.Image;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public interface FastPixelReaderWriter extends FastPixelReader {

    Image getImage();

    void goToPixel(int x, int y);

    boolean gotToNextPixel();

    int getRed();

    int getGreen();

    int getBlue();

    int getOpacity();

    void setRed(int red);

    void setGreen(int green);

    void setBlue(int blue);

    void setOpacity(int opacity);

    default void setArgb(int opacity, int red, int green, int blue) {
        setOpacity(opacity);
        setRgb(red, green, blue);
    }

    default void setRgb(int red, int green, int blue) {
        setRed(red);
        setGreen(green);
        setBlue(blue);
    }

    default boolean createCache(boolean copyImageData) {
        // default implementations with no cache
        return false;
    }

    default void writeCache() {
        // default implementations with no cache
    }

    // Service part

    static FastPixelReaderWriterProvider getProvider() {
        return SingleServiceProvider.getProvider(FastPixelReaderWriterProvider.class, () -> ServiceLoader.load(FastPixelReaderWriterProvider.class));
    }

    static FastPixelReaderWriter create(Image image) {
        return getProvider().createFastPixelReaderWriter(image);
    }
}
