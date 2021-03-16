package bacnet.e4.rap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import bacnet.expressionAtlas.HeatMapMultiOmicsView;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.raprcp.NavigationManagement;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;

public class InitViewLeishmania implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3252983689419871498L;

    public static final String ID = "bacnet.Leishmania"; //$NON-NLS-1$

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;

    private Button btnStat;
    private Button btnLeisheild;

    private ScrolledComposite scrolledComposite;

    @Inject
    EPartService partService;

    @Inject
    EModelService modelService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;
    private Composite composite_1;

    @Inject
    public InitViewLeishmania() {}

    @PostConstruct
    public void createPartControl(Composite parent) {
        focused = true;
        /**
         * Uncomment to add Password !!!!!!!!!!
         */
        // openPasswordDialog();
        // System.out.println("create init view");

        parent.setLayout(new GridLayout(1, false));
        scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Composite composite_6 = new Composite(composite, SWT.BORDER);
        composite_6.setLayout(new GridLayout(1, false));
        composite_6.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        composite_6.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        composite_1 = new Composite(composite_6, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        composite_1.setLayout(new GridLayout(2, false));
        composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Composite composite_7 = new Composite(composite_1, SWT.NONE);
        composite_7.setLayout(new GridLayout(1, false));
        composite_7.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnStat = new Button(composite_7, SWT.NONE);
        btnStat.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/LeishHome.png"));
        btnStat.addSelectionListener(this);

        Label lblStationnaryPhaseData = new Label(composite_7, SWT.NONE);
        lblStationnaryPhaseData.setAlignment(SWT.CENTER);
        lblStationnaryPhaseData.setText("Omics viewer : Amastigote vs Promastigote");
        lblStationnaryPhaseData.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblStationnaryPhaseData.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Composite composite_17 = new Composite(composite_1, SWT.NONE);
        composite_17.setLayout(new GridLayout(1, false));
        composite_17.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnLeisheild = new Button(composite_17, SWT.NONE);
        btnLeisheild.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/LeishHeatmap.png"));
        btnLeisheild.addSelectionListener(this);

        Label lblLeisheild = new Label(composite_17, SWT.NONE);
        lblLeisheild.setAlignment(SWT.CENTER);
        lblLeisheild.setText("MultiOmics Viewer : DNA vs RNA vs Protein");
        lblLeisheild.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblLeisheild.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        // fontData = new FontData("Arial", 12, SWT.BOLD);

        Composite composite_10 = new Composite(composite_1, SWT.BORDER);
        GridData gd_composite_10 = new GridData(SWT.CENTER, SWT.CENTER, true, true, 2, 1);
        gd_composite_10.widthHint = 300;
        composite_10.setLayoutData(gd_composite_10);
        composite_10.setLayout(new GridLayout(1, false));

        Label lblLastUpdate = new Label(composite_10, SWT.NONE);
        lblLastUpdate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        lblLastUpdate.setText("Last update: 2018-08-15");

        scrolledComposite.setContent(composite);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        System.out.println("createdd init view");

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
        NavigationManagement.pushStateView(InitViewLeishmania.ID);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnStat) {
            GenomeTranscriptomeView.displayLeishmaniaAmavsPro(partService);
        } else if (e.getSource() == btnLeisheild) {
            HeatMapMultiOmicsView.displayOmicsView(partService);
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
