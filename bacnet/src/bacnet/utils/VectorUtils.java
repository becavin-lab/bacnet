package bacnet.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.OmicsData;

/**
 * Vector are array with one dimension like : <code>String[]</code> or <code>double[]</code>
 * 
 * @author UIBC
 *
 */
public class VectorUtils {

    /**
     * Calculate union of two vector L1 and L2 <br>
     * 
     * @param vector1
     * @param vector2
     * @return L1 U L2
     */
    public static String[] union(String[] vector1, String[] vector2) {
        TreeSet<String> setFinal = new TreeSet<String>();
        for (String element : vector1)
            setFinal.add(element);
        for (String element : vector2)
            setFinal.add(element);

        String[] vectorFinal = new String[setFinal.size()];
        int i = 0;
        for (String element : setFinal) {
            vectorFinal[i] = element;
            i++;
        }
        return vectorFinal;
    }

    /**
     * Calculate the union of a list of Vector
     * 
     * @param lists
     * @return a vector containing all the elements from the different lists
     */
    public static String[] union(ArrayList<String[]> lists) {
        String[] vectorFinal = lists.get(0).clone();
        for (int i = 1; i < lists.size(); i++) {
            vectorFinal = union(vectorFinal, lists.get(i));
        }
        return vectorFinal;
    }

    /**
     * Calculate intersection of two vectors L1 and L2 <br>
     * 
     * @param vector1
     * @param vector2
     * @return L1 inter L2
     */
    public static String[] intersect(String[] vector1, String[] vector2) {
        TreeSet<String> setFinal = new TreeSet<String>();
        for (String element : vector1) {
            for (String element2 : vector2) {
                if (element2.equals(element)) {
                    setFinal.add(element);
                }
            }

        }

        String[] vectorFinal = new String[setFinal.size()];
        int i = 0;
        for (String element : setFinal) {
            vectorFinal[i] = element;
        }
        return vectorFinal;
    }

    /**
     * Calculate the intersection of a list of Vector
     * 
     * @param lists
     * @return a <code>String[]</code> containing all the elements contained in the different lists
     */
    public static String[] intersect(ArrayList<String[]> lists) {
        String[] vectorFinal = lists.get(0).clone();
        for (int i = 1; i < lists.size(); i++) {
            vectorFinal = intersect(vectorFinal, lists.get(i));
        }
        return vectorFinal;
    }

    /**
     * Return the subVector of vector between index begin and index end
     * 
     * @param vector double[]
     * @param begin index
     * @param end index
     * @return
     */
    public static double[] subVector(double[] vector, int begin, int end) {
        double[] subVector = new double[end - begin + 1];
        int k = 0;
        for (int i = begin; i < end + 1; i++) {
            subVector[k] = vector[i];
            k++;
        }
        return subVector;
    }

    /**
     * Calculate mean of a vector
     * 
     * @param vector a double[]
     * @return the value of the mean
     */
    public static double mean(double[] vector) {
        double sum = 0;
        int length = 0;
        for (double element : vector) {
            if (element != OmicsData.MISSING_VALUE) {
                sum += element;
                length++;
            }
        }
        return sum / (double) length;
    }

    public static double mean(Double[] vector) {
        double sum = 0;
        int length = 0;
        for (double element : vector) {
            if (element != OmicsData.MISSING_VALUE) {
                sum += element;
                length++;
            }
        }
        return sum / (double) length;
    }

    /**
     * Calculate median of a vector
     * 
     * @param vector
     * @return
     */
    public static double median(double[] vector) {
        double median;
        double[] vectorB = vector.clone();
        int length = vectorB.length;
        Arrays.sort(vectorB);
        if (length == 0)
            median = OmicsData.MISSING_VALUE;
        else if (length == 1)
            median = vector[0];
        else if (length % 2 == 0) {
            median = vectorB[length / 2] + (vectorB[length / 2 - 1] - vectorB[length / 2]) / 2;
        } else {
            int index = length / 2;
            median = vectorB[index];
        }
        return median;
    }

    /**
     * Calculate median of a vector
     * 
     * @param vector
     * @return
     */
    public static double median(Double[] vector) {
        double median;
        Double[] vectorB = vector.clone();
        int length = vectorB.length;
        Arrays.sort(vectorB);
        if (length % 2 == 0) {
            median = vectorB[length / 2] + (vectorB[length / 2 - 1] - vectorB[length / 2]) / 2;
        } else
            median = vectorB[length / 2];
        return median;
    }

    /**
     * Return a list of quantiles<br>
     * Example: quantiles(vector,10)[1] will calculate the 1d decile, the 1d of 10th quantile<br>
     * Example: quantiles(vector,100)[5] will calculate the 5d centile, the 5th of 100th quantile = the
     * 5% cut-off<br>
     * 
     * @param vector
     * @param k
     * @return
     */
    public static double[] quantiles(double[] vector, int k) {
        double[] quantiles = new double[k + 1];
        double[] vectorB = vector.clone();
        Arrays.sort(vectorB);
        quantiles[0] = vectorB[0];
        for (int j = 1; j < k + 1; j++) {
            int i = 0;
            double q = (double) (vector.length * j) / (double) k;
            int index = 0;
            if (MathUtils.isInteger(q)) {
                if (q == vectorB.length) {
                    index = vectorB.length - 1;
                    // System.out.println();
                    quantiles[j] = vectorB[index];
                } else {
                    index = (int) q;
                    quantiles[j] = vectorB[index] + (vectorB[index] - vectorB[index + 1]) / 2;
                }
            } else {
                index = (int) q;
                quantiles[j] = vectorB[index];
            }
            // System.out.println(q + " "+index);

        }
        return quantiles;
    }

    /**
     * Calculate variance of a vector V(X)=mean(X^2)-(mean(X))^2
     * 
     * @param vector a double[]
     * @return the value of the variance
     */
    public static double variance(double vector[]) {
        if (vector.length == 0)
            return OmicsData.MISSING_VALUE;
        else if (vector.length == 1)
            return OmicsData.MISSING_VALUE;;
        return mean(squareVector(vector)) - mean(vector) * mean(vector);
    }

    /**
     * Calculate deviation of a vector sigma(X)=sqrt(V(X))
     * 
     * @param vector a double[]
     * @return the value of the variance
     */
    public static double deviation(double vector[]) {
        if (vector.length == 0)
            return OmicsData.MISSING_VALUE;
        else if (vector.length == 1)
            return OmicsData.MISSING_VALUE;;
        return Math.sqrt(variance(vector));
    }

    /**
     * Calcul of the square of a vector
     * 
     * @param vector a double[]
     * 
     * @return a double[]
     */
    public static double[] squareVector(double[] vector) {
        double[] square = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            square[i] = vector[i] * vector[i];
        }
        return square;
    }

    /**
     * Find maximum value of a vector
     * 
     * @param vector a double[]
     * 
     * @return a double
     */
    public static double max(double[] vector) {
        double max = vector[0];
        for (int i = 1; i < vector.length; i++) {
            if (vector[i] != OmicsData.MISSING_VALUE) {
                max = Math.max(max, vector[i]);
            }
        }
        return max;
    }

    /**
     * Find minimum value of a vector
     * 
     * @param vector a double[]
     * 
     * @return a double
     */
    public static double min(double[] vector) {
        double min = vector[0];
        for (int i = 1; i < vector.length; i++) {
            if (vector[i] != OmicsData.MISSING_VALUE) {
                min = Math.min(min, vector[i]);
            }
        }
        return min;
    }

    /**
     * Sum of all term in the vector
     * 
     * @param vector a double[]
     * 
     * @return a double
     */
    public static double sum(double[] vector) {
        double sum = 0;
        for (int i = 1; i < vector.length; i++) {
            sum += vector[i];
        }
        return sum;
    }

    /**
     * return=(coeff1)*array1 + (coeff2)*array2
     * 
     * @param coeff1 = double
     * @param array1 = vector
     * 
     * @param coeff2 = double
     * @param array2 = vector
     * 
     * @return result vector
     */
    public static double[] addition(double coeff1, double[] vector1, double coeff2, double[] vector2) {
        if (vector1.length != vector2.length) {
            System.err.println("Vectors have not the same size");
            return null;
        }
        double[] addition = new double[vector1.length];
        for (int i = 0; i < vector1.length; i++) {
            addition[i] = coeff1 * vector1[i] + coeff2 * vector2[i];
        }
        return addition;
    }

    public static double[] minus(double[] vector1, double[] vector2) {
        double[] resultVector = new double[vector1.length];
        for (int i = 0; i < resultVector.length; i++) {
            resultVector[i] = vector1[i] - vector2[i];
        }
        return resultVector;
    }

    public static double[] deleteMissingValue(double[] vector) {
        ArrayList<Double> newList = new ArrayList<Double>();
        for (double value : vector) {
            if (value != ExpressionMatrix.MISSING_VALUE)
                newList.add(value);
            // else System.out.println("found missing values");
        }
        double[] resultVector = new double[newList.size()];
        for (int i = 0; i < resultVector.length; i++) {
            resultVector[i] = newList.get(i);
        }
        return resultVector;
    }

    /**
     * Calculate euclidean distance between vector x and vector y
     * 
     * @param x
     * @param y
     * @return
     */
    public static double euclideanDistance(double[] x, double[] y) {
        double sum = 0;
        for (int i = 0; i < x.length; i++)
            if (!(Double.isNaN(x[i]) || Double.isNaN(y[i]))) {
                sum += (x[i] - y[i]) * (x[i] - y[i]);
            }
        return Math.sqrt(sum);
    }

    public static double pearsonCorrelation(double[] x, double[] y) {
        double pearson = covariance(x, y) / (deviation(x) * deviation(y));
        return pearson;
    }

    /**
     * Calculate the covariance of x * y
     * 
     * @param x
     * @param y
     * @return
     */
    public static double covariance(double[] x, double[] y) {
        double mean1 = mean(x);
        double mean2 = mean(y);
        double sum = 0;
        double quantity = 0;
        for (int i = 0; i < x.length; i++)
            if (!(Double.isNaN(x[i]) || Double.isNaN(y[i]))) {
                sum += (x[i] - mean1) * (y[i] - mean2);
                quantity++;
            }
        return sum / quantity;
    }

    /**
     * Add an element to the end of the vector
     * 
     * @param vector
     * @param text
     * @return
     */
    public static Object[] addElement(Object[] vector, String text) {
        Object[] newVector = new Object[vector.length + 1];
        for (int i = 0; i < vector.length; i++) {
            newVector[i] = vector[i];
        }
        newVector[vector.length] = text;
        return newVector;
    }

    /**
     * log2(value)
     * 
     * @param array
     * @return
     */
    public static double[] log10(double[] vector) {
        return transform(vector, 0);
    }

    /**
     * log2(value)
     * 
     * @param array
     * @return
     */
    public static double[] log2(double[] vector) {
        return transform(vector, 0);
    }

    /**
     * 2^value
     * 
     * @param array
     * @return
     */
    public static double[] pow2(double[] vector) {
        return transform(vector, 1);
    }

    /**
     * Transform all value of this vector, using MathUtils.transform
     * 
     * @param array
     * @param type
     * @return
     */
    public static double[] transform(double[] vector, int type) {
        double[] transform = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            double value = vector[i];
            transform[i] = MathUtils.transform(value, type);
        }
        return transform;
    }

    /**
     * Return a <code>ArrayList<String></code> representation of the input vector
     * 
     * @param vector
     * @return
     */
    public static ArrayList<String> toList(String[] vector) {
        ArrayList<String> result = new ArrayList<>();
        for (String temp : vector) {
            result.add(temp);
        }
        return result;
    }

    /**
     * Return a <code>String</code> representation of the input vector Tab delimited
     * 
     * @param vector
     * @return
     */
    public static String toString(String[] vector) {
        String result = "";
        for (String temp : vector) {
            result += temp + "\t";
        }

        return result;
    }

    /**
     * Return a <code>String</code> representation of the input TreeSet Tab delimited
     * 
     * @param vector
     * @return
     */
    public static String toString(TreeSet<String> vector) {
        String result = "";
        for (String temp : vector) {
            result += temp + "\t";
        }
        return result;
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

}
