package edu.rpi.legup.puzzle.lightup;

import edu.rpi.legup.model.elements.Element;
import edu.rpi.legup.model.gameboard.GridCell;
import java.awt.*;
import java.awt.event.MouseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightUpCell extends GridCell<Integer> {
    private int lightLevel;

    public LightUpCell(int valueInt, @Nullable Point location) {
        super(valueInt, location);
        resetLightLevel();
    }

    /**
     * Sets the type of this {@code LightUpCell}.
     *
     * @param e element to set the type of this binary cell to
     * @param m mouse event to extract button pressed from
     */
    @Override
    public void setType(@NotNull Element e, @NotNull MouseEvent m) {
        setData(switch(e.getElementID()) {
            case "LTUP-ELEM-0002" -> -4;
            case "LTUP-ELEM-0001" -> -1;
            case "LTUP-ELEM-0004" -> -2;
            case "LTUP-ELEM-0003" -> switch(m.getButton()) {
                case MouseEvent.BUTTON1 -> switch(data) {
                    case 0,1,2,3 -> data + 1;
                    default -> 0;
                };
                case MouseEvent.BUTTON3 -> switch(data) {
                    case 1,2,3,4 -> data - 1;
                    default -> 4;
                };
                default -> data;
            };
            default -> data;
        });
    }

    public @Nullable LightUpCellType getType() {
        switch (data) {
            case -4:
                return LightUpCellType.BULB;
            case -3:
                return LightUpCellType.EMPTY;
            case -2:
                return LightUpCellType.UNKNOWN;
            case -1:
                return LightUpCellType.BLACK;
            default:
                if (data >= 0) {
                    return LightUpCellType.NUMBER;
                }
        }
        return null;
    }

    /**
     * Gets whether this {@code LightUpCell} is illuminated.
     *
     * @return {@code true} if the cell is lit; {@code false} otherwise
     */
    public boolean isLit() { return lightLevel > 0; }

    /** Increases the light level of this tile. To be called when a bulb is added that illuminates this tile. */
    public void addLight() { ++lightLevel; }

    /** Decreases the light level of this tile. To be called when a bulb is removed that illuminated this tile. */
    public void removeLight() { --lightLevel; }

    /** Resets the light level of this tile to its starting value. */
    public void resetLightLevel() { lightLevel = 0; }

    /**
     * Performs a deep copy on the {@code LightUpCell}.
     *
     * @return a new copy of the {@code LightUpCell} that is independent of this one
     */
    @Override
    public @NotNull LightUpCell copy() {
        LightUpCell copy = new LightUpCell(data, (location != null ? (Point) location.clone() : null));
        copy.setIndex(index);
        copy.setModifiable(isModifiable);
        copy.setGiven(isGiven);
        copy.setGoal(isGoal);
        copy.lightLevel = this.lightLevel;
        return copy;
    }

    @Override
    public boolean isKnown() {
        return data != -2;
    }

    @Override
    public String describeState(boolean isPlural) {
        return getType().toString().toLowerCase();
    }
}
