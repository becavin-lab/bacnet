package bacnet.datamodel.proteomics;

import java.io.Serializable;
import java.util.ArrayList;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Sequence;

/**
 * Object for managing every mapped sequences
 * 
 * @author UIBC
 *
 */
public class NTerm extends Sequence implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7191352336233310587L;

	/**
	 * Type of modification at the start of a peptide
	 * 
	 * @author UIBC
	 *
	 */
	public enum TypeModif {
		AcD3, For, Ace, NH2, PyroGlu
	}

	public enum MappingFrame {
		plus, plusM, plusP, minus, minusM, minusP
	}

	public enum ExperimentName {
		Trypsin_Soluble, Trypsin_Insoluble, Trypsin_Actino_Soluble, Trypsin_Actino_Insoluble, GluC_Actino_Soluble,
		GluC_Actino_Insoluble
	}

	public static String NTERM_PROJECT_BHI = "EGDe_37C_TIS";
	/**
	 * Total number of spectra found
	 */
	private int spectra = 0;
	/**
	 * Each column of this vector give the number of spectra for each condition
	 */
	private int[] spectrum = new int[6];
	/**
	 * If the peptide has also been found with MaxQuant analysis software, we write
	 * something in this attribute, otherwise its blank
	 */
	private String maxQuant = "";
	/**
	 * If the peptide has also been found with MaxQuant analysis software, we write
	 * something in this attribute, otherwise its blank, each column is for one type
	 * of experiments.
	 */
	private String[] maxQuantum = { "", "", "", "", "", "" };
	private int score = 0;
	private int threshold = 0;

	/**
	 * Modify sequence provided by Francis
	 */
	private String modifSequence = "";
	/**
	 * Peptide sequence extracted from modified Sequence
	 */
	private String sequencePeptide = "";
	/**
	 * Translated sequence of the mapped peptide
	 */
	private String sequenceMap = "";
	/**
	 * First codon of the mapped sequence
	 */
	private String startCodon = "";
	/**
	 * Codon upstream to mapped sequence
	 */
	private String previousCodon = "";
	/**
	 * Codon downstream to first codon of mapped sequence
	 */
	private String nextCodon = "";
	/**
	 * Number of duplicates having same sequencePeptide (e.g. peptide mapped on
	 * different regions)
	 */
	private ArrayList<String> duplicates = new ArrayList<>();
	/**
	 * Indicates if the first codon or previousCodon is a Start codon
	 */
	private String startCode = "";
	/**
	 * Name of the genome element which <code>NTerm</code> overlaps
	 */
	private String overlap = "";
	/**
	 * Check if the peptide is in frame with genome element which it overlaps
	 */
	private boolean overlapInFrame = false;
	/**
	 * CodonUsage of the peptideSequence
	 */
	private double codonUsage = 0;
	/**
	 * Type of TSS upstream to this NTerm : i.e. internal, intergenic, ...
	 */
	private String tssUptype = "";
	/**
	 * Distance where can be found a TSS upstream to this NTerm
	 */
	private int tssUpDistance = ExpressionMatrix.MISSING_VALUE;
	/**
	 * Coverage of the closest TSS upstream to this NTerm
	 */
	private int tssUpCoverage = ExpressionMatrix.MISSING_VALUE;
	/**
	 * Type of TSS downstream to this NTerm : i.e. internal, intergenic, ...
	 */
	private String tssDowntype = "";
	/**
	 * Distance where can be found a TSS downstream to this NTerm
	 */
	private int tssDownDistance = ExpressionMatrix.MISSING_VALUE;
	/**
	 * Coverage of the closest TSS downstream to this NTerm
	 */
	private int tssDownCoverage = ExpressionMatrix.MISSING_VALUE;
	/**
	 * Frame where the peptide has been mapped
	 */
	private MappingFrame mappingFrame = MappingFrame.plus;
	/**
	 * Type of modification
	 */
	private TypeModif typeModif = TypeModif.AcD3;
	/**
	 * Potential type of TIS of this NTerm
	 */
	private String typeOverlap = "aTIS";
	/**
	 * Indicate if it was found in soluble part
	 */
	private boolean soluble = false;
	/**
	 * Indicate if it was found in soluble part
	 */
	private boolean insoluble = false;

	/**
	 * Value of the binding between anti-SD sequence and the region -20bp upstream
	 * to the peptide
	 */
	private double antiSDBindingFreeEnergy = 0;

	/**
	 * TIS Name : A TIS is a group of NTerm have same position but a slightly
	 * different position
	 */
	private String TISName = "";

	public NTerm() {
		super();
	}

	public NTerm(String name, int from, int to, char strand) {
		super(name, from, to, strand);
	}

	@Override
	public String toString() {
		String ret = this.getName() + "\t" + this.getBegin() + "\t" + this.getEnd() + "\t" + this.getEnd() + "\t"
				+ this.getStrand();
		return ret;
	}

	/**
	 * Return if the NTerm was detected in the first experiment, or the second, or
	 * both
	 */
	public String getExperiment() {
		String experiment = "";
		if (this.getSpectrum()[0] != 0 || this.getSpectrum()[1] != 0) {
			experiment = "Experiment_1;";
		}
		if (this.getSpectrum()[2] != 0 || this.getSpectrum()[3] != 0 || this.getSpectrum()[4] != 0
				|| this.getSpectrum()[5] != 0) {
			experiment += "Experiment_2;";
		}
		return experiment;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof NTerm) {
			NTerm nTerm2 = (NTerm) object;
			if (this.getSequencePeptide().equals(nTerm2.getSequencePeptide())) {
				if (this.getTypeModif() == nTerm2.getTypeModif()) {
					return true;
				} else
					return false;
			} else
				return false;
		} else
			return false;

	};

	/*
	 * ******************************************************* Getter and Setter
	 * *******************************************************
	 */

	public int getSpectra() {
		return spectra;
	}

	public void setSpectra(int spectra) {
		this.spectra = spectra;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public String getModifSequence() {
		return modifSequence;
	}

	public void setModifSequence(String modifSequence) {
		this.modifSequence = modifSequence;
	}

	public String getSequencePeptide() {
		return sequencePeptide;
	}

	public void setSequencePeptide(String sequencePeptide) {
		this.sequencePeptide = sequencePeptide;
	}

	public String getSequenceMap() {
		return sequenceMap;
	}

	public void setSequenceMap(String sequenceMap) {
		this.sequenceMap = sequenceMap;
	}

	public ArrayList<String> getDuplicates() {
		return duplicates;
	}

	public void setDuplicates(ArrayList<String> duplicates) {
		this.duplicates = duplicates;
	}

	public String getStartCodon() {
		return startCodon;
	}

	public void setStartCodon(String startCodon) {
		this.startCodon = startCodon;
	}

	public String getPreviousCodon() {
		return previousCodon;
	}

	public void setPreviousCodon(String previousCodon) {
		this.previousCodon = previousCodon;
	}

	public String getMaxQuant() {
		return maxQuant;
	}

	public void setMaxQuant(String maxQuant) {
		this.maxQuant = maxQuant;
	}

	public String getNextCodon() {
		return nextCodon;
	}

	public void setNextCodon(String nextCodon) {
		this.nextCodon = nextCodon;
	}

	public String getStartCode() {
		return startCode;
	}

	public void setStartCode(String startCode) {
		this.startCode = startCode;
	}

	public String getOverlap() {
		return overlap;
	}

	public void setOverlap(String overlap) {
		this.overlap = overlap;
	}

	public boolean isOverlapInFrame() {
		return overlapInFrame;
	}

	public void setOverlapInFrame(boolean overlapInFrame) {
		this.overlapInFrame = overlapInFrame;
	}

	public double getCodonUsage() {
		return codonUsage;
	}

	public void setCodonUsage(double codonUsage) {
		this.codonUsage = codonUsage;
	}

	public int getTssUpDistance() {
		return tssUpDistance;
	}

	public void setTssUpDistance(int tssUpDistance) {
		this.tssUpDistance = tssUpDistance;
	}

	public int getTssUpCoverage() {
		return tssUpCoverage;
	}

	public void setTssUpCoverage(int tssUpCoverage) {
		this.tssUpCoverage = tssUpCoverage;
	}

	public int getTssDownDistance() {
		return tssDownDistance;
	}

	public void setTssDownDistance(int tssDownDistance) {
		this.tssDownDistance = tssDownDistance;
	}

	public int getTssDownCoverage() {
		return tssDownCoverage;
	}

	public void setTssDownCoverage(int tssDownCoverage) {
		this.tssDownCoverage = tssDownCoverage;
	}

	public String getTssUptype() {
		return tssUptype;
	}

	public void setTssUptype(String tssUptype) {
		this.tssUptype = tssUptype;
	}

	public String getTssDowntype() {
		return tssDowntype;
	}

	public void setTssDowntype(String tssDowntype) {
		this.tssDowntype = tssDowntype;
	}

	public MappingFrame getMappingFrame() {
		return mappingFrame;
	}

	public void setMappingFrame(MappingFrame mappingFrame) {
		this.mappingFrame = mappingFrame;
	}

	public TypeModif getTypeModif() {
		return typeModif;
	}

	public void setTypeModif(TypeModif typeModif) {
		this.typeModif = typeModif;
	}

	public String getTypeOverlap() {
		return typeOverlap;
	}

	public void setTypeOverlap(String typeOverlap) {
		this.typeOverlap = typeOverlap;
	}

	public String[] getMaxQuantum() {
		return maxQuantum;
	}

	public void setMaxQuantum(String[] maxQuantum) {
		this.maxQuantum = maxQuantum;
	}

	public boolean isSoluble() {
		return soluble;
	}

	public void setSoluble(boolean soluble) {
		this.soluble = soluble;
	}

	public boolean isInsoluble() {
		return insoluble;
	}

	public void setInsoluble(boolean insoluble) {
		this.insoluble = insoluble;
	}

	public int[] getSpectrum() {
		return spectrum;
	}

	public void setSpectrum(int[] spectrum) {
		this.spectrum = spectrum;
	}

	public double getAntiSDBindingFreeEnergy() {
		return antiSDBindingFreeEnergy;
	}

	public void setAntiSDBindingFreeEnergy(double antiSDBindingFreeEnergy) {
		this.antiSDBindingFreeEnergy = antiSDBindingFreeEnergy;
	}

	public String getTISName() {
		return TISName;
	}

	public void setTISName(String tISName) {
		TISName = tISName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
