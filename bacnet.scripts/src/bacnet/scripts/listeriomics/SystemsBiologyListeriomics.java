package bacnet.scripts.listeriomics;

import java.io.File;
import java.io.IOException;

import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.Network;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Genome;
import bacnet.scripts.genome.CircularGenomeJPanel;
import bacnet.utils.FileUtils;
import bacnet.utils.VectorUtils;
import ca.ualberta.stothard.cgview.CgviewIO;

/**
 * List of method to create Co-expression network and analyse it
 * 
 * @author christophebecavin
 *
 */
public class SystemsBiologyListeriomics {

	public static void run() {

		/*
		 * Get the list of data
		 */
		// Experiment geneExp = Experiment.getGeneralExp();
		// TreeSet<String> list = new TreeSet<String>();
		// for(BioCondition bioCond : geneExp.getBioConditions()){
		// if(bioCond.getReference().contains("Unpublished")){
		// //list.add(bioCond.getName());
		// }else{
		// if(bioCond.getTypeDataContained().contains(TypeData.Tiling)){
		// list.add(bioCond.getName()+"["+TypeData.Tiling+"]");
		// }
		// if(bioCond.getTypeDataContained().contains(TypeData.RNASeq)){
		// String name = bioCond.getName();
		// if(name.contains("Long") || name.contains("Short")
		// ||name.contains("Medium") || name.contains("BHI_2011_EGDe")){
		// System.out.println("Not included");
		// }else{
		// list.add(bioCond.getName()+"["+TypeData.RNASeq+"]");
		// }
		// }
		// }
		// }
		// TabDelimitedTableReader.saveTreeSet(list,
		// Database.LISTDATA_COEXPR_NETWORK_TRANSCRIPTOMES_PATH+"_"+Genome.EGDE_NAME+".txt");
		//
		// /*
		// * Filter expression matrix : Need to first run
		// Expression.summarize(expTemp,Genome.loadGenome(genome));
		// */
		// ExpressionMatrix expression =
		// ExpressionMatrix.loadTab(Expression.PATH_ALLDataType+"_"+Genome.EGDE_NAME+".excel",
		// false);
		// expression =
		// expression.getSubMatrixColumn(TabDelimitedTableReader.readList(Database.LISTDATA_COEXPR_NETWORK_TRANSCRIPTOMES_PATH+"_"+Genome.EGDE_NAME+".txt"));
		// expression.save(Database.COEXPR_NETWORK_TRANSCRIPTOMES_PATH+"_Temp_"+Genome.EGDE_NAME);
		// expression.saveTab(Database.COEXPR_NETWORK_TRANSCRIPTOMES_PATH+"_Temp_"+Genome.EGDE_NAME+".excel","GenomeElements");

		/*
		 * Compute Network
		 */
		Network.getCoExpressionGlobalMatrix(Genome.loadEgdeGenome());

		// createCircularGenomeView();
	}

	public static void runPostInit() {
		createCircularGenomeView();
	}

	/**
	 * Create background figure for circos graph<br>
	 * We use CGView to display a full circular genome
	 */
	private static void createCircularGenomeView() {
		Genome genome = Genome.loadEgdeGenome();
		CircularGenomeJPanel panel = new CircularGenomeJPanel(900, 900, genome, "Listeria Monocytogenes EGD-e");
		try {
			String fileTempPNG = "/Users/cbecavin/Documents/RNABindingProtein/Stat_Expression/NEW_CircularView.png";
			// String fileTempPNG =
			// FileUtils.removeExtension(Network.CIRCOS_BACK_PATH)+".png";
			System.out.println("file " + fileTempPNG);
			CgviewIO.writeToPNGFile(panel.getCgview(), fileTempPNG);
			// CgviewIO.writeToSVGFile(panel.getCgview(), Network.CIRCOS_BACK_PATH, false,
			// false);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	/**
	 * Calculate co-expression matrix from Database.LOGFC_MATRIX_TRANSCRIPTOMES_PATH
	 */
	@SuppressWarnings("unused")
	private static void createCoExpressionMatrixEGDeFromLOGFC(Experiment exp, Genome genome) {
		ExpressionMatrix logFCMatrix = new ExpressionMatrix();
		int i = 0;
		for (String gene : genome.getGenes().keySet()) {
			logFCMatrix.getRowNames().put(gene, i);
			i++;
		}
		for (String sRNA : genome.getsRNAs().keySet()) {
			logFCMatrix.getRowNames().put(sRNA, i);
			i++;
		}
		for (String asRNA : genome.getAsRNAs().keySet()) {
			logFCMatrix.getRowNames().put(asRNA, i);
			i++;
		}
		for (String cisReg : genome.getCisRegs().keySet()) {
			logFCMatrix.getRowNames().put(cisReg, i);
			i++;
		}

		for (BioCondition bioCond : exp.getBioConditions()) {
			if (bioCond.getTypeDataContained().contains(TypeData.Tiling)) {
				for (String comparison : bioCond.getComparisonNames()) {
					logFCMatrix.addHeader(comparison);
				}
			}
		}
		double[][] values = new double[logFCMatrix.getRowNames().size()][logFCMatrix.getHeaders().size()];
		logFCMatrix.setValues(values);

		/*
		 * Fill the matrix with GeneExpression values
		 */
		for (String comp : logFCMatrix.getHeaders()) {
			/*
			 * Gene
			 */
			String fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_Gene_GEonly.txt";
			File file = new File(fileName);
			if (file.exists()) {
				ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, true);
				String headerGE = "LOGFC_" + comp + GeneExpression.EXTENSION;
				for (String gene : genome.getGenes().keySet()) {
					// System.out.println(header+" "+gene+" "+comp);
					if (matrix.getRowNames().containsKey(gene)) {
						logFCMatrix.setValue(matrix.getValue(gene, headerGE), gene, comp);
					}
				}
			} else {
				fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_Gene.txt";
				if (file.exists()) {
					ExpressionMatrix matrixTiling = ExpressionMatrix.loadTab(fileName, true);
					String headerTiling = "LOGFC_" + comp + Tiling.EXTENSION;
					for (String gene : genome.getGenes().keySet()) {
						// System.out.println(header+" "+gene+" "+comp);
						if (matrixTiling.getHeaders().contains(headerTiling)) {
							double value = matrixTiling.getValue(gene, headerTiling);
							logFCMatrix.setValue(matrixTiling.getValue(gene, headerTiling), gene, comp);
						}

					}
				}
			}
			/*
			 * sRNA
			 */
			fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_Srna.txt";
			System.out.println("load: " + fileName);
			ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, true);
			if (matrix != null) {
				String header = "LOGFC_" + comp + Tiling.EXTENSION;
				for (String sRNA : genome.getsRNAs().keySet()) {
					logFCMatrix.setValue(matrix.getValue(sRNA, header), sRNA, comp);
				}
			}
			/*
			 * asRNA
			 */
			fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_ASrna.txt";
			System.out.println("load: " + fileName);
			matrix = ExpressionMatrix.loadTab(fileName, true);
			if (matrix != null) {
				String header = "LOGFC_" + comp + Tiling.EXTENSION;
				for (String asRNA : genome.getAsRNAs().keySet()) {
					logFCMatrix.setValue(matrix.getValue(asRNA, header), asRNA, comp);
				}
			}
			/*
			 * cisRegs
			 */
			fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_CisReg.txt";
			System.out.println("load: " + fileName);
			matrix = ExpressionMatrix.loadTab(fileName, true);
			if (matrix != null) {
				String header = "LOGFC_" + comp + Tiling.EXTENSION;
				for (String cisReg : genome.getCisRegs().keySet()) {
					logFCMatrix.setValue(matrix.getValue(cisReg, header), cisReg, comp);
				}
			}
		}

		logFCMatrix.save(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_Temp_" + genome.getSpecies());
		logFCMatrix.saveTab(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_Temp_" + genome.getSpecies() + ".txt",
				"Probes");

		/*
		 * Create coexpressionMatrix
		 */
		Network.getCoExpressionGlobalMatrix(genome);
	}

	/**
	 * Calculate co-expression matrix from Database.LOGFC_MATRIX_TRANSCRIPTOMES_PATH
	 */
	@SuppressWarnings("unused")
	private static void createCoExpressionMatrix(Experiment exp, Genome genome) {
		ExpressionMatrix matrixLOGFC = ExpressionMatrix
				.load(Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());
		ExpressionMatrix coExpressionMatrix = new ExpressionMatrix();
		double[][] values = new double[matrixLOGFC.getRowNames().size()][matrixLOGFC.getRowNames().size()];
		coExpressionMatrix.setValues(values);
		int i = 0;
		for (String rowName : matrixLOGFC.getRowNames().keySet()) {
			coExpressionMatrix.getHeaders().add(rowName);
			coExpressionMatrix.getRowNames().put(rowName, i);
			i++;
		}

		for (String gene1 : coExpressionMatrix.getRowNames().keySet()) {
			System.out.println(gene1);
			for (String gene2 : coExpressionMatrix.getHeaders()) {
				double[] vector1 = matrixLOGFC.getRow(gene1);
				double[] vector2 = matrixLOGFC.getRow(gene2);
				double pearsonCorrelation = VectorUtils.pearsonCorrelation(vector1, vector2);
				if (Math.abs(pearsonCorrelation) > 0.7) {
					// System.out.println(pearsonCorrelation +" - "+gene1 +" - "+gene2);
					coExpressionMatrix.setValue(pearsonCorrelation, gene1, gene2);
					// coExpressionMatrix.setValue(-pearsonCorrelation, gene2, gene1);
				}
			}
		}
		coExpressionMatrix.setAnnotations(new String[0][0]);
		coExpressionMatrix.getHeaderAnnotation().clear();
		coExpressionMatrix = Annotation.addAnnotation(coExpressionMatrix, genome);
		coExpressionMatrix
				.setName(FileUtils.removePath(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + genome.getSpecies()));
		coExpressionMatrix.save(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + genome.getSpecies());
		coExpressionMatrix.saveTab(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + genome.getSpecies() + ".txt",
				"Probes");
	}

}
