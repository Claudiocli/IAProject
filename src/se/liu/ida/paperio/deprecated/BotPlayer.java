package se.liu.ida.paperio.deprecated;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * BotPlayer adds bot movement on top of abstract class Player. Movement is randomised as long as it is valid moves. A
 * BotPlayer has a random Pokemon name.
 * @deprecated
 */
@Deprecated(forRemoval = true)
class BotPlayer extends Player{

    /**
     * Constructs a new BotPLayer on a random spot on the game area with specified color with a randomized direction
     * @param height height of game area player is constructed in
     * @param width width of game area player is constructed in
     * @param color the color of the player
     */
    BotPlayer(int height, int width, Color color){
        super(height, width, color);

        ArrayList<String> names = new ArrayList<>();
        BufferedReader br=null;
        try {
            br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("pokemon_names.txt")));
            while (br.ready())
                names.add(br.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        } finally   {
            if (br!=null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        this.name = names.get(new Random().nextInt(names.size()));
    }

    // Make smarter bots
    /**
     * Decides where the bot shall move and moves accordingly
     */
    @Override
    public void move() {
        x += dx;
        y += dy;
        double rand = Math.random();
        if (rand < 0.05 && dx != -1) {
            dx = 1;
            dy = 0;
        }else if (rand < 0.1 && dx != 1) {
            dx = -1;
            dy = 0;
        }else if (rand < 0.15 && dy != -1) {
            dx = 0;
            dy = 1;
        }else if (rand < 0.2 && dy != 1) {
            dx = 0;
            dy = -1;
        }
        avoidOutOfBounds();

    }

    /**
     * Checks if player is moving outside of game area and changes direction to prevent it
     */
    private void avoidOutOfBounds(){
        if(x == 0 && y == height - 1){
            if(dx == -1){
                dx = 0;
                dy = -1;
            }else {
                dx = 1;
                dy = 0;
            }
        }else if(x == width -1 && y == 0){
            if(dx == 1){
                dx = 0;
                dy = 1;
            } else {
                dx = -1;
                dy = 0;
            }
        }else if(x == width - 1 && y == height -1){
            if(dx == 1){
                dx = 0;
                dy = -1;
            }else {
                dx = -1;
                dy = 0;
            }
        }else if(x == 0 && y == 0){
            if(dx == -1){
                dx = 0;
                dy = 1;
            }else {
                dx = 1;
                dy = 0;
            }
        }else if ((x == 0 && dx == -1) || (x == width -1 &&  dx == 1)) {
            dx = 0;
            dy = 1;
        }else if ((y == 0 && dy == -1) || (y == height -1 && dy == 1))  {
            dx = 1;
            dy = 0;
        }
    }

    /**
     * Overridden die method from Player. Adds a timer to the bot. After set interval, the bot will respawn
     */
    @Override
    protected void die() {
        super.die();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setAlive(true);
            }
        },5000);
    }
}