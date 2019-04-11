package bacnet.datamodel.sequence;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.biojava3.core.sequence.ChromosomeSequence;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.compound.DNACompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.template.CompoundSet;
import org.biojava3.core.sequence.template.SequenceReader;

import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.sequence.Sequence.SeqType;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;

/**
 * A ChromosomeSequence is a DNASequence but keeps track of geneSequences
 * 
 * @author Scooter Willis
 * 
 *         Here I modified the original Chromosome class to one which include:
 *         coding and noncoding regions + proteins
 * @author Christophe B�cavin
 * 
 * 
 */
public class ChromosomeBacteriaSequence extends ChromosomeSequence {

	private String name = "NULL";

	private GenomeNCBI parentGenome = new GenomeNCBI();

	private String chromosomeID = "";

	private LinkedHashMap<String, DNASequence> codingSequenceHashMap = new LinkedHashMap<String, DNASequence>();
	private LinkedHashMap<String, DNASequence> noncodingSequenceHashMap = new LinkedHashMap<String, DNASequence>();
	private LinkedHashMap<String, String> geneNameToLocusTagMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> locusTagToGeneNameMap = new LinkedHashMap<String, String>();

	/**
	 * List of sRNAs of EGDe
	 */
	private ArrayList<Srna> sRNAs = new ArrayList<Srna>();
	/**
	 * List of sRNAs cisRegs of EGDe
	 */
	private ArrayList<Srna> asRNAs = new ArrayList<Srna>();
	/**
	 * List of asRNAs of EGDe
	 */
	private ArrayList<Srna> cisRegs = new ArrayList<Srna>();
	/**
	 * List of Operons of EGDe
	 */
	private ArrayList<Operon> operons = new ArrayList<Operon>();
	/**
	 * List of other elements : terminator, etc...
	 */
	private ArrayList<Sequence> elements = new ArrayList<Sequence>();

	public ChromosomeBacteriaSequence() {
	}

	public ChromosomeBacteriaSequence(String seqString) {
		super(seqString, DNACompoundSet.getDNACompoundSet());
	}

	public ChromosomeBacteriaSequence(SequenceReader<NucleotideCompound> proxyLoader) {
		super(proxyLoader, DNACompoundSet.getDNACompoundSet());
	}

	public ChromosomeBacteriaSequence(String seqString, CompoundSet<NucleotideCompound> compoundSet) {
		super(seqString, compoundSet);
	}

	public ChromosomeBacteriaSequence(SequenceReader<NucleotideCompound> proxyLoader,
			CompoundSet<NucleotideCompound> compoundSet) {
		super(proxyLoader, compoundSet);
	}

	/**
	 * Load all serialized Srna object found in Srna.PATHSerialize
	 */
	public void loadSrnas() {
		File file = new File(Srna.PATHSerialize + File.separator);
		File[] files = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".srna"))
					return true;
				return false;
			}
		});
		System.err.println("Found " + files.length + " serialize/srna files");
		ArrayList<Srna> allSrnasTemp = new ArrayList<Srna>();
		for (File fileTemp : files) {
			Srna sRNA = Srna.load(fileTemp.getAbsolutePath());
			allSrnasTemp.add(sRNA);
		}
		// reorganize this list ordering by ids
		ArrayList<Srna> allSrnas = new ArrayList<Srna>();
		for (int i = 0; i < allSrnasTemp.size(); i++) {
			for (Srna sRNA : allSrnasTemp) {
				// System.out.println(sRNA.getName()+" " +sRNA.getId());
				if (Integer.parseInt(sRNA.getId()) == i) {
					allSrnas.add(sRNA);
				}
			}
		}

		for (Srna sRNA : allSrnas) {
			switch (sRNA.getTypeSrna()) {
			case Srna:
				sRNAs.add(sRNA);
				break;
			case ASrna:
				asRNAs.add(sRNA);
				break;
			case CisReg:
				cisRegs.add(sRNA);
				break;
			}
		}

	}

	/**
	 * Add Operons and Terminator from Annotation.EGDE_SUPPTABLE <br>
	 * This table comes directly from Wurtzel et al. annotation table
	 * 
	 * @throws IOException
	 */
	public void loadOperonsAndTerminator() throws IOException {
		File file = new File(Annotation.EGDE_SUPPTABLE);
		if (file.exists()) {
			String[][] suppData = TabDelimitedTableReader.read(Annotation.EGDE_SUPPTABLE);
			for (int i = 0; i < suppData.length; i++) {
				// System.out.println(name);
				if (suppData[i][3].contains("Operon")) {
					Operon operon = Operon.convertDNASequenceOperon(ArrayUtils.getRow(suppData, i),
							this.getParentGenome());
					operons.add(operon);
				} else if (suppData[i][3].contains("Term")) {
					String[] infos = ArrayUtils.getRow(suppData, i);
					Sequence element = new Sequence("Term_" + infos[4], Integer.parseInt(infos[0]),
							Integer.parseInt(infos[1]));
					element.setLength(element.getEnd() - element.getBegin() + 1);
					element.setType(SeqType.terminator);
					element.setComment(infos[4]);
					element.setStrand(infos[2].charAt(0));
					elements.add(element);
				}
			}
			System.out.println("Egde operons list read");
		}
	}

	/*
	 * *****************************************************************************
	 * ******************** Getters and setters
	 */
	/**
	 * Get the gene based on accession. Will return null if not found
	 * 
	 * @param accession
	 * @return
	 */
	public DNASequence getCodingRegion(String accession) {
		return codingSequenceHashMap.get(accession);
	}

	/**
	 * Get the gene based on accession. Will return null if not found
	 * 
	 * @param accession
	 * @return
	 */
	public DNASequence getNonCodingRegion(String accession) {
		return noncodingSequenceHashMap.get(accession);
	}

	/**
	 * Get the gene based on accession. Will return null if not found
	 * 
	 * @param accession
	 * @return
	 */
	public GenomeNCBI getParentGenome() {
		return parentGenome;
	}

	public void setParentGenome(GenomeNCBI parentGenome) {
		this.parentGenome = parentGenome;
	}

	public LinkedHashMap<String, DNASequence> getCodingSequenceHashMap() {
		return codingSequenceHashMap;
	}

	public void setCodingSequenceHashMap(LinkedHashMap<String, DNASequence> codingSequenceHashMap) {
		this.codingSequenceHashMap = codingSequenceHashMap;
	}

	public LinkedHashMap<String, DNASequence> getNoncodingSequenceHashMap() {
		return noncodingSequenceHashMap;
	}

	public void setNoncodingSequenceHashMap(LinkedHashMap<String, DNASequence> noncodingSequenceHashMap) {
		this.noncodingSequenceHashMap = noncodingSequenceHashMap;
	}

	public LinkedHashMap<String, String> getGeneNameToLocusTagMap() {
		return geneNameToLocusTagMap;
	}

	public void setGeneNameToLocusTagMap(LinkedHashMap<String, String> geneNameToLocusTagMap) {
		this.geneNameToLocusTagMap = geneNameToLocusTagMap;
	}

	public LinkedHashMap<String, String> getLocusTagToGeneNameMap() {
		return locusTagToGeneNameMap;
	}

	public void setLocusTagToGeneNameMap(LinkedHashMap<String, String> locusTagToGeneNameMap) {
		this.locusTagToGeneNameMap = locusTagToGeneNameMap;
	}

	public ArrayList<Srna> getsRNAs() {
		return sRNAs;
	}

	public void setsRNAs(ArrayList<Srna> sRNAs) {
		this.sRNAs = sRNAs;
	}

	public ArrayList<Srna> getAsRNAs() {
		return asRNAs;
	}

	public void setAsRNAs(ArrayList<Srna> asRNAs) {
		this.asRNAs = asRNAs;
	}

	public ArrayList<Srna> getCisRegs() {
		return cisRegs;
	}

	public void setCisRegs(ArrayList<Srna> cisRegs) {
		this.cisRegs = cisRegs;
	}

	public ArrayList<Operon> getOperons() {
		return operons;
	}

	public void setOperons(ArrayList<Operon> operons) {
		this.operons = operons;
	}

	public ArrayList<Sequence> getElements() {
		return elements;
	}

	public void setElements(ArrayList<Sequence> elements) {
		this.elements = elements;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChromosomeID() {
		return chromosomeID;
	}

	public void setChromosomeID(String chromosomeID) {
		this.chromosomeID = chromosomeID;
	}

}
