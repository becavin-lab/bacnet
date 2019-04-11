package bacnet.datamodel.annotation;

import java.io.File;
import java.util.HashMap;

import org.eclipse.swt.graphics.Color;

import bacnet.Database;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GeneNCBITools;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.table.core.ColorMapper;
import bacnet.utils.StringColor;

/**
 * Tools for calculating codon Usage of amino acid sequences<br>
 * Codon Usage tables came from : http://www.kazusa.or.jp/codon/
 * 
 * @author UIBC
 *
 */
public class CodonUsage {

	/**
	 * Link Cdon to their codon usage value<br>
	 * It is create from the reading of Tables in
	 * <code>Project.getANNOTATION_PATH()+"CodonUsage"+File.separator</code>
	 */
	private HashMap<String, Double> codonTable = new HashMap<String, Double>();
	private ColorMapper colorMapper;

	/**
	 * Providing the name of codon Table, it loads the Table and create
	 * <code>CodonUsage</code>
	 * 
	 * @param codonTableName
	 */
	public CodonUsage(String codonTableName) {
		String fileName = Database.getInstance().getPath() + "Analysis/Egd-e Annotation/CodonUsage" + File.separator;
		if (codonTableName.equals("EGD-e")) {
			fileName += "CodonUsage_ListeriaEGDe.txt";
		} else if (codonTableName.equals("Innocua")) {
			fileName += "CodonUsage_ListeriaInnocua.txt";
		} else if (codonTableName.equals("B.subtilis")) {
			fileName += "CodonUsage_Bsubtilis168.txt";
		} else if (codonTableName.equals("Human")) {
			fileName += "CodonUsage_ListeriaEGDe.txt";
		} else if (codonTableName.equals("E. coli K12")) {
			fileName += "CodonUsage_Ecolik12.txt";
		}

		String[][] codonUsage;
		codonUsage = TabDelimitedTableReader.read(fileName);
		codonTable = new HashMap<String, Double>();
		for (int i = 1; i < codonUsage.length; i++) {
			codonTable.put(codonUsage[i][1], Double.parseDouble(codonUsage[i][3]));
		}

		// colorMapper =
		// ColorMapper.load(Project.getANNOTATION_PATH()+"CodonUsage"+File.separator+"CodonUsage.col");

	}

	/**
	 * Calculate Codon usage of a sequence<br>
	 * Providing <code>codonTable</code> has been loaded first
	 * 
	 * @see CodonUsage
	 * @param sequence
	 * @return
	 */
	public double calculate(String sequence) {
		StringColor str = GeneNCBITools.codonDisplay(sequence);
		String[] codons = str.str.split(" ");
		double codonUsage = 0;
		int count = 0;
		for (int i = 0; i < codons.length; i++) {
			String codon = codons[i];
			if (codonTable.containsKey(codon)) {
				double value = codonTable.get(codon);
				codonUsage += value;
				count++;
			}
		}
		codonUsage = codonUsage / count;
		return codonUsage;
	}

	public StringColor colorizeSequence(String sequence) {
		StringColor str = GeneNCBITools.codonDisplay(sequence);
		StringColor result = new StringColor();
		String[] codons = str.str.split(" ");
		for (int i = 0; i < codons.length; i++) {
			String codon = codons[i];
			if (codonTable.containsKey(codon)) {
				double value = codonTable.get(codon);
				result.addF(codon, getCodonColor(value));
				result.add(" ");
			} else {
				result.add(codon + " ");
			}
		}
		return result;
	}

	public Color getCodonColor(double value) {
		return colorMapper.parseColor(value);
	}

	public double[][] getValues(String sequence) {
		StringColor str = GeneNCBITools.codonDisplay(sequence);
		String[] codons = str.str.split(" ");
		double[][] values = new double[codons.length][2];
		int k = 2;
		for (int i = 0; i < codons.length; i++) {
			String codon = codons[i];
			if (codonTable.containsKey(codon)) {
				double value = codonTable.get(codon);
				values[i][0] = k;
				values[i][1] = value;
				k += 3;
			} else {
				values[i][0] = k;
				values[i][1] = 0;
			}
		}
		return values;
	}

	/**
	 * To verify the codon prediction on EGD-e genome we look at all the genome and
	 * count the number of AAA, ATG and CCC codons
	 */
	public static void verifyCodonUsage() {
		Chromosome chromo = Genome.loadEgdeGenome().getFirstChromosome();
		double totalNumber = 0;
		double codonAAA = 0;
		double codonATG = 0;
		double codonCCC = 0;
		double codonGAT = 0;
		double codonGCT = 0;
		double codonACA = 0;
		for (Gene gene : chromo.getGenes().values()) {
			for (int i = 0; i < gene.getLength() - 3; i = i + 3) {
				// + strand
				String codon = gene.getSequence().substring(i, i + 3);
				// System.out.println(codon);
				totalNumber++;
				if (codon.equals("AAA"))
					codonAAA++;
				else if (codon.equals("ATG"))
					codonATG++;
				else if (codon.equals("CCC"))
					codonCCC++;
				else if (codon.equals("GAT"))
					codonGAT++;
				else if (codon.equals("GCT"))
					codonGCT++;
				else if (codon.equals("ACA"))
					codonACA++;
			}
		}
		System.out.println("Find: " + totalNumber + " codons");
		System.out.println("With: " + codonAAA / totalNumber + " AAA codons, " + codonATG / totalNumber
				+ " ATG codons, " + codonCCC / totalNumber + " CCC codons");
		System.out.println("With: " + codonGAT / totalNumber + " GAT codons, " + codonGCT / totalNumber
				+ " GCT codons, " + codonACA / totalNumber + " ACA codons");
	}
}
