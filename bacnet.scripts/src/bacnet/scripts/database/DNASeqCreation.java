package bacnet.scripts.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.ColNames;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.core.Comparison;
import bacnet.utils.VectorUtils;

/*
 * Convert all DNASeq data
 */
public class DNASeqCreation {


    /**
     * Parse WIG file and calculate Copy Number Variation = raw coverage / mean coverage<br>
     * The WIG has to be Raw number of reads per basepair
     */
    public static void parseWIGFile() {
        
        // Get dna bioconditions
        ArrayList<BioCondition> dnaSeqBioConds = new ArrayList<>();
        for(BioCondition bioCond : BioCondition.getAllBioConditions()) {
            if(bioCond.getName().contains("DNA")) {
                dnaSeqBioConds.add(bioCond);
            }
        }
        
        
        
        Genome genome = Genome.loadGenome(Genome.Donovani_NAME);
        ArrayList<String> chromoNames = genome.getChromosomeNames();
        ArrayList<String> meanCoverages = new ArrayList<>();
        for (BioCondition bioCond : dnaSeqBioConds) {
            for(NGS rnaSeq : bioCond.getNGSSeqs()) {
                String fileName = rnaSeq.getName()+".wig";
                ArrayList<String> rows =
                        TabDelimitedTableReader.readList(OmicsData.PATH_NGS_RAW + File.separator + fileName, true);
                String finalFileName = ExpressionData.PATH_NGS_NORM + File.separator + fileName;
                // try {
                // FileWriter fileW = new FileWriter(finalFileName, false);
                // BufferedWriter bufferW = new BufferedWriter(fileW);
                ArrayList<Double> values = new ArrayList<>();
                String chromoID = "";
                
                for (String row : rows) {
                    if(row.contains("variableStep")) {
                        chromoID = row.replace("variableStep chrom=","");
                    }else {
                        int begin = Integer.parseInt(row.split("\t")[0]);
                        double coverage = Double.parseDouble(row.split("\t")[1]);
                        values.add(coverage);
                        if (begin % 100000 == 0) {
                            System.out.println(
                                    "Pos: " + begin + " chromo: " + chromoID + " " + coverage);
                        }
                    }
                }
                Double[] valuesdouble = values.toArray(new Double[0]);
                System.out.println(fileName + "\t" + VectorUtils.mean(valuesdouble));
                meanCoverages.add(fileName + "\t" + VectorUtils.mean(valuesdouble));
                double meanCov = VectorUtils.mean(valuesdouble);
    
                // Calculate CNV and save it in a file
                try {
                    FileWriter fileW = new FileWriter(finalFileName, false);
                    BufferedWriter bufferW = new BufferedWriter(fileW);
                    for (String row : rows) {
                        if(row.contains("variableStep")) {
                            chromoID = row.replace("variableStep chrom=","");
                            bufferW.write(row);
                            bufferW.newLine();
                        }else {
                            int begin = Integer.parseInt(row.split("\t")[0]);
                            double coverage = Double.parseDouble(row.split("\t")[1]);
                            double newCov = 2 * (coverage / meanCov);
                            if (newCov >= 4) {
                                newCov = 4;
                            }
                            if (begin % 100000 == 0) {
                                System.out.println("Pos: " + begin + " chromo: " + chromoID + " " + newCov);
                            }
                            bufferW.write(begin + "\t" + newCov);
                            bufferW.newLine();
                        }
                    }
                    bufferW.close();
                    fileW.close();
                    // System.out.println("Table saved in: "+fileName);
                } catch (IOException e) {
                    System.out.println("Error when writing to the file : " + fileName + " - " + e);
                }
            }
        }
        TabDelimitedTableReader.saveList(meanCoverages, Database.getInstance().getPath() + "MeanCovDNA.txt");
        

    }

    /**
     * For each RNAseq and Sequence element, calculate the mean value of copy number variation
     * THIS NEED TO BE RUN AFTER CONVERSION of RNASeq files
     */
    public static void calcGeneCNV() {
        /*
         * Calculate CNV per genes
         */
        TreeSet<String> comparisons = new TreeSet<>();
        for (BioCondition bioCond : BioCondition.getAllBioConditions()) {
            if (bioCond.getName().contains("DNA")) {
                for(String comparison : bioCond.getComparisonDataNames()) {
                    comparisons.add(comparison);
                }
                ArrayList<String> tableFinal = new ArrayList<>();
                tableFinal.add(ColNames.GenomeElements+"\t"+ColNames.VALUE);
                NGS rnaSeq = bioCond.getNGSSeqs().get(0);
                rnaSeq.load();

                Genome genome = Genome.loadGenome(Genome.Donovani_NAME);
                for(Sequence sequence : genome.getAllElements()) {
                    Chromosome chromo = sequence.getChromosome();
                    ExpressionData dataset = rnaSeq.getDatasets().get(chromo.getAccession().toString());
                    double[] values = dataset.read(sequence.getBegin(), sequence.getEnd());
                    tableFinal.add(sequence.getName() + "\t" + VectorUtils.mean(values));
                }
                TabDelimitedTableReader.saveList(tableFinal,
                        OmicsData.PATH_NGS_NORM + bioCond.getName() + NGS.EXTENSION);
            }
        }
        
        /**
         * Calculate CNV fold
         */
        for(String comparison : comparisons) {
            BioCondition bioCond1 = BioCondition.getBioCondition(BioCondition.parseName(comparison)[0]);
            BioCondition bioCond2 = BioCondition.getBioCondition(BioCondition.parseName(comparison)[1]);
            String[][] cnv1 = TabDelimitedTableReader.read(OmicsData.PATH_NGS_NORM + bioCond1.getName() + NGS.EXTENSION);
            String[][] cnv2 = TabDelimitedTableReader.read(OmicsData.PATH_NGS_NORM + bioCond2.getName() + NGS.EXTENSION);
            String[][] diff = new String[cnv1.length][cnv1[0].length];
            diff[0][0] = ColNames.GenomeElements+"";
            diff[0][1] = ColNames.LOGFC+"";
            System.out.println("CNV1: "+cnv1.length+ " CNV2:" + cnv2.length);
            for(int i=1; i<cnv1.length; i++) {
                String gene1 = cnv1[i][0];
                String gene2 = cnv2[i][0];
                if(gene1.equals(gene2)) {
                    double diffValue = Double.parseDouble(cnv1[i][1]) - Double.parseDouble(cnv2[i][1]);
                    diff[i][0] = cnv1[i][0];
                    diff[i][1] = diffValue+"";
                }else {
                    System.err.println(gene1 + " not equal " + gene2);
                }
            }
            TabDelimitedTableReader.save(diff,OmicsData.PATH_NGS_NORM + bioCond1.getName() + " vs " + bioCond2.getName() + NGS.EXTENSION);
            
            
        }

    }
}
