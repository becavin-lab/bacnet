package bacnet.datamodel.dataset;

import java.util.ArrayList;

import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Genome;

/**
 * List of method for creating EGDe_Mean data: A data created from the mean of
 * all Wild Type data (37C in BHI)
 * 
 * @author UIBC
 *
 */
public class EGDeWTdata {

	public static String NAME_Mean = ExpressionData.GENERAL_WT_NAME + "_Mean";
	public static String NAME_Deviation = ExpressionData.GENERAL_WT_NAME + "_Deviation";

	/**
	 * List of wild types to use for creating EGDe_Mean
	 */
	public static String[] wildTypes = { "EGDe-010910-Porto", "EGDe_010910", "EGDe_021012", "EGDe_030510",
			"EGDe_050609", "EGDe_090610", "EGDe_121109", "EGDe_130911", "EGDe_171111", "EGDe_180110", "EGDe_241109",
			"EGDe_270407", "EGDe_270911", "EGDe_280212" };

	/**
	 * From the list of Wild Type data given by <code>EGDeWTdata.wildTypes</code>
	 * calculate the mean and save it
	 * 
	 */
	public static void createEGDeAverage() {
		// read all WT data
		ArrayList<String> wtList = new ArrayList<>();
		for (String wtType : wildTypes)
			wtList.add(wtType);

		ArrayList<GeneExpression> geneExprs = new ArrayList<GeneExpression>();
		ArrayList<Tiling> tilingsPlus = new ArrayList<Tiling>();
		ArrayList<Tiling> tilingsMinus = new ArrayList<Tiling>();
		String ret = "Create from: \n";
		for (String bioCond : wtList) {
			System.out.println(bioCond);
			BioCondition bioCondTemp = BioCondition.getBioCondition(bioCond);
			ret += bioCond + "\n";
			bioCondTemp.getGeneExprs().get(0).read();
			geneExprs.add(bioCondTemp.getGeneExprs().get(0));
			bioCondTemp.getTilings().get(0).read();
			tilingsPlus.add(bioCondTemp.getTilings().get(0));
			bioCondTemp.getTilings().get(1).read();
			tilingsMinus.add(bioCondTemp.getTilings().get(1));

		}
		System.out.println(ret);

		/*
		 * Create a biocondition and data corresponding containig the mean of expression
		 */
		BioCondition bioCondition = new BioCondition(NAME_Mean);
		bioCondition.setComment(ret);
		bioCondition.setGenomeName(Genome.EGDE_NAME);
		bioCondition.setWildType(true);
		GeneExpression geneExprWT = GeneExpression.getMean(geneExprs, NAME_Mean + GeneExpression.EXTENSION);
		geneExprWT.setBioCondName(NAME_Mean);
		geneExprWT.save();
		bioCondition.getGeneExprs().add(geneExprWT);
		Tiling tilingWTPlus = Tiling.getMean(tilingsPlus, NAME_Mean + ".+" + Tiling.EXTENSION);
		tilingWTPlus.setBioCondName(NAME_Mean);
		tilingWTPlus.save();
		bioCondition.getTilings().add(tilingWTPlus);
		Tiling tilingWTMinus = Tiling.getMean(tilingsMinus, NAME_Mean + ".-" + Tiling.EXTENSION);
		tilingWTMinus.setBioCondName(NAME_Mean);
		tilingWTMinus.save();
		bioCondition.getTilings().add(tilingWTMinus);

		/*
		 * Create a BioCondition and data corresponding containing the stat devition of
		 * expression
		 */
		BioCondition bioConditionDev = new BioCondition(NAME_Deviation);
		bioConditionDev.setComment(ret);
		bioConditionDev.setGenomeName(Genome.EGDE_NAME);
		bioConditionDev.setWildType(true);
		GeneExpression geneExprWTDev = GeneExpression.getMean(geneExprs, NAME_Deviation + GeneExpression.EXTENSION);
		geneExprWTDev.setBioCondName(NAME_Mean);
		geneExprWTDev.save();
		bioConditionDev.getGeneExprs().add(geneExprWTDev);
		Tiling tilingWTPlusDev = Tiling.getDeviation(tilingsPlus, NAME_Deviation + ".+" + Tiling.EXTENSION);
		tilingWTPlusDev.setBioCondName(NAME_Mean);
		tilingWTPlusDev.save();
		bioConditionDev.getTilings().add(tilingWTPlusDev);
		Tiling tilingWTMinusDev = Tiling.getDeviation(tilingsMinus, NAME_Deviation + ".-" + Tiling.EXTENSION);
		tilingWTMinusDev.setBioCondName(NAME_Mean);
		tilingWTMinusDev.save();
		bioConditionDev.getTilings().add(tilingWTMinusDev);

	}

	/**
	 * Read GeneEpression corresponding to NAME_Mean, and return it
	 * 
	 * @return
	 */
	public static GeneExpression getGeneExpr() {
		ArrayList<String> bioconds = new ArrayList<String>();
		bioconds.add(NAME_Mean);
		return GeneExpression.getGExpressionData(bioconds).get(NAME_Mean);
	}

	/**
	 * Read all Tiling data corresponding to NAME_Mean, and return an ArrayList of
	 * Tiling
	 * 
	 * @return
	 */
	public static ArrayList<Tiling> getTilings() {
		ArrayList<String> bioconds = new ArrayList<String>();
		bioconds.add(NAME_Mean);
		return Tiling.getTilingData(bioconds).get(NAME_Mean);
	}
}
