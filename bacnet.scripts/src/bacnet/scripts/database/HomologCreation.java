package bacnet.scripts.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import bacnet.Database;
import bacnet.datamodel.phylogeny.Phylogenomic;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.utils.ArrayUtils;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class HomologCreation {

	/**
	 * Path for data on the server when running homolog search blasts
	 */
	// public static String PATH_SCRIPT = "/home/becavin/Yersiniomics/";

//	public static String PATH_SCRIPT = "/pasteur/homes/cbecavin/Yersiniomics/";
	public static String PATH_SCRIPT = "D:/Programming/GitRepository/ListeriomicsSample/GenomeNCBI/Temp/";


	/**
	 * Path for Blastp on the server when running homolog search
	 */
//	public static String PATH_SERVER_BLAST = "/share/apps/local/rmblast-2-2-28/bin/";
	public static String PATH_SERVER_BLAST = "C:/Program Files/NCBI/blast-2.9.0+/bin/";

	/**
	 * Path for Blast+ on windows
	 */
	public static String PATH_BLAST_WIN = "C:/Program Files/NCBI/blast-2.9.0+/bin/";
	/**
	 * Path for Blast+ on MacOSX
	 */
	public static String PATH_BLAST_MAC = "/opt/ncbi-blast-2.7.1+/bin/";

	/**
	 * Shortcut for running blastP
	 */
	public static String blastP = getBlastFolder() + "blastp" + getBlastExtension();
	/**
	 * Shortcut for running makeblastdb
	 */
	public static String makeblastdb = "\"" + getBlastFolder() + "makeblastdb\"" + getBlastExtension();
	/**
	 * Path for BlastDB folder
	 */
	public static String PATH_BLASTDB = GenomeNCBI.PATH_TEMP + File.separator + "BLASTDB" + File.separator;
	/**
	 * Cutoff for indeityt value
	 */
	public static float IDENTITY_CUTOFF = 0.2f;

	/**
	 * Create blastP databases
	 */
	public static String createBlastDB(String logs) {
		logs += "Run Blast database creation.\n";
		folderCreation(PATH_BLASTDB);

		/*
		 * Blast database creation will be performed in different threads
		 */
		logs = createFAA(logs);
		// Test if Blast is installed
		if (FileUtils.exists(PATH_BLAST_WIN)) {
			logs = createBlastDatabases(logs, PATH_BLASTDB, Genome.getAvailableGenomes());
			logs += verifyDatabase(logs);
		} else {
			logs += "Cannot find an installation of Blast+.\n"
					+ "Install Blast+ from : ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/\n"
					+ "Update variables bacnet.scripts.database.PATH_BLAST_WIN or bacnet.scripts.database.PATH_BLAST_MAC\n";
		}

		return logs;
	}

	/**
	 * Create a FAA file in the BLASTDB folder for each genome. Will be use to
	 * create the db.
	 */
	public static String createFAA(String logs) {
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		for (String genomeTemp : listGenomes) {
			File pathGenome = new File(GenomeNCBI.PATH_GENOMES + genomeTemp + File.separator);
			final String filterFinal = ".fna";
			File[] files = pathGenome.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(filterFinal))
						return true;
					return false;
				}
			});
			String genome = GenomeNCBI.processGenomeName(genomeTemp);
			if (files.length == 1) {
				// Output file
				String input = files[0].getAbsolutePath();
				String output = PATH_BLASTDB + genome + "/" + genome + ".ORF.faa";
				folderCreation(PATH_BLASTDB + genome + "/");
				try {
					FileUtils.copy(input, output);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Copy " + input + " to " + output);
			} else {
				System.err.println("Different .faa were detected for " + genomeTemp
						+ ". Concatenate all .faa in one file to be able to run homolog search");
			}
		}
		logs += "All .faa files were copied to " + PATH_BLASTDB;
		return logs;
	}

	/**
	 * Create blast database for each genome contained in genomesInput
	 * 
	 * @param logs
	 * @param pathInput
	 * @param genomesInput
	 */
	public static String createBlastDatabases(String logs, String pathInput, ArrayList<String> genomesInput) {
		// build the fusion of this databases
		final ArrayList<String> genomes = genomesInput;
		final String pathFolder = pathInput;
		String dbType = "prot";
		final String dbtypeFinal = dbType;

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (String genomeTemp : genomes) {
			final String genome = GenomeNCBI.processGenomeName(genomeTemp);
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						String path_faa = pathFolder + genome + File.separator + genome + ".ORF.faa";
						String suffix = ".ORF";
						final String out = PATH_BLASTDB + genome + File.separator + genome + suffix;
						folderCreation(PATH_BLASTDB + genome + File.separator);
						String execProcess = HomologCreation.makeblastdb + " -in \"" + path_faa
								+ "\" -parse_seqids -out \"" + out + "\" -dbtype " + dbtypeFinal + " -title " + genome;
						System.out.println(execProcess);
						CMD.runProcess(execProcess, true, PATH_BLASTDB);

					} catch (Exception e) {
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
		logs += "All BlastP databases have been created in " + PATH_BLASTDB + "\n";
		return logs;
	}

	/**
	 * Look at each genome folder and look at the size of each data
	 */
	public static String verifyDatabase(String logs) {
		System.out.println("Verify database");
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		ArrayList<String> verifyDatabase = new ArrayList<>();
		verifyDatabase.add("Name\tfaa\tphr Blast\tpsq Blast");
		for (String genomeTemp : listGenomes) {
			System.out.println(genomeTemp);
			String genomeName = GenomeNCBI.processGenomeName(genomeTemp);
			File genomeFolder = new File(PATH_BLASTDB + genomeName + File.separator);
			int lengthFNA = 0;
			for (File file : genomeFolder.listFiles()) {
				if (file.getAbsolutePath().endsWith(".faa")) {
					lengthFNA += file.length();
				}
			}
			int lengthPhrBlast = 0;
			int lengthPsqBlast = 0;
			for (File file : genomeFolder.listFiles()) {
				if (file.getAbsolutePath().endsWith(".phr")) {
					lengthPhrBlast += file.length();
				}
				if (file.getAbsolutePath().endsWith(".psq")) {
					lengthPsqBlast += file.length();
				}
			}
			if (lengthPhrBlast == 0) {
				System.err.println("Cannot find : .phr for " + genomeName);
				logs += "Cannot find : .phr for " + genomeName + "\n";
			}
			if (lengthPsqBlast == 0) {
				System.err.println("Cannot find : .psq for " + genomeName);
				logs += "Cannot find : .psq for " + genomeName + "\n";
			}
			verifyDatabase.add(genomeName + "\t" + lengthFNA + "\t" + lengthPhrBlast + "\t" + lengthPsqBlast);
		}
		TabDelimitedTableReader.saveList(verifyDatabase, PATH_BLASTDB + "VerifyDatabase.excel");
		logs += "File created in : " + PATH_BLASTDB + "VerifyDatabase.excel\n";
		return logs;
	}

	/**
	 * Creation of the general command file that will be used to create the ones for
	 * each blast
	 */
	public static String createBlastScript(String logs) {
		folderCreation(GenomeNCBI.PATH_TEMP + "Threads/");
		// String blastDBFolder = PATH_BLASTDB;
		// String scriptFolder = GenomeNCBI.PATH_TEMP + "Threads";
		/*
		 * Change DB directory if you want to run it on a cluster
		 */
		String blastDBFolder = PATH_SCRIPT + "BLASTDB/";

		ArrayList<String> blastFile = new ArrayList<>();


//		blastFile.add("PATH="+PATH_SERVER_BLAST+":$PATH");
//		blastFile.add("blastp -query " + blastDBFolder + "_fileGenomePivot -db " + blastDBFolder
//				+ "_databaseTarget -out " + blastDBFolder
//				+ "_blastP_VS_T -evalue 0.01 -max_target_seqs 1 -outfmt \"6 qseqid sseqid qlen slen length nident positive evalue bitscore\"");
//		blastFile.add("blastp -query " + blastDBFolder + "_fileGenomeTarget -db " + blastDBFolder
//				+ "_databasePivot -out " + blastDBFolder
//				+ "_blastT_VS_P -evalue 0.01 -max_target_seqs 1 -outfmt \"6 qseqid sseqid qlen slen length nident positive evalue bitscore\"");
		blastFile.add("\"" + HomologCreation.blastP + "\"" + " -query " + blastDBFolder + "_fileGenomePivot -db " + blastDBFolder
				+ "_databaseTarget -out " + blastDBFolder
				+ "_blastP_VS_T -evalue 0.01 -max_target_seqs 1 -outfmt \"6 qseqid sseqid qlen slen length nident positive evalue bitscore\"");
		blastFile.add("\"" + HomologCreation.blastP + "\"" + " -query " + blastDBFolder + "_fileGenomeTarget -db " + blastDBFolder
				+ "_databasePivot -out " + blastDBFolder
				+ "_blastT_VS_P -evalue 0.01 -max_target_seqs 1 -outfmt \"6 qseqid sseqid qlen slen length nident positive evalue bitscore\"");

		blastFile.add("echo _fileGenomePivot VS _fileGenomeTarget Blast search completed");
		// ">" + scriptFolder + "_fileGenomePivotVS_fileGenomeTarget.control.txt");

		TabDelimitedTableReader.saveList(blastFile, GenomeNCBI.PATH_TEMP + "Threads/BlastTemp.txt");
		/*
		 * Create the blast commands
		 */
		createBlastCommands(".ORF", 0.01);
		logs += "All blast script created in : " + GenomeNCBI.PATH_TEMP
				+ "Threads/Commands/\nRun them with bash or using calculus cluster (see bacnet.e4.rap.setup.RunBlastSGE.sh or"
				+ " RunBlastSlurm.sh)\nBut fix first value of: HomologCreation.PATH_SCRIPT -> " + PATH_SCRIPT
				+ " which is the path for data on your server.\n"
				+ "You need also to fix the value of: HomologCreation.PATH_SERVER_BLAST -> " + PATH_SERVER_BLAST
				+ " which is the path for blastp on your server.\n"
				+ "Run again script creation after fixing these path\n";

		return logs;
	}

	/**
	 * Launch blast commands creation
	 * 
	 * @param suffix
	 * @param evalueCutoff
	 */
	public static void createBlastCommands(String suffix, double evalueCutoff) {
		folderCreation(GenomeNCBI.PATH_TEMP + "Threads/Commands/");
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		for (int i = 0; i < listGenomes.size(); i++) {
			String genome_pivot = listGenomes.get(i);
			genome_pivot = GenomeNCBI.processGenomeName(genome_pivot);
			String genome_pivot_path = getFAAPath(genome_pivot);
			if ((i + 1) < listGenomes.size()) {
				ArrayList<String> list_genomes_toBlast = extractList(listGenomes, i + 1, listGenomes.size());
				createBlastCommands(genome_pivot, ".ORF", genome_pivot_path, list_genomes_toBlast, 0);
				System.out.println("Blast commands done for " + genome_pivot);
			}
		}
	}

	/**
	 * Create .bat files with the command lines for each blast.
	 * 
	 * @param genomePivot
	 * @param proteinGenome
	 * @param suffix
	 * @param pathGenome
	 * @param listGenomes
	 * @param evalueCutoff
	 */
	public static void createBlastCommands(String genomePivot, String suffix, String pathGenome,
			ArrayList<String> listGenomes, double evalueCutoff) {
		final ArrayList<String> listGenome = listGenomes;

		ExecutorService executor = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors());

		for (String genomeNameTemp : listGenome) {
			final String genomeName = genomeNameTemp;
			final String suffixFinal = suffix;

			if (!genomeName.equals(genomePivot)) {
				/*
				 * Create .bat file for each blast
				 */
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						String separator = "/";
						String databaseTarget = genomeName + separator + genomeName + suffixFinal;
						String databasePivot = genomePivot + separator + genomePivot + suffixFinal;
						String fileGenomePivot = genomePivot + separator + genomePivot + suffix + ".faa";
						String fileGenomeTarget = genomeName + separator + genomeName + suffix + ".faa";
						String blastP_VS_T = "Results" + separator + "resultBlast_" + genomePivot + "_vs_" + genomeName
								+ ".blast.txt";
						String blastT_VS_P = "Results" + separator + "resultBlast_" + genomeName + "_vs_" + genomePivot
								+ ".blast.txt";
						String args = FileUtils
								.readText(GenomeNCBI.PATH_TEMP + "Threads" + File.separator + "BlastTemp.txt");
						args = args.replaceAll("_fileGenomePivot", fileGenomePivot);
						args = args.replaceAll("_fileGenomeTarget", fileGenomeTarget);
						args = args.replaceAll("_blastP_VS_T", blastP_VS_T);
						args = args.replaceAll("_blastT_VS_P", blastT_VS_P);
						args = args.replaceAll("_databasePivot", databasePivot);
						args = args.replaceAll("_databaseTarget", databaseTarget);
						FileUtils.saveText(args, GenomeNCBI.PATH_TEMP + "Threads" + File.separator + "Commands"
								+ File.separator + genomePivot + "_vs_" + genomeName + ".sh");
					}
				};
				executor.execute(runnable);
			}
		}

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception");
		}
	}

	/**
	 * Check every if every blastP has been performed and extract first information by calcuating indentities metric of each blast
	 * @param logs
	 * @return
	 */
	public static String verifyBlastResults(String logs) {
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		// int size_list = 10;
		int size_list = listGenomes.size();
		/*
		 * Check that every Blast was run and finished
		 */
		checkBlastComplete(size_list, listGenomes, logs);

		/*
		 * Add identity information in table results !!! VERY LONG RUN !!!
		 */
		addColumnIdentities(size_list, listGenomes, logs);
		System.out.println("Finish verification of blast results and first parsing");
		return logs;
	}
	
	/**
	 * Create all homolog tables for each genome<br>
	 * 3 different log files are saved<br>
	 * TabDelimitedTableReader.saveTreeSet(proteinNotFound, GenomeNCBI.PATH_HOMOLOGS + "ProteinNotfFound.homologs.txt");<br>
	 * TabDelimitedTableReader.saveTreeSet(noHomologs, GenomeNCBI.PATH_HOMOLOGS + "NoHomologsFound.homologs.txt");<br>
	 * TabDelimitedTableReader.saveTreeSet(lowHomologs, GenomeNCBI.PATH_HOMOLOGS + "LowHomologsFound.homologs.txt");<br><br>
	 *	
	 * @param logs
	 * @return
	 */
	public static String createHomologTable(String logs) {
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		int size_list = listGenomes.size();

		/*
		 * Combine everything together per genomes and search every homologs
		 */
		TreeSet<String> proteinNotFound = new TreeSet<String>();
		TreeSet<String> noHomologs = new TreeSet<String>();
		TreeSet<String> lowHomologs = new TreeSet<String>();
		for (int i = 0; i < size_list; i++) {
		//for (int i = 0; i < 4; i++) {
		//	int i = 8;
			/*
			 * Combine all homologs in on table
			 */
			String genome_pivot = listGenomes.get(i);
			genome_pivot = GenomeNCBI.processGenomeName(genome_pivot);
			String message = "Summary table creation for: " + genome_pivot;
			logs += message + "\n";
			ArrayList<String> list_genomes_toBlast = extractList(listGenomes, 0, size_list);
			createSummaryTable(genome_pivot, list_genomes_toBlast, proteinNotFound, noHomologs, lowHomologs);
		}
		TabDelimitedTableReader.saveTreeSet(proteinNotFound, GenomeNCBI.PATH_HOMOLOGS + "ProteinNotfFound.homologs.txt");
		TabDelimitedTableReader.saveTreeSet(noHomologs, GenomeNCBI.PATH_HOMOLOGS + "NoHomologsFound.homologs.txt");
		TabDelimitedTableReader.saveTreeSet(lowHomologs, GenomeNCBI.PATH_HOMOLOGS + "LowHomologsFound.homologs.txt");
		String message = "Save list of protein Id not found in query genome in: "+GenomeNCBI.PATH_HOMOLOGS + "ProteinNotfFound.homologs.txt"+"\n";
		message += "Save list of protein Id with no homolog in target genome: "+GenomeNCBI.PATH_HOMOLOGS + "NoHomologsFound.homologs.txt"+"\n";
		message += "Save list of protein Id with low similarity (<20%) in target genome: "+GenomeNCBI.PATH_HOMOLOGS + "LowHomologsFound.homologs.txt"+"\n";
		System.out.println(message);
		System.out.println("Finish homolog tables creation");
		return logs+message;
	}
	
	/**
	 * Add homologs to every gene by parsing all homolog tables
	 * @param logs
	 * @return
	 */
	public static String addHomologToGene(String logs) {
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		int size_list = listGenomes.size();
		ArrayList<String> genomeModified = new ArrayList<String>();
		
		/*
		 * Add the homologs to every gene object
		 */
		for (int i = 0; i < size_list; i++) {
				
			/*
			 * Add the phylogeny to the gene object
			 */
			String genome_pivot = listGenomes.get(i);
			genome_pivot = GenomeNCBI.processGenomeName(genome_pivot);
			addHomologsToGenes(genome_pivot);
			String message = "Phylogeny added to gene of: " + genome_pivot;
			logs += message + "\n";
			System.out.println(message);
			genomeModified.add(genome_pivot);
		}
		TabDelimitedTableReader.saveList(genomeModified, GenomeNCBI.PATH_HOMOLOGS + "AddedtoGenome.homologs.txt");
		
		/*
		 * Create homolog search summary file to validate creation
		 */
		System.out.println("Load log files");
		genomeModified = TabDelimitedTableReader.readList(GenomeNCBI.PATH_HOMOLOGS + "AddedtoGenome.homologs.txt");
		ArrayList<String> proteinNotFound = TabDelimitedTableReader.readList(GenomeNCBI.PATH_HOMOLOGS + "ProteinNotfFound.homologs.txt");
		ArrayList<String> noHomologs = TabDelimitedTableReader.readList(GenomeNCBI.PATH_HOMOLOGS + "NoHomologsFound.homologs.txt");
		ArrayList<String> lowHomologs = TabDelimitedTableReader.readList(GenomeNCBI.PATH_HOMOLOGS + "LowHomologsFound.homologs.txt");
		ArrayList<String> finalLog = new ArrayList<String>();
		finalLog.add("Nb of protein Id not found in query genome in: "+proteinNotFound.size());
		finalLog.add("Nb of protein Id with no homolog in target genome: "+noHomologs.size());
		finalLog.add("Nb of protein Id with low similarity (<20%) in target genome: "+lowHomologs.size());
		finalLog.add("Nb of genomes in which homolog search was added: "+ genomeModified.size()+"/"+Genome.getAvailableGenomes().size());
		TabDelimitedTableReader.saveList(finalLog, Phylogenomic.HOMOLOG_SUMMARY);
		logs+= Phylogenomic.HOMOLOG_SUMMARY + " created\n";
		
		System.out.println("Finish extraction of Blast results");
		return logs;
	}

	/**
	 * Check that every Blast was completed
	 * 
	 * @param size_list
	 * @param listGenomes
	 * @param logs
	 */
	private static void checkBlastComplete(int size_list, ArrayList<String> listGenomes, String logs) {
		for (int i = 0; i < size_list; i++) {
			String genome_pivot = listGenomes.get(i);
			genome_pivot = GenomeNCBI.processGenomeName(genome_pivot);
			String message = "Extract blast results for " + genome_pivot + " vs all other genomes " + i + "/"
					+ size_list;
			logs += message + "\n";
			if ((i + 1) < size_list) {
				ArrayList<String> list_genomes_toBlast = extractList(listGenomes, i + 1, size_list);
				/*
				 * Calculate identity value
				 */
				String results_folder = PATH_BLASTDB + File.separator + "Results" + File.separator;
				for (String genome_target : list_genomes_toBlast) {
					// System.out.println("Add column for: "+genome_pivot + " vs " + genome_target);
					String path_fileblast = results_folder + "resultBlast_" + genome_pivot + "_vs_" + genome_target
							+ ".blast.txt";
					String path_fileblast2 = results_folder + "resultBlast_" + genome_target + "_vs_" + genome_pivot
							+ ".blast.txt";
					File file = new File(path_fileblast);
					if (!file.exists()) {
						System.err.println("cannot find: " + path_fileblast);
					}
					file = new File(path_fileblast2);
					if (!file.exists()) {
						System.err.println("cannot find: " + path_fileblast);
					}

				}
			}
		}
	}

	/**
	 * Add column identity to all Blast results
	 * 
	 * @param size_list
	 * @param listGenomes
	 * @param logs
	 */
	private static void addColumnIdentities(int size_list, ArrayList<String> listGenomes, String logs) {
		for (int i = 0; i < size_list; i++) {
//		int i = 8;
			String genome_pivot = listGenomes.get(i);
			genome_pivot = GenomeNCBI.processGenomeName(genome_pivot);
			String message = "Extract blast results for " + genome_pivot + " vs all other genomes " + i + "/"
					+ size_list;
			logs += message + "\n";
			System.out.println(message);
			System.out.println(genome_pivot+" "+i);
			if ((i + 1) < size_list) {
				ArrayList<String> list_genomes_toBlast = extractList(listGenomes, i + 1, size_list);
//			ArrayList<String> list_genomes_toBlast = extractList(listGenomes, 12, 13);
//				/*
//				 * Calculate identity value
//				 */
				addColumnIdentity(genome_pivot, list_genomes_toBlast);
			}
		}
	}

	/**
	 * For each blast go through the table and add the column identity nident/qlen
	 * = column 5 / column 2
	 * BlastP results:
	 * qseqid sseqid qlen slen length nident positive evalue bitscore
	 * 
	 * @param genome_pivot
	 * @param listGenomes
	 */
	private static void addColumnIdentity(String genome_pivot, ArrayList<String> listGenomes) {
		String addedcolumn_folder = PATH_BLASTDB + File.separator + "AddedColumnIdentity" + File.separator;
		folderCreation(addedcolumn_folder);
		String results_folder = PATH_BLASTDB + File.separator + "Results" + File.separator;
		folderCreation(results_folder);
		for (String genome_target : listGenomes) {
			System.out.println("Add column for: " + genome_pivot + " vs " + genome_target);
			String[][] genomeP_vs_genomeT = TabDelimitedTableReader
					.read(results_folder + "resultBlast_" + genome_pivot + "_vs_" + genome_target + ".blast.txt");
//			String[][] genomeT_vs_genomeP = TabDelimitedTableReader
//					.read(results_folder + "resultBlast_" + genome_target + "_vs_" + genome_pivot + ".blast.txt");
			String[] columToAdd_P_VS_T = new String[genomeP_vs_genomeT.length];
			//String[] columToAdd_T_VS_P = new String[genomeT_vs_genomeP.length];

			for (int i = 0; i < genomeP_vs_genomeT.length; i++) {
				//String id_genomeT = genomeP_vs_genomeT[i][1].split("\\|")[1];
				//HashMap<String, Integer> indexRow_T_vs_P = indexRows(genomeT_vs_genomeP);
				float identitiesP_vs_T = (Float.valueOf(genomeP_vs_genomeT[i][5]))
						/ (Float.valueOf(genomeP_vs_genomeT[i][2]));
				columToAdd_P_VS_T[i] = String.valueOf(identitiesP_vs_T);
//				float identitiesP_vs_T = (Float.valueOf(genomeP_vs_genomeT[i][5]))
//						/ (Float.valueOf(genomeP_vs_genomeT[i][3]));
//				if (indexRow_T_vs_P.containsKey(id_genomeT)) {
//					int row = indexRow_T_vs_P.get(id_genomeT);
//					float identitiesT_vs_P = (Float.valueOf(genomeT_vs_genomeP[row][5]))
//							/ (Float.valueOf(genomeT_vs_genomeP[row][3]));
//					if (identitiesP_vs_T > identitiesT_vs_P) {
//						columToAdd_P_VS_T[i] = String.valueOf(identitiesP_vs_T);
//						columToAdd_T_VS_P[row] = String.valueOf(identitiesP_vs_T);
//					} else {
//						columToAdd_P_VS_T[i] = String.valueOf(identitiesT_vs_P);
//						columToAdd_T_VS_P[row] = String.valueOf(identitiesT_vs_P);
//					}
//				} else {
//					columToAdd_P_VS_T[i] = String.valueOf(identitiesP_vs_T);
//				}
			}
//			for (int i = 0; i < genomeT_vs_genomeP.length; i++) {
//				try {
//					if (columToAdd_T_VS_P[i].equals(""))
//						;
//				} catch (NullPointerException e) {
//					float identitiesT_vs_P = (Float.valueOf(genomeT_vs_genomeP[i][5]))
//							/ (Float.valueOf(genomeT_vs_genomeP[i][3]));
//					columToAdd_T_VS_P[i] = String.valueOf(identitiesT_vs_P);
//				}
//			}
			genomeP_vs_genomeT = ArrayUtils.addColumn(genomeP_vs_genomeT, columToAdd_P_VS_T);
			//genomeT_vs_genomeP = ArrayUtils.addColumn(genomeT_vs_genomeP, columToAdd_T_VS_P);
			TabDelimitedTableReader.save(genomeP_vs_genomeT,
					addedcolumn_folder + genome_pivot + "_vs_" + genome_target + ".blast.txt");
			//TabDelimitedTableReader.save(genomeT_vs_genomeP,
			//		addedcolumn_folder + genome_target + "_vs_" + genome_pivot + ".blast.txt");
			//System.out.println("Column added: " + genome_pivot + " vs " + genome_target);
			
			/*
			 * Add column the other way
			 */
			System.out.println("Add column for: " + genome_target + " vs " + genome_pivot);			
			String[][] genomeT_vs_genomeP = TabDelimitedTableReader
			.read(results_folder + "resultBlast_" + genome_target + "_vs_" + genome_pivot + ".blast.txt");
			String[] columToAdd_T_VS_P = new String[genomeT_vs_genomeP.length];
			for (int i = 0; i < genomeT_vs_genomeP.length; i++) {
				float identitiesT_vs_P = (Float.valueOf(genomeT_vs_genomeP[i][5]))
						/ (Float.valueOf(genomeT_vs_genomeP[i][2]));
				columToAdd_T_VS_P[i] = String.valueOf(identitiesT_vs_P);
			}
			genomeT_vs_genomeP = ArrayUtils.addColumn(genomeT_vs_genomeP, columToAdd_T_VS_P);
			TabDelimitedTableReader.save(genomeT_vs_genomeP,
					addedcolumn_folder + genome_target + "_vs_" + genome_pivot + ".blast.txt");
			//System.out.println("Column added: " + genome_target + " vs " + genome_pivot);

		}
	}

	/**
	 * Create a summary table for each genome with the list of protein and the list
	 * of homologies
	 * 
	 * @param genome_pivot
	 */
	private static void createSummaryTable(String genome_pivot, ArrayList<String> listGenomes, TreeSet<String> proteinNotFound, TreeSet<String> noHomologs, TreeSet<String> lowHomologs) {
		ArrayList<String> proteinList = getProteinList(genome_pivot);
		String[][] newTable = new String[proteinList.size() + 1][2];
		newTable[0][0] = "Gene_Id";
		newTable[0][1] = "Homologs";
		HashMap<String,String> proteinIdtoLocusTagPivot = Genome.loadGeneFromProteinId(GenomeNCBI.unprocessGenomeName(genome_pivot));
		for (String genome_target : listGenomes) {
		//for(int w=0; w < 4 ;w++) {
			//String genome_target = listGenomes.get(w);
			if (!genome_target.equals(genome_pivot)) {
				HashMap<String,String> proteinIdtoLocusTagTarget = Genome.loadGeneFromProteinId(GenomeNCBI.unprocessGenomeName(genome_target));
				String pathBlast = PATH_BLASTDB + File.separator + "AddedColumnIdentity" + File.separator + genome_pivot
						+ "_vs_" + genome_target + ".blast.txt";
				String[][] homologyTable = TabDelimitedTableReader.read(pathBlast);
				HashMap<String, Integer> indexRowHashmap = indexRows(homologyTable);
				int i = 1;
				for (String proteinName : proteinList) {
					// add gene_name to protein id
					String gene = proteinIdtoLocusTagPivot.get(proteinName);
					if (gene == null) {
						String rowNotFound = genome_pivot + "\t" + proteinName;
						proteinNotFound.add(rowNotFound);
						System.err.println("Cannot find gene for :" + proteinName);
					}
					newTable[i][0] = gene;
					if (indexRowHashmap.containsKey(proteinName)) {
						int indexRow = indexRowHashmap.get(proteinName);
						float identity = Float.parseFloat(homologyTable[indexRow][homologyTable[indexRow].length - 1]);
						String proteinTargetName = homologyTable[indexRow][1].replaceAll("ref|", "")
								.replace('|', ' ').trim();
						if (identity > IDENTITY_CUTOFF) {
							// Replace proteinId by locustag
							String geneTarget = proteinIdtoLocusTagTarget.get(proteinTargetName);
							if (geneTarget == null) {
								System.err.println("Cannot find gene for : " + proteinTargetName);
								String rowNotFound = genome_target + "\t" + proteinTargetName;
								proteinNotFound.add(rowNotFound);
							}

							newTable[i][1] += GenomeNCBI.unprocessGenomeName(genome_target) + ";" + geneTarget
									+ ";"+proteinTargetName+ ";" + homologyTable[indexRow][homologyTable[indexRow].length - 1] + ";;";
						}else {
							String rowNotFound = genome_pivot + "\t" + proteinName + "\t" + genome_target + "\t" + proteinTargetName +"\t"+ identity;
							lowHomologs.add(rowNotFound);
						}
					} else {
						String rowNotFound = genome_pivot + "\t" + proteinName + "\t" + genome_target;
						noHomologs.add(rowNotFound);
					}
					i++;
				}
			}
			/*
			 * Comment out if you want to control ortholog parsing by looking at log files
			 */
//			TabDelimitedTableReader.saveTreeSet(proteinNotFound, GenomeNCBI.PATH_HOMOLOGS + "ProteinNotfFound.homologs.txt");
//			TabDelimitedTableReader.saveTreeSet(noHomologs, GenomeNCBI.PATH_HOMOLOGS + "NoHomologsFound.homologs.txt");	
//			TabDelimitedTableReader.saveTreeSet(lowHomologs, GenomeNCBI.PATH_HOMOLOGS + "LowHomologsFound.homologs.txt");
			
		}
		for (int i = 1; i < newTable.length; i++) {
			try {
				newTable[i][1] = newTable[i][1].substring(4);
			} catch (NullPointerException e) {
			}
		}
		System.out.println("Save : " + GenomeNCBI.PATH_HOMOLOGS + genome_pivot + ".Allhomologs.txt");
		TabDelimitedTableReader.save(newTable, GenomeNCBI.PATH_HOMOLOGS + genome_pivot + ".Allhomologs.txt");
	}

	/**
	 * Create a hashmap between the value of the first column for each row and the
	 * index of the row
	 * 
	 * @param table
	 * @return
	 */
	private static HashMap<String, Integer> indexRows(String[][] table) {
		HashMap<String, Integer> hashMap = new HashMap<>();
		for (int i = 0; i < table.length; i++) {
			//System.out.println("Put "+table[i][0] + " "+ i);
			hashMap.put(table[i][0], i);
		}
		return hashMap;
	}

	/**
	 * Return the list of all protein id present in the genome given
	 * 
	 * @param genome
	 * @return
	 */
	private static ArrayList<String> getProteinList(String genome) {
		ArrayList<String> protein_list = new ArrayList<String>();
		try {
			FileInputStream inStream = new FileInputStream(getFAAPath(genome));
			FastaReader<ProteinSequence, AminoAcidCompound> fastaReader = new FastaReader<ProteinSequence, AminoAcidCompound>(
					inStream, new NCBIFastaHeaderParser<ProteinSequence, AminoAcidCompound>(),
					new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
			LinkedHashMap<String, ProteinSequence> genomeSequences = fastaReader.process();
			for (String key : genomeSequences.keySet()) {
				String seq_name = key.split(" ")[0];
				protein_list.add(seq_name);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return protein_list;
	}

	/**
	 * Go through all result files and create Conservation HadhMap for each Gene
	 */
	private static void addHomologsToGenes(String genome) {
		Genome genomeLoaded = Genome.loadGenome(GenomeNCBI.unprocessGenomeName(genome), false);
		String[][] blastArray = TabDelimitedTableReader.read(GenomeNCBI.PATH_HOMOLOGS + genome + ".Allhomologs.txt");
		for (int i = 1; i < blastArray.length; i++) {
			String locus = blastArray[i][0];
			String allInfo = blastArray[i][1];
			// System.out.println(locus);
			Gene gene = genomeLoaded.getGeneFromName(locus);
			if (gene != null) {
				LinkedHashMap<String, String> conservationHashMap = new LinkedHashMap<>();
				conservationHashMap.put(GenomeNCBI.unprocessGenomeName(genome), locus + ";"+gene.getProtein_id()+";1.0");
				if (!allInfo.equals("")) {
					String[] conservations = allInfo.split(";;");
					for (String conservation : conservations) {
						String genomeTarget = conservation.split(";")[0];
						String geneTarget = conservation.split(";")[1] + ";" + conservation.split(";")[2] + ";" + conservation.split(";")[3];
						// System.out.println(genomeTarget + " -----" + geneTarget);
						conservationHashMap.put(genomeTarget, geneTarget);
					}
				}
				gene.setConservationHashMap(conservationHashMap);
				// System.out.println("Found "+conservationHashMap.size()+ " homologs for
				// "+gene.getName());
				gene.setConservation(conservationHashMap.size());
				String genepath = Database.getGENOMES_PATH() + GenomeNCBI.unprocessGenomeName(genome) + File.separator
						+ "Sequences" + File.separator + gene.getName();
				// System.out.println(genepath);
				gene.save(genepath);
			}
		}
	}

	/**
	 * Extract a portion of an ArrayList into another ArrayList
	 * 
	 * @param list_init
	 * @param index_start : index of the first data taken
	 * @param index_end   : index of the last data taken + 1
	 * @return
	 */
	public static ArrayList<String> extractList(ArrayList<String> list_init, int index_start, int index_end) {
		ArrayList<String> list_end = new ArrayList<>();
		int index = index_start;
		while (index < list_init.size() && index < index_end) {
			String genome = GenomeNCBI.processGenomeName(list_init.get(index));
			list_end.add(genome);
			index++;
		}
		return list_end;
	}

	public static String getFAAPath(String genome) {
		String path = new String();
		File folder = new File(PATH_BLASTDB + genome + "/");
		File[] list_file = folder.listFiles();
		for (File file : list_file) {
			if (file.getAbsolutePath().endsWith(".faa")) {
				return file.getAbsolutePath();
			}
		}
		System.out.println(".faa file not found for " + genome);
		return path;
	}

	public static String getBlastFolder() {
		String os = System.getProperty("os.arch");
		if (os.equals("amd64"))
			return PATH_BLAST_WIN;
		else
			return PATH_BLAST_MAC;
	}

	public static String getBlastExtension() {
		String os = System.getProperty("os.arch");
		if (os.equals("amd64"))
			return ".exe";
		else
			return "";
	}

	public static void folderCreation(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
			System.out.println("Folder: " + path + " created");
		}
	}

	/**
	 * For a specific type, return the extension of the corresponding file
	 * 
	 * @param type
	 * @return
	 */
	public static String fileExtension(BlastOutputTYPE type) {
		if (type == BlastOutputTYPE.XML) {
			return ".xml";
		} else if (type == BlastOutputTYPE.ASN || type == BlastOutputTYPE.ASN_Bin || type == BlastOutputTYPE.ASN_TxT) {
			return ".asn";
		} else
			return ".txt";
	}
}
