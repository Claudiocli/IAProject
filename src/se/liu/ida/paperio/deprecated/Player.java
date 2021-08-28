package se.liu.ida.paperio.deprecated;

import java.awt.Color;
import java.util.ArrayList;

import se.liu.ida.paperio.Tile;

/**
 * An abstract class for a general player in the game. Human player and bot player differs a bit but their common logic
 * is specified here. It keeps track of players position, speed, color, owned and contested tiles and name. Two players
 * can also be compared that compares number of owned tiles of the player.
 * @deprecated
 */
@Deprecated(forRemoval = true)
abstract class Player implements Comparable<Player> {
    protected int x;
    protected int y;
    protected int dx;
    protected int dy;
    private Color color;
    private ArrayList<Tile> tilesOwned = new ArrayList<>();
    private ArrayList<Tile> tilesContested = new ArrayList<>();
    protected int height;
    protected int width;
    protected String name;

    private Boolean isAlive = true; 

    /**
     * Initializes a player on a random spot on the game area with specified color
     * @param height height of game area player is constructed in
     * @param width width of game area player is constructed in
     * @param color the color of the player
     */
    protected Player(int height, int width, Color color)   {
        x = (int)(Math.random() * (width - 2) +1);
        y = (int)(Math.random() * (height - 2) +1);

        if(x < 5){
            x += 5;
        }else if(x > (width -5)){
            x-= 5;
        }
        if(y < 5){
            y+= 5;
        }else if(y > (height) - 5){
            y -= 5;
        }
        this.color = color;
        this.height = height;
        this.width = width;

        double rand = Math.random();
        if (rand < 0.25) {
            dx = 1;
            dy = 0;
        } else if (rand < 5) {
            dx = -1;
            dy = 0;
        } else if (rand < 0.75) {
            dx = 0;
            dy = 1;
        } else {
            dx = 0;
            dy = -1;
        }
    }

    /**
     * The x position in the tile system
     * @return x position in the tile system
     */
    public int getX(){
        return x;
    }
    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * The y position in the tile system
     * @return y position in the tile system
     */
    public int getY(){
        return y;
    }
    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return color of the player
     */
    public Color getColor(){
        return color;
    }
    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Abstract method to move the player
     */
    public abstract void move();

    /**
     * Logic for when player gets killed. Turns all associated tiles to neutral
     */
    protected void die() {
        isAlive = false;
        ArrayList<Tile> ownedTilesCopy = (ArrayList<Tile>)tilesOwned.clone();
        ArrayList<Tile> contestedTilesCopy = (ArrayList<Tile>)tilesContested.clone();
        for(int i = 0; i < ownedTilesCopy.size(); i++){
            ownedTilesCopy.get(i).setOwner(null);
        }

        for(int i = 0; i < contestedTilesCopy.size(); i++){
            contestedTilesCopy.get(i).setContestedOwner(null);
        }
        tilesOwned.clear();
        tilesContested.clear();

    }

    /**
     * Add tile to players list of owned tiles
     * @param t Tile to be added to players owned list
     */
    public void setTileOwned(Tile t){
        tilesOwned.add(t);
        // t.setOwner(this);
        t.setContestedOwner(null);
    }

    /**
     * Remove a tile from owned
     * @param t tile to be removed from owned
     */
    public void removeTileOwned(Tile t){
        tilesOwned.remove(t);
    }

    /**
     * Get tiles owned by player
     * @return Tiles owned by player
     */
    public ArrayList<Tile> getTilesOwned(){
        return tilesOwned;
    }

    /**
     * Get as a percentage how much of the total game area a player owns
     * @return percentage of how much of the total game area a player owns
     */
    public double getPercentOwned(){
        return 100 * getTilesOwned().size() / (double)(height*width);
    }

    /**
     * Add tile to players list of contested tiles
     * @param t Tile to be added to players contested list
     */
    public void setTileContested(Tile t){
        tilesContested.add(t);
        // t.setContestedOwner(this);
    }

    /**
     * Get tiles contested by player
     * @return Tiles contested by player
     */
    public ArrayList<Tile> getTilesContested(){
        return tilesContested;
    }


    /**
     * Sets contested tiles to owned by player
     */
    public void contestToOwned(){
        for (Tile t : tilesContested) {
            setTileOwned(t);
        }
        tilesContested.clear();
    }

    /**
     * Kills the player contesting a tile when travelling on it
     * @param t tile which contested owner should get killed
     */
    public void checkCollision(Tile t){
        if(t.getContestedOwner() != null) {
            t.getContestedOwner().die();
        }
    }

    /**
     * Get the players speed in x direction
     * @return Players speed in x direction
     */
    public int getDx() {
        return dx;
    }

    /**
     * Get the players speed in y direction
     * @return Players speed in y direction
     */
    public int getDy() {
        return dy;
    }

    /**
     * Get name of player
     * @return Name of player
     */
    public String getName() {
        return name;
    }

    /**
     * Get alive state of player
     * @return alive state of player
     */
    public Boolean getAlive() {
        return isAlive;
    }

    /**
     * Set alive state of player
     * @param alive alive state of player
     */
    public void setAlive(Boolean alive) {
        isAlive = alive;
    }

    /**
     * Compares two players by the number of tiles owned.
     * @param player Player to compare this to
     * @return 1 if this owns more tiles than player, -1 if player owns more tiles than this or 0 otherwise
     */
    public int compareTo(Player player){
        return Integer.compare(player.getTilesOwned().size(), tilesOwned.size());
    }
}