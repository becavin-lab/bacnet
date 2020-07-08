package bacnet.genomeBrowser.tracksGUI;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
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
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.ProteomicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.proteomics.TIS;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.genomeBrowser.core.Track;
import bacnet.genomeBrowser.core.Track.DisplayType;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.BasicColor;
import bacnet.utils.ExpressionMatrixStat;

/**
 * A TrackCanvasData is the Canvas in which will be displayed datasets
 * 
 * @author christophebecavin
 *
 */
public class TrackCanvasData extends Canvas implements MouseMoveListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1080025663659839957L;

    /**
     * Minimal height of a data track
     */
    public int MINIMUM_DATA_SIZE = 20;
    /**
     * Height of the sequence track (when displayed)
     */
    public static int HEIGHT_SEQUENCE = 60;

    private String Name = "TrackCanvasData";

    private Composite trackComposite;
    // data to display
    /**
     * All information about the data to display
     */
    private Track track = new Track();

    /**
     * <code>Annotation</code> data for all Genome information
     */
    private Annotation annotation;

    /**
     * NTerm which will be highlighted
     */
    private NTerm nTermHighlight;
    /**
     * Type of NTerm to display, if = All display all types
     */
    private String nTermTypeOverlap = "All";
    /**
     * MassSpecData where the selected NTerm is
     */
    private NTermData massSpecData;

    /*
     * Chromosome Id used currently
     */
    private String genomeName = "";
    /*
     * Chromosome Id used currently
     */
    private String chromoID = "";

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
    /**
     * Height in pixels of the drawing area
     */
    private int heightPix;
    /**
     * Width in Base pairs of the drawing area
     */
    private int widthBP;

    // data related parameters
    /**
     * Height of all the data window = all data combine
     */
    private int dataWindowSize;
    /**
     * Vertical decay imposed by the vertical slider
     */
    private int decaySliderVBar = 0;
    /**
     * Vertical zoom
     */
    private double zoomVertical = 1;
    /**
     * Height of on data
     */
    private int dataSizeReference;
    /**
     * Height of the sequence track
     */
    private int sequenceHeight = 0;
    /**
     * Width in pixel of one base pair (fundamental unit for the rest of the display)
     */
    private double bpSizeH;

    private boolean testData = false;
    private TreeMap<String, String[][]> saveData = new TreeMap<>();

    public TrackCanvasData(Composite parent, int style, Composite trackComposite) {
        super(parent, style);
        this.trackComposite = trackComposite;
        this.setBackground(BasicColor.WHITE);
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
        this.chromoID = track.getChromosomeID();
        this.genomeName = track.getGenomeName();
        // System.out.println("size canvas start: "+this.getSize().x+" -
        // "+this.getSize().y);

        /*
         * Set the size of the Canvas when resize is performed
         */
        this.addListener(SWT.Resize, new Listener() {
            /**
             * 
             */
            private static final long serialVersionUID = -2783714325835743163L;

            @Override
            public void handleEvent(Event e) {
                // System.out.println("Resize canvas start: "+getSize().x+" - "+getSize().y);
                heightPix = getSize().y;
            }
        });

        /*
         * set PaintListener
         */
        this.addPaintListener(new PaintListener() {
            private static final long serialVersionUID = -7537187200585736369L;

            @Override
            public void paintControl(PaintEvent e) {
                // System.out.println("PaintControl executed:"+e.display+" in "+e.widget+",
                // size:"+e.x+" "+e.y+"
                // "+e.width+" "+e.height+" at "+e.time);
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
                // System.err.println("TrackData");
                // System.out.println("Fixed parameters, widthBP:"+widthBP+" bpSizeH:
                // "+bpSizeH);
                // System.out.println("Zoom: "+getTrack().getZoom().getZoomRatio()+" zoompos:
                // "+getTrack().getZoom().getZoomPosition());
                // System.out.println("Display bp: "+track.getDisplayRegion().getX1()+" -
                // "+track.getDisplayRegion().getX2()+" :
                // "+track.getDisplayRegion().getWidth());
                // int center =
                // (track.getDisplayRegion().getX1()+track.getDisplayRegion().getX2())/2;
                // System.out.println("Pixel width: "+e.width+" center "+e.width/2+"pix it
                // should be
                // "+convertBPtoX(center)+" (for "+center+" bp)");
                /*
                 * Display Tracks
                 */
                if (track.isDisplaySequence())
                    displaySequence(e.gc);
                displayData(e.gc);
                displayLegendAfter(e.gc);

                /*
                 * For testing the display need to save data displayed
                 */
                if (testData) {
                    String[][] saveDataFinal = new String[saveData.keySet().size() * 2][widthBP + 1];
                    int i = 0;
                    for (String dataName : saveData.keySet()) {
                        saveDataFinal[i][0] = dataName;
                        saveDataFinal[i + 1][0] = dataName;
                        for (int j = 0; j < widthBP; j++) {
                            saveDataFinal[i][j + 1] = saveData.get(dataName)[0][j];
                            saveDataFinal[i + 1][j + 1] = saveData.get(dataName)[1][j];
                        }
                        i++;
                        i++;
                    }
                    // TabDelimitedTableReader.save(saveDataFinal,"D:/"+getName()+"_"+this.hashCode()+".excel");
                    TabDelimitedTableReader.save(saveDataFinal, "D:/" + getCanvasName() + ".excel");
                }
            }
        });

        this.redraw();
    }

    /**
     * Fix the number of pixel for one base pair<br>
     * <li>bpSizeH = widthPix / (double) widthBP<br>
     * <li>bpSizeV = heightPix / number of data
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
     * <li><code>annotLine</code>
     * <li><code>dataWindowSize</code>
     * <li><code>dataSize</code>
     * <li><code>dataSeparationSize</code>
     */
    private void setVerticalDisplayParameter(GC gc) {
        /*
         * If sequence is displayed we assign sequenceHeight
         */
        if (track.isDisplaySequence()) {
            sequenceHeight = HEIGHT_SEQUENCE;
        } else {
            sequenceHeight = 0;
        }
        /*
         * the size of datawindow is dependent of heightpix and sequenceHeight
         */
        heightPix = this.getSize().y;
        dataWindowSize = heightPix - sequenceHeight;

        float totalSize = track.getDatas().getDataTotalSize(track.getDisplayType()) * (float) zoomVertical;
        dataSizeReference = (int) (dataWindowSize / totalSize);
        // System.out.println("window: "+dataWindowSize+" TotalSize: "+totalSize+"
        // SizeReference:
        // "+dataSizeReference);
        // System.out.println("Minimum: "+MINIMUM_DATA_SIZE);
        if (dataSizeReference < MINIMUM_DATA_SIZE * zoomVertical) {
            dataSizeReference = (int) (MINIMUM_DATA_SIZE * zoomVertical);
            // System.out.println("New window: "+dataWindowSize+" TotalSize: "+totalSize+"
            // SizeReference:
            // "+dataSizeReference);
        }
        dataSizeReference = (int) (dataSizeReference * zoomVertical);
        dataWindowSize = (int) (dataSizeReference * totalSize);
        heightPix = dataWindowSize + sequenceHeight;
        if (this.getTrackComposite() instanceof TracksComposite) {
            TracksComposite trackComposite = (TracksComposite) this.getTrackComposite();
            trackComposite.setVerticalBarProperties();
        }
        if (this.getTrackComposite() instanceof TracksComposite) {
            TracksComposite trackComposite = (TracksComposite) this.getTrackComposite();
            trackComposite.setVerticalBarProperties();
        }

        /*
         * If testData is true we save a matrix representation of the genome viewer
         */
        if (testData) {
            // saveData = new String[track.getDatas().getDataCount()*10+2][widthBP+1];
            for (String names : track.getDatas().getDataDisplayedNames()) {
                String[][] saveDataArray = new String[2][widthBP];
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < widthBP; j++) {
                        saveDataArray[i][j] = "";
                    }
                }
                saveData.put(names, saveDataArray);
            }
            // System.out.println((widthBP+1)+" "+track.getDatas().getDataCount());
        }
    }

    /**
     * From the value of the data at a given genomic position, this function return the position in the
     * <code>GC</code>
     * 
     * @param bpIndex position in the genome
     * @param dataIndex index of the current data used
     * @param value of the data
     * @param min minimum of this data
     * @param max maximum of this data
     * @return vector int[] = [x,y]
     */
    private int[] getDataPosition(String dataName, int bpIndex, int dataIndex, double value, double min, double max,
            int minPosition) {
        /*
         * Convert x position (from bp to pixel)
         */
        int x = convertBPtoX(bpIndex);

        /*
         * Calculate y position depending on the data size
         */
        int y = 0;
        if (track.getDisplayType() == DisplayType.BIOCOND) {
            y = minPosition;
            y += (int) ((max - value)
                    * (dataSizeReference * zoomVertical * track.getDatas().getBioCondSize(dataName) / (max - min)));
        } else if (track.getDisplayType() == DisplayType.DATA) {
            y = minPosition;
            y += (int) ((max - value)
                    * (dataSizeReference * zoomVertical * track.getDatas().getDataSize().get(dataName) / (max - min)));
        } else {
            y = (int) ((max - value) * (dataSizeReference * zoomVertical / (max - min)));
        }

        /*
         * If the slider has been used we decay the display
         */
        y += decaySliderVBar;
        /*
         * Return a vector [x,y]
         */
        int[] ret = {x, y};
        return ret;
    }

    /**
     * Return the position in pixel of the minimal value
     * 
     * @param dataName
     * @param dataIndex
     * @param min
     * @param max
     * @return
     */
    private int getDataPositionMin(String dataName, int dataIndex, double min, double max) {
        /*
         * Convert y position
         */
        int y = 0;
        if (track.getDisplayType() == DisplayType.BIOCOND) {
            boolean found = false;
            for (String previousBioCondName : track.getDatas().getBioConditionHashMaps().keySet()) {
                if (previousBioCondName.equals(dataName)) {
                    found = true;
                }
                if (!found) {
                    float dataSize = track.getDatas().getBioCondSize(previousBioCondName);
                    y += dataSizeReference * zoomVertical * dataSize;
                }
            }
        } else if (track.getDisplayType() == DisplayType.DATA) {
            for (int i = 0; i < dataIndex; i++) {
                String previousDataName = track.getDatas().getDataDisplayedNames().get(i);
                float dataSize = track.getDatas().getDataSize().get(previousDataName);
                y += dataSizeReference * zoomVertical * dataSize;
            }
        }
        return y;
    }

    /**
     * Display every type of data in a GC (Graphic context)<br>
     * THE CODE CAN BE IMPROVE HERE
     * 
     * @param gc
     */
    private void displayData(GC gc) {
        GElement.setDefaultFont(gc);
        /*
         * Depending of DisplayType we have different way of representation
         */
        if (track.getDisplayType() == DisplayType.OVERLAY) {
            displayDataOVERLAY(gc);
        } else if (track.getDisplayType() == DisplayType.BIOCOND) {
            displayDataBIOCOND(gc);
        } else if (track.getDisplayType() == DisplayType.DATA) {
            displayDataDATASEPARATED(gc);
        }

    }

    /**
     * Method run when track are overlayed together
     * 
     * @param gc
     */
    private void displayDataOVERLAY(GC gc) {
        double max = track.getDatas().getMax();
        double min = track.getDatas().getMin();
        min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
        max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
        int[] position = getDataPosition("All", 1, 0, 0, min, max, 0);
        displayDataLegend(gc, "all Data", position, min, max, 0, 0, TypeData.unknown);
        GElement.setExpressionAlpha(gc);
        /*
         * Display Tiling datasets
         */
        displayTiling(gc, track.getDatas().getTilings(), min, max, 0, 0);
        /*
         * Display GeneExpression datasets
         */
        displayGeneExpression(gc, track.getDatas().getGeneExprs(), min, max, 0, 0);
        /*
         * Display ExpressionMatrices
         */
        displayExpressionMatrix(gc, track.getDatas().getMatrices(), min, max, 0, 0);
        /*
         * Select only TSS and display them
         */
        ArrayList<ExpressionData> seqDatasTemp = new ArrayList<ExpressionData>();
        for (ExpressionData seqData : track.getDatas().getTSSDatas()) {
            if (seqData.getChromosomeID().equals(chromoID)) {
                seqDatasTemp.add(seqData);
            }
        }
        displayTSS(gc, seqDatasTemp, min, max, 0, 0);
        /*
         * Select only TermSeqs and display them
         */
        seqDatasTemp.clear();
        for (ExpressionData seqData : track.getDatas().getTermSeqDatas()) {
            if (seqData.getChromosomeID().equals(chromoID)) {
                seqDatasTemp.add(seqData);
            }
        }
        displayTermSeq(gc, seqDatasTemp, min, max, 0, 0);
        /*
         * Select only RNASeq and display them
         */
        seqDatasTemp.clear();
        for (ExpressionData seqData : track.getDatas().getRNASeqDatas()) {
            if (seqData.getChromosomeID().equals(chromoID)) {
                seqDatasTemp.add(seqData);
            }
        }
        displayRNASeq(gc, seqDatasTemp, min, max, 0, 0);
        /*
         * Select only DNASeq and display them
         */
        seqDatasTemp.clear();
        for (ExpressionData seqData : track.getDatas().getDNASeqDatas()) {
            if (seqData.getChromosomeID().equals(chromoID)) {
                seqDatasTemp.add(seqData);
            }
        }
        displayRNASeq(gc, seqDatasTemp, min, max, 0, 0);
        /*
         * Select only Riboseq and display them
         */
        seqDatasTemp.clear();
        seqDatasTemp = new ArrayList<ExpressionData>();
        for (ExpressionData seqData : track.getDatas().getRiboSeqDatas()) {
            if (seqData.getChromosomeID().equals(chromoID)) {
                seqDatasTemp.add(seqData);
            }
        }
        displayRNASeq(gc, seqDatasTemp, min, max, 0, 0);
        /*
         * Display Proteomics datasets 
         */
        displayProteomicsData(gc, track.getDatas().getProteomes(), min, max, 0, 0);
        GElement.setDefaultAlpha(gc);
        /*
         * Display NTerminomics datsets 
         */
        displayNTerm(gc, track.getDatas().getNTermDatas(), min, max, 0, 0);
        GElement.setExpressionAlpha(gc);
        /*
         * Finalize data display
         */
        gc.setBackground(BasicColor.WHITE);
        gc.setForeground(BasicColor.BLACK);
        GElement.setDataNameFont(gc);
        TreeSet<TypeData> typeData = new TreeSet<OmicsData.TypeData>();
        for (OmicsData datas : track.getDatas().getDataDisplayed()) {
            typeData.add(datas.getType());
        }
        gc.drawString(" all Data " + typeData, 5, position[1] - 6);
        GElement.setDefaultFont(gc);
    }

    /**
     * Method run when track are displayed by biological conditions
     * 
     * @param gc
     */
    private void displayDataBIOCOND(GC gc) {
        int k = 0;
        for (String bcName : track.getDatas().getBioConditionHashMaps().keySet()) {
            gc.setBackground(BasicColor.WHITE);
            gc.setForeground(BasicColor.BLACK);
            double max = track.getDatas().getBioCondMax(bcName);
            double min = track.getDatas().getBioCondMin(bcName);
            int dataMinimumPosition = getDataPositionMin(bcName, k, min, max);
            int[] position = getDataPosition(bcName, 1, k, 0, min, max, dataMinimumPosition);
            /*
             * If only RNASeq data are in displayed we need to change the legend of the data (log transformed)
             */

            TypeData typeData = TypeData.unknown;
            if (track.getDatas().getTilings(bcName).size() == 0 && track.getDatas().getGeneExprs(bcName).size() == 0
                    && track.getDatas().getSeqDatas(bcName).size() != 0) {
                for (ExpressionData ngs : track.getDatas().getDNASeqDatas(bcName)) {
                    if (ngs.getType() == TypeData.DNASeq)
                        typeData = TypeData.DNASeq;
                }
                for (ExpressionData ngs : track.getDatas().getRiboSeqDatas(bcName)) {
                    if (ngs.getType() == TypeData.RiboSeq)
                        typeData = TypeData.RiboSeq;
                }
                for (ExpressionData ngs : track.getDatas().getTSSDatas(bcName)) {
                    if (ngs.getType() == TypeData.TSS)
                        typeData = TypeData.TSS;
                }
                for (ExpressionData ngs : track.getDatas().getTermSeqDatas(bcName)) {
                    if (ngs.getType() == TypeData.TermSeq)
                        typeData = TypeData.TermSeq;
                }
                for (ExpressionData ngs : track.getDatas().getRNASeqDatas(bcName)) {
                    if (ngs.getType() == TypeData.RNASeq)
                        typeData = TypeData.RNASeq;
                }
            }
            displayDataLegend(gc, bcName, position, min, max, k, dataMinimumPosition, typeData);
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getTilings(bcName).size() != 0)
                displayTiling(gc, track.getDatas().getTilings(bcName), min, max, k, dataMinimumPosition);
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getGeneExprs(bcName).size() != 0)
                displayGeneExpression(gc, track.getDatas().getGeneExprs(bcName), min, max, k, dataMinimumPosition);
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getMatrices(bcName).size() != 0)
                displayExpressionMatrix(gc, track.getDatas().getMatrices(bcName), min, max, k, dataMinimumPosition);
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getRNASeqDatas(bcName).size() != 0) {
                ArrayList<ExpressionData> seqDatasTemp = new ArrayList<ExpressionData>();
                for (ExpressionData rnaSeq : track.getDatas().getRNASeqDatas(bcName)) {
                    if (rnaSeq.getChromosomeID().equals(chromoID)) {
                        seqDatasTemp.add(rnaSeq);
                    }
                }
                displayRNASeq(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
            }
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getDNASeqDatas(bcName).size() != 0) {
                ArrayList<ExpressionData> seqDatasTemp = new ArrayList<ExpressionData>();
                for (ExpressionData rnaSeq : track.getDatas().getDNASeqDatas(bcName)) {
                    if (rnaSeq.getChromosomeID().equals(chromoID)) {
                        seqDatasTemp.add(rnaSeq);
                    }
                }
                displayRNASeq(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
            }
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getRiboSeqDatas(bcName).size() != 0) {
                ArrayList<ExpressionData> seqDatasTemp = new ArrayList<ExpressionData>();
                for (ExpressionData rnaSeq : track.getDatas().getRiboSeqDatas(bcName)) {
                    if (rnaSeq.getChromosomeID().equals(chromoID)) {
                        seqDatasTemp.add(rnaSeq);
                    }
                }
                displayRNASeq(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
            }
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getTSSDatas(bcName).size() != 0) {
                ArrayList<ExpressionData> seqDatasTemp = new ArrayList<ExpressionData>();
                for (ExpressionData rnaSeq : track.getDatas().getTSSDatas(bcName)) {
                    if (rnaSeq.getChromosomeID().equals(chromoID)) {
                        seqDatasTemp.add(rnaSeq);
                    }
                }
                displayTSS(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
            }
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getTermSeqDatas(bcName).size() != 0) {
                ArrayList<ExpressionData> seqDatasTemp = new ArrayList<ExpressionData>();
                for (ExpressionData rnaSeq : track.getDatas().getTermSeqDatas(bcName)) {
                    if (rnaSeq.getChromosomeID().equals(chromoID)) {
                        seqDatasTemp.add(rnaSeq);
                    }
                }
                displayTermSeq(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
            }
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getProteomes(bcName).size() != 0)
                displayProteomicsData(gc, track.getDatas().getProteomes(bcName), min, max, k, dataMinimumPosition);
            GElement.setExpressionAlpha(gc);
            if (track.getDatas().getNTermDatas(bcName).size() != 0)
                displayNTerm(gc, track.getDatas().getNTermDatas(bcName), min, max, k, dataMinimumPosition);
            gc.setBackground(BasicColor.WHITE);
            gc.setForeground(BasicColor.BLACK);
            GElement.setDefaultFont(gc);
            k++;
        }
        for (String bcName : track.getDatas().getBioConditionHashMaps().keySet()) {

            GElement.setDataNameFont(gc);
            double max = track.getDatas().getBioCondMax(bcName);
            double min = track.getDatas().getBioCondMin(bcName);
            int dataMinimumPosition = getDataPositionMin(bcName, k, min, max);
            int[] position = getDataPosition(bcName, 1, k, max, min, max, dataMinimumPosition);
            String typeData = track.getDatas().getBioConditionHashMaps().get(bcName).getTypeDataContained() + "";
            if (bcName.contains("DNA"))
                typeData = "[" + TypeData.DNASeq + "]";
            gc.drawString(" " + bcName + " " + typeData, 3, position[1] + 3);
            GElement.setDefaultFont(gc);
        }
    }

    /**
     * Method run when track are displayed with all datasets separated
     * 
     * @param gc
     */
    private void displayDataDATASEPARATED(GC gc) {
        int k = 0;
        /*
         * Display Tiling data
         */
        TreeSet<String> alreadyDisplayedData = new TreeSet<String>();
        ArrayList<Tiling> tilingsTemp = new ArrayList<Tiling>();
        for (Tiling tiling : track.getDatas().getTilings()) {
            if (!alreadyDisplayedData.contains(tiling.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(tiling.getName())) {
                GElement.setExpressionAlpha(gc);
                double max = tiling.getMax();
                double min = tiling.getMin();
                min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                int dataMinimumPosition = getDataPositionMin(tiling.getName(), k, min, max);
                tilingsTemp.clear();
                tilingsTemp.add(tiling);
                int[] position = getDataPosition(tiling.getName(), 1, k, 0, min, max, dataMinimumPosition);
                displayDataLegend(gc, tiling.getName(), position, min, max, k, dataMinimumPosition, tiling.getType());
                GElement.setExpressionAlpha(gc);
                displayTiling(gc, tilingsTemp, min, max, k, dataMinimumPosition);
                gc.setBackground(BasicColor.WHITE);
                gc.setForeground(BasicColor.BLACK);
                GElement.setDataNameFont(gc);
                gc.drawString(" " + tiling.getName() + " [" + tiling.getType() + "]", 5, position[1] - 6);
                GElement.setDefaultFont(gc);
                k++;
                alreadyDisplayedData.add(tiling.getName());
            }
        }
        /*
         * Display GeneExpression Data
         */
        ArrayList<GeneExpression> geneExprsTemp = new ArrayList<GeneExpression>();
        for (GeneExpression geneExpr : track.getDatas().getGeneExprs()) {
            if (!alreadyDisplayedData.contains(geneExpr.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(geneExpr.getName())) {
                double max = geneExpr.getMax();
                double min = geneExpr.getMin();
                min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                int dataMinimumPosition = getDataPositionMin(geneExpr.getName(), k, min, max);
                geneExprsTemp.clear();
                geneExprsTemp.add(geneExpr);
                int[] position = getDataPosition(geneExpr.getName(), 1, k, 0, min, max, dataMinimumPosition);
                displayDataLegend(gc, geneExpr.getName(), position, min, max, k, dataMinimumPosition,
                        geneExpr.getType());
                GElement.setExpressionAlpha(gc);
                displayGeneExpression(gc, geneExprsTemp, min, max, k, dataMinimumPosition);
                gc.setBackground(BasicColor.WHITE);
                gc.setForeground(BasicColor.BLACK);
                GElement.setDataNameFont(gc);
                gc.drawString(" " + geneExpr.getName() + " [" + geneExpr.getType() + "]", 5, position[1] - 6);
                GElement.setDefaultFont(gc);
                k++;
                alreadyDisplayedData.add(geneExpr.getName());
            }
        }

        /*
         * Display ExpressionMatrix Data
         */
        ArrayList<ExpressionMatrix> matricestemp = new ArrayList<ExpressionMatrix>();
        for (ExpressionMatrix matrix : track.getDatas().getMatrices()) {
            if (!alreadyDisplayedData.contains(matrix.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(matrix.getName())) {
                double max = ExpressionMatrixStat.max(matrix, matrix.getGenomeViewerColumnIndex());
                double min = ExpressionMatrixStat.min(matrix, matrix.getGenomeViewerColumnIndex());
                min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                int dataMinimumPosition = getDataPositionMin(matrix.getName(), k, min, max);
                matricestemp.clear();
                matricestemp.add(matrix);
                int[] position = getDataPosition(matrix.getName(), 1, k, 0, min, max, dataMinimumPosition);
                displayDataLegend(gc, matrix.getName(), position, min, max, k, dataMinimumPosition, matrix.getType());
                GElement.setExpressionAlpha(gc);
                displayExpressionMatrix(gc, matricestemp, min, max, k, dataMinimumPosition);
                gc.setBackground(BasicColor.WHITE);
                gc.setForeground(BasicColor.BLACK);
                GElement.setDataNameFont(gc);
                gc.drawString(" " + matrix.getName() + " [" + matrix.getType() + "]", 5, position[1] - 6);
                GElement.setDefaultFont(gc);
                k++;
                alreadyDisplayedData.add(matrix.getName());
            }
        }

        /*
         * Display RNASeq data
         */
        ArrayList<ExpressionData> seqDatasTemp = new ArrayList<ExpressionData>();
        for (ExpressionData seqData : track.getDatas().getRNASeqDatas()) {
            if (!alreadyDisplayedData.contains(seqData.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                if (seqData.getChromosomeID().equals(chromoID)) {
                    double max = seqData.getMax();
                    double min = seqData.getMin();
                    min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                    max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                    int dataMinimumPosition = getDataPositionMin(seqData.getName(), k, min, max);
                    seqDatasTemp.clear();
                    seqDatasTemp.add(seqData);
                    int[] position = getDataPosition(seqData.getName(), 1, k, 0, min, max, dataMinimumPosition);
                    displayDataLegend(gc, seqData.getName(), position, min, seqData.getMax(), k, dataMinimumPosition,
                            seqData.getType());
                    GElement.setExpressionAlpha(gc);
                    displayRNASeq(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
                    gc.setBackground(BasicColor.WHITE);
                    gc.setForeground(BasicColor.BLACK);
                    GElement.setDataNameFont(gc);
                    gc.drawString(" " + seqData.getName() + " [" + seqData.getType() + "]", 5, position[1] - 6);
                    GElement.setDefaultFont(gc);
                    k++;
                    alreadyDisplayedData.add(seqData.getName());
                }
            }
        }

        /*
         * Display DNASeq data
         */
        seqDatasTemp = new ArrayList<ExpressionData>();
        for (ExpressionData seqData : track.getDatas().getDNASeqDatas()) {
            if (!alreadyDisplayedData.contains(seqData.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                if (seqData.getChromosomeID().equals(chromoID)) {
                    double max = seqData.getMax();
                    double min = seqData.getMin();
                    min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                    max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                    int dataMinimumPosition = getDataPositionMin(seqData.getName(), k, min, max);
                    seqDatasTemp.clear();
                    seqDatasTemp.add(seqData);
                    int[] position = getDataPosition(seqData.getName(), 1, k, 0, min, max, dataMinimumPosition);
                    displayDataLegend(gc, seqData.getName(), position, min, seqData.getMax(), k, dataMinimumPosition,
                            seqData.getType());
                    GElement.setExpressionAlpha(gc);
                    displayRNASeq(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
                    gc.setBackground(BasicColor.WHITE);
                    gc.setForeground(BasicColor.BLACK);
                    GElement.setDataNameFont(gc);
                    gc.drawString(" " + seqData.getName() + " [" + seqData.getType() + "]", 5, position[1] - 6);
                    GElement.setDefaultFont(gc);
                    k++;
                    alreadyDisplayedData.add(seqData.getName());
                }
            }
        }

        /*
         * Display RiboSeq data
         */
        seqDatasTemp = new ArrayList<ExpressionData>();
        for (ExpressionData seqData : track.getDatas().getRiboSeqDatas()) {
            if (!alreadyDisplayedData.contains(seqData.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                if (seqData.getChromosomeID().equals(chromoID)) {
                    double max = seqData.getMax();
                    double min = seqData.getMin();
                    min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                    max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                    int dataMinimumPosition = getDataPositionMin(seqData.getName(), k, min, max);
                    seqDatasTemp.clear();
                    seqDatasTemp.add(seqData);
                    int[] position = getDataPosition(seqData.getName(), 1, k, 0, min, max, dataMinimumPosition);
                    displayDataLegend(gc, seqData.getName(), position, min, seqData.getMax(), k, dataMinimumPosition,
                            seqData.getType());
                    GElement.setExpressionAlpha(gc);
                    displayRNASeq(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
                    gc.setBackground(BasicColor.WHITE);
                    gc.setForeground(BasicColor.BLACK);
                    GElement.setDataNameFont(gc);
                    gc.drawString(" " + seqData.getName() + " [" + seqData.getType() + "]", 5, position[1] - 6);
                    GElement.setDefaultFont(gc);
                    k++;
                    alreadyDisplayedData.add(seqData.getName());
                }
            }
        }

        /*
         * Display TSS data
         */
        for (ExpressionData seqData : track.getDatas().getTSSDatas()) {
            if (!alreadyDisplayedData.contains(seqData.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                if (seqData.getChromosomeID().equals(chromoID)) {
                    double max = seqData.getMax();
                    double min = seqData.getMin();
                    min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                    max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                    int dataMinimumPosition = getDataPositionMin(seqData.getName(), k, min, max);
                    seqDatasTemp.clear();
                    seqDatasTemp.add(seqData);
                    int[] position = getDataPosition(seqData.getName(), 1, k, 0, min, max, dataMinimumPosition);
                    displayDataLegend(gc, seqData.getName(), position, min, seqData.getMax(), k, dataMinimumPosition,
                            seqData.getType());
                    GElement.setExpressionAlpha(gc);
                    displayTSS(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
                    gc.setBackground(BasicColor.WHITE);
                    gc.setForeground(BasicColor.BLACK);
                    GElement.setDataNameFont(gc);
                    gc.drawString(" " + seqData.getName() + " [" + seqData.getType() + "]", 5, position[1] - 6);
                    GElement.setDefaultFont(gc);
                    k++;
                    alreadyDisplayedData.add(seqData.getName());
                }
            }
        }

        /*
         * Display TermSeq data
         */
        for (ExpressionData seqData : track.getDatas().getTermSeqDatas()) {
            if (!alreadyDisplayedData.contains(seqData.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                if (seqData.getChromosomeID().equals(chromoID)) {
                    double max = seqData.getMax();
                    double min = seqData.getMin();
                    min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                    max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                    int dataMinimumPosition = getDataPositionMin(seqData.getName(), k, min, max);
                    seqDatasTemp.clear();
                    seqDatasTemp.add(seqData);
                    int[] position = getDataPosition(seqData.getName(), 1, k, 0, min, max, dataMinimumPosition);
                    displayDataLegend(gc, seqData.getName(), position, min, seqData.getMax(), k, dataMinimumPosition,
                            seqData.getType());
                    GElement.setExpressionAlpha(gc);
                    displayTermSeq(gc, seqDatasTemp, min, max, k, dataMinimumPosition);
                    gc.setBackground(BasicColor.WHITE);
                    gc.setForeground(BasicColor.BLACK);
                    GElement.setDataNameFont(gc);
                    gc.drawString(" " + seqData.getName() + " [" + seqData.getType() + "]", 5, position[1] - 6);
                    GElement.setDefaultFont(gc);
                    k++;
                    alreadyDisplayedData.add(seqData.getName());
                }
            }
        }

        /*
         * Display Proteome Data
         */
        ArrayList<ProteomicsData> proteomeTemp = new ArrayList<ProteomicsData>();
        for (ProteomicsData matrix : track.getDatas().getProteomes()) {
            if (!alreadyDisplayedData.contains(matrix.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(matrix.getName())) {
                double max = ExpressionMatrixStat.max(matrix, matrix.getGenomeViewerColumnIndex());
                double min = ExpressionMatrixStat.min(matrix, matrix.getGenomeViewerColumnIndex());
                min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
                max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
                int dataMinimumPosition = getDataPositionMin(matrix.getName(), k, min, max);
                proteomeTemp.clear();
                proteomeTemp.add(matrix);
                int[] position = getDataPosition(matrix.getName(), 1, k, 0, min, max, dataMinimumPosition);
                displayDataLegend(gc, matrix.getName(), position, min, max, k, dataMinimumPosition, matrix.getType());
                GElement.setExpressionAlpha(gc);
                displayProteomicsData(gc, proteomeTemp, min, max, k, dataMinimumPosition);
                gc.setBackground(BasicColor.WHITE);
                gc.setForeground(BasicColor.BLACK);
                GElement.setDataNameFont(gc);
                gc.drawString(" " + matrix.getName() + " [" + matrix.getType() + "]", 5, position[1] - 6);
                GElement.setDefaultFont(gc);
                k++;
                alreadyDisplayedData.add(matrix.getName());
            }
        }

        /*
         * Display NTerm data
         */
        ArrayList<NTermData> nTermsTemp = new ArrayList<NTermData>();
        for (NTermData seqData : track.getDatas().getNTermDatas()) {
            if (!alreadyDisplayedData.contains(seqData.getName())
                    && !track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                nTermsTemp.clear();
                nTermsTemp.add(seqData);
                int dataMinimumPosition = getDataPositionMin(seqData.getName(), k, -10, 10);
                int[] position = getDataPosition(seqData.getName(), 1, k, 0, -10, 10, dataMinimumPosition);
                displayDataLegend(gc, seqData.getName(), position, -10, 10, k, dataMinimumPosition, seqData.getType());
                GElement.setExpressionAlpha(gc);
                displayNTerm(gc, nTermsTemp, -10, 10, k, dataMinimumPosition);
                gc.setBackground(BasicColor.WHITE);
                gc.setForeground(BasicColor.BLACK);
                GElement.setDataNameFont(gc);
                gc.drawString(" " + seqData.getName() + " [" + seqData.getType() + "]", 5, position[1] - 6);
                GElement.setDefaultFont(gc);
                k++;
                alreadyDisplayedData.add(seqData.getName());
            }
        }
    }

    /**
     * Display all information about a current data in a GC (Graphic context)<br>
     * <li>line in the center representing 0 value
     * <li>min, max and mean value
     * <li>name of the data
     * 
     * @param gc
     * @param name
     * @param min
     * @param max
     * @param dataIndex
     */
    private void displayDataLegend(GC gc, String name, int[] position, double min, double max, int dataIndex,
            int minPosition, TypeData typeData) {
        GElement.setExpressionAlpha(gc);
        gc.drawLine(0, position[1], this.getSize().x, position[1]);
        GElement.setDefaultAlpha(gc);

        int minPos = getDataPosition(name, 1, dataIndex, min, min, max, minPosition)[1];
        int maxPos = getDataPosition(name, 1, dataIndex, max, min, max, minPosition)[1];
        if (!track.getDatas().getDataNOTDisplayed().contains(name)) {
            if (!name.contains("TIS")) {
                // gc.drawLine(1,maxPos , 1, minPos);
                // gc.drawString("0", 6, position[1]-4);
                // gc.drawString(max+"", 6, maxPos+1);
                // gc.drawString(min+"", 6, minPos-10);
                gc.drawLine(1, minPos, 20, minPos);
                gc.drawLine(1, maxPos, 20, maxPos);
                int i = Math.round((float) (min));
                /*
                 * Manage logTransformed or not HERE !
                 * Add : if data is NGS -> if logTransformed then ...
                 */
                if (typeData == TypeData.RNASeq || typeData == TypeData.TSS || typeData == TypeData.TermSeq || typeData == TypeData.RiboSeq) {
                    // System.out.println(name);
                    if (name.contains("DNA") || name.contains("LC-E75") || name.contains("dCas9")) {
                        while (i < max) {
                            int valuePosition = getDataPosition(name, 1, dataIndex, i, min, max, minPosition)[1];
                            gc.drawLine(1, valuePosition, 3, valuePosition);
                            gc.drawString(i + "", 5, valuePosition - 4);
                            i = i + 2;
                        }
                    } else {
                        while (i < max) {
                            int valuePosition = getDataPosition(name, 1, dataIndex, i, min, max, minPosition)[1];
                            gc.drawLine(1, valuePosition, 3, valuePosition);
                            if(i>0) {
                                gc.drawString(Math.rint(Math.pow(2, i)) + "", 5, valuePosition - 4);
                            } else {
                                gc.drawString(Math.rint(Math.pow(2, -i)) + "", 5, valuePosition - 4);
                            }
                            i = i + 2;
                        }
                    }
                } else {
                    while (i < max) {
                        int valuePosition = getDataPosition(name, 1, dataIndex, i, min, max, minPosition)[1];
                        gc.drawLine(1, valuePosition, 3, valuePosition);
                        gc.drawString(i + "", 5, valuePosition - 4);
                        i = i + 2;
                    }
                }
            }
        }
    }

    /**
     * Display sequence = For each nucleotide, give the translation of the codon in which it is in the
     * center
     * 
     * @param gc
     */
    private void displaySequence(GC gc) {

        gc.setLineWidth(1);
        for (int i = track.getDisplayRegion().getX1(); i < track.getDisplayRegion().getX2(); i++) {
            int[] position = {convertBPtoX(i), dataWindowSize + decaySliderVBar};
            gc.drawLine(0, position[1] + 2, this.getSize().x, position[1] + 2);
            position[1] = position[1] + 5;
            GElement.displaySequence(gc, track.getChromosome(), i, position, bpSizeH, track.getZoom(), 0);
            position[1] = position[1] + 13;
            GElement.displaySequence(gc, track.getChromosome(), i, position, bpSizeH, track.getZoom(), 2);
            position[1] = position[1] + 13;
            gc.drawLine(0, position[1], this.getSize().x, position[1]);
            position[1] = position[1] + 2;
            GElement.displaySequence(gc, track.getChromosome(), i, position, bpSizeH, track.getZoom(), 1);
            position[1] = position[1] + 15;
            GElement.displaySequence(gc, track.getChromosome(), i, position, bpSizeH, track.getZoom(), 3);
        }
    }

    /**
     * Function to display a list of <code>Tiling</code> in a GC (Graphic context)
     * 
     * @param gc
     * @param tilings
     * @param min
     * @param max
     * @param dataIndex
     */
    private void displayTiling(GC gc, ArrayList<Tiling> tilings, double min, double max, int dataIndex,
            int minPosition) {
        /*
         * Read data
         */
        for (Tiling tiling : tilings) {
            tiling.get(track.getDisplayRegion().getX1(), track.getDisplayRegion().getX2(), false);
        }

        /*
         * Local variable used to keep in memory previousPostion of a probe
         */
        ArrayList<int[]> previousPositions = new ArrayList<int[]>();

        /*
         * Find begin and last probes of the region displayed
         */
        TreeMap<Integer, Integer> probes = Database.getInstance().getProbesTiling();
        Integer lastProbe = probes.lastKey();
        Integer beginProbe = lastProbe;
        if (track.getDisplayRegion().getX1() < lastProbe)
            beginProbe = probes.ceilingKey(track.getDisplayRegion().getX1());
        Integer endProbe = lastProbe;
        if (track.getDisplayRegion().getX2() < lastProbe)
            endProbe = probes.ceilingKey(track.getDisplayRegion().getX2());

        /*
         * Go from beginProbe to LastProbe and display them
         */
        for (int i : probes.subMap(beginProbe, endProbe).keySet()) {
            int k = 0;
            for (Tiling tiling : tilings) {
                if (!track.getDatas().getDataNOTDisplayed().contains(tiling.getName())) {
                    /*
                     * Find position in the GC: <br> = Conversion from basepair position to pixel
                     */
                    double value = tiling.getValues()[probes.get(i)];
                    String dataName = tiling.getName();
                    if (track.getDisplayType() == DisplayType.BIOCOND)
                        dataName = tiling.getBioCondName();
                    int[] position = getDataPosition(dataName, i, dataIndex, value, min, max, minPosition);

                    /*
                     * A tiling probes is a 25bp probes, but depending on bpSizeH it change its size Size parameter will
                     * be used to decide: - if we draw lines between points - if we display all the data, or just a
                     * percentage of them - what will be the final size of the drawing
                     */
                    double size = bpSizeH * 25;
                    // System.out.println("Size: "+size);

                    /*
                     * Set color of the probes
                     */
                    if (track.getDatas().getDisplay()[i]) {
                        gc.setBackground(track.getDatas().getDataColors().get(tiling.getName()));
                    } else {
                        gc.setBackground(BasicColor.GREY);
                    }

                    /*
                     * Display each probes <br> If size < 0.3, the more we got data the less number of probes we display
                     * That is why, display of probes is dependent of Math.random()<(size*10)
                     */
                    boolean drawProbe = false;
                    if (size > 1) {
                        drawProbe = true;
                    } else {
                        if (Math.random() < (size) * 3) {
                            drawProbe = true;
                        }
                    }

                    /*
                     * if zoom is high we display lines between tiling probes
                     */
                    int[] minPos = getDataPosition(dataName, i, dataIndex, 0, min, max, minPosition);
                    gc.setLineWidth(1);
                    if (i == beginProbe) {
                        previousPositions.add(position);
                    } else if (drawProbe) {
                        int[] polygon = {position[0], minPos[1], previousPositions.get(k)[0], minPos[1],
                                previousPositions.get(k)[0], previousPositions.get(k)[1], position[0], position[1]};
                        gc.fillPolygon(polygon);
                        // gc.drawLine(previousPositions.get(k)[0],
                        // previousPositions.get(k)[1],position[0],position[1]);
                        previousPositions.set(k, position);
                        k++;
                    }

                    if (testData) {
                        String[][] saveDataArray = saveData.get(tiling.getName());
                        for (int index = -7; index < 7; index++) {
                            if (i + index - track.getDisplayRegion().getX1() > 0 && (i + index) < widthBP) {
                                saveDataArray[0][i + index - track.getDisplayRegion().getX1()] = (i + index) + "";
                                saveDataArray[1][i + index - track.getDisplayRegion().getX1()] = value + "";
                            }
                        }
                    }

                }
            }

        }

        // System.out.println("Displayed: "+dataDisplayed+" :
        // "+probes.subMap(beginProbe, endProbe).size());

    }

    /**
     * Function to display a list of GeneExpressionStreaming data in a GC (Graphic context)
     * 
     * @param gc
     * @param seqDatas
     * @param dataIndex
     */
    private void displayRNASeq(GC gc, ArrayList<ExpressionData> seqDatas, double min, double max, int dataIndex,
            int minPosition) {

        // for(ExpressionData seqData : seqDatas){
        // // read the position between this positions
        // if(!seqData.isAlreadyRead()){
        // seqData.read(track.getDisplayRegion().getX1(),
        // track.getDisplayRegion().getX2());
        // }
        // }

        for (ExpressionData seqData : seqDatas) {
            if (!track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                String dataName = seqData.getName();
                if (track.getDisplayType() == DisplayType.BIOCOND)
                    dataName = seqData.getBioCondName();
                int[] minPos = getDataPosition(dataName, track.getDisplayRegion().getX1(), dataIndex, 0, min, max,
                        minPosition);

                ArrayList<Integer> polygonList = new ArrayList<>();
                double[] values = seqData.read(track.getDisplayRegion().getX1(), track.getDisplayRegion().getX2());
                int k = 0;
                for (int i = track.getDisplayRegion().getX1(); i < track.getDisplayRegion().getX2(); i++) {
                    double value = values[k];
                    if (seqData.getName().contains("DNA")) {
                        if (value > 5) {
                            value = 5;
                        }
                    }
                    // System.out.println(value+" "+min+" "+max);
                    k++;
                    if (testData) {
                        String[][] saveDataArray = saveData.get(seqData.getName());
                        saveDataArray[0][i - track.getDisplayRegion().getX1()] = (i) + "";
                        saveDataArray[1][i - track.getDisplayRegion().getX1()] = value + "";

                    }

                    /*
                     * get position in the canvas
                     */
                    int[] position = getDataPosition(dataName, i, dataIndex, value, min, max, minPosition);
                    // set colors
                    if (track.getDatas().getDisplay()[i]) {
                        gc.setForeground(track.getDatas().getDataColors().get(seqData.getName()));
                        gc.setBackground(track.getDatas().getDataColors().get(seqData.getName()));
                    } else
                        gc.setForeground(BasicColor.GREY);

                    /*
                     * Get a random number to decide if we display the point or not If Math.random()<(bpSizeH)*8), we
                     * will have maximum 9000 points displayed
                     * 
                     */
                    boolean drawProbe = false;
                    if (bpSizeH > 1) {
                        drawProbe = true;
                    } else {
                        if (Math.random() < (bpSizeH) * 8) {
                            drawProbe = true;
                        }
                    }

                    // draw a line if we are not at the first position
                    if (i == track.getDisplayRegion().getX1()) {
                        polygonList.add(position[0]);
                        polygonList.add(minPos[1]);
                        polygonList.add(position[0]);
                        polygonList.add(position[1]);

                    } else {
                        if (drawProbe) {
                            polygonList.add(position[0]);
                            polygonList.add(position[1]);
                            // pointDrawn++;
                        }
                    }
                }
                polygonList.add(polygonList.get(polygonList.size() - 2));
                polygonList.add(minPos[1]);
                int[] polygon = new int[polygonList.size()];
                for (int i = 0; i < polygon.length; i++) {
                    polygon[i] = polygonList.get(i);
                }

                gc.fillPolygon(polygon);

                // System.out.println(pointDrawn+" pointDrawn on
                // "+track.getDisplayRegion().getWidth());
            }
        }
    }

    /**
     * Function to display a list of GeneExpressionStreaming data in a GC (Graphic context)
     * 
     * @param gc
     * @param seqDatas
     * @param dataIndex
     */
    private void displayTSS(GC gc, ArrayList<ExpressionData> seqDatas, double min, double max, int dataIndex,
            int minPosition) {

        for (ExpressionData seqData : seqDatas) {
            // read the position between this positions
            // if(!seqData.isAlreadyRead()){
            seqData.read(track.getDisplayRegion().getX1(), track.getDisplayRegion().getX2());
            // }
        }

        for (ExpressionData seqData : seqDatas) {
            if (!track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                String dataName = seqData.getName();
                if (track.getDisplayType() == DisplayType.BIOCOND)
                    dataName = seqData.getBioCondName();
                int[] minPos = getDataPosition(dataName, track.getDisplayRegion().getX1(), dataIndex, 0, min, max,
                        minPosition);

                /*
                 * Draw TSS data
                 */
                double[] values = seqData.read(track.getDisplayRegion().getX1(), track.getDisplayRegion().getX2());
                int k = 0;
                for (int i = track.getDisplayRegion().getX1(); i < track.getDisplayRegion().getX2(); i++) {
                    double value = values[k];
                    // if(value)
                    // System.out.println("TSS: "+value);
                    k++;
                    if (value != 0) {
                        int[] position = getDataPosition(dataName, i, dataIndex, value, min, max, minPosition);
                        if (track.getDatas().getDisplay()[i]) {
                            gc.setForeground(track.getDatas().getDataColors().get(seqData.getName()));
                        } else {
                            gc.setForeground(BasicColor.GREY);
                        }
                        gc.setBackground(BasicColor.WHITE);
                        /*
                         * Depending of the zoom level change the size of the pilar for the TSS
                         */
                        double size = bpSizeH * 25;
                        if (size < 0.1) {
                            gc.setLineWidth(1);
                        } else if (size < 1) {
                            gc.setLineWidth(2);
                        } else {
                            gc.setLineWidth(3);
                        }

                        gc.drawLine(position[0], minPos[1], position[0], position[1]);
                        if (seqData.getName().contains("TSS_f")) {
                            gc.drawRectangle(position[0], position[1], 5, 5);
                            if (size > 0.5)
                                if(value>0) {
                                    gc.drawString(Math.rint(Math.pow(2, value)) + "", position[0] + 13, position[1]);
                                } else {
                                    gc.drawString(Math.rint(Math.pow(2, -value)) + "", position[0] + 13, position[1]);
                                }
                        } else {
                            gc.drawRectangle(position[0] - 5, position[1], 5, 5);
                            if (size > 0.5)
                                if(value>0) {
                                    gc.drawString(Math.rint(Math.pow(2, value)) + "", position[0] + 13, position[1]);
                                } else {
                                    gc.drawString(Math.rint(Math.pow(2, -value)) + "", position[0] + 13, position[1]);
                                }
                        }
                        gc.setLineWidth(1);

                        if (testData) {
                            String[][] saveDataArray = saveData.get(seqData.getName());
                            saveDataArray[0][i - track.getDisplayRegion().getX1()] = i + "";
                            saveDataArray[1][i - track.getDisplayRegion().getX1()] = value + "";
                        }
                    }
                }

            }
        }
    }

    /**
     * Function to display a list of GeneExpressionStreaming data in a GC (Graphic context)
     * 
     * @param gc
     * @param seqDatas
     * @param dataIndex
     */
    private void displayTermSeq(GC gc, ArrayList<ExpressionData> seqDatas, double min, double max, int dataIndex,
            int minPosition) {

        for (ExpressionData seqData : seqDatas) {
            // read the position between this positions
            // if(!seqData.isAlreadyRead()){
            seqData.read(track.getDisplayRegion().getX1(), track.getDisplayRegion().getX2());
            // }
        }

        for (ExpressionData seqData : seqDatas) {
            if (!track.getDatas().getDataNOTDisplayed().contains(seqData.getName())) {
                String dataName = seqData.getName();
                if (track.getDisplayType() == DisplayType.BIOCOND)
                    dataName = seqData.getBioCondName();
                int[] minPos = getDataPosition(dataName, track.getDisplayRegion().getX1(), dataIndex, 0, min, max,
                        minPosition);

                /*
                 * Draw TSS data
                 */
                double[] values = seqData.read(track.getDisplayRegion().getX1(), track.getDisplayRegion().getX2());
                int k = 0;
                for (int i = track.getDisplayRegion().getX1(); i < track.getDisplayRegion().getX2(); i++) {
                    double value = values[k];
                    // if(value)
                    // System.out.println("TSS: "+value);
                    k++;
                    if (value != 0) {
                        int[] position = getDataPosition(dataName, i, dataIndex, value, min, max, minPosition);
                        // System.out.println(i);
                        if (track.getDatas().getDisplay()[i]) {
                            gc.setForeground(track.getDatas().getDataColors().get(seqData.getName()));
                        } else {
                            gc.setForeground(BasicColor.GREY);
                        }
                        gc.setBackground(BasicColor.WHITE);
                        /*
                         * Depending of the zoom level change the size of the pilar for the TSS
                         */
                        double size = bpSizeH * 25;
                        if (size < 0.1) {
                            gc.setLineWidth(1);
                        } else if (size < 1) {
                            gc.setLineWidth(2);
                        } else {
                            gc.setLineWidth(3);
                        }

                        gc.drawLine(position[0], minPos[1], position[0], position[1]);
                        if (seqData.getName().contains("TermSeq_f")) {
                            gc.drawLine(position[0], position[1], position[0] - 5, position[1]);
                            if (size > 0.5)
                                if(value>0) {
                                    gc.drawString(Math.rint(Math.pow(2, value)) + "", position[0] + 13, position[1]);
                                } else {
                                    gc.drawString(Math.rint(Math.pow(2, -value)) + "", position[0] + 13, position[1]);
                                }
                        } else {
                            gc.drawLine(position[0], position[1], position[0] + 5, position[1]);
                            if (size > 0.5)
                                if(value>0) {
                                    gc.drawString(Math.rint(Math.pow(2, value)) + "", position[0] + 13, position[1]);
                                } else {
                                    gc.drawString(Math.rint(Math.pow(2, -value)) + "", position[0] + 13, position[1]);
                                }
                        }
                        gc.setLineWidth(1);

                        if (testData) {
                            String[][] saveDataArray = saveData.get(seqData.getName());
                            saveDataArray[0][i - track.getDisplayRegion().getX1()] = i + "";
                            saveDataArray[1][i - track.getDisplayRegion().getX1()] = value + "";
                        }
                    }
                }

            }
        }
    }

    /**
     * Function to display a list of GeneExpressionStreaming data in a GC (Graphic context)
     * 
     * @param gc
     * @param geneExprs
     * @param min
     * @param max
     * @param dataIndex
     */
    private void displayNTerm(GC gc, ArrayList<NTermData> nTerms, double min, double max, int dataIndex,
            int minPosition) {
        GElement.setAnnotationFont(gc);
        int beginDraw = track.getDisplayRegion().getX1();
        int endDraw = track.getDisplayRegion().getX2();

        for (NTermData dataNTerm : nTerms) {
            if (!track.getDatas().getDataNOTDisplayed().contains(dataNTerm.getName())) {
                String dataName = dataNTerm.getName();
                if (track.getDisplayType() == DisplayType.BIOCOND)
                    dataName = dataNTerm.getBioCondName();
                ArrayList<NTerm> nTermDisplay = dataNTerm.getElementsToDisplay(beginDraw, endDraw, nTermTypeOverlap);
                /*
                 * reorder to get the formyl in First Position And the highlighted nTerm in the last position
                 */
                nTermDisplay = dataNTerm.reorderModif(nTermDisplay, nTermHighlight);
                ArrayList<TIS> tisList = dataNTerm.getTIStoDisplay(nTermDisplay);
                for (TIS tis : tisList) {
                    int begin = tis.getBegin();
                    int end = tis.getEnd();
                    int length = tis.getLength();
                    if (begin < track.getDisplayRegion().getX1()) {
                        begin = track.getDisplayRegion().getX1();
                        if (end > track.getDisplayRegion().getX2()) {
                            length = track.getDisplayRegion().getWidth();
                        } else {
                            length = end - track.getDisplayRegion().getX1() + 1;
                        }
                    } else {
                        if (end > track.getDisplayRegion().getX2()) {
                            length = track.getDisplayRegion().getX2() - begin + 1;
                        }
                    }

                    double value = 8;
                    if (!tis.isStrand())
                        value = -8;
                    int[] position = getDataPosition(dataName, begin, dataIndex, value, min, max, minPosition);
                    int[] positionZero = getDataPosition(dataName, begin, dataIndex, 0, min, max, minPosition);
                    double geneSize = bpSizeH * length;
                    gc.setLineWidth(1);
                    Color darkColor = BasicColor.LIGHTERGREY;
                    Color lightColor = BasicColor.LIGHTGREY;
                    Color lineColor = BasicColor.GREY;
                    Color textColor = BasicColor.BLACK;
                    if (tis.isFormylated()) {
                        darkColor = BasicColor.DARK_OPERON;
                        lightColor = BasicColor.LIGHT_OPERON;
                        lineColor = BasicColor.LINE_OPERON;
                    }
                    Rectangle rectangle =
                            new Rectangle(position[0], position[1], (int) geneSize, positionZero[1] - position[1]);
                    if (!tis.isStrand())
                        rectangle = new Rectangle(position[0], positionZero[1], (int) geneSize,
                                position[1] - positionZero[1]);
                    if (nTermHighlight != null && tis.getnTerms().contains(nTermHighlight)) {
                        lineColor = BasicColor.BLACK;
                        gc.setLineWidth(3); // change the size of the line to highlights elements
                    }
                    GElement.displayGene(gc, tis, rectangle, track, lightColor, darkColor, lineColor, textColor, true);
                    gc.setLineWidth(1);

                    if (testData) {
                        String[][] saveDataArray = saveData.get(dataNTerm.getName());
                        for (int index = 0; index < length; index++) {
                            saveDataArray[0][begin + index - track.getDisplayRegion().getX1()] = begin + index + "";
                            saveDataArray[1][begin + index - track.getDisplayRegion().getX1()] = value + "";
                        }
                    }
                }
            }
        }

    }

    /**
     * Function to display a list of GeneExpressionStreaming data in a GC (Graphic context)
     * 
     * @param gc
     * @param geneExprs
     * @param min
     * @param max
     * @param dataIndex
     */
    private void displayGeneExpression(GC gc, ArrayList<GeneExpression> geneExprs, double min, double max,
            int dataIndex, int minPosition) {
        Chromosome chromo = track.getChromosome();
        Annotation annot = chromo.getAnnotation();
        int beginDraw = track.getDisplayRegion().getX1();
        int endDraw = track.getDisplayRegion().getX2();
        ArrayList<Sequence> sequences = annot.getElements(chromo, beginDraw, endDraw);
        int[] position = new int[2];
        for (Sequence sequence : sequences) {
            String accession = sequence.getName();

            int begin = sequence.getBegin();
            int end = sequence.getEnd();
            int length = sequence.getLength();
            if (begin < track.getDisplayRegion().getX1()) {
                begin = track.getDisplayRegion().getX1();
                if (end > track.getDisplayRegion().getX2()) {
                    length = track.getDisplayRegion().getWidth();
                } else {
                    length = end - track.getDisplayRegion().getX1() + 1;
                }
            } else {
                if (end > track.getDisplayRegion().getX2()) {
                    length = track.getDisplayRegion().getX2() - begin + 1;
                }
            }

            // display element
            for (GeneExpression geneExpr : geneExprs) {
                if (!track.getDatas().getDataNOTDisplayed().contains(geneExpr.getName())) {
                    // System.out.println(accession);
                    String dataName = geneExpr.getName();
                    if (track.getDisplayType() == DisplayType.BIOCOND)
                        dataName = geneExpr.getBioCondName();
                    if (geneExpr.containProbe(accession)) {
                        double value = geneExpr.getMedianValue(accession);
                        position = getDataPosition(dataName, begin, dataIndex, value, min, max, minPosition);
                        int[] positionZero = getDataPosition(dataName, begin, dataIndex, 0, min, max, minPosition);
                        double geneSize = bpSizeH * length;
                        if (track.getDatas().getDisplay()[begin])
                            gc.setBackground(track.getDatas().getDataColors().get(geneExpr.getName()));
                        else
                            gc.setBackground(BasicColor.LIGHTGREY);
                        if (geneSize == 0)
                            geneSize = 1;

                        if (value > 0) {
                            gc.fillRectangle(position[0], position[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                            gc.drawRectangle(position[0], position[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                        } else {
                            gc.fillRectangle(position[0], positionZero[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                            gc.drawRectangle(position[0], positionZero[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                        }
                        // int minPos = getDataPosition(1, dataIndex, min, min, max)[1];
                        // gc.drawLine(position[0],position[1],position[0],minPos);
                        // gc.drawLine(position[0]+(int)geneSize,position[1],position[0]+(int)geneSize,minPos);
                        if (testData) {
                            String[][] saveDataArray = saveData.get(geneExpr.getName());
                            for (int index = 0; index < length; index++) {
                                saveDataArray[0][begin + index - track.getDisplayRegion().getX1()] = begin + index + "";
                                saveDataArray[1][begin + index - track.getDisplayRegion().getX1()] = value + "";
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to display a list of ExpressionMatrix data in a GC (Graphic context)<br>
     * All rowNames should correspond to a locus on the genome, otherwise it will be ignore
     * 
     * @param gc
     * @param geneExprs
     * @param min
     * @param max
     * @param dataIndex
     */
    private void displayExpressionMatrix(GC gc, ArrayList<ExpressionMatrix> matrices, double min, double max,
            int dataIndex, int minPosition) {
        Chromosome chromo = track.getChromosome();
        Annotation annot = chromo.getAnnotation();
        int beginDraw = track.getDisplayRegion().getX1();
        int endDraw = track.getDisplayRegion().getX2();
        ArrayList<Sequence> sequences = annot.getElements(chromo, beginDraw, endDraw);
        int[] position = new int[2];
        for (Sequence sequence : sequences) {
            String accession = sequence.getName();
            String accessionOld = sequence.getFeature("old_locus_tag"); //most of the ArrayExpress data are with old_locs_tag
            int begin = sequence.getBegin();
            int end = sequence.getEnd();
            int length = sequence.getLength();
            if (begin < track.getDisplayRegion().getX1()) {
                begin = track.getDisplayRegion().getX1();
                if (end > track.getDisplayRegion().getX2()) {
                    length = track.getDisplayRegion().getWidth();
                } else {
                    length = end - track.getDisplayRegion().getX1();
                }
            } else {
                if (end > track.getDisplayRegion().getX2()) {
                    length = track.getDisplayRegion().getX2() - begin;
                }
            }

            // display element
            for (ExpressionMatrix matrix : matrices) {
                if (!track.getDatas().getDataNOTDisplayed().contains(matrix.getName())) {
                    // System.out.println(accession);
                    String dataName = matrix.getName();
                    if (track.getDisplayType() == DisplayType.BIOCOND)
                        dataName = matrix.getBioCondName();
                    if (!matrix.getRowNames().containsKey(accession)) {
                    	accession = accessionOld;	
                    }
                    if (matrix.getRowNames().containsKey(accession)) {
                        double value = matrix.getValue(matrix.getRowNames().get(accession),
                                matrix.getGenomeViewerColumnIndex());
                        position = getDataPosition(dataName, begin, dataIndex, value, min, max, minPosition);
                        int[] positionZero = getDataPosition(dataName, begin, dataIndex, 0, min, max, minPosition);
                        double geneSize = bpSizeH * length;
                        if (track.getDatas().getDisplay()[begin])
                            gc.setBackground(track.getDatas().getDataColors().get(matrix.getName()));
                        else
                            gc.setBackground(BasicColor.LIGHTGREY);
                        if (geneSize == 0)
                            geneSize = 1;

                        if (value > 0) {
                            gc.fillRectangle(position[0], position[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                            gc.drawRectangle(position[0], position[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                        } else {
                            gc.fillRectangle(position[0], positionZero[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                            gc.drawRectangle(position[0], positionZero[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                        }
                        // int minPos = getDataPosition(1, dataIndex, min, min, max)[1];
                        // gc.drawLine(position[0],position[1],position[0],minPos);
                        // gc.drawLine(position[0]+(int)geneSize,position[1],position[0]+(int)geneSize,minPos);

                        if (testData) {
                            String[][] saveDataArray = saveData.get(matrix.getName());
                            for (int index = 0; index < length; index++) {
                                saveDataArray[0][begin + index - track.getDisplayRegion().getX1()] = begin + index + "";
                                saveDataArray[1][begin + index - track.getDisplayRegion().getX1()] = value + "";
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Function to display a list of ExpressionMatrix data in a GC (Graphic context)<br>
     * All rowNames should correspond to a locus on the genome, otherwise it will be ignore
     * 
     * @param gc
     * @param geneExprs
     * @param min
     * @param max
     * @param dataIndex
     */
    private void displayProteomicsData(GC gc, ArrayList<ProteomicsData> matrices, double min, double max, int dataIndex,
            int minPosition) {
        Chromosome chromo = track.getChromosome();
        Annotation annot = chromo.getAnnotation();
        int beginDraw = track.getDisplayRegion().getX1();
        int endDraw = track.getDisplayRegion().getX2();
        ArrayList<Sequence> sequences = annot.getElements(chromo, beginDraw, endDraw);
        int[] position = new int[2];
        for (Sequence sequence : sequences) {
            String accession = sequence.getName();

            int begin = sequence.getBegin();
            int end = sequence.getEnd();
            int length = sequence.getLength();
            if (begin < track.getDisplayRegion().getX1()) {
                begin = track.getDisplayRegion().getX1();
                if (end > track.getDisplayRegion().getX2()) {
                    length = track.getDisplayRegion().getWidth();
                } else {
                    length = end - track.getDisplayRegion().getX1();
                }
            } else {
                if (end > track.getDisplayRegion().getX2()) {
                    length = track.getDisplayRegion().getX2() - begin;
                }
            }

            // display element
            for (ExpressionMatrix matrix : matrices) {
                if (!track.getDatas().getDataNOTDisplayed().contains(matrix.getName())) {
                    // System.out.println(accession);
                    String dataName = matrix.getName();
                    if (track.getDisplayType() == DisplayType.BIOCOND)
                        dataName = matrix.getBioCondName();
                    if (matrix.getRowNames().containsKey(accession)) {
                        double value = matrix.getValue(matrix.getRowNames().get(accession),
                                matrix.getGenomeViewerColumnIndex());
                        position = getDataPosition(dataName, begin, dataIndex, value, min, max, minPosition);
                        int[] positionZero = getDataPosition(dataName, begin, dataIndex, 0, min, max, minPosition);
                        double geneSize = bpSizeH * length;
                        if (track.getDatas().getDisplay()[begin])
                            gc.setBackground(track.getDatas().getDataColors().get(matrix.getName()));
                        else
                            gc.setBackground(BasicColor.LIGHTGREY);
                        if (geneSize == 0)
                            geneSize = 1;

                        if (value > 0) {
                            gc.fillRectangle(position[0], position[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                            gc.drawRectangle(position[0], position[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                        } else {
                            gc.fillRectangle(position[0], positionZero[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                            gc.drawRectangle(position[0], positionZero[1], (int) geneSize,
                                    Math.abs(position[1] - positionZero[1]));
                        }
                        // int minPos = getDataPosition(1, dataIndex, min, min, max)[1];
                        // gc.drawLine(position[0],position[1],position[0],minPos);
                        // gc.drawLine(position[0]+(int)geneSize,position[1],position[0]+(int)geneSize,minPos);

                    }
                }
            }
        }
    }

    /**
     * Display all decorative elements which will be draw at the end, so on every other elements
     * 
     * @param gc
     */
    private void displayLegendAfter(GC gc) {
        /*
         * display line of information
         */
        if (isDisplayMouseLine()) {
            gc.setLineWidth(2);
            gc.setForeground(BasicColor.BLACK);
            gc.setBackground(BasicColor.WHITE);
            gc.drawLine(getMouseXPosition(), 0, getMouseXPosition(), heightPix);
            gc.drawLine(0, getMouseYPosition(), this.getSize().x, getMouseYPosition());
            gc.drawString(this.convertXtoBP(getMouseXPosition()) + " bp", getMouseXPosition() + 5,
                    getMouseYPosition() - 15);
            gc.setLineWidth(1);
        }
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
     * (inverse function of basepairToXimage(int bpIndex))<br>
     * If MassSpec data has not been register do it
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
            System.out.println(e.getSource() + " " + e.x + " y " + e.y);
            @SuppressWarnings("unused")
            String info = "";
            int basePair = convertXtoBP(e.x);

            String name = massSpecData.getAnnotation().getElementATbp(track.getChromosome(), basePair);
            if (name != null) {

                info = name + "  " + massSpecData.getElements().get(name).getModifSequence();
            }

        }

    }

    /**
     * From the position of the vertical bar draw the corresponding part of the data
     * 
     * @param sliderVBarPosition
     */
    public void moveVertically(int sliderVBarIncrement) {
        if (!(this.getTrack().getDisplayType() == DisplayType.OVERLAY)) {
            // System.out.println("slidervertical: "+sliderVBarIncrement);
            decaySliderVBar = -sliderVBarIncrement;
        }
    }

    /**
     * Get base-pair position of the clicked element
     * 
     * @param e
     * @return
     */
    public int clickedElement(MouseEvent e) {
        int basePair = convertXtoBP(e.x);
        return basePair;
    }

    /*
     * ***************************************************************************** **************
     * 
     * GETTER AND SETTER
     * 
     * ***************************************************************************** **************
     */

    public String getnTermTypeOverlap() {
        return nTermTypeOverlap;
    }

    public void setnTermTypeOverlap(String nTermTypeOverlap) {
        this.nTermTypeOverlap = nTermTypeOverlap;
    }

    public NTerm getnTermHighlight() {
        return nTermHighlight;
    }

    public void setnTermHighlight(NTerm nTermHighlight) {
        this.nTermHighlight = nTermHighlight;
    }

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

    public NTermData getMassSpecData() {
        return massSpecData;
    }

    public void setMassSpecData(NTermData massSpecData) {
        this.massSpecData = massSpecData;
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

    public int getDecaySliderVBar() {
        return decaySliderVBar;
    }

    public void setDecaySliderVBar(int decaySliderVBar) {
        this.decaySliderVBar = decaySliderVBar;
    }

    public int getDataWindowSize() {
        return dataWindowSize;
    }

    public void setDataWindowSize(int dataWindowSize) {
        this.dataWindowSize = dataWindowSize;
    }

    public double getBpSizeH() {
        return bpSizeH;
    }

    public void setBpSizeH(double bpSizeH) {
        this.bpSizeH = bpSizeH;
    }

    public Composite getTrackComposite() {
        return trackComposite;
    }

    public void setTrackComposite(Composite trackComposite) {
        this.trackComposite = trackComposite;
    }

    public double getZoomVertical() {
        return zoomVertical;
    }

    public void setZoomVertical(double zoomVertical) {
        this.zoomVertical = zoomVertical;
    }

    public boolean isTestData() {
        return testData;
    }

    public void setTestData(boolean testData) {
        this.testData = testData;
    }

    public String getCanvasName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genomeName) {
        this.genomeName = genomeName;
    }

    public String getChromoID() {
        return chromoID;
    }

    public void setChromoID(String chromoID) {
        this.chromoID = chromoID;
    }

}
