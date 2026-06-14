package edu.rpi.legup.ui.proofeditorui.rulesview;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

/**
 * The {@code RuleButtonUI} class is the UI delegate for the {@code RuleButton} class,
 * smoothing out the raster icons that have been destructively resized to a standard size.
 * It extends the {@code FlatButtonUI} class to maintain the FlatLaf look and feel.
 */
public class RuleButtonUI extends FlatButtonUI {

    public static ComponentUI createUI(JComponent c) {
        ComponentUI ui = (ComponentUI) UIManager.get("CustomHintButtonUIInstance");
        if (ui == null) {
            ui = new RuleButtonUI();
            UIManager.put("CustomHintButtonUIInstance", ui);
        }
        return ui;
    }

    protected RuleButtonUI() {
        super(false);
    }

    @Override
    protected void paintIcon(Graphics graphics, JComponent b, Rectangle iconRect) {
        Graphics2D g = (Graphics2D) graphics.create();

        try {
            // Resizing process messes with a lot of raster images, this smoothes them out
            if (!(((AbstractButton) b).getIcon() instanceof FlatSVGIcon)) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }

            super.paintIcon(g, b, iconRect);

        } finally {
            g.dispose();
        }
    }
}