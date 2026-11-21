package dev.webfx.demo.emptiness;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.storage.LocalStorage;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/**
 * A WebGL demo illustrating the emptiness of phenomena.
 *
 * <p>A sphere seen from far looks solid and existing by itself. As the camera approaches, it dissolves, revealing the
 * smaller spheres it is made of. The camera then dives into one of them, which in turn dissolves into its own parts,
 * and so on, forever: at no level is there a sphere that exists from its own side, independently of its parts.</p>
 *
 * @author Bruno Salmon
 */
public final class EmptinessApplication extends Application {

    private static final Color BACKGROUND_COLOR = Color.rgb(4, 5, 11);
    private static final Color TEXT_COLOR = Color.rgb(255, 255, 255, 0.85);
    private static final Color DIM_TEXT_COLOR = Color.rgb(255, 255, 255, 0.55);

    private final Canvas canvas = new Canvas();
    private final EmptinessScene emptinessScene = new EmptinessScene(canvas);

    @Override
    public void start(Stage primaryStage) {
        // Title & message (top left)
        Text title = createText("Emptiness", Font.font("Helvetica", FontWeight.LIGHT, 28), TEXT_COLOR);
        Text message = createText(emptinessScene.isWebGLAvailable() ?
                        "Seen from afar, each sphere looks solid and existing by itself.\n" +
                        "Come closer, and it dissolves into its parts - which are only spheres too." :
                        "Sorry, this demo requires WebGL.",
                Font.font("Helvetica", 14), DIM_TEXT_COLOR);
        VBox header = new VBox(6, title, message);
        header.setMouseTransparent(true);

        // Settings (bottom left)
        Text statsText = createText("", Font.font("Helvetica", 12), DIM_TEXT_COLOR);
        GridPane settings = new GridPane();
        settings.setHgap(12);
        settings.setVgap(8);
        int row = 1; // (row 0 = the presets, added once the settings controls are created)
        Map<String, Slider> sliders = new LinkedHashMap<>(); // setting key -> slider (for the presets)
        addSlider(settings, sliders, row++, "partsCount", "Parts per sphere", 8, 2000, emptinessScene.getPartsCount(),
                v -> emptinessScene.setPartsCount((int) Math.round(v)), v -> Integer.toString((int) Math.round(v)));
        addSlider(settings, sliders, row++, "spacing", "Spacing", 0, 1, emptinessScene.getSpacing(),
                emptinessScene::setSpacing, v -> (int) Math.round(v * 100) + "%");
        addSlider(settings, sliders, row++, "hueVariety", "Colour variety", 0, 1, emptinessScene.getHueVariety(),
                emptinessScene::setHueVariety, v -> (int) Math.round(v * 100) + "%");
        addSlider(settings, sliders, row++, "speed", "Speed", 0, 0.5, emptinessScene.getSpeed(),
                emptinessScene::setSpeed, v -> format1(v * 60) + " levels/min");
        // Stems: 0 (the default) = no stems
        addSlider(settings, sliders, row++, "stemThickness", "Stem thickness", 0, 1, emptinessScene.getStemThickness(),
                emptinessScene::setStemThickness, v -> (int) Math.round(v * 100) + "%");
        // Reveal size: how big (diameter, in % of the window height, shown as "% of window") a sphere looks when it dissolves into its parts
        addSlider(settings, sliders, row++, "revealSize", "Reveal size", 0.10, 0.55, emptinessScene.getRevealSize(),
                emptinessScene::setRevealSize, v -> (int) Math.round(v * 100) + "% of window");
        // Reveal target only: only the spheres on the dive path reveal their parts (less GPU usage with many parts)
        Switch revealTargetOnlySwitch = new Switch();
        revealTargetOnlySwitch.setCursor(Cursor.HAND);
        boolean revealTargetOnly = loadSetting(REVEAL_TARGET_ONLY_KEY, emptinessScene.isRevealTargetOnly() ? 1 : 0, 0, 1) == 1;
        emptinessScene.setRevealTargetOnly(revealTargetOnly);
        revealTargetOnlySwitch.setSelected(revealTargetOnly);
        revealTargetOnlySwitch.selectedProperty().addListener((observable, oldValue, selected) -> {
            emptinessScene.setRevealTargetOnly(selected);
            saveSetting(REVEAL_TARGET_ONLY_KEY, selected ? 1 : 0);
        });
        settings.addRow(row++, createText("Reveal target only", Font.font("Helvetica", 13), TEXT_COLOR), revealTargetOnlySwitch);
        // Presets: a click loads the preset, a Ctrl (or Cmd) click saves the current settings into it
        HBox presetButtons = new HBox(8);
        for (int i = 1; i <= PRESETS_COUNT; i++)
            presetButtons.getChildren().add(createPresetButton(i, sliders, revealTargetOnlySwitch));
        // First start (no setting saved yet) => starting with the first start preset
        boolean firstStart = loadText(SELECTED_PRESET_KEY) == null && loadText(REVEAL_TARGET_ONLY_KEY) == null
                && sliders.keySet().stream().allMatch(key -> loadText(key) == null);
        // The selected preset (remembered like the settings) is unselected as soon as a setting is changed by hand
        selectPreset((int) loadSetting(SELECTED_PRESET_KEY, 0, 0, PRESETS_COUNT));
        if (firstStart)
            applyPreset(FIRST_START_PRESET, sliders, revealTargetOnlySwitch);
        sliders.values().forEach(slider -> slider.valueProperty().addListener((observable, oldValue, newValue) -> onSettingChangedByHand()));
        revealTargetOnlySwitch.selectedProperty().addListener((observable, oldValue, selected) -> onSettingChangedByHand());
        settings.add(createText("Presets", Font.font("Helvetica", 13), TEXT_COLOR), 0, 0);
        settings.add(presetButtons, 1, 0, 2, 1);
        settings.add(statsText, 0, row, 3, 1);
        settings.setPadding(new Insets(14));
        settings.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.06), new CornerRadii(10), null)));
        settings.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Text help = createText("Space or tap: pause   H or double tap: hide", Font.font("Helvetica", 12), DIM_TEXT_COLOR);
        help.setMouseTransparent(true);

        StackPane root = new StackPane(canvas, header, settings, help);
        StackPane.setAlignment(header, Pos.TOP_LEFT);
        StackPane.setMargin(header, new Insets(24));
        StackPane.setAlignment(settings, Pos.BOTTOM_LEFT);
        StackPane.setMargin(settings, new Insets(20));
        StackPane.setAlignment(help, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(help, new Insets(20));

        Scene scene = new Scene(root, 1000, 700, BACKGROUND_COLOR);
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        Runnable togglePanels = () -> {
            boolean visible = !settings.isVisible();
            settings.setVisible(visible);
            header.setVisible(visible);
            help.setVisible(visible);
        };
        // A tap (or click) pauses, a double tap hides the panels (the way to hide them on tablets and mobiles). The first
        // tap of a double tap has already paused, so the second one also cancels that pause.
        canvas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1)
                emptinessScene.togglePause();
            else if (e.getClickCount() == 2) {
                emptinessScene.togglePause();
                togglePanels.run();
            }
        });
        canvas.setCursor(Cursor.HAND);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE)
                emptinessScene.togglePause();
            else if (e.getCode() == KeyCode.H)
                togglePanels.run();
        });

        new AnimationTimer() {
            private long lastStatsNanos;
            private int framesCount;

            @Override
            public void handle(long now) {
                emptinessScene.onAnimationFrame(now);
                framesCount++;
                if (lastStatsNanos == 0)
                    lastStatsNanos = now;
                else if (now - lastStatsNanos >= 500_000_000L) {
                    int fps = (int) Math.round(framesCount * 1e9 / (now - lastStatsNanos));
                    statsText.setText(fps + " fps   " + emptinessScene.getDrawCount() + " spheres   " + emptinessScene.getStemDrawCount() + " stems   "
                            + emptinessScene.getLevelsCount() + " levels deep");
                    framesCount = 0;
                    lastStatsNanos = now;
                }
            }
        }.start();

        primaryStage.setTitle("Emptiness");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // The settings are remembered in the local storage (the value stored for a setting is applied at startup, and saved
    // again each time the setting changes)
    private static Slider addSlider(GridPane grid, Map<String, Slider> sliders, int row, String key, String label, double min, double max, double defaultValue,
                                  DoubleConsumer setter, DoubleFunction<String> formatter) {
        double value = loadSetting(key, defaultValue, min, max);
        if (value != defaultValue)
            setter.accept(value);
        Text labelText = createText(label, Font.font("Helvetica", 13), TEXT_COLOR);
        Text valueText = createText(formatter.apply(value), Font.font("Helvetica", 13), DIM_TEXT_COLOR);
        Slider slider = new Slider(min, max, value);
        slider.setPrefWidth(180);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            valueText.setText(formatter.apply(newValue.doubleValue()));
            setter.accept(newValue.doubleValue());
            saveSetting(key, newValue.doubleValue());
        });
        grid.addRow(row, labelText, slider, valueText);
        sliders.put(key, slider);
        return slider;
    }

    private static final String STORAGE_KEY_PREFIX = "emptiness.";

    // ======================================== Presets ========================================

    // The hardcoded presets (in the format logged when saving a preset in learning mode). A preset saved in the local
    // storage (Ctrl or Cmd click) replaces its hardcoded value.
    private static final String[] DEFAULT_PRESETS = {
            "partsCount=8;spacing=0.5;hueVariety=1;speed=0.33;stemThickness=0.05;revealSize=0.1;revealTargetOnly=0",
            "partsCount=81;spacing=0.5;hueVariety=0.6;speed=0.33;stemThickness=0.05;revealSize=0.1;revealTargetOnly=0",
            "partsCount=1040;spacing=0.66;hueVariety=0.5;speed=0.24;stemThickness=0;revealSize=0.24;revealTargetOnly=0",
            "partsCount=2000;spacing=0.53;hueVariety=0.5;speed=0.33;stemThickness=0;revealSize=0.22;revealTargetOnly=0",
            "partsCount=2000;spacing=1;hueVariety=0.5;speed=0.28;stemThickness=0;revealSize=0.36;revealTargetOnly=0",
    };
    private static final int PRESETS_COUNT = DEFAULT_PRESETS.length;
    private static final int FIRST_START_PRESET = 2; // the preset applied when no setting is saved yet
    private static final String REVEAL_TARGET_ONLY_KEY = "revealTargetOnly";
    private static final Background PRESET_BACKGROUND = presetBackground(Color.rgb(255, 255, 255, 0.12));
    private static final Background PRESET_SELECTED_BACKGROUND = presetBackground(Color.rgb(10, 132, 255));
    private static final Background PRESET_SAVED_BACKGROUND = presetBackground(Color.rgb(80, 200, 120, 0.8));
    private static final String SELECTED_PRESET_KEY = "selectedPreset";

    private final StackPane[] presetButtons = new StackPane[PRESETS_COUNT];
    private int selectedPreset; // 1 to PRESETS_COUNT, or 0 if none
    private boolean applyingPreset; // true while a preset is applied (so its changes don't unselect it)

    // Learning mode: the presets are saved in the local storage (Ctrl or Cmd click). Each save also logs the preset in
    // the console, in the format a preset is stored, so it can be copied to be hardcoded later.
    private StackPane createPresetButton(int number, Map<String, Slider> sliders, Switch revealTargetOnlySwitch) {
        StackPane button = new StackPane(createText(Integer.toString(number), Font.font("Helvetica", FontWeight.BOLD, 13), TEXT_COLOR));
        button.setMinSize(30, 24);
        button.setPrefSize(30, 24);
        button.setBackground(PRESET_BACKGROUND);
        button.setCursor(Cursor.HAND);
        presetButtons[number - 1] = button;
        String storageKey = "preset" + number;
        button.setOnMouseClicked(e -> {
            e.consume(); // not a tap on the canvas (no pause)
            if (e.isControlDown() || e.isMetaDown()) { // Save
                StringBuilder sb = new StringBuilder();
                sliders.forEach((key, slider) -> sb.append(key).append('=').append(slider.getValue()).append(';'));
                sb.append(REVEAL_TARGET_ONLY_KEY).append('=').append(revealTargetOnlySwitch.isSelected() ? 1 : 0);
                saveText(storageKey, sb.toString());
                Console.log("Emptiness preset " + number + ": \"" + sb + "\"");
                selectPreset(number); // (the current settings are now that preset)
                flash(button, PRESET_SAVED_BACKGROUND);
            } else // Load
                applyPreset(number, sliders, revealTargetOnlySwitch);
        });
        // On macOS, a Ctrl click is also a right click: consuming the context menu event prevents the browser menu
        button.setOnContextMenuRequested(Event::consume);
        return button;
    }

    // Applies the preset (saved in the local storage, or its hardcoded value otherwise) and selects it
    private void applyPreset(int number, Map<String, Slider> sliders, Switch revealTargetOnlySwitch) {
        String preset = loadText("preset" + number);
        if (preset == null)
            preset = DEFAULT_PRESETS[number - 1];
        applyingPreset = true;
        for (String setting : preset.split(";")) {
            int equals = setting.indexOf('=');
            if (equals <= 0)
                continue;
            String key = setting.substring(0, equals);
            try {
                double value = Double.parseDouble(setting.substring(equals + 1));
                Slider slider = sliders.get(key);
                if (slider != null)
                    slider.setValue(value); // (clamped by the slider) => applied & saved by the slider listener
                else if (key.equals(REVEAL_TARGET_ONLY_KEY))
                    revealTargetOnlySwitch.setSelected(value == 1);
            } catch (NumberFormatException ignored) { // unreadable setting => skipped
            }
        }
        applyingPreset = false;
        selectPreset(number);
    }

    private static Background presetBackground(Color color) {
        return new Background(new BackgroundFill(color, new CornerRadii(6), null));
    }

    private void selectPreset(int number) {
        selectedPreset = number;
        saveSetting(SELECTED_PRESET_KEY, number);
        for (int i = 0; i < PRESETS_COUNT; i++)
            presetButtons[i].setBackground(presetBackgroundOf(i + 1));
    }

    private Background presetBackgroundOf(int number) {
        return number == selectedPreset ? PRESET_SELECTED_BACKGROUND : PRESET_BACKGROUND;
    }

    private void onSettingChangedByHand() {
        if (!applyingPreset && selectedPreset != 0)
            selectPreset(0);
    }

    private void flash(StackPane button, Background background) {
        button.setBackground(background);
        new Timeline(new KeyFrame(Duration.millis(600), e -> {
            for (int i = 0; i < PRESETS_COUNT; i++)
                if (presetButtons[i] == button)
                    button.setBackground(presetBackgroundOf(i + 1));
        })).play();
    }

    // Returns the value stored for that setting (within min and max), or the default value if none (or if the local
    // storage is not available, for example in some private browsing modes)
    private static double loadSetting(String key, double defaultValue, double min, double max) {
        try {
            String stored = loadText(key);
            if (stored != null) {
                double value = Double.parseDouble(stored);
                if (!Double.isNaN(value))
                    return Math.max(min, Math.min(max, value));
            }
        } catch (Exception e) { // no local storage, or not a number => default value
        }
        return defaultValue;
    }

    private static void saveSetting(String key, double value) {
        saveText(key, Double.toString(value));
    }

    private static String loadText(String key) {
        try {
            return LocalStorage.getItem(STORAGE_KEY_PREFIX + key);
        } catch (Exception e) { // no local storage
            return null;
        }
    }

    private static void saveText(String key, String text) {
        try {
            LocalStorage.setItem(STORAGE_KEY_PREFIX + key, text);
        } catch (Exception e) { // no local storage => just not remembered
        }
    }

    private static Text createText(String text, Font font, Color color) {
        Text t = new Text(text);
        t.setFont(font);
        t.setFill(color);
        return t;
    }

    private static String format1(double v) {
        return Double.toString(Math.round(v * 10) / 10d);
    }
}
