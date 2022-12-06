package bacnet.sequenceTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.apache.commons.*;
import bacnet.Database;
import bacnet.datamodel.phylogeny.Phylogenomic;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
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
 * All the tools to display homologs in the Gene Panel view: - It creates the homolog table - Create
 * the homolog figure - Display both of them
 * 
 * @author UIBC
 *
 */
public class GeneViewHomologTools {

    /**
     * Load genome info and add homolog information on the column index 1 and 2
     */
    public static String[][] loadArrayHomologs(Sequence sequence, String[][] bioCondsArray,
            ArrayList<String[]> bioConds, ArrayList<String[]> bioCondsToDisplay) {
        String[][] bioCondsTemp = TabDelimitedTableReader.read(Database.getInstance().getGenomeArrayPath());
        /*
         * Add two columns for homologs information
         */
        bioCondsArray = new String[bioCondsTemp.length][bioCondsTemp[0].length + 15];
        for (int i = 0; i < bioCondsArray.length; i++) {
            bioCondsArray[i][0] = bioCondsTemp[i][0];
            bioCondsArray[i][1] = bioCondsTemp[i][1];
            bioCondsArray[i][2] = "";
            bioCondsArray[i][3] = "";
            bioCondsArray[i][4] = "";
            bioCondsArray[i][5] = "";
            bioCondsArray[i][6] = "";
            bioCondsArray[i][7] = "";
            bioCondsArray[i][8] = "";
            bioCondsArray[i][9] = "";
            bioCondsArray[i][10] = "";
            bioCondsArray[i][11] = "";
            bioCondsArray[i][12] = "";
            bioCondsArray[i][13] = "";
            bioCondsArray[i][14] = "";
            bioCondsArray[i][15] = "";
            bioCondsArray[i][16] = "";

            for (int j = 2; j < bioCondsTemp[0].length; j++) {
                bioCondsArray[i][j + 15] = bioCondsTemp[i][j];
            }
        }
        
        	//  0: genome_target 1:geneTarget 2:oldLocusTarget
    		//	3: proteinTargetName 4:qcovs 5:pident 6:bidirectional 7:evalue 8:bitscore 9:qstart 
    		//	10:qend 11:sstart 12:send 13:slen 14:nident 15:matchedLength
        
        bioCondsArray[0][2] = "Homolog Locus";
        bioCondsArray[0][3] = "Homolog Old Locus";
        bioCondsArray[0][4] = "Homolog Protein";
        bioCondsArray[0][5] = "Coverage (%)";
        bioCondsArray[0][6] = "Similarity (%)";
        bioCondsArray[0][7] = "Bidirectional";
        bioCondsArray[0][8] = "E-value";
        bioCondsArray[0][9] = "Bitscore";
        bioCondsArray[0][10] = "Query homol. start";
        bioCondsArray[0][11] = "Query homol. end";
        bioCondsArray[0][12] = "Subject homol. start";
        bioCondsArray[0][13] = "Subject homol. end";
        bioCondsArray[0][14] = "Subject length";
        bioCondsArray[0][15] = "# identity";
        bioCondsArray[0][16] = "Matched Length";

        /*
         * Add homologs information
         */
        int genomeIndex = ArrayUtils.findColumn(bioCondsArray, "Name (GenBank)");
        for (int i = 1; i < bioCondsArray.length; i++) {
            String genome = bioCondsArray[i][genomeIndex];
            if (sequence.getConservationHashMap().containsKey(genome)) {
                bioCondsArray[i][2] = sequence.getConservationHashMap().get(genome).split(";")[0];
                bioCondsArray[i][3] = sequence.getConservationHashMap().get(genome).split(";")[1];
                bioCondsArray[i][4] = sequence.getConservationHashMap().get(genome).split(";")[2];
                bioCondsArray[i][5] = String.format("%.1f", Float.parseFloat(sequence.getConservationHashMap().get(genome).split(";")[3]));
                bioCondsArray[i][6] = String.format("%.1f", Float.parseFloat(sequence.getConservationHashMap().get(genome).split(";")[4]));
                bioCondsArray[i][7] = sequence.getConservationHashMap().get(genome).split(";")[5];
                bioCondsArray[i][8] = sequence.getConservationHashMap().get(genome).split(";")[6];
                bioCondsArray[i][9] = String.format("%.1f", Float.parseFloat(sequence.getConservationHashMap().get(genome).split(";")[7]));
                bioCondsArray[i][10] = String.format("%d", Integer.parseInt(sequence.getConservationHashMap().get(genome).split(";")[8]));
                bioCondsArray[i][11] = String.format("%d", Integer.parseInt(sequence.getConservationHashMap().get(genome).split(";")[9]));
                bioCondsArray[i][12] = String.format("%d", Integer.parseInt(sequence.getConservationHashMap().get(genome).split(";")[10]));
                bioCondsArray[i][13] = String.format("%d", Integer.parseInt(sequence.getConservationHashMap().get(genome).split(";")[11]));
                bioCondsArray[i][14] = String.format("%d", Integer.parseInt(sequence.getConservationHashMap().get(genome).split(";")[12]));
                bioCondsArray[i][15] = String.format("%d", Integer.parseInt(sequence.getConservationHashMap().get(genome).split(";")[13]));
                bioCondsArray[i][16] = String.format("%d", Integer.parseInt(sequence.getConservationHashMap().get(genome).split(";")[14]));

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

    
    /**
     * Read Phylogenomic figure and modify the labels with homolog table
     * @param sequence
     * @param genomes
     * @return
     */
    public static String getPhyloFigure(Gene sequence, ArrayList<String> genomeNames) {
    	/*
         * Replace strain name by homolog info
         */
        String textSVG = FileUtils
                .readText(Phylogenomic.getPhylogenomicFigurePath());
        HashMap<String, String> genomeToAttribute = Phylogenomic.parsePhylogenomicFigure(textSVG);
        /*
		 * Highlight selected strain
		 */
		for (String genome : genomeNames) {
			String lineAttribute = genomeToAttribute.get(genome);
			int indexOfLine = textSVG.indexOf(lineAttribute);
			int indexOfstyle = lineAttribute.indexOf("style");
			int lengthStyle = "style=\"".length();
			int posToADD = indexOfLine + indexOfstyle + lengthStyle;
			String textToADD = "fill:purple; ";
			textSVG = textSVG.substring(0, posToADD) + textToADD + textSVG.substring(posToADD, textSVG.length());
        }

		/*
		 * Modify strain name by their similarity value
		 */

        for (String genome : genomeToAttribute.keySet()) {
        	if (sequence.getConservationHashMap().containsKey(GenomeNCBI.unprocessGenomeName(genome))) {
        		String accession = sequence.getConservationHashMap().get(GenomeNCBI.unprocessGenomeName(genome));
        		String gene = accession.split(";")[0];
        		String oldLocus = accession.split(";")[1];
                String similarity = String.format("%.1f", Float.parseFloat(accession.split(";")[4]))+"%";
        		int indexOfGenome = textSVG.indexOf(">"+genome+"<");
        		String textToADD = similarity + "  --  " + gene + " - " + oldLocus ;
                textSVG = textSVG.substring(0, indexOfGenome+1) + textToADD + textSVG.substring(indexOfGenome + genome.length() + 1, textSVG.length());
            }else {
            	int indexOfGenome = textSVG.indexOf(">"+genome+"<");
        		String textToADD = "";
                textSVG = textSVG.substring(0, indexOfGenome+1) + textToADD + textSVG.substring(indexOfGenome + genome.length() + 1, textSVG.length());
            }
        }
        return textSVG;
    }

    /**
     * Load SVG figure of phylogeny and replace all strain name by homolog informations<br>
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
                if (selectedGenomes.contains(GenomeNCBI.processGenomeName(bioCond[1]))) {
                    image = ResourceManager.getPluginImage("bacnet.core", "icons/checked.bmp");
                } else {
                    image = ResourceManager.getPluginImage("bacnet.core", "icons/unchecked.bmp");
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
            TableViewerColumn col2 =
                    createTableViewerColumn(tableHomolog, bioCondsArray[0][i], i + 1, comparatorBioCondition);
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
            			System.out.println("in txtSearch: "+ txtSearchHomolog.getText());
                        if (bioCond[cell.getColumnIndex() - 1].toLowerCase().contains(txtSearchHomolog.getText().toLowerCase())) {
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
