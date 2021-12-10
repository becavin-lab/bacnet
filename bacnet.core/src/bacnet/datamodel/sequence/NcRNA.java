package bacnet.datamodel.sequence;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

public class NcRNA extends Gene {

    /**
     * 
     */
    private static final long serialVersionUID = 3968704131603938471L;

    private String product = "";
    private TypeNcRNA typeNcRNA = TypeNcRNA.tRNA;

    public enum TypeNcRNA {
        tRNA, rRNA, pseudoGene
    }

    public NcRNA() {
        super();
    }

    public NcRNA(String name, int from, int to, char strand) {
        super(name, from, to, strand);
        setType(SeqType.NcRNA);
    }

    public static NcRNA load(String fileName) {
        try {
            // Create necessary input streams
            FileInputStream fis = new FileInputStream(fileName); // Read from file
            GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
            ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
            // Read in an object. It should be a vector of scribbles
            NcRNA seq = (NcRNA) in.readObject();
            in.close();
            return seq;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } // Close the stream.

    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public TypeNcRNA getTypeNcRNA() {
        return typeNcRNA;
    }

    public void setTypeNcRNA(TypeNcRNA typeNcRNA) {
        this.typeNcRNA = typeNcRNA;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
