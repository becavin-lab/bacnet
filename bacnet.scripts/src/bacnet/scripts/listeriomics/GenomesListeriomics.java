package bacnet.scripts.listeriomics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import bacnet.Database;
import bacnet.datamodel.annotation.LocusTag;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequenceNCBI.GenomeConversion;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.FastaFileReader;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.database.GenomesCreation;
import bacnet.scripts.genome.RASTEGDeGenome;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

public class GenomesListeriomics {

    public static void run() {

        //GenomesCreation.downloadAll();

        //GenomesCreation.convertAll();


        // String genomeName = "Listeria monocytogenes FSL J1-208";
        // GenomeNCBI genomeNCBI = GenomeNCBITools.loadGenome(genomeName, PATH_NCBIGenome, false, true);
        // GenomeConversion.run(genomeNCBI, PATH_NCBIGenome+genomeName, genomeName);

        // /*
        // * Create a list of EGDe element to increase loading
        // */
        //GenomesCreation.createEGDeGenomeElementList();
        // /*
        // * Add RAST genes to EGD-e genome
        // */
        //RASTEGDeGenome.run();

        /*
         * Due to change in the locus tag naming of NCBI we need to add old locus tag in the information
         */
//        LocusTag.parseOldLocusTagChange();
//        LocusTag.parseNewLocusTagChange();
//        LocusTag.addLocusChange();

        /*
         * Verify the conversion and create PTT files for SynTView
         */
        //GenomesCreation.verifyAll();
        //createPTTandFAAFiles();
        // showAllLocusID();

    }


    /**
     * Run all the methods concerning genomes in Listeriomics<br>
     * Run after loading of Workbench!
     */
    public static void runPostInit() {
        // /*
        // * Run alignment of each Srna
        // */
        // SrnaAlignment.multiSpeciesAlign();
        // SrnaAlignment.summarizeResultsInTable();


    }
    
}
