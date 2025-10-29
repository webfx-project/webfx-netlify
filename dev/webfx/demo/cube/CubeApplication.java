package dev.webfx.demo.cube;

import dev.webfx.demo.cube.cubescene.CubeScene;
import dev.webfx.demo.cube.smokescene.SmokeScene;
import dev.webfx.demo.cube.textscene.TextScene;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.resource.Resource;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;

/**
 * This demo illustrates how to use WebGL in WebFX. WebFX provides a WebGLRenderingContext over a JavaFX canvas that you
 * can use to program the GPU, exactly as you would do from a JS webapp, but the rest of the UI is using the standard
 * JavaFX API.
 *
 * Note that this demo is not cross-platform (as opposed to all other WebFX demos) because WebGL is a technology only
 * available in the browser (OpenJFX doesn't provide an equivalent low-level API access to OpenGL).
 *
 * @author Bruno Salmon
 */
public class CubeApplication extends Application {

    private static final Font FONT = Font.loadFont(Resource.toUrl("Injekuta-Reg.otf", CubeApplication.class), 14);
    private static final Color KEY_BORDER_COLOR = new Color(1, 1, 1, 0.2);
    private static final CornerRadii KEY_BORDER_RADII = new CornerRadii(5);

    private final Canvas canvas = new Canvas();
    private final CubeScene cubeScene = new CubeScene(canvas);
    private final SmokeScene smokeScene = new SmokeScene(canvas);
    private final TextScene textScene = new TextScene();
    private final Circle selectedPenCircle = new Circle(13, Color.TRANSPARENT);
    private boolean generateSmoke;
    private double lastGeneratedSmokeTimeSeconds;

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(40);
        gridPane.setVgap(15);

        Switch tubeSwitch, codeSwitch;
        int rowIndex = 0;
        // FPS
        Text fpsText = createWhiteText("");
        gridPane.addRow(rowIndex++, createWhiteText("FPS"), fpsText);
        // Pen heat
        gridPane.addRow(rowIndex++, createWhiteText("Pen Heat"), createHeatBox());
        // HDPI switch (displayed only on HDPI screens)
        if (WebFxKitLauncher.getCanvasPixelDensity(canvas) > 1) // happens on HDPI screens
            gridPane.addRow(rowIndex++, createWhiteText("HDPI"), createHdpiSwitch());
        // Rotate switch
        gridPane.addRow(rowIndex++, createWhiteText("Rotate"), createSwitch(cubeScene.autoRotateProperty()));
        // Tube switch
        gridPane.addRow(rowIndex++, createWhiteText("Tube"), tubeSwitch = createSwitch(cubeScene.tubeProperty()));
        // Videos switch
        gridPane.addRow(rowIndex++, createWhiteText("Videos"), createSwitch(cubeScene.playVideosProperty()));
        // Code switch
        gridPane.addRow(rowIndex++, createWhiteText("Code"), codeSwitch = createSwitch(cubeScene.codeDoorShowingProperty()));
        // Keys legend
        gridPane.addRow(rowIndex, createWhiteText("Keys"), createKeysBox());

        // Switching Tube on when user switches Code on (to make the Code door visible inside the cube)
        codeSwitch.selectedProperty().addListener(observable -> {
            if (codeSwitch.isSelected())
                tubeSwitch.setSelected(true);
        });

        new AnimationTimer() {
            final double fpsRefreshSeconds = 0.5;
            double lastFrameSeconds;
            int framesCount;

            @Override
            public void handle(long now) {
                // FPS measurement
                framesCount++;
                final double nowSeconds = now / 1_000_000_000d;
                if (lastFrameSeconds == 0)
                    lastFrameSeconds = nowSeconds;
                else if (nowSeconds - lastFrameSeconds >= fpsRefreshSeconds) {
                    fpsText.setText(Integer.toString((int) (1d * framesCount / fpsRefreshSeconds)));
                    framesCount = 0;
                    lastFrameSeconds = nowSeconds;
                }
                // Cube scene animation
                cubeScene.onAnimationFrame(now);
                // Smoke scene animation (can be stopped after 8s of last smoke generation as it consumes GPU even with no smoke)
                if (lastGeneratedSmokeTimeSeconds > 0 && nowSeconds < lastGeneratedSmokeTimeSeconds + 6)
                    smokeScene.onAnimationFrame(now);
                // Text scene animation
                textScene.onAnimationFrame(now);
            }
        }.start();

        Scene scene = new Scene(new StackPane(canvas, textScene.getContainer(), gridPane), 800, 600, Color.web("#310E68"));

        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        StackPane.setAlignment(gridPane, Pos.BOTTOM_LEFT);
        StackPane.setMargin(gridPane, new Insets(20));
        gridPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static Text createWhiteText(String text) {
        Text t = new Text(text);
        t.setFont(FONT);
        t.setFill(Color.WHITE);
        return t;
    }

    private static Switch createSwitch(BooleanProperty booleanProperty) {
        Switch s = new Switch();
        s.setCursor(Cursor.HAND);
        s.setSelected(booleanProperty.get());
        booleanProperty.bind(s.selectedProperty());
        return s;
    }

    private Switch createHdpiSwitch() {
        Switch hdpiSwitch = new Switch();
        hdpiSwitch.setCursor(Cursor.HAND);
        hdpiSwitch.setSelected(true);
        double pixelDensity = WebFxKitLauncher.getCanvasPixelDensity(canvas);
        FXProperties.runOnPropertiesChange(() -> {
            WebFxKitLauncher.setCanvasPixelDensity(canvas, hdpiSwitch.isSelected() ? pixelDensity : 1);
        }, hdpiSwitch.selectedProperty());
        return hdpiSwitch;
    }

    private HBox createHeatBox() {
        Color[] penColors = { Color.CYAN, Color.PINK, Color.ORANGE, Color.RED };
        Circle[] penCircles = Arrays.stream(penColors).map(this::createHeatCircle).toArray(Circle[]::new);
        HBox heatBox = new HBox(10, penCircles);
        heatBox.getChildren().add(selectedPenCircle);
        selectedPenCircle.setManaged(false);
        selectedPenCircle.setMouseTransparent(true);
        selectedPenCircle.strokeProperty().bind(textScene.penColorProperty());
        textScene.setPenColor(null);
        FXProperties.runOnPropertiesChange(() -> {
            if (textScene.getPenColor() != Color.CYAN) {
                Point2D penPosition = textScene.getPenPosition();
                if (penPosition != null) {
                    smokeScene.movePointer(penPosition.getX(), penPosition.getY(), generateSmoke);
                    lastGeneratedSmokeTimeSeconds = System.nanoTime() / 1_000_000_000d;
                }
                generateSmoke = penPosition != null;
            }
        }, textScene.penPositionProperty());
        return heatBox;
    }

    private Circle createHeatCircle(Color color) {
        Circle c = new Circle(10, color);
        c.setCursor(Cursor.HAND);
        c.setOnMouseClicked(e -> {
            textScene.setPenColor(color);
            selectedPenCircle.layoutXProperty().bind(c.layoutXProperty());
            selectedPenCircle.layoutYProperty().bind(c.layoutYProperty());
            textScene.restart();
            smokeScene.setTextureWidthFactor (color == Color.PINK ? 1    : color == Color.ORANGE ? 0.7 : color == Color.RED ? 0.67 : 1);
            smokeScene.setTextureHeightFactor(color == Color.PINK ? 0.25 : color == Color.ORANGE ? 0.4 : color == Color.RED ? 0.7  : 1);
            lastGeneratedSmokeTimeSeconds = 0;
        });
        return c;
    }

    private HBox createKeysBox() {
        return new HBox(5, new StackPane[]{
                createKeyNode("←", KeyCode.LEFT),
                createKeyNode("↑", KeyCode.UP),
                createKeyNode("↓", KeyCode.DOWN),
                createKeyNode("→", KeyCode.RIGHT)
        });
    }

    private StackPane createKeyNode(String keyText, KeyCode keyCode) {
        StackPane keyNode = new StackPane(createWhiteText(keyText));
        keyNode.setPrefSize(30, 30);
        keyNode.setBorder(new Border(new BorderStroke(KEY_BORDER_COLOR, BorderStrokeStyle.SOLID, KEY_BORDER_RADII, BorderStroke.THIN)));
        keyNode.setCursor(Cursor.HAND);
        keyNode.setOnMousePressed( e -> cubeScene.onKeyPressed(keyCode));
        keyNode.setOnMouseReleased(e -> cubeScene.onKeyReleased(keyCode));
        cubeScene.setKeyListener(keyCode, pressed ->
                keyNode.setBackground(pressed ? new Background(new BackgroundFill(KEY_BORDER_COLOR, KEY_BORDER_RADII, null)) : null));
        return keyNode;
    }
}