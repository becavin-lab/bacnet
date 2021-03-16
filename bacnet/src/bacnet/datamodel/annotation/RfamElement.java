package bacnet.datamodel.annotation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.biojava3.genome.parsers.gff.FeatureI;
import org.biojava3.genome.parsers.gff.FeatureList;
import org.biojava3.genome.parsers.gff.GFF3Reader;
import bacnet.Database;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

public class RfamElement {

    public static String RFAM_GENOME = getRfamDirectory() + "rFamGenome.txt";

    private String id;
    private String name;
    private String alias;
    private String note;
    private String type;
    private String species;
    private int begin;
    private int end;
    private char strand;
    private double score;

    public RfamElement(FeatureI rfam) {
        this.id = rfam.getAttribute("ID");
        this.name = rfam.getAttribute("Name");
        this.alias = rfam.getAttribute("Alias");
        this.note = rfam.getAttribute("Note");
        this.type = rfam.type();
        this.species = rfam.seqname();
        this.begin = Math.abs(rfam.location().bioStart());
        this.end = Math.abs(rfam.location().bioEnd());
        this.strand = rfam.location().bioStrand();
    }

    /**
     * Read a Gff from Rfam and get all possible information
     * 
     * @param gffFile
     * @return LinkedHashMap : element name -> RfamElement object
     * @throws IOException
     */
    public static LinkedHashMap<String, RfamElement> getRfamElementsFromGFF(String gffFile) throws IOException {
        LinkedHashMap<String, RfamElement> rfamList = new LinkedHashMap<String, RfamElement>();
        String rFamGenome = getRfamGenomeId(gffFile);
        if (!rFamGenome.equals("")) {
            FeatureList listGenes =
                    GFF3Reader.read(getRfamDirectory() + "genome_gff" + File.separator + rFamGenome + ".gff3");
            for (FeatureI feature : listGenes) {
                RfamElement rfam = new RfamElement(feature);
                rfamList.put(rfam.getId(), rfam);
            }
        }
        return rfamList;
    }

    /**
     * Read a Gff from Rfam and add the information to an existing HashMap
     * 
     * @param gffFile
     * @param rfamList LinkedHashMap : element name -> RfamElement object
     * @throws Exception
     */
    public static void getRfamElementsFromGFF(String gffFile, HashMap<String, RfamElement> rfamList) throws Exception {
        if (!gffFile.equals("")) {
            FeatureList listGenes =
                    GFF3Reader.read(getRfamDirectory() + "genome_gff" + File.separator + gffFile + ".gff3");
            for (FeatureI feature : listGenes) {
                RfamElement rfam = new RfamElement(feature);
                rfamList.put(rfam.getId(), rfam);
            }
        }
    }

    /**
     * Return the list of Rfam element in a particular Genome<br>
     * <br>
     * 
     * Search for all NCBI GFF in a particular genome folder (e.g. genomeName),<br>
     * thanks to RFAM_GENOME table it found corresponding Rfam GFF file if it exists<br>
     * and create HashMap<String, RfamElement> rfamList
     * 
     * @param genomeName folder where all genome files are
     * @return HashMap<String, RfamElement> rfamList
     * @throws Exception
     */
    public static HashMap<String, RfamElement> getRfamElementsFromGenome(String genomeName) throws Exception {
        File path = new File(genomeName);
        File[] files = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".gff"))
                    return true;
                return false;
            }
        });
        // we had only the features coming from the file (.gff)
        HashMap<String, RfamElement> rfamList = new HashMap<String, RfamElement>();
        for (int i = 0; i < files.length; i++) {
            String gffFile = getRfamGenomeId(FileUtils.removePath(files[i].getAbsolutePath()));
            getRfamElementsFromGFF(gffFile, rfamList);
        }
        return rfamList;
    }

    /**
     * Given an NCBI Gff file name, this function search in RFAM_GENOME table and return Rfam Gff file
     * name if it exists
     * 
     * @param genome
     * @return
     * @throws IOException
     */
    public static String getRfamGenomeId(String genome) throws IOException {
        // read rFam table of correspondence between genome ids
        String[][] array = TabDelimitedTableReader.read(RFAM_GENOME);
        String rFamGenome = "";
        for (int i = 0; i < array.length; i++) {
            if (array[i][1].equals(genome))
                rFamGenome = array[i][4];
        }
        return rFamGenome;
    }

    /**
     * Project.getANNOTATION_PATH()+"RfamData"+File.separator;
     * 
     * @return
     */
    public static String getRfamDirectory() {
        return Database.getInstance().getPath() + "Genome" + File.separator + "Rfam" + File.separator;
    }

    @Override
    public String toString() {
        String ret = "ID: " + id + "\n Type: " + type + "\n Alias: " + alias + "\n";
        ret += begin + "-" + end;
        ret += "  strand: " + strand + "\n";
        return ret;
    }

    /*
     * ******************************** Getters and setters ********************************
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}
