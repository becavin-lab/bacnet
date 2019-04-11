package bacnet.scripts.genome;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.biojava3.core.sequence.Strand;

import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.database.NGSCreation;
import bacnet.utils.UNAfold;

/**
 * Calculate Shine-Dalgarno profile for bacterial genome. <br>
 * Only first chromosome is taken into account
 * 
 * @author christophebecavin
 *
 */
public class SDProfile {

	public static String PATH = Database.getInstance().getPath() + "SD Profile/";

	public static void run() {

		// test();

		// GenomeConversion.run(Genome.ECOLI_NAME);
		// GenomeConversion.run(Genome.BACSUBTILIS_NAME);

		// Genome genome = Genome.loadEgdeGenome();
		//
		// calculateSDProfile(genome);

		// Genome genome2 = Genome.loadGenome(Genome.ECOLI_NAME);
		//
		// calculateSDProfile(genome2);

		// Genome genome3 = Genome.loadGenome(Genome.BACSUBTILIS_NAME);
		//
		// calculateSDProfile(genome3);

		// cleanUpTempFiles();

		// regroupResults(Genome.EGDE_NAME);
		// regroupResults(Genome.ECOLI_NAME);
		// regroupResults(Genome.BACSUBTILIS_NAME);
		// correctResults(Genome.EGDE_NAME);

		// organizeResults(Genome.EGDE_NAME);
		// organizeResults(Genome.ECOLI_NAME);
		// organizeResults(Genome.BACSUBTILIS_NAME);

		// verifyResults(Genome.EGDE_NAME);
		// verifyResults(Genome.ECOLI_NAME);
		// verifyResults(Genome.BACSUBTILIS_NAME);

		filterCopyResults(Genome.EGDE_NAME, "EGDe");
		filterCopyResults(Genome.ECOLI_NAME, "Ecoli");
		filterCopyResults(Genome.BACSUBTILIS_NAME, "Bsub");

		StartCodonProfile.run();

		Experiment exp = new Experiment();
		exp.addBioCond(BioCondition.getBioCondition("SDProfile_Bsub"));
		exp.addBioCond(BioCondition.getBioCondition("SDProfile_EGDe"));
		exp.addBioCond(BioCondition.getBioCondition("SDProfile_Ecoli"));
		// // Weissman RiboProfiling data if needed
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Bsub_30_WEISS_1"));
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Bsub_60_WEISS_1"));
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Bsub_60_WEISS_2"));
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Ecoli_OlacZ_WEISS"));
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Ecoli_OlacZ_WEISS"));
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Ecoli_ompF_WEISS"));
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Ecoli_ompF_WEISS"));
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Ecoli_WEISS_1"));
		// exp.addBioCond(BioCondition.getBioCondition("Ribo_Ecoli_WEISS_2"));

		boolean logTransformed = true;
		NGSCreation.convertCoverageFiles(exp, logTransformed);

	}

	public static void calculateSDProfile(Genome genome) {
		int step = 10;
		// int nbstep = genome.getFirstChromosome().getLength() / step + 1;
		// System.out.println(genome.getFirstChromosome().getLength()+" "+nbstep+"
		// "+(nbstep*500));
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int i = 30000; i < 30100; i++) {
			final Genome genomeTemp = genome;
			final int begin = i * step;
			final int end = (i + 1) * step;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					calculateOneSDProfile(genomeTemp, begin, end, Strand.POSITIVE,
							genomeTemp.getSpecies() + "_+_" + begin + "_" + end + ".wig");
				}
			};
			executor.execute(runnable);
			Runnable runnable2 = new Runnable() {
				@Override
				public void run() {
					calculateOneSDProfile(genomeTemp, begin, end, Strand.NEGATIVE,
							genomeTemp.getSpecies() + "_-_" + begin + "_" + end + ".wig");
				}
			};
			executor.execute(runnable2);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception");
		}
		System.err.println("All SD profile have been calculated");

	}

	public static void calculateOneSDProfile(Genome genome, int begin, int end, Strand strand, String fileName) {
		ArrayList<String> results = new ArrayList<>();
		for (int i = begin; i < end; i++) {
			if (i > 6 && i < (genome.getFirstChromosome().getLength() - 6)) {
				String sequence = genome.getFirstChromosome().getSequenceAsString(i - 6, i + 6, strand);
				// System.out.println(sequence);
				double energy = UNAfold.hybridRNA(sequence, i + "" + strand, Sequence.ANTI_SD_SEQ,
						i + "" + strand + "anti-SD", true);
				String result = i + "\t";
				result += energy;
				// if(energy<0){
				// result+= (-energy)+"";
				// }else{
				// result+="0";
				// }
				results.add(result);
			}
		}
		TabDelimitedTableReader.saveList(results, Database.getTEMP_PATH() + fileName);
	}

	public static void cleanUpTempFiles() {

		String PATH = UNAfold.PATH_DATA;
		System.out.println("Clean up: " + PATH);
		// max length 4639675
		for (int i = 1000000; i < 4000000; i++) {
			String fileNameNeg = PATH + i + "NEGATIVE-" + i + "NEGATIVEanti-SD";
			String fileNamePos = PATH + i + "POSITIVE-" + i + "POSITIVEanti-SD";

			/*
			 * Try to delete the File
			 */
			try {
				String[] fileNames = { fileNamePos + ".run", fileNamePos + ".ct", fileNamePos + ".asc",
						fileNamePos + ".37.plot", fileNamePos + ".37.ext" };
				for (String fileName : fileNames) {
					File file = new File(fileName);
					file.delete();
				}
				if (i % 100 == 0)
					System.out.println("Delete: " + fileNamePos);
			} catch (Exception e) {
				System.out.println("File do not exist:" + fileNamePos);
			}
			try {
				String[] fileNames = { fileNameNeg + ".run", fileNameNeg + ".ct", fileNameNeg + ".asc",
						fileNameNeg + ".37.plot", fileNameNeg + ".37.ext" };
				for (String fileName : fileNames) {
					File file = new File(fileName);
					file.delete();
				}
				if (i % 100 == 0)
					System.out.println("Delete: " + fileNameNeg);
			} catch (Exception e) {
				System.out.println("File do not exist:" + fileNamePos);
			}

		}

	}

	/**
	 * Read all text files created during SDProfile calculation and regroup them
	 * 
	 * @param genome
	 */
	public static void regroupResults(String genome) {
		String path = PATH + genome + File.separator;
		File pathFile = new File(path);
		ArrayList<String> resultsPlus = new ArrayList<>();
		ArrayList<String> resultsMinus = new ArrayList<>();
		ArrayList<String> resultsPlusFilter = new ArrayList<>();
		ArrayList<String> resultsMinusFilter = new ArrayList<>();
		System.out.println(path);
		for (File file : pathFile.listFiles()) {
			String[][] array = TabDelimitedTableReader.read(file);
			for (int i = 0; i < array.length; i++) {
				String result = array[i][0] + "\t" + array[i][1];
				if (file.getAbsolutePath().contains("_-_"))
					resultsMinus.add(result);
				else
					resultsPlus.add(result);
				System.out.println(result);

				/*
				 * Filter : take only minus values an transform into positive
				 */
				Double energy = Double.parseDouble(array[i][1]);
				if (energy < 0) {
					result += (-energy) + "";
				} else {
					result += "0";
				}
				String resultFilter = array[i][0] + "\t" + result;
				if (file.getAbsolutePath().contains("_-_"))
					resultsMinusFilter.add(resultFilter);
				else
					resultsPlusFilter.add(resultFilter);
				System.out.println(resultFilter);
			}
		}
		TabDelimitedTableReader.saveList(resultsMinus, PATH + genome + "_-_.wig");
		TabDelimitedTableReader.saveList(resultsPlus, PATH + genome + "_+_.wig");
		TabDelimitedTableReader.saveList(resultsMinus, PATH + genome + "_-_Filter.wig");
		TabDelimitedTableReader.saveList(resultsPlus, PATH + genome + "_+_Filter.wig");

	}

	/**
	 * Data from EGDe SD profile are corrupted need to correct them<br>
	 * Need to inverse result of values after the 1710999 base pair<br>
	 * <br>
	 * 
	 * IMPORTANT: Run only one time 13/01/2014
	 * 
	 * @param genome
	 */
	public static void correctResults(String genome) {
		genome = genome.replaceAll(" ", "_");
		String fileName = PATH + genome + "_-_.wig";
		String[][] array = TabDelimitedTableReader.read(fileName);
		for (int i = 0; i < array.length; i++) {
			int indexBP = Integer.parseInt(array[i][0]); // position in the genome in the .wig file
			double coverage = Double.parseDouble(array[i][1]); // position in the genome in the .wig file
			if (indexBP > 1710999) {
				array[i][1] = (-coverage) + "";
			}
		}
		TabDelimitedTableReader.save(array, fileName);
		fileName = PATH + genome + "_+_.wig";
		array = TabDelimitedTableReader.read(fileName);
		for (int i = 0; i < array.length; i++) {
			int indexBP = Integer.parseInt(array[i][0]); // position in the genome in the .wig file
			double coverage = Double.parseDouble(array[i][1]); // position in the genome in the .wig file
			if (indexBP > 1710999) {
				array[i][1] = (-coverage) + "";
			}
		}
		TabDelimitedTableReader.save(array, fileName);
	}

	/**
	 * Go through the tables created, reorganize the different indexes to order Base
	 * pairs.<br>
	 * For random values, verify that the SD calculation is good.
	 * 
	 * @param genome
	 */
	public static void organizeResults(String genomeName) {
		Genome genome = Genome.loadGenome(genomeName);
		genomeName = genomeName.replaceAll(" ", "_");
		/*
		 * Organize RawData by copying the coverage value at the right base pair index
		 */
		System.out.println(genome.getFirstChromosome().getLength());
		String[][] arrayPlus = TabDelimitedTableReader.read(PATH + genomeName + "_+_ - RawData.wig");
		String[][] arrayMinus = TabDelimitedTableReader.read(PATH + genomeName + "_-_ - RawData.wig");
		System.out.println("length: " + arrayPlus.length);
		String[][] arrayPlusNew = new String[genome.getFirstChromosome().getLength() + 1][2];
		String[][] arrayMinusNew = new String[genome.getFirstChromosome().getLength() + 1][2];
		for (int i = 0; i < arrayPlusNew.length; i++) {
			arrayPlusNew[i][0] = i + "";
			arrayPlusNew[i][1] = 0 + "";
			arrayMinusNew[i][0] = i + "";
			arrayMinusNew[i][1] = 0 + "";
		}
		for (int i = 0; i < arrayPlus.length; i++) {
			// System.out.println(i+" "+arrayPlus[i][0]+" - "+arrayPlus[i][1]);
			int indexBP = Integer.parseInt(arrayPlus[i][0]); // position in the genome in the .wig file
			double coverage = Double.parseDouble(arrayPlus[i][1]); // position in the genome in the .wig file
			arrayPlusNew[indexBP][0] = indexBP + "";
			arrayPlusNew[indexBP][1] = coverage + "";
		}
		TabDelimitedTableReader.save(arrayPlusNew, PATH + genomeName + "_+_.wig");
		for (int i = 0; i < (arrayMinus.length - 1); i++) {
			int indexBP = Integer.parseInt(arrayMinus[i][0]); // position in the genome in the .wig file
			double coverage = Double.parseDouble(arrayMinus[i][1]); // position in the genome in the .wig file
			arrayMinusNew[indexBP][0] = indexBP + "";
			arrayMinusNew[indexBP][1] = coverage + "";
		}
		TabDelimitedTableReader.save(arrayMinusNew, PATH + genomeName + "_-_.wig");
	}

	/**
	 * For each Gene calculate the anti-SD energy and compare it to the values given
	 * by the SDProfile
	 * 
	 * @param genomeName
	 */
	public static void verifyResults(String genomeName) {
		Genome genome = Genome.loadGenome(genomeName);
		System.out.println(genome.getFirstChromosome().getLength());
		genomeName = genomeName.replaceAll(" ", "_");
		// /*
		// * Organize RawData by copying the coverage value at the right base pair index
		// */
		ArrayList<String> results = new ArrayList<>();
		String[][] arrayPlus = TabDelimitedTableReader.read(PATH + genomeName + "_+_.wig");
		String[][] arrayMinus = TabDelimitedTableReader.read(PATH + genomeName + "_-_.wig");
		System.out.println(arrayPlus.length + "  -  " + arrayMinus.length);// int countdiff = 0;
		for (Gene gene : genome.getFirstChromosome().getGenes().values()) {
			// if(gene.getName().equals("b0001")){
			// verifyGene(gene, arrayPlus, arrayMinus);
			// }
			String result = verifyGene(gene, arrayPlus, arrayMinus);
			if (!result.equals("")) {
				results.add(result);
			}
		}
		System.out.println("Diff: " + results.size() + " on " + genome.getFirstChromosome().getGenes().size());
		TabDelimitedTableReader.saveList(results, "D:/DiffEnergy.txt");
	}

	/**
	 * For a given Gene calculate its SD binding energy, and compare to the value
	 * given by SDProfile data
	 * 
	 * @param gene
	 * @param arrayPlus
	 * @param arrayMinus
	 */
	public static String verifyGene(Gene gene, String[][] arrayPlus, String[][] arrayMinus) {

		double energy = UNAfold.hybridRNA(gene.getSDSequence(), gene.getName(), Sequence.ANTI_SD_SEQ, "anti-SD", true);
		// System.out.println(gene.getName()+" SD value: "+energy);
		if (gene.isStrand()) {
			Sequence seq = new Sequence("sd", gene.getBegin() - 20, gene.getBegin() + 3, '+');
			double energyMin = 10000;
			for (int i = seq.getBegin(); i < seq.getEnd(); i++) {
				double energyTemp = Double.parseDouble(arrayPlus[i][1]);
				if (energyTemp < energyMin)
					energyMin = energyTemp;
				// System.out.println(energyTemp);
			}
			if (energyMin < (energy - 0.5))
				System.out.println("Lower value - " + gene.getName() + " Data: " + energyMin + "  gene: " + energy);
			if (energyMin > (energy + 0.5))
				System.err.println("Higher value - " + gene.getName() + " Data: " + energyMin + "  gene: " + energy);
			if (energyMin == energy)
				return "";
			else {
				String result = gene.getName() + "\t" + energyMin + "\t" + energy + "\t" + (energyMin - energy) + "\t"
						+ Math.abs((energyMin - energy));
				return result;
			}
		} else {
			Sequence seq = new Sequence("sd", gene.getEnd() - 3, gene.getEnd() + 20, '-');
			double energyMin = 10000;
			for (int i = seq.getBegin(); i < seq.getEnd(); i++) {
				double energyTemp = Double.parseDouble(arrayMinus[i][1]);
				if (energyTemp < energyMin)
					energyMin = energyTemp;
				// System.out.println(energyTemp);
			}
			if (energyMin < (energy - 0.5))
				System.out.println("Lower value - " + gene.getName() + " Data: " + energyMin + "  gene: " + energy);
			if (energyMin > (energy + 0.5))
				System.err.println("Higher value - " + gene.getName() + " Data: " + energyMin + "  gene: " + energy);
			if (energyMin == energy)
				return "";
			else {
				String result = gene.getName() + "\t" + energyMin + "\t" + energy + "\t" + (energyMin - energy) + "\t"
						+ Math.abs((energyMin - energy));
				return result;
			}
		}
	}

	/**
	 * Filters all files by deleting all positive results, and inverting energies.
	 * 
	 * @param genome
	 */
	public static void filterCopyResults(String genome, String genomeName) {
		genome = genome.replaceAll(" ", "_");
		String fileName = PATH + genome + "_-_.wig";
		ArrayList<String> resultsPlusFilter = new ArrayList<>();
		ArrayList<String> resultsMinusFilter = new ArrayList<>();
		System.out.println(fileName);
		String[][] array = TabDelimitedTableReader.read(fileName);
		for (int i = 0; i < array.length; i++) {
			/*
			 * Filter : take only minus values an transform into positive
			 */
			Double energy = Double.parseDouble(array[i][1]);
			String result = "";
			if (energy < -1) {
				result += (-energy) + "";
			} else {
				result += "0";
			}
			String resultsFilter = array[i][0] + "\t" + result;
			resultsMinusFilter.add(resultsFilter);
			// System.out.println(resultsFilter);
		}
		TabDelimitedTableReader.saveList(resultsMinusFilter, PATH + genome + "_-_Filter.wig");
		String dataName = "SDProfile_" + genomeName;
		TabDelimitedTableReader.saveList(resultsMinusFilter, OmicsData.PATH_NGS_RAW + dataName + "_r.wig");

		fileName = PATH + genome + "_+_.wig";
		System.out.println(fileName);
		array = TabDelimitedTableReader.read(fileName);
		for (int i = 0; i < array.length; i++) {
			/*
			 * Filter : take only minus values an transform into positive
			 */
			Double energy = Double.parseDouble(array[i][1]);
			String result = "";
			if (energy < 0) {
				result += (-energy) + "";
			} else {
				result += "0";
			}
			String resultsFilter = array[i][0] + "\t" + result;
			resultsPlusFilter.add(resultsFilter);
			// System.out.println(resultsFilter);
		}
		TabDelimitedTableReader.saveList(resultsPlusFilter, PATH + genome + "_+_Filter.wig");
		dataName = "SDProfile_" + genomeName;
		TabDelimitedTableReader.saveList(resultsPlusFilter, OmicsData.PATH_NGS_RAW + dataName + "_f.wig");
	}

	public static void test() {
		Genome genome = Genome.loadEgdeGenome();

		Sequence seq = genome.getGeneFromName("lmo2000");
		test(seq, genome);
		// seq = genome.getGeneFromName("lmo0837");
		// test(seq,genome);

	}

	private static void test(Sequence seq, Genome genome) {
		double energy = UNAfold.hybridRNA(seq.getSDSequence(), seq.getName(), Sequence.ANTI_SD_SEQ, "anti-SD", true);
		System.out.println(seq.getName() + " SD value: " + energy);
		// postivie strand
		int decay = 6;
		for (int i = seq.getBegin() - 20; i < seq.getBegin() + 3; i++) {
			String sequence = genome.getFirstChromosome().getSequenceAsString(i - decay, i + decay, Strand.POSITIVE);
			energy = UNAfold.hybridRNA(sequence, seq.getName(), Sequence.ANTI_SD_SEQ, seq.getName() + "anti-SD", true);
			System.out.println(seq.getName() + " SD: " + energy);
		}
		// negative strand
		for (int i = seq.getEnd() - 3; i < seq.getEnd() + 20; i++) {
			String sequence = genome.getFirstChromosome().getSequenceAsString(i - decay, i + decay, Strand.NEGATIVE);
			energy = UNAfold.hybridRNA(sequence, seq.getName(), Sequence.ANTI_SD_SEQ, seq.getName() + "anti-SD", true);
			System.out.println(seq.getName() + " SD: " + energy);
		}
	}
}
