package dev.webfx.demo.cube.smokescene;

import dev.webfx.kit.webgl.WebGLFramebuffer;
import dev.webfx.kit.webgl.WebGLTexture;

/**
 * @author Bruno Salmon
 */
final class FBO {
    final WebGLTexture texture;
    final WebGLFramebuffer fbo;
    final int texId;

    public FBO(WebGLTexture texture, WebGLFramebuffer fbo, int texId) {
        this.texture = texture;
        this.fbo = fbo;
        this.texId = texId;
    }
}
