package bacnet;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.swt.widgets.Shell;
import bacnet.datamodel.annotation.Signature;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.TabDelimitedTableReader;

/**
 * A ModelProvider specifically design for the Website Database<br>
 * Contains persistent variable, used through all the website. It is loaded for each User, it is not
 * shared by users
 * 
 * @author UIBC
 *
 */
public class Database {
    /*
     * For eclipse.rcp
     */
    // public enum Database {
    // INSTANCE;

    /**
     * If exlipse.rap
     */
    /**
     * NavigationListener
     */
    private BrowserNavigationListener navigationListener;

    public BrowserNavigationListener getNavigationListener() {
        return navigationListener;
    }

    public void setNavigationListener(BrowserNavigationListener navigationListener) {
        this.navigationListener = navigationListener;
    }

    /**
     * Static variable to discriminate BACNET project from the others
     */
    public static String BACNET = "Bacnet";

    /**
     * Static variable to discriminate Listeriomics project from the others
     */
    public static String LISTERIOMICS_PROJECT = "Listeriomics";
    /**
     * Static variable to discriminate Listeriomics project from the others
     */
    public static String YERSINIOMICS_PROJECT = "Yersiniomics";
    /**
     * Static variable to discriminate Listeriomics project from the others
     */
    public static String URY_YERSINIOMICS_PROJECT = "URY_Yersiniomics";

    /**
     * Static variable to discriminate Listeriomics project from the others
     */
    public static String UIBCLISTERIOMICS_PROJECT = "UIBCListeriomics";
    /**
     * Static variable to discriminate Listeriomics project from the others
     */
    public static String CRISPRGO_PROJECT = "CRISPRGo";
    /**
     * Static variable to discriminate Listeriomics project from the others
     */
    public static String LEISHOMICS_PROJECT = "Leishomics";

    /**
     * Path when using MAC OX X
     */
    public static String PATH_MAC = "/mnt/c/bacnet/BacnetDatabases.ini";
    /**
     * Path for website database on Windows
     */
    public static String PATH_WIN = "C:\\bacnet\\BacnetDatabases.ini";
    //public static String PATH_WIN = "/mnt/c/bacnet/BacnetDatabases.ini";
    //public static String PATH_WIN = "C:\\Users\\ipmc\\Documents\\BACNET\\bacnet-private\\BacnetDatabases.ini";
    //public static String PATH_WIN = "C:\\Users\\Pierre\\Documents\\Yersiniomics\\bacnet\\BacnetDatabases.ini";

    /**
     * Path for website database on Pasteur Virtual Machine
     */
    public static String PATH_WEBSITE = "/srv/yersiniomics/BacnetDatabases.ini";
    //public static String PATH_WEBSITE="C:\\bacnet\\BacnetDatabases.ini";
    //public static String PATH_WEBSITE="/mnt/c/bacnet/BacnetDatabases.ini";
    
    /*******************************************************************
     * Database information
     ********************************************************************/
    /**
     * Name of the current project loaded
     */
    private String projectName = "ListeriomicsSample";
    /**
     * The absolute path on the database folder
     */
    private String path = "";
    /*
     * List of databases available
     */
    private HashMap<String, String> listDatabases = new HashMap<>();
    /**
     * Hashmap of database features
     */
    private HashMap<String, String> databaseFeatures = new HashMap<>();
    /**
     * Init View
     */
    private String initView = "";
    /**
     * Species which the website uses
     */
    private String species = "";
    /**
     * Logo = BannerView image
     */
    private String logo = "";

    /*******************************
     * URL at start of the application
     */
    private String currentState = "";

    /**
     * Title of the Webpage
     */
    private String webpageTitle = "";

    /**
     * Google Stat Id (if you want to have stats on your website)
     */
    private String googleId = "";
    /*******
     * Database folders
     */

    /**
     * Path for the genome table
     */
    private String genomeArrayPath = "";
    /**
     * Path for the transcriptome table
     */
    private String transcriptomesArrayPath = "";
    /**
     * Path for the transcriptome comparisons table
     */
    private String transcriptomesComparisonsArrayPath = "";
    /**
     * Path for the proteome table
     */
    private String proteomesArrayPath = "";
    /**
     * Path for the transcriptome comparisons table
     */
    private String proteomesComparisonsArrayPath = "";
    /**
     * Path for coexpression table
     */
    private String coExprNetworkArrayPath = "";
    /**
     * Path for the table with all biological conditions for database creation
     */
    private String bioconditionsArrayPath = "";
    /**
     * Path for the table with all compariosns for database creation
     */
    private String comparisonsArrayPath = "";

    /*******************************************************************
     * Non static variables: genomes, biocondition
     ********************************************************************/
    /**
     * General Experiment containing all the BioCondition
     */
    private Experiment generalExperiment;
    /**
     * List of all biological conditions available
     */
    private ArrayList<String> allBioConditions = new ArrayList<String>();
    /**
     * List of all genomes available
     */
    private ArrayList<String> genomeList = new ArrayList<String>();
    /**
     * Load a Genome and keep it in memory
     */
    private TreeMap<String, Genome> genomes = new TreeMap<String, Genome>();
    /**
     * Load a GenomeNCBI and keep it in memory
     */
    private TreeMap<String, GenomeNCBI> genomesNCBI = new TreeMap<String, GenomeNCBI>();

    /**
     * ProbeInformation for Tiling and GeneExpression
     */
    private TreeMap<Integer, Integer> probesTiling = new TreeMap<Integer, Integer>();
    /**
     * ProbeInformation for GeneExpression
     */
    private TreeMap<String, Integer> probesGExpression = new TreeMap<String, Integer>();

    ///////////////////////////////////////////////////////////////////////////////////////
    // Listeria Specific //
    ///////////////////////////////////////////////////////////////////////////////////////
    /**
     * List of genes in lmo (to increase loading of viewers)
     */
    private ArrayList<String> geneListEGDe = new ArrayList<String>();
    /**
     * List of sRNAs in lmo (to increase loading of viewers)
     */
    private ArrayList<String> sRNAListEGDe = new ArrayList<String>();
    /**
     * List of asRNAs in lmo (to increase loading of viewers)
     */
    private ArrayList<String> asRNAListEGDe = new ArrayList<String>();
    /**
     * List of cisRegs in lmo (to increase loading of viewers)
     */
    private ArrayList<String> cisRegRNAListEGDe = new ArrayList<String>();
    /**
     * HashMap linking genome name to LofFC matrices<br>
     * Matrices containing all logFC values in a matrix GenomeElement vs BioCondition
     */
    private HashMap<String, ExpressionMatrix> logFCTable = new HashMap<>();
    /**
     * HashMap linking genome name to LofFC proteomic matrices<br>
     * Matrices containing all logFC values of proteomic in a matrix GenomeElement vs BioCondition
     */
    private HashMap<String, ExpressionMatrix> logFCProteomeTable = new HashMap<>();
    /**
     * HashMap linking genome name to Expr proteomes matrices<br>
     * Matrices containing all logFC values in a matrix GenomeElement vs BioCondition
     */
    private HashMap<String, ExpressionMatrix> exprProteomesTable = new HashMap<>();
    /**
     * Matrix containing all stat values in a matrix GenomeElement vs BioCondition
     */
    private ExpressionMatrix statTable;
    /**
     * Map linking Signature name to signatureID
     */
    private TreeMap<String, String> signaturesNametoID = new TreeMap<String, String>();

    private Database() {}

    public static Database getInstance() {
        // return Database.INSTANCE;
        return SingletonUtil.getSessionInstance(Database.class);
    }

    /**
     * Print out JVM properties And set this.path depending on the Operating System HAS TO BE RUN AT
     * STARTING OF EVERY PROJECT
     */
    public void readListDatabases() {
        /**
         * Check values of the JVM
         */
        // RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        // List<String> arguments = runtimeMxBean.getInputArguments();
        // for (String argument : arguments) {
        // System.out.println(argument);
        // }

        /**
         * Read list of Databases available to set path of the project
         */
        String os = System.getProperty("os.name");
        String path_list_database = "";
        System.out.println(os);
        if (os.equals("Mac OS X"))
            path_list_database = Database.PATH_MAC;
        else if (os.contains("Windows"))
            path_list_database = Database.PATH_WIN;
        else
            path_list_database = Database.PATH_WEBSITE;
        
        /*
         * Check if database .ini file can be found
         */
        File file = new File(path_list_database);
        if(file.exists()) {
            listDatabases = TabDelimitedTableReader.readHashMap(path_list_database);        	
        }else {
        	System.err.println("Cannot read list of databases in : " + path_list_database);
        }
        for (String project_name : listDatabases.keySet()) {
            System.out.println(project_name + " ; " + listDatabases.get(project_name));
        }

    }

    /**
     * MOST IMPORTANT INIT METHOD : Set all variables for the website
     */
    public boolean setVariables() {
        if (Database.getInstance() != null) {
        	String initFileName = getDATA_PATH() + "database.ini";
            System.out.println("Read database variables: " + initFileName);
            File file = new File(initFileName);
            if(file.exists()) {
            	databaseFeatures = TabDelimitedTableReader.readHashMap(initFileName); 
            	databaseFeatures = TabDelimitedTableReader.readHashMap(getDATA_PATH() + "database.ini");
                System.out.println("Read database parameters:");
                for (String project_name : databaseFeatures.keySet()) {
                    System.out.println(project_name + " ; " + databaseFeatures.get(project_name));
                }
                setWebpageTitle(databaseFeatures.get("WEB_PAGE_TITLE"));
                setGoogleId(databaseFeatures.get("GOOGLE_ID"));
                setInitView(databaseFeatures.get("INIT_VIEW"));
                setSpecies(databaseFeatures.get("SPECIES"));
                setLogo(databaseFeatures.get("LOGO"));

                setGenomeArrayPath(getDATA_PATH() + "Genomes.txt");
                setTranscriptomesArrayPath(getTRANSCRIPTOMES_PATH() + "Transcriptomes.txt");
                setTranscriptomesComparisonsArrayPath(getTRANSCRIPTOMES_PATH() + "ComparisonsTranscriptomes.txt");
                setProteomesArrayPath(getPROTEOMES_PATH() + "Proteomes.txt");
                setProteomesComparisonsArrayPath(getPROTEOMES_PATH() + "ComparisonsProteomes.txt");
                setCoExprNetworkArrayPath(getNETWORK_PATH() + "CoExpressionNetworks.txt");
                setBioConditionsArrayPath(getDATA_PATH() + "BioConditions.txt");
                setExperimentComparisonTablePath(getDATA_PATH() + "Comparisons.txt");
                System.out.println("Every file path has been setup");
                return true;
            }else {
            	System.err.println("Cannot read .ini file in : " + initFileName);
            	return false;
            }
        }else {
        	return true;
        }
    }

    /**
     * Run at the initialization of the workbench in Eclipse RAP website
     */
    public void initDatabase(Shell shell) {
        try {
            InitDatabaseThread thread = new InitDatabaseThread(this);
            new ProgressMonitorDialog(shell).run(true, false, thread);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Class for loading databases in a thread
     * 
     * @author cbecavin
     *
     */
    public static class InitDatabaseThread implements IRunnableWithProgress {
        private Database database;

        public InitDatabaseThread(Database database) {
            this.database = database;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            initDatabaseMonitor(database, monitor);
        }
    }

    /**
     * Initialize appropriate database with right files
     * 
     * @param database
     * @param monitor
     */
    public static void initDatabaseMonitor(Database database, IProgressMonitor monitor) {
        monitor.beginTask("Init " + database.getProjectName() + " database : ", 2);
        database.readListDatabases();
        database.setPath(database.listDatabases.get(database.getProjectName()));
        if(database.setVariables()) {
        	/*
        	 * If database.ini was read pursue with database loading
        	 */
	        database.setAllBioConditions(BioCondition.getAllBioConditionNames());
	        monitor.worked(1);
	        database.setGenomeList(Genome.getAvailableGenomes());
	        monitor.worked(1);
	        if (database.getProjectName().equals(LISTERIOMICS_PROJECT)
	                || database.getProjectName().equals(UIBCLISTERIOMICS_PROJECT)) {
	            database.setGeneListEGDe(TabDelimitedTableReader
	                    .readList(getANNOTATIONDATA_PATH() + database.getDatabaseFeatures().get("EGDE_GENE")));
	            monitor.worked(1);
	            database.setsRNAListEGDe(TabDelimitedTableReader
	                    .readList(getANNOTATIONDATA_PATH() + database.getDatabaseFeatures().get("EGDe_SRNA")));
	            monitor.worked(1);
	            database.setAsRNAListEGDe(TabDelimitedTableReader
	                    .readList(getANNOTATIONDATA_PATH() + database.getDatabaseFeatures().get("EGDe_ASRNA")));
	            monitor.worked(1);
	            database.setCisRegRNAListEGDe(TabDelimitedTableReader
	                    .readList(getANNOTATIONDATA_PATH() + database.getDatabaseFeatures().get("EGDE_CISREG")));
	            monitor.worked(1);
	            database.setSignaturesNametoID(Signature.loadSignaturesNametoID(
	                    getANNOTATIONDATA_PATH() + database.getDatabaseFeatures().get("SIGNATURES")));
	        }
	        System.out.println("Database have been read");
        }else {
        	System.out.println("Setup correctly database.ini file");
        }
        	
        
    }

    /**
     * Initialize appropriate database with right files
     * 
     * @param database
     * @param monitor
     */
    public static void initDatabase(String project_name) {
        System.out.println("initDatabase()");
        Database database = Database.getInstance();
        database.readListDatabases();
        database.setPath(database.listDatabases.get(database.getProjectName()));
        if(database.setVariables()) {
        	/*
        	 * If database.ini was read pursue with database loading
        	 */
	        database.setAllBioConditions(BioCondition.getAllBioConditionNames());
	        database.setGenomeList(Genome.getAvailableGenomes());
	        System.out.println("finished");
	        if (database.getProjectName().equals(Database.LISTERIOMICS_PROJECT)
	                || database.getProjectName().equals(Database.UIBCLISTERIOMICS_PROJECT)) {
	            database.setGeneListEGDe(
	                    TabDelimitedTableReader.readList(getDATA_PATH() + database.getDatabaseFeatures().get("EGDE_GENE")));
	            database.setsRNAListEGDe(
	                    TabDelimitedTableReader.readList(getDATA_PATH() + database.getDatabaseFeatures().get("EGDe_SRNA")));
	            database.setAsRNAListEGDe(TabDelimitedTableReader
	                    .readList(getDATA_PATH() + database.getDatabaseFeatures().get("EGDe_ASRNA")));
	            database.setCisRegRNAListEGDe(TabDelimitedTableReader
	                    .readList(getDATA_PATH() + database.getDatabaseFeatures().get("EGDE_CISREG")));
	            database.setSignaturesNametoID(Signature
	                    .loadSignaturesNametoID(getDATA_PATH() + database.getDatabaseFeatures().get("SIGNATURES")));
	        }
        }else {
        	System.out.println("Setup correctly database.ini file");
        }
    }

    /**
     * Load logFC transcriptome table for a specific genome
     * @param genomeName
     * @return
     */
    public ExpressionMatrix getLogFCTranscriptomesTable(String genomeName) {
        if (logFCTable.containsKey(genomeName)) {
            return logFCTable.get(genomeName);
        } else {
            if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT
                    && genomeName.equals(Genome.EGDE_NAME)) {
                System.out.println(getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genomeName + "_PRIVATE");
                ExpressionMatrix matrix =
                        ExpressionMatrix.load(getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genomeName + "_PRIVATE");
                logFCTable.put(genomeName, matrix);
                return matrix;
            } else {
                System.out.println(getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genomeName);
                ExpressionMatrix matrix =
                        ExpressionMatrix.load(getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genomeName);
                logFCTable.put(genomeName, matrix);
                return matrix;
            }
        }
    }
    
    /**
     * Load Logfc proteome tables for a specific genome
     * @param genomeName
     * @return
     */
    public ExpressionMatrix getLogFCProteomesTable(String genomeName) {
        if (logFCProteomeTable.containsKey(genomeName)) {
            return logFCProteomeTable.get(genomeName);
        } else {
            if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT
                    && genomeName.equals(Genome.EGDE_NAME)) {
                System.out.println(getLOGFC_MATRIX_PROTEOMES_PATH() + "_" + genomeName + "_PRIVATE");
                ExpressionMatrix matrix =
                        ExpressionMatrix.load(getLOGFC_MATRIX_PROTEOMES_PATH() + "_" + genomeName + "_PRIVATE");
                logFCProteomeTable.put(genomeName, matrix);
                return matrix;
            } else {
                System.out.println(getLOGFC_MATRIX_PROTEOMES_PATH() + "_" + genomeName);
                ExpressionMatrix matrix =
                        ExpressionMatrix.load(getLOGFC_MATRIX_PROTEOMES_PATH() + "_" + genomeName);
                logFCProteomeTable.put(genomeName, matrix);
                return matrix;
            }
        }
    }

    public ExpressionMatrix getExprProteomesTable(String genomeName) {
        if (exprProteomesTable.containsKey(genomeName)) {
            return exprProteomesTable.get(genomeName);
        } else {
            if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT
                    && genomeName.equals(Genome.EGDE_NAME)) {
                System.out.println(getEXPRESSION_MATRIX_PROTEOMES_PATH() + "_" + genomeName + "_PRIVATE");
                ExpressionMatrix matrix =
                        ExpressionMatrix.load(getEXPRESSION_MATRIX_PROTEOMES_PATH() + "_" + genomeName + "_PRIVATE");
                exprProteomesTable.put(genomeName, matrix);
                return matrix;
            } else {
                System.out.println(getEXPRESSION_MATRIX_PROTEOMES_PATH() + "_" + genomeName);
                ExpressionMatrix matrix =
                        ExpressionMatrix.load(getEXPRESSION_MATRIX_PROTEOMES_PATH() + "_" + genomeName);
                exprProteomesTable.put(genomeName, matrix);
                return matrix;
            }
        }
    }

    public ExpressionMatrix getStatTable() {
        if (statTable == null) {
            statTable = ExpressionMatrix.load(getSTAT_MATRIX_TRANSCRIPTOMES_PATH());
        }
        return statTable;
    }

    /**
     * Read probes for Tiling in Tiling.PROBES_PATH
     * 
     * @return
     */
    public TreeMap<Integer, Integer> getProbesTiling() {
        if (probesTiling.size() == 0) {
            try {
                @SuppressWarnings("resource")
                DataInputStream in =
                        new DataInputStream(new BufferedInputStream(new FileInputStream(Tiling.PROBES_PATH)));
                int i = 0;
                while (true) {
                    probesTiling.put(in.readInt(), i);
                    i++;
                }
            } catch (EOFException e) {
                System.err.println("Probe Tiling has been read");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return probesTiling;
    }

    public void setProbesTiling(TreeMap<Integer, Integer> probesTiling) {
        this.probesTiling = probesTiling;
    }

    /**
     * Read probes for GExpression in GeneExpression.PROBES_PATH
     * 
     * @return
     */
    public TreeMap<String, Integer> getProbesGExpression() {
        if (probesGExpression.size() == 0) {
            File file = new File(GeneExpression.PROBES_PATH);
            try {
                if (file.exists()) {
                    @SuppressWarnings("resource")
                    DataInputStream in = new DataInputStream(
                            new BufferedInputStream(new FileInputStream(GeneExpression.PROBES_PATH)));
                    int i = 0;
                    while (true) {
                        probesGExpression.put(in.readUTF(), i);
                        i++;
                    }
                }
            } catch (EOFException e) {
                System.err.println("Probe GExpression has been read");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return probesGExpression;
    }

    public void setProbesGExpression(TreeMap<String, Integer> probesGExpression) {
        this.probesGExpression = probesGExpression;
    }

    /**
     * Cleanup database when closing the website
     */
    public void cleanUpDatabase() {
        generalExperiment = new Experiment();
        allBioConditions = new ArrayList<String>();
        genomeList = new ArrayList<String>();
        genomesNCBI = new TreeMap<String, GenomeNCBI>();
        probesTiling = new TreeMap<Integer, Integer>();
        probesGExpression = new TreeMap<String, Integer>();
        geneListEGDe = new ArrayList<String>();
        sRNAListEGDe = new ArrayList<String>();
        asRNAListEGDe = new ArrayList<String>();
        cisRegRNAListEGDe = new ArrayList<String>();
        logFCTable = new HashMap<>();
        exprProteomesTable = new HashMap<>();
        statTable = new ExpressionMatrix();
        signaturesNametoID = new TreeMap<String, String>();
        for (Genome genome : genomes.values()) {
            genome.clearGenome();
        }
        genomes.clear();
    }

    /*****************************************************************
     * GETTER AND SETTERS
     * 
     */

    public static String getDATA_PATH() {
        return Database.getInstance().getPath() + File.separator + "Database" + File.separator;
    }

    public static String getBIOCONDITION_PATH() {
        return getDATA_PATH() + "BioConditions" + File.separator;
    }

    public static String getTRANSCRIPTOMES_PATH() {
        return getDATA_PATH() + "Transcriptomes" + File.separator;
    }

    public static String getPROTEOMES_PATH() {
        return getDATA_PATH() + "Proteomes" + File.separator;
    }

    public static String getMULTIOMICS_PATH() {
        return getDATA_PATH() + "Multiomics" + File.separator;
    }
    
    public static String getNETWORK_PATH() {
        return getDATA_PATH() + "Network" + File.separator;
    }

    public static String getGENOMES_PATH() {
        return getDATA_PATH() + "Genomes" + File.separator;
    }

    public static String getANNOTATIONDATA_PATH() {
        return getDATA_PATH() + "Annotation" + File.separator;
    }

    public static String getSIGNATURES_PATH() {
        return getANNOTATIONDATA_PATH() + "Signatures" + File.separator;
    }

    public static String getTEMP_PATH() {
        return Database.getInstance().getPath() + File.separator + "Temp" + File.separator;
    }

    public static String getANALYSIS_PATH() {
        return Database.getInstance().getPath() + File.separator + "Analysis" + File.separator;
    }

    /**
     * Path for loading Proteomes matrix data showing relative expression values
     */
    public static String getMULTIOMICS_MATRIX_PATH() {
        return Database.getMULTIOMICS_PATH() + "Table_OMICS";
    }
    
    /**
     * Path for loading Transcriptomes matrix data showing Log(Fold Change) values
     */
    public static String getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() {
        return Database.getNETWORK_PATH() + "Network_CoExpr";
    }

    /**
     * Path for loading Transcriptomes matrix data showing Log(Fold Change) values
     */
    public static String getLISTDATA_COEXPR_NETWORK_TRANSCRIPTOMES_PATH() {
        return Database.getNETWORK_PATH() + "Network_CoExpr_ListData";
    }
    
    /**
     * Path for loading Transcriptomes matrix data showing Log(Fold Change) values
     */
    public static String getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() {
        return Database.getTRANSCRIPTOMES_PATH() + "Table_LOGFC";
    }

    /**
     * Path for loading Transcriptomes matrix data showing statistical p-values
     */
    public static String getSTAT_MATRIX_TRANSCRIPTOMES_PATH() {
        return Database.getTRANSCRIPTOMES_PATH() + "Table_STAT";
    }

    
    /**
     * Path for loading Proteomes matrix data showing absolute expression values
     */
    public static String getEXPRESSION_MATRIX_PROTEOMES_PATH() {
        return Database.getPROTEOMES_PATH() + "Table_Expr";
    }

    /**
     * Path for loading Proteomes matrix data showing relative expression values
     */
    public static String getLOGFC_MATRIX_PROTEOMES_PATH() {
        return Database.getPROTEOMES_PATH() + "Table_LOGFC";
    }

    /**
     * Path for loading Proteomes matrix data showing Fold Change values
     */
    public static String getSTAT_MATRIX_PROTEOMES_PATH() {
        return Database.getPROTEOMES_PATH() + "Table_STAT";
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HashMap<String, String> getListDatabases() {
        return listDatabases;
    }

    public void setListDatabases(HashMap<String, String> listDatabases) {
        this.listDatabases = listDatabases;
    }

    public String getInitView() {
        return initView;
    }

    public void setInitView(String initView) {
        this.initView = initView;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getWebpageTitle() {
        return webpageTitle;
    }

    public void setWebpageTitle(String webpageTitle) {
        this.webpageTitle = webpageTitle;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getTranscriptomesArrayPath() {
        return transcriptomesArrayPath;
    }

    public void setTranscriptomesArrayPath(String transcriptomesArrayPath) {
        this.transcriptomesArrayPath = transcriptomesArrayPath;
    }

    public String getTranscriptomesComparisonsArrayPath() {
        return transcriptomesComparisonsArrayPath;
    }

    public void setTranscriptomesComparisonsArrayPath(String transcriptomesComparisonsArrayPath) {
        this.transcriptomesComparisonsArrayPath = transcriptomesComparisonsArrayPath;
    }

    public String getProteomesArrayPath() {
        return proteomesArrayPath;
    }

    public void setProteomesArrayPath(String proteomesArrayPath) {
        this.proteomesArrayPath = proteomesArrayPath;
    }

    public String getProteomesComparisonsArrayPath() {
		return proteomesComparisonsArrayPath;
	}

	public void setProteomesComparisonsArrayPath(String proteomesComparisonsArrayPath) {
		this.proteomesComparisonsArrayPath = proteomesComparisonsArrayPath;
	}

	public String getCoExprNetworkArrayPath() {
        return coExprNetworkArrayPath;
    }

    public void setCoExprNetworkArrayPath(String coExprNetworkArrayPath) {
        this.coExprNetworkArrayPath = coExprNetworkArrayPath;
    }

    public Experiment getGeneralExperiment() {
        return generalExperiment;
    }

    public void setGeneralExperiment(Experiment generalExperiment) {
        this.generalExperiment = generalExperiment;
    }

    public ArrayList<String> getAllBioConditions() {
        return allBioConditions;
    }

    public void setAllBioConditions(ArrayList<String> allBioConditions) {
        this.allBioConditions = allBioConditions;
    }

    public ArrayList<String> getGenomeList() {
        return genomeList;
    }

    public void setGenomeList(ArrayList<String> genomeList) {
        this.genomeList = genomeList;
    }

    public TreeMap<String, Genome> getGenomes() {
        return genomes;
    }

    public void setGenomes(TreeMap<String, Genome> genomes) {
        this.genomes = genomes;
    }

    public TreeMap<String, GenomeNCBI> getGenomesNCBI() {
        return genomesNCBI;
    }

    public void setGenomesNCBI(TreeMap<String, GenomeNCBI> genomesNCBI) {
        this.genomesNCBI = genomesNCBI;
    }

    public ArrayList<String> getGeneListEGDe() {
        return geneListEGDe;
    }

    public void setGeneListEGDe(ArrayList<String> geneListEGDe) {
        this.geneListEGDe = geneListEGDe;
    }

    public ArrayList<String> getsRNAListEGDe() {
        return sRNAListEGDe;
    }

    public void setsRNAListEGDe(ArrayList<String> sRNAListEGDe) {
        this.sRNAListEGDe = sRNAListEGDe;
    }

    public ArrayList<String> getAsRNAListEGDe() {
        return asRNAListEGDe;
    }

    public void setAsRNAListEGDe(ArrayList<String> asRNAListEGDe) {
        this.asRNAListEGDe = asRNAListEGDe;
    }

    public ArrayList<String> getCisRegRNAListEGDe() {
        return cisRegRNAListEGDe;
    }

    public void setCisRegRNAListEGDe(ArrayList<String> cisRegRNAListEGDe) {
        this.cisRegRNAListEGDe = cisRegRNAListEGDe;
    }

    public HashMap<String, ExpressionMatrix> getLogFCTable() {
        return logFCTable;
    }

    public void setLogFCTable(HashMap<String, ExpressionMatrix> logFCTable) {
        this.logFCTable = logFCTable;
    }

    public HashMap<String, ExpressionMatrix> getLogFCProteomeTable() {
        return logFCProteomeTable;
    }
    
    public void setLogFCProteomeTable(HashMap<String, ExpressionMatrix> logFCProteomeTable) {
        this.logFCProteomeTable = logFCProteomeTable;
    }
    
    public HashMap<String, ExpressionMatrix> getExprProteomesTable() {
        return exprProteomesTable;
    }

    public void setExprProteomesTable(HashMap<String, ExpressionMatrix> exprProteomesTable) {
        this.exprProteomesTable = exprProteomesTable;
    }

    public TreeMap<String, String> getSignaturesNametoID() {
        return signaturesNametoID;
    }

    public void setSignaturesNametoID(TreeMap<String, String> signaturesNametoID) {
        this.signaturesNametoID = signaturesNametoID;
    }

    public void setStatTable(ExpressionMatrix statTable) {
        this.statTable = statTable;
    }

    public HashMap<String, String> getDatabaseFeatures() {
        return databaseFeatures;
    }

    public void setDatabaseFeatures(HashMap<String, String> databaseFeatures) {
        this.databaseFeatures = databaseFeatures;
    }

    public String getGenomeArrayPath() {
        return genomeArrayPath;
    }

    public void setGenomeArrayPath(String genomeArrayPath) {
        this.genomeArrayPath = genomeArrayPath;
    }

    public String getBioConditionsArrayPath() {
        return bioconditionsArrayPath;
    }

    public void setBioConditionsArrayPath(String bioconditionsArrayPath) {
        this.bioconditionsArrayPath = bioconditionsArrayPath;
    }

    public String getExperimentComparisonTablePath() {
        return comparisonsArrayPath;
    }

    public void setExperimentComparisonTablePath(String experimentComparisonTablePath) {
        this.comparisonsArrayPath = experimentComparisonTablePath;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

}
