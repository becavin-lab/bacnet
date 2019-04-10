package bacnet.table.gui;

import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import bacnet.datamodel.annotation.Signature;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.expressionAtlas.core.SelectGenomeElementDialog;
import bacnet.expressionAtlas.core.SignatureSelectionDialog;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;

public class HideExpressionMatrixDialog extends TitleAreaDialog implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6890471316308295477L;
	// GUI
	private Button btnAddRow;
	private Button btnHideRow;
	public Table listAddRow;
	public Table listHideRow;
	public List listAddColumn;
	public List listHideColumn;
	private Button btnHideColumn;
	private Button btnAddColumn;
	private Button btnLoadRow;
	private Button btnLoadColumn;
	private Button btnSignaturesLoader;
	private Button btnSelectGenes;

	// data
	private ArrayList<String> showRow = new ArrayList<>();
	private ArrayList<String> hideRow = new ArrayList<>();
	private ArrayList<String> hideDialogExcludeColumn = new ArrayList<String>();
	private final ExpressionMatrix matrix;

	private EPartService partService;
	private Shell shell;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public HideExpressionMatrixDialog(ExpressionMatrix matrix, ArrayList<String> hideDialogExcludeRow,
			ArrayList<String> hideDialogExcludeColumn, Shell shell, EPartService partService) {
		super(shell);
		this.shell = shell;
		this.partService = partService;
		this.matrix = matrix;
		this.hideDialogExcludeColumn = hideDialogExcludeColumn;
		this.hideRow = hideDialogExcludeRow;
		for (String rowName : matrix.getRowNamesToList()) {
			if (!hideRow.contains(rowName))
				showRow.add(rowName);
		}
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Hide or Show Elements");
		setMessage("Select specific rows or columns and press ok");
		Composite container = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(container, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1);
		gd_composite.heightHint = 339;
		gd_composite.widthHint = 697;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(9, false));

		Label lblRow = new Label(composite, SWT.NONE);
		lblRow.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
		lblRow.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));
		lblRow.setAlignment(SWT.CENTER);
		lblRow.setText("Row");
		new Label(composite, SWT.NONE);

		Label label = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gd_label = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
		gd_label.heightHint = 318;
		gd_label.widthHint = 7;
		label.setLayoutData(gd_label);
		new Label(composite, SWT.NONE);

		Label lblColumn = new Label(composite, SWT.NONE);
		lblColumn.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
		lblColumn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));
		lblColumn.setAlignment(SWT.CENTER);
		lblColumn.setText("Column");

		listAddRow = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.VIRTUAL);
		GridData gd_listAddRow = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_listAddRow.heightHint = 300;
		gd_listAddRow.widthHint = 150;
		listAddRow.setLayoutData(gd_listAddRow);

		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_1.setLayout(new GridLayout(1, false));
		btnAddRow = new Button(composite_1, SWT.NONE);
		btnAddRow.setText("< add");
		btnAddRow.addSelectionListener(this);

		btnHideRow = new Button(composite_1, SWT.NONE);
		btnHideRow.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnHideRow.setText("hide >");
		btnLoadRow = new Button(composite_1, SWT.NONE);
		btnLoadRow.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/sigLoad.bmp"));
		btnLoadRow.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnLoadRow.setToolTipText("Load list of element to hide");

		btnSelectGenes = new Button(composite_1, SWT.NONE);
		btnSelectGenes.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnSelectGenes.setText("Select genes");
		btnSelectGenes.addSelectionListener(this);

		btnSignaturesLoader = new Button(composite_1, SWT.NONE);
		btnSignaturesLoader.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnSignaturesLoader.setText("Select list of genes");
		btnSignaturesLoader.addSelectionListener(this);
		btnHideRow.addSelectionListener(this);
		btnLoadRow.addSelectionListener(this);

		listHideRow = new Table(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.VIRTUAL);
		GridData gd_listHideRow = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_listHideRow.heightHint = 300;
		gd_listHideRow.widthHint = 150;
		listHideRow.setLayoutData(gd_listHideRow);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		listAddColumn = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_listAddColumn = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_listAddColumn.heightHint = 300;
		gd_listAddColumn.widthHint = 150;
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
		btnLoadColumn.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/sigLoad.bmp"));
		btnLoadColumn.setToolTipText("Load list of columns to hide");
		btnHideColumn.addSelectionListener(this);
		btnLoadColumn.addSelectionListener(this);

		listHideColumn = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_listHideColumn = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		gd_listHideColumn.heightHint = 300;
		gd_listHideColumn.widthHint = 150;
		listHideColumn.setLayoutData(gd_listHideColumn);

		initDataColumn();
		initDataRow();

		return container;
	}

	/**
	 * Initialize column lists
	 */
	private void initDataColumn() {
		listHideColumn.setItems(hideDialogExcludeColumn.toArray(new String[0]));
		for (String header : matrix.getHeaders()) {
			if (!hideDialogExcludeColumn.contains(header))
				listAddColumn.add(header);
		}
		for (String header : matrix.getHeaderAnnotation()) {
			if (!hideDialogExcludeColumn.contains(header))
				listAddColumn.add(header);
		}
	}

	/**
	 * Initialize row lists (which are in fact Table.VIRTUAL)
	 */
	private void initDataRow() {
		listAddRow.addListener(SWT.SetData, new Listener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1435814750737212462L;

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(showRow.get(index));
			}
		});
		listAddRow.setItemCount(showRow.size());
		listHideRow.addListener(SWT.SetData, new Listener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -333006213053355498L;

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(hideRow.get(index));
			}
		});
		listHideRow.setItemCount(hideRow.size());
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
		return new Point(711, 518);
	}

	@Override
	public void okPressed() {
		hideDialogExcludeColumn.clear();
		for (String colName : listHideColumn.getItems()) {
			hideDialogExcludeColumn.add(colName);
		}
		this.close();
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		String[] selection = new String[0];
		if (e.getSource() == btnAddRow) {
			for (TableItem element : listHideRow.getSelection()) {
				showRow.add(element.getText());
				hideRow.remove(element.getText());
			}
			initDataRow();
			listAddRow.redraw();
			listHideRow.clearAll();
		} else if (e.getSource() == btnHideRow) {
			for (TableItem element : listAddRow.getSelection()) {
				System.out.println(element.getText());
				showRow.remove(element.getText());
				hideRow.add(element.getText());
			}
			initDataRow();
			listAddRow.clearAll();
			listHideRow.redraw();
		} else if (e.getSource() == btnAddColumn) {
			selection = listHideColumn.getSelection();
			for (int i = 0; i < selection.length; i++) {
				listHideColumn.remove(selection[i]);
				listAddColumn.add(selection[i]);
			}
		} else if (e.getSource() == btnHideColumn) {
			selection = listAddColumn.getSelection();
			for (int i = 0; i < selection.length; i++) {
				listAddColumn.remove(selection[i]);
				listHideColumn.add(selection[i]);
			}
		} else if (e.getSource() == btnLoadRow) {
			try {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open a signature (list of row names)");
				// String[] filterExt = {"*.txt","*.*" };
				String fileName = fd.open();
				if (fileName != null) {
					ArrayList<String> signature = TabDelimitedTableReader.readList(fileName);

					// fill listAddRow with all the row
					showRow = new ArrayList<String>();
					hideRow = new ArrayList<String>();
					for (String rowName : matrix.getRowNames().keySet()) {
						if (signature.contains(rowName))
							showRow.add(rowName);
						else
							hideRow.add(rowName);
					}

					// update the different lists
					listAddRow.redraw();
					listHideRow.redraw();

					// print the element not found
					for (String rowName : signature) {
						if (!(showRow.contains(rowName) || hideRow.contains(rowName)))
							System.out.println("Not found: " + rowName);
					}
				}
			} catch (Exception e1) {
				System.out.println("Cannot read the signature");
			}
		} else if (e.getSource() == btnSelectGenes) {
			TreeSet<String> includeElements = new TreeSet<>();
			TreeSet<String> excludeElements = new TreeSet<>();
			SelectGenomeElementDialog dialog = new SelectGenomeElementDialog(shell, partService, includeElements,
					excludeElements, Genome.EGDE_NAME);
			if (dialog.open() == 0) {
				ArrayList<String> signature = new ArrayList<>();
				for (String row : includeElements) {
					signature.add(row);
				}
				// fill listAddRow with all the row
				showRow = new ArrayList<String>();
				hideRow = new ArrayList<String>();
				for (String rowName : matrix.getRowNames().keySet()) {
					if (signature.contains(rowName))
						showRow.add(rowName);
					else
						hideRow.add(rowName);
				}

				// update the different lists
				listAddRow.redraw();
				listHideRow.redraw();

				// print the element not found
				for (String rowName : signature) {
					if (!(showRow.contains(rowName) || hideRow.contains(rowName)))
						System.out.println("Not found: " + rowName);
				}
			}

		} else if (e.getSource() == btnSignaturesLoader) {
			SignatureSelectionDialog dialog = new SignatureSelectionDialog(shell);
			if (dialog.open() == 0) {
				Signature signature = dialog.getSignature();
				System.out.println(signature.getName());
				// fill listAddRow with all the row
				showRow = new ArrayList<String>();
				hideRow = new ArrayList<String>();
				for (String rowName : matrix.getRowNames().keySet()) {
					if (signature.getElements().contains(rowName))
						showRow.add(rowName);
					else
						hideRow.add(rowName);
				}

				// update the different lists
				listAddRow.redraw();
				listHideRow.redraw();

				// print the element not found
				for (String rowName : signature.getElements()) {
					if (!(showRow.contains(rowName) || hideRow.contains(rowName)))
						System.out.println("Not found: " + rowName);
				}
			}
		} else if (e.getSource() == btnLoadColumn) {
			try {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open a signature (list of column names)");
				// String[] filterExt = {"*.txt","*.*" };
				String fileName = fd.open();
				if (fileName != null) {
					ArrayList<String> signature = TabDelimitedTableReader.readList(fileName);

					// fill listAddRow with all the row
					ArrayList<String> addCol = new ArrayList<String>();
					ArrayList<String> hideCol = new ArrayList<String>();
					for (String header : matrix.getHeaders()) {
						if (signature.contains(header))
							listAddColumn.add(header);
						else
							listHideColumn.add(header);
					}

					// fill the different lists
					listAddColumn.removeAll();
					listHideColumn.removeAll();
					listAddColumn.setItems(addCol.toArray(new String[0]));
					listHideColumn.setItems(hideCol.toArray(new String[0]));

					// print the element not found
					for (String rowName : signature) {
						if (!(addCol.contains(rowName) || hideCol.contains(rowName)))
							System.out.println("Not found: " + rowName);
					}
				}
			} catch (Exception e1) {
				System.out.println("Cannot read the signature");
			}
		}

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
