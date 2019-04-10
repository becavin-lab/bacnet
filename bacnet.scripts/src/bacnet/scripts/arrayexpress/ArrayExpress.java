package bacnet.scripts.arrayexpress;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

/**
 * Different methods to curate and parse the different data for Listeriomics
 * website <br>
 * <br>
 * 
 * ONLY METHODS BEFORE FINAL DATA PROCESSING ARE PRESENT HERE
 * 
 * 
 * @author UIBC
 *
 */
public class ArrayExpress {

	public static String ARRAYEXPRESS_PATH = Database.getInstance().getPath() + "/ArrayExpress/";

	// public static String DATA_TABLE = Database.getInstance().getPath() +
	// "ArrayExpress-Listeria
	// mono-15102014.txt";
	public static String DATA_TABLE = ARRAYEXPRESS_PATH + "ArrayExpress.txt";
	// public static String DATA_CURATED_PATH = Database.getInstance().getPath() +
	// "/Listeriomics/ArrayExpressCurated/";
	public static String DATA_CURATED_PATH = Database.getInstance().getPath() + "/ArrayExpressCurated/";

	public static void run() {
		/*
		 * Download data
		 */
		ArrayExpressDataImport.run();
		ArrayExpressTechnology.run();

		searchStudyMissing();
		/*
		 * createMatrices();
		 */

	}

	/**
	 * Create a new txt file containing all the accession data for every file to
	 * download
	 */
	public static void createArrayExpressTable() {
		System.out.println("Create ArrayExpress Table");
		ArrayList<String> arrayExpressTable = new ArrayList<>();
		String[][] bioConds = TabDelimitedTableReader.read(Database.getInstance().getBioConditionsArrayPath());
		ArrayList<String> accession = new ArrayList<>();

		System.out.println(ARRAYEXPRESS_PATH);

		if (!FileUtils.exists(ARRAYEXPRESS_PATH)) {
			System.out.println("Create folder: " + ARRAYEXPRESS_PATH);
			File file = new File(ARRAYEXPRESS_PATH);
			file.mkdir();
		}

		arrayExpressTable
				.add("ID" + "\t" + "Accession" + "\t" + "Title" + "\t" + "Type" + "\t" + "Organism" + "\t" + "Date");

		int j = 1;
		for (int i = 12; i < bioConds.length; i++) {
			if (!accession.contains(bioConds[i][4])) {
				arrayExpressTable.add(String.valueOf(j) + "\t" + bioConds[i][4] + "\t" + bioConds[i][18] + "\t"
						+ bioConds[i][1] + "\t" + bioConds[i][15] + "\t" + bioConds[i][3]);
				accession.add(bioConds[i][4]);
				j++;
			}
		}
		TabDelimitedTableReader.saveList(arrayExpressTable, ARRAYEXPRESS_PATH + "ArrayExpress.txt");
	}

	/**
	 * Create a txt file containing the different technologies used
	 */
	/**
	 * 
	 */
	public static void createTechnologiesTable() {
		ArrayList<String> technologiesTable = new ArrayList<>();
		String[][] bioConds = TabDelimitedTableReader.read(Database.getInstance().getBioConditionsArrayPath());

		technologiesTable.add("Type" + "\t" + "Dataset" + "\t" + "Techno");

		for (int i = 12; i < bioConds.length; i++) {
			technologiesTable.add(bioConds[i][1] + "\t" + bioConds[i][4] + "\t" + bioConds[i][6]);
		}
		TabDelimitedTableReader.saveList(technologiesTable, ARRAYEXPRESS_PATH + "Technologies.txt");
	}

	/**
	 * Create a txt file giving the name of each array technology
	 */
	public static void createArrayTable() {
		File folder = new File(Database.getInstance().getPath() + File.separator + "ArrayExpressTechnology");
		File[] array_list = folder.listFiles();
		ArrayList<String> arrayTable = new ArrayList<>();
		arrayTable.add("Techno\tName");
		for (int i = 0; i < array_list.length; i++) {
			String array_name = array_list[i].getName();
			if (!array_name.equals("Table_resume.txt") && !array_name.startsWith(".DS")) {
				String path = array_list[i].getAbsolutePath() + File.separator + array_name + ".name.txt";
				System.out.println(path);

				String line = FileUtils.readText(path);
				System.out.println("line: " + line);
				String techno_name = line.split("\t")[1];
				arrayTable.add(array_name + "\t" + techno_name);
			}
		}
		TabDelimitedTableReader.saveList(arrayTable, ARRAYEXPRESS_PATH + "/Arrays.txt");
	}

	/**
	 * Use the BioConditions and the curated sdrf files from ArrayExpressRawData
	 * folders to create a Comparison table
	 */
	public static void createComparisonsTable() {
		String[][] bioConds = TabDelimitedTableReader.read(Database.getInstance().getBioConditionsArrayPath());
		File folder = new File(ArrayExpress.ARRAYEXPRESS_PATH);
		File[] array_list = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (!name.toLowerCase().endsWith(".txt") && !name.toLowerCase().startsWith("."));
			}
		});
		HashMap<String, ArrayList<String>> ref_to_biocondname = new HashMap<>();
		int column_ref = ArrayUtils.findColumn(bioConds, "ArrayExpressId");
		for (int i = 12; i < bioConds.length; i++) {
			if (ref_to_biocondname.containsKey(bioConds[i][column_ref])) {
				ArrayList<String> list_biocond = ref_to_biocondname.get(bioConds[i][column_ref]);
				list_biocond.add(bioConds[i][0]);
				ref_to_biocondname.put(bioConds[i][column_ref], list_biocond);
			} else {
				ArrayList<String> list_biocond = new ArrayList<>();
				list_biocond.add(bioConds[i][0]);
				ref_to_biocondname.put(bioConds[i][column_ref], list_biocond);
			}
		}

		ArrayList<String> list_comparisons = new ArrayList<>();
		list_comparisons.add("BioCondName\tRefBioCondName\tFileName\tArrayExpressId");
		String bioCond1 = new String();
		String bioCond2 = new String();
		String file_name = new String();
		ArrayList<String> list_biocond = new ArrayList<>();
		for (int j = 0; j < array_list.length; j++) {
			list_biocond = ref_to_biocondname.get(array_list[j].getName());
			HashMap<String, ArrayList<String>> biocondMap = new HashMap<>();
			HashMap<String, ArrayList<String>> gsmIDMap = new HashMap<>();
			HashMap<String, String> listcomparaisons_data = new HashMap<>();
			String[][] curatedSDRF = TabDelimitedTableReader
					.read(array_list[j] + File.separator + array_list[j].getName() + "_curated.sdrf.txt");
			int replicata_column = ArrayUtils.findColumn(curatedSDRF, "REPLICATA");
			int biocond_column = ArrayUtils.findColumn(curatedSDRF, "SAMPLE_NAME");
			int array_data_file = ArrayUtils.findColumn(curatedSDRF, "DERIVED_ARRAY_FILE");
			System.err.println(array_list[j].getName());

			for (int k = 1; k < curatedSDRF.length; k++) {
				String replicata = curatedSDRF[k][replicata_column];
				String arrayName = curatedSDRF[k][array_data_file];
				String bioCondName = curatedSDRF[k][biocond_column];

				if (biocondMap.containsKey(replicata)) {
					if (!gsmIDMap.get(replicata).contains(arrayName)) {
						ArrayList<String> list_gsm = gsmIDMap.get(replicata);
						list_gsm.add(arrayName);
						gsmIDMap.put(replicata, list_gsm);
					}
					ArrayList<String> list = biocondMap.get(replicata);
					list.add(bioCondName);
					biocondMap.put(replicata, list);
				} else {
					ArrayList<String> list = new ArrayList<>();
					ArrayList<String> list_gsm = new ArrayList<>();
					list.add(bioCondName);
					list_gsm.add(arrayName);
					biocondMap.put(replicata, list);
					gsmIDMap.put(replicata, list_gsm);
				}
				if (!curatedSDRF[k][replicata_column + 1].equals("")
						&& !curatedSDRF[k][replicata_column + 1].equals(null)) {
					listcomparaisons_data.put(replicata, curatedSDRF[k][replicata_column + 1]);
				}
			}
			for (String data : listcomparaisons_data.keySet()) {
				for (int i = 0; i < gsmIDMap.get(data).size(); i++) {
					if (list_biocond.size() > 2) {
						bioCond1 = biocondMap.get(data).get(i);
						bioCond2 = biocondMap.get(listcomparaisons_data.get(data)).get(i);
					} else {
						bioCond1 = list_biocond.get(0);
						bioCond2 = list_biocond.get(1);
					}
					file_name = gsmIDMap.get(data).get(i);
					list_comparisons
							.add(bioCond1 + "\t" + bioCond2 + "\t" + file_name + "\t" + array_list[j].getName());
				}
			}
		}
		TabDelimitedTableReader.saveList(list_comparisons, Database.getInstance().getExperimentComparisonTablePath());
	}

	/**
	 * Method to remove the unsued lines in the data files for BUGS and MEXP
	 * technologies If those lines remain present it causes failures to create the
	 * curated tables with the global method
	 */
	public static void cleanDataTable() {
		String[][] comparisons = TabDelimitedTableReader
				.read(Database.getInstance().getExperimentComparisonTablePath());
		for (int i = 0; i < comparisons.length; i++) {
			String arrayExpressId = comparisons[i][ArrayUtils.findColumn(comparisons, "ArrayExpressId")];
			if (arrayExpressId.contains("BUGS") || arrayExpressId.contains("MEXP")) {
				String file_data = comparisons[i][ArrayUtils.findColumn(comparisons, "FileName")];
				String[][] data_table = TabDelimitedTableReader
						.read(ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + file_data);
				int index_row = 0;
				while (index_row < data_table.length && !data_table[index_row][0].contains("Reporter")) {
					data_table = ArrayUtils.deleteRow(data_table, index_row);
					index_row++;
				}
				ArrayList<String> new_data_table = new ArrayList<>();
				for (int j = 0; j < data_table.length; j++) {
					String row = new String();
					for (int k = 0; k < data_table[j].length; k++) {
						row += data_table[j][k] + "\t";
					}
					new_data_table.add(row.substring(0, row.length() - 1));
				}
				TabDelimitedTableReader.saveList(new_data_table,
						ArrayExpressDataUtils.PATH + arrayExpressId + File.separator + file_data);
			}
		}
	}

	/**
	 * Go through the adfTable files and create the curated files for the studies
	 * based on those technologies
	 */
//    public static void createCuratedTable() {
//        if (!FileUtils.exists(DATA_CURATED_PATH)) {
//            System.out.println("Create folder: " + DATA_CURATED_PATH);
//            File file = new File(DATA_CURATED_PATH);
//            file.mkdir();
//        }
//        String[][] technologies = TabDelimitedTableReader.read(ARRAYEXPRESS_PATH + "/ArrayExpressTechno.txt");
//        File folder = new File(ArrayExpressTechnology.PATH);
//        File[] technoID = folder.listFiles();
//        ArrayList<String> list_technoID = new ArrayList<>();
//        for (File techno : technoID) {
//            list_technoID.add(techno.getName());
//        }
//        for (int i = 0; i < technologies.length; i++) {
//            if (list_technoID.contains(technologies[i][0])) {
//                String[] list_arrayExpressId =
//                        technologies[i][ArrayUtils.findColumn(technologies, "Study based on this technology")]
//                                .split(";");
//                for (int j = 0; j < list_arrayExpressId.length; j++) {
//                    String arrayExpressId = list_arrayExpressId[j];
//                    System.err.println(arrayExpressId);
//                    YersiniaExtractProbes.run(technologies[i][0], arrayExpressId);
//                }
//            } else {
//                System.err.println("Curated table not created for studies based on: " + technologies[i][0]);
//            }
//        }
//        removeColumn(ArrayExpress.DATA_CURATED_PATH
//                + "7hPestisStatMutant_yitR_Affymetrix_2010_KIM6 vs 7hPestisMidLogMutant_yitR_Affymetrix_2010_KIM6.txt",
//                "ABS_CALL");
//        removeColumn(ArrayExpress.DATA_CURATED_PATH
//                + "7hPestisFleaBiofilmMutant_yitR_Affymetrix_2010_KIM6 vs 7hPestisFlowcellMutant_yitR_Affymetrix_2010_KIM6.txt",
//                "ABS_CALL");
//    }

	/**
	 * Copy the transcriptomics curated table in the NormData>ExprMatrix folder
	 * 
	 * @throws IOException
	 */
	public static void copyTranscriptomicsTable() throws IOException {
		File folder = new File(ArrayExpress.DATA_CURATED_PATH);
		File[] list = folder.listFiles();
		String folder_end = OmicsData.PATH_EXPR_NORM;
		for (int i = 0; i < list.length; i++) {
			if (list[i].getName().endsWith(".txt")) {
				FileUtils.copy(list[i].getAbsolutePath(), folder_end + list[i].getName());
				FileUtils.copy(list[i].getAbsolutePath(), OmicsData.PATH_EXPR_RAW + list[i].getName());
			}
		}

	}

	public static void removeColumn(String path, String name) {
		String[][] table = TabDelimitedTableReader.read(path);
		int i = 0;
		while (i < table[0].length && !table[0][i].equals(name)) {
			i++;
		}
		table = ArrayUtils.deleteColumn(table, i);
		TabDelimitedTableReader.save(table, path);
	}

	/**
	 * Search in ArrayExpress list of data which BioCondition is not being created
	 */
	private static void searchStudyMissing() {
		String[][] arrayData = TabDelimitedTableReader.read(ArrayExpress.DATA_TABLE);
		String[][] bioConds = TabDelimitedTableReader.read(Database.getInstance().getExperimentComparisonTablePath());
		for (int k = 1; k < arrayData.length; k++) {
			String arrayExpressIDData = arrayData[k][1];
			String studyName = arrayData[k][2];
			boolean found = false;
			for (int i = 1; i < bioConds.length; i++) {
				String arrayExpressIdBioCond = bioConds[i][ArrayUtils.findColumn(bioConds, "ArrayExpressId")];
				if (arrayExpressIDData.equals(arrayExpressIdBioCond)) {
					found = true;
				}
			}
			if (!found) {
				System.out.println("Did not found: " + arrayExpressIDData + "  " + studyName);
			}
		}
	}

	/**
	 * Read a potential ExpressionMatrix, remove all duplicate rows by calculating
	 * mean when 2 or more rows have same name. WARNING: No annotation information
	 * is allowed, only real values
	 * 
	 * @param dataPath
	 * @param comparison true if it is an ExpressionMatrix for a comparison, if no
	 *                   LOGFC column are found in a row, it will delete the row
	 *                   (gain speed and space)
	 * @return
	 */
	public static ExpressionMatrix curateMatrix(String dataPath, boolean comparison) {
		String[][] array = TabDelimitedTableReader.read(dataPath);
		ExpressionMatrix matrix = new ExpressionMatrix();
		/*
		 * Create list of RowName, values, headers, et initialize matrix
		 */
		TreeSet<String> rowNamesSet = new TreeSet<>();
		for (int i = 1; i < array.length; i++) {
			rowNamesSet.add(array[i][0]);
		}
		TreeMap<String, Integer> rowNames = new TreeMap<>();
		int k = 0;
		for (String rowName : rowNamesSet) {
			rowNames.put(rowName, k);
			k++;
		}
		matrix.setRowNames(rowNames);
		double[][] values = new double[rowNames.size()][array[0].length - 1];
		matrix.setValues(values);
		ArrayList<String> headers = new ArrayList<>();
		for (int j = 1; j < array[0].length; j++) {
			headers.add(array[0][j]);
		}
		matrix.setHeaders(headers);

		/*
		 * go through array and remove duplicates by calculating mean of each column for
		 * every duplicate rowname
		 */
		ArrayList<String> keepRowName = new ArrayList<>();
		for (String rowName : matrix.getRowNames().keySet()) {
			for (int j = 1; j < array[0].length; j++) {
				String header = array[0][j];
				double sum = 0;
				double count = 0;
				for (int i = 1; i < array.length; i++) {
					if (array[i][0].equals(rowName)) {
						if (!array[i][j].equals("") && !array[i][j].equals("null")) { // Beware of Missing values
							sum += Double.parseDouble(array[i][j]);
							count++;
						}
					}
				}
				sum = sum / count;
				if (count == 0)
					sum = OmicsData.MISSING_VALUE;
				matrix.setValue(sum, rowName, header);
				if (comparison && header.contains("LOGFC")) { // if we have no results in LOGFC column we remove the row
					if (count != 0) {
						keepRowName.add(rowName);
					}
				} else {
					keepRowName.add(rowName);
				}
			}
		}
		matrix = matrix.getSubMatrixRow(keepRowName);
		return matrix;
	}

}
