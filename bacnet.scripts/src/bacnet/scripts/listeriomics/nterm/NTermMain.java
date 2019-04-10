package bacnet.scripts.listeriomics.nterm;

import java.util.ArrayList;
import java.util.HashMap;

import bacnet.Database;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.proteomics.NTermUtils;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Srna;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.phylogeny.PhylogenySmallORFs;
import bacnet.utils.ArrayUtils;

/**
 * General method for running all methods related to NTerm project
 * 
 * @author UIBC
 *
 */
public class NTermMain {

	/**
	 * General method for running all methods related to NTerm project
	 */
	public static void run() {
		/*
		 * Creation of Database for Proteomics software
		 */
//		NTermDatabase.createDataBases();
//		NTermDatabase.statDatabase();

		/*
		 * Map data and summarize a first time
		 */
//		String nameRawData = NTermCreateData.PATH_RAW_DATA+"NTerm_Combine_07032014.txt";
//		String nameRawData = NTermCreateData.PATH_RAW_DATA+"NTerm_Blood_TIS_Final_06032015.txt";
		String nameMassSpecData = "EGDe_Combine_TIS";
//		String nameMassSpecData = "Blood_TIS_Final";
//		String maxQuant = NTermCreateData.PATH_RAW_DATA+"MQ_NTerm_Combine_07032014";
//		String maxQuant = NTermCreateData.PATH_RAW_DATA+"MQ_NTerm_Blood_TIS_Final_06032015";
//		System.out.println(nameRawData);
//		NTermCreateData.run(nameRawData,nameMassSpecData);	
//		NTermFilter filter = new NTermFilter(NTermData.load(nameMassSpecData),NTermFilter.DEFAULT_FILTER_PATH);
//		filter.applyFilter();
//		NTermData massSpecData = NTermData.load(nameMassSpecData);
//		massSpecData.createGeneHashMap();
//		NTermCreateData.addMaXQuantInfo(massSpecData, maxQuant,true);
//		NTermSummaryTable.getSummaryTables(massSpecData, NTermCreateData.PATH_RESULTS);

		/*
		 * Create final data for publication
		 */
		String nameMassSpecDataFinal = "EGDe_37C_TIS";
//		String nameMassSpecDataFinal = nameMassSpecData; // for blood data
//		NTermCreateFinalData.run(nameMassSpecData,nameMassSpecDataFinal);

		/*
		 * Create final table of the NTerm to display in NTerminomics
		 * 
		 */
		createListeriomicsTable(nameMassSpecDataFinal);

		/*
		 * Some methods to do statistics of the results
		 */
//		NTermStatProteolyse.run(nameMassSpecData);
//		NTermUtils.countRTerminal(nameMassSpecData);
//		NTermUtils.compareMassSpecData(nameMassSpecData, nameMassSpecData2);
//		NTermStat.peptideLogo();
//		findLeaderLess();

		/*
		 * Some methods to do Statistics on the Translation Initiation sites
		 */
//		NTermStat stat = new NTermStat();
//		stat.tableGeneEGDe();
		// stat.display();
//		stat.statTranscriptomes();

//		addTotalSpectraCount();

		/*
		 * Some methods for small ORFs finding
		 */
		PhylogenySmallORFs.run();
//		PhylogenyRli42vsRsbR.run();
//		System.out.println(">WholeRegion");
//		System.out.println(Genome.loadEgdeGenome().getFirstChromosome().getSequenceAsString(1399327, 		1399616, Strand.POSITIVE));
//		System.out.println(">sbrA-50bp");
//		System.out.println(Genome.loadEgdeGenome().getFirstChromosome().getSequenceAsString(1399327, 		1399616, Strand.POSITIVE));
//		System.out.println(">rli42-50bp");
//		System.out.println(Genome.loadEgdeGenome().getFirstChromosome().getSequenceAsString(1399461, 		1399666, Strand.NEGATIVE));
//		System.out.println(">prfA");
//		System.out.println(Genome.loadEgdeGenome().getFirstChromosome().getSequenceAsString(204353, 204624, Strand.NEGATIVE));

		/*
		 * Look at transcirptomic datasets for rli42 and RsbR regulation
		 */
		// NTermTranscriptome.getTranscriptomes();
		// NTermTranscriptome.getRegulatoryRegion();

	}

	public static void createListeriomicsTable(String fileName) {
		NTermData ntermData = NTermData.load(fileName);
		Chromosome chromo = Genome.loadEgdeGenome().getFirstChromosome();
		String[] headers = { "PeptideID", "Peptide Type", "Type Modif.", "Peptide Sequence", "Peptide Begin",
				"Peptide End", "Strand", "Nb. Spectra", "Threshold", "Score", "Protein Overlap", "DistanceOverlap",
				"Size Protein (aa)", "Protein Begin", "Protein End", "Sequence Protein" };
		String header = "";
		for (String temp : headers) {
			header += temp + "\t";
		}
		ArrayList<String> finalList = new ArrayList<>();
		finalList.add(header);
		for (String nTermID : ntermData.getElements().keySet()) {
			NTerm nTerm = ntermData.getElements().get(nTermID);
			if (!nTerm.getTypeOverlap().equals("uncategorized")) {
				String row = nTermID + "\t" + nTerm.getTypeOverlap() + "\t" + nTerm.getTypeModif() + "\t"
						+ nTerm.getSequencePeptide() + "\t" + nTerm.getBegin() + "\t" + nTerm.getEnd() + "\t"
						+ nTerm.getStrand();
				row += "\t" + nTerm.getSpectra() + "\t" + nTerm.getThreshold() + "\t" + nTerm.getScore();
				String geneInfo = "";
				String[] overlaps = nTerm.getOverlap().split(" : ");
				if (overlaps.length > 1) {
					String geneOverlap = overlaps[0];
					if (geneOverlap.contains("rli")) {
						Srna sRNA = chromo.getsRNAs().get(geneOverlap);
						geneInfo = sRNA.getName() + "\t" + overlaps[1] + "\t" + sRNA.getSequenceAA().length() + "\t"
								+ sRNA.getBegin() + "\t" + sRNA.getEnd() + "\t" + sRNA.getSequenceAA();
					} else if (geneOverlap.contains("lmo")) {
						Gene gene = chromo.getGenes().get(geneOverlap);
						System.out.println(geneOverlap + " - ");
						geneInfo = gene.getName() + "\t" + overlaps[1] + "\t" + gene.getSequenceAA().length() + "\t"
								+ gene.getBegin() + "\t" + gene.getEnd() + "\t" + gene.getSequenceAA();
					}
					System.out.println(geneOverlap);
				}
				// geneInfo = nTerm.get+"\t"+nTerm.get
				row += "\t" + geneInfo;

				finalList.add(row);
			}
		}
		TabDelimitedTableReader.saveList(finalList, Database.getPROTEOMES_PATH() + "NTerm.txt");
		System.out.println(finalList.size());
	}

	public static void addTotalSpectraCount() {
		String[][] array = TabDelimitedTableReader.read("D:/christophe_mapped_serum.txt");

		for (int i = 1; i < array.length; i++) {
			int spectra = 0;
			String acD3 = array[i][18];
			String ace = array[i][19];
			String formodif = array[i][20];

			if (!acD3.equals("")) {
				int addThis = Integer.parseInt(acD3.split(";")[1]);
				spectra += addThis;
			}
			if (!ace.equals("")) {
				int addThis = Integer.parseInt(ace.split(";")[1]);
				spectra += addThis;
			}
			if (!formodif.equals("")) {
				int addThis = Integer.parseInt(formodif.split(";")[1]);
				spectra += addThis;
			}
			array[i][3] = spectra + "";
		}
		// TabDelimitedTableReader.save(array, "D:/christophe_mapped_serum_modif.txt");

		HashMap<String, Integer> lmoToSpectra = new HashMap<String, Integer>();
		for (int i = 1; i < array.length; i++) {
			String lmo = array[i][4];
			int spectra = Integer.parseInt(array[i][3]);
			if (lmoToSpectra.containsKey(lmo)) {
				int total = lmoToSpectra.get(lmo);
				total += spectra;
				lmoToSpectra.put(lmo, total);
			} else {
				lmoToSpectra.put(lmo, spectra);
			}
		}

		for (int i = 1; i < array.length; i++) {
			String lmo = array[i][4];
			int spectra = lmoToSpectra.get(lmo);
			array[i][2] = spectra + "";
		}
		TabDelimitedTableReader.save(array, "D:/christophe_mapped_serum_modif.txt");

	}

	/**
	 * Add RAST info to final supplementary tables
	 */
	public static void addRASTInfo() {
		Genome genome = Genome.loadEgdeGenome();
		String[][] array = TabDelimitedTableReader.read("D:/suppl_table_3.txt");
		for (int i = 1; i < array.length; i++) {
			String lmo = array[i][ArrayUtils.findColumn(array, "lmo")];
			Gene gene = genome.getGeneFromName(lmo);
			System.out.println(gene.getFeature("RAST_Locus"));
			System.out.println(gene.getFeature("RAST_Overlap"));
			String RAST = gene.getFeature("RAST_Locus");
			String info = "";
			if (!RAST.equals("")) {
				if (!gene.getFeature("RAST_Overlap").equals("")) {
					Gene geneRAST = genome.getGeneFromName(RAST);
					if (geneRAST.isStrand()) {
						int startRAST = geneRAST.getBegin();
						int startNTerm = Integer.parseInt(array[i][ArrayUtils.findColumn(array, "Nterm start")]);
						if (startNTerm == startRAST) {
							info = "Predicted:" + RAST + ":" + startRAST;
						} else {
							info = "No:" + RAST + ":" + startRAST + ":diff:" + (startNTerm - startRAST);
						}
					} else {
						int startRAST = geneRAST.getEnd();
						int startNTerm = Integer.parseInt(array[i][ArrayUtils.findColumn(array, "Nterm start")]);
						if (startNTerm == startRAST) {
							info = "Predicted:" + RAST + ":" + startRAST;
						} else {
							info = "No:" + RAST + ":" + startRAST + ":diff:" + (startNTerm - startRAST);
						}
					}
				} else {
					if (gene.isStrand()) {
						int startRAST = gene.getBegin();
						int startNTerm = Integer.parseInt(array[i][ArrayUtils.findColumn(array, "Nterm start")]);
						info = "No:" + RAST + ":" + startRAST + ":diff:" + (startNTerm - startRAST);

					} else {
						int startRAST = gene.getEnd();
						int startNTerm = Integer.parseInt(array[i][ArrayUtils.findColumn(array, "Nterm start")]);
						info = "No:" + RAST + ":" + startRAST + ":diff:" + (startNTerm - startRAST);
					}
				}
			}
			array[i][ArrayUtils.findColumn(array, "Christophe oracle")] = info;
		}
		TabDelimitedTableReader.save(array, "D:/suppl_table_3.txt");

	}

	/**
	 * Search for upstream TSS for each <code>NTerm</code><br>
	 * It uses list of aTIS
	 * 
	 * @param nameNTermData
	 */
	public static void findLeaderLess() {
		String[][] array = TabDelimitedTableReader
				.read(NTermUtils.getPATH() + "Statistics/LeaderLess_EGDe_protein.txt");
		ArrayList<String> aTISGenes = TabDelimitedTableReader
				.readList(NTermCreateFinalData.PATH_LIST + "aTIS_Francis_19052015.txt");
		for (int i = 1; i < array.length; i++) {
			String gene = array[i][3];
			if (aTISGenes.contains(gene)) {
				array[i][1] = "aTIS";
			}
		}
		TabDelimitedTableReader.save(array, NTermUtils.getPATH() + "Statistics/LeaderLess_EGDe_proteinyo.txt");
	}
}
