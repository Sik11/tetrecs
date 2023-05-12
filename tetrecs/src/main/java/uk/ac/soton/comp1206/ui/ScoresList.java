package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
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

/**
 * ScoresList is a Vbox of all the scores
 */
public class ScoresList extends VBox {
  private final SimpleListProperty<Pair<String,Integer>> scores = new SimpleListProperty<>();
  private final SimpleListProperty<Pair<String,Integer>> remoteScores = new SimpleListProperty<>();
  private static final Logger logger = LogManager.getLogger(ScoresScene.class);


  /**
   * Create ScoresList
   */
  public ScoresList() {

    //Set style
    getStyleClass().add("scorelist");
    setAlignment(Pos.CENTER);
    setSpacing(2);

    //Update score list when score array list is updated
    scores.addListener((ListChangeListener<? super Pair<String, Integer>>) (c) -> updateList());
    remoteScores.addListener((ListChangeListener<? super Pair<String, Integer>>) (c) -> updateList());
  }

  /**
   * Update the ScoreList
   */
  public void updateList() {
    //Remove previous children
    getChildren().clear();

    HBox scoreBox = new HBox();
    scoreBox.getStyleClass().add("scoreitem");
    scoreBox.setAlignment(Pos.CENTER);
    scoreBox.setSpacing(10);

    var vBox1 = new VBox();
    var vBox2 = new VBox();


    int counter1 = 0;
    int counter2 = 0;
    int time = 0;
    for (Pair<String, Integer> score : scores) {
      counter1++;
      if (counter1 > 5) {
        break;
      }
      //Create an HBox for each score
      var localScoreBox = new HBox();
      localScoreBox.getStyleClass().add("scoreitem");
      localScoreBox.setAlignment(Pos.CENTER);
      localScoreBox.setSpacing(10);

      //Add names
      var names = new Text(score.getKey());
      names.getStyleClass().add("myname");
      names.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(names, Priority.ALWAYS);

//      Add points
      var points = new Text(score.getValue().toString());
      points.getStyleClass().add("scoreitem");
      points.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(points, Priority.ALWAYS);

      localScoreBox.getChildren().addAll(names, points);
      vBox1.getChildren().add(localScoreBox);
      reveal(localScoreBox,time);
      time += 1000;
    }
    time=0;
    for (Pair<String, Integer> remoteScore : remoteScores) {
      counter2++;
      if (counter2>5) {
        break;
      }
      var remoteScoreBox = new HBox();
      remoteScoreBox.getStyleClass().add("scoreitem");
      remoteScoreBox.setAlignment(Pos.CENTER);
      remoteScoreBox.setSpacing(10);

      var remoteNames = new Text(remoteScore.getKey());
      remoteNames.getStyleClass().add("myname");
      remoteNames.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(remoteNames, Priority.ALWAYS);

      //Add points
      var remotePoints = new Text(remoteScore.getValue().toString());
      remotePoints.getStyleClass().add("scoreitem");
      remotePoints.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(remotePoints, Priority.ALWAYS);

      remoteScoreBox.getChildren().addAll(
          remoteNames, remotePoints);
      reveal(remoteScoreBox,time);
      time +=1000;
      vBox2.getChildren().add(remoteScoreBox);
    }
    //Add score box
    scoreBox.getChildren().addAll(vBox1,vBox2);
    getChildren().add(scoreBox);
  }

  /**
   * Get the scoreProperty
   * @return scoreProperty
   */
  public ListProperty<Pair<String,Integer>> scoreProperty() {
    return scores;
  }

  /**
   * Get remotescoreProperty
   * @return remoteScoreProperty
   */
  public ListProperty<Pair<String,Integer>> remoteScoreProperty(){
    return remoteScores;
  }

  /**
   * Reveal node
   * @param n node to reveal
   * @param time duration of the transition
   */
  public void reveal(Node n,int time){
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(time),n);
    fadeTransition.setFromValue(0);
    fadeTransition.setToValue(1.0);
    fadeTransition.setCycleCount(1);
    fadeTransition.play();
  }
}
