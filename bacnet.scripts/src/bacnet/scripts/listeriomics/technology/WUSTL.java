package bacnet.scripts.listeriomics.technology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.arrayexpress.ArrayExpress;
import bacnet.scripts.arrayexpress.ArrayExpressDataUtils;
import bacnet.scripts.arrayexpress.ArrayExpressTechnology;
import bacnet.utils.ArrayUtils;
import bacnet.utils.VectorUtils;

public class WUSTL {

	public static void extractProbes(String technoID, String arrayExpressId) {
		/*
		 * Read Comparison and extract the data fileName of the corresponding data
		 */
		String[][] array = TabDelimitedTableReader.read(Database.getInstance().getExperimentComparisonTablePath());
		TreeSet<String> comparisons = new TreeSet<>();
		HashMap<String, ArrayList<String>> comparisonToFileName = new HashMap<>();

		/**
		 * Create an HashMap between probeID and GeneId
		 */
		HashMap<String, String> probeToGene = new HashMap<>();
		String[][] technoIDArray = TabDelimitedTableReader
				.read(ArrayExpressTechnology.PATH + technoID + ".adfTable.txt");
		for (int i = 1; i < technoIDArray.length; i++) {
			probeToGene.put(technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Reporter Name")],
					technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Comment[ORF]")]);
		}

		/**
		 * For each Comparison, look at the different corresponding data
		 */
		for (int i = 0; i < array.length; i++) {
			String comparison = array[i][ArrayUtils.findColumn(array, "BioCondName")] + " vs "
					+ array[i][ArrayUtils.findColumn(array, "RefBioCondName")];
			String fileName = array[i][ArrayUtils.findColumn(array, "FileName")];
			String arrayExpressIdTemp = array[i][ArrayUtils.findColumn(array, "ArrayExpressId")];
			if (arrayExpressIdTemp.equals(arrayExpressId)) {

				if (!comparisonToFileName.containsKey(comparison)) {
					ArrayList<String> fileNames = new ArrayList<>();
					fileNames.add(fileName);
					comparisonToFileName.put(comparison, fileNames);
				} else {
					comparisonToFileName.get(comparison).add(fileName);
				}
			}
		}

		/**
		 * Go through the HashMap Comparisons and combine data in one file for each
		 * comparison<br>
		 * Median of LogFC is calculated and will be used for display
		 */
		for (String comparison : comparisonToFileName.keySet()) {
			ArrayList<String> fileNames = comparisonToFileName.get(comparison);
			if (fileNames.size() == 1) {
				ExpressionMatrix matrix = ExpressionMatrix.loadTab(
						ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + fileNames.get(0), false);
				boolean pvalue = false;
				if (matrix.getHeaders().contains("p-value"))
					pvalue = true;
				matrix.getHeaders().clear();
				matrix.getHeaders().add("LOGFC");
				if (pvalue)
					matrix.getHeaders().add("p-value");
				matrix.saveTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", "Probe"); // apparently
																								// it goes
																								// here, add
																								// weird name
																								// and see
																								// what
																								// happen
			} else {
				/*
				 * Fusion all the dataFile
				 */
				ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
				int i = 0;
				for (String fileName : fileNames) {
					System.out.println(comparison + "   " + fileName);
					ExpressionMatrix matrixTemp = ExpressionMatrix
							.loadTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + fileName, false);
					matrixTemp.getHeaders().clear();
					// We add only one header, so only the first column will be read
					matrixTemp.getHeaders().add("Value_" + i);
					i++;
					// matrixTemp.saveTab(file.getAbsolutePath(), "Probe");
					matrices.add(matrixTemp);
					// matrixTemp.saveTab(ArrayExpressListeriomics.DATA_CURATED_PATH+comparison+"_"+i+".txt",
					// "Probe");
				}
				ExpressionMatrix matrixFinal = ExpressionMatrix.merge(matrices, true);
				/*
				 * Calculate median and standard deviation in the column LOGFC
				 */
				double[] columnFC = new double[matrixFinal.getNumberRow()];
				double[] columnSTDev = new double[matrixFinal.getNumberRow()];
				for (String rowName : matrixFinal.getRowNames().keySet()) {
					double[] row = matrixFinal.getRow(rowName);
					columnFC[matrixFinal.getRowNames().get(rowName)] = VectorUtils
							.median(VectorUtils.deleteMissingValue(row));
					columnSTDev[matrixFinal.getRowNames().get(rowName)] = VectorUtils
							.deviation(VectorUtils.deleteMissingValue(row));
				}
				matrixFinal.addColumn("LOGFC", columnFC);
				matrixFinal.addColumn("STDEV", columnSTDev);

				/*
				 * Save the file with the name: comparison.txt
				 */
				matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", "Probe");
			}

			/*
			 * Change rowNames
			 */
			String[][] arrayMatrix = TabDelimitedTableReader.read(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt");
			for (int i = 0; i < arrayMatrix.length; i++) {
				String rowName = arrayMatrix[i][0];
				if (rowName.contains("lmo")) {
					rowName = rowName.split(" ")[0];
				} else if ((rowName.contains("LMOf2365"))) {
					rowName = rowName.split("_")[0] + "_" + rowName.split("_")[1];
				}
				arrayMatrix[i][0] = rowName;
			}
			TabDelimitedTableReader.save(arrayMatrix, ArrayExpress.DATA_CURATED_PATH + comparison + ".txt");

			/*
			 * Curate data by removing duplicates
			 */
			ExpressionMatrix matrix = ArrayExpress.curateMatrix(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt",
					true);
			matrix.saveTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", "Gene");

			/*
			 * Remove rows with no Data
			 */
			ExpressionMatrix matrixFinal = ExpressionMatrix
					.loadTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", true);
			ArrayList<String> rowNames = new ArrayList<>();
			for (String rowName : matrixFinal.getRowNames().keySet()) {
				double value = matrixFinal.getValue(rowName, "LOGFC");
				if (value == OmicsData.MISSING_VALUE) {

				} else {
					rowNames.add(rowName);
					System.out.println("keep: '" + value + "'");
				}
			}
			matrixFinal = matrixFinal.getSubMatrixRow(rowNames);
			matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", "Probe");

			/*
			 * Filter EGDe and F2365 genes
			 */
			if (comparison.contains("EGDe")) {
				matrixFinal = filterEGDe(matrixFinal);
			} else if (comparison.contains("F2365")) {
				matrixFinal = filterF2365(matrixFinal);
			} else {
				System.err.println("No strain specified for this data: " + comparison);
			}
			matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", "Gene");
		}

	}

	/**
	 * Keep only the row containing EGD-e genes: "lmo" gene
	 * 
	 * @param matrix
	 * @return
	 */
	public static ExpressionMatrix filterEGDe(ExpressionMatrix matrix) {
		ArrayList<String> includeRowNames = new ArrayList<>();
		for (String rowName : matrix.getRowNamesToList()) {
			if (rowName.contains("lmo")) {
				includeRowNames.add(rowName);
			}
		}
		return matrix.getSubMatrixRow(includeRowNames);

	}

	/**
	 * Keep only the row containing F2365 genes: "LMOf2365" gene
	 * 
	 * @param matrix
	 * @return
	 */
	public static ExpressionMatrix filterF2365(ExpressionMatrix matrix) {
		ArrayList<String> includeRowNames = new ArrayList<>();
		for (String rowName : matrix.getRowNamesToList()) {
			if (rowName.contains("LMOf2365")) {
				includeRowNames.add(rowName);
			}
		}
		return matrix.getSubMatrixRow(includeRowNames);

	}

}
