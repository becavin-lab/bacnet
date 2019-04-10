package bacnet.scripts.arrayexpress;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import bacnet.Database;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;

/**
 * List of methods to determine the different technology used
 * 
 * @author UIBC
 *
 */
public class ArrayExpressTechnology {

	public static String FTP_URL = "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/array/";
	public static String HTML_URL_ARRAY = "http://www.ebi.ac.uk/arrayexpress/arrays/";
	public static String HTML_URL_EXP = "http://www.ebi.ac.uk/arrayexpress/experiments/";
	public static String PATH = Database.getInstance().getPath() + File.separator + "ArrayExpressTechnology"
			+ File.separator;
	public static String LIST_PATH = PATH + "ListTechno.txt";
	// public static String TABLE_PATH = Database.getInstance().getPath() +
	// "ArraysTechnology-16102014.txt";
	public static String TABLE_PATH = Database.getInstance().getPath() + File.separator + "ArrayExpress"
			+ File.separator + "Technologies.txt";

	/**
	 * Main method, where all other methods are ran
	 */
	public static void run() {
		downloadAllTechno();
		createTechnoTable();
		createHTMLPageSummary();
	}

	/**
	 * Given an ArrayDesgin ID return the full name of the microarray
	 * 
	 * @param arrayId
	 * @return
	 */
	public static String getTechnoName(String arrayId) {
		String[][] array = TabDelimitedTableReader.read(TABLE_PATH);
		for (int i = 0; i < array.length; i++) {
			if (array[i][1].equals(arrayId)) {
				return array[i][2];
			}
		}
		return "";
	}

	/**
	 * Go through all SDRF files and read the "Array Design REF" entry, save in
	 * ListTechno.txt
	 */
	public static TreeSet<String> getAlltechnology() {
		TreeSet<String> allTechno = new TreeSet<>();
		String[][] datasets = TabDelimitedTableReader.read(ArrayExpress.DATA_TABLE);
		for (int i = 1; i < datasets.length; i++) {
			String datasetId = datasets[i][1];
			String type = datasets[i][3];
			if (!type.equals("RNA-seq")) {
				String techno = ArrayExpressDataUtils.getArrayDesignRef(datasetId);
				if (!techno.equals("")) {
					allTechno.add(techno);
				} else {
					System.err.println("Did not found techno for: " + datasetId);
				}
			}
		}
		// TabDelimitedTableReader.saveTreeSet(allTechno, LIST_PATH);
		return allTechno;
	}

	/**
	 * Download all ADF files
	 */
	public static void downloadAllTechno() {
		for (String techno : getAlltechnology()) {
			downloadFile(techno, ".adf.txt", PATH + techno + ".adf.txt");
		}
	}

	/**
	 * Create a table with different info about Array Design: Name, number of data
	 * which use it
	 */
	public static void createTechnoTable() {

		// String[][] array = TabDelimitedTableReader.read("D:/Technology.txt");
		String[][] array = TabDelimitedTableReader.read(ArrayExpress.ARRAYEXPRESS_PATH + "/Technologies.txt");
		HashMap<String, TreeSet<String>> technoMap = new HashMap<String, TreeSet<String>>();
		HashMap<String, TreeSet<String>> typeMap = new HashMap<String, TreeSet<String>>();
		String[][] arrayTechnoName = TabDelimitedTableReader.read(ArrayExpress.ARRAYEXPRESS_PATH + "/Arrays.txt");

		for (int i = 1; i < array.length; i++) {
			String techno = array[i][2];
			String dataset = array[i][1];
			String type = array[i][0];
			if (technoMap.containsKey(techno)) {
				TreeSet<String> listData = technoMap.get(techno);
				listData.add(dataset);
				technoMap.put(techno, listData);
				TreeSet<String> listType = typeMap.get(techno);
				listType.add(type);
				typeMap.put(techno, listType);
			} else {
				TreeSet<String> listData = new TreeSet<String>();
				listData.add(dataset);
				technoMap.put(techno, listData);
				TreeSet<String> listType = new TreeSet<String>();
				listType.add(type);
				typeMap.put(techno, listType);
			}
		}

		ArrayList<String> result = new ArrayList<String>();
		result.add("Technology ID\tType of technology\tName\tNb study\tStudy based on this technology");
		for (String techno : technoMap.keySet()) {
			String dataName = "";
			for (int i = 1; i < arrayTechnoName.length; i++) {
				String technoTemp = arrayTechnoName[i][0];
				String name = arrayTechnoName[i][1];
				System.out.println(technoTemp);
				if (technoTemp.equals(techno)) {
					dataName = name;
				}
			}
			String data = "";
			for (String dataTemp : technoMap.get(techno))
				data += dataTemp + ";";
			String type = "";
			for (String dataTemp : typeMap.get(techno))
				type += dataTemp + ";";
			result.add(techno + "\t" + type + "\t" + dataName + "\t" + technoMap.get(techno).size() + "\t" + data);
		}
		// TabDelimitedTableReader.saveList(result, "D:/technoArray.excel");
		TabDelimitedTableReader.saveList(result, ArrayExpress.ARRAYEXPRESS_PATH + "/ArrayExpressTechno.txt");

		// ArrayList<String> idToName = new ArrayList<>();
		// String header = "ArrayID\tName\tNb study\tStudy based on this array";
		// idToName.add(header);
		// for(String techno : TabDelimitedTableReader.readList(LIST_PATH)){
		// String arrayDesignName = "";
		// String string = "";
		// String[][] adf = TabDelimitedTableReader.read(PATH+techno+".adf.txt");
		// if(adf[0][0].equals("Array Design Name")){
		// arrayDesignName = adf[0][1];
		// System.out.println(arrayDesignName);
		//
		// }
		//// for(int i=0;i<10;i++){
		////// if(adf[i][0].equals("Comment[Description]")){
		////// description = adf[i][1];
		////// System.out.println(arrayDesignName);
		////// }
		//// if(adf[i][0].equals("Printing Protocol")){
		//// description = adf[i][1];
		//// System.out.println(arrayDesignName);
		//// }
		//// }
		//
		// /**
		// * Find data using this technology
		// */
		// int count = 0;
		// String dataList = "";
		// String[][] datasets =
		// TabDelimitedTableReader.read(ArrayExpressListeriomics.DATA_TABLE);
		// for(int i =1;i<datasets.length;i++){
		// String datasetId = datasets[i][1];
		// String type = datasets[i][3];
		// if(!type.equals("RNA-seq")){
		// String technoTemp = ArrayExpressDataUtils.getArrayDesignRef(datasetId);
		// if(techno.equals(technoTemp)){
		// dataList+=datasetId+";";
		// count++;
		// }
		// }
		// }
		// idToName.add(techno+"\t"+arrayDesignName+"\t"+count+"\t"+dataList);
		// }
		// TabDelimitedTableReader.saveList(idToName, TABLE_PATH);
	}

	/**
	 * Read TABLE_PATH and create an HTML table from it to ease accession to
	 * ArrayExpress info
	 */
	public static void createHTMLPageSummary() {
		String[][] array = TabDelimitedTableReader.read(TABLE_PATH);
		for (int i = 1; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				String cell = array[i][j];
				if (array[0][j].equals("ArrayID")) {
					cell = "<a href=\"" + HTML_URL_ARRAY + cell + "/\">" + cell + "</a>";
				}
				if (array[0][j].equals("Data based on this array")) {
					String[] datas = cell.split(";");
					String newCell = "";
					for (String data : datas) {
						newCell += "<a href=\"" + HTML_URL_EXP + data + "/\">" + data + "</a>; ";
					}
					cell = newCell;
				}
				array[i][j] = cell;
			}
		}
		// TabDelimitedTableReader.saveInHTML(array, Database.getInstance().getPath() +
		// "ListArrays.html",
		// "Listeria Array Designs");
		TabDelimitedTableReader.saveInHTML(array,
				Database.getInstance().getPath() + File.separator + "ArrayExpress/ListArrays.html",
				Database.getInstance().getProjectName() + "Array_Designs");
	}

	/**
	 * Given an accession and an extension type:
	 * <li>check if the file does not already exist in the current folder
	 * <li>Download it if not
	 * <li>Print out a message if no file has been downloaded
	 * 
	 * @param accession
	 * @param extension
	 */
	public static void downloadFile(String accession, String extension, String fileName) {
		String id = accession.split("-")[1];
//        String extension = ".adf.txt";
//        String fileName = PATH + accession + extension;
		try {
			File file = new File(fileName);
			if (file.exists()) {
				// System.out.println("Already exist: "+fileName);
			} else {
				URL idfURL = new URL(FTP_URL + "/" + id + "/" + accession + "/" + accession + extension);
				System.out.println("Download: " + idfURL + " to " + fileName);
				FileUtils.copyURLToFile(idfURL, new File(fileName));
			}
		} catch (Exception e) {
			System.out.println(accession + extension);
		}
		File file = new File(fileName);
		if (!file.exists()) {
			System.out.println("File missing: " + fileName);
		}
	}

	/**
	 * From array probe sequence try to retrieve the gene name
	 */
	public static void findProbes() {
		Genome genome = Genome.loadEgdeGenome();
		String[][] array = TabDelimitedTableReader.read(ArrayExpressTechnology.PATH + "A-GEOD-17774.adfTable.txt");
		for (int i = 1; i < array.length; i++) {
			String sequence = array[i][2];
			for (Gene gene : genome.getChromosomes().get(0).getGenes().values()) {
				String geneSeq = gene.getSequence();
				if (geneSeq.contains(sequence)) {
					array[i][1] = gene.getName();
				}
			}

		}
		TabDelimitedTableReader.save(array, ArrayExpressTechnology.PATH + "A-GEOD-17774.adfTableModif.txt");
	}

	public static void downloadAllFilesTechno() {
		String[][] technologies = TabDelimitedTableReader
				.read(Database.getInstance().getPath() + "/ArrayExpress/Technologies.txt");
		ArrayList<String> list_techno = new ArrayList<>();
		for (int i = 1; i < technologies.length; i++) {
			if (!list_techno.contains(technologies[i][2]) && !technologies[i][2].equals("N/A")) {
				list_techno.add(technologies[i][2]);
			}
		}
		for (String techno : list_techno) {
			File pathFile = new File(ArrayExpressTechnology.PATH + techno + File.separator);
			if (!pathFile.exists()) {
				System.out.println("Create folder: " + pathFile.getAbsolutePath());
				pathFile.mkdir();
				downloadFile(techno, ".adf.txt", pathFile.getAbsolutePath() + File.separator + techno + ".adf.txt");
				downloadFile(techno, ".reporters.txt",
						pathFile.getAbsolutePath() + File.separator + techno + ".reporters.txt");
				downloadFile(techno, "_comments.txt",
						pathFile.getAbsolutePath() + File.separator + techno + "_comments.txt");
				downloadFile(techno, ".compositesequences.txt",
						pathFile.getAbsolutePath() + File.separator + techno + ".compositesequences.txt");
			}
		}
	}

	public static void adfCleaning() {
		String[][] technologies = TabDelimitedTableReader.read(ArrayExpress.ARRAYEXPRESS_PATH + "/Technologies.txt");
		ArrayList<String> list_techno = new ArrayList<>();
		for (int i = 1; i < technologies.length; i++) {
			if (!list_techno.contains(technologies[i][2]) && !technologies[i][2].equals("N/A")) {
				list_techno.add(technologies[i][2]);
			}
		}
		for (String techno : list_techno) {
			String fileName = ArrayExpressTechnology.PATH + techno + "/" + techno + ".adf.txt";
			ArrayList<String> lines = TabDelimitedTableReader.readList(fileName, true);
			ArrayList<String> newADF = new ArrayList<>();
			boolean found = false;
			for (String line : lines) {
				if (line.startsWith("Array Design Name")) {
					bacnet.utils.FileUtils.saveText(line,
							ArrayExpressTechnology.PATH + techno + "/" + techno + ".name.txt");
				}
				if (line.startsWith("Reporter Name"))
					found = true;
				if (found)
					newADF.add(line);
			}
			TabDelimitedTableReader.saveList(newADF, ArrayExpressTechnology.PATH + techno + "/" + techno + ".adf.txt");
		}
	}
}
