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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Genome;
import bacnet.expressionAtlas.core.ComparisonAtlas;
import bacnet.expressionAtlas.core.GenomeElementAtlas;
import bacnet.raprcp.NavigationManagement;
import bacnet.swt.ResourceManager;
import bacnet.utils.BasicColor;
import bacnet.utils.Filter;

public class HeatMapTranscriptomicsView implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -2640442866422682301L;

    public static final String ID = "bacnet.HeatMapTranscriptomicsView"; //$NON-NLS-1$

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;

    private String sequence = "";
    private String viewId = "";
    private String genomeName = Genome.EGDE_NAME;
    private Composite compositeSummary;
    private TableCompositeHeatMap tableComposite;
    private Combo cmbDataUsed;
    private Text txtCutoffLogFC;
    private Button btnUpdateCutoff;

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public HeatMapTranscriptomicsView() {}

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

                Composite composite_8 = new Composite(compositeSummary, SWT.BORDER);
                composite_8.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
                composite_8.setLayout(new GridLayout(3, false));

                {
                    Composite composite = new Composite(composite_8, SWT.NONE);
                    composite.setLayout(new GridLayout(4, false));
                    {
                        Label lblApplyCutoffFor = new Label(composite, SWT.NONE);
                        lblApplyCutoffFor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
                        lblApplyCutoffFor.setText("Apply cut-off |Log(Fold-Change| >");
                    }

                    txtCutoffLogFC = new Text(composite, SWT.BORDER);
                    txtCutoffLogFC.setText("1.5");

                    Label lbllogfoldchange = new Label(composite, SWT.NONE);
                    lbllogfoldchange.setText("for");
                    {
                        cmbDataUsed = new Combo(composite, SWT.NONE);
                        GridData gd_cmbDataUsed = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
                        gd_cmbDataUsed.widthHint = 306;
                        cmbDataUsed.setLayoutData(gd_cmbDataUsed);
                    }
                }

                btnUpdateCutoff = new Button(composite_8, SWT.TOGGLE);
                btnUpdateCutoff.setBackground(BasicColor.BUTTON);;
                btnUpdateCutoff.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
                btnUpdateCutoff.setText("Update HeatMap");
                btnUpdateCutoff.addSelectionListener(this);
                {
                    tableComposite = new TableCompositeHeatMap(compositeSummary, SWT.NONE, genomeName, false,
                            partService, shell);
                    tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                }

            }
            scrolledComposite.setContent(compositeSummary);
            scrolledComposite.setMinSize(compositeSummary.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }

    }

    private void initData(String genomeName) {
        this.setGenomeName(genomeName);
        ExpressionMatrix logFCMatrix = Database.getInstance().getLogFCTranscriptomesTable(genomeName);
        tableComposite.initData(logFCMatrix);
        tableComposite.setGenomeName(genomeName);
    }

    private void updateComboData(ArrayList<String> comparisons) {
        cmbDataUsed.removeAll();
        cmbDataUsed.add("All Columns");
        cmbDataUsed.select(0);
        for (String comp : comparisons) {
            cmbDataUsed.add(comp);
        }
    }

    /**
     * Update the list of columns from the display
     * 
     * @param includeComparisons list of comparisons to include
     */
    public void updateExcludeColumns(ArrayList<String> includeComparisons) {
        ArrayList<String> excludeComparisons = new ArrayList<>();
        ArrayList<String> removedBioConds = BioCondition.getAllBioConditionNames();
        for (String bioCondName : removedBioConds) {
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            for (String comp : bioCond.getComparisonNames()) {
                excludeComparisons.add(comp);
            }
        }

        for (String includeColumn : includeComparisons) {
            excludeComparisons.remove(includeColumn);
        }
        tableComposite.setExcludeColumn(excludeComparisons);
    }

    /**
     * Update the list of rows to exclude from the display
     */
    public void updateExcludeRows() {
        /*
         * Get cut-off
         */
        double cutoffLogFC = GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF;
        try {
            cutoffLogFC = Double.parseDouble(txtCutoffLogFC.getText());
        } catch (Exception e) {
            txtCutoffLogFC.setText(GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF + "");
        }
        Filter filter = new Filter();
        filter.setCutOff1(cutoffLogFC);

        /*
         * Create list of genome elements to exclude for each analysis
         */

        ArrayList<String> excludedGenomesElements = new ArrayList<>();
        ComparisonAtlas atlas = new ComparisonAtlas(Database.getInstance().getLogFCTranscriptomesTable(genomeName),
                tableComposite.getExcludeColumn(), cmbDataUsed.getItem(cmbDataUsed.getSelectionIndex()), filter);
        for (String genomeElement : atlas.getExcludeRows()) {
            excludedGenomesElements.add(genomeElement);
        }

        tableComposite.setExcludeRow(excludedGenomesElements);
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
        if (!sequence.equals("")) {
            parameters.put(NavigationManagement.GENE, sequence);
        }

        String stateValue = "";
        for (String comparison : cmbDataUsed.getItems()) {
            stateValue += comparison + ":";
        }
        parameters.put(NavigationManagement.LIST, stateValue);
        NavigationManagement.pushStateView(this.getViewId(), parameters);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnUpdateCutoff) {
            System.out.println("Update HeatMap");
            updateExcludeRows();
            tableComposite.updateInfo();
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    public static void displayComparisonsAndElement(String genomeName, ArrayList<String> comparisons, String sequence,
            EPartService partService) {
    	System.out.println("in displayComparisonsAndElement");
        ArrayList<String> genomeArrays = BioCondition.getTranscriptomesGenomes();
        if (genomeArrays.contains(genomeName)) {
            String id = HeatMapTranscriptomicsView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
            // initiate view
            ResourceManager.openView(partService, HeatMapTranscriptomicsView.ID, id);
            // update data
            MPart part = partService.findPart(id);
            HeatMapTranscriptomicsView view = (HeatMapTranscriptomicsView) part.getObject();
            view.setViewId(id);
            view.initData(genomeName);
            view.updateExcludeColumns(comparisons);

            if (!sequence.equals("")) {
                ArrayList<String> excludedGenomesElements = new ArrayList<>();
                for (String genomeElement : Database.getInstance().getCisRegRNAListEGDe()) {
                    excludedGenomesElements.add(genomeElement);
                }
                for (String genomeElement : Database.getInstance().getsRNAListEGDe()) {
                    excludedGenomesElements.add(genomeElement);
                }
                for (String genomeElement : Database.getInstance().getAsRNAListEGDe()) {
                    excludedGenomesElements.add(genomeElement);
                }
                for (String genomeElement : Database.getInstance().getGeneListEGDe()) {
                    excludedGenomesElements.add(genomeElement);
                }
                excludedGenomesElements.remove(sequence);
                view.getTableComposite().setExcludeRow(excludedGenomesElements);
            }
            view.getTableComposite().updateInfo();
            view.updateComboData(comparisons);
            view.setSequence(sequence);
            view.pushState();
        }
    }

    /**
     * Run a <code>HeatMapTranscriptomicsView</code> displaying a specific comparison of bioCond
     * 
     * @param bioConds
     */
    public static void displayBioConditionsExpressionAtlas(HashMap<String, ArrayList<String>> genomeToComparisons,
            EPartService partService) {

        // Create a heatmap for each genome
        for (String genomeName : genomeToComparisons.keySet()) {
            ArrayList<String> genomeArrays = BioCondition.getTranscriptomesGenomes();
            if (genomeArrays.contains(genomeName)) {
                ArrayList<String> comparisons = genomeToComparisons.get(genomeName);
                String id = HeatMapTranscriptomicsView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
                // initiate view
                //ResourceManager.openView(partService, "test", id);
                ResourceManager.openView(partService, HeatMapTranscriptomicsView.ID, id);
                // update data
                MPart part = partService.findPart(id);
                HeatMapTranscriptomicsView view = (HeatMapTranscriptomicsView) part.getObject();
                view.setViewId(id);
                view.initData(genomeName);
                view.updateExcludeColumns(comparisons);
                view.updateComboData(comparisons);
                view.updateExcludeRows();
                view.pushState();
                view.getTableComposite().updateInfo();
            }
        }
    }

    /**
     * Display the view with saved parameters
     * 
     * @param gene
     * @param partService
     */
    public static void displayHeatMapTranscriptomicsView(EPartService partService, HashMap<String, String> parameters) {
        String genomeName = parameters.get(NavigationManagement.GENOME);
        String[] genes = parameters.get(NavigationManagement.LIST).split(":");
        ArrayList<String> comparisons = new ArrayList<>();
        for (String gene : genes) {
            comparisons.add(gene);
        }
        if (parameters.containsKey(NavigationManagement.GENE)) {
            displayComparisonsAndElement(genomeName, comparisons, parameters.get(NavigationManagement.GENE),
                    partService);
        } else {
            HashMap<String, ArrayList<String>> genomeToComparisons = new HashMap<>();
            genomeToComparisons.put(genomeName, comparisons);
            displayBioConditionsExpressionAtlas(genomeToComparisons, partService);
        }

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

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

}
