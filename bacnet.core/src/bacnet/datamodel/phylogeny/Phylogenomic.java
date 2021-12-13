package bacnet.datamodel.phylogeny;

import java.io.File;
import java.util.HashMap;
import bacnet.Database;

public class Phylogenomic {

    static public String PHYLO_GENOME_SVG = Database.getANNOTATIONDATA_PATH() + "PhylogenyGenomes.svg";
    static public String HOMOLOG_SUMMARY = Database.getInstance().getPath() + "/HomologsStats.txt";
    
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
     * Parse phylogenomic SVG figure and extract attributes corresponding to each genome
     * @param svgTxt
     * @return HashMap<String, String> genomeToAttribute
     */
    public static HashMap<String, String> parsePhylogenomicFigure(String svgTxt) {
    	HashMap<String, String> genomeToAttribute = new HashMap<String, String>();
    	svgTxt.replaceAll("\n","");
    	String[] phyloTree = svgTxt.replaceAll("\n","").split("<text");
		
    	/*
    	 * Go through all text line
    	 */
    	for(String line : phyloTree) {
    		if(line.contains("/text") && line.contains(Database.getInstance().getSpecies())) {
    			line = line.split("/text")[0].trim();
    			String attribute = line.substring(0,line.indexOf(">")).trim();
				String genomeTemp = line.substring(line.indexOf(">")+1, line.indexOf("<")).trim();
				genomeToAttribute.put(genomeTemp, attribute);
    		}
		}
    	return genomeToAttribute;
    }
    
    
    
    /**
     * Init static variables after Database change
     */
    public static void initStaticVariables() {
    	PHYLO_GENOME_SVG = Database.getANNOTATIONDATA_PATH() + "PhylogenyGenomes.svg";
    }
    
}
