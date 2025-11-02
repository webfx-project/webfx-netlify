package dev.webfx.extras.fastpixelreaderwriter.spi.impl.teavm;

import dev.webfx.extras.fastpixelreaderwriter.FastPixelReaderWriter;
import dev.webfx.extras.fastpixelreaderwriter.spi.FastPixelReaderWriterProvider;
import javafx.scene.image.Image;

/**
 * @author Bruno Salmon
 */
public final class TeaVMFastPixelReaderWriterProvider implements FastPixelReaderWriterProvider {

    @Override
    public FastPixelReaderWriter createFastPixelReaderWriter(Image image) {
        return new TeaVMFastPixelReaderWriter(image);
    }
}
