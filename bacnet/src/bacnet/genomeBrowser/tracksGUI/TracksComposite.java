package bacnet.genomeBrowser.tracksGUI;

import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TouchEvent;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import bacnet.Database;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.genomeBrowser.CompositeNTermInfo;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.genomeBrowser.core.Track;
import bacnet.genomeBrowser.core.Track.DisplayType;
import bacnet.genomeBrowser.dialog.AddProteomicsDataDialog;
import bacnet.genomeBrowser.dialog.AddTranscriptomicsDataDialog;
import bacnet.genomeBrowser.dialog.LegendDialog;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.sequenceTools.GeneView;
import bacnet.sequenceTools.SrnaView;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.FileUtils;
import bacnet.views.HelpPage;

/**
 * In TracksComposite both <code>TrackCanvasData</code> and <code>TrackCanvasGenome</code> will be
 * displayed. It is the whole genbome viewer canvas
 * 
 * @author christophebecavin
 *
 */
public class TracksComposite extends Composite implements SelectionListener, MouseListener {

    /**
     * 
     */
    private static final long serialVersionUID = -1957427699848660677L;

    private String parentViewId = "";

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;

    private EPartService partService;
    private Shell shell;

    /*
     * Datasets and tracks
     */
    private Track track;
    private final TrackCanvasData canvasData;
    private final TrackCanvasGenome canvasGenome;
    /**
     * 
     * Selected NTerm
     */
    private NTerm nTerm;

    /**
     * MassSpecData where the selected NTerm is
     */
    private NTermData massSpecData;

    private boolean transcriptome = true;

    private final GridLayout showInfo;
    private final GridLayout hideInfo;
    private final GridData gridDataCmpInfo;

    private final Slider sliderHbar;
    private final Slider sliderVBar;
    private final Button btnLegendManagement;
    private final Composite cmpNTermInfo;
    private final Table tableNtermInfo;
    private final Button btnSequence;
    private final Button btnZoomInVertical;
    private final Button btnZoomOutVertical;
    private final Button btnAddProteomicsData;
    private final Button btnSaveDataSelection;
    private final Button btnLoadData;
    private final Button btnHelp;
    private final Button btnZoomIn;
    private final Button btnZoomOut;
    private final Button btnAddTranscriptomicsData;
    private Combo comboTypeDisplay;
    private Combo comboAbsoluteRelative;
    private Composite compositeAddData;
    private Composite compositeTypeDisplay;

    /*
     * ********* NTerm related
     */
    private final Button btnPreviousnterm;
    private final Button btnNextnterm;
    private final Combo comboNTermFilter;
    private Button btnHideNterminomicsPanel;
    private Combo comboNTermData;
    private CompositeNTermInfo compositeNterm;
    private Composite compositeGenome_1;
    private Composite compositeChromosome;
    private Combo comboChromosome;
    private Label lblTutorial;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public TracksComposite(Composite parent, int style, boolean transcriptome, EPartService partService, Shell shell) {
        super(parent, style);
        this.partService = partService;
        this.shell = shell;
        this.transcriptome = transcriptome;

        showInfo = new GridLayout(2, false);
        hideInfo = new GridLayout(1, false);
        setLayout(showInfo);

        cmpNTermInfo = new Composite(this, SWT.NONE);
        gridDataCmpInfo = new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 2);
        cmpNTermInfo.setLayoutData(gridDataCmpInfo);
        cmpNTermInfo.setLayout(new GridLayout(1, false));
        Label lblSelectTypeOf = new Label(cmpNTermInfo, SWT.NONE);
        lblSelectTypeOf.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblSelectTypeOf.setSize(214, 15);
        lblSelectTypeOf.setText("Select proteomics data to display");

        Composite composite_2 = new Composite(cmpNTermInfo, SWT.BORDER);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        // btnAnimate = new Button(composite_2, SWT.NONE);
        // btnAnimate.setText("Animate");
        // btnAnimate.addSelectionListener(this);

        btnHideNterminomicsPanel = new Button(composite_2, SWT.NONE);
        btnHideNterminomicsPanel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        btnHideNterminomicsPanel.setText("Hide NTerminomics panel");

        comboNTermData = new Combo(composite_2, SWT.NONE);
        comboNTermData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

        comboNTermFilter = new Combo(composite_2, SWT.NONE);
        comboNTermFilter.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        comboNTermFilter.setSize(91, 23);
        comboNTermFilter.addSelectionListener(this);
        comboNTermFilter.addSelectionListener(this);
        comboNTermData.addSelectionListener(this);
        btnHideNterminomicsPanel.addSelectionListener(this);

        Label lblSelectedPeptideProperties = new Label(cmpNTermInfo, SWT.NONE);
        lblSelectedPeptideProperties.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblSelectedPeptideProperties.setText("Properties of selected peptide");

        {
            /*
             * Need to be commented to be able to Use Windows Builder!
             */
            // compositeNterm = new CompositeNTermInfo(cmpNTermInfo, SWT.NONE | SWT.BORDER);
            // compositeNterm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
            // 1, 1));
        }

        Composite cmpSearchNTerm = new Composite(cmpNTermInfo, SWT.BORDER);
        cmpSearchNTerm.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        cmpSearchNTerm.setSize(300, 59);
        cmpSearchNTerm.setLayout(new GridLayout(2, false));

        btnPreviousnterm = new Button(cmpSearchNTerm, SWT.NONE);
        btnPreviousnterm.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        btnPreviousnterm.setImage(ResourceManager.getPluginImage("bacnet", "icons/remove.bmp"));
        btnPreviousnterm.addSelectionListener(this);
        btnNextnterm = new Button(cmpSearchNTerm, SWT.NONE);
        btnNextnterm.setImage(ResourceManager.getPluginImage("bacnet", "icons/add.bmp"));
        btnNextnterm.addSelectionListener(this);

        Label lblPossibleAssiociatedPeptides = new Label(cmpNTermInfo, SWT.NONE);
        lblPossibleAssiociatedPeptides.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblPossibleAssiociatedPeptides.setSize(133, 15);
        lblPossibleAssiociatedPeptides.setText("Group of similar peptides");

        tableNtermInfo = new Table(cmpNTermInfo, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd_table = new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 1);
        gd_table.widthHint = 250;
        tableNtermInfo.setLayoutData(gd_table);
        tableNtermInfo.setSize(250, 295);

        Composite compositeGenome;
        compositeGenome_1 = new Composite(this, SWT.NONE);
        compositeGenome_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        compositeGenome_1.setLayout(new GridLayout(2, false));

        Composite compositeSettings = new Composite(compositeGenome_1, SWT.BORDER);
        compositeSettings.setLayout(new GridLayout(5, false));
        compositeSettings.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Composite composite_5 = new Composite(compositeSettings, SWT.NONE);
        composite_5.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
        composite_5.setLayout(new GridLayout(1, false));
        composite_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        lblTutorial = new Label(composite_5, SWT.WRAP);
        GridData gd_lblTutorial = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
        gd_lblTutorial.widthHint = 150;
        lblTutorial.setLayoutData(gd_lblTutorial);
        lblTutorial.setText(
                "Left-click: Position in BP\nRight-click: Center view\nDouble-click: Display Gene/RNA Information");
        lblTutorial.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
        lblTutorial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnHelp = new Button(composite_5, SWT.NONE);
        btnHelp.setToolTipText("How to use Genome viewer ?");
        btnHelp.setImage(ResourceManager.getPluginImage("bacnet", "icons/help.png"));
        btnHelp.addSelectionListener(this);

        Composite composite_1 = new Composite(compositeSettings, SWT.BORDER);
        composite_1.setLayout(new GridLayout(7, false));
        composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Composite composite = new Composite(composite_1, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
        composite.setLayout(new GridLayout(2, false));
        composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Label lblSearch = new Label(composite, SWT.NONE);
        lblSearch.setAlignment(SWT.RIGHT);
        lblSearch.setText("Search:");
        lblSearch.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Text txtSearch = new Text(composite, SWT.BORDER);
        GridData gd_txtSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtSearch.widthHint = 83;
        txtSearch.setLayoutData(gd_txtSearch);
        txtSearch.setToolTipText("Search a genome element, or type a base pair to reach");
        txtSearch.addSelectionListener(this);
        txtSearch.addKeyListener(new KeyListener() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == 16777296 || e.keyCode == 13) {
                    System.out.println("Search for " + txtSearch.getText());
                    if (track.search(txtSearch.getText())) {
                        moveHorizontally(track.getDisplayRegion().getMiddleH());
                        redrawAllCanvas();
                        setHorizontalBarProperties();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {}
        });

        Label lblHorizontal = new Label(composite_1, SWT.NONE);
        lblHorizontal.setText("Genome");
        lblHorizontal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnZoomIn = new Button(composite_1, SWT.NONE);
        btnZoomIn.setToolTipText("Zoom In horizontally");
        btnZoomIn.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/zoomIN.bmp"));
        btnZoomOut = new Button(composite_1, SWT.NONE);
        btnZoomOut.setToolTipText("Zoom Out horizontally");
        btnZoomOut.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/zoomOUT.bmp"));

        btnZoomOut.addSelectionListener(this);

        btnSequence = new Button(composite_1, SWT.NONE);
        btnSequence.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
        btnSequence.setToolTipText("Display DNA and Amino acid sequence");
        btnSequence.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/sequence.bmp"));
        btnSequence.addSelectionListener(this);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);

        Label lblVertical = new Label(composite_1, SWT.NONE);
        lblVertical.setText("Height");
        lblVertical.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnZoomInVertical = new Button(composite_1, SWT.NONE);
        btnZoomInVertical.setToolTipText("Zoom In vertically");
        btnZoomInVertical.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/zoomIN.bmp"));

        btnZoomOutVertical = new Button(composite_1, SWT.NONE);
        btnZoomOutVertical.setToolTipText("Zoom Out vertically");
        btnZoomOutVertical.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/zoomOUT.bmp"));
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);

        compositeChromosome = new Composite(composite_1, SWT.NONE);
        compositeChromosome.setLayout(new GridLayout(2, false));
        compositeChromosome.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 1));
        compositeChromosome.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Label lblChromosome = new Label(compositeChromosome, SWT.NONE);
        lblChromosome.setText("Chromosome");
        lblChromosome.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        comboChromosome = new Combo(compositeChromosome, SWT.NONE);
        GridData gd_comboChromosome = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_comboChromosome.widthHint = 205;
        comboChromosome.setLayoutData(gd_comboChromosome);
        comboChromosome.addSelectionListener(this);
        btnZoomOutVertical.addSelectionListener(this);
        btnZoomInVertical.addSelectionListener(this);
        btnZoomIn.addSelectionListener(this);

        Composite composite_4 = new Composite(compositeSettings, SWT.NONE);
        composite_4.setLayout(new GridLayout(1, false));
        composite_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        btnLegendManagement = new Button(composite_4, SWT.NONE);
        btnLegendManagement.setText("Legend and Data Management");
        btnLegendManagement.setToolTipText("Display data legend");
        btnLegendManagement.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/legend.bmp"));
        btnLegendManagement.addSelectionListener(this);

        btnSaveDataSelection = new Button(composite_4, SWT.NONE);
        btnSaveDataSelection.setText("Save data selection");
        btnSaveDataSelection.addSelectionListener(this);
        btnSaveDataSelection.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
        btnLoadData = new Button(composite_4, SWT.NONE);
        btnLoadData.setText("Load data selection");
        btnLoadData.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txtload.bmp"));
        btnLoadData.addSelectionListener(this);

        compositeAddData = new Composite(compositeSettings, SWT.NONE);
        compositeAddData.setLayout(new GridLayout(1, false));
        compositeAddData.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnAddTranscriptomicsData = new Button(compositeAddData, SWT.NONE);
        btnAddTranscriptomicsData.setText("AddTranscriptomics Data");
        btnAddTranscriptomicsData.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/hideData.bmp"));
        btnAddTranscriptomicsData.setToolTipText("Choose transcriptomics data to display");

        btnAddProteomicsData = new Button(compositeAddData, SWT.NONE);
        btnAddProteomicsData.setText("Add Proteomics Data");
        btnAddProteomicsData.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/hideData.bmp"));
        btnAddProteomicsData.addSelectionListener(this);
        btnAddProteomicsData.setToolTipText("Choose proteomics data to display");
        btnAddTranscriptomicsData.addSelectionListener(this);

        compositeTypeDisplay = new Composite(compositeSettings, SWT.NONE);
        compositeTypeDisplay.setLayout(new GridLayout(1, false));
        compositeTypeDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        comboAbsoluteRelative = new Combo(compositeTypeDisplay, SWT.NONE);
        comboAbsoluteRelative.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboAbsoluteRelative.add("Absolute expression data");
        comboAbsoluteRelative.add("Relative expression data");
        comboAbsoluteRelative.select(0);
        comboAbsoluteRelative.addSelectionListener(this);

        comboTypeDisplay = new Combo(compositeTypeDisplay, SWT.NONE);
        comboTypeDisplay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboTypeDisplay.add("Display by BioCondition");
        comboTypeDisplay.add("Display by Data");
        comboTypeDisplay.add("Overlay all");

        comboTypeDisplay.addSelectionListener(this);

        new Label(compositeGenome_1, SWT.NONE);

        compositeGenome = new Composite(compositeGenome_1, SWT.NONE);
        compositeGenome.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        compositeGenome.setLayout(new GridLayout(2, false));

        canvasData = new TrackCanvasData(compositeGenome, SWT.BORDER, this);
        canvasData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        canvasData.setLayout(new GridLayout(1, false));
        canvasData.addMouseListener(this);
        canvasData.addTouchListener(new TouchListener() {

            /**
             * 
             */
            private static final long serialVersionUID = 3717918891571350209L;

            @Override
            public void touch(TouchEvent e) {
                // TODO Auto-generated method stub
                System.out.println("Oh yeah touch me!!!");
            }
        });
        canvasData.addGestureListener(new GestureListener() {

            /**
             * 
             */
            private static final long serialVersionUID = 8814918800437499321L;

            @Override
            public void gesture(GestureEvent e) {
                // TODO Auto-generated method stub
                System.out.println("Please make a gesture!!");
            }
        });

        sliderVBar = new Slider(compositeGenome, SWT.VERTICAL);
        sliderVBar.setEnabled(false);
        sliderVBar.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
        sliderVBar.addMouseListener(this);

        canvasGenome = new TrackCanvasGenome(compositeGenome, SWT.BORDER);
        canvasGenome.setLayout(new GridLayout(1, false));
        GridData gd_canvasGenome = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
        gd_canvasGenome.heightHint = 120;
        canvasGenome.setLayoutData(gd_canvasGenome);
        canvasGenome.addMouseListener(this);
        new Label(compositeGenome, SWT.NONE);
        //
        //
        sliderHbar = new Slider(compositeGenome, SWT.BORDER);
        sliderHbar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
        sliderHbar.addMouseListener(this);
        sliderHbar.addSelectionListener(this);
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome_1, SWT.NONE);

        // initNTermInfo();

    }

    public void setTrack(Track track) {
        boolean nterm = false;
        if (track.getDatas().getDataNames().contains(NTerm.NTERM_PROJECT_BHI))
            nterm = true;

        if (!nterm) {
            hideInfoPanel();
        }

        this.track = track;
        if (transcriptome) {
            if (track.getDisplayType() == DisplayType.OVERLAY)
                comboTypeDisplay.select(2);
            if (track.getDisplayType() == DisplayType.BIOCOND)
                comboTypeDisplay.select(0);
            if (track.getDisplayType() == DisplayType.DATA)
                comboTypeDisplay.select(1);
        }
        canvasData.setTrack(this.track);
        canvasGenome.setTrack(this.track);

        if (!track.getDatas().isDisplayAbsoluteValue()) {
            this.getComboAbsoluteRelative().select(1);
        }

        updateComboChromosome();

        setHorizontalBarProperties();
        setVerticalBarProperties();
        if (track.getDatas().getNTermDatas().size() != 0) {
            for (NTermData data : track.getDatas().getNTermDatas()) {
                comboNTermData.add(data.getName());
            }
            comboNTermData.select(0);
            massSpecData = track.getDatas().getNTermDatas().get(0);
            int bpPosition = track.getDisplayRegion().getMiddleH();
            nTerm = massSpecData.findPreviousNterm(bpPosition,
                    comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
            setNTermInfo();
            if (nTerm.isStrand())
                moveHorizontally(nTerm.getBegin());
            else
                moveHorizontally(nTerm.getEnd());
            canvasData.setnTermHighlight(nTerm);
            canvasData.setHeightPix(canvasData.getHeightPix() * 2);
            setHorizontalBarProperties();
            setVerticalBarProperties();
        }
        redrawAllCanvas();

        /*
         * Depend on the type of website
         */
        if (Database.getInstance().getProjectName() == Database.CRISPRGO_PROJECT) {
            btnAddProteomicsData.dispose();
            btnAddTranscriptomicsData.dispose();
            comboAbsoluteRelative.dispose();
            comboTypeDisplay.dispose();
        }

    }

    /**
     * Update the Combo for the chromosomes
     */
    private void updateComboChromosome() {
        if (!comboChromosome.isDisposed()) {
            comboChromosome.removeAll();
            comboChromosome.redraw();
            Genome genome = Genome.loadGenome(track.getGenomeName());
            for (String chromoID : genome.getChromosomes().keySet()) {
                Chromosome chromo = genome.getChromosomes().get(chromoID);
                comboChromosome.add(chromo.getChromosomeNumber() + " - " + chromoID);
            }
            System.out.println("ChromoID: " + track.getChromosomeID());
            comboChromosome.select(comboChromosome
                    .indexOf(track.getChromosome().getChromosomeNumber() + " - " + track.getChromosomeID()));
        }
    }

    /**
     * Depending on <code>Track</code> parameters (<code>Region</code> and <code>Zoom</code>) fix
     * properties of HorizontalBar
     */
    public void setHorizontalBarProperties() {
        if (track.getZoom().getZoomPosition() == 1)
            sliderHbar.setEnabled(false);
        else {
            int ratio = 30;
            int minimum = 0;
            int maximum = 0;
            int increment = 0;
            int pageIncrement = 0;
            int thumb = 0;
            int selection = 0;

            /*
             * Update data
             */
            minimum = 0;
            increment = track.getDisplayRegion().getWidth() / ratio;
            pageIncrement = track.getDisplayRegion().getWidth() / ratio;
            thumb = 100;
            if (track.getDisplayRegion().getWidth() / ratio > 100000) {
                // System.out.println("fix thumb");
                thumb = track.getDisplayRegion().getWidth() / ratio;
            }
            if (thumb < 1000) {
                thumb = 1000;
            }
            maximum = track.getDataRegion().getWidth() + thumb;
            selection = track.getDisplayRegion().getMiddleH();

            /*
             * Display info
             */
            // System.out.println("min: "+minimum+" max: "+maximum+" width:
            // "+track.getDisplayRegion().getWidth()+" pos: "+selection);
            // System.out.println("total bins:
            // "+(track.getDataRegion().getWidth()/track.getDisplayRegion().getWidth())/30);
            // System.out.println("ratio: "+ratio+" increment: "+increment+" thumb:
            // "+thumb+" pos: "+selection);
            // System.out.println((increment*ratio)*thumb);
            // System.out.println((increment*ratio));

            /*
             * Set data
             */
            sliderHbar.setEnabled(true);
            sliderHbar.setMinimum(minimum);
            sliderHbar.setIncrement(increment);
            sliderHbar.setPageIncrement(pageIncrement);
            sliderHbar.setMaximum(maximum);
            sliderHbar.setThumb(thumb);
            sliderHbar.setSelection(selection);
        }
    }

    /**
     * Depending on <code>CanvasData</code> parameters fix properties of VerticalBar
     */
    public void setVerticalBarProperties() {
        // System.out.println("canvas size: "+canvasData.getSize().y+" -heightpix:
        // "+canvasData.getHeightPix());
        if (canvasData.getSize().y < canvasData.getHeightPix()) {
            // System.out.println("Set slider");
            sliderVBar.setEnabled(true);
            sliderVBar.setMinimum(0);
            int length = (canvasData.getHeightPix() - canvasData.getSize().y) + 2;
            sliderVBar.setMaximum(length + length / 10);
            sliderVBar.setIncrement(length / 10);
            sliderVBar.setThumb(length / 10);
            sliderVBar.setPageIncrement(length / 10);
        } else {
            sliderVBar.setEnabled(false);
        }
    }

    /**
     * Update cmbDataSelection, comboNTermFilter
     */
    private void initNTermInfo() {
        comboNTermFilter.removeAll();
        for (String filter : NTermData.TYPE_OVERLAPS) {
            if (!filter.equals("uncategorized")) {
                comboNTermFilter.add(filter);
            }
        }
        comboNTermFilter.add("All");
        comboNTermFilter.select(comboNTermFilter.indexOf("All"));
        canvasData.setnTermTypeOverlap("All");
        nTerm = null;
        canvasData.setnTermHighlight(null);
    }

    /**
     * Depending of NTerm selected, update info in the Table
     */
    private void setNTermInfo() {
        // System.out.println(compositeNterm);
        compositeNterm.setNTermInfo(nTerm, massSpecData);
        ArrayList<NTerm> nTerms = massSpecData.getTisMap().get(nTerm).getnTerms();
        tableNtermInfo.removeAll();
        tableNtermInfo.setHeaderVisible(true);
        tableNtermInfo.setLinesVisible(true);

        String[] titles = {"Modif", " # ", "Sequence", "ID"};
        for (int i = 0; i < titles.length; i++) {
            TableColumn column = new TableColumn(tableNtermInfo, SWT.NONE);
            column.setText(titles[i]);
        }
        for (int i = 0; i < nTerms.size(); i++) {
            NTerm nTermTemp = nTerms.get(i);
            TableItem item = new TableItem(tableNtermInfo, SWT.NONE);
            item.setText(0, nTermTemp.getTypeModif() + "");
            item.setText(1, nTermTemp.getSpectra() + "");
            item.setText(2, nTermTemp.getSequencePeptide());
            item.setText(3, nTermTemp.getName());
        }
        for (int i = 0; i < titles.length; i++) {
            tableNtermInfo.getColumn(i).pack();
        }
        tableNtermInfo.update();
        tableNtermInfo.redraw();

        /*
         * Select NTerm in the Table
         */
        for (int i = 0; i < tableNtermInfo.getItemCount(); i++) {
            String item = tableNtermInfo.getItem(i).getText(3);
            // System.out.println(item);
            if (item.equals(nTerm.getName())) {
                tableNtermInfo.select(i);
            }
        }
    }

    /**
     * Move horizontally the tracks
     */
    public void moveHorizontally(int position) {
        track.moveHorizontally(position);
    }

    /**
     * Hide the info Panel showing NTerm data
     */
    public void hideInfoPanel() {
        setLayout(hideInfo);
        gridDataCmpInfo.exclude = true;
        cmpNTermInfo.setVisible(false);
        getShell().layout(true, true);
    }

    /**
     * Show the info Panel showing NTerm data
     */
    public void showInfoPanel() {
        if (!cmpNTermInfo.isVisible()) {
            setLayout(showInfo);
            gridDataCmpInfo.exclude = false;
            cmpNTermInfo.setVisible(true);
            getShell().layout(true, true);
        }
    }

    /**
     * Display a specific element of the genome only by data.getDisplay() to false, except in the region
     * of the genomeElement
     * 
     * @param text
     */
    public void displaySpecificRegion(String text) {
        Sequence seq = track.getChromosome().getAllElements().get(text);
        if (seq != null) {
            for (int i = 0; i < track.getDatas().getDisplay().length; i++)
                track.getDatas().getDisplay()[i] = false;
            int[] positions = {seq.getBegin(), seq.getEnd()};
            for (int i = positions[0] - 1; i < positions[1] + 2; i++) {
                track.getDatas().getDisplay()[i] = true;
            }
            canvasData.redraw();
            canvasGenome.redraw();
            setHorizontalBarProperties();
        }
    }

    /**
     * Search a specific element and center the view on it
     * 
     * @param text
     * @return
     */
    public boolean search(String text) {
        try {
            int position = Integer.parseInt(text);
            moveHorizontally(position);
            canvasData.redraw();
            canvasGenome.redraw();
            setHorizontalBarProperties();
            return true;
        } catch (Exception e) {
            // go through annotation table and search for an element
            System.out.println("Search: " + text);
            if (text.contains("peptide")) {
                if (massSpecData != null) {
                    nTerm = massSpecData.getElements().get(text);
                    canvasData.setnTermHighlight(nTerm);
                    search(nTerm.getBegin() + "");
                    setNTermInfo();
                    return true;
                }
            } else {
                text = text.toUpperCase();
                for (String name : track.getChromosome().getAllElements().keySet()) {
                    // System.out.println(name);
                    String nameTemp = name.toUpperCase();
                    if (nameTemp.contains(text)) {
                        Sequence seq = track.getChromosome().getAllElements().get(name);
                        if (seq.isStrand())
                            moveHorizontally(seq.getBegin());
                        else
                            moveHorizontally(seq.getEnd());
                        canvasData.redraw();
                        canvasGenome.redraw();
                        setHorizontalBarProperties();
                        return true;
                    } else if (track.getChromosome().getAllElements().get(name) instanceof Gene) {
                        Gene gene = (Gene) track.getChromosome().getAllElements().get(name);
                        String geneName = gene.getName().toUpperCase();
                        if (geneName.contains(text)) {
                            Sequence seq = track.getChromosome().getAllElements().get(name);
                            if (seq.isStrand())
                                moveHorizontally(seq.getBegin());
                            else
                                moveHorizontally(seq.getEnd());
                            canvasData.redraw();
                            canvasGenome.redraw();
                            setHorizontalBarProperties();
                            return true;
                        }
                    }
                }
            }
            System.out.println("not found");
        }
        return false;
    }

    public void redrawAllCanvas() {
        canvasData.redraw();
        canvasGenome.redraw();
        pushState();
    }

    /**
     * Push states to change url
     */
    public void pushState() {
        HashMap<String, String> parameters = new HashMap<>();
        // Genome
        parameters.put(NavigationManagement.GENOME, track.getGenomeName());
        parameters.put(NavigationManagement.CHROMO, track.getChromosomeID());
        // bioConditions
        String bioConditions = "";
        for (String bioCondition : track.getDatas().getBioConditionHashMaps().keySet()) {
            if (bioCondition.contains(" vs ")) {
                bioConditions += BioCondition.parseName(bioCondition)[0] + ":";
            } else {
                bioConditions += bioCondition + ":";
            }
        }
        parameters.put(NavigationManagement.LIST, bioConditions);
        // Not displayed
        if (track.getDatas().getDataNOTDisplayed().size() != 0) {
            String notInclude = "";
            for (String data : track.getDatas().getDataNOTDisplayed()) {
                notInclude += data + ":";
            }
            parameters.put(NavigationManagement.LISTNOTINCLUDE, notInclude);
        }

        // Style
        String style = track.getDisplayType() + ":" + track.getDatas().isDisplayAbsoluteValue() + ":"
                + track.getDisplayRegion().getMiddleH() + ":" + track.getZoom().getZoomPosition() + ":"
                + track.isDisplaySequence();
        parameters.put(NavigationManagement.STYLE, style);
        // push state
        NavigationManagement.pushStateView(this.getParentViewId(), parameters);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnZoomOut) {
            track.zoom(false);
            if (track.getZoom().getZoomPosition() < 27)
                track.setDisplaySequence(false);
            redrawAllCanvas();
            setHorizontalBarProperties();
        } else if (e.getSource() == btnZoomIn) {
            track.zoom(true);
            redrawAllCanvas();
            setHorizontalBarProperties();
        } else if (e.getSource() == btnZoomInVertical) {
            if (track.getZoom().getZoomHeightPosition() < track.getZoom().getZoomHeightMax()) {
                track.getZoom().setZoomHeightPosition(track.getZoom().getZoomHeightPosition() + 1);
            }
            canvasData.setZoomVertical((double) track.getZoom().getZoomHeightPosition() / 5);
            redrawAllCanvas();
            setVerticalBarProperties();
        } else if (e.getSource() == btnZoomOutVertical) {
            if (1 < track.getZoom().getZoomHeightPosition()) {
                track.getZoom().setZoomHeightPosition(track.getZoom().getZoomHeightPosition() - 1);
            }
            canvasData.setZoomVertical((double) track.getZoom().getZoomHeightPosition() / 5);
            redrawAllCanvas();
            setVerticalBarProperties();
        } else if (e.getSource() == comboChromosome) {
            String chromoID = comboChromosome.getItem(comboChromosome.getSelectionIndex()).split(" - ")[1];
            System.out.println("New chromo selected:" + chromoID);
            track.setNewChromosome(chromoID);
            setTrack(track);
        } else if (e.getSource() == btnSequence) {
            if (track.isDisplaySequence()) {
                track.setDisplaySequence(false);
            } else {
                track.setDisplaySequence(true);
                while (track.getZoom().getZoomPosition() != track.getZoom().getZoomNumber()) {
                    track.zoom(true);
                }
            }
            redrawAllCanvas();
            setHorizontalBarProperties();
        } else if (e.getSource() == comboNTermFilter) {
            String typeOverlap = comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex());
            canvasData.setnTermTypeOverlap(typeOverlap);
            if (track.getDatas().getNTermDatas().size() != 0) {
                nTerm = massSpecData.getPreviousNterm(nTerm,
                        comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
                setNTermInfo();
                if (nTerm.isStrand())
                    moveHorizontally(nTerm.getBegin());
                else
                    moveHorizontally(nTerm.getEnd());
                canvasData.setnTermHighlight(nTerm);
            }
            canvasData.setnTermHighlight(nTerm);
            setHorizontalBarProperties();
            redrawAllCanvas();
        } else if (e.getSource() == comboTypeDisplay) {
            if (comboTypeDisplay.getSelectionIndex() == 0) {
                track.setDisplayType(DisplayType.BIOCOND);
                track.getDatas().setDataColors();
            } else if (comboTypeDisplay.getSelectionIndex() == 1) {
                track.setDisplayType(DisplayType.DATA);
                track.getDatas().setDataColors();
            } else {
                track.setDisplayType(DisplayType.OVERLAY);
                track.getDatas().setDataColors();
            }
            canvasData.setDecaySliderVBar(0);
            redrawAllCanvas();
            setVerticalBarProperties();
        } else if (e.getSource() == comboAbsoluteRelative) {
            if (comboAbsoluteRelative.getSelectionIndex() == 0 && !track.getDatas().isDisplayAbsoluteValue()) {
                System.out.println("Select absolute value display");
                track.getDatas().setDisplayAbsoluteValue(true);
                track.getDatas().relativeTOabsoluteValue();
                setTrack(track);
            } else if (comboAbsoluteRelative.getSelectionIndex() == 1 && track.getDatas().isDisplayAbsoluteValue()) {
                System.out.println("Select relative value display");
                track.getDatas().setDisplayAbsoluteValue(false);
                track.getDatas().absoluteTOrelativeValue();
                setTrack(track);
            }
        } else if (e.getSource() == btnAddTranscriptomicsData) {
            AddTranscriptomicsDataDialog dialog = new AddTranscriptomicsDataDialog(shell, track);
            int ok = dialog.open();
            if (ok == Status.OK) {
                if (track.getDatas().isDisplayAbsoluteValue())
                    comboAbsoluteRelative.select(0);
                else
                    comboAbsoluteRelative.select(1);
                initNTermInfo();
                setTrack(track);
            }
        } else if (e.getSource() == btnAddProteomicsData) {
            AddProteomicsDataDialog dialog = new AddProteomicsDataDialog(shell, track);
            int ok = dialog.open();
            if (ok == Status.OK) {
                if (track.getDatas().isDisplayAbsoluteValue())
                    comboAbsoluteRelative.select(0);
                else
                    comboAbsoluteRelative.select(1);
                initNTermInfo();
                setTrack(track);
            }
        } else if (e.getSource() == btnLegendManagement) {
            LegendDialog dialog = new LegendDialog(shell, track);
            int ok = dialog.open();
            if (ok == Status.OK) {
                setTrack(track);
            }
        } else if (e.getSource() == btnSaveDataSelection) {
            String text = track.savetoTextFile();
            String textHTML = text.replaceAll("\n", "<br>");
            SaveFileUtils.saveTextFile("GenomeViewer-Session.gview", text, true, "", textHTML, partService, shell);
        } else if (e.getSource() == btnLoadData) {
            try {
                FileDialog fd = new FileDialog(shell, SWT.OPEN);
                fd.setText("Open .gview file of your previous session");
                String fileName = fd.open();
                if (fileName != null) {
                    String textTrack = FileUtils.readText(fileName);
                    GenomeTranscriptomeView.displayBioConditionsFromText(partService, textTrack);
                }
            } catch (Exception e1) {
                System.out.println("Cannot read the list of data");
            }
        } else if (e.getSource() == btnHideNterminomicsPanel) {
            hideInfoPanel();
        } else if (e.getSource() == btnPreviousnterm) {
            if (nTerm == null) {
                int bpPosition = track.getDisplayRegion().getMiddleH();
                nTerm = massSpecData.findPreviousNterm(bpPosition,
                        comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
                setNTermInfo();
                if (nTerm.isStrand())
                    moveHorizontally(nTerm.getBegin());
                else
                    moveHorizontally(nTerm.getEnd());
                canvasData.setnTermHighlight(nTerm);
                redrawAllCanvas();
                setHorizontalBarProperties();
            } else {
                nTerm = massSpecData.getPreviousNterm(nTerm,
                        comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
                setNTermInfo();
                if (nTerm.isStrand())
                    moveHorizontally(nTerm.getBegin());
                else
                    moveHorizontally(nTerm.getEnd());
                canvasData.setnTermHighlight(nTerm);
                redrawAllCanvas();
                setHorizontalBarProperties();
            }
        } else if (e.getSource() == btnNextnterm) {
            if (nTerm == null) {
                int bpPosition = track.getDisplayRegion().getMiddleH();
                nTerm = massSpecData.findNextNterm(bpPosition,
                        comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
                setNTermInfo();
                if (nTerm.isStrand())
                    moveHorizontally(nTerm.getBegin());
                else
                    moveHorizontally(nTerm.getEnd());
                canvasData.setnTermHighlight(nTerm);
                redrawAllCanvas();
                setHorizontalBarProperties();
            } else {
                nTerm = massSpecData.getNextNterm(nTerm,
                        comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
                setNTermInfo();
                if (nTerm.isStrand())
                    moveHorizontally(nTerm.getBegin());
                else
                    moveHorizontally(nTerm.getEnd());
                canvasData.setnTermHighlight(nTerm);
                redrawAllCanvas();
                setHorizontalBarProperties();
            }
        } else if (e.getSource() == comboNTermData) {
            String nTermName = comboNTermData.getItem(comboNTermData.getSelectionIndex());
            massSpecData = track.getDatas().getNTermDatas(nTermName).get(0);
            canvasData.setMassSpecData(massSpecData);
            int bpPosition = track.getDisplayRegion().getMiddleH();
            nTerm = massSpecData.findNextNterm(bpPosition,
                    comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
            setNTermInfo();
            if (nTerm.isStrand())
                moveHorizontally(nTerm.getBegin());
            else
                moveHorizontally(nTerm.getEnd());
            canvasData.setnTermHighlight(nTerm);
            redrawAllCanvas();
            setHorizontalBarProperties();
        } else if (e.getSource() == btnHelp) {
            HelpPage.helpGenomeViewer(partService);
        }
        // else if(e.getSource()==btnAnimate){
        // int initpos = sliderHbar.getSelection();
        // moveHorizontally(initpos+100);
        // redrawAllCanvas();
        // setHorizontalBarProperties();
        //
        //
        //
        // }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        if (e.getSource() == canvasData) {
            if (massSpecData != null) {
                int basepair = canvasData.convertXtoBP(e.x);
                NTerm nTermDown = massSpecData.findNextNterm(basepair,
                        comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
                NTerm nTermUp = massSpecData.findPreviousNterm(basepair,
                        comboNTermFilter.getItem(comboNTermFilter.getSelectionIndex()));
                int newPos = basepair;
                if ((nTermDown.getBegin() - basepair) < (basepair - nTermUp.getEnd())) {
                    nTerm = nTermDown;
                    newPos = nTermDown.getBegin();
                } else {
                    nTerm = nTermUp;
                    newPos = nTermUp.getBegin();
                }

                canvasData.setnTermHighlight(nTerm);
                search(newPos + "");
                setNTermInfo();
            }
        } else if (e.getSource() == canvasGenome) {
            /*
             * Mouse is in CanvasGenome
             */
            Object obj = canvasGenome.clickedElement(e);
            if (obj != null) {
                // System.out.println("Double click select "+obj.getClass());
                if (obj instanceof Gene) {
                    Gene gene = (Gene) obj;
                    // System.out.println("Gene: "+gene.getName());
                    GeneView.displayGene(gene, partService);
                } else if (obj instanceof Srna) {
                    Srna sRNA = (Srna) obj;
                    SrnaView.displaySrna(sRNA, partService);
                }
            }
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        // System.out.println("mouse up");

        if (e.button == 1) { // right button
            // System.out.println(e.x+" - "+e.y+" ; data:"+canvasData.getBounds()+" : genome
            // "+canvasGenome.getBounds());
            if (e.getSource() == sliderHbar) {
                // System.out.println(sliderHbar.getSelection()+" "+sliderHbar.getThumb()+"
                // "+sliderHbar.getMaximum()+"
                // "+(sliderHbar.getMaximum()-sliderHbar.getThumb()));
                moveHorizontally(sliderHbar.getSelection());
                redrawAllCanvas();
            } else if (e.getSource() == sliderVBar) {
                canvasData.moveVertically(sliderVBar.getSelection());
                redrawAllCanvas();
            } else if (e.getSource() == canvasData) {
                /*
                 * Mouse is in the CanvasData
                 */
                if (canvasData.isDisplayMouseLine()) {
                    canvasData.setDisplayMouseLine(false);
                    canvasGenome.setDisplayMouseLine(false);
                } else {
                    canvasData.setDisplayMouseLine(true);
                    canvasData.setMouseXPosition(e.x);
                    canvasData.setMouseYPosition(e.y);
                    canvasGenome.setDisplayMouseLine(true);
                    canvasGenome.setMouseXPosition(e.x);
                    canvasGenome.setMouseYPosition(e.y);
                }
                redrawAllCanvas();
            }
        } else if (e.button == 3) { // left button center the position
            int position = canvasData.convertXtoBP(e.x);
            track.search(position + "");
            redrawAllCanvas();
            setHorizontalBarProperties();
        }
    }

    public GridLayout getShowInfo() {
        return showInfo;
    }

    public Combo getComboAbsoluteRelative() {
        return comboAbsoluteRelative;
    }

    public void setComboAbsoluteRelative(Combo comboAbsoluteRelative) {
        this.comboAbsoluteRelative = comboAbsoluteRelative;
    }

    public Button getBtnZoomIn() {
        return btnZoomIn;
    }

    public Button getBtnZoomOut() {
        return btnZoomOut;
    }

    public Slider getSliderHbar() {
        return sliderHbar;
    }

    public Slider getSliderVBar() {
        return sliderVBar;
    }

    public Button getBtnZoomInVertical() {
        return btnZoomInVertical;
    }

    public Button getBtnZoomOutVertical() {
        return btnZoomOutVertical;
    }

    public TrackCanvasData getCanvasData() {
        return canvasData;
    }

    public TrackCanvasGenome getCanvasGenome() {
        return canvasGenome;
    }

    public String getParentViewId() {
        return parentViewId;
    }

    public void setParentViewId(String parentViewId) {
        this.parentViewId = parentViewId;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public void mouseDown(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public Composite getCompositeAddData() {
        return compositeAddData;
    }

    public void setCompositeAddData(Composite compositeAddData) {
        this.compositeAddData = compositeAddData;
    }

    public Composite getCompositeTypeDisplay() {
        return compositeTypeDisplay;
    }

    public void setCompositeTypeDisplay(Composite compositeTypeDisplay) {
        this.compositeTypeDisplay = compositeTypeDisplay;
    }

    public CompositeNTermInfo getCompositeNterm() {
        return compositeNterm;
    }

    public void setCompositeNterm(CompositeNTermInfo compositeNterm) {
        this.compositeNterm = compositeNterm;
    }

    public Track getTrack() {
        return track;
    }

}
