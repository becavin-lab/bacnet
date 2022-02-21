package bacnet.scripts.listeriomics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.compound.DNACompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.io.DNASequenceCreator;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.GenericFastaHeaderParser;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import org.biojava3.core.sequence.io.template.SequenceCreatorInterface;

import bacnet.Database;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.database.HomologCreation;
import bacnet.utils.FileUtils;

public class Peptidomics {

	/**
	 * BlastP the epitope sequences versus all Listeria genomes
	 * @throws IOException
	 */
	public static void runEpitopes() throws IOException {
		/*
		 * Create fake protein fasta in Excel
		 * UPDATE 15th february
		 * Suppl table S2 - Listeria epitopes 211130.xlsx
		 * to
		 * Peptidomics/peptidomics.ORF.faa
		 */
		
		/*
		 * Exec blastDB creation
		 *  "/opt/ncbi-blast-2.12.0+/bin/makeblastdb" -in "//Users//christophebecavin//Documents//Peptidomics//GenomeNCBI//Blastdb//peptidomics//peptidomics.ORF.faa" -parse_seqids -out "//Users//christophebecavin//Documents//Peptidomics//GenomeNCBI//Blastdb/peptidomics/peptidomics.ORF" -dbtype prot -title peptidomics
 		*
		 */
		
		/*
		 * Create all blastDB
		 * 
		 */
		//HomologCreation.createBlastDB("");
		
		/*
		 * Modify function for blastscript creation to add peptidomics
		 */
		//createBlastScript("","peptidomics");
		
		/*
		 * Run every Blast on MACOSX : cat ls peptidomics*.sh | sh
		 */


		/*
		 * Add identities
		 */
		//addIdentitesandFilter();
		
		/*
		 * Create final table
		 */
		createFinalTable();
	}
	
	
	/**
	 * BlastP the EDG antigene sequences versus all Listeria genomes
	 * @throws IOException
	 */
	public static void runAntigen() throws IOException {
		/*
		 * Create fasta file with protein antigens
		 */
		//createFastaAntigen();
		
		/*
		 * Exec blastDB creation
		 *  "/opt/ncbi-blast-2.12.0+/bin/makeblastdb" -in "//Users//christophebecavin//Documents//Peptidomics//GenomeNCBI//Blastdb//EGD_antigen//EGD_antigen.ORF.faa" -parse_seqids -out "//Users//christophebecavin//Documents//Peptidomics//GenomeNCBI//Blastdb/EGD_antigen/EGD_antigen.ORF" -dbtype prot -title EGD_antigen
 		*
		 */
		
		/*
		 * Create all blastDB
		 * 
		 */
		//HomologCreation.createBlastDB("");
		
		/*
		 * Modify function for blastscript creation to add peptidomics
		 */
		//createBlastScript("", "EGD_antigen");
		
		/*
		 * Run every Blast on MACOSX : cat ls EGD_antigen*.sh | sh
		 */

		/*
		 * Add identities
		 */
		//addIdentites();
		
		/**
		 * Create a conservation table for all EGD antigen
		 */
		//createAntigenResults();
		
		
		/*
		 * Add antigen conservation info to final table - Be careful that epitopes hit int the same protein_id than antigen full protein
		 * id WP_ peptidomics should be the same than id WP_ antigen
		 */
		combineEpiBlastTables();
		addToResultTable();
	}
	
	
	/*
	 * Add One column with average protein conservation in Result_peptides table
	 */
	public static void addToResultTable() {
		ArrayList<String> listResults = new ArrayList<String>();
		HashMap<String, String> geneToIdent = TabDelimitedTableReader.readHashMap(Database.getInstance().getPath() + "/EGD_antigen_Blast.txt");
		String[][] resultPeptide = TabDelimitedTableReader.read(Database.getInstance().getPath() + "/Result_peptides.txt");
		String header = "";
		for(int j=0;j<resultPeptide[0].length;j++) {
			header += resultPeptide[0][j] + "\t";
		}
		listResults.add(header+"Global_conservation");
		for(int i=0;i<resultPeptide.length;i++){
			String genename = resultPeptide[i][6];
			String newrow = "";
			for(int j=0;j<resultPeptide[i].length;j++) {
				newrow += resultPeptide[i][j] + "\t";
			}
			System.out.println(genename+" "+geneToIdent.get(genename));
			listResults.add(newrow+geneToIdent.get(genename));
		}
		TabDelimitedTableReader.saveList(listResults, Database.getInstance().getPath() + "/Result_peptides_final.txt");
	}
	
	/*
	 * Add antigen conservation info to final table - Be careful that epitopes hit int the same protein_id than antigen full protein
	 * id WP_ peptidomics should be the same than id WP_ antigen
	 */
	public static void combineEpiBlastTables() {
		// Read genomes and Epitopes
		ArrayList<String> genomes = Genome.getAvailableGenomes();
		String[][] peptidomics = TabDelimitedTableReader.read(Database.getInstance().getPath() + "/Suppl table S2 - Listeria epitopes 211130.txt");
		String pathEpiBlast = Database.getInstance().getPath() + "/EpiBlast/";
		String pathAntigen = Database.getInstance().getPath() + "/AntigenBlast/";
		for(int j=1;j < peptidomics.length; j++) {
			int nbGenome = 0;
			String peptideID = peptidomics[j][0];
			String genename = peptidomics[j][3];
			System.out.println(pathEpiBlast + peptideID + "_Blast_search.txt");
			String[][] epiblast = TabDelimitedTableReader.read(pathEpiBlast + peptideID + "_Blast_search.txt");
			ArrayList<String> resultsPeptide = new ArrayList<String>();
			resultsPeptide.add("Genome\tIdentity\tProtein\tqlen\tslen\tProtein_Cons\tprotLen\thitLen\tBlast_hit");
			for(int i=1;i<epiblast.length;i++) {
				String new_row = "";
				for(int w = 0; w<epiblast[0].length; w++) {
					new_row += epiblast[i][w] + "\t";
				}
				String genome = epiblast[i][0];
				String[][] pathblast = TabDelimitedTableReader.read(pathAntigen + "/"+genename+"_Conservation.txt");
				  for(int k=0; k<pathblast.length; k++) {
					if(pathblast[k][1].equals(genome)) {
						new_row += pathblast[k][3]+"\t"+ pathblast[k][4]+"\t"+pathblast[k][5]+"\t"+pathblast[k][2];
					}
				}
				resultsPeptide.add(new_row);
			}
			//TabDelimitedTableReader.saveList(resultsPeptide, pathEpiBlast + peptideID + "_Blast_search_modif.txt");			
			TabDelimitedTableReader.saveList(resultsPeptide, pathEpiBlast + peptideID + "_Blast_search.txt");			
		}
	}
	
	
	/**
	 * Read all results for LMON EGD gene conservation and summarize in tables
	 * String pathAntigen = Database.getInstance().getPath() + "/AntigenBlast/";
	 */
	public static void createAntigenResults(){
		String[][] peptidomics = TabDelimitedTableReader.read(Database.getInstance().getPath() + "/Suppl table S2 - Listeria epitopes 211130.txt");
		TreeSet<String> listGene = new TreeSet<>();
		for(int i=1; i<peptidomics.length; i++) {
			String protein = peptidomics[i][3];
			listGene.add(protein);
		}
		System.out.println("Antigen number "+listGene.size());
		String pathAntigen = Database.getInstance().getPath() + "/AntigenBlast/";
		HomologCreation.folderCreation(pathAntigen);
		ArrayList<String> listResults = new ArrayList<>();
		listResults.add("EGD_antigen\tGlobal Identity");
		for(String antigen : listGene) {
			System.out.println(antigen);
			ArrayList<String> listResultsEGD = new ArrayList<>();
			listResultsEGD.add("EGD_antigen\tGenome\tProtein_ID\tIdentity\tProtein_Len\tHit_Len");
			float identMean = 0;
			for(String genome : Genome.getAvailableGenomes()) {
				genome = GenomeNCBI.processGenomeName(genome);
				File file = new File(HomologCreation.PATH_RESULTS + "EGD_antigen_vs_" + genome + ".blast.txt");
				String[][] blastResult = TabDelimitedTableReader.read(file);
				boolean found = false;
				int k=1;
				while(!found) {
					String name = blastResult[k][0];
					if(name.equals(antigen)) {
						String proteinid = blastResult[k][1];
						String ident = blastResult[k][4];
						identMean = identMean + Float.valueOf(ident);
						String qlen = blastResult[k][2];
						String hlen = blastResult[k][3];
						listResultsEGD.add(antigen+"\t"+genome+"\t"+proteinid+"\t"+ident+"\t"+qlen+"\t"+hlen);
						found = true;
					}
					k++;
				}
			}
			TabDelimitedTableReader.saveList(listResultsEGD, pathAntigen + "/"+antigen+"_Conservation.txt");
			identMean = identMean / Genome.getAvailableGenomes().size();
			listResults.add(antigen + "\t" + identMean);
		}
		TabDelimitedTableReader.saveList(listResults, Database.getInstance().getPath() + "/EGD_antigen_Blast.txt");
	}

	
	/**
	 * Read epitopes list and EGD genome
	 * Extract antigen from the genome
	 * Save as a fasta to GenomeNCBI.PATH_BLASTDB + "EGD_antigen/EGD_antigen.ORF.faa"
	 */
	public static void createFastaAntigen() {
		/*
		 * Load EGD genomes, extract the protein and create fasta file
		 */
		Genome genome = Genome.loadGenome(Genome.EGDC_NAME);
		String[][] peptidomics = TabDelimitedTableReader.read(Database.getInstance().getPath() + "/Suppl table S2 - Listeria epitopes 211130.txt");
		TreeSet<String> listGene = new TreeSet<>();
		for(int i=1; i<peptidomics.length; i++) {
			String protein = peptidomics[i][3];
			listGene.add(protein);
		}
		/*
		 * Need to load GFF file and extract old locus tag versus new locus tag
		 * Example : locus_tag=LMON_RS14580   vs     old_locus_tag=LMON_2875
		 */
		String fileGFF = GenomeNCBI.PATH_GENOMES + Genome.EGDC_NAME + "/genomic_light.gff";
		LinkedHashMap<String, String> newTOoldlocus = new LinkedHashMap<String, String>();
		String[][] tableGFF = TabDelimitedTableReader.read(fileGFF);
		for(int i=0;i<tableGFF.length;i++) {
			if(tableGFF[i][2].contains("gene")) {
				System.out.println(tableGFF[i][8]);
				String locus = "";
				String oldlocustag = "";
				for(String feature : tableGFF[i][8].split(";")) {
					if(feature.startsWith("locus_tag")) {
						locus = feature.replace("locus_tag=", "");
						System.out.println(locus);
					}else if(feature.startsWith("old_locus_tag")) {
						oldlocustag = feature.replace("old_locus_tag=", "");
						System.out.println(oldlocustag);
					}
				}
				newTOoldlocus.put(oldlocustag, locus);

			}
		}
		
		ArrayList<String> faaFile = new ArrayList<>();
		for(String genename : listGene) {
			System.out.println("GetGene:"+genename);
			String genenameNew = newTOoldlocus.get(genename);
			System.out.println(genenameNew);
			Gene gene = genome.getGeneFromName(genenameNew);
			faaFile.add(">"+genename);
			faaFile.add(gene.getSequenceAA());
		}
		String pathAntigen = GenomeNCBI.PATH_BLASTDB + "EGD_antigen/";
		HomologCreation.folderCreation(pathAntigen);
		TabDelimitedTableReader.saveList(faaFile, pathAntigen + "EGD_antigen.ORF.faa");
	}
	
	/**
	 * Create final table: Count number of epitopes conserved per genomes
	 * 
	 * Create multiple tables for each epi: pathEpiBlast + peptideID + "_Blast_search.txt"
	 * Create one table for heatmap visu genome vs epitopes:  Database.getInstance().getPath() + "/Result_Blast.txt"
	 * Create one table with the number of genome per epitopes: Database.getInstance().getPath() + "/Result_peptides.txt"
	 * 
	 */
	public static void createFinalTable() {
		// Read genomes and Epitopes
		ArrayList<String> genomes = Genome.getAvailableGenomes();
		String[][] peptidomics = TabDelimitedTableReader.read(Database.getInstance().getPath() + "/Suppl table S2 - Listeria epitopes 211130.txt");
		// Prepare final tables
		ArrayList<String> listResults = new ArrayList<String>();
		String headers = "Peptide\tNbGenome in which it is found\tnbGenome for search\tnb of results found";
		for(int k=1;k<7;k++) {
			headers+="\t"+peptidomics[0][k];
		}
		listResults.add(headers);
		String[][] finalTable = new String[genomes.size()+1][peptidomics.length+1];
		
		String pathEpiBlast = Database.getInstance().getPath() + "/EpiBlast/";
		HomologCreation.folderCreation(pathEpiBlast);
		
		finalTable[0][0] = "Peptidomics";
		for(int j=1;j < peptidomics.length; j++) {
			int nbGenome = 0;
			String peptideID = peptidomics[j][0];
			finalTable[0][j+1] = peptideID;
			ArrayList<String> resultsPeptide = new ArrayList<String>();
			resultsPeptide.add("Genome\tIdentity\tProtein\tqlen\tslen");
			int nbLine = 0;			
			for(int i=0; i< genomes.size(); i++) {
				String genome = genomes.get(i);
				finalTable[i+1][0] = genome; 
				genome = GenomeNCBI.processGenomeName(genome);
				File file = new File(HomologCreation.PATH_RESULTS + "peptidomics_vs_" + genome + ".blast.txt");
				String resultGene = "";
				if(file.exists()) {
					String[][] blastResult = TabDelimitedTableReader.read(HomologCreation.PATH_RESULTS + "peptidomics_vs_" + genome + ".blast.txt");
					boolean found = false;
					for(int k=0; k < blastResult.length; k++) {
						if(peptideID.equals(blastResult[k][0])) {
							/*
							 * Test if perfect match
							 */
							double identity = Double.parseDouble(blastResult[k][blastResult[0].length-1]);
							String new_row = genome + "\t"+identity;
							for(int w = 1; w <(blastResult[0].length-1); w++) {
								new_row += "\t"+ blastResult[k][w];
							}
							resultsPeptide.add(new_row);
							found = true;
							nbLine++;
							resultGene += blastResult[k][1]+";";
							
						}
					}
					if(!found) {
						resultsPeptide.add(genome + "\t0\t0\t0\t0");
						nbLine++;
						finalTable[i+1][j+1] = "0";
					} else {
						nbGenome++;
						finalTable[i+1][j+1] = resultGene.split(";").length + "";
					}
				} else {
					resultsPeptide.add(genome + "\t0\t0\t0\t0");
					nbLine++;
				}				
			}
			
			TabDelimitedTableReader.saveList(resultsPeptide, pathEpiBlast + peptideID + "_Blast_search.txt");
			String resultLine = peptideID + "\t"+nbGenome+"\t"+(genomes.size())+"\t"+nbLine;
			for(int k=1;k<7;k++) {
				resultLine+="\t"+peptidomics[j][k];
			}
			listResults.add(resultLine);
		}
		TabDelimitedTableReader.save(finalTable, Database.getInstance().getPath() + "/Result_Blast.txt");
		TabDelimitedTableReader.saveList(listResults, Database.getInstance().getPath() + "/Result_peptides.txt");
	}

	
	
	/**
	 * Version for Epitopes !
	 * Add similiarities ratio in all tables : similarities = nident / qlen   (column 5 / column 2)
	 * Filter and keep only 100% match
	 * Save to : HomologCreation.PATH_RESULTS + "peptidomics_vs_" + genome + ".blast.txt"
	 */
	public static void addIdentitesandFilter() {
		int column_nident = 5;
		int column_qlen = 2;
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		for(String genome : listGenomes) {
			genome = GenomeNCBI.processGenomeName(genome);
			String path_fileblast = HomologCreation.PATH_RESULTS + "resultBlast_peptidomics_vs_" + genome + ".blast.txt";
			System.out.println(path_fileblast);
			String[][] genomeT_vs_genomeP = TabDelimitedTableReader.read(path_fileblast);
			ArrayList<String> resultTable = new ArrayList<String>();
			if(genomeT_vs_genomeP.length!=0) {
				for (int i = 0; i < genomeT_vs_genomeP.length; i++) {
					float identitiesT_vs_P = (Float.valueOf(genomeT_vs_genomeP[i][column_nident]))
							/ (Float.valueOf(genomeT_vs_genomeP[i][column_qlen]));
					if(identitiesT_vs_P == 1) {
						String proteinId = genomeT_vs_genomeP[i][1].substring(4,genomeT_vs_genomeP[i][1].length()-1);
						String newRow = genomeT_vs_genomeP[i][0] + "\t" + proteinId ;
						newRow += "\t" + genomeT_vs_genomeP[i][column_qlen] + "\t" + genomeT_vs_genomeP[i][column_nident] + "\t" + identitiesT_vs_P;
						resultTable.add(newRow);
					}
				}
				TabDelimitedTableReader.saveList(resultTable,HomologCreation.PATH_RESULTS + "peptidomics_vs_" + genome + ".blast.txt");
			}
		}
	}
	
	/**
	 * Version for Antigen !
	 * Add similiarities ratio in all tables : similarities = nident / qlen   (column 5 / column 2)
	 * Save to : HomologCreation.PATH_RESULTS + "EGD_antigen_vs_" + genome + ".blast.txt"
	 */
	public static void addIdentites() {
		int column_nident = 5;
		int column_qlen = 2;
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		for(String genome : listGenomes) {
			genome = GenomeNCBI.processGenomeName(genome);
			String path_fileblast = HomologCreation.PATH_RESULTS + "resultBlast_EGD_antigen_vs_" + genome + ".blast.txt";
			System.out.println(path_fileblast);
			String[][] genomeT_vs_genomeP = TabDelimitedTableReader.read(path_fileblast);
			ArrayList<String> resultTable = new ArrayList<String>();
			if(genomeT_vs_genomeP.length!=0) {
				for (int i = 0; i < genomeT_vs_genomeP.length; i++) {
					float identitiesT_vs_P = (Float.valueOf(genomeT_vs_genomeP[i][column_nident]))
							/ (Float.valueOf(genomeT_vs_genomeP[i][column_qlen]));
					String proteinId = genomeT_vs_genomeP[i][1].substring(4,genomeT_vs_genomeP[i][1].length()-1);
					String newRow = genomeT_vs_genomeP[i][0] + "\t" + proteinId ;
					newRow += "\t" + genomeT_vs_genomeP[i][column_qlen] + "\t" + genomeT_vs_genomeP[i][column_nident] + "\t" + identitiesT_vs_P;
					resultTable.add(newRow);
				}
				TabDelimitedTableReader.saveList(resultTable,HomologCreation.PATH_RESULTS + "EGD_antigen_vs_" + genome + ".blast.txt");
			}
		}
	}
	
	
	/**
	 * Creation of the general command file that will be used to create the ones for
	 * each blast
	 */
	public static String createBlastScript(String logs, String queryname) {
		/*
		 * Change DB directory if you want to run it on a cluster
		 */
		String blastDBFolder = HomologCreation.PATH_BLASTDB;
		String blastOutFolder = HomologCreation.PATH_RESULTS;

		/*
		 * Run bidirectionnal BlastP 
		 */
		ArrayList<String> blastFile = new ArrayList<>();
		blastFile.add("\"" + HomologCreation.blastP + "\"" + " -query " + blastDBFolder + "_fileGenomePivot -db " + blastDBFolder
				+ "_databaseTarget -out " + blastOutFolder
				+ "_blastP_VS_T -evalue 100 -max_target_seqs 5 -outfmt \"6 qseqid sseqid qlen slen length nident positive evalue bitscore\"");
				blastFile.add("echo _fileGenomePivot VS _fileGenomeTarget Blast search completed");
		// ">" + scriptFolder + "_fileGenomePivotVS_fileGenomeTarget.control.txt");

		TabDelimitedTableReader.saveList(blastFile, HomologCreation.BLAST_SCRIPT_TEMP);
		/*
		 * Create the blast commands
		 */
		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
		ArrayList<String> list_genomes_toBlast = HomologCreation.extractList(listGenomes, 0, listGenomes.size());
		String genome_pivot_path = HomologCreation.getFAAPath(queryname);
		createBlastCommands(queryname, ".ORF", genome_pivot_path, list_genomes_toBlast);
		System.out.println("Blast commands done for " + queryname);
		
		logs += "All blast script created in : " + GenomeNCBI.PATH_THREADS
				+ "\nRun them with bash or using a cluster (see bacnet.scripts.ext.scripts.RunBlastSGE.sh or"
				+ " RunBlastSlurm.sh)\nBut fix first value of: HomologCreation.PATH_SCRIPT -> " + HomologCreation.PATH_SCRIPT
				+ " which is the path for data on your server.\n"
				+ "You need also to fix the value of: HomologCreation.PATH_BLAST -> " + HomologCreation.PATH_BLAST
				+ " which is the path for blastp on your server.\n"
				+ "Run again script creation after fixing these path\n";
		System.out.println(logs);
		return logs;
	}

	
	/**
	 * Create .bat or .sh files with the command lines for each blast.
	 * 
	 * @param genomePivot
	 * @param proteinGenome
	 * @param suffix
	 * @param pathGenome
	 * @param listGenomes
	 * @param evalueCutoff
	 */
	public static void createBlastCommands(String genomePivot, String suffix, String pathGenome,
			ArrayList<String> listGenomes) {
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
						String databaseTarget = genomeName + HomologCreation.FILE_SEPARATOR + genomeName + suffixFinal;
						String databasePivot = genomePivot + HomologCreation.FILE_SEPARATOR + genomePivot + suffixFinal;
						String fileGenomePivot = genomePivot + HomologCreation.FILE_SEPARATOR + genomePivot + suffix + ".faa";
						String fileGenomeTarget = genomeName + HomologCreation.FILE_SEPARATOR + genomeName + suffix + ".faa";
						String blastP_VS_T = "resultBlast_" + genomePivot + "_vs_" + genomeName + ".blast.txt";
						String blastT_VS_P = "resultBlast_" + genomeName + "_vs_" + genomePivot	+ ".blast.txt";
						String args = FileUtils
								.readText(HomologCreation.BLAST_SCRIPT_TEMP);
						args = args.replaceAll("_fileGenomePivot", fileGenomePivot);
						args = args.replaceAll("_fileGenomeTarget", fileGenomeTarget);
						args = args.replaceAll("_blastP_VS_T", blastP_VS_T);
						args = args.replaceAll("_blastT_VS_P", blastT_VS_P);
						args = args.replaceAll("_databasePivot", databasePivot);
						args = args.replaceAll("_databaseTarget", databaseTarget);
						String extension = ".sh";
//						String os = System.getProperty("os.name");
//				        if (os.contains("Windows"))
//				        	extension = ".bat";
						FileUtils.saveText(args, GenomeNCBI.PATH_THREADS + genomePivot + "_vs_" + genomeName + extension);
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
	}
	
	
	@Deprecated
	public static void readFAAProteins(String genome) throws IOException {
		// Read protein.faa and get all proteins id vs length in mapProteins
		String proteinpath = HomologCreation.PATH_BLASTDB + genome + File.separator +  genome + ".ORF.faa";
		FileInputStream inStream = new FileInputStream( proteinpath );
		FastaReader<ProteinSequence,AminoAcidCompound> fastaReader = 
				new FastaReader<ProteinSequence,AminoAcidCompound>(inStream, 
						new GenericFastaHeaderParser<ProteinSequence,AminoAcidCompound>(), 
						new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
		LinkedHashMap<String, ProteinSequence> b = fastaReader.process();
		LinkedHashMap<String, Integer> mapProteins = new LinkedHashMap<>();
		for (  Entry<String, ProteinSequence> entry : b.entrySet() ) {
			String seqId = entry.getValue().getOriginalHeader().split(" ")[0];
			int sequence = entry.getValue().getSequenceAsString().length();
			mapProteins.put(seqId, sequence);
			System.out.println(seqId +" "+ sequence);
		}
	}
	
	@Deprecated
	public static void OrganizePhylogenetic(String path, String results_folder) {
		// Read list of genomes organized by phylogenomic
		ArrayList<String> listResults = new ArrayList<String>();
		listResults.add("Peptide\tNbGenome in which it is found\tnbGenome for search\tnb of results found");
		String[][] genomeTable = TabDelimitedTableReader.read(path + "Genomes.txt");
		String[][] peptidomics = TabDelimitedTableReader.read(path + "EGD_Listeria_peptides_190923.txt");
		ArrayList<String> peptidomicsList = TabDelimitedTableReader.readList(path + "Peptide_order_191022.txt");
		String[][] finalTable = new String[genomeTable.length+1][peptidomics.length+1];
		
		finalTable[0][0] = "Peptidomics";
		for(int j=0;j < peptidomicsList.size(); j++) {
			int nbGenome = 0;
			String peptide = peptidomicsList.get(j);
			String peptideID = "";
			for(int k=0;k<peptidomics.length;k++) {
				if(peptide.equals(peptidomics[k][2])) {
					peptideID = peptidomics[k][1];
				}
			}
			finalTable[0][j+1] = peptide;
			ArrayList<String> resultsPeptide = new ArrayList<String>();
			resultsPeptide.add("Genome\tIdentity\tProtein\tGeneName\tqlen\tslen\tqSeq\tsSeq");
			int nbLine = 0;			
			for(int i=1; i< genomeTable.length; i++) {
				String genome = genomeTable[i][1];
				finalTable[i][0] = genome; 
				genome = GenomeNCBI.processGenomeName(genome);
				File file = new File(results_folder + "peptidomics_vs_" + genome + ".blast.txt");
				String resultGene = "";
				if(file.exists()) {
					String[][] blastResult = TabDelimitedTableReader.read(results_folder + "peptidomics_vs_" + genome + ".blast.txt");
					boolean found = false;
					for(int k=0; k < blastResult.length; k++) {
						if(peptideID.equals(blastResult[k][0])) {
							/*
							 * Test if perfect match
							 */
							double identity = Double.parseDouble(blastResult[k][blastResult[0].length-1]);
							String new_row = genome + "\t"+identity;
							for(int w = 1; w <(blastResult[0].length-1); w++) {
								new_row += "\t"+ blastResult[k][w];
							}
							resultsPeptide.add(new_row);
							found = true;
							nbLine++;
							resultGene += blastResult[k][1]+";";
							
						}
					}
					if(!found) {
						resultsPeptide.add(genome + "\t0\t0\t0\t0\t0\t0\t0");
						nbLine++;
						finalTable[i][j+1] = "0";
					} else {
						nbGenome++;
						finalTable[i][j+1] = resultGene.split(";").length + "";
					}
				} else {
					resultsPeptide.add(genome + "\t0\t0\t0\t0\t0\t0\t0");
					nbLine++;
				}				
			}
			
			TabDelimitedTableReader.saveList(resultsPeptide, path + peptideID + "_Blast_search.txt");
			listResults.add(peptide + "\t"+nbGenome+"\t"+(genomeTable.length-1)+"\t"+nbLine);
		}
		TabDelimitedTableReader.save(finalTable, path + "Result_Blast.txt");
		TabDelimitedTableReader.saveList(listResults, path + "Result_peptides.txt");
	}
	
	
}
