package uk.ac.soton.comp1206.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Multimedia class handles playing media.
 */
public class Multimedia {

    /**
     * Logger used to Log events
     */
    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    /**
     * MediaPlayer that helps play sounds from the sounds folder
     */
    private static MediaPlayer audioPlayer;
    /**
     * MediaPlayer that plays sounds from other folders.
     */
    private static MediaPlayer musicPlayer;


    /**
     * Play audio from file in sounds folder
     * @param file file name
     */
    public static void playAudio(String file) {
        String songFile = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
        logger.info("Playing song: " + songFile);
        try {
            Media play = new Media(songFile);
            audioPlayer = new MediaPlayer(play);
            audioPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }

    /**
     * Play background music on repeat from file in folder
     * @param file file name
     */
    public static void playBackgroundMusic(String file) {
        String songFile = Multimedia.class.getResource("/" + file).toExternalForm();
        logger.info("Playing audio: " + songFile);

        try {
            Media play = new Media(songFile);
            musicPlayer = new MediaPlayer(play);
            Runnable onEnd = new Runnable() {
                @Override
                public void run() {
                    musicPlayer.dispose();
                    musicPlayer = new MediaPlayer(play);
                    musicPlayer.play();
                    musicPlayer.setOnEndOfMedia(this);
                }
            };
        musicPlayer.setOnEndOfMedia(onEnd);
        musicPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }
}
