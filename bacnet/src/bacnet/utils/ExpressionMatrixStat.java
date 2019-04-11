package bacnet.utils;

import java.util.ArrayList;
import bacnet.datamodel.dataset.ExpressionMatrix;

/**
 * List of method for doing Stat on ExpressionMatrices
 * 
 *
 * @author Christophe Bécavin
 *
 */
public class ExpressionMatrixStat {

    /**
     * Apply a filter to an ExpressionMatrix
     * 
     * @param matrix
     * @param filter
     * @return
     */
    public static ExpressionMatrix filter(ExpressionMatrix matrix, Filter filter) {
        ExpressionMatrix filteredMatrix = new ExpressionMatrix();
        if (filter.isFilterColumn() && matrix.getNumberRow() != 0) {
            ArrayList<String> includeRow = new ArrayList<String>();
            for (String rowName : matrix.getRowNames().keySet()) {
                double value = matrix.getValue(rowName, filter.getTableElementName());
                if (filter.filterValue(value)) {
                    includeRow.add(rowName);
                }
            }
            filteredMatrix = matrix.getSubMatrixRow(includeRow);
            if (filteredMatrix.getValues().length == 0) {
                filteredMatrix.setValues(new double[1][1]);
            }
        } else if (matrix.getNumberColumn() != 0) {
            ArrayList<String> includeColumn = new ArrayList<String>();
            for (String header : matrix.getHeaders()) {
                double value = matrix.getValue(filter.getTableElementName(), header);
                if (!filter.filterValue(value)) {
                    includeColumn.add(header);
                }
            }
            filteredMatrix = matrix.getSubMatrixColumn(includeColumn);
            if (filteredMatrix.getValues().length == 0) {
                filteredMatrix.setValues(new double[1][1]);
            }
        } else
            return matrix;
        return filteredMatrix;
    }

    /**
     * log2(values)
     * 
     * @param expr
     * @return
     */
    public static ExpressionMatrix log2(ExpressionMatrix expr) {
        System.out.println("Calculate log2(Expr)");
        ExpressionMatrix exprLog = expr.clone();
        exprLog.setValues(ArrayUtils.log2(exprLog.getValues()));
        return exprLog;
    }

    /**
     * 2^values
     * 
     * @param expr
     * @return
     */
    public static ExpressionMatrix pow2(ExpressionMatrix expr) {
        System.out.println("Calculate pow2(Expr)");
        ExpressionMatrix exprLog = expr.clone();
        exprLog.setValues(ArrayUtils.pow2(exprLog.getValues()));
        return exprLog;
    }

    /**
     * Search for the minimum on all the values
     * 
     * @param expr
     * @return
     */
    public static double min(ExpressionMatrix expr) {
        return ArrayUtils.min(expr.getValues());
    }

    /**
     * Search for the minimum on one column
     * 
     * @param expr
     * @param columnIndex
     * @return
     */
    public static double min(ExpressionMatrix expr, int columnIndex) {
        return VectorUtils.min(expr.getColumn(columnIndex));
    }

    /**
     * Calculate the mean of the ExpressionMatrix
     * 
     * @param expr
     * @return
     */
    public static double mean(ExpressionMatrix expr) {
        return ArrayUtils.mean(expr.getValues());
    }

    /**
     * Calculate the mean of the ExpressionMatrix
     * 
     * @param expr
     * @return
     */
    public static double mean(ExpressionMatrix expr, int columnIndex) {
        return VectorUtils.mean(expr.getColumn(columnIndex));
    }

    /**
     * Calculate the median of the ExpressionMatrix
     * 
     * @param expr
     * @return
     */
    public static double median(ExpressionMatrix expr, int columnIndex) {
        return VectorUtils.median(expr.getColumn(columnIndex));
    }

    /**
     * Search for the maximum of all the values
     * 
     * @param expr
     * @return
     */
    public static double max(ExpressionMatrix expr) {
        return ArrayUtils.max(expr.getValues());
    }

    /**
     * Search for the maximum on one column
     * 
     * @param expr
     * @param columnIndex
     * @return
     */
    public static double max(ExpressionMatrix expr, int columnIndex) {
        return VectorUtils.max(expr.getColumn(columnIndex));
    }

    /**
     * Calculate statistical deviation of a specific column
     * 
     * @param expr
     * @param columnIndex
     * @return
     */
    public static double deviation(ExpressionMatrix expr, int columnIndex) {
        return VectorUtils.deviation(expr.getColumn(columnIndex));
    }

    /**
     * Perform an addition or soustraction between two ExpressionMatrix <br>
     * First the intersection in term of RowNames and headers is found, then addition is done for each
     * row
     * 
     * @param expr1
     * @param expr2
     * @param type true for addition, false for soustraction
     * @return
     */
    public static ExpressionMatrix addition(ExpressionMatrix expr1, ExpressionMatrix expr2, boolean type) {
        ExpressionMatrix finalMatrix = new ExpressionMatrix();
        System.out.println("Calculate the addition of two ExpressionMatrix");
        // test if both matrix have the same number of column
        if (expr1.getNumberColumn() != expr2.getNumberColumn()) {
            System.err.println("Matrices have not the same number of column");
            return null;
        }

        // first find the intersection of all rowNames, and fill header
        for (int j = 0; j < expr1.getHeaders().size(); j++) {
            if (type)
                finalMatrix.getHeaders().add(expr1.getHeader(j) + "+" + expr2.getHeader(j));
            else
                finalMatrix.getHeaders().add(expr1.getHeader(j) + "-" + expr2.getHeader(j));
        }
        int i = 0;
        for (String rowName : expr1.getRowNames().keySet()) {
            if (expr2.getRowNames().containsKey(rowName)) {
                finalMatrix.getRowNames().put(rowName, i);
                i++;
            }
        }
        System.out.println("Found " + i + " rows, and " + finalMatrix.getHeaders().size() + " columns");
        // create values
        finalMatrix.setValues(new double[finalMatrix.getRowNames().size()][finalMatrix.getHeaders().size()]);

        // then fill values
        for (String rowName : finalMatrix.getRowNames().keySet()) {
            for (int j = 0; j < finalMatrix.getNumberColumn(); j++) {
                String header = expr1.getHeader(j);
                String header2 = expr2.getHeader(j);
                double addition = ExpressionMatrix.MISSING_VALUE;
                if (type)
                    addition = expr1.getValue(rowName, header) + expr2.getValue(rowName, header2);
                else
                    addition = expr1.getValue(rowName, header) - expr2.getValue(rowName, header2);
                finalMatrix.setValue(addition, rowName, finalMatrix.getHeader(j));
            }
        }
        System.out.println("Finish addition");
        return finalMatrix;
    }

    /**
     * Perform an addition between two ExpressionMatrix <br>
     * First the intersection in term of RowNames and headers is found, then addition is done for each
     * row
     * 
     * @param expr1
     * @param expr2
     * @return
     */
    public static ExpressionMatrix plus(ExpressionMatrix expr1, ExpressionMatrix expr2) {
        return addition(expr1, expr2, true);
    }

    /**
     * Perform a soustraction between two ExpressionMatrix <br>
     * First the intersection in term of RowNames and headers is found, then addition is done for each
     * row
     * 
     * @param expr1
     * @param expr2
     * @return
     */
    public static ExpressionMatrix minus(ExpressionMatrix expr1, ExpressionMatrix expr2) {
        return addition(expr1, expr2, false);
    }

    public static ExpressionMatrix addMean(ExpressionMatrix stat, ExpressionMatrix expr) {
        double[] tempRow = new double[expr.getNumberColumn()];
        int i = 0;
        for (String header : expr.getHeaders()) {

            /*
             * the statistical calculation is here
             */
            Double statValue = VectorUtils.mean(VectorUtils.deleteMissingValue(expr.getColumn(header)));

            tempRow[i] = statValue;
            i++;
        }
        stat.addRow("Mean", tempRow);
        return stat;
    }

    public static ExpressionMatrix addMedian(ExpressionMatrix stat, ExpressionMatrix expr) {
        double[] tempRow = new double[expr.getNumberColumn()];
        int i = 0;
        for (String header : expr.getHeaders()) {

            /*
             * the statistical calculation is here
             */
            Double statValue = VectorUtils.median(VectorUtils.deleteMissingValue(expr.getColumn(header)));

            tempRow[i] = statValue;
            i++;
        }
        stat.addRow("Median", tempRow);
        return stat;
    }

    public static ExpressionMatrix variance(ExpressionMatrix expr) {
        ExpressionMatrix tempExpr = new ExpressionMatrix(expr.getHeaders());
        tempExpr.createValues(1, tempExpr.getHeaders().size());
        double[] tempRow = new double[expr.getNumberColumn()];
        int i = 0;
        for (String header : expr.getHeaders()) {

            /*
             * the statistical calculation is here
             */
            Double statValue = VectorUtils.variance(VectorUtils.deleteMissingValue(expr.getColumn(header)));

            tempRow[i] = statValue;
            i++;
        }
        tempExpr.getRowNames().put("Variance", 0);
        tempExpr.copyRowAt("Variance", tempRow);
        return tempExpr;
    }

    public static ExpressionMatrix addVariance(ExpressionMatrix stat, ExpressionMatrix expr) {
        double[] tempRow = new double[expr.getNumberColumn()];
        int i = 0;
        for (String header : expr.getHeaders()) {

            /*
             * the statistical calculation is here
             */
            Double statValue = VectorUtils.variance(VectorUtils.deleteMissingValue(expr.getColumn(header)));

            tempRow[i] = statValue;
            i++;
        }
        stat.addRow("Variance", tempRow);
        return stat;
    }

    public static ExpressionMatrix deviation(ExpressionMatrix expr) {
        ExpressionMatrix tempExpr = new ExpressionMatrix(expr.getHeaders());
        tempExpr.createValues(1, tempExpr.getHeaders().size());
        double[] tempRow = new double[expr.getNumberColumn()];
        int i = 0;
        for (String header : expr.getHeaders()) {

            /*
             * the statistical calculation is here
             */
            Double statValue = VectorUtils.deviation(VectorUtils.deleteMissingValue(expr.getColumn(header)));

            tempRow[i] = statValue;
            i++;
        }
        tempExpr.getRowNames().put("Deviation", 0);
        tempExpr.copyRowAt("Deviation", tempRow);
        return tempExpr;
    }

    public static ExpressionMatrix addDeviation(ExpressionMatrix stat, ExpressionMatrix expr) {
        double[] tempRow = new double[expr.getNumberColumn()];
        int i = 0;
        for (String header : expr.getHeaders()) {

            /*
             * the statistical calculation is here
             */
            Double statValue = VectorUtils.deviation(VectorUtils.deleteMissingValue(expr.getColumn(header)));

            tempRow[i] = statValue;
            i++;
        }
        stat.addRow("Deviation", tempRow);
        return stat;
    }

}
