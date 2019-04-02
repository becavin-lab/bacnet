package bacnet.scripts.listeriomics.srna;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequence.Srna.TypeSrna;
import bacnet.datamodel.sequenceNCBI.GenomeConversion;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.Blast;
import bacnet.scripts.blast.BlastOutput;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.scripts.blast.BlastResult;
import bacnet.utils.ArrayUtils;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class SrnaAlignmentBlastN {
	
	public static int SRNA_NUMBER = 10;
	//public static int SRNA_NUMBER = 295;
	
	public static void run(){
		// sRNA conservation tools
			multiSpeciesAlign();
//				SrnaAlignment.multiSpeciesAlign10403S();
//				SrnaAlignment.summarizeResultsInTable();
//				SrnaAlignment.summarizeResultsInTable10403S();
							
				/*
				 * We have to run in runPostInit(){ 
				 *         SrnaAlignment.createConservationTables();
				 *          }
				 * 	to create table of conservation and save them in html format!
				 * 
				 */
	}
	
	
	/**
	 * Run a Blast for each sRNA on a list of Genomes given in tempDatabase
	 */
	public static void multiSpeciesAlign(){
		Genome genome = Genome.loadEgdeGenome();
		
		ArrayList<Srna> sRNAs = new ArrayList<>();
//		for(Srna sRNA : genome.getChromosomes().get(0).getsRNAs().values()) sRNAs.add(sRNA);
		for(Srna sRNA : genome.getChromosomes().get(0).getAsRNAs().values()) sRNAs.add(sRNA);
		for(Srna sRNA : genome.getChromosomes().get(0).getCisRegs().values()) sRNAs.add(sRNA);
		
		for(Srna sRNA : SrnaTables.getOliverInfo()){
			if(sRNA.getName().equals("rli64")){
				sRNAs.add(sRNA);
			}
		}
		
		
		multiSpeciesAlign(sRNAs);
	}
	
	/**
	 * Run a Blast for each sRNA (found in Oliver et al. in 10403S genome) on a list of Genomes given in tempDatabase
	 */
	public static void multiSpeciesAlign10403S(){
		//ArrayList<Srna> sRNAs = Srna.getSrnas10403S();
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		for(Srna sRNA : SrnaTables.getOliverInfo()){
			if(sRNA.getName().equals("rli64")){
				sRNAs.add(sRNA);
			}
		}
		multiSpeciesAlign(sRNAs);
	}
	
	/**
	 * Run a Blast for each sRNA on a list of Genomes given in tempDatabase
	 * @param sRNAs
	 */
	public static void multiSpeciesAlign(ArrayList<Srna> sRNAs){
		//for(int i=0;i<SRNA_NUMBER;i++){
		for(int i=0;i<sRNAs.size();i++){
			Srna sRNA = sRNAs.get(i);
			if(sRNA.getEnd()==-1000000){
				if(sRNA.isStrand()){
					sRNA.setEnd(sRNA.getBegin()+150);
				}else{
					sRNA.setEnd(sRNA.getBegin());
					sRNA.setBegin(sRNA.getEnd()-150);
				}
			}
			String sequence = sRNA.getSequence();
			
			// save query sequence
			String blastQuery = ">"+sRNA.getName()+"\n"+sequence;
			System.out.println(blastQuery);
			String fileNameQuery = Database.getTEMP_PATH()+"tempSeq_"+sRNA.getName().replaceAll("/", "")+".txt";
			FileUtils.saveText(blastQuery,fileNameQuery);
			
			// run Blast
			BlastOutputTYPE outType = BlastOutputTYPE.ASN;
			String blastResult = Database.getTEMP_PATH()+"resultBlast_"+sRNA.getName().replaceAll("/", "")+BlastOutput.fileExtension(outType);
			String tempDatabase = Database.getTEMP_PATH()+"tempBlastDatabase";
			final String[] args = {Blast.blastN,"-query","\""+fileNameQuery+"\"","-db","\""+tempDatabase+"\"","-out","\""+blastResult+"\"","-outfmt",outType.ordinal()+"","-evalue 0.001","-word_size 4"};
			try {
				// run Blast
				CMD.runProcess(args, true);
				// convert asn in different format
				String blastResultHTML = Database.getTEMP_PATH()+File.separator+FileUtils.removeExtensionAndPath(blastResult);
				// convert in HTML to put in the webpage
				BlastOutput.convertOuput(blastResult, blastResultHTML, true, BlastOutputTYPE.PAIRWISE);
				// convert in XML to analyse results
				BlastOutput.convertOuput(blastResult, FileUtils.removeExtension(blastResult), false, BlastOutputTYPE.XML);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("All process RNA");
	}
	
	
	
	
	/**
	 * 	From Blast Result extract Genome containing the sRNA<br>
	 * 
	 * A filter is applied to keep results with identities above 50%
	 * @param sRNAs
	 * @throws IOException
	 */
	public static void extractInfo(ArrayList<Srna> sRNAs){
		// read each sRNA XML file and right the table accordingly
		//for(int i=0;i<SRNA_NUMBER;i++){
		for(int i=0;i<sRNAs.size();i++){
			Srna sRNA = sRNAs.get(i);
			// we need to delete one line in the file otherwise it will not be read
			String blastResult = Database.getTEMP_PATH()+"resultBlast_"+sRNA.getName().replaceAll("/", "")+".xml";
			String text = FileUtils.readText(blastResult);
			String[] lines = text.split("\n");
			// delete line 2
			if(lines[1].contains("DOCTYPE BlastOutput")){
				lines[1]=lines[0];
				text="";
				for(int j=1;j<lines.length;j++){
					text+=lines[j]+"\n";
				}
				FileUtils.saveText(text, blastResult);
			}
			// get Blast results
			ArrayList<BlastResult> results = BlastResult.getResultsFromXML(blastResult);
			
			// filter results
			//results = BlastResult.filterIdentities(results, 50);
			
			// delete duplicates
			TreeMap<String,String[]> resultsTree = new TreeMap<String, String[]>();
			for(BlastResult blt : results){
				System.out.println(blt.hit_accession+" "+blt.eValue+" "+blt.bitScore+"  "+blt.identities);
				if(resultsTree.containsKey(blt.hit_accession)){
					// get result with lowest score
					double evaluePrevious = Double.parseDouble(resultsTree.get(blt.hit_accession)[0]);
					double evalue = blt.eValue;
					if(evaluePrevious > evalue){
						String[] result = {blt.eValue+"",blt.identities+""};
						resultsTree.put(blt.hit_accession, result);
					}
					// get result with highest identities
//					double identities = 
//					double currentIdentities = blt.identities;
//					if(currentIdentities > identities){
//						resultsTree.put(blt.hit_accession, currentIdentities);
//					}
				}else{
					String[] result = {blt.eValue+"",blt.identities+""};
					resultsTree.put(blt.hit_accession, result);
				}
			}
			
			// write results
			String ret = "";
			for(String def : resultsTree.keySet()){
				ret+=def+"\t"+resultsTree.get(def)[0]+"\t"+resultsTree.get(def)[1]+"\n";
			}
			
			FileUtils.saveText(ret, Database.getTEMP_PATH()+sRNA.getName().replaceAll("/", "")+".txt");
		}
	}
	
	
	/**
	 * Read all the XML results of Blast and create two PhyloXML files and a sRNA conservation table<br>
	 * @throws IOException
	 */
	public static void summarizeResultsInTable(){
		ArrayList<Srna> sRNAs = Srna.getEGDeALLSrnas();
		for(Srna sRNA : SrnaTables.getOliverInfo()){
			if(sRNA.getName().equals("rli64")){
				sRNAs.add(sRNA);
			}
		}
		
		TabDelimitedTableReader.save(summarizeResultsInTable(sRNAs), Srna.PATH_CONSERVATION);
	}
	
	/**
	 * Read all the XML results of Blast and create two PhyloXML files and a sRNA conservation table<br>
	 * @throws IOException
	 */
	public static void summarizeResultsInTable10403S() throws IOException{
//		ArrayList<Srna> sRNAs = Srna.getSrnas10403S();
//		TabDelimitedTableReader.save(summarizeResultsInTable(sRNAs), Database.getTEMP_PATH()+"sRNAConservation10403S.txt");
	}
	
	/**
	 * Read all the XML results of Blast and create two PhyloXML files and a sRNA conservation table<br>
	 * 
	 * @param sRNAs
	 * @return
	 * @throws IOException
	 */
	public static String[][] summarizeResultsInTable(ArrayList<Srna> sRNAs){
		
		extractInfo(sRNAs);
		
		// create a map linking accession ID (found in the Blast result) and the name of the genome (found in my Genome folder)
		String[][] arrayGenome = TabDelimitedTableReader.read(Database.getTEMP_PATH()+"GenomesArray.txt");
		TreeMap<String,String> accessionToName = new TreeMap<String, String>();
		for(int i=0;i<arrayGenome.length;i++) accessionToName.put(arrayGenome[i][2], arrayGenome[i][0]);
		TreeMap<String,Integer> accessionToID = new TreeMap<String, Integer>();
		for(int i=0;i<arrayGenome.length;i++) accessionToID.put(arrayGenome[i][2], Integer.parseInt(arrayGenome[i][1]));
		TreeSet<Integer> listIDs = new TreeSet<Integer>();
		
		String[][] table = new String[sRNAs.size()+1][accessionToName.size()+1];
		table[0][0] = "sRNA\\Genomes";
		int u=1;
		for(String accession : accessionToName.keySet()){
			table[0][u] = accessionToName.get(accession);
			u++;
		}
		//for(int i=0;i<SRNA_NUMBER;i++){
		for(int i=0;i<sRNAs.size();i++){
			Srna sRNA = sRNAs.get(i);
			table[i+1][0] = sRNA.getName();
			listIDs.clear();
			// fill the array with 0
			for(int j=1;j<table[0].length;j++)	table[i+1][j] = "0";
			// read blast result
			String[][] bltResults = TabDelimitedTableReader.read(Database.getTEMP_PATH()+sRNA.getName().replaceAll("/", "")+".txt");
			
			// add the info to the sRNA conservation table
			for(int k=0;k<bltResults.length;k++){
				String genomeAccession = bltResults[k][0];
				// get genome name
				for(String genomeAliases : accessionToName.keySet()){ 
					String[] aliases = genomeAliases.split(";");
					for(String alias : aliases){
						// summarize
						if(genomeAccession.equals(alias)){
							// add the info to the sRNA conservation table
							String genomeName = accessionToName.get(genomeAliases);
							System.out.println(genomeName);
							table[i+1][ArrayUtils.findColumn(table, genomeName)] = bltResults[k][2]+"";
							// add the info to the list of IDs from Jolley phylogeny tree
							listIDs.add(accessionToID.get(genomeAliases));
						}
					}
				}
			}
			
//			// create phyloXML table with bacteria only present in the alignment
//			Phylogeny phy = PhylogenyToolsJolley.readAllBacteriaPhylogeny();
//			PhylogenyToolsJolley.extractPhylogeny(phy, listIDs);
//			PhylogenyToolsJolley.addInfoTOGenomePhylogeny(phy);
//			FileUtils.saveText(phy.toPhyloXML(0),WebUtils.WEBPATH+"sRNAs"+File.separator+"PhyloXML"+File.separator+"PhyloXML_"+sRNA.getName().replaceAll("/", "")+".xml");
//			
//			// create phyloXML table with bacteria only present in the alignment
//			phy = PhylogenyToolsJolley.readAllBacteriaPhylogeny();
//			PhylogenyToolsJolley.hidePhylogeny(phy, listIDs);
//			PhylogenyToolsJolley.addInfoTOGenomePhylogeny(phy);
//			FileUtils.saveText(phy.toPhyloXML(0),WebUtils.WEBPATH+"sRNAs"+File.separator+"PhyloXML"+File.separator+"PhyloXMLAll_"+sRNA.getName().replaceAll("/", "")+".xml");
		}
		return table;
	}
	
	/**
	 * Add Srna to any Genome by reading BlastResults and extracting the lower e-value result<br>
	 * Srna with an identities less than 25% are not kept!!!<br>
	 * 
	 * This method has to be run after <code>multiSpeciesAlign()</code> because it will uses the Blast Result files in Temp folder 
	 * 
	 * @param genome
	 */
	public static void addToGenome(String genomeName){
		Genome genome = Genome.loadGenome(genomeName);
		String[][] arrayGenome = TabDelimitedTableReader.read(Database.getTEMP_PATH()+"GenomesArray.txt");
		TreeMap<String,String> accessionToName = new TreeMap<String, String>();
		for(int i=0;i<arrayGenome.length;i++){
			for(String accession : arrayGenome[i][2].split(";")){
				accessionToName.put(accession, arrayGenome[i][0]);
			}
		}
		
		ArrayList<Srna> sRNAs = Srna.getEGDeALLSrnas();
		for(Srna sRNA : SrnaTables.getOliverInfo()){
			if(sRNA.getName().equals("rli64")){
				sRNAs.add(sRNA);
			}
		}
		
		for(Srna sRNA : sRNAs){
			/*
			 * Get position in the current Genome
			 */
			BlastResult bltResult = extractPosition(sRNA,genome,accessionToName);
			if(bltResult!=null){
				char strand = '+';
				if(!bltResult.strand) strand = '-';
				Srna sRNANew = new Srna(sRNA.getName(), bltResult.begin, bltResult.end, strand);
				sRNANew.getFeatures().put("Conservation from EGD-e:", bltResult.identities+"");
				sRNANew.setGenomeName(genome.getSpecies());
				
				sRNANew.setFoundIn(sRNA.getFoundIn());
				sRNANew.setRef(sRNA.getRef());
				sRNANew.setTypeSrna(sRNA.getTypeSrna());
				sRNANew.setSynonym(sRNA.getSynonym());
				for(String accession : genome.getChromosomes().keySet()){
					Chromosome chromo = genome.getChromosomes().get(accession);
					if(accession.equals(bltResult.hit_accession)){
						sRNANew.setChromosomeID(accession);
						if(sRNANew.getTypeSrna()==TypeSrna.Srna){
							chromo.getsRNAs().put(sRNANew.getName(),sRNANew);
						}else if(sRNANew.getTypeSrna()==TypeSrna.CisReg){
							chromo.getCisRegs().put(sRNANew.getName(),sRNANew);
						}else if(sRNANew.getTypeSrna()==TypeSrna.ASrna){
							chromo.getAsRNAs().put(sRNANew.getName(),sRNANew);
						}else{
							System.err.println("Did not include: "+sRNANew.getName());
						}
					}
				}
			}
			String pathSequences = Database.getGENOMES_PATH()+File.separator+genomeName+File.separator+"Sequences"+File.separator;
			System.out.println(pathSequences+sRNA.getName());
			sRNA.save(pathSequences+sRNA.getName());
		}
		
		String path = Database.getGENOMES_PATH()+genomeName;
		GenomeConversion.updateAllElements(genome);
		GenomeConversion.createAnnotation(genome,path);
		
	}
	
	/**
	 * 	From Blast Result extract position in the Genome of each sRNA<br>
	 * 
	 * 	Srna with an identity lower than 25% are not inserted
	 * 
	 * @param sRNAs
	 * @throws IOException
	 */
	public static BlastResult extractPosition(Srna sRNA,Genome genome,TreeMap<String,String> accessionToName){
		// read each sRNA XML file and right the table accordingly
		// we need to delete one line in the file otherwise it will not be read
		String blastResult = Database.getTEMP_PATH()+"resultBlast_"+sRNA.getName().replaceAll("/", "")+".xml";
		String text = FileUtils.readText(blastResult);
		String[] lines = text.split("\n");
		// delete line 2
		if(lines[1].contains("DOCTYPE BlastOutput")){
			lines[1]=lines[0];
			text="";
			for(int j=1;j<lines.length;j++){
				text+=lines[j]+"\n";
			}
			FileUtils.saveText(text, blastResult);
		}
		// get Blast results
		ArrayList<BlastResult> results = BlastResult.getResultsFromXML(blastResult);
		double evaluePrevious = 1000000000;
		BlastResult finalResult = null;
		for(BlastResult blt : results){
			System.out.println(blt.hit_accession+" "+blt.eValue+" "+blt.bitScore+"  "+blt.identities);
			String currentGenomeName = accessionToName.get(blt.hit_accession).replaceAll("_", " ");
			if(currentGenomeName.equals(genome.getSpecies())){
				System.out.println(currentGenomeName+" --------- "+genome.getSpecies());
				// get result with lower score
				double evalue = blt.eValue;
				if(evaluePrevious > evalue){
					finalResult = blt;
					evaluePrevious = evalue;
				}
			}
		}
		return finalResult;
	}
	
	/**
	 * Read <code>Srna.PATH_CONSERVATION</code> table and display it in HeatMapView <br>
	 * Headers are reorganized by their phylogeny positions
	 * @throws Exception
	 */
	public static void createConservationTables() throws Exception{
		ExpressionMatrix allSrna = ExpressionMatrix.loadTab(Srna.PATH_CONSERVATION,true);
		
		// first organize column by phylogeny
//		Phylogeny phy = PhylogenyToolsJolley.readAllBacteriaPhylogeny();
//		String[][] arrayGenome = TabDelimitedTableReader.read(Database.getTEMP_PATH()+"GenomesArray.txt");
//		TreeMap<String,String> idToName = new TreeMap<String, String>();
//		for(int i=0;i<arrayGenome.length;i++) idToName.put(arrayGenome[i][1], arrayGenome[i][0]);
//		ArrayList<String> headers = new ArrayList<String>();
//	    for(String node :  phy.getAllExternalNodeNames()) {
//	     	String id = node.substring(0, node.indexOf("|"));
//	     	if(node.indexOf("Ehrlichia")!=-1){
//	     		System.out.println(node+"op");
//	     	}
//	     	if(idToName.containsKey(id)){
//	     		//System.out.println(idToName.get(id));
//	     		headers.add(idToName.get(id));
//	     	}
//	    }
//	    
//	    for(String header : allSrna.getHeaders()){
//	    	if(!headers.contains(header)){
//	    		System.err.println(header+"uo");
//	    		
//	    	}
//	    }
//		System.out.println(headers.size()+" "+allSrna.getNumberColumn()+" "+allSrna.getHeaders().size());
//		allSrna = allSrna.getSubMatrixColumn(headers);

		ArrayList<Srna> sRNAs = Srna.getEGDeALLSrnas();
		for(Srna sRNA : SrnaTables.getOliverInfo()){
			if(sRNA.getName().equals("rli64")){
				sRNAs.add(sRNA);
			}
		}
		
		ArrayList<String> rowNames = new ArrayList<String>();
		for(Srna sRNA : sRNAs){
			if(sRNA.getTypeSrna()==TypeSrna.Srna) rowNames.add(sRNA.getName());
		}
//		TableSWTView.displayMatrix(allSrna.getSubMatrixRow(rowNames),"List of sRNAs");
//		rowNames.clear();
//		for(Srna sRNA : sRNAs){
//			if(sRNA.getTypeSrna()==TypeSrna.ASrna) rowNames.add(sRNA.getName());
//		}
//		TableSWTView.displayMatrix(allSrna.getSubMatrixRow(rowNames),"List of asRNAs");
//		rowNames.clear();
//		for(Srna sRNA : sRNAs){
//			if(sRNA.getTypeSrna()==TypeSrna.CisReg) rowNames.add(sRNA.getName());
//		}
//		TableSWTView.displayMatrix(allSrna.getSubMatrixRow(rowNames),"List of cisRegs");
		rowNames.clear();
	}
	
	/**
	 * Read sRNAConservation10403S.txt table and display it in HeatMapView <br>
	 * Headers are reorganized by their phylogeny positions
	 * @throws Exception
	 */
	public static void createConservationTables10403S() throws Exception{
		//ExpressionMatrix allSrna = ExpressionMatrix.readFromFile(Database.getTEMP_PATH()+"sRNAConservation10403S.txt",true);
		
		// first organize column by phylogeny
//		Phylogeny phy = PhylogenyToolsJolley.readAllBacteriaPhylogeny();
//		String[][] arrayGenome = TabDelimitedTableReader.read(Database.getTEMP_PATH()+"GenomesArray.txt");
//		TreeMap<String,String> idToName = new TreeMap<String, String>();
//		for(int i=0;i<arrayGenome.length;i++) idToName.put(arrayGenome[i][1], arrayGenome[i][0]);
//		ArrayList<String> headers = new ArrayList<String>();
//	    for(String node :  phy.getAllExternalNodeNames()) {
//	     	String id = node.substring(0, node.indexOf("|"));
//	     	if(node.indexOf("Ehrlichia")!=-1){
//	     		System.out.println(node+"op");
//	     	}
//	     	if(idToName.containsKey(id)){
//	     		//System.out.println(idToName.get(id));
//	     		headers.add(idToName.get(id));
//	     	}
//	    }
//	    
//	    for(String header : allSrna.getHeaders()){
//		    	if(!headers.contains(header)){
//		    		System.err.println(header+"uo");
//		    		
//		    	}
//	    }
//	    System.out.println(headers.size()+" "+allSrna.getNumberColumn()+" "+allSrna.getHeaders().size());
//	    allSrna = allSrna.getSubMatrixColumn(headers);
//	    HeatMapView.displayMatrix(allSrna,"List of sRNAs from 10403S");
	}
}
