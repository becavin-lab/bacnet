package bacnet.scripts.listeriomics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.database.HomologCreation;
import bacnet.utils.ArrayUtils;

public class Peptidomics {

	public static void run() {
		/*
		 * Create fake protein fasta in Excel
		 * EGD_Listeria_peptides_190923.xslx
		 * to
		 * C:\Users\ipmc\OneDrive\Listeriomics\GenomeNCBI\Temp\BLASTDB\peptidomics\peptidomics.ORF.faa
		 */
		
		/*
		 * Exec blastDB creation
		 * "C:/Program Files/NCBI/blast-2.9.0+/bin/makeblastdb".exe -in "C:\\Users\\ipmc\\OneDrive\\Listeriomics\GenomeNCBI\\BLASTDB\peptidomics\peptidomics.ORF.faa" -parse_seqids -out "C:\\Users\\ipmc\\OneDrive\\Listeriomics\\GenomeNCBI\\BLASTDB\peptidomics\peptidomics.ORF" -dbtype prot -title peptidomics
 		*
 		*
		 */
		
		/*
		 * Modify function for blastscript creation to add peptidomics
		 * HomologCreation.createBlastScript <- remoive second line of blastp and parameter -max_target_seqs 1 
		 * Genome.getAvailableGenomes() <- add first line "peptidomics" to ArrayList of genome
		 */
		
		/*
		 * Run every Blast on the server
		 */
		// Done there : /pasteur/projets/policy01/BioIT/Chris_Listeriomics/
		//  sbatch --array=1-236 RunBlast.sh ListScriptPeptidomics.txt
		/*
		 * Combine results
		 */
		String path = "C:\\Users\\ipmc\\OneDrive\\Peptidomics\\";
		String results_folder = path + "Blast_result\\";
		
		/*
		 * Add identities
		 */
//		ArrayList<String> listGenomes = Genome.getAvailableGenomes();
//		for(String genome : listGenomes) {
//			HashMap<String,String> proteinIdtoLocusTagTarget = Genome.loadGeneFromProteinId(GenomeNCBI.unprocessGenomeName(genome));
//			genome = GenomeNCBI.processGenomeName(genome);
//			String path_fileblast = results_folder + "resultBlast_peptidomics_vs_" + genome + ".blast.txt";
//			System.out.println(path_fileblast);
//			String[][] genomeT_vs_genomeP = TabDelimitedTableReader.read(path_fileblast);
//			ArrayList<String> resultTable = new ArrayList<String>();
//			if(genomeT_vs_genomeP.length!=0) {
//				String[] columToAdd_T_VS_P = new String[genomeT_vs_genomeP.length];
//				ArrayList<Integer> indexRemove = new ArrayList<Integer>();
//				for (int i = 0; i < genomeT_vs_genomeP.length; i++) {
//					float identitiesT_vs_P = (Float.valueOf(genomeT_vs_genomeP[i][5]))
//							/ (Float.valueOf(genomeT_vs_genomeP[i][2]));
//					columToAdd_T_VS_P[i] = String.valueOf(identitiesT_vs_P);
//					String querySeq = genomeT_vs_genomeP[i][genomeT_vs_genomeP[i].length-2];
//					String hitSeq = genomeT_vs_genomeP[i][genomeT_vs_genomeP[i].length-1];
//					if(querySeq.equals(hitSeq) && identitiesT_vs_P == 1) {
//						String proteinId = genomeT_vs_genomeP[i][1].substring(4,genomeT_vs_genomeP[i][1].length()-1);
//						String geneName = proteinIdtoLocusTagTarget.get(proteinId);
//						String newRow = genomeT_vs_genomeP[i][0] + "\t" + proteinId + "\t" + geneName;
//						newRow += "\t" + genomeT_vs_genomeP[i][2] + "\t" + genomeT_vs_genomeP[i][3] + "\t"+querySeq+"\t"+hitSeq+"\t"+identitiesT_vs_P;
//						resultTable.add(newRow);
//						
//						
//					}
//				}
//				TabDelimitedTableReader.saveList(resultTable,results_folder + "peptidomics_vs_" + genome + ".blast.txt");
//			}			
//		}
		
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
