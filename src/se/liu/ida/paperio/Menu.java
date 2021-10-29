package se.liu.ida.paperio;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.GridLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Class responsible for displaying menu and receiving game settings. Uses an
 * actionListener to forward actions from Menu.
 */
public class Menu extends JPanel {

    private JTextField p1NameFld;
    private JTextField p2NameFld;
    private JSpinner areaHeightSpnr;
    private JSpinner areaWidthSpnr;
    private JSpinner gameSpeedSpnr;
    private JSpinner botNumberSpnr;
    private transient ActionListener actionListener;

    /**
     * Initializes a menu with actionListener responsible for reacting to game start
     * 
     * @param actionListener actionListener responsible for reacting to game start
     */
    Menu(ActionListener actionListener) {
        this.actionListener = actionListener;
        setBackground(Color.BLACK);

        GridLayout gridLayout = new GridLayout(8, 2);
        setLayout(gridLayout);

        addComponents();

    }

    /**
     * Adds and styles all components in menu
     */
    private void addComponents() {
        // Empty cells
        add(new JLabel(" "));
        add(new JLabel(" "));

        // Play buttons
        JButton playBtn = new JButton("Play Singleplayer");
        JButton playMultiBtn = new JButton("Play Multiplayer");

        JButton[] buttons = { playBtn, playMultiBtn };

        // Styles and add play buttons
        for (JButton button : buttons) {
            button.setSize(100, 26);
            button.addActionListener(actionListener);
            button.setFont(new Font("Monospaced", Font.PLAIN, 40));
            button.setBackground(Color.BLACK);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);

            add(button);
        }

        // Name labels
        JLabel p1name = new JLabel("Player 1 name:");
        JLabel p2name = new JLabel("Player 2 name:");

        JLabel[] labels = { p1name, p2name };

        // Style and add labels
        for (JLabel label : labels) {
            label.setFont(new Font("Monospaced", Font.PLAIN, 24));
            label.setForeground(Color.WHITE);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.BOTTOM);
            add(label);
        }
        
        ArrayList<String> names = new ArrayList<>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("pokemon_names.txt")));
			while (br.ready())
				names.add(br.readLine());
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
        Random r = new Random();
        // Name text fields
        p1NameFld = new JTextField(names.get(r.nextInt(names.size())));
        p2NameFld = new JTextField(names.get(r.nextInt(names.size())));

        JTextField[] textFields = { p1NameFld, p2NameFld };

        // Styles and adds name text fields
        for (JTextField textField : textFields) {
            textField.setFont(new Font("Monospaced", Font.PLAIN, 40));
            textField.setBackground(Color.BLACK);
            textField.setForeground(Color.gray.brighter());
            textField.addMouseListener(new FieldMouseListener(textField));
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setCaretColor(Color.WHITE);
            add(textField);
        }

        // Setting labels and spinners
        JLabel areaHeightLabel = new JLabel("Game area height:");
        areaHeightSpnr = new JSpinner(new SpinnerNumberModel(40, 25, 500, 5));
        JLabel areaWidthLabel = new JLabel("Game area width:");
        areaWidthSpnr = new JSpinner(new SpinnerNumberModel(40, 25, 500, 5));
        JLabel speedLabel = new JLabel("Game speed (1-5):");
        gameSpeedSpnr = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JLabel botNumberLabel = new JLabel("Number of bots:");
        botNumberSpnr = new JSpinner(new SpinnerNumberModel(1, 0, 25, 1));

        JLabel[] settingLabels = { areaHeightLabel, areaWidthLabel, speedLabel, botNumberLabel };
        JSpinner[] settingSpinners = { areaHeightSpnr, areaWidthSpnr, gameSpeedSpnr, botNumberSpnr };

        // Style setting labels
        for (JLabel label : settingLabels) {
            label.setFont(new Font("Monospaced", Font.PLAIN, 24));
            label.setForeground(Color.WHITE);
            label.setHorizontalAlignment(JLabel.RIGHT);
        }

        // Style setting spinners
        for (JSpinner spinner : settingSpinners) {
            JTextField textField = (JTextField) spinner.getEditor().getComponent(0);
            spinner.setFont(new Font("Monospaced", Font.PLAIN, 24));
            textField.setBackground(Color.BLACK);
            textField.setForeground(Color.WHITE);
            textField.setHorizontalAlignment(JTextField.LEFT);
        }

        JComponent[] settingComponents = { areaHeightLabel, areaHeightSpnr, areaWidthLabel, areaWidthSpnr, speedLabel,
                gameSpeedSpnr, botNumberLabel, botNumberSpnr };

        // Add setting labels and spinners
        for (JComponent component : settingComponents) {
            add(component);
        }

    }

    /**
     * Get player 1 name written in p1 name field
     * 
     * @return player 1 name written in p1 name field
     */
    public String getP1Name() {
        return p1NameFld.getText();
    }

    /**
     * Get player 2 name written in p1 name field
     * 
     * @return player 2 name written in p1 name field
     */
    public String getP2Name() {
        return p2NameFld.getText();
    }

    /**
     * Get game area height specified in area height spinner
     * 
     * @return Game area height specified in area height spinner
     */
    public int getAreaHeight() {
        return Integer.valueOf(((JTextField) areaHeightSpnr.getEditor().getComponent(0)).getText());
    }

    /**
     * Get game area width specified in area width spinner
     * 
     * @return Game area width specified in area width spinner
     */
    public int getAreaWidth() {
        return Integer.valueOf(((JTextField) areaWidthSpnr.getEditor().getComponent(0)).getText());
    }

    /**
     * Get game speed specified in game speed spinner
     * 
     * @return Game speed specified in game speed spinner
     */
    public int getGameSpeed() {
        return Integer.valueOf(((JTextField) gameSpeedSpnr.getEditor().getComponent(0)).getText());
    }

    /**
     * Get number of bots specified in bot number spinner
     * 
     * @return Number of bots specified in bot number spinner
     */
    public int getBotNumber() {
        return Integer.valueOf(((JTextField) botNumberSpnr.getEditor().getComponent(0)).getText());
    }

    /**
     * Mouse listener responsible for reacting on name text field clicks and
     * clearing placeholder
     */
    class FieldMouseListener implements MouseListener {

        private boolean changed = false;
        private JTextField textField;

        public FieldMouseListener(JTextField textField) {
            this.textField = textField;
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!changed) {
                textField.setText("");
                textField.setForeground(Color.WHITE);
            }
            changed = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

}
