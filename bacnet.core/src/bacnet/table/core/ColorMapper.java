package bacnet.table.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.BasicColor;
import bacnet.utils.VectorUtils;

public class ColorMapper implements Serializable, Cloneable {

    private static final long serialVersionUID = 26986650775300526L;

    public enum TypeMapper {
        LOGFC, FC, STAT, STDEV, CORR, OTHER
    }

    public transient Display display;
    public boolean displayValues = false;
    private transient double[] values;

    private transient TypeMapper type = TypeMapper.OTHER;
    private TreeSet<String> columnNames = new TreeSet<String>();

    private transient double min;
    private transient double max;
    private transient double mean;
    private transient double median;
    private transient double var;
    private transient double deviation;
    private transient int nbElements;

    private double centerPos;
    private double minPos;
    private double maxPos;
    private double midLeftPos;
    private double midRightPos;

    private transient Color centerColor;
    private transient Color minColor;
    private transient Color maxColor;
    private transient Color midLeftColor;
    private transient Color midRightColor;
    private transient Color rowNameCellColor;
    private transient Color rowNameTextColor;
    private transient Color textColor;

    private Font fontRowName;
    private Font fontText;
    private Font fontDouble;

    // ********** attributes only used for saving Color during serialization
    private ColorSWT centerColorSWT;
    private ColorSWT minColorSWT;
    private ColorSWT maxColorSWT;
    private ColorSWT midLeftColorSWT;
    private ColorSWT midRightColorSWT;
    private ColorSWT headerColorSWT;
    private ColorSWT textHeaderColorSWT;
    private ColorSWT textColorSWT;

    public ColorMapper(Shell shell) {
        display = shell.getDisplay();
        setDefaultColor();
        setDefaultFont();
    }

    public Color parseColor(double value) {
        // find between which bound this value is
        TreeMap<Double, Color> valuesMap = new TreeMap<Double, Color>();
        valuesMap.put(this.minPos, minColor);
        valuesMap.put(midLeftPos, midLeftColor);
        valuesMap.put(centerPos, centerColor);
        valuesMap.put(midRightPos, midRightColor);
        valuesMap.put(maxPos, maxColor);
        Iterator<Double> itr = valuesMap.keySet().iterator();
        double previousValue = itr.next();
        if (value < previousValue)
            return valuesMap.get(previousValue);
        while (itr.hasNext()) {
            double nextValue = itr.next();
            if (value < nextValue) {
                return getColorBetween(value, previousValue, nextValue, valuesMap.get(previousValue),
                        valuesMap.get(nextValue));
            }
            previousValue = nextValue;
        }
        return valuesMap.get(previousValue);
    }

    public Color getColorBetween(double value, double left, double right, Color leftColor, Color rightColor) {
        double size = right - left;
        double position = value - left;
        position = position / size;

        int redPos = leftColor.getRed() + (int) ((rightColor.getRed() - leftColor.getRed()) * position);
        int greenPos = leftColor.getGreen() + (int) ((rightColor.getGreen() - leftColor.getGreen()) * position);
        int bluePos = leftColor.getBlue() + (int) ((rightColor.getBlue() - leftColor.getBlue()) * position);

        return new Color(display, redPos, greenPos, bluePos);
    }

    public void setValues(ExpressionMatrix exprMatrix) {
        ArrayList<Double> valuesList = new ArrayList<Double>();
        double[][] valuesTemp = exprMatrix.getValues();
        if (valuesTemp.length != 0) {
            for (int j = 0; j < valuesTemp[0].length; j++) {
                if (this.getColumnNames().contains(exprMatrix.getHeader(j))) {
                    for (int i = 0; i < valuesTemp.length; i++) {
                        double value = valuesTemp[i][j];
                        if (!(value == ExpressionMatrix.MISSING_VALUE))
                            valuesList.add(value);
                    }
                }
            }
        }
        values = new double[valuesList.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = valuesList.get(i);
        }

        setStat();
        if (type.equals(TypeMapper.STDEV)) {
            setDefaultPos();
            setStdevDataColor();
        } else if (type.equals(TypeMapper.STAT)) {
            min = 0;
            max = 1;
            setStatMapper();
        } else {
            setDefaultPos();
        }
    }

    public void setDefault() {
        setDefaultPos();
        setDefaultColor();
        displayValues = false;
    }

    public void setDefaultPos() {
        centerPos = mean;
        minPos = min;
        maxPos = max;
        midLeftPos = minPos / 2;
        midRightPos = maxPos / 2;
    }

    public void setDefaultColor() {
        centerColor = new Color(display, 255, 255, 255);
        maxColor = new Color(display, 27,120,55);
        minColor = new Color(display, 118,42,131);
        midRightColor = new Color(display, 90,174,97);
        midLeftColor = new Color(display, 153,112,171);
        rowNameCellColor = new Color(display, 240, 240, 240);
        rowNameTextColor = new Color(display, 0, 0, 0);
        textColor = new Color(display, 0, 0, 0);
    }

    public void setDefaultFont() {
        fontRowName = SWTResourceManager.getBodyFont(SWT.BOLD);

        fontText = SWTResourceManager.getBodyFont(SWT.NORMAL);

        fontDouble = SWTResourceManager.getBodyFont(SWT.NORMAL);

    }

    public void setStat() {
        if (values.length == 0) {
            setStatDataValues();
        } else {
            min = VectorUtils.min(values);
            max = VectorUtils.max(values);
            mean = VectorUtils.mean(values);
            median = VectorUtils.median(values);
            nbElements = values.length;
            var = VectorUtils.variance(values);
            deviation = VectorUtils.deviation(values);
        }
    }

    public double[] getValues() {
        return values;
    }

    public Color getInocuaColor(double value) {
        int type = (int) value;
        switch (type) {
            case 0:
                return BasicColor.GREY;
            case 1:
                return BasicColor.YELLOW;
            case 2:
                return BasicColor.BLUE;
            case 3:
                return BasicColor.GREEN;
            default:
                return BasicColor.GREEN;
        }
    }

    /**
     * Set parameters corresponding to stat data i.e. value from 0 to 1
     * 
     * @return
     */
    public void setStatMapper() {
        setStatDataColor();
        setStatDataValues();
    }

    public void setStatDataValues() {

        minPos = 0;
        midLeftPos = 0.05;
        centerPos = 0.1;
        midRightPos = 0.2;
        maxPos = 1;

        min = 0;
        max = 1;
        mean = 0.5;
        median = 0.5;
        nbElements = 1;
        var = 1;
        deviation = 1;
    }

    public void setStatDataColor() {
        centerColor = new Color(display, 0, 0, 120);
        minColor = new Color(display, 0, 0, 255);
        maxColor = new Color(display, 0, 0, 0);
        midLeftColor = new Color(display, 0, 0, 185);
        midRightColor = new Color(display, 0, 0, 65);
        rowNameCellColor = new Color(display, 200, 200, 200);
        rowNameTextColor = new Color(display, 0, 0, 0);
        textColor = new Color(display, 255, 255, 255);
    }

    public void setStdevDataColor() {
        centerColor = new Color(display, 147, 73, 0);
        minColor = new Color(display, 0, 0, 0);
        maxColor = new Color(display, 255, 0, 0);
        midLeftColor = new Color(display, 0, 0, 0);
        midRightColor = new Color(display, 255, 128, 0);
        rowNameCellColor = new Color(display, 200, 200, 200);
        rowNameTextColor = new Color(display, 0, 0, 0);
        textColor = new Color(display, 255, 255, 255);
    }

    public static void setColorMapperValues(ArrayList<ExpressionMatrix> matrices) {
        // go through matrices and see what kind of colorMapper to display
    }

    @Override
    public ColorMapper clone() {
        ColorMapper cloned;
        try {
            cloned = (ColorMapper) super.clone();

            // Color does not implement clone so we need to create a copy by hand
            cloned.setCenterColor(cloneColor(this.centerColor));
            cloned.setRowNameCellColor(cloneColor(this.rowNameCellColor));
            cloned.setMaxColor(cloneColor(this.maxColor));
            cloned.setMinColor(cloneColor(this.minColor));
            cloned.setMidLeftColor(cloneColor(this.midLeftColor));
            cloned.setMidRightColor(cloneColor(this.midRightColor));
            cloned.setTextColor(cloneColor(this.textColor));
            cloned.setRowNameTextColor(cloneColor(this.rowNameTextColor));
            return cloned;
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private Color cloneColor(Color color) {
        Color newColor = new Color(display, color.getRed(), color.getGreen(), color.getBlue());
        return newColor;
    }

    public static ColorMapper load(String fileName, Shell shell) {
        if (fileName != null) {
            try {
                // Create necessary input streams
                FileInputStream fis = new FileInputStream(fileName); // Read from file
                GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
                ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
                // Read in an object. It should be a vector of scribbles
                ColorMapper colorMapper = (ColorMapper) in.readObject();
                in.close(); // Close the stream.

                // Set SWT.Color which are not serializable
                Display display = shell.getDisplay();
                colorMapper.setCenterColor(colorMapper.getCenterColorSWT().toColor(display));
                colorMapper.setMinColor(colorMapper.getMinColorSWT().toColor(display));
                colorMapper.setMaxColor(colorMapper.getMaxColorSWT().toColor(display));
                colorMapper.setMidLeftColor(colorMapper.getMidLeftColorSWT().toColor(display));
                colorMapper.setMidRightColor(colorMapper.getMidRightColorSWT().toColor(display));
                colorMapper.setRowNameCellColor(colorMapper.getHeaderColorSWT().toColor(display));
                colorMapper.setRowNameTextColor(colorMapper.getTextHeaderColorSWT().toColor(display));
                colorMapper.setTextColor(colorMapper.getTextColorSWT().toColor(display));

                return colorMapper;
            }
            // Print out exceptions. We should really display them in a dialog...
            catch (Exception e) {
                System.out.println(e);
            }
        }
        return null;
    }

    public void save(String fileName) {
        if (fileName != null) {
            try {
                // Update all ColorSWT, as SWT.Color is not serializable
                centerColorSWT = new ColorSWT(centerColor);
                minColorSWT = new ColorSWT(minColor);
                maxColorSWT = new ColorSWT(maxColor);
                midLeftColorSWT = new ColorSWT(midLeftColor);
                midRightColorSWT = new ColorSWT(midRightColor);
                headerColorSWT = new ColorSWT(rowNameCellColor);
                textHeaderColorSWT = new ColorSWT(rowNameTextColor);
                textColorSWT = new ColorSWT(textColor);

                // Create the necessary output streams to save the scribble.

                FileOutputStream fos = new FileOutputStream(fileName);
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                ObjectOutputStream out = new ObjectOutputStream(gzos);
                out.writeObject(this); // Write the entire Vector of scribbles
                out.flush(); // Always flush the output.
                out.close(); // And close the stream.
                System.out.println("ColorMapper saved in: " + fileName);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    /*************************************************
     * GETTERS and SETTERS
     */
    public TypeMapper getType() {
        return type;
    }

    public TreeSet<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(TreeSet<String> columnNames) {
        this.columnNames = columnNames;
    }

    public void setType(TypeMapper type) {
        this.type = type;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getVar() {
        return var;
    }

    public void setVar(double var) {
        this.var = var;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public int getNbElements() {
        return nbElements;
    }

    public void setNbElements(int nbElements) {
        this.nbElements = nbElements;
    }

    public double getCenterPos() {
        return centerPos;
    }

    public void setCenterPos(double centerPos) {
        this.centerPos = centerPos;
    }

    public double getMinPos() {
        return minPos;
    }

    public void setMinPos(double minPos) {
        this.minPos = minPos;
    }

    public double getMaxPos() {
        return maxPos;
    }

    public void setMaxPos(double maxPos) {
        this.maxPos = maxPos;
    }

    public double getMidLeftPos() {
        return midLeftPos;
    }

    public void setMidLeftPos(double midLeftPos) {
        this.midLeftPos = midLeftPos;
    }

    public double getMidRightPos() {
        return midRightPos;
    }

    public void setMidRightPos(double midRightPos) {
        this.midRightPos = midRightPos;
    }

    public Color getCenterColor() {
        return centerColor;
    }

    public void setCenterColor(Color centerColor) {
        this.centerColor = centerColor;
    }

    public Color getMinColor() {
        return minColor;
    }

    public void setMinColor(Color minColor) {
        this.minColor = minColor;
    }

    public Color getMaxColor() {
        return maxColor;
    }

    public void setMaxColor(Color maxColor) {
        this.maxColor = maxColor;
    }

    public Color getMidLeftColor() {
        return midLeftColor;
    }

    public void setMidLeftColor(Color midLeftColor) {
        this.midLeftColor = midLeftColor;
    }

    public Color getMidRightColor() {
        return midRightColor;
    }

    public void setMidRightColor(Color midRightColor) {
        this.midRightColor = midRightColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Font getFontDouble() {
        return fontDouble;
    }

    public void setFontDouble(Font fontDouble) {
        this.fontDouble = fontDouble;
    }

    public ColorSWT getCenterColorSWT() {
        return centerColorSWT;
    }

    public void setCenterColorSWT(ColorSWT centerColorSWT) {
        this.centerColorSWT = centerColorSWT;
    }

    public ColorSWT getMinColorSWT() {
        return minColorSWT;
    }

    public void setMinColorSWT(ColorSWT minColorSWT) {
        this.minColorSWT = minColorSWT;
    }

    public ColorSWT getMaxColorSWT() {
        return maxColorSWT;
    }

    public void setMaxColorSWT(ColorSWT maxColorSWT) {
        this.maxColorSWT = maxColorSWT;
    }

    public ColorSWT getMidLeftColorSWT() {
        return midLeftColorSWT;
    }

    public void setMidLeftColorSWT(ColorSWT midLeftColorSWT) {
        this.midLeftColorSWT = midLeftColorSWT;
    }

    public ColorSWT getMidRightColorSWT() {
        return midRightColorSWT;
    }

    public void setMidRightColorSWT(ColorSWT midRightColorSWT) {
        this.midRightColorSWT = midRightColorSWT;
    }

    public ColorSWT getHeaderColorSWT() {
        return headerColorSWT;
    }

    public void setHeaderColorSWT(ColorSWT headerColorSWT) {
        this.headerColorSWT = headerColorSWT;
    }

    public ColorSWT getTextHeaderColorSWT() {
        return textHeaderColorSWT;
    }

    public void setTextHeaderColorSWT(ColorSWT textHeaderColorSWT) {
        this.textHeaderColorSWT = textHeaderColorSWT;
    }

    public ColorSWT getTextColorSWT() {
        return textColorSWT;
    }

    public void setTextColorSWT(ColorSWT textColorSWT) {
        this.textColorSWT = textColorSWT;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public Color getRowNameCellColor() {
        return rowNameCellColor;
    }

    public void setRowNameCellColor(Color rowNameCellColor) {
        this.rowNameCellColor = rowNameCellColor;
    }

    public Color getRowNameTextColor() {
        return rowNameTextColor;
    }

    public void setRowNameTextColor(Color rowNameTextColor) {
        this.rowNameTextColor = rowNameTextColor;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Font getFontRowName() {
        return fontRowName;
    }

    public void setFontRowName(Font fontRowName) {
        this.fontRowName = fontRowName;
    }

    public Font getFontText() {
        return fontText;
    }

    public void setFontText(Font fontText) {
        this.fontText = fontText;
    }
}
