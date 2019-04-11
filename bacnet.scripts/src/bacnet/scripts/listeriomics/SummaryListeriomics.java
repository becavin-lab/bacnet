package bacnet.scripts.listeriomics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.CMD;

/**
 * Methods for creating the different figures and Tables of the paper
 * 
 * @author UIBC
 *
 */
@SuppressWarnings("unused")
public class SummaryListeriomics {

    public static String DATA_PATH = Database.getInstance().getPath() + "Listeriomics";

    public static void run() {

        createGenomeTable();
        // createSrnaConservationFigure();
        // createBioConditionFigure(true);
        // createSummaryNetwork();
        // createSummaryNetworkProteome();
        createBioConditionFigure(false);
        // createSummaryRNASeq();
        // geneFoldChangeStatistics();
        // geneAbsoluteExpressionStatistics();
        // sRNAExpressionStatistics();
    }

    /**
     * Save a table with the expression in each dataset of all the genes of EGD-e
     */
    private static void geneAbsoluteExpressionStatistics() {
        /*
         * Create list of data
         */
        ArrayList<String> bioCondFiltered = new ArrayList<String>();
        for (BioCondition bioCond : BioCondition.getAllBioConditions()) {
            String datatype = "";
            if (!bioCond.getReference().contains("Unpublished")) {
                if (bioCond.getTypeDataContained().contains(TypeData.RNASeq)) {
                    datatype = "[RNASeq]";
                } else if (bioCond.getTypeDataContained().contains(TypeData.ExpressionMatrix)) {
                    datatype = "[ExpressionMatrix]";
                } else if (bioCond.getTypeDataContained().contains(TypeData.Tiling)) {
                    datatype = "[Tiling]";
                } else if (bioCond.getTypeDataContained().contains(TypeData.GeneExpr)) {
                    datatype = "[GeneExpr]";
                }

                if (!datatype.equals("")) {
                    System.out.println(bioCond.getName() + datatype);
                    bioCondFiltered.add(bioCond.getName() + datatype);
                }
            }
        }

        /*
         * filter table
         */
        ExpressionMatrix matrix = ExpressionMatrix.loadTab(
                Database.getTRANSCRIPTOMES_PATH() + "Expression/AllDataType_" + Genome.EGDE_NAME + ".excel", false);
        bioCondFiltered.addAll(matrix.getHeaderAnnotation());
        matrix = matrix.getSubMatrixColumn(bioCondFiltered);
        matrix.saveTab(Database.getTRANSCRIPTOMES_PATH() + "Expression/AllDataType_" + Genome.EGDE_NAME + "_Lite.excel",
                "locus");

    }

    /**
     * Save a table with the expression in each dataset of all the genes of EGD-e
     */
    private static void sRNAExpressionStatistics() {
        /*
         * Create list of data
         */
        ArrayList<String> bioCondFiltered = new ArrayList<String>();
        ArrayList<String> compFiltered = new ArrayList<String>();
        for (BioCondition bioCond : BioCondition.getAllBioConditions()) {
            String datatype = "";
            // if(!bioCond.getReference().contains("Unpublished")){
            // if(bioCond.getTypeDataContained().contains(TypeData.RNASeq)){
            // datatype = "[RNASeq]";
            // //}else
            // if(bioCond.getTypeDataContained().contains(TypeData.ExpressionMatrix)){
            // // datatype = "[ExpressionMatrix]";
            // }else
            if (bioCond.getTypeDataContained().contains(TypeData.Tiling)) {
                datatype = "[Tiling]";
                for (String comp : bioCond.getComparisonNames()) {
                    compFiltered.add(comp);
                }
                // }else if(bioCond.getTypeDataContained().contains(TypeData.GeneExpr)){
                // datatype = "[GeneExpr]";
            }

            if (!datatype.equals("")) {
                System.out.println(bioCond.getName() + datatype);
                bioCondFiltered.add(bioCond.getName() + datatype);
            }
            // }
        }

        /*
         * Load sRNA info
         */
        Database.initDatabase(Database.LISTERIOMICS_PROJECT);
        ArrayList<String> sRNAs = Database.getInstance().getsRNAListEGDe();
        System.out.println(sRNAs.size());

        /*
         * filter table Expr
         */
        ExpressionMatrix exprMatrix = ExpressionMatrix.loadTab(
                Database.getTRANSCRIPTOMES_PATH() + "Expression/AllDataType_" + Genome.EGDE_NAME + ".excel", false);
        bioCondFiltered.addAll(exprMatrix.getHeaderAnnotation());
        exprMatrix = exprMatrix.getSubMatrix(sRNAs, bioCondFiltered);
        exprMatrix.setOrdered(true, Database.getInstance().getsRNAListEGDe());
        exprMatrix.saveTab(DATA_PATH + "/Expression/SrnaExpression_" + Genome.EGDE_NAME + "_PRIVATE.excel", "sRNA");

        /*
         * Filter comparison table
         */

        ExpressionMatrix logFCMatrix = ExpressionMatrix
                .loadTab(Database.getTRANSCRIPTOMES_PATH() + "Table_LOGFC_" + Genome.EGDE_NAME + ".excel", false);
        compFiltered.addAll(exprMatrix.getHeaderAnnotation());
        logFCMatrix = logFCMatrix.getSubMatrix(sRNAs, compFiltered);
        double[] foldChange = new double[logFCMatrix.getNumberRow()];
        for (String genomeElement : logFCMatrix.getRowNamesToList()) {
            int sumDiffExpr = 0;
            for (String bioCondName : logFCMatrix.getHeaders()) {
                double logFC = logFCMatrix.getValue(genomeElement, bioCondName);
                if (Math.abs(logFC) >= 2) {
                    sumDiffExpr++;
                }
            }
            foldChange[logFCMatrix.getRowNames().get(genomeElement)] = sumDiffExpr;
        }
        logFCMatrix.addColumn("DiffExpr", foldChange);
        logFCMatrix.setOrdered(true, Database.getInstance().getsRNAListEGDe());
        logFCMatrix.saveTab(DATA_PATH + "/Expression/SrnaComparison_" + Genome.EGDE_NAME + "_PRIVATE.excel", "sRNA");

    }

    /**
     * Go through all transcriptomics data and calculate in how many dataset their expression change for
     * each gene and COG cluster
     */
    private static void geneFoldChangeStatistics() {
        ExpressionMatrix matrix =
                ExpressionMatrix.load(Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + Genome.EGDE_NAME);
        ArrayList<String> result = new ArrayList<String>();
        result.add("Name\tCount\ttotal");
        Genome genome = Genome.loadEgdeGenome();
        HashMap<String, Integer> cogmap = new HashMap<String, Integer>();
        HashMap<String, Integer> totalCogmap = new HashMap<String, Integer>();

        for (String locus : matrix.getRowNames().keySet()) {
            int count = 0;
            System.out.println(locus);
            Gene gene = genome.getGeneFromName(locus);

            for (String header : matrix.getHeaders()) {
                double value = matrix.getValue(locus, header);
                value = Math.abs(value);
                if (gene != null) {
                    String cog = gene.getCog();
                    if (cog.contains(";")) {
                        for (String cogTemp : cog.split(";")) {
                            cogTemp = cogTemp.trim();
                            if (totalCogmap.containsKey(cogTemp)) {
                                totalCogmap.put(cogTemp, totalCogmap.get(cogTemp) + 1);
                            } else {
                                totalCogmap.put(cogTemp, 1);
                            }
                        }
                    } else {
                        if (totalCogmap.containsKey(cog)) {
                            totalCogmap.put(cog, totalCogmap.get(cog) + 1);
                        } else {
                            totalCogmap.put(cog, 1);
                        }
                    }
                }
                if (value > 1.5) {
                    count++;
                    if (gene != null) {
                        String cog = gene.getCog();
                        if (cog.contains(";")) {
                            for (String cogTemp : cog.split(";")) {
                                cogTemp = cogTemp.trim();
                                if (cogmap.containsKey(cogTemp)) {
                                    cogmap.put(cogTemp, cogmap.get(cogTemp) + 1);
                                } else {
                                    cogmap.put(cogTemp, 1);
                                }
                            }
                        } else {
                            if (cogmap.containsKey(cog)) {
                                cogmap.put(cog, cogmap.get(cog) + 1);
                            } else {
                                cogmap.put(cog, 1);
                            }
                        }
                    }
                }
            }
            result.add(locus + "\t" + count + "\t" + matrix.getHeaders().size());
        }
        TabDelimitedTableReader.saveList(result, Database.getInstance().getPath() + "ListLmoVariability.txt");
        ArrayList<String> finalcogmap = new ArrayList<>();
        finalcogmap.add("Name\tNumber\tTotal");
        for (String key : totalCogmap.keySet()) {
            finalcogmap.add(key + "\t" + cogmap.get(key) + "\t" + totalCogmap.get(key));
        }
        TabDelimitedTableReader.saveList(finalcogmap, Database.getInstance().getPath() + "COGVariability.txt");

        matrix = ExpressionMatrix.loadTab(Database.getInstance().getPath() + "ListLmoVariability.txt", true);
        matrix = Annotation.addAnnotation(matrix, Genome.loadEgdeGenome());
        matrix.saveTab(Database.getInstance().getPath() + "ListLmoVariability.txt", "Gene");

    }

    /**
     * Create metadata analysis network of transcriptomic datasets
     */
    @SuppressWarnings("unused")
    private static void createSummaryNetwork() {
        // TreeSet<String> listnode = new TreeSet<String>();
        // ArrayList<String> network = new ArrayList<String>();
        // for(BioCondition bioCond : BioCondition.getAllBioConditions()){
        // if(!bioCond.getReference().contains("Unpublished")){
        // for(String comp : bioCond.getComparisons()){
        // BioCondition bioCond2 = BioCondition.getBioCondition(comp);
        // String edge1 =
        // bioCond.getMutant()+"-"+bioCond.getGrowth()+"-"+bioCond.getMedia()+"-"+bioCond.getTemperature()+"\tpp\t"+bioCond2.getMutant()+"-"+bioCond2.getGrowth()+"-"+bioCond2.getMedia()+"-"+bioCond2.getTemperature();
        // listnode.add(bioCond.getMutant()+"-"+bioCond.getGrowth()+"-"+bioCond.getMedia()+"\t"+bioCond.getGrowth()+"\t"+bioCond.getTemperature());
        // listnode.add(bioCond2.getMutant()+"-"+bioCond2.getGrowth()+"-"+bioCond2.getMedia()+"\t"+bioCond2.getGrowth()+"\t"+bioCond2.getTemperature());
        // network.add(edge1);
        // }
        // }
        // }
        // TabDelimitedTableReader.saveList(network,
        // Project.getPath()+"ModifNetworks.sif");
        // TabDelimitedTableReader.saveTreeSet(listnode,
        // Project.getPath()+"ListNode.txt");

        ArrayList<BioCondition> bioCondTemps = BioCondition.getAllBioConditions();
        ArrayList<BioCondition> bioConds = new ArrayList<>();
        for (BioCondition bioCond : bioCondTemps) {
            if (!bioCond.isNoData() && !bioCond.getTypeDataContained().contains(TypeData.Proteome)
                    && !bioCond.getReference().contains("Unpublished")) {
                bioConds.add(bioCond);
            }
        }
        System.out.println("NB bioCond: " + bioConds.size());
        TreeMap<String, Integer> mutants = new TreeMap<>();
        TreeMap<String, Integer> growths = new TreeMap<>();
        TreeMap<String, Integer> medias = new TreeMap<>();
        TreeMap<String, Integer> genomes = new TreeMap<>();
        TreeMap<String, Integer> temperatures = new TreeMap<>();

        TreeMap<String, Integer> networks = new TreeMap<>();
        TreeSet<String> listnode = new TreeSet<String>();
        for (BioCondition bioCond : bioConds) {
            ArrayList<String> allModif = new ArrayList<>();
            for (String mutant : bioCond.getMutant()) {
                mutant = "Mutant";
                if (mutants.containsKey(mutant)) {
                    mutants.put(mutant, mutants.get(mutant) + 1);
                } else {
                    mutants.put(mutant, 1);
                }
                allModif.add(mutant);
            }
            for (String growth : bioCond.getGrowth()) {
                if (growths.containsKey(growth)) {
                    growths.put(growth, growths.get(growth) + 1);
                } else {
                    growths.put(growth, 1);
                }
                allModif.add(growth);
            }
            for (String media : bioCond.getMedia()) {
                if (medias.containsKey(media)) {
                    medias.put(media, medias.get(media) + 1);
                } else {
                    medias.put(media, 1);
                }
                allModif.add(media);
            }
            String temperature = bioCond.getTemperature();

            if (temperatures.containsKey(temperature)) {
                temperatures.put(temperature, temperatures.get(temperature) + 1);
            } else {
                temperatures.put(temperature, 1);
            }
            allModif.add(temperature);
            String genomeName = bioCond.getGenomeUsed();
            if (genomes.containsKey(genomeName)) {
                genomes.put(genomeName, genomes.get(genomeName) + 1);
                // allModif.add(genomeName);
            } else {
                genomes.put(genomeName, 1);
            }

            for (String modif1 : allModif) {
                for (String modif2 : allModif) {
                    if (!modif1.equals(modif2)) {
                        String edge1 = modif1 + "\tpp\t" + modif2;
                        String edge2 = modif2 + "\tpp\t" + modif1;
                        if (networks.containsKey(edge1)) {
                            networks.put(edge1, networks.get(edge1) + 1);
                        } else if (networks.containsKey(edge2)) {
                            networks.put(edge2, networks.get(edge2) + 1);
                        } else {
                            networks.put(edge1, 1);
                        }
                    }
                }
            }
        }

        ArrayList<String> finalNetwork = new ArrayList<>();
        ArrayList<String> networkAttributes = new ArrayList<>();
        networkAttributes.add("Name\tStrength");
        for (String key : networks.keySet()) {
            finalNetwork.add(key);
            networkAttributes.add(key.replaceAll("pp", "(pp)").replaceAll("\t", " ") + "\t" + networks.get(key));
        }
        TabDelimitedTableReader.saveList(finalNetwork, Database.getInstance().getPath() + "ModifNetworks.sif");
        TabDelimitedTableReader.saveList(networkAttributes, Database.getInstance().getPath() + "NetworkAttrib.sif");

        /*
         * Manage node
         */
        for (String mutant : mutants.keySet()) {
            listnode.add(mutant + "\tMedias\t" + mutants.get(mutant));
        }
        for (String mutant : medias.keySet()) {
            if (mutant.contains("Broth")) {
                listnode.add(mutant + "\tBroth\t" + medias.get(mutant));
            }
        }
        for (String mutant : medias.keySet()) {
            if (mutant.contains("cells")) {
                listnode.add(mutant + "\tCells\t" + medias.get(mutant));
            }
        }
        for (String mutant : medias.keySet()) {
            if (!mutant.contains("Broth") && !mutant.contains("cells")) {
                listnode.add(mutant + "\tMedias\t" + medias.get(mutant));
            }
        }
        for (String mutant : growths.keySet()) {
            listnode.add(mutant + "\tGrowths\t" + growths.get(mutant));
        }
        for (String mutant : temperatures.keySet()) {
            listnode.add(mutant + "\tTemperatures\t" + temperatures.get(mutant));
        }
        TabDelimitedTableReader.saveTreeSet(listnode, Database.getInstance().getPath() + "ListNode.txt");

    }

    /**
     * Create metanalysis network of proteomics datasets
     */
    @SuppressWarnings("unused")
    private static void createSummaryNetworkProteome() {
        ArrayList<BioCondition> bioCondTemps = BioCondition.getAllBioConditions();
        ArrayList<BioCondition> bioConds = new ArrayList<>();
        for (BioCondition bioCond : bioCondTemps) {
            if (!bioCond.isNoData() && bioCond.getTypeDataContained().contains(TypeData.Proteome)
                    && !bioCond.getReference().contains("Unpublished")) {
                bioConds.add(bioCond);
            }
        }
        System.out.println("NB bioCond: " + bioConds.size());
        TreeMap<String, Integer> mutants = new TreeMap<>();
        TreeMap<String, Integer> growths = new TreeMap<>();
        TreeMap<String, Integer> medias = new TreeMap<>();
        TreeMap<String, Integer> localization = new TreeMap<>();
        TreeMap<String, Integer> genomes = new TreeMap<>();
        TreeMap<String, Integer> temperatures = new TreeMap<>();

        TreeMap<String, Integer> networks = new TreeMap<>();
        TreeSet<String> listnode = new TreeSet<String>();
        for (BioCondition bioCond : bioConds) {
            ArrayList<String> allModif = new ArrayList<>();
            for (String mutant : bioCond.getMutant()) {
                mutant = "Mutant";
                if (mutants.containsKey(mutant)) {
                    mutants.put(mutant, mutants.get(mutant) + 1);
                } else {
                    mutants.put(mutant, 1);
                }
                allModif.add(mutant);
            }
            for (String growth : bioCond.getGrowth()) {
                if (growths.containsKey(growth)) {
                    growths.put(growth, growths.get(growth) + 1);
                } else {
                    growths.put(growth, 1);
                }
                allModif.add(growth);
            }
            for (String media : bioCond.getMedia()) {
                if (medias.containsKey(media)) {
                    medias.put(media, medias.get(media) + 1);
                } else {
                    medias.put(media, 1);
                }
                allModif.add(media);
            }
            for (String media : bioCond.getLocalization()) {
                if (localization.containsKey(media)) {
                    localization.put(media, localization.get(media) + 1);
                } else {
                    localization.put(media, 1);
                }
                allModif.add(media);
            }
            String temperature = bioCond.getTemperature();

            if (temperatures.containsKey(temperature)) {
                temperatures.put(temperature, temperatures.get(temperature) + 1);
            } else {
                temperatures.put(temperature, 1);
            }
            allModif.add(temperature);
            String genomeName = bioCond.getGenomeUsed();
            if (genomes.containsKey(genomeName)) {
                genomes.put(genomeName, genomes.get(genomeName) + 1);
                // allModif.add(genomeName);
            } else {
                genomes.put(genomeName, 1);
            }

            for (String modif1 : allModif) {
                for (String modif2 : allModif) {
                    if (!modif1.equals(modif2)) {
                        String edge1 = modif1 + "\tpp\t" + modif2;
                        String edge2 = modif2 + "\tpp\t" + modif1;
                        if (networks.containsKey(edge1)) {
                            networks.put(edge1, networks.get(edge1) + 1);
                        } else if (networks.containsKey(edge2)) {
                            networks.put(edge2, networks.get(edge2) + 1);
                        } else {
                            networks.put(edge1, 1);
                        }
                    }
                }
            }
        }

        ArrayList<String> finalNetwork = new ArrayList<>();
        ArrayList<String> networkAttributes = new ArrayList<>();
        networkAttributes.add("Name\tStrength");
        for (String key : networks.keySet()) {
            finalNetwork.add(key);
            networkAttributes.add(key.replaceAll("pp", "(pp)").replaceAll("\t", " ") + "\t" + networks.get(key));
        }
        TabDelimitedTableReader.saveList(finalNetwork, Database.getInstance().getPath() + "ModifNetworksProteome.sif");
        TabDelimitedTableReader.saveList(networkAttributes,
                Database.getInstance().getPath() + "NetworkAttribProteome.sif");

        /*
         * Manage node
         */
        for (String mutant : mutants.keySet()) {
            listnode.add(mutant + "\tMedias\t" + mutants.get(mutant));
        }
        for (String mutant : medias.keySet()) {
            if (mutant.contains("Broth")) {
                listnode.add(mutant + "\tBroth\t" + medias.get(mutant));
            }
        }
        for (String mutant : medias.keySet()) {
            if (mutant.contains("cells")) {
                listnode.add(mutant + "\tCells\t" + medias.get(mutant));
            }
        }
        for (String mutant : medias.keySet()) {
            if (!mutant.contains("Broth") && !mutant.contains("cells")) {
                listnode.add(mutant + "\tMedias\t" + medias.get(mutant));
            }
        }
        for (String mutant : localization.keySet()) {
            listnode.add(mutant + "\tLocalization\t" + localization.get(mutant));
        }
        for (String mutant : growths.keySet()) {
            listnode.add(mutant + "\tGrowths\t" + growths.get(mutant));
        }
        for (String mutant : temperatures.keySet()) {
            listnode.add(mutant + "\tTemperatures\t" + temperatures.get(mutant));
        }
        TabDelimitedTableReader.saveTreeSet(listnode, Database.getInstance().getPath() + "ListNodeProteome.txt");

    }

    /**
     * Create summary tables of RNAseq datasets
     */
    @SuppressWarnings("unused")
    private static void createSummaryRNASeq() {
        ArrayList<String> list = new ArrayList<String>();
        for (BioCondition bioCondition : Experiment.getGeneralExp().getBioConditions()) {
            if (bioCondition.getTypeDataContained().contains(TypeData.RNASeq)) {
                String data = "";
                for (NGS rnaseq : bioCondition.getNGSSeqs()) {
                    data += rnaseq.getName();
                    for (String rawData : rnaseq.getRawDatas()) {
                        data += ";" + rawData;
                    }
                }
                list.add(bioCondition.getName() + "\t" + bioCondition.getArrayExpressTechnoId() + "\t"
                        + bioCondition.getArrayExpressId() + "\t" + bioCondition.getReference() + "\t"
                        + bioCondition.getDate() + "\t" + data);
            }
        }

        TabDelimitedTableReader.saveList(list, "D:/listRNASeq.txt");

        String[][] rnaseq = TabDelimitedTableReader.read("D:/rnaseq.txt");
        String[][] rnaseq2 = TabDelimitedTableReader.read("D:/listRNASeq.txt");

        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < rnaseq.length; i++) {
            String row = "";
            for (int k = 0; k < rnaseq[0].length; k++) {
                row += rnaseq[i][k] + "\t";
            }
            boolean found = false;
            for (int j = 0; j < rnaseq2.length && !found; j++) {
                String data = rnaseq2[j][0];
                System.out.println(rnaseq[i][0] + " " + data);
                if (rnaseq[i][0].equals(data)) {
                    for (int k = 0; k < rnaseq2[0].length; k++) {
                        row += rnaseq2[j][k] + "\t";
                    }

                    result.add(row);
                    found = true;
                }
            }
        }
        TabDelimitedTableReader.saveList(result, "D:/rnaseqfinal.txt");

    }

    /**
     * Create Table 1 for Listeriomics paper with the list of Genomes and different infos<br>
     * Save in: Project.getPath()+"Table 1 - Genomes.txt"
     */
    public static void createGenomeTable() {
        ArrayList<String> table = new ArrayList<>();
        String[] headers = {"Strain", "Accession (Plasmid)", "Length Chromosomes", "G+C content", "CDS",
                "rRNA and tRNA", "sRNAs from EGD-e", "CisRegs from EGD-e", "asRNAs from EGD-e", "Prophage"};
        String header = "";
        for (String temp : headers)
            header += temp + "\t";
        table.add(header);

        ArrayList<String> genomesList = Genome.getAvailableGenomes();
        for (String genomeName : genomesList) {
            Genome genome = Genome.loadGenome(genomeName);
            String strain = genome.getSpecies();
            String accession = "";
            String length = "";
            String GC = "";
            String CDS = "";
            String rRNAtRNA = "";
            String sRNA = "";
            String cisRegs = "";
            String asRNAs = "";
            for (String chromoName : genome.getChromosomes().keySet()) {
                accession += genome.getFirstChromosome().getAccession().toString() + " - ";
                length += genome.getFirstChromosome().getLength() + " - ";
                GC += ((double) genome.getFirstChromosome().getLength()
                        / (double) genome.getFirstChromosome().getGCCount()) + " - ";
                CDS += genome.getFirstChromosome().getGenes().size() + " - ";
                rRNAtRNA = genome.getFirstChromosome().getNcRNAs().size() + " - ";
                sRNA += genome.getFirstChromosome().getsRNAs().size() + " - ";
                cisRegs += genome.getFirstChromosome().getCisRegs().size() + " - ";
                asRNAs += genome.getFirstChromosome().getAsRNAs().size() + " - ";
            }
            String[] rows = {strain, accession, length, GC, CDS, rRNAtRNA, sRNA, cisRegs, asRNAs};
            String row = "";
            for (String temp : rows)
                row += temp + "\t";
            table.add(row);

        }

        TabDelimitedTableReader.saveList(table, Database.getInstance().getPath() + "Table 1 - Genomes.txt");

    }

    /**
     * Create the different element for the figure representing the BioCondition
     */
    public static void createBioConditionFigure(boolean transcriptomes) {
        Genome genome = Genome.loadEgdeGenome();

        ArrayList<BioCondition> bioCondTemps = BioCondition.getAllBioConditions();
        ArrayList<BioCondition> bioConds = new ArrayList<>();
        for (BioCondition bioCond : bioCondTemps) {
            if (!bioCond.isNoData()) {
                bioConds.add(bioCond);
            }
        }
        bioCondTemps = bioConds;
        bioConds = new ArrayList<BioCondition>();

        ArrayList<String> listBioCond = new ArrayList<String>();
        for (BioCondition bioCond : bioCondTemps) {
            if (!bioCond.getReference().contains("Unpublished")) {
                if (transcriptomes) {
                    if (!bioCond.getTypeDataContained().contains(TypeData.Proteome)) {
                        bioConds.add(bioCond);
                        System.out.println(bioCond.getName() + " - " + bioCond.getReference());
                        listBioCond.add(bioCond.getName() + " - " + bioCond.getReference());
                    }
                } else {
                    if (bioCond.getTypeDataContained().contains(TypeData.Proteome)) {
                        bioConds.add(bioCond);
                        System.out.println(bioCond.getName() + " - " + bioCond.getReference());
                        listBioCond.add(bioCond.getName() + " - " + bioCond.getReference());
                    }
                }
            }
        }
        TabDelimitedTableReader.saveList(listBioCond, Database.getInstance().getPath() + "listBioCond.txt");

        TreeMap<String, Integer> mutants = new TreeMap<>();
        TreeMap<String, Integer> growths = new TreeMap<>();
        TreeMap<String, Integer> medias = new TreeMap<>();
        TreeMap<String, Integer> genomes = new TreeMap<>();
        TreeMap<String, Integer> temperatures = new TreeMap<>();
        TreeSet<String> mutantsSet = new TreeSet<>();

        TreeMap<String, Integer> networks = new TreeMap<>();

        for (BioCondition bioCond : bioConds) {
            ArrayList<String> allModif = new ArrayList<>();
            for (String mutant : bioCond.getMutant()) {
                if (mutants.containsKey(mutant)) {
                    mutants.put(mutant, mutants.get(mutant) + 1);
                } else {
                    mutants.put(mutant, 1);
                }
                if (mutant.contains("lmo") && !mutant.contains("_Over")) {
                    mutantsSet.add(mutant);
                }
                // allModif.add(mutant);
            }
            for (String growth : bioCond.getGrowth()) {
                if (growths.containsKey(growth)) {
                    growths.put(growth, growths.get(growth) + 1);
                } else {
                    growths.put(growth, 1);
                }
                allModif.add(growth);
            }
            for (String media : bioCond.getMedia()) {
                if (medias.containsKey(media)) {
                    medias.put(media, medias.get(media) + 1);
                } else {
                    medias.put(media, 1);
                }
                allModif.add(media);
            }
            String temperature = bioCond.getTemperature();
            if (temperatures.containsKey(temperature)) {
                temperatures.put(temperature, temperatures.get(temperature) + 1);
                allModif.add(temperature);
            } else {
                temperatures.put(temperature, 1);
            }
            String genomeName = bioCond.getGenomeUsed();
            if (genomes.containsKey(genomeName)) {
                genomes.put(genomeName, genomes.get(genomeName) + 1);
                // allModif.add(genomeName);
            } else {
                genomes.put(genomeName, 1);
            }

            // for(String modif1 : allModif){
            // for(String modif2 : allModif){
            // if(!modif1.equals(modif2)){
            // String edge1 = modif1+"\tpp\t"+modif2;
            // String edge2 = modif2+"\tpp\t"+modif1;
            // if(networks.containsKey(edge1)){
            // networks.put(edge1, networks.get(edge1)+1);
            // }else if(networks.containsKey(edge2)){
            // networks.put(edge2, networks.get(edge2)+1);
            // }else{
            // networks.put(edge1, 1);
            // }
            // }
            // }
            // }
        }

        // ArrayList<String> finalNetwork = new ArrayList<>();
        // for(String key : networks.keySet()) finalNetwork.add(key);
        // TabDelimitedTableReader.saveList(finalNetwork,
        // Project.getPath()+"ModifNetworks.sif");

        ArrayList<String> summaryTable = new ArrayList<>();
        String header = "Mutant";
        summaryTable.add(header);
        for (String mutant : mutants.keySet()) {
            if (mutant.contains("lmo")) {
                String mutantTemp = mutant;
                mutant = mutant.replaceFirst("_Over", "");
                Sequence seq = genome.getElement(mutant);
                if (seq instanceof Gene) {
                    Gene gene = (Gene) seq;
                    summaryTable.add(mutantTemp + " (" + mutants.get(mutant) + ") - " + gene.getGeneName());
                } else {
                    summaryTable.add(mutantTemp + " (" + mutants.get(mutant) + ")");
                }
            } else {
                summaryTable.add(mutant + " (" + mutants.get(mutant) + ")");
            }
        }
        summaryTable.add("");
        summaryTable.add("Broth");
        int k = 0;
        for (String mutant : medias.keySet()) {
            if (mutant.contains("Broth")) {
                summaryTable.add(mutant + " (" + medias.get(mutant) + ")");
                k += medias.get(mutant);
            }
        }
        summaryTable.add(k + "");
        summaryTable.add("");
        summaryTable.add("Cells");
        k = 0;
        for (String mutant : medias.keySet()) {
            if (mutant.contains("cells")) {
                summaryTable.add(mutant + " (" + medias.get(mutant) + ")");
                k += medias.get(mutant);
            }
        }
        summaryTable.add(k + "");
        summaryTable.add("");
        summaryTable.add("Media specific properties");
        k = 0;
        for (String mutant : medias.keySet()) {
            if (!mutant.contains("Broth") && !mutant.contains("cells")) {
                summaryTable.add(mutant + " (" + medias.get(mutant) + ")");
                k += medias.get(mutant);
            }
        }
        summaryTable.add(k + "");
        summaryTable.add("");
        summaryTable.add("Growth");
        k = 0;
        for (String mutant : growths.keySet()) {
            summaryTable.add(mutant + " (" + growths.get(mutant) + ")");
            k += growths.get(mutant);
        }
        summaryTable.add(k + "");
        summaryTable.add("");
        summaryTable.add("Temperature");
        k = 0;
        for (String mutant : temperatures.keySet()) {
            summaryTable.add(mutant + " (" + temperatures.get(mutant) + ")");
            k += temperatures.get(mutant);
        }
        summaryTable.add(k + "");
        summaryTable.add("");
        summaryTable.add("Genomes");
        k = 0;
        for (String mutant : genomes.keySet()) {
            summaryTable.add(mutant + " (" + genomes.get(mutant) + ")");
            k += genomes.get(mutant);
        }
        summaryTable.add(k + "");

        System.out.println("Number BioCond: " + bioConds.size());
        int countTiling = 0;
        int countRnaSeq = 0;
        int countTSS = 0;
        int countGeneExpr = 0;
        int countProteome = 0;
        int comparisonTiling = 0;
        int comparisonRNASeq = 0;
        int comparisonGeneExpr = 0;
        int comparisonProteome = 0;
        int comparisons = 0;

        TreeSet<String> arrayExpressId = new TreeSet<>();
        for (BioCondition bioCond : bioConds) {
            arrayExpressId.add(bioCond.getReference());
            if (bioCond.getTilings().size() != 0) {
                countTiling++;
            }
            if (bioCond.getNGSSeqs().size() != 0) {
                countRnaSeq++;
            }
            if (bioCond.getGeneExprs().size() != 0 || bioCond.getMatrices().size() != 0) {
                countGeneExpr++;
            }
            if (bioCond.getProteomes().size() != 0) {
                countProteome++;
            }

            /*
             * Count comparisons
             */
            for (String bioCondName2 : bioCond.getComparisons()) {
                comparisons++;
                BioCondition compBioCond = bioCond.compare(BioCondition.getBioCondition(bioCondName2), false);
                if (compBioCond.getTilings().size() != 0) {
                    comparisonTiling++;
                } else if (compBioCond.getGeneExprs().size() != 0) {
                    comparisonGeneExpr++;
                }
                if (compBioCond.getNGSSeqs().size() != 0) {
                    comparisonRNASeq++;
                }
                if (compBioCond.getMatrices().size() != 0) {
                    comparisonGeneExpr++;
                }
                if (compBioCond.getProteomes().size() != 0) {
                    comparisonProteome++;
                }
            }

        }

        summaryTable.add("");
        summaryTable.add("DataType");
        summaryTable.add("Publication = " + arrayExpressId.size());
        summaryTable.add("BioCondition = " + bioConds.size());
        summaryTable.add("Comparison = " + comparisons);

        summaryTable.add("Data absolute expression: ("
                + (countGeneExpr + countTiling + countRnaSeq + countTSS + countProteome) + ")");
        summaryTable.add("Gene Expression (" + countGeneExpr + ")");
        summaryTable.add("Tiling (" + countTiling + ")");
        summaryTable.add("RNASeq (" + countRnaSeq + ")");
        summaryTable.add("TSS (" + countTSS + ")");
        summaryTable.add("Proteome (" + countProteome + ")");

        summaryTable.add("Data relative expression: ("
                + (comparisonGeneExpr + comparisonTiling + comparisonRNASeq + comparisonProteome) + ")");
        summaryTable.add("Gene Expression (" + comparisonGeneExpr + ")");
        summaryTable.add("Tiling (" + comparisonTiling + ")");
        summaryTable.add("RNASeq (" + comparisonRNASeq + ")");
        summaryTable.add("Proteome (" + comparisonProteome + ")");

        TabDelimitedTableReader.saveList(summaryTable, Database.getInstance().getPath() + "ModifTable.txt");

        String input = Database.getInstance().getPath() + "PositionGeneCGView.tab";
        String output = Database.getInstance().getPath() + "PositionGeneCGView.svg";

        // CircularGenomeJPanel panel = new CircularGenomeJPanel(500, 500, genome, "fj",
        // mutantsSet);
        // try {
        // CgviewIO.writeToPNGFile(panel.getCgview(), "yo");
        // } catch (IOException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }

        ArrayList<String> cgViewTabFile = new ArrayList<>();
        cgViewTabFile.add("%" + genome.getFirstChromosome().getLength());
        cgViewTabFile.add("!strand\tslot\tstart\tstop\ttype\tlabel");
        for (String elementName : mutantsSet) {

            Sequence seq = genome.getElement(elementName);
            if (seq != null) {
                String name = seq.getName() + " (" + mutants.get(elementName) + ")";
                if (seq instanceof Gene) {
                    Gene gene = (Gene) seq;
                    if (!gene.getGeneName().equals("")) {
                        name = gene.getGeneName() + " (" + mutants.get(elementName) + ")";
                    }
                }
                String row = "reverse\t1\t" + seq.getBegin() + "\t" + seq.getEnd() + "\tgene\t" + name;
                if (seq.isStrand())
                    row = "forward\t2\t" + seq.getBegin() + "\t" + seq.getEnd() + "\tgene\t" + name;
                cgViewTabFile.add(row);
            }
        }
        TabDelimitedTableReader.saveList(cgViewTabFile, input);
        // String execProcess = "java -jar \"/Applications/cgview/cgview.jar\" -i
        // \""+input+"\" -o
        // \""+output+"\" -f svg";
        String execProcess = "\"C:/cgview/cgview.jar\" -i \"" + input + "\" -o \"" + output + "\" -f svg";
        try {
            CMD.runProcess(execProcess, true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
