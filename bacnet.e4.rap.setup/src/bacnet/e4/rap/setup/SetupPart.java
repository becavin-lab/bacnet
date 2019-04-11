package bacnet.e4.rap.setup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeConversion;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.database.BioConditionCreation;
import bacnet.scripts.database.DataFolder;
import bacnet.scripts.database.DataValidation;
import bacnet.scripts.database.DatabaseCreation;
import bacnet.scripts.database.GenomesCreation;
import bacnet.scripts.database.ProteomicsCreation;
import bacnet.scripts.database.TranscriptomesCreation;
import bacnet.swt.ResourceManager;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import bacnet.utils.VectorUtils;

/**
 * SetupPart for initialisation and creation of the Database
 * 
 * @author cbecavin
 *
 */
public class SetupPart implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3563942149356313184L;

    @Inject
    EPartService partService;

    @Inject
    EModelService modelService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)

    private Shell shell;
    private Label lblPathData;
    private Combo comboProject;
    private Label lblBioconditions;
    private Button btnAddBiologicalConditions;
    private Button btnAddGenomes;
    private Button btnValidateGenomicsDatabase;
    private Label labelGenomes;
    private Label label_Transcriptomics;
    private Label lblBioCondPath;
    private Label lblPathGenomics;
    private Label lblTranscriPath;
    private Label lblTechno;
    private Label lblProteomePath;
    private Label labelProteomics;
    private Text console;
    private Table tableBioCondition;
    private Table tableTranscriptomes;
    private Button btnValidateBioconditionsDatabase;
    private Button buttonAddTranscriptomics;
    private Button buttonValidateTrancriptomics;
    private Button btnValidateProteomics;
    private Button btnAddProteomesFrom;
    private Table tableProteomics;
    private Table tableGenome;

    private DataValidation dataValidation = new DataValidation();
    private String logs = "";
    private Button btnCreateTranscriptomicsTable;
    private Button btnCreateProteomicsTable;
    private Label lblNewLabel_1;
    private Label lblHaveYouCreated;
    private TabItem tbtmComparisons;
    private Composite composite_8;
    private Table tableComparisons;
    private Label lblComparisonsPath;
    private Label lblComparisons;
    private Button btnValidateComparisonsDatabase;
    private Button btnAddComparisonsFrom;
    private Button btnCreateTranscriptomicsComparisons;
    private Button btnCreateLogfcTranscriptome;
    private Button btnCreateExprPrteome;
    private Button btnDownloadGenomesFrom;

    public SetupPart() {}

    /**
     * Create contents of the view part.
     * 
     * @throws IOException
     */
    @PostConstruct
    public void createControls(Composite parent) throws IOException {
        System.out.println("Run Setup page");

        SessionControl.initBacnetApp(partService, modelService, shell);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBounds(0, 0, 613, 448);
        composite.setLayout(new GridLayout(1, false));

        Label lblNewLabel = new Label(composite, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1));
        lblNewLabel.setText("Welcome to BACNET plateform");

        Label lblCreateupdateYourDatabase = new Label(composite, SWT.NONE);
        lblCreateupdateYourDatabase.setText("Create/update your database here");

        Composite composite_1 = new Composite(composite, SWT.NONE);
        composite_1.setLayout(new GridLayout(2, false));

        Label lblSelectedWebsite = new Label(composite_1, SWT.NONE);
        lblSelectedWebsite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSelectedWebsite.setText("Selected website :");

        comboProject = new Combo(composite_1, SWT.NONE);
        comboProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        for (String projectName : Database.getInstance().getListDatabases().keySet()) {
            comboProject.add(projectName);
        }
        comboProject.select(comboProject.indexOf(Database.getInstance().getProjectName()));
        comboProject.addSelectionListener(this);

        Composite composite_2 = new Composite(composite, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        composite_2.setLayout(new GridLayout(2, false));

        Label lblDatabasePath = new Label(composite_2, SWT.NONE);
        lblDatabasePath.setText("Database path : ");

        lblPathData = new Label(composite_2, SWT.NONE);
        lblPathData.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

        TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
        GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_tabFolder.heightHint = 315;
        tabFolder.setLayoutData(gd_tabFolder);

        TabItem tbtmGenomics = new TabItem(tabFolder, SWT.NONE);
        tbtmGenomics.setText("Genomics");

        Composite composite_4 = new Composite(tabFolder, SWT.NONE);
        tbtmGenomics.setControl(composite_4);
        composite_4.setLayout(new GridLayout(2, false));

        tableGenome = new Table(composite_4, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        tableGenome.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 7));
        tableGenome.setHeaderVisible(true);
        tableGenome.setLinesVisible(true);

        Label lblReadListOf_1 = new Label(composite_4, SWT.NONE);
        lblReadListOf_1.setText("Read list of Genomes in : ");

        lblPathGenomics = new Label(composite_4, SWT.NONE);
        lblPathGenomics.setText("New Label");

        labelGenomes = new Label(composite_4, SWT.NONE);
        labelGenomes.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        labelGenomes.setText("New Label");

        btnValidateGenomicsDatabase = new Button(composite_4, SWT.NONE);
        btnValidateGenomicsDatabase.setText("Validate Genomics database");
        btnValidateGenomicsDatabase.addSelectionListener(this);

        btnDownloadGenomesFrom = new Button(composite_4, SWT.NONE);
        btnDownloadGenomesFrom.setText("Download genomes from RefSeq");
        btnDownloadGenomesFrom.addSelectionListener(this);

        btnAddGenomes = new Button(composite_4, SWT.NONE);
        btnAddGenomes.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnAddGenomes.setText("Add unvalidated Genomes to the database");
        btnAddGenomes.addSelectionListener(this);
        btnAddGenomes.setToolTipText("! Validate first your datasets !");
        new Label(composite_4, SWT.NONE);
        TabItem tbtmBiologicalConditions = new TabItem(tabFolder, SWT.NONE);
        tbtmBiologicalConditions.setText("Biological Conditions");

        Composite composite_3 = new Composite(tabFolder, SWT.NONE);
        tbtmBiologicalConditions.setControl(composite_3);
        composite_3.setLayout(new GridLayout(2, false));

        tableBioCondition = new Table(composite_3, SWT.BORDER | SWT.FULL_SELECTION);
        tableBioCondition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 7));
        tableBioCondition.setHeaderVisible(true);
        tableBioCondition.setLinesVisible(true);

        Label lblReadListOf = new Label(composite_3, SWT.NONE);
        lblReadListOf.setText("Read List of Biological Conditions:");

        lblBioCondPath = new Label(composite_3, SWT.NONE);
        lblBioCondPath.setText("New Label");

        lblBioconditions = new Label(composite_3, SWT.NONE);
        lblBioconditions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        lblBioconditions.setText("New Label");

        btnValidateBioconditionsDatabase = new Button(composite_3, SWT.NONE);
        btnValidateBioconditionsDatabase.setText("Validate Bioconditions database");
        btnValidateBioconditionsDatabase.addSelectionListener(this);
        btnAddBiologicalConditions = new Button(composite_3, SWT.NONE);
        btnAddBiologicalConditions.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnAddBiologicalConditions.setText("Add unvalidated Biological conditions to the database");
        btnAddBiologicalConditions.setToolTipText("! Validate first your datasets !");
        btnCreateTranscriptomicsTable = new Button(composite_3, SWT.NONE);
        btnCreateTranscriptomicsTable.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnCreateTranscriptomicsTable.setText("Create Transcriptomics table");
        btnCreateTranscriptomicsTable.addSelectionListener(this);
        btnCreateProteomicsTable = new Button(composite_3, SWT.NONE);
        btnCreateProteomicsTable.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnCreateProteomicsTable.setText("Create Proteomics table");
        btnCreateProteomicsTable.addSelectionListener(this);
        btnAddBiologicalConditions.addSelectionListener(this);

        tbtmComparisons = new TabItem(tabFolder, 0);
        tbtmComparisons.setText("Comparisons");

        composite_8 = new Composite(tabFolder, SWT.NONE);
        tbtmComparisons.setControl(composite_8);
        composite_8.setLayout(new GridLayout(2, false));

        tableComparisons = new Table(composite_8, SWT.BORDER | SWT.FULL_SELECTION);
        tableComparisons.setLinesVisible(true);
        tableComparisons.setHeaderVisible(true);
        tableComparisons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 7));

        Label lblReadListOf_3 = new Label(composite_8, SWT.NONE);
        lblReadListOf_3.setText("Read List of Comparisons");

        lblComparisonsPath = new Label(composite_8, SWT.NONE);
        lblComparisonsPath.setText("New Label");

        lblComparisons = new Label(composite_8, SWT.NONE);
        lblComparisons.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        lblComparisons.setText("New Label");

        btnValidateComparisonsDatabase = new Button(composite_8, SWT.NONE);
        btnValidateComparisonsDatabase.setText("Validate Comparisons database");
        btnValidateComparisonsDatabase.addSelectionListener(this);
        btnAddComparisonsFrom = new Button(composite_8, SWT.NONE);
        btnAddComparisonsFrom.setToolTipText("! Validate first your datasets !");
        btnAddComparisonsFrom.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnAddComparisonsFrom.setText("Add unvalidated Comparisons to the database");
        btnAddComparisonsFrom.addSelectionListener(this);
        btnCreateTranscriptomicsComparisons = new Button(composite_8, SWT.NONE);
        btnCreateTranscriptomicsComparisons.setText("Create Transcriptomics Comparisons table");
        new Label(composite_8, SWT.NONE);
        btnCreateTranscriptomicsComparisons.addSelectionListener(this);

        TabItem tbtmTranscriptomics = new TabItem(tabFolder, SWT.NONE);
        tbtmTranscriptomics.setText("Transcriptomics");

        Composite composite_5 = new Composite(tabFolder, SWT.NONE);
        tbtmTranscriptomics.setControl(composite_5);
        composite_5.setLayout(new GridLayout(2, false));

        tableTranscriptomes = new Table(composite_5, SWT.BORDER | SWT.FULL_SELECTION);
        tableTranscriptomes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 8));
        tableTranscriptomes.setHeaderVisible(true);
        tableTranscriptomes.setLinesVisible(true);

        lblNewLabel_1 = new Label(composite_5, SWT.NONE);
        lblNewLabel_1.setText("Have you created the transcriptomics table in Bioconditions panel?");

        Label lblReadListOf_2 = new Label(composite_5, SWT.NONE);
        lblReadListOf_2.setText("Read list of Transcriptomes in : ");

        lblTranscriPath = new Label(composite_5, SWT.NONE);
        lblTranscriPath.setText("New Label");

        label_Transcriptomics = new Label(composite_5, SWT.NONE);
        label_Transcriptomics.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        label_Transcriptomics.setText("New Label");

        lblTechno = new Label(composite_5, SWT.WRAP);
        lblTechno.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lblTechno.setText("New Label");

        buttonValidateTrancriptomics = new Button(composite_5, SWT.NONE);
        buttonValidateTrancriptomics.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        buttonValidateTrancriptomics.setText("Validate Transcriptomics database");
        buttonValidateTrancriptomics.addSelectionListener(this);
        buttonAddTranscriptomics = new Button(composite_5, SWT.NONE);
        buttonAddTranscriptomics.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        buttonAddTranscriptomics.setText("Add unvalidated Transcriptomes to the database");
        buttonAddTranscriptomics.addSelectionListener(this);
        buttonAddTranscriptomics.setToolTipText("! Validate first your datasets !");

        btnCreateLogfcTranscriptome = new Button(composite_5, SWT.NONE);
        btnCreateLogfcTranscriptome.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnCreateLogfcTranscriptome.setText("Create TranscriptomeAtlas");
        btnCreateLogfcTranscriptome.addSelectionListener(this);
        btnCreateLogfcTranscriptome.setToolTipText("This table should be created for heatmap visualisation");

        TabItem tbtmProteomics = new TabItem(tabFolder, SWT.NONE);
        tbtmProteomics.setText("Proteomics");

        Composite composite_6 = new Composite(tabFolder, SWT.NONE);
        tbtmProteomics.setControl(composite_6);
        composite_6.setLayout(new GridLayout(2, false));

        tableProteomics = new Table(composite_6, SWT.BORDER | SWT.FULL_SELECTION);
        tableProteomics.setLinesVisible(true);
        tableProteomics.setHeaderVisible(true);
        tableProteomics.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 7));

        lblHaveYouCreated = new Label(composite_6, SWT.NONE);
        lblHaveYouCreated.setText("Have you created the proteomics table in Bioconditions panel?");

        Label label_1 = new Label(composite_6, SWT.NONE);
        label_1.setText("Read list of Transcriptomes in : ");

        lblProteomePath = new Label(composite_6, SWT.NONE);
        lblProteomePath.setText("Read list of Proteomes in : ");

        labelProteomics = new Label(composite_6, SWT.WRAP);
        labelProteomics.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        labelProteomics.setText("New Label");

        btnValidateProteomics = new Button(composite_6, SWT.NONE);
        btnValidateProteomics.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnValidateProteomics.setText("Validate Proteomics database");
        btnValidateProteomics.addSelectionListener(this);
        btnAddProteomesFrom = new Button(composite_6, SWT.NONE);
        btnAddProteomesFrom.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnAddProteomesFrom.setText("Add unvalidated Proteomes to the database");
        btnAddProteomesFrom.addSelectionListener(this);
        btnAddProteomesFrom.setToolTipText("! Validate first your datasets !");

        btnCreateExprPrteome = new Button(composite_6, SWT.NONE);
        btnCreateExprPrteome.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnCreateExprPrteome.setText("Create ProteomeAtlas");
        btnCreateExprPrteome.addSelectionListener(this);

        Composite composite_7 = new Composite(composite, SWT.NONE);
        composite_7.setLayout(new GridLayout(1, false));
        composite_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblConsole = new Label(composite_7, SWT.NONE);
        lblConsole.setText("Console: ");

        console = new Text(composite_7, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
        GridData gd_console = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_console.heightHint = 200;
        console.setLayoutData(gd_console);

        initProjectInfo();

        DatabaseCreation.preProcessing();

    }

    /*
     * Init Database if the selection has changed
     */
    private void initDatabase(String projectName) {
        Database database = Database.getInstance();
        database.setProjectName(projectName);
        database.initDatabase(shell);
        OmicsData.initStaticVariables();
        GenomeNCBI.initStaticVariables();
    }

    private void initProjectInfo() {
        logs = "";
        lblPathData.setText(Database.getDATA_PATH());
        DataFolder.createMissingFolders();
        dataValidation = new DataValidation();
        initGenomics();
        initBioconditions();
        initComparisons();
        initTranscriptomics();
        initProteomics();
        updateConsole();
    }

    private void initBioconditions() {
        lblBioCondPath.setText(Database.getInstance().getBioConditionsArrayPath());
        tableBioCondition.removeAll();
        if (FileUtils.exists(Database.getInstance().getBioConditionsArrayPath())) {
            String[][] bioConds = TabDelimitedTableReader.read(Database.getInstance().getBioConditionsArrayPath());
            lblBioconditions.setText("Found : " + bioConds.length + " Bioconditions in the database");
            tableBioCondition.setLinesVisible(true);
            tableBioCondition.setHeaderVisible(true);
            String[] titles = {"BioConditions", "Validated"};
            for (int i = 0; i < titles.length; i++) {
                TableColumn column = new TableColumn(tableBioCondition, SWT.NONE);
                column.setText(titles[i]);
            }
            TreeSet<String> setBioCond = new TreeSet<>();
            for (int i = 1; i < bioConds.length; i++) {
                setBioCond.add(bioConds[i][0]);
            }
            for (String biocond : setBioCond) {
                dataValidation.getBioconditions().put(biocond, false);
                TableItem item = new TableItem(tableBioCondition, SWT.NONE);
                item.setText(0, biocond);
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            for (int i = 0; i < titles.length; i++) {
                tableBioCondition.getColumn(i).pack();
            }
        } else {
            logs += "No Biological Conditions available\nCannot find file: "
                    + Database.getInstance().getBioConditionsArrayPath() + "\n";
            updateConsole();
        }
    }

    private void updateBioConditions() {
        int i = 0;
        for (String bioCondName : dataValidation.getBioconditions().keySet()) {
            TableItem item = tableBioCondition.getItem(i);
            item.setText(0, bioCondName);
            boolean validated = dataValidation.getBioconditions().get(bioCondName);
            if (validated) {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
            } else {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            i++;
        }
    }

    private void initComparisons() {
        lblComparisonsPath.setText(Database.getInstance().getExperimentComparisonTablePath());
        tableComparisons.removeAll();
        if (FileUtils.exists(Database.getInstance().getExperimentComparisonTablePath())) {
            String[][] bioConds =
                    TabDelimitedTableReader.read(Database.getInstance().getExperimentComparisonTablePath());
            lblBioconditions.setText("Found : " + bioConds.length + " Comparisons in the database");
            tableComparisons.setLinesVisible(true);
            tableComparisons.setHeaderVisible(true);
            String[] titles = {"Comparisons", "Validated"};
            for (int i = 0; i < titles.length; i++) {
                TableColumn column = new TableColumn(tableComparisons, SWT.NONE);
                column.setText(titles[i]);
            }
            TreeSet<String> setBioCond = new TreeSet<>();
            for (int i = 1; i < bioConds.length; i++) {
                setBioCond.add(bioConds[i][0] + " vs " + bioConds[i][1]);
            }
            for (String biocond : setBioCond) {
                dataValidation.getComparisons().put(biocond, false);
                TableItem item = new TableItem(tableComparisons, SWT.NONE);
                item.setText(0, biocond);
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            for (int i = 0; i < titles.length; i++) {
                tableComparisons.getColumn(i).pack();
            }
        } else {
            logs += "No Comparisons available\nCannot find file: "
                    + Database.getInstance().getExperimentComparisonTablePath() + "\n";
            updateConsole();
        }

        if (!FileUtils.exists(Database.getInstance().getTranscriptomesComparisonsArrayPath())) {
            logs += "No Transcriptomes Comparisons table available\nCannot find file: "
                    + Database.getInstance().getTranscriptomesComparisonsArrayPath() + "\n";
            logs += "Have you clicked on Create Transcriptomics Comparisons table in Comparisons panel?\n";
            updateConsole();
        }
    }

    private void updateComparisons() {
        int i = 0;
        for (String compName : dataValidation.getComparisons().keySet()) {
            TableItem item = tableComparisons.getItem(i);
            item.setText(0, compName);
            boolean validated = dataValidation.getComparisons().get(compName);
            if (validated) {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
            } else {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            i++;
        }
    }

    private void initGenomics() {
        tableGenome.removeAll();
        lblPathGenomics.setText(Database.getInstance().getGenomeArrayPath());
        if (FileUtils.exists(Database.getInstance().getGenomeArrayPath())) {
            ArrayList<String> genomes = Genome.getAvailableGenomes();
            labelGenomes.setText("Found : " + genomes.size() + " Genomes in the database");
            tableGenome.setLinesVisible(true);
            tableGenome.setHeaderVisible(true);
            String[] titles = {"Genomes", "Validated"};
            for (int i = 0; i < titles.length; i++) {
                TableColumn column = new TableColumn(tableGenome, SWT.NONE);
                column.setText(titles[i]);
            }
            for (String genome : genomes) {
                dataValidation.getGenomes().put(genome, false);
                TableItem item = new TableItem(tableGenome, SWT.NONE);
                item.setText(0, genome);
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            for (int i = 0; i < titles.length; i++) {
                tableGenome.getColumn(i).pack();
            }
        } else {
            logs += "No genomes available\nCannot find file: " + Database.getInstance().getGenomeArrayPath() + "\n";
            updateConsole();
        }
    }

    private void updateGenomics() {
        int i = 0;
        for (String genome : dataValidation.getGenomes().keySet()) {
            TableItem item = tableGenome.getItem(i);
            item.setText(0, genome);
            boolean validated = dataValidation.getGenomes().get(genome);
            if (validated) {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
            } else {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            i++;
        }
        if (!FileUtils.exists(Database.getInstance().getGenomeArrayPath())) {
            logs += "No genomes summary table available\nCannot find file: "
                    + Database.getInstance().getGenomeArrayPath() + "\nYou need to create that table manually!";
            updateConsole();
        }
    }

    private void initTranscriptomics() {
        tableTranscriptomes.removeAll();
        lblTranscriPath.setText(Database.getInstance().getTranscriptomesArrayPath());
        if (FileUtils.exists(Database.getInstance().getTranscriptomesArrayPath())) {
            String[][] bioConds = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesArrayPath());
            TreeSet<String> listType = new TreeSet<>();
            label_Transcriptomics.setText("Found : " + bioConds.length + " Transcriptomes in the database");
            tableTranscriptomes.setLinesVisible(true);
            tableTranscriptomes.setHeaderVisible(true);
            String[] titles = {"Transcriptomes", "Validated"};
            for (int i = 0; i < titles.length; i++) {
                TableColumn column = new TableColumn(tableTranscriptomes, SWT.NONE);
                column.setText(titles[i]);
            }
            for (int i = 1; i < bioConds.length; i++) {
                dataValidation.getTranscriptomes().put(bioConds[i][0], false);
                for (String temp : bioConds[i][1].split(",")) {
                    listType.add(temp);
                }
                TableItem item = new TableItem(tableTranscriptomes, SWT.NONE);
                item.setText(0, bioConds[i][0]);
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            label_Transcriptomics.setText("Found : " + bioConds.length + " Transcriptomes in the database");
            lblTechno.setText("Type of data: " + VectorUtils.toString(listType));
            for (int i = 0; i < titles.length; i++) {
                tableTranscriptomes.getColumn(i).pack();
            }
        } else {
            logs += "No Transcriptomes available\nCannot find file: "
                    + Database.getInstance().getTranscriptomesArrayPath() + "\n";
            logs += "Have you clicked on Create Transcriptomics table in BioConditions panel?\n";
            updateConsole();
        }
        logs += "Have you clicked on Create TranscriptomeAtlas?\n";
        updateConsole();
    }

    private void updateTranscriptomics() {
        int i = 0;
        for (String bioCondName : dataValidation.getTranscriptomes().keySet()) {
            TableItem item = tableTranscriptomes.getItem(i);
            item.setText(0, bioCondName);
            boolean validated = dataValidation.getTranscriptomes().get(bioCondName);
            if (validated) {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
            } else {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            i++;
        }
        logs += "Create a new TranscriptomeAtlas, if you have just added transcriptomes\n";
        updateConsole();
    }

    private void initProteomics() {
        lblProteomePath.setText(Database.getInstance().getProteomesArrayPath());
        tableProteomics.removeAll();
        if (FileUtils.exists(Database.getInstance().getProteomesArrayPath())) {
            String[][] bioConds = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
            labelProteomics.setText("Found : " + bioConds.length + " Proteomes in the database");
            labelProteomics.setText("Found : " + bioConds.length + " Proteomes in the database");
            tableProteomics.setLinesVisible(true);
            tableProteomics.setHeaderVisible(true);
            String[] titles = {"Proteomes", "Validated"};
            for (int i = 0; i < titles.length; i++) {
                TableColumn column = new TableColumn(tableProteomics, SWT.NONE);
                column.setText(titles[i]);
            }
            for (int i = 1; i < bioConds.length; i++) {
                dataValidation.getProteomes().put(bioConds[i][0], false);
                TableItem item = new TableItem(tableProteomics, SWT.NONE);
                item.setText(0, bioConds[i][0]);
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            for (int i = 0; i < titles.length; i++) {
                tableProteomics.getColumn(i).pack();
            }
        } else {
            logs += "No Proteomes available\nCannot find file: " + Database.getInstance().getProteomesArrayPath()
                    + "\n";
            updateConsole();
        }
        logs += "Have you clicked on Create ProteomeAtlas?\n";
        updateConsole();
    }

    private void updateProteomics() {
        int i = 0;
        for (String bioCondName : dataValidation.getProteomes().keySet()) {
            TableItem item = tableProteomics.getItem(i);
            item.setText(0, bioCondName);
            boolean validated = dataValidation.getProteomes().get(bioCondName);
            if (validated) {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/checked.bmp"));
            } else {
                item.setImage(1, ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp"));
            }
            i++;
        }
        logs += "Please create a ProteomeAtlas, if you have just added proteomes\n";
        updateConsole();
    }

    private void updateConsole() {
        console.setText(logs);
    }

    @PreDestroy
    public void dispose() {}

    @Focus
    public void setFocus() {
        // TODO Set the focus to control
    }

    /**
     * Ran when the BannerView is opened
     */
    public static void runTests() {

    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == comboProject) {
            String projectName = comboProject.getItem(comboProject.getSelectionIndex());
            initDatabase(projectName);
            initProjectInfo();
        } else if (e.getSource() == btnValidateGenomicsDatabase) {
            logs += "--- Validate genomes\n";
            logs = dataValidation.validateGenomes(logs);
            updateConsole();
            updateGenomics();
        } else if (e.getSource() == btnDownloadGenomesFrom) {
            logs += "--- Download genomes from RefSeq\n";
            System.out.println("yo");
            String[][] newGenomes = TabDelimitedTableReader.read(Database.getInstance().getGenomeArrayPath());
            int index = ArrayUtils.findColumn(newGenomes, "RefSeq FTP");
            System.out.println("yes");
            if (index == -1) {
                logs += "No RefSeq available in " + Database.getInstance().getGenomeArrayPath() + "\n";
                logs += "Impossible to download the genomes";
            } else {
                ArrayList<String> listGenomes = new ArrayList<>();
                for (String genome : dataValidation.getGenomes().keySet()) {
                    if (!dataValidation.getGenomes().get(genome)) {
                        listGenomes.add(genome);
                    }
                }
                GenomesCreation.downloadGenomes(listGenomes);
            }
        } else if (e.getSource() == btnAddGenomes) {
            logs += "--- Add genomes to database\n";
            for (String genome : dataValidation.getGenomes().keySet()) {
                if (!dataValidation.getGenomes().get(genome)) {
                    logs += "Convert genome: " + genome + "\n";
                    GenomeNCBI genomeNCBI = GenomeNCBITools.loadGenome(genome, GenomeNCBI.PATH_GENOMES, false, true);
                    GenomeConversion.run(genomeNCBI, GenomeNCBI.PATH_GENOMES + "/" + genome, genome);
                    Genome genomeTemp = Genome.loadGenome(genome);
                    if (genomeTemp == null) {
                        logs += genome + " does not exists. Click: Add unvalidated Genome to the database" + "\n";
                    } else if (genomeTemp.getFirstChromosome().getGenes().size() == 0) {
                        logs += genome + " was not added to the database, check errors in the console" + "\n";
                    } else {
                        logs += genome + " was added to the database" + "\n";
                        dataValidation.getGenomes().put(genome, true);
                    }
                }
            }
            updateConsole();
            updateGenomics();
        } else if (e.getSource() == btnValidateBioconditionsDatabase) {
            logs += "--- Validate bioconditions\n";
            logs = dataValidation.validateBioConditions(logs);
            updateConsole();
            updateBioConditions();
        } else if (e.getSource() == btnAddBiologicalConditions) {
            logs += "--- Add bioconditions\n";
            ArrayList<String> bioConditions = new ArrayList<>();
            for (String bioCondName : dataValidation.getBioconditions().keySet()) {
                if (!dataValidation.getBioconditions().get(bioCondName)) {
                    bioConditions.add(bioCondName);
                }
            }
            logs = BioConditionCreation.addExperimentFromTable(Database.getInstance().getBioConditionsArrayPath(),
                    bioConditions, logs);
            logs = dataValidation.validateBioConditions(logs);
            updateConsole();
            updateBioConditions();
        } else if (e.getSource() == btnCreateTranscriptomicsTable) {
            logs = "--- Create Transcriptomics table\n";
            BioConditionCreation.createSummaryTranscriptomesTable();
            logs += "Transcriptomics table saved : " + Database.getInstance().getTranscriptomesArrayPath() + "\n";
            updateConsole();
            initTranscriptomics();
        } else if (e.getSource() == btnCreateProteomicsTable) {
            logs = "--- Create Proteomics table\n";
            BioConditionCreation.createSummaryProteomesTable();
            logs += "Proteomics table saved : " + Database.getInstance().getProteomesArrayPath() + "\n";
            updateConsole();
            initProteomics();
        } else if (e.getSource() == btnValidateComparisonsDatabase) {
            logs += "--- Validate comparisons\n";
            logs = dataValidation.validateComparisons(logs);
            updateConsole();
            updateComparisons();
        } else if (e.getSource() == btnAddComparisonsFrom) {
            logs += "--- Add comparisons\n";
            ArrayList<String> comparisons = new ArrayList<>();
            for (String bioCondName : dataValidation.getComparisons().keySet()) {
                if (!dataValidation.getComparisons().get(bioCondName)) {
                    comparisons.add(bioCondName);
                }
            }
            logs = BioConditionCreation.addComparisonFromTable(comparisons, logs);
            logs = dataValidation.validateComparisons(logs);
            updateConsole();
            updateComparisons();
        } else if (e.getSource() == btnCreateTranscriptomicsComparisons) {
            logs = "--- Create Transcriptomics Comparisons table\n";
            BioConditionCreation.createSummaryTranscriptomesComparisonsTable();
            logs += "Transcriptomics table saved : " + Database.getInstance().getTranscriptomesComparisonsArrayPath()
                    + "\n";
            updateConsole();
        } else if (e.getSource() == buttonValidateTrancriptomics) {
            logs += "--- Validate transcriptomics\n";
            logs = dataValidation.validateTranscriptomics(logs);
            updateConsole();
            updateTranscriptomics();
        } else if (e.getSource() == buttonAddTranscriptomics) {
            logs += "--- Add transcriptomes\n";
            ArrayList<String> bioConds = new ArrayList<>();
            for (String bioCondName : dataValidation.getTranscriptomes().keySet()) {
                if (!dataValidation.getTranscriptomes().get(bioCondName)) {
                    bioConds.add(bioCondName);
                }
            }
            logs = TranscriptomesCreation.addTranscriptomeToDatabase(bioConds, logs);
            logs = dataValidation.validateTranscriptomics(logs);
            updateConsole();
            updateTranscriptomics();
        } else if (e.getSource() == btnCreateLogfcTranscriptome) {
            logs += "--- Create TranscriptomeAtlas\n";
            ArrayList<String> bioConds = new ArrayList<>();
            for (String bioCondName : dataValidation.getTranscriptomes().keySet()) {
                bioConds.add(bioCondName);
            }
            logs = TranscriptomesCreation.createLogfcTranscriptomeTable(bioConds, logs, true);
            updateConsole();
        } else if (e.getSource() == btnValidateProteomics) {
            logs += "--- Validate proteomics\n";
            logs = dataValidation.validateProteomics(logs);
            updateConsole();
            updateProteomics();
        } else if (e.getSource() == btnAddProteomesFrom) {
            logs += "--- Add proteomics\n";
            ArrayList<String> bioConds = new ArrayList<>();
            for (String bioCondName : dataValidation.getProteomes().keySet()) {
                if (!dataValidation.getProteomes().get(bioCondName)) {
                    bioConds.add(bioCondName);
                }
            }
            logs += "Convert all Proteomics data" + "\n";
            ProteomicsCreation.addProteomesToDatabase(bioConds);
            logs = dataValidation.validateProteomics(logs);
            updateConsole();
            updateProteomics();
        } else if (e.getSource() == btnCreateExprPrteome) {
            logs += "--- Create ProteomeAtlas\n";
            ArrayList<String> bioConds = new ArrayList<>();
            for (String bioCondName : dataValidation.getProteomes().keySet()) {
                bioConds.add(bioCondName);
            }
            ProteomicsCreation.createProteomeTable(bioConds, logs);
            updateConsole();
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }
}
