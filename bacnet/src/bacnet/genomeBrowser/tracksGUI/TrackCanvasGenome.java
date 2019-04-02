package bacnet.genomeBrowser.tracksGUI;


import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequence.Srna.TypeSrna;
import bacnet.genomeBrowser.core.Track;
import bacnet.utils.BasicColor;

public class TrackCanvasGenome extends Canvas implements MouseMoveListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1080025663659839957L;

    public static float RATIO_ANNOT_ELEMENT_SEPARATION = 0.1f;
    /**
     * Height of the bp position track
     */
    public static int HEIGHT_BPPOSITION = 20;

    /**
     * data to display
     */
    private Track track = new Track();

    /**
     * <code>Annotation</code> data for all Genome information
     */
    private Annotation annotation;

    /**
     * When mouse is clicked we displayed a line is displayed to show synteny: mouseYPosition gives the
     * X position
     */
    private int mouseXPosition = 0;
    /**
     * When mouse is clicked we displayed a line is displayed to show synteny: mouseYPosition gives the
     * Y position
     */
    private int mouseYPosition = 0;

    /**
     * If true we display a vertical line at the pointer of the mouse
     */
    private boolean displayMouseLine = false;

    // display arguments
    private int heightPix;
    private int widthBP;

    /**
     * Width in pixel of one base pair (fundamental unit for the rest of the display)
     */
    private double bpSizeH;

    /**
     * Height of one annnotation element
     */
    private int annotElementHeight;
    /**
     * Height of the separation between two annotation element
     */
    private int annotSeparationHeight;

    // number of annotation element to display
    private float annotNumber = 3.5f;
    private int annotSeparationNumber = 3;



    public TrackCanvasGenome(Composite parent, int style) {
        super(parent, style);
        this.setBackground(BasicColor.WHITE);

        /*
         * Set the size of the Canvas when resize is performed
         */
        this.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event e) {
                heightPix = getSize().y;
            }
        });
    }

    /**
     * Set the data of the Canvas, and implement the PaintListener
     * 
     * Execute different tasks:<br>
     * <li>Fix the number of pixel for one base pair
     * <li>Set width and height parameters for data and annotation track Display in order:<br>
     * <li>different line and ticks for the base pair position, every display needed in the background
     * <li>display all the data
     * <li>display sequence if needed
     * <li>display annotation
     * <li>different genome name and mouse line in foreground
     * 
     * @param tracks <code>Track</code>
     * @param data <code>TrackData</code>
     */
    public void setTrack(Track track) {
        this.track = track;
        this.annotation = track.getChromosome().getAnnotation();

        /*
         * set PaintListener
         */
        this.addPaintListener(new PaintListener() {
            private static final long serialVersionUID = -7537187200585736369L;

            @Override
            public void paintControl(PaintEvent e) {
                GElement.setDefaultAlpha(e.gc);
                GElement.setDefaultFont(e.gc);
                e.gc.setAdvanced(true);
                e.gc.setBackground(BasicColor.WHITE);
                e.gc.setForeground(BasicColor.BLACK);
                e.gc.fillRectangle(e.x, e.y, e.width, e.height);


                /*
                 * Set parameters
                 */
                setHorizontalDisplayParameter(e);
                setVerticalDisplayParameter(e.gc);
                // System.err.println("TrackGenome");
                // System.out.println("Fixed parameters, widthBP:"+widthBP+" bpSizeH: "+bpSizeH);
                // System.out.println("Zoom: "+getTrack().getZoom().getZoomRatio()+" zoompos:
                // "+getTrack().getZoom().getZoomPosition());
                // System.out.println("Display bp: "+track.getDisplayRegion().getX1()+" -
                // "+track.getDisplayRegion().getX2()+" : "+track.getDisplayRegion().getWidth());
                // int center = (track.getDisplayRegion().getX1()+track.getDisplayRegion().getX2())/2;
                // System.out.println("Pixel width: "+e.width+" center "+e.width/2+"pix it should be
                // "+convertBPtoX(center)+" (for "+center+" bp)");
                /*
                 * Display Tracks
                 */
                displayLegendBefore(e);
                displayAnnotation(e.gc);
                displayLegendAfter(e.gc);
            }
        });
        this.redraw();
    }

    /**
     * Fix the number of pixel for one base pair<br>
     * <li>bpSizeH = widthPix / (double) widthBP<br>
     * 
     * @return true if the value has been changed
     */
    private boolean setHorizontalDisplayParameter(PaintEvent e) {
        /*
         * fix bpSizeH
         */
        widthBP = track.getDisplayRegion().getWidth();
        double previousBPSizeH = bpSizeH;
        bpSizeH = (double) e.width / (double) widthBP;
        if (previousBPSizeH != bpSizeH)
            return true;
        else
            return false;
    }

    /**
     * Set different parameters needed for displaying Genome viewer<br>
     * <li><code>annotNumber</code>
     * <li><code>annotSeparationNumber</code>
     * <li><code>annotSeparationSize</code>
     * <li><code>annotElementHeight</code>
     * <li><code>annotSeparationHeight</code>
     */
    private void setVerticalDisplayParameter(GC gc) {
        /*
         * Set Number of element to display in the annotation Track
         */
        if (annotation.getNbElementType() == 5) {
            annotNumber = 3.5f;
            annotSeparationNumber = 3;
        } else {
            annotNumber = 3.7f;
            annotSeparationNumber = 2;
        }

        /*
         * calculate annot size parameters
         */
        float ratio = annotNumber + RATIO_ANNOT_ELEMENT_SEPARATION * annotSeparationNumber;
        // System.out.println("Annot "+RATIO_ANNOT_ELEMENT_SEPARATION+" "+annotNumber+"
        // "+annotSeparationNumber);
        annotElementHeight = (int) (1 / ratio * (heightPix - HEIGHT_BPPOSITION));
        // System.out.println(ratio);
        annotSeparationHeight = (int) (RATIO_ANNOT_ELEMENT_SEPARATION * annotElementHeight);
        // System.out.println("element: "+annotElementHeight+" sep "+annotSeparationHeight);
    }

    /**
     * Return the sequence from the annotation to display<br>
     * The position of this sequence is different from the input one, only if:<br>
     * <li>begin < displayRegion.X1
     * <li>end > displayRegion.X2
     * 
     * @param sequence
     * @return
     */
    private Sequence getDisplayedSequence(Sequence sequence) {
        int begin = sequence.getBegin();
        int end = sequence.getEnd();
        if (begin < track.getDisplayRegion().getX1()) {
            begin = track.getDisplayRegion().getX1();
        }
        if (end > track.getDisplayRegion().getX2()) {
            end = track.getDisplayRegion().getX2();
        }
        Sequence displaySequence = new Sequence("display", begin, end, sequence.getStrand());
        return displaySequence;
    }

    /**
     * Display all annotation info which are available
     * 
     * @param gc
     */
    private void displayAnnotation(GC gc) {
        Chromosome chromo = track.getChromosome();
        Annotation annot = chromo.getAnnotation();
        GElement.setAnnotationFont(gc);
        int beginDraw = track.getDisplayRegion().getX1();
        int endDraw = track.getDisplayRegion().getX2();
        ArrayList<Sequence> sequences = annot.getElements(chromo, beginDraw, endDraw);
        // System.err.println(sequences.size());
        int[] position = new int[2];
        for (Sequence sequence : sequences) {
            Sequence displaySequence = getDisplayedSequence(sequence);
            int geneSize = (int) (bpSizeH * displaySequence.getLength());
            // display element
            Color lightColor = null;
            Color darkColor = null;
            Color lineColor = null;
            Color textColor = BasicColor.WHITE;
            switch (sequence.getType()) {
                case Gene:
                    Gene gene = (Gene) sequence;
                    if (sequence.isStrand()) { // plus genes
                        if (gene.getComment().contains("RAST")) {
                            textColor = BasicColor.BLACK;
                            lightColor = BasicColor.LIGHTGREY;
                            darkColor = BasicColor.LIGHTERGREY;
                            lineColor = BasicColor.GREY;
                            position = getAnnotationPosition(displaySequence.getBegin(), -2);
                            Rectangle rectangle = new Rectangle(position[0], position[1] + annotElementHeight, geneSize,
                                    annotElementHeight);
                            GElement.displayGene(gc, sequence, rectangle, track, lightColor, darkColor, lineColor,
                                    textColor, false);
                        } else {
                            lightColor = BasicColor.REDLIGHT_GENE;
                            darkColor = BasicColor.REDDARK_GENE;
                            lineColor = BasicColor.REDLINE_GENE;
                            position = getAnnotationPosition(displaySequence.getBegin(), -1);
                            Rectangle rectangle =
                                    new Rectangle(position[0], position[1], geneSize, annotElementHeight * 2);
                            GElement.displayGene(gc, sequence, rectangle, track, lightColor, darkColor, lineColor,
                                    textColor, false);
                        }

                    } else { // minus genes
                        if (gene.getComment().contains("RAST")) {
                            textColor = BasicColor.BLACK;
                            lightColor = BasicColor.LIGHTGREY;
                            darkColor = BasicColor.LIGHTERGREY;
                            lineColor = BasicColor.GREY;
                            position = getAnnotationPosition(displaySequence.getBegin(), -1);
                            Rectangle rectangle = new Rectangle(position[0], position[1], geneSize, annotElementHeight);
                            GElement.displayGene(gc, sequence, rectangle, track, lightColor, darkColor, lineColor,
                                    textColor, false);
                        } else {
                            lightColor = BasicColor.BLUELIGHT_GENE;
                            darkColor = BasicColor.BLUEDARK_GENE;
                            lineColor = BasicColor.BLUELINE_GENE;
                            position = getAnnotationPosition(displaySequence.getBegin(), -2);
                            Rectangle rectangle =
                                    new Rectangle(position[0], position[1], geneSize, annotElementHeight * 2);
                            GElement.displayGene(gc, sequence, rectangle, track, lightColor, darkColor, lineColor,
                                    textColor, false);
                        }

                    }
                    break;
                case NcRNA:
                    textColor = BasicColor.BLACK;
                    lightColor = BasicColor.LIGHT_NCRNA;
                    darkColor = BasicColor.DARK_NCRNA;
                    lineColor = BasicColor.LINE_NCRNA;
                    position = getAnnotationPosition(displaySequence.getBegin(), -1);
                    Rectangle rectangle = new Rectangle(position[0], position[1], geneSize, annotElementHeight * 2);
                    GElement.displayGene(gc, sequence, rectangle, track, lightColor, darkColor, lineColor, textColor,
                            false);
                    break;
                case Srna: // sRNA and riboswitch
                    Srna sRNA = (Srna) sequence;
                    if (sRNA.getTypeSrna() == TypeSrna.Srna) {
                        lightColor = BasicColor.LIGHT_SRNA;
                        darkColor = BasicColor.DARK_SRNA;
                        lineColor = BasicColor.LINE_SRNA;
                    } else if (sRNA.getTypeSrna() == TypeSrna.CisReg) {
                        lightColor = BasicColor.LIGHT_CISREG;
                        darkColor = BasicColor.DARK_CISREG;
                        lineColor = BasicColor.LINE_CISREG;
                    } else { // asRNA
                        lightColor = BasicColor.LIGHT_ASRNA;
                        darkColor = BasicColor.DARK_ASRNA;
                        lineColor = BasicColor.LINE_ASRNA;
                    }
                    if (sequence.isStrand()) {
                        position = getAnnotationPosition(displaySequence.getBegin(), -3);
                    } else {
                        position = getAnnotationPosition(displaySequence.getBegin(), -4);
                    }
                    Rectangle rectangleSrna = new Rectangle(position[0], position[1], geneSize, annotElementHeight * 1);
                    GElement.displayGene(gc, sequence, rectangleSrna, track, lightColor, darkColor, lineColor,
                            textColor, false);
                    break;
                case Operon: // Operon
                    gc.setBackground(BasicColor.LIGHT_OPERON);
                    gc.setForeground(BasicColor.DARK_OPERON);
                    position = getAnnotationPosition(displaySequence.getBegin(), -5);
                    GElement.displayOperon(gc, sequence.getName(), position, geneSize, annotElementHeight / 2,
                            track.getZoom());
                    break;
                case terminator:
                    gc.setBackground(BasicColor.LIGHTBLUE);
                    position = getAnnotationPosition(displaySequence.getBegin(), -5);
                    GElement.displayTerminator(gc, sequence.getName(), position, geneSize, annotElementHeight / 2,
                            track.getZoom());
                    break;
                case unknown:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Display all decorative elements = not annotation, not data
     * 
     * @param gc
     */
    private void displayLegendBefore(PaintEvent e) {
        /*
         * Print nbOfLegend different base pair positions on the legend
         */
        GElement.setDefaultFont(e.gc);
        int nbOfLegend = 11;
        int length = track.getDisplayRegion().getWidth();
        double legendRatio = length / nbOfLegend;
        e.gc.drawLine(0, HEIGHT_BPPOSITION, e.width, HEIGHT_BPPOSITION);

        for (int i = 0; i < nbOfLegend; i++) {
            int xPos = track.getDisplayRegion().getX1() + (int) Math.rint(legendRatio * i);
            int[] position = {convertBPtoX(xPos), HEIGHT_BPPOSITION};
            String bpName = xPos + " bp";
            if (track.getDisplayRegion().getWidth() > 10000) {
                double xPosFloat = (double) xPos / 1000;
                xPosFloat = Double.parseDouble(String.format("%.2f", xPosFloat).replaceFirst(",", "."));
                bpName = xPosFloat + "kbp";
            }

            e.gc.drawString(bpName + "", position[0] + 5, position[1] - 17);
            e.gc.drawLine(position[0], position[1], position[0], position[1] - 5);
        }

    }

    /**
     * Display all decorative elements which will be draw at the end, so on every other elements
     * 
     * @param gc
     */
    private void displayLegendAfter(GC gc) {
        /*
         * display Genome name
         */
        GElement.setDefaultFont(gc);
        String ret = track.getGenomeName() + " : " + track.getChromosome().getLength() + " (bp)";
        gc.setForeground(BasicColor.BLACK);
        gc.setBackground(BasicColor.WHITE);
        int[] position = getAnnotationPosition(0, -5);
        gc.drawString(ret, 5, position[1]);

        if (isDisplayMouseLine()) {
            gc.setLineWidth(2);
            gc.setForeground(BasicColor.BLACK);
            gc.setBackground(BasicColor.WHITE);
            gc.drawLine(getMouseXPosition(), 0, getMouseXPosition(), heightPix);
            gc.drawString(this.convertXtoBP(getMouseXPosition()) + " bp", getMouseXPosition() + 5,
                    getMouseYPosition() - 15);
            gc.setLineWidth(1);
        }
    }


    /**
     * Return correct positions for drawing annotation elements
     * 
     * @param bpIndex position in the genome
     * @param dataIndex type of data to display
     *        <ul>
     *        <li>-1 plus genes tRNA and rRNA
     *        <li>-2 minus genes
     *        <li>-3 sRNA riboswitch and asRNA
     *        <li>-4 operon and terminator
     *        <li>1 sequence
     *        <li>2 legend
     *        </ul>
     * @return int[] = {x,y}, positions for drawing the corresponding element
     */
    private int[] getAnnotationPosition(int bpIndex, int dataIndex) {
        int x = convertBPtoX(bpIndex);
        int y = 0;
        switch (dataIndex) {
            case -1:
                y = HEIGHT_BPPOSITION + annotElementHeight / 2 + 2 * annotSeparationHeight;
                break; // display + gene
            case -2:
                y = HEIGHT_BPPOSITION + annotElementHeight + annotElementHeight / 2 + 2 * annotSeparationHeight;
                break; // display - gene
            case -3:
                y = HEIGHT_BPPOSITION + annotElementHeight + annotElementHeight / 2 + 2 * annotSeparationHeight;
                break; // display sRNA +
            case -4:
                y = HEIGHT_BPPOSITION + 2 * annotElementHeight + annotElementHeight / 2 + 2 * annotSeparationHeight;
                break; // display sRNA -
            case -5:
                y = HEIGHT_BPPOSITION + annotSeparationHeight;
                break; // display operon plus + terminator
        }
        int[] ret = {x, y};
        return ret;
    }


    /**
     * Convert position in the genome into a position in the display<br>
     * (inverse function of xImageTobasepair(int x))
     * 
     * @param bpIndex in the genome
     * @return x position in the view
     */
    private int convertBPtoX(int bpIndex) {
        int x = (int) ((bpIndex - track.getDisplayRegion().getX1()) * bpSizeH);
        return x;
    }

    /**
     * Convert position in the display to a position in the genome<br>
     * (inverse function of basepairToXimage(int bpIndex))
     * 
     * @param x position in the view
     * @return bpIndex in the genome
     */
    public int convertXtoBP(int x) {
        int basePair = (int) (x / bpSizeH + track.getDisplayRegion().getX1());
        return basePair;
    }

    /**
     * If the mouse is over the genome viewer, detect it is on the annotation, and display the
     * corresponding gene, sRNA, asRNA
     * 
     * @param e
     */
    @Override
    public void mouseMove(MouseEvent e) {
        if (isDisplayMouseLine()) {
            /*
             * We display a line and bp info at mouse pointer
             */
            setMouseXPosition(e.x);
            setMouseYPosition(e.y);
            this.redraw();
        } else {
            // System.out.println(e.getSource() + " " +e.x+" y "+ e.y);
            String info = "";
            int basePair = convertXtoBP(e.x);
            Sequence seq = track.getChromosome().getAnnotation().getElementInfoATbp(track.getChromosome(), basePair);
            if (seq != null) {
                String ret = seq.getName() + "\n" + seq.getBegin() + ".." + seq.getEnd() + " (" + seq.getStrand() + ")";
                if (seq instanceof Gene) {
                    Gene gene = (Gene) seq;
                    ret += "\n" + gene.getInfo();
                } else if (seq instanceof Srna) {
                    Srna sRNA = (Srna) seq;
                    ret += "\n" + sRNA.getRef();
                }
                info = ret;

            }
            if (!info.equals("")) {
                this.setToolTipText(info);
            }
        }

    }

    public Object clickedElement(MouseEvent e) {
        String info = "";
        int basePair = convertXtoBP(e.x);
        Sequence seq = track.getChromosome().getAnnotation().getElementInfoATbp(track.getChromosome(), basePair);
        if (seq != null) {
            return seq;
        }
        if (!info.equals("")) {
            this.setToolTipText(info);
        }
        return null;
    }


    /*
     * ******************************************************************************
     * 
     * GETTER and SETTER
     * 
     * ******************************************************************************
     */

    public int getMouseXPosition() {
        return mouseXPosition;
    }

    public void setMouseXPosition(int mouseXPosition) {
        this.mouseXPosition = mouseXPosition;
    }

    public int getMouseYPosition() {
        return mouseYPosition;
    }

    public void setMouseYPosition(int mouseYPosition) {
        this.mouseYPosition = mouseYPosition;
    }

    public boolean isDisplayMouseLine() {
        return displayMouseLine;
    }

    public void setDisplayMouseLine(boolean displayMouseLine) {
        this.displayMouseLine = displayMouseLine;
    }

    public Track getTrack() {
        return track;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public int getHeightPix() {
        return heightPix;
    }

    public void setHeightPix(int heightPix) {
        this.heightPix = heightPix;
    }

    public int getWidthBP() {
        return widthBP;
    }

    public void setWidthBP(int widthBP) {
        this.widthBP = widthBP;
    }

    public double getBpSizeH() {
        return bpSizeH;
    }

    public void setBpSizeH(double bpSizeH) {
        this.bpSizeH = bpSizeH;
    }

    public int getAnnotElementHeight() {
        return annotElementHeight;
    }

    public void setAnnotElementHeight(int annotElementHeight) {
        this.annotElementHeight = annotElementHeight;
    }

    public int getAnnotSeparationHeight() {
        return annotSeparationHeight;
    }

    public void setAnnotSeparationHeight(int annotSeparationHeight) {
        this.annotSeparationHeight = annotSeparationHeight;
    }

    public float getAnnotNumber() {
        return annotNumber;
    }

    public void setAnnotNumber(float annotNumber) {
        this.annotNumber = annotNumber;
    }

    public int getAnnotSeparationNumber() {
        return annotSeparationNumber;
    }

    public void setAnnotSeparationNumber(int annotSeparationNumber) {
        this.annotSeparationNumber = annotSeparationNumber;
    }

}
