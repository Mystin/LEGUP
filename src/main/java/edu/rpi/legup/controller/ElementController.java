package edu.rpi.legup.controller;

import static edu.rpi.legup.app.GameBoardFacade.*;

import edu.rpi.legup.app.GameBoardFacade;
import edu.rpi.legup.app.LegupPreferences;
import edu.rpi.legup.history.AutoCaseRuleCommand;
import edu.rpi.legup.history.EditDataCommand;
import edu.rpi.legup.history.ICommand;
import edu.rpi.legup.model.Goal;
import edu.rpi.legup.model.GoalType;
import edu.rpi.legup.model.Puzzle;
import edu.rpi.legup.model.elements.Element;
import edu.rpi.legup.model.elements.PlaceableElement;
import edu.rpi.legup.model.gameboard.Board;
import edu.rpi.legup.model.gameboard.CaseBoard;
import edu.rpi.legup.model.gameboard.GridBoard;
import edu.rpi.legup.model.gameboard.GridCell;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.model.tree.TreeElement;
import edu.rpi.legup.model.tree.TreeElementType;
import edu.rpi.legup.model.tree.TreeTransition;
import edu.rpi.legup.ui.DynamicView;
import edu.rpi.legup.ui.boardview.BoardView;
import edu.rpi.legup.ui.boardview.ElementSelection;
import edu.rpi.legup.ui.boardview.ElementView;
import edu.rpi.legup.ui.boardview.SelectionItemView;
import edu.rpi.legup.ui.proofeditorui.treeview.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.*;
import java.awt.geom.Point2D;

/**
 * The {@code ElementController} class manages UI interactions related to elements in a {@link BoardView}.
 * It handles mouse events, key events, and actions related to element selection and manipulation.
 */
public class ElementController extends ZoomViewController implements ActionListener {

    protected BoardView boardView;
    private Element selectedElement;
    private boolean goalPlacementMode;
    private GoalType currentGoalType;
    private PlaceableElement currentGoalValue;
    private Object goalValueData;
    private boolean assumeSolution;

    /** Constructs a controller to handle ui events associated with interacting with a {@link BoardView}. */
    public ElementController() {
        this.boardView = null;
        this.selectedElement = null;
        this.goalPlacementMode = false;
        this.currentGoalType = GoalType.DEFAULT;
        this.currentGoalValue = null;
        this.goalValueData = null;
        this.assumeSolution = false;
    }

    /**
     * Sets the currently selected element to the specified {@code Element}.
     *
     * @param selectedElement the {@code Element} to set as the currently selected element
     */
    public void setSelectedElement(Element selectedElement) { this.selectedElement = selectedElement; }

    /**
     * Sets whether the goal condition is being modified.
     *
     * @param goalPlacementMode {@code true} if the goal is being modified; {@code false} otherwise
     */
    public void setGoalPlacementMode(boolean goalPlacementMode) {
        this.goalPlacementMode = goalPlacementMode;
        if (goalPlacementMode) { this.selectedElement = null; }
    }

    /**
     * Sets the current {@code GoalType} being used for the puzzle.
     *
     * @param goalType goal type to be used for the puzzle
     */
    public void setGoalType(GoalType goalType) {
        this.currentGoalType = goalType;
        Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
        if (puzzle != null) {
            puzzle.setGoal(new Goal(goalType));
            // If the editor panel exists, update its displayed goal text
            if (GameBoardFacade.getInstance().getLegupUI() != null
                    && GameBoardFacade.getInstance().getLegupUI().getPuzzleEditor() != null) {
                GameBoardFacade.getInstance()
                        .getLegupUI()
                        .getPuzzleEditor()
                        .setGoalText(puzzle.getGoal().getGoalText());
            }
        }
    }

    public void setGoalValue(PlaceableElement selectedElement) { this.currentGoalValue = selectedElement; }

    public void setGoalValueData(Object valueData) { this.goalValueData = valueData; }

    public GoalType getCurrentGoalType() { return this.currentGoalType; }

    public PlaceableElement getCurrentGoalValue() { return this.currentGoalValue; }

    public void setAssumeSolution(boolean assume) {
        this.assumeSolution = assume;
        Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
        puzzle.getGoal().setAssumeSolution(assume);
    }

    /**
     * Sets the {@code BoardView}.
     *
     * @param boardView board view
     */
    public void setBoardView(@Nullable BoardView boardView) { this.boardView = boardView; }

    @Override
    public void mouseReleased(@NotNull ControllerMouseEvent e) { // TODO: this whole thing needs a rewrite
        TreePanel treePanel = GameBoardFacade.getInstance().getLegupUI().getTreePanel();
        TreeView treeView = null;
        if (treePanel != null) {
            treeView = treePanel.getTreeView();
        }

        BoardView boardView = getInstance().getLegupUI().getBoardView();
        if (boardView == null) {
            boardView = getInstance().getLegupUI().getEditorBoardView();
        }

        Board board = boardView.getBoard();
        ElementView elementView = boardView.getElement(e.getPrecisePoint());
        TreeViewSelection selection = null;
        if (treeView != null) {
            selection = treeView.getSelection();
        }
        // funny
        if (elementView != null) {
            ICommand command;
            if (board instanceof CaseBoard caseBoard) {
                command = new AutoCaseRuleCommand(elementView, selection, caseBoard.getCaseRule(), caseBoard, e);
            }
            else { command = new EditDataCommand(elementView, selection, e); }

            if (command.canExecute()) {
                command.execute();
                getInstance().getHistory().pushChange(command);
                if (treeView != null) {
                    treeView.updateError("");
                }
            } else {
                if (treeView != null) {
                    treeView.updateError(command.getError());
                }
            }
        }
        //        if (selectedElement != null) {
        GridBoard b = (GridBoard) this.boardView.getBoard();
        Point2D point = e.getPrecisePoint();

        if (goalPlacementMode) {
            PuzzleElement selectedCell = b.getPuzzleElements().get(boardView.boardIndexAt(point));
            if (selectedCell != null) {
                // Get the type of the goal value to determine how to set the cell's data and goal
                // data
                PlaceableElement element = (PlaceableElement) goalValueData;
                // If the element being placed isn't a number tile, simply toggle the cell's goal
                // status
                if (!element.toString().equals("Number Tile")) {
                    selectedCell.setGoal(!selectedCell.isGoal());
                }
                // If the element is a number tile and not already a goal cell, toggle the cell's
                // goal status
                // and set the goal data to the data value of the cell before placement
                else if (element.toString().equals("Number Tile") && !selectedCell.isGoal()) {
                    selectedCell.setGoal(!selectedCell.isGoal());
                    PuzzleElement tempCell = selectedCell.copy();
                    selectedCell.setGoalData(tempCell.getData());
                }
                if (selectedCell.isGoal() && goalValueData != null) {
                    // Create a temporary cell copy to determine what data value this element
                    // represents
                    PuzzleElement tempCell = selectedCell.copy();
                    if (element.toString().equals("Number Tile")) {
                        tempCell.setType(element, e);
                    } else {
                        selectedCell.setGoalData(tempCell.getData());
                        tempCell.setType(element, null);
                    }
                    // Set the cell's data to the goal value's data so the cell reflects the goal
                    // condition in the editor
                    selectedCell.setData(tempCell.getData());
                }
                // If the cell was already a goal and is being toggled off, clear the goal data and
                // reset the cell's data to the non-goal state
                else if (!selectedCell.isGoal()) {
                    selectedCell.setData(selectedCell.getGoalData());
                    selectedCell.setGoalData(null);
                }

                // Keep the Puzzle.goal cell list in sync with the cell's goal flag so
                // Goal.getHoverText(...) can find the goal cell when hovering.
                Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
                if (puzzle != null && selectedCell instanceof GridCell) {
                    GridCell<?> gridCell = (GridCell<?>) selectedCell;
                    Goal goal = puzzle.getGoal();
                    // If cell was just marked as a goal, add to goal list if not already present
                    if (selectedCell.isGoal()) {
                        boolean exists = false;
                        for (GridCell c : goal.getElements()) {
                            if (c.getLocation().equals(gridCell.getLocation())) {
                                exists = true;
                                // If cell is a number tile, update the goal cell in the goal list
                                // to have the correct data for hover text
                                if (element.toString().equals("Number Tile")) {
                                    goal.getElements().remove(c);
                                    goal.addElement(gridCell.copy());
                                }
                                break;
                            }
                        }
                        if (!exists) {
                            // Add a copy so Goal stores an independent snapshot of the goal cell
                            goal.addElement(gridCell.copy());
                        }

                    } else {
                        // If cell was unmarked as a goal, remove any matching location from the
                        // goal list
                        var itr = goal.getElements().iterator();
                        while (itr.hasNext()) {
                            GridCell c = itr.next();
                            if (c.getLocation().equals(gridCell.getLocation())) {
                                itr.remove();
                            }
                        }
                    }
                    // Update the editor's goal text so the UI reflects the new goal condition
                    if (GameBoardFacade.getInstance().getLegupUI() != null
                            && GameBoardFacade.getInstance().getLegupUI().getPuzzleEditor()
                                    != null) {
                        GameBoardFacade.getInstance()
                                .getLegupUI()
                                .getPuzzleEditor()
                                .setGoalText(goal.getGoalText());
                    }
                }
            }
        } else {
            b.setCell(scaledPoint.x, scaledPoint.y, this.selectedElement, e);
        }
        boardView.repaint();
    }

    @Override
    public void mouseExited(@NotNull ControllerMouseEvent e) {
        if (boardView.getSelection().getHover() != null) {
            boardView.getSelection().clearHover();
            boardView.repaint();
        }
    }

    @Override
    public void mouseDragged(@NotNull ControllerMouseEvent e) { updateHover(e.getPrecisePoint()); }

    @Override
    public void mouseMoved(@NotNull ControllerMouseEvent e) { updateHover(e.getPrecisePoint()); }

    /**
     * Updates which element the hover is being applied to.
     *
     * @param point location of the cursor
     */
    private void updateHover(@NotNull Point2D point) {
        BoardView boardView = getInstance().getLegupUI().getBoardView();
        if (boardView == null) {
            boardView = getInstance().getLegupUI().getEditorBoardView();
        }
        DynamicView dynamicView = getInstance().getLegupUI().getDynamicBoardView();
        if (dynamicView == null) {
            dynamicView = getInstance().getLegupUI().getEditorDynamicBoardView();
        }
        Board board = boardView.getBoard();
        TreeElement treeElement = boardView.getTreeElement();
        ElementView elementView = boardView.getElement(point);
        ElementSelection selection = boardView.getSelection();
        String error = null;

        if (elementView != null && elementView != selection.getHover()) {
            selection.setHover(elementView);

            PuzzleElement cell = elementView.getPuzzleElement();
            if (cell instanceof GridCell && cell.isGoal()) {
                // Safely attempt to get hover text from the puzzle goal. Goal.getHoverText throws
                // IllegalArgumentException if the cell isn't present in the Goal's cell list, so
                // guard against that and any null puzzles.
                Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
                if (puzzle != null) {
                    try {
                        String hoverText = puzzle.getGoal().getHoverText((GridCell) cell);
                        boardView.getPeer().setToolTipText(hoverText);
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        // If goal data not found or something else is null, clear tooltip instead
                        boardView.getPeer().setToolTipText(null);
                    }
                } else {
                    boardView.getPeer().setToolTipText(null);
                }
            } else {
                boardView.getPeer().setToolTipText(null);
            }

            if (LegupPreferences.showMistakes()) {
                PuzzleElement element = elementView.getPuzzleElement();
                if (treeElement != null
                        && treeElement.getType() == TreeElementType.TRANSITION
                        && board.getModifiedData().contains(element)) {
                    TreeTransition transition = (TreeTransition) treeElement;
                    if (transition.isJustified() && !transition.isCorrect()) {
                        error = transition.getRule().checkRuleAt(transition, element);
                    }
                }
                if (error != null) {
                    dynamicView.updateError(error);
                } else {
                    dynamicView.resetStatus();
                }
            }
            boardView.repaint();
        }
    }

    /**
     * Callback invoked when a cell is changed by a mouse event. Intended to be overridden by
     * subclasses to handle puzzle-specific cell modification logic.
     *
     * @param e the mouse event that triggered the cell change
     * @param data the {@link PuzzleElement} representing the cell being changed
     */
    public void changeCell(MouseEvent e, PuzzleElement data) {}

    // Action fired from SelectionItemView in a DataSelectionView
    @SuppressWarnings("Unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        BoardView boardView = getInstance().getLegupUI().getBoardView();
        if (boardView == null) {
            boardView = getInstance().getLegupUI().getEditorBoardView();
        }
        ElementView selectedElement = boardView.getSelection().getFirstSelection();
        PuzzleElement puzzleElement = selectedElement.getPuzzleElement();

        TreeView treeView = GameBoardFacade.getInstance().getLegupUI().getTreePanel().getTreeView();
        TreeViewSelection selection = treeView.getSelection();
        TreeElementView selectedView = selection.getFirstSelection();
        TreeTransitionView transitionView = (TreeTransitionView) selectedView;

        Board prevBord = transitionView.getTreeElement().getParents().get(0).getBoard();

        int value = (Integer) ((SelectionItemView) e.getSource()).getData().getData();

        puzzleElement.setData(value);

        if (puzzleElement.equalsData(prevBord.getPuzzleElement(puzzleElement))) {
            puzzleElement.setModified(false);
        } else {
            puzzleElement.setModified(true);
        }

        transitionView.getTreeElement().propagateChange(puzzleElement);

        boardView.repaint();
        boardView.getSelection().clearSelection();
    }

    /**
     * Invoked when a key has been typed. See the class description for {@link KeyEvent} for a
     * definition of a key typed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(@NotNull KeyEvent e) {}

    /**
     * Invoked when a key has been pressed. See the class description for {@link KeyEvent} for a
     * definition of a key pressed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(@NotNull KeyEvent e) {}

    /**
     * Invoked when a key has been released. See the class description for {@link KeyEvent} for a
     * definition of a key released event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(@NotNull KeyEvent e) {
        Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
        BoardView boardView = getInstance().getLegupUI().getBoardView();
        if (boardView == null) {
            boardView = getInstance().getLegupUI().getEditorBoardView();
        }
        Board board = boardView.getBoard();
        if (board instanceof CaseBoard) {
            CaseBoard caseBoard = (CaseBoard) board;
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                puzzle.notifyBoardListeners(listener -> listener.onCaseBoardAdded(caseBoard));
            }
        }
    }
}
