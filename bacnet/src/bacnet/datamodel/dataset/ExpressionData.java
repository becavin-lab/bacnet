package bacnet.datamodel.dataset;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.utils.VectorUtils;

/**
 * Abstract Class describing data related to transcriptome <br>
 * This data are saved in DataStream and might be read in streaming mode <br>
 * <br>
 * boolean[] read is used as a "read" indicator <br>
 * As soon as a position in double[] values has been read, boolean[] read is updated <br>
 * 
 * 
 * @author Chris
 *
 */
public class ExpressionData extends OmicsData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7625087278597156645L;

    private boolean alreadyRead = false;

    /*
     * stat values
     */
    private int length;
    private double min = 1000000;
    private double max = -10000000;
    private double median;
    private double mean;
    private double variance;

    private String genomeName = "";
    private String chromosomeID = "";
    /**
     * indicate if the serializable part of the object TranscriptomeData has been read or not
     */
    private transient boolean infoRead = false;

    /**
     * ExpressionMatrix used only at the creation of the data
     */
    private transient ExpressionMatrix matrix;
    /**
     * Array containing all the values, which is read in streaming mode
     */
    private transient double[] values;
    /**
     * Array used during streaming reading, to indicate wether or not a part of the data has been read
     */
    private transient boolean[] read;

    public ExpressionData() {}

    public ExpressionData(String name) {
        setName(name);
    }

    /**
     * Return the value at the right position in the double[] values argument<br>
     * Read the data if necessary
     * 
     * @param position
     * @return
     */
    public double getValue(int position) {
        // if(read[position]){
        // return getValues()[position];
        // }else{
        read(position);
        return getValues()[position];
        // }
    }

    /**
     * Save stat and values in two separate files <br>
     * Saving is done via DataStream to allow streaming reading
     * 
     * @throws IOException
     */
    public void save() {
        // save stat data
        String fileName = PATH_STREAMING + getName();
        String statfileName = fileName + "Info" + EXTENSION;

        try {
            // Create the necessary output streams to save the scribble.
            FileOutputStream fos = new FileOutputStream(statfileName);
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

        // save double[] values
        fileName = fileName + EXTENSION;
        System.out.println("save: " + fileName);
        try {
            System.out.println(fileName);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            for (int i = 0; i < values.length; i++) {
                // System.out.println(values[i]);
                out.writeDouble(values[i]);
            }
            out.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Load information on the data: name, date, note, stat, etc ... The path for data loading will be:
     * PATH_STREAMING+this.getName()+"Info"+this.EXTENSION;
     */
    public void load() {
        String fileName = PATH_STREAMING + getName() + "Info" + EXTENSION;
        load(fileName);
    }

    /**
     * Load information on the data: name, date, note, stat, etc ... The path for data loading will be:
     * PATH_STREAMING+this.getName()+"Info"+this.EXTENSION;
     */
    public void load(boolean createValues) {
        String fileName = PATH_STREAMING + getName() + "Info" + EXTENSION;
        load(fileName, createValues);
    }

    /**
     * Load information on the data: name, date, note, stat, etc ...
     * 
     * @param fileName Path for the file "Info.data"
     * @throws IOException
     */
    public void load(String fileName) {
        load(fileName, true);
    }

    /**
     * Load information on the data: name, date, note, stat, etc ...
     * 
     * @param fileName Path for the file "Info.data"
     * @throws IOException
     */
    public void load(String fileName, boolean createValues) {
        try {
            // Create necessary input streams
            FileInputStream fis = new FileInputStream(fileName); // Read from file
            GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
            ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
            // Read in an object. It should be a vector of scribbles
            ExpressionData data = (ExpressionData) in.readObject();
            in.close();
            setName(data.getName());
            setNote(data.getNote());
            setType(data.getType());
            setDate(data.getDate());
            setBioCondName(data.getBioCondName());
            mean = data.getMean();
            median = data.getMedian();
            if (data.getName().contains("DNA")) {
                median = 1;
            }
            max = data.getMax();
            min = data.getMin();
            length = data.getLength();
            variance = data.getVariance();
            infoRead = true;
            if (createValues) {
                setValues(new double[getLength()]);
                setRead(new boolean[getLength()]);
            } else {
                setValues(new double[1]);
                setRead(new boolean[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the entire TranscriptomeData
     * 
     * @throws IOException
     */
    public void read() {
        if (!isInfoRead()) {
            load();
        }
        String fileName = PATH_STREAMING + getName() + EXTENSION;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            values = new double[length];
            read = new boolean[length];
            for (int i = 0; i < values.length; i++) {
                values[i] = in.readDouble();
                read[i] = true;
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Cannot read data all the data" + this.getName());
            e.printStackTrace();
        }
    }

    /**
     * Read only one position in the data
     * 
     * @param position line index in the data
     * @throws IOException
     */
    public double read(int position) {
        if (!isInfoRead()) {
            load();
        }
        String fileName = PATH_STREAMING + getName() + EXTENSION;
        // System.out.println(fileName);
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            int doubleSize = 8;
            in.skip((long) (doubleSize * position));
            // if(!read[position]){ // readData
            // values[position] = in.readDouble();
            // read[position] = true;
            // }
            double value = in.readDouble();
            in.close();
            return value;
        } catch (IOException e) {
            System.err.println("Cannot read data at position: " + position + " for " + this.getName());
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * Read only one position in the data
     * 
     * @param position line index in the data
     * @throws IOException
     */
    public double read(int position, boolean useChromo) {
        if (!isInfoRead()) {
            load();
        }
        String fileName = PATH_STREAMING + getName() + "_" + chromosomeID + ".rnaseq";
        // System.out.println(fileName);
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            int doubleSize = 8;
            in.skip((long) (doubleSize * position));
            // if(!read[position]){ // readData
            // values[position] = in.readDouble();
            // read[position] = true;
            // }
            double value = in.readDouble();
            in.close();
            return value;
        } catch (IOException e) {
            System.err.println("Cannot read data at position: " + position + " for " + this.getName());
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * Read the TranscriptomeData between begin and end <br>
     * Careful: begin < end <br>
     * It is a streaming reading so it check if the data are already read or not and act consequently
     * 
     * @param begin first index in double[] values
     * @param end last index in double[] values
     * @return a vector double[] containing all the values
     * @throws IOException
     */
    public double[] read(int begin, int end) {
        if (!isInfoRead()) {
            load();
        }
        String fileName = PATH_STREAMING + getName() + EXTENSION;
        if (isNGS()) {
            fileName = PATH_STREAMING + getName() + "_" + this.getChromosomeID() + NGS.EXTENSION;
        }
        DataInputStream in;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));

            int doubleSize = 8;
            in.skip((long) (doubleSize * begin));
            // System.out.println("Read:"+this.getName()+" from "+begin+" to "+end+"
            // starting at: "+(long)(doubleSize*begin));
            double[] values = new double[end - begin + 1];
            int k = 0;
            for (int i = begin; i < end; i++) {
                Double value = in.readDouble();
                values[k] = value;
                k++;
            }
            in.close();
            return values;
        } catch (IOException e) {
            System.err.println("Cannot read data at position: " + begin + " to " + end + " for " + this.getName());
            e.printStackTrace();
            return new double[0];
        }
    }

    /**
     * Set statistical parameters using values array
     */
    public void setStat() {
        this.setMax(VectorUtils.max(VectorUtils.deleteMissingValue(this.getValues())));
        this.setMin(VectorUtils.min(VectorUtils.deleteMissingValue(this.getValues())));
        this.setMean(VectorUtils.mean(VectorUtils.deleteMissingValue(this.getValues())));
        this.setMedian(VectorUtils.median(VectorUtils.deleteMissingValue(this.getValues())));
        this.setVariance(VectorUtils.variance(VectorUtils.deleteMissingValue(this.getValues())));
    }

    /**
     * Create Tiling data from the comparison of this one and data2
     * 
     * @param data2
     * @param calcData true if we want to calculate the data
     * @return
     * @throws IOException
     */
    public ExpressionData compare(ExpressionData data2, boolean calcData) {
        this.read();
        data2.read();
        ExpressionData compData = new ExpressionData(this.getName() + " vs " + data2.getName());
        compData.setBioCondName(this.getBioCondName() + " vs " + data2.getBioCondName());
        compData.setLength(this.getLength());
        if (calcData) {
            double[] values = new double[this.getLength()];
            boolean[] read = new boolean[this.getLength()];
            for (int j = 0; j < values.length; j++) {
                if (this.getValues()[j] == ExpressionMatrix.MISSING_VALUE
                        || data2.getValues()[j] == ExpressionMatrix.MISSING_VALUE) {
                    values[j] = ExpressionMatrix.MISSING_VALUE;
                } else {
                    values[j] = this.getValues()[j] - data2.getValues()[j];
                }
                read[j] = true;
            }
            compData.setValues(values);
            compData.setRead(read);
            compData.setInfoRead(true);
            compData.setStat();
        }
        return compData;
    }

    /**
     * Return corresponding BioCondition
     * 
     * @return
     */
    public BioCondition getBioCondition() {
        return BioCondition.getBioCondition(getBioCondName());
    }

    public boolean isNGS() {
        if (this.getType() == TypeData.DNASeq || this.getType() == TypeData.RNASeq || this.getType() == TypeData.RiboSeq
                || this.getType() == TypeData.TermSeq || this.getType() == TypeData.TSS) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * *************************************************************************** GETTERS and SETTERS
     * ***************************************************************************
     */

    public boolean isAlreadyRead() {
        return alreadyRead;
    }

    public void setAlreadyRead(boolean alreadyRead) {
        this.alreadyRead = alreadyRead;
    }

    public static String getPATH() {
        return PATH_STREAMING;
    }

    public static void setPATH(String pATH) {
        PATH_STREAMING = pATH;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public boolean isInfoRead() {
        return infoRead;
    }

    public void setInfoRead(boolean infoRead) {
        this.infoRead = infoRead;
    }

    public ExpressionMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(ExpressionMatrix matrix) {
        this.matrix = matrix;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    public boolean[] getRead() {
        return read;
    }

    public void setRead(boolean[] read) {
        this.read = read;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public static String getGeneralWtName() {
        return GENERAL_WT_NAME;
    }

    public static String getExtension() {
        return EXTENSION;
    }

    @Override
    public String toString() {
        String ret = getName() + " " + getDate() + "\n";
        ret += "min " + min + " mean " + mean + " max " + max + "\n";
        ret += "median " + median + " variance " + variance + " length " + length + " RawData: ";
        for (String rawData : getRawDatas())
            ret += " " + rawData + " ";
        return ret;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genomeName) {
        this.genomeName = genomeName;
    }

    public String getChromosomeID() {
        return chromosomeID;
    }

    public void setChromosomeID(String chromosomeID) {
        this.chromosomeID = chromosomeID;
    }

}
