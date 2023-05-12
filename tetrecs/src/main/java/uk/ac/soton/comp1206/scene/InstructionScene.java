package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation.Status;
import javafx.animation.RotateTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;


/**
 * The instruction scene of the game. Gives instructions on how to play the game.
 */
public class InstructionScene extends BaseScene {

    /**
     * Logger used to log events
     */
    private static final Logger logger = LogManager.getLogger(InstructionScene.class);
    /**
     * Instructions image to display
     */
    private final ImageView instructions = new ImageView(new Image(getClass().getResource("/images"
        + "/Instructions.png").toExternalForm()));
    /**
     * Create a new Instruction scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public InstructionScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the scene layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var instrPane = new StackPane();
        instrPane.setMaxWidth(gameWindow.getWidth());
        instrPane.setMaxHeight(gameWindow.getHeight());
        instrPane.getStyleClass().add("menu-background");
        root.getChildren().add(instrPane);

        var mainPane = new BorderPane();
        instrPane.getChildren().add(mainPane);

        var vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        mainPane.setTop(vBox);

        var instruction = new Text("Instructions");
        instruction.getStyleClass().add("heading");
        instruction.setTextAlignment(TextAlignment.CENTER);

        var text = new Text("TetrECS is a fast paced gravity-free block placement game, where you"
            + " must survive by clearing rows and columns via careful placement of the upcoming "
            + "blocks before e time runs out. Lose all 3 lives and you're destroyed");
        text.getStyleClass().add("instructions");
        text.setWrappingWidth(gameWindow.getWidth());

        instructions.setPreserveRatio(true);
        instructions.setFitWidth(500);

        var pieces = new Text("Game Pieces");
        pieces.getStyleClass().add("heading");
        pieces.setTextAlignment(TextAlignment.CENTER);

        vBox.getChildren().add(instruction);
        vBox.getChildren().add(text);
        vBox.getChildren().add(instructions);
        vBox.getChildren().add(pieces);


        var grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);
        int col = -1;
        int row = 0;
//        Dynamically display all pieces on different boards
        for (int i = 0; i < 15; i++) {
            PieceBoard pieceBoard = new PieceBoard(new Grid(3,3),gameWindow.getWidth()/13,
                gameWindow.getWidth()/13);
            pieceBoard.setPiece(GamePiece.createPiece(i));
            if (col > 3){
                col = 0;
                row++;
            } else {
                col++;
            }
            grid.add(pieceBoard,col,row,1,1);
        }
        logger.info(getScene());
        mainPane.setCenter(grid);
    }

    /**
     * Initialise the instruction scene
     */
    @Override
    public void initialise() {
        logger.info("Initialising menu");
        logger.info(getScene());
        getScene().setOnKeyReleased(event -> {
            logger.info(event.getCode() + " KEY PRESSED");
            if (event.getCode() != KeyCode.ESCAPE) return;
            gameWindow.startMenu();
        });
    }
}
