package bacnet.scripts.listeriomics.technology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TestUtils;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.arrayexpress.ArrayExpress;
import bacnet.scripts.arrayexpress.ArrayExpressDataUtils;
import bacnet.scripts.arrayexpress.ArrayExpressTechnology;
import bacnet.scripts.core.normalization.MedianNormalization;
import bacnet.utils.ArrayUtils;
import bacnet.utils.ExpressionMatrixStat;
import bacnet.utils.VectorUtils;

public class Febit {

    public static void extractProbes(String technoID, String arrayExpressId) {

        /**
         * Create an HashMap between probeID and GeneId
         */
        HashMap<String, String> probeToGene = new HashMap<>();
        String[][] technoIDArray =
                TabDelimitedTableReader.read(ArrayExpressTechnology.PATH + technoID + ".adfTable.txt");
        for (int i = 1; i < technoIDArray.length; i++) {
            probeToGene.put(technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Reporter Name")],
                    technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Comment[ORF]")]);
        }

        String[] bioCondNames = {"4C_120_Febit_2013", "4C_60_Febit_2013", "4C_30_Febit_2013", "4C_0_Febit_2013"};
        for (String bioCondName : bioCondNames) {
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
            int k = 0;
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
            }
            ExpressionMatrix matrixFinal = ExpressionMatrix.merge(matrices, true);
            /*
             * Calculate median and standard deviation in the column LOGFC
             */
            double[] columnFC = new double[matrixFinal.getNumberRow()];
            double[] columnSTDev = new double[matrixFinal.getNumberRow()];
            for (String rowName : matrixFinal.getRowNames().keySet()) {
                double[] row = matrixFinal.getRow(rowName);
                columnFC[matrixFinal.getRowNames().get(rowName)] =
                        VectorUtils.median(VectorUtils.deleteMissingValue(row));
                columnSTDev[matrixFinal.getRowNames().get(rowName)] =
                        VectorUtils.deviation(VectorUtils.deleteMissingValue(row));
            }
            matrixFinal.addColumn("VALUE", columnFC);
            matrixFinal.addColumn("STDEV", columnSTDev);
            matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", "Probe");
            System.out.println();

            /*
             * Replace now the probeId by the GeneID
             */
            String[][] arrayData =
                    TabDelimitedTableReader.read(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt");
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
            ExpressionMatrix matrixBioCond =
                    ArrayExpress.curateMatrix(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", false);
            matrixBioCond.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", "Probe");

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
        String[] bioCondNames = {"4C_120_Febit_2013", "4C_60_Febit_2013", "4C_30_Febit_2013"};
        for (String bioCondName : bioCondNames) {
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            BioCondition refBioCond = BioCondition.getBioCondition(bioCond.getComparisons().get(0));
            ExpressionMatrix expr1 =
                    ExpressionMatrix.loadTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", false);
            ExpressionMatrix expr2 =
                    ExpressionMatrix.loadTab(ArrayExpress.DATA_CURATED_PATH + refBioCond.getName() + ".txt", false);
            ArrayList<String> includColumn = new ArrayList<>();
            includColumn.add("VALUE");
            ExpressionMatrix expr1Minus = expr1.getSubMatrixColumn(includColumn);
            ExpressionMatrix expr2Minus = expr2.getSubMatrixColumn(includColumn);
            ExpressionMatrix exprFoldChange = ExpressionMatrixStat.minus(expr1Minus, expr2Minus);
            exprFoldChange.getHeaders().clear();
            exprFoldChange.getHeaders().add("LOGFC");

            /*
             * Calculate t-test
             */
            double[] columntTest = new double[exprFoldChange.getNumberRow()];
            for (String rowName : exprFoldChange.getRowNames().keySet()) {
                double[] leftValue = {expr1.getValue(rowName, "Value_0"), expr1.getValue(rowName, "Value_1"),
                        expr1.getValue(rowName, "Value_2")};
                double[] rightValue = {expr2.getValue(rowName, "Value_0"), expr2.getValue(rowName, "Value_1"),
                        expr2.getValue(rowName, "Value_2")};
                try {
                    columntTest[exprFoldChange.getRowNames().get(rowName)] = TestUtils.tTest(leftValue, rightValue);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MathException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            exprFoldChange.addColumn("p-value", columntTest);
            exprFoldChange.saveTab(
                    ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + " vs " + refBioCond.getName() + ".txt",
                    "Probes");

        }
    }

    /**
     * In some column Gene locus name is missing so we add it using EGDe genome and GenBank ID of each
     * gene
     * 
     * @param technoID
     */
    public static void curateTechnoTable(String technoID) {

        Genome genome = Genome.loadEgdeGenome();
        String[][] technoIDArray =
                TabDelimitedTableReader.read(ArrayExpressTechnology.PATH + technoID + ".adfTable.txt");
        for (int i = 1; i < technoIDArray.length; i++) {
            String locus = technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Comment[ORF]")];
            String description = technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Comment[Product Name]")];
            String genBank =
                    technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Reporter Database Entry [genbank]")];
            if (locus.equals("")) {
                System.out.println(genBank + "  " + description);
                for (Gene gene : genome.getFirstChromosome().getGenes().values()) {
                    String genBankGene = gene.getProtein_id();
                    if (genBankGene.equals(genBank)) {
                        System.out.println("found " + genBankGene + "  " + gene.getComment());
                        technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Comment[ORF]")] = gene.getName();
                    }
                }
            }
        }
        TabDelimitedTableReader.save(technoIDArray, ArrayExpressTechnology.PATH + technoID + ".adfTable.txt");

    }
}
