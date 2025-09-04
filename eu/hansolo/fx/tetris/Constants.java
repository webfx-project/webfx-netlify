package eu.hansolo.fx.tetris;

import java.util.HashMap;
import java.util.Map;


public class Constants {
    //public static final String HOME_FOLDER          = new StringBuilder(System.getProperty("user.home")).append(File.separator).toString();
    //public static final String PROPERTIES_FILE_NAME = "tetris.properties";
    public static final String HIGHSCORE_KEY        = "highscore";

    public static final Map<Integer, Long> LEVEL_SPEED_MAP = new HashMap<Integer, Long>() {{
        put(1, 500000000L);
        put(2, 500000000L);
        put(3, 500000000L);
        put(4, 500000000L);
        put(5, 500000000L);
        put(6, 500000000L);
        put(7, 500000000L);
        put(8, 500000000L);
        put(9, 500000000L);
        put(10, 400000000L);
        put(11, 400000000L);
        put(12, 400000000L);
        put(13, 300000000L);
        put(14, 300000000L);
        put(15, 200000000L);
        put(16, 200000000L);
        put(17, 200000000L);
        put(18, 100000000L);
        put(19, 100000000L);
        put(20, 100000000L);
    }};
}

