package bacnet.scripts.listeriomics.technology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TestUtils;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.arrayexpress.ArrayExpress;
import bacnet.scripts.arrayexpress.ArrayExpressDataUtils;
import bacnet.scripts.arrayexpress.ArrayExpressTechnology;
import bacnet.scripts.core.normalization.MedianNormalization;
import bacnet.utils.ArrayUtils;
import bacnet.utils.ExpressionMatrixStat;
import bacnet.utils.MathUtils;
import bacnet.utils.VectorUtils;

public class Agilent {



    public static void extractProbes(String technoID, String arrayExpressId, String geneColumnName) {

        /**
         * Create an HashMap between probeID and GeneId
         */
        HashMap<String, String> probeToGene = new HashMap<>();
        String[][] technoIDArray =
                TabDelimitedTableReader.read(ArrayExpressTechnology.PATH + technoID + ".adfTable.txt");
        for (int i = 1; i < technoIDArray.length; i++) {
            probeToGene.put(technoIDArray[i][ArrayUtils.findColumn(technoIDArray, "Reporter Name")],
                    technoIDArray[i][ArrayUtils.findColumn(technoIDArray, geneColumnName)]);
        }

        ArrayList<String> bioCondNames = new ArrayList<>();
        for (String bioCond : BioCondition.getAllBioConditionNames()) {
            // if(bioCond.contains("LO28")) bioCondNames.add(bioCond); // for E-GEOD-32434
            // if(bioCond.contains("J0161")) bioCondNames.add(bioCond); // for E-GEOD-27936
            if (bioCond.contains("Agilent_2013"))
                bioCondNames.add(bioCond); // for E-GEOD-43052
        }


        for (String bioCondName : bioCondNames) {
            System.out.println(bioCondName);
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
            int k = 0;
            ExpressionMatrix matrixFinal = new ExpressionMatrix();

            for (ExpressionMatrix matrixTemp : bioCond.getMatrices()) {
                if (matrixTemp.getRawDatas().size() == 1) {
                    matrixFinal = ExpressionMatrix.loadTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator
                            + matrixTemp.getRawDatas().get(0), true);
                    matrixFinal = MedianNormalization.norm(matrixFinal);
                    matrixFinal.getHeaders().clear();
                    matrixFinal.getHeaders().add("VALUE");
                    matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt",
                            "Probe");
                } else {
                    for (String rawDataName : matrixTemp.getRawDatas()) {
                        System.out.println(rawDataName);
                        matrixTemp = ExpressionMatrix.loadTab(
                                ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + rawDataName, true);
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
                    matrixFinal = ExpressionMatrix.merge(matrices, true);

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
                    matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt",
                            "Probe");
                }
            }


            /*
             * Replace now the probeId by the GeneID
             */
            String[][] arrayData = TabDelimitedTableReader
                    .read(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt");
            for (int i = 1; i < arrayData.length; i++) {
                String probe = arrayData[i][0];
                if (probeToGene.containsKey(probe)) {
                    arrayData[i][0] = probeToGene.get(probe);
                } else {
                    System.out.println("could not find probe: " + probe);
                }
            }
            TabDelimitedTableReader.save(arrayData,
                    ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt");

            /*
             * Remove duplicate probes
             */
            ExpressionMatrix matrixBioCond = ArrayExpress
                    .curateMatrix(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", false);
            matrixBioCond.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", "Probe");
        }
    }

    /**
     * Extract Probes from Matrix when its a Comparison
     * 
     * @param technoID
     * @param arrayExpressId
     * @param comparison
     */
    public static void extractProbesComparison(String technoID, String arrayExpressId) {

        /*
         * Read Comparison and extract the data fileName of the corresponding data
         */
        String[][] array = TabDelimitedTableReader.read(Database.getInstance().getExperimentComparisonTablePath());
        TreeSet<String> comparisons = new TreeSet<>();
        HashMap<String, ArrayList<String>> comparisonToFileName = new HashMap<>();


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


        /**
         * For each Comparison, look at the different corresponding data
         */
        for (int i = 0; i < array.length; i++) {
            String comparison = array[i][ArrayUtils.findColumn(array, "BioCondName")] + " vs "
                    + array[i][ArrayUtils.findColumn(array, "RefBioCondName")];
            String fileName = array[i][ArrayUtils.findColumn(array, "FileName")];
            String arrayExpressIdTemp = array[i][ArrayUtils.findColumn(array, "ArrayExpressId")];
            if (arrayExpressIdTemp.equals(arrayExpressId)) {

                if (!comparisonToFileName.containsKey(comparison)) {
                    ArrayList<String> fileNames = new ArrayList<>();
                    fileNames.add(fileName);
                    comparisonToFileName.put(comparison, fileNames);
                } else {
                    comparisonToFileName.get(comparison).add(fileName);
                }
            }
        }



        /**
         * Go through the HashMap Comparisons and combine data in one file for each comparison<br>
         * Median of LogFC is calculated and will be used for display
         */
        for (String comparison : comparisonToFileName.keySet()) {
            ArrayList<String> fileNames = comparisonToFileName.get(comparison);
            if (fileNames.size() == 1) {
                ExpressionMatrix matrix = ExpressionMatrix.loadTab(
                        ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + fileNames.get(0), false);
                matrix.getHeaders().clear();
                matrix.getHeaders().add("LOGFC");
                matrix.saveTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", "Probe");
            } else {
                /*
                 * Fusion all the dataFile
                 */
                ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
                int i = 0;
                for (String fileName : fileNames) {
                    System.out.println(comparison + "   " + fileName);
                    ExpressionMatrix matrixTemp = ExpressionMatrix
                            .loadTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + fileName, false);
                    matrixTemp.getHeaders().clear();
                    // We add only one header, so only the first column will be read
                    matrixTemp.getHeaders().add("Value_" + i);
                    i++;
                    // matrixTemp.saveTab(file.getAbsolutePath(), "Probe");
                    matrices.add(matrixTemp);
                    // matrixTemp.saveTab(ArrayExpressListeriomics.DATA_CURATED_PATH+comparison+"_"+i+".txt", "Probe");
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
                matrixFinal.addColumn("LOGFC", columnFC);
                matrixFinal.addColumn("STDEV", columnSTDev);


                /*
                 * Save the file with the name: comparison.txt
                 */
                matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", "Probe");
            }

            /*
             * Replace now the probeId by the GeneID
             */
            String[][] arrayData =
                    TabDelimitedTableReader.read(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt");
            for (int i = 1; i < arrayData.length; i++) {
                String probe = arrayData[i][0];
                if (probeToGene.containsKey(probe)) {
                    arrayData[i][0] = probeToGene.get(probe);
                } else {
                    System.out.println("could not find probe: " + probe);
                }
            }
            TabDelimitedTableReader.save(arrayData, ArrayExpress.DATA_CURATED_PATH + comparison + ".txt");

            /*
             * Remove duplicate probes
             */
            ExpressionMatrix matrixBioCond = ArrayExpress
                    .curateMatrix(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", false);
            matrixBioCond.saveTab(ArrayExpress.DATA_CURATED_PATH + comparison + ".txt", "Probe");
        }
    }

    /**
     * Calculate Log of Fold Change (mean difference) for each value, and p-value
     * 
     * @param technoID
     * @param arrayExpressId
     */
    public static void calculateComparison(String technoID, String arrayExpressId, boolean ttest) {
        /*
         * Create comparison Matrix
         */
        ArrayList<String> bioCondNames = new ArrayList<>();
        for (String bioCond : BioCondition.getAllBioConditionNames()) {
            // if(bioCond.contains("Agilent_2012")) bioCondNames.add(bioCond); // for E-GEOD-32434
            // if(bioCond.contains("J0161")) bioCondNames.add(bioCond); // for E-GEOD-27936
            if (bioCond.contains("Agilent_2013"))
                bioCondNames.add(bioCond); // for E-GEOD-43052
        }

        for (String bioCondName : bioCondNames) {
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            if (bioCond.getComparisons().size() != 0) {
                BioCondition refBioCond = BioCondition.getBioCondition(bioCond.getComparisons().get(0));
                ExpressionMatrix expr1 = ExpressionMatrix
                        .loadTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", false);
                ExpressionMatrix expr2 = ExpressionMatrix
                        .loadTab(ArrayExpress.DATA_CURATED_PATH + refBioCond.getName() + ".txt", false);
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
                if (ttest) {
                    double[] columntTest = new double[exprFoldChange.getNumberRow()];
                    for (String rowName : exprFoldChange.getRowNames().keySet()) {
                        double[] leftValue = {expr1.getValue(rowName, "Value_0"), expr1.getValue(rowName, "Value_1"),
                                expr1.getValue(rowName, "Value_2")};
                        double[] rightValue = {expr2.getValue(rowName, "Value_0"), expr2.getValue(rowName, "Value_1"),
                                expr2.getValue(rowName, "Value_2")};
                        try {
                            columntTest[exprFoldChange.getRowNames().get(rowName)] =
                                    TestUtils.tTest(leftValue, rightValue);
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (MathException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    exprFoldChange.addColumn("p-value", columntTest);
                }

                /*
                 * Save ExpressionMatrix
                 */
                exprFoldChange.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + " vs "
                        + refBioCond.getName() + ".txt", "Probes");
            }
        }
    }

    /**
     * Data are not Log transformed:<br>
     * <li>add the min value to be sure to have only positive values
     * <li>log2 transform
     * 
     * @param technoID
     * @param arrayExpressId
     * @param geneColumnName
     */
    public static void normalize(String technoID, String arrayExpressId, String geneColumnName) {

        ArrayList<String> bioCondNames = new ArrayList<>();
        for (String bioCond : BioCondition.getAllBioConditionNames()) {
            if (bioCond.contains("Agilent_2013"))
                bioCondNames.add(bioCond); // for E-GEOD-43052
        }

        for (String bioCondName : bioCondNames) {
            System.out.println(bioCondName);
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            for (ExpressionMatrix matrixTemp : bioCond.getMatrices()) {
                for (String rawDataNameTemp : matrixTemp.getRawDatas()) {
                    String rawDataName = rawDataNameTemp.replace("_curated", "");
                    System.out.println(rawDataName);
                    matrixTemp = ExpressionMatrix
                            .loadTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + rawDataName, true);
                    double min = ExpressionMatrixStat.min(matrixTemp, 0);
                    for (String rowName : matrixTemp.getRowNames().keySet()) {
                        double value = matrixTemp.getValue(rowName, "VALUE");
                        value = value - min + 1; // avoid negative numbers (due to background soustraction)
                        value = MathUtils.log2(value);
                        matrixTemp.setValue(value, rowName, "VALUE");
                    }
                    matrixTemp.saveTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + rawDataNameTemp,
                            "Probe");
                }
            }
        }
    }

}
