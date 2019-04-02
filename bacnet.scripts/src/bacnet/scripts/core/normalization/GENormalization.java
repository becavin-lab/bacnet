package bacnet.scripts.core.normalization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import bacnet.Database;
import bacnet.datamodel.dataset.EGDeWTdata;
import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.core.Rscript;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

public class GENormalization {

    public static void norm(Experiment exp) throws IOException {

        /*
         * run one thread for each mutant tiling data normalization The number of thread is limited by the
         * number of processor
         */

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int i = 0;
        for (BioCondition bioCondTemp : exp.getBioConds().values()) {
            if (bioCondTemp.getGeneExprs().size() != 0) {
                final BioCondition bioCond = bioCondTemp;
                // create temp folder
                final String tempPath = Database.getTEMP_PATH() + bioCond.getName();
                File path = new File(tempPath);
                path.mkdir();
                // copy L_monocytogenes.cdf in this folder
                FileUtils.copy(Rscript.getRscriptDirectory() + "L_monocytogenes.cdf",
                        tempPath + File.separator + "L_monocytogenes.cdf");
                // run normalization*
                if (!bioCond.getName().equals("EGDe_DNA") && !bioCond.getName().equals("EGDe_37C_Mean")
                        && !bioCond.getName().equals("EGDe_37C_Deviation")) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String finalPath = ExpressionData.PATH_GENEXPR_COMPLETE;
                                String tempFileName = bioCond.getName();
                                String rScript = tempPath + File.separator + tempFileName + "-script.R";
                                String listFilesPath = tempPath + File.separator + tempFileName + "-targets.txt";

                                ArrayList<String> rawData = bioCond.getGeneExprs().get(0).getRawDatas();
                                if (rawData.size() < 3) {
                                    System.out.println("size" + rawData.size() + " " + tempFileName);
                                }

                                createTargetsTable(bioCond, listFilesPath);
                                createRnormScript(listFilesPath, finalPath, tempFileName, rScript);

                                // System.err.println("^^^^^^^^^^^" + bioCond.getName());
                                // if(bioCond.getName().equals("rli32")){
                                Rscript.run(rScript);
                                // }


                                // System.out.println("Finish thread");
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    };
                    i++;
                    executor.execute(runnable);
                }
            }
        }
        System.err.println("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
        System.err.println("dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
        System.err.println("d");
        System.err.println("               Number of threads run: " + i + "  expected numb of data " + i);
        System.err.println("d");
        System.err.println("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
        System.err.println("dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Interrupted exception");
        }
        System.err.println("All GeneExpression data normalized");

    }

    private static void createRnormScript(String listFilesPath, String finalPath, String tempFileName, String rScript) {

        // System.err.println("Create R script for normalization");


        String text = "# path where to save GE file\n";
        text += "finalFilepath = \"" + finalPath + tempFileName + ".ge\"\n";
        text += "# path where all scripts are\n";
        text += "scriptDir = \"" + Rscript.getRscriptDirectory() + "\"\n";
        text += "# path of the table containing the list of files to normalize\n";
        text += "manipfile = \"" + listFilesPath + "\"\n";

        text += "# temp file\n";
        text += "tempDir = \"" + Database.getTEMP_PATH() + tempFileName + "\"\n";

        text += "\n# run the script\n";
        text += "source(paste(scriptDir,\"GeneExpressionNormalization.R\",sep=\"\"))\n";
        text += "GeneExpressionNormalization(finalFilepath,scriptDir,manipfile,tempDir)\n";

        text = text.replaceAll("\\\\", "/");
        // System.out.println(text);
        FileUtils.saveText(text, rScript);
    }

    public static void createTargetsTable(BioCondition bioCond, String fileName) {
        ArrayList<String> rawData = bioCond.getGeneExprs().get(0).getRawDatas();
        // System.out.println(bioCond.getName());
        /*
         * For reference Wild Type data, bioCond.getComparisons() is void. Here the normalization calculate
         * also LPE test and FDR, so we need to prive a Data to compare to, no matter which one it is, it
         * will not affect the final normalization has only expression value will be extracted
         */
        if (bioCond.getName().equals(OmicsData.GENERAL_WT_DATA)
                || bioCond.getName().equals(OmicsData.GENERAL_WT_NAME)) {
            bioCond.getComparisons().add("EGDe_010910");
        }
        BioCondition bioCondRef = BioCondition.getBioCondition(bioCond.getComparisons().get(0));
        ArrayList<String> rawDataWT = bioCondRef.getGeneExprs().get(0).getRawDatas();
        // System.out.println(rawData.size() + " WT "+rawDataWT.size()+" "+fileName);

        String[][] targets = new String[(rawData.size() + rawDataWT.size() + 1)][3];
        targets[0][0] = "Name";
        targets[0][1] = "FileName";
        targets[0][2] = "Target";
        int i = 1;
        for (String rawDataName : rawDataWT) {
            targets[i][0] = FileUtils.removeExtensionAndPath(ExpressionData.PATH_GENEXPR_RAW + rawDataName);
            targets[i][1] = (ExpressionData.PATH_GENEXPR_RAW + rawDataName).replaceAll("\\\\", "/");;
            targets[i][2] = "Egde";
            i++;
        }
        for (String rawDataName : rawData) {
            targets[i][0] = FileUtils.removeExtensionAndPath(ExpressionData.PATH_GENEXPR_RAW + rawDataName);
            targets[i][1] = (ExpressionData.PATH_GENEXPR_RAW + rawDataName).replaceAll("\\\\", "/");;
            targets[i][2] = bioCond.getName();
            i++;
        }

        TabDelimitedTableReader.save(targets, fileName);
    }

    /**
     * Extract information contained in all .ge files of the experiment
     * 
     * @param experiment
     */
    public static void extractInformation(Experiment experiment) {
        // ExecutorService executor = Executors.newFixedThreadPool(1);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (BioCondition bioCondTemp : experiment.getBioConds().values()) {
            final BioCondition bioCond = bioCondTemp;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        extractInformation(bioCond);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
            executor.execute(runnable);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Interrupted exception");
        }
        System.out.println("All .ge data converted");
    }


    /**
     * Load .ge files resulting from R script normalization and extract the important columns = column
     * 0-4 = log Expression values and median expression
     * 
     * @param bioCond
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void extractInformation(BioCondition bioCond) throws FileNotFoundException, IOException {
        if (!bioCond.getName().equals(ExpressionData.GENERAL_WT_NAME)
                && !bioCond.getName().equals(EGDeWTdata.NAME_Mean)) {
            if (bioCond.getGeneExprs().size() != 0) {
                String fileName = ExpressionData.PATH_GENEXPR_COMPLETE + bioCond.getGeneExprs().get(0).getName();
                System.out.println(fileName + "  " + bioCond.getGeneExprs().size());
                String[][] array = TabDelimitedTableReader.read(fileName);
                // delete first column
                array = ArrayUtils.deleteColumn(array, 0);
                // delete row containing "_x" because its a duplicate of another row
                int k = -1;
                for (int i = 1; i < array.length; i++) {
                    if (array[i][0].contains("_x"))
                        k = i;
                }
                if (k != -1)
                    array = ArrayUtils.deleteRow(array, k);
                // delete all "_at" and "_s" suffix in the gene name put by Affymetrix
                for (int i = 1; i < array.length; i++) {
                    array[i][0] = array[i][0].replaceFirst("_at", "");
                    array[i][0] = array[i][0].replaceFirst("_s", "");
                    array[i][0] = array[i][0].replaceAll("\"", "");
                }
                // transform to ExpressionMatrix
                ExpressionMatrix geneExpMatrix = ExpressionMatrix.arrayToExpressionMatrix(array, true);

                // Create two ExpressionMatrix to put the results in
                ExpressionMatrix exprMatrix = new ExpressionMatrix();

                exprMatrix.setRowNames(geneExpMatrix.getRowNames());
                exprMatrix.setValues(geneExpMatrix.getRowNames().size(), 5);
                // extract information from column 0-4
                for (int i = 0; i < 5; i++) {
                    String header = geneExpMatrix.getHeader(i);
                    exprMatrix.addHeader(header);
                    System.out.println(header);
                    for (String probe : exprMatrix.getRowNames().keySet()) {
                        double value = geneExpMatrix.getValue(probe, header);
                        exprMatrix.setValue(value, probe, header);
                    }
                }

                // save exprMatrix
                exprMatrix.saveTab(ExpressionData.PATH_GENEXPR_NORM + bioCond.getGeneExprs().get(0).getName(),
                        "probes");
            }
        } else {
            // if GENERAL_WT_NAME extract data from GENERAL_WT_DATA
            String fileName =
                    ExpressionData.PATH_GENEXPR_COMPLETE + ExpressionData.GENERAL_WT_DATA + GeneExpression.EXTENSION;
            System.out.println(fileName + "  " + bioCond.getGeneExprs().size());
            String[][] array = TabDelimitedTableReader.read(fileName);
            // delete first column
            array = ArrayUtils.deleteColumn(array, 0);
            // delete row containing "_x" because its a duplicate of another row
            int k = -1;
            for (int i = 1; i < array.length; i++) {
                if (array[i][0].contains("_x"))
                    k = i;
            }
            if (k != -1)
                array = ArrayUtils.deleteRow(array, k);
            // delete all "_at" and "_s" suffix in the gene name put by Affymetrix
            for (int i = 1; i < array.length; i++) {
                array[i][0] = array[i][0].replaceFirst("_at", "");
                array[i][0] = array[i][0].replaceFirst("_s", "");
                array[i][0] = array[i][0].replaceAll("\"", "");
            }
            ExpressionMatrix geneExpMatrix = ExpressionMatrix.arrayToExpressionMatrix(array, true);

            // Create two ExpressionMatrix to put the results in
            ExpressionMatrix exprMatrix = new ExpressionMatrix();

            exprMatrix.setRowNames(geneExpMatrix.getRowNames());
            exprMatrix.setValues(geneExpMatrix.getRowNames().size(), 5);
            // extract information from column 0-4
            for (int i = 0; i < 5; i++) {
                String header = geneExpMatrix.getHeader(i);
                exprMatrix.addHeader(header);
                System.out.println(header);
                for (String probe : exprMatrix.getRowNames().keySet()) {
                    double value = geneExpMatrix.getValue(probe, header);
                    exprMatrix.setValue(value, probe, header);
                }
            }

            // save exprMatrix
            exprMatrix.saveTab(
                    ExpressionData.PATH_GENEXPR_NORM + ExpressionData.GENERAL_WT_NAME + GeneExpression.EXTENSION,
                    "probes");
        }

    }

    /**
     * Check temp folder, and GenExprComplete folder to see if some GeneExpression are not present in
     * GeneExprComplete
     */
    public static void qualityControlNorm() {
        File file = new File(Database.getTEMP_PATH());
        String[] files = file.list();
        File fileNorm = new File(ExpressionData.PATH_GENEXPR_COMPLETE);
        TreeSet<String> found = new TreeSet<String>();
        for (String fileTemp : files) {
            for (String fileNormTemp : fileNorm.list()) {
                if (fileTemp.equals(FileUtils.removeExtension(fileNormTemp))) {
                    found.add(fileTemp);
                }
            }
        }
        System.out.println(found.size() + " data normalized");
        for (String fileTemp : files) {
            if (!found.contains(fileTemp)) {
                System.out.println("Not created: " + fileTemp);
            }
        }

    }

    /**
     * Check temp folder, and GenExprComplete folder to see if some GeneExpression are not present in
     * GeneExprComplete
     */
    public static void qualityControlExtract() {
        File file = new File(ExpressionData.PATH_GENEXPR_COMPLETE);
        String[] files = file.list();
        File fileNorm = new File(ExpressionData.PATH_GENEXPR_NORM);
        TreeSet<String> found = new TreeSet<String>();
        for (String fileTemp : files) {
            for (String fileNormTemp : fileNorm.list()) {
                if (FileUtils.removeExtension(fileTemp).equals(FileUtils.removeExtension(fileNormTemp))) {
                    found.add(FileUtils.removeExtension(fileTemp));
                }
            }
        }
        System.out.println(found.size() + " data extracted");
        for (String fileTemp : files) {
            if (!found.contains(FileUtils.removeExtension(fileTemp))) {
                System.out.println("Not created: " + fileTemp);
            }
        }

    }

}
