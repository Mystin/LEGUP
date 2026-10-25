package edu.rpi.legup.model;

import edu.rpi.legup.model.gameboard.PuzzleElement;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * A <b>Goal</b> is an object that stores the type of goal and a list of GridCells, which holds the
 * type of cell and the cell's location. Goal's should only be constructed twice; once upon loading
 * the puzzle, and once upon saving the puzzle.
 *
 * <p>If the GoalType is DEFAULT, cellList must be null.
 */
public class Goal {
    private ArrayList<PuzzleElement<?>> elementList;
    private final GoalType goalType;
    private boolean assumeSolution;

    /**
     * Constructs a Goal object with an empty cell list with no assumed solution
     *
     * @param goalType type of goal
     */
    public Goal(GoalType goalType) {
        this.elementList = new ArrayList<>();
        this.goalType = goalType;
        this.assumeSolution = false;
    }

    /**
     * Constructs a Goal object with an empty cell list
     *
     * @param goalType type of goal
     * @param assume whether to assume there is a solution to the puzzle
     */
    public Goal(GoalType goalType, boolean assume) {
        this.elementList = new ArrayList<>();
        this.goalType = goalType;
        this.assumeSolution = assume;
    }

    /**
     * Constructs a {@code Goal} object only requiring a given cell.
     *
     * @param element Holds location and type of puzzle element
     * @param goalType type of goal
     */
    public Goal(PuzzleElement<?> element, GoalType goalType) {
        this.elementList = new ArrayList<>();
        if (element != null) {
            elementList.add(element);
        }
        this.goalType = goalType;
        this.assumeSolution = false;
    }

    /**
     * Get the goal's cells.
     *
     * @return GridCell cells
     */
    public ArrayList<PuzzleElement<?>> getElements() {
        return elementList;
    }

    public void addElement(PuzzleElement<?> element) {
        elementList.add(element);
    }

    public void removeElement(PuzzleElement<?> element) {
        elementList.remove(element);
    }

    /**
     * Get the goal type.
     *
     * @return the type of goal this is
     */
    public GoalType getType() {
        return goalType;
    }

    /**
     * Get the value of assumeSolution
     *
     * @return assumeSolution
     */
    public boolean assumeSolution() {
        return assumeSolution;
    }

    /**
     * Set the value of assumeSolution
     *
     * @param assume whether to assume there is a solution to the puzzle
     */
    public void setAssumeSolution(boolean assume) {
        assumeSolution = assume;
    }

    /**
     * Creates tool tip text for a cell being hovered over.
     *
     * @param element {@code PuzzleElement} to create text for
     * @return {@code String} tool tip text
     */
    public String getHoverText(PuzzleElement<?> element) {
        PuzzleElement<?> goalElement = null;
        for (int i = 0; i < elementList.size() && goalElement == null; ++i) {
            if (elementList.get(i).getIndex() == element.getIndex()) {
                goalElement = elementList.get(i);
            }
        }

        if (goalElement == null) {
            throw new IllegalArgumentException("Cell is not a goal condition.");
        }
        String text = "Prove this ";
        return switch (goalType) {
            case GoalType.PROVE_CELL_MUST_BE ->
                    text + "is forced to be " + goalElement.describeState(false) + ".";
            case GoalType.PROVE_CELL_MIGHT_NOT_BE ->
                    text + "is not forced to be " + goalElement.describeState(false) + ".";
            case GoalType.PROVE_SINGLE_CELL_VALUE ->
                    text + "is forced to have only one possible value.";
            case GoalType.PROVE_MULTIPLE_CELL_VALUE ->
                    text + "is forced to have multiple possible values.";
            case GoalType.PROVE_VALUES_ARE_POSSIBLE ->
                    text + "can be " + goalElement.describeState(false) + ".";
            case GoalType.PROVE_VALUES_ARE_IMPOSSIBLE ->
                    text + "cannot be " + goalElement.describeState(false) + ".";
            default -> null;
        };
    }

    /**
     * Get the text description of the goal condition.
     *
     * @return String text.
     */
    public String getGoalText() {

        if (goalType == GoalType.DEFAULT) {
            return "Find all solutions to the puzzle or prove none exist.";
        }

        String text = "Prove ";
        if (assumeSolution) {
            text += "that if there is a solution, then ";
        }
        return switch (goalType) {
            case GoalType.PROVE_CELL_MUST_BE ->
                    text
                            + getValueSeparatedGoalText(" is forced to be ", " are forced to be ")
                            + ".";
            case GoalType.PROVE_CELL_MIGHT_NOT_BE ->
                    text
                            + getValueSeparatedGoalText(
                                    " is not forced to be ", " are not forced to be ")
                            + ".";
            case GoalType.PROVE_SINGLE_CELL_VALUE -> {
                text += (elementList.size() > 1 ? "elements " : "element ");
                text += concatElementIdentities(elementList);
                text += (elementList.size() > 1 ? " are each" : " is");
                yield text + " forced to have exactly one possible value.";
            }
            case GoalType.PROVE_MULTIPLE_CELL_VALUE -> {
                text += (elementList.size() > 1 ? "elements " : "element ");
                text += concatElementIdentities(elementList);
                text += (elementList.size() > 1 ? " are each" : " is");
                yield text + " not forced to have exactly one possible value.";
            }
            case GoalType.PROVE_ANY_SOLUTION -> "Find any solution to the puzzle.";
            case GoalType.PROVE_NO_SOLUTION -> "Prove that there are no solutions to the puzzle.";
            case GoalType.PROVE_VALUES_ARE_POSSIBLE ->
                    text + getValueSeparatedGoalText(" can be ", " can be ") + " at the same time.";
            case GoalType.PROVE_VALUES_ARE_IMPOSSIBLE ->
                    text
                            + getValueSeparatedGoalText(" cannot be ", " cannot be ")
                            + " at the same time";

            default -> "Unrecognized goal condition.";
        };
    }

    /**
     * Create grammatically correct String listing cell locations.
     *
     * @param cells List of cells to stringify.
     * @return String text.
     */
    private String concatElementIdentities(ArrayList<PuzzleElement<?>> cells) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < cells.size(); ++i) {
            if (i == 1 && cells.size() == 2) {
                text.append(" and ");
            } else if (i != 0) {
                text.append(", ");
                if (i == cells.size() - 1) {
                    text.append("and ");
                }
            }
            text.append(cells.get(i).describeIdentity());
        }
        return text.toString();
    }

    /**
     * Get a TreeMap of ArrayLists of cells separated by state description string keys.
     *
     * @throws IllegalStateException when cellList is empty.
     */
    private TreeMap<String, ArrayList<PuzzleElement<?>>> getElementsByState() throws IllegalStateException {

        if (elementList.isEmpty()) throw new IllegalStateException("No goal cells to copy.");
        TreeMap<String, ArrayList<PuzzleElement<?>>> cellsByState = new TreeMap<>();

        for (PuzzleElement<?> cell : elementList) {
            String state = cell.describeState(false);
            if (!cellsByState.containsKey(state)) {
                cellsByState.put(state, new ArrayList<>());
            }
            cellsByState.get(state).add(cell);
        }
        return cellsByState;
    }

    /**
     * Get String describing how the goal condition relates goal cells to their values.
     *
     * @param singleCondition Relationship between one cell and its value.
     * @param pluralCondition Relationship between multiple cells and their values. If null,
     *     singleCondition will be used instead.
     * @return String Description text.
     */
    private String getValueSeparatedGoalText(String singleCondition, String pluralCondition) {

        if (pluralCondition == null) {
            pluralCondition = singleCondition;
        }

        TreeMap<String, ArrayList<PuzzleElement<?>>> cellsByState;
        try {
            cellsByState = getElementsByState();
        } catch (IllegalStateException e) {
            return "No condition.";
        }

        StringBuilder text = new StringBuilder();
        boolean delimiter = false;
        for (Map.Entry<String, ArrayList<PuzzleElement<?>>> state : cellsByState.entrySet()) {
            if (delimiter) {
                text.append(" and ");
            }
            delimiter = true;
            text.append(state.getValue().size() > 1 ? "cells " : "cell ");
            text.append(concatElementIdentities(state.getValue()));
            text.append(state.getValue().size() > 1 ? pluralCondition : singleCondition);
            text.append(state.getValue().getFirst().describeState(state.getValue().size() > 1));
        }
        return text.toString();
    }
}
