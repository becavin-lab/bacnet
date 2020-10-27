package bacnet.expressionAtlas.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.expdesign.BioCondition;


/**
 * Run thread for opening BioCondition, Comparisons, detect Genome and open corresponding LogFC
 * ExpressionMatrix
 * 
 * @author christophebecavin
 *
 */
public class OpenExpressionMatrixAndComparisons implements IRunnableWithProgress {
	
    private ArrayList<String> bioConditions = new ArrayList<>();
    private HashMap<String, ArrayList<String>> genomeToComparisons;
    /**
     * Whether it is transcriptome or proteome expression matrix
     * True if it is transcriptome
     */
    private boolean transcriptome = true;

    /**
     * Open BioCondition, Comparisons, detect Genome and open corresponding LogFC ExpressionMatrix
     * 
     * @param bioConditions
     * @param genomeToComparisons
     */
    public OpenExpressionMatrixAndComparisons(ArrayList<String> bioConditions, HashMap<String, ArrayList<String>> genomeToComparisons, boolean transcriptome) {
        this.bioConditions = bioConditions;
        this.genomeToComparisons = genomeToComparisons;
        this.transcriptome = transcriptome;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        int sizeProcess = bioConditions.size();
        // Tell the user what you are doing
        monitor.beginTask("Loading HeatMap", sizeProcess);
        for (String bioCondName : bioConditions) {
            monitor.subTask("Loading : " + bioCondName);
            monitor.worked(1);
            BioCondition bioCondition = BioCondition.getBioCondition(bioCondName);
            String genome = bioCondition.getGenomeName();
            for (String compName : bioCondition.getComparisonNames()) {
                System.out.println(compName);
                if (genomeToComparisons.containsKey(genome)) {
                    genomeToComparisons.get(genome).add(compName);
                } else {
                    ArrayList<String> compNames = new ArrayList<>();
                    compNames.add(compName);
                    genomeToComparisons.put(genome, compNames);
                }
            }
        }
        monitor.beginTask("Loading HeatMap", genomeToComparisons.size());
        for (String genomeName : genomeToComparisons.keySet()) {
            monitor.subTask("Loading ExpressionAtlas for : " + genomeName);
            monitor.worked(1);
            if(transcriptome) {
                @SuppressWarnings("unused")
            	ExpressionMatrix logFCMatrix = Database.getInstance().getLogFCTranscriptomesTable(genomeName);
            } else {
            	@SuppressWarnings("unused")
            	ExpressionMatrix logFCMatrix = Database.getInstance().getLogFCProteomesTable(genomeName);
            }
        }

        // You are done
        monitor.done();
    }
}
