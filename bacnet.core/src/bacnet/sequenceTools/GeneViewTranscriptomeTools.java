package bacnet.sequenceTools;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
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
			GenomeElementAtlas atlas = new GenomeElementAtlas(sequence, filter, true);
			viewer.getLblOver().setText(atlas.getOverBioConds().size() + " data");
			viewer.getLblUnder().setText(atlas.getUnderBioConds().size() + " data");
			viewer.getLblNodiff().setText(atlas.getNotDiffExpresseds().size() + " data");

			updateTranscriptAtlasTable(atlas, viewer, arrayDataList);
		}
	}

	/**
	 * Update table showing transcriptomes information
	 */
	private static void updateTranscriptAtlasTable(GenomeElementAtlas atlas, GeneView viewer, String[][] arrayDataList) {
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


	/**
	 * Update  transcriptomes table = transcript expression with a value over 0
	 */
	public static void updateTranscriptomesTable(Sequence sequence, GeneView viewer, String[][] arrayTranscriptomesList) {
		System.out.println("updateTranscriptomeTable");
		/*
		 * Update expressed list
		 */
		ArrayList<String> bioConditions = new ArrayList<>();
		HashMap<String, Double> normValues = new HashMap<>();

		System.out.println("getGenomeSelected: "+ viewer.getGenomeSelected());
		ExpressionMatrix exprTranscriptomesMatrix = Database.getInstance().getExprTranscriptomesTable(viewer.getGenomeSelected());

		System.out.println("sequence: " + sequence.getName());

		if (exprTranscriptomesMatrix.getRowNames().containsKey(sequence.getName())) {
			System.out.println("in if");

			for (String header : exprTranscriptomesMatrix.getHeaders()) {
				System.out.println("headers: "+ header.substring(0, header.length()-8));
				double value = exprTranscriptomesMatrix.getValue(sequence.getName(), header);
				if (value > 0) {
					bioConditions.add(header.substring(0, header.length()-8));
					normValues.put(header.substring(0, header.length()-8), value);
				}
			}
		}
		System.out.println("after if");

		viewer.getLblOverTranscriptomes().setText(bioConditions.size() + " datasets out of " + exprTranscriptomesMatrix.getHeaders().size());

		/*
		 * Update table
		 */
		Table tableTranscriptomes = viewer.getTableTranscriptomes();
		tableTranscriptomes.removeAll();
		tableTranscriptomes.setHeaderVisible(true);
		tableTranscriptomes.setLinesVisible(true);
		TableColumn column1 = new TableColumn(tableTranscriptomes, SWT.NONE);
		column1.setText("DESeq2 norm");
		column1.setAlignment(SWT.LEFT);
		System.out.println("column 1");
		System.out.println("length "+arrayTranscriptomesList[0].length);

		for (int i = 0; i < arrayTranscriptomesList[0].length; i++) {

			//System.out.println("head column "+i);
			TableColumn column = new TableColumn(tableTranscriptomes, SWT.NONE);
			column.setText(arrayTranscriptomesList[0][i]);
			column.setAlignment(SWT.LEFT);
		}
		for (int i = 1; i < arrayTranscriptomesList.length; i++) {
			//System.out.println("head column "+i);
			String dataName = arrayTranscriptomesList[i][ArrayUtils.findColumn(arrayTranscriptomesList, "Data Name")];
			if (bioConditions.contains(dataName)) {
				System.out.println("dataName "+dataName);
				TableItem item = new TableItem(tableTranscriptomes, SWT.NONE);
				System.out.println("TableItem ");

				item.setText(0, normValues.get(dataName).toString());
				System.out.println("TableItem ");

				for (int j = 0; j < arrayTranscriptomesList[0].length; j++) {
					System.out.println("j "+ j);

					item.setText(j+1, arrayTranscriptomesList[i][j]);

				}
			}
		}
		for (int i = 0; i < arrayTranscriptomesList[0].length+1; i++) {
			System.out.println("pack "+ i);

			tableTranscriptomes.getColumn(i).pack();
		}
		tableTranscriptomes.update();
		tableTranscriptomes.redraw();
	}
}
