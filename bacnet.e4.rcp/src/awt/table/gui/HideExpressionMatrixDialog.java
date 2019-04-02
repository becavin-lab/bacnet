package awt.table.gui;


import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import bacnet.Database;
import bacnet.datamodel.annotation.Signature;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.expressionAtlas.core.SelectGenomeElementDialog;
import bacnet.expressionAtlas.core.SignatureSelectionDialog;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;

public class HideExpressionMatrixDialog extends Dialog implements SelectionListener{

	// GUI
	private Button btnAddRow;
	private Button btnHideRow;
	public List listAddRow;
	public List listHideRow;
	public List listAddColumn;
	public List listHideColumn;
	private Button btnHideColumn;
	private Button btnAddColumn;
	private Button btnLoadRow;
	private Button btnLoadColumn;
	private Button btnSignaturesLoader;
	private Button btnSelectGenes;
	private Button btnAddAll;
	private Button btnHideall;

	// data
	private TreeSet<String> hideDialogExcludeRow = new TreeSet<String>();
	private TreeSet<String> hideDialogExcludeColumn = new TreeSet<String>();
	private ExpressionMatrix matrix;

	private Shell shell;
	private EPartService partService;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public HideExpressionMatrixDialog(ExpressionMatrix matrix, TreeSet<String> hideDialogExcludeRow, TreeSet<String> hideDialogExcludeColumn, Shell parentShell, EPartService partService) {
		super(parentShell);
		this.shell = parentShell;
		this.partService = partService;
		this.matrix = matrix;
		this.hideDialogExcludeColumn = hideDialogExcludeColumn;
		this.hideDialogExcludeRow = hideDialogExcludeRow;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(container, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_composite.heightHint = 319;
		gd_composite.widthHint = 697;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(7, false));

		Label lblRow = new Label(composite, SWT.NONE);
		GridData gd_lblRow = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_lblRow.widthHint = 316;
		lblRow.setLayoutData(gd_lblRow);
		lblRow.setAlignment(SWT.CENTER);
		lblRow.setText("Row");

		Label label = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gd_label = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
		gd_label.heightHint = 288;
		gd_label.widthHint = 7;
		label.setLayoutData(gd_label);

		Label lblColumn = new Label(composite, SWT.NONE);
		GridData gd_lblColumn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_lblColumn.widthHint = 306;
		lblColumn.setLayoutData(gd_lblColumn);
		lblColumn.setAlignment(SWT.CENTER);
		lblColumn.setText("Column");

		listAddRow = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_listAddRow = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_listAddRow.widthHint = 116;
		listAddRow.setLayoutData(gd_listAddRow);


		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));

		btnAddAll = new Button(composite_1, SWT.NONE);
		btnAddAll.setText("< add all");
		btnAddAll.addSelectionListener(this);
		btnAddRow = new Button(composite_1, SWT.NONE);
		btnAddRow.setText("< add");
		btnAddRow.addSelectionListener(this);

		btnHideRow = new Button(composite_1, SWT.NONE);
		btnHideRow.setText("hide >");

		btnHideall = new Button(composite_1, SWT.NONE);
		btnHideall.setText("hide all >");
		btnHideall.addSelectionListener(this);
		btnLoadRow = new Button(composite_1, SWT.NONE);
		btnLoadRow.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/sigLoad.bmp"));
		btnLoadRow.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnLoadRow.setToolTipText("Load list of element to hide");

		btnSelectGenes = new Button(composite_1, SWT.NONE);
		btnSelectGenes.setText("Select genes");
		btnSelectGenes.addSelectionListener(this);

		btnSignaturesLoader = new Button(composite_1, SWT.NONE);
		btnSignaturesLoader.setText("Select list of genes");
		btnSignaturesLoader.addSelectionListener(this);
		btnHideRow.addSelectionListener(this);
		btnLoadRow.addSelectionListener(this);

		listHideRow = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_listHideRow = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_listHideRow.widthHint = 112;
		listHideRow.setLayoutData(gd_listHideRow);



		listAddColumn = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_listAddColumn = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_listAddColumn.widthHint = 162;
		listAddColumn.setLayoutData(gd_listAddColumn);


		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));

		btnAddColumn = new Button(composite_2, SWT.NONE);
		btnAddColumn.setText("< add");
		btnAddColumn.addSelectionListener(this);

		btnHideColumn = new Button(composite_2, SWT.NONE);
		btnHideColumn.setText("hide >");

		btnLoadColumn = new Button(composite_2, SWT.NONE);
		btnLoadColumn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnLoadColumn.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/sigLoad.bmp"));
		btnLoadColumn.setToolTipText("Load list of columns to hide");
		btnHideColumn.addSelectionListener(this);
		btnLoadColumn.addSelectionListener(this);

		listHideColumn = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_listHideColumn = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_listHideColumn.widthHint = 175;
		listHideColumn.setLayoutData(gd_listHideColumn);

		listHideRow.setItems(hideDialogExcludeRow.toArray(new String[0]));
		listHideColumn.setItems(hideDialogExcludeColumn.toArray(new String[0]));
		for(String rowName : matrix.getRowNamesToList()){
			if(!hideDialogExcludeRow.contains(rowName)) listAddRow.add(rowName);
		}
		for(String header : matrix.getHeaders()){
			if(!hideDialogExcludeColumn.contains(header)) listAddColumn.add(header);
		}
		for(String header : matrix.getHeaderAnnotation()){
			if(!hideDialogExcludeColumn.contains(header)) listAddColumn.add(header);
		}

		return container;
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
		return new Point(739, 420);
	}

	@Override
	public void okPressed(){
		hideDialogExcludeColumn.clear();
		hideDialogExcludeRow.clear();
		for(String colName : listHideColumn.getItems()){
			hideDialogExcludeColumn.add(colName);
		}
		for(String rowName : listHideRow.getItems()){
			hideDialogExcludeRow.add(rowName);
		}
		this.close();
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		String[] selection = new String[0];
		if(e.getSource()==btnAddRow){
			selection = listHideRow.getSelection();
			for(int i=0;i<selection.length;i++){
				listAddRow.add(selection[i]);				
			}
			for(int i=0;i<selection.length;i++){
				listHideRow.remove(selection[i]);
			}
		}else if(e.getSource()==btnAddAll){
			for(String rowName : matrix.getRowNamesToList()){
				listAddRow.add(rowName);
			}
			listHideRow.removeAll();
			listAddRow.deselectAll();
		}else if(e.getSource()==btnHideall){
			for(String rowName : matrix.getRowNamesToList()){
				listHideRow.add(rowName);
			}
			listAddRow.removeAll();
			listHideRow.deselectAll();
		}else if(e.getSource()==btnHideRow){
			selection = listAddRow.getSelection();
			for(int i=0;i<selection.length;i++){
				listHideRow.add(selection[i]);				
			}
			for(int i=0;i<selection.length;i++){
				listAddRow.remove(selection[i]);
			}
		}else if(e.getSource()==btnAddColumn){
			selection = listHideColumn.getSelection();
			for(int i=0;i<selection.length;i++){
				listHideColumn.remove(selection[i]);
				listAddColumn.add(selection[i]);				
			}
		}else if(e.getSource()==btnHideColumn){
			selection = listAddColumn.getSelection();
			for(int i=0;i<selection.length;i++){
				listAddColumn.remove(selection[i]);
				listHideColumn.add(selection[i]);
			}
		}else if(e.getSource()==btnLoadRow){
			try{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open a signature (list of row names)");
				String[] filterExt = {"*.txt","*.*" };
				fd.setFilterExtensions(filterExt);
				fd.setFilterPath(Database.getInstance().getPATH());
				String fileName = fd.open();
				if(fileName!=null){
					ArrayList<String> signature = TabDelimitedTableReader.readList(fileName);

					// fill listAddRow with all the row
					ArrayList<String> addRow = new ArrayList<String>();
					ArrayList<String> hideRow = new ArrayList<String>();
					for(String rowName : matrix.getRowNames().keySet()){
						if(signature.contains(rowName)) addRow.add(rowName);
						else hideRow.add(rowName);
					}

					// fill the different lists
					listAddRow.removeAll();
					listHideRow.removeAll();
					listAddRow.setItems(addRow.toArray(new String[0]));
					listHideRow.setItems(hideRow.toArray(new String[0]));

					// print the element not found
					for(String rowName : signature){
						if(!(addRow.contains(rowName) || hideRow.contains(rowName))) System.out.println("Not found: "+rowName);
					}
				}
			}catch(Exception e1){
				System.out.println("Cannot read the signature");
			}
		}else if(e.getSource()==btnSelectGenes){
			TreeSet<String> includeElements = new TreeSet<>();
			TreeSet<String> excludeElements = new TreeSet<>();
			SelectGenomeElementDialog dialog = new SelectGenomeElementDialog(shell,partService,includeElements,excludeElements,Genome.EGDE_NAME);
			if(dialog.open()==0){
				ArrayList<String> signature = new ArrayList<>();
				for(String row : excludeElements){
					signature.add(row);
				}
				
				// fill listAddRow with all the row
				ArrayList<String> addRow = new ArrayList<String>();
				ArrayList<String> hideRow = new ArrayList<String>();
				for(String rowName : matrix.getRowNames().keySet()){
					if(signature.contains(rowName)) addRow.add(rowName);
					else hideRow.add(rowName);
				}

				// fill the different lists
				listAddRow.removeAll();
				listHideRow.removeAll();
				listAddRow.setItems(addRow.toArray(new String[0]));
				listHideRow.setItems(hideRow.toArray(new String[0]));

				// print the element not found
				for(String rowName : signature){
					if(!(addRow.contains(rowName) || hideRow.contains(rowName))) System.out.println("Not found: "+rowName);
				}
			}

		}else if(e.getSource()==btnSignaturesLoader){
			SignatureSelectionDialog dialog = new SignatureSelectionDialog(shell);
			if(dialog.open()==0){
				Signature signature = dialog.getSignature();
				System.out.println(signature.getName());
				// fill listAddRow with all the row
				ArrayList<String> addRow = new ArrayList<String>();
				ArrayList<String> hideRow = new ArrayList<String>();
				for(String rowName : matrix.getRowNames().keySet()){
					if(signature.getElements().contains(rowName)) addRow.add(rowName);
					else hideRow.add(rowName);
				}

				// fill the different lists
				listAddRow.removeAll();
				listHideRow.removeAll();
				listAddRow.setItems(addRow.toArray(new String[0]));
				listHideRow.setItems(hideRow.toArray(new String[0]));

				// print the element not found
				for(String rowName : signature.getElements()){
					if(!(addRow.contains(rowName) || hideRow.contains(rowName))) System.out.println("Not found: "+rowName);
				}
			}
		}else if(e.getSource()==btnLoadColumn){
			try{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open a signature (list of column names)");
				String[] filterExt = {"*.txt","*.*" };
				fd.setFilterExtensions(filterExt);
				fd.setFilterPath(Database.getInstance().getPATH());
				String fileName = fd.open();
				if(fileName!=null){
					ArrayList<String> signature = TabDelimitedTableReader.readList(fileName);

					// fill listAddRow with all the row
					ArrayList<String> addCol = new ArrayList<String>();
					ArrayList<String> hideCol = new ArrayList<String>();
					for(String header : matrix.getHeaders()){
						if(signature.contains(header)) listAddColumn.add(header);
						else listHideColumn.add(header);
					}

					// fill the different lists
					listAddColumn.removeAll();
					listHideColumn.removeAll();
					listAddColumn.setItems(addCol.toArray(new String[0]));
					listHideColumn.setItems(hideCol.toArray(new String[0]));

					// print the element not found
					for(String rowName : signature){
						if(!(addCol.contains(rowName) || hideCol.contains(rowName))) System.out.println("Not found: "+rowName);
					}
				}
			}catch(Exception e1){
				System.out.println("Cannot read the signature");
			}
		}

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public TreeSet<String> getHideDialogExcludeRow() {
		return hideDialogExcludeRow;
	}

	public void setHideDialogExcludeRow(TreeSet<String> hideDialogExcludeRow) {
		this.hideDialogExcludeRow = hideDialogExcludeRow;
	}

	public TreeSet<String> getHideDialogExcludeColumn() {
		return hideDialogExcludeColumn;
	}

	public void setHideDialogExcludeColumn(TreeSet<String> hideDialogExcludeColumn) {
		this.hideDialogExcludeColumn = hideDialogExcludeColumn;
	}
}
