package bacnet.sequenceTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import bacnet.Database;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.table.core.BioConditionComparator;
import bacnet.utils.ArrayUtils;
import bacnet.utils.BasicColor;
import bacnet.utils.FileUtils;
import bacnet.utils.HTMLUtils;
import bacnet.utils.RWTUtils;

/**
 * 
 * All the tools to display homologs in the Gene Panel view: - It creates the
 * homolog table - Create the homolog figure - Display both of them
 * 
 * @author UIBC
 *
 */
public class GeneViewHomologTools {

	/**
	 * Load genome info and add homolog information on the column index1 and 2
	 */
	public static String[][] loadArrayHomologs(Sequence sequence, String[][] bioCondsArray,
			ArrayList<String[]> bioConds, ArrayList<String[]> bioCondsToDisplay) {

		String[][] bioCondsTemp = TabDelimitedTableReader.read(Database.getInstance().getGenomeArrayPath());
		/*
		 * Add two columnsd for homologs information
		 */
		bioCondsArray = new String[bioCondsTemp.length][bioCondsTemp[0].length + 2];
		for (int i = 0; i < bioCondsArray.length; i++) {
			bioCondsArray[i][0] = bioCondsTemp[i][0];
			bioCondsArray[i][1] = "";
			bioCondsArray[i][2] = "";
			for (int j = 1; j < bioCondsTemp[0].length; j++) {
				bioCondsArray[i][j + 2] = bioCondsTemp[i][j];
			}
		}
		bioCondsArray[0][1] = "Homolog";
		bioCondsArray[0][2] = "Similarity";

		/*
		 * Add homologs information
		 */
		int genomeIndex = ArrayUtils.findColumn(bioCondsArray, "Name");
		for (int i = 1; i < bioCondsArray.length; i++) {
			String genome = bioCondsArray[i][genomeIndex];
			if (sequence.getConservationHashMap().containsKey(genome)) {
				bioCondsArray[i][1] = sequence.getConservationHashMap().get(genome).split(";")[0];
				bioCondsArray[i][2] = sequence.getConservationHashMap().get(genome).split(";")[1];
			}
		}

		/*
		 * initiate table
		 */
		bioConds.clear();
		for (int i = 0; i < bioCondsArray.length; i++) {
			bioConds.add(ArrayUtils.getRow(bioCondsArray, i));
		}
		bioCondsToDisplay.clear();
		for (int i = 0; i < bioCondsArray.length; i++) {
			bioCondsToDisplay.add(ArrayUtils.getRow(bioCondsArray, i));
		}
		bioConds.remove(0);
		bioCondsToDisplay.remove(0);

		return bioCondsArray;
	}

	public static String getPhyloFigure(Gene sequence, ArrayList<String> genomes) {
		/*
		 * Replace strain name by homolog info
		 */
		String textSVG = FileUtils
				.readText(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("PHYLO_GENOME"));
		String textNew = "fill=\"#276FA0\" font-family=\"'ArialMT'\" font-size=\"11.9468\">";
		String textOld = "font-family=\"'ArialMT'\" font-size=\"11.9468\">";

		for (String genome : Genome.getAvailableGenomes()) {
			String newText = "";
			if (sequence.getConservationHashMap().containsKey(genome)) {
				String gene = sequence.getConservationHashMap().get(genome).split(";")[0];
				String similarity = sequence.getConservationHashMap().get(genome).split(";")[1];
				newText = similarity + "  --  " + gene;
			}
			if (genomes.contains(genome)) {
				textSVG = textSVG.replaceAll(textOld + genome.trim() + "</text>", textNew + newText.trim() + "</text>");
			} else {
				textSVG = textSVG.replaceAll(genome.trim() + "</text>", newText.trim() + "</text>");
			}
		}
		return textSVG;
	}

	/**
	 * Load SVG figure of phylogeny and replace all strain name by homolog
	 * informations<br>
	 * Save to JPG file and display it
	 */
	public static void loadFigureHomologs(Gene sequence, Browser browserHomolog, ArrayList<String> selectedGenomes) {
		/*
		 * Replace strain name by homolog info
		 */
		String textSVG = getPhyloFigure(sequence, selectedGenomes);

		/*
		 * Display homolog figure
		 */
		try {
			File tempSVGFile = File.createTempFile(sequence.getName(), "Phylogeny.svg");
			FileUtils.saveText(textSVG, tempSVGFile.getAbsolutePath());
			String html = SaveFileUtils.modifyHTMLwithFile(tempSVGFile.getAbsolutePath(), HTMLUtils.SVG);
			browserHomolog.setText(html);
			browserHomolog.redraw();
			tempSVGFile.deleteOnExit();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * Update Table for Biological Condition
	 * 
	 * @param bioConds
	 */
	public static void updateHomologTable(TableViewer tableHomolog, String[][] bioCondsArray,
			ArrayList<String[]> bioConds, ArrayList<String[]> bioCondsToDisplay,
			BioConditionComparator comparatorBioCondition, ArrayList<String> columnNames,
			ArrayList<String> selectedGenomes, Text txtSearchHomolog) {
		setColumnNames(columnNames, bioCondsArray);
		for (TableColumn col : tableHomolog.getTable().getColumns()) {
			col.dispose();
		}
		tableHomolog.setContentProvider(new ArrayContentProvider());

		createColumns(selectedGenomes, txtSearchHomolog, tableHomolog, bioCondsArray, comparatorBioCondition);

		tableHomolog.getTable().setHeaderVisible(true);
		tableHomolog.getTable().setLinesVisible(true);
		// Add sequenceeee!è!!!

		/*
		 * Remove first row
		 */
		tableHomolog.setInput(bioCondsToDisplay);
		comparatorBioCondition = new BioConditionComparator(columnNames);
		tableHomolog.setComparator(comparatorBioCondition);
		for (int i = 0; i < tableHomolog.getTable().getColumnCount(); i++) {
			tableHomolog.getTable().getColumn(i).pack();
		}

	}

	/**
	 * Add the name of the columns
	 */
	private static void setColumnNames(ArrayList<String> columnNames, String[][] bioCondsArray) {
		columnNames.clear();
		for (String title : bioCondsArray[0]) {
			columnNames.add(title);
		}
	}

	private static TableViewerColumn createTableViewerColumn(TableViewer tableHomolog, String title,
			final int colNumber, BioConditionComparator comparatorBioCondition) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableHomolog, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	/**
	 * Create Columns of Homolog table
	 * 
	 * @param bioCondsArray
	 * @param bioConds
	 */
	private static void createColumns(ArrayList<String> selectedGenomes, Text txtSearchHomolog,
			TableViewer tableHomolog, String[][] bioCondsArray, BioConditionComparator comparatorBioCondition) {
		TableViewerColumn col = createTableViewerColumn(tableHomolog, "Select", 0, comparatorBioCondition);
		col.setLabelProvider(new ColumnLabelProvider() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -70923942574212205L;

			@Override
			public void update(ViewerCell cell) {
				String[] bioCond = (String[]) cell.getElement();
				Image image = null;
				if (selectedGenomes.contains(bioCond[3])) {
					image = ResourceManager.getPluginImage("bacnet", "icons/checked.bmp");
				} else {
					image = ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp");
				}
				cell.setImage(image);
				Color colorBack = BasicColor.LIGHTGREY;
				int rowIndex = Integer.parseInt(bioCond[0]);
				if (rowIndex % 2 == 0) {
					colorBack = BasicColor.WHITE;
				}
				cell.setText("");
				cell.setBackground(colorBack);
			}
		});

		for (int i = 0; i < bioCondsArray[0].length; i++) {
			TableViewerColumn col2 = createTableViewerColumn(tableHomolog, bioCondsArray[0][i], i + 1,
					comparatorBioCondition);
			col2.setLabelProvider(new CellLabelProvider() {

				/**
				 * 
				 */
				private static final long serialVersionUID = -8247269722692614416L;

				@Override
				public void update(ViewerCell cell) {
					String[] bioCond = (String[]) cell.getElement();
					String text = bioCond[cell.getColumnIndex() - 1];
					String colName = bioCondsArray[0][cell.getColumnIndex() - 1];
					if (colName.equals("Reference")) {
						cell.setText(RWTUtils.setPubMedLink(text));
					} else if (colName.equals("Sequence ID")) {
						cell.setText("<a href='https://www.ncbi.nlm.nih.gov/nuccore/" + text + "' target='_blank'>"
								+ text + "</a>");
					} else {
						cell.setText(text);
					}
					Color colorBack = BasicColor.LIGHTGREY;
					int rowIndex = Integer.parseInt(bioCond[0]);
					if (rowIndex % 2 == 0) {
						colorBack = BasicColor.WHITE;
					}
					if (!txtSearchHomolog.getText().equals("")) {
						if (bioCond[cell.getColumnIndex() - 1].contains(txtSearchHomolog.getText())) {
							colorBack = BasicColor.YELLOW;
						}
					}
					cell.setBackground(colorBack);
				}
			});
			col2.getColumn().pack();
		}
	}

}
