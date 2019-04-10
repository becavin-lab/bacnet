package bacnet.sequenceTools;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Sequence;
import bacnet.utils.ArrayUtils;

public class GeneViewProteomeTools {

	/**
	 * Update proteomics table
	 */
	public static void updateProteomesTable(Sequence sequence, GeneView viewer, String[][] arrayProteomeList) {
		/*
		 * Update overexpressed list
		 */
		ArrayList<String> bioConditions = new ArrayList<>();
		System.out.println(viewer.getGenomeSelected());
		ExpressionMatrix exprProteomesMatrix = Database.getInstance().getExprProteomesTable(viewer.getGenomeSelected());
		System.out.println(sequence.getName());
		if (exprProteomesMatrix.getRowNames().containsKey(sequence.getName())) {
			for (String header : exprProteomesMatrix.getHeaders()) {
				double value = exprProteomesMatrix.getValue(sequence.getName(), header);
				if (value > 0) {
					bioConditions.add(header);
				}
			}
		}
		viewer.getLblOverProteomes().setText(bioConditions.size() + "");

		/*
		 * Update table
		 */
		Table tableProteomes = viewer.getTableProteomes();
		tableProteomes.removeAll();
		tableProteomes.setHeaderVisible(true);
		tableProteomes.setLinesVisible(true);
		for (int i = 0; i < arrayProteomeList[0].length; i++) {
			TableColumn column = new TableColumn(tableProteomes, SWT.NONE);
			column.setText(arrayProteomeList[0][i]);
			column.setAlignment(SWT.LEFT);
		}
		for (int i = 1; i < arrayProteomeList.length; i++) {
			String dataName = arrayProteomeList[i][ArrayUtils.findColumn(arrayProteomeList, "Data Name")];
			if (bioConditions.contains(dataName)) {
				TableItem item = new TableItem(tableProteomes, SWT.NONE);
				for (int j = 0; j < arrayProteomeList[0].length; j++) {
					item.setText(j, arrayProteomeList[i][j]);
				}
			}
		}
		for (int i = 0; i < arrayProteomeList[0].length; i++) {
			tableProteomes.getColumn(i).pack();
		}
		tableProteomes.update();
		tableProteomes.redraw();
	}
}
