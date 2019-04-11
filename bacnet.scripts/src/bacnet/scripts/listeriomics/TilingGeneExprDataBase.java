package bacnet.scripts.listeriomics;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import bacnet.Database;
import bacnet.datamodel.annotation.Signature;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Genome;
import bacnet.scripts.core.Expression;
import bacnet.scripts.core.normalization.GENormalization;
import bacnet.scripts.core.normalization.GRNormalization;
import bacnet.scripts.core.normalization.MedianNormalization;
import bacnet.scripts.database.ComparisonsCreation;

public class TilingGeneExprDataBase {

	/**
	 * This fundamental method create all the files necessary to make Listeriomics
	 * software and website work!<br>
	 * <br>
	 * 
	 * @throws Exception
	 */
	public static void run() throws Exception {

		Experiment exp = Experiment.getGeneralExp();
		// ExperimentTools.findMissingCELfiles();
		// Experiment exp = new Experiment();
		// exp.addBioCond(BioCondition.getBioCondition("rli38"));
		// exp.addBioCond(BioCondition.getBioCondition("EGDe_121109"));
		/*
		 * Run normalization
		 */
		/*
		 * LAST RUN: 07/05/2014 NO ERROR !!!! Run GeneExpression Normalization
		 */
		GENormalization.norm(exp);

		/*
		 * LAST RUN: 07/05/2014 NO ERROR !!!! Run Tiling normalization
		 */
		// GRNormalization.norm(exp);
		/*
		 * Verify if all data have been normalized
		 */
		// verifNumberOfData(exp);
		/*
		 * Organized normalized data, this method has to be run when we are sure that
		 * normalize(exp) is finished, otherwise it will not be completed
		 */
		organizeExpressionData(exp);
		/*
		 * Create EGDeWT Mean and Deviation and Convert in streaming data wait for all
		 * normalization to finish before converting
		 */
		GeneExpression.convert(exp);
		// Tiling.convert(exp);
		/*
		 * Create Wild type data which are the total mean on all data WARNING: TAKE VERY
		 * LONG LAST RUN: 07/05/2014
		 */
		// EGDeWTdata.createEGDeAverage();
		/*
		 * Organize comparisons data
		 */
		calcGenExprTilingComparisons(exp);

		/*
		 * Quality control tools
		 */
		// qualityControl(exp);

		System.out.println("Finish database Cossart lab creation ");
	}

	private static void organizeExpressionData(Experiment exp) {
		try {
			/*
			 * normalize all GeneExpression data
			 * 
			 * LAST RUN: 05/12/2013 NO ERROR !!!! 86 GeneExpression created
			 */
			// System.out.println("Expected number of GE files + EGDeWT (not normalized):
			// "+Experiment.getGeneralExp().getGeneExprs().size());
			GENormalization.extractInformation(exp);
			MedianNormalization.normGE(exp);
			GENormalization.qualityControlNorm();
			GENormalization.qualityControlExtract();
			GeneExpression.createProbeList();

			/*
			 * normalize all Tiling data
			 * 
			 * LAST RUN: 05/12/2013 NO ERROR !!!! 156 Tiling created
			 */
			MedianNormalization.normTiling(exp);
			GRNormalization.createEdgeWT();
			GRNormalization.qualityControlNorm(exp);
			Tiling.createProbeList();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void convert(Experiment exp) {
		/*
		 * wait for all normalization to finish before converting
		 */
		try {
			GeneExpression.convert(exp);
			Tiling.convert(exp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Calculate GeneExpr and Tiling comparisons files
	 * 
	 * @param exp
	 */
	public static void calcGenExprTilingComparisons(Experiment exp) {
		/*
		 * From all BioCondition get a list of all possible comparisons
		 */
		TreeSet<String> compSet = new TreeSet<String>();
		for (BioCondition bioCond : exp.getBioConditions()) {
			if (bioCond.getTypeDataContained().contains(TypeData.GeneExpr)
					|| bioCond.getTypeDataContained().contains(TypeData.Tiling)) {
				for (String comparison : bioCond.getComparisonNames()) {
					compSet.add(comparison);
				}
			}
		}
		ArrayList<String> compList = new ArrayList<String>();
		for (String comp : compSet)
			compList.add(comp);
		if (!compList.isEmpty()) {
			/*
			 * Create all streaming data comparisons
			 */
			ComparisonsCreation.setAllComparisonStreamData(compList);

			/*
			 * Create all comparison matrix and lists
			 */
			ComparisonsCreation.createAllCompMatrix(compList);

		}

	}

	/**
	 * Go through all <code>BioCondition</code> and detect which GeneExpression and
	 * Tiling have not been normalized
	 * 
	 * @param exp
	 * @return
	 */
	@SuppressWarnings("unused")
	private static void verifNumberOfData(Experiment exp) {
		ArrayList<String> geneExprs = new ArrayList<>();
		ArrayList<String> tilings = new ArrayList<>();
		for (BioCondition bioCond : exp.getBioConditions()) {
			for (GeneExpression geneExpr : bioCond.getGeneExprs())
				geneExprs.add(geneExpr.getName());
			for (Tiling tiling : bioCond.getTilings())
				tilings.add(tiling.getName());
		}

		for (String geneExpr : geneExprs) {
			File file = new File(OmicsData.PATH_EXPR_NORM + geneExpr);
			if (!file.exists()) {
				System.err.println("Did not found: " + geneExpr);
			}
		}
		for (String tiling : tilings) {
			File file = new File(OmicsData.PATH_TILING_NORM + tiling);
			if (!file.exists()) {
				System.err.println("Did not found: " + OmicsData.PATH_TILING_NORM + tiling);
			}
		}
	}

	/**
	 * Look at the Expression of HouseKeeping genes<br>
	 * Has to be run from PostInit!
	 * 
	 * @param exp
	 */
	@SuppressWarnings("unused")
	private static void qualityControl(Experiment exp) {
		Expression.summarize(exp, Genome.loadEgdeGenome());
		Database database = Database.initDatabase(Database.LISTERIOMICS_PROJECT);
		Signature hkGenes = Signature.getSignatureFromName("House-Keeping genes");

		ExpressionMatrix allGeneExpr = ExpressionMatrix.load(Expression.PATH_ALLGENEXPR);
		allGeneExpr = allGeneExpr.getSubMatrixRow(hkGenes.getElements());
		allGeneExpr.saveTab(Database.getTEMP_PATH() + "GeneExprHouseKeeping_QC.txt", "r");
		allGeneExpr = ExpressionMatrix.loadTab(Expression.PATH_ALLGENEXPR + "_WT.txt", true);
		allGeneExpr = allGeneExpr.getSubMatrixRow(hkGenes.getElements());
		allGeneExpr.saveTab(Database.getTEMP_PATH() + "GeneExprHouseKeeping_WT_QC.txt", "r");

		ExpressionMatrix allTiling = ExpressionMatrix.load(Expression.PATH_ALLTILING);
		allTiling = allTiling.getSubMatrixRow(hkGenes.getElements());
		allTiling.saveTab(Database.getTEMP_PATH() + "TilingHouseKeeping_QC.txt", "r");

		/*
		 * Then execute in bacnet.rcp.test.test.runPostInit():
		 * HeatMapView.displayMatrix(ExpressionMatrix.loadTab(Database.getTEMP_PATH()+
		 * "GeneExprHouseKeeping_QC.txt", true), "GeneExprHouseKeeping_QC");
		 * HeatMapView.displayMatrix(ExpressionMatrix.loadTab(Database.getTEMP_PATH()+
		 * "TilingHouseKeeping_QC.txt", true), "TilingHouseKeeping_QC");
		 */

	}
}
