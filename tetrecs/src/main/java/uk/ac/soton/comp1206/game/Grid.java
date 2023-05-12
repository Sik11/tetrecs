package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * Logger used to log events.
     */
    private static final Logger logger = LogManager.getLogger(Grid.class);
    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Check if the piece can be placed in the grid
     * @param piece piece to be played
     * @param x column
     * @param y row
     * @return true or false depending on if piece can be played
     */
    public boolean canPlayPiece(GamePiece piece, int x, int y){
        boolean bool = true;
        int [][] blocks = piece.getBlocks();
        int nx = x -1;
        int ny = y -1;
//        Check if cells in the grid are empty if a piece's block is to be placed there
        if (get(x,y)==0){
            for (int x1 = 0; x1<3;x1++){
                for (int y1 = 0; y1<3;y1++){
                    if ((get(nx+x1,ny+y1)!=0) && blocks[x1][y1]>0) {
                        bool = false;
                        break;
                    }
                }
            }
        } else {
//            No need to run the code, if cell that was clicked is not empty
            bool = false;
        }
        logger.info("Can play? {}",bool);
       return bool;
    }

    /**
     * Place piece in the grid
     * @param piece piece to be placed
     * @param x column
     * @param y row
     */
    public void playPiece(GamePiece piece, int x, int y) {
        int[][] blocks = piece.getBlocks();
        int nx = x - 1;
        int ny = y - 1;
        if (get(x,y)==0) {
//            Update cells in grid to contain piece blocks.
            for (int x1 = 0; x1 < 3; x1++) {
                for (int y1 = 0; y1 < 3; y1++) {
                    if ((get(nx + x1, ny + y1) == 0) && blocks[x1][y1] > 0) {
                        this.set(nx + x1, ny + y1, blocks[x1][y1]);
                    }
                }
            }
        }
    }

    /**
     * Update the Grid using a string of values
     * @param s string of values
     */
    public void updateGrid(String s){
        int x =0;
        int y = 0;
        var values = s.split(" ");
        if (s.equals("null")){
//            If values passed are 0's or null, then set the grid to be empty
            for (int  i =x ;i < 5;i++){
                for (int j=y;j<5;j++){
                    this.set(i,j,0);
                }
            }
        } else {
            for (String value : values) {
//                for each number in the long string passed, assign it to a position in the grid
                if (x < 5) {
                    if (y < 5) {
//                        Handle number wrong format exception
                        if (value.equals("0")) {
                            this.set(x, y, 0);
                            logger.info("grid{}{}) was set to 0", x, y);
                        } else {
                            this.set(x, y, Integer.parseInt(value));
                            logger.info("grid{}{} was set to {}", x, y, value);
                        }
                    } else {
                        x++;
                        y = 0;
                        if (value.equals("0")) {
                            this.set(x, y, 0);
                            logger.info("grid{}{}) was set to 0", x, y);
                        } else {
                            this.set(x, y, Integer.parseInt(value));
                            logger.info("grid{}{}) was set to {}", x, y, value);
                        }
                    }
                    y++;
                }
            }
        }
    }
}
