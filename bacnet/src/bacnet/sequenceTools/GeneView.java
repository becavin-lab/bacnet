package bacnet.sequenceTools;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import bacnet.Database;
import bacnet.datamodel.annotation.SubCellCompartment;
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
import bacnet.expressionAtlas.HeatMapTranscriptomicsView;
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
    private Text lblName;
    private Text lblBegin;
    private Text lblEnd;
    private Text lblSizeaa;
    private Text lblSizeBP;
    private Text lblProduct;
    private Text lblStrand;
    private Text lblLocus;
    private Text lblCog;
    private Label lblProtID;
    private Combo comboGenome;
    private Text textFeature;
    private Composite composite_pvalue;
    private Text lblOperon;
    private Composite composite_7;
    private Table tableGenes;
    private ScrolledComposite scrolledComposite;
    private Label lblGene;
    private TrackCanvasGenome canvasGenome;
    private Text txtSearch;
    @SuppressWarnings("unused")
    private boolean browserIsFocus = false;

    private String[][] arrayDataList = new String[0][0];
    private TabItem tbtmSynteny;
    private Composite composite_12;
    private Browser browserSynteny;
    private Composite composite_15;
    private Button btnNucleotideSequence;
    private Button btnAminoAcidSequence;
    private Button btnLocalization;

    /*
     * Expression atlas variables
     */
    private String[][] arrayProteomeList = new String[0][0];
    private Composite composite;
    private Table tableOver;
    private Table tableUnder;
    private Table tableNodiff;
    private Label lblOver;
    private Label lblUnder;
    private Label lblNodiff;

    private Button btnGenomeViewer;
    private TabFolder tabFolder;
    private TabItem tbtmGeneralInformation;
    private TabItem tbtmExpressionData;
    private Button btnUpdateCutoff;
    private Text txtCutoffLogFC;
    private Text txtCutoffPvalue;
    private Button btnHeatmapview;
    private Button btnZoomplus;
    private Button btnZoomminus;
    private Combo comboChromosome;
    private Label lblTranscriptomesData;
    private TabItem tbtmProteomes;
    private Composite composite_9;
    private Table tableProteomes;
    private Label lblOverProteomes;
    private Text lblConservation;
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
    private Button btnHelp;
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
    private Composite composite_6;
    private Button btnSaveAsPng;
    private Button btnSaveAsSvg;
    private Composite composite_16;
    private Composite composite_17;
    private Text txtSearchGenome;
    private Button btnSelectall;
    private Button btnUnselectall;
    private Button btnDonwloadtxt;
    private Composite composite_18;
    private Composite compSuppInfo;

    @Inject
    public GeneView() {

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
        container.setBounds(0, 0, 586, 480);
        container.setLayout(new GridLayout(4, false));

        compositeGenome = new Composite(container, SWT.NONE);
        compositeGenome.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        compositeGenome.setLayout(new GridLayout(7, false));

        Label lblSelectAGenome = new Label(compositeGenome, SWT.NONE);
        lblSelectAGenome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSelectAGenome.setText("Select genome");

        comboGenome = new Combo(compositeGenome, SWT.NONE);
        GridData gd_comboGenome = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_comboGenome.widthHint = 220;
        comboGenome.setLayoutData(gd_comboGenome);

        Label lblChromosome = new Label(compositeGenome, SWT.NONE);
        lblChromosome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblChromosome.setText("Chromosome");

        comboChromosome = new Combo(compositeGenome, SWT.NONE);
        GridData gd_comboChromosome = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_comboChromosome.widthHint = 125;
        comboChromosome.setLayoutData(gd_comboChromosome);

        btnNcbiChromosome = new Button(compositeGenome, SWT.NONE);
        btnNcbiChromosome.setToolTipText("Access to more information on the genome");
        btnNcbiChromosome.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/ncbi.png"));
        btnNcbiChromosome.addSelectionListener(this);

        btnGetAnnotationInformation = new Button(compositeGenome, SWT.NONE);
        btnGetAnnotationInformation.setText("Browse and download annotation table");
        btnGetAnnotationInformation.setToolTipText("Browse and download annotation table");
        btnGetAnnotationInformation.addSelectionListener(this);
        btnGetAnnotationInformation.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
        new Label(compositeGenome, SWT.NONE);

        lblTranscriptomesData = new Label(compositeGenome, SWT.NONE);
        lblTranscriptomesData.setText(" * Transcriptomes data available");
        lblTranscriptomesData.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);
        comboChromosome.addSelectionListener(this);
        comboGenome.addSelectionListener(this);
        new Label(container, SWT.NONE);
        btnHelp = new Button(container, SWT.NONE);
        btnHelp.setToolTipText("How to use Gene panel");
        btnHelp.setImage(ResourceManager.getPluginImage("bacnet", "icons/help.png"));
        btnHelp.addSelectionListener(this);

        composite_7 = new Composite(container, SWT.BORDER);
        composite_7.setLayout(new GridLayout(1, false));
        composite_7.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));

        Label lblSearch = new Label(composite_7, SWT.NONE);
        lblSearch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblSearch.setText("Search");
        lblSearch.setForeground(BasicColor.GREY);

        txtSearch = new Text(composite_7, SWT.BORDER);
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
                    System.out.println("Search for " + txtSearch.getText());
                    ArrayList<String> searchResults = search(txtSearch.getText());
                    if (searchResults.size() != 0) {
                        listGenes.clear();
                        for (String gene : searchResults) {
                            String text = gene;
                            String oldLocusTag = genome.getChromosomes().get(chromoID).getGenes().get(gene).getFeature("old_locus_tag");
                            if (!oldLocusTag.equals("")) {
                                text += " - " + oldLocusTag;
                            }
                            String geneName = genome.getChromosomes().get(chromoID).getGenes().get(gene).getGeneName();
                            if (!geneName.equals("")) {
                                text += " (" + geneName + ")";
                            }
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

        scrolledComposite = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite composite_11 = new Composite(scrolledComposite, SWT.BORDER);
        composite_11.setLayout(new GridLayout(1, false));

        lblGene = new Label(composite_11, SWT.BORDER | SWT.CENTER);
        GridData gd_lblGene = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblGene.widthHint = 400;
        lblGene.setLayoutData(gd_lblGene);
        lblGene.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblGene.setFont(SWTResourceManager.getTitleFont());
        lblGene.setText("gene");

        tabFolder = new TabFolder(composite_11, SWT.NONE);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        tabFolder.addSelectionListener(this);
        tbtmGeneralInformation = new TabItem(tabFolder, SWT.NONE);
        tbtmGeneralInformation.setText("General information");

        Composite composite_1 = new Composite(tabFolder, SWT.BORDER);
        tbtmGeneralInformation.setControl(composite_1);
        composite_1.setLayout(new GridLayout(3, false));

        Composite compGenomeViewer = new Composite(composite_1, SWT.NONE);
        compGenomeViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        compGenomeViewer.setLayout(new GridLayout(2, false));

        canvasGenome = new TrackCanvasGenome(compGenomeViewer, SWT.BORDER);
        GridData gd_canvasGenome = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2);
        gd_canvasGenome.heightHint = 125;
        canvasGenome.setLayoutData(gd_canvasGenome);
        canvasGenome.setLayout(new GridLayout(1, false));
        canvasGenome.addMouseListener(this);

        btnZoomplus = new Button(compGenomeViewer, SWT.NONE);
        btnZoomplus.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
        btnZoomplus.setToolTipText("Zoom In horizontally");
        btnZoomplus.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/zoomIN.bmp"));
        btnZoomplus.addSelectionListener(this);
        btnZoomminus = new Button(compGenomeViewer, SWT.NONE);
        btnZoomminus.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnZoomminus.setToolTipText("Zoom Out horizontally");
        btnZoomminus.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/zoomOUT.bmp"));
        btnZoomminus.addSelectionListener(this);

        Composite compGeneralInfo = new Composite(composite_1, SWT.BORDER);
        GridData gd_compGeneralInfo = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
        gd_compGeneralInfo.widthHint = 350;
        compGeneralInfo.setLayoutData(gd_compGeneralInfo);
        compGeneralInfo.setLayout(new GridLayout(2, false));

        lblLocus = new Text(compGeneralInfo, SWT.READ_ONLY);
        lblLocus.setTouchEnabled(true);
        GridData gd_lblLocus = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblLocus.widthHint = 160;
        lblLocus.setLayoutData(gd_lblLocus);
        lblLocus.setText("Locus");

        lblBegin = new Text(compGeneralInfo, SWT.READ_ONLY);
        lblBegin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblBegin.setText("Begin");

        lblStrand = new Text(compGeneralInfo, SWT.READ_ONLY);
        GridData gd_lblStrand = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblStrand.widthHint = 65;
        lblStrand.setLayoutData(gd_lblStrand);
        lblStrand.setText("Strand");

        lblEnd = new Text(compGeneralInfo, SWT.READ_ONLY);
        lblEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblEnd.setText("End");

        lblSizeBP = new Text(compGeneralInfo, SWT.READ_ONLY);
        GridData gd_lblSizeBP = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblSizeBP.widthHint = 50;
        lblSizeBP.setLayoutData(gd_lblSizeBP);
        lblSizeBP.setText("Size");

        lblSizeaa = new Text(compGeneralInfo, SWT.READ_ONLY);
        lblSizeaa.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblSizeaa.setText("SizeAA");

        lblName = new Text(compGeneralInfo, SWT.READ_ONLY);
        lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        lblName.setText("Name: ");

        lblProduct = new Text(compGeneralInfo, SWT.READ_ONLY | SWT.WRAP);
        GridData gd_lblProduct = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_lblProduct.heightHint = 60;
        lblProduct.setLayoutData(gd_lblProduct);
        lblProduct.setText("Product");
        new Label(compGeneralInfo, SWT.NONE);
        new Label(compGeneralInfo, SWT.NONE);
        new Label(compGeneralInfo, SWT.NONE);

        composite_15 = new Composite(compGeneralInfo, SWT.BORDER);
        composite_15.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        composite_15.setLayout(new GridLayout(2, false));

        btnNucleotideSequence = new Button(composite_15, SWT.NONE);
        btnNucleotideSequence.setText("Nucleotide sequence");
        btnNucleotideSequence.addSelectionListener(this);
        btnAminoAcidSequence = new Button(composite_15, SWT.NONE);
        btnAminoAcidSequence.setText("Amino acid sequence");
        btnAminoAcidSequence.addSelectionListener(this);

        compSuppInfo = new Composite(composite_1, SWT.BORDER);
        GridData gd_compSuppInfo = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
        gd_compSuppInfo.widthHint = 300;
        compSuppInfo.setLayoutData(gd_compSuppInfo);
        compSuppInfo.setLayout(new GridLayout(1, false));

        lblOperon = new Text(compSuppInfo, SWT.READ_ONLY);
        lblOperon.setText("Operon");
        lblProtID = new Label(compSuppInfo, SWT.READ_ONLY);
        lblProtID.setText("protid");
        RWTUtils.setMarkup(lblProtID);

        lblCog = new Text(compSuppInfo, SWT.READ_ONLY | SWT.WRAP);
        lblCog.setText("COG");

        lblConservation = new Text(compSuppInfo, SWT.NONE);
        lblConservation.setText("Homologs in 00/50 Listeria genomes");

        composite_localization = new Composite(composite_1, SWT.BORDER);
        composite_localization.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 4));
        composite_localization.setLayout(new GridLayout(1, false));

        lblPredictedSubcellularLocalization = new Label(composite_localization, SWT.WRAP);
        GridData gd_lblPredictedSubcellularLocalization = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblPredictedSubcellularLocalization.widthHint = 200;
        gd_lblPredictedSubcellularLocalization.heightHint = 55;
        lblPredictedSubcellularLocalization.setLayoutData(gd_lblPredictedSubcellularLocalization);
        RWTUtils.setMarkup(lblPredictedSubcellularLocalization);
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
        textFeature = new Text(composite_1, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
        textFeature.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);

        tbtmHomologs = new TabItem(tabFolder, SWT.NONE);
        tbtmHomologs.setText("Homologs");

        composite_13 = new Composite(tabFolder, SWT.NONE);
        tbtmHomologs.setControl(composite_13);
        composite_13.setLayout(new GridLayout(3, false));

        composite_14 = new Composite(composite_13, SWT.NONE);
        composite_14.setLayout(new GridLayout(3, false));
        composite_14.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        btnExportToFasta = new Button(composite_14, SWT.NONE);
        btnExportToFasta.setText("Export selected gene to multi sequence fasta file");
        btnExportToFasta.addSelectionListener(this);

        lblConservation2 = new Label(composite_14, SWT.NONE);
        lblConservation2.setText("Homologs in 00/50 Listeria genomes");
        new Label(composite_14, SWT.NONE);

        composite_18 = new Composite(composite_13, SWT.NONE);
        composite_18.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
        composite_18.setLayout(new GridLayout(1, false));

        composite_6 = new Composite(composite_18, SWT.NONE);
        composite_6.setLayout(new GridLayout(3, false));

        Label lblDownloadPhylogenomicConservation = new Label(composite_6, SWT.NONE);
        lblDownloadPhylogenomicConservation.setText("Download Phylogenomic conservation tree as");
        lblDownloadPhylogenomicConservation.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

        btnSaveAsPng = new Button(composite_6, SWT.NONE);
        btnSaveAsPng.setToolTipText("Download as PNG image");
        btnSaveAsPng.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/png.bmp"));

        btnSaveAsSvg = new Button(composite_6, SWT.NONE);
        btnSaveAsSvg.setToolTipText("Download as SVG vector image (for Illustrator, GIMP, ...)");
        btnSaveAsSvg.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/svg.bmp"));
        btnSaveAsSvg.addSelectionListener(this);
        btnSaveAsPng.addSelectionListener(this);

        browserHomolog = new Browser(composite_18, SWT.BORDER);
        GridData gd_browserHomolog = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_browserHomolog.widthHint = 400;
        browserHomolog.setLayoutData(gd_browserHomolog);

        composite_16 = new Composite(composite_13, SWT.BORDER);
        composite_16.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
        composite_16.setLayout(new GridLayout(1, false));

        composite_17 = new Composite(composite_16, SWT.NONE);
        composite_17.setLayout(new GridLayout(8, false));

        txtSearchGenome = new Text(composite_17, SWT.BORDER);
        txtSearchGenome.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
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

        Label lblSearch_1 = new Label(composite_17, SWT.NONE);
        lblSearch_1.setText("Search");
        lblSearch_1.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

        btnSelectall = new Button(composite_17, SWT.NONE);
        btnSelectall.setToolTipText("Select all genes");
        btnSelectall.setImage(ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
        btnSelectall.addSelectionListener(this);

        Label lblSelectAll = new Label(composite_17, SWT.NONE);
        lblSelectAll.setText("Select all");
        lblSelectAll.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

        btnUnselectall = new Button(composite_17, SWT.NONE);
        btnUnselectall.setToolTipText("Unselect all genes");
        btnUnselectall.setImage(ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
        btnUnselectall.addSelectionListener(this);

        Label lblUnselectAll = new Label(composite_17, SWT.NONE);
        lblUnselectAll.setText("Unselect all");
        lblUnselectAll.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

        btnDonwloadtxt = new Button(composite_17, SWT.NONE);
        btnDonwloadtxt.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
        btnDonwloadtxt.addSelectionListener(this);

        Label lblDownload = new Label(composite_17, SWT.NONE);
        lblDownload.setText("Download gene selection as a table");
        lblDownload.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

        Label lblClickOneTime = new Label(composite_16, SWT.NONE);
        lblClickOneTime.setText(
                "Select strain to highlight. Double click to acces gene information");
        lblClickOneTime.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

        tableHomologViewer = new TableViewer(composite_16, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        tableHomolog = tableHomologViewer.getTable();
        tableHomolog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        RWTUtils.setMarkup(tableHomolog);
        tableHomologViewer.getTable().setHeaderVisible(true);
        tableHomologViewer.getTable().setLinesVisible(true);
        tableHomologViewer.setLabelProvider(new TableLabelProvider());
        tableHomologViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                for (int i : tableHomolog.getSelectionIndices()) {
                    String selectedGenome = tableHomolog.getItem(i).getText(columnNames.indexOf("Name") + 1);
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
                                .getText(columnNames.indexOf("Name") + 1);
                String selectedGene =
                        tableHomologViewer.getTable().getItem(tableHomologViewer.getTable().getSelectionIndex())
                                .getText(columnNames.indexOf("Homolog") + 1);
                // System.out.println(tableHomologViewer.getTable().getSelectionIndex()+"
                // "+columnNames.indexOf("Name")+ "yahou "+selectedGene+" "+selectedGenome);
                Genome genome = Genome.loadGenome(selectedGenome);
                Gene gene = genome.getGeneFromName(selectedGene);
                GeneView.displayGene(gene, partService);
            }
        });
        new Label(composite_13, SWT.NONE);

        tbtmSynteny = new TabItem(tabFolder, SWT.NONE);
        tbtmSynteny.setText("Synteny");

        composite_12 = new Composite(tabFolder, SWT.NONE);
        tbtmSynteny.setControl(composite_12);
        composite_12.setLayout(new GridLayout(1, false));
        browserSynteny = new Browser(composite_12, SWT.NONE);
        browserSynteny.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        tbtmExpressionData = new TabItem(tabFolder, SWT.NONE);
        tbtmExpressionData.setText("Expression Atlas");

        composite = new Composite(tabFolder, SWT.BORDER);
        tbtmExpressionData.setControl(composite);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        composite.setLayout(new GridLayout(2, false));

        Composite composite_8 = new Composite(composite, SWT.BORDER);
        composite_8.setLayout(new GridLayout(1, false));

        {
            Composite composite_2 = new Composite(composite_8, SWT.NONE);
            composite_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            composite_2.setLayout(new GridLayout(2, false));

            Label lbllogfoldchange = new Label(composite_2, SWT.NONE);
            lbllogfoldchange.setText("|Log(Fold-Change)| >");

            txtCutoffLogFC = new Text(composite_2, SWT.BORDER);
            txtCutoffLogFC.setText("1.5");
        }
        {
            composite_pvalue = new Composite(composite_8, SWT.NONE);
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
        btnUpdateCutoff = new Button(composite_8, SWT.NONE);
        btnUpdateCutoff.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
        btnUpdateCutoff.setText("Choose cut-off and update Expression Atlas");
        btnUpdateCutoff.addSelectionListener(this);

        Composite composite_3 = new Composite(composite, SWT.NONE);
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

        tableOver = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd_listOver = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
        gd_listOver.heightHint = 150;
        tableOver.setLayoutData(gd_listOver);
        tableOver.addSelectionListener(this);

        Label lblUnderExpressedIn = new Label(composite, SWT.NONE);
        lblUnderExpressedIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblUnderExpressedIn.setText("Under expressed in");

        lblUnder = new Label(composite, SWT.NONE);
        lblUnder.setText("under");

        tableUnder = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd_listUnder = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
        gd_listUnder.heightHint = 150;
        tableUnder.setLayoutData(gd_listUnder);
        tableUnder.addSelectionListener(this);

        Label lblNoDiffExpression = new Label(composite, SWT.NONE);
        lblNoDiffExpression.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblNoDiffExpression.setText("No diff. expression in");

        lblNodiff = new Label(composite, SWT.NONE);
        lblNodiff.setText("nodiff");

        tableNodiff = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd_listNodiff = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
        gd_listNodiff.heightHint = 150;
        tableNodiff.setLayoutData(gd_listNodiff);
        tableNodiff.addSelectionListener(this);

        tbtmProteomes = new TabItem(tabFolder, SWT.NONE);
        tbtmProteomes.setText("Proteomes");

        composite_9 = new Composite(tabFolder, SWT.NONE);
        tbtmProteomes.setControl(composite_9);
        composite_9.setLayout(new GridLayout(2, false));

        Label lblOverExpressedInProteomes = new Label(composite_9, SWT.NONE);
        lblOverExpressedInProteomes.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblOverExpressedInProteomes.setText("Found in ");

        lblOverProteomes = new Label(composite_9, SWT.NONE);
        lblOverProteomes.setText("over");

        tableProteomes = new Table(composite_9, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        tableProteomes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        scrolledComposite.setContent(composite_11);
        scrolledComposite.setMinSize(composite_11.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT
                || Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
            initSyntenyBrowser();
        }

    }

    private void initSyntenyBrowser() {
        try {
            String realUrl = FileUtils.getPath(NavigationManagement.getURL());
            if (realUrl.contains("/Listeriomics/")) {
                realUrl = realUrl.replaceAll("Listeriomics/", "");
            } else if (realUrl.contains("/UIBCListeriomics/")) {
                realUrl = realUrl.replaceAll("UIBCListeriomics/", "");
            }
            realUrl = "https://listeriomics.pasteur.fr/";
            String pathGraphHTML = realUrl + "SynTView/flash/indexFinal.html";
            System.out.println("SyntView: " + pathGraphHTML);
            browserSynteny.setUrl(pathGraphHTML);
            browserSynteny.redraw();
        } catch (Exception e) {
            System.out.println("Cannot create brower");
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
            OpenGenomesThread thread = new OpenGenomesThread(genomeNames);
            new ProgressMonitorDialog(this.shell).run(true, false, thread);
            genome = Genome.loadGenome(genomeName);

        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        initGenomeInfo();

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
        } else if (Database.getInstance().getProjectName() == Database.YERSINIOMICS_PROJECT) {
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
     * Initi Widgets for Yersiniomics
     */
    private void initYersiniomics() {
        arrayDataList = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesComparisonsArrayPath());
        arrayProteomeList = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
        ArrayList<String> dataTranscriptomes = BioCondition.getTranscriptomesGenomes();
        for (String genomeTemp : Database.getInstance().getGenomeList()) {
            if (dataTranscriptomes.contains(genomeTemp)) {
                genomeTemp = genomeTemp + " *";
            }
            comboGenome.add(genomeTemp);
        }
        tbtmSynteny.dispose();
        composite_localization.dispose();
        // arrayGeneToLocalization =
        // TabDelimitedTableReader.read(SubCellCompartment.LOCALIZATION_PATH);
    }

    /**
     * The comboGenome contains modified genome name so we need this method to get selected element<br>
     * a '*' is add to genome name when a transcriptome data is available
     */
    public String getGenomeSelected() {
        if (comboGenome.isDisposed()) {
            return Genome.EGDE_NAME;
        } else {
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
     * a '*' is add to genome name when a transcriptome data is available
     * 
     * @param genome
     */
    public void setGenomeSelected(String genome) {
        if (!comboGenome.isDisposed()) {
            // select genome
            for (String genomeItem : comboGenome.getItems()) {
                if (genomeItem.contains(genome)) {
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
            String text = gene;
            String oldLocusTag = genome.getChromosomes().get(chromoID).getGenes().get(gene).getFeature("old_locus_tag");
            if (!oldLocusTag.equals("")) {
                text += " - " + oldLocusTag;
            }
            String geneName = genome.getChromosomes().get(chromoID).getGenes().get(gene).getGeneName();
            if (!geneName.equals("")) {
                text += " (" + geneName + ")";
            }
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
                    || Database.getInstance().getProjectName() == Database.YERSINIOMICS_PROJECT) {
                updateAllGeneOmicsInfo();
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
        String title = sequence.getName();
        // System.out.println(seq.getName());
        if (!sequence.getGeneName().equals("") && !sequence.getGeneName().equals("-"))
            title += " - " + sequence.getGeneName();
        lblGene.setText(title);
        lblLocus.setText("Locus: " + sequence.getName() +  " - "+ sequence.getFeature("old_locus_tag") );
        lblBegin.setText("Begin: " + sequence.getBegin() + "");
        lblSizeBP.setText("Size bp: " + sequence.getLength());
        lblStrand.setText("Strand: " + sequence.getStrand());
        lblName.setText("Gene: " + sequence.getGeneName());
        lblEnd.setText("End: " + sequence.getEnd() + "");
        lblSizeaa.setText("Size aa: " + sequence.getLengthAA());
        lblProduct.setText("Product: " + sequence.getProduct());
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
         * Transcriptome update
         */
        if (genomeTranscriptomes.contains(genome.getSpecies())) {
            GeneViewTranscriptomeTools.updateExpressionAtlas(sequence, txtCutoffLogFC, this, arrayDataList);
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

        if (!sequence.getOperon().equals("")) {
            Operon operon = genome.getChromosomes().get(chromoID).getOperons().get(sequence.getOperon());
            lblOperon.setText("In " + sequence.getOperon() + " containing " + operon.getGenes().size() + " genes");
        } else {
            lblOperon.setText("Not in an operon");
        }
        lblConservation.setText("Homologs in " + (sequence.getConservation() - 1) + "/"
                + Genome.getAvailableGenomes().size() + " "+Database.getInstance().getSpecies()+" genomes");
        lblConservation2.setText("Homologs in " + (sequence.getConservation() - 1) + "/"
                + Genome.getAvailableGenomes().size() + " "+Database.getInstance().getSpecies()+" genomes");
        lblProduct.setText("Product: " + sequence.getProduct());
        lblProtID.setText("ProteinId: " + RWTUtils.setProteinNCBILink(sequence.getProtein_id()));
        lblCog.setText("COG: " + sequence.getCog());

        ArrayList<String> genomeTranscriptomes = BioCondition.getTranscriptomesGenomes();
        ArrayList<String> genomeProteomes = BioCondition.getProteomeGenomes();
        /*
         * Homolog update
         */
        updateHomolog();

        /*
         * Load localization
         */
        GeneViewLocalizationTools.loadLocalizationFigure(browserLocalization, arrayGeneToLocalization, sequence,
                bioCondsArray, null, genome);

        /*
         * Update synteny view
         */
        try {
            browserSynteny.evaluate("search('" + sequence.getName() + "')");
        } catch (Exception e) {
            System.out.println("Cannot evaluate: " + "search('" + sequence.getName() + "')");
        }

        /*
         * Transcriptome update
         */
        if (genomeTranscriptomes.contains(genome.getSpecies())) {
            // System.out.println(genome.getSpecies());
            GeneViewTranscriptomeTools.updateExpressionAtlas(sequence, txtCutoffLogFC, this, arrayDataList);
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
    
    public void updateHomolog() {
    	bioCondsArray = GeneViewHomologTools.loadArrayHomologs(sequence, bioCondsArray, bioConds, bioCondsToDisplay);
        GeneViewHomologTools.loadFigureHomologs(sequence, browserHomolog, selectedGenomes);
        GeneViewHomologTools.updateHomologTable(tableHomologViewer, bioCondsArray, bioCondsToDisplay, bioConds,
                comparatorBioCondition, columnNames, selectedGenomes, txtSearchGenome);
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
            String comparison = tableOver.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name"));
            // System.out.println(comparison);
            comparisons.add(comparison);
        }
        for (int index : tableUnder.getSelectionIndices()) {
            String comparison = tableUnder.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name"));
            // System.out.println(comparison);
            comparisons.add(comparison);
        }
        for (int index : tableNodiff.getSelectionIndices()) {
            String comparison = tableNodiff.getItem(index).getText(ArrayUtils.findColumn(arrayDataList, "Data Name"));
            // System.out.println(comparison);
            comparisons.add(comparison);
        }
        return comparisons;
    }

    /**
     * Display a given gene
     * 
     * @param gene
     * @param partService
     */
    public static void displayGene(Gene gene, EPartService partService) {
        String id = GeneView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
        // initiate view
        ResourceManager.openView(partService, GeneView.ID, id);
        // update data
        MPart part = partService.findPart(id);
        GeneView view = (GeneView) part.getObject();
        view.setViewID(id);
         System.out.println("Select current genome:  "+FileUtils.removeExtension(gene.getGenomeName()));
        view.initGenomeInfo(gene.getGenomeName());
        view.setGenomeSelected(gene.getGenomeName());
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
     * It will display Genome.getDefautGenome();
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

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == comboGenome) {
            genome = Genome.loadGenome(getGenomeSelected());
            chromoID = genome.getFirstChromosome().getAccession().toString();
            updateComboChromosome(chromoID);
            updateListGenomeElements();
            updateGenomeViewer();
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
                System.out.println("Convert Phylogeny.svg to Phylogeny.png\nHave you set ImageMagick PATH in ImageMagick.getConvertPATH()\nYours is set to: "+ImageMagick.getConvertPATH());
                CMD.runProcess(ImageMagick.getConvertPATH() + " " + tempSVGFile.getAbsolutePath() + " " + tempPNGFile);
                SaveFileUtils.saveFile("Listeria_Phylogenomic_Tree_" + sequence.getName() + ".png", tempPNGFile,
                        "PNG image file", partService, shell);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } else if (e.getSource() == btnSaveAsSvg) {
            String textSVG = GeneViewHomologTools.getPhyloFigure(sequence, selectedGenomes);
            SaveFileUtils.saveTextFile("Listeria_Phylogenomic_Tree_" + sequence.getName() + ".svg", textSVG, true,
                    "SVG (vector image) file", textSVG, partService, shell);
        } else if (e.getSource() == btnDonwloadtxt) {
            String[][] arrayToSave = new String[1][columnNames.size()];
            for (int i = 0; i < columnNames.size(); i++) {
                arrayToSave[0][i] = columnNames.get(i);
            }
            int k = 1;
            for (int i = 1; i < bioCondsArray.length; i++) {
                String genomeName = bioCondsArray[i][columnNames.indexOf("Name")];
                if (selectedGenomes.contains(genomeName)) {
                    arrayToSave = ArrayUtils.addRow(arrayToSave, ArrayUtils.getRow(bioCondsArray, i), k);
                    k++;
                }
            }
            String arrayRep = ArrayUtils.toString(arrayToSave);
            String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(arrayToSave);
            SaveFileUtils.saveTextFile("Listeria_Genomic_Table_" + sequence.getName() + ".txt", arrayRep, true, "",
                    arrayRepHTML, partService, shell);
        } else if (e.getSource() == btnSelectall) {
            selectedGenomes.clear();
            tableHomolog.selectAll();
            for (int i : tableHomolog.getSelectionIndices()) {
                String selectedGenome = GenomeNCBI.processGenomeName(tableHomolog.getItem(i).getText(columnNames.indexOf("Name") + 1));
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
        } else if (e.getSource() == btnUpdateCutoff) {
            GeneViewTranscriptomeTools.updateExpressionAtlas(sequence, txtCutoffLogFC, this, arrayDataList);
        } else if (e.getSource() == btnHeatmapview) {
            String sequenceName = sequence.getName();
            HeatMapTranscriptomicsView.displayComparisonsAndElement(genome.getSpecies(), getSelectedComparisons(),
                    sequenceName, partService);
        } else if (e.getSource() == btnExportToFasta) {
            ArrayList<String> fastaFile = new ArrayList<String>();
            // Load genomes
            HashMap<String, String> genomeToGenes = new HashMap<>();
            for (int i : tableHomologViewer.getTable().getSelectionIndices()) {
                TableItem item = tableHomologViewer.getTable().getItem(i);
                String genomeName = item.getText(ArrayUtils.findColumn(bioCondsArray, "Name")+1);
                String gene = item.getText(ArrayUtils.findColumn(bioCondsArray, "Homolog Protein")+1);
                genomeToGenes.put(genomeName, gene);
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
            }
        } else if (e.getSource() == btnHelp) {
            HelpPage.helpGeneView(partService);
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

    public Composite getComposite_7() {
        return composite_7;
    }

    public void setComposite_7(Composite composite_7) {
        this.composite_7 = composite_7;
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

    public Text getTxtCutoffLogFC() {
        return txtCutoffLogFC;
    }

    public void setTxtCutoffLogFC(Text txtCutoffLogFC) {
        this.txtCutoffLogFC = txtCutoffLogFC;
    }

    public Text getTxtCutoffPvalue() {
        return txtCutoffPvalue;
    }

    public void setTxtCutoffPvalue(Text txtCutoffPvalue) {
        this.txtCutoffPvalue = txtCutoffPvalue;
    }

    public Label getLblTranscriptomesData() {
        return lblTranscriptomesData;
    }

    public void setLblTranscriptomesData(Label lblTranscriptomesData) {
        this.lblTranscriptomesData = lblTranscriptomesData;
    }

    public Table getTableProteomes() {
        return tableProteomes;
    }

    public void setTableProteomes(Table tableProteomes) {
        this.tableProteomes = tableProteomes;
    }

    public Label getLblOverProteomes() {
        return lblOverProteomes;
    }

    public void setLblOverProteomes(Label lblOverProteomes) {
        this.lblOverProteomes = lblOverProteomes;
    }

    public Label getLblNodiff() {
        return lblNodiff;
    }

    public void setLblNodiff(Label lblNodiff) {
        this.lblNodiff = lblNodiff;
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
