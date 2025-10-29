package dev.webfx.demo.cube.cubescene;

import dev.webfx.kit.webgl.WebGLProgram;
import dev.webfx.kit.webgl.WebGLRenderingContext;
import dev.webfx.kit.webgl.WebGLShader;
import dev.webfx.kit.webgl.WebGLTexture;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.typedarray.TypedArray;
import dev.webfx.platform.typedarray.TypedArrayFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
final class Util {

    static WebGLShader loadShader(WebGLRenderingContext gl, int type, String source) {
        WebGLShader shader = gl.createShader(type);

        // Send the source to the shader object
        gl.shaderSource(shader, source);

        // Compile the shader program
        gl.compileShader(shader);

        // See if it compiled successfully
        if (gl.getShaderParameter(shader, gl.COMPILE_STATUS) == null) {
            Console.log("An error occurred compiling the shaders:" + gl.getShaderInfoLog(shader));
            gl.deleteShader(shader);
            return null;
        }

        return shader;
    }

    // Initialize a shader program, so WebGL knows how to draw our data
    static WebGLProgram initShaderProgram(WebGLRenderingContext gl, String vsSource, String fsSource) {
        WebGLShader vertexShader = Util.loadShader(gl, gl.VERTEX_SHADER, vsSource);
        WebGLShader fragmentShader = Util.loadShader(gl, gl.FRAGMENT_SHADER, fsSource);

        // Create the shader program
        WebGLProgram shaderProgram = gl.createProgram();
        gl.attachShader(shaderProgram, vertexShader);
        gl.attachShader(shaderProgram, fragmentShader);
        gl.linkProgram(shaderProgram);

        // If creating the shader program failed, alert
        if (gl.getProgramParameter(shaderProgram, gl.LINK_STATUS) == null) {
            Console.log("Unable to initialize the shader program:" + gl.getProgramInfoLog(shaderProgram));
            return null;
        }

        return shaderProgram;
    }


    static WebGLTexture initTexture(WebGLRenderingContext gl) {
        WebGLTexture texture = gl.createTexture();
        gl.bindTexture(gl.TEXTURE_2D, texture);

        // Because video has to be download over the internet they might take a moment until it's ready so put a single
        // pixel in the texture, so we can use it immediately.
        int level = 0;
        int internalFormat = gl.RGBA;
        int width = 1;
        int height = 1;
        int border = 0;
        int srcFormat = gl.RGBA;
        int srcType = gl.UNSIGNED_BYTE;
        Color color = Color.web("#582559");
        TypedArray pixel = TypedArrayFactory.createUint8Array(color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255, 255); // opaque purple
        gl.texImage2D(
                gl.TEXTURE_2D,
                level,
                internalFormat,
                width,
                height,
                border,
                srcFormat,
                srcType,
                pixel);

        // Turn off mips and set wrapping to clamp to edge, so it will work regardless of the dimensions of the video.
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

        return texture;
    }

    static Image loadImage(String resourcePath, Runnable onLoaded) {
        Image image = new Image(Resource.toUrl(resourcePath, Util.class), true);
        if (onLoaded != null) {
            image.progressProperty().addListener(observable -> {
                if (image.getProgress() >= 1)
                    onLoaded.run();
            });
        }
        return image;
    }

    static void updateImageTexture(WebGLRenderingContext gl, WebGLTexture texture, Image image) {
        if (image.getProgress() < 1)
            return;
        // Because images have to be downloaded over the internet they might take a moment until they are ready.
        // Until then put a single pixel in the texture so we can use it immediately. When the image has finished
        // downloading we'll update the texture with the contents of the image.
        int level = 0;
        int internalFormat = gl.RGBA;
        int srcFormat = gl.RGBA;
        int srcType = gl.UNSIGNED_BYTE;

        gl.bindTexture(gl.TEXTURE_2D, texture);
        gl.texImage2D(
                gl.TEXTURE_2D,
                level,
                internalFormat,
                srcFormat,
                srcType,
                image);

        // WebGL1 has different requirements for power of 2 images vs. non power of 2 images so check if the image is a
        // power of 2 in both dimensions.
        if (isPowerOf2((int) image.getWidth()) && isPowerOf2((int) image.getHeight())) {
            // Yes, it's a power of 2. Generate mips.
            gl.generateMipmap(gl.TEXTURE_2D);
        } else {
            // No, it's not a power of 2. Turn off mips and set wrapping to clamp to edge
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        }
    }

    private static boolean isPowerOf2(int value) {
        return (value & (value - 1)) == 0;
    }

}
