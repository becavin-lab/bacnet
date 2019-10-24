package bacnet.scripts.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.ProteomicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.VectorUtils;
import bacnet.utils.XMLUtils;

public class BioConditionCreation {

    /**
     * Read a Table summarizing all BioCondition and parse every information available<br>
     * Save in BioCondition.PATH every <code>BioCondition</code> created
     * 
     * @param fileName
     * @throws IOException
     */
    public static void addExperimentFromTable(String fileName) {
        addExperimentFromTable(fileName, new ArrayList<>(), "");
    }

    /**
     * Read a Table summarizing all BioCondition and parse every information available<br>
     * Save in BioCondition.PATH every <code>BioCondition</code> created
     * 
     * @param fileName
     * @param listBioCondsToCreates if you do not want to recreate all bioconditions
     * @throws IOException
     */
    public static String addExperimentFromTable(String fileName, ArrayList<String> listBioCondsToCreates, String logs) {

        // Read the table containing Experimental Design
        String[][] expDesignTable = TabDelimitedTableReader.read(fileName);

        HashMap<String, String[][]> expTablesTemp = extractBioCondition(expDesignTable);

        /*
         * Remove biological conditions you do not want to recreate
         */
        HashMap<String, String[][]> expTables = new HashMap<>();
        if (listBioCondsToCreates.size() > 0) {
            for (String bioCondName : listBioCondsToCreates) {
                expTables.put(bioCondName, expTablesTemp.get(bioCondName));
            }
        } else {
            expTables = expTablesTemp;
        }

        Experiment generalExp = new Experiment();
        generalExp.setName(Database.getInstance().getProjectName());

        for (String bioCondName : expTables.keySet()) {
            String[][] expTable = expTables.get(bioCondName);
            if (expTable[0].length == 0) {
                System.out.println("problem no Data found for " + bioCondName);
                logs += "Problem no Data found for " + bioCondName + "\n";
            } else {
                BioCondition bioCond = createBioCondFromTable(bioCondName, expTable);
                generalExp.addBioCond(bioCond);
                logs += bioCondName + " created\n";
            }
        }
        // generalExp.save(Experiment.GENERAL_EXP_PATH);

        /*
         * Save all BioCondition
         */
        File path = new File(Database.getBIOCONDITION_PATH());
        path.mkdirs();
        for (String name : generalExp.getBioConds().keySet()) {
            BioCondition bio = generalExp.getBioConds().get(name);
            bio.save(Database.getBIOCONDITION_PATH() + name);
            logs += name + " saved\n";
        }

        // /*
        // * Save bioCondition in XML to control
        // */
//        for (String name : generalExp.getBioConds().keySet()) {
//            BioCondition bio = generalExp.getBioConds().get(name);
//            XMLUtils.encodeToFile(bio, Database.getBIOCONDITION_PATH() + name + ".xml");
//        }
        return logs;
    }

    /**
     * Read the table summarizing all the experiments done, and separate in several table according to
     * BioCondName = 4th column
     * 
     * @param expDesignTable
     * @return ArrayList of tables
     */
    private static HashMap<String, String[][]> extractBioCondition(String[][] expDesignTable) {
        HashMap<String, String[][]> bioConds = new HashMap<String, String[][]>();
        for (int i = 1; i < expDesignTable.length; i++) {
            String bioCond = expDesignTable[i][ArrayUtils.findColumn(expDesignTable, "BioCondName")];
            if (!bioConds.containsKey(bioCond)) {
                /*
                 * Go through expDesignTable and regroup all rows with same bioCondName
                 */
                ArrayList<String> tableList = new ArrayList<String>();
                tableList.add(VectorUtils.toString(ArrayUtils.getRow(expDesignTable, 0)));
                for (int k = 1; k < expDesignTable.length; k++) {
                    String bioCondTemp = expDesignTable[k][ArrayUtils.findColumn(expDesignTable, "BioCondName")];
                    if (bioCond.equals(bioCondTemp)) {
                        /*
                         * Copy row to a String
                         */
                        tableList.add(VectorUtils.toString(ArrayUtils.getRow(expDesignTable, k)));
                    }
                }
                /*
                 * Transform list into a table
                 */
                File tempFile;
                try {
                    tempFile = File.createTempFile("list", "table");
                    TabDelimitedTableReader.saveList(tableList, tempFile.getAbsolutePath());
                    String[][] table = TabDelimitedTableReader.read(tempFile);
                    bioConds.put(bioCond, table);
                    FileUtils.forceDelete(tempFile);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Found " + bioConds.size() + " bioCondition");
        return bioConds;
    }

    /**
     * From bioCondTable we extract the different Data using the many columns available in the Table
     * 
     * @param bioCondTable
     * @return
     */
    private static BioCondition createBioCondFromTable(String name, String[][] bioCondTable) {
        /*
         * set BioCond parameters
         */
        BioCondition bioCond = new BioCondition();
        bioCond.setName(name);
        bioCond.setGenomeName(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Strain array")]);
        bioCond.setGenomeUsed(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Strain used")]);
        bioCond.setComment(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Comment")]);
        bioCond.setReference(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Reference")]);
        /*
         * Study Name
         */
        if (ArrayUtils.findColumn(bioCondTable, "StudyName") != -1) {
            bioCond.setArrayExpressId(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "StudyName")]);
        }

        /*
         * ArrayExpress information
         */
        if (ArrayUtils.findColumn(bioCondTable, "ArrayExpressId") != -1) {
            bioCond.setArrayExpressId(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "ArrayExpressId")]);
        }
        /*
         * ArrayExpress Technology ID
         */
        if (ArrayUtils.findColumn(bioCondTable, "arrayExpressTechnoID") != -1) {
            bioCond.setArrayExpressTechnoId(
                    bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "arrayExpressTechnoID")]);
        }

        /*
         * Localization of the proteins if available
         */
        if (ArrayUtils.findColumn(bioCondTable, "Localization") != -1) {
            String[] mutants = bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Localization")].split(";");
            for (String mutant : mutants) {
                bioCond.getLocalization().add(mutant);
            }
        }
        /*
         * Mutant
         */
        if (!bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Mutant")].equals("")) {
            String[] mutants = bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Mutant")].split(";");
            for (String mutant : mutants) {
                bioCond.getMutant().add(mutant);
            }
        }
        /*
         * Growth phase
         */
        if (!bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Growth")].equals("")) {
            String[] growths = bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Growth")].split(";");
            for (String growth : growths) {
                bioCond.getGrowth().add(growth);
            }
        } else {
            bioCond.getGrowth().add("Exp phase");
        }
        /*
         * Temperature
         */
        if (ArrayUtils.findColumn(bioCondTable, "Temperature") != -1) {
            if (!bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Temperature")].equals("")) {
                bioCond.setTemperature(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Temperature")]);
            }
        }
        /*
         * Time point if needed
         */
        if (ArrayUtils.findColumn(bioCondTable, "TimePoint") != -1) {
            bioCond.setTime(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "TimePoint")]);
        }

        /*
         * Type of Media and Drugs added to it
         */
        if (!bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Media")].equals("")) {
            String[] medium = bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Media")].split(";");
            for (String media : medium) {
                bioCond.getMedia().add(media);
            }
        } else {
            bioCond.getMedia().add("BHI Broth");
        }
        /*
         * Properties or quantity of the media and growth
         */
        if (ArrayUtils.findColumn(bioCondTable, "MediaGrowthProperties") != -1) {
            String[] medium = bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "MediaGrowthProperties")].split(";");
            for (String media : medium) {
                bioCond.getMediaGrowthProperties().add(media);
            }
        }
        /*
         * For Cossart data, the ID of the experiment
         */
        if (ArrayUtils.findColumn(bioCondTable, "ExpID") != -1) {
            if (!bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "ExpID")].equals("")) {
                bioCond.setExperienceNb(
                        Integer.parseInt(bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "ExpID")]));
            }
        }
        /*
         * If No Data are available
         */
        if (ArrayUtils.findColumn(bioCondTable, "NoDataAvailable") != -1) {
            if (!bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "NoDataAvailable")].equals("")) {
                bioCond.setNoData(true);
            }
        }
        /*
         * Date of publication or creation
         */
        String date = bioCondTable[1][ArrayUtils.findColumn(bioCondTable, "Date")];
        bioCond.setDate(date);
        System.out.println("Creating BioCondition: " + bioCond.getName());

        /*
         * go through the table and create data
         */
        if (!bioCond.isNoData()) {
            for (int i = 1; i < bioCondTable.length; i++) {
                TypeData dataType =
                        TypeData.valueOf(bioCondTable[i][ArrayUtils.findColumn(bioCondTable, "Technology")]);
                String rawDataName = bioCondTable[i][ArrayUtils.findColumn(bioCondTable, "FileName")];
                date = bioCondTable[i][ArrayUtils.findColumn(bioCondTable, "Date")];
                bioCond.getTypeDataContained().add(dataType);
                if (!rawDataName.equals("")) {
                    if (dataType == TypeData.ExpressionMatrix) {
                        boolean found = false;
                        for (ExpressionMatrix matrix : bioCond.getMatrices()) {
                            if (matrix.getName().equals(name)) {
                                matrix.getRawDatas().add(rawDataName);
                                found = true;
                            }
                        }
                        if (!found) {
                            ExpressionMatrix matrix = new ExpressionMatrix();
                            matrix.setName(name);
                            matrix.setDate(date);
                            matrix.getRawDatas().add(rawDataName);
                            bioCond.getMatrices().add(matrix);
                        }
                    } else if (dataType == TypeData.GeneExpr) {
                        String nameGeneExpr = bioCond.getName() + GeneExpression.EXTENSION;
                        boolean found = false;
                        for (GeneExpression geneExpr : bioCond.getGeneExprs()) {
                            if (geneExpr.getName().equals(nameGeneExpr)) {
                                geneExpr.getRawDatas().add(rawDataName);
                                found = true;
                            }
                        }
                        if (!found) {
                            GeneExpression geneExprData = new GeneExpression();
                            geneExprData.setName(nameGeneExpr);
                            geneExprData.setDate(date);
                            geneExprData.getRawDatas().add(rawDataName);
                            bioCond.getGeneExprs().add(geneExprData);
                        }

                    } else if (dataType == TypeData.Tiling) {
                        String nameTilingPlus = bioCond.getName() + ".+" + Tiling.EXTENSION;
                        String nameTilingMinus = bioCond.getName() + ".-" + Tiling.EXTENSION;
                        boolean found = false;
                        for (Tiling tiling : bioCond.getTilings()) {
                            if (tiling.getName().equals(nameTilingPlus) || tiling.getName().equals(nameTilingMinus)) {
                                tiling.getRawDatas().add(rawDataName);
                                found = true;
                            }
                        }
                        if (!found) {
                            Tiling tilingDataPlus = new Tiling();
                            Tiling tilingDataMinus = new Tiling();;
                            tilingDataPlus.setName(nameTilingPlus);
                            tilingDataMinus.setName(nameTilingMinus);
                            tilingDataPlus.setDate(date);
                            tilingDataMinus.setDate(date);
                            tilingDataPlus.getRawDatas().add(rawDataName);
                            tilingDataMinus.getRawDatas().add(rawDataName);
                            bioCond.getTilings().add(tilingDataPlus);
                            bioCond.getTilings().add(tilingDataMinus);
                        }

                    } else if (dataType == TypeData.RNASeq || dataType == TypeData.DNASeq
                            || dataType == TypeData.RiboSeq || dataType == TypeData.TermSeq
                            || dataType == TypeData.TSS) {
                        Genome genome = Genome.loadGenome(bioCond.getGenomeName());
                        if (rawDataName.contains("_f") && rawDataName.contains(".wig")) {
                            String nameNGSPlus = bioCond.getName() + "_f";
                            String nameNGSMinus = bioCond.getName() + "_r";
                            boolean found = false;
                            /*
                             * If different rawdata are available (replicates) we will not create a new dataset
                             */
                            for (NGS ngs : bioCond.getNGSSeqs()) {
                                if (ngs.getName().equals(nameNGSPlus)) {
                                    ngs.getRawDatas().add(rawDataName);
                                    found = true;
                                } else if (ngs.getName().equals(nameNGSMinus)) {
                                    rawDataName = rawDataName.replaceFirst("_f", "_r");
                                    ngs.getRawDatas().add(rawDataName);
                                    found = true;
                                }
                            }
                            if (!found) {
                                NGS rnaSeqPlus = new NGS(nameNGSPlus, bioCond.getGenomeName());
                                rnaSeqPlus.setDate(date);
                                rnaSeqPlus.setType(dataType);
                                NGS rnaSeqMinus = new NGS(nameNGSMinus, bioCond.getGenomeName());
                                rnaSeqMinus.setDate(date);
                                rnaSeqMinus.setType(dataType);
                                for (String chromoID : genome.getChromosomes().keySet()) {
                                    ExpressionData datasetPlus = new ExpressionData(nameNGSPlus);
                                    datasetPlus.setType(dataType);
                                    datasetPlus.setDate(date);
                                    datasetPlus.setChromosomeID(chromoID);
                                    datasetPlus.setGenomeName(bioCond.getGenomeName());
                                    rnaSeqPlus.getDatasets().put(chromoID, datasetPlus);
                                    ExpressionData datasetMinus = new ExpressionData(nameNGSMinus);
                                    rnaSeqMinus.getDatasets().put(chromoID, datasetMinus);
                                    datasetMinus.setType(dataType);
                                    datasetMinus.setDate(date);
                                    datasetMinus.setChromosomeID(chromoID);
                                    datasetMinus.setGenomeName(bioCond.getGenomeName());
                                }
                                bioCond.getNGSSeqs().add(rnaSeqPlus);
                                bioCond.getNGSSeqs().add(rnaSeqMinus);
                                rnaSeqPlus.getRawDatas().add(rawDataName);
                                rawDataName = rawDataName.replaceFirst("_f", "_r");
                                rnaSeqMinus.getRawDatas().add(rawDataName);
                            }
                        } else if (rawDataName.contains("_nostrand")) {
                            String nameNGSNOStrand = bioCond.getName();
                            boolean found = false;
                            for (NGS ngs : bioCond.getNGSSeqs()) {
                                if (ngs.getName().equals(nameNGSNOStrand)) {
                                    ngs.getRawDatas().add(rawDataName);
                                    found = true;
                                }
                            }
                            if (!found) {
                                NGS rnaSeqPlus = new NGS(nameNGSNOStrand, bioCond.getGenomeName());
                                rnaSeqPlus.setDate(date);
                                rnaSeqPlus.setType(dataType);
                                for (String chromoID : genome.getChromosomes().keySet()) {
                                    System.out.println("chromoID" + chromoID);
                                    ExpressionData datasetPlus = new ExpressionData(nameNGSNOStrand);
                                    datasetPlus.setType(dataType);
                                    datasetPlus.setChromosomeID(chromoID);
                                    datasetPlus.setDate(date);
                                    datasetPlus.setGenomeName(bioCond.getGenomeName());
                                    rnaSeqPlus.getDatasets().put(chromoID, datasetPlus);
                                }
                                rnaSeqPlus.getRawDatas().add(rawDataName);
                                bioCond.getNGSSeqs().add(rnaSeqPlus);
                            }

                        } else {
                            System.err.println("Could not find if NGS is single-end or pairend-end."
                                    + " Wig files should be named: file_f.wig or file_nostrand.wig");
                        }
                    } else if (dataType == TypeData.NTerm) {
                        NTermData massSpec = new NTermData();
                        massSpec.setName(rawDataName);
                        massSpec.setDate(date);
                        massSpec.setType(dataType);
                        bioCond.getnTerms().add(massSpec);
                    } else if (dataType == TypeData.Proteome) {
                        ProteomicsData matrix = new ProteomicsData();
                        matrix.setName(name);
                        matrix.setDate(date);
                        matrix.setType(dataType);
                        matrix.getRawDatas().add(rawDataName);
                        bioCond.getProteomes().add(matrix);
                    }
                }
            }
        }

        for (OmicsData data : bioCond.getOmicsData()) {
            System.out.println("add:" + data.getName() + "   " + data.getType());
            bioCond.getTypeDataContained().add(data.getType());
        }

        // set Parent BioCond for all
        for (OmicsData exprData : bioCond.getOmicsData()) {
            exprData.setBioCondName(bioCond.getName());
        }
        for (NGS ngs : bioCond.getNGSSeqs()) {
            ngs.setBioCondName(bioCond.getName());
        }
        return bioCond;
    }

    /**
     * Read a Table and construct the list of Comparisons from it
     * 
     * @param fileName
     * @throws IOException
     */
    public static void addComparisonFromTable(String fileName) {
        ArrayList<String> comparisons = TabDelimitedTableReader.readList(fileName, true);
        ArrayList<String> listCompsToCreates = new ArrayList<>();
        for (String line : comparisons) {
            String comp = line.split("\t")[0] + BioCondition.SEPARATOR + line.split("\t")[1];
            listCompsToCreates.add(comp);
        }
        addComparisonFromTable(listCompsToCreates, "");
    }

    /**
     * Read a Table and construct the list of Comparisons from it
     * 
     * @param fileName
     * @param listCompsToCreates if you do not want to recreate all comparisons
     * @throws IOException
     */
    public static String addComparisonFromTable(ArrayList<String> listCompsToCreates, String logs) {
        /*
         * Update list of Comparison in each BioCondition
         */
        for (String comparison : listCompsToCreates) {
            String leftBC = BioCondition.parseName(comparison)[0];
            String rightBC = BioCondition.parseName(comparison)[1];
            BioCondition bioCond1 = BioCondition.getBioCondition(leftBC);
            BioCondition bioCond2 = BioCondition.getBioCondition(rightBC);
            if (bioCond1 == null) {
                System.err.println("Could not find: " + leftBC);
            } else {
                if (!bioCond1.getComparisons().contains(rightBC)) {
                    bioCond1.getComparisons().add(rightBC);
                    bioCond1.save(Database.getBIOCONDITION_PATH() + leftBC);
                    XMLUtils.encodeToFile(bioCond1, Database.getBIOCONDITION_PATH() + leftBC + ".xml");
                    System.out.println("Saved: " + leftBC + " after adding comparison: " + rightBC);
                }
            }
            if (bioCond2 == null) {
                System.err.println("Could not find: " + leftBC);
            } else {
                if (!bioCond2.getAntiComparisons().contains(rightBC)) {
                    bioCond2.getAntiComparisons().add(leftBC);
                    bioCond2.save(Database.getBIOCONDITION_PATH() + rightBC);
                    XMLUtils.encodeToFile(bioCond2, Database.getBIOCONDITION_PATH() + rightBC + ".xml");
                    System.out.println("Saved: " + leftBC + " after adding comparison: " + rightBC);
                }
            }
        }
        return logs;
    }

    /**
     * Create a table will all main information on the different BioCondition available<br>
     * This table will be loaded by BioConditionView
     */
    public static void createSummaryTranscriptomesTable() {
        ArrayList<String> tableResult = new ArrayList<>();
        String[] titles =
                {"Data Name", "Type", "Date", "Growth", "TimePoint", "Temp.", "Mutant", "Media", "MediaGrowthProperties", "Nb genes",
                        "Strain used", "Strain array", "Reference", "ArrayExpressId", "ArrayExpressTechnoID"};
        String header = "";
        for (String title : titles)
            header += title + "\t";
        tableResult.add(header.trim());

        for (BioCondition bioCondition : BioCondition.getAllBioConditions()) {
            if (!bioCondition.getTypeDataContained().contains(TypeData.Proteome)
                    && !bioCondition.getTypeDataContained().contains(TypeData.NTerm)) {
                String row = "";
                row += bioCondition.getName() + "\t";
                String typeDatacontained =
                        bioCondition.getTypeDataContained().toString().replace('[', ' ').replace(']', ' ');
                if (bioCondition.getTypeDataContained().size() == 0)
                    typeDatacontained = "GeneExpr";
                if (typeDatacontained.contains("ExpressionMatrix"))
                    typeDatacontained = typeDatacontained.replaceAll("ExpressionMatrix", "GeneExpr");
                row += typeDatacontained.trim() + "\t";
                row += bioCondition.getDate() + "\t";
                row += bioCondition.getGrowth().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                row += bioCondition.getTime() + "\t";
                row += bioCondition.getTemperature().toString().replace('[', ' ').replace(']', ' ').replace('C', ' ')
                        .trim() + "\t";
                row += bioCondition.getMutant().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                row += bioCondition.getMedia().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                row += bioCondition.getMediaGrowthProperties().toString().replace('[', ' ').replace(']', ' ').trim()
                        + "\t";
                row += "\t";
                row += bioCondition.getGenomeUsed() + "\t";
                row += bioCondition.getGenomeName() + "\t";
                row += bioCondition.getReference() + "\t";
                row += bioCondition.getArrayExpressId() + "\t";
                row += bioCondition.getArrayExpressTechnoId();
                tableResult.add(row.trim());
            }

        }
        TabDelimitedTableReader.saveList(tableResult, Database.getInstance().getTranscriptomesArrayPath());
    }

    /**
     * Create a table will all main information on the different BioCondition available<br>
     * This table will be loaded by BioConditionView
     */
    public static void createSummaryTranscriptomesComparisonsTable() {
        ArrayList<String> tableResult = new ArrayList<>();
        String[] titles = {"Data Name", "Growth", "Temp.", "Mutant", "Media", "MediaGrowthProperties", "VS", "Growth",
                "�C", "Mutant", "MediaGrowthProperties", "Media", "Type", "ArrayExpressId", "Date", "Strain used", "Strain array",
                "Reference"};
        String header = "";
        for (String title : titles)
            header += title + "\t";
        tableResult.add(header.trim());

        for (BioCondition bioCondition : BioCondition.getAllBioConditions()) {
            if (!bioCondition.getTypeDataContained().contains(TypeData.Proteome)
                    && !bioCondition.getTypeDataContained().contains(TypeData.NTerm)) {
                for (String comparison : bioCondition.getComparisonNames()) {
                    String row = "";
                    row += comparison + "\t";
                    row += bioCondition.getGrowth().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                    row += bioCondition.getTemperature().toString().replace('[', ' ').replace(']', ' ')
                            .replace('C', ' ').trim() + "\t";
                    row += bioCondition.getMutant().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                    row += bioCondition.getMedia().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                    row += bioCondition.getMediaGrowthProperties().toString().replace('[', ' ').replace(']', ' ').trim()
                            + "\t";
                    row += "VS" + "\t";
                    bioCondition = BioCondition.getBioCondition(BioCondition.parseName(comparison)[1]);
                    row += bioCondition.getGrowth().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                    row += bioCondition.getTemperature().toString().replace('[', ' ').replace(']', ' ')
                            .replace('C', ' ').trim() + "\t";
                    row += bioCondition.getMutant().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                    row += bioCondition.getMedia().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                    row += bioCondition.getMediaGrowthProperties().toString().replace('[', ' ').replace(']', ' ').trim()
                            + "\t";
                    String typeDatacontained =
                            bioCondition.getTypeDataContained().toString().replace('[', ' ').replace(']', ' ');
                    if (bioCondition.getTypeDataContained().size() == 0)
                        typeDatacontained = "GeneExpr";
                    if (typeDatacontained.contains("ExpressionMatrix"))
                        typeDatacontained = typeDatacontained.replaceAll("ExpressionMatrix", "GeneExpr");
                    // typeDatacontained = typeDatacontained.replaceAll("GeneExpr",
                    // "GeneExpression");
                    row += typeDatacontained.trim() + "\t";
                    row += bioCondition.getArrayExpressId() + "\t";
                    row += bioCondition.getDate() + "\t";
                    row += bioCondition.getGenomeUsed() + "\t";
                    row += bioCondition.getGenomeName() + "\t";

                    row += bioCondition.getReference() + "\t";
                    tableResult.add(row.trim());
                }

            }
        }
        TabDelimitedTableReader.saveList(tableResult, Database.getInstance().getTranscriptomesComparisonsArrayPath());
    }

    /**
     * Create a table will all main information on the different BioCondition available<br>
     * This table will be loaded by BioConditionView
     */
    public static void createSummaryProteomesTable() {
        ArrayList<String> tableResult = new ArrayList<>();
        String[] titles = {"Data Name", "Localization", "Type", "Date", "Nb proteins", "Comment", "Growth", "TimePoint",
                "Temp.", "Mutant", "Media", "MediaGrowthProperties", "Pride Id", "Reference", "Strain used", "Strain array",};
        String header = "";
        for (String title : titles)
            header += title + "\t";
        tableResult.add(header.trim());

        for (BioCondition bioCondition : BioCondition.getAllBioConditions()) {
            String row = "";
            System.out.println(bioCondition.getName());
            System.out.println(bioCondition.getTypeDataContained());
            if (bioCondition.getTypeDataContained().contains(TypeData.Proteome)
                    || bioCondition.getTypeDataContained().contains(TypeData.NTerm)) {
                row += bioCondition.getName() + "\t";
                row += bioCondition.getLocalization().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                String typeDatacontained =
                        bioCondition.getTypeDataContained().toString().replace('[', ' ').replace(']', ' ');
                row += typeDatacontained.trim() + "\t";
                row += bioCondition.getDate() + "\t";
                row += " \t";
                row += bioCondition.getComment() + "\t";
                row += bioCondition.getGrowth().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                row += bioCondition.getTime() + "\t";
                row += bioCondition.getTemperature().toString().replace('[', ' ').replace(']', ' ').replace('C', ' ')
                        .trim() + "\t";
                row += bioCondition.getMutant().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                row += bioCondition.getMedia().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
                row += bioCondition.getMediaGrowthProperties().toString().replace('[', ' ').replace(']', ' ').trim()
                        + "\t";
                row += bioCondition.getArrayExpressId() + "\t";
                row += bioCondition.getReference() + "\t";
                row += bioCondition.getGenomeUsed() + "\t";
                row += bioCondition.getGenomeName() + "\t";
                tableResult.add(row.trim());
            }
        }
        TabDelimitedTableReader.saveList(tableResult, Database.getInstance().getProteomesArrayPath());
    }

    /**
     * Read Array in expDesignTableFile and search which Raw data is missing<br>
     * Save missing .CEL and .txt files in three lists:
     * <li>NotFoundCEl GeneExpr.txt
     * <li>NotFoundCEl Tiling.txt
     * <li>NotFoundCEl RNASeq.txt
     */
    public static void findMissingCELfilesListeria() {
        ArrayList<String> allGeneExprCEL = new ArrayList<>();
        File file = new File(OmicsData.PATH_GENEXPR_RAW);
        for (String name : file.list()) {
            allGeneExprCEL.add(name);
            // System.out.println(name);
        }
        ArrayList<String> allTilingCEL = new ArrayList<>();
        File file2 = new File(OmicsData.PATH_TILING_RAW);
        for (String name : file2.list()) {
            allTilingCEL.add(name);
            // System.out.println(name);
        }
        ArrayList<String> allRNASeq = new ArrayList<>();
        File file3 = new File(OmicsData.PATH_NGS_RAW);
        for (String name : file3.list()) {
            allRNASeq.add(name);
            // System.out.println(name);
        }
        ArrayList<String> notFoundGeneExpr = new ArrayList<>();
        ArrayList<String> notFoundTiling = new ArrayList<>();
        ArrayList<String> notFoundRNASeq = new ArrayList<>();

        String[][] array = TabDelimitedTableReader.read(Database.getInstance().getBioConditionsArrayPath());
        for (int i = 1; i < array.length; i++) {
            if (array[i][ArrayUtils.findColumn(array, "Technology")].equals("Expression")) {
                if (!allGeneExprCEL.contains(array[i][ArrayUtils.findColumn(array, "FileName")]))
                    notFoundGeneExpr.add(array[i][ArrayUtils.findColumn(array, "FileName")]);
            } else if (array[i][ArrayUtils.findColumn(array, "Technology")].equals("Tiling")) {
                if (!allTilingCEL.contains(array[i][ArrayUtils.findColumn(array, "FileName")]))
                    notFoundTiling.add(array[i][ArrayUtils.findColumn(array, "FileName")]);
            } else {
                if (!allRNASeq.contains(array[i][ArrayUtils.findColumn(array, "FileName")]))
                    notFoundRNASeq.add(array[i][ArrayUtils.findColumn(array, "FileName")]);
            }
        }

        TabDelimitedTableReader.saveList(notFoundGeneExpr, Database.getDATA_PATH() + "NotFoundCEl GeneExpr.txt");
        TabDelimitedTableReader.saveList(notFoundTiling, Database.getDATA_PATH() + "NotFoundCEl Tiling.txt");
        TabDelimitedTableReader.saveList(notFoundRNASeq, Database.getDATA_PATH() + "NotFoundCEl RNASeq.txt");

    }

}
