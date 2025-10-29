package dev.webfx.demo.cube.smokescene;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.webgl.*;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.typedarray.TypedArrayFactory;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;

import java.util.HashMap;
import java.util.Map;

public class SmokeScene {

    private static final int TEXTURE_DOWN_SAMPLE = 1;
    private static final int TEXTURE_OFFSET = 10;
    private static final double DENSITY_DISSIPATION = 0.98;
    private static final double VELOCITY_DISSIPATION = 0.99;
    private static final double PRESSURE_DISSIPATION = 0.8;
    private static final double PRESSURE_ITERATIONS = 25;
    private static final double CURL = 30;
    private static final double SPLAT_RADIUS = 0.005;

    private final Canvas canvas;
    private final WebGLRenderingContext gl;
    private final DoubleProperty textureWidthFactorProperty = new SimpleDoubleProperty(1) {
        @Override
        protected void invalidated() {
            if (gl != null) {
                initFramebuffers();
                returnCleanContextForCubeScene();
            }
        }
    };
    private final DoubleProperty textureHeightFactorProperty = new SimpleDoubleProperty(1) {
        @Override
        protected void invalidated() {
            if (gl != null) {
                initFramebuffers();
                returnCleanContextForCubeScene();
            }
        }
    };

    private SupportedFormat formatRGBA, formatRG, formatR;
    private int halfFloatTexType;
    private boolean supportLinearFiltering;

    private int textureWidth, textureHeight;
    private DoubleFBO density, velocity, pressure;
    private FBO divergence, curl;

    private GLProgram clearProgram, displayProgram, splatProgram, advectionProgram, divergenceProgram, curlProgram, vorticityProgram, pressureProgram, gradientSubtractProgram;

    private final Pointer pointer = new Pointer();

    public SmokeScene(Canvas canvas) {
        this.canvas = canvas;
        gl = WebGL.getWebGLContext(canvas);
        if (gl == null)
            return;

        // const params = { alpha: false, depth: false, stencil: false, antialias: false };
        // gl = WebGL.getWebGL2Context(webGLNode);
        boolean isWebGL2 = false; // gl != null;
        /*if (!isWebGL2)
            gl = WebGL.getWebGLContext(webGLNode);*/

        Object halfFloat = null;
        if (isWebGL2) {
            gl.getExtension("EXT_color_buffer_float");
            supportLinearFiltering = gl.getExtension("OES_texture_float_linear") != null;
        } else {
            halfFloat = gl.getExtension("OES_texture_half_float");
            supportLinearFiltering = gl.getExtension("OES_texture_half_float_linear") != null;
        }

        halfFloatTexType = isWebGL2 ? gl.HALF_FLOAT : 36193; // halfFloat.HALF_FLOAT_OES;
        if (isWebGL2) {
            formatRGBA = getSupportedFormat(gl, gl.RGBA16F, gl.RGBA, halfFloatTexType);
            formatRG = getSupportedFormat(gl, gl.RG16F, gl.RG, halfFloatTexType);
            formatR = getSupportedFormat(gl, gl.R16F, gl.RED, halfFloatTexType);
        } else {
            formatRGBA = getSupportedFormat(gl, gl.RGBA, gl.RGBA, halfFloatTexType);
            formatRG = getSupportedFormat(gl, gl.RGBA, gl.RGBA, halfFloatTexType);
            formatR = getSupportedFormat(gl, gl.RGBA, gl.RGBA, halfFloatTexType);
        }

        WebGLShader
                baseVertexShader       = compileShader(gl.VERTEX_SHADER,   Resource.getText(Resource.toUrl("baseVertexShader.glsl", getClass()))),
                clearShader            = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("clearShader.glsl", getClass()))),
                displayShader          = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("displayShader.glsl", getClass()))),
                splatShader            = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("splatShader.glsl", getClass()))),
                advectionManualFilteringShader = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("advectionManualFilteringShader.glsl", getClass()))),
                advectionShader        = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("advectionShader.glsl", getClass()))),
                divergenceShader       = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("divergenceShader.glsl", getClass()))),
                curlShader             = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("curlShader.glsl", getClass()))),
                vorticityShader        = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("vorticityShader.glsl", getClass()))),
                pressureShader         = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("pressureShader.glsl", getClass()))),
                gradientSubtractShader = compileShader(gl.FRAGMENT_SHADER, Resource.getText(Resource.toUrl("gradientSubtractShader.glsl", getClass())));

        clearProgram            = new GLProgram(baseVertexShader, clearShader);
        displayProgram          = new GLProgram(baseVertexShader, displayShader);
        splatProgram            = new GLProgram(baseVertexShader, splatShader);
        advectionProgram        = new GLProgram(baseVertexShader, supportLinearFiltering ? advectionShader : advectionManualFilteringShader);
        divergenceProgram       = new GLProgram(baseVertexShader, divergenceShader);
        curlProgram             = new GLProgram(baseVertexShader, curlShader);
        vorticityProgram        = new GLProgram(baseVertexShader, vorticityShader);
        pressureProgram         = new GLProgram(baseVertexShader, pressureShader);
        gradientSubtractProgram = new GLProgram(baseVertexShader, gradientSubtractShader);

        initBlit();

        FXProperties.runNowAndOnPropertiesChange(this::onResize, canvas.widthProperty(), canvas.heightProperty());

        returnCleanContextForCubeScene();
    }

    private void returnCleanContextForCubeScene() {
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);
        gl.disable(gl.BLEND);
    }

    private void onResize() {
        UiScheduler.scheduleInAnimationFrame(() -> {
            gl.viewport(0, 0, gl.getDrawingBufferWidth(), gl.getDrawingBufferHeight());
            initFramebuffers();
            returnCleanContextForCubeScene();
        }, 1);
    }

    double getTextureWidthFactor() {
        return textureWidthFactorProperty.get();
    }

    public DoubleProperty textureWidthFactorProperty() {
        return textureWidthFactorProperty;
    }

    public void setTextureWidthFactor(double factor) {
        textureWidthFactorProperty().set(factor);
    }


    public DoubleProperty textureHeightFactorProperty() {
        return textureHeightFactorProperty;
    }

    double getTextureHeightFactor() {
        return textureHeightFactorProperty.get();
    }

    public void setTextureHeightFactor(double factor) {
        textureHeightFactorProperty().set(factor);
    }


    private long lastAnimationFrameTime;

    public void onAnimationFrame(long now) {
        if (gl == null)
            return;

        double dt = Math.min((now - lastAnimationFrameTime) * 1d / 1_000_000_000, 0.016);
        lastAnimationFrameTime = now;

        gl.viewport(0, 0, textureWidth, textureHeight);

        double texelWidth = 1d / (getTextureWidthFactor() * textureWidth);
        double texelHeight = 1d / (getTextureHeightFactor() * textureHeight);

        advectionProgram.bind();
        gl.uniform2f(advectionProgram.uniforms.get("texelSize"), texelWidth, texelHeight);
        gl.uniform1i(advectionProgram.uniforms.get("uVelocity"), velocity.getRead().texId);
        gl.uniform1i(advectionProgram.uniforms.get("uSource"), velocity.getRead().texId);
        gl.uniform1f(advectionProgram.uniforms.get("dt"), dt);
        gl.uniform1f(advectionProgram.uniforms.get("dissipation"), VELOCITY_DISSIPATION);
        blit(velocity.getWrite().fbo);
        velocity.swap();

        gl.uniform1i(advectionProgram.uniforms.get("uVelocity"), velocity.getRead().texId);
        gl.uniform1i(advectionProgram.uniforms.get("uSource"), density.getRead().texId);
        gl.uniform1f(advectionProgram.uniforms.get("dissipation"), DENSITY_DISSIPATION);
        blit(density.getWrite().fbo);
        density.swap();

        if (pointer.moved) {
            splat(pointer.x, pointer.y, pointer.dx, pointer.dy, pointer.color);
            pointer.moved = false;
        }

        curlProgram.bind();
        gl.uniform2f(curlProgram.uniforms.get("texelSize"), texelWidth, texelHeight);
        gl.uniform1i(curlProgram.uniforms.get("uVelocity"), velocity.getRead().texId);
        blit(curl.fbo);

        vorticityProgram.bind();
        gl.uniform2f(vorticityProgram.uniforms.get("texelSize"), texelWidth, texelHeight);
        gl.uniform1i(vorticityProgram.uniforms.get("uVelocity"), velocity.getRead().texId);
        gl.uniform1i(vorticityProgram.uniforms.get("uCurl"), curl.texId);
        gl.uniform1f(vorticityProgram.uniforms.get("curl"), CURL);
        gl.uniform1f(vorticityProgram.uniforms.get("dt"), dt);
        blit(velocity.getWrite().fbo);
        velocity.swap();

        divergenceProgram.bind();
        gl.uniform2f(divergenceProgram.uniforms.get("texelSize"), texelWidth, texelHeight);
        gl.uniform1i(divergenceProgram.uniforms.get("uVelocity"), velocity.getRead().texId);
        blit(divergence.fbo);

        clearProgram.bind();
        int pressureTexId = pressure.getRead().texId;
        gl.activeTexture(gl.TEXTURE0 + pressureTexId);
        gl.bindTexture(gl.TEXTURE_2D, pressure.getRead().texture);
        gl.uniform1i(clearProgram.uniforms.get("uTexture"), pressureTexId);
        gl.uniform1f(clearProgram.uniforms.get("value"), PRESSURE_DISSIPATION);
        blit(pressure.getWrite().fbo);
        pressure.swap();

        pressureProgram.bind();
        gl.uniform2f(pressureProgram.uniforms.get("texelSize"), texelWidth, texelHeight);
        gl.uniform1i(pressureProgram.uniforms.get("uDivergence"), divergence.texId);
        pressureTexId = pressure.getRead().texId;
        gl.uniform1i(pressureProgram.uniforms.get("uPressure"), pressureTexId);
        gl.activeTexture(gl.TEXTURE0 + pressureTexId);
        for (int i = 0; i < PRESSURE_ITERATIONS; i++) {
            gl.bindTexture(gl.TEXTURE_2D, pressure.getRead().texture);
            blit(pressure.getWrite().fbo);
            pressure.swap();
        }

        gradientSubtractProgram.bind();
        gl.uniform2f(gradientSubtractProgram.uniforms.get("texelSize"), texelWidth, texelHeight);
        gl.uniform1i(gradientSubtractProgram.uniforms.get("uPressure"), pressure.getRead().texId);
        gl.uniform1i(gradientSubtractProgram.uniforms.get("uVelocity"), velocity.getRead().texId);
        blit(velocity.getWrite().fbo);
        velocity.swap();

        gl.viewport(0, (int) (gl.getDrawingBufferHeight() * (1 - getTextureHeightFactor())), gl.getDrawingBufferWidth(), (int) (gl.getDrawingBufferHeight() * getTextureHeightFactor()));
        displayProgram.bind();
        gl.uniform1i(displayProgram.uniforms.get("uTexture"), density.getRead().texId);
        blit(null);

        returnCleanContextForCubeScene();
    }

    private void initBlit() {
        gl.bindBuffer(gl.ARRAY_BUFFER, gl.createBuffer());
        gl.bufferData(gl.ARRAY_BUFFER, TypedArrayFactory.createFloat32Array(-1, -1, -1, 1, 1, 1, 1, -1), gl.STATIC_DRAW);
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, gl.createBuffer());
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, TypedArrayFactory.createUint16Array(0, 1, 2, 0, 2, 3), gl.STATIC_DRAW);
        gl.vertexAttribPointer(0, 2, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(0);
    }

    private void blit(WebGLFramebuffer destination) {
        if (destination == null) {
            gl.enable(gl.BLEND);
            gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
        } else
            gl.disable(gl.BLEND);
        gl.bindFramebuffer(gl.FRAMEBUFFER, destination);
        gl.drawElements(gl.TRIANGLES, 6, gl.UNSIGNED_SHORT, 0);
    }

    private void splat(double x, double y, double dx, double dy, Color color) {
        double canvasWidth = canvas.getWidth(), canvasHeight = canvas.getHeight() * getTextureHeightFactor();
        splatProgram.bind();
        gl.uniform1i(splatProgram.uniforms.get("uTarget"), velocity.getRead().texId);
        gl.uniform1f(splatProgram.uniforms.get("aspectRatio"), canvasWidth / canvasHeight);
        gl.uniform2f(splatProgram.uniforms.get("point"), x / canvasWidth, 1.0 - y / canvasHeight);
        gl.uniform3f(splatProgram.uniforms.get("color"), dx, -dy, 1.0);
        gl.uniform1f(splatProgram.uniforms.get("radius"), SPLAT_RADIUS);
        blit(velocity.getWrite().fbo);
        velocity.swap();

        gl.uniform1i(splatProgram.uniforms.get("uTarget"), density.getRead().texId);
        gl.uniform3f(splatProgram.uniforms.get("color"), color.red * 0.3, color.green * 0.3, color.blue * 0.3);
        blit(density.getWrite().fbo);
        density.swap();
    }

    private static SupportedFormat getSupportedFormat(WebGLRenderingContext gl, int internalFormat, int format, int type) {
        if (supportRenderTextureFormat(gl, internalFormat, format, type))
            return new SupportedFormat(internalFormat, format);
        if (internalFormat == gl.R16F)
            return getSupportedFormat(gl, gl.RG16F, gl.RG, type);
        if (internalFormat == gl.RG16F)
            return getSupportedFormat(gl, gl.RGBA16F, gl.RGBA, type);
        return null;
    }

    private static boolean supportRenderTextureFormat(WebGLRenderingContext gl, int internalFormat, int format, int type) {
        WebGLTexture texture = gl.createTexture();
        gl.bindTexture(gl.TEXTURE_2D, texture);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
        gl.texImage2D(gl.TEXTURE_2D, 0, internalFormat, 4, 4, 0, format, type, null);

        WebGLFramebuffer fbo = gl.createFramebuffer();
        gl.bindFramebuffer(gl.FRAMEBUFFER, fbo);
        gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, texture, 0);

        int status = gl.checkFramebufferStatus(gl.FRAMEBUFFER);
        return status == gl.FRAMEBUFFER_COMPLETE;
    }

    private WebGLShader compileShader(int type, String source) {
        WebGLShader shader = gl.createShader(type);
        gl.shaderSource(shader, source);
        gl.compileShader(shader);

        if (gl.getShaderParameter(shader, gl.COMPILE_STATUS) == null)
            throw new RuntimeException(gl.getShaderInfoLog(shader));

        return shader;
    }

    private void initFramebuffers() {
        textureWidth = gl.getDrawingBufferWidth() >> TEXTURE_DOWN_SAMPLE;
        textureHeight = gl.getDrawingBufferHeight() >> TEXTURE_DOWN_SAMPLE;

        int texType = halfFloatTexType;
        SupportedFormat rgba = formatRGBA;
        SupportedFormat rg = formatRG;
        SupportedFormat r = formatR;

        density = createDoubleFBO(TEXTURE_OFFSET + 2, textureWidth, textureHeight, rgba.internalFormat, rgba.format, texType, supportLinearFiltering ? gl.LINEAR : gl.NEAREST);
        velocity = createDoubleFBO(TEXTURE_OFFSET + 0, textureWidth, textureHeight, rg.internalFormat, rg.format, texType, supportLinearFiltering ? gl.LINEAR : gl.NEAREST);
        divergence = createFBO(TEXTURE_OFFSET + 4, textureWidth, textureHeight, r.internalFormat, r.format, texType, gl.NEAREST);
        curl = createFBO(TEXTURE_OFFSET + 5, textureWidth, textureHeight, r.internalFormat, r.format, texType, gl.NEAREST);
        pressure = createDoubleFBO(TEXTURE_OFFSET + 6, textureWidth, textureHeight, r.internalFormat, r.format, texType, gl.NEAREST);
    }

    private FBO createFBO(int texId, int w, int h, int internalFormat, int format, int type, int param) {
        gl.activeTexture(gl.TEXTURE0 + texId);
        WebGLTexture texture = gl.createTexture();
        gl.bindTexture(gl.TEXTURE_2D, texture);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, param);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, param);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
        gl.texImage2D(gl.TEXTURE_2D, 0, internalFormat, w, h, 0, format, type, null);

        WebGLFramebuffer fbo = gl.createFramebuffer();
        gl.bindFramebuffer(gl.FRAMEBUFFER, fbo);
        gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, texture, 0);
        gl.viewport(0, 0, w, h);
        gl.clear(gl.COLOR_BUFFER_BIT);

        return new FBO(texture, fbo, texId);
    }

    private DoubleFBO createDoubleFBO(int texId, int w, int h, int internalFormat, int format, int type, int param) {
        FBO fbo1 = createFBO(texId, w, h, internalFormat, format, type, param);
        FBO fbo2 = createFBO(texId + 1, w, h, internalFormat, format, type, param);
        return new DoubleFBO(fbo1, fbo2);
    }

    public void movePointer(double x, double y, boolean down) {
        if (!pointer.down && down)
            pointer.color = new Color(Math.random() + 0.2, Math.random() + 0.2, Math.random() + 0.2);
        pointer.down = down;
        if (down) {
            pointer.moved = true;
            pointer.dx = (x - pointer.x) * 10.0;
            pointer.dy = (y - pointer.y) * 10.0;
            pointer.x = x;
            pointer.y = y;
        }
    }

    private class GLProgram {

        final WebGLProgram program;
        final Map<String, WebGLUniformLocation> uniforms = new HashMap<>();

        GLProgram(WebGLShader vertexShader, WebGLShader fragmentShader) {
            program = gl.createProgram();

            gl.attachShader(program, vertexShader);
            gl.attachShader(program, fragmentShader);
            gl.linkProgram(program);

            if (gl.getProgramParameter(program, gl.LINK_STATUS) == null)
                throw new RuntimeException(gl.getProgramInfoLog(program));

            Object programParameter = gl.getProgramParameter(program, gl.ACTIVE_UNIFORMS);
            double uniformCount = Numbers.toDouble(programParameter);
            for (int i = 0; i < uniformCount; i++) {
                String uniformName = gl.getActiveUniform(program, i).getName();
                uniforms.put(uniformName, gl.getUniformLocation(program, uniformName));
            }
        }

        private void bind() {
            gl.useProgram(program);
        }
    }

}