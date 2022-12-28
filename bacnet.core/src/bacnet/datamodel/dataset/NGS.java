package bacnet.datamodel.dataset;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;
import org.biojava3.core.sequence.io.BufferedReaderBytesRead;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Genome;
import bacnet.utils.ArrayUtils;
import bacnet.utils.MathUtils;
import bacnet.utils.VectorUtils;

/**
 * Class for manipulating various RNASeq data, herited from ExpressionData <br>
 * double[] value is of genome size, when ones want to see RNAseq expression at a certain bp
 * position, just extract values[bpposition]<br>
 * Need to be loaded first, and unlog has RNASeq will be logged for display when convert in the
 * platform
 * 
 * @author UIBC
 *
 */
public class NGS extends OmicsData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3283911438157029331L;
    public static String EXTENSION = ".ngs";

    private HashMap<String, ExpressionData> datasets = new HashMap<>();

    private String genomeName = "";

    private boolean logTransformed = true;

    /**
     * indicate if the serializable part of the object TranscriptomeData has been read or not
     */
    private transient boolean infoRead = false;

    public NGS() {}

    public NGS(String name, String genomeName) {
        this.setName(name);
        setType(TypeData.RNASeq);
        setGenomeName(genomeName);
    }

    /**
     * Load information on the datasets: name, date, note, stat, etc ... The path for data loading will
     * be: PATH_STREAMING+this.getName()+"_"chromoID+"Info"+this.EXTENSION;
     */
    public void load() {
        Genome genome = Genome.loadGenome(genomeName);
        for (String chromoID : genome.getChromosomes().keySet()) {
            String name = this.getName() + "_" + chromoID;
            String fileName = PATH_STREAMING + name + "Info" + ExpressionData.EXTENSION;
            //System.out.println(fileName);
            ExpressionData dataset = this.getDatasets().get(chromoID);
            dataset.load(fileName, false);
            getDatasets().put(chromoID, dataset);
        }
        infoRead = true;
    }

    
    /**
     * Load the entire TranscriptomeData
     * 
     * @throws IOException
     */
    public void read() {
        for (String chromoID : datasets.keySet()) {
            ExpressionData dataset = datasets.get(chromoID);
            if (!dataset.isInfoRead()) {
                load();
            }
            String fileName = PATH_STREAMING + getName() + "_" + chromoID + ExpressionData.EXTENSION;
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
                dataset.setValues(new double[dataset.getLength()]);
                dataset.setRead(new boolean[dataset.getLength()]);
                for (int i = 0; i < dataset.getValues().length; i++) {
                    dataset.getValues()[i] = in.readDouble();
                    dataset.getRead()[i] = true;
                }
                in.close();
            } catch (IOException e) {
                System.err.println("Cannot read data all the data" + this.getName());
                e.printStackTrace();
            }
        }
    }

    /**
     * Save stat and values in two separate files <br>
     * Saving is done via DataStream to allow streaming reading
     * 
     * @throws IOException
     */
    public void save(boolean saveValues) {
        Genome genome = Genome.loadGenome(genomeName);
        for (String chromoID : genome.getChromosomes().keySet()) {
            // save stat data
            String fileName = PATH_STREAMING + getName() + "_" + chromoID;
            String statfileName = fileName + "Info" + ExpressionData.EXTENSION;
            ExpressionData dataset = datasets.get(chromoID);
            try {
                // Create the necessary output streams to save the scribble.
                FileOutputStream fos = new FileOutputStream(statfileName);
                // Save to file
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                // Compressed
                ObjectOutputStream out = new ObjectOutputStream(gzos);
                // Save objects
                out.writeObject(dataset); // Write the entire Vector of scribbles
                out.flush(); // Always flush the output.
                out.close(); // And close the stream.
            }
            // Print out exceptions. We should really display them in a dialog...
            catch (IOException e) {
                System.out.println(e);
            }

            if (saveValues) {
                // save double[] values
                fileName = fileName + ExpressionData.EXTENSION;
                System.out.println("save: " + fileName);
                try {
                    DataOutputStream out =
                            new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
                    for (int i = 0; i < dataset.getValues().length; i++) {
                        out.writeDouble(dataset.getValues()[i]);
                    }
                    out.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    /**
     * Get value at a certain base pair position and unlog it by calculating 2^value<br>
     * It works faster if data has been already loaded by this.read() or this.read(from,to)
     */
    public double getUnlogValue(int bpPosition, String chromoID) {
        ExpressionData dataset = datasets.get(chromoID);
        double value = dataset.getValue(bpPosition);
        value = Math.pow(2, value);
        if (value == 1)
            value = 0;
        return value;
    }

    /**
     * Run the conversion of wigfiles
     * @param data
     * @param logTransformed
     */
    public void convert(NGS data, boolean logTransformed) {
        convertWigFile(data, logTransformed);
    }

    /**
     * WIG file are common RNASeq data<br>
     * Calculate first the MEDIAN value of the different replicates ad then log2 transform and save the
     * NGS data Two columns: <br>
     * <li>one for the base pair position
     * <li>one for the coverage value
     * 
     * @param rawDataPath
     * @param genome
     */
    public void convertWigFile(NGS data, boolean logTransformed) {
        if (logTransformed) {
            System.out.println("Log transformed RNASEq data!");
        }

        /*
         * Create the dataset
         */
        this.setGenomeName(genomeName);
        Genome genome = Genome.loadGenome(data.getGenomeName());
        for (Chromosome chromo : genome.getChromosomes().values()) {
            /*
             * Preload all the data!
             */
            HashMap<String, ArrayList<String>> hashmapData = new HashMap<>();
            for (String rawDataPath : this.getRawDatas()) {
                ArrayList<String> listRow =
                        readWIGChromo(ExpressionData.PATH_NGS_NORM + rawDataPath, chromo.getChromosomeID(), genome);
                if (listRow.size() == 0) {
                    System.err.println("No RNASeq value for chromo:" + chromo.getChromosomeID() + "for dataset "
                            + ExpressionData.PATH_NGS_NORM + rawDataPath);
                    for (int i = 1; i < (chromo.getLength() + 1); i++) {
                        listRow.add(i + "\t0");
                    }
                }
                hashmapData.put(rawDataPath, listRow);
            }

            String chromoID = chromo.getChromosomeID();
            ExpressionData dataset = data.getDatasets().get(chromo.getChromosomeID());
            dataset.setLength(chromo.getLength() + 1);
            dataset.setValues(new double[dataset.getLength()]);
            dataset.setRead(new boolean[dataset.getLength()]);
            double[] values = new double[dataset.getLength()];

            System.out.println("Chromo: " + chromo.getLength() + " data:" + dataset.getLength());

            /*
             * Get values in each array and add them (raw data read) First column will be the base pair index in
             * the genome Second column will be the coverage value
             */
            double[][] tempValues = new double[dataset.getLength()][this.getRawDatas().size()];
            int k = 0;
            for (String rawDataPath : this.getRawDatas()) {
                System.out.println(rawDataPath + "   data length: " + chromo.getLength());
                ArrayList<String> listRow = hashmapData.get(rawDataPath);

                if (rawDataPath.endsWith(".wig")) {
                    if (listRow.size() != 0) {
                        for (int i = 0; i < listRow.size(); i++) {
                            // position in the genome in the .wig file
                            int indexBP = Integer.parseInt(listRow.get(i).split("\t")[0]) - 1;
                            // position in the genome in the .wig file
                            double coverage = Double.parseDouble(listRow.get(i).split("\t")[1]);
                            tempValues[indexBP][k] = coverage;
                        }
                    } else {
                        System.err.println("WIG file not recognized: " + ExpressionData.PATH_NGS_RAW + rawDataPath);
                    }
                } else {
                    System.err.println(rawDataPath + "Not a wig file. You need to convert your file to WIG file");
                }
                k++;
            }
            /*
             * Calculate the average value of the different rawData
             */
            for (int i = 0; i < values.length; i++) {
                values[i] = VectorUtils.median(ArrayUtils.getRow(tempValues, i));
                // valuesString[i][0] = i+"";
                // valuesString[i][1] = values[i]+"";
            }

            /*
             * Log2 transform for better visualisation
             */
            if (logTransformed) {
                double[] newValues = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    if (values[i] < 0) {
                        newValues[i] = - MathUtils.log2(-values[i]);
                    } else if (values[i] == 0) {
                        newValues[i] = 0; // log2(0)=null so we transform to 1
                    } else if (values[i] < 1) {
                        newValues[i] = 1; // log2(0.5) is negative so we transform to 1
                    } else {
                        newValues[i] = MathUtils.log2(values[i]);
                    }
                    
                }
                values = newValues;
            }
       

            /*
             * Copy values into the data
             */
            for (int i = 0; i < values.length; i++) {
                dataset.getValues()[i] = values[i];
                dataset.getRead()[i] = true;
            }
            /*
             * Set statistical values on the data
             */
            dataset.setMax(VectorUtils.max(dataset.getValues()));
            dataset.setMin(VectorUtils.min(dataset.getValues()));
            dataset.setMean(VectorUtils.mean(dataset.getValues()));
            dataset.setMedian(VectorUtils.median(dataset.getValues()));
            dataset.setVariance(VectorUtils.variance(dataset.getValues()));

            // save double[] values and remove it to release memory
            String fileName = PATH_STREAMING + getName() + "_" + chromoID;
            fileName = fileName + ExpressionData.EXTENSION;
            System.out.println("save: " + fileName);
            try {
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
                for (int i = 0; i < dataset.getValues().length; i++) {
                    out.writeDouble(dataset.getValues()[i]);
                }
                out.close();
            } catch (IOException e) {
                System.out.println(e);
            }
            dataset.setValues(new double[0]);
            dataset.setRead(new boolean[0]);

            this.getDatasets().put(chromo.getChromosomeID(), dataset);
        }

    }

    /**
     * Create RNASeq data from the comparison of this one and data2
     * 
     * @param data2
     * @param calcData true if we want to calculate the data
     * @return
     * @throws IOException
     */
    public NGS compare(NGS data2, boolean calcData) {
        
    	if (calcData) {
            this.read();
            data2.read();
        } else {
            // do not create the vector double[] et read[]
            this.load();
        }
        NGS compRNASeq = new NGS(this.getName() + "_vs_" + data2.getName(), this.getGenomeName());
        compRNASeq.setBioCondName(this.getBioCondName() + "_vs_" + data2.getBioCondName());
        
        GeneExpression compData = new GeneExpression(this.getName() + "_vs_" + data2.getName());
       compData.setBioCondName(this.getBioCondName() + "_vs_" + data2.getBioCondName());

        
        /*
        for (String chromoID : this.getDatasets().keySet()) {
            ExpressionData dataset1 = this.getDatasets().get(chromoID);
            ExpressionData dataset2 = data2.getDatasets().get(chromoID);

            ExpressionData compData = new ExpressionData(this.getName() + " vs " + data2.getName());
            compData.setBioCondName(this.getBioCondName() + " vs " + data2.getBioCondName());
            compData.setLength(dataset1.getLength());
            compData.setType(TypeData.RNASeq);
            compData.setChromosomeID(chromoID);
            compData.setGenomeName(dataset1.getGenomeName());
            if (calcData) {
                double[] values = new double[dataset1.getLength()];
                boolean[] read = new boolean[dataset1.getLength()];
                for (int j = 0; j < values.length; j++) {
                    if (dataset1.getValues()[j] == ExpressionMatrix.MISSING_VALUE
                            || dataset2.getValues()[j] == ExpressionMatrix.MISSING_VALUE) {
                        values[j] = ExpressionMatrix.MISSING_VALUE;
                    } else {
                        // if(dataset1.getName().contains("DNA")){
                        // if(dataset1.getValues()[j] > dataset2.getValues()[j]){
                        // values[j] = dataset1.getValues()[j] / dataset2.getValues()[j];
                        // }else{
                        // values[j] = - dataset2.getValues()[j] / dataset1.getValues()[j];
                        // }
                        // }else{
                        values[j] = dataset1.getValues()[j] - dataset2.getValues()[j];
                        // }
                    }
                    read[j] = true;
                }
                compData.setValues(values);
                compData.setRead(read);
                compData.setInfoRead(true);
                compData.setStat();
            }
            compRNASeq.getDatasets().put(chromoID, compData);
        }*/

        return compRNASeq;
    }

    /**
     * Will read your wig file and parse it to BACNET <br>
     * ONLY WIG FILE WITH variableStep and raw format are accepted No fixedStep format
     * 
     * @param fileName
     * @param chromoName
     * @return list of position and the corresponding coverage on chromoName chromosome
     */
    public ArrayList<String> readWIGChromo(String fileName, String chromoName, Genome genome) {
        System.out.println("Read WIG: " + fileName);
        ArrayList<String> list = new ArrayList<String>();
        try {
            File file = new File(fileName);
            FileInputStream fi = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fi);
            BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

            String line = br.readLine();
            /*
             * Detect the type of WIG file
             */
            if (line.contains("variableStep")) {
                /*
                 * variableStep format for WIG file
                 */
                String chromoNameLine = "";
                int span = 1;
                while (line != null) {
                    if (line.contains("variableStep")) {
                        line = line.replace("variableStep chrom=", "").trim();
                        chromoNameLine = line.split(" ")[0];
                        if (line.contains("span=")) {
                            span = Integer.parseInt(line.replace("span=", "").split(" ")[1]);
                        } else {
                            span = 1;
                        }
                    } else {
                        if (chromoNameLine.equals(chromoName)) {
                            int begin = Integer.parseInt(line.split("\t")[0]);
                            double coverage = Double.parseDouble(line.split("\t")[1]);
                            for (int k = 0; k < span; k++) {
                                String result = (begin + k) + "\t" + coverage;
                                list.add(result);
                            }
                        }

                    }
                    line = br.readLine();
                }
            } else {
                /*
                 * WIG file with only "chromo \t position \t coverage"
                 */
                // Read the lines and put them in ArrayList
                String chromoFirst = genome.getFirstChromosome().getChromosomeID();
                while (line != null) {
                    String[] lineSplit = line.split("\t");
                    if (lineSplit.length == 3) {
                        String chromoNameLine = lineSplit[0];
                        if (chromoNameLine.equals(chromoName)) {
                            int begin = Integer.parseInt(lineSplit[1]);
                            double coverage = Double.parseDouble(lineSplit[2]);
                            String result = begin + "\t" + coverage;
                            list.add(result);
                        }
                    } else if (lineSplit.length == 2) {
                        if (chromoFirst.equals(chromoName)) {
                            int begin = Integer.parseInt(lineSplit[0]);
                            double coverage = Double.parseDouble(lineSplit[1]);
                            String result = begin + "\t" + coverage;
                            list.add(result);
                        }
                    }
                    line = br.readLine();
                }
            }
            br.close();
            isr.close();
            // If stream was created from File object then we need to close it
            if (fi != null) {
                fi.close();
            }
        } catch (Exception e) {
            System.err.println("Cannot read:" + fileName);
        }
        return list;
    }

    /*
     * Getter and Setter
     */

    public boolean isInfoRead() {
        return infoRead;
    }

    public void setInfoRead(boolean infoRead) {
        this.infoRead = infoRead;
    }

    public HashMap<String, ExpressionData> getDatasets() {
        return datasets;
    }

    public void setDatasets(HashMap<String, ExpressionData> datasets) {
        this.datasets = datasets;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genomeName) {
        this.genomeName = genomeName;
    }

    public boolean isLogTransformed() {
        return logTransformed;
    }

    public void setLogTransformed(boolean logTransformed) {
        this.logTransformed = logTransformed;
    }

}
