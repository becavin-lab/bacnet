package bacnet.scripts.database;

import java.io.File;
import java.util.ArrayList;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.core.normalization.MedianNormalization;
import bacnet.utils.ArrayUtils;
import bacnet.utils.MathUtils;
import bacnet.utils.VectorUtils;

public class NGSCreation {

    /**
     * Go through all RNASeq data found in the list <code>BioCondition.getRNASeqBioConditions()</code>
     * and convert all of them
     */
    public static void convertCoverageFiles(Experiment exp, boolean logTransformed) {
        /*
         * Convert and save RNASeq data
         */
        for (BioCondition bioCond : exp.getBioConditions()) {
            for (NGS data : bioCond.getNGSSeqs()) {
                // data.load();
                data.convert(data, logTransformed);
                if (!logTransformed) {
                    data.setLogTransformed(logTransformed);
                }
                data.setBioCondName(bioCond.getName());
                data.save(false);
            }
        }

        /*
         * Calculate comparison data for vizualization, and organize diff expression matrices
         */
        /*
        for (BioCondition bioCond : exp.getBioConditions()) {
            for (NGS data : bioCond.getNGSSeqs()) {
                if (data.getType() == TypeData.RNASeq) {
                    System.out.println(bioCond.getName());
                    for (String bioCondName2 : bioCond.getComparisons()) {
                        BioCondition compBioCond = bioCond.compare(BioCondition.getBioCondition(bioCondName2), true);
                        for (NGS data2 : compBioCond.getNGSSeqs()) {
                            data2.save(true);
                            System.out.println("saved: " + data.getName());
                        }
                    }
                }
            }
        }*/

    }

    /**
     * ConvertCountFile and add them to the database
     * 
     * @param exp
     */
    public static void normalizeCountFiles(Experiment exp) {
        for (BioCondition bioCond : exp.getBioConditions()) {
            if(bioCond.getTypeDataContained().contains(TypeData.RNASeq)) {
                for (String comparison : bioCond.getComparisonDataNames()) {
                    String fileName = OmicsData.PATH_NGS_NORM + comparison + NGS.EXTENSION;
                    System.out.println("Search "+fileName);
                    File file = new File(fileName);
                    if (file.exists()) {
                        System.out.println("Modify: " + fileName);
                        String[][] array = TabDelimitedTableReader.read(fileName);

                        // small RNA names have to be changed from "rli11_sbrA" to "rli11 - sbrA"
                        for (int i = 0; i < array.length; i++) {
                            String name = array[i][0];
                            if (name.contains("_")) {
                                if (name.contains("rli")) {
                                    name = name.replaceFirst("_", " - ");
                                } else if (name.equals("Cobalamin_2")) {
                                    name = "Cobalamin 2";
                                }
                                array[i][0] = name;
                            }
                        }

                        ExpressionMatrix matrix = ExpressionMatrix.arrayToExpressionMatrix(array, true);
                        //matrix.getHeaders().clear();
                        //matrix.getHeaders().add("LOGFC");
                        //matrix.getHeaders().add("p-value");
                        //matrix.saveTab(OmicsData.PATH_NGS_NORM + compBioCond.getName() + NGS.EXTENSION,"GenomeElements");
                        matrix.setName(comparison);
                        matrix.setBioCondName(comparison);
                        matrix.save(OmicsData.PATH_STREAMING + comparison + OmicsData.EXTENSION);
                        
                    }
                }
            }
        }

        /*
         * Process expression matrix
         */
        /*
        for (BioCondition bioCond : exp.getBioConditions()) {
            if (bioCond.getTypeDataContained().contains(TypeData.RNASeq)) {
                int nbDuplicate = bioCond.getNGSSeqs().get(0).getRawDatas().size();
                System.out.println(bioCond.getName() + " dupl:" + nbDuplicate);
                if (nbDuplicate == 1) {
                    String fileName = OmicsData.PATH_NGS_NORM + bioCond.getName() + NGS.EXTENSION;
                    File file = new File(fileName);
                    if (file.exists()) {
                        String[][] array = TabDelimitedTableReader.read(new File(fileName), ",");

                        // small RNA names have to be changed from "rli11_sbrA" to "rli11 - sbrA"
                        for (int i = 0; i < array.length; i++) {
                            String name = array[i][0];
                            if (name.contains("_")) {
                                if (name.contains("rli")) {
                                    name = name.replaceFirst("_", " - ");
                                } else if (name.equals("Cobalamin_2")) {
                                    name = "Cobalamin 2";
                                }
                                array[i][0] = name;
                            }
                        }

                        ExpressionMatrix matrix = ExpressionMatrix.arrayToExpressionMatrix(array, true);
                        matrix.getHeaders().clear();
                        matrix.getHeaders().add("UNLOGVALUE");
                        double[] logValues = new double[matrix.getNumberRow()];
                        for (String rowName : matrix.getRowNames().keySet()) {
                            int index = matrix.getRowNames().get(rowName);
                            double value = matrix.getValue(rowName, "UNLOGVALUE");
                            double logvalue = MathUtils.log2(value);
                            if (value < 1)
                                logvalue = 1;
                            logValues[index] = logvalue;
                        }
                        matrix.addColumn("VALUE", logValues);
                        matrix = MedianNormalization.norm(matrix, "VALUE");
                        matrix.saveTab(OmicsData.PATH_NGS_NORM + bioCond.getName() + NGS.EXTENSION, "GenomeElements");
                    }
                } else {
                    ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
                    for (int i = 1; i < (nbDuplicate + 1); i++) {
                        String fileName = OmicsData.PATH_NGS_NORM + bioCond.getName() + "_" + i + NGS.EXTENSION;
                        File file = new File(fileName);
                        if (file.exists()) {
                            String[][] array = TabDelimitedTableReader.read(new File(fileName), ",");

                            // small RNA names have to be changed from "rli11_sbrA" to "rli11 - sbrA"
                            for (int k = 0; k < array.length; k++) {
                                String name = array[k][0];
                                if (name.contains("_")) {
                                    if (name.contains("rli")) {
                                        name = name.replaceFirst("_", " - ");
                                    } else if (name.equals("Cobalamin_2")) {
                                        name = "Cobalamin 2";
                                    }
                                    array[k][0] = name;
                                }
                            }

                            ExpressionMatrix matrix = ExpressionMatrix.arrayToExpressionMatrix(array, true);
                            matrix.getHeaders().clear();
                            matrix.getHeaders().add("VALUE_" + i);
                            matrices.add(matrix);
                        }
                    }
                    if (matrices.size() != 0) {
                        ExpressionMatrix matrix = ExpressionMatrix.merge(matrices, false);
                        double[] medianS = new double[matrix.getNumberRow()];
                        double[] logMedians = new double[matrix.getNumberRow()];
                        for (String rowName : matrix.getRowNames().keySet()) {
                            int index = matrix.getRowNames().get(rowName);
                            double median = VectorUtils.median(ArrayUtils.getRow(matrix.getValues(), index));
                            double logMedian = MathUtils.log2(median);
                            if (median < 1)
                                logMedian = 1;
                            medianS[index] = median;
                            logMedians[index] = logMedian;
                        }
                        matrix.addColumn("UNLOGVALUE", medianS);
                        matrix.addColumn("VALUE", logMedians);
                        matrix = MedianNormalization.norm(matrix, "VALUE");
                        matrix.saveTab(OmicsData.PATH_NGS_NORM + bioCond.getName() + NGS.EXTENSION, "GenomeElements");
                    }
                }
            }
        }*/
    }

}
