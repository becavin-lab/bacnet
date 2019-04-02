package awt.table.gui;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.ResourceManager;
import bacnet.table.core.Filter;
import bacnet.table.core.Filter.TypeFilter;
import bacnet.utils.VectorUtils;

public class FilterDialog extends Dialog implements SelectionListener{
	
	
	// data
	private Filter filter;
	private ExpressionMatrix matrix;
	private Button btnColumn;
	private Button btnRow;
	private Combo cmbType;
	private Button btnLoadFilter;
	private Button btnSaveFilter;
	private Combo cmbRow;
	private Combo cmbColumn;
	private Composite composite_1;
	private Label lblCutoff;
	private Composite composite_2;
	private Text txtCutoff2;
	private Text txtCutoff1;
	private Label lblMin;
	private Label lblMax;
	private Label lblNewLabel;
	private Composite composite_3;
	private Label lblNewLabel_1;
	private Text textName;
	
	private Shell shell;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public FilterDialog(Filter filter, Shell parentShell) {
		super(parentShell);
		this.shell = parentShell;
		this.filter = filter;
		this.matrix = filter.getMatrix();
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 2;
		
		composite_3 = new Composite(container, SWT.NONE);
		composite_3.setLayout(new GridLayout(2, false));
		composite_3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		lblNewLabel_1 = new Label(composite_3, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Name of the filter :");
		
		textName = new Text(composite_3, SWT.BORDER);
		GridData gd_textName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textName.widthHint = 274;
		textName.setLayoutData(gd_textName);
		
		lblNewLabel = new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblSelectAFilter = new Label(container, SWT.NONE);
		lblSelectAFilter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSelectAFilter.setText("Select a filter type");
		
		cmbType = new Combo(container, SWT.READ_ONLY);
		cmbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbType.setItems(Filter.TYPE_REPRESENTATION);
		cmbType.select(0);
		cmbType.addSelectionListener(this);
		
		new Label(container, SWT.NONE);
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		btnLoadFilter = new Button(composite, SWT.NONE);
		btnLoadFilter.setToolTipText("Open filter file");
		btnLoadFilter.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/fltOpen.bmp"));
		btnLoadFilter.addSelectionListener(this);
		btnSaveFilter = new Button(composite, SWT.NONE);
		btnSaveFilter.setToolTipText("Save filter file");
		btnSaveFilter.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/fltSave.bmp"));
		btnSaveFilter.addSelectionListener(this);
		
		btnRow = new Button(container, SWT.RADIO);
		btnRow.setText("Row");
		btnRow.addSelectionListener(this);
		
		btnColumn = new Button(container, SWT.RADIO);
		btnColumn.setText("column");
		btnColumn.addSelectionListener(this);
		btnColumn.setSelection(true);
		
		cmbRow = new Combo(container, SWT.READ_ONLY);
		cmbRow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbRow.setItems(matrix.getRowNamesToList().toArray(new String[0]));
		cmbRow.select(0);
		cmbRow.addSelectionListener(this);
		cmbColumn = new Combo(container, SWT.READ_ONLY);
		cmbColumn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbColumn.setItems(matrix.getHeaders().toArray(new String[0]));
		cmbColumn.select(1);
		cmbColumn.addSelectionListener(this);
		
		lblMin = new Label(container, SWT.NONE);
		lblMin.setAlignment(SWT.RIGHT);
		GridData gd_lblMin = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblMin.widthHint = 187;
		lblMin.setLayoutData(gd_lblMin);
		lblMin.setText("Min");
		
		composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		
		lblCutoff = new Label(composite_1, SWT.NONE);
		lblCutoff.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCutoff.setText("Cutoff 1: ");
		
		txtCutoff1 = new Text(composite_1, SWT.BORDER);
		GridData gd_txtCutoff1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtCutoff1.widthHint = 73;
		txtCutoff1.setLayoutData(gd_txtCutoff1);
		
		lblMax = new Label(container, SWT.NONE);
		lblMax.setAlignment(SWT.RIGHT);
		GridData gd_lblMax = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblMax.widthHint = 185;
		lblMax.setLayoutData(gd_lblMax);
		lblMax.setText("Max");
		
		composite_2 = new Composite(container, SWT.NONE);
		composite_2.setLayout(new GridLayout(2, false));
		
		Label lblCutoff_1 = new Label(composite_2, SWT.NONE);
		lblCutoff_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCutoff_1.setText("Cutoff 2:");
		
		txtCutoff2 = new Text(composite_2, SWT.BORDER);
		GridData gd_txtCutoff2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtCutoff2.widthHint = 85;
		txtCutoff2.setLayoutData(gd_txtCutoff2);
		txtCutoff2.setEnabled(false);

		initDisplay();
		
		return container;
	}
	
	
	public void initDisplay(){
		// name
		textName.setText(filter.getName());
		// type
		cmbType.select(filter.getTypeFilter().ordinal());
		if(filter.getTypeFilter().equals(TypeFilter.BETWEEN)) txtCutoff2.setEnabled(true);
		// row or column ?
		if(filter.isFilterColumn()){
			btnColumn.setSelection(true);
			cmbRow.setEnabled(false);
		}else{
			btnRow.setSelection(true);
			cmbColumn.setEnabled(false);
		}
		// row/column name
		
		if(!filter.getTableElementName().equals("")){
			if(filter.isFilterColumn()) cmbColumn.select(cmbColumn.indexOf(filter.getTableElementName()));
			else	cmbRow.select(cmbRow.indexOf(filter.getTableElementName()));
		}else{
			if(filter.isFilterColumn()) cmbColumn.select(0);
			else	cmbRow.select(0);
		}
	
		// min and max
		setMinMax(filter.getTableElementName());
		
		// textBox
		txtCutoff1.setText(filter.getCutOff1()+"");
		if(filter.getCutOff2()!=-1000000) txtCutoff2.setText(filter.getCutOff2()+"");
		
		
		
	}
	
	private void setMinMax(String tableElementName){
		if(filter.isFilterColumn()){
			String colName = tableElementName;
			if(matrix.getHeaders().contains(colName)){
				double[] column = matrix.getColumn(colName);
				lblMin.setText("Min: "+VectorUtils.min(column));
				lblMax.setText("Max: "+VectorUtils.max(column));				
			}else{
				lblMin.setText("Contain no values");
				lblMax.setText("Contain no values");
			}
		}else{
			String rowName = tableElementName;
			if(matrix.getRowNames().containsKey(rowName)){
				double[] row = matrix.getRow(rowName);
				lblMin.setText("Min: "+VectorUtils.min(row));
				lblMax.setText("Max: "+VectorUtils.max(row));
			}else{
				lblMin.setText("Contain no values");
				lblMax.setText("Contain no values");
			}
		};
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(652, 402);
	}
	
	@Override
	public void okPressed(){
		try{
			// name
			filter.setName(textName.getText());
			// element name
			if(filter.isFilterColumn()) filter.setTableElementName(cmbColumn.getItem(cmbColumn.getSelectionIndex()));
			else	filter.setTableElementName(cmbRow.getItem(cmbRow.getSelectionIndex()));
			
			// cutoff
			filter.setCutOff1(Double.parseDouble(txtCutoff1.getText()));
			if(filter.getTypeFilter()==TypeFilter.BETWEEN) filter.setCutOff2(Double.parseDouble(txtCutoff2.getText()));
			if(filter.isFilterColumn()){
				filter.getExcludeRow().clear();
				for(String rowName : matrix.getRowNames().keySet()){
					double value = matrix.getValue(rowName, cmbColumn.getText());
					if(!filter.filterValue(value)){
						filter.getExcludeRow().add(rowName); 
					}
				}
			}else{
				filter.getExcludeColumn().clear();
				cmbRow.getText();
				for(String header : matrix.getHeaders()){
					double value = matrix.getValue(cmbRow.getText(), header);
					if(!filter.filterValue(value)){
						filter.getExcludeColumn().add(header); 
					}
				}
			}
			this.close();
		}catch(Exception e1){
			System.err.println("Cannot parse the cutOff");
			this.close();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnRow || e.getSource()==btnColumn){
			if(btnRow.getSelection()){
				cmbRow.setEnabled(true);
				cmbColumn.setEnabled(false);
				filter.setFilterColumn(false);
			}else{
				cmbRow.setEnabled(false);
				cmbColumn.setEnabled(true);
				filter.setFilterColumn(true);
			}
		}else if(e.getSource()==cmbType){
			filter.setTypeFilter(TypeFilter.values()[cmbType.getSelectionIndex()]);
			System.out.println(filter.getTypeFilter());
			switch(filter.getTypeFilter()){
				case BETWEEN: txtCutoff2.setEnabled(true); break;
				default: txtCutoff2.setEnabled(false); break;
			}
			
		}else if(e.getSource()==cmbRow){
			setMinMax(cmbRow.getText());
		}else if(e.getSource()==cmbColumn){
			setMinMax(cmbColumn.getText());
		}else if(e.getSource()==btnLoadFilter){
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
		     fd.setText("Open the Filter in: ");
		     fd.setFilterPath(Database.getInstance().getPATH());
		     String[] filterExt = {"*.flt","*.*" };
		     fd.setFilterExtensions(filterExt);
		     String fileName = fd.open();
			try{
				filter = Filter.load(fileName);
				initDisplay();
				System.out.println("Filter loaded");
			}catch(Exception e1){
				System.out.println("Cannot read the filter");
			}
		}else if(e.getSource()==btnSaveFilter){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
		     fd.setText("Save the Filter to: ");
		     fd.setFilterPath(Database.getInstance().getPATH());
		     String[] filterExt = {"*.flt","*.*" };
		     fd.setFilterExtensions(filterExt);
		     String fileName = fd.open();
			try{
				filter.save(fileName);
				System.out.println("Filter saved");
			}catch(Exception e1){
				System.out.println("Cannot save the filter");
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
