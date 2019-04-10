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
import bacnet.utils.VectorUtils;

/**
 * 
 * GeneExpression array data from Listeria tiling chip ArrayExpress id :
 * A-AFFY-140
 * 
 * Here double[] values vector has length = 5*probes.size because we put 5
 * columns column 0-2 = 3 replicatas column 3 = median expression column 4 = var
 * expression
 * 
 * @author UIBC
 *
 */
public class GeneExpression extends ExpressionData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -947282303133711974L;
	public static String PROBES_PATH = Database.getDATA_PATH() + "StreamingData" + File.separator
			+ "probeGExpression.data";
	public static String PROBES_PATH_2 = OmicsData.PATH_GENEXPR_NORM + File.separator + "probeGExpression.data";
	public static String EXTENSION = ".ge";
	public static int COL_NUMBER = 5;

	private transient boolean dataRead = false;

	private transient TreeMap<String, Integer> probes;

	public GeneExpression() {
		setType(TypeData.GeneExpr);
	}

	public GeneExpression(String name) {
		super(name);
		setType(TypeData.GeneExpr);
		probes = Database.getInstance().getProbesGExpression();
		setLength(probes.size() * COL_NUMBER);
		setValues(new double[getLength()]);
		setRead(new boolean[getLength()]);
	}

	@Override
	public void load() {
		super.load();
		probes = Database.getInstance().getProbesGExpression();
	}

	/**
	 * Get the median value of corresponding probe in double[][] values
	 * 
	 * @param probe
	 * @return
	 */
	public double getMedianValue(String probe) {
		if (!isDataRead()) {
			read();
		}
		int index = probes.get(probe);
		return this.getValues()[index * COL_NUMBER + 3];
	}

	/**
	 * Set the median value of corresponding probe in double[][] values
	 * 
	 * @param probe
	 * @return
	 */
	public double setMedianValue(String probe, double value) {
		if (!isDataRead()) {
			read();
		}
		int index = probes.get(probe);
		return this.getValues()[index * COL_NUMBER + 3] = value;
	}

	/**
	 * Extract the column containing the median<br>
	 * 
	 * @return a vector of all median
	 */
	public double[] getMedianVector() {
		if (!isDataRead()) {
			read();
		}
		double[] valueLogFC = new double[this.getLength()];
		int i = 0;
		for (String probe : Database.getInstance().getProbesGExpression().keySet()) {
			valueLogFC[i] = this.getMedianValue(probe);
			i++;
		}
		return valueLogFC;
	}

	/**
	 * Get all technical replicates values of corresponding probe in double[][]
	 * values
	 * 
	 * @param probe
	 * @return
	 */
	public double[] getTriplicateValue(String probe) {
		if (!isDataRead()) {
			read();
		}
		int index = probes.get(probe);
		double[] values = new double[3];
		values[0] = this.getValues()[index * COL_NUMBER];
		values[1] = this.getValues()[index * COL_NUMBER + 1];
		values[2] = this.getValues()[index * COL_NUMBER + 2];
		return values;
	}

	/**
	 * Get variance value of corresponding probe in double[][] values
	 * 
	 * @param probe
	 * @return
	 */
	public double getVarianceValue(String probe) {
		if (!isDataRead()) {
			read();
		}
		int index = probes.get(probe);
		return this.getValues()[index * COL_NUMBER + 4];
	}

	/**
	 * Check if TreeMap<String, Integer> probes contains a probe
	 * 
	 * @param probe
	 * @return
	 */
	public boolean containProbe(String probe) {
		if (probes.containsKey(probe))
			return true;
		else
			return false;
	}

	/**
	 * Load ExpressionMatrix found in TranscriptomeData.PATH_GENEXPR_NORM+getName()
	 */
	public void loadMatrix() {
		System.out.println("Read: " + ExpressionData.PATH_GENEXPR_NORM + getName());
		ExpressionMatrix exprMatrix = ExpressionMatrix.loadTab(ExpressionData.PATH_GENEXPR_NORM + getName(), true);
		this.setMatrix(exprMatrix);
	}

	/**
	 * Transform a <code>GeneExpression</code> into an <code>ExpressionMatrix</code>
	 * 
	 * @return representation of the <code>GFeneExpression</code>
	 */
	public ExpressionMatrix toExpressionMatrix() {
		this.read();
		ArrayList<String> headers = new ArrayList<>();
		String[] headersVector = { "Value_1", "Value_2", "Value_3", "LOGFC", "Var" };
		for (String header : headersVector)
			headers.add(header);
		ExpressionMatrix matrix = new ExpressionMatrix(headers);
		matrix.setName(getName());
		double[][] values = new double[Database.getInstance().getProbesGExpression().size()][headers.size()];
		for (String probe : Database.getInstance().getProbesGExpression().keySet()) {
			matrix.getRowNames().put(probe, Database.getInstance().getProbesGExpression().get(probe));
			values[Database.getInstance().getProbesGExpression().get(probe)][0] = getTriplicateValue(probe)[0];
			values[Database.getInstance().getProbesGExpression().get(probe)][1] = getTriplicateValue(probe)[1];
			values[Database.getInstance().getProbesGExpression().get(probe)][2] = getTriplicateValue(probe)[2];
			values[Database.getInstance().getProbesGExpression().get(probe)][3] = getMedianValue(probe);
			values[Database.getInstance().getProbesGExpression().get(probe)][4] = getVarianceValue(probe);
		}
		matrix.setValues(values);
		return matrix;
	}

	/**
	 * Read all GeneExpr data and save in GeneExpressionStreaming format
	 * 
	 * @throws Exception
	 */
	public static void convert(Experiment experiment) throws Exception {
		for (GeneExpression geneExp : experiment.getGeneExprs()) {
			if (!geneExp.getName().contains(EGDeWTdata.NAME_Mean)
					&& !geneExp.getName().contains(EGDeWTdata.NAME_Deviation)) {
				geneExp.loadMatrix();
				geneExp.setLength(Database.getInstance().getProbesGExpression().size() * COL_NUMBER);
				geneExp.setValues(new double[geneExp.getLength()]);
				geneExp.setRead(new boolean[geneExp.getLength()]);
				geneExp.convertData(geneExp.getMatrix());
				geneExp.save();
			}
		}
		System.err.println("All GeneExprData have been converted");
	}

	/**
	 * Convert data from an ExpressionMatrix to a GeneExpression
	 * 
	 * @param exprMatrix
	 */
	public void convertData(ExpressionMatrix exprMatrix) {
		// getValues
		probes = Database.getInstance().getProbesGExpression();
		// size difference should be equal to 1, because we have 2 equal probe
		// lmo2395_at and lmo2395_x_at
		System.err.println(exprMatrix.getNumberRow() + "  -  " + probes.size() + " = "
				+ (exprMatrix.getNumberRow() - probes.size()));
		int i = 0;
		int k = 0;
		double[] median = new double[probes.size()];
		for (String probe : probes.keySet()) {
			for (int j = 0; j < 5; j++) {
				// System.out.println(probe);
				median[k] = exprMatrix.getValue(probe, exprMatrix.getHeader(3));
				this.getValues()[i] = exprMatrix.getValue(probe, exprMatrix.getHeader(j)); // get column 0-5 in the
																							// exprMatrix
				this.getRead()[i] = true;
				i++;
			}
			k++;
		}

		// getStat
		this.setMax(VectorUtils.max(median));
		this.setMin(VectorUtils.min(median));
		this.setMean(VectorUtils.mean(median));
		this.setMedian(VectorUtils.median(median));
		this.setVariance(VectorUtils.variance(median));
	}

	public boolean isDataRead() {
		return dataRead;
	}

	public void setDataRead(boolean dataRead) {
		this.dataRead = dataRead;
	}

	/**
	 * Read general GeneExpression data and extract the list of probes from it
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void createProbeList() throws FileNotFoundException, IOException {
		String fileName = PROBES_PATH;
		// create the list thanks to Egde Tiling Data
		System.out.println(PATH_GENEXPR_NORM + GENERAL_WT_NAME + EXTENSION);
		ExpressionMatrix matrix = ExpressionMatrix.loadTab(PATH_GENEXPR_NORM + GENERAL_WT_NAME + EXTENSION, false);
		// ExpressionMatrix matrix = new ExpressionMatrix();
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			String[][] array = new String[matrix.getRowNames().size()][1];
			int i = 0;
			for (String probe : matrix.getRowNames().keySet()) {
				out.writeUTF(probe);
				array[i][0] = probe;
				System.out.println(probe + "  " + matrix.getRowNames().get(probe));
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
	 * Create GeneExpression data from the comparison of this one and data2
	 * 
	 * @param data2
	 * @param calcData true if we want to calculate the data
	 * @return
	 * @throws IOException
	 */
	public GeneExpression compare(GeneExpression data2, boolean calcData) {
		/*
		 * Create the New omics data
		 */
		this.read();
		data2.read();
		GeneExpression compData = new GeneExpression(this.getName() + " vs " + data2.getName());
		compData.setBioCondName(this.getBioCondName() + " vs " + data2.getBioCondName());
		compData.setLength(this.getLength());

		/*
		 * If calcData is TRUE we will calculate the relative expression OmicsData
		 */
		if (calcData) {
			double[] values = new double[this.getLength()];
			boolean[] read = new boolean[this.getLength()];
			for (int j = 0; j < values.length; j++) {
				values[j] = 0;
				read[j] = true;
			}
			compData.setValues(values);
			compData.setRead(read);

			// fill the data by creating a fake ExpressionMatrix
			ArrayList<String> headers = new ArrayList<String>();
			String[] headersArray = { "value1", "value2", "value3", "median", "var" };
			for (String header : headersArray)
				headers.add(header);
			ExpressionMatrix matrix = new ExpressionMatrix(headers);
			probes = Database.getInstance().getProbesGExpression();
			matrix.setRowNames(probes);
			matrix.setValues(new double[probes.size()][headers.size()]);
			for (String rowName : probes.keySet()) {
				double[] value = this.getTriplicateValue(rowName);
				double[] value2 = data2.getTriplicateValue(rowName);
				double[] diff = { value[0] - value2[0], value[1] - value2[1], value[2] - value2[2] };
				matrix.setValue(diff[0], rowName, "value1");
				matrix.setValue(diff[1], rowName, "value2");
				matrix.setValue(diff[2], rowName, "value3");
				matrix.setValue(this.getMedianValue(rowName) - data2.getMedianValue(rowName), rowName, "median");
				matrix.setValue(this.getVarianceValue(rowName) - data2.getVarianceValue(rowName), rowName, "var");
			}
			compData.convertData(matrix);
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
	public static GeneExpression getMean(ArrayList<GeneExpression> datas, String name) {
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("Mean1");
		headers.add("Mean2");
		headers.add("Mean3");
		headers.add("Mean");
		headers.add("Deviation");
		ExpressionMatrix meanData = new ExpressionMatrix(headers);
		double[][] values = new double[datas.get(0).probes.size()][COL_NUMBER];

		int i = 0;
		for (String probe : datas.get(0).probes.keySet()) {
			meanData.getRowNames().put(probe, i);
			double[] row = new double[datas.size()];
			for (int j = 0; j < row.length; j++) {
				row[j] = datas.get(j).getMedianValue(probe);
			}
			values[i][0] = VectorUtils.mean(row);
			values[i][1] = VectorUtils.mean(row);
			values[i][2] = VectorUtils.mean(row);
			values[i][3] = VectorUtils.mean(row);
			values[i][4] = VectorUtils.variance(row);
			i++;
		}
		meanData.setValues(values);
		// meanData.saveTab("D:/"+name+"test.txt", "d");
		GeneExpression geneExpr = new GeneExpression(name);
		geneExpr.convertData(meanData);
		return geneExpr;
	}

	/**
	 * List all TilingData available by biocondName. Initiate all
	 * TilingDataStreaming.
	 * 
	 * @param geneExp
	 * @return
	 */
	public static TreeMap<String, GeneExpression> getGExpressionData(ArrayList<String> bioConditions) {
		TreeMap<String, GeneExpression> bioConds = new TreeMap<String, GeneExpression>();
		for (int i = 0; i < bioConditions.size(); i++) {
			String bioCondName = bioConditions.get(i);
			BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
			for (GeneExpression geneExpr : bioCond.getGeneExprs()) {
				geneExpr.read();
				geneExpr.setProbes(Database.getInstance().getProbesGExpression());
				// add to list
				bioConds.put(bioCondName, geneExpr);
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
	public static TreeMap<String, GeneExpression> getGExpressionData(ArrayList<String> leftBCs,
			ArrayList<String> rightBCs) {
		TreeMap<String, GeneExpression> bioConds = new TreeMap<String, GeneExpression>();
		for (int i = 0; i < leftBCs.size(); i++) {
			String bioCondName = leftBCs.get(i);
			BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
			for (GeneExpression geneExpr : bioCond.getGeneExprs()) {
				geneExpr.read();
				geneExpr.setProbes(Database.getInstance().getProbesGExpression());

				// add to list
				bioConds.put(bioCondName, geneExpr);
			}
			bioCondName = rightBCs.get(i);
			bioCond = BioCondition.getBioCondition(bioCondName);
			for (GeneExpression geneExpr : bioCond.getGeneExprs()) {
				geneExpr.read();
				geneExpr.setProbes(Database.getInstance().getProbesGExpression());

				// add to list
				bioConds.put(bioCondName, geneExpr);
			}
		}
		return bioConds;
	}

	public TreeMap<String, Integer> getProbes() {
		return probes;
	}

	public void setProbes(TreeMap<String, Integer> probes) {
		this.probes = probes;
	}

}
