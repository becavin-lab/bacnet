package bacnet.scripts.core.normalization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.core.Rscript;
import bacnet.utils.FileUtils;

public class GRNormalization {

	/**
	 * Normalize the Tiling contained in the Experiment <br>
	 * Run one thread for each bioCondition<br>
	 * The number of thread is limited by the number of processor<br>
	 * <br>
	 * 
	 * It runs DNA reference normalization (Hubert et al.), it can uses also an
	 * additional reference data for normalization (indicates by boolean
	 * normalization).
	 * 
	 * 
	 * @param exp list of all BioCondition to check
	 */
	public static void norm(Experiment exp) {
		TreeSet<String> listdata = new TreeSet<String>();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		int i = 0;
		for (BioCondition bioCondTemp : exp.getBioConds().values()) {
			final BioCondition bioCond = bioCondTemp;
			// create temp folder
			final String tempPath = Database.getTEMP_PATH() + bioCond.getName();
			File path = new File(tempPath);
			path.mkdir();
			// run normalization
			if (bioCond.getTilings().size() != 0 && !bioCond.getName().equals("EGDe_DNA")
					&& !bioCond.getName().equals("EGDe_37C_Mean") && !bioCond.getName().equals("EGDe_37C_Deviation")) {
				listdata.add(bioCond.getName());
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							boolean normalization = true;
							if (bioCond.getName().equals(OmicsData.GENERAL_WT_DATA)
									|| bioCond.getName().equals(OmicsData.GENERAL_WT_NAME))
								normalization = false;

							String finalPath = OmicsData.PATH_TILING_NORM + File.separator;
							String tempFileName = bioCond.getName();

							String rScript = Database.getTEMP_PATH() + bioCond.getName() + File.separator + tempFileName
									+ "-script-Tiling.R";
							String listFilesPath = Database.getTEMP_PATH() + bioCond.getName() + File.separator
									+ bioCond.getName() + "-filecats-Tiling.txt";
							String tempDir = Database.getTEMP_PATH() + bioCond.getName() + File.separator;
							createFilecatsTable(bioCond, listFilesPath, normalization);
							createRnormScript(listFilesPath, finalPath, tempFileName, tempDir, rScript, normalization);
							// System.out.println(bioCond.getName());
							Rscript.run(rScript);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				i++;
				executor.execute(runnable);
			}
		}
		System.err.println("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
		System.err.println("dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
		System.err.println("d");
		System.err.println(
				"               Number of threads run: " + i + "  expected numb of data " + (listdata.size() * 2));
		System.err.println("d");
		System.err.println("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
		System.err.println("dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");

	}

	/**
	 * Create the R script to run for Tiling normalisation
	 * 
	 * @param listFilesPath list of Data to normalize
	 * @param finalPath     where to put data after normalization
	 * @param tempFileName  temp data names
	 * @param tempDir       where the temp folder is
	 * @param rScript       name of the rScript
	 * @param normalization indicates if we use a reference data for normalization
	 *                      or not
	 */
	private static void createRnormScript(String listFilesPath, String finalPath, String tempFileName, String tempDir,
			String rScript, boolean normalization) {

		// System.out.println("Create R script for normalization");

		String text = "# path for finding CEL data\n";
		text += "celDir = \"" + OmicsData.PATH_TILING_RAW + "\"\n";
		text += "# path where to save GR file\n";
		text += "grDir = \"" + finalPath + "\"\n";
		text += "# path where all scripts are\n";
		text += "scriptDir = \"" + Rscript.getRscriptDirectory() + "\"\n";
		text += "# path where the annotation file is\n";
		text += "probeRDA = paste(scriptDir,\"probeAnnoAll.rda\",sep=\"\")\n";
		text += "# path of the table containing the list of files to normalize\n";
		text += "filecatsDir = \"" + listFilesPath + "\"\n";

		text += "\n# temp file\n";
		text += "tempDir = \"" + tempDir + "\"\n";
		text += "tempCELFile = paste(tempDir,\"" + tempFileName + "-CEL.rda\",sep=\"\")\n";
		text += "tempNormFile = paste(tempDir,\"" + tempFileName + "-Norm.rda\",sep=\"\")\n";
		text += "tempStrandFile = paste(tempDir,\"" + tempFileName + "\",sep=\"\")\n";

		text += "\n# run the script\n";
		String function = "TilingNormalization";
		// if(normalization==false) function = "NoTilingNormalization";
		text += "source(paste(scriptDir,\"" + function + ".R\",sep=\"\"))\n";
		text += function + "(filecatsDir,celDir,grDir,probeRDA,scriptDir,tempCELFile,tempNormFile,tempStrandFile)\n";

		text = text.replaceAll("\\\\", "/");
		// System.out.println(text);

		FileUtils.saveText(text, rScript);
	}

	/**
	 * Create filecats = list of data in a table, used by the R script to run Tiling
	 * normalization
	 * 
	 * @param bioCond       to normalized
	 * @param fileName      name of the filecats file
	 * @param normalization indicates if we use a reference data for normalization
	 *                      or not
	 */
	public static void createFilecatsTable(BioCondition bioCond, String fileName, boolean normalization) {
		ArrayList<String> rawData = bioCond.getTilings().get(0).getRawDatas();
		/*
		 * If we don't have 3 technical replicates, we create more by copying last
		 * RawData
		 */
		ArrayList<String> rawDataTemp = new ArrayList<String>();
		for (String rawDataName : rawData)
			rawDataTemp.add(rawDataName);
		if (rawDataTemp.size() < 3) {
			String rawDataName = rawDataTemp.get(rawDataTemp.size() - 1);
			while (rawDataTemp.size() != 3) {
				rawDataTemp.add(rawDataName);
			}
		}

		/*
		 * Create filecats Table and save it
		 */
		String[] finalColumn1 = new String[0];
		String[] finalColumn2 = new String[0];
		if (normalization) {
			BioCondition bioCondRef = BioCondition.getBioCondition(bioCond.getComparisons().get(0));
			ArrayList<String> rawDataWT = bioCondRef.getTilings().get(0).getRawDatas();
			String[] col1 = { "col1", "T_EGDe_DNA_W1.CEL", "T_EGDe_DNA_W2.CEL", "T_EGDe_DNA_W3.CEL", rawDataTemp.get(0),
					rawDataTemp.get(1), rawDataTemp.get(2), rawDataWT.get(0), rawDataWT.get(1), rawDataWT.get(2) };
			String[] col2 = { "col2", "ADN", "ADN", "ADN", bioCond.getName(), bioCond.getName(), bioCond.getName(),
					"WT", "WT", "WT" };
			finalColumn1 = col1;
			finalColumn2 = col2;
		} else {
			String[] colTemp = { "col1", "T_EGDe_DNA_W1.CEL", "T_EGDe_DNA_W2.CEL", "T_EGDe_DNA_W3.CEL",
					rawDataTemp.get(0), rawDataTemp.get(1), rawDataTemp.get(2) };
			finalColumn1 = colTemp;
			String[] colTemp2 = { "col2", "ADN", "ADN", "ADN", bioCond.getName(), bioCond.getName(),
					bioCond.getName() };
			finalColumn2 = colTemp2;
		}

		String[][] filecats = new String[finalColumn1.length][3];
		for (int i = 0; i < filecats.length; i++) {
			if (i != 0)
				filecats[i][0] = i + "";
			else
				filecats[i][0] = "";
			filecats[i][1] = finalColumn1[i];
			filecats[i][2] = finalColumn2[i];
		}
		TabDelimitedTableReader.save(filecats, fileName);
	}

	/**
	 * By copying GENERAL_WT_DATA create GENERAL_WT_NAME tiling data
	 */
	public static void createEdgeWT() {
		try {
			String input = OmicsData.PATH_TILING_NORM + OmicsData.GENERAL_WT_DATA + ".+" + Tiling.EXTENSION;
			String output = OmicsData.PATH_TILING_NORM + OmicsData.GENERAL_WT_NAME + ".+" + Tiling.EXTENSION;
			FileUtils.copy(input, output);
			input = OmicsData.PATH_TILING_NORM + OmicsData.GENERAL_WT_DATA + ".-" + Tiling.EXTENSION;
			output = OmicsData.PATH_TILING_NORM + OmicsData.GENERAL_WT_NAME + ".-" + Tiling.EXTENSION;
			FileUtils.copy(input, output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Check temp folder, and GeneExprComplete folder to see if some GeneExpression
	 * are not present in GeneExprComplete
	 * 
	 * @param exp list of all BioCondition to check
	 */
	public static void qualityControlNorm(Experiment exp) {
		ArrayList<Tiling> tilings = exp.getTilings();
		ArrayList<String> files = new ArrayList<String>();
		for (Tiling tiling : tilings) {
			files.add(tiling.getName());
		}
		File fileNorm = new File(OmicsData.PATH_TILING_NORM);
		TreeSet<String> found = new TreeSet<String>();
		for (String fileTemp : files) {
			for (String fileNormTemp : fileNorm.list()) {
				if (fileTemp.equals(fileNormTemp)) {

					found.add(fileTemp);
				}
			}
		}
		System.out.println(found.size() + " data normalized");
		for (String fileTemp : files) {
			if (!found.contains(fileTemp)) {
				System.out.println("Not created: " + fileTemp);
			}
		}

	}

}
