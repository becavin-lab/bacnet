package bacnet.sequenceTools;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Sequence;
import bacnet.expressionAtlas.core.GenomeElementAtlas;
import bacnet.utils.ArrayUtils;
import bacnet.utils.Filter;

public class GeneViewProteomeTools {

	/**
     * Update display of expression data results
     */
    public static void updateProteinAtlas(Sequence sequence, Text txtCutoffLogFC, GeneView viewer, String[][] arrayProteinAtlasList) {
        if (sequence != null) {
        	//System.out.println("true");
            double cutoffLogFC = GenomeElementAtlas.DEFAULT_LOGFC_PROTEOMIC_CUTOFF;

            try {
                cutoffLogFC = Double.parseDouble(viewer.getTxtCutoffLogFCProteome().getText());
                
            } catch (Exception e) {
                viewer.getTxtCutoffLogFC().setText(GenomeElementAtlas.DEFAULT_LOGFC_PROTEOMIC_CUTOFF + "");
            }
        	//System.out.println("cutoff: "+ cutoffLogFC);

            Filter filter = new Filter();
            filter.setCutOff1(cutoffLogFC);
            GenomeElementAtlas atlas = new GenomeElementAtlas(sequence, filter, false);
            viewer.getLblOverProteome().setText(atlas.getOverBioConds().size() + " data");
            viewer.getLblUnderProteome().setText(atlas.getUnderBioConds().size() + " data");
            viewer.getLblNodiffProteome().setText(atlas.getNotDiffExpresseds().size() + " data");

            updateProteinAtlasTable(atlas, viewer, arrayProteinAtlasList);
        }
    }

    /**
     * Update table showing proteomics information
     */
    private static void updateProteinAtlasTable(GenomeElementAtlas atlas, GeneView viewer, String[][] arrayProteinAtlasList) {
        /*
         * Update overexpressed list
         */
        Table tableOver = viewer.getTableOverProteome();
        tableOver.removeAll();
        tableOver.setHeaderVisible(true);
        tableOver.setLinesVisible(true);
        for (int i = 0; i < arrayProteinAtlasList[0].length; i++) {
            TableColumn column = new TableColumn(tableOver, SWT.NONE);
            column.setText(arrayProteinAtlasList[0][i]);
            column.setAlignment(SWT.LEFT);
            
        }
        for (int i = 1; i < arrayProteinAtlasList.length; i++) {
            String dataName = arrayProteinAtlasList[i][ArrayUtils.findColumn(arrayProteinAtlasList, "Data Name")];
            if (atlas.getOverBioConds().contains(dataName)) {
                TableItem item = new TableItem(tableOver, SWT.NONE);
                for (int j = 0; j < arrayProteinAtlasList[0].length; j++) {
                    item.setText(j, arrayProteinAtlasList[i][j]);
                }
            }
        }
        for (int i = 0; i < arrayProteinAtlasList[0].length; i++) {
            tableOver.getColumn(i).pack();
        }
        tableOver.update();
        tableOver.redraw();

        /*
         * Update under-expressed list
         */
        Table tableUnder = viewer.getTableUnderProteome();
        tableUnder.removeAll();
        tableUnder.setHeaderVisible(true);
        tableUnder.setLinesVisible(true);
        for (int i = 0; i < arrayProteinAtlasList[0].length; i++) {
            TableColumn column = new TableColumn(tableUnder, SWT.NONE);
            column.setText(arrayProteinAtlasList[0][i]);
            column.setAlignment(SWT.LEFT);
        }
        for (int i = 1; i < arrayProteinAtlasList.length; i++) {
            String dataName = arrayProteinAtlasList[i][ArrayUtils.findColumn(arrayProteinAtlasList, "Data Name")];
            if (atlas.getUnderBioConds().contains(dataName)) {
                TableItem item = new TableItem(tableUnder, SWT.NONE);
                for (int j = 0; j < arrayProteinAtlasList[0].length; j++) {
                    item.setText(j, arrayProteinAtlasList[i][j]);
                }
            }
        }
        for (int i = 0; i < arrayProteinAtlasList[0].length; i++) {
            tableUnder.getColumn(i).pack();
        }
        tableUnder.update();
        tableUnder.redraw();

        /*
         * Update no dfiff expressed genes list
         */
        Table tableNodiff = viewer.getTableNodiffProteome();
        tableNodiff.removeAll();
        tableNodiff.setHeaderVisible(true);
        tableNodiff.setLinesVisible(true);
        for (int i = 0; i < arrayProteinAtlasList[0].length; i++) {
            TableColumn column = new TableColumn(tableNodiff, SWT.NONE);
            column.setText(arrayProteinAtlasList[0][i]);
            column.setAlignment(SWT.LEFT);
        }
        for (int i = 1; i < arrayProteinAtlasList.length; i++) {
            String dataName = arrayProteinAtlasList[i][ArrayUtils.findColumn(arrayProteinAtlasList, "Data Name")];
            if (atlas.getNotDiffExpresseds().contains(dataName)) {
                TableItem item = new TableItem(tableNodiff, SWT.NONE);
                for (int j = 0; j < arrayProteinAtlasList[0].length; j++) {
                    item.setText(j, arrayProteinAtlasList[i][j]);
                }
            }
        }
        for (int i = 0; i < arrayProteinAtlasList[0].length; i++) {
            tableNodiff.getColumn(i).pack();
        }
        tableNodiff.update();
        tableNodiff.redraw();
    }
    
    
    /**
     * Update proteomics table = Protein expression with a value over 0
     */
    public static void updateProteomesTable(Sequence sequence, GeneView viewer, String[][] arrayProteomeList) {
    	//System.out.println("updateProteomesTable");
        /*
         * Update expressed list
         */
        ArrayList<String> bioConditions = new ArrayList<>();
        HashMap<String, Double> LFQValues = new HashMap<>();

        //System.out.println("getGenomeSelected: "+ viewer.getGenomeSelected());
        ExpressionMatrix exprProteomesMatrix = Database.getInstance().getExprProteomesTable(viewer.getGenomeSelected());

        //System.out.println("sequence: " + sequence.getName());
        //System.out.println("exprProteomesMatrixRowNames: " + exprProteomesMatrix.getName());

        if (exprProteomesMatrix.getRowNames().containsKey(sequence.getName())) {
            //System.out.println("in if");

            for (String header : exprProteomesMatrix.getHeaders()) {
                //System.out.println("headers: "+ header);
                double value = exprProteomesMatrix.getValue(sequence.getName(), header);
                if (value > 0) {
                    bioConditions.add(header);
                    LFQValues.put(header, value);
                }
            }
        }
        //System.out.println("after if");

        viewer.getLblOverProteomes().setText(bioConditions.size() + " datasets out of " + exprProteomesMatrix.getHeaders().size());

        /*
         * Update table
         */
        Table tableProteomes = viewer.getTableProteomes();
        tableProteomes.removeAll();
        tableProteomes.setHeaderVisible(true);
        tableProteomes.setLinesVisible(true);
        TableColumn column1 = new TableColumn(tableProteomes, SWT.NONE);
        column1.setText("Log10(LFQ)");
        column1.setAlignment(SWT.LEFT);
        //System.out.println("column 1");
        //System.out.println("length "+arrayProteomeList[0].length);

        for (int i = 0; i < arrayProteomeList[0].length; i++) {
        	
            //System.out.println("head column "+i);
            TableColumn column = new TableColumn(tableProteomes, SWT.NONE);
            column.setText(arrayProteomeList[0][i]);
            column.setAlignment(SWT.LEFT);
        }
        for (int i = 1; i < arrayProteomeList.length; i++) {
            //System.out.println("head column "+i);
            String dataName = arrayProteomeList[i][ArrayUtils.findColumn(arrayProteomeList, "Data Name")];
            if (bioConditions.contains(dataName)) {
                //System.out.println("dataName "+dataName);

                TableItem item = new TableItem(tableProteomes, SWT.NONE);
                item.setText(0, LFQValues.get(dataName).toString());
                for (int j = 0; j < arrayProteomeList[0].length; j++) {
                    //System.out.println("j "+ j);

                    item.setText(j+1, arrayProteomeList[i][j]);
                    
                }
            }
        }
        for (int i = 0; i < arrayProteomeList[0].length+1; i++) {
            //System.out.println("pack "+ i);

            tableProteomes.getColumn(i).pack();
        }
        tableProteomes.update();
        tableProteomes.redraw();
    }
}
