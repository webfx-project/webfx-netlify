package dev.webfx.demo.cube.cubescene;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.webgl.WebGLRenderingContext;
import dev.webfx.kit.webgl.WebGLTexture;
import dev.webfx.platform.resource.Resource;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

/**
 * @author Bruno Salmon
 */
final class Video {
    private final WebGLRenderingContext gl;
    private final String resourcePath;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    boolean playing, started;

    Video(WebGLRenderingContext gl, String resourcePath) {
        this.gl = gl;
        this.resourcePath = resourcePath;
    }

    void play(boolean play) {
        if (mediaPlayer == null) {
            if (!play)
                return;
            mediaPlayer = new MediaPlayer(new Media(Resource.toUrl(resourcePath, getClass())));
            mediaPlayer.setMute(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setOnPlaying(() -> playing = true);
            FXProperties.runOnPropertiesChange(() -> started = true, mediaPlayer.currentTimeProperty());
            mediaView = new MediaView(mediaPlayer);
        }
        boolean statusPlaying = mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
        if (play && !statusPlaying)
            mediaPlayer.play();
        if (!play && statusPlaying)
            mediaPlayer.pause();
    }

    void updateVideoTexture(WebGLTexture videoTexture) {
        if (playing && started) {
            int level = 0;
            int internalFormat = gl.RGBA;
            int srcFormat = gl.RGBA;
            int srcType = gl.UNSIGNED_BYTE;
            gl.bindTexture(gl.TEXTURE_2D, videoTexture);
            gl.texImage2D(
                    gl.TEXTURE_2D,
                    level,
                    internalFormat,
                    srcFormat,
                    srcType,
                    mediaView);
        }
    }
}
