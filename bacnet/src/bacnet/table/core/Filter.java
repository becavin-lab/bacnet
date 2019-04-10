package bacnet.table.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import bacnet.datamodel.dataset.ExpressionMatrix;

public class Filter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6834197254054420424L;

	private String name = "Filter";
	private double cutOff1;
	private double cutOff2 = -1000000;
	private boolean filterColumn = true;
	private String tableElementName = "";
	private TypeFilter typeFilter;
	private transient ExpressionMatrix matrix;
	private transient ArrayList<String> excludeRow = new ArrayList<String>();
	private transient ArrayList<String> excludeColumn = new ArrayList<String>();

	public enum TypeFilter {
		SUPERIOR, SUPERIOR_ABS, INFERIOR, INFERIOR_ABS, BETWEEN
	}

	public static String[] TYPE_REPRESENTATION = { "a < Values", "a < |Values|", "a > Values", "a > |Values|",
			"a < Values < b" };

	public Filter() {
	}

	public Filter(ExpressionMatrix matrix) {
		this.matrix = matrix;
		setDefault();
	}

	public void setDefault() {
		cutOff1 = 0;
		typeFilter = TypeFilter.SUPERIOR;
		filterColumn = true;
	}

	public boolean filterValue(double value) {
		switch (typeFilter) {
		case SUPERIOR:
			if (value > cutOff1)
				return true;
			else
				return false;
		case SUPERIOR_ABS:
			if (Math.abs(value) > cutOff1)
				return true;
			else
				return false;
		case INFERIOR:
			if (value < cutOff1)
				return true;
			else
				return false;
		case INFERIOR_ABS:
			if (Math.abs(value) < cutOff1)
				return true;
			else
				return false;
		case BETWEEN:
			if (cutOff1 < value && value < cutOff2)
				return true;
			return false;
		}
		return false;
	}

	public double getCutOff1() {
		return cutOff1;
	}

	public void setCutOff1(double cutOff1) {
		this.cutOff1 = cutOff1;
	}

	public double getCutOff2() {
		return cutOff2;
	}

	public void setCutOff2(double cutOff2) {
		this.cutOff2 = cutOff2;
	}

	public void createInferiorFilter(double inf) {
		this.typeFilter = TypeFilter.INFERIOR;
		this.cutOff1 = inf;
	}

	public void createInferiorAbsFilter(double inf) {
		this.typeFilter = TypeFilter.INFERIOR_ABS;
		this.cutOff1 = inf;
	}

	public void createSuperiorFilter(double inf, double sup) {
		this.typeFilter = TypeFilter.BETWEEN;
		this.cutOff1 = inf;
		this.cutOff2 = sup;
	}

	public void createSuperiorFilter(double sup) {
		this.typeFilter = TypeFilter.SUPERIOR_ABS;
		this.cutOff1 = sup;
	}

	public void createSuperiorAbsFilter(double sup) {
		this.typeFilter = TypeFilter.SUPERIOR_ABS;
		this.cutOff1 = sup;
	}

	@Override
	public String toString() {
		return "Type: " + typeFilter + " inf: " + cutOff1 + " sup: " + cutOff2 + " column? " + filterColumn + " name: "
				+ tableElementName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFilterColumn() {
		return filterColumn;
	}

	public void setFilterColumn(boolean filterColumn) {
		this.filterColumn = filterColumn;
	}

	public ExpressionMatrix getMatrix() {
		return matrix;
	}

	public void setMatrix(ExpressionMatrix matrix) {
		this.matrix = matrix;
	}

	public String getTableElementName() {
		return tableElementName;
	}

	public void setTableElementName(String tableElementName) {
		this.tableElementName = tableElementName;
	}

	public ArrayList<String> getExcludeRow() {
		return excludeRow;
	}

	public void setExcludeRow(ArrayList<String> excludeRow) {
		this.excludeRow = excludeRow;
	}

	public ArrayList<String> getExcludeColumn() {
		return excludeColumn;
	}

	public void setExcludeColumn(ArrayList<String> excludeColumn) {
		this.excludeColumn = excludeColumn;
	}

	@Override
	public Filter clone() {
		Filter cloned = new Filter(matrix);
		cloned.setCutOff1(cutOff1);
		cloned.setCutOff2(cutOff2);
		cloned.setExcludeColumn((ArrayList<String>) excludeColumn.clone());
		cloned.setExcludeRow((ArrayList<String>) excludeRow.clone());
		cloned.setFilterColumn(filterColumn);
		cloned.setName(name);
		cloned.setTableElementName(tableElementName);
		cloned.setTypeFilter(typeFilter);
		return cloned;
	}

	/**
	 * Prompt for a filename, and load a scribble from that file. Read compressed,
	 * serialized data with a FileInputStream. Uncompress that data with a
	 * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream.
	 * Replace current data with new data, and redraw everything.
	 */
	public static Filter load(String fileName) {
		if (fileName != null) { // If user didn't click "Cancel".
			try {
				// Create necessary input streams
				FileInputStream fis = new FileInputStream(fileName); // Read from file
				GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
				ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
				// Read in an object. It should be a vector of scribbles
				Filter filter = (Filter) in.readObject();
				in.close(); // Close the stream.
				return filter;
			}
			// Print out exceptions. We should really display them in a dialog...
			catch (Exception e) {
				System.out.println(e);
			}
		}
		return null;
	}

	/**
	 * Prompt the user for a filename, and save the scribble in that file. Serialize
	 * the vector of lines with an ObjectOutputStream. Compress the serialized
	 * objects with a GZIPOutputStream. Write the compressed, serialized data to a
	 * file with a FileOutputStream. Don't forget to flush and close the stream.
	 */
	public void save(String fileName) {
		// Create a file dialog to query the user for a filename.
		if (fileName != null) { // If user didn't click "Cancel".
			try {
				// Create the necessary output streams to save the scribble.
				FileOutputStream fos = new FileOutputStream(fileName);
				// Save to file
				GZIPOutputStream gzos = new GZIPOutputStream(fos);
				// Compressed
				ObjectOutputStream out = new ObjectOutputStream(gzos);
				// Save objects
				out.writeObject(this); // Write the entire Vector of scribbles
				out.flush(); // Always flush the output.
				out.close(); // And close the stream.
			}
			// Print out exceptions. We should really display them in a dialog...
			catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	public TypeFilter getTypeFilter() {
		return typeFilter;
	}

	public void setTypeFilter(TypeFilter typeFilter) {
		this.typeFilter = typeFilter;
	}

}
