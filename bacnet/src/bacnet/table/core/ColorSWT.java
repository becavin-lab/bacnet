package bacnet.table.core;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;

public class ColorSWT implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3693115797571798995L;

	private int red;
	private int blue;
	private int green;

	public ColorSWT(int red, int blue, int green) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public ColorSWT(Color color) {
		this.red = color.getRed();
		this.green = color.getGreen();
		this.blue = color.getBlue();
	}

	public Color toColor(Device device) {

		return new Color(device, this.red, this.green, this.blue);
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

}
