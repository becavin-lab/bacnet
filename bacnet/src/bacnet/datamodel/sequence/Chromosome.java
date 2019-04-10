package bacnet.datamodel.sequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.ChromosomeSequence;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.DataSource;
import org.biojava3.core.sequence.compound.DNACompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.template.CompoundSet;
import org.biojava3.core.sequence.template.SequenceReader;

import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.sequence.Sequence.SeqType;
import bacnet.utils.FileUtils;

/**
 * A ChromosomeSequence is a DNASequence but keeps track of geneSequences
 * 
 * @author Scooter Willis
 * 
 *         Here I modified the original Chromosome class to one which include:
 *         coding and noncoding regions + proteins
 * @author Christophe Becavin
 * 
 * 
 */
public class Chromosome extends ChromosomeSequence {

	/**
	 * Parent Genome where this chromosome is placed
	 */
	private Genome parentGenome = new Genome();

	private String name = "NULL";

	private String chromosomeID = "";
	/**
	 * List of the element in the chromosome and their positions
	 */
	private Annotation annotation = new Annotation();

	private LinkedHashMap<String, Sequence> allElements = new LinkedHashMap<String, Sequence>();
	private LinkedHashMap<String, Gene> genes = new LinkedHashMap<String, Gene>();
	private LinkedHashMap<String, NcRNA> ncRNAs = new LinkedHashMap<String, NcRNA>();
	private LinkedHashMap<String, Gene> genesAlternative = new LinkedHashMap<String, Gene>();

	private LinkedHashMap<String, Operon> operons = new LinkedHashMap<String, Operon>();
	private LinkedHashMap<String, Srna> sRNAs = new LinkedHashMap<String, Srna>();
	private LinkedHashMap<String, Srna> asRNAs = new LinkedHashMap<String, Srna>();
	private LinkedHashMap<String, Srna> cisRegs = new LinkedHashMap<String, Srna>();
	private LinkedHashMap<String, Sequence> elements = new LinkedHashMap<String, Sequence>();

	private LinkedHashMap<String, String> locusTagToGeneNameMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> geneNameToLocusTagMap = new LinkedHashMap<String, String>();

	public Chromosome() {
	}

	public Chromosome(String seqString) {
		super(seqString, DNACompoundSet.getDNACompoundSet());
	}

	public Chromosome(SequenceReader<NucleotideCompound> proxyLoader) {
		super(proxyLoader, DNACompoundSet.getDNACompoundSet());
	}

	public Chromosome(String seqString, CompoundSet<NucleotideCompound> compoundSet) {
		super(seqString, compoundSet);
	}

	public Chromosome(SequenceReader<NucleotideCompound> proxyLoader, CompoundSet<NucleotideCompound> compoundSet) {
		super(proxyLoader, compoundSet);
	}

	/**
	 * Void a Chromosome when Website is quit USEFUL in Eclipse RAP
	 */
	public void clearChromosome() {
		Annotation annot = this.getAnnotation();
		annot.setAnnotation(new String[0][0]);
		this.getAllElements().clear();
		this.getGenes().clear();
		this.getNcRNAs().clear();
		this.getGenesAlternative().clear();
		this.getOperons().clear();
		this.getsRNAs().clear();
		this.getAsRNAs().clear();
		this.getCisRegs().clear();
		this.getElements().clear();
		this.getLocusTagToGeneNameMap().clear();
		this.getGeneNameToLocusTagMap().clear();
		DNASequence dnaSequence = new DNASequence("A");
		Chromosome chromoNew = new Chromosome(dnaSequence.getProxySequenceReader());
		this.setProxySequenceReader(chromoNew.getProxySequenceReader());
		this.setParentSequence(chromoNew.getParentSequence());
	}

	public static LinkedHashMap<String, Chromosome> getChromosomeFromDNASequence(
			LinkedHashMap<String, DNASequence> dnaSequenceList, Genome genome, int k, String fileName) {
		LinkedHashMap<String, Chromosome> chromosomeSequenceList = new LinkedHashMap<String, Chromosome>();
		for (String key : dnaSequenceList.keySet()) {
			DNASequence dnaSequence = dnaSequenceList.get(key);
			Chromosome chromosome = new Chromosome(dnaSequence.getProxySequenceReader()); // we want the underlying
																							// sequence but don't need
																							// storage
			String accession = dnaSequence.getAccession().toString();
			if (accession.equals("NULL")) {
				accession = FileUtils.removeExtensionAndPath(fileName);
				chromosome.setAccession(new AccessionID(accession, DataSource.NCBI));
			} else {
				chromosome.setAccession(dnaSequence.getAccession());
			}
			chromosome.setChromosomeID(accession);
			chromosome.setDescription(dnaSequence.getDescription());
			chromosome.setChromosomeNumber(k);
			if (dnaSequence.getDescription().split(",").length == 2) {
				String name = dnaSequence.getDescription().split(",")[1].trim();
				chromosome.setName(name);
			}

			chromosome.setDNAType(DNAType.CHROMOSOME);

			if (chromosome.getLength() < 50000)
				chromosome.setDNAType(DNAType.PLASMID);
			if (chromosome.getLength() < 20000)
				chromosome.setDNAType(DNAType.CONTIG);
			if (chromosome.getDescription().contains("contig"))
				chromosome.setDNAType(DNAType.CONTIG);
			chromosome.setParentGenome(genome);
			/*
			 * Does not include CONTIG !!!!!!!
			 * 
			 */
			if (chromosome.getDNAType() != DNAType.CONTIG) {
				chromosomeSequenceList.put(chromosome.getAccession().toString(), chromosome);
			}
			k++;
		}
		return chromosomeSequenceList;
	}

	/**
	 * Read Annotation and from the list of sequences obtained, create the different
	 * LinkedHashMap<br>
	 * This method is run at the construction of a Genome object
	 * 
	 * @param path
	 */
	public void setArrayListWithAnnotation(String path) {
		String[][] annot = getAnnotation().getAnnotation();

		for (int i = 1; i < annot.length; i++) {
			String name = annot[i][Annotation.getNameColumn()];
			SeqType type = SeqType.valueOf(annot[i][Annotation.getTypeColumn()]);
			// System.out.println(name);
			// System.out.println(type);
			switch (type) {
			case Gene:
				Gene gene = Gene.load(path + name);
				if (gene.getComment().contains("RAST")) {
					getGenesAlternative().put(name, gene);
					getAllElements().put(name, gene);
				} else {
					getGenes().put(name, gene);
					getAllElements().put(name, gene);
					if (!gene.getGeneName().equals("-")) {
						getLocusTagToGeneNameMap().put(name, gene.getGeneName());
						getGeneNameToLocusTagMap().put(gene.getGeneName(), name);
					}
				}
				break;
			case NcRNA:
				NcRNA ncRNA = NcRNA.load(path + name);
				getNcRNAs().put(name, ncRNA);
				getAllElements().put(name, ncRNA);
				break;
			case Srna:
				Srna sRna = Srna.load(path + name);
				switch (sRna.getTypeSrna()) {
				case Srna:
					getsRNAs().put(name, sRna);
					break;
				case CisReg:
					getCisRegs().put(name, sRna);
					break;
				case ASrna:
					getAsRNAs().put(name, sRna);
					break;
				}
				getAllElements().put(name, sRna);
				break;
			case Operon:
				Operon operon = Operon.load(path + name);
				getOperons().put(name, operon);
				getAllElements().put(name, operon);
				break;
			default:
				if (!name.equals("No gene")) {
					Sequence seq = Sequence.load(path + name);
					getElements().put(name, seq);
					getAllElements().put(name, seq);
				}
				break;
			}

		}
	}

	/**
	 * Given a sequence name, return the corresponding Sequence<br>
	 * If name is a Gene name, we search also in geneNameToLocusTagMap
	 * 
	 * @param name
	 * @return
	 */
	public Sequence getSequence(String name) {
		if (getAllElements().containsKey(name)) {
			return getAllElements().get(name);
		} else if (getGeneNameToLocusTagMap().containsKey(name)) {
			return getAllElements().get(getGeneNameToLocusTagMap().get(name));
		} else
			return null;
	}

	/**
	 * Return a List with all name of genes
	 * 
	 * @return
	 */
	public ArrayList<String> getGeneNameList() {
		ArrayList<String> geneNames = new ArrayList<>();
		for (String key : this.getGenes().keySet()) {
			geneNames.add(key);
		}
		return geneNames;
	}

	/**
	 * Return the gene downstream
	 * 
	 * @param gene
	 * @return
	 */
	public Gene getGeneDownstream(Gene gene) {
		int index = this.getGeneNameList().indexOf(gene.getName());
		if (index != -1) {
			if (gene.isStrand()) {
				Gene geneDonwstream = this.getGenes().get(this.getGeneNameList().get(index + 1));
				return geneDonwstream;
			} else {
				Gene geneDonwstream = this.getGenes().get(this.getGeneNameList().get(index - 1));
				return geneDonwstream;
			}
		}
		return null;
	}

	/**
	 * Return the gene upstream
	 * 
	 * @param gene
	 * @return
	 */
	public Gene getGeneUpstream(Gene gene) {
		int index = this.getGeneNameList().indexOf(gene.getName());
		if (index != -1) {
			if (gene.isStrand()) {
				Gene geneUpstream = this.getGenes().get(this.getGeneNameList().get(index - 1));
				return geneUpstream;
			} else {
				Gene geneUpstream = this.getGenes().get(this.getGeneNameList().get(index + 1));
				return geneUpstream;
			}
		}
		return null;
	}

	/*
	 * *****************************************************************************
	 * ***************************************** Getters and Setters
	 * *****************************************************************************
	 * *****************************************
	 */

	public LinkedHashMap<String, Gene> getGenesAlternative() {
		return genesAlternative;
	}

	public void setGenesAlternative(LinkedHashMap<String, Gene> genesAlternative) {
		this.genesAlternative = genesAlternative;
	}

	public Genome getParentGenome() {
		return parentGenome;
	}

	public void setParentGenome(Genome parentGenome) {
		this.parentGenome = parentGenome;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	public LinkedHashMap<String, Sequence> getAllElements() {
		return allElements;
	}

	public void setAllElements(LinkedHashMap<String, Sequence> allElements) {
		this.allElements = allElements;
	}

	public LinkedHashMap<String, Gene> getGenes() {
		return genes;
	}

	public void setGenes(LinkedHashMap<String, Gene> genes) {
		this.genes = genes;
	}

	public LinkedHashMap<String, NcRNA> getNcRNAs() {
		return ncRNAs;
	}

	public void setNcRNAs(LinkedHashMap<String, NcRNA> ncRNAs) {
		this.ncRNAs = ncRNAs;
	}

	public LinkedHashMap<String, Operon> getOperons() {
		return operons;
	}

	public void setOperons(LinkedHashMap<String, Operon> operons) {
		this.operons = operons;
	}

	public LinkedHashMap<String, Srna> getsRNAs() {
		return sRNAs;
	}

	public void setsRNAs(LinkedHashMap<String, Srna> sRNAs) {
		this.sRNAs = sRNAs;
	}

	public LinkedHashMap<String, Srna> getAsRNAs() {
		return asRNAs;
	}

	public void setAsRNAs(LinkedHashMap<String, Srna> asRNAs) {
		this.asRNAs = asRNAs;
	}

	public LinkedHashMap<String, Srna> getCisRegs() {
		return cisRegs;
	}

	public void setCisRegs(LinkedHashMap<String, Srna> cisRegs) {
		this.cisRegs = cisRegs;
	}

	public LinkedHashMap<String, Sequence> getElements() {
		return elements;
	}

	public void setElements(LinkedHashMap<String, Sequence> elements) {
		this.elements = elements;
	}

	public LinkedHashMap<String, String> getLocusTagToGeneNameMap() {
		return locusTagToGeneNameMap;
	}

	public void setLocusTagToGeneNameMap(LinkedHashMap<String, String> locusTagToGeneNameMap) {
		this.locusTagToGeneNameMap = locusTagToGeneNameMap;
	}

	public LinkedHashMap<String, String> getGeneNameToLocusTagMap() {
		return geneNameToLocusTagMap;
	}

	public void setGeneNameToLocusTagMap(LinkedHashMap<String, String> geneNameToLocusTagMap) {
		this.geneNameToLocusTagMap = geneNameToLocusTagMap;
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
