package bacnet.table.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.Filter;
import bacnet.table.core.Filter.TypeFilter;

/**
 * Dialog for managing <code>Filter</code> for <code>ExpressionMatrix</code>
 * 
 * @see <code>FilterExpressionMatrixDialog</code>
 * @author UIBC
 *
 */
public class FilterDialog extends Dialog implements SelectionListener {


    /**
     * 
     */
    private static final long serialVersionUID = 112312911659116561L;
    // data
    private Filter filter;
    private ExpressionMatrix matrix;
    private Button btnColumn;
    private Button btnRow;
    private Combo cmbType;
    private Combo cmbRow;
    private Combo cmbColumn;
    private Composite composite_1;
    private Label lblCutoff;
    private Composite composite_2;
    private Text txtCutoff2;
    private Text txtCutoff1;
    private Composite composite_3;
    private Label lblNewLabel_1;
    private Text textName;
    private Composite composite;
    private Composite composite_4;
    private Composite composite_5;
    private Composite composite_6;
    private Label lblSetTheProperties;
    private Label label;


    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public FilterDialog(Filter filter, Shell parentShell) {
        super(parentShell);
        this.filter = filter;
        this.matrix = filter.getMatrix();
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();
        gridLayout.numColumns = 2;

        lblSetTheProperties = new Label(container, SWT.NONE);
        lblSetTheProperties.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblSetTheProperties.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
        lblSetTheProperties.setText("Set Filter properties");
        new Label(container, SWT.NONE);

        label = new Label(container, SWT.NONE);

        composite_3 = new Composite(container, SWT.NONE);
        composite_3.setLayout(new GridLayout(2, false));
        composite_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));

        lblNewLabel_1 = new Label(composite_3, SWT.NONE);
        lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblNewLabel_1.setText("Name of the filter :");

        textName = new Text(composite_3, SWT.BORDER);
        GridData gd_textName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_textName.widthHint = 188;
        textName.setLayoutData(gd_textName);

        composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
        composite.setLayout(new GridLayout(2, false));

        composite_5 = new Composite(composite, SWT.NONE);
        composite_5.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
        composite_5.setLayout(new GridLayout(2, false));

        Label lblSelectAFilter = new Label(composite_5, SWT.NONE);
        lblSelectAFilter.setText("Select a type of filter :");

        cmbType = new Combo(composite_5, SWT.READ_ONLY);
        cmbType.setItems(Filter.TYPE_REPRESENTATION);
        cmbType.select(0);
        cmbType.addSelectionListener(this);

        composite_4 = new Composite(composite, SWT.NONE);
        composite_4.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
        composite_4.setLayout(new GridLayout(2, false));

        composite_1 = new Composite(composite_4, SWT.NONE);
        composite_1.setLayout(new GridLayout(2, false));

        lblCutoff = new Label(composite_1, SWT.NONE);
        lblCutoff.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblCutoff.setText("Set a :");

        txtCutoff1 = new Text(composite_1, SWT.BORDER);
        GridData gd_txtCutoff1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_txtCutoff1.widthHint = 73;
        txtCutoff1.setLayoutData(gd_txtCutoff1);

        composite_2 = new Composite(composite_4, SWT.NONE);
        composite_2.setLayout(new GridLayout(2, false));

        Label lblCutoff_1 = new Label(composite_2, SWT.NONE);
        lblCutoff_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblCutoff_1.setText("Set b :");

        txtCutoff2 = new Text(composite_2, SWT.BORDER);
        GridData gd_txtCutoff2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_txtCutoff2.widthHint = 85;
        txtCutoff2.setLayoutData(gd_txtCutoff2);
        txtCutoff2.setEnabled(false);

        composite_6 = new Composite(container, SWT.NONE);
        composite_6.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
        composite_6.setLayout(new GridLayout(2, false));

        btnColumn = new Button(composite_6, SWT.RADIO);
        btnColumn.setText("column");
        btnColumn.addSelectionListener(this);
        btnColumn.setSelection(true);
        cmbColumn = new Combo(composite_6, SWT.READ_ONLY);
        GridData gd_cmbColumn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_cmbColumn.widthHint = 300;
        cmbColumn.setLayoutData(gd_cmbColumn);
        cmbColumn.setItems(matrix.getHeaders().toArray(new String[0]));
        cmbColumn.select(1);

        btnRow = new Button(composite_6, SWT.RADIO);
        btnRow.setText("Row");
        btnRow.addSelectionListener(this);

        cmbRow = new Combo(composite_6, SWT.READ_ONLY);
        GridData gd_cmbRow = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_cmbRow.widthHint = 300;
        cmbRow.setLayoutData(gd_cmbRow);
        cmbRow.setItems(matrix.getRowNamesToList().toArray(new String[0]));
        cmbRow.select(0);
        cmbRow.addSelectionListener(this);
        cmbColumn.addSelectionListener(this);

        initDisplay();

        return container;
    }


    public void initDisplay() {
        // name
        textName.setText(filter.getName());
        // type
        cmbType.select(filter.getTypeFilter().ordinal());
        if (filter.getTypeFilter().equals(TypeFilter.BETWEEN))
            txtCutoff2.setEnabled(true);
        // row or column ?
        if (filter.isFilterColumn()) {
            btnColumn.setSelection(true);
            cmbRow.setEnabled(false);
        } else {
            btnRow.setSelection(true);
            cmbColumn.setEnabled(false);
        }
        // row/column name

        if (!filter.getTableElementName().equals("")) {
            if (filter.isFilterColumn())
                cmbColumn.select(cmbColumn.indexOf(filter.getTableElementName()));
            else
                cmbRow.select(cmbRow.indexOf(filter.getTableElementName()));
        } else {
            if (filter.isFilterColumn())
                cmbColumn.select(0);
            else
                cmbRow.select(0);
        }

        // textBox
        txtCutoff1.setText(filter.getCutOff1() + "");
        if (filter.getCutOff2() != -1000000)
            txtCutoff2.setText(filter.getCutOff2() + "");



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

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(427, 374);
    }

    @Override
    public void okPressed() {
        try {
            // name
            filter.setName(textName.getText());
            // element name
            if (filter.isFilterColumn())
                filter.setTableElementName(cmbColumn.getItem(cmbColumn.getSelectionIndex()));
            else
                filter.setTableElementName(cmbRow.getItem(cmbRow.getSelectionIndex()));

            // cutoff
            filter.setCutOff1(Double.parseDouble(txtCutoff1.getText()));
            if (filter.getTypeFilter() == TypeFilter.BETWEEN)
                filter.setCutOff2(Double.parseDouble(txtCutoff2.getText()));
            if (filter.isFilterColumn()) {
                filter.getExcludeRow().clear();
                for (String rowName : matrix.getRowNames().keySet()) {
                    double value = matrix.getValue(rowName, cmbColumn.getText());
                    if (!filter.filterValue(value)) {
                        filter.getExcludeRow().add(rowName);
                    }
                }
            } else {
                filter.getExcludeColumn().clear();
                cmbRow.getText();
                for (String header : matrix.getHeaders()) {
                    double value = matrix.getValue(cmbRow.getText(), header);
                    if (!filter.filterValue(value)) {
                        filter.getExcludeColumn().add(header);
                    }
                }
            }
            this.close();
        } catch (Exception e1) {
            System.err.println("Cannot parse the cutOff");
            this.close();
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnRow || e.getSource() == btnColumn) {
            if (btnRow.getSelection()) {
                cmbRow.setEnabled(true);
                cmbColumn.setEnabled(false);
                filter.setFilterColumn(false);
            } else {
                cmbRow.setEnabled(false);
                cmbColumn.setEnabled(true);
                filter.setFilterColumn(true);
            }
        } else if (e.getSource() == cmbType) {
            filter.setTypeFilter(TypeFilter.values()[cmbType.getSelectionIndex()]);
            System.out.println(filter.getTypeFilter());
            switch (filter.getTypeFilter()) {
                case BETWEEN:
                    txtCutoff2.setEnabled(true);
                    break;
                default:
                    txtCutoff2.setEnabled(false);
                    break;
            }

        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }
}
