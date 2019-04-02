package bacnet.scripts.phylogeny;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.forester.io.parsers.PhylogenyParser;
import org.forester.io.parsers.util.ParserUtils;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyMethods;
import org.forester.phylogeny.PhylogenyNode;
import org.forester.phylogeny.data.Identifier;
import org.forester.phylogeny.data.Taxonomy;
import org.forester.phylogeny.iterators.PhylogenyNodeIterator;
import bacnet.Database;
import bacnet.reader.TabDelimitedTableReader;

/**
 * This list of function is useful to manipulate Newick files given by Jolley in May 2012, from its publication Jolley et al. Microb. 2012<br>
 * It contains 1565 nodes, each bacteria=node has an ID. The link between these ID and an NCBI ID (i.e. NC32010.1) is given by the table at
 * GenomePhylogeny.PATH_INFOTABLE
 * 
 * @author Christophe Bécavin
 *
 */
public class PhylogenyToolsJolley {

	public static String ALLBACTERIA_NWK_1 = Database.getANALYSIS_PATH()+"/Egd-e Annotation/1565_52loci_nucleotides_labels.nwk";
	public static String ALLBACTERIA_NWK_2 = Database.getANALYSIS_PATH()+"/Egd-e Annotation/1565_52loci_protein_labels.nwk";
	public static String ALLFIRMICUTES_AVAILABLE = Database.getANALYSIS_PATH()+"/Egd-e Annotation/All-Firmicutes-Availables.txt";
	public static String Bacili_NWK = Database.getANALYSIS_PATH()+"/Egd-e Annotation/Bacili.nwk";
	public static String ALLFIRMICUTES_NWK = Database.getANALYSIS_PATH()+"/Egd-e Annotation/AllFirmicutes.nwk";
	public static String Clostridiales_NWK = Database.getANALYSIS_PATH()+"/Egd-e Annotation/Clostridiales.nwk";
	public static String Lactobacillales_NWK = Database.getANALYSIS_PATH()+"/Egd-e Annotation/Lactobacillales.nwk";
	

	public static Phylogeny addInfoToPhylogeny(Phylogeny phy, HashMap<String, String[]> infoNodes){
		PhylogenyNodeIterator it = phy.iteratorExternalForward();
		int count = 0;
		while(it.hasNext()) {
			PhylogenyNode node = it.next();
			String name = node.getName();
			if(infoNodes.containsKey(name.trim())){
				String[] rowResult = infoNodes.get(name.trim());
				Taxonomy taxo = new Taxonomy();
				
				node.setName(rowResult[0]);

				taxo.setScientificName(rowResult[1]);
				taxo.setCommonName(rowResult[1]);
				node.getNodeData().addTaxonomy(taxo);
				count++;
			}else{
				node.setName("");
				//phy.deleteSubtree(node, true);
			}
		}
		System.out.println(phy.getNumberOfExternalNodes()+"    "+count);
		return phy;
	}

	/**
	 * If a node of the Phylogeny phy, is include in listIDs, delete the node <br>
	 * @param phy
	 * @param infoNodes
	 */
	public static void extractPhylogeny(Phylogeny phy,TreeSet<Integer> listIDs){
		PhylogenyNodeIterator it = phy.iteratorExternalForward();
		int count = 0;
		while(it.hasNext()) {
			PhylogenyNode node = it.next();
			String name = node.getName();
			int id = Integer.parseInt(name.substring(0, name.indexOf("|")));
			boolean found = false;
			for(Integer listID : listIDs){
				if(listID==id){
					found = true;
				}
			}
			if(!found) phy.deleteSubtree(node, true);
		}

		phy.recalculateNumberOfExternalDescendants(true);
		System.out.println(phy.getNumberOfExternalNodes()+"    "+count);
	}

	/**
	 * If a node of the Phylogeny phy, is include in listIDs, hide it by setting the name to void <br>
	 * @param phy
	 * @param infoNodes
	 */
	public static void hidePhylogeny(Phylogeny phy,TreeSet<Integer> listIDs){
		PhylogenyNodeIterator it = phy.iteratorExternalForward();
		int count = 0;
		while(it.hasNext()) {
			PhylogenyNode node = it.next();
			String name = node.getName();
			node.setName("");
			int id = Integer.parseInt(name.substring(0, name.indexOf("|")));
			for(Integer listID : listIDs){
				if(listID==id){
					node.setName(name);
				}
			}
		}
		System.out.println(phy.getNumberOfExternalNodes()+"    "+count);
	}

	/**
	 * Get a Phylogeny where nodeName contain ID from Jolley and add all taxonomy information contained in it
	 * @return
	 */
	public static Phylogeny addInfoTOGenomePhylogeny(Phylogeny phy){
	    TreeMap<Integer, String[]> jolleyIDtoInfo = GenomePhylogeny.getJolleyIDtoInfo();
        PhylogenyNodeIterator it = phy.iteratorExternalForward();
		while(it.hasNext()) {
			PhylogenyNode node = it.next();
			String name = node.getName();
			//System.out.println(name+" "+name.substring(0, name.indexOf("|"))+" "+name.substring(name.indexOf("|")+1, name.length()-1));
			if(!name.equals("")){
				int id = Integer.parseInt(name.substring(0, name.indexOf("|")));
				if(jolleyIDtoInfo.containsKey(id)){
					if(include(jolleyIDtoInfo.get(id))){
						node.setName(jolleyIDtoInfo.get(id)[6]+" "+jolleyIDtoInfo.get(id)[1]);  // species + isolate + NCBI id
						Taxonomy t1 = new Taxonomy();
						t1.setScientificName(jolleyIDtoInfo.get(id)[11]); //species..class
						t1.setAuthority(jolleyIDtoInfo.get(id)[13]+" - "+jolleyIDtoInfo.get(id)[14]); // sequencing center
						t1.setIdentifier(new Identifier(jolleyIDtoInfo.get(id)[2]));
						t1.setCommonName(jolleyIDtoInfo.get(id)[8]);  // add aliases NCBI

						node.getNodeData().addTaxonomy( t1 );
					}else{
						phy.deleteSubtree(node, true);
						// System.out.println(node.getName());
					}
					//System.out.println(node.getName());
				}else{
					phy.deleteSubtree(node, true);
					///System.out.println(node.getName());
				}
			}
		}
		phy.recalculateNumberOfExternalDescendants(true);
		System.out.println(phy.getNumberOfExternalNodes()+" nodes");
		return phy;
	}

	/**
	 * Define the condition for including a phylogenic node
	 * @param infos
	 * @return true if it is include in the phylogenic tree
	 */
	private static boolean include(String[] infos){
		//String info = infos[8];
		//return true;
		if(!infos[2].equals("")) return true;
		//if(infos[8].equals("Bacilli")) return true;  // include baccili
		//if(infos[13].equals("finished")) return true; // complete sequence
		return false;
	}

	/**
	 * Read Newick file containing all bacteria phylogeny <br>
	 * @return Phylogeny object created from Newick file
	 */
	public static Phylogeny readPhylogeny(String fileName){
		try {
			File treeFile = new File(fileName);
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
	 * Read Newick file containing all bacteria phylogeny <br>
	 * @return Phylogeny object created from Newick file
	 */
	public static Phylogeny readAllBacteriaPhylogeny(){
		try {
			//File treeFile = new File(ALLBACTERIA_NWK_2);
			File treeFile = new File(ALLFIRMICUTES_NWK);
			PhylogenyParser parser = ParserUtils.createParserDependingOnFileType(treeFile, true );
			Phylogeny[] phys = PhylogenyMethods.readPhylogenies( parser, treeFile );
			//phys[0] = addInfoTOGenomePhylogeny(phys[0]);
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
	 * Read Newick file containing all bacteria phylogeny <br>
	 * Remove all non firmicutes species<br>
	 * Remove bacteria for which no complete genome is available : no NP_ or CP id
	 * @return Phylogeny object created from Newick file
	 */
	public static Phylogeny readFirmicutesPhylogeny(){
		try {
			File treeFile = new File(ALLBACTERIA_NWK_2);
			PhylogenyParser parser = ParserUtils.createParserDependingOnFileType(treeFile, true );
			Phylogeny[] phys = PhylogenyMethods.readPhylogenies( parser, treeFile );
			phys[0] = addInfoTOGenomePhylogeny(phys[0]);
			TreeSet<String> nodesNamesFirmicutes = new TreeSet<>();
			for(PhylogenyNode node :phys[0].getExternalNodes()){
				String taxonomy = node.getNodeData().getTaxonomies().get(0).getCommonName();
				String ncbiId = node.getNodeData().getTaxonomies().get(0).getIdentifier().toString();
				if(taxonomy.equals("Bacilli") || taxonomy.equals("Clostridia") || taxonomy.equals("Erysipelotrichi")){
					//System.out.println(node.getName());
					if(ncbiId.contains("NC_") || ncbiId.contains("CP0")){
						//System.out.println(node.getName());
						nodesNamesFirmicutes.add(node.getName() + " - "+ncbiId);
					}else{
						phys[0].deleteSubtree(node, true);
					}
				}else{
					phys[0].deleteSubtree(node, true);
				}
			}
			TabDelimitedTableReader.saveTreeSet(nodesNamesFirmicutes, ALLFIRMICUTES_AVAILABLE);
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

}
