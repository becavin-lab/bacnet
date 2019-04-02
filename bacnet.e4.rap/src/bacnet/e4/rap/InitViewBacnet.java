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
import org.eclipse.jface.dialogs.InputDialog;
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
import bacnet.Database;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Genome.OpenGenomesThread;
import bacnet.expressionAtlas.ProteomicsView;
import bacnet.expressionAtlas.TranscriptomicsView;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.genomeBrowser.NTerminomicsView;
import bacnet.raprcp.NavigationManagement;
import bacnet.sequenceTools.GeneView;
import bacnet.sequenceTools.GenomicsView;
import bacnet.sequenceTools.SrnaSummaryView;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.FileUtils;
import bacnet.utils.RWTUtils;
import bacnet.views.CoExprNetworkView;

public class InitViewBacnet implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3252983689419871498L;

    public static final String ID = "bacnet.InitViewBacnet"; //$NON-NLS-1$

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;
    private Label lblTzest;
    private Button btnListeriomics;
    private Button btnLeishomics;
    private Link linkUIBC;
    private Link linkPubli;
    private Link linkHUB;
    private Button btnCRISPR;
    private Button btnYersiniomics;
    private Link linkLicenceField;


    @Inject
    EPartService partService;

    @Inject
    EModelService modelService;


    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public InitViewBacnet() {}

    @PostConstruct
    public void createPartControl(Composite parent) {
        
        focused = true;
        parent.setLayout(new GridLayout(1, false));
        ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Composite composite_11 = new Composite(composite, SWT.BORDER);
        GridData gd_composite_11 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_composite_11.widthHint = 850;
        composite_11.setLayoutData(gd_composite_11);
        composite_11.setLayout(new GridLayout(2, false));
        composite_11.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Composite composite_9 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_9 = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
        gd_composite_9.widthHint = 425;
        gd_composite_9.heightHint = 200;
        composite_9.setLayoutData(gd_composite_9);
        composite_9.setLayout(new GridLayout(1, false));
        composite_9.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnListeriomics = new Button(composite_9, SWT.BORDER);
        GridData gd_btnListeriomics = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_btnListeriomics.heightHint = 100;
        btnListeriomics.setLayoutData(gd_btnListeriomics);
        btnListeriomics.addSelectionListener(this);
        btnListeriomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/LogoListeriomics.png"));


        Label lblIntracellularMouse = new Label(composite_9, SWT.WRAP);
        lblIntracellularMouse.setAlignment(SWT.CENTER);
        GridData gd_lblIntracellularMouse = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblIntracellularMouse.widthHint = 400;
        lblIntracellularMouse.setLayoutData(gd_lblIntracellularMouse);
        lblIntracellularMouse.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblIntracellularMouse.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblIntracellularMouse.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        lblIntracellularMouse.setText(
                "Listeriomics integrates all complete genomes, transcriptomes and proteomes published for <i>Listeria</i> "
                        + "species to date. It allows navigating among all these datasets with enriched metadata in a user-friendly format. "
                        + "Use Listeriomics for deciphering regulatory mechanisms of your genome element of interest.\r");
        gd_composite_11.widthHint = 850;
        gd_composite_9.heightHint = 180;

        Composite composite_15 = new Composite(composite_11, SWT.NONE);
        GridData gd_composite_15 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_composite_15.heightHint = 200;
        gd_composite_15.widthHint = 425;
        composite_15.setLayoutData(gd_composite_15);
        composite_15.setLayout(new GridLayout(1, false));
        composite_15.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        btnLeishomics = new Button(composite_15, SWT.BORDER);
        GridData gd_btnLeishomics = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        gd_btnLeishomics.heightHint = 100;
        btnLeishomics.setLayoutData(gd_btnLeishomics);
        // btnGeneView.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/SystemsBio.png"));
        btnLeishomics.addSelectionListener(this);
        btnLeishomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/LogoLeishomics.png"));

        Label lblinfo = new Label(composite_15, SWT.WRAP);
        lblinfo.setAlignment(SWT.CENTER);
        lblinfo.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblinfo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        GridData gd_lblinfo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblinfo.widthHint = 400;
        lblinfo.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        lblinfo.setLayoutData(gd_lblinfo);
        lblinfo.setText("Acces to the Omics viewer : Amastigote vs Promastigote. MultiOmics Viewer : DNA vs RNA vs Protein");



        Composite composite_6 = new Composite(composite, SWT.BORDER);
        GridData gd_composite_6 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_composite_6.widthHint = 850;
        composite_6.setLayoutData(gd_composite_6);
        composite_6.setLayout(new GridLayout(2, false));
        composite_6.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        Composite composite_10 = new Composite(composite_6, SWT.NONE);
        GridData gd_composite_10 = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        gd_composite_10.widthHint = 425;
        gd_composite_10.heightHint = 200;
        composite_10.setLayoutData(gd_composite_10);
        composite_10.setLayout(new GridLayout(1, false));
        composite_10.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        btnYersiniomics = new Button(composite_10, SWT.BORDER);
        GridData gd_btnYersiniomics = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_btnYersiniomics.heightHint = 100;
        btnYersiniomics.setLayoutData(gd_btnYersiniomics);
        btnYersiniomics.addSelectionListener(this);
        btnYersiniomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/LogoYersiniomics.png"));

        Label lblGoThroughAll = new Label(composite_10, SWT.WRAP);
        lblGoThroughAll.setAlignment(SWT.CENTER);
        GridData gd_lblGoThroughAll = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblGoThroughAll.widthHint = 400;
        lblGoThroughAll.setLayoutData(gd_lblGoThroughAll);
        lblGoThroughAll.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        lblGoThroughAll.setText(
                "Yersiniomics integrates all complete genomes, transcriptomes and proteomes published for <i>Yersinia</i> "
                        + "species to date. It allows navigating among all these datasets with enriched metadata in a user-friendly format. "
                        + "Use Yersiniomics for deciphering regulatory mechanisms of your genome element of interest.\r");
        lblGoThroughAll.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblGoThroughAll.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Composite composite_21 = new Composite(composite_6, SWT.NONE);
        GridData gd_composite_21 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        gd_composite_21.widthHint = 425;
        gd_composite_21.heightHint = 200;
        composite_21.setLayoutData(gd_composite_21);
        composite_21.setLayout(new GridLayout(1, false));
        composite_21.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        btnCRISPR = new Button(composite_21, SWT.BORDER);
        GridData gd_btnCRISPR = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_btnCRISPR.heightHint = 100;
        btnCRISPR.setLayoutData(gd_btnCRISPR);
        btnCRISPR.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
        btnCRISPR.addSelectionListener(this);
        btnCRISPR.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/LogoCRISPRbrowserMini.png"));

        Label lblGoThroughAll_1 = new Label(composite_21, SWT.WRAP);
        lblGoThroughAll_1.setAlignment(SWT.CENTER);
        GridData gd_lblGoThroughAll_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_lblGoThroughAll_1.widthHint = 400;
        lblGoThroughAll_1.setLayoutData(gd_lblGoThroughAll_1);
        lblGoThroughAll_1.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        lblGoThroughAll_1.setText("We performed a CRISPR-dCas9 screen using a pool of ~92,000 sgRNAs in "
                + "E. coli MG1655 during growth in rich medium for 17 generations. During this experiment, "
                + "guides that reduce the cell fitness, for instance by blocking the expression of essential "
                + "genes, are depleted from the library. For each guide, a log2-transformed fold change value"
                + " (log2FC) represents the enrichment or depletion over the course of the experiment.");
        lblGoThroughAll_1.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
        lblGoThroughAll_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Composite composite_19 = new Composite(composite, SWT.NONE);
        composite_19.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        composite_19.setLayout(new GridLayout(1, false));


        Label lblLastUpdate = new Label(composite_19, SWT.NONE);
        lblLastUpdate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
        lblLastUpdate.setText("Last update: December 2018");

        linkPubli = new Link(composite_19, SWT.NONE);
        linkPubli.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        linkPubli.setText("<a>Cite BACNET</a>");
        linkPubli.addSelectionListener(this);

        Link link = new Link(composite_19, SWT.NONE);
        link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        link.setText("Contact us: <a>listeriomics AT pasteur DOT fr</a>");

        Label label_1 = new Label(composite_19, SWT.NONE);

        Label lblImagelicence = new Label(composite_19, SWT.CENTER);
        lblImagelicence.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblImagelicence.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/cccommons.png"));

        Label lblThisWorkIs = new Label(composite_19, SWT.NONE);
        lblThisWorkIs.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblThisWorkIs.setAlignment(SWT.CENTER);
        lblThisWorkIs.setText("This work is licensed under");
        linkLicenceField = new Link(composite_19, SWT.NONE);
        linkLicenceField.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        linkLicenceField.setText("<a>Creative Commons Attribution 4.0 International License</a>");
        linkLicenceField.addSelectionListener(this);

        Label label = new Label(composite_19, SWT.NONE);

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
        lblPasteur.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/Pasteur.png"));

        Label lblInra = new Label(composite_3, SWT.NONE);
        lblInra.setSize(0, 15);
        lblInra.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblInra.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/INRA.png"));
        Label lblAnr = new Label(composite_3, SWT.NONE);
        lblAnr.setSize(0, 15);
        lblAnr.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/Logo ANR.png"));

        Label lblBacnet = new Label(composite_3, SWT.NONE);
        lblBacnet.setSize(0, 15);
        lblBacnet.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/investissement-davenir.png"));

        scrolledComposite.setContent(composite);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

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
        NavigationManagement.pushStateView(InitViewBacnet.ID);
    }


    @Override
    public void widgetSelected(SelectionEvent e) {
        if(e.getSource() == btnCRISPR) {
            String url = "https://crispr.pasteur.fr";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == btnListeriomics) {
            String url = "https://listeriomics.pasteur.fr";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == btnLeishomics) {
            String url = "https://leishomics.pasteur.fr";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == btnYersiniomics) {
            String url = "https://yersiniomics.pasteur.fr";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        }else if (e.getSource() == linkPubli) {
            String url = "";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == linkHUB) {
            String url = "https://research.pasteur.fr/en/team/bioinformatics-and-biostatistics-hub/";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        } else if (e.getSource() == linkLicenceField) {
            String url = "http://creativecommons.org/licenses/by/4.0/";
            NavigationManagement.openURLInExternalBrowser(url, partService);
        }
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
