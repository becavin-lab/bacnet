package bacnet.table.core;

import java.awt.Font;
import java.util.TreeMap;
import org.eclipse.swt.widgets.Shell;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.table.core.ColorMapperRCP.Type;


public class ColorMapperRCPList implements Cloneable {

    private TreeMap<Type, ColorMapperRCP> colorMappers = new TreeMap<Type, ColorMapperRCP>();

    public ColorMapperRCPList() {}

    public ColorMapperRCPList(ExpressionMatrix matrix, Shell shell) {
        for (String header : matrix.getHeaders()) {
            boolean typeFound = false;
            for (Type type : Type.values()) {
                if (header.startsWith(type.toString() + "_")) {
                    ColorMapperRCP colorMapper = colorMappers.get(type);
                    if (colorMapper == null) {
                        colorMapper = new ColorMapperRCP(shell);
                        colorMapper.setType(type);
                        colorMappers.put(type, colorMapper);
                    }
                    colorMapper.getColumnNames().add(header);

                    typeFound = true;
                }
            }
            if (!typeFound) {
                ColorMapperRCP colorMapper = colorMappers.get(Type.OTHER);
                if (colorMapper == null) {
                    colorMapper = new ColorMapperRCP(shell);
                    colorMapper.setType(Type.OTHER);
                    colorMappers.put(Type.OTHER, colorMapper);
                }
                colorMapper.getColumnNames().add(header);
                colorMapper.setValues(matrix);
            }
        }

        for (Type type : colorMappers.keySet()) {
            colorMappers.get(type).setValues(matrix);
        }

        System.out.println();
    }

    public ColorMapperRCP getCorrespondingMapper(String header) {
        for (Type type : Type.values()) {
            if (header.contains(type.toString() + "_")) {
                return colorMappers.get(type);
            }
        }
        return colorMappers.get(Type.OTHER);

    }

    public ColorMapperRCP getFirstMapper() {
        for (Type type : colorMappers.keySet()) {
            return colorMappers.get(type);
        }
        System.out.println("No ColorMapper found");
        return null;
    }

    public void setAllFonts(Font font) {
        setFontsDouble(font);
        setFontsString(font);
    }


    public void setFontsString(Font font) {
        for (Type type : colorMappers.keySet()) {
            colorMappers.get(type).setFontString(font);
        }
    }

    public void setFontsDouble(Font font) {
        for (Type type : colorMappers.keySet()) {
            colorMappers.get(type).setFontDouble(font);
        }
    }

    @Override
    public ColorMapperRCPList clone() {
        ColorMapperRCPList cloned = new ColorMapperRCPList();
        for (Type type : colorMappers.keySet()) {
            cloned.getColorMappers().put(type, colorMappers.get(type).clone());
        }
        return cloned;
    }



    /*
     * GETTERs and SETTERs
     */
    public TreeMap<Type, ColorMapperRCP> getColorMappers() {
        return colorMappers;
    }

    public void setColorMappers(TreeMap<Type, ColorMapperRCP> colorMappers) {
        this.colorMappers = colorMappers;
    }


}
