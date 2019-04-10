package bacnet.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Public class with general array manipulation utilities, not specific to
 * PROJECTNAME.
 * 
 * @author Christophe Becavin
 */

public class ArrayUtils {

	/**
	 * Find the index of a column, given its header
	 * 
	 * @param array     containing the column
	 * @param colHeader name of the column to find
	 * @return
	 */
	public static int findColumn(String[][] array, String colHeader) {
		String UTF8_BOM = "ϻ�";
		String colHeaderTemp = colHeader.toUpperCase();
		for (int j = 0; j < array[0].length; j++) {
			String colName = array[0][j].toUpperCase();
			if (colName.startsWith(UTF8_BOM)) {
				colName = colName.substring(3);
			}
			// System.out.println(colName);
			if (colName.equals(colHeaderTemp)) {
				return j;
			}
		}
		// System.err.println("Did not found column: "+colHeader);
		return -1;
	}

	/**
	 * Transform to a Vector an array
	 * 
	 * @param array
	 * @return
	 */
	public static double[] toVector(double[][] array) {
		double[] vector = new double[array.length * array[0].length];
		int k = 0;
		for (double[] values : array) {
			for (double value : values) {
				vector[k] = value;
				k++;
			}
		}
		return vector;
	}

	/**
	 * Return array Y=(x_ij ^ 2)
	 */
	public static double[][] square(double[][] array) {
		double[][] tempMatrix = new double[array.length][array[0].length];
		for (int i = 0; i < tempMatrix.length; i++) {
			for (int j = 0; j < tempMatrix[0].length; j++) {
				tempMatrix[i][j] = array[i][j];
			}
		}
		return tempMatrix;
	}

	/**
	 * Extract a Row from a Matrix
	 * 
	 * @param array a Matrix or double[][]
	 * @param index row index for extraction
	 * 
	 * @return a double[] containing the row
	 */
	public static String[] getRow(String[][] array, int index) {
		String[] vector = new String[array[0].length];
		for (int i = 0; i < array[0].length; i++) {
			vector[i] = array[index][i];
		}
		return vector;
	}

	public static double[] getRow(double[][] array, int index) {
		double[] vector = new double[array[0].length];
		for (int i = 0; i < array[0].length; i++) {
			vector[i] = array[index][i];
		}
		return vector;
	}

	public static int[] getRow(int[][] array, int index) {
		int[] vector = new int[array[0].length];
		for (int i = 0; i < array[0].length; i++) {
			vector[i] = array[index][i];
		}
		return vector;
	}

	public static double[][] addRow(double[][] array, double[] row) {
		if (row.length == array[0].length) {
			double[][] newArray = new double[array.length + 1][array[0].length];
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[i][j] = array[i][j];
				}
			}
			for (int j = 0; j < array[0].length; j++) {
				newArray[array.length][j] = row[j];
			}
			return newArray;
		} else {
			System.out.println("The row has not been added to the array, because it does not have the same size");
			return array;
		}
	}

	/**
	 * Add the input row in the String[][] array, at the postiion given by index
	 * integer
	 * 
	 * @param array
	 * @param row
	 * @param index
	 * @return
	 */
	public static String[][] addRow(String[][] array, String[] row, int index) {
		if (row.length == array[0].length) {
			String[][] newArray = new String[array.length + 1][array[0].length];
			for (int i = 0; i < index; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[i][j] = array[i][j];
				}
			}
			for (int j = 0; j < array[0].length; j++) {
				newArray[index][j] = row[j];
			}
			for (int i = index; i < array.length; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[i + 1][j] = array[i][j];
				}
			}
			return newArray;
		} else {
			System.out.println("The row has not been added to the array, because it does not have the same size");
			return array;
		}
	}

	public static double[][] deleteRow(double[][] array, int index) {
		if (index < array.length) {
			double[][] newArray = new double[array.length - 1][array[0].length];
			int k = 0;
			for (int i = 0; i < index; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[k][j] = array[i][j];
				}
				k++;
			}
			for (int i = index + 1; i < array.length; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[k][j] = array[i][j];
				}
				k++;
			}
			return newArray;
		} else {
			System.out.println("The row has not been added to the array, because it does not have the same size");
			return array;
		}
	}

	public static String[][] deleteRow(String[][] array, int index) {
		if (index < array.length) {
			String[][] newArray = new String[array.length - 1][array[0].length];
			int k = 0;
			for (int i = 0; i < index; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[k][j] = array[i][j];
				}
				k++;
			}
			for (int i = index + 1; i < array.length; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[k][j] = array[i][j];
				}
				k++;
			}
			return newArray;
		} else {
			System.out.println("The row has not been added to the array, because it does not have the same size");
			return array;
		}
	}

	/**
	 * Extract a Column from a Matrix
	 * 
	 * @param array a Matrix or double[][]
	 * @param index column index for extraction
	 * 
	 * @return a double[] containing the column
	 */
	public static double[] getColumn(double[][] array, int index) {
		double[] vector = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			vector[i] = array[i][index];
		}
		return vector;
	}

	public static String[] getColumn(String[][] array, int index) {
		String[] vector = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			vector[i] = array[i][index];
		}
		return vector;
	}

	public static double[][] addColumn(double[][] array, double[] column) {
		if (column.length == array.length) {
			double[][] newArray = new double[array.length][array[0].length + 1];
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[i][j] = array[i][j];
				}
			}
			for (int i = 0; i < array.length; i++) {
				newArray[i][array[0].length] = column[i];
			}
			return newArray;
		} else {
			System.out.println("The column has not been added to the array, because it does not have the same size");
			return array;
		}
	}

	public static String[][] addColumn(String[][] array, String[] column) {
		if (column.length == array.length) {
			String[][] newArray = new String[array.length][array[0].length + 1];
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[0].length; j++) {
					newArray[i][j] = array[i][j];
				}
			}
			for (int i = 0; i < array.length; i++) {
				newArray[i][array[0].length] = column[i];
			}
			return newArray;
		} else {
			System.out.println("The column has not been added to the array, because it does not have the same size");
			return array;
		}
	}

	public static double[][] deleteColumn(double[][] array, int index) {
		if (index < array[0].length) {
			double[][] newArray = new double[array.length][array[0].length - 1];
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < index; j++) {
					newArray[i][j] = array[i][j];
				}
				for (int j = index + 1; j < array[0].length; j++) {
					newArray[i][j - 1] = array[i][j];
				}
			}
			return newArray;
		} else {
			System.out.println("The column has not been added to the array, because it does not have the same size");
			return array;
		}
	}

	public static String[][] deleteColumn(String[][] array, int index) {
		if (index < array[0].length) {
			String[][] newArray = new String[array.length][array[0].length - 1];
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < index; j++) {
					newArray[i][j] = array[i][j];
				}
				for (int j = index + 1; j < array[0].length; j++) {
					newArray[i][j - 1] = array[i][j];
				}
			}

			return newArray;
		} else {
			System.out.println("The column has not been added to the array, because it does not have the same size");
			return array;
		}
	}

	/**
	 * Return an array which correspond to the row and colum
	 * 
	 * @param array
	 * @param listRowIndex
	 * @param listColumnIndex
	 * @return
	 */
	public static String[][] subArray(String[][] array, ArrayList<Integer> listRowIndex,
			ArrayList<Integer> listColumnIndex) {
		String[][] arrayTemp = new String[listRowIndex.size()][listColumnIndex.size()];
		for (int i = 0; i < listRowIndex.size(); i++) {
			for (int j = 0; j < listColumnIndex.size(); j++) {
				arrayTemp[i][j] = array[listRowIndex.get(i)][listColumnIndex.get(j)];
			}
		}
		return arrayTemp;
	}

	public static String[][] subArray(String[][] array, int begin, int end) {
		String[][] arrayTemp = new String[end - begin + 2][array[0].length - 1];
		int k = 0;
		int m = 0;
		for (int j = 1; j < array[0].length; j++) {
			arrayTemp[k][m] = array[0][j];
			m++;
		}
		k = 1;
		for (int i = begin; i < end + 1; i++) {
			m = 0;
			for (int j = 1; j < array[0].length; j++) {
				arrayTemp[k][m] = array[i][j];
				m++;
			}
			k++;
		}
		return arrayTemp;
	}

	/**
	 * Return a new Array of String which is the fusion of both array If header =
	 * true; the first row of array2 is conserved
	 * 
	 * @param array
	 * @param array2
	 * @param header
	 * @return
	 */
	public static String[][] fusion(String[][] array, String[][] array2, boolean header) {
		int nbRow = array.length + array2.length;
		if (!header)
			nbRow = array.length + array2.length - 1;
		String[][] arrayTemp = new String[nbRow][array[0].length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				arrayTemp[i][j] = array[i][j];
			}
		}
		int k = array.length;
		if (header) {
			for (int j = 0; j < array[0].length; j++) {
				arrayTemp[k][j] = array2[0][j];
			}
			k++;
		}
		for (int i = 1; i < array2.length; i++) {
			for (int j = 0; j < array2[0].length; j++) {
				arrayTemp[k][j] = array2[i][j];
			}
			k++;
		}
		return arrayTemp;
	}

	/**
	 * @method toStringMatrix export a array as a string
	 * @param array
	 * @return string string representation of array
	 */
	public static String toStringMatrix(double[][] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length - 1; j++)
				sb.append("" + array[i][j] + "\t");
			sb.append(array[i][array[0].length - 1] + "\n");
		}
		return sb.toString();
	}

	/**
	 * @method toString export a array as a string
	 * @param array
	 * @return string string representation of array
	 */
	public static String toString(String[][] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length - 1; j++)
				sb.append("" + array[i][j] + "\t");
			sb.append(array[i][array[0].length - 1] + "\n");
		}
		return sb.toString();
	}

	/**
	 * @method ArrayList<String[]> export a array as an ArrayList<String[]>
	 * @param array
	 * @return ArrayList<String[]> ArrayList<Stringv> representation of array
	 */
	public static String toString(ArrayList<String[]> arrayList) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arrayList.size(); i++) {
			for (int j = 0; j < arrayList.get(0).length - 1; j++) {
				sb.append("" + arrayList.get(i)[j] + "\t");
			}
			sb.append(arrayList.get(i)[arrayList.get(0).length - 1] + "\n");
		}
		return sb.toString();
	}

	public static String[][] toArray(ArrayList<String[]> arrayList) {
		String[][] array = new String[arrayList.size()][arrayList.get(0).length];
		for (int i = 0; i < arrayList.size(); i++) {
			for (int j = 0; j < arrayList.get(0).length; j++) {
				array[i][j] = arrayList.get(i)[j];
			}
		}
		return array;
	}

	/**
	 * @method ArrayList<String[]> export a array as an ArrayList<String[]>
	 * @param array
	 * @return ArrayList<String[]> ArrayList<Stringv> representation of array
	 */
	public static ArrayList<String[]> toList(String[][] array) {
		ArrayList<String[]> arrayList = new ArrayList<>();
		for (int i = 0; i < array.length; i++) {
			arrayList.add(ArrayUtils.getRow(array, i));
		}
		return arrayList;
	}

	/**
	 * @method sum(array,index) return the sum of all value in the array
	 * @param array a Matrix or double[][]
	 * @return sum a double
	 */
	public static double sum(double[][] array) {
		double sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += VectorUtils.sum(ArrayUtils.getRow(array, i));
		}
		return sum;
	}

	/**
	 * Return the mean of the whole array
	 * 
	 * @param array
	 * @return
	 */
	public static double mean(double[][] array) {
		double sum = sum(array);
		sum = sum / (array.length * array[0].length);
		return sum;
	}

	/**
	 * @method min(array) return minimumValue of a array
	 * @param array a Matrix or double[][]
	 * @return min a double
	 */
	public static double min(double[][] array) {
		double min = array[0][0];
		for (int i = 0; i < array[0].length; i++) {
			double mintemp = VectorUtils.min(ArrayUtils.getColumn(array, i));
			if (min > mintemp) {
				min = mintemp;
			}
		}
		return min;
	}

	/**
	 * @method max(array) return minimumValue of a array
	 * @param array a Matrix or double[][]
	 * @return max a double
	 */
	public static double max(double[][] array) {
		double max = array[0][0];
		for (int i = 0; i < array[0].length; i++) {
			double maxtemp = VectorUtils.max(ArrayUtils.getColumn(array, i));
			if (max < maxtemp) {
				max = maxtemp;
			}
		}
		return max;
	}

	/**
	 * @method zeroRow(array,index) Put zero in the index Row of a Matrix
	 * @param array a Matrix or double[][]
	 * @param index row index
	 */
	public static void zeroRow(double[][] array, int index) {
		for (int i = 0; i < array[0].length; i++) {
			array[i][index] = 0;
		}
	}

	/**
	 * @method zeroColumn(array,index) Put zero in the index Column of a Matrix
	 * @param array a Matrix or double[][]
	 * @param index row index
	 */
	public static void zeroColumn(double[][] array, int index) {
		for (int i = 0; i < array[0].length; i++) {
			array[index][i] = 0;
		}
	}

	/**
	 * @method eliminateColumn(array,index) eliminate the last Column of a Matrix
	 * @param array a double[][]
	 * @return same array with one dimension deleted
	 */
	public static double[][] eliminateColumn(double[][] array) {
		double[][] arrayTemp = new double[array.length][array[0].length - 1];
		for (int i = 0; i < arrayTemp.length; i++) {
			for (int j = 0; j < arrayTemp[0].length; j++) {
				arrayTemp[i][j] = array[i][j];
			}
		}
		return arrayTemp;
	}

	/**
	 * Center all the row from the Matrix
	 * 
	 * @param array a Matrix or double[][]
	 * 
	 * @return the centered array
	 */
	public static double[][] centerRow(double[][] array) {
		double[][] tempMatrix = new double[array.length][array[0].length];
		// loop on all the row
		for (int i = 0; i < array.length; i++) {
			// find mean
			double[] row = getRow(array, i);
			double mean = VectorUtils.mean(row);
			for (int j = 0; j < array[0].length; j++) {
				tempMatrix[i][j] = array[i][j] - mean;
			}
		}
		return tempMatrix;
	}

	/**
	 * Center all the column from the Matrix
	 * 
	 * @param array a Matrix or double[][]
	 * 
	 * @return the centered array
	 */
	public static double[][] centerColumn(double[][] array) {
		double[][] tempMatrix = new double[array.length][array[0].length];
		// loop on all the column
		for (int i = 0; i < array[0].length; i++) {
			// find mean
			double[] column = getColumn(array, i);
			double mean = VectorUtils.mean(column);
			for (int j = 0; j < array.length; j++) {
				tempMatrix[j][i] = array[j][i] - mean;
			}
		}
		return tempMatrix;
	}

	/**
	 * Sort an array using column given by the index
	 * 
	 * @param array to sort
	 * @param index column index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static double[][] sortColumn(double[][] array, int index) {
		double[][] arrayTemp = array.clone();
		// double[] colToSort = getColumn(array, index);
		// double[] colToSortCopy = colToSort.clone();
		// Arrays.sort(colToSort);
		final int d = index; // 0 <= d <= n-1
		Arrays.sort(arrayTemp, new java.util.Comparator() {
			@Override
			public int compare(Object arg0, Object arg1) {
				double[] o1 = (double[]) arg0;
				double[] o2 = (double[]) arg1;
				if (o1[d] > o2[d])
					return 1; // 1 for descending order
				else if (o1[d] < o2[d])
					return -1; // 1 for descending order
				else
					return 0;
			}
		});
		return arrayTemp;
	}

	/**
	 * Sort a column inside a String[][] array <br>
	 * <br>
	 * WARNING: if two values are the same in the column, it will display only the
	 * first one with this value in the sorted array
	 * 
	 * @param array  to classify
	 * @param index  of the column to sort
	 * @param number True, it sorts the column using Arrays.sort(Double)<br>
	 *               False, it uses Arrays.sort(String)
	 * @return
	 */
	public static String[][] sortColumn(String[][] array, int index, boolean number) {
		String[][] arrayTemp = new String[array.length][array[0].length];
		String[] colSort = getColumn(array, index);
		String[] col = colSort.clone();
		if (number) {
			double[] colSortDouble = new double[colSort.length];
			double[] colDouble = new double[colSort.length];
			for (int i = 0; i < colSort.length; i++) {
				colSortDouble[i] = Double.parseDouble(colSort[i]);
				colDouble[i] = Double.parseDouble(col[i]);
			}
			Arrays.sort(colSortDouble);
			for (int i = 0; i < colSortDouble.length; i++) {
				int previousIndex = -1;
				for (int k = 0; k < col.length; k++) {
					if (colDouble[k] == colSortDouble[i])
						previousIndex = k;
				}
				for (int j = 0; j < array[0].length; j++) {
					if (previousIndex == -1) {
						System.out.println("Cannot find " + colSort[i]);
						System.out.println();
					} else {
						arrayTemp[i][j] = array[previousIndex][j];
					}
				}
			}
		} else {
			Arrays.sort(colSort);
			for (int i = 0; i < colSort.length; i++) {
				int previousIndex = -1;
				for (int k = 0; k < col.length; k++) {
					if (col[k].equals(colSort[i]))
						previousIndex = k;
				}
				for (int j = 0; j < array[0].length; j++) {
					if (previousIndex == -1) {
						System.out.println(colSort[i]);
						System.out.println();
					} else {
						arrayTemp[i][j] = array[previousIndex][j];
					}
				}
			}
		}
		return arrayTemp;
	}

	/**
	 * return=(coeff1)*array1 + (coeff2)*array2
	 * 
	 * @param coeff1
	 * @param array1
	 * 
	 * @param coeff2
	 * @param array2
	 * 
	 * @return result array
	 */
	public static double[][] addition(double coeff1, double[][] array1, double coeff2, double[][] array2) {
		double[][] result = new double[array1.length][array1[0].length];
		for (int i = 0; i < array1.length; i++) {
			for (int j = 0; j < array1[0].length; j++) {
				result[i][j] = coeff1 * array1[i][j] + coeff2 * array2[i][j];
			}
		}
		return result;
	}

	/**
	 * Return transposed array of input
	 * 
	 * @param array
	 * @return transpose
	 */
	public static double[][] transpose(double[][] array) {
		double[][] transpose = new double[array[0].length][array.length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				transpose[j][i] = array[i][j];

			}
		}
		return transpose;
	}

	public static String[][] transpose(String[][] array) {
		String[][] transpose = new String[array[0].length][array.length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				transpose[j][i] = array[i][j];

			}
		}
		return transpose;
	}

	/**
	 * log2(value)
	 * 
	 * @param array
	 * @return
	 */
	public static double[][] log2(double[][] array) {
		return transform(array, 0);
	}

	/**
	 * 2^value
	 * 
	 * @param array
	 * @return
	 */
	public static double[][] pow2(double[][] array) {
		return transform(array, 1);
	}

	/**
	 * Transform all value of this array, using MathUtils.transform
	 * 
	 * @param array
	 * @param type
	 * @return
	 */
	public static double[][] transform(double[][] array, int type) {
		double[][] transform = new double[array.length][array[0].length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				double value = array[i][j];
				transform[i][j] = MathUtils.transform(value, type);
			}
		}
		return transform;
	}

	/**
	 * Multiply a vector(=in fact a diagonal matrice) times a array X'=MX or XM with
	 * a diagonal = vector
	 * 
	 * @param vector the diagonal of the diagonal array
	 * @param array
	 * 
	 * @return result array
	 */
	public static double[][] diagonalTimes(double[] diagonal, double[][] array) {
		// X'=MX so we have m_ii on each row :
		double[][] returnMatrix = new double[array.length][array[0].length];
		for (int i = 0; i < array.length; i++) {
			for (int k = 0; k < array[0].length; k++) {
				returnMatrix[i][k] = array[i][k] * diagonal[i];
			}
		}
		return returnMatrix;
	}

	public static double[][] diagonalTimes(double[][] array, double[] diagonal) {
		// X'=XM so we have m_ii on each column i :
		double[][] returnMatrix = new double[array.length][array[0].length];
		for (int i = 0; i < array[0].length; i++) {
			for (int k = 0; k < array.length; k++) {
				returnMatrix[k][i] = array[k][i] * diagonal[i];
			}
		}
		return returnMatrix;
	}

	/**
	 * Display a Matrix
	 * 
	 * @param String the name of our array
	 * @param array  a double[][] or double[]
	 * 
	 * @return void
	 */
	public static void displayMatrix(String name, double[][] array) {
		System.out.println();
		System.out.println(name);
		for (double[] element : array) {
			for (int j = 0; j < element.length; j++) {
				System.out.print(element[j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	public static void displayMatrix(String name, String[][] array) {
		System.out.println();
		System.out.println(name);
		for (String[] element : array) {
			for (int j = 0; j < element.length; j++) {
				System.out.print(element[j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	public static void displayMatrix(String name, Object[][] r) {
		System.out.println();
		System.out.println(name);
		for (int i = 0; i < r.length; i++) {
			for (Object o : r[i]) {
				System.out.print(o.toString() + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	public static void displayVector(String name, double[] r) {
		System.out.println();
		System.out.println(name);
		for (double element : r) {

			System.out.print(element + "\t");

			System.out.println();
		}
		System.out.println();
	}

	public static void displayVector(String name, String[] r) {
		System.out.println();
		System.out.println(name);
		for (String element : r) {

			System.out.print(element + "\t");

			System.out.println();
		}
		System.out.println();
	}

	/**
	 * Calcul of euclidean distance Matrix
	 * 
	 * @param array a double[][] or array
	 * @return distance array double[][]
	 */
	public static double[][] euclideanDistance(double[][] array) {
		double[][] distance = new double[array.length][array.length];
		// Euclidean measure=new Euclidean();
		// for(int i = 0 ; i < distance.length; i++){
		// for(int j = 0 ; j < distance.length; j++){
		// distance[i][j] = measure.calculateMeasureDouble(array[i], array[j]);
		// distance[j][i]=distance[i][j];
		// }
		// }
		return distance;
	}

	/**
	 * Calcul of the covariance Matrix
	 * 
	 * @param array a double[][] or array
	 * @return distance array double[][]
	 */
	public static double[][] covarianceMatrix(double[][] array) throws Exception {
		double covarianceMatrix[][] = new double[array.length][array.length];
		for (int i = 0; i < array.length; i++) {
			double[] item1 = array[i];
			for (int j = i; j < array.length; j++) {
				double[] item2 = array[j];
				covarianceMatrix[i][j] = covariance(item1, item2);
				covarianceMatrix[j][i] = covarianceMatrix[i][j];
			}
		}
		return covarianceMatrix;
	}

	private static double covariance(double vector1[], double vector2[]) throws Exception {
		if (vector1.length != vector2.length)
			throw new Exception("Vectors do not have the same length");
		double mean1 = VectorUtils.mean(vector1);
		double mean2 = VectorUtils.mean(vector2);
		double sum = 0;
		for (int i = 0; i < vector1.length; i++) {
			sum += (vector1[i] - mean1) * (vector2[i] - mean2);
		}
		return sum / (vector1.length - 1);
	}

	/**
	 * Calcul of the Inner Product Matrix
	 * 
	 * @param array a double[][] or array
	 * @return innerProduct Matrix double[][] or array
	 */
	public static double[][] innerProduct(double[][] array) {
		double[][] innerProduct = new double[array.length][array.length];
		for (int i = 0; i < innerProduct.length; i++) {
			for (int j = 0; j < innerProduct.length; j++) {
				innerProduct[i][j] = 0;
				for (int k = 0; k < array[0].length; k++) {
					innerProduct[i][j] += array[i][k] * array[j][k];
				}
				innerProduct[j][i] = innerProduct[i][j];
			}
		}

		return innerProduct;
	}

	/**
	 * Calcul of the Outer Product Matrix
	 * 
	 * @param array a double[][] or array
	 * @return innerProduct Matrix double[][] or array
	 */
	public static double[][] outerProduct(double[][] array) {
		double[][] outerProduct = new double[array[0].length][array[0].length];
		for (int i = 0; i < outerProduct.length; i++) {
			for (int j = 0; j < outerProduct.length; j++) {
				outerProduct[i][j] = 0;
				for (int k = 0; k < array[0].length; k++) {
					outerProduct[i][j] += array[k][i] * array[k][j];
				}
				outerProduct[j][i] = outerProduct[i][j];
			}
		}
		return outerProduct;
	}

	/**
	 * Rescale each Column of the array, at the end all the points as to fill in a
	 * cube of diameter gridSize
	 * 
	 * @param array    a double[][] or array
	 * @param gridSize a double
	 * @return newarray rescaled double[][]
	 */
	public static double[][] rescaleData(double[][] positionMatrix, double gridSize) {

		// First we search the maximal value for each axes
		double[] max = new double[positionMatrix[0].length];
		for (int i = 0; i < positionMatrix.length; i++) {
			for (int k = 0; k < max.length; k++) {
				max[k] = Math.max(max[k], Math.abs(positionMatrix[i][k]));
			}
		}
		double maxmax = VectorUtils.max(max);
		System.out.println("Value maxmax for the rescaling = " + maxmax);

		// if x=maxmax -> x=gridSize so we this conversion.
		for (int i = 0; i < positionMatrix.length; i++) {
			for (int k = 0; k < positionMatrix[0].length; k++) {
				positionMatrix[i][k] = positionMatrix[i][k] * gridSize / maxmax;
			}
		}
		System.out.println("Rescaling factor= " + gridSize / maxmax);

		return positionMatrix;
	}

	/**
	 * Transform an array into a List of rows
	 * 
	 * @param array
	 * @return
	 */
	public static List<String[]> fromArraytoList(String[][] array) {
		ArrayList<String[]> results = new ArrayList<String[]>();
		for (int i = 0; i < array.length; i++) {
			String[] row = getRow(array, i);
			results.add(row);
		}
		return results;
	}

	public static TreeMap<String, Integer> clone(TreeMap<String, Integer> array) {
		TreeMap<String, Integer> newArray = new TreeMap<String, Integer>();
		for (String key : array.keySet()) {
			newArray.put(key, array.get(key));
		}
		return newArray;
	}

	public static ArrayList<String> clone(ArrayList<String> array) {
		ArrayList<String> newArray = new ArrayList<String>();
		for (String value : array) {
			newArray.add(value);
		}
		return newArray;
	}

	public static String[][] clone(String[][] array) {
		String[][] newArray = new String[array.length][array[0].length];
		for (int i = 0; i < newArray.length; i++) {
			for (int j = 0; j < newArray[0].length; j++) {
				newArray[i][j] = array[i][j];
			}
		}
		return newArray;
	}

	public static double[][] clone(double[][] array) {
		double[][] newArray = new double[array.length][array[0].length];
		for (int i = 0; i < newArray.length; i++) {
			for (int j = 0; j < newArray[0].length; j++) {
				newArray[i][j] = array[i][j];
			}
		}
		return newArray;
	}

}
