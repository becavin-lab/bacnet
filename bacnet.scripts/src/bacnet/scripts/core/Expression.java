package bacnet.scripts.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.EGDeWTdata;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.ColNames;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.genomeBrowser.core.DataTrack;
import bacnet.genomeBrowser.core.Track;
import bacnet.utils.VectorUtils;

/**
 * A list of methods to combine transcriptomics data in ExpressionMatrix
 * 
 * @author UIBC
 *
 */
public class Expression {

	public static String PATH_ALLGENEXPR = Database.getTRANSCRIPTOMES_PATH() + File.separator + "AllGeneExpr";
	public static String PATH_ALLTILING = Database.getTRANSCRIPTOMES_PATH() + File.separator + "AllTiling";
	public static String PATH_ALLRNASEQ = Database.getTRANSCRIPTOMES_PATH() + File.separator + "AllRNASeq";
	public static String PATH_ALLExpMatrix = Database.getTRANSCRIPTOMES_PATH() + File.separator + "AllExpMatrix";
	public static String PATH_ALLDataType = Database.getTRANSCRIPTOMES_PATH() + File.separator + "AllDataType";

	private ArrayList<BioCondition> bioConditions = new ArrayList<BioCondition>();
	private ArrayList<String> genomeElements = new ArrayList<String>();
	private Genome genome = new Genome();

	/**
	 * Indicate the type of data to show
	 */
	private TypeData typeData = TypeData.GeneExpr;

	private ExpressionMatrix expression;

	/**
	 * Create <code>Expression</code> object to regroups multiple Expression data
	 * 
	 * @param tests
	 * @param genomeElements
	 * @param comparisonList
	 * @param tiling
	 */
	public Expression(Genome genome, ArrayList<String> genomeElements, ArrayList<BioCondition> bioCondList,
			TypeData typeData) {
		this.genome = genome;
		this.genomeElements = genomeElements;
		this.typeData = typeData;
		this.bioConditions = bioCondList;
	}

	/**
	 * Combine all Transcriptomes describe in <code>Expression</code> data, and
	 * return the combine <code>ExpressionMatrix</code>
	 * 
	 * @return
	 */
	public ExpressionMatrix createExpressionMatrix(Genome genome) {
		// prepare data
		expression = initExpressionMatrix();
		DataTrack datas = new DataTrack(new Track(genome, genome.getFirstChromosome().getChromosomeID()));
		for (BioCondition bioCondition : bioConditions) {
			datas.addBioCondition(bioCondition.getName());
		}

		/*
		 * Init data specifically in the case of Tiling and RNASeq
		 */
		HashMap<String, ExpressionMatrix> matricesRNASeq = new HashMap<String, ExpressionMatrix>();
		if (typeData == TypeData.Tiling) {
			for (BioCondition bioCondition : bioConditions) {
				datas.addBioCondition(bioCondition.getName());
				for (Tiling tilingTemp : bioCondition.getTilings()) {
					tilingTemp.read();
				}
			}
		} else if (typeData == TypeData.RNASeq) {
			for (BioCondition bioCondition : bioConditions) {
				if (bioCondition.getNGSSeqs().size() != 0) {
					ExpressionMatrix matrix = ExpressionMatrix
							.loadTab(OmicsData.PATH_NGS_NORM + bioCondition.getName() + NGS.EXTENSION, false);
					matricesRNASeq.put(bioCondition.getName(), matrix);
				}
			}
		}

		/*
		 * Process matrix
		 */
		for (String gElement : genomeElements) {
			System.out.println(gElement);
			Sequence sequence = genome.getElement(gElement);
			if (sequence != null) {
				for (BioCondition bioCondition : bioConditions) {
					// System.out.println(bioCondition.getName());
					if (typeData == TypeData.Tiling) {
						double[] value = new double[0];
						for (Tiling tilingTemp : datas.getTilings(bioCondition.getName())) {
							if (sequence.isStrand() && tilingTemp.getName().contains("+")) {
								value = tilingTemp.get(sequence.getBegin(), sequence.getEnd(), true);
							} else if (!sequence.isStrand() && tilingTemp.getName().contains("-")) {
								value = tilingTemp.get(sequence.getBegin(), sequence.getEnd(), true);
							}
						}
						double medianValue = VectorUtils.median(value);
						expression.setValue(medianValue, gElement,
								bioCondition.getName() + "[" + this.getTypeData() + "]");
					} else if (typeData == TypeData.GeneExpr) {
						TreeMap<String, Integer> probes = Database.getInstance().getProbesGExpression();
						GeneExpression geneExpr = datas.getGeneExprs(bioCondition.getName()).get(0);
						if (probes.containsKey(gElement)) {
							double value = geneExpr.getMedianValue(gElement);
							expression.setValue(value, gElement,
									bioCondition.getName() + "[" + this.getTypeData() + "]");
						}
					} else if (typeData == TypeData.ExpressionMatrix) {
						ExpressionMatrix matrix = datas.getMatrices(bioCondition.getName()).get(0);
						if (matrix.getRowNames().containsKey(gElement)) {
							double value = matrix.getValue(gElement, ColNames.VALUE + "");
							expression.setValue(value, gElement,
									bioCondition.getName() + "[" + this.getTypeData() + "]");
						}
					} else if (typeData == TypeData.RNASeq) {
						ExpressionMatrix matrix = matricesRNASeq.get(bioCondition.getName());
						if (matrix != null) {
							if (matrix.getRowNames().containsKey(gElement)) {
								double value = matrix.getValue(gElement, ColNames.VALUE + "");
								expression.setValue(value, gElement,
										bioCondition.getName() + "[" + this.getTypeData() + "]");
							}
						}
					}
				}
			}
		}
		return expression;
	}

	/**
	 * Create an ExpressionMatrix for the data <br>
	 * Method dependents on this.bioConditions, this.tiling and this.genomeElements
	 * 
	 * @param genome
	 * @return
	 */
	private ExpressionMatrix initExpressionMatrix() {
		ExpressionMatrix expression = new ExpressionMatrix();
		TreeMap<String, Integer> rowNames = new TreeMap<String, Integer>();
		int k = 0;
		for (String gElement : genomeElements) {
			rowNames.put(gElement, k);
			k++;
		}
		expression.setRowNames(rowNames);
		expression.setValues(new double[k][bioConditions.size()]);
		ArrayList<String> headers = new ArrayList<String>();
		for (BioCondition bioCond : bioConditions) {
			headers.add(bioCond.getName() + "[" + this.getTypeData() + "]");
		}
		expression.setHeaders(headers);
		return expression;
	}

	/**
	 * Create 3 ExpressionMatrix: <br>
	 * <li>GeneExpression of genes on all BioCondition, saved in PATH_ALLGENEXPR
	 * <li>Tiling of genes on all BioCondition, saved in PATH_ALLTILING_GENE
	 * <li>Tiling of Srnas on all BioCondition, saved in PATH_ALLTILING_SRNA
	 */
	public static void summarize(Experiment exp, Genome genome) {
		ArrayList<BioCondition> bioConds = exp.getBioConditions();

		/*
		 * List of biological conditions
		 */
		ArrayList<BioCondition> bioCondGeneExprs = new ArrayList<BioCondition>();
		for (BioCondition bioCond : bioConds) {
			if (bioCond.getGeneExprs().size() != 0) {
				bioCondGeneExprs.add(bioCond);
			}
		}
		ArrayList<BioCondition> bioCondTilings = new ArrayList<BioCondition>();
		for (BioCondition bioCond : bioConds) {
			if (bioCond.getTilings().size() != 0) {
				bioCondTilings.add(bioCond);
			}
		}
		ArrayList<BioCondition> bioCondRNASeqs = new ArrayList<BioCondition>();
		for (BioCondition bioCond : bioConds) {
			if (bioCond.getNGSSeqs().size() != 0) {
				bioCondRNASeqs.add(bioCond);
			}
		}
		ArrayList<BioCondition> bioCondExprMatrices = new ArrayList<BioCondition>();
		for (BioCondition bioCond : bioConds) {
			if (bioCond.getMatrices().size() != 0) {
				bioCondExprMatrices.add(bioCond);
			}
		}

		/*
		 * List of genomeElements
		 */
		ArrayList<String> genes = new ArrayList<String>();
		ArrayList<String> allGenomeElements = genome.getAllElementNames();

		ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
		/*
		 * All GeneExpression data
		 */
		if (bioCondGeneExprs.size() != 0) {
			Expression showExpr = new Expression(genome, genes, bioCondGeneExprs, TypeData.GeneExpr);
			ExpressionMatrix matrix = showExpr.createExpressionMatrix(genome);
			Annotation.addAnnotation(matrix, genome);
			matrix.saveTab(PATH_ALLGENEXPR + "_" + genome.getSpecies() + ".excel", "Gene");
			matrices.add(matrix);
		}
		// /*
		// * All Tiling
		// */
		if (bioCondTilings.size() != 0) {
			Expression showExpr = new Expression(genome, allGenomeElements, bioCondTilings, TypeData.Tiling);
			ExpressionMatrix matrix = showExpr.createExpressionMatrix(genome);
			Annotation.addAnnotation(matrix, genome);
			matrix.saveTab(PATH_ALLTILING + "_" + genome.getSpecies() + ".excel", "GenomeElements");
			matrices.add(matrix);
		}
		/*
		 * All RNASeq
		 */
		if (bioCondRNASeqs.size() != 0) {
			Expression showExpr = new Expression(genome, allGenomeElements, bioCondRNASeqs, TypeData.RNASeq);
			ExpressionMatrix matrix = showExpr.createExpressionMatrix(genome);
			Annotation.addAnnotation(matrix, genome);
			matrix.saveTab(PATH_ALLRNASEQ + "_" + genome.getSpecies() + ".excel", "GenomeElements");
			matrices.add(matrix);
		}
		/*
		 * ExpressionMatrix
		 */
		if (bioCondExprMatrices.size() != 0) {
			Expression showExpr = new Expression(genome, genes, bioCondExprMatrices, TypeData.ExpressionMatrix);
			ExpressionMatrix matrix = showExpr.createExpressionMatrix(genome);
			Annotation.addAnnotation(matrix, genome);
			matrix.saveTab(PATH_ALLExpMatrix + "_" + genome.getSpecies() + ".excel", "Gene");
			matrices.add(matrix);
		}

		if (matrices.size() > 0) {
			ExpressionMatrix matrix = ExpressionMatrix.merge(matrices, false);
			matrix.saveTab(PATH_ALLDataType + "_" + genome.getSpecies() + ".excel", "GenomeElements");
		}

	}

	/**
	 * Test ShowExpression.showExpression();
	 */
	public static void test() {
		ArrayList<String> genomeElements = new ArrayList<String>();
		Genome genome = Genome.loadEgdeGenome();
		for (String gene : genome.getFirstChromosome().getGenes().keySet()) {
			genomeElements.add(gene);
		}
		for (String sRNA : genome.getFirstChromosome().getsRNAs().keySet()) {
			genomeElements.add(sRNA);
		}
		for (String operon : genome.getFirstChromosome().getOperons().keySet()) {
			genomeElements.add(operon);
		}

		ArrayList<String> bioCondList = new ArrayList<String>();
		bioCondList.add(EGDeWTdata.NAME_Deviation);
		bioCondList.add(OmicsData.GENERAL_WT_NAME);
		bioCondList.add("virR");

		// Expression show = new Expression(genomeElements, bioCondList, true);
		// ExpressionMatrix result = show.showExpression();
		// try {
		// TableView.displayMatrix(result, "Show expression");
		// } catch (PartInitException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public ArrayList<BioCondition> getBioConditions() {
		return bioConditions;
	}

	public void setBioConditions(ArrayList<BioCondition> bioConditions) {
		this.bioConditions = bioConditions;
	}

	public ArrayList<String> getGenomeElements() {
		return genomeElements;
	}

	public void setGenomeElements(ArrayList<String> genomeElements) {
		this.genomeElements = genomeElements;
	}

	public Genome getGenome() {
		return genome;
	}

	public void setGenome(Genome genome) {
		this.genome = genome;
	}

	public TypeData getTypeData() {
		return typeData;
	}

	public void setTypeData(TypeData typeData) {
		this.typeData = typeData;
	}

	public ExpressionMatrix getExpression() {
		return expression;
	}

	public void setExpression(ExpressionMatrix expression) {
		this.expression = expression;
	}

}
