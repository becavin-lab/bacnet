
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.BioConditionComparator;
import bacnet.utils.ArrayUtils;
import bacnet.utils.RWTUtils;
import bacnet.views.HelpPage;

public class ProteomicsExpressionView implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 3273618417542386059L;

    public static final String ID = "bacnet.ProteomicsView"; //$NON-NLS-1$

    public static final int DATA_LIMIT = 25;
    private TableViewer tableProteomeViewer;
    private BioConditionComparator comparatorBioCondition;

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;

    /**
     * Array used to init data
     */
    private String[][] bioCondsArray;
    /**
     * List used to init data
     */
    private ArrayList<String[]> bioConds;
    /**
     * Array which will be displayed
     */
    private ArrayList<String[]> bioCondsToDisplay;

    private ArrayList<String> columnNames = new ArrayList<>();
    private Button btnGenomeViewer;
    private Button btnHeatmap;

    private final Image imageTSS;
    private final Image imageGeneExpr;
    private ProteomicsDataFilterComposite compositeDataFilter;
    private Table tableProteome;
    private ArrayList<String> selectedProteomes = new ArrayList<>();
    private Button btnHelp;
    private Button btnSaveTxt;
    private Button btnUnselectall;
    private Button btnSelectall;

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public ProteomicsExpressionView() {
        imageTSS = ResourceManager.getPluginImage("bacnet", "icons/tss.bmp");
        imageGeneExpr = ResourceManager.getPluginImage("bacnet", "icons/GeneExpr.bmp");
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
        container.setBounds(0, 0, 688, 439);
        container.setLayout(new GridLayout(3, false));
        Label lblXxSrnas = new Label(container, SWT.BORDER | SWT.CENTER);
        lblXxSrnas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        lblXxSrnas.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
        lblXxSrnas.setText(Database.getInstance().getSpecies() + " Proteomics Datasets");
        lblXxSrnas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnHelp = new Button(container, SWT.NONE);
        btnHelp.setToolTipText("How to use Proteomic summary panel ?");
        btnHelp.setImage(ResourceManager.getPluginImage("bacnet", "icons/help.png"));
        btnHelp.addSelectionListener(this);

        // if(Database.getInstance().getTypeProject()==TypeProject.Listeriomics ||
        // Database.getInstance().getTypeProject()==TypeProject.UIBCListeriomics){

        ScrolledComposite scrolledComposite =
                new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 3);
        gd_scrolledComposite.widthHint = 300;
        scrolledComposite.setLayoutData(gd_scrolledComposite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        compositeDataFilter = new ProteomicsDataFilterComposite(scrolledComposite, SWT.NONE, this);
        scrolledComposite.setContent(compositeDataFilter);
        scrolledComposite.setMinSize(compositeDataFilter.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        {
            Composite composite_2 = new Composite(container, SWT.BORDER);
            composite_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            composite_2.setLayout(new GridLayout(4, false));
            {
                Label label = new Label(composite_2, SWT.NONE);
                label.setText("Select biological condtions and : ");
            }
            {
                Composite composite_1 = new Composite(composite_2, SWT.NONE);
                composite_1.setLayout(new GridLayout(2, false));
                {
                    btnGenomeViewer = new Button(composite_1, SWT.NONE);
                    btnGenomeViewer.setImage(ResourceManager.getPluginImage("bacnet", "icons/genomeViewer.bmp"));
                    {
                        Label lblDisplayTheseBiocondition = new Label(composite_1, SWT.WRAP);
                        GridData gd_lblDisplayTheseBiocondition =
                                new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
                        gd_lblDisplayTheseBiocondition.widthHint = 200;
                        lblDisplayTheseBiocondition.setLayoutData(gd_lblDisplayTheseBiocondition);
                        lblDisplayTheseBiocondition
                                .setText("Visualize their transcriptomics datasets with the Genome Viewer");
                        // lblDisplayTheseBiocondition.setForeground(BasicColor.GREY);
                    }
                    btnGenomeViewer.addSelectionListener(this);
                }
            }
            {
                Composite composite_2_1 = new Composite(composite_2, SWT.NONE);
                composite_2_1.setLayout(new GridLayout(2, false));
                {
                    btnHeatmap = new Button(composite_2_1, SWT.NONE);
                    btnHeatmap.setImage(ResourceManager.getPluginImage("bacnet", "icons/compareexpression.bmp"));
                    btnHeatmap.addSelectionListener(this);
                }
                {
                    Label lblDisplayFoldchangeOf = new Label(composite_2_1, SWT.WRAP);
                    GridData gd_lblDisplayFoldchangeOf = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
                    gd_lblDisplayFoldchangeOf.widthHint = 200;
                    lblDisplayFoldchangeOf.setLayoutData(gd_lblDisplayFoldchangeOf);
                    lblDisplayFoldchangeOf.setText("Visualize protein detection with the HeatMap Viewer");
                }
            }
            new Label(composite_2, SWT.NONE);
        }
        new Label(container, SWT.NONE);
        {
            Composite composite_2 = new Composite(container, SWT.BORDER);
            composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            composite_2.setLayout(new GridLayout(10, false));

            btnSelectall = new Button(composite_2, SWT.NONE);
            btnSelectall.setToolTipText("Select all genomes");
            btnSelectall.setImage(ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
            btnSelectall.addSelectionListener(this);
            Label lblSelectAll = new Label(composite_2, SWT.NONE);
            lblSelectAll.setText("Select all");
            lblSelectAll.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

            btnUnselectall = new Button(composite_2, SWT.NONE);
            btnUnselectall.setToolTipText("Unselect all genomes");
            btnUnselectall.setImage(ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            btnUnselectall.addSelectionListener(this);
            Label lblUnselectAll = new Label(composite_2, SWT.NONE);
            lblUnselectAll.setText("Unselect all");
            lblUnselectAll.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

            new Label(composite_2, SWT.NONE);

            btnSaveTxt = new Button(composite_2, SWT.NONE);
            btnSaveTxt.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
            btnSaveTxt.setToolTipText("Download the table in tabulated text format");

            Label lblSaveSelectionAs = new Label(composite_2, SWT.NONE);
            lblSaveSelectionAs.setText("Download proteome selection as a table");
            lblSaveSelectionAs.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
            btnSaveTxt.addSelectionListener(this);
        }
        new Label(container, SWT.NONE);
        // }
        tableProteomeViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        tableProteome = tableProteomeViewer.getTable();
        tableProteome.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        tableProteomeViewer.getTable().setHeaderVisible(true);
        tableProteomeViewer.getTable().setLinesVisible(true);
        RWTUtils.setMarkup(tableProteome);
        new Label(container, SWT.NONE);
        tableProteomeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                for (int i : tableProteome.getSelectionIndices()) {
                    String selectedGenome = tableProteome.getItem(i).getText(columnNames.indexOf("Data Name") + 1);
                    if (!selectedGenome.equals("")) {
                        if (selectedProteomes.contains(selectedGenome)) {
                            if (tableProteome.getSelectionIndices().length == 1) {
                                selectedProteomes.remove(selectedGenome);
                                tableProteomeViewer.replace(tableProteomeViewer.getTable().getItem(i).getData(), i);
                            }
                        } else {
                            selectedProteomes.add(selectedGenome);
                            tableProteomeViewer.replace(tableProteomeViewer.getTable().getItem(i).getData(), i);
                        }
                    }
                }
            }
        });

        setData();
        if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT
                || Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
            compositeDataFilter.updateInfo();
        }
    }

    /**
     * Set all starting variables
     */
    private void setData() {
        /*
         * Load Table
         */
        bioCondsArray = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
        bioConds = TabDelimitedTableReader.readList(Database.getInstance().getProteomesArrayPath(), true, true);
        bioCondsToDisplay =
                TabDelimitedTableReader.readList(Database.getInstance().getProteomesArrayPath(), true, true);
        if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
            ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<String[]>();
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Reference")];
                if (info.contains("Unpublished (Cossart lab)")) {
                    //
                } else {
                    bioCondsToDisplayTemp.add(row);
                }
            }
            bioCondsToDisplay = bioCondsToDisplayTemp;
        }

        bioConds.remove(0);
        bioCondsToDisplay.remove(0);
        updateBioConditionTable();
        /*
         * Fill combos
         */
        TreeSet<String> mutantSet = new TreeSet<>();
        for (String[] rows : bioConds) {
            String mutants = rows[ArrayUtils.findColumn(bioCondsArray, "Mutant")];
            for (String mutant : mutants.split(",")) {
                if (!mutant.equals("")) {
                    mutant = mutant.trim();
                    Genome genome = Genome.loadEgdeGenome();
                    Sequence seq = genome.getElement(mutant);
                    if (seq != null && seq instanceof Gene) {
                        Gene gene = (Gene) seq;
                        String text = gene.getName();
                        if (!gene.getGeneName().equals(""))
                            text += " (" + gene.getGeneName() + ")";
                        mutantSet.add(text);
                    } else {
                        mutantSet.add(mutant.trim());
                    }
                }
            }
        }

    }

    @Focus
    public void onFocus() {
        if (!focused) {
            compositeDataFilter.pushState();
            focused = true;
        } else {
            focused = false;
        }
    }

    /**
     * Update Table for Biological Condition
     * 
     * @param bioConds
     */
    public void updateBioConditionTable() {

        setColumnNames();
        for (TableColumn col : tableProteomeViewer.getTable().getColumns()) {
            col.dispose();
        }
        tableProteomeViewer.setContentProvider(new ArrayContentProvider());

        createColumns();

        tableProteomeViewer.getTable().setHeaderVisible(true);
        tableProteomeViewer.getTable().setLinesVisible(true);
        /*
         * Remove first row
         */
        tableProteomeViewer.setInput(bioCondsToDisplay);
        comparatorBioCondition = new BioConditionComparator(columnNames);
        tableProteomeViewer.setComparator(comparatorBioCondition);
        for (int i = 0; i < tableProteomeViewer.getTable().getColumnCount(); i++) {
            tableProteomeViewer.getTable().getColumn(i).pack();
        }
    }

    /**
     * Add the name of the columns
     */
    private void setColumnNames() {
        columnNames = new ArrayList<>();
        for (String title : bioCondsArray[0]) {
            columnNames.add(title);
        }
    }

    private TableViewerColumn createTableViewerColumn(String title, final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(tableProteomeViewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setResizable(true);
        column.setMoveable(true);
        column.addSelectionListener(getSelectionAdapter(column, colNumber));
        return viewerColumn;
    }

    private void createColumns() {
        TableViewerColumn col = createTableViewerColumn("Select", 0);
        col.setLabelProvider(new ColumnLabelProvider() {
            /**
             * 
             */
            private static final long serialVersionUID = -70923942574212205L;

            @Override
            public String getText(Object element) {
                return "";
            }

            @Override
            public Image getImage(Object element) {
                String[] bioCond = (String[]) element;
                Image image = null;
                if (selectedProteomes.contains(bioCond[columnNames.indexOf("Data Name")])) {
                    image = ResourceManager.getPluginImage("bacnet", "icons/checked.bmp");
                } else {
                    image = ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp");
                }
                return image;
            }
        });

        for (int i = 0; i < bioConds.get(0).length; i++) {
            final int k = i;
            TableViewerColumn col2 = createTableViewerColumn(bioCondsArray[0][i], i + 1);
            col2.setLabelProvider(new ColumnLabelProvider() {
                /**
                 * 
                 */
                private static final long serialVersionUID = -55186278705378176L;

                @Override
                public String getText(Object element) {
                    String[] bioCond = (String[]) element;
                    String colName = columnNames.get(k);
                    String text = bioCond[k];
                    if (colName.equals("Type")) {
                        return "";
                    } else if (colName.equals("Reference")) {
                        return RWTUtils.setPubMedLink(text);
                    } else if (colName.equals("Pride Id")) {
                        return RWTUtils.setPrideLink(text);
                    } else {
                        return text;
                    }
                }

                @Override
                public Image getImage(Object element) {
                    String[] bioCond = (String[]) element;
                    if (k == columnNames.indexOf("Type")) {
                        String typeDataContained = bioCond[k];
                        if (typeDataContained.contains(TypeData.NTerm + "")) {
                            return imageTSS;
                        } else if (!typeDataContained.contains(TypeData.Proteome + "")) {
                            return imageGeneExpr;
                        } else {
                            return imageGeneExpr;
                        }
                    } else
                        return null;
                }

            });
            col2.getColumn().pack();

            // }

        }

    }

    private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
        SelectionAdapter selectionAdapter = new SelectionAdapter() {

            /**
             * 
             */
            private static final long serialVersionUID = 7102838693143247943L;

            @Override
            public void widgetSelected(SelectionEvent e) {
                comparatorBioCondition.setColumn(index - 1);
                int dir = comparatorBioCondition.getDirection();
                tableProteomeViewer.getTable().setSortDirection(dir);
                tableProteomeViewer.getTable().setSortColumn(column);
                tableProteomeViewer.refresh();
            }
        };
        return selectionAdapter;
    }

    /**
     * Retreive the name of the BioCond selected in tableBioCondition
     * 
     * @return
     */
    private HashMap<String, ArrayList<String>> getSelectedBioConditions() {
        HashMap<String, ArrayList<String>> genomeToBioConds = new HashMap<>();
        for (String bioCondName : selectedProteomes) {
            // System.out.println("select: "+index);
            BioCondition bioCondition = BioCondition.getBioCondition(bioCondName);
            String genome = bioCondition.getGenomeName();
            if (genomeToBioConds.containsKey(genome)) {
                genomeToBioConds.get(genome).add(bioCondName);
            } else {
                ArrayList<String> bioCondNames = new ArrayList<>();
                bioCondNames.add(bioCondName);
                genomeToBioConds.put(genome, bioCondNames);
            }
        }
        return genomeToBioConds;
    }

    /**
     * Display a search in Genomic View
     * 
     * @param gene
     * @param partService
     */
    public static void displayProteomicsView(EPartService partService, HashMap<String, String> parameters) {
        partService.showPart(ID, PartState.ACTIVATE);
        // update data
        MPart part = partService.findPart(ID);
        ProteomicsExpressionView view = (ProteomicsExpressionView) part.getObject();
        ProteomicsDataFilterComposite compositeDataFilter = view.getCompositeDataFilter();
        for (String stateId : parameters.keySet()) {
            String stateValue = parameters.get(stateId);
            if (stateId.equals(NavigationManagement.SEARCH)) {
                compositeDataFilter.getTextSearch().setText(parameters.get(NavigationManagement.SEARCH));
            }
            if (stateId.contains(NavigationManagement.COMBO)) {
                Combo combo = compositeDataFilter.getComboGenome();
                for (int i = 0; i < compositeDataFilter.getComboGenome().getItemCount(); i++) {
                    String item = combo.getItem(i);
                    if (item.equals(stateValue)) {
                        combo.select(i);
                    }
                }
            }
            if (stateId.contains(NavigationManagement.BUTTON)) {
                for (Button button : compositeDataFilter.getAllButtons()) {
                    String state = button.getText();
                    state = state.substring(0, state.indexOf('(') - 1);
                    if (stateValue.equals(state)) {
                        button.setSelection(true);
                    }
                }
            }

        }
        compositeDataFilter.updateView();
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnGenomeViewer) {
            HashMap<String, ArrayList<String>> genomeToBioConds = getSelectedBioConditions();
            for (String genomeName : genomeToBioConds.keySet()) {
                GenomeTranscriptomeView.displayGenomeElementAndBioConditions(partService, genomeName,
                        genomeToBioConds.get(genomeName), "");
            }
        } else if (e.getSource() == btnHeatmap) {
            HashMap<String, ArrayList<String>> genomeToBioConds = getSelectedBioConditions();
            for (String genomeName : genomeToBioConds.keySet()) {
                //HeatMapProteomicsView.displayBioConditions(genomeName, genomeToBioConds.get(genomeName), partService);
            }
        } else if (e.getSource() == tableProteomeViewer.getTable()) {
        } else if (e.getSource() == btnHelp) {
            HelpPage.helpProteomicView(partService);
        } else if (e.getSource() == btnSelectall) {
            selectedProteomes = new ArrayList<>();
            tableProteome.selectAll();
            for (int i : tableProteome.getSelectionIndices()) {
                String selectedGenome = tableProteome.getItem(i).getText(columnNames.indexOf("Data Name") + 1);
                if (!selectedProteomes.contains(selectedGenome)) {
                    selectedProteomes.add(selectedGenome);
                }
            }
            tableProteomeViewer.refresh();
        } else if (e.getSource() == btnUnselectall) {
            selectedProteomes = new ArrayList<>();
            tableProteome.deselectAll();
            tableProteomeViewer.refresh();
        } else if (e.getSource() == btnSaveTxt) {
            String[][] arrayToSave = new String[1][columnNames.size()];
            for (int i = 0; i < columnNames.size(); i++) {
                arrayToSave[0][i] = columnNames.get(i);
            }
            int k = 1;
            for (int i = 1; i < bioCondsArray.length; i++) {
                String transcript = bioCondsArray[i][columnNames.indexOf("Data Name")];
                if (selectedProteomes.contains(transcript)) {
                    arrayToSave = ArrayUtils.addRow(arrayToSave, ArrayUtils.getRow(bioCondsArray, i), k);
                    k++;
                }
            }
            String arrayRep = ArrayUtils.toString(arrayToSave);
            String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(arrayToSave);
            SaveFileUtils.saveTextFile(Database.getInstance().getSpecies() + "_Proteomic_Table.txt", arrayRep, true, "", arrayRepHTML, partService,
                    shell);
        } else {
            compositeDataFilter.updateBioConditionList();
            compositeDataFilter.pushState();
            updateBioConditionTable();
            compositeDataFilter.updateInfo();
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

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

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
    }

    public ProteomicsDataFilterComposite getCompositeDataFilter() {
        return compositeDataFilter;
    }

    public void setCompositeDataFilter(ProteomicsDataFilterComposite compositeDataFilter) {
        this.compositeDataFilter = compositeDataFilter;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

}
