package edu.rpi.legup.controller;

import edu.rpi.legup.app.GameBoardFacade;
import edu.rpi.legup.app.LegupPreferences;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.model.tree.TreeElement;
import edu.rpi.legup.model.tree.TreeElementType;
import edu.rpi.legup.model.tree.TreeTransition;
import edu.rpi.legup.ui.boardview.BoardView;
import edu.rpi.legup.ui.boardview.ElementSelection;
import edu.rpi.legup.ui.boardview.ElementView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;

/**
 * {@code BoardController} is an abstract class that is a {@code ZoomViewController} for
 * {@code BoardView}s. It provides methods for accepting user input which individual puzzle
 * subclasses should use to trigger changes in the board, as well as some basic mouse interaction
 * functionality which puzzles should make use of.
 */
public abstract class BoardController <ViewType extends BoardView> extends ZoomViewController<ViewType> {

    /** Last element view moused over in a drag. */
    private ElementView lastDraggedView;

    /**
     * Flag indicating that a dragging action has been performed and is still underway
     * (see {@link #mouseDragged(ControllerMouseEvent)}).
     */
    private boolean isDragging;

    /** Constructs a controller with no initial {@code BoardView}. */
    public BoardController() {
        this.lastDraggedView = null;
        this.isDragging = false;
    }

    /**
     * Set the current {@code BoardView} to a new view.
     * <p>
     * <b>Note:</b> this method <i>does not</i> install this controller on the view.
     *
     * @param newView new {@code BoardView} to set as the controlled view
     */
    @Override
    public void setView(@Nullable ViewType newView) {
        lastDraggedView = null;
        isDragging = false;
    }

    @Override
    public void mousePressed(@NotNull ControllerMouseEvent e) {
        lastDraggedView = getElementViewAt(e.getPrecisePoint());
    }

    @Override
    public void mouseReleased(@NotNull ControllerMouseEvent e) {
        if (!isDragging) {
            ElementView view = getElementViewAt(e.getPrecisePoint());
            if (view != null) {
                selectElement(view, e.getButton());
            }
        }
        isDragging = false;
    }

    @Override
    public void mouseMoved(@NotNull ControllerMouseEvent e) {
        updateHover(getElementViewAt(e.getPrecisePoint()));
    }

    @Override
    public void mouseDragged(@NotNull ControllerMouseEvent e) {
        updateHover(getElementViewAt(e.getPrecisePoint()));

        // Dragging is triggered if and only if the cursor travels onto a new element view after previously
        // having been over a different element view; otherwise normal interaction behavior remains.
        ElementView view = getElementViewAt(e.getPrecisePoint());
        if (view != null && view != lastDraggedView) {
            if (lastDraggedView != null && canDragBetween(view, lastDraggedView)) {
                dragBetween(view, lastDraggedView, e.getButton());
                isDragging = true;
            }
            lastDraggedView = view;
        }
    }

    @Override
    public void mouseExited(@NotNull ControllerMouseEvent e) {
        updateHover(null);
    }

    /**
     * Gets the first selectable {@code ElementView} under the cursor.
     *
     * @param p the {@code Point2D} location of the cursor
     * @return the first selectable {@code ElementView} under the cursor, or {@code null} if none exists
     */
    private ElementView getElementViewAt(@NotNull Point2D p) {
        for (ElementView view : getView().getElementViews()) {
            if (isViewSelectable(view) && view.contains(p)) {
                return view;
            }
        }
        return null;
    }

    /**
     * Returns whether the given view is selectable for the purposes of triggering hovering,
     * clicking, or dragging.
     *
     * @param view the {@code ElementView} to check
     * @return {@code true} if the specified element view is selectable; {@code false} otherwise
     */
    protected boolean isViewSelectable(@NotNull ElementView view) {
        return true;
    }

    /**
     * Performs a selection or "clicking" action on a specified element view.
     *
     * @param view {@code ElementView} being selected
     * @param mouseButton mouse button used to select view
     */
    protected void selectElement(@NotNull ElementView view, int mouseButton) {}

    /**
     * Returns whether the user should be allowed to drag from one element view for another. For example,
     * only allowing dragging from neighboring element views would be implemented here.
     *
     * @param to {@code ElementView} that is being dragged to
     * @param from {@code ElementView} that is being dragged from
     * @return {@code true} to permit a drag from one element to another; {@code false} otherwise
     */
    protected boolean canDragBetween(@NotNull ElementView to, @NotNull ElementView from) {
        return false;
    }

    /**
     * Perform a dragging action between two element views.
     *
     * @param to {@code ElementView} to drag to
     * @param from {@code ElementView} to drag from
     * @param mouseButton mouse button being held while dragging
     */
    protected void dragBetween(@NotNull ElementView to, @NotNull ElementView from, int mouseButton) {}

    /**
     * Updates the hovered element view to the specified view, changes the tooltip to the
     * element's goal text, and displays the element's error message, if there is one.
     *
     * @param elementView {@code ElementView} to set as the new hovered element
     */
    protected void updateHover(@Nullable ElementView elementView) {
        BoardView boardView = getView();
        ElementSelection selection = boardView.getSelection();

        if (elementView != selection.getHover()) {
            selection.setHover(elementView);
            boardView.repaint();

            if (elementView != null) {
                PuzzleElement<?> element = elementView.getPuzzleElement();

                // Update tool tip text
                if (element.isGoal()) {
                    boardView.getPeer().setToolTipText(
                            GameBoardFacade.getInstance().getPuzzleModule().getGoal().getHoverText(element)
                    );
                }
                else {
                    boardView.getPeer().setToolTipText(null);
                }

                // Update error notification
                if (LegupPreferences.showMistakes()) {

                    TreeElement treeElement = boardView.getTreeElement();
                    String error = "";
                    if (treeElement != null
                            && treeElement.getType() == TreeElementType.TRANSITION
                            && boardView.getBoard().getModifiedData().contains(element)) {
                        TreeTransition transition = (TreeTransition) treeElement;
                        if (transition.isJustified() && !transition.isCorrect()) {
                            error = transition.getRule().checkRuleAt(transition, element);
                        }
                    }
                    boardView.updateError(error);
                }
            }
            else {
                boardView.getPeer().setToolTipText(null);
                boardView.updateError("");
            }
        }
    }
}
