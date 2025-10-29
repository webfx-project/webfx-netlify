package dev.webfx.demo.cube.smokescene;

/**
 * @author Bruno Salmon
 */
final class DoubleFBO {
    FBO fbo1;
    FBO fbo2;

    public DoubleFBO(FBO fbo1, FBO fbo2) {
        this.fbo1 = fbo1;
        this.fbo2 = fbo2;
    }

    public FBO getRead() {
        return fbo1;
    }

    public FBO getWrite() {
        return fbo2;
    }

    void swap() {
        FBO temp = fbo1;
        fbo1 = fbo2;
        fbo2 = temp;
    }
}
