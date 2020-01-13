package bacnet.scripts.database;

import java.util.HashMap;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.ProteomicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Genome;
import bacnet.utils.FileUtils;

public class DataValidation {

    /**
     * HashMap for validation of genomes
     */
    private HashMap<String, Boolean> genomes = new HashMap<>();

    /**
     * HashMap for validation of bioconditions
     */
    private HashMap<String, Boolean> bioconditions = new HashMap<>();

    /**
     * HashMap for validation of comparisons
     */
    private HashMap<String, Boolean> comparisons = new HashMap<>();

    /**
     * HashMap for validation of transcriptomes
     */
    private HashMap<String, Boolean> transcriptomes = new HashMap<>();

    /**
     * HashMap for validation of proteomes
     */
    private HashMap<String, Boolean> proteomes = new HashMap<>();

    /**
     * HashMap for validation of proteomes
     */
    private HashMap<String, Boolean> coExprNetworks = new HashMap<>();

    
    public DataValidation() {

    }

    /**
     * Validate every genomes by loading them
     * 
     * @param logs
     * @return
     */
    public String validateGenomes(String logs) {
        for (String genome : this.getGenomes().keySet()) {
            if (!this.getGenomes().get(genome)) {
                logs += "Validate genome: " + genome + "\n";
                Genome genomeTemp = Genome.loadGenome(genome);
                if (genomeTemp == null) {
                    logs += genome + " does not exists. Click: Add unvalidated Genomes to the database" + "\n";
                } else if (genomeTemp.getFirstChromosome().getGenes().size() == 0) {
                    logs += genome + " does not exists. Click: Add unvalidated Genomes to the database" + "\n";
                } else {
                    logs += genome + " loaded with " + genomeTemp.getChromosomes().size() + " chromosomes and "
                            + genomeTemp.getGeneNames().size() + " genes\n";
                    this.getGenomes().put(genome, true);
                }
            }
        }
        return logs;
    }

    /**
     * Validate every bioconditionsss by loading them
     * 
     * @return
     */
    public String validateBioConditions(String logs) {
        for (String bioCondName : this.getBioconditions().keySet()) {
            if (!this.getBioconditions().get(bioCondName)) {
                logs += "Validate biocondition: " + bioCondName + "\n";
                BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
                if (bioCond == null) {
                    logs += bioCondName + " does not exists. Click: Add unvalidated BioConditions to the database"
                            + "\n";
                } else {
                    this.getBioconditions().put(bioCondName, true);
                }
            }
        }
        return logs;
    }

    /**
     * Validate every comparisons by loading them
     * 
     * @return
     */
    public String validateComparisons(String logs) {
        for (String compName : this.getComparisons().keySet()) {
            if (!this.getComparisons().get(compName)) {
                // logs+= "Validate comparison: "+compName+"\n";
                String leftBC = BioCondition.parseName(compName)[0];
                String rightBC = BioCondition.parseName(compName)[1];
                BioCondition bioCond1 = BioCondition.getBioCondition(leftBC);
                BioCondition bioCond2 = BioCondition.getBioCondition(rightBC);
                if (bioCond1 == null) {
                    logs += bioCond1 + " does not exists. Click: Add unvalidated Biological Conditions to the database"
                            + "\n";
                } else if (bioCond2 == null) {
                    logs += bioCond2 + " does not exists. Click: Add unvalidated Biological Conditions to the database"
                            + "\n";
                } else {
                    if (bioCond1.getComparisons().contains(rightBC)) {
                        this.getComparisons().put(compName, true);
                    } else {
                        logs += compName + " does not exists. Click: Add unvalidated Comparisons to the database"
                                + "\n";
                    }
                }

            }
        }
        return logs;
    }

    /**
     * Validate Transcriptomes data by looking if streamingdata files exists
     * 
     * @param logs
     * @return
     */
    public String validateTranscriptomics(String logs) {
        for (String biocondName : this.getTranscriptomes().keySet()) {
            if (!this.getTranscriptomes().get(biocondName)) {
                BioCondition bioCond = BioCondition.getBioCondition(biocondName);
                System.out.println(bioCond.getName());
                // logs+= "Validate transcriptomes in: "+biocondName+"\n";
                boolean validate = true;
                for (ExpressionMatrix transData : bioCond.getMatrices()) {
                    String fileNameInfo = OmicsData.PATH_STREAMING + transData.getName() + OmicsData.EXTENSION;
                    if (!FileUtils.exists(fileNameInfo)) {
                        validate = false;
                    }
                }
                for (Tiling transData : bioCond.getTilings()) {
                    String fileNameInfo = OmicsData.PATH_STREAMING + transData.getName() + OmicsData.EXTENSION;
                    //System.out.println(fileNameInfo);
                    if (!FileUtils.exists(fileNameInfo)) {
                        validate = false;
                    }
                }
                for (GeneExpression transData : bioCond.getGeneExprs()) {
                    String fileNameInfo = OmicsData.PATH_STREAMING + transData.getName() + OmicsData.EXTENSION;
                    //System.out.println(fileNameInfo);
                    if (!FileUtils.exists(fileNameInfo)) {
                        validate = false;
                    }
                }
                for (NGS transData : bioCond.getNGSSeqs()) {
                    String chromosome =
                            Genome.loadGenome(transData.getGenomeName()).getFirstChromosome().getChromosomeID();
                    String fileNameInfo =
                            OmicsData.PATH_STREAMING + transData.getName() + "_" + chromosome + NGS.EXTENSION;
                    if (!FileUtils.exists(fileNameInfo)) {
                        validate = false;
                    }
                }
                for (String transData : bioCond.getComparisonDataNames()) {
                    if (bioCond.getTypeDataContained().contains(TypeData.ExpressionMatrix)) {
                        String fileNameInfo = OmicsData.PATH_STREAMING + transData + OmicsData.EXTENSION;
                        if (!FileUtils.exists(fileNameInfo)) {
                            validate = false;
                        }
                    }
                }
                if (validate) {
                    this.getTranscriptomes().put(biocondName, true);
                } else {
                    logs += "Missing transcriptomes datasets for " + biocondName
                            + " - Click: Add unvalidated Transcriptomes to the database" + "\n";
                }
            }
        }
        return logs;
    }

    /**
     * Validate Transcriptomes data by looking if streamingdata files exists
     * 
     * @param logs
     * @return
     */
    public String validateProteomics(String logs) {
        for (String biocondName : this.getProteomes().keySet()) {
            if (!this.getProteomes().get(biocondName)) {
                BioCondition bioCond = BioCondition.getBioCondition(biocondName);
                // logs+= "Validate proteomes in: "+biocondName+"\n";
                boolean validate = true;
                for (OmicsData transData : bioCond.getnTerms()) {
                    String fileNameInfo = OmicsData.PATH_STREAMING + transData.getName() + OmicsData.EXTENSION;
                    if (!FileUtils.exists(fileNameInfo)) {
                        validate = false;
                    }
                }
                for (OmicsData transData : bioCond.getProteomes()) {
                    String fileNameInfo = OmicsData.PATH_STREAMING + transData.getName() + ProteomicsData.EXTENSION;
                    System.out.println(fileNameInfo);
                    if (!FileUtils.exists(fileNameInfo)) {
                        validate = false;
                    }
                }
                if (validate) {
                    this.getProteomes().put(biocondName, true);
                    System.out.println("Validated "+biocondName);
                } else {
                    logs += "Missing proteomes datasets for " + biocondName
                            + " - Click: Add unvalidated Comparisons to the database" + "\n";
                }
            }
        }
        return logs;
    }
    
    
    /**
     * Validate Transcriptomes data by looking if streamingdata files exists
     * 
     * @param logs
     * @return
     */
    public String validateCoExprNetworks(String logs) {
        for (String genomeName : this.getCoExprNetworks().keySet()) {
            if (!this.getCoExprNetworks().get(genomeName)) {
                String networkPath = Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genomeName;
                // logs+= "Validate proteomes in: "+biocondName+"\n";
                boolean validate = true;
                if (!FileUtils.exists(networkPath)) {
                    validate = false;
                }
                if (validate) {
                    this.getCoExprNetworks().put(genomeName, true);
                } else {
                    logs += "Missing coExpr networks for " + genomeName
                            + " - Click: Add unvalidated CoExpr Networks to the database" + "\n";
                }
            }
        }
        return logs;
    }

    /***************************************
     * GETTER AND SETTER
     */

    public HashMap<String, Boolean> getGenomes() {
        return genomes;
    }

    public void setGenomes(HashMap<String, Boolean> genomes) {
        this.genomes = genomes;
    }

    public HashMap<String, Boolean> getBioconditions() {
        return bioconditions;
    }

    public void setBioconditions(HashMap<String, Boolean> bioconditions) {
        this.bioconditions = bioconditions;
    }

    public HashMap<String, Boolean> getTranscriptomes() {
        return transcriptomes;
    }

    public void setTranscriptomes(HashMap<String, Boolean> transcriptomes) {
        this.transcriptomes = transcriptomes;
    }

    public HashMap<String, Boolean> getProteomes() {
        return proteomes;
    }

    public void setProteomes(HashMap<String, Boolean> proteomes) {
        this.proteomes = proteomes;
    }

    public HashMap<String, Boolean> getComparisons() {
        return comparisons;
    }

    public void setComparisons(HashMap<String, Boolean> comparisons) {
        this.comparisons = comparisons;
    }

    public HashMap<String, Boolean> getCoExprNetworks() {
        return coExprNetworks;
    }

    public void setCoExprNetworks(HashMap<String, Boolean> coExprNetworks) {
        this.coExprNetworks = coExprNetworks;
    }

}
