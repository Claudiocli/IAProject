

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * PaperIO is the main class used to start window and keep track of current state and switch between states.
 */
public class PaperIO extends JFrame implements ActionListener{
    private static final String BOARD_CARD_NAME = "board";
    private static final String MENU_CARD_NAME = "menu";

    private Board board;
    private Menu menu;
    private JPanel cards;

    /**
     * Initializes a new occurrence of game
     */
    public PaperIO(){
        initUI();
    }

    /**
     * Specifies size, title and layout etc for game
     */
    private void initUI(){

        setSize(1000, 1000);
        setResizable(true);
        setVisible(true);
        setTitle("Paper.io");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        menu = new Menu(this);
        cards = new JPanel(new CardLayout());
        cards.add(menu, MENU_CARD_NAME);

        add(cards);
    }

    /**
     * Enum with all possible game states
     */
    private enum STATE{
        GAME,
        MENU
    }

    /**
     * Sets game state to specified state
     * @param s STATE game should be set to
     */
    private void setState(STATE s){
        CardLayout cardLayout = (CardLayout) cards.getLayout();
        if(s == STATE.GAME){
            cardLayout.show(cards, BOARD_CARD_NAME);
            board.setPaused(false);
        }else if(s == STATE.MENU){
            cardLayout.show(cards, MENU_CARD_NAME);
            board.setPaused(true);
        }
    }

    /**
     * Reacts to game actions such as game start and game paused
     * @param e event to react to
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Play Singleplayer":
                board = new Board(this, menu.getP1Name(), menu.getAreaHeight(), menu.getAreaWidth(), menu.getGameSpeed(), menu.getBotNumber());
                cards.add(board, BOARD_CARD_NAME);
                setState(STATE.GAME);
                break;
            case "Play Multiplayer":
                board = new Board(this, menu.getP1Name(), menu.getP2Name(), menu.getAreaHeight(), menu.getAreaWidth(), menu.getGameSpeed(), menu.getBotNumber());
                cards.add(board, BOARD_CARD_NAME);
                setState(STATE.GAME);
                break;
            case "End Game":
                setState(STATE.MENU);
                break;
        }
    }

}