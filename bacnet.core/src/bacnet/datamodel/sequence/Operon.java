package bacnet.datamodel.sequence;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import org.biojava3.core.sequence.DNASequence;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;

public class Operon extends Sequence {

    /**
     * 
     */
    private static final long serialVersionUID = -8863494706241798280L;

    private TreeSet<String> genes = new TreeSet<String>();

    public Operon() {
        super();
        setType(SeqType.Operon);
    }

    public Operon(String name, int from, int to, char strand) {
        super(name, from, to, strand);
        setType(SeqType.Operon);
    }

    public static Operon load(String fileName) {
        try {
            // Create necessary input streams
            FileInputStream fis = new FileInputStream(fileName); // Read from file
            GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
            ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
            // Read in an object. It should be a vector of scribbles
            Operon seq = (Operon) in.readObject();
            in.close();
            return seq;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } // Close the stream.

    }

    /**
     * Take row from Annot_sup table and creat an Operon Object from it
     * 
     * @param row
     * @param genome
     * @return
     */
    public static Operon convertDNASequenceOperon(String[] row, GenomeNCBI genome) {
        String note = row[4].trim();
        String name = note.split("->")[0].trim();
        Operon operon = new Operon(name, Integer.parseInt(row[0]), Integer.parseInt(row[1]), row[2].toCharArray()[0]);
        note = note.split("->")[1].trim();
        String[] genesS = note.split(" ");
        // search cooresponding genes
        for (String gene : genesS) {
            DNASequence seq = genome.getGeneFromName(gene);
            operon.getGenes().add(seq.getAccession().toString());
        }
        return operon;
    }

    public TreeSet<String> getGenes() {
        return genes;
    }

    public void setGenes(TreeSet<String> genes) {
        this.genes = genes;
    }

    @Override
    public String toString() {
        return getName() + "-> " + genes.size() + " gene(s)";
    }

    /**
     * Return a list of all genes part of this Operon
     * 
     * @return
     */
    public String toStringGenes() {
        String ret = "";
        for (String geneID : genes) {
            Gene gene = Genome.loadGenome(getGenomeName()).getGeneFromName(geneID);
            if (gene != null) {
                String genename = gene.getGeneName();
                if (genename.equals("-"))
                    ret += geneID + " ";
                else
                    ret += geneID + "(" + genename + ") ";
            } else {
                ret += geneID + " ";
            }
        }
        return ret;
    }

    /**
     * Return a String containing a description of Operon properties
     * 
     * @return
     */
    public String toStringInfo() {
        String ret = "Name: " + getName() + "\n";
        String strandTemp = "+";
        if (!isStrand())
            strandTemp = "-";
        ret += "Strand: " + strandTemp + "\n";
        ret += "from : " + getBegin() + "\n";
        ret += "to : " + getEnd() + "\n\n";

        ret += genes.size() + " gene(s):\n";
        ret += toStringGenes() + "\n";
        return ret;
    }

}
