package bacnet.utils;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class StringColor {

	public String str = "";
	public Color[] colorForeground = new Color[0];
	public Color[] colorBackground = new Color[0];
	Display display;

	public StringColor() {
		display = Display.getDefault();
	}

	/**
	 * Add text colorF will be applied to the foreground colorB will be applied to
	 * the background
	 * 
	 * @param text
	 * @param colorF
	 * @param colorB
	 */
	public void add(String text, Color colorF, Color colorB) {
		str += text;
		for (int i = 0; i < text.length(); i++) {
			colorForeground = addElement(colorForeground, colorF);
			colorBackground = addElement(colorBackground, colorB);
		}
	}

	/**
	 * Add text Default colorization will be applied BLACK = Foreground WHITE =
	 * Background
	 * 
	 * @param text
	 * @param color
	 */
	public void add(String text) {
		add(text, BasicColor.BLACK, BasicColor.WHITE);
	}

	/**
	 * Add text The colorization will be applied on the FOREGROUND
	 * 
	 * @param text
	 * @param color of the foreground
	 */
	public void addF(String text, Color color) {
		add(text, color, BasicColor.WHITE);
	}

	/**
	 * Add text The colorization will be applied on the BACKGROUND
	 * 
	 * @param text
	 * @param color of the background
	 */
	public void addB(String text, Color color) {
		add(text, BasicColor.BLACK, color);
	}

	public Color[] addElement(Color[] vector, Color color) {
		Color[] newVector = new Color[vector.length + 1];
		for (int i = 0; i < vector.length; i++) {
			newVector[i] = vector[i];
		}
		newVector[vector.length] = color;
		return newVector;
	}

}
