package dev.webfx.demo.cube.cubescene;

import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.webgl.*;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.typedarray.TypedArrayFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */

public final class CubeScene {

    private static final Color BACKGROUND_COLOR = Color.web("#310E68");

    private final WebGLRenderingContext gl;

    // Program info
    private WebGLProgram program;
    private int vertexPosition, vertexNormal, textureCoord;
    private WebGLUniformLocation uProjectionMatrix, uModelViewMatrix, uNormalMatrix, uLightDirection, uSampler;
    // Buffers
    private WebGLBuffer positionBuffer, normalBuffer, textureCoordBuffer, indicesBuffer;

    private final Matrix4d
            projectionMatrix = new Matrix4d(),
            modelViewMatrix = new Matrix4d().translate(0, 0, -6), // amount to translate;
            normalMatrix = new Matrix4d(),
            invertMatrix = new Matrix4d();
    private final Vector3d
            lightDirection = new Vector3d(0, 0, 1),
            cameraPosition = new Vector3d(),
            mouseRotationAxisX = new Vector3d(),
            mouseRotationAxisY = new Vector3d();
    private WebGLTexture[] textures;
    private Image[] images;
    private Video[] videos;
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Map<KeyCode, Consumer<Boolean>> keyListeners = new HashMap<>();


    private final BooleanProperty playVideosProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (gl != null) {
                playOrPauseVideos();
                if (!get())
                    updateTexturesFromImages();
            }
        }
    };

    private final BooleanProperty codeDoorShowingProperty = new SimpleBooleanProperty();

    private final BooleanProperty tubeProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (gl != null) {
                playOrPauseVideos();
                if (!get() && !isPlayVideos())
                    updateTexturesFromImages();
            }
        }
    };
    private final BooleanProperty autoRotateProperty = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            if (get())
                autoRotateFactor = 0.01;
        }
    };
    private double autoRotateAngle;
    private double lastMouseX, lastMouseY, mouseDeltaX, mouseDeltaY, mouseInertiaFactor;
    private double autoRotateFactor;
    private long lastAnimationFrameTime;

    public CubeScene(Canvas canvas) {
        gl = WebGL.getWebGLContext(canvas);
        if (gl == null) {
            return;
        }

        // Create the shader program
        String vsSource = Resource.getText(Resource.toUrl("vertexShader.glsl", getClass()));
        String fsSource = Resource.getText(Resource.toUrl("fragmentShader.glsl", getClass()));
        program = Util.initShaderProgram(gl, vsSource, fsSource);

        // Collect all the info needed to use the shader program. Look up which attribute our shader program is using
        // for aVertexPosition and look up uniform locations.
        vertexPosition = gl.getAttribLocation(program, "aVertexPosition");
        vertexNormal = gl.getAttribLocation(program, "aVertexNormal");
        textureCoord = gl.getAttribLocation(program, "aTextureCoord");
        uProjectionMatrix = gl.getUniformLocation(program, "uProjectionMatrix");
        uModelViewMatrix = gl.getUniformLocation(program, "uModelViewMatrix");
        uNormalMatrix = gl.getUniformLocation(program, "uNormalMatrix");
        uLightDirection = gl.getUniformLocation(program, "uLightDirection");
        uSampler = gl.getUniformLocation(program, "uSampler");

        // Here's where we call the routine that builds all the objects we'll be drawing.
        positionBuffer = initPositionBuffer();
        textureCoordBuffer = initTextureBuffer();
        indicesBuffer = initIndicesBuffer();
        normalBuffer = initNormalBuffer();

        // Flip image pixels into the bottom-to-top order that WebGL expects.
        gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);

        textures = new WebGLTexture[] {
                Util.initTexture(gl),  // Font face
                Util.initTexture(gl),  // Back face
                Util.initTexture(gl),  // Top face
                Util.initTexture(gl),  // Bottom face
                Util.initTexture(gl),  // Right face
                Util.initTexture(gl),  // Left face
                Util.initTexture(gl),  // Code door (facing right)
                Util.initTexture(gl),  // Code door (facing left)
        };

        images = new Image[] {
                loadImage("WebFXCard.webp"),         // Front face   - image aspect ratio: 1 👍
                loadImage("JavaFullStackCard.webp"), // Back face    - image aspect ratio: 1 👍
                loadImage("LongTermCard.webp"),      // Top face     - image aspect ratio: 1 👍
                loadImage("MagicalCard.webp"),       // Bottom face  - image aspect ratio: 1 👍
                loadImage("CrossPlatformCard.webp"), // Right face   - image aspect ratio: 1 👍
                loadImage("ResponsiveCard.webp"),    // Left face    - image aspect ratio: 1 👍
                loadImage("CodeDoor.webp"),          // Door (right) - image aspect ratio: 1 👍
                loadImage("CodeDoor.webp"),          // Door (left)  - image aspect ratio: 1 👍
        };

        videos = new Video[]{
                new Video(gl, "FX2048.mp4"),      // Front face  - video aspect ratio: 1    👍
                new Video(gl, "ModernGauge.mp4"), // Back face   - video aspect ratio: 1    👍
                new Video(gl, "SpaceFX.mp4"),     // Left face   - video aspect ratio: 1    👍
                new Video(gl, "Mandelbrot.mp4"),  // Right face  - video aspect ratio: 1.55 👎🤷
                new Video(gl, "DemoFX.mp4"),      // Top face    - video aspect ratio: 1.77 👎🤷
                new Video(gl, "EnzoClocks.mp4"),  // Bottom face - video aspect ratio: 1.69 👎🤷
        };

        // Mouse steering
        canvas.setCursor(Cursor.OPEN_HAND);
        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX(); lastMouseY = e.getY();
            mouseDeltaX = 0; mouseDeltaY = 0;
            canvas.setCursor(Cursor.CLOSED_HAND);
        });
        canvas.setOnMouseDragged(e -> {
            mouseDeltaX = e.getX() - lastMouseX; mouseDeltaY = e.getY() - lastMouseY;
            mouseInertiaFactor = 0.005;
            lastMouseX = e.getX(); lastMouseY = e.getY();
        });
        canvas.setOnMouseReleased(e -> canvas.setCursor(Cursor.OPEN_HAND));
        // Mouse wheel:
        canvas.setOnScroll(e -> {
            mouseDeltaX = e.getDeltaX(); mouseDeltaY = e.getDeltaY();
            mouseInertiaFactor = 0.001;
        });

        // Keyboard steering
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Scene scene = canvas.getScene();
            if (scene != null) {
                scene.setOnKeyPressed( e -> onKeyPressed(e.getCode()));
                scene.setOnKeyReleased(e -> onKeyReleased(e.getCode()));
            }
        }, canvas.sceneProperty());

        FXProperties.runNowAndOnPropertiesChange(() -> {
            // Create a perspective matrix, a special matrix that is used to simulate the distortion of perspective in a camera.
            // Our field of view is 45 degrees, with a width/height ratio that matches the display size of the canvas, and
            // we only want to see objects between 0.1 units and 100 units away from the camera.
            double fieldOfView = Math.toRadians(45); // in radians
            double aspect = canvas.getWidth() / canvas.getHeight();
            double zNear = 0.1;
            double zFar = 100.0;

            projectionMatrix.identity().perspective(fieldOfView, aspect, zNear, zFar);
        }, canvas.widthProperty(), canvas.heightProperty());
    }

    private void applyMouseCubeRotation() {
        // Refreshing the mouse rotation axis before applying the rotation
        modelViewMatrix.invert(invertMatrix).normalize3x3();
        invertMatrix.transformDirection(mouseRotationAxisX.set(1, 0, 0)); // transforming the screen X axis back into the modelViewMatrix coordinates
        invertMatrix.transformDirection(mouseRotationAxisY.set(0, 1, 0)); // transforming the screen Y axis back into the modelViewMatrix coordinates

        // Applying the rotation
        modelViewMatrix
                .rotate(mouseDeltaY * mouseInertiaFactor, mouseRotationAxisX)
                .rotate(mouseDeltaX * mouseInertiaFactor, mouseRotationAxisY);
    }

    public boolean isAutoRotate() {
        return autoRotateProperty.get();
    }

    public BooleanProperty autoRotateProperty() {
        return autoRotateProperty;
    }

    public void setAutoRotate(boolean autoRotateProperty) {
        this.autoRotateProperty.set(autoRotateProperty);
    }

    public boolean isTube() {
        return tubeProperty.get();
    }

    public BooleanProperty tubeProperty() {
        return tubeProperty;
    }

    public void setTube(boolean tubeProperty) {
        this.tubeProperty.set(tubeProperty);
    }

    public boolean isPlayVideos() {
        return playVideosProperty.get();
    }

    public BooleanProperty playVideosProperty() {
        return playVideosProperty;
    }

    public void setPlayVideos(boolean playVideos) {
        playVideosProperty.set(playVideos);
    }

    public boolean isCodeDoorShowing() {
        return codeDoorShowingProperty.get();
    }

    public BooleanProperty codeDoorShowingProperty() {
        return codeDoorShowingProperty;
    }

    public void setCodeDoorShowing(boolean showCodeDoorProperty) {
        this.codeDoorShowingProperty.set(showCodeDoorProperty);
    }

    private boolean isTextureShowing(int index) {
        switch (index) {
            case 0: // front
            case 1: // back
            case 2: // top
            case 3: // bottom
                return true; // always showing
            case 4: // left
            case 5: // right
                return !isTube(); // not showing in tube mode
            case 6: // door (right)
            case 7: // door (left)
                return isCodeDoorShowing();
        }
        return false; // Shouldn't happen anyway
    }

    private Image loadImage(String resourcePath) {
        return Util.loadImage(resourcePath, this::updateTexturesFromImagesOrVideos);
    }

    private void updateTexturesFromImagesOrVideos() {
        if (isPlayVideos())
            updateTexturesFromVideos();
        else
            updateTexturesFromImages();
    }

    private void updateTexturesFromImages() {
        for (int i = 0; i < images.length; i++) {
            //if (isTextureShowing(i))
                Util.updateImageTexture(gl, textures[i], images[i]);
        }
    }

    private void updateTexturesFromVideos() {
        for (int i = 0; i < videos.length; i++) {
            if (isTextureShowing(i))
                videos[i].updateVideoTexture(textures[i]);
        }
    }

    private void playOrPauseVideos() {
        for (int i = 0; i < videos.length; i++) {
            videos[i].play(isPlayVideos() && isTextureShowing(i));
        }
    }

    public void setKeyListener(KeyCode keyCode, Consumer<Boolean> keyListener) {
        keyListeners.put(keyCode, keyListener);
    }

    public void onKeyPressed(KeyCode keyCode) {
        pressedKeys.add(keyCode);
        Consumer<Boolean> keyListener = keyListeners.get(keyCode);
        if (keyListener != null)
            keyListener.accept(true);
    }

    public void onKeyReleased(KeyCode keyCode) {
        pressedKeys.remove(keyCode);
        Consumer<Boolean> keyListener = keyListeners.get(keyCode);
        if (keyListener != null)
            keyListener.accept(false);
    }

    public void onAnimationFrame(long now) {
        if (gl == null)
            return;

        // Updating the cube textures from videos
        if (isPlayVideos())
            updateTexturesFromVideos();

        // Auto-rotating the cube
        if (autoRotateFactor >= 0.01) {
            long deltaTime = now - lastAnimationFrameTime;
            double deltaAngle = autoRotateFactor * deltaTime * 1d / 1_000_000_000; // convert to seconds
            autoRotateAngle += deltaAngle;
            modelViewMatrix.rotateXYZ(deltaAngle * 0.3, -deltaAngle * 0.7, - deltaAngle * Math.cos(autoRotateAngle));
            if (!isAutoRotate())
                autoRotateFactor *= 0.97;
            else
                autoRotateFactor = Math.min(1, autoRotateFactor * 1.05);
        }
        lastAnimationFrameTime = now;

        // Steering the cube rotation from the mouse
        applyMouseCubeRotation(); // Applies the rotation from the latest detected by the mouse
        mouseInertiaFactor *= 0.95;

        // Steering the camera from the keyboard
        if (!pressedKeys.isEmpty()) {
            // Refreshing camera position (in cube coordinates)
            modelViewMatrix.invert(invertMatrix).getTranslation(cameraPosition);

            Vector3d p = cameraPosition;
            double x = p.x, y = p.y, z = p.z;
            boolean insideCube = Math.abs(x) < 1 && Math.abs(y) < 1 && Math.abs(z) < 1;
            if (pressedKeys.contains(KeyCode.UP))
                modelViewMatrix.translateLocal(0, 0, 0.05);
            else if (pressedKeys.contains(KeyCode.DOWN))
                modelViewMatrix.translateLocal(0, 0, -0.05);
            if (pressedKeys.contains(KeyCode.LEFT))
                modelViewMatrix.rotateLocalY(-Math.PI / 180);
            else if (pressedKeys.contains(KeyCode.RIGHT))
                modelViewMatrix.rotateLocalY(Math.PI / 180);
            if (insideCube && isCodeDoorShowing()) {
                modelViewMatrix.invert(invertMatrix).getTranslation(cameraPosition);
                if (x * cameraPosition.x < 0) { // x changed sign => just crossed the door
                    WebFxKitLauncher.getApplication().getHostServices().showDocument("https://github.com/webfx-demos/webfx-demo-cube/blob/main/webfx-demo-cube-application/src/main/java/dev/webfx/demo/cube/CubeApplication.java");
                }
            }
        }

        // Drawing the scene
        drawScene();
    }

    private WebGLBuffer initPositionBuffer() {
        // Create a buffer for the square's positions.
        WebGLBuffer positionBuffer = gl.createBuffer();

        // Select the positionBuffer as the one to apply buffer operations to from here out.
        gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);

        double s = 0.001; // small value used for the doors x position
        double d = 0.75;  // size of the door

        // Now create an array of positions for the square.
        double[] positions = {
                -1.0, -1.0,  1.0,  1.0, -1.0,  1.0,  1.0,  1.0,  1.0, -1.0,  1.0,  1.0, // Front face
                 1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,  1.0, -1.0,  1.0,  1.0, -1.0, // Back face
                -1.0,  1.0,  1.0,  1.0,  1.0,  1.0,  1.0,  1.0, -1.0, -1.0,  1.0, -1.0, // Top face
                -1.0, -1.0, -1.0,  1.0, -1.0, -1.0,  1.0, -1.0,  1.0, -1.0, -1.0,  1.0, // Bottom face
                 1.0, -1.0,  1.0,  1.0, -1.0, -1.0,  1.0,  1.0, -1.0,  1.0,  1.0,  1.0, // Right face
                -1.0, -1.0, -1.0, -1.0, -1.0,  1.0, -1.0,  1.0,  1.0, -1.0,  1.0, -1.0, // Left face
                   s,   -d,    d,    s,   -d,   -d,    s,    d,   -d,    s,    d,    d, // Door (right)
                  -s,   -d,   -d,   -s,   -d,    d,   -s,    d,    d,   -s,    d,   -d, // Door (left)
        };

        // Now pass the list of positions into WebGL to build the shape. We do this by creating a Float32Array from the
        // JavaScript array, then use it to fill the current buffer.
        gl.bufferData(gl.ARRAY_BUFFER, TypedArrayFactory.createFloat32Array(positions), gl.STATIC_DRAW);

        return positionBuffer;
    }

    private WebGLBuffer initIndicesBuffer() {
        WebGLBuffer indicesBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indicesBuffer);

        // This array defines each face as two triangles, using the indices into the vertex array to specify each
        // triangle's position.
        double[] indices = {
                 0,  1,  2,  0,  2,  3, // front
                 4,  5,  6,  4,  6,  7, // back
                 8,  9, 10,  8, 10, 11, // top
                12, 13, 14, 12, 14, 15, // bottom
                16, 17, 18, 16, 18, 19, // right
                20, 21, 22, 20, 22, 23, // left
                24, 25, 26, 24, 26, 27, // door (right)
                28, 29, 30, 28, 30, 31, // door (left)
        };

        // Now send the element array to GL
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,
                TypedArrayFactory.createUint16Array(indices),
                gl.STATIC_DRAW);

        return indicesBuffer;
    }

    private WebGLBuffer initNormalBuffer() {
        WebGLBuffer normalBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, normalBuffer);

        double[] vertexNormals = {
                 0.0,  0.0,  1.0,  0.0,  0.0,  1.0,  0.0,  0.0,  1.0,  0.0,  0.0,  1.0, // Front
                 0.0,  0.0, -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, -1.0, // Back
                 0.0,  1.0,  0.0,  0.0,  1.0,  0.0,  0.0,  1.0,  0.0,  0.0,  1.0,  0.0, // Top
                 0.0, -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, -1.0,  0.0, // Bottom
                 1.0,  0.0,  0.0,  1.0,  0.0,  0.0,  1.0,  0.0,  0.0,  1.0,  0.0,  0.0, // Right
                -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, // Left
                 1.0,  0.0,  0.0,  1.0,  0.0,  0.0,  1.0,  0.0,  0.0,  1.0,  0.0,  0.0, // Door (right)
                -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, -1.0,  0.0,  0.0, // Door (left)
        };

        gl.bufferData(gl.ARRAY_BUFFER,
                TypedArrayFactory.createFloat32Array(vertexNormals),
                gl.STATIC_DRAW);

        return normalBuffer;
    }

    private WebGLBuffer initTextureBuffer() {
        WebGLBuffer textureCoordBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, textureCoordBuffer);

        double[] textureCoordinates = {
                0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // Front
                0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // Back
                0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // Top
                0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // Bottom
                0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // Right
                0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // Left
                0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // Door (right)
                0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // Door (left)
        };

        gl.bufferData(gl.ARRAY_BUFFER,
                TypedArrayFactory.createFloat32Array(textureCoordinates),
                gl.STATIC_DRAW);

        return textureCoordBuffer;
    }

    // Tell WebGL how to pull out the positions from the position buffer into the vertexPosition attribute.
    private void setPositionAttribute() {
        int numComponents = 3; // pull out 2 values per iteration
        int type = gl.FLOAT; // the data in the buffer is 32bit floats
        boolean normalize = false; // don't normalize
        int stride = 0; // how many bytes to get from one set of values to the next. 0 = use type and numComponents above
        int offset = 0; // how many bytes inside the buffer to start from
        gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
        gl.vertexAttribPointer(
                vertexPosition,
                numComponents,
                type,
                normalize,
                stride,
                offset);
        gl.enableVertexAttribArray(vertexPosition);
    }

    // tell webgl how to pull out the texture coordinates from buffer
    private void setTextureAttribute() {
        int num = 2; // every coordinate composed of 2 values
        int type = gl.FLOAT; // the data in the buffer is 32-bit float
        boolean normalize = false; // don't normalize
        int stride = 0; // how many bytes to get from one set to the next
        int offset = 0; // how many bytes inside the buffer to start from
        gl.bindBuffer(gl.ARRAY_BUFFER, textureCoordBuffer);
        gl.vertexAttribPointer(
                textureCoord,
                num,
                type,
                normalize,
                stride,
                offset);
        gl.enableVertexAttribArray(textureCoord);
    }

    // Tell WebGL how to pull out the normals from the normal buffer into the vertexNormal attribute.
    private void setNormalAttribute() {
        int numComponents = 3;
        int type = gl.FLOAT;
        boolean normalize = false;
        int stride = 0;
        int offset = 0;
        gl.bindBuffer(gl.ARRAY_BUFFER, normalBuffer);
        gl.vertexAttribPointer(
                vertexNormal,
                numComponents,
                type,
                normalize,
                stride,
                offset);
        gl.enableVertexAttribArray(vertexNormal);
    }

    private static final double[] DOUBLE_16_ARRAY = new double[16];

    private void drawScene() {
        gl.viewport(0, 0, gl.getDrawingBufferWidth(), gl.getDrawingBufferHeight());

        Color bc = BACKGROUND_COLOR;
        gl.clearColor(bc.getRed(), bc.getGreen(), bc.getBlue(), 1.0); // Clear to background color, fully opaque
        gl.clearDepth(1.0); // Clear everything
        gl.enable(gl.DEPTH_TEST); // Enable depth testing
        gl.depthFunc(gl.LEQUAL); // Near things obscure far things

        // Clear the canvas before we start drawing on it.
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

        modelViewMatrix.invert(normalMatrix).transpose();

        // Tell WebGL how to pull out the positions from the position buffer into the vertexPosition attribute.
        setPositionAttribute();
        setTextureAttribute();
        setNormalAttribute();

        // Tell WebGL which indices to use to index the vertices
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indicesBuffer);

        // Tell WebGL to use our program when drawing
        gl.useProgram(program);

        // Set the shader uniforms
        gl.uniformMatrix4fv(
                uProjectionMatrix,
                false,
                projectionMatrix.get(DOUBLE_16_ARRAY));
        gl.uniformMatrix4fv(
                uModelViewMatrix,
                false,
                modelViewMatrix.get(DOUBLE_16_ARRAY));
        gl.uniformMatrix4fv(
                uNormalMatrix,
                false,
                normalMatrix.get(DOUBLE_16_ARRAY));
        gl.uniform3f(
                uLightDirection,
                lightDirection.x, lightDirection.y, lightDirection.z
        );

        for (int i = 0; i < textures.length; i++) {
            if (isTextureShowing(i)) {
                gl.activeTexture(gl.TEXTURE0 + i);
                gl.bindTexture(gl.TEXTURE_2D, textures[i]);
                gl.uniform1i(uSampler, i);
                gl.drawElements(gl.TRIANGLES, 6, gl.UNSIGNED_SHORT, 12 * i);
            }
        }
    }
}