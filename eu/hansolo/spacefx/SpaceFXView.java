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

import dev.webfx.platform.audio.Audio;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.uuid.Uuid;
import dev.webfx.platform.visibility.Visibility;
import dev.webfx.platform.visibility.VisibilityState;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.hansolo.spacefx.Config.*;


public class SpaceFXView extends StackPane {

    private static final boolean                    ENABLE_NEW_VERSION = false;
    private static final long                       SCREEN_TOGGLE_INTERVAL  = 10_000_000_000L;
    private static final Random                     RND                     = new Random();
    private static final boolean                    IS_BROWSER              = UserAgent.isBrowser();
    //private              Task<Boolean>              initTask;
    private              Level1                     level1;
    private              Level2                     level2;
    private              Level3                     level3;
    private              long                       lastScreenToggle;
    private              boolean                    readyToStart;
    private              boolean                    waitUserInteractionBeforePlayingSound = IS_BROWSER;
    private              boolean                    running;
    private              boolean                    gameOverScreen;
    private              boolean                    hallOfFameScreen;
    //private              Properties                 properties;
    private              Label                      playerInitialsLabel;
    private              InitialDigit               digit1;
    private              InitialDigit               digit2;
    private              HBox                       playerInitialsDigits;
    private              Button                     saveInitialsButton;
    private              List<Player>               hallOfFame;
    private              VBox                       hallOfFameBox;
    private              Level                      level;
    private              Difficulty                 initialDifficulty = Difficulty.EASY;
    private              Difficulty                 minLevelDifficulty;
    private              Difficulty                 levelDifficulty;
    private final        Image                      startImg                = WebFXUtil.newImage("startscreen.jpg");
    private final        Image                      gameOverImg             = WebFXUtil.newImage("gameover.jpg");
    private final        Image                      hallOfFameImg           = WebFXUtil.newImage("halloffamescreen.jpg");
    //private final        Image                      startImg                = isDesktop() ? WebFxUtil.newImage("startscreen.jpg")) : isIOS() ? WebFxUtil.newImage("startscreenIOS.jpg")) : WebFxUtil.newImage("startscreenAndroid.png"));
    //private final        Image                      gameOverImg             = isDesktop() ? WebFxUtil.newImage("gameover.jpg")) : isIOS() ? WebFxUtil.newImage("gameoverIOS.jpg")) : WebFxUtil.newImage("gameoverAndroid.png"));
    //private final        Image                      hallOfFameImg           = isDesktop() ? WebFxUtil.newImage("halloffamescreen.jpg")) : isIOS() ? WebFxUtil.newImage("halloffamescreenIOS.jpg")) : WebFxUtil.newImage("halloffamescreenAndroid.png"));
    private              ScaledImage[]              asteroidImages;
    //private              Image                      torpedoButtonImg;
    //private              Image                      rocketButtonImg;
    //private              Image                      shieldButtonImg;
    private              ScaledImage                spaceshipImg;
    private              ScaledImage                spaceshipUpImg;
    private              ScaledImage                spaceshipDownImg;
    private              ScaledImage                miniSpaceshipImg;
    private              ScaledImage                deflectorShieldImg;
    private              ScaledImage                miniDeflectorShieldImg;
    private              ScaledImage                torpedoImg;
    private              ScaledImage                bigTorpedoImg;
    private              ScaledImage                asteroidExplosionImg;
    private              ScaledImage                spaceShipExplosionImg;
    private              ScaledImage                hitImg;
    private              ScaledImage                shieldUpImg;
    private              ScaledImage                lifeUpImg;
    private              ScaledImage                bigTorpedoBonusImg;
    private              ScaledImage                starburstBonusImg;
    private              ScaledImage                miniBigTorpedoBonusImg;
    private              ScaledImage                miniStarburstBonusImg;
    private              ScaledImage                upExplosionImg;
    private              ScaledImage                rocketExplosionImg;
    private              ScaledImage                rocketImg;
    private              Audio                      laserSound;
    private              Audio                      rocketLaunchSound;
    private              Audio                      rocketExplosionSound;
    private              Audio                      enemyLaserSound;
    private              Audio                      enemyBombSound;
    private              Audio                      explosionSound;
    private              Audio                      asteroidExplosionSound;
    private              Audio                      torpedoHitSound;
    private              Audio                      spaceShipExplosionSound;
    private              Audio                      enemyBossExplosionSound;
    private              Audio                      gameoverSound;
    private              Audio                      shieldHitSound;
    private              Audio                      enemyHitSound;
    private              Audio                      deflectorShieldSound;
    private              Audio                      levelBossTorpedoSound;
    private              Audio                      levelBossRocketSound;
    private              Audio                      levelBossBombSound;
    private              Audio                      levelBossExplosionSound;
    private              Audio                      shieldUpSound;
    private              Audio                      lifeUpSound;
    private              Audio                      levelUpSound;
    private              Audio                      bonusSound;
    private final        Audio                      gameMusic;
    private final        Audio                      music;
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
    private              List<Torpedo>              torpedos;
    private              List<BigTorpedo>           bigTorpedos;
    private              List<Rocket>               rockets;
    private              List<EnemyTorpedo>         enemyTorpedos;
    private              List<EnemyBomb>            enemyBombs;
    private              List<EnemyBossTorpedo>     enemyBossTorpedos;
    private              List<EnemyBossRocket>      enemyBossRockets;
    private              List<LevelBossTorpedo>     levelBossTorpedos;
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
    private              long                       score;
    private              long                       levelKills;
    private              long                       kills;
    private              double                     scorePosX;
    private              double                     scorePosY;
    private              double                     mobileOffsetY;
    private              boolean                    hasBeenHit;
    private              int                        noOfLifes;
    private              int                        noOfShields;
    private              boolean                    bigTorpedosEnabled;
    private              boolean                    starburstEnabled;
    private              long                       lastShieldActivated;
    private              long                       lastEnemyBossAttack;
    private              long                       lastShieldUp;
    private              long                       lastLifeUp;
    private              long                       lastWave;
    private              long                       lastBombDropped;
    private              long                       lastTorpedoFired;
    private              long                       lastStarBlast;
    private              long                       lastBigTorpedoBonus;
    private              long                       lastStarburstBonus;
    private              long                       lastTimerCall;
    private              AnimationTimer             timer;
    private              AnimationTimer             screenTimer;
    private              Circle                     shipTouchArea;
    private              double                     shipTouchGoalX;
    private              double                     shipTouchGoalY;
    private              EventHandler<TouchEvent>   touchHandler;
    private              boolean                    autoFire; // WebFX addition for touch devices
    private              boolean                    gamePaused;
    private              long                       gamePauseNanoTime;
    private              long                       gamePauseNanoDuration;
    private              Text                       difficultyText;
    private              Pane                       incrementDifficultyButton;
    private              Pane                       decrementDifficultyButton;
    private              VBox                       difficultyBox;
    private              Pane                       volumeButton;

    // ******************** Constructor ***************************************
    public SpaceFXView(Stage stage) {
        gameMusic = WebFXUtil.newMusic("RaceToMars.mp3");
        music = WebFXUtil.newMusic("CityStomper.mp3");

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
                layoutInArea(volumeButton, WIDTH / 2 - 40 * SCALING_FACTOR, 45 * SCALING_FACTOR, 0, 0, 0, HPos.CENTER, VPos.TOP);
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
                    spaceShip.vX = deltaGoalX / biggestDelta * 5;
                    spaceShip.vY = deltaGoalY / biggestDelta * 5;
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
        difficultyBox = new VBox(30 * SCALING_FACTOR, incrementDifficultyButton, difficultyText, decrementDifficultyButton);
        difficultyBox.setAlignment(Pos.CENTER);

        volumeButton = createSvgButton(null,false, true, () -> toggleMuteSound());
        displayVolume();

        // background music
        WebFXUtil.setLooping(music, true);
        WebFXUtil.setVolume(music, 1);

        // for game background music
        WebFXUtil.setLooping(gameMusic, true);
        WebFXUtil.setVolume(gameMusic, 1);

        // Load sounds
        laserSound              = WebFXUtil.newSound("laserSound.mp3");
        rocketLaunchSound       = WebFXUtil.newSound("rocketLaunch.mp3");
        rocketExplosionSound    = WebFXUtil.newSound("rocketExplosion.mp3");
        enemyLaserSound         = WebFXUtil.newSound("enemyLaserSound.mp3");
        enemyBombSound          = WebFXUtil.newSound("enemyBomb.mp3");
        explosionSound          = WebFXUtil.newSound("explosionSound.mp3");
        asteroidExplosionSound  = WebFXUtil.newSound("asteroidExplosion.mp3");
        torpedoHitSound         = WebFXUtil.newSound("hit.mp3");
        spaceShipExplosionSound = WebFXUtil.newSound("spaceShipExplosionSound.mp3");
        enemyBossExplosionSound = WebFXUtil.newSound("enemyBossExplosion.mp3");
        gameoverSound           = WebFXUtil.newSound("gameover.mp3");
        shieldHitSound          = WebFXUtil.newSound("shieldhit.mp3");
        enemyHitSound           = WebFXUtil.newSound("enemyBossShieldHit.mp3");
        deflectorShieldSound    = WebFXUtil.newSound("deflectorshieldSound.mp3");
        levelBossTorpedoSound   = WebFXUtil.newSound("levelBossTorpedo.mp3");
        levelBossRocketSound    = WebFXUtil.newSound("levelBossRocket.mp3");
        levelBossBombSound      = WebFXUtil.newSound("levelBossBomb.mp3");
        levelBossExplosionSound = WebFXUtil.newSound("explosionSound1.mp3");
        shieldUpSound           = WebFXUtil.newSound("shieldUp.mp3");
        lifeUpSound             = WebFXUtil.newSound("lifeUp.mp3");
        levelUpSound            = WebFXUtil.newSound("levelUp.mp3");
        bonusSound              = WebFXUtil.newSound("bonus.mp3");

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
        torpedos                      = new ArrayList<>();
        bigTorpedos                   = new ArrayList<>();
        enemyRocketExplosions         = new ArrayList<>();
        explosions                    = new ArrayList<>();
        asteroidExplosions            = new ArrayList<>();
        upExplosions                  = new ArrayList<>();
        enemyTorpedos                 = new ArrayList<>();
        enemyBombs                    = new ArrayList<>();
        enemyBossTorpedos             = new ArrayList<>();
        enemyBossRockets              = new ArrayList<>();
        levelBossTorpedos             = new ArrayList<>();
        levelBossRockets              = new ArrayList<>();
        levelBossBombs                = new ArrayList<>();
        levelBossExplosions           = new ArrayList<>();
        enemyBossExplosions           = new ArrayList<>();
        rocketExplosions              = new ArrayList<>();
        hits                          = new ArrayList<>();
        enemyHits                     = new ArrayList<>();
        long deltaTime                = FPS_60;
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (gamePaused)
                    return;
                now = gameNanoTime();
                if (now > lastTimerCall) {
                    lastTimerCall = now + deltaTime;
                    updateAndDraw();
                }
                if (now > lastEnemyBossAttack + ENEMY_BOSS_ATTACK_INTERVAL) {
                    spawnEnemyBoss(spaceShip);
                    lastEnemyBossAttack = now;
                }
                if (now > lastShieldUp + SHIELD_UP_SPAWN_INTERVAL && noOfShields < NO_OF_SHIELDS) {
                    spawnShieldUp();
                    lastShieldUp = now;
                }
                if (now > lastLifeUp + LIFE_UP_SPAWN_INTERVAL && noOfLifes < NO_OF_LIFES) {
                    spawnLifeUp();
                    lastLifeUp = now;
                }
                if (now > lastWave + WAVE_SPAWN_INTERVAL && SHOW_ENEMIES) {
                    spawnWave();
                    lastWave = now;
                }
                if (now > lastBigTorpedoBonus + BIG_TORPEDO_BONUS_INTERVAL) {
                    spawnBigTorpedoBonus();
                    lastBigTorpedoBonus = now;
                }
                if (now > lastStarburstBonus + STARBURST_BONUS_INTERVAL) {
                    spawnStarburstBonus();
                    lastStarburstBonus = now;
                }
            }
        };
        screenTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastScreenToggle == 0)
                    lastScreenToggle = now;
                if (!running && now > lastScreenToggle + SCREEN_TOGGLE_INTERVAL) {
                    hallOfFameScreen = !hallOfFameScreen;
                    Helper.enableNode(hallOfFameBox, hallOfFameScreen);
                    Helper.enableNode(volumeButton, !hallOfFameScreen);
                    Helper.enableNode(difficultyBox, !hallOfFameScreen);
                    ctx.drawImage(hallOfFameScreen ? hallOfFameImg : startImg, 0, 0, WIDTH, HEIGHT);
                    lastScreenToggle = now;
                }
            }
        };

        shipTouchArea = new Circle();

        touchHandler = e -> {
            EventType<TouchEvent>  type  = e.getEventType();
            if (TouchEvent.TOUCH_PRESSED.equals(type)) {
/*
                if (SHOW_BUTTONS) {
                    double x = e.getTouchPoint().getX();
                    double y = e.getTouchPoint().getY();
                    if (Helper.isInsideCircle(TORPEDO_BUTTON_CX, TORPEDO_BUTTON_CY, TORPEDO_BUTTON_R, x, y)) {
                        spawnWeapon(spaceShip.x, spaceShip.y);
                    } else if (Helper.isInsideCircle(ROCKET_BUTTON_CX, ROCKET_BUTTON_CY, ROCKET_BUTTON_R, x, y)) {
                        if (rockets.size() < MAX_NO_OF_ROCKETS) {
                            spawnRocket(spaceShip.x, spaceShip.y);
                        }
                    } else if (Helper.isInsideCircle(SHIELD_BUTTON_CX, SHIELD_BUTTON_CY, SHIELD_BUTTON_R, x, y)) {
                        if (noOfShields > 0 && !spaceShip.shield) {
                            lastShieldActivated = WebFxUtil.nanoTime();
                            spaceShip.shield = true;
                            playSound(deflectorShieldSound);
                        }
                    }
                }
*/
            }
        };

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
        /*initTask = new Task<>() {
            @Override protected Boolean call() {*/
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
                //torpedoButtonImg        = WebFxUtil.newScaledImage("torpedoButton.png", 64, 64);
                //rocketButtonImg         = WebFxUtil.newScaledImage("rocketButton.png", 64, 64);
                //shieldButtonImg         = WebFxUtil.newScaledImage("shieldButton.png", 64, 64);
        asteroidExplosionImg    = ScaledImage.create("asteroidExplosion.png", 1024, 896);

                // Init levels
                level1 = new Level1();

        spaceShipExplosionImg   = ScaledImage.create("spaceshipexplosion.png", 800, 600);
        deflectorShieldImg      = ScaledImage.create("deflectorshield.png", 100, 100);
        miniDeflectorShieldImg  = ScaledImage.create("deflectorshield.png", 16, 16);
        bigTorpedoImg           = ScaledImage.create("bigtorpedo.png", 22, 40);
        shieldUpImg             = ScaledImage.create("shieldUp.png", 50, 50);
        lifeUpImg               = ScaledImage.create("lifeUp.png", 50, 50);
        bigTorpedoBonusImg      = ScaledImage.create("bigTorpedoBonus.png", 50, 50);
        starburstBonusImg       = ScaledImage.create("starburstBonus.png", 50, 50);
        miniBigTorpedoBonusImg  = ScaledImage.create("bigTorpedoBonus.png", 20, 20);
        miniStarburstBonusImg   = ScaledImage.create("starburstBonus.png", 20, 20);
        upExplosionImg          = ScaledImage.create("upExplosion.png", 400, 700);
        rocketExplosionImg      = ScaledImage.create("rocketExplosion.png", 960, 768);
        rocketImg               = ScaledImage.create("rocket.png", 17, 50);

        level2 = new Level2();
        level3 = new Level3();

        deflectorShieldRadius   = deflectorShieldImg.getWidth() * 0.5;
        spaceShip               = new SpaceShip(spaceshipImg, spaceshipUpImg, spaceshipDownImg);

        // Adjust audio clip volumes
        WebFXUtil.setVolume(explosionSound, 0.5); // explosionSound.mp3
        WebFXUtil.setVolume(torpedoHitSound, 0.5); // hit.mp3
        WebFXUtil.setVolume(laserSound, 0.3); // laserSound.mp3
        WebFXUtil.setVolume(spaceShipExplosionSound, 0.5); // spaceShipExplosionSound.mp3
        WebFXUtil.setVolume(asteroidExplosionSound, 0.7); // asteroidExplosion.mp3

        initAsteroids();

/*
                return true;
            }
        };
        initTask.setOnSucceeded(e -> {
*/
        shipTouchArea.setCenterX(spaceShip.x);
        shipTouchArea.setCenterY(spaceShip.y);
        shipTouchArea.setRadius(deflectorShieldRadius);
        shipTouchArea.setStroke(Color.TRANSPARENT);
        shipTouchArea.setFill(Color.TRANSPARENT);
        readyToStart = true;

        displayDifficulty();
/*
        });
        initTask.setOnFailed(e -> readyToStart = false);
        new Thread(initTask, "initThread").start();
*/
    }

    private void initStars() {
        for (int i = 0; i < NO_OF_STARS; i++) {
            Star star = new Star();
            star.y = RND.nextDouble() * HEIGHT;
            stars[i] = star;
        }
    }

    private void initAsteroids() {
        for (int i = 0 ; i < NO_OF_ASTEROIDS ; i++) {
            asteroids[i] = new Asteroid(asteroidImages[RND.nextInt(asteroidImages.length)]);
        }
    }


    private static final Color STAR_COLOR = Color.rgb(255, 255, 255, 0.9);

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
            for (int i = 0; i < NO_OF_STARS; i++) {
                Star star = stars[i];
                star.update();
                ctx.fillOval(star.x, star.y, star.size, star.size);
            }
        }

        // Draw Asteroids
        for (int i = 0 ; i < NO_OF_ASTEROIDS ; i++) {
            Asteroid asteroid = asteroids[i];
            asteroid.update();
            ctx.save();
            ctx.translate(asteroid.cX, asteroid.cY);
            ctx.rotate(asteroid.rot);
            ctx.scale(asteroid.scale, asteroid.scale);
            ctx.translate(-asteroid.imgCenterX, -asteroid.imgCenterY);
            asteroid.drawImage(ctx);
            ctx.restore();

            // Check for torpedo hits
            forEach(torpedos, torpedo -> {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, asteroid.cX, asteroid.cY, asteroid.radius)) {
                    asteroid.hits--;
                    if (asteroid.hits <= 0) {
                        double explosionScale = 2 * asteroid.scale;
                        asteroidExplosions.add(new AsteroidExplosion(asteroid.cX - ASTEROID_EXPLOSION_FRAME_CENTER * explosionScale, asteroid.cY - ASTEROID_EXPLOSION_FRAME_CENTER * 2 * asteroid.scale, asteroid.vX, asteroid.vY, 2 * asteroid.scale));
                        score += asteroid.value;
                        asteroid.respawn();
                        torpedo.toBeRemoved = true;
                        playSound(asteroidExplosionSound);
                    } else {
                        hits.add(new Hit(torpedo.x - HIT_FRAME_CENTER, torpedo.y - HIT_FRAME_HEIGHT, asteroid.vX, asteroid.vY));
                        torpedo.toBeRemoved = true;
                        playSound(torpedoHitSound);
                    }
                }
            });

            // Check for bigTorpedo hits
            forEach(bigTorpedos, bigTorpedo -> {
                if (isHitCircleCircle(bigTorpedo.x, bigTorpedo.y, bigTorpedo.radius, asteroid.cX, asteroid.cY, asteroid.radius)) {
                    asteroid.hits--;
                    if (asteroid.hits <= 0) {
                        double explosionScale = 2 * asteroid.scale;
                        asteroidExplosions.add(new AsteroidExplosion(asteroid.cX - ASTEROID_EXPLOSION_FRAME_CENTER * explosionScale, asteroid.cY - ASTEROID_EXPLOSION_FRAME_CENTER * 2 * asteroid.scale, asteroid.vX, asteroid.vY, 2 * asteroid.scale));
                        score += asteroid.value;
                        asteroid.respawn();
                        bigTorpedo.toBeRemoved = true;
                        playSound(asteroidExplosionSound);
                    } else {
                        hits.add(new Hit(bigTorpedo.x - HIT_FRAME_CENTER, bigTorpedo.y - HIT_FRAME_HEIGHT, asteroid.vX, asteroid.vY));
                        bigTorpedo.toBeRemoved = true;
                        playSound(torpedoHitSound);
                    }
                }
            });

            // Check for rocket hits
            forEach(rockets, rocket -> {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, asteroid.cX, asteroid.cY, asteroid.radius)) {
                    rocketExplosions.add(new RocketExplosion(asteroid.cX - ROCKET_EXPLOSION_FRAME_CENTER * asteroid.scale, asteroid.cY - ROCKET_EXPLOSION_FRAME_CENTER * asteroid.scale, asteroid.vX, asteroid.vY, asteroid.scale));
                    score += asteroid.value;
                    asteroid.respawn();
                    rocket.toBeRemoved = true;
                    playSound(rocketExplosionSound);
                }
            });

            // Check for spaceship hit
            if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, asteroid.cX, asteroid.cY, asteroid.radius);
                } else {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, asteroid.cX, asteroid.cY, asteroid.radius);
                }
                if (hit) {
                    spaceShipExplosion.countX = 0;
                    spaceShipExplosion.countY = 0;
                    spaceShipExplosion.x      = spaceShip.x - SPACESHIP_EXPLOSION_FRAME_WIDTH;
                    spaceShipExplosion.y      = spaceShip.y - SPACESHIP_EXPLOSION_FRAME_HEIGHT;
                    if (spaceShip.shield) {
                        playSound(explosionSound);
                        double explosionScale = 2 * asteroid.scale;
                        asteroidExplosions.add(new AsteroidExplosion(asteroid.cX - ASTEROID_EXPLOSION_FRAME_CENTER * explosionScale, asteroid.cY - ASTEROID_EXPLOSION_FRAME_CENTER * 2 * asteroid.scale, asteroid.vX, asteroid.vY, 2 * asteroid.scale));
                    } else {
                        playSound(spaceShipExplosionSound);
                        hasBeenHit = true;
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
                    }
                    asteroid.respawn();
                }
            }
        }

        // Draw Wave
        forEach(waves, wave -> {
            if (wave.isRunning) {
                wave.update(ctx);
            } else {
                wavesToRemove.add(wave);
            }
        });
        waves.removeAll(wavesToRemove);

        // Draw EnemyBoss
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

            // Check for torpedo hits with enemy boss
            forEach(torpedos, torpedo -> {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                    enemyBoss.hits -= TORPEDO_DAMAGE;
                    if (enemyBoss.hits == 0) {
                        enemyBossExplosions.add(
                            new EnemyBossExplosion(enemyBoss.x - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.y - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.vX,
                                                           enemyBoss.vY, 0.5));
                        score += enemyBoss.value;
                        kills++;
                        levelKills++;
                        enemyBoss.toBeRemoved = true;
                        torpedo.toBeRemoved = true;
                        playSound(enemyBossExplosionSound);
                    } else {
                        enemyHits.add(new EnemyHit(torpedo.x - ENEMY_HIT_FRAME_CENTER, torpedo.y - ENEMY_HIT_FRAME_CENTER, enemyBoss.vX, enemyBoss.vY));
                        torpedo.toBeRemoved = true;
                        playSound(enemyHitSound);
                    }
                }
            });

            // Check for bigTorpedo hits with enemy boss
            forEach(bigTorpedos, bigTorpedo -> {
                if (isHitCircleCircle(bigTorpedo.x, bigTorpedo.y, bigTorpedo.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                    enemyBoss.hits -= BIG_TORPEDO_DAMAGE;
                    if (enemyBoss.hits <= 0) {
                        enemyBossExplosions.add(
                            new EnemyBossExplosion(enemyBoss.x - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.y - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.vX,
                                                   enemyBoss.vY, 0.5));
                        score += enemyBoss.value;
                        kills++;
                        levelKills++;
                        enemyBoss.toBeRemoved = true;
                        bigTorpedo.toBeRemoved = true;
                        playSound(enemyBossExplosionSound);
                    } else {
                        enemyHits.add(new EnemyHit(bigTorpedo.x - ENEMY_HIT_FRAME_CENTER, bigTorpedo.y - ENEMY_HIT_FRAME_CENTER, enemyBoss.vX, enemyBoss.vY));
                        bigTorpedo.toBeRemoved = true;
                        playSound(enemyHitSound);
                    }
                }
            });

            // Check for rocket hits with enemy boss
            forEach(rockets, rocket -> {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                    enemyBossExplosions.add(
                        new EnemyBossExplosion(enemyBoss.x - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.y - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.vX, enemyBoss.vY, 0.5));
                    score += enemyBoss.value;
                    kills++;
                    levelKills++;
                    enemyBoss.toBeRemoved = true;
                    rocket.toBeRemoved = true;
                    playSound(enemyBossExplosionSound);
                }
            });


            // Check for space ship hit with enemy boss
            if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, enemyBoss.x, enemyBoss.y, enemyBoss.radius);
                } else {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius);
                }
                if (hit) {
                    if (spaceShip.shield) {
                        enemyBossExplosions.add(new EnemyBossExplosion(enemyBoss.x - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.y - ENEMY_BOSS_EXPLOSION_FRAME_CENTER * 0.5, enemyBoss.vX, enemyBoss.vY, 0.5));
                        playSound(enemyBossExplosionSound);
                    } else {
                        spaceShipExplosion.countX = 0;
                        spaceShipExplosion.countY = 0;
                        spaceShipExplosion.x = spaceShip.x - SPACESHIP_EXPLOSION_FRAME_WIDTH;
                        spaceShipExplosion.y = spaceShip.y - SPACESHIP_EXPLOSION_FRAME_HEIGHT;
                        playSound(spaceShipExplosionSound);
                        hasBeenHit = true;
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
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

            // Check for torpedo hits with enemy boss
            forEach(torpedos, torpedo -> {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, lbx, lby, levelBoss.radius)) {
                    levelBoss.hits -= TORPEDO_DAMAGE;
                    if (levelBoss.hits <= 0) {
                        levelBossExplosions.add(new LevelBossExplosion(levelBoss.x - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.y - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.vX, levelBoss.vY, 1.0));
                        score += levelBoss.value;
                        kills++;
                        levelBoss.toBeRemoved = true;
                        levelBossActive = false;
                        levelKills = 0;
                        nextLevel();
                        torpedo.toBeRemoved = true;
                        playSound(levelBossExplosionSound);
                    } else {
                        enemyHits.add(new EnemyHit(torpedo.x - ENEMY_HIT_FRAME_CENTER, torpedo.y - ENEMY_HIT_FRAME_CENTER, levelBoss.vX, levelBoss.vY));
                        torpedo.toBeRemoved = true;
                        playSound(enemyHitSound);
                    }
                }
            });

            // Check for bigTorpedo hits with enemy boss
            forEach(bigTorpedos, bigTorpedo -> {
                if (isHitCircleCircle(bigTorpedo.x, bigTorpedo.y, bigTorpedo.radius, lbx, lby, levelBoss.radius)) {
                    levelBoss.hits -= BIG_TORPEDO_DAMAGE;
                    if (levelBoss.hits <= 0) {
                        levelBossExplosions.add(new LevelBossExplosion(levelBoss.x - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.y - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.vX, levelBoss.vY, 1.0));
                        score += levelBoss.value;
                        kills++;
                        levelBoss.toBeRemoved = true;
                        levelBossActive = false;
                        levelKills = 0;
                        nextLevel();
                        bigTorpedo.toBeRemoved = true;
                        playSound(levelBossExplosionSound);
                    } else {
                        enemyHits.add(new EnemyHit(bigTorpedo.x - ENEMY_HIT_FRAME_CENTER, bigTorpedo.y - ENEMY_HIT_FRAME_CENTER, levelBoss.vX, levelBoss.vY));
                        bigTorpedo.toBeRemoved = true;
                        playSound(enemyHitSound);
                    }
                }
            });

            // Check for rocket hits with level boss
            forEach(rockets, rocket -> {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, lbx, lby, levelBoss.radius)) {
                    levelBoss.hits -= ROCKET_DAMAGE;
                    if (levelBoss.hits <= 0) {
                        levelBossExplosions.add(new LevelBossExplosion(levelBoss.x - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.y - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.vX, levelBoss.vY, 1.0));
                        score += levelBoss.value;
                        kills++;
                        //levelKills++;
                        levelBoss.toBeRemoved = true;
                        levelBossActive = false;
                        levelKills = 0;
                        nextLevel();
                        rocket.toBeRemoved = true;
                        playSound(levelBossExplosionSound);
                    } else {
                        enemyHits.add(new EnemyHit(rocket.x - ENEMY_HIT_FRAME_CENTER, rocket.y - ENEMY_HIT_FRAME_CENTER, levelBoss.vX, levelBoss.vY));
                        rocket.toBeRemoved = true;
                        playSound(enemyHitSound);
                    }
                }
            });

            // Check for space ship hit with level boss
            if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, levelBoss.x, levelBoss.y, levelBoss.radius);
                } else {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, levelBoss.x, levelBoss.y, levelBoss.radius);
                }
                if (hit) {
                    boolean levelBossExplodes = false;
                    if (spaceShip.shield) {
                        lastShieldActivated = 0;
                        levelBoss.hits -= SHIELD_DAMAGE;
                        levelBossExplodes = levelBoss.hits <= 0;
                    } else {
                        spaceShipExplosion.countX = 0;
                        spaceShipExplosion.countY = 0;
                        spaceShipExplosion.x = spaceShip.x - SPACESHIP_EXPLOSION_FRAME_WIDTH;
                        spaceShipExplosion.y = spaceShip.y - SPACESHIP_EXPLOSION_FRAME_HEIGHT;
                        playSound(spaceShipExplosionSound);
                        hasBeenHit = true;
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        } else {
                            levelBossExplodes = true;
                        }
                    }
                    if (levelBossExplodes) {
                        levelBossExplosions.add(new LevelBossExplosion(levelBoss.x - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.y - LEVEL_BOSS_EXPLOSION_FRAME_CENTER, levelBoss.vX, levelBoss.vY, 1.0));
                        score += levelBoss.value;
                        kills++;
                        //levelKills++;
                        levelBoss.toBeRemoved = true;
                        levelBossActive = false;
                        levelKills = 0;
                        nextLevel();
                        playSound(levelBossExplosionSound);
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

            // Check for space ship contact
            boolean hit;
            if (spaceShip.shield) {
                hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, bonus.cX, bonus.cY, bonus.radius);
            } else {
                hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, bonus.cX, bonus.cY, bonus.radius);
            }
            if (hit) {
                if (bonus instanceof LifeUp) {
                    if (noOfLifes <= NO_OF_LIFES - 1) { noOfLifes++; }
                    playSound(lifeUpSound);
                } else if (bonus instanceof ShieldUp) {
                    if (noOfShields <= NO_OF_SHIELDS - 1) { noOfShields++; }
                    playSound(shieldUpSound);
                } else if (bonus instanceof BigTorpedoBonus) {
                    bigTorpedosEnabled = true;
                    playSound(bonusSound);
                } else if (bonus instanceof StarburstBonus) {
                    starburstEnabled = true;
                    playSound(bonusSound);
                }
                upExplosions.add(new UpExplosion(bonus.cX - UP_EXPLOSION_FRAME_CENTER, bonus.cY - UP_EXPLOSION_FRAME_CENTER, bonus.vX, bonus.vY, 1.0));
                bonus.toBeRemoved = true;
            }
        });

        // Draw Torpedos
        forEach(torpedos, torpedo -> {
            torpedo.update();
            torpedo.drawImage(ctx,torpedo.x - torpedo.radius, torpedo.y - torpedo.radius);
        });

        // Draw BigTorpedos
        forEach(bigTorpedos, bigTorpedo -> {
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

        // Draw EnemyTorpedos
        forEach(enemyTorpedos, enemyTorpedo -> {
            enemyTorpedo.update();
            enemyTorpedo.drawImage(ctx, enemyTorpedo.x, enemyTorpedo.y);
        });

        // Draw EnemyBombs
        forEach(enemyBombs, enemyBomb -> {
            enemyBomb.update();
            enemyBomb.drawImage(ctx, enemyBomb.x, enemyBomb.y);
        });

        // Draw EnemyBossTorpedos
        forEach(enemyBossTorpedos, enemyBossTorpedo -> {
            enemyBossTorpedo.update();
            enemyBossTorpedo.drawImage(ctx, enemyBossTorpedo.x, enemyBossTorpedo.y);
        });

        // Draw EnemyBossRockets
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

        // Draw LevelBossTorpedos
        forEach(levelBossTorpedos, levelBossTorpedo -> {
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

        // Draw LevelBossRockets
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

        // Draw LevelBossBombs
        forEach(levelBossBombs, levelBossBomb -> {
            levelBossBomb.update();
            levelBossBomb.drawImage(ctx, levelBossBomb.x, levelBossBomb.y);
        });

        // Draw Explosions
        forEach(explosions, explosion -> {
            explosion.update();
            explosion.drawFrame(ctx, level.getExplosionImg(), EXPLOSION_FRAME_WIDTH, EXPLOSION_FRAME_HEIGHT);
        });

        // Draw AsteroidExplosions
        forEach(asteroidExplosions, asteroidExplosion -> {
            asteroidExplosion.update();
            asteroidExplosion.drawFrame(ctx, asteroidExplosionImg, ASTEROID_EXPLOSION_FRAME_WIDTH, ASTEROID_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw RocketExplosions
        forEach(rocketExplosions, rocketExplosion -> {
            rocketExplosion.update();
            rocketExplosion.drawFrame(ctx, rocketExplosionImg, ROCKET_EXPLOSION_FRAME_WIDTH, ROCKET_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw EnemyRocketExplosions
        forEach(enemyRocketExplosions, enemyRocketExplosion -> {
            enemyRocketExplosion.update();
            enemyRocketExplosion.drawFrame(ctx, level.getEnemyRocketExplosionImg(), ENEMY_ROCKET_EXPLOSION_FRAME_WIDTH, ENEMY_ROCKET_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw EnemyBossExplosions
        forEach(enemyBossExplosions, enemyBossExplosion -> {
            enemyBossExplosion.update();
            enemyBossExplosion.drawFrame(ctx, level.getEnemyBossExplosionImg(), ENEMY_BOSS_EXPLOSION_FRAME_WIDTH, ENEMY_BOSS_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw LevelBossExplosions
        forEach(levelBossExplosions, levelBossExplosion -> {
            levelBossExplosion.update();
            levelBossExplosion.drawFrame(ctx, level.getLevelBossExplosionImg(), LEVEL_BOSS_EXPLOSION_FRAME_WIDTH, LEVEL_BOSS_EXPLOSION_FRAME_HEIGHT);
        });

        // Draw UpExplosions
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

        // Draw Spaceship, score, lifes and shields
        if (hasBeenHit) {
            spaceShipExplosion.update();
            spaceShipExplosion.drawFrame(ctx, spaceShipExplosionImg, SPACESHIP_EXPLOSION_FRAME_WIDTH, SPACESHIP_EXPLOSION_FRAME_HEIGHT, spaceShip.x - SPACESHIP_EXPLOSION_FRAME_CENTER, spaceShip.y - SPACESHIP_EXPLOSION_FRAME_CENTER);
            if (noOfLifes > 0)
                spaceShip.respawn();
        }
        if (noOfLifes > 0) {
            // Draw Spaceship or it's explosion
            if (!hasBeenHit) {
                // Draw space ship
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

                if (spaceShip.shield) {
                    long delta = gameNanoTime() - lastShieldActivated;
                    if (delta > DEFLECTOR_SHIELD_TIME) {
                        spaceShip.shield = false;
                        noOfShields--;
                    } else {
                        ctx.setStroke(SPACEFX_COLOR_TRANSLUCENT);
                        ctx.setFill(SPACEFX_COLOR_TRANSLUCENT);
                        ctx.strokeRect(SHIELD_INDICATOR_X, SHIELD_INDICATOR_Y + mobileOffsetY, SHIELD_INDICATOR_WIDTH, SHIELD_INDICATOR_HEIGHT);
                        ctx.fillRect(SHIELD_INDICATOR_X, SHIELD_INDICATOR_Y + mobileOffsetY, SHIELD_INDICATOR_WIDTH - SHIELD_INDICATOR_WIDTH * delta / DEFLECTOR_SHIELD_TIME,
                                     SHIELD_INDICATOR_HEIGHT);
                        ctx.setGlobalAlpha(RND.nextDouble() * 0.5 + 0.1);
                        deflectorShieldImg.drawImage(ctx, spaceShip.x - deflectorShieldRadius, spaceShip.y - deflectorShieldRadius);
                        ctx.setGlobalAlpha(1);
                    }
                }

                if (bigTorpedosEnabled) {
                    long delta = gameNanoTime() - lastBigTorpedoBonus;
                    if (delta > BIG_TORPEDO_TIME) {
                        bigTorpedosEnabled = false;
                    }
                }

                if (starburstEnabled) {
                    long delta = gameNanoTime() - lastStarburstBonus;
                    if (delta > STARBURST_TIME) {
                        starburstEnabled = false;
                    }
                }
            }

            // Draw score
            ctx.setFill(SPACEFX_COLOR);
            ctx.setFont(scoreFont);
            ctx.fillText(Long.toString(score), scorePosX, scorePosY + mobileOffsetY);

            // Draw lifes
            for (int i = 0 ; i < noOfLifes ; i++) {
                miniSpaceshipImg.drawImage(ctx, i * miniSpaceshipImg.getWidth() + 10, 20 + mobileOffsetY);
            }

            // Draw shields
            for (int i = 0 ; i < noOfShields ; i++) {
                miniDeflectorShieldImg.drawImage(ctx, WIDTH - i * (miniDeflectorShieldImg.getWidth() + 5), 20 + mobileOffsetY);
            }

            // Draw bigTorpedo and starburst icon
            if (starburstEnabled) {
                miniStarburstBonusImg.drawImage(ctx, 10, 40 + mobileOffsetY);
            } else if (bigTorpedosEnabled) {
                miniBigTorpedoBonusImg.drawImage(ctx, 10, 40 + mobileOffsetY);
            }
        }

        // Draw Buttons
        /*if (SHOW_BUTTONS) {
            ctx.drawImage(torpedoButtonImg, TORPEDO_BUTTON_X, TORPEDO_BUTTON_Y);
            ctx.drawImage(rocketButtonImg, ROCKET_BUTTON_X, ROCKET_BUTTON_Y);
            ctx.drawImage(shieldButtonImg, SHIELD_BUTTON_X, SHIELD_BUTTON_Y);
        }*/

        // Remove sprites
        removeIf(enemyBosses, sprite -> sprite.toBeRemoved);
        removeIf(levelBosses, sprite -> sprite.toBeRemoved);
        removeIf(bonuses, sprite -> sprite.toBeRemoved);
        removeIf(torpedos, sprite -> sprite.toBeRemoved);
        removeIf(bigTorpedos, sprite -> sprite.toBeRemoved);
        removeIf(rockets, sprite -> sprite.toBeRemoved);
        removeIf(enemyTorpedos, sprite -> sprite.toBeRemoved);
        removeIf(enemyBombs, sprite -> sprite.toBeRemoved);
        removeIf(enemyBossTorpedos, sprite -> sprite.toBeRemoved);
        removeIf(enemyBossRockets, sprite -> sprite.toBeRemoved);
        removeIf(levelBossTorpedos, sprite -> sprite.toBeRemoved);
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


    // Spawn different objects
    private void spawnWeapon(final double x, final double y) {
        if (starburstEnabled) {
            fireStarburst();
        } else if (bigTorpedosEnabled) {
            bigTorpedos.add(new BigTorpedo(bigTorpedoImg, x, y, 0, -BIG_TORPEDO_SPEED * 2.333333, 45));
        } else {
            torpedos.add(new Torpedo(torpedoImg, x, y));
        }
        playSound(laserSound);
    }

/*
    private void spawnBigTorpedo(final double x, final double y) {
        bigTorpedos.add(new BigTorpedo(bigTorpedoImg, x, y, 0, -BIG_TORPEDO_SPEED * 2.333333, 45));
        playSound(laserSound);
    }
*/

    private void spawnRocket(final double x, final double y) {
        rockets.add(new Rocket(rocketImg, x, y));
        playSound(rocketLaunchSound);
    }

    private void spawnEnemyTorpedo(final double x, final double y, final double vX, final double vY) {
        double vFactor = ENEMY_TORPEDO_SPEED / Math.abs(vY); // make sure the speed is always the defined one
        enemyTorpedos.add(new EnemyTorpedo(level.getEnemyTorpedoImg(), x, y, vFactor * vX, vFactor * vY));
        playSound(enemyLaserSound);
    }

    private void spawnEnemyBomb(final double x, final double y) {
        enemyBombs.add(new EnemyBomb(level.getEnemyBombImg(), x, y, 0, ENEMY_BOMB_SPEED));
        playSound(enemyBombSound);
    }

    private void spawnEnemyBoss(final SpaceShip spaceShip) {
        if (levelBossActive || !SHOW_ENEMY_BOSS) { return; }
        enemyBosses.add(new EnemyBoss(spaceShip, level.getEnemyBossImg4(), RND.nextBoolean()));
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
        if (levelDifficulty != Difficulty.EASY)
            bonuses.add(new StarburstBonus(starburstBonusImg));
    }

    private void spawnWave() {
        switch (levelDifficulty) {
            case EASY:
                if (levelKills < NO_OF_KILLS_STAGE_1 && !levelBossActive) {
                    waves.add(new Wave(WAVE_TYPES_SLOW[RND.nextInt(WAVE_TYPES_SLOW.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], false, false));
                } else if (levelKills >= NO_OF_KILLS_STAGE_1 && levelKills < NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    if (RND.nextBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_MEDIUM[RND.nextInt(WAVE_TYPES_MEDIUM.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, false));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_SLOW, WaveType.TYPE_11_SLOW, spaceShip, 10, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], false, false));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    spawnLevelBoss(spaceShip);
                } else if (!levelBossActive) {
                    waves.add(new Wave(WAVE_TYPES_MEDIUM[RND.nextInt(WAVE_TYPES_MEDIUM.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, true));
                }
                break;
            case RELAX:
                if (levelKills < NO_OF_KILLS_STAGE_1 && !levelBossActive) {
                    if (RND.nextBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_MEDIUM[RND.nextInt(WAVE_TYPES_MEDIUM.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], false, false));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_MEDIUM, WaveType.TYPE_11_MEDIUM, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], false, false));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_1 && levelKills < NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    if (RND.nextBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[RND.nextInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, false));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, false));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    spawnLevelBoss(spaceShip);
                } else if (!levelBossActive) {
                    if (RND.nextBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[RND.nextInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, true));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, false));
                    }
                }
                break;
            default: // HARD, HERO, etc...
                if (levelKills < NO_OF_KILLS_STAGE_1 && !levelBossActive) {
                    if (RND.nextBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[RND.nextInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, false));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, false));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_1 && levelKills < NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    if (RND.nextBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[RND.nextInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, true));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, true));
                    }
                } else if (levelKills >= NO_OF_KILLS_STAGE_2 && !levelBossActive) {
                    spawnLevelBoss(spaceShip);
                } else if (!levelBossActive) {
                    if (RND.nextBoolean()) {
                        waves.add(new Wave(WAVE_TYPES_FAST[RND.nextInt(WAVE_TYPES_FAST.length)], spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, true));
                    } else {
                        waves.add(new Wave(WaveType.TYPE_10_FAST, WaveType.TYPE_11_FAST, spaceShip, levelDifficulty.noOfEnemies, level.getEnemyImages()[RND.nextInt(level.getEnemyImages().length)], true, true));
                    }
                }
                break;
        }
    }

    private void spawnEnemyBossTorpedo(final double x, final double y, final double vX, final double vY) {
        double factor = ENEMY_BOSS_TORPEDO_SPEED / Math.abs(vY); // make sure the speed is always the defined one
        enemyBossTorpedos.add(new EnemyBossTorpedo(level.getEnemyBossTorpedoImg(), x, y, factor * vX, factor * vY));
        playSound(enemyLaserSound);
    }

    private void spawnEnemyBossRocket(final double x, final double y) {
        enemyBossRockets.add(new EnemyBossRocket(spaceShip, level.getEnemyBossRocketImg(), x, y));
        playSound(rocketLaunchSound);
    }

    private void spawnLevelBossTorpedo(final double x, final double y, final double vX, final double vY, final double r) {
        double factor = LEVEL_BOSS_TORPEDO_SPEED / Math.abs(vY); // make sure the speed is always the defined one
        levelBossTorpedos.add(new LevelBossTorpedo(level.getLevelBossTorpedoImg(), x, y, factor * vX, factor * vY, r));
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


    // Hit test
    private boolean isHitCircleCircle(final double c1X, final double c1Y, final double c1R, final double c2X, final double c2Y, final double c2R) {
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
            playSound(gameoverSound);
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
        explosions.clear();
        torpedos.clear();
        bigTorpedos.clear();
        enemyTorpedos.clear();
        enemyBombs.clear();
        enemyBossTorpedos.clear();
        enemyBossRockets.clear();
        enemyBosses.clear();
        levelBosses.clear();
        levelBossTorpedos.clear();
        levelBossRockets.clear();
        levelBossBombs.clear();
        bonuses.clear();
        waves.clear();
        initAsteroids();
        spaceShip.init();
        hasBeenHit  = false;
        noOfLifes   = NO_OF_LIFES;
        noOfShields = NO_OF_SHIELDS;
        //initLevel();
        score       = 0;
        kills       = 0;
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
                displayDifficulty();
            } else if (level == level1) { // returning to level 1 => increasing minimal difficulty
                // Increasing minimal difficulty, unless we already reach the most difficulty level
                increaseDifficulty();
            } else
                displayDifficulty();

            levelDifficulty = minLevelDifficulty;
        }
    }

    private void increaseDifficulty() {
        Difficulty difficulty = isRunning() ? minLevelDifficulty : initialDifficulty;
        Difficulty[] difficulties = Difficulty.values();
        // Increasing minimal difficulty, unless we already reach the most difficulty level
        if (difficulty != difficulties[difficulties.length - 1])
            difficulty = difficulties[difficulty.ordinal() + 1];
        if (isRunning())
            minLevelDifficulty = difficulty;
        else
            initialDifficulty = difficulty;
        displayDifficulty();
    }

    private void decreaseDifficulty() {
        Difficulty difficulty = isRunning() ? minLevelDifficulty : initialDifficulty;
        Difficulty[] difficulties = Difficulty.values();
        // Increasing minimal difficulty, unless we already reach the most difficulty level
        if (difficulty != difficulties[0])
            difficulty = difficulties[difficulty.ordinal() - 1];
        if (isRunning())
            minLevelDifficulty = difficulty;
        else
            initialDifficulty = difficulty;
        displayDifficulty();
    }

    private void displayDifficulty() {
        boolean isRunning = isRunning();
        Difficulty difficulty = isRunning ? minLevelDifficulty : initialDifficulty;
        difficultyText.setText(difficulty.name());
        difficultyText.setFill(difficulty.color);
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

    private void playMusic(Audio music) {
        if (PLAY_MUSIC && !soundMuted && !waitUserInteractionBeforePlayingSound && !gamePaused)
            WebFXUtil.playMusic(music);
        else
            pauseMusic(music);
    }

    private void pauseMusic(Audio music) {
        WebFXUtil.pauseMusic(music);
    }

    // Play audio clips
    private void playSound(final Audio sound) {
        if (PLAY_SOUND && !soundMuted && !waitUserInteractionBeforePlayingSound && !gamePaused)
            WebFXUtil.playSound(sound);
    }

    private boolean soundMuted;

    void muteSound(boolean soundMuted) {
        this.soundMuted = soundMuted;
        displayVolume();
        applyGameMusic();
    }

    void toggleMuteSound() {
        if (waitUserInteractionBeforePlayingSound) {
            waitUserInteractionBeforePlayingSound = false;
            applyGameMusic();
        } else
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
        score                         = 0;
        levelKills                    = 0;
        kills                         = 0;
        hasBeenHit                    = false;
        noOfLifes                     = NO_OF_LIFES;
        noOfShields                   = NO_OF_SHIELDS;
        bigTorpedosEnabled            = false;
        starburstEnabled              = false;
        lastShieldActivated           = 0;
        lastEnemyBossAttack           = gameNanoTime();
        lastShieldUp                  = gameNanoTime();
        lastLifeUp                    = gameNanoTime();
        lastWave                      = gameNanoTime();
        lastTorpedoFired              = gameNanoTime();
        lastStarBlast                 = gameNanoTime();
        lastBigTorpedoBonus           = gameNanoTime();
        lastStarburstBonus            = gameNanoTime();
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

    public void increaseSpaceShipVx() { spaceShip.vX = 5; }
    public void decreaseSpaceShipVx() { spaceShip.vX = -5; }
    public void stopSpaceShipVx() { spaceShip.vX = 0; }

    public void increaseSpaceShipVy() { spaceShip.vY = 5; }
    public void decreaseSpaceShipVy() { spaceShip.vY = -5; }
    public void stopSpaceShipVy() { spaceShip.vY = 0; }

    public void activateSpaceShipShield() {
        if (noOfShields > 0 && !spaceShip.shield) {
            lastShieldActivated = gameNanoTime();
            spaceShip.shield = true;
            playSound(deflectorShieldSound);
        }
    }

    public void fireSpaceShipRocket() {
        // Max 3 rockets at the same time -- Only 1 rocket in auto fire (otherwise too easy) except when level boss fired torpedos
        if (rockets.size() < MAX_NO_OF_ROCKETS + (autoFire && !spaceShip.shield && levelBossTorpedos.isEmpty() ? -2 : 0)) {
            spawnRocket(spaceShip.x, spaceShip.y);
        }
    }

    private Scheduled autoFireScheduled;

    public void fireSpaceShipWeapon() {
        if (autoFireScheduled != null)
            autoFireScheduled.cancel();
        if (gamePaused)
            return;
        if (gameNanoTime() - lastTorpedoFired >= MIN_TORPEDO_INTERVAL) {
            spawnWeapon(spaceShip.x, spaceShip.y);
            lastTorpedoFired = gameNanoTime();
            // Auto firing rockets when autoFire is on and levelBoss has fired rockets and torpedo
            if (autoFire && (spaceShip.shield || !levelBossRockets.isEmpty() || !levelBossTorpedos.isEmpty()))
                fireSpaceShipRocket();
        }
        if (autoFire && isRunning())
            autoFireScheduled = Scheduler.scheduleDelay(300, this::fireSpaceShipWeapon);
    }

    public void setAutoFire(boolean autoFire) {
        if (this.autoFire != autoFire) {
            this.autoFire = autoFire;
            if (autoFire && isRunning())
                fireSpaceShipWeapon();
        }
    }

    public void mouseFire(MouseEvent e) {
        if (isRunning() && score > 0 && !isGamePaused() && gameNanoTime() > spaceShip.born + SpaceShip.INVULNERABLE_TIME / 2) {
            activateSpaceShipShield();
            fireSpaceShipRocket();
        }
    }

    public void fireStarburst() {
        if (!starburstEnabled || (gameNanoTime() - lastStarBlast < MIN_STARBURST_INTERVAL)) { return; }
        double offset    = Math.toRadians(-135);
        double angleStep = Math.toRadians(22.5);
        double angle     = 0;
        double x         = spaceShip.x;
        double y         = spaceShip.y;
        double vX;
        double vY;
        for (int i = 0 ; i < 5 ; i++) {
            vX = BIG_TORPEDO_SPEED * Math.cos(offset + angle);
            vY = BIG_TORPEDO_SPEED * Math.sin(offset + angle);
            bigTorpedos.add(new BigTorpedo(bigTorpedoImg, x, y, vX * BIG_TORPEDO_SPEED, vY * BIG_TORPEDO_SPEED, Math.toDegrees(angle)));
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


        public AnimatedSprite(final int maxFrameX, final int maxFrameY, final double scale) {
            this(0, 0, 0, 0, 0, 0, maxFrameX, maxFrameY, scale);
        }
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
        private final Random rnd        = new Random();
        private final double xVariation = 0;
        private final double minSpeedY  = 4;
        private       double x;
        private       double y;
        private       double size;
        private       double vX;
        private       double vY;
        private       double vYVariation;


        public Star() {
            // Random size
            size = rnd.nextInt(2) + 1;

            // Position
            x = (int)(rnd.nextDouble() * WIDTH);
            y = -size;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            // Velocity
            vX = (int) (Math.round((rnd.nextDouble() * xVariation) - xVariation * 0.5));
            vY = (int) (Math.round(((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation));
        }


        private void respawn() {
            x = (int) (RND.nextDouble() * WIDTH);
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
            return new StringBuilder(this.id).append(",").append(this.name).append(",").append(this.score).toString();
        }

        @Override public String toString() {
            return new StringBuilder().append("{ ")
                                      .append("\"id\"").append(":").append(id).append(",")
                                      .append("\"name\"").append(":").append(name).append(",")
                                      .append("\"score\"").append(":").append(score)
                                      .append(" }")
                                      .toString();
        }
    }

    private static final long         ENEMY_SPAWN_INTERVAL = (long) (250_000_000L * SCALING_FACTOR);

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
                    if (smartEnemies.size() < levelDifficulty.noOfSmartEnemies && RND.nextBoolean()) {
                        smartEnemies.add(enemy);
                    }
                    lastEnemySpawned = gameNanoTime();
                }

                forEach(enemies, enemy -> {
                    if (level.getIndex() > 1 &&
                        !enemy.smart &&
                        enemy.frameCounter > waveType1.totalFrames * 0.35 &&
                        smartEnemies.contains(enemy)) {
                        enemy.smart = RND.nextBoolean();
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

                    // Check for torpedo hits
                    forEach(torpedos, torpedo -> {
                        if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, enemy.x, enemy.y, enemy.radius)) {
                            explosions.add(new Explosion(enemy.x - EXPLOSION_FRAME_CENTER * 0.35, enemy.y - EXPLOSION_FRAME_CENTER * 0.35, enemy.vX, enemy.vY, 0.35));
                            score += enemy.value;
                            kills++;
                            levelKills++;
                            enemy.toBeRemoved = true;
                            torpedo.toBeRemoved = true;
                            playSound(spaceShipExplosionSound);
                        }
                    });

                    // Check for bigTorpedo hits
                    forEach(bigTorpedos, bigTorpedo -> {
                        if (isHitCircleCircle(bigTorpedo.x, bigTorpedo.y, bigTorpedo.radius, enemy.x, enemy.y, enemy.radius)) {
                            explosions.add(new Explosion(enemy.x - EXPLOSION_FRAME_CENTER * 0.35, enemy.y - EXPLOSION_FRAME_CENTER * 0.35, enemy.vX, enemy.vY, 0.35));
                            score += enemy.value;
                            kills++;
                            levelKills++;
                            enemy.toBeRemoved = true;
                            bigTorpedo.toBeRemoved = true;
                            playSound(spaceShipExplosionSound);
                        }
                    });

                    // Check for rocket hits
                    forEach(rockets, rocket -> {
                        if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, enemy.x, enemy.y, enemy.radius)) {
                            rocketExplosions.add(new RocketExplosion(enemy.x - EXPLOSION_FRAME_CENTER * 0.5, enemy.y - EXPLOSION_FRAME_CENTER * 0.5, enemy.vX, enemy.vY, 0.5));
                            score += enemy.value;
                            kills++;
                            levelKills++;
                            enemy.toBeRemoved = true;
                            rocket.toBeRemoved = true;
                            playSound(rocketExplosionSound);
                        }
                    });

                    // Check for space ship hit
                    if (spaceShip.isVulnerable && !hasBeenHit) {
                        boolean hit;
                        if (spaceShip.shield) {
                            hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, enemy.x, enemy.y, enemy.radius);
                        } else {
                            hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, enemy.x, enemy.y, enemy.radius);
                        }
                        if (hit) {
                            if (spaceShip.shield) {
                                explosions.add(new Explosion(enemy.x - EXPLOSION_FRAME_CENTER * 0.35, enemy.y - EXPLOSION_FRAME_CENTER * 0.35, enemy.vX, enemy.vY, 0.35));
                                playSound(spaceShipExplosionSound);
                            } else {
                                spaceShipExplosion.countX = 0;
                                spaceShipExplosion.countY = 0;
                                shipTouchGoalX = 0;
                                shipTouchGoalY = 0;
                                spaceShipExplosion.x      = spaceShip.x - SPACESHIP_EXPLOSION_FRAME_WIDTH;
                                spaceShipExplosion.y      = spaceShip.y - SPACESHIP_EXPLOSION_FRAME_HEIGHT;
                                playSound(spaceShipExplosionSound);
                                hasBeenHit = true;
                                noOfLifes--;
                                if (0 == noOfLifes) {
                                    gameOver();
                                }
                            }
                            enemy.toBeRemoved = true;
                        }
                    }
                });

                removeIf(enemies, enemy -> enemy.toBeRemoved);
                if (enemies.isEmpty() && enemiesSpawned == noOfEnemies) { isRunning = false; }
            }
        }

        private Enemy spawnEnemy() {
            Enemy enemy;
            if (alternateWaveType) {
                enemy = new Enemy(toggle ? waveType1 : waveType2, spaceShip, image, canFire, canBomb);
            } else {
                enemy = new Enemy(waveType1, spaceShip, image, canFire, canBomb);
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
            this.image = asteroidImages[RND.nextInt(asteroidImages.length)];
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
            super(image, x, y - image.getWidth(), 0, TORPEDO_SPEED);
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


        public Enemy(final WaveType waveType, final SpaceShip spaceShip, final ScaledImage image, final boolean canFire, final boolean canBomb) {
            this(waveType, spaceShip, image, canFire, canBomb, false);
        }
        public Enemy(final WaveType waveType, final SpaceShip spaceShip, final ScaledImage image, final boolean canFire, final boolean canBomb, final boolean smart) {
            super(image);
            this.waveType     = waveType;
            this.frameCounter = 0;
            this.spaceShip    = spaceShip;
            this.canFire      = canFire;
            this.canBomb      = canBomb;
            this.noOfBombs    = NO_OF_ENEMY_BOMBS;
            this.toBeRemoved  = false;
            this.smart        = smart;
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

                    double area = 0.5 * (-p1[1] * p2[0] + p0[1] * (-p1[0] + p2[0]) + p0[0] * (p1[1] - p2[1]) + p1[0] * p2[1]);
                    double s    = 1 / (2 * area) * (p0[1] * p2[0] - p0[0] * p2[1] + (p2[1] - p0[1]) * spaceShip.x + (p0[0] - p2[0]) * spaceShip.y);
                    double t    = 1 / (2 * area) * (p0[0] * p1[1] - p0[1] * p1[0] + (p0[1] - p1[1]) * spaceShip.x + (p1[0] - p0[0]) * spaceShip.y);
                    if (s > 0 && t > 0 && 1 - s - t > 0) {
                        spawnEnemyTorpedo(x, y, vX * 2, vY * 2);
                        lastShot = now;
                    }
                }
            }

            if (canBomb && now - lastBombDropped > BOMB_DROP_INTERVAL && noOfBombs > 0) {
                if (now - lastBomb > TIME_BETWEEN_BOMBS && spaceShip.y > y) {
                    if (spaceShip.x > x - BOMB_RANGE && spaceShip.x < x + BOMB_RANGE) {
                        spawnEnemyBomb(x, y);
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

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        hasBeenHit = true;
                        playSound(spaceShipExplosionSound);
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
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

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        hasBeenHit = true;
                        playSound(spaceShipExplosionSound);
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
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

                    double area = 0.5 * (-p1[1] * p2[0] + p0[1] * (-p1[0] + p2[0]) + p0[0] * (p1[1] - p2[1]) + p1[0] * p2[1]);
                    double s    = 1 / (2 * area) * (p0[1] * p2[0] - p0[0] * p2[1] + (p2[1] - p0[1]) * spaceShip.x + (p0[0] - p2[0]) * spaceShip.y);
                    double t    = 1 / (2 * area) * (p0[0] * p1[1] - p0[1] * p1[0] + (p0[1] - p1[1]) * spaceShip.x + (p1[0] - p0[0]) * spaceShip.y);
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

                    double area = 0.5 * (-p1[1] * p2[0] + p0[1] * (-p1[0] + p2[0]) + p0[0] * (p1[1] - p2[1]) + p1[0] * p2[1]);
                    double s    = 1 / (2 * area) * (p0[1] * p2[0] - p0[0] * p2[1] + (p2[1] - p0[1]) * spaceShip.x + (p0[0] - p2[0]) * spaceShip.y);
                    double t    = 1 / (2 * area) * (p0[0] * p1[1] - p0[1] * p1[0] + (p0[1] - p1[1]) * spaceShip.x + (p1[0] - p0[0]) * spaceShip.y);
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

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        hasBeenHit = true;
                        playSound(spaceShipExplosionSound);
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
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

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        hasBeenHit = true;
                        playSound(spaceShipExplosionSound);
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
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

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        hasBeenHit = true;
                        playSound(spaceShipExplosionSound);
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
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

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        hasBeenHit = true;
                        playSound(spaceShipExplosionSound);
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
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

            if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT) {
                toBeRemoved = true;
            } else if (spaceShip.isVulnerable && !hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    toBeRemoved = true;
                    if (spaceShip.shield) {
                        playSound(shieldHitSound);
                    } else {
                        hasBeenHit = true;
                        playSound(spaceShipExplosionSound);
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
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

    private class Explosion extends AnimatedSprite {

        public Explosion(final double x, final double y, final double vX, final double vY, final double scale) {
            super(x, y, vX, vY, 8, 7, scale);
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
                if (countX == 0 && countY == 0) {
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

        public StarburstBonus(final ScaledImage image) {
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

    private static Pane createSvgButton(String content, boolean fill, boolean stroke, Runnable clickRunnable) {
        SVGPath path = createSvgPath(content, fill, stroke);
        // We now embed the svg path in a pane. The reason is for a better click experience. Because in JavaFX (not in
        // the browser), the clicking area is only the filled shape, not the empty space in that shape. So when clicking
        // on a gear icon on a mobile for example, even if globally our finger covers the icon, the final click point
        // may be in this empty space, making the button not reacting, leading to a frustrating experience.
        Pane pane = new Pane(path); // Will act as the mouse click area covering the entire surface
        // The pane needs to be reduced to the svg path size (which we can get using the layout bounds).
        path.sceneProperty().addListener(p -> { // This postpone is necessary only when running in the browser, not in standard JavaFX
            Bounds b = path.getLayoutBounds(); // Bounds computation should be correct now even in the browser
            pane.setMinSize(b.getWidth(), b.getHeight());
            pane.setMaxSize(b.getWidth(), b.getHeight());
        });
        pane.setCursor(Cursor.HAND);
        pane.setOnMouseClicked(e -> {
            clickRunnable.run();
            e.consume();
        });
        return pane;
    }

    private final static Color SVG_COLOR = Color.gray(0.75);
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
