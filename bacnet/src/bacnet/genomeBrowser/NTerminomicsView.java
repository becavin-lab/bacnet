package bacnet.genomeBrowser;

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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import bacnet.Database;
import bacnet.datamodel.sequence.Genome;
import bacnet.expressionAtlas.core.SelectGenomeElementDialog;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.BioConditionComparator;
import bacnet.utils.ArrayUtils;

public class NTerminomicsView implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -4058433027371889806L;

    public static final String ID = "bacnet.NTerminomicsView"; //$NON-NLS-1$

    private boolean focused = false;

    private Composite compositeSummary;
    private TableViewer tableBioCondition;
    private BioConditionComparator comparatorBioCondition;

    /**
     * Array used to init data
     */
    private String[][] bioCondsArray;
    private ArrayList<String[]> bioCondsToDisplay;
    private ArrayList<String[]> bioConds;

    @SuppressWarnings("unused")
    private ArrayList<String> selectedNTerms = new ArrayList<>();

    private ArrayList<String> columnNames = new ArrayList<>();
    private Label lblXxSrnas;
    private Button btnSaveTxt;
    private Text txtSearch;
    private Button btnHide;

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public NTerminomicsView() {

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
        container.setBounds(0, 0, 559, 300);
        container.setLayout(new GridLayout(1, false));
        lblXxSrnas = new Label(container, SWT.BORDER | SWT.CENTER);
        lblXxSrnas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblXxSrnas.setFont(SWTResourceManager.getTitleFont());
        lblXxSrnas.setText("XX Listeria Complete Genomes");
        lblXxSrnas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        ScrolledComposite scrolledComposite =
                new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        compositeSummary = new Composite(scrolledComposite, SWT.BORDER);
        compositeSummary.setLayout(new GridLayout(1, false));

        Composite composite_2 = new Composite(compositeSummary, SWT.NONE);
        composite_2.setLayout(new GridLayout(7, false));

        Label lblClickOneTime = new Label(composite_2, SWT.NONE);
        lblClickOneTime.setText("Click on a Nterm peptide row to visualize it on the NTerminomics Viewer");
        lblClickOneTime.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

        Label label = new Label(composite_2, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

        txtSearch = new Text(composite_2, SWT.BORDER);
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtSearch.addKeyListener(new org.eclipse.swt.events.KeyListener() {
            /**
             * 
             */
            private static final long serialVersionUID = 2890510302565471497L;

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == 16777296 || e.keyCode == 13) {
                    // System.out.println("Search for "+txtSearch.getText());
                    bioCondsToDisplay.clear();
                    for (String[] row : bioConds) {
                        boolean found = false;
                        for (String cell : row) {
                            if (cell.contains(txtSearch.getText())) {
                                found = true;
                            }
                        }
                        if (found) {
                            bioCondsToDisplay.add(row);
                        }
                    }
                    pushState();
                    tableBioCondition.refresh();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Label lblSearch = new Label(composite_2, SWT.NONE);
        lblSearch.setText("Search");
        lblSearch.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

        btnHide = new Button(composite_2, SWT.NONE);
        btnHide.setText("Select genome elements");
        btnHide.setToolTipText("Select specific genome elements");
        btnHide.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/hideData.bmp"));
        btnHide.addSelectionListener(this);

        btnSaveTxt = new Button(composite_2, SWT.NONE);
        btnSaveTxt.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
        btnSaveTxt.setToolTipText("Download NTerm peptides table");

        Label lblSaveSelectionAs = new Label(composite_2, SWT.NONE);
        lblSaveSelectionAs.setText("Download NTerm peptides table");
        lblSaveSelectionAs.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
        btnSaveTxt.addSelectionListener(this);
        {
            tableBioCondition = new TableViewer(compositeSummary, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
            Table table = tableBioCondition.getTable();
            table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
            tableBioCondition.getTable().setHeaderVisible(true);
            tableBioCondition.getTable().setLinesVisible(true);
            tableBioCondition.addSelectionChangedListener(new ISelectionChangedListener() {

                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    TableViewer viewer = (TableViewer) event.getSource();
                    int i = viewer.getTable().getSelectionIndex();
                    String peptideID =
                            tableBioCondition.getTable().getItem(i).getText(columnNames.indexOf("PeptideID"));
                    GenomeTranscriptomeView.displayNTerminomics(partService, peptideID);
                }
            });

        }
        scrolledComposite.setContent(compositeSummary);
        scrolledComposite.setMinSize(compositeSummary.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        setData();
    }

    /**
     * Set all starting variables
     */
    private void setData() {
        /*
         * Add genomes
         */
        /*
         * Load Table
         */
        String nterminomeArrayPath =
                Database.getInstance().getPath() + Database.getInstance().getDatabaseFeatures().get("NTERM_ARRAY");
        bioCondsArray = TabDelimitedTableReader.read(nterminomeArrayPath);
        bioConds = TabDelimitedTableReader.readList(nterminomeArrayPath, true, true);
        bioCondsToDisplay = TabDelimitedTableReader.readList(nterminomeArrayPath, true, true);
        bioCondsToDisplay.remove(0);
        bioConds.remove(0);
        updateGenomeTable();
        lblXxSrnas.setText("Browse through " + (bioCondsArray.length - 1) + " NTerm peptides");

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
        if (this.txtSearch.getText() != "") {
            parameters.put(NavigationManagement.SEARCH, this.txtSearch.getText());
            NavigationManagement.pushStateView(NTerminomicsView.ID, parameters);
        } else {
            NavigationManagement.pushStateView(NTerminomicsView.ID);
        }
    }

    /**
     * Update Table for Biological Condition
     * 
     * @param bioConds
     */
    private void updateGenomeTable() {

        setColumnNames();
        for (TableColumn col : tableBioCondition.getTable().getColumns()) {
            col.dispose();
        }
        tableBioCondition.setContentProvider(new ArrayContentProvider());

        createColumns();

        tableBioCondition.getTable().setHeaderVisible(true);
        tableBioCondition.getTable().setLinesVisible(true);

        tableBioCondition.setInput(bioCondsToDisplay);
        comparatorBioCondition = new BioConditionComparator(columnNames);
        tableBioCondition.setComparator(comparatorBioCondition);
        for (int i = 0; i < tableBioCondition.getTable().getColumnCount(); i++) {
            tableBioCondition.getTable().getColumn(i).pack();
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
        final TableViewerColumn viewerColumn = new TableViewerColumn(tableBioCondition, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setResizable(true);
        column.setMoveable(true);
        column.addSelectionListener(getSelectionAdapter(column, colNumber));
        return viewerColumn;
    }

    private void createColumns() {
        for (int i = 0; i < bioCondsArray[0].length; i++) {
            final int k = i;
            // TableViewerColumn col = createTableViewerColumn("Select", 0);
            // col.setLabelProvider(new ColumnLabelProvider(){
            //
            // /**
            // *
            // */
            // private static final long serialVersionUID = -6764053440293157951L;
            //
            // @Override
            // public Image getImage(Object element) {
            // String[] bioCond = (String[]) element;
            // if(selectedNTerms.contains(bioCond[0])){
            // return ResourceManager.getPluginImage("bacnet", "icons/checked.bmp");
            // }else{
            // return ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp");
            // }
            // }
            // });
            TableViewerColumn col2 = createTableViewerColumn(bioCondsArray[0][i], i);
            col2.setLabelProvider(new ColumnLabelProvider() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 3506742358064425914L;

                @Override
                public String getText(Object element) {
                    String[] bioCond = (String[]) element;
                    return bioCond[k];
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
            private static final long serialVersionUID = -6997325110066606691L;

            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println(e.getSource());
                comparatorBioCondition.setColumn(index);
                int dir = comparatorBioCondition.getDirection();
                tableBioCondition.getTable().setSortDirection(dir);
                tableBioCondition.getTable().setSortColumn(column);
                tableBioCondition.refresh();
            }
        };
        return selectionAdapter;
    }

    /**
     * Display the view with saved parameters
     * 
     * @param gene
     * @param partService
     */
    public static void displayNTerminomicsView(EPartService partService, HashMap<String, String> parameters) {
        partService.showPart(ID, PartState.ACTIVATE);
        // update data
        MPart part = partService.findPart(ID);
        NTerminomicsView view = (NTerminomicsView) part.getObject();
        for (String stateId : parameters.keySet()) {
            String stateValue = parameters.get(stateId);
            if (stateId.equals(NavigationManagement.SEARCH)) {
                view.getTxtSearch().setText(stateValue);
                view.getBioCondsToDisplay().clear();
                for (String[] row : view.getBioConds()) {
                    boolean found = false;
                    for (String cell : row) {
                        if (cell.contains(view.getTxtSearch().getText())) {
                            found = true;
                        }
                    }
                    if (found) {
                        view.getBioCondsToDisplay().add(row);
                    }
                }
            }
        }
        view.getTableBioCondition().refresh();
        view.pushState();
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnSaveTxt) {
            bioCondsToDisplay.add(0, columnNames.toArray(new String[0]));
            String arrayRep = ArrayUtils.toString(bioCondsToDisplay);
            if (bioCondsToDisplay.size() < 100) {
                String[][] array = ArrayUtils.toArray(bioCondsToDisplay);
                String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(array);
                SaveFileUtils.saveTextFile("Listeria_NTerm_Table.txt", arrayRep, true, "", arrayRepHTML, partService,
                        shell);
            } else {
                SaveFileUtils.saveTextFile("Listeria_NTerm_Table.txt", arrayRep, true, "", "", partService, shell);
            }
        } else if (e.getSource() == btnHide) {
            tableBioCondition.getTable().deselectAll();
            TreeSet<String> includeElements = new TreeSet<>();
            TreeSet<String> excludeElements = new TreeSet<>();
            SelectGenomeElementDialog dialog = new SelectGenomeElementDialog(shell, partService, includeElements,
                    excludeElements, Genome.EGDE_NAME);
            if (dialog.open() == 0) {
                ArrayList<String> includeRows = new ArrayList<>();
                for (String row : includeElements) {
                    includeRows.add(row);
                }
                bioCondsToDisplay.clear();
                for (String[] row : bioConds) {
                    String gene = row[columnNames.indexOf("Protein Overlap")];
                    if (!gene.equals("")) {
                        if (includeRows.contains(gene)) {
                            bioCondsToDisplay.add(row);
                        }
                    }
                }
                tableBioCondition.refresh();
            }
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

    public Text getTxtSearch() {
        return txtSearch;
    }

    public void setTxtSearch(Text txtSearch) {
        this.txtSearch = txtSearch;
    }

    public TableViewer getTableBioCondition() {
        return tableBioCondition;
    }

    public void setTableBioCondition(TableViewer tableBioCondition) {
        this.tableBioCondition = tableBioCondition;
    }

    public ArrayList<String[]> getBioConds() {
        return bioConds;
    }

    public void setBioConds(ArrayList<String[]> bioConds) {
        this.bioConds = bioConds;
    }
}
