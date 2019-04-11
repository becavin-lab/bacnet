package bacnet.scripts.listeriomics.srna;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import org.biojava3.core.sequence.Strand;
import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequence.Srna.TypeSrna;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.Blast;
import bacnet.scripts.blast.BlastOutput;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.scripts.blast.BlastResult;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class Srna10403SOliver {

    /**
     * Get all Srna from Oliver et al. 2009 list, get Sequence in
     * <code>10403S pseudo_chromosome</code><br>
     * Run BlastN on EGD-e.<br>
     * When Blast are all performed, summarize information in a Table.<br>
     * Choose which rli to keep
     */
    public static void findEGDePosition() {
        ArrayList<Srna> sRNAsTemp = new ArrayList<Srna>();
        ArrayList<Srna> sRNAs = new ArrayList<Srna>();
        sRNAsTemp = SrnaTables.getOliverInfo();
        /*
         * Curate list of Srnas by deleting those with MISSING_VALUE
         */
        for (Srna sRNA : sRNAsTemp) {
            if (sRNA.getBegin() == OmicsData.MISSING_VALUE || sRNA.getEnd() == OmicsData.MISSING_VALUE) {
                System.out.println("Do not include " + sRNA.getName());
            } else if (sRNA.getTypeSrna() == TypeSrna.Srna) {
                sRNAs.add(sRNA);
            }
        }

        /*
         * Run Blast
         */
        // runBlast(sRNAs);
        /*
         * Summarize in a table
         */
        try {
            summarizeResultsInTable(sRNAs);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Run Blast of all Srna found in Oliver et al study against EGD-e and full 10403S sequence
     */
    public static void runBlast(ArrayList<Srna> sRNAs) {
        /*
         * Create Blast database containing EGD-e and 10403S
         */
        createDatabase();
        /*
         * run Blast by extracting sequence from "10403S pseudo_chromosome"
         */
        GenomeNCBI pseudoChromo10403S = GenomeNCBITools.loadGenome("10403S pseudo_chromosome", Srna.PATH, true, false);
        for (Srna sRNA : sRNAs) {
            String sequence = pseudoChromo10403S.getFirstChromosome().getSequenceAsString(sRNA.getBegin(),
                    sRNA.getEnd(), Strand.POSITIVE);
            // save query sequence
            String blastQuery = ">" + sRNA.getName() + "\n" + sequence;
            System.out.println(blastQuery);
            String fileNameQuery = Database.getTEMP_PATH() + "tempSeq_" + sRNA.getName().replaceAll("/", "") + ".txt";
            FileUtils.saveText(blastQuery, fileNameQuery);

            // run Blast
            BlastOutputTYPE outType = BlastOutputTYPE.ASN;
            String blastResult = Database.getTEMP_PATH() + "resultBlast_" + sRNA.getName().replaceAll("/", "")
                    + BlastOutput.fileExtension(outType);
            String tempDatabase = Database.getTEMP_PATH() + "tempBlastDatabase";
            final String[] args = {Blast.blastN, "-query", "\"" + fileNameQuery + "\"", "-db",
                    "\"" + tempDatabase + "\"", "-out", "\"" + blastResult + "\"", "-outfmt", outType.ordinal() + "",
                    "-evalue 0.001", "-word_size 4"};
            try {
                // run Blast
                CMD.runProcess(args, true);
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

    /**
     * Create database file which contains all genome available <br>
     * It will be used by Blast afterwards<br>
     * <br>
     * 
     * BE CAREFULL THAT THESE GENOMES HAVE BEEN ESTABLISHED FOR BLAST SEARCH (=database is created
     * through Blast.createBlastDatabases();)
     * 
     */
    public static void createDatabase() {
        // Blast.createBlastDatabases();
        @SuppressWarnings("unused")
        String tempDatabase = Database.getTEMP_PATH() + "tempBlastDatabase";
        ArrayList<String> databases = new ArrayList<String>();
        // save map between genomeNCBI name and NWK ID in the phylogeny tree
        String genome = Genome.EGDE_NAME;
        databases.add(GenomeNCBITools.getPATH() + genome + File.separator + genome);
        String genome3 = Genome.DP10403S_NAME;
        databases.add(GenomeNCBITools.getPATH() + genome3 + File.separator + genome3);

        // String title = "Database containing " + databases.size() + " bacterial genomes";
        // Blast.createFusionBlastDatabase(databases, title, tempDatabase,true);

    }

    /**
     * Read all the XML results of Blast and create a sRNA conservation table<br>
     * header = {"Srna","EGD-e identites","EGD-e begin","EGD-e end","10403S identities","10403S
     * begin","10403S end","10403S Average GEI","Delta_sigB Average GEI","FC range
     * (10403S/Delta_sigB)"};
     * 
     * @param sRNAs
     * @return
     * @throws IOException
     */
    public static void summarizeResultsInTable(ArrayList<Srna> sRNAs) throws IOException {

        extractInfo(sRNAs);

        String[] headers = {"Srna", "EGD-e identites", "EGD-e begin", "EGD-e end", "10403S identities", "10403S begin",
                "10403S end", "10403S Average GEI", "Delta_sigB Average GEI", "FC range (10403S/Delta_sigB)"};
        String[][] results = new String[sRNAs.size() + 1][headers.length];
        int w = 0;
        for (String header : headers) {
            results[0][w] = header;
            w++;
        }

        // for(int i=0;i<SRNA_NUMBER;i++){
        for (int i = 0; i < sRNAs.size(); i++) {
            Srna sRNA = sRNAs.get(i);
            results[i + 1][0] = sRNA.getName();
            // fill the array with 0
            for (int j = 1; j < results[0].length; j++)
                results[i + 1][j] = "0";
            results[i + 1][7] = sRNA.getFeature("10403S Average GEI (Oliver et al. 2009)");
            results[i + 1][8] = sRNA.getFeature("Delta_sigB Average GEI (Oliver et al. 2009)");
            results[i + 1][9] = sRNA.getFeature("FC range (10403S/Delta_sigB) (Oliver et al. 2009)");
            // read blast result
            String[][] bltResults = TabDelimitedTableReader.read(Database.getTEMP_PATH() + sRNA.getName() + ".txt");

            // add the info to the sRNA conservation table
            for (int k = 0; k < bltResults.length; k++) {
                String genomeAccession = bltResults[k][0];
                // get genome name
                if (genomeAccession.equals("CP002002")) {
                    results[i + 1][4] = bltResults[k][2];
                    results[i + 1][5] = bltResults[k][3];
                    results[i + 1][6] = bltResults[k][4];
                } else if (genomeAccession.equals("NC_003210")) {
                    results[i + 1][1] = bltResults[k][2];
                    results[i + 1][2] = bltResults[k][3];
                    results[i + 1][3] = bltResults[k][4];
                }
            }
        }
        TabDelimitedTableReader.save(results, SrnaTables.PATH + "sRNA Oliver-1 aligned.txt");
    }

    /**
     * From Blast Result extract Genome containing the sRNA<br>
     * 
     * @param sRNAs
     * @throws IOException
     */
    private static void extractInfo(ArrayList<Srna> sRNAs) {
        // read each sRNA XML file and right the table accordingly
        // for(int i=0;i<SRNA_NUMBER;i++){
        for (int i = 0; i < sRNAs.size(); i++) {
            Srna sRNA = sRNAs.get(i);
            // we need to delete one line in the file otherwise it will not be read
            String blastResult = Database.getTEMP_PATH() + "resultBlast_" + sRNA.getName().replaceAll("/", "") + ".xml";
            String text = FileUtils.readText(blastResult);
            String[] lines = text.split("\n");
            // delete line 2
            if (lines[1].contains("DOCTYPE BlastOutput")) {
                lines[1] = lines[0];
                text = "";
                for (int j = 1; j < lines.length; j++) {
                    text += lines[j] + "\n";
                }
                FileUtils.saveText(text, blastResult);
            }
            // get Blast results
            ArrayList<BlastResult> results = BlastResult.getResultsFromXML(blastResult);
            // delete duplicates
            TreeMap<String, String[]> resultsTree = new TreeMap<String, String[]>();
            for (BlastResult blt : results) {
                System.out.println(blt.hit_accession + " " + blt.eValue + " " + blt.bitScore + "  " + blt.identities);
                if (resultsTree.containsKey(blt.hit_accession)) {
                    // get result with highest score
                    double evaluePrevious = Double.parseDouble(resultsTree.get(blt.hit_accession)[0]);
                    double evalue = blt.eValue;
                    if (evaluePrevious > evalue) {
                        String[] result =
                                {blt.eValue + "", blt.identities + "", blt.begin + "", blt.end + "", blt.strand + ""};
                        resultsTree.put(blt.hit_accession, result);
                    }
                    // get result with highest identities
                    // double identities =
                    // double currentIdentities = blt.identities;
                    // if(currentIdentities > identities){
                    // resultsTree.put(blt.hit_accession, currentIdentities);
                    // }
                } else {
                    String[] result =
                            {blt.eValue + "", blt.identities + "", blt.begin + "", blt.end + "", blt.strand + ""};
                    resultsTree.put(blt.hit_accession, result);
                }
            }

            // write results
            String ret = "";
            for (String def : resultsTree.keySet()) {
                ret += def + "\t";
                for (String temp : resultsTree.get(def)) {
                    ret += temp + "\t";
                }
                ret += "\n";
            }

            FileUtils.saveText(ret, Database.getTEMP_PATH() + sRNA.getName().replaceAll("/", "") + ".txt");
        }
    }
}
