package bacnet.expressionAtlas.core;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeSet;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Sequence;
import bacnet.utils.Filter;

public class GenomeElementAtlas implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6277013816841128158L;

    public static String PATH = Database.getDATA_PATH() + "GenomeElementAtlas" + File.separator;
    public static double DEFAULT_LOGFC_CUTOFF = 1;
    public static double DEFAULT_LOGFC_PROTEOMIC_CUTOFF = 1;
    public static double DEFAULT_COEXP_CUTOFF = 0.950;

    public static double DEFAULT_PVAL_CUTOFF = 0.05;
    public static double DEFAULT_PVAL_PROTEOMIC_CUTOFF = 0.05;

    private String name = "";
    
    private TreeSet<String> overBioConds = new TreeSet<>();
    private TreeSet<String> underBioConds = new TreeSet<>();
    private TreeSet<String> notDiffExpresseds = new TreeSet<>();
    private HashMap<String, Double> values = new HashMap<>();
    private HashMap<String, Double> pValues = new HashMap<>();
    private HashMap<String, Double> adjPvalues = new HashMap<>();


    //add hashmap for each with values

    public GenomeElementAtlas() {}

    /**
     * For a specific sequence update the list of BioCondition in which the genome element is
     * over-expressed, under-expressed or not differently expressed<br>
     * The update will be dependent of the value of the input filter = cutoff for LogFC and p-value
     * 
     * @param seq
     * @param filter object encapsulated cutoff values: cutOff1 = logFC , cutOff2 = pvalue
     * @param stat if the statistical value should be taken, into account or not
     */
    public GenomeElementAtlas(Sequence seq, Filter filter1, Filter filter2, boolean transcriptome) {
        System.out.println(seq.getGenomeName());
        ExpressionMatrix logFCMatrix = null;
        ExpressionMatrix pValueMatrix = null;
        ExpressionMatrix adjPvalueMatrix = null;

        if(transcriptome) {
        	logFCMatrix = Database.getInstance().getLogFCTranscriptomesTable(seq.getGenomeName());
        	pValueMatrix = Database.getInstance().getPvalueTranscriptomesTable(seq.getGenomeName());
        	adjPvalueMatrix = Database.getInstance().getAdjPvalueTranscriptomesTable(seq.getGenomeName());

        } else {
        	logFCMatrix = Database.getInstance().getLogFCProteomesTable(seq.getGenomeName());
        	pValueMatrix = Database.getInstance().getPvalueProteomesTable(seq.getGenomeName());
        	adjPvalueMatrix = Database.getInstance().getAdjPvalueProteomesTable(seq.getGenomeName());

        }
        String genomeElement = seq.getName();
        if (logFCMatrix.getRowNames().containsKey(genomeElement)) {
            for (String bioCondName : logFCMatrix.getHeaders()) {
                double logFC = logFCMatrix.getValue(genomeElement, bioCondName);
                double pvalue = pValueMatrix.getValue(genomeElement, bioCondName);
                double adjPvalue = adjPvalueMatrix.getValue(genomeElement, bioCondName);

                this.values.put(bioCondName, logFC);
                this.pValues.put(bioCondName, pvalue);
                this.adjPvalues.put(bioCondName, adjPvalue);

                
                

                if (logFC < -filter1.getCutOff1() && pvalue < filter2.getCutOff1()) {
                    // under-expressed
                    this.underBioConds.add(bioCondName);
                } else if (logFC > filter1.getCutOff1() && pvalue < filter2.getCutOff1()) {
                    // over-expressed
                	this.overBioConds.add(bioCondName);
                } else {
                	this.notDiffExpresseds.add(bioCondName);
                }
            }
        }
    }

    /*
     * ************************ GETTER AND SETTERS ************************
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeSet<String> getOverBioConds() {
        return overBioConds;
    }

    public void setOverBioConds(TreeSet<String> overBioConds) {
        this.overBioConds = overBioConds;
    }

    public TreeSet<String> getUnderBioConds() {
        return underBioConds;
    }

    public void setUnderBioConds(TreeSet<String> underBioConds) {
        this.underBioConds = underBioConds;
    }

    public TreeSet<String> getNotDiffExpresseds() {
        return notDiffExpresseds;
    }

    public void setNotDiffExpresseds(TreeSet<String> notDiffExpresseds) {
        this.notDiffExpresseds = notDiffExpresseds;
    }
    
    public HashMap<String, Double> getValues() {
        return values;
    }
    
    public HashMap<String, Double> getPvalues() {
        return pValues;
    }

    public HashMap<String, Double> getAdjPvalues() {
        return adjPvalues;
    }


}
