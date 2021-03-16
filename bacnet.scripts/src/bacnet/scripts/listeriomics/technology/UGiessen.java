package bacnet.scripts.listeriomics.technology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TestUtils;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.arrayexpress.ArrayExpress;
import bacnet.scripts.arrayexpress.ArrayExpressDataUtils;
import bacnet.scripts.core.normalization.MedianNormalization;
import bacnet.utils.ArrayUtils;
import bacnet.utils.ExpressionMatrixStat;
import bacnet.utils.MathUtils;
import bacnet.utils.VectorUtils;

public class UGiessen {

    public static void extractProbes(String technoID, String arrayExpressId) {

        ArrayList<String> bioCondNames = new ArrayList<>();
        for (String bioCond : BioCondition.getAllBioConditionNames()) {
            if (bioCond.contains("Mouse_Macrophages_"))
                bioCondNames.add(bioCond); // for E-MEXP-1947
        }

        for (String bioCondName : bioCondNames) {
            System.out.println(bioCondName);
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
            int k = 0;
            ExpressionMatrix matrixFinal = new ExpressionMatrix();

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
                matrixFinal.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", "Probe");

            }

            /*
             * Remove duplicate probes
             */
            ExpressionMatrix matrixBioCond =
                    ArrayExpress.curateMatrix(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", false);
            matrixBioCond.saveTab(ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + ".txt", "Probe");
        }
    }

    /**
     * Delete all first 4 columns of each raw data
     * 
     * @param technoID
     * @param arrayExpressId
     */
    public static void curateRawData(String technoID, String arrayExpressId) {

        ArrayList<String> bioCondNames = new ArrayList<>();
        for (String bioCond : BioCondition.getAllBioConditionNames()) {
            if (bioCond.contains("Mouse_Macrophages_"))
                bioCondNames.add(bioCond); // for E-MEXP-1947
        }

        for (String bioCondName : bioCondNames) {
            System.out.println(bioCondName);
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            for (ExpressionMatrix matrixTemp : bioCond.getMatrices()) {
                for (String rawDataNameTemp : matrixTemp.getRawDatas()) {
                    String rawDataName = rawDataNameTemp.replace("_curated", "");
                    System.out.println(rawDataName);
                    String[][] rawData = TabDelimitedTableReader
                            .read(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + rawDataName);
                    rawData = ArrayUtils.deleteColumn(rawData, 0);
                    rawData = ArrayUtils.deleteColumn(rawData, 0);
                    rawData = ArrayUtils.deleteColumn(rawData, 0);
                    rawData = ArrayUtils.deleteColumn(rawData, 0);
                    TabDelimitedTableReader.save(rawData,
                            ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + rawDataNameTemp);
                }
            }
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
            if (bioCond.contains("Mouse_Macrophages_"))
                bioCondNames.add(bioCond); // for E-MEXP-1947
        }

        for (String bioCondName : bioCondNames) {
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
            if (bioCond.getComparisons().size() != 0) {
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
                exprFoldChange.saveTab(
                        ArrayExpress.DATA_CURATED_PATH + bioCond.getName() + " vs " + refBioCond.getName() + ".txt",
                        "Probes");
            }
        }
    }

    public static void combineMatrices(String technoID, String arrayExpressId) {
        /*
         * Read Comparison and extract the data fileName of the corresponding data
         */
        String[][] array = TabDelimitedTableReader.read(Database.getInstance().getExperimentComparisonTablePath());
        HashMap<String, ArrayList<String>> comparisonToFileName = new HashMap<>();

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
            /*
             * Fusion all the dataFile
             */
            ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
            int i = 0;
            for (String fileName : fileNames) {
                System.out.println(comparison + "   " + fileName);
                ExpressionMatrix matrixTemp = ExpressionMatrix
                        .loadTab(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + fileName, false);
                ArrayList<String> keepColumn = new ArrayList<>();
                keepColumn.add("LOGFC");
                matrixTemp = matrixTemp.getSubMatrixColumn(keepColumn);
                matrixTemp.getHeaders().clear();
                // We add only one header, so only the first column will be read
                matrixTemp.getHeaders().add("LogFc_" + i);
                i++;
                // matrixTemp.saveTab(file.getAbsolutePath(), "Probe");
                matrices.add(matrixTemp);
                // matrixTemp.saveTab(ArrayExpressListeriomics.DATA_CURATED_PATH+comparison+"_"+i+".txt",
                // "Probe");
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

    }

    /**
     * Open every processed files and extract the different signal from it. <br>
     * Create one file for each array
     */
    public static void processAllData() {

        String[] datas = {"E-MEXP-1118", "E-MEXP-1144", "E-MEXP-1162", "E-MEXP-1170", "E-TABM-663"};

        /**
         * Go through all technology
         */
        for (String data : datas) {
            File folder = new File(ArrayExpressDataUtils.PATH + data + File.separator);
            for (File file : folder.listFiles()) {
                if (file.getAbsolutePath().contains("processed-data")) {
                    System.out.println(file.getAbsolutePath());
                    String[][] array = TabDelimitedTableReader.read(file);
                    String[] rowNames = ArrayUtils.getColumn(array, 0);
                    String[] firstColumn = new String[0];
                    String[] secondColumn = new String[0];

                    for (int j = 1; j < array[0].length; j++) {
                        firstColumn = ArrayUtils.getColumn(array, j);
                        j++;
                        secondColumn = ArrayUtils.getColumn(array, j);
                        if (!firstColumn[0].equals(secondColumn[0])) {
                            System.out.println("Columns first and second have not the same header: " + firstColumn[0]
                                    + " vs " + secondColumn[0]);
                        }

                        /*
                         * Combine columns together
                         */
                        String[][] finalArray = new String[array.length - 1][3];
                        String name = firstColumn[0];
                        String header1 = firstColumn[1].split(":")[1];
                        String header2 = secondColumn[1].split(":")[1];
                        finalArray[0][0] = "Probes";
                        finalArray[0][1] = header1;
                        finalArray[0][2] = header2;
                        for (int i = 1; i < finalArray.length; i++) {
                            finalArray[i][0] = rowNames[i + 1];
                            /*
                             * Copy and log transform the columns
                             */
                            if (firstColumn[i + 1].equals("NA") || secondColumn[i + 1].equals("NA")
                                    || firstColumn[i + 1].equals("null") || secondColumn[i + 1].equals("null")) {
                                finalArray[i][1] = OmicsData.MISSING_VALUE + "";
                                finalArray[i][2] = OmicsData.MISSING_VALUE + "";
                            } else {
                                finalArray[i][1] = MathUtils.log2(Double.parseDouble(firstColumn[i + 1])) + "";
                                finalArray[i][2] = MathUtils.log2(Double.parseDouble(secondColumn[i + 1])) + "";
                            }

                        }
                        TabDelimitedTableReader.save(finalArray,
                                ArrayExpressDataUtils.PATH + data + File.separator + name + ".txt");

                        /*
                         * Median normalize
                         */
                        ExpressionMatrix matrix = ExpressionMatrix
                                .loadTab(ArrayExpressDataUtils.PATH + data + File.separator + name + ".txt", false);
                        matrix = MedianNormalization.norm(matrix);
                        matrix.saveTab(ArrayExpressDataUtils.PATH + data + File.separator + name + ".txt", "Probe");

                        /*
                         * Calculate LOGFC (Cy3 - Cy5) LogFC First column - Second Column
                         */
                        double[] columnFC = new double[matrix.getNumberRow()];
                        for (String rowName : matrix.getRowNames().keySet()) {
                            double value1 = matrix.getValue(rowName, matrix.getHeaders().get(0));
                            double value2 = matrix.getValue(rowName, matrix.getHeaders().get(1));
                            if (value1 == OmicsData.MISSING_VALUE || value2 == OmicsData.MISSING_VALUE) {
                                columnFC[matrix.getRowNames().get(rowName)] = OmicsData.MISSING_VALUE;
                            } else {
                                columnFC[matrix.getRowNames().get(rowName)] = value1 - value2;
                            }
                        }
                        matrix.addColumn("LOGFC", columnFC);
                        matrix.saveTab(ArrayExpressDataUtils.PATH + data + File.separator + name + ".txt", "Probe");

                    }

                }
            }

        }
    }
}
