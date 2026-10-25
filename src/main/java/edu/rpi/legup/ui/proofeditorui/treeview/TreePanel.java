package edu.rpi.legup.ui.proofeditorui.treeview;

import edu.rpi.legup.app.GameBoardFacade;
import edu.rpi.legup.controller.TreeController;
import edu.rpi.legup.history.AddTreeElementCommand;
import edu.rpi.legup.history.DeleteTreeElementCommand;
import edu.rpi.legup.history.ICommand;
import edu.rpi.legup.history.MergeCommand;
import edu.rpi.legup.ui.zoompane.ZoomPane;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * {@code TreePanel} is a JPanel that manages and displays a tree view with associated toolbar and
 * status information. It provides methods to interact with the tree view, such as adding, deleting,
 * and merging tree elements, and updating the status based on actions performed.
 */
public class TreePanel extends JPanel {

    private final ZoomPane treeViewPane;
    private final TreeView treeView;
    private final TreeToolbarPanel toolbar;

    /** Constructs a {@code TreePanel} and initializes the UI components. */
    public TreePanel() {

        setLayout(new BorderLayout());
        TitledBorder title = BorderFactory.createTitledBorder("Proof Tree");
        title.setTitleJustification(TitledBorder.CENTER);
        setBorder(title);

        treeView = new TreeView(new TreeController());

        toolbar = new TreeToolbarPanel(this);
        treeViewPane = new ZoomPane(treeView);

        add(treeViewPane, BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);

        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        if (treeViewPane != null) {
            treeViewPane.putClientProperty("ZoomPane.viewPadding", null);
            Insets treeViewPadding = UIManager.getInsets("Tree.viewPadding");
            if (treeViewPadding != null) {
                treeViewPane.putClientProperty("ZoomPane.viewPadding", treeViewPadding);
            }
        }
    }

    /**
     * Gets the {@code TreeView} instance associated with this panel.
     *
     * @return the {@code TreeView} instance
     */
    public TreeView getTreeView() { return treeView; }

    /**
     * Adds a new tree element by executing an {@link AddTreeElementCommand}. If the command cannot
     * be executed, it updates the error message.
     */
    public void add() {
        TreeViewSelection selection = treeView.getSelection();

        String error = "";
        AddTreeElementCommand add = new AddTreeElementCommand(selection);

        if (add.canExecute()) {
            add.execute();
            GameBoardFacade.getInstance().getHistory().pushChange(add);
        }
        else { error = add.getError(); }

        treeView.updateError(error);
    }

    /**
     * Deletes the selected tree element by executing a {@link DeleteTreeElementCommand}. If the
     * command cannot be executed, it updates the error message.
     */
    public void delete() {
        TreeViewSelection selection = treeView.getSelection();

        String error = "";
        DeleteTreeElementCommand del = new DeleteTreeElementCommand(selection);

        if (del.canExecute()) {
            del.execute();
            GameBoardFacade.getInstance().getHistory().pushChange(del);
        }
        else { error = del.getError(); }

        treeView.updateError(error);
    }

    /**
     * Merges selected tree elements by executing a {@link MergeCommand}. If the command cannot be
     * executed, it updates the error message.
     */
    public void merge() {
        TreeViewSelection selection = treeView.getSelection();

        String error = "";
        ICommand merge = new MergeCommand(selection);

        if (merge.canExecute()) {
            merge.execute();
            GameBoardFacade.getInstance().getHistory().pushChange(merge);
        }
        else { error = merge.getError(); }

        treeView.updateError(error);
    }

    /**
     * Toggles the collapsed state of the selected tree elements. If an element is collapsed, it
     * will be expanded, and vice versa.
     */
    public void collapse() {
        TreeViewSelection selection = treeView.getSelection();
        for (TreeElementView view : selection.getSelectedViews()) {
            view.setCollapsed(!view.isCollapsed());
        }
    }
}
