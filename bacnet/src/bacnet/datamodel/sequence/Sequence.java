package bacnet.datamodel.sequence;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.Strand;
import bacnet.datamodel.dataset.ExpressionMatrix;

/**
 * Object for manipulating part of Sequence.<br>
 * Main attributes are <code>name, begin, end, strand, genome, and type</code><br>
 * <br>
 * 
 * SeqType {Gene,NcRNA,Operon,Srna,ASrna,terminator,unknown}<br>
 * <br>
 * 
 * for each <code>Sequence</code> we force the condition: <br>
 * <code>this.begin inf_to this.end</code>
 * 
 * @author Christophe Bécavin
 *
 */
public class Sequence implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4818676464640692972L;

    public static int DEFAULT_LENGTH = 300;
    /**
     * Anti-SD sequence taken from Correlations between Shine-Dalgarno Sequences and Gene Features Such
     * as Predicted Expression Levels and Operon Structures Jiong Ma, Allan Campbell, and Samuel Karlin
     * J. Bacteriol. October 2002 vol. 184 no. 20 5733-5745
     */
    public static String ANTI_SD_SEQ = "AUCACCUCCUUU";

    private String name = "";
    private SeqType type = SeqType.Gene;
    private String id = "";
    private int begin = -1;
    private int end = -1;
    private char strand = '+';
    private int length = -1;
    private int conservation = -1;
    private String comment = "";
    private String ref = "";
    private String genomeName = Genome.EGDE_NAME;
    private String chromosomeID = Genome.EGDE_CHROMO_NAME;
    private TreeSet<String> synonym = new TreeSet<String>();
    /**
     * List of features
     */
    private LinkedHashMap<String, String> features = new LinkedHashMap<String, String>();

    /**
     * HashMap linking genomeName to conserved gene<br>
     * Give information about the genome in which this gene is conserved
     */
    private LinkedHashMap<String, String> conservationHashMap = new LinkedHashMap<String, String>();

    /**
     * List of signatures ID which this Sequence is part of
     */
    private TreeSet<String> signatures = new TreeSet<String>();

    public enum SeqType {
        Gene, NcRNA, Operon, Srna, ASrna, terminator, unknown
    }

    public Sequence() {}

    /**
     * Create a Sequence object<br>
     * if(begin>end) <br>
     * this.end = begin <br>
     * this.begin = end <br>
     * this.strand = '-' <br>
     * 
     * @param name of the sequence
     * @param begin
     * @param end
     */
    public Sequence(String name, int begin, int end) {
        this.name = name;
        if (begin < end) {
            this.begin = begin;
            this.end = end;
        } else {
            this.end = begin;
            this.begin = end;
            this.strand = '-';
        }
        this.length = this.end - this.begin + 1;
    }

    /**
     * Create a Sequence with a specific name, from, to and strand <br>
     * CAREFUL: <br>
     * from < to ALWAYS<br>
     * sense is given by strand<br>
     * If to < from it will correct automatically the position and strand, to have from < to
     * 
     * @param name
     * @param begin
     * @param end
     */
    public Sequence(String name, int begin, int end, char strand) {
        this.name = name;
        if (strand == '+' || strand == '-')
            this.strand = strand;
        else
            System.err.println("Wrong format for strand");
        /*
         * Be sure than from < to
         */
        if (begin <= end) {
            this.begin = begin;
            this.end = end;
            setStrand(strand);
        } else if (end != ExpressionMatrix.MISSING_VALUE) {
            if (strand == '-') {
                setStrand('-');
                this.begin = end;
                this.end = begin;
            } else {
                System.err.println(name + " has wrong strand information");
                setStrand('-');
                this.begin = end;
                this.end = begin;
            }
        } else {
            this.begin = begin;
            this.end = end;
            setStrand(strand);
        }
        this.length = this.end - this.begin + 1;

    }

    /**
     * Return True if strand=='+'
     * 
     * @return
     */
    public boolean isStrand() {
        if (getStrand() == '+')
            return true;
        else
            return false;
    }

    /**
     * Read chromosome and extract the corresponding sequence as a String
     * 
     * @return
     */
    public String getSequence() {
        Chromosome chromosome = this.getChromosome();
        Strand strand = Strand.POSITIVE;
        if (!this.isStrand())
            strand = Strand.NEGATIVE;
        if (getEnd() > chromosome.getLength()) {
            int newEnd = getEnd() - chromosome.getLength();
            String seq1 = chromosome.getSequenceAsString(getBegin(), chromosome.getLength(), strand);
            String seq2 = chromosome.getSequenceAsString(1, newEnd, strand);
            String seq = "";
            if (strand.equals(Strand.POSITIVE)) {
                seq = seq1 + seq2;
            } else {
                seq = seq2 + seq1;
            }
            return seq;
        } else {
            return chromosome.getSequenceAsString(getBegin(), getEnd(), strand);
        }

    }

    /**
     * Return RNA sequence of the <code>Sequence</code>
     * 
     * @return
     */
    public String getSequenceRNA() {
        DNASequence seqNucleotide = new DNASequence(getSequence());
        return seqNucleotide.getRNASequence().getSequenceAsString();
    }

    /**
     * Get Amino Acid sequence of a Sequence
     * 
     * @return
     */
    public String getSequenceAA() {
        DNASequence seqNucleotide = new DNASequence(getSequence());
        ProteinSequence protein = seqNucleotide.getRNASequence().getProteinSequence();
        return protein.getSequenceAsString();
    }

    /**
     * When an Amino Acid sequence contains Stop codon ('*'), this method search the first start codon
     * and remove sequence before, and then create different sequence, from one stop codon to the other.
     * 
     * @return
     */
    public ArrayList<String> getSequenceAACurated() {
        ArrayList<String> sequences = new ArrayList<String>();
        String seq = getSequenceAA();
        // Search first start codon and remove the rest of the sequence
        int index = seq.indexOf("M");
        if (index != -1) {
            seq = seq.substring(index, seq.length());
            int k = 0;
            for (int i = 0; i < seq.length(); i++) {
                char letter = seq.charAt(i);
                if (letter == '*') {
                    String subSeq = seq.substring(k, i);
                    k = i + 1;
                    if (!subSeq.equals(""))
                        sequences.add(subSeq);
                }
            }
            String subSeq = seq.substring(k, seq.length());
            sequences.add(subSeq);
            // System.out.println(seq);
            // for(String seqd : sequences){
            // System.out.println(seqd);
            // }
        }
        return sequences;
    }

    /**
     * When an Amino Acid sequence contains Stop codon ('*'), this method search the first start codon
     * and remove sequence before, and then create different sequence, from one stop codon to the other.
     * 
     * @return
     */
    public static ArrayList<String> getSequenceAACurated(String sequence) {
        ArrayList<String> sequences = new ArrayList<String>();
        String seq = sequence;
        int index = seq.indexOf("M");
        if (index != -1) {
            seq = seq.substring(index, seq.length());
            int k = 0;
            for (int i = 0; i < seq.length(); i++) {
                char letter = seq.charAt(i);
                if (letter == '*') {
                    String subSeq = seq.substring(k, i);
                    k = i + 1;
                    if (!subSeq.equals(""))
                        sequences.add(subSeq);
                }
            }
            String subSeq = seq.substring(k, seq.length());
            sequences.add(subSeq);
            // System.out.println(seq);
            // for(String seqd : sequences){
            // System.out.println(seqd);
            // }
        }
        return sequences;
    }

    /**
     * Return corresponding Genome given by this.genomeName<br>
     * Load it if necessary
     * 
     * @return
     */
    public Genome getGenome() {
        return Genome.loadGenome(getGenomeName());
    }

    /**
     * Return corresponding Chromosome given by this.getChromosomeNb<br>
     * Load it if necessary
     * 
     * @return
     */
    public Chromosome getChromosome() {
        return getGenome().getChromosomes().get(this.getChromosomeID());
    }

    /**
     * Create a String containing all features and their attribute
     * 
     * @return
     */
    public String getFeaturesText() {
        String ret = "";
        for (String attribute : getFeatures().keySet()) {
            ret += attribute + ": " + getFeatures().get(attribute) + "\n";
        }
        return ret;
    }

    /**
     * Used when displaying information in <code>SrnaView</code><br>
     * Display only supplementary information of each Srna publication
     * 
     * @param ref
     * @return
     */
    public String getFeaturesTextForTable(String ref) {
        String ret = "";
        String[] taboos = {"strand", "name", "length", "type", "to", "from"};
        for (String attribute : getFeatures().keySet()) {
            if (attribute.contains(ref)) {
                boolean found = false;
                for (String taboo : taboos) {
                    if (attribute.contains(taboo))
                        found = true;
                }
                if (!found) {
                    String name = attribute.replaceFirst(ref, "").replace('(', ' ').replace(')', ' ').trim();
                    ret += name + ": " + getFeatures().get(attribute) + "; ";
                }
            }
        }
        return ret;
    }

    public String getSynonymsText() {
        String ret = "";
        for (String syno : getSynonym()) {
            ret += syno + ", ";
        }
        return ret;
    }

    /**
     * Get the nucleotide sequence 20bp upstream to the <code>Sequence</code>, +3bp for including start
     * codon
     * 
     * @return
     */
    public String getSDSequence() {
        return getSDSequence(20);
    }

    /**
     * Get the nucleotide sequence n bp upstream to the <code>Sequence</code>, +3bp for including start
     * codon
     * 
     * @param sizeSD the number of base pair to include before start codon
     * @return
     */
    public String getSDSequence(int sizeSD) {
        if (isStrand()) {
            Sequence seq = new Sequence("sd", begin - sizeSD, begin + 2, '+');
            seq.setGenomeName(this.getGenomeName());
            seq.setChromosomeID(this.getChromosomeID());
            return seq.getSequence();
        } else {
            Sequence seq = new Sequence("sd", end - 2, end + sizeSD, '-');
            seq.setGenomeName(this.getGenomeName());
            seq.setChromosomeID(this.getChromosomeID());
            return seq.getSequence();
        }
    }

    /**
     * Get the nucleotide sequence n bp downstream to the <code>Sequence</code>, +3bp for including
     * start codon
     * 
     * @param sizeSD the number of base pair to include before start codon
     * @return
     */
    public String get3UTRSequence(int sizeUTR) {
        if (isStrand()) {
            Sequence seq = new Sequence("utr", end - 2, end + sizeUTR, '+');
            seq.setGenomeName(this.getGenomeName());
            seq.setChromosomeID(this.getChromosomeID());
            return seq.getSequence();
        } else {
            Sequence seq = new Sequence("sd", begin - sizeUTR, begin + 2, '-');
            seq.setGenomeName(this.getGenomeName());
            seq.setChromosomeID(this.getChromosomeID());
            return seq.getSequence();
        }
    }

    /**
     * Search the consensus SD sequence in the 20bp upstream to a <code>Sequence</code>
     * 
     * @return
     */
    public boolean foundSDSequence() {
        String consensusAntiSD = "GGAGG";
        if (getSDSequence().indexOf(consensusAntiSD) != -1)
            return true;
        else
            return false;
    }

    /**
     * Calculate the overlapping size of two genes <br>
     * First, find the gene upstream to the other<br>
     * Then, as we got gene1.begin < gene2.begin, overlap = gene1.end - gene2.begin<br>
     * overlap can be negative, it will mean that we have no overlap (gene1.end < gene2.begin) <br>
     * or overlap can be higher than gene2.length, in that case overlap = gene2.length
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static int overlap(Sequence gene1, Sequence gene2) {
        int begin1 = gene1.getBegin();
        int end1 = gene1.getEnd();
        int begin2 = gene2.getBegin();
        int end2 = gene2.getEnd();

        // found gene upstream to the other
        int overlap = -1000000;
        if (begin1 < begin2) {
            overlap = end1 - begin2;
            if (overlap > 0 && gene2.getLength() <= overlap)
                overlap = gene2.getLength();
        } else {
            overlap = end2 - begin1;
            if (overlap > 0 && gene1.getLength() <= overlap)
                overlap = gene1.getLength();
        }
        return overlap;
    }

    /**
     * Decide if two genes are overlapping <br>
     * First, calculate the overlapping size of two genes, finding the gene upstream to the other<br>
     * If overlap is negative it will mean that we have no overlap (gene1.end < gene2.begin) <br>
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static boolean isOverlap(Sequence gene1, Sequence gene2) {
        int overlap = overlap(gene1, gene2);

        if (overlap <= Math.max(gene1.getLength(), gene2.getLength()) && overlap > -50) {
            // if (overlap <= gene2.getLength() && overlap > -50) {
            // if(gene1.getName().equals("peptide-1624")){
            // System.out.println("gene1.getName() "+gene1.getSequenceAA()+" "+overlap);
            // }
            return true;
        } else
            return false;
    }

    /**
     * Decide if two genes are overlapping <br>
     * First, calculate the overlapping size of two genes, finding the gene upstream to the other<br>
     * If overlap is negative it will mean that we have no overlap (gene1.end < gene2.begin) <br>
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static boolean isOverlap(Sequence gene1, Sequence gene2, int overlapCutoff) {
        int overlap = overlap(gene1, gene2);
        if (overlap <= Math.max(gene1.getLength(), gene2.getLength()) && overlap > overlapCutoff) {
            // if(gene1.getName().equals("peptide-1624")){
            // System.out.println("gene1.getName() "+gene1.getSequenceAA()+" "+overlap);
            // }
            return true;
        } else
            return false;
    }

    /**
     * Decide if first Sequence is overlapping the start region of the second <br>
     * 
     * @param gene1 <code>Sequence</code> to test for overlapping
     * @param gene2 <code>Sequence</code> which start region will be extracted
     * @param sizeStart number of base pair to integrate in the "start region of gene2
     * @return
     */
    public static boolean isOverlapStart(Sequence gene1, Sequence gene2, int sizeStart) {
        Sequence seq = new Sequence();
        if (gene2.isStrand()) {
            seq = new Sequence(gene2.getName(), gene2.getBegin(), gene2.getBegin() + (sizeStart + 1));
        } else {
            seq = new Sequence(gene2.getName(), gene2.getEnd(), gene2.getEnd() - (sizeStart + 1));
        }
        return isOverlap(gene1, seq);
    }

    /**
     * Decide if two sequences have same strand
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static boolean isSameStrand(Sequence gene1, Sequence gene2) {
        boolean xnor = (gene1.isStrand() == gene2.isStrand());
        return xnor;
    }

    /**
     * If :
     * <li>sequence.getBegin()==this.getBegin()
     * <li>&& sequence.getEnd()==this.getEnd()
     * <li>&& sequence.getStrand()==this.getStrand()<br>
     * return true
     * 
     * @param sequence
     * @return
     */
    public boolean isEqual(Sequence sequence) {
        if (sequence.getBegin() == this.getBegin() && sequence.getEnd() == this.getEnd()
                && sequence.getStrand() == this.getStrand()) {
            return true;
        } else
            return false;

    }

    /**
     * Print different <br>
     * 
     */
    @Override
    public String toString() {
        String ret = this.getName() + "\t" + this.getBegin() + "\t" + this.getEnd() + "\t" + this.getLength() + "\t"
                + this.getStrand();
        return ret;
    }

    /*
     * ********************************** Serialization **********************************
     */

    /**
     * Read compressed, serialized data with a FileInputStream. Uncompress that data with a
     * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream. Replace current data
     * with new data, and redraw everything.
     */
    public static Sequence load(String fileName) {
        try {
            // Create necessary input streams
            FileInputStream fis = new FileInputStream(fileName); // Read from file
            GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
            ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
            // Read in an object. It should be a vector of scribbles
            Sequence seq = (Sequence) in.readObject();
            in.close();
            return seq;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } // Close the stream.

    }

    /**
     * Serialize the vector of lines with an ObjectOutputStream. Compress the serialized objects with a
     * GZIPOutputStream. Write the compressed, serialized data to a file with a FileOutputStream. Don't
     * forget to flush and close the stream.
     */
    public void save(String fileName) {
        try {
            // Create the necessary output streams to save the scribble.
            FileOutputStream fos = new FileOutputStream(fileName);
            // Save to file
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            // Compressed
            ObjectOutputStream out = new ObjectOutputStream(gzos);
            // Save objects
            out.writeObject(this); // Write the entire Vector of scribbles
            out.flush(); // Always flush the output.
            out.close(); // And close the stream.
        }
        // Print out exceptions. We should really display them in a dialog...
        catch (IOException e) {
            System.out.println(e);
        }
    }

    /*
     * *****************************************************************************
     * ************************ Getter and Setter
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SeqType getType() {
        return type;
    }

    public void setType(SeqType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public char getStrand() {
        return strand;
    }

    public void setStrand(char strand) {
        this.strand = strand;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getConservation() {
        return conservation;
    }

    public void setConservation(int conservation) {
        this.conservation = conservation;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genome) {
        this.genomeName = genome;
    }

    public String getChromosomeID() {
        return chromosomeID;
    }

    public void setChromosomeID(String chromosomeID) {
        this.chromosomeID = chromosomeID;
    }

    public TreeSet<String> getSynonym() {
        return synonym;
    }

    public void setSynonym(TreeSet<String> synonym) {
        this.synonym = synonym;
    }

    /**
     * Get HashMap linking genomeName to conserved gene<br>
     * Give information about the genome in which this gene is conserved
     */
    public LinkedHashMap<String, String> getConservationHashMap() {
        return conservationHashMap;
    }

    public void setConservationHashMap(LinkedHashMap<String, String> conservationHashMap) {
        this.conservationHashMap = conservationHashMap;
    }

    public LinkedHashMap<String, String> getFeatures() {
        return features;
    }

    /**
     * Get the feature in the HashMap, pointed by the key attribute<br>
     * return "" if not found
     * 
     * @param attribute
     * @return
     */
    public String getFeature(String attribute) {
        String ret = getFeatures().get(attribute);
        if (ret == null) {
            return "";
        } else
            return ret;
    }

    public void setFeatures(LinkedHashMap<String, String> features) {
        this.features = features;
    }

    public TreeSet<String> getSignatures() {
        return signatures;
    }

    public String getSignaturesToString() {
        String ret = "";
        for (String signature : signatures) {
            ret += signature + ",";
        }
        return ret;
    }

    public void setSignatures(TreeSet<String> signatures) {
        this.signatures = signatures;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
