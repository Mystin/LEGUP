package edu.rpi.legup.controller;

import edu.rpi.legup.app.GameBoardFacade;
import edu.rpi.legup.model.Goal;
import edu.rpi.legup.model.GoalType;
import edu.rpi.legup.model.Puzzle;
import edu.rpi.legup.model.elements.Element;
import edu.rpi.legup.model.elements.PlaceableElement;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.ui.boardview.BoardView;
import edu.rpi.legup.ui.boardview.ElementView;
import org.jetbrains.annotations.NotNull;

/**
 * {@code EditorController} is a {@code BoardController} to be used in the puzzle editor to edit
 * the game board. Provides functionality for placing elements on the board and editing the goal
 * condition of the puzzle.
 */
public abstract class EditorController <ViewType extends BoardView> extends BoardController<ViewType> {

    /** Enum defining different selection modes that can be used in the editor. */
    public enum SelectionMode {
        PLACEMENT,
        GOAL,
    }

    /** {@code PlaceableElement} that the user has selected to place on the board. */
    private PlaceableElement selectedElement;

    /** The {@code SelectionMode} to use for editing. */
    private SelectionMode selectionMode;

    /** Constructs a controller to be used in the puzzle editor. */
    public EditorController() {
        this.selectedElement = null;
        this.selectionMode = SelectionMode.PLACEMENT;
    }

    /**
     * Sets the currently selected element to the specified {@code Element}.
     *
     * @param selectedElement the {@code Element} to set as the currently selected element
     */
    public void setSelectedElement(PlaceableElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    /**
     * Sets whether the goal condition is being modified.
     *
     * @param selectionMode the {@code SelectionMode} to use for editing
     */
    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode; // TODO: Test that selectedElement doesn't get messed up from switching
    }

    /**
     * Sets the current {@code GoalType} being used for the puzzle.
     *
     * @param goalType goal type to be used for the puzzle
     */
    public void setGoalType(GoalType goalType) {
        Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
        if (goalType != puzzle.getGoal().getType()) {
            puzzle.setGoal(new Goal(goalType));
            GameBoardFacade.getInstance()
                    .getLegupUI()
                    .getPuzzleEditor()
                    .setGoalText(puzzle.getGoal().getGoalText());
        }
    }

    @Override
    protected void selectElement(@NotNull ElementView view, int mouseButton) {
        if (selectionMode == SelectionMode.PLACEMENT && selectedElement != null ) {
            setElementData(view, selectedElement, mouseButton);
        }
        else if (selectionMode == SelectionMode.GOAL) {
            selectElementGoal(view.getPuzzleElement(), mouseButton);
        }
    }

    /**
     * Helper for {@link #selectElement} that sets the goal condition of the selected element.
     * The default implementation is to toggle whether the element is a goal.
     *
     * @param selected {@code PuzzleElement} selected while in goal placement mode
     * @param mouseButton mouse button that the user pressed to select the element
     */
    protected void selectElementGoal(@NotNull PuzzleElement<?> selected, int mouseButton) {
        Goal goal = GameBoardFacade.getInstance().getPuzzleModule().getGoal();
        if (selected.isGoal()) {
            selected.setGoal(false);
            goal.removeElement(selected);
        }
        else {
            selected.setGoal(true);
            goal.addElement(selected.copy());
        }
    }

    /**
     * Helper for {@link #selectElement} that sets the data of the element in the element view to
     * the proper value when the user has selected {@code element} from the element selector menu.
     *
     * @param view {@code ElementView} that the user interacted with, has the element to set the data of
     * @param element {@code Element} that the user selected before interacting with the view
     * @param mouseButton mouse button that the user pressed to interact with the view
     */
    protected abstract void setElementData(@NotNull ElementView view,
                                           @NotNull Element element, int mouseButton);

    @Override
    protected boolean canDragBetween(@NotNull ElementView to, @NotNull ElementView from) {
        return true;
    }

    @Override
    protected void dragBetween(@NotNull ElementView to, @NotNull ElementView from, int mouseButton) {
        selectElement(to, mouseButton);
    }
}
