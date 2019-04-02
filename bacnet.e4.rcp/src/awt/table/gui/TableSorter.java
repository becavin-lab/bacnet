package awt.table.gui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;





public class TableSorter extends ViewerSorter {
	private int propertyIndex;
	private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;

	private int direction = ASCENDING;

	public TableSorter() {
		this.propertyIndex = 0;
		direction = ASCENDING;
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
		String[] p1 = (String[]) e1;
		String[] p2 = (String[]) e2;
		int rc = 0;
		// test if it's a Double or not
		try{
			double d1 = Double.parseDouble(p1[propertyIndex]);
			double d2 = Double.parseDouble(p2[propertyIndex]);
			if(d1==d2) rc = 0;
			if(d1>d2) rc=1;
			if(d1<d2) rc=-1;
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}catch(Exception e){
			try{
				rc = p1[propertyIndex].compareTo(p2[propertyIndex]);
			}catch(Exception ezqd2){
				System.out.println("Cannot compare two values");
			}
		
			// If descending order, flip the direction
			if (direction == DESCENDING) {
			rc = -rc;
			}
			return rc;
		}
		
	}
}