package se.liu.ida.paperio;

import java.awt.Color;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

/**
 * A tile in the game area. A tile has an x and y position, a color. It can also have a AIplayer as owner and a AIplayer
 * as contested owner. A tiles color does depend on owner and contested owner.
 */
@Id("tile")
public class Tile {

    private AIPlayer owner;
    @Param(2)
    private Color color;
    @Param(3)
    private AIPlayer contestedOwner;
    @Param(0)
    private int x;
    @Param(1)
    private int y;

    /**
     * Initializes a tile at position (x, y)
     * @param x x position of the tile
     * @param y y position of the tile
     */
    public Tile(int x, int y){
        this.x = x;
        this.y = y;
        this.color = Color.WHITE;
    }

    /**
     * Decides what color to be drawn depending on owner and contested owner
     * @return color of the tile
     */
    public Color getColor(){
        // If a Tile has an owner and Tile is not being contested,
        // returns owner's color darkened
        if(owner != null && contestedOwner == null) {
            return owner.getColor().darker();
        }
        // If Tile has no owner and is being contested,
        // returns contestedOwner's color with an alpha of 100
        else if (owner == null && contestedOwner != null) {
            return(new Color(contestedOwner.getColor().getRed(), contestedOwner.getColor().getGreen(),
                    contestedOwner.getColor().getBlue(), 100));
        }
        // If Tile has an owner and is being contested by someone,
        // returns contestedOwner's color with an alpha of 100
        else if (owner != null && contestedOwner != owner){
            return blendColors();
        }else{
            return color;
        }
    }
    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Blends colors of owner and contested owner
     * @return the blended color
     */
    private Color blendColors(){
        float blendedRed = ((owner.getColor().getRed() / 255f) * (contestedOwner.getColor().getRed() / 255f));
        float blendedGreen = ((owner.getColor().getGreen() / 255f) * (contestedOwner.getColor().getGreen() / 255f));
        float blendedBlue = ((owner.getColor().getBlue() / 255f) * (contestedOwner.getColor().getBlue() / 255f));

        return(new Color(((blendedRed + 1 ) / 2),((blendedGreen + 1) / 2),((blendedBlue +1 )/ 2)));
    }

    /**
     * @return AIPlayer that is contesting Tile
     */
    public AIPlayer getContestedOwner() {
        return contestedOwner;
    }

    /**
     * Sets a AIplayer as contestant to Tile
     * @param contestedOwner AIPlayer that is contesting Tile
     */
    public void setContestedOwner(AIPlayer contestedOwner) {
        this.contestedOwner = contestedOwner;
    }

    /**
     * Get owner of tile
     * @return AIPlayer that is owner of tile
     */
    public AIPlayer getOwner() {
        return owner;
    }

    /**
     * Sets owner to owner of tile and removes current owner and contested owner
     * @param owner AIPlayer to be set as owner of tile
     */
    public void setOwner(AIPlayer owner) {
        if(this.owner != null){
            this.owner.removeTileOwned(this);
        }
        this.owner = owner;
    }

    /**
     * Get the tiles x-position
     * @return The x-position of the tile
     */
    public int getX() {
        return x;
    }
    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get the tiles y-position
     * @return The y-position of the tile
     */
    public int getY() {
        return y;
    }
    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

}