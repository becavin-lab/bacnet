package bacnet.sequenceTools;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.biojava3.core.sequence.Strand;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import bacnet.Database;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Genome.OpenGenomesThread;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.expressionAtlas.HeatMapTranscriptomicsView;
import bacnet.expressionAtlas.core.GenomeElementAtlas;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.genomeBrowser.core.Track;
import bacnet.genomeBrowser.tracksGUI.TrackCanvasGenome;
import bacnet.raprcp.NavigationManagement;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.ArrayUtils;
import bacnet.utils.BasicColor;
import bacnet.utils.Filter;
import bacnet.utils.RWTUtils;
import bacnet.views.HelpPage;

public class SrnaView implements SelectionListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2943857063947928839L;

	public static final String ID = "bacnet.SrnaView"; //$NON-NLS-1$

	/**
	 * Current viewId = ID + math.random()
	 */
	private String viewId = "";

	/**
	 * Indicates if we focus the view, so we can pushState navigation
	 */
	private boolean focused = false;

	/**
	 * Egde Genome loaded
	 */
	private Genome genome;
	private ArrayList<String> listSrnas = new ArrayList<>();
	private HashMap<String, String> sRNARefToLink = new HashMap<>();
	private Srna seq;

	private Label lblRef;
	private Text lblBegin;
	private Text lblEnd;
	private Text lblSizeBP;
	private Text lblSynonims;
	private Text lblStrand;
	private Text lblNote;
	private Composite composite_pvalue;
	private Text txtSeqnucleotide;
	private Composite composite_6;
	private Composite composite_7;
	private Table tableSrnas;
	private Label lblOrAnyOther;
	private ScrolledComposite scrolledComposite;
	private Composite composite_2;
	private Label lblGene;
	private Label lblPredictedSecondaryStructure;
	private Label lblStructure;
	private Button btnOpenStructure;
	private Table tableOtherInfo;
	private TrackCanvasGenome canvasGenome;
	private Label lblDescribedIn;
	private Label label_2;
	private Label label_3;
	private Text txtSearch;
	private Label lblNewLabel;

	/*
	 * Expression Atlas variables
	 */
	private String[][] arrayDataList = new String[0][0];
	private Composite composite;
	private Table tableOver;
	private Table tableUnder;
	private Table tableNoDiff;
	private Label lblOver;
	private Label lblUnder;
	private Label lblNodiff;
	private Button btnGenomeViewer;
	private Composite composite_3;
	private Composite composite_8_1;
	private TabItem tbtmGeneralInformation;
	private TabItem tbtmExpressionAtlas;
	private Button btnHeatmapview;
	private Button btnUpdateCutoff;
	private Text txtCutoffLogFC;
	private Text txtCutoffPvalue;
	private Button btnZoomplus;
	private Button btnZoomminus;
	private Button btnHelp;

	@Inject
	private EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	private Button btnDisplayNucleotideSequence;
	private TabFolder tabFolder;

	@Inject
	public SrnaView() {

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
		container.setBounds(0, 0, 758, 434);
		container.setLayout(new GridLayout(2, false));

		composite_7 = new Composite(container, SWT.BORDER);
		composite_7.setLayout(new GridLayout(1, false));
		composite_7.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));

		lblNewLabel = new Label(composite_7, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Search");
		lblNewLabel.setForeground(BasicColor.GREY);

		txtSearch = new Text(composite_7, SWT.BORDER);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearch.addKeyListener(new KeyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6692115140408345058L;

			@Override
			public void keyReleased(KeyEvent e) {
				if (txtSearch.getText().equals("")) {
					updateInfo();
				}
				if (e.keyCode == 16777296 || e.keyCode == 13) {
					System.out.println("Search for " + txtSearch.getText());
					ArrayList<String> searchResults = search(txtSearch.getText());
					if (searchResults.size() != 0) {
						listSrnas.clear();
						for (String geneName : searchResults)
							listSrnas.add(geneName);
						tableSrnas.removeAll();
						tableSrnas.setItemCount(listSrnas.size());
						tableSrnas.update();
						tableSrnas.select(listSrnas.indexOf(searchResults.get(0)));
						seq = (Srna) genome.getElement(searchResults.get(0));
						updateAllWidgets();
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});
		lblOrAnyOther = new Label(composite_7, SWT.NONE);
		lblOrAnyOther.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblOrAnyOther.setText("Select a non coding RNA");
		lblOrAnyOther.setForeground(BasicColor.GREY);

		tableSrnas = new Table(composite_7, SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
		GridData gd_tableSrnas = new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 1);
		gd_tableSrnas.widthHint = 160;
		tableSrnas.setLayoutData(gd_tableSrnas);
		tableSrnas.addListener(SWT.SetData, new Listener() {
			private static final long serialVersionUID = 6744063943372593076L;

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(listSrnas.get(index));
			}
		});
		tableSrnas.addSelectionListener(this);

		scrolledComposite = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		composite_2 = new Composite(scrolledComposite, SWT.BORDER);
		composite_2.setLayout(new GridLayout(2, false));

		lblGene = new Label(composite_2, SWT.BORDER | SWT.CENTER);
		GridData gd_lblGene = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		gd_lblGene.widthHint = 240;
		lblGene.setLayoutData(gd_lblGene);
		lblGene.setBackground(ResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblGene.setFont(ResourceManager.getTitleFont());
		lblGene.setText("gene");

		btnHelp = new Button(composite_2, SWT.NONE);
		btnHelp.setToolTipText("How to use Small RNA panel ?");
		btnHelp.setImage(ResourceManager.getPluginImage("bacnet", "icons/help.png"));
		btnHelp.addSelectionListener(this);

		tabFolder = new TabFolder(composite_2, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tabFolder.addSelectionListener(this);
		tbtmGeneralInformation = new TabItem(tabFolder, SWT.NONE);
		tbtmGeneralInformation.setText("General Information");

		Composite composite_4 = new Composite(tabFolder, SWT.NONE);
		tbtmGeneralInformation.setControl(composite_4);
		composite_4.setLayout(new GridLayout(2, false));
		Composite composite_10 = new Composite(composite_4, SWT.NONE);
		composite_10.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		composite_10.setLayout(new GridLayout(2, false));

		canvasGenome = new TrackCanvasGenome(composite_10, SWT.BORDER);
		GridData gd_canvasGenome = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2);
		gd_canvasGenome.heightHint = 125;
		canvasGenome.setLayoutData(gd_canvasGenome);
		canvasGenome.setLayout(new GridLayout(1, false));
		canvasGenome.addMouseListener(this);

		btnZoomplus = new Button(composite_10, SWT.NONE);
		btnZoomplus.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		btnZoomplus.setToolTipText("Zoom In horizontally");
		btnZoomplus.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/zoomIN.bmp"));
		btnZoomplus.addSelectionListener(this);
		btnZoomminus = new Button(composite_10, SWT.NONE);
		btnZoomminus.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnZoomminus.setToolTipText("Zoom Out horizontally");
		btnZoomminus.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/zoomOUT.bmp"));
		btnZoomminus.addSelectionListener(this);

		Composite composite_9 = new Composite(composite_4, SWT.BORDER);
		composite_9.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_9.setLayout(new GridLayout(2, false));

		lblBegin = new Text(composite_9, SWT.READ_ONLY);
		lblBegin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblBegin.setText("Begin");

		lblEnd = new Text(composite_9, SWT.READ_ONLY);
		lblEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblEnd.setText("End");

		lblSizeBP = new Text(composite_9, SWT.READ_ONLY);
		lblSizeBP.setText("Size");

		lblStrand = new Text(composite_9, SWT.READ_ONLY);
		lblStrand.setText("Strand");

		lblRef = new Label(composite_9, SWT.READ_ONLY);
		RWTUtils.setMarkup(lblRef);
		lblRef.setText("Ref:");
		new Label(composite_9, SWT.NONE);

		lblSynonims = new Text(composite_9, SWT.WRAP);
		lblSynonims.setText("Synonyms");
		new Label(composite_9, SWT.NONE);

		lblNote = new Text(composite_9, SWT.WRAP | SWT.READ_ONLY);
		GridData gd_lblNote = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_lblNote.heightHint = 28;
		lblNote.setLayoutData(gd_lblNote);
		lblNote.setText("Note");

		btnDisplayNucleotideSequence = new Button(composite_9, SWT.NONE);
		btnDisplayNucleotideSequence.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnDisplayNucleotideSequence.setText("Export nucleotide sequence");
		btnDisplayNucleotideSequence.addSelectionListener(this);
		txtSeqnucleotide = new Text(composite_9, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		GridData gd_txtSeqnucleotide = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_txtSeqnucleotide.heightHint = 76;
		txtSeqnucleotide.setLayoutData(gd_txtSeqnucleotide);

		lblDescribedIn = new Label(composite_9, SWT.NONE);
		lblDescribedIn.setText("Described in");
		new Label(composite_9, SWT.NONE);

		tableOtherInfo = new Table(composite_9, SWT.BORDER | SWT.FULL_SELECTION);
		tableOtherInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableOtherInfo.setHeaderVisible(true);
		tableOtherInfo.setLinesVisible(true);
		RWTUtils.setMarkup(tableOtherInfo);

		composite_6 = new Composite(composite_4, SWT.BORDER);
		composite_6.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		composite_6.setLayout(new GridLayout(1, false));

		lblPredictedSecondaryStructure = new Label(composite_6, SWT.WRAP);
		GridData gd_lblPredictedSecondaryStructure = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblPredictedSecondaryStructure.widthHint = 250;
		gd_lblPredictedSecondaryStructure.heightHint = 43;
		lblPredictedSecondaryStructure.setLayoutData(gd_lblPredictedSecondaryStructure);
		lblPredictedSecondaryStructure.setText("Predicted secondary structure (UNAFold 37\u00B0C)");

		btnOpenStructure = new Button(composite_6, SWT.NONE);
		btnOpenStructure.setText("Open Secondary Structure");
		btnOpenStructure.addSelectionListener(this);

		lblStructure = new Label(composite_6, SWT.BORDER);
		GridData gd_lblStructure = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_lblStructure.widthHint = 400;
		gd_lblStructure.heightHint = 400;
		lblStructure.setLayoutData(gd_lblStructure);

		tbtmExpressionAtlas = new TabItem(tabFolder, SWT.NONE);
		tbtmExpressionAtlas.setText("Expression Atlas");

		composite = new Composite(tabFolder, SWT.BORDER);
		tbtmExpressionAtlas.setControl(composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		composite_8_1 = new Composite(composite, SWT.BORDER);
		composite_8_1.setLayout(new GridLayout(1, false));

		{
			Composite composite_2_1 = new Composite(composite_8_1, SWT.NONE);
			composite_2_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			composite_2_1.setLayout(new GridLayout(2, false));

			Label lbllogfoldchange = new Label(composite_2_1, SWT.NONE);
			lbllogfoldchange.setText("|Log(Fold-Change)| >");

			txtCutoffLogFC = new Text(composite_2_1, SWT.BORDER);
			txtCutoffLogFC.setText("1.5");
		}
		{
			composite_pvalue = new Composite(composite_8_1, SWT.NONE);
			composite_pvalue.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			composite_pvalue.setLayout(new GridLayout(3, false));

			Label lblAnd = new Label(composite_pvalue, SWT.NONE);
			lblAnd.setText("and");

			Label lblPvalueFdrby = new Label(composite_pvalue, SWT.NONE);
			lblPvalueFdrby.setText("p-value FDRBY <");

			txtCutoffPvalue = new Text(composite_pvalue, SWT.BORDER);
			txtCutoffPvalue.setText("0.05");

			if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
				composite_pvalue.dispose();
			}
		}
		btnUpdateCutoff = new Button(composite_8_1, SWT.NONE);
		btnUpdateCutoff.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnUpdateCutoff.setText("Choose cut-off and update Expression Atlas");
		btnUpdateCutoff.addSelectionListener(this);

		composite_3 = new Composite(composite, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_3.setLayout(new GridLayout(4, false));

		Label lblClickOnOne = new Label(composite_3, SWT.WRAP);
		lblClickOnOne.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
		lblClickOnOne.setText("Select data and click here to display on:");
		lblClickOnOne.setForeground(BasicColor.GREY);

		Label lblGenomeViewer = new Label(composite_3, SWT.NONE);
		lblGenomeViewer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblGenomeViewer.setText("Genome Viewer");

		Label lblOr = new Label(composite_3, SWT.NONE);
		lblOr.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
		lblOr.setText("   or   ");
		lblOr.setForeground(BasicColor.GREY);

		Label lblHeatmapViewer = new Label(composite_3, SWT.NONE);
		lblHeatmapViewer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblHeatmapViewer.setText("HeatMap Viewer");

		btnGenomeViewer = new Button(composite_3, SWT.NONE);
		btnGenomeViewer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnGenomeViewer.setImage(ResourceManager.getPluginImage("bacnet", "icons/genomeViewer.bmp"));
		btnGenomeViewer.addSelectionListener(this);

		btnHeatmapview = new Button(composite_3, SWT.NONE);
		btnHeatmapview.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnHeatmapview.setImage(ResourceManager.getPluginImage("bacnet", "icons/compareexpression.bmp"));
		btnHeatmapview.addSelectionListener(this);

		Label lblOverExpressedIn = new Label(composite, SWT.NONE);
		lblOverExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOverExpressedIn.setText("Over expressed in");

		lblOver = new Label(composite, SWT.NONE);
		lblOver.setText("over");

		tableOver = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_tableOver = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd_tableOver.heightHint = 150;
		gd_tableOver.widthHint = 170;
		tableOver.setLayoutData(gd_tableOver);
		tableOver.addSelectionListener(this);

		Label lblUnderExpressedIn = new Label(composite, SWT.NONE);
		lblUnderExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUnderExpressedIn.setText("Under expressed in");

		lblUnder = new Label(composite, SWT.NONE);
		lblUnder.setText("under");

		tableUnder = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_tableUnder = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd_tableUnder.heightHint = 150;
		gd_tableUnder.widthHint = 81;
		tableUnder.setLayoutData(gd_tableUnder);
		tableUnder.addSelectionListener(this);

		Label lblNoDiffExpression = new Label(composite, SWT.NONE);
		lblNoDiffExpression.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNoDiffExpression.setText("No diff. expression in");

		lblNodiff = new Label(composite, SWT.NONE);
		lblNodiff.setText("nodiff");

		tableNoDiff = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_tablediff = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd_tablediff.heightHint = 150;
		gd_tablediff.widthHint = 170;
		tableNoDiff.setLayoutData(gd_tablediff);
		tableNoDiff.addSelectionListener(this);

		scrolledComposite.setContent(composite_2);
		scrolledComposite.setMinSize(composite_2.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		updateInfo();
		updateGenome();
		updateAllWidgets();

	}

	/**
	 * Update all lists of genes after a genome selection, or opening of the view
	 */
	private void updateInfo() {

		if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT
				|| Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
			sRNARefToLink = TabDelimitedTableReader.readHashMap(Srna.PATHTABLE_SrnaReference);
			arrayDataList = TabDelimitedTableReader
					.read(Database.getInstance().getTranscriptomesComparisonsArrayPath());
			String genomeName = Genome.EGDE_NAME;
			try {
				ArrayList<String> genomeNames = new ArrayList<>();
				genomeNames.add(genomeName);
				OpenGenomesThread thread = new OpenGenomesThread(genomeNames);
				new ProgressMonitorDialog(this.shell).run(true, false, thread);
				genome = Genome.loadGenome(genomeName);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			if (genome.getSpecies().equals(Genome.EGDE_NAME)) {
				listSrnas = new ArrayList<>();
				for (String rna : Database.getInstance().getsRNAListEGDe())
					listSrnas.add(rna);
				for (String rna : Database.getInstance().getAsRNAListEGDe())
					listSrnas.add(rna);
				for (String rna : Database.getInstance().getCisRegRNAListEGDe())
					listSrnas.add(rna);
			}
			tableSrnas.removeAll();
			tableSrnas.setItemCount(listSrnas.size());
			tableSrnas.update();
		}
	}

	/**
	 * Update all panels and info after a gene selection
	 */
	public void updateAllWidgets() {
		if (seq != null) {
			String title = seq.getName();
			lblGene.setText(title);
			lblBegin.setText("Begin: " + seq.getBegin() + "");
			lblSizeBP.setText("Size bp: " + seq.getLength());
			lblStrand.setText("Strand: " + seq.getStrand());
			lblRef.setText("Ref: " + RWTUtils.setSrnaLink(seq.getRef(), sRNARefToLink));
			lblEnd.setText("End: " + seq.getEnd() + "");
			lblSynonims.setText("Synonyms: " + seq.getSynonymsText());
			lblNote.setText("Note: " + seq.getComment());
			txtSeqnucleotide.setText(seq.getSequence());

			String url = Srna.PATH_SEC_STRUCTURE + "Mini-" + seq.getName() + ".png";
			lblStructure.setImage(SWTResourceManager.getImage(url));
			updateTable();
			canvasGenome.getTrack().moveHorizontally(seq.getBegin());
			canvasGenome.redraw();
			ArrayList<String> genomeTranscriptomes = BioCondition.getTranscriptomesGenomes();
			if (genomeTranscriptomes.contains(genome.getSpecies())) {
				updateExpressionAtlas();
			} else {
				lblOver.setText("No data");
				lblUnder.setText("No data");
				lblNodiff.setText("No data");
				tableOver.removeAll();
				tableUnder.removeAll();
				tableNoDiff.removeAll();
			}
		} else {
			seq = (Srna) genome.getElement(tableSrnas.getItems()[0].getText());
			updateAllWidgets();
			canvasGenome.getTrack().moveHorizontally(seq.getBegin());
			canvasGenome.redraw();
		}
		pushState();
	}

	/**
	 * Update display of expression data results
	 */
	public void updateExpressionAtlas() {
		if (seq != null) {
			double cutoffLogFC = GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF;
			try {
				cutoffLogFC = Double.parseDouble(txtCutoffLogFC.getText());
			} catch (Exception e) {
				txtCutoffLogFC.setText(GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF + "");
			}
			Filter filter = new Filter();
			filter.setCutOff1(cutoffLogFC);
			GenomeElementAtlas atlas = new GenomeElementAtlas(seq, filter);
			lblOver.setText(atlas.getOverBioConds().size() + " data");
			lblUnder.setText(atlas.getUnderBioConds().size() + " data");
			lblNodiff.setText(atlas.getNotDiffExpresseds().size() + " data");
			updateTranscriptomesTable(atlas);
		}
	}

	private void updateTranscriptomesTable(GenomeElementAtlas atlas) {
		/*
		 * Update overexpressed list
		 */
		tableOver.removeAll();
		tableOver.setHeaderVisible(true);
		tableOver.setLinesVisible(true);
		for (int i = 0; i < arrayDataList[0].length; i++) {
			TableColumn column = new TableColumn(tableOver, SWT.NONE);
			column.setText(arrayDataList[0][i]);
			column.setAlignment(SWT.LEFT);
		}
		for (int i = 1; i < arrayDataList.length; i++) {
			String dataName = arrayDataList[i][ArrayUtils.findColumn(arrayDataList, "Data Name")];
			if (atlas.getOverBioConds().contains(dataName)) {
				TableItem item = new TableItem(tableOver, SWT.NONE);
				for (int j = 0; j < arrayDataList[0].length; j++) {
					item.setText(j, arrayDataList[i][j]);
				}
			}
		}
		for (int i = 0; i < arrayDataList[0].length; i++) {
			tableOver.getColumn(i).pack();
		}
		tableOver.update();
		tableOver.redraw();

		/*
		 * Update under-expressed list
		 */
		tableUnder.removeAll();
		tableUnder.setHeaderVisible(true);
		tableUnder.setLinesVisible(true);
		for (int i = 0; i < arrayDataList[0].length; i++) {
			TableColumn column = new TableColumn(tableUnder, SWT.NONE);
			column.setText(arrayDataList[0][i]);
			column.setAlignment(SWT.LEFT);
		}
		for (int i = 1; i < arrayDataList.length; i++) {
			String dataName = arrayDataList[i][ArrayUtils.findColumn(arrayDataList, "Data Name")];
			if (atlas.getUnderBioConds().contains(dataName)) {
				TableItem item = new TableItem(tableUnder, SWT.NONE);
				for (int j = 0; j < arrayDataList[0].length; j++) {
					item.setText(j, arrayDataList[i][j]);
				}
			}
		}
		for (int i = 0; i < arrayDataList[0].length; i++) {
			tableUnder.getColumn(i).pack();
		}
		tableUnder.update();
		tableUnder.redraw();

		/*
		 * Update no dfiff expressed genes list
		 */
		tableNoDiff.removeAll();
		tableNoDiff.setHeaderVisible(true);
		tableNoDiff.setLinesVisible(true);
		for (int i = 0; i < arrayDataList[0].length; i++) {
			TableColumn column = new TableColumn(tableNoDiff, SWT.NONE);
			column.setText(arrayDataList[0][i]);
			column.setAlignment(SWT.LEFT);
		}
		for (int i = 1; i < arrayDataList.length; i++) {
			String dataName = arrayDataList[i][ArrayUtils.findColumn(arrayDataList, "Data Name")];
			if (atlas.getNotDiffExpresseds().contains(dataName)) {
				TableItem item = new TableItem(tableNoDiff, SWT.NONE);
				for (int j = 0; j < arrayDataList[0].length; j++) {
					item.setText(j, arrayDataList[i][j]);
				}
			}
		}
		for (int i = 0; i < arrayDataList[0].length; i++) {
			tableNoDiff.getColumn(i).pack();
		}
		tableNoDiff.update();
		tableNoDiff.redraw();
	}

	/**
	 * Return the list of comparisons which have been selected
	 * 
	 * @param text
	 * @return
	 */
	public ArrayList<String> getSelectedComparisons() {
		ArrayList<String> comparisons = new ArrayList<>();
		for (int index : tableOver.getSelectionIndices()) {
			String comparison = tableOver.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name"));
			System.out.println(comparison);
			comparisons.add(comparison);
		}
		for (int index : tableUnder.getSelectionIndices()) {
			String comparison = tableUnder.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name"));
			System.out.println(comparison);
			comparisons.add(comparison);
		}
		for (int index : tableNoDiff.getSelectionIndices()) {
			String comparison = tableNoDiff.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name"));
			System.out.println(comparison);
			comparisons.add(comparison);
		}
		return comparisons;
	}

	public void updateGenome() {
		Track track = new Track(Genome.loadEgdeGenome(), Genome.EGDE_CHROMO_NAME);
		canvasGenome.setTrack(track);
		canvasGenome.redraw();
	}

	public void updateTable() {

		tableOtherInfo.removeAll();
		tableOtherInfo.setHeaderVisible(true);
		tableOtherInfo.setLinesVisible(true);

		String[] titles = { "Ref", "Name", "Begin", "End", "Size", "Strand", "Supp. Info." };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(tableOtherInfo, SWT.NONE);
			column.setText(titles[i]);
			column.setAlignment(SWT.LEFT);
		}
		// System.out.println(seq.getFeaturesText());
		for (int i = 0; i < seq.getFoundIn().size(); i++) {
			String refCurrent = seq.getFoundIn().get(i);
			String linkRef = RWTUtils.setSrnaLink(refCurrent, sRNARefToLink);
			TableItem item = new TableItem(tableOtherInfo, SWT.NONE);
			item.setText(0, linkRef);
			item.setText(1, seq.getFeature("name (" + refCurrent + ")"));
			item.setText(2, seq.getFeature("from (" + refCurrent + ")"));
			item.setText(3, seq.getFeature("to (" + refCurrent + ")"));
			item.setText(4, seq.getFeature("length (" + refCurrent + ")"));
			String strand = "+";
			if (seq.getFeature("strand (" + refCurrent + ")").equals("false"))
				strand = "-";
			item.setText(5, strand);
			item.setText(6, seq.getFeaturesTextForTable(refCurrent));
		}
		for (int i = 0; i < titles.length; i++) {
			tableOtherInfo.getColumn(i).pack();
		}
		tableOtherInfo.update();
		tableOtherInfo.redraw();
	}

	/**
	 * Search a text file in the annotation<br>
	 * <li>Search if it is a position and go
	 * <li>Search if it is contains in
	 * <code>getChromosome().getAllElements().keySet()</code>
	 * <li>Search if it is contains in <code>gene.getName()</code><br>
	 * All the search are case insensitive using <code>text.toUpperCase();</code>
	 * 
	 * @param text
	 * @return
	 */
	public ArrayList<String> search(String text) {
		Chromosome chromosome = genome.getFirstChromosome();
		ArrayList<String> searchResult = new ArrayList<>();
		try {
			/*
			 * If a base pair position has been wrote, it will reach it directly
			 */
			int position = Integer.parseInt(text);
			Sequence sequenceTemp = chromosome.getAnnotation().getElementInfoATbp(chromosome, position);
			if (sequenceTemp instanceof Srna) {
				searchResult.add(sequenceTemp.getName());
			}
		} catch (Exception e) {
			/*
			 * go through chromosome and search for a Gene with this name
			 */
			ArrayList<String> listSrnaTemp = new ArrayList<>();
			for (String rna : Database.getInstance().getsRNAListEGDe())
				listSrnaTemp.add(rna);
			for (String rna : Database.getInstance().getAsRNAListEGDe())
				listSrnaTemp.add(rna);
			for (String rna : Database.getInstance().getCisRegRNAListEGDe())
				listSrnaTemp.add(rna);

			/*
			 * Go through all Srna and search information in the anniotation of each gene
			 */
			text = text.toUpperCase();
			for (String name : listSrnaTemp) {
				Srna sRNA = (Srna) genome.getElement(name);
				if (name.contains(text)) {
					if (!searchResult.contains(sRNA.getName())) {
						searchResult.add(sRNA.getName());
					}
				}
				String sRNAInfo = sRNA.getFoundInText() + "  -  " + sRNA.getSynonym() + " - " + sRNA.getTypeSrna()
						+ " - " + sRNA.getFeaturesText();
				String sRNAInfoTemp = sRNAInfo.toUpperCase();
				if (sRNAInfoTemp.contains(text)) {
					if (!searchResult.contains(sRNA.getName())) {
						searchResult.add(sRNA.getName());
					}
				}
			}
		}

		// for(String geneName : searchResult){
		// System.out.println(geneName);
		// }
		return searchResult;
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
		if (genome != null) {
			parameters.put(NavigationManagement.LIST, seq.getName());
			Item item = tabFolder.getItem(tabFolder.getSelectionIndex());
			parameters.put(NavigationManagement.ITEM, item.getText());
			NavigationManagement.pushStateView(viewId, parameters);
		}
	}

	/**
	 * Open Srna View with a specific Srna to display
	 * 
	 * @param seq
	 * @param partService
	 */
	public static void displaySrna(Srna seq, EPartService partService) {
		String id = SrnaView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, SrnaView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		SrnaView view = (SrnaView) part.getObject();
		view.setViewId(id);
		view.setSeq(seq);
		for (int i = 0; i < view.getListSrnas().size(); i++) {
			if (view.getListSrnas().get(i).equals(seq.getName())) {
				view.getTableSrnas().select(i);
				view.getTableSrnas().showItem(view.getTableSrnas().getItem(i));
			}
		}
		view.updateAllWidgets();
	}

	/**
	 * Display the view with saved parameters
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void displaySrnaView(EPartService partService, HashMap<String, String> parameters) {
		String id = SrnaView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, SrnaView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		SrnaView view = (SrnaView) part.getObject();
		view.setViewId(id);
		String srnaName = parameters.get(NavigationManagement.LIST).replaceFirst(";", "");
		Srna sRNA = view.getGenome().getFirstChromosome().getsRNAs().get(srnaName);
		view.setSeq(sRNA);
		String tabItemName = parameters.get(NavigationManagement.ITEM);
		for (int i = 0; i < view.getListSrnas().size(); i++) {
			if (view.getListSrnas().get(i).equals(sRNA.getName())) {
				view.getTableSrnas().select(i);
				view.getTableSrnas().showItem(view.getTableSrnas().getItem(i));
			}
		}
		view.updateAllWidgets();
		if (!tabItemName.equals("")) {
			for (TabItem tabItem : view.getTabFolder().getItems()) {
				if (tabItem.getText().equals(tabItemName)) {
					view.getTabFolder().setSelection(view.getTabFolder().indexOf(tabItem));
				}
			}
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == tableSrnas) {
			seq = (Srna) Genome.loadEgdeGenome().getElement(listSrnas.get(tableSrnas.getSelectionIndex()));
			updateAllWidgets();
		} else if (e.getSource() == tabFolder) {
			pushState();
		} else if (e.getSource() == btnOpenStructure) {
			String url = Srna.PATH_SEC_STRUCTURE + File.separator + seq.getName() + ".png";
			SecondStructureRNADialog dialog = new SecondStructureRNADialog(shell, url);
			dialog.open();
		} else if (e.getSource() == btnZoomplus) {
			canvasGenome.getTrack().zoom(true);
			canvasGenome.redraw();
		} else if (e.getSource() == btnZoomminus) {
			canvasGenome.getTrack().zoom(false);
			canvasGenome.redraw();
		} else if (e.getSource() == btnGenomeViewer) {
			GenomeTranscriptomeView.displayGenomeElementAndBioConditions(partService, genome.getSpecies(),
					getSelectedComparisons(), seq.getName());
		} else if (e.getSource() == btnUpdateCutoff) {
			updateExpressionAtlas();
		} else if (e.getSource() == btnHeatmapview) {
			String sequence = seq.getName();
			HeatMapTranscriptomicsView.displayComparisonsAndElement(genome.getSpecies(), getSelectedComparisons(),
					sequence, partService);
		} else if (e.getSource() == btnDisplayNucleotideSequence) {
			Strand strand = Strand.POSITIVE;
			if (!seq.isStrand())
				strand = Strand.NEGATIVE;
			SequenceDisplayDialog dialog = new SequenceDisplayDialog(shell, partService, genome,
					Genome.EGDE_CHROMO_NAME, true, seq.getBegin(), seq.getEnd(), strand);
			dialog.open();
		} else if (e.getSource() == btnHelp) {
			HelpPage.helpSrnaView(partService);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (e.getSource() == canvasGenome) {
			Object obj = canvasGenome.clickedElement(e);
			if (obj != null) {
				// System.out.println("select "+obj.getClass());
				if (obj instanceof Gene) {
					Gene gene = (Gene) obj;
					GeneView.displayGene(gene, partService);
				} else if (obj instanceof Srna) {
					Srna sRNA = (Srna) obj;
					SrnaView.displaySrna(sRNA, partService);
				}
			}
		}
	}

	@Override
	public void mouseDown(MouseEvent e) {

	}

	@Override
	public void mouseUp(MouseEvent e) {
		if (e.button == 3) { // left button
			int position = canvasGenome.convertXtoBP(e.x);
			canvasGenome.getTrack().search(position + "");
			canvasGenome.redraw();
		} else if (e.button == 2) {
			System.out.println(e.count);
		}
	}

	public Srna getSeq() {
		return seq;
	}

	public void setSeq(Srna seq) {
		this.seq = seq;
	}

	public ArrayList<String> getListSrnas() {
		return listSrnas;
	}

	public void setListSrnas(ArrayList<String> listSrnas) {
		this.listSrnas = listSrnas;
	}

	public Text getTxtSeqnucleotide() {
		return txtSeqnucleotide;
	}

	public void setTxtSeqnucleotide(Text txtSeqnucleotide) {
		this.txtSeqnucleotide = txtSeqnucleotide;
	}

	public Composite getComposite_6() {
		return composite_6;
	}

	public void setComposite_6(Composite composite_6) {
		this.composite_6 = composite_6;
	}

	public Composite getComposite_7() {
		return composite_7;
	}

	public void setComposite_7(Composite composite_7) {
		this.composite_7 = composite_7;
	}

	public Table getTableSrnas() {
		return tableSrnas;
	}

	public void setTableSrnas(Table tableSrnas) {
		this.tableSrnas = tableSrnas;
	}

	public Label getLblOrAnyOther() {
		return lblOrAnyOther;
	}

	public void setLblOrAnyOther(Label lblOrAnyOther) {
		this.lblOrAnyOther = lblOrAnyOther;
	}

	public ScrolledComposite getScrolledComposite() {
		return scrolledComposite;
	}

	public void setScrolledComposite(ScrolledComposite scrolledComposite) {
		this.scrolledComposite = scrolledComposite;
	}

	public Composite getComposite_2() {
		return composite_2;
	}

	public void setComposite_2(Composite composite_2) {
		this.composite_2 = composite_2;
	}

	public Label getLblGene() {
		return lblGene;
	}

	public void setLblGene(Label lblGene) {
		this.lblGene = lblGene;
	}

	public Label getLblPredictedSecondaryStructure() {
		return lblPredictedSecondaryStructure;
	}

	public void setLblPredictedSecondaryStructure(Label lblPredictedSecondaryStructure) {
		this.lblPredictedSecondaryStructure = lblPredictedSecondaryStructure;
	}

	public Label getLblStructure() {
		return lblStructure;
	}

	public void setLblStructure(Label lblStructure) {
		this.lblStructure = lblStructure;
	}

	public Button getBtnOpenStructure() {
		return btnOpenStructure;
	}

	public void setBtnOpenStructure(Button btnOpenStructure) {
		this.btnOpenStructure = btnOpenStructure;
	}

	public Table getTableOtherInfo() {
		return tableOtherInfo;
	}

	public void setTableOtherInfo(Table tableOtherInfo) {
		this.tableOtherInfo = tableOtherInfo;
	}

	public Label getLblDescribedIn() {
		return lblDescribedIn;
	}

	public void setLblDescribedIn(Label lblDescribedIn) {
		this.lblDescribedIn = lblDescribedIn;
	}

	public Label getLabel_2() {
		return label_2;
	}

	public void setLabel_2(Label label_2) {
		this.label_2 = label_2;
	}

	public Label getLabel_3() {
		return label_3;
	}

	public void setLabel_3(Label label_3) {
		this.label_3 = label_3;
	}

	public String[][] getArrayDataList() {
		return arrayDataList;
	}

	public void setArrayDataList(String[][] arrayDataList) {
		this.arrayDataList = arrayDataList;
	}

	public Genome getGenome() {
		return genome;
	}

	public void setGenome(Genome genome) {
		this.genome = genome;
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public void setTabFolder(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
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

}
