package uk.ac.soton.comp1206.game;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game{

    /**
     * Logger used to log events.
     */
    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * User score.
     */
    private final SimpleIntegerProperty score;
    /**
     * Level of the user.
     */
    private final SimpleIntegerProperty level;
    /**
     * Lives user has.
     */
    private final SimpleIntegerProperty lives;
    /**
     * Game multiplier.
     */
    private final SimpleIntegerProperty multiplier;
    /**
     * Number of Blocks cleared.
     */
    private int clearedBlocks;
    /**
     * Number of lines cleared.
     */
    private int clearedLines;
    /**
     * Point limit used in changing User Level
     */
    private int pointLimit = 1000;
    /**
     * Game Timer
     */
    private Timer timer;
    /**
     * Task timer performs
     */
    private TimerTask timerTask;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;
    /**
     * Grid model used to display Current GamePiece
     */
    protected final Grid miniGrid;
    /**
     * Grid model used to display Next GamePiece
     */
    protected final Grid nanoGrid;
    /**
     * Current GamePiece to be played
     */
    protected GamePiece currentPiece;
    /**
     * Next GamePiece to be played.
     */
    protected GamePiece followingPiece;

    /**
     * Listener that listens for the next GamePiece
     */
    private NextPieceListener nextPieceListener;

    /**
     * Listener that listens for a line being cleared
     */
    private LineClearedListener lineClearedListener;

    /**
     * Boolean that changes depending on a piece being played.
     */
    private boolean piecePlayed;

    /**
     * Listener that listens for the game looping
     */
    private GameLoopListener gameLoopListener;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
//        Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
//        miniGrid and nanoGrid are both 3x3 Grids.
        this.miniGrid = new Grid(3,3);
        this.nanoGrid = new Grid (3,3);
//        Spawn Game Pieces and assign them to currentPiece and followingPiece when game starts.
        this.currentPiece = spawnPiece();
        this.followingPiece = spawnPiece();
//        When game starts, score and level of user should be set to 0. User should have 3 lives
//        and the value of the multiplier should be initialised to 1.
        this.score = new SimpleIntegerProperty(0);
        this.level = new SimpleIntegerProperty(0);
        this.lives = new SimpleIntegerProperty(3);
        this.multiplier = new SimpleIntegerProperty(1);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
//        Start the timer when game starts.
        gameLoopListener.gameLooped(timer);
    }

    /**
     * Loop the game.
     */
    public void gameLoop(){
//        Subtract lives, set the value of the multiplier to 1 and call the nextPiece
        logger.info("GameLoop started");
        setLives(getLives()-1);
        logger.info("lives reduced");
        nextPiece();
        logger.info("current piece changed");
        setMultiplier(1);
        logger.info("multiplier set to 1");
    }

    /**
     * Restart the timer
     */
    public void updateTimer(){
        logger.info("Timer has been updated");
        logger.info(piecePlayed);
        if (timer != null && timerTask != null) {
//            Cancel existing timers and timertasks.
            timer.cancel();
            timerTask.cancel();
        }
        timerTask = new TimerTask() {

            public void run() {
//                Listen for events while the timer is running. If a piece has been placed,
//                restart the timer. Else, minus lives and restart timer.
                Platform.runLater(()->{
                    if(piecePlayed){
                        gameLoopListener.gameLooped(timer);
                    } else if( getLives()>-1) {
                        logger.info("ABOUT TO MINUS LIVES, PIECE PLAYED IS" + piecePlayed);
                        gameLoop();
                        gameLoopListener.gameLooped(timer);
                        //updateTimer();
                    }
                });
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, getTimerDelay());
    }

    /**
     * Set game lives.
     * @param lives number of lives
     */
    public void setLives(int lives) {
        this.lives.set(lives);
    }

    /**
     * Set value of multiplier.
     * @param multiplier number the multiplier will be set to
     */
    public void setMultiplier(int multiplier) {
        this.multiplier.set(multiplier);
    }


    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
//        Place current GamePiece if possible
        if (grid.canPlayPiece(currentPiece,x,y)){
            grid.playPiece(currentPiece,x,y);
            piecePlayed = true;
            logger.info("Piece has now been placed.");
            gameLoopListener.gameLooped(timer);
//            Play music when a piece has been played.
            Multimedia.playAudio("pling.wav");
            logger.info("Piece was placed with value " + currentPiece.getValue());;
            afterPiece();
            checkLvl();
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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
     * Get the level of the User
     * @return level of User
     */
    public int getLevel() {
        return level.get();
    }

    /**
     * Get the number of lives the User has.
     * @return number of lives.
     */
    public int getLives() {
        return lives.get();
    }

    /**
     * Get the score of the User
     * @return user Score
     */
    public int getScore() {
        return score.get();
    }


    /**
     * Get the SimpleIntegerProperty levelProperty
     * @return levelProperty
     */
    public SimpleIntegerProperty levelProperty() {
        return level;
    }

    /**
     * Get the timerDelay Value
     * @return timerDelay value
     */
    public int getTimerDelay(){
//        Calculate the delay at the maximum of either 2500 milliseconds or 12000 - 500 * the current level
        logger.info("Timer Delay Value is now{}",Math.max(12000 - (500 * getLevel()), 2500));
        return Math.max(12000 - (500 * getLevel()), 2500);
    }

    /**
     * Get the SimpleIntegerProperty livesProperty
     * @return livesProperty
     */
    public SimpleIntegerProperty livesProperty() {
        return lives;
    }

    /**
     * Get the SimpleIntegerProperty multiplierProperty
     * @return multiplierProperty
     */
    public SimpleIntegerProperty multiplierProperty() {
        return multiplier;
    }

    /**
     * Get the SimpleIntegerProperty scoreProperty
     * @return scoreProperty
     */
    public SimpleIntegerProperty scoreProperty() {
        return score;
    }

    /**
     * Spawn a new GamePiece
     * @return  spawned GamePiece
     */
    public GamePiece spawnPiece(){
        Random rand = new Random();
        int no = rand.nextInt(15);
        return GamePiece.createPiece(no);
    }

    /**
     * Assign the next piece to the CurrentPiece and spawn a new piece as the followingPiece.
     * Listen for the updated values of CurrentPiece and FollowingPiece and set piecePlayed to
     * false.
     */
    public void nextPiece(){
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece,followingPiece);
        logger.info("Next Piece has a value of: " + currentPiece.getValue());
        piecePlayed = false;
        logger.info("PIECE PLAYED IS NOW FALSE");
    }

    /**
     * Swap the current GamePiece and the next GamePiece and listen for the updated values of
     * currentPiece and followingPiece. Sound is played when this is done.
     */
    public void swapCurrentPiece(){
        var tempCurrent = currentPiece;
        currentPiece = followingPiece;
        followingPiece = tempCurrent;
        nextPieceListener.nextPiece(currentPiece,followingPiece);
        logger.info("Current and Following piece have been swapped");
        Multimedia.playAudio("transition.wav");
    }

    /**
     * After piece has been placed, check the grid for any full Columns and/or Rows before
     * clearing them. Update score and multiplier accordingly.
     */
    public void afterPiece(){
        ArrayList<Integer> rows = new ArrayList<>();
        ArrayList<Integer> cols = new ArrayList<>();
        for (int i=0 ; i<grid.getCols(); i++){
            if (checkCol(i)){
                cols.add(i);
            }
            if (checkRow(i)){
                rows.add(i);
            }
        }
        for (Integer i: rows){
            clearRow(i);
        }
        for (Integer i: cols){
            clearCol(i);
        }
        score(clearedBlocks,clearedLines);
        if(clearedLines>0){
            multiplier.set(multiplier.get()+1);
        } else {
            multiplier.set(1);
        }
        clearedBlocks = 0;
        clearedLines = 0;
//        Get the next Piece
        nextPiece();
    }

    /** Check if column is full
     * @param n column to check
     * @return true or false if column is full or not
     */
    public boolean checkCol(int n){
        boolean bool = true;
        int i = 0;
        while(i< grid.getRows() && bool){
            bool = grid.get(n,i) > 0;
            i++;
        }
        return bool;
    }

    /**
     * Check if row is full
     * @param n row to check
     * @return true or false if row is full or not
     */
    public boolean checkRow(int n){
        boolean bool = true;
        int i = 0;
        while (i<grid.getCols()&&bool){
            bool = grid.get(i, n) > 0;
            i++;
        }
        return bool;
    }

    /**
     * Clear row
     * @param n row to be cleared
     */
    public void clearRow(int n){
        //add all the coordinates in row to coordinates array;
        var coordinates = new GameBlockCoordinate[getCols()];
        for (int i = 0; i< getCols(); i++){
            clearedBlocks++;
            coordinates[i] = new GameBlockCoordinate(i,n);
        }
//        Listen for the line to be cleared.
        lineClearedListener.lineCleared(coordinates);
//        Set the value of all the coordinates in coordinates to 0.
        for (GameBlockCoordinate coordinate:coordinates){
            grid.set(coordinate.getX(),coordinate.getY(),0);
        }
//        Play audio to show line has been cleared.
        Multimedia.playAudio("explode.wav");
        clearedLines++;
    }


    /**
     * Clear columns
     * @param n column to be cleared.
     */
    public void clearCol(int n){
        var coordinates = new GameBlockCoordinate[getRows()];
        for (int i = 0; i< getRows(); i++){
            clearedBlocks++;
            coordinates[i] = new GameBlockCoordinate(n,i);
        }
        lineClearedListener.lineCleared(coordinates);
        for (GameBlockCoordinate coordinate:coordinates){
            grid.set(coordinate.getX(),coordinate.getY(),0);
        }
        Multimedia.playAudio("explode.wav");
        clearedLines++;
    }


    /**
     * Set score using the lines cleared and the number of blocks cleared.
     * @param blocks number of blocks cleared
     * @param lines number of lines Cleared
     */
    public void score(int blocks, int lines){
        if (lines > 0){
            score.set(score.get()+lines*blocks*10*this.multiplier.get());
        }
    }

    /**
     * Check and update the level of the Player/User.
     */
    public void checkLvl(){
//        increment level of player if score is greater than point limit.
        if (score.get()>=pointLimit){
            level.set(level.get()+1);
            pointLimit += 1000;
        }
    }

    /**
     * Get the miniGrid
     * @return miniGrid
     */
    public Grid getMiniGrid() {
        return miniGrid;
    }

    /**
     * Get the Current GamePiece
     * @return current GamePiece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /** Sets the NextPieceListener
     * @param nextPieceListener listener that'll be assigned to instance of NextPieceListener
     */
    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    /**
     * Rotate current GamePiece
     * @param piece piece that'll be rotated
     */
    public void rotateCurrentPiece(GamePiece piece){
        piece.rotate();
        Multimedia.playAudio("rotate.wav");
    }

    /**
     * Rotate current GamePiece to the left
     */
    public void rotateLeft(){
        currentPiece.rotate(1);
        nextPieceListener.nextPiece(currentPiece,followingPiece);
        Multimedia.playAudio("rotate.wav");
    }

    /**
     * Rotate current GamePiece to the right
     */
    public void rotateRight(){
        currentPiece.rotate(3);
        nextPieceListener.nextPiece(currentPiece,followingPiece);
        Multimedia.playAudio("rotate.wav");
    }

    /** Get the nano Grid holding the next GamePiece
     * @return the nanoGrid
     */
    public Grid getNanoGrid() {
        return nanoGrid;
    }

    /** Get the Following/Next GamePiece
     * @return the following GamePiece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }


    /** Rotate Current GamePiece when board is clicked and send the updated value to the
     * nextPiece Listener along with the following GamePiece.
     * @param board GameBoard that will be clicked.
     */
    public void clicked(GameBoard board){
        rotateCurrentPiece(currentPiece);
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Set the LineClearedListener
     * @param lineClearedListener LineClearedListener that will be assigned to the Game's
     *                            instance of LineClearedListener
     */
    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * Set GameLoopListener
     * @param gameLoopListener GameLoopListener that will be assigned to the Game's
     *                             instance of GameLoopListener
     */
    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    /**
     * Restart timer when game is Looped
     * @param timer timer to be restarted
     */
    public void gameLooped(Timer timer){
        updateTimer();
    }

    /**
     * Get Timer
     * @return timer
     */
    public Timer getTimer() {
        return timer;
    }
}
