package bacnet.table.gui;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import bacnet.table.core.Filter;
import bacnet.table.core.Filter.TypeFilter;
import bacnet.table.core.FilterList;

/**
 * Dialog displaying the list of <code>Filters</code> currently used on the
 * <code>ExpressionMatrix</code>
 * 
 * @see <code>HeatMapView</code>
 * @author UIBC
 *
 */
public class FilterExpressionMatrixDialog extends TitleAreaDialog implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 8526381730648269264L;
    private FilterList filters;
    private List listFilter;
    private Button btnCreateNew;
    private Button btnEdit;
    private Button btnRemove;
    private Label label;
    private Composite composite_1;
    private Label lblName;
    private Label lblType;
    private Label lblRoworcolumn;
    private Label lblElementname;

    private Shell shell;

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public FilterExpressionMatrixDialog(FilterList filters, Shell shell) {
        super(shell);
        this.shell = shell;
        this.filters = filters;
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle("Filters applied to the table");
        setMessage("Add or remove different filters");
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label lblListOfFilters = new Label(container, SWT.NONE);
        lblListOfFilters.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblListOfFilters.setText("List of filters");
        new Label(container, SWT.NONE);

        listFilter = new List(container, SWT.BORDER | SWT.H_SCROLL);
        GridData gd_listFilter = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 7);
        gd_listFilter.heightHint = 312;
        gd_listFilter.widthHint = 182;
        listFilter.setLayoutData(gd_listFilter);
        listFilter.addSelectionListener(this);

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new GridLayout(5, false));
        btnEdit = new Button(composite, SWT.NONE);
        btnEdit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnEdit.setText("Edit");
        btnEdit.addSelectionListener(this);
        btnRemove = new Button(composite, SWT.NONE);
        btnRemove.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnRemove.setText("Remove");
        btnRemove.addSelectionListener(this);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        btnCreateNew = new Button(composite, SWT.NONE);
        btnCreateNew.setText("New");
        btnCreateNew.addSelectionListener(this);

        listFilter.setItems(filters.getFilters().keySet().toArray(new String[0]));

        label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gd_label = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_label.widthHint = 219;
        label.setLayoutData(gd_label);
        new Label(container, SWT.NONE);

        Label lblFilterDescription = new Label(container, SWT.NONE);
        lblFilterDescription.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblFilterDescription.setText("Description");

        composite_1 = new Composite(container, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        composite_1.setLayout(new GridLayout(1, false));

        lblName = new Label(composite_1, SWT.NONE);
        lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblType = new Label(composite_1, SWT.NONE);
        lblType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblRoworcolumn = new Label(composite_1, SWT.NONE);
        lblRoworcolumn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblElementname = new Label(composite_1, SWT.NONE);
        lblElementname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        return area;
    }

    private void updateLabel(Filter filter) {
        lblName.setText(filter.getName());

        if (filter.getTypeFilter() == TypeFilter.BETWEEN) {
            lblType.setText("The filter is: " + Filter.TYPE_REPRESENTATION[filter.getTypeFilter().ordinal()]
                    .replaceFirst("a", filter.getCutOff1() + "").replaceFirst("b", filter.getCutOff2() + ""));
        } else {
            lblType.setText("The filter is: " + Filter.TYPE_REPRESENTATION[filter.getTypeFilter().ordinal()]
                    .replaceFirst("a", filter.getCutOff1() + ""));
        }

        if (filter.isFilterColumn()) {
            lblRoworcolumn.setText("it will be apply on the column:");
        } else {
            lblRoworcolumn.setText("it will be apply on the row:");
        }

        lblElementname.setText(filter.getTableElementName());
    }

    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Ok", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
    }

    @Override
    public void okPressed() {
        filters.updateExclude();
        this.close();
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(505, 527);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnCreateNew) {
            Filter filter = new Filter(filters.getMatrix());
            FilterDialog dialog = new FilterDialog(filter, shell);
            int ok = dialog.open();
            if (ok == Status.OK) {
                filters.getFilters().put(filter.getName(), filter);
                listFilter.add(filter.getName());
            }
        } else if (e.getSource() == btnEdit) {
            if (listFilter.getSelection().length != 0) {
                Filter filter = filters.getFilters().get(listFilter.getSelection()[0]);
                Filter filterClone = filter.clone();
                FilterDialog dialog = new FilterDialog(filterClone, shell);
                int ok = dialog.open();
                if (ok == Status.OK) {
                    filters.getFilters().remove(filter.getName());
                    filters.getFilters().put(filterClone.getName(), filterClone);
                    listFilter.setItems(filters.getFilters().keySet().toArray(new String[0]));
                }
            }
        } else if (e.getSource() == btnRemove) {
            if (listFilter.getSelection().length != 0) {
                Filter filter = filters.getFilters().get(listFilter.getSelection()[0]);
                filters.getFilters().remove(filter.getName());
                listFilter.setItems(filters.getFilters().keySet().toArray(new String[0]));
            }

        } else if (e.getSource() == listFilter) {
            if (listFilter.getSelection().length != 0) {
                Filter filter = filters.getFilters().get(listFilter.getSelection()[0]);
                updateLabel(filter);
            }
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }
}
