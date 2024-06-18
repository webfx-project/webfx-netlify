/*
 * Copyright (c) 2020 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.spacefx;

import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.uuid.Uuid;
import dev.webfx.platform.visibility.Visibility;
import dev.webfx.platform.visibility.VisibilityState;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.hansolo.spacefx.Config.*;


public class SpaceFXView extends StackPane {

    private static final Color                      STAR_COLOR              = Color.rgb(255, 255, 255, 0.9);
    private static final boolean                    ENABLE_NEW_VERSION      = true;
    private static final long                       SCREEN_TOGGLE_INTERVAL  = 10_000_000_000L;
    private static final Random                     RND                     = new Random();
    private static final boolean                    IS_BROWSER              = UserAgent.isBrowser();
    private static final Color[] RAINBOW_BLASTER_WAVES_COLORS = {
            whiten(Color.rgb(255, 0,   0,   0.50), 0), // red
            whiten(Color.rgb(255, 165, 0,   0.40), 1), // orange
            whiten(Color.rgb(255, 255, 0,   0.35), 2), // yellow
            whiten(Color.rgb(0,   128, 0,   0.30), 3), // green
            whiten(Color.rgb(0,   255, 255, 0.25), 4), // cyan
            whiten(Color.rgb(0,   0,   255, 0.20), 5)  // blue
    };

    private static Color whiten(Color rainbowColor, int waveIndex) {
        // Whiten color (making outer wave whiter)
        double rainbowFactor = 1.0 + ((0.1 - 1.0) * waveIndex) / 5; // from 1.0 (red => keeps red) to 0.1 (blue => more white)
        double red   = Math.min(1, rainbowColor.getRed()   * rainbowFactor + 1 - rainbowFactor);
        double green = Math.min(1, rainbowColor.getGreen() * rainbowFactor + 1 - rainbowFactor);
        double blue  = Math.min(1, rainbowColor.getBlue()  * rainbowFactor + 1 - rainbowFactor);
        return Color.color(red, green, blue, rainbowColor.getOpacity());
    }

    private              Level1                     level1;
    private              Level2                     level2;
    private              Level3                     level3;
    private              long                       lastScreenToggle;
    private              boolean                    readyToStart;
    private              boolean                    waitUserInteractionBeforePlayingSound = IS_BROWSER;
    private              boolean                    running;
    private              boolean                    gameOverScreen;
    private              boolean                    hallOfFameScreen;
    private              Label                      playerInitialsLabel;
    private              InitialDigit               digit1;
    private              InitialDigit               digit2;
    private              HBox                       playerInitialsDigits;
    private              Button                     saveInitialsButton;
    private              List<Player>               hallOfFame;
    private              VBox                       hallOfFameBox;
    private              Level                      level;
    private              Difficulty                 initialDifficulty = Difficulty.valueOf(PropertyManager.INSTANCE.getDifficultyString("initialDifficulty"));
    private              Difficulty                 minLevelDifficulty;
    private              Difficulty                 levelDifficulty;
    private final        Image                      startImg                = WebFXUtil.newImage("startscreen.jpg");
    private final        Image                      gameOverImg             = WebFXUtil.newImage("gameover.jpg");
    private final        Image                      hallOfFameImg           = WebFXUtil.newImage("halloffamescreen.jpg");
    private              ScaledImage[]              asteroidImages;
    private              ScaledImage                spaceshipImg;
    private              ScaledImage                spaceshipUpImg;
    private              ScaledImage                spaceshipDownImg;
    private              ScaledImage                miniSpaceshipImg;
    private              ScaledImage                deflectorShieldImg;
    private              ScaledImage                miniDeflectorShieldImg;
    private              ScaledImage                torpedoImg;
    private              ScaledImage                bigTorpedoImg;
    private              ScaledImage                bigTorpedo360Img;
    private              ScaledImage                asteroidExplosionImg;
    private              ScaledImage                spaceShipExplosionImg;
    private              ScaledImage                hitImg;
    private              ScaledImage                shieldUpImg;
    private              ScaledImage                lifeUpImg;
    private              ScaledImage                bigTorpedoBonusImg;
    private              ScaledImage                starburstBonusImg;
    private              ScaledImage                starburst360BonusImg;
    private              ScaledImage                rainbowBlasterBonusImg;
    private              ScaledImage                speedUpImg;
    private              ScaledImage                furyBonusImg;
    private              ScaledImage                miniBigTorpedoBonusImg;
    private              ScaledImage                miniStarburstBonusImg;
    private              ScaledImage                miniStarburst360BonusImg;
    private              ScaledImage                miniRainbowBlasterBonusImg;
    private              ScaledImage                miniSpeedUpImg;
    private              ScaledImage                miniFuryBonusImg;
    private              ScaledImage                upExplosionImg;
    private              ScaledImage                rocketExplosionImg;
    private              ScaledImage                rocketImg;
    private              AudioClip                  laserSound;
    private              AudioClip                  rocketLaunchSound;
    private              AudioClip                  rocketExplosionSound;
    private              AudioClip                  enemyLaserSound;
    private              AudioClip                  enemyBombSound;
    private              AudioClip                  asteroidExplosionSound;
    private              AudioClip                  torpedoHitSound;
    private              AudioClip                  spaceShipExplosionSound;
    private              AudioClip                  enemyBossExplosionSound;
    private              AudioClip                  rainbowBlasterSound;
    private              AudioClip                  gameOverSound;
    private              AudioClip                  shieldHitSound;
    private              AudioClip                  enemyHitSound;
    private              AudioClip                  deflectorShieldSound;
    private              AudioClip                  levelBossTorpedoSound;
    private              AudioClip                  levelBossRocketSound;
    private              AudioClip                  levelBossBombSound;
    private              AudioClip                  levelBossExplosionSound;
    private              AudioClip                  shieldUpSound;
    private              AudioClip                  lifeUpSound;
    private              AudioClip                  levelUpSound;
    private              AudioClip                  bonusSound;
    private final        MediaPlayer                gameMusic;
    private final        MediaPlayer                music;
    private              double                     deflectorShieldRadius;
    private              boolean                    levelBossActive;
    private              Font                       scoreFont;
    private              double                     backgroundViewportY;
    private              Canvas                     canvas;
    private              GraphicsContext            ctx;
    private              Star[]                     stars;
    private              Asteroid[]                 asteroids;
    private              SpaceShip                  spaceShip;
    private              SpaceShipExplosion         spaceShipExplosion;
    private              List<Wave>                 waves;
    private              List<Wave>                 wavesToRemove;
    private              List<EnemyBoss>            enemyBosses;
    private              List<LevelBoss>            levelBosses;
    private              List<Bonus>                bonuses;
    private              List<Torpedo>              torpedoes;
    private              List<BigTorpedo>           bigTorpedoes;
    private              List<Rocket>               rockets;
    private              List<EnemyTorpedo>         enemyTorpedoes;
    private              List<EnemyBomb>            enemyBombs;
    private              List<EnemyBossTorpedo>     enemyBossTorpedoes;
    private              List<EnemyBossRocket>      enemyBossRockets;
    private              List<LevelBossTorpedo>     levelBossTorpedoes;
    private              List<LevelBossRocket>      levelBossRockets;
    private              List<LevelBossBomb>        levelBossBombs;
    private              List<LevelBossExplosion>   levelBossExplosions;
    private              List<EnemyBossExplosion>   enemyBossExplosions;
    private              List<EnemyRocketExplosion> enemyRocketExplosions;
    private              List<RocketExplosion>      rocketExplosions;
    private              List<Explosion>            explosions;
    private              List<AsteroidExplosion>    asteroidExplosions;
    private              List<UpExplosion>          upExplosions;
    private              List<Hit>                  hits;
    private              List<EnemyHit>             enemyHits;
    private              RainbowBlasterWaves        rainbowBlasterWaves;
    private              long                       score;
    private              long                       levelKills;
    private              double                     scorePosX;
    private              double                     scorePosY;
    private              double                     mobileOffsetY;
    private              boolean                    hasBeenHit;
    private              int                        noOfLives;
    private              int                        noOfShields;
    private              boolean                    bigTorpedoesEnabled;
    private              boolean                    starburstEnabled;
    private              boolean                    starburst360Enabled;
    private              boolean                    speedUpEnabled;
    private              boolean                    furyEnabled;
    private              boolean                    rainbowBlasterEnabled;
    private              boolean                    rainbowBlasterBonusShowing;
    private              long                       lastShieldActivated;
    private              long                       lastSpeedUpActivated;
    private              long                       lastFuryActivated;
    private              long                       lastStarburstActivated;
    private              long                       lastEnemyBossAttack;
    private              long                       lastShieldUp;
    private              long                       lastLifeUp;
    private              long                       lastWave;
    private              long                       lastBombDropped;
    private              long                       lastTorpedoFired;
    private              long                       lastStarBlast;
    private              long                       lastBigTorpedoBonus;
    private              long                       lastStarburstBonus;
    private              long                       lastRainbowBlasterBonus;
    private              long                       lastSpeedUp;
    private              long                       lastFury;
    private              long                       lastTimerCall;
    private              AnimationTimer             timer;
    private              AnimationTimer             screenTimer;
    private              Circle                     shipTouchArea;
    private              double                     shipTouchGoalX;
    private              double                     shipTouchGoalY;
    private              boolean                    autoFire;
    private              boolean                    gamePaused;
    private              long                       gamePauseNanoTime;
    private              long                       gamePauseNanoDuration;
    private              Text                       difficultyText;
    private              Pane                       incrementDifficultyButton;
    private              Pane                       decrementDifficultyButton;
    private              VBox                       difficultyBox;
    private              Pane                       volumeButton;
    private final        Map<KeyCode, Long>         pressedKeys = new HashMap<>();
    private              boolean                    torpedoArmed;
    private              boolean                    rocketArmed;
    private              boolean                    shieldArmed;

    // ******************** Constructor ***************************************
    public SpaceFXView(Stage stage) {
        gameMusic = newMusic("RaceToMars.mp3");
        music = newMusic("CityStomper.mp3");

        init(stage);
        initOnBackground(stage);

        stage.showingProperty().addListener((p,o,value) -> {
            if(!value) {
                screenTimer.stop();
                timer.stop();
                WebFXUtil.stopMusic(music);
            }
        });

        Pane pane = new Pane(canvas, difficultyBox, volumeButton, shipTouchArea, hallOfFameBox, playerInitialsLabel, playerInitialsDigits, saveInitialsButton) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                layoutInArea(difficultyBox, 0, isRunning() ? 0 : -140 * SCALING_FACTOR, WIDTH, HEIGHT, 0, HPos.CENTER, VPos.TOP);
                layoutInArea(volumeButton, WIDTH / 2 - 6 * SCALING_FACTOR, 37 * SCALING_FACTOR, 0, 0, 0, HPos.CENTER, VPos.TOP);
            }
        };
        pane.setMaxSize(WIDTH, HEIGHT);
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        /*if (SHOW_BUTTONS) {
            canvas.addEventHandler(TouchEvent.TOUCH_PRESSED, touchHandler);
            shipTouchArea.setOnTouchMoved(e -> {
                spaceShip.x = e.getTouchPoint().getX();
                spaceShip.y = e.getTouchPoint().getY();
            });
        } else {*/
        shipTouchArea.setOnMouseDragged(e -> {
            if (!isGamePaused()) {
                shipTouchGoalX = e.getX();
                shipTouchGoalY = e.getY();
                double deltaGoalX = shipTouchGoalX - spaceShip.x;
                double deltaGoalY = shipTouchGoalY - spaceShip.y;
                double biggestDelta = Math.max(Math.abs(deltaGoalX), Math.abs(deltaGoalY));
                if (biggestDelta > 0) {
                    spaceShip.vX = deltaGoalX / biggestDelta * 5 * VELOCITY_FACTOR_X * (speedUpEnabled ? 4 : 1);
                    spaceShip.vY = deltaGoalY / biggestDelta * 5 * VELOCITY_FACTOR_Y * (speedUpEnabled ? 4 : 1);
                }
                setAutoFire(true); // Activating auto fire when using mouse or touch (if not already done)
            }
        });
        // Space shield and rocket fire management
        if (!IS_BROWSER) // The problem with setOnDragDetected() in the browser is that it drags & move the shipTouchArea node
            shipTouchArea.setOnDragDetected(this::mouseFire); // What works the best on other platforms
        else
            shipTouchArea.setOnMouseClicked(this::mouseFire); // Ok for browser
        canvas.setOnMouseClicked(this::mouseFire); // In case the player clicks outside the ship touch area
        //}

        saveInitialsButton.setOnAction(e -> storePlayer());

        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        //StackPane.setAlignment(pane, Pos.CENTER);
        getChildren().add(pane);

        // Start playing background music
        applyGameMusic();

        // Start timer to toggle between start screen and hall of fame
        screenTimer.start();
    }

    public void onKeyPressed(KeyCode keyCode, String keyText) {
        pressedKeys.put(keyCode, System.currentTimeMillis());
        if (isRunning()) {
            if (keyText != null) {
                switch (keyText.toUpperCase()) {
                    case "P" : toggleGamePause(); return;
                    case "A" : toggleAutoFire(); return;
                    case "M" : toggleMuteSound(); return;
                }
            }
            handleGamePressedKeys();
        } else if (isHallOfFameScreen() && saveInitialsButton.isVisible()) {
            switch (keyCode) {
                case UP:
                    if (getDigit1().isSelected()) {
                        getDigit1().up();
                    }
                    if (getDigit2().isSelected()) {
                        getDigit2().up();
                    }
                    break;
                case DOWN:
                    if (getDigit1().isSelected()) {
                        getDigit1().down();
                    }
                    if (getDigit2().isSelected()) {
                        getDigit2().down();
                    }
                    break;
                case LEFT:
                    if (getDigit2().isSelected()) {
                        getDigit1().setSelected(true);
                    }
                    break;
                case RIGHT:
                    if (getDigit1().isSelected()) {
                        getDigit2().setSelected(true);
                    }
                    break;
                case SPACE:
                    storePlayer();
                    break;
            }
        } else if (isStartScreen()) {
            if ("M".equalsIgnoreCase(keyText))
                toggleMuteSound();
            else {
                switch (keyCode) {
                    case UP:
                        increaseDifficulty();
                        break;
                    case DOWN:
                        decreaseDifficulty();
                        break;
                    case SPACE:
                        if (isReadyToStart())
                            startGame();
                        break;
                }
            }
        } else if (!gameOverScreen && keyCode == KeyCode.SPACE && isReadyToStart()) {
            startGame();
        }
    }

    public void onKeyReleased(KeyCode keyCode) {
        pressedKeys.remove(keyCode);
        if (isRunning()) {
            switch (keyCode) {
                case UP:
                case DOWN:
                    stopSpaceShipVy();
                    break;
                case LEFT:
                case RIGHT:
                    stopSpaceShipVx();
                    break;
                case S:
                    shieldArmed = true;
                    break;
                case R:
                    rocketArmed = true;
                    break;
                case SPACE:
                    torpedoArmed = true;
                    break;
            }
        }
    }

    private boolean isKeyPressed(KeyCode key) {
        return pressedKeys.containsKey(key);
    }

    private boolean isKeyPressedAfter(KeyCode key, KeyCode otherKey) {
        Long time = pressedKeys.get(key);
        if (time == null)
            return false;
        Long otherTime = pressedKeys.get(otherKey);
        return otherTime == null || time > otherTime;
    }

    private void handleGamePressedKeys() {
        if (isRunning()) {
            if (isKeyPressedAfter(KeyCode.UP, KeyCode.DOWN)) {
                decreaseSpaceShipVy();
            } else if (isKeyPressedAfter(KeyCode.DOWN, KeyCode.UP)) {
                increaseSpaceShipVy();
            }
            if (isKeyPressedAfter(KeyCode.LEFT, KeyCode.RIGHT)) {
                decreaseSpaceShipVx();
            } else if (isKeyPressedAfter(KeyCode.RIGHT, KeyCode.LEFT)) {
                increaseSpaceShipVx();
            }
            if (isKeyPressed(KeyCode.S) && shieldArmed) {
                activateSpaceShipShield();
                shieldArmed = false;
            }
            if (isKeyPressed(KeyCode.R) && rocketArmed) {
                fireSpaceShipRocket();
                rocketArmed = false;
            }
            if (isKeyPressed(KeyCode.SPACE) && torpedoArmed) {
                fireSpaceShipWeapon();
                torpedoArmed = false;
            }
        }
    }

    // ******************** Methods *******************************************
    public void init(Stage stage) {
        scoreFont        = Fonts.spaceBoy(SCORE_FONT_SIZE);
        running          = false;
        gameOverScreen   = false;
        levelBossActive  = false;
        //lastScreenToggle = System.nanoTime();
        hallOfFameScreen = false;

        playerInitialsLabel = new Label("Type in your initials");
        playerInitialsLabel.setAlignment(Pos.CENTER);
        playerInitialsLabel.setPrefWidth(WIDTH);
        playerInitialsLabel.setTextFill(null); // To allow css color in browser
        Helper.enableNode(playerInitialsLabel, false);

        digit1 = new InitialDigit();
        digit2 = new InitialDigit();
        ToggleGroup toggleGroup = new ToggleGroup();
        digit1.setToggleGroup(toggleGroup);
        digit2.setToggleGroup(toggleGroup);
        digit1.setSelected(true);
        playerInitialsDigits = new HBox(0, digit1, digit2);
        Helper.enableNode(playerInitialsDigits, false);

        saveInitialsButton = new Button("Save Initials");
        saveInitialsButton.setPrefWidth(WIDTH * 0.6);
        saveInitialsButton.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        saveInitialsButton.setTextFill(null); // To allow css color management
        saveInitialsButton.setBorder(null); // To allow css border management
        Helper.enableNode(saveInitialsButton, false);

        // PreFill hall of fame
        //properties = PropertyManager.INSTANCE.getProperties();

        Player p1 = new Player(PropertyManager.INSTANCE.getString("hallOfFame1"));
        Player p2 = new Player(PropertyManager.INSTANCE.getString("hallOfFame2"));
        Player p3 = new Player(PropertyManager.INSTANCE.getString("hallOfFame3"));

        hallOfFame = new ArrayList<>(3);
        hallOfFame.add(p1);
        hallOfFame.add(p2);
        hallOfFame.add(p3);

        HBox p1Entry  = createHallOfFameEntry(p1);
        HBox p2Entry  = createHallOfFameEntry(p2);
        HBox p3Entry  = createHallOfFameEntry(p3);
        hallOfFameBox = new VBox(20, p1Entry, p2Entry, p3Entry);
        hallOfFameBox.setPrefWidth(WIDTH * 0.6);
        hallOfFameBox.setAlignment(Pos.CENTER);
        //hallOfFameBox.setTranslateY(-HEIGHT * 0.1);
        hallOfFameBox.setMouseTransparent(true);
        hallOfFameBox.relocate((WIDTH - hallOfFameBox.getPrefWidth()) * 0.5, (HEIGHT - hallOfFameBox.getPrefHeight()) * 0.5 -HEIGHT * 0.1);
        Helper.enableNode(hallOfFameBox, false);

        difficultyText = new Text();
        difficultyText.setFont(Fonts.spaceBoy(WIDTH / 10));

        incrementDifficultyButton = createSvgButton(
                "M 10.419383,2.7920361 0.44372521,19.200594 c -0.82159289,1.471294 0.2330761,3.327162 1.95783919,3.327162 H 21.793496 c 1.716023,0 2.779433,-1.847128 1.95784,-3.327162 L 14.335061,2.7920361 c -0.847814,-1.5295618 -3.059123,-1.5295618 -3.915678,0 z",
                true, false, this::increaseDifficulty);
        decrementDifficultyButton = createSvgButton(
                "M 10.322413,21.701054 0.34675429,5.2924958 c -0.8215929,-1.471294 0.2330761,-3.327162 1.95783921,-3.327162 H 21.696526 c 1.716023,0 2.779433,1.847127 1.95784,3.327162 L 14.238091,21.701054 c -0.847814,1.529561 -3.059123,1.529561 -3.915678,0 z",
                true, false, this::decreaseDifficulty);
        difficultyBox = new VBox(incrementDifficultyButton, difficultyText, decrementDifficultyButton);
        difficultyBox.setAlignment(Pos.CENTER);
        difficultyText.setOnMouseClicked(e -> {
            userInteracted();
            e.consume(); // Not starting game when clicking on difficulty
        });

        volumeButton = createSvgButton(null,false, true, this::toggleMuteSound);
        displayVolume();

        // background music
        WebFXUtil.setLooping(music, true);

        // for game background music
        WebFXUtil.setLooping(gameMusic, true);

        // Load sounds
        laserSound              = newSound("laserSound.mp3");
        rocketLaunchSound       = newSound("rocketLaunch.mp3");
        rocketExplosionSound    = newSound("rocketExplosion.mp3");
        enemyLaserSound         = newSound("enemyLaserSound.mp3");
        enemyBombSound          = newSound("enemyBomb.mp3");
        asteroidExplosionSound  = newSound("asteroidExplosion.mp3");
        torpedoHitSound         = newSound("hit.mp3");
        spaceShipExplosionSound = newSound("spaceShipExplosionSound.mp3");
        enemyBossExplosionSound = newSound("enemyBossExplosion.mp3");
        gameOverSound           = newSound("gameover.mp3");
        shieldHitSound          = newSound("shieldhit.mp3");
        enemyHitSound           = newSound("enemyBossShieldHit.mp3");
        deflectorShieldSound    = newSound("deflectorshieldSound.mp3");
        levelBossTorpedoSound   = newSound("levelBossTorpedo.mp3");
        levelBossRocketSound    = newSound("levelBossRocket.mp3");
        levelBossBombSound      = newSound("levelBossBomb.mp3");
        levelBossExplosionSound = newSound("explosionSound1.mp3");
        shieldUpSound           = newSound("shieldUp.mp3");
        lifeUpSound             = newSound("lifeUp.mp3");
        levelUpSound            = newSound("levelUp.mp3");
        bonusSound              = newSound("bonus.mp3");
        rainbowBlasterSound     = newSound("rainbowBlasterSound.mp3");

        // Variable initialization
        canvas                        = new Canvas(WIDTH, HEIGHT);
        ctx                           = canvas.getGraphicsContext2D();
        stars                         = new Star[NO_OF_STARS];
        asteroids                     = new Asteroid[NO_OF_ASTEROIDS];
        spaceShipExplosion            = new SpaceShipExplosion(0, 0, 0, 0);
        waves                         = new ArrayList<>();
        wavesToRemove                 = new ArrayList<>();
        enemyBosses                   = new ArrayList<>();
        levelBosses                   = new ArrayList<>();
        bonuses                       = new ArrayList<>();
        rockets                       = new ArrayList<>();
        torpedoes                     = new ArrayList<>();
        bigTorpedoes                  = new ArrayList<>();
        enemyRocketExplosions         = new ArrayList<>();
        explosions                    = new ArrayList<>();
        asteroidExplosions            = new ArrayList<>();
        upExplosions                  = new ArrayList<>();
        enemyTorpedoes                = new ArrayList<>();
        enemyBombs                    = new ArrayList<>();
        enemyBossTorpedoes            = new ArrayList<>();
        enemyBossRockets              = new ArrayList<>();
        levelBossTorpedoes            = new ArrayList<>();
        levelBossRockets              = new ArrayList<>();
        levelBossBombs                = new ArrayList<>();
        levelBossExplosions           = new ArrayList<>();
        enemyBossExplosions           = new ArrayList<>();
        rocketExplosions              = new ArrayList<>();
        hits                          = new ArrayList<>();
        enemyHits                     = new ArrayList<>();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (gamePaused)
                    return;
                handleGamePressedKeys();
                now = gameNanoTime();
                if (now > lastTimerCall) {
                    lastTimerCall = now + FPS_60;
                    updateAndDraw();
                }
                if (now > lastEnemyBossAttack + ENEMY_BOSS_ATTACK_INTERVAL) {
                    spawnEnemyBoss(spaceShip);
                    lastEnemyBossAttack = randomiseEnemyNanoTime(now);
                }
                if (now > lastShieldUp + SHIELD_UP_SPAWN_INTERVAL && noOfShields < NO_OF_SHIELDS) {
                    spawnShieldUp();
                    lastShieldUp = randomiseBonusNanoTime(now);
                }
                if (now > lastLifeUp + LIFE_UP_SPAWN_INTERVAL && noOfLives < NO_OF_LIVES) {
                    spawnLifeUp();
                    lastLifeUp = randomiseBonusNanoTime(now);
                }
                if (now > lastWave + WAVE_SPAWN_INTERVAL && SHOW_ENEMIES) {
                    spawnWave();
                    lastWave = randomiseEnemyNanoTime(now);
                }
                if (now > lastBigTorpedoBonus + BIG_TORPEDO_BONUS_INTERVAL) {
                    spawnBigTorpedoBonus();
                    lastBigTorpedoBonus = randomiseBonusNanoTime(now);
                }
                if (!speedUpEnabled && now > lastSpeedUp + SPEED_UP_BONUS_INTERVAL && levelDifficulty.compareTo(Difficulty.HERO) >= 0) {
                    spawnSpeedUp();
                    lastSpeedUp = randomiseBonusNanoTime(now);
                }
                if (!starburstEnabled && now > lastStarburstBonus + (levelDifficulty.compareTo(Difficulty.NINJA) >= 0 ? 0.8 : 1) * STARBURST_BONUS_INTERVAL) {
                    spawnStarburstBonus(); // Can be starburst 360 if level >= ninja
                    lastStarburstBonus = randomiseBonusNanoTime(now);
                }
                if (!rainbowBlasterEnabled && !rainbowBlasterBonusShowing && now > lastRainbowBlasterBonus + RAINBOW_BLASTER_BONUS_INTERVAL && levelDifficulty.compareTo(Difficulty.JEDI) >= 0) {
                    spawnBlasterBonus();
                    lastRainbowBlasterBonus = randomiseBonusNanoTime(now);
                }
                if (!furyEnabled && now > lastFury + FURY_BONUS_INTERVAL && levelDifficulty.compareTo(Difficulty.NEO) >= 0) {
                    spawnFuryBonus();
                    lastFury = randomiseBonusNanoTime(now);
                }
            }
        };
        screenTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastScreenToggle == 0)
                    lastScreenToggle = now;
                if (!running && now > lastScreenToggle + SCREEN_TOGGLE_INTERVAL) {
                    hallOfFameScreen = !hallOfFameScreen;
                    Helper.enableNode(hallOfFameBox,  hallOfFameScreen);
                    Helper.enableNode(volumeButton,  !hallOfFameScreen);
                    Helper.enableNode(difficultyBox, !hallOfFameScreen);
                    ctx.drawImage(hallOfFameScreen ?  hallOfFameImg : startImg, 0, 0, WIDTH, HEIGHT);
                    lastScreenToggle = now;
                }
            }
        };

        shipTouchArea = new Circle();

        initStars();

        scorePosX = WIDTH * 0.5;
        scorePosY = 40 * SCALING_FACTOR;

        //mobileOffsetY = isIOS() ? 30 : 0;
        mobileOffsetY = 0;

        // Preparing GraphicsContext
        ctx.setFont(scoreFont);
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        WebFXUtil.onImageLoaded(startImg, () -> ctx.drawImage(startImg, 0, 0, WIDTH, HEIGHT));
        Visibility.addVisibilityListener(visibilityState -> {
            if (isRunning()) {
                if (visibilityState == VisibilityState.HIDDEN) {
                    pauseGame();
                } else {
                    resumeGame();
                }
            }
        });
    }

    private void initOnBackground(Stage stage) {
        // Load images
        spaceshipImg            = ScaledImage.create("spaceship.png", 48, 48);
        torpedoImg              = ScaledImage.create("torpedo.png", 17, 20);
        spaceshipUpImg          = ScaledImage.create("spaceshipUp.png", 48, 48);
        spaceshipDownImg        = ScaledImage.create("spaceshipDown.png", 48, 48);
        miniSpaceshipImg        = ScaledImage.create("spaceship.png", 16, 16);
        hitImg                  = ScaledImage.create("torpedoHit2.png", 400, 160);
        asteroidImages          = new ScaledImage[] {
                ScaledImage.create("asteroid1.png", 140, 140),
                ScaledImage.create("asteroid2.png", 140, 140),
                ScaledImage.create("asteroid3.png", 140, 140),
                ScaledImage.create("asteroid4.png", 110, 110),
                ScaledImage.create("asteroid5.png", 100, 100),
                ScaledImage.create("asteroid6.png", 120, 120),
                ScaledImage.create("asteroid7.png", 110, 110),
                ScaledImage.create("asteroid8.png", 100, 100),
                ScaledImage.create("asteroid9.png", 130, 130),
                ScaledImage.create("asteroid10.png", 120, 120),
                ScaledImage.create("asteroid11.png", 140, 140)};
        asteroidExplosionImg       = ScaledImage.create("asteroidExplosion.png", 1024, 896);
        spaceShipExplosionImg      = ScaledImage.create("spaceshipexplosion.png", 800, 600);
        deflectorShieldImg         = ScaledImage.create("deflectorshield.png", 100, 100);
        miniDeflectorShieldImg     = ScaledImage.create("deflectorshield.png", 16, 16);
        bigTorpedoImg              = ScaledImage.create("bigtorpedo.png", 22, 40);
        bigTorpedo360Img           = ScaledImage.create("bigtorpedo360.png", 22, 40);
        shieldUpImg                = ScaledImage.create("shieldUp.png", 50, 50);
        lifeUpImg                  = ScaledImage.create("lifeUp.png", 50, 50);
        bigTorpedoBonusImg         = ScaledImage.create("bigTorpedoBonus.png", 50, 50);
        starburstBonusImg          = ScaledImage.create("starburstBonus.png", 50, 50);
        starburst360BonusImg       = ScaledImage.create("starburst360Bonus.png", 50, 50);
        speedUpImg                 = ScaledImage.create("speedUp.png", 50, 50);
        furyBonusImg               = ScaledImage.create("furyBonus.png", 50, 50);
        rainbowBlasterBonusImg     = ScaledImage.create("rainbowBlasterBonus.png", 50, 50);
        miniBigTorpedoBonusImg     = ScaledImage.create("bigTorpedoBonus.png", 20, 20);
        miniStarburstBonusImg      = ScaledImage.create("starburstBonus.png", 20, 20);
        miniStarburst360BonusImg   = ScaledImage.create("starburst360Bonus.png", 20, 20);
        miniSpeedUpImg             = ScaledImage.create("speedUp.png", 20, 20);
        miniFuryBonusImg           = ScaledImage.create("furyBonus.png", 20, 20);
        miniRainbowBlasterBonusImg = ScaledImage.create("rainbowBlasterBonus.png", 20, 20);
        upExplosionImg             = ScaledImage.create("upExplosion.png", 400, 700);
        rocketExplosionImg         = ScaledImage.create("rocketExplosion.png", 960, 768);
        rocketImg                  = ScaledImage.create("rocket.png", 17, 50);

        // Init levels
        level1 = new Level1();
        level2 = new Level2();
        level3 = new Level3();

        deflectorShieldRadius   = deflectorShieldImg.getWidth() * 0.5;
        spaceShip               = new SpaceShip(spaceshipImg, spaceshipUpImg, spaceshipDownImg);

        initAsteroids();

        shipTouchArea.setCenterX(spaceShip.x);
        shipTouchArea.setCenterY(spaceShip.y);
        shipTouchArea.setRadius(deflectorShieldRadius);
        shipTouchArea.setStroke(Color.TRANSPARENT);
        shipTouchArea.setFill(Color.TRANSPARENT);
        readyToStart = true;

        displayDifficulty();
    }

    private void initStars() {
        for (int i = 0; i < NO_OF_STARS; i++) {
            Star star = new Star();
            star.y = randomDouble() * HEIGHT;
            stars[i] = star;
        }
    }

    private void initAsteroids() {
        for (int i = 0 ; i < NO_OF_ASTEROIDS ; i++) {
            asteroids[i] = new Asteroid(asteroidImages[randomInt(asteroidImages.length)]);
        }
    }

    private static boolean randomBoolean() {
        return RND.nextBoolean();
    }

    private static double randomDouble() {
        return RND.nextDouble();
    }

    private static int randomInt(int bound) {
        return RND.nextInt(bound);
    }

    private static long randomLong(long origin, long bound) {
        //return RND.nextLong(origin, bound); // Not supported by GWT
        return origin + (long) (randomDouble() * (bound - origin));
    }

    private static long randomiseEnemyNanoTime(long nanoTime) {
        return randomLong(nanoTime - 5_000_000_000L, nanoTime + 10_000_000_000L); // up to +/- 5s
    }

    private static long randomiseBonusNanoTime(long nanoTime) {
        return randomLong(nanoTime - 15_000_000_000L, nanoTime + 30_000_000_000L);
    }

    // Update and draw
    private void updateAndDraw() {
        ctx.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw background
        if (SHOW_BACKGROUND) {
            backgroundViewportY -= 0.5;
            if (backgroundViewportY <= 0) {
                backgroundViewportY = SWITCH_POINT;
            }
            ScaledImage backgroundImg = level.getBackgroundImg();
            backgroundImg.drawImage(ctx, 0, backgroundViewportY, 0, 0);
            // On high-res screens, this may not cover the canvas up to the bottom, so we add the background a second time
            backgroundImg.drawImage(ctx, 0, 0, 0, backgroundImg.getHeight() - backgroundViewportY);
        }

        // Draw Stars
        if (SHOW_STARS) {
            ctx.setFill(STAR_COLOR);
            forEach(stars, star -> {
                star.update();
                ctx.fillOval(star.x, star.y, star.size, star.size);
            });
        }

        // Draw Asteroids
        forEach(asteroids, asteroid -> {
            asteroid.update();
            ctx.save();
            ctx.translate(asteroid.cX, asteroid.cY);
            ctx.rotate(asteroid.rot);
            ctx.scale(asteroid.scale, asteroid.scale);
            ctx.translate(-asteroid.imgCenterX, -asteroid.imgCenterY);
            asteroid.drawImage(ctx);
            ctx.restore();

            // Check for rainbow blaster waves hit with enemy boss
            if (isHitRainbowBlasterWavesCircle(asteroid.x, asteroid.y, asteroid.radius)) {
                asteroid.hits = 0;
                onAsteroidHit(asteroid, asteroid.x, asteroid.y, false);
                return;
            }

            // Check for torpedo hits
            forEach(torpedoes, torpedo -> {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, asteroid.cX, asteroid.cY, asteroid.radius)) {
                    torpedo.toBeRemoved = true;
                    asteroid.hits--;
                    onAsteroidHit(asteroid, torpedo.x, torpedo.y, false);
                }
            });

            // Check for bigTorpedo hits
            forEach(bigTorpedoes, bigTorpedo -> {
                if (isHitCircleCircle(bigTorpedo.x, bigTorpedo.y, bigTorpedo.radius, asteroid.cX, asteroid.cY, asteroid.radius)) {
                    bigTorpedo.toBeRemoved = true;
                    asteroid.hits--;
                    onAsteroidHit(asteroid, bigTorpedo.x, bigTorpedo.y, false);
                }
            });

            // Check for rocket hits
            forEach(rockets, rocket -> {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, asteroid.cX, asteroid.cY, asteroid.radius)) {
                    rocket.toBeRemoved = true;
                    asteroid.hits = 0;
                    onAsteroidHit(asteroid, rocket.x, rocket.y, true);
                }
            });

            // Check for spaceship hit
            if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(asteroid.cX, asteroid.cY, asteroid.radius);
                if (hit) {
                    if (spaceShip.shield) {
                        asteroid.hits = 0;
                        onAsteroidHit(asteroid, asteroid.cX, asteroid.cY, false);
                    } else {
                        onSpaceshipHit(); // with asteroid
                    }
                    asteroid.respawn();
                }
            }
        });

        // Draw Wave
        forEach(waves, wave -> {
            if (wave.isRunning) {
                wave.update(ctx);
            } else {
                wavesToRemove.add(wave);
            }
        });
        waves.removeAll(wavesToRemove);

        // Draw Enemy Bosses
        forEach(enemyBosses, enemyBoss -> {
            enemyBoss.update();
            ctx.save();
            ctx.translate(enemyBoss.x - enemyBoss.radius, enemyBoss.y - enemyBoss.radius);
            ctx.save();
            ctx.translate(enemyBoss.radius, enemyBoss.radius);
            ctx.rotate(enemyBoss.r);
            ctx.translate(-enemyBoss.radius, -enemyBoss.radius);
            enemyBoss.drawImage(ctx);
            ctx.restore();
            ctx.restore();

            // Check for rainbow blaster waves hit with enemy boss
            if (isHitRainbowBlasterWavesCircle(enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                enemyBoss.hits = 0;
                onEnemyBossHit(enemyBoss, rainbowBlasterWaves.x, rainbowBlasterWaves.y);
            }

            // Check for torpedo hits with enemy boss
            forEach(torpedoes, torpedo -> {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                    enemyBoss.hits -= TORPEDO_DAMAGE;
                    onEnemyBossHit(enemyBoss, torpedo.x, torpedo.y);
                    torpedo.toBeRemoved = true;
                }
            });

            // Check for big Torpedoes hits with enemy boss
            forEach(bigTorpedoes, bigTorpedo -> {
                if (isHitCircleCircle(bigTorpedo.x, bigTorpedo.y, bigTorpedo.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                    enemyBoss.hits -= BIG_TORPEDO_DAMAGE;
                    onEnemyBossHit(enemyBoss, bigTorpedo.x, bigTorpedo.y);
                    bigTorpedo.toBeRemoved = true;
                }
            });

            // Check for rocket hits with enemy boss
            forEach(rockets, rocket -> {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                    enemyBoss.hits = 0;
                    onEnemyBossHit(enemyBoss, rocket.x, rocket.y);
                    rocket.toBeRemoved = true;
                }
            });

            // Check for spaceship hit with enemy boss
            if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(enemyBoss.x, enemyBoss.y, enemyBoss.radius);
                if (hit) {
                    if (spaceShip.shield) {
                        enemyBossExplosions.add(new EnemyBossExplosion(enemyBoss.x - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.y - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.vX, enemyBoss.vY, 0.5));
                        playSound(enemyBossExplosionSound);
                    } else {
                        onSpaceshipHit(); // with enemy boss
                    }
                    enemyBoss.toBeRemoved = true;
                }
            }
        });

        // Draw LevelBoss
        forEach(levelBosses, levelBoss -> {
            levelBoss.update();
            ctx.save();
            ctx.translate(levelBoss.x - levelBoss.radiusX, levelBoss.y - levelBoss.radiusY);
            ctx.save();
            ctx.translate(levelBoss.radiusX, levelBoss.radiusY);
            ctx.rotate(levelBoss.r);
            ctx.translate(-levelBoss.radiusX, -levelBoss.radiusY);
            levelBoss.drawImage(ctx);
            ctx.restore();
            ctx.restore();

            double lbx = levelBoss.x, lby = levelBoss.y + levelBoss.radiusY - levelBoss.radiusX;

            // Check for rainbow blaster waves hit with level boss
            if (isHitRainbowBlasterWavesCircle(levelBoss.x, levelBoss.y, levelBoss.radius)) {
                levelBoss.hits -= 2;
                onLevelBossHit(levelBoss, levelBoss.x, levelBoss.y);
            }

            // Check for torpedo hits with level boss
            forEach(torpedoes, torpedo -> {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, lbx, lby, levelBoss.radius)) {
                    levelBoss.hits -= TORPEDO_DAMAGE;
                    onLevelBossHit(levelBoss, torpedo.x, torpedo.y);
                    torpedo.toBeRemoved = true;
                }
            });

            // Check for bigTorpedo hits with enemy boss
            forEach(bigTorpedoes, bigTorpedo -> {
                if (isHitCircleCircle(bigTorpedo.x, bigTorpedo.y, bigTorpedo.radius, lbx, lby, levelBoss.radius)) {
                    levelBoss.hits -= BIG_TORPEDO_DAMAGE;
                    onLevelBossHit(levelBoss, bigTorpedo.x, bigTorpedo.y);
                    bigTorpedo.toBeRemoved = true;
                }
            });

            // Check for rocket hits with level boss
            forEach(rockets, rocket -> {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, lbx, lby, levelBoss.radius)) {
                    levelBoss.hits -= ROCKET_DAMAGE;
                    onLevelBossHit(levelBoss, rocket.x, rocket.y);
                    rocket.toBeRemoved = true;
                }
            });

            // Check for spaceship hit with level boss
            if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(levelBoss.x, levelBoss.y, levelBoss.radius);
                if (hit) {
                    boolean levelBossExplodes = false;
                    if (spaceShip.shield) {
                        lastShieldActivated = 0;
                        levelBoss.hits -= SHIELD_DAMAGE;
                        levelBossExplodes = levelBoss.hits <= 0;
                    } else {
                        boolean isGameOver = onSpaceshipHit(); // with level boss
                        if (!isGameOver) {
                            levelBossExplodes = true;
                        }
                    }
                    if (levelBossExplodes) {
                        onLevelBossHit(levelBoss, 0, 0);
                    }
                }
            }
        });

        // Draw Bonuses
        forEach(bonuses, bonus -> {
            bonus.update();
            ctx.save();
            ctx.translate(bonus.cX, bonus.cY);
            ctx.rotate(bonus.rot);
            ctx.translate(-bonus.imgCenterX, -bonus.imgCenterY);
            bonus.drawImage(ctx);
            ctx.restore();

            // Check for spaceship contact
            boolean hit = isHitSpaceshipCircle(bonus.cX, bonus.cY, bonus.radius);
            if (hit) {
                if (bonus instanceof LifeUp) {
                    if (noOfLives <= NO_OF_LIVES - 1) { noOfLives++; }
                    playSound(lifeUpSound);
                } else if (bonus instanceof ShieldUp) {
                    if (noOfShields <= NO_OF_SHIELDS - 1) { noOfShields++; }
                    playSound(shieldUpSound);
                } else if (bonus instanceof BigTorpedoBonus) {
                    bigTorpedoesEnabled = true;
                    playSound(bonusSound);
                } else if (bonus instanceof FuryBonus) {
                    furyEnabled = true;
                    lastFuryActivated = gameNanoTime();
                    playSound(bonusSound);
                } else if (bonus instanceof StarburstBonus) {
                    starburstEnabled = true;
                    starburst360Enabled = ((StarburstBonus) bonus).is360;
                    lastStarburstActivated = gameNanoTime();
                    playSound(bonusSound);
                } else if (bonus instanceof SpeedUp) {
                    speedUpEnabled = true;
                    lastSpeedUpActivated = gameNanoTime();
                    playSound(bonusSound);
                } else if (bonus instanceof BlasterBonus) {
                    rainbowBlasterEnabled = true;
                    rainbowBlasterBonusShowing = false;
                    playSound(shieldUpSound);
                }
                upExplosions.add(new UpExplosion(bonus.cX - UP_EXPLOSION_FRAME_CENTER, bonus.cY - UP_EXPLOSION_FRAME_CENTER, bonus.vX, bonus.vY, 1.0));
                bonus.toBeRemoved = true;
            }
        });

        // Draw Torpedoes
        forEach(torpedoes, torpedo -> {
            torpedo.update();
            torpedo.drawImage(ctx,torpedo.x - torpedo.radius, torpedo.y - torpedo.radius);
        });

        // Draw Big Torpedoes
        forEach(bigTorpedoes, bigTorpedo -> {
            bigTorpedo.update();
            ctx.save();
            ctx.translate(bigTorpedo.x - bigTorpedo.width / 2, bigTorpedo.y - bigTorpedo.height / 2);
            ctx.save();
            ctx.translate(bigTorpedo.width / 2, bigTorpedo.height / 2);
            ctx.rotate(bigTorpedo.r - 45);
            ctx.translate(-bigTorpedo.width / 2, -bigTorpedo.height / 2);
            bigTorpedo.drawImage(ctx);
            ctx.restore();
            ctx.restore();
        });

        // Draw Rockets
        forEach(rockets, rocket -> {
            rocket.update();
            rocket.drawImage(ctx, rocket.x - rocket.halfWidth, rocket.y - rocket.halfHeight);
        });

        // Draw Enemy Torpedoes
        forEach(enemyTorpedoes, enemyTorpedo -> {
            enemyTorpedo.update();
            enemyTorpedo.drawImage(ctx, enemyTorpedo.x, enemyTorpedo.y);
        });

        // Draw Enemy Bombs
        forEach(enemyBombs, enemyBomb -> {
            enemyBomb.update();
            enemyBomb.drawImage(ctx, enemyBomb.x, enemyBomb.y);
        });

        // Draw Enemy Boss Torpedoes
        forEach(enemyBossTorpedoes, enemyBossTorpedo -> {
            enemyBossTorpedo.update();
            enemyBossTorpedo.drawImage(ctx, enemyBossTorpedo.x, enemyBossTorpedo.y);
        });

        // Draw Enemy Boss Rockets
        forEach(enemyBossRockets, enemyBossRocket -> {
            enemyBossRocket.update();
            ctx.save();
            ctx.translate(enemyBossRocket.x - enemyBossRocket.width / 2, enemyBossRocket.y - enemyBossRocket.height / 2);
            ctx.save();
            ctx.translate(enemyBossRocket.width / 2, enemyBossRocket.height / 2);
            ctx.rotate(enemyBossRocket.r);
            ctx.translate(-enemyBossRocket.width / 2, -enemyBossRocket.height / 2);
            enemyBossRocket.drawImage(ctx, 0, 0);
            ctx.restore();
            ctx.restore();
        });

        // Draw Level Boss Torpedoes
        forEach(levelBossTorpedoes, levelBossTorpedo -> {
            levelBossTorpedo.update();
            ctx.save();
            ctx.translate(levelBossTorpedo.x - levelBossTorpedo.width / 2, levelBossTorpedo.y - levelBossTorpedo.height / 2);
            ctx.save();
            ctx.translate(levelBossTorpedo.width / 2, levelBossTorpedo.height / 2);
            ctx.rotate(levelBossTorpedo.r);
            ctx.translate(-levelBossTorpedo.width / 2, -levelBossTorpedo.height / 2);
            levelBossTorpedo.drawImage(ctx, 0, 0);
            ctx.restore();
            ctx.restore();
        });

        // Draw Level Boss Rockets
        forEach(levelBossRockets, levelBossRocket -> {
            levelBossRocket.update();
            ctx.save();
            ctx.translate(levelBossRocket.x - levelBossRocket.width / 2, levelBossRocket.y - levelBossRocket.height / 2);
            ctx.save();
            ctx.translate(levelBossRocket.width / 2, levelBossRocket.height / 2);
            ctx.rotate(levelBossRocket.r);
            ctx.translate(-levelBossRocket.width / 2, -levelBossRocket.height / 2);
            levelBossRocket.drawImage(ctx, 0, 0);
            ctx.restore();
            ctx.restore();
        });

        // Draw Level Boss Bombs
        forEach(levelBossBombs, levelBossBomb -> {
            levelBossBomb.update();
            levelBossBomb.drawImage(ctx, levelBossBomb.x, levelBossBomb.y);
        });

        // Draw Enemy Explosions
        forEach(explosions, explosion -> {
            explosion.update();
            explosion.drawFrame(ctx, explosion.level.getExplosionImg(), EXPLOSION_FRAME_WIDTH, EXPLOSION_FRAME_HEIGHT);
        });

        // Draw Asteroid Explosions
        forEach(asteroidExplosions, asteroidExplosion -> {
            asteroidExplosion.update();
            if (!asteroidExplosion.toBeRemoved) // may happen
                asteroidExplosion.drawFrame(ctx, asteroidExplosionImg, ASTEROID_EXPLOSION_FRAME_WIDTH, ASTEROID_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw Rocket Explosions
        forEach(rocketExplosions, rocketExplosion -> {
            rocketExplosion.update();
            rocketExplosion.drawFrame(ctx, rocketExplosionImg, ROCKET_EXPLOSION_FRAME_WIDTH, ROCKET_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw Enemy Rocket Explosions
        forEach(enemyRocketExplosions, enemyRocketExplosion -> {
            enemyRocketExplosion.update();
            enemyRocketExplosion.drawFrame(ctx, level.getEnemyRocketExplosionImg(), ENEMY_ROCKET_EXPLOSION_FRAME_WIDTH, ENEMY_ROCKET_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw Enemy Boss Explosions
        forEach(enemyBossExplosions, enemyBossExplosion -> {
            enemyBossExplosion.update();
            enemyBossExplosion.drawFrame(ctx, level.getEnemyBossExplosionImg(), ENEMY_BOSS_EXPLOSION_FRAME_WIDTH, ENEMY_BOSS_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw Level Boss Explosions
        forEach(levelBossExplosions, levelBossExplosion -> {
            levelBossExplosion.update();
            levelBossExplosion.drawFrame(ctx, level.getLevelBossExplosionImg(), LEVEL_BOSS_EXPLOSION_FRAME_WIDTH, LEVEL_BOSS_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw Up Explosions
        forEach(upExplosions, upExplosion -> {
            upExplosion.update();
            upExplosion.drawFrame(ctx, upExplosionImg, UP_EXPLOSION_FRAME_WIDTH, UP_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw Hits
        forEach(hits, hit -> {
            hit.update();
            hit.drawFrame(ctx, hitImg, HIT_FRAME_WIDTH, HIT_FRAME_HEIGHT);
        });

        // Draw EnemyBoss Hits
        forEach(enemyHits, hit -> {
            hit.update();
            hit.drawFrame(ctx, level.getEnemyBossHitImg(), ENEMY_HIT_FRAME_WIDTH, ENEMY_HIT_FRAME_HEIGHT);
        });

        // Draw Blaster
        if (rainbowBlasterWaves != null) {
            rainbowBlasterWaves.update();
            if (rainbowBlasterWaves.toBeRemoved)
                rainbowBlasterWaves = null;
            else { // drawing rainbow blaster waves
                ctx.save();
                double x = rainbowBlasterWaves.x;
                double y = rainbowBlasterWaves.y;
                double r = rainbowBlasterWaves.radius; // internal radius of the current rainbow wave to draw
                double w = 20; // width of the current rainbow wave to draw (20 = with of inner red wave)
                for (int i = 0; i < RAINBOW_BLASTER_WAVES_COLORS.length; i++) { // iterating waves from inner to outer
                    ctx.setStroke(RAINBOW_BLASTER_WAVES_COLORS[i]);
                    double ra = r + w / 2; // average radius between inner and outer radius of the current wave
                    ctx.setLineWidth(w);
                    ctx.strokeOval(x - ra, y - ra, 2 * ra, 2 * ra);
                    // Preparing next rainbow wave
                    r += w;
                    w *= 1.4; // increasing factor (outer waves are larger than inner waves)
                    // Rainbow color hue rotating effect (for next animation frame)
                    RAINBOW_BLASTER_WAVES_COLORS[i] = RAINBOW_BLASTER_WAVES_COLORS[i].deriveColor(10, 1, 1, 1);
                }
                ctx.restore();
            }
        }

        // Draw Spaceship, score, lives and shields
        if (hasBeenHit) {
            spaceShipExplosion.update();
            spaceShipExplosion.drawFrame(ctx, spaceShipExplosionImg, SPACESHIP_EXPLOSION_FRAME_WIDTH, SPACESHIP_EXPLOSION_FRAME_HEIGHT, spaceShip.x - SPACESHIP_EXPLOSION_FRAME_CENTER, spaceShip.y - SPACESHIP_EXPLOSION_FRAME_CENTER);
            if (noOfLives > 0)
                spaceShip.respawn();
        }
        if (noOfLives > 0) {
            // Draw Spaceship or it's explosion
            if (!hasBeenHit) {
                // Draw spaceship
                spaceShip.update();

                ctx.save();
                ctx.setGlobalAlpha(spaceShip.isVulnerable ? 1.0 : 0.5);
                if (spaceShip.vY < 0) {
                    spaceshipUpImg.drawImage(ctx,spaceShip.x - spaceShip.radius, spaceShip.y - spaceShip.radius);
                } else if (spaceShip.vY > 0) {
                    spaceshipDownImg.drawImage(ctx,spaceShip.x - spaceShip.radius, spaceShip.y - spaceShip.radius);
                } else {
                    spaceshipImg.drawImage(ctx,spaceShip.x - spaceShip.radius, spaceShip.y - spaceShip.radius);
                }

                ctx.restore();

                long now = gameNanoTime();
                if (spaceShip.shield) {
                    long delta = now - lastShieldActivated;
                    if (delta > DEFLECTOR_SHIELD_TIME) {
                        spaceShip.shield = false;
                        noOfShields--;
                    } else {
                        ctx.setStroke(SPACEFX_COLOR_TRANSLUCENT);
                        ctx.setFill(SPACEFX_COLOR_TRANSLUCENT);
                        ctx.strokeRect(SHIELD_INDICATOR_X, SHIELD_INDICATOR_Y + mobileOffsetY, SHIELD_INDICATOR_WIDTH, SHIELD_INDICATOR_HEIGHT);
                        ctx.fillRect(SHIELD_INDICATOR_X, SHIELD_INDICATOR_Y + mobileOffsetY, SHIELD_INDICATOR_WIDTH - SHIELD_INDICATOR_WIDTH * delta / DEFLECTOR_SHIELD_TIME,
                                     SHIELD_INDICATOR_HEIGHT);
                        ctx.setGlobalAlpha(randomDouble() * 0.5 + 0.1);
                        deflectorShieldImg.drawImage(ctx, spaceShip.x - deflectorShieldRadius, spaceShip.y - deflectorShieldRadius);
                        ctx.setGlobalAlpha(1);
                    }
                }

                if (bigTorpedoesEnabled) {
                    long delta = now - lastBigTorpedoBonus;
                    if (delta > BIG_TORPEDO_TIME) {
                        bigTorpedoesEnabled = false;
                    }
                }

                if (starburstEnabled) {
                    long delta = now - lastStarburstActivated;
                    if (delta > (starburst360Enabled ? STARBURST360_TIME : STARBURST_TIME)) {
                        starburstEnabled = false;
                        if (bigTorpedoesEnabled) { // resetting the bigTorpedo lifespan as it was not used during the starburst
                            if (lastBigTorpedoBonus < lastStarburstActivated)
                                lastBigTorpedoBonus += delta;
                            else
                                lastBigTorpedoBonus = now;
                        }
                        if (furyEnabled) {
                            if (lastFury < lastFuryActivated)
                                lastFuryActivated += delta;
                            else
                                lastFuryActivated = now;
                        }
                    }
                }

                if (furyEnabled && !starburstEnabled) {
                    long delta = now - lastFuryActivated;
                    if (delta > FURY_TIME) {
                        furyEnabled = false;
                    }
                }

                if (speedUpEnabled) {
                    long delta = now - lastSpeedUpActivated;
                    if (delta > SPEED_UP_TIME) {
                        speedUpEnabled = false;
                    }
                }
            }

            // Draw score
            ctx.setFill(SPACEFX_COLOR);
            ctx.setFont(scoreFont);
            ctx.fillText(Long.toString(score), scorePosX, scorePosY + mobileOffsetY);

            // Draw lives
            for (int i = 0; i < noOfLives; i++) {
                miniSpaceshipImg.drawImage(ctx, i * miniSpaceshipImg.getWidth() + 10, 20 + mobileOffsetY);
            }

            // Draw shields
            for (int i = 1 ; i <= noOfShields ; i++) {
                miniDeflectorShieldImg.drawImage(ctx, WIDTH - i * (miniDeflectorShieldImg.getWidth() + 5), 20 + mobileOffsetY);
            }

            // Draw mini bonus icons
            double x = 10;
            if (starburstEnabled) {
                ScaledImage starburstImg = starburst360Enabled ? miniStarburst360BonusImg : miniStarburstBonusImg;
                starburstImg.drawImage(ctx, x, 40 + mobileOffsetY);
                x += starburstImg.getWidth() + 5;
            }
            if (bigTorpedoesEnabled) {
                miniBigTorpedoBonusImg.drawImage(ctx, x, 40 + mobileOffsetY);
                x += miniBigTorpedoBonusImg.getWidth() + 5;
            }
            if (furyEnabled) {
                miniFuryBonusImg.drawImage(ctx, x, 40 + mobileOffsetY);
                x += miniFuryBonusImg.getWidth() + 5;
            }
            if (speedUpEnabled) {
                miniSpeedUpImg.drawImage(ctx, x, 40 + mobileOffsetY);
                x += miniSpeedUpImg.getWidth() + 5;
            }
            if (rainbowBlasterEnabled) {
                miniRainbowBlasterBonusImg.drawImage(ctx, x, 40 + mobileOffsetY);
                x += miniRainbowBlasterBonusImg.getWidth() + 5;
            }
        }

        // Remove sprites
        removeIf(enemyBosses, sprite -> sprite.toBeRemoved);
        removeIf(levelBosses, sprite -> sprite.toBeRemoved);
        removeIf(bonuses, sprite -> sprite.toBeRemoved);
        removeIf(torpedoes, sprite -> sprite.toBeRemoved);
        removeIf(bigTorpedoes, sprite -> sprite.toBeRemoved);
        removeIf(rockets, sprite -> sprite.toBeRemoved);
        removeIf(enemyTorpedoes, sprite -> sprite.toBeRemoved);
        removeIf(enemyBombs, sprite -> sprite.toBeRemoved);
        removeIf(enemyBossTorpedoes, sprite -> sprite.toBeRemoved);
        removeIf(enemyBossRockets, sprite -> sprite.toBeRemoved);
        removeIf(levelBossTorpedoes, sprite -> sprite.toBeRemoved);
        removeIf(levelBossRockets, sprite -> sprite.toBeRemoved);
        removeIf(levelBossBombs, sprite -> sprite.toBeRemoved);
        removeIf(levelBossExplosions, sprite -> sprite.toBeRemoved);
        removeIf(enemyBossExplosions, sprite -> sprite.toBeRemoved);
        removeIf(enemyRocketExplosions, sprite -> sprite.toBeRemoved);
        removeIf(rocketExplosions, sprite -> sprite.toBeRemoved);
        removeIf(explosions, sprite -> sprite.toBeRemoved);
        removeIf(asteroidExplosions, sprite -> sprite.toBeRemoved);
        removeIf(upExplosions, sprite -> sprite.toBeRemoved);
        removeIf(hits, sprite -> sprite.toBeRemoved);
        removeIf(enemyHits, sprite -> sprite.toBeRemoved);

        // Remove waves
        wavesToRemove.clear();
    }

    private boolean onSpaceshipHit() { // returns true if game over
        if (rainbowBlasterEnabled) {
            rainbowBlasterEnabled = false;
            rainbowBlasterWaves = new RainbowBlasterWaves();
            lastRainbowBlasterBonus = randomiseBonusNanoTime(gameNanoTime());
            playSound(rainbowBlasterSound);
        }
        // The spaceship is protected with rainbow blaster waves
        if (rainbowBlasterWaves != null)
            return false;
        spaceShipExplosion.countX = 0;
        spaceShipExplosion.countY = 0;
        shipTouchGoalX            = 0;
        shipTouchGoalY            = 0;
        spaceShipExplosion.x      = spaceShip.x - SPACESHIP_EXPLOSION_FRAME_WIDTH;
        spaceShipExplosion.y      = spaceShip.y - SPACESHIP_EXPLOSION_FRAME_HEIGHT;
        playSound(spaceShipExplosionSound);
        hasBeenHit = true;
        noOfLives--;
        if (0 == noOfLives) {
            gameOver();
            return true;
        }
        return false;
    }

    private void onAsteroidHit(Asteroid asteroid, double x, double y, boolean rocket) {
        if (asteroid.hits <= 0) {
            double explosionScale = 2 * asteroid.scale;
            if (rocket)
                rocketExplosions.add(new RocketExplosion(asteroid.cX - ROCKET_EXPLOSION_FRAME_CENTER * asteroid.scale, asteroid.cY - ROCKET_EXPLOSION_FRAME_CENTER * asteroid.scale, asteroid.vX, asteroid.vY, asteroid.scale));
            else
                asteroidExplosions.add(new AsteroidExplosion(asteroid.cX - ASTEROID_EXPLOSION_FRAME_CENTER * explosionScale, asteroid.cY - ASTEROID_EXPLOSION_FRAME_CENTER * 2 * asteroid.scale, asteroid.vX, asteroid.vY, 2 * asteroid.scale));
            score += asteroid.value;
            asteroid.respawn();
            playSound(asteroidExplosionSound);
        } else {
            hits.add(new Hit(x - HIT_FRAME_CENTER, y - HIT_FRAME_HEIGHT, asteroid.vX, asteroid.vY));
            playSound(torpedoHitSound);
        }
    }

    private void onEnemyBossHit(EnemyBoss enemyBoss, double x, double y) {
        if (enemyBoss.hits <= 0) {
            enemyBossExplosions.add(
                    new EnemyBossExplosion(enemyBoss.x - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.y - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.vX,
                            enemyBoss.vY, 0.5));
            score += enemyBoss.value;
            levelKills++;
            enemyBoss.toBeRemoved = true;
            playSound(enemyBossExplosionSound);
        } else {
            enemyHits.add(new EnemyHit(x - ENEMY_HIT_FRAME_CENTER, y - ENEMY_HIT_FRAME_CENTER, enemyBoss.vX, enemyBoss.vY));
            playSound(enemyHitSound);
        }
    }

    private void onLevelBossHit(LevelBoss levelBoss, double x, double y) {
        if (levelBoss.hits <= 0) {
            levelBossExplosions.add(new LevelBossExplosion(levelBoss.x - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.y - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.vX, levelBoss.vY, 1.0));
            score += levelBoss.value;
            levelBoss.toBeRemoved = true;
            levelBossActive = false;
            levelKills = 0;
            nextLevel();
            playSound(levelBossExplosionSound);
        } else {
            enemyHits.add(new EnemyHit(x - ENEMY_HIT_FRAME_CENTER, y - ENEMY_HIT_FRAME_CENTER, levelBoss.vX, levelBoss.vY));
            playSound(enemyHitSound);
        }
    }

    // Spawn different objects
    private void spawnWeapon(final double x, final double y) {
        if (starburstEnabled) {
            fireStarburst();
        } else if (gameNanoTime() - lastTorpedoFired >= MIN_TORPEDO_INTERVAL) {
            if (bigTorpedoesEnabled) {
                bigTorpedoes.add(new BigTorpedo(bigTorpedoImg, x, y, 0, -BIG_TORPEDO_SPEED * 2.333333, 45));
                playSound(laserSound);
            } else {
                torpedoes.add(new Torpedo(torpedoImg, x, y));
                playSound(laserSound);
            }
            lastTorpedoFired = gameNanoTime();
        }
    }

/*
    private void spawnBigTorpedo(final double x, final double y) {
        bigTorpedoes.add(new BigTorpedo(bigTorpedoImg, x, y, 0, -BIG_TORPEDO_SPEED * 2.333333, 45));
        playSound(laserSound);
    }
*/

    private void spawnRocket(final double x, final double y) {
        rockets.add(new Rocket(rocketImg, x, y));
        playSound(rocketLaunchSound);
    }

    private void spawnEnemyTorpedo(final double x, final double y, final double vX, final double vY, Level waveLevel) {
        double vFactor = ENEMY_TORPEDO_SPEED / Math.sqrt(0.5 * vX * vX + 0.5 * vY * vY); // make sure the speed is always the defined one
        enemyTorpedoes.add(new EnemyTorpedo(waveLevel.getEnemyTorpedoImg(), x, y, vFactor * vX, vFactor * vY));
        playSound(enemyLaserSound);
    }

    private void spawnEnemyBomb(final double x, final double y, Level waveLevel) {
        enemyBombs.add(new EnemyBomb(waveLevel.getEnemyBombImg(), x, y, 0, ENEMY_BOMB_SPEED));
        playSound(enemyBombSound);
    }

    private void spawnEnemyBoss(final SpaceShip spaceShip) {
        if (levelBossActive || !SHOW_ENEMY_BOSS) { return; }
        enemyBosses.add(new EnemyBoss(spaceShip, level.getEnemyBossImg4(), randomBoolean()));
    }

    private void spawnLevelBoss(final SpaceShip spaceShip) {
        if (levelBossActive) { return; }
        levelBossActive = true;
        levelBosses.add(new LevelBoss(spaceShip, level.getLevelBossImg(), true, true));
    }

    private void spawnShieldUp() {
        bonuses.add(new ShieldUp(shieldUpImg));
    }

    private void spawnLifeUp() {
        bonuses.add(new LifeUp(lifeUpImg));
    }

    private void spawnBigTorpedoBonus() {
        bonuses.add(new BigTorpedoBonus(bigTorpedoBonusImg));
    }

    private void spawnStarburstBonus() {
        //if (level.equals(level1)) { return; }
        if (levelDifficulty == Difficulty.EASY) { return; }
        boolean is360 = levelDifficulty.compareTo(Difficulty.NINJA) >= 0 && randomBoolean() || levelDifficulty == Difficulty.NINJA && score < 100;
        bonuses.add(new StarburstBonus(is360 ? starburst360BonusImg : starburstBonusImg, is360));
    }

    private void spawnSpeedUp() {
        bonuses.add(new SpeedUp(speedUpImg));
    }

    private void spawnBlasterBonus() {
        bonuses.add(new BlasterBonus(rainbowBlasterBonusImg));
        rainbowBlasterBonusShowing = true;
    }

    private void spawnFuryBonus() {
        bonuses.add(new FuryBonus(furyBonusImg));
    }

    private void spawnWave() {
        switch (levelDifficulty) {
            case EASY:
                if (levelKills < NO_OF_KILLS_STAGE_1 && !levelBossActive) {
                    waves.add(new Wave(WAVE_TYPES_SLOW[randomInt(WAVE_TYPES_SLOW.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], false, false));
                } else if (levelKills >= NO_OF_KILLS_STAGE_1 && levelKills < NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    if (randomBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_MEDIUM[randomInt(WAVE_TYPES_MEDIUM.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, false));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_SLOW, WaveType.TYPE_11_SLOW, spaceShip, 10, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], false, false));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    spawnLevelBoss(spaceShip);
                } else if (!levelBossActive) {
                    waves.add(new Wave(WAVE_TYPES_MEDIUM[randomInt(WAVE_TYPES_MEDIUM.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, true));
                }
                break;
            case WARRIOR:
                if (levelKills < NO_OF_KILLS_STAGE_1 && !levelBossActive) {
                    if (randomBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_MEDIUM[randomInt(WAVE_TYPES_MEDIUM.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], false, false));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_MEDIUM, WaveType.TYPE_11_MEDIUM, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], false, false));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_1 && levelKills < NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    if (randomBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[randomInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, false));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, false));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    spawnLevelBoss(spaceShip);
                } else if (!levelBossActive) {
                    if (randomBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[randomInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, true));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, false));
                    }
                }
                break;
            default: // HARD, HERO, etc...
                if (levelKills < NO_OF_KILLS_STAGE_1 && !levelBossActive) {
                    if (randomBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[randomInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, false));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, false));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_1 && levelKills < NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    if (randomBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[randomInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, true));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, true));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    spawnLevelBoss(spaceShip);
                } else if (!levelBossActive) {
                    if (randomBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[randomInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, true));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[randomInt(level.getEnemyImages().length)], true, true));
                    }
                }
                break;
        }
    }

    private void spawnEnemyBossTorpedo(final double x, final double y, final double vX, final double vY) {
        double vFactor = ENEMY_BOSS_TORPEDO_SPEED / Math.sqrt(0.5 * vX * vX + 0.5 * vY * vY); // make sure the speed is always the defined one
        enemyBossTorpedoes.add(new EnemyBossTorpedo(level.getEnemyBossTorpedoImg(), x, y, vFactor * vX, vFactor * vY));
        playSound(enemyLaserSound);
    }

    private void spawnEnemyBossRocket(final double x, final double y) {
        enemyBossRockets.add(new EnemyBossRocket(spaceShip, level.getEnemyBossRocketImg(), x, y));
        playSound(rocketLaunchSound);
    }

    private void spawnLevelBossTorpedo(final double x, final double y, final double vX, final double vY, final double r) {
        double vFactor = LEVEL_BOSS_TORPEDO_SPEED / Math.sqrt(0.5 * vX * vX + 0.5 * vY * vY); // make sure the speed is always the defined one
        levelBossTorpedoes.add(new LevelBossTorpedo(level.getLevelBossTorpedoImg(), x, y, vFactor * vX, vFactor * vY, r));
        playSound(levelBossTorpedoSound);
    }

    private void spawnLevelBossRocket(final double x, final double y) {
        levelBossRockets.add(new LevelBossRocket(spaceShip, level.getLevelBossRocketImg(), x, y));
        playSound(levelBossRocketSound);
    }

    private void spawnLevelBossBomb(final double x, final double y) {
        levelBossBombs.add(new LevelBossBomb(level.getLevelBossBombImg(), x, y, 0, LEVEL_BOSS_BOMB_SPEED));
        playSound(levelBossBombSound);
    }

    // Spaceship hit test
    private boolean isHitSpaceshipCircle(final double c2X, final double c2Y, final double c2R) {
        if (spaceShip.shield) {
            return isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, c2X, c2Y, c2R);
        } else {
            return isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, c2X, c2Y, c2R);
        }
    }

    // Blaster wave hit test
    private boolean isHitRainbowBlasterWavesCircle(final double c2X, final double c2Y, final double c2R) {
        return rainbowBlasterWaves != null && isHitCircleCircle(rainbowBlasterWaves.x, rainbowBlasterWaves.y, rainbowBlasterWaves.radius, c2X, c2Y, c2R);
    }

    // Hit test
    private static boolean isHitCircleCircle(final double c1X, final double c1Y, final double c1R, final double c2X, final double c2Y, final double c2R) {
        double distX    = c1X - c2X;
        double distY    = c1Y - c2Y;
        double distance = Math.sqrt((distX * distX) + (distY * distY));
        return (distance <= c1R + c2R);
    }

    // Game Over
    private void gameOver() {
        running = false;
        gameOverScreen = true;

        boolean isInHallOfFame = score > hallOfFame.get(2).score;

        PauseTransition pauseBeforeGameOverScreen = new PauseTransition(Duration.millis(2000));
        pauseBeforeGameOverScreen.setOnFinished(e -> {
            ctx.clearRect(0, 0, WIDTH, HEIGHT);
            ctx.drawImage(gameOverImg, 0, 0, WIDTH, HEIGHT);
            ctx.setFill(SPACEFX_COLOR);
            ctx.setFont(scoreFont);
            ctx.fillText(Long.toString(score), scorePosX, HEIGHT * 0.25);
            timer.stop();
            playSound(gameOverSound);
            if (PLAY_MUSIC)
                WebFXUtil.stopMusic(gameMusic);
        });
        pauseBeforeGameOverScreen.play();

        if (isInHallOfFame) {
            PauseTransition pauseInGameOverScreen = new PauseTransition(Duration.millis(5000));
            pauseInGameOverScreen.setOnFinished(e -> {
                // Add player to hall of fame
                ctx.clearRect(0, 0, WIDTH, HEIGHT);
                ctx.drawImage(hallOfFameImg, 0, 0, WIDTH, HEIGHT);

                hallOfFameScreen = true;
                Helper.enableNode(hallOfFameBox, true);
                Helper.enableNode(playerInitialsLabel, true);
                Helper.enableNode(playerInitialsDigits, true);
                Helper.enableNode(saveInitialsButton, true);
                playerInitialsLabel.relocate((WIDTH - playerInitialsLabel.getPrefWidth()) * 0.5, HEIGHT * 0.7);
                playerInitialsDigits.relocate((WIDTH - digit1.getPrefWidth() - digit2.getPrefWidth()) * 0.5, HEIGHT * 0.8);
                saveInitialsButton.relocate((WIDTH - saveInitialsButton.getPrefWidth()) * 0.5, HEIGHT - saveInitialsButton.getPrefHeight() - HEIGHT * 0.075);
                Platform.runLater(() -> playerInitialsDigits.requestFocus());
            });
            pauseInGameOverScreen.play();
        } else {
            // Back to StartScreen
            PauseTransition pauseInGameOverScreen = new PauseTransition(Duration.millis(5000));
            pauseInGameOverScreen.setOnFinished(a -> reInitGame());
            pauseInGameOverScreen.play();
        }
    }


    // Reinitialize game
    private void reInitGame() {
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        ctx.drawImage(startImg, 0, 0, WIDTH, HEIGHT);

        Helper.enableNode(hallOfFameBox, false);
        Helper.enableNode(volumeButton, true);
        gameOverScreen = false;
        hallOfFameScreen = false;
        bigTorpedoesEnabled = false;
        starburstEnabled = false;
        starburst360Enabled = false;
        rainbowBlasterEnabled = false;
        speedUpEnabled = false;
        furyEnabled = false;
        levelBossActive = false;
        torpedoArmed = true;
        rocketArmed = true;
        shieldArmed = true;
        waves.clear();
        wavesToRemove.clear();
        enemyBosses.clear();
        levelBosses.clear();
        bonuses.clear();
        rockets.clear();
        torpedoes.clear();
        bigTorpedoes.clear();
        enemyRocketExplosions.clear();
        explosions.clear();
        asteroidExplosions.clear();
        upExplosions.clear();
        enemyTorpedoes.clear();
        enemyBombs.clear();
        enemyBossTorpedoes.clear();
        enemyBossRockets.clear();
        levelBossTorpedoes.clear();
        levelBossRockets.clear();
        levelBossBombs.clear();
        levelBossExplosions.clear();
        enemyBossExplosions.clear();
        rocketExplosions.clear();
        hits.clear();
        enemyHits.clear();

        initAsteroids();
        spaceShip.init();
        hasBeenHit  = false;
        noOfLives   = NO_OF_LIVES;
        noOfShields = NO_OF_SHIELDS;
        score       = 0;
        levelKills  = 0;
        applyGameMusic();

        displayDifficulty();
        displayVolume();
        screenTimer.start();
    }

    private void initLevel() {
        minLevelDifficulty = null;
        setLevel(level1);
    }

    private void setLevel(Level level) {
        this.level = level;
        if (!ENABLE_NEW_VERSION)
            levelDifficulty = level.getDifficulty();
        else {
            // Minimal difficulty management
            if (minLevelDifficulty == null) { // happens when initialising the game
                minLevelDifficulty = initialDifficulty; // initial difficulty = easy
                Helper.enableNode(difficultyBox, true);
                displayDifficulty();
            } else if (level == level1) { // returning to level 1 => increasing minimal difficulty
                // Increasing minimal difficulty, unless we already reach the most difficulty level
                increaseDifficulty();
            } else {
                if (level.getDifficulty().compareTo(minLevelDifficulty) > 0) {
                    minLevelDifficulty = level.getDifficulty();
                    displayDifficulty();
                }
            }

            levelDifficulty = minLevelDifficulty;
        }
    }

    private void increaseDifficulty() {
        Difficulty difficulty = isRunning() ? minLevelDifficulty : initialDifficulty;
        Difficulty[] difficulties = Difficulty.values();
        // Increasing minimal difficulty, unless we already reach the most difficulty level
        if (difficulty != difficulties[difficulties.length - 1])
            difficulty = difficulties[difficulty.ordinal() + 1];
        setDifficulty(difficulty);
    }

    private void decreaseDifficulty() {
        Difficulty difficulty = isRunning() ? minLevelDifficulty : initialDifficulty;
        Difficulty[] difficulties = Difficulty.values();
        // Increasing minimal difficulty, unless we already reach the most difficulty level
        if (difficulty != difficulties[0])
            difficulty = difficulties[difficulty.ordinal() - 1];
        setDifficulty(difficulty);
    }

    private void setDifficulty(Difficulty difficulty) {
        boolean changed = minLevelDifficulty != difficulty || initialDifficulty != difficulty;
        if (isRunning())
            minLevelDifficulty = difficulty;
        initialDifficulty = difficulty;
        if (changed) {
            PropertyManager.INSTANCE.set("initialDifficulty", difficulty.toString());
            displayDifficulty();
        }
    }

    private void displayDifficulty() {
        boolean isRunning = isRunning();
        Difficulty difficulty = isRunning ? minLevelDifficulty : initialDifficulty;
        difficultyText.setText(difficulty.name());
        difficultyText.setFill(difficulty.color);
        difficultyText.setEffect(difficulty.color == Color.BLACK ? new DropShadow(30, Color.WHITE) : null);
        double opacity = isRunning ? 0 : 1;
        difficultyBox.setOpacity(opacity);
        incrementDifficultyButton.setOpacity(opacity);
        decrementDifficultyButton.setOpacity(opacity);
        difficultyBox.setMouseTransparent(isRunning);
        difficultyBox.requestLayout(); // In case the vertical position has changed (
        if (isRunning) {
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.seconds(2), new KeyValue(difficultyBox.opacityProperty(), 1)),
                    new KeyFrame(Duration.seconds(5), new KeyValue(difficultyBox.opacityProperty(), 0))
            );
            timeline.play();
        }
        if (!ENABLE_NEW_VERSION)
            Helper.enableNode(difficultyBox, false);
        lastScreenToggle = 0; // resetting the screen toggle (especially when user increased or decreased difficulty)
    }

    // Create Hall of Fame entry
    private HBox createHallOfFameEntry(final Player player) {
        Label playerName  = new Label(player.name);
        playerName.setTextFill(SPACEFX_COLOR);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label playerScore = new Label(Long.toString(player.score));
        playerScore.setTextFill(SPACEFX_COLOR);
        playerScore.setAlignment(Pos.CENTER_RIGHT);

        HBox entry = new HBox(20, playerName, spacer, playerScore);
        entry.setPrefWidth(WIDTH);
        return entry;
    }

    private static MediaPlayer newMusic(String resourceName) {
        return WebFXUtil.newMusic(resourceName);
    }

    private static AudioClip newSound(String resourceName) {
        // Adjusting sounds volume, because they are not all the same level and many are too loud compared to the music
        // especially when there are lots of ennemies.
        double volume;
        switch (resourceName) {
            case "explosionSound.mp3":
            case "hit.mp3":
            case "spaceShipExplosionSound.mp3":
                volume = 0.25;
                break;
            case "laserSound.mp3":
                volume = 0.15;
                break;
            case "asteroidExplosion.mp3":
                volume = 0.35;
                break;
            case "shieldUp.mp3":
            case "rainbowBlasterSound.mp3":
            case "levelUp.mp3":
            case "shieldhit.mp3":
            case "rocketExplosion.mp3":
            case "levelBossBomb.mp3":
            case "explosionSound1.mp3":
            case "enemyBossShieldHit.mp3":
            case "enemyBossExplosion.mp3":
            case "deflectorshieldSound.mp3":
                volume = 1;
                break;
            default:
                volume = 0.5;
        }
        AudioClip sound = WebFXUtil.newSound(resourceName);
        sound.setVolume(volume);
        return sound;
    }

    private void playMusic(MediaPlayer music) {
        if (PLAY_MUSIC && !soundMuted && !waitUserInteractionBeforePlayingSound && !gamePaused)
            WebFXUtil.playMusic(music);
        else
            pauseMusic(music);
    }

    private void pauseMusic(MediaPlayer music) {
        WebFXUtil.pauseMusic(music);
    }

    // Play audio clips
    private void playSound(final AudioClip sound) {
        if (PLAY_SOUND && !soundMuted && !waitUserInteractionBeforePlayingSound && !gamePaused)
            WebFXUtil.playSound(sound);
    }

    private boolean soundMuted = PropertyManager.INSTANCE.getBoolean("soundMuted");

    void muteSound(boolean soundMuted) {
        this.soundMuted = soundMuted;
        displayVolume();
        applyGameMusic();
        PropertyManager.INSTANCE.set("soundMuted", Boolean.toString(soundMuted));
    }

    void toggleMuteSound() {
        // If the music is not initially playing in the browser (because it is waiting for user interaction first), and
        // the user clicks on the volume button, it's probably because he actually wants to turn on the music, and not
        // turn it off! So in that case, the call to userInteracted() should turn on the music like the user expects,
        // and we don't toggle the soundMuted (otherwise this would turn off the music).
        boolean skip = waitUserInteractionBeforePlayingSound && !soundMuted;
        if (waitUserInteractionBeforePlayingSound)
            userInteracted();
        if (!skip)
            muteSound(!soundMuted);
        lastScreenToggle = 0; // resetting the screen toggle (especially when user increased or decreased difficulty)
    }

    private void displayVolume() {
        volumeButton.getChildren().set(0, createSvgPath(
                soundMuted ? "m 42.82353,13.646772 18.82353,22.588237 m 0,-22.588237 -18.82353,22.588237 M 33.411765,2.3526543 17.411764,16.470302 H 2.3529403 V 34.352655 H 17.411764 l 16.000001,14.117648 z"
                        :                                            "m 53.5,5 q 16.409227,19.9254884 0,39.8509784 M 33.411765,2.3526543 17.411764,16.470302 H 2.3529403 V 34.352655 H 17.411764 l 16.000001,14.117648 z"
                , false, true));
        if (!ENABLE_NEW_VERSION)
            Helper.enableNode(volumeButton, false);
    }

    private void applyGameMusic() {
        if (isRunning()) {
            pauseMusic(music);
            playMusic(gameMusic);
        } else {
            pauseMusic(gameMusic);
            playMusic(music);
        }
    }

    private long lastNextLevelTime; // To fix possible multiple shortly calls to nextLevel()
    // Iterate through levels
    private void nextLevel() {
        long now = gameNanoTime();
        if (now > lastNextLevelTime + 10_000_000_000L) { // Waiting at least 10s since last call (because sometimes nextLevel() is called multiple times)
            lastNextLevelTime = now;
            playSound(levelUpSound);
            if (level3.equals(level)) {
                setLevel(level1);
            } else if (level2.equals(level)) {
                setLevel(level3);
            } else if (level1.equals(level)) {
                setLevel(level2);
            }
        }
    }


    // ******************** Public Methods ************************************
    public void startGame() {
        if (gameOverScreen) { return; }
        running = true;
        initLevel();
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        if (SHOW_BACKGROUND) {
            level.getBackgroundImg().drawImage(ctx, 0, 0);
        }
        pauseMusic(music);
        playMusic(gameMusic);
        Helper.enableNode(hallOfFameBox, false);
        Helper.enableNode(volumeButton, false);
        screenTimer.stop();
        long now                      = gameNanoTime();
        score                         = 0;
        levelKills                    = 0;
        hasBeenHit                    = false;
        noOfLives                     = NO_OF_LIVES;
        noOfShields                   = NO_OF_SHIELDS;
        bigTorpedoesEnabled           = false;
        starburstEnabled              = false;
        lastShieldActivated           = 0;
        lastStarburstActivated        = 0;
        lastEnemyBossAttack           = randomiseEnemyNanoTime(now);
        lastShieldUp                  = randomiseBonusNanoTime(now);
        lastLifeUp                    = randomiseBonusNanoTime(now);
        lastWave                      = randomiseEnemyNanoTime(now);
        lastTorpedoFired              = now;
        lastStarBlast                 = now;
        lastBigTorpedoBonus           = randomiseBonusNanoTime(now);
        lastStarburstBonus            = randomiseBonusNanoTime(now);
        lastSpeedUp                   = initialDifficulty != Difficulty.HERO ? randomiseBonusNanoTime(now) : now - SPEED_UP_BONUS_INTERVAL + 5_000_000_000L;
        lastStarburstBonus            = initialDifficulty != Difficulty.NINJA ? randomiseBonusNanoTime(now) : now - STARBURST_BONUS_INTERVAL + 5_000_000_000L;
        lastRainbowBlasterBonus       = initialDifficulty != Difficulty.JEDI ? randomiseBonusNanoTime(now) : now - RAINBOW_BLASTER_BONUS_INTERVAL + 5_000_000_000L;
        lastFury                      = initialDifficulty != Difficulty.NEO ? randomiseBonusNanoTime(now) : now - FURY_BONUS_INTERVAL + 5_000_000_000L;
        backgroundViewportY           = SWITCH_POINT;
        autoFire = false;
        timer.start();
        userInteracted();
    }

    public void userInteracted() {
        if (waitUserInteractionBeforePlayingSound) {
            waitUserInteractionBeforePlayingSound = false;
            applyGameMusic();
        }
    }

    public boolean isReadyToStart() { return readyToStart; }

    public boolean isRunning() { return running; }

    public boolean isHallOfFameScreen() { return hallOfFameScreen; }

    public boolean isStartScreen() { return !running && !hallOfFameScreen && !gameOverScreen; }

    public void increaseSpaceShipVx() { spaceShip.vX = 5 * VELOCITY_FACTOR_X; }
    public void decreaseSpaceShipVx() { spaceShip.vX = -5 * VELOCITY_FACTOR_X; }
    public void stopSpaceShipVx() { spaceShip.vX = 0; }

    public void increaseSpaceShipVy() { spaceShip.vY = 5 * VELOCITY_FACTOR_Y; }
    public void decreaseSpaceShipVy() { spaceShip.vY = -5 * VELOCITY_FACTOR_Y; }
    public void stopSpaceShipVy() { spaceShip.vY = 0; }

    public void activateSpaceShipShield() {
        if (noOfShields > 0 && !spaceShip.shield) {
            lastShieldActivated = gameNanoTime();
            spaceShip.shield = true;
            playSound(deflectorShieldSound);
        }
    }

    public void fireSpaceShipRocket() {
        // Max 3 rockets at the same time -- Only 1 rocket in auto fire (otherwise too easy) except when level boss fired torpedoes
        if (rockets.size() < MAX_NO_OF_ROCKETS + (autoFire && !spaceShip.shield && levelBossTorpedoes.isEmpty() ? -2 : 0)) {
            spawnRocket(spaceShip.x, spaceShip.y);
        }
    }

    private Scheduled autoFireScheduled;

    public void fireSpaceShipWeapon() {
        if (autoFireScheduled != null)
            autoFireScheduled.cancel();
        if (gamePaused)
            return;
        spawnWeapon(spaceShip.x, spaceShip.y);
        // Auto firing rockets when autoFire is on and levelBoss has fired rockets and torpedo
        if (autoFire && (spaceShip.shield || !levelBossRockets.isEmpty() || !levelBossTorpedoes.isEmpty()))
            fireSpaceShipRocket();
        if (autoFire && isRunning())
            autoFireScheduled = Scheduler.scheduleDelay( (furyEnabled && !starburstEnabled ? FURY_AUTO_FIRE_TORPEDO_INTERVAL : AUTO_FIRE_TORPEDO_INTERVAL) / 1_000_000L, this::fireSpaceShipWeapon);
    }

    public void setAutoFire(boolean autoFire) {
        if (this.autoFire != autoFire) {
            this.autoFire = autoFire;
            if (autoFire && isRunning())
                fireSpaceShipWeapon();
        }
    }

    public void toggleAutoFire() {
        setAutoFire(!autoFire);
    }

    public void mouseFire(MouseEvent e) {
        if (isRunning() && score > 0 && !isGamePaused() && gameNanoTime() > spaceShip.born + SpaceShip.INVULNERABLE_TIME / 2) {
            activateSpaceShipShield();
            fireSpaceShipRocket();
        }
    }

    public void fireStarburst() {
        if (!starburstEnabled/* || (gameNanoTime() - lastStarBlast < MIN_STARBURST_INTERVAL)*/) { return; }
        double offset    = Math.toRadians(-135);
        double angleStep = Math.toRadians(22.5);
        double angle     = 0;
        double x         = spaceShip.x;
        double y         = spaceShip.y;
        double vX;
        double vY;
        int n = starburst360Enabled ? 16 : 5;
        for (int i = 0 ; i < n ; i++) {
            vX = BIG_TORPEDO_SPEED * Math.cos(offset + angle);
            vY = BIG_TORPEDO_SPEED * Math.sin(offset + angle);
            bigTorpedoes.add(new BigTorpedo(starburst360Enabled ? bigTorpedo360Img : bigTorpedoImg, x, y, vX * BIG_TORPEDO_SPEED, vY * BIG_TORPEDO_SPEED, Math.toDegrees(angle)));
            angle += angleStep;
        }
        lastStarBlast = gameNanoTime();
        playSound(laserSound);
    }

    public InitialDigit getDigit1() { return digit1; }
    public InitialDigit getDigit2() { return digit2; }

    public void storePlayer() {
        hallOfFame.add(new Player((digit1.getCharacter() + digit2.getCharacter()), score));
        Collections.sort(hallOfFame);
        hallOfFame = hallOfFame.stream().limit(3).collect(Collectors.toList());

        // Store hall of fame in properties
        PropertyManager.INSTANCE.set("hallOfFame1", hallOfFame.get(0).toPropertyString());
        PropertyManager.INSTANCE.set("hallOfFame2", hallOfFame.get(1).toPropertyString());
        PropertyManager.INSTANCE.set("hallOfFame3", hallOfFame.get(2).toPropertyString());
        PropertyManager.INSTANCE.storeProperties();

        HBox p1Entry = createHallOfFameEntry(new Player(PropertyManager.INSTANCE.getString("hallOfFame1")));
        HBox p2Entry = createHallOfFameEntry(new Player(PropertyManager.INSTANCE.getString("hallOfFame2")));
        HBox p3Entry = createHallOfFameEntry(new Player(PropertyManager.INSTANCE.getString("hallOfFame3")));
        hallOfFameBox.getChildren().setAll(p1Entry, p2Entry, p3Entry);
        hallOfFameBox.relocate((WIDTH - hallOfFameBox.getPrefWidth()) * 0.5, (HEIGHT - hallOfFameBox.getPrefHeight()) * 0.5);

        Helper.enableNode(playerInitialsLabel, false);
        Helper.enableNode(playerInitialsDigits, false);
        Helper.enableNode(saveInitialsButton, false);

        PauseTransition waitForHallOfFame = new PauseTransition(Duration.millis(3000));
        waitForHallOfFame.setOnFinished(a -> reInitGame());
        waitForHallOfFame.play();
    }


    // ******************** Space Object Classes ******************************
    private abstract class Sprite {
        protected final Random rnd;
        public          ScaledImage   image;
        public          double  x;
        public          double  y;
        public          double  r;
        public          double  vX;
        public          double  vY;
        public          double  vR;
        public          double  width;
        public          double  height;
        public          double  size;
        public          double  radius, radiusX, radiusY; // radiusX & radiusY have been added for LevelBoss images which are not square
        public          boolean toBeRemoved;

        public Sprite() {
            this(null, 0, 0, 0, 0, 0, 0);
        }
        public Sprite(final ScaledImage image) {
            this(image, 0, 0, 0, 0, 0, 0);
        }
        public Sprite(final ScaledImage image, final double x, final double y) {
            this(image, x, y, 0, 0, 0, 0);
        }
        public Sprite(final ScaledImage image, final double x, final double y, final double vX, final double vY) {
            this(image, x, y, 0, vX, vY, 0);
        }
        public Sprite(final ScaledImage image, final double x, final double y, final double r, final double vX, final double vY) {
            this(image, x, y, r, vX, vY, 0);
        }
        public Sprite(final ScaledImage image, final double x, final double y, final double r, final double vX, final double vY, final double vR) {
            this.rnd         = new Random();
            this.image       = image;
            this.x           = x;
            this.y           = y;
            this.r           = r;
            this.vX          = vX;
            this.vY          = vY;
            this.vR          = vR;
            if (image != null)
                computeImageSizeDependentFields();
            this.toBeRemoved = false;
        }

        protected void computeImageSizeDependentFields() {
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
            }
            size = Math.max(width, height);
            radius = size * 0.5;
            radiusX = width / 2;
            radiusY = height / 2;
            WebFXUtil.onImageLoadedIfLoading(image.getImage(), () -> {
                computeImageSizeDependentFields();
                update();
            });
        }

        protected void init() {}

        public void respawn() {}

        public abstract void update();

        public void drawImage(GraphicsContext ctx) {
            image.drawImage(ctx);
        }

        public void drawImage(GraphicsContext ctx, double x, double y) {
            image.drawImage(ctx, x, y);
        }
    }

    private abstract class AnimatedSprite extends Sprite {
        protected final int    maxFrameX;
        protected final int    maxFrameY;
        protected       double scale;
        protected       int    countX;
        protected       int    countY;

        public AnimatedSprite(final double x, final double y, final double vX, final double vY, final int maxFrameX, final int maxFrameY, final double scale) {
            this(x, y, 0, vX, vY, 0, maxFrameX, maxFrameY, scale);
        }

        public AnimatedSprite(final double x, final double y, final double r, final double vX, final double vY, final double vR, final int maxFrameX, final int maxFrameY, final double scale) {
            super(null, x, y, r, vX, vY, vR);
            this.maxFrameX = maxFrameX;
            this.maxFrameY = maxFrameY;
            this.scale     = scale;
            this.countX    = 0;
            this.countY    = 0;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                    countY = 0;
                }
                countX = 0;
            }
        }

        public void drawFrame(GraphicsContext ctx, ScaledImage scaledImage, double frameWidth, double frameHeight) {
            drawFrame(ctx, scaledImage, frameWidth, frameHeight, x, y);
        }

        public void drawFrame(GraphicsContext ctx, ScaledImage scaledImage, double frameWidth, double frameHeight, double x, double y) {
            scaledImage.drawFrame(ctx, frameWidth, frameHeight, countX, countY, x, y, scale);
        }
    }

    private abstract class Bonus extends Sprite {
        protected final double  xVariation   = 2;
        protected final double  minSpeedY    = 2;
        protected final double  minRotationR = 0.1;
        protected       double  imgCenterX;
        protected       double  imgCenterY;
        protected       double  cX;
        protected       double  cY;
        protected       double  rot;
        protected       boolean rotateRight;
        protected       double  vYVariation;

        public Bonus(final ScaledImage image) {
            super(image);
        }
    }

    private class Star {
        private final double xVariation = 0;
        private final double minSpeedY  = 4;
        private       double x;
        private       double y;
        private final double size;
        private final double vX;
        private final double vY;


        public Star() {
            // Random size
            Random rnd = new Random();
            size = rnd.nextInt(2) + 1;

            // Position
            x = (int)(rnd.nextDouble() * WIDTH);
            y = -size;

            // Random Speed
            double vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            // Velocity
            vX = (int) (Math.round((rnd.nextDouble() * xVariation) - xVariation * 0.5));
            vY = (int) (Math.round(((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation));
        }


        private void respawn() {
            x = (int) (randomDouble() * WIDTH);
            y = -size;
        }

        private void update() {
            x += vX;
            y += vY;

            // Respawn star
            if(y > HEIGHT + size) {
                respawn();
            }
        }
    }

    private class RainbowBlasterWaves {
        private double radius = spaceShip.radius / 2;
        private final double x = spaceShip.x;
        private final double y = spaceShip.y;
        private double factor = 0.2;
        private boolean toBeRemoved;

        private void update() {
            factor = factor * 0.96;
            radius *= (1 + factor);
            if (y - radius < -0.25 * HEIGHT && y + radius > 1.25 * HEIGHT)
                toBeRemoved = true;
        }
    }

    private class Player implements Comparable<Player> {
        private final String id;
        private       String name;
        private       Long   score;


        public Player(final String propertyString) {
            this(propertyString.isEmpty() ? null : propertyString.split(",")[0], propertyString.isEmpty() ? "--" : propertyString.split(",")[1], propertyString.isEmpty() ? 0 : Long.valueOf(propertyString.split(",")[2]));
        }
        public Player(final String name, final Long score) {
            this(Uuid.randomUuid(), name, score);
        }
        public Player(final String id, final String name, final Long score) {
            this.id    = id;
            this.name  = name;
            this.score = score;
        }


        @Override public int compareTo(final Player player) {
            return Long.compare(player.score, this.score);
        }

        public String toPropertyString() {
            return this.id + "," + this.name + "," + this.score;
        }

        @Override public String toString() {
            return "{ " +
                   "\"id\"" + ":" + id + "," +
                   "\"name\"" + ":" + name + "," +
                   "\"score\"" + ":" + score +
                   " }";
        }
    }

    private class Wave {
        private final        WaveType     waveType1;
        private final        WaveType     waveType2;
        private final        SpaceShip    spaceShip;
        private final        int          noOfEnemies;
        private final        int          noOfSmartEnemies;
        private final        ScaledImage        image;
        private final        boolean      canFire;
        private final        boolean      canBomb;
        private final        List<Enemy>  enemies;
        private final        List<Enemy>  smartEnemies;
        private              int          enemiesSpawned;
        private              long         lastEnemySpawned;
        private              boolean      alternateWaveType;
        private              boolean      toggle;
        private              boolean      isRunning;
        public final         Level        level = SpaceFXView.this.level;

        public Wave(final WaveType waveType, final SpaceShip spaceShip, final int noOfEnemies, final ScaledImage image, final boolean canFire, final boolean canBomb) {
            this(waveType, null, spaceShip, noOfEnemies, image, canFire, canBomb);
        }

        public Wave(final WaveType waveType1, final WaveType waveType2, final SpaceShip spaceShip, final int noOfEnemies, final ScaledImage image, final boolean canFire, final boolean canBomb) {
            if (null == waveType1) { throw new IllegalArgumentException("You need at least define waveType1."); }
            this.waveType1         = waveType1;
            this.waveType2         = waveType2;
            this.spaceShip         = spaceShip;
            this.noOfEnemies       = noOfEnemies;
            this.noOfSmartEnemies  = levelDifficulty.noOfSmartEnemies;
            this.image             = image;
            this.canFire           = canFire;
            this.canBomb           = canBomb;
            this.enemies           = new ArrayList<>(noOfEnemies);
            this.smartEnemies      = new ArrayList<>();
            this.enemiesSpawned    = 0;
            this.alternateWaveType = waveType2 != null;
            this.toggle            = true;
            this.isRunning         = true;
        }

        public void update(final GraphicsContext ctx) {
            if (isRunning) {
                if (enemiesSpawned < noOfEnemies && gameNanoTime() - lastEnemySpawned > ENEMY_SPAWN_INTERVAL) {
                    Enemy enemy = spawnEnemy();
                    if (smartEnemies.size() < levelDifficulty.noOfSmartEnemies && randomBoolean()) {
                        smartEnemies.add(enemy);
                    }
                    lastEnemySpawned = gameNanoTime();
                }

                forEach(enemies, enemy -> {
                    if (level.getIndex() > 1 &&
                        !enemy.smart &&
                        enemy.frameCounter > waveType1.totalFrames * 0.35 &&
                        smartEnemies.contains(enemy)) {
                        enemy.smart = randomBoolean();
                    }

                    enemy.update();

                    ctx.save();
                    ctx.translate(enemy.x - enemy.radius, enemy.y - enemy.radius);
                    ctx.save();
                    ctx.translate(enemy.radius, enemy.radius);
                    ctx.rotate(enemy.r);
                    ctx.translate(-enemy.radius, -enemy.radius);
                    enemy.drawImage(ctx, 0, 0);
                    ctx.restore();
                    ctx.restore();

                    // Check for rainbow blaster waves hit
                    if (isHitRainbowBlasterWavesCircle(enemy.x, enemy.y, enemy.radius)) {
                        onEnemyHit(enemy, false);
                    }

                    // Check for torpedo hits
                    forEach(torpedoes, torpedo -> {
                        if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, enemy.x, enemy.y, enemy.radius)) {
                            onEnemyHit(enemy, false);
                            torpedo.toBeRemoved = true;
                        }
                    });

                    // Check for bigTorpedo hits
                    forEach(bigTorpedoes, bigTorpedo -> {
                        if (isHitCircleCircle(bigTorpedo.x, bigTorpedo.y, bigTorpedo.radius, enemy.x, enemy.y, enemy.radius)) {
                            onEnemyHit(enemy, false);
                            bigTorpedo.toBeRemoved = true;
                        }
                    });

                    // Check for rocket hits
                    forEach(rockets, rocket -> {
                        if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, enemy.x, enemy.y, enemy.radius)) {
                            onEnemyHit(enemy, true);
                            rocket.toBeRemoved = true;
                        }
                    });

                    // Check for spaceship hit
                    if (spaceShip.isVulnerable && !hasBeenHit) {
                        boolean hit = isHitSpaceshipCircle(enemy.x, enemy.y, enemy.radius);
                        if (hit) {
                            if (spaceShip.shield) {
                                explosions.add(new Explosion(enemy.x - EXPLOSION_FRAME_CENTER * 0.35, enemy.y - EXPLOSION_FRAME_CENTER * 0.35, enemy.vX, enemy.vY, 0.35, enemy.waveLevel));
                                playSound(spaceShipExplosionSound);
                            } else {
                                onSpaceshipHit(); // with enemy
                            }
                            enemy.toBeRemoved = true;
                        }
                    }
                });

                removeIf(enemies, enemy -> enemy.toBeRemoved);
                if (enemies.isEmpty() && enemiesSpawned == noOfEnemies) { isRunning = false; }
            }
        }

        private void onEnemyHit(Enemy enemy, boolean rocket) {
            score += enemy.value;
            levelKills++;
            enemy.toBeRemoved = true;
            if (rocket) {
                rocketExplosions.add(new RocketExplosion(enemy.x - EXPLOSION_FRAME_CENTER * 0.5, enemy.y - EXPLOSION_FRAME_CENTER * 0.5, enemy.vX, enemy.vY, 0.5));
                playSound(rocketExplosionSound);
            } else {
                explosions.add(new Explosion(enemy.x - EXPLOSION_FRAME_CENTER * 0.35, enemy.y - EXPLOSION_FRAME_CENTER * 0.35, enemy.vX, enemy.vY, 0.35, enemy.waveLevel));
                playSound(spaceShipExplosionSound);
            }
        }

        private Enemy spawnEnemy() {
            Enemy enemy;
            if (alternateWaveType) {
                enemy = new Enemy(toggle ? waveType1 : waveType2, spaceShip, image, canFire, canBomb, level);
            } else {
                enemy = new Enemy(waveType1, spaceShip, image, canFire, canBomb, level);
            }
            toggle = !toggle;
            enemies.add(enemy);
            enemiesSpawned++;
            return enemy;
        }
    }


    // ******************** Sprites *******************************************
    private class SpaceShip extends Sprite {
        private static final long      INVULNERABLE_TIME = 3_000_000_000L;
        private        final ScaledImage     imageUp;
        private        final ScaledImage     imageDown;
        private              long      born;
        private              boolean   shield;
        public               boolean   isVulnerable;


        public SpaceShip(final ScaledImage image, final ScaledImage imageUp, final ScaledImage imageDown) {
            super(image);
            this.imageUp   = imageUp;
            this.imageDown = imageDown;
            init();
        }

        @Override protected void init() {
            this.born         = gameNanoTime();
            this.x            = WIDTH * 0.5;
            computeImageSizeDependentFields();
            this.vX           = 0;
            this.vY           = 0;
            this.shield       = false;
            this.isVulnerable = false;
        }

        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            this.y            = HEIGHT - 2 * height;
        }

        @Override public void respawn() {
            this.vX           = 0;
            this.vY           = 0;
            this.shield       = false;
            this.born         = gameNanoTime();
            this.isVulnerable = false;
        }

        @Override public void update() {
            if (!isVulnerable && gameNanoTime() - born > INVULNERABLE_TIME) {
                isVulnerable = true;
            }
            x += vX;
            y += vY;
            if (x + width * 0.5 > WIDTH) {
                x = WIDTH - width * 0.5;
            }
            if (x - width * 0.5 < 0) {
                x = width * 0.5;
            }
            if (y + height * 0.5 > HEIGHT) {
                y = HEIGHT - height * 0.5;
            }
            if (y - height * 0.5 < 0) {
                y = height * 0.5;
            }
            if (shipTouchGoalX > 0 || shipTouchGoalY > 0) {
                if (vX > 0 && x > shipTouchGoalX || vX < 0 && x < shipTouchGoalX) {
                    x = shipTouchGoalX;
                    vX = 0;
                    shipTouchGoalX = 0;
                }
                if (vY > 0 && y > shipTouchGoalY || vY < 0 && y < shipTouchGoalY) {
                    y = shipTouchGoalY;
                    vY = 0;
                    shipTouchGoalY = 0;
                }
            }
            shipTouchArea.setCenterX(x);
            shipTouchArea.setCenterY(y);
        }
    }

    private class Asteroid extends Sprite {
        private static final int     MAX_VALUE      = 10;
        private final        Random  rnd            = new Random();
        private final        double  xVariation     = 2;
        private final        double  minSpeedY      = 2;
        private final        double  minRotationR   = 0.1;
        private              double  imgCenterX;
        private              double  imgCenterY;
        private              double  radius;
        private              double  cX;
        private              double  cY;
        private              double  rot;
        private              boolean rotateRight;
        private              double  scale;
        private              double  vYVariation;
        private              int     value;
        private              int     hits;

        public Asteroid(final ScaledImage image) {
            super(image);
            init();
        }

        @Override protected void init() {
            // Position
            x   = rnd.nextDouble() * WIDTH;
            y   = -image.getHeight();
            rot = 0;

            // Random Size
            scale = (rnd.nextDouble() * 0.4) + 0.4; // 0.4 - 0.8

            // Value
            value = (int) (1 / scale * MAX_VALUE);

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            computeImageSizeDependentFields();

            // No of hits (1 - 3) depending on asteroid size
            hits = (int) (size / (140 * 0.8 * SCALING_FACTOR /* max size */) * 4);

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            // Velocity
            vX          = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            vY          = (((rnd.nextDouble() * 1.5) + minSpeedY * 1/scale) * vYVariation) * VELOCITY_FACTOR_Y;
            vR          = ((rnd.nextDouble() * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            width      = image.getWidth() * scale;
            height     = image.getHeight() * scale;
            size       = Math.max(width, height);
            radius     = size * 0.5;
            imgCenterX = width * 0.5;
            imgCenterY = height * 0.5;
        }

        @Override public void respawn() {
            this.image = asteroidImages[randomInt(asteroidImages.length)];
            init();
        }

        @Override public void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
            } else {
                rot -= vR;
            }
            rot = (rot + 360) % 360;

            // Respawn asteroid
            if(x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                respawn();
            }
        }
    }

    private class Torpedo extends Sprite {

        public Torpedo(final ScaledImage image, final double x, final double y) {
            super(image, x, y - image.getHeight(), 0, TORPEDO_SPEED);
        }

        @Override public void update() {
            y -= vY;
            if (y < -size) {
                toBeRemoved = true;
            }
        }
    }

    private class BigTorpedo extends Sprite {

        public BigTorpedo(final ScaledImage image, final double x, final double y, final double vX, final double vY, final double r) {
            super(image, x, y, r, vX, vY);
        }

        @Override public void update() {
            x += vX;
            y += vY;
            if (x < -width || x > WIDTH + width || y < -height || y > HEIGHT + height) {
                toBeRemoved = true;
            }
        }
    }

    private class Rocket extends Sprite {
        public double halfWidth;
        public double halfHeight;

        public Rocket(final ScaledImage image, final double x, final double y) {
            super(image, x, y - image.getWidth(), 0, ROCKET_SPEED);
            halfWidth  = width * 0.5;
            halfHeight = height * 0.5;
        }

        @Override public void update() {
            y -= vY;
            if (y < -size) {
                toBeRemoved = true;
            }
        }
    }

    private class Enemy extends Sprite {
        public static final  long      TIME_BETWEEN_SHOTS  = 500_000_000L;
        public static final  long      TIME_BETWEEN_BOMBS  = 1_000_000_000L;
        public static final  double    HALF_ANGLE_OF_SIGHT = 5;
        private static final double    BOMB_RANGE          = 10;
        private static final int       MAX_VALUE           = 50;
        private final        WaveType  waveType;
        public               int       frameCounter;
        private              SpaceShip spaceShip;
        public               boolean   canFire;
        public               boolean   canBomb;
        public               boolean   smart;
        private              int       noOfBombs;
        private              double    oldX;
        private              double    oldY;
        private              double    dX;
        private              double    dY;
        private              double    dist;
        private              double    factor;
        public               int       value;
        public               long      lastShot;
        public               long      lastBomb;
        public               boolean   toBeRemoved;
        private final        Level     waveLevel; // Capturing enemy level for getting the right explosion image


        public Enemy(final WaveType waveType, final SpaceShip spaceShip, final ScaledImage image, final boolean canFire, final boolean canBomb, Level waveLevel) {
            this(waveType, spaceShip, image, canFire, canBomb, false, waveLevel);
        }

        public Enemy(final WaveType waveType, final SpaceShip spaceShip, final ScaledImage image, final boolean canFire, final boolean canBomb, final boolean smart, Level waveLevel) {
            super(image);
            this.waveType     = waveType;
            this.frameCounter = 0;
            this.spaceShip    = spaceShip;
            this.canFire      = canFire;
            this.canBomb      = canBomb;
            this.noOfBombs    = NO_OF_ENEMY_BOMBS;
            this.toBeRemoved  = false;
            this.smart        = smart;
            this.waveLevel    = waveLevel;
            init();
        }

        @Override protected void init() {
            x    = waveType.coordinates.get(0).x;
            y    = waveType.coordinates.get(0).y;
            r    = waveType.coordinates.get(0).r;
            oldX = x;
            oldY = y;

            // Value
            value = rnd.nextInt(MAX_VALUE) + 1;

            computeImageSizeDependentFields();

            // Velocity
            vX = 0;
            vY = 1;

            lastShot = gameNanoTime();
        }

        @Override public void update() {
            if (toBeRemoved) { return; }
            oldX = x;
            oldY = y;
            if (smart) {
                dX     = spaceShip.x - x;
                dY     = spaceShip.y - y;
                dist   = Math.sqrt(dX * dX + dY * dY);
                factor = ENEMY_SPEED / dist;
                if (spaceShip.isVulnerable && spaceShip.y > y && y < OUT_OF_SENSING_HEIGHT) {
                    vX = dX * factor;
                    vY = dY * factor;
                }
                x += vX;
                y += vY;
                r = Math.toDegrees(Math.atan2(vY, vX)) - 90;
            } else {
                x  = waveType.coordinates.get(frameCounter).x;
                y  = waveType.coordinates.get(frameCounter).y;
                double newR  = waveType.coordinates.get(frameCounter).r;
                if (frameCounter == 0 || r == -90) // initial frame
                    r = newR;
                else { // Smoothing enemy rotation to max 5° to prevent ugly rotation jumps
                    double deltaR = (newR - r + 360) % 360;
                    if (deltaR > 180)
                        deltaR -= 360;
                    if (deltaR > 0)
                        r += Math.min(5, deltaR);
                    else
                        r += Math.max(-5, deltaR);
                }
                vX = x - oldX;
                vY = y - oldY;
            }

            long now = gameNanoTime();

            if (canFire) {
                if (now - lastShot > TIME_BETWEEN_SHOTS) {
                    double[] p0 = { x, y };
                    double[] p1 = Helper.rotatePointAroundRotationCenter(x + HEIGHT * vX, y + HEIGHT * vY, x, y, -HALF_ANGLE_OF_SIGHT);
                    double[] p2 = Helper.rotatePointAroundRotationCenter(x + HEIGHT * vX, y + HEIGHT * vY, x, y, HALF_ANGLE_OF_SIGHT);

                    double targetX = spaceShip.x, targetY = spaceShip.y - spaceShip.radius;
                    double area = 0.5 * (-p1[1] * p2[0] + p0[1] * (-p1[0] + p2[0]) + p0[0] * (p1[1] - p2[1]) + p1[0] * p2[1]);
                    double s    = 1 / (2 * area) * (p0[1] * p2[0] - p0[0] * p2[1] + (p2[1] - p0[1]) * targetX + (p0[0] - p2[0]) * targetY);
                    double t    = 1 / (2 * area) * (p0[0] * p1[1] - p0[1] * p1[0] + (p0[1] - p1[1]) * targetX + (p1[0] - p0[0]) * targetY);
                    if (s > 0 && t > 0 && 1 - s - t > 0) {
                        spawnEnemyTorpedo(x, y, vX * 2, vY * 2, waveLevel);
                        lastShot = now;
                    }
                }
            }

            if (canBomb && now - lastBombDropped > BOMB_DROP_INTERVAL && noOfBombs > 0) {
                if (now - lastBomb > TIME_BETWEEN_BOMBS && spaceShip.y > y) {
                    if (spaceShip.x > x - BOMB_RANGE && spaceShip.x < x + BOMB_RANGE) {
                        spawnEnemyBomb(x, y, waveLevel);
                        lastBomb        = now;
                        lastBombDropped = now;
                        noOfBombs--;
                    }
                }
            }

            // Remove Enemy
            if (smart) {
                if(x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                    toBeRemoved = true;
                }
            } else {
                frameCounter++;
                if (frameCounter >= waveType.totalFrames) {
                    toBeRemoved = true;
                }
            }
        }
    }

    private class EnemyTorpedo extends Sprite {

        public EnemyTorpedo(final ScaledImage image, final double x, final double y, final double vX, final double vY) {
            super(image, x - image.getWidth() / 2.0, y, vX, vY);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT || isHitRainbowBlasterWavesCircle(x, y, radius)) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(x, y, radius);
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        onSpaceshipHit(); // with torpedo
                    }
                }
            }
        }
    }

    private class EnemyBomb extends Sprite {

        public EnemyBomb(final ScaledImage image, final double x, final double y, final double vX, final double vY) {
            super(image, x - image.getWidth() / 2.0, y, vX, vY);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT || isHitRainbowBlasterWavesCircle(x, y, radius)) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(x, y, radius);
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        onSpaceshipHit(); // with enemy bomb
                    }
                }
            }
        }
    }

    private class EnemyBoss extends Sprite {
        private static final int       MAX_VALUE            = 100;
        private static final long      TIME_BETWEEN_SHOTS   = 500_000_000L;
        private static final long      TIME_BETWEEN_ROCKETS = 5_000_000_000L;
        private static final double    HALF_ANGLE_OF_SIGHT  = 10;
        private final        SpaceShip spaceShip;
        private              double    dX;
        private              double    dY;
        private              double    dist;
        private              double    factor;
        private              int       value;
        private              int       hits;
        private              long      lastShot;
        private              long      lastRocket;
        private              boolean   hasRockets;


        public EnemyBoss(final SpaceShip spaceShip, final ScaledImage image, final boolean hasRockets) {
            super(image);
            this.spaceShip  = spaceShip;
            this.hasRockets = hasRockets;
            init();
        }

        @Override protected void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = -image.getHeight();

            // Value
            value = rnd.nextInt(MAX_VALUE) + 1;

            computeImageSizeDependentFields();

            // Velocity
            vX = 0;
            vY = ENEMY_BOSS_SPEED;

            // No of hits
            hits = 5;
        }

        @Override public void update() {
            dX     = spaceShip.x - x;
            dY     = spaceShip.y - y;
            dist   = Math.sqrt(dX * dX + dY * dY);
            factor = ENEMY_BOSS_SPEED / dist;
            if (spaceShip.isVulnerable && y < OUT_OF_SENSING_HEIGHT) {
                vX = dX * factor;
                vY = dY * factor;
            }

            x += vX;
            y += vY;

            r = Math.toDegrees(Math.atan2(vY, vX)) - 90;

            long now = gameNanoTime();

            if (hasRockets) {
                if (now - lastRocket > TIME_BETWEEN_ROCKETS) {
                    double[] p0 = { x, y };
                    double[] p1 = Helper.rotatePointAroundRotationCenter(x + HEIGHT * vX, y + HEIGHT * vY, x, y, -HALF_ANGLE_OF_SIGHT);
                    double[] p2 = Helper.rotatePointAroundRotationCenter(x + HEIGHT * vX, y + HEIGHT * vY, x, y, HALF_ANGLE_OF_SIGHT);

                    double targetX = spaceShip.x, targetY = spaceShip.y - spaceShip.radius;
                    double area = 0.5 * (-p1[1] * p2[0] + p0[1] * (-p1[0] + p2[0]) + p0[0] * (p1[1] - p2[1]) + p1[0] * p2[1]);
                    double s    = 1 / (2 * area) * (p0[1] * p2[0] - p0[0] * p2[1] + (p2[1] - p0[1]) * targetX + (p0[0] - p2[0]) * targetY);
                    double t    = 1 / (2 * area) * (p0[0] * p1[1] - p0[1] * p1[0] + (p0[1] - p1[1]) * targetX + (p1[0] - p0[0]) * targetY);
                    if (s > 0 && t > 0 && 1 - s - t > 0) {
                        spawnEnemyBossRocket(x, y);
                        lastRocket = now;
                    }
                }
            } else {
                if (now - lastShot > TIME_BETWEEN_SHOTS) {
                    double[] p0 = { x, y };
                    double[] p1 = Helper.rotatePointAroundRotationCenter(x + HEIGHT * vX, y + HEIGHT * vY, x, y, -HALF_ANGLE_OF_SIGHT);
                    double[] p2 = Helper.rotatePointAroundRotationCenter(x + HEIGHT * vX, y + HEIGHT * vY, x, y, HALF_ANGLE_OF_SIGHT);

                    double targetX = spaceShip.x, targetY = spaceShip.y - spaceShip.radius;
                    double area = 0.5 * (-p1[1] * p2[0] + p0[1] * (-p1[0] + p2[0]) + p0[0] * (p1[1] - p2[1]) + p1[0] * p2[1]);
                    double s    = 1 / (2 * area) * (p0[1] * p2[0] - p0[0] * p2[1] + (p2[1] - p0[1]) * targetX + (p0[0] - p2[0]) * targetY);
                    double t    = 1 / (2 * area) * (p0[0] * p1[1] - p0[1] * p1[0] + (p0[1] - p1[1]) * targetX + (p1[0] - p0[0]) * targetY);
                    if (s > 0 && t > 0 && 1 - s - t > 0) {
                        spawnEnemyBossTorpedo(x, y, vX, vY);
                        lastShot = now;
                    }
                }
            }

            switch (hits) {
                case 5: image = level.getEnemyBossImg4();break;
                case 4: image = level.getEnemyBossImg3();break;
                case 3: image = level.getEnemyBossImg2();break;
                case 2: image = level.getEnemyBossImg1();break;
                case 1: image = level.getEnemyBossImg0();break;
            }

            // Remove enemy boss
            if(x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
            }
        }
    }

    private class EnemyBossTorpedo extends Sprite {

        public EnemyBossTorpedo(final ScaledImage image, final double x, final double y, final double vX, final double vY) {
            super(image, x - image.getWidth() / 2.0, y, vX, vY);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT || isHitRainbowBlasterWavesCircle(x, y, radius)) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(x, y, radius);
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        onSpaceshipHit(); // with enemy boss torpedo
                    }
                }
            }
        }
    }

    private class EnemyBossRocket extends Sprite {
        private final long      rocketLifespan = 2_500_000_000L;
        private final SpaceShip spaceShip;
        private       long      born;
        private       double    dX;
        private       double    dY;
        private       double    dist;
        private       double    factor;

        public EnemyBossRocket(final SpaceShip spaceShip, final ScaledImage image, final double x, final double y) {
            super(image, x - image.getWidth() / 2.0, y, 0, 1);
            this.spaceShip = spaceShip;
            this.born      = gameNanoTime();
        }

        @Override public void update() {
            dX     = spaceShip.x - x;
            dY     = spaceShip.y - y;
            dist   = Math.sqrt(dX * dX + dY * dY);
            factor = ENEMY_BOSS_ROCKET_SPEED / dist;
            if (spaceShip.y > y) {
                vX = dX * factor;
                vY = dY * factor;
            }

            x += vX;
            y += vY;

            r = Math.toDegrees(Math.atan2(vY, vX)) - 90;

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT || isHitRainbowBlasterWavesCircle(x, y, radius)) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(x, y, radius);
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        onSpaceshipHit(); // with enemy boss rocket
                    }
                }
            }
            if (gameNanoTime() - born > rocketLifespan) {
                enemyRocketExplosions.add(new EnemyRocketExplosion(x - ENEMY_ROCKET_EXPLOSION_FRAME_WIDTH * 0.25, y - ENEMY_ROCKET_EXPLOSION_FRAME_HEIGHT * 0.25, vX, vY, 0.5));
                toBeRemoved = true;
            }
        }
    }

    private class LevelBoss extends Sprite {
        private static final int       MAX_VALUE            = 500;
        private static final long      TIME_BETWEEN_SHOTS   = 400_000_000L;
        private static final long      TIME_BETWEEN_ROCKETS = 3_500_000_000L;
        private static final long      TIME_BETWEEN_BOMBS   = 2_500_000_000L;
        private static final double    HALF_ANGLE_OF_SIGHT  = 22;
        private static final double    BOMB_RANGE           = 50;
        private static final long      WAITING_PHASE        = 10_000_000_000L;
        private final        SpaceShip spaceShip;
        private              double    dX;
        private              double    dY;
        private              double    dist;
        private              double    factor;
        private              double    weaponSpawnY;
        private              double    vpX;
        private              double    vpY;
        private              int       value;
        private              int       hits;
        private              long      lastShot;
        private              long      lastRocket;
        private              boolean   hasRockets;
        private              boolean   hasBombs;
        private              long      waitingStart;

        public LevelBoss(final SpaceShip spaceShip, final ScaledImage image, final boolean hasRockets, final boolean hasBombs) {
            super(image);
            this.spaceShip  = spaceShip;
            this.hasRockets = hasRockets;
            this.hasBombs   = hasBombs;
            init();
        }

        @Override protected void init() {
            // Position
            x = 0.5 * WIDTH;
            y = -image.getHeight();

            // Value
            value = MAX_VALUE;

            computeImageSizeDependentFields();

            // Velocity
            vX = 0;
            vY = LEVEL_BOSS_SPEED;

            // Rotation
            r = 0;

            // No of hits
            hits = 80;

            waitingStart = 0;
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            weaponSpawnY = height * 0.4;
        }

        @Override public void update() {
            if (y < height * 0.6) {
                // Approaching
                vY = LEVEL_BOSS_SPEED;
            } else {
                if (waitingStart == 0) {
                    waitingStart = gameNanoTime();
                }
                dX     = spaceShip.x - x;
                dY     = spaceShip.y - y;
                dist   = Math.sqrt(dX * dX + dY * dY);
                factor = LEVEL_BOSS_SPEED / dist;
                vpX    = dX * factor;
                vpY    = dY * factor;

                if (gameNanoTime() < waitingStart + WAITING_PHASE) {
                    // Waiting
                    vX = dX * factor * 10;
                    vY = 0;
                } else if (y < OUT_OF_SENSING_HEIGHT) {
                    // Attacking
                    vX = vpX;
                    vY = vpY;
                    r  = Math.toDegrees(Math.atan2(vY, vX)) - 90;
                }
            }

            x += vX;
            y += vY;

            long now = gameNanoTime();

            if (hasRockets) {
                if (now - lastRocket > TIME_BETWEEN_ROCKETS) {
                    double[] p0 = { x, y };
                    double[] p1 = Helper.rotatePointAroundRotationCenter(x + HEIGHT * vpX, y + HEIGHT * vpY, x, y, -HALF_ANGLE_OF_SIGHT);
                    double[] p2 = Helper.rotatePointAroundRotationCenter(x + HEIGHT * vpX, y + HEIGHT * vpY, x, y, HALF_ANGLE_OF_SIGHT);

                    double area = 0.5 * (-p1[1] * p2[0] + p0[1] * (-p1[0] + p2[0]) + p0[0] * (p1[1] - p2[1]) + p1[0] * p2[1]);
                    double s    = 1 / (2 * area) * (p0[1] * p2[0] - p0[0] * p2[1] + (p2[1] - p0[1]) * spaceShip.x + (p0[0] - p2[0]) * spaceShip.y);
                    double t    = 1 / (2 * area) * (p0[0] * p1[1] - p0[1] * p1[0] + (p0[1] - p1[1]) * spaceShip.x + (p1[0] - p0[0]) * spaceShip.y);
                    if (s > 0 && t > 0 && 1 - s - t > 0) {
                        spawnLevelBossRocket(x, y + weaponSpawnY);
                        lastRocket = now;
                    }
                }
            }
            if (hasBombs) {
                if (now - lastBombDropped > TIME_BETWEEN_BOMBS && spaceShip.y > y) {
                    if (spaceShip.x > x - BOMB_RANGE && spaceShip.x < x + BOMB_RANGE) {
                        spawnLevelBossBomb(x, y);
                        lastBombDropped = now;
                    }
                }
            }

            if (now - lastShot > TIME_BETWEEN_SHOTS) {
                double xx = x;
                double yy = y;
                // Empiric correction for the level boss image which is not square
                if (level == level1)
                    xx +=  (radius - radiusX) * 2;
                else
                    xx +=  (radius - radiusY) * 0.3;
                double[] p0 = { xx, yy };
                double[] p1 = Helper.rotatePointAroundRotationCenter(xx + HEIGHT * vpX, yy + HEIGHT * vpY, xx, yy, -HALF_ANGLE_OF_SIGHT);
                double[] p2 = Helper.rotatePointAroundRotationCenter(xx + HEIGHT * vpX, yy + HEIGHT * vpY, xx, yy, HALF_ANGLE_OF_SIGHT);

                double area = 0.5 * (-p1[1] * p2[0] + p0[1] * (-p1[0] + p2[0]) + p0[0] * (p1[1] - p2[1]) + p1[0] * p2[1]);
                double s    = 1 / (2 * area) * (p0[1] * p2[0] - p0[0] * p2[1] + (p2[1] - p0[1]) * spaceShip.x + (p0[0] - p2[0]) * spaceShip.y);
                double t    = 1 / (2 * area) * (p0[0] * p1[1] - p0[1] * p1[0] + (p0[1] - p1[1]) * spaceShip.x + (p1[0] - p0[0]) * spaceShip.y);
                if (s > 0 && t > 0 && 1 - s - t > 0 && vY != 0) {
                    double[] tp = Helper.rotatePointAroundRotationCenter(xx, yy + radiusY, xx, yy, r);
                    spawnLevelBossTorpedo(tp[0], tp[1], vX, vY, r);
                    lastShot = now;
                }
            }

            // Remove level boss
            if(x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
                nextLevel();
            }
        }
    }

    private class LevelBossTorpedo extends Sprite {

        public LevelBossTorpedo(final ScaledImage image, final double x, final double y, final double vX, final double vY, final double r) {
            super(image, x - image.getWidth() / 2.0, y, r, vX, vY);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT || isHitRainbowBlasterWavesCircle(x, y, radius)) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(x, y, radius);
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        onSpaceshipHit(); // with level boss torpedo
                    }
                }
            }
        }
    }

    private class LevelBossRocket extends Sprite {
        private final long      rocketLifespan = 3_000_000_000L;
        private final SpaceShip spaceShip;
        private       long      born;
        private       double    dX;
        private       double    dY;
        private       double    dist;
        private       double    factor;


        public LevelBossRocket(final SpaceShip spaceShip, final ScaledImage image, final double x, final double y) {
            super(image, x - image.getWidth() / 2.0, y, 0, 1);
            this.spaceShip = spaceShip;
            this.born      = gameNanoTime();
        }

        @Override public void update() {
            dX     = spaceShip.x - x;
            dY     = spaceShip.y - y;
            dist   = Math.sqrt(dX * dX + dY * dY);
            factor = ENEMY_BOSS_ROCKET_SPEED / dist;
            if (spaceShip.y > y) {
                vX = dX * factor;
                vY = dY * factor;
            }

            x += vX;
            y += vY;

            r = Math.toDegrees(Math.atan2(vY, vX)) - 90;

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT || isHitRainbowBlasterWavesCircle(x, y, radius)) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(x, y, radius);
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        onSpaceshipHit(); // with level boss rocket
                    }
                }
            }
            if (gameNanoTime() - born > rocketLifespan) {
                enemyRocketExplosions.add(new EnemyRocketExplosion(x - ENEMY_ROCKET_EXPLOSION_FRAME_WIDTH * 0.25, y - ENEMY_ROCKET_EXPLOSION_FRAME_HEIGHT * 0.25, vX, vY, 0.5));
                toBeRemoved = true;
            }
        }
    }

    private class LevelBossBomb extends Sprite {

        public LevelBossBomb(final ScaledImage image, final double x, final double y, final double vX, final double vY) {
            super(image, x - image.getWidth() / 2.0, y, vX, vY);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT || isHitRainbowBlasterWavesCircle(x, y, radius)) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit = isHitSpaceshipCircle(x, y, radius);
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        onSpaceshipHit(); // with level boss bomb
                    }
                }
            }
        }
    }

    // ******************** AnimatedSprites ***********************************
    private class EnemyRocketExplosion extends AnimatedSprite {

        public EnemyRocketExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            super(x, y, vX, vY, 4, 7, scale);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class AsteroidExplosion extends AnimatedSprite {

        public AsteroidExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            super(x, y, vX, vY, 8, 7, scale);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class Explosion extends AnimatedSprite {

        public final Level level;

        public Explosion(final double x, final double y, final double vX, final double vY, final double scale, Level level) {
            super(x, y, vX, vY, 8, 7, scale);
            this.level = level;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class EnemyBossExplosion extends AnimatedSprite {

        public EnemyBossExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            super(x, y, vX, vY, 4, 7, scale);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countX == maxFrameX && countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class LevelBossExplosion extends AnimatedSprite {

        public LevelBossExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            super(x, y, vX, vY, 8, 3, scale);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class UpExplosion extends AnimatedSprite {

        public UpExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            super(x, y, vX, vY, 4, 7, scale);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class SpaceShipExplosion extends AnimatedSprite {

        public SpaceShipExplosion(final double x, final double y, final double vX, final double vY) {
            super(x, y, vX, vY, 8, 6, 1.0);
        }

        @Override public void update() {
            countX++;
            if (countX == maxFrameX) {
                countX = 0;
                countY++;
                if (countY == maxFrameY) {
                    countY = 0;
                }
                if (countY == 0) {
                    hasBeenHit = false;
                    spaceShip.x = WIDTH * 0.5;
                    spaceShip.y = HEIGHT - 2 * spaceShip.height;
                    shipTouchArea.setCenterX(spaceShip.x);
                    shipTouchArea.setCenterY(spaceShip.y);
                }
            }
        }
    }

    private class RocketExplosion extends AnimatedSprite {

        public RocketExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            super(x, y, vX, vY, 5, 4, scale);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class Hit extends AnimatedSprite {

        public Hit(final double x, final double y, final double vX, final double vY) {
            super(x, y, vX, vY, 5, 2, 1.0);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class EnemyHit extends AnimatedSprite {

        public EnemyHit(final double x, final double y, final double vX, final double vY) {
            super(x, y, vX, vY, 5, 2, 1.0);
        }

        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    // ******************** Bonuses *******************************************
    private class ShieldUp extends Bonus {

        public ShieldUp(final ScaledImage image) {
            super(image);
            init();
        }

        @Override protected void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = - image.getHeight();
            rot = 0;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            computeImageSizeDependentFields();

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;
            vR = (((rnd.nextDouble()) * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            imgCenterX = width * 0.5;
            imgCenterY = height * 0.5;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
            } else {
                rot -= vR;
            }
            rot = (rot + 360) % 360;

            // Remove shieldUp
            if (x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
            }
        }
    }

    private class LifeUp extends Bonus {

        public LifeUp(final ScaledImage image) {
            super(image);
            init();
        }

        @Override protected void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = - image.getHeight();
            rot = 0;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            computeImageSizeDependentFields();

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;
            vR = (((rnd.nextDouble()) * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            imgCenterX = width * 0.5;
            imgCenterY = height * 0.5;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
            } else {
                rot -= vR;
            }
            rot = (rot + 360) % 360;

            // Remove lifeUp
            if (x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
            }
        }
    }

    private class BigTorpedoBonus extends Bonus {

        public BigTorpedoBonus(final ScaledImage image) {
            super(image);
            init();
        }

        @Override protected void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = -image.getHeight();
            rot = 0;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            computeImageSizeDependentFields();

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;
            vR = (((rnd.nextDouble()) * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            imgCenterX = width * 0.5;
            imgCenterY = height * 0.5;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
            } else {
                rot -= vR;
            }
            rot = (rot + 360) % 360;

            // Remove lifeUp
            if (x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
            }
        }
    }

    private class StarburstBonus extends Bonus {

        public final boolean is360;

        public StarburstBonus(final ScaledImage image, boolean is360) {
            super(image);
            this.is360 = is360;
            init();
        }

        @Override protected void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = -image.getHeight();
            rot = 0;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            computeImageSizeDependentFields();

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;
            vR = (((rnd.nextDouble()) * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            imgCenterX = width * 0.5;
            imgCenterY = height * 0.5;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
            } else {
                rot -= vR;
            }
            rot = (rot + 360) % 360;

            // Remove lifeUp
            if (x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
            }
        }
    }

    private class SpeedUp extends Bonus {

        public SpeedUp(final ScaledImage image) {
            super(image);
            init();
        }

        @Override protected void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = -image.getHeight();
            rot = 0;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            computeImageSizeDependentFields();

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;
            vR = (((rnd.nextDouble()) * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            imgCenterX = width * 0.5;
            imgCenterY = height * 0.5;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
            } else {
                rot -= vR;
            }
            rot = (rot + 360) % 360;

            // Remove lifeUp
            if (x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
            }
        }
    }

    private class FuryBonus extends Bonus {

        public FuryBonus(final ScaledImage image) {
            super(image);
            init();
        }

        @Override protected void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = -image.getHeight();
            rot = 0;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            computeImageSizeDependentFields();

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;
            vR = (((rnd.nextDouble()) * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            imgCenterX = width * 0.5;
            imgCenterY = height * 0.5;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
            } else {
                rot -= vR;
            }
            rot = (rot + 360) % 360;

            // Remove lifeUp
            if (x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
            }
        }
    }

    private class BlasterBonus extends Bonus {

        public BlasterBonus(final ScaledImage image) {
            super(image);
            init();
        }

        @Override protected void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = -image.getHeight();
            rot = 0;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            computeImageSizeDependentFields();

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;
            vR = (((rnd.nextDouble()) * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        @Override
        protected void computeImageSizeDependentFields() {
            super.computeImageSizeDependentFields();
            imgCenterX = width * 0.5;
            imgCenterY = height * 0.5;
        }

        @Override public void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
            } else {
                rot -= vR;
            }
            rot = (rot + 360) % 360;

            // Remove lifeUp
            if (x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                toBeRemoved = true;
                rainbowBlasterBonusShowing = false;
            }
        }
    }


    // Safe utility loop method that never raises ConcurrentModificationException
    private static <T> void forEach(List<T> list, Consumer<? super T> action) {
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            if (t != null) // Very rare, but it was observed that for any reason it could be null (causing NPE in action code)
                action.accept(t);
        }
    }

    private static <T> void removeIf(List<T> list, Predicate<? super T> filter) {
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            if (t != null && filter.test(t))
                list.remove(i--);
        }
    }

    private static <T> void forEach(T array[], Consumer<? super T> action) {
        for (int i = 0; i < array.length; i++) {
            T t = array[i];
            if (t != null) // Very rare, but it was observed that for any reason it could be null (causing NPE in action code)
                action.accept(t);
        }
    }

    private long gameNanoTime() {
        return gamePaused ? gamePauseNanoTime : System.nanoTime() - gamePauseNanoDuration;
    }

    boolean isGamePaused() {
        return gamePaused;
    }

    void toggleGamePause() {
        if (isGamePaused())
            resumeGame();
        else
            pauseGame();
    }

    void pauseGame() {
        if (!gamePaused) {
            gamePauseNanoTime = gameNanoTime();
            gamePaused = true;
            applyGameMusic();
        }
    }

    void resumeGame() {
        if (gamePaused) {
            gamePaused = false;
            gamePauseNanoDuration += gameNanoTime() - gamePauseNanoTime;
            applyGameMusic();
            if (autoFire)
                fireSpaceShipWeapon();
        }
    }

    private Pane createSvgButton(String content, boolean fill, boolean stroke, Runnable clickRunnable) {
        SVGPath path = createSvgPath(content, fill, stroke);
        // We now embed the svg path in a pane. The reason is for a better click experience. Because in JavaFX (not in
        // the browser), the clicking area is only the filled shape, not the empty space in that shape. So when clicking
        // on a gear icon on a mobile for example, even if globally our finger covers the icon, the final click point
        // may be in this empty space, making the button not reacting, leading to a frustrating experience.
        StackPane pane = new StackPane(path); // Will act as the mouse click area covering the entire surface
        // The pane needs to be reduced to the svg path size (which we can get using the layout bounds).
        double size = 64 * SCALING_FACTOR;
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);
        pane.setCursor(Cursor.HAND);
        pane.setOnMouseClicked(e -> {
            clickRunnable.run();
            userInteracted();
            e.consume();
        });
        return pane;
    }

    private final static Color SVG_COLOR = Color.rgb(51, 210, 206);

    private static SVGPath createSvgPath(String content, boolean fill, boolean stroke) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setFill(fill ? SVG_COLOR : Color.TRANSPARENT);
        if (stroke) {
            path.setStroke(SVG_COLOR);
            path.setStrokeWidth(7);
            path.setStrokeLineJoin(StrokeLineJoin.ROUND);
            path.setStrokeLineCap(StrokeLineCap.ROUND);
        }
        double scale = 2 * SCALING_FACTOR;
        path.setScaleX(scale);
        path.setScaleY(scale);
        return path;
    }
}
