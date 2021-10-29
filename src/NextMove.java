

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;
import it.unical.mat.embasp.languages.asp.SymbolicConstant;

@Id("nextMove")
public class NextMove {
	@Param(0)
	private int x;
	@Param(1)
	private int y;
	@Param(2)
	private SymbolicConstant name;

	public NextMove() {
		this.x = -1;
		this.y = -1;
	}

	/**
	 * @return the x
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
	 * @return the y
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
	 * @return the name
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

}
