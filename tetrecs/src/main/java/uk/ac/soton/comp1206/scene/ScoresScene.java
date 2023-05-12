package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;
import uk.ac.soton.comp1206.ui.ScoresList;


/**
 * The scores scene of the game. Displays score.
 */
public class ScoresScene extends BaseScene {

    /**
     * Logger logs events
     */
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    /**
     * Model of game
     */
    private final Game game;
    /**
     * ArrayList for localScores Simple list property
     */
    ArrayList<Pair<String,Integer>> list = new ArrayList<>();
    /**
     * Observable list for localScores Simple List property
     */
    ObservableList<Pair<String,Integer>> observableList = FXCollections.observableArrayList(list);
    /**
     * Observable ArrayList that'll be used to store the name and scores of local players.
     */
    SimpleListProperty<Pair<String, Integer>> localScores =
        new SimpleListProperty<>(observableList);
    /**
     * ArrayList for remoteScores Simple list property
     */
    ArrayList<Pair<String,Integer>> list1 = new ArrayList<>();
    /**
     * Observable list for remoteScores Simple List property
     */
    ObservableList<Pair<String,Integer>> observableList1 = FXCollections.observableArrayList(list1);
    /**
     * Observable ArrayList that'll be used to store the name and scores of remote players.
     */
    SimpleListProperty<Pair<String, Integer>> remoteScores =
        new SimpleListProperty<>(observableList1);
    /**
     * Logo to display
     */
    private final ImageView logo = new ImageView(new Image(getClass().getResource("/images"
        + "/TetrECS.png").toExternalForm()));
    /**
     * text file to read and write scores to
     */
    private final File file = new File("src/main/resources/scores.txt");
    /**
     * name of player
     */
    private String name;
    /**
     * instance of communicator to help send messages over a network
     */
    private final Communicator communicator;
    /**
     * Leaderboard to display
     */
    private Leaderboard leaderboard = new Leaderboard();

    /**
     * Create a new scores scene
     * @param gameWindow the Game Window this will be displayed in
     * @param game Model of game
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.game = game;
        logger.info("Creating Scores Scene");

        communicator = gameWindow.getCommunicator();
    }

    /**
     * Create a new scores scene
     * @param gameWindow the Game Window this will be displayed in
     * @param game Model of Game
     * @param board LeaderBoard
     */
    public ScoresScene(GameWindow gameWindow, Game game, Leaderboard board){
        super(gameWindow);
        this.game = game;
        leaderboard = board;
        communicator = gameWindow.getCommunicator();
    }

    /**
     * Build the scene layout
     */
    @Override
    public void build(){
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var scoresPane = new StackPane();
        scoresPane.setMaxWidth(gameWindow.getWidth());
        scoresPane.setMaxHeight(gameWindow.getHeight());
        scoresPane.getStyleClass().add("menu-background");
        root.getChildren().add(scoresPane);

        var mainPane = new BorderPane();
        scoresPane.getChildren().add(mainPane);

        logo.setPreserveRatio(true);
        logo.setFitWidth(600);
        mainPane.setTop(logo);
        mainPane.setMargin(logo, new Insets(gameWindow.getHeight()/6,gameWindow.getWidth()/6,
            gameWindow.getHeight()/6,gameWindow.getWidth()/8));
//        What is shown on scene depends on if game is SinglePlayer or Multiplayer
        if (!(game instanceof MultiplayerGame)) {
            communicator.addListener((message) -> {
                if (!message.startsWith("HISCORES ")) return;
                logger.info("Listener has started");
                Platform.runLater(() -> {
                    this.loadOnlineScores(message);
                });
            });

            communicator.send("HISCORES ");

            var scoresList = new ScoresList();
            scoresList.scoreProperty().bind(localScores);
            scoresList.remoteScoreProperty().bind(remoteScores);

            gameWindow.getScene().setOnKeyReleased(event -> {
                logger.info(event.getCode() + " KEY PRESSED");
                if (event.getCode() != KeyCode.ESCAPE)
                    return;
                gameWindow.startMenu();
            });

            mainPane.setCenter(scoresList);
        } else {
            gameWindow.getScene().setOnKeyReleased(event -> {
                logger.info(event.getCode() + " KEY PRESSED");
                if (event.getCode() != KeyCode.ESCAPE)
                    return;
                gameWindow.startMenu();
            });
            mainPane.setCenter(leaderboard);
        }
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        logger.info("Initialising menu");
//        When Escape key is pressed, go to start menu
        getScene().setOnKeyReleased(event -> {
            logger.info(event.getCode() + " KEY PRESSED");
            if (event.getCode() != KeyCode.ESCAPE) return;
            gameWindow.startMenu();
        });
        if (!(game instanceof MultiplayerGame)) {
            try {
                if (file.exists() && file.length() != 0) {
                    loadScores(file);
                } else {
                    writeDefault(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (game.getScore() > localScores.get(4).getValue()) {
                var td = new TextInputDialog("Please Enter Your Name");
                td.setTitle("Enter Your Name");
                td.setHeaderText("Congratulations! You've entered the high score board!!");
                td.setContentText("Please enter your name:");
                td.showAndWait();
                name = td.getEditor().getText();

                localScores.add(new Pair<>(name, game.getScore()));
                Comparator<Pair<String, Integer>> comparator = Comparator.comparing(Pair::getValue);
                localScores.sort(comparator.reversed());
                if (localScores.size() > 5) {
                    localScores.remove(5, localScores.size());
                }
                try {
                    writeScores(file);
                } catch (Exception e) {
                    logger.info("Error writing to file");
                }
            }
            if (remoteScores.size() >= 5) {
                if (game.getScore() > remoteScores.get(4).getValue()) {
                    if (name == null) {
                        var td = new TextInputDialog("Please Enter Your Name");
                        td.setTitle("Enter Your Name");
                        td.setHeaderText("We don't have your name yet?");
                        td.setContentText("Please enter your name:");
                        td.showAndWait();
                        name = td.getEditor().getText();
                    }
                    writeOnlineScore(name, game.getScore());
                }
            }

            logger.info(remoteScores);
            logger.info(observableList1);
        }
    }

    /**
     * Load scores from a file
     * @param f file to load scores from
     * @throws Exception If file cannot be read
     */
    public void loadScores(File f) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(f)) ;
        for (String line; (line = br.readLine()) != null; ) {
            String[] nameInt = line.split(":");
            //listProperty.add(new Pair<>(nameInt[0], Integer.parseInt(nameInt[1])));
            localScores.add(new Pair<>(nameInt[0], Integer.parseInt(nameInt[1])));
        }
        br.close();
    }

    /**
     * Write Scores from file
     * @param f file to write scores from
     * @throws Exception If file cannot be written to
     */
    public void writeScores(File f) throws Exception{
        //FileWriter out = new FileWriter(f);
        PrintStream out = new PrintStream(f);
        out.flush();
        for (Pair<String, Integer> stringIntegerPair : localScores) {
            out.println(
                stringIntegerPair.getKey() + ":" + stringIntegerPair.getValue().toString()
            );
        }
        out.close();
    }

    /**
     * Write default scores to a file if file is empty or doesn't exist
     * @param f file to write to
     * @throws Exception If file cannot be written to
     */
    public void writeDefault(File f) throws Exception{
        PrintStream out = new PrintStream(f);
        out.println("Orasiki:10000");
        out.println("Oli:9000");
        out.println("Adam:8790");
        out.println("Twice:1001");
        out.println("Beyonce:1000");
        out.close();
    }

    /**
     * Load Online scores
     * @param message contains all the online scores
     */
    public synchronized void loadOnlineScores(String message) {
        logger.info("Received scores: {}",message);
        remoteScores.clear();
        var scores = message.split("[\n]+");
        int count = 0;
        for(String score : scores) {
            count++;
            if (count>5){
                break;
            }
            var parts = score.split("[:]+");
            if (parts.length < 2) continue;
            var name = parts[0];
            if(name.contains("HISCORES ")){
                name = name.replace("HISCORES ","");
            }
            var points = parts[1];
            try {
                if(points != null)
                    remoteScores.add(new Pair<>(name, Integer.parseInt(points)));
            } catch (NumberFormatException e) {
                remoteScores.add(new Pair<>(name, Integer.parseInt("0")));
            }
            Comparator<Pair<String,Integer>> comparator = Comparator.comparing(Pair::getValue);
            remoteScores.sort(comparator.reversed());
            logger.info("Received score: {}={}", name, points);
        }
        logger.info(remoteScores.toString());
    }

    /**
     * Write score to online Server
     * @param name name of Player
     * @param score player's score
     */
    public void writeOnlineScore(String name, int score){
        logger.info("Sending score: {} = {}",name, score);
        communicator.send("HISCORE " + name + ":" + score);
        communicator.send("HISCORES ");
    }
}
