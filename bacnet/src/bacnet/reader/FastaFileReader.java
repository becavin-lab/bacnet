package bacnet.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.DataSource;
import org.biojava3.core.sequence.TaxonomyID;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.compound.DNACompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.io.BufferedReaderBytesRead;
import org.biojava3.core.sequence.io.DNASequenceCreator;
import org.biojava3.core.sequence.io.template.FastaHeaderParserInterface;
import org.biojava3.core.sequence.io.template.SequenceCreatorInterface;
import org.biojava3.core.sequence.template.AbstractSequence.AnnotationType;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.CompoundSet;
import org.biojava3.core.sequence.template.Sequence;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Gene;
import bacnet.utils.FileUtils;

public class FastaFileReader<S extends Sequence<?>, C extends Compound> {

    public static String[] allfileExt = {".fasta", ".fa", ".seq", ".fsa", ".fna", ".ffn", ".faa", ".frn"};
    public static String[] seqfileExt = {".fasta", ".fa", ".seq", ".fsa", ".fna", ".ffn", ".frn"};
    public static String[] proteinfileExt = {".fasta", ".fa", ".fsa", ".faa",};

    SequenceCreatorInterface<C> sequenceCreator;
    FastaHeaderParserInterface<S, C> headerParser;
    BufferedReaderBytesRead br;
    InputStreamReader isr;
    FileInputStream fi = null;

    /**
     * 
     * From a .ffn file in root/file.faa We had all CodingInfo into the ChromosomeSequence
     * 
     * This method use my personal NCBIFastaHeaderParser
     * 
     * @param root
     * @param file
     * @param chromosomes
     * @return
     * @throws Exception
     */
    public static LinkedHashMap<String, ChromosomeBacteriaSequence> addCodingRegionFeaturesFromFFN(File root, File file,
            LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes) throws Exception {
        // we get only .ffn (i.e. Fasta coding regions) files in the folder root
        File[] files = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".ffn"))
                    return true;
                return false;
            }
        });
        // we had only the features coming from the file (.ffn) with the same name as
        // file (.fna)
        for (int i = 0; i < files.length; i++) {
            // the two files have the same name
            if (FileUtils.removeExtensionAndPath(files[i].getAbsolutePath())
                    .equals(FileUtils.removeExtensionAndPath(file.getAbsolutePath()))) {

                // We read the fasta file containing protein region
                FileInputStream inStream = new FileInputStream(files[i]);
                DNACompoundSet compoundSet = DNACompoundSet.getDNACompoundSet();
                FastaFileReader<DNASequence, NucleotideCompound> fastafileReader =
                        new FastaFileReader<DNASequence, NucleotideCompound>(inStream,
                                new NCBIFastaHeaderParser<DNASequence, NucleotideCompound>(),
                                new DNASequenceCreator(DNACompoundSet.getDNACompoundSet()));
                LinkedHashMap<String, DNASequence> codingSequences = fastafileReader.process(compoundSet);

                // we add it to the chromosome
                for (ChromosomeBacteriaSequence chromosome : chromosomes.values()) {
                    chromosome.setCodingSequenceHashMap(codingSequences);
                }
                System.out.println("add " + codingSequences.size() + " coding regions");
            }

        }
        return chromosomes;
    }

    /**
     * 
     * From a .frn file in root/file.frn We add all Noncoding Info into the ChromosomeSequence
     * 
     * This method use my personal NCBIFastaHeaderParser
     * 
     * @param root
     * @param file
     * @param chromosomes
     * @return
     * @throws Exception
     */
    public static LinkedHashMap<String, ChromosomeBacteriaSequence> addNonCodingRegionFeaturesFromFRN(File root,
            File file, LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes) throws Exception {
        // we get only .frn (i.e. Fasta non-coding regions) files in the folder root
        File[] files = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".frn"))
                    return true;
                return false;
            }
        });

        // we had only the features coming from the file (.ffn) with the same name as
        // file (.fna)
        for (int i = 0; i < files.length; i++) {
            // the two files have the same name
            if (FileUtils.removeExtensionAndPath(files[i].getAbsolutePath())
                    .equals(FileUtils.removeExtensionAndPath(file.getAbsolutePath()))) {

                // We read the fasta file containing noncoding region
                FileInputStream inStream = new FileInputStream(files[i]);
                DNACompoundSet compoundSet = DNACompoundSet.getDNACompoundSet();
                FastaFileReader<DNASequence, NucleotideCompound> fastafileReader =
                        new FastaFileReader<DNASequence, NucleotideCompound>(inStream,
                                new NCBIFastaHeaderParser<DNASequence, NucleotideCompound>(),
                                new DNASequenceCreator(compoundSet));
                LinkedHashMap<String, DNASequence> codingSequences = fastafileReader.process(compoundSet);
                // for ( Entry<String, DNASequence> entry : codingSequences.entrySet() ) {
                // System.out.println( entry.getValue().getOriginalHeader() + "=" +
                // entry.getValue().getSequenceAsString() );
                // }

                // we add it to the chromosome
                for (Entry<String, ChromosomeBacteriaSequence> entry : chromosomes.entrySet()) {
                    entry.getValue().setNoncodingSequenceHashMap(codingSequences);
                }
                System.out.println("add " + codingSequences.size() + " non coding regions");
            }

        }
        return chromosomes;
    }

    /**
     * According to the info we found in Chromosomes, we update the info of the different components
     * (coding, ncoding, proteins)
     * 
     * @param chromosomes
     * @return
     */
    public static LinkedHashMap<String, ChromosomeBacteriaSequence> updateInfoFromChromosome(
            LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes) {
        for (ChromosomeBacteriaSequence chromo : chromosomes.values()) {
            System.out.println("update:" + chromo.getAccession().toString());
            chromo.setAnnotationType(AnnotationType.CURATED);
            chromo.setBioBegin(1);
            chromo.setBioEnd(chromo.getLength());
            try {
                chromo.setTaxonomy(new TaxonomyID(chromo.getDescription().split(",")[0].trim(), DataSource.NCBI));
                chromo.setSource("RefSeq NCBI");
            } catch (Exception e) {
                chromo.setTaxonomy(new TaxonomyID("", DataSource.GENBANK));
                chromo.setSource("GenBank NCBI");
            }

            LinkedHashMap<String, DNASequence> codingSequenceHashMap = chromo.getCodingSequenceHashMap();
            LinkedHashMap<String, DNASequence> noncodingSequenceHashMap = chromo.getNoncodingSequenceHashMap();
            System.out.println("Load: " + chromo.getAccession().toString() + " " + codingSequenceHashMap.size()
                    + " coding sequences");
            System.out.println("Load: " + chromo.getAccession().toString() + " " + noncodingSequenceHashMap.size()
                    + " non-coding sequences");
            // Update coding hashmap
            for (Entry<String, DNASequence> entry : codingSequenceHashMap.entrySet()) {
                entry.getValue().setAnnotationType(chromo.getAnnotationType());
                entry.getValue().setDNAType(chromo.getDNAType());
                entry.getValue().setParentSequence(chromo);
                entry.getValue().setSource(chromo.getSource());
                entry.getValue().setTaxonomy(chromo.getTaxonomy());
            }
            chromo.setCodingSequenceHashMap(codingSequenceHashMap);

            // update non-coding hashmap
            for (Entry<String, DNASequence> entry : noncodingSequenceHashMap.entrySet()) {
                entry.getValue().setAnnotationType(chromo.getAnnotationType());
                entry.getValue().setDNAType(chromo.getDNAType());
                entry.getValue().setParentSequence(chromo);
                entry.getValue().setSource(chromo.getSource());
                entry.getValue().setTaxonomy(chromo.getTaxonomy());
            }
            chromo.setNoncodingSequenceHashMap(noncodingSequenceHashMap);

        }
        return chromosomes;
    }

    /**
     * If you are going to use FileProxyProteinSequenceCreator then do not use this constructor because
     * we need details about local file offsets for quick reads. InputStreams does not give you the name
     * of the stream to access quickly via file seek. A seek in an inputstream is forced to read all the
     * data so you don't gain anything.
     * 
     * @param br
     * @param headerParser
     * @param sequenceCreator
     */
    public FastaFileReader(InputStream is, FastaHeaderParserInterface<S, C> headerParser,
            SequenceCreatorInterface<C> sequenceCreator) {
        this.headerParser = headerParser;
        isr = new InputStreamReader(is);
        this.br = new BufferedReaderBytesRead(isr);
        this.sequenceCreator = sequenceCreator;
    }

    /**
     * If you are going to use the FileProxyProteinSequenceCreator then you need to use this constructor
     * because we need details about the location of the file.
     * 
     * @param file
     * @param headerParser
     * @param sequenceCreator
     * @throws Exception
     */
    public FastaFileReader(File file, FastaHeaderParserInterface<S, C> headerParser,
            SequenceCreatorInterface<C> sequenceCreator) throws Exception {
        this.headerParser = headerParser;
        fi = new FileInputStream(file);
        isr = new InputStreamReader(fi);
        this.br = new BufferedReaderBytesRead(isr);
        this.sequenceCreator = sequenceCreator;
    }

    /**
     * The parsing is done in this method
     * 
     * ******* There is a problem when the genes in the fasta file have the same Id, so I modify this
     * method accordingly
     * 
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, S> process(CompoundSet<?> compoundSet) throws Exception {
        LinkedHashMap<String, S> sequences = new LinkedHashMap<String, S>();
        String line = "";
        String header = "";
        StringBuilder sb = new StringBuilder();
        int maxSequenceLength = -1;
        long fileIndex = 0;
        long sequenceIndex = 0;
        boolean keepGoing = true;
        @SuppressWarnings("unused")
        int i = 0;
        do {
            i++;
            line = line.trim(); // nice to have but probably not needed
            if (line.length() != 0) {
                if (line.startsWith(">")) {
                    if (sb.length() > 0) {
                        String sequ = sb.toString();
                        // Test if all compound are part of CompoundSet
                        if (compoundSet instanceof AminoAcidCompoundSet) {
                            for (int j = 0; j < sequ.length(); j++) {
                                String nucl = sequ.charAt(j) + "";
                                AminoAcidCompound compound =
                                        ((AminoAcidCompoundSet) compoundSet).getCompoundForString(nucl);
                                if (compound == null) {
                                    sequ = sequ.replace(nucl, "X");
                                }
                            }

                        } else if (compoundSet instanceof DNACompoundSet) {
                            for (int j = 0; j < sequ.length(); j++) {
                                String nucl = sequ.charAt(j) + "";
                                NucleotideCompound compound = ((DNACompoundSet) compoundSet).getCompoundForString(nucl);
                                if (compound == null) {
                                    sequ = sequ.replace(nucl, "N");
                                }
                            }

                        }
                        S sequence = (S) sequenceCreator.getSequence(sequ, sequenceIndex);
                        // System.err.println(sequence.getClass());
                        headerParser.parseHeader(header, sequence);
                        sequences.put(sequence.getAccession().toString(), sequence);
                        if (maxSequenceLength < sb.length()) {
                            maxSequenceLength = sb.length();
                        }
                        sb = new StringBuilder(maxSequenceLength);
                    }
                    header = line.substring(1);
                } else if (line.startsWith(";")) {
                } else {
                    // mark the start of the sequence with the fileIndex before the line was read
                    if (sb.length() == 0) {
                        sequenceIndex = fileIndex;
                    }
                    sb.append(line);
                }
            }
            fileIndex = br.getBytesRead();
            line = br.readLine();
            if (line == null) {
                // System.out.println("Sequence index=" + sequenceIndex + " " + fileIndex );
                String sequ = sb.toString();
                // Test if all compound are part of CompoundSet
                if (compoundSet instanceof AminoAcidCompoundSet) {
                    for (int j = 0; j < sequ.length(); j++) {
                        String nucl = sequ.charAt(j) + "";
                        AminoAcidCompound compound = ((AminoAcidCompoundSet) compoundSet).getCompoundForString(nucl);
                        if (compound == null) {
                            sequ = sequ.replace(nucl, "X");
                        }
                    }

                } else if (compoundSet instanceof DNACompoundSet) {
                    for (int j = 0; j < sequ.length(); j++) {
                        String nucl = sequ.charAt(j) + "";
                        NucleotideCompound compound = ((DNACompoundSet) compoundSet).getCompoundForString(nucl);
                        if (compound == null) {
                            sequ = sequ.replace(nucl, "N");
                        }
                    }

                }
                S sequence = (S) sequenceCreator.getSequence(sequ, sequenceIndex);
                headerParser.parseHeader(header, sequence);
                sequences.put(sequence.getAccession().toString(), sequence);
                keepGoing = false;

                // headerParser.parseHeader(header, sequence);
                // sequences.put(String.valueOf(i),sequence);
                // keepGoing = false;
            }
        } while (keepGoing);
        br.close();
        isr.close();
        // If stream was created from File object then we need to close it
        if (fi != null) {
            fi.close();
        }
        // System.err.println("Finish reading FNA");
        return sequences;
    }

    /**
     * Save all amino acid sequence in a fasta file
     * 
     * @param sequences
     * @param fileName
     */
    public static void saveProteins(ArrayList<Gene> sequences, String fileName) {
        String[][] pttTable = new String[sequences.size() * 2][1];
        int i = 0;
        for (Gene gene : sequences) {
            try {
                String seqAA = gene.getSequenceAA();
                pttTable[i][0] = ">" + gene.getName() + "|" + gene.getOldLocusTag() + "|" + gene.getNewLocusTag();
                i++;
                pttTable[i][0] = seqAA;
                i++;
            } catch (Exception e) {
                System.err.println("Cannot get sequence for " + gene.getName() + " " + gene.getStrand() + " "
                        + gene.getBegin() + " " + gene.getEnd() + " " + gene.getChromosome().getLength());
            }
        }
        TabDelimitedTableReader.save(pttTable, fileName);
    }

    /**
     * Save all amino acid sequence in a fasta file
     * 
     * @param sequences
     * @param fileName
     */
    public static void saveCDS(ArrayList<Gene> sequences, String fileName) {
        String[][] pttTable = new String[sequences.size() * 2][1];
        int i = 0;
        for (Gene gene : sequences) {
            pttTable[i][0] = ">" + gene.getName();
            i++;
            System.out.println(gene.getName());
            // if(gene.getEnd()<gene.getGenome().getChromosomes().get(gene.getChromosomeNb()).getLength()){
            pttTable[i][0] = gene.getSequence();
            // }else{
            // pttTable[i][0] = "";
            // }
            i++;
        }
        TabDelimitedTableReader.save(pttTable, fileName);
    }
}
