package edu.rpi.legup.ui;

import com.formdev.flatlaf.ui.FlatScrollPaneUI;
import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JViewport;
import javax.swing.plaf.ComponentUI;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * The {@code JitterlessScrollPaneUI} class is the UI delegate for the {@code JitterlessScrollPane}
 * class, enabling it to have subpixel trackpad scrolling. It extends the {@code FlatScrollPaneUI}
 * class to maintain the FlatLaf look and feel.
 */
public class JitterlessScrollPaneUI extends FlatScrollPaneUI {

    public static ComponentUI createUI(JComponent c) {
        return new JitterlessScrollPaneUI();
    }

    @Override
    protected void installDefaults(JScrollPane scrollPane) {
        super.installDefaults(scrollPane);
    }

    // A copy of FlatScrollPaneUI.createMouseWheelListener to use this.mouseWheelMovesSmooth
    @Override
    protected MouseWheelListener createMouseWheelListener() {
        MouseWheelListener superListener = super.createMouseWheelListener();
        return e -> {
            if (scrollpane instanceof JitterlessScrollPane &&
                    isSmoothScrollingEnabled() &&
                    scrollpane.isWheelScrollingEnabled() &&
                    e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL &&
                    e.getPreciseWheelRotation() != 0 &&
                    e.getPreciseWheelRotation() != e.getWheelRotation()) {

                mouseWheelMovedSmooth(e);
            } else {
                superListener.mouseWheelMoved(e);
            }
        };
    }

    // Mostly a copy of FlatScrollPaneUI.mouseWheelMovesSmooth
    private void mouseWheelMovedSmooth(MouseWheelEvent e) {

        JViewport viewport = scrollpane.getViewport();
        if (viewport == null)
            return;

        JScrollBar scrollbar = scrollpane.getVerticalScrollBar();
        if (scrollbar == null || !scrollbar.isVisible() || e.isShiftDown()) {
            scrollbar = scrollpane.getHorizontalScrollBar();
            if (scrollbar == null || !scrollbar.isVisible())
                return;
        }

        e.consume();

        double rotation = e.getPreciseWheelRotation();

        int unitIncrement;
        int orientation = scrollbar.getOrientation();
        Component view = viewport.getView();

        if (view instanceof Scrollable) {
            Scrollable scrollable = (Scrollable) view;

            Rectangle visibleRect = new Rectangle(viewport.getExtentSize());
            unitIncrement = scrollable.getScrollableUnitIncrement(visibleRect, orientation, 1);

            if (unitIncrement > 0) {

                if (orientation == SwingConstants.VERTICAL) {
                    visibleRect.y += unitIncrement;
                    visibleRect.height -= unitIncrement;
                } else {
                    visibleRect.x += unitIncrement;
                    visibleRect.width -= unitIncrement;
                }

                int unitIncrement2 = scrollable.getScrollableUnitIncrement(visibleRect, orientation, 1);
                if (unitIncrement2 > 0)
                    unitIncrement = Math.min(unitIncrement, unitIncrement2);
            }
        } else {

            int direction = rotation < 0 ? -1 : 1;
            unitIncrement = scrollbar.getUnitIncrement(direction);
        }

        // Compute new position based on previous sub-pixel offset
        JitterlessScrollPane jsp = (JitterlessScrollPane) scrollpane;
        double delta = rotation * unitIncrement * e.getScrollAmount() +
                ((orientation == SwingConstants.VERTICAL)
                ? jsp.getFracOffsetY()
                : jsp.getFracOffsetX());
        int idelta = (int) Math.round(delta);

        int value = scrollbar.getValue();
        int minValue = scrollbar.getMinimum();
        int maxValue = scrollbar.getMaximum() - scrollbar.getModel().getExtent();
        int newValue = Math.max(minValue, Math.min(value + idelta, maxValue));

        if (newValue != value)
            scrollbar.setValue(newValue);

        // Set new sub-pixel offset
        double newFracOffset =
                (value + delta > minValue && value + delta < maxValue) ?
                        delta - idelta : 0.0;
        if (orientation == SwingConstants.VERTICAL) {
            jsp.setFracOffsetY(newFracOffset);
        } else {
            jsp.setFracOffsetX(newFracOffset);
        }
    }
}
