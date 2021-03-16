package bacnet.expressionAtlas;

import java.util.ArrayList;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.raprcp.NavigationManagement;
import bacnet.swt.ResourceManager;

public class HeatMapProteomicsExpressionView implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 7806377909856153015L;

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;

    public static final String ID = "bacnet.HeatMapProteomicsView"; //$NON-NLS-1$

    private String viewId = "";
    private String genomeName = Genome.EGDE_NAME;
    private Composite compositeSummary;
    private TableCompositeHeatMap tableComposite;

    private ArrayList<String> bioConditions = new ArrayList<>();

    @Inject
    private EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public HeatMapProteomicsExpressionView() {}

    /**
     * Create contents of the view part.
     * 
     * @param parent
     */
    @PostConstruct
    public void createPartControl(Composite parent) {
        focused = true;
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));

        {
            ScrolledComposite scrolledComposite =
                    new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
            scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));
            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);

            {
                compositeSummary = new Composite(scrolledComposite, SWT.BORDER);
                compositeSummary.setLayout(new GridLayout(1, false));
                {
                    tableComposite =
                            new TableCompositeHeatMap(compositeSummary, SWT.NONE, genomeName, true, partService, shell);
                    tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
                }

            }
            scrolledComposite.setContent(compositeSummary);
            scrolledComposite.setMinSize(compositeSummary.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }

    }

    private void initData() {
        ExpressionMatrix exprMatrix = Database.getInstance().getExprProteomesTable(genomeName);
        tableComposite.initData(exprMatrix);
    }

    /**
     * Update the list of columns from the display
     * 
     * @param includeComparisons list of comparisons to include
     */
    public void updateExcludeColumns(ArrayList<String> bioConditions) {
        ExpressionMatrix matrixExprProteomes = Database.getInstance().getExprProteomesTable(genomeName);
        /*
         * Find biocondition to exclude
         */
        ArrayList<String> excludeBioConditions = new ArrayList<>();
        for (String bioCondName : matrixExprProteomes.getHeaders()) {
            excludeBioConditions.add(bioCondName);
        }
        for (String includeColumn : bioConditions) {
            excludeBioConditions.remove(includeColumn);
        }
        tableComposite.setExcludeColumn(excludeBioConditions);
        /*
         * Find rowName to exclude
         */
        ArrayList<String> excludeRowNames = new ArrayList<>();
        for (String rowName : matrixExprProteomes.getRowNames().keySet()) {
            boolean noValue = true;
            for (String header : bioConditions) {
                double value = matrixExprProteomes.getValue(rowName, header);
                if (value > 0) {
                    noValue = false;
                }
            }
            if (noValue) {
                excludeRowNames.add(rowName);
            }
        }
        tableComposite.setExcludeRow(excludeRowNames);

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
        parameters.put(NavigationManagement.GENOME, genomeName);
        String stateValue = "";
        for (String comparison : bioConditions) {
            stateValue += comparison + ":";
        }
        parameters.put(NavigationManagement.LIST, stateValue);
        NavigationManagement.pushStateView(this.getViewId(), parameters);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {}

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * Run a <code>HeatMapView</code> displaying a specific bioCond
     * 
     * @param bioConds
     */
    public static void displayBioConditions(String genomeName, ArrayList<String> bioConditions,
            EPartService partService) {
        String id = HeatMapProteomicsExpressionView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
        // initiate view
        ResourceManager.openView(partService, HeatMapProteomicsExpressionView.ID, id);
        // update data
        MPart part = partService.findPart(id);
        HeatMapProteomicsExpressionView view = (HeatMapProteomicsExpressionView) part.getObject();
        view.setViewId(id);
        view.setGenomeName(genomeName);
        view.initData();
        view.updateExcludeColumns(bioConditions);
        view.getTableComposite().updateInfo();
        view.getTableComposite().setTranscriptomics(false);
        view.setBioConditions(bioConditions);
        view.pushState();
    }

    /**
     * Display the view with saved parameters
     * 
     * @param gene
     * @param partService
     */
    public static void displayHeatMapProteomicsView(EPartService partService, HashMap<String, String> parameters) {
        String genomeName = parameters.get(NavigationManagement.GENOME);
        String[] genes = parameters.get(NavigationManagement.LIST).split(":");
        ArrayList<String> bioConditions = new ArrayList<>();
        for (String gene : genes) {
            System.out.println(gene);
            bioConditions.add(gene);
        }
        displayBioConditions(genomeName, bioConditions, partService);
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

    public ArrayList<String> getBioConditions() {
        return bioConditions;
    }

    public void setBioConditions(ArrayList<String> bioConditions) {
        this.bioConditions = bioConditions;
    }

}
