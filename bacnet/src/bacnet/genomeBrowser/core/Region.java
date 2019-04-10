package bacnet.genomeBrowser.core;

import java.io.Serializable;

public class Region implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1103556086987455111L;
	private int x1 = 0;
	private int y1 = 0;
	private int x2 = 0;
	private int y2 = 0;

	private int width = 0;
	private int height = 0;

	private int middleH = 0;
	private int middleV = 0;

	public boolean regionChanged = false;

	public Region() {
	}

	public Region(int x1, int x2) {
		if (x2 < x1)
			System.err.println("Error X2 < X1 : " + x2 + "<" + x1);
		this.setX1(x1);
		this.setX2(x2);
		calculateSize();
		calculateMiddle();
	}

	public void calculateSize() {
		this.setWidth(getX2() - getX1());
		this.setHeight(getY2() - getY1());
	}

	public void calculateMiddle() {
		this.setMiddleH(getX1() + (getX2() - getX1()) / 2);
		this.setMiddleV(getY1() + (getY2() - getY1()) / 2);
	}

	public void zoomRegion(int newWidth) {
		x1 = middleH - newWidth / 2;
		x2 = middleH + newWidth / 2;
		width = newWidth;
	}

	public void moveHorizontally(int selection) {
		int increment = selection - middleH;
		x1 += increment;
		x2 += increment;
		middleH = selection;
		regionChanged = true;
		// System.out.println("increment: "+increment+" pos: "+selection);
	}

	/**
	 * This method is run after a zoom or a moveHorizontally it move the region to
	 * be sure that x1>0 and x2<lastIndex
	 * 
	 * @param lastIndex
	 */
	public void validateRegion(int lastIndex) {
		if (x1 < 0) {
			x2 = x2 - x1;
			x1 = 0;
		}
		if (x2 > lastIndex) {
			x1 = x1 - (x2 - lastIndex);
			x2 = lastIndex;
		}
		calculateSize();
		calculateMiddle();
	}

	// **************************************
	// ******* Getters and Setters ******
	// **************************************
	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getY1() {
		return y1;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public int getY2() {
		return y2;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getMiddleH() {
		return middleH;
	}

	public void setMiddleH(int middleH) {
		this.middleH = middleH;
	}

	public int getMiddleV() {
		return middleV;
	}

	public void setMiddleV(int middleV) {
		this.middleV = middleV;
	}

	@Override
	public String toString() {
		String ret = getX1() + "-" + getX2() + ":" + getWidth() + " mid " + middleH + "\n";
		return ret;
	}

	/**
	 * Clone the object
	 */
	@Override
	public Region clone() {
		Region o = null;
		try {
			o = (Region) super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}
		return o;
	}

}
