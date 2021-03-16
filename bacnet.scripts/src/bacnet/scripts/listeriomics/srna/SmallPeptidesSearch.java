package bacnet.scripts.listeriomics.srna;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import bacnet.Database;
import bacnet.datamodel.sequence.Gene;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.Blast;
import bacnet.scripts.blast.BlastOutput;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

/**
 * List of methods to search for small unannotated peptides in reference genomes<br>
 * If the peptide is not found we will try to find its surrounding genes in other strains and look
 * at thze intergenic region to find the peptide.
 * 
 * @author UIBC
 *
 */
@Deprecated
public class SmallPeptidesSearch {

    public static String GENOME_PATH = "D:/Listeria-BigData/Genomes/Bacillus-Staph-Listeria/";

    /**
     * List of methods to search for small unannotated peptides in reference genomes<br>
     * If the peptide is not found we will try to find its surrounding genes in other strains and look
     * at thze intergenic region to find the peptide.
     * 
     * @author UIBC
     *
     */
    public static void run() {

        /*
         * Run blastP of the surrounding genes
         */
        ArrayList<String> listGenomes = new ArrayList<>();
        try {
            listGenomes = createDatabase();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
         * Run blastP
         */
        // Genome genome = Genome.loadEgdeGenome();
        // Sequence geneUp = genome.getElement("lmo1374");
        // Sequence geneDown = genome.getElement("lmo1375");
        // Sequence rli42 = genome.getElement("lmo1375");
        // ArrayList<Sequence> genes = new ArrayList<>();
        // genes.add(geneUp);
        // genes.add(geneDown);
        // multiSpeciesAlignBlastN(genes);
        // for(int i=0;i<SRNA_NUMBER;i++){
        // for(Sequence gene : genes){
        // alignBlastP(gene.getName(),gene.getSequenceAA(),listGenomes);
        // }

        alignBlastP("HMPREF1015_00196",
                "MVIKLKKLWLILFIFAFLFPNSTFAQGSPLIVINKKTNELAFYNHGKLQMREKVATGKMNELTPEGLFTVTVKAKNPFYRKKNIPGGHPNNPLGTRWIGFNAKNTGGRIYGVHGTNNPSSIGHYISNGCIRMNNQAIERLYENVPIGTKIKVVTSTQSLDQIARQYGAIR",
                listGenomes);

    }

    /**
     * Create database file which contains all genome available <br>
     * 
     * This genome list contains all the genomes from GenomeFolder and all the genome contained in
     * GenomePhylogeny.PATH_TABLE (=All bacteria phylogeny)
     */
    public static ArrayList<String> createDatabase() throws IOException {
        /*
         * Find list of genomes and create a BlastN and BlastP database for each genome
         */
        File file = new File(GENOME_PATH);
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".fna"))
                    return true;
                return false;
            }
        });
        ArrayList<String> listGenomes = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String genome = FileUtils.removeExtension(files[i].getAbsolutePath());
            listGenomes.add(genome);

            /*
             * Create databases
             */
            String out = genome;
            String execProcess = Blast.makeblastdb + " -in \"" + files[i].getCanonicalPath() + "\" -out \"" + out
                    + "\" -title \"" + genome + "\" -parse_seqids -dbtype " + "nucl";
            // CMD.runProcess(execProcess, true);
            String fileName = FileUtils.removeExtension(files[i].getAbsolutePath()) + ".PATRIC.faa";
            execProcess = Blast.makeblastdb + " -in \"" + fileName + "\" -out \"" + out + "\" -title \"" + genome
                    + "\" -parse_seqids -dbtype " + "prot";
            CMD.runProcess(execProcess);
        }
        TabDelimitedTableReader.saveList(listGenomes, GENOME_PATH + "ListGenomes.txt");

        System.out.println("Fusion of the databases for Blast " + listGenomes.size());
        String text = "\\#\n\\# Alias file created 06/09/2011 16:47:20\n\\#\nTITLE ";
        text += "Blast database Bacillus-Staph-Listeria\n";
        text += "DBLIST ";
        double length = 0;
        int nseq = 0;
        for (int k = 0; k < listGenomes.size(); k++) {
            text += "\"" + listGenomes.get(k) + "\" ";
        }
        text += "\nNSEQ " + nseq;

        if (length < Integer.MAX_VALUE) {
            /*
             * sometimes this value can overcome the MAX_VALUE of an int<br> That is why I put it in a Double
             * and then convert to an int, to avoid Error: ncbi::NStr::StringToUInt8()
             */
            int lengthTemp = (int) length;
            text += "\nLENGTH " + lengthTemp;
        } else
            text += "\nLENGTH " + length;
        // System.out.println("Database fusion: "+text);
        FileUtils.saveText(text, Database.getTEMP_PATH() + "tempBlastDatabase" + ".nal");
        return listGenomes;
    }

    public static void multiSpeciesAlignBlastN(ArrayList<Gene> genes) {
        // for(int i=0;i<SRNA_NUMBER;i++){
        for (int i = 0; i < genes.size(); i++) {
            Gene gene = genes.get(i);
            String sequence = gene.getSequence();

            // save query sequence
            String blastQuery = ">" + gene.getName() + "\n" + sequence;
            System.out.println(blastQuery);
            String fileNameQuery = Database.getTEMP_PATH() + "tempSeq_" + gene.getName().replaceAll("/", "") + ".txt";
            FileUtils.saveText(blastQuery, fileNameQuery);

            // run Blast
            BlastOutputTYPE outType = BlastOutputTYPE.ASN;
            String blastResult = Database.getTEMP_PATH() + "resultBlastN_" + gene.getName().replaceAll("/", "")
                    + BlastOutput.fileExtension(outType);
            String tempDatabase = Database.getTEMP_PATH() + "tempBlastDatabase";
            final String[] args = {Blast.blastN, "-query", "\"" + fileNameQuery + "\"", "-db",
                    "\"" + tempDatabase + "\"", "-out", "\"" + blastResult + "\"", "-outfmt", outType.ordinal() + "",
                    "-evalue 0.001", "-word_size 4"};
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
        System.out.println("All process RNA");
    }

    public static void alignBlastP(String geneName, String sequenceAA, ArrayList<String> listGenomes) {
        String blastQuery = ">" + geneName + "\n" + sequenceAA;
        System.out.println(blastQuery);
        String fileNameQuery = Database.getTEMP_PATH() + "tempSeq_" + geneName + ".txt";
        FileUtils.saveText(blastQuery, fileNameQuery);
        for (String genome : listGenomes) {
            // run Blast
            BlastOutputTYPE outType = BlastOutputTYPE.ASN;
            String blastResult = Database.getTEMP_PATH() + "resultBlastP_" + geneName + "_"
                    + FileUtils.removeExtensionAndPath(genome) + "_" + BlastOutput.fileExtension(outType);
            // String tempDatabase = Database.getTEMP_PATH()+"tempBlastDatabase";
            String tempDatabase = genome;
            final String[] args = {Blast.blastP, "-query", "\"" + fileNameQuery + "\"", "-db",
                    "\"" + tempDatabase + "\"", "-out", "\"" + blastResult + "\"", "-outfmt", outType.ordinal() + "",
                    "-evalue 0.001", "-word_size 4"};
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
    }

}
