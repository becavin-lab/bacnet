package bacnet.datamodel.proteomics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.Strand;

import bacnet.Database;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.proteomics.NTerm.TypeModif;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.reader.TabDelimitedTableReader;

public class NTermFilter {

	public static String DEFAULT_FILTER_PATH = Database.getInstance().getPath() + "N-Term SVN/Results/NTerm filter.txt";

	private ArrayList<String> typeOverlaps = new ArrayList<>();
	private NTermData nTermExp;
	private ArrayList<String> duplicateSequenceToRemove = new ArrayList<>();
	private int aTISCutoff = 25;
	/**
	 * Create a list of cutoff to apply to the list of Nterm<br>
	 */
	private HashMap<String, ArrayList<String>> cutoffHashMap = new HashMap<>();

	/**
	 * Filter a NTermData using the table of filter found in fileName
	 * 
	 * @param nTermExp
	 * @param fileName
	 */
	public NTermFilter(NTermData nTermExp, String fileName) {
		setnTermExp(nTermExp);
		setCutoffHashMap(readFilter(fileName));
		setOverlapsFromCutoffs();
		System.out.println();
	}

	/**
	 * Search for overlap with Gene, sRNAn, asRNA and ncRNA of each NTerm
	 * 
	 * @param nameNTermData
	 */
	public void applyFilter() {
		// aTIS variation 62
		// NoGroup 3331
		// aTIS long variation 1
		// internal TIS 100
		// weird Stuff 22
		// aTIS 2024
		//
		// NoGroup 4823
		// aTIS variation 83
		// aTIS long variation 1
		// weird Stuff 30
		// internal TIS 132
		// aTIS 2723
		/*
		 * Search overlap with Genome elements
		 */
		Genome genome = Genome.loadEgdeGenome();
		ArrayList<String> overlapList = new ArrayList<>();
		TreeSet<String> typeOverlapSet = new TreeSet<>();
		for (NTerm nTerm : this.getnTermExp().getElements().values()) {
			nTerm.setTypeOverlap("noGroup");
			typeOverlapSet.add("noGroup");
			/*
			 * Search the type of Overlap
			 */
			Sequence overlapSequence = findOverlap(nTerm, genome.getFirstChromosome());
			if (overlapSequence != null) {
				int[] overlap = getOverlap(nTerm, overlapSequence);
				overlapList.add(overlapSequence.getName() + "\t" + overlap[0]);
				if (overlap[0] % 3 == 0)
					nTerm.setOverlapInFrame(true);
				nTerm.setOverlap(overlapSequence.getName() + " : " + overlap[0]);
				/*
				 * Apply every cutoff available
				 */
				for (String key : cutoffHashMap.keySet()) {
					ArrayList<String> cutoffs = cutoffHashMap.get(key);
					String typeOverlap = cutoffs.get(0);
					typeOverlapSet.add(typeOverlap);
					boolean passCutOff = true;
					for (int i = 1; i < cutoffs.size() && passCutOff; i++) {
						// System.out.println("Apply cutoff: "+cutoffs.get(i));
						passCutOff = applyCutOff(cutoffs.get(i), nTerm, overlapSequence, overlap);
					}
					/*
					 * If Nterm has passed the cutoff
					 */
					if (passCutOff) {
						nTerm.setTypeOverlap(typeOverlap);
						nTerm.setOverlap(overlapSequence.getName() + " : " + overlap[0]);
					}
				}
			} else {
				nTerm.setOverlap("intergenic");
			}
		}

		/*
		 * Remove sequence put in duplicateSequenceToRemove
		 */
		String typeOverlapDuplicate = "duplicates";
		typeOverlapSet.add(typeOverlapDuplicate);
		int i = 0;
		for (NTerm nTerm : this.getnTermExp().getElements().values()) {
			if (this.getDuplicateSequenceToRemove().contains(nTerm.getName())) {
				i++;
				nTerm.setTypeOverlap(typeOverlapDuplicate);
				nTerm.setOverlap(nTerm.getName() + " : " + (nTerm.getDuplicates().size() + 1));
				// System.out.println("Duplicate sequence: "+i+" "+nTerm.getSequencePeptide()+"
				// from nTerm: "+nTerm.getName()+" is removed from classification");
			}
		}

		/*
		 * Create Overlap Type list
		 */
		TabDelimitedTableReader.saveList(overlapList, "D:/overlaps.txt");
		this.getnTermExp().getTypeOverlaps().clear();
		for (String typeOverlap : typeOverlapSet) {
			this.getnTermExp().getTypeOverlaps().add(typeOverlap);
			System.out.println(typeOverlap);
		}
		this.getnTermExp().save();
	}

	/**
	 * Run some tests to check that filters has been well applied
	 * 
	 */
	public void verifFilter() {
		HashMap<String, Integer> typeOverlapMap = new HashMap<>();
		for (NTerm nTerm : this.getnTermExp().getElements().values()) {
			if (nTerm.getTypeOverlap().equals("")) {
				System.out.println("No Type: " + nTerm.getName());
			}

			if (typeOverlapMap.containsKey(nTerm.getTypeOverlap())) {
				typeOverlapMap.put(nTerm.getTypeOverlap(), typeOverlapMap.get(nTerm.getTypeOverlap()) + 1);
			} else {
				typeOverlapMap.put(nTerm.getTypeOverlap(), 1);
			}
		}
		for (String temp : typeOverlapMap.keySet())
			System.out.println(temp + "  " + typeOverlapMap.get(temp));

	}

	/**
	 * Read a cutoff<br>
	 * Interprete each cutoff by returning true of false if the NTerm pass the
	 * condition<br>
	 * If the cutoff contain an "or": parse it in two separate cutoff<br>
	 * If the cutoff starts with an "!": return the opposite of the cutoff<br>
	 * 
	 * @param cutoff
	 * @param nTerm
	 * @param overlapSequence
	 * @param overlap
	 * @return
	 */
	public boolean applyCutOff(String cutoff, NTerm nTerm, Sequence overlapSequence, int[] overlap) {
		// System.out.println("nTerm "+nTerm+" "+begin+" "+end+" "+strand+"
		// "+overlap[0]+" "+overlapSequence.getName());
		if (cutoff.contains(" or ")) {
			boolean passCutoff1 = applyCutOff(cutoff.split(" or ")[0], nTerm, overlapSequence, overlap);
			boolean passCutoff2 = applyCutOff(cutoff.split(" or ")[1], nTerm, overlapSequence, overlap);
			if (cutoff.split(" or ").length == 2) {
				return (passCutoff1 || passCutoff2);
			} else {
				boolean passCutoff3 = applyCutOff(cutoff.split(" or ")[2], nTerm, overlapSequence, overlap);
				return (passCutoff1 || passCutoff2 || passCutoff3);
			}
		} else if (cutoff.startsWith("!")) {
			return (!applyCutOff(cutoff.replaceFirst("!", ""), nTerm, overlapSequence, overlap));
		} else {
			cutoff = cutoff.trim();
			// System.out.println("Applying cutoff:" + cutoff);
			if (cutoff.contains("Overlap=")) {
				double value = Double.parseDouble(cutoff.replaceFirst("Overlap=", ""));
				return overlapEqual(overlap, value);
			} else if (cutoff.contains("Overlap>")) {
				double value = Double.parseDouble(cutoff.replaceFirst("Overlap>", ""));
				return overlapSup(overlap, value);
			} else if (cutoff.contains("Overlap<")) {
				double value = Double.parseDouble(cutoff.replaceFirst("Overlap<", ""));
				return overlapInf(overlap, value);
			} else if (cutoff.equals("START")) {
				return isStart(nTerm, "START");
			} else if (cutoff.equals("START-1")) {
				return isStart(nTerm, "START-1");
			} else if (cutoff.equals("NoForSTART-1")) {
				boolean isStart = isStart(nTerm, "START-1");
				boolean formyl = (nTerm.getTypeModif() == TypeModif.For);
				boolean test = (isStart && !formyl);
				return test;
			} else if (cutoff.equals("START+1")) {
				return isStart(nTerm, "START+1");
			} else if (cutoff.equals("InFrame")) {
				return nTerm.isOverlapInFrame();
			} else if (cutoff.equals("ForMAndAltStart")) {
				return isForMandAltStart(nTerm);
			} else if (cutoff.equals("STOP")) {
				return containStopCodon(nTerm, overlapSequence, overlap);
			} else if (cutoff.equals("RemoveDuplicates")) {
				for (String duplicate : nTerm.getDuplicates()) {
					this.getDuplicateSequenceToRemove().add(duplicate);
				}
				return true;
			} else if (cutoff.equals("RemoveAllDuplicates")) {
				if (nTerm.getDuplicates().size() != 0) {
					this.getDuplicateSequenceToRemove().add(nTerm.getName());
				}
				return true;
			} else {
				System.out.println(cutoff + "  could not be interpret");
				return false;
			}
		}
	}

	/**
	 * Return true if overlap[0] < value
	 * 
	 * @param overlap
	 * @param value
	 * @return
	 */
	public static boolean overlapEqual(int[] overlap, double value) {
		if (overlap[0] == value)
			return true;
		return false;
	}

	/**
	 * Return true if overlap[0] < value
	 * 
	 * @param overlap
	 * @param value
	 * @return
	 */
	public static boolean overlapInf(int[] overlap, double value) {
		if (overlap[0] < value)
			return true;
		return false;
	}

	/**
	 * Return true if overlap[0] < value
	 * 
	 * @param overlap
	 * @param value
	 * @return
	 */
	public static boolean overlapSup(int[] overlap, double value) {
		if (overlap[0] > value)
			return true;
		return false;
	}

	/**
	 * Find i an NTerm start with a methyonine (START), or if the amino acid just
	 * before the peptide is a Methyonine (START-1), or just after (START+1)
	 * 
	 * @param nTerm
	 * @param typeCodon START or START-1 or START+1 or nothing
	 * @return
	 */
	public static boolean isStart(NTerm nTerm, String typeCodon) {
		String startcode = nTerm.getStartCode();
		if (startcode.equals(""))
			return false;
		String[] codonCodes = startcode.split("; ");
		for (String code : codonCodes) {
			// System.out.println("code: "+code);
			String newCode = code.toUpperCase().substring(2);
			if (newCode.equals(typeCodon)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find if the NTerm start with a Methyonine<br>
	 * if it corresponds to a regular start codon "ATG" we need a Formyl
	 * modificaition to prove its a real translation initiation<br>
	 * If the methyonine corresponds to any other type of start codon, we don't need
	 * more to prove it's a translation initiation
	 * 
	 * @param nTerm
	 * @return
	 */
	public static boolean isForMandAltStart(NTerm nTerm) {
		String codon = nTerm.getStartCodon();
		if (nTerm.getSequencePeptide().startsWith("M")) {
			if (codon.equals("ATG-M")) {
				if (nTerm.getTypeModif() == TypeModif.For)
					return true;
				else
					return false;
			} else {
				return true;
			}
		} else {
			return false;
		}

	}

	/**
	 * Return true if it contains a StopCodon between the peptide and the overlaping
	 * sequence
	 * 
	 * @param nTerm
	 * @param overlapSequence
	 * @return
	 */
	public static boolean containStopCodon(NTerm nTerm, Sequence overlapSequence, int[] overlap) {
		int begin = nTerm.getBegin();
		int end = overlapSequence.getBegin();
		Strand strand = Strand.POSITIVE;
		if (!nTerm.isStrand()) {
			begin = overlapSequence.getEnd();
			end = nTerm.getEnd();
			strand = Strand.NEGATIVE;
		}
		if (begin > end) {
			int temp = begin;
			begin = end;
			end = temp;
		}
		String sequence = nTerm.getGenome().getFirstChromosome().getSequenceAsString(begin, end, strand);
		DNASequence seq = new DNASequence(sequence);
		String sequenceAA = seq.getRNASequence().getProteinSequence().getSequenceAsString();
		if (sequenceAA.contains("*")) {
			System.err.println("nTerm contain stopCodon between the peptide and the overlaping sequence: "
					+ nTerm.getName() + ":" + nTerm.getSequencePeptide() + ":" + nTerm.getSequenceAA() + "  seq: "
					+ sequence + "  seqAA: " + sequenceAA);
			return true;
		} else
			return false;
	}

	/**
	 * Create a list of cutoff to apply to the list of Nterm<br>
	 */
	public static HashMap<String, ArrayList<String>> readFilter(String fileName) {
		HashMap<String, ArrayList<String>> cutoffHashMap = new HashMap<>();
		String[][] array = TabDelimitedTableReader.read(fileName);
		int i = 1;
		while (i < array.length) {
			ArrayList<String> cutoffList = new ArrayList<>();
			String typeOverlap = array[i][0];
			cutoffList.add(typeOverlap);
			cutoffList.add(array[i][1]);
			i++;
			while (i < array.length && array[i][0].equals("")) {
				cutoffList.add(array[i][1]);
				i++;
			}
			cutoffHashMap.put(i + "", cutoffList);
		}
		return cutoffHashMap;
	}

	/**
	 * Find with which element on EGDe genome and RAST genome an NTerm overlap:<br>
	 * <li>region of 25bp at the beginning of EGDe gene
	 * <li>region of 25bp at the beginning of EGDe RAST gene, if this overlap is
	 * smaller than with EGde take this one, in order to detect bona-fide aTIS
	 * <li>region of 25bp at the beginning of Srna or ASrna of EGDe, if this overlap
	 * is smaller than with Srna take this one, in order to detect bona-fide aTIS
	 * <li>overlap in the middle of EGDe gene
	 * <li>overlap in the middle of EGDe RAST gene
	 * <li>overlap in the middle of Srna or ASrna of EGDe<br>
	 * <br>
	 * The 25bp are given by <code>aTISCutoff</code> argument of the
	 * <code>NTermFilter</code> object
	 *
	 * @param nTerm
	 * @param genome
	 * @param genomeRAST
	 * @return
	 */
	private Sequence findOverlap(NTerm nTerm, Chromosome chromosomeEGDe) {
		/*
		 * Start of Gene, Gene RAST and sRNA or ASrna
		 */
		Sequence seqOverlap = null;
		for (Gene gene : chromosomeEGDe.getGenes().values()) {
			// if(nTerm.getName().equals("peptide-1627") &&
			// gene.getName().equals("lmo2436")){
			// System.out.println();
			// }
			if (Sequence.isSameStrand(nTerm, gene) && Sequence.isOverlapStart(nTerm, gene, aTISCutoff)) {
				seqOverlap = gene;
			}
		}
		for (Srna sRNA : chromosomeEGDe.getsRNAs().values()) {
			if (Sequence.isSameStrand(nTerm, sRNA) && Sequence.isOverlapStart(nTerm, sRNA, aTISCutoff)) {
				if (seqOverlap != null) {
					int overlap = getOverlap(nTerm, seqOverlap)[0];
					int overlapSrna = getOverlap(nTerm, sRNA)[0];
					/*
					 * Take the element which overlap the closer to its start
					 */
					if (Math.abs(overlapSrna) < Math.abs(overlap)) {
						seqOverlap = sRNA;
					}
				} else {
					seqOverlap = sRNA;
				}
			}
		}
		if (seqOverlap != null)
			return seqOverlap;

		/*
		 * Rest of the Gene, Gene RAST and sRNA or ASrna
		 */
		for (Gene gene : chromosomeEGDe.getGenes().values()) {
			if (Sequence.isSameStrand(nTerm, gene) && Sequence.isOverlap(nTerm, gene))
				return gene;
		}
		for (Srna sRNA : chromosomeEGDe.getsRNAs().values()) {
			if (Sequence.isSameStrand(nTerm, sRNA) && Sequence.isOverlap(nTerm, sRNA))
				return sRNA;
		}
		for (Srna sRNA : chromosomeEGDe.getAsRNAs().values()) {
			if (Sequence.isSameStrand(nTerm, sRNA) && Sequence.isOverlap(nTerm, sRNA))
				return sRNA;
		}
		return null;
	}

	/**
	 * Return the difference between the start
	 * 
	 * @param nTerm
	 * @param overlapSequence
	 * @return
	 */
	private int[] getOverlap(NTerm nTerm, Sequence overlapSequence) {
		int diffBegin = -100000;
		int diffEnd = -100000;
		if (nTerm.isStrand()) {
			diffBegin = nTerm.getBegin() - overlapSequence.getBegin();
			diffEnd = overlapSequence.getEnd() - nTerm.getEnd();
		} else {
			diffEnd = nTerm.getBegin() - overlapSequence.getBegin();
			diffBegin = overlapSequence.getEnd() - nTerm.getEnd();
		}
		int[] overlap = { diffBegin, diffEnd };
		return overlap;
	}

	/**
	 * Get a final list of all OverlapType, from cutoffs table
	 */
	private void setOverlapsFromCutoffs() {
		TreeSet<String> overlapsSet = new TreeSet<>();
		for (String key : cutoffHashMap.keySet()) {
			overlapsSet.add(cutoffHashMap.get(key).get(0)); // First element in the different cutoffs list is always the
															// type of the overlap
		}
		String typeOverlapDuplicate = "Duplicates";
		overlapsSet.add(typeOverlapDuplicate);
		getTypeOverlaps().clear();
		for (String overlapType : overlapsSet)
			getTypeOverlaps().add(overlapType);
	}

	/*
	 * *********************************** GETTERS AND SETTERS
	 * 
	 * ***********************************
	 */
	public NTermData getnTermExp() {
		return nTermExp;
	}

	public void setnTermExp(NTermData nTermExp) {
		this.nTermExp = nTermExp;
	}

	public ArrayList<String> getTypeOverlaps() {
		return typeOverlaps;
	}

	public void setTypeOverlaps(ArrayList<String> typeOverlaps) {
		this.typeOverlaps = typeOverlaps;
	}

	public int getaTISCutoff() {
		return aTISCutoff;
	}

	public void setaTISCutoff(int aTISCutoff) {
		this.aTISCutoff = aTISCutoff;
	}

	public HashMap<String, ArrayList<String>> getCutoffHashMap() {
		return cutoffHashMap;
	}

	public void setCutoffHashMap(HashMap<String, ArrayList<String>> cutoffHashMap) {
		this.cutoffHashMap = cutoffHashMap;
	}

	public ArrayList<String> getDuplicateSequenceToRemove() {
		return duplicateSequenceToRemove;
	}

	public void setDuplicateSequenceToRemove(ArrayList<String> duplicateSequenceToRemove) {
		this.duplicateSequenceToRemove = duplicateSequenceToRemove;
	}

}
