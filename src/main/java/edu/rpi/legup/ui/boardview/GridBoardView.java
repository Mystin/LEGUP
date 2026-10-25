package edu.rpi.legup.ui.boardview;

import edu.rpi.legup.controller.ElementController;
import edu.rpi.legup.model.gameboard.GridBoard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * A view class for a grid-based board that displays elements in a grid layout. This class extends
 * BoardView and is responsible for managing and rendering grid-based elements.
 */
public abstract class GridBoardView extends BoardView {
    protected static final Logger LOGGER = LogManager.getLogger(GridBoardView.class.getName());

    protected Dimension gridSize;
    protected Dimension elementSize;

    /**
     * {@code GridBoardView} constructor creates a {@code GridBoardView} object utilizing a controller to
     * handle the ui events
     *
     * @param controller controller that handles the ui events
     * @param gridSize dimension of the grid
     */
    public GridBoardView(ElementController controller, Dimension gridSize) {
        this(controller);
        this.gridSize = gridSize;
        this.elementSize = new Dimension(30, 30);
    }

    /**
     * {@code GridBoardView} constructor creates a {@code GridBoardView} object utilizing a controller to
     * handle the ui events.
     *
     * @param controller controller that handles the ui events
     */
    private GridBoardView(@Nullable ElementController controller) { super(controller); }

    /**
     * Gets the {@code GridElementView} from the index or {@code null} if out of bounds.
     *
     * @param index index of the {@code ElementView}
     * @return {@code GridElementView} at the specified index
     */
    public GridElementView getElement(int index) {
        return (0 <= index && index < elementViews.size()) ? (GridElementView) elementViews.get(index) : null;
    }

    /**
     * Retrieves the {@code GridElementView} at the specified grid coordinates {@code (x,y)}. Returns
     * {@code null} if the coordinates are out of bounds.
     *
     * @param x the x-coordinate (column) of the element view to retrieve
     * @param y the y-coordinate (row) of the element view to retrieve
     * @return the {@code GridElementView} at the specified coordinates, or {@code null} if out of bounds
     */
    public GridElementView getElement(int x, int y) {
        if (0 <= x && x < gridSize.width && 0 <= y && y < gridSize.height) {
            return (GridElementView) elementViews.get(y * gridSize.width + x);
        }
        return null;
    }

    /**
     * Determines the proper dimension of the grid view based on grid size and element size.
     *
     * @return the dimension of the grid view
     */
    protected @NotNull Dimension getProperSize() {
        return new Dimension(gridSize.width * elementSize.width, gridSize.height * elementSize.height);
    }

    /**
     * Retrieves the selection popup menu for data selection, containing options for all the eligible options
     * to set an element's data to.
     *
     * @return a {@code DataSelectionView} to present data selection options
     */
    public @NotNull DataSelectionView getSelectionPopupMenu() {
        return new DataSelectionView((ElementController) getController());
    }

    /**
     * Gets the size of each element in the grid.
     *
     * @return the dimension of each element in the grid
     */
    public @NotNull Dimension getElementSize() { return elementSize; }

    @Override
    public int boardIndexAt(@NotNull Point2D p) {
        Point loc = boardLocationAt(p);
        return loc.y * ((GridBoard) board).getWidth() + loc.x;
    }

    /**
     * Gets the location of the puzzle element being displayed at the specified point.
     *
     * @param p {@code Point2D} in view coordinates
     * @return location of the puzzle element on the board at the specified point or {@code null} if there is none
     */
    public Point boardLocationAt(@NotNull Point2D p) {
        return new Point(
                (int) Math.round(p.getX() / elementSize.width),
                (int) Math.round(p.getY() / elementSize.height)
        );
    }
}
