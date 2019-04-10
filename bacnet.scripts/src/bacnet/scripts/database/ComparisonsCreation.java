package bacnet.scripts.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.stat.inference.TestUtils;
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.EGDeWTdata;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Operon;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Sequence.SeqType;
import bacnet.datamodel.sequence.Srna.TypeSrna;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.core.Comparison;
import bacnet.scripts.core.stat.FDR;
import bacnet.scripts.core.stat.LPE;
import bacnet.scripts.core.stat.StatTest;
import bacnet.scripts.core.stat.StatTest.TypeStat;
import bacnet.scripts.core.stat.StatUtils;
import bacnet.table.core.ColorMapper;
import bacnet.utils.ExpressionMatrixStat;
import bacnet.utils.FileUtils;
import bacnet.utils.Filter;

public class ComparisonsCreation {


    /**
     * Go through all comparisons and create for each <code>Tiling</code> and <code>GenExpression</code>
     * different matrices giving the number of genes, Srna and asRNA overexpressed and underexpressed
     * 
     * @param compList
     */
    public static void createAllCompMatrix(ArrayList<String> compList) {
        Genome genome = Genome.loadEgdeGenome();
        /*
         * create all GenomeElement lists
         */
        ArrayList<String> genes = new ArrayList<String>();
        // ArrayList<String> operons = new ArrayList<String>();
        ArrayList<String> sRNAs = new ArrayList<String>();
        ArrayList<String> asRNAs = new ArrayList<String>();
        ArrayList<String> ncRNAs = new ArrayList<String>();
        ArrayList<String> cisRegs = new ArrayList<String>();

        for (Chromosome chromo : genome.getChromosomes().values()) {
            for (String gene : chromo.getGenes().keySet())
                genes.add(gene);
            // for(String gene : genome.getChromosomes().get(0).getOperons().keySet()) operons.add(gene);
            for (String gene : chromo.getNcRNAs().keySet())
                ncRNAs.add(gene);
            for (String gene : chromo.getsRNAs().keySet())
                sRNAs.add(gene);
            for (String gene : chromo.getAsRNAs().keySet())
                asRNAs.add(gene);
            for (String gene : chromo.getCisRegs().keySet())
                cisRegs.add(gene);
        }
        // update report
        // String html =
        // FileUtils.readText(Project.getDATA_PATH()+"/ComparisonData/ComparisonReportRaw.html");
        // html = html.replaceAll("VnbGenes", genes.size()+"");
        // html = html.replaceAll("VnbBothGenes", genes.size()+"");
        // //html = html.replaceAll("VnbOperons", operons.size()+"");
        // //html = html.replaceAll("VnbncRNAs", ncRNAs.size()+"");
        // html = html.replaceAll("VnbSrnas", sRNAs.size()+"");
        // html = html.replaceAll("VnbASrnas", asRNAs.size()+"");
        // html = html.replaceAll("VnbCisRegs", cisRegs.size()+"");
        // FileUtils.saveText(html, Project.getDATA_PATH()+"ComparisonReport.html");


        /*
         * Create cutOff
         */
        Filter filterFDRBY = new Filter();
        filterFDRBY.setTypeFilter(Filter.TypeFilter.INFERIOR);
        filterFDRBY.setTableElementName(TypeStat.FDRBY + "");
        filterFDRBY.setCutOff1(0.05);
        Filter filterTtest = new Filter();
        filterTtest.setTypeFilter(Filter.TypeFilter.INFERIOR);
        filterTtest.setTableElementName(TypeStat.TSTUDENTTILING + "");
        filterTtest.setCutOff1(0.05);
        Filter filterLogFC = new Filter();
        filterLogFC.setTypeFilter(Filter.TypeFilter.SUPERIOR_ABS);
        filterLogFC.setTableElementName(TypeStat.LOGFC + "");
        filterLogFC.setCutOff1(1.5);

        /*
         * calculate allCompMatrix
         */
        for (String comparisonTemp : compList) {
            final String comparison = comparisonTemp;
            // create a folder for each comparison

            String path = OmicsData.PATH_COMPARISONS + comparison + File.separator;
            File file = new File(path);
            file.mkdir();

            ArrayList<Filter> filters = new ArrayList<Filter>();
            filters.add(filterLogFC);
            filters.add(filterFDRBY);
            /**
             * Genes
             */
            calcFilterAndSave(genes, SeqType.Gene + "", comparison, filters, true, true);
            calcFilterAndSave(genes, SeqType.Gene + "_GEonly", comparison, filters, false, true);
        }
    }

    /**
     * Calculate a comparison, filter the results, and save ExpressionMatrix and list of elements
     * up-regulated and down-regulated
     * 
     * @param elements
     * @param ID
     * @param comparison
     * @param filter
     * @param geneExpression
     */
    public static void calcFilterAndSave(ArrayList<String> elements, String ID, String comparison,
            ArrayList<Filter> filters, boolean tiling, boolean geneExpression) {
        /*
         * Calculate comparison
         */
        ArrayList<String> comparisons = new ArrayList<String>();
        String path = OmicsData.PATH_COMPARISONS + comparison + File.separator;
        comparisons.add(comparison);
        ExpressionMatrix compMatrix = calculateCompMatrix(comparisons, elements, tiling, geneExpression);
        if (compMatrix != null) {
            // System.out.println(ID+" "+comparison);
            Annotation.addAnnotation(compMatrix, Genome.loadEgdeGenome());
            compMatrix.saveTab(path + comparison + "_" + ID + ".txt", "Genome elements");
            // compMatrix.save(path+comparison+"_"+ID);
            ExpressionMatrix compMatrixFilter = compMatrix.clone();
            /*
             * Apply filter
             */
            for (Filter filter : filters) {
                if (filter.getTableElementName().contains(TypeStat.FDRBY + "")) {
                    filter.setTableElementName("STAT_" + comparison + "_" + TypeStat.FDRBY + Tiling.EXTENSION);
                    if (!compMatrix.getHeaders().contains(filter.getTableElementName()))
                        filter.setTableElementName(
                                "STAT_" + comparison + "_" + TypeStat.FDRBY + GeneExpression.EXTENSION);
                    compMatrixFilter = ExpressionMatrixStat.filter(compMatrixFilter, filter);
                } else if (filter.getTableElementName().contains(TypeStat.TSTUDENTTILING + "")) {
                    filter.setTableElementName("STAT_" + comparison + "_" + TypeStat.TSTUDENTTILING + Tiling.EXTENSION);
                    compMatrixFilter = ExpressionMatrixStat.filter(compMatrixFilter, filter);
                } else if (filter.getTableElementName().contains(TypeStat.LOGFC + "")) {
                    filter.setTableElementName(TypeStat.LOGFC + "_" + comparison + Tiling.EXTENSION);
                    if (!compMatrix.getHeaders().contains(filter.getTableElementName()))
                        filter.setTableElementName(TypeStat.LOGFC + "_" + comparison + GeneExpression.EXTENSION);
                    compMatrixFilter = ExpressionMatrixStat.filter(compMatrixFilter, filter);
                }
                System.err.println(
                        "Filter: " + filter.getTableElementName() + " size " + compMatrixFilter.getNumberRow());
            }
            ArrayList<String> listFilter = compMatrixFilter.getRowNamesToList();
            ArrayList<String> overExpressed = new ArrayList<String>();
            ArrayList<String> underExpressed = new ArrayList<String>();
            ArrayList<String> allExpressed = new ArrayList<String>();
            String colName = "LOGFC_" + comparison + Tiling.EXTENSION;
            if (!compMatrix.getHeaders().contains(colName))
                colName = "LOGFC_" + comparison + GeneExpression.EXTENSION;

            for (String element : listFilter) {
                double logFC = compMatrix.getValue(element, colName);
                if (logFC < 0) {
                    allExpressed.add(element);
                    underExpressed.add(element);
                } else {
                    allExpressed.add(element);
                    overExpressed.add(element);
                }
            }
            System.out.println("Size of element list" + allExpressed.size());
            if (compMatrixFilter.getNumberRow() > 1) {
                compMatrixFilter.saveTab(path + comparison + "_" + ID + "_Filter.txt", "Genome elements");
                // compMatrixFilter.save(path+comparison+"_"+ID+"_Filter");
            }

            // TabDelimitedTableReader.saveList(allExpressed, path+comparison+"_"+ID+"_List.txt");
            // TabDelimitedTableReader.saveList(overExpressed, path+comparison+"_"+ID+"_Over_List.txt");
            // TabDelimitedTableReader.saveList(underExpressed, path+comparison+"_"+ID+"_Under_List.txt");

        }
    }


    /**
     * Calculate a list of comparisons
     * 
     * @param comparisons
     * @param genomeElements
     * @param tiling
     * @param geneExpr
     * @return
     */
    public static ExpressionMatrix calculateCompMatrix(ArrayList<String> comparisons, ArrayList<String> genomeElements,
            boolean tiling, boolean geneExpr) {

        // create autotest depending of boolean: tiling and geneExpr
        ArrayList<StatTest> tests = new ArrayList<StatTest>();
        if (geneExpr && tiling)
            tests.add(new StatTest(TypeStat.AUTO_BOTH));
        else if (geneExpr && !tiling)
            tests.add(new StatTest(TypeStat.AUTO_GENEXPR));
        else if (!geneExpr && tiling)
            tests.add(new StatTest(TypeStat.AUTO_TILING));

        // calculate matrices
        ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
        Comparison comp = new Comparison();
        if (geneExpr) {
            comp = new Comparison(tests, genomeElements, comparisons, false);
            ExpressionMatrix compMatrix = ComparisonsCreation.calc(comp);
            compMatrix.setFirstRowName("Genome elements");
            if (compMatrix.getNumberColumn() != 0) {
                matrices.add(compMatrix);
            }
        }
        if (tiling) {
            comp = new Comparison(tests, genomeElements, comparisons, true);
            ExpressionMatrix compMatrix2 = ComparisonsCreation.calc(comp);
            compMatrix2.setFirstRowName("Genome elements");
            if (compMatrix2.getNumberColumn() != 0) {
                matrices.add(compMatrix2);
            }
        }

        // finalize matrix
        if (matrices.size() != 0) {
            ExpressionMatrix compMatrixFinal = ExpressionMatrix.merge(matrices, false);
            if (matrices.size() > 1) {
                compMatrixFinal = reorganizeMatrix(compMatrixFinal, tests, comp);
            }
            // compMatrixFinal = Annotation.addAnnotation(compMatrixFinal);
            return compMatrixFinal;
        } else
            return null;
    }



    /**
     * Reorder headers of the comparison Matrix to group GenExpression and Tiling results coming from
     * the same comparison <br>
     * 
     * @param compMatrix
     * @param tests
     * @param comp
     * @return
     */
    public static ExpressionMatrix reorganizeMatrix(ExpressionMatrix compMatrix, ArrayList<StatTest> tests,
            Comparison comp) {
        ArrayList<String> newHeaders = new ArrayList<String>();
        for (int i = 0; i < comp.getLeftBCs().size(); i++) {
            String name = comp.getLeftBCs().get(i) + " vs " + comp.getRightBCs().get(i);
            for (String header : compMatrix.getHeaders()) {
                if (header.contains(name))
                    newHeaders.add(header);
            }
        }
        compMatrix.reorderHeaders(newHeaders);

        for (StatTest test : tests) {
            if (test.getType() == TypeStat.AUTO_BOTH) {
                // first calculate mean of FDRBY and FDRBonf on GeneExpression and Tiling for each comparison
                ArrayList<String> colToRemove = new ArrayList<String>();
                for (int i = 0; i < comp.getLeftBCs().size(); i++) {
                    String name = comp.getLeftBCs().get(i) + " vs " + comp.getRightBCs().get(i);
                    for (String rowName : compMatrix.getRowNames().keySet()) {
                        // FDRBY
                        double mean =
                                (compMatrix.getValue(rowName, "STAT_" + name + "_FDRBY" + GeneExpression.EXTENSION)
                                        + compMatrix.getValue(rowName, "STAT_" + name + "_FDRBY" + Tiling.EXTENSION))
                                        / 2;
                        if (mean < 0)
                            mean = compMatrix.getValue(rowName, "STAT_" + name + "_FDRBY" + Tiling.EXTENSION); // in
                                                                                                               // case
                                                                                                               // GE
                                                                                                               // value
                                                                                                               // is
                                                                                                               // equal
                                                                                                               // to
                                                                                                               // MISSING_VALUE
                        compMatrix.setValue(mean, rowName, "STAT_" + name + "_FDRBY" + Tiling.EXTENSION);
                        // FDRBONF
                        mean = (compMatrix.getValue(rowName, "STAT_" + name + "_FDRBONF" + GeneExpression.EXTENSION)
                                + compMatrix.getValue(rowName, "STAT_" + name + "_FDRBONF" + Tiling.EXTENSION)) / 2;
                        if (mean < 0)
                            mean = compMatrix.getValue(rowName, "STAT_" + name + "_FDRBONF" + Tiling.EXTENSION); // in
                                                                                                                 // case
                                                                                                                 // GE
                                                                                                                 // value
                                                                                                                 // is
                                                                                                                 // equal
                                                                                                                 // to
                                                                                                                 // MISSING_VALUE
                        compMatrix.setValue(mean, rowName, "STAT_" + name + "_FDRBONF" + Tiling.EXTENSION);
                        // ARRAYS_CORR
                        double ratio = ExpressionMatrix.MISSING_VALUE;
                        if (compMatrix.getValue(rowName,
                                "CORR_" + name + "_ARRAYSCORR" + GeneExpression.EXTENSION) != 0) {
                            ratio = compMatrix.getValue(rowName, "CORR_" + name + "_ARRAYSCORR" + Tiling.EXTENSION)
                                    / compMatrix.getValue(rowName,
                                            "CORR_" + name + "_ARRAYSCORR" + GeneExpression.EXTENSION);
                        }
                        compMatrix.setValue(ratio, rowName, "CORR_" + name + "_ARRAYSCORR" + Tiling.EXTENSION);
                    }

                    colToRemove.add("STAT_" + name + "_FDRBY" + GeneExpression.EXTENSION);
                    colToRemove.add("STAT_" + name + "_FDRBONF" + GeneExpression.EXTENSION);
                    colToRemove.add("CORR_" + name + "_ARRAYSCORR" + GeneExpression.EXTENSION);
                }

                // remove unecessary column
                for (String colName : colToRemove)
                    compMatrix.deleteColumn(colName);
            }

            if (test.getType() == TypeStat.ARRAYSCORR) {
                // first calculate mean of FDRBY and FDRBonf on GeneExpression and Tiling for each comparison
                ArrayList<String> colToRemove = new ArrayList<String>();
                for (int i = 0; i < comp.getLeftBCs().size(); i++) {
                    String name = comp.getLeftBCs().get(i) + " vs " + comp.getRightBCs().get(i);
                    for (String rowName : compMatrix.getRowNames().keySet()) {
                        double ratio = ExpressionMatrix.MISSING_VALUE;
                        if (compMatrix.getValue(rowName,
                                "CORR_" + name + "_ARRAYSCORR" + GeneExpression.EXTENSION) != 0) {
                            ratio = compMatrix.getValue(rowName, "CORR_" + name + "_ARRAYSCORR" + Tiling.EXTENSION)
                                    / compMatrix.getValue(rowName,
                                            "CORR_" + name + "_ARRAYSCORR" + GeneExpression.EXTENSION);
                        }
                        compMatrix.setValue(ratio, rowName, "CORR_" + name + "_ARRAYSCORR" + Tiling.EXTENSION);
                    }
                    colToRemove.add("CORR_" + name + "_ARRAYSCORR" + GeneExpression.EXTENSION);
                }

                // remove unecessary column
                for (String colName : colToRemove)
                    compMatrix.deleteColumn(colName);
            }
        }
        return compMatrix;
    }


    /**
     * Create summarize webpage
     * 
     * @throws IOException
     */
    private static void summarizeInHTML(String path, String comparison) throws IOException {
        String html = FileUtils.readText(Database.getDATA_PATH() + "ComparisonReport.html");
        html = html.replaceAll("VName", comparison);

        /*
         * Then replace all cell of the table by specific information
         */
        ArrayList<String> list = TabDelimitedTableReader.readList(path + comparison + "_Gene_GEonly_Over_List.txt");
        html = html.replaceAll("VPlusGenes", list.size() + "");
        list = TabDelimitedTableReader.readList(path + comparison + "_Gene_GEonly_Under_List.txt");
        html = html.replaceAll("VMinusGenes", list.size() + "");
        list = TabDelimitedTableReader.readList(path + comparison + "_Gene_Over_List.txt");
        html = html.replaceAll("VPlusBothGenes", list.size() + "");
        list = TabDelimitedTableReader.readList(path + comparison + "_Gene_Under_List.txt");
        html = html.replaceAll("VMinusBothGenes", list.size() + "");
        // list = TabDelimitedTableReader.readList(path+comparison+"_Operon_Under_List.txt");
        // html = html.replaceAll("VMinusOperons", list.size()+"");
        // list = TabDelimitedTableReader.readList(path+comparison+"_Operon_Over_List.txt");
        // html = html.replaceAll("VPlusOperons", list.size()+"");
        // list = TabDelimitedTableReader.readList(path+comparison+"_NcRNA_Over_List.txt");
        // html = html.replaceAll("VPlusncRNAs", list.size()+"");
        // list = TabDelimitedTableReader.readList(path+comparison+"_NcRNA_Under_List.txt");
        // html = html.replaceAll("VMinusncRNAs", list.size()+"");
        list = TabDelimitedTableReader.readList(path + comparison + "_Srna_Under_List.txt");
        html = html.replaceAll("VMinusSrnas", list.size() + "");
        list = TabDelimitedTableReader.readList(path + comparison + "_Srna_Over_List.txt");
        html = html.replaceAll("VPlusSrnas", list.size() + "");
        list = TabDelimitedTableReader.readList(path + comparison + "_ASrna_Over_List.txt");
        html = html.replaceAll("VPlusASrnas", list.size() + "");
        list = TabDelimitedTableReader.readList(path + comparison + "_ASrna_Under_List.txt");
        html = html.replaceAll("VMinusASrnas", list.size() + "");
        list = TabDelimitedTableReader.readList(path + comparison + "_CisReg_Over_List.txt");
        html = html.replaceAll("VPlusCisRegs", list.size() + "");
        list = TabDelimitedTableReader.readList(path + comparison + "_CisReg_Under_List.txt");
        html = html.replaceAll("VMinusCisRegs", list.size() + "");

        FileUtils.saveText(html, path + comparison + "_Report.html");

    }


    /**
     * Run the comparison by calculating statistical parameters given in tests ArrayList
     * 
     * @return compMatrix containing the different statistical values
     * @throws Exception
     */
    public static ExpressionMatrix calc(Comparison comp) {
        // Tiling array comparison
        if (comp.isTiling()) {
            // load data
            TreeMap<String, ArrayList<Tiling>> bioConds = Tiling.getTilingData(comp.getLeftBCs(), comp.getRightBCs());
            // add EGDeWT_Average to bioConds but not in leftBCs or rightBCs
            if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT) {
                bioConds.put(EGDeWTdata.NAME_Mean, EGDeWTdata.getTilings());
            }
            // curate comparison
            curateComparisonTiling(bioConds, comp);

            for (int i = 0; i < comp.getLeftBCs().size(); i++) {
                System.out.println(comp.getLeftBCs().get(i) + "  " + comp.getRightBCs().get(i));
            }

            // prepare data
            Genome genome = Genome.loadEgdeGenome();
            final ExpressionMatrix compExpression = createCompExpressionMatrix(genome, comp);

            // go through all genome element
            final TreeMap<String, ArrayList<Tiling>> bioCondsTemp = bioConds;
            final Comparison compTemp = comp;

            // load all tiling Data (multi-thread operation)
            for (String gElement : compTemp.getGenomeElements()) {

                Sequence sequence = genome.getElement(gElement);
                if (sequence != null) {
                    // System.out.println(gElement);
                    // get the corresponding strand specific tiling data, and get the data
                    TreeMap<String, double[]> values = new TreeMap<String, double[]>();
                    for (String bioCondName : bioCondsTemp.keySet()) {
                        for (Tiling tilingTemp : bioCondsTemp.get(bioCondName)) {
                            if (sequence.isStrand() && tilingTemp.getName().contains("+")) {
                            	double[] value = tilingTemp.get(sequence.getBegin(), sequence.getEnd(), true);
                                values.put(bioCondName, value);
                            } else if (!sequence.isStrand() && tilingTemp.getName().contains("-")) {
                                double[] value = tilingTemp.get(sequence.getBegin(), sequence.getEnd(), true);
                                values.put(bioCondName, value);
                            }
                        }
                    }

                    // then compare everything
                    for (int i = 0; i < compTemp.getLeftBCs().size(); i++) {
                        double[] leftValue = values.get(compTemp.getLeftBCs().get(i));
                        double[] rightValue = values.get(compTemp.getRightBCs().get(i));
                        // double[] meanWTvalue = values.get(EGDeWTdata.NAME_Mean);

                        for (StatTest test : compTemp.getTests()) {
                            try {
                                String data = Tiling.EXTENSION;
                                if (test.getType() == TypeStat.FC) {
                                    double testResult = StatUtils.foldChangeTiling(leftValue, rightValue, false);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.FC + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + data);
                                } else if (test.getType() == TypeStat.LOGFC) {
                                    double testResult = StatUtils.foldChangeTiling(leftValue, rightValue, true);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.LOGFC + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + data);
                                } else if (test.getType() == TypeStat.ARRAYSCORR) {
                                    double testResult = StatUtils.foldChangeTiling(leftValue, rightValue, true);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.CORR + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.ARRAYSCORR + data);
                                    // }else if(test.getType()==TypeStat.LNFCWT){
                                    // double testResult = StatUtils.foldChangeTiling(leftValue, meanWTvalue,true);
                                    // compExpression.setValue(testResult, gElement,
                                    // ColorMapper.TypeMapper.LOGFC+"_"+compTemp.getLeftBCs().get(i)+" vs
                                    // "+compTemp.getRightBCs().get(i)+"_"+TypeStat.LNFCWT+data);
                                } else if (test.getType() == TypeStat.LPE) { // do nothing
                                } else if (test.getType() == TypeStat.FDRBH || test.getType() == TypeStat.FDRBY
                                        || test.getType() == TypeStat.FDRBONF) {
                                    // we calculate ttest and adjust it after
                                    double testResult = TestUtils.tTest(leftValue, rightValue);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + test.getType() + data);
                                } else if (test.getType() == TypeStat.AUTO_BOTH) {
                                    // calc LOGFC
                                    double testResult = StatUtils.foldChangeTiling(leftValue, rightValue, true);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.LOGFC + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + data);
                                    // fill ARRYAS_CORR column
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.CORR + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.ARRAYSCORR + data);
                                    // calc LOGFC_WT
                                    // testResult = StatUtils.foldChangeTiling(leftValue, meanWTvalue,true);
                                    // compExpression.setValue(testResult, gElement,
                                    // ColorMapper.TypeMapper.LOGFC+"_"+compTemp.getLeftBCs().get(i)+" vs
                                    // "+compTemp.getRightBCs().get(i)+"_"+TypeStat.LNFCWT+data);
                                    // calc T-test (Tiling)
                                    testResult = TestUtils.tTest(leftValue, rightValue);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.TSTUDENTTILING
                                                    + data);
                                    // calc T-test (Tiling) for FDRBH
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBY + data);
                                    // calc T-test (Tiling) for FDRBY
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBONF + data);
                                } else if (test.getType() == TypeStat.AUTO_TILING) {
                                    // calc LOGFC
                                    double testResult = StatUtils.foldChangeTiling(leftValue, rightValue, true);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.LOGFC + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + data);
                                    // calc LOGFC_WT
                                    // testResult = StatUtils.foldChangeTiling(leftValue, meanWTvalue,true);
                                    // compExpression.setValue(testResult, gElement,
                                    // ColorMapper.TypeMapper.LOGFC+"_"+compTemp.getLeftBCs().get(i)+" vs
                                    // "+compTemp.getRightBCs().get(i)+"_"+TypeStat.LNFCWT+data);
                                    // calc T-test (Tiling)

                                    testResult = TestUtils.tTest(leftValue, rightValue);

                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.TSTUDENTTILING
                                                    + data);
                                    // calc T-test (Tiling) for FDRBH
                                    testResult = TestUtils.tTest(leftValue, rightValue);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBH + data);
                                    // calc T-test (Tiling) for FDRBY
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBY + data);
                                    // calc T-test (Tiling) for FDRBonf
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBONF + data);
                                } else {
                                    double testResult = test.run(leftValue, rightValue);
                                    compExpression.setValue(testResult, gElement,
                                            ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                    + compTemp.getRightBCs().get(i) + "_" + test.getType() + data);
                                }
                            } catch (IllegalArgumentException | MathException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }


            // if we FDR we adjust the matrix in consequence
            for (StatTest test : comp.getTests()) {
                if (test.getType() == TypeStat.FDRBH || test.getType() == TypeStat.FDRBY
                        || test.getType() == TypeStat.FDRBONF) {
                    FDR.adjust(compExpression, test, comp.isTiling());
                } else if (test.getType() == TypeStat.AUTO_BOTH) {
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBY), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBONF), comp.isTiling());
                } else if (test.getType() == TypeStat.AUTO_GENEXPR) {
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBH), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBY), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBONF), comp.isTiling());
                } else if (test.getType() == TypeStat.AUTO_TILING) {
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBH), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBY), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBONF), comp.isTiling());
                }
            }
            return compExpression;
        } else {
            // load data
            TreeMap<String, GeneExpression> bioConds =
                    GeneExpression.getGExpressionData(comp.getLeftBCs(), comp.getRightBCs());
            // add EGDeWT_Average to bioConds but not to leftBCs or rightBCs
            if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT) {
                bioConds.put(EGDeWTdata.NAME_Mean, EGDeWTdata.getGeneExpr());
            }

            // curate comparison
            curateComparisonGexpression(bioConds, comp);

            for (int i = 0; i < comp.getLeftBCs().size(); i++) {
                System.out.println(comp.getLeftBCs().get(i) + "  " + comp.getRightBCs().get(i));
            }

            // prepare data
            Genome genome = Genome.loadEgdeGenome();
            final ExpressionMatrix compExpression = createCompExpressionMatrix(genome, comp);

            // init estimate in case LPE is selected
            final TreeMap<String, PolynomialSplineFunction> estimateLPE = LPE.createEstimates(bioConds);// create
                                                                                                        // ExpressionMatrix

            // go through all genome element (multithread)
            final TreeMap<String, GeneExpression> bioCondsTemp = bioConds;
            final Comparison compTemp = comp;
            LinkedHashMap<String, Operon> operons = new LinkedHashMap<String, Operon>();
            operons = genome.getFirstChromosome().getOperons();
            // load all tiling Data
            for (String gElement : compExpression.getRowNames().keySet()) {
                // get begin and end of this element
                Sequence sequence = genome.getElement(gElement);
                if (sequence != null) {
                    // System.out.println(gElement);
                    TreeMap<String, double[]> values = new TreeMap<String, double[]>();
                    for (String bioCondName : bioCondsTemp.keySet()) {
                        GeneExpression geneExpr = bioCondsTemp.get(bioCondName);
                        Operon operon = operons.get(gElement);
                        if (operon != null) {
                            TreeMap<String, Integer> probes = Database.getInstance().getProbesGExpression();
                            int size = 0;
                            for (String gene : operon.getGenes()) {
                                if (probes.containsKey(gene))
                                    size++;
                            }
                            double[] value = new double[3 * size];
                            int k = 0;
                            for (String gene : operon.getGenes()) {
                                if (probes.containsKey(gene)) {
                                    double[] valueTemp = geneExpr.getTriplicateValue(gene);
                                    for (int i = 0; i < valueTemp.length; i++) {
                                        value[k] = valueTemp[i];
                                        k++;
                                    }
                                }
                            }
                            values.put(bioCondName, value);
                        } else {
                            double[] value = geneExpr.getTriplicateValue(gElement);
                            values.put(bioCondName, value);
                        }
                    }

                    // then compare everything
                    for (int i = 0; i < compTemp.getLeftBCs().size(); i++) {
                        double[] leftValue = values.get(compTemp.getLeftBCs().get(i));
                        double[] rightValue = values.get(compTemp.getRightBCs().get(i));
                        // double[] meanWTvalue = values.get(EGDeWTdata.NAME_Mean);

                        for (StatTest test : compTemp.getTests()) {
                            String data = GeneExpression.EXTENSION;
                            if (test.getType() == TypeStat.FC) {
                                double testResult = StatUtils.foldChangeGeneExpression(leftValue, rightValue, false);
                                compExpression.setValue(testResult, gElement, ColorMapper.TypeMapper.FC + "_"
                                        + compTemp.getLeftBCs().get(i) + " vs " + compTemp.getRightBCs().get(i) + data);
                            } else if (test.getType() == TypeStat.LOGFC) {
                                double testResult = StatUtils.foldChangeGeneExpression(leftValue, rightValue, true);
                                compExpression.setValue(testResult, gElement, ColorMapper.TypeMapper.LOGFC + "_"
                                        + compTemp.getLeftBCs().get(i) + " vs " + compTemp.getRightBCs().get(i) + data);
                            } else if (test.getType() == TypeStat.ARRAYSCORR) {
                                double testResult = StatUtils.foldChangeGeneExpression(leftValue, rightValue, true);
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.CORR + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + TypeStat.ARRAYSCORR + data);
                                // }else if(test.getType()==TypeStat.LNFCWT){
                                // double testResult = StatUtils.foldChangeGeneExpression(leftValue, meanWTvalue,true);
                                // compExpression.setValue(testResult, gElement,
                                // ColorMapper.TypeMapper.LOGFC+"_"+compTemp.getLeftBCs().get(i)+" vs
                                // "+compTemp.getRightBCs().get(i)+"_"+TypeStat.LNFCWT+data);
                            } else if (test.getType() == TypeStat.LPE) {
                                double testResult =
                                        LPE.calcP(leftValue, rightValue, estimateLPE.get(compTemp.getLeftBCs().get(i)),
                                                estimateLPE.get(compTemp.getRightBCs().get(i)));
                                compExpression.setValue(testResult, gElement, "STAT_" + compTemp.getLeftBCs().get(i)
                                        + " vs " + compTemp.getLeftBCs().get(i) + "_LPE" + data);
                            } else if (test.getType() == TypeStat.TSTUDENTTILING) { // do nothing
                            } else if (test.getType() == TypeStat.FDRBH || test.getType() == TypeStat.FDRBY
                                    || test.getType() == TypeStat.FDRBONF) {
                                // we calculate LPE, and adjust it after
                                double testResult =
                                        LPE.calcP(leftValue, rightValue, estimateLPE.get(compTemp.getLeftBCs().get(i)),
                                                estimateLPE.get(compTemp.getRightBCs().get(i)));
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + test.getType() + data);
                            } else if (test.getType() == TypeStat.AUTO_BOTH) {
                                // calc LOGFC
                                double testResult = StatUtils.foldChangeGeneExpression(leftValue, rightValue, true);
                                compExpression.setValue(testResult, gElement, ColorMapper.TypeMapper.LOGFC + "_"
                                        + compTemp.getLeftBCs().get(i) + " vs " + compTemp.getRightBCs().get(i) + data);
                                // fill ARRAYS_CORR column
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.CORR + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + TypeStat.ARRAYSCORR + data);
                                // // calc LOGFC_WT
                                // testResult = StatUtils.foldChangeGeneExpression(leftValue, meanWTvalue,true);
                                // compExpression.setValue(testResult, gElement,
                                // ColorMapper.TypeMapper.LOGFC+"_"+compTemp.getLeftBCs().get(i)+" vs
                                // "+compTemp.getRightBCs().get(i)+"_"+TypeStat.LNFCWT+data);
                                // calc LPE
                                testResult =
                                        LPE.calcP(leftValue, rightValue, estimateLPE.get(compTemp.getLeftBCs().get(i)),
                                                estimateLPE.get(compTemp.getRightBCs().get(i)));
                                compExpression.setValue(testResult, gElement, "STAT_" + compTemp.getLeftBCs().get(i)
                                        + " vs " + compTemp.getRightBCs().get(i) + "_LPE" + data);
                                // calc LPE for FDRBH
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBY + data);
                                // calc LPE for FDRBY
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBONF + data);
                            } else if (test.getType() == TypeStat.AUTO_GENEXPR) {
                                // calc FC
                                double testResult = StatUtils.foldChangeGeneExpression(leftValue, rightValue, false);
                                compExpression.setValue(testResult, gElement, ColorMapper.TypeMapper.FC + "_"
                                        + compTemp.getLeftBCs().get(i) + " vs " + compTemp.getRightBCs().get(i) + data);
                                // calc LogFC
                                testResult = StatUtils.foldChangeGeneExpression(leftValue, rightValue, true);
                                compExpression.setValue(testResult, gElement, ColorMapper.TypeMapper.LOGFC + "_"
                                        + compTemp.getLeftBCs().get(i) + " vs " + compTemp.getRightBCs().get(i) + data);
                                // // calc LOGFC_WT
                                // testResult = StatUtils.foldChangeGeneExpression(leftValue, meanWTvalue,true);
                                // compExpression.setValue(testResult, gElement,
                                // ColorMapper.TypeMapper.LOGFC+"_"+compTemp.getLeftBCs().get(i)+" vs
                                // "+compTemp.getRightBCs().get(i)+"_"+TypeStat.LNFCWT+data);
                                // calc LPE
                                // testResult = TestUtils.tTest(leftValue, rightValue);
                                testResult =
                                        LPE.calcP(leftValue, rightValue, estimateLPE.get(compTemp.getLeftBCs().get(i)),
                                                estimateLPE.get(compTemp.getRightBCs().get(i)));
                                compExpression.setValue(testResult, gElement, "STAT_" + compTemp.getLeftBCs().get(i)
                                        + " vs " + compTemp.getRightBCs().get(i) + "_LPE" + data);
                                // calc LPE for FDRBH
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBH + data);
                                // calc LPE for FDRBY
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBY + data);
                                // calc LPE for FDRBonf
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + TypeStat.FDRBONF + data);
                            } else {
                                double testResult = test.run(leftValue, rightValue);
                                compExpression.setValue(testResult, gElement,
                                        ColorMapper.TypeMapper.STAT + "_" + compTemp.getLeftBCs().get(i) + " vs "
                                                + compTemp.getRightBCs().get(i) + "_" + test.getType() + data);
                            }

                        }
                    }
                }
            }


            // if we FDR we adjust the matrix in consequence
            for (StatTest test : comp.getTests()) {
                if (test.getType() == TypeStat.FDRBH || test.getType() == TypeStat.FDRBY
                        || test.getType() == TypeStat.FDRBONF) {
                    FDR.adjust(compExpression, test, comp.isTiling());
                } else if (test.getType() == TypeStat.AUTO_BOTH) {
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBY), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBONF), comp.isTiling());
                } else if (test.getType() == TypeStat.AUTO_GENEXPR) {
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBH), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBY), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBONF), comp.isTiling());
                } else if (test.getType() == TypeStat.AUTO_TILING) {
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBH), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBY), comp.isTiling());
                    FDR.adjust(compExpression, new StatTest(TypeStat.FDRBONF), comp.isTiling());
                }
            }
            return compExpression;
        }

    }

    /**
     * Create the ExpressionMatrix which will be used for calculating comparisons
     * 
     * @param genome
     * @return
     */
    private static ExpressionMatrix createCompExpressionMatrix(Genome genome, Comparison comp) {
        ExpressionMatrix expression = new ExpressionMatrix();
        TreeMap<String, Integer> probes = Database.getInstance().getProbesGExpression();
        TreeMap<String, Integer> rowNames = new TreeMap<String, Integer>();
        LinkedHashMap<String, Operon> operons = new LinkedHashMap<String, Operon>();
        try {
            operons = genome.getFirstChromosome().getOperons();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int k = 0;
        for (String gElement : comp.getGenomeElements()) {
            if (!comp.isTiling()) {
                if (probes.containsKey(gElement)) {
                    rowNames.put(gElement, k);
                    k++;
                }
                try {
                    Operon operon = operons.get(gElement);
                    if (operon != null) {
                        rowNames.put(gElement, k);
                        k++;
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                rowNames.put(gElement, k);
                k++;
            }
        }

        // set row names
        expression.setRowNames(rowNames);

        /*
         * set the different headers
         */
        ArrayList<String> headers = new ArrayList<String>();
        for (int i = 0; i < comp.getLeftBCs().size(); i++) {
            String leftBC = comp.getLeftBCs().get(i);
            String rightBC = comp.getRightBCs().get(i);
            for (StatTest test : comp.getTests()) {
                String data = Tiling.EXTENSION;
                if (!comp.isTiling())
                    data = GeneExpression.EXTENSION;
                if (test.getType() == TypeStat.FC)
                    headers.add(ColorMapper.TypeMapper.FC + "_" + leftBC + " vs " + rightBC + data);
                else if (test.getType() == TypeStat.LOGFC)
                    headers.add(ColorMapper.TypeMapper.LOGFC + "_" + leftBC + " vs " + rightBC + data);
                // else if(test.getType()==TypeStat.LNFCWT) headers.add(ColorMapper.TypeMapper.LOGFC+"_"+leftBC+" vs
                // "+rightBC+"_"+TypeStat.LNFCWT+data);
                else if (test.getType() == TypeStat.ARRAYSCORR)
                    headers.add(ColorMapper.TypeMapper.CORR + "_" + leftBC + " vs " + rightBC + "_"
                            + TypeStat.ARRAYSCORR + data);
                else if (test.getType() == TypeStat.LPE) { // LPE is only for GeneExpression data
                    if (!comp.isTiling())
                        headers.add("STAT_" + leftBC + " vs " + rightBC + "_LPE" + data);
                } else if (test.getType() == TypeStat.TSTUDENTTILING) { // this t student is only for Tiling data
                    if (comp.isTiling())
                        headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + test.getType()
                                + data);
                } else if (test.getType() == TypeStat.AUTO_BOTH) {
                    headers.add(ColorMapper.TypeMapper.LOGFC + "_" + leftBC + " vs " + rightBC + data);
                    if (comp.isTiling())
                        headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_"
                                + TypeStat.TSTUDENTTILING + data);
                    else
                        headers.add("STAT_" + leftBC + " vs " + rightBC + "_LPE" + data);
                    // headers.add(ColorMapper.TypeMapper.LOGFC+"_"+leftBC+" vs "+rightBC+"_"+TypeStat.LNFCWT+data);
                    headers.add(ColorMapper.TypeMapper.CORR + "_" + leftBC + " vs " + rightBC + "_"
                            + TypeStat.ARRAYSCORR + data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + TypeStat.FDRBY
                            + data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + TypeStat.FDRBONF
                            + data);
                } else if (test.getType() == TypeStat.AUTO_GENEXPR) {
                    headers.add(ColorMapper.TypeMapper.LOGFC + "_" + leftBC + " vs " + rightBC + data);
                    headers.add(ColorMapper.TypeMapper.FC + "_" + leftBC + " vs " + rightBC + data);
                    // headers.add(ColorMapper.TypeMapper.LOGFC+"_"+leftBC+" vs "+rightBC+"_"+TypeStat.LNFCWT+data);
                    headers.add("STAT_" + leftBC + " vs " + rightBC + "_LPE" + data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + TypeStat.FDRBH
                            + data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + TypeStat.FDRBY
                            + data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + TypeStat.FDRBONF
                            + data);
                } else if (test.getType() == TypeStat.AUTO_TILING) {
                    headers.add(ColorMapper.TypeMapper.LOGFC + "_" + leftBC + " vs " + rightBC + data);
                    // headers.add(ColorMapper.TypeMapper.LOGFC+"_"+leftBC+" vs "+rightBC+"_"+TypeStat.LNFCWT+data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_"
                            + TypeStat.TSTUDENTTILING + data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + TypeStat.FDRBH
                            + data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + TypeStat.FDRBY
                            + data);
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + TypeStat.FDRBONF
                            + data);
                } else
                    headers.add(ColorMapper.TypeMapper.STAT + "_" + leftBC + " vs " + rightBC + "_" + test.getType()
                            + data);
            }
        }
        expression.setValues(new double[k][headers.size()]);
        expression.setHeaders(headers);
        return expression;
    }


    /**
     * Go through both leftBC and rightBC and delete element which are not in bioConds
     * 
     * @param bioConds
     */
    private static void curateComparisonTiling(TreeMap<String, ArrayList<Tiling>> bioConds, Comparison comp) {
        int k = 0;
        while (k < comp.getLeftBCs().size()) {
            String left = comp.getLeftBCs().get(k);
            String right = comp.getRightBCs().get(k);

            if (bioConds.containsKey(left) && bioConds.containsKey(right))
                k++;
            else {
                comp.getLeftBCs().remove(k);
                comp.getRightBCs().remove(k);
            }
        }
    }

    /**
     * Go through both leftBC and rightBC and delete element which are not in bioConds
     * 
     * @param bioConds
     */
    private static void curateComparisonGexpression(TreeMap<String, GeneExpression> bioConds, Comparison comp) {
        int k = 0;
        while (k < comp.getLeftBCs().size()) {
            String left = comp.getLeftBCs().get(k);
            String right = comp.getRightBCs().get(k);

            if (bioConds.containsKey(left) && bioConds.containsKey(right))
                k++;
            else {
                comp.getLeftBCs().remove(k);
                comp.getRightBCs().remove(k);
            }
        }
    }

    /**
     * For each Comparison calculates corresponding TranscriptomeData, and save it
     * 
     * @param allComp
     */
    public static void setAllComparisonStreamData(ArrayList<String> allComp) {
        for (String comp : allComp) {
            String[] bioConds = BioCondition.parseName(comp);
            BioCondition bioCond1 = BioCondition.getBioCondition(bioConds[0]);
            BioCondition bioCond2 = BioCondition.getBioCondition(bioConds[1]);
            BioCondition compareBioCond = bioCond1.compare(bioCond2, true);
            // save all tiling and GeneExpr
            for (GeneExpression geneExpr : compareBioCond.getGeneExprs()) {
                geneExpr.save();
            }
            for (Tiling tiling : compareBioCond.getTilings()) {
                tiling.save();
            }
        }
    }



    /**
     * Create Database.logFCMatrix by regrouping every logFC values for genes, sRNAs, asRNAs, and
     * cisRegs in every <code>BioCondition</code> given <code>Experiment</code><br>
     * 
     * @param exp list of BioCondition
     * @param genome current genome with list of genes, sRNAs, asRNAs and cisRegs
     */
    public static void createLogFCMatrix(Experiment exp, Genome genome) {
        /*
         * Init comparison matrix: in headers we put all comparisons, in rownames all genes, srna and ASrna
         */
        ExpressionMatrix logFCMatrix = new ExpressionMatrix();
        int i = 0;
        for (String gene : genome.getChromosomes().get(0).getGenes().keySet()) {
            logFCMatrix.getRowNames().put(gene, i);
            i++;
        }
        for (String sRNA : genome.getChromosomes().get(0).getsRNAs().keySet()) {
            logFCMatrix.getRowNames().put(sRNA, i);
            i++;
        }
        for (String asRNA : genome.getChromosomes().get(0).getAsRNAs().keySet()) {
            logFCMatrix.getRowNames().put(asRNA, i);
            i++;
        }
        for (String cisReg : genome.getChromosomes().get(0).getCisRegs().keySet()) {
            logFCMatrix.getRowNames().put(cisReg, i);
            i++;
        }
        for (BioCondition bioCond : exp.getBioConditions()) {
            for (String comparison : bioCond.getComparisonNames()) {
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
            for (String comp : comparisonNames) {
                String fileName = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_Gene_GEonly.txt";
                String fileNameTiling = OmicsData.PATH_COMPARISONS + "/" + comp + File.separator + comp + "_Gene.txt";
                System.out.println("load: " + fileName);
                ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, true);
                ExpressionMatrix matrixTiling = ExpressionMatrix.loadTab(fileNameTiling, true);
                if (matrix != null) {
                    String headerGE = "LOGFC_" + comp + GeneExpression.EXTENSION;
                    String headerTiling = "LOGFC_" + comp + Tiling.EXTENSION;
                    for (String gene : genome.getChromosomes().get(0).getGenes().keySet()) {
                        // System.out.println(header+" "+gene+" "+comp);
                        if (matrix.getRowNames().containsKey(gene)) {
                            logFCMatrix.setValue(matrix.getValue(gene, headerGE), gene, comp);
                        } else {
                            if (matrixTiling.getHeaders().contains(headerTiling)) {
                                double value = matrixTiling.getValue(gene, headerTiling);
                                System.out.println(value);
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
                matrix = ExpressionMatrix.loadTab(fileName, true);
                if (matrix != null) {
                    String header = "LOGFC_" + comp + Tiling.EXTENSION;
                    for (String sRNA : genome.getChromosomes().get(0).getsRNAs().keySet()) {
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
                    for (String asRNA : genome.getChromosomes().get(0).getAsRNAs().keySet()) {
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
                    for (String cisReg : genome.getChromosomes().get(0).getCisRegs().keySet()) {
                        logFCMatrix.setValue(matrix.getValue(cisReg, header), cisReg, comp);
                    }
                }
            }
        }

        logFCMatrix.setAnnotations(new String[0][0]);
        logFCMatrix.getHeaderAnnotation().clear();
        logFCMatrix = Annotation.addAnnotation(logFCMatrix, Genome.loadEgdeGenome());
        logFCMatrix.save(Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH());
        logFCMatrix.saveTab(Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + ".txt", "Probes");
    }

    /**
     * Create Database.statTable by calculating stat values for genes, sRNAs, and asRNAs in every
     * BioCondition<br>
     * For genes we use FDRBY calculated from GebneExpression array<br>
     * For sRNAs and asRNAs we use TSTUDENTTILING for Tiling array
     */
    public static void createStatMatrix(Experiment exp, Genome genome) {
        /*
         * Init comparison matrix: in headers we put all comparisons, in rownames all genes, srna and ASrna
         */
        ExpressionMatrix logStatTable = new ExpressionMatrix();
        int i = 0;
        for (String gene : genome.getChromosomes().get(0).getGenes().keySet()) {
            logStatTable.getRowNames().put(gene, i);
            i++;
        }
        for (String sRNA : genome.getChromosomes().get(0).getsRNAs().keySet()) {
            logStatTable.getRowNames().put(sRNA, i);
            i++;
        }
        for (String asRNA : genome.getChromosomes().get(0).getAsRNAs().keySet()) {
            logStatTable.getRowNames().put(asRNA, i);
            i++;
        }
        for (String cisReg : genome.getChromosomes().get(0).getCisRegs().keySet()) {
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
                    for (String gene : genome.getChromosomes().get(0).getGenes().keySet()) {
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
                    for (String sRNA : genome.getChromosomes().get(0).getsRNAs().keySet()) {
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
                    for (String asRNA : genome.getChromosomes().get(0).getAsRNAs().keySet()) {
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
                    for (String cisReg : genome.getChromosomes().get(0).getCisRegs().keySet()) {
                        logStatTable.setValue(matrix.getValue(cisReg, header), cisReg, comp);
                    }
                }
            }
        }

        // logStatTable.setAnnotations(new String[0][0]);
        // logStatTable.getHeaderAnnotation().clear();
        // logStatTable = Annotation.addAnnotation(logStatTable, Genome.loadEgdeGenome());
        logStatTable.save(Database.getSTAT_MATRIX_TRANSCRIPTOMES_PATH());
        logStatTable.saveTab(Database.getSTAT_MATRIX_TRANSCRIPTOMES_PATH() + ".txt", "Gene");
    }

    /**
     * Create an array :<br>
     * <li>Column = comparison
     * <li>Row = Gene, Srna or Asrna
     * <li>Cell = "+" if a specific gene has been detected over-expressed, and "-" if
     * under-expressed<br>
     * <br>
     * 
     * Save it in : Comparison.PATH_DIFF_LISTS+".txt"
     */
    public static void summarizeDiffExpr(Experiment exp, Genome genome) {
        /*
         * Init comparison matrix: in headers we put all comparisons, in rownames all genes, srna and ASrna
         */
        ExpressionMatrix diffExprTable = new ExpressionMatrix();
        int i = 0;
        for (String gene : genome.getChromosomes().get(0).getGenes().keySet()) {
            diffExprTable.getRowNames().put(gene, i);
            i++;
        }
        for (String sRNA : genome.getChromosomes().get(0).getsRNAs().keySet()) {
            diffExprTable.getRowNames().put(sRNA, i);
            i++;
        }
        for (String asRNA : genome.getChromosomes().get(0).getAsRNAs().keySet()) {
            diffExprTable.getRowNames().put(asRNA, i);
            i++;
        }
        for (String cisReg : genome.getChromosomes().get(0).getCisRegs().keySet()) {
            diffExprTable.getRowNames().put(cisReg, i);
            i++;
        }
        for (BioCondition bioCond : exp.getBioConditions()) {
            for (String comparison : bioCond.getComparisonNames()) {
                diffExprTable.addHeader(comparison);
            }
        }
        double[][] values = new double[diffExprTable.getRowNames().size()][diffExprTable.getHeaders().size()];
        diffExprTable.setValues(values);


        /*
         * Fill the matrix with GeneExpression values
         */
        for (BioCondition bioCond : exp.getBioConditions()) {
            ArrayList<String> comparisonNames = bioCond.getComparisonNames();
            for (String comparison : comparisonNames) {
                /*
                 * Genes
                 */
                ArrayList<String> over = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison
                        + File.separator + comparison + "_GEonly_Over_List.txt");
                ArrayList<String> under = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison
                        + File.separator + comparison + "_GEonly_Under_List.txt");
                for (String gene : over) {
                    diffExprTable.setValue(1, gene, comparison);
                }
                for (String gene : under) {
                    diffExprTable.setValue(-1, gene, comparison);
                }

                /*
                 * sRNA
                 */
                over = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison + File.separator
                        + comparison + "_Srna_Over_List.txt");
                under = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison + File.separator
                        + comparison + "_Srna_Under_List.txt");
                for (String gene : over) {
                    diffExprTable.setValue(1, gene, comparison);
                }
                for (String gene : under) {
                    diffExprTable.setValue(-1, gene, comparison);
                }

                /*
                 * asRNA
                 */
                over = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison + File.separator
                        + comparison + "_ASrna_Over_List.txt");
                under = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison + File.separator
                        + comparison + "_ASrna_Under_List.txt");
                for (String gene : over) {
                    diffExprTable.setValue(1, gene, comparison);
                }
                for (String gene : under) {
                    diffExprTable.setValue(-1, gene, comparison);
                }
                /*
                 * cisRegs
                 */
                over = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison + File.separator
                        + comparison + "_CisReg_Over_List.txt");
                under = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison + File.separator
                        + comparison + "_CisReg_Under_List.txt");
                for (String gene : over) {
                    diffExprTable.setValue(1, gene, comparison);
                }
                for (String gene : under) {
                    diffExprTable.setValue(-1, gene, comparison);
                }
            }
        }

        diffExprTable.saveTab(Comparison.PATH_DIFF_EXPR + ".txt", "Gene");
    }

    /**
     * Create an array :<br>
     * <li>Row = comparison
     * <li>Column = Number of gene, Srna Asrna over or under-expressed<br>
     * <br>
     * Save it in : Comparison.PATH_DIFF_LISTS+".txt"
     */
    public static void summarizeDiffLists(Experiment exp) {
        ArrayList<String> comparisons = new ArrayList<>();
        for (BioCondition bioCond : exp.getBioConditions()) {
            for (String comparison : bioCond.getComparisonNames()) {
                comparisons.add(comparison);
            }
        }
        String[] header = {"Gene Diff expressed", "Gene Over", "Gene Under", "Srna Over", "Srna Under", "ASrna Over",
                "ASrna Under", "CisReg Over", "CisReg Under"};
        String[][] summary = new String[comparisons.size() + 1][header.length];
        for (int k = 0; k < header.length; k++) {
            summary[0][k] = header[k];
        }
        int i = 1;
        for (String comparison : comparisons) {
            String[] row = {comparison, "", "", "", "", "", "", "", ""};
            for (int k = 0; k < header.length; k++) {
                summary[i][k] = row[k];
            }
            ArrayList<String> over = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison
                    + File.separator + comparison + "_Gene_Over_List.txt");
            ArrayList<String> under = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison
                    + File.separator + comparison + "_Gene_Under_List.txt");
            ArrayList<String> overSrna = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison
                    + File.separator + comparison + "_Srna_Over_List.txt");
            ArrayList<String> underSrna = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison
                    + File.separator + comparison + "_Srna_Under_List.txt");
            ArrayList<String> overASrna = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/" + comparison
                    + File.separator + comparison + "_ASrna_Over_List.txt");
            ArrayList<String> underASrna = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/"
                    + comparison + File.separator + comparison + "_ASrna_Under_List.txt");
            ArrayList<String> overCisReg = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/"
                    + comparison + File.separator + comparison + "_ASrna_Over_List.txt");
            ArrayList<String> underCisReg = TabDelimitedTableReader.readList(OmicsData.PATH_COMPARISONS + "/"
                    + comparison + File.separator + comparison + "_ASrna_Under_List.txt");
            String[] newRow = {comparison, over.size() + "", under.size() + "", overSrna.size() + "",
                    underSrna.size() + "", overASrna.size() + "", underASrna.size() + "", overCisReg.size() + "",
                    underCisReg.size() + ""};
            for (int k = 0; k < newRow.length; k++) {
                summary[i][k] = newRow[k];
            }
            i++;
        }
        TabDelimitedTableReader.save(summary, Comparison.PATH_DIFF_LISTS + ".txt");
    }

}
