package bacnet.reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

import org.biojava3.core.sequence.io.BufferedReaderBytesRead;

public class TabDelimitedTableReader {

	/**
	 * Read file with a tab separator
	 * 
	 * @param file
	 * @return
	 */
	public static String[][] read(File file) {
		return read(file, "\t");
	}

	/**
	 * 
	 * Read a Tab-delimited file The number of cell in each column has not to be the
	 * same When a blank cell is found, it is replace by a void space.
	 * 
	 * @param file
	 * @param separator between the columns
	 * @return String array representation of the file
	 * @throws IOException
	 * @author Christophe B�cavin
	 */
	public static String[][] read(File file, String separator) {
		ArrayList<String> rowList = readList(file.getAbsolutePath(), true);
		int nbRow = rowList.size();
		int nbColumn = rowList.get(0).split(separator).length;
		String[][] ret = new String[nbRow][nbColumn];
		for (int i = 0; i < rowList.size(); i++) {
			int j = 0;
			while (j < rowList.get(i).split(separator).length) {
				ret[i][j] = rowList.get(i).split(separator)[j].trim();
				j++;
			}
			while (j < nbColumn) {
				ret[i][j] = "";
				j++;
			}
		}
		// System.out.println(Arrays.toString(ret));
		// ArrayUtils.displayMatrix("yo", ret);
		// System.out.println("Read table, found "+nbRow+" rows, "+nbColumn+"
		// columns.");

		return ret;
	}

	public static String[][] read(String fileName) {
		return read(new File(fileName));
	}

	/**
	 * Read a tabdelimited table and extract the line as an ArrayList of String
	 * 
	 * @param fileName
	 * @param index    column index
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> readList(String fileName, int index) {
		// System.out.println(fileName);
		ArrayList<String> list = new ArrayList<String>();
		try {
			File file = new File(fileName);
			FileInputStream fi = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fi);
			BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

			String line = br.readLine();
			// Read the lines and put them in ArrayList
			while (line != null) {
				list.add(line.split("\t")[index]);
				line = br.readLine();
			}
			br.close();
			isr.close();
			// If stream was created from File object then we need to close it
			if (fi != null) {
				fi.close();
			}

		} catch (Exception e) {
			System.err.println("Cannot read:" + fileName);
		}
		return list;
	}

	/**
	 * Read a tabdelimited table and extract the line as an ArrayList of String
	 * 
	 * @param fileName
	 * @param allLine  include the all line, or only the first column element of the
	 *                 line (if the file is a TabDelimited array)
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> readList(String fileName, boolean allLine) {
		// System.out.println(fileName);
		ArrayList<String> list = new ArrayList<String>();
		try {
			File file = new File(fileName);
			FileInputStream fi = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fi);
			BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

			String line = br.readLine();
			// Read the lines and put them in ArrayList
			while (line != null) {
				if (allLine) {
					list.add(line);
				} else {
					list.add(line.split("\t")[0]);
				}
				line = br.readLine();
			}
			br.close();
			isr.close();
			// If stream was created from File object then we need to close it
			if (fi != null) {
				fi.close();
			}

		} catch (Exception e) {
			System.err.println("Cannot read:" + fileName);
		}
		return list;
	}

	/**
	 * Read a tabdelimited table and extract the line as an ArrayList of
	 * String[]<br>
	 * Mainly used to load String[][] in SWT table graphic viewer
	 * 
	 * @param fileName
	 * @param allLine  include the all line, or only the first column element of the
	 *                 line (if the file is a TabDelimited array)
	 * @param not      used but indicates that the lines will be in a vector
	 *                 String[]
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String[]> readList(String fileName, boolean allLine, boolean vector) {
		// System.out.println(fileName);
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			File file = new File(fileName);
			FileInputStream fi = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fi);
			BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

			String line = br.readLine();
			// Read the lines and put them in ArrayList
			while (line != null) {
				String[] row = line.split("\t");
				list.add(row);
				line = br.readLine();
			}
			br.close();
			isr.close();
			// If stream was created from File object then we need to close it
			if (fi != null) {
				fi.close();
			}

		} catch (Exception e) {
			System.err.println("Cannot read:" + fileName);
		}
		return list;
	}

	/**
	 * Read a tabdelimited table and extract the first column as an ArrayList of
	 * String
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> readList(String fileName) {
		return readList(fileName, false);
	}

	/**
	 * Read a tabdelimited table and extract the line as an HashMap<first column,
	 * second column> of Strings
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, String> readHashMap(String fileName) {
		HashMap<String, String> hashmap = new HashMap<String, String>();
		try {
			File file = new File(fileName);
			FileInputStream fi = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fi);
			BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

			String line = br.readLine();
			// Read the lines and put them in ArrayList
			while (line != null) {
				hashmap.put(line.split("\t")[0], line.split("\t")[1]);
				line = br.readLine();
			}
			br.close();
			isr.close();
			// If stream was created from File object then we need to close it
			if (fi != null) {
				fi.close();
			}

		} catch (Exception e) {
			System.err.println("Cannot read:" + fileName);
		}
		return hashmap;
	}

	/**
	 * Save the array to the destination file parameter
	 *
	 * @param fileName the path of the destination file
	 * @param append   if true, then bytes will be written to the end of the file
	 *                 rather than the beginning
	 * @param head     if true, add the headers
	 */
	public static File save(Object[][] a, String fileName, boolean append, boolean addComment) {
		try {
			FileWriter fileW = new FileWriter(fileName, append);
			BufferedWriter bufferW = new BufferedWriter(fileW);

			// comment
			if (addComment) {
				bufferW.write("#" + " Generated: " + (new Date()).toString());
				bufferW.newLine();
				bufferW.newLine();
			}

			for (int i = 0; i < a.length; i++) {
				StringBuffer strBuf = new StringBuffer();
				for (int j = 0; j < a[i].length; j++) {
					String text = "";
					if (a[i][j] != null) {
						text = a[i][j].toString();
					}
					if (j == (a[i].length - 1))
						strBuf.append(text);
					else
						strBuf.append(text + "\t");

				}
				bufferW.write(strBuf.toString());
				bufferW.newLine();
			}
			bufferW.close();
			fileW.close();
			// System.out.println("Table saved in: "+fileName);
			return new File(fileName);

		} catch (IOException e) {
			System.out.println("Error when writing to the file : " + fileName + " - " + e);
			return null;
		}
	}

	public static File save(Object[][] a, String fileName) {
		return save(a, fileName, false, false);
	}

	/**
	 * Save the table in a text file containing an HTML table
	 * 
	 * @param a
	 * @param fileName
	 * @param title    webpage title
	 * @return
	 */
	public static File saveInHTML(Object[][] a, String fileName, String title) {
		try {
			FileWriter fileW = new FileWriter(fileName, false);
			BufferedWriter bufferW = new BufferedWriter(fileW);
			bufferW.write("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"ISO-8859-1\">\n<title>" + title
					+ "</title>\n</head>\n<body>\n");
			bufferW.write(title);
			bufferW.write("<table border=\"1\">\n");
			for (int i = 0; i < a.length; i++) {
				StringBuffer strBuf = new StringBuffer();
				strBuf.append("<tr>");
				for (int j = 0; j < a[i].length; j++) {
					if (i == 0) {
						strBuf.append("<th>" + a[i][j].toString() + "</th>");
					} else if (j == 0) {
						strBuf.append("<th>" + a[i][j] + "</th>");
					} else
						strBuf.append("<td>" + a[i][j].toString() + "</td>");
				}
				strBuf.append("</tr>\n");
				bufferW.write(strBuf.toString());
				bufferW.newLine();
			}
			bufferW.write("</table>\n</body>\n</html>\n");
			bufferW.close();
			fileW.close();
			// System.out.println("Table saved in: "+fileName);
			return new File(fileName);

		} catch (IOException e) {
			System.out.println("Error when writing to the file : " + fileName + " - " + e);
			return null;
		}
	}

	/**
	 * Return an HTML version of the table
	 * 
	 * @param a
	 * @param title
	 * @return
	 */
	public static String getInHTML(String[][] a, String title) {
		String html = "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"ISO-8859-1\">\n<title>" + title
				+ "</title>\n</head>\n<body>\n";
		html += "<table border=\"1\">\n";
		for (int i = 0; i < a.length; i++) {
			html += "<tr>";
			for (int j = 0; j < a[i].length; j++) {
				if (i == 0) {
					html += "<th>" + a[i][j].toString() + "</th>";
				} else if (j == 0) {
					html += "<th>" + a[i][j] + "</th>";
				} else
					html += "<td>" + a[i][j].toString() + "</td>";
			}
			html += "</tr>\n";
		}
		html += "</table>\n</body>\n</html>\n";
		return html;
	}

	/**
	 * Return an HTML version of the ArrayList<String>
	 * 
	 * @param list
	 * @param title
	 * @return
	 */
	public static String getInHTML(ArrayList<String> list, String title) {
		String html = "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"ISO-8859-1\">\n<title>" + title
				+ "</title>\n</head>\n<body>\n";
		for (String element : list) {
			html += element + "\n<br>";
		}
		html += "</body>\n</html>\n";
		return html;
	}

	/**
	 * Save the table in a text file containing an HTML table
	 * 
	 * @param a
	 * @param fileName
	 * @return
	 */
	public static String getHTMLVersion(Object[][] a) {
		String html = "<table border=\"1\">\n";
		for (int i = 0; i < a.length; i++) {
			html += "<tr>";
			for (int j = 0; j < a[i].length; j++) {
				if (i == 0) {
					html += "<th>" + a[i][j].toString() + "</th>";
				} else if (j == 0) {
					html += "<th>" + a[i][j] + "</th>";
				} else
					html += "<td>" + a[i][j].toString() + "</td>";
			}
			html += "</tr>\n";
		}
		html += "</table>";
		return html;
	}

	/**
	 * Save an ArrayList of String in a tabdelimited table
	 * 
	 * @param list     of String
	 * @param fileName
	 * @throws IOException
	 */
	public static void saveList(ArrayList<String> list, String fileName) {
		try {
			FileWriter fileW = new FileWriter(fileName, false);
			BufferedWriter bufferW = new BufferedWriter(fileW);
			for (String line : list) {
				bufferW.write(line);
				bufferW.newLine();
			}

			bufferW.close();
			fileW.close();
			// System.out.println("Table saved in: "+fileName);
		} catch (IOException e) {
			System.out.println("Error when writing to the file : " + fileName + " - " + e);
		}
	}

	/**
	 * Save a TreeSet of String in a Tabdelimited table
	 * 
	 * @param list
	 * @param fileName
	 * @throws IOException
	 */
	public static void saveTreeSet(TreeSet<String> list, String fileName) {
		try {
			FileWriter fileW = new FileWriter(fileName, false);
			BufferedWriter bufferW = new BufferedWriter(fileW);
			for (String line : list) {
				bufferW.write(line);
				bufferW.newLine();
			}

			bufferW.close();
			fileW.close();
			// System.out.println("Table saved in: "+fileName);
		} catch (IOException e) {
			System.out.println("Error when writing to the file : " + fileName + " - " + e);
		}
	}

	/**
	 * Save HashMap<String, String> to a 2 columns tab-delimited table
	 * 
	 * @param hashmap
	 * @param fileName
	 */
	public static void saveHashMap(HashMap<String, String> hashmap, String fileName) {
		try {
			FileWriter fileW = new FileWriter(fileName, false);
			BufferedWriter bufferW = new BufferedWriter(fileW);
			for (String key : hashmap.keySet()) {
				bufferW.write(key + "\t" + hashmap.get(key));
				bufferW.newLine();
			}

			bufferW.close();
			fileW.close();
			// System.out.println("Table saved in: "+fileName);
		} catch (IOException e) {
			System.out.println("Error when writing to the file : " + fileName + " - " + e);
		}
	}

}
