package bacnet.scripts.blast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import bacnet.Database;
import bacnet.datamodel.sequence.Sequence;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class Blast {

    public static String PATH_BLAST_WIN = "C:/Program Files/NCBI/blast-2.3.0+/bin/";
    public static String PATH_BLAST_MAC = "/opt/blast-2.3.0+/bin/";

    public static String tblastN = "\"" + getBlastFolder() + "tblastn\" -task tblastn";
    public static String blastN = "\"" + getBlastFolder() + "blastn\" -task blastn";
    public static String blastP = "\"" + getBlastFolder() + "blastp\" -task blastp";
    public static String blastX =
            "\"" + getBlastFolder() + "blastx\" -num_threads " + (Runtime.getRuntime().availableProcessors());
    public static String blastdbcmd = "\"" + getBlastFolder() + "blastdbcmd\"";
    public static String makeblastdb = "\"" + getBlastFolder() + "makeblastdb\"";
    public static String blastdb_aliastool = "\"" + getBlastFolder() + "blastdb_aliastool\"";
    public static String blast_formatter = "\"" + getBlastFolder() + "blast_formatter\"";

    public static String getBLAST_RESULT_PATH() {
        return Database.getTEMP_PATH() + "resultBlast";
    }

    /*
     * *************************************************** Blast processes
     * ***************************************************
     */

    public static String blastNucleotide() {
        return "";
    }

    public static String showSearchPath() throws IOException {
        String execProcess = Blast.blastdbcmd + " -show_blastdb_search_path";
        return CMD.runProcess(execProcess, true);
    }

    public static String listAllDB(String path) throws IOException {
        String execProcess = Blast.blastdbcmd + " -list " + path + " -recursive";
        return CMD.runProcess(execProcess, true);
    }

    public static String getBlastDBInfo(String database) throws IOException {
        String[] args = {Blast.blastdbcmd, "-db", database, "-entry", "all"};
        return CMD.runProcess(args, true);
    }

    /**
     * Run a Blast for each sRNA on a list of Genomes given in tempDatabase
     * 
     * @param sRNAs
     */
    public static void multiSpeciesAlignNucleotide(ArrayList<Sequence> seqs, boolean smallSequence) {

        // prepareDatabase();

        // for(int i=0;i<SRNA_NUMBER;i++){
        for (int i = 0; i < seqs.size(); i++) {
            Sequence seq = seqs.get(i);
            if (seq.getEnd() == -1000000) {
                if (seq.isStrand()) {
                    seq.setEnd(seq.getBegin() + 150);
                } else {
                    seq.setEnd(seq.getBegin());
                    seq.setBegin(seq.getEnd() - 150);
                }
            }
            String sequence = seq.getSequence();

            // save query sequence
            String blastQuery = ">" + seq.getName() + "\n" + sequence;
            System.out.println(blastQuery);
            String fileNameQuery = Database.getTEMP_PATH() + "tempSeq_" + seq.getName().replaceAll("/", "") + ".txt";
            FileUtils.saveText(blastQuery, fileNameQuery);

            // run Blast

            BlastOutputTYPE outType = BlastOutputTYPE.ASN;
            String blastResult = Database.getTEMP_PATH() + "resultBlast_" + seq.getName().replaceAll("/", "")
                    + BlastOutput.fileExtension(outType);
            String tempDatabase = Database.getTEMP_PATH() + "tempBlastDatabase";
            String[] args = {Blast.blastN, "-query", "\"" + fileNameQuery + "\"", "-db", "\"" + tempDatabase + "\"",
                    "-out", "\"" + blastResult + "\"", "-outfmt", outType.ordinal() + ""};
            if (smallSequence) {
                String[] newArgs = {Blast.blastN, "-query", "\"" + fileNameQuery + "\"", "-db",
                        "\"" + tempDatabase + "\"", "-out", "\"" + blastResult + "\"", "-outfmt",
                        outType.ordinal() + "", "-evalue 0.001", "-word_size 4"};
                args = newArgs;
            }
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
        System.out.println("All process Blast");
    }

    /**
     * Run a BlastP given a a list of sequence name, amino acid sequences, and a database name
     * 
     * @param names a list of sequence names
     * @param sequences A list of amino acid sequences
     * @param database
     */
    public static void multiSpeciesAlignProtein(ArrayList<String> names, ArrayList<String> sequences, String database,
            double evalueCutoff) {
        for (int i = 0; i < names.size(); i++) {
            alignProtein(names.get(i), sequences.get(i), database, evalueCutoff);
        }
    }

    /**
     * Run a BlastP given a sequence name, an amino acid sequence and a database name
     * 
     * @param name
     * @param sequence
     * @param database
     */
    public static void alignProtein(String name, String sequence, String database, double evalueCutoff) {

        // prepareDatabase();
        String tempPath = Database.getTEMP_PATH() + name + File.separator + "Blast" + File.separator;
        File temp = new File(tempPath);
        temp.mkdir();
        // save query sequence
        String blastQuery = ">" + name + "\n" + sequence;
        String fileNameQuery = Database.getTEMP_PATH() + "tempSeq_" + name + ".txt";
        FileUtils.saveText(blastQuery, fileNameQuery);

        // run Blast
        BlastOutputTYPE outType = BlastOutputTYPE.ASN;
        String blastResult = tempPath + "resultBlast_" + name + "_" + FileUtils.removePath(database)
                + BlastOutput.fileExtension(outType);
        // String tempDatabase = Database.getTEMP_PATH()+"tempBlastDatabase";
        final String[] args = {Blast.blastP, "-query", "\"" + fileNameQuery + "\"", "-db", "\"" + database + "\"",
                "-out", "\"" + blastResult + "\"", "-outfmt", outType.ordinal() + "", "-evalue", evalueCutoff + ""};

        try {
            // run Blast
            CMD.runProcess(args, true);
            // convert asn in different format
            String blastResultHTML = tempPath + File.separator + FileUtils.removeExtensionAndPath(blastResult);
            // convert in HTML to put in the webpage
            BlastOutput.convertOuput(blastResult, blastResultHTML, true, BlastOutputTYPE.PAIRWISE);
            // convert in XML to analyse results
            BlastOutput.convertOuput(blastResult, FileUtils.removeExtension(blastResult), false, BlastOutputTYPE.XML);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Blast process: " + name);
    }

    /**
     * Run TblastN on multy amino acid sequence against nucleotide database that will be translated in
     * the 6 frame
     * 
     * @param names
     * @param sequences
     * @param database
     * @param evalueCutoff
     */
    public static void multiSpeciesAlignTblastN(ArrayList<String> names, ArrayList<String> sequences, String database,
            double evalueCutoff) {
        for (int i = 0; i < names.size(); i++) {
            alignTblastN(names.get(i), sequences.get(i), database, evalueCutoff);
        }
    }

    /**
     * Run a BlastN given a sequence name, an amino acid sequence and a database name
     * 
     * @param name nama of the sequence
     * @param sequence nucleotide sequence
     * @param database BlastN database
     */
    public static void alignblastN(String name, String sequence, String database, double evalueCutoff) {

        // prepareDatabase();
        String tempPath = Database.getTEMP_PATH() + name + File.separator + "Blast" + File.separator;
        File temp = new File(tempPath);
        temp.mkdir();
        // save query sequence
        String blastQuery = ">" + name + "\n" + sequence;
        String fileNameQuery = Database.getTEMP_PATH() + "tempSeq_" + name + ".txt";
        FileUtils.saveText(blastQuery, fileNameQuery);

        // run Blast
        BlastOutputTYPE outType = BlastOutputTYPE.ASN;
        String blastResult = tempPath + "resultBlast_" + name + "_" + FileUtils.removePath(database)
                + BlastOutput.fileExtension(outType);
        // String tempDatabase = Database.getTEMP_PATH()+"tempBlastDatabase";
        // final String[] args =
        // {Blast.tblastN,"-query","\""+fileNameQuery+"\"","-db","\""+database+"\"","-out","\""+blastResult+"\"","-outfmt",outType.ordinal()+"","-evalue",evalueCutoff+""};
        final String[] args = {Blast.blastN, "-query", "\"" + fileNameQuery + "\"", "-db", "\"" + database + "\"",
                "-out", "\"" + blastResult + "\"", "-outfmt", outType.ordinal() + ""};

        // wordsize 2-3
        // BLOSUM 30 si je m attend a peu de complexite
        // virer le filtre de complexite
        // positives = nub of positive scroing

        try {
            // run Blast
            CMD.runProcess(args, true);
            // convert asn in different format
            String blastResultHTML = tempPath + File.separator + FileUtils.removeExtensionAndPath(blastResult);
            // convert in HTML to put in the webpage
            BlastOutput.convertOuput(blastResult, blastResultHTML, true, BlastOutputTYPE.PAIRWISE);
            // convert in XML to analyse results
            BlastOutput.convertOuput(blastResult, FileUtils.removeExtension(blastResult), false, BlastOutputTYPE.XML);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Blast process: " + name);
    }

    /**
     * Run a TBlastN given a sequence name, an amino acid sequence and a database name
     * 
     * @param name
     * @param sequence amino acid sequence
     * @param database BlastN database
     */
    public static void alignTblastN(String name, String sequence, String database, double evalueCutoff) {

        // prepareDatabase();
        String tempPath = Database.getTEMP_PATH() + name + File.separator + "Blast" + File.separator;
        File temp = new File(tempPath);
        temp.mkdir();
        // save query sequence
        String blastQuery = ">" + name + "\n" + sequence;
        String fileNameQuery = Database.getTEMP_PATH() + "tempSeq_" + name + ".txt";
        FileUtils.saveText(blastQuery, fileNameQuery);

        // run Blast
        BlastOutputTYPE outType = BlastOutputTYPE.ASN;
        String blastResult = tempPath + "resultBlast_" + name + "_" + FileUtils.removePath(database)
                + BlastOutput.fileExtension(outType);
        // String tempDatabase = Database.getTEMP_PATH()+"tempBlastDatabase";
        // final String[] args =
        // {Blast.tblastN,"-query","\""+fileNameQuery+"\"","-db","\""+database+"\"","-out","\""+blastResult+"\"","-outfmt",outType.ordinal()+"","-evalue",evalueCutoff+""};
        final String[] args = {Blast.tblastN, "-query", "\"" + fileNameQuery + "\"", "-db", "\"" + database + "\"",
                "-out", "\"" + blastResult + "\"", "-outfmt", outType.ordinal() + ""};

        // wordsize 2-3
        // BLOSUM 30 si je m attend a peu de complexite
        // virer le filtre de complexite
        // positives = nub of positive scroing

        try {
            // run Blast
            CMD.runProcess(args, true);
            // convert asn in different format
            String blastResultHTML = tempPath + File.separator + FileUtils.removeExtensionAndPath(blastResult);
            // convert in HTML to put in the webpage
            BlastOutput.convertOuput(blastResult, blastResultHTML, true, BlastOutputTYPE.PAIRWISE);
            // convert in XML to analyse results
            BlastOutput.convertOuput(blastResult, FileUtils.removeExtension(blastResult), false, BlastOutputTYPE.XML);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Blast process: " + name);
    }

    /**
     * From Blast Result extract Genome containing the sRNA<br>
     * 
     * 
     * @param sRNAs
     * @throws IOException
     */
    public static void extractInfo(ArrayList<String> sequenceNames, String genome, String suffix, double evalueCutoff,
            boolean best) {
        // read each sRNA XML file and right the table accordingly
        // for(int i=0;i<SRNA_NUMBER;i++){
        for (int i = 0; i < sequenceNames.size(); i++) {
            // we need to delete one line in the file otherwise it will not be read
            File blastResult = new File(Database.getTEMP_PATH() + sequenceNames.get(i) + "/Blast/resultBlast_"
                    + sequenceNames.get(i) + "_" + genome + suffix + ".xml");
            System.out.println(blastResult.getAbsolutePath());
            if (blastResult.exists()) {
                String text = FileUtils.readText(blastResult.getAbsolutePath());
                String[] lines = text.split("\n");
                // delete line 2
                if (lines[1].contains("DOCTYPE BlastOutput")) {
                    lines[1] = lines[0];
                    text = "";
                    for (int j = 1; j < lines.length; j++) {
                        text += lines[j] + "\n";
                    }
                    FileUtils.saveText(text, blastResult.getAbsolutePath());
                }
                // get Blast results
                ArrayList<BlastResult> results = BlastResult.getResultsFromXML(blastResult.getAbsolutePath());

                // filter results
                // results = BlastResult.filterIdentities(results, 10);

                // delete duplicates
                TreeMap<String, String[]> resultsTree = new TreeMap<String, String[]>();

                if (results.size() != 0) {
                    double evalueMin = results.get(0).eValue;
                    for (BlastResult blt : results) {
                        /*
                         * A cutoff is applyed here!!!!!!!!
                         */
                        if (blt.eValue < evalueCutoff) {
                            String accession = blt.hit_accession;
                            if (best) {
                                /*
                                 * Select only one best result
                                 */
                                accession = "Best";
                            }

                            /*
                             * If duplicate get result with lowest evalue
                             */
                            if (resultsTree.containsKey(accession)) {
                                double evalue = blt.eValue;
                                if (evalueMin > evalue) {
                                    evalueMin = evalue;
                                    String[] result = {blt.eValue + "", blt.identities + "", blt.ident + "",
                                            blt.bitScore + "", blt.hit_id + "", blt.hit_def + "", blt.hSequence};
                                    resultsTree.put(accession, result);
                                }
                            } else {
                                String[] result = {blt.eValue + "", blt.identities + "", blt.ident + "",
                                        blt.bitScore + "", blt.hit_id + "", blt.hit_def + "", blt.hSequence};
                                resultsTree.put(accession, result);
                            }
                        }
                    }

                }
                // write results
                String ret = "";
                ret += "hit_accession\teValue\tidentities\tident\tbitScore\thit_id\tblt.hit_def\thit_seq\n";

                for (String def : resultsTree.keySet()) {
                    ret += def + "\t" + resultsTree.get(def)[0] + "\t" + resultsTree.get(def)[1] + "\t"
                            + resultsTree.get(def)[2] + "\t" + resultsTree.get(def)[3] + "\t" + resultsTree.get(def)[4]
                            + "\t" + resultsTree.get(def)[5] + "\t" + resultsTree.get(def)[6] + "\n";
                }

                FileUtils.saveText(ret, Database.getTEMP_PATH() + sequenceNames.get(i) + "/Blast/resultBlast_"
                        + sequenceNames.get(i) + "_" + genome + suffix + ".txt");
            }
        }
    }

    /*
     * ********************************************* Blast settings management
     * *********************************************
     */

    public static String getBlastFolder() {
        String os = System.getProperty("os.arch");
        if (os.equals("amd64"))
            return PATH_BLAST_WIN;
        else
            return PATH_BLAST_MAC;
    }

    // public static String setBlastFolder(){
    // DirectoryDialog fd = new
    // DirectoryDialog(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
    // SWT.OPEN);
    // fd.setText("Select Blast folder (containg blast executable, usually in
    // /program/NCBI/blast/bin/)");
    // String fileName = fd.open();
    // if(fileName!=null){
    // System.out.println("New Blast folder: " + fileName);
    // Activator.getDefault().getPreferenceStore().setValue(BLAST_DIR,fileName);
    // System.out.println("This new folder is Blast folder: "+isBlastFolder(new
    // File(fileName)));
    // }
    // return fileName;
    // }

    /*
     * ************************************************
     * 
     * Blast database management
     * 
     * ************************************************
     */
    /**
     * Create blast database for each genome contained in GenomeFolder
     * 
     * !!!!!!!!!! WARNING !!!!!!!! Only the first .faa or .fna file will be taken into account Since
     * 2016 NCBI RefSeq database is organized with only one fna file for each genome
     * 
     * @throws IOException
     */
    public static void createBlastDatabases(String pathInput, String[] genomesInput, boolean nucleotide,
            boolean smallORF) {
        // build the fusion of this databases
        final String[] genomes = genomesInput;
        final String pathFolder = pathInput;
        String dbType = "prot";
        if (nucleotide)
            dbType = "nucl";
        final String dbtypeFinal = dbType;

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (String genomeTemp : genomes) {
            final String genome = genomeTemp;
            final boolean smallORFTemp = smallORF;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        String path = pathFolder + genome;
                        String suffix = ".ORF";
                        if (smallORFTemp)
                            suffix = ".SmallORF";
                        if (dbtypeFinal.equals("nucl")) {
                            suffix = "";
                        }

                        File file = new File(path);
                        String filter = ".fna";
                        File[] files = new File[0];
                        if (dbtypeFinal.equals("prot")) {
                            filter = ".faa";
                        }
                        final String filterFinal = suffix + filter;
                        files = file.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                if (name.endsWith(filterFinal))
                                    return true;
                                return false;
                            }
                        });
                        File fileTemp = files[0];
                        System.out.println("Add: " + fileTemp.getAbsolutePath());

                        final String out = path + File.separator + genome + suffix;
                        String execProcess = Blast.makeblastdb + " -in \"" + files[0].getCanonicalPath()
                                + "\" -parse_seqids -out \"" + out + "\" -dbtype " + dbtypeFinal + " -title " + genome;
                        System.out.println(execProcess);
                        CMD.runProcess(execProcess, true);

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            };
            executor.execute(runnable);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Interrupted exception");
        }
        System.err.println("All Threads done");
    }

}
