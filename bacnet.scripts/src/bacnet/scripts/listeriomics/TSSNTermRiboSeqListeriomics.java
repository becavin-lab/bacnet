package bacnet.scripts.listeriomics;

import java.util.ArrayList;
import java.util.TreeSet;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;

/**
 * List of methods to process TSS, TermSeq, and RiboSeq of Listeriomics Some TSS
 * and NTerm lists were provided in tab-delimited format. Some methods are
 * provided here to construct .wig files
 *
 * @author Christophe Bécavin
 *
 */
public class TSSNTermRiboSeqListeriomics {

	public static String PATH_TSSasRNA = ExpressionData.PATH_NGS_RAW + "asRNA-TSS-EGD-e/";
	public static String PATH_TSS_EGDe = ExpressionData.PATH_NGS_RAW + "TSS_data_EGDe.txt";
	public static String PATH_TSS_Innocua = ExpressionData.PATH_NGS_RAW + "TSS_data_Innocua.txt";
	public static String[] TSS_HEADERS = { "EGDe_37C_TSS", "sigB", "prfA", "EGDe_30C", "EGDe_minusO2", "EGDe_Stat",
			"EGDe_Complete_TSS" };
	public static String[] TSS_Innocua_HEADERS = { "Innocua_37C_TSS" };
	public static String PATH_TermSeq_EGDe = ExpressionData.PATH_NGS_RAW + "TermSeq_data_EGDe.txt";
	public static String[] TermSeq_HEADERS = { "EGDe_37C_TermSeq" };

	/**
	 * Run createListeriomicsTSSTermSeq() to construct wig files from tab-delimited
	 * files
	 */
	public static void run() {

		// RiboSeq - nothing to do wig files are easy to download

		/*
		 * TSS data creation
		 */
		createListeriomicsTSSTermSeq();

	}

	/**
	 * Process Daniel Dar TermSeq
	 */
	public void processDanielRiboSeq() {
		String[] listdata = { "Innocua_37C_RiboSeq_r.wig", "Innocua_37C_RNASeq_r.wig", "EGDe_37C_RiboSeq_r.wig",
				"EGDe_37C_Weizmann_r.wig" };
		for (String fileName : listdata) {
			fileName = Database.getInstance().getPath() + "Listeriomics/RiboProfiling/" + fileName;
			System.out.println(fileName);
			ArrayList<String> array = TabDelimitedTableReader.readList(fileName, true);
			int i = 1;
			ArrayList<String> newWig = new ArrayList<>();
			for (String row : array) {
				String[] elements = row.split("\t");
				String newRow = elements[0] + "\t" + i + "\t" + elements[3];
				i++;
				newWig.add(newRow);
			}
			TabDelimitedTableReader.saveList(newWig, fileName + "._modify.txt");
		}
	}

	/**
	 * Create TSS (Transcription Start Site) and TermSeq wig files
	 * 
	 * @param exp
	 */
	public static void createListeriomicsTSSTermSeq() {

		/*
		 * EGD-e TSS
		 */
		String[][] tssArray = TabDelimitedTableReader.read(PATH_TSS_EGDe);
		Genome genome = Genome.loadEgdeGenome();
		for (String header : TSS_HEADERS) {
			int j = ArrayUtils.findColumn(tssArray, header);

			double[] valuesPlus = new double[genome.getFirstChromosome().getLength()];
			double[] valuesMinus = new double[genome.getFirstChromosome().getLength()];

			for (int i = 1; i < tssArray.length; i++) {
				int position = Integer.parseInt(tssArray[i][0]);
				boolean strand = true;
				int value = Integer.parseInt(tssArray[i][j]);
				if (tssArray[i][1].contains("-"))
					strand = false;
				if (value != 0) {
					if (strand) {
						valuesPlus[position - 1] = Math.log(value) / Math.log(2);
					} else {
						valuesMinus[position - 1] = -Math.log(value) / Math.log(2);
					}
				}
			}

			/*
			 * Create wig file
			 */
			ArrayList<String> plusWig = new ArrayList<>();
			ArrayList<String> minusWig = new ArrayList<>();
			plusWig.add("variableStep chrom=" + genome.getFirstChromosome().getChromosomeID());
			minusWig.add("variableStep chrom=" + genome.getFirstChromosome().getChromosomeID());
			for (int i = 1; i < valuesPlus.length; i++) {
				plusWig.add((i + 1) + "\t" + valuesPlus[i]);
				minusWig.add((i + 1) + "\t" + valuesMinus[i]);
			}

			String dataName = header;
			if (!dataName.contains("_TSS")) {
				dataName = dataName + "_TSS";
			}
			TabDelimitedTableReader.saveList(plusWig, OmicsData.PATH_NGS_NORM + "/" + dataName + "_f.wig");
			TabDelimitedTableReader.saveList(minusWig, OmicsData.PATH_NGS_NORM + "/" + dataName + "_r.wig");

		}

		/*
		 * Innocua TSS
		 */
		tssArray = TabDelimitedTableReader.read(PATH_TSS_Innocua);
		genome = Genome.loadGenome("Listeria innocua Clip11262");
		for (String header : TSS_Innocua_HEADERS) {
			int j = ArrayUtils.findColumn(tssArray, header);

			double[] valuesPlus = new double[genome.getFirstChromosome().getLength()];
			double[] valuesMinus = new double[genome.getFirstChromosome().getLength()];

			for (int i = 1; i < tssArray.length; i++) {
				int position = Integer.parseInt(tssArray[i][0]);
				boolean strand = true;
				int value = Integer.parseInt(tssArray[i][j]);
				if (tssArray[i][1].contains("-"))
					strand = false;
				if (value != 0) {
					if (strand) {
						valuesPlus[position - 1] = Math.log(value) / Math.log(2);
					} else {
						valuesMinus[position - 1] = -Math.log(value) / Math.log(2);
					}
				}
			}

			/*
			 * Create wig file
			 */
			ArrayList<String> plusWig = new ArrayList<>();
			ArrayList<String> minusWig = new ArrayList<>();
			plusWig.add("variableStep chrom=" + genome.getFirstChromosome().getChromosomeID());
			minusWig.add("variableStep chrom=" + genome.getFirstChromosome().getChromosomeID());
			for (int i = 1; i < valuesPlus.length; i++) {
				plusWig.add((i + 1) + "\t" + valuesPlus[i]);
				minusWig.add((i + 1) + "\t" + valuesMinus[i]);
			}

			String dataName = header;
			if (!dataName.contains("_TSS")) {
				dataName = dataName + "_TSS";
			}
			TabDelimitedTableReader.saveList(plusWig, OmicsData.PATH_NGS_NORM + "/" + dataName + "_f.wig");
			TabDelimitedTableReader.saveList(minusWig, OmicsData.PATH_NGS_NORM + "/" + dataName + "_r.wig");

		}

		/*
		 * EGD-e TermSeq
		 */
		tssArray = TabDelimitedTableReader.read(PATH_TermSeq_EGDe);
		genome = Genome.loadEgdeGenome();
		for (String header : TermSeq_HEADERS) {
			int j = ArrayUtils.findColumn(tssArray, header);

			double[] valuesPlus = new double[genome.getFirstChromosome().getLength()];
			double[] valuesMinus = new double[genome.getFirstChromosome().getLength()];

			for (int i = 1; i < tssArray.length; i++) {
				int position = Integer.parseInt(tssArray[i][0]);
				boolean strand = true;
				int value = Integer.parseInt(tssArray[i][j]);
				if (tssArray[i][1].contains("-"))
					strand = false;
				if (value != 0) {
					if (strand) {
						valuesPlus[position - 1] = Math.log(value) / Math.log(2);
					} else {
						valuesMinus[position - 1] = -Math.log(value) / Math.log(2);
					}
				}
			}

			/*
			 * Create wig file
			 */
			ArrayList<String> plusWig = new ArrayList<>();
			ArrayList<String> minusWig = new ArrayList<>();
			plusWig.add("variableStep chrom=" + genome.getFirstChromosome().getChromosomeID());
			minusWig.add("variableStep chrom=" + genome.getFirstChromosome().getChromosomeID());
			for (int i = 1; i < valuesPlus.length; i++) {
				plusWig.add((i + 1) + "\t" + valuesPlus[i]);
				minusWig.add((i + 1) + "\t" + valuesMinus[i]);
			}

			String dataName = header;
			TabDelimitedTableReader.saveList(plusWig, OmicsData.PATH_NGS_NORM + "/" + dataName + "_f.wig");
			TabDelimitedTableReader.saveList(minusWig, OmicsData.PATH_NGS_NORM + "/" + dataName + "_r.wig");

		}
	}

	/**
	 * Run different function to curate the data given by Omri 15/11/2012
	 */
	public static void createASrnaTSStable() {
		for (String tss : TSS_HEADERS) {
			parseList(tss);
		}
		combineAllLists();
		addToFinalData();

	}

	/**
	 * Add ASrna TSS information at the end of TSS_expression_acrosse_conditions
	 * file
	 */
	private static void addToFinalData() {
		ArrayList<String> tssList = TabDelimitedTableReader
				.readList(OmicsData.PATH_NGS_RAW + "TSS_expression_across_conditions.txt", true);
		ArrayList<String> tssListASrna = TabDelimitedTableReader.readList(PATH_TSSasRNA + "all_TSS_ASrna.txt", true);
		for (String line : tssListASrna) {
			tssList.add(line);
		}
		TabDelimitedTableReader.saveList(tssList, OmicsData.PATH_NGS_RAW + "TSS_expression_across_conditions.txt");
	}

	/**
	 * Read all list of ASrnaTSS and combine them into one table
	 */
	private static void combineAllLists() {
		/*
		 * Create a list of all positions
		 */
		TreeSet<String> allTSS = new TreeSet<String>();
		for (String tss : TSS_HEADERS) {
			String[][] data = TabDelimitedTableReader.read(PATH_TSSasRNA + tss + "_parsed.txt");
			for (String[] line : data) {
				allTSS.add(line[0] + "\t" + line[1]);
			}
		}
		System.out.println("found " + allTSS.size() + " positions");
		/*
		 * go through that list and create the final table
		 */
		String[][] allTssArray = TabDelimitedTableReader.read(PATH_TSSasRNA + "all_asRNA.pos.txt");
		ArrayList<String> finalTssList = new ArrayList<String>();
		for (String tss : allTSS) {
			String position = tss.split("\t")[0];
			/*
			 * Find information about this TSS
			 */
			int index = -1;
			for (int i = 0; i < allTssArray.length; i++) {
				for (String element : allTssArray[i][4].split(",")) {
					String positionTemp = element.split("\\(")[0].trim();
					if (position.equals(positionTemp))
						index = i;
				}
			}

			String ret = tss + "\tASrna\tNA\tNA";
			if (index != -1)
				ret = tss + "\tASrna\t" + allTssArray[index][7] + "\t" + allTssArray[index][6];

			/*
			 * Fill nbReads with the different value found in different bioCond
			 */
			int[] nbReads = new int[6];
			for (int j = 0; j < TSS_HEADERS.length; j++) {
				String[][] tssArray = TabDelimitedTableReader.read(PATH_TSSasRNA + TSS_HEADERS[j] + "_parsed.txt");
				int k = -1;
				for (int i = 0; i < tssArray.length; i++) {
					if (position.equals(tssArray[i][0]))
						k = i;
				}
				if (k != -1)
					nbReads[j] = Integer.parseInt(tssArray[k][2]);
			}
			int count = 0;
			for (int nbRead : nbReads) {
				ret += "\t" + nbRead;
				count += nbRead;
			}
			ret += "\t" + count;
			finalTssList.add(ret);
		}
		TabDelimitedTableReader.saveList(finalTssList, PATH_TSSasRNA + "all_TSS_ASrna.txt");
	}

	/**
	 * Curate list given by Omri, by parsing each element
	 * 
	 * @param fileName
	 */
	private static void parseList(String fileName) {
		ArrayList<String> wtNew = new ArrayList<String>();
		ArrayList<String> wt = TabDelimitedTableReader.readList(PATH_TSSasRNA + fileName + ".txt");
		for (String line : wt) {

			String[] elements = line.split(",");
			for (String element : elements) {
				int index = -1;
				String pos = element.split("\\(")[0].trim();
				String subElement = element.substring(element.indexOf("str") + 3, element.indexOf(')'));
				String strand = subElement.split("\\#")[0];
				String nbReads = subElement.split("\\#")[1];
				String ret = pos + "\t" + strand + "\t" + nbReads;
				wtNew.add(ret);
				// System.out.println(ret);
			}
		}
		TabDelimitedTableReader.saveList(wtNew, PATH_TSSasRNA + fileName + "_parsed.txt");
	}

}
