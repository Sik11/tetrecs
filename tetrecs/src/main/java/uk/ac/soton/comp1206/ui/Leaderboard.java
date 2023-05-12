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
import uk.ac.soton.comp1206.scene.ScoresScene;

public class Leaderboard extends ScoresList {

  private final SimpleListProperty<Pair<Pair<String,Integer>,String>> channelScores =
      new SimpleListProperty<>();

  public Leaderboard() {
    super();

    //Set style
    getStyleClass().add("scorelist");
    setAlignment(Pos.CENTER);
    setSpacing(2);

    //Update score list when score array list is updated

    channelScores.addListener((ListChangeListener<? super Pair<Pair<String,Integer>,String>>) (c) -> updateList());
  }

  public void updateList() {

    getChildren().clear();

    HBox scoreBox = new HBox();
    scoreBox.getStyleClass().add("scoreitem");
    scoreBox.setAlignment(Pos.CENTER);
    scoreBox.setSpacing(10);

    var vBox1 = new VBox();

    for (Pair<Pair<String,Integer>,String> score : channelScores) {
      var channelScoreBox = new HBox();
      channelScoreBox.getStyleClass().add("scoreitem");
      channelScoreBox.setAlignment(Pos.CENTER);
      channelScoreBox.setSpacing(10);

      //Add names
      var names = new Text(score.getKey().getKey());
      names.getStyleClass().add("myname");
      names.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(names, Priority.ALWAYS);

      var points = new Text(score.getKey().getValue().toString());
      points.getStyleClass().add("scoreitem");
      points.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(points, Priority.ALWAYS);

      if (score.getValue().equals("-1")){
        points.setStrikethrough(true);
        names.setStrikethrough(true);
      }

      channelScoreBox.getChildren().addAll(names, points);
      vBox1.getChildren().add(channelScoreBox);
    }
    scoreBox.getChildren().addAll(vBox1);
    getChildren().add(scoreBox);

  }

  public SimpleListProperty<Pair<Pair<String,Integer>,String>> getChannelScores(){
    return channelScores;
  }
}
