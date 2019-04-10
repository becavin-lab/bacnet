package bacnet.scripts.core.stat;

import bacnet.utils.MathUtils;
import bacnet.utils.VectorUtils;

public class StatUtils {

	// public static void pieChart(ArrayList<ExpressionMatrix> matrices) throws
	// PartInitException{
	// ModelProvider.INSTANCE.setExpressionMatrixListToDisplay(matrices);
	// IWorkbenchPage page =
	// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	// double id = Math.random();
	// page.showView(PieChartView.ID, " "+id, IWorkbenchPage.VIEW_ACTIVATE);
	//
	// }
	//
	// public static void histogram(ExpressionMatrix matrix) throws
	// PartInitException{
	// ModelProvider.INSTANCE.setExpressionMatrixToDisplay(matrix);
	// IWorkbenchPage page =
	// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	// double id = Math.random();
	// page.showView(HistogramView.ID, " "+id, IWorkbenchPage.VIEW_ACTIVATE);
	//
	// }
	//
	// /**
	// *
	// * @param experiment
	// * @param afterNorm true if we want to load data which have been Median/HK
	// norm<br>
	// * false if we want to load data with no Median/HK normalization
	// * @throws PartInitException
	// * @throws FileNotFoundException
	// * @throws IOException
	// */
	// public static void histogramTiling(Experiment experiment, boolean afterNorm)
	// throws
	// PartInitException, FileNotFoundException, IOException{
	// ModelProvider.INSTANCE.setExpressionMatrixToDisplay(TilingData.loadandCombineTilingData(experiment,afterNorm));
	// IWorkbenchPage page =
	// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	// double id = Math.random();
	// page.showView(HistogramView.ID, " "+id, IWorkbenchPage.VIEW_ACTIVATE);
	// }
	//
	// /**
	// *
	// * @param experiment
	// * @param afterNorm true if we want to load data which have been Median/HK
	// norm<br>
	// * false if we want to load data with no Median/HK normalization
	// * @throws PartInitException
	// * @throws FileNotFoundException
	// * @throws IOException
	// */
	// public static void histogramGeneExpData(Experiment experiment,boolean
	// afterNorm) throws
	// PartInitException, FileNotFoundException, IOException{
	// ModelProvider.INSTANCE.setExpressionMatrixToDisplay(GeneExpData.loadandCombineGeneExpData(experiment,afterNorm));
	// IWorkbenchPage page =
	// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	// double id = Math.random();
	// page.showView(HistogramView.ID, " "+id, IWorkbenchPage.VIEW_ACTIVATE);
	// }
	//
	//
	// public static void boxplot(ExpressionMatrix matrix) throws PartInitException{
	// ModelProvider.INSTANCE.setExpressionMatrixToDisplay(matrix);
	// IWorkbenchPage page =
	// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	// double id = Math.random();
	// page.showView(BoxPlotView.ID, " "+id, IWorkbenchPage.VIEW_ACTIVATE);
	//
	// }
	//
	// /**
	// *
	// * @param experiment
	// * @param afterNorm true if we want to load data which have been Median/HK
	// norm<br>
	// * false if we want to load data with no Median/HK normalization
	// * @throws PartInitException
	// * @throws FileNotFoundException
	// * @throws IOException
	// */
	// public static void boxplotTiling(Experiment experiment,boolean afterNorm)
	// throws
	// PartInitException, FileNotFoundException, IOException{
	// ModelProvider.INSTANCE.setExpressionMatrixToDisplay(TilingData.loadandCombineTilingData(experiment,afterNorm));
	// IWorkbenchPage page =
	// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	// double id = Math.random();
	// page.showView(BoxPlotView.ID, " "+id, IWorkbenchPage.VIEW_ACTIVATE);
	// }
	//
	// /**
	// *
	// * @param experiment
	// * @param afterNorm true if we want to load data which have been Median/HK
	// norm<br>
	// * false if we want to load data with no Median/HK normalization
	// * @throws PartInitException
	// * @throws FileNotFoundException
	// * @throws IOException
	// */
	// public static void boxplotGeneExpData(Experiment experiment,boolean
	// afterNorm) throws
	// PartInitException, FileNotFoundException, IOException{
	// ModelProvider.INSTANCE.setExpressionMatrixToDisplay(GeneExpData.loadandCombineGeneExpData(experiment,afterNorm));
	// IWorkbenchPage page =
	// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	// double id = Math.random();
	// page.showView(BoxPlotView.ID, " "+id, IWorkbenchPage.VIEW_ACTIVATE);
	//
	// }

	/**
	 * Vector1 and vector2 are the values of all the probes contained in a specific
	 * genome element, so:<br>
	 * We calculate the difference within vector1-vector2 (probe by probe) And then
	 * calculate the median the resulting vector
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static double foldChangeTiling(double[] vector1, double[] vector2, boolean log) {
		if (log) {
			double[] diff = VectorUtils.minus(vector1, vector2);
			return VectorUtils.median(diff);
		} else {
			double[] diff = foldChange(VectorUtils.pow2(vector1), VectorUtils.pow2(vector2));
			return VectorUtils.mean(diff);
		}

	}

	public static double foldChangeTilingWT(double[] vector1, double vector2Median, boolean log) {
		if (log)
			return VectorUtils.median(vector1) - vector2Median;
		else {
			double vector1Median = VectorUtils.median(vector1);
			return foldChange(Math.pow(2, vector1Median), Math.pow(2, vector2Median));
		}
	}

	/**
	 * Vector1 and Vectior2 correspond to three technical replicates, so:<br>
	 * We calculate the fold change by measuring the relative expression (or ratio)
	 * of the median expression of vector1 and vector2
	 * 
	 * @param vector1
	 * @param vector2
	 * @param log     if true we calculate relative expression, if false we "unlog"
	 *                and calculate the ratio of expression
	 * @return
	 */
	public static double foldChangeGeneExpression(double[] vector1, double[] vector2, boolean log) {
		if (log)
			return VectorUtils.median(vector1) - VectorUtils.median(vector2);
		else {
			double vector1Median = VectorUtils.median(vector1);
			double vector2Median = VectorUtils.median(vector2);
			return foldChange(Math.pow(2, vector1Median), Math.pow(2, vector2Median));
		}
	}

	/**
	 * Use this fonction when comparing a vector to a median value already
	 * calculated <br>
	 * For example when vector2Median is equal to WT average expression
	 * 
	 * @param vector1
	 * @param vector2Median
	 * @param log
	 * @return
	 */
	public static double foldChangeGeneExpressionWT(double[] vector1, double vector2Median, boolean log) {
		if (log)
			return VectorUtils.median(vector1) - vector2Median;
		else {
			double vector1Median = VectorUtils.median(vector1);
			return foldChange(Math.pow(2, vector1Median), Math.pow(2, vector2Median));
		}
	}

	/**
	 * Calculate ratio value1/value2
	 * 
	 * @param value1
	 * @param value2
	 * @return
	 */
	public static double foldChange(double value1, double value2) {
		if (value1 >= value2)
			return value1 / value2;
		else
			return -value2 / value1;
	}

	public static double[] foldChange(double[] vector1, double[] vector2) {
		double[] fc = new double[vector1.length];
		if (vector1.length != vector2.length) {
			System.err.println("FoldChange error: Vector have not the same size");
			return null;
		}
		for (int i = 0; i < vector1.length; i++) {
			double value1 = vector1[i];
			double value2 = vector2[i];
			fc[i] = foldChange(value1, value2);
		}
		return fc;
	}

	public static double[] logFC(double[] vector1, double[] vector2) {
		double[] logFC = foldChange(vector1, vector2);
		for (int i = 0; i < vector1.length; i++) {
			logFC[i] = MathUtils.log2(logFC[i]);
		}
		return logFC;
	}

}
