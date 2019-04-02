package bacnet.scripts.database;

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
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import bacnet.Database;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.Blast;
import bacnet.scripts.blast.BlastOutput;
import bacnet.scripts.blast.BlastResult;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.utils.ArrayUtils;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class HomologCreation {
    public static String PATH_BLAST_WIN_GENERAL = "C:/Program Files/NCBI/blast-2.7.1+/";
    public static String PATH_BLAST_WIN = "C:/Program Files/NCBI/blast-2.7.1+/bin/";
    public static String PATH_BLAST_MAC = "/opt/ncbi-blast-2.7.1+/bin/";

    public static String tblastN = getBlastFolder() + "tblastn" + getBlastExtension() + " -task tblastn";
    public static String blastN = getBlastFolder() + "blastn" + getBlastExtension() + " -task blastn";
    public static String blastP = getBlastFolder() + "blastp" + getBlastExtension();
    public static String blastX = getBlastFolder() + "blastx" + getBlastExtension() + " -num_threads "
            + (Runtime.getRuntime().availableProcessors());
    public static String blastdbcmd = getBlastFolder() + "blastdbcmd" + getBlastExtension();
    public static String makeblastdb = getBlastFolder() + "makeblastdb" + getBlastExtension();
    public static String blastdb_aliastool = getBlastFolder() + "blastdb_aliastool" + getBlastExtension();
    public static String blast_formatter = getBlastFolder() + "blast_formatter" + getBlastExtension();

    public static String PATH_BLASTDB = Database.getInstance().getPath() + File.separator + "BLASTDB/";
    public static String PATH_GENOMES_LIST =
            Database.getInstance().getPath() + File.separator + "List_Genomes_BLAST.txt";

    public static String getBLAST_RESULT_PATH() {
        return Database.getTEMP_PATH() + "resultBlast";
    }

    /**
     * Run blast script for each genome against the other
     */
    public static void run() {
        folderCreation(PATH_BLASTDB);
        ArrayList<String> listGenomes = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
        
        /*
         * Create Blast database
         */
 //       createFAA();
 //       createBlastDB();
 //       verifyDatabase();
 //       System.err.println("Blast database created");
        
        /*
         * Create the blast.txt file needed later to create the file containing command lines
         */
//        createCommandFileGeneral();
       
        /*
         * Create the blast commands
         */
//        createBlastCommands(".ORF", 0.01);
        
        /*
         * Once the commands have been runned on the shell, launch the creation of the final table
         */
        ArrayList <String> listTable = TabDelimitedTableReader.readList(Database.getInstance().getPath() + "/ListTable.txt");
        for (int i = 10; i < listTable.size(); i++) {
            String genome_pivot = listGenomes.get(i);
            genome_pivot = genome_pivot.replaceAll(" ", "_");
            String genome_pivot_path = getFAAPath(genome_pivot);
            System.err.println(genome_pivot);
            if ((i + 1) < listGenomes.size()) {
                ArrayList<String> list_genomes_toBlast = extractList(listGenomes, i + 1, listGenomes.size());
                runBlast(genome_pivot, genome_pivot_path, 0.01, GenomeNCBI.PATH_RAW, list_genomes_toBlast, true);
            }
        }
    }
    
    /**
     * Create a FAA file in the BLASTDB folder for each genome. Will be use to create the db.
     */
    public static void createFAA() {
        ArrayList<String> listGenomes = Database.getInstance().getGenomeList();
        for (String genomeTemp : listGenomes) {
            File pathGenome = new File(GenomeNCBI.PATH_RAW + genomeTemp + File.separator);
            final String filterFinal = ".faa";
            File[] files = pathGenome.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.endsWith(filterFinal) && !name.contains("ORF.faa"))
                        return true;
                    return false;
                }
            });
            String genome = genomeTemp.replaceAll(" ", "_");
            if (files.length == 1)
                System.out.println(
                        files[0].getAbsolutePath() + " to " + PATH_BLASTDB + genome + "/" + genome + ".ORF.faa");


            // Output file
            Path output = Paths.get(PATH_BLASTDB + genome + "/" + genome + ".ORF.faa");
            folderCreation(PATH_BLASTDB + genome + "/");


            // Charset for read and write
            Charset charset = StandardCharsets.UTF_8;

            List<String> lines;
            try {
                lines = Files.readAllLines(Paths.get(files[0].getAbsolutePath()), charset);
                Files.write(output, lines, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Create blastP databases
     */
    public static void createBlastDB() {
        final ArrayList<String> listGenomes = Database.getInstance().getGenomeList();
        String[] genomesInput = new String[listGenomes.size()];
        for (int i = 0; i < listGenomes.size(); i++) {
            genomesInput[i] = listGenomes.get(i).replaceAll(" ", "_");
        }
        /*
         * Blast database creation will be performed in different threads
         */
        createBlastDatabases(PATH_BLASTDB, genomesInput);

    }

    /**
     * Create blast database for each genome contained in genomesInput
     * 
     * @param pathInput
     * @param genomesInput
     */
    public static void createBlastDatabases(String pathInput, String[] genomesInput) {
        // build the fusion of this databases
        final String[] genomes = genomesInput;
        final String pathFolder = pathInput;
        String dbType = "prot";
        final String dbtypeFinal = dbType;

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (String genomeTemp : genomes) {
            final String genome = genomeTemp;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        String path_faa = pathFolder + genome + File.separator + genome + ".ORF.faa";
                        String suffix = ".ORF";
                        final String out = PATH_BLASTDB + genome + File.separator + genome + suffix;
                        folderCreation(PATH_BLASTDB + genome + File.separator);
                        String execProcess = HomologCreation.makeblastdb + " -in \"" + path_faa
                                + "\" -parse_seqids -out \"" + out + "\" -dbtype " + dbtypeFinal + " -title " + genome;
                        System.out.println(execProcess);
                        CMD.runProcess(execProcess, true, PATH_BLASTDB);

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
     * Look at each genome folder and look at the size of each data
     */
    public static void verifyDatabase() {
        System.out.println("Verify database");
        ArrayList<String> listGenomes = Database.getInstance().getGenomeList();
        ArrayList<String> verifyDatabase = new ArrayList<>();
        verifyDatabase.add("Name\tFNA\tExcel\tfaa\tSmall faa\tphr Blast\tsmall phr Blast");
        for (String genomeTemp : listGenomes) {
            String genomeName = genomeTemp.replaceAll(" ", "_");
            File genomeFolder = new File(PATH_BLASTDB + genomeName + File.separator);
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
            File orfList = new File(PATH_BLASTDB + genomeName + File.separator + genomeName + ".excel");
            File smallOrffaa = new File(GenomeNCBI.class + genomeName + File.separator + genomeName + ".SmallORF.faa");
            File orffaa = new File(PATH_BLASTDB + genomeName + File.separator + genomeName + ".ORF.faa");
            verifyDatabase.add(genomeName + "\t" + lengthFNA + "\t" + orfList.length() + "\t" + orffaa.length() + "\t"
                    + smallOrffaa.length() + "\t" + lengthORFBlast + "\t" + lengthsmallORFBlast);

        }
        TabDelimitedTableReader.saveList(verifyDatabase, PATH_BLASTDB + "verifyDatabase.excel");
    }
    
    /**
     * Creation of the general command file that will be used to create the ones for each blast
     */
    public static void createCommandFileGeneral() {
      folderCreation(PATH_BLASTDB + "Threads/");
      ArrayList <String> blastFile = new ArrayList<>();
      blastFile.add("@blastP -query " + PATH_BLASTDB + "fileGenomePivot -db " + PATH_BLASTDB + "databaseTarget -out " + PATH_BLASTDB + "blastP_VS_T -evalue 0.01 -max_target_seqs 1 -outfmt '6 qseqid sseqid qlen slen length nident positive evalue bitscore'");
      blastFile.add("@blastP -query " + PATH_BLASTDB + "fileGenomeTarget -db " + PATH_BLASTDB + "databasePivot -out " + PATH_BLASTDB + "blastT_VS_P -evalue 0.01 -max_target_seqs 1 -outfmt '6 qseqid sseqid qlen slen length nident positive evalue bitscore'");
      blastFile.add("@echo Blast fileGenomePivot VS fileGenomeTarget done >" + PATH_BLASTDB + "RunThreads/BlastControl/fileGenomePivotVSfileGenomeTarget.control.txt");
      TabDelimitedTableReader.saveList(blastFile, PATH_BLASTDB + "Threads/Blast.txt");
    }

    /**
     * Launch blast commands creation 
     * @param suffix
     * @param evalueCutoff
     */
    public static void createBlastCommands(String suffix, double evalueCutoff) {
        folderCreation(PATH_BLASTDB + "Threads/Commands/");
        ArrayList<String> listGenomes = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
    	for (int i = 0; i < listGenomes.size(); i++) {
            String genome_pivot = listGenomes.get(i);
            genome_pivot = genome_pivot.replaceAll(" ", "_");
            String genome_pivot_path = getFAAPath(genome_pivot);
            System.err.println(genome_pivot);
            if ((i + 1) < listGenomes.size()) {
                ArrayList<String> list_genomes_toBlast = extractList(listGenomes, i + 1, listGenomes.size());
                createBlastCommands(genome_pivot, ".ORF", genome_pivot_path, list_genomes_toBlast, 0);
                System.err.println("Blast commands done for " + genome_pivot);
            }
        }
    }
    
      /**
     * Create .bat files with the command lines for each blast. 
     * 
     * @param genomePivot
     * @param proteinGenome
     * @param suffix
     * @param pathGenome
     * @param listGenomes
     * @param evalueCutoff
     */
    public static void createBlastCommands(String genomePivot, String suffix, String pathGenome,
            ArrayList<String> listGenomes, double evalueCutoff) {
        final ArrayList<String> listGenome = listGenomes;
        
        ExecutorService executor = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors());

        for (String genomeNameTemp : listGenome) {
            final String genomeName = genomeNameTemp;
            final String suffixFinal = suffix;

            
            if (!genomeName.equals(genomePivot)) {
                /*
                 * Create .bat file for each blast
                 */
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                            String databaseTarget = genomeName + "/" + genomeName + suffixFinal;
                            String databasePivot = genomePivot + "/" + genomePivot + suffixFinal;
                            String fileGenomePivot = genomePivot + "/" + genomePivot + suffix + ".faa";
                            String fileGenomeTarget = genomeName + "/" + genomeName + suffix + ".faa";
                            String blastP_VS_T  = genomePivot + "/resultBlast_" + genomeName + ".blast.txt";
                            String blastT_VS_P = genomeName + "/resultBlast_" + genomePivot + ".blast.txt";
                            String args = FileUtils.readText(PATH_BLASTDB + "RunThreads/Blast.txt");
                            args = args.replaceAll("fileGenomePivot", fileGenomePivot);
                            args = args.replaceAll("fileGenomeTarget", fileGenomeTarget);
                            args = args.replaceAll("blastP_VS_T", blastP_VS_T);
                            args = args.replaceAll("blastT_VS_P", blastT_VS_P);
                            args = args.replaceAll("databasePivot", databasePivot);
                            args = args.replaceAll("databaseTarget", databaseTarget);
                            FileUtils.saveText(args, PATH_BLASTDB + "RunThreads/Commands/" + genomePivot + "_" + genomeName + ".bat");
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
	     * Run blast for one genome, fasta file in fileName, against all other genomes in the list
	     * 
	     * @param fileName
	     * @param evalueCutoff
	     * @param pathGenome
	     * @param listGenomes
	     * @param best
	     */
    public static void runBlast(String genome_pivot, String fileName, double evalueCutoff, String pathGenome,
    		ArrayList<String> listGenomes, boolean best) {

    	/*
    	 * Add identity value 
    	 */
    	addColumnIdentity(genome_pivot, listGenomes);
    	System.err.println("Identity added to table for: " + genome_pivot);

    	/*
    	 * Create one summary table for the genome
    	 */
    	createSummaryTable(genome_pivot);
    	System.err.println("Summary table created for: " + genome_pivot);

    	/*
    	 * Add the phylogeny to the gene object
    	 */
//    	addPhylogenyToGenes(genome_pivot, listGenomes);
//    	System.err.println("Phylogeny added to gene of: " + genome_pivot);
    }

	/**
     * For each blast go through the table and add the column identity (compare ppos/qlen for both ways and take the highest)
     * 
     * @param genome_pivot
     * @param listGenomes
     */
    public static void addColumnIdentity(String genome_pivot, ArrayList <String> listGenomes) {
    	folderCreation(PATH_BLASTDB + genome_pivot + "/AddedColumnIdentity/"); 
    	for (String genome_target : listGenomes) {
    		folderCreation(PATH_BLASTDB + genome_target + "/AddedColumnIdentity/");
    		String[][] genomeP_vs_genomeT = TabDelimitedTableReader.read(PATH_BLASTDB + genome_pivot + "/resultBlast_" + genome_target + "_"  + ".blast.txt");
    		String[][] genomeT_vs_genomeP = TabDelimitedTableReader.read(PATH_BLASTDB + genome_target + "/resultBlast_" + genome_pivot + "_"  + ".blast.txt");
    		String[] columToAdd_P_VS_T = new String[genomeP_vs_genomeT.length];
    		String[] columToAdd_T_VS_P = new String[genomeT_vs_genomeP.length];
    		for (int i=0; i<genomeP_vs_genomeT.length; i++) {
    			String id_genomeT = genomeP_vs_genomeT[i][1].split("\\|")[1];
    			HashMap <String, Integer> indexRow_T_vs_P = indexRows(genomeT_vs_genomeP);
    			float identitiesP_vs_T = (Float.valueOf(genomeP_vs_genomeT[i][5]))/(Float.valueOf(genomeP_vs_genomeT[i][3]));
        		if (indexRow_T_vs_P.containsKey(id_genomeT)) {
        			int row = indexRow_T_vs_P.get(id_genomeT);
            		float identitiesT_vs_P = (Float.valueOf(genomeT_vs_genomeP[row][5]))/(Float.valueOf(genomeT_vs_genomeP[row][3]));
            		if (identitiesP_vs_T > identitiesT_vs_P) {
            			columToAdd_P_VS_T[i] = String.valueOf(identitiesP_vs_T);
            			columToAdd_T_VS_P[row] = String.valueOf(identitiesP_vs_T);
            		}else {
            			columToAdd_P_VS_T[i] = String.valueOf(identitiesT_vs_P);
            			columToAdd_T_VS_P[row] = String.valueOf(identitiesT_vs_P);
            		}
        		}else {
        			columToAdd_P_VS_T[i] = String.valueOf(identitiesP_vs_T);
        		}
    		}
    		for (int i=0; i<genomeT_vs_genomeP.length; i++) {
    			try {
    				if(columToAdd_T_VS_P[i].equals(""));
    			}catch (NullPointerException e) {
    				float identitiesT_vs_P = (Float.valueOf(genomeT_vs_genomeP[i][5]))/(Float.valueOf(genomeT_vs_genomeP[i][3]));
    				columToAdd_T_VS_P[i] = String.valueOf(identitiesT_vs_P);
    	        }
    		}
    		genomeP_vs_genomeT = ArrayUtils.addColumn(genomeP_vs_genomeT, columToAdd_P_VS_T);
    		genomeT_vs_genomeP = ArrayUtils.addColumn(genomeT_vs_genomeP, columToAdd_T_VS_P);
    		TabDelimitedTableReader.save(genomeP_vs_genomeT, PATH_BLASTDB + genome_pivot + "/AddedColumnIdentity/" + genome_target  + ".blast.txt");
    		TabDelimitedTableReader.save(genomeT_vs_genomeP, PATH_BLASTDB + genome_target + "/AddedColumnIdentity/" + genome_pivot  + ".blast.txt");
    		System.err.println("Column added: " + genome_pivot + " vs " + genome_target);
    	}
    }
    
    /**
     * Create a summary table for each genome with the list of protein and the list of homologies
     * 
     * @param genome_pivot
     */
    public static void createSummaryTable(String genome_pivot) {
    	folderCreation(PATH_BLASTDB + "Homologs/");
    	ArrayList<String> listGenomes = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
    	ArrayList <String> proteinList = getProteinList(genome_pivot);
    	String[][] newTable = new String[proteinList.size()][2]; 
    	for (String genome_target : listGenomes) {
    		if (!genome_target.equals(genome_pivot)) {
    			String[][] homologyTable = TabDelimitedTableReader.read(PATH_BLASTDB + genome_pivot + "/AddedColumnIdentity/" + genome_target + ".blast.txt");
    			HashMap <String, Integer> indexRowHashmap = indexRows(homologyTable);
    			int i = 0;
    			for (String proteinName : proteinList) {
    				newTable[i][0] = proteinName;
    				if (indexRowHashmap.containsKey(proteinName)) {
    					int indexRow = indexRowHashmap.get(proteinName);
    					newTable[i][1] += genome_target + ";" + homologyTable[indexRow][1].split("\\|")[1] + ";" + homologyTable[indexRow][homologyTable[indexRow].length-1] + ";-;;";
    				}
    				i ++;
    			}
    		}
    		System.out.println("Data for: " + genome_target + " added.");
    	}
    	for (int i=0; i<newTable.length; i++) {
    		try {
        		newTable[i][1] = newTable[i][1].substring(4); 
    		}catch (NullPointerException e) {
    		}
    	}
    	TabDelimitedTableReader.save(newTable, PATH_BLASTDB + "Homologs/" + genome_pivot + ".Allhomologs.txt");
    }
    
    /**
     * Create a hashmap between the value of the first column for each row and the index of the row
     * 
     * @param table
     * @return
     */
    public static HashMap <String, Integer> indexRows(String[][] table) {
    	HashMap <String, Integer> hashMap = new HashMap<>();
    	for (int i=0; i<table.length; i++) {
    		hashMap.put(table[i][0], i);
    	}
    	return hashMap;
    }
    
    /**
     * Return the list of all protein id present in the genome given
     * 
     * @param genome
     * @return
     */
    public static ArrayList <String> getProteinList (String genome) {
        ArrayList<String> protein_list = new ArrayList<String>();
        try {
            FileInputStream inStream = new FileInputStream(getFAAPath(genome));
            FastaReader<ProteinSequence, AminoAcidCompound> fastaReader =
                    new FastaReader<ProteinSequence, AminoAcidCompound>(inStream,
                            new NCBIFastaHeaderParser<ProteinSequence, AminoAcidCompound>(),
                            new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
            LinkedHashMap<String, ProteinSequence> genomeSequences = fastaReader.process();
            for (String key : genomeSequences.keySet()) {
                String seq_name = key.split(" ")[0];
                protein_list.add(seq_name);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return protein_list;
    }
    /**
     * Go through all result files and create Conservation HadhMap for each Gene
     */
    public static void addPhylogenyToGenes(String genome, ArrayList<String> genomeList) {
        for (String genomeName : genomeList) {
            Genome genomeLoaded = Genome.loadGenome(genome);
            String[][] blastArray =
                    TabDelimitedTableReader.read(PATH_BLASTDB + genome + File.separator + genome + "_AllBlasts.excel");
            for (int i = 0; i < blastArray.length; i++) {
                String locus = blastArray[i][0];
                String allInfo = blastArray[i][1];
                // System.out.println(locus);
                Gene gene = (Gene) genomeLoaded.getGeneFromName(locus);
                if (gene != null) {
                    // System.out.println(gene.getName());
                    String[] conservations = allInfo.split(";;");
                    LinkedHashMap<String, String> conservationHashMap = new LinkedHashMap<>();
                    for (String conservation : conservations) {
                        String genomeTarget = conservation.split(";")[0];
                        String geneTarget = conservation.split(";")[1] + ";" + conservation.split(";")[2] + ";";
                        if (conservation.split(";").length == 4) {
                            geneTarget += conservation.split(";")[3];
                        } else {
                            geneTarget += " ;";
                        }
                        // System.out.println(genomeTarget+" -----" + geneTarget);
                        conservationHashMap.put(genomeTarget, geneTarget);
                    }
                    gene.setConservationHashMap(conservationHashMap);
                    gene.setConservation(conservationHashMap.size());
                    gene.save(Database.getGENOMES_PATH() + genomeName + "/Sequences/" + gene.getName());

                }
            }
        }
    }

    /**
     * Extract a portion of an ArrayList into another ArrayList
     * 
     * @param list_init
     * @param index_start : index of the first data taken
     * @param index_end : index of the last data taken + 1
     * @return
     */
    public static ArrayList<String> extractList(ArrayList<String> list_init, int index_start, int index_end) {
        ArrayList<String> list_end = new ArrayList<>();
        int index = index_start;
        while (index < list_init.size() && index < index_end) {
            String genome = list_init.get(index).replaceAll(" ", "_");
            list_end.add(genome);
            index++;
        }
        return list_end;
    }

    public static String getFAAPath(String genome) {
        String path = new String();
        File folder = new File(PATH_BLASTDB + genome + "/");
        File[] list_file = folder.listFiles();
        for (File file : list_file) {
            if (file.getAbsolutePath().endsWith(".faa")) {
                return file.getAbsolutePath();
            }
        }
        System.out.println(".faa file not found for " + genome);
        return path;
    }

    public static String getBlastFolder() {
        String os = System.getProperty("os.arch");
        if (os.equals("amd64"))
            return PATH_BLAST_WIN;
        else
            return PATH_BLAST_MAC;
    }

    public static String getBlastExtension() {
        String os = System.getProperty("os.arch");
        if (os.equals("amd64"))
            return ".exe";
        else
            return "";
    }

    public static void folderCreation(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            System.out.println("Folder: " + path + " created");
        }
    }

    /**
     * For a specific type, return the extension of the corresponding file
     * 
     * @param type
     * @return
     */
    public static String fileExtension(BlastOutputTYPE type) {
        if (type == BlastOutputTYPE.XML) {
            return ".xml";
        } else if (type == BlastOutputTYPE.ASN || type == BlastOutputTYPE.ASN_Bin || type == BlastOutputTYPE.ASN_TxT) {
            return ".asn";
        } else
            return ".txt";
    }
}