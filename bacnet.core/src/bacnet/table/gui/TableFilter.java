package bacnet.table.gui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class TableFilter extends ViewerFilter {

    /**
     * 
     */
    private static final long serialVersionUID = -7621715116380436406L;
    private String searchString;

    public void setSearchText(String s) {
        // Search must be a substring of the existing value
        this.searchString = ".*" + s + ".*";
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (searchString == null || searchString.length() == 0) {
            return true;
        }
        String[] row = (String[]) element;
        for (String cell : row) {
            if (cell.matches(searchString))
                return true;
        }
        return false;
    }
}
