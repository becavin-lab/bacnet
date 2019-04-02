package bacnet.scripts.phylogeny;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.forester.archaeopteryx.Archaeopteryx;
import org.forester.phylogeny.Phylogeny;
import bacnet.Database;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.GenomeNCBIFolderTools;
import bacnet.scripts.blast.MultiSequenceBlastProtein;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import bacnet.utils.VectorUtils;

/**
 * Tools to blast a small ORF on other bacterial genomes, and display results on a phylogeny tree
 * 
 * @author UIBC
 *
 */
public class PhylogenyRli42vsRsbR {

	//	public static String PATH_GENOMES = "/Users/christophebecavin/Documents/PasteurSVN/Analysis/Egd-e Annotation/All-Firmicutes-Species_NCBI_ID.txt";
	//	public static String PATH_GENOMES = "D:/PasteurSVN/Analysis/Egd-e Annotation/All-Firmicutes-Species_NCBI_ID.txt";
	//public static String PATH_GENOMES_LIST = GenomeNCBITools.PATH_NCBI_WIN+"listBacteria.excel";
	private static String FTP_PATH = "";

	public static void run(){
		
		/*
		 * Get list of bacteria genomes
		 */
		//cleanListGenome();
		
		/*
		 * downloadGenomes
		 */
		//GenomeNCBIFolderTools.downloadAllGenomes(tableGenomes,pathGenomes);
		
		/**
		 * Create BlastDB for ORF and smallORF
		 */
			
//		TreeSet<String> listGenome = new TreeSet<>();
//		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
//		for(String bacteria : listBacteria){
//			File path = new File(GenomeNCBITools.PATH_NCBI_BacGenome+bacteria+File.separator);
//			for(File file : path.listFiles()){
//				if(file.getAbsolutePath().endsWith("_Info.excel")){
//					System.out.println(file.getAbsolutePath());
//					file.delete();
//				}
//				
//			}	
//		}
//		TabDelimitedTableReader.saveTreeSet(listGenome, GenomeNCBIFolderTools.PATH_GENOMES_LIST+"multichromo.txt");
		
//		BlastDatabase.createDatabaseForSmallORF();
//		BlastDatabase.createDatabaseForORF();
		
		
		/* 
		 * Run BlastP
		 */
//		MultiSequenceBlastProtein.run(Database.getInstance().getPATH()+"N-TermSVN/Homologs/ListeriaStressosome.txt",
//				false, 0.0001,GenomeNCBITools.PATH_NCBI_BacGenome,"path"+"/List_bacteria_assembly_summary.txt",true);
//		MultiSequenceBlastProtein.run(Database.getInstance().getPATH()+"N-Term SVN/Homologs/Bacillus168Stressosome.txt",
//				false, 0.0001,GenomeNCBITools.PATH_NCBI_BacGenome,path+"/List_bacteria_assembly_summary.txt",true);
//		MultiSequenceBlastProtein.run(Database.getInstance().getPATH()+"N-Term SVN/Homologs/ListeriaStressosome.txt",
//				false, 0.0001,GenomeNCBITools.PATH_NCBI_BacGenome,Database.getInstance().getPATH()+"N-Term SVN/Homologs/List_bacteria_assembly_summary.txt",false);
//		MultiSequenceBlastProtein.run(Database.getInstance().getPATH()+"N-Term SVN/Homologs/Bacillus168Stressosome.txt",
//				false, 0.0001,GenomeNCBITools.PATH_NCBI_BacGenome,Database.getInstance().getPATH()+"N-Term SVN/Homologs/List_bacteria_assembly_summary.txt",false);
		MultiSequenceBlastProtein.run(Database.getInstance().getPath()+"N-TermSVN/Homologs/Rli42.txt",
				true, 0.01,GenomeNCBITools.PATH_NCBI_BacGenome,"path"+"/List_bacteria_assembly_summary.txt",true);

		/**
		 *    Combine all files
		 */
//		combineAllBlastResults(Database.getInstance().getPATH()+"N-Term SVN/Homologs/ListeriaStressosome.txt",Database.getInstance().getPATH()+"N-Term SVN/Homologs/List_bacteria_assembly_summary.txt");
//		combineAllBlastResults(Database.getInstance().getPATH()+"N-Term SVN/Homologs/Bacillus168Stressosome.txt",Database.getInstance().getPATH()+"N-Term SVN/Homologs/List_bacteria_assembly_summary.txt");
//		combineAllBlastResults(Database.getInstance().getPATH()+"N-Term SVN/Homologs/Rli42.txt",path+"/List_bacteria_assembly_summary.txt");
		
		/*
		 * Summarize paralogs presence for stressosome
		 */
//		findNumberParalogs();
		
		
		/*
		 * Combine all files in a lite table version
		 */
		//combineAllBlastResultsLite(Database.getInstance().getPATH()+"N-Term SVN/Homologs/ListeriaStressosome.txt",GenomeNCBITools.PATH_NCBI_BacGenome+"/List_bacteria_assembly_summary.txt");
		//combineAllBlastResultsLite(Database.getInstance().getPATH()+"N-Term SVN/Homologs/Bacillus168Stressosome.txt",GenomeNCBITools.PATH_NCBI_BacGenome+"/List_bacteria_assembly_summary.txt");
//		combineAllBlastResultsLite(Database.getInstance().getPATH()+"N-Term SVN/Homologs/Rli42.txt",path+"/List_bacteria_assembly_summary.txt");
		
		
		/*
		 * assuming that Lite.excel table have been reduced manually by removing doublons
		 */
		//createPhylogenyFile(smallORFs);

		/*
		 * ALIGN using ClustalW and create phylogeny file using MEGA6<br>
		 * Display phylogeny with FigTree
		 */

		/*
		 * Display multi align with appropriate colors
		 */
		//		displayMultiAlign(smallORFs);

		/*
		 * Displaying the newly created tree with Archaeopteryx. OBSOLETE
		 */
		//displayPhylogeny(smallORFs);
		
//		String path = "/Users/cbecavin/Documents/PasteurSVN/N-Term SVN/Homologs/";
//		String[][] rsbRBlast = TabDelimitedTableReader.read(path+"RSbRST_rli42_AllBacteria.txt");
//		ArrayList<String> finalresults = new ArrayList<>();
//		for(int i=0;i<rsbRBlast.length;i++){
//			String strain = rsbRBlast[i][0];
//			if(!strain.equals("Strain")){
//				System.out.println(strain);
//				String rli42 = rsbRBlast[i][1];
//				String rsbR = rsbRBlast[i][2];
//				String rsbS = rsbRBlast[i][4];
//				String rsbT = rsbRBlast[i][6];
//				
//				String newLine = strain+"\t"+rli42+"\t";
//				String seq="";
//				if(!rsbR.equals("")){
//					seq = MultiSequenceBlastProtein.findORFSequence(rsbR, strain);
//				}
//				newLine+=rsbR+"\t"+seq+"\t";
//				seq="";
//				if(!rsbS.equals("")){
//					seq = MultiSequenceBlastProtein.findORFSequence(rsbS, strain);
//				}
//				newLine+=rsbS+"\t"+seq+"\t";
//				seq="";
//				if(!rsbT.equals("")){
//					seq = MultiSequenceBlastProtein.findORFSequence(rsbT, strain);
//				}
//				newLine+=rsbT+"\t"+seq;
//				finalresults.add(newLine);
//			}
//		}
//		
//		TabDelimitedTableReader.saveList(finalresults, path+"RSbRST_rli42_Sequences_AllBacteria.txt");
		
		
		
		
	}
	
	public static void combineAllBlastResults(String fileName,String listBacteriaFileName){
		/*
		 * Set-up variables
		 */
		String suffix = "";
		ArrayList<String> orfs = new ArrayList<String>();
		/*
		 * Add rli42
		 */
		orfs.add("Rli42_EGD-e;MTNKKVVRVVVILMLIAIVLSSVLTGVLMFL");
		
		if(FileUtils.getExtension(fileName).equals(".txt")){
			HashMap<String, String> nameToAminoAcidSequence = TabDelimitedTableReader.readHashMap(fileName);
			for(String key : nameToAminoAcidSequence.keySet()){
				orfs.add(key+";"+nameToAminoAcidSequence.get(key));
				System.out.println(key+";"+nameToAminoAcidSequence.get(key));
				File file = new File(Database.getTEMP_PATH()+key+"/");
				file.mkdir();
			}
		}else if(FileUtils.getExtension(fileName).equals(".fasta")){
			try {
				FileInputStream inStream = new FileInputStream( fileName );
				FastaReader<ProteinSequence,AminoAcidCompound> fastaReader = 
				new FastaReader<ProteinSequence,AminoAcidCompound>(
						inStream, 
						new NCBIFastaHeaderParser<ProteinSequence,AminoAcidCompound>(), 
						new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
				LinkedHashMap<String, ProteinSequence> genomeSequences = fastaReader.process();
				for(String key : genomeSequences.keySet()){
					orfs.add(key+";"+genomeSequences.get(key).getSequenceAsString());
					System.out.println(key+";"+genomeSequences.get(key).getSequenceAsString());
					File file = new File(Database.getTEMP_PATH()+key+"/");
					file.mkdir();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		
		/*
		 * initiate table
		 */
		ArrayList<String> listBacteria = TabDelimitedTableReader.readList(listBacteriaFileName);
		String[][] results = new String[listBacteria.size()+1][11*orfs.size()+1];
		for(int i=0;i<results.length;i++){
			for(int j =0;j<results[0].length;j++){
				results[i][j] = "";
			}
		}
		HashMap<String,Integer> genomeToIndex = new HashMap<String, Integer>();
		for(int i=1;i<results.length;i++){
			results[i][0] = listBacteria.get(i-1);
			genomeToIndex.put(listBacteria.get(i-1), i);
		}
		results[0][0] = "Strain";
		
		for(int k=0;k<orfs.size();k++){
			String orf = orfs.get(k);
			String orfName = orf.split(";")[0];
			ArrayList<String> listHits = TabDelimitedTableReader.readList(Database.getTEMP_PATH()+"/"+orfName+"/"+orfName+"_BlastResult.excel",true);
			int index = 11*k + 1;
			for(int j=0;j<listHits.get(0).split("\t").length;j++){
				String headerTemp = listHits.get(0).split("\t")[j];
				results[0][index+j] = headerTemp+"_"+orfName;
			}
			
			for(int i=1;i<listHits.size();i++){
				String genome = listHits.get(i).split("\t")[1];
				int indexRow = genomeToIndex.get(genome);
				for(int j=0;j<listHits.get(i).split("\t").length;j++){
					String rowTemp = listHits.get(i).split("\t")[j];
					results[indexRow][index+j] = rowTemp;
				}
								
			}
		}
		
		TabDelimitedTableReader.save(results,Database.getTEMP_PATH()+"/"+FileUtils.removeExtensionAndPath(fileName)+"_ResultBlast.excel");
		
	}
	
	public static void combineAllBlastResultsLite(String fileName,String listBacteriaFileName){
		/*
		 * Set-up variables
		 */
		String suffix = "";
		ArrayList<String> orfs = new ArrayList<String>();
		/*
		 * Add rli42
		 */
		orfs.add("Rli42_EGD-e;MTNKKVVRVVVILMLIAIVLSSVLTGVLMFL");
		
		if(FileUtils.getExtension(fileName).equals(".txt")){
			HashMap<String, String> nameToAminoAcidSequence = TabDelimitedTableReader.readHashMap(fileName);
			for(String key : nameToAminoAcidSequence.keySet()){
				orfs.add(key+";"+nameToAminoAcidSequence.get(key));
				System.out.println(key+";"+nameToAminoAcidSequence.get(key));
				File file = new File(Database.getTEMP_PATH()+key+"/");
				file.mkdir();
			}
		}else if(FileUtils.getExtension(fileName).equals(".fasta")){
			try {
				FileInputStream inStream = new FileInputStream( fileName );
				FastaReader<ProteinSequence,AminoAcidCompound> fastaReader = 
				new FastaReader<ProteinSequence,AminoAcidCompound>(
						inStream, 
						new NCBIFastaHeaderParser<ProteinSequence,AminoAcidCompound>(), 
						new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
				LinkedHashMap<String, ProteinSequence> genomeSequences = fastaReader.process();
				for(String key : genomeSequences.keySet()){
					orfs.add(key+";"+genomeSequences.get(key).getSequenceAsString());
					System.out.println(key+";"+genomeSequences.get(key).getSequenceAsString());
					File file = new File(Database.getTEMP_PATH()+key+"/");
					file.mkdir();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*
		 * initiate table
		 */
		ArrayList<String> listBacteria = TabDelimitedTableReader.readList(listBacteriaFileName);
		String[][] results = new String[listBacteria.size()+1][6*orfs.size()+1];
		for(int i=0;i<results.length;i++){
			for(int j =0;j<results[0].length;j++){
				results[i][j] = "";
			}
		}
		HashMap<String,Integer> genomeToIndex = new HashMap<String, Integer>();
		for(int i=1;i<results.length;i++){
			results[i][0] = listBacteria.get(i-1);
			genomeToIndex.put(listBacteria.get(i-1), i);
		}
		results[0][0] = "Strain";
		
		for(int k=0;k<orfs.size();k++){
			String orf = orfs.get(k);
			String orfName = orf.split(";")[0];
			ArrayList<String> listHits = TabDelimitedTableReader.readList(Database.getTEMP_PATH()+"/"+orfName+"/"+orfName+"_BlastResult_Lite.excel",true);
			int index = 6*k + 1;
			for(int j=0;j<listHits.get(0).split("\t").length;j++){
				String headerTemp = listHits.get(0).split("\t")[j];
				results[0][index+j] = headerTemp+"_"+orfName;
			}
			
			for(int i=1;i<listHits.size();i++){
				String genome = listHits.get(i).split("\t")[1];
				int indexRow = genomeToIndex.get(genome);
				for(int j=0;j<listHits.get(i).split("\t").length;j++){
					String rowTemp = listHits.get(i).split("\t")[j];
					results[indexRow][index+j] = rowTemp;
				}
								
			}
		}
		
		System.out.println("Saved"+Database.getTEMP_PATH()+"/"+FileUtils.removeExtensionAndPath(fileName)+"_ResultBlast_Lite.excel");
		TabDelimitedTableReader.save(results,Database.getTEMP_PATH()+"/"+FileUtils.removeExtensionAndPath(fileName)+"_ResultBlast_Lite.excel");
		
	}

	public static void findNumberParalogs(){
		String orfName = "RsbR Lmo0889";
		//String orfName = "RsbT Lmo0891";
		//String orfName = "RsbS Lmo0890";
		ArrayList<String> listBlastResult = TabDelimitedTableReader.readList(Database.getTEMP_PATH()+"/"+orfName+"/"+orfName+"_BlastResult_All.excel",true);
		ArrayList<String> listGenomes = TabDelimitedTableReader.readList(Database.getInstance().getPath()+"N-Term SVN/Homologs/List_bacteria_assembly_summary.txt");
		
		ArrayList<String> finalResults = new ArrayList<>();
		finalResults.add("Strain\tNb Paralogs\tListParalogs:Accession;eValue;identities;HitDefinition");
		for(String genomeName : listGenomes){
			ArrayList<String> rows = new ArrayList<>();
			for(int i=0;i<listBlastResult.size();i++){
				String row = listBlastResult.get(i);
				//System.out.println(row);
				String genomeTemp = row.split("\t")[1];
				
				if(genomeName.equals(genomeTemp) && row.split("\t").length>4){
					rows.add(row);
				}
			}
			
			String finalRow = genomeName+"\t"+rows.size()+"\t";
			if(rows.size()>0){
				for(String row : rows){
					String hit_accession = row.split("\t")[2];
					String eValue = row.split("\t")[3];
					String identites = row.split("\t")[4];
					String hitdef = row.split("\t")[8];
					finalRow += hit_accession+";"+eValue+";"+identites+";"+hitdef+";;";
				}
			}
			
			//ORF	Strain	hit_accession	eValue	identities	ident	bitScore	hit_id	blt.hit_def	hit_seq	ORF_seq
			//RsbR Lmo0889	Acetobacterium_woodii_DSM_1030	WP_014356131.1	3.45E-29	17.26618705	48	110.538	WP_014356131.1	RsbT co-antagonist protein rsbRA [Acetobacterium woodii]	YENRLKEQSHTIREMSTPTIKLWEGVMVLPIVGVVDSMRAQHMMESMLSKIAETYAKVIILDIHGVAAVDTAVANHLIKITKATKLMGCECILSGISPAVAQTIIQLGIDMDAINTRATLSDALSEAFTMLNLKVCKKK	 
			System.out.println("paralog: "+genomeName+" "+rows.size());
			System.out.println(finalRow);
			finalResults.add(finalRow);
		}
	
		TabDelimitedTableReader.saveList(finalResults, Database.getTEMP_PATH()+"/"+orfName+"_BlastResult_All.excel");
		
	}
	
	/**
	 * From the downloaded list of genomes on RefSeq, clean the list of strain
	 * bacteria_assembly_summary.txt is the final output
	 * 
	 */
	private static void cleanListGenome(){
		/*
		 * correct strain names
		 */
//		String[][] liststrain = TabDelimitedTableReader.read("D:/Genome NCBI/strain_name.txt");
//		for(int i =0;i<liststrain.length;i++){
//			String species = liststrain[i][0];
//			String strain = liststrain[i][1];
//			if(!species.contains(strain)){
//				species = species + " " +strain;
//				System.out.println("change name:"+species);
//			}else{
//				System.out.println(species);
//			}
//			liststrain[i][2]=species;
//		}
//		TabDelimitedTableReader.save(liststrain, "D:/Genome NCBI/strain_name_correct.txt");
		
		/*
		 * Remove duplicates in the list of strain
		 */
		String[][] arraystrain = TabDelimitedTableReader.read("D:/GenomeNCBI/bacteria_assembly_summary_raw.txt");
		TreeSet<String> listGenomes = new TreeSet<>();
		ArrayList<String> correctArray = new ArrayList<>();
		for(int i =0;i<arraystrain.length;i++){
			String strain = arraystrain[i][ArrayUtils.findColumn(arraystrain, "Corrected_strain_name")];
			if(!listGenomes.contains(strain)){
				correctArray.add(VectorUtils.toString(ArrayUtils.getRow(arraystrain, i)));
				listGenomes.add(strain);
			}
		}
		TabDelimitedTableReader.saveList(correctArray, "D:/GenomeNCBI/bacteria_assembly_summary.txt");
	}
	
	public static void displayMultiAlign(ArrayList<Sequence> smallORFs, EPartService partService){
		for(Sequence smallORF : smallORFs){
			String fileNameTemp = Database.getTEMP_PATH()+"/"+smallORF.getName()+"/Summary-"+smallORF.getName()+"-Alignment.fasta";
			ArrayList<String> listseq = TabDelimitedTableReader.readList(fileNameTemp);
			ArrayList<String> list = new ArrayList<>();
			for(int i=0;i<listseq.size();i++){
				//System.out.println(">"+listseq.get(i));
				list.add(">"+listseq.get(i).split(" ")[0].trim());
				list.add(listseq.get(i).split(" ")[listseq.get(i).split(" ").length-1].trim());
			}
			String fileName = Database.getTEMP_PATH()+"/"+smallORF.getName()+"/Summary-"+smallORF.getName()+"-Alignment-Curate.fasta";
			TabDelimitedTableReader.saveList(list, fileName);
			//AlignmentView.displayMultiAlignment(fileName,smallORF.getName(),partService);
			
		}
	}

	/**
	 * reading filtered table and create fasta file, assuming that Lite.excel table have been reduced manually by removing doublons
	 */
	public static void createPhylogenyFile(ArrayList<Sequence> smallORFs){
		for(Sequence smallORF : smallORFs){
			String[][] summary = TabDelimitedTableReader.read(Database.getTEMP_PATH()+"/"+smallORF.getName()+"/Summary-"+smallORF.getName()+"-Lite.excel");
			ArrayList<String> hitList = new ArrayList();
			for(int i=0;i<summary.length;i++){
				hitList.add(">"+summary[i][1]);
				hitList.add(summary[i][0]);
			}
			TabDelimitedTableReader.saveList(hitList, Database.getTEMP_PATH()+"/"+smallORF.getName()+"/Summary-"+smallORF.getName()+"-Sequences-Lite.fasta");
		}
	}


	/**
	 * Displaying the newly created tree with Archaeopteryx.
	 */
	public static void displayPhylogeny(ArrayList<Sequence> smallORFs){
		for(Sequence smallORF : smallORFs){
			String[][] summary = TabDelimitedTableReader.read(Database.getTEMP_PATH()+"/"+smallORF.getName()+"/Summary-"+smallORF.getName()+"-Lite.excel");
			HashMap<String, String[]> infoNodes = new HashMap<>();
			for(int i=1;i<summary.length;i++){
				String[] row = {summary[i][0],summary[i][1]};
				String genomeNCBI = summary[i][1];
				if(!genomeNCBI.equals("")){
					infoNodes.put(genomeNCBI, row);
				}
			}
			final Phylogeny[] phylogenies = {PhylogenyToolsJolley.readPhylogeny(Database.getTEMP_PATH()+"/"+smallORF.getName()+"/"+smallORF.getName()+".nwk")};
			PhylogenyToolsJolley.addInfoToPhylogeny(phylogenies[0], infoNodes);
			Archaeopteryx.createApplication( phylogenies,Database.getANALYSIS_PATH()+"/Egd-e Annotation/_aptx_configuration_file.txt","Phylogeny with forester");
		}
	}


	/**
	 * Remove specific files in genome folders
	 */
	public static void removeFiles(){
		String[][] genomeArray = TabDelimitedTableReader.read(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
		/*
		 * Copy fna files to temp folder
		 */
		for(int i=1;i<genomeArray.length;i++){
			String genomeName = genomeArray[i][1];
			String pathName = GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator;
			File pathGenome = new File(pathName);

			for(File file : pathGenome.listFiles()){
				if(file.getAbsolutePath().contains(genomeName)){
					file.delete();
				}
			}
		}
	}

	
	/**
	 * Read all Newick file from Firmicutes and extract the final list of genomes<br>
	 * Newick file were downloaded from PATRIC website
	 * @return
	 */
	private static ArrayList<String> getAllGenomesFromNewick(){
		/**
		 * Read Newick files and extract the list of all genomes
		 */
		ArrayList<String> genomes = new ArrayList<>();
		ArrayList<String> genomes1 = extractGenomesFromNewick(GenomeNCBIFolderTools.PATH_GENOMES_LIST + "Bacillales.nwk");
		for(String genome : genomes1) genomes.add(genome);
		ArrayList<String> genomes2 = extractGenomesFromNewick(GenomeNCBIFolderTools.PATH_GENOMES_LIST + "Clostridiales.nwk");
		for(String genome : genomes2) genomes.add(genome);
		ArrayList<String> genomes3 = extractGenomesFromNewick(GenomeNCBIFolderTools.PATH_GENOMES_LIST + "Lactobacillales.nwk");
		for(String genome : genomes3) genomes.add(genome);
		genomes.add("Macrococcus_caseolyticus_JCSC5402");
		genomes.add("Abiotrophia_defectiva_ATCC_49176");
		genomes.add("Streptococcus_sp._SK643");
		TabDelimitedTableReader.saveList(genomes, GenomeNCBIFolderTools.PATH_GENOMES_LIST+"AllGenomes.txt");
		return genomes;
	}

	/**
	 * Parse a Newick file and extract all genomes
	 * @param fileName
	 * @return
	 */
	private static ArrayList<String> extractGenomesFromNewick(String fileName){
		ArrayList<String> genomes = new ArrayList<>();
		String textFile = FileUtils.readText(fileName);
		String[] allGenomes = textFile.split(":");
		for(String genomeTemp : allGenomes){
			if(genomeTemp.contains(",")){
				String genome = genomeTemp.split(",")[1];
				if(!genome.equals("")){
					int begin = genome.lastIndexOf('(')+1;
					if(begin==-1) begin = 0;
					genome = genome.substring(begin);
					genomes.add(genome);
					//System.out.println(genome);
				}
				String number = genomeTemp.split(",")[0];
				//System.err.println(number);
			}else{
				//System.err.println(genomeTemp);
			}

		}
		return genomes;
	}

	/**
	 * Remove all files of the blast database
	 */
	private static void cleanFolders(){
		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
		for(String genomeName : listBacteria){
			File path = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator);
			boolean found = false;
			for(File file : path.listFiles()){
				String name = file.getAbsolutePath();
				if(name.contains(".phr") || name.contains(".pin") || name.contains(".psq")){
					if(name.contains("smallORF")){
						System.out.println(name);
						file.delete();
					}
					if(!name.contains("ORF")){
						System.out.println(name);
						file.delete();
					}
				}

				if(name.contains(".faa") && !name.contains(".SmallORF")){
					found =true;
				}
			}

			if(!found){
				System.out.println(path.getAbsolutePath());
			}

			if(path.listFiles().length<6){
				System.err.println(path.getAbsolutePath());
			}
		}
	}

}
