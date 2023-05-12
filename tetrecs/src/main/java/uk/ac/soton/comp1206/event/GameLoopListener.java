package uk.ac.soton.comp1206.event;

import java.util.Timer;
import javafx.animation.AnimationTimer;

/**
 * The Game Loop listener is used to handle the event when the game is looped. It passes the
 * timer that will restart.
 */
public interface GameLoopListener {
  /**
   * Handle a game looped event
   * @param timer timer that is restarted
   */
  public void gameLooped(Timer timer);
}
