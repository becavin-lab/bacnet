package bacnet.scripts.database;

import java.io.File;
import java.util.ArrayList;

import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.annotation.Signature;
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
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.arrayexpress.ArrayExpress;
import bacnet.scripts.core.Expression;
import bacnet.scripts.core.normalization.MedianNormalization;
import bacnet.scripts.core.normalization.VarianceNormalization;
import bacnet.scripts.core.stat.StatTest;
import bacnet.scripts.core.stat.StatTest.TypeStat;
import bacnet.utils.ExpressionMatrixStat;
import bacnet.utils.FileUtils;
import bacnet.utils.Filter;
import bacnet.utils.VectorUtils;

/**
 * List of methods to add the different transcriptomic datasetsto your
 * multi-omics website
 * 
 * @author UIBC
 *
 */
public class TranscriptomesCreation {

	/**
	 * Convert all transcriptomics files<br>
	 * <br>
	 * - Convert all Seq data: RNASeq, RiboSeq, TSS, TermSeq<br>
	 * - Convert all GeneExpression and Tiling<br>
	 * - Convert all ExpressionMatrix<br>
	 * 
	 * @param bioConds
	 * @param logs
	 * @return logs of the process
	 */
	public static String addTranscriptomeToDatabase(ArrayList<String> bioConds, String logs) {
		/*
		 * List biological conditions to add
		 */
		boolean logTransformed = true;
		Experiment exp = new Experiment();
		for (String bioCond : bioConds) {
			exp.addBioCond(BioCondition.getBioCondition(bioCond));
		}

		/*
		 * RNASeq, RiboSeq, TSS, TermSeq
		 */
		logs += "Convert all Seq data: RNASeq, RiboSeq, TSS, TermSeq\n";
		// NGSCreation.convertCoverageFiles(exp, logTransformed);
		/*
		 * Optional NGSCreation.normalizeCountFiles(exp);
		 */

		/*
		 * Tiling and GeneExpressionData
		 */
		logs += "Convert all GeneExpression and Tiling\n";
		try {
			FileUtils.copy(GeneExpression.PROBES_PATH_2, GeneExpression.PROBES_PATH);
			FileUtils.copy(Tiling.PROBES_PATH_2, Tiling.PROBES_PATH);
			// GeneExpression.convert(exp);
			// Tiling.convert(exp);
//            TilingGeneExprDataBase.calcGenExprTilingComparisons(exp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * ExpressionMatrix
		 */
		logs += "Convert all ExpressionMatrix\n";
		convertExpressionMatrix(exp);

		return logs;

	}

	/**
	 * Process unprocessed data and save them in Streaming folder, ExpressionMatrix
	 * of comparisons mainly
	 * 
	 * @param exp
	 */
	private static void convertExpressionMatrix(Experiment exp) {
		for (BioCondition bioCondition : exp.getBioConditions()) {
			if (bioCondition.getTypeDataContained().contains(TypeData.ExpressionMatrix)) {
				System.out.println(bioCondition.getName());
				for (ExpressionMatrix data : bioCondition.getMatrices()) {
					File fileRawData = new File(OmicsData.PATH_EXPR_RAW + data.getName() + ".txt");
					if (fileRawData.exists()) {
						System.out.println("convert: " + OmicsData.PATH_EXPR_RAW + data.getName() + ".txt");
						/*
						 * remove duplicates in rowNames (if needed)
						 */
						ExpressionMatrix matrix = ArrayExpress
								.curateMatrix(OmicsData.PATH_EXPR_RAW + data.getName() + ".txt", true);
						/*
						 * Save matrix
						 */
						matrix = MedianNormalization.norm(matrix, "VALUE");
						matrix.saveTab(OmicsData.PATH_EXPR_NORM + data.getName() + ".txt", "Gene");
						matrix.setName(data.getName());
						matrix.setBioCondName(bioCondition.getName());
						matrix.save(OmicsData.PATH_STREAMING + data.getName() + OmicsData.EXTENSION);
						System.out.println(data.getName() + OmicsData.EXTENSION);
					}
				}
				for (String comparison : bioCondition.getComparisonDataNames()) {
					File fileRawData = new File(OmicsData.PATH_EXPR_RAW + comparison + ".txt");
					if (fileRawData.exists()) {
						System.out.println("convert: " + OmicsData.PATH_EXPR_RAW + comparison + ".txt");
						/*
						 * remove duplicates in rowNames (if needed)
						 */
						ExpressionMatrix matrix = ArrayExpress
								.curateMatrix(OmicsData.PATH_EXPR_RAW + comparison + ".txt", true);
						/*
						 * Save matrix
						 */
						matrix = MedianNormalization.norm(matrix, "LOGFC");
						matrix.saveTab(OmicsData.PATH_EXPR_NORM + comparison + ".txt", "Gene");
						matrix.setName(comparison);
						matrix.setBioCondName(comparison);
						matrix.save(OmicsData.PATH_STREAMING + comparison + OmicsData.EXTENSION);
					}
				}
			}
		}
	}

	/**
	 * Create LogFC summary matrix for transcriptomics datasets
	 * 
	 * @param bioConds
	 * @param logs
	 * @param varianceNorm if variance normalization is needed
	 * @return the logs of the creation
	 */
	public static String createLogfcTranscriptomeTable(ArrayList<String> bioConds, String logs, boolean varianceNorm) {
		/*
		 * List biological conditions to add
		 */
		Experiment exp = new Experiment();
		for (String bioCond : bioConds) {
			exp.addBioCond(BioCondition.getBioCondition(bioCond));
		}
		/*
		 * Modify datasets to assure variance = 1
		 */
		logs += "Apply variance normalization to all datasets (Long calculation)\n";
		if (varianceNorm) {
			// varianceNormalization(exp);
		}

		/*
		 * Create one LogFC table per Genome
		 */
		logs += "Create LogFC transcriptomes table\n";
		ArrayList<String> genomeList = BioCondition.getTranscriptomesGenomes();
		for (String genomeName : genomeList) {
			Genome genome = Genome.loadGenome(genomeName);
			Experiment expTemp = new Experiment();
			for (BioCondition bioCond : exp.getBioConditions()) {
				if (bioCond.getGenomeName().equals(genomeName)) {
					if (bioCond.containTranscriptomes()) {
						expTemp.addBioCond(bioCond);
					}
				}
			}

			/*
			 * Summarize all comparisons in matrices
			 */
			createLogFCMatrix(expTemp, genome);

			/*
			 * Summarize all expression in matrices
			 */
			Expression.summarize(expTemp, genome);

		}
		logs += Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + " tables created";
		return logs;
	}

	/**
	 * Run some scripts to analyse the statistics of the transcriptomics data and
	 * performed VarianceNormalization:<br>
	 * It will reduce the total variance of each data to 1<br>
	 * For each column of the ExpressionMatrix, multiply each value by:
	 * Math.sqr(1/var(x))<br>
	 * So final variance of each column is equal to zero
	 * 
	 * @param exp
	 */
	public static void varianceNormalization(Experiment exp) {
		ArrayList<String> stats = new ArrayList<>();
		double cutoff = 1.5;
		String[] headers = { "Name", "nbRow", "FilteredElements logFC>" + cutoff, "Min", "Median", "Mean", "Max",
				"deviation", "1st decile", "last decile", "TypeData" };
		String header = "";
		for (String temp : headers)
			header += temp + "\t";
		stats.add(header);

		Filter filterLogFC = new Filter();
		filterLogFC.setTypeFilter(Filter.TypeFilter.SUPERIOR_ABS);
		filterLogFC.setTableElementName(TypeStat.LOGFC + "");
		filterLogFC.setCutOff1(cutoff);

		for (BioCondition bioCond : exp.getBioConditions()) {
			System.out.println("biocond: " + bioCond.getName());
			for (String bioCond2Name : bioCond.getComparisons()) {
				System.out.println("bio2 " + bioCond2Name);
				BioCondition bioCond2 = BioCondition.getBioCondition(bioCond2Name);
				BioCondition bioConditionCompare = bioCond.compare(bioCond2, false);
				for (OmicsData data : bioConditionCompare.getTranscriptomesData()) {
					System.out.println("test: " + data.getName());
					if (data.getType() == TypeData.Tiling) {
						filterLogFC.setTableElementName("LOGFC");
						Tiling transcriptome = (Tiling) data;
						transcriptome.read();
						System.out.println(data.getName());
						ExpressionMatrix matrix = transcriptome.toExpressionMatrix();
						matrix = VarianceNormalization.norm(matrix, "LOGFC");
						for (String rowName : matrix.getRowNames().keySet()) {
							transcriptome.setValue(Integer.parseInt(rowName), matrix.getValue(rowName, "LOGFC"));
						}
						transcriptome.setStat();
						transcriptome.save();

						System.out.println(data.getName());
						String stat = matrix.getName() + "\t" + matrix.getNumberRow() + "\t";
						ExpressionMatrix filteredMatrix = ExpressionMatrixStat.filter(matrix, filterLogFC);
						stat += filteredMatrix.getNumberRow() + "\t";
						stat += ExpressionMatrixStat.min(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.median(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.mean(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.max(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.deviation(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += VectorUtils.quantiles(
								VectorUtils.deleteMissingValue(matrix.getColumn(matrix.getGenomeViewerColumnIndex())),
								100)[10] + "\t";
						stat += VectorUtils.quantiles(
								VectorUtils.deleteMissingValue(matrix.getColumn(matrix.getGenomeViewerColumnIndex())),
								100)[90] + "\t";
						stat += data.getType() + "";
						stats.add(stat);
					} else if (data.getType() == TypeData.GeneExpr) {
						filterLogFC.setTableElementName(ColNames.LOGFC + "");
						GeneExpression transcriptome = (GeneExpression) data;
						transcriptome.read();
						ExpressionMatrix matrix = transcriptome.toExpressionMatrix();
						matrix = VarianceNormalization.norm(matrix, ColNames.LOGFC + "");
						for (String rowName : matrix.getRowNames().keySet()) {
							transcriptome.setMedianValue(rowName, matrix.getValue(rowName, ColNames.LOGFC + ""));
						}
						transcriptome.setStat();
						transcriptome.save();

						System.out.println(data.getName());
						String stat = matrix.getName() + "\t" + matrix.getNumberRow() + "\t";
						ExpressionMatrix filteredMatrix = ExpressionMatrixStat.filter(matrix, filterLogFC);
						stat += filteredMatrix.getNumberRow() + "\t";
						stat += ExpressionMatrixStat.min(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.median(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.mean(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.max(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.deviation(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += VectorUtils.quantiles(
								VectorUtils.deleteMissingValue(matrix.getColumn(matrix.getGenomeViewerColumnIndex())),
								100)[10] + "\t";
						stat += VectorUtils.quantiles(
								VectorUtils.deleteMissingValue(matrix.getColumn(matrix.getGenomeViewerColumnIndex())),
								100)[90] + "\t";
						stat += data.getType() + "";
						stats.add(stat);
					} else if (data.getType() == TypeData.ExpressionMatrix) {
						filterLogFC.setTableElementName(ColNames.LOGFC + "");
						ExpressionMatrix matrix = (ExpressionMatrix) data;
						matrix.load();
						/*
						 * Remove rows with missing values
						 */
						ArrayList<String> noMissingValueRow = new ArrayList<>();
						for (String row : matrix.getRowNames().keySet()) {
							double value = matrix.getValue(row, ColNames.LOGFC + "");
							if (value != OmicsData.MISSING_VALUE) {
								noMissingValueRow.add(row);
							}
						}
						matrix = matrix.getSubMatrixRow(noMissingValueRow);

						/*
						 * Normalize by the variance
						 */
						matrix = VarianceNormalization.norm(matrix, ColNames.LOGFC + "");
						matrix.save(OmicsData.PATH_STREAMING + data.getName() + OmicsData.EXTENSION);

						/*
						 * Save information in a table
						 */
						System.out.println(data.getName());
						String stat = matrix.getName() + "\t" + matrix.getNumberRow() + "\t";
						ExpressionMatrix filteredMatrix = ExpressionMatrixStat.filter(matrix, filterLogFC);
						stat += filteredMatrix.getNumberRow() + "\t";
						stat += ExpressionMatrixStat.min(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.median(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.mean(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.max(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += ExpressionMatrixStat.deviation(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
						stat += VectorUtils.quantiles(
								VectorUtils.deleteMissingValue(matrix.getColumn(matrix.getGenomeViewerColumnIndex())),
								100)[10] + "\t";
						stat += VectorUtils.quantiles(
								VectorUtils.deleteMissingValue(matrix.getColumn(matrix.getGenomeViewerColumnIndex())),
								100)[90] + "\t";
						stat += data.getType() + "";
						stats.add(stat);
					} else if (data.getType() == TypeData.RNASeq) {
						String fileNameRNASeq = OmicsData.PATH_NGS_NORM + bioConditionCompare.getName() + NGS.EXTENSION;
						File file = new File(fileNameRNASeq);
						if (file.exists()) {
							filterLogFC.setTableElementName(ColNames.LOGFC + "");
							ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileNameRNASeq, false);

							/*
							 * Normalize by the variance
							 */
							matrix = VarianceNormalization.norm(matrix, ColNames.LOGFC + "");
							matrix.saveTab(OmicsData.PATH_NGS_NORM + bioConditionCompare.getName() + NGS.EXTENSION,
									"gene");

							/*
							 * Save information in a table
							 */
							System.out.println(data.getName());
							String stat = matrix.getName() + "\t" + matrix.getNumberRow() + "\t";
							ExpressionMatrix filteredMatrix = ExpressionMatrixStat.filter(matrix, filterLogFC);
							stat += filteredMatrix.getNumberRow() + "\t";
							stat += ExpressionMatrixStat.min(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
							stat += ExpressionMatrixStat.median(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
							stat += ExpressionMatrixStat.mean(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
							stat += ExpressionMatrixStat.max(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
							stat += ExpressionMatrixStat.deviation(matrix, matrix.getGenomeViewerColumnIndex()) + "\t";
							stat += VectorUtils.quantiles(VectorUtils
									.deleteMissingValue(matrix.getColumn(matrix.getGenomeViewerColumnIndex())), 100)[10]
									+ "\t";
							stat += VectorUtils.quantiles(VectorUtils
									.deleteMissingValue(matrix.getColumn(matrix.getGenomeViewerColumnIndex())), 100)[90]
									+ "\t";
							stat += data.getType() + "";
							stats.add(stat);
						}
					}

				}
			}
		}
		TabDelimitedTableReader.saveList(stats, Database.getInstance().getPath() + "StatTranscriptomeNormed.txt");
	}

	/**
	 * Create Database.logFCMatrix by regrouping every logFC values for genes,
	 * sRNAs, asRNAs, and cisRegs in every <code>BioCondition</code> given
	 * <code>Experiment</code><br>
	 * 
	 * @param exp    list of BioCondition
	 * @param genome current genome with list of genes, sRNAs, asRNAs and cisRegs
	 */
	public static void createLogFCMatrix(Experiment exp, Genome genome) {
		/*
		 * Init comparison matrix: in headers we put all comparisons, in rownames all
		 * genes, srna and ASrna
		 */
		ExpressionMatrix logFCMatrix = new ExpressionMatrix();
		int i = 0;
		for (String genomeElement : genome.getAllElementNames()) {
			logFCMatrix.getRowNames().put(genomeElement, i);
			i++;
		}
		for (BioCondition bioCond : exp.getBioConditions()) {
			for (String comparison : bioCond.getComparisonNames()) {
				if (bioCond.getTypeDataContained().contains(TypeData.GeneExpr)) {
					logFCMatrix.addHeader(comparison + "_GE");
				} else {
					System.out.println(comparison);
					logFCMatrix.addHeader(comparison);
				}
			}
		}
		double[][] values = new double[logFCMatrix.getRowNames().size()][logFCMatrix.getHeaders().size()];
		logFCMatrix.setValues(values);

		/*
		 * Fill the matrix with GeneExpression values
		 */
		for (BioCondition bioCond : exp.getBioConditions()) {
			ArrayList<String> comparisonNames = bioCond.getComparisonNames();
			for (String comp : comparisonNames) {
				if (bioCond.getTypeDataContained().size() == 0
						|| bioCond.getTypeDataContained().contains(TypeData.ExpressionMatrix)) {
					String fileName = OmicsData.PATH_STREAMING + comp + OmicsData.EXTENSION;
					System.out.println("Load: " + fileName);
					ExpressionMatrix matrix = ExpressionMatrix.load(fileName);
					for (String gene : genome.getAllElementNames()) {
						if (matrix.getRowNames().containsKey(gene)) {
							logFCMatrix.setValue(matrix.getValue(gene, ColNames.LOGFC + ""), gene, comp);
						}
					}
				} else if (bioCond.getTypeDataContained().contains(TypeData.RNASeq)) {
					String fileNameRNASeq = OmicsData.PATH_NGS_NORM + comp + NGS.EXTENSION;
					System.out.println("Load: " + fileNameRNASeq);
					File file = new File(fileNameRNASeq);
					if (file.exists()) {
						ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileNameRNASeq, false);
						for (String gene : genome.getAllElementNames()) {
							if (matrix.getRowNames().containsKey(gene)) {
								logFCMatrix.setValue(matrix.getValue(gene, ColNames.LOGFC + ""), gene, comp);
							}
						}
					}
				} else if (bioCond.getTypeDataContained().contains(TypeData.GeneExpr)) {
					String fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp
							+ "_Gene_GEonly.txt";
					System.out.println("Load: " + fileName);
					File file = new File(fileName);
					if (file.exists()) {
						ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, true);
						String headerGE = "LOGFC_" + comp + GeneExpression.EXTENSION;
						for (String gene : genome.getAllElementNames()) {
							// System.out.println(header+" "+gene+" "+comp);
							if (matrix.getRowNames().containsKey(gene)) {
								logFCMatrix.setValue(matrix.getValue(gene, headerGE), gene, comp + "_GE");
							}
						}
					}
				} else if (bioCond.getTypeDataContained().contains(TypeData.Tiling)) {
					/**
					 * Go through all comparisons files for Tiling and add them to LogFC table
					 */
					String[] typeFiles = { "_Gene", "_Srna", "_ASrna.txt", "_CisReg.txt" };
					for (String typeFile : typeFiles) {
						String fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + typeFile
								+ ".txt";
						System.out.println("Load: " + fileName);
						File file = new File(fileName);
						if (file.exists()) {
							ExpressionMatrix matrixTiling = ExpressionMatrix.loadTab(fileName, true);
							String headerTiling = "LOGFC_" + comp + Tiling.EXTENSION;
							for (String gene : genome.getAllElementNames()) {
								// System.out.println(header+" "+gene+" "+comp);
								if (matrixTiling.getRowNames().containsKey(gene)) {
									if (matrixTiling.getHeaders().contains(headerTiling)) {
										double value = matrixTiling.getValue(gene, headerTiling);
										logFCMatrix.setValue(matrixTiling.getValue(gene, headerTiling), gene, comp);
									}
								}
							}
						}
					}
				}
			}
		}

		logFCMatrix.setAnnotations(new String[0][0]);
		logFCMatrix.getHeaderAnnotation().clear();
		logFCMatrix = Annotation.addAnnotationLite(logFCMatrix, genome);
		logFCMatrix.setName(
				FileUtils.removePath(Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies()));
		logFCMatrix.save(Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());
		logFCMatrix.saveTab(Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies() + ".excel",
				"Probes");
		System.out.println("Saved: " + Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());

	}

	/**
	 * Create Database.statTable by calculating stat values for genes, sRNAs, and
	 * asRNAs in every BioCondition<br>
	 * For genes we use FDRBY calculated from GebneExpression array<br>
	 * For sRNAs and asRNAs we use TSTUDENTTILING for Tiling array
	 * 
	 * @param exp
	 * @param genome
	 */
	public static void createStatMatrix(Experiment exp, Genome genome) {
		/*
		 * Init comparison matrix: in headers we put all comparisons, in rownames all
		 * genes, srna and ASrna
		 */
		ExpressionMatrix logStatTable = new ExpressionMatrix();
		int i = 0;
		for (String gene : genome.getFirstChromosome().getGenes().keySet()) {
			logStatTable.getRowNames().put(gene, i);
			i++;
		}
		for (String sRNA : genome.getFirstChromosome().getsRNAs().keySet()) {
			logStatTable.getRowNames().put(sRNA, i);
			i++;
		}
		for (String asRNA : genome.getFirstChromosome().getAsRNAs().keySet()) {
			logStatTable.getRowNames().put(asRNA, i);
			i++;
		}
		for (String cisReg : genome.getFirstChromosome().getCisRegs().keySet()) {
			logStatTable.getRowNames().put(cisReg, i);
			i++;
		}
		for (BioCondition bioCond : exp.getBioConditions()) {
			for (String comparison : bioCond.getComparisonNames()) {
				logStatTable.addHeader(comparison);
			}
		}
		double[][] values = new double[logStatTable.getRowNames().size()][logStatTable.getHeaders().size()];
		logStatTable.setValues(values);

		/*
		 * Fill the matrix with GeneExpression values
		 */
		for (BioCondition bioCond : exp.getBioConditions()) {
			ArrayList<String> comparisonNames = bioCond.getComparisonNames();
			for (String comp : comparisonNames) {
				String fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_Gene_GEonly.txt";
				String fileNameTiling = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_Gene.txt";
				System.out.println("load: " + fileName);
				ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, true);
				ExpressionMatrix matrixTiling = ExpressionMatrix.loadTab(fileNameTiling, true);
				if (matrix != null) {
					String headerGE = "STAT_" + comp + "_" + StatTest.TypeStat.FDRBY + GeneExpression.EXTENSION;
					String headerTiling = "STAT_" + comp + "_" + StatTest.TypeStat.TSTUDENTTILING + Tiling.EXTENSION;
					for (String gene : genome.getFirstChromosome().getGenes().keySet()) {
						// System.out.println(header+" "+gene+" "+comp);
						if (matrix.getRowNames().containsKey(gene)) {
							logStatTable.setValue(matrix.getValue(gene, headerGE), gene, comp);
						} else {
							if (matrixTiling.getHeaders().contains(headerTiling)) {
								logStatTable.setValue(matrixTiling.getValue(gene, headerTiling), gene, comp);
							}
						}
					}
				}
				/*
				 * sRNA
				 */
				fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_Srna.txt";
				System.out.println("load: " + fileName);
				matrix = ExpressionMatrix.loadTab(fileName, true);
				if (matrix != null) {
					String header = "STAT_" + comp + "_" + StatTest.TypeStat.TSTUDENTTILING + Tiling.EXTENSION;
					for (String sRNA : genome.getFirstChromosome().getsRNAs().keySet()) {
						logStatTable.setValue(matrix.getValue(sRNA, header), sRNA, comp);
					}
				}
				/*
				 * asRNA
				 */
				fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_ASrna.txt";
				System.out.println("load: " + fileName);
				matrix = ExpressionMatrix.loadTab(fileName, true);
				if (matrix != null) {
					String header = "STAT_" + comp + "_" + StatTest.TypeStat.TSTUDENTTILING + Tiling.EXTENSION;
					for (String asRNA : genome.getFirstChromosome().getAsRNAs().keySet()) {
						logStatTable.setValue(matrix.getValue(asRNA, header), asRNA, comp);
					}
				}
				/*
				 * cisRegs
				 */
				fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_CisReg.txt";
				System.out.println("load: " + fileName);
				matrix = ExpressionMatrix.loadTab(fileName, true);
				if (matrix != null) {
					String header = "STAT_" + comp + "_" + StatTest.TypeStat.TSTUDENTTILING + Tiling.EXTENSION;
					for (String cisReg : genome.getFirstChromosome().getCisRegs().keySet()) {
						logStatTable.setValue(matrix.getValue(cisReg, header), cisReg, comp);
					}
				}
			}
		}

		// logStatTable.setAnnotations(new String[0][0]);
		// logStatTable.getHeaderAnnotation().clear();
		// logStatTable = Annotation.addAnnotation(logStatTable,
		// Genome.loadEgdeGenome());
		logStatTable.save(Database.getSTAT_MATRIX_TRANSCRIPTOMES_PATH());
		logStatTable.saveTab(Database.getSTAT_MATRIX_TRANSCRIPTOMES_PATH() + ".txt", "Gene");
	}

	/**
	 * Get all GeneExpression from the lab
	 * 
	 * @return list of {@link GeneExpression}
	 */
	public static ArrayList<GeneExpression> getDataFromLab() {
		/*
		 * Get only the GeneExpression data
		 */
		ArrayList<GeneExpression> geneExprs = new ArrayList<>();
		for (BioCondition bioCond : BioCondition.getAllBioConditions()) {
			for (String bioCond2Name : bioCond.getComparisons()) {
				BioCondition bioCond2 = BioCondition.getBioCondition(bioCond2Name);
				BioCondition bioConditionCompare = bioCond.compare(bioCond2, false);
				for (OmicsData data : bioConditionCompare.getTranscriptomesData()) {
					if (data instanceof GeneExpression) {
						GeneExpression transcriptome = (GeneExpression) data;
						geneExprs.add(transcriptome);
					}
				}
			}
		}
		return geneExprs;
	}

	/**
	 * Look at the Expression of HouseKeeping genes<br>
	 * 
	 * @param exp
	 */
	private static void qualityControl(Experiment exp) {
		Database.initDatabase(Database.LISTERIOMICS_PROJECT);
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
		allTiling = ExpressionMatrix.loadTab(Expression.PATH_ALLTILING + "_WT.txt", true);
		allTiling = allTiling.getSubMatrixRow(hkGenes.getElements());
		allTiling.saveTab(Database.getTEMP_PATH() + "TilingHouseKeeping_WT_QC.txt", "r");

		/*
		 * Then execute in bacnet.rcp.test.test.runPostInit():
		 * HeatMapView.displayMatrix(ExpressionMatrix.loadTab(Project.getTEMP_PATH()+
		 * "GeneExprHouseKeeping_QC.txt", true), "GeneExprHouseKeeping_QC");
		 * HeatMapView.displayMatrix(ExpressionMatrix.loadTab(Project.getTEMP_PATH()+
		 * "TilingHouseKeeping_QC.txt", true), "TilingHouseKeeping_QC");
		 */

	}
}
