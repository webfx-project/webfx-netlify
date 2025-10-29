package dev.webfx.demo.cube;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * @author Bruno Salmon
 */
public class Switch extends Pane {

    private final static double RADIUS = 11, WIDTH = 50;
    private final Circle switchKnob;

    private final BooleanProperty selectedProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            updateSwitchUi(true);
        }
    };

    private final ObjectProperty<Paint> selectedBackgroundFillProperty = new SimpleObjectProperty<Paint>(Color.web("#23EA00")) {
        @Override
        protected void invalidated() {
            updateSwitchUi(true);
        }
    };

    private final ObjectProperty<Paint> unselectedBackgroundFillProperty = new SimpleObjectProperty<Paint>(Color.LIGHTGRAY) {
        @Override
        protected void invalidated() {
            updateSwitchUi(true);
        }
    };

    private final ObjectProperty<Paint> backgroundFillProperty = new SimpleObjectProperty<Paint>() {
        @Override
        protected void invalidated() {
            setBackground(new Background(new BackgroundFill(get(), new CornerRadii(RADIUS), null)));
        }
    };

    public Switch() {
        super(new Circle(RADIUS - 3));
        switchKnob = (Circle) getChildren().get(0);
        switchKnob.setLayoutY(RADIUS);
        setKnobFill(Color.WHITE);
        setMinSize(WIDTH, 2 * RADIUS);
        setMaxSize(WIDTH, 2 * RADIUS);
        setOnMouseClicked(e -> setSelected(!isSelected()));
        updateSwitchUi(false);
    }

    public void setKnobFill(Paint fill) {
        switchKnob.setFill(fill);
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }

    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }

    public void setSelected(boolean selected) {
        this.selectedProperty.set(selected);
    }

    public Paint getUnselectedBackgroundFill() {
        return unselectedBackgroundFillProperty.get();
    }

    public ObjectProperty<Paint> unselectedBackgroundFillProperty() {
        return unselectedBackgroundFillProperty;
    }

    public void setUnselectedBackgroundFill(Paint unselectedBackgroundFill) {
        this.unselectedBackgroundFillProperty.set(unselectedBackgroundFill);
    }

    public Paint getSelectedBackgroundFill() {
        return selectedBackgroundFillProperty.get();
    }

    public ObjectProperty<Paint> selectedBackgroundFillProperty() {
        return selectedBackgroundFillProperty;
    }

    public void setSelectedBackgroundFill(Paint selectedBackgroundFill) {
        this.selectedBackgroundFillProperty.set(selectedBackgroundFill);
    }

    private void updateSwitchUi(boolean animate) {
        double layoutX = isSelected() ? WIDTH - RADIUS : RADIUS;
        Paint backgroundFill = isSelected() ? getSelectedBackgroundFill() : getUnselectedBackgroundFill();
        if (!animate) {
            switchKnob.setLayoutX(layoutX);
            backgroundFillProperty.set(backgroundFill);
        } else {
            animateProperty(200, switchKnob.layoutXProperty(), layoutX);
            animateProperty(200, backgroundFillProperty, backgroundFill);
        }
    }

    static <T> Timeline animateProperty(int durationMillis, WritableValue<T> target, T endValue) {
        Timeline timeline = new Timeline(new KeyFrame(new Duration(durationMillis), new KeyValue(target, endValue, Interpolator.EASE_BOTH)));
        timeline.play();
        return timeline;
    }

}
