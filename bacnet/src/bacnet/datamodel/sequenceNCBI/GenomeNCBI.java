/**
 * <copyright> </copyright>
 *
 * $Id$
 */
package bacnet.datamodel.sequenceNCBI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.DNASequence.DNAType;
import org.biojava3.core.sequence.DataSource;
import org.biojava3.core.sequence.compound.DNACompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.io.DNASequenceCreator;
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.annotation.COGannotation;
import bacnet.datamodel.annotation.GlaserFCannotation;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Operon;
import bacnet.reader.FastaFileReader;
import bacnet.reader.GFFNCBIReader;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

/**
 * Object containing an entire genome, created from NCBI RefSeq data<br>
 * Data for this genome come from: <it> NCBI.fna (for the sequence) <it> NCBI.gff (for Gene, CDS,
 * and ncRNA lists) If it exists some information are added: <it> Rfam.gff (other info) <it> supp
 * info in a .txt file <it> supp info about RAST results in a .txt file For EGD-e only: <it>
 * Srna.xml are read to add to the genome
 * 
 * @author UIBC
 *
 */
public class GenomeNCBI {

    /**
     * Path for all raw data for Genomes : Database.getInstance().getPath() + "/GenomeNCBI/";
     * 
     */
    public static String PATH_RAW = Database.getInstance().getPath() + File.separator + "GenomeNCBI" + File.separator;
    /**
     * Path for all Genomes : Database.getInstance().getPath() + "/GenomeNCBI/Genomes/";
     */
    public static String PATH_GENOMES = PATH_RAW + "Genomes"+ File.separator;
    
    /**
     * Path for all new Genomes : Database.getInstance().getPath() + "/GenomeNCBI/GenomesNew/"; This
     * might be used if all IDs have been changed on NCBI servers like it was the cas in Sept 2016
     * @deprecated only used in Old Listeriomics database
     */
    @Deprecated
    public static String PATH_GENOMES_NEW = PATH_RAW + "GenomesNew" + File.separator;
    /**
     * Path for all Temp fils, mainly created during Genome related operations. Database.getInstance().getPath() + "/Temp/";
     */
    public static String PATH_TEMP = PATH_RAW + "Temp" + File.separator;
    
    /**
     * Path for all homologs calculation file : Database.getInstance().getPath() +
     * "/GenomeNCBI/Homologs/";
     */
    public static String PATH_HOMOLOGS = PATH_RAW + "Homologs"+ File.separator ;
    /**
     * Path for all homologs calculation file : Database.getInstance().getPath() +
     * "/GenomeNCBI/Homologs/";
     */
    public static String PATH_PROTEINID = PATH_RAW + "ProteinId"+ File.separator ;
    /**
     * Path for all supplementary information for genome annotation : Database.getInstance().getPath() +
     * "/GenomeNCBI/Annotation/";
     */
    public static String PATH_ANNOTATION = PATH_RAW + "Annotation"+ File.separator;

    private LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes = new LinkedHashMap<>();
    private String species;

    public GenomeNCBI() {}

    /**
     * Create a genome with all chromosomes found If annotation = TRUE , we had the annotation we can
     * found in other fasta files annotation = FALSE, we read only the complete genome + eventual
     * plasmids
     * 
     * @param genomeName
     * @param annotation
     * @throws Exception
     */
    public GenomeNCBI(String genomePath, boolean annotation) throws Exception {
        // read Genome sequence given in the .fna
        this.species = FileUtils.removeExtensionAndPath(genomePath);
        File file = new File(genomePath);
        System.out.println(genomePath);
        System.out.println("Load " + this.species);
        /*
         ****** First we read Genome sequence information in the different fna files
         *
         * As we are dealing with bacteria, we are sure that the chromosome with maximum length is THE
         * chromosome, other are plasmids. So we search this chromosome and classify the other as plasmid by
         * ordering the list
         * 
         */
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
            	System.out.println(name);
                if (name.endsWith(".fna"))
                    return true;
                return false;
            }
        });
        for (int i = 0; i < files.length; i++) {
            /*
             * read Genome sequence in .fna
             */
            System.out.println("Read fasta:" + files[i].getAbsolutePath());
            FileInputStream inStream = new FileInputStream(files[i]);
            DNACompoundSet compoundSet = DNACompoundSet.getDNACompoundSet();
            FastaFileReader<DNASequence, NucleotideCompound> fastaReader =
                    new FastaFileReader<DNASequence, NucleotideCompound>(inStream,
                            new NCBIFastaHeaderParser<DNASequence, NucleotideCompound>(),
                            new DNASequenceCreator(compoundSet));
            LinkedHashMap<String, DNASequence> genomeSequences = fastaReader.process(compoundSet);
            /*
             * Create Chromosomes
             */
            int k = 1;
            if (genomeSequences.size() != 1) {
                System.out.println("More than one chromosomes in the fasta file " + files[i]);
            }
            for (String key : genomeSequences.keySet()) {
                DNASequence dnaSequence = genomeSequences.get(key); // get the first DNASequence (ony one normally) in
                                                                    // the HashMap
                ChromosomeBacteriaSequence chromosome =
                        new ChromosomeBacteriaSequence(dnaSequence.getProxySequenceReader());
                /*
                 * Control that accesion was read
                 */
                String accession = dnaSequence.getAccession().toString();
                if (accession.equals("NULL")) {
                    accession = FileUtils.removeExtensionAndPath(files[i].getAbsolutePath());
                    chromosome.setAccession(new AccessionID(accession, DataSource.NCBI));
                } else {
                    chromosome.setAccession(dnaSequence.getAccession());
                }
                chromosome.setChromosomeID(accession);
                chromosome.setDescription(dnaSequence.getDescription());
                if (dnaSequence.getDescription().split(",").length == 2) {
                    String name = dnaSequence.getDescription().split(",")[1].trim();
                    chromosome.setName(name);
                }
                chromosome.setChromosomeNumber(k);
                chromosome.setDNAType(DNAType.CHROMOSOME);
                // System.out.println(chromosome.getAccession().toString()+"
                // "+chromosome.getLength());
                if (chromosome.getLength() < 50000)
                    chromosome.setDNAType(DNAType.PLASMID);
                if (chromosome.getDescription().contains("contig"))
                    chromosome.setDNAType(DNAType.CONTIG);
                if (chromosome.getLength() < 2500)
                    chromosome.setDNAType(DNAType.CONTIG);
                chromosome.setParentGenome(this);
                /*
                 * Does not include CONTIG !!!!!!!
                 * 
                 */
                if (chromosome.getDNAType() != DNAType.CONTIG) {
                    chromosomes.put(chromosome.getAccession().toString(), chromosome);
                }
                k++;

            }

            /**
             * Add genes from GFF file
             */
            if (annotation) {
                GFFNCBIReader.addAnnotationFromGFF(files[i], chromosomes);
            }

        }

        /*
         * If available we add other informations from external files Mainly designed/used for Listeria
         * monocytogenes EGD-e
         */
        if (annotation) {
            // update common info
            chromosomes = FastaFileReader.updateInfoFromChromosome(chromosomes);
            try {
                if (this.species.equals(Genome.EGDE_NAME)) {
                    // add sRNAs
                    this.getChromosomes().get(Genome.EGDE_CHROMO_NAME).loadSrnas();
                    // add Operons
                    this.getChromosomes().get(Genome.EGDE_CHROMO_NAME).loadOperonsAndTerminator();
                    // add other info: Operon, COg, Glaser, length, isoelec, codon, mass
                    String[][] array = TabDelimitedTableReader.read(Annotation.EGDE_ANNOTATION);
                    for (int i = 1; i < array.length; i++) {
                        String name = array[i][0];
                        DNASequence sequence = getGeneFromName(name);
                        if (sequence != null) {
                            System.out.println(name);
                            for (int j = 2; j < array[0].length; j++) {
                                String note = array[0][j] + " : ";
                                if (array[0][j].equals("COG")) {
                                    note += COGannotation.getCOGDescription(array[i][j]);
                                } else if (array[0][j].equals("GlaserFunctionalCategory")) {
                                    note += GlaserFCannotation.getGlaserFCDescription(array[i][j]);
                                } else {
                                    note += array[i][j];
                                }
                                sequence.addNote(note);
                            }
                        }
                    }

                    // add Operon info to Genes
                    for (Operon operon : this.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getOperons()) {
                        System.out.println("load Operon: " + operon);
                        for (String geneName : operon.getGenes()) {
                            DNASequence gene = this.getChromosomes().get(Genome.EGDE_CHROMO_NAME)
                                    .getCodingSequenceHashMap().get(geneName);
                            gene.addNote("Operon : " + operon.getName());
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Problem during annotation parsing");
            }

        }

        System.out.println("Number of chromosomes found " + chromosomes.size());
        // System.out.println("Number of coding+noncoding region
        // "+(getCodingSequencesList().size()+getNonCodingSequencesList().size()));
        // System.out.println("Number of locus_Tag "+getLocusTagList().size());

    }
    
    /**
     * Replace all spaces in genomeName by "_" to have folder name compatible for Blast search<br>
     * All "_" already present are replaced by "--"
     * @param genomeName
     * @return
     */
    public static String processGenomeName(String genomeName) {
    	// Detect first if genomeName contain "_" and replace them by "--"
    	genomeName = genomeName.replaceAll("_", "--");
    	// then replace all " " by "_"
    	genomeName = genomeName.replaceAll(" ", "_");
    	return genomeName;
    }
    
    /**
     * Go back to previous genomeName by eplacing all "_" by spaces<br>
     * All "_" present before processing where replaced by "--" and are processed back
     * @param genomeName
     * @return
     */
    public static String unprocessGenomeName(String genomeName) {
    	// replace all "_" by " "
    	genomeName = genomeName.replaceAll("_", " ");
    	// Detect first if genomeName contain "--" and replace them by "_"
    	genomeName = genomeName.replaceAll("--", "_");
    	return genomeName;
    }
    

    /**
     * Put in a list all the CodingSequence found
     * 
     * @param allChromosomes true if we include all chromosomes
     * @return
     */
    public ArrayList<DNASequence> getCodingSequencesList(boolean allChromosomes) {
        ArrayList<DNASequence> listCoding = new ArrayList<DNASequence>();
        if (allChromosomes) {
            for (ChromosomeBacteriaSequence chromosome : chromosomes.values()) {
                for (String accession : chromosome.getCodingSequenceHashMap().keySet()) {
                    listCoding.add(chromosome.getCodingRegion(accession));
                }
            }
        } else {
            for (String accession : getFirstChromosome().getCodingSequenceHashMap().keySet()) {
                listCoding.add(getFirstChromosome().getCodingRegion(accession));
            }
        }
        return listCoding;
    }

    /**
     * Put in a sublist all the CodingSequence found in locuTag list
     * 
     * @param allChromosomes true if we include all chromosomes
     * @return
     */
    public ArrayList<DNASequence> getSubCodingSequencesList(ArrayList<String> locusTag, boolean allChromosomes) {
        ArrayList<DNASequence> listSubCoding = new ArrayList<DNASequence>();
        if (allChromosomes) {
            for (ChromosomeBacteriaSequence chromosome : chromosomes.values()) {
                for (String accession : chromosome.getCodingSequenceHashMap().keySet()) {
                    if (locusTag.contains(chromosome.getCodingRegion(accession).getAccession().toString())) {
                        listSubCoding.add(chromosome.getCodingRegion(accession));
                    }
                }
            }
        } else {
            for (String accession : getFirstChromosome().getCodingSequenceHashMap().keySet()) {
                if (locusTag.contains(getFirstChromosome().getCodingRegion(accession).getAccession().toString())) {
                    listSubCoding.add(getFirstChromosome().getCodingRegion(accession));
                }
            }
        }
        return listSubCoding;
    }

    /**
     * Put in a list all the locusTag found in the list of genes
     * 
     * @param allChromosomes true if we include all chromosomes
     * @return
     */
    public ArrayList<String> getLocusTagList(boolean allChromosomes) {
        ArrayList<String> listLocusTag = new ArrayList<String>();
        if (allChromosomes) {
            for (ChromosomeBacteriaSequence chromosome : chromosomes.values()) {
                for (String locusTag : chromosome.getCodingSequenceHashMap().keySet()) {
                    listLocusTag.add(locusTag);
                }
            }
        } else {
            for (String locusTag : getFirstChromosome().getCodingSequenceHashMap().keySet()) {
                listLocusTag.add(locusTag);
            }
        }
        return listLocusTag;
    }

    /**
     * Put in a list all the CodingSequence found
     * 
     * @param allChromosomes true if we include all chromosomes
     * @return
     */
    public ArrayList<String> getLocusTagCodingList(boolean allChromosomes) {
        ArrayList<String> listLocusTag = new ArrayList<String>();
        for (DNASequence sequence : getCodingSequencesList(allChromosomes)) {
            listLocusTag.add(sequence.getAccession().toString());
        }
        return listLocusTag;
    }

    public ArrayList<String> getLocusTagKnownCodingList(boolean allChromosomes) {
        ArrayList<String> listLocusTag = new ArrayList<String>();
        if (allChromosomes) {
            for (ChromosomeBacteriaSequence chromosome : this.getChromosomes().values()) {
                for (String accession : chromosome.getLocusTagToGeneNameMap().keySet()) {
                    listLocusTag.add(accession);
                }
            }
        } else {
            for (String accession : getFirstChromosome().getLocusTagToGeneNameMap().keySet()) {
                listLocusTag.add(accession);
            }
        }
        return listLocusTag;
    }

    public ArrayList<String> getLocusTagNonCodingList(boolean allChromosomes) {
        ArrayList<String> listLocusTag = new ArrayList<String>();
        for (DNASequence sequence : getNonCodingSequencesList(allChromosomes)) {
            listLocusTag.add(sequence.getAccession().toString());
        }
        return listLocusTag;
    }

    /**
     * Put in a list all the NonCodingSequence found
     * 
     * @param allChromosomes true if we include all chromosomes or not
     * @return
     */
    public ArrayList<DNASequence> getNonCodingSequencesList(boolean allChromosomes) {
        ArrayList<DNASequence> listNonCoding = new ArrayList<DNASequence>();
        if (allChromosomes) {
            for (ChromosomeBacteriaSequence chromosome : chromosomes.values()) {
                for (String accession : chromosome.getNoncodingSequenceHashMap().keySet()) {
                    listNonCoding.add(chromosome.getNonCodingRegion(accession));
                }
            }
        } else {
            for (String accession : getFirstChromosome().getNoncodingSequenceHashMap().keySet()) {
                listNonCoding.add(getFirstChromosome().getNonCodingRegion(accession));
            }
        }
        return listNonCoding;
    }

    /**
     * Go through codingSequence and nonCodingSequence searching for corresponding accession or locus
     * 
     * @param name
     * @return
     */
    public DNASequence getGeneFromName(String name) {
        name = name.trim();

        for (ChromosomeBacteriaSequence chromosome : this.getChromosomes().values()) {
            // search name in Coding or nonCoding locusTag
            if (chromosome.getCodingSequenceHashMap().containsKey(name)) {
                return chromosome.getCodingSequenceHashMap().get(name);
            }
            if (chromosome.getNoncodingSequenceHashMap().containsKey(name)) {
                return chromosome.getNoncodingSequenceHashMap().get(name);
            }
            // search name in Known GeneName
            if (chromosome.getGeneNameToLocusTagMap().containsKey(name)) {
                String locusTag = chromosome.getGeneNameToLocusTagMap().get(name);
                return chromosome.getCodingSequenceHashMap().get(locusTag);
            }
        }

        // System.out.println("Has not found: "+name);
        return null;

    }

    /**
     * Look in LocusTagToGeneName if a geneName exists
     * 
     * @param name
     * @return
     */
    public String getGeneName(String locusTag) {
        locusTag = locusTag.trim();
        for (ChromosomeBacteriaSequence chromosome : this.getChromosomes().values()) {
            if (chromosome.getLocusTagToGeneNameMap().containsKey(locusTag)) {
                return chromosome.getLocusTagToGeneNameMap().get(locusTag);
            }
        }
        return locusTag;
    }

    /**
     * Look in all LocusTagToGeneName exists
     * 
     * @param name
     * @return
     */
    public String getLocusTag(String geneName) {
        geneName = geneName.trim();
        if (geneName.equals("prfA"))
            return "lmo0200";
        for (ChromosomeBacteriaSequence chromosome : this.getChromosomes().values()) {
            if (chromosome.getGeneNameToLocusTagMap().containsKey(geneName)) {
                return chromosome.getGeneNameToLocusTagMap().get(geneName);
            }
        }
        return geneName;
    }

    /**
     * Init static variables after Database change
     */
    public static void initStaticVariables() {
        PATH_RAW = Database.getInstance().getPath() + File.separator + "GenomeNCBI" + File.separator;
        PATH_GENOMES = PATH_RAW + "Genomes"+ File.separator;
        PATH_GENOMES_NEW = PATH_RAW + "GenomesNew" + File.separator;
        PATH_HOMOLOGS = PATH_RAW + "Homologs"+ File.separator ;
        PATH_ANNOTATION = PATH_RAW + "Annotation"+ File.separator;
        PATH_TEMP = PATH_RAW + "Temp" + File.separator;        
    }

    /**
     * Override of equals method from Object
     * 
     * To be equal two genome has to come from the same species.
     */
    @Override
    public boolean equals(Object o) {
        if (species.equals(((GenomeNCBI) o).species)) {
            return true;
        } else
            return false;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Get the first chromosome in the HashMap
     * 
     * Is Used to replace: GenomeNCBI.getChromosomes().get(0)
     * 
     * @return
     */
    public ChromosomeBacteriaSequence getFirstChromosome() {
        String accession = this.getChromosomes().keySet().iterator().next();
        ChromosomeBacteriaSequence chromosome = this.getChromosomes().get(accession);
        return chromosome;
    }

    public LinkedHashMap<String, ChromosomeBacteriaSequence> getChromosomes() {
        return chromosomes;
    }

    public void setChromosomes(LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes) {
        this.chromosomes = chromosomes;
    }

}
