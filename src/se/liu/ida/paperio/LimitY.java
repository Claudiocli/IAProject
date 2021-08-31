package se.liu.ida.paperio;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("limiteY")
public class LimitY {
	@Param(0)
	private int yMin;
	@Param(1)
	private int yMax;

	public LimitY() {
		yMin = 0;
		yMax = Board.getInstance().getAreaWidth();
	}

	/**
	 * @return the yMin
	 */
	public int getyMin() {
		return yMin;
	}

	/**
	 * @param yMin the yMin to set
	 */
	public void setyMin(int yMin) {
		this.yMin = yMin;
	}

	/**
	 * @return the yMax
	 */
	public int getyMax() {
		return yMax;
	}

	/**
	 * @param yMax the yMax to set
	 */
	public void setyMax(int yMax) {
		this.yMax = yMax;
	}

}
