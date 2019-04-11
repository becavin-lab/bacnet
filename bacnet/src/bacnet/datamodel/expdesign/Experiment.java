package bacnet.datamodel.expdesign;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import bacnet.Database;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.Tiling;

public class Experiment implements Serializable {

    /**
     * This class is the highest class in the Experiment architecture.
     *
     * It contains the different bioConds of the experiment.
     * 
     */
    private static final long serialVersionUID = 993350014891502366L;

    private String name = "";

    private LinkedHashMap<String, BioCondition> bioConds;

    public Experiment() {
        bioConds = new LinkedHashMap<String, BioCondition>();
    }

    public Experiment(String fileName) {
        super();
        this.load(fileName);
    }

    /**
     * Load all experiment<br>
     * If all experiments are already loaded just get the general experiment found in
     * ModelProvider.INSTANCE
     * 
     * @return
     */
    public static Experiment getGeneralExp() {
        Experiment experiment = Database.getInstance().getGeneralExperiment();
        if (experiment == null) {
            experiment = new Experiment();
            for (BioCondition bioCond : BioCondition.getAllBioConditions()) {
                experiment.addBioCond(bioCond);
            }
        }
        return experiment;
    }

    /**
     * Add using a specific Key
     * 
     * @param bioCondKey
     * @param bioCond
     */
    public void addBioCond(String bioCondKey, BioCondition bioCond) {
        bioConds.put(bioCondKey, bioCond);
    }

    /**
     * Add using the bioCondName as a key
     * 
     * @param bioCond
     */
    public void addBioCond(BioCondition bioCond) {
        bioConds.put(bioCond.getName(), bioCond);
    }

    /**
     * Delete a BioCondition using a specific key
     * 
     * @param bioCondKey
     */
    public void deleteBioCond(String bioCondKey) {
        bioConds.remove(bioCondKey);
    }

    /**
     * Delete using the bioCondName as a key
     * 
     * @param bioCond
     */
    public void deleteBioCond(BioCondition bioCond) {
        bioConds.remove(bioCond.getName());
    }

    /**
     * Return all GeneExpression data contains in all BioCondition
     * 
     * @return
     */
    public ArrayList<GeneExpression> getGeneExprs() {
        ArrayList<GeneExpression> geneExprs = new ArrayList<GeneExpression>();
        for (BioCondition bioCond : getBioConds().values()) {
            for (GeneExpression geneExpr : bioCond.getGeneExprs()) {
                geneExprs.add(geneExpr);
            }
        }
        return geneExprs;
    }

    /**
     * Return all Tiling data contains in all BioCondition
     * 
     * @return
     */
    public ArrayList<Tiling> getTilings() {
        ArrayList<Tiling> tilings = new ArrayList<Tiling>();
        for (BioCondition bioCond : getBioConds().values()) {
            for (Tiling tiling : bioCond.getTilings()) {
                tilings.add(tiling);
            }
        }
        return tilings;
    }

    /**
     * Return all NGS data contains in all BioCondition
     * 
     * @return
     */
    public ArrayList<NGS> getRNASeqs() {
        ArrayList<NGS> ngss = new ArrayList<NGS>();
        for (BioCondition bioCond : getBioConds().values()) {
            for (NGS ngs : bioCond.getNGSSeqs()) {
                ngss.add(ngs);
            }
        }
        return ngss;
    }

    /*
     * ************************************************************** Load and Save
     * **************************************************************
     */
    /**
     * Read compressed, serialized data with a FileInputStream. Uncompress that data with a
     * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream. Replace current data
     * with new data, and redraw everything.
     */
    public void load(String fileName) {
        // Create a file dialog to query the user for a filename.
        if (fileName != null) { // If user didn't click "Cancel".
            try {
                // Create necessary input streams
                FileInputStream fis = new FileInputStream(fileName); // Read from file
                GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
                ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
                // Read in an object. It should be a vector of scribbles
                Experiment exp = (Experiment) in.readObject();
                in.close(); // Close the stream.
                this.setBioConds(exp.getBioConds());
                this.setName(exp.getName());
            }
            // Print out exceptions. We should really display them in a dialog...
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Prompt the user for a filename, and save the scribble in that file. Serialize the vector of lines
     * with an ObjectOutputStream. Compress the serialized objects with a GZIPOutputStream. Write the
     * compressed, serialized data to a file with a FileOutputStream. Don't forget to flush and close
     * the stream.
     */
    public void save(String fileName) {
        // Create a file dialog to query the user for a filename.
        if (fileName != null) { // If user didn't click "Cancel".
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
    }

    /*
     * ************************************************************** Getter and Setters
     * **************************************************************
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedHashMap<String, BioCondition> getBioConds() {
        return bioConds;
    }

    public ArrayList<BioCondition> getBioConditions() {
        ArrayList<BioCondition> bioConds = new ArrayList<BioCondition>();
        for (BioCondition bioCond : getBioConds().values()) {
            bioConds.add(bioCond);
        }
        return bioConds;
    }

    public void setBioConds(LinkedHashMap<String, BioCondition> bioConds) {
        this.bioConds = bioConds;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
