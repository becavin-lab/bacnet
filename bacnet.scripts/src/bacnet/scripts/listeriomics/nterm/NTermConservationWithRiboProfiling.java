package bacnet.scripts.listeriomics.nterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import bacnet.Database;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.scripts.blast.Blast;
import bacnet.scripts.blast.BlastOutput;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

/**
 * Run different method to compare iTIS position (internal translation) to data of Ribosome
 * profiling in Bacillus and Ecoli:<br>
 * <li>compare the gene by BlastN
 * <li>find internal translation in the ribosome profiling
 * <li>find internal SD sequence
 * 
 * @author UIBC
 *
 */
public class NTermConservationWithRiboProfiling {

    public static void run() {

        // String[] genomesInput = { Genome.BACSUBTILIS_NAME, Genome.ECOLI_NAME };
        // Blast.createBlastDatabases(GenomeNCBITools.getPATH(), genomesInput, false);
        // String tempDatabase = Database.getTEMP_PATH()+"tempBlastDatabase";
        // String title = "Database containing "+2+" bacterial genomes";
        // ArrayList<String> databases = new ArrayList<String>();
        // databases.add(GenomeNCBITools.getPATH()+Genome.BACSUBTILIS_NAME+File.separator+Genome.BACSUBTILIS_NAME);
        // databases.add(GenomeNCBITools.getPATH()+Genome.ECOLI_NAME+File.separator+Genome.ECOLI_NAME);
        // Blast.createFusionBlastDatabase(databases, title, tempDatabase,true);
        // String[][] iTISArray = TabDelimitedTableReader.read(NTermUtils.getPATH()+"iTIS preliminary
        // list.txt");
        // Genome genome = Genome.loadEgdeGenome();
        // ArrayList<Sequence> sequences = new ArrayList<>();
        // for(int i=1;i<iTISArray.length;i++){
        // Sequence sequence = genome.getElement(iTISArray[i][0]);
        // //System.out.println("Load:"+sequence.getName()+" size "+sequence.getLength()+" sizeExpect
        // "+iTISArray[i][1]);
        // int iTISPos = Integer.parseInt(iTISArray[i][2]);
        // System.out.println("iTIS: "+iTISPos+" codon "+sequence.getSequence().substring(iTISPos,
        // iTISPos+3)+" expect "+iTISArray[i][3]);
        // sequences.add(sequence);
        // }
        // multiAlign(sequences,"_Bsubtilis",Genome.BACSUBTILIS_NAME);
        // multiAlign(sequences,"_Ecoli",Genome.ECOLI_NAME);

    }

    /**
     * Run a Blast for each sRNA on a list of Genomes given in tempDatabase
     * 
     * @param sRNAs
     * @throws IOException
     */
    public static void multiAlign(ArrayList<Sequence> sequences, String suffix, String genomeName) {
        // for(int i=0;i<SRNA_NUMBER;i++){
        for (int i = 0; i < sequences.size(); i++) {
            Sequence sequence = sequences.get(i);
            String sequenceString = sequence.getSequenceAA();

            // save query sequence
            String blastQuery = ">" + sequence.getName() + "\n" + sequenceString;
            System.out.println(blastQuery);
            String fileNameQuery =
                    Database.getTEMP_PATH() + "tempSeq_" + sequence.getName().replaceAll("/", "") + ".txt";
            FileUtils.saveText(blastQuery, fileNameQuery);

            // run Blast
            BlastOutputTYPE outType = BlastOutputTYPE.ASN;
            String blastResult = Database.getTEMP_PATH() + "resultBlast_" + sequence.getName().replaceAll("/", "")
                    + suffix + BlastOutput.fileExtension(outType);
            String tempDatabase = GenomeNCBITools.getPATH() + genomeName + File.separator + genomeName;
            final String[] args = {Blast.blastP, "-query", "\"" + fileNameQuery + "\"", "-db",
                    "\"" + tempDatabase + "\"", "-out", "\"" + blastResult + "\"", "-outfmt", outType.ordinal() + ""};
            try {
                // run Blast
                CMD.runProcess(args);
                // convert asn in different format
                String blastResultHTML =
                        Database.getTEMP_PATH() + File.separator + FileUtils.removeExtensionAndPath(blastResult);
                // convert in HTML to put in the webpage
                BlastOutput.convertOuput(blastResult, blastResultHTML, true, BlastOutputTYPE.PAIRWISE);
                // convert in XML to analyse results
                BlastOutput.convertOuput(blastResult, FileUtils.removeExtension(blastResult), false,
                        BlastOutputTYPE.XML);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("All process Genes");
    }

    // public static void readResults(ArrayList<Sequence> sequences){
    // BlastResult.getResultsFromXML(fileName)
    // }

}
