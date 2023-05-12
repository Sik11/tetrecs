package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.event.ClickedListener;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * UI elements that displays upcoming piece
 */
public class PieceBoard extends GameBoard {

  /**
   * ClickedListener that listens for the board being clicked
   */
  private ClickedListener clickedListener;


  /**
   * Create a PieceBoard
   * @param grid Grid that's linked to UI element
   * @param width width of the board
   * @param height height of the board
   */
  public PieceBoard(Grid grid, double width, double height) {
    super(grid, width, height);
  }

  /**
   * Set piece displayed on board
   * @param piece GamePiece to be displayed
   */
  public void setPiece(GamePiece piece) {
    int[][] blocks = piece.getBlocks();
    for (int col = 0; col < super.getCols(); col++) {
      for (int row = 0; row < super.getRows(); row++) {
        grid.set(col, row, blocks[col][row]);
      }
    }
    this.blocks[1][1].highlight();
  }

  /**
   * When right-clicked, call the clicked method and pass the mouseevent and board as parameters.
   */
  public void clicked(){
    this.setOnMouseClicked((e) -> {
      if (e.getButton() != MouseButton.PRIMARY) return;
      clicked(e, this);
    });
  }

  /**
   * Listen for board being clicked
   * @param event MouseEvent
   * @param board Board that was clicked
   */
  private void clicked(MouseEvent event, GameBoard board){
    if(clickedListener != null) {
      clickedListener.clicked(board);
    }
  }

  /**
   * Assign instance of clicked listener in class to another clicked listener
   * @param listener ClickedListener that'll be assigned
   */
  public void setOnClicked(ClickedListener listener){
    this.clickedListener = listener;
  }
}
