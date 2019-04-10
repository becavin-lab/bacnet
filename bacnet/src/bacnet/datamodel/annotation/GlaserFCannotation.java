package bacnet.datamodel.annotation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.reader.TabDelimitedTableReader;

public class GlaserFCannotation {

	public static String MANDINFC_MAP = Database.getInstance().getPath()
			+ "Analysis/Egd-e Annotation/GlaserFC_info.txt";

	/**
	 * Return the Glaser Functional categories description corresponding to a
	 * specific glaserFCID
	 * 
	 * @param glaserFCID
	 * @return
	 */
	public static String getGlaserFCDescription(String glaserFCID) {
		try {
			TreeMap<String, String> mandinFCMap = getGlaserFCMap();
			String result = mandinFCMap.get(glaserFCID);
			if (result != null)
				return result;
			else
				return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Return a map from glaserFCID to Glaser et al. 2001 Functional categories
	 * description
	 * 
	 * @return
	 * @throws IOException
	 */
	public static TreeMap<String, String> getGlaserFCMap() throws IOException {
		TreeMap<String, String> mandinFCMap = new TreeMap<>();
		String fileToOpen = MANDINFC_MAP;
		File file = new File(fileToOpen);
		if (file.exists()) {
			String[][] table = TabDelimitedTableReader.read(file);
			// ArrayUtils.displayMatrix("jh", table);
			for (int i = 1; i < table.length; i++) {
				mandinFCMap.put(table[i][0], table[i][1]);
			}
			System.out.println("Glaser FC annotation read");
			return mandinFCMap;
		}
		return mandinFCMap;
	}

	/**
	 * Transform the result of getGlaserFCclassification() method into an
	 * ExpressionMatrix which can be load by PieChartView for display
	 * 
	 * @param classification
	 * @return
	 */
	public static ExpressionMatrix getPieChartData(HashMap<String, ArrayList<String>> classification) {
		ExpressionMatrix dataset = new ExpressionMatrix("GlaserFCType", classification.size());
		int totalSize = 0;
		for (String cog : classification.keySet()) {
			totalSize += classification.get(cog).size();
		}
		int i = 0;
		for (String cog : classification.keySet()) {
			dataset.getRowNames().put(cog, i);
			double ratio = (double) classification.get(cog).size() / (double) totalSize;
			System.out.println(ratio * 100);
			dataset.setValue(ratio * 100, i, 0);
			i++;
		}
		return dataset;
	}

}
