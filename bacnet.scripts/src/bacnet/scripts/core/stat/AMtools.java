package bacnet.scripts.core.stat;

import java.util.ArrayList;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.scripts.core.vennDiagram.VennDiagram;

public class AMtools {
    /**
     * Calculate AM matrix
     * 
     * @param values
     * @return
     */
    public static double[][] AM(double[][] values) {
        ArrayList<String> indexList = new ArrayList<String>();
        for (int j = 0; j < values[0].length; j++) {
            indexList.add(j + "");
        }
        ArrayList<ArrayList<String>> combinations = VennDiagram.combinator(indexList, 2);

        double[][] am = new double[values.length * (values[0].length * (values[0].length - 1) / 2)][2];
        int k = 0;
        for (int i = 0; i < values.length; i++) {
            for (ArrayList<String> indexes : combinations) {
                double x1 = values[i][Integer.parseInt(indexes.get(0))];
                double x2 = values[i][Integer.parseInt(indexes.get(1))];
                double A = A(x1, x2);
                double M = M(x1, x2);
                am[k][0] = A;
                am[k][1] = M;
                // am[k][2] = Double.parseDouble(indexes.get(0));
                // am[k][3] = Double.parseDouble(indexes.get(1));
                k++;
                // A = A(x2,x1);
                // M = M(x2,x1);
                // am[k][0] = A;
                // am[k][1] = M;
                //// am[k][2] = Double.parseDouble(indexes.get(1));
                //// am[k][3] = Double.parseDouble(indexes.get(0));
                // k++;
            }
        }

        // ArrayUtils.displayMatrix("d", am);
        return am;
    }

    public static ExpressionMatrix AM(ExpressionMatrix matrix) {
        return getExpressionMatrix(AM(matrix.getValues()));

    }
    // public static double[][] calcVarM(double[][] AM,double[][] values){
    // double[][] avarM = new double[values.length][2];
    // int k=0;
    // for(int i =0;i<values.length;i++){
    // for(ArrayList<String> indexes : combinations){
    // double x1 = values[i][Integer.parseInt(indexes.get(0))];
    // double x2 = values[i][Integer.parseInt(indexes.get(1))];
    // double A = A(x1,x2);
    // double M = M(x1,x2);
    // avarM[k][0] = A;
    // avarM[k][1] = M;
    //// am[k][2] = Double.parseDouble(indexes.get(0));
    //// am[k][3] = Double.parseDouble(indexes.get(1));
    // k++;
    // A = A(x2,x1);
    // M = M(x2,x1);
    // avarM[k][0] = A;
    // avarM[k][1] = M;
    //// am[k][2] = Double.parseDouble(indexes.get(1));
    //// am[k][3] = Double.parseDouble(indexes.get(0));
    // k++;
    // }
    // }
    //
    // //ArrayUtils.displayMatrix("d", am);
    // return avarM;
    // }



    // public static void displayAM(double[][] AM){
    // ExpressionMatrix am = getExpressionMatrix(AM);
    // try {
    // ScatterPlotView.displayMatrix(am, "AM");
    // }
    // catch (PartInitException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    public static ExpressionMatrix getExpressionMatrix(double[][] AM) {
        ExpressionMatrix am = new ExpressionMatrix();
        am.addHeader("A");
        am.addHeader("M");
        for (int i = 0; i < AM.length; i++) {
            am.getRowNames().put(i + "", i);
        }
        am.setValues(AM);
        return am;
    }


    /**
     * Calculate M = x1-x2<br>
     * Represents the variability within expression value x1 and x2
     * 
     * @param x1
     * @param x2
     * @return
     */
    public static double M(double x1, double x2) {
        return x1 - x2;
    }

    /**
     * Calculate A = (x1+x2)/2<br>
     * Represents mean value of x1+x2
     * 
     * @param x1
     * @param x2
     * @return
     */
    public static double A(double x1, double x2) {
        return (x1 + x2) / 2;
    }
}
