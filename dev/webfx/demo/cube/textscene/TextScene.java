package dev.webfx.demo.cube.textscene;

import dev.webfx.platform.resource.Resource;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public final class TextScene {

    private final Pane container = new StackPane();
    private final Circle penPoint = new Circle(10, Color.PINK);
    private final List<PenStroke> penStrokes;
    private int currentlyAnimatedPenStrokeIndex;
    private long penStrokeStartTime;
    private double penStrokeFullyVisibleLength;
    private final BooleanProperty animateProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            penPoint.setVisible(false);
            penStrokes.forEach(p -> p.getPath().setVisible(false));
            currentlyAnimatedPenStrokeIndex = -1;
        }
    };
    private final ObjectProperty<Point2D> penPositionProperty = new SimpleObjectProperty<Point2D>() {
        @Override
        protected void invalidated() {
            Point2D p = get();
            penPoint.setVisible(p != null);
            if (p != null) {
                penPoint.setCenterX(p.getX());
                penPoint.setCenterY(p.getY());
            }
        }
    };
    private Timeline lastTimeline;
    private Page page;
    private double textStrokeLengthAtPageStart;

    public TextScene() {
        penStrokes = PenStroke.generatePenStrokesFromSvgText(Resource.getText(Resource.toUrl("text.svg", getClass())));
        penStrokes.forEach(penStroke -> container.getChildren().add(penStroke.getPath()));
        container.getChildren().add(penPoint);
        penPoint.setManaged(false);
        penPoint.setVisible(false);
        container.setMouseTransparent(true);
    }

    public Pane getContainer() {
        return container;
    }

    public void setAnimate(boolean animate) {
        animateProperty.set(animate);
    }

    public boolean isAnimate() {
        return animateProperty.get();
    }

    public BooleanProperty animateProperty() {
        return animateProperty;
    }

    public void restart() {
        setAnimate(false);
        setAnimate(true);
    }

    private void setPenPosition(Point2D penPosition) {
        penPositionProperty.set(penPosition);
    }

    public Point2D getPenPosition() {
        return penPositionProperty.get();
    }

    public ObjectProperty<Point2D> penPositionProperty() {
        return penPositionProperty;
    }

    public void setPenColor(Paint color) {
        penColorProperty().set(color);
    }

    public Paint getPenColor() {
        return penColorProperty().get();
    }

    public ObjectProperty<Paint> penColorProperty() {
        return penPoint.fillProperty();
    }

    public void onAnimationFrame(long now) {
        if (!isAnimate())
            return;
        if (currentlyAnimatedPenStrokeIndex == -1) {
            penStrokeStartTime = now;
            currentlyAnimatedPenStrokeIndex = 0;
            penStrokeFullyVisibleLength = 0;
            lastTimeline = null;
            page = null;
            textStrokeLengthAtPageStart = 0;
            Page.paginate(penStrokes.stream().map(PenStroke::getWord).distinct().collect(Collectors.toList()), container.getWidth());
        }
        double textStrokeVisibleLength = PenStroke.PEN_STROKE_SPEED_PIXELS_PER_SECOND * (now - penStrokeStartTime) / 1_000_000_000;
        while (currentlyAnimatedPenStrokeIndex < penStrokes.size()) {
            PenStroke penStroke = penStrokes.get(currentlyAnimatedPenStrokeIndex);
            Page penPage = penStroke.getWord().getPage();
            if (penPage != page) {
                page = penPage;
                textStrokeLengthAtPageStart = textStrokeVisibleLength;
            }
            Path penStrokePath = penStroke.getPath();
            double penStrokeLength = penStroke.getLength();
            penStrokePath.setTranslateX(-penPage.getMinX());
            penStrokePath.setVisible(true);
            penStrokePath.setStroke(getPenColor());
            double penStrokeVisibleLength = textStrokeVisibleLength - penStrokeFullyVisibleLength;
            if (penStrokeVisibleLength < penStrokeLength) {
                penStrokePath.getStrokeDashArray().setAll(penStrokeVisibleLength, penStrokeLength);
                Point2D penPosition = penStroke.getPointAtLength(penStrokeVisibleLength);
                if (penPosition != null) {
                    penPosition = penPosition.add(penStrokePath.getTranslateX(), 0);
                    setPenPosition(penPosition);
                }
                break;
            } else {
                penStrokePath.getStrokeDashArray().clear();
                penStrokeFullyVisibleLength += penStrokeLength;
                currentlyAnimatedPenStrokeIndex++;
                double visibleTime = computePenStrokeVisibleTime(penStroke, textStrokeVisibleLength);
                double cyanTime = visibleTime * 0.2;
                double whiteTime = visibleTime * 0.4;
                double fadingTime = visibleTime * 0.8;
                setPenPosition(null); // Will cause a change of smoke color
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().setAll(
                        new KeyFrame(Duration.seconds(cyanTime),    new KeyValue(penStrokePath.strokeProperty(), getPenColor() != Color.CYAN ? Color.CYAN : Color.PINK)),
                        new KeyFrame(Duration.seconds(whiteTime),   new KeyValue(penStrokePath.strokeProperty(), Color.WHITE)),
                        new KeyFrame(Duration.seconds(fadingTime),  new KeyValue(penStrokePath.strokeProperty(), Color.WHITE)),
                        new KeyFrame(Duration.seconds(visibleTime), new KeyValue(penStrokePath.strokeProperty(), Color.TRANSPARENT))
                );
                timeline.play();
                if (currentlyAnimatedPenStrokeIndex == penStrokes.size()) {
                    penPoint.setVisible(false);
                    lastTimeline = timeline;
                    timeline.setOnFinished(e -> {
                        if (lastTimeline == timeline)
                            setPenColor(null);
                    });
                }
            }
        }
    }

    private double computePenStrokeVisibleTime(PenStroke penStroke, double textStrokeVisibleLength) {
        Page nextPage = page.getNextPage();
        double visibleTime;
        if (nextPage != null) {
            double pageTextStrokeVisibleLength = textStrokeVisibleLength - textStrokeLengthAtPageStart;
            double p = pageTextStrokeVisibleLength / page.getTextStrokeLength();
            double remainingTimeBeforePageFinished = page.getDrawingDuration() * (1 - p);
            double nextPageAvgSpeedX = nextPage.getTextWidth() / nextPage.getDrawingDuration();
            double x = penStroke.getMinX() - page.getFirstWord().getMinX();
            double nextPageTimeToReachSamePosition = x / nextPageAvgSpeedX;
            visibleTime = remainingTimeBeforePageFinished + nextPageTimeToReachSamePosition;
            if (visibleTime > 3)
                visibleTime -= 1;
        } else
            visibleTime = Math.max(6, page.getDrawingDuration() + 3);
        return visibleTime;
    }
}
