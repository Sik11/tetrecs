package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The Line Cleared listener is used to handle the event when an array of GameBlock Coordinates
 * is cleared. It passes the array of GameBlockCoordinates that was cleared.
 */
public interface LineClearedListener {

  /**
   * Handle a line cleared event
   * @param coordinates GameBlockCoordinates that were cleared
   */
  public void lineCleared(GameBlockCoordinate[] coordinates);
}
