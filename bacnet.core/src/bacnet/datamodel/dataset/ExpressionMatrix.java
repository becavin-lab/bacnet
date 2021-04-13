package bacnet.datamodel.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

/**
 * 
 * This is the principal data model used in the software for managing big table of expression
 * 
 * 
 * 
 * @author christophebecavin
 *
 */
public class ExpressionMatrix extends OmicsData implements Cloneable, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6000672805382130897L;

    /**
     * the rowNames are in a TreeMap to have a matching between names and index in values array, we
     * navigate easily in the TreeMap which is sorted (using a modifyed Comparator)
     */
    private TreeMap<String, Integer> rowNames = new TreeMap<String, Integer>();
    /**
     * In some cases (clustering for example) we need the row to be specifically organized We use a List
     * to force the organization of the row A boolean indicate if this matrix as to be used.
     */
    private ArrayList<String> orderedRowNames = new ArrayList<String>();
    /**
     * Indicates wether or not rows need to be ordered thanks to ArrayList<String> orderedRowNames
     */
    private boolean ordered = false;
    /**
     * Column names are in an ArrayList<String> so we can navigate easily, and have access to the index
     * of the column just by giving an header
     */
    private ArrayList<String> headers = new ArrayList<String>();
    /**
     * I use an array of double to gain memory and increase access time
     */
    private double[][] values;
    /**
     * we put all important informations in a table (which should be of the size of "values")
     */
    private ArrayList<String> headerAnnotation = new ArrayList<String>();
    /**
     * String[][] for all annotation information
     */
    private String[][] annotations = new String[0][0];

    /**
     * The name of first row used when converted to an array
     */
    private String firstRowName = "";
    /**
     * The name of second row used when converted to an array
     */
    private String secondRowName = "";

    /**
     * Indicates if an ExpressionMatrix has been loaded or not
     */
    private transient boolean loaded = false;

    public ExpressionMatrix() {
        this.setType(TypeData.ExpressionMatrix);
        headers = new ArrayList<String>();
        rowNames = new TreeMap<String, Integer>(new ExpressionMatrixCompare());
        headerAnnotation = new ArrayList<String>();
        values = new double[1][1];
        orderedRowNames = new ArrayList<String>();
    }

    /**
     * Create a void ExpressionMatrix and associate headers
     * 
     * @param headers
     */
    public ExpressionMatrix(ArrayList<String> headers) {
        this.headers = headers;
        this.setType(TypeData.ExpressionMatrix);
        rowNames = new TreeMap<String, Integer>(new ExpressionMatrixCompare());
        headerAnnotation = new ArrayList<String>();
        values = new double[1][1];
        orderedRowNames = new ArrayList<String>();
    }

    public ExpressionMatrix(String header, int nbRow) {
        this.setType(TypeData.ExpressionMatrix);
        headers = new ArrayList<String>();
        this.headers.add(header);
        rowNames = new TreeMap<String, Integer>(new ExpressionMatrixCompare());
        headerAnnotation = new ArrayList<String>();
        values = new double[nbRow][1];
        orderedRowNames = new ArrayList<String>();
    }

    public void setValues(double[][] values) {
        this.values = values;
    }

    /**
     * Create new double[][] values with nbRow and nbColumn All value will then be equal to 0
     * 
     * @param nbRow
     * @param nbColumn
     */
    public void setValues(int nbRow, int nbColumn) {
        this.values = new double[nbRow][nbColumn];
    }

    public double[][] getValues() {
        return values;
    }

    public String[][] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String[][] annotations) {
        this.annotations = annotations;
    }

    public void createValues(int rowNumber, int colNumber) {
        values = new double[rowNumber][colNumber];
    }

    public void setRowNames(TreeMap<String, Integer> rowNames) {
        this.rowNames = rowNames;
    }

    public TreeMap<String, Integer> getRowNames() {
        return rowNames;
    }

    public ArrayList<String> getRowNamesToList() {
        ArrayList<String> rowNamesList = new ArrayList<String>();
        for (String rowName : rowNames.keySet()) {
            rowNamesList.add(rowName);
        }
        return rowNamesList;
    }

    public ArrayList<String> getOrderedRowNames() {
        return orderedRowNames;
    }

    public void setOrderedRowNames(ArrayList<String> orderedRowNames) {
        this.orderedRowNames = orderedRowNames;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public void setOrdered(boolean ordered, ArrayList<String> orderedRowNames) {
        this.ordered = ordered;
        this.orderedRowNames = orderedRowNames;
    }

    public ArrayList<String> getHeaders() {
        return headers;
    }

    public String[] getHeadersToArray() {
        String[] headerOnly = getHeaders().toArray(new String[0]);
        String[] titles = new String[headerOnly.length + 1];
        titles[0] = firstRowName;
        for (int i = 1; i < titles.length; i++) {
            titles[i] = headerOnly[i - 1];
        }
        return titles;
    }

    public String[] getHeadersListToArray() {
        String[] headerOnly = getHeaders().toArray(new String[0]);
        String[] titles = new String[headerOnly.length + 2];
        titles[0] = firstRowName;
        titles[1] = secondRowName;
        for (int i = 2; i < titles.length; i++) {
            titles[i] = headerOnly[i - 2];
        }
        return titles;
    }

    /**
     * Return the appropriate column index for displaying in the Genome viewer<br>
     * By order of priority:
     * <li>LOGFC containing column
     * <li>FC containing column
     * <li>p-value containing column
     * <li>VALUE
     * <li>first column
     * 
     * @return
     */
    public int getGenomeViewerColumnIndex() {
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header.contains(ColNames.LOGFC + ""))
                return i;
        }
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header.contains(ColNames.FC + ""))
                return i;
        }
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header.contains(ColNames.PVALUE + ""))
                return i;
        }
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header.contains(ColNames.VALUE + ""))
                return i;
        }
        return 0;
    }

    /**
     * Return a list of the data contain in this matrix which correspond to a given strand
     * 
     * @param strand=true -> + strand strand=false -> - strand
     * @return
     */
    public ArrayList<String> getStrandHeaders(boolean strand) {
        ArrayList<String> colNames = getHeaders();
        ArrayList<String> selectedCol = new ArrayList<String>();
        // if(strand) we consider only the expression in the same strand of the sRNA_tss
        for (String colName : colNames) {
            if (strand) {
                if (colName.contains("+"))
                    selectedCol.add(colName);
            } else {
                if (colName.contains("-"))
                    selectedCol.add(colName);
            }
        }
        return selectedCol;
    }

    public void setHeaders(ArrayList<String> headers) {
        this.headers = headers;
    }

    public String getHeader(int i) {
        return headers.get(i);
    }

    public void setHeader(String header, int i) {
        headers.set(i, header);
    }

    public void addHeader(String header) {
        headers.add(header);
    }

    public void modifHeader(String oldHeader, String newHeader) {
        int index = headers.indexOf(oldHeader);
        if (index != -1)
            headers.set(index, newHeader);
        else
            System.out.println("The header as not been found, no change has been performed");
    }

    /**
     * Giving a list of header we reorganize the ExpressionMatrix
     * 
     * @param headers
     */
    public void reorderHeaders(ArrayList<String> headers) {
        ArrayList<String> oldHeaders = this.getHeaders();
        double[][] values = this.getValues();
        double[][] values2 = ArrayUtils.clone(values);

        int j = 0;
        for (String header : headers) {
            double[] column = ArrayUtils.getColumn(values, oldHeaders.indexOf(header));
            for (int i = 0; i < values.length; i++) {
                values2[i][j] = column[i];
            }
            j++;
        }

        this.setValues(values2);
        this.setHeaders(headers);
    }

    public ArrayList<String> getHeaderAnnotation() {
        return headerAnnotation;
    }

    public void setHeaderAnnotation(ArrayList<String> headerAnnotation) {
        this.headerAnnotation = headerAnnotation;
    }

    /**
     * Get value in the ExpressionMatrix
     * 
     * @param rowName name of the row
     * @param colName name of the column
     * @return
     */
    public double getValue(String rowName, String colName) {
        int indexRow = rowNames.get(rowName);
        int indexCol = headers.indexOf(colName);
        if (getValues().length < indexRow) {
            System.err.println(this.getName() + " nbRow:" + getValues().length + " try access row:" + indexRow);
        }
        if (getValues()[0].length < indexCol) {
            System.err.println(this.getName() + " nbCol:" + getValues()[0].length + " try access col:" + indexCol);
        }
        // System.out.println(this.getName());
        return getValues()[indexRow][indexCol];
    }

    public String getValueAnnotation(String rowName, String colName) {
        int indexRow = rowNames.get(rowName);
        int indexCol = headerAnnotation.indexOf(colName);
        if (getAnnotations().length < indexRow) {
            System.err.println(this.getName() + " nbRow:" + getAnnotations().length + " try access row:" + indexRow);
        }
        if (getAnnotations()[0].length < indexCol) {
            System.err.println(this.getName() + " nbCol:" + getAnnotations()[0].length + " try access col:" + indexCol);
        }
        return getAnnotations()[indexRow][indexCol];
    }

    public void setValue(double value, String rowName, String colName) {
        int indexRow = rowNames.get(rowName);
        int indexCol = headers.indexOf(colName);
        if (getValues().length < indexRow) {
            System.err.println(this.getName() + " nbRow:" + getValues().length + " try access row:" + indexRow);
        }
        if (getValues()[0].length < indexCol) {
            System.err.println(this.getName() + " nbCol:" + getValues()[0].length + " try access col:" + indexCol);
        }
        getValues()[indexRow][indexCol] = value;
    }

    public void setValue(double value, int i, int j) {
        if (getValues().length < i) {
            System.err.println(this.getName() + " nbRow:" + getValues().length + " try access row:" + i);
        }
        if (getValues()[0].length < j) {
            System.err.println(this.getName() + " nbCol:" + getValues()[0].length + " try access col:" + j);
        }
        getValues()[i][j] = value;
    }

    public String setValueAnnotation(String annot, String rowName, String colName) {
        int indexRow = rowNames.get(rowName);
        int indexCol = headerAnnotation.indexOf(colName);

        if (getAnnotations().length < indexRow) {
            System.err.println(this.getName() + " nbRow:" + getAnnotations().length + " try access row:" + indexRow);
        }
        if (getAnnotations()[0].length < indexCol) {
            System.err.println(this.getName() + " nbCol:" + getAnnotations()[0].length + " try access col:" + indexCol);
        }
        return getAnnotations()[indexRow][indexCol] = annot;
    }

    public String getFirstRowName() {
        return firstRowName;
    }

    public void setFirstRowName(String firstRowName) {
        this.firstRowName = firstRowName;
    }

    public String getSecondRowName() {
        return secondRowName;
    }

    public void setSecondRowName(String secondRowName) {
        this.secondRowName = secondRowName;
    }

    public double getValue(int i, int j) {
        if (!isOrdered())
            return getValues()[i][j];
        else {
            String rowName = orderedRowNames.get(i);
            if (getValues().length < rowNames.get(rowName)) {
                System.err.println(
                        this.getName() + " nbRow:" + getValues().length + " try access row:" + rowNames.get(rowName));
            }
            if (getValues()[0].length < j) {
                System.err.println(this.getName() + " nbCol:" + getValues()[0].length + " try access col:" + j);
            }
            return getValues()[rowNames.get(rowName)][j];
        }
    }

    public String getValueAnnotation(int i, int j) {
        if (!isOrdered())
            return getAnnotations()[i][j];
        else {
            String rowName = orderedRowNames.get(i);
            if (getAnnotations().length < rowNames.get(rowName)) {
                System.err.println(this.getName() + " nbRow:" + getAnnotations().length + " try access row:"
                        + rowNames.get(rowName));
            }
            if (getAnnotations()[0].length < j) {
                System.err.println(this.getName() + " nbCol:" + getAnnotations()[0].length + " try access col:" + j);
            }
            return getAnnotations()[rowNames.get(rowName)][j];
        }
    }

    public int getNumberRow() {
        // test first if rowNames and values have the same size
        // System.out.println(values.length + " "+ rowNames.size());
        if (values.length != rowNames.size())
            System.out.println("Problem in the number of rows of this ExpressionMatrix");
        return values.length;
    }

    /**
     * Get the number of column of the <code>values</code> array
     * 
     * @return
     */
    public int getNumberColumn() {
        if (values[0].length != headers.size())
            System.out.println("Problem in the number of columns of this ExpressionMatrix");
        return values[0].length;
    }

    /**
     * Get the number of column of the <code>values</code> array + <code>annotations</code> array
     * 
     * @return
     */
    public int getNumberColumnWithAnnotation() {
        if (values[0].length != headers.size())
            System.out.println("Problem in the number of columns of this ExpressionMatrix");
        if (annotations.length == 0)
            return values[0].length;
        else
            return values[0].length + annotations[0].length;
    }

    /*
     * ******************************************************* Row manager part
     * *******************************************************
     */
    /**
     * Add a row at the end of values
     */
    public void addRow(String rowName, double[] row) {
        int previousLength = values.length;
        values = ArrayUtils.addRow(values, row);
        if (headerAnnotation.size() != 0) {
            String[] newAnnotation = new String[headerAnnotation.size()];
            for (int i = 0; i < newAnnotation.length; i++)
                newAnnotation[i] = " ";
            annotations = ArrayUtils.addRow(annotations, newAnnotation, annotations.length);
        }
        int newLentgh = values.length;
        // we test if the adding was performed: Otherwise we do not add an element to
        // rowNames
        if (previousLength < newLentgh)
            rowNames.put(rowName, values.length - 1);
        else
            System.err.println("Row was not added");
    }

    public void copyRowAt(String rowName, double[] row) {
        if (rowNames.containsKey(rowName) && row.length == values[0].length) {
            int index = rowNames.get(rowName);
            for (int j = 0; j < this.getNumberColumn(); j++) {
                values[index][j] = row[j];
            }
        } else {
            System.out.println(
                    "Impossible to add this row: rowName as not been found, or row has not the same size than values column");
        }
    }

    public double[] getRow(String rowName) {
        int index = rowNames.get(rowName);
        return ArrayUtils.getRow(values, index);
    }

    public double[] getSubRow(String rowName, ArrayList<String> colNames) {
        double[] subRow = new double[colNames.size()];
        double[] row = getRow(rowName);
        int i = 0;
        for (String colName : colNames) {
            subRow[i] = row[headers.indexOf(colName)];
            i++;
        }
        return subRow;
    }

    public String getRowName(int i) {
        for (String key : rowNames.keySet()) {
            Integer value = rowNames.get(key);
            if (value == i)
                return key;
        }
        return null;
    }

    public void sort(int colIndex) {
        ArrayList<String> orderedRowNames = new ArrayList<String>();
        double[][] values = this.getValues();
        double[][] valuesSorted = ArrayUtils.sortColumn(values, colIndex);

        for (int i = 0; i < valuesSorted.length; i++) {
            double[] row = ArrayUtils.getRow(valuesSorted, i);
            // search where is this value in the non sorted double[][]
            int index = -1;
            boolean found = false;
            for (int k = 0; k < values.length && !found; k++) {
                // compare row to current row
                found = true;
                for (int j = 0; j < row.length && found; j++) {
                    if (row[j] != values[k][j]) {
                        found = false;
                    }
                }
                if (found) {
                    index = k;
                    String rowName = this.getRowName(index);
                    if (!orderedRowNames.contains(rowName)) {
                        orderedRowNames.add(rowName);
                        found = true;
                    }
                }
            }
            if (!found) {
                System.err.println("Problem when sorting matrix! The value was not found");
            }
            // if(i%100==0) System.out.println(i);
        }

        this.setOrdered(true);
        this.setOrderedRowNames(orderedRowNames);

    }

    /**
     * TO IMPROVE<br>
     * <li>Sort double[][] values
     * <li>Using sorted array, reorder rows through OrderedRowNames
     * 
     * @param colIndex
     * @param ascending
     */
    public ExpressionMatrix sort(int colIndex, boolean ascending) {
        // ArrayList<String> orderedRowNames = new ArrayList<String>();
        // double[][] values = this.getValues();
        // double[][] valuesSorted = ArrayUtils.sortColumn(values, colIndex);
        // ArrayList<String> rowNames = this.getRowNamesToList();
        //
        // for(int i=0;i<valuesSorted.length;i++){
        // double value = valuesSorted[i][colIndex];
        // // search where is this value in the non sorted double[][]
        // int index = -1;
        // boolean found = false;
        // //System.out.println(value);
        // for(String rowName : rowNames){
        // if(!orderedRowNames.contains(rowName) && !found){
        // // compare value to current value
        // double currentValue = values[this.getRowNames().get(rowName)][colIndex];
        // if(value == currentValue){
        // //System.out.println(rowName);
        // orderedRowNames.add(rowName);
        // found = true;
        // }
        // }
        // }
        // if(!found){
        // System.err.println("Problem when sorting matrix! The value was not found");
        // }
        // //if(i%100==0) System.out.println(i);
        // }
        //
        // this.setOrdered(true);
        // this.setOrderedRowNames(orderedRowNames);

        String[][] arrayTemp = toArray(firstRowName);
        String[] header = ArrayUtils.getRow(arrayTemp, 0);
        arrayTemp = ArrayUtils.deleteRow(arrayTemp, 0);
        boolean sortNumbers = true;
        if (colIndex > getHeaders().size())
            sortNumbers = false;

        final int d = colIndex; // 0 <= d <= n-1
        final boolean ascendingSort = ascending;
        if (sortNumbers) {
            Arrays.sort(arrayTemp, new Comparator<String[]>() {
                @Override
                public int compare(String[] o1, String[] o2) {
                    double value1 = Double.valueOf(o1[d]);
                    double value2 = Double.valueOf(o2[d]);
                    if (ascendingSort)
                        return Double.compare(value1, value2);
                    else
                        return -Double.compare(value1, value2);
                }
            });
        } else {
            Arrays.sort(arrayTemp, new Comparator<String[]>() {
                @Override
                public int compare(String[] o1, String[] o2) {
                    String value1 = o1[d];
                    String value2 = o2[d];
                    if (ascendingSort)
                        return value1.compareTo(value2);
                    else
                        return -value1.compareTo(value2);
                }
            });
        }

        /*
         * Add headers
         */
        String[][] arrayFinal = new String[arrayTemp.length + 1][arrayTemp[0].length];
        for (int j = 0; j < arrayFinal[0].length; j++)
            arrayFinal[0][j] = header[j];
        for (int i = 1; i < arrayFinal.length; i++) {
            for (int j = 0; j < arrayFinal[0].length; j++)
                arrayFinal[i][j] = arrayTemp[i - 1][j];
        }

        return ExpressionMatrix.arrayToExpressionMatrix(arrayFinal, true);

    }

    /*
     * ******************************************************* Column manager part
     * *******************************************************
     */

    /**
     * Add a column at the end of Matrix
     */
    public void addColumn(String colName, double[] column) {
        int previousLength = getValues()[0].length;
        setValues(ArrayUtils.addColumn(values, column));
        int newLentgh = getValues()[0].length;
        // we test if the adding was performed: Otherwise we do not add an element to
        // header
        if (previousLength < newLentgh)
            headers.add(colName);
        else
            System.err.println("Column was not added");
    }

    public void copyColumnAt(String colName, double[] column) {
        if (headers.contains(colName) && column.length == values.length) {
            int index = headers.indexOf(colName);
            for (int i = 0; i < values.length; i++) {
                values[i][index] = column[i];
            }
        } else {
            System.out.println(
                    "Impossible to add this column: colName has not been found, or column has not the same size than values column");
        }
    }

    /**
     * Extract the column at this position
     * 
     * @param colName
     * @return
     */
    public double[] getColumn(String colName) {
        int index = headers.indexOf(colName);
        return ArrayUtils.getColumn(values, index);
    }

    public double[] getColumn(int j) {
        return ArrayUtils.getColumn(values, j);
    }

    public void deleteColumn(String colName) {
        int i = headers.indexOf(colName);
        this.setValues(ArrayUtils.deleteColumn(values, i));
        this.getHeaders().remove(i);
    }

    /*
     * ******************************************************* SubMatrix part
     * *******************************************************
     */
    public ExpressionMatrix getSubMatrix(ArrayList<String> rowNames, ArrayList<String> colNames) {
        ExpressionMatrix subExpr = getSubMatrixRow(rowNames);
        return subExpr.getSubMatrixColumn(colNames);
    }

    /**
     * Provide a list of rowName and extract them from the ExpressionMatrix
     * 
     * @param includeRowNames is an ArrayList<String> so it is first converted into a TreeSet to avoid
     *        duplicates and then the row extraction algorithm is ran
     * @return
     */
    public ExpressionMatrix getSubMatrixRow(ArrayList<String> includeRowNames) {
        TreeSet<String> includeRowNamesSet = new TreeSet<String>();
        for (String rowName : includeRowNames)
            includeRowNamesSet.add(rowName);
        return getSubMatrixRow(includeRowNamesSet);
    }

    /**
     * Provide a list of rowName and extract them from the ExpressionMatrix
     * 
     * @param includeRowNames is a TreeSet<String>
     * @return
     */
    public ExpressionMatrix getSubMatrixRow(TreeSet<String> includeRowNames) {
        ExpressionMatrix subExpr = new ExpressionMatrix(headers);
        subExpr.firstRowName = this.firstRowName;
        subExpr.setName(this.getName());
        subExpr.setBioCondName(this.getBioCondName());
        subExpr.setDate(this.getDate());
        subExpr.setNote(this.getNote());

        // set ordered if necessary
        subExpr.setOrdered(this.isOrdered());
        if (this.getOrderedRowNames().size() != 0) {
            ArrayList<String> newOrderedRowNames = new ArrayList<String>();
            for (String rowNames : this.getOrderedRowNames()) {
                if (includeRowNames.contains(rowNames)) {
                    newOrderedRowNames.add(rowNames);
                }
            }
            subExpr.setOrderedRowNames(newOrderedRowNames);
        }

        // create RowNames
        int k = 0;
        for (String rowName : includeRowNames) {
            if (this.getRowNames().containsKey(rowName)) {
                subExpr.getRowNames().put(rowName, k);
                k++;
            } else {
                // System.out.println(rowName);
            }
        }
        // fill submatrix
        double[][] newValues = new double[subExpr.getRowNames().size()][getNumberColumn()];
        subExpr.setValues(newValues);
        for (String rowName : subExpr.getRowNames().keySet()) {
            subExpr.copyRowAt(rowName, this.getRow(rowName));
        }

        // take care of annotation
        if (headerAnnotation.size() != 0) {
            subExpr.setHeaderAnnotation(headerAnnotation);
            String[][] annotationNew = new String[subExpr.getRowNames().size()][headerAnnotation.size()];
            for (String rowName : includeRowNames) {
                if (this.getRowNames().containsKey(rowName)) {
                    for (int i = 0; i < headerAnnotation.size(); i++) {
                        annotationNew[subExpr.getRowNames().get(rowName)][i] =
                                this.getValueAnnotation(rowName, headerAnnotation.get(i));
                    }
                }
            }
            subExpr.setAnnotations(annotationNew);
        }

        return subExpr;
    }

    /**
     * Generate a subMatrix with random rows
     * 
     * @param proportion indicate of much of the original rows will be include in the randomized matrix
     * @return
     */
    public ExpressionMatrix getSubMatrixRowRandom(double proportion) {
        System.out.println("Randomize ExpressionMatrix");
        int nbRow = (int) (proportion * (double) this.getNumberRow());
        System.out.println(nbRow);
        TreeSet<String> includeRowNamesSet = new TreeSet<String>();
        ArrayList<String> rowNames = this.getRowNamesToList();
        for (int i = 0; i < nbRow; i++) {
            if (i % 5000 == 0)
                System.out.println("random: " + i);
            int pos = (int) (Math.random() * (double) this.getNumberRow());
            String rowName = rowNames.get(pos);
            // System.out.println(i+" "+rowName);
            includeRowNamesSet.add(rowName);
        }
        ArrayList<String> includeRowNames = new ArrayList<String>();
        for (String rowName : includeRowNamesSet)
            includeRowNames.add(rowName);
        ExpressionMatrix subMatrix = this.getSubMatrixRow(includeRowNames);
        System.out.println("ExpressionMatrix random created");
        return subMatrix;
    }

    /**
     * Extract a matrix with only selected columns
     * 
     * @param includeColNames columns to include
     * @return
     */
    public ExpressionMatrix getSubMatrixColumn(ArrayList<String> includeColNames) {
        ExpressionMatrix subExpr = new ExpressionMatrix();
        subExpr.setName(this.getName());
        subExpr.setDate(this.getDate());
        subExpr.setNote(this.getNote());
        subExpr.setType(this.getType());
        subExpr.setBioCondName(this.getBioCondName());

        if (isOrdered()) {
            subExpr.setOrdered(isOrdered(), orderedRowNames);
        }

        // create header
        for (String colName : includeColNames) {
            if (this.getHeaders().contains(colName)) {
                subExpr.addHeader(colName);
            }
        }
        // fill the ExpressionMatrix
        subExpr.setRowNames(this.getRowNames());
        double[][] newValues = new double[getNumberRow()][subExpr.getHeaders().size()];
        subExpr.setValues(newValues);
        for (String colName : includeColNames) {
            if (this.getHeaders().contains(colName)) {
                subExpr.copyColumnAt(colName, this.getColumn(colName));
            }

        }

        // take care of annotation
        if (headerAnnotation.size() != 0) {
            // create new header annotation
            ArrayList<String> headerAnnotationTemp = new ArrayList<String>();
            for (String colName : includeColNames) {
                if (this.getHeaderAnnotation().contains(colName)) {
                    headerAnnotationTemp.add(colName);
                }
            }
            subExpr.setHeaderAnnotation(headerAnnotationTemp);
            String[][] annotationNew = new String[getNumberRow()][headerAnnotationTemp.size()];
            for (String rowName : subExpr.getRowNames().keySet()) {
                for (int i = 0; i < headerAnnotationTemp.size(); i++) {
                    annotationNew[subExpr.getRowNames().get(rowName)][i] =
                            this.getValueAnnotation(rowName, headerAnnotationTemp.get(i));

                }
            }
            subExpr.setAnnotations(annotationNew);
        }

        return subExpr;
    }

    /**
     * Return a subMatrix containing only rows without Missingvalues
     * 
     * @return
     */
    public ExpressionMatrix getMissingValuesFreeMatrix() {
        ArrayList<String> includeRowNames = new ArrayList<String>();
        TreeSet<String> excludeRowNames = new TreeSet<String>();
        for (String rowName : rowNames.keySet()) {
            includeRowNames.add(rowName);
            for (String colName : headers) {
                if (getValue(rowName, colName) == MISSING_VALUE) {
                    excludeRowNames.add(rowName);
                }
            }
        }

        for (String rowName : excludeRowNames)
            includeRowNames.remove(rowName);
        return getSubMatrixRow(includeRowNames);

    }

    public ExpressionMatrix transpose() {
        ExpressionMatrix matrixT = new ExpressionMatrix();
        double[][] values = new double[getNumberColumn()][getNumberRow()];
        matrixT.setValues(values);
        if (ordered) {
            for (String colName : headers)
                matrixT.getOrderedRowNames().add(colName);
            matrixT.setOrdered(true);
            for (String rowName : orderedRowNames) {
                int i = 0;
                matrixT.addHeader(rowName);
                for (String colName : headers) {
                    matrixT.getRowNames().put(colName, i);
                    matrixT.setValue(getValue(rowName, colName), colName, rowName);
                    i++;
                }
            }
        } else {
            for (String rowName : rowNames.keySet()) {
                int i = 0;
                matrixT.addHeader(rowName);
                for (String colName : headers) {
                    matrixT.getRowNames().put(colName, i);
                    matrixT.setValue(getValue(rowName, colName), colName, rowName);
                    i++;
                }
            }
        }
        return matrixT;
    }

    /**
     * Create a fusion of a list of ExpressionMatrix
     * 
     * @param matrices
     * @param intersection say if we want the intersection of all rows
     * @return resulting ExpressionMatrix
     */
    public static ExpressionMatrix merge(ArrayList<ExpressionMatrix> matrices, boolean intersection) {
        System.out.println("Do a fusion of " + matrices.size() + " matrices");
        ExpressionMatrix finalMatrix = new ExpressionMatrix();
        // first fill the header
        for (ExpressionMatrix matrix : matrices) {
            System.out.println(matrix.getName());
            for (String header : matrix.getHeaders()) {
                if (!finalMatrix.getHeaders().contains(header)) {
                    finalMatrix.getHeaders().add(header);
                }
            }
        }
        // first fill header annotation
        TreeSet<String> headerAnnot = new TreeSet<String>();
        for (ExpressionMatrix matrix : matrices) {
            for (String header : matrix.getHeaderAnnotation()) {
                if (!finalMatrix.getHeaderAnnotation().contains(header)) {
                    headerAnnot.add(header);
                }
            }

        }
        for (String header : headerAnnot) {
            finalMatrix.headerAnnotation.add(header);
        }

        String[] tempRowNames = new String[matrices.get(0).getRowNames().size()];
        if (intersection) { // find the intersection of all rownames
            int i = 0;
            for (String rowName : matrices.get(0).getRowNames().keySet()) {
                tempRowNames[i] = rowName;
                i++;
            }
            for (int j = 0; j < tempRowNames.length; j++) {
                for (ExpressionMatrix matrix : matrices) {
                    if (!matrix.getRowNames().containsKey(tempRowNames[j])) {
                        // System.out.println(matrices.indexOf(matrix));
                        tempRowNames[j] = "Delete";
                    }
                }

            }

        } else { // find the union of all rowNames
            TreeSet<String> curatedRowNamesSet = new TreeSet<String>();
            for (ExpressionMatrix matrix : matrices) {
                for (String rowName : matrix.getRowNames().keySet())
                    curatedRowNamesSet.add(rowName);
            }
            tempRowNames = new String[curatedRowNamesSet.size()];
            int i = 0;
            for (String rowName : curatedRowNamesSet) {
                tempRowNames[i] = rowName;
                i++;
            }
        }

        int i = 0;
        for (String rowName : tempRowNames) {
            if (!rowName.equals("Delete")) {
                finalMatrix.getRowNames().put(rowName, i);
                i++;
            }
        }
        System.out.println("Found " + i + " rows, and " + finalMatrix.getHeaders().size() + " columns");
        // create values
        double[][] values = new double[finalMatrix.getRowNames().size()][finalMatrix.getHeaders().size()];
        for (int k = 0; k < values.length; k++) {
            for (int j = 0; j < values[0].length; j++) {
                values[k][j] = MISSING_VALUE;
            }
        }
        finalMatrix.setValues(values);

        // create annotation
        String[][] annot = new String[finalMatrix.getRowNames().size()][finalMatrix.headerAnnotation.size()];
        for (int k = 0; k < annot.length; k++) {
            for (int j = 0; j < annot[0].length; j++) {
                annot[k][j] = "";
            }
        }
        finalMatrix.setAnnotations(annot);

        // then fill values
        for (String rowName : finalMatrix.getRowNames().keySet()) {
            for (String header : finalMatrix.getHeaders()) {
                for (ExpressionMatrix matrix : matrices) {
                    if (matrix.getRowNames().containsKey(rowName) && matrix.getHeaders().contains(header)) {
                        // System.out.println(rowName+" "+header);
                        // System.out.println("value: "+matrix.getValue(rowName, header));
                        finalMatrix.setValue(matrix.getValue(rowName, header), rowName, header);
                    }
                }
            }
            for (String header : finalMatrix.getHeaderAnnotation()) {
                for (ExpressionMatrix matrix : matrices) {
                    if (matrix.getRowNames().containsKey(rowName) && matrix.getHeaderAnnotation().contains(header)) {
                        finalMatrix.setValueAnnotation(matrix.getValueAnnotation(rowName, header), rowName, header);
                    }
                }

            }
        }
        // System.out.println("Finish fusion");
        return finalMatrix;

    }

    /*
     * ******************************************************* Import manager part
     * *******************************************************
     */
    public static ExpressionMatrix arrayToExpressionMatrix(String[][] array, boolean ordered, String name) {
        ExpressionMatrix matrix = arrayToExpressionMatrix(array, ordered);
        matrix.setName(name);
        return matrix;
    }

    public static ExpressionMatrix arrayToExpressionMatrix(String[][] array, boolean ordered) {
        ArrayList<String> headers = new ArrayList<String>();
        ArrayList<String> headerAnnotation = new ArrayList<String>();
        boolean annotationReached = false;
        for (int j = 1; j < array[0].length; j++) {
        	int nbVoid = 0;
            for (int i = 1; i < Math.min(10, array.length); i++) {
                try {
                    /*
                     * We search the all column for non void Cell, then we check if it's a Double or a String
                     */
                    // int i = 1;
                    if (!array[i][j].equals("") && !array[i][j].equals("null")) {
                        @SuppressWarnings("unused")
                        double value = Double.parseDouble(array[i][j]);

                        // System.out.println("null value at position: "+i+" : "+j+" "+);
                    } else nbVoid += 1;
                    // System.out.println("First value of column: "+j+" at line "+i+" Value:
                    // "+array[0][j]);
                    // double value = Double.parseDouble(array[i][j]);
                    // headers.add(array[0][j]);
                } catch (Exception e) {
                    // headerAnnotation.add(array[0][j]);
                    // as soon as we reach a row not containing numbers we are in the "annotation"
                    // part
                    annotationReached = true;
                }
            }
            if (!annotationReached) {
            	if (nbVoid == Math.min(10, array.length)-1) {
            		headerAnnotation.add(array[0][j]);
            	} else headers.add(array[0][j]);
            } else {
                headerAnnotation.add(array[0][j]);
            }

        }
        ExpressionMatrix matrix = new ExpressionMatrix(headers);
        matrix.setHeaderAnnotation(headerAnnotation);
        matrix.firstRowName = array[0][0];
        matrix.setName("");
        matrix.setOrdered(ordered);

        /*
         * Fill values array
         */
        double[][] values = new double[array.length - 1][headers.size()];
        for (int i = 0; i < values.length; i++) {
            if (!matrix.getRowNames().containsKey(array[i + 1][0])) {
                for (int j = 0; j < values[0].length; j++) {
                    if (array[i + 1][j + 1].equals("") || array[i + 1][j + 1].equals("null")) {
                        values[i][j] = MISSING_VALUE;
                    } else {
                        if (array[i + 1][j + 1].contains("inf"))
                            values[i][j] = 0;
                        else if (array[i + 1][j + 1].contains("NA"))
                            values[i][j] = 0;
                        else 
                            values[i][j] = Double.parseDouble(array[i + 1][j + 1]);
                    }
                }
                matrix.getRowNames().put(array[i + 1][0], i);
                if (ordered)
                    matrix.orderedRowNames.add(array[i + 1][0]);
            }
        }

        matrix.setValues(values);

        /*
         * Fill annotation array
         */
        if (headerAnnotation.size() != 0) {
            String[][] annotations = new String[array.length - 1][headerAnnotation.size()];
            for (int i = 0; i < annotations.length; i++) {
                for (int j = 0; j < annotations[0].length; j++) {
                    annotations[i][j] = array[i + 1][j + 1 + headers.size()];
                }
            }
            matrix.setAnnotations(annotations);
        }
        return matrix;
    }

    public static ArrayList<ExpressionMatrix> arrayToExpressionMatrixList(String[][] array) {
        ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
        int begin = 1;
        int end = 1;
        String rowName = array[1][0];
        for (int i = 1; i < array.length; i++) {
            if (!rowName.equals(array[i][0]) || i == array.length - 1) {
                end = i - 1;
                if (i == array.length - 1)
                    end = i;
                String[][] arrayTemp = ArrayUtils.subArray(array, begin, end);
                ExpressionMatrix matrix = ExpressionMatrix.arrayToExpressionMatrix(arrayTemp, false);
                matrix.firstRowName = matrix.getName();
                matrix.setNote(rowName);
                matrices.add(matrix);
                begin = i;
                rowName = array[i][0];
            }
        }
        return matrices;

    }

    /**
     * Load an ExpressionMatrix from a Tabdelimited text file
     * 
     * @param fileName
     * @param ordered
     * @return
     * @throws IOException
     */
    public static ExpressionMatrix loadTab(String fileName, boolean ordered) {
        File file = new File(fileName);
        if (file.exists()) {
            String[][] array = TabDelimitedTableReader.read(new File(fileName));
            String name = FileUtils.removeExtensionAndPath(fileName);
            if (array.length == 0)
                return null;
            return ExpressionMatrix.arrayToExpressionMatrix(array, ordered, name);
        } else {
            return null;
        }
    }

    public static ArrayList<ExpressionMatrix> readFromFileList(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            String[][] array = TabDelimitedTableReader.read(new File(fileName));
            if (array.length == 0)
                return null;
            return ExpressionMatrix.arrayToExpressionMatrixList(array);
        } else {
            return null;
        }
    }

    /*
     * ******************************************************* Export manager part
     * *******************************************************
     */

    /**
     * Transform an ExpressionMatrix to a String[][]
     * 
     * @param firstRowName Name of the row name column
     * @return
     */
    public String[][] toArray(String firstRowName) {
        return toArray(firstRowName, true, true);
    }

    /**
     * Transform an ExpressionMatrix to a String[][]
     * 
     * @param firstRowName Name of the row name column
     * @param header Export the header ?
     * @param annotation Export the annotation ?
     * @return
     */
    public String[][] toArray(String firstRowName, boolean header, boolean annotation) {
        if (annotation) {
            String[][] array = new String[rowNames.size()][headers.size() + headerAnnotation.size() + 1];
            int i = 0;
            // write headers info if header is true
            if (header) {
                array = new String[rowNames.size() + 1][headers.size() + headerAnnotation.size() + 1];
                array[0][0] = firstRowName;
                for (int k = 0; k < headers.size(); k++) {
                    array[0][k + 1] = headers.get(k);
                }
                i = 1;
                // write annotation info
                for (int k = 0; k < headerAnnotation.size(); k++) {
                    array[0][k + 1 + headers.size()] = headerAnnotation.get(k);
                }
            }

            // write data values
            for (String keyTemp : rowNames.keySet()) {
                String key = keyTemp;
                int k = i;
                if (header)
                    k = i - 1;
                if (isOrdered())
                    key = orderedRowNames.get(k);
                array[i][0] = String.valueOf(key);
                for (int j = 0; j < headers.size(); j++) {
                    if (this.getValue(key, headers.get(j)) == MISSING_VALUE) {
                        array[i][j + 1] = "";
                    } else
                        array[i][j + 1] = String.valueOf(this.getValue(key, headers.get(j)));
                }
                // write annotation info
                for (int j = 0; j < headerAnnotation.size(); j++) {
                    array[i][headers.size() + 1 + j] = this.getValueAnnotation(key, headerAnnotation.get(j));
                }
                i++;
            }
            return array;
        } else {
            String[][] array = new String[rowNames.size()][headers.size()];
            int i = 0;

            // write headers info if header is true
            if (header) {
                array = new String[rowNames.size() + 1][headers.size() + 1];
                array[0][0] = firstRowName;
                for (int k = 0; k < headers.size(); k++) {
                    array[0][k + 1] = headers.get(k);
                }
                i = 1;
            }

            // write data values
            for (String keyTemp : rowNames.keySet()) {
                String key = keyTemp;
                int k = i;
                if (header)
                    k = i - 1;
                if (isOrdered())
                    key = orderedRowNames.get(k);
                array[i][0] = String.valueOf(key);
                for (int j = 0; j < headers.size(); j++) {
                    if (this.getValue(key, headers.get(j)) == MISSING_VALUE) {
                        array[i][j + 1] = "";
                    } else
                        array[i][j + 1] = String.valueOf(this.getValue(key, headers.get(j)));
                }
                i++;
            }
            return array;
        }
    }

    /**
     * Save in a TabDelimited text file
     * 
     * @param fileName
     * @param firstRowName
     */
    public void saveTab(String fileName, String firstRowName) {
        TabDelimitedTableReader.save(this.toArray(firstRowName), fileName);
    }

    public static ArrayList<ExpressionMatrix> setFirstandSecondRowNameInList(ArrayList<ExpressionMatrix> listExpression,
            String firstRownName, String secondRownName) {
        for (ExpressionMatrix expr : listExpression) {
            expr.setFirstRowName(firstRownName);
            expr.setSecondRowName(secondRownName);
        }
        return listExpression;
    }

    /**
     * Read compressed, serialized data with a FileInputStream. Uncompress that data with a
     * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream. Replace current data
     * with new data, and redraw everything.
     */
    public static ExpressionMatrix load(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                // Create necessary input streams
                FileInputStream fis = new FileInputStream(fileName); // Read from file
                GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
                ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
                // Read in an object. It should be a vector of scribbles
                ExpressionMatrix matrix = (ExpressionMatrix) in.readObject();
                in.close();
                matrix.setLoaded(true);
                return matrix;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } // Close the stream.

    }

    /**
     * Read compressed, serialized data with a FileInputStream. Uncompress that data with a
     * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream. Replace current data
     * with new data, and redraw everything.
     */
    public void load() {
        System.out.println("load transcriptomics data : " +OmicsData.PATH_STREAMING + this.getName() + EXTENSION);
        ExpressionMatrix matrixLoaded = ExpressionMatrix.load(OmicsData.PATH_STREAMING + this.getName() + EXTENSION);
        this.setAnnotations(matrixLoaded.getAnnotations());
        this.setBioCondName(matrixLoaded.getBioCondName());
        this.setDate(matrixLoaded.getDate());
        this.setHeaderAnnotation(matrixLoaded.getHeaderAnnotation());
        this.setHeaders(matrixLoaded.getHeaders());
        this.setFirstRowName(matrixLoaded.getFirstRowName());
        this.setSecondRowName(matrixLoaded.getSecondRowName());
        this.setName(matrixLoaded.getName());
        this.setNote(matrixLoaded.getNote());
        this.setOrdered(matrixLoaded.isOrdered(), matrixLoaded.getOrderedRowNames());
        this.setRawDatas(matrixLoaded.getRawDatas());
        this.setRowNames(matrixLoaded.getRowNames());
        this.setValues(matrixLoaded.getValues());
        this.setLoaded(true);
    }

    /**
     * Serialize the vector of lines with an ObjectOutputStream. Compress the serialized objects with a
     * GZIPOutputStream. Write the compressed, serialized data to a file with a FileOutputStream. Don't
     * forget to flush and close the stream.
     */
    public void save(String fileName) {
        try {
            // Create the necessary output streams to save the scribble.
            FileOutputStream fos = new FileOutputStream(fileName);
            // Save to file
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            // Compressed
            ObjectOutputStream out = new ObjectOutputStream(gzos);
            // Save objects
            out.writeObject(this); // Write the entire Vector of scribbles
            out.flush(); // Always flush the output.
            out.close(); // And close the stream.
        }
        // Print out exceptions. We should really display them in a dialog...
        catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Serialize the vector of lines with an ObjectOutputStream. Compress the serialized objects with a
     * GZIPOutputStream. Write the compressed, serialized data to a file with a FileOutputStream. Don't
     * forget to flush and close the stream.
     */
    public void save() {
        String fileName = OmicsData.PATH_STREAMING + this.getName() + EXTENSION;
        save(fileName);
    }

    /**
     * Export in an array a List of ExpressionMatrix All ExpressionMatrix from the list are supposed to
     * have the same headers, and thus the same number of column
     * 
     * The first column = the annotation of each ExpressionMatrix The second column = the RowNames of
     * each ExpressionMatrix
     * 
     * @param firstRowName
     * @param secondRowName
     * @param listExpression
     * @return
     */
    public static String[][] toArrayList(String firstRowName, String secondRowName,
            ArrayList<ExpressionMatrix> listExpression) {
        String[][] array = new String[ExpressionMatrix.getTotalRowSize(listExpression)
                + 1][listExpression.get(0).headers.size() + 2 + listExpression.get(0).headerAnnotation.size()];
        // first we right Array
        array[0][0] = firstRowName;
        array[0][1] = secondRowName;
        for (int i = 0; i < listExpression.get(0).getHeaders().size(); i++) {
            array[0][i + 2] = listExpression.get(0).getHeader(i);
        }
        // write annotation info
        for (int i = 0; i < listExpression.get(0).headerAnnotation.size(); i++) {
            array[0][i + 2 + listExpression.get(0).getHeaders().size()] = listExpression.get(0).headerAnnotation.get(i);
        }
        int i = 1;
        for (ExpressionMatrix expr : listExpression) {
            for (String key : expr.getRowNames().keySet()) {
                array[i][0] = expr.getFirstRowName();
                array[i][1] = key;
                for (int j = 0; j < expr.getHeaders().size(); j++) {
                    if (expr.getValue(key, expr.getHeader(j)) == MISSING_VALUE) {
                        array[i][j + 2] = "";
                    } else
                        array[i][j + 2] = String.valueOf(expr.getValue(key, expr.getHeader(j)));
                }
                // write annotation info
                for (int j = 0; j < expr.getHeaderAnnotation().size(); j++) {
                    array[i][j + 2 + expr.getHeaders().size()] =
                            expr.getValueAnnotation(key, expr.getHeaderAnnotation().get(j));
                }
                i++;
            }
        }
        return array;
    }

    public static void saveToFileList(String fileName, String firstRowName, String secondRowName,
            ArrayList<ExpressionMatrix> listExpression, boolean append, boolean addComment) {
        String[][] finalArray = toArrayList(firstRowName, secondRowName, listExpression);
        TabDelimitedTableReader.save(finalArray, fileName, append, addComment);
    }

    public static void saveToFileList(String fileName, String firstRowName, String secondRowName,
            ArrayList<ExpressionMatrix> listExpression) {
        saveToFileList(fileName, firstRowName, secondRowName, listExpression, false, false);
    }

    /*
     * ******************************************************* Utils part
     * *******************************************************
     */
    public static ExpressionMatrix createStochasticMatrix(int n, int p) {
        ArrayList<String> headers = new ArrayList<String>();
        for (int i = 0; i < p; i++) {
            headers.add("Variable_" + i);
        }
        ExpressionMatrix stochast = new ExpressionMatrix(headers);
        double[][] values = new double[n][p];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                values[i][j] = (Math.random() - 0.5) * 10;
                stochast.getRowNames().put("row_" + i, i);
            }
        }
        stochast.setValues(values);
        stochast.firstRowName = "Stochast";
        return stochast;
    }

    /**
     * Count the total Number of row if we concatenate all ExpressionMatrix in the ArrayList
     * 
     * @param listExpression
     * @return
     */
    public static int getTotalRowSize(ArrayList<ExpressionMatrix> listExpression) {
        int count = 0;
        for (ExpressionMatrix expr : listExpression) {
            count += expr.getNumberRow();
        }
        return count;
    }

    /**
     * Return the number of column of the first ExpressionMatrix
     * 
     * @param listExpression
     * @return
     */
    public static int getTotalColumnSize(ArrayList<ExpressionMatrix> listExpression) {
        return listExpression.get(0).getNumberColumn();
    }

    /**
     * Create a matrix containing only one element = MISSING_VALUE
     * 
     * @return
     */
    public static ExpressionMatrix getVoidMatrix() {
        ExpressionMatrix matrix = new ExpressionMatrix("Element", 1);
        matrix.getRowNames().put("No Element to Display", 0);
        matrix.setValue(MISSING_VALUE, 0, 0);
        return matrix;
    }

    @Override
    public ExpressionMatrix clone() {
        try {
            ExpressionMatrix newMatrix = (ExpressionMatrix) super.clone();

            newMatrix.setHeaders(ArrayUtils.clone(this.headers));
            newMatrix.setValues(ArrayUtils.clone(this.getValues()));
            newMatrix.setRowNames(ArrayUtils.clone(this.getRowNames()));
            newMatrix.setNote(this.getNote());
            newMatrix.setFirstRowName(this.firstRowName);
            newMatrix.setSecondRowName(this.secondRowName);
            if (this.getHeaderAnnotation().size() != 0) {
                newMatrix.setAnnotations(ArrayUtils.clone(this.getAnnotations()));
                newMatrix.setHeaderAnnotation(ArrayUtils.clone(this.getHeaderAnnotation()));
            }
            newMatrix.setOrdered(this.isOrdered());
            if (this.isOrdered()) {
                newMatrix.setOrderedRowNames(ArrayUtils.clone(this.getOrderedRowNames()));
            }
            return newMatrix;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error when cloning ExpressionMatrix!");
            return this;
        }
    }

    /**
     * I create this comparator in the case where the key are Integer, we use then the Integer
     * comparator instead of String --- Because with String normal comparator 1,2,3,10,12 will be
     * classified as 1,10,12,2,3 ---
     * 
     * @author Chris
     *
     */
    class ExpressionMatrixCompare implements Comparator<String>, Serializable {
        /**
        * 
        */
        private static final long serialVersionUID = -2421644173305918367L;

        public int compare(String s1, String s2) {
            try {
                Integer int1 = Integer.valueOf(s1);
                Integer int2 = Integer.valueOf(s2);
                return int1.compareTo(int2);
            } catch (NumberFormatException ex) {
                return s1.compareTo(s2);
            }

        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

}
