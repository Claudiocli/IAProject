package se.liu.ida.paperio;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;
import it.unical.mat.embasp.languages.asp.SymbolicConstant;

/**
 * Class "bean-ified" from the <code>Player</code>. Cause this code is a mess,
 * only minor changing were made
 * 
 * @author Claudio Lucisano
 */
@Id("player")
public class AIPlayer implements Comparable<AIPlayer> {
	@Param(0)
	private int x;
	@Param(1)
	private int y;
	private int dx;
	private int dy;
	private Color color;
	private ArrayList<Tile> tilesOwned = new ArrayList<>();
	private ArrayList<Tile> tilesContested = new ArrayList<>();
	private int height;
	private int width;
	@Param(2)
	private SymbolicConstant name;
	@Param(3)
	private SymbolicConstant currentDirection;
	@Param(4)
	private int areaSize;

	private int nextKey;

	private Boolean isAlive = true;

	private Random r;

	/**
	 * Initializes a AIplayer on a random spot on the game area with specified color
	 * 
	 * @param height height of game area AIplayer is constructed in
	 * @param width  width of game area AIplayer is constructed in
	 * @param color  the color of the AIplayer
	 */
	public AIPlayer(int height, int width, Color color) {
		r = new Random();

		while ((x - 3 < 0 || x + 3 >= width) || (y - 3 < 0 || y + 3 >= height)) {
			x = r.nextInt(width);
			y = r.nextInt(height);
		}

		this.color = color;
		this.height = height;
		this.width = width;

		ArrayList<SymbolicConstant> names = new ArrayList<>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("pokemon_names.txt")));
			while (br.ready())
				names.add(new SymbolicConstant(br.readLine()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		this.name = names.get(r.nextInt(names.size()));

		double rand = r.nextDouble();
		if (rand < 0.25d) {
			dx = 1;
			dy = 0;
			this.currentDirection = Board.EAST_DIRECTION;
		} else if (rand < 0.5d) {
			dx = -1;
			dy = 0;
			this.currentDirection = Board.WEST_DIRECTION;
		} else if (rand < 0.75d) {
			dx = 0;
			dy = 1;
			this.currentDirection = Board.SOUTH_DIRECTION;
		} else {
			dx = 0;
			dy = -1;
			this.currentDirection = Board.NORTH_DIRECTION;
		}
	}

	public AIPlayer(int height, int width, Color color, String name) {
		r = new Random();

		while ((x - 3 < 0 || x + 3 >= width) || (y - 3 < 0 || y + 3 >= height)) {
			x = r.nextInt(width);
			y = r.nextInt(height);
		}

		this.color = color;
		this.height = height;
		this.width = width;
		this.name = new SymbolicConstant(name);

		double rand = r.nextDouble();
		if (rand < 0.25d) {
			dx = 1;
			dy = 0;
			this.currentDirection = Board.EAST_DIRECTION;
		} else if (rand < 0.5d) {
			dx = -1;
			dy = 0;
			this.currentDirection = Board.WEST_DIRECTION;
		} else if (rand < 0.75d) {
			dx = 0;
			dy = 1;
			this.currentDirection = Board.SOUTH_DIRECTION;
		} else {
			dx = 0;
			dy = -1;
			this.currentDirection = Board.NORTH_DIRECTION;
		}
	}

	public AIPlayer() {
		r = new Random();
		// Main attributes setup
		this.color = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
		this.height = Board.getInstance().getAreaHeight();
		this.width = Board.getInstance().getAreaWidth();
		// Name setup
		ArrayList<SymbolicConstant> names = new ArrayList<>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("pokemon_names.txt")));
			while (br.ready())
				names.add(new SymbolicConstant(br.readLine()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		this.name = names.get(r.nextInt(names.size()));
		// Coords setup
		while ((x - 3 < 0 || x + 3 >= width) || (y - 3 < 0 || y + 3 >= height)) {
			x = r.nextInt(width);
			y = r.nextInt(height);
		}
		// Initial direction
		double rand = r.nextDouble();
		if (rand < 0.25d) {
			dx = 1;
			dy = 0;
			this.currentDirection = Board.EAST_DIRECTION;
		} else if (rand < 0.5d) {
			dx = -1;
			dy = 0;
			this.currentDirection = Board.WEST_DIRECTION;
		} else if (rand < 0.75d) {
			dx = 0;
			dy = 1;
			this.currentDirection = Board.SOUTH_DIRECTION;
		} else {
			dx = 0;
			dy = -1;
			this.currentDirection = Board.NORTH_DIRECTION;
		}
	}

	/**
	 * Logic for when AIplayer gets killed. Turns all associated tiles to neutral
	 */
	public void die() {
		isAlive = false;
		ArrayList<Tile> ownedTilesCopy = (ArrayList<Tile>) tilesOwned.clone();
		ArrayList<Tile> contestedTilesCopy = (ArrayList<Tile>) tilesContested.clone();
		for (int i = 0; i < ownedTilesCopy.size(); i++) {
			ownedTilesCopy.get(i).setOwner(null);
		}

		for (int i = 0; i < contestedTilesCopy.size(); i++) {
			contestedTilesCopy.get(i).setContestedOwner(null);
		}
		tilesOwned.clear();
		this.areaSize = 0;
		tilesContested.clear();

	}

	/**
	 * The x position in the tile system
	 * 
	 * @return x position in the tile system
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
	 * The y position in the tile system
	 * 
	 * @return y position in the tile system
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

	/**
	 * @return color of the AIplayer
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Get name of AIplayer
	 * 
	 * @return Name of AIplayer
	 */
	public SymbolicConstant getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(SymbolicConstant name) {
		this.name = name;
	}

	/**
	 * @return the currentDirection
	 */
	public SymbolicConstant getCurrentDirection() {
		return currentDirection;
	}

	/**
	 * @param currentDirection the currentDirection to set
	 */
	public void setCurrentDirection(SymbolicConstant currentDirection) {
		this.currentDirection = currentDirection;
	}

	/**
	 * Add tile to AIplayers list of owned tiles
	 * 
	 * @param t Tile to be added to AIplayers owned list
	 */
	public void setTileOwned(Tile t) {
		tilesOwned.add(t);
		t.setOwner(this);
		t.setContestedOwner(null);
		this.areaSize = tilesOwned.size();
	}

	/**
	 * Remove a tile from owned
	 * 
	 * @param t tile to be removed from owned
	 */
	public void removeTileOwned(Tile t) {
		tilesOwned.remove(t);
	}

	/**
	 * Get tiles owned by AIplayer
	 * 
	 * @return Tiles owned by AIplayer
	 */
	public List<Tile> getTilesOwned() {
		return tilesOwned;
	}

	/**
	 * Get as a percentage how much of the total game area a AIplayer owns
	 * 
	 * @return percentage of how much of the total game area a AIplayer owns
	 */
	public double getPercentOwned() {
		return 100 * getTilesOwned().size() / (double) (height * width);
	}

	/**
	 * Add tile to AIplayers list of contested tiles
	 * 
	 * @param t Tile to be added to AIplayers contested list
	 */
	public void setTileContested(Tile t) {
		tilesContested.add(t);
		t.setContestedOwner(this);
	}

	/**
	 * Get tiles contested by AIplayer
	 * 
	 * @return Tiles contested by AIplayer
	 */
	public List<Tile> getTilesContested() {
		return tilesContested;
	}

	/**
	 * Sets contested tiles to owned by AIplayer
	 */
	public void contestToOwned() {
		for (Tile t : tilesContested) {
			setTileOwned(t);
		}
		tilesContested.clear();
	}

	/**
	 * Kills the AIplayer contesting a tile when travelling on it
	 * 
	 * @param t tile which contested owner should get killed
	 */
	public void checkCollision(Tile t) {
		if (t.getContestedOwner() != null) {
			t.getContestedOwner().die();
		}
	}

	/**
	 * Get the AIplayers speed in x direction
	 * 
	 * @return AIPlayers speed in x direction
	 */
	public int getDx() {
		return dx;
	}

	/**
	 * Get the AIplayers speed in y direction
	 * 
	 * @return AIPlayers speed in y direction
	 */
	public int getDy() {
		return dy;
	}

	/**
	 * Get alive state of AIplayer
	 * 
	 * @return alive state of AIplayer
	 */
	public Boolean getAlive() {
		return isAlive;
	}

	/**
	 * Set alive state of AIplayer
	 * 
	 * @param alive alive state of AIplayer
	 */
	public void setAlive(Boolean alive) {
		isAlive = alive;
	}

	/**
	 * Compares two AIplayers by the number of tiles owned.
	 * 
	 * @param AIplayer AIPlayer to compare this to
	 * @return 1 if this owns more tiles than AIplayer, -1 if AIplayer owns more
	 *         tiles than this or 0 otherwise
	 */
	public int compareTo(AIPlayer other) {
		return Integer.compare(other.getTilesOwned().size(), tilesOwned.size());
	}

	/**
	 * Set key to change dx and dy in next tick
	 * 
	 * @param nextKey key to change dx and dy in next tick
	 */
	public void setNextKey(int nextKey) {
		this.nextKey = nextKey;
		updateD();
	}

	/**
	 * Moves the player in different directions
	 */
	public void move() {
		x += dx;
		y += dy;
	}

	/**
	 * Updates dx and dy regarding to key sent as input
	 */
	public void updateD() {
		// Left
		if ((nextKey == KeyEvent.VK_LEFT || nextKey == KeyEvent.VK_A) && dx != 1) {
			dx = -1;
			dy = 0;
		}

		// Right
		if ((nextKey == KeyEvent.VK_RIGHT || nextKey == KeyEvent.VK_D) && dx != -1) {
			dx = 1;
			dy = 0;
		}

		// Up
		if ((nextKey == KeyEvent.VK_UP || nextKey == KeyEvent.VK_W) && dy != 1) {
			dx = 0;
			dy = -1;
		}

		// Down
		if ((nextKey == KeyEvent.VK_DOWN || nextKey == KeyEvent.VK_S) && dy != -1) {
			dx = 0;
			dy = 1;
		}
	}

	/**
	 * @return the areaSize
	 */
	public int getAreaSize() {
		return areaSize;
	}

	/**
	 * @param areaSize the areaSize to set
	 */
	public void setAreaSize(int areaSize) {
		this.areaSize = areaSize;
	}

}
