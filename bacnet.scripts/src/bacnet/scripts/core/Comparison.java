package bacnet.scripts.core;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.scripts.core.stat.StatTest;

public class Comparison {

	public static String PATH_DIFF_EXPR = Database.getDATA_PATH() + "Summary Comparison" + File.separator + "Diff_Expr";
	public static String PATH_DIFF_LISTS = Database.getDATA_PATH() + "Summary Comparison" + File.separator
			+ "Diff_List";

	private ArrayList<String> leftBCs = new ArrayList<String>();
	private ArrayList<String> rightBCs = new ArrayList<String>();

	private ArrayList<String> genomeElements = new ArrayList<String>();

	/**
	 * List of Stat tests to run
	 */
	private ArrayList<StatTest> tests = new ArrayList<StatTest>();

	/**
	 * Indicate if we calculate comparison with tiling or geneexpression
	 */
	private boolean tiling = true;

	private ExpressionMatrix expression;
	private ExpressionMatrix compExpression;

	public Comparison() {
	}

	/**
	 * Create a Comparison object used in Statistical comparison case
	 * 
	 * @param tests
	 * @param genomeElements
	 * @param comparisonList
	 * @param tiling
	 */
	public Comparison(ArrayList<StatTest> tests, ArrayList<String> genomeElements, ArrayList<String> comparisonList,
			boolean tiling) {
		this.tests = tests;
		this.genomeElements = genomeElements;
		this.tiling = tiling;
		computeComparison(comparisonList);
	}

	/**
	 * For the list obtained in wizard panel, compute information and extract leftBC
	 * and rightBC information. These 2 lists will then be used to calculate
	 * comparison
	 * 
	 * @param comparisonList
	 */
	private void computeComparison(ArrayList<String> comparisonList) {
		for (String comp : comparisonList) {
			String[] compElement = comp.split(BioCondition.SEPARATOR);
			System.out.println(compElement[0] + "new" + compElement[1]);
			leftBCs.add(compElement[0].trim());
			rightBCs.add(compElement[1].trim());
		}
	}

	/**
	 * Go through both leftBC and rightBC and delete element which are not in
	 * bioConds
	 * 
	 * @param bioConds
	 */
	private void curateComparisonTiling(TreeMap<String, ArrayList<Tiling>> bioConds) {
		int k = 0;
		while (k < leftBCs.size()) {
			String left = leftBCs.get(k);
			String right = rightBCs.get(k);

			if (bioConds.containsKey(left) && bioConds.containsKey(right))
				k++;
			else {
				leftBCs.remove(k);
				rightBCs.remove(k);
			}
		}
	}

	/**
	 * Go through both leftBC and rightBC and delete element which are not in
	 * bioConds
	 * 
	 * @param bioConds
	 */
	private void curateComparisonGexpression(TreeMap<String, GeneExpression> bioConds) {
		int k = 0;
		while (k < leftBCs.size()) {
			String left = leftBCs.get(k);
			String right = rightBCs.get(k);

			if (bioConds.containsKey(left) && bioConds.containsKey(right))
				k++;
			else {
				leftBCs.remove(k);
				rightBCs.remove(k);
			}
		}
	}

	public ArrayList<String> getLeftBCs() {
		return leftBCs;
	}

	public void setLeftBCs(ArrayList<String> leftBCs) {
		this.leftBCs = leftBCs;
	}

	public ArrayList<String> getRightBCs() {
		return rightBCs;
	}

	public void setRightBCs(ArrayList<String> rightBCs) {
		this.rightBCs = rightBCs;
	}

	public ArrayList<String> getGenomeElements() {
		return genomeElements;
	}

	public void setGenomeElements(ArrayList<String> genomeElements) {
		this.genomeElements = genomeElements;
	}

	public ArrayList<StatTest> getTests() {
		return tests;
	}

	public void setTests(ArrayList<StatTest> tests) {
		this.tests = tests;
	}

	public boolean isTiling() {
		return tiling;
	}

	public void setTiling(boolean tiling) {
		this.tiling = tiling;
	}

	public ExpressionMatrix getExpression() {
		return expression;
	}

	public void setExpression(ExpressionMatrix expression) {
		this.expression = expression;
	}

	public ExpressionMatrix getCompExpression() {
		return compExpression;
	}

	public void setCompExpression(ExpressionMatrix compExpression) {
		this.compExpression = compExpression;
	}
}
