package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
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
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;

/**
 * The Multiple Player challenge scene. Holds the UI for the multiple player challenge mode in the
 * game.
 */
public class MultiplayerScene extends ChallengeScene implements NextPieceListener{

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected MultiplayerGame game;
    private GameBoard board;
    private PieceBoard pieceBoard;
    private PieceBoard nextBoard;
    private GameBlock currentBlock;
    private ScrollPane scroller;
    private ScrollPane scroller1;
    private HBox horizontalPane;
    private TextField text;
    private VBox messages;
    private boolean scrollToBottom;
    private Communicator communicator;
    private ArrayList<Pair<Pair<String,Integer>,String>> list = new ArrayList<>();
    private ObservableList <Pair<Pair<String,Integer>,String>> observableList =
        FXCollections.observableArrayList(list);
    private SimpleListProperty<Pair<Pair<String,Integer>,String>> channelScores =
        new SimpleListProperty<>(observableList);
    private Leaderboard leaderboard;
    private ArrayList<Pair<String,GameBoard>> boardList = new ArrayList<>();
    private ObservableList <Pair<String,GameBoard>> observableBoardList =
        FXCollections.observableArrayList(boardList);
    private SimpleListProperty<Pair<String,GameBoard>> playerBoard =
        new SimpleListProperty<>(observableBoardList);
    private final StringProperty t = new SimpleStringProperty();

    /**
     * Create a new Multiple Player challenge scene
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        communicator = gameWindow.getCommunicator();
    }

    /**
     * Build the scene
     */
    @Override
    public void build(){

        logger.info("Building " + this.getClass().getName());
        setupGame();
        communicator.send("PIECE ");
        communicator.send("SCORES ");

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        var numberBox = new HBox();
        numberBox.setSpacing(20);
        numberBox.setAlignment(Pos.CENTER);
        numberBox.setPrefWidth(gameWindow.getWidth());

        var currentScoreBox = new HBox();
        currentScoreBox.setSpacing(2);
        var currentScoreLabel = new Text("Score: ");
        currentScoreLabel.getStyleClass().add("heading");
        var scoreText = new Text();
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.textProperty().bind(game.scoreProperty().asString());
        scoreText.getStyleClass().add("score");
        currentScoreBox.getChildren().addAll(currentScoreLabel,scoreText);

        var levelBox = new HBox();
        levelBox.setSpacing(2);
        var currentLevelLabel = new Text("Level: ");
        currentLevelLabel.getStyleClass().add("heading");
        var levelText = new Text();
        levelText.setTextAlignment(TextAlignment.CENTER);
        levelText.textProperty().bind(game.levelProperty().asString());
        levelText.getStyleClass().add("level");
        levelBox.getChildren().addAll(currentLevelLabel,levelText);

        var livesBox = new HBox();
        livesBox.setSpacing(2);
        var currentLivesLabel = new Text("Lives: ");
        currentLivesLabel.getStyleClass().add("heading");
        var livesText = new Text();
        livesText.setTextAlignment(TextAlignment.CENTER);
        livesText.textProperty().bind(game.livesProperty().asString());
        livesText.getStyleClass().add("lives");
        livesBox.getChildren().addAll(currentLivesLabel,livesText);
        //sideBar.getChildren().addAll(currentLivesLabel,livesText);

        var multiplierBox = new HBox();
        multiplierBox.setSpacing(2);
        var currentMultiplierLabel = new Text("Multiplier: ");
        currentMultiplierLabel.getStyleClass().add("heading");
        var multiplierText = new Text();
        multiplierText.setTextAlignment(TextAlignment.CENTER);
        multiplierText.textProperty().bind(game.multiplierProperty().asString());
        multiplierText.getStyleClass().add("level");
        multiplierBox.getChildren().addAll(currentMultiplierLabel,multiplierText);

        numberBox.getChildren().addAll(currentScoreBox,levelBox,livesBox,
            multiplierBox);
        mainPane.setTop(numberBox);
        mainPane.getTop().setStyle("-fx-background-color: black");

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2.5,gameWindow.getWidth()/2.5);
        var boardView = new VBox();
        boardView.setAlignment(Pos.TOP_CENTER);
        var bottomBar = new VBox();


        pieceBoard = new PieceBoard(game.getMiniGrid(),gameWindow.getWidth()/6,
            gameWindow.getWidth()/6);
        nextBoard = new PieceBoard(game.getNanoGrid(),gameWindow.getWidth()/8,
            gameWindow.getHeight()/7);
        try {
            mainPane.setLeft(createLeftBar());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainPane.setRight(createSideBar());
        t.set(null);
        communicator.send("BOARD "+t.get());

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnClicked(this::clicked);

        pieceBoard.setOnClicked(this::clicked);
        nextBoard.setOnClicked(this::swap);
        game.setNextPieceListener(this);
        game.setLineClearedListener(this::lineCleared);

        currentBlock = board.getBlock(0,0);

        communicator.addListener((message)-> {
            if (message.startsWith("MSG")) {
                Platform.runLater(() -> {
                    var text = new Text(message.replaceFirst("MSG ", ""));
                    //String[] messageText = text.split(":");
                    messages.getChildren().add(text);
                    if (scroller.getVvalue() == 0.0f || scroller.getVvalue() > 0.9f) {
                        scrollToBottom = true;
                    }
                });
            } else if (message.startsWith("SCORES ")) {
                Platform.runLater(() -> {
                    channelScores.clear();
                    var text = message.replaceFirst("SCORES ", "").split("\n");
                    for (String player : text) {
                        var playerInfo = player.split(":");
                        channelScores.add(new Pair<>(new Pair<>(playerInfo[0],
                            Integer.parseInt(playerInfo[1])),
                            playerInfo[2]));
                    }
                });
            } else if (message.startsWith("BOARD ")) {
                Platform.runLater(() -> {
                    //uniqueGrid = new Grid(5,5);
                    mainPane.getChildren().remove(mainPane.getCenter());
                    boardView.getChildren().clear();
                    bottomBar.getChildren().clear();
                    var text = message.replaceFirst("BOARD ", "").split(":");
                    var player = text[0];
                    t.set(text[1]);
                    var board1 = new GameBoard(5,5,
                        gameWindow.getWidth() / 9, gameWindow.getHeight() / 8);
                    board1.getGrid().updateGrid(text[1]);

                    var pair = new Pair<>(player, board1);
                    for (Pair<String, GameBoard> p : playerBoard) {
                        if (Objects.equals(p.getKey(), pair.getKey())) {
                            playerBoard.set(playerBoard.indexOf(p),pair);
                            break;
                        }
                    }
                    if (!playerBoard.contains(pair)) {
                        playerBoard.add(pair);
                    }
                    logger.info(playerBoard.toString());
                    scroller1 = new ScrollPane();
                    scroller1.getStyleClass().add("scroller");

                    var boardsText = new Text("BOARDS ");
                    boardsText.setTextAlignment(TextAlignment.CENTER);
                    boardsText.getStyleClass().add("heading");

                    var hBox = new HBox();
                    hBox.setSpacing(5);
                    for (Pair<String, GameBoard> p : playerBoard) {
                        var pairBox = new VBox();
                        pairBox.setSpacing(10);
                        var playerName = new Text(p.getKey());
                        playerName.getStyleClass().add("myname");
                        var b = p.getValue();
                        //logger.info(Arrays.deepToString(b.getBlocks()));
                        pairBox.getChildren().addAll(playerName, b);
                        hBox.getChildren().add(pairBox);
                    }

                    scroller1.setContent(hBox);
                    scroller1.setFitToWidth(true);
                    scroller1.setPadding(new Insets(10,10,10,10));
                    scroller1.setVbarPolicy(ScrollBarPolicy.NEVER);
                    scroller1.setHbarPolicy(ScrollBarPolicy.ALWAYS);

                    bottomBar.getChildren().addAll(boardsText, scroller1);
                    boardView.getChildren().addAll(board,bottomBar);
                    mainPane.setCenter(boardView);

                    Platform.runLater(()->{
                        scene.addPostLayoutPulseListener(this::goToBottom);
                    });
                });
            }
        });
        game.setOnGameLoop(timer1 -> {
            if (game.getLives() >= 0) {
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
            } else {
                timer1.purge();
                timer1.cancel();
                gameWindow.startScores(game, leaderboard);
            }
        });
    }




    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Handle when a board is clicked.
     * @param board
     */
    private void clicked(GameBoard board){game.clicked(board);}
    private void swap(GameBoard board){game.swapCurrentPiece();}
    public void gameLooped(Timer timer){
        game.gameLooped(timer);
    }




    public void nextPiece(GamePiece piece, GamePiece following){
        pieceBoard.setPiece(piece);
        nextBoard.setPiece(following);
    }

    public void lineCleared(GameBlockCoordinate[] blockCoordinates){
        board.fadeOut(blockCoordinates);
    }

    public void updateSelected(int x, int y){
        currentBlock = board.getBlock(x,y);
        currentBlock.outline();
    }

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
        game = new MultiplayerGame(5, 5,gameWindow);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        root.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.SPACE) {
                game.swapCurrentPiece();
            }
        });
        getScene().setOnKeyReleased(event -> {
            logger.info(event.getCode() + " KEY PRESSED");
            if (event.getCode() == KeyCode.ESCAPE) {
                game.setLives(-1);
                communicator.send("LIVES " + game.getLives());
                communicator.send("DIE ");
                shutDown();
                //gameWindow.startMenu();
            } else if (event.getCode() ==
                KeyCode.Z || event.getCode() == KeyCode.Q || event.getCode() == KeyCode.OPEN_BRACKET){
                game.rotateLeft();
            }else if (event.getCode() ==
                KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET){
                game.rotateRight();
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
                if(currentBlock.getX()< board.getCols()-1)  {
                    updatePrevious(x,y);
                    updateSelected(x+1,y);
                }
            } else if(event.getCode() == KeyCode.ENTER|| event.getCode() == KeyCode.X){
                blockClicked(currentBlock);
            }
        });

    }

    public VBox createSideBar() {
        var sideBar = new VBox();
        sideBar.setPadding(new Insets(4,4,4,4));
        sideBar.setAlignment(Pos.TOP_LEFT);

        var leaderBoardText = new Text("Leaderboard");
        var currentPiece = new Text("Current Piece");
        var nextPiece = new Text("Next Piece");
        leaderBoardText.getStyleClass().add("heading");
        currentPiece.getStyleClass().add("heading");
        nextPiece.getStyleClass().add("heading");

        leaderboard = new Leaderboard();
        leaderboard.getChannelScores().bind(channelScores);

        var scroller = new ScrollPane();
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("scroller");
        scroller.setMinHeight(gameWindow.getHeight()/5);
        scroller.setContent(leaderboard);
        Platform.runLater(()->{
            scene.addPostLayoutPulseListener(this::goToBottom);
        });
        if (game.getCurrentPiece()!=null) {
            pieceBoard.setPiece(game.getCurrentPiece());
        }
        if (game.getFollowingPiece()!=null) {
            nextBoard.setPiece(game.getFollowingPiece());
        }
        sideBar.getChildren().addAll(leaderBoardText,scroller,currentPiece,pieceBoard,nextPiece,
            nextBoard);
        sideBar.setSpacing(5);

        return sideBar;
    }

    public VBox createLeftBar() throws IOException {
        var leftBar = new VBox();
        leftBar.setPadding(new Insets(4,4,4,4));
        leftBar.setAlignment(Pos.TOP_CENTER);
        leftBar.setSpacing(10);

        scroller = new ScrollPane();
        scroller.getStyleClass().add("scroller");

        //Fit the scroll pane to the width
        scroller.setFitToWidth(true);
        scroller.setPrefHeight(gameWindow.getHeight()/1.5);

        var chatText = new Text("ChatBox");
        chatText.setTextAlignment(TextAlignment.CENTER);
        chatText.getStyleClass().add("heading");
        messages = new VBox();
        messages.getStyleClass().add("messages");

        scroller.setContent(messages);

        horizontalPane = new HBox();

        text = new TextField();
        text.setPromptText("Enter message");
        HBox.setHgrow(text, Priority.ALWAYS);

        text.setOnKeyPressed((e) -> {
            if (e.getCode() != KeyCode.ENTER)
                return;
            sendCurrentMessage(text.getText());
            text.clear();
            text.requestFocus();
        });

        var sendButton = new Button("Send");
        HBox.setHgrow(sendButton, Priority.NEVER);
        sendButton.setOnAction((e) -> {
            sendCurrentMessage(text.getText());
            text.clear();
            text.requestFocus();
        });

        horizontalPane.setSpacing(5);
        horizontalPane.getChildren().add(text);
        horizontalPane.getChildren().add(sendButton);
        horizontalPane.setAlignment(Pos.BOTTOM_LEFT);

        leftBar.getChildren().addAll(chatText, scroller, horizontalPane);

        Platform.runLater(() -> {
            scene.addPostLayoutPulseListener(this::goToBottom);
        });
        return leftBar;
    }

    public void shutDown(){
        logger.info("Cleaning up the game");
        this.game.getTimer().cancel();
        this.gameWindow.getCommunicator().clearListeners();
        gameWindow.startScores(game,leaderboard);
    }

    private void sendCurrentMessage(String text) {
        if(text.isEmpty()) return;
        communicator.send("MSG" + " " + text);
    }

    private void goToBottom() {
        if(!scrollToBottom) return;
        scroller.setVvalue(1.0f);
        scrollToBottom = false;
    }
}
