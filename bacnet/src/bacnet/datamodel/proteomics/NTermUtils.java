package bacnet.datamodel.proteomics;

import java.io.File;
import java.util.ArrayList;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.Strand;

import bacnet.Database;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.sequence.Codon;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.UNAfold;

public class NTermUtils {

	public static String getPATH() {
		return Database.getInstance().getPath() + "N-TermSVN" + File.separator;
	}

	/**
	 * From a list of peptide position retrieve the complete sequence of the ORF (in
	 * frame) containing the peptide (amino acid sequence) <br>
	 * WARNING: Be careful that your peptide info (begin,end) are correct. Test
	 * (begin-end+1)/3 = an integer, meaning that it is actually the sequence of a
	 * amino acid sequence.<br>
	 * If one or two nucleotides are included where they should not be, the
	 * algorithm will not work.
	 * 
	 * @param fileName
	 */
	public static void getListOfFullORFfromPeptide(String fileName, Genome genome) {
		String[][] array = TabDelimitedTableReader.read(fileName);
		ArrayList<String> results = new ArrayList<>();
		String header = "";
		for (String cell : array[0])
			header += cell + "\t";
		header += "ORF begin\tORF end\tORF surrounding from Start to Stop";
		results.add(header);
		for (int i = 1; i < array.length; i++) {
			int begin = Integer.parseInt(array[i][1]);
			int end = Integer.parseInt(array[i][2]);
			char strand = array[i][3].charAt(0);
			Sequence sequence = new Sequence("Nterm", begin, end, strand);
			Sequence orf = getFullORF(sequence, genome);
			String row = "";
			for (String cell : array[i])
				row += cell + "\t";
			row += orf.getBegin() + "\t" + orf.getEnd() + "\t" + orf.getSequenceAA();
			results.add(row);
		}

		TabDelimitedTableReader.saveList(results, fileName + "_result.txt");
	}

	/**
	 * Giving a position and a strand on the genome we extract the full ORF (in
	 * frame) containing the peptide (amino acid sequence) <br>
	 * 
	 * @param seq
	 * @param genome
	 * @return
	 */
	public static Sequence getFullORF(Sequence seq, Genome genome) {
		return getFullORF(seq.getSequenceAA(), seq.getBegin(), seq.getEnd(), seq.isStrand(), genome);
	}

	/**
	 * Giving a position and a strand on the genome we extract the full ORF (in
	 * frame) containing the peptide (amino acid sequence) <br>
	 * 
	 * @param peptideSeq
	 * @param begin
	 * @param strand
	 */
	public static Sequence getFullORF(String peptideSeq, int beginPeptide, int endPeptide, boolean strand,
			Genome genome) {
		int end = -1;
		int begin = -1;
		// First search the next stop codon
		Strand strandNCBI = Strand.POSITIVE;
		if (!strand) {
			strandNCBI = Strand.NEGATIVE;
			boolean found = false;
			for (int i = beginPeptide; !found; i = i - 3) {
				String codon = genome.getFirstChromosome().getSequenceAsString(i, i + 2, strandNCBI);
				if (Codon.isStop(codon)) {
					found = true;
					end = i;
				}
			}
		} else {
			boolean found = false;
			for (int i = endPeptide; !found; i = i + 3) {
				String codon = genome.getFirstChromosome().getSequenceAsString(i - 2, i, strandNCBI);
				// System.out.println(i);
				// System.out.println(codon);
				// System.out.println(Codon.getAminoAcid(codon));
				if (Codon.isStop(codon)) {
					found = true;
					end = i;
				}
			}
		}

		// Then start codon
		if (!strand) {
			strandNCBI = Strand.NEGATIVE;
			boolean found = false;
			for (int i = endPeptide; !found; i = i + 3) {
				String codon = genome.getFirstChromosome().getSequenceAsString(i - 2, i, strandNCBI);
				if (Codon.isStart(codon)) {
					found = true;
					begin = i;
				}
			}
		} else {
			boolean found = false;
			for (int i = beginPeptide; !found; i = i - 3) {
				String codon = genome.getFirstChromosome().getSequenceAsString(i, i + 2, strandNCBI);
				if (Codon.isStart(codon)) {
					found = true;
					begin = i;
				}
			}
		}

		// System.out.println(begin+" "+end+" "+strand);
		Sequence sequence = new Sequence("ORF", begin, end + 2, '+');
		if (!strand)
			sequence = new Sequence("ORF", end - 2, begin, '-');
		if (sequence.getSequenceAA().contains(peptideSeq)) {
			System.out.println("ORF contain the sequence: " + peptideSeq);
		} else {
			System.out.println("ORF does not contain the sequence: " + peptideSeq);
		}
		System.out.println(sequence.getBegin() + "  " + sequence.getEnd() + "  " + sequence.getStrand() + "  "
				+ sequence.getSequenceAA() + "   " + peptideSeq);
		// System.out.println((double)sequence.getLength()/3);
		return sequence;
	}

	/**
	 * NOT FINISHED <br>
	 * <br>
	 * Search for the different start codon before a peptide, and the stop codon
	 * after this peptide<br>
	 * Print the nucleotide sequence plus the codon, and identify Start, STOp and
	 * the reference Peptide used
	 */
	public static void getStartCodonBefore() {
		Sequence nTerm = new Sequence("nTerm", 643935, 643961);
		Sequence allNterm = new Sequence("allNterm", 643800, 643979);
		// Sequence allNterm = new Sequence("allNterm", 652414, 652370);

		String finalResult = "";
		boolean found = false;
		for (int i = 0; i < allNterm.getSequence().length(); i++) {
			String codon = allNterm.getSequence().substring(i, i + 3);
			if (!found) {
				String seq = allNterm.getSequence().substring(i, i + nTerm.getLength());
				// System.out.println(seq);
				if (seq.equals(nTerm.getSequence())) {
					System.out.println("found");
					finalResult += "-PEPTIDE-";
					found = true;
				}
			}
			finalResult += codon + "(" + Codon.getAminoAcid(codon);
			if (Codon.isStart(codon))
				finalResult += "-Start";
			if (Codon.isStop(codon))
				finalResult += "-Stop";
			finalResult += ")";
			i++;
			i++;
			// System.out.println(i+" "+allNterm.getSequence().length());
		}
		// System.out.println(nTerm.getSequenceAA());
		// System.out.println(finalResult);
		//
		// System.out.println(nTerm.getSequence());
	}

	/**
	 * Get the next stop codon in frame and dowstream to a bpPosition
	 * 
	 * @param genome
	 * @param bpPosition
	 * @param strand
	 * @return
	 */
	public static String searchStopCodon(Genome genome, int bpPosition, boolean strand) {
		Strand strandNCBI = Strand.POSITIVE;
		if (!strand) {
			strandNCBI = Strand.NEGATIVE;
			String seq = genome.getFirstChromosome().getSequenceAsString(1, bpPosition, strandNCBI);
			DNASequence seqDNA = new DNASequence(seq);
			String seqAA = seqDNA.getRNASequence().getProteinSequence().getSequenceAsString();
			if (seqAA.indexOf("*") != -1) {
				// System.out.println("size: "+seqAA.indexOf("*"));
				seqAA = seqAA.substring(0, seqAA.indexOf("*"));
			}
			return seqAA;
		} else {
			String seq = genome.getFirstChromosome().getSequenceAsString(bpPosition,
					genome.getFirstChromosome().getLength(), strandNCBI);
			DNASequence seqDNA = new DNASequence(seq);
			String seqAA = seqDNA.getRNASequence().getProteinSequence().getSequenceAsString();
			if (seqAA.indexOf("*") != -1) {
				// System.out.println("size: "+seqAA.indexOf("*"));
				seqAA = seqAA.substring(0, seqAA.indexOf("*"));
			}
			return seqAA;
		}
	}

	/**
	 * Count the number of peptides with an R (arginine), and finishing with an R
	 * 
	 * @param massSpecName
	 */
	public static void countRTerminal(String massSpecName) {
		NTermData massSpec = NTermData.load(massSpecName);
		int endWithR = 0;
		int containsR = 0;
		int noR = 0;
		for (NTerm nTerm : massSpec.getElements().values()) {
			String sequence = nTerm.getSequencePeptide();
			if (sequence.endsWith("R")) {
				endWithR++;
			} else if (sequence.contains("R")) {
				System.out.println(sequence);
				containsR++;
			} else
				noR++;
		}
		System.err.println("Nb peptides:" + massSpec.getElements().size() + " finish with R: " + endWithR
				+ " contain an R, but not at the end: " + containsR + " contain no R: " + noR);
	}

	/**
	 * Return the list of NTerm common to both experiment<br>
	 * NTerm are equal if:<br>
	 * <li>same peptide sequence
	 * <li>same modification
	 * 
	 * @param NTermDataName1
	 * @param NTermDataName2
	 * @return
	 */
	public static ArrayList<NTerm> compareNTermData(String NTermDataName1, String NTermDataName2) {
		NTermData massSpec1 = NTermData.load(NTermDataName1);
		NTermData massSpec2 = NTermData.load(NTermDataName2);
		ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
		for (NTerm nTerm1 : massSpec1.getElements().values()) {
			for (NTerm nTerm2 : massSpec2.getElements().values()) {
				if (nTerm1.equals(nTerm2) && !nTerms.contains(nTerm1)) {
					nTerms.add(nTerm1);
				}
			}
		}

		int[] numberTypeOverlap = new int[massSpec1.getTypeOverlaps().size()];
		int i = 0;
		for (String typeOverlap : massSpec1.getTypeOverlaps()) {
			for (NTerm nTerm : nTerms) {
				String type = nTerm.getTypeOverlap();
				if (type.equals(typeOverlap)) {
					numberTypeOverlap[i] = numberTypeOverlap[i] + 1;
				}
			}
			i++;
		}
		String result = "Common NTerm: " + nTerms.size();
		i = 0;
		for (String typeOverlap : massSpec1.getTypeOverlaps()) {
			result += " " + typeOverlap + ": " + numberTypeOverlap[i];
			i++;
		}
		massSpec1.getStat();
		massSpec2.getStat();
		System.out.println(result);
		return nTerms;
	}

	/**
	 * Find SD binding free energy on region -20bp upstream to each lmo
	 * 
	 * @param nameNTermData
	 */
	public static void findSDSequenceforLMO() {
		Genome egde = Genome.loadEgdeGenome();
		/*
		 * Calculate anti-sd sequence binding freeEnergy
		 */
		ArrayList<String> lmos = new ArrayList<String>();
		for (Sequence seq : egde.getFirstChromosome().getAllElements().values()) {
			if (seq.getName().contains("rli")) {
				double energy = UNAfold.hybridRNA(seq.getSDSequence(), seq.getName(), Sequence.ANTI_SD_SEQ, "anti-SD",
						false);
				System.out.println(seq.getName() + "\t" + energy);
				lmos.add(seq.getName() + "\t" + energy);
			}
		}
		TabDelimitedTableReader.saveList(lmos, getPATH() + "Shine-Dalgarno Energy for rlis.txt");
	}

}
