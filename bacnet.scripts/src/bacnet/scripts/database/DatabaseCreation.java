package bacnet.scripts.database;

import java.io.IOException;
import bacnet.scripts.listeriomics.MainListeriomics;

/**
 * All methods for Database Creation of your multi-omics website
 * 
 * @author Christophe Bécavin
 *
 */
public class DatabaseCreation {

    /**
     * Method for all pre-processing of the database UNCOMMENT in bacnet.e4.rap.setup.SetupPart
     * 
     * @throws IOException
     */
    public static void preProcessing() throws IOException {

        /*
         * Scripts for data pre-processing
         */

        // MainLeishomics.run();

        /*
         * Create CRISPRGo database
         */
        // MainCRISPRGo.run();

        /*
         * Create Leishomics database
         */
        // MainLeishomics.run();

        /*
         * Create Yersiniomics database
         */
        // MainYersiniomics.run();
        System.out.println("Database Pre-processing done");
    }

    /**
     * This method in an example of all methods which should be run to create your multi-omics website
     * <br>
     * It is never run !<br>
     * <br>
     * <li>All supplementary annotation files created
     * <li>Genomes creation
     * <li>Phylogenomics figures and otrholog search
     * <li>BioCondition creation
     * <li>Comparisons creation
     * <li>Downloard and create ArrayExpress files
     * <li>Process all Transcriptome files
     * <li>Process all Prteomics files
     * <li>Create Expression and Protein Atlas
     * <li>Create co-expression network
     * <li>Create summary files once all database elements have been created
     * 
     */
    public static void runCreation() {
        /*
         * Create Listeriomics database
         */
        MainListeriomics.run();

        System.out.println("Database Pre-processing done");
    }

}
