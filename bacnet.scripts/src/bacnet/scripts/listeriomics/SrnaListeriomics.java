package bacnet.scripts.listeriomics;

import java.io.File;
import bacnet.Database;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequenceNCBI.GenomeConversion;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.scripts.listeriomics.srna.SrnaFinalList;
import bacnet.utils.FileUtils;

/**
 * List of methods to manage Srna data for Listeriomics
 * 
 * @author UIBC
 *
 */
public class SrnaListeriomics {

    
    public static void run() {

        /*
         * Create folder for Srna
         */
//        if (!FileUtils.exists(Srna.PATH)) {
//            System.out.println("Create folder: " + Srna.PATH);
//            File file = new File(Srna.PATH);
//            file.mkdir();
//        }
        
        /*
         * Create EGD-e genome
         */
//        String genomeName = Genome.EGDE_NAME;
//        System.out.println("Convert: " + genomeName);
//        GenomeNCBI genomeNCBI = GenomeNCBITools.loadGenome(genomeName, GenomeNCBI.PATH_GENOMES, false, true);
//        GenomeConversion.run(genomeNCBI, GenomeNCBI.PATH_GENOMES + genomeName, genomeName);
        
        /*
         * create sRNAList and save in XML and serialize
         */

        //SrnaFinalList.createFinalList();
        // run 20/11/2012
        Srna.setSrnaOrder();
        //SrnaFinalList.createSummaryTables();
        //SrnaFinalList.createFoldingFigures();

        /*
         * Need to copy manually all these files:
         * 
         * public static String PATHFigure_Srna = Database.getANNOTATIONDATA_PATH() + "SrnaCircularGenome.png";
         * public static String PATHFigure_ASrna = Database.getANNOTATIONDATA_PATH() + "ASrnaCircularGenome.png";
         * public static String PATHFigure_CISReg = Database.getANNOTATIONDATA_PATH() + "CisRegCircularGenome.png";
         * public static String PATHTABLE_SrnaReference = Database.getANNOTATIONDATA_PATH() + "sRNAReference.txt";
         * 
         */
        

        // SrnaAlignmentBlastN.run();
        // SrnaAlignmentBlastP.run();

        /*
         * cell width = 8 ; cell height = 15 Use ConservationColorMapper.col
         */
        // create Circular genome images
        //CircularGenomeJPanel panel = new CircularGenomeJPanel(100, 100, TypeSrna.Srna);
        // try {
        // CgviewIO.writeToPNGFile(panel.getCgview(), "D:/circularView.png");
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // CircularGenomeJPanel panel = new CircularGenomeJPanel(300,300,TypeSrna.Srna);
        // CgviewIO.writeToPNGFile(panel.getCgview(), Srna.PATHFigure_Srna);
        // //CgviewIO.writeHTMLFile(panel.getCgview(), "img"+File.separator+"Genome sRNAs.png", "PNG",
        // WebUtils.WEBPATH+"Genome sRNAs.html", true);
        // panel = new CircularGenomeJPanel(300,300,TypeSrna.CisReg);
        // CgviewIO.writeToPNGFile(panel.getCgview(), Srna.PATHFigure_CISReg);
        // panel = new CircularGenomeJPanel(300,300,TypeSrna.ASrna);
        // CgviewIO.writeToPNGFile(panel.getCgview(), Srna.PATHFigure_ASrna);
        // CgviewIO.writeHTMLFile(panel.getCgview(), "img"+File.separator+"Genome asRNAs.png", "PNG",
        // WebUtils.WEBPATH+"Genome asRNAs.html", true);

    }
}
