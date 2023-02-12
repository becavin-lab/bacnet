package bacnet.expressionAtlas;

import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import bacnet.expressionAtlas.core.OpenExpressionMatrixAndComparisons;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.BioConditionComparator;
import bacnet.utils.ArrayUtils;
import bacnet.utils.BasicColor;
import bacnet.utils.RWTUtils;
import bacnet.views.HelpPage;

/**
 * View in which is displayed the table with all transcriptomics data available
 * 
 * @author christophebecavin
 *
 */
public class TranscriptomicsView implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -5118476676200457964L;

    public static final String ID = "bacnet.TranscriptomicsView"; //$NON-NLS-1$

    public static final int DATA_LIMIT = 25;
    private TableViewer tableBioConditionViewer;
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

    /**
     * Composite to filter list of data
     */
    private TranscriptomicsDataFilterComposite compositeDataFilter;

    private ArrayList<String> selectedTranscriptomes = new ArrayList<>();
    /*
     * Images
     */
    private final Image imageTSS;
    private final Image imageRNASeq;
    private final Image imageTSSTilingGeneExpr;
    private final Image imageTilingGeneExpr;
    private final Image imageGeneExpr;
    private final Image imageTiling;
    private Table tableBioCondition;
    private Button btnHeatmap;
    //private Button btnHelp;
    private Button btnUnselectall;
    private Button btnSelectall;
    private Button btnSaveTxt;

    @Inject
    private EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public TranscriptomicsView() {
        imageTSS = ResourceManager.getPluginImage("bacnet.core", "icons/tss.bmp");
        imageRNASeq = ResourceManager.getPluginImage("bacnet.core", "icons/rnaSeq.bmp");
        imageTSSTilingGeneExpr = ResourceManager.getPluginImage("bacnet.core", "icons/tssTilingGeneExpr.bmp");
        imageTilingGeneExpr = ResourceManager.getPluginImage("bacnet.core", "icons/TilingGeneExpr.bmp");
        imageGeneExpr = ResourceManager.getPluginImage("bacnet.core", "icons/geneexpr.bmp");
        imageTiling = ResourceManager.getPluginImage("bacnet.core", "icons/tiling.bmp");
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
        container.setLayout(new GridLayout(3, false));
        Label lblXxSrnas = new Label(container, SWT.BORDER | SWT.CENTER);
        lblXxSrnas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        lblXxSrnas.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
        lblXxSrnas.setText(Database.getInstance().getSpecies() + " Transcriptomics Datasets");
        lblXxSrnas.setBackground(BasicColor.HEADER);
/*
        btnHelp = new Button(container, SWT.NONE);
        btnHelp.setToolTipText("How to use Transcriptomic summary panel ?");
        btnHelp.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/help.png"));
        btnHelp.addSelectionListener(this);
*/
        ScrolledComposite scrolledComposite =
                new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
        gd_scrolledComposite.widthHint = 300;
        scrolledComposite.setLayoutData(gd_scrolledComposite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        compositeDataFilter = new TranscriptomicsDataFilterComposite(scrolledComposite, SWT.NONE, this);
        scrolledComposite.setContent(compositeDataFilter);
        scrolledComposite.setMinSize(compositeDataFilter.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        Composite compositeTable = new Composite(container, SWT.NONE);
        compositeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        compositeTable.setLayout(new GridLayout(1, false));
        {
            Composite composite_1 = new Composite(compositeTable, SWT.BORDER);
            composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            composite_1.setLayout(new GridLayout(7, false));

            Label lblSelectBiologicalCondtions = new Label(composite_1, SWT.NONE);
            lblSelectBiologicalCondtions.setText("Select biological conditions and: ");
            {
                btnGenomeViewer = new Button(composite_1, SWT.TOGGLE);
                btnGenomeViewer.setBackground(BasicColor.BUTTON);
                btnGenomeViewer.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genomeViewer.bmp"));
                btnGenomeViewer.addSelectionListener(this);
            }
            {
                Label lblDisplayTheseBiocondition = new Label(composite_1, SWT.WRAP);
                GridData gd_lblDisplayTheseBiocondition = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
                gd_lblDisplayTheseBiocondition.widthHint = 200;
                lblDisplayTheseBiocondition.setLayoutData(gd_lblDisplayTheseBiocondition);
                lblDisplayTheseBiocondition.setText("Visualize their transcriptomics datasets with the Genome Viewer");
                // lblDisplayTheseBiocondition.setForeground(BasicColor.GREY);
            }

            Label label = new Label(composite_1, SWT.NONE);
            label.setText("     ");

            btnHeatmap = new Button(composite_1, SWT.TOGGLE);
            btnHeatmap.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/compareexpression.bmp"));
            btnHeatmap.addSelectionListener(this);
            btnHeatmap.setBackground(BasicColor.BUTTON);

            Label lblDisplayDataComparisons = new Label(composite_1, SWT.WRAP);
            GridData gd_lblDisplayDataComparisons = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_lblDisplayDataComparisons.widthHint = 200;
            lblDisplayDataComparisons.setLayoutData(gd_lblDisplayDataComparisons);
            lblDisplayDataComparisons
                    .setText("Visualize differently expressed genes and non-coding RNAs with the HeatMap Viewer");
            new Label(composite_1, SWT.NONE);
        }

        Composite composite_2 = new Composite(compositeTable, SWT.BORDER);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(10, false));

        btnSelectall = new Button(composite_2, SWT.TOGGLE);
		btnSelectall.setBackground(BasicColor.BUTTON);
		btnSelectall.setToolTipText("Select all genomes");
		btnSelectall.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/checked.bmp"));
		btnSelectall.addSelectionListener(this);
		btnSelectall.setText("Select all");
		btnSelectall.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));

		btnUnselectall = new Button(composite_2, SWT.TOGGLE);
		btnUnselectall.setBackground(BasicColor.BUTTON);
		btnUnselectall.setToolTipText("Unselect all genomes");
		btnUnselectall.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/unchecked.bmp"));
		btnUnselectall.addSelectionListener(this);
		btnUnselectall.setText("Unselect all");
		btnUnselectall.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));

        btnSaveTxt = new Button(composite_2, SWT.TOGGLE);
        btnSaveTxt.setBackground(BasicColor.BUTTON);
        btnSaveTxt.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/txt.bmp"));
        btnSaveTxt.setToolTipText("Download the table in tabulated text format");
        btnSaveTxt.setText("Download transcriptome selection as a table");
        btnSaveTxt.setFont(SWTResourceManager.getBodyFont(12, SWT.NORMAL));

        new Label(composite_2, SWT.NONE);
        new Label(composite_2, SWT.NONE);
        new Label(composite_2, SWT.NONE);
        btnSaveTxt.addSelectionListener(this);

        tableBioConditionViewer = new TableViewer(compositeTable, SWT.BORDER | SWT.MULTI);
        tableBioCondition = tableBioConditionViewer.getTable();
        tableBioCondition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        tableBioCondition.setHeaderVisible(true);
        tableBioCondition.setLinesVisible(true);
        RWTUtils.setMarkup(tableBioCondition);
        tableBioConditionViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                for (int i : tableBioCondition.getSelectionIndices()) {
                    String selectedGenome = tableBioCondition.getItem(i).getText(columnNames.indexOf("Data Name") + 1);
                    if (!selectedGenome.equals("")) {
                        if (selectedTranscriptomes.contains(selectedGenome)) {
                            if (tableBioCondition.getSelectionIndices().length == 1) {
                                selectedTranscriptomes.remove(selectedGenome);
                                tableBioConditionViewer.replace(tableBioConditionViewer.getTable().getItem(i).getData(),
                                        i);
                            }
                        } else {
                            System.out.println("add: " + selectedGenome);
                            selectedTranscriptomes.add(selectedGenome);
                            tableBioConditionViewer.replace(tableBioConditionViewer.getTable().getItem(i).getData(), i);
                        }
                    }
                }
            }
        });

        {
            Composite composite_1 = new Composite(compositeTable, SWT.BORDER);
            composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            composite_1.setLayout(new GridLayout(8, false));

            Label label_1 = new Label(composite_1, SWT.NONE);
            label_1.setImage(imageGeneExpr);
            Label lblGeneExpressionArray = new Label(composite_1, SWT.NONE);
            lblGeneExpressionArray.setText("Gene expression array");
/*
            Label lblNewLabel = new Label(composite_1, SWT.NONE);
            lblNewLabel.setImage(imageTiling);
            Label lblTilingArrayAvailable = new Label(composite_1, SWT.NONE);
            lblTilingArrayAvailable.setText("Tiling array");

            
            Label lblNewLabel_1 = new Label(composite_1, SWT.NONE);
            lblNewLabel_1.setImage(imageTSS);
            Label lblTssAvailable = new Label(composite_1, SWT.NONE);
            lblTssAvailable.setText("TSS and TermSeq");
*/
            Label label = new Label(composite_1, SWT.NONE);
            label.setImage(imageRNASeq);

            Label lblRnaseq = new Label(composite_1, SWT.NONE);
            lblRnaseq.setText("RNA-Seq and Ribo-Seq");
        }
        tableBioConditionViewer.getTable().addSelectionListener(this);
        new Label(container, SWT.NONE);
        setData();
        if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT
                || Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT|| 
                Database.getInstance().getProjectName() == Database.YERSINIOMICS_PROJECT|| 
                Database.getInstance().getProjectName() == Database.URY_YERSINIOMICS_PROJECT|| 
                Database.getInstance().getProjectName() == Database.CLOSTRIDIOMICS_PROJECT || 
                Database.getInstance().getProjectName() == Database.STAPHYLOMICS_PROJECT ) {
            compositeDataFilter.updateInfo();
        }
        compositeDataFilter.redraw();

    }

    /**
     * Set all starting variables
     */
    private void setData() {

        /*
         * Load Table
         */
        bioCondsArray = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesArrayPath());
        bioConds = TabDelimitedTableReader.readList(Database.getInstance().getTranscriptomesArrayPath(), true, true);
        bioCondsToDisplay =
                TabDelimitedTableReader.readList(Database.getInstance().getTranscriptomesArrayPath(), true, true);
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
        if (Database.getInstance().getProjectName() != Database.URY_YERSINIOMICS_PROJECT) {
            ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<String[]>();
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Bibliographical reference")];
                if (info.contains("Unpublished (URY)")) {
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
        if (Database.getInstance().getSpecies().equals("Yersinia")) {
            TreeSet<String> mutantSet = new TreeSet<>();
            for (String[] rows : bioConds) {
                String mutants = rows[ArrayUtils.findColumn(bioCondsArray, "Mutant")];
                                for (String mutant : mutants.split(",")) {
                    if (!mutant.equals("")) {
                        mutant = mutant.trim();
                        /*
                        Genome genome = Genome.loadEgdeGenome();
                        Sequence seq = genome.getElement(mutant);
                        if (seq != null && seq instanceof Gene) {
                            Gene gene = (Gene) seq;
                            String text = gene.getName();
                            if (!gene.getGeneName().equals(""))
                                text += " (" + gene.getGeneName() + ")";
                            mutantSet.add(text);
                        } else {
                        */
                            mutantSet.add(mutant.trim());
                        //}
                    }
                }
            }
            for (String mutant : mutantSet) {
                compositeDataFilter.getComboMutant().add(mutant);
            }
            compositeDataFilter.getComboMutant().select(0);
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
        for (TableColumn col : tableBioConditionViewer.getTable().getColumns()) {
            col.dispose();
        }
        tableBioConditionViewer.setContentProvider(new ArrayContentProvider());

        createColumns();

        tableBioConditionViewer.getTable().setHeaderVisible(true);
        tableBioConditionViewer.getTable().setLinesVisible(true);
        /*
         * Remove first row
         */
        tableBioConditionViewer.setInput(bioCondsToDisplay);
        comparatorBioCondition = new BioConditionComparator(columnNames);
        tableBioConditionViewer.setComparator(comparatorBioCondition);
        for (int i = 0; i < tableBioConditionViewer.getTable().getColumnCount(); i++) {
            tableBioConditionViewer.getTable().getColumn(i).pack();
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

    /**
     * Create all column = initiate table
     * 
     * @param title
     * @param colNumber
     * @return
     */
    private TableViewerColumn createTableViewerColumn(String title, final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(tableBioConditionViewer, SWT.FILL);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setResizable(true);
        column.setMoveable(true);
        column.addSelectionListener(getSelectionAdapter(column, colNumber));
        return viewerColumn;
    }

    /**
     * Create one column
     */
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
                if (selectedTranscriptomes.contains(bioCond[columnNames.indexOf("Data Name")])) {
                    image = ResourceManager.getPluginImage("bacnet.core", "icons/checked.bmp");
                } else {
                    image = ResourceManager.getPluginImage("bacnet.core", "icons/unchecked.bmp");
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
                    System.out.println("colName: "+colName);
                    if(k!=bioCond.length) {
                    	String text = bioCond[k];
                        System.out.println("text: "+text);

	                    if (colName.equals("Type")) {
	                        return "";
	                    } else if (colName.equals("Strain used")) {
							return "<i>" + text.substring(0,text.indexOf(" ", 10)) + "</i>" + text.substring(text.indexOf(" ", 10), text.length());
	                    } else if (colName.equals("Reference strain")) {
							return "<i>" + text.substring(0,text.indexOf(" ", 10)) + "</i>" + text.substring(text.indexOf(" ", 10), text.length());
	                    } else if (colName.equals("Bibliographical reference")) {
	                        return RWTUtils.setPubMedLink(text);
	                    } else if (colName.equals("ENA project")) {
	                        return RWTUtils.setENAExpLink(text);
	                    } else if (colName.equals("DESeq2 report")) {
	                        return RWTUtils.setRNAdiffLink(text);
	                    } else if (colName.equals("GEO project")) {
	                        return RWTUtils.setGEOLink(text);
	                    } else if (colName.equals("GEO platform")) {
	                        return RWTUtils.setGEOLink(text);
	                    } else if (colName.equals("ArrayExpressID")) {
	                        return RWTUtils.setArrayExpressExpLink(text);
	                    } else if (colName.equals("ArrayExpressTechnoID")) {
	                        return RWTUtils.setArrayExpressArrayLink(text);
	                    } else {
	                        return text;
	                    }
                    } else {
                    	return "";
                    }
                }

                @Override
                public Image getImage(Object element) {
                    String[] bioCond = (String[]) element;
                    if (k == columnNames.indexOf("Type")) {
                        String typeDataContained = bioCond[k];
                        if (typeDataContained.contains(TypeData.TSS + "")
                                && typeDataContained.contains(TypeData.GeneExpr + "")) {
                            return imageTSSTilingGeneExpr;
                        } else if (typeDataContained.contains(TypeData.TSS + "")) {
                            return imageTSS;
                        } else if (typeDataContained.contains(TypeData.TermSeq + "")) {
                            return imageTSS;
                        } else if (typeDataContained.contains(TypeData.RNASeq + "")) {
                            return imageRNASeq;
                        } else if (typeDataContained.contains(TypeData.RiboSeq + "")) {
                            return imageRNASeq;
                        } else if (typeDataContained.contains(TypeData.Tiling + "")) {
                            return imageTilingGeneExpr;
                        } else {
                            return imageGeneExpr;
                        }
                    } else
                        return null;
                }

            });
            col2.getColumn().pack();
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
                System.out.println("click on column" + e.getSource());
                comparatorBioCondition.setColumn(index - 1);
                int dir = comparatorBioCondition.getDirection();
                tableBioConditionViewer.getTable().setSortDirection(dir);
                tableBioConditionViewer.getTable().setSortColumn(column);
                tableBioConditionViewer.refresh();
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
        for (String bioCondName : selectedTranscriptomes) {
            System.out.println("select: " + bioCondName);
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
    public static void displayTranscriptomicsView(EPartService partService, HashMap<String, String> parameters) {
        partService.showPart(ID, PartState.ACTIVATE);
        // update data
        MPart part = partService.findPart(ID);
        TranscriptomicsView view = (TranscriptomicsView) part.getObject();
        TranscriptomicsDataFilterComposite compositeDataFilter = view.getCompositeDataFilter();
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
                Combo combo2 = compositeDataFilter.getComboMutant();
                for (int i = 0; i < combo2.getItemCount(); i++) {
                    String item = combo2.getItem(i);
                    if (item.equals(stateValue)) {
                        combo2.select(i);
                        compositeDataFilter.getBtnChooseOneMutant().setSelection(true);
                        compositeDataFilter.getBtnAllMutant().setSelection(false);
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
            HashMap<String, ArrayList<String>> genomeToComparisons = new HashMap<>();
            try {
                IRunnableWithProgress thread = new OpenExpressionMatrixAndComparisons(selectedTranscriptomes, genomeToComparisons, true);
                new ProgressMonitorDialog(shell).run(true, false, thread);
                HeatMapTranscriptomicsView.displayBioConditionsExpressionAtlas(genomeToComparisons, partService);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == btnSelectall) {
            selectedTranscriptomes = new ArrayList<>();
            tableBioCondition.selectAll();
            for (int i : tableBioCondition.getSelectionIndices()) {
                String selectedGenome = tableBioCondition.getItem(i).getText(columnNames.indexOf("Data Name") + 1);
                if (!selectedTranscriptomes.contains(selectedGenome)) {
                    selectedTranscriptomes.add(selectedGenome);
                }
            }
            tableBioConditionViewer.refresh();
        } else if (e.getSource() == btnUnselectall) {
            selectedTranscriptomes = new ArrayList<>();
            tableBioCondition.deselectAll();
            tableBioConditionViewer.refresh();
        } else if (e.getSource() == btnSaveTxt) {
            String[][] arrayToSave = new String[1][columnNames.size()];
            for (int i = 0; i < columnNames.size(); i++) {
                arrayToSave[0][i] = columnNames.get(i);
            }
            int k = 1;
            for (int i = 1; i < bioCondsArray.length; i++) {
                String transcript = bioCondsArray[i][columnNames.indexOf("Data Name")];
                if (selectedTranscriptomes.contains(transcript)) {
                    arrayToSave = ArrayUtils.addRow(arrayToSave, ArrayUtils.getRow(bioCondsArray, i), k);
                    k++;
                }
            }
            String arrayRep = ArrayUtils.toString(arrayToSave);
            String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(arrayToSave);
            SaveFileUtils.saveTextFile(Database.getInstance().getSpecies() + "_Transcriptomic_Table.txt", arrayRep, true, "", arrayRepHTML,
                    partService, shell);
       /* } else if (e.getSource() == btnHelp) {
            HelpPage.helpTrancriptomicView(partService);*/
        } else {
            compositeDataFilter.updateBioConditionList();
            compositeDataFilter.pushState();
            //updateBioConditionTable();
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

    public ArrayList<String[]> getBioCondsToDisplay() {
        return bioCondsToDisplay;
    }

    public void setBioCondsToDisplay(ArrayList<String[]> bioCondsToDisplay) {
        this.bioCondsToDisplay = bioCondsToDisplay;
    }

    public ArrayList<String[]> getBioConds() {
        return bioConds;
    }

    public void setBioConds(ArrayList<String[]> bioConds) {
        this.bioConds = bioConds;
    }

    public TranscriptomicsDataFilterComposite getCompositeDataFilter() {
        return compositeDataFilter;
    }

    public void setCompositeDataFilter(TranscriptomicsDataFilterComposite compositeDataFilter) {
        this.compositeDataFilter = compositeDataFilter;
    }

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
    }
}
