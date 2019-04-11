package bacnet.scripts.core.stat;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.utils.ArrayUtils;
import bacnet.utils.VectorUtils;

/**
 * Class for calculating LPE statistics <br>
 * Inspired from R library LPE<br>
 * 
 * @see http://www.bioconductor.org/packages/devel/bioc/html/LPE.html
 * @author UIBC
 *
 */
public class LPE {

	public static void testLPE() {
		try {
			// ExpressionMatrix data1 =
			// ExpressionMatrix.readFromFile(Project.getProjectFolder()+File.separator+"LPEdata1.txt",
			// true);
			// ExpressionMatrix data2 =
			// ExpressionMatrix.readFromFile(Project.getProjectFolder()+File.separator+"LPEdata2.txt",
			// true);
			// ExpressionMatrix resultLPE =
			// ExpressionMatrix.readFromFile(Project.getProjectFolder()+File.separator+"LPEresult.txt",
			// true);
			// PolynomialSplineFunction lpe1 = estimate(data1);
			// PolynomialSplineFunction lpe2 = estimate(data2);
			//
			// ExpressionMatrix result = new ExpressionMatrix();
			// String[] headersArray =
			// {"x.c1","x.c2","x.c3","median.1","std.dev.1","y.t1","y.t2","y.t3","median.2","std.dev.2","median.diff","pooled.std.dev","z.stats"};
			// result.setValues(new double[data1.getNumberRow()][headersArray.length]);
			// for(String header : headersArray) result.addHeader(header);
			// result.setRowNames(data1.getRowNames());
			// for(int i=0;i<data1.getValues().length;i++){
			// for(int j=0;j<3;j++){
			// result.setValue(data1.getValue(i, j), i, j);
			// result.setValue(data2.getValue(i, j), i, j+5);
			// }
			//
			// double[] x = ArrayUtils.getRow(data1.getValues(), i);
			// double[] y = ArrayUtils.getRow(data2.getValues(), i);
			// double median1 = VectorUtils.median(x);
			// double median2 = VectorUtils.median(y);
			// result.setValue(median1,i,3);
			// result.setValue(Math.sqrt(lpe1.value(median1)),i,4);
			// //result.setValue(VectorUtils.variance(x),i,4);
			// result.setValue(median2,i,8);
			// result.setValue(Math.sqrt(lpe2.value(median2)),i,9);
			// //result.setValue(VectorUtils.variance(y),i,9);
			// result.setValue(StatUtils.foldChangeGeneExpression(x,y,true),i,10);
			//
			// double sigma_pooled = Math.sqrt((Math.PI/2)*lpe1.value(median1)/3 +
			// lpe2.value(median2)/3);
			// result.setValue(sigma_pooled,i,11);
			// result.setValue(run(x, y, lpe1, lpe2),i,12);
			// }
			//
			// result.saveTab(Project.getProjectFolder()+File.separator+"LPEresultChris.txt",
			// "gene");
			// ExpressionMatrix diff = ExpressionMatrixStat.minus(result, resultLPE);
			// diff.saveTab(Project.getProjectFolder()+File.separator+"LPEresultDiff.txt",
			// "gene");
			// // ScatterPlotView.displayMatrix(amR, "A vs M");
			//

			@SuppressWarnings("unused")
			ExpressionMatrix leyMatrix = ExpressionMatrix
					.loadTab(Database.getInstance().getPath() + "/Leypreprocess 1.txt", true);
			// leyMatrix = ExpressionMatrixStat.log2(leyMatrix);

			// ExpressionMatrix leyMatrix =
			// ExpressionMatrix.readFromFile(Project.getProjectFolder()+"/Ley
			// dataset.txt", true);
			// PolynomialSplineFunction lpe1 = estimate(leyMatrix);

			// leyMatrix = AMtools.getExpressionMatrix(AMtools.AM(leyMatrix.getValues()));
			//
			// leyMatrix.saveToFile(Project.getProjectFolder()+"\\Ley AM.txt", "yo");
			//

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * For each comparison calculate the LPE estimate, by creating a matrix of all
	 * expression values and estimates LPE using each of these matrices
	 * 
	 * @param estimateLPE
	 * @param bioCondsTemp
	 */
	public static TreeMap<String, PolynomialSplineFunction> createEstimates(
			TreeMap<String, GeneExpression> bioCondsTemp) {
		TreeMap<String, PolynomialSplineFunction> estimateLPE = new TreeMap<String, PolynomialSplineFunction>();
		TreeMap<String, Integer> probes = Database.getInstance().getProbesGExpression();
		for (String geneExp : bioCondsTemp.keySet()) {
			ExpressionMatrix expression = new ExpressionMatrix();
			TreeMap<String, Integer> rowNames = new TreeMap<String, Integer>();
			int k = 0;
			for (String probe : probes.keySet()) {
				rowNames.put(probe, k);
				k++;
			}
			expression.setRowNames(rowNames);
			expression.setValues(new double[k][3]);
			ArrayList<String> headers = new ArrayList<String>();
			for (int j = 0; j < 3; j++) {
				headers.add(j + "");
			}
			expression.setHeaders(headers);

			GeneExpression bioCond1 = bioCondsTemp.get(geneExp);
			System.out.println(geneExp);
			bioCond1.load();
			for (String probe : rowNames.keySet()) {
				int l = rowNames.get(probe);
				double[] vector = bioCond1.getTriplicateValue(probe);
				expression.setValue(vector[0], l, 0);
				expression.setValue(vector[1], l, 1);
				expression.setValue(vector[2], l, 2);
			}

			// System.out.println(bioCond1.getMean());
			try {
				estimateLPE.put(geneExp, LPE.estimate(expression));
			} catch (Exception e) {
				System.out.println("Cannot calculate LPE estimate");
			}

		}
		return estimateLPE;
	}

	public static double calcZ(double[] vector1, double[] vector2, PolynomialSplineFunction lpe1,
			PolynomialSplineFunction lpe2) {
		double median1 = VectorUtils.median(vector1);
		double median2 = VectorUtils.median(vector2);
		try {
			double sigma_pooled = lpe1.value(median1) / vector1.length + lpe2.value(median2) / vector2.length;
			sigma_pooled = Math.sqrt(Math.PI / 2 * sigma_pooled);

			double z = (median1 - median2) / sigma_pooled;
			if (Double.isNaN(z))
				return 0;
			return z;
		} catch (ArgumentOutsideDomainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	public static double calcP(double[] vector1, double[] vector2, PolynomialSplineFunction lpe1,
			PolynomialSplineFunction lpe2) {
		double median1 = VectorUtils.median(vector1);
		double median2 = VectorUtils.median(vector2);
		try {
			double sigma_pooled = lpe1.value(median1) / vector1.length + lpe2.value(median2) / vector2.length;

			if (sigma_pooled > 0) {
				sigma_pooled = Math.sqrt(Math.PI / 2 * sigma_pooled);
				// evalue p-value with a normal distribution of mean = 0 and stdDev =
				// sigma_pooled
				NormalDistributionImpl normaDistrib = new NormalDistributionImpl(0, sigma_pooled);
				// calculate p-value = P(X < x) with normaDistrib
				double p = normaDistrib.cumulativeProbability(median1 - median2);
				// As we take account both tail of the distrib we need to fix p

				p = Math.min(p, 1 - p);
				p = 2 * p;
				// System.out.println((median1 - median2)+ " sig "+sigma_pooled+" z "+(median1 -
				// median2)/sigma_pooled + " p "+p);
				return p;
			} else {
				return 1;
			}

		} catch (ArgumentOutsideDomainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	public static PolynomialSplineFunction estimate(ExpressionMatrix matrix) throws Exception {

		/*
		 * Estimate local pooled error, on the complete matrix of expression
		 */
		ExpressionMatrix am = AMtools.AM(matrix);
		// ScatterPlotView.displayMatrix(am, "A vs M");
		am.setValues(ArrayUtils.sortColumn(am.getValues(), 0));
		// am = filterAM(am);
		// ScatterPlotView.displayMatrix(am, "A vs M");
		double[] quantiles = VectorUtils.quantiles(am.getColumn("A"), 100);
		ExpressionMatrix aVarM = calcVarM(am, quantiles);
		// ScatterPlotView.displayMatrix(aVarM, "a vs Var.M");
		try {
			LoessInterpolator spline = new LoessInterpolator();
			PolynomialSplineFunction interpolation = spline.interpolate(aVarM.getColumn(0), aVarM.getColumn(1));

			// ExpressionMatrix estimate = new ExpressionMatrix();
			// estimate.addHeader("A");
			// estimate.addHeader("M");
			// double[][] newValues = new double[1000][2];
			// for(int i=0;i<newValues.length;i++){
			// newValues[i][0] = (double)(i)/1000.0;
			// newValues[i][1] = interpolation.value((double)(i)/1000.0);
			// //newValues[i][1] = poly.eval((double)(i)/1000.0);
			// estimate.getRowNames().put(i+"", i);
			//
			// }
			// estimate.setValues(newValues);
			// ScatterPlotView.displayMatrix(estimate, "a vs Var.M estimate");
			return interpolation;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static ExpressionMatrix calcVarM(ExpressionMatrix am, double[] quantiles) {
		// build final Expression Matrix
		ExpressionMatrix aVarM = new ExpressionMatrix();
		aVarM.addHeader("A");
		aVarM.addHeader("varM");
		for (int i = 0; i < quantiles.length; i++) {
			aVarM.getRowNames().put(i + "", i);
		}
		aVarM.setValues(new double[quantiles.length][2]);

		// estimate variance within two consecutive quantiles
		int k = 0;
		for (int i = 0; i < quantiles.length - 1; i++) {
			double nextQuantile = quantiles[i + 1];
			ArrayList<Double> mValuesList = new ArrayList<Double>();
			while (am.getValues()[k][0] < nextQuantile) {
				mValuesList.add(am.getValues()[k][1]);
				k++;
			}

			// calc var de mValues
			double[] mValues = new double[mValuesList.size()];
			for (int j = 0; j < mValues.length; j++)
				mValues[j] = mValuesList.get(j);

			double var = VectorUtils.variance(mValues);
			// System.out.println(quantiles[i] +" "+ var);
			aVarM.setValue(quantiles[i], i, 0);
			aVarM.setValue(var, i, 1);
		}
		aVarM.setValue(quantiles[quantiles.length - 1], quantiles.length - 1, 0);
		aVarM.setValue(0, quantiles.length - 1, 1);
		return aVarM;
	}

	/**
	 * Delete values inferior to 0 in am matrix which as been sorted by A values
	 * 
	 * @param am
	 * @return
	 */
	public static ExpressionMatrix filterAM(ExpressionMatrix am) {
		int i = 0;
		while (am.getValue(i, 0) < 0) {
			i++;
		}
		// delete i first rows
		ExpressionMatrix amNew = new ExpressionMatrix();
		amNew.addHeader("A");
		amNew.addHeader("M");
		double[][] newValues = new double[am.getNumberRow()][2];
		int k = 0;
		while (i < am.getNumberRow()) {
			newValues[k][0] = am.getValue(i, 0);
			newValues[k][1] = am.getValue(i, 1);
			amNew.getRowNames().put(k + "", k);
			k++;
			i++;
		}
		amNew.setValues(newValues);
		return amNew;
	}
}
