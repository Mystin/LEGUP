package edu.rpi.legup.puzzle.lightup;

import edu.rpi.legup.model.gameboard.GridBoard;
import edu.rpi.legup.model.gameboard.GridCell;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightUpBoard extends GridBoard {

    /**
     * Creates a {@code LightUpBoard} of the specified width and height.
     *
     * @param width width of the board
     * @param height height of the board
     */
    public LightUpBoard(int width, int height) { super(width, height); }

    /**
     * Creates a square {@code LightUpBoard} of the specified size.
     *
     * @param size width and height of the board
     */
    public LightUpBoard(int size) { this(size, size); }

    /**
     * Recalculates the light effects of all bulbs on the board. In most cases it is better to call
     * {@link #calculateBulbEffect(int, int, boolean)}.
     */
    public void recalculateBulbEffects() {
        for (int y = 0; y < this.dimension.height; y++) {
            for (int x = 0; x < this.dimension.width; x++) {
                getCell(x, y).resetLightLevel();
            }
        }

        for (int y = 0; y < this.dimension.height; y++) {
            for (int x = 0; x < this.dimension.width; x++) {
                if (getCell(x, y).getType() == LightUpCellType.BULB) { calculateBulbEffect(x, y, true); }
            }
        }
    }



    /**
     * Adds or removes light from cells in the area of effect of the given location based on the
     * value of {@code addingLight}.
     *
     * @param x the x-coordinate of the cell to calculate the area of effect of
     * @param y the y-coordinate of the cell to calculate the area of effect of
     * @param addingLight {@code true} if light should be added to cells in the area of effect; {@code false}
     *                                if light should be removed
     */
    public void calculateBulbEffect(int x, int y, boolean addingLight) {
        if (inBounds(x, y)) {
            Consumer<LightUpCell> lightEffect = addingLight ? LightUpCell::addLight : LightUpCell::removeLight;
            Predicate<LightUpCellType> validType =
                    type -> type != LightUpCellType.NUMBER && type != LightUpCellType.BLACK;
            lightEffect.accept(getCell(x, y));
            LightUpCell cell;

            // Apply light effect in cardinal directions until NUMBER or BLACK encountered or run out of bounds
            int i = x;
            while (inBounds(--i, y) && validType.test((cell = getCell(i, y)).getType())) { lightEffect.accept(cell); }
            i = x;
            while (inBounds(++i, y) && validType.test((cell = getCell(i, y)).getType())) { lightEffect.accept(cell); }
            i = y;
            while (inBounds(x, --i) && validType.test((cell = getCell(x, i)).getType())) { lightEffect.accept(cell); }
            i = y;
            while (inBounds(x, ++i) && validType.test((cell = getCell(x, i)).getType())) { lightEffect.accept(cell); }
        }
    }

    /**
     * Gets adjacent cells to the specified cell.
     *
     * @param cell {@code LightUpCell}
     * @return {@code Set} of adjacent {@code LightUpCell}s
     */
    public @NotNull Set<LightUpCell> getAdj(@NotNull LightUpCell cell) {
        Set<LightUpCell> adjCells = new HashSet<>();
        cell = (LightUpCell) getPuzzleElement(cell);

        Point loc = cell.getLocation();
        LightUpCell up = getCell(loc.x, loc.y + 1);
        if (up != null) {
            adjCells.add(up);
        }
        LightUpCell down = getCell(loc.x, loc.y - 1);
        if (down != null) {
            adjCells.add(down);
        }
        LightUpCell right = getCell(loc.x + 1, loc.y);
        if (right != null) {
            adjCells.add(right);
        }
        LightUpCell left = getCell(loc.x - 1, loc.y);
        if (left != null) {
            adjCells.add(left);
        }
        return adjCells;
    }

    /**
     * Gets the number of adjacent cells of the specified type
     *
     * @param cell base cell
     * @param type specified type
     * @return the number of adjacent cells
     */
    public int getNumAdj(@NotNull LightUpCell cell, @NotNull LightUpCellType type) {
        int num = 0;
        Set<LightUpCell> adjCells = getAdj(cell);
        for (LightUpCell c : adjCells) {
            if (c.getType() == type) {
                num++;
            }
        }
        return num;
    }

    /**
     * Gets the number of adjacent cells
     *
     * @param cell LightUpCell
     * @return number of adjacent cells
     */
    public int getNumAdjLite(@NotNull LightUpCell cell) {
        int num = 0;
        Set<LightUpCell> adjCells = getAdj(cell);
        for (LightUpCell c : adjCells) {
            if (c.isLit()) {
                num++;
            }
        }
        return num;
    }

    /**
     * Gets the number of adjacent cells that are placeable
     *
     * @param cell specified cell
     * @return number of adjacent cells that are placeable
     */
    public int getNumPlaceable(@NotNull LightUpCell cell) {
        int num = 0;
        Set<LightUpCell> adjCells = getAdj(cell);
        for (LightUpCell c : adjCells) {
            if (c.getType() == LightUpCellType.UNKNOWN && !c.isLit()) {
                num++;
            }
        }
        return num;
    }

    /**
     * Gets the LightUpCell at a specific location
     *
     * @param x x location of the cell
     * @param y y location of the cell
     * @return cell at location x, y
     */
    @Override
    public LightUpCell getCell(int x, int y) {
        return (LightUpCell) super.getCell(x, y);
    }

    /**
     * Notifies the board of a change to a puzzle element and updates lighting
     *
     * @param puzzleElement the puzzle element that has changed
     */
    @Override
    public void notifyChange(@NotNull PuzzleElement puzzleElement) {
        super.notifyChange(puzzleElement);
        recalculateBulbEffects();
    }

    /**
     * Performs a deep copy of the {@code LightUpBoard}.
     *
     * @return a new copy of the {@code LightUpBoard} that is independent of this one
     */
    @Override
    public @NotNull LightUpBoard copy() {
        LightUpBoard copy = new LightUpBoard(dimension.width, dimension.height);
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                copy.setCell(x, y, getCell(x, y).copy());
            }
        }
        for (PuzzleElement e : modifiedData) { copy.getPuzzleElement(e).setModifiable(false); }
        return copy;
    }
}
