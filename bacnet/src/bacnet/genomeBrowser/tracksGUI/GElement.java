package bacnet.genomeBrowser.tracksGUI;

import org.biojava3.core.sequence.Strand;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import bacnet.datamodel.proteomics.TIS;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Codon;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Sequence;
import bacnet.genomeBrowser.core.Track;
import bacnet.genomeBrowser.core.Zoom;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.BasicColor;

/**
 * Here are all the methods for displaying each Genome element
 * 
 * @author UIBC
 *
 */
public class GElement {
    /**
     * Size of the arrow drawn in each rectangle to indicate strand
     */
    public static double ARROW_SIZE = 30;


    /**
     * 
     * @param gc
     * @param sequence
     * @param rectangle
     * @param track
     */
    public static void displayGene(GC gc, Sequence sequence, Rectangle rectangle, Track track, Color lightColor,
            Color darkColor, Color lineColor, Color textColor, boolean nTerm) {
        /*
         * Draw gene box (draw a rectangle and fill it
         */
        if (sequence instanceof TIS)
            ARROW_SIZE = 5;
        else
            ARROW_SIZE = 30;
        if (rectangle.width == 0)
            rectangle.width = 1;
        int sizeArrow = (int) ARROW_SIZE;
        /*
         * Draw lines to visually see the begin and end of each genome on data tracks
         */
        gc.setForeground(lineColor);
        if (!nTerm && rectangle.width > 35) {
            setMinimumAlpha(gc);
            gc.drawLine(rectangle.x, 0, rectangle.x, gc.getClipping().height);
            gc.drawLine(rectangle.x + rectangle.width, 0, rectangle.x + rectangle.width, gc.getClipping().height);

        }
        setDefaultAlpha(gc);
        if (sequence.isStrand()) {
            boolean displayEnd = true;
            if ((sequence.getEnd() - (int) ARROW_SIZE / 2) > track.getDisplayRegion().getX2())
                displayEnd = false;

            if (displayEnd) { // draw a rectangle with an arrow head
                if ((rectangle.width - (int) ARROW_SIZE) < 0) {
                    sizeArrow = rectangle.width;
                } else {
                    sizeArrow = (int) ARROW_SIZE;
                }
                int newX = rectangle.x + rectangle.width - sizeArrow;
                if (newX < rectangle.x)
                    newX = rectangle.x;
                int[] pointArray = {rectangle.x, rectangle.y, newX, rectangle.y, newX + sizeArrow,
                        rectangle.y + rectangle.height / 2, newX, rectangle.y + rectangle.height, rectangle.x,
                        rectangle.y + rectangle.height};
                // Fill the entire shape
                gc.setBackground(lightColor);
                gc.fillPolygon(pointArray);
                // fill the figure with a gradient color
                gc.setForeground(darkColor);
                gc.fillGradientRectangle(rectangle.x, rectangle.y, rectangle.width - sizeArrow, rectangle.height,
                        false);
                // draw the line surrounding the shape
                gc.setForeground(lineColor);
                gc.drawPolygon(pointArray);
            } else { // draw only a rectangle
                int[] pointArray = {rectangle.x + rectangle.width, rectangle.y, rectangle.x, rectangle.y, rectangle.x,
                        rectangle.y + rectangle.height, rectangle.x + rectangle.width, rectangle.y + rectangle.height,};
                // fill the figure with a gradient color
                gc.setBackground(lightColor);
                gc.setForeground(darkColor);
                gc.fillGradientRectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height, false);
                // draw the line surrounding the shape
                gc.setForeground(lineColor);
                gc.drawPolygon(pointArray);
            }
        } else {
            boolean displayEnd = true;
            if ((sequence.getBegin() + (int) ARROW_SIZE / 2) < track.getDisplayRegion().getX1())
                displayEnd = false;

            if (displayEnd) { // draw a rectangle with an arrow head
                if ((rectangle.width - (int) ARROW_SIZE) < 0) {
                    sizeArrow = rectangle.width;
                } else {
                    sizeArrow = (int) ARROW_SIZE;
                }
                int newX = rectangle.x + sizeArrow;
                if (newX > (rectangle.x + rectangle.width))
                    newX = rectangle.x;
                int[] pointArray = {newX, rectangle.y, rectangle.x + rectangle.width, rectangle.y,
                        rectangle.x + rectangle.width, rectangle.y + rectangle.height, newX,
                        rectangle.y + rectangle.height, rectangle.x, rectangle.y + rectangle.height / 2};
                // Fill the entire shape
                gc.setBackground(lightColor);
                gc.fillPolygon(pointArray);
                // fill the figure with a gradient color
                gc.setForeground(lightColor);
                gc.setBackground(darkColor);
                gc.fillGradientRectangle(rectangle.x + sizeArrow, rectangle.y, rectangle.width - sizeArrow,
                        rectangle.height, false);
                // draw the arrow
                gc.setForeground(lineColor);
                gc.drawPolygon(pointArray);
            } else { // draw only a rectangle
                int[] pointArray = {rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y,
                        rectangle.x + rectangle.width, rectangle.y + rectangle.height, rectangle.x,
                        rectangle.y + rectangle.height};
                // fill the figure with a gradient color
                gc.setForeground(lightColor);
                gc.setBackground(darkColor);
                gc.fillGradientRectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height, false);
                // draw the arrow
                gc.setForeground(lineColor);
                gc.drawPolygon(pointArray);
            }

        }

        /*
         * print name if necessary
         */
        int decayString = 5;
        if (sequence.isStrand()) {
            // set text box color
            gc.setForeground(textColor);
            gc.setBackground(darkColor);
        } else {
            // set text box color
            gc.setForeground(textColor);
            gc.setBackground(lightColor);
            if (!(sequence instanceof TIS)) {
                decayString = 30;
            }
        }
        if (rectangle.width > 300) {
            if (sequence instanceof Gene) {
                Gene gene = (Gene) sequence;
                if (!gene.getGeneName().equals("")) {
                    gc.drawString(gene.getName() + " - " + gene.getGeneName(), rectangle.x + decayString,
                            rectangle.y + rectangle.height / 2 - 7);
                } else {
                    gc.drawString(gene.getName(), rectangle.x + decayString, rectangle.y + rectangle.height / 2 - 7);
                }
                gc.drawString(((Gene) sequence).getProduct(), rectangle.x + decayString,
                        rectangle.y + rectangle.height / 2 + 5);
            } else if (sequence instanceof TIS) {
                TIS tis = (TIS) sequence;
                gc.drawString(tis.getName() + " - " + tis.getLength() + " " + tis.getRefSequence(),
                        rectangle.x + decayString, rectangle.y + rectangle.height / 2 + 5);
            } else {
                gc.drawString(sequence.getName(), rectangle.x + decayString, rectangle.y + rectangle.height / 2 - 7);
            }
        } else if (rectangle.width > 100) {
            if (sequence instanceof TIS) {
                TIS tis = (TIS) sequence;
                gc.drawString(tis.getName() + " - " + tis.getRefSequence(), rectangle.x + decayString,
                        rectangle.y + rectangle.height / 2 + 5);
            } else {
                String name = sequence.getName();
                if (sequence instanceof Gene) {
                    Gene gene = (Gene) sequence;
                    if (!gene.getGeneName().equals("")) {
                        name = gene.getName() + " - " + gene.getGeneName();
                    }
                }
                gc.drawString(name, rectangle.x + decayString, rectangle.y + rectangle.height / 2 - 7);
            }
        } else if (rectangle.width > 60) {
            String name = sequence.getName();
            if (sequence instanceof Gene) {
                Gene gene = (Gene) sequence;
                if (!gene.getGeneName().equals("")) {
                    name = gene.getGeneName();
                }
            }
            gc.drawString(name, rectangle.x + decayString, rectangle.y + rectangle.height / 2 - 7);
        }

    }

    public static void displayOperon(GC gc, String accession, int[] position, int geneSize, int height, Zoom zoom) {
        // System.out.println(accession+" "+position[0]+" "+geneSize+ " "+zoom.getZoomPosition());
        int zoomPos = zoom.getZoomNumber() - zoom.getZoomPosition();
        if (geneSize == 0)
            geneSize = 1;
        gc.fillGradientRectangle(position[0], position[1], geneSize, height, false);
        gc.setForeground(BasicColor.LINE_OPERON);
        gc.drawRectangle(position[0], position[1], geneSize, height);

    }

    public static void displayTerminator(GC gc, String accession, int[] position, int geneSize, int height, Zoom zoom) {
        // System.out.println(accession+" "+position[0]+" "+geneSize+ " "+zoom.getZoomPosition());
        if (zoom.getZoomPosition() > 3) {
            if (geneSize == 0)
                geneSize = 1;
            gc.fillOval(position[0], position[1], geneSize, height);
            gc.drawOval(position[0], position[1], geneSize, height);
        }
    }

    /**
     * Display sequence information:
     * <li>nucleotide sequence on plus strand
     * <li>nucleotide sequence on minus strand
     * <li>amino acid sequence for each nucleotide position on the plus strand (= the three reading
     * frame of the plus strand)
     * 
     * @param gc
     * @param chromosome
     * @param bpIndex
     * @param position
     * @param bpSizeH
     * @param zoom
     * @param type
     */
    public static void displaySequence(GC gc, Chromosome chromosome, int bpIndex, int[] position, double bpSizeH,
            Zoom zoom, int type) {
        // display sequence
        setDefaultFont(gc);
        gc.setBackground(BasicColor.WHITE);
        gc.setForeground(BasicColor.BLACK);
        if (bpSizeH > 1) {
            if (bpIndex > 0 && bpIndex < chromosome.getLength()) {
                NucleotideCompound compound = chromosome.getCompoundAt(bpIndex);
                switch (type) {
                    case 0: // display +strand sequence
                        gc.drawString(compound.toString(), position[0] + 1, position[1]);
                        break;
                    case 1: // display +strand amino acid
                        int codonFrame = bpIndex % 3;
                        if (codonFrame == 1)
                            gc.setBackground(BasicColor.WHITE);
                        else if (codonFrame == 2)
                            gc.setBackground(BasicColor.REDLIGHT_NUCLEOTIDE);
                        else
                            gc.setBackground(BasicColor.REDLIGHT_GENE);
                        String codon = chromosome.getSequenceAsString(bpIndex, bpIndex + 2, Strand.POSITIVE);
                        gc.drawString(Codon.getAminoAcid(codon), position[0] + 1, position[1]);
                        break;
                    case 2: // display -strand sequence
                        gc.drawString(compound.getComplement().toString(), position[0], position[1]);
                        break;
                    case 3: // display -strand amino acid
                        codonFrame = bpIndex % 3;
                        if (codonFrame == 1)
                            gc.setBackground(BasicColor.BLUELIGHT_GENE);
                        else if (codonFrame == 2)
                            gc.setBackground(BasicColor.BLUELIGHT_NUCLEOTIDE);
                        else
                            gc.setBackground(BasicColor.WHITE);
                        codon = chromosome.getSequenceAsString(bpIndex - 2, bpIndex, Strand.NEGATIVE);
                        gc.drawString(Codon.getAminoAcid(codon), position[0], position[1]);
                        break;
                }
            }
        }
        gc.setBackground(BasicColor.WHITE);
    }

    /**
     * Set Font to : "Arial",9,SWT.BOLD
     * 
     * @param gc
     */
    public static void setAnnotationFont(GC gc) {
        gc.setFont(SWTResourceManager.getBodyFont(10, SWT.BOLD));
    }

    /**
     * Set Font to : "Arial",12,SWT.NORMAL
     * 
     * @param gc
     */
    public static void setDefaultFont(GC gc) {
        gc.setFont(SWTResourceManager.getBodyFont(11, SWT.NORMAL));
    }

    /**
     * Set Font to : "Arial",11,SWT.BOLD
     * 
     * @param gc
     */
    public static void setDataNameFont(GC gc) {
        gc.setFont(SWTResourceManager.getBodyFont(10, SWT.BOLD));
    }

    /**
     * Set Alpha to 150
     * 
     * @param gc
     */
    public static void setExpressionAlpha(GC gc) {
        gc.setAlpha(150);
    }

    /**
     * Set Alpha to 15
     * 
     * @param gc
     */
    public static void setMinimumAlpha(GC gc) {
        gc.setAlpha(15);
    }

    /**
     * Set Alpha to 255
     * 
     * @param gc
     */
    public static void setDefaultAlpha(GC gc) {
        gc.setAlpha(255);
    }
}
