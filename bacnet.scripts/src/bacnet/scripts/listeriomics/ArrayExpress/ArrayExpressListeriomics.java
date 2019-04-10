package bacnet.scripts.listeriomics.ArrayExpress;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.listeriomics.technology.CUFSL;
import bacnet.utils.ArrayUtils;

/**
 * Different methods to curate and parse the different data for Listeriomics
 * website <br>
 * <br>
 * 
 * ONLY METHODS BEFORE FINAL DATA PROCESSING ARE PRESENT HERE
 * 
 * 
 * @author UIBC
 *
 */
public class ArrayExpressListeriomics {

	public static String DATA_TABLE = Database.getInstance().getPath() + "ArrayExpress-Listeria mono-15102014.txt";
	public static String DATA_CURATED_PATH = Database.getInstance().getPath() + "/Listeriomics/ArrayExpressCurated/";

	public static void run() {
		/*
		 * Download data
		 */
//		createHTMLPageSummary();
//		ArrayExpressListeriaDataImport.run();
		ArrayExpressTechnology.run();

		// searchStudyMissing();
		createMatrices();

	}

	/**
	 * Search in ArrayExpress list of data which BioCondition is not being created
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private static void searchStudyMissing() {
		String[][] arrayData = TabDelimitedTableReader.read(ArrayExpressListeriomics.DATA_TABLE);
		String[][] bioConds = TabDelimitedTableReader.read(Database.getInstance().getExperimentComparisonTablePath());
		for (int k = 1; k < arrayData.length; k++) {
			String arrayExpressIDData = arrayData[k][1];
			String studyName = arrayData[k][2];
			boolean found = false;
			for (int i = 1; i < bioConds.length; i++) {
				String arrayExpressIdBioCond = bioConds[i][ArrayUtils.findColumn(bioConds, "ArrayExpressId")];
				if (arrayExpressIDData.equals(arrayExpressIdBioCond)) {
					found = true;
				}
			}
			if (!found) {
				System.out.println("Did not found: " + arrayExpressIDData + "  " + studyName);
			}
		}
	}

	/**
	 * Some data has to be curated specially<br>
	 * We regroupe in this method the many function needed to create the right
	 * data.<br>
	 * MANY OF THESE FUNCTIONS HAVE TO BE RAN ONLY ONE TIME
	 */
	private static void createMatrices() {

//		Agilent.extractProbes("A-GEOD-14631", "E-GEOD-32434","Comment[LMO]");
//		Agilent.calculateComparison("A-GEOD-14631", "E-GEOD-32434",true);
//		Agilent.extractProbesComparison("A-GEOD-14687", "E-GEOD-32913");
//		Agilent.extractProbes("A-GEOD-13262", "E-GEOD-27936","Comment[GeneName]");
//		Agilent.calculateComparison("A-GEOD-13262", "E-GEOD-27936",false);
//		Agilent.normalize("A-GEOD-16415", "E-GEOD-43052","Comment[ORF]");
//		Agilent.extractProbes("A-GEOD-16415", "E-GEOD-43052","Comment[ORF]");
//		Agilent.calculateComparison("A-GEOD-16415", "E-GEOD-43052",true);

//		CUFSL.extractProbesComparison("A-GEOD-4604", "E-GEOD-6421",true);
//		CUFSL.extractProbesComparison("A-GEOD-5029", "E-GEOD-11347",false);
		CUFSL.extractProbesComparison("A-GEOD-5029", "E-GEOD-22672", false);

//		JCVI.extractProbes("A-GEOD-8830", "E-GEOD-27521");
//		JCVI.extractProbes("A-GEOD-5854", "E-GEOD-20274");
//		JCVI.extractProbes("A-GEOD-5854", "E-GEOD-40598");
//		JCVI.extractProbes("A-GEOD-5856", "E-GEOD-25195");
//		JCVI.createMatrix("A-JCVI-21", "E-MEXP-2279");  // E-MEXP-2279 was first manually curated
//		JCVI.extractProbes("A-GEOD-9904", "E-GEOD-19570"); // Did not include all the comparisons available in this experiments, for clarity reasons
//		JCVI.extractProbes("A-GEOD-4281","E-GEOD-49056");
//		JCVI.extractProbes("A-GEOD-4281","E-GEOD-42730");
//		JCVI.extractProbes("A-GEOD-4281","E-GEOD-21427");

//		Febit.curateTechnoTable("A-GEOD-17037");
//		Febit.extractProbes("A-GEOD-17037","E-GEOD-46182");
//		Febit.calculateComparison("A-GEOD-17037","E-GEOD-46182");

//		IHM.extractProbes("A-GEOD-7062", "E-GEOD-12146");
//		IHM.extractProbes("A-GEOD-7062-modify", "E-GEOD-12151");

//		UGiessen.processAllData();
//		UGiessen.combineMatrices("A-MEXP-752", "E-MEXP-1118");
//		UGiessen.combineMatrices("A-MEXP-752", "E-MEXP-1144");
//		UGiessen.combineMatrices("A-MEXP-752", "E-MEXP-1162");
//		UGiessen.combineMatrices("A-MEXP-752", "E-MEXP-1170");
//		UGiessen.combineMatrices("A-MEXP-752", "E-TABM-663");
//		UGiessen.curateRawData("A-MEXP-1460", "E-MEXP-1947");
//		UGiessen.extractProbes("A-MEXP-1460", "E-MEXP-1947");
//		UGiessen.calculateComparison("A-MEXP-1460", "E-MEXP-1947",true);

//		Tasmania.extractProbes("A-GEOD-5334", "E-GEOD-19918");
//		Tasmania.extractProbes("A-GEOD-5334", "E-GEOD-18796");
//		Tasmania.extractProbes("A-GEOD-5334", "E-GEOD-46612");

//		Wuerzburg.extractProbes("A-GEOD-6554", "E-GEOD-12143");
//		Wuerzburg.extractProbes("A-GEOD-6554", "E-GEOD-12145");
//		Wuerzburg.extractProbes("A-GEOD-6554", "E-GEOD-18363");

//		WUSTL.extractProbes("A-GEOD-13395", "E-GEOD-34083");
//		WUSTL.extractProbes("A-GEOD-13395", "E-GEOD-28507");

//		GeneralArray.extractProbes("A-GEOD-17774", "E-GEOD-52325");
//		GeneralArray.extractProbes("A-GEOD-17774", "E-GEOD-51193");

//		GeneralArray.extractProbes("A-GEOD-6489", "E-GEOD-22819");
//		GeneralArray.extractProbes("A-GEOD-6489", "E-GEOD-32172");
//		GeneralArray.extractProbes("A-GEOD-6489", "E-GEOD-41891");
//		GeneralArray.extractProbes("A-GEOD-6846", "E-GEOD-19014");
//		GeneralArray.extractProbes("A-GEOD-6846", "E-GEOD-11459");
//		GeneralArray.extractProbes("A-GEOD-10909", "E-GEOD-24107");
//		GeneralArray.extractProbes("A-GEOD-7235", "E-GEOD-12634");

//		Macroarray.normalize("A-GEOD-7248", "E-GEOD-13057");
//		Macroarray.extractProbes("A-GEOD-7248", "E-GEOD-13057");
//		Macroarray.calculateComparison("A-GEOD-7248", "E-GEOD-13057");

//		NimbleGen.normalize("A-GEOD-8610", "E-GEOD-16336");
//		NimbleGen.extractProbes("A-GEOD-8610", "E-GEOD-16336");
//		NimbleGen.calculateComparison("A-GEOD-8610", "E-GEOD-16336");

//		LT.extractProbes("A-GEOD-11577", "E-GEOD-26690");
//		LT.calculateComparison("A-GEOD-11577", "E-GEOD-26690");

		// E-GEOD-16887 was manually curated
		// E-MTAB-116 and E-MTAB-118 cannot be processed, cause no normalized data are
		// available
		// E-GEOD-20367 was not used has it is a genomic Tiling array

	}

	/**
	 * Read a potential ExpressionMatrix, remove all duplicate rows by calculating
	 * mean when 2 or more rows have same name. WARNING: No annotation information
	 * is allowed, only real values
	 * 
	 * @param dataPath
	 * @param comparison true if it is an ExpressionMatrix for a comparison, if no
	 *                   LOGFC column are found in a row, it will delete the row
	 *                   (gain speed and space)
	 * @return
	 */
	public static ExpressionMatrix curateMatrix(String dataPath, boolean comparison) {
		String[][] array = TabDelimitedTableReader.read(dataPath);
		ExpressionMatrix matrix = new ExpressionMatrix();
		/*
		 * Create list of RowName, values, headers, et initialize matrix
		 */
		TreeSet<String> rowNamesSet = new TreeSet<>();
		for (int i = 1; i < array.length; i++) {
			rowNamesSet.add(array[i][0]);
		}
		TreeMap<String, Integer> rowNames = new TreeMap<>();
		int k = 0;
		for (String rowName : rowNamesSet) {
			rowNames.put(rowName, k);
			k++;
		}
		matrix.setRowNames(rowNames);
		double[][] values = new double[rowNames.size()][array[0].length - 1];
		matrix.setValues(values);
		ArrayList<String> headers = new ArrayList<>();
		for (int j = 1; j < array[0].length; j++) {
			headers.add(array[0][j]);
		}
		matrix.setHeaders(headers);

		/*
		 * go through array and remove duplicates by calculating mean of each column for
		 * every duplicate rowname
		 */
		ArrayList<String> keepRowName = new ArrayList<>();
		for (String rowName : matrix.getRowNames().keySet()) {
			for (int j = 1; j < array[0].length; j++) {
				String header = array[0][j];
				double sum = 0;
				double count = 0;
				for (int i = 1; i < array.length; i++) {
					if (array[i][0].equals(rowName)) {
						if (!array[i][j].equals("") && !array[i][j].equals("null")) { // Beware of Missing values
							sum += Double.parseDouble(array[i][j]);
							count++;
						}
					}
				}
				sum = sum / count;
				if (count == 0)
					sum = OmicsData.MISSING_VALUE;
				matrix.setValue(sum, rowName, header);
				if (comparison && header.contains("LOGFC")) { // if we have no results in LOGFC column we remove the row
					if (count != 0) {
						keepRowName.add(rowName);
					}
				} else {
					keepRowName.add(rowName);
				}
			}
		}
		matrix = matrix.getSubMatrixRow(keepRowName);
		return matrix;
	}

	/**
	 * DATA_TABLe has been created from a copy paste of ArrayExpress search results.
	 * From the web^page we create the table, curated it and convert it to HTML Read
	 * DATA_TABLE and create an HTML table from it to ease accession to ArrayExpress
	 * info
	 */
	public static void createHTMLPageSummary() {
		String[][] array = TabDelimitedTableReader.read(DATA_TABLE);
		for (int i = 1; i < array.length; i++) {
			for (int j = 0; j < array[0].length - 2; j++) {
				String cell = array[i][j];
				if (cell.contains("http")) {
					cell = "<a href=\"" + cell + "\">" + cell + "</a>";
				}
				array[i][j] = cell;
			}

			array[i][1] = "<a href=\"" + array[i][array[0].length - 1] + "\">" + array[i][1] + "</a>";
		}
		array = ArrayUtils.deleteColumn(array, array[0].length - 1);
		array = ArrayUtils.deleteColumn(array, array[0].length - 1);
		TabDelimitedTableReader.saveInHTML(array, Database.getInstance().getPath() + "ListTranscriptomes.html",
				"Listeria Transcriptomics data");
	}

}
