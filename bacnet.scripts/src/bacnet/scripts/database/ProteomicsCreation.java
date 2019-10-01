package bacnet.scripts.database;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.ColNames;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.ProteomicsData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import bacnet.utils.MathUtils;

public class ProteomicsCreation {

    public static String DATA_TABLE = Database.getInstance().getPath() + "Proteomics-Listeria mono-15102014.txt";

    /**
     * Clean proteomics tables, and convert them for the database
     * 
     * @param bioConds
     */
    public static void addProteomesToDatabase(ArrayList<String> bioConds) {
        Experiment exp = new Experiment();
        for (String bioCond : bioConds) {
            exp.addBioCond(BioCondition.getBioCondition(bioCond));
        }
        ArrayList<String> genomeList = BioCondition.getProteomeGenomes();
        for (String genomeName : genomeList) {
            Experiment expTemp = new Experiment();
            for (BioCondition bioCond : exp.getBioConditions()) {
                if (bioCond.getGenomeName().equals(genomeName)) {
                    expTemp.addBioCond(bioCond);
                }
            }
            /*
             * Summarize all comparisons in matrices
             */
            Genome genome = Genome.loadGenome(genomeName);
            addMissingValuesToMatrices(expTemp, genome);
            convertProteomicsData(expTemp, genome);
        }
    }

    /**
     * Convert all ExpressionMatrix found for Proteomic datasets and save them in Streaming folder
     * 
     * @param exp
     * @param genome
     */
    public static void convertProteomicsData(Experiment exp, Genome genome) {
        ArrayList<BioCondition> massSpecBioCond = new ArrayList<>();
        for (BioCondition bioCond : exp.getBioConditions()) {
            if (bioCond.getTypeDataContained().contains(TypeData.NTerm)
                    || bioCond.getTypeDataContained().contains(TypeData.Proteome)) {
                massSpecBioCond.add(bioCond);
            }
        }

        ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
        for (BioCondition bioCondition : massSpecBioCond) {
            System.out.println(bioCondition.getName());
            if (bioCondition.getTypeDataContained().contains(TypeData.Proteome)) {
                /*
                 * Read data
                 */
                for (ProteomicsData proteome : bioCondition.getProteomes()) {
                    String fileName = OmicsData.PATH_PROTEOMICS_NORM + proteome.getRawDatas().get(0);
                    // String fileName = OmicsData.PATH_PROTEOMICS_NORM + proteome.getName() +
                    // ".txt";
                    File file = new File(fileName);
                    if (file.exists()) {
                        ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, false);
                        System.out.println("Convert: " + fileName);
                        /*
                         * Select only the VALUE column
                         */
                        ArrayList<String> includeColNames = new ArrayList<>();
                        includeColNames.add(ColNames.VALUE + "");
                        if (!matrix.getHeaders().contains(ColNames.VALUE + "")) {
                            System.out.println("No " + ColNames.VALUE + " column found for " + fileName);
                        }
                        matrix = matrix.getSubMatrixColumn(includeColNames);

                        /*
                         * Not used = log transformation
                         */
//                         int nbProteins = 0;
//                         for (int i = 0; i < matrix.getValues().length; i++) {
//	                         for (int j = 0; j < matrix.getValues()[0].length; j++) {
//		                         if (!(matrix.getValues()[i][j] == OmicsData.MISSING_VALUE) && (matrix.getValues()[i][j] != 0)) {
//			                         matrix.getValues()[i][j] = MathUtils.log2(matrix.getValues()[i][j]);
//			                         matrix.getValues()[i][j] = 1;
//		                         }
//	                         }
//                         }
                        
                        /*
                         * Save expressionMatrix
                         */
                        proteome.setHeaders(matrix.getHeaders());
                        proteome.setRowNames(matrix.getRowNames());
                        proteome.setValues(matrix.getValues());
                        System.out.println(
                                "Save " + OmicsData.PATH_STREAMING + proteome.getName() + ProteomicsData.EXTENSION);
                        proteome.save(OmicsData.PATH_STREAMING + proteome.getName() + ProteomicsData.EXTENSION);
                    }
                }

                for (String comparisonName : bioCondition.getComparisonNames()) {
                    System.out.println("comp: " + comparisonName);
                    String fileName = OmicsData.PATH_PROTEOMICS_NORM + comparisonName + ".txt";
                    File file = new File(fileName);
                    if (file.exists()) {
                        ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, false);
                        /*
                         * Select only the LOGFC column
                         */
                        ArrayList<String> includeColNames = new ArrayList<>();
                        includeColNames.add(ColNames.LOGFC + "");
                        if (!matrix.getHeaders().contains(ColNames.LOGFC + "")) {
                            System.out.println("No " + ColNames.LOGFC + " column found for " + fileName);
                        }
                        matrix = matrix.getSubMatrixColumn(includeColNames);

                        /*
                         * Save expressionMatrix
                         */
                        ProteomicsData proteome = new ProteomicsData();
                        proteome.setName(comparisonName);
                        proteome.setHeaders(matrix.getHeaders());
                        proteome.setRowNames(matrix.getRowNames());
                        proteome.setValues(matrix.getValues());
                        proteome.save(OmicsData.PATH_STREAMING + proteome.getName() + ProteomicsData.EXTENSION);

                    }
                }

            } else if (bioCondition.getTypeDataContained().contains(TypeData.NTerm)) {
                String fileName = OmicsData.PATH_PROTEOMICS_NORM + bioCondition.getName() + ".txt";
                ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, false);
                ArrayList<String> includeColNames = new ArrayList<>();
                includeColNames.add(ColNames.VALUE + "");
                matrix = matrix.getSubMatrixColumn(includeColNames);
                /*
                 * Add to list for fusion matrix
                 */
                matrix.getHeaders().clear();
                matrix.getHeaders().add(bioCondition.getName());
                matrices.add(matrix);

            }

        }
    }

    /**
     * In order to represent proteomics data we need to add a -1 value in every missing row.<br>
     * 
     * @param exp
     * @param genome
     */
    public static void addMissingValuesToMatrices(Experiment exp, Genome genome) {
        ArrayList<BioCondition> massSpecBioCond = new ArrayList<>();
        for (BioCondition bioCond : exp.getBioConditions()) {
            if (bioCond.getTypeDataContained().contains(TypeData.NTerm)
                    || bioCond.getTypeDataContained().contains(TypeData.Proteome)) {
                massSpecBioCond.add(bioCond);
            }
        }
        for (BioCondition bioCondition : massSpecBioCond) {
            System.out.println(bioCondition.getName());
            if (bioCondition.getName().equals("Trypsin_2013_LI0521_EGDe")) {
                System.out.println();
            }
            if (bioCondition.getTypeDataContained().contains(TypeData.Proteome)) {
                /*
                 * Read data
                 */
                for (ProteomicsData proteome : bioCondition.getProteomes()) {
                    String fileName = OmicsData.PATH_PROTEOMICS_RAW + proteome.getRawDatas().get(0);
                    ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, false);
                    for (String geneName : genome.getAllElementNames()) {
                        if (!matrix.getRowNames().keySet().contains(geneName)) {
                            // System.out.println("gene:" + geneName);
                        	double[] newLine = new double[matrix.getHeaders().size()];
                        	for(int i=0;i<newLine.length;i++) {
                        		newLine[i] = -1;
                        	}
                        	matrix.addRow(geneName, newLine);
                        }
                    }
                    String fileNameNew = OmicsData.PATH_PROTEOMICS_NORM + proteome.getRawDatas().get(0);
                    matrix.saveTab(fileNameNew, ColNames.GenomeElements + "");
                }

                for (String comparisonName : bioCondition.getComparisonNames()) {
                    System.out.println("comp: " + comparisonName);
                    String fileName = OmicsData.PATH_PROTEOMICS_RAW + comparisonName + ".txt";
                    File file = new File(fileName);
                    if (file.exists()) {
                        ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, false);
                        for (String geneName : genome.getAllElementNames()) {
                            if (!matrix.getRowNames().keySet().contains(geneName)) {
                                // System.out.println("gene:" + geneName);
                                if (matrix.getHeaders().size() == 3) {
                                    double[] values = {0, 1, 1};
                                    matrix.addRow(geneName, values);
                                } else if (matrix.getHeaders().size() == 2) {
                                    double[] values = {0, 1};
                                    matrix.addRow(geneName, values);
                                } else {
                                    matrix.addRow(geneName, new double[matrix.getHeaders().size()]);
                                }
                            }
                        }
                        String fileNameNew = OmicsData.PATH_PROTEOMICS_NORM + comparisonName + ".txt";
                        matrix.saveTab(fileNameNew, ColNames.GenomeElements + "");
                    }
                }
            }
        }
    }

    /**
     * Create Expression summary matrix for proteomics datasets
     * 
     * @param bioConds
     * @param logs
     * @return the logs of the creation
     */
    public static String createProteomeTable(ArrayList<String> bioConds, String logs) {
        /*
         * List biological conditions to add
         */
        Experiment exp = new Experiment();
        for (String bioCond : bioConds) {
            exp.addBioCond(BioCondition.getBioCondition(bioCond));
        }

        /*
         * Create one protein table per Genome
         */
        logs += "Create Protein table\n";
        ArrayList<String> genomeList = BioCondition.getProteomeGenomes();
        for (String genomeName : genomeList) {
            Experiment expTemp = new Experiment();
            for (BioCondition bioCond : exp.getBioConditions()) {
                if (bioCond.getGenomeName().equals(genomeName)) {
                    if (bioCond.getProteomicsData().size() > 0) {
                        expTemp.addBioCond(bioCond);
                    }
                }
            }

            /*
             * Deal with Expr values
             */
            summarizeProteomes(expTemp, genomeName);
            /*
             * Deal with fold change
             * UNUSED
             */
            //createLogFCMatrix(expTemp, genomeName);

        }
        logs += Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + " tables created";
        return logs;
    }

    /**
     * Create a table will all protein expression for each Genome
     * 
     * @param exp
     * @param genome
     */
    public static void summarizeProteomes(Experiment exp, String genomeName) {
        Genome genome = Genome.loadGenome(genomeName);
        ArrayList<BioCondition> massSpecBioCond = new ArrayList<>();
        for (BioCondition bioCond : exp.getBioConditions()) {
            if (bioCond.getTypeDataContained().contains(TypeData.NTerm)
                    || bioCond.getTypeDataContained().contains(TypeData.Proteome)) {
                massSpecBioCond.add(bioCond);
            }
        }

        String[][] arrayProteomics = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
        ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
        for (BioCondition bioCondition : massSpecBioCond) {
            System.out.println(bioCondition.getName());

            if (bioCondition.getTypeDataContained().contains(TypeData.Proteome)) {
                /*
                 * Read data
                 */
                for (ProteomicsData proteome : bioCondition.getProteomes()) {
                    proteome.load();
                    int nbProteins = 0;
                    for (int i = 0; i < proteome.getValues().length; i++) {
                        for (int j = 0; j < proteome.getValues()[0].length; j++) {
                            if (!(proteome.getValues()[i][j] == OmicsData.MISSING_VALUE)
                                    && (proteome.getValues()[i][j] != 0)) {
                                nbProteins++;
                            }
                        }
                    }

                    /*
                     * Update proteomics table
                     */
                    for (int i = 1; i < arrayProteomics.length; i++) {
                        String dataName = arrayProteomics[i][ArrayUtils.findColumn(arrayProteomics, "Data Name")];
                        if (dataName.equals(bioCondition.getName())) {
                            // System.out.println(matrix.getRowNames().size());
                            arrayProteomics[i][ArrayUtils.findColumn(arrayProteomics, "Nb proteins")] = nbProteins + "";
                        }
                    }
                    TabDelimitedTableReader.save(arrayProteomics, Database.getInstance().getProteomesArrayPath());
                    /*
                     * Add to list for fusion matrix
                     */
                    proteome.getHeaders().clear();
                    proteome.getHeaders().add(proteome.getName());
                    matrices.add(proteome);
                }

            } else if (bioCondition.getTypeDataContained().contains(TypeData.NTerm)) {
                String fileName = OmicsData.PATH_PROTEOMICS_NORM + bioCondition.getName() + ".txt";
                ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, false);
                ArrayList<String> includeColNames = new ArrayList<>();
                includeColNames.add("VALUE");
                matrix = matrix.getSubMatrixColumn(includeColNames);
                /*
                 * Update proteomics table
                 */
                for (int i = 1; i < arrayProteomics.length; i++) {
                    String dataName = arrayProteomics[i][ArrayUtils.findColumn(arrayProteomics, "Data Name")];
                    if (dataName.equals(bioCondition.getName())) {
                        arrayProteomics[i][ArrayUtils.findColumn(arrayProteomics, "Nb proteins")] =
                                matrix.getRowNames().size() + "";
                    }
                }
                TabDelimitedTableReader.save(arrayProteomics, Database.getInstance().getProteomesArrayPath());
                /*
                 * Add to list for fusion matrix
                 */
                matrix.getHeaders().clear();
                matrix.getHeaders().add(bioCondition.getName());
                matrices.add(matrix);

            }

        }
        ExpressionMatrix absoluteValue = ExpressionMatrix.merge(matrices, false);
        absoluteValue.setAnnotations(new String[0][0]);
        absoluteValue.getHeaderAnnotation().clear();
        absoluteValue = Annotation.addAnnotationLite(absoluteValue, genome);
        absoluteValue.setName(
                FileUtils.removePath(Database.getEXPRESSION_MATRIX_PROTEOMES_PATH() + "_" + genome.getSpecies()));

        System.out.println("Save " + Database.getEXPRESSION_MATRIX_PROTEOMES_PATH() + "_" + genome.getSpecies());
        absoluteValue.saveTab(Database.getEXPRESSION_MATRIX_PROTEOMES_PATH() + "_" + genome.getSpecies() + ".excel",
                "Probes");
        absoluteValue.save(Database.getEXPRESSION_MATRIX_PROTEOMES_PATH() + "_" + genome.getSpecies());
    }

    /**
     * Create a table will all protein expression for each Genome
     * 
     * @param exp
     * @param genome
     */
    public static void createLogFCMatrix(Experiment exp, String genomeName) {
        /*
         * Init comparison matrix: in headers we put all comparisons, in rownames all genes, srna and ASrna
         */
        Genome genome = Genome.loadGenome(genomeName);
        ExpressionMatrix logFCMatrix = new ExpressionMatrix();
        int i = 0;
        for (String genomeElement : genome.getAllElementNames()) {
            logFCMatrix.getRowNames().put(genomeElement, i);
            i++;
        }
        for (BioCondition bioCond : exp.getBioConditions()) {
            for (String comparison : bioCond.getComparisonNames()) {
                System.out.println(comparison);
                logFCMatrix.addHeader(comparison);
            }
        }
        double[][] values = new double[logFCMatrix.getRowNames().size()][logFCMatrix.getHeaders().size()];
        logFCMatrix.setValues(values);

        /*
         * Fill the matrix with GeneExpression values
         */
        for (BioCondition bioCond : exp.getBioConditions()) {
            ArrayList<String> comparisonNames = bioCond.getComparisonNames();
            for (String comparisonName : comparisonNames) {
                ProteomicsData proteome = new ProteomicsData();
                proteome.setName(comparisonName);
                proteome.load();
                for (String gene : genome.getAllElementNames()) {
                    if (proteome.getRowNames().containsKey(gene)) {
                        logFCMatrix.setValue(proteome.getValue(gene, ColNames.LOGFC + ""), gene, comparisonName);
                    }
                }
            }
        }

        logFCMatrix.setAnnotations(new String[0][0]);
        logFCMatrix.getHeaderAnnotation().clear();
        logFCMatrix = Annotation.addAnnotationLite(logFCMatrix, genome);
        logFCMatrix
                .setName(FileUtils.removePath(Database.getLOGFC_MATRIX_PROTEOMES_PATH() + "_" + genome.getSpecies()));
        logFCMatrix.save(Database.getLOGFC_MATRIX_PROTEOMES_PATH() + "_" + genome.getSpecies());
        logFCMatrix.saveTab(Database.getLOGFC_MATRIX_PROTEOMES_PATH() + "_" + genome.getSpecies() + ".excel", "Probes");
        System.out.println("Saved: " + Database.getLOGFC_MATRIX_PROTEOMES_PATH() + "_" + genome.getSpecies());

    }

    /**
     * DATA_TABLe has been created from a copy paste of ArrayExpress search results. From the web^page
     * we create the table, curated it and convert it to HTML Read DATA_TABLE and create an HTML table
     * from it to ease accession to ArrayExpress info
     */
    public static void createHTMLPageSummary() {
        String[][] array = TabDelimitedTableReader.read(DATA_TABLE);
        for (int i = 1; i < array.length; i++) {
            for (int j = 0; j < array[0].length - 2; j++) {
                String cell = array[i][j];
                if (cell.contains("http")) {
                    cell = "<a href=\"" + cell + "\">" + cell + "</a>";
                }
                array[i][j] = cell;
            }

            array[i][1] = "<a href=\"" + array[i][array[0].length - 1] + "\">" + array[i][1] + "</a>";
        }
        array = ArrayUtils.deleteColumn(array, array[0].length - 1);
        array = ArrayUtils.deleteColumn(array, array[0].length - 1);
        TabDelimitedTableReader.saveInHTML(array, Database.getInstance().getPath() + "ListProteomes.html",
                "Listeria Proteomics data");
    }

    /**
     * Create a matrix from TIS data for which each gene has its corresponding number of spectra
     */
    public static void createMatrixFromTISData() {
        for (BioCondition bioCondition : BioCondition.getAllBioConditions()) {
            if (bioCondition.getTypeDataContained().contains(TypeData.NTerm)) {
                TreeSet<String> geneFound = new TreeSet<>();
                NTermData massSpec = NTermData.load(bioCondition.getName());
                System.out.println(bioCondition.getName());
                System.out.println(massSpec.getName());
                for (NTerm nTerm : massSpec.getNTerms()) {
                    if (nTerm.getTypeOverlap().contains("aTIS") || nTerm.getTypeOverlap().contains("annotated TIS")) {
                        geneFound.add(nTerm.getOverlap().split(":")[0].trim());
                        System.out.println(nTerm.getOverlap().split(":")[0].trim());
                    }
                }

                ExpressionMatrix matrix = new ExpressionMatrix("VALUE", geneFound.size());
                int i = 0;
                for (String gene : geneFound) {
                    matrix.getRowNames().put(gene, i);
                    i++;
                }
                for (NTerm nTerm : massSpec.getNTerms()) {
                    if (nTerm.getTypeOverlap().contains("aTIS") || nTerm.getTypeOverlap().contains("annotated TIS")) {
                        String gene = nTerm.getOverlap().split(":")[0].trim();
                        double value = nTerm.getSpectra();
                        matrix.setValue(matrix.getValue(gene, "VALUE") + value, gene, "VALUE");
                    }
                }
                matrix.saveTab(OmicsData.PATH_PROTEOMICS_RAW + massSpec.getName() + ".txt", "Probes");
            }
        }
    }

}
