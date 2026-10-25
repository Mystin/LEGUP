package edu.rpi.legup.model.gameboard;

import edu.rpi.legup.model.Goal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract class representing a game board. This class provides functionality for managing puzzle
 * elements, tracking modifications, and determining if the board is modifiable.
 */
public abstract class Board <ElementData> {

    protected List<PuzzleElement<ElementData>> puzzleElements;
    protected Set<PuzzleElement<ElementData>> modifiedData;
    protected Goal goal;
    protected boolean isModifiable;
    protected boolean isModifiableCaseRule;

    /** {@code Board} constructor creates an empty board. */
    public Board() {
        this.puzzleElements = new ArrayList<>();
        this.modifiedData = new HashSet<>();
        this.isModifiable = true;
        this.isModifiableCaseRule = true;
        this.goal = null;
    }

    /**
     * {@code Board} constructor creates a board with null elements.
     *
     * @param size number of elements for the board
     */
    public Board(int size) {
        this();
        for (int i = 0; i < size; i++) { puzzleElements.add(null); }
    }

    /**
     * Gets a specific {@code PuzzleElement} from the board.
     *
     * @param puzzleElement the puzzle element to retrieve
     * @return the puzzle element at the corresponding index, or {@code null} if not found
     */
    public PuzzleElement<ElementData> getPuzzleElement(@NotNull PuzzleElement<ElementData> puzzleElement) {
        int index = puzzleElement.getIndex();
        return 0 < index && index < puzzleElements.size() ? puzzleElements.get(index) : null;
    }

    /**
     * Sets a specific {@code PuzzleElement} on the board.
     *
     * @param index index of the puzzle element
     * @param puzzleElement the puzzle element to set at the index
     */
    public void setPuzzleElement(int index, @Nullable PuzzleElement<ElementData> puzzleElement) {
        if (0 < index && index < puzzleElements.size()) { puzzleElements.set(index, puzzleElement); }
    }

    /**
     * Gets the number of elements on the board.
     *
     * @return number of elements on the board
     */
    public int getElementCount() { return puzzleElements.size(); }

    /**
     * Gets the {@code PuzzleElement}s on the board.
     *
     * @return puzzle elements on the board
     */
    public List<PuzzleElement<ElementData>> getPuzzleElements() { return puzzleElements; }

    /**
     * Sets the {@code PuzzleElement}s on the board.
     *
     * @param puzzleElements elements on the board
     */
    public void setPuzzleElements(@NotNull List<PuzzleElement<ElementData>> puzzleElements) {
        this.puzzleElements = List.copyOf(puzzleElements);
    }

    /**
     * Gets the modifiable attribute for the board.
     *
     * @return {@code true} if the board is modifiable; {@code false} otherwise
     */
    public boolean isModifiable() { return isModifiable; }

    /**
     * Sets the modifiable attribute for the board.
     *
     * @param isModifiable {@code true} if the board is modifiable; {@code false} otherwise
     */
    public void setModifiable(boolean isModifiable) { this.isModifiable = isModifiable; }

    /**
     * Gets whether this board is modifiable as a result of a case rule.
     *
     * @return {@code true} if this board is modifiable; false otherwise
     */
    public boolean isModifiableCaseRule() { return isModifiableCaseRule; }

    /**
     * Sets whether this board is modifiable as a result of a case rule.
     *
     * @param isModifiableCaseRule {@code true} if this board is modifiable; {@code false} otherwise
     */
    public void setModifiableCaseRule(boolean isModifiableCaseRule) {
        this.isModifiableCaseRule = isModifiableCaseRule;
    }

    /**
     * Gets whether any of {@code PuzzleElement}s of this board has been modified by the user.
     *
     * @return {@code true} if the board has been modified; {@code false} otherwise
     */
    public boolean isModified() { return !modifiedData.isEmpty(); }

    /**
     * Gets the set of modified {@code PuzzleElement}s from the board.
     *
     * @return set of modified puzzle element from the board
     */
    public Set<PuzzleElement<ElementData>> getModifiedData() { return modifiedData; }

    /**
     * Adds a {@code PuzzleElement} that has been modified to the list.
     *
     * @param puzzleElement puzzle element that has been modified
     */
    public void addModifiedData(@NotNull PuzzleElement<ElementData> puzzleElement) {
        modifiedData.add(puzzleElement);
        puzzleElement.setModified(true);
    }

    /**
     * Removes a {@code PuzzleElement} that has no longer been modified from the list.
     *
     * @param data puzzle element that has no longer been modified
     */
    public void removeModifiedData(@NotNull PuzzleElement<ElementData> data) {
        modifiedData.remove(data);
        data.setModified(false);
    }

    /**
     * Called when a {@code PuzzleElement} on this board's data has changed and passes in the equivalent
     * puzzle element with the new data.
     *
     * @param puzzleElement equivalent puzzle element with the new data
     */
    public void notifyChange(@NotNull PuzzleElement<ElementData> puzzleElement) {
        puzzleElements.set(puzzleElement.getIndex(), puzzleElement);
    }

    /**
     * Called when a {@code PuzzleElement} has been added and passes in the equivalent puzzle
     * element with the data.
     *
     * @param puzzleElement equivalent puzzle element
     */
    public void notifyAddition(@NotNull PuzzleElement<ElementData> puzzleElement) {}

    /**
     * Called when a {@code PuzzleElement} has been deleted and passes in the equivalent puzzle element.
     *
     * @param puzzleElement equivalent puzzle element
     */
    public void notifyDeletion(@NotNull PuzzleElement<ElementData> puzzleElement) {}

    /**
     * Creates a {@code Board} that is the result of merging all the boards in {@code boards}. For each puzzle
     * element, if the value is shared by all of the {@code boards}, that value is used; otherwise the value from
     * {@code lca} is used.
     *
     * @param lca the lowest common ancestor of all of the {@code boards}
     * @param boards the boards to merge
     * @return the result of merging all of the {@code boards}
     */
    public Board<ElementData> mergedBoard(@NotNull Board<ElementData> lca,
                                          @NotNull List<Board<ElementData>> boards) {
        Board<ElementData> mergedBoard = lca.copy();

        Board<ElementData> firstBoard = boards.getFirst();
        for (PuzzleElement<ElementData> lcaData : lca.getPuzzleElements()) {
            PuzzleElement<ElementData> sharedData = firstBoard.getPuzzleElement(lcaData);

            boolean isShared = true;
            for (Board<ElementData> board : boards) {
                if (!sharedData.equalsData(board.getPuzzleElement(lcaData))) {
                    isShared = false;
                    break;
                }
            }

            if (isShared && !lcaData.equalsData(sharedData)) {
                PuzzleElement<ElementData> mergedData = mergedBoard.getPuzzleElement(lcaData);
                mergedData.setData(sharedData.getData());
                mergedBoard.addModifiedData(mergedData);
            }
        }

        return mergedBoard;
    }

    /**
     * Determines if this board contains the equivalent puzzle elements as the one specified.
     *
     * @param board board to check equivalence
     * @return {@code true} if the boards are equivalent; {@code false} otherwise
     */
    public boolean equalsBoard(@NotNull Board<ElementData> board) {
        for (PuzzleElement<ElementData> element : puzzleElements) {
            if (!element.equalsData(board.getPuzzleElement(element))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Performs a deep copy of this board.
     *
     * @return a new copy of the board
     */
    public abstract Board<ElementData> copy();
}
