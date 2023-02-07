package bacnet.sequenceTools;

import java.net.*;
import java.nio.file.Files;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.biojava3.core.sequence.Strand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.ProgressEvent;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import bacnet.datamodel.annotation.SubCellCompartment;
import bacnet.datamodel.dataset.Network;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Genome.GetMultiFastaThread;
import bacnet.datamodel.sequence.Genome.OpenGenomesThread;
import bacnet.datamodel.sequence.Operon;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.expressionAtlas.HeatMapProteomicsView;
import bacnet.expressionAtlas.HeatMapTranscriptomicsView;
import bacnet.expressionAtlas.core.GenomeElementAtlas;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.genomeBrowser.core.Track;
import bacnet.genomeBrowser.tracksGUI.TrackCanvasGenome;
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
import bacnet.utils.ListUtils;
import bacnet.utils.RWTUtils;
import bacnet.views.HelpPage;

public class GeneView implements SelectionListener, MouseListener {

	/**
	 * Serivale version ID
	 */
	private static final long serialVersionUID = -5632436594051134260L;

	public static final String ID = "bacnet.GeneView"; //$NON-NLS-1$

	/**
	 * Current viewId = ID + math.random()
	 */
	private String viewID = "";

	/**
	 * Indicates if we focus the view, so we can pushState navigation
	 */
	private boolean focused = false;
	
	/**
	 * Egde Genome loaded
	 */
	private Genome genome;
	private String chromoID = "";
	private Track trackGenome;
	private ArrayList<String> listGenes = new ArrayList<>();
	private Gene sequence;
	private Label lblName;
	private Label lblBegin;
	private Label lblEnd;
	private Label lblSizeaa;
	private Label lblSizeBP;
	private Label lblProduct;
	private Label lblStrand;
	private Label lblLocus;
	private Text lblCog;
	private Label lblProtID;
	private Combo comboGenome;
	private Text textFeature;

	private boolean loading; 

	private Text lblOperon;
	private Table tableGenes;
	private Label lblGene;
	private TrackCanvasGenome canvasGenome;
	private Text txtSearch;
	@SuppressWarnings("unused")
	private boolean browserIsFocus = false;
	private Network generalNetwork;
	private Network filteredNetwork;

	/*
	 * General buttons
	 */
	private TabFolder tabFolder;
	private TabItem tbtmGeneralInformation;
	private Label lblTranscriptomesData;



	private TabItem tbtmSynteny;
	private Composite compSynt;
	private Browser browserSynteny;
	
	private TabItem tbtmKEGG;
	private Composite compositeKEGG;
	private Browser browserKEGG;
	private String KEGGPath;
	private Button btnKEGG;
	
	private TabItem tbtmUniprot;
	private Composite compositeUniprot;
	private Browser browserUniprot;
	private String UniprotPath;
	private Button btnUniprot;
	
	private TabItem tbtmInterpro;
	private Composite compositeInterpro;
	private Browser browserInterpro;
	private String InterproPath;
	private Button btnInterpro;

	private TabItem tbtmIntact;
	private Composite compositeIntact;
	private Browser browserIntact;
	private Button btnIntact;
	private String IntactPath;

		
	private TabItem tbtmString;
	private Composite compositeString;
	private Composite compositeString_row_1;

	private Browser browserString;
	private Button btnString;
	private String StringPath;
	private String stringURL;


	private Composite composite_15;
	private Button btnNucleotideSequence;
	private Button btnAminoAcidSequence;
	private Button btnLocalization;
	private Button btnShowSynteny;


	/*
	 * GenomeViewer
	 */
	private Button btnZoomplus;
	private Button btnZoomminus;
	private Combo comboChromosome;

	/*
	 * Expression atlas variables
	 */
	private Composite compositeTranscriptome;
	private String[][] arrayDataList = new String[0][0];
	private ArrayList<String[]> arrayDataToDisplay;
	private Table tableOver;
	private Table tableUnder;
	private Table tableNodiff;
	private Label lblOver;
	private Label lblUnder;
	private Label lblNodiff;

	private TabItem tbtmExpressionData;
	private Button btnUpdateCutoff;
	private Text txtCutoffLogFC;
	private Text txtCutoffPvalue;
	private Button btnHeatmapview;
	private Button btnGenomeViewer;

	private TabItem tbtmTranscriptomes;
	private String[][] arrayTranscriptomesList = new String[0][0];
	//private ArrayList<String[]> arrayProteomeToDisplay;
	private Composite composite_101;
	private Composite composite_102;
	private Composite composite_103;
	private Table tableTranscriptomes;
	private Label lblExprTranscriptomes;

	/*
	 * Protein atlas variables
	 */
	private String[][] arrayProteinAtlasList = new String[0][0];
	private ArrayList<String[]> arrayProteinAtlasToDisplay;
	private Composite compositeProteome;
	private Table tableOverProteome;
	private Table tableUnderProteome;
	private Table tableNodiffProteome;
	private Label lblOverProteome;
	private Label lblUnderProteome;
	private Label lblNodiffProteome;

	private TabItem tbtmProteomeData;
	private Button btnUpdateCutoffProteome;
	private Text txtCutoffLogFCProteome;
	private Text txtCutoffPvalueProteome;
	private Button btnHeatmapviewProteome;
	private Button btnGenomeViewerProteome;


	/*
	 * Proteomes
	 */
	private TabItem tbtmProteomes;
	private String[][] arrayProteomeList = new String[0][0];
	private ArrayList<String[]> arrayProteomeToDisplay;
	private Composite composite_09;
	private Composite composite_091;
	private Composite composite_092;
	private Table tableProteomes;
	private Label lblExprProteomes;


	/*
	 * Coregulation
	 */
	private ArrayList<String> coExpCol = new ArrayList<>();
	private TabItem tbtmCoExp;
	private Composite compositeCoExpression;
	private Button btnUpdateCutoffCoExp;
	private Text txtCutoffCoExp;
	private TableViewer tablePosCoExpViewer;
	private TableViewer tableNegCoExpViewer;
	private BioConditionComparator comparatorPosCoExp;
	private BioConditionComparator comparatorNegCoExp;
	private String[][] posCoExpArray;
	private String[][] negCoExpArray;
	private String[][] posCoExpArrayTemp;
	private String[][] negCoExpArrayTemp;
    private Button btnCorrPlus;
    private Button btnCorrMinus;
    private Button btnExportNetwork;

    private Boolean browserCompleted =false;
    
	/*
	 * Other
	 */

	private Label lblConservation;
	private TabItem tbtmHomologs;
	private Composite composite_13;
	private Composite composite_14;
	private Label lblConservation2;
	private TableViewer tableHomologViewer;
	private Table tableHomolog;

	private BioConditionComparator comparatorBioCondition;

	/**
	 * Array used to init data
	 */
	private String[][] bioCondsArray;
	/**
	 * List used to init data
	 */
	private ArrayList<String[]> bioConds = new ArrayList<String[]>();
	/**
	 * Array which will be displayed
	 */
	private ArrayList<String[]> bioCondsToDisplay = new ArrayList<String[]>();

	private ArrayList<String> columnNames = new ArrayList<>();
	private Button btnExportToFasta;
	private Composite compositeGenome;
	private Composite composite_localization;
	private Label lblPredictedSubcellularLocalization;
	private String[][] arrayGeneToLocalization;
	//private Button btnHelp;
	private Browser browserLocalization;
	private Browser browserHomolog;
	private ArrayList<String> selectedGenomes = new ArrayList<>();

	@Inject
	private EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	private Button btnGetAnnotationInformation;
	private Button btnNcbiChromosome;
	private Button btnSaveAsPng;
	private Button btnSaveAsSvg;
	private Text txtSearchGenome;
	private Button btnSelectall;
	private Button btnUnselectall;
	private Button btnDownloadtxt;
	private Composite compSuppInfo;
	private Composite composite_1;
	private HashMap<String, String> syntenyHashMap;
	private HashMap<String, String> KEGGHashMap;
	private HashMap<String, String> stringHashMap;

	
	@Inject
	public GeneView() {


	}
	private ArrayList<String> test;


	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		focused = true;
		
		/*
		 * create HashMap for genomes with synteny
		 */
		syntenyHashMap = new HashMap<String,String>();
		File syntenyFile = new File(Database.getInstance().getSyntenyHashMapPath());
		if(syntenyFile.exists()) {
			syntenyHashMap = HashMapFromTextFile(Database.getInstance().getSyntenyHashMapPath());
		}else {
			System.out.println("Cannot read: "+Database.getInstance().getSyntenyHashMapPath());
		}
		/*
		syntenyHashMap.put("Yersinia pestis CO92", "CO92");
		syntenyHashMap.put("Yersinia enterocolitica Y11", "Y11");
		syntenyHashMap.put("Yersinia enterocolitica 8081", "8081");
*/
		
		/*
		 * create HashMap for KEGG and Uniprot genomes
		 */
		
		
		
		KEGGHashMap = new HashMap<String,String>();
		File KEGGFile = new File(Database.getInstance().getKEGGHashMapPath());
		if(KEGGFile.exists()) {
			KEGGHashMap = HashMapFromTextFile(Database.getInstance().getKEGGHashMapPath());
		}else {
			System.out.println("Cannot read: "+Database.getInstance().getKEGGHashMapPath());
		}
		
		/*
		 * create HashMap for String genomes
		 */
		stringHashMap = new HashMap<String,String>();		
		File stringFile = new File(Database.getInstance().getStringHashMapPath());
		if(stringFile.exists()) {
			stringHashMap = HashMapFromTextFile(Database.getInstance().getStringHashMapPath());
		}else {
			System.out.println("Cannot read: "+Database.getInstance().getStringHashMapPath());
		}
		
		/*
		stringHashMap.put("Yersinia pestis CO92", "214092");
		stringHashMap.put("Yersinia enterocolitica 8081", "393305");
*/
		Composite container = new Composite(parent, SWT.NONE);
		container.setBounds(0, 0, 586, 480);
		container.setLayout(new GridLayout(4, false));
        
		compositeGenome = new Composite(container, SWT.NONE);
		compositeGenome.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 2, 1));
		compositeGenome.setLayout(new GridLayout(9, false));
		
		Label lblSelectAGenome = new Label(compositeGenome, SWT.NONE);
		lblSelectAGenome.setText("Select genome");
		
		comboGenome = new Combo(compositeGenome, SWT.NONE);
		GridData gd_comboGenome = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_comboGenome.widthHint = 300;
		comboGenome.setLayoutData(gd_comboGenome);

		lblTranscriptomesData = new Label(compositeGenome, SWT.NONE);
		GridData gd_lblTranscriptomesData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_lblTranscriptomesData.widthHint = 200;
		lblTranscriptomesData.setLayoutData(gd_lblTranscriptomesData);
		lblTranscriptomesData.setText(" * Transcriptomics or \nproteomics data available");
		lblTranscriptomesData.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
		
		
		Label lblChromosome = new Label(compositeGenome, SWT.NONE);
		lblChromosome.setText("Chromosome/Plasmids");

		comboChromosome = new Combo(compositeGenome, SWT.FLAT);
		GridData gd_comboChromosome = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_comboChromosome.widthHint = 200;
		comboChromosome.setLayoutData(gd_comboChromosome);

		btnNcbiChromosome = new Button(compositeGenome, SWT.TOGGLE);
		btnNcbiChromosome.setBackground(BasicColor.WHITE);
		btnNcbiChromosome.setToolTipText("Access more information on the genome");
		btnNcbiChromosome.setBackground(BasicColor.BUTTON);

		btnNcbiChromosome.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/refseq.png"));
		btnNcbiChromosome.addSelectionListener(this);

		btnGetAnnotationInformation = new Button(compositeGenome, SWT.TOGGLE);
		btnGetAnnotationInformation.setText("Browse and download annotation table");
		btnGetAnnotationInformation.setToolTipText("Browse and download annotation table");
		btnGetAnnotationInformation.addSelectionListener(this);
		btnGetAnnotationInformation.setBackground(BasicColor.BUTTON);
		//btnGetAnnotationInformation.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/txt.bmp"));
		//new Label(compositeGenome, SWT.NONE);


		
		/*
		new Label(compositeGenome, SWT.NONE);
		new Label(compositeGenome, SWT.NONE);
		new Label(compositeGenome, SWT.NONE);
		new Label(compositeGenome, SWT.NONE);
		new Label(compositeGenome, SWT.NONE);
		new Label(compositeGenome, SWT.NONE);
		*/
		
		comboChromosome.addSelectionListener(this);
		comboGenome.addSelectionListener(this);
		
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		/*
		btnHelp = new Button(container, SWT.NONE);
		btnHelp.setToolTipText("How to use Gene panel");
		btnHelp.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/help.png"));
		btnHelp.addSelectionListener(this);
*/
		Composite composite_7 = new Composite(container, SWT.BORDER);
		composite_7.setLayout(new GridLayout(1, false));
		composite_7.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));

		Label lblSearch = new Label(composite_7, SWT.NONE);
		lblSearch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblSearch.setText("Search");
		lblSearch.setForeground(BasicColor.GREY);

		txtSearch = new Text(composite_7, SWT.BORDER);
		txtSearch.setBackground(BasicColor.WHITE);

		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearch.addKeyListener(new KeyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6597107306029419456L;

			@Override
			public void keyReleased(KeyEvent e) {
				if (txtSearch.getText().equals("")) {
					updateListGenomeElements();
				}
				if (e.keyCode == 16777296 || e.keyCode == 13) {
					ArrayList<String> searchResults = search(txtSearch.getText());
					if (searchResults.size() != 0) {
						listGenes.clear();
						for (String gene : searchResults) {
							String text = "";
							String oldLocusTag = genome.getChromosomes().get(chromoID).getGenes().get(gene).getOldLocusTag().replace("%2C", ",");
							if (!oldLocusTag.equals("")) {
								text += oldLocusTag+" - ";
								
							}
							String geneName = genome.getChromosomes().get(chromoID).getGenes().get(gene).getGeneName();
							if (!geneName.equals("")) {
								text += "(" + geneName +") - ";
							}
							text += gene;
							listGenes.add(text);
						}
						tableGenes.removeAll();
						tableGenes.setItemCount(listGenes.size());
						tableGenes.update();
						tableGenes.select(getListGenes().indexOf(searchResults.get(0)));
						sequence = genome.getGeneFromName(searchResults.get(0), chromoID);
						updateGeneInfo();
					}
				}

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}

		});

		tableGenes = new Table(composite_7, SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
		GridData gd_list = new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 1);
		gd_list.widthHint = 200;
		tableGenes.setLayoutData(gd_list);
		tableGenes.addListener(SWT.SetData, new Listener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6528589900380370286L;

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(listGenes.get(index));
				
			}
		});
		tableGenes.addSelectionListener(this);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite_11 = new Composite(scrolledComposite, SWT.BORDER);
		composite_11.setLayout(new GridLayout(1, false));

		lblGene = new Label(composite_11, SWT.BORDER | SWT.CENTER);
		GridData gd_lblGene = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblGene.widthHint = 400;
		lblGene.setLayoutData(gd_lblGene);
		lblGene.setBackground(BasicColor.HEADER);
		lblGene.setFont(SWTResourceManager.getBodyFont(20, SWT.BOLD));
		//lblGene.setForeground(BasicColor.WHITE);
		lblGene.setText("gene");

		tabFolder = new TabFolder(composite_11, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.addSelectionListener(this);
		tbtmGeneralInformation = new TabItem(tabFolder, SWT.NONE);
		tbtmGeneralInformation.setText("General information");
		
		scrolledComposite.setContent(composite_11);
		scrolledComposite.setMinSize(composite_11.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		composite_1 = new Composite(tabFolder, SWT.BORDER);
		tbtmGeneralInformation.setControl(composite_1);
		composite_1.setLayout(new GridLayout(4, false));

		Composite compGenomeViewer = new Composite(composite_1, SWT.NONE);
		compGenomeViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		compGenomeViewer.setLayout(new GridLayout(2, false));

		btnZoomminus = new Button(compGenomeViewer, SWT.TOGGLE);
		btnZoomminus.setBackground(BasicColor.BUTTON);
		btnZoomminus.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnZoomminus.setToolTipText("Zoom Out horizontally");
		btnZoomminus.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomOUT.bmp"));
		btnZoomminus.addSelectionListener(this);
		
		btnZoomplus = new Button(compGenomeViewer, SWT.TOGGLE);
		btnZoomplus.setBackground(BasicColor.BUTTON);
		btnZoomplus.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		btnZoomplus.setToolTipText("Zoom In horizontally");
		btnZoomplus.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomIN.bmp"));
		btnZoomplus.addSelectionListener(this);
		
		
		canvasGenome = new TrackCanvasGenome(compGenomeViewer, SWT.BORDER);
		GridData gd_canvasGenome = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_canvasGenome.heightHint = 125;
		canvasGenome.setLayoutData(gd_canvasGenome);
		canvasGenome.setLayout(new GridLayout(1, false));
		canvasGenome.addMouseListener(this);


		Composite compGeneralInfo = new Composite(composite_1, SWT.BORDER);
		GridData gd_compGeneralInfo = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		//gd_compGeneralInfo.widthHint = 450;
		compGeneralInfo.setLayoutData(gd_compGeneralInfo);
		compGeneralInfo.setLayout(new GridLayout(5, false));

		lblLocus = new Label(compGeneralInfo, SWT.READ_ONLY);
		lblLocus.setTouchEnabled(true);
		GridData gd_lblLocus = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		//gd_lblLocus.widthHint = 250;
		RWTUtils.setMarkup(lblLocus);
		lblLocus.setLayoutData(gd_lblLocus);
		lblLocus.setText("<b>Locus: </b>");

		lblStrand = new Label(compGeneralInfo, SWT.READ_ONLY);
		GridData gd_lblStrand = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		//gd_lblStrand.widthHint = 65;
		RWTUtils.setMarkup(lblStrand);
		lblStrand.setLayoutData(gd_lblStrand);
		lblStrand.setText("<b>Strand: </b>");

		lblBegin = new Label(compGeneralInfo, SWT.READ_ONLY);
		lblBegin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		RWTUtils.setMarkup(lblBegin);
		lblBegin.setText("<b>Begin: </b>");
		
		lblEnd = new Label(compGeneralInfo, SWT.READ_ONLY);
		lblEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		RWTUtils.setMarkup(lblEnd);
		lblEnd.setText("<b>End: </b>");

		lblSizeBP = new Label(compGeneralInfo, SWT.READ_ONLY);
		GridData gd_lblSizeBP = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSizeBP.widthHint = 80;
		lblSizeBP.setLayoutData(gd_lblSizeBP);
		RWTUtils.setMarkup(lblSizeBP);
		lblSizeBP.setText("<b>Size: </b>");

		lblSizeaa = new Label(compGeneralInfo, SWT.READ_ONLY);
		lblSizeaa.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		RWTUtils.setMarkup(lblSizeaa);
		lblSizeaa.setText("<b>SizeAA: </b>");

		lblName = new Label(compGeneralInfo, SWT.READ_ONLY);
		lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		RWTUtils.setMarkup(lblName);
		lblName.setText("<b>Gene: </b>");

		lblProduct = new Label(compGeneralInfo, SWT.READ_ONLY);
		GridData gd_lblProduct = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		//gd_lblProduct.heightHint = 60;
		RWTUtils.setMarkup(lblProduct);
		lblProduct.setLayoutData(gd_lblProduct);
		lblProduct.setText("<b>Product: </b>");

		
		composite_15 = new Composite(composite_1, SWT.BORDER);
		composite_15.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		composite_15.setLayout(new GridLayout(1, false));

		btnNucleotideSequence = new Button(composite_15, SWT.TOGGLE);
		btnNucleotideSequence.setText("Nucleotide sequence");
		btnNucleotideSequence.setToolTipText("Display the nucleotide sequence of the gene, available for download.");
		btnNucleotideSequence.setFont(SWTResourceManager.getBodyFont(12, SWT.BOLD));
		btnNucleotideSequence.setBackground(BasicColor.BUTTON);
		
		btnNucleotideSequence.addSelectionListener(this);
		btnAminoAcidSequence = new Button(composite_15, SWT.TOGGLE);
		btnAminoAcidSequence.setBackground(BasicColor.BUTTON);
		btnAminoAcidSequence.setToolTipText("Display the amino acid sequence of the protein, available for download.");
		btnAminoAcidSequence.setText("Amino acid sequence");
		btnAminoAcidSequence.setFont(SWTResourceManager.getBodyFont(12, SWT.BOLD));

		btnAminoAcidSequence.addSelectionListener(this);

		compSuppInfo = new Composite(composite_1, SWT.BORDER);
		GridData gd_compSuppInfo = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		//gd_compSuppInfo.widthHint = 550;
		compSuppInfo.setLayoutData(gd_compSuppInfo);
		compSuppInfo.setLayout(new GridLayout(1, false));

		/*
		lblOperon = new Text(compSuppInfo, SWT.READ_ONLY);
		lblOperon.setText("Operon");
		*/
		
		lblProtID = new Label(compSuppInfo, SWT.NONE);
		RWTUtils.setMarkup(lblProtID);
		lblProtID.setText("<b>GenBank Protein</b>: ");
		
		
		/*
		lblCog = new Text(compSuppInfo, SWT.READ_ONLY | SWT.WRAP);
		lblCog.setText("COG");
		*/
		
		lblConservation = new Label(compSuppInfo, SWT.NONE);
		RWTUtils.setMarkup(lblConservation);
		lblConservation.setText("Homologs in 000/000 Yersinia genomes");

		
		//textFeature = new Text(compSuppInfo, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);		
		//textFeature.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compFeatures = new Composite(composite_1, SWT.BORDER);
		GridData gd_compFeatures = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_compFeatures.heightHint = 50;
		compFeatures.setLayoutData(gd_compFeatures);
		compFeatures.setLayout(new GridLayout(1, false));
		
		textFeature = new Text(compFeatures, SWT.WRAP);
		GridData gd_textFeature = new GridData(SWT.RIGHT, SWT.FILL, false, true, 1, 1);
		gd_textFeature.heightHint = 50;
		textFeature.setLayoutData(gd_textFeature);
		
		
		compSynt = new Composite(composite_1, SWT.BORDER);
		GridData gd_compSynt = new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1);
		compSynt.setLayoutData(gd_compSynt);
		compSynt.setLayout(new GridLayout(1, false));

		/*
		btnShowSynteny = new Button(compSynt, SWT.CENTER);
		btnShowSynteny.setText("Show synteny");
		btnShowSynteny.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		btnShowSynteny.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
		
		browserSynteny = new Browser(compSynt, SWT.NONE);
		*/
		//browserSynteny.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		
		
		/*
		composite_localization = new Composite(composite_1, SWT.BORDER);
		composite_localization.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 4));
		composite_localization.setLayout(new GridLayout(1, false));

		lblPredictedSubcellularLocalization = new Label(composite_localization, SWT.WRAP);
		RWTUtils.setMarkup(lblPredictedSubcellularLocalization);
		GridData gd_lblPredictedSubcellularLocalization = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblPredictedSubcellularLocalization.widthHint = 200;
		gd_lblPredictedSubcellularLocalization.heightHint = 55;
		lblPredictedSubcellularLocalization.setLayoutData(gd_lblPredictedSubcellularLocalization);
		lblPredictedSubcellularLocalization
		.setText("Predicted subcellular localization of L. mono. EGD-e proteins (<a href='"
				+ RWTUtils.getPubMedLink("22912771") + "' target='_blank'>Renier et al., Plos One 2012</a>)");

		btnLocalization = new Button(composite_localization, SWT.NONE);
		btnLocalization.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnLocalization.setText("Click here for more information ");
		btnLocalization.addSelectionListener(this);

		browserLocalization = new Browser(composite_localization, SWT.NONE);
		GridData gd_browserLocalization = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_browserLocalization.widthHint = 184;
		gd_browserLocalization.heightHint = 250;
		browserLocalization.setLayoutData(gd_browserLocalization);
		*/
		
		tbtmHomologs = new TabItem(tabFolder, SWT.NONE);
		tbtmHomologs.setText("Homologs");

		composite_13 = new Composite(tabFolder, SWT.NONE);
		tbtmHomologs.setControl(composite_13);
		composite_13.setLayout(new GridLayout(3, false));

		Composite composite_18 = new Composite(composite_13, SWT.NONE);
		composite_18.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		composite_18.setLayout(new GridLayout(1, false));

		browserHomolog = new Browser(composite_18, SWT.BORDER);
		GridData gd_browserHomolog = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_browserHomolog.widthHint = 430;
		browserHomolog.setLayoutData(gd_browserHomolog);

		Composite composite_16 = new Composite(composite_13, SWT.BORDER);
		composite_16.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_16.setLayout(new GridLayout(1, false));

		Composite composite_171 = new Composite(composite_16, SWT.NONE);
		composite_171.setLayout(new GridLayout(5, false));
		btnSelectall = new Button(composite_171, SWT.TOGGLE);
		btnSelectall.setToolTipText("Select all genes");
		btnSelectall.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));
		btnSelectall.setBackground(BasicColor.BUTTON);
		btnSelectall.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/checked.bmp"));
		btnSelectall.addSelectionListener(this);
		btnSelectall.setText("Select all");

		btnUnselectall = new Button(composite_171, SWT.TOGGLE);
		btnUnselectall.setBackground(BasicColor.BUTTON);
		btnUnselectall.setToolTipText("Unselect all genes");
		btnUnselectall.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));
		btnUnselectall.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/unchecked.bmp"));
		btnUnselectall.addSelectionListener(this);
		btnUnselectall.setText("Unselect all");

		btnDownloadtxt = new Button(composite_171, SWT.TOGGLE);
		btnDownloadtxt.setText("Export selection as table");
		btnDownloadtxt.setBackground(BasicColor.BUTTON);
		btnDownloadtxt.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));
		btnDownloadtxt.addSelectionListener(this);
		
		btnExportToFasta = new Button(composite_171, SWT.TOGGLE);
		btnExportToFasta.setText("Export selection as fasta");
		btnExportToFasta.setBackground(BasicColor.BUTTON);
		btnExportToFasta.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));
		btnExportToFasta.addSelectionListener(this);

		btnSaveAsSvg = new Button(composite_171, SWT.TOGGLE);
		btnSaveAsSvg.setToolTipText("Download as SVG vector image (for Illustrator, GIMP, ...)");
		btnSaveAsSvg.setBackground(BasicColor.BUTTON);
		btnSaveAsSvg.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));
		btnSaveAsSvg.setText("Download tree");
		btnSaveAsSvg.addSelectionListener(this);
		
		
		Composite composite_17 = new Composite(composite_16, SWT.NONE);
		composite_17.setLayout(new GridLayout(5, false));

		lblConservation2 = new Label(composite_17, SWT.NONE);
		lblConservation2.setText("Homologs in 000/000 Yersinia genomes");

		new Label(composite_17, SWT.NONE);
		new Label(composite_17, SWT.NONE);
		
		Label lblSearch_1 = new Label(composite_17, SWT.NONE);
		lblSearch_1.setText("Search:");
		lblSearch_1.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));
		
		txtSearchGenome = new Text(composite_17, SWT.BORDER);
		txtSearchGenome.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearchGenome.setToolTipText("Search");

		txtSearchGenome.addKeyListener(new KeyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == 16777296 || e.keyCode == 13) {
					if (!txtSearchGenome.getText().equals("")) {
						tableHomologViewer.refresh();
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {}
		});

	

		
		Label lblClickOneTime = new Label(composite_16, SWT.NONE);
		lblClickOneTime.setText(
				"Select strain to highlight. Double click to acces gene information");
		lblClickOneTime.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		
		
		tableHomologViewer = new TableViewer(composite_16, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableHomolog = tableHomologViewer.getTable();
		RWTUtils.setMarkup(tableHomolog);
		tableHomolog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		tableHomologViewer.getTable().setHeaderVisible(true);
		tableHomologViewer.getTable().setLinesVisible(true);
		tableHomologViewer.setLabelProvider(new TableLabelProvider());
		tableHomologViewer.addSelectionChangedListener(new ISelectionChangedListener() {

		@Override
			public void selectionChanged(SelectionChangedEvent event) {
				for (int i : tableHomolog.getSelectionIndices()) {
					String selectedGenome = tableHomolog.getItem(i).getText(columnNames.indexOf("Name (GenBank)") + 1);
					selectedGenome = GenomeNCBI.processGenomeName(selectedGenome);
					if (selectedGenomes.contains(selectedGenome)) {
						if (tableHomolog.getSelectionIndices().length == 1) {
							selectedGenomes.remove(selectedGenome);
							tableHomologViewer.replace(tableHomologViewer.getTable().getItem(i).getData(), i);
						}
					} else {
						selectedGenomes.add(selectedGenome);
						tableHomologViewer.replace(tableHomologViewer.getTable().getItem(i).getData(), i);
					}
				}
				GeneViewHomologTools.loadFigureHomologs(sequence, browserHomolog, selectedGenomes);
			}
		});
		tableHomologViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				String selectedGenome =
						tableHomologViewer.getTable().getItem(tableHomologViewer.getTable().getSelectionIndex())
						.getText(columnNames.indexOf("Name (GenBank)") + 1);
				String selectedGene =
						tableHomologViewer.getTable().getItem(tableHomologViewer.getTable().getSelectionIndex())
						.getText(columnNames.indexOf("Homolog Locus") + 1);
				Genome genome = Genome.loadGenome(selectedGenome);
				ArrayList<String> genomeNames = new ArrayList<>();
				genomeNames.add(selectedGenome);
				/*
				if(selectedGenome.equals("Yersinia pestis CO92")||selectedGenome.equals("Yersinia pestis KIM5")||selectedGenome.equals("Yersinia pestis 91001")
						||selectedGenome.equals("Yersinia pseudotuberculosis YPIII")||selectedGenome.equals("Yersinia pseudotuberculosis IP32953")||selectedGenome.equals("Yersinia entomophaga MH96")
						||selectedGenome.equals("Yersinia enterocolitica Y1")||selectedGenome.equals("Yersinia enterocolitica Y11")) {
					generalNetwork = new Network();
					generalNetwork = Network.load(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());
				} */
				Gene gene = genome.getGeneFromName(selectedGene);
				GeneView.displayGene(gene, partService);
			}
		});
		new Label(composite_13, SWT.NONE);

		/*
		 * Differential transcriptomics section
		 */
		{
			tbtmExpressionData = new TabItem(tabFolder, SWT.NONE);
			tbtmExpressionData.setText("Transcript differential expressions");

			compositeTranscriptome = new Composite(tabFolder, SWT.BORDER);
			tbtmExpressionData.setControl(compositeTranscriptome);
			compositeTranscriptome.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			compositeTranscriptome.setLayout(new GridLayout(2, false));

			Composite composite_8 = new Composite(compositeTranscriptome, SWT.BORDER);
			composite_8.setLayout(new GridLayout(2, false));

			{
				Composite composite_logfc = new Composite(composite_8, SWT.NONE);
				composite_logfc.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				composite_logfc.setLayout(new GridLayout(2, false));

				Label lbllogfoldchange = new Label(composite_logfc, SWT.NONE);
				lbllogfoldchange.setText("|Log2(Fold-Change)| >");

				txtCutoffLogFC = new Text(composite_logfc, SWT.BORDER);
				txtCutoffLogFC.setText(GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF+"");
			}
			{
				Composite  composite_pvalue = new Composite(composite_8, SWT.NONE);
				composite_pvalue.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				composite_pvalue.setLayout(new GridLayout(4, false));

				Label lblAnd = new Label(composite_pvalue, SWT.NONE);
				lblAnd.setText("and");

				Label lblPvalue = new Label(composite_pvalue, SWT.NONE);
				lblPvalue.setText("p-value <");

				txtCutoffPvalue = new Text(composite_pvalue, SWT.BORDER);
				txtCutoffPvalue.setText(GenomeElementAtlas.DEFAULT_PVAL_CUTOFF+"");
				
				Label lblZeroPvalue = new Label(composite_pvalue, SWT.NONE);
				lblZeroPvalue.setText("0.00 means that the Log2(FC) is not known.\n0.0e+00 means the p-value or the adjusted p-value is not known.");
				lblZeroPvalue.setForeground(BasicColor.GREY);
				
				/*
				if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
					composite_pvalue.dispose();
				}*/
			}
			btnUpdateCutoff = new Button(composite_8, SWT.TOGGLE);
			btnUpdateCutoff.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
			btnUpdateCutoff.setText("Choose cut-off and update transcript differential expressions");
			btnUpdateCutoff.setBackground(BasicColor.BUTTON);
			btnUpdateCutoff.addSelectionListener(this);

			Composite composite_3 = new Composite(compositeTranscriptome, SWT.NONE);
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

			btnGenomeViewer = new Button(composite_3, SWT.TOGGLE);
			btnGenomeViewer.setBackground(BasicColor.BUTTON);
			btnGenomeViewer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnGenomeViewer.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genomeViewer.bmp"));
			btnGenomeViewer.addSelectionListener(this);

			btnHeatmapview = new Button(composite_3, SWT.TOGGLE);
			btnHeatmapview.setBackground(BasicColor.BUTTON);
			btnHeatmapview.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnHeatmapview.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/compareexpression.bmp"));
			btnHeatmapview.addSelectionListener(this);

			Label lblOverExpressedIn = new Label(compositeTranscriptome, SWT.NONE);
			lblOverExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblOverExpressedIn.setText("Over expressed in");

			lblOver = new Label(compositeTranscriptome, SWT.NONE);
			lblOver.setText("over");

			tableOver = new Table(compositeTranscriptome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listOver = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listOver.heightHint = 150;
			RWTUtils.setMarkup(tableOver);
			tableOver.setLayoutData(gd_listOver);
			tableOver.addSelectionListener(this);

			Label lblUnderExpressedIn = new Label(compositeTranscriptome, SWT.NONE);
			lblUnderExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblUnderExpressedIn.setText("Under expressed in");

			lblUnder = new Label(compositeTranscriptome, SWT.NONE);
			lblUnder.setText("under");

			tableUnder = new Table(compositeTranscriptome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listUnder = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listUnder.heightHint = 150;
			tableUnder.setLayoutData(gd_listUnder);
			tableUnder.addSelectionListener(this);
			RWTUtils.setMarkup(tableUnder);

			Label lblNoDiffExpression = new Label(compositeTranscriptome, SWT.NONE);
			lblNoDiffExpression.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblNoDiffExpression.setText("No diff. expression in");

			lblNodiff = new Label(compositeTranscriptome, SWT.NONE);
			lblNodiff.setText("nodiff");

			tableNodiff = new Table(compositeTranscriptome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listNodiff = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listNodiff.heightHint = 150;
			tableNodiff.setLayoutData(gd_listNodiff);
			tableNodiff.addSelectionListener(this);
			RWTUtils.setMarkup(tableNodiff);

		}

		/*
		 * Transpcriptome Expression (RNASeq) tab
		 */

		tbtmTranscriptomes = new TabItem(tabFolder, SWT.NONE);
		tbtmTranscriptomes.setText("Transcriptomes");


		composite_101 = new Composite(tabFolder, SWT.NONE);
		tbtmTranscriptomes.setControl(composite_101);
		composite_101.setLayout(new GridLayout(1, false));
		composite_101.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		composite_102 = new Composite(composite_101, SWT.NONE);
		composite_102.setLayout(new GridLayout(1, false));

		Label transcriptomesExpl = new Label(composite_102, SWT.NONE);
		transcriptomesExpl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 0, 0));
		transcriptomesExpl.setText("For RNASeq experiments, feature counts were normalized using the Transcripts Per Million (TPM) method.\nThis value is displayed here and is only indicative for the presence or absence of transcript. It should not be used for direct analysis.");
		transcriptomesExpl.setForeground(BasicColor.GREY);
		
		composite_103 = new Composite(composite_101, SWT.NONE);
		composite_103.setLayout(new GridLayout(2, false));

		Label lblOverExpressedInTranscriptomes = new Label(composite_103, SWT.NONE);
		lblOverExpressedInTranscriptomes.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 0, 0));
		lblOverExpressedInTranscriptomes.setText("Found in");

		lblExprTranscriptomes = new Label(composite_103, SWT.NONE);
		lblExprTranscriptomes.setText("");

		
		tableTranscriptomes = new Table(composite_101, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableTranscriptomes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		//scrolledComposite.setContent(composite_11);
		//scrolledComposite.setMinSize(composite_11.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		/*
		 * Protein atlas section
		 */

		{
			tbtmProteomeData = new TabItem(tabFolder, SWT.NONE);
			tbtmProteomeData.setText("Protein differential abundances");

			compositeProteome = new Composite(tabFolder, SWT.BORDER);
			tbtmProteomeData.setControl(compositeProteome);
			compositeProteome.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			compositeProteome.setLayout(new GridLayout(2, false));

			Composite composite_8 = new Composite(compositeProteome, SWT.BORDER);
			composite_8.setLayout(new GridLayout(2, false));

			{
				Composite composite_logfc = new Composite(composite_8, SWT.NONE);
				composite_logfc.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				composite_logfc.setLayout(new GridLayout(2, false));

				Label lbllogfoldchange = new Label(composite_logfc, SWT.NONE);
				lbllogfoldchange.setText("|Log2(Fold-Change)| >");

				txtCutoffLogFCProteome = new Text(composite_logfc, SWT.BORDER);
				txtCutoffLogFCProteome.setText(GenomeElementAtlas.DEFAULT_LOGFC_PROTEOMIC_CUTOFF+"");
			}
			{
				Composite  composite_pvalue = new Composite(composite_8, SWT.NONE);
				composite_pvalue.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				composite_pvalue.setLayout(new GridLayout(4, false));

				Label lblAnd = new Label(composite_pvalue, SWT.NONE);
				lblAnd.setText("and");

				Label lblPvalueFdrby = new Label(composite_pvalue, SWT.NONE);
				lblPvalueFdrby.setText("p-value <");

				txtCutoffPvalueProteome = new Text(composite_pvalue, SWT.BORDER);
				txtCutoffPvalueProteome.setText(GenomeElementAtlas.DEFAULT_PVAL_PROTEOMIC_CUTOFF+"");
				
				Label lblZeroPvalue = new Label(composite_pvalue, SWT.NONE);
				lblZeroPvalue.setText("0.00 means that the Log2(FC) is not known.\n0.0e+00 means the p-value or the adjusted p-value is not known.");
				lblZeroPvalue.setForeground(BasicColor.GREY);
				
				/*if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
					composite_pvalue.dispose();
				}*/
			}
			btnUpdateCutoffProteome = new Button(composite_8, SWT.TOGGLE);
			btnUpdateCutoffProteome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
			btnUpdateCutoffProteome.setText("Choose cut-off and update protein differential abundances");
			btnUpdateCutoffProteome.setBackground(BasicColor.BUTTON);

			btnUpdateCutoffProteome.addSelectionListener(this);

			Composite composite_3 = new Composite(compositeProteome, SWT.NONE);
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

			btnGenomeViewerProteome = new Button(composite_3, SWT.TOGGLE);
			btnGenomeViewerProteome.setBackground(BasicColor.BUTTON);
			btnGenomeViewerProteome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnGenomeViewerProteome.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genomeViewer.bmp"));
			btnGenomeViewerProteome.addSelectionListener(this);

			btnHeatmapviewProteome = new Button(composite_3, SWT.TOGGLE);
			btnHeatmapviewProteome.setBackground(BasicColor.BUTTON);

			btnHeatmapviewProteome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnHeatmapviewProteome.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/compareexpression.bmp"));
			btnHeatmapviewProteome.addSelectionListener(this);

			Label lblOverExpressedIn = new Label(compositeProteome, SWT.NONE);
			lblOverExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblOverExpressedIn.setText("Over expressed in");

			lblOverProteome = new Label(compositeProteome, SWT.NONE);
			lblOverProteome.setText("over");

			tableOverProteome = new Table(compositeProteome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listOver = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listOver.heightHint = 150;
			tableOverProteome.setLayoutData(gd_listOver);
			tableOverProteome.addSelectionListener(this);
			RWTUtils.setMarkup(tableOverProteome);

			Label lblUnderExpressedIn = new Label(compositeProteome, SWT.NONE);
			lblUnderExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblUnderExpressedIn.setText("Under expressed in");

			lblUnderProteome = new Label(compositeProteome, SWT.NONE);
			lblUnderProteome.setText("under");

			tableUnderProteome = new Table(compositeProteome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listUnder = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listUnder.heightHint = 150;
			tableUnderProteome.setLayoutData(gd_listUnder);
			tableUnderProteome.addSelectionListener(this);
			RWTUtils.setMarkup(tableUnderProteome);

			Label lblNoDiffExpression = new Label(compositeProteome, SWT.NONE);
			lblNoDiffExpression.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblNoDiffExpression.setText("No diff. expression in");

			lblNodiffProteome = new Label(compositeProteome, SWT.NONE);
			lblNodiffProteome.setText("nodiff");

			tableNodiffProteome = new Table(compositeProteome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listNodiff = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listNodiff.heightHint = 150;
			tableNodiffProteome.setLayoutData(gd_listNodiff);
			tableNodiffProteome.addSelectionListener(this);
			RWTUtils.setMarkup(tableNodiffProteome);

		}

		tbtmProteomes = new TabItem(tabFolder, SWT.NONE);
		tbtmProteomes.setText("Proteomes");

		composite_09 = new Composite(tabFolder, SWT.NONE);
		tbtmProteomes.setControl(composite_09);
		composite_09.setLayout(new GridLayout(1, false));

		composite_091 = new Composite(composite_09, SWT.NONE);
		composite_091.setLayout(new GridLayout(1, false));

		Label proteomesExpl1 = new Label(composite_091, SWT.NONE);
		proteomesExpl1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		proteomesExpl1.setText("For Orbitrap experiments, displayed value is log10(raw LFQ)."
				+ "\nFor FTICR experiments, displayed value is raw FTICR intensity."
				+ "\nFor 2D gel experiments, if proteins are detected in several spots, displayed value is the most intense measured spot intensity value."
				+ "\n'-1' means that the protein is detected but no value is available.");

		proteomesExpl1.setForeground(BasicColor.GREY);

		composite_092 = new Composite(composite_09, SWT.NONE);
		composite_092.setLayout(new GridLayout(2, false));

		Label lblOverExpressedInProteomes = new Label(composite_092, SWT.NONE);
		lblOverExpressedInProteomes.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOverExpressedInProteomes.setText("Found in");

		lblExprProteomes = new Label(composite_092, SWT.NONE);
		lblExprProteomes.setText("");

		tableProteomes = new Table(composite_09, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableProteomes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		/*
		 * Co-Expression viewer
		 */

		//tbtmCoExp = new TabItem(tabFolder, SWT.NONE);
		//tbtmCoExp.setText("Co-Expression");
		
		/*
		 * Synteny viewer
		 */
		/*
		tbtmSynteny = new TabItem(tabFolder, SWT.NONE);
		tbtmSynteny.setText("Synteny");

		compositeSynteny = new Composite(tabFolder, SWT.NONE);
		tbtmSynteny.setControl(compositeSynteny);
		compositeSynteny.setLayout(new GridLayout(1, false));
		//browserSynteny = new Browser(compositeSynteny, SWT.NONE);
		//browserSynteny.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT
				|| Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT || Database.getInstance().getProjectName() == Database.YERSINIOMICS_PROJECT || Database.getInstance().getProjectName() == Database.URY_YERSINIOMICS_PROJECT) {
			initSyntenyBrowser();
		}

		/*
		 * KEGG
		 */
		
		tbtmKEGG = new TabItem(tabFolder, SWT.NONE);
		//tbtmKEGG.setText("KEGG");
		tbtmKEGG.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/KEGG.png"));

		compositeKEGG = new Composite(tabFolder, SWT.NONE);
		tbtmKEGG.setControl(compositeKEGG);
		compositeKEGG.setLayout(new GridLayout(1, false));
		btnKEGG = new Button(compositeKEGG, SWT.TOGGLE);
		btnKEGG.addSelectionListener(this);
		btnKEGG.setText("Open KEGG in a new tab of your browser");
		btnKEGG.setBackground(BasicColor.BUTTON);
		browserKEGG = new Browser(compositeKEGG, SWT.NONE);
		browserKEGG.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browserKEGG.setUrl("");
		
		/*
		 * Uniprot
		 */
		
		tbtmUniprot = new TabItem(tabFolder, SWT.NONE);
		//tbtmUniprot.setText("UniProt");
		tbtmUniprot.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/uniprot.png"));

		compositeUniprot = new Composite(tabFolder, SWT.NONE);
		tbtmUniprot.setControl(compositeUniprot);
		compositeUniprot.setLayout(new GridLayout(1, false));
		btnUniprot = new Button(compositeUniprot, SWT.TOGGLE);
		btnUniprot.addSelectionListener(this);
		btnUniprot.setBackground(BasicColor.BUTTON);

		btnUniprot.setText("Open UniProt in a new tab of your browser");
		browserUniprot = new Browser(compositeUniprot, SWT.NONE);
		browserUniprot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browserUniprot.setUrl("");
		
		/*
		 * InterPro
		 */
		
		tbtmInterpro = new TabItem(tabFolder, SWT.NONE);
		tbtmInterpro.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/interpro.png"));

		compositeInterpro = new Composite(tabFolder, SWT.NONE);
		tbtmInterpro.setControl(compositeInterpro);
		compositeInterpro.setLayout(new GridLayout(1, false));
		btnInterpro = new Button(compositeInterpro, SWT.TOGGLE);
		btnInterpro.addSelectionListener(this);
		btnInterpro.setBackground(BasicColor.BUTTON);

		btnInterpro.setText("Open InterPro in a new tab of your browser");
		browserInterpro = new Browser(compositeInterpro, SWT.NONE);
		browserInterpro.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browserInterpro.setUrl("");
		
		/*
		 * IntAct
		 */
		
		tbtmIntact = new TabItem(tabFolder, SWT.NONE);
		tbtmIntact.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/intact.png"));
		compositeIntact = new Composite(tabFolder, SWT.NONE);
		tbtmIntact.setControl(compositeIntact);
		compositeIntact.setLayout(new GridLayout(1, false));
		btnIntact = new Button(compositeIntact, SWT.TOGGLE);
		btnIntact.setBackground(BasicColor.BUTTON);

		btnIntact.addSelectionListener(this);
		btnIntact.setText("Open IntAct in a new tab of your browser");
		browserIntact = new Browser(compositeIntact, SWT.NONE);
		browserIntact.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browserIntact.setUrl("");
		
		/*
		 * String
		 */
		
		tbtmString = new TabItem(tabFolder, SWT.NONE);
		//tbtmString.setText("String");
		tbtmString.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/string.png"));
		compositeString = new Composite(tabFolder, SWT.NONE);
		tbtmString.setControl(compositeString);
		compositeString.setLayout(new GridLayout(1, false));
		compositeString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		compositeString_row_1 = new Composite(compositeString, SWT.NONE);
		compositeString_row_1.setLayout(new GridLayout(2, false));
		compositeString_row_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		btnString = new Button(compositeString_row_1, SWT.TOGGLE);
		btnString.addSelectionListener(this);
		btnString.setBackground(BasicColor.BUTTON);
		btnString.setText("Open STRING in a new tab of your browser");
		
		Label lblString = new Label(compositeString_row_1, SWT.NONE);
		lblString.setText("Open Yersiniomics in Mozilla Firefox if STRING cannot load.");		
		
		browserString = new Browser(compositeString, SWT.NONE);
		browserString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browserString.setText("");
			
	}

	
	
    public static class OpenGenomesAndNetworkThread implements IRunnableWithProgress {
        private ArrayList<String> genomeNames = new ArrayList<>();
        Network generalNetwork = new Network();
        public OpenGenomesAndNetworkThread(ArrayList<String> genomeNames) {
            this.genomeNames = genomeNames;
        }
        
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            int sizeProcess = genomeNames.size()*2;
            // Tell the user what you are doing
            monitor.beginTask("Loading genomes and co-expression networks", sizeProcess);
            monitor.worked(1);
            // Optionally add subtasks
            int i = 1;
            for (String genomeName : genomeNames) {
                monitor.subTask("Loading genome " + i + "/" + genomeNames.size()*2 + " : " + genomeName);
                Genome.loadGenome(genomeName);
                monitor.worked(1);
                monitor.subTask("Loading co-expression " + 2*i + "/" + genomeNames.size()*2 + " : " + genomeName);
                Network.load(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genomeName);
                i=i+2;
            }
            // You are done
            monitor.done();
        }
    }
    
 
	
	private void updateSyntenyBrowser() {
		compSynt.dispose();
		compSynt = new Composite(composite_1, SWT.BORDER);
		GridData gd_compSynt = new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1);
		compSynt.setLayoutData(gd_compSynt);
		compSynt.setLayout(new GridLayout(1, false));
		composite_1.layout(true,true);

		if(syntenyHashMap.containsKey(genome.getSpecies())){
			if (Database.getInstance().getProjectName() == Database.URY_YERSINIOMICS_PROJECT || (Database.getInstance().getProjectName() == Database.YERSINIOMICS_PROJECT) && !genome.getSpecies().equals("Yersinia pseudotuberculosis IP31758")) {
				btnShowSynteny = new Button(compSynt, SWT.TOGGLE);
				btnShowSynteny.setBackground(BasicColor.BUTTON);
				btnShowSynteny.setText("Show synteny");
				btnShowSynteny.setToolTipText("Load the genome synteny computed with the other genomes of the same genus available in Yersiniomics, in the SynTVieW software.");
				btnShowSynteny.addSelectionListener(this);
				btnShowSynteny.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
				btnShowSynteny.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
				browserSynteny = new Browser(compSynt, SWT.NONE);
				browserSynteny.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				composite_1.layout(true,true);
			}
		}
	}
	
	private void loadSynteny() {
		String syntenyURL = new String();
		String syntenyURLPrefix = "https://yersiniomics.pasteur.fr/SynTView/site/?dataDir=\"data/";
		if (Database.getInstance().getProjectName() == Database.URY_YERSINIOMICS_PROJECT && genome.getSpecies().equals("Yersinia pseudotuberculosis IP31758")) {
			syntenyURLPrefix = "https://yersiniomics.pasteur.fr/SynTView/siteRestricted/?dataDir=\"data/";
		}
		try {
			//pathGraphHTML = "http://hub15.hosting.pasteur.fr:8080/SynTView/site/?dataDir=\"data/CO92\"";
			syntenyURL = syntenyURLPrefix + syntenyHashMap.get(genome.getSpecies()) + "\"";
			browserSynteny.setUrl(syntenyURL);
		} catch (Exception e) {
			System.out.println("Cannot create Synteny browser");
		}
	}
	
	private void updateCrossRefs() {
		
		try {
			if(KEGGHashMap.containsKey(genome.getSpecies())) {
				String locus = sequence.getOldLocusTag();
				if (locus.equals("")) {
					locus = sequence.getName();
				} else {
					if(genome.getSpecies().equals("Yersinia pestis 91001") || genome.getSpecies().equals("Clostridioides difficile 630Derm")) {
						String[] splitLoc = locus.split("%2C");
						if (splitLoc.length==2) {
							locus = splitLoc[1];
						} else {
							locus = sequence.getName();
						}		
					}
				}

					KEGGPath = "https://www.genome.jp/entry/"+ KEGGHashMap.get(genome.getSpecies()) + ":" + locus;
					browserKEGG.setUrl(KEGGPath);
					
					IntactPath = "https://www.ebi.ac.uk/intact/search?query="+ locus;
						    
					// open and read HTML page at KEGG conversion API tool
					URL url = new URL("https://rest.kegg.jp/conv/uniprot/"+ KEGGHashMap.get(genome.getSpecies()) + ":" + locus);
					InputStream is = url.openStream();
					int ptr = 0;
					StringBuffer buffer = new StringBuffer();
					while ((ptr = is.read()) != -1) {
					    buffer.append((char)ptr);
					}
					if (buffer.length()==1) { //if string buffer is empty because no Uniprot accession is found in KEGG conversion tool
						browserUniprot.setText("<h2 style=\"text-align:center\">Could not find UniProt ID</h2>");
						UniprotPath = "";
						browserInterpro.setText("<h2 style=\"text-align:center\">Could not find UniProt ID for InterPro query</h2>");
						InterproPath = "";

						//query IntAct only by locus name
						IntactPath = "https://www.ebi.ac.uk/intact/search?query="+ locus;
						browserIntact.setUrl(IntactPath);
						
					} else {
						String conversion = buffer.toString();
						String[] splitUp = conversion.split(":");
						String UpAccession = splitUp[2].substring(0,splitUp[2].length()-1);
						UniprotPath ="https://www.uniprot.org/uniprotkb/"+UpAccession;
						browserUniprot.setUrl(UniprotPath);	
						InterproPath ="https://www.ebi.ac.uk/interpro/protein/UniProt/"+UpAccession;
						browserInterpro.setUrl(InterproPath);
						
						//query IntAct by locus name and UniProt accession
						IntactPath = "https://www.ebi.ac.uk/intact/search?query="+ locus + "%20" + UpAccession;
						browserIntact.setUrl(IntactPath);

					}
			} else {
				System.out.println("does not contains key genome for KEGG browser: "+ genome.getSpecies());
			}
		} catch (Exception e) {
			System.out.println("Cannot create KEGG browser");
		}
		
		/*
		 * Update String Network
		 */
		
		if (stringHashMap.containsKey(genome.getSpecies())) {
			String locus2 = sequence.getOldLocusTag();

			if (locus2.equals("")) {
				locus2 = sequence.getName();
			}
			StringPath = "https://string-db.org/api/tsv-no-header/get_link?identifiers="+ locus2 +"&species="+stringHashMap.get(genome.getSpecies());
			try {
				InputStream in = new URL(StringPath).openStream();
				String result = IOUtils.toString(in);
				stringURL = result;
				browserString.setUrl(stringURL);
			} catch (Exception e) {
				stringURL = "";
				browserString.setText("<h2 style=\"text-align:center\">Could not find String ID</h2>");
			}
		} 
	}
	
	private void updateCrossRefsBrowsers() {		
		
		tbtmKEGG.dispose();
		tbtmKEGG = new TabItem(tabFolder, SWT.NONE);
		tbtmKEGG.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/KEGG.png"));

		compositeKEGG = new Composite(tabFolder, SWT.NONE);
		tbtmKEGG.setControl(compositeKEGG);
		compositeKEGG.setLayout(new GridLayout(1, false));
		btnKEGG = new Button(compositeKEGG, SWT.TOGGLE);
		btnKEGG.addSelectionListener(this);
		btnKEGG.setText("Open KEGG in a new tab of your browser");
		btnKEGG.setBackground(BasicColor.BUTTON);
		browserKEGG = new Browser(compositeKEGG, SWT.NONE);
		browserKEGG.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tbtmUniprot.dispose();
		tbtmUniprot = new TabItem(tabFolder, SWT.NONE);
		tbtmUniprot.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/uniprot.png"));

		compositeUniprot = new Composite(tabFolder, SWT.NONE);
		tbtmUniprot.setControl(compositeUniprot);
		compositeUniprot.setLayout(new GridLayout(1, false));
		btnUniprot = new Button(compositeUniprot, SWT.TOGGLE);
		btnUniprot.setBackground(BasicColor.BUTTON);
		btnUniprot.addSelectionListener(this);
		btnUniprot.setText("Open UniProt in a new tab of your browser");
		browserUniprot = new Browser(compositeUniprot, SWT.NONE);
		browserUniprot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tbtmInterpro.dispose();
		tbtmInterpro = new TabItem(tabFolder, SWT.NONE);
		tbtmInterpro.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/interpro.png"));
		compositeInterpro = new Composite(tabFolder, SWT.NONE);
		tbtmInterpro.setControl(compositeInterpro);
		compositeInterpro.setLayout(new GridLayout(1, false));
		btnInterpro = new Button(compositeInterpro, SWT.TOGGLE);
		btnInterpro.addSelectionListener(this);
		btnInterpro.setBackground(BasicColor.BUTTON);
		btnInterpro.setText("Open InterPro in a new tab of your browser");
		browserInterpro = new Browser(compositeInterpro, SWT.NONE);
		browserInterpro.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tbtmIntact.dispose();
		tbtmIntact = new TabItem(tabFolder, SWT.NONE);
		tbtmIntact.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/intact.png"));
		compositeIntact = new Composite(tabFolder, SWT.NONE);
		tbtmIntact.setControl(compositeIntact);
		compositeIntact.setLayout(new GridLayout(1, false));
		btnIntact = new Button(compositeIntact, SWT.TOGGLE);
		btnIntact.addSelectionListener(this);
		btnIntact.setBackground(BasicColor.BUTTON);
		btnIntact.setText("Open IntAct in a new tab of your browser");
		browserIntact = new Browser(compositeIntact, SWT.NONE);
		browserIntact.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tbtmString.dispose();

		if (stringHashMap.containsKey(genome.getSpecies())) {
			tbtmString = new TabItem(tabFolder, SWT.NONE);
			compositeString = new Composite(tabFolder, SWT.NONE);
			tbtmString.setControl(compositeString);
			tbtmString.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/string.png"));
			compositeString.setLayout(new GridLayout(1, false));
			compositeString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			compositeString_row_1 = new Composite(compositeString, SWT.NONE);
			compositeString_row_1.setLayout(new GridLayout(2, false));
			compositeString_row_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			
			btnString = new Button(compositeString_row_1, SWT.TOGGLE);
			btnString.addSelectionListener(this);
			btnString.setBackground(BasicColor.BUTTON);
			btnString.setText("Open STRING in a new tab of your browser");
			
			Label lblString = new Label(compositeString_row_1, SWT.NONE);
			lblString.setText("Open Yersiniomics in Mozilla Firefox if STRING cannot load.");	
			
			browserString = new Browser(compositeString, SWT.NONE);
			browserString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			browserString.setText("");
	
		} else {
			
		}
		
		
		
		try {
			

			if (KEGGHashMap.containsKey(genome.getSpecies())){
				KEGGPath = "";
				UniprotPath = "";
				IntactPath = "";
				InterproPath = "";
				browserKEGG.setUrl(KEGGPath);
				browserUniprot.setUrl(UniprotPath);
				browserIntact.setUrl(IntactPath);
				browserInterpro.setUrl(InterproPath);

				
			} else {
				tbtmKEGG.dispose();
				tbtmUniprot.dispose();
				tbtmIntact.dispose();
				tbtmInterpro.dispose();
				}

		} catch (Exception e) {
			System.out.println("Cannot create browser");
		}
	}

	/**
	 * When a genome is selected this method will trigger every update possible of the widgets
	 * 
	 * @param genomeName
	 */
	
	
	
	private void initGenomeInfo(String genomeName) {
		

		try {
			ArrayList<String> genomeNames = new ArrayList<>();
			genomeNames.add(genomeName);
			
			/*
			if(genomeName.equals("Yersinia pestis CO92")||genomeName.equals("Yersinia pestis KIM5")||genomeName.equals("Yersinia pestis 91001")
					||genomeName.equals("Yersinia pseudotuberculosis YPIII")||genomeName.equals("Yersinia pseudotuberculosis IP32953")||genomeName.equals("Yersinia entomophaga MH96")
					||genomeName.equals("Yersinia enterocolitica Y1")||genomeName.equals("Yersinia enterocolitica Y11")) {
				
				OpenGenomesAndNetworkThread thread = new OpenGenomesAndNetworkThread(genomeNames);
				new ProgressMonitorDialog(this.shell).run(true, false, thread);
				genome = Genome.loadGenome(genomeName);
				generalNetwork = new Network();
				generalNetwork = Network.load(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());
				updateCoExpViewer();
			} else { */
				OpenGenomesThread thread = new OpenGenomesThread(genomeNames);
				new ProgressMonitorDialog(this.shell).run(true, false, thread);
				genome = Genome.loadGenome(genomeName);
			//}

		
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		//for (String genomeItem : comboGenome.getItems()) {System.out.println("before initGenome Info genomeItem: "+genomeItem)}
		initGenomeInfo();
		updateSyntenyBrowser();
		updateCrossRefsBrowsers();	
		//for (String genomeItem2 : comboGenome.getItems()) {System.out.println("after initGenome Info genomeItem: "+genomeItem2)}
		this.setGenomeSelected(genomeName);
		this.chromoID = genome.getFirstChromosome().getChromosomeID();
		updateComboChromosome(this.chromoID);
		updateListGenomeElements();
		updateGenomeViewer();
		updateGeneInfo();    
	}

	/**
	 * Init GeneView accrodingly to the project selected
	 * NEED TO BE MODIFIED TO BE LESS DEPENDENT OF PROJECT SELECTION
	 */
	private void initGenomeInfo(){
		if (Database.getInstance().getProjectName().equals(Database.LEISHOMICS_PROJECT)) {
			initLeishmania();
		} else if (Database.getInstance().getProjectName().equals(Database.CRISPRGO_PROJECT)) {
			initCrispromics();
		} else if (Database.getInstance().getProjectName().equals("Yersiniomics")) {
			initYersiniomics();
		} else if (Database.getInstance().getProjectName().equals(Database.UIBCLISTERIOMICS_PROJECT)
				|| Database.getInstance().getProjectName().equals(Database.LISTERIOMICS_PROJECT)) {
			initListeriomics();
		} else if (Database.getInstance().getProjectName() == "ListeriomicsSample") {
			initListeriomicsSample();
		} else if (Database.getInstance().getProjectName() == Database.YERSINIOMICS_PROJECT || Database.getInstance().getProjectName() == Database.URY_YERSINIOMICS_PROJECT) {
			initYersiniomics();
		} else if (Database.getInstance().getProjectName() == Database.CLOSTRIDIOMICS_PROJECT) {
			initYersiniomics();
		} else {
			initDefault();
		}
	}

	/**
	 * When a genome is selected this method will trigger every update possible of the widgets
	 * 
	 * @param genomeName
	 */
	private void initGenomeInfo(String genomeName, String chromoID) {
		//System.out.println("initGenomeInfo 2");
		try {
			ArrayList<String> genomeNames = new ArrayList<>();
			genomeNames.add(genomeName);
			/*
			if(genomeName.equals("Yersinia pestis CO92")||genomeName.equals("Yersinia pestis KIM5")||genomeName.equals("Yersinia pestis 91001")
					||genomeName.equals("Yersinia pseudotuberculosis YPIII")||genomeName.equals("Yersinia pseudotuberculosis IP32953")||genomeName.equals("Yersinia entomophaga MH96")
					||genomeName.equals("Yersinia enterocolitica Y1")||genomeName.equals("Yersinia enterocolitica Y11")) {
				OpenGenomesAndNetworkThread thread = new OpenGenomesAndNetworkThread(genomeNames);
				new ProgressMonitorDialog(this.shell).run(true, false, thread);
				genome = Genome.loadGenome(genomeName);
				generalNetwork = new Network();
				generalNetwork = Network.load(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());
			} else { */
				OpenGenomesThread thread = new OpenGenomesThread(genomeNames);
				new ProgressMonitorDialog(this.shell).run(true, false, thread);
				genome = Genome.loadGenome(genomeName);
			//}

		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		initGenomeInfo();

		this.chromoID = chromoID;
				
		updateComboChromosome(this.chromoID);
		updateListGenomeElements();
		updateGenomeViewer();
		updateGeneInfo();
	}

	/**
	 * Remove some Widgets for Leishmania view
	 */
	private void initLeishmania() {
		tbtmExpressionData.dispose();
		tbtmHomologs.dispose();
		tbtmProteomes.dispose();
		tbtmSynteny.dispose();
		composite_localization.dispose();
		if (comboGenome.getItemCount() == 0) {
			for (String genomeTemp : Database.getInstance().getGenomeList()) {
				comboGenome.add(genomeTemp);
			}
			int index = -1;
			for (int i = 0; i < comboGenome.getItems().length; i++) {
				if (comboGenome.getItem(i).contains(genome.getSpecies())) {
					index = i;
				}
			}
			comboGenome.select(index);
		}
	}

	/**
	 * Remove some Widgets for Leishmania view
	 */
	private void initDefault() {
		System.out.println("Default initialisation for GeneView");
		tbtmHomologs.dispose();
		tbtmSynteny.dispose();
		composite_localization.dispose();
		arrayDataList = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesComparisonsArrayPath());
		arrayProteomeList = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
		ArrayList<String> dataTranscriptomes = BioCondition.getTranscriptomesGenomes();
		for (String genomeTemp : Database.getInstance().getGenomeList()) {
			if (dataTranscriptomes.contains(genomeTemp)) {
				genomeTemp = genomeTemp + " *";
			}
			comboGenome.add(genomeTemp);
		}
		int index = -1;
		for (int i = 0; i < comboGenome.getItems().length; i++) {
			if (comboGenome.getItem(i).contains(genome.getSpecies())) {
				index = i;
			}
		}
		comboGenome.select(index);

	}

	/**
	 * Remove some Widgets for Leishmania view
	 */
	private void initCrispromics() {
		tbtmExpressionData.dispose();
		tbtmHomologs.dispose();
		tbtmProteomes.dispose();
		tbtmSynteny.dispose();
		compSuppInfo.dispose();
		composite_localization.dispose();
		if (comboGenome.getItemCount() == 0) {
			for (String genomeTemp : Database.getInstance().getGenomeList()) {
				comboGenome.add(genomeTemp);
			}
			int index = -1;
			for (int i = 0; i < comboGenome.getItems().length; i++) {
				if (comboGenome.getItem(i).contains(genome.getSpecies())) {
					index = i;
				}
			}
			comboGenome.select(index);
		}
	}

	/**
	 * Initi Widgets for Listeriomics and UIBCListeriomics
	 */
	private void initListeriomics() {
		arrayDataList = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesComparisonsArrayPath());
		arrayProteomeList = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
		ArrayList<String> dataTranscriptomes = BioCondition.getTranscriptomesGenomes();
		for (String genomeTemp : Database.getInstance().getGenomeList()) {
			if (dataTranscriptomes.contains(genomeTemp)) {
				genomeTemp = genomeTemp + " *";
			}
			comboGenome.add(genomeTemp);
		}
		int index = -1;
		for (int i = 0; i < comboGenome.getItems().length; i++) {
			if (comboGenome.getItem(i).contains(genome.getSpecies())) {
				index = i;
			}
		}
		comboGenome.select(index);
		arrayGeneToLocalization = TabDelimitedTableReader.read(SubCellCompartment.LOCALIZATION_PATH);
	}

	/**
	 * Initi Widgets for Listeriomics and UIBCListeriomics
	 */
	private void initListeriomicsSample() {
		tbtmSynteny.dispose();
		composite_localization.dispose();
		arrayDataList = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesComparisonsArrayPath());
		arrayProteomeList = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
		ArrayList<String> dataTranscriptomes = BioCondition.getTranscriptomesGenomes();
		for (String genomeTemp : Database.getInstance().getGenomeList()) {
			if (dataTranscriptomes.contains(genomeTemp)) {
				genomeTemp = genomeTemp + " *";
			}
			comboGenome.add(genomeTemp);
		}
		int index = -1;
		for (int i = 0; i < comboGenome.getItems().length; i++) {
			if (comboGenome.getItem(i).contains(genome.getSpecies())) {
				index = i;
			}
		}
		comboGenome.select(index);
	}


	/**
	 * Init Widgets for Yersiniomics
	 */

    
    /**
     * Init Widgets for Yersiniomics
     */
    private void initYersiniomics() {
        //System.out.println("initYersiniomics");
    	ArrayList<String> omicsGenomes = new ArrayList<String>();
        arrayDataList = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesComparisonsArrayPath());
        TabDelimitedTableReader.readList(Database.getInstance().getTranscriptomesComparisonsArrayPath(), true, true);
        
        arrayProteomeList = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
        TabDelimitedTableReader.readList(Database.getInstance().getProteomesArrayPath(), true, true);
        arrayTranscriptomesList = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesArrayPath());
        arrayProteinAtlasList = TabDelimitedTableReader.read(Database.getInstance().getProteomesComparisonsArrayPath());
        TabDelimitedTableReader.readList(Database.getInstance().getProteomesComparisonsArrayPath(), true, true);
        /*
  	  if (Database.getInstance().getProjectName() != Database.URY_YERSINIOMICS_PROJECT) {
  		  List<Integer> rowNum = new ArrayList<>(); 
          int i=0;
          for (String[] row : arrayDataToDisplay) {
              String info = row[ArrayUtils.findColumn(arrayDataList, "Reference")];              
              if (!info.contains("Unpublished (URY)")) {
                	i++;
              } 
          }
          String[][] arrayDataToDisplayTemp = new String[i][arrayDataList[0].length];
          String[][] arrayProteomeToDisplayTemp = new String[0][0];
          String[][] arrayProteinAtlasToDisplayTemp = new String[0][0];

          i=0;
          for (String[] row : arrayDataToDisplay) {
        	  System.out.println("transcriptome");
              String info = row[ArrayUtils.findColumn(arrayDataList, "Reference")];              
              if (info.contains("Unpublished (URY)")) {
                	//
              } else {
            	  System.out.println("TRUE");
            	  ArrayUtils.addRow(arrayDataToDisplayTemp, row, i);
            	  i++;
              }
          }
          arrayDataList = arrayDataToDisplayTemp;
          /*
          for (String[] row : arrayProteomeToDisplay) {
        	  System.out.println("proteome");

              String info = row[ArrayUtils.findColumn(arrayProteomeList, "Reference")];              
              if (info.contains("Unpublished (URY)")) {
                	//
              } else {
            	  ArrayUtils.addRow(arrayProteomeToDisplayTemp,row,arrayProteomeToDisplayTemp.length+1);
              }
          }
          arrayProteomeList = arrayProteomeToDisplayTemp;

          for (String[] row : arrayProteinAtlasToDisplay) {
        	  System.out.println("proteome atlas");

              String info = row[ArrayUtils.findColumn(arrayProteinAtlasList, "Reference")];              
              if (info.contains("Unpublished (URY)")) {
                	//
              } else {
            	  ArrayUtils.addRow(arrayProteinAtlasToDisplayTemp,row,arrayProteinAtlasToDisplayTemp.length+1);
              }
          }
          arrayProteinAtlasList = arrayProteinAtlasToDisplayTemp;

      }
		 */
		ArrayList<String> dataTranscriptomes = BioCondition.getTranscriptomesGenomes();
		//System.out.println("dataTranscriptomes: "+dataTranscriptomes);
		//System.out.println("Database.getInstance().getGenomeList(: "+Database.getInstance().getGenomeList());

		for (String genomeTemp : Database.getInstance().getGenomeList()) {
			if (dataTranscriptomes.contains(genomeTemp)) {
				//System.out.println("genomeTemp: "+genomeTemp);

				genomeTemp = genomeTemp + " *";
			}
			comboGenome.add(genomeTemp);
			omicsGenomes.add(genomeTemp);

		}
	}

	
	/**
	 * The comboGenome contains modified genome name so we need this method to get selected element<br>
	 * a '*' is add to genome name when a transcriptome data is available
	 */
	public String getGenomeSelected() {
		//System.out.println("getGenomeSelected");

		if (comboGenome.isDisposed()) {
			return Genome.EGDE_NAME;
		} else {
			//System.out.println("getSelectionIndex: " + comboGenome.getSelectionIndex());

			String genome = comboGenome.getItem(comboGenome.getSelectionIndex());

			int extensionIndex = genome.lastIndexOf("*");

			if (extensionIndex == -1)
				return genome;
			else {

				genome = genome.substring(0, extensionIndex);
				genome = genome.trim();
				return genome;
			}
		}
	}

	/**
	 * The comboGenome contains modified genome name so we need this method to select an element<br>
	 * a '*' is add to genome name when a transcriptome or proteome data is available
	 * 
	 * @param genome
	 */
	public void setGenomeSelected(String genome) {
		//System.out.println("setGenomeSelected");
		if (!comboGenome.isDisposed()) {

			// select genome
			for (String genomeItem : comboGenome.getItems()) {
				if (genomeItem.equals(genome) || genomeItem.equals(genome.concat(" *"))) {
					// System.out.println("item: "+genomeItem);
					comboGenome.select(comboGenome.indexOf(genomeItem));
				}
			}
			// push state
			HashMap<String, String> parameters = new HashMap<>();
			parameters.put(NavigationManagement.GENOME, genome);
			NavigationManagement.pushStateView(ID, parameters);

		}
	}

	/**
	 * Update all lists of genes after a genome selection, or opening of the view
	 */
	private void updateListGenomeElements() {
		/*
		 * Update list of genes
		 */
		listGenes = new ArrayList<>();
		for (String gene : genome.getChromosomes().get(chromoID).getGenes().keySet()) {
			String text = "";
			String oldLocusTag = genome.getChromosomes().get(chromoID).getGenes().get(gene).getOldLocusTag().replace("%2C", ",");
			if (!oldLocusTag.equals("")) {
				text += oldLocusTag + " - ";
			}
			String geneName = genome.getChromosomes().get(chromoID).getGenes().get(gene).getGeneName();
			if (!geneName.equals("")) {
				text += "(" + geneName + ") - ";
			}
			text += gene;
			listGenes.add(text);
		}
		for (String gene : genome.getChromosomes().get(chromoID).getGenesAlternative().keySet()) {
			listGenes.add(gene);
		}
		for (String gene : genome.getChromosomes().get(chromoID).getNcRNAs().keySet()) {
			listGenes.add(gene);
		}
		// System.out.println("first element: "+listGenes.get(0));
		tableGenes.removeAll();
		tableGenes.setItemCount(listGenes.size());
		tableGenes.select(0);
		sequence = genome.getGeneFromName(tableGenes.getSelection()[0].getText(), chromoID);
		//sequence = genome.getGeneFromName("YPO_RS01055", chromoID);
		tableGenes.update();

	}

	/**
	 * Update the Combo for the chromosomes
	 */
	private void updateComboChromosome(String chromoID) {
		if (!comboChromosome.isDisposed()) {
			comboChromosome.removeAll();
			comboChromosome.redraw();
			for (String chromoIDTemp : genome.getChromosomes().keySet()) {
				Chromosome chromo = genome.getChromosomes().get(chromoIDTemp);
				comboChromosome.add(chromo.getChromosomeNumber() + " - " + chromoIDTemp);
			}

			/*
			 * Set selection
			 */
			for (int i = 0; i < comboChromosome.getItemCount(); i++) {
				if (comboChromosome.getItem(i).contains(chromoID)) {
					comboChromosome.select(i);
				}
			}
		}
	}

	/**
	 * Update all the information for the gene selected: <br>
	 * <li>Homolog update
	 * <li>Load localization
	 * <li>Update synteny view
	 * <li>Transcriptome update
	 * <li>Proteome update
	 */
	public void updateGeneInfo() {
		if (sequence != null) {
			updateGeneBasicInfo();
			if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT
					|| Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT
					|| Database.getInstance().getProjectName() == Database.YERSINIOMICS_PROJECT || Database.getInstance().getProjectName() == Database.URY_YERSINIOMICS_PROJECT
					) {
				updateAllGeneOmicsInfo();
			} else if (Database.getInstance().getProjectName() == Database.CLOSTRIDIOMICS_PROJECT) {
				updateGeneOmicsInfo();
				updateCrossRefs();
			} else if (Database.getInstance().getProjectName() == "ListeriomicsSample") {
				updateGeneOmicsInfo();
				updateHomolog();
			} else if (Database.getInstance().getProjectName() != Database.CRISPRGO_PROJECT) {
				updateGeneOmicsInfo();

			}

			/*
			 * push states
			 */
			pushState();
		}
	}

	public void updateGeneBasicInfo() {
		String title = "";
		if (!sequence.getFeature("old_locus_tag").equals(""))
			title += sequence.getFeature("old_locus_tag") + " - ";
		if (!sequence.getGeneName().equals("") && !sequence.getGeneName().equals("-"))
			title += sequence.getGeneName() +" - " ;
		title += sequence.getName();
		lblGene.setText(title);
		lblLocus.setText("<b>Locus:</b> " + sequence.getName() +  " - "+ sequence.getFeature("old_locus_tag") );
		lblBegin.setText("<b>Begin:</b> " + sequence.getBegin() + "");
		lblSizeBP.setText("<b>Size bp:</b> " + sequence.getLength());
		lblStrand.setText("<b>Strand:</b> " + sequence.getStrand());
		lblName.setText("<b>Gene:</b> " + sequence.getGeneName());

		lblEnd.setText("<b>End:</b> " + sequence.getEnd() + "");
		lblSizeaa.setText("<b>Size aa:</b> " + sequence.getLengthAA());
		lblProduct.setText("<b>Product:</b> " + sequence.getProduct());
		textFeature.setText("Note: " + sequence.getComment() + "\nFeatures: " + sequence.getFeaturesText());

		canvasGenome.getTrack().moveHorizontally(sequence.getBegin());
		canvasGenome.redraw();

	}

	/**
	 * Update all the information for the gene selected: <br>
	 * <li>Transcriptome update
	 * <li>Proteome update
	 */
	public void updateGeneOmicsInfo() {
		ArrayList<String> genomeTranscriptomes = BioCondition.getTranscriptomesGenomes();
		ArrayList<String> genomeProteomes = BioCondition.getProteomeGenomes();
		/*
		 * Expression atlas update
		 */
		if (genomeTranscriptomes.contains(genome.getSpecies())) {
			GeneViewTranscriptomeTools.updateExpressionAtlas(sequence, txtCutoffLogFC,txtCutoffPvalue, this, arrayDataList);
		} else {
			lblOver.setText("No data");
			lblUnder.setText("No data");
			lblNodiff.setText("No data");
			tableOver.removeAll();
			tableUnder.removeAll();
			tableNodiff.removeAll();
		}



		/*
		 * Proteome update
		 */
		if (genomeProteomes.contains(genome.getSpecies())) {
			GeneViewProteomeTools.updateProteomesTable(sequence, this, arrayProteomeList);
		} else {
			tableProteomes.removeAll();
		}

	}

	/**
	 * Update all the information for the gene selected: <br>
	 * <li>Homolog update
	 * <li>Load localization
	 * <li>Update synteny view
	 * <li>Transcriptome update
	 * <li>Proteome update
	 */
	public void updateAllGeneOmicsInfo() {
		/*
		if (!sequence.getOperon().equals("")) {
			Operon operon = genome.getChromosomes().get(chromoID).getOperons().get(sequence.getOperon());
			lblOperon.setText("In " + sequence.getOperon() + " containing " + operon.getGenes().size() + " genes");
		} else {
			lblOperon.setText("Not in an operon");
		}
		*/
		lblConservation.setText("Homologs in <b>" + (sequence.getConservation()) + "/"
				+ Genome.getAvailableGenomes().size() + "</b> "+Database.getInstance().getSpecies()+" genomes");
		lblConservation2.setText("Homologs in " + (sequence.getConservation()) + "/"
				+ Genome.getAvailableGenomes().size() + " "+Database.getInstance().getSpecies()+" genomes");
		lblProduct.setText("<b>Product: </b>" + sequence.getProduct());
		lblProtID.setText("<b>GenBank Protein: </b>" + RWTUtils.setProteinNCBILink(sequence.getProtein_id()));
		//lblCog.setText("COG: " + sequence.getCog());
		
		/*
		 * Bioconditions update
		 */
		ArrayList<String> genomeTranscriptomes = BioCondition.getTranscriptomesGenomes();
		ArrayList<String> genomeProteomes = BioCondition.getProteomeGenomes();
		ArrayList<String> genomeRNASeq = BioCondition.getRNASeqGenomes();
		
		/*
		 * KEGG and Uniprot update
		 */
		
		updateCrossRefs();

		/*
		 * Homolog update
		 */
		updateHomolog();
		
		/*
		 * Update synteny view
		 */
		try {			
			browserSynteny.evaluate("goToGene('" + sequence.getName() + "')");
		} catch (Exception e) {
			System.out.println("Cannot evaluate: " + "goToGene('" + sequence.getName() + "')");
		}
		
		/*
		 * Transcriptome update
		 */
		if (genomeTranscriptomes.contains(genome.getSpecies())) {
			GeneViewTranscriptomeTools.updateExpressionAtlas(sequence, txtCutoffLogFC, txtCutoffPvalue, this, arrayDataList);
		} else {
			lblOver.setText("No data");
			lblUnder.setText("No data");
			lblNodiff.setText("No data");
			tableOver.removeAll();
			tableUnder.removeAll();
			tableNodiff.removeAll();
			tbtmExpressionData.dispose();
		}

		/*
		 * Absence/presence Transcriptome update
		 */
		if (genomeRNASeq.contains(genome.getSpecies())) {
			//System.out.println("yes: " +genome.getSpecies());
			GeneViewTranscriptomeTools.updateTranscriptomesTable(sequence, this, arrayTranscriptomesList);
			//System.out.println("updateProteomesTable done");
		} else {
			tableTranscriptomes.removeAll();
			lblExprTranscriptomes.setText("No data");
			tbtmTranscriptomes.dispose();
		}

		/*
		 * Protein atlas update
		 */
		if (genomeProteomes.contains(genome.getSpecies())) {
			// System.out.println(genome.getSpecies());
			GeneViewProteomeTools.updateProteinAtlas(sequence, txtCutoffLogFCProteome, txtCutoffPvalueProteome, this, arrayProteinAtlasList);
		} else {
			lblOverProteome.setText("No data");
			lblUnderProteome.setText("No data");
			lblNodiffProteome.setText("No data");
			tableOverProteome.removeAll();
			tableUnderProteome.removeAll();
			tableNodiffProteome.removeAll();
			tbtmProteomeData.dispose();
		}

		/*
		 * Absence/presence Proteome update
		 */
		if (genomeProteomes.contains(genome.getSpecies())) {
			//System.out.println("yes: " +genome.getSpecies());
			GeneViewProteomeTools.updateProteomesTable(sequence, this, arrayProteomeList);
			//System.out.println("updateProteomesTable done");

		} else {
			tableProteomes.removeAll();
			lblExprProteomes.setText("No data");
			tbtmProteomes.dispose();
		}
		
		/*
		 * Coexpression network update
		 */
		/*
		if(genome.getSpecies().equals("Yersinia pestis CO92")||genome.getSpecies().equals("Yersinia pestis KIM5")||genome.getSpecies().equals("Yersinia pestis 91001")
				||genome.getSpecies().equals("Yersinia pseudotuberculosis YPIII")||genome.getSpecies().equals("Yersinia pseudotuberculosis IP32953")||genome.getSpecies().equals("Yersinia entomophaga MH96")
				||genome.getSpecies().equals("Yersinia enterocolitica Y1")||genome.getSpecies().equals("Yersinia enterocolitica Y11")) {
			updateCoExp();
		} else {
			tbtmCoExp.dispose();
		}
		*/

		/*
		 * Load localization
		 */
		/*	
		GeneViewLocalizationTools.loadLocalizationFigure(browserLocalization, arrayGeneToLocalization, sequence,
				bioCondsArray, null, genome);
			*/
	}

	public void updateHomolog() {
		bioCondsArray = GeneViewHomologTools.loadArrayHomologs(sequence, bioCondsArray, bioConds, bioCondsToDisplay);
		GeneViewHomologTools.loadFigureHomologs(sequence, browserHomolog, selectedGenomes);
		GeneViewHomologTools.updateHomologTable(tableHomologViewer, bioCondsArray, bioCondsToDisplay, bioConds,
				comparatorBioCondition, columnNames, selectedGenomes, txtSearchGenome);

	}

	public void updateTranscrAndProtViewers() {
		tbtmExpressionData.dispose();
		tbtmTranscriptomes.dispose();
		tbtmProteomeData.dispose();
		tbtmProteomes.dispose();

		/*
		 * Differential transcriptomics section
		 */
		{
			tbtmExpressionData = new TabItem(tabFolder, SWT.NONE);
			tbtmExpressionData.setText("Transcript differential expressions");

			compositeTranscriptome = new Composite(tabFolder, SWT.BORDER);
			tbtmExpressionData.setControl(compositeTranscriptome);
			compositeTranscriptome.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			compositeTranscriptome.setLayout(new GridLayout(2, false));

			Composite composite_8 = new Composite(compositeTranscriptome, SWT.BORDER);
			composite_8.setLayout(new GridLayout(1, false));

			{
				Composite composite_logfc = new Composite(composite_8, SWT.NONE);
				composite_logfc.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				composite_logfc.setLayout(new GridLayout(2, false));

				Label lbllogfoldchange = new Label(composite_logfc, SWT.NONE);
				lbllogfoldchange.setText("|Log2(Fold-Change)| >");

				txtCutoffLogFC = new Text(composite_logfc, SWT.BORDER);
				txtCutoffLogFC.setText(GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF+"");
			}
			{
				Composite  composite_pvalue = new Composite(composite_8, SWT.NONE);
				composite_pvalue.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				composite_pvalue.setLayout(new GridLayout(4, false));

				Label lblAnd = new Label(composite_pvalue, SWT.NONE);
				lblAnd.setText("and");

				Label lblPvalueFdrby = new Label(composite_pvalue, SWT.NONE);
				lblPvalueFdrby.setText("p-value <");
				

				txtCutoffPvalue = new Text(composite_pvalue, SWT.BORDER);
				txtCutoffPvalue.setText(GenomeElementAtlas.DEFAULT_PVAL_CUTOFF+"");

				Label lblZeroPvalue = new Label(composite_pvalue, SWT.NONE);
				lblZeroPvalue.setText("0.00 means that the Log2(FC) is not known.\n0.0e+00 means the p-value or the adjusted p-value is not known.");
				lblZeroPvalue.setForeground(BasicColor.GREY);
				
				
				/*if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
					composite_pvalue.dispose();
				}*/
			}
			btnUpdateCutoff = new Button(composite_8, SWT.TOGGLE);
			btnUpdateCutoff.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
			btnUpdateCutoff.setText("Choose cut-off and update transcript differential expressions");
			btnUpdateCutoff.setBackground(BasicColor.BUTTON);

			btnUpdateCutoff.addSelectionListener(this);

			Composite composite_3 = new Composite(compositeTranscriptome, SWT.NONE);
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

			btnGenomeViewer = new Button(composite_3, SWT.TOGGLE);
			btnGenomeViewer.setBackground(BasicColor.BUTTON);
			btnGenomeViewer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnGenomeViewer.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genomeViewer.bmp"));
			btnGenomeViewer.addSelectionListener(this);

			btnHeatmapview = new Button(composite_3, SWT.TOGGLE);
			btnHeatmapview.setBackground(BasicColor.BUTTON);
			btnHeatmapview.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnHeatmapview.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/compareexpression.bmp"));
			btnHeatmapview.addSelectionListener(this);

			Label lblOverExpressedIn = new Label(compositeTranscriptome, SWT.NONE);
			lblOverExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblOverExpressedIn.setText("Over expressed in");

			lblOver = new Label(compositeTranscriptome, SWT.NONE);
			lblOver.setText("over");

			tableOver = new Table(compositeTranscriptome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listOver = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listOver.heightHint = 150;
			RWTUtils.setMarkup(tableOver);
			tableOver.setLayoutData(gd_listOver);
			tableOver.addSelectionListener(this);


			Label lblUnderExpressedIn = new Label(compositeTranscriptome, SWT.NONE);
			lblUnderExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblUnderExpressedIn.setText("Under expressed in");

			lblUnder = new Label(compositeTranscriptome, SWT.NONE);
			lblUnder.setText("under");

			tableUnder = new Table(compositeTranscriptome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listUnder = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listUnder.heightHint = 150;
			tableUnder.setLayoutData(gd_listUnder);
			RWTUtils.setMarkup(tableUnder);
			tableUnder.addSelectionListener(this);

			Label lblNoDiffExpression = new Label(compositeTranscriptome, SWT.NONE);
			lblNoDiffExpression.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblNoDiffExpression.setText("No diff. expression in");

			lblNodiff = new Label(compositeTranscriptome, SWT.NONE);
			lblNodiff.setText("nodiff");

			tableNodiff = new Table(compositeTranscriptome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listNodiff = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listNodiff.heightHint = 150;
			tableNodiff.setLayoutData(gd_listNodiff);
			tableNodiff.addSelectionListener(this);
			RWTUtils.setMarkup(tableNodiff);

		}

		/*
		 * Transpcriptome Expression (RNASeq) tab
		 */

		tbtmTranscriptomes = new TabItem(tabFolder, SWT.NONE);
		tbtmTranscriptomes.setText("Transcriptomes");


		composite_101 = new Composite(tabFolder, SWT.NONE);
		tbtmTranscriptomes.setControl(composite_101);
		composite_101.setLayout(new GridLayout(1, false));
		composite_101.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		composite_102 = new Composite(composite_101, SWT.NONE);
		composite_102.setLayout(new GridLayout(1, false));

		Label transcriptomesExpl = new Label(composite_102, SWT.NONE);
		transcriptomesExpl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 0, 0));
		transcriptomesExpl.setText("For RNASeq experiments, feature counts were normalized using the Transcripts Per Million (TPM) method.\nThis value is displayed here and is only indicative for the presence or absence of transcript. It should not be used for direct analysis.");
		transcriptomesExpl.setForeground(BasicColor.GREY);
		
		composite_103 = new Composite(composite_101, SWT.NONE);
		composite_103.setLayout(new GridLayout(2, false));

		Label lblOverExpressedInTranscriptomes = new Label(composite_103, SWT.NONE);
		lblOverExpressedInTranscriptomes.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 0, 0));
		lblOverExpressedInTranscriptomes.setText("Found in");

		lblExprTranscriptomes = new Label(composite_103, SWT.NONE);
		lblExprTranscriptomes.setText("");

		tableTranscriptomes = new Table(composite_101, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableTranscriptomes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));


		/*
		 * Protein atlas section
		 */

		{
			tbtmProteomeData = new TabItem(tabFolder, SWT.NONE);
			tbtmProteomeData.setText("Protein differential abundances");

			compositeProteome = new Composite(tabFolder, SWT.BORDER);
			tbtmProteomeData.setControl(compositeProteome);
			compositeProteome.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			compositeProteome.setLayout(new GridLayout(2, false));

			Composite composite_8 = new Composite(compositeProteome, SWT.BORDER);
			composite_8.setLayout(new GridLayout(1, false));

			{
				Composite composite_logfc = new Composite(composite_8, SWT.NONE);
				composite_logfc.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				composite_logfc.setLayout(new GridLayout(2, false));

				Label lbllogfoldchange = new Label(composite_logfc, SWT.NONE);
				lbllogfoldchange.setText("|Log2(Fold-Change)| >");

				txtCutoffLogFCProteome = new Text(composite_logfc, SWT.BORDER);
				txtCutoffLogFCProteome.setText(GenomeElementAtlas.DEFAULT_LOGFC_PROTEOMIC_CUTOFF+"");
			}
			{
				Composite  composite_pvalue = new Composite(composite_8, SWT.NONE);
				composite_pvalue.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				composite_pvalue.setLayout(new GridLayout(4, false));

				Label lblAnd = new Label(composite_pvalue, SWT.NONE);
				lblAnd.setText("and");

				Label lblPvalueFdrby = new Label(composite_pvalue, SWT.NONE);
				lblPvalueFdrby.setText("p-value <");

				txtCutoffPvalueProteome = new Text(composite_pvalue, SWT.BORDER);
				txtCutoffPvalueProteome.setText(GenomeElementAtlas.DEFAULT_PVAL_PROTEOMIC_CUTOFF+"");
				
				Label lblZeroPvalue = new Label(composite_pvalue, SWT.NONE);
				lblZeroPvalue.setText("0.00 means that the Log2(FC) is not known.\n0.0e+00 means the p-value or the adjusted p-value is not known.");
				lblZeroPvalue.setForeground(BasicColor.GREY);
				/*if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
					composite_pvalue.dispose();
				}*/
			}
			btnUpdateCutoffProteome = new Button(composite_8, SWT.TOGGLE);
			btnUpdateCutoffProteome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
			btnUpdateCutoffProteome.setText("Choose cut-off and update protein differential abundances");
			btnUpdateCutoffProteome.setBackground(BasicColor.BUTTON);

			btnUpdateCutoffProteome.addSelectionListener(this);

			Composite composite_3 = new Composite(compositeProteome, SWT.NONE);
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

			btnGenomeViewerProteome = new Button(composite_3, SWT.TOGGLE);
			btnGenomeViewerProteome.setBackground(BasicColor.BUTTON);
			btnGenomeViewerProteome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnGenomeViewerProteome.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genomeViewer.bmp"));
			btnGenomeViewerProteome.addSelectionListener(this);

			btnHeatmapviewProteome = new Button(composite_3, SWT.TOGGLE);
			btnHeatmapviewProteome.setBackground(BasicColor.BUTTON);
			btnHeatmapviewProteome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnHeatmapviewProteome.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/compareexpression.bmp"));
			btnHeatmapviewProteome.addSelectionListener(this);

			Label lblOverExpressedIn = new Label(compositeProteome, SWT.NONE);
			lblOverExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblOverExpressedIn.setText("Over expressed in");

			lblOverProteome = new Label(compositeProteome, SWT.NONE);
			lblOverProteome.setText("over");

			tableOverProteome = new Table(compositeProteome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listOver = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listOver.heightHint = 150;
			tableOverProteome.setLayoutData(gd_listOver);
			tableOverProteome.addSelectionListener(this);
			RWTUtils.setMarkup(tableOverProteome);


			Label lblUnderExpressedIn = new Label(compositeProteome, SWT.NONE);
			lblUnderExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblUnderExpressedIn.setText("Under expressed in");

			lblUnderProteome = new Label(compositeProteome, SWT.NONE);
			lblUnderProteome.setText("under");

			tableUnderProteome = new Table(compositeProteome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listUnder = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listUnder.heightHint = 150;
			tableUnderProteome.setLayoutData(gd_listUnder);
			tableUnderProteome.addSelectionListener(this);
			RWTUtils.setMarkup(tableUnderProteome);

			Label lblNoDiffExpression = new Label(compositeProteome, SWT.NONE);
			lblNoDiffExpression.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblNoDiffExpression.setText("No diff. expression in");

			lblNodiffProteome = new Label(compositeProteome, SWT.NONE);
			lblNodiffProteome.setText("nodiff");

			tableNodiffProteome = new Table(compositeProteome, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_listNodiff = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_listNodiff.heightHint = 150;
			tableNodiffProteome.setLayoutData(gd_listNodiff);
			tableNodiffProteome.addSelectionListener(this);
			RWTUtils.setMarkup(tableNodiffProteome);

		}





		tbtmProteomes = new TabItem(tabFolder, SWT.NONE);
		tbtmProteomes.setText("Proteomes");

		composite_09 = new Composite(tabFolder, SWT.NONE);
		tbtmProteomes.setControl(composite_09);
		composite_09.setLayout(new GridLayout(1, false));

		composite_091 = new Composite(composite_09, SWT.NONE);
		composite_091.setLayout(new GridLayout(1, false));

		Label proteomesExpl1 = new Label(composite_091, SWT.NONE);
		proteomesExpl1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		proteomesExpl1.setText("For Orbitrap experiments, displayed value is log10(raw LFQ)."
				+ "\nFor FTICR experiments, displayed value is raw FTICR intensity."
				+ "\nFor 2D gel experiments, if proteins are detected in several spots, displayed value is the most intense measured spot intensity value."
				+ "\n'-1' means that the protein is detected but no value is available.");

		proteomesExpl1.setForeground(BasicColor.GREY);

		composite_092 = new Composite(composite_09, SWT.NONE);
		composite_092.setLayout(new GridLayout(2, false));

		Label lblOverExpressedInProteomes = new Label(composite_092, SWT.NONE);
		lblOverExpressedInProteomes.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOverExpressedInProteomes.setText("Found in");

		lblExprProteomes = new Label(composite_092, SWT.NONE);
		lblExprProteomes.setText("");

		tableProteomes = new Table(composite_09, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableProteomes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

	}


	private void setColumnNames() {
		coExpCol = new ArrayList<>();
		coExpCol.add("Correlation coefficient");
		coExpCol.add("Gene");
		coExpCol.add("About");
	}
	
	public void updateCoExpViewer() {
		tbtmCoExp.dispose();
		tbtmCoExp = new TabItem(tabFolder, SWT.NONE);
		tbtmCoExp.setText("Co-Expression");

		compositeCoExpression = new Composite(tabFolder, SWT.BORDER);
		tbtmCoExp.setControl(compositeCoExpression);
		compositeCoExpression.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeCoExpression.setLayout(new GridLayout(2, false));

		Composite composite_9 = new Composite(compositeCoExpression, SWT.BORDER);
		composite_9.setLayout(new GridLayout(1, false));

		Composite composite_CoExp = new Composite(composite_9, SWT.NONE);
		//composite_CoExp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_CoExp.setLayout(new GridLayout(5, false));

		Label lblCoExp= new Label(composite_CoExp, SWT.NONE);
		lblCoExp.setText("Display links for two genomic elements having a correlation higher than");
		btnCorrMinus = new Button(composite_CoExp, SWT.NONE);
		btnCorrMinus.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomOUT.bmp"));
		btnCorrMinus.addSelectionListener(this);
		txtCutoffCoExp = new Text(composite_CoExp, SWT.BORDER);
		txtCutoffCoExp.setText(GenomeElementAtlas.DEFAULT_COEXP_CUTOFF+"");

		
        btnCorrPlus = new Button(composite_CoExp, SWT.NONE);
        btnCorrPlus.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomIN.bmp"));
		btnCorrPlus.addSelectionListener(this);

		btnUpdateCutoffCoExp = new Button(composite_9, SWT.NONE);
		btnUpdateCutoffCoExp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
		btnUpdateCutoffCoExp.setText("Choose cut-off and update Co-Expression view");
		btnUpdateCutoffCoExp.addSelectionListener(this);
		
        btnExportNetwork = new Button(composite_9, SWT.NONE);
        btnExportNetwork.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
        btnExportNetwork.setText("Export filtered network");
        btnExportNetwork.addSelectionListener(this);
        
		Composite composite_3 = new Composite(compositeCoExpression, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_3.setLayout(new GridLayout(4, false));

		Label lblCoExpPos = new Label(compositeCoExpression, SWT.NONE);
		lblCoExpPos.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCoExpPos.setText("Positively coexpressed with");

		tablePosCoExpViewer = new TableViewer(compositeCoExpression, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd_listPos = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		gd_listPos.heightHint = 205;
		tablePosCoExpViewer.getTable().setLayoutData(gd_listPos);
		tablePosCoExpViewer.getTable().addSelectionListener(this);
		tablePosCoExpViewer.getTable().setHeaderVisible(true);
		tablePosCoExpViewer.getTable().setLinesVisible(true);

		
		Label lblCoExpNeg = new Label(compositeCoExpression, SWT.NONE);
		lblCoExpNeg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCoExpNeg.setText("Negatively coexpressed with");
		
		tableNegCoExpViewer = new TableViewer(compositeCoExpression, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd_listNeg = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		gd_listNeg.heightHint = 205;
		tableNegCoExpViewer.getTable().setLayoutData(gd_listNeg);
		tableNegCoExpViewer.getTable().addSelectionListener(this);
		tableNegCoExpViewer.getTable().setHeaderVisible(true);
		tableNegCoExpViewer.getTable().setLinesVisible(true);
		
		tablePosCoExpViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				String selectedGene =
						tablePosCoExpViewer.getTable().getItem(tablePosCoExpViewer.getTable().getSelectionIndex())
						.getText(coExpCol.indexOf("Gene"));
				//System.out.println("selectedGene: "+selectedGene);
				//System.out.println(tableHomologViewer.getTable().getSelectionIndex()+ " " + columnNames.indexOf("Name (GenBank)")+ "yahou "+selectedGene+" "+selectedGenome);
				Gene gene = genome.getGeneFromName(selectedGene);
				//System.out.println("gene: "+gene);

				GeneView.displayGene(gene, partService);
			}
		});
		
		tableNegCoExpViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				String selectedGene =
						tableNegCoExpViewer.getTable().getItem(tableNegCoExpViewer.getTable().getSelectionIndex())
						.getText(coExpCol.indexOf("Gene"));
				//System.out.println(tableHomologViewer.getTable().getSelectionIndex()+ " " + columnNames.indexOf("Name (GenBank)")+ "yahou "+selectedGene+" "+selectedGenome);
				Gene gene = genome.getGeneFromName(selectedGene);
				GeneView.displayGene(gene, partService);
			}
		});
	}
		
	
	public void updateCoExp() {
		
		ArrayList<String> genomeElementArrayList = new ArrayList<>();
		genomeElementArrayList.add(sequence.getName());
		filteredNetwork = generalNetwork.filterNetwork(genomeElementArrayList,Double.parseDouble(txtCutoffCoExp.getText()));
		//ArrayList<String> networkList = filteredNetwork.toArrayList();

		setColumnNames();

		//System.out.println("update");

		for (String vertice : filteredNetwork.getEdges().keySet()) {
			//System.out.println("vertices "+ vertice);
			HashMap<String, Double> edgesTemp = filteredNetwork.getEdges().get(vertice);
			int l =edgesTemp.size();
			posCoExpArrayTemp = new String[l][3];
			negCoExpArrayTemp = new String[l][3];

			//System.out.println("edgesTemp "+ edgesTemp);
			int ipos=0;
			int ineg=0;
			for (String targetVertice : edgesTemp.keySet()) {
				Double corr = edgesTemp.get(targetVertice);
				if(corr>0) {
					posCoExpArrayTemp[ipos][0]=corr.toString();
					posCoExpArrayTemp[ipos][1]=targetVertice;
					posCoExpArrayTemp[ipos][2]=filteredNetwork.getVertices().get(targetVertice);
					ipos=ipos+1;
				} else {
					negCoExpArrayTemp[ineg][0]=corr.toString();
					negCoExpArrayTemp[ineg][1]=targetVertice;
					negCoExpArrayTemp[ineg][2]=filteredNetwork.getVertices().get(targetVertice);
					ineg=ineg+1;
				}
			}
			posCoExpArray = new String[ipos][3];
			negCoExpArray = new String[ineg][3];
			
			for (int z=0; z<ipos; z++) {
				posCoExpArray[z][0] = posCoExpArrayTemp[z][0];
				posCoExpArray[z][1] = posCoExpArrayTemp[z][1];
				posCoExpArray[z][2] = posCoExpArrayTemp[z][2];
				}
			for (int y=0; y<ineg; y++) {
				negCoExpArray[y][0] = negCoExpArrayTemp[y][0];
				negCoExpArray[y][1] = negCoExpArrayTemp[y][1];
				negCoExpArray[y][2] = negCoExpArrayTemp[y][2];
				}
			/*
			 * positive regulations columns
			 */
			for (TableColumn col : tablePosCoExpViewer.getTable().getColumns()) {
				col.dispose();
			}

			tablePosCoExpViewer.setContentProvider(new ArrayContentProvider());
			
			TableViewerColumn viewerPosColumn = new TableViewerColumn(tablePosCoExpViewer, SWT.NONE);
			TableColumn posColumn = viewerPosColumn.getColumn();
			posColumn.setText("Correlation coefficient");
			posColumn.setResizable(true);
			posColumn.setMoveable(true);
			posColumn.addSelectionListener(getPosSelectionAdapter(posColumn, 0));


			viewerPosColumn.setLabelProvider(new ColumnLabelProvider() {
				/**
				 * 
				 */
				
				private static final long serialVersionUID = -55186278705378146L;

				@Override
				public String getText(Object element) {
					String[] bioCond = (String[]) element;
					String text = bioCond[0];
					return text;
				}
			});

			//System.out.println("column 1 created");

			TableViewerColumn viewerPosColumn2 = new TableViewerColumn(tablePosCoExpViewer, SWT.NONE);
			TableColumn posColumn2 = viewerPosColumn2.getColumn();
			posColumn2.setText("Gene");
			posColumn2.setResizable(true);
			posColumn2.setMoveable(true);
			posColumn2.addSelectionListener(getPosSelectionAdapter(posColumn2, 1));
			viewerPosColumn2.setLabelProvider(new ColumnLabelProvider() {
				/**
				 * 
				 */
				private static final long serialVersionUID = -55186278705378176L;

				@Override
				public String getText(Object element) {
					String[] bioCond = (String[]) element;
					String text = bioCond[1];
					return text;
				}
			});
			
			TableViewerColumn viewerPosColumn3 = new TableViewerColumn(tablePosCoExpViewer, SWT.NONE);
			TableColumn posColumn3 = viewerPosColumn3.getColumn();
			posColumn3.setText("About");
			posColumn3.setResizable(true);
			posColumn3.setMoveable(true);
			posColumn3.addSelectionListener(getPosSelectionAdapter(posColumn3, 1));
			viewerPosColumn3.setLabelProvider(new ColumnLabelProvider() {
				/**
				 * 
				 */
				private static final long serialVersionUID = -55186278705378176L;

				@Override
				public String getText(Object element) {
					String[] bioCond = (String[]) element;
					String text = bioCond[2];
					return text;
				}
			});

			tablePosCoExpViewer.getTable().setHeaderVisible(true);
			tablePosCoExpViewer.getTable().setLinesVisible(true);
			tablePosCoExpViewer.setInput(posCoExpArray);
			comparatorPosCoExp = new BioConditionComparator(coExpCol);
			tablePosCoExpViewer.setComparator(comparatorPosCoExp);
			
			/*
			 * negative regulations columns
			 */
			
			for (TableColumn col : tableNegCoExpViewer.getTable().getColumns()) {
				col.dispose();
			}

			tableNegCoExpViewer.setContentProvider(new ArrayContentProvider());
			TableViewerColumn viewerNegColumn = new TableViewerColumn(tableNegCoExpViewer, SWT.NONE);
			TableColumn negColumn = viewerNegColumn.getColumn();
			negColumn.setText("Correlation coefficient");
			negColumn.setResizable(true);
			negColumn.setMoveable(true);
			negColumn.addSelectionListener(getNegSelectionAdapter(negColumn, 0));


			viewerNegColumn.setLabelProvider(new ColumnLabelProvider() {
				/**
				 * 
				 */
				
				private static final long serialVersionUID = -55186278705378146L;

				@Override
				public String getText(Object element) {
					//System.out.println("in getText ");

					String[] bioCond = (String[]) element;
					String text = bioCond[0];
					//System.out.println("text: "+text);

					return text;
				}
			});

			//System.out.println("column 1 created");

			TableViewerColumn viewerNegColumn2 = new TableViewerColumn(tableNegCoExpViewer, SWT.NONE);
			TableColumn negColumn2 = viewerNegColumn2.getColumn();
			negColumn2.setText("Gene");
			negColumn2.setResizable(true);
			negColumn2.setMoveable(true);
			negColumn2.addSelectionListener(getNegSelectionAdapter(negColumn2, 1));
			viewerNegColumn2.setLabelProvider(new ColumnLabelProvider() {
				/**
				 * 
				 */
			
				private static final long serialVersionUID = -55186278705378176L;

				@Override
				public String getText(Object element) {
					String[] bioCond = (String[]) element;
					String text = bioCond[1];
					return text;
				}
			});
			
			TableViewerColumn viewerNegColumn3 = new TableViewerColumn(tableNegCoExpViewer, SWT.NONE);
			TableColumn negColumn3 = viewerNegColumn3.getColumn();
			negColumn3.setText("About");
			negColumn3.setResizable(true);
			negColumn3.setMoveable(true);
			negColumn3.addSelectionListener(getNegSelectionAdapter(negColumn3, 1));
			viewerNegColumn3.setLabelProvider(new ColumnLabelProvider() {
				/**
				 * 
				 */
			
				private static final long serialVersionUID = -55186278705378176L;

				@Override
				public String getText(Object element) {
					String[] bioCond = (String[]) element;
					String text = bioCond[2];
					return text;
				}
			});


			tableNegCoExpViewer.getTable().setHeaderVisible(true);
			tableNegCoExpViewer.getTable().setLinesVisible(true);

			tableNegCoExpViewer.setInput(negCoExpArray);
			
			comparatorNegCoExp = new BioConditionComparator(coExpCol);
			tableNegCoExpViewer.setComparator(comparatorNegCoExp);
			
			for (int j = 0; j < 3; j++) {
				tablePosCoExpViewer.getTable().getColumn(j).pack();
				tableNegCoExpViewer.getTable().getColumn(j).pack();
			}

		}
		
	}

	private SelectionAdapter getPosSelectionAdapter(final TableColumn column, final int index) {


		SelectionAdapter selectionAdapter = new SelectionAdapter() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 7102838693143247943L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				comparatorPosCoExp.setColumn(index);
				int dir = comparatorPosCoExp.getDirection();
				tablePosCoExpViewer.getTable().setSortDirection(dir);
				tablePosCoExpViewer.getTable().setSortColumn(column);
				tablePosCoExpViewer.refresh();
			}
		};
		return selectionAdapter;
	}
	private SelectionAdapter getNegSelectionAdapter(final TableColumn column, final int index) {


		SelectionAdapter selectionAdapter = new SelectionAdapter() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 7102838693143247943L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				comparatorNegCoExp.setColumn(index);
				int dir = comparatorNegCoExp.getDirection();
				tableNegCoExpViewer.getTable().setSortDirection(dir);
				tableNegCoExpViewer.getTable().setSortColumn(column);
				tableNegCoExpViewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * Update genome viewer
	 */
	public void updateGenomeViewer() {
		trackGenome = new Track(genome, chromoID);
		canvasGenome.setTrack(trackGenome);
		canvasGenome.redraw();
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
			parameters.put(NavigationManagement.GENOME, genome.getSpecies());
			parameters.put(NavigationManagement.CHROMO, chromoID);
			parameters.put(NavigationManagement.LIST, sequence.getName());
			Item item = tabFolder.getItem(tabFolder.getSelectionIndex());
			parameters.put(NavigationManagement.ITEM, item.getText());
			NavigationManagement.pushStateView(this.getViewID(), parameters);
		}
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
			String comparison = tableOver.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name")+3);
			comparisons.add(comparison);
		}
		for (int index : tableUnder.getSelectionIndices()) {
			String comparison = tableUnder.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name")+3);
			comparisons.add(comparison);
		}
		for (int index : tableNodiff.getSelectionIndices()) {
			String comparison = tableNodiff.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name")+3);
			comparisons.add(comparison);
		}
		//System.out.println("comparisons: "+ comparisons);
		return comparisons;
	}

	public ArrayList<String> getSelectedComparisonsProteome() {
		ArrayList<String> comparisons = new ArrayList<>();
		for (int index : tableOverProteome.getSelectionIndices()) {
			String comparison = tableOverProteome.getItem(index).getText(ArrayUtils.findColumn(arrayProteomeList, "Data Name")+3);
			comparisons.add(comparison);
		}
		for (int index : tableUnderProteome.getSelectionIndices()) {
			String comparison = tableUnderProteome.getItem(index).getText(ArrayUtils.findColumn(arrayProteomeList, "Data Name")+3);
			comparisons.add(comparison);
		}
		for (int index : tableNodiffProteome.getSelectionIndices()) {
			String comparison = tableNodiffProteome.getItem(index).getText(ArrayUtils.findColumn(arrayProteomeList, "Data Name")+3);
			comparisons.add(comparison);
		}
		//System.out.println("comparisons: "+ comparisons);
		return comparisons;
	}
	/**
	 * Display a given gene
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void displayGene(Gene gene, EPartService partService) {
		//System.out.println("displayGene");

		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		//System.out.println("gene.getGenomeName(): "+gene.getGenomeName());


		view.initGenomeInfo(gene.getGenomeName(),gene.getChromosomeID());
		//System.out.println("after initGenomeInfo");
		view.setGenomeSelected(gene.getGenomeName());
		//System.out.println("after setGenomeSelected");
		
		view.setSequence(gene);
		
		for (int i = 0; i < view.getListGenes().size(); i++) {        	
			if (view.getListGenes().get(i).equals(gene.getName())) {
				view.getTableGenes().select(i);
				view.getTableGenes().showItem(view.getTableGenes().getItem(i));
			}
		}
		view.updateGeneInfo();
	}

	/**
	 * Display GeneView <br>
	 * It will display Genome.getDefautGenome(); CO92
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void openGeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = Genome.getDefautGenome();
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	/**
	 * Display GeneView <br>
	 * It will display KIM
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void openKIMGeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia pestis KIM5";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	public static void openPestoidesGeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia pestis Pestoides F";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	
	public static void openEV76GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia pestis EV76-CN";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	
	/**
	 * Display GeneView <br>
	 * It will display IP32953
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void openIP32953GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia pseudotuberculosis IP32953";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	public static void openIP31758GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia pseudotuberculosis IP31758";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}


	/**
	 * Display GeneView <br>
	 * It will display YPIII
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void openYPIIIGeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia pseudotuberculosis YPIII";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}


	/**
	 * Display GeneView <br>
	 * It will display Y11
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void openY11GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia enterocolitica Y11";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	/**
	 * Display GeneView <br>
	 * It will display Y1
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void openY1GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia enterocolitica Y1";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	/**
	 * Display GeneView <br>
	 * It will display 8081
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void open8081GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia enterocolitica 8081";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	public static void openWAGeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia enterocolitica WA";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	public static void openIP38326GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia enterocolitica IP38326";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	public static void openIP38023GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia enterocolitica IP38023";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	public static void openIP37485GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia enterocolitica IP37485";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	public static void openIP37574GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia enterocolitica IP37574";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	/**
	 * Display GeneView <br>
	 * It will display QMA0440
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void openQMA0440GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia ruckeri QMA0440";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	
	/**
	 * Display GeneView <br>
	 * It will display SC09
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void openSC09GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia ruckeri SC09";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}

	/**
	 * Display GeneView <br>
	 * It will display 91001
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void open91001GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia pestis 91001";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	/**
	 * Display GeneView <br>
	 * It will display MH96
	 * 
	 * @param gene
	 * @param partService
	 */

	public static void openMH96GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Yersinia entomophaga MH96";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	

	public static void open630GeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Clostridioides difficile 630";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	

	public static void open630bisGeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Clostridioides difficile 630 bis";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	

	public static void open630DermGeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Clostridioides difficile 630Derm";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	public static void open630deltaErmGeneView(EPartService partService) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		NavigationManagement.pushStateView(id, new HashMap<>());
		GeneView view = (GeneView) part.getObject();
		view.setViewID(id);
		String genomeName = "Clostridioides difficile 630 delta erm";
		view.initGenomeInfo(genomeName);
		view.setGenomeSelected(genomeName);
	}
	/**
	 * Display the view with saved parameters
	 * 
	 * @param gene
	 * @param partService
	 */
	public static void displayGeneView(EPartService partService, HashMap<String, String> parameters) {
		String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
		// initiate view
		ResourceManager.openView(partService, GeneView.ID, id);
		// update data
		MPart part = partService.findPart(id);
		GeneView view = (GeneView) part.getObject();
		String genomeName = parameters.get(NavigationManagement.GENOME);
		String chromoID = parameters.get(NavigationManagement.CHROMO);
		String geneName = parameters.get(NavigationManagement.LIST).replaceFirst(";", "");
		view.setViewID(id);
		view.initGenomeInfo(genomeName, chromoID);
		view.setGenomeSelected(genomeName);
		Gene gene = view.getGenome().getChromosomes().get(chromoID).getGenes().get(geneName);
		view.setSequence(gene);
		for (int i = 0; i < view.getListGenes().size(); i++) {
			if (view.getListGenes().get(i).equals(gene.getName())) {
				view.getTableGenes().select(i);
				view.getTableGenes().showItem(view.getTableGenes().getItem(i));
			}
		}
		view.updateGeneInfo();

		String tabItemName = parameters.get(NavigationManagement.ITEM);
		if (!tabItemName.equals("")) {
			for (TabItem tabItem : view.getTabFolder().getItems()) {
				if (tabItem.getText().equals(tabItemName)) {
					view.getTabFolder().setSelection(view.getTabFolder().indexOf(tabItem));
				}
			}
		}
	}

	/**
	 * Search a text file in the annotation<br>
	 * <li>Search if it is a position and go
	 * <li>Search if it is contains in <code>getChromosome().getAllElements().keySet()</code>
	 * <li>Search if it is contains in <code>gene.getName()</code><br>
	 * All the search are case insensitive using <code>text.toUpperCase();</code>
	 * 
	 * @param text
	 * @return
	 */
	public ArrayList<String> search(String text) {
		Chromosome chromosome = genome.getChromosomes().get(chromoID);
		ArrayList<String> searchResult = new ArrayList<>();
		try {
			/*
			 * If a base pair position has been wrote, it will reach it directly
			 */
			int position = Integer.parseInt(text);
			Sequence sequenceTemp = chromosome.getAnnotation().getElementInfoATbp(chromosome, position);
			if (sequenceTemp instanceof Gene) {
				searchResult.add(sequenceTemp.getName());
			}
		} catch (Exception e) {
			/*
			 * go through chromosome and search for a Gene with this name
			 */
			
			text = text.toUpperCase();
			for (Gene gene : chromosome.getGenes().values()) {
				String geneName = gene.getName();
				String geneNameTemp = geneName.toUpperCase();
				if (geneNameTemp.contains(text)) {
					if (!searchResult.contains(gene.getName())) {
						searchResult.add(gene.getName());
					}
				}
				geneName = gene.getOldLocusTag();
				geneNameTemp = geneName.toUpperCase();
				if (geneNameTemp.contains(text)) {
					if (!searchResult.contains(gene.getName())) {
						searchResult.add(gene.getName());
					}
				}
				geneName = gene.getGeneName();
				geneNameTemp = geneName.toUpperCase();
				if (geneNameTemp.contains(text)) {
					if (!searchResult.contains(gene.getName())) {
						searchResult.add(gene.getName());
					}
				}
				String geneInfo = gene.getProduct() + "  -  " + gene.getProtein_id() + " - " + gene.getComment() + " - "
						+ gene.getFeaturesText();
				String geneInfoTemp = geneInfo.toUpperCase();
				if (geneInfoTemp.contains(text)) {
					if (!searchResult.contains(gene.getName())) {
						searchResult.add(gene.getName());
					}
				}
			}
		}

		// for(String geneName : searchResult){
		// System.out.println(geneName);
		// }
		return searchResult;
	}



    /**
     * Read and convert a .txt file to a HashMap
     * 
     * @param filePath
     * @return
     */
    
    public static HashMap<String, String> HashMapFromTextFile(String filePath) {
    	HashMap<String,String> map = new HashMap<String,String>();
    	BufferedReader br = null;
    	try {
    		File file = new File(filePath);
    		br = new BufferedReader(new FileReader(file));
    		String line = null;
    		while ((line=br.readLine()) != null) {
    			String[] parts =line.split("\t");
    			String key = parts[0].trim();


    			String value = parts[1].trim();
    			//System.out.println("key: "+key);
    			//System.out.println("value: "+value);
    			map.put(key, value);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		if(br != null) {
    			try {
    				br.close();
    			} catch (Exception e) {
    			}
    		}
    	}
    	return map;
    }
    
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == comboGenome) {
			genome = Genome.loadGenome(getGenomeSelected());
			updateTranscrAndProtViewers();
			/*
			if(genome.getSpecies().equals("Yersinia pestis CO92")||genome.getSpecies().equals("Yersinia pestis KIM5")||genome.getSpecies().equals("Yersinia pestis 91001")
					||genome.getSpecies().equals("Yersinia pseudotuberculosis YPIII")||genome.getSpecies().equals("Yersinia pseudotuberculosis IP32953")||genome.getSpecies().equals("Yersinia entomophaga MH96")
					||genome.getSpecies().equals("Yersinia enterocolitica Y1")||genome.getSpecies().equals("Yersinia enterocolitica Y11")) {
				generalNetwork = new Network();
				generalNetwork = Network.load(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());
				updateCoExpViewer();
			}
			
			 */
			chromoID = genome.getFirstChromosome().getAccession().toString();
			updateComboChromosome(chromoID);
			updateListGenomeElements();
			updateGenomeViewer();
			updateCrossRefsBrowsers();
			updateSyntenyBrowser();
			updateGeneInfo();
		} else if (e.getSource() == comboChromosome) {
			chromoID = comboChromosome.getItem(comboChromosome.getSelectionIndex()).split(" - ")[1];
			updateListGenomeElements();
			updateGenomeViewer();
			updateGeneInfo();
		} else if (e.getSource() == btnGetAnnotationInformation) {
			genome = Genome.loadGenome(getGenomeSelected());
			
			AnnotationView.openAnnotationView(partService, genome);
		} else if (e.getSource() == tabFolder) {
			pushState();
		} else if (e.getSource() == btnSaveAsPng) {
			String textSVG = GeneViewHomologTools.getPhyloFigure(sequence, selectedGenomes);
			try {
				File tempSVGFile = File.createTempFile("Highlightstrain", "Phylogeny.svg");
				FileUtils.saveText(textSVG, tempSVGFile.getAbsolutePath());
				File tempPNGFile = File.createTempFile("Highlightstrain", "Phylogeny.png");
				//System.out.println("Convert Phylogeny.svg to Phylogeny.png\nHave you set ImageMagick PATH in ImageMagick.getConvertPATH()\nYours is set to: "+ImageMagick.getConvertPATH());
				CMD.runProcess(ImageMagick.getConvertPATH() + " " + tempSVGFile.getAbsolutePath() + " " + tempPNGFile);
				SaveFileUtils.saveFile("Yersinia_Phylogenomic_Tree_" + sequence.getName() + ".png", tempPNGFile,
						"PNG image file", partService, shell);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getSource() == btnSaveAsSvg) {
			String textSVG = GeneViewHomologTools.getPhyloFigure(sequence, selectedGenomes);
			SaveFileUtils.saveTextFile(Database.getInstance().getSpecies()+"_Phylogenomic_Tree_" + sequence.getName() + ".svg", textSVG, true,
					"SVG (vector image) file", textSVG, partService, shell);
		} else if (e.getSource() == btnDownloadtxt) {
			System.out.println("click on btnDonwload: " +columnNames.size());
			String[][] arrayToSave = new String[1][columnNames.size()];
			for (int i = 0; i < columnNames.size(); i++) {
				arrayToSave[0][i] = columnNames.get(i);
			}
			int k = 1;
			for (int i = 1; i < bioCondsArray.length; i++) {
				String genomeName = bioCondsArray[i][columnNames.indexOf("Name (GenBank)")];
				String processedGenomeName = GenomeNCBI.processGenomeName(genomeName);
				if (selectedGenomes.contains(processedGenomeName)) {
					arrayToSave = ArrayUtils.addRow(arrayToSave, ArrayUtils.getRow(bioCondsArray, i), k);
					k++;
				}
			}
			String arrayRep = ArrayUtils.toString(arrayToSave);
			String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(arrayToSave);
			SaveFileUtils.saveTextFile(Database.getInstance().getSpecies()+"_Genomic_Table_" + sequence.getName() + ".txt", arrayRep, true, "",
					arrayRepHTML, partService, shell);
		} else if (e.getSource() == btnSelectall) {
			selectedGenomes.clear();
			tableHomolog.selectAll();
			for (int i : tableHomolog.getSelectionIndices()) {
				String selectedGenome = GenomeNCBI.processGenomeName(tableHomolog.getItem(i).getText(columnNames.indexOf("Name (GenBank)") + 1));
				if (!selectedGenomes.contains(selectedGenome)) {
					selectedGenomes.add(selectedGenome);
				}
			}
			GeneViewHomologTools.loadFigureHomologs(sequence, browserHomolog, selectedGenomes);
			tableHomologViewer.refresh();
		} else if (e.getSource() == btnUnselectall) {
			selectedGenomes.clear();
			tableHomolog.deselectAll();
			GeneViewHomologTools.loadFigureHomologs(sequence, browserHomolog, selectedGenomes);
			tableHomologViewer.refresh();
		} else if (e.getSource() == tableGenes) {
			sequence = genome.getGeneFromName(tableGenes.getSelection()[0].getText());
			updateGeneInfo();
		} else if (e.getSource() == btnZoomplus) {
			trackGenome.zoom(true);
			canvasGenome.redraw();
		} else if (e.getSource() == btnNcbiChromosome) {
			NavigationManagement.openURLInExternalBrowser(RWTUtils.getGenomeNCBILink(chromoID), partService);
		} else if (e.getSource() == btnZoomminus) {
			trackGenome.zoom(false);
			canvasGenome.redraw();
		} else if (e.getSource() == btnShowSynteny) {
			btnShowSynteny.dispose();
			compSynt.layout(true,true);
			loadSynteny();
		} else if (e.getSource() == btnKEGG) {
			NavigationManagement.openURLInExternalBrowser(KEGGPath, partService);
		} else if (e.getSource() == btnUniprot) {
			NavigationManagement.openURLInExternalBrowser(UniprotPath, partService);
		} else if (e.getSource() == btnInterpro) {
			NavigationManagement.openURLInExternalBrowser(InterproPath, partService);
		} else if (e.getSource() == btnIntact) {
			NavigationManagement.openURLInExternalBrowser(IntactPath, partService);
		} else if (e.getSource() == btnString) {
			NavigationManagement.openURLInExternalBrowser(stringURL, partService);
			
		} else if (e.getSource() == btnNucleotideSequence) {
			Strand strand = Strand.POSITIVE;
			if (!sequence.isStrand())
				strand = Strand.NEGATIVE;
			SequenceDisplayDialog dialog = new SequenceDisplayDialog(shell, partService, genome, chromoID, true,
					sequence.getBegin(), sequence.getEnd(), strand);
			dialog.open();
		} else if (e.getSource() == btnAminoAcidSequence) {
			Strand strand = Strand.POSITIVE;
			if (!sequence.isStrand())
				strand = Strand.NEGATIVE;
			SequenceDisplayDialog dialog = new SequenceDisplayDialog(shell, partService, genome, chromoID, false,
					sequence.getBegin(), sequence.getEnd(), strand);
			dialog.open();
		} else if (e.getSource() == btnLocalization) {
			LocalizationDialog dialog =
					new LocalizationDialog(shell, arrayGeneToLocalization, sequence, bioCondsArray, genome);
			dialog.open();
		} else if (e.getSource() == btnGenomeViewer) {			
			GenomeTranscriptomeView.displayGenomeElementAndBioConditions(partService, genome.getSpecies(),
					getSelectedComparisons(), sequence.getName());
		} else if (e.getSource() == btnGenomeViewerProteome) {
			GenomeTranscriptomeView.displayGenomeElementAndBioConditions(partService, genome.getSpecies(),
					getSelectedComparisonsProteome(), sequence.getName());
		} else if (e.getSource() == btnUpdateCutoff) {
			GeneViewTranscriptomeTools.updateExpressionAtlas(sequence, txtCutoffLogFC, txtCutoffPvalue, this, arrayDataList);
		} else if (e.getSource() == btnUpdateCutoffProteome) {
			GeneViewProteomeTools.updateProteinAtlas(sequence, txtCutoffLogFCProteome, txtCutoffPvalue, this, arrayProteinAtlasList);
		} else if (e.getSource() == btnUpdateCutoffCoExp) {
			updateCoExp();
		} else if (e.getSource() == btnCorrMinus) {
            double cutoff = Double.parseDouble(txtCutoffCoExp.getText());
            if (cutoff > Network.CORR_CUTOFF) {
                cutoff = cutoff - 0.005;
                String cutoffString = cutoff + "";
                if (cutoffString.length() >= 5)
                    cutoffString = cutoffString.substring(0, 5);
                txtCutoffCoExp.setText(cutoffString);
                updateCoExp();
            }
        } else if (e.getSource() == btnExportNetwork) {
            //String[][] networkList = filteredNetwork.toArray();
            ArrayList<String> networkList = filteredNetwork.toArrayList();
            String arrayRep = filteredNetwork.toString();
            String arrayRepHTML = TabDelimitedTableReader.getTableInHTML(networkList);
            SaveFileUtils.saveTextFile("network.txt", arrayRep, true, "", arrayRepHTML, partService, shell);
        } else if (e.getSource() == btnCorrPlus) {
            double cutoff = Double.parseDouble(txtCutoffCoExp.getText());
            cutoff = cutoff + 0.005;
            String cutoffString = cutoff + "";
            if (cutoffString.length() >= 5)
                cutoffString = cutoffString.substring(0, 5);
            txtCutoffCoExp.setText(cutoffString);
            updateCoExp();
        } else if (e.getSource() == btnHeatmapview) {
			String sequenceName = sequence.getName();
			HeatMapTranscriptomicsView.displayComparisonsAndElement(genome.getSpecies(), getSelectedComparisons(),
					sequenceName, partService);
		} else if (e.getSource() == btnHeatmapviewProteome) {
			String sequenceName = sequence.getName();
			HeatMapProteomicsView.displayComparisonsAndElement(genome.getSpecies(), getSelectedComparisonsProteome(),
					sequenceName, partService);
		} else if (e.getSource() == btnExportToFasta) {
			ArrayList<String> fastaFile = new ArrayList<String>();
			// Load genomes
			HashMap<String, String> genomeToGenes = new HashMap<>();
			
			for (int i = 1; i < bioCondsArray.length; i++) {
				String genomeName = bioCondsArray[i][columnNames.indexOf("Name (GenBank)")];
				String processedGenomeName = GenomeNCBI.processGenomeName(genomeName);
				if (selectedGenomes.contains(processedGenomeName)) {
					genomeToGenes.put(genomeName, bioCondsArray[i][columnNames.indexOf("Homolog Protein")]);
				}
			}
			
			try {
				GetMultiFastaThread thread = new GetMultiFastaThread(genomeToGenes);
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(this.shell);
				dialog.run(true, true, thread);
				fastaFile = thread.getMultiFasta();
				String text = ListUtils.toString(fastaFile, "\n");
				SaveFileUtils.saveTextFile(sequence.getName() + "-MultiSeq.fasta", text, true, "", text, partService,
						shell);

				// InternalBrowser.openList(fastaFile,"Export this fasta file to a text file
				// software (Copy all + paste)",partService);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}/*
		} else if (e.getSource() == btnHelp) {
			HelpPage.helpGeneView(partService);*/
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
				if (obj instanceof Gene) {
					Gene gene = (Gene) obj;
					System.out.println("select obj "+gene.getName());

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
			trackGenome.search(position + "");
			canvasGenome.redraw();
		} else if (e.button == 2) {
			System.out.println(e.count);
		}
	}

	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8407623310655339121L;

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}
	}
	
	

	public Gene getSequence() {
		return sequence;
	}

	public void setSequence(Gene sequence) {
		this.sequence = sequence;
	}

	public void setChromoID(String chromoID) {
		this.chromoID = chromoID;
	}
	
	public Combo getComboGenome() {
		return comboGenome;
	}

	public void setComboGenome(Combo comboGenome) {
		this.comboGenome = comboGenome;
	}

	public ArrayList<String> getListGenes() {
		return listGenes;
	}

	public void setListGenes(ArrayList<String> listGenes) {
		this.listGenes = listGenes;
	}

	public Table getTableGenes() {
		return tableGenes;
	}

	public void setTableGenes(Table tableGenes) {
		this.tableGenes = tableGenes;
	}

	public Genome getGenome() {
		return genome;
	}

	public void setGenome(Genome genome) {
		this.genome = genome;
	}
	public Table getTableTranscriptomes() {
		return tableTranscriptomes;
	}

	public void setTableTranscriptomes(Table tableTranscriptomes) {
		this.tableTranscriptomes = tableTranscriptomes;
	}

	public Table getTableOver() {
		return tableOver;
	}

	public void setTableOver(Table tableOver) {
		this.tableOver = tableOver;
	}

	public Table getTableUnder() {
		return tableUnder;
	}

	public void setTableUnder(Table tableUnder) {
		this.tableUnder = tableUnder;
	}

	public Table getTableNodiff() {
		return tableNodiff;
	}

	public void setTableNodiff(Table tableNodiff) {
		this.tableNodiff = tableNodiff;
	}

	public Label getLblOver() {
		return lblOver;
	}

	public void setLblOver(Label lblOver) {
		this.lblOver = lblOver;
	}

	public Label getLblUnder() {
		return lblUnder;
	}

	public void setLblUnder(Label lblUnder) {
		this.lblUnder = lblUnder;
	}

	public Label getLblNodiff() {
		return lblNodiff;
	}

	public void setLblNodiff(Label lblNodiff) {
		this.lblNodiff = lblNodiff;
	}

	public Label getLblOverTranscriptomes() {
		return lblExprTranscriptomes;
	}

	public void setLblOverTranscriptomes(Label lblExprTranscriptomes) {
		this.lblExprTranscriptomes = lblExprTranscriptomes;
	}


	public Label getLblOverProteome() {
		return lblOverProteome;
	}

	public void setLblOveProteome(Label lblOver) {
		this.lblOverProteome = lblOver;
	}

	public Label getLblUnderProteome() {
		return lblUnderProteome;
	}

	public void setLblUnderProteome(Label lblUnder) {
		this.lblUnderProteome = lblUnder;
	}

	public Label getLblNodiffProteome() {
		return lblNodiffProteome;
	}

	public void setLblNodiffProteome(Label lblNodiff) {
		this.lblNodiffProteome = lblNodiff;
	}

	public Text getTxtCutoffLogFC() {
		return txtCutoffLogFC;
	}

	public Text getTxtCutoffLogFCProteome() {
		return txtCutoffLogFCProteome;
	}

	public void setTxtCutoffLogFC(Text txtCutoffLogFC) {
		this.txtCutoffLogFC = txtCutoffLogFC;
	}

	public void setTxtCutoffLogFCProteome(Text txtCutoffLogFCProteome) {
		this.txtCutoffLogFCProteome = txtCutoffLogFCProteome;
	}

	public Text getTxtCutoffPvalue() {
		return txtCutoffPvalue;
	}

	public void setTxtCutoffPvalue(Text txtCutoffPvalue) {
		this.txtCutoffPvalue = txtCutoffPvalue;
	}
	public Text getTxtCutoffPvalueProteome() {
		return txtCutoffPvalueProteome;
	}

	public void getTxtCutoffPvalueProteome(Text txtCutoffPvalueProteome) {
		this.txtCutoffPvalueProteome = txtCutoffPvalueProteome;
	}
	public Label getLblTranscriptomesData() {
		return lblTranscriptomesData;
	}

	public void setLblTranscriptomesData(Label lblTranscriptomesData) {
		this.lblTranscriptomesData = lblTranscriptomesData;
	}

	public Table getTableOverProteome() {
		return tableOverProteome;
	}

	public void setTableOverProteome(Table tableOverProteome) {
		this.tableOverProteome = tableOverProteome;
	}

	public Table getTableUnderProteome() {
		return tableUnderProteome;
	}

	public void setTableUnderProteome(Table tableUnderProteome) {
		this.tableUnderProteome = tableUnderProteome;
	}

	public Table getTableNodiffProteome() {
		return tableNodiffProteome;
	}

	public void setTableNodiffProteome(Table tableNodiffProteome) {
		this.tableNodiffProteome = tableNodiffProteome;
	}

	public Table getTableProteomes() {
		return tableProteomes;
	}

	public void setTableProteomes(Table tableProteomes) {
		this.tableProteomes = tableProteomes;
	}

	public Label getLblOverProteomes() {
		return lblExprProteomes;
	}

	public void setLblOverProteomes(Label lblExprProteomes) {
		this.lblExprProteomes = lblExprProteomes;
	}

	public Track getTrackGenome() {
		return trackGenome;
	}

	public void setTrackGenome(Track trackGenome) {
		this.trackGenome = trackGenome;
	}

	public String[][] getArrayProteomeList() {
		return arrayProteomeList;
	}

	public void setArrayProteomeList(String[][] arrayProteomeList) {
		this.arrayProteomeList = arrayProteomeList;
	}

	public String[][] getArrayTranscriptomesList() {
		return arrayTranscriptomesList;
	}

	public void setArrayTranscriptomesList(String[][] arrayProteomeList) {
		this.arrayTranscriptomesList = arrayProteomeList;
	}

	public String[][] getBioCondsArray() {
		return bioCondsArray;
	}

	public void setBioCondsArray(String[][] bioCondsArray) {
		this.bioCondsArray = bioCondsArray;
	}

	public ArrayList<String[]> getBioConds() {
		return bioConds;
	}

	public void setBioConds(ArrayList<String[]> bioConds) {
		this.bioConds = bioConds;
	}

	public ArrayList<String[]> getBioCondsToDisplay() {
		return bioCondsToDisplay;
	}

	public void setBioCondsToDisplay(ArrayList<String[]> bioCondsToDisplay) {
		this.bioCondsToDisplay = bioCondsToDisplay;
	}

	public String[][] getArrayGeneToLocalization() {
		return arrayGeneToLocalization;
	}

	public void setArrayGeneToLocalization(String[][] arrayGeneToLocalization) {
		this.arrayGeneToLocalization = arrayGeneToLocalization;
	}

	public String[][] getArrayDataList() {
		return arrayDataList;
	}

	public void setArrayDataList(String[][] arrayDataList) {
		this.arrayDataList = arrayDataList;
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public void setTabFolder(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
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
