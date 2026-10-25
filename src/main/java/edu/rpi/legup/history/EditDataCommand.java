package edu.rpi.legup.history;

import edu.rpi.legup.app.GameBoardFacade;
import edu.rpi.legup.controller.ElementController;
import edu.rpi.legup.model.Puzzle;
import edu.rpi.legup.model.gameboard.Board;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.model.observer.ITreeListener;
import edu.rpi.legup.model.tree.*;
import edu.rpi.legup.ui.boardview.BoardView;
import edu.rpi.legup.ui.boardview.ElementView;
import edu.rpi.legup.ui.proofeditorui.treeview.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * {@code EditDataCommand} is a {@code PuzzleCommand} that edits the data of a list of puzzle element
 * within a single {@link TreeTransition}. It will create a new transition if the given selection
 * is a node with no children.
 */
public class EditDataCommand extends PuzzleCommand {

    /** {@code TreeTransition} that this command has edited. */
    private TreeTransition transition;

    /** New data to edit into the transition. */
    private final List<PuzzleElement<?>> newPuzzleElements;

    /** Saved data to bring back in the event of an undo. */
    private List<PuzzleElement<?>> oldPuzzleElements;

    /** {@code TreeViewSelection} from which to find the transition to make changes to. */
    private final TreeViewSelection selection;

    /**
     * Constructs a puzzle command to edit the board data of a {@code TreeTransition}.
     *
     * @param elements a {@code List} of {@code PuzzleElement}s to edit onto the board
     * @param selection {@code TreeViewSelection} from which to find or create a
     *        {@code TreeTransition} to apply edits to
     */
    public EditDataCommand(List<PuzzleElement<?>> elements, TreeViewSelection selection) {
        this.transition = null;
        this.newPuzzleElements = elements;
        this.oldPuzzleElements = null;
        this.selection = selection.copy();
    }

    /**
     * Constructs a puzzle command to edit the board data of a {@code TreeTransition}.
     *
     * @param element a {@code PuzzleElement} to edit onto the board
     * @param selection {@code TreeViewSelection} from which to find or create a
     *        {@code TreeTransition} to apply edits to
     */
    public EditDataCommand(PuzzleElement<?> element, TreeViewSelection selection) {
        this(List.of(element), selection);
    }

    /**
     * Executes the edit data command by finding or creating a {@code TreeTransition},
     * editing in the new puzzle elements, and propagating changes.
     */
    @Override
    public void executeCommand() {
        Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
        Tree tree = puzzle.getTree();
        TreeView treeView = GameBoardFacade.getInstance().getLegupUI().getTreePanel().getTreeView();
        BoardView boardView = GameBoardFacade.getInstance().getLegupUI().getBoardView();
        TreeElementView selectedView = selection.getFirstSelection();
        TreeElement treeElement = selectedView.getTreeElement();

        // If we haven't found transition yet, this is the first execution, and we need to do setup
        if (transition == null) {
            // If a node is selected, it would error if it already had children so we just add one
            if (treeElement.getType() == TreeElementType.NODE) {
                transition = tree.addNewTransition((TreeNode) treeElement);
                puzzle.notifyTreeListeners(listener -> listener.onTreeElementAdded(transition));
                puzzle.notifyTreeListeners(listener -> listener.onTreeSelectionChanged(
                        new TreeViewSelection(treeView.getElementView(transition))));
            }
            else {
                transition = (TreeTransition) treeElement;
            }
            // Save old board data in case of undo
            Board board = transition.getBoard();
            oldPuzzleElements = new ArrayList<>();
            for (PuzzleElement<?> element : newPuzzleElements) {
                oldPuzzleElements.add(board.getPuzzleElement(element).copy());
            }
            // Edit data in
            editBoardElements(board, newPuzzleElements);
            puzzle.notifyBoardListeners(listener -> listener.onTreeElementChanged(transition));
        }
        // This is a redo, so we have to do less work
        else {
            // If this command added the transition we saved the changes, so we just have to add it back
            if (treeElement.getType() == TreeElementType.NODE) {
                ((TreeNode) treeElement).addChild(transition);
                puzzle.notifyTreeListeners(listener -> listener.onTreeElementAdded(transition));
                puzzle.notifyTreeListeners(listener -> listener.onTreeSelectionChanged(
                        new TreeViewSelection(treeView.getElementView(transition))));
            }
            // Transition may have other modified data, so we have to edit ours back in
            else {
                editBoardElements(transition.getBoard(), newPuzzleElements);
                puzzle.notifyBoardListeners(listener -> listener.onTreeElementChanged(transition));
            }
        }


        // OLD VER
        if (treeElement.getType() == TreeElementType.NODE) {
            TreeNode treeNode = (TreeNode) treeElement;
            if (transition == null) {
                transition = tree.addNewTransition(treeNode);
            }
            else {
                treeNode.addChild(transition);
            }
            puzzle.notifyTreeListeners(listener -> listener.onTreeElementAdded(transition));
        } else {
            transition = (TreeTransition) treeElement;
        }
        Board board = transition.getBoard();

        Board prevBoard = transition.getParents().get(0).getBoard();
        ((ElementController) boardView.getController()).changeCell(event, puzzleElement);

        if (prevBoard.getPuzzleElement(selectedPuzzleElement).equalsData(puzzleElement)) {
            board.removeModifiedData(puzzleElement);
        } else {
            board.addModifiedData(puzzleElement);
        }
        transition.propagateChange(puzzleElement);

        final TreeElement finalTreeElement = transition;
        puzzle.notifyBoardListeners(listener -> listener.onTreeElementChanged(finalTreeElement));
        puzzle.notifyBoardListeners(listener -> listener.onBoardDataChanged(puzzleElement));

        puzzle.notifyTreeListeners(listener -> listener.onTreeSelectionChanged(
                new TreeViewSelection(treeView.getElementView(transition))));
    }

    /**
     * Gets the reason why the command cannot be executed
     *
     * @return if command cannot be executed, returns reason for why the command cannot be executed,
     *     otherwise null if command can be executed
     */
    @Override
    public String getErrorString() {
        List<TreeElementView> selectedViews = selection.getSelectedViews();
        if (selectedViews.size() != 1) {
            return CommandError.ONE_SELECTED_VIEW.toString();
        }
        TreeElementView selectedView = selection.getFirstSelection();
        Board board = selectedView.getTreeElement().getBoard();
        if (selectedView.getType() == TreeElementType.NODE) {
            TreeNodeView nodeView = (TreeNodeView) selectedView;
            if (!nodeView.getChildViews().isEmpty()) {
                return CommandError.UNMODIFIABLE_BOARD.toString();
            }
            for (PuzzleElement<?> element : newPuzzleElements) {
                if (!board.getPuzzleElement(element).isModifiable()) {
                    return CommandError.UNMODIFIABLE_DATA.toString();
                }
            }
        } else {
            if (!board.isModifiable()) {
                return CommandError.UNMODIFIABLE_BOARD.toString();
            } else if (!board.isModifiableCaseRule()) {
                return CommandError.UNMODIFIABLE_DATA_CASE_RULE.toString();
            }
            for (PuzzleElement<?> element : newPuzzleElements) {
                if (!board.getPuzzleElement(element).isModifiable()) {
                    return CommandError.UNMODIFIABLE_DATA.toString();
                }
            }
        }
        return null;
    }

    /** Undoes the edit data command, restoring the previous state of the puzzle element. */
    @SuppressWarnings("unchecked")
    @Override
    public void undoCommand() {
        TreeElementView selectedView = selection.getFirstSelection();
        Tree tree = getInstance().getTree();
        Puzzle puzzle = getInstance().getPuzzleModule();

        Board board = transition.getBoard();
        PuzzleElement selectedPuzzleElement = elementView.getPuzzleElement();

        if (selectedView.getType() == TreeElementType.NODE) {
            tree.removeTreeElement(transition);
            puzzle.notifyTreeListeners(
                    (ITreeListener listener) -> listener.onTreeElementRemoved(transition));
        }

        Board prevBoard = transition.getParents().get(0).getBoard();

        puzzleElement.setData(savePuzzleElement.getData());
        board.notifyChange(puzzleElement);

        if (prevBoard.getPuzzleElement(selectedPuzzleElement).equalsData(puzzleElement)) {
            board.removeModifiedData(puzzleElement);
        } else {
            board.addModifiedData(puzzleElement);
        }
        transition.propagateChange(puzzleElement);

        puzzle.notifyBoardListeners(listener -> listener.onBoardDataChanged(puzzleElement));

        final TreeElement finalTreeElement = selection.getFirstSelection().getTreeElement();
        puzzle.notifyBoardListeners(listener -> listener.onTreeElementChanged(finalTreeElement));
        puzzle.notifyTreeListeners(listener -> listener.onTreeSelectionChanged(selection));
    }

    /**
     * Edit new puzzle element data into board to match the specified elements passed in.
     * Propagates and notifies board listeners of changes.
     *
     * @param board {@code Board} to edit elements of
     * @param newElements {@code PuzzleElement}s to edit onto the board
     */
    private void editBoardElements(@NotNull Board board, @NotNull List<PuzzleElement<?>> newElements) {
        Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
        for (PuzzleElement<?> element : newElements) {
            PuzzleElement<?> boardElement = board.getPuzzleElement(element);
            if (element.getData().getClass().isInstance(boardElement.getData())) {
                boardElement.setData(element.getData());
            }
            else {
                throw new IllegalStateException();
            }
            puzzle.notifyBoardListeners(listener -> listener.onBoardDataChanged(boardElement));
        }
    }
}
