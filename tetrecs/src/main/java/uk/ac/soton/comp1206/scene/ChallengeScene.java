package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener{

    /**
     * Logger is used to log events
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * Model used to model a single player Game
     */
    protected Game game;
    /**
     * UI that is used for the main board.
     */
    private GameBoard board;
    /**
     * UI for the board that shows the current Game piece
     */
    private PieceBoard pieceBoard;
    /**
     * UI for the board that shows the next Game piece
     */
    private PieceBoard nextBoard;
    /**
     * UI for currentBlock
     */
    private GameBlock currentBlock;
    /**
     * file contains the scores to be read
     */
    private final File file = new File("src/main/resources/scores.txt");


    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build(){
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);
//       Place the main board the center of the BorderPane create a left sideBar which displays
//       the highscore
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);
//      place pieceBoard and nextBoard at the right SideBar
        pieceBoard = new PieceBoard(game.getMiniGrid(),gameWindow.getWidth()/5,
            gameWindow.getWidth()/5);
        nextBoard = new PieceBoard(game.getNanoGrid(),gameWindow.getWidth()/7,
            gameWindow.getHeight()/6);
        mainPane.setRight(createSideBar());
        try {
            mainPane.setLeft(createLeftBar());
        } catch (Exception e){
            e.printStackTrace();
        }

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        //Handle board being clicked
        board.setOnClicked(this::clicked);
//      Handle pieceBoard being clicked
        pieceBoard.setOnClicked(this::clicked);
//        Handle nextBoard being clicked - call the swap method
        nextBoard.setOnClicked(this::swap);
//        Listen for lines being cleared and for the nextPiece
        game.setNextPieceListener(this);
        game.setLineClearedListener(this::lineCleared);

        currentBlock = board.getBlock(0,0);
//        When gameLooped is called, restart timer, and start the timer animation if player has
//        any lives left.
        game.setOnGameLoop(timer1 -> {
            if (game.getLives()>=0) {
                gameLooped(timer1);
                var animationTimer = new AnimationTimer() {
                    double width = gameWindow.getWidth();
                    int red = 0;
                    int green = 255;
                    double interval = width / (game.getTimerDelay() / 16.5);
                    double change = 255 / (game.getTimerDelay() / 50);

                    @Override
                    public void handle(long now) {
                        var rectangle = new Rectangle(width, gameWindow.getHeight() / 15,
                            Color.rgb(red, green, 0));
                        mainPane.setBottom(rectangle);
                        width -= interval;
//                        Change color values gradually so the color of the rectangle gradually
//                        changes
                        if ((red + change) < 255) {
                            red += change;
                        } else {
                            if ((green - change) > 0) {
                                green -= change;
                            }
                        }
                    }
                };
                animationTimer.start();
            } else{
                timer1.purge();
                timer1.cancel();
                gameWindow.startScores(game);
            }
        });
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clicked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Handle when a board is clicked
     * @param board the board that was clicked
     */
    private void clicked(GameBoard board){game.clicked(board);}

    /** Swap the pieces when a board is clicked
     * @param board the board that was clicked.
     */
    private void swap(GameBoard board){game.swapCurrentPiece();}

    /** Restart the timer when method is called
     * @param timer timer to restart
     */
    public void gameLooped(Timer timer){
        game.gameLooped(timer);
    }


    /** Update the boards showing the current and following Game Piece
     * @param piece piece to be placed on the board showing the current GamePiece
     * @param following piece to be placed on the board showing the next GamePiece
     */
    public void nextPiece(GamePiece piece, GamePiece following){
        pieceBoard.setPiece(piece);
        nextBoard.setPiece(following);
    }

    /**
     * Clear array of GameBlock coordinates
     * @param blockCoordinates array of coordinates of GameBlocks to clear
     */
    public void lineCleared(GameBlockCoordinate[] blockCoordinates){
        board.fadeOut(blockCoordinates);
    }

    /**
     * Outline a GameBlock on a board
     * @param x col
     * @param y row
     */
    public void updateSelected(int x, int y){
        currentBlock = board.getBlock(x,y);
        currentBlock.outline();
    }

    /**
     * Remove outline on GameBlock
     * @param x row
     * @param y col
     */
    public void updatePrevious(int x, int y){
        currentBlock = board.getBlock(x,y);
        currentBlock.clearOutline();
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        getScene().setOnKeyReleased(event -> {
            logger.info(event.getCode() + " KEY PRESSED");
//            Listen for a particular key event and rotate, outline, swap current GamePiece and
//            following GamePiece or go back to menu. You can listen for one event or the other.
            if (event.getCode() == KeyCode.ESCAPE) {
                shutDown();
            } else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
                game.swapCurrentPiece();
            } else if (event.getCode() ==
                KeyCode.Z || event.getCode() == KeyCode.Q || event.getCode() == KeyCode.OPEN_BRACKET){
                game.rotateLeft();
            }else if (event.getCode() ==
                KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET){
                game.rotateRight();
//                Move around the GameBoard with keyboard navigations.
            } else if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W){
                int x = currentBlock.getX();
                int y = currentBlock.getY();
                if(currentBlock.getY()>0) {
                    updatePrevious(x,y);
                    updateSelected(x,y-1);
                }
            } else if(event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S){
                int x = currentBlock.getX();
                int y = currentBlock.getY();
                if(currentBlock.getY()< board.getRows()-1) {
                    updatePrevious(x,y);
                    updateSelected(x,y+1);
                }
            } else if(event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A){
                int x = currentBlock.getX();
                int y = currentBlock.getY();
                if(currentBlock.getX()> 0){
                    updatePrevious(x,y);
                    updateSelected(x-1,y);
                }
            } else if(event.getCode() == KeyCode.RIGHT|| event.getCode() == KeyCode.D){
                int x = currentBlock.getX();
                int y = currentBlock.getY();
                if(currentBlock.getX()< board.getCols()-1) {
                    updatePrevious(x,y);
                    updateSelected(x+1,y);
                }
            } else if(event.getCode() == KeyCode.ENTER|| event.getCode() == KeyCode.X){
                blockClicked(currentBlock);
            }
        });

    }

    /**
     * Create Side Bar and add it to right of the mainPane
     * @return sideBar - the vertical sideBar.
     */
    public VBox createSideBar() {
        var sideBar = new VBox();
        sideBar.setPadding(new Insets(4,4,4,4));
        sideBar.setAlignment(Pos.TOP_CENTER);

        var currentScoreLabel = new Text("Score: ");
        currentScoreLabel.getStyleClass().add("heading");
        var scoreText = new Text();
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.textProperty().bind(game.scoreProperty().asString());
        scoreText.getStyleClass().add("score");
        sideBar.getChildren().addAll(currentScoreLabel,scoreText);

        var currentLevelLabel = new Text("Level: ");
        currentLevelLabel.getStyleClass().add("heading");
        var levelText = new Text();
        levelText.setTextAlignment(TextAlignment.CENTER);
        levelText.textProperty().bind(game.levelProperty().asString());
        levelText.getStyleClass().add("level");
        sideBar.getChildren().addAll(currentLevelLabel,levelText);

        var currentLivesLabel = new Text("Lives: ");
        currentLivesLabel.getStyleClass().add("heading");
        var livesText = new Text();
        livesText.setTextAlignment(TextAlignment.CENTER);
        livesText.textProperty().bind(game.livesProperty().asString());
        livesText.getStyleClass().add("lives");
        sideBar.getChildren().addAll(currentLivesLabel,livesText);

        var currentMultiplierLabel = new Text("Multiplier: ");
        currentMultiplierLabel.getStyleClass().add("heading");
        var multiplierText = new Text();
        multiplierText.setTextAlignment(TextAlignment.CENTER);
        multiplierText.textProperty().bind(game.multiplierProperty().asString());
        multiplierText.getStyleClass().add("level");
        sideBar.getChildren().addAll(currentMultiplierLabel,multiplierText);

        pieceBoard.setPiece(game.getCurrentPiece());
        nextBoard.setPiece(game.getFollowingPiece());
        sideBar.getChildren().addAll(pieceBoard);
        sideBar.getChildren().addAll(nextBoard);
        sideBar.setSpacing(5);

        return sideBar;
    }

    /**
     * Create a left sideBar
     * @return left Sidebar
     * @throws IOException if file cannot be read
     */
    public VBox createLeftBar() throws IOException {
        var leftBar = new VBox();
        leftBar.setPadding(new Insets(4,4,4,4));
        leftBar.setAlignment(Pos.TOP_CENTER);
//        Read the Highscore from a txt file
        var highScoreLabel = new Text("High Score:");
        highScoreLabel.getStyleClass().add("heading");
        BufferedReader br = new BufferedReader(new FileReader(file));
        var line = br.readLine();
        String[] nameInt = line.split(":");
        SimpleIntegerProperty highscore = new SimpleIntegerProperty(Integer.parseInt(nameInt[1]));

        var highScoreText = new Text();
        highScoreText.textProperty();
        highScoreText.setTextAlignment(TextAlignment.CENTER);
        highScoreText.getStyleClass().add("hiscore");
        highScoreText.textProperty().bind(highscore.asString());
        game.scoreProperty().addListener((observable, oldValue, newValue) ->{
            if (newValue.intValue() > highscore.getValue()){
                highscore.set(newValue.intValue());
            }
        });

        leftBar.getChildren().addAll(highScoreLabel,highScoreText);

        return leftBar;
    }

    /**
     * Shut Down the Game and return to menu.
     */
    public void shutDown(){
        logger.info("Cleaning up the game");
        this.game.getTimer().cancel();
        this.gameWindow.getCommunicator().clearListeners();
        gameWindow.startMenu();
    }

}
