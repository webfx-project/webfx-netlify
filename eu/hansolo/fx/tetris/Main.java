package eu.hansolo.fx.tetris;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.util.scene.DeviceSceneUtil;
import dev.webfx.platform.audio.Audio;
import dev.webfx.platform.audio.AudioService;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.shutdown.Shutdown;
import dev.webfx.platform.useragent.UserAgent;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


public class Main extends Application {

    private final static boolean IS_BROWSER = UserAgent.isBrowser();

    protected enum GameMode {
        STANDARD(Color.rgb(0, 0, 0), Color.rgb(18, 18,18)),
        GLOSSY(Color.rgb(0, 0, 0), Color.rgb(18, 18,18)),
        GITHUB(Color.rgb(15, 18, 23), Color.rgb(22, 27, 34));

        public final Color backgroundColor;
        public final Color patternColor;

        GameMode(final Color backgroundColor, final Color patternColor) {
            this.backgroundColor = backgroundColor;
            this.patternColor    = patternColor;
        }
    }
    protected enum BlockType {
        BLUE(1, new Integer[][] { { 0, 1 },
                                        { 0, 1 },
                                        { 1, 1 }},
                      new Integer[][] { { 1, 1, 1 },
                                        { 0, 0, 1 }},
                      new Integer[][] { { 1, 1 },
                                        { 1, 0 },
                                        { 1, 0 }},
                      new Integer[][] { { 1, 0, 0 },
                                        { 1, 1, 1 }}
             ),
        CYAN(2, new Integer[][] { { 1 },
                                        { 1 },
                                        { 1 },
                                        { 1 } },
                      new Integer[][] { { 1, 1, 1, 1 } },
                      new Integer[][] { { 1 },
                                        { 1 },
                                        { 1 },
                                        { 1 } },
                      new Integer[][] { { 1, 1, 1, 1 } }
             ),
        GREEN(3, new Integer[][] { { 0, 1, 1 },
                                         { 1, 1, 0 } },
                       new Integer[][] { { 1, 0 },
                                         { 1, 1 },
                                         { 0, 1 } },
                       new Integer[][] { { 0, 1, 1 },
                                         { 1, 1, 0 } },
                       new Integer[][] { { 1, 0 },
                                         { 1, 1 },
                                         { 0, 1 } }
              ),
        YELLOW(4, new Integer[][] { { 1, 1 },
                                          { 1, 1 } },
                        new Integer[][] { { 1, 1 },
                                          { 1, 1 } },
                        new Integer[][] { { 1, 1 },
                                          { 1, 1 } },
                        new Integer[][] { { 1, 1 },
                                          { 1, 1 } }),
        ORANGE(5, new Integer[][] { { 1, 0 },
                                          { 1, 0 },
                                          { 1, 1 } },
                        new Integer[][] { { 0, 0, 1 },
                                          { 1, 1, 1 } },
                        new Integer[][] { { 1, 1 },
                                          { 0, 1 },
                                          { 0, 1 } },
                        new Integer[][] { { 1, 1, 1 },
                                          { 1, 0, 0 } }),
        PURPLE(6, new Integer[][] { { 0, 1, 0 },
                                          { 1, 1, 1 } },
                        new Integer[][] { { 0, 1 },
                                          { 1, 1 },
                                          { 0, 1 }},
                        new Integer[][] { { 1, 1, 1 },
                                          { 0, 1, 0 } },
                        new Integer[][] { { 1, 0 },
                                          { 1, 1 },
                                          { 1, 0 } }),
        RED(7, new Integer[][] { { 1, 1, 0 },
                                       { 0, 1, 1 } },
                     new Integer[][] { { 0, 1 },
                                       { 1, 1 },
                                       { 1, 0 } },
                     new Integer[][] { { 1, 1, 0 },
                                       { 0, 1, 1 } },
                     new Integer[][] { { 0, 1 },
                                       { 1, 1 },
                                       { 1, 0 } }
               );

        public final int         code;
        public final Integer[][] matrix_0;
        public final Integer[][] matrix_90;
        public final Integer[][] matrix_180;
        public final Integer[][] matrix_270;



        BlockType(final int code, final Integer[][] matrix_0, final Integer[][] matrix_90, final Integer[][] matrix_180, final Integer[][] matrix_270) {
            this.code       = code;
            this.matrix_0   = matrix_0;
            this.matrix_90  = matrix_90;
            this.matrix_180 = matrix_180;
            this.matrix_270 = matrix_270;
        }
    }

    private static final int                   MATRIX_WIDTH    = 10;
    private static final int                   MATRIX_HEIGHT   = 20;
    private static final double                CELL_WIDTH      = 24;
    private static final double                CELL_HEIGHT     = 24;
    private static final double                GAME_WIDTH      = MATRIX_WIDTH * CELL_WIDTH;
    private static final double                GAME_HEIGHT     = MATRIX_HEIGHT * CELL_HEIGHT;
    private static final double                WIDTH           = GAME_WIDTH + 50;
    private static final double                HEIGHT          = GAME_HEIGHT + 50;
    private static final Random                RND             = new Random();
    private static final Integer[][]           MATRIX          = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                                                                   { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
                                                                 };
    private              boolean               running;
    private              long                  lastGameOver;
    private              long                  lastUpdateCheck;
    private              AnimationTimer        timer;
    private              Canvas                bkgCanvas;
    private              GraphicsContext       bkgCtx;
    private              Canvas                canvas;
    private              GraphicsContext       ctx;
    private              Image                 startScreenImg;
    private              Image                 cyanBlockImg;
    private              Image                 blueBlockImg;
    private              Image                 orangeBlockImg;
    private              Image                 yellowBlockImg;
    private              Image                 greenBlockImg;
    private              Image                 purpleBlockImg;
    private              Image                 redBlockImg;
    private              Image                 cyanGlossyBlockImg;
    private              Image                 blueGlossyBlockImg;
    private              Image                 orangeGlossyBlockImg;
    private              Image                 yellowGlossyBlockImg;
    private              Image                 greenGlossyBlockImg;
    private              Image                 purpleGlossyBlockImg;
    private              Image                 redGlossyBlockImg;
    private              Image                 githubDarkGreenBlockImg;
    private              Image                 githubGreenBlockImg;
    private              Image                 githubLightGreenBlockImg;
    private              Image                 githubVeryLightGreenBlockImg;
    //private              MediaPlayer           mediaPlayer;
    private              Audio                 soundTrack;
    private              Audio                 moveBlockSnd;
    private              Audio                 rotateBlockSnd;
    private              Audio                 levelUpSnd;
    private              Audio                 clearLineSnd;
    private              Audio                 clear4LinesSnd;
    private              Audio                 blockFallingSnd;
    private              Audio                 blockLandedSnd;
    private              Audio                 gameOverSnd;
    private              GameMode              gameMode;
    private              int                   level;
    private              Block                 activeBlock;
    private              Block                 nextBlock;
    private              long                  highscore;
    private              long                  score;
    private              int                   linesCleared;
    private              int                   noOfLifes;
    private              Map<BlockType, Image> imageMap;
    private              Label                 highScoreLabel;
    private              Label                 highScoreValueLabel;
    private              Label                 scoreLabel;
    private              Label                 scoreValueLabel;
    private              Label                 levelLabel;
    private              Label                 levelValueLabel;
    private              Canvas                previewCanvas;
    private              GraphicsContext       previewCtx;
    private              ImageView             startScreenView;

    private              HBox                  gameBox; // Moved to a field for WebFX (visibility management in the browser)
    // Fields used for playing sounds when lines are cleared
    private              int                   clearLineCount;
    private              Runnable              playClearLineSoundRunnable;
    private              Runnable              playBlockFallingSndRunnable;
    // Fields used for the mouse/touch support:
    private              long                  mousePressedTime;
    private              Block                 draggedBlock;
    private              long                  lastDraggedDownTime;


    // ******************** Methods *******************************************
    @Override public void init() {
        running         = false;
        highscore       = PropertyManager.INSTANCE.getLong(Constants.HIGHSCORE_KEY, 0);
        level           = 1;
        imageMap        = new ConcurrentHashMap<>(BlockType.values().length);
        lastUpdateCheck = System.nanoTime();
        timer           = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (running) {
                    // Update block position
                    if (now > lastUpdateCheck + Constants.LEVEL_SPEED_MAP.get(level)) {
                        if (null == activeBlock) { spawnBlock(); }
                        redraw(true);

                        // Increase level every 10 lines cleared
                        if (linesCleared >= 10) {
                            linesCleared = 0;
                            level++;
                            playSound(levelUpSnd);
                            if (level > 20) { level = 0; }
                            levelValueLabel.setText(Integer.toString(level));
                        }

                        // Check for failed
                        for (int i = 0 ; i < MATRIX_WIDTH ; i++) {
                            if (MATRIX[1][i] > 0) {
                                noOfLifes--;
                                if (noOfLifes == 0) {
                                    gameOver();
                                } else {
                                    restartLevel();
                                }
                            }
                        }

                        lastUpdateCheck = now;
                    }
                } else {
                    if (!startScreenView.isVisible()) {
                        if (now > lastGameOver + 5_000_000_000l) {
                            startScreen(true);
                        }
                    }
                }
            }
        };

        // Setup canvas nodes
        bkgCanvas = new Canvas(WIDTH, HEIGHT);
        bkgCtx    = bkgCanvas.getGraphicsContext2D();

        canvas    = new Canvas(GAME_WIDTH, GAME_HEIGHT);
        ctx       = canvas.getGraphicsContext2D();

        previewCanvas = new Canvas(100, 100);
        previewCtx    = previewCanvas.getGraphicsContext2D();

        // Load all images
        loadImages();

        // Load all sounds
        //loadSounds(); // Moved to start(), otherwise fails with Gluon (AudioService must be called from the UI thread)

        // Initialize block
        activeBlock = null;
        nextBlock   = new Block(BlockType.values()[RND.nextInt(BlockType.values().length)], MATRIX_WIDTH * 0.5, -CELL_HEIGHT);

        // Set Game Mode
        setGameMode(GameMode.GLOSSY);

        // Initialize data
        highScoreLabel      = createLabel("HIGHSCORE");
        highScoreValueLabel = createLabel(Long.toString(highscore));

        scoreLabel      = createLabel("SCORE");
        scoreValueLabel = createLabel(Long.toString(score));

        levelLabel      = createLabel("LEVEL");
        levelValueLabel = createLabel(Integer.toString(level));

        // Initialize level
        noOfLifes    = 3;
        score        = 0;
        linesCleared = 0;
    }

    @Override public void start(final Stage stage) {
        loadSounds(); // Better place (called by UI thread) to load sounds with Gluon AudioService
        //mediaPlayer = new MediaPlayer(soundTrack);
        soundTrack.setLooping(true); // mediaPlayer.setCycleCount(-1);
        soundTrack.setVolume(0.5); // mediaPlayer.setVolume(0.5);

        final StackPane gamePane = new StackPane(bkgCanvas, canvas);

        final VBox highScoreBox = new VBox(10, highScoreLabel, highScoreValueLabel);
        highScoreBox.setPadding(new Insets(5 + (IS_BROWSER ? 5 : 0)));
        highScoreBox.setAlignment(Pos.CENTER);
        highScoreBox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));

        final VBox scoreBox = new VBox(10, scoreLabel, scoreValueLabel);
        scoreBox.setPadding(new Insets(5+ (IS_BROWSER ? 5 : 0)));
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));

        final VBox levelBox = new VBox(10, levelLabel, levelValueLabel);
        levelBox.setPadding(new Insets(5+ (IS_BROWSER ? 5 : 0)));
        levelBox.setAlignment(Pos.CENTER);
        levelBox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));

        StackPane previewPane = new StackPane(previewCanvas);
        previewPane.setPadding(new Insets(5+ (IS_BROWSER ? 5 : 0)));
        previewPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));

        final VBox dataPane = new VBox(50, highScoreBox, scoreBox, levelBox, previewPane);
        gameBox  = new HBox(10, gamePane, dataPane);
        gameBox.setAlignment(Pos.CENTER);
        gameBox.setFillHeight(false);

        dataPane.setPrefWidth(200);
        dataPane.setAlignment(Pos.TOP_CENTER);
        dataPane.setPadding(new Insets(10));
        gameBox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        startScreenView = new ImageView(startScreenImg);

        StackPane pane = new StackPane(gameBox, startScreenView);

        // Setting a dummy empty root for the time being (we will wait fonts and images are loaded to show the content)
        final Scene scene = DeviceSceneUtil.newScene(new Pane(), 500, 530, Color.BLACK);

        ScalePane scalePane = new ScalePane(pane);
        pane.setMaxSize(500, 530); // Necessary to scale up with ScalePane
        scalePane.setBackground(gameBox.getBackground());
        // Making the game box invisible at start in the browser, because the start image sometimes displays a bit later
        gameBox.setVisible(false); // This prevents a possible initial flash where we see it before the image (will be made visible on game start)
        DeviceSceneUtil.onFontsAndImagesLoaded(() -> scene.setRoot(scalePane), startScreenImg);

        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);

        scene.setOnKeyPressed(e -> {
            if (startScreenView.isVisible()) {
                startScreen(false);
            } else if (!running && System.nanoTime() > lastGameOver + 5_000_000_000L) {
                switch (e.getCode()) {
                    case SPACE: {
                        level = 1;
                        startLevel();
                        break;
                    }
                }
            } else if (activeBlock != null) {
                switch (e.getCode()) {
                    case LEFT: activeBlock.moveLeft(); break;
                    case RIGHT: activeBlock.moveRight(); break;
                    case SPACE: activeBlock.rotate(); break;
                    case DOWN: activeBlock.drop(); break;
                    default:
                        if ("M".equalsIgnoreCase(e.getText())) {
                            if (GameMode.STANDARD == gameMode) {
                                setGameMode(GameMode.GITHUB);
                            } else if (GameMode.GITHUB == gameMode) {
                                setGameMode(GameMode.GLOSSY);
                            } else {
                                setGameMode(GameMode.STANDARD);
                            }
                            break;
                        }
                }
            }
        });
        scene.setOnMouseClicked(e -> {
            if (startScreenView.isVisible()) {
                startScreen(false);
            } else if (!running && System.nanoTime() > lastGameOver + 5_000_000_000L) {
                level = 1;
                startLevel();
            }
        });
        // Memorising the mouse/touch pressed time for further use in other mouse handlers
        canvas.setOnMousePressed(e -> mousePressedTime = System.currentTimeMillis());
        // Allowing the block to be dragged through mouse/touch
        canvas.setOnMouseDragged(e -> {
            // If the player dragged a previous block that reached the bottom, he must release the mouse/touch in order
            // to drag the new active block.
            if (!running || activeBlock == null || draggedBlock != null && draggedBlock != activeBlock)
                return;
            // Vertical block drag management (down direction only) to eventually speed up the block to bottom
            double deltaY = e.getY() - (activeBlock.y + getBlockHeight(activeBlock) * CELL_HEIGHT);
            // We wait 100ms between 2 moves, and also initially because the player may just want to swipe down
            long now = System.currentTimeMillis();
            if (deltaY > CELL_HEIGHT && now > lastDraggedDownTime + 100 && now > mousePressedTime + 100) {
                draggedBlock = activeBlock;
                activeBlock.moveDown();
                lastDraggedDownTime = now;
            }
            if (deltaY <= CELL_HEIGHT || draggedBlock != null) {
                // Horizontal block drag management (both directions)
                double deltaX = e.getX() - (activeBlock.x * CELL_WIDTH + getBlockWidth(activeBlock) * CELL_WIDTH / 2);
                if (deltaX < -CELL_WIDTH) {
                    draggedBlock = activeBlock;
                    activeBlock.moveLeft();
                } else if (deltaX > CELL_WIDTH) {
                    draggedBlock = activeBlock;
                    activeBlock.moveRight();
                }
            }
        });
        // Dropping the active block on swipe down (touch devices only)
        canvas.setOnSwipeDown(e -> {
            // We don't mix gestures, so we don't drop the block if the player dragged it before (he must release the
            // mouse/touch in order to swipe down again)
            if (!running || activeBlock == null || draggedBlock != null) { return; }
            draggedBlock = activeBlock; // this is to prevent the rotation on mouse released
            activeBlock.drop();

        });
        // Rotating the block on mouse/touch released
        canvas.setOnMouseReleased(e -> {
            // We don't rotate the block if the player dragged it before - or swiped it down (he must release the
            // mouse/touch in order to rotate it again)
            if (running && activeBlock != null && draggedBlock == null) {
                activeBlock.rotate();
            }
            draggedBlock = null; // To allow a new gesture (either on the same or on a new block)
        });

        startScreen(true);

        //timer.start();
        if (!IS_BROWSER)
            soundTrack.play(); //mediaPlayer.play();
    }

    @Override public void stop() {
        Shutdown.exit(0);
        //Platform.exit();
        //System.exit(0);
    }


    // Helper methods
    private void loadImages() {
        startScreenImg = new Image(Resource.toUrl("startScreen.png", getClass()), 500, 530, true, false);

        cyanBlockImg   = new Image(Resource.toUrl("cyanBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        blueBlockImg   = new Image(Resource.toUrl("blueBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        orangeBlockImg = new Image(Resource.toUrl("orangeBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        yellowBlockImg = new Image(Resource.toUrl("yellowBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        greenBlockImg  = new Image(Resource.toUrl("greenBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        purpleBlockImg = new Image(Resource.toUrl("purpleBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        redBlockImg    = new Image(Resource.toUrl("redBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);

        cyanGlossyBlockImg   = new Image(Resource.toUrl("cyanGlossyBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        blueGlossyBlockImg   = new Image(Resource.toUrl("blueGlossyBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        orangeGlossyBlockImg = new Image(Resource.toUrl("orangeGlossyBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        yellowGlossyBlockImg = new Image(Resource.toUrl("yellowGlossyBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        greenGlossyBlockImg  = new Image(Resource.toUrl("greenGlossyBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        purpleGlossyBlockImg = new Image(Resource.toUrl("purpleGlossyBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        redGlossyBlockImg    = new Image(Resource.toUrl("redGlossyBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);

        githubDarkGreenBlockImg      = new Image(Resource.toUrl("githubDarkGreenBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        githubGreenBlockImg          = new Image(Resource.toUrl("githubGreenBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        githubLightGreenBlockImg     = new Image(Resource.toUrl("githubLightGreenBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
        githubVeryLightGreenBlockImg = new Image(Resource.toUrl("githubVeryLightGreenBlock.png", getClass()), CELL_WIDTH, CELL_HEIGHT, true, false);
    }

    private void loadSounds() {
        soundTrack      = loadMusic(Resource.toUrl("soundtrack.mp3", getClass()));
        moveBlockSnd    = loadSound(Resource.toUrl("tetris-move-block.mp3", getClass()));
        rotateBlockSnd  = loadSound(Resource.toUrl("tetris-rotate-block.mp3", getClass()));
        levelUpSnd      = loadSound(Resource.toUrl("tetris-level-up-jingle.mp3", getClass()));
        clearLineSnd    = loadSound(Resource.toUrl("tetris-line-clear.mp3", getClass()));
        clear4LinesSnd  = loadSound(Resource.toUrl("tetris-4-lines.mp3", getClass()));
        blockFallingSnd = loadSound(Resource.toUrl("tetris-block-falling.mp3", getClass()));
        blockLandedSnd  = loadSound(Resource.toUrl("tetris-block-landed.mp3", getClass()));
        gameOverSnd     = loadSound(Resource.toUrl("tetris-game-over.mp3", getClass()));
    }

    private Audio loadMusic(String url) {
        return AudioService.loadMusic(url);
    }

    private Audio loadSound(String url) {
        return AudioService.loadSound(url);
    }
    private Label createLabel(final String text) {
        Label label = new Label(text);
        label.setFont(Fonts.silkworm(17));
        label.setTextFill(Color.WHITE);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }


    // ******************** Game control **************************************
    public void setGameMode(final GameMode mode) {
        this.gameMode = mode;
        this.imageMap.clear();
        switch(this.gameMode) {
            case STANDARD : {
                imageMap.put(BlockType.CYAN, cyanBlockImg);
                imageMap.put(BlockType.BLUE, blueBlockImg);
                imageMap.put(BlockType.ORANGE, orangeBlockImg);
                imageMap.put(BlockType.YELLOW, yellowBlockImg);
                imageMap.put(BlockType.GREEN, greenBlockImg);
                imageMap.put(BlockType.PURPLE, purpleBlockImg);
                imageMap.put(BlockType.RED, redBlockImg);
                break;
            }
            case GLOSSY   : {
                imageMap.put(BlockType.CYAN, cyanGlossyBlockImg);
                imageMap.put(BlockType.BLUE, blueGlossyBlockImg);
                imageMap.put(BlockType.ORANGE, orangeGlossyBlockImg);
                imageMap.put(BlockType.YELLOW, yellowGlossyBlockImg);
                imageMap.put(BlockType.GREEN, greenGlossyBlockImg);
                imageMap.put(BlockType.PURPLE, purpleGlossyBlockImg);
                imageMap.put(BlockType.RED, redGlossyBlockImg);
                break;
            }
            case GITHUB   : {
                imageMap.put(BlockType.CYAN, githubDarkGreenBlockImg);
                imageMap.put(BlockType.BLUE, githubDarkGreenBlockImg);
                imageMap.put(BlockType.ORANGE, githubDarkGreenBlockImg);
                imageMap.put(BlockType.YELLOW, githubGreenBlockImg);
                imageMap.put(BlockType.GREEN, githubGreenBlockImg);
                imageMap.put(BlockType.PURPLE, githubLightGreenBlockImg);
                imageMap.put(BlockType.RED, githubVeryLightGreenBlockImg);
                break;
            }
            default       : {
                imageMap.put(BlockType.CYAN, cyanBlockImg);
                imageMap.put(BlockType.BLUE, blueBlockImg);
                imageMap.put(BlockType.ORANGE, orangeBlockImg);
                imageMap.put(BlockType.YELLOW, yellowBlockImg);
                imageMap.put(BlockType.GREEN, greenBlockImg);
                imageMap.put(BlockType.PURPLE, purpleBlockImg);
                imageMap.put(BlockType.RED, redBlockImg);
                break;
            }
        }
        drawPreview();
        drawBackground();
        redraw(false);
    }


    // Play audio clips
    private void playSound(final Audio audioClip) { audioClip.play(); }


    // Spawn block
    private void spawnBlock() {
        activeBlock = nextBlock;
        nextBlock   = new Block(BlockType.values()[RND.nextInt(BlockType.values().length)], MATRIX_WIDTH * 0.5, -CELL_HEIGHT);
        drawPreview();
    }


    // Start Screen
    private void startScreen(final boolean visible) {
        startScreenView.setVisible(visible);
        startScreenView.setManaged(visible);
        running = visible;
        if (visible) {
            timer.stop();
        } else {
            level = 1;
            startLevel();
            timer.start();
        }
    }


    // Start Level
    private void startLevel() {
        gameBox.setVisible(true);
        soundTrack.play(); //mediaPlayer.play();
        running   = true;
        level     = 1;
        noOfLifes = 3;
        restartLevel();
    }


    // Restart level
    private void restartLevel() {
        clearMatrix();
        activeBlock  = null;
        nextBlock    = new Block(BlockType.values()[RND.nextInt(BlockType.values().length)], MATRIX_WIDTH * 0.5, -CELL_HEIGHT);
        linesCleared = 0;
    }


    // Game Over
    private void gameOver() {
        soundTrack.stop(); //mediaPlayer.stop();
        playSound(gameOverSnd);
        PropertyManager.INSTANCE.set(Constants.HIGHSCORE_KEY, Long.toString(highscore));
        PropertyManager.INSTANCE.storeProperties();
        running = false;
        level   = 1;
        clearMatrix();
        lastGameOver = System.nanoTime();
    }


    // Clear game matrix
    private void clearMatrix() {
        for (int y = 0 ; y < MATRIX_HEIGHT ; y++) {
            for (int x = 0 ; x < MATRIX_WIDTH ; x++) {
                MATRIX[y][x] = 0;
                redraw(false);
            }
        }
    }


    // Get angle related block matrix for given block
    private Integer[][] getBlockMatrix(final Block block) {
        switch (block.angle) {
            case 0   : { return block.blockType.matrix_0; }
            case 90  : { return block.blockType.matrix_90; }
            case 180 : { return block.blockType.matrix_180; }
            case 270 : { return block.blockType.matrix_270; }
            default  : { return new Integer[0][0]; }
        }
    }

    private int getBlockWidth(Block block) {
        return getBlockSize(block, false);
    }

    private int getBlockHeight(Block block) {
        return getBlockSize(block, true);
    }

    private int getBlockSize(Block block, boolean height) {
        Integer[][] blockMatrix = getBlockMatrix(block);
        if (blockMatrix == null || blockMatrix.length == 0)
            return 0;
        if (height)
            return blockMatrix.length;
        Integer[] blockRow = blockMatrix[0];
        return blockRow == null ? 0 : blockRow.length;
    }


    // Check whether the next move is possible in y direction
    private boolean moveDownAllowed(final Block block) {
        if (!block.active) { return false; }
        block.y += CELL_HEIGHT; // Moving to the requested position, just for the time of the test
        boolean allowed = checkBlockAllowed(block); // test
        block.y -= CELL_HEIGHT; // Rolling back to position before the test
        return allowed;
    }

    private boolean moveLeftAllowed(final Block block) {
        if (!block.active) { return false; }
        block.x--; // Moving to the requested position, just for the time of the test
        boolean allowed = checkBlockAllowed(block); // test
        block.x++; // Rolling back to position before the test
        return allowed;
    }

    private boolean moveRightAllowed(final Block block) {
        if (!block.active) { return false; }
        block.x++; // Moving to the requested position, just for the time of the test
        boolean allowed = checkBlockAllowed(block); // test
        block.x--; // Rolling back to position before the test
        return allowed;
    }

    private boolean rotateAllowed(final Block block) {
        if (!block.active) { return false; }
        block.angle = (block.angle + 90) % 360; // Moving to the requested position, just for the time of the test
        boolean allowed = checkBlockAllowed(block); // test
        block.angle = (block.angle - 90) % 360; // Rolling back to position before the test
        return allowed;
    }

    // Test if the block stays inside the matrix and doesn't overlap other blocks
    private boolean checkBlockAllowed(final Block block) {
        if (block.x < 0 || block.x + getBlockWidth(block) > MATRIX_WIDTH) { return false; }
        if (block.y / CELL_HEIGHT + getBlockHeight(block) > MATRIX_HEIGHT) { return false; }
        final Integer[][] blockMatrix = getBlockMatrix(block);
        for (int y = 0 ; y < blockMatrix.length ; y++) {
            for (int x = 0; x < blockMatrix[y].length; x++) {
                if (blockMatrix[y][x] == 0) { continue; }
                int matrixX = (int) (block.x + x);
                int matrixY = (int) ((block.y + CELL_HEIGHT) / CELL_HEIGHT) + y - 1;
                if (matrixX < 0 || matrixX > MATRIX_WIDTH - 1) { continue; }
                if (matrixY < 0 || matrixY > MATRIX_HEIGHT - 1) { continue; }
                if (MATRIX[matrixY][matrixX] > 0) { return false; }
            }
        }
        return true;
    }

    // Check for complete rows
    private void checkForCompleteRows() {
        for (int i = 0 ; i < MATRIX[0].length ; i++) {
            if (MATRIX[0][i] > 0) {
                noOfLifes--;
                if (noOfLifes < 0) { /* Game Over */ return; }
                for (int y = MATRIX_HEIGHT - 1 ; y >= 0 ; y--) {
                    for (int x = 0 ; x < MATRIX[y].length ; x++) {
                        MATRIX[y][x] = 0;
                    }
                }
                return;
            }
        }
        for (int y = MATRIX_HEIGHT - 1 ; y >= 0 ; y--) {
            int rowSum = 0;
            for (int x = 0 ; x < MATRIX_WIDTH ; x++) {
                if (MATRIX[y][x] > 0) { rowSum++; }
            }
            if (rowSum == MATRIX_WIDTH) {
                clearLine(y);
                shiftDown(y);
                clearLine(0);
                y++;
                linesCleared++;
                score += 100;
                if (score > highscore) { highscore = score; }
                highScoreValueLabel.setText(Long.toString(highscore));
                scoreValueLabel.setText(Long.toString(score));
            }
        }
    }

    private void clearLine(final int line) {
        for (int x = 0 ; x < MATRIX_WIDTH ; x++) { MATRIX[line][x] = 0; }
        if (line != 0) // Ignoring line 0 which is called each time in addition
            clearLineCount++;
        // Because clearLine() can be called multiple times consecutively, we postpone playing the sound, so it will
        // be played only once (and with a special sound if 4 lines have been cleared)
        if (playClearLineSoundRunnable == null) {
            Platform.runLater(playClearLineSoundRunnable = () -> {
                playSound(clearLineCount>= 4 ? clear4LinesSnd : clearLineSnd);
                playClearLineSoundRunnable = null;
                clearLineCount = 0;
            });
        }
    }

    private void shiftDown(final int line) {
        for (int y = line ; y > 0 ; y--) {
            for (int x = 0 ; x < MATRIX_WIDTH ; x++) {
                MATRIX[y][x] = MATRIX[y - 1][x];
            }
        }
        // Because shiftDown() can be called multiple times consecutively, we postpone playing the sound, so it will
        // be played only once.
        if (playBlockFallingSndRunnable == null) {
            Platform.runLater(playBlockFallingSndRunnable = () -> {
                playSound(blockFallingSnd);
                playBlockFallingSndRunnable = null;
            });
        }
    }

    // ******************** Redraw ********************************************
    private void drawPreview() {
        previewCtx.clearRect(0, 0, 100, 100);
        final Integer[][] blockMatrix = getBlockMatrix(nextBlock);
        for (int y = 0 ; y < blockMatrix.length ; y++) {
            for (int x = 0 ; x < blockMatrix[y].length ; x++) {
                if (blockMatrix[y][x] == 1) {
                    previewCtx.drawImage(imageMap.get(nextBlock.blockType), (x * CELL_WIDTH), (y * CELL_HEIGHT));
                }
            }
        }
    }

    private void drawBackground() {
        bkgCtx.clearRect(0, 0, WIDTH, HEIGHT);
        switch(gameMode) {
            case STANDARD : {
                bkgCtx.setFill(gameMode.backgroundColor);
                bkgCtx.fillRect(0, 0, WIDTH, HEIGHT);
                bkgCtx.setStroke(Color.GRAY);
                bkgCtx.setLineWidth(10);
                bkgCtx.strokeRoundRect(10, 10, WIDTH - 20, HEIGHT - 20, 10, 10);
                break;
            }
            case GLOSSY   : {
                bkgCtx.setFill(gameMode.backgroundColor);
                bkgCtx.fillRect(0, 0, WIDTH, HEIGHT);
                bkgCtx.setStroke(Color.GRAY);
                bkgCtx.setLineWidth(10);
                bkgCtx.strokeRoundRect(10, 10, WIDTH - 20, HEIGHT - 20, 10, 10);
                break;
            }
            case GITHUB   : {
                bkgCtx.setFill(gameMode.backgroundColor);
                bkgCtx.fillRect(0, 0, WIDTH, HEIGHT);
                bkgCtx.setStroke(Color.rgb(48, 54, 60));
                bkgCtx.setLineWidth(2);
                bkgCtx.strokeRoundRect(10, 10, WIDTH - 20, HEIGHT - 20, 10, 10);
                break;
            }
            default       : {

            }
        }
    }

    private void redraw(final boolean update) {
        ctx.clearRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        ctx.setFill(gameMode.backgroundColor);
        ctx.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        // Draw matrix with inactive blocks
        ctx.setFill(gameMode.patternColor);
        for (int y = 0 ; y < MATRIX_HEIGHT ;y++) {
            for (int x = 0; x < MATRIX_WIDTH; x++) {
                switch(gameMode) {
                    case STANDARD : ctx.fillRect(x * CELL_WIDTH + 1, y * CELL_HEIGHT + 1, 22, 22);  break;
                    case GLOSSY   : ctx.fillRect(x * CELL_WIDTH + 1, y * CELL_HEIGHT + 1, 22, 22);  break;
                    case GITHUB   : ctx.fillRoundRect(x * CELL_WIDTH + 2, y * CELL_HEIGHT + 2, 20, 20, 5, 5);  break;
                }
                switch(MATRIX[y][x]) {
                    case 1 : ctx.drawImage(imageMap.get(BlockType.BLUE), x * CELL_WIDTH, y * CELL_HEIGHT); break;
                    case 2 : ctx.drawImage(imageMap.get(BlockType.CYAN), x * CELL_WIDTH, y * CELL_HEIGHT); break;
                    case 3 : ctx.drawImage(imageMap.get(BlockType.GREEN), x * CELL_WIDTH, y * CELL_HEIGHT); break;
                    case 4 : ctx.drawImage(imageMap.get(BlockType.YELLOW), x * CELL_WIDTH, y * CELL_HEIGHT); break;
                    case 5 : ctx.drawImage(imageMap.get(BlockType.ORANGE), x * CELL_WIDTH, y * CELL_HEIGHT); break;
                    case 6 : ctx.drawImage(imageMap.get(BlockType.PURPLE), x * CELL_WIDTH, y * CELL_HEIGHT); break;
                    case 7 : ctx.drawImage(imageMap.get(BlockType.RED), x * CELL_WIDTH, y * CELL_HEIGHT); break;
                }
            }
        }

        // Draw active block
        if (noOfLifes > 0 && null != activeBlock) {
            if (update) { activeBlock.update(); }
            final Integer[][] blockMatrix = getBlockMatrix(activeBlock);
            for (int y = 0 ; y < blockMatrix.length ; y++) {
                for (int x = 0 ; x < blockMatrix[y].length ; x++) {
                    if (blockMatrix[y][x] == 1) {
                        ctx.drawImage(imageMap.get(activeBlock.blockType), (activeBlock.x * CELL_WIDTH) + (x * CELL_WIDTH), (activeBlock.y) + (y * CELL_HEIGHT));
                    }
                }
            }
        }
        if(!running) {
            ctx.setFont(Fonts.silkworm(24));
            ctx.setFill(Color.WHITE);
            ctx.setTextAlign(TextAlignment.CENTER);
            ctx.fillText("GAME OVER", GAME_WIDTH * 0.5, GAME_HEIGHT * 0.5);
        }

        if (null != activeBlock && !activeBlock.active) { activeBlock = null; }
    }


    // ******************** Inner Classes *************************************
    private abstract class Sprite {
        public Image     image;
        public Bounds    bounds;
        public double    x; // Center of Sprite in x-direction
        public double    y; // Center of Sprite in y-direction
        public double    r;
        public double    vX;
        public double    vY;
        public double    vR;
        public double    width;
        public double    height;
        public double    size;
        public double    radius;
        public boolean   toBeRemoved;


        // ******************** Constructors **************************************
        public Sprite() {
            this(null, 0, 0, 0, 0, 0, 0);
        }
        public Sprite(final Image image) {
            this(image, 0, 0, 0, 0, 0, 0);
        }
        public Sprite(final Image image, final double x, final double y) {
            this(image, x, y, 0, 0, 0, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double vX, final double vY) {
            this(image, x, y, 0, vX, vY, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double r, final double vX, final double vY) {
            this(image, x, y, r, vX, vY, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double r, final double vX, final double vY, final double vR) {
            this.image       = image;
            this.x           = x;
            this.y           = y;
            this.r           = r;
            this.vX          = vX;
            this.vY          = vY;
            this.vR          = vR;
            this.width       = null == image ? 0 : image.getWidth();
            this.height      = null == image ? 0 : image.getHeight();
            this.size        = this.width > this.height ? width : height;
            this.radius      = this.size * 0.5;
            this.toBeRemoved = false;
            this.bounds      = null == image ? new Bounds(0, 0, 0, 0) : new Bounds(x - image.getWidth() * 0.5, y - image.getHeight() * 0.5, image.getWidth(), image.getHeight());
        }


        // ******************** Methods *******************************************
        protected void init() {}

        public void respawn() {}

        public abstract void update();
    }

    private class Block extends Sprite {
        public BlockType   blockType;
        public int         code;
        public int         angle;
        public boolean     active;


        // ******************** Constructors **************************************
        public Block(final BlockType blockType, final double x, final double y) {
            super(imageMap.get(blockType));
            this.blockType   = blockType;
            this.code        = blockType.code;
            this.x           = x;
            this.y           = y;
            this.vX          = 0;
            this.vY          = 0;
            this.width       = CELL_WIDTH;
            this.height      = CELL_HEIGHT;
            this.angle       = 0;
            this.active      = true;
            this.bounds.set(x, y, width, height);
            init();
        }


        // ******************** Methods *******************************************
        @Override protected void init() {
            size   = width > height ? width : height;
            radius = size * 0.5;
        }

        @Override public void update() {
            if (active) {
                if (moveDownAllowed(Block.this)) {
                    this.y += CELL_HEIGHT;
                } else {
                    // Store block in MATRIX
                    final Integer[][] blockMatrix = getBlockMatrix(Block.this);
                    for (int y = 0 ; y < blockMatrix.length ; y++) {
                        for (int x = 0 ; x < blockMatrix[y].length ; x++) {
                            int my = (int) (this.y / CELL_HEIGHT + y);
                            if (my > 0 && blockMatrix[y][x] > 0) {
                                MATRIX[(int) (this.y / CELL_HEIGHT + y)][(int) (this.x + x)] = this.code;
                            }
                        }
                    }
                    this.active = false;
                }
            }
            checkForCompleteRows();
        }

        public void moveLeft() {
            if (!moveLeftAllowed(Block.this)) { return; }
            this.x--;
            playSound(moveBlockSnd);
            redraw(false);
        }

        public void moveRight() {
            if (!moveRightAllowed(Block.this)) { return; }
            this.x++;
            playSound(moveBlockSnd);
            redraw(false);
        }

        public void moveDown() {
            // Always allowed
            playSound(moveBlockSnd);
            redraw(true); // moves the block down, and eventually spawn a new block
        }

        public void rotate() {
            if (!rotateAllowed(this)) {
                // While refusing left & right moves will appear obvious to the player, refusing rotations may be less
                // obvious. So we play a sound to clearly inform the player that we considered his request, but refused
                // it due to the presence of other blocks of the frame.
                playSound(blockFallingSnd); // This sound looks a bit like an error beep, so good to use here.
                return;
            }
            this.angle = (this.angle + 90) % 360;
            playSound(rotateBlockSnd);
            redraw(false);
        }

        public void drop() {
            while(active) { redraw(true); }
            playSound(blockLandedSnd);
        }
    }

    public class Bounds {
        public double x;
        public double y;
        public double width;
        public double height;
        public double minX;
        public double minY;
        public double maxX;
        public double maxY;
        public double centerX;
        public double centerY;


        // ******************** Constructors **************************************
        public Bounds() {
            this(0, 0, 0, 0);
        }
        public Bounds(final double width, final double height) {
            this(0, 0, width, height);
        }
        public Bounds(final double x, final double y, final double width, final double height) {
            set(x, y, width, height);
        }


        // ******************** Methods *******************************************
        public void set(final Bounds bounds) {
            set(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        public void set(final double x, final double y, final double width, final double height) {
            this.x       = x;
            this.y       = y;
            this.width   = width;
            this.height  = height;
            this.minX    = x;
            this.minY    = y;
            this.maxX    = x + width;
            this.maxY    = y + height;
            this.centerX = x + width * 0.5;
            this.centerY = y + height * 0.5;
        }

        public boolean contains(final double x, final double y) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }

        public boolean intersects(final Bounds other) {
            return other.minX <= maxX && minX <= other.maxX && other.minY <= maxY && minY <= other.maxY;
        }

        @Override public String toString() {
            return minX + ", " + minY + ", " + maxX + ", " + maxY + ", " + width + ", " + height;
        }
    }


    // ******************** Start *********************************************
    public static void main(String[] args) {
        launch(args);
    }
}