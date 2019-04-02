package bacnet.scripts.core.normalization;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.utils.VectorUtils;

/**
 * Variance normalization methods which will reduce the total variance of each data to 1<br>
 * For each column of the ExpressionMatrix, multiply each value by: Math.sqr(1/var(x))<br>
 * So final variance of each column is equal to zero
 * 
 * @author Christophe Bécavin
 *
 */
public class VarianceNormalization {

    /**
     * Normalize by variance the column with a specific name<br>
     * Multiply each value by Math.sqr(1/var(x))
     * 
     * @param matrix
     * @param columnHeader
     * @return
     */
    public static ExpressionMatrix norm(ExpressionMatrix matrix, String columnHeader) {
        // calculate median
        double variance = VectorUtils
                .variance(VectorUtils.deleteMissingValue(matrix.getColumn(matrix.getHeaders().indexOf(columnHeader))));
        double factor = Math.sqrt(1 / variance);
        // delete median to each value
        for (int i = 0; i < matrix.getNumberRow(); i++) {
            if (matrix.getValue(i, matrix.getHeaders().indexOf(columnHeader)) != OmicsData.MISSING_VALUE) {
                matrix.setValue(matrix.getValue(i, matrix.getHeaders().indexOf(columnHeader)) * factor, i,
                        matrix.getHeaders().indexOf(columnHeader));
            }
        }
        // verify results
        variance = VectorUtils.variance(matrix.getColumn(matrix.getHeaders().indexOf(columnHeader)));
        if (factor != 0)
            System.err.println("Variance is equal to: " + variance);
        return matrix;
    }


}
