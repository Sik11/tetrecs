package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;


/**
 * The Lobby Scene of the game. Allows users to join channels.
 */
public class LobbyScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    private ArrayList<String> list = new ArrayList<>();
    private ObservableList<String> observableList = FXCollections.observableArrayList(list);
    private SimpleListProperty<String> channelList =
        new SimpleListProperty<>(observableList);
    private ArrayList<String> list1 = new ArrayList<>();
    private ObservableList<String> observableList1 = FXCollections.observableArrayList(list1);
    private SimpleListProperty<String> userList =
        new SimpleListProperty<>(observableList1);
    private ScrollPane scroller;
    private HBox horizontalPane;
    private VBox messages;
    private TextField text;
    private boolean scrollToBottom;
    private SimpleBooleanProperty atLobby;
    private final Communicator communicator;
    private TimerTask timerTask;
    private Timer timer;
    private TimerTask timerTask1;
    private Timer timer1;


    /**
     * Create a new Lobby scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Lobby Scene");
        communicator = gameWindow.getCommunicator();
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build(){
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background");
        root.getChildren().add(lobbyPane);

        var mainPane = new BorderPane();
        lobbyPane.getChildren().add(mainPane);

        atLobby = new SimpleBooleanProperty(true);
        startTimer();

        var channelBox = new VBox();
        var channelHeading = new Text("LIST OF CHANNELS");
        channelHeading.getStyleClass().add("heading");
        channelBox.getChildren().add(channelHeading);
        channelBox.setAlignment(Pos.CENTER);
        channelBox.setSpacing(10);

        var button = new Button("START NEW CHANNEL");
        button.setOnMouseClicked(this::createChannel);
        mainPane.setBottom(button);
        mainPane.setCenter(channelBox);
//      What's built on the screen depends on events.
        communicator.addListener((message) -> {
            if (message.startsWith("CHANNELS ")) {
                logger.info("Listener has started");
                Platform.runLater(() -> {
                    channelList.addListener((ListChangeListener<String>) (c) -> {
                        channelBox.getChildren().clear();
                        channelBox.getChildren().add(channelHeading);
                        channelBox.setAlignment(Pos.CENTER);
                        channelBox.setSpacing(10);
                        for (String channel : channelList) {
                            //Add channel
                            var channelText = new Text(channel);
                            channelText.setId("channel");
                            channelText.getStyleClass().add("channelItem");
                            channelText.setTextAlignment(TextAlignment.CENTER);
                            channelText.setOnMouseClicked((e)->{
                                atLobby.set(false);
                                communicator.send("JOIN "+channel);
                            });
                            channelBox.getChildren().add(channelText);
                        }
                    });
                    this.loadChannels(message);
                });

            } else if (message.startsWith("JOIN ")) {
                Platform.runLater(() -> {
                    atLobby.set(false);
                    var channelName = message.replaceFirst("JOIN ","");
                    logger.info("Joining channel");
                    mainPane.getChildren().removeAll(channelBox, button);
                    logger.info("Channel box was removed");
                    logger.info("Button was removed");

                    getUsers();

                    userList.addListener((ListChangeListener<String>) (c) -> {
                        var channelDetails = new VBox();
                        var heading = new HBox();
                        heading.setSpacing(10);
                        heading.setPrefWidth(gameWindow.getWidth());
                        var channel = new Text("Name of Channel: " + channelName);
                        channel.getStyleClass().add("heading");

                        var startButton = new Button("Start Game");
                        startButton.setOnMouseClicked((e)->{
                            communicator.send("START ");
                        });
                        var partButton = new Button("Leave Channel");
                        var usernameButton = new Button("Change Username");
                        startButton.setOnMouseClicked((e)->{
                            Platform.runLater(()->{
                                communicator.send("START");
                            });
                        });
                        usernameButton.setOnMouseClicked((e)->{
                            var td = new TextInputDialog("Please Enter Your Preferred "
                                + "Username");
                            td.setTitle("Enter New User Name");
                            td.setHeaderText("What would you like to call Your New Username?");
                            td.setContentText("Please enter your New Nickname: ");
                            td.showAndWait();
                            communicator.send("NICK " + td.getEditor().getText());
                        });
                        partButton.setOnMouseClicked((e)->{
                            Platform.runLater(()->{
                                timer1.cancel();
                                atLobby.set(true);
                                communicator.send("PART ");
                            });
                        });
                        heading.getChildren().addAll(channel,startButton,partButton,usernameButton);
                        var channelUsers = new HBox();
                        channelUsers.setFillHeight(true);
                        channelUsers.setAlignment(Pos.TOP_LEFT);
                        channelUsers.setSpacing(10);
                        channelUsers.getStyleClass().add("playerBox");
                        var userLabel = new Text("LIST OF USERS:");
                        channelUsers.getChildren().add(userLabel);
                        for (String s : userList) {
                            var user = new Text(s);
                            user.setTextAlignment(TextAlignment.CENTER);
                            channelUsers.getChildren().add(user);
                        }
                        channelDetails.getChildren().addAll(heading, channelUsers);
                        channelDetails.setAlignment(Pos.TOP_LEFT);
                        mainPane.setTop(channelDetails);
                        mainPane.getTop().setStyle("-fx-background-color: black");
                    });


                    scroller = new ScrollPane();
                    scroller.getStyleClass().add("scroller");

                    //Fit scroller to width.
                    scroller.setFitToWidth(true);

                    messages = new VBox();
                    messages.getStyleClass().add("messages");

                    scroller.setContent(messages);

                    //Set the Center of the BorderPane to hold the scroller
                    mainPane.setCenter(scroller);

                    horizontalPane = new HBox();
                    mainPane.setBottom(horizontalPane);

                    //Add our input text field
                    text = new TextField();
                    text.setPromptText("Enter message");
                    HBox.setHgrow(text, Priority.ALWAYS);

                    //Send message when we press enter
                    text.setOnKeyPressed((e) -> {
                        if (e.getCode() != KeyCode.ENTER)
                            return;
                        sendMessage(text.getText());
                        text.clear();
                        text.requestFocus();
                    });

                    var sendButton = new Button("Send");
                    HBox.setHgrow(sendButton, Priority.NEVER);
                    sendButton.setOnAction((e) -> {
                            sendMessage(text.getText());
                            text.clear();
                            text.requestFocus();
                    });

                    horizontalPane.getChildren().add(text);
                    horizontalPane.getChildren().add(sendButton);

                    scene.addPostLayoutPulseListener(this::goToBottom);
                });
            } else if (message.startsWith("USERS ")){
                Platform.runLater(() -> {
                    userList.clear();
                    logger.info("Received users: {}", message);
                    var users = message.split("[\n]+");
                    if (users[0].contains("USERS ")) {
                        users[0] = users[0].replaceFirst("USERS ", "");
                    }
                    userList.addAll(Arrays.asList(users));
                });
            } else if (message.startsWith("MSG ")) {
                Platform.runLater(()->{
                    var text = new Text(message.replaceFirst("MSG ", ""));
                    messages.getChildren().add(text);
                    if(scroller.getVvalue() == 0.0f || scroller.getVvalue() > 0.9f) {
                        scrollToBottom = true;
                    }
                });
            } else if (message.startsWith("PARTED")){
                logger.info("Hi");
                Platform.runLater(()->{
                    mainPane.getChildren().clear();
                    mainPane.setCenter(channelBox);
                    mainPane.setBottom(button);
                    startTimer();
                });
            }  else if(message.startsWith("NICK ")){
                Platform.runLater(()->{
                    var name = message.replaceFirst("NICK ","").split(":");
                });
            } else if(message.startsWith("START")){
                Platform.runLater(()->{
                    timer1.cancel();
                    timer.cancel();
                    gameWindow.startMultiplayer();
                });
            } else if(message.startsWith("ERROR ")){
                Platform.runLater(()->{
                    var text = message.replaceFirst("ERROR ","");
                    var alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error Dialog");
                    alert.setHeaderText("Looks like there is an error!");
                    alert.setContentText(text);
                    alert.showAndWait();
                });
            }
        });
    }

    /**
     * Initialise the lobby
     */
    @Override
    public void initialise() {
        logger.info("Initialising menu");
        getScene().setOnKeyReleased(event -> {
            logger.info(event.getCode() + " KEY PRESSED");
            if (event.getCode() != KeyCode.ESCAPE)
                return;
            Platform.runLater(() -> {
                if (timer1 != null && timer != null) {
                    timer1.cancel();
                    timer.cancel();
                }
                gameWindow.startMenu();
            });
        });
    }


    /**
     * Start timer
     */
    public void startTimer(){
        // Timer looks for available channels
        if (atLobby.get()) {
            logger.info("Timer has been started");
            if (timer != null && timerTask != null) {
                timer.cancel();
                timerTask.cancel();
            }
            timerTask = new TimerTask() {

                public void run() {
                    Platform.runLater(() -> {
                        communicator.send("LIST ");
                    });
                    startTimer();
                }

            };

            timer = new Timer();
            timer.schedule(timerTask, 1000);
        }
    }

    /**
     * Get Users when in a channel
     */
    public void getUsers() {
        if(!atLobby.get()) {
            logger.info("Timer has been started");
            if (timer1 != null && timerTask1 != null) {
                timer1.cancel();
                timerTask1.cancel();
            }
            timerTask1 = new TimerTask() {

                public void run() {
                    Platform.runLater(()->{
                        communicator.send("USERS ");
                    });
                    getUsers();
                }

            };

            timer1 = new Timer();
            timer1.schedule(timerTask1, 1000);
        }
    }

    /**
     * Load all channels
     * @param message string to load channels from
     */
    public void loadChannels(String message){
        logger.info("Loading channels");
        channelList.clear();
        logger.info("Received channels: {}",message);
        var channels = message.split("[\n]+");
        if (channels[0].contains("CHANNELS ")) {
            channels[0] = channels[0].replaceFirst("CHANNELS ", "");
        }
        channelList.addAll(Arrays.asList(channels));
        logger.info(channelList.toString());
    }

    /**
     * Create a channel when mouse is clicked
     * @param event mouse event
     */
    public void createChannel(MouseEvent event){
        var td = new TextInputDialog("Please Enter the Name of your Channel");
        td.setTitle("Enter Channel Name");
        td.setHeaderText("What would you like to call Your Channel?");
        td.setContentText("Please enter the name of your channel:");
        td.showAndWait();
        var channelName = td.getEditor().getText();
        communicator.send("CREATE " + channelName);
    }

    /**
     * Send message
     * @param text text to send to server
     */
    private void sendMessage(String text) {
        if(text.isEmpty()) return;
        communicator.send("MSG" + " " + text);
    }

    /**
     * Scroll to bottom of scroller
     */
    private void goToBottom() {
        if(!scrollToBottom) return;
        scroller.setVvalue(1.0f);
        scrollToBottom = false;
    }
}
