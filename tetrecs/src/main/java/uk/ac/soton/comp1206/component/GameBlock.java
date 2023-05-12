package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Blend;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    /**
     * Logger logs events
     */
    private static final Logger logger = LogManager.getLogger(GameBlock.class);
    /**
     * State of GameBlock
     */
    private boolean highlighted;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    /**
     * Board block belongs to
     */
    private final GameBoard gameBoard;

    /**
     * width of block
     */
    private final double width;
    /**
     * height of block
     */
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    /**
     * graphics context for outlining blocks
     */
    private final GraphicsContext outline = getGraphicsContext2D();

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
            logger.info(COLOURS[value.get()].getOpacity());
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        var color = new Color(0.0,0.0,0.0,0.3);

        gc.setFill(color);
        gc.fillRect(0,0, width, height);

        //Light.Distant light = new Light.Distant();
        //light.setAzimuth(-120.0);

        //var lighting = new Lighting();
        //lighting.setLight(light);
        //lighting.setSurfaceScale(10.0);


        //Border
        gc.setStroke(new Color(1.0,1.0,1.0,0.3));
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        Light.Distant light = new Light.Distant();
        light.setAzimuth(-120.0);

        var lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(10.0);
        gc.applyEffect(lighting);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour and opacity
     * @param colour the colour to paint
     * @param opacity the opacity of graphics context
     */
    private void paintColor(Paint colour,Double opacity) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);
        gc.setGlobalAlpha(opacity);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        Light.Distant light = new Light.Distant();
        light.setAzimuth(-120.0);

        var lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(10.0);
        gc.applyEffect(lighting);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Outline this canvas, to show mouse is on it.
     */
    public void outline(){
        outline.setStroke(Color.WHITE);
        outline.setLineWidth(10);
        outline.strokeRect(0,0,width,height);


        Light.Distant light = new Light.Distant();
        light.setAzimuth(-120.0);

        var lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(10.0);
        outline.applyEffect(lighting);

        outline.setLineWidth(1);
    }

    /**
     * Highlight Block by placing a circle on it.
     */
    public void highlight(){
        highlighted = true;
        var gc = getGraphicsContext2D();
        gc.setFill(new Color(1.0,1.0,1.0,0.5));
        gc.fillOval(height/4,width/4, width/2, height/2);


        Light.Distant light = new Light.Distant();
        light.setAzimuth(-120.0);

        var lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(3.0);
    }

    /**
     * Fade out Block using animation timer
     */
    public void fadeOut(){
        AnimationTimer timer = new AnimationTimer() {
            double opacity = COLOURS[value.get()].getOpacity();
            Color color = COLOURS[value.get()];
            @Override
            public void handle(long now) {
                logger.info(opacity);
                paintEmpty();
                paintColor(color,opacity);
                opacity -= 0.01;

                if (opacity <= 0.3){
                    paintEmpty();
                    stop();
                    logger.info("Animation Stops Here!");
                }
            }

        };
        timer.start();
        logger.info("Animation timer started");
    }


    /**
     * Remove outline on Block
     */
    public void clearOutline(){
        paint();
        if(isHighlighted()){
            highlight();
        }
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Set the value of this block
     * @param i value to Set
     */
    public void setValue(int i){
        value.setValue(i);
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Check whether block is highlighted.
     * @return boolean depending on state of Block
     */
    public boolean isHighlighted() {
        return highlighted;
    }
}
