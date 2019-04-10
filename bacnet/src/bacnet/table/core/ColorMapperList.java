package bacnet.table.core;

import java.util.TreeMap;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Shell;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.table.core.ColorMapper.TypeMapper;

public class ColorMapperList implements Cloneable {

	private TreeMap<TypeMapper, ColorMapper> colorMappers = new TreeMap<TypeMapper, ColorMapper>();

	public ColorMapperList() {
	}

	/**
	 * Create a list of ColorMapper from a matrix<br>
	 * Each column name corresponding to a TypeMapper will be associated to a
	 * ColorMapper
	 * 
	 * @param matrix
	 */
	public ColorMapperList(ExpressionMatrix matrix, Shell shell) {
		if (matrix != null) {
			for (String header : matrix.getHeaders()) {
				boolean typeFound = false;
				for (TypeMapper type : TypeMapper.values()) {
					if (header.startsWith(type.toString() + "_")) {
						ColorMapper colorMapper = colorMappers.get(type);
						if (colorMapper == null) {
							colorMapper = new ColorMapper(shell);
							colorMapper.setType(type);
							colorMappers.put(type, colorMapper);
						}
						colorMapper.getColumnNames().add(header);
						typeFound = true;
					}
				}
				if (!typeFound) {
					ColorMapper colorMapper = colorMappers.get(TypeMapper.OTHER);
					if (colorMapper == null) {
						colorMapper = new ColorMapper(shell);
						colorMapper.setType(TypeMapper.OTHER);
						colorMappers.put(TypeMapper.OTHER, colorMapper);
					}
					colorMapper.getColumnNames().add(header);
				}
			}
			for (TypeMapper type : colorMappers.keySet()) {
				colorMappers.get(type).setValues(matrix);
			}
		}
	}

	public ColorMapper getCorrespondingMapper(String header) {
		for (TypeMapper type : TypeMapper.values()) {
			if (header.contains(type.toString() + "_")) {
				return colorMappers.get(type);
			}
		}
		return colorMappers.get(TypeMapper.OTHER);

	}

	public ColorMapper getFirstMapper() {
		for (TypeMapper type : colorMappers.keySet()) {
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
		for (TypeMapper type : colorMappers.keySet()) {
			colorMappers.get(type).setFontText(font);
		}
	}

	public void setFontsDouble(Font font) {
		for (TypeMapper type : colorMappers.keySet()) {
			colorMappers.get(type).setFontDouble(font);
		}
	}

	@Override
	public ColorMapperList clone() {
		ColorMapperList cloned = new ColorMapperList();
		for (TypeMapper type : colorMappers.keySet()) {
			cloned.getColorMappers().put(type, colorMappers.get(type).clone());
		}
		return cloned;
	}

	/*
	 * GETTERs and SETTERs
	 */
	public TreeMap<TypeMapper, ColorMapper> getColorMappers() {
		return colorMappers;
	}

	public void setColorMappers(TreeMap<TypeMapper, ColorMapper> colorMappers) {
		this.colorMappers = colorMappers;
	}

}
