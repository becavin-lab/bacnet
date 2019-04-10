package bacnet.table.core;

import java.io.Serializable;
import java.util.HashMap;

public class MatrixSize implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3440001575014228874L;

	private int widthRowNameDefault = 120;
	private int widthDefault = 120;
	private int heightDefault = 30;
	private HashMap<String, Integer> width = new HashMap<String, Integer>();
	private HashMap<String, Integer> height = new HashMap<String, Integer>();

	public MatrixSize() {

	}

	public int getWidthRowNameDefault() {
		return widthRowNameDefault;
	}

	public void setWidthRowNameDefault(int widthRowNameDefault) {
		this.widthRowNameDefault = widthRowNameDefault;
	}

	public int getWidthDefault() {
		return widthDefault;
	}

	public void setWidthDefault(int widthDefault) {
		this.widthDefault = widthDefault;
	}

	public int getHeightDefault() {
		return heightDefault;
	}

	public void setHeightDefault(int heightDefault) {
		this.heightDefault = heightDefault;
	}

	public HashMap<String, Integer> getWidth() {
		return width;
	}

	public void setWidth(HashMap<String, Integer> width) {
		this.width = width;
	}

	public HashMap<String, Integer> getHeight() {
		return height;
	}

	public void setHeight(HashMap<String, Integer> height) {
		this.height = height;
	}
}
