package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.ScoresScene;

public class ChannelsList extends VBox {
  private final SimpleListProperty<String> channels = new SimpleListProperty<>();
  //private final SimpleListProperty<Pair<String,Integer>> remoteScores = new
  // SimpleListProperty<>();
  private static final Logger logger = LogManager.getLogger(ScoresScene.class);



  public ChannelsList() {

    //Set style
    //getStyleClass().add("scorelist");
    setAlignment(Pos.CENTER);
    setSpacing(2);

    //Update score list when score array list is updated
    channels.addListener((ListChangeListener<String>) (c) -> updateList());
    //remoteScores.addListener((ListChangeListener<? super Pair<String, Integer>>) (c) ->
    // updateList());

  }

  public void updateList() {
//    logger.info("Score list was updated!");
//    logger.info(remoteScores.toString());

    //Remove previous children
    getChildren().clear();

    //Loop through the top scores
//    int counter = 0;
//    for(Pair<String,Integer> score : scores) {
//
//      //Only do the top 5 scores
//      counter++;
//      if(counter > 5) break;
//     var channelBox = new HBox();
    //getStyleClass().add("scoreitem");
    setAlignment(Pos.CENTER);
    setSpacing(10);
    var channelHeading = new Text("LIST OF CHANNELS");
    channelHeading.getStyleClass().add("heading");
    getChildren().add(channelHeading);

    for (String channel : channels) {
      //Add channel
      var channelText = new Text(channel);
      channelText.setId("channel");
      channelText.getStyleClass().add("channelItem");
      channelText.setTextAlignment(TextAlignment.CENTER);

      getChildren().add(channelText);
    }
  }

  public ListProperty<String> channelProperty() {
    return channels;
  }
}
