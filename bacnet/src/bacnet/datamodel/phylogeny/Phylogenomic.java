package bacnet.datamodel.phylogeny;

import java.io.File;

import bacnet.Database;

public class Phylogenomic {

    static public String PHYLO_GENOME_SVG = Database.getANNOTATIONDATA_PATH() + "PhylogenyGenomes.svg";
    
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
     * Initi static variables after Database change
     */
    public static void initStaticVariables() {
    	PHYLO_GENOME_SVG = Database.getANNOTATIONDATA_PATH() + "PhylogenyGenomes.svg";
    }
    
}
