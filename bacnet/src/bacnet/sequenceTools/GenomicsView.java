package bacnet.sequenceTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
import bacnet.Database;
import bacnet.datamodel.phylogeny.Phylogenomic;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.BioConditionComparator;
import bacnet.utils.ArrayUtils;
import bacnet.utils.BasicColor;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;
import bacnet.utils.HTMLUtils;
import bacnet.utils.ImageMagick;
import bacnet.utils.RWTUtils;
import bacnet.views.HelpPage;

public class GenomicsView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 497572638968902304L;

	public static final String ID = "bacnet.GenomicsView"; //$NON-NLS-1$

	/**
	 * Indicates if we focus the view, so we can pushState navigation
	 */
	private boolean focused = false;

	private Composite compositeSummary;
	private TableViewer tableGenomeViewer;
	private BioConditionComparator comparatorBioCondition;
	private Table tableGenome;
	private ArrayList<String> selectedGenomes = new ArrayList<>();
	@SuppressWarnings("unused")
	private String url = "";

	/**
	 * Array used to init data
	 */
	private String[][] bioCondsArray;
	private ArrayList<String[]> bioCondsToDisplay;

	private ArrayList<String> columnNames = new ArrayList<>();
	private Label lblXxSrnas;
	private Button btnHelp;

	private Browser browserPhylo;
	private Button btnSavePng;
	private Button btnSaveSVG;
	private Button btnSaveTxt;

	@Inject
	EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	private Button btnSelectall;
	private Button btnUnselectall;
	private Text txtSearch;

	@Inject
	public GenomicsView() {

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
		container.setBounds(0, 10, 1081, 624);
		container.setLayout(new GridLayout(4, false));
		lblXxSrnas = new Label(container, SWT.BORDER | SWT.CENTER);
		lblXxSrnas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblXxSrnas.setFont(SWTResourceManager.getTitleFont());
		lblXxSrnas.setText("XX Listeria Complete Genomes");
		lblXxSrnas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnHelp = new Button(container, SWT.NONE);
		btnHelp.setToolTipText("How to use Genomics panel");
		btnHelp.setImage(ResourceManager.getPluginImage("bacnet", "icons/help.png"));
		btnHelp.addSelectionListener(this);

		Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new GridLayout(3, false));

		Label lblSaveAs = new Label(composite_1, SWT.NONE);
		lblSaveAs.setText("Download phylogenomic tree as ");
		lblSaveAs.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		btnSavePng = new Button(composite_1, SWT.NONE);
		btnSavePng.setToolTipText("Download as PNG image");
		btnSavePng.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/png.bmp"));

		btnSaveSVG = new Button(composite_1, SWT.NONE);
		btnSaveSVG.setToolTipText("Download as SVG vector image (for Illustrator, GIMP, ...)");
		btnSaveSVG.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/svg.bmp"));
		btnSavePng.addSelectionListener(this);
		btnSaveSVG.addSelectionListener(this);
		ScrolledComposite scrolledComposite = new ScrolledComposite(container,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 2));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		compositeSummary = new Composite(scrolledComposite, SWT.BORDER);
		compositeSummary.setLayout(new GridLayout(1, false));

		Composite composite_2 = new Composite(compositeSummary, SWT.NONE);
		composite_2.setLayout(new GridLayout(10, false));

		txtSearch = new Text(composite_2, SWT.BORDER);
		txtSearch.setText("");
		GridData gd_txtSearch = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtSearch.widthHint = 131;
		txtSearch.setLayoutData(gd_txtSearch);

		Label lblSearch = new Label(composite_2, SWT.NONE);
		lblSearch.setText("Search");
		lblSearch.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		new Label(composite_2, SWT.NONE);

		btnSelectall = new Button(composite_2, SWT.NONE);
		btnSelectall.setToolTipText("Select all genomes");
		btnSelectall.setImage(ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
		btnSelectall.addSelectionListener(this);
		Label lblSelectAll = new Label(composite_2, SWT.NONE);
		lblSelectAll.setText("Select all");
		lblSelectAll.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		btnUnselectall = new Button(composite_2, SWT.NONE);
		btnUnselectall.setToolTipText("Unselect all genomes");
		btnUnselectall.setImage(ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
		btnUnselectall.addSelectionListener(this);
		Label lblUnselectAll = new Label(composite_2, SWT.NONE);
		lblUnselectAll.setText("Unselect all");
		lblUnselectAll.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		new Label(composite_2, SWT.NONE);

		btnSaveTxt = new Button(composite_2, SWT.NONE);
		btnSaveTxt.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
		btnSaveTxt.setToolTipText("Download the table in tabulated text format");

		Label lblSaveSelectionAs = new Label(composite_2, SWT.NONE);
		lblSaveSelectionAs.setText("Download genome selection as a table");
		lblSaveSelectionAs.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
		btnSaveTxt.addSelectionListener(this);
		Label lblClickOneTime = new Label(compositeSummary, SWT.NONE);
		lblClickOneTime.setText("Select strain to highlight. Double click to acces strain information");
		lblClickOneTime.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
		{
			tableGenomeViewer = new TableViewer(compositeSummary, SWT.BORDER | SWT.MULTI);
			tableGenome = tableGenomeViewer.getTable();
			tableGenome.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			RWTUtils.setMarkup(tableGenome);
			tableGenomeViewer.getTable().setHeaderVisible(true);
			tableGenomeViewer.getTable().setLinesVisible(true);
			tableGenomeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {

					for (int i : tableGenome.getSelectionIndices()) {
						String selectedGenome = tableGenome.getItem(i).getText(columnNames.indexOf("Name") + 1);
						selectedGenome = GenomeNCBI.processGenomeName(selectedGenome);
						if (selectedGenomes.contains(selectedGenome)) {
							if (tableGenome.getSelectionIndices().length == 1) {
								selectedGenomes.remove(selectedGenome);
								tableGenomeViewer.replace(tableGenomeViewer.getTable().getItem(i).getData(), i);
							}
						} else {
							selectedGenomes.add(selectedGenome);
							tableGenomeViewer.replace(tableGenomeViewer.getTable().getItem(i).getData(), i);
						}
					}
					if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT
							|| Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT
							|| Database.getInstance().getProjectName() == Database.YERSINIOMICS_PROJECT) {
						loadPhylogenomicFigure(selectedGenomes);
					}
				}
			});
			tableGenomeViewer.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					String selectedGenome = tableGenomeViewer.getTable()
							.getItem(tableGenomeViewer.getTable().getSelectionIndex())
							.getText(columnNames.indexOf("Name") + 1);
					System.out.println("Select: " + selectedGenome);
					Genome genome = Genome.loadGenome(selectedGenome);

					Gene gene = genome.getFirstChromosome().getGenes().values().iterator().next();
					GeneView.displayGene(gene, partService);
				}
			});
			txtSearch.addKeyListener(new KeyListener() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void keyReleased(KeyEvent e) {
					if (e.keyCode == 16777296 || e.keyCode == 13) {
						if (!txtSearch.getText().equals("")) {
							searchText();
						}
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {
				}
			});
		}
		scrolledComposite.setContent(compositeSummary);
		scrolledComposite.setMinSize(compositeSummary.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		{
			ScrolledComposite scrolledCompositeCanvas = new ScrolledComposite(container,
					SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			GridData gd_scrolledCompositeCanvas = new GridData(SWT.LEFT, SWT.TOP, false, true, 1, 1);
			gd_scrolledCompositeCanvas.widthHint = 420;
			scrolledCompositeCanvas.setLayoutData(gd_scrolledCompositeCanvas);
			scrolledCompositeCanvas.setExpandHorizontal(true);
			scrolledCompositeCanvas.setExpandVertical(true);

			Composite composite = new Composite(scrolledCompositeCanvas, SWT.NONE);
			GridLayout gl_composite = new GridLayout(1, false);
			gl_composite.verticalSpacing = 0;
			gl_composite.marginWidth = 0;
			gl_composite.marginHeight = 0;
			composite.setLayout(gl_composite);

			browserPhylo = new Browser(composite, SWT.NONE);
			GridData gd_browserPhylo = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
			gd_browserPhylo.heightHint = 1000;
			gd_browserPhylo.widthHint = 700;
			browserPhylo.setLayoutData(gd_browserPhylo);
			scrolledCompositeCanvas.setContent(composite);
			scrolledCompositeCanvas.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}

		setData();
	}

	/**
	 * Set all starting variables
	 */
	private void setData() {
		/*
		 * Add genomes
		 */
		url = Phylogenomic.PHYLO_GENOME_SVG;
		File file = new File(url);
		if (file.exists()) {
			loadPhylogenomicFigure(new ArrayList<>());
		}else {
			System.out.println("cannot find : "+url);
		}

		/*
		 * Load Table
		 */
		file = new File(Database.getInstance().getGenomeArrayPath());
		if(file.exists()) {
			bioCondsArray = TabDelimitedTableReader.read(Database.getInstance().getGenomeArrayPath());
			bioCondsToDisplay = TabDelimitedTableReader.readList(Database.getInstance().getGenomeArrayPath(), true, true);
			bioCondsToDisplay.remove(0);
			updateGenomeTable();
			lblXxSrnas.setText((bioCondsArray.length - 1) + " " + Database.getInstance().getSpecies() + " complete genomes");
		}else {
			System.out.println("Cannot read: "+Database.getInstance().getGenomeArrayPath());
		}

	}

	/**
	 * Triggered by the search Text
	 */
	private void searchText() {
		tableGenomeViewer.refresh();
		pushState();
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
	 * Push genome, chromosome, gene and Tabitem state
	 */
	public void pushState() {
		HashMap<String, String> parameters = new HashMap<>();
		if (this.txtSearch.getText() != "") {
			parameters.put(NavigationManagement.SEARCH, this.txtSearch.getText());
			NavigationManagement.pushStateView(GenomicsView.ID, parameters);
		} else {
			NavigationManagement.pushStateView(GenomicsView.ID);
		}
	}

	/**
	 * Update Table for Biological Condition
	 * 
	 * @param bioConds
	 */
	private void updateGenomeTable() {

		setColumnNames();
		for (TableColumn col : tableGenomeViewer.getTable().getColumns()) {
			col.dispose();
		}
		tableGenomeViewer.setContentProvider(new ArrayContentProvider());

		createColumns();

		tableGenomeViewer.getTable().setHeaderVisible(true);
		tableGenomeViewer.getTable().setLinesVisible(true);
		/*
		 * Remove first row
		 */
		tableGenomeViewer.setInput(bioCondsToDisplay);
		comparatorBioCondition = new BioConditionComparator(columnNames);
		tableGenomeViewer.setComparator(comparatorBioCondition);
		for (int i = 0; i < tableGenomeViewer.getTable().getColumnCount(); i++) {
			tableGenomeViewer.getTable().getColumn(i).pack();
		}
	}

	/**
	 * Add the name of the columns
	 */
	private void setColumnNames() {
		columnNames = new ArrayList<>();
		for (String title : bioCondsArray[0]) {
			columnNames.add(title);
		}
	}

	private TableViewerColumn createTableViewerColumn(String title, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableGenomeViewer, SWT.NONE);
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
			private static final long serialVersionUID = -70923942574212205L;

			@Override
			public void update(ViewerCell cell) {
				String[] bioCond = (String[]) cell.getElement();
				Image image = null;
				if (selectedGenomes.contains(GenomeNCBI.processGenomeName(bioCond[1]))) {
					image = ResourceManager.getPluginImage("bacnet", "icons/checked.bmp");
				} else {
					image = ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp");
				}
				cell.setText("");
				cell.setImage(image);
				Color colorBack = BasicColor.LIGHTGREY;
				int rowIndex = Integer.parseInt(bioCond[0]);
				if (rowIndex % 2 == 0) {
					colorBack = BasicColor.WHITE;
				}
				cell.setBackground(colorBack);
			}
		});

		for (int i = 0; i < bioCondsArray[0].length; i++) {
			TableViewerColumn col2 = createTableViewerColumn(bioCondsArray[0][i], i + 1);
			col2.setLabelProvider(new CellLabelProvider() {

				/**
				 * 
				 */
				private static final long serialVersionUID = -8247269722692614416L;

				@Override
				public void update(ViewerCell cell) {
					String[] bioCond = (String[]) cell.getElement();
					String text = bioCond[cell.getColumnIndex() - 1];
					String colName = bioCondsArray[0][cell.getColumnIndex() - 1];
					if (colName.equals("Reference")) {
						RWTUtils.setPubMedLink(text);
					} else if (colName.equals("Sequence ID")) {
						cell.setText("<a href='https://www.ncbi.nlm.nih.gov/nuccore/" + text + "' target='_blank'>"
								+ text + "</a>");
					} else {
						cell.setText(text);
					}
					Color colorBack = BasicColor.LIGHTGREY;
					int rowIndex = Integer.parseInt(bioCond[0]);
					if (rowIndex % 2 == 0) {
						colorBack = BasicColor.WHITE;
					}
					if (!txtSearch.getText().equals("")) {
						if (bioCond[cell.getColumnIndex() - 1].contains(txtSearch.getText())) {
							colorBack = BasicColor.YELLOW;
						}
					}
					cell.setBackground(colorBack);
				}
			});
			col2.getColumn().pack();
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
				comparatorBioCondition.setColumn(index - 1);
				int dir = comparatorBioCondition.getDirection();
				tableGenomeViewer.getTable().setSortDirection(dir);
				tableGenomeViewer.getTable().setSortColumn(column);
				tableGenomeViewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * Load SVG figure of phylogeny and replace all strain name by homolog
	 * informations<br>
	 * Save to JPG file and display it
	 */
	public void loadPhylogenomicFigure(ArrayList<String> genomeNames) {
		/*
		 * Replace strain name by homolog info
		 */
		String textSVG = getPhyloFigure(genomeNames);

		/*
		 * Display homolog figure
		 */
		try {
			File tempSVGFile = File.createTempFile("Highlightstrain", "Phylogeny.svg");
			FileUtils.saveText(textSVG, tempSVGFile.getAbsolutePath());
			String html = SaveFileUtils.modifyHTMLwithFile(tempSVGFile.getAbsolutePath(), HTMLUtils.SVG);
			// System.out.println(textSVG);
			browserPhylo.setText(html);
			browserPhylo.redraw();
			tempSVGFile.deleteOnExit();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * DO NOT WORK WITH NEW VERSION OF FIGTREE Return the text file of the SVG
	 * figure, modified with highlighted strains
	 * 
	 * @param genomeNames
	 * @return
	 */
	public String getPhyloFigure(ArrayList<String> genomeNames) {
		/*
		 * Replace strain name by homolog info
		 */
		String textSVG = FileUtils.readText(Phylogenomic.getPhylogenomicFigurePath());
		HashMap<String, String> genomeToAttribute = Phylogenomic.parsePhylogenomicFigure(textSVG);

		/*
		 * Highlight selected strain
		 */
		for (String genome : genomeNames) {
			String lineAttribute = genomeToAttribute.get(genome);
			int indexOfLine = textSVG.indexOf(lineAttribute);
			int indexOfstyle = lineAttribute.indexOf("style");
			int lengthStyle = "style=\"".length();
			int posToADD = indexOfLine + indexOfstyle + lengthStyle;
			String textToADD = "fill:purple; ";
			textSVG = textSVG.substring(0, posToADD) + textToADD + textSVG.substring(posToADD, textSVG.length());
		}
		return textSVG;
	}

	/**
	 * Display a search in Genomic View
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void displayGenomeView(EPartService partService, HashMap<String, String> parameters) {
		partService.showPart(ID, PartState.ACTIVATE);
		// update data
		MPart part = partService.findPart(ID);
		NavigationManagement.pushStateView(ID, parameters);
		GenomicsView view = (GenomicsView) part.getObject();
		view.getTxtSearch().setText(parameters.get(NavigationManagement.SEARCH));
		view.searchText();
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnHelp) {
			HelpPage.helpGenomeView(partService);
		} else if (e.getSource() == btnSavePng) {
			String textSVG = getPhyloFigure(selectedGenomes);
			try {
				File tempSVGFile = File.createTempFile("Highlightstrain", "Phylogeny.svg");
				FileUtils.saveText(textSVG, tempSVGFile.getAbsolutePath());
				File tempPNGFile = File.createTempFile("Highlightstrain", "Phylogeny.png");
				System.out.println(
						"Convert Phylogeny.svg to Phylogeny.png\nHave you set ImageMagick PATH in ImageMagick.getConvertPATH()\nYours is set to: "
								+ ImageMagick.getConvertPATH());

				CMD.runProcess(ImageMagick.getConvertPATH() + " " + tempSVGFile.getAbsolutePath() + " " + tempPNGFile);
				SaveFileUtils.saveFile("Listeria_Phylogenomic_Tree.png", tempPNGFile, "PNG image file", partService,
						shell);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getSource() == btnSaveSVG) {
			String textSVG = getPhyloFigure(selectedGenomes);
			SaveFileUtils.saveTextFile("Listeria_Phylogenomic_Tree.svg", textSVG, true, "SVG (vector image) file",
					textSVG, partService, shell);
		} else if (e.getSource() == btnSaveTxt) {
			String[][] arrayToSave = new String[1][columnNames.size()];
			for (int i = 0; i < columnNames.size(); i++) {
				arrayToSave[0][i] = columnNames.get(i);
			}
			int k = 1;
			for (int i = 1; i < bioCondsArray.length; i++) {
				String genomeName = bioCondsArray[i][1];
				if (selectedGenomes.contains(GenomeNCBI.processGenomeName(genomeName))) {
					arrayToSave = ArrayUtils.addRow(arrayToSave, ArrayUtils.getRow(bioCondsArray, i), k);
					k++;
				}
			}
			String arrayRep = ArrayUtils.toString(arrayToSave);
			String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(arrayToSave);
			SaveFileUtils.saveTextFile("Listeria_Genomic_Table.txt", arrayRep, true, "", arrayRepHTML, partService,
					shell);
		} else if (e.getSource() == btnSelectall) {
			selectedGenomes = new ArrayList<>();
			tableGenome.selectAll();
			for (int i : tableGenome.getSelectionIndices()) {
				String selectedGenome = GenomeNCBI
						.processGenomeName(tableGenome.getItem(i).getText(columnNames.indexOf("Name") + 1));
				if (!selectedGenomes.contains(selectedGenome)) {
					selectedGenomes.add(selectedGenome);
				}
			}
			loadPhylogenomicFigure(selectedGenomes);
			tableGenomeViewer.refresh();
		} else if (e.getSource() == btnUnselectall) {
			selectedGenomes = new ArrayList<>();
			tableGenome.deselectAll();
			loadPhylogenomicFigure(selectedGenomes);
			tableGenomeViewer.refresh();
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

	public Text getTxtSearch() {
		return txtSearch;
	}

	public void setTxtSearch(Text txtSearch) {
		this.txtSearch = txtSearch;
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

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

}
