package dev.webfx.extras.panes;

import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public final class FlexPane extends Pane {

    private static final String GROW_CONSTRAINT = "flexbox-grow";
    private static final String MARGIN_CONSTRAINT = "flexbox-margin";

    private final DoubleProperty horizontalSpaceProperty = newLayoutDoubleProperty();

    private final DoubleProperty verticalSpaceProperty = newLayoutDoubleProperty();

    private final BooleanProperty spaceTopProperty = createLayoutBooleanProperty();

    private final BooleanProperty spaceLeftProperty = createLayoutBooleanProperty();

    private final BooleanProperty spaceRightProperty = createLayoutBooleanProperty();

    private final BooleanProperty spaceBottomProperty = createLayoutBooleanProperty();

    private final BooleanProperty flexLastRowProperty = createLayoutBooleanProperty(true);

    private final ObjectProperty<Pos> alignmentProperty = newLayoutObjectProperty(Pos.TOP_LEFT);

    private final BooleanProperty distributeRemainingRowSpaceProperty = createLayoutBooleanProperty();

    private double computedMinHeight;
    private boolean performingLayout;

    private BooleanProperty createLayoutBooleanProperty() {
        return createLayoutBooleanProperty(false);
    }

    private BooleanProperty createLayoutBooleanProperty(boolean initialValue) {
        return FXProperties.newBooleanProperty(initialValue, this::requestLayout);
    }

    private DoubleProperty newLayoutDoubleProperty() {
        return FXProperties.newDoubleProperty(this::requestLayout);
    }

    private <T> ObjectProperty<T> newLayoutObjectProperty(T initialValue) {
        return FXProperties.newObjectProperty(initialValue, this::requestLayout);
    }

    public FlexPane(Node... children) {
        getChildren().setAll(children);
        // This is necessary to clear the previous computed min/pref/max height cached value memorized in Region.min/pref/maxHeight()
        widthProperty().addListener(observable -> clearSizeCache());
        getChildren().addListener((InvalidationListener) observable -> clearSizeCache());
    }

    public FlexPane(double horizontalSpace, double verticalSpace, Node... children) {
        this(children);
        setHorizontalSpace(horizontalSpace);
        setVerticalSpace(verticalSpace);
    }

    private void clearSizeCache() {
        // Parent.clearSizeCache() is not accessible (package visibility) but requestLayout() will call it
        requestLayout();
    }

    public double getHorizontalSpace() {
        return horizontalSpaceProperty.get();
    }

    public DoubleProperty horizontalSpaceProperty() {
        return horizontalSpaceProperty;
    }

    public void setHorizontalSpace(double horizontalSpace) {
        this.horizontalSpaceProperty.set(horizontalSpace);
    }

    public double getVerticalSpace() {
        return verticalSpaceProperty.get();
    }

    public DoubleProperty verticalSpaceProperty() {
        return verticalSpaceProperty;
    }

    public void setVerticalSpace(double verticalSpace) {
        this.verticalSpaceProperty.set(verticalSpace);
    }

    public boolean isSpaceTop() {
        return spaceTopProperty.get();
    }

    public BooleanProperty spaceTopProperty() {
        return spaceTopProperty;
    }

    public void setSpaceTop(boolean spaceTop) {
        this.spaceTopProperty.set(spaceTop);
    }

    public boolean isSpaceLeft() {
        return spaceLeftProperty.get();
    }

    public BooleanProperty spaceLeftProperty() {
        return spaceLeftProperty;
    }

    public void setSpaceLeft(boolean spaceLeft) {
        this.spaceLeftProperty.set(spaceLeft);
    }

    public boolean isSpaceRight() {
        return spaceRightProperty.get();
    }

    public BooleanProperty spaceRightProperty() {
        return spaceRightProperty;
    }

    public void setSpaceRight(boolean spaceRight) {
        this.spaceRightProperty.set(spaceRight);
    }

    public boolean isSpaceBottom() {
        return spaceBottomProperty.get();
    }

    public BooleanProperty spaceBottomProperty() {
        return spaceBottomProperty;
    }

    public void setSpaceBottom(boolean spaceBottom) {
        this.spaceBottomProperty.set(spaceBottom);
    }

    public boolean isFlexLastRow() {
        return flexLastRowProperty.get();
    }

    public BooleanProperty flexLastRowProperty() {
        return flexLastRowProperty;
    }

    public void setFlexLastRow(boolean flexLastRow) {
        this.flexLastRowProperty.set(flexLastRow);
    }

    public Pos getAlignment() {
        return alignmentProperty.get();
    }

    public ObjectProperty<Pos> alignmentProperty() {
        return alignmentProperty;
    }

    public void setAlignment(Pos alignment) {
        this.alignmentProperty.set(alignment);
    }

    public boolean isDistributeRemainingRowSpace() {
        return distributeRemainingRowSpaceProperty.get();
    }

    public BooleanProperty distributeRemainingRowSpaceProperty() {
        return distributeRemainingRowSpaceProperty;
    }

    public void setDistributeRemainingRowSpace(boolean distributeRemainingRowSpace) {
        this.distributeRemainingRowSpaceProperty.set(distributeRemainingRowSpace);
    }

    private final Map<Integer, FlexBoxRow> grid = new HashMap<>();

    public static void setGrow(Node child, double value) {
        setConstraint(child, GROW_CONSTRAINT, value);
    }

    public static double getGrow(Node child) {
        Object o = getConstraint(child, GROW_CONSTRAINT);
        return o == null ? 1 : (double) o;
    }

    public static void setMargin(Node child, Insets value) {
        setConstraint(child, MARGIN_CONSTRAINT, value);
    }

    public static Insets getMargin(Node child) {
        return (Insets) getConstraint(child, MARGIN_CONSTRAINT);
    }

    // Writing setConstraint() again as Pane.setConstraint() is package private
    private static void setConstraint(Node node, Object key, Object value) {
        if (value == null)
            node.getProperties().remove(key);
        else
            node.getProperties().put(key, value);
        if (node.getParent() != null)
            node.getParent().requestLayout();
    }

    // Writing getConstraint() again as Pane.getConstraint() is package private
    private static Object getConstraint(Node node, Object key) {
        if (node.hasProperties())
            return node.getProperties().get(key);
        return null;
    }

    @Override
    protected double computeMinHeight(double width) {
        if (width < 0) {
            width = getWidth();
            if (width == 0) // This usually happens on first flex box layout when the box width is still unknown
                width = Double.MAX_VALUE; // resetting width to avoid a wrong first min height
        }
        computeLayout(width, false);
        return computedMinHeight;
    }

    @Override
    protected double computePrefHeight(double width) {
        return computeMinHeight(width);
    }

    @Override
    protected void layoutChildren() {
        performingLayout = true;
        computeLayout(getWidth(),true);
        performingLayout = false;
    }


    @Override
    public void requestLayout() {
        if (!performingLayout)
            super.requestLayout();
    }

    private void computeLayout(double width, boolean apply) {
        if (width == 0)
            return;
        // Removing the possible left & right spaces from computation
        double horizontalSpace = getHorizontalSpace(), verticalSpace = getVerticalSpace();
        width -= horizontalSpace * ( (isSpaceLeft() ? 1 : 0) + (isSpaceRight() ? 1 : 0) );

        // Initializing the grid with a first row (empty for now)
        int row = 0;
        FlexBoxRow flexBoxRow = new FlexBoxRow(), previousFlexBoxRow = null;
        grid.clear();
        addToGrid(row, flexBoxRow);

        // Getting all visible items to layout
        List<FlexBoxItem> flexBoxItems = getManagedChildren().stream().filter(Node::isVisible).map(FlexBoxItem::new).collect(Collectors.toList());

        // First pass: we add them on each row, as long as they fit inside (according to their minWidth), otherwise we create a new row
        double minWidthSum = 0, previousMinWidthSum = 0;

        for (int i = 0, n = flexBoxItems.size(); i < n; i++) {
            FlexBoxItem flexBoxItem = flexBoxItems.get(i);
            double nodeWidth = flexBoxItem.minWidth;
            minWidthSum += nodeWidth;

            if (minWidthSum >= width) {
                previousFlexBoxRow = flexBoxRow;
                previousMinWidthSum = minWidthSum;
                addToGrid(++row, flexBoxRow = new FlexBoxRow());
                minWidthSum = nodeWidth;
            }

            flexBoxRow.addItem(flexBoxItem);

            // Adding horizontal space (except for the last node)
            if (i + 1 < n)
                minWidthSum += horizontalSpace;
        }

        // If flexLastRow is true, we move some items down from previous rows to harmonize item areas
        if (isFlexLastRow() && previousFlexBoxRow != null) {
            List<FlexBoxItem> previousItems = previousFlexBoxRow.items;
            while (previousItems.size() > 1) {
                FlexBoxItem lastItem =  previousItems.get(previousItems.size() - 1);
                double lastWidth = lastItem.minWidth + horizontalSpace;
                if (minWidthSum + lastWidth > Math.min(width, previousMinWidthSum - lastWidth))
                    break;
                previousFlexBoxRow.removeItem(lastItem);
                flexBoxRow.addFirstItem(lastItem);
                previousMinWidthSum -= lastWidth;
                minWidthSum += lastWidth;
            }
        }

        // Second pass: we resize and position each item on each row
        Insets padding = getPadding();
        double y = padding.getTop() + (isSpaceTop() ? verticalSpace : 0);
        int i = 0;
        int noGridRows = grid.size();

        for (Integer rowIndex : grid.keySet()) { // Iterating over all rows
            flexBoxRow = grid.get(rowIndex); // The row
            List<FlexBoxItem> rowItems = flexBoxRow.getItems(); // All items on that row
            int noRowItems = rowItems.size();
            boolean isLastRow = i >= noGridRows - 1;

            double remainingWidth = width - flexBoxRow.rowMinWidth - (horizontalSpace * (noRowItems - 1)) - padding.getLeft() - padding.getRight();
            double flexGrowCellWidth = remainingWidth / flexBoxRow.flexGrowSum;

            double x = padding.getLeft() + (isSpaceLeft() ? horizontalSpace : 0);
            double rowMaxHeight = 0;

            for (FlexBoxItem flexBoxItem : rowItems) { // Iterating over all items on the row
                Node rowNode = flexBoxItem.node;

                double rowNodeMinWidth = flexBoxItem.minWidth;
                double rowNodeMaxWidth = rowNode.maxWidth(-1);
                //System.out.println("minWidth = " + rowNodeMaxWidth + ", maxWidth = " + rowNodeMaxWidth + " for " + rowNode);
                double rowNodeStretchedWidth = rowNodeMinWidth + (flexGrowCellWidth * flexBoxItem.grow);
                double rowNodeWidth = isLastRow && !isFlexLastRow() ? rowNodeMinWidth : Math.min(rowNodeMaxWidth, Math.max(rowNodeStretchedWidth, rowNodeMinWidth));

                double h = rowNode.prefHeight(rowNodeWidth);
                rowMaxHeight = Math.max(rowMaxHeight, h);
                /*if (apply)
                    layoutInArea(rowNode, snapPositionX(x), snapPositionY(y), snapSizeX(x + rowNodeWidth) - snapPositionX(x), snapSizeY(h), 0, flexBoxItem.margin, getAlignment().getHpos(), VPos.TOP);*/
                flexBoxItem.layoutX = x;
                flexBoxItem.layoutWidth = rowNodeWidth;
                x += rowNodeWidth + horizontalSpace;
            }

            if (apply) {
                double remainingSpaceX = isDistributeRemainingRowSpace() ? (width - x) / (rowItems.size() + 1) : 0;
                double deltaLayoutX = remainingSpaceX;
                for (FlexBoxItem flexBoxItem : rowItems) {
                    Node rowNode = flexBoxItem.node;
                    flexBoxItem.layoutX += deltaLayoutX;
                    layoutInArea(rowNode, snapPositionX(flexBoxItem.layoutX), snapPositionY(y), snapSizeX(flexBoxItem.layoutX + flexBoxItem.layoutWidth) - snapPositionX(flexBoxItem.layoutX), snapSizeY(rowMaxHeight), 0, flexBoxItem.margin, getAlignment().getHpos(), getAlignment().getVpos());
                    deltaLayoutX += remainingSpaceX;
                }
            }

            y += rowMaxHeight;
            if (!isLastRow)
                y += verticalSpace;
            i++;
        }

        // Computing and memorising computedMinHeight just from the last y position
        computedMinHeight = y + (spaceBottomProperty.get() ? verticalSpace : 0) + padding.getBottom();
    }

    private void addToGrid(int row, FlexBoxRow flexBoxRow) {
        grid.put(row, flexBoxRow);
    }

    private static final class FlexBoxItem {
        final Node node;
        final double grow;
        final double minWidth;
        final Insets margin;
        double layoutX;
        double layoutWidth;

        FlexBoxItem(Node node) {
            this.node = node;
            minWidth = node.minWidth(-1);
            grow = getGrow(node);
            margin = getMargin(node);
        }
    }

    private static final class FlexBoxRow {
        private final ArrayList<FlexBoxItem> items = new ArrayList<>();
        double rowMinWidth;
        double flexGrowSum;

        void addItem(FlexBoxItem flexBoxItem) {
            items.add(flexBoxItem);
            rowMinWidth += flexBoxItem.minWidth;
            flexGrowSum += flexBoxItem.grow;
        }

        void addFirstItem(FlexBoxItem flexBoxItem) {
            items.add(0, flexBoxItem);
            rowMinWidth += flexBoxItem.minWidth;
            flexGrowSum += flexBoxItem.grow;
        }

        void removeItem(FlexBoxItem flexBoxItem) {
            if (items.remove(flexBoxItem)) {
                rowMinWidth -= flexBoxItem.minWidth;
                flexGrowSum -= flexBoxItem.grow;
            }
        }

        ArrayList<FlexBoxItem> getItems() {
            return items;
        }
    }

}
