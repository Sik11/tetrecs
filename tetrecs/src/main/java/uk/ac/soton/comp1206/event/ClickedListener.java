package uk.ac.soton.comp1206.event;

import javafx.scene.input.MouseEvent;
import uk.ac.soton.comp1206.component.GameBoard;

/**
 * ClickedListener is an interface that listens for when the GameBoard is clicked
 */
public interface ClickedListener {

  /**
   * Listen for when GameBoard is clicked
   * @param board board that is clicked
   */
  public void clicked(GameBoard board);
}
