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
	public int getXMin() {
		return xMin;
	}

	/**
	 * @param xMin the xMin to set
	 */
	public void setXMin(int xMin) {
		this.xMin = xMin;
	}

	/**
	 * @return the xMax
	 */
	public int getXMax() {
		return xMax;
	}

	/**
	 * @param xMax the xMax to set
	 */
	public void setXMax(int xMax) {
		this.xMax = xMax;
	}

}
