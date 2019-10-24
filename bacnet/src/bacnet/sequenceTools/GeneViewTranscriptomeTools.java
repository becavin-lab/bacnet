package bacnet.sequenceTools;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.sequence.Sequence;
import bacnet.expressionAtlas.core.GenomeElementAtlas;
import bacnet.utils.ArrayUtils;
import bacnet.utils.Filter;
import bacnet.utils.RWTUtils;

public class GeneViewTranscriptomeTools {
    /**
     * Update display of expression data results
     */
    public static void updateExpressionAtlas(Sequence sequence, Text txtCutoffLogFC, GeneView viewer,
            String[][] arrayDataList) {
        if (sequence != null) {
            double cutoffLogFC = GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF;
            try {
                cutoffLogFC = Double.parseDouble(viewer.getTxtCutoffLogFC().getText());
            } catch (Exception e) {
                viewer.getTxtCutoffLogFC().setText(GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF + "");
            }
            Filter filter = new Filter();
            filter.setCutOff1(cutoffLogFC);
            GenomeElementAtlas atlas = new GenomeElementAtlas(sequence, filter);
            viewer.getLblOver().setText(atlas.getOverBioConds().size() + " data");
            viewer.getLblUnder().setText(atlas.getUnderBioConds().size() + " data");
            viewer.getLblNodiff().setText(atlas.getNotDiffExpresseds().size() + " data");

            updateTranscriptomesTable(atlas, viewer, arrayDataList);
        }
    }

    /**
     * Update table showing transcriptomes information
     */
    private static void updateTranscriptomesTable(GenomeElementAtlas atlas, GeneView viewer, String[][] arrayDataList) {
        /*
         * Update overexpressed list
         */
        Table tableOver = viewer.getTableOver();
        tableOver.removeAll();
        tableOver.setHeaderVisible(true);
        tableOver.setLinesVisible(true);
        for (int i = 0; i < arrayDataList[0].length; i++) {
            TableColumn column = new TableColumn(tableOver, SWT.NONE);
            column.setText(arrayDataList[0][i]);
            column.setAlignment(SWT.LEFT);
            
        }
        for (int i = 1; i < arrayDataList.length; i++) {
            String dataName = arrayDataList[i][ArrayUtils.findColumn(arrayDataList, "Data Name")];
            if (atlas.getOverBioConds().contains(dataName)) {
                TableItem item = new TableItem(tableOver, SWT.NONE);
                for (int j = 0; j < arrayDataList[0].length; j++) {
                    item.setText(j, arrayDataList[i][j]);
                }
            }
        }
        for (int i = 0; i < arrayDataList[0].length; i++) {
            tableOver.getColumn(i).pack();
        }
        tableOver.update();
        tableOver.redraw();

        /*
         * Update under-expressed list
         */
        Table tableUnder = viewer.getTableUnder();
        tableUnder.removeAll();
        tableUnder.setHeaderVisible(true);
        tableUnder.setLinesVisible(true);
        for (int i = 0; i < arrayDataList[0].length; i++) {
            TableColumn column = new TableColumn(tableUnder, SWT.NONE);
            column.setText(arrayDataList[0][i]);
            column.setAlignment(SWT.LEFT);
        }
        for (int i = 1; i < arrayDataList.length; i++) {
            String dataName = arrayDataList[i][ArrayUtils.findColumn(arrayDataList, "Data Name")];
            if (atlas.getUnderBioConds().contains(dataName)) {
                TableItem item = new TableItem(tableUnder, SWT.NONE);
                for (int j = 0; j < arrayDataList[0].length; j++) {
                    item.setText(j, arrayDataList[i][j]);
                }
            }
        }
        for (int i = 0; i < arrayDataList[0].length; i++) {
            tableUnder.getColumn(i).pack();
        }
        tableUnder.update();
        tableUnder.redraw();

        /*
         * Update no dfiff expressed genes list
         */
        Table tableNodiff = viewer.getTableNodiff();
        tableNodiff.removeAll();
        tableNodiff.setHeaderVisible(true);
        tableNodiff.setLinesVisible(true);
        for (int i = 0; i < arrayDataList[0].length; i++) {
            TableColumn column = new TableColumn(tableNodiff, SWT.NONE);
            column.setText(arrayDataList[0][i]);
            column.setAlignment(SWT.LEFT);
        }
        for (int i = 1; i < arrayDataList.length; i++) {
            String dataName = arrayDataList[i][ArrayUtils.findColumn(arrayDataList, "Data Name")];
            if (atlas.getNotDiffExpresseds().contains(dataName)) {
                TableItem item = new TableItem(tableNodiff, SWT.NONE);
                for (int j = 0; j < arrayDataList[0].length; j++) {
                    item.setText(j, arrayDataList[i][j]);
                }
            }
        }
        for (int i = 0; i < arrayDataList[0].length; i++) {
            tableNodiff.getColumn(i).pack();
        }
        tableNodiff.update();
        tableNodiff.redraw();
    }
}
