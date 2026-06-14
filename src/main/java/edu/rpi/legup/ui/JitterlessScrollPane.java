package edu.rpi.legup.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * The {@code JitterlessScrollPane} class is a {@code JScrollPane} that combats jittering of
 * its contents caused by HiDPI screen scaling by translating its contents from Swing's
 * internal pixel coordinate system to the display's HiDPI-scaled pixel coordinate system.
 */
public class JitterlessScrollPane extends JScrollPane {

    // Store the sub-pixel offset from trackpad scrolling
    private double fracOffsetX = 0;
    private double fracOffsetY = 0;

    public JitterlessScrollPane(Component view) {
        super(view);
    }

    @Override
    public String getUIClassID() {
        return "JitterlessScrollPaneUI";
    }

    public void setFracOffsetX(double fracOffsetX) {
        this.fracOffsetX = fracOffsetX;
    }

    public double getFracOffsetX() {
        return fracOffsetX;
    }

    public void setFracOffsetY(double fracOffsetY) {
        this.fracOffsetY = fracOffsetY;
    }

    public double getFracOffsetY() {
        return fracOffsetY;
    }

    @Override
    public JViewport createViewport() {
        return new JitterlessViewport();
    }

    /**
     * Sets the viewport of the {@code JitterlessScrollPane} to the given viewport.
     *
     * @param viewport The new viewport to be used; Must be a {@code JitterlessViewport} or null.
     *
     * @see javax.swing.JScrollPane#setViewport
     * @see edu.rpi.legup.ui.JitterlessScrollPane.JitterlessViewport
     */
    @Override
    public void setViewport(JViewport viewport) {
        if (viewport == null || viewport instanceof JitterlessViewport) {
            super.setViewport(viewport);
        } else {
            throw new IllegalArgumentException("The viewport of a JitterlessScrollPane can only be " +
                    "set to a JitterlessViewport");
        }
    }

    /**
     * The {@code JitterlessViewport} class is a {@code JViewport} used by the
     * {@code JitterlessScrollPane} class to translate its contents to combat jittering
     * without causing clipping issues with the {@code JitterlessScrollPane}'s border.
     */
    public class JitterlessViewport extends JViewport {

        @Override
        protected void paintChildren(Graphics graphics) {

            Graphics2D g = (Graphics2D) graphics.create();

            // Get DPI scale from screen
            AffineTransform scaleDPI = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getDefaultTransform();

            // Translate graphics to snap contents to DPI-scaled screen pixel coordinates
            long scaledX = Math.round((horizontalScrollBar.getValue() + fracOffsetX) * scaleDPI.getScaleX());
            long scaledY = Math.round((verticalScrollBar.getValue() + fracOffsetY) * scaleDPI.getScaleY());
            g.translate(
                    horizontalScrollBar.getValue() - (scaledX / scaleDPI.getScaleX()),
                    verticalScrollBar.getValue() - (scaledY / scaleDPI.getScaleY())
            );

            super.paintChildren(g);
            g.dispose();
        }
    }
}
