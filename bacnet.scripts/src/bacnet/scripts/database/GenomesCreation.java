package bacnet.scripts.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import bacnet.Database;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeConversion;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.FastaFileReader;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.genome.CircularGenomeJPanel;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import ca.ualberta.stothard.cgview.CgviewIO;

/**
 * List of tools available for genome creation
 * 
 * @author cbecavin
 *
 */
public class GenomesCreation {
	
	/**
	 * Name fo the column in which RefSeq FTP url is found
	 */
	public static String COLNAME_REFSEQ = "RefSeq.FTP";

    /**
     * Download all genomes available in the database
     */
    public static void downloadAll() {
        ArrayList<String> listGenomes = new ArrayList<>();
        for (String genomeName : Genome.getAvailableGenomes()) {
            listGenomes.add(genomeName);
        }
        String logs = "";
        downloadGenomes(listGenomes, logs);
    }

    /**
     * Convert all genomes available in the database
     */
    public static void convertAll() {
        for (String genomeName : Genome.getAvailableGenomes()) {
            System.out.println("Convert: " + genomeName);
            GenomeNCBI genomeNCBI = GenomeNCBITools.loadGenome(genomeName, GenomeNCBI.PATH_GENOMES, false, true);
            GenomeConversion.run(genomeNCBI, GenomeNCBI.PATH_GENOMES + genomeName, genomeName);
        }
    }

    /**
     * Load all Genomes created, and save a table with the different information contains in the genomes
     */
    public static void verifyAll() {
        ArrayList<String> results = new ArrayList<>();
        for (String genomeName : Genome.getAvailableGenomes()) {
            Genome genome = Genome.loadGenome(genomeName, Database.getGENOMES_PATH(), false, true);
            for (String chromoID : genome.getChromosomes().keySet()) {
                Chromosome chromo = genome.getChromosomes().get(chromoID);
                System.out.println(genome.getSpecies() + "\t" + chromo.getAccession().toString() + "\t"
                        + chromo.getLength() + "\t genes: " + chromo.getGenes().size() + "\t ncRNA: "
                        + genome.getChromosomes().get(chromoID).getNcRNAs().size());
                results.add((genome.getSpecies() + "\t" + chromo.getAccession() + "\t"
                        + genome.getChromosomes().get(chromoID).getDescription() + "\t"
                        + genome.getChromosomes().get(chromoID).getLength() + "\t"
                        + genome.getChromosomes().get(chromoID).getGenes().size() + "\t"
                        + genome.getChromosomes().get(chromoID).getNcRNAs().size()));
            }
        }
        System.out.println("Save Table: " + Database.getInstance().getPath() + "/VerifyGenomeConversion.txt");
        TabDelimitedTableReader.saveList(results, Database.getInstance().getPath() + "/VerifyGenomeConversion.txt");
    }

    /**
     * Download new genomes taken from a text file and open all gzip files
     */
    public static String downloadGenomes(ArrayList<String> listGenomes, String logs) {
    	System.out.println("Download Genomes");
        String[][] newGenomes = TabDelimitedTableReader.read(Database.getInstance().getGenomeArrayPath());
        for (int i = 1; i < newGenomes.length; i++) {
            String strain = newGenomes[i][ArrayUtils.findColumn(newGenomes, "Name")];
            if (listGenomes.contains(strain)) {
                String refSeqFTP = newGenomes[i][ArrayUtils.findColumn(newGenomes, GenomesCreation.COLNAME_REFSEQ)];
                String ftp = refSeqFTP;
                System.out.println("Ref:" + refSeqFTP);
                if (refSeqFTP.equals("")) {
                	int index = ArrayUtils.findColumn(newGenomes, GenomesCreation.COLNAME_REFSEQ);
                    if (index == -1) {
                        logs += "No \"RefSeq FTP\" and \"GenBank FTP\" columns available in " + Database.getInstance().getGenomeArrayPath() + "\n";
                        logs += "Impossible to download the genomes";
                    } else {
                    	ftp = newGenomes[i][ArrayUtils.findColumn(newGenomes, GenomesCreation.COLNAME_REFSEQ)];
                    }
                }
                System.out.println(ftp);

                /*
                 * Create folder
                 */
                String path = GenomeNCBI.PATH_GENOMES + strain + "/";
                File file = new File(path);
                if (file.exists()) {
                    file.mkdir();
                }
                System.out.println(path);

                /*
                 * create list of file to download
                 */
                String folderFTP = FileUtils.removePath(ftp);
                System.out.println(folderFTP);
                ArrayList<String> downloadFile = new ArrayList<>();
                downloadFile.add("md5checksums.txt");
                downloadFile.add(folderFTP + "_assembly_report.txt");
                downloadFile.add(folderFTP + "_assembly_stats.txt");
                downloadFile.add(folderFTP + "_genomic.fna.gz");
                downloadFile.add(folderFTP + "_genomic.gff.gz");
                downloadFile.add(folderFTP + "_protein.faa.gz");

                /*
                 * Donwload file
                 */
                for (String fileName : downloadFile) {
                    String filePath = path + fileName;
                    try {
                        URL idfURL = new URL(ftp + "/" + fileName);
                        System.out.println("Download: " + idfURL + " to " + filePath);
                        org.apache.commons.io.FileUtils.copyURLToFile(idfURL, new File(filePath));
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                /*
                 * MD5 Checksum
                 */
                String[][] md5sums = TabDelimitedTableReader.read(new File(path + "md5checksums.txt"), " ");
                HashMap<String, String> fileToMD5 = new HashMap<>();
                for (int j = 0; j < md5sums.length; j++) {
                    fileToMD5.put(md5sums[j][2], md5sums[j][0]);
                }
                System.out.println();
                try {
                    FileInputStream fis = new FileInputStream(new File(path + folderFTP + "_genomic.fna.gz"));
                    String realmd5fna = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                    fis.close();
                    FileInputStream fis2 = new FileInputStream(new File(path + folderFTP + "_genomic.gff.gz"));
                    String realmd5gff = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis2);
                    fis2.close();
                    FileInputStream fis3 = new FileInputStream(new File(path + folderFTP + "_protein.faa.gz"));
                    String realmd5faa = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                    fis3.close();
                    String namefna = "./" + folderFTP + "_genomic.fna.gz";
                    String md5fna = fileToMD5.get(namefna);
                    String namegff = "./" + folderFTP + "_genomic.gff.gz";
                    String md5gff = fileToMD5.get(namegff);
                    String namefaa = "./" + folderFTP + "_protein.faa.gz";
                    String md5faa = fileToMD5.get(namefaa);
                    System.out.println(path);
                    System.out.println(realmd5fna + " " + md5fna + " " + namefna);
                    System.out.println(realmd5gff + " " + md5gff + " " + namegff);
                    System.out.println(realmd5faa + " " + md5faa + " " + namefaa);
                    if (!realmd5fna.equals(md5fna)) {
                        System.err.println("Not equal:" + namefna);
                    }
                    if (!realmd5gff.equals(md5gff)) {
                        System.err.println("Not equal:" + namegff);
                    }
                    if (!realmd5faa.equals(md5faa)) {
                        System.err.println("Not equal:" + namefaa);
                    }

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                /*
                 * Unzip files
                 */

                String namefnagz = path + folderFTP + "_genomic.fna.gz";
                String newfna = path + folderFTP + ".fna";
                String namegffgz = path + folderFTP + "_genomic.gff.gz";
                String newgff = path + folderFTP + ".gff";
                String namefaagz = path + folderFTP + "_protein.faa.gz";
                String newfaa = path + folderFTP + ".faa";
                try {
                    FileUtils.extractGZIP(namefnagz, newfna);
                    FileUtils.extractGZIP(namegffgz, newgff);
                    FileUtils.extractGZIP(namefaagz, newfaa);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                
                try {
					org.apache.commons.io.FileUtils.forceDelete(new File(namefnagz));
					org.apache.commons.io.FileUtils.forceDelete(new File(namegffgz));
	                org.apache.commons.io.FileUtils.forceDelete(new File(namefaagz));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        return logs;

    }

    /**
     * Create Table 1 with the list of Genomes and different infos<br>
     * Save in: Project.getPath()+"Table 1 - Genomes.txt"
     */
    public static String createGenomeTable(String logs) {
        ArrayList<String> table = new ArrayList<>();
        String[] headers = {"Strain", "Length", "Nb Chromosomes", "CDS",
                "rRNA and tRNA"};
        String header = "";
        for (String temp : headers)
            header += temp + "\t";
        table.add(header);

        ArrayList<String> genomesList = Genome.getAvailableGenomes();
        for (String genomeName : genomesList) {
            Genome genome = Genome.loadGenome(genomeName);
            String strain = genome.getSpecies();
            
            String length = genome.getLengthGenome() + "";
            String nbChromo = genome.getChromosomes().size()+"";
            String CDS = genome.getGenes().size()+"";
            String rRNAtRNA = genome.getNcRNAs().size()+"";
            String[] rows = {strain, length, nbChromo, CDS, rRNAtRNA};
            String row = "";
            for (String temp : rows)
                row += temp + "\t";
            table.add(row);
            
        }
        
        TabDelimitedTableReader.saveList(table, Database.getInstance().getPath() + "/Genomes_summary.txt");
        logs += "Genome summary table saved in : "+ Database.getInstance().getPath() + "/Genomes_summary.txt\n";
        logs += "Modify your Genomes.txt file using this summary table\n";
        return logs;
    }

    /**
     * Some genomes have been downloaded without PTT files so we create them<br>
     * PTT files are used by SynTVIew software
     */
    public static void createPTTandFAAFiles() {
        for (String genomeName : Genome.getAvailableGenomes()) {
            Genome genome = Genome.loadGenome(genomeName);
            for (Chromosome chromo : genome.getChromosomes().values()) {
                System.out.println(chromo.getAccession().toString());
                String fileName = GenomeNCBI.PATH_GENOMES + File.separator + genomeName + File.separator
                        + chromo.getAccession().toString() + ".ptt";
                System.out.println(fileName);
                ArrayList<Gene> genes = new ArrayList<>();
                for (Gene gene : chromo.getGenes().values())
                    genes.add(gene);
                // PTTReader.saveProteins(genes, fileName, chromo);
                FastaFileReader.saveProteins(genes, Database.getGENOMES_PATH() + genomeName + File.separator
                        + chromo.getAccession().toString() + ".faa");
            }
        }
    }

    public static void showAllLocusID() {
        ArrayList<String> locusIDs = new ArrayList<>();
        for (String genomeName : Genome.getAvailableGenomes()) {
            Genome genome = Genome.loadGenome(genomeName);
            for (Chromosome chromo : genome.getChromosomes().values()) {
                String result = genomeName + "\t" + chromo.getGenes().values().iterator().next().getName();
                System.out.println(result);
                locusIDs.add(result);
            }
        }
        TabDelimitedTableReader.saveList(locusIDs,
                Database.getInstance().getPath() + "LocusIDFromGgenomeAvailables.excel");
    }

    /**
     * Concatenate all genomes together and create a huge fasta file. Submit it to:
     * http://bigsdb.pasteur.fr/perl/bigsdb/bigsdb.pl?db=pubmlst_listeria_seqdef_public&page=batchSequenceQuery
     */
    public static void searchForSTandCC() {
        ArrayList<String> fastaFile = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 10; k++) {
                int index = i * 10 + k;
                if (index < Genome.getAvailableGenomes().size()) {
                    String genomeName = Genome.getAvailableGenomes().get(index);
                    fastaFile.add(">" + genomeName.replaceAll(" ", "_"));
                    Genome genome = Genome.loadGenome(genomeName);
                    String seq = "";
                    for (Chromosome chromo : genome.getChromosomes().values()) {
                        seq += chromo.getSequenceAsString();
                    }
                    fastaFile.add(seq);
                }
            }
            TabDelimitedTableReader.saveList(fastaFile, Database.getInstance().getPath() + "Allgenome" + i + ".fasta");
            fastaFile.clear();
        }
    }

    /**
     * Create a list of all Genes, all ASrnas, and all CisRegs of EGD-e<br>
     * Srnas list is already created in EGDe_SRNA_PATH
     */
    public static void createEGDeGenomeElementList() {
        ArrayList<String> genes = new ArrayList<>();
        for (String gene : Genome.loadEgdeGenome().getChromosomes().get(Genome.EGDE_CHROMO_NAME).getGenes().keySet()) {
            genes.add(gene);
        }
        TabDelimitedTableReader.saveList(genes,
                Database.getANNOTATIONDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDE_GENE"));

        ArrayList<String> asRNAs = new ArrayList<>();
        for (String asRNA : Genome.loadEgdeGenome().getChromosomes().get(Genome.EGDE_CHROMO_NAME).getAsRNAs()
                .keySet()) {
            asRNAs.add(asRNA);
        }
        TabDelimitedTableReader.saveList(asRNAs,
                Database.getANNOTATIONDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDe_ASRNA"));

        ArrayList<String> cisRegs = new ArrayList<>();
        for (String cisReg : Genome.loadEgdeGenome().getChromosomes().get(Genome.EGDE_CHROMO_NAME).getCisRegs()
                .keySet()) {
            cisRegs.add(cisReg);
        }
        TabDelimitedTableReader.saveList(cisRegs,
                Database.getANNOTATIONDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDE_CISREG"));
    }
    
    /**
     * Display a circular genome view from a specific genome
     * @param genome
     * @param title
     */
    @Deprecated
    public static void createCircularGenomeView(Genome genome, String title) {
    	 // create Circular genome images
         CircularGenomeJPanel panel = new CircularGenomeJPanel(1000, 1000, genome, title);
         try {
        	 CgviewIO.writeToPNGFile(panel.getCgview(), "c:/circularView.png");
         } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         }
    }
    
    
}
