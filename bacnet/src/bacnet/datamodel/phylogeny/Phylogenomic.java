package bacnet.datamodel.phylogeny;

import java.io.File;

import bacnet.Database;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;

public class Phylogenomic {

    static public String PHYLO_GENOME_SVG = Database.getANNOTATIONDATA_PATH() + "PhylogenyGenomes.svg";
    static public String HOMOLOG_SUMMARY = GenomeNCBI.PATH_HOMOLOGS + "HomologsStats.txt";
    
    /**
     * Test if the Phylogenomic figure exist and rturn its path
     * @return
     */
    public static String getPhylogenomicFigurePath() {
    	File file = new File(PHYLO_GENOME_SVG);
    	if(file.exists()) {
    		return PHYLO_GENOME_SVG;
    	}else {
    		return null;
    	}
    	
    	
    }
    
    /**
     * Init static variables after Database change
     */
    public static void initStaticVariables() {
    	PHYLO_GENOME_SVG = Database.getANNOTATIONDATA_PATH() + "PhylogenyGenomes.svg";
    }
    
}
