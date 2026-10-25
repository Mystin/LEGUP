package edu.rpi.legup.ui.boardview;

import edu.rpi.legup.controller.ElementController;
import edu.rpi.legup.model.gameboard.Board;
import edu.rpi.legup.model.gameboard.CaseBoard;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.model.observer.IBoardListener;
import edu.rpi.legup.model.tree.TreeElement;
import edu.rpi.legup.ui.zoompane.ZoomView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract class representing a view for a board in a puzzle game. {@code BoardView}s handle the visual
 * representation of and displaying user interactions with the board elements.
 */
public abstract class BoardView extends ZoomView implements IBoardListener {
    protected TreeElement treeElement;
    protected Board board;
    protected ArrayList<ElementView> elementViews;
    protected ElementController elementController;
    protected ElementSelection selection;

    /**
     * {@code BoardView} constructor creates a view for the board object utilizing a controller to handle any ui
     * events.
     *
     * @param controller controller that handles ui events
     */
    public BoardView(@Nullable ElementController controller) {
        super(controller);
        this.treeElement = null;
        this.board = null;
        this.elementViews = new ArrayList<>();
        this.selection = new ElementSelection();
    }

    /**
     * Gets the proper size of the board view.
     *
     * @return size of the board view
     */
    @NotNull protected abstract Dimension getProperSize();

    /**
     * Gets the {@code ElementView} from the index or {@code null} if out of bounds.
     *
     * @param index index of the {@code ElementView}
     * @return {@code ElementView} at the specified index
     */
    public abstract ElementView getElement(int index);

    /**
     * Sets the {@code ElementView} list.
     *
     * @param elements list of {@code ElementView}s
     */
    public void setElementViews(@NotNull ArrayList<ElementView> elements) { elementViews = elements; }

    /**
     * Gets the {@code ElementView} from the location specified or {@code null} if one does not exist at that
     * location.
     *
     * @param point location in view coordinates
     * @return {@code ElementView} at the specified location, or {@code null} if none exists
     */
    public ElementView getElement(@NotNull Point2D point) {
        for (ElementView element : elementViews) {
            if (element.isWithinBounds(point)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Gets the {@code ElementSelection} for this {@code BoardView}.
     *
     * @return the {@code ElementSelection}
     */
    @NotNull public ElementSelection getSelection() { return selection; }

    /**
     * Gets the {@code Board} associated with this view.
     *
     * @return {@code Board} being viewed
     */
    public Board getBoard() { return board; }

    /**
     * Sets the {@code Board} associated with this view.
     *
     * @param board {@code Board} to view
     */
    public void setBoard(@Nullable Board board) {
        if (this.board != board) {
            this.board = board;

            if (board instanceof CaseBoard) {
                syncWithBoard(((CaseBoard) board).getBaseBoard());
                setCasePickable();
            }
            else { syncWithBoard(board); }
        }
    }

    /**
     * Sync the state of the {@code BoardView} with that of the current {@code Board}.
     *
     * @param board the {@code Board} to sync with
     */
    protected void syncWithBoard(@Nullable Board board) { // TODO: remove parameter once CaseBoard is actually a board
        if (board == null) {
            elementViews = new ArrayList<>();
            selection.clearSelection();
            selection.clearHover();
        }
        else {
            List<PuzzleElement> elementList = new ArrayList<>(board.getPuzzleElements());
            Iterator<ElementView> iterator = elementViews.iterator();

            while (iterator.hasNext()) {
                ElementView view = iterator.next();
                PuzzleElement element = board.getPuzzleElement(view.getPuzzleElement());

                if (element == null) { iterator.remove(); }
                else {
                    view.setPuzzleElement(element);
                    view.setShowCasePicker(false);
                    elementList.remove(element);
                }
            }

            for (PuzzleElement element : elementList) { elementViews.add(createElementView(element)); }
        }
    }

    /**
     * Create an {@code ElementView} for the specified {@code PuzzleElement}.
     *
     * @param element {@code PuzzleElement} to create view for
     * @return {@code ElementView} to display {@code element}
     */
    protected abstract ElementView createElementView(PuzzleElement element);

    /** Configures the view to handle case interactions. */
    protected void setCasePickable() {
        for (ElementView elementView : elementViews) {
            elementView.setShowCasePicker(true);
            elementView.setCaseRulePickable(((CaseBoard) board).isPickable(elementView.getPuzzleElement(), null));
        }
    }

    /**
     * Called when the tree element has changed.
     *
     * @param treeElement tree element that changed
     */
    @Override
    public void onTreeElementChanged(@NotNull TreeElement treeElement) {
        this.treeElement = treeElement;
        setBoard(treeElement.getBoard());
        repaint();
    }

    /**
     * Called when a case board has been added to the view.
     *
     * @param caseBoard case board to be added
     */
    @Override
    public void onCaseBoardAdded(@NotNull CaseBoard caseBoard) {
        setBoard(caseBoard);
        repaint();
    }

    /**
     * Gets the current tree element.
     *
     * @return the current {@code TreeElement}, or {@code null} if none is set
     */
    public TreeElement getTreeElement() { return this.treeElement; }

    /**
     * Gets the number of element views in this board view.
     *
     * @return the number of element views
     */
    public int getElementCount() { return elementViews.size(); }

    /**
     * Gets the {@code ElementView}s associated with the {@code BoardView}.
     *
     * @return list of {@code ElementView}s
     */
    @NotNull public ArrayList<ElementView> getElementViews() { return elementViews; }

    @Override
    public void draw(@NotNull Graphics graphics) { drawBoard((Graphics2D) graphics); }

    /**
     * Draws the board and its elements.
     *
     * @param graphics2D the {@code Graphics2D} context used for drawing
     */
    public void drawBoard(@NotNull Graphics2D graphics2D) {
        for (ElementView element : elementViews) { element.draw(graphics2D); }
    }

    @Override
    public void onBoardDataChanged(@NotNull PuzzleElement puzzleElement) {
        for (ElementView view : elementViews) {
            if (view.getPuzzleElement().getIndex() == puzzleElement.getIndex()) {
                repaint(view.getBounds2D());
                return;
            }
        }
    }

    /**
     * Gets the index of the puzzle element being displayed at the specified point.
     *
     * @param p {@code Point2D} in view coordinates
     * @return index of the puzzle element at the specified point or {@code -1} if there is none
     */
    public abstract int boardIndexAt(@NotNull Point2D p);

    /**
     * Gets the selection popup menu for this board view.
     *
     * @return the {@code DataSelectionView} associated with this view
     */
    @NotNull public abstract DataSelectionView getSelectionPopupMenu();
}
