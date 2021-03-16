package bacnet.scripts.blast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import bacnet.Database;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Codon;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import bacnet.utils.VectorUtils;

public class MultiSequenceTBlastNProtein {

    public static String PATH_GENOMES_LIST = "ListGenome.txt";

    public static void run(String fileName, double evalueCutoff, String pathGenome, String listBacteriaFileName) {

        /*
         * Download genomes
         */

        /*
         * Create database
         */
        // if(smallORF){
        // createDatabaseForSmallORF();
        // }else{
        // createDatabaseForORF();
        // }

        /*
         * Set-up variables
         */
        String suffix = "";
        ArrayList<String> orfs = new ArrayList<String>();
        if (FileUtils.getExtension(fileName).equals(".txt")) {
            HashMap<String, String> nameToAminoAcidSequence = TabDelimitedTableReader.readHashMap(fileName);
            for (String key : nameToAminoAcidSequence.keySet()) {
                orfs.add(key + ";" + nameToAminoAcidSequence.get(key));
                System.out.println(key + ";" + nameToAminoAcidSequence.get(key));
                File file = new File(Database.getTEMP_PATH() + key + "/");
                file.mkdir();
            }
        } else if (FileUtils.getExtension(fileName).equals(".fasta")) {
            try {
                FileInputStream inStream = new FileInputStream(fileName);
                FastaReader<ProteinSequence, AminoAcidCompound> fastaReader =
                        new FastaReader<ProteinSequence, AminoAcidCompound>(inStream,
                                new NCBIFastaHeaderParser<ProteinSequence, AminoAcidCompound>(),
                                new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
                LinkedHashMap<String, ProteinSequence> genomeSequences = fastaReader.process();
                for (String key : genomeSequences.keySet()) {
                    orfs.add(key + ";" + genomeSequences.get(key).getSequenceAsString());
                    System.out.println(key + ";" + genomeSequences.get(key).getSequenceAsString());
                    File file = new File(Database.getTEMP_PATH() + key + "/");
                    file.mkdir();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /*
         * Blast sequences
         */
        blastORF(orfs, suffix, pathGenome, listBacteriaFileName, evalueCutoff);

        /*
         * Extract info and summarize in one table
         */
        // extractInfoORF(orfs, evalueCutoff, suffix, smallORF,true);

    }

    /**
     * On each genome, blastP ORF
     * 
     * @param smallORF
     * @param geneLeftEGDe
     * @param geneRightEGDe
     */
    private static void blastORF(ArrayList<String> oRFs, String suffix, String pathGenome, String listBacteriaFileName,
            double evalueCutoff) {
        final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(listBacteriaFileName);
        ExecutorService executor = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors());
        for (String oRFTemp : oRFs) {
            // for(int k=1;k<10;k++){
            for (String genomeNameTemp : listBacteria) {
                final String genomeName = genomeNameTemp;
                final String orf = oRFTemp;
                final String suffixFinal = suffix;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String database = pathGenome + genomeName + File.separator + genomeName + suffixFinal;
                            // File file = new
                            // File(GenomeNCBITools.PATH_NCBI_WIN+genomeName+File.separator+genomeName+".phr");
                            // if(file.length()>10){ // Database exists
                            String name = orf.split(";")[0];
                            String seq = orf.split(";")[1];
                            // System.out.println(name+" "+seq);
                            Blast.alignTblastN(name, seq, database, evalueCutoff);
                            // }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                };
                executor.execute(runnable);
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Interrupted exception");
        }
        System.err.println("All Threads done");
    }

    /**
     * Create a text file with all results for each genome and orf
     * 
     * @param oRFs
     * @param evalueCutoff
     * @param suffix
     * @param smallORF
     * @param best true when only the best alignment is extracted
     */
    @SuppressWarnings("unused")
    private static void extractInfoORF(ArrayList<String> oRFs, double evalueCutoff, String suffix, boolean smallORF,
            boolean best) {
        // final ArrayList<String> listBacteria =
        // TabDelimitedTableReader.readList(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
        final ArrayList<String> listBacteria = TabDelimitedTableReader.readList("D:/ListGenomes.txt");
        HashMap<String, String> listFastaFiles = new HashMap<String, String>();
        // if(!smallORF){
        // for(String genomeName : listBacteria){
        // File file = new File(GenomeNCBITools.PATH_NCBI_WIN+genomeName+"/"+genomeName+suffix+".faa");
        // String fasta = "";
        // try {
        // fasta = org.apache.commons.io.FileUtils.readFileToString(file);
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // System.out.println("Loaded "+genomeName);
        // listFastaFiles.put(genomeName, fasta);
        //
        // }
        // }

        String[] headers = {"ORF", "Strain", "hit_accession", "eValue", "identities", "ident", "bitScore", "hit_id",
                "blt.hit_def", "hit_seq", "ORF_seq"};
        String header = "";
        for (String headerTemp : headers)
            header += headerTemp + "\t";

        for (String orf : oRFs) {
            String orfName = orf.split(";")[0];
            ArrayList<String> finalResult = new ArrayList<>();
            finalResult.add(header);
            for (String genomeName : listBacteria) {
                /*
                 * Summarize in a tablefor every genome
                 */
                ArrayList<String> seqNames = new ArrayList<>();
                seqNames.add(orfName);
                Blast.extractInfo(seqNames, genomeName, suffix, evalueCutoff, true);

                /*
                 * Regroup all genome blast and get Gene sequence
                 */
                ArrayList<String> blastHit = TabDelimitedTableReader.readList(Database.getTEMP_PATH() + orfName
                        + "/Blast/resultBlast_" + orfName + "_" + genomeName + suffix + ".txt", true);

                String[][] orfList = new String[0][0];
                if (blastHit.size() > 1 && smallORF)
                    orfList = TabDelimitedTableReader.read(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName
                            + File.separator + genomeName + "_Info.excel");

                System.out.println(orfName + "  " + genomeName);

                int length = blastHit.size();
                if (best && length > 2)
                    length = 2;
                for (int i = 1; i < length; i++) {
                    String row = orfName + "\t" + genomeName + "\t" + blastHit.get(i);
                    String hitName = row.split("\t")[8];
                    if (smallORF) {
                        /*
                         * Find sequence of the sORF
                         */
                        String sequence = "";
                        if (!orfName.equals("")) {
                            System.out.println(genomeName + " " + orfName);
                            boolean found = false;
                            for (int k = 1; k < orfList.length && !found; k++) {
                                if (orfList[k][4].equals(hitName)) {
                                    sequence = orfList[k][0];
                                    System.out.println("Found ORF:" + hitName + " seq:" + sequence);
                                    row += "\t" + sequence;
                                    found = true;
                                }
                            }
                            if (!found)
                                row += "\t" + " ";
                        }
                        finalResult.add(row);

                    } else {
                        if (listFastaFiles.containsKey(genomeName)) {
                            boolean found = false;
                            for (String rowFasta : listFastaFiles.get(genomeName).split(">")) {
                                if (rowFasta.contains(hitName) && !found) {
                                    String sequence = rowFasta.substring(hitName.length()).trim().replaceAll("\n", "")
                                            .replaceAll("\r", "");
                                    System.out.println("seq:" + sequence);
                                    row += "\t" + sequence;
                                    found = true;
                                }
                            }
                        } else
                            row += "\t" + " ";
                        finalResult.add(row);
                    }
                }

                if (blastHit.size() == 1) {
                    String[] rows = {orfName, genomeName, "", "", "", "", "", "", "", "", ""};
                    String row = "";
                    for (String rowTemp : rows)
                        row += rowTemp + "\t";
                    finalResult.add(row);
                }
            }
            /*
             * Save table with all blast results
             */
            TabDelimitedTableReader.saveList(finalResult,
                    Database.getTEMP_PATH() + "/" + orfName + "/" + orfName + "_BlastResult.excel");
        }
        // TabDelimitedTableReader.saveList(finalResult,
        // Database.getTEMP_PATH()+/fileName+"_BlastResult.excel");
    }

    /**
     * Filtering table of results by e-value
     */
    public static void filterSmallORF(ArrayList<String> smallORFs, double eValueCutoff) {
        for (String smallORF : smallORFs) {
            String name = smallORF.split(";")[0];
            smallORF.split(";");
            String[][] summary =
                    TabDelimitedTableReader.read(Database.getTEMP_PATH() + "/" + name + "/Summary-" + name + ".excel");
            ArrayList<String> hitList = new ArrayList<String>();
            ArrayList<String> summaryLite = new ArrayList<String>();
            for (int i = 1; i < summary.length; i++) {
                if (!summary[i][1].equals("")) {
                    String genomeNCBI = summary[i][0];
                    double eValue = Double.parseDouble(summary[i][2]);
                    if (eValue < eValueCutoff) {
                        hitList.add(">" + genomeNCBI);
                        hitList.add(summary[i][6]);

                        String row = "";
                        for (int j = 0; j < summary[0].length; j++)
                            row += summary[i][j] + "\t";
                        row += genomeNCBI.split("_")[0] + "_" + genomeNCBI.split("_")[1];
                        summaryLite.add(row);
                    }
                }
            }
            TabDelimitedTableReader.saveList(hitList,
                    Database.getTEMP_PATH() + "/" + name + "/Summary-" + name + "-Sequences.fasta");
            TabDelimitedTableReader.saveList(summaryLite,
                    Database.getTEMP_PATH() + "/" + name + "/Summary-" + name + "-Lite.excel");
        }
    }

    /**
     * Summarize blast result in a table
     * 
     * @param smallORFs
     * @param evalueCutoff
     * @param bestHit
     */
    @SuppressWarnings("unused")
    private static void extractInfoSmallORF(ArrayList<String> smallORFs, double evalueCutoff, boolean bestHit) {
        final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
        // for(int i=1;i<11;i++){
        ArrayList<String> hitList = new ArrayList<String>();
        ArrayList<String> firstResult = new ArrayList<>();
        String[] headers = {"Strain", "Hit id", "evalue", "identities", "bitscore", "hit seq", "orfSequence"};
        String header = "";
        for (String headerTemp : headers)
            header += headerTemp + "\t";
        firstResult.add(header);
        for (String orf : smallORFs) {
            String name = orf.split(";")[0];
            orf.split(";");
            for (String genomeName : listBacteria) {
                ArrayList<String> seqNames = new ArrayList<>();
                seqNames.add(name);
                Blast.extractInfo(seqNames, genomeName, ".SmallORF", evalueCutoff, bestHit);
                String[] bestHitSmallORFs = extractMinimumInfo(name, genomeName, ".SmallORF");
                if (!bestHitSmallORFs[0].equals("")) {
                    /*
                     * Find sequence of the sORF
                     */
                    String[][] orfList = TabDelimitedTableReader.read(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName
                            + File.separator + genomeName + "_Info.excel");
                    String row = genomeName + "\t" + VectorUtils.toString(bestHitSmallORFs);
                    String orfName = bestHitSmallORFs[0];
                    String sequence = "";
                    if (!orfName.equals("")) {
                        System.out.println(genomeName + " " + orfName);
                        for (int k = 1; k < orfList.length; k++) {
                            if (orfList[k][4].equals(orfName)) {
                                sequence = orfList[k][0];
                                System.out.println("Found ORF:" + orfName + " seq:" + sequence);
                            }
                        }
                        // if(!hitList.contains(sequence)){
                        hitList.add(">" + genomeName + "|" + bestHitSmallORFs[0]);
                        hitList.add(sequence);
                        // }
                    }
                    firstResult.add(row + sequence);
                }

            }
            TabDelimitedTableReader.saveList(hitList,
                    Database.getTEMP_PATH() + "/" + name + "/Summary-" + name + "-Sequences.txt");
            TabDelimitedTableReader.saveList(firstResult,
                    Database.getTEMP_PATH() + "/" + name + "/Summary-" + name + ".excel");
        }
    }

    /**
     * Extract only the result with best evalue
     * 
     * @param seqNames
     * @return
     */
    private static String[] extractMinimumInfo(String seqNames, String genomeName, String suffix) {
        double evalueMin = 1000000;
        int indexMinEvalue = -1;
        String[][] bltResults = TabDelimitedTableReader.read(Database.getTEMP_PATH() + seqNames + "/Blast/resultBlast_"
                + seqNames + "_" + genomeName + suffix + ".txt");
        for (int k = 1; k < bltResults.length; k++) {
            double evalue = Double.parseDouble(bltResults[k][1]);
            if (evalue < evalueMin) {
                evalueMin = evalue;
                indexMinEvalue = k;
            }
        }
        if (indexMinEvalue != -1) {
            String[] rowTemp = ArrayUtils.getRow(bltResults, indexMinEvalue);
            String[] row = {rowTemp[6], rowTemp[1], rowTemp[2], rowTemp[4], rowTemp[7]};
            return row;
        } else {
            String[] result = {"", "", "", "", ""};
            return result;
        }
    }

    /**
     * <li>Create a fasta file with all proteins from the different chromosomes
     * <li>Using faa file create blast
     * <li>Verify by looking at all file size
     * 
     */
    public static void createDatabaseForORF() {
        createFastaAA();
        createBlastDB();
        verifyDatabase();
    }

    /**
     * <li>translate every genomes on the 6 frames
     * <li>Create a fasta file with all proteins from the different chromosomes
     * <li>Using faa file create blast
     * <li>Verify by looking at all file size
     * 
     */
    public static void createDatabaseForSmallORF() {
        /*
         * Translate all genomes in the 6 frames from a start codon to stop codon
         */
        try {
            int cutoffLength = 50;
            translateGenomes(cutoffLength);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
         * Create blastP database from the list of smallORFs
         */
        createFastaAASmallORF();
        createBlastSmallORFDB();
        verifyDatabase();
    }

    /**
     * Read all fasta file from each genome and create a list of every possible small peptides<br>
     * Extract from NTermDatabase.createTISDB()
     * 
     * @throws Exception
     */
    public static void translateGenomes(int cutoffLengthTemp) throws Exception {
        final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
        final int cutoffLength = cutoffLengthTemp;
        int j = 1;
        // for(int k=1;k<100;k++){ //13:53 - 14:02 =9 minutes
        ExecutorService executor = Executors.newFixedThreadPool(12);
        for (String genomeNameTemp : listBacteria) {
            final String genomeName = genomeNameTemp;
            File file =
                    new File(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator + genomeName + ".excel");
            if (file.length() < 1000) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println(genomeName);
                            GenomeNCBI genome = new GenomeNCBI(
                                    GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator, false);
                            ArrayList<String> peptides = new ArrayList<String>();
                            ArrayList<String> tisDB = new ArrayList<String>();
                            for (ChromosomeBacteriaSequence chromosome : genome.getChromosomes().values()) {
                                String sequence = chromosome.getSequenceAsString().toUpperCase();
                                ArrayList<Integer> startOccurences = new ArrayList<Integer>();
                                for (int i = 0; i < Codon.startCodon.length; i++) {
                                    startOccurences = FileUtils.searchPosition(Codon.startCodon[i][0], sequence);
                                    ProteinTools.getAllPeptides(sequence, startOccurences, peptides,
                                            Codon.startCodon[i][1].charAt(0), Codon.startCodon[i][0],
                                            chromosome.getLength(), true, cutoffLength);
                                }

                                String seqComplement =
                                        chromosome.getReverseComplement().getSequenceAsString().toUpperCase();
                                for (int i = 0; i < Codon.startCodon.length; i++) {
                                    startOccurences = FileUtils.searchPosition(Codon.startCodon[i][0], seqComplement);
                                    ProteinTools.getAllPeptides(seqComplement, startOccurences, peptides,
                                            Codon.startCodon[i][1].charAt(0), Codon.startCodon[i][0],
                                            chromosome.getLength(), false, cutoffLength);
                                }

                                TreeSet<String> finalPeptides = new TreeSet<String>();
                                for (String peptide : peptides) {
                                    finalPeptides.add(peptide);
                                }
                                System.out.println("Found " + finalPeptides.size() + chromosome.getAccession());

                                /*
                                 * Save in a file
                                 */
                                for (String peptide : finalPeptides) {
                                    tisDB.add(peptide + "\t" + chromosome.getAccession());
                                }
                            }
                            System.out.println(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator
                                    + genomeName + ".excel");
                            TabDelimitedTableReader.saveList(tisDB, GenomeNCBITools.PATH_NCBI_BacGenome + genomeName
                                    + File.separator + genomeName + ".excel");
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                };
                j++;
                executor.execute(runnable);
            }
        }
        System.err.println("Number of threads run: " + j + "  expected numb of data " + (listBacteria.size()));
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Interrupted exception");
        }
        System.err.println("All Genomes done");
    }

    /**
     * Extract result and create fasta files
     */
    public static void createFastaAASmallORF() {
        /*
         * Extract result and create fasta files
         */
        ExecutorService executor = Executors.newFixedThreadPool(20);
        final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);

        // for(int i=1;i<51;i++){ //18.26
        for (String genomeNameTemp : listBacteria) {
            final String genomeName = genomeNameTemp;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator
                                + genomeName + "_Info.excel");
                        File fileExcel = new File(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator
                                + genomeName + ".excel");
                        File fileFAA = new File(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator
                                + genomeName + ".SmallORF.faa");
                        if (file.length() < 1000 && fileExcel.exists() && fileFAA.length() < 1000) {
                            System.out.println(genomeName);
                            String[][] orfList = TabDelimitedTableReader.read(GenomeNCBITools.PATH_NCBI_BacGenome
                                    + genomeName + File.separator + genomeName + ".excel");
                            ArrayList<String> results = new ArrayList<>();
                            // results.add(genomeName);
                            ArrayList<String> fastaFile = new ArrayList<>();
                            for (int i = 0; i < orfList.length; i++) {
                                String sequence = orfList[i][0];
                                int length = sequence.length();
                                // System.out.println(genomeName+" "+orfList[i][0]+"\t"+orfList[i][1]);
                                int begin = Integer.parseInt(orfList[i][1]);
                                boolean strand = Boolean.parseBoolean(orfList[i][2]);
                                char strandChar = '+';
                                if (!strand)
                                    strandChar = '-';
                                int end = -1;
                                if (strand)
                                    end = begin + length * 3;
                                else
                                    end = begin - length * 3;
                                String name = "sORF_" + i;
                                String row = orfList[i][0] + "\t" + orfList[i][1] + "\t" + orfList[i][2] + "\t"
                                        + orfList[i][3] + "\t" + name + "|" + begin + "--" + end + "-(" + strandChar
                                        + ")";
                                results.add(row);
                                String rowFasta = ">" + name + "|" + begin + "--" + end + "-(" + strandChar + ")";
                                fastaFile.add(rowFasta.trim());
                                fastaFile.add(sequence.trim());
                            }
                            System.out.println("Save: " + GenomeNCBITools.PATH_NCBI_BacGenome + genomeName
                                    + File.separator + genomeName + ".SmallORF.faa");
                            TabDelimitedTableReader.saveList(fastaFile, GenomeNCBITools.PATH_NCBI_BacGenome + genomeName
                                    + File.separator + genomeName + ".SmallORF.faa");
                            System.out.println("Save: " + GenomeNCBITools.PATH_NCBI_BacGenome + genomeName
                                    + File.separator + genomeName + "_Info.excel");
                            TabDelimitedTableReader.saveList(results, GenomeNCBITools.PATH_NCBI_BacGenome + genomeName
                                    + File.separator + genomeName + "_Info.excel");
                        }
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

    /**
     * Create blastP databases
     */
    public static void createBlastSmallORFDB() {
        final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
        ArrayList<String> addGenome = new ArrayList<>();
        // for(int i=1;i<11;i++){
        for (String genomeName : listBacteria) {
            File file = new File(
                    GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator + genomeName + ".SmallORF.phr");
            File fileFaa = new File(
                    GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator + genomeName + ".SmallORF.faa");
            if (file.length() < 1000 && fileFaa.exists()) {
                addGenome.add(genomeName);
            }
        }

        String[] genomesInput = new String[addGenome.size()];
        for (int i = 0; i < addGenome.size(); i++) {
            // System.out.println(addGenome.get(i));
            genomesInput[i] = addGenome.get(i);
        }
        /*
         * Blast database creation will be performed in different threads
         */
        Blast.createBlastDatabases(GenomeNCBITools.PATH_NCBI_BacGenome, genomesInput, false, true);

    }

    /**
     * Look at each genome folder and look at the size of each data
     */
    public static void verifyDatabase() {
        System.out.println("Verify database");
        ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
        ArrayList<String> verifyDatabase = new ArrayList<>();
        verifyDatabase.add("Name\tFNA\tExcel\tfaa\tSmall faa\tphr Blast\tsmall phr Blast");
        for (String genomeName : listBacteria) {
            File genomeFolder = new File(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator);
            int lengthFNA = 0;
            for (File file : genomeFolder.listFiles()) {
                if (file.getAbsolutePath().endsWith(".fna")) {
                    lengthFNA += file.length();
                }
            }
            int lengthsmallORFBlast = 0;
            int lengthORFBlast = 0;
            for (File file : genomeFolder.listFiles()) {
                if (file.getAbsolutePath().endsWith(".SmallORF.phr")) {
                    lengthsmallORFBlast += file.length();
                }
                if (file.getAbsolutePath().endsWith(".ORF.phr")) {
                    lengthsmallORFBlast += file.length();
                }
            }
            File orfList =
                    new File(GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator + genomeName + ".excel");
            File smallOrffaa = new File(
                    GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator + genomeName + ".SmallORF.faa");
            File orffaa = new File(
                    GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator + genomeName + ".ORF.faa");
            verifyDatabase.add(genomeName + "\t" + lengthFNA + "\t" + orfList.length() + "\t" + orffaa.length() + "\t"
                    + smallOrffaa.length() + "\t" + lengthORFBlast + "\t" + lengthsmallORFBlast);

        }
        TabDelimitedTableReader.saveList(verifyDatabase, GenomeNCBITools.PATH_NCBI_BacGenome + "verifyDatabase.excel");
    }

    /**
     * Create blastP databases
     */
    public static void createFastaAA() {
        ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
        for (String genome : listBacteria) {
            File pathGenome = new File(GenomeNCBITools.PATH_NCBI_BacGenome + genome + "/");
            final String filterFinal = ".faa";
            File[] files = pathGenome.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(filterFinal) && !name.contains("ORF.faa"))
                        return true;
                    return false;
                }
            });

            if (files.length == 1)
                System.out.println(files[0].getAbsolutePath() + " to " + GenomeNCBITools.PATH_NCBI_BacGenome + genome
                        + "/" + genome + ".ORF.faa");
            /*
             * Concatenate files
             */

            ArrayList<Path> inputs = new ArrayList<Path>();
            for (File file : files) {
                inputs.add(Paths.get(file.getAbsolutePath()));
            }

            // Output file
            Path output = Paths.get(GenomeNCBITools.PATH_NCBI_BacGenome + genome + "/" + genome + ".ORF.faa");
            // Path output = Paths.get("D:/Temp/"+genome+".ORF.faa");

            // Charset for read and write
            Charset charset = StandardCharsets.UTF_8;

            // Join files (lines)
            for (Path path : inputs) {
                List<String> lines;
                try {
                    lines = Files.readAllLines(path, charset);
                    Files.write(output, lines, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Create blastP databases
     */
    public static void createBlastDB() {
        final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
        String[] genomesInput = new String[listBacteria.size()];
        for (int i = 0; i < listBacteria.size(); i++) {
            // System.out.println(addGenome.get(i));
            genomesInput[i] = listBacteria.get(i);
        }
        /*
         * Blast database creation will be performed in different threads
         */
        Blast.createBlastDatabases(GenomeNCBITools.PATH_NCBI_BacGenome, genomesInput, false, false);

    }

    /**
     * Remove specific files in genome folders
     */
    public static void removeFiles() {
        String[][] genomeArray = TabDelimitedTableReader.read(PATH_GENOMES_LIST);
        /*
         * Copy fna files to temp folder
         */
        for (int i = 1; i < genomeArray.length; i++) {
            String genomeName = genomeArray[i][1];
            String pathName = GenomeNCBITools.PATH_NCBI_BacGenome + genomeName + File.separator;
            File pathGenome = new File(pathName);

            for (File file : pathGenome.listFiles()) {
                if (file.getAbsolutePath().contains(genomeName)) {
                    file.delete();
                }
            }
        }
    }

}
