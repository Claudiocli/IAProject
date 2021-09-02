package se.liu.ida.paperio;

import java.awt.event.KeyEvent;

import it.unical.mat.embasp.base.Callback;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.AnswerSet;
import it.unical.mat.embasp.languages.asp.AnswerSets;

public class PlayerUpdateCallback implements Callback	{

	private AIPlayer player;

	@Override
	public void callback(Output arg0) {
		AnswerSets answersets = (AnswerSets) arg0;
		boolean gotNextMove = false;
		for (AnswerSet a : answersets.getOptimalAnswerSets())  {
			try {
				for (Object obj : a.getAtoms()) {
					if (!(obj instanceof NextMove))
						continue;
					var nextMove = (NextMove) obj;
					if (nextMove.getX() == 0 && nextMove.getY() == -1)  {
						player.setCurrentDirection(Board.NORTH_DIRECTION);
						player.setNextKey(KeyEvent.VK_UP);
					}
					if (nextMove.getX() == 0 && nextMove.getY() == 1)  {
						player.setCurrentDirection(Board.SOUTH_DIRECTION);
						player.setNextKey(KeyEvent.VK_DOWN);
					}
					if (nextMove.getX() == -1 && nextMove.getY() == 0)  {
						player.setCurrentDirection(Board.WEST_DIRECTION);
						player.setNextKey(KeyEvent.VK_LEFT);
					}
					if (nextMove.getX() == 1 && nextMove.getY() == 0)  {
						player.setCurrentDirection(Board.EAST_DIRECTION);
						player.setNextKey(KeyEvent.VK_RIGHT);
					}
					gotNextMove = true;
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (gotNextMove)
				break;
		}
		player.updateD();
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(AIPlayer player) {
		this.player = player;
	}
	
}
