package bacnet.table.core;

import java.util.ArrayList;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import bacnet.datamodel.dataset.OmicsData.ColNames;

public class BioConditionComparator extends ViewerComparator {
    /**
    * 
    */
    private static final long serialVersionUID = 4101481773521242366L;
    private int propertyIndex;
    private ArrayList<String> columnNames = new ArrayList<>();
    private static final int DESCENDING = 1;
    private int direction = 1 - DESCENDING;

    public BioConditionComparator(ArrayList<String> columnNames) {
        this.propertyIndex = 0;
        this.columnNames = columnNames;
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
        int rc = 0;
        String[] bioCond1 = (String[]) e1;
        String[] bioCond2 = (String[]) e2;
        String value1 = bioCond1[propertyIndex];
        String value2 = bioCond2[propertyIndex];
        /*
         * Check columnname to order the column
         */
        String columnName = columnNames.get(propertyIndex);
        if (columnName.equals("Start") || columnName.equals("End")) {
            Integer value1Int = Integer.parseInt(value1);
            Integer value2Int = Integer.parseInt(value2);
            rc = value1Int.compareTo(value2Int);
        } else if (columnName.equals("Log2FoldChange") || columnName.equals(ColNames.LOGFC + "")
                || columnName.equals(ColNames.PVALUE + "") || columnName.equals(ColNames.FC + "")) {

        	Double value1Int = Double.parseDouble(value1);
            Double value2Int = Double.parseDouble(value2);
            rc = value1Int.compareTo(value2Int);
        } else {
            rc = value1.compareTo(value2);
        }
        // If descending order, flip the direction
        if (direction == DESCENDING) {
            rc = -rc;
        }
        return rc;
    }

}
