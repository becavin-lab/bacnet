package bacnet.datamodel.proteomics;

import java.io.File;
import java.util.ArrayList;

import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.proteomics.NTerm.ExperimentName;
import bacnet.datamodel.proteomics.NTerm.TypeModif;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;

/**
 * List of methods for creating Summary Table for TIS <br>
 * Using NTermFilter we have regroup TIS in different overlap groups, which we
 * use now to create the Summary Tables.
 * 
 * 
 * @author UIBC
 *
 */
public class NTermSummaryTable {

	public static void getSummaryTables(NTermData NTermData, String pathResult) {
		ArrayList<String> summary = new ArrayList<>();
		File path = new File(pathResult + NTermData.getName());
		path.mkdir();
		for (String typeOverlap : NTermData.getTypeOverlaps()) {
			String result = getSummaryTable(typeOverlap, NTermData, path.getAbsolutePath() + File.separator);
			summary.add(result);
		}
		TabDelimitedTableReader.saveList(summary, pathResult + NTermData.getName() + "_Summary.txt");
		getGeneSummaryTable(NTermData, pathResult);

	}

	/**
	 * For every peptide, corresponding to a Type of overlap, create a summary
	 * table. When a peptide has been already put in a summary peptide, its other
	 * mapped peptide are put in <code>notInSummary ArrayList</code>
	 * 
	 * @param typeOverlap
	 * @param nameNTermData
	 * @param alreadyMapped
	 * @param notInSummary
	 */
	private static String getSummaryTable(String typeOverlap, NTermData nTermData, String pathResult) {
		int sequenceInSerumAndBHI = 0;
		int totalSequenceInSerum = 0;
		Genome genome = Genome.loadEgdeGenome();
		ArrayList<String> finalTable = new ArrayList<String>();
		String[] headers = { "Peptide sequence", "Duplicates mapping", "MaxQuant", "MaxQuantum", "TIS", "MappingFrame",
				"Soluble", "Insoluble", "Begin", "End", "Strand", "AcD3 Name;AcD3 spectra;AcD3 score;AcD3 threshold",
				"Ace Name;Ace spectra;Ace score;Ace threshold", "For Name;For spectra;For score;For threshold",
				"Experiments Spectrum", "Start codon", "Previous codon", "Next codon", "Start code", "Overlap",
				"Overlap in frame", "Operon", "Description", "COG", "TSS up type (-position)", "TSS up distance",
				"TSS up coverage", "TSS down type (-position)", "TSS down distance", "TSS down coverage", "Codon Usage",
				"SD ?", "SD sequence (20bp upstream)" };

		String header = "";
		for (String temp : headers)
			header += temp + "\t";
		finalTable.add(header);
		for (NTerm nTerm : nTermData.getNTerms()) {
			if (nTerm.getTypeOverlap() == typeOverlap) {
				NTerm acD3Nterm = new NTerm();
				NTerm aceNterm = new NTerm();
				NTerm forNterm = new NTerm();

				if (nTerm.getTypeModif() == TypeModif.AcD3)
					acD3Nterm = nTerm;
				if (nTerm.getTypeModif() == TypeModif.Ace)
					aceNterm = nTerm;
				if (nTerm.getTypeModif() == TypeModif.For)
					forNterm = nTerm;

				String maxquantum = "";
				if (!nTerm.getMaxQuantum()[0].equals(""))
					maxquantum += ExperimentName.Trypsin_Soluble + ";" + nTerm.getMaxQuantum()[0] + ";";
				if (!nTerm.getMaxQuantum()[1].equals(""))
					maxquantum += ExperimentName.Trypsin_Insoluble + ";" + nTerm.getMaxQuantum()[1] + ";";
				if (!nTerm.getMaxQuantum()[2].equals(""))
					maxquantum += ExperimentName.Trypsin_Actino_Soluble + ";" + nTerm.getMaxQuantum()[2] + ";";
				if (!nTerm.getMaxQuantum()[3].equals(""))
					maxquantum += ExperimentName.Trypsin_Actino_Insoluble + ";" + nTerm.getMaxQuantum()[3] + ";";
				if (!nTerm.getMaxQuantum()[4].equals(""))
					maxquantum += ExperimentName.GluC_Actino_Soluble + ";" + nTerm.getMaxQuantum()[4] + ";";
				if (!nTerm.getMaxQuantum()[5].equals(""))
					maxquantum += ExperimentName.GluC_Actino_Insoluble + ";" + nTerm.getMaxQuantum()[5] + ";";

				String tisName = "";
				if (nTermData.getTisMap().containsKey(nTerm)) {
					tisName = nTermData.getTisMap().get(nTerm).getName();
				}

				String[] rows = { nTerm.getSequencePeptide(), (nTerm.getDuplicates().size() + 1) + "",
						nTerm.getMaxQuant(), maxquantum, tisName, nTerm.getMappingFrame() + "", nTerm.isSoluble() + "",
						nTerm.isInsoluble() + "", nTerm.getBegin() + "", nTerm.getEnd() + "", nTerm.getStrand() + "" };
				String row = "";
				for (String temp : rows)
					row += temp + "\t";

				if (!acD3Nterm.getName().equals(""))
					row += acD3Nterm.getName() + ";" + acD3Nterm.getSpectra() + ";" + acD3Nterm.getScore() + ";"
							+ acD3Nterm.getThreshold() + "\t";
				else
					row += "\t";
				if (!aceNterm.getName().equals(""))
					row += aceNterm.getName() + ";" + aceNterm.getSpectra() + ";" + aceNterm.getScore() + ";"
							+ aceNterm.getThreshold() + "\t";
				else
					row += "\t";
				if (!forNterm.getName().equals(""))
					row += forNterm.getName() + ";" + forNterm.getSpectra() + ";" + forNterm.getScore() + ";"
							+ forNterm.getThreshold() + "\t";
				else
					row += "\t";

				String spectrum = "";
				if (nTerm.getSpectrum()[0] != 0)
					spectrum += ExperimentName.Trypsin_Soluble + ";" + nTerm.getSpectrum()[0] + ";";
				if (nTerm.getSpectrum()[1] != 0)
					spectrum += ExperimentName.Trypsin_Insoluble + ";" + nTerm.getSpectrum()[1] + ";";
				if (nTerm.getSpectrum()[2] != 0)
					spectrum += ExperimentName.Trypsin_Actino_Soluble + ";" + nTerm.getSpectrum()[2] + ";";
				if (nTerm.getSpectrum()[3] != 0)
					spectrum += ExperimentName.Trypsin_Actino_Insoluble + ";" + nTerm.getSpectrum()[3] + ";";
				if (nTerm.getSpectrum()[4] != 0)
					spectrum += ExperimentName.GluC_Actino_Soluble + ";" + nTerm.getSpectrum()[4] + ";";
				if (nTerm.getSpectrum()[5] != 0)
					spectrum += ExperimentName.GluC_Actino_Insoluble + ";" + nTerm.getSpectrum()[5] + ";";
				row += spectrum + "\t";

				String operon = "";
				String cog = "";
				String description = "";
				if (!nTerm.getOverlap().equals("") && !nTerm.getOverlap().equals("intergenic")) {
					String locus = nTerm.getOverlap().split(":")[0].trim();
					Object sequence = genome.getElement(locus);
					if (sequence != null) {
						if (sequence instanceof Gene) {
							Gene gene = (Gene) sequence;
							operon = gene.getOperon();
							cog = gene.getCog();
							description = gene.getInfo();
						} else {
							Sequence seq = (Sequence) sequence;
							description = seq.getComment();
						}
					}
				}
				String[] rows2 = { nTerm.getStartCodon(), nTerm.getPreviousCodon(), nTerm.getNextCodon(),
						nTerm.getStartCode(), nTerm.getOverlap(), nTerm.isOverlapInFrame() + "", operon, description,
						cog, nTerm.getTssUptype(), nTerm.getTssUpDistance() + "", nTerm.getTssUpCoverage() + "",
						nTerm.getTssDowntype(), nTerm.getTssDownDistance() + "", nTerm.getTssDownCoverage() + "",
						nTerm.getCodonUsage() + "", nTerm.getAntiSDBindingFreeEnergy() + "", nTerm.getSDSequence() };
				for (String temp : rows2)
					row += temp + "\t";

				finalTable.add(row);
			}
		}
		TabDelimitedTableReader.saveList(finalTable, pathResult + nTermData.getName() + "_" + typeOverlap + ".txt");

		String result = typeOverlap + "\t" + (finalTable.size() - 1) + "\t FoundBHIAndSerum: " + sequenceInSerumAndBHI
				+ " / " + totalSequenceInSerum;
		System.out.println(result);
		return result;
	}

	private static void getGeneSummaryTable(NTermData NTermData, String pathResult) {
		// Genome genome = Genome.loadEgdeGenome();
		// String[][] arrays =
		// TabDelimitedTableReader.read(NTermUtils.getPATH()+"EGDePossiblePeptide.txt");
		// ArrayList<String> detectables = new ArrayList<String>();
		// for(int i=1;i<arrays.length;i++){
		// detectables.add(arrays[i][2]);
		// }
		// GeneExpression geneExpr = new
		// GeneExpression(EGDeWTdata.NAME_Mean+GeneExpression.EXTENSION);
		// try {
		// geneExpr.read();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// ArrayList<String> finalTable = new ArrayList<String>();
		// String[] headers = {"Locus","detectable","Transcriptome EGDe_37C_Mean.ge","nb
		// peptides","nb_spectra","aTIS spectra nb","dTISM spectra nb","dTIS spectra
		// nb","uTIS spectra
		// nb","aTIS","dTISM","dTIS","uTIS","Operon","Description","COG"};
		// String header = "";
		// for(String temp : headers) header+=temp+"\t";
		// finalTable.add(header);
		// for(Gene gene : genome.getChromosomes().get(0).getGenes().values()){
		// String detectable = "";
		// if(detectables.contains(gene.getName())) detectable="yes";
		// String expression = "";
		// if(geneExpr.containProbe(gene.getName())){
		// expression = geneExpr.getMedianValue(gene.getName())+"";
		// }
		// String operon = "";
		// String cog = "";
		// String description = "";
		// operon = gene.getOperon();
		// cog = gene.getCog();
		// description = gene.getInfo();
		//
		// if(NTermData.getGenes().containsKey(gene.getName())){
		// String row = "";
		// ArrayList<NTerm> nTerms = nTermExp.getGenes().get(gene.getName());
		//
		// row+=gene.getName()+"\t"+detectable+"\t"+expression+"\t"+nTerms.size()+"\t";
		// int aTISnb = 0;
		// String aTIS = "";
		// int dTISMnb = 0;
		// String dTISM = "";
		// int dTISnb = 0;
		// String dTIS = "";
		// int uTISnb = 0;
		// String uTIS = "";
		// int nbSpectra = 0;
		// for(NTerm nTerm : nTerms){
		// nbSpectra+=nTerm.getSpectra();
		// TypeOverlap type = nTerm.getTypeOverlap();
		// if(type==TypeOverlap.aTIS){
		// aTISnb+=nTerm.getSpectra();
		// aTIS+=nTerm.getName()+";";
		// }else if(type==TypeOverlap.dTISM){
		// dTISMnb+=nTerm.getSpectra();
		// dTISM+=nTerm.getName()+";";
		// }else if(type==TypeOverlap.dTIS){
		// dTISnb+=nTerm.getSpectra();
		// dTIS+=nTerm.getName()+";";
		// }else if(type==TypeOverlap.uTIS){
		// uTISnb+=nTerm.getSpectra();
		// uTIS+=nTerm.getName()+";";
		// }
		// }
		//
		// String[] rows2 =
		// {nbSpectra+"",aTISnb+"",dTISMnb+"",dTISnb+"",uTISnb+"",aTIS,dTISM,dTIS,uTIS,operon,description,cog};
		// for(String temp : rows2) row+=temp+"\t";
		// finalTable.add(row);
		// }else{
		// String[] rows =
		// {gene.getName(),detectable,expression,"0","","","","","","","","",operon,description,cog};
		// String row = "";
		// for(String temp : rows) row+=temp+"\t";
		// finalTable.add(row);
		// }
		// }
		// TabDelimitedTableReader.saveList(finalTable,
		// NTermCreateData.PATH_RESULTS+"Summary_lmo_"+nameNTermData+".txt");
	}
}
