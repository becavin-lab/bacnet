/**
 * 
 */
package bacnet.genomeBrowser.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;

/**
 * @author Chris
 *
 */
public class Track implements Serializable, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 4061565768191225185L;
    public static String EXTENSION = ".tck";

    public enum DisplayType {
        BIOCOND, OVERLAY, DATA
    }

    // data arguments
    private DataTrack datas = new DataTrack(this);

    // Genome data
    private String genomeName = "";
    private String chromosomeID = "";
    private transient Chromosome chromosome;

    // display arguments
    private Region dataRegion;
    private Region displayRegion;
    private Zoom zoom;
    private DisplayType displayType = DisplayType.BIOCOND;

    private boolean displaySequence = false;

    public boolean trackChangedFlag = false;

    public Track() {

    }

    /**
     * Set every parameter using genome and chromosome number
     * 
     * @param genome
     * @param chromoNb
     */
    public Track(Genome genome, String chromoID) {
        this.setChromosomeID(chromoID);
        this.setChromosome(genome.getChromosomes().get(chromoID));
        this.setGenomeName(genome.getSpecies());
        Region region = new Region(0, genome.getChromosomes().get(chromoID).getLength());
        this.setDataRegion(region);
        Zoom zoom = new Zoom(region.getWidth());
        this.setZoom(zoom);
        int begin = 0;
        int end = 5000;
        Region displayRegion = new Region(begin, end);
        zoom.setZoomPosition(displayRegion);
        this.setDisplayRegion(displayRegion);
        this.setDatas(new DataTrack(this));
    }

    public void setNewChromosome(String chromoID) {
        this.setChromosomeID(chromoID);
        Genome genome = Genome.loadGenome(genomeName);
        this.setChromosome(genome.getChromosomes().get(chromoID));
        this.getDatas().initDisplayBoolean();
        Region region = new Region(0, genome.getChromosomes().get(chromoID).getLength());
        this.setDataRegion(region);
        Zoom zoom = new Zoom(region.getWidth());
        this.setZoom(zoom);
        int begin = 0;
        int end = 5000;
        Region displayRegion = new Region(begin, end);
        zoom.setZoomPosition(displayRegion);
        this.setDisplayRegion(displayRegion);
    }

    public void moveHorizontally(int selection) {
        displayRegion.moveHorizontally(selection);
        displayRegion.validateRegion(dataRegion.getWidth());
    }

    public void zoom(boolean in) {
        zoom.zoomRegion(displayRegion, in);
        displayRegion.validateRegion(dataRegion.getWidth());
    }

    /**
     * Search a text file in the annotation<br>
     * <li>Search if it is a position and go
     * <li>Search if it is contains in <code>getChromosome().getAllElements().keySet()</code>
     * <li>Search if it is contains in <code>gene.getName()</code><br>
     * All the search are case insensitive using <code>text.toUpperCase();</code>
     * 
     * @param text
     * @return
     */
    public boolean search(String text) {
        try {
            /*
             * If a base pair position has been wrote, it will reach it directly
             */
            int position = Integer.parseInt(text);
            moveHorizontally(position);
            return true;
        } catch (Exception e) {
            /*
             * Search if there is an entry for the element in the annotation
             */
            if (getChromosome().getAllElements().get(text) != null) {
                Sequence sequence = getChromosome().getAllElements().get(text);
                if (sequence.isStrand())
                    moveHorizontally(sequence.getBegin());
                else
                    moveHorizontally(sequence.getEnd());
                return true;
            }

            /*
             * go through annotation and search for an element
             */
            text = text.toUpperCase();
            /*
             * Search first Srna
             */
            for (Srna sRNA : getChromosome().getsRNAs().values()) {
                String name = sRNA.getName();
                String nameTemp = name.toUpperCase();
                if (nameTemp.contains(text)) {
                    Sequence seq = getChromosome().getAllElements().get(name);
                    if (seq.isStrand())
                        moveHorizontally(seq.getBegin());
                    else
                        moveHorizontally(seq.getEnd());
                    return true;
                }
            }
            /*
             * Search Gene
             */
            for (Gene gene : getChromosome().getGenes().values()) {
                String geneName = gene.getName();
                String geneNameTemp = geneName.toUpperCase();
                if (geneNameTemp.contains(text)) {
                    Sequence seq = getChromosome().getAllElements().get(geneName);
                    if (seq.isStrand())
                        moveHorizontally(seq.getBegin());
                    else
                        moveHorizontally(seq.getEnd());
                    return true;
                }
                geneName = gene.getGeneName();
                geneNameTemp = geneName.toUpperCase();
                if (geneNameTemp.contains(text)) {
                    Sequence seq = getChromosome().getAllElements().get(gene.getName());
                    if (seq.isStrand())
                        moveHorizontally(seq.getBegin());
                    else
                        moveHorizontally(seq.getEnd());
                    return true;
                }
            }
            /*
             * Search the rest
             */
            for (String name : getChromosome().getAllElements().keySet()) {
                String nameTemp = name.toUpperCase();
                if (nameTemp.equals(text)) {
                    Sequence seq = getChromosome().getAllElements().get(name);
                    if (seq.isStrand())
                        moveHorizontally(seq.getBegin());
                    else
                        moveHorizontally(seq.getEnd());
                    return true;
                } else if (nameTemp.contains(text)) {
                    Sequence seq = getChromosome().getAllElements().get(name);
                    if (seq.isStrand())
                        moveHorizontally(seq.getBegin());
                    else
                        moveHorizontally(seq.getEnd());
                    return true;
                }
            }
            System.out.println("not found");
        }
        return false;
    }

    /**
     * Search an NTerm among all MassSpecData
     * 
     * @param text
     * @param datas
     * @return
     */
    public NTerm searchNterm(String text) {
        for (BioCondition bioCond : datas.getBioConditionHashMaps().values()) {
            for (NTermData data : bioCond.getnTerms()) {
                if (data.getElements().containsKey(text)) {
                    NTerm nTerm = data.getElements().get(text);
                    if (nTerm.isStrand())
                        moveHorizontally(nTerm.getBegin());
                    else
                        moveHorizontally(nTerm.getEnd());
                    return nTerm;
                }
            }
        }
        return null;
    }

    /**
     * Return the MassSpecData where an Nterm can be found
     * 
     * @param nTerm
     * @param datas
     * @return
     */
    public NTermData correspondingMassSpecData(NTerm nTerm) {
        for (BioCondition bioCond : datas.getBioConditionHashMaps().values()) {
            for (NTermData data : bioCond.getnTerms()) {
                if (data.getElements().containsKey(nTerm.getName())) {
                    return data;
                }
            }
        }
        return null;
    }
    //
    // @Override
    // public String toString(){
    // String ret = "dispRegion: "+displayRegion.toString()+" in "+displayType+"\n";
    // ret+=zoom+"\n";
    // for(String bioCondition : datas.getBioConditionHashMaps().keySet()){
    // ret+=" data: "+bioCondition+" type: "+"\n";
    // }
    // return ret;
    // }

    /**
     * Save Track information to a text file
     * 
     * @return
     */
    public String savetoTextFile() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String fileText = "#Data visualization saved: " + dateFormat.format(date) + "\n";
        fileText += "#BioCondition" + "\n";
        for (String bioCondition : getDatas().getBioConditionHashMaps().keySet()) {
            if (bioCondition.contains(" vs ")) {
                fileText += BioCondition.parseName(bioCondition)[0] + "\n";
            } else {
                fileText += bioCondition + "\n";
            }

        }
        fileText += "#DataNotDisplayed" + "\n";
        for (String data : getDatas().getDataNOTDisplayed()) {
            fileText += data + "\n";
        }
        fileText += "#Genome" + "\n";
        fileText += this.getGenomeName() + "\n" + this.getChromosomeID() + "\n";
        fileText += "#Style" + "\n";
        fileText += this.getDisplayType() + "\n";
        fileText += "AbsoluteValueDisplayed=" + this.getDatas().isDisplayAbsoluteValue() + "\n";
        fileText += "position=" + this.getDisplayRegion().getMiddleH() + "\n";
        fileText += "zoom horizontal=" + this.getZoom().getZoomPosition() + "\n";
        fileText += "display sequence=" + this.isDisplaySequence() + "\n";
        return fileText;
    }

    /**
     * Load a Track from a text file
     * 
     * @param textTrack
     * @return
     */
    public static Track loadFromText(String textTrack) {
        /*
         * Parse file
         */

        String[] parsedTrack = textTrack.split("#");
        // for(int i=0;i<parsedTrack.length;i++){
        // System.out.println(i+" - "+parsedTrack[i]);
        // }

        /*
         * BioCondition
         */
        ArrayList<String> bioCondNames = new ArrayList<String>();
        for (String bioCondName : parsedTrack[2].split("\n")) {
            if (!bioCondName.equals("BioCondition") && !bioCondName.equals("")) {
                bioCondNames.add(bioCondName);
            }
        }
        /*
         * DataNotDisplayed
         */
        ArrayList<String> dataNotDisplayed = new ArrayList<String>();
        for (String data : parsedTrack[3].split("\n")) {
            if (!data.equals("DataNotDisplayed") && !data.equals("")) {
                dataNotDisplayed.add(data);
            }
        }
        /*
         * Genome
         */
        String genomeName = parsedTrack[4].split("\n")[1];
        String chromoID = parsedTrack[4].split("\n")[2];
        /*
         * Style
         */
        DisplayType displayType = DisplayType.valueOf(parsedTrack[5].split("\n")[1]);
        int sliderPosition = Integer.parseInt(parsedTrack[5].split("\n")[2].split("=")[1]);
        boolean absoluteValueDisplayed = Boolean.parseBoolean(parsedTrack[5].split("\n")[3].split("=")[1]);

        /*
         * Create Track
         */
        Track track = new Track(Genome.loadGenome(genomeName), chromoID);
        track.getDatas().setDisplayAbsoluteValue(absoluteValueDisplayed);
        for (String bioCondName : bioCondNames) {
            track.getDatas().addBioCondition(bioCondName);
        }
        track.setDisplayType(displayType);
        track.getDatas().setDataNOTDisplayed(dataNotDisplayed);
        track.getDatas().setDataColors();
        track.getDatas().setDataSizes();
        track.moveHorizontally(sliderPosition);
        return track;
    }

    /**
     * Load Track from a serialized file
     * 
     * @param fileName
     * @return
     */
    public static Track load(String fileName) {
        if (fileName != null) {
            try {
                // Create necessary input streams
                FileInputStream fis = new FileInputStream(fileName); // Read from file
                GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
                ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
                // Read in an object. It should be a vector of scribbles
                Track track = (Track) in.readObject();
                in.close(); // Close the stream.
                return track;
            }
            // Print out exceptions. We should really display them in a dialog...
            catch (Exception e) {
                System.out.println(e);
            }
        }
        return null;
    }

    public void save(String fileName) {
        if (fileName != null) {
            try {
                // Create the necessary output streams to save the scribble.
                FileOutputStream fos = new FileOutputStream(fileName);
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                ObjectOutputStream out = new ObjectOutputStream(gzos);
                out.writeObject(this); // Write the entire Vector of scribbles
                out.flush(); // Always flush the output.
                out.close(); // And close the stream.
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    // **************************************
    // ******* Getters and Setters ******
    // **************************************

    public Chromosome getChromosome() {
        return chromosome;
    }

    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genome) {
        this.genomeName = genome;
    }

    public Region getDataRegion() {
        return dataRegion;
    }

    public void setDataRegion(Region dataRegion) {
        this.dataRegion = dataRegion;
    }

    public Region getDisplayRegion() {
        return displayRegion;
    }

    public void setDisplayRegion(Region displayRegion) {
        this.displayRegion = displayRegion;
    }

    public Zoom getZoom() {
        return zoom;
    }

    public void setZoom(Zoom zoom) {
        this.zoom = zoom;
    }

    public DisplayType getDisplayType() {
        return displayType;
    }

    public void setDisplayType(DisplayType displayType) {
        this.displayType = displayType;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public DataTrack getDatas() {
        return datas;
    }

    public void setDatas(DataTrack datas) {
        this.datas = datas;
    }

    public String getChromosomeID() {
        return chromosomeID;
    }

    public void setChromosomeID(String chromosomeID) {
        this.chromosomeID = chromosomeID;
    }

    public boolean isDisplaySequence() {
        return displaySequence;
    }

    public void setDisplaySequence(boolean displaySequence) {
        this.displaySequence = displaySequence;
    }

}
