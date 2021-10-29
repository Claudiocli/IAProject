

import java.awt.*;
import java.util.List;

/**
 * A Painter is responsible for drawing the game area. Multiple painters can be
 * used to draw the game area from different AIplayers views.
 */
public class Painter {

    private int width;
    private int height;
    private final int scale;
    private List<AIPlayer> players;
    private AIPlayer focussedPlayer;
    private Board board;
    private boolean draw = true;

    /**
     * Create a new painter with scale, board which state to be drawn, AIplayer to
     * follow and all AIplayers
     * 
     * @param scale            how much a tile should be scaled from one pixel
     * @param board            board which state to be drawn
     * @param focussedAIPlayer AIplayer to follow from which view the game area and
     *                         AIplayer should be drawn
     * @param AIplayers        all AIplayers that at each time should be drawn
     */
    Painter(int scale, Board board, AIPlayer focussedAIPlayer, List<AIPlayer> players) {
        this.scale = scale;
        this.board = board;
        this.players = players;
        this.focussedPlayer = focussedAIPlayer;
    }

    /**
     * Sets whether painter should draw or not
     * 
     * @param draw true if painter should continue to draw, false otherwise
     */
    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    /**
     * Method is called from board to initialize a draw with graphics received
     * 
     * @param g graphics object used to draw
     */
    void draw(Graphics g) {
        if (draw) {
            height = g.getClipBounds().height;
            width = g.getClipBounds().width;
            drawGameArea(g);
            drawAIPlayers(g);
        }
    }

    /**
     * Draws all AIplayers and their name on the map with corresponding color.
     * Doesn't draw AIplayers not seen by AIplayer.
     * 
     * @param g Graphics object received as argument in paintComponent method
     */
    private void drawAIPlayers(Graphics g) {
        int drawX;
        int drawY;

        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontMetrics fontMetrics = g.getFontMetrics();

        for (AIPlayer player : players) {
            // x and y position relative to focussedPlayer at which AIplayer should be drawn
            drawX = (player.getX() - focussedPlayer.getX()) * scale + ((width - scale) / 2);
            drawY = (player.getY() - focussedPlayer.getY()) * scale + ((height - scale) / 2);
            if (player != focussedPlayer) {
                // For all other players than focussedPlayer we need to smooth animations
                // regarding to animation smoothing
                // of focussedPlayer
                drawX += ((player.getDx() - focussedPlayer.getDx()) * scale
                        * ((board.getTickCounter() + 1) / (double) board.getTickReset()));
                drawY += ((player.getDy() - focussedPlayer.getDy()) * scale
                        * ((board.getTickCounter() + 1) / (double) board.getTickReset()));
            }
            g.setColor(Color.BLACK);
            g.drawString(player.getName().getValue(), drawX + (scale - fontMetrics.stringWidth(player.getName().getValue())) / 2,
                    drawY + scale + 16);

            // Draw player if visible
            if ((drawX + scale > 0 && drawX < width) && (drawY + scale > 0 && drawY < height)) {
                g.setColor(player.getColor());
                g.fillRect(drawX, drawY, scale, scale);
            }
        }
    }

    /**
     * Draws all tiles on the map with colors corresponding to owner and contested
     * owner. Doesn't draw tiles not seen by AIplayer.
     * 
     * @param g Graphics object received as argument in paintComponent method
     */
    private void drawGameArea(Graphics g) {
        int drawX;
        int drawY;

        for (int y = 0; y < board.getAreaHeight(); y++) {
            for (int x = 0; x < board.getAreaWidth(); x++) {
                // x and y position relative to focussedPlayer at which tile should be drawn
                drawX = (x - focussedPlayer.getX()) * scale + ((width - scale) / 2);
                drawY = (y - focussedPlayer.getY()) * scale + ((height - scale) / 2);

                // If visible, draw the tile's color EDIT: drawing first with white, to have
                // lighter colors
                if ((drawX + scale > 0 && drawX < width) && (drawY + scale > 0 && drawY < height)) {
                    g.setColor(Color.WHITE);
                    g.fillRect(drawX, drawY, scale, scale);
                    g.setColor(board.getTile(x, y).getColor());
                    g.fillRect(drawX, drawY, scale, scale);
                }
            }
        }
    }

}
