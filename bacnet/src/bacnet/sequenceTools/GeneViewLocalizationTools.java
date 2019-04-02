package bacnet.sequenceTools;

import java.io.File;
import java.io.IOException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import bacnet.Database;
import bacnet.datamodel.annotation.SubCellCompartment;
import bacnet.datamodel.annotation.SubCellCompartment.TypeCompartment;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.raprcp.SaveFileUtils;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import bacnet.utils.HTMLUtils;
import bacnet.utils.RWTUtils;

public class GeneViewLocalizationTools {

    /**
     * Load SVG figure of phylogeny and replace all strain name by homolog informations<br>
     * Save to JPG file and display it
     */
    public static void loadLocalizationFigure(Browser browserLocalization, String[][] arrayGeneToLocalization,
            Sequence sequence, String[][] bioCondsArray, Table tableLocalization, Genome genome) {
        // String os = System.getProperty("os.name");
        // if(!os.equals("Mac OS X")){
        /*
         * Replace strain name by homolog info
         */
        String textSVG = FileUtils
                .readText(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("PROTEIN_LOC"));
        String suffix = "fill=\"#414042\" font-family=\"'ArialMT'\" font-size=\"12\">";
        String newSuffix = "fill=\"#ED1C24\" font-family=\"'Arial-BoldMT'\" font-size=\"12\">";

        /*
         * Extract localization
         */
        String[] info = new String[arrayGeneToLocalization[0].length];
        for (int i = 0; i < info.length; i++)
            info[i] = "";
        info[1] = "CP";
        if (genome.getSpecies().equals(Genome.EGDE_NAME)) {
            boolean found = false;
            for (int i = 0; i < arrayGeneToLocalization.length && !found; i++) {
                if (arrayGeneToLocalization[i][0].equals(sequence.getName())) {
                    info = ArrayUtils.getRow(arrayGeneToLocalization, i);
                    found = true;
                }
            }
        } else {
            String locus = "";
            for (int i = 0; i < bioCondsArray.length; i++) {
                if (bioCondsArray[i][ArrayUtils.findColumn(bioCondsArray, "Name")].equals(Genome.EGDE_NAME)) {
                    locus = bioCondsArray[i][ArrayUtils.findColumn(bioCondsArray, "Homolog")];
                    System.out.println(locus);
                }
            }

            if (locus.equals("")) {
                info[1] = "Unknown";
            } else {
                boolean found = false;
                for (int i = 0; i < arrayGeneToLocalization.length && !found; i++) {
                    if (arrayGeneToLocalization[i][0].equals(locus)) {
                        info = ArrayUtils.getRow(arrayGeneToLocalization, i);
                        found = true;
                    }
                }
            }
        }

        String[] geneLocalization =
                info[ArrayUtils.findColumn(arrayGeneToLocalization, "Subcellular location")].split("/");
        for (String compartment : geneLocalization) {
            if (!compartment.equals("Unknown")) {
                String nameCompartment =
                        SubCellCompartment.COMPARTMENT_NAMES[TypeCompartment.valueOf(compartment).ordinal()];
                /*
                 * Some of the compartment names are displayed in two lines so we need to modify the 2 lines in the
                 * SVG to display them in RED
                 */
                if (nameCompartment.equals("anchored to CM")) {
                    String nameToReplace = "anchored";
                    textSVG = textSVG.replaceFirst(suffix + nameToReplace, newSuffix + nameToReplace);
                    nameToReplace = "to CM";
                    textSVG = textSVG.replaceFirst(suffix + nameToReplace, newSuffix + nameToReplace);
                } else if (nameCompartment.equals("Protein complex")) {
                    String nameToReplace = "Protein";
                    textSVG = textSVG.replaceFirst(suffix + nameToReplace, newSuffix + nameToReplace);
                    nameToReplace = "complex";
                    textSVG = textSVG.replaceFirst(suffix + nameToReplace, newSuffix + nameToReplace);
                } else if (nameCompartment.equals("integral to CM")) {
                    String nameToReplace = "integral";
                    textSVG = textSVG.replaceFirst(suffix + nameToReplace, newSuffix + nameToReplace);
                    nameToReplace = "to  CM";
                    textSVG = textSVG.replaceFirst(suffix + nameToReplace, newSuffix + nameToReplace);
                } else if (nameCompartment.equals("Cytoplasmic Membrane")) {
                    String nameToReplace = "Cytoplasmic";
                    textSVG = textSVG.replaceFirst(suffix + nameToReplace, newSuffix + nameToReplace);
                    nameToReplace = "Membrane";
                    textSVG = textSVG.replaceFirst(suffix + nameToReplace, newSuffix + nameToReplace);
                } else {
                    textSVG = textSVG.replaceFirst(suffix + nameCompartment, newSuffix + nameCompartment);
                }
            }

        }

        if (tableLocalization != null) {
            updateTableSecretion(info, tableLocalization, arrayGeneToLocalization);
        }

        /*
         * Display homolog figure after conversion from SVG to JPG
         */
        try {
            File tempSVGFile = File.createTempFile(sequence.getName(), "Localization.svg");
            FileUtils.saveText(textSVG, tempSVGFile.getAbsolutePath());
            String html = SaveFileUtils.modifyHTMLwithFile(tempSVGFile.getAbsolutePath(),HTMLUtils.SVG);
            browserLocalization.setText(html);
            browserLocalization.redraw();
            tempSVGFile.deleteOnExit();

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * Update secretion table in Gene Viewer
     * 
     * @param info
     * @param tableLocalization
     * @param arrayGeneToLocalization
     */
    private static void updateTableSecretion(String[] info, Table tableLocalization,
            String[][] arrayGeneToLocalization) {
        tableLocalization.removeAll();
        tableLocalization.setHeaderVisible(true);
        tableLocalization.setLinesVisible(true);
        String[] titles = {"Information", "Secretion"};
        for (int i = 0; i < titles.length; i++) {
            TableColumn column = new TableColumn(tableLocalization, SWT.NONE);
            column.setText(titles[i]);
            column.setAlignment(SWT.LEFT);
        }
        // System.out.println(seq.getFeaturesText());
        for (int i = 2; i < info.length; i++) {
            // String refCurrent = sequence.getFoundIn().get(i);
            TableItem item = new TableItem(tableLocalization, SWT.NONE);
            item.setText(0, arrayGeneToLocalization[0][i]);
            item.setText(1, info[i]);

        }
        for (int i = 0; i < titles.length; i++) {
            tableLocalization.getColumn(i).pack();
        }
        tableLocalization.update();
        tableLocalization.redraw();
    }

}
