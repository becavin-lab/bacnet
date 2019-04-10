package bacnet.datamodel.proteomics;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import bacnet.datamodel.proteomics.NTerm.TypeModif;
import bacnet.datamodel.sequence.Sequence;
import bacnet.utils.ListUtils;

/**
 * This class is used to regroup different Nterm. The assumption is that we
 * regroup in a TIS every Nterm comes from the same peptide but are now
 * different due to proteolysis and C-Terminal ragging
 * 
 * @author UIBC
 *
 */
public class TIS extends Sequence implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3452878896031108870L;

	private String refSequence = "";

	private NTerm nTermRef = new NTerm();

	private int totalSpectra = 0;

	private String overlapingGene = "";

	private ArrayList<NTerm> nTerms = new ArrayList<NTerm>();

	private ArrayList<String> overlaps = new ArrayList<>();

	private ArrayList<TypeModif> modifs = new ArrayList<NTerm.TypeModif>();

	public TIS() {

	}

	/**
	 * From all the nTerms (supposed in the same strand), get the minimal begin and
	 * the maximum end
	 */
	public void findPosition() {
		this.setStrand(nTerms.get(0).getStrand());
		this.setChromosomeID(nTerms.get(0).getChromosomeID());
		this.setGenomeName(nTerms.get(0).getGenomeName());
		int begin = nTerms.get(0).getBegin();
		int end = nTerms.get(0).getEnd();

		for (NTerm nTermTemp : nTerms) {
			if (nTermTemp.getBegin() < begin)
				begin = nTermTemp.getBegin();
			if (nTermTemp.getEnd() > end)
				end = nTermTemp.getEnd();
		}
		this.setBegin(begin);
		this.setEnd(end);
		this.setLength(end - begin + 1);
	}

	/**
	 * Find the Nterm with the highest spectrum
	 */
	public void findNTermRef() {
		/*
		 * Calculate total spectra
		 */
		totalSpectra = 0;
		for (NTerm nTermTemp : nTerms) {
			totalSpectra += nTermTemp.getSpectra();
		}

		/*
		 * Find NTerm ref by searching in the right order: - Formylated sequence with
		 * highest score - Peptide starting with Methyle amino acid with highest score -
		 * Peptide without methyl group with highest score
		 */
		// - Formylated sequence with highest score
		int maxScore = 0;
		boolean found = false;
		// System.out.println("NEW TIS -------------------");
		for (NTerm nTermTemp : nTerms) {
			if (nTermTemp.getTypeModif() == TypeModif.For) {
				// System.out.println(nTermTemp.getModifSequence()+" - "+nTermTemp.getScore());
				if (maxScore < nTermTemp.getScore()) {
					// System.out.println("Max score: "+nTermTemp.getModifSequence()+" -
					// "+nTermTemp.getScore());
					maxScore = nTermTemp.getScore();
					this.setnTermRef(nTermTemp);
					found = true;
				}
			}
		}
		// - Peptide starting with Methyle amino acid with highest score
		if (!found) {
			for (NTerm nTermTemp : nTerms) {
				if (nTermTemp.getSequencePeptide().startsWith("M")) {
					if (maxScore < nTermTemp.getScore()) {
						maxScore = nTermTemp.getScore();
						this.setnTermRef(nTermTemp);
						found = true;
					}
				}
			}
		}

		// - Peptide without methyl group with highest score
		if (!found) {
			for (NTerm nTermTemp : nTerms) {
				if (maxScore < nTermTemp.getScore()) {
					maxScore = nTermTemp.getScore();
					this.setnTermRef(nTermTemp);
					found = true;
				}
			}
		}

	}

	/**
	 * Find the total sequence of all peptides
	 */
	public void findRefSequence() {
		ArrayList<String> listSeq = new ArrayList<String>();
		for (NTerm nTermTemp : nTerms) {
			listSeq.add(nTermTemp.getSequencePeptide());
		}
		String refSeq = ListUtils.unionOFString(listSeq);
		if (refSeq.equals("false")) {
			refSeq = "";
			for (String seq : listSeq) {
				if (seq.length() > refSeq.length()) {
					refSeq = seq;
				}
			}
		}
		this.setRefSequence(refSeq);
	}

	/**
	 * List the type of gene overlap of each NTerm
	 */
	public void findOverlaps() {
		ArrayList<String> listOverlap = new ArrayList<>();
		for (NTerm nTermTemp : nTerms) {
			if (nTermTemp.getTypeOverlap().split(" - ").length > 0) {
				for (String overlap : nTermTemp.getTypeOverlap().split(" - ")) {
					if (!listOverlap.contains(overlap)) {
						listOverlap.add(overlap);
					}
				}
			} else {
				if (!listOverlap.contains(nTermTemp.getTypeOverlap())) {
					listOverlap.add(nTermTemp.getTypeOverlap());
				}
			}
		}
		// System.out.println(listOverlap);
		this.setOverlaps(listOverlap);
	}

	/**
	 * List the type of modification overlap of each NTerm
	 */
	public void findModifs() {
		ArrayList<TypeModif> listModifs = new ArrayList<TypeModif>();
		for (NTerm nTermTemp : nTerms) {
			if (!listModifs.contains(nTermTemp.getTypeModif())) {
				listModifs.add(nTermTemp.getTypeModif());
			}
		}
		this.setModifs(listModifs);
	}

	/**
	 * Return if the NTerms of this TIS were detected in the first experiment, or
	 * the second, or both
	 * 
	 * @return
	 */
	public String getExperiment() {
		String experiment = "";
		for (NTerm nTerm : this.getnTerms()) {
			String experimentNTerm = nTerm.getExperiment();
			if (experimentNTerm.contains("Experiment_1;")) {
				if (!experiment.contains("Experiment_1;")) {
					experiment = "Experiment_1;";
				}
			}
			if (experimentNTerm.contains("Experiment_2;")) {
				if (!experiment.contains("Experiment_2;")) {
					experiment += "Experiment_2;";
				}
			}
		}
		return experiment;
	}

	/**
	 * Search if the TIS was found formylated in the first experiment, or the
	 * second, or both
	 * 
	 * @return
	 */
	public String getFormylatedExperiment() {
		String experiment = "";
		for (NTerm nTerm : this.getnTerms()) {
			if (nTerm.getTypeModif() == TypeModif.For) {
				String experimentNTerm = nTerm.getExperiment();
				if (experimentNTerm.contains("Experiment_1;")) {
					if (!experiment.contains("For_Experiment_1;")) {
						experiment = "For_Experiment_1;";
					}
				}
				if (experimentNTerm.contains("Experiment_2;")) {
					if (!experiment.contains("For_Experiment_2;")) {
						experiment += "For_Experiment_2;";
					}
				}
			}
		}
		return experiment;
	}

	/**
	 * Return true if the TIS includes a formylated NTerm
	 * 
	 * @return
	 */
	public boolean isFormylated() {
		for (NTerm nTerm : this.getnTerms()) {
			if (nTerm.getTypeModif() == TypeModif.For) {
				return true;
			}
		}
		return false;
	}

	public int getUTRLength() {
		int utrLength = getnTerms().get(0).getTssUpDistance();
		return utrLength;
	}

	/*
	 * ********************************** Serialization
	 * **********************************
	 */

	/**
	 * Read compressed, serialized data with a FileInputStream. Uncompress that data
	 * with a GZIPInputStream. Deserialize the vector of lines with a
	 * ObjectInputStream. Replace current data with new data, and redraw everything.
	 */
	public static Sequence load(String fileName) {
		try {
			// Create necessary input streams
			FileInputStream fis = new FileInputStream(fileName); // Read from file
			GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
			ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
			// Read in an object. It should be a vector of scribbles
			TIS seq = (TIS) in.readObject();
			in.close();
			return seq;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} // Close the stream.
	}

	public String getRefSequence() {
		return refSequence;
	}

	public void setRefSequence(String refSequence) {
		this.refSequence = refSequence;
	}

	public ArrayList<NTerm> getnTerms() {
		return nTerms;
	}

	/**
	 * Return a character string with all peptide names
	 * 
	 * @return
	 */
	public String getnTermsString() {
		String result = "";
		for (NTerm nTerm : getnTerms()) {
			result += nTerm.getName() + ";";
		}
		return result;
	}

	/**
	 * Return a character string with all peptide Modif sequences
	 * 
	 * @return
	 */
	public String getnTermsSequences() {
		String result = "";
		for (NTerm nTerm : getnTerms()) {
			result += nTerm.getModifSequence() + ";";
		}
		return result;
	}

	public void setnTerms(ArrayList<NTerm> nTerms) {
		this.nTerms = nTerms;
	}

	public ArrayList<String> getOverlaps() {
		return overlaps;
	}

	public String getOverlapsString() {
		String result = "";
		for (String overlap : getOverlaps()) {
			result += overlap + ";";
		}
		return result;
	}

	public void setOverlaps(ArrayList<String> overlaps) {
		this.overlaps = overlaps;
	}

	public String getOverlapingGene() {
		return overlapingGene;
	}

	public void setOverlapingGene(String overlapingGene) {
		this.overlapingGene = overlapingGene;
	}

	public ArrayList<TypeModif> getModifs() {
		return modifs;
	}

	public String getModifsString() {
		String result = "";
		for (TypeModif modif : getModifs()) {
			result += modif + ";";
		}
		return result;
	}

	public void setModifs(ArrayList<TypeModif> modifs) {
		this.modifs = modifs;
	}

	public NTerm getnTermRef() {
		return nTermRef;
	}

	public void setnTermRef(NTerm nTermRef) {
		this.nTermRef = nTermRef;
	}

	public int getTotalSpectra() {
		return totalSpectra;
	}

	public void setTotalSpectra(int totalSpectra) {
		this.totalSpectra = totalSpectra;
	}

}
