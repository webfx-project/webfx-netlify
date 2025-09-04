package eu.hansolo.fx.tetris;

import dev.webfx.platform.resource.Resource;
import javafx.scene.text.Font;


public class Fonts {
    private static final String SILKWORM_NAME;
    private static       String silkwormName;

    private Fonts() {}


    static {
        try {
            silkwormName  = Font.loadFont(Resource.toUrl("Silkworm.ttf", Fonts.class), 10).getFamily();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        SILKWORM_NAME = silkwormName;
    }


    // ******************** Methods *******************************************
    public static Font silkworm(final double size) { return new Font(SILKWORM_NAME, size); }

}
