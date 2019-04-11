package bacnet.scripts.listeriomics.nterm;

import java.io.File;
import java.util.ArrayList;

import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.proteomics.NTerm.TypeModif;
import bacnet.datamodel.proteomics.NTermCreateData;
import bacnet.datamodel.proteomics.TIS;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;

/**
 * Genertal class for creation of NTerminomics tables
 * 
 * @author christophebecavin
 *
 */
public class NTermCreateFinalData {

	private NTermData massSpecData;
	private Genome genome;
	private String nameMassSpecDataFinal = "";
	private NTermData massSpecDataNew;
	private ArrayList<NTerm> allNterms = new ArrayList<>();
	public static String PATH_LIST = NTermCreateData.PATH_RESULTS + "FrancisFilter_19052015" + File.separator;
	private int i = 1;

	/**
	 * From the list created by Francis, update all Nterm peptides, indicating which
	 * type of TIS (translation initiation sites) they are: annotated TIS multiple
	 * TIS updated TIS internal TIS
	 * 
	 * @param nameMassSpecData
	 * @param nameMassSpecDataFinal
	 */
	public static void run(String nameMassSpecData, String nameMassSpecDataFinal) {
		new NTermCreateFinalData(nameMassSpecData, nameMassSpecDataFinal);
	}

	/**
	 * From the list created by Francis, update all Nterm peptides, indicating which
	 * type of TIS (translation initiation sites) they are: annotated TIS multiple
	 * TIS updated TIS internal TIS
	 * 
	 * @param nameMassSpecData
	 * @param nameMassSpecDataFinal
	 */
	public NTermCreateFinalData(String nameMassSpecData, String nameMassSpecDataFinal) {
		this.massSpecData = NTermData.load(nameMassSpecData);
		this.setNameMassSpecDataFinal(nameMassSpecDataFinal);
		this.genome = Genome.loadEgdeGenome();

		/*
		 * Create new data
		 */
		this.massSpecDataNew = new NTermData(nameMassSpecDataFinal, massSpecData.getElements());
		massSpecDataNew.setType(TypeData.NTerm);
		massSpecDataNew.setBioCondName(nameMassSpecDataFinal);

		// get a list of all Nterms, and modify their overlap category
		for (NTerm nTerm : massSpecDataNew.getElements().values()) {
			nTerm.setTypeOverlap("uncategorized");
			massSpecDataNew.getNTerms().add(nTerm);
			allNterms.add(nTerm);
		}

		/*
		 * fill each category of NTzerms for the final classification
		 */
		getATIS();
		getMultipleTIS();
		getUpdatedTIS();
		getInternalTIS();
		getNewTIS();

		getUncategorized();

		verifyTIS();

		massSpecDataNew.createAnnotation();
		massSpecDataNew.save();

		/*
		 * Create summary tables for the paper
		 */
		// createATISTable();
	}

	/**
	 * Load list of gene having an updated TIS peptide and search for every NTerm
	 * which are exactly at the beginning of the gene or +3 nucleotide after
	 */
	private void getNewTIS() {
		String overlapType = NTermData.TYPE_OVERLAPS[4];
		massSpecData.getTypeOverlaps().add(overlapType);
		String[][] multipleTISGenes = TabDelimitedTableReader.read(PATH_LIST + "newTIS_Francis_19052015.txt");
		System.out.println(multipleTISGenes.length + " : new TIS read");

		/*
		 * For each gene find the corresponding aTIS
		 */
		for (int j = 0; j < multipleTISGenes.length; j++) {
			int startalternateNTerm = Integer.parseInt(multipleTISGenes[j][0]);

			/*
			 * For alternative TIS position get NTerms and update their overlap value and
			 * create a TIS for them
			 */
			ArrayList<NTerm> aTISGroup = new ArrayList<>();
			String tisName = "TIS_" + i;
			@SuppressWarnings("unchecked")
			ArrayList<NTerm> allNtermTemp = (ArrayList<NTerm>) allNterms.clone();
			for (NTerm nTerm : allNtermTemp) {
				if (nTerm.isStrand()) {
					if (nTerm.getBegin() == startalternateNTerm) {
						System.out.println(
								nTerm.getName() + " " + startalternateNTerm + " " + nTerm.getSequencePeptide());
						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					} else if (nTerm.getBegin() == startalternateNTerm + 3) {
						System.out.println(
								nTerm.getName() + " " + startalternateNTerm + " " + nTerm.getSequencePeptide());
						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				} else {
					if (nTerm.getEnd() == startalternateNTerm) {
						System.out.println(
								nTerm.getName() + " " + startalternateNTerm + " " + nTerm.getSequencePeptide());

						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					} else if (nTerm.getEnd() == startalternateNTerm - 3) {
						System.out.println(
								nTerm.getName() + " " + startalternateNTerm + " " + nTerm.getSequencePeptide());

						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				}
			}
			/*
			 * Create one TIS for each multiple TIS, if necessary
			 */
			if (aTISGroup.size() != 0) {

				TIS tis = new TIS();
				tis.setName(tisName);
				tis.setnTerms(aTISGroup);
				tis.findPosition();
				tis.findModifs();
				tis.findOverlaps();
				tis.findRefSequence();
				tis.findNTermRef();
				massSpecDataNew.getTisList().add(tis);
				for (NTerm nTermTemp : aTISGroup) {
					massSpecDataNew.getTisMap().put(nTermTemp, tis);
				}
				i++;
			}
		}
	}

	/**
	 * Load list of gene having an updated TIS peptide and search for every NTerm
	 * which are exactly at the beginning of the gene or +3 nucleotide after
	 */
	private void getInternalTIS() {
		String overlapType = NTermData.TYPE_OVERLAPS[3];
		massSpecData.getTypeOverlaps().add(overlapType);
		String[][] multipleTISGenes = TabDelimitedTableReader.read(PATH_LIST + "internalTIS_Francis_19052015.txt");
		System.out.println(multipleTISGenes.length + " : internalTIS read");

		/*
		 * For each gene find the corresponding aTIS
		 */
		for (int j = 0; j < multipleTISGenes.length; j++) {
			String gene = multipleTISGenes[j][0];
			int startalternateNTerm = Integer.parseInt(multipleTISGenes[j][1]);

			/*
			 * For alternative TIS position get NTerms and update their overlap value and
			 * create a TIS for them
			 */
			ArrayList<NTerm> aTISGroup = new ArrayList<>();
			String tisName = "TIS_" + i;
			@SuppressWarnings("unchecked")
			ArrayList<NTerm> allNtermTemp = (ArrayList<NTerm>) allNterms.clone();
			for (NTerm nTerm : allNtermTemp) {
				if (nTerm.isStrand()) {
					if (nTerm.getBegin() == startalternateNTerm) {
						System.out.println(gene);
						System.out.println(nTerm.getName() + " " + gene + " " + nTerm.getSequencePeptide());
						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					} else if (nTerm.getBegin() == startalternateNTerm + 3) {
						System.out.println(gene);
						System.out.println(nTerm.getName() + " " + gene + " " + nTerm.getSequencePeptide());
						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				} else {
					if (nTerm.getEnd() == startalternateNTerm) {
						System.out.println(gene);
						System.out.println(nTerm.getName() + " " + gene + " " + nTerm.getSequencePeptide());

						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					} else if (nTerm.getEnd() == startalternateNTerm - 3) {
						System.out.println(gene);
						System.out.println(nTerm.getName() + " " + gene + " " + nTerm.getSequencePeptide());

						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				}
			}
			/*
			 * Create one TIS for each multiple TIS, if necessary
			 */
			if (aTISGroup.size() != 0) {

				TIS tis = new TIS();
				tis.setName(tisName);
				tis.setnTerms(aTISGroup);
				tis.setOverlapingGene(gene);
				tis.findPosition();
				tis.findModifs();
				tis.findOverlaps();
				tis.findRefSequence();
				tis.findNTermRef();
				massSpecDataNew.getTisList().add(tis);
				for (NTerm nTermTemp : aTISGroup) {
					massSpecDataNew.getTisMap().put(nTermTemp, tis);
				}
				i++;
			}
		}
	}

	/**
	 * Load list of gene having an updated TIS peptide and search for every NTerm
	 * which are exactly at the beginning of the gene or +3 nucleotide after
	 */
	private void getUpdatedTIS() {
		String overlapType = NTermData.TYPE_OVERLAPS[2];
		massSpecData.getTypeOverlaps().add(overlapType);
		String[][] multipleTISGenes = TabDelimitedTableReader.read(PATH_LIST + "updatedTIS_Francis_19052015.txt");
		System.out.println(multipleTISGenes.length + " : updatedTIS read");

		/*
		 * For each gene find the corresponding aTIS
		 */
		for (int j = 0; j < multipleTISGenes.length; j++) {
			String gene = multipleTISGenes[j][0];
			int startalternateNTerm = Integer.parseInt(multipleTISGenes[j][1]);

			/*
			 * For alternative TIS position get NTerms and update their overlap value and
			 * create a TIS for them
			 */
			ArrayList<NTerm> aTISGroup = new ArrayList<>();
			String tisName = "TIS_" + i;
			@SuppressWarnings("unchecked")
			ArrayList<NTerm> allNtermTemp = (ArrayList<NTerm>) allNterms.clone();
			for (NTerm nTerm : allNtermTemp) {
				if (nTerm.isStrand()) {
					if (nTerm.getBegin() == startalternateNTerm) {
						// System.out.println(gene);
						// System.out.println(nTerm.getName()+" "+gene+" "+nTerm.getSequencePeptide());
						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					} else if (nTerm.getBegin() == startalternateNTerm + 3) {
						// System.out.println(gene);
						// System.out.println(nTerm.getName()+" "+gene+" "+nTerm.getSequencePeptide());
						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				} else {
					if (nTerm.getEnd() == startalternateNTerm) {
						// System.out.println(gene);
						// System.out.println(nTerm.getName()+" "+gene+" "+nTerm.getSequencePeptide());

						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					} else if (nTerm.getEnd() == startalternateNTerm - 3) {
						// System.out.println(gene);
						// System.out.println(nTerm.getName()+" "+gene+" "+nTerm.getSequencePeptide());

						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				}
			}
			/*
			 * Create one TIS for each multiple TIS, if necessary
			 */
			if (aTISGroup.size() != 0) {

				TIS tis = new TIS();
				tis.setName(tisName);
				tis.setnTerms(aTISGroup);
				tis.setOverlapingGene(gene);
				tis.findPosition();
				tis.findModifs();
				tis.findOverlaps();
				tis.findRefSequence();
				tis.findNTermRef();
				massSpecDataNew.getTisList().add(tis);
				for (NTerm nTermTemp : aTISGroup) {
					massSpecDataNew.getTisMap().put(nTermTemp, tis);
				}
				i++;
			}
		}
	}

	/**
	 * Load list of gene having a multiple TIS peptide and search for every NTerm
	 * which are exactly at the beginning of the gene or +3 nucleotide after
	 */
	private void getMultipleTIS() {
		String overlapType = NTermData.TYPE_OVERLAPS[1];
		massSpecData.getTypeOverlaps().add(overlapType);
		String[][] multipleTISGenes = TabDelimitedTableReader.read(PATH_LIST + "multipleTIS_Francis_19052015.txt");
		System.out.println(multipleTISGenes.length + " : multipleTIS read");

		/*
		 * For each gene find the corresponding aTIS
		 */
		for (int j = 0; j < multipleTISGenes.length; j++) {
			String gene = multipleTISGenes[j][0];
			int startalternateNTerm = Integer.parseInt(multipleTISGenes[j][2]);
			/*
			 * From gene name get corresponding TIS
			 */
			for (TIS tis : massSpecDataNew.getTisList()) {
				if (tis.getOverlapingGene().equals(gene)
						&& tis.getOverlapsString().contains(NTermData.TYPE_OVERLAPS[0])) {
					// System.out.println(tis.getName()+" : "+tis.getOverlapingGene());
					ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
					for (NTerm nTerm : tis.getnTerms()) {
						System.out.println(tis.getnTerms().size() + " " + tis.getName());
						boolean found = false;
						if (nTerm.isStrand()) {
							if (nTerm.getBegin() == startalternateNTerm) {
								found = true;
							}
						} else {
							if (nTerm.getEnd() == startalternateNTerm) {
								found = true;
							}
						}
						if (!found) {
							nTerm.setTypeOverlap(NTermData.TYPE_OVERLAPS[0] + " - " + NTermData.TYPE_OVERLAPS[1]);
							nTerms.add(nTerm);
						}
					}
					System.out.println("Find aTIS" + gene);
					i++;
					tis.setnTerms(nTerms);
					tis.setOverlapingGene(gene);
					tis.findPosition();
					tis.findModifs();
					tis.findOverlaps();
					tis.findRefSequence();
					tis.findNTermRef();
				}

			}

			/*
			 * For alternative TIS position get NTerms and update their overlap value and
			 * create a TIS for them
			 */
			ArrayList<NTerm> aTISGroup = new ArrayList<>();
			String tisName = "TIS_" + i;
			for (NTerm nTerm : massSpecDataNew.getElements().values()) {
				if (nTerm.isStrand()) {
					if (nTerm.getBegin() == startalternateNTerm) {
						// System.out.println(gene+" "+startGene+" "+startalternateNTerm);
						// System.out.println(nTerm.getName()+" "+gene+" "+nTerm.getSequencePeptide());
						System.out.println("multipleTIS: " + nTerm.getName() + " " + nTerm.getTISName());
						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				} else {
					if (nTerm.getEnd() == startalternateNTerm) {
						System.out.println("multipleTIS: " + gene + nTerm.getName() + " " + nTerm.getTISName());

						nTerm.setTypeOverlap(overlapType);
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				}
			}
			/*
			 * Create one TIS for each multiple TIS, if necessary
			 */
			if (aTISGroup.size() != 0) {
				TIS tis = new TIS();
				tis.setName(tisName);
				tis.setnTerms(aTISGroup);
				tis.setOverlapingGene(gene);
				tis.findPosition();
				tis.findModifs();
				tis.findOverlaps();
				tis.findRefSequence();
				tis.findNTermRef();
				massSpecDataNew.getTisList().add(tis);
				for (NTerm nTermTemp : aTISGroup) {
					massSpecDataNew.getTisMap().put(nTermTemp, tis);
				}
				i++;
			}
		}
	}

	/**
	 * Load list of gene having an aTIS peptide and search for every NTerm which are
	 * exactly at the beginning of the gene or +3 nucleotide after
	 */
	private void getATIS() {
		String overlapType = NTermData.TYPE_OVERLAPS[0];
		massSpecData.getTypeOverlaps().add(overlapType);
		ArrayList<String> aTISGenes = TabDelimitedTableReader.readList(PATH_LIST + "aTIS_Francis_19052015.txt");
		System.out.println(aTISGenes.size() + " : aTIS read");
		/*
		 * For each gene find the corresponding aTIS
		 */
		String[][] annot = massSpecData.getAnnotation().getAnnotation();
		for (String lmo : aTISGenes) {
			Gene gene = genome.getGeneFromName(lmo);
			ArrayList<NTerm> aTISGroup = new ArrayList<>();
			/*
			 * Create one TIS for each lmo
			 */
			TIS tis = new TIS();
			String tisName = "TIS_" + i;
			tis.setName(tisName);

			/*
			 * Create NTerms //System.out.println("Search all NTerms for : "+lmo);
			 */
			if (gene.isStrand()) {
				for (int i = 1; i < annot.length; i++) {
					int begin = (int) (Double.parseDouble(annot[i][1]));
					if (begin == gene.getBegin()) {
						String nTermName = annot[i][0];
						NTerm nTerm = massSpecDataNew.getElements().get(nTermName);
						nTerm.setTypeOverlap(overlapType);
						nTerm.setOverlap(lmo + " : 0");
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					} else if (begin == gene.getBegin() + 3) {
						String nTermName = annot[i][0];
						NTerm nTerm = massSpecDataNew.getElements().get(nTermName);
						nTerm.setTypeOverlap(overlapType);
						nTerm.setOverlap(lmo + " : 3");
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				}
			} else {
				for (int i = 1; i < annot.length; i++) {
					int end = (int) (Double.parseDouble(annot[i][2]));
					if (end == gene.getEnd()) {
						String nTermName = annot[i][0];
						NTerm nTerm = massSpecDataNew.getElements().get(nTermName);
						nTerm.setTypeOverlap(overlapType);
						nTerm.setOverlap(lmo + " : 0");
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					} else if (end == gene.getEnd() - 3) {
						String nTermName = annot[i][0];
						NTerm nTerm = massSpecDataNew.getElements().get(nTermName);
						nTerm.setTypeOverlap(overlapType);
						nTerm.setOverlap(lmo + " : 3");
						nTerm.setTISName(tisName);
						aTISGroup.add(nTerm);
						allNterms.remove(nTerm);
						/*
						 * Remove the duplicate mapping if existing
						 */
						for (String nTermDuplicateName : nTerm.getDuplicates()) {
							NTerm nTermDuplicate = massSpecDataNew.getElements().get(nTermDuplicateName);
							allNterms.remove(nTermDuplicate);
						}
					}
				}
			}

			if (aTISGroup.size() == 0) {
				System.err.println("Cannot find aTIS for: " + lmo);
			}
			/*
			 * Create one TIS for each lmo
			 */
			tis.setnTerms(aTISGroup);
			tis.setOverlapingGene(lmo);
			tis.findPosition();
			tis.findModifs();
			tis.findOverlaps();
			tis.findRefSequence();
			tis.findNTermRef();
			massSpecDataNew.getTisList().add(tis);
			for (NTerm nTermTemp : aTISGroup) {
				massSpecDataNew.getTisMap().put(nTermTemp, tis);
			}
			i++;
		}
	}

	/**
	 * Verify of all Nterm has been associated to a TIS element
	 */
	private void verifyTIS() {
		System.out.println("Classify " + massSpecDataNew.getElements().size() + " NTerms in TIS - we have "
				+ massSpecDataNew.getTisMap().size() + " classified");
		for (NTerm nTerm : massSpecDataNew.getTisMap().keySet()) {
			if (massSpecDataNew.getTisMap().get(nTerm) == null) {
				System.out.println(nTerm.getName() + " is associated to a null TIS");
			}
		}
	}

	/**
	 * Put all remaining <code>NTerm</code> in the "uncategorized" group
	 */
	private void getUncategorized() {
		int count = 0;
		for (NTerm nTerm : allNterms) {
			if (nTerm.getTypeOverlap().equals("uncategorized")) {
				count++;
				ArrayList<NTerm> aTISGroup = new ArrayList<>();
				aTISGroup.add(nTerm);
				TIS tis = new TIS();
				tis.setName("TIS_" + i);
				nTerm.setTISName("TIS_" + i);
				tis.setnTerms(aTISGroup);
				tis.findPosition();
				tis.findModifs();
				tis.findOverlaps();
				tis.findRefSequence();
				tis.findNTermRef();
				massSpecDataNew.getTisList().add(tis);
				massSpecDataNew.getTisMap().put(nTerm, tis);
				i++;
			} else {
				System.out.println("cannot classified: " + nTerm.getName() + " : " + nTerm.getOverlap() + ": "
						+ nTerm.getTypeOverlap());
			}
		}
		System.out.println(count + " : peptides uncategorized remaining (for a total of: "
				+ massSpecDataNew.getNTerms().size() + " peptides)");
	}
	@SuppressWarnings("unused")
	private void createATISTable() {
		ArrayList<String> results = new ArrayList<>();
		String header = "Gene\tTIS_Name\tFormylation\tFormylated?\trefSequence\tList NTermSequences found\tTotal Spectra\tNTerm Ref Seq\tNTerm ref score\tNterm ref threshold\tUTR length";
		results.add(header);
		for (TIS tis : massSpecDataNew.getTisList()) {

			if (tis.getName().equals("TIS_247")) {
				// System.out.println("sfh");
			}
			if (tis.getnTerms().get(0).getTypeOverlap().equals("aTIS")) {
				String seqPeptide = tis.getnTermRef().getSequencePeptide();
				if (tis.getnTermRef().getTypeModif() == TypeModif.For)
					seqPeptide = "For-" + seqPeptide;
				results.add(tis.getnTerms().get(0).getOverlap().split(" :")[0] + "\t" + tis.getName() + "\t"
						+ tis.getFormylatedExperiment() + "\t" + tis.isFormylated() + "\t" + tis.getRefSequence() + "\t"
						+ tis.getnTermsSequences() + "\t" + tis.getTotalSpectra() + "\t" + seqPeptide + "\t"
						+ tis.getnTermRef().getScore() + "\t" + tis.getnTermRef().getThreshold() + "\t"
						+ tis.getUTRLength());
			}
		}
		TabDelimitedTableReader.saveList(results, PATH_LIST + "/aTISSummary.txt");

	}

	public String getNameMassSpecDataFinal() {
		return nameMassSpecDataFinal;
	}

	public void setNameMassSpecDataFinal(String nameMassSpecDataFinal) {
		this.nameMassSpecDataFinal = nameMassSpecDataFinal;
	}

}
