package bacnet.utils;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class BasicColor {

    public static Display display = Display.getCurrent();

    // BLACK
    public static Color BLACK = new Color(display, 0, 0, 0);
    // Grey
    public static Color GREY = new Color(display, 127, 127, 127);
    // Grey
    public static Color LIGHTGREY = new Color(display, 245, 245, 245);
    // Grey
    public static Color LIGHTERGREY = new Color(display, 210, 210, 210);
    // YELLOW
    public static Color YELLOW = new Color(display, 255, 255, 0);
    // WHITE
    public static Color WHITE = new Color(display, 255, 255, 255);
    // BLUE
    public static Color BLUE = new Color(display, 0, 0, 255);
    // GREEN
    public static Color GREEN = new Color(display, 0, 150, 150);
    // RED
    public static Color RED = new Color(display, 255, 0, 0);
    // ORANGE
    public static Color ORANGE = new Color(display, 255, 127, 0);
    // Purple
    public static Color PURPLE = new Color(display, 128, 0, 128);
    // LIGHTBLUE
    public static Color LIGHTBLUE = new Color(display, 149, 224, 245);
    // CYAN
    public static Color CYAN = new Color(display, 0, 217, 217);


    public static Color BANNER_COLOR = new Color(display, 199, 255, 247);
    
    public static Color LIGHT_ONE = new Color(display, 202, 240, 248);
    public static Color DARK_ONE = new Color(display, 255, 255, 255);
    
    public static Color LIGHT_TWO = new Color(display, 173, 232, 244);
    public static Color DARK_TWO = new Color(display, 230, 250, 255);
    
    public static Color LIGHT_THREE = new Color(display, 144, 224, 239);
    public static Color DARK_THREE = new Color(display, 202, 240, 248);

    public static Color BROWN = new Color(display, 158, 107, 17);

    public static Color gradientColor(int gradient) {
        return new Color(display, 255, 127, gradient % 255);
    }

    /**
     * Very light color for the background of the software banner
     */
    // public static Color BANNER_BACKGROUND = new Color(display, 20, 95, 172);
    public static Color BANNER_BACKGROUND = new Color(display, 143, 179, 215);
    // public static Color BANNER_BACKGROUND = new Color(display, 211, 226, 253);

    /**
     * Color used for RNASEq when there is only one strand available = non directional RNASeq
     */
    public static Color RNASEQ_NOSTRAND = new Color(display, 236, 92, 77);

    /**
     * Light red color used for plus strand genes
     */
    public static Color REDLIGHT_GENE = new Color(display, 255, 229, 189);
    public static Color REDLIGHT_COV = new Color(display, 239, 172, 73);

    /**
     * Dark red color used for plus strand genes
     */
    public static Color REDDARK_GENE = new Color(display, 255, 229, 189);
    /**
     * Very dark red color used for plus strand genes surrounding
     */
    public static Color REDLINE_GENE = new Color(display, 122, 50, 52);

    /**
     * Light blue color used for plus strand genes
     */
    public static Color BLUELIGHT_GENE = new Color(display, 184, 232, 255);
    public static Color BLUELIGHT_COV = new Color(display, 79, 183, 232);

    /**
     * Dark blue color used for plus strand genes
     */
    public static Color BLUEDARK_GENE = new Color(display, 69, 77, 196);
    /**
     * Very dark blue color used for plus strand genes surrounding
     */
    public static Color BLUELINE_GENE = new Color(display, 28, 61, 113);

    /**
     * Light color used for Srna
     */
    public static Color LIGHT_SRNA = new Color(display, 204, 51, 204);
    /**
     * Dark color used for Srna
     */
    public static Color DARK_SRNA = new Color(display, 120, 30, 120);
    /**
     * Very dark color used for Srna surrounding
     */
    public static Color LINE_SRNA = new Color(display, 74, 19, 74);

    /**
     * Light color used for ASrna
     */
    public static Color LIGHT_ASRNA = new Color(display, 5, 211, 170);
    /**
     * Dark color used for ASrna
     */
    public static Color DARK_ASRNA = new Color(display, 5, 153, 123);
    /**
     * Very dark color used for ASrna surrounding
     */
    public static Color LINE_ASRNA = new Color(display, 5, 80, 63);
    /**
     * Light color used for NCrna
     */
    public static Color LIGHT_CISREG = new Color(display, 37, 185, 13);
    /**
     * Dark color used for NCrna
     */
    public static Color DARK_CISREG = new Color(display, 61, 132, 50);
    /**
     * Very dark color used for NCrna surrounding
     */
    public static Color LINE_CISREG = new Color(display, 44, 81, 38);

    /**
     * Light color used for NCrna
     */
    public static Color LIGHT_NCRNA = new Color(display, 255, 255, 73);
    /**
     * Dark color used for NCrna
     */
    public static Color DARK_NCRNA = new Color(display, 227, 227, 69);
    /**
     * Very dark color used for NCrna surrounding
     */
    public static Color LINE_NCRNA = new Color(display, 183, 183, 56);

    /**
     * Light color used for ASrna
     */
    public static Color LIGHT_OPERON = new Color(display, 254, 127, 0);
    /**
     * Dark color used for ASrna
     */
    public static Color DARK_OPERON = new Color(display, 214, 108, 1);
    /**
     * Very dark color used for NCrna surrounding
     */
    public static Color LINE_OPERON = new Color(display, 96, 48, 1);

    /**
     * red light color for nucleotide representation
     */
    public static Color REDLIGHT_NUCLEOTIDE = new Color(display, 239, 161, 161);
    /**
     * blue light color for nucleotide representation
     */
    public static Color BLUELIGHT_NUCLEOTIDE = new Color(display, 150, 183, 246);

    public static Color getColors(int index) {
        int modulo = index % 9;
        switch (modulo) {
            case 1:
                return RED;
            case 2:
                return BLUE;
            case 0:
                return GREEN;
            case 3:
                return CYAN;
            case 4:
                return ORANGE;
            case 5:
                return YELLOW;
            case 6:
                return LIGHTBLUE;
            case 7:
                return GREY;
            case 8:
                return BLACK;
        }
        return BLACK;
    }

    /**
     * Transform SWT color to AWT color
     * 
     * @param color
     * @return
     */
    public static java.awt.Color getAWTColor(Color color) {
        java.awt.Color awtColor = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
        return awtColor;
    }
}
