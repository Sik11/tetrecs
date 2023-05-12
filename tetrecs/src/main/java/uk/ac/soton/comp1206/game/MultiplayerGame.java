package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class MultiplayerGame extends Game{

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private ArrayList<Integer> filledRows = new ArrayList<>();
    private ArrayList<Integer> filledCols = new ArrayList<>();
    /**
     * Number of rows
     */
    //protected final int rows;
    private SimpleIntegerProperty score;
    private SimpleIntegerProperty level;
    private SimpleIntegerProperty lives;
    private SimpleIntegerProperty multiplier;
//    private SimpleIntegerProperty timerDelay;
    private int clearedBlocks;
    private int clearedLines;
    private int pointLimit = 1000;
    private GameBlock[][] blocks;
    private Timer timer;
    private ScheduledExecutorService scheduler;
    private TimerTask timerTask;
    private Timer timer1;
    private TimerTask timerTask1;
    LinkedList<Integer> linkedList = new LinkedList<>();
    private BlockingQueue<Integer> pieces = new LinkedBlockingQueue<>();

    /**
     * Number of columns
     */
//    protected final int cols;
//
//    /**
//     * The grid model linked to the game
//     */
//    protected final Grid grid;
//    protected final Grid miniGrid;
//    protected final Grid nanoGrid;
    protected GamePiece currentPiece;
    protected GamePiece followingPiece;
    private NextPieceListener nextPieceListener;
    private LineClearedListener lineClearedListener;
    private boolean piecePlayed;
    private GameLoopListener gameLoopListener;
    private Communicator communicator;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, GameWindow gameWindow) {
        super(cols,rows);
//        this.cols = cols;
//        this.rows = rows;
//
//        //Create a new grid model to represent the game state
//        this.grid = new Grid(cols,rows);
//        this.miniGrid = new Grid(3,3);
//        this.nanoGrid = new Grid (3,3);
        this.score = new SimpleIntegerProperty(0);
        this.level = new SimpleIntegerProperty(0);
        this.lives = new SimpleIntegerProperty(3);
        this.multiplier = new SimpleIntegerProperty(1);
        this.communicator = gameWindow.getCommunicator();
        communicator.addListener((message) -> {
            if (message.startsWith("PIECE")) {
                Platform.runLater(() -> {
                    var text = message.replaceFirst("PIECE ", "");
                    try {
                        pieces.put(Integer.parseInt(text));
                        logger.info("{} was put",text);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info("PIECES QUEUE: {}" + pieces.toString());
                    if (pieces.size()<2){
                        communicator.send("PIECE ");
                    } else {
                        logger.info(pieces.toString());
                        try {
                            this.currentPiece = spawnPiece(pieces.take());
                            logger.info("currentpiece is set");
                            var pieceArr = pieces.toArray();
                            this.followingPiece = spawnPiece((int) pieceArr[0]);
                            logger.info("followPiece is set");
                            nextPieceListener.nextPiece(currentPiece,followingPiece);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (message.startsWith("SCORE ")) {
                Platform.runLater(() -> {
                    var text = message.replaceFirst("SCORE ", "").split(":");
                    communicator.send("SCORES ");
                });
            } else if (message.startsWith("LIVES ")) {
                Platform.runLater(() -> {
                    var text = message.replaceFirst("LIVES ","");
                    communicator.send("SCORES ");
                    if (text.equals("-1")){
                        communicator.send("DIE ");
                    }
                });
            }
        });
        //communicator.send("BOARD " + "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0");
        communicator.send("SCORE " + getScore());
        //this.followingPiece = spawnPiece(Integer.parseInt(pieces.take()));
        //communicator.send("PIECE ");
        //requestPieces();
        //Platform.runLater(this::requestPieces);
//        while (currentPiece!=null || followingPiece!=null){
//            requestPiece();
//        }
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        //updateTimer();
        //if (pieces.size()>=2) {
        gameLoopListener.gameLooped(timer);
    }

    public void gameLoop(){
        logger.info("GameLoop started");
        setLives(getLives()-1);
        communicator.send("LIVES " + getLives());
        logger.info("lives reduced");
        nextPiece();
        logger.info("current piece changed");
        setMultiplier(1);
        logger.info("multiplier set to 1");
    }

    public void updateTimer(){
        logger.info("Timer has been updated");
        logger.info(piecePlayed);
        if (timer != null && timerTask != null) {
            timer.cancel();
            timerTask.cancel();
        }
        timerTask = new TimerTask() {

            public void run() {
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
//        scheduler = Executors.newScheduledThreadPool(1);
        timer.schedule(timerTask, getTimerDelay());
//        ScheduledFuture<?> timerHandle =
//        scheduler.schedule(timerTask,getTimerDelay(), TimeUnit.MILLISECONDS);

    }

    public void requestPieces(){
        logger.info("Pieces have been updated");
        //logger.info(piecePlayed);
        if (timer1 != null && timerTask1 != null) {
            timer1.cancel();
            timerTask1.cancel();
        }
        timerTask1 = new TimerTask() {

            public void run() {
                Platform.runLater(()->{
                        communicator.send("PIECE ");
                });
            }
        };

        timer1 = new Timer();
//        scheduler = Executors.newScheduledThreadPool(1);
        timer1.schedule(timerTask1, 0);
//        ScheduledFuture<?> timerHandle =
//        scheduler.schedule(timerTask,getTimerDelay(), TimeUnit.MILLISECONDS);

    }

    public void setLives(int lives) {
        this.lives.set(lives);
    }

    public void setMultiplier(int multiplier) {
        this.multiplier.set(multiplier);
    }

//    public void calcTimerDelay() {
//        if(g)
//        this.timerDelay.set(timerDelay);
//    }

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

        //Get the new value for this block
        //int previousValue = grid.get(x,y);
        //int newValue = previousValue + 1;
        //if (newValue  > GamePiece.PIECES) {
            //newValue = 0;
        //}

        if (grid.canPlayPiece(currentPiece,x,y)){
            //afterPiece(x,y);
            grid.playPiece(currentPiece,x,y);
            var board = "";
            piecePlayed = true;
            logger.info("PIECE PLAYED IS NOW TRUE");
            //updateTimer();
            Multimedia.playAudio("pling.wav");
            logger.info("Piece was placed with value " + currentPiece.getValue());;
            afterPiece();
            checkLvl();
            for(int row = 0; row < grid.getRows(); row++) {
                for (int col = 0; col < grid.getCols(); col++) {
                    board = board.concat(grid.getGridProperty(row, col).getValue().toString() + " ");
                }
            }
            communicator.send("BOARD " + board);
            gameLoopListener.gameLooped(timer);
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

    public int getLevel() {
        return level.get();
    }

    public int getLives() {
        return lives.get();
    }

    public int getScore() {
        return score.get();
    }

//    public int getTimerDelay() {
//        return timerDelay.get();
//    }
//
//    public SimpleIntegerProperty timerDelayProperty() {
//        return timerDelay;
//    }

    public SimpleIntegerProperty levelProperty() {
        return level;
    }

    public int getTimerDelay(){
        logger.info(Math.max(12000 - (500 * getLevel()), 2500));
        return Math.max(12000 - (500 * getLevel()), 2500);
    }

    public SimpleIntegerProperty livesProperty() {
        return lives;
    }

    public SimpleIntegerProperty multiplierProperty() {
        return multiplier;
    }

    public SimpleIntegerProperty scoreProperty() {
        return score;
    }

    public GamePiece spawnPiece(int i){
        return GamePiece.createPiece(i);
    }
    public void requestPiece(){
        Platform.runLater(()->{
            communicator.send("PIECE ");
        });
    }

    public void nextPiece(){
        communicator.send("PIECE");
        logger.info("Next Piece has a value of: " + currentPiece.getValue());
        piecePlayed = false;
        logger.info("PIECE PLAYED IS NOW FALSE");
    }

    public void swapCurrentPiece(){
        var tempCurrent = currentPiece;
        currentPiece = followingPiece;
        followingPiece = tempCurrent;
        nextPieceListener.nextPiece(currentPiece,followingPiece);
        logger.info("Current and Following piece have been swapped");
        Multimedia.playAudio("transition.wav");
    }

    public void afterPiece(){
        ArrayList<Integer> rows = new ArrayList<>();
        ArrayList<Integer> cols = new ArrayList<>();
//        for (int i = 0; i< grid.getCols();i++){
//            for (int j = 0; j < grid.getRows(); j++) {
//                if (checkCol(i) && checkRow(j)) {
//                    clearCol(i);
//                    clearRow(j);
//                    logger.info("Row " + j + " and column " + i + " was cleared");
//                } else if (checkCol(i)) {
//                    clearCol(i);
//                    logger.info("Column " + i + " was cleared");
//                } else if (checkRow(j)) {
//                    clearRow(j);
//                    logger.info("Row " + j + " was cleared");
//                }
//            }
//        }
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
        nextPiece();
    }

    public boolean checkCol(int n){
        //logger.info("Checking column " + n);
        boolean bool = true;
        int i = 0;
        while(i< grid.getRows() && bool){
            //logger.info("Value at " + n +" column and row " + i + " is " + grid.get(n,i));
            bool = grid.get(n,i) > 0;
            //logger.info("full? " + bool);
            i++;
        }
        return bool;
    }

    public boolean checkRow(int n){
        //logger.info("Checking row " + n);
        boolean bool = true;
        int i = 0;
        while (i<grid.getCols()&&bool){
            //logger.info("Value at " + n +" row and column " + i + " is " + grid.get(i,n));
            bool = grid.get(i, n) > 0;
            //logger.info("full? " + bool);
            i++;
        }
        return bool;
    }

    public void clearRow(int n){
        var coordinates = new GameBlockCoordinate[getCols()];
        for (int i = 0; i< getCols(); i++){
            clearedBlocks++;
            coordinates[i] = new GameBlockCoordinate(i,n);
        }
        lineClearedListener.lineCleared(coordinates);
        for (GameBlockCoordinate coordinate:coordinates){
            grid.set(coordinate.getX(),coordinate.getY(),0);
        }
        Multimedia.playAudio("explode.wav");
        clearedLines++;
    }


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


    public void score(int blocks, int lines){
        if (lines > 0){
            var newScore = score.get()+lines*blocks*10*this.multiplier.get();
            score.set(newScore);
            communicator.send("SCORE " +newScore);
            //logger.info("Score is: " + score.get());
            //logger.info("Cleared blocks is "+clearedBlocks);
            //logger.info("Cleared blocks is "+clearedBlocks);
        }
    }

    public void checkLvl(){
        if (score.get()>=pointLimit){
            level.set(level.get()+1);
            pointLimit += 1000;
        }
    }

    public Grid getMiniGrid() {
        return miniGrid;
    }

    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    public void rotateCurrentPiece(GamePiece piece){
        piece.rotate();
        Multimedia.playAudio("rotate.wav");
    }

    public void rotateLeft(){
        currentPiece.rotate(1);
        nextPieceListener.nextPiece(currentPiece,followingPiece);
        Multimedia.playAudio("rotate.wav");
    }
    public void rotateRight(){
        currentPiece.rotate(3);
        nextPieceListener.nextPiece(currentPiece,followingPiece);
        Multimedia.playAudio("rotate.wav");
    }

    public Grid getNanoGrid() {
        return nanoGrid;
    }

    public GamePiece getFollowingPiece() {
        return followingPiece;
    }


    public void clicked(GameBoard board){
        rotateCurrentPiece(currentPiece);
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    public void gameLooped(Timer timer){
        updateTimer();
    }

    public Timer getTimer() {
        return timer;
    }
}
