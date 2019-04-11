package bacnet.datamodel.sequence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.compound.DNACompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.io.DNASequenceCreator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.reader.FastaFileReader;
import bacnet.reader.GFFNCBIReader;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

/**
 * Class for managing Genome<br>
 * <li>Contains all Genes, non-coding RNA and all other elements found in the
 * genome GFF {@link GFFNCBIReader}
 * 
 * @author Christophe Bécavin
 *
 */
public class Genome {

	/**
	 * Name of EGD-e genome
	 */
	public static String EGDE_NAME = "Listeria monocytogenes EGD-e";
	/**
	 * Name of EGD-e chromosomegenome
	 */
	public static String EGDE_CHROMO_NAME = "NC_003210.1";

	/**
	 * Name of RAST EGD-e genome
	 */
	public static String EGDE_RAST_NAME = "RAST_Listeria_monocytogenes_EGD_e_uid61583";

	/**
	 * Default genome for Yersinia
	 */
	public static String YERSINIA_NAME = "Yersinia enterocolitica subsp enterocolitica 8081";
	/**
	 * Name of EGD genome
	 */
	public static String EGDC_NAME = "Listeria monocytogenes EGD";

	/**
	 * Name of 10403S genome
	 */
	public static String DP10403S_NAME = "Listeria monocytogenes 10403S";

	/**
	 * Other name for 10403S genome
	 */
	public static String DP10403S_NAME_BIS = "Listeria_monocytogenes_10403S_uid54461";

	/**
	 * Name for bacillus genome
	 */
	public static String BACSUBTILIS_NAME = "Bacillus_subtilis_168_uid57675";

	/**
	 * Name for Ecoli genome
	 */
	public static String ECOLI_NAME = "Escherichia coli str K-12";
	/**
	 * Name of Leishmania reference genome
	 */
	public static String Donovani_NAME = "Leishmania donovani BPK282A1 V2";
	/**
	 * Name of the bacteria
	 */
	private String species;
	/**
	 * List of chromosomes forming the genome
	 */
	private LinkedHashMap<String, Chromosome> chromosomes = new LinkedHashMap<String, Chromosome>();

	public Genome() {
	}

	/**
	 * Create a Genome from .fna, .annot, and all serialized Sequence object:<br>
	 * <li>Read Genome sequence information in the different fna files <br>
	 * <li>Load annotation information<br>
	 * <br>
	 * As we are dealing with bacteria, we are sure that the chromosome with maximum
	 * length is THE chromosome, other are plasmids. So we search this chromosome
	 * and classify the other as plasmid by ordering the list.<br>
	 * 
	 * @param genomePath
	 * @param annotation
	 * @throws Exception
	 */
	public Genome(String genomePath, boolean annotation) throws Exception {
		this.species = FileUtils.removeExtensionAndPath(genomePath);
		System.out.println("g : " + genomePath);
		File file = new File(genomePath);
		System.out.println("Load " + this.species);
		File[] files = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".fna"))
					return true;
				return false;
			}
		});

		int k = 1; // chromosome index
		for (int i = 0; i < files.length; i++) {
			/*
			 * read Genome sequence in .fna
			 */
			System.out.println(files[i]);
			FileInputStream inStream = new FileInputStream(files[i]);
			DNACompoundSet compoundSet = DNACompoundSet.getDNACompoundSet();
			FastaFileReader<DNASequence, NucleotideCompound> fastaReader = new FastaFileReader<DNASequence, NucleotideCompound>(
					inStream, new NCBIFastaHeaderParser<DNASequence, NucleotideCompound>(),
					new DNASequenceCreator(compoundSet));
			LinkedHashMap<String, DNASequence> genomeSequences = fastaReader.process(compoundSet);

			/*
			 * Convert to Chromosome object
			 */
			LinkedHashMap<String, Chromosome> chromosomesTemp = Chromosome.getChromosomeFromDNASequence(genomeSequences,
					this, k, files[i].getAbsolutePath());

			/*
			 * We add annotation information
			 */
			if (annotation) {
				for (Chromosome chromo : chromosomesTemp.values()) {
					chromo.setAnnotation(Annotation
							.load(genomePath + File.separator + chromo.getAccession().toString() + "_Annotation"));
					chromo.setArrayListWithAnnotation(genomePath + File.separator + "Sequences" + File.separator);
				}
			}

			/*
			 * Add to chromosome HashMap
			 */
			for (String accession : chromosomesTemp.keySet()) {
				chromosomes.put(accession, chromosomesTemp.get(accession));
			}
		}

		System.out.println("Number of chromosomes found " + chromosomes.size());
	}

	/**
	 * Clear current genome from JVM by setting it up to a 4 base genome
	 */
	public void clearGenome() {
		for (Chromosome chromo : this.getChromosomes().values()) {
			chromo.clearChromosome();
		}
	}

	/**
	 * Go through all chromosomes and each Genes map and GeneName map to find the
	 * corresponding gene
	 * 
	 * @param name
	 * @return
	 */
	public Gene getGeneFromName(String name) {
		for (String accessionChromo : this.getChromosomes().keySet()) {
			Gene gene = getGeneFromName(name, accessionChromo);
			if (gene != null)
				return gene;
		}
		return null;
	}

	/**
	 * Go through all chromosomes and each Genes map and GeneName map to find the
	 * corresponding gene
	 * 
	 * @param name
	 * @return
	 */
	public Gene getGeneFromName(String name, String accessionChromo) {
		if (name.equals("No gene"))
			return null;
		if (name.contains("(") && name.contains(")")) {
			name = name.substring(0, name.indexOf("(")).trim();
		}
		Chromosome chromo = this.getChromosomes().get(accessionChromo);
		if (chromo.getGeneNameToLocusTagMap().containsKey(name)) {
			Gene gene = chromo.getGenes().get(chromo.getGeneNameToLocusTagMap().get(name));
			return gene;
		}
		if (chromo.getGenes().containsKey(name)) {
			return chromo.getGenes().get(name);
		}
		if (chromo.getGenesAlternative().containsKey(name)) {
			System.out.println("return: " + name);
			return chromo.getGenesAlternative().get(name);
		}
		if (chromo.getNcRNAs().containsKey(name)) {
			System.out.println("return: " + name);
			return chromo.getNcRNAs().get(name);
		}
		return null;
	}

	/**
	 * Return all element names contained in all chromosomes
	 * 
	 * @param name
	 * @return the list of Element names
	 */
	public ArrayList<String> getAllElementNames() {
		ArrayList<String> allElements = new ArrayList<>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			for (String element : chromo.getAllElements().keySet()) {
				allElements.add(element);
			}
		}
		return allElements;
	}

	/**
	 * Return all elements contained in all chromosomes
	 * 
	 * @param name
	 * @return the list of Elements
	 */
	public ArrayList<Sequence> getAllElements() {
		ArrayList<Sequence> allElements = new ArrayList<>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			for (Sequence element : chromo.getAllElements().values()) {
				allElements.add(element);
			}
		}
		return allElements;
	}

	/**
	 * Search an element by its name in the genome
	 * 
	 * @param name
	 * @return
	 */
	public Sequence getElement(String name) {
		for (Chromosome chromo : this.getChromosomes().values()) {
			if (chromo.getGeneNameToLocusTagMap().containsKey(name)) {
				Gene gene = chromo.getGenes().get(chromo.getGeneNameToLocusTagMap().get(name));
				return gene;
			}
			if (chromo.getAllElements().containsKey(name)) {
				return chromo.getAllElements().get(name);
			}
		}
		return null;
	}

	/**
	 * Return all the Gene locus tag name available in every chromosome
	 * 
	 * @param name
	 * @return
	 */
	public ArrayList<String> getGeneNames() {
		ArrayList<String> geneNames = new ArrayList<>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			geneNames.addAll(chromo.getGeneNameList());
		}
		return geneNames;
	}
	
	/**
	 * Return all Genes in all chromosomes
	 * 
	 * @return the LinkedHashMap<String, Sequence>
	 */
	public LinkedHashMap<String, Gene> getGenes() {
		LinkedHashMap<String, Gene> elements = new LinkedHashMap<String, Gene>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			for (String key : chromo.getGenes().keySet()) {
				elements.put(key, chromo.getGenes().get(key));
			}
		}
		return elements;
	}
	
	/**
	 * Return all NcRNAs in all chromosomes
	 * 
	 * @return the LinkedHashMap<String, Sequence>
	 */
	public LinkedHashMap<String, NcRNA> getNcRNAs() {
		LinkedHashMap<String, NcRNA> elements = new LinkedHashMap<String, NcRNA>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			for (String key : chromo.getNcRNAs().keySet()) {
				elements.put(key, chromo.getNcRNAs().get(key));
			}
		}
		return elements;
	}

	/**
	 * Return all Operon in all chromosomes
	 * 
	 * @return the LinkedHashMap<String, Sequence>
	 */
	public LinkedHashMap<String, Operon> getOperons() {
		LinkedHashMap<String, Operon> elements = new LinkedHashMap<String, Operon>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			for (String key : chromo.getOperons().keySet()) {
				elements.put(key, chromo.getOperons().get(key));
			}
		}
		return elements;
	}
	
	/**
	 * Return all Srnas in all chromosomes
	 * 
	 * @return the LinkedHashMap<String, Sequence>
	 */
	public LinkedHashMap<String, Srna> getsRNAs() {
		LinkedHashMap<String, Srna> elements = new LinkedHashMap<String, Srna>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			for (String key : chromo.getsRNAs().keySet()) {
				elements.put(key, chromo.getsRNAs().get(key));
			}
		}
		return elements;
	}

	/**
	 * Return all AsRNas in all chromosomes
	 * 
	 * @return the LinkedHashMap<String, Sequence>
	 */
	public LinkedHashMap<String, Srna> getAsRNAs() {
		LinkedHashMap<String, Srna> elements = new LinkedHashMap<String, Srna>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			for (String key : chromo.getAsRNAs().keySet()) {
				elements.put(key, chromo.getAsRNAs().get(key));
			}
		}
		return elements;
	}

	/**
	 * Return all CisRegs in all chromosomes
	 * 
	 * @return the LinkedHashMap<String, Sequence>
	 */
	public LinkedHashMap<String, Srna> getCisRegs() {
		LinkedHashMap<String, Srna> elements = new LinkedHashMap<String, Srna>();
		for (Chromosome chromo : this.getChromosomes().values()) {
			for (String key : chromo.getCisRegs().keySet()) {
				elements.put(key, chromo.getCisRegs().get(key));
			}
		}
		return elements;
	}


	/**
	 * Search in locustag names, gene name, and newlocustag if a gene is present or
	 * not
	 * 
	 * @param name
	 * @return
	 */
	public Sequence searchElement(String name) {
		for (Chromosome chromo : this.getChromosomes().values()) {
			if (chromo.getGeneNameToLocusTagMap().containsKey(name)) {
				Gene gene = chromo.getGenes().get(chromo.getGeneNameToLocusTagMap().get(name));
				return gene;
			}
			if (chromo.getAllElements().containsKey(name)) {
				return chromo.getAllElements().get(name);
			}
			for (Gene gene : chromo.getGenes().values()) {
				if (gene.getOldLocusTag().equals(name)) {
					return gene;
				}
				if (gene.getNewLocusTag().equals(name)) {
					return gene;
				}

			}
		}
		return null;

	}

	/**
	 * 
	 * Return the total length of the genome by adding all chromosomes lengths
	 * 
	 * @return
	 */
	public int getLengthGenome() {
		int length = 0;
		for (Chromosome chromo : getChromosomes().values()) {
			System.out.println(chromo.getName() + " : " + chromo.getLength());
			length += chromo.getLength();
		}
		return length;
	}

	/**
	 * Find in ModelProvider if Egde Genome is already loaded If not search
	 * EGDE_NAME in GENOMEDIR
	 * 
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Genome loadEgdeGenome() {
		return loadGenome(EGDE_NAME);
	}

	/**
	 * Find in ModelProvider if Genome Accession is already loaded If not search
	 * load it
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Genome loadGenome(String genomeAccession) {
		// System.err.println("Load: "+genomeAccession);
		if (genomeAccession.equals("Listeria_monocytogenes_EGD_e_uid61583")) {
			genomeAccession = EGDE_NAME;
		}
		return loadGenome(genomeAccession, Database.getGENOMES_PATH(), true, true);
	}

	/**
	 * Load/return a genome given by a genomeAccession ID<br>
	 * <br>
	 * 
	 * If keepInMemory is true : Find in ModelProvider if Genome Accession is
	 * already loaded if not load it <br>
	 * If keepInMemory is false : Only load the Genome without saving it <br>
	 * <br>
	 * If annotation is true : Load annotation information of the genome <br>
	 * If anotation is false : Only load the sequence of the genome
	 * 
	 * @param genomeAccession
	 * @param keepInMemory
	 * @param annotation
	 * @return
	 */
	public static Genome loadGenome(String genomeAccession, String path, boolean keepInMemory, boolean annotation) {
		if (keepInMemory) {
			TreeMap<String, Genome> genomes = Database.getInstance().getGenomes();
			Genome genome = new Genome();
			if (genomes.size() == 0) {
				try {
					genome = new Genome(path + File.separator + genomeAccession, annotation);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Database.getInstance().getGenomes().put(genomeAccession, genome);
			} else {
				// System.out.println("Genome already loaded");
				genome = genomes.get(genomeAccession);
				if (genome == null) { // if this genome has not been loaded -> load it
					try {
						System.out.println(path + File.separator + genomeAccession);
						genome = new Genome(path + File.separator + genomeAccession, annotation);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Database.getInstance().getGenomes().put(genomeAccession, genome);
				}
			}
			return genome;
		} else {
			try {
				return new Genome(path + File.separator + genomeAccession, annotation);
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * Get an ordered list of all chromosomes The order is fixed by the order of the
	 * chromosomes in the FNA (fasta) file used for reading the genome
	 * 
	 * @return
	 */
	public ArrayList<String> getChromosomeNames() {
		ArrayList<String> chromoNames = new ArrayList<>();
		for (String chromoID : this.getChromosomes().keySet()) {
			chromoNames.add(chromoID);
		}
		return chromoNames;
	}

	/**
	 * Read a text file containing the different genome ID available for Listeria
	 * 
	 * @return
	 */
	public static ArrayList<String> getAvailableGenomes() {
		ArrayList<String> listGenomes = TabDelimitedTableReader.readList(Database.getInstance().getGenomeArrayPath(),
				1);
		listGenomes.remove(0);
		return listGenomes;
	}

	/**
	 * Return defaut genome for a databse>br> For the moment -> first element of
	 * getAvailableGenomes()
	 * 
	 * @return
	 */
	public static String getDefautGenome() {
		return getAvailableGenomes().get(0);
	}

	/**
	 * Modify id of gene, to be sure that the suffix number is always of 4
	 * numbers<br>
	 * ex: replace lmpc1 by lmpc0001, lmpc12 by lmpc0012<br>
	 * 
	 * @param name
	 * @param suffix used for gene name, ex: lmo, lmpc, LMRG_
	 * @return
	 */
	public static String modifyID(String name, String prefix) {
		try {
			String id = name.replaceFirst(prefix, "");
			if (id.length() == 1)
				id = "000" + id;
			if (id.length() == 2)
				id = "00" + id;
			if (id.length() == 3)
				id = "0" + id;
			String finaleName = prefix + id;
			return finaleName;
		} catch (Exception e) {
			return null;
		}
	}

	public static class OpenGenomesThread implements IRunnableWithProgress {
		private ArrayList<String> genomeNames = new ArrayList<>();

		public OpenGenomesThread(ArrayList<String> genomeNames) {
			this.genomeNames = genomeNames;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			int sizeProcess = genomeNames.size();
			// Tell the user what you are doing
			monitor.beginTask("Loading genomes", sizeProcess);
			monitor.worked(1);
			// Optionally add subtasks
			int i = 1;
			for (String genomeName : genomeNames) {
				monitor.subTask("Loading genome " + i + "/" + genomeNames.size() + " : " + genomeName);
				Genome.loadGenome(genomeName);
				monitor.worked(1);
				i++;
			}
			// You are done
			monitor.done();
		}
	}

	public static class GetMultiFastaThread implements IRunnableWithProgress {
		private HashMap<String, String> genomeToGenes = new HashMap<>();
		private ArrayList<String> fastaFile = new ArrayList<>();

		public GetMultiFastaThread(HashMap<String, String> genomeToGenes) {
			this.genomeToGenes = genomeToGenes;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			int sizeProcess = genomeToGenes.size();
			// Tell the user what you are doing
			monitor.beginTask("Loading genomes", sizeProcess);
			monitor.worked(1);
			// Optionally add subtasks
			int i = 1;
			for (String genomeName : genomeToGenes.keySet()) {
				String gene = genomeToGenes.get(genomeName);
				monitor.subTask(
						"Getting sequence " + i + "/" + genomeToGenes.size() + " : " + gene + " - " + genomeName);
				String sequenceAA = Genome.getProteinSequenceFromFasta(genomeName, gene);
				fastaFile.add(">" + gene + "|" + genomeName.replaceAll(" ", "_"));
				fastaFile.add(sequenceAA);
				monitor.worked(1);
				i++;

				// Check if the user pressed "cancel"
				if (monitor.isCanceled()) {
					monitor.done();
					return;
				}

			}

			// You are done
			monitor.done();
		}

		public ArrayList<String> getMultiFasta() {
			return fastaFile;
		}

		public void setMultiFasta(ArrayList<String> fastaFile) {
			this.fastaFile = fastaFile;
		}
	}

	/**
	 * Read all accessible FAA files to extract protein sequence of a gene
	 * 
	 * @param genomeName
	 * @param gene
	 * @return
	 */
	public static String getProteinSequenceFromFasta(String genomeName, String gene) {
		String sequenceAA = "";
		/*
		 * get all faa
		 */
		File path = new File(Database.getGENOMES_PATH() + genomeName + File.separator);
		for (File file : path.listFiles()) {
			if (file.getAbsolutePath().endsWith(".faa")) {
				Path pathFile = FileSystems.getDefault().getPath(file.getAbsolutePath());
				List<String> lines;
				try {
					lines = Files.readAllLines(pathFile, StandardCharsets.UTF_8);
					for (int i = 0; i < lines.size(); i++) {
						String line = lines.get(i);
						if (line.startsWith(">")) {
							if (line.contains(gene)) {
								sequenceAA = lines.get(i + 1);
								return sequenceAA;
							}
						}

					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return sequenceAA;
	}

	/*
	 * ************************************************************** Getters and
	 * Setters *********************************************
	 */
	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	/**
	 * Get the first chromosome in the HashMap
	 * 
	 * Is Used to replace: Genome.loadGenome(genomeName).getChromosomes().get(0)
	 * 
	 * @return
	 */
	public Chromosome getFirstChromosome() {
		String accession = this.getChromosomes().keySet().iterator().next();
		Chromosome chromosome = this.getChromosomes().get(accession);
		return chromosome;
	}

	public LinkedHashMap<String, Chromosome> getChromosomes() {
		return chromosomes;
	}

	public void setChromosomes(LinkedHashMap<String, Chromosome> chromosomes) {
		this.chromosomes = chromosomes;
	}

}
