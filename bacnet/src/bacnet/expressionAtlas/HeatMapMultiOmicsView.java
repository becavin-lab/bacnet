package bacnet.expressionAtlas;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.HTMLUtils;
import bacnet.utils.ListUtils;

public class HeatMapMultiOmicsView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2640442866422682301L;

	public static final String ID = "bacnet.HeatMapMultiOmicsView"; //$NON-NLS-1$

	/**
	 * Current viewId = ID + math.random()
	 */
	private String viewId = "";

	public static final String HEATMAP_NAME = "HeatMap";
	public static final String SCATTER_NAME = "Scatter plot";

	/**
	 * Indicates if we focus the view, so we can pushState navigation
	 */
	private boolean focused = false;

	private String sequence = "";
	private String genomeName = Genome.EGDE_NAME;
	private Composite compositeSummary;
	private TableCompositeHeatMap tableComposite;
	private Text txtCutoffLogFC;
	private Button btnUpdateCutoff;
	private Button btnPrint;
	private Composite compFilter;
	private Button btnDnDNA;
	private Button btnUpDNA;
	private Button btnNoDNA;
	private Button btnUpRNA;
	private Button btnDnRNA;
	private Button btnUpProt;
	private Button btnDnProt;
	private Button btnNoProt;
	private Button btnNoRNA;
	private ArrayList<Button> allButtons = new ArrayList<>();

	/**
	 * Describe comportment of list overlap
	 */
	private boolean intersect = true;

	/**
	 * Multiomics matrix
	 */
	// private ExpressionMatrix omicsMatrix = new ExpressionMatrix();
	/**
	 * LogFC Matrix = Multiomics with only the logFC values
	 */
	private ExpressionMatrix logfcOmicsMatrix = new ExpressionMatrix();
	/**
	 * List of genes up for DNA
	 */
	private ArrayList<String> upDNAList = new ArrayList<>();
	/**
	 * List of genes up for RNA
	 */
	private ArrayList<String> upRNAList = new ArrayList<>();
	/**
	 * List of genes up for Prot
	 */
	private ArrayList<String> upProtList = new ArrayList<>();
	/**
	 * List of genes down for DNA
	 */
	private ArrayList<String> downDNAList = new ArrayList<>();
	/**
	 * List of genes down for RNA
	 */
	private ArrayList<String> downRNAList = new ArrayList<>();
	/**
	 * List of genes down for Prot
	 */
	private ArrayList<String> downProtList = new ArrayList<>();
	/**
	 * List of genes no change for DNA
	 */
	private ArrayList<String> noDNAList = new ArrayList<>();
	/**
	 * List of genes no change for RNA
	 */
	private ArrayList<String> noRNAList = new ArrayList<>();
	/**
	 * List of genes no change for Prot
	 */
	private ArrayList<String> noProtList = new ArrayList<>();

	@Inject
	EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	private TabFolder tabFolder;
	private TabItem tbtmHeatmap;
	private TabItem tbtmScatterPlot;
	private Composite composite;
	private Button btnListUnion;
	private Button btnListIntersection;
	private Combo cmbYaxis;
	private Combo cmbXaxis;
	private Browser browserGraph;
	private Button btnSavePNG;

	@Inject
	public HeatMapMultiOmicsView() {
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
		container.setBounds(0, 0, 721, 563);
		container.setLayout(new GridLayout(1, false));

		{
			ScrolledComposite scrolledComposite = new ScrolledComposite(container,
					SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.setExpandVertical(true);

			{
				compositeSummary = new Composite(scrolledComposite, SWT.BORDER);
				compositeSummary.setLayout(new GridLayout(2, false));

				compFilter = new Composite(compositeSummary, SWT.NONE);
				compFilter.setLayout(new GridLayout(1, false));
				GridData gd_compFilter = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
				gd_compFilter.widthHint = 270;
				compFilter.setLayoutData(gd_compFilter);

				Label lblFilterMultiomicsExperiment = new Label(compFilter, SWT.NONE);
				lblFilterMultiomicsExperiment.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
				lblFilterMultiomicsExperiment.setText("Filter multi-omics experiment");
				lblFilterMultiomicsExperiment.setFont(SWTResourceManager.getTitleFont());
				lblFilterMultiomicsExperiment.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
				new Label(compFilter, SWT.NONE);

				Composite composite_6 = new Composite(compFilter, SWT.NONE);
				composite_6.setLayout(new GridLayout(1, false));

				Label lblCombineListsUsing = new Label(composite_6, SWT.NONE);
				lblCombineListsUsing.setText("Combine lists using:");

				Composite composite_7 = new Composite(composite_6, SWT.NONE);
				composite_7.setLayout(new GridLayout(2, false));

				btnListIntersection = new Button(composite_7, SWT.RADIO);
				btnListIntersection.setSelection(true);
				btnListIntersection.setText("List intersection");
				btnListIntersection.addSelectionListener(this);
				btnListUnion = new Button(composite_7, SWT.RADIO);
				btnListUnion.setText("List union");
				btnListUnion.addSelectionListener(this);

				Label lblFilterDnaseq = new Label(compFilter, SWT.NONE);
				lblFilterDnaseq.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				lblFilterDnaseq.setText("Filter DNASeq");
				lblFilterDnaseq.setFont(SWTResourceManager.getTitleFont());
				lblFilterDnaseq.setBackground(SWTResourceManager.getColor(232, 232, 232));

				Composite composite_2 = new Composite(compFilter, SWT.NONE);
				composite_2.setLayout(new GridLayout(1, false));
				composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

				btnUpDNA = new Button(composite_2, SWT.CHECK);
				btnUpDNA.setText("+ change of CNV");
				btnUpDNA.addSelectionListener(this);
				btnDnDNA = new Button(composite_2, SWT.CHECK);
				btnDnDNA.setText("- change of CNV");
				btnDnDNA.addSelectionListener(this);
				btnNoDNA = new Button(composite_2, SWT.CHECK);
				btnNoDNA.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				btnNoDNA.setText("No change of CNV");
				btnNoDNA.addSelectionListener(this);
				Label lblFilterRnaseq = new Label(compFilter, SWT.NONE);
				lblFilterRnaseq.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				lblFilterRnaseq.setText("Filter RNASeq");
				lblFilterRnaseq.setFont(SWTResourceManager.getTitleFont());
				lblFilterRnaseq.setBackground(SWTResourceManager.getColor(232, 232, 232));

				Composite composite_3 = new Composite(compFilter, SWT.NONE);
				composite_3.setLayout(new GridLayout(1, false));

				btnUpRNA = new Button(composite_3, SWT.CHECK);
				btnUpRNA.setText("+ change of RNA expression");
				btnUpRNA.addSelectionListener(this);
				btnDnRNA = new Button(composite_3, SWT.CHECK);
				btnDnRNA.setText("- change of RNA expression");
				btnDnRNA.addSelectionListener(this);
				btnNoRNA = new Button(composite_3, SWT.CHECK);
				btnNoRNA.setText("No change of RNA expression");
				btnNoRNA.addSelectionListener(this);
				Label lblFilterProteomic = new Label(compFilter, SWT.NONE);
				lblFilterProteomic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				lblFilterProteomic.setText("Filter Proteomic");
				lblFilterProteomic.setFont(SWTResourceManager.getTitleFont());
				lblFilterProteomic.setBackground(SWTResourceManager.getColor(232, 232, 232));

				Composite composite_4 = new Composite(compFilter, SWT.NONE);
				composite_4.setLayout(new GridLayout(1, false));

				btnUpProt = new Button(composite_4, SWT.CHECK);
				btnUpProt.setText("+ change of Protein production");
				btnUpProt.addSelectionListener(this);
				btnDnProt = new Button(composite_4, SWT.CHECK);
				btnDnProt.setText("- change of Protein production");
				btnDnProt.addSelectionListener(this);
				btnNoProt = new Button(composite_4, SWT.CHECK);
				btnNoProt.setText("No change of Protein production");
				btnNoProt.addSelectionListener(this);

				tabFolder = new TabFolder(compositeSummary, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				tabFolder.addSelectionListener(this);

				tbtmHeatmap = new TabItem(tabFolder, SWT.NONE);
				tbtmHeatmap.setText(HEATMAP_NAME);

				{
					tableComposite = new TableCompositeHeatMap(tabFolder, SWT.NONE, genomeName, false, partService,
							shell);
					tbtmHeatmap.setControl(tableComposite);

				}

				tbtmScatterPlot = new TabItem(tabFolder, SWT.NONE);
				tbtmScatterPlot.setText(SCATTER_NAME);

				composite = new Composite(tabFolder, SWT.NONE);
				tbtmScatterPlot.setControl(composite);
				composite.setLayout(new GridLayout(1, false));

				Composite composite_1 = new Composite(composite, SWT.NONE);
				composite_1.setLayout(new GridLayout(3, false));

				Label lblChoosDataFor = new Label(composite_1, SWT.NONE);
				GridData gd_lblChoosDataFor = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_lblChoosDataFor.widthHint = 182;
				lblChoosDataFor.setLayoutData(gd_lblChoosDataFor);
				lblChoosDataFor.setText("Choose data for xAxis");

				Label lblChooseDataFor = new Label(composite_1, SWT.NONE);
				GridData gd_lblChooseDataFor = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_lblChooseDataFor.widthHint = 186;
				lblChooseDataFor.setLayoutData(gd_lblChooseDataFor);
				lblChooseDataFor.setText("Choose data for Yaxis");
				new Label(composite_1, SWT.NONE);

				cmbXaxis = new Combo(composite_1, SWT.NONE);
				cmbXaxis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				cmbYaxis = new Combo(composite_1, SWT.NONE);
				cmbYaxis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				cmbXaxis.addSelectionListener(this);
				cmbYaxis.addSelectionListener(this);
				btnSavePNG = new Button(composite_1, SWT.NONE);
				btnSavePNG.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/png.bmp"));
				btnSavePNG.addSelectionListener(this);

				Composite composite_5 = new Composite(composite, SWT.NONE);
				composite_5.setLayout(new GridLayout(1, false));
				composite_5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

				browserGraph = new Browser(composite_5, SWT.NONE);
				browserGraph.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			}
			scrolledComposite.setContent(compositeSummary);
			scrolledComposite.setMinSize(compositeSummary.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			allButtons.add(btnListIntersection);
			allButtons.add(btnListUnion);
			allButtons.add(btnDnDNA);
			allButtons.add(btnUpDNA);
			allButtons.add(btnNoDNA);
			allButtons.add(btnUpRNA);
			allButtons.add(btnDnRNA);
			allButtons.add(btnUpProt);
			allButtons.add(btnDnProt);
			allButtons.add(btnNoProt);
			allButtons.add(btnNoRNA);

		}

	}

	public static class InitOmicsMatrixThread implements IRunnableWithProgress {

		HeatMapMultiOmicsView view;

		public InitOmicsMatrixThread(HeatMapMultiOmicsView view) {
			this.view = view;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Display Multi-Omics view", 2);
			monitor.worked(1);
			monitor.subTask("Load Multi-Omics matrix");
			this.view.initData(Genome.Donovani_NAME);
			monitor.worked(1);
			monitor.done();
		}
	}

	private void initData(String genomeName) {
		this.setGenomeName(genomeName);
		this.setLogfcOmicsMatrix(ExpressionMatrix.load(Database.getMULTIOMICS_MATRIX_PATH() + "_" + genomeName));

		/*
		 * Omics Heatmap
		 */
		this.setUpDNAList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_Up_DNA_" + Genome.Donovani_NAME));
		this.setUpRNAList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_Up_RNA_" + Genome.Donovani_NAME));
		this.setUpProtList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_Up_Prot_" + Genome.Donovani_NAME));

		this.setDownDNAList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_Down_DNA_" + Genome.Donovani_NAME));
		this.setDownRNAList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_Down_RNA_" + Genome.Donovani_NAME));
		this.setDownProtList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_Down_Prot_" + Genome.Donovani_NAME));

		this.setNoDNAList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_No_DNA_" + Genome.Donovani_NAME));
		this.setNoRNAList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_No_RNA_" + Genome.Donovani_NAME));
		this.setNoProtList(TabDelimitedTableReader
				.readList(Database.getMULTIOMICS_MATRIX_PATH() + "_No_Prot_" + Genome.Donovani_NAME));

	}

	private void initView(String genomeName) {

		tableComposite.initData(this.getLogfcOmicsMatrix());
		tableComposite.setGenomeName(genomeName);

		/*
		 * Scatter plot
		 */
		for (String header : this.getLogfcOmicsMatrix().getHeaders()) {
			cmbXaxis.add(header);
			cmbYaxis.add(header);
		}
		cmbXaxis.select(0);
		cmbYaxis.select(2);
	}

	/**
	 * Update the list of rows to exclude from the display
	 */
	public void updateDisplayedElements() {
		boolean noneSelected = true;
		ArrayList<ArrayList<String>> listOfList = new ArrayList<>();
		if (btnUpDNA.getSelection()) {
			listOfList.add(upDNAList);
			noneSelected = false;
		}
		if (btnUpRNA.getSelection()) {
			listOfList.add(upRNAList);
			noneSelected = false;
		}
		if (btnUpProt.getSelection()) {
			listOfList.add(upProtList);
			noneSelected = false;
		}

		if (btnDnDNA.getSelection()) {
			listOfList.add(downDNAList);
			noneSelected = false;
		}
		if (btnDnRNA.getSelection()) {
			listOfList.add(downRNAList);
			noneSelected = false;
		}
		if (btnDnProt.getSelection()) {
			listOfList.add(downProtList);
			noneSelected = false;
		}

		if (btnNoRNA.getSelection()) {
			listOfList.add(noDNAList);
			noneSelected = false;
		}
		if (btnNoRNA.getSelection()) {
			listOfList.add(noRNAList);
			noneSelected = false;
		}
		if (btnNoProt.getSelection()) {
			listOfList.add(noProtList);
			noneSelected = false;
		}

		ArrayList<String> displayGene = new ArrayList<>();
		if (noneSelected) {
			displayGene = this.getLogfcOmicsMatrix().getRowNamesToList();
		} else if (intersect) {
			displayGene = ListUtils.intersect(listOfList);
		} else {
			displayGene = ListUtils.union(listOfList);
		}
		System.out.println("Select : " + tabFolder.getItem(tabFolder.getSelectionIndex()).getText());

		if (tabFolder.getItem(tabFolder.getSelectionIndex()).getText().equals(HEATMAP_NAME)) {
			ArrayList<String> excludedGenomesElements = new ArrayList<>();
			for (String gene : this.getLogfcOmicsMatrix().getRowNamesToList()) {
				if (!displayGene.contains(gene)) {
					excludedGenomesElements.add(gene);
				}
			}
			tableComposite.setExcludeRow(excludedGenomesElements);
			tableComposite.updateInfo();
		} else {
			updateScatteRPlot(displayGene);

		}
		pushState();
	}

	private void updateScatteRPlot(ArrayList<String> displayGene) {
		try {
			String dataName = "omics" + Math.random() + ".txt";
			File file = File.createTempFile("omics" + Math.random(), ".txt");
			String dataPath = file.getAbsolutePath();
			ArrayList<String> pointList = new ArrayList<>();
			pointList.add("name\tX\tY\tannotation");
			String xAxis = cmbXaxis.getItem(cmbXaxis.getSelectionIndex());
			String yAxis = cmbYaxis.getItem(cmbYaxis.getSelectionIndex());
			for (String geneName : displayGene) {
				pointList.add(geneName + "\t" + this.getLogfcOmicsMatrix().getValue(geneName, xAxis) + "\t"
						+ this.getLogfcOmicsMatrix().getValue(geneName, yAxis) + "\t"
						+ this.getLogfcOmicsMatrix().getValueAnnotation(geneName, "Product"));
			}
			System.out.println("Omics data - " + dataPath);
			TabDelimitedTableReader.saveList(pointList, dataPath);

			/*
			 * Update scatterplot.html
			 */
			String html = SaveFileUtils.modifyHTMLwithFile(dataPath, HTMLUtils.SCATTER);
			browserGraph.setText(html);
			browserGraph.redraw();

		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
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
		Item item = tabFolder.getItem(tabFolder.getSelectionIndex());
		parameters.put(NavigationManagement.ITEM, item.getText());

		int buttonPressed = 1;
		for (Button button : allButtons) {
			if (button.getSelection()) {
				parameters.put(NavigationManagement.BUTTON + buttonPressed, button.getText());
				buttonPressed++;
			}
		}

		NavigationManagement.pushStateView(this.getViewId(), parameters);

	}

	/**
	 * Display the view with saved parameters
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void displayOmicsView(EPartService partService, HashMap<String, String> parameters) {
		String id = HeatMapMultiOmicsView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, HeatMapMultiOmicsView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		HeatMapMultiOmicsView view = (HeatMapMultiOmicsView) part.getObject();
		view.setViewId(id);
		try {
			InitOmicsMatrixThread thread = new InitOmicsMatrixThread(view);
			new ProgressMonitorDialog(view.getShell()).run(true, false, thread);
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		view.initView(Genome.Donovani_NAME);
		for (String stateId : parameters.keySet()) {
			String stateValue = parameters.get(stateId);
			if (stateId.equals(NavigationManagement.ITEM)) {
				if (!stateValue.equals("")) {
					for (TabItem tabItem : view.getTabFolder().getItems()) {
						if (tabItem.getText().equals(stateValue)) {
							view.getTabFolder().setSelection(view.getTabFolder().indexOf(tabItem));
						}
					}
				}
			}
			if (stateId.contains(NavigationManagement.BUTTON)) {
				for (Button button : view.getAllButtons()) {
					if (button.getText().equals(stateValue)) {
						button.setSelection(true);
					}
				}
			}
		}
		view.updateDisplayedElements();
	}

	/**
	 * Display the view with saved parameters
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void displayOmicsView(EPartService partService) {
		String id = HeatMapMultiOmicsView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, HeatMapMultiOmicsView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		HeatMapMultiOmicsView view = (HeatMapMultiOmicsView) part.getObject();
		view.setViewId(id);

		try {
			InitOmicsMatrixThread thread = new InitOmicsMatrixThread(view);
			new ProgressMonitorDialog(view.getShell()).run(true, false, thread);
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		view.initView(Genome.Donovani_NAME);
		view.pushState();
		view.getTableComposite().updateInfo();

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnListIntersection) {
			intersect = true;
			updateDisplayedElements();
		} else if (e.getSource() == btnListUnion) {
			intersect = false;
			updateDisplayedElements();
		} else if (e.getSource() == tabFolder) {
			System.out.println("Tabfolder");
			if (this.getLogfcOmicsMatrix().getNumberColumn() > 1) {
				updateDisplayedElements();
			}
		} else if (e.getSource() == btnSavePNG) {
			String jsScript = HTMLUtils.getPluginTextFile("bacnet", "html/printscreen.js");
			jsScript = jsScript.replaceFirst("_FileName", "ScatterPlot_" + Database.getInstance().getProjectName());
			browserGraph.execute(jsScript);
		} else {
			System.out.println("source: " + e.getSource());
			updateDisplayedElements();
		}

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public TableCompositeHeatMap getTableComposite() {
		return tableComposite;
	}

	public void setTableComposite(TableCompositeHeatMap tableComposite) {
		this.tableComposite = tableComposite;
	}

	public String getGenomeName() {
		return genomeName;
	}

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}

	public String getViewId() {
		return viewId;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public ExpressionMatrix getLogfcOmicsMatrix() {
		return logfcOmicsMatrix;
	}

	public void setLogfcOmicsMatrix(ExpressionMatrix logfcOmicsMatrix) {
		this.logfcOmicsMatrix = logfcOmicsMatrix;
	}

	// public ExpressionMatrix getOmicsMatrix() {
	// return omicsMatrix;
	// }
	//
	// public void setOmicsMatrix(ExpressionMatrix omicsMatrix) {
	// this.omicsMatrix = omicsMatrix;
	// }

	public Composite getCompositeSummary() {
		return compositeSummary;
	}

	public void setCompositeSummary(Composite compositeSummary) {
		this.compositeSummary = compositeSummary;
	}

	public Text getTxtCutoffLogFC() {
		return txtCutoffLogFC;
	}

	public void setTxtCutoffLogFC(Text txtCutoffLogFC) {
		this.txtCutoffLogFC = txtCutoffLogFC;
	}

	public Button getBtnUpdateCutoff() {
		return btnUpdateCutoff;
	}

	public void setBtnUpdateCutoff(Button btnUpdateCutoff) {
		this.btnUpdateCutoff = btnUpdateCutoff;
	}

	public Button getBtnPrint() {
		return btnPrint;
	}

	public void setBtnPrint(Button btnPrint) {
		this.btnPrint = btnPrint;
	}

	public Composite getCompFilter() {
		return compFilter;
	}

	public void setCompFilter(Composite compFilter) {
		this.compFilter = compFilter;
	}

	public Button getBtnDnDNA() {
		return btnDnDNA;
	}

	public void setBtnDnDNA(Button btnDnDNA) {
		this.btnDnDNA = btnDnDNA;
	}

	public Button getBtnPosDNA() {
		return btnUpDNA;
	}

	public void setBtnPosDNA(Button btnPosDNA) {
		this.btnUpDNA = btnPosDNA;
	}

	public Button getBtnNoDNA() {
		return btnNoDNA;
	}

	public void setBtnNoDNA(Button btnNoDNA) {
		this.btnNoDNA = btnNoDNA;
	}

	public Button getBtnUpRNA() {
		return btnUpRNA;
	}

	public void setBtnUpRNA(Button btnUpRNA) {
		this.btnUpRNA = btnUpRNA;
	}

	public Button getBtnDnRNA() {
		return btnDnRNA;
	}

	public void setBtnDnRNA(Button btnDnRNA) {
		this.btnDnRNA = btnDnRNA;
	}

	public Button getBtnUpProt() {
		return btnUpProt;
	}

	public void setBtnUpProt(Button btnUpProt) {
		this.btnUpProt = btnUpProt;
	}

	public Button getBtnDnProt() {
		return btnDnProt;
	}

	public void setBtnDnProt(Button btnDnProt) {
		this.btnDnProt = btnDnProt;
	}

	public Button getBtnNoProt() {
		return btnNoProt;
	}

	public void setBtnNoProt(Button btnNoProt) {
		this.btnNoProt = btnNoProt;
	}

	public Button getBtnNoRNA() {
		return btnNoRNA;
	}

	public void setBtnNoRNA(Button btnNoRNA) {
		this.btnNoRNA = btnNoRNA;
	}

	public ArrayList<String> getUpDNAList() {
		return upDNAList;
	}

	public void setUpDNAList(ArrayList<String> upDNAList) {
		this.upDNAList = upDNAList;
	}

	public ArrayList<String> getUpRNAList() {
		return upRNAList;
	}

	public void setUpRNAList(ArrayList<String> upRNAList) {
		this.upRNAList = upRNAList;
	}

	public ArrayList<String> getUpProtList() {
		return upProtList;
	}

	public void setUpProtList(ArrayList<String> upProtList) {
		this.upProtList = upProtList;
	}

	public ArrayList<String> getDownDNAList() {
		return downDNAList;
	}

	public void setDownDNAList(ArrayList<String> downDNAList) {
		this.downDNAList = downDNAList;
	}

	public ArrayList<String> getDownRNAList() {
		return downRNAList;
	}

	public void setDownRNAList(ArrayList<String> downRNAList) {
		this.downRNAList = downRNAList;
	}

	public ArrayList<String> getDownProtList() {
		return downProtList;
	}

	public void setDownProtList(ArrayList<String> downProtList) {
		this.downProtList = downProtList;
	}

	public ArrayList<String> getNoDNAList() {
		return noDNAList;
	}

	public void setNoDNAList(ArrayList<String> noDNAList) {
		this.noDNAList = noDNAList;
	}

	public ArrayList<String> getNoRNAList() {
		return noRNAList;
	}

	public void setNoRNAList(ArrayList<String> noRNAList) {
		this.noRNAList = noRNAList;
	}

	public ArrayList<String> getNoProtList() {
		return noProtList;
	}

	public boolean isIntersect() {
		return intersect;
	}

	public void setIntersect(boolean intersect) {
		this.intersect = intersect;
	}

	public void setNoProtList(ArrayList<String> noProtList) {
		this.noProtList = noProtList;
	}

	public EPartService getPartService() {
		return partService;
	}

	public void setPartService(EPartService partService) {
		this.partService = partService;
	}

	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public void setTabFolder(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
	}

	public TabItem getTbtmHeatmap() {
		return tbtmHeatmap;
	}

	public void setTbtmHeatmap(TabItem tbtmHeatmap) {
		this.tbtmHeatmap = tbtmHeatmap;
	}

	public TabItem getTbtmScatterPlot() {
		return tbtmScatterPlot;
	}

	public void setTbtmScatterPlot(TabItem tbtmScatterPlot) {
		this.tbtmScatterPlot = tbtmScatterPlot;
	}

	public Composite getComposite() {
		return composite;
	}

	public void setComposite(Composite composite) {
		this.composite = composite;
	}

	public ArrayList<Button> getAllButtons() {
		return allButtons;
	}

	public void setAllButtons(ArrayList<Button> allButtons) {
		this.allButtons = allButtons;
	}
}
