package bacnet.scripts.listeriomics.nterm;

import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.biojava3.core.sequence.Strand;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.proteomics.NTermUtils;
import bacnet.datamodel.proteomics.TIS;
import bacnet.datamodel.sequence.Codon;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.genome.SDProfile;
import bacnet.scripts.genome.StartCodonProfile;
import bacnet.utils.ArrayUtils;
import bacnet.utils.UNAfold;
import bacnet.utils.VectorUtils;

/**
 * List of method to do statistics about common features in N-Terminus of genes:
 * - start codon - SD binding - AU proportion - Folding energy
 * 
 * @author UIBC
 *
 */
public class NTermStat {

	private String PATH_iTIS = NTermUtils.getPATH() + "Statistics/StatGenes/iTIS_List.excel";
	private static int DECAY_MINUS = 20;
	private static int DECAY_PLUS = 60;
	private String startCodon = "";
	private String sdEnergy = "";
	private String tirSequence = "";
	private String atNumber = "";
	private String gcNumber = "";
	private String tirStructure = "";
	private String codonCAI = "";
	private String codonCAIGene = "";
	private String chargedResidues = "";
	private String chargedResiduesGene = "";
	private String utrLength = "";
	private String expTilingBHI1 = "";
	private String expTilingBHI2 = "";
	private String expTilingStat = "";
	private String expTiling30C = "";
	private String expTilingBlood = "";
	private String aTISExp1 = "";
	private String aTISExp2 = "";
	private String aTISBlood = "";
	private String totalNbSpectrum = "";
	private String detectable = "";
	private String ncRNAUpstream = "";
	private String comment = "";
	private NTermData massSpec;
	private NTermData massSpec2;

	private GeneExpression geneExpr;

	private Genome genome;
	private ArrayList<Gene> listGenes = new ArrayList<>();

	public NTermStat() {
		genome = Genome.loadEgdeGenome();
		ArrayList<String> listGenesTemp = new ArrayList<>();
		// listGenes = getiTIS();
		for (Gene gene : genome.getFirstChromosome().getGenes().values()) {
			listGenes.add(gene);
		}

//		setSDBindingEnergy();
//		setTIRStructureEnergy();
//		setGeneSequence();
//		setTIRSequence();
	}

	public void display() {
		ExpressionMatrix matrix = ExpressionMatrix.loadTab(NTermUtils.getPATH() + "Statistics/Statistics Genes.excel",
				true);

//		HistogramView.displayMatrix(matrix, "aTIS and iTIS");
//		TableAWTView.displayMatrix(matrix, "heatMap");
	}

	public ArrayList<Gene> getiTIS() {
		String[][] arrayiTIS = TabDelimitedTableReader.read(PATH_iTIS);
		ArrayList<Gene> listGeneTemp = new ArrayList<>();
		for (int i = 1; i < arrayiTIS.length; i++) {
			String geneName = arrayiTIS[i][ArrayUtils.findColumn(arrayiTIS, "Overlap")];
			Gene gene = genome.getGeneFromName(geneName);
			String iTISName = arrayiTIS[i][ArrayUtils.findColumn(arrayiTIS, "comment1")];
			Gene geneITIS = null;
			if (gene.isStrand()) {
				int begin = Integer.parseInt(arrayiTIS[i][ArrayUtils.findColumn(arrayiTIS, "Begin")]);
				geneITIS = new Gene(iTISName, begin, gene.getEnd(), gene.getStrand());
			} else {
				int end = Integer.parseInt(arrayiTIS[i][ArrayUtils.findColumn(arrayiTIS, "End")]);
				geneITIS = new Gene(iTISName, gene.getBegin(), end, gene.getStrand());
			}
			geneITIS.setChromosomeID(gene.getChromosomeID());
			geneITIS.setGenomeName(gene.getGenomeName());
			geneITIS.setCog(gene.getCog());
			geneITIS.setGeneName(gene.getGeneName());
			geneITIS.setFeatures(gene.getFeatures());
			geneITIS.setOperon(gene.getOperon());
			geneITIS.setComment(gene.getComment());
			geneITIS.setProduct(gene.getProduct());
			geneITIS.getFeatures().put("Gene", gene.getName());
			geneITIS.getFeatures().put("Formyl",
					arrayiTIS[i][ArrayUtils.findColumn(arrayiTIS, "For or non-AUG start?")]);
			geneITIS.getFeatures().put("peptide", arrayiTIS[i][ArrayUtils.findColumn(arrayiTIS, "peptide")]);
			int shorter = 0;
			if (gene.isStrand()) {
				shorter = geneITIS.getBegin() - gene.getBegin();
			} else {
				shorter = gene.getEnd() - geneITIS.getEnd();
			}
			int shorterFrancis = Integer.parseInt(arrayiTIS[i][ArrayUtils.findColumn(arrayiTIS, "distance")]);
			if (shorter == shorterFrancis) {
				System.out.println(gene.getName() + " : " + shorter + " : " + shorterFrancis);
			}
			geneITIS.getFeatures().put("length difference", shorter + "");
			listGeneTemp.add(geneITIS);
		}
		return listGeneTemp;
	}

	/**
	 * Get all lmos from EGD-e, and extract different information: <br>
	 * 
	 * The Translation Iinitiation region here is always: -20bp ---- 60bp around
	 * start codon
	 * <li>Start codon
	 * <li>SD energy
	 * <li>Structure energy ....
	 */
	public void tableGeneEGDe() {
		/*
		 * Fill header
		 */
		ArrayList<String> result = new ArrayList<>();
		String[] headers = { "Name", "Length (aa)", "Start codon", "SD energy", "TIR structure (-20 to +60bp)",
				"A-T number (TIR)", "G-C number(TIR)", "GC number", "Codon adaptation Index of TIR",
				"Codon adaptation Index of Gene", "Charged residues proportion (TIR)",
				"Charged residues proportion (Gene)", "GeneExpr: BHI 37C Exp 1", "GeneExpr: BHI 37C Exp 2",
				"GeneExpr: BHI 37C Stat", "GeneExpr: BHI 30C", "GeneExpr: Human blood 60min", "UTR length",
				"Total nb spectra", "N-Term aTIS Exp1", "N-Term aTIS Exp2", "N-Term aTIS Blood", "Detectable ?",
				"ncRNA (-100bp upstream)", "Gene", "Description", "COG", "Strand", "Begin", "End",
				"TIRSequence (-" + DECAY_MINUS + ":+" + DECAY_PLUS + " around start codon)" };
		String header = "";
		for (String temp : headers)
			header += temp + "\t";
		result.add(header);

		for (int i = 0; i < listGenes.size(); i++) {
			Gene gene = listGenes.get(i);

			startCodon = getStartCodon(gene) + "";
			sdEnergy = getSDEnergy(gene);
			tirStructure = getTIRStructureEnergy(gene);
			int decay = DECAY_PLUS;
			if (decay > gene.getLength())
				decay = gene.getLength() - 1;
			tirSequence = gene.getSequenceRNA().substring(3, decay);
			atNumber = getAUNumber(tirSequence) + "";
			gcNumber = getGCNumber(tirSequence) + "";
			String gcNumberGene = getGCNumber(gene.getSequence()) + "";
			codonCAI = getCAI(gene);
			codonCAIGene = getCAIGene(gene);
			chargedResidues = getChargedResidues(gene, decay) + "";
			chargedResiduesGene = getChargedResiduesGene(gene) + "";
			utrLength = getUTRLength(gene) + "";
			expTilingBHI1 = getGeneExpr(gene, "EGDe_270407") + "";
			expTilingBHI2 = getGeneExpr(gene, "EGDe_280212") + "";
			expTilingStat = getGeneExpr(gene, "EGDe_Stat") + "";
			expTiling30C = getGeneExpr(gene, "EGDe_30C") + "";
			expTilingBlood = getGeneExpr(gene, "EGDe_HB60") + "";
			String[] atis = getAtisBHI(gene);
			aTISExp1 = atis[0];
			aTISExp2 = atis[1];
			aTISBlood = getAtisBlood(gene);
			totalNbSpectrum = getTotalSpectrum(gene) + "";
			detectable = getDetectable(gene);
			ncRNAUpstream = getNCRNAUpstream(gene);
			comment = getComment(gene);
			String[] rows = { gene.getName(), gene.getLengthAA() + "", startCodon, sdEnergy, tirStructure, atNumber,
					gcNumber, gcNumberGene, codonCAI, codonCAIGene, chargedResidues, chargedResiduesGene, expTilingBHI1,
					expTilingBHI2, expTilingStat, expTiling30C, expTilingBlood, utrLength, totalNbSpectrum, aTISExp1,
					aTISExp2, aTISBlood, detectable, ncRNAUpstream, gene.getGeneName(), comment, gene.getCog(),
					gene.getStrand() + "", gene.getBegin() + "", gene.getEnd() + "", tirSequence };
			String row = "";
			for (String temp : rows)
				row += temp + "\t";
			result.add(row);
		}
		TabDelimitedTableReader.saveList(result, NTermUtils.getPATH() + "Statistics/Statistics Genes.excel");

	}

	/**
	 * Count the number of A and T nucleotides
	 * 
	 * @param sequence
	 * @return
	 */
	public static double getAUNumber(String sequence) {
		return ((double) StringUtils.countMatches(sequence, "A") + (double) StringUtils.countMatches(sequence, "U"))
				/ sequence.length();
	}

	/**
	 * Count the number of G and C nucleotides
	 * 
	 * @param sequence
	 * @return
	 */
	public static double getGCNumber(String sequence) {
		return ((double) StringUtils.countMatches(sequence, "G") + (double) StringUtils.countMatches(sequence, "C"))
				/ sequence.length();
	}

	/**
	 * Return information about gene or iTIS
	 * 
	 * @param gene
	 * @return
	 */
	public static String getComment(Gene gene) {
		if (gene.getName().contains("iTIS")) {
			String commentTemp = "LengthDiff:" + gene.getFeature("length difference") + ";Peptide:"
					+ gene.getFeature("peptide") + ";Gene:" + gene.getFeature("Gene") + ";" + gene.getComment();
			return commentTemp;
		} else {
			return gene.getComment();
		}
	}

	/**
	 * Return start codon proportion in the genome
	 * 
	 * @param gene
	 * @return
	 */
	public static double getStartCodon(Gene gene) {
		String startCodon = gene.getSequenceRNA().substring(0, 3);
		// String sdEnergy = ""+UNAfold.hybridRNA(startCodon,"start",
		// "AUG","anti-SD",true);
		// System.out.println(gene.getName()+" : "+startCodon+" : "+sdEnergy);
		if (startCodon.equals("AUG")) {
			return 80.7;
		} else if (startCodon.equals("UUG")) {
			return 10.6;
		} else if (startCodon.equals("GUG")) {
			return 8.3;
		} else if (startCodon.equals("AUU") || startCodon.equals("CUG")) {
			return 0.001;
		} else if (startCodon.equals("AUA") || startCodon.equals("AUC")) {
			return 0.0003;
		} else {
			return 0;
		}
	}

	/**
	 * Return the binding energy with aSD sequence
	 * 
	 * @param gene
	 * @return
	 */
	public static String getSDEnergy(Gene gene) {
		String[][] geneArray = TabDelimitedTableReader
				.read(NTermUtils.getPATH() + "Statistics/StatGenes/aSDBindingEnergy.excel");
		for (int i = 0; i < geneArray.length; i++) {
			if (geneArray[i][0].equals(gene.getName())) {
				return geneArray[i][1] + "";
			}
		}
		System.err.println("Did not found aSD for : " + gene.getName());
		return "";
	}

	/**
	 * For every gene calculate the SD-aSD hybridization affinity and save it in:
	 * /StatGenes/aSDBindingEneryg.excel
	 */
	public void setSDBindingEnergy() {
		System.out.println("Set binding energy");
		ArrayList<String> result = new ArrayList<>();
		int k = 0;
		for (Gene gene : listGenes) {
			String row = gene.getName() + "\t";
			double sdEnergy = UNAfold.hybridRNA(gene.getSDSequence(), gene.getName(), Sequence.ANTI_SD_SEQ,
					"anti-SD" + k, true);
			row += sdEnergy;
			result.add(row);
			k++;
		}
		TabDelimitedTableReader.saveList(result, NTermUtils.getPATH() + "Statistics/StatGenes/aSDBindingEnergy.excel");
	}

	/**
	 * For very gene read TIRStructure.excel and extract the mean value of energy:
	 * We calculate the 2nd structure free energy and save it a table<br>
	 * And we calculate the energy for 40 different sequence: -20+40 to -20+80 and
	 * calculate the average energy
	 * 
	 * @param gene
	 * @return
	 */
	public static String getTIRStructureEnergy(Gene gene) {
		String[][] geneArray = TabDelimitedTableReader
				.read(NTermUtils.getPATH() + "Statistics/StatGenes/TIRStructure.excel");
		for (int i = 0; i < geneArray.length; i++) {
			if (geneArray[i][0].equals(gene.getName())) {
				return geneArray[i][1] + "";
			}
		}
		System.err.println("Did not found TIR structure for : " + gene.getName());
		return "";
	}

	/**
	 * For every gene calculate the 2nd structure free energy and save it a
	 * table<br>
	 * We calculate the energy for 40 different sequence: -20+40 to -20+80 and
	 * calculate the average energy
	 */
	public void setTIRStructureEnergy() {
		ArrayList<String> result = new ArrayList<>();
		int k = 0;
		System.out.println("Set TIR start structure");
		for (Gene gene : listGenes) {
			String row = gene.getName() + "\t";
			double[] values = new double[40];
			for (int i = 0; i < 40 && ((i + (DECAY_PLUS - 20)) < gene.getLength()); i++) {
				String tirSequence = gene.getSDSequence(DECAY_MINUS)
						+ gene.getSequenceRNA().substring(3, (DECAY_PLUS - 20) + i);
				double energy = UNAfold.foldRNA("TIR" + i, tirSequence, "Fold" + i, true);
				values[i] = energy;
				row += energy + "\t";
				System.out.println(gene.getName() + " : " + energy);
			}
			// if gene size is under 40bp
			if (gene.getLength() < (DECAY_PLUS - 20)) {
				for (int i = 0; i < 40; i++) {
					String tirSequence = gene.getSDSequence(DECAY_MINUS) + gene.getSequenceRNA();
					double energy = UNAfold.foldRNA("TIR" + i, tirSequence, "Fold" + i, true);
					values[i] = energy;
					row += energy + "\t";
					System.out.println(gene.getName() + " : " + energy);
				}
			}
			double meanEnergy = VectorUtils.mean(values);
			row += meanEnergy;
			result.add(row);
			k++;
			if (k % 500 == 0) {
				TabDelimitedTableReader.saveList(result,
						NTermUtils.getPATH() + "Statistics/StatGenes/TIRStructure_" + k + ".excel");
			}
		}
		TabDelimitedTableReader.saveList(result, NTermUtils.getPATH() + "Statistics/StatGenes/TIRStructure.excel");
	}

	/**
	 * We create a fasta file with all the TIR DNA sequence (-18 +63)
	 */
	public void setTIRSequence() {
		ArrayList<String> result = new ArrayList<>();
		System.out.println("Set TIR structure");
		for (Gene gene : listGenes) {
			result.add(">" + gene.getName());
			int decay = DECAY_PLUS;
			if (gene.getLength() < decay) {
				decay = gene.getLength() - 1;
				if (decay % 3 != 0)
					decay = decay - 1;
				if (decay % 3 != 0)
					decay = decay - 1;
			}
			tirSequence = gene.getSequenceRNA().substring(0, decay);
			result.add(tirSequence.replaceAll("U", "T"));
		}
		TabDelimitedTableReader.saveList(result, NTermUtils.getPATH() + "Statistics/StatGenes/TIRSequence.fasta");
	}

	/**
	 * Create a fasta file with all the gene sequences
	 */
	public void setGeneSequence() {
		ArrayList<String> result = new ArrayList<>();
		System.out.println("Set gene sequence");
		for (Gene gene : listGenes) {
			result.add(">" + gene.getName());
			String sequence = gene.getSequence();
			result.add(sequence);
		}
		TabDelimitedTableReader.saveList(result, NTermUtils.getPATH() + "Statistics/StatGenes/GeneSequence.fasta");
	}

	/**
	 * After generating TIRSequence.fasta in setTIRSequence() method we submit to
	 * http://genomes.urv.cat/CAIcal/ to calculate CAI value and expected CAI value
	 * The table with CAI values is saved from the website in: CAIResults.excel
	 * 
	 * @param gene
	 * @return
	 */
	public String getCAI(Gene gene) {
		String[][] geneArray = TabDelimitedTableReader
				.read(NTermUtils.getPATH() + "Statistics/StatGenes/CAIResults.excel");
		for (int i = 0; i < geneArray.length; i++) {
			if (geneArray[i][0].equals(gene.getName())) {
				return geneArray[i][1] + "";
			}
		}
		System.err.println("Did not found CAI for : " + gene.getName());
		return "";
	}

	/**
	 * After generating GeneSequence.fasta in setGeneSequence() method we submit to
	 * http://genomes.urv.cat/CAIcal/ to calculate CAI value and expected CAI value
	 * The table with CAI values is saved from the website in:
	 * StatGenes/CAIGeneResults.excel
	 * 
	 * @param gene
	 * @return
	 */
	public String getCAIGene(Gene gene) {
		String[][] geneArray = TabDelimitedTableReader
				.read(NTermUtils.getPATH() + "Statistics/StatGenes/CAIGeneResults.excel");
		for (int i = 0; i < geneArray.length; i++) {
			if (geneArray[i][0].equals(gene.getName())) {
				return geneArray[i][1] + "";
			}
		}
		System.err.println("Did not found CAI for : " + gene.getName());
		return "";
	}

	/**
	 * By reading the List in: /StatGenes/EGDeDetectable.txt Say if a gene is
	 * detectable by the spectometer or not
	 * 
	 * @param gene
	 * @return
	 */
	public String getDetectable(Gene gene) {
		ArrayList<String> genes = TabDelimitedTableReader
				.readList(NTermUtils.getPATH() + "Statistics/StatGenes/EGDeDetectable.txt");
		if (genes.contains(gene.getName())) {
			return "Yes";
		}
		return "No";
	}

	/**
	 * Go through all MassSpec data and find if the lmio was detected
	 * 
	 * @param gene
	 * @return
	 */
	public String[] getAtisBHI(Gene gene) {
		if (massSpec == null) {
			BioCondition bioCond = BioCondition.getBioCondition("EGDe_TIS_Final");
			massSpec = bioCond.getnTerms().get(0);
			massSpec = NTermData.load(massSpec.getName());
		}
		String[] result = { "-50", "-50" };
		if (gene.getName().contains("iTIS")) {
			result[1] = gene.getFeature("Formyl");
			return result;
		}
		for (TIS tis : massSpec.getTisList()) {
			NTerm nTerm = tis.getnTermRef();
			if (nTerm.getTypeOverlap().equals("aTIS")) {
				if (nTerm.getOverlap().contains(gene.getName())) {
					String overlap = "";
//					if(nTerm.getTypeModif()==TypeModif.For){
//						overlap = "10";
//					}else{
//						overlap = "5";
//						
//					}

					int[] spectrum = nTerm.getSpectrum();
					if (spectrum[0] != 0 || spectrum[1] != 0) {
						overlap = (spectrum[0] + spectrum[1]) + "";
						result[0] = overlap;
					}
					if (spectrum[2] != 0 || spectrum[3] != 0 || spectrum[4] != 0 || spectrum[5] != 0) {
						result[1] = (spectrum[2] + spectrum[3] + spectrum[4] + spectrum[5]) + "";
					}
				}
			}
		}

		return result;
	}

	public String getAtisBlood(Gene gene) {
		if (massSpec2 == null) {
			BioCondition bioCond = BioCondition.getBioCondition("Blood_TIS_Final");
			massSpec2 = bioCond.getnTerms().get(0);
			massSpec2 = NTermData.load(massSpec.getName());
		}
		if (gene.getName().contains("iTIS")) {
			return gene.getFeature("Formyl");
		}
		for (TIS tis : massSpec2.getTisList()) {
			NTerm nTerm = tis.getnTermRef();
			if (nTerm.getTypeOverlap().equals("aTIS")) {
				if (nTerm.getOverlap().contains(gene.getName())) {
					return nTerm.getSpectra() + "";
//					if(nTerm.getTypeModif()==TypeModif.For){
//						return "10";
//					}else{
//						return "5";
//					}
				}
			}
		}

		return "-50";
	}

	/**
	 * Sum up the different charge of the amino acids of the TIR
	 * 
	 * @param gene
	 * @return
	 */
	public int getChargedResidues(Gene gene, int decay) {
		String tirSequenceAA = gene.getSequenceAA().substring(0, decay / 3);
		int charged = 0;
		for (char aa : tirSequenceAA.toCharArray()) {
			charged += Codon.getAminoAcidCharge(aa + "");
		}
		return charged;
	}

	/**
	 * Sum up the different charge of the amino acids of the gene sequence
	 * 
	 * @param gene
	 * @return
	 */
	public int getChargedResiduesGene(Gene gene) {
		String sequenceAA = gene.getSequenceAA();
		int charged = 0;
		for (char aa : sequenceAA.toCharArray()) {
			charged += Codon.getAminoAcidCharge(aa + "");
		}
		return charged;
	}

	/**
	 * For each gene search the closest TSS downstream, and return the UTR length
	 * (=distance between gene and TSS) We don't start at the start codon but -20bp,
	 * in case there is a TSS in this region indicating a possible short UTR with
	 * alternative aTIS
	 * 
	 * @param gene
	 * @return
	 */
	@Deprecated
	public int getUTRLength(Gene gene) {
		/*
		 * Load TSS data
		 */
//		if(tssNGSPlus==null && tssNGSMinus==null){
//			BioCondition bioCond = BioCondition.getBioCondition("EGDe_Complete_TSS");
//			tssNGSPlus = bioCond.getTsss().get(0);
//			tssNGSMinus = bioCond.getTsss().get(1);
//			tssNGSPlus.read();
//			tssNGSMinus.read();
//		}
//
//		if(gene.isStrand()){
//			/*
//			 * Search TSS upstream on plus strand
//			 */
//			int k=-20;
//			int begin = gene.getBegin();
//			while(tssNGSPlus.getValues()[begin-k]==0){
//				//System.out.println(tssNGSPlus.getValues()[peptide.getBegin()-k]);
//				k++;
//			}
//			return k;
//		}else{
//			/*
//			 * Search TSS upstream on minus strand
//			 */
//			int k=-20;
//			int begin = gene.getEnd();
//			while((begin+k)<tssNGSMinus.getLength() && tssNGSMinus.getValues()[begin+k]==0){
//				//System.out.println((peptide.getEnd()+k)+"  "+tssNGSMinus.getValues()[peptide.getEnd()+k]);
//				k++;
//			}
//			return k;
//		}
		return -1;
	}

	/**
	 * Return the median value expression (calculated on 10 BHI 37C data) in the
	 * Gene Expression array of the gene
	 * 
	 * @param gene
	 * @return
	 */
	public double getGeneExpr(Gene gene, String dataName) {
		BioCondition bioCond = BioCondition.getBioCondition(dataName);
		geneExpr = bioCond.getGeneExprs().get(0);
		geneExpr.read();

		String name = gene.getName();
		if (name.contains("iTIS"))
			name = name.replaceFirst("iTIS ", "").split("_")[0];
		if (Database.getInstance().getProbesGExpression().containsKey(name)) {
			return geneExpr.getMedianValue(name);
		} else {
			return 0;
		}
	}

	/**
	 * For each gene add the number of spectra of every peptide available (not only
	 * aTIS peptides)
	 * 
	 * @param gene
	 * @return
	 */
	public int getTotalSpectrum(Gene gene) {
		if (massSpec == null) {
			BioCondition bioCond = BioCondition.getBioCondition("EGDe_TIS_Final");
			massSpec = bioCond.getnTerms().get(0);
			massSpec = NTermData.load(massSpec.getName());
		}
		int count = 0;
		String name = gene.getName();
		if (name.contains("iTIS"))
			name = gene.getFeature("Gene");
		for (TIS tis : massSpec.getTisList()) {
			for (NTerm nTerm : tis.getnTerms()) {
				if (nTerm.getOverlap().contains(name)) {
					if (gene.isStrand() && gene.getBegin() <= nTerm.getBegin()) {
						if (gene.getName().equals("lmo2653")) {
							System.out.println(nTerm.getName() + " " + nTerm.getSpectra());
						}
						count += nTerm.getSpectra();
					}
					if (!gene.isStrand() && nTerm.getEnd() <= gene.getEnd()) {
						if (gene.getName().equals("lmo2653")) {
							System.out.println(nTerm.getName() + " " + nTerm.getSpectra());
						}
						count += nTerm.getSpectra();
					}
				}
			}
		}
		return count;
	}

	/**
	 * For each gene search if the end of a smallRNA or cisReg element is in the
	 * region -200bp - start codon
	 * 
	 * @param gene
	 * @return
	 */
	public String getNCRNAUpstream(Gene gene) {
		int distance = 100;
		if (gene.isStrand()) {
			int begin = gene.getBegin() - distance;
			int end = gene.getBegin();
			for (Srna sRNA : genome.getFirstChromosome().getsRNAs().values()) {
				if (sRNA.isStrand()) {
					if (sRNA.getEnd() > begin && sRNA.getEnd() < end) {
						return sRNA.getName();
					}
				}
			}
			for (Srna sRNA : genome.getFirstChromosome().getCisRegs().values()) {
				if (sRNA.isStrand()) {
					if (sRNA.getEnd() > begin && sRNA.getEnd() < end) {
						return sRNA.getName();
					}
				}
			}
		} else {
			int begin = gene.getEnd();
			int end = gene.getEnd() + distance;
			for (Srna sRNA : genome.getFirstChromosome().getsRNAs().values()) {
				if (!sRNA.isStrand()) {
					if (sRNA.getBegin() < end && sRNA.getBegin() > begin) {
						return sRNA.getName();
					}
				}
			}
			for (Srna sRNA : genome.getFirstChromosome().getCisRegs().values()) {
				if (!sRNA.isStrand()) {
					if (sRNA.getBegin() < end && sRNA.getBegin() > begin) {
						return sRNA.getName();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Create a table where we put all lmos detectable by the mass spectrometer
	 */
	public static void detectable() {

		String cutPeptide = "E";
		Genome genome = Genome.loadEgdeGenome();
		for (Gene gene : genome.getFirstChromosome().getGenes().values()) {
			String sequence = gene.getSequenceAA();
			if (sequence.indexOf(cutPeptide) != -1) {
				sequence = sequence.substring(0, sequence.indexOf(cutPeptide) + 1);
			}
			if (sequence.length() < 5) {
				// System.err.println(sequence);
			} else {
				System.out.println(sequence);
			}
		}

		/*
		 * use <code>http://web.expasy.org/compute_pi/</code> to calculate mass and
		 * isoelectric point
		 */
		String[][] predictedPeptide = TabDelimitedTableReader
				.read(NTermUtils.getPATH() + "EGDePeptideMass_" + cutPeptide + ".txt");
		ArrayList<String> predicted = new ArrayList<String>();
		ArrayList<String> notPredicted = new ArrayList<String>();
		String[] headers = { "Sequence", "Start codon", "Locus", "Theoretical pI", "Molecular weight", "Gene",
				"Description", "COG", "Strand", "Begin", "End" };
		String header = "";
		for (String temp : headers)
			header += temp + "\t";
		predicted.add(header);
		notPredicted.add(header);

		for (Gene gene : genome.getFirstChromosome().getGenes().values()) {
			String sequence = gene.getSequenceAA();
			if (sequence.indexOf(cutPeptide) != -1) {
				sequence = sequence.substring(0, sequence.indexOf(cutPeptide) + 1);
			}
			if (sequence.length() > 5) {
				boolean found = false;
				for (int i = 1; i < predictedPeptide.length; i++) {
					String seq = predictedPeptide[i][0];
					if (sequence.equals(seq)) {
						found = true;
						double mass = Double.parseDouble(predictedPeptide[i][2]);
						double pI = Double.parseDouble(predictedPeptide[i][1]);
						String[] rows = { seq, gene.getSequence().substring(0, 3), gene.getName(), pI + "", mass + "",
								gene.getGeneName(), gene.getComment(), gene.getCog(), gene.getStrand() + "",
								gene.getBegin() + "", gene.getEnd() + "" };
						String row = "";
						for (String temp : rows)
							row += temp + "\t";
						if (mass > 700 && mass < 5700) {
							predicted.add(row);

						} else {
							notPredicted.add(row);

						}
					}
				}
				if (!found)
					System.err.println("not found" + gene.getName());
			} else {
				String[] rows = { sequence, gene.getSequence().substring(0, 3), gene.getName(), "", "",
						gene.getGeneName(), gene.getComment(), gene.getCog(), gene.getStrand() + "",
						gene.getBegin() + "", gene.getEnd() + "" };
				String row = "";
				for (String temp : rows)
					row += temp + "\t";
				notPredicted.add(row);
			}
		}
	}

	/**
	 * Given a genome extract main information such as:<br>
	 * <li>number of start codons, stop codons
	 * <li>number of high SD binding sites
	 * <li>number of possible ORFs
	 * <li>number of predicted genes<br>
	 * <br>
	 * From this metrics, predict the number of possible internal translation
	 * initiation site
	 * 
	 * @param genome
	 */
	public static void statInternalORF(Genome genome) {
		String genomeName = genome.getSpecies().replaceAll(" ", "_");
		String fileName = SDProfile.PATH + genomeName + "_+_.wig";
		String[][] arrayPlus = TabDelimitedTableReader.read(fileName);
		fileName = SDProfile.PATH + genomeName + "_-_.wig";
		String[][] arrayMinus = TabDelimitedTableReader.read(fileName);

		ArrayList<String> table = new ArrayList<>();
		table.add("Genome " + genome.getSpecies() + "\t" + genome.getFirstChromosome().getLength());

		int numberCodon = 0;
		int numberStart = 0;
		int numberStartandSD = 0;
		int numberStop = 0;
		int numberSD = 0;
		for (int i = 2; i < genome.getFirstChromosome().getLength(); i++) {
			String codon = genome.getFirstChromosome().getSequenceAsString(i - 1, i + 1, Strand.POSITIVE);
			numberCodon++;
			if (Codon.isStart(codon)) {
				numberStart++;
				double minEnergy = 30;
				for (int j = 1; j < 20; j++) {
					if (i - j > 0) {
						Double energy = Double.parseDouble(arrayPlus[i - j][1]);
						if (minEnergy > energy)
							minEnergy = energy;
					}
				}
				if (minEnergy < StartCodonProfile.SD_ENERGY_CUTOFF) {
					numberStartandSD++;
				}
			} else if (Codon.isStop(codon))
				numberStop++;

			Double energy = Double.parseDouble(arrayPlus[i][1]);
			if (energy < -6) {
				numberSD++;
			}

		}

		for (int i = 2; i < genome.getFirstChromosome().getLength(); i++) {
			String codon = genome.getFirstChromosome().getSequenceAsString(i - 1, i + 1, Strand.NEGATIVE);
			numberCodon++;
			if (Codon.isStart(codon)) {
				numberStart++;
				double minEnergy = 30;
				for (int j = 1; j < 20; j++) {
					if (i + j < genome.getFirstChromosome().getLength()) {
						Double energy = Double.parseDouble(arrayPlus[i + j][1]);
						if (minEnergy > energy)
							minEnergy = energy;
					}
				}
				if (minEnergy < StartCodonProfile.SD_ENERGY_CUTOFF) {
					numberStartandSD++;
				}
			} else if (Codon.isStop(codon))
				numberStop++;

			Double energy = Double.parseDouble(arrayMinus[i][1]);
			if (energy < -6) {
				numberSD++;
			}
		}
		table.add("Number of codons\t" + numberCodon);
		table.add("Number of start codons\t" + numberStart);
		table.add("Number of stop codons\t" + numberStop);
		table.add("Number of SD sequence below -7 kcal/mol\t" + numberSD);
		table.add("Number of start codons having 20bp before a SD sequence below -7 kcal/mol\t" + numberStartandSD);
		table.add("Number of predicted ORFs\t" + genome.getFirstChromosome().getGenes().size());

		ArrayList<String> geneSDsequences = new ArrayList<>();
		geneSDsequences.add("Gene\tNumberInternalSD\tposition\tProduct\tName");
		for (Gene gene : genome.getFirstChromosome().getGenes().values()) {
			int numberInternalSD = 0;
			for (int i = gene.getBegin() + 5; i < gene.getEnd() - 5; i++) {
				if (gene.isStrand()) {
					String codon = genome.getFirstChromosome().getSequenceAsString(i - 1, i + 1, Strand.POSITIVE);
					if (Codon.isStart(codon)) {
						double minEnergy = 30;
						for (int j = 1; j < 20; j++) {
							if (i - j > 0) {
								Double energy = Double.parseDouble(arrayPlus[i - j][1]);
								if (minEnergy > energy)
									minEnergy = energy;
							}
						}
						if (minEnergy < StartCodonProfile.SD_ENERGY_CUTOFF) {
							numberInternalSD++;
						}
					}
				} else {
					String codon = genome.getFirstChromosome().getSequenceAsString(i - 1, i + 1, Strand.NEGATIVE);
					if (Codon.isStart(codon)) {
						double minEnergy = 30;
						for (int j = 1; j < 20; j++) {
							if (i + j < genome.getFirstChromosome().getLength()) {
								Double energy = Double.parseDouble(arrayPlus[i + j][1]);
								if (minEnergy > energy)
									minEnergy = energy;
							}
						}
						if (minEnergy < StartCodonProfile.SD_ENERGY_CUTOFF) {
							numberInternalSD++;
						}
					}
				}
			}
			geneSDsequences.add(gene.getName() + "\t" + numberInternalSD + "\t" + gene.getStrand() + ";"
					+ gene.getBegin() + ";" + gene.getEnd() + "\t" + gene.getProduct() + "\t" + gene.getGeneName());
		}
		TabDelimitedTableReader.saveList(geneSDsequences,
				NTermUtils.getPATH() + "Internal_SD_stat-" + genome.getSpecies() + ".txt");

		TabDelimitedTableReader.saveList(table, NTermUtils.getPATH() + "ORF_SD_stat-" + genome.getSpecies() + ".txt");

	}

	/**
	 * Create a fasta file with all sequences to create a logo
	 */
	public static void peptideLogo() {
		Genome genome = Genome.loadGenome(Genome.EGDE_NAME);
		ArrayList<String> fastaFile = new ArrayList<>();
		/**
		 * Logo of aTIS
		 */
		// ArrayList<String> lmosaTIS =
		// TabDelimitedTableReader.readList(NTermUtils.getPATH()+"List peptides/aTIS
		// List.txt");
		// for(String aTISlmo : lmosaTIS){
		// Sequence gene = genome.getElement(aTISlmo);
		// fastaFile.add(">"+aTISlmo);
		// fastaFile.add(getLogo(gene));
		// }
		// TabDelimitedTableReader.saveList(fastaFile, NTermUtils.getPATH()+"List
		// peptides/aTIS logo.fasta");
		/*
		 * Logo of all Lmos
		 */
		fastaFile.clear();
		TreeSet<String> noATISfound = new TreeSet<>();
		for (Gene gene : genome.getFirstChromosome().getGenes().values()) {
			fastaFile.add(">" + gene.getName());
			fastaFile.add(getLogo(gene));
			// if(!lmosaTIS.contains(gene.getName())){
			// noATISfound.add(gene.getName());
			// }
		}
		TabDelimitedTableReader.saveList(fastaFile, NTermUtils.getPATH() + "Statistics/lmos logo.fasta");
		// /*
		// * Lmos with high SD: 1289
		// */
		// ArrayList<String> highSDlmo =
		// TabDelimitedTableReader.readList(NTermUtils.getPATH()+"List peptides/highSD
		// lmo.txt");
		// for(String aTISlmo : highSDlmo){
		// Sequence gene = genome.getElement(aTISlmo);
		// fastaFile.add(">"+aTISlmo);
		// fastaFile.add(getLogo(gene));
		// }
		// TabDelimitedTableReader.saveList(fastaFile, NTermUtils.getPATH()+"List
		// peptides/highSD logo.fasta");
		//
		//
		// TabDelimitedTableReader.saveTreeSet(noATISfound, NTermUtils.getPATH()+"List
		// peptides/no aTIS lmo List.txt");
		// /*
		// * Logo of lmos for which no aTIS exists
		// */
		// fastaFile.clear();
		// ArrayList<String> nolmosaTIS =
		// TabDelimitedTableReader.readList(NTermUtils.getPATH()+"List peptides/no aTIS
		// lmo List.txt");
		// for(String aTISlmo : nolmosaTIS){
		// Sequence gene = genome.getElement(aTISlmo);
		// fastaFile.add(">"+aTISlmo);
		// fastaFile.add(getLogo(gene));
		// }
		// TabDelimitedTableReader.saveList(fastaFile, NTermUtils.getPATH()+"List
		// peptides/no aTIS logo.fasta");

		// /*
		// * Logo of all iTIS
		// */
		// String[][] iTIS = TabDelimitedTableReader.read(NTermUtils.getPATH()+"List
		// peptides/iTIS List.txt");
		// fastaFile.clear();
		// for(int i = 1;i<iTIS.length;i++){
		// int begin = Integer.parseInt(iTIS[i][2]);
		// int end = Integer.parseInt(iTIS[i][3]);
		// String strand = iTIS[i][4];
		// Sequence gene = new Sequence("iTIS-"+i, begin, end, strand.charAt(0));
		// fastaFile.add(">"+gene.getName());
		// fastaFile.add(getLogo(gene));
		// }
		// TabDelimitedTableReader.saveList(fastaFile, NTermUtils.getPATH()+"List
		// peptides/iTIS logo.fasta");

	}

	/**
	 * For a given Sequence, return the nucleotide sequence before and after
	 * starting position
	 * 
	 * @param gene
	 * @return
	 */
	public static String getLogo(Sequence gene) {
		String sequence = "";
		if (gene.isStrand()) {
			Sequence seq = new Sequence("sd", gene.getBegin() - 20, gene.getBegin() + 10, '+');
			seq.setGenomeName(gene.getGenomeName());
			seq.setChromosomeID(gene.getChromosomeID());
			sequence = seq.getSequenceRNA();
		} else {
			Sequence seq = new Sequence("sd", gene.getEnd() - 10, gene.getEnd() + 20, '-');
			seq.setGenomeName(gene.getGenomeName());
			seq.setChromosomeID(gene.getChromosomeID());
			sequence = seq.getSequenceRNA();
		}
		return sequence;
	}

}
