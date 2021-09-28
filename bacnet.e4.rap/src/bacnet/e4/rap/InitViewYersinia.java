package bacnet.e4.rap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Genome.OpenGenomesThread;
import bacnet.expressionAtlas.ProteomicsView;
import bacnet.expressionAtlas.TranscriptomicsView;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.raprcp.NavigationManagement;
import bacnet.sequenceTools.GeneView;
import bacnet.sequenceTools.GenomicsView;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.BasicColor;
import bacnet.utils.FileUtils;
import bacnet.utils.RWTUtils;
import bacnet.views.CoExprNetworkView;

public class InitViewYersinia implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -9052983689419871498L;

    public static final String ID = "bacnet.Yersinia"; //$NON-NLS-1$

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;
    
    private Button btnCO92;
    private Button btnKIM;
    private Button btnCoExpression;
    private Button btnY11;
    private Button btn8081;
    private Button btnYPIII;
    private Button btnIP32953;
    private Button btnCSF007;
    private Button btn91001;
    private Button btnLoadData;
    private Button btnGeneView;
    private Button btnAccessWiki;
    private Link linkPubli;
    private Link linkHUB;
    private Button btnTranscriptomics;
    private Button btnGenomics;
    private Button btnProteomics;
    private Link linkLicenceField;

    @Inject
    EPartService partService;

    @Inject
    EModelService modelService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public InitViewYersinia() {}

    @PostConstruct
    public void createPartControl(Composite parent) {
    	//AppSpecificMethods AppSpecificMethods = new AppSpecificMethods();
    	//AppSpecificMethods.openPasswordDialog(shell);

        //System.out.println("Load InitView");

        focused = true;
        parent.setLayout(new GridLayout(1, false));
        ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Composite composite_Intro = new Composite(composite, SWT.NONE);
        composite_Intro.setLayout(new GridLayout(2, false));
        GridData gd_composite_Intro = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        gd_composite_Intro.widthHint = 850;
        composite_Intro.setLayoutData(gd_composite_Intro);
        // composite_Intro.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        Label lblListeriomicsIsSo = new Label(composite_Intro, SWT.WRAP);
        lblListeriomicsIsSo.setAlignment(SWT.CENTER);
        // lblListeriomicsIsSo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblListeriomicsIsSo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
        // lblListeriomicsIsSo.setData(RWT.MARKUP_ENABLED, Boolean.TRUE );
        // lblListeriomicsIsSo.setText("Systems biology of the model pathogen
        // <i>yersinia</i>");
        // lblListeriomicsIsSo.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
        lblListeriomicsIsSo.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/ToolBar/logoYersiniomics.png"));
        new Label(composite_Intro, SWT.NONE);
        Label lblNewLabel_1 = new Label(composite_Intro, SWT.WRAP);
        RWTUtils.setMarkup(lblNewLabel_1);
        lblNewLabel_1.setAlignment(SWT.CENTER);
        GridData gd_lblNewLabel_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblNewLabel_1.widthHint = 700;
        lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
        lblNewLabel_1.setText(
                "Yersiniomics integrates all complete genomes, transcriptomes and proteomes published for <i>Yersinia</i> "
                        + "species to date. It allows navigating among all these datasets with enriched metadata in a user-friendly format. "
                        + "Use Yersiniomics for deciphering regulatory mechanisms of your genome element of interest.\r");
        // lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblNewLabel_1.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        
        Composite composite_11 = new Composite(composite, SWT.BORDER);
        GridData gd_composite_11 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_composite_11.widthHint = 1500;
        composite_11.setLayoutData(gd_composite_11);
        composite_11.setLayout(new GridLayout(5, false));
        composite_11.setBackground(BasicColor.LIGHT_ONE);

        Composite composite_12 = new Composite(composite_11, SWT.NONE);
        composite_12.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        composite_12.setLayout(new GridLayout(1, false));
        composite_12.setBackground(BasicColor.LIGHT_ONE);
        
        Label lblFastAccessTo = new Label(composite_12, SWT.WRAP);
        GridData gd_lblFastAccessTo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblFastAccessTo.widthHint = 200;
        lblFastAccessTo.setLayoutData(gd_lblFastAccessTo);
        lblFastAccessTo.setAlignment(SWT.CENTER);
        lblFastAccessTo.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
        lblFastAccessTo.setBackground(BasicColor.LIGHT_ONE);
        lblFastAccessTo.setForeground(BasicColor.BLACK);
        lblFastAccessTo.setText("Reference genomes quick access");
        //Label lblImage = new Label(composite_12, SWT.BORDER);
        //lblImage.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/InitPage/genomeView.png"));
        
        Composite composite_1 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_1.heightHint = 115;
        composite_1.setLayoutData(gd_composite_1);
        composite_1.setSize(480, 109);
        composite_1.setLayout(new GridLayout(1, false));
        composite_1.setBackground(BasicColor.LIGHT_ONE);

        //ToolBar toolBar = new ToolBar(composite_1, SWT.FLAT | SWT.RIGHT);
        Label lblPestis = new Label(composite_1, SWT.WRAP);
        RWTUtils.setMarkup(lblPestis);
        lblPestis.setAlignment(SWT.CENTER);
        GridData gd_lblPestis = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblPestis.widthHint = 300;
        lblPestis.setLayoutData(gd_lblPestis);
        lblPestis.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblPestis.setBackground(BasicColor.LIGHT_ONE);
        lblPestis.setForeground(BasicColor.BLACK);
        lblPestis.setText("<i>Yersinia pestis</i>");
        
        btnCO92 = new Button(composite_1, SWT.BORDER);
        btnCO92.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnCO92.setText("CO92");
        btnCO92.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnCO92.setBackground(BasicColor.DARK_ONE);
        //sbtnCO92.setForeground(BasicColor.BLACK);
        btnCO92.addSelectionListener(this);
        
        btnKIM = new Button(composite_1, SWT.BORDER);
        btnKIM.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnKIM.setText("KIM");
        btnKIM.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnKIM.setBackground(BasicColor.DARK_ONE);
        //btnKIM.setForeground(BasicColor.BLACK);
        btnKIM.addSelectionListener(this);
/*
        Label lblCO92 = new Label(composite_1, SWT.WRAP);
        RWTUtils.setMarkup(lblCO92);
        lblCO92.setAlignment(SWT.CENTER);
        GridData gd_lblCO92 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblCO92.widthHint = 300;
        lblCO92.setLayoutData(gd_lblCO92);
        lblCO92.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblCO92.setBackground(BasicColor.LIGHT_ONE);
        lblCO92.setForeground(BasicColor.BLACK);
        lblCO92.setText("View of <i>Yersinia pestis</i> CO92 RNASeq and LC-MS/MS datas or KIM microarray datas");
*/
        Composite composite_23 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_23 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_23.heightHint = 115;
        composite_23.setLayoutData(gd_composite_23);
        composite_23.setLayout(new GridLayout(1, false));
        composite_23.setBackground(BasicColor.LIGHT_ONE);
        composite_23.setForeground(BasicColor.BLACK);
        
        Label lblPseudo = new Label(composite_23, SWT.WRAP);
        RWTUtils.setMarkup(lblPseudo);
        lblPseudo.setAlignment(SWT.CENTER);
        GridData gd_lblPseudo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblPseudo.widthHint = 300;
        lblPseudo.setLayoutData(gd_lblPseudo);
        lblPseudo.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblPseudo.setBackground(BasicColor.LIGHT_ONE);
        lblPseudo.setForeground(BasicColor.BLACK);
        lblPseudo.setText("<i>Yersinia pseudotuberculosis</i>");

        btnIP32953 = new Button(composite_23, SWT.BORDER);
        btnIP32953.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnIP32953.addSelectionListener(this);
        btnIP32953.setText("IP32953");
        btnIP32953.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnIP32953.setBackground(BasicColor.DARK_ONE);

        
        btnYPIII = new Button(composite_23, SWT.BORDER);
        btnYPIII.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnYPIII.addSelectionListener(this);
        btnYPIII.setText("YPIII");
        btnYPIII.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnYPIII.setBackground(BasicColor.DARK_ONE);

/*
        Label lblYPIII = new Label(composite_23, SWT.WRAP);
        RWTUtils.setMarkup(lblYPIII);
        lblYPIII.setAlignment(SWT.CENTER);
        GridData gd_lblYPIII = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblYPIII.widthHint = 300;
        lblYPIII.setLayoutData(gd_lblYPIII);
        lblYPIII.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblYPIII.setBackground(BasicColor.LIGHT_ONE);
        lblYPIII.setForeground(BasicColor.BLACK);
        lblYPIII.setText("View of <i>Yersinia pseudotuberculosis</i> IP32953 RNASeq datas or YPIII microarray datas");
    */    
        Composite composite_9 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_9 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_9.heightHint = 115;
        composite_9.setLayoutData(gd_composite_9);
        composite_9.setLayout(new GridLayout(1, false));
        composite_9.setBackground(BasicColor.LIGHT_ONE);
        composite_9.setForeground(BasicColor.BLACK);
        
        Label lblEntero = new Label(composite_9, SWT.WRAP);
        RWTUtils.setMarkup(lblEntero);
        lblEntero.setAlignment(SWT.CENTER);
        GridData gd_lblEntero = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblEntero.widthHint = 300;
        lblEntero.setLayoutData(gd_lblEntero);
        lblEntero.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblEntero.setBackground(BasicColor.LIGHT_ONE);
        lblEntero.setForeground(BasicColor.BLACK);
        lblEntero.setText("<i>Yersinia enterocolitica</i>");

        btnY11 = new Button(composite_9, SWT.BORDER);
        btnY11.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnY11.addSelectionListener(this);
        btnY11.setText("Y11");
        btnY11.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnY11.setBackground(BasicColor.DARK_ONE);
        
        btn8081 = new Button(composite_9, SWT.BORDER);
        btn8081.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btn8081.addSelectionListener(this);
        btn8081.setText("8081");
        btn8081.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btn8081.setBackground(BasicColor.DARK_ONE);
/*
        Label lblY11 = new Label(composite_9, SWT.WRAP);
        RWTUtils.setMarkup(lblY11);
        lblY11.setAlignment(SWT.CENTER);
        GridData gd_lblY11 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblY11.widthHint = 300;
        lblY11.setLayoutData(gd_lblY11);
        lblY11.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblY11.setBackground(BasicColor.LIGHT_ONE);
        lblY11.setForeground(BasicColor.BLACK);
        lblY11.setText("View of <i>Yersinia enterocolitica</i> Y11 RNASeq datas or 8081 datas");
    */    
        Composite composite_20 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_20 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_9.heightHint = 115;
        composite_20.setLayoutData(gd_composite_20);
        composite_20.setLayout(new GridLayout(1, false));
        composite_20.setBackground(BasicColor.LIGHT_ONE);
        composite_20.setForeground(BasicColor.BLACK);
        
        Label lblRuck = new Label(composite_20, SWT.WRAP);
        RWTUtils.setMarkup(lblRuck);
        lblRuck.setAlignment(SWT.CENTER);
        GridData gd_lblRuck = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblRuck.widthHint = 300;
        lblRuck.setLayoutData(gd_lblRuck);
        lblRuck.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblRuck.setBackground(BasicColor.LIGHT_ONE);
        lblRuck.setForeground(BasicColor.BLACK);
        lblRuck.setText("<i>Yersinia ruckeri</i>");
        
        btnCSF007 = new Button(composite_20, SWT.BORDER);
        btnCSF007.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnCSF007.addSelectionListener(this);
        btnCSF007.setText("QMA0440");
        btnCSF007.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnCSF007.setBackground(BasicColor.DARK_ONE);
        
        Label lblMicr = new Label(composite_20, SWT.WRAP);
        RWTUtils.setMarkup(lblMicr);
        lblMicr.setAlignment(SWT.CENTER);
        GridData gd_lblMicr = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblMicr.widthHint = 300;
        lblMicr.setLayoutData(gd_lblRuck);
        lblMicr.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblMicr.setBackground(BasicColor.LIGHT_ONE);
        lblMicr.setForeground(BasicColor.BLACK);
        lblMicr.setText("<i>Yersinia pestis</i> biovar <i>Microtus</i>");
        
        btn91001 = new Button(composite_20, SWT.BORDER);
        btn91001.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btn91001.addSelectionListener(this);
        btn91001.setText("91001");
        btn91001.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btn91001.setBackground(BasicColor.DARK_ONE);
        /*
        Label lblCSF007 = new Label(composite_20, SWT.WRAP);
        RWTUtils.setMarkup(lblCSF007);
        lblCSF007.setAlignment(SWT.CENTER);
        GridData gd_lblCSF007 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblCSF007.widthHint = 300;
        lblCSF007.setLayoutData(gd_lblCSF007);
        lblCSF007.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblCSF007.setBackground(BasicColor.LIGHT_ONE);
        lblCSF007.setForeground(BasicColor.BLACK);
        lblCSF007.setText("View of <i>Yersinia ruckeri</i> CSF007-82 RNASeq datas");
*/
        Composite composite_6 = new Composite(composite, SWT.BORDER);
        GridData gd_composite_6 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_composite_6.widthHint = 1500;
        composite_6.setLayoutData(gd_composite_6);
        composite_6.setLayout(new GridLayout(5, false));
        composite_6.setBackground(BasicColor.LIGHT_TWO);
        Composite composite_8 = new Composite(composite_6, SWT.NONE);
        composite_8.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        composite_8.setLayout(new GridLayout(1, false));
        composite_8.setBackground(BasicColor.LIGHT_TWO);
        Label lblBrowseOmicsDatasets = new Label(composite_8, SWT.WRAP);
        lblBrowseOmicsDatasets.setAlignment(SWT.CENTER);
        GridData gd_lblBrowseOmicsDatasets = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblBrowseOmicsDatasets.widthHint = 200;
        lblBrowseOmicsDatasets.setLayoutData(gd_lblBrowseOmicsDatasets);
        lblBrowseOmicsDatasets.setText("Browse omics datasets");
        lblBrowseOmicsDatasets.setForeground(BasicColor.BLACK);
        lblBrowseOmicsDatasets.setFont(SWTResourceManager.getTitleFont());
        lblBrowseOmicsDatasets.setBackground(BasicColor.LIGHT_TWO);


        //Label lblImage_3 = new Label(composite_8, SWT.BORDER);
        //lblImage_3.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/InitPage/heatmap.png"));
        Composite composite_15 = new Composite(composite_6, SWT.NONE);
        GridData gd_composite_15 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_15.heightHint = 115;
        composite_15.setLayoutData(gd_composite_15);
        composite_15.setLayout(new GridLayout(1, false));
        composite_15.setBackground(BasicColor.LIGHT_TWO);
        btnGeneView = new Button(composite_15, SWT.BORDER);
        btnGeneView.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        // btnGeneView.setImage(ResourceManager.getPluginImage("bacnet.core",
        // "icons/InitPage/SystemsBio.png"));
        btnGeneView.addSelectionListener(this);
        btnGeneView.setText("Genes information");
        btnGeneView.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnGeneView.setBackground(BasicColor.DARK_TWO);


        Label lblinfo = new Label(composite_15, SWT.WRAP);
        RWTUtils.setMarkup(lblinfo);
        lblinfo.setAlignment(SWT.CENTER);
        lblinfo.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblinfo.setBackground(BasicColor.LIGHT_TWO);
        lblinfo.setForeground(BasicColor.BLACK);

        GridData gd_lblinfo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblinfo.widthHint = 300;
        lblinfo.setLayoutData(gd_lblinfo);
        lblinfo.setText("Access to all information about <i>Yersinia</i> species genes: "
                + "functional annotation, gene conservation, synteny, expression atlas and protein atlas. ");
        
        Composite composite_10 = new Composite(composite_6, SWT.NONE);
        GridData gd_composite_10 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_10.heightHint = 100;
        composite_10.setLayoutData(gd_composite_10);
        composite_10.setLayout(new GridLayout(1, false));
        composite_10.setBackground(BasicColor.LIGHT_TWO);
        btnGenomics = new Button(composite_10, SWT.BORDER);
        btnGenomics.setText("Genomics browser");
        btnGenomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnGenomics.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnGenomics.addSelectionListener(this);
        btnGenomics.setBackground(BasicColor.DARK_TWO);

        Label lblGoThroughAll = new Label(composite_10, SWT.WRAP);
        RWTUtils.setMarkup(lblGoThroughAll);
        lblGoThroughAll.setAlignment(SWT.CENTER);
        GridData gd_lblGoThroughAll = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblGoThroughAll.widthHint = 300;
        lblGoThroughAll.setLayoutData(gd_lblGoThroughAll);
        lblGoThroughAll.setText("Browse all 121 <i>Yersinia</i> complete genomes available on Yersiniomics."
                + " Visualize strain relationship in a phylogenomic tree. Access to all their annotated genome elements.");
        lblGoThroughAll.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblGoThroughAll.setBackground(BasicColor.LIGHT_TWO);
        lblGoThroughAll.setForeground(BasicColor.BLACK);

        Composite composite_21 = new Composite(composite_6, SWT.NONE);
        GridData gd_composite_21 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_21.heightHint = 100;
        composite_21.setLayoutData(gd_composite_21);
        composite_21.setLayout(new GridLayout(1, false));
        composite_21.setBackground(BasicColor.LIGHT_TWO);
        btnTranscriptomics = new Button(composite_21, SWT.BORDER);
        btnTranscriptomics.setText("Transcriptomics browser");
        btnTranscriptomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnTranscriptomics.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnTranscriptomics.addSelectionListener(this);
        btnTranscriptomics.setBackground(BasicColor.DARK_TWO);

        Label lblGoThroughAll_1 = new Label(composite_21, SWT.WRAP);
        RWTUtils.setMarkup(lblGoThroughAll_1);
        lblGoThroughAll_1.setAlignment(SWT.CENTER);
        GridData gd_lblGoThroughAll_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblGoThroughAll_1.widthHint = 300;
        lblGoThroughAll_1.setLayoutData(gd_lblGoThroughAll_1);
        lblGoThroughAll_1
                .setText("Browse all 104 <i>Yersinia</i> species transcriptomics datasets available on Yersiniomics. "
                        + "Visualize them on the genome browser. Extract differently expressed genome elements and display their fold changes in a heatmap viewer.");
        lblGoThroughAll_1.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblGoThroughAll_1.setBackground(BasicColor.LIGHT_TWO);
        lblGoThroughAll_1.setForeground(BasicColor.BLACK);

        Composite composite_22 = new Composite(composite_6, SWT.NONE);
        GridData gd_composite_22 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_22.heightHint = 100;
        composite_22.setLayoutData(gd_composite_22);
        composite_22.setLayout(new GridLayout(1, false));
        composite_22.setBackground(BasicColor.LIGHT_TWO);
        btnProteomics = new Button(composite_22, SWT.BORDER);
        btnProteomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnProteomics.setText("Proteomics browser");
        btnProteomics.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnProteomics.addSelectionListener(this);
        btnProteomics.setBackground(BasicColor.DARK_TWO);

        Label lblGoThroughAll_2 = new Label(composite_22, SWT.WRAP);
        RWTUtils.setMarkup(lblGoThroughAll_2);
        lblGoThroughAll_2.setAlignment(SWT.CENTER);
        GridData gd_lblGoThroughAll_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblGoThroughAll_2.widthHint = 300;
        lblGoThroughAll_2.setLayoutData(gd_lblGoThroughAll_2);
        lblGoThroughAll_2
                .setText("Browse all 9 <i>Yersinia</i> species proteomics datasets available on Yersiniomics. "
                        + "Visualize them on the genome browser. Display protein detection patterns for each datasets in a heatmap viewer.");
        lblGoThroughAll_2.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblGoThroughAll_2.setBackground(BasicColor.LIGHT_TWO);
        lblGoThroughAll_2.setForeground(BasicColor.BLACK);

        Composite composite_16 = new Composite(composite, SWT.BORDER);
        GridData gd_composite_16 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_composite_16.widthHint = 1500;
        composite_16.setLayoutData(gd_composite_16);
        composite_16.setLayout(new GridLayout(4, false));
        composite_16.setBackground(BasicColor.LIGHT_THREE);
        Composite composite_17 = new Composite(composite_16, SWT.NONE);
        composite_17.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 2));
        composite_17.setLayout(new GridLayout(1, false));
        composite_17.setBackground(BasicColor.LIGHT_THREE);
        Label lblListeriomicsSpecificTools = new Label(composite_17, SWT.WRAP);
        lblListeriomicsSpecificTools.setAlignment(SWT.CENTER);
        GridData gd_lblListeriomicsSpecificTools = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblListeriomicsSpecificTools.widthHint = 200;
        lblListeriomicsSpecificTools.setLayoutData(gd_lblListeriomicsSpecificTools);
        lblListeriomicsSpecificTools.setBackground(BasicColor.LIGHT_THREE);
        lblListeriomicsSpecificTools.setForeground(BasicColor.BLACK);
        lblListeriomicsSpecificTools.setText("Yersiniomics specific tools");
        lblListeriomicsSpecificTools.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
        //Label lblImage_2 = new Label(composite_17, SWT.BORDER);
        //lblImage_2.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/InitPage/SysBio.png"));

        Composite composite_2 = new Composite(composite_16, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        composite_2.setSize(275, 99);
        composite_2.setLayout(new GridLayout(1, false));
        composite_2.setBackground(BasicColor.LIGHT_THREE);
        btnCoExpression = new Button(composite_2, SWT.BORDER);
        btnCoExpression.addSelectionListener(this);
        btnCoExpression.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnCoExpression.setText("Co-Expression Network");
        btnCoExpression.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnCoExpression.setBackground(BasicColor.DARK_THREE);

        Label lblExpressionAtlas_1 = new Label(composite_2, SWT.WRAP);
        lblExpressionAtlas_1.setAlignment(SWT.CENTER);
        GridData gd_lblExpressionAtlas_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblExpressionAtlas_1.widthHint = 400;
        lblExpressionAtlas_1.setLayoutData(gd_lblExpressionAtlas_1);
        lblExpressionAtlas_1.setText("Access to the co-expression network tool to search for potential regulations");
        lblExpressionAtlas_1.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblExpressionAtlas_1.setBackground(BasicColor.LIGHT_THREE);
        lblExpressionAtlas_1.setForeground(BasicColor.BLACK);

        Composite composite_18 = new Composite(composite_16, SWT.NONE);
        composite_18.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        composite_18.setBackground(BasicColor.LIGHT_THREE);
        composite_18.setLayout(new GridLayout(1, false));
        btnLoadData = new Button(composite_18, SWT.BORDER | SWT.CENTER);
        btnLoadData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnLoadData.setText(" Load data selection");
        btnLoadData.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/txtload.bmp"));
        btnLoadData.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnLoadData.setBackground(BasicColor.DARK_THREE);

        Label lblLoadAPrevious = new Label(composite_18, SWT.WRAP);
        lblLoadAPrevious.setAlignment(SWT.CENTER);
        lblLoadAPrevious.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        GridData gd_lblLoadAPrevious = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblLoadAPrevious.widthHint = 400;
        lblLoadAPrevious.setLayoutData(gd_lblLoadAPrevious);
        lblLoadAPrevious.setText("Load a previous genome viewer visualization saved in .gview file");
        lblLoadAPrevious.setBackground(BasicColor.LIGHT_THREE);
        lblLoadAPrevious.setForeground(BasicColor.BLACK);
        btnLoadData.addSelectionListener(this);

        Composite composite_5 = new Composite(composite_16, SWT.NONE);
        composite_5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        composite_5.setLayout(new GridLayout(1, false));
        composite_5.setBackground(BasicColor.LIGHT_THREE);
        btnAccessWiki = new Button(composite_5, SWT.BORDER);
        btnAccessWiki.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnAccessWiki.setText("Access Yersiniomics wiki");
        btnAccessWiki.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnAccessWiki.addSelectionListener(this);
        btnAccessWiki.setBackground(BasicColor.DARK_THREE);

        Label lblGoToThe = new Label(composite_5, SWT.WRAP);
        lblGoToThe.setAlignment(SWT.CENTER);
        lblGoToThe.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        GridData gd_lblGoToThe = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblGoToThe.widthHint = 400;
        lblGoToThe.setLayoutData(gd_lblGoToThe);
        lblGoToThe.setText(
                "Go to the Yersiniomics wiki page for tutorials and description of the different tools included in Yersiniomics. "
                        + "Be careful, it might not display if you disallow your internet browser to display pop-up webpage.");
        lblGoToThe.setBackground(BasicColor.LIGHT_THREE);
        lblGoToThe.setForeground(BasicColor.BLACK);

        new Label(composite_16, SWT.NONE);
        new Label(composite_16, SWT.NONE);
        new Label(composite_16, SWT.NONE);

        Composite composite_19 = new Composite(composite, SWT.NONE);
        composite_19.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        composite_19.setLayout(new GridLayout(1, false));

        Label lblLastUpdate = new Label(composite_19, SWT.NONE);
        lblLastUpdate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
        lblLastUpdate.setText("Last update: September 2021");

        linkPubli = new Link(composite_19, SWT.NONE);
        linkPubli.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        linkPubli.setText("<a>Cite Yersiniomics</a>");
        linkPubli.addSelectionListener(this);

        Link link = new Link(composite_19, SWT.NONE);
        link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        link.setText("Contact us if you have a recently published \"omics\" dataset you want to be integrated to Yersiniomics:  <a href=\"mailto:yersiniomics@pasteur.fr\">yersiniomics@pasteur.fr</a>");

        new Label(composite_19, SWT.NONE);

        Label lblImagelicence = new Label(composite_19, SWT.CENTER);
        lblImagelicence.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblImagelicence.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/cccommons.png"));

        Label lblThisWorkIs = new Label(composite_19, SWT.NONE);
        lblThisWorkIs.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblThisWorkIs.setAlignment(SWT.CENTER);
        lblThisWorkIs.setText("This work is licensed under");
        linkLicenceField = new Link(composite_19, SWT.NONE);
        linkLicenceField.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        linkLicenceField.setText("<a>Creative Commons Attribution 4.0 International License</a>");
        linkLicenceField.addSelectionListener(this);

        new Label(composite_19, SWT.NONE);

        Label lblCredits = new Label(composite_19, SWT.NONE);
        lblCredits.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblCredits.setAlignment(SWT.CENTER);
        lblCredits.setText("Credits");
        linkHUB = new Link(composite_19, SWT.NONE);
        linkHUB.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        linkHUB.setText("<a>Bioinformatics and Biostatistics HUB, Institut Pasteur, Paris</a>");
        linkHUB.addSelectionListener(this);

        Label lblBacnet_1 = new Label(composite_19, SWT.NONE);
        lblBacnet_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblBacnet_1.setText("BACNET 10-BINF-02-01");

        Composite composite_3 = new Composite(composite_19, SWT.NONE);
        composite_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        composite_3.setLayout(new GridLayout(4, false));

        Label lblPasteur = new Label(composite_3, SWT.NONE);
        lblPasteur.setSize(0, 15);
        lblPasteur.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblPasteur.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/Pasteur.png"));

        Label lblInra = new Label(composite_3, SWT.NONE);
        lblInra.setSize(0, 15);
        lblInra.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblInra.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/INRA.png"));
        Label lblAnr = new Label(composite_3, SWT.NONE);
        lblAnr.setSize(0, 15);
        lblAnr.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/Logo ANR.png"));

        Label lblBacnet = new Label(composite_3, SWT.NONE);
        lblBacnet.setSize(0, 15);
        lblBacnet.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/investissement-davenir.png"));

        scrolledComposite.setContent(composite);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        pushState();

        //System.out.println("InitView loaded");
 
    }

    @Focus
    public void onFocus() {
        if (!focused) {
            pushState();
            System.out.println("InitView Focused");
            focused = true;
        } else {
            focused = false;
        }
    }

    /**
     * Push genome, chromosome, gene and Tabitem state
     */
    public void pushState() {
        NavigationManagement.pushStateView(InitViewYersinia.ID);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnCoExpression) {
            partService.showPart(CoExprNetworkView.ID, PartState.ACTIVATE);
            NavigationManagement.pushStateView(CoExprNetworkView.ID);
        }else if (e.getSource() == btnCO92) {
            GeneView.openGeneView(partService);
        	//GenomeTranscriptomeView.displayYersiCO92(partService);
        }else if (e.getSource() == btnKIM) {
            GeneView.openKIMGeneView(partService);
        	//GenomeTranscriptomeView.displayYersiKIM(partService);
        } else if (e.getSource() == btnIP32953) {
            GeneView.openIP32953GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiIP32953(partService);
        } else if (e.getSource() == btnYPIII) {
            GeneView.openYPIIIGeneView(partService);
        	//GenomeTranscriptomeView.displayYersiYPIII(partService);
        } else if (e.getSource() == btnY11) {
            GeneView.openY11GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiY11(partService);
        } else if (e.getSource() == btn8081) {
            GeneView.open8081GeneView(partService);
        	//GenomeTranscriptomeView.displayYersi8081(partService);
        } else if (e.getSource() == btnCSF007) {
            GeneView.openQMA0440GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiCSF007(partService);
        } else if (e.getSource() == btn91001) {
            GeneView.open91001GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiCSF007(partService);
        }else if (e.getSource() == btnProteomics) {
            partService.showPart(ProteomicsView.ID, PartState.ACTIVATE);
            NavigationManagement.pushStateView(ProteomicsView.ID);
        } else if (e.getSource() == btnGenomics) {
            partService.showPart(GenomicsView.ID, PartState.ACTIVATE);
            NavigationManagement.pushStateView(GenomicsView.ID);
        } else if (e.getSource() == btnTranscriptomics) {
            try {
                ArrayList<String> genomeNames = new ArrayList<>();
                genomeNames.add(Genome.YERSINIA_NAME);
                OpenGenomesThread thread = new OpenGenomesThread(genomeNames);
                new ProgressMonitorDialog(this.shell).run(true, false, thread);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            partService.showPart(TranscriptomicsView.ID, PartState.ACTIVATE);
            NavigationManagement.pushStateView(TranscriptomicsView.ID);
        } else if (e.getSource() == btnAccessWiki) {
            UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
            launcher.openURL("https://listeriomics.pasteur.fr/WikiListeriomics/index.php/Summary");
        } else if (e.getSource() == btnGeneView) {
            GeneView.openGeneView(partService);
        } else if (e.getSource() == linkPubli) {
            String url = "";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == linkHUB) {
            String url = "https://research.pasteur.fr/en/team/bioinformatics-and-biostatistics-hub/";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == linkLicenceField) {
            String url = "http://creativecommons.org/licenses/by/4.0/";
            NavigationManagement.openURLInExternalBrowser(url, partService);
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
        }
        // else if(e.getSource()==btnCheckSessionStatus){
        // SessionControl sessionControl = new SessionControl();
        // sessionControl.testApplicationContext(partService);
        // }else if(e.getSource()==btnCloseGenomeViewer){
        // //SaveFileUtils.createDownloadUrl("toto2.txt","Test download de fichier",
        // partService);
        // //SaveFileUtils.createDownloadUrl("toto3.txt","Test download de fichier",
        // partService);
        // //SessionControl.closeGenomeViewer(partService);

    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }
}
