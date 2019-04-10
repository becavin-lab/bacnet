package bacnet.scripts.listeriomics.technology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.arrayexpress.ArrayExpress;
import bacnet.scripts.arrayexpress.ArrayExpressDataUtils;
import bacnet.scripts.arrayexpress.ArrayExpressTechnology;
import bacnet.scripts.core.normalization.MedianNormalization;
import bacnet.utils.ArrayUtils;
import bacnet.utils.ExpressionMatrixStat;
import bacnet.utils.MathUtils;
import bacnet.utils.VectorUtils;

public class NimbleGen {

	public static void extractProbes(String technoID, String arrayExpressId) {

		ArrayList<String> bioCondNames = new ArrayList<>();
		for (String bioCond : BioCondition.getAllBioConditionNames()) {
			if (bioCond.contains("NimbleGen_2010"))
				bioCondNames.add(bioCond); // for E-GEOD-16336
		}

		HashMap<String, String> probeToGene = new HashMap<>();
		String[][] technoIDArray = TabDelimitedTableReader
				.read(ArrayExpressTechnology.PATH + technoID + ".adfTable.txt");
		for (int i = 1; i < technoIDArray.length; i++) {
			probeToGene.put(technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Reporter Name")],
					technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Comment[ORF]")]);
		}

		for (String bioCondName : bioCondNames) {
			System.out.println(bioCondName);
			BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
			ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
			int k = 0;
			ExpressionMatrix matrixFinal = new ExpressionMatrix();

			for (ExpressionMatrix matrixTemp : bioCond.getMatrices()) {
				for (String rawDataName : matrixTemp.getRawDatas()) {
					System.out.println(rawDataName);
					matrixTemp = ExpressionMatrix
							.loadTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + rawDataName, true);
					matrixTemp = MedianNormalization.norm(matrixTemp);
					matrixTemp.getHeaders().clear();
					// We add only one header, so only the first column will be read
					matrixTemp.getHeaders().add("Value_" + k);
					k++;
					// matrixTemp.saveTab(file.getAbsolutePath(), "Probe");
					matrices.add(matrixTemp);
					// matrixTemp.saveTab(ArrayExpressListeriomics.DATA_CURATED_PATH+comparison+"_"+i+".txt",
					// "Probe");
				}
				matrixFinal = ExpressionMatrix.merge(matrices, true);

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
				matrixFinal.addColumn("VALUE", columnFC);
				matrixFinal.addColumn("STDEV", columnSTDev);
				matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", "Probe");

			}

			/*
			 * Replace now the probeId by the GeneID
			 */
			String[][] arrayData = TabDelimitedTableReader
					.read(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt");
			for (int i = 1; i < arrayData.length; i++) {
				String probe = arrayData[i][0];
				if (probeToGene.containsKey(probe)) {
					arrayData[i][0] = probeToGene.get(probe);
				} else {
					System.out.println("could not find probe: " + probe);
				}
			}
			TabDelimitedTableReader.save(arrayData, ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt");

			/*
			 * Remove duplicate probes
			 */
			ExpressionMatrix matrixBioCond = ArrayExpress
					.curateMatrix(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", false);
			matrixBioCond.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", "Probe");
		}
	}

	/**
	 * Data are not Log transformed:<br>
	 * <li>add the min value to be sure to have only positive values
	 * <li>log2 transform
	 * 
	 * @param technoID
	 * @param arrayExpressId
	 * @param geneColumnName
	 */
	public static void normalize(String technoID, String arrayExpressId) {

		ArrayList<String> bioCondNames = new ArrayList<>();
		for (String bioCond : BioCondition.getAllBioConditionNames()) {
			if (bioCond.contains("NimbleGen_2010"))
				bioCondNames.add(bioCond); // for E-GEOD-16336
		}

		for (String bioCondName : bioCondNames) {
			System.out.println(bioCondName);
			BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
			for (ExpressionMatrix matrixTemp : bioCond.getMatrices()) {
				for (String rawDataNameTemp : matrixTemp.getRawDatas()) {
					String rawDataName = rawDataNameTemp.replace("_curated", "");
					System.out.println(rawDataName);
					matrixTemp = ExpressionMatrix
							.loadTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + rawDataName, true);
					double min = ExpressionMatrixStat.min(matrixTemp, 0);
					for (String rowName : matrixTemp.getRowNames().keySet()) {
						double value = matrixTemp.getValue(rowName, "VALUE");
						value = value - min + 1; // avoid negative numbers (due to background soustraction)
						value = MathUtils.log2(value);
						matrixTemp.setValue(value, rowName, "VALUE");
					}
					matrixTemp.saveTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + rawDataNameTemp,
							"Probe");

				}
			}
		}
	}

	/**
	 * Calculate Log of Fold Change (mean difference) for each value, and p-value
	 * 
	 * @param technoID
	 * @param arrayExpressId
	 */
	public static void calculateComparison(String technoID, String arrayExpressId) {
		/*
		 * Create comparison Matrix
		 */
		ArrayList<String> bioCondNames = new ArrayList<>();
		for (String bioCond : BioCondition.getAllBioConditionNames()) {
			if (bioCond.contains("NimbleGen_2010"))
				bioCondNames.add(bioCond); // for E-GEOD-13057
		}

		for (String bioCondName : bioCondNames) {
			BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
			if (bioCond.getComparisons().size() != 0) {
				BioCondition refBioCond = BioCondition.getBioCondition(bioCond.getComparisons().get(0));
				ExpressionMatrix expr1 = ExpressionMatrix
						.loadTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", false);
				ExpressionMatrix expr2 = ExpressionMatrix
						.loadTab(ArrayExpress.DATA_CURATED_PATH + refBioCond.getName() + ".txt", false);
				ArrayList<String> includColumn = new ArrayList<>();
				includColumn.add("VALUE");
				ExpressionMatrix expr1Minus = expr1.getSubMatrixColumn(includColumn);
				ExpressionMatrix expr2Minus = expr2.getSubMatrixColumn(includColumn);
				ExpressionMatrix exprFoldChange = ExpressionMatrixStat.minus(expr1Minus, expr2Minus);
				exprFoldChange.getHeaders().clear();
				exprFoldChange.getHeaders().add("LOGFC");

				/*
				 * Save ExpressionMatrix
				 */
				exprFoldChange.saveTab(
						ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + " vs " + refBioCond.getName() + ".txt",
						"Probes");
			}
		}
	}

}
