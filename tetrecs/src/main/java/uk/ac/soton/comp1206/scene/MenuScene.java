package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation.Status;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    /**
     * Logger used to log events
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * Logo to display on menu screen
     */
    private final ImageView logo = new ImageView(new Image(getClass().getResource("/images"
        + "/TetrECS.png").toExternalForm()));
    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(vBox);

        var singlePlayer = new Text("Single Player");
        singlePlayer.getStyleClass().add("menuItem");
        vBox.getChildren().add(singlePlayer);
        singlePlayer.setOnMouseClicked(this::startGame);

        var multiPlayer = new Text("Multi Player");
        multiPlayer.getStyleClass().add("menuItem");
        vBox.getChildren().add(multiPlayer);
        multiPlayer.setOnMouseClicked(this::startLobby);

        var instructions = new Text("How to Play");
        instructions.getStyleClass().add("menuItem");
        vBox.getChildren().add(instructions);
        instructions.setOnMouseClicked(this::startInstructions);

        var exit = new Text("Exit");
        exit.getStyleClass().add("menuItem");
        vBox.getChildren().add(exit);
        exit.setOnMouseClicked(this::quit);

        logo.setPreserveRatio(true);
        logo.setFitWidth(600);
        mainPane.setTop(logo);
        mainPane.setMargin(logo, new Insets(gameWindow.getHeight()/6,gameWindow.getWidth()/6,
            gameWindow.getHeight()/6,gameWindow.getWidth()/8));
        animateLogo();
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        logger.info("Initialising menu");
        getScene().setOnKeyReleased(event -> {
            logger.info(event.getCode() + " KEY PRESSED");
            if (event.getCode() != KeyCode.ESCAPE) return;
            Platform.exit();
        });
    }

    /**
     * Handle when the Single Player text is clicked
     * @param event event
     */
    private void startGame(MouseEvent event) {
        gameWindow.startChallenge();
    }

    /**
     * Handle when 'How to Play' text is clicked
     * @param event event
     */
    private void startInstructions(MouseEvent event) {gameWindow.startInstructions();}

    /**
     * Handle when the Multiplayer text is pressed
     * @param event event
     */
    private void startLobby(MouseEvent event) {gameWindow.startLobby();}

    /**
     * Handle when the Quit text is clicked
     * @param event event
     */
    private void quit(MouseEvent event){
        gameWindow.cleanup();
        Platform.exit();
    }

    /**
     * Animate the logo
     */
    public void animateLogo() {
        RotateTransition rt = new RotateTransition(Duration.millis(3000),logo);
        rt.setFromAngle(10);
        rt.setToAngle(-10);
        rt.setCycleCount(4);
        rt.setAutoReverse(true);
        rt.setOnFinished(event -> {
            if (rt.statusProperty().get() == Status.STOPPED){
                rt.stop();
                rt.playFromStart();
            }
        });
        rt.play();
    }
}
