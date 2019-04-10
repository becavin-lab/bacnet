package bacnet.sequenceTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.expressionAtlas.core.SelectGenomeElementDialog;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.BioConditionComparator;
import bacnet.utils.ArrayUtils;

public class AnnotationView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4058433027371889806L;

	public static final String ID = "bacnet.AnnotationView"; //$NON-NLS-1$

	/**
	 * Indicates if we focus the view, so we can pushState navigation
	 */
	private boolean focused = false;

	private String viewID = "";
	private Composite compositeSummary;
	private TableViewer tableBioCondition;
	private BioConditionComparator comparatorBioCondition;

	/**
	 * Array used to init data
	 */
	private String[][] bioCondsArray;
	private ArrayList<String[]> bioCondsToDisplay;
	private ArrayList<String[]> bioConds;

	private ArrayList<String> selectedGenes = new ArrayList<>();

	private ArrayList<String> columnNames = new ArrayList<>();
	private Label lblXxSrnas;
	private Button btnSaveTxt;

	private Genome genome;

	@Inject
	EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	private Text txtSearch;
	private Button btnHide;
	private Button btnSelectall;
	private Button btnUnselectall;

	@Inject
	public AnnotationView() {

	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		focused = true;
		Composite container = new Composite(parent, SWT.NONE);
		container.setBounds(0, 0, 559, 300);
		container.setLayout(new GridLayout(1, false));
		lblXxSrnas = new Label(container, SWT.BORDER | SWT.CENTER);
		lblXxSrnas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblXxSrnas.setFont(SWTResourceManager.getTitleFont());
		lblXxSrnas.setText("XX Listeria Complete Genomes");
		lblXxSrnas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		ScrolledComposite scrolledComposite = new ScrolledComposite(container,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		compositeSummary = new Composite(scrolledComposite, SWT.BORDER);
		compositeSummary.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(compositeSummary, SWT.NONE);
		composite.setLayout(new GridLayout(9, false));

		btnHide = new Button(composite, SWT.NONE);
		btnHide.setText("Select genome elements");
		btnHide.setToolTipText("Select specific genome elements");
		btnHide.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/hideData.bmp"));
		btnHide.addSelectionListener(this);

		txtSearch = new Text(composite, SWT.BORDER);

		Label lblSearch = new Label(composite, SWT.NONE);
		lblSearch.setText("Search");
		lblSearch.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		btnSelectall = new Button(composite, SWT.NONE);
		btnSelectall.setToolTipText("Select all genomes");
		btnSelectall.setImage(ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
		btnSelectall.addSelectionListener(this);
		Label lblSelectAll = new Label(composite, SWT.NONE);
		lblSelectAll.setText("Select all");
		lblSelectAll.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		btnUnselectall = new Button(composite, SWT.NONE);
		btnUnselectall.setToolTipText("Unselect all genomes");
		btnUnselectall.setImage(ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
		btnUnselectall.addSelectionListener(this);
		Label lblUnselectAll = new Label(composite, SWT.NONE);
		lblUnselectAll.setText("Unselect all");
		lblUnselectAll.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		btnSaveTxt = new Button(composite, SWT.NONE);
		btnSaveTxt.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
		btnSaveTxt.setToolTipText("Download gene selection into a table");
		btnSaveTxt.addSelectionListener(this);

		Label lblSaveSelectionAs = new Label(composite, SWT.NONE);
		lblSaveSelectionAs.setText("Download gene selection into a table");
		lblSaveSelectionAs.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
		txtSearch.addKeyListener(new org.eclipse.swt.events.KeyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2890510302565471497L;

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == 16777296 || e.keyCode == 13) {
					System.out.println("Search for " + txtSearch.getText());
					bioCondsToDisplay.clear();
					selectedGenes.clear();
					tableBioCondition.getTable().deselectAll();
					for (String[] row : bioConds) {
						boolean found = false;
						for (String cell : row) {
							if (cell.contains(txtSearch.getText())) {
								found = true;
							}
						}
						if (found) {
							bioCondsToDisplay.add(row);
						}
					}
					tableBioCondition.refresh();
					pushState();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		Label lblClickOneTime = new Label(compositeSummary, SWT.NONE);
		lblClickOneTime.setText("Double click on a gene to visualize it on the Gene View");
		lblClickOneTime.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
		{
			tableBioCondition = new TableViewer(compositeSummary, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			Table table = tableBioCondition.getTable();
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			tableBioCondition.getTable().setHeaderVisible(true);
			tableBioCondition.getTable().setLinesVisible(true);
			tableBioCondition.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					TableViewer viewer = (TableViewer) event.getSource();
					for (int index : viewer.getTable().getSelectionIndices()) {
						String selectedGene = tableBioCondition.getTable().getItem(index)
								.getText(columnNames.indexOf("Locus tag"));
						if (selectedGenes.contains(selectedGene)) {
							if (tableBioCondition.getTable().getSelectionIndices().length == 1) {
								selectedGenes.remove(selectedGene);
								tableBioCondition.replace(tableBioCondition.getTable().getItem(index).getData(), index);
							}
						} else {
							selectedGenes.add(selectedGene);
							tableBioCondition.replace(tableBioCondition.getTable().getItem(index).getData(), index);
						}
					}
				}
			});
			tableBioCondition.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					TableViewer viewer = (TableViewer) event.getSource();
					int i = viewer.getTable().getSelectionIndex();
					String geneName = tableBioCondition.getTable().getItem(i).getText(columnNames.indexOf("Locus tag"));
					System.out.println("gnee " + geneName);
					Gene gene = (Gene) genome.getGeneFromName(geneName);
					GeneView.displayGene(gene, partService);
				}
			});

		}
		scrolledComposite.setContent(compositeSummary);
		scrolledComposite.setMinSize(compositeSummary.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * Set all starting variables
	 */
	private void setData(Genome genome) {
		/*
		 * Load Table
		 */
		this.genome = genome;
		bioCondsArray = Annotation.getAnnotationGenes(genome, genome.getGeneNames());
		bioConds = ArrayUtils.toList(bioCondsArray);
		bioCondsToDisplay = ArrayUtils.toList(bioCondsArray);
		bioCondsToDisplay.remove(0);
		bioConds.remove(0);
		updateGenomeTable();
		lblXxSrnas.setText(
				"Browse through " + (bioCondsArray.length - 1) + " genes of " + genome.getSpecies() + " strain");
	}

	@Focus
	public void onFocus() {
		if (!focused) {
			pushState();
			focused = true;
		} else {
			focused = false;
		}
	}

	/**
	 * Push states to change url
	 */
	private void pushState() {
		HashMap<String, String> parameters = new HashMap<>();
		if (txtSearch.getText() != "") {
			parameters.put(NavigationManagement.SEARCH, txtSearch.getText());
		}
		parameters.put(NavigationManagement.GENOME, genome.getSpecies());
		NavigationManagement.pushStateView(viewID, parameters);
	}

	/**
	 * Update Table for Biological Condition
	 * 
	 * @param bioConds
	 */
	private void updateGenomeTable() {

		setColumnNames();
		for (TableColumn col : tableBioCondition.getTable().getColumns()) {
			col.dispose();
		}
		tableBioCondition.setContentProvider(new ArrayContentProvider());

		createColumns();

		tableBioCondition.getTable().setHeaderVisible(true);
		tableBioCondition.getTable().setLinesVisible(true);

		tableBioCondition.setInput(bioCondsToDisplay);
		comparatorBioCondition = new BioConditionComparator(columnNames);
		tableBioCondition.setComparator(comparatorBioCondition);
		for (int i = 0; i < tableBioCondition.getTable().getColumnCount(); i++) {
			tableBioCondition.getTable().getColumn(i).pack();
		}

	}

	/**
	 * Add the name of the columns
	 */
	private void setColumnNames() {
		columnNames = new ArrayList<>();
		columnNames.add("Select");
		for (String title : bioCondsArray[0]) {
			columnNames.add(title);
		}
	}

	private TableViewerColumn createTableViewerColumn(String title, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableBioCondition, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	private void createColumns() {

		TableViewerColumn col = createTableViewerColumn("Select", 0);
		col.setLabelProvider(new ColumnLabelProvider() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6764053440293157951L;

			@Override
			public Image getImage(Object element) {
				String[] bioCond = (String[]) element;
				if (selectedGenes.contains(bioCond[columnNames.indexOf("Locus tag") - 1])) {

					return ResourceManager.getPluginImage("bacnet", "icons/checked.bmp");
				} else {
					return ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp");
				}
			}

			@Override
			public String getText(Object element) {
				return "";
			}
		});
		col.getColumn().pack();
		for (int i = 0; i < bioCondsArray[0].length; i++) {
			final int k = i;
			TableViewerColumn col2 = createTableViewerColumn(bioCondsArray[0][i], i + 1);
			col2.setLabelProvider(new ColumnLabelProvider() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 3506742358064425914L;

				@Override
				public String getText(Object element) {
					String[] bioCond = (String[]) element;
					return bioCond[k];
				}
			});
			col2.getColumn().pack();
			// }
		}

	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6997325110066606691L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(e.getSource());
				comparatorBioCondition.setColumn(index);
				int dir = comparatorBioCondition.getDirection();
				tableBioCondition.getTable().setSortDirection(dir);
				tableBioCondition.getTable().setSortColumn(column);
				tableBioCondition.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * Display a given gene
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void openAnnotationView(EPartService partService, Genome genome) {
		String id = AnnotationView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, AnnotationView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		AnnotationView view = (AnnotationView) part.getObject();
		view.setViewID(id);
		view.setData(genome);
		view.pushState();
	}

	/**
	 * Display the view with saved parameters
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void displayAnnotationView(EPartService partService, HashMap<String, String> parameters) {
		String id = AnnotationView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, AnnotationView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		AnnotationView view = (AnnotationView) part.getObject();
		String genomeName = parameters.get(NavigationManagement.GENOME);
		Genome genome = Genome.loadGenome(genomeName);
		view.setData(genome);

		if (parameters.containsKey(NavigationManagement.SEARCH)) {
			view.getTxtSearch().setText(parameters.get(NavigationManagement.SEARCH));
			view.getBioCondsToDisplay().clear();
			view.getSelectedGenes().clear();
			view.getTableBioCondition().getTable().deselectAll();
			for (String[] row : view.getBioConds()) {
				boolean found = false;
				for (String cell : row) {
					if (cell.contains(view.getTxtSearch().getText())) {
						found = true;
					}
				}
				if (found) {
					view.getBioCondsToDisplay().add(row);
				}
			}
			view.getTableBioCondition().refresh();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnSaveTxt) {
			ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<>();
			if (selectedGenes.size() != 0) {
				for (String[] row : bioConds) {
					String gene = row[columnNames.indexOf("Locus tag") - 1];
					if (!gene.equals("")) {
						if (selectedGenes.contains(gene)) {
							bioCondsToDisplayTemp.add(row);
						}
					}
				}
			} else {
				bioCondsToDisplayTemp = bioCondsToDisplay;
			}
			String[] headers = new String[columnNames.size() - 1];
			for (int i = 1; i < columnNames.size(); i++) {
				headers[i - 1] = columnNames.get(i);
			}
			bioCondsToDisplayTemp.add(0, headers);
			String arrayRep = ArrayUtils.toString(bioCondsToDisplayTemp);
			if (bioCondsToDisplayTemp.size() < 100) {
				String[][] array = ArrayUtils.toArray(bioCondsToDisplayTemp);
				String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(array);
				SaveFileUtils.saveTextFile("Listeria_Annotation_Table_" + genome.getSpecies() + ".txt", arrayRep, true,
						"", arrayRepHTML, partService, shell);
			} else {
				SaveFileUtils.saveTextFile("Listeria_Annotation_Table_" + genome.getSpecies() + ".txt", arrayRep, true,
						"", "", partService, shell);
			}
		} else if (e.getSource() == btnSelectall) {
			selectedGenes = new ArrayList<>();
			tableBioCondition.getTable().selectAll();
			for (int i : tableBioCondition.getTable().getSelectionIndices()) {
				String selectedGenome = tableBioCondition.getTable().getItem(i)
						.getText(columnNames.indexOf("Name") + 1);
				if (!selectedGenes.contains(selectedGenome)) {
					selectedGenes.add(selectedGenome);
				}
			}
			tableBioCondition.refresh();
		} else if (e.getSource() == btnUnselectall) {
			selectedGenes = new ArrayList<>();
			tableBioCondition.getTable().deselectAll();
			tableBioCondition.refresh();
		} else if (e.getSource() == btnHide) {
			tableBioCondition.getTable().deselectAll();
			selectedGenes.clear();
			TreeSet<String> includeElements = new TreeSet<>();
			TreeSet<String> excludeElements = new TreeSet<>();
			SelectGenomeElementDialog dialog = new SelectGenomeElementDialog(shell, partService, includeElements,
					excludeElements, Genome.EGDE_NAME);
			if (dialog.open() == 0) {
				ArrayList<String> includeRows = new ArrayList<>();
				for (String row : includeElements) {
					includeRows.add(row);
				}
				bioCondsToDisplay.clear();
				for (String[] row : bioConds) {
					String gene = row[columnNames.indexOf("Locus tag") - 1];
					if (!gene.equals("")) {
						if (includeRows.contains(gene)) {
							bioCondsToDisplay.add(row);
						}
					}
				}
				tableBioCondition.refresh();
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public String[][] getBioCondsArray() {
		return bioCondsArray;
	}

	public void setBioCondsArray(String[][] bioCondsArray) {
		this.bioCondsArray = bioCondsArray;
	}

	public ArrayList<String[]> getBioCondsToDisplay() {
		return bioCondsToDisplay;
	}

	public void setBioCondsToDisplay(ArrayList<String[]> bioCondsToDisplay) {
		this.bioCondsToDisplay = bioCondsToDisplay;
	}

	public ArrayList<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(ArrayList<String> columnNames) {
		this.columnNames = columnNames;
	}

	public TableViewer getTableBioCondition() {
		return tableBioCondition;
	}

	public void setTableBioCondition(TableViewer tableBioCondition) {
		this.tableBioCondition = tableBioCondition;
	}

	public Genome getGenome() {
		return genome;
	}

	public void setGenome(Genome genome) {
		this.genome = genome;
	}

	public Text getTxtSearch() {
		return txtSearch;
	}

	public void setTxtSearch(Text txtSearch) {
		this.txtSearch = txtSearch;
	}

	public ArrayList<String[]> getBioConds() {
		return bioConds;
	}

	public void setBioConds(ArrayList<String[]> bioConds) {
		this.bioConds = bioConds;
	}

	public ArrayList<String> getSelectedGenes() {
		return selectedGenes;
	}

	public void setSelectedGenes(ArrayList<String> selectedGenes) {
		this.selectedGenes = selectedGenes;
	}

	public String getViewID() {
		return viewID;
	}

	public void setViewID(String viewID) {
		this.viewID = viewID;
	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}
}
