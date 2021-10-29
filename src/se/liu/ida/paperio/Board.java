package se.liu.ida.paperio;

import javax.swing.*;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.languages.asp.ASPMapper;
import it.unical.mat.embasp.languages.asp.AnswerSet;
import it.unical.mat.embasp.languages.asp.AnswerSets;
import it.unical.mat.embasp.languages.asp.SymbolicConstant;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Timer;
import java.util.*;

/**
 * The board class is the class responsible for main game logic. This class
 * initializes the tile grid, players and keeps track of them. Board is also
 * specifies key bindings, fills enclosed areas and keeps track of a timer to
 * tick through game logic. Board draws the live scoreboard but uses one or two
 * Painter:s to draw the game area and players on it.
 */
public class Board extends JPanel {
    private static final boolean DEBUG = false;

    private static final String PAUSE = "Pause";

    public static final SymbolicConstant NORTH_DIRECTION = new SymbolicConstant("north");
    public static final SymbolicConstant SOUTH_DIRECTION = new SymbolicConstant("south");
    public static final SymbolicConstant WEST_DIRECTION = new SymbolicConstant("west");
    public static final SymbolicConstant EAST_DIRECTION = new SymbolicConstant("east");

    private final int areaHeight;
    private final int areaWidth;
    private transient Tile[][] gameArea;
    private static final int SCALE = 20;

    private transient AIPlayer focussed;
    private transient AIPlayer focussed2;

    private int botNumber;
    private transient ArrayList<AIPlayer> players;
    private transient HashMap<Tile, AIPlayer> tileAIPlayerMap = new HashMap<>();

    private int tickCounter = 0;
    private final int tickReset;

    private transient ArrayList<AIPlayer> deadBots = new ArrayList<>();
    private boolean paused = true;
    private transient ActionListener actionListener;

    private transient ArrayList<Painter> painters = new ArrayList<>();
    private transient HashMap<AIPlayer, Painter> focussedPainter = new HashMap<>();

    private Random r;
    private boolean multiplayer = false;

    // I know, have mercy. The entire class, if not the whole code, should be refactored, 
    // but it's a mess and it would take too much time. Damn spaghetti code.
    private static Board instance = null;

    private List<Color> colorList = new ArrayList<>(
            Arrays.asList(Color.magenta, Color.green, Color.red, Color.blue, Color.orange, Color.yellow, Color.pink,
                    new Color(142, 12, 255), new Color(255, 43, 119), new Color(100, 255, 162)));

    /**
     * Creates board for singleplayer
     * 
     * @param actionListener listener for key presses and state updates
     * @param p1name         name of player
     * @param areaHeight     height of game area
     * @param areaWidth      width of game area
     * @param gameSpeed      game speed between 1 and 5, 5 being the fastest
     * @param botNumber      number of bots to have in game
     */
    Board(ActionListener actionListener, String p1name, int areaHeight, int areaWidth, int gameSpeed, int botNumber) {
        players = new ArrayList<>();
        instance = this;

        this.actionListener = actionListener;
        this.areaHeight = areaHeight;
        this.areaWidth = areaWidth;
        this.botNumber = botNumber;
        int[] speeds = { 12, 10, 8, 6, 4 };
        tickReset = speeds[gameSpeed - 1];

        r = new Random();

        players.add(
                new AIPlayer(areaHeight, areaWidth, new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)), p1name));
        focussed = players.get(0);

        initBoard();

        painters.add(new Painter(SCALE, this, focussed, players));
        focussedPainter.put(focussed, painters.get(0));
    }

    /**
     * Creates board for multiplayer
     * 
     * @param actionListener listener for key presses and state updates
     * @param p1name         name of player 1
     * @param p2name         name of player 2
     * @param areaHeight     height of game area
     * @param areaWidth      width of game area
     * @param gameSpeed      game speed between 1 and 5, 5 being the fastest
     * @param botNumber      number of bots to have in game
     */
    Board(ActionListener actionListener, String p1name, String p2name, int areaHeight, int areaWidth, int gameSpeed,
            int botNumber) {
        players = new ArrayList<>();
        r = new Random();
        instance = this;
        multiplayer = true;

        this.actionListener = actionListener;
        this.areaHeight = areaHeight;
        this.areaWidth = areaWidth;
        this.botNumber = botNumber;
        int[] speeds = { 12, 10, 8, 6, 4, 2 };
        tickReset = speeds[gameSpeed - 1];

        players.add(
                new AIPlayer(areaHeight, areaWidth, new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)), p1name));
        players.add(
                new AIPlayer(areaHeight, areaWidth, new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)), p2name));
        focussed = players.get(0);
        focussed2 = players.get(1);

        initBoard();

        painters.add(new Painter(SCALE, this, focussed, players));
        painters.add(new Painter(SCALE, this, focussed2, players));
        focussedPainter.put(focussed, painters.get(0));
        focussedPainter.put(focussed2, painters.get(1));
    }

    /**
     * Initializes necessary variables, timer, players etc required for the board
     */
    private void initBoard() {
        this.gameArea = new Tile[areaWidth][areaHeight];
        for (int i = 0; i < gameArea.length; i++) {
            for (int j = 0; j < gameArea[i].length; j++) {
                gameArea[i][j] = new Tile(i, j);
            }
        }

        specifyKeyActions();

        setBackground(Color.BLACK);

        // Adds new bots and give them a color either from colorList or randomized
        for (int i = 0; i < botNumber; i++) {
            players.add(new AIPlayer());
        }

        ArrayList<AIPlayer> playersToRemove = new ArrayList<>();
        // Gives each player a starting area and makes sure that they don't spawn too
        // close to each other
        for (int i = 0; i < players.size(); i++) {
            // If bot is too close to another bot, remove it and create a new one instead
            if (!checkSpawn(players.get(i))) {
                playersToRemove.add(players.get(i));
                if (botNumber > 9) {
                    players.add(new AIPlayer(gameArea.length, gameArea[0].length,
                            new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255))));
                } else {
                    players.add(new AIPlayer(gameArea.length, gameArea[0].length, colorList.get(i)));
                }
            } else {
                startingArea(players.get(i));
            }
        }

        for (AIPlayer p : playersToRemove)
            players.remove(p);

        // Starts a timer to tick the game logic
        // TODO: ensure the ConcurrentModificationException was raised because of the little initial delay
        final int INITIAL_DELAY = 100;
        final int PERIOD_INTERVAL = 1000 / 60;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(), INITIAL_DELAY, PERIOD_INTERVAL);
    }

    /**
     * Specifies necessary key bindings and key actions for game
     */
    private void specifyKeyActions() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUP");
        am.put("moveUP", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                focussed.setNextKey(KeyEvent.VK_UP);
                focussed.setCurrentDirection(NORTH_DIRECTION);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDOWN");
        am.put("moveDOWN", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                focussed.setNextKey(KeyEvent.VK_DOWN);
                focussed.setCurrentDirection(SOUTH_DIRECTION);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLEFT");
        am.put("moveLEFT", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                focussed.setNextKey(KeyEvent.VK_LEFT);
                focussed.setCurrentDirection(WEST_DIRECTION);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRIGHT");
        am.put("moveRIGHT", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                focussed.setNextKey(KeyEvent.VK_RIGHT);
                focussed.setCurrentDirection(EAST_DIRECTION);
            }
        });
        if (multiplayer) {
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveP2UP");
            am.put("moveP2UP", new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    focussed2.setNextKey(KeyEvent.VK_UP);
                    focussed2.setCurrentDirection(NORTH_DIRECTION);
                }
            });
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveP2DOWN");
            am.put("moveP2DOWN", new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    focussed2.setNextKey(KeyEvent.VK_DOWN);
                    focussed2.setCurrentDirection(SOUTH_DIRECTION);
                }
            });
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveP2LEFT");
            am.put("moveP2LEFT", new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    focussed2.setNextKey(KeyEvent.VK_LEFT);
                    focussed2.setCurrentDirection(WEST_DIRECTION);
                }
            });
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveP2RIGHT");
            am.put("moveP2RIGHT", new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    focussed2.setNextKey(KeyEvent.VK_RIGHT);
                    focussed2.setCurrentDirection(EAST_DIRECTION);
                }
            });
        }

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), PAUSE);
        am.put(PAUSE, new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                ActionEvent action = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, PAUSE);
                actionListener.actionPerformed(action);
            }
        });
    }

    /**
     * Marks all tiles in the starting area of a player to owned by player
     * 
     * @param player player to generate starting area for
     */
    private void startingArea(AIPlayer player) {
        int x = player.getX();
        int y = player.getY();
        if (!checkSpawn(player)) {
            AIPlayer playerCopy = new AIPlayer(gameArea.length, gameArea[0].length, player.getColor());
            startingArea(playerCopy);
        }
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                player.setTileOwned(getTile(i, j));
            }
        }
    }

    /**
     * Makes sure that a player doesn't spawn on, or too close to another player.
     * Range is set to ±9 square tiles
     * 
     * @param player AIPlayer that you want to check surroundings for other players
     * @return True if nobody is close, False otherwise
     */
    private boolean checkSpawn(AIPlayer player) {
        int x = player.getX();
        int y = player.getY();
        for (int i = x - 3; i <= x + 3; i++) {
            for (int j = y - 3; j <= y + 3; j++) {
                if (getTile(i, j).getOwner() != null || getTile(i, j).getContestedOwner() != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Overrides paintComponent and is called whenever everything should be drawn on
     * the screen
     * 
     * @param g Graphics element used to draw elements on screen
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < painters.size(); i++) {
            // Set clipping area for painter
            g.setClip(0, 0, getWidth(), getHeight());

            // Move graphics to top-left of clipping area
            g.translate(0, 0);

            // Painter paints area
            painters.get(i).draw(g);

            // Move graphics back to top-left of window
            g.translate(0, 0);
        }
        try {
            drawScoreboard(g);

        } catch (IndexOutOfBoundsException ignored) {
            // THIS EXCEPTION SHOULD NOT EXIST!
            // Furthermore an exception should always be handled
            // FIXME: error unhandled
        }
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Draws the live scoreboard up in the rightmost corner
     * 
     * @param g Graphics object received as argument in paintComponent method
     */
    private void drawScoreboard(Graphics g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        FontMetrics fontMetrics = g.getFontMetrics();
        int fontHeight = fontMetrics.getHeight();
        int barWidth;
        int barHeight = fontHeight + 4;

        AIPlayer player;
        String string;
        Color color;

        double highestPercentOwned = players.get(0).getPercentOwned();
        Collections.sort(players);
        for (int i = 0; i < Integer.min(5, players.size()); i++) {
            player = players.get(i);
            string = String.format("%.2f%% - %s", player.getPercentOwned(), player.getName());
            color = player.getColor();

            barWidth = (int) ((player.getPercentOwned() / highestPercentOwned) * (getWidth() / 4));
            g.setColor(player.getColor());
            g.fillRect(getWidth() - barWidth, barHeight * i, barWidth, barHeight);
            // If color is perceived as dark set the font color to white, else black
            if (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue() < 127) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawString(string, 2 + getWidth() - barWidth, barHeight * i + fontHeight);
        }
    }

    /**
     * Method to end game and tell this to PaperIO class
     */
    private void endGame() {
                
        JOptionPane.showMessageDialog(this, "You lost, game over", "GAME OVER", JOptionPane.PLAIN_MESSAGE);
        actionListener.actionPerformed(new ActionEvent(this, 0, "End Game"));
    }

    /**
     * Method that respawns dead bots after a set interval
     */
    private void respawnBots() {
        for (int i = 0; i < deadBots.size(); i++) {
            if (Boolean.FALSE.equals(deadBots.get(i).getAlive())) {
                AIPlayer player = new AIPlayer(gameArea.length, gameArea[0].length,
                        new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
                startingArea(player);
                players.add(player);
                deadBots.remove(deadBots.get(i));
            }
        }
    }

    /**
     * Method that detects player-to-player head on collision
     * 
     * @param player AIPlayer you want to check collision for
     * @param tile   Tile that AIPlayer currently is on
     */
    private void findCollision(AIPlayer player, Tile tile) {
        // If corresponding tile is found in tileAIPlayerMap
        if (tileAIPlayerMap.containsKey(tile)) {

            // Iterate through all entries in tileAIPlayerMap, if the Tile in entry matches
            // Tile in input,
            // compare sizes between players and destroy one of them. The player with the
            // largest tiles contested
            // survives. If both players have the same amount of tiles contested, the player
            // with the most tiles
            // owned survives. If both players have the same amount of tiles contested and
            // tiles owned,
            // the first player added to AIPlayers list dies.
            for (Map.Entry<Tile, AIPlayer> entry : tileAIPlayerMap.entrySet()) {
                if (entry.getKey() == tile) {
                    if (entry.getValue().getTilesContested().size() > player.getTilesContested().size()) {
                        entry.getValue().die();
                    } else if (entry.getValue().getTilesContested().size() < player.getTilesContested().size()) {
                        player.die();
                    } else if (entry.getValue().getTilesContested().size() == player.getTilesContested().size()) {
                        if (entry.getValue().getTilesOwned().size() > player.getTilesOwned().size()) {
                            entry.getValue().die();
                        } else {
                            player.die();
                        }
                    }
                }
            }
        } else { // If no corresponding tile is found, add tile and player to tileAIPlayerMap
            tileAIPlayerMap.put(tile, player);
        }
        // Remove dead players
        players.removeIf(p -> !p.getAlive());
    }

    /**
     * After a player has traveled out to enclose an area the area needs to be
     * filled. This method depends on that the AIPlayer.contestedToOwned() method
     * has been called. The method works by doing a depth first search from each
     * tile adjacent to a tile owned by the player sent as parameter. If the DFS
     * algorithm finds a boundary we know it is not enclosed and should not be
     * filled. The boundary is the smallest rectangle surrounding all owned tiles by
     * the player to minimize cost of method. If the DFS can't find the boundary or
     * if the one the DFS starts on we know it should be filled.
     * 
     * @param player The player whose enclosure to be filled
     */
    private void fillEnclosure(AIPlayer player) {
        // Set boundary
        int maxX = 0;
        int minX = gameArea.length;
        int maxY = 0;
        int minY = gameArea[0].length;
        for (Tile t : player.getTilesOwned()) {
            if (t.getX() > maxX)
                maxX = t.getX();
            if (t.getX() < minX)
                minX = t.getX();
            if (t.getY() > maxY)
                maxY = t.getY();
            if (t.getY() < minY)
                minY = t.getY();
        }

        // Necessary collections for DFS to work
        ArrayList<Tile> outside = new ArrayList<>();
        ArrayList<Tile> inside = new ArrayList<>();
        ArrayList<Tile> visited = new ArrayList<>();
        HashSet<Tile> toCheck = new HashSet<>();

        // Add all adjacent tiles
        int y;
        int x;
        for (Tile t : player.getTilesOwned()) {
            // I know this is horrible, I don't have time to fix this shit.
            y = t.getX();
            x = t.getY();
            // Can be enclosed in one single if statement
            if (y - 1 >= 0)
                toCheck.add(gameArea[y - 1][x]);
            if (y + 1 < gameArea.length)
                toCheck.add(gameArea[y + 1][x]);
            if (x - 1 >= 0)
                toCheck.add(gameArea[y][x - 1]);
            if (x + 1 < gameArea[y].length)
                toCheck.add(gameArea[y][x + 1]);
        }

        // Loop over all tiles to do DFS from
        for (Tile t : toCheck) {
            if (!inside.contains(t)) {
                Stack<Tile> stack = new Stack<>();
                boolean canContinue = true;
                Tile v;
                visited.clear();

                // DFS algorithm
                stack.push(t);
                while ((!stack.empty()) && canContinue) {
                    v = stack.pop();
                    if (!visited.contains(v) && (v.getOwner() != player)) {
                        // Please, have mercy. They don't know what they're doin'
                        y = v.getX();
                        x = v.getY();
                        if (outside.contains(v) // If already declared as outside
                                || x < minX || x > maxX || y < minY || y > maxY // If outside of boundary
                                || x == gameArea[0].length - 1 || x == 0 || y == 0 || y == gameArea.length - 1) {
                            // If it is an edge tile
                            canContinue = false;
                        } else {
                            visited.add(v);
                            if (y - 1 >= 0)
                                stack.push(gameArea[y - 1][x]);
                            if (y + 1 < gameArea.length)
                                stack.push(gameArea[y + 1][x]);
                            if (x - 1 >= 0)
                                stack.push(gameArea[y][x - 1]);
                            if (x + 1 < gameArea[y].length)
                                stack.push(gameArea[y][x + 1]);
                        }
                    }
                }
                if (canContinue) { // If DFS don't find boundary
                    inside.addAll(visited);
                } else {
                    outside.addAll(visited);
                }
            }
        }

        // Set all enclosed tiles to be owned by player
        for (Tile t : inside) {
            player.setTileOwned(t);
        }
    }

    /**
     * Set board to paused mode, meaning logic and graphics are not updated
     * 
     * @param b True if game should be paused, false otherwise
     */
    void setPaused(Boolean b) {
        paused = b;
    }

    /**
     * Get height of game area
     * 
     * @return height of game area
     */
    int getAreaHeight() {
        return areaHeight;
    }

    /**
     * Get width of game area
     * 
     * @return width of game area
     */
    int getAreaWidth() {
        return areaWidth;
    }

    /**
     * Get current tick counter
     * 
     * @return current tick counter
     */
    int getTickCounter() {
        return tickCounter;
    }

    /**
     * Get how often tick is reset, impacting speed of game
     * 
     * @return how often tick is reset
     */
    int getTickReset() {
        return tickReset;
    }

    /**
     * Get tile at position (x,y)
     * 
     * @param x x position of tile
     * @param y y position of tile
     * @return tile at position (x,y)
     */
    Tile getTile(int x, int y) {
        // Why? Just why?
        // return gameArea[y][x];
        return gameArea[x][y];
    }

    public static Board getInstance() {
        // instance would be initialized in one of the constructor
        // I know it's bad, but it's just for semplicity's sake
        return instance;
    }

    public Tile[][] getMapTiles() {
        return gameArea;
    }

    // Should be refactored
    /**
     * ScheduleTask is responsible for receiving and responding to timer calls
     */
    private class ScheduleTask extends TimerTask {
        private DLV2DesktopService desktopService;
        private Handler handler;
        // private OptionDescriptor noFactsOption;
        private ASPInputProgram fixedProgram;
        private ASPInputProgram variableProgram;

        private int cont;

        public ScheduleTask() {
            this.desktopService = new DLV2DesktopService("lib\\Dlv2\\dlv2_32bit.exe");
            this.handler = new DesktopHandler(desktopService);
            // this.noFactsOption = new OptionDescriptor("--no-facts");
            this.fixedProgram = new ASPInputProgram();
            this.variableProgram = new ASPInputProgram();

            // Debug purpose
            this.cont = 0;

            // Adding options
            // this.handler.addOption(this.noFactsOption);

            this.fixedProgram.addFilesPath("src\\se\\liu\\ida\\paperio\\AI.dl");

            // Registering all the classes needed to the ASPMapper
            try {
                ASPMapper.getInstance().registerClass(AIPlayer.class);
                ASPMapper.getInstance().registerClass(Tile.class);
                ASPMapper.getInstance().registerClass(NextMove.class);
            } catch (ObjectNotValidException | IllegalAnnotationException e) {
                JOptionPane.showMessageDialog(null, "An error has occured while registering classes in DLV", "Error mapping classes", JOptionPane.ERROR_MESSAGE);
                // System.out.println(e instanceof ObjectNotValidException ? "OBJECT NOT VALID" : "ILLEGAL ANNOTATION");
                // e.printStackTrace();
            }

            this.handler.addProgram(this.fixedProgram);
            this.handler.addProgram(this.variableProgram);
        }

        private AIPlayer getPlayerByName(String name)  {
            for (AIPlayer p : players)  {
                if (p.getName().getValue().equals(name))
                    return p;
            }
            return null;
        }

        /**
         * Gets called by timer at specified interval. Calls tick at specified rate and
         * repaint each time
         */
        @Override
        public void run() {
            if (!paused) {
                // What
                // tickCounter++;
                // tickCounter %= tickReset;
                // if (tickCounter == 0) {
                //     tick();
                // }
                tick();
                repaint();
            }
        }

        /**
         * Method responsible for main logic of the game. Checks collisions and if
         * enclosures should be filled.
         */
        private void tick() {
            // What's this?
            tileAIPlayerMap.clear();

            // DLV2 setup
            // Adding map and players to the program (it changes everytime `tick()` is called)            
            this.variableProgram.clearAll();

            try {
                for (var p : players)
                    this.variableProgram.addObjectInput(p);
                Tile[][] map = Board.getInstance().getMapTiles();
                for (var x = 0; x < map.length; x++)
                    for (var y = 0; y < map[0].length; y++)
                        this.variableProgram.addObjectInput(map[x][y]);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "An error has occurred while generating the input for DLV", "Input DLV Error", JOptionPane.ERROR_MESSAGE);
                // e.printStackTrace();
            }

            if (DEBUG)
                writeInputDLV();                

            // Calling DLV2
            Output o = this.handler.startSync();

            // Handling output
            AnswerSets answers = (AnswerSets) o;

            if (DEBUG)
                writeOutputDLV(answers);

            for (AnswerSet a : answers.getOptimalAnswerSets())  {
                try {
                    for (Object obj : a.getAtoms()) {
                        if (!(obj instanceof NextMove))
                            continue;
                        NextMove nextMove = (NextMove) obj;
                        AIPlayer player = getPlayerByName(nextMove.getName().getValue());
                        if (DEBUG)
                            printPlayerInfoDebug(player, nextMove);
                    }
                } catch (IllegalAccessException e) {
                    JOptionPane.showMessageDialog(null, "An error has occurred when handling DLV's output: Illegal Access");
                } catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "An error has occurred when handling DLV's output: Illegal Argument");
                } catch(InstantiationException e) {
                    JOptionPane.showMessageDialog(null, "An error has occurred when handling DLV's output: Instantiation Error");
                } catch(InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    JOptionPane.showMessageDialog(null, "An error has occurred when handling DLV's output: Generic");
                    // System.err.println(e.getClass() + " error message:\n"+ e.getMessage());
                    // e.printStackTrace();
                }
            }

            for (int i = 0; i < players.size(); i++) {

                var player = players.get(i);

                // movement "logic"
                player.move();

                // Kill player if player moves outside game area
                if (player.getX() < 0 || player.getX() >= areaWidth || player.getY() < 0
                        || player.getY() >= areaHeight) {
                    player.die();
                } else {
                    Tile tile = getTile(player.getX(), player.getY());
                    player.checkCollision(tile);
                    findCollision(player, tile);

                    // If player is outside their owned territory
                    if (tile.getOwner() != player && Boolean.TRUE.equals(player.getAlive())) {
                        player.setTileContested(tile);
                        // If player arrives back to an owned tile
                    } else if (!player.getTilesContested().isEmpty()) {
                        player.contestToOwned();
                        fillEnclosure(player);
                    }
                }
                // If AIPlayer is killed, add it to deadBots list
                if (player instanceof AIPlayer && Boolean.FALSE.equals(player.getAlive())) {
                    deadBots.add(player);
                }
            }
            respawnBots();

            if (DEBUG)
                System.out.println("Current player position: ["+focussed.getX() + "," + focussed.getY()+"]");

            focussedPainter.get(focussed).setDraw(focussed.getAlive());
            if (multiplayer) {
                focussed2.updateD();
                focussedPainter.get(focussed2).setDraw(focussed2.getAlive());
            }
            if (multiplayer
                    && (Boolean.FALSE.equals(focussed.getAlive()) && Boolean.FALSE.equals(focussed2.getAlive()))) {
                endGame();
            }
            if (Boolean.FALSE.equals(focussed.getAlive()))
                endGame();

            // Remove dead players
            players.removeIf(p -> !p.getAlive());
        }

        private void writeInputDLV()    {
            BufferedWriter bw = null;
            try {
                if (this.cont == 0)
                   bw = new BufferedWriter(new FileWriter(new File("src\\se\\liu\\ida\\paperio\\input.txt")));
                else
                   bw = new BufferedWriter(new FileWriter(new File("src\\se\\liu\\ida\\paperio\\input.txt"), true));
                bw.write("Input "+this.cont+"\n"+this.variableProgram.getPrograms().replaceAll("[\\r\\n]", "")+"\n");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "An error has occurred while writing on file the input of DLV", "Writing Input Error", JOptionPane.ERROR_MESSAGE);
                // e.printStackTrace();
            } finally   {
                if (bw!=null)
                    try {
                        bw.close();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "An error has occurred while closing the file to write on!", "Writing Error", JOptionPane.ERROR_MESSAGE);
                    //    e.printStackTrace();
                    }
            }
        }

        private void writeOutputDLV(AnswerSets answers)   {
            BufferedWriter bw = null;
            try {
                if (this.cont == 0)
                    bw = new BufferedWriter(new FileWriter(new File("src\\se\\liu\\ida\\paperio\\output.txt")));
                else
                    bw = new BufferedWriter(new FileWriter(new File("src\\se\\liu\\ida\\paperio\\output.txt"), true));
                bw.write("Output "+this.cont+"\n"+answers.getAnswerSetsString()+"\n");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "An error has occurred while writing on file the output of DLV", "Writing Output Error", JOptionPane.ERROR_MESSAGE);
                // e.printStackTrace();
            } finally   {
                if (bw!=null)
                    try {
                        bw.close();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "An error has occurred while closing the file to write on!", "Writing Error", JOptionPane.ERROR_MESSAGE);
                    //    e.printStackTrace();
                    }
                this.cont++;
            }
        }

        private void printPlayerInfoDebug(AIPlayer player, NextMove nextMove)  {
            System.out.println("player: "+player.getName().getValue()+" - current tile["+player.getX()+","+player.getY()+"]"+" - next direction: ");
            if (nextMove.getX() == player.getX() && nextMove.getY() == player.getY() - 1)  {
                player.setCurrentDirection(Board.NORTH_DIRECTION);
                player.setNextKey(KeyEvent.VK_UP);
                System.out.println("UP");
            }
            if (nextMove.getX() == player.getX() && nextMove.getY() == player.getY() + 1)  {
                player.setCurrentDirection(Board.SOUTH_DIRECTION);
                player.setNextKey(KeyEvent.VK_DOWN);
                System.out.println("DOWN");
            }
            if (nextMove.getX() == player.getX() - 1 && nextMove.getY() == player.getY())  {
                player.setCurrentDirection(Board.WEST_DIRECTION);
                player.setNextKey(KeyEvent.VK_LEFT);
                System.out.println("LEFT");
            }
            if (nextMove.getX() == player.getX() + 1 && nextMove.getY() == player.getY())  {
                player.setCurrentDirection(Board.EAST_DIRECTION);
                player.setNextKey(KeyEvent.VK_RIGHT);
                System.out.println("RIGHT");
            }
            System.out.println("Player is = "+player);
        }

    }
}