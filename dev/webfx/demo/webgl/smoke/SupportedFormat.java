package dev.webfx.demo.webgl.smoke;

/**
 * @author Bruno Salmon
 */
final class SupportedFormat {
    int internalFormat;
    int format;

    public SupportedFormat(int internalFormat, int format) {
        this.internalFormat = internalFormat;
        this.format = format;
    }
}
