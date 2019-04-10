package bacnet.scripts.core;

import java.io.File;
import java.io.IOException;

import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;
import bacnet.utils.VectorUtils;

public class UNAfold {

	private static String PATH_BIN = "C:/UNAFold/bin/";
	public static String PATH_DATA = Database.getTEMP_PATH() + "UNAFold" + File.separator;

	public static String hybrid_ss_min = "\"" + PATH_BIN + "hybrid-ss-min\" --suffix DAT --mfold";
	public static String hybrid_ss = "\"" + PATH_BIN + "hybrid-ss\" --suffix DAT --tracebacks 100";
	public static String ct_energy = "\"" + PATH_BIN + "ct-energy\" --suffix DAT";
	public static String ct_prob = "\"" + PATH_BIN + "ct-prob\"";
	public static String plot2ann = "perl " + "\"" + PATH_BIN + "plot2ann.pl\""; // Warning: This program is a Perl
																					// script (Perl has to be installed
																					// on
																					// your computer)
	public static String unafold = "perl " + "\"" + PATH_BIN + "UNAFold.pl\""; // Warning: This program is a Perl script
																				// (Perl has to be installed on your
																				// computer)
	public static String sir_graph = "\"" + PATH_BIN + "sir_graph\"";

	public static String hybrid_min = "\"" + PATH_BIN + "hybrid-min\"";

	/**
	 * Run UNAFOld.pl Perl script
	 * 
	 * @param seq
	 * @param fileName
	 */
	public static void unafoldRNA(Sequence seq, String fileName) {
		String fastaFile = ">" + seq.getName() + "\n" + seq.getSequenceRNA();
		try {
			System.out.println(fastaFile);
			File fileTemp = File.createTempFile("UnaFold", seq.getName());
			FileUtils.saveText(fastaFile, fileTemp.getAbsolutePath());
			String execProcess = unafold + " \"" + fileTemp.getAbsolutePath() + "\"";
			CMD.runProcess(execProcess, true);
			fileTemp.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * For a specific sequence, get its RNA sequence and fold it<br>
	 * Calculate possible free energies<br>
	 * transform results into annotation file<br>
	 * Display using sir_graph
	 * 
	 * @param name
	 * @param seq
	 * @param fileName   prefix of the file (Extension will be add automatically),
	 *                   it will be saved in the path <code>UnaFold.PATH_DATA</code>
	 * @param removeTemp remiove temporary files from hardrive
	 */
	public static double foldRNA(String name, String seq, String fileName, boolean removeTemp) {
		String fastaFile = ">" + name + "\n" + seq;
		double energy = 0;
		try {
			// System.out.println(fastaFile);
			File fileTemp = File.createTempFile("UnaFold", name);
			FileUtils.saveText(fastaFile, fileTemp.getAbsolutePath());
			String execProcess = hybrid_ss_min + " -o \"" + (PATH_DATA + fileName) + "\" \""
					+ fileTemp.getAbsolutePath() + "\"";
			// String execProcess = hybrid_ss+" -o \""+(PATH_DATA+fileName)+"\"
			// \""+fileTemp.getAbsolutePath()+"\"";
			CMD.runProcess(execProcess, true);
			fileTemp.delete();
			selectFirstResult(fileName + ".ct");
			// plot2Ann(fileName+".37.ct", fileName+".37.plot",fileName+".37.ann");
			// selectFirstResult(fileName+".37.ann",selectFirstResult(fileName+".37.ct"));
			// plotFolding(fileName+".ct",900,"");
			// plotFolding(fileName+".ct",300,"Mini-");
			/*
			 * Get energy
			 */
			File file = new File(PATH_DATA + fileName + ".DG");
			String[][] energies = TabDelimitedTableReader.read(file);
			try {
				energy = Double.parseDouble(energies[1][1]);
			} catch (Exception e) {
			}
			if (removeTemp) {
				file = new File(PATH_DATA + fileName + ".PLOT");
				file.delete();
				file = new File(PATH_DATA + fileName + ".ANN");
				file.delete();
				file = new File(PATH_DATA + fileName + ".RUN");
				file.delete();
				file = new File(PATH_DATA + fileName + ".CT");
				file.delete();
				file = new File(PATH_DATA + fileName + ".DG");
				file.delete();
			}
			return energy;
		} catch (Exception e) {
			e.printStackTrace();
			return energy;
		}
	}

	/**
	 * For a specific sequence, get its RNA sequence and fold it<br>
	 * Calculate possible free energies<br>
	 * transform results into annotation file<br>
	 * Display using sir_graph
	 * 
	 * @param seq
	 * @param fileName   prefix of the file (Extension will be add automatically),
	 *                   it will be saved in the path <code>UnaFold.PATH_DATA</code>
	 * @param removeTemp remiove temporary files from hardrive
	 */
	public static void foldRNA(Sequence seq, String fileName, boolean removeTemp) {
		foldRNA(seq.getName(), seq.getSequenceRNA(), fileName, removeTemp);
	}

	/**
	 * Remove duplicates results, keep only first result
	 * 
	 * @param fileName
	 */
	public static int selectFirstResult(String fileName) {

		String[][] array = TabDelimitedTableReader.read(PATH_DATA + fileName);
		System.out.println("Has " + array.length + " rows instead of " + array[0][0]);
		int finalLength = Integer.parseInt(array[0][0]) + 1;

		String[][] newArray = new String[finalLength][array[0].length];
		for (int i = 0; i < newArray.length; i++) {
			for (int j = 0; j < newArray[0].length; j++) {
				newArray[i][j] = array[i][j];
			}
		}

		TabDelimitedTableReader.save(newArray, PATH_DATA + fileName);
		return finalLength;
	}

	/**
	 * Remove duplicates results, keep only first result
	 * 
	 * @param fileName
	 * @param finalLength number of row to extract
	 */
	public static void selectFirstResult(String fileName, int finalLength) {

		String[][] array = TabDelimitedTableReader.read(PATH_DATA + fileName);
		System.out.println("Has " + array.length + " rows instead of " + array[0][0]);

		String[][] newArray = new String[finalLength][array[0].length];
		for (int i = 0; i < newArray.length; i++) {
			for (int j = 0; j < newArray[0].length; j++) {
				newArray[i][j] = array[i][j];
			}
		}

		TabDelimitedTableReader.save(newArray, PATH_DATA + fileName);
	}

	/**
	 * From a bunch of stochast folding, generate a list of Free Energies
	 * 
	 * @param fileName name of the file to read
	 * @return
	 */
	public static double[] calcEnergy(String fileName) {
		String execProcess = ct_energy + " " + (PATH_DATA + fileName) + "\"";
		try {
			String out = CMD.runProcess(execProcess, false);
			String[] freeEnergiesString = out.split("\n");
			System.out.println("nb of folding: " + freeEnergiesString.length);
			double[] freeEnergies = new double[freeEnergiesString.length];
			for (int i = 0; i < freeEnergies.length; i++) {
				try {
					double value = Double.parseDouble(freeEnergiesString[i]);
					freeEnergies[i] = value;
				} catch (Exception e1) {
					freeEnergies[i] = OmicsData.MISSING_VALUE;
				}
			}
			System.out.println("Mean free energies: " + VectorUtils.mean(freeEnergies));
			System.out.println("Stat Dev: " + VectorUtils.deviation(freeEnergies));
			return freeEnergies;
		} catch (IOException e) {
			e.printStackTrace();
			return new double[0];
		}
	}

	/**
	 * Convert a .ct file and .plot file into an .ann file<br>
	 * The output of plot2ann script is save into a .ann file
	 * 
	 * @param fileNameCT
	 * @param fileNamePlot
	 * @param fileNameAnn  Name of the annotation file to save
	 * @return
	 */
	public static double[][] plot2Ann(String fileNameCT, String fileNamePlot, String fileNameAnn) {
		String execProcess = plot2ann + " \"" + (PATH_DATA + fileNamePlot) + "\" \"" + (PATH_DATA + fileNameCT) + "\"";
		try {
			String out = CMD.runProcess(execProcess, false);
			String[] annotString = out.split("\n");
			System.out.println("nb of folding: " + annotString.length);
			double[][] annot = new double[annotString.length][annotString[0].split("\t").length];
			for (int i = 0; i < annot.length; i++) {
				for (int j = 0; j < annot[0].length; j++) {
					try {
						double value = Double.parseDouble(annotString[i].split("\t")[j]);
						annot[i][j] = value;
					} catch (Exception e1) {
						annot[i][j] = OmicsData.MISSING_VALUE;
					}
				}
			}
			Object[][] obj = new Object[annot.length][annot[0].length];
			for (int i = 0; i < annot.length; i++) {
				for (int j = 0; j < annot[0].length; j++) {
					obj[i][j] = annot[i][j];
				}
			}
			TabDelimitedTableReader.save(obj, PATH_DATA + fileNameAnn);
			return annot;
		} catch (IOException e) {
			e.printStackTrace();
			return new double[0][0];
		}
	}

	/**
	 * Run sir_graph software on a particular file
	 * 
	 * @param fileName
	 */
	public static void plotFolding(String fileName, int width, String prefix) {
		// String execProcess = sir_graph+" \""+(PATH_DATA+fileName)+"\"";
		String output = PATH_DATA + prefix + FileUtils.removeExtension(fileName) + ".png";

		String[][] array = TabDelimitedTableReader.read(PATH_DATA + fileName);
		String energy = array[0][1];
		String name = array[0][2];
		String title = "(" + name + ") " + energy;
		String execProcess = sir_graph + " -png " + width + " -t \"" + title + "\" -o \"" + output + "\" \""
				+ (PATH_DATA + fileName) + "\"";
		// String execProcess = sir_graph+" -png "+width+" -prob
		// \""+FileUtils.removeExtension(fileName)+".ann\" -o \""+output+"\"
		// \""+(PATH_DATA+fileName)+"\"";
		try {

			String out = CMD.runProcess(execProcess, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 */
	public static double hybridRNA(String seq1, String name1, String seq2, String name2, boolean removeTemp) {
		try {
			String fileName = name1 + "-" + name2;
			File file1 = new File(Database.getTEMP_PATH() + name1 + ".seq");
			File file2 = new File(Database.getTEMP_PATH() + name2 + ".seq");
			FileUtils.saveText(seq1, file1.getAbsolutePath());
			FileUtils.saveText(seq2, file2.getAbsolutePath());

			File path = new File(PATH_DATA);
			path.mkdir();

			String execProcess = hybrid_min + " -o \"" + (PATH_DATA + fileName) + "\" \"" + file1.getAbsolutePath()
					+ "\" \"" + file2.getAbsolutePath() + "\"";
			String out = CMD.runProcess(execProcess, false);
			/*
			 * Get energy
			 */
			File file3 = new File(PATH_DATA + fileName + ".DG");
			String[][] energies = TabDelimitedTableReader.read(file3);
			double energy = 0;
			try {
				energy = Double.parseDouble(energies[1][1]);
			} catch (Exception e) {

			}
			// plotFolding(fileName+".ct");
			// System.out.println(energy);
			if (removeTemp) {
				file1.delete();
				file2.delete();
				file3.delete();
				File file = new File(PATH_DATA + fileName + ".PLOT");
				file.delete();
				file = new File(PATH_DATA + fileName + ".ANN");
				file.delete();
				file = new File(PATH_DATA + fileName + ".RUN");
				file.delete();
				file = new File(PATH_DATA + fileName + ".CT");
				file.delete();
				file = new File(PATH_DATA + fileName + ".DG");
				file.delete();
			}
			return energy;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}

	}

}
