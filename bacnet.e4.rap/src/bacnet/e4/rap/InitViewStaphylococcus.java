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
import org.eclipse.swt.browser.Browser;
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

import bacnet.Database;
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

public class InitViewStaphylococcus implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -9052983689419871498L;

    public static final String ID = "bacnet.Staphylococcus"; //$NON-NLS-1$

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;
    
    private Button btnUSA300_ISMMS1;
    private Button btnNewman;
    private Button btnUSA300_TCH1516;
    private Button btnYPIII;
    private Button btnIP32953;
    private Button btnIP31758;
    private Button btnEV76;
    private Button btnY11;
    private Button btnY1;
    private Button btn8081;
    private Button btnWA;
    private Button btnSC09;
    private Button btnQMA0440;
    private Button btnUSA300_FPR3757;
    private Button btnMH96;
    private Button btnIP38326;
    private Button btnIP38023;
    private Button btnIP37485;
    private Button btnIP37574;
    private Button btnCoExpression;
    private Button btnLoadData;
    private Button btnDownloadData;
    private Button btnGeneView;
    private Button btnAccessWiki;
    private Link linkPubli;
    private Link linkPubli2;
    private Link linkYersinia;
    private Button btnTranscriptomics;
    private Button btnGenomics;
    private Button btnProteomics;
    private Link linkLicenceField;
	private Button btnHelp;

    @Inject
    EPartService partService;

    @Inject
    EModelService modelService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public InitViewStaphylococcus() {}

    @PostConstruct
    public void createPartControl(Composite parent) {
    	System.out.println("TEST INIT");
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
        composite_Intro.setLayout(new GridLayout(1, false));
        GridData gd_composite_Intro = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
        composite_Intro.setLayoutData(gd_composite_Intro);
        // composite_Intro.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_DARK_SHADOW));
        new Label(composite_Intro, SWT.NONE);

        Label yersiniomicsLogo = new Label(composite_Intro, SWT.NONE);
        yersiniomicsLogo.setAlignment(SWT.CENTER);
        yersiniomicsLogo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        yersiniomicsLogo.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/ToolBar/LogoStaphylomics.png"));
        
        Label lblIntro = new Label(composite_Intro, SWT.NONE);
        lblIntro.setAlignment(SWT.CENTER);
        GridData gd_lblIntro = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        //gd_lblIntro.widthHint=900;
        lblIntro.setLayoutData(gd_lblIntro);
        lblIntro.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        lblIntro.setText(
                "<br>"+Database.getInstance().getWebpageTitle()+" integrates complete <b>genomes</b>, <b>transcriptomes</b> and <b>proteomes</b> published for <i>"+Database.getInstance().getSpecies()+"</i> species.</br>"
                		+ "<br>Access <b>enriched information</b> about <i>"+Database.getInstance().getSpecies()+"</i> species genes in complete genomes:</br>"
                        + "Annotation, gene conservation, synteny, transcript atlas, protein atlas, integration of external databases."
                		+ "<br></br>Use "+Database.getInstance().getWebpageTitle()+" to decipher <b>regulatory mechanisms</b> of your genome element of interest,<br>"
                        + "navigating among all these datasets with <b>enriched metadata</b> in a user-friendly format.</br>"
                        );

        lblIntro.setFont(SWTResourceManager.getBodyFont(20,SWT.NORMAL));
        new Label(composite_Intro, SWT.NONE);

        if (Database.getInstance().getProjectName() == Database.URY_YERSINIOMICS_PROJECT) {

        	Label privateDatabase = new Label(composite_Intro, SWT.NONE);
        	GridData gd_PrivateDatabase = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
            //gd_lblIntro.widthHint=900;
        	privateDatabase.setLayoutData(gd_PrivateDatabase);
        	privateDatabase.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
            privateDatabase.setText("<b>You are on the <i>Yersinia</i> Research Unit private database website</b>" );
            privateDatabase.setFont(SWTResourceManager.getBodyFont(30,SWT.NORMAL));
            privateDatabase.setForeground(BasicColor.RED);;

        }
        
        Composite gene_view_composite = new Composite(composite, SWT.BORDER);
        GridData gd_gene_view_composite = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        //gd_gene_view_composite.widthHint = 1500;
        gene_view_composite.setLayoutData(gd_gene_view_composite);
        gene_view_composite.setLayout(new GridLayout(1, false));
        gene_view_composite.setBackground(BasicColor.GOLD_DARK_ONE);
        
        Label spacer_0 = new Label(gene_view_composite, SWT.NONE);
        spacer_0.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        
        Label lblGeneViewer = new Label(gene_view_composite, SWT.WRAP);
        GridData gd_lblGeneViewer = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        lblGeneViewer.setLayoutData(gd_lblGeneViewer);
        lblGeneViewer.setAlignment(SWT.CENTER);
        lblGeneViewer.setFont(SWTResourceManager.getTitleFont(30, SWT.BOLD));
        lblGeneViewer.setBackground(BasicColor.GOLD_DARK_ONE);
        RWTUtils.setMarkup(lblGeneViewer);
        lblGeneViewer.setForeground(BasicColor.BLACK);
        lblGeneViewer.setText("Gene viewers<sup style=\"font-family: Times New Roman;  font-size:18px; color:purple\"><i>i</i></sup>");
        lblGeneViewer.setToolTipText("Access enriched gene information of a genome by clicking on its name\nHover over the genome button for more information on the strain");
        Label spacer_1 = new Label(gene_view_composite, SWT.NONE);
        spacer_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 
        Composite gene_view_composite_row_1 = new Composite(gene_view_composite, SWT.NONE);
        GridData gd_gene_view_composite_row_1 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        gd_gene_view_composite_row_1.widthHint = 1200;
        gene_view_composite_row_1.setLayoutData(gd_gene_view_composite_row_1);
        gene_view_composite_row_1.setLayout(new GridLayout(5, false));
        gene_view_composite_row_1.setBackground(BasicColor.GOLD_DARK_ONE);
        
        Composite pestis_composite = new Composite(gene_view_composite_row_1, SWT.NONE);
        GridData gd_pestis_composite = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        pestis_composite.setLayoutData(gd_pestis_composite);
        pestis_composite.setLayout(new GridLayout(1, false));
        pestis_composite.setBackground(BasicColor.GOLD_DARK_ONE);
        
        Label lblPestis = new Label(pestis_composite, SWT.NONE);
        RWTUtils.setMarkup(lblPestis);
        GridData gd_lblPestis = new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1);
        lblPestis.setLayoutData(gd_lblPestis);
        lblPestis.setFont(SWTResourceManager.getBodyFont(22,SWT.BOLD));
        lblPestis.setBackground(BasicColor.GOLD_DARK_ONE);
        lblPestis.setForeground(BasicColor.BLACK);
        lblPestis.setText("<i>S. aureus</i>");
        
        Composite composite_pestis_row_1 = new Composite(pestis_composite, SWT.NONE);
        GridData gd_composite_pestis_row_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        composite_pestis_row_1.setLayoutData(gd_composite_pestis_row_1);
        composite_pestis_row_1.setLayout(new GridLayout(3, false));
        composite_pestis_row_1.setBackground(BasicColor.GOLD_DARK_ONE);
               
        btnNewman = new Button(composite_pestis_row_1, SWT.TOGGLE);
        btnNewman.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnNewman.setText("Newman");
        btnNewman.setToolTipText("Newman");
        btnNewman.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnNewman.setBackground(BasicColor.GOLD_LIGHT_ONE);
        //btnNewman.setForeground(BasicColor.BLACK);
        btnNewman.addSelectionListener(this);
       

        Composite composite_pestis_row_2 = new Composite(pestis_composite, SWT.NONE);
        GridData gd_composite_pestis_row_2 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        composite_pestis_row_2.setLayoutData(gd_composite_pestis_row_2);
        composite_pestis_row_2.setLayout(new GridLayout(3, false));
        composite_pestis_row_2.setBackground(BasicColor.GOLD_DARK_ONE);
        
        btnUSA300_TCH1516 = new Button(composite_pestis_row_2, SWT.TOGGLE);
        btnUSA300_TCH1516.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnUSA300_TCH1516.setText("USA300_TCH1516");
        btnUSA300_TCH1516.setToolTipText("USA300_TCH1516");
        btnUSA300_TCH1516.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnUSA300_TCH1516.setBackground(BasicColor.GOLD_LIGHT_ONE);
        //btnUSA300_TCH1516.setForeground(BasicColor.BLACK);
        btnUSA300_TCH1516.addSelectionListener(this);
        
        
        btnUSA300_FPR3757 = new Button(composite_pestis_row_2, SWT.TOGGLE);
        btnUSA300_FPR3757.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        btnUSA300_FPR3757.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnUSA300_FPR3757.addSelectionListener(this);
        btnUSA300_FPR3757.setText("USA300_FPR3757");
        btnUSA300_FPR3757.setToolTipText("USA300_FPR3757");
        btnUSA300_FPR3757.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnUSA300_FPR3757.setBackground(BasicColor.GOLD_LIGHT_ONE);
        

        btnUSA300_ISMMS1 = new Button(composite_pestis_row_2, SWT.TOGGLE);
        btnUSA300_ISMMS1.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnUSA300_ISMMS1.setText("USA300_ISMMS1");
        btnUSA300_ISMMS1.setToolTipText("USA300_ISMMS1");
        btnUSA300_ISMMS1.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnUSA300_ISMMS1.setBackground(BasicColor.GOLD_LIGHT_ONE);
        //btnUSA300_ISMMS1.setForeground(BasicColor.BLACK);
        btnUSA300_ISMMS1.addSelectionListener(this);
        
        
        //btn91001.setForeground(BasicColor.BLACK);
        
        /*
        btnEV76 = new Button(composite_pestis_row_2, SWT.TOGGLE);
        btnEV76.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnEV76.setText("EV76-CN");
        btnEV76.setToolTipText("Lineage 1.ORI\nIsolated in Madagascar"
        		+ "\nUsed as vaccinal strain");
        btnEV76.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnEV76.setBackground(BasicColor.LIGHT_ONE);
        //btnEV76.setForeground(BasicColor.BLACK);
        btnEV76.addSelectionListener(this);

        Composite composite_pseudotb = new Composite(gene_view_composite_row_1, SWT.NONE);
        GridData gd_composite_pseudotb = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        composite_pseudotb.setLayoutData(gd_composite_pseudotb);
        composite_pseudotb.setLayout(new GridLayout(1, false));
        composite_pseudotb.setBackground(BasicColor.DARK_ONE);
        composite_pseudotb.setForeground(BasicColor.BLACK);
        
        
        Label lblPseudo = new Label(composite_pseudotb, SWT.NONE);
        RWTUtils.setMarkup(lblPseudo);
        lblPseudo.setAlignment(SWT.CENTER);
        GridData gd_lblPseudo = new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1);
        lblPseudo.setLayoutData(gd_lblPseudo);
        lblPseudo.setFont(SWTResourceManager.getBodyFont(22,SWT.BOLD));
        lblPseudo.setBackground(BasicColor.DARK_ONE);
        lblPseudo.setForeground(BasicColor.BLACK);
        lblPseudo.setText("<i>Y. pseudotuberculosis</i>");

        Composite composite_pseudotb_row_1 = new Composite(composite_pseudotb, SWT.NONE);
        GridData gd_composite_pseudotb_row_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        composite_pseudotb_row_1.setLayoutData(gd_composite_pseudotb_row_1);
        composite_pseudotb_row_1.setLayout(new GridLayout(2, false));
        composite_pseudotb_row_1.setBackground(BasicColor.DARK_ONE);
        
        btnYPIII = new Button(composite_pseudotb_row_1, SWT.TOGGLE);
        btnYPIII.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnYPIII.addSelectionListener(this);
        btnYPIII.setText("YPIII");
        btnYPIII.setToolTipText("Genotype 6");

        btnYPIII.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnYPIII.setBackground(BasicColor.LIGHT_ONE);
        
        btnIP32953 = new Button(composite_pseudotb_row_1, SWT.TOGGLE);
        btnIP32953.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnIP32953.addSelectionListener(this);
        btnIP32953.setText("IP32953");
        btnIP32953.setToolTipText("Genotype 16\nFirst Y. pseudotuberculosis genome to be sequenced");

        btnIP32953.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnIP32953.setBackground(BasicColor.LIGHT_ONE);
        
        Composite composite_pseudotb_row_2 = new Composite(composite_pseudotb, SWT.NONE);
        GridData gd_composite_pseudotb_row_2 = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        composite_pseudotb_row_2.setLayoutData(gd_composite_pseudotb_row_2);
        composite_pseudotb_row_2.setLayout(new GridLayout(1, false));
        composite_pseudotb_row_2.setBackground(BasicColor.DARK_ONE);
        
        btnIP31758 = new Button(composite_pseudotb_row_2, SWT.TOGGLE);
        btnIP31758.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnIP31758.addSelectionListener(this);
        btnIP31758.setText("IP31758");
        btnIP31758.setToolTipText("Genotype 8\nEtiologic agent of Far East Scarlet-Like Fever (FESLF)");
        btnIP31758.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnIP31758.setBackground(BasicColor.LIGHT_ONE);
 
        Composite composite_entero = new Composite(gene_view_composite_row_1, SWT.NONE);
        GridData gd_composite_entero = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);

        composite_entero.setLayoutData(gd_composite_entero);
        composite_entero.setLayout(new GridLayout(1, false));
        composite_entero.setBackground(BasicColor.DARK_ONE);
        
        Label lblEntero = new Label(composite_entero, SWT.NONE);
        RWTUtils.setMarkup(lblEntero);
        lblEntero.setAlignment(SWT.CENTER);
        GridData gd_lblEntero = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        lblEntero.setLayoutData(gd_lblEntero);
        lblEntero.setFont(SWTResourceManager.getBodyFont(22,SWT.BOLD));
        lblEntero.setBackground(BasicColor.DARK_ONE);
        lblEntero.setForeground(BasicColor.BLACK);
        lblEntero.setText("<i>Y. enterocolitica</i>");
              
        Composite composite_entero_row_1 = new Composite(composite_entero, SWT.NONE);
        GridData gd_composite_entero_row_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        composite_entero_row_1.setLayoutData(gd_composite_entero_row_1);
        composite_entero_row_1.setLayout(new GridLayout(2, false));
        composite_entero_row_1.setBackground(BasicColor.DARK_ONE);
        composite_entero_row_1.setForeground(BasicColor.BLACK);
 
        btn8081 = new Button(composite_entero_row_1, SWT.TOGGLE);
        btn8081.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btn8081.addSelectionListener(this);
        btn8081.setText("8081");
        btn8081.setToolTipText("Genotype 1B");

        btn8081.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btn8081.setBackground(BasicColor.LIGHT_ONE);

        btnWA = new Button(composite_entero_row_1, SWT.TOGGLE);
        btnWA.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnWA.addSelectionListener(this);
        btnWA.setText("WA");
        btnWA.setToolTipText("Genotype 1B");

        btnWA.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnWA.setBackground(BasicColor.LIGHT_ONE);
        
        Composite composite_entero_row_2 = new Composite(composite_entero, SWT.NONE);
        GridData gd_composite_entero_row_2 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        composite_entero_row_2.setLayoutData(gd_composite_entero_row_2);
        composite_entero_row_2.setLayout(new GridLayout(2, false));
        composite_entero_row_2.setBackground(BasicColor.DARK_ONE);
        composite_entero_row_2.setForeground(BasicColor.BLACK);
        
        btnY1 = new Button(composite_entero_row_2, SWT.TOGGLE);
        btnY1.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnY1.addSelectionListener(this);
        btnY1.setText("Y1");
        btnY1.setToolTipText("Genotype 4");

        btnY1.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnY1.setBackground(BasicColor.LIGHT_ONE);
        
        btnY11 = new Button(composite_entero_row_2, SWT.TOGGLE);
        btnY11.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnY11.addSelectionListener(this);
        btnY11.setText("Y11");
        btnY11.setToolTipText("Genotype 4");

        btnY11.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnY11.setBackground(BasicColor.LIGHT_ONE);
        
        Composite composite_ruckeri = new Composite(gene_view_composite_row_1, SWT.NONE);
        GridData gd_composite_ruckeri = new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1);
        composite_ruckeri.setLayoutData(gd_composite_ruckeri);
        composite_ruckeri.setLayout(new GridLayout(1, false));
        composite_ruckeri.setBackground(BasicColor.DARK_ONE);
        composite_ruckeri.setForeground(BasicColor.BLACK);
        
        Label lblRuck = new Label(composite_ruckeri, SWT.NONE);
        RWTUtils.setMarkup(lblRuck);
        lblRuck.setAlignment(SWT.CENTER);
        GridData gd_lblRuck = new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1);
        lblRuck.setLayoutData(gd_lblRuck);
        lblRuck.setFont(SWTResourceManager.getBodyFont(22,SWT.BOLD));
        lblRuck.setBackground(BasicColor.DARK_ONE);
        lblRuck.setForeground(BasicColor.BLACK);
        lblRuck.setText("<i>Y. ruckeri</i>");
        
        Composite composite_ruckeri_row_1 = new Composite(composite_ruckeri, SWT.NONE);
        GridData gd_composite_ruckeri_row_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        composite_ruckeri_row_1.setLayoutData(gd_composite_ruckeri_row_1);
        composite_ruckeri_row_1.setLayout(new GridLayout(2, false));
        composite_ruckeri_row_1.setBackground(BasicColor.DARK_ONE);
        composite_ruckeri_row_1.setForeground(BasicColor.BLACK);
        
        btnSC09 = new Button(composite_ruckeri_row_1, SWT.TOGGLE);
        btnSC09.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnSC09.addSelectionListener(this);
        btnSC09.setText("SC09");
        btnSC09.setToolTipText("Etiologic agent of enteric redmouth disease in fish");

        btnSC09.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnSC09.setBackground(BasicColor.LIGHT_ONE);
        
        Composite composite_ruckeri_row_2 = new Composite(composite_ruckeri, SWT.NONE);
        GridData gd_composite_ruckeri_row_2 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        composite_ruckeri_row_2.setLayoutData(gd_composite_ruckeri_row_2);
        composite_ruckeri_row_2.setLayout(new GridLayout(2, false));
        composite_ruckeri_row_2.setBackground(BasicColor.DARK_ONE);
        composite_ruckeri_row_2.setForeground(BasicColor.BLACK);
        
        btnQMA0440 = new Button(composite_ruckeri_row_2, SWT.TOGGLE);
        btnQMA0440.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnQMA0440.addSelectionListener(this);
        btnQMA0440.setText("QMA0440");
        btnQMA0440.setToolTipText("Etiologic agent of enteric redmouth disease in fish");
        btnQMA0440.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnQMA0440.setBackground(BasicColor.LIGHT_ONE);
        
        Composite composite_entomophaga = new Composite(gene_view_composite_row_1, SWT.NONE);
        GridData gd_composite_entomophaga = new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1);
        composite_entomophaga.setLayoutData(gd_composite_entomophaga);
        composite_entomophaga.setLayout(new GridLayout(1, false));
        composite_entomophaga.setBackground(BasicColor.DARK_ONE);
        composite_entomophaga.setForeground(BasicColor.BLACK);
        
        Label lblEntomo = new Label(composite_entomophaga, SWT.NONE);
        RWTUtils.setMarkup(lblEntomo);
        lblEntomo.setAlignment(SWT.CENTER);
        GridData gd_lblEntomo = new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1);
        lblEntomo.setLayoutData(gd_lblEntomo);
        lblEntomo.setFont(SWTResourceManager.getBodyFont(22,SWT.BOLD));
        lblEntomo.setBackground(BasicColor.DARK_ONE);
        lblEntomo.setForeground(BasicColor.BLACK);
        lblEntomo.setText("<i>Y. entomophaga</i>");
        
        Composite composite_entomophaga_row_1= new Composite(composite_entomophaga, SWT.NONE);
        GridData gd_composite_entomophaga_row_1 = new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1);
        composite_entomophaga_row_1.setLayoutData(gd_composite_entomophaga_row_1);
        composite_entomophaga_row_1.setLayout(new GridLayout(2, false));
        composite_entomophaga_row_1.setBackground(BasicColor.DARK_ONE);
        composite_entomophaga_row_1.setForeground(BasicColor.BLACK);
        
        btnMH96 = new Button(composite_entomophaga_row_1, SWT.TOGGLE);
        btnMH96.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
        btnMH96.addSelectionListener(this);
        btnMH96.setText("MH96");
        btnMH96.setToolTipText("Insect pathogen");
        btnMH96.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnMH96.setBackground(BasicColor.LIGHT_ONE);
        
        */
        
        Composite composite_omics_browser = new Composite(composite, SWT.BORDER);
        GridData gd_composite_omics_browser = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        composite_omics_browser.setLayoutData(gd_composite_omics_browser);
        composite_omics_browser.setLayout(new GridLayout(1, false));
        composite_omics_browser.setBackground(BasicColor.GOLD_DARK_TWO);
        
        new Label(composite_omics_browser, SWT.NONE);
        
        Label lblBrowseOmicsDatasets = new Label(composite_omics_browser, SWT.NONE);
        lblBrowseOmicsDatasets.setAlignment(SWT.CENTER);
        GridData gd_lblBrowseOmicsDatasets = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        lblBrowseOmicsDatasets.setLayoutData(gd_lblBrowseOmicsDatasets);
        RWTUtils.setMarkup(lblBrowseOmicsDatasets);
        lblBrowseOmicsDatasets.setText("Omics browsers<sup style=\"font-family: Times New Roman;  font-size:18px; color:purple\"><i>i</i></sup>");
        lblBrowseOmicsDatasets.setForeground(BasicColor.BLACK);
        lblBrowseOmicsDatasets.setFont(SWTResourceManager.getTitleFont(30, SWT.BOLD));
        lblBrowseOmicsDatasets.setBackground(BasicColor.GOLD_DARK_TWO);
        lblBrowseOmicsDatasets.setToolTipText("Access complete genomes, transcriptomes and proteomes gathered on "+Database.getInstance().getWebpageTitle()+"\nHover over the browser buttons for more information on their specific functionalities");

        new Label(composite_omics_browser, SWT.NONE);
        
        Composite composite_omics_browser_row_1 = new Composite(composite_omics_browser, SWT.NONE);
        GridData gd_composite_omics_browser_row_1 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        gd_composite_omics_browser_row_1.widthHint = 800;
        composite_omics_browser_row_1.setLayoutData(gd_composite_omics_browser_row_1);
        composite_omics_browser_row_1.setLayout(new GridLayout(3, false));
        composite_omics_browser_row_1.setBackground(BasicColor.GOLD_DARK_TWO);
        
        btnGenomics = new Button(composite_omics_browser_row_1, SWT.TOGGLE);
        btnGenomics.setText("Genomics browser");
        btnGenomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        btnGenomics.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnGenomics.addSelectionListener(this);
        btnGenomics.setBackground(BasicColor.GOLD_LIGHT_TWO);
        btnGenomics.setToolTipText("Browse all complete genomes\n"
                + " Visualize strain relationship in a phylogenomic tree\n"
                + "Access all annotated genome elements");

        btnTranscriptomics = new Button(composite_omics_browser_row_1, SWT.TOGGLE);
        btnTranscriptomics.setText("Transcriptomics browser");
        btnTranscriptomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        btnTranscriptomics.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnTranscriptomics.addSelectionListener(this);
        btnTranscriptomics.setBackground(BasicColor.GOLD_LIGHT_TWO);
        btnTranscriptomics.setToolTipText("Browse all microarray and RNA-Seq biological conditions available on "+Database.getInstance().getWebpageTitle()+"\n"
                        + "Visualize transcript fold changes and RNA-Seq coverage in the genome viewer\n"
                        + "Display transcript fold change patterns in the heatmap viewer");
       
        btnProteomics = new Button(composite_omics_browser_row_1, SWT.TOGGLE);
        btnProteomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        btnProteomics.setText("Proteomics browser");
        btnProteomics.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnProteomics.addSelectionListener(this);
        btnProteomics.setBackground(BasicColor.GOLD_LIGHT_TWO);
        btnProteomics.setToolTipText("Browse all proteomics biological conditions available on "+Database.getInstance().getWebpageTitle()+"\n"
        		+ "Visualize protein abundances and fold changes in the genome viewer"
        		+ "\nDisplay protein abundance fold change patterns in the heatmap viewer");

        new Label(composite_omics_browser, SWT.NONE);

        Composite composite_data_loading = new Composite(composite, SWT.BORDER);
        GridData gd_composite_data_loading = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        composite_data_loading.setLayoutData(gd_composite_data_loading);
        composite_data_loading.setLayout(new GridLayout(1, false));
        composite_data_loading.setBackground(BasicColor.GOLD_DARK_THREE);
          
        new Label(composite_data_loading, SWT.NONE);

        Label lblDataLoading = new Label(composite_data_loading, SWT.WRAP);
        lblDataLoading.setAlignment(SWT.CENTER);
        GridData gd_lblDataLoading = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        lblDataLoading.setLayoutData(gd_lblDataLoading);
        RWTUtils.setMarkup(lblDataLoading);
        lblDataLoading.setText("Data loading<sup style=\"font-family: Times New Roman;  font-size:18px; color:purple\"><i>i</i></sup>");
        lblDataLoading.setForeground(BasicColor.BLACK);
        lblDataLoading.setFont(SWTResourceManager.getTitleFont(30, SWT.BOLD));
        lblDataLoading.setBackground(BasicColor.GOLD_DARK_THREE);
        lblDataLoading.setToolTipText("Load on "+Database.getInstance().getWebpageTitle()+" and download from "+Database.getInstance().getWebpageTitle()+"\nHover over the load buttons for more information on their specific functionalities ");

        new Label(composite_data_loading, SWT.NONE);

        Composite composite_data_loading_row_1 = new Composite(composite_data_loading, SWT.NONE);
        GridData gd_composite_data_loading_row_1 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        gd_composite_data_loading_row_1.widthHint = 600;
        composite_data_loading_row_1.setLayoutData(gd_composite_data_loading_row_1);
        composite_data_loading_row_1.setLayout(new GridLayout(2, false));
        composite_data_loading_row_1.setBackground(BasicColor.GOLD_DARK_THREE);
        
        btnLoadData = new Button(composite_data_loading_row_1, SWT.TOGGLE);
        btnLoadData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        btnLoadData.setText(" Load genome viewer");
        //btnLoadData.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/txtload.bmp"));
        btnLoadData.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnLoadData.setBackground(BasicColor.GOLD_LIGHT_THREE);
        btnLoadData.setToolTipText("Load a genome viewer displaying specific omics\ndata previously saved in a .gview file");
        btnLoadData.addSelectionListener(this);

        btnDownloadData = new Button(composite_data_loading_row_1, SWT.TOGGLE);
        btnDownloadData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        btnDownloadData.setText(" Download processed data");
        //btnDownloadData.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/txtload.bmp"));
        btnDownloadData.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnDownloadData.setBackground(BasicColor.GOLD_LIGHT_THREE);
        btnDownloadData.setToolTipText("Download processed transcriptomics and proteomics\ndata for each genome in a table format");
        btnDownloadData.addSelectionListener(this);

        new Label(composite_data_loading, SWT.NONE);
        
        Composite composite_foot = new Composite(composite, SWT.NONE);
        composite_foot.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        composite_foot.setLayout(new GridLayout(4, true));
        
        Composite composite_19 = new Composite(composite_foot, SWT.NONE);
        composite_19.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 4, 1));
        composite_19.setLayout(new GridLayout(1, true));
        
        Label lblLastUpdate = new Label(composite_19, SWT.NONE);
        lblLastUpdate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        lblLastUpdate.setText("Last update: January 2023");
        lblLastUpdate.setFont(SWTResourceManager.getBodyFont(20,SWT.BOLD));
        
        new Label(composite_19, SWT.NONE);

        linkPubli = new Link(composite_19, SWT.NONE);
        linkPubli.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        linkPubli.setText("For more information on the website functionalities, please go to <a>L\u00EA-Bury et al.</a>");
        linkPubli.setFont(SWTResourceManager.getBodyFont(18,SWT.NORMAL));
        linkPubli.addSelectionListener(this);
        
        new Label(composite_19, SWT.NONE);
        linkPubli2 = new Link(composite_19, SWT.NONE);
        linkPubli2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        linkPubli2.setText("If you use "+Database.getInstance().getWebpageTitle()+", please cite our <a>article</a>");
        linkPubli2.setFont(SWTResourceManager.getBodyFont(18,SWT.NORMAL));
        linkPubli2.addSelectionListener(this);

        new Label(composite_19, SWT.NONE);
        Label lblContact = new Label(composite_19, SWT.NONE);
        lblContact.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        lblContact.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        lblContact.setText("Contact us if you have a recently published \"omics\" datasets<br/>you want to be integrated to "+Database.getInstance().getWebpageTitle()+":<br><a href=\"mailto:yersiniomics@pasteur.fr\">yersiniomics@pasteur.fr</a></br>");
        lblContact.setFont(SWTResourceManager.getBodyFont(18,SWT.NORMAL));
        lblContact.setAlignment(SWT.CENTER);

/*
        new Label(composite_19, SWT.NONE);

        Label lblImagelicence = new Label(composite_19, SWT.CENTER);
        lblImagelicence.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblImagelicence.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/cccommons.png"));

        Label lblThisWorkIs = new Label(composite_19, SWT.NONE);
        lblThisWorkIs.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        lblThisWorkIs.setAlignment(SWT.CENTER);
        lblThisWorkIs.setText("This work is licensed under");
        linkLicenceField = new Link(composite_19, SWT.NONE);
        linkLicenceField.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        linkLicenceField.setText("<a>Creative Commons Attribution 4.0 International License</a>");
        linkLicenceField.addSelectionListener(this);
*/
        new Label(composite_19, SWT.NONE);

        Label lblCredits = new Label(composite_19, SWT.NONE);
        lblCredits.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblCredits.setAlignment(SWT.CENTER);
        lblCredits.setText("Credits");
        lblCredits.setFont(SWTResourceManager.getBodyFont(20,SWT.BOLD));

        linkYersinia = new Link(composite_19, SWT.NONE);
        linkYersinia.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        linkYersinia.setText("<a>Yersinia Research Unit, Institut Pasteur, Paris</a>");
        linkYersinia.setFont(SWTResourceManager.getBodyFont(18,SWT.NORMAL));

        linkYersinia.addSelectionListener(this);
       
        new Label(composite_19, SWT.NONE);

        Composite composite_3 = new Composite(composite_19, SWT.NONE);
        GridData gd_composite_3 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);

        composite_3.setLayoutData(gd_composite_3);
        composite_3.setLayout(new GridLayout(1, false));
        
        Label lblAll = new Label(composite_3, SWT.NONE);
        lblAll.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblAll.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/logos.png"));
        
        new Label(composite_19, SWT.NONE);
        new Label(composite_19, SWT.NONE);
        
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
        NavigationManagement.pushStateView(InitViewStaphylococcus.ID);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnCoExpression) {
            partService.showPart(CoExprNetworkView.ID, PartState.ACTIVATE);
            NavigationManagement.pushStateView(CoExprNetworkView.ID);
        //}else if (e.getSource() == btnCO92) {
        //    GeneView.openGeneView(partService);
        }else if (e.getSource() == btnUSA300_ISMMS1) {
            GeneView.openUSA300_ISMMS1GeneView(partService);
        }else if (e.getSource() == btnNewman) {
            GeneView.openNewmanGeneView(partService);
        } else if (e.getSource() == btnUSA300_FPR3757) {
            GeneView.openUSA300_FPR3757GeneView(partService);
        } else if (e.getSource() == btnUSA300_TCH1516) {
            GeneView.openUSA300_TCH1516GeneView(partService);
        } else if (e.getSource() == btnEV76) {
            GeneView.openEV76GeneView(partService);
        } else if (e.getSource() == btnIP32953) {
            GeneView.openIP32953GeneView(partService);
        } else if (e.getSource() == btnYPIII) {
            GeneView.openYPIIIGeneView(partService);
        } else if (e.getSource() == btnIP31758) {
            GeneView.openIP31758GeneView(partService);
        } else if (e.getSource() == btnY11) {
            GeneView.openY11GeneView(partService);
        } else if (e.getSource() == btnY1) {
            GeneView.openY1GeneView(partService);
        } else if (e.getSource() == btn8081) {
            GeneView.open8081GeneView(partService);
        } else if (e.getSource() == btnIP38326) {
            GeneView.openIP38326GeneView(partService);
        } else if (e.getSource() == btnWA) {
            GeneView.openWAGeneView(partService);
        } else if (e.getSource() == btnIP38023) {
            GeneView.openIP38023GeneView(partService);
        } else if (e.getSource() == btnIP37485) {
            GeneView.openIP37485GeneView(partService);
        } else if (e.getSource() == btnIP37574) {
            GeneView.openIP37574GeneView(partService);
        } else if (e.getSource() == btnSC09) {
            GeneView.openSC09GeneView(partService);
        } else if (e.getSource() == btnQMA0440) {
            GeneView.openQMA0440GeneView(partService);
        } else if (e.getSource() == btnMH96) {
            GeneView.openMH96GeneView(partService);
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
            String url = "https://journals.asm.org/doi/full/10.1128/spectrum.03826-22";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == linkPubli2) {
            String url = "https://journals.asm.org/doi/full/10.1128/spectrum.03826-22";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == linkYersinia) {
            String url = "https://research.pasteur.fr/en/team/yersinia/";
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
        } else if (e.getSource() == btnDownloadData) {
            //String url = "https://yersiniomics.pasteur.fr/Download/";
            //NavigationManagement.openURLInExternalBrowser(url, partService);
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
