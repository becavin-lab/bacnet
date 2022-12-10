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
    private Button btnPestoides;
    private Button btnYPIII;
    private Button btnIP32953;
    private Button btnIP31758;
    private Button btnPB1;
    private Button btnY11;
    private Button btnY1;
    private Button btn8081;
    private Button btnWA;
    private Button btnQMA0440;
    private Button btn91001;
    private Button btnMH96;
    private Button btnIP38326;
    private Button btnIP38023;
    private Button btnIP37485;
    private Button btnIP37574;
    private Button btnCoExpression;
    private Button btnLoadData;
    private Button btnGeneView;
    private Button btnAccessWiki;
    private Link linkPubli;
    private Link linkYersinia;
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
        composite_Intro.setLayout(new GridLayout(1, false));
        GridData gd_composite_Intro = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        gd_composite_Intro.widthHint = 1800;
        composite_Intro.setLayoutData(gd_composite_Intro);
        // composite_Intro.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        new Label(composite_Intro, SWT.WRAP);

        Label lblListeriomicsIsSo = new Label(composite_Intro, SWT.WRAP);
        lblListeriomicsIsSo.setAlignment(SWT.CENTER);
        // lblListeriomicsIsSo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblListeriomicsIsSo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
        // lblListeriomicsIsSo.setData(RWT.MARKUP_ENABLED, Boolean.TRUE );
        // lblListeriomicsIsSo.setText("Systems biology of the model pathogen
        // <i>yersinia</i>");
        // lblListeriomicsIsSo.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
        lblListeriomicsIsSo.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/ToolBar/LogoYersiniomics2.png"));
        //new Label(composite_Intro, SWT.NONE);
        new Label(composite_Intro, SWT.WRAP);

        Label lblNewLabel_1 = new Label(composite_Intro, SWT.WRAP);
        RWTUtils.setMarkup(lblNewLabel_1);
        lblNewLabel_1.setAlignment(SWT.CENTER);
        GridData gd_lblNewLabel_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblNewLabel_1.widthHint = 1500;
        lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
        lblNewLabel_1.setText(
                "Yersiniomics integrates all complete genomes, transcriptomes and proteomes published for <i>Yersinia</i> "
                        + "species to date. It allows navigating among all these datasets with enriched metadata in a user-friendly format. "
                        + "Use Yersiniomics for deciphering regulatory mechanisms of your genome element of interest.\r");
        // lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblNewLabel_1.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        new Label(composite_Intro, SWT.WRAP);

        Composite composite_11 = new Composite(composite, SWT.BORDER);
        GridData gd_composite_11 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_composite_11.widthHint = 1800;
        composite_11.setLayoutData(gd_composite_11);
        composite_11.setLayout(new GridLayout(5, false));
        composite_11.setBackground(BasicColor.LIGHT_ONE);

        Composite composite_12 = new Composite(composite_11, SWT.NONE);
        composite_12.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        composite_12.setLayout(new GridLayout(1, false));
        composite_12.setBackground(BasicColor.LIGHT_ONE);
        //GridData gd_composite_12 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        //gd_composite_12.widthHint = 300;
        
        Label lblFastAccessTo = new Label(composite_12, SWT.WRAP);
        GridData gd_lblFastAccessTo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblFastAccessTo.widthHint = 330;
        lblFastAccessTo.setLayoutData(gd_lblFastAccessTo);
        lblFastAccessTo.setAlignment(SWT.CENTER);
        lblFastAccessTo.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
        lblFastAccessTo.setBackground(BasicColor.LIGHT_ONE);
        lblFastAccessTo.setForeground(BasicColor.BLACK);
        lblFastAccessTo.setText("Reference genomes quick access");
        

        Label lblinfo = new Label(composite_12, SWT.WRAP);
        RWTUtils.setMarkup(lblinfo);
        lblinfo.setAlignment(SWT.CENTER);
        lblinfo.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblinfo.setBackground(BasicColor.LIGHT_ONE);
        lblinfo.setForeground(BasicColor.BLACK);

        GridData gd_lblinfo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblinfo.widthHint = 330;
        lblinfo.setLayoutData(gd_lblinfo);
        lblinfo.setText("Access to all information about <i>Yersinia</i> species genes in reference genomes: "
                + "functional annotation, gene conservation, synteny, expression atlas and protein atlas. ");
        
        
        //Label lblImage = new Label(composite_12, SWT.BORDER);
        //lblImage.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/InitPage/genomeView.png"));
        Composite composite_01 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_01 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        
        composite_01.setLayoutData(gd_composite_01);
        composite_01.setSize(480, 109);
        composite_01.setLayout(new GridLayout(1, false));
        composite_01.setBackground(BasicColor.LIGHT_ONE);
        
        

        //ToolBar toolBar = new ToolBar(composite_1, SWT.FLAT | SWT.RIGHT);
        Label lblPestis = new Label(composite_01, SWT.WRAP);
        RWTUtils.setMarkup(lblPestis);
        lblPestis.setAlignment(SWT.CENTER);
        GridData gd_lblPestis = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        gd_lblPestis.widthHint = 330;
        lblPestis.setLayoutData(gd_lblPestis);
        lblPestis.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblPestis.setBackground(BasicColor.LIGHT_ONE);
        lblPestis.setForeground(BasicColor.BLACK);
        lblPestis.setText("<i>Y. pestis</i>");
        
        Composite composite_1 = new Composite(composite_01, SWT.NONE);
        GridData gd_composite_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_1.widthHint = 100;

        //gd_composite_1.heightHint = 115;
        composite_1.setLayoutData(gd_composite_1);
        composite_1.setSize(480, 109);
        composite_1.setLayout(new GridLayout(3, false));
        composite_1.setBackground(BasicColor.LIGHT_ONE);
        
        btnCO92 = new Button(composite_1, SWT.BORDER);
        btnCO92.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnCO92.setText("CO92");
        btnCO92.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnCO92.setBackground(BasicColor.DARK_ONE);
        //sbtnCO92.setForeground(BasicColor.BLACK);
        btnCO92.addSelectionListener(this);
        
        btnKIM = new Button(composite_1, SWT.BORDER);
        btnKIM.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnKIM.setText("KIM");
        btnKIM.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnKIM.setBackground(BasicColor.DARK_ONE);
        //btnKIM.setForeground(BasicColor.BLACK);
        btnKIM.addSelectionListener(this);
        
        btn91001 = new Button(composite_1, SWT.BORDER);
        btn91001.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btn91001.addSelectionListener(this);
        btn91001.setText("91001");
        btn91001.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btn91001.setBackground(BasicColor.DARK_ONE);
        
        Composite composite_001 = new Composite(composite_01, SWT.NONE);
        GridData gd_composite_001 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_1.widthHint = 100;

        //gd_composite_1.heightHint = 115;
        composite_001.setLayoutData(gd_composite_001);
        composite_001.setSize(480, 109);
        composite_001.setLayout(new GridLayout(1, false));
        composite_001.setBackground(BasicColor.LIGHT_ONE);
        
        btnPestoides = new Button(composite_001, SWT.BORDER);
        btnPestoides.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnPestoides.setText("Pestoides F");
        btnPestoides.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnPestoides.setBackground(BasicColor.DARK_ONE);
        //btnKIM.setForeground(BasicColor.BLACK);
        btnPestoides.addSelectionListener(this);
        
        
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
        Composite composite_023 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_023 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_23.widthHint = 250;

        //gd_composite_23.heightHint = 115;
        composite_023.setLayoutData(gd_composite_023);
        composite_023.setLayout(new GridLayout(1, false));
        composite_023.setBackground(BasicColor.LIGHT_ONE);
        composite_023.setForeground(BasicColor.BLACK);
        
        
        Label lblPseudo = new Label(composite_023, SWT.WRAP);
        RWTUtils.setMarkup(lblPseudo);
        lblPseudo.setAlignment(SWT.CENTER);
        GridData gd_lblPseudo = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        gd_lblPseudo.widthHint = 330;
        lblPseudo.setLayoutData(gd_lblPseudo);
        lblPseudo.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblPseudo.setBackground(BasicColor.LIGHT_ONE);
        lblPseudo.setForeground(BasicColor.BLACK);
        lblPseudo.setText("<i>Y. pseudotuberculosis</i>");
        
        Composite composite_00023 = new Composite(composite_023, SWT.NONE);
        GridData gd_composite_00023 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_23.widthHint = 250;

        //gd_composite_23.heightHint = 115;
        composite_00023.setLayoutData(gd_composite_00023);
        composite_00023.setLayout(new GridLayout(1, false));
        composite_00023.setBackground(BasicColor.LIGHT_ONE);
        composite_00023.setForeground(BasicColor.BLACK);
        
        Label lblYpstb = new Label(composite_00023, SWT.WRAP);
        RWTUtils.setMarkup(lblYpstb);
        lblYpstb.setAlignment(SWT.CENTER);
        GridData gd_lblYpstb = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblYpstb.widthHint = 200;
        lblYpstb.setLayoutData(gd_lblYpstb);
        lblYpstb.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblYpstb.setBackground(BasicColor.LIGHT_ONE);
        lblYpstb.setForeground(BasicColor.BLACK);
        lblYpstb.setText("Classical strains");

        Composite composite_23 = new Composite(composite_023, SWT.NONE);
        GridData gd_composite_23 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_23.widthHint = 250;

        //gd_composite_23.heightHint = 115;
        composite_23.setLayoutData(gd_composite_23);
        composite_23.setLayout(new GridLayout(2, false));
        composite_23.setBackground(BasicColor.LIGHT_ONE);
        composite_23.setForeground(BasicColor.BLACK);
        
       
        btnYPIII = new Button(composite_23, SWT.BORDER);
        btnYPIII.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnYPIII.addSelectionListener(this);
        btnYPIII.setText("YPIII");
        btnYPIII.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnYPIII.setBackground(BasicColor.DARK_ONE);
        
        btnIP32953 = new Button(composite_23, SWT.BORDER);
        btnIP32953.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnIP32953.addSelectionListener(this);
        btnIP32953.setText("IP32953");
        btnIP32953.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnIP32953.setBackground(BasicColor.DARK_ONE);
        
        Composite composite_0023 = new Composite(composite_023, SWT.NONE);
        GridData gd_composite_0023 = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        //gd_composite_14.widthHint = 100;
        composite_0023.setLayoutData(gd_composite_0023);
        composite_0023.setLayout(new GridLayout(1, false));
        composite_0023.setBackground(BasicColor.LIGHT_ONE);
        composite_0023.setForeground(BasicColor.BLACK);        
        
        Label lblFESLF = new Label(composite_0023, SWT.WRAP);
        RWTUtils.setMarkup(lblFESLF);
        lblFESLF.setAlignment(SWT.CENTER);
        GridData gd_lblFESLF = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblFESLF.widthHint = 200;
        lblFESLF.setLayoutData(gd_lblFESLF);
        lblFESLF.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblFESLF.setBackground(BasicColor.LIGHT_ONE);
        lblFESLF.setForeground(BasicColor.BLACK);
        lblFESLF.setText("Far East scarlet-like fever strain");
        
        
        btnIP31758 = new Button(composite_0023, SWT.BORDER);
        btnIP31758.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnIP31758.addSelectionListener(this);
        btnIP31758.setText("IP31758");
        btnIP31758.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnIP31758.setBackground(BasicColor.DARK_ONE);


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
        
        Composite composite_15 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_15 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_15.widthHint = 500;

        composite_15.setLayoutData(gd_composite_15);
        composite_15.setLayout(new GridLayout(1, false));
        composite_15.setBackground(BasicColor.LIGHT_ONE);
        composite_15.setForeground(BasicColor.BLACK);
        
        Label lblEntero = new Label(composite_15, SWT.WRAP);
        RWTUtils.setMarkup(lblEntero);
        lblEntero.setAlignment(SWT.CENTER);
        GridData gd_lblEntero = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblEntero.widthHint = 480;
        lblEntero.setLayoutData(gd_lblEntero);
        lblEntero.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblEntero.setBackground(BasicColor.LIGHT_ONE);
        lblEntero.setForeground(BasicColor.BLACK);
        lblEntero.setText("<i>Y. enterocolitica</i>");
        
        /* private version
        Composite composite_13 = new Composite(composite_15, SWT.NONE);
        GridData gd_composite_13 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_13.widthHint = 100;
        composite_13.setLayoutData(gd_composite_13);
        composite_13.setLayout(new GridLayout(3, false));
        composite_13.setBackground(BasicColor.LIGHT_ONE);
        composite_13.setForeground(BasicColor.BLACK);
        
        Composite composite_013 = new Composite(composite_13, SWT.NONE);
        GridData gd_composite_013 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_13.widthHint = 100;
        composite_013.setLayoutData(gd_composite_013);
        composite_013.setLayout(new GridLayout(1, false));
        composite_013.setBackground(BasicColor.LIGHT_ONE);
        composite_013.setForeground(BasicColor.BLACK);
        
        Label lblEnteroGen235a = new Label(composite_013, SWT.WRAP);
        RWTUtils.setMarkup(lblEnteroGen235a);
        lblEnteroGen235a.setAlignment(SWT.CENTER);
        GridData gd_lblEnteroGen235a = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblEnteroGen235a.widthHint = 100;
        lblEnteroGen235a.setLayoutData(gd_lblEnteroGen235a);
        lblEnteroGen235a.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblEnteroGen235a.setBackground(BasicColor.LIGHT_ONE);
        lblEnteroGen235a.setForeground(BasicColor.BLACK);
        lblEnteroGen235a.setText("Genotype 2/3-5a");
        
        btnIP38326 = new Button(composite_013, SWT.BORDER);
        btnIP38326.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnIP38326.addSelectionListener(this);
        btnIP38326.setText("IP38326");
        //btnIP38326.setText("Coming soon");
        btnIP38326.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnIP38326.setBackground(BasicColor.DARK_ONE);
        
        
        Composite composite_0013 = new Composite(composite_13, SWT.NONE);
        GridData gd_composite_0013 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_13.widthHint = 100;
        composite_0013.setLayoutData(gd_composite_0013);
        composite_0013.setLayout(new GridLayout(1, false));
        composite_0013.setBackground(BasicColor.LIGHT_ONE);
        composite_0013.setForeground(BasicColor.BLACK);
        
        
        Label lblEnteroGen239a = new Label(composite_0013, SWT.WRAP);
        RWTUtils.setMarkup(lblEnteroGen239a);
        lblEnteroGen239a.setAlignment(SWT.CENTER);
        GridData gd_lblEnteroGen239a = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblEnteroGen239a.widthHint = 100;
        lblEnteroGen239a.setLayoutData(gd_lblEnteroGen239a);
        lblEnteroGen239a.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblEnteroGen239a.setBackground(BasicColor.LIGHT_ONE);
        lblEnteroGen239a.setForeground(BasicColor.BLACK);
        lblEnteroGen239a.setText("Genotype 2/3-9a");
        
        btnIP38023 = new Button(composite_0013, SWT.BORDER);
        btnIP38023.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnIP38023.addSelectionListener(this);
        btnIP38023.setText("IP38023");
        //btnIP38023.setText("Coming soon");
        btnIP38023.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnIP38023.setBackground(BasicColor.DARK_ONE);
        
        Composite composite_00013 = new Composite(composite_13, SWT.NONE);
        GridData gd_composite_00013 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_13.widthHint = 100;
        composite_00013.setLayoutData(gd_composite_00013);
        composite_00013.setLayout(new GridLayout(1, false));
        composite_00013.setBackground(BasicColor.LIGHT_ONE);
        composite_00013.setForeground(BasicColor.BLACK);
        
        Label lblEnteroGen239b = new Label(composite_00013, SWT.WRAP);
        RWTUtils.setMarkup(lblEnteroGen239b);
        lblEnteroGen239b.setAlignment(SWT.CENTER);
        GridData gd_lblEnteroGen239b = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblEnteroGen239b.widthHint = 100;
        lblEnteroGen239b.setLayoutData(gd_lblEnteroGen239b);
        lblEnteroGen239b.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblEnteroGen239b.setBackground(BasicColor.LIGHT_ONE);
        lblEnteroGen239b.setForeground(BasicColor.BLACK);
        lblEnteroGen239b.setText("Genotype 2/3-9b");
        
        btnIP37485 = new Button(composite_00013, SWT.BORDER);
        btnIP37485.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnIP37485.addSelectionListener(this);
        btnIP37485.setText("IP37485");
        //btnIP37485.setText("Coming soon");
        btnIP37485.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnIP37485.setBackground(BasicColor.DARK_ONE);
        // Composite composite_9 = new Composite(composite_24, SWT.NONE);
        */
        
        Composite composite_9 = new Composite(composite_15, SWT.NONE);
        GridData gd_composite_9 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_9.widthHint = 100;
        composite_9.setLayoutData(gd_composite_9);
        composite_9.setLayout(new GridLayout(2, false));
        composite_9.setBackground(BasicColor.LIGHT_ONE);
        composite_9.setForeground(BasicColor.BLACK);
        
        Composite composite_09 = new Composite(composite_9, SWT.NONE);
        GridData gd_composite_09 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_9.widthHint = 100;
        composite_09.setLayoutData(gd_composite_09);
        composite_09.setLayout(new GridLayout(1, false));
        composite_09.setBackground(BasicColor.LIGHT_ONE);
        composite_09.setForeground(BasicColor.BLACK);


        
        
        Composite composite_24 = new Composite(composite_15, SWT.NONE);
        GridData gd_composite_24 = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        composite_24.setLayoutData(gd_composite_24);
        composite_24.setLayout(new GridLayout(3, false));
        composite_24.setBackground(BasicColor.LIGHT_ONE);
        composite_24.setForeground(BasicColor.BLACK);        
        
        

        Label lblEnteroGen1B = new Label(composite_09, SWT.WRAP);
        RWTUtils.setMarkup(lblEnteroGen1B);
        lblEnteroGen1B.setAlignment(SWT.CENTER);
        GridData gd_lblEntero1B = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblEntero1B.widthHint = 130;
        lblEnteroGen1B.setLayoutData(gd_lblEntero1B);
        lblEnteroGen1B.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblEnteroGen1B.setBackground(BasicColor.LIGHT_ONE);
        lblEnteroGen1B.setForeground(BasicColor.BLACK);
        lblEnteroGen1B.setText("Genotype 1B");
        
        Composite composite_009 = new Composite(composite_09, SWT.NONE);
        GridData gd_composite_009 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_9.widthHint = 100;
        composite_009.setLayoutData(gd_composite_009);
        composite_009.setLayout(new GridLayout(1, false));
        composite_009.setBackground(BasicColor.LIGHT_ONE);
        composite_009.setForeground(BasicColor.BLACK);

        btn8081 = new Button(composite_009, SWT.BORDER);
        btn8081.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btn8081.addSelectionListener(this);
        btn8081.setText("8081");
        btn8081.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btn8081.setBackground(BasicColor.DARK_ONE);

        btnWA = new Button(composite_09, SWT.BORDER);
        btnWA.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnWA.addSelectionListener(this);
        btnWA.setText("WA");
        btnWA.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnWA.setBackground(BasicColor.DARK_ONE);
      
        
        Composite composite_14 = new Composite(composite_9, SWT.NONE);
        GridData gd_composite_14 = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        //gd_composite_14.widthHint = 100;
        composite_14.setLayoutData(gd_composite_14);
        composite_14.setLayout(new GridLayout(1, false));
        composite_14.setBackground(BasicColor.LIGHT_ONE);
        composite_14.setForeground(BasicColor.BLACK);        
        
        Label lblGen4 = new Label(composite_14, SWT.WRAP);
        RWTUtils.setMarkup(lblGen4);
        lblGen4.setAlignment(SWT.CENTER);
        GridData gd_lblGen4 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblGen4.widthHint = 130;
        lblGen4.setLayoutData(gd_lblGen4);
        lblGen4.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGen4.setBackground(BasicColor.LIGHT_ONE);
        lblGen4.setForeground(BasicColor.BLACK);
        lblGen4.setText("Genotype 4");
        
        Composite composite_114 = new Composite(composite_14, SWT.NONE);
        GridData gd_composite_114 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        composite_114.setLayoutData(gd_composite_114);
        // for private version
        // composite_114.setLayout(new GridLayout(2, false));
        // for public version
        composite_114.setLayout(new GridLayout(1, false));
        composite_114.setBackground(BasicColor.LIGHT_ONE);
        composite_114.setForeground(BasicColor.BLACK);
        
        btnY1 = new Button(composite_114, SWT.BORDER);
        btnY1.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnY1.addSelectionListener(this);
        btnY1.setText("Y1");
        btnY1.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnY1.setBackground(BasicColor.DARK_ONE);
        
        // private version
        // btnY11 = new Button(composite_114, SWT.BORDER);
        // public version
        btnY11 = new Button(composite_14, SWT.BORDER);
        btnY11.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnY11.addSelectionListener(this);
        btnY11.setText("Y11");
        btnY11.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnY11.setBackground(BasicColor.DARK_ONE);
        
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
        
        /* private version
        btnIP37574 = new Button(composite_14, SWT.BORDER);
        btnIP37574.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnIP37574.addSelectionListener(this);
        btnIP37574.setText("IP37574");
       // btnIP37574.setText("Coming soon");
        btnIP37574.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnIP37574.setBackground(BasicColor.DARK_ONE);
        */
        
        Composite composite_20 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_20 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_9.widthHint = 100;
        composite_20.setLayoutData(gd_composite_20);
        composite_20.setLayout(new GridLayout(1, false));
        composite_20.setBackground(BasicColor.LIGHT_ONE);
        composite_20.setForeground(BasicColor.BLACK);
        
        Composite composite_020 = new Composite(composite_20, SWT.NONE);
        GridData gd_composite_020 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_9.widthHint = 100;
        composite_020.setLayoutData(gd_composite_020);
        composite_020.setLayout(new GridLayout(1, false));
        composite_020.setBackground(BasicColor.LIGHT_ONE);
        composite_020.setForeground(BasicColor.BLACK);
        
        Label lblRuck = new Label(composite_020, SWT.WRAP);
        RWTUtils.setMarkup(lblRuck);
        lblRuck.setAlignment(SWT.CENTER);
        GridData gd_lblRuck = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblRuck.widthHint = 200;
        lblRuck.setLayoutData(gd_lblRuck);
        lblRuck.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblRuck.setBackground(BasicColor.LIGHT_ONE);
        lblRuck.setForeground(BasicColor.BLACK);
        lblRuck.setText("<i>Y. ruckeri</i>");
        
        btnQMA0440 = new Button(composite_020, SWT.BORDER);
        btnQMA0440.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnQMA0440.addSelectionListener(this);
        btnQMA0440.setText("QMA0440");
        btnQMA0440.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnQMA0440.setBackground(BasicColor.DARK_ONE);
        
        Composite composite_0020 = new Composite(composite_20, SWT.NONE);
        GridData gd_composite_0020 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_9.widthHint = 100;
        composite_0020.setLayoutData(gd_composite_0020);
        composite_0020.setLayout(new GridLayout(1, false));
        composite_0020.setBackground(BasicColor.LIGHT_ONE);
        composite_0020.setForeground(BasicColor.BLACK);
        
        Label lblEntomo = new Label(composite_0020, SWT.WRAP);
        
        RWTUtils.setMarkup(lblEntomo);
        lblEntomo.setAlignment(SWT.CENTER);
        GridData gd_lblEntomo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblEntomo.widthHint = 200;
        lblEntomo.setLayoutData(gd_lblEntomo);
        lblEntomo.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        lblEntomo.setBackground(BasicColor.LIGHT_ONE);
        lblEntomo.setForeground(BasicColor.BLACK);
        lblEntomo.setText("<i>Y. entomophaga</i>");
       
        btnMH96 = new Button(composite_0020, SWT.BORDER);
        btnMH96.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnMH96.addSelectionListener(this);
        btnMH96.setText("MH96");
        btnMH96.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        btnMH96.setBackground(BasicColor.DARK_ONE);
        
     
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
        gd_composite_6.widthHint = 1800;
        composite_6.setLayoutData(gd_composite_6);
        composite_6.setLayout(new GridLayout(4, false));
        composite_6.setBackground(BasicColor.LIGHT_TWO);
        Composite composite_8 = new Composite(composite_6, SWT.NONE);
        composite_8.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        composite_8.setLayout(new GridLayout(1, false));
        composite_8.setBackground(BasicColor.LIGHT_TWO);
        Label lblBrowseOmicsDatasets = new Label(composite_8, SWT.WRAP);
        lblBrowseOmicsDatasets.setAlignment(SWT.CENTER);
        GridData gd_lblBrowseOmicsDatasets = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblBrowseOmicsDatasets.widthHint = 330;
        lblBrowseOmicsDatasets.setLayoutData(gd_lblBrowseOmicsDatasets);
        lblBrowseOmicsDatasets.setText("Browse omics datasets");
        lblBrowseOmicsDatasets.setForeground(BasicColor.BLACK);
        lblBrowseOmicsDatasets.setFont(SWTResourceManager.getTitleFont());
        lblBrowseOmicsDatasets.setBackground(BasicColor.LIGHT_TWO);

        /*
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
        */
        
        Composite composite_10 = new Composite(composite_6, SWT.NONE);
        GridData gd_composite_10 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        //gd_composite_10.heightHint = 100;
        composite_10.setLayoutData(gd_composite_10);
        composite_10.setLayout(new GridLayout(1, false));
        composite_10.setBackground(BasicColor.LIGHT_TWO);
        btnGenomics = new Button(composite_10, SWT.BORDER);
        btnGenomics.setText("Genomics browser");
        btnGenomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnGenomics.setFont(SWTResourceManager.getBodyFont(20,SWT.NORMAL));
        btnGenomics.addSelectionListener(this);
        btnGenomics.setBackground(BasicColor.DARK_TWO);

        Label lblGoThroughAll = new Label(composite_10, SWT.WRAP);
        RWTUtils.setMarkup(lblGoThroughAll);
        lblGoThroughAll.setAlignment(SWT.CENTER);
        GridData gd_lblGoThroughAll = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblGoThroughAll.widthHint = 460;
        lblGoThroughAll.setLayoutData(gd_lblGoThroughAll);
        lblGoThroughAll.setText("Browse all 194 <i>Yersinia</i> complete genomes available on Yersiniomics."
                + " Visualize strain relationship in a phylogenomic tree. Access to all their annotated genome elements.");
        lblGoThroughAll.setFont(SWTResourceManager.getBodyFont(15,SWT.NORMAL));
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
        btnTranscriptomics.setFont(SWTResourceManager.getBodyFont(20,SWT.NORMAL));
        btnTranscriptomics.addSelectionListener(this);
        btnTranscriptomics.setBackground(BasicColor.DARK_TWO);

        Label lblGoThroughAll_1 = new Label(composite_21, SWT.WRAP);
        RWTUtils.setMarkup(lblGoThroughAll_1);
        lblGoThroughAll_1.setAlignment(SWT.CENTER);
        GridData gd_lblGoThroughAll_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblGoThroughAll_1.widthHint = 460;
        lblGoThroughAll_1.setLayoutData(gd_lblGoThroughAll_1);
        lblGoThroughAll_1
                .setText("Browse all 253 <i>Yersinia</i> species transcriptomics datasets available on Yersiniomics. "
                        + "Visualize them on the genome browser. Extract differently expressed genome elements and display their fold changes in a heatmap viewer.");
        lblGoThroughAll_1.setFont(SWTResourceManager.getBodyFont(15,SWT.NORMAL));
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
        btnProteomics.setFont(SWTResourceManager.getBodyFont(20,SWT.NORMAL));
        btnProteomics.addSelectionListener(this);
        btnProteomics.setBackground(BasicColor.DARK_TWO);

        Label lblGoThroughAll_2 = new Label(composite_22, SWT.WRAP);
        RWTUtils.setMarkup(lblGoThroughAll_2);
        lblGoThroughAll_2.setAlignment(SWT.CENTER);
        GridData gd_lblGoThroughAll_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblGoThroughAll_2.widthHint = 460;
        lblGoThroughAll_2.setLayoutData(gd_lblGoThroughAll_2);
        lblGoThroughAll_2
                .setText("Browse all 76 <i>Yersinia</i> species proteomics datasets available on Yersiniomics. "
                        + "Visualize them on the genome browser. Display protein detection patterns for each datasets in a heatmap viewer.");
        lblGoThroughAll_2.setFont(SWTResourceManager.getBodyFont(15,SWT.NORMAL));
        lblGoThroughAll_2.setBackground(BasicColor.LIGHT_TWO);
        lblGoThroughAll_2.setForeground(BasicColor.BLACK);

        /*
        Composite composite_16 = new Composite(composite, SWT.BORDER);
        GridData gd_composite_16 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_composite_16.widthHint = 1800;
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
        gd_lblListeriomicsSpecificTools.widthHint = 330;
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
        btnCoExpression.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnCoExpression.setBackground(BasicColor.DARK_THREE);

        Label lblExpressionAtlas_1 = new Label(composite_2, SWT.WRAP);
        lblExpressionAtlas_1.setAlignment(SWT.CENTER);
        GridData gd_lblExpressionAtlas_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblExpressionAtlas_1.widthHint = 460;
        lblExpressionAtlas_1.setLayoutData(gd_lblExpressionAtlas_1);
        lblExpressionAtlas_1.setText("Access to the co-expression network tool to search for potential regulations");
        lblExpressionAtlas_1.setFont(SWTResourceManager.getBodyFont(15,SWT.NORMAL));
        lblExpressionAtlas_1.setBackground(BasicColor.LIGHT_THREE);
        lblExpressionAtlas_1.setForeground(BasicColor.BLACK);
*/
        Composite composite_18 = new Composite(composite, SWT.NONE);
        composite_18.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        //composite_18.setBackground(BasicColor.LIGHT_THREE);
        composite_18.setLayout(new GridLayout(1, false));
        btnLoadData = new Button(composite_18, SWT.BORDER | SWT.CENTER);
        btnLoadData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnLoadData.setText(" Load data selection");
        btnLoadData.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/txtload.bmp"));
        btnLoadData.setFont(SWTResourceManager.getBodyFont(20,SWT.NORMAL));
        //btnLoadData.setBackground(BasicColor.DARK_THREE);

        Label lblLoadAPrevious = new Label(composite_18, SWT.WRAP);
        lblLoadAPrevious.setAlignment(SWT.CENTER);
        lblLoadAPrevious.setFont(SWTResourceManager.getBodyFont(15,SWT.NORMAL));
        GridData gd_lblLoadAPrevious = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_lblLoadAPrevious.widthHint = 460;
        lblLoadAPrevious.setLayoutData(gd_lblLoadAPrevious);
        lblLoadAPrevious.setText("Load a previous genome viewer visualization saved in .gview file");
        //lblLoadAPrevious.setBackground(BasicColor.LIGHT_THREE);
        lblLoadAPrevious.setForeground(BasicColor.BLACK);
        btnLoadData.addSelectionListener(this);
        new Label(composite_18, SWT.NONE);

        /*
        Composite composite_5 = new Composite(composite_16, SWT.NONE);
        composite_5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        composite_5.setLayout(new GridLayout(1, false));
        composite_5.setBackground(BasicColor.LIGHT_THREE);
        btnAccessWiki = new Button(composite_5, SWT.BORDER);
        btnAccessWiki.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnAccessWiki.setText("Access Yersiniomics wiki");
        btnAccessWiki.setFont(SWTResourceManager.getBodyFont(22,SWT.NORMAL));
        btnAccessWiki.addSelectionListener(this);
        btnAccessWiki.setBackground(BasicColor.DARK_THREE);

        Label lblGoToThe = new Label(composite_5, SWT.WRAP);
        lblGoToThe.setAlignment(SWT.CENTER);
        lblGoToThe.setFont(SWTResourceManager.getBodyFont(15,SWT.NORMAL));
        GridData gd_lblGoToThe = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblGoToThe.widthHint = 460;
        lblGoToThe.setLayoutData(gd_lblGoToThe);
        lblGoToThe.setText(
                "Go to the Yersiniomics wiki page for tutorials and description of the different tools included in Yersiniomics. "
                        + "Be careful, it might not display if you disallow your internet browser to display pop-up webpage.");
        lblGoToThe.setBackground(BasicColor.LIGHT_THREE);
        lblGoToThe.setForeground(BasicColor.BLACK);
*/
        //new Label(composite_16, SWT.NONE);
        //new Label(composite_16, SWT.NONE);

        Composite composite_19 = new Composite(composite, SWT.NONE);
        composite_19.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        composite_19.setLayout(new GridLayout(1, false));

        Label lblLastUpdate = new Label(composite_19, SWT.NONE);
        lblLastUpdate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        lblLastUpdate.setText("Last update: December 2022");
        lblLastUpdate.setFont(SWTResourceManager.getBodyFont(20,SWT.BOLD));

        new Label(composite_19, SWT.NONE);
        linkPubli = new Link(composite_19, SWT.NONE);
        linkPubli.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        linkPubli.setText("<a>Cite Yersiniomics</a>");
        linkPubli.setFont(SWTResourceManager.getBodyFont(18,SWT.NORMAL));

        linkPubli.addSelectionListener(this);

        new Label(composite_19, SWT.NONE);
        Link link = new Link(composite_19, SWT.NONE);
        link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        link.setText("Contact us if you have a recently published \"omics\" dataset you want to be integrated to Yersiniomics:  <a href=\"mailto:yersiniomics@pasteur.fr\">yersiniomics@pasteur.fr</a>");
        link.setFont(SWTResourceManager.getBodyFont(18,SWT.NORMAL));

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
        //gd_composite_3.heightHint = 20;

        composite_3.setLayoutData(gd_composite_3);
        composite_3.setLayout(new GridLayout(4, false));

        Label lblPasteur = new Label(composite_3, SWT.NONE);
        lblPasteur.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblPasteur.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/Pasteur.png"));

        Label lblUP = new Label(composite_3, SWT.NONE);
        lblUP.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblUP.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/UP.png"));
       
        Label lblAID = new Label(composite_3, SWT.NONE);
        lblAID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblAID.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/AID.png"));
        
        Composite composite_31 = new Composite(composite_19, SWT.NONE);
        GridData gd_composite_31 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        //gd_composite_3.heightHint = 20;

        composite_31.setLayoutData(gd_composite_31);
        composite_31.setLayout(new GridLayout(5, false));
        
        Label lblCNRS= new Label(composite_31, SWT.NONE);
        lblCNRS.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/CNRS.png"));
        
        Label lblANR = new Label(composite_31, SWT.NONE);
        lblANR.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/ANR.png"));
        
        Label lblIBEID = new Label(composite_31, SWT.NONE);
        lblIBEID.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/IBEID.png"));
       
        Label lblFRM = new Label(composite_31, SWT.NONE);
        lblFRM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblFRM.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/FRM.png"));

        Label lblINCEPTION = new Label(composite_31, SWT.NONE);
        lblINCEPTION.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblINCEPTION.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/logos/INCEPTION.png"));
        
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
        } else if (e.getSource() == btn91001) {
            GeneView.open91001GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiCSF007(partService);
        } else if (e.getSource() == btnPestoides) {
            GeneView.openPestoidesGeneView(partService);
        } else if (e.getSource() == btnIP32953) {
            GeneView.openIP32953GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiIP32953(partService);
        } else if (e.getSource() == btnYPIII) {
            GeneView.openYPIIIGeneView(partService);
        	//GenomeTranscriptomeView.displayYersiYPIII(partService);
        } else if (e.getSource() == btnIP31758) {
            GeneView.openIP31758GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiIP32953(partService);
        } else if (e.getSource() == btnY11) {
            GeneView.openY11GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiY11(partService);
        } else if (e.getSource() == btnY1) {
            GeneView.openY1GeneView(partService);
        } else if (e.getSource() == btn8081) {
            GeneView.open8081GeneView(partService);
        	//GenomeTranscriptomeView.displayYersi8081(partService);
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
        } else if (e.getSource() == btnQMA0440) {
            GeneView.openQMA0440GeneView(partService);
        	//GenomeTranscriptomeView.displayYersiCSF007(partService);
    
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
            String url = "";
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
