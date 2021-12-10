package bacnet.datamodel.dataset;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import bacnet.Database;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.VectorUtils;

/**
 * 
 * Tiling array data from Listeria tiling chip ArrayExpress id : A-MEXP-1572 *
 * 
 * @author UIBC
 *
 */
public class Tiling extends ExpressionData {

    /**
     * 
     */
    private static final long serialVersionUID = -7904822758820760928L;

    public static String PROBES_PATH = Database.getDATA_PATH() + "StreamingData" + File.separator + "probeTiling.data";
    public static String PROBES_PATH_2 = OmicsData.PATH_TILING_NORM + File.separator + "probeTiling.data";

    public static String EXTENSION = ".gr";
    private transient TreeMap<Integer, Integer> probes;

    public Tiling() {
        setType(TypeData.Tiling);
    }

    public Tiling(String name) {
        super(name);
        setType(TypeData.Tiling);
        probes = Database.getInstance().getProbesTiling();
        setLength(probes.size());
        setValues(new double[getLength()]);
        setRead(new boolean[getLength()]);
    }

    /**
     * Transform a <code>Tiling</code> into an <code>ExpressionMatrix</code>
     * 
     * @return representation of the <code>GFeneExpression</code>
     */
    public ExpressionMatrix toExpressionMatrix() {
        this.read();
        ArrayList<String> headers = new ArrayList<>();
        String[] headersVector = {"LOGFC"};
        for (String header : headersVector)
            headers.add(header);
        ExpressionMatrix matrix = new ExpressionMatrix(headers);
        matrix.setName(getName());
        double[][] values = new double[Database.getInstance().getProbesTiling().size()][headers.size()];
        for (Integer probe : Database.getInstance().getProbesTiling().keySet()) {
            matrix.getRowNames().put(probe + "", Database.getInstance().getProbesTiling().get(probe));
            values[Database.getInstance().getProbesTiling().get(probe)][0] = get(probe);
        }
        matrix.setValues(values);
        return matrix;
    }

    @Override
    public void load() {
        super.load();
        probes = Database.getInstance().getProbesTiling();
    }

    public double get(int bpPosition) {
        int index = probes.get(probes.ceilingKey(bpPosition));
        return this.read(index);
    }

    public void setValue(int bpPosition, double value) {
        int index = probes.get(probes.ceilingKey(bpPosition));
        // System.out.println(bpPosition+" :probe: "+probes.ceilingKey(bpPosition));
        if (!isInfoRead()) {
            this.read();
        }
        this.getValues()[index] = value;
    }

    /**
     * read probe values between two positions in the genome <br>
     * important: bpbegin < bpend
     * 
     * @param bpbegin
     * @param bpend
     * @param extract define if we return the values or just read the data
     * @return
     */
    public double[] get(int bpbegin, int bpend, boolean extract) {
        int begin = getPosition(bpbegin);
        int end = getPosition(bpend);
        this.read();
        if (extract)
            return VectorUtils.subVector(this.getValues(), begin, end - 1);
        else
            return null;
    }

    /**
     * Return the index position in the tiling matrix of a particular probes closest to bpPosition
     * 
     * @param bpPosition
     * @return
     */
    public int getPosition(int bpPosition) {

        int lastProbe = probes.lastKey();
        if (bpPosition < lastProbe)
            return probes.get(probes.ceilingKey(bpPosition));
        else
            return probes.get(lastProbe);
    }

    /**
     * Load ExpressionMatrix found in TranscriptomeData.PATH_TILING_NORM + getName()
     */
    public void loadMatrix() {
        String[][] arrayTemp = TabDelimitedTableReader.read(PATH_TILING_NORM + getName());
        String[] firstRow = {"Probe", getName()};
        arrayTemp = ArrayUtils.addRow(arrayTemp, firstRow, 0);
        ExpressionMatrix exprMatrix = ExpressionMatrix.arrayToExpressionMatrix(arrayTemp, false);
        this.setMatrix(exprMatrix);

    }

    /**
     * Read Tiling data in Experiment and save in TilingDataStreaming format
     * 
     * @param experiment containing the Tiling Data to convert
     * @throws Exception
     */
    public static void convert(Experiment experiment) throws Exception {
        for (Tiling tiling : experiment.getTilings()) {
            if (!tiling.getName().contains(EGDeWTdata.NAME_Mean)
                    && !tiling.getName().contains(EGDeWTdata.NAME_Deviation)) {
                try {
                    tiling.loadMatrix();
                    tiling.setLength(Database.getInstance().getProbesTiling().size());
                    tiling.setValues(new double[tiling.getLength()]);
                    tiling.setRead(new boolean[tiling.getLength()]);
                    tiling.convertData(tiling.getMatrix());
                    tiling.save();
                } catch (Exception e) {

                }
            }
        }
        System.err.println("All TilingData have been converted");
    }

    public void convertData(ExpressionMatrix exprMatrix) {
        // getValues
        probes = Database.getInstance().getProbesTiling();
        int i = 0;
        for (Integer probe : probes.keySet()) {
            if (exprMatrix.getRowNames().containsKey(String.valueOf(probe))) {
                this.getValues()[i] = exprMatrix.getValue(String.valueOf(probe), exprMatrix.getHeader(0));
            } else {
                this.getValues()[i] = ExpressionMatrix.MISSING_VALUE;
            }
            this.getRead()[i] = true;
            i++;
        }
        // setStat
        setStat();
    }

    /**
     * Read WildType tiling data and extract the list of probes from it
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void createProbeList() throws FileNotFoundException, IOException {
        String fileName = PROBES_PATH;
        // create the list thanks to Egde Tiling Data
        String[][] arrayTemp = TabDelimitedTableReader.read(PATH_TILING_NORM + GENERAL_WT_NAME + ".+.gr");
        String[] firstRow = {"Probe", "index"};
        arrayTemp = ArrayUtils.addRow(arrayTemp, firstRow, 0);
        ExpressionMatrix exprMatrix = ExpressionMatrix.arrayToExpressionMatrix(arrayTemp, false);
        try {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            String[][] array = new String[exprMatrix.getRowNames().size()][1];
            int i = 0;
            for (String probe : exprMatrix.getRowNames().keySet()) {
                out.writeInt(Integer.valueOf(probe));
                array[i][0] = probe;
                // System.out.println(probe+" "+exprMatrix.getRowNames().get(probe));
                i++;
            }
            out.close();
            System.out.println("ProbeList saved");
            // TabDelimitedTableReader.save(array, "D:/text.txt");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Create Tiling data from the comparison of this one and data2
     * 
     * @param data2
     * @param calcData true if we want to calculate the data
     * @return
     * @throws IOException
     */
    public Tiling compare(Tiling data2, boolean calcData) {
        this.read();
        data2.read();
        Tiling compData = new Tiling(this.getName() + " vs " + data2.getName());
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
     * Calculate the mean expression of list of Tiling
     * 
     * @param datas
     * @param name
     * @return
     */
    public static Tiling getMean(ArrayList<Tiling> datas, String name) {
        ArrayList<String> headers = new ArrayList<String>();
        headers.add("Mean");
        ExpressionMatrix meanData = new ExpressionMatrix(headers);
        double[][] values = new double[datas.get(0).probes.size()][1];

        int i = 0;
        for (Integer probe : datas.get(0).probes.keySet()) {
            meanData.getRowNames().put(probe + "", i);
            double[] row = new double[datas.size()];
            for (int j = 0; j < row.length; j++) {
                row[j] = datas.get(j).get(probe);
            }
            values[i][0] = VectorUtils.mean(row);
            i++;
        }
        meanData.setValues(values);
        // meanData.saveTab("D:/"+name+"test.txt", "d");
        Tiling tiling = new Tiling(name);
        tiling.convertData(meanData);
        return tiling;
    }

    /**
     * Calculate the stat deviation on each probe of a list of Tiling
     * 
     * @param datas
     * @param name
     * @return
     */
    public static Tiling getDeviation(ArrayList<Tiling> datas, String name) {
        ArrayList<String> headers = new ArrayList<String>();
        headers.add("Deviation");
        ExpressionMatrix meanData = new ExpressionMatrix(headers);
        double[][] values = new double[datas.get(0).probes.size()][1];

        int i = 0;
        for (Integer probe : datas.get(0).probes.keySet()) {
            meanData.getRowNames().put(probe + "", i);
            double[] row = new double[datas.size()];
            for (int j = 0; j < row.length; j++) {
                row[j] = datas.get(j).get(probe);
            }
            values[i][0] = VectorUtils.deviation(row);
            i++;
        }
        meanData.setValues(values);
        // meanData.saveTab("D:/"+name+"test.txt", "d");
        Tiling tiling = new Tiling(name);
        tiling.convertData(meanData);
        return tiling;
    }

    /**
     * List all TilingData available by biocondName. Read all tiling data inside.
     * 
     * @param geneExp
     * @return
     */
    public static TreeMap<String, ArrayList<Tiling>> getTilingData(ArrayList<String> bioConditions) {
        TreeMap<String, ArrayList<Tiling>> bioConds = new TreeMap<String, ArrayList<Tiling>>();
        for (int i = 0; i < bioConditions.size(); i++) {
            String bioCondName = bioConditions.get(i);
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            for (Tiling tiling : bioCond.getTilings()) {
                tiling.read();
                tiling.setProbes(Database.getInstance().getProbesTiling());

                // add to list
                if (bioConds.containsKey(bioCondName)) {
                    bioConds.get(bioCondName).add(tiling);
                } else {
                    ArrayList<Tiling> datas = new ArrayList<Tiling>();
                    datas.add(tiling);
                    bioConds.put(bioCondName, datas);
                }
            }
        }
        return bioConds;
    }

    /**
     * Load all Tiling present in leftBCs and rightBCs.
     * 
     * @param geneExp
     * @return
     */
    public static TreeMap<String, ArrayList<Tiling>> getTilingData(ArrayList<String> leftBCs,
            ArrayList<String> rightBCs) {
        TreeMap<String, ArrayList<Tiling>> bioConds = new TreeMap<String, ArrayList<Tiling>>();
        for (int i = 0; i < leftBCs.size(); i++) {
            String bioCondName = leftBCs.get(i);
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            for (Tiling tiling : bioCond.getTilings()) {
                tiling.read();
                tiling.setProbes(Database.getInstance().getProbesTiling());

                // add to list
                if (bioConds.containsKey(bioCondName)) {
                    bioConds.get(bioCondName).add(tiling);
                } else {
                    ArrayList<Tiling> datas = new ArrayList<Tiling>();
                    datas.add(tiling);
                    bioConds.put(bioCondName, datas);
                }
            }
            bioCondName = rightBCs.get(i);
            bioCond = BioCondition.getBioCondition(bioCondName);
            for (Tiling tiling : bioCond.getTilings()) {
                tiling.read();
                tiling.setProbes(Database.getInstance().getProbesTiling());

                // add to list
                if (bioConds.containsKey(bioCondName)) {
                    bioConds.get(bioCondName).add(tiling);
                } else {
                    ArrayList<Tiling> datas = new ArrayList<Tiling>();
                    datas.add(tiling);
                    bioConds.put(bioCondName, datas);
                }
            }
        }
        return bioConds;
    }

    public TreeMap<Integer, Integer> getProbes() {
        return probes;
    }

    public void setProbes(TreeMap<Integer, Integer> probes) {
        this.probes = probes;
    }

}
