package edu.rpi.legup.model.gameboard;

import edu.rpi.legup.model.elements.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

/**
 * {@code PuzzleElement} represents a single element in a puzzle grid. It holds data and provides various
 * methods to manage and retrieve its properties, including modifiability, modification status, and
 * validity.
 *
 * @param <T> the type of data held by the {@code PuzzleElement}
 */
public abstract class PuzzleElement<T> {
    protected int index;
    protected T data;
    protected T goalData;
    protected boolean isModifiable;
    protected boolean isModified;
    protected boolean isGiven;
    protected boolean isGoal;
    protected boolean isValid;

    /** {@code PuzzleElement} constructor creates a new puzzle element. */
    public PuzzleElement() {
        this.index = -1;
        this.data = null;
        this.goalData = null;
        this.isModifiable = true;
        this.isModified = false;
        this.isGiven = false;
        this.isGoal = false;
        this.isValid = true;
    }

    /**
     * {@code PuzzleElement} constructor creates a new puzzle element from the specified data.
     *
     * @param data data used to create the puzzle element
     */
    public PuzzleElement(@Nullable T data) {
        this();
        this.data = data;
    }

    /**
     * Gets the data that represents this puzzle element.
     *
     * @return data value
     */
    public T getData() { return data; }

    /**
     * Sets the data value that represents this puzzle element.
     *
     * @param data data value that represents this puzzle element
     */
    public void setData(@Nullable T data) { this.data = data; }

    /**
     * Gets the data value that represents the goal condition for this puzzle element.
     *
     * @return data value that represents the goal condition for this puzzle element
     */
    public T getGoalData() { return goalData; }

    /**
     * Sets the data value that represents the goal condition for this puzzle element.
     *
     * @param goalData data value that represents the goal condition for this puzzle element
     */
    public void setGoalData(@Nullable T goalData) { this.goalData = goalData; }

    /**
     * Sets the data of this {@code PuzzleElement} to reflect the specified {@code Element}. The default
     * implementation does nothing, but subclasses can override this method to provide functionality.
     *
     * @param e the {@code Element} to reflect
     * @param m the mouse event that triggered this call; used to change behavior depending on button pressed
     */
    public void setType(@NotNull Element e, @NotNull MouseEvent m) {}

    /**
     * Gets whether this puzzle element is modifiable.
     *
     * @return {@code true} if this puzzle element is modifiable; {@code false} otherwise
     */
    public boolean isModifiable() { return isModifiable; }

    /**
     * Sets whether this puzzle element is modifiable.
     *
     * @param isModifiable {@code true} if this puzzle element is modifiable; {@code false} otherwise
     */
    public void setModifiable(boolean isModifiable) { this.isModifiable = isModifiable; }

    /**
     * Gets whether the puzzle element has been modified.
     *
     * @return {@code true} if the puzzle element has been modified; {@code false} otherwise
     */
    public boolean isModified() { return isModified; }

    /**
     * Sets whether the puzzle element has been modified.
     *
     * @param isModified {@code true} if the puzzle element has been modified; {@code false} otherwise
     */
    public void setModified(boolean isModified) { this.isModified = isModified; }

    /**
     * Gets the index of this puzzle element.
     *
     * @return index of this puzzle element
     */
    public int getIndex() { return index; }

    /**
     * Sets the index of this puzzle element.
     *
     * @param index index of this puzzle element
     */
    public void setIndex(int index) { this.index = index; }

    /**
     * Gets whether this puzzle element is given.
     *
     * @return {@code true} if the puzzle element is given; {@code false} otherwise
     */
    public boolean isGiven() { return isGiven; }

    /**
     * Sets whether this puzzle element is given.
     *
     * @param given {@code true} if the puzzle element is given; {@code false} otherwise
     */
    public void setGiven(boolean given) { isGiven = given; }

    /**
     * Gets whether this puzzle element is a goal condition.
     *
     * @return {@code true} if the puzzle element is a goal condition; {@code false} otherwise
     */
    public boolean isGoal() { return isGoal; }

    /**
     * Sets whether this puzzle element is a goal condition.
     *
     * @param goal {@code true} if the puzzle element is a goal condition; {@code false} otherwise
     */
    public void setGoal(boolean goal) { isGoal = goal; }

    /**
     * Get whether this puzzle element data is a valid change according to the rule applied to the
     * transition that this puzzle element is contained in.
     *
     * @return {@code true} if the puzzle element logically follows from the rule; {@code false} otherwise
     */
    public boolean isValid() { return this.isValid; }

    /**
     * Sets whether this puzzle element data is a valid change according to the rule applied to the
     * transition that this puzzle element is contained in.
     *
     * @param isValid {@code true} if the puzzle element logically follows from the rule; {@code false} otherwise
     */
    public void setValid(boolean isValid) { this.isValid = isValid; }

    /**
     * Defines equality between {@code PuzzleElement}s as being in the same location.
     * This is useful in most applications because of how often we need to look up
     * elements in the same location to replace or compare data against them.
     * {@link #equalsData(PuzzleElement)} is available for testing equality of data.
     * <p>
     * <b>Note:</b> Puzzles that do not have a static number of elements should override this.
     * Index can only be solely relied on for location equality if it never changes.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if {@code obj} is a {@code PuzzleElement} and {@code index == obj.index};
     * {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof PuzzleElement<?> elem && index == elem.getIndex();
    }

    /**
     * Tests whether two puzzle elements objects have the same data.
     *
     * @param puzzleElement puzzle element to check for equality
     * @return {@code true} if the puzzle element are equal; {@code false} otherwise
     */
    public boolean equalsData(@NotNull PuzzleElement<?> puzzleElement) { return data.equals(puzzleElement.data); }

    /**
     * Determines if the element has a non-unknown/non-empty value.
     * <p>
     * The default implementation is to check if the element's data is the integer 0.
     *
     * @return {@code true} if cell is "known"; {@code false} otherwise
     */
    public boolean isKnown() {
        if (data instanceof Integer) {
            return (Integer) data != 0;
        }
        return data != null;
    }

    /**
     * Get text description of element's state. Used for generating goal condition text.
     * May need to be used in a plural context when multiple elements share the same data.
     *
     * @param isPlural Flag informing if the description will be used in a plural context
     * @return {@code String} describing element's state
     */
    public String describeState(boolean isPlural) {
        return "data = " + getData();
    }

    /**
     * Get text identification for the element. It is probably easiest to use the element's
     * location to identify it in most cases. Used for generating goal condition text.
     *
     * @return {@code String} identifying the element
     */
    public String describeIdentity() { return "at index=" + index; }

    /**
     * Copies this puzzle element to a new puzzle element object.
     *
     * @return copied puzzle element object
     */
    public abstract PuzzleElement<T> copy();
}
