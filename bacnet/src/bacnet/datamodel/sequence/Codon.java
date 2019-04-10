package bacnet.datamodel.sequence;

import java.util.TreeSet;

import org.biojava3.core.sequence.DNASequence;

import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

public class Codon {

	public static String[][] startCodon = { { "ATG", "M" }, { "TTG", "L" }, { "CTG", "L" }, { "ATT", "I" },
			{ "ATC", "I" }, { "ATA", "I" }, { "GTG", "V" } };
	// public static String[][] startCodon = {{"ATG","M"},{"TTG","L"},{"GTG","V"}};
	// public static String[][] startCodon = {{"ATG","M"}};

	public static String[][] aminoacidCharge = { { "R", "1" }, { "H", "1" }, { "K", "1" }, { "D", "-1" },
			{ "E", "-1" } };

	/**
	 * Using aminoacidCharge array, return the value of the charge for each amino
	 * acid aminoacidCharge = {{"R","1"},{"H","1"},{"K","1"},{"D","-1"},{"E","-1"}}
	 * else =0
	 * 
	 * @param aa
	 * @return
	 */
	public static int getAminoAcidCharge(String aa) {
		for (int i = 0; i < aminoacidCharge.length; i++) {
			if (aminoacidCharge[i][0].equals(aa)) {
				return Integer.parseInt(aminoacidCharge[i][1]);
			}
		}
		return 0;
	}

	/**
	 * Translate a codon
	 * 
	 * @param codon a <code>String</code> of 3 nucleotides
	 * @return
	 */
	public static String getAminoAcid(String codon) {
		codon = "TTT" + codon + "TTT"; // we add F amino acid before and after to avoid automcatic translation in
										// Methyonine and other specific translation
		DNASequence seq = new DNASequence(codon);
		String aminoacid = seq.getRNASequence().getProteinSequence().getSequenceAsString();
		aminoacid = aminoacid.charAt(1) + "";
		// System.out.println(aminoacid);
		return aminoacid;
	}

	/**
	 * Test if a codon is a start codon <br>
	 * 
	 * @param codon a <code>String</code> of 3 nucleotides
	 * @return
	 */
	public static boolean isStart(String codon) {
		for (int i = 0; i < startCodon.length; i++) {
			if (codon.equals(startCodon[i][0]))
				return true;
		}
		return false;
	}

	/**
	 * Test if a codon is a stop codon <br>
	 * 
	 * @param codon a <code>String</code> of 3 nucleotides
	 * @return
	 */
	public static boolean isStop(String codon) {
		if (getAminoAcid(codon).indexOf("*") != -1) {
			return true;
		}
		return false;
	}

	/**
	 * Return first threee nucleotide of a Sequence
	 * 
	 * @param sequence
	 * @return
	 */
	public static String getFirstCodon(Sequence sequence) {
		return sequence.getSequence().substring(0, 3);
	}

	/**
	 * If it is a start codon return the corresponding codon, an empty
	 * <code>String</code> otherwise
	 * 
	 * @param codon a <code>String</code> of 3 nucleotides
	 * @return
	 */
	public static String startCodon(String codon) {
		if (isStart(codon)) {
			for (int i = 0; i < startCodon.length; i++) {
				if (codon.equals(startCodon[i][0]))
					return startCodon[i][1];
			}
		}
		return "";
	}

	/**
	 * Go through genome on both strand and calculate the proportion of each codon
	 * 
	 * @param genome
	 * @param fileName
	 */
	public static void getCodonProportion(Genome genome, String fileName) {
		String sequence = genome.getChromosomes().get(0).getSequenceAsString()
				+ genome.getChromosomes().get(0).getReverseComplement().getSequenceAsString();
		TreeSet<String> allCodons = getAllCodons();
		double[] codonValue = new double[allCodons.size()];
		String[][] codonTable = new String[allCodons.size()][2];
		int i = 0;
		double totalCodonNumber = 0;
		for (String codon : allCodons) {
			codonTable[i][0] = codon;
			double number = (double) FileUtils.searchPosition(codon, sequence).size();
			codonValue[i] = number;
			totalCodonNumber += number;
			i++;
		}
		for (int k = 0; k < codonValue.length; k++) {
			codonTable[k][1] = (codonValue[k] / totalCodonNumber) + "";
		}
		TabDelimitedTableReader.save(codonTable, fileName);
	}

	/**
	 * Return a TreeSet containing all possible codons
	 * 
	 * @return
	 */
	public static TreeSet<String> getAllCodons() {
		TreeSet<String> allCodons = new TreeSet<String>();
		String[] nucleotides = { "A", "T", "G", "C" };
		for (String nucleotide1 : nucleotides) {
			for (String nucleotide2 : nucleotides) {
				for (String nucleotide3 : nucleotides) {
					String codon = nucleotide1 + nucleotide2 + nucleotide3;
					allCodons.add(codon);
				}
			}
		}
		System.out.println("Find " + allCodons.size() + " codons");
		return allCodons;
	}
}
