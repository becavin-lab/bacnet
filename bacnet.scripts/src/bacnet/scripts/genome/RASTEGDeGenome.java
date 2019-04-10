package bacnet.scripts.genome;

import java.util.ArrayList;
import java.util.HashMap;

import bacnet.Database;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequenceNCBI.GenomeConversion;
import bacnet.reader.TabDelimitedTableReader;

/**
 * List of methods for adding RAST genes to EGD-e annotation
 * 
 * @author UIBC
 *
 */
public class RASTEGDeGenome {

	public static void run() {
		Genome genome = Genome.loadEgdeGenome();
		String genomePath = Database.getGENOMES_PATH() + Genome.EGDE_NAME;
		HashMap<String, String> oldRASTtoNewRAST = new HashMap<>();
		/*
		 * Load all RAST genes
		 */
		String[][] array = TabDelimitedTableReader.read(GenomeNCBI.PATH_ANNOTATION + "RAST_genome/RAST-EGD-e.txt");
		ArrayList<Gene> genesRAST = new ArrayList<>();
		for (int i = 1; i < array.length; i++) {
			if (array[i][2].equals("peg")) {
				int begin = Integer.parseInt(array[i][4]);
				int end = Integer.parseInt(array[i][5]);
				char strand = array[i][6].charAt(0);
				String product = array[i][7];
				String figFam = array[i][9];
				String evidence = array[i][10];
				String name = "RAST_lmo" + i;
				String oldName = array[i][0];
				Gene geneRAST = new Gene(name, begin, end, strand);
				geneRAST.setProduct(product);
				geneRAST.getFeatures().put("RAST_FigFamily", figFam);
				geneRAST.getFeatures().put("RAST_evidence_codes", evidence);
				genesRAST.add(geneRAST);
			}
		}

		/*
		 * Get Only ORFs with a different position, or new ORFs from RAST
		 */
		String[][] arrayDiffORFs = TabDelimitedTableReader
				.read(GenomeNCBI.PATH_ANNOTATION + "RAST_genome/DiffOrfs_RAST_062014.txt");
		ArrayList<Gene> genesRASTtoAdd = new ArrayList<>();
		for (int i = 1; i < arrayDiffORFs.length; i++) {
			int begin = Integer.parseInt(arrayDiffORFs[i][1]);
			int end = Integer.parseInt(arrayDiffORFs[i][2]);
			if (begin > end) {
				int temp = begin;
				begin = end;
				end = temp;
			}
			char strand = arrayDiffORFs[i][4].charAt(0);
			String oldRASTname = arrayDiffORFs[i][0];
			String overlapLmo = arrayDiffORFs[i][6];
			String relativePosition = arrayDiffORFs[i][5];
			boolean found = false;
			for (Gene geneRAST : genesRAST) {
				if (geneRAST.getBegin() == begin && geneRAST.getEnd() == end && geneRAST.getStrand() == strand) {
					found = true;
					oldRASTtoNewRAST.put(oldRASTname, geneRAST.getName());
					geneRAST.setComment("RASTgene overlap: " + overlapLmo + " with relative position: "
							+ relativePosition + " (aa)");
					genesRASTtoAdd.add(geneRAST);
					Gene geneNCBI = genome.getGeneFromName(overlapLmo);
					if (geneNCBI != null) {
						geneRAST.getFeatures().put("NCBI_Overlap",
								geneNCBI.getName() + " - " + geneNCBI.getProduct() + " - " + geneNCBI.getComment());
						geneRAST.getFeatures().put("NCBI_Overlap Features", geneNCBI.getFeaturesText());
						geneNCBI.getFeatures().put("RAST_Overlap",
								relativePosition + " (aa) with " + geneRAST.getName());
						geneNCBI.getFeatures().put("RAST_Locus", geneRAST.getName());
						geneNCBI.getFeatures().put("RAST_Product", geneRAST.getProduct());
						geneNCBI.getFeatures().put("RAST_FigFamily", geneRAST.getFeature("RAST_FigFamily"));
						geneNCBI.getFeatures().put("RAST_evidence_codes", geneRAST.getFeature("RAST_evidence_codes"));
						geneNCBI.save(genomePath + "/Sequences/" + geneNCBI.getName());
					}
				}
			}
			if (!found) {
				System.err.println("Did not found: " + oldRASTname + "-" + begin + "-" + end + "-" + strand);

			}
		}

		/*
		 * Add new ORFs
		 */
		String[][] arrayNewORFs = TabDelimitedTableReader
				.read(GenomeNCBI.PATH_ANNOTATION + "RAST_genome/NewOrfs_RAST_062014.txt");
		for (int i = 1; i < arrayNewORFs.length; i++) {
			int begin = Integer.parseInt(arrayNewORFs[i][1]);
			int end = Integer.parseInt(arrayNewORFs[i][2]);
			if (begin > end) {
				int temp = begin;
				begin = end;
				end = temp;
			}
			char strand = arrayNewORFs[i][4].charAt(0);
			String oldRASTname = arrayNewORFs[i][0];
			String note = arrayNewORFs[i][5];
			boolean found = false;
			for (Gene geneRAST : genesRAST) {
				if (geneRAST.getBegin() == begin && geneRAST.getEnd() == end && geneRAST.getStrand() == strand) {
					oldRASTtoNewRAST.put(oldRASTname, geneRAST.getName());
					found = true;
					geneRAST.getFeatures().put("RAST_Locus", geneRAST.getName());
					geneRAST.setComment("RAST new ORFs : " + note);
					genesRASTtoAdd.add(geneRAST);
				}
			}
			if (!found) {
				System.err.println("Did not found: " + oldRASTname + "-" + begin + "-" + end + "-" + strand);

			}
		}

		/*
		 * For gene with same position add the annotation from RAST
		 */
		String[][] arraySameORFs = TabDelimitedTableReader
				.read(GenomeNCBI.PATH_ANNOTATION + "RAST_genome/Same_RAST_062014.txt");
		ArrayList<Gene> modifyGene = new ArrayList<>();
		for (int i = 1; i < arraySameORFs.length; i++) {
			String lmo = arraySameORFs[i][1];
			String oldRASTname = arraySameORFs[i][0];
			Gene geneNCBI = genome.getGeneFromName(lmo);
			if (geneNCBI != null) {
				for (Gene geneRAST : genesRAST) {
					if (geneRAST.getBegin() == geneNCBI.getBegin() && geneRAST.getEnd() == geneNCBI.getEnd()
							&& geneRAST.getStrand() == geneNCBI.getStrand()) {
						oldRASTtoNewRAST.put(oldRASTname, geneRAST.getName());
						geneNCBI.getFeatures().put("RAST_Locus", geneRAST.getName());
						geneNCBI.getFeatures().put("RAST_Product", geneRAST.getProduct());
						if (geneNCBI.getProduct().equals("hypothetical protein")) {
							System.out.println(geneNCBI.getName() + " " + geneNCBI.getProduct());
							geneNCBI.setProduct(
									geneNCBI.getProduct() + " - " + "RAST_Product: " + geneRAST.getProduct());
						}
						geneNCBI.getFeatures().put("RAST_FigFamily", geneRAST.getFeature("RAST_FigFamily"));
						geneNCBI.getFeatures().put("RAST_evidence_codes", geneRAST.getFeature("RAST_evidence_codes"));
						geneNCBI.save(genomePath + "/Sequences/" + geneNCBI.getName());
						// System.out.println("save: "+gene.getProduct()+" "+geneNCBI.getName()+"
						// "+genomePath+"/Sequences/"+geneNCBI.getName());
						modifyGene.add(geneRAST);
					}
				}
			}
		}

		TabDelimitedTableReader.saveHashMap(oldRASTtoNewRAST,
				GenomeNCBI.PATH_ANNOTATION + "RAST_genome/New_RAST_ID.txt");
		/*
		 * Verify
		 */
		int found = 0;
		for (Gene geneRAST : genesRAST) {
			if (genesRASTtoAdd.contains(geneRAST) || modifyGene.contains(geneRAST)) {
				found++;
			} else {
				System.out.println("Not found");
				System.out.println(geneRAST.toString());
				System.out.println(geneRAST.getComment());
			}

		}
		System.out.println("Modify: " + found + " genes on " + genesRAST.size());

		/*
		 * Add to EGD-e genome
		 */
		modifyGenome(genome, genesRASTtoAdd, genomePath);
	}

	private static void modifyGenome(Genome genome, ArrayList<Gene> genesRASTtoAdd, String genomePath) {
		for (Gene gene : genesRASTtoAdd) {
			gene.setGenomeName(genome.getSpecies());
			gene.setChromosomeID(Genome.EGDE_CHROMO_NAME);
			genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getGenesAlternative().put(gene.getName(), gene);
			gene.save(genomePath + "/Sequences/" + gene.getName());
		}

		for (Gene gene : genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getGenes().values()) {
			if (!gene.getFeaturesText().contains("RAST_Product")) {
				gene.getFeatures().put("RAST_Information", "Not Found by RAST automatic annotation software");
				gene.save(genomePath + "/Sequences/" + gene.getName());
			}
		}
		// put all elements in a LinkedHashMap<String, Sequence> allElements to create
		// Annotation
		GenomeConversion.updateAllElements(genome);
		// create Annotation
		GenomeConversion.createAnnotation(genome, genomePath);
	}

	/**
	 * Transform a list of old RAST ids into new RAST id
	 */
	public static void getNewRASTids() {
		ArrayList<String> listRAST = TabDelimitedTableReader
				.readList(GenomeNCBI.PATH_ANNOTATION + "RAST_genome/list.txt");
		HashMap<String, String> hashmap = TabDelimitedTableReader
				.readHashMap(GenomeNCBI.PATH_ANNOTATION + "RAST_genome/New_RAST_ID.txt");
		ArrayList<String> result = new ArrayList<>();
		for (int i = 0; i < listRAST.size(); i++) {
			String rastOld = listRAST.get(i);
			if (hashmap.containsKey(rastOld)) {
				result.add(hashmap.get(rastOld) + "\t" + rastOld);
			} else {
				System.out.println("did not found: " + rastOld);
			}
		}
		TabDelimitedTableReader.saveList(result, GenomeNCBI.PATH_ANNOTATION + "RAST_genome/list_New.txt");

	}
}
