package bacnet.scripts.listeriomics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import bacnet.Database;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

/**
 * Different methods to create phylogeny tree, and tables for each genome
 * elements
 * 
 * @author UIBC
 *
 */
public class PhylogenyListeriomics {

	public static String PATH = GenomeNCBI.PATH_PHYLOGENY;
	public static String LOCUSTAG_CHANGE_PATH = GenomeNCBI.PATH_PHYLOGENY + "LocusTagChangeForPhylogeny.txt";
	public static String NOTFOUND_LOCUSTAG_CHANGE_PATH = GenomeNCBI.PATH_PHYLOGENY + "NotFound_LocusTagChange.txt";
	public static String bigDataset = GenomeNCBI.PATH_PHYLOGENY + "blastp_all_vs_all.tab";

	public static void run() {
		// String before = "NZ_CM001052";
		// String after = "CM001052";
		// ArrayList<String> list =
		// TabDelimitedTableReader.readList("/Users/cbecavin/Downloads/Listeria_homology/"+before+".1_prot_vs_all.tab",true);
		// ArrayList<String> set = new ArrayList<>();
		// for(String temp : list){
		// //System.out.println(temp);
		// set.add(temp.replaceAll(before, after));
		// }
		// TabDelimitedTableReader.saveList(set,
		// "/Users/cbecavin/Downloads/Listeria_homology/"+after+".1_prot_vs_all.tab");

		// System.out.println(set.size());
		// getAlllocusTag();
		// getAlllocusTagINRA();

		// getGenomes();

		// confirmPresenceOfAllLocus();
		// searchMissingLocus();

		// parseData();
		//
		//
		// //findLocusTag("LMHCC_RS12815");
		// transformData();
		addPhylogenyToGenes();

		/*
		 * In SVG file get the sorted list of genomes and update Genome table
		 * accordingly!
		 */
		// cleanSVGFile();

	}

	/*
	 * Go through all BlastP datasets and verify that each locus can be found in
	 * Listeriomics genomes Save in a file the ocustag to take into account
	 */
	@SuppressWarnings("unused")
	private static void confirmPresenceOfAllLocus() {
		HashMap<String, String> idToGenomeFirstChromo = new HashMap<>();
		HashMap<String, String> idToGenomeAllChromo = new HashMap<>();
		String[][] arrayGenome = TabDelimitedTableReader.read(PATH + "listGenomes.excel");
		for (int i = 1; i < arrayGenome.length; i++) {
			if (arrayGenome[i][3].equals("TRUE")) {
				idToGenomeFirstChromo.put(arrayGenome[i][0], arrayGenome[i][1]);
			}
		}
		for (int i = 1; i < arrayGenome.length; i++) {
			idToGenomeAllChromo.put(arrayGenome[i][0], arrayGenome[i][1]);
		}

		// HashMap<String,String> oldLocusToLocusTag =
		// TabDelimitedTableReader.readHashMap(PATH+"LocusTagCorrection.txt");
		ArrayList<String> foundList = new ArrayList<>();
		ArrayList<String> notfoundList = new ArrayList<>();
		TreeSet<String> listGeneBlast = new TreeSet<>();
		for (String genomeId : idToGenomeFirstChromo.keySet()) {
			String genomeName = idToGenomeFirstChromo.get(genomeId);
			Genome genome = Genome.loadGenome(genomeName);

			for (String row : TabDelimitedTableReader
					.readList(PATH + "RawData" + File.separator + genomeId + "_prot_vs_all.tab", true)) {
				String gene = row.split("\t")[0].split(":")[1];
				String gene2 = row.split("\t")[1].split(":")[1];
				String genomeId2 = row.split("\t")[1].split(":")[0];
				if (genomeId2.contains("NZ_"))
					genomeId2 = genomeId2.replaceFirst("NZ_", "");
				if (genomeId2.equals("CP006593")) {
					genomeId2 = "NC_021837";
				}
				String genomeName2 = idToGenomeAllChromo.get(genomeId2);
				if (genomeName2 == null) {
					System.err.println("Cannot find: " + gene2 + "\t" + genomeId2);
				}
				// System.out.println(temp);
				listGeneBlast.add(gene + "\t" + genomeName);
				listGeneBlast.add(gene2 + "\t" + genomeName2);

			}
		}

		for (String row : listGeneBlast) {
			String gene = row.split("\t")[0];
			String genomeName = row.split("\t")[1];
			// System.out.println(genomeName);
			Genome genome = Genome.loadGenome(genomeName);
			Sequence seq = genome.searchElement(gene);
			if (seq == null) {
				foundList.add(gene + "\tNULL\t" + genomeName);
				notfoundList.add(gene + "\tNULL\t" + genomeName);
				System.out.println("Cannot find: " + gene);
			} else {
				foundList.add(gene + "\t" + seq.getName() + "\t" + genomeName);
			}
		}

		TabDelimitedTableReader.saveList(foundList, LOCUSTAG_CHANGE_PATH);
		TabDelimitedTableReader.saveList(notfoundList, NOTFOUND_LOCUSTAG_CHANGE_PATH);
	}

	/**
	 * By going through all GFF available search for the corresponding locustag
	 */
	public static void searchMissingLocus() {
		String[][] arrayGenome = TabDelimitedTableReader.read(NOTFOUND_LOCUSTAG_CHANGE_PATH);
		ArrayList<String> listResults = new ArrayList<>();
		for (int i = 0; i < arrayGenome.length; i++) {
			String locus = arrayGenome[i][0];
			String genomeName = arrayGenome[i][2];
			String found = findLocusTag(locus, genomeName);
			listResults.add(locus + "\t" + genomeName + "\t" + found);
		}
		TabDelimitedTableReader.saveList(listResults, NOTFOUND_LOCUSTAG_CHANGE_PATH + "_Corrected.txt");

	}

	/**
	 * Using LOCUSTAG_CHANGE_PATH modify every table by changin locutag ID or
	 * removing the row if the gene is not found
	 */
	public static void parseData() {
		File newPath = new File(PATH + "RawDataParsed");
		newPath.mkdir();

		HashMap<String, String> idToGenome = new HashMap<>();
		String[][] arrayGenome = TabDelimitedTableReader.read(PATH + "listGenomes.excel");
		for (int i = 1; i < arrayGenome.length; i++) {
			if (arrayGenome[i][3].equals("TRUE")) {
				idToGenome.put(arrayGenome[i][0], arrayGenome[i][1]);
			}
		}
		HashMap<String, String> oldLocusToLocusTag = TabDelimitedTableReader.readHashMap(LOCUSTAG_CHANGE_PATH);
		TreeSet<String> cannotFind = new TreeSet<>();
		for (String genomeId : idToGenome.keySet()) {
			ArrayList<String> newresultTable = new ArrayList<>();
			System.out.println(genomeId);
			ArrayList<String> bestHitList = new ArrayList<>();
			String locustag = "";
			for (String row : TabDelimitedTableReader
					.readList(PATH + "RawData" + File.separator + genomeId + "_prot_vs_all.tab", true)) {
				String locutagLeft = row.split("\t")[0].split(":")[1];
				String locutagRight = row.split("\t")[1].split(":")[1];
				String genomeIdRight = row.split("\t")[1].split(":")[0];
				/*
				 * Assuming the first hit in the list is always the one with the best score We
				 * only take the first hit of each genome vs genome : query comparison
				 */
				if (!locustag.equals(locutagLeft)) {
					locustag = locutagLeft;
					bestHitList.clear();
				}
				String bestHit = locutagLeft + "_" + genomeIdRight;
				if (!bestHitList.contains(bestHit)) {
					if (!oldLocusToLocusTag.containsKey(locutagLeft)) {
						cannotFind.add(locutagLeft + "\t" + row.split("\t")[0].split(":")[0]);
					} else if (!oldLocusToLocusTag.containsKey(locutagRight)) {
						cannotFind.add(locutagRight + "\t" + row.split("\t")[1].split(":")[0]);
					} else {
						String newLocusTagLeft = oldLocusToLocusTag.get(locutagLeft);
						String newLocusTagRight = oldLocusToLocusTag.get(locutagRight);
						if (!newLocusTagLeft.equals("NULL") && !newLocusTagRight.equals("NULL")) {
							String newRow = row.replaceAll(locutagLeft, newLocusTagLeft).replaceAll(locutagRight,
									newLocusTagRight);
							newresultTable.add(newRow);
						}
					}
					bestHitList.add(bestHit);
					if (bestHitList.size() % 5000 == 0) {
						System.out.println("Nb genes processed :" + bestHitList.size());
					}
				} else {
					// System.out.println("Not include: "+row);
				}
			}
			TabDelimitedTableReader.saveList(newresultTable,
					PATH + "RawDataParsed" + File.separator + genomeId + "_prot_vs_all.tab");
		}
		TabDelimitedTableReader.saveTreeSet(cannotFind, PATH + File.separator + "WasNotFound.txt");
	}

	/**
	 * For each genome save a list of genes and their homologs
	 */
	public static boolean transformData() {
		HashMap<String, String> idToGenome = new HashMap<>();
		// HashMap<String, String> idToGenome2 = new HashMap<>();
		// HashMap<String,String> badGenomeId = new HashMap<>();
		// badGenomeId.put("NZ_CM001469","NZ_CM001470");
		// badGenomeId.put("NZ_CP011398","NZ_CP011399");
		// badGenomeId.put("CP013724","CP013725");

		ArrayList<String> notFoundLocusTag = new ArrayList<>();
		HashMap<String, String> oldLocusToLocusTag = new HashMap<>();
		String[][] arrayGenome = TabDelimitedTableReader.read(PATH + "listGenomes.excel");
		for (int i = 1; i < arrayGenome.length; i++) {
			if (arrayGenome[i][3].equals("TRUE")) {
				idToGenome.put(arrayGenome[i][0], arrayGenome[i][1]);
			}
		}
		for (String genomeID : idToGenome.keySet()) {
			String genomeName = idToGenome.get(genomeID);
			HashMap<String, String> geneToHomologs = new HashMap<>();
			System.out.println(genomeID);
			String fileName = PATH + "RawDataParsed" + File.separator + genomeID + "_prot_vs_all.tab";
			System.out.println("Load " + PATH + "RawDataParsed" + File.separator + genomeID + "_prot_vs_all.tab");
			String[][] blastArray = TabDelimitedTableReader.read(fileName);
			for (int i = 0; i < blastArray.length; i++) {
				/*
				 * Add query sequence
				 */
				String geneQuery = blastArray[i][0].split(":")[1];
				if (!geneToHomologs.containsKey(geneQuery)) {
					String sequenceAAGeneQuery = "-";
					// System.out.println(genomeName+" "+geneQuery);
					Genome genome = Genome.loadGenome(genomeName);
					Sequence geneTargetObject = genome.getElement(geneQuery);
					if (geneTargetObject == null) {
						// System.out.println("Cannot find: "+geneQuery);
						if (oldLocusToLocusTag.containsKey(geneQuery)) {
							String oldGenQuery = geneQuery;
							geneQuery = oldLocusToLocusTag.get(geneQuery);
							geneTargetObject = genome.getElement(geneQuery);
							if (geneTargetObject == null) {
								notFoundLocusTag.add(geneQuery + "\t" + genomeName);
								System.err.println("Cannot find 2: " + geneQuery + "  " + oldGenQuery);
								// return false;
							} else {
								// sequenceAAGeneQuery = geneTargetObject.getSequenceAA();
								// System.out.println("Finally found: "+geneQuery);
							}
						} else {
							notFoundLocusTag.add(geneQuery + "\t" + genomeName);

							System.err.println("Cannot find 1: " + geneQuery);
							// return false;
						}
					}

					String infoQuery = genomeName + ";" + geneQuery + ";100;" + sequenceAAGeneQuery + ";;";
					geneToHomologs.put(geneQuery, infoQuery);
				}

				String idGenometarget = blastArray[i][1].split(":")[0];
				// if(badGenomeId.containsKey(idGenometarget)){
				// idGenometarget = badGenomeId.get(idGenometarget);
				// }
				String genomeTarget = idToGenome.get(idGenometarget);
				String geneTarget = blastArray[i][1].split(":")[1];
				String geneIdentity = blastArray[i][2];
				/*
				 * Get amino acid sequence of target gene
				 */
				String sequenceAAGeneTarget = "-";
				try {
					// System.out.println(genomeTarget+" "+geneTarget);
					Genome genome = Genome.loadGenome(genomeTarget);
					Sequence geneTargetObject = genome.getElement(geneTarget);
					if (geneTargetObject == null) {
						if (oldLocusToLocusTag.containsKey(geneTarget)) {
							@SuppressWarnings("unused")
							String oldGeneTarget = geneTarget;
							geneTarget = oldLocusToLocusTag.get(geneTarget);
							geneTargetObject = genome.getElement(geneTarget);
							if (geneTargetObject == null) {
								notFoundLocusTag.add(geneTarget + "\t" + genomeTarget);

								// System.err.println("2 - Cannot find 2: "+geneTarget+" "+oldGeneTarget);
								/// return false;
							} else {
								// sequenceAAGeneTarget = geneTargetObject.getSequenceAA();
								// System.out.println("Finally found: "+geneTarget);
							}
						} else {
							notFoundLocusTag.add(geneTarget + "\t" + genomeTarget);

							// System.err.println("2 - Cannot find 1 : "+idGenometarget+" "+geneTarget+"
							// "+genomeTarget+" "+genomeName);
							// return false;
						}
					} else {
						// sequenceAAGeneTarget = geneTargetObject.getSequenceAA();
					}
				} catch (Exception e) {
					// System.err.println("Cannot get sequence for:
					// "+blastArray[i][1].split(":")[0]+" "+geneTarget);
					notFoundLocusTag.add(geneTarget + "\t" + genomeTarget);
				}
				// System.out.println(idToGenome.get(genomeTarget)+" "+" "+geneQuery+"
				// "+geneTarget+"
				// "+sequenceAAGeneTarget);
				if (geneToHomologs.containsKey(geneQuery)) {
					String info = geneToHomologs.get(geneQuery);
					info += genomeTarget + ";" + geneTarget + ";" + geneIdentity + ";" + sequenceAAGeneTarget + ";;";
					geneToHomologs.put(geneQuery, info);
				} else {
					String info = genomeTarget + ";" + geneTarget + ";" + geneIdentity + ";" + sequenceAAGeneTarget
							+ ";;";
					geneToHomologs.put(geneQuery, info);
				}
				// TabDelimitedTableReader.saveList(notFoundLocusTag,
				// PATH+"NotFoundLocusTag.txt");
			}

			TabDelimitedTableReader.saveHashMap(geneToHomologs, PATH + "Summary/" + genomeName + ".excel");
			TabDelimitedTableReader.saveList(notFoundLocusTag, PATH + "NotFoundLocusTagInFinalFiles.txt");
		}

		return true;
	}

	public static void getAlllocusTagINRA() {
		HashMap<String, String> idToGenome = new HashMap<>();
		HashMap<String, String> idToGenome2 = new HashMap<>();
		HashMap<String, String> badGenomeId = new HashMap<>();
		badGenomeId.put("NZ_CM001469", "NZ_CM001470");
		badGenomeId.put("NZ_CP011398", "NZ_CP011399");
		badGenomeId.put("CP013724", "CP013725");

		ArrayList<String> locusTags = new ArrayList<>();
		String[][] arrayGenome = TabDelimitedTableReader.read(PATH + "listGenomes.excel");
		for (int i = 1; i < arrayGenome.length; i++) {
			if (arrayGenome[i][3].equals("TRUE")) {
				idToGenome.put(arrayGenome[i][0], arrayGenome[i][1]);
				idToGenome2.put(arrayGenome[i][2], arrayGenome[i][1]);
			}
		}
		for (String genomeID : idToGenome.keySet()) {
			String genomeName = idToGenome.get(genomeID);
			System.out.println(genomeID);
			String fileName = PATH + "RawData" + File.separator + genomeID + "_prot_vs_all.tab";
			System.out.println("Load " + PATH + "RawData" + File.separator + genomeID + "_prot_vs_all.tab");
			File file = new File(fileName);
			if (file.exists()) {
				String[][] blastArray = TabDelimitedTableReader.read(fileName);
				TreeSet<String> allLocus = new TreeSet<>();
				for (int i = 0; i < blastArray.length; i++) {
					String locusTag = blastArray[i][0].split(":")[1];
					allLocus.add(locusTag);
				}

				for (String locus : allLocus) {
					locusTags.add(genomeName + "\t" + genomeID + "\t" + locus);
				}
			}
		}

		TabDelimitedTableReader.saveList(locusTags, PATH + "AllLocusTagINRA.txt");
	}

	public static void getAlllocusTag() {
		HashMap<String, String> idToGenome = new HashMap<>();
		HashMap<String, String> idToGenome2 = new HashMap<>();
		HashMap<String, String> badGenomeId = new HashMap<>();
		badGenomeId.put("NZ_CM001469", "NZ_CM001470");
		badGenomeId.put("NZ_CP011398", "NZ_CP011399");
		badGenomeId.put("CP013724", "CP013725");

		ArrayList<String> locusTags = new ArrayList<>();
		String[][] arrayGenome = TabDelimitedTableReader.read(PATH + "listGenomes.excel");
		for (int i = 1; i < arrayGenome.length; i++) {
			if (arrayGenome[i][3].equals("TRUE")) {
				idToGenome.put(arrayGenome[i][0], arrayGenome[i][1]);
				idToGenome2.put(arrayGenome[i][2], arrayGenome[i][1]);
			}
		}
		for (String genomeID : idToGenome.keySet()) {
			String genomeName = idToGenome.get(genomeID);
			Genome genome = Genome.loadGenome(genomeName);

			for (String locus : genome.getFirstChromosome().getGenes().keySet()) {
				locusTags.add(genomeName + "\t" + genomeID + "\t" + locus);

			}
		}

		TabDelimitedTableReader.saveList(locusTags, PATH + "AllLocusTag.txt");
	}

	/**
	 * They used old_locus_tag for the conservation analysis, so we need to create a
	 * HashMap to have the equivalent GeneId old_locus_tag ---> locus_tag in the GFF
	 */
	public static void getAlllocusTagFromGFF() {
		ArrayList<String> listLocusTag = new ArrayList<>();
		for (String genomeName : Genome.getAvailableGenomes()) {
			File path = new File(GenomeNCBI.PATH_GENOMES + genomeName);
			for (File file : path.listFiles()) {
				if (file.getAbsolutePath().endsWith(".gff")) {
					ArrayList<String> gffList = TabDelimitedTableReader.readList(file.getAbsolutePath(), true);
					for (String line : gffList) {
						if (line.contains("gene") && line.contains("locus_tag")) {
							line = line.replaceAll("\"", "");
							String[] allFeatures = line.split(";");
							String locusTag = "";
							String oldLocusTag = "";
							for (String feature : allFeatures) {
								if (feature.startsWith("locus_tag")) {
									locusTag = feature.replaceFirst("locus_tag=", "");
								}
								if (feature.startsWith("old_locus_tag")) {
									oldLocusTag = feature.replaceFirst("old_locus_tag=", "");
								}
							}
							System.out.println(oldLocusTag + " --> " + locusTag);
							// listLocusTag.add(oldLocusTag+"\t"+locusTag);
							if (!locusTag.equals("")) {
								listLocusTag.add(genomeName + "\t" + locusTag);
							}
							// System.out.println(line);
						}
					}
				}
			}
		}
		TabDelimitedTableReader.saveList(listLocusTag, PATH + "AllLocusTag.txt");

	}

	public static void findLocusTag(String locus) {
		for (String genomeName : Genome.getAvailableGenomes()) {
			findLocusTag(locus, genomeName);
		}

	}

	/**
	 * Search a locustag in all GFF present for a genomeName
	 * 
	 * @param locus
	 * @param genomeName
	 */
	public static String findLocusTag(String locus, String genomeName) {
		File path = new File(GenomeNCBI.PATH_GENOMES + genomeName);
		for (File file : path.listFiles()) {
			if (file.getAbsolutePath().endsWith(".gff")) {
				ArrayList<String> gffList = TabDelimitedTableReader.readList(file.getAbsolutePath(), true);
				for (String line : gffList) {
					// System.out.println(line);
					if (line.contains(locus)) {
						// System.out.println(genomeName);
						// System.out.println(line);
						return "Old_GFF";
					}

				}
			}
		}

		path = new File(Database.getInstance().getPath() + "/NCBIGenomesNew/" + genomeName);
		// System.out.println(path.getAbsolutePath());
		for (File file : path.listFiles()) {
			if (file.getAbsolutePath().endsWith(".gff")) {
				ArrayList<String> gffList = TabDelimitedTableReader.readList(file.getAbsolutePath(), true);
				for (String line : gffList) {
					// System.out.println(line);
					if (line.contains(locus)) {
						// System.out.println("New: "+genomeName);
						// System.out.println("New: "+line);
						return "New_GFF";
					}

				}
			}

		}

		path = new File(GenomeNCBI.PATH_GENOMES + genomeName);
		// System.out.println(path.getAbsolutePath());
		for (File file : path.listFiles()) {
			if (file.getAbsolutePath().endsWith(".gff")) {
				ArrayList<String> gffList = TabDelimitedTableReader.readList(file.getAbsolutePath(), true);
				for (String line : gffList) {
					// System.out.println(line);
					if (line.contains(locus)) {
						// System.out.println("New: "+genomeName);
						// System.out.println("New: "+line);
						return "GenBank_GFF";
					}

				}
			}

		}

		return "Not_Found";

	}

	/**
	 * Clean SVG file to be able to replace strain name by homolog information<br>
	 * Names of strain are created in many different "Text table" in the SVG file
	 * which have to be removed
	 */
	public static void cleanSVGFile() {
		// ArrayList<String> listLines =
		// TabDelimitedTableReader.readList(PATH+"supertreeListeria50G.svg",true);
		// TreeSet<String> strainPresent = new TreeSet<String>();
		// ArrayList<String> newSVG = new ArrayList<String>();
		// for(String line : listLines){
		// if(line.contains("<text transform=") && !line.contains("Phylogenomic
		// distance")){
		// System.out.println(line);
		// String[] yep = line.split(">");
		// String nameStrain = "";
		// for(String temp : yep){
		// if(!temp.startsWith("<")){
		// temp = temp.replaceFirst("</tspan","");
		// nameStrain+=temp;
		// }
		// }
		// nameStrain = nameStrain.replaceAll("_"," ").trim();
		// System.out.println(nameStrain);
		// strainPresent.add(nameStrain);
		//
		// String newLine = line.substring(0, line.indexOf(">"))+"
		// font-family=\"\'MyriadPro-Regular\'\"
		// font-size=\"12\">"+nameStrain+"</text>";
		// newSVG.add(newLine);
		// System.out.println(newLine);
		// }else{
		// newSVG.add(line);
		// }
		// }
		// TabDelimitedTableReader.saveList(newSVG, PATH+"PhylogenyListeria50.svg");
		//

		/*
		 * Verify strain names
		 */
		String textSVG = FileUtils.readText(PATH + "PhylogenyListeria50.svg");
		for (String strain : Genome.getAvailableGenomes()) {
			if (!textSVG.contains(strain)) {
				System.err.println("Not found: " + strain);
			} else {
				System.out.println("Found :" + strain);
			}
		}
		FileUtils.saveText(textSVG,
				Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("PHYLO_GENOME"));
		// ImageExportUtilsSWT.saveSVGtoJPG(Database.LISTERIOMICS_GENOMES_Phylo_FIGURE_PATH_SVG,Database.LISTERIOMICS_GENOMES_Phylo_FIGURE_PATH_JPG,0.99f,450,0);

	}

	/**
	 * Go through all result files and create Conservation HadhMap for each Gene
	 */
	public static void addPhylogenyToGenes() {
		for (String genomeName : Genome.getAvailableGenomes()) {

			// String genomeName = Genome.EGDE_NAME;
			Genome genome = Genome.loadGenome(genomeName);
			String[][] blastArray = TabDelimitedTableReader.read(PATH + "Summary/" + genomeName + ".excel");
			for (int i = 0; i < blastArray.length; i++) {
				String locus = blastArray[i][0];
				String allInfo = blastArray[i][1];
				// System.out.println(locus);
				Gene gene = genome.getGeneFromName(locus);
				if (gene != null) {
					// System.out.println(gene.getName());
					String[] conservations = allInfo.split(";;");
					LinkedHashMap<String, String> conservationHashMap = new LinkedHashMap<>();
					for (String conservation : conservations) {
						String genomeTarget = conservation.split(";")[0];
						String geneTarget = conservation.split(";")[1] + ";" + conservation.split(";")[2] + ";";
						if (conservation.split(";").length == 4) {
							geneTarget += conservation.split(";")[3];
						} else {
							geneTarget += " ;";
						}
						// System.out.println(genomeTarget+" -----" + geneTarget);
						conservationHashMap.put(genomeTarget, geneTarget);
					}
					gene.setConservationHashMap(conservationHashMap);
					gene.setConservation(conservationHashMap.size());
					gene.save(Database.getGENOMES_PATH() + genomeName + "/Sequences/" + gene.getName());
					// XMLUtils.encodeToFile(gene,
					// Database.getGENOMES_PATH()+genomeName+"/Sequences/"+gene.getName()+".new.xml");

				}
			}
		}
	}

	/**
	 * get the list of genome IDs and corresponding genome Names<br>
	 * And verify if all genomes are here!
	 */
	public static void getGenomes() {
		/*
		 * Get genome IDS from the rawdata
		 */
		ArrayList<String> listGenomeIDs = TabDelimitedTableReader.readList(PATH + "listGenomeIDs.excel");

		ArrayList<String> arrayGenome = new ArrayList<>();
		arrayGenome.add("ChromoIDDataset\tGenomeName\tChromoID\tFoundInDataset\tLength\tDescription");
		for (String genomeName : Genome.getAvailableGenomes()) {
			Genome genome = Genome.loadGenome(genomeName);
			for (Chromosome chromo : genome.getChromosomes().values()) {
				String genomeID = chromo.getAccession().toString();
				genomeID = genomeID.substring(0, genomeID.indexOf('.'));
				if (genomeID.contains("NZ_"))
					genomeID = genomeID.replaceFirst("NZ_", "");
				boolean found = false;
				if (listGenomeIDs.contains(genomeID)) {
					listGenomeIDs.remove(genomeID);
					found = true;
				}
				System.out.println(genomeID);
				arrayGenome.add(genomeID + "\t" + genomeName + "\t" + chromo.getAccession() + "\t" + found + "\t"
						+ chromo.getLength() + "\t" + chromo.getDescription());
			}
		}
		System.out.println("Not found");
		for (String genomeId : listGenomeIDs) {
			System.out.println("not found:" + genomeId);
		}
		TabDelimitedTableReader.saveList(arrayGenome, PATH + "listGenomes.excel");

	}

}
