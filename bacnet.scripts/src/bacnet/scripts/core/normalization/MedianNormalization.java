package bacnet.scripts.core.normalization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.ExpressionMatrixStat;
import bacnet.utils.VectorUtils;

public class MedianNormalization {

	/**
	 * Load all TilingData in this Experiment and normalize by the median, and save
	 * in the Tiling/MedianNorm path
	 * 
	 * @param exp
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void normTiling(Experiment exp) throws FileNotFoundException, IOException {
		ArrayList<Tiling> tilings = exp.getTilings();
		// load all tiling Data (multi-thread operation)
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (Tiling tiling : tilings) {
			final Tiling tempTiling = tiling;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						tempTiling.loadMatrix();
						ExpressionMatrix tilingMatrix = tempTiling.getMatrix();
						if (tempTiling.getName().contains("_NoNorm")) {
							tilingMatrix = ExpressionMatrixStat.log2(tilingMatrix);
						}
						ExpressionMatrix normTiling = MedianNormalization.norm(tilingMatrix);
						// ExpressionMatrix normTiling = tilingMatrix;
						String[][] array = normTiling.toArray("probes");
						array = ArrayUtils.deleteRow(array, 0);
						TabDelimitedTableReader.save(array, OmicsData.PATH_TILING_NORM + tempTiling.getName());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			};
			executor.execute(runnable);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception");
		}
		System.err.println("All TilingData have been Median Normalized");
	}

	/**
	 * For each column of the ExpressionMatrix, delete the median. So final median
	 * of each column is equal to zero
	 * 
	 * @param matrix
	 * @return
	 */
	public static ExpressionMatrix norm(ExpressionMatrix matrix) {
		for (int j = 0; j < matrix.getNumberColumn(); j++) {
			// calculate median
			double median = VectorUtils.median(VectorUtils.deleteMissingValue(matrix.getColumn(j)));
			// delete median to each value
			for (int i = 0; i < matrix.getNumberRow(); i++) {
				if (matrix.getValue(i, j) != OmicsData.MISSING_VALUE) {
					matrix.setValue(matrix.getValue(i, j) - median, i, j);
				}
			}
			// verify results
			median = VectorUtils.median(matrix.getColumn(j));
			if (median != 0)
				System.err.println("median is equal to " + median);
		}
		return matrix;
	}

	/**
	 * Normalize by median the column with a specific name
	 * 
	 * @param matrix
	 * @param columnHeader
	 * @return
	 */
	public static ExpressionMatrix norm(ExpressionMatrix matrix, String columnHeader) {
		// calculate median
		double median = VectorUtils
				.median(VectorUtils.deleteMissingValue(matrix.getColumn(matrix.getHeaders().indexOf(columnHeader))));
		// delete median to each value
		for (int i = 0; i < matrix.getNumberRow(); i++) {
			if (matrix.getValue(i, matrix.getHeaders().indexOf(columnHeader)) != OmicsData.MISSING_VALUE) {
				matrix.setValue(matrix.getValue(i, matrix.getHeaders().indexOf(columnHeader)) - median, i,
						matrix.getHeaders().indexOf(columnHeader));
			}
		}
		// verify results
		median = VectorUtils.median(matrix.getColumn(matrix.getHeaders().indexOf(columnHeader)));
		if (median != 0)
			System.err.println("median is equal to " + median);
		return matrix;
	}

	public static void normGE(Experiment exp) throws FileNotFoundException, IOException {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (GeneExpression geneExpTemp : exp.getGeneExprs()) {
			final GeneExpression geneExp = geneExpTemp;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						// load exprMatrix
						ExpressionMatrix exprMatrix = ExpressionMatrix
								.loadTab(OmicsData.PATH_GENEXPR_NORM + geneExp.getName(), false);
						// norm median expression
						exprMatrix = MedianNormalization.normGE(exprMatrix);
						exprMatrix.saveTab(OmicsData.PATH_GENEXPR_NORM + geneExp.getName(), "probes");
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			};
			executor.execute(runnable);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception");
		}
		System.err.println("All GeneExpression data have been normalized");
	}

	/**
	 * From a GE ExpressionMatrix containing 5 columns (3replicates exp + median +
	 * stdev) Apply median normalization to the first 3 columns, and recalculate
	 * median and stdev
	 * 
	 * @param matrix
	 * @return
	 */
	public static ExpressionMatrix normGE(ExpressionMatrix matrix) {
		ExpressionMatrix matrixNew = matrix.clone();
		// first we normalize the 3 first column
		for (int j = 0; j < 3; j++) {
			// calculate median
			double median = VectorUtils.median(matrixNew.getColumn(j));
			// delete median to each value
			for (int i = 0; i < matrixNew.getNumberRow(); i++) {
				matrixNew.setValue(matrixNew.getValue(i, j) - median, i, j);
			}
			// verify results
			median = VectorUtils.median(matrixNew.getColumn(j));
			if (median != 0)
				System.err.println("median is equal to " + median);
		}

		// then we recalculate median and stdev for every probe
		for (int i = 0; i < matrixNew.getNumberRow(); i++) {
			double[] rowValues = new double[3];
			for (int j = 0; j < 3; j++) {
				rowValues[j] = matrixNew.getValue(i, j);
			}
			matrixNew.setValue(VectorUtils.median(rowValues), i, 3);
			matrixNew.setValue(VectorUtils.deviation(rowValues), i, 4);
		}
		return matrixNew;
	}

}
