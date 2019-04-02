package bacnet.scripts.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Codon;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.ProteinTools;
import bacnet.utils.FileUtils;

/**
 * Different methods to create blast database for the genome, the ORF, and the smallORF
 * @author cbecavin
 *
 */
public class BlastDatabase {
    
    
	/**
	 * <li> Create a fasta file with all proteins from the different chromosomes
	 * <li> Using faa file create blast
	 * <li> Verify by looking at all file size 
	 * 
	 */
	public static void createDatabaseForORF(String PATH_GENOMES_LIST){
		createFastaAA(PATH_GENOMES_LIST);
		createBlastDB(PATH_GENOMES_LIST);
		verifyDatabase(PATH_GENOMES_LIST);
	}

	/**
	 * <li> translate every genomes on the 6 frames
	 * <li> Create a fasta file with all proteins from the different chromosomes
	 * <li> Using faa file create blast
	 * <li> Verify by looking at all file size 
	 * 
	 */
	public static void createDatabaseForSmallORF(){
		/*
		 * Translate all genomes in the 6 frames from a start codon to stop codon
		 */
//		try{
//			int cutoffLength = 60;
//			translateGenomes(cutoffLength);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		/*
		 * Create blastP database from the list of smallORFs
		 */
		createFastaAASmallORF();
//		createBlastSmallORFDB();
//		verifyDatabase();
	}

	/**
	 * Read all fasta file from each genome and create a list of every possible small peptides<br>
	 * Extract from NTermDatabase.createTISDB()
	 * @throws Exception 
	 */
	public static void translateGenomes(int cutoffLengthTemp, String PATH_GENOMES_LIST) throws Exception{
		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
		//final ArrayList<String> listBacteria = new ArrayList<>();
		//listBacteria.add("Vibrio_brasiliensis_LMG_20546");
		
		
		final int cutoffLength = cutoffLengthTemp;
		int j = 1;
		//for(int k=1;k<100;k++){ //13:53 - 14:02 =9 minutes
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-2);
		for(String genomeNameTemp : listBacteria){
			final String genomeName = genomeNameTemp;
			File file = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".excel");
			//if(file.length()<1000){

				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							System.out.println(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName);
							GenomeNCBI genome = new GenomeNCBI(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator,false);
							ArrayList<String> tisDB = new ArrayList<String>();
							for(ChromosomeBacteriaSequence chromosome : genome.getChromosomes().values()){
								System.out.println("chromo: "+chromosome.getAccession());
								ArrayList<String> peptides = new ArrayList<String>();
								String sequence = chromosome.getSequenceAsString().toUpperCase();
								ArrayList<Integer> startOccurences = new ArrayList<Integer>();
								for(int i=0;i<Codon.startCodon.length;i++){
									startOccurences = FileUtils.searchPosition(Codon.startCodon[i][0], sequence);
									ProteinTools.getAllPeptides(sequence, startOccurences, peptides,Codon.startCodon[i][1].charAt(0),Codon.startCodon[i][0],chromosome.getLength(),true,cutoffLength);
								}

								String seqComplement = chromosome.getReverseComplement().getSequenceAsString().toUpperCase();
								for(int i=0;i<Codon.startCodon.length;i++){
									startOccurences = FileUtils.searchPosition(Codon.startCodon[i][0], seqComplement);
									ProteinTools.getAllPeptides(seqComplement, startOccurences, peptides,Codon.startCodon[i][1].charAt(0),Codon.startCodon[i][0],chromosome.getLength(),false,cutoffLength);
								}

								TreeSet<String> finalPeptides = new TreeSet<String>();
								for(String peptide : peptides){
									finalPeptides.add(peptide);
								}
								System.out.println("Found "+finalPeptides.size()+chromosome.getAccession());

								/*
								 * Save in a file
								 */
								for(String peptide : finalPeptides){
									tisDB.add(peptide+"\t"+chromosome.getAccession());
								}
							}
							System.out.println(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".excel");
							TabDelimitedTableReader.saveList(tisDB, GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".excel");
						}
						catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				};
				j++;
				executor.execute(runnable);
			//}
		}
		System.err.println("Number of threads run: "+j+"  expected numb of data "+(listBacteria.size()));
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception");
		}
		System.err.println("All Genomes done");
	}

	/**
	 * Extract result and create fasta files
	 */
	public static void createFastaAASmallORF(){
		/*
		 * Extract result and create fasta files
		 */
		ExecutorService executor = Executors.newFixedThreadPool(20);
//		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
		final ArrayList<String> listBacteria = new ArrayList<>();
		listBacteria.add("Listeria_monocytogenes_EGD-e");
		//for(int i=1;i<51;i++){  //18.26
		for(String genomeNameTemp : listBacteria){
			final String genomeName = genomeNameTemp;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						//File file = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+"_Info.excel");
						
						File fileExcel = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".excel");
						File fileFAA = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.faa");
						//if(file.length()<1000 && fileExcel.exists() && fileFAA.length()<1000){
						//if(fileExcel.exists() && fileFAA.length()<1000){
						if(fileExcel.exists()){
							System.out.println(genomeName);
							ArrayList<String> orfList = TabDelimitedTableReader.readList(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".excel",true);
							ArrayList<String> results = new ArrayList<>();
							results.add(genomeName);
							try {
								//System.out.println("Table saved in: "+fileName);
								FileWriter fileW = new FileWriter(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.faa", false);
								BufferedWriter bufferW = new BufferedWriter(fileW);
								for(int i=0;i<orfList.size();i++){
									String sequence = orfList.get(i).split("\t")[0];
									int length = sequence.length();
									//System.out.println(genomeName+" "+orfList[i][0]+"\t"+orfList[i][1]);
									int begin = Integer.parseInt(orfList.get(i).split("\t")[1]);
									char strandChar = orfList.get(i).split("\t")[2].charAt(0);
									
									System.out.println(strandChar);
									int end = -1;
									if(strandChar == '+') end = begin + length*3;
									else end = begin - length*3;
									String name = "sORF_"+i;
									String chromosome=orfList.get(i).split("\t")[3];
//									String row = orfList[i][0]+"\t"+orfList[i][1]+"\t"+orfList[i][2]+"\t"+orfList[i][3]+"\t"+name+"|"+begin+"--"+end+"-("+strandChar+")";
//									results.add(row);
									String rowFasta = ">"+name+"|"+chromosome+"--"+begin+"--"+end+"-("+strandChar+")";
									bufferW.write(rowFasta.trim());
									bufferW.newLine();
									bufferW.write(sequence.trim());
									bufferW.newLine();
								}
								System.out.println("Save: "+GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.faa");
								bufferW.close();
								fileW.close();
							}
							catch (IOException e) {
								System.out.println("Error when writing to the file : " + GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.faa" + " - " + e);
							}
						}
					}catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			};
			executor.execute(runnable);
		}

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception");
		}
		System.err.println("All Threads done");
	}

	/**
	 * Create blastP databases
	 */
	public static void createBlastSmallORFDB(String PATH_GENOMES_LIST){
		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
//		final ArrayList<String> listBacteria = new ArrayList<>();
//		listBacteria.add("Vibrio_brasiliensis_LMG_20546");
		ArrayList<String> addGenome = new ArrayList<>();
		//for(int i=1;i<11;i++){
		for(String genomeName : listBacteria){
			File file = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.phr");
			File fileFaa = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.faa");
			//if(file.length()<1000 && fileFaa.exists()){
				addGenome.add(genomeName);
			//}
		}

		String[] genomesInput = new String[addGenome.size()];
		for(int i=0;i<addGenome.size();i++){
			//System.out.println(addGenome.get(i));
			genomesInput[i] = addGenome.get(i);
		}
		/*
		 * Blast database creation will be performed in different threads
		 */
		Blast.createBlastDatabases(GenomeNCBITools.PATH_NCBI_BacGenome,genomesInput,false,true);

	}

	/**
	 * Look at each genome folder and look at the size of each data
	 */
	public static void verifyDatabase(String PATH_GENOMES_LIST){
		System.out.println("Verify database");
		ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
		ArrayList<String> verifyDatabase = new ArrayList<>();
		verifyDatabase.add("Name\tFNA\tExcel\tfaa\tSmall faa\tphr Blast\tsmall phr Blast");
		for(String genomeName : listBacteria){
			File genomeFolder = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator);
			int lengthFNA = 0;
			for(File file : genomeFolder.listFiles()){
				if(file.getAbsolutePath().endsWith(".fna")){
					lengthFNA+=file.length();
				}
			}
			int lengthsmallORFBlast = 0;
			int lengthORFBlast = 0;
			for(File file : genomeFolder.listFiles()){
				if(file.getAbsolutePath().endsWith(".SmallORF.phr")){
					lengthsmallORFBlast+=file.length();
				}
				if(file.getAbsolutePath().endsWith(".ORF.phr")){
					lengthsmallORFBlast+=file.length();
				}
			}
			File orfList = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".excel");
			File smallOrffaa = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.faa");
			File orffaa = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".ORF.faa");
			verifyDatabase.add(genomeName+"\t"+lengthFNA+"\t"+orfList.length()+"\t"+orffaa.length()+"\t"+smallOrffaa.length()+"\t"+lengthORFBlast+"\t"+lengthsmallORFBlast);

		}
		TabDelimitedTableReader.saveList(verifyDatabase, GenomeNCBITools.PATH_NCBI_BacGenome+"verifyDatabase.excel");
	}


	/**
	 * Create blastP databases
	 */
	public static void createFastaAA(String PATH_GENOMES_LIST){
		ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
		for(String genome : listBacteria){
			File pathGenome = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genome+"/");
			final String filterFinal = ".faa";
			File[] files = pathGenome.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if(name.endsWith(filterFinal) && !name.contains("ORF.faa")) return true;
					return false;
				}
			});

			if(files.length==1) System.out.println(files[0].getAbsolutePath()+" to "+GenomeNCBITools.PATH_NCBI_BacGenome+genome+"/"+genome+".ORF.faa");
			/*
			 * Concatenate files
			 */

			ArrayList<Path> inputs = new ArrayList<Path>();
			for(File file: files){
				inputs.add(Paths.get(file.getAbsolutePath()));
			}

			// Output file
			Path output = Paths.get(GenomeNCBITools.PATH_NCBI_BacGenome+genome+"/"+genome+".ORF.faa");
			//Path output = Paths.get("D:/Temp/"+genome+".ORF.faa");

			// Charset for read and write
			Charset charset = StandardCharsets.UTF_8;

			// Join files (lines)
			for (Path path : inputs) {
				List<String> lines;
				try {
					lines = Files.readAllLines(path, charset);
					Files.write(output, lines, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Create blastP databases
	 */
	public static void createBlastDB(String PATH_GENOMES_LIST){
		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
		String[] genomesInput = new String[listBacteria.size()];
		for(int i=0;i<listBacteria.size();i++){
			//System.out.println(addGenome.get(i));
			genomesInput[i] = listBacteria.get(i);
		}
		/*
		 * Blast database creation will be performed in different threads
		 */
		Blast.createBlastDatabases(GenomeNCBITools.PATH_NCBI_BacGenome,genomesInput,false,false);

	}

	/**
	 * Remove specific files in genome folders
	 */
	public static void removeFiles(String PATH_GENOMES_LIST){
		String[][] genomeArray = TabDelimitedTableReader.read(PATH_GENOMES_LIST);
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
	 * Remove in all genome folders a certain type of file
	 * @param extension
	 */
	public static void cleanDatabase(String extension){
		File path = new File(GenomeNCBITools.PATH_NCBI_BacGenome);
		for(File file : path.listFiles()){
			System.out.println(file.getAbsolutePath());
			if(!file.getAbsolutePath().contains(".DS_Store")){
				File pathGen = new File(file.getAbsolutePath()+File.separator);
				for(File fileFasta : pathGen.listFiles()){
					String abspath = fileFasta.getAbsolutePath();
					if(abspath.endsWith(extension)){
						System.out.println(fileFasta.getAbsolutePath());
						fileFasta.delete();
					}					
				}
			}

		}
	}
}
