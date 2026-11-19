package dev.webfx.demo.emptiness;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
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
        int row = 0;
        addSlider(settings, row++, "Parts per sphere", 8, 1000, emptinessScene.getPartsCount(),
                v -> emptinessScene.setPartsCount((int) Math.round(v)), v -> Integer.toString((int) Math.round(v)));
        addSlider(settings, row++, "Spacing", 0, 1, emptinessScene.getSpacing(),
                emptinessScene::setSpacing, v -> (int) Math.round(v * 100) + "%");
        addSlider(settings, row++, "Reveal distance", 1.6, 12, emptinessScene.getRevealDistance(),
                emptinessScene::setRevealDistance, v -> format1(v) + " radii");
        addSlider(settings, row++, "Colour variety", 0, 1, emptinessScene.getHueVariety(),
                emptinessScene::setHueVariety, v -> (int) Math.round(v * 100) + "%");
        addSlider(settings, row++, "Speed", 0, 0.5, emptinessScene.getSpeed(),
                emptinessScene::setSpeed, v -> format1(v * 60) + " levels/min");
        // Stems: 0 (the default) = no stems
        addSlider(settings, row++, "Stem thickness", 0, 1, emptinessScene.getStemThickness(),
                emptinessScene::setStemThickness, v -> (int) Math.round(v * 100) + "%");
        settings.add(statsText, 0, row, 3, 1);
        settings.setPadding(new Insets(14));
        settings.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.06), new CornerRadii(10), null)));
        settings.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Text help = createText("Space: pause   H: hide", Font.font("Helvetica", 12), DIM_TEXT_COLOR);
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
        canvas.setOnMouseClicked(e -> emptinessScene.togglePause());
        canvas.setCursor(Cursor.HAND);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE)
                emptinessScene.togglePause();
            else if (e.getCode() == KeyCode.H) {
                boolean visible = !settings.isVisible();
                settings.setVisible(visible);
                header.setVisible(visible);
                help.setVisible(visible);
            }
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

    private static Slider addSlider(GridPane grid, int row, String label, double min, double max, double value,
                                  DoubleConsumer setter, DoubleFunction<String> formatter) {
        Text labelText = createText(label, Font.font("Helvetica", 13), TEXT_COLOR);
        Text valueText = createText(formatter.apply(value), Font.font("Helvetica", 13), DIM_TEXT_COLOR);
        Slider slider = new Slider(min, max, value);
        slider.setPrefWidth(180);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            valueText.setText(formatter.apply(newValue.doubleValue()));
            setter.accept(newValue.doubleValue());
        });
        grid.addRow(row, labelText, slider, valueText);
        return slider;
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
