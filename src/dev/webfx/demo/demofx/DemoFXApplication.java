package dev.webfx.demo.demofx;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.DemoFX;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.effect.IEffect;
import com.chrisnewland.demofx.effect.addon.FadeOutAddOnEffect;
import com.chrisnewland.demofx.effect.addon.RotateAddOnEffect;
import com.chrisnewland.demofx.effect.addon.VolumeAddOnEffect;
import com.chrisnewland.demofx.effect.effectfactory.IEffectFactory;
import com.chrisnewland.demofx.effect.fake3d.SnowfieldSprite;
import com.chrisnewland.demofx.effect.fake3d.StarfieldSprite;
import com.chrisnewland.demofx.effect.fractal.FractalRings;
import com.chrisnewland.demofx.effect.fractal.Mandelbrot;
import com.chrisnewland.demofx.effect.fractal.Sierpinski;
import com.chrisnewland.demofx.effect.pixel.Twister;
import com.chrisnewland.demofx.effect.shape.*;
import com.chrisnewland.demofx.effect.spectral.Equaliser;
import com.chrisnewland.demofx.effect.sprite.*;
import com.chrisnewland.demofx.effect.text.*;
import com.chrisnewland.demofx.util.ImageUtil;
import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.color.ColorSeries;
import dev.webfx.extras.util.color.Colors;
import dev.webfx.extras.panes.FlexPane;
import dev.webfx.kit.util.scene.DeviceSceneUtil;
import dev.webfx.platform.audio.AudioService;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;

public class DemoFXApplication extends Application {
    private final StackPane root = new StackPane();
    private final Scene scene = DeviceSceneUtil.newScene(root, 800, 600);
    private double SCALE_FACTOR;
    private Font MENU_BUTTONS_FONT;
    private Font EFFECT_BUTTONS_FONT;
    private Insets BUTTON_PADDING;
    private final ColorSeries buttonColorSeries = Colors.createColorHueShiftSeries();
    private FlexPane menuBox, effectsBox;
    private DemoFX introDemo, waitDemo, effectDemo, animationDemo;
    private AbstractEffect lastEffect;
    private boolean started;
    private final Image quaver = loadDemoImage("quaver.png");
    private final Image quaver2 = loadDemoImage("quaver2.png");
    private Image purpleQuaver;
    long t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, tend = 264000;
    private Pane effectsPane;

    @Override
    public void start(Stage stage) {
        // Loading the Roboto font for demo buttons (this also ensures that the ♪ symbol will be correctly rendered)
        Font.loadFont(Resource.toUrl("Roboto-Regular.ttf", DemoFXApplication.class), 18);
        stage.setTitle("DemoFX");
        stage.setScene(scene);
        SCALE_FACTOR = Math.min(scene.getWidth(), scene.getHeight()) / 600;
        MENU_BUTTONS_FONT = Font.font("Roboto", 32 * SCALE_FACTOR);
        EFFECT_BUTTONS_FONT = Font.font("Roboto", 24 * SCALE_FACTOR);
        BUTTON_PADDING = new Insets(10 * SCALE_FACTOR);
        menuBox = new FlexPane(40, 40,
                createMenuButton("Play effects", this::playEffects),
                createMenuButton("Play animation", this::playAnimation)
        );
        stage.show();
        runIntroDemo();
    }

    private void runIntroDemo() {
        introDemo = newIntroDemo();
        menuBox.setMaxHeight(scene.getHeight() / 3);
        StackPane.setMargin(menuBox, new Insets(40 * SCALE_FACTOR));
        root.getChildren().setAll(introDemo.getPane(), menuBox);
        introDemo.runDemo();
        root.setOnMouseClicked(e -> {
            if (started)
                animationDemo.stopDemo();
        });
        started = false;
        waitDemo = newWaitDemo();
        animationDemo = newAnimationDemo();
    }

    private void playAnimation() {
        introDemo.stopDemo();
        root.setCursor(Cursor.DEFAULT);
        root.getChildren().setAll(waitDemo.getPane());
        waitDemo.runDemo();
        UiScheduler.scheduleInAnimationFrame(() -> {
            waitDemo.stopDemo();
            root.getChildren().setAll(animationDemo.getPane());
            animationDemo.runDemo();
        }, 6);
        started = true;
    }

    private void playEffects() {
        if (effectsBox == null)
            effectsBox = new FlexPane(10 * SCALE_FACTOR, 10 * SCALE_FACTOR, dev.webfx.platform.util.Arrays.nonNulls(Node[]::new,
                    createEffectButton("Bobs", "bobs"),
                    createEffectButton("Burst", "burst"),
                    createEffectButton("Chord", "chord"),
                    createEffectButton("Checkerboard", "checkerboard"),
                    createEffectButton("Concentric", "concentric"),
                    createEffectButton("Credits", "credits"),
                    AudioService.supportsMusicSpectrumAnalysis() ? createEffectButton("Equaliser ♪", "equaliser", "https://cdn.pixabay.com/download/audio/2022/03/15/audio_8cb749d484.mp3?filename=happy-ukulele-fun-positive-comedy-glockenspiel-music-93694.mp3") : null,
                    createEffectButton("Falling", "falling"),
                    createEffectButton("Fractal rings", "fractalrings"),
                    createEffectButton("Honeycomb", "honeycomb"),
                    createEffectButton("Mandala", "mandala"),
                    createEffectButton("Mandelbrot", "mandelbrot"),
                    createEffectButton("Mask stack", "maskstack"),
                    createEffectButton("Moire", "moire"),
                    createEffectButton("Moire 2", "moire2"),
                    //createDemoButton("Moremoire", "moremoire"), // too slow to start
                    createEffectButton("Glow board", "glowboard"),
                    createEffectButton("Grid", "grid"),
                    //createDemoButton("Rainbow", "rainbow"), // Performance problem with PixelWrite.setPixels()
                    createEffectButton("Rings", "rings"),
                    createEffectButton("Sea", "sea"),
                    createEffectButton("Sine lines", "sinelines"),
                    createEffectButton("Spin", "spin"),
                    createEffectButton("Stars", "stars"),
                    createEffectButton("Sierpinski", "sierpinski"),
                    createEffectButton("Snow field", "snowfieldsprite"),
                    createEffectButton("Star field", "starfieldsprite"),
                    createEffectButton("Text flash", "textflash"),
                    createEffectButton("Text ring", "textring"),
                    //createDemoButton("Text wave", "textwave"), // to slow to start
                    createEffectButton("Text wave sprite", "textwavesprite"),
                    createEffectButton("Tiles", "tiles"),
                    createEffectButton("Twister", "twister"),
                    createEffectButton("Word search", "wordsearch"),
                    createEffectButton("Back", null)
            ));
        effectsBox.setOnMouseClicked(null);
        StackPane.setMargin(effectsBox, new Insets(30 * SCALE_FACTOR));
        effectsPane = new Pane();
        effectsPane.setBackground(BackgroundFactory.newBackground(Color.BLACK));
        showEffectsBox();
    }

    private DemoConfig newDemoConfig(String audioResource) {
        DemoConfig demoConfig = new DemoConfig(scene.getWidth(), scene.getHeight());
        demoConfig.setAudioFilename(Resource.toUrl(audioResource, DemoFXApplication.class));
        demoConfig.setAudioVolume(0.8); // To prevent saturation during rocky music
        return demoConfig;
    }

    private DemoFX newIntroDemo() {
        return new DemoFX(newDemoConfig(null), (IEffectFactory) demoConfig -> Collections.listOf(
                new WordSearch(demoConfig, "\n\n\n\n\n\n\n\n\n\nPowered by DemoFX\n\nA JavaFX Canvas library\n\nby Chris Newland")
                //new TextWaveSprite(demoConfig, new String[] { OperatingSystem.isMobile() ? "Tap to play" : "Click to play"}, demoConfig.getHeight() * 0.75, demoConfig.getHeight() / 1000, Math.max(3, Math.min(5, demoConfig.getWidth() / 150)), true)
        ));
    }

    private DemoFX newWaitDemo() {
        return new DemoFX(newDemoConfig(null), (IEffectFactory) demoConfig -> Collections.listOf(
                new WordSearch(demoConfig, "Doing pre-computation\n\nbut most animation\n\nis realtime", 0)
        ));
    }

    private DemoFX newAnimationDemo() {
        return new DemoFX(newDemoConfig("DemoFX3.mp3"), (IEffectFactory) demoConfig -> Collections.listOf(
                // Starting sequence: Star field with majority white stars, then blue, purple & orange stars
                new FadeOutAddOnEffect(scheduleEffect(new StarfieldSprite(demoConfig, Color.WHITE, Color.WHITE, Color.web("#0c53a9"), Color.web("#850ca6"), Color.web("#a25e11")), 0, (t1 = 15820) + 5000), 3000 /* 3s fadeout effect 2s after fractal rings starts */),

                // Fractal sequence:
                // 1) Fractal rings
                scheduleEffect(new RotateAddOnEffect(new FractalRings(demoConfig), 23920, -1, t2 = 32000, 1), t1, t3 = 39900),
                // 3) Mandelbrot (declared before 2) so it is displayed behind Sierpinski)
                scheduleEffect(new Mandelbrot(demoConfig), t3, t6 = 80000),
                // 2) Sierpinski
                scheduleEffect(new Sierpinski(demoConfig), t2, t4 = 47850),
                // 4) Chord on top of Mandelbrot (still running)
                scheduleEffect(new Chord(demoConfig, Color.ORANGE), t4, t5 = 64000),
                // Text wave between 4) and 5)
                scheduleEffect(new TextWaveSprite(demoConfig, new String[] {"Realtime Mandelbrot computation"}, demoConfig.getHeight() * 0.75, demoConfig.getHeight() / 1200, 2 * Math.max(3, Math.min(5, demoConfig.getWidth() / 150))), t4 + 2000, t5),
                // 5) Concentric colored quavers on top of Mandelbrot (still running)
                scheduleEffect(new Concentric(demoConfig, 18, Arrays.stream("#00ACEB #00A656 #FCE400 #F36126 #CE0166 #91248D".split(" ")).map(c -> createTintedQuaver(Color.web(c))).toArray(Image[]::new))
                        // Pulse times matching the music:
                        .setPulseTimes(64000, 64257, 64758, 65268, 65750, 66497, 66753, 67258, 68011, 68261, 68754, 69258, 69751, 70251, 70754, 71246, 72005, 72259, 72756, 73254, 73754, 76001, 76254, 76756, 77256, 77751, 78503, 78754, 79255, 80000, 80256, 80755, 81256, 81650), t5, t6),

                // Audio spectrum sequence:
                // 1) Equaliser with falling quavers on top of a glow board
                scheduleEffect(new Glowboard(demoConfig, 32), t6, t7 = 96000),
                scheduleEffect(new FadeOutAddOnEffect(new Falling(demoConfig, purpleQuaver = createTintedQuaver(Color.web("#BD4AEA")), quaver2), 1000), t6, 88000 + 1000),
                scheduleEffect(new Equaliser(demoConfig), t6, 88000),
                // 2) Sine lines with volume effect, on top of a sea of quavers, on top of the same glow board (still running)
                scheduleEffect(new Sea(demoConfig, purpleQuaver), 88000, t7),
                scheduleEffect(new VolumeAddOnEffect(new SineLines(demoConfig), 40.0, 22, 14, 25, 15, 17, 5, 21, 12, 23), 88000, t7),
                scheduleEffect(new SineLines(demoConfig), 88000, t7),

                // Rocky music 1 sequence:
                // 2) Moire2 effect (declared before 1) to be behind the still running JavaFX tiles)
                scheduleEffect(new Moire2(demoConfig), 112000, t8 = 128000),
                // 1) JavaFX tiles
                scheduleEffect(new Tiles(demoConfig), t7, t8),
                // 3) Moire effect on top of a red checkerboard
                scheduleEffect(new Checkerboard(demoConfig, Color.RED), t8, t9 = 136000),
                scheduleEffect(new Moire(demoConfig), t8, t9),
                // 4) Rings effect on top of an orange checkerboard
                scheduleEffect(new Checkerboard(demoConfig, Color.ORANGE), t9, t10 = 144000),
                scheduleEffect(new Rings(demoConfig), t9, t10),
                // 5) Mask stack effect (with tiger image), on top of a blue checkerboard
                scheduleEffect(new Checkerboard(demoConfig), t10, t11 = 152000),
                scheduleEffect(new MaskStack(demoConfig, true), t10, t11 = 152000),
                // 6) Twister effect, on top of a cyan blue checkerboard
                scheduleEffect(new Checkerboard(demoConfig, Color.web("#00C2B6")), t10 = 152000, t11 = 159874),
                scheduleEffect(new Twister(demoConfig, Color.web("#FCC738"), Color.web("#E6933F"), Color.web("#FC6038"), Color.web("#F2386E")), t10 = 152000, t11 = 159874),

                // Rocky music 2 sequence:
                // 2) Text ring (Entirely in Java and JavaFX), on top of mandalas (starting later), on top of a Moire2 effect
                scheduleEffect(new Moire2(demoConfig), t13 = 175000, t14 = 192000),
                scheduleEffect(new Mandala(demoConfig, 16), 184000, t14),
                scheduleEffect(new Burst(demoConfig, loadDemoImage("star.png"), 8), 187000, t14),
                scheduleEffect(new TextRing(demoConfig, new TextRing.RingData("Entirely    in    Java    and    JavaFX", Math.min(demoConfig.getWidth(), demoConfig.getHeight()) * 0.4, 0.12, -1, 3.25, 2)), t13, t14),
                // 1) Spin effect (Java logo) with a fading out effect at the end
                scheduleEffect(new FadeOutAddOnEffect(new Spin(demoConfig), 2000), t12 = 159874, t13 + 2000),

                // Ending sequence:
                // 1) Amazing work (word search effect)
                scheduleEffect(new FadeOutAddOnEffect(new WordSearch(demoConfig, "Amazing work\n\nThank you Chris Newland\n\n@chriswhocodes"), 2500), t14, (t15 = t14 + 18000) + 2000),
                // 3) Credits (declared before 2) so it's behind the snow)
                scheduleEffect(new FadeOutAddOnEffect(new Credits(demoConfig, Color.web("#D0D0D0"), (t16 = tend - 4 * (75 + 100 + 75)) - t15), 1000), t15, t16),
                // 2) Snow field
                scheduleEffect(new FadeOutAddOnEffect(new SnowfieldSprite(demoConfig), 1000), t14 + 8000, t16),
                // 4) Thank you for watching (flash text)
                scheduleEffect(lastEffect = new TextFlash(demoConfig, "Thank you for watching", false, 75, 100, 75), t16, tend)
        )).setOnCompleted(() -> UiScheduler.scheduleDelay(lastEffect.isEffectFinished() ? 1000 : 0, this::runIntroDemo));  // Waiting 1s more before returning to intro
    }

    private <E extends IEffect> E scheduleEffect(E effect, long start, long stop) {
        if (start > 0)
            effect.setStartOffsetMillis(start);
        if (stop > 0)
            effect.setStopOffsetMillis(stop);
        return effect;
    }

    private Node createButton(String text, boolean isEffectButton, Runnable runnable) {
        Text buttonText = new Text(text);
        buttonText.setFont(isEffectButton ? EFFECT_BUTTONS_FONT : MENU_BUTTONS_FONT);
        buttonText.setFill(Color.WHITE);
        StackPane.setMargin(buttonText, BUTTON_PADDING);
        StackPane button = new StackPane(buttonText);
        button.setBackground(BackgroundFactory.newBackground(buttonColorSeries.nextColor()));

        button.setOnMouseClicked(e -> {
            runnable.run();
            e.consume();
        });
        button.setCursor(Cursor.HAND);
        return button;
    }

    private Node createMenuButton(String text, Runnable runnable) {
        return createButton(text, false, runnable);
    }

    private Node createEffectButton(String text, String effect) {
        return createEffectButton(text, effect, null);
    }

    private Node createEffectButton(String text, String effect, String audioUrl) {
        return createButton(text, true, () -> {
            if (effect != null)
                runEffectDemo(effect, audioUrl);
            else // Back button
                exitEffects();
        });
    }

    private boolean showEffectBox;

    private void runEffectDemo(String effect, String audioUrl) {
        if (effectDemo != null)
            effectDemo.stopDemo();
        DemoConfig config = audioUrl == null ? DemoConfig.buildConfig("-e", effect, "-w", "" + scene.getWidth(), "-h", "" + scene.getHeight()) : DemoConfig.buildConfig("-e", effect, "-a", audioUrl, "-w", "" + scene.getWidth(), "-h", "" + scene.getHeight());
        effectDemo = new DemoFX(config);
        effectsPane = effectDemo.getPane();
        effectsPane.setOnMouseClicked(e -> toggleShowEffectsBox());
        effectsPane.setCursor(Cursor.HAND);
        effectsBox.setOnMouseClicked(e -> toggleShowEffectsBox());
        effectsBox.setCursor(Cursor.HAND);
        toggleShowEffectsBox(); // Will hide the effect buttons
        effectDemo.runDemo();
    }

    private void updateEffectRootContent() {
        if (showEffectBox)
            root.getChildren().setAll(effectsPane, effectsBox);
        else
            root.getChildren().setAll(effectsPane);
    }

    private void showEffectsBox() {
        showEffectBox = true;
        updateEffectRootContent();
    }

    private void toggleShowEffectsBox() {
        showEffectBox = !showEffectBox;
        updateEffectRootContent();
    }

    private void exitEffects() {
        if (effectDemo != null)
            effectDemo.stopDemo();
        runIntroDemo();
    }

    private Image loadDemoImage(String name) {
        return new Image(Resource.toUrl(name, DemoFXApplication.class), true);
    }

    private Image createTintedQuaver(Color color) {
        WritableImage image = new WritableImage(24, 45);
        if (quaver.getProgress() >= 1)
            tintQuaver(quaver, image, color);
        else
            quaver.progressProperty().addListener((observableValue, number, t1) -> tintQuaver(quaver, image, color));
        return image;
    }

    private void tintQuaver(Image quaver, WritableImage image, Color color) {
        if (quaver.getProgress() == 1)
            ImageUtil.tintImage(quaver, color.getHue(), image);
    }

}