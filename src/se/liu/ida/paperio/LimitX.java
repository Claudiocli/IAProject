package se.liu.ida.paperio;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("limiteX")
public class LimitX {
	@Param(0)
	private int xMin;
	@Param(1)
	private int xMax;

	public LimitX() {
		this.xMin = 0;
		this.xMax = Board.getInstance().getAreaWidth();
	}

	/**
	 * @return the xMin
	 */
	public int getxMin() {
		return xMin;
	}

	/**
	 * @param xMin the xMin to set
	 */
	public void setxMin(int xMin) {
		this.xMin = xMin;
	}

	/**
	 * @return the xMax
	 */
	public int getxMax() {
		return xMax;
	}

	/**
	 * @param xMax the xMax to set
	 */
	public void setxMax(int xMax) {
		this.xMax = xMax;
	}

}
