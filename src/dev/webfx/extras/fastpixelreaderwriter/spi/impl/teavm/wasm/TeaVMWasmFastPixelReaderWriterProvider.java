package dev.webfx.extras.fastpixelreaderwriter.spi.impl.teavm.wasm;

import dev.webfx.extras.fastpixelreaderwriter.FastPixelReaderWriter;
import dev.webfx.extras.fastpixelreaderwriter.spi.FastPixelReaderWriterProvider;
import javafx.scene.image.Image;

/**
 * @author Bruno Salmon
 */
public final class TeaVMWasmFastPixelReaderWriterProvider implements FastPixelReaderWriterProvider {

    @Override
    public FastPixelReaderWriter createFastPixelReaderWriter(Image image) {
        return new TeaVMWasmFastPixelReaderWriter(image);
    }
}
