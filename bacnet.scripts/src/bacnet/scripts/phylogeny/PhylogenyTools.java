package bacnet.scripts.phylogeny;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import org.forester.io.parsers.PhylogenyParser;
import org.forester.io.parsers.util.ParserUtils;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyMethods;
import org.forester.phylogeny.PhylogenyNode;
import org.forester.phylogeny.iterators.PhylogenyNodeIterator;
import bacnet.Database;
import bacnet.datamodel.annotation.RfamElement;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;

/**
 * Different methods to manipulate and display Newick and other phylogeny files
 *  
 *
 * @author Christophe Bécavin
 *
 */
public class PhylogenyTools {

	public static String ALLBACTERIA_XML = Database.getANALYSIS_PATH()+"/Egd-e Annotation/PhyloXML all bacteria.xml";
	public static String ALLBACTERIA_XML_1 = Database.getANALYSIS_PATH()+"/Egd-e Annotation/1565_52loci_nucleotides.xml";
	public static String ALLBACTERIA_XML_2 = Database.getANALYSIS_PATH()+"/Egd-e Annotation/1565_52loci_protein.xml";
	
	public static Phylogeny addInfoToPhylogeny(Phylogeny phy, HashMap<String, String[]> infoNodes){
		PhylogenyNodeIterator it = phy.iteratorExternalForward();
	     int count = 0;
	     while(it.hasNext()) {
	     	PhylogenyNode node = it.next();
	          String name = node.getName();
	                 if(infoNodes.containsKey(name.trim())){
	                 	  String[] rowResult = infoNodes.get(name);
	                 	  node.setName(rowResult[0]);
	               	  count++;
	               	  //node.getBranchData().setBranchColor( new BranchColor( Color.GREEN) );
	                      // To make colored subtrees thicker:
	                      //node.getBranchData().setBranchWidth( new BranchWidth( 4 ) );
	                 }
	          }
	     	System.out.println(phy.getNumberOfExternalNodes()+"    "+count);
	     return phy;
	}
	
	/**
	 * If a node of the Phylogeny phy, is not a key of infoNodes Map, delete the tree <br>
	 * return the subTree obtained 
	 * @param phy
	 * @param infoNodes
	 * @return
	 */
	public static Phylogeny extractPhylogeny(Phylogeny phy,HashMap<String, String[]> infoNodes){
		PhylogenyNodeIterator it = phy.iteratorExternalForward();
	     int count = 0;
	     while(it.hasNext()) {
	     	PhylogenyNode node = it.next();
	          String name = node.getName();
	                 if(!infoNodes.containsKey(name.trim())){
	               	  phy.deleteSubtree(node, true);
	                 }
	          }
	     phy.recalculateNumberOfExternalDescendants(true);
	     System.out.println(phy.getNumberOfExternalNodes()+"    "+count);
	     return phy;
		
	}
	
	/**
	 * From PhyloXMl of all bacteria, get only the genome which have a NCBI genome id
	 * @return
	 */
	public static Phylogeny getNCBIGenomePhylogeny(){
		//GenomeNCBIids ids = new GenomeNCBIids();
		Phylogeny phy = readAllBacteriaPhylogeny();
//		PhylogenyNodeIterator it = phy.iteratorExternalForward();
//	     while(it.hasNext()) {
//	     	PhylogenyNode node = it.next();
//	          String name = node.getName();
//	          if(name.equals("646564513")){
//	             	  phy.deleteSubtree(node, true);
//	          }else if(!ids.imgTaxonTOncbi.containsKey(name.trim())){
//	              	  phy.deleteSubtree(node, true);
//	              	  //System.out.println(node.getName());
//	          }
//	     }
//	     phy.recalculateNumberOfExternalDescendants(true);
//	     System.out.println(phy.getNumberOfExternalNodes()+" nodes");
	     return phy;
		
	}
	
	/**
	 * Read PhyloXML file containing all bacteria phylogeny <br>
	 * read from ALLBACTERIA_XML
	 * @return Phylogeny object created from PhyloXML
	 */
	public static Phylogeny readAllBacteriaPhylogeny(){
		try {
			File treeFile = new File(ALLBACTERIA_XML);
			PhylogenyParser parser = ParserUtils.createParserDependingOnFileType(treeFile, true );
			Phylogeny[] phys = PhylogenyMethods.readPhylogenies( parser, treeFile );
			System.out.println(phys[0].getNumberOfExternalNodes()+" nodes");
			return phys[0];
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Read PhyloXML file containing all bacteria phylogeny <br>
	 * Read from ALLBACTERIA_XML_2
	 * @return Phylogeny object created from PhyloXML
	 */
	public static Phylogeny readAllBacteriarLMSTPhylogeny(){
		try {
			File treeFile = new File(ALLBACTERIA_XML_2);
			PhylogenyParser parser = ParserUtils.createParserDependingOnFileType(treeFile, true );
			Phylogeny[] phys = PhylogenyMethods.readPhylogenies( parser, treeFile );
			System.out.println(phys[0].getNumberOfExternalNodes()+" nodes");
			return phys[0];
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void createInfoNodes(){
		String[][] array = new String[0][0];
		// this array give link between NCBI genome ID(for genome info, Blast results), Rfam genome ID (for ncRNA), and IMG_taxon ID (for phylogeny)
		array = TabDelimitedTableReader.read(RfamElement.RFAM_GENOME);
		String[][] result = new String[0][0];
     	String[][] resultTemp = new String[0][0];
		// give results on antisens regu element in RFam genomes
		result = TabDelimitedTableReader.read(Database.getANALYSIS_PATH()+"Jeff/Antisense_summary-curated.txt");
			// temp array for results
	     	resultTemp = new String[result.length][result[0].length+1];
	     	for(int i=0;i<result.length;i++){
	     		for(int k=0;k<result[0].length;k++){
	     			resultTemp[i][k] = result[i][k];
	     		}
	     		resultTemp[i][result[0].length] = "";
	     	}
		
		
     	
     	//System.out.println(RfamElement.RFAM_GENOME);
     	// create nodes from the result table
     	HashMap<String, String[]> infoNodes = new HashMap<String, String[]>();
     	for(int i=0;i<result.length;i++){
     		String genomeName = result[i][1];
     		String[] rowResult = ArrayUtils.getRow(result, i);
     		//search for each row of results, the corresponding Rfam Id in RfamElement.RFAM_GENOME array
     		for(int k=0;k<array.length;k++){
     			if(genomeName.equals("CP000459.1")){
     			}
     			//System.out.println(genomeName+"  "+array[k][4]+"  "+k);
     			if(genomeName.equals(array[k][4])){
     				String yop = array[k][8];
     				resultTemp[i][result[0].length] = yop;
     				if(infoNodes.containsKey(array[k][8])){
     					String[] row = infoNodes.get(array[k][8]);
     					try{
     						if(Integer.parseInt(row[4])<Integer.parseInt(rowResult[4])){
     							infoNodes.put(array[k][8].trim(),rowResult);
     						}
     					}catch(Exception e){
     						System.out.println(row[0]+" " +row[5] +"  "+row[4]+" "+ rowResult[4]+"  "+"  "+rowResult[5]+" "+rowResult[0]);
     					}
     					
     				}else{
     					infoNodes.put(array[k][8].trim(),rowResult);
     				}
     			}
     		}
     	}
     	
	}
}
