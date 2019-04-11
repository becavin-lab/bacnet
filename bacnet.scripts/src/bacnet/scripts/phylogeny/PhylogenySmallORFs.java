package bacnet.scripts.phylogeny;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.forester.archaeopteryx.Archaeopteryx;
import org.forester.phylogeny.Phylogeny;

import bacnet.Database;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.Blast;
import bacnet.scripts.blast.GenomeNCBIFolderTools;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import bacnet.utils.VectorUtils;

/**
 * Tools to blast a small ORF on other bacterial genomes, and display results on
 * a phylogeny tree
 * 
 * @author UIBC
 *
 */
public class PhylogenySmallORFs {

	// public static String PATH_GENOMES =
	// "/Users/christophebecavin/Documents/PasteurSVN/Analysis/Egd-e
	// Annotation/All-Firmicutes-Species_NCBI_ID.txt";
	// public static String PATH_GENOMES = "D:/PasteurSVN/Analysis/Egd-e
	// Annotation/All-Firmicutes-Species_NCBI_ID.txt";
	// public static String PATH_GENOMES_LIST =
	// GenomeNCBITools.PATH_NCBI_WIN+"listBacteria.excel";
	
	public static void run(EPartService partService) {
		String tableGenomes = "D:/GenomeNCBI/bacteria_assembly_summary.txt";
		String pathGenomes = "D:/GenomeNCBI/AllBacteria";

		/*
		 * Get list of bacteria genomes
		 */
		cleanListGenome();

		/*
		 * downloadGenomes
		 */
		GenomeNCBIFolderTools.downloadAllGenomes(tableGenomes, pathGenomes);

		/*
		 * Create blastN database and BlastP database
		 */
		String[][] newGenomes = TabDelimitedTableReader.read(tableGenomes);
		String[] genomes = new String[newGenomes.length];
		for(int i=1;i<newGenomes.length;i++){
			genomes[i] = newGenomes[i][ArrayUtils.findColumn(newGenomes, "folder_name")];
		}
		Blast.createBlastDatabases(pathGenomes, genomes, true, false);
//		Blast.createBlastDatabases(pathGenomes, genomes, false, false);

		/*
		 * Run BlastP
		 */
		//MultiSequenceBlastProtein.run("D:/smallORF.txt", true);

		/*
		 * assuming that Lite.excel table have been reduced manually by removing
		 * doublons
		 */
		ArrayList<Sequence> smallORFs = new ArrayList<Sequence>();
		// Load your list of smallORFs here !!!
		createPhylogenyFile(smallORFs);

		/*
		 * ALIGN using ClustalW and create phylogeny file using MEGA6<br> Display
		 * phylogeny with FigTree
		 */

		/*
		 * Display multi align with appropriate colors
		 */
		displayMultiAlign(partService);

		/*
		 * Displaying the newly created tree with Archaeopteryx. OBSOLETE
		 */
		displayPhylogeny(smallORFs);
	}

	/**
	 * From the downloaded list of genomes on RefSeq, clean the list of strain
	 * bacteria_assembly_summary.txt is the final output
	 * 
	 */
	private static void cleanListGenome() {
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
		for (int i = 0; i < arraystrain.length; i++) {
			String strain = arraystrain[i][ArrayUtils.findColumn(arraystrain, "Corrected_strain_name")];
			if (!listGenomes.contains(strain)) {
				correctArray.add(VectorUtils.toString(ArrayUtils.getRow(arraystrain, i)));
				listGenomes.add(strain);
			}
		}
		TabDelimitedTableReader.saveList(correctArray, "D:/GenomeNCBI/bacteria_assembly_summary.txt");
	}

	public static void displayMultiAlign(ArrayList<Sequence> smallORFs, EPartService partService) {
		for (Sequence smallORF : smallORFs) {
			String fileNameTemp = Database.getTEMP_PATH() + "/" + smallORF.getName() + "/Summary-" + smallORF.getName()
					+ "-Alignment.fasta";
			ArrayList<String> listseq = TabDelimitedTableReader.readList(fileNameTemp);
			ArrayList<String> list = new ArrayList<>();
			for (int i = 0; i < listseq.size(); i++) {
				// System.out.println(">"+listseq.get(i));
				list.add(">" + listseq.get(i).split(" ")[0].trim());
				list.add(listseq.get(i).split(" ")[listseq.get(i).split(" ").length - 1].trim());
			}
			String fileName = Database.getTEMP_PATH() + "/" + smallORF.getName() + "/Summary-" + smallORF.getName()
					+ "-Alignment-Curate.fasta";
			TabDelimitedTableReader.saveList(list, fileName);
			// AlignmentView.displayMultiAlignment(fileName,smallORF.getName(),partService);

		}
	}

	public static void displayMultiAlign(EPartService partService) {
		String orfName = "Rli42_EGD-e";
		String fileName = Database.getTEMP_PATH() + "/" + orfName + "/Summary-" + orfName + "-Alignment.fasta";
		ArrayList<String> listseq = TabDelimitedTableReader.readList(fileName);
		TabDelimitedTableReader.saveList(listseq, fileName);
		// AlignmentView.displayMultiAlignment(fileName,orfName,partService);
	}

	/**
	 * reading filtered table and create fasta file, assuming that Lite.excel table
	 * have been reduced manually by removing doublons
	 */
	public static void createPhylogenyFile(ArrayList<Sequence> smallORFs) {
		for (Sequence smallORF : smallORFs) {
			String[][] summary = TabDelimitedTableReader.read(Database.getTEMP_PATH() + "/" + smallORF.getName()
					+ "/Summary-" + smallORF.getName() + "-Lite.excel");
			ArrayList<String> hitList = new ArrayList<String>();
			for (int i = 0; i < summary.length; i++) {
				hitList.add(">" + summary[i][1]);
				hitList.add(summary[i][0]);
			}
			TabDelimitedTableReader.saveList(hitList, Database.getTEMP_PATH() + "/" + smallORF.getName() + "/Summary-"
					+ smallORF.getName() + "-Sequences-Lite.fasta");
		}
	}

	/**
	 * Displaying the newly created tree with Archaeopteryx.
	 */
	public static void displayPhylogeny(ArrayList<Sequence> smallORFs) {
		for (Sequence smallORF : smallORFs) {
			String[][] summary = TabDelimitedTableReader.read(Database.getTEMP_PATH() + "/" + smallORF.getName()
					+ "/Summary-" + smallORF.getName() + "-Lite.excel");
			HashMap<String, String[]> infoNodes = new HashMap<>();
			for (int i = 1; i < summary.length; i++) {
				String[] row = { summary[i][0], summary[i][1] };
				String genomeNCBI = summary[i][1];
				if (!genomeNCBI.equals("")) {
					infoNodes.put(genomeNCBI, row);
				}
			}
			final Phylogeny[] phylogenies = { PhylogenyToolsJolley.readPhylogeny(
					Database.getTEMP_PATH() + "/" + smallORF.getName() + "/" + smallORF.getName() + ".nwk") };
			PhylogenyToolsJolley.addInfoToPhylogeny(phylogenies[0], infoNodes);
			Archaeopteryx.createApplication(phylogenies,
					Database.getANALYSIS_PATH() + "/Egd-e Annotation/_aptx_configuration_file.txt",
					"Phylogeny with forester");
		}
	}

	/**
	 * Remove specific files in genome folders
	 */
	public static void removeFiles() {
		String[][] genomeArray = TabDelimitedTableReader.read(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
		/*
		 * Copy fna files to temp folder
		 */
		for (int i = 1; i < genomeArray.length; i++) {
			String genomeName = genomeArray[i][1];
			String pathName = GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator;
			File pathGenome = new File(pathName);

			for (File file : pathGenome.listFiles()) {
				if (file.getAbsolutePath().contains(genomeName)) {
					file.delete();
				}
			}
		}
	}

	/**
	 * Read all Newick file from Firmicutes and extract the final list of
	 * genomes<br>
	 * Newick file were downloaded from PATRIC website
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private static ArrayList<String> getAllGenomesFromNewick() {
		/**
		 * Read Newick files and extract the list of all genomes
		 */
		ArrayList<String> genomes = new ArrayList<>();
		ArrayList<String> genomes1 = extractGenomesFromNewick(
				GenomeNCBIFolderTools.PATH_GENOMES_LIST + "Bacillales.nwk");
		for (String genome : genomes1)
			genomes.add(genome);
		ArrayList<String> genomes2 = extractGenomesFromNewick(
				GenomeNCBIFolderTools.PATH_GENOMES_LIST + "Clostridiales.nwk");
		for (String genome : genomes2)
			genomes.add(genome);
		ArrayList<String> genomes3 = extractGenomesFromNewick(
				GenomeNCBIFolderTools.PATH_GENOMES_LIST + "Lactobacillales.nwk");
		for (String genome : genomes3)
			genomes.add(genome);
		genomes.add("Macrococcus_caseolyticus_JCSC5402");
		genomes.add("Abiotrophia_defectiva_ATCC_49176");
		genomes.add("Streptococcus_sp._SK643");
		TabDelimitedTableReader.saveList(genomes, GenomeNCBIFolderTools.PATH_GENOMES_LIST + "AllGenomes.txt");
		return genomes;
	}

	/**
	 * Parse a Newick file and extract all genomes
	 * 
	 * @param fileName
	 * @return
	 */
	private static ArrayList<String> extractGenomesFromNewick(String fileName) {
		ArrayList<String> genomes = new ArrayList<>();
		String textFile = FileUtils.readText(fileName);
		String[] allGenomes = textFile.split(":");
		for (String genomeTemp : allGenomes) {
			if (genomeTemp.contains(",")) {
				String genome = genomeTemp.split(",")[1];
				if (!genome.equals("")) {
					int begin = genome.lastIndexOf('(') + 1;
					if (begin == -1)
						begin = 0;
					genome = genome.substring(begin);
					genomes.add(genome);
					// System.out.println(genome);
				}
				String number = genomeTemp.split(",")[0];
				// System.err.println(number);
			} else {
				// System.err.println(genomeTemp);
			}

		}
		return genomes;
	}

	/**
	 * Remove all files of the blast database
	 */
	@SuppressWarnings("unused")
	private static void cleanFolders() {
		final ArrayList<String> listBacteria = TabDelimitedTableReader
				.readList(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
		for (String genomeName : listBacteria) {
			File path = new File(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator);
			boolean found = false;
			for (File file : path.listFiles()) {
				String name = file.getAbsolutePath();
				if (name.contains(".phr") || name.contains(".pin") || name.contains(".psq")) {
					if (name.contains("smallORF")) {
						System.out.println(name);
						file.delete();
					}
					if (!name.contains("ORF")) {
						System.out.println(name);
						file.delete();
					}
				}

				if (name.contains(".faa") && !name.contains(".SmallORF")) {
					found = true;
				}
			}

			if (!found) {
				System.out.println(path.getAbsolutePath());
			}

			if (path.listFiles().length < 6) {
				System.err.println(path.getAbsolutePath());
			}
		}
	}

}
