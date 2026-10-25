package edu.rpi.legup.ui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * An abstract base class for panels in the LEGUP application. This class extends {@link JPanel} and
 * defines common properties and methods for all panels in the LEGUP user interface (currently only
 * implements toolbar scale)
 */
public abstract class LegupPanel extends JPanel {
    /** Alerts panel that it will be going visible now */
    protected final int TOOLBAR_ICON_SCALE = 40;

    /** Abstract method to make the panel visible */
    public abstract void makeVisible();

    /**
     * Adds a centered titled border to the specified component.
     *
     * @param c {@code JComponent} to give titled border
     * @param title title to put in the {@code TitledBorder}
     */
    protected void addTitledBorder(@NotNull JComponent c, @NotNull String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleJustification(TitledBorder.CENTER);
        c.setBorder(border);
    }
}
