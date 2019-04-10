package bacnet.scripts.listeriomics.nterm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.template.AbstractSequence.AnnotationType;

import bacnet.Database;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequenceNCBI.GeneNCBITools;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

/**
 * Methods to look at overlapping elements between NCBI and RAST genomes
 * 
 * @author christophebecavin
 *
 */
public class RASToverlap {

	public static String PATH = Database.getANALYSIS_PATH() + "Egd-c annotation/RAST genome/";
	public static String PATH_Conservation = Database.getANALYSIS_PATH()
			+ "Egd-c annotation/Homolog/EGDc_Conservation.txt";
	public static String PATH_Conservation_Cutoff = Database.getANALYSIS_PATH()
			+ "Egd-c annotation/Homolog/EGDc_Conservation_cutoff.txt";

	public static void getOverlap(String nCBIGenome, String rASTGenome, String summaryFileName) {
		ArrayList<String> up100 = TabDelimitedTableReader
				.readList(PATH + "comparison/" + summaryFileName + "-sameStopDiffStartPlus100.txt");
		ArrayList<String> noMatch = TabDelimitedTableReader
				.readList(PATH + "comparison/" + summaryFileName + "-noMatch.txt");
		// ArrayList<String> diffStop =
		// TabDelimitedTableReader.readList(RASTannotation.PATH+"comparison/"+summaryFileName+"-diffStopDiffStart.txt");

		getOverlapList(up100, nCBIGenome, rASTGenome, summaryFileName + "-EGD-e-sameStopDiffStartPlus100");
		getOverlapList(noMatch, nCBIGenome, rASTGenome, summaryFileName + "-noMatch");
		// getOverlapList(diffStop, nCBIGenome, rASTGenome,
		// summaryFileName+"-diffStopdiffStart");

	}

	/**
	 * Read NCBI and RAST genomes and look at overlapping elements
	 * 
	 * @param genes
	 * @param nCBIGenome
	 * @param rASTGenome
	 * @param summaryFileName
	 */
	public static void getOverlapList(ArrayList<String> genes, String nCBIGenome, String rASTGenome,
			String summaryFileName) {
		// load both genomes
		GenomeNCBI genomeNCBI = GenomeNCBITools.loadGenome(nCBIGenome);
		GenomeNCBI genomeRAST = getGenome(rASTGenome);
		// initialize lists
		ArrayList<DNASequence> rast = genomeRAST.getCodingSequencesList(false);
		ArrayList<DNASequence> codingNCBI = genomeNCBI.getCodingSequencesList(false);
		ArrayList<DNASequence> noncodingNCBI = genomeNCBI.getNonCodingSequencesList(false);
		ArrayList<DNASequence> sRNAs = new ArrayList<DNASequence>();
		if (nCBIGenome.equals(Genome.EGDE_NAME)) {
			Genome genome = Genome.loadEgdeGenome();
			for (Srna sRNA : genome.getFirstChromosome().getsRNAs().values()) {
				DNASequence gElement = GeneNCBITools.convert(sRNA);
				sRNAs.add(gElement);
			}
		}

		String ret = "Overlap list\tEquivalent\tdescription\tfig family\tevidence\tOverlap1\tOverlap2\tOverlap3\n";
		for (String geneName : genes) {
			// get Gene
			System.out.println(geneName);
			DNASequence gene = genomeRAST.getGeneFromName(geneName);
			if (gene == null)
				gene = genomeNCBI.getGeneFromName(geneName);
			if (gene != null) {
				System.out.println(gene.getAccession());
				String equivalent = equivalent(gene.getAccession().toString(), false);
				ret += gene.getAccession() + "-" + gene.getBioBegin() + "-" + gene.getBioEnd() + "-" + gene.getLength()
						+ "\t" + equivalent + "\t" + GeneNCBITools.getProduct(gene) + "\t"
						+ GeneNCBITools.searchElement(gene, "Fig family : ") + "\t"
						+ GeneNCBITools.searchElement(gene, "Note : ") + "\t";

				for (DNASequence gElement : rast) {
					if (GeneNCBITools.isOverlap(gene, gElement)
							&& !gElement.getAccession().toString().equals(geneName)) {
						ret += gElement.getAccession() + "-" + gElement.getBioBegin() + "-" + gElement.getBioEnd()
								+ "-over-" + GeneNCBITools.overlap(gene, gElement);
						ret += "\t";
					}
				}

				for (DNASequence gElement : codingNCBI) {
					if (GeneNCBITools.isOverlap(gene, gElement)
							&& !gElement.getAccession().toString().equals(geneName)) {
						ret += gElement.getAccession() + "-" + gElement.getBioBegin() + "-" + gElement.getBioEnd()
								+ "-over-" + GeneNCBITools.overlap(gene, gElement);
						ret += "\t";
					}
				}
				for (DNASequence gElement : noncodingNCBI) {
					if (GeneNCBITools.isOverlap(gene, gElement)
							&& !gElement.getAccession().toString().equals(geneName)) {
						ret += gElement.getAccession() + "-" + gElement.getBioBegin() + "-" + gElement.getBioEnd()
								+ "-over-" + GeneNCBITools.overlap(gene, gElement);
						ret += "\t";
					}
				}
				if (nCBIGenome.equals(Genome.EGDE_NAME)) {
					for (DNASequence gElement : sRNAs) {
						if (GeneNCBITools.isOverlap(gene, gElement)
								&& !gElement.getAccession().toString().equals(geneName)) {
							ret += gElement.getAccession() + "-" + gElement.getBioBegin() + "-" + gElement.getBioEnd()
									+ "-over-" + GeneNCBITools.overlap(gene, gElement);
							ret += "\t";
						}
					}
				}
				ret += "\n";
			}
		}

		FileUtils.saveText(ret, PATH + "comparison/" + summaryFileName + "-overlap.txt");
	}

	/**
	 * Given a sequence name, it gives the conservation in the three different
	 * species, in a String[]:<br>
	 * 
	 * @param seqName seuqnece Name
	 * @param cutoff  if true use Table created by applying a cut-off
	 * @return String[] = { id in EGD-e, id in EGD-c, id in 10403S }
	 */
	public static String equivalent(String seqName, boolean cutoff) {
		String[][] conservationArray = new String[0][0];
		if (cutoff)
			conservationArray = TabDelimitedTableReader.read(PATH_Conservation_Cutoff);
		else
			conservationArray = TabDelimitedTableReader.read(PATH_Conservation);
		for (int i = 0; i < conservationArray.length; i++) {
			for (int j = 0; j < conservationArray[0].length; j++) {
				if (seqName.equals(conservationArray[i][j])) {
					String ret = "";
					for (String temp : ArrayUtils.getRow(conservationArray, i)) {
						if (!temp.equals(seqName))
							ret += temp + ",";
					}
					return ret;
				}
			}
		}
		return "";
	}

	public static void getEGDeOverlap() {
		getOverlap(Genome.EGDE_NAME, "RAST EGD-e", "EGD-e");
	}

	public static void getEGDcOverlap() {
		getOverlap("Listeria_monocytogenes_EGD_c", "RAST EGD-c", "EGD-c");
	}

	public static void get10403SOverlap() {
		getOverlap("Listeria_10403S", "RAST 10403S", "10403S");
		addEGDeFilter();
	}

	/**
	 * Read "10403S-noMatch-overlap.txt" and add a column indicating if the
	 * corresponding lmo gene is kept or not in "EGD-e-noMatch-decision.txt"
	 */
	public static void addEGDeFilter() {
		String[][] array = TabDelimitedTableReader.read(PATH + "comparison/EGD-e-noMatch-decision.txt");
		HashMap<String, String> nameTodecision = new HashMap<String, String>();
		for (int i = 0; i < array.length; i++) {
			String name = array[i][0].split("-")[0];
			nameTodecision.put(name, array[i][1]);
		}

		String[][] overlap = TabDelimitedTableReader.read(PATH + "comparison/10403S-noMatch-overlap.txt");
		String[] lmoDecision = new String[overlap.length];
		lmoDecision[0] = "EGDe decision";
		for (int i = 1; i < overlap.length; i++) {
			lmoDecision[i] = "";
			String[] equivalents = overlap[i][1].split(",");
			for (String equivalent : equivalents) {
				if (nameTodecision.containsKey(equivalent)) {
					lmoDecision[i] = nameTodecision.get(equivalent);
				}
			}
		}
		overlap = ArrayUtils.addColumn(overlap, lmoDecision);
		TabDelimitedTableReader.save(overlap, PATH + "comparison/10403S-noMatch-overlap-EGDeDecision.txt");

	}

	/**
	 * Read list of gene having same end position but different starting point
	 * 
	 * @param genome   name of the genome
	 * @param onlyRAST if true, read only gene coming from RAST automatic annotation
	 * @return
	 */
	public static ArrayList<String> getSameEndList(String genome, boolean onlyRAST) {
		ArrayList<String> allElement = TabDelimitedTableReader
				.readList(PATH + "comparison/List " + genome + " sameEnd.txt");
		if (onlyRAST) {
			ArrayList<String> finalList = new ArrayList<String>();
			for (String element : allElement) {
				if (element.contains("fig"))
					finalList.add(element);
			}
			return finalList;
		} else
			return allElement;

	}

	/**
	 * Read list of gene not having been associated (RAST and NCBI association)
	 * 
	 * @param genome   name of the genome
	 * @param onlyRAST if true, read only gene coming from RAST automatic annotation
	 * @return
	 */
	public static ArrayList<String> getNotAssociatedList(String genome, boolean onlyRAST) {
		ArrayList<String> allElement = TabDelimitedTableReader
				.readList(PATH + "comparison/List " + genome + " notAssoc.txt");
		if (onlyRAST) {
			ArrayList<String> finalList = new ArrayList<String>();
			for (String element : allElement) {
				if (element.contains("fig"))
					finalList.add(element);
			}
			return finalList;
		} else
			return allElement;

	}

	/**
	 * Read the list of RAST elements totally unknown and create a list of Sequence
	 * from it
	 * 
	 * @param genome
	 * @param name
	 * @return
	 */
	public static ArrayList<Sequence> getUnknownRAST(String genome, String name) {
		ArrayList<Sequence> seqs = new ArrayList<Sequence>();
		ArrayList<String> listSeq = TabDelimitedTableReader.readList(PATH + "comparison/" + name + " unknown.txt");
		int i = 0;
		for (String seq : listSeq) {
			String[] infos = seq.split("-");
			int begin = Integer.parseInt(infos[1]);
			int end = Integer.parseInt(infos[2]);
			char strand = '+';
			if (end < begin)
				strand = '-';
			Sequence seqNew = new Sequence("rastlmo" + i, begin, end, strand);
			seqNew.setRef("RAST");
			seqNew.setId(infos[0]);
			seqNew.setGenomeName(Genome.EGDE_NAME);
			seqNew.setChromosomeID(Genome.EGDE_CHROMO_NAME);
			seqs.add(seqNew);
			i++;
		}

		return seqs;
	}

	public static ArrayList<Sequence> getEGDeUnknownRAST() {
		return getUnknownRAST(Genome.EGDE_NAME, "EGD-e");
	}

	/**
	 * Go through EGDe overlap and find when two genes overlap
	 */
	public static void getEGDeAutoOverlap() {
		GenomeNCBI genome = GenomeNCBITools.loadEgdeGenome();
		ArrayList<String> results = new ArrayList<String>();
		String ret = "Gene1\tGene2\toverlap";
		results.add(ret);
		ArrayList<String> finalList = TabDelimitedTableReader
				.readList(PATH + "comparison/" + "EGD-e genes with diff start and overlap.txt");
		int k = 0;
		for (DNASequence seq : genome.getCodingSequencesList(true)) {
			for (DNASequence seq2 : genome.getCodingSequencesList(true)) {
				if (!seq.equals(seq2) && GeneNCBITools.isOverlap(seq, seq2)) {
					if (k == 1) {
						k = 0; // if k==1 it means last step an overlap was found so we need to skip this step
								// otherwise we will have a doublon
					} else {
						String found = "";
						if (finalList.contains(seq.getAccession().toString()))
							found = seq.getAccession().toString();
						if (finalList.contains(seq2.getAccession().toString()))
							found = seq2.getAccession().toString();
						results.add(seq.getAccession().toString() + "\t" + seq2.getAccession().toString() + "\t"
								+ GeneNCBITools.overlap(seq, seq2) + "\t" + found);
						k = 1;
					}
				}
			}
		}

		TabDelimitedTableReader.saveList(results, PATH + "comparison/" + "EGD-e autoOverlap.txt");
	}

	/**
	 * Find RASt element which overlap another element in the genome
	 */
	public static void getEGDeRASTAutoOverlap() {
		GenomeNCBI genome = getEGDe();
		ArrayList<String> results = new ArrayList<String>();
		String ret = "Gene1\tGene2\toverlap";
		results.add(ret);
		ArrayList<String> finalList = TabDelimitedTableReader
				.readList(PATH + "comparison/" + "EGD-e genes with diff start and overlap.txt");
		int k = 0;
		for (DNASequence seq : genome.getCodingSequencesList(true)) {
			for (DNASequence seq2 : genome.getCodingSequencesList(true)) {
				if (!seq.equals(seq2) && GeneNCBITools.isOverlap(seq, seq2)) {
					if (k == 1) {
						k = 0; // if k==1 it means last step an overlap was found so we need to skip this step
								// otherwise we will have a doublon
					} else {
						String found = "";
						if (finalList.contains(seq.getAccession().toString()))
							found = seq.getAccession().toString();
						if (finalList.contains(seq2.getAccession().toString()))
							found = seq2.getAccession().toString();
						results.add(seq.getAccession().toString() + "\t" + seq2.getAccession().toString() + "\t"
								+ GeneNCBITools.overlap(seq, seq2) + "\t" + found);
						k = 1;
					}
				}
			}
		}
		TabDelimitedTableReader.saveList(results, PATH + "comparison/" + "RAST EGD-e autoOverlap.txt");
	}

	/**
	 * Read RAST automoticaly annotated EGD-e genome
	 * 
	 * @return
	 */
	public static GenomeNCBI getEGDe() {
		return getGenome("RAST EGD-e");
	}

	/**
	 * Read a genome from RAST results
	 * 
	 * @param genomeName
	 * @return
	 */
	public static GenomeNCBI getGenome(String genomeName) {
		// create genome
		GenomeNCBI genome = new GenomeNCBI();
		LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes = new LinkedHashMap<String, ChromosomeBacteriaSequence>();
		/*
		 * Get sequence
		 */
		if (genomeName.equals("EGD-e"))
			System.err.println("sdfh " + genomeName);
		ChromosomeBacteriaSequence chromosome = new ChromosomeBacteriaSequence();
		chromosomes.put("RAST_Chromosome", chromosome);
		genome.setChromosomes(chromosomes);

		// add info from RAST result table
		String[][] rastResults = new String[0][0];
		rastResults = TabDelimitedTableReader.read(PATH + genomeName + ".txt");
		for (int i = 1; i < rastResults.length; i++) {
			String locusTag = rastResults[i][0];
			// System.out.println(locusTag);
			DNASequence seq = new DNASequence(rastResults[i][10]);
			seq.setAccession(new AccessionID(locusTag));

			int begin = Integer.parseInt(rastResults[i][3]);
			int end = Integer.parseInt(rastResults[i][4]);
			seq.setBioBegin(begin);
			seq.setBioEnd(end);
			seq.setDescription(rastResults[i][6]);
			seq.setAnnotationType(AnnotationType.PREDICTED);
			seq.addNote("Type : " + rastResults[i][2]);
			seq.addNote("RAST id : " + rastResults[i][1]);
			seq.addNote("Strand : " + rastResults[i][5]);
			seq.addNote("product : " + rastResults[i][6]);
			seq.addNote("note : " + rastResults[i][9]);
			seq.addNote("Fig family : " + rastResults[i][8]);

			// add newly created DNASequence to the chromosome
			seq.setParentSequence(chromosome);

			// if it is an ncRNA put it also in NonSequencelist
			if (rastResults[i][2].equals("rna")) {
				chromosome.getNoncodingSequenceHashMap().put(locusTag, seq);
			} else {
				chromosome.getCodingSequenceHashMap().put(locusTag, seq);
			}
			// System.out.println(GeneTools.toStringCodingInfo(seq));
		}

		return genome;
	}

}
