package bacnet.table.core;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import bacnet.datamodel.dataset.ExpressionMatrix;

public class TableViewerComparator extends ViewerComparator {
	/**
	* 
	*/
	private static final long serialVersionUID = -9021641281635843621L;
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = 1 - DESCENDING;

	public TableViewerComparator() {
		this.propertyIndex = 0;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		// System.out.println("compare");
		String[] row1 = (String[]) e1;
		String[] row2 = (String[]) e2;
		ExpressionMatrix matrix = (ExpressionMatrix) viewer.getData("matrix");
		int rc = 0;
		if (propertyIndex == 0) {
			rc = row1[0].compareTo(row2[0]);
		} else if (propertyIndex > 0 && propertyIndex < matrix.getHeaders().size() + 1) {
			if (row1[propertyIndex].equals(""))
				rc = 1;
			else if (row2[propertyIndex].equals(""))
				rc = -1;
			else {
				double value1 = Double.parseDouble(row1[propertyIndex]);
				double value2 = Double.parseDouble(row2[propertyIndex]);
				if (value1 == value2)
					rc = 0;
				else if (value1 < value2)
					rc = -1;
				else
					rc = 1;
			}
		} else {
			rc = row1[propertyIndex].compareTo(row2[propertyIndex]);
		}

		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

}
