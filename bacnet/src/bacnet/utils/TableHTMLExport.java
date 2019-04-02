package bacnet.utils;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JTable;

import bacnet.reader.TabDelimitedTableReader;

public class TableHTMLExport {

    public static void exportInHTML(JTable table, String fileName) {

        // first save in an image
        BufferedImage tamponSauvegarde =
                new BufferedImage(table.getWidth(), table.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = tamponSauvegarde.getGraphics();
        table.paint(g);
        try {
            ImageIO.write(tamponSauvegarde, "PNG", new File(fileName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // then create an HTML file containing all the info for the table
        String[][] tableSave = new String[table.getRowCount() * table.getColumnCount() + 2][1];
        String html =
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//\" \"http://www.w3.org/TR/html4/frameset.dtd\">\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" "
                        + "content=\"text/html;charset=\"ISO-8859-1\">\n<title>Big Table</title>\n</head>\n<body>\n";
        html += "<h3>" + FileUtils.removeExtensionAndPath(fileName) + " : " + table.getRowCount() + " RNAs "
                + table.getColumnCount() + " genomes</h3>";
        html += "<img style=\"border:0\" src=\"" + FileUtils.removePath(fileName) + "\" width=\"" + table.getWidth()
                + "\" height=\"" + table.getHeight() + "\" usemap=\"#tablemap\" />";
        html += "<map id=\"tablemap\" name=\"tablemap\">";
        tableSave[0][0] = html;
        // go through every cell and write the appropriate link and tooltips
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                String value = table.getModel().getValueAt(i, j) + "";
                if (!value.equals("0.0")) {
                    String sRNA = ((String) table.getModel().getValueAt(i, 0));
                    // System.out.println(value+" ");
                    Rectangle rect = table.getCellRect(i, j, false);
                    html = "<area shape=\"rect\" coords=";
                    html += "\"" + rect.getMinX() + "," + rect.getMinY() + "," + rect.getMaxX() + "," + rect.getMaxY()
                            + "\"";
                    html += " href=\"sRNAs" + File.separator + sRNA + ".html\" title=\"";
                    if (j == 0)
                        html += value;
                    else
                        html += sRNA + " vs " + table.getModel().getColumnName(j) + " Autoscore: " + value;
                    html += "\"/>";
                    tableSave[i * table.getColumnCount() + j + 1][0] = html;
                } else {
                    tableSave[i * table.getColumnCount() + j + 1][0] = "";
                }
            }
        }
        tableSave[table.getRowCount() * table.getColumnCount() + 1][0] = "</map></body></html>";
        TabDelimitedTableReader.save(tableSave, FileUtils.removeExtension(fileName) + ".html");
    }
}
