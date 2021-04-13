package bacnet.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import bacnet.datamodel.dataset.ExpressionMatrix;

public class ExpressionMatrixReader {

    public static ExpressionMatrix read(String fileName) throws IOException {
        String[][] stringMatrix = TabDelimitedTableReader.read(new File(fileName));
        ExpressionMatrix exprMatrix = new ExpressionMatrix();

        // first read of the header
        ArrayList<String> headers = new ArrayList<String>();
        for (int i = 1; i < stringMatrix[0].length; i++) {
            headers.add(stringMatrix[0][i]);
        }
        exprMatrix.setHeaders(headers);

        // create values and rowNames
        TreeMap<String, Integer> rowNames = exprMatrix.getRowNames();
        double[][] values = new double[stringMatrix.length - 1][stringMatrix[0].length - 1];
        for (int i = 0; i < values.length; i++) {
            rowNames.put(stringMatrix[i + 1][0], i);
            for (int j = 0; j < values[i].length; j++) {
                values[i][j] = Double.valueOf(stringMatrix[i + 1][j + 1]);
            }
        }
        exprMatrix.setValues(values);
        return exprMatrix;

    }

}
