package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * NextPieceListener interface listens for the nextPieces and is used to update the PieceBoard
 */
public interface NextPieceListener {

  /**
   * Listen for the next Game Pieces
   * @param piece the Current GamePiece
   * @param following the next GamePiece
   */
  void nextPiece(GamePiece piece, GamePiece following);
}
