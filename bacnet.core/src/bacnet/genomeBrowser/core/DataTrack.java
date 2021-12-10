package bacnet.genomeBrowser.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.eclipse.swt.graphics.Color;
import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.ProteomicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.genomeBrowser.core.Track.DisplayType;
import bacnet.utils.BasicColor;
import bacnet.utils.ExpressionMatrixStat;

public class DataTrack {

    /**
     * Parent Track
     */
    private Track track;

    /**
     * True if we display Absolute value expression (Log(Value)) False if we display Relative value
     * expression (Log(FC))
     */
    private boolean displayAbsoluteValue = true;

    /**
     * Indicate if at a specific position of the genome : data are displayed, or just leave in grey
     * color
     */
    private boolean[] display = new boolean[0];

    /**
     * HashMap used to quickly access a specific BioCondition
     */
    private LinkedHashMap<String, BioCondition> bioConditionHashMaps = new LinkedHashMap<String, BioCondition>();

    /**
     * List of data that should be displayed
     */
    private ArrayList<String> dataNOTDisplayed = new ArrayList<String>();

    /**
     * Indicate the color to display for each data
     */
    private TreeMap<String, Color> dataColors = new TreeMap<String, Color>();
    /**
     * Indicate the size factor of each data
     */
    private final TreeMap<String, Float> dataSizes = new TreeMap<String, Float>();

    public DataTrack(Track track) {
        this.track = track;
    }

    /**
     * Count the number of data available, without duplicate and filtered
     * 
     * @param filteredData
     * @return
     */
    public int getDataCount() {
        int count = 0;
        for (BioCondition bioCond : getBioConditionHashMaps().values()) {
            for (OmicsData tscData : bioCond.getOmicsData()) {
                if (!getDataNOTDisplayed().contains(tscData.getName())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Return the names of the data which will be displayd
     * 
     * @return
     */
    public ArrayList<String> getDataDisplayedNames() {
        TreeSet<String> displayedData = new TreeSet<String>();
        for (BioCondition bioCond : getBioConditionHashMaps().values()) {
            for (OmicsData tscData : bioCond.getOmicsData()) {
                if (!getDataNOTDisplayed().contains(tscData.getName())) {
                    displayedData.add(tscData.getName());
                }
            }
        }
        ArrayList<String> alreadyDisplayedData = new ArrayList<>();
        for (String data : displayedData)
            alreadyDisplayedData.add(data);
        return alreadyDisplayedData;
    }

    /**
     * Get the list of data to display
     * 
     * @return
     */
    public ArrayList<OmicsData> getDataDisplayed() {
        ArrayList<OmicsData> omicsDatasets = new ArrayList<>();
        for (BioCondition bioCond : getBioConditionHashMaps().values()) {
            for (OmicsData tscData : bioCond.getOmicsData()) {
                if (!getDataNOTDisplayed().contains(tscData.getName())) {
                    omicsDatasets.add(tscData);
                }
            }
        }
        return omicsDatasets;
    }

    /**
     * Return the list of all the data available
     * 
     * @param filteredData
     * @return
     */
    public ArrayList<String> getDataNames() {
        ArrayList<String> alldata = new ArrayList<>();
        for (BioCondition bioCond : getBioConditionHashMaps().values()) {
            for (OmicsData tscData : bioCond.getOmicsData()) {
                alldata.add(tscData.getName());
            }
        }
        return alldata;
    }

    /**
     * For each data assign a Color <br>
     * <br>
     * OVERLAYBC : Color are always the same, blue and red for tiling, black for GeneExpr, other color
     * for the rest <br>
     * SUPERIMPOSED, OVERLAYDATA : Color.getColors(index) is used
     * 
     * @param type
     */
    public void setDataColors() {
        if (track.getDisplayType() == DisplayType.BIOCOND) { // OverlayBC, the color will be the same for all BC
            for (BioCondition bioCond : getBioConditionHashMaps().values()) {
                ArrayList<OmicsData> tscDatas = bioCond.getOmicsData();
                for (OmicsData tscData : tscDatas) {
                    if (tscData instanceof Tiling) {
                        if (tscData.getName().contains("+"))
                            dataColors.put(tscData.getName(), BasicColor.RED);
                        else
                            dataColors.put(tscData.getName(), BasicColor.BLUE);
                    } else if (tscData instanceof GeneExpression) {
                        dataColors.put(tscData.getName(), BasicColor.GREY);
                    } else if (tscData.getType() == TypeData.RNASeq || tscData.getType() == TypeData.DNASeq
                            || tscData.getType() == TypeData.TSS || tscData.getType() == TypeData.TermSeq
                            || tscData.getType() == TypeData.RiboSeq) {
                        if (tscData.getName().contains("+") || tscData.getName().contains("_f")) {
                            dataColors.put(tscData.getName(), BasicColor.RED);
                        } else if (tscData.getName().contains("-") || tscData.getName().contains("_r")) {
                            dataColors.put(tscData.getName(), BasicColor.BLUE);
                        } else
                            dataColors.put(tscData.getName(), BasicColor.RNASEQ_NOSTRAND);
                    } else if (tscData instanceof NTermData) {
                        dataColors.put(tscData.getName(), BasicColor.GREY);
                    } else if (tscData instanceof ExpressionMatrix) {
                        dataColors.put(tscData.getName(), BasicColor.LIGHTGREY);
                    } else if (tscData instanceof ProteomicsData) {
                        dataColors.put(tscData.getName(), BasicColor.LIGHTERGREY);
                    }
                }
            }
        } else { // one color for each data
            int k = 0;
            TreeSet<String> alreadyDisplayedData = new TreeSet<String>();
            for (BioCondition bioCond : getBioConditionHashMaps().values()) {
                ArrayList<OmicsData> tscDatas = bioCond.getOmicsData();
                for (OmicsData tscData : tscDatas) {
                    if (!alreadyDisplayedData.contains(tscData.getName())) {
                        alreadyDisplayedData.add(tscData.getName());
                        Color color = BasicColor.getColors(k);
                        if (tscData.getType() == TypeData.RNASeq) {
                            if (tscData.getName().contains("+")) {
                                dataColors.put(tscData.getName(), BasicColor.RED);
                            } else if (tscData.getName().contains("-")) {
                                dataColors.put(tscData.getName(), BasicColor.BLUE);
                            } else
                                dataColors.put(tscData.getName(), BasicColor.getColors(k));
                        } else if (tscData instanceof NTermData) {
                            dataColors.put(tscData.getName(), BasicColor.CYAN);
                        } else
                            dataColors.put(tscData.getName(), color);
                        k++;
                    }
                }
            }
        }
    }

    /**
     * For each data assign a Size <br>
     * <br>
     * <li>1.5 for Tiling array, GeneExpression array and RNASeq
     * <li>1 for TSS
     * <li>0.5 for NTerm and Proteomics data
     * 
     * @param type
     */
    public void setDataSizes() {
        for (BioCondition bioCond : getBioConditionHashMaps().values()) {
            ArrayList<OmicsData> tscDatas = bioCond.getOmicsData();
            for (OmicsData tscData : tscDatas) {
                if (tscData.getType() == TypeData.GeneExpr || tscData.getType() == TypeData.Tiling
                        || tscData.getType() == TypeData.ExpressionMatrix) {
                    dataSizes.put(tscData.getName(), 1.5f);
                } else if (tscData.getType() == TypeData.TSS || tscData.getType() == TypeData.RNASeq
                        || tscData.getType() == TypeData.TermSeq || tscData.getType() == TypeData.RiboSeq
                        || tscData.getType() == TypeData.Proteome || tscData.getType() == TypeData.NTerm) {
                    dataSizes.put(tscData.getName(), 1f);
                }
            }
        }
    }

    /**
     * Add all the size of the Tracks to know the total size of the display window
     * 
     * @param type
     */
    public float getDataTotalSize(DisplayType type) {
        float totalSize = 0;
        if (type == DisplayType.BIOCOND) {
            for (String bioCondName : bioConditionHashMaps.keySet()) {
                totalSize += getBioCondSize(bioCondName);
            }
        } else if (type == DisplayType.DATA) {
            for (BioCondition bioCond : getBioConditionHashMaps().values()) {
                ArrayList<OmicsData> tscDatas = bioCond.getOmicsData();
                for (OmicsData tscData : tscDatas) {
                    if (!getDataNOTDisplayed().contains(tscData.getName())) {
                        totalSize += dataSizes.get(tscData.getName());
                    }
                }
            }
        } else {
            totalSize = 1;
        }
        return totalSize;
    }

    /**
     * Return the size ratio that have a given BioCondition
     * 
     * @param bioCondName
     * @return
     */
    public float getBioCondSize(String bioCondName) {
        float maxSize = 0;
        ArrayList<OmicsData> tscDatas = bioConditionHashMaps.get(bioCondName).getOmicsData();
        for (OmicsData tscData : tscDatas) {
            if (!getDataNOTDisplayed().contains(tscData.getName())) {
                if (maxSize < dataSizes.get(tscData.getName())) {
                    maxSize = dataSizes.get(tscData.getName());
                }
            }
        }
        return maxSize;
    }

    /**
     * Initiate Display boolean Vector when the first is added
     */
    public void initDisplayBoolean() {
        int genomeSize = 0;
        try {
            genomeSize = track.getChromosome().getLength();
            display = new boolean[genomeSize];
            for (int i = 0; i < display.length; i++)
                display[i] = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Read every ExpressionData contained in bioCondName
     * 
     * @param bioCondName
     * @param genomeSize
     */
    public void addBioCondition(String bioCondName) {
        System.out.println("Add: " + bioCondName);
        if (this.getDisplay().length == 0) {
            initDisplayBoolean();
        }
        BioCondition bioCond = BioCondition.getBioCondition(bioCondName, true);
        System.out.println("biocond: "+bioCond);

        if (bioCond.getOmicsData().size() != 0 || bioCond.getComparisons().size() != 0) {
            System.out.println("addBioCondition if 1");
            System.out.println("bioCond.getOmicsData(): " + bioCond.getOmicsData());
            System.out.println("bioCond.getComparisons(): "+bioCond.getComparisons());

            /*
             * If the biological condition we add has no Absolute value expression data, we force the view to
             * switch to relative expression mode
             */
            if (bioCond.getOmicsData().size() == 0 && bioCond.getComparisons().size() != 0) {
                System.out.println("addBioCondition if 2");
                System.out.println("Add: " + bioCondName + " with no omics data");
                if (isDisplayAbsoluteValue()) {
                    setDisplayAbsoluteValue(false);
                    absoluteTOrelativeValue();
                }
            }

            if (displayAbsoluteValue) {
                System.out.println("addBioCondition if 3");

                bioConditionHashMaps.put(bioCond.getName(), bioCond);
            } else {
                System.out.println("addBioCondition if 4");

                /*
                 * Create and add Comparison BioCondition if available
                 */
                if (bioCond.getComparisons().size() != 0 ) {
                    System.out.println("addBioCondition if 5");

                    for (String bioCond2Name : bioCond.getComparisons()) {
                        BioCondition comparisonBioCond =
                                bioCond.compare(BioCondition.getBioCondition(bioCond2Name), false);
                        bioConditionHashMaps.put(comparisonBioCond.getName(), comparisonBioCond);
                    }
                } else {                System.out.println("addBioCondition if 6");

                    bioConditionHashMaps.put(bioCond.getName(), bioCond);
                }
            }
            orderBioCondition();
            loadData();
        }
    }

    /**
     * Changed all BioCondition to display when switching from "ABSOLUTE VALUE DISPLAY3 to "RELATIVE
     * VALUE DISPLAY"
     */
    public void absoluteTOrelativeValue() {
        LinkedHashMap<String, BioCondition> newBioConditionHashMaps = new LinkedHashMap<String, BioCondition>();
        for (BioCondition bioCond : bioConditionHashMaps.values()) {
            if (bioCond.getComparisons().size() != 0) {
                for (String bioCond2Name : bioCond.getComparisons()) {
                    BioCondition comparisonBioCond = bioCond.compare(BioCondition.getBioCondition(bioCond2Name), false);
                    newBioConditionHashMaps.put(comparisonBioCond.getName(), comparisonBioCond);
                }
            } else if (bioCond.getAntiComparisons().size() != 0) {
                for (String bioCond2Name : bioCond.getAntiComparisons()) {
                    BioCondition comparisonBioCond = BioCondition.getBioCondition(bioCond2Name).compare(bioCond, false);
                    newBioConditionHashMaps.put(comparisonBioCond.getName(), comparisonBioCond);
                }
            }
            // else{
            // newBioConditionHashMaps.put(bioCond.getName(), bioCond);
            // }
        }
        this.setBioConditionHashMaps(newBioConditionHashMaps);
        orderBioCondition();
        loadData();
        setDataColors();
        setDataSizes();
    }

    /**
     * Changed all BioCondition to display when switching from "RELATIVE VALUE DISPLAY3 to "ABSOLUTE
     * VALUE DISPLAY"
     */
    public void relativeTOabsoluteValue() {
        LinkedHashMap<String, BioCondition> newBioConditionHashMaps = new LinkedHashMap<String, BioCondition>();
        for (BioCondition comparisonBioCond : bioConditionHashMaps.values()) {
            if (comparisonBioCond.getName().contains(" vs ")) {
                BioCondition bioCond1 =
                        BioCondition.getBioCondition(BioCondition.parseName(comparisonBioCond.getName())[0]);
                BioCondition bioCond2 =
                        BioCondition.getBioCondition(BioCondition.parseName(comparisonBioCond.getName())[1]);
                boolean added = false;
                if (bioCond1.getOmicsData().size() != 0) {
                    newBioConditionHashMaps.put(bioCond1.getName(), bioCond1);
                    added = true;
                }
                if (bioCond2.getOmicsData().size() != 0) {
                    newBioConditionHashMaps.put(bioCond2.getName(), bioCond2);
                    added = true;
                }
                /*
                 * If nothing has been added
                 */
                if (!added) {
                    newBioConditionHashMaps.put(comparisonBioCond.getName(), comparisonBioCond);
                }
            } else {
                newBioConditionHashMaps.put(comparisonBioCond.getName(), comparisonBioCond);
            }
        }
        this.setBioConditionHashMaps(newBioConditionHashMaps);
        orderBioCondition();
        loadData();
        setDataColors();
        setDataSizes();
    }

    /**
     * Load all data included in the Biological Condition (bioConditionHashMaps)
     */
    private void loadData() {
        for (BioCondition bioCond : bioConditionHashMaps.values()) {
        	for (Tiling tiling : bioCond.getTilings()) {
                if (!tiling.isInfoRead()) {
                    tiling.load();
                }
            }
            for (GeneExpression geneExpr : bioCond.getGeneExprs()) {
                if (!geneExpr.isInfoRead()) {
                    geneExpr.load();
                }
            }
            for (NGS ngsExpr : bioCond.getNGSSeqs()) {
                if (!ngsExpr.isInfoRead()) {
                    ngsExpr.load();
                }
            }
            for (NTermData nTermData : bioCond.getnTerms()) {
                nTermData.load();
            }
            for (ProteomicsData proteome : bioCond.getProteomes()) {
                if (!proteome.isLoaded()) {
                    proteome.load();
                }
            }
            //System.out.println("bioCond.getMatrices: "+bioCond.getMatrices());
            for (ExpressionMatrix matrix : bioCond.getMatrices()) {

                if (!matrix.isLoaded()) {
                    matrix.load();
                    //System.out.println("matrix row names: " + matrix.getRowNamesToList());
                    //System.out.println("matrix loaded ");
                }
            }
        }
    }

    /**
     * Add BioCondition in BioConditionHashMaps respecting a certain order: 1 - RNASeq 2 - Tiling 3 -
     * Gene Expression 4 - ExpressionMatrix 5 - TSS 6 - Proteome 7 - Nterm 8 - Unknown
     * 
     * @param bioCond
     */
    private void orderBioCondition() {
        LinkedHashMap<String, BioCondition> bioConditionHashMaps = this.getBioConditionHashMaps();
        /*
         * Then we reorder the list
         */
        LinkedHashMap<String, BioCondition> newBioConditionHashMaps = new LinkedHashMap<String, BioCondition>();
        // 1 - RNASeq
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.RNASeq)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        // 2 - Tiling
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.Tiling)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        // 3 - Gene Expression
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.GeneExpr)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        // 4 - ExpressionMatrix
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.ExpressionMatrix)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        // 5 - TSS
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.TSS)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        // 5 - TermSeq
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.TermSeq)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        // RiboSeq
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.RiboSeq)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        // 6 - Proteome
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.Proteome)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        // 7 - Nterm
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.NTerm)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }

        // 8 - Unknown
        for (String bioCondNameTemp : bioConditionHashMaps.keySet()) {
            BioCondition bioConditionTemp = bioConditionHashMaps.get(bioCondNameTemp);
            if (bioConditionTemp.getTypeDataContained().contains(TypeData.unknown)) {
                if (!newBioConditionHashMaps.containsKey(bioCondNameTemp)) {
                    newBioConditionHashMaps.put(bioCondNameTemp, bioConditionTemp);
                }
            }
        }
        this.setBioConditionHashMaps(newBioConditionHashMaps);
    }

    /**
     * Return the list of Tiling given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<Tiling> getTilings(String bioCondName) {
        ArrayList<Tiling> tilings = new ArrayList<Tiling>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData instanceof Tiling)
                tilings.add((Tiling) tscData);
        }
        return tilings;
    }

    /**
     * Return the list of GeneExpression given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<GeneExpression> getGeneExprs(String bioCondName) {
        ArrayList<GeneExpression> geneExprs = new ArrayList<GeneExpression>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData instanceof GeneExpression)
                geneExprs.add((GeneExpression) tscData);
        }
        return geneExprs;
    }

    /**
     * Return the list of RNASeqData given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getSeqDatas(String bioCondName) {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (ExpressionData exprData : getRNASeqDatas(bioCondName)) {
            seqDatas.add(exprData);
        }
        for (ExpressionData exprData : getDNASeqDatas(bioCondName)) {
            seqDatas.add(exprData);
        }
        for (ExpressionData exprData : getRiboSeqDatas(bioCondName)) {
            seqDatas.add(exprData);
        }
        for (ExpressionData exprData : getTSSDatas(bioCondName)) {
            seqDatas.add(exprData);
        }
        for (ExpressionData exprData : getTermSeqDatas(bioCondName)) {
            seqDatas.add(exprData);
        }
        return seqDatas;
    }

    /**
     * Return the list of RNASeqData given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getRNASeqDatas(String bioCondName) {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData.getType() == TypeData.RNASeq)
                seqDatas.add((ExpressionData) tscData);
        }
        return seqDatas;
    }

    /**
     * Return the list of RiboSeqData given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getDNASeqDatas(String bioCondName) {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData.getType() == TypeData.DNASeq)
                seqDatas.add((ExpressionData) tscData);
        }
        return seqDatas;
    }

    /**
     * Return the list of RiboSeqData given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getRiboSeqDatas(String bioCondName) {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData.getType() == TypeData.RiboSeq)
                seqDatas.add((ExpressionData) tscData);
        }
        return seqDatas;
    }

    /**
     * Return the list of TSS given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getTSSDatas(String bioCondName) {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData.getType() == TypeData.TSS)
                seqDatas.add((ExpressionData) tscData);
        }
        return seqDatas;
    }

    /**
     * Return the list of Term given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getTermSeqDatas(String bioCondName) {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData.getType() == TypeData.TermSeq)
                seqDatas.add((ExpressionData) tscData);
        }
        return seqDatas;
    }

    /**
     * Return the list of SeqData given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionMatrix> getMatrices(String bioCondName) {
        ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData instanceof ExpressionMatrix)
                matrices.add((ExpressionMatrix) tscData);
        }
        return matrices;
    }

    /**
     * Return the list of NTermData given a bioCondName<br>
     * 
     * @param bioCondName
     * @return null if no BiCond was found
     */
    public ArrayList<NTermData> getNTermDatas(String bioCondName) {
        ArrayList<NTermData> seqDatas = new ArrayList<NTermData>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData instanceof NTermData)
                seqDatas.add((NTermData) tscData);
        }
        return seqDatas;
    }

    /**
     * Return the list of MassSpecData given a bioCondName<br>
     * 
     * @param bioCondName
     * @return null if no BiCond was found
     */
    public ArrayList<ProteomicsData> getProteomes(String bioCondName) {
        ArrayList<ProteomicsData> seqDatas = new ArrayList<ProteomicsData>();
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData instanceof ProteomicsData)
                seqDatas.add((ProteomicsData) tscData);
        }
        return seqDatas;
    }

    /**
     * Return the list of Tilings
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<Tiling> getTilings() {
        ArrayList<Tiling> tilings = new ArrayList<Tiling>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData instanceof Tiling)
                    tilings.add((Tiling) tscData);
            }
        }
        return tilings;
    }

    /**
     * Return the list of GeneExpressions
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<GeneExpression> getGeneExprs() {
        ArrayList<GeneExpression> geneExprs = new ArrayList<GeneExpression>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData instanceof GeneExpression)
                    geneExprs.add((GeneExpression) tscData);
            }
        }
        return geneExprs;
    }

    /**
     * Return the list of SeqDatas
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getRNASeqDatas() {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData.getType() == TypeData.RNASeq)
                    seqDatas.add((ExpressionData) tscData);
            }
        }
        return seqDatas;
    }

    /**
     * Return the list of SeqDatas
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getDNASeqDatas() {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData.getType() == TypeData.DNASeq)
                    seqDatas.add((ExpressionData) tscData);
            }
        }
        return seqDatas;
    }

    /**
     * Return the list of SeqDatas
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getRiboSeqDatas() {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData.getType() == TypeData.RiboSeq)
                    seqDatas.add((ExpressionData) tscData);
            }
        }
        return seqDatas;
    }

    /**
     * Return the list of SeqDatas
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getTSSDatas() {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData.getType() == TypeData.TSS)
                    seqDatas.add((ExpressionData) tscData);
            }
        }
        return seqDatas;
    }

    /**
     * Return the list of SeqDatas
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionData> getTermSeqDatas() {
        ArrayList<ExpressionData> seqDatas = new ArrayList<ExpressionData>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData.getType() == TypeData.TermSeq)
                    seqDatas.add((ExpressionData) tscData);
            }
        }
        return seqDatas;
    }

    /**
     * Return the list of SeqData given a bioCondName
     * 
     * @param bioCondName
     * @return
     */
    public ArrayList<ExpressionMatrix> getMatrices() {
        ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData instanceof ExpressionMatrix)
                    matrices.add((ExpressionMatrix) tscData);
            }
        }
        return matrices;
    }

    /**
     * Return the list of NTermData given a bioCondName<br>
     * 
     * @param bioCondName
     * @return null if no BiCond was found
     */
    public ArrayList<NTermData> getNTermDatas() {
        ArrayList<NTermData> seqDatas = new ArrayList<NTermData>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData instanceof NTermData)
                    seqDatas.add((NTermData) tscData);
            }
        }
        return seqDatas;
    }

    /**
     * Return the list of MassSpecData given a bioCondName<br>
     * 
     * @param bioCondName
     * @return null if no BiCond was found
     */
    public ArrayList<ProteomicsData> getProteomes() {
        ArrayList<ProteomicsData> seqDatas = new ArrayList<ProteomicsData>();
        for (String bioCondName : getBioConditionHashMaps().keySet()) {
            for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
                if (tscData instanceof ProteomicsData)
                    seqDatas.add((ProteomicsData) tscData);
            }
        }
        return seqDatas;
    }

    public double getMax() {
        double max = -1000000;
        for (BioCondition bioCond : getBioConditionHashMaps().values()) {
            for (OmicsData tscData : bioCond.getOmicsData()) {
                if (tscData instanceof ExpressionData) {
                    ExpressionData data = (ExpressionData) tscData;
                    double maxData = data.getMax();
                    if (max < maxData)
                        max = maxData;

                }
                if (tscData instanceof ExpressionMatrix || tscData instanceof ProteomicsData) {
                    ExpressionMatrix data = (ExpressionMatrix) tscData;
                    double maxData = ExpressionMatrixStat.max(data, data.getGenomeViewerColumnIndex());
                    if (max < maxData)
                        max = maxData;
                } else {
                    double maxData = 9;
                    if (max < maxData)
                        max = maxData;
                    if (tscData.getName().contains("DNA")) {
                        max = 5;
                    }
                }

            }
        }
        max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
        return max;
    }

    public double getBioCondMax(String bioCondName) {
        double max = -1000000;
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData instanceof ExpressionData) {
                ExpressionData data = (ExpressionData) tscData;
                double maxData = data.getMax();
                // System.out.println(maxData+" "+data.getName());
                if (max < maxData)
                    max = maxData;

            }
            if (tscData instanceof ExpressionMatrix || tscData instanceof ProteomicsData) {
                ExpressionMatrix data = (ExpressionMatrix) tscData;
                double maxData = ExpressionMatrixStat.max(data, data.getGenomeViewerColumnIndex());
                if (max < maxData)
                    max = maxData;
            } else {
                double maxData = 9;
                if (max < maxData)
                    max = maxData;
                if (tscData.getName().contains("DNA")) {
                    max = 5;
                }
            }
        }
        max = Double.parseDouble(String.format("%.2f", max).replaceFirst(",", "."));
        return max;
    }

    public double getMin() {
        double min = 1000000;
        for (BioCondition bioCond : getBioConditionHashMaps().values()) {
            for (OmicsData tscData : bioCond.getOmicsData()) {
                if (tscData instanceof ExpressionData) {
                    ExpressionData data = (ExpressionData) tscData;
                    double minData = data.getMin();
                    if (min > minData)
                        min = minData;
                }
                if (tscData instanceof ExpressionMatrix || tscData instanceof ProteomicsData) {
                    ExpressionMatrix data = (ExpressionMatrix) tscData;
                    double minData = ExpressionMatrixStat.min(data, data.getGenomeViewerColumnIndex());
                    if (min > minData)
                        min = minData;
                } else {
                    double minData = -9;
                    if (min > minData)
                        min = minData;
                }
            }
        }
        min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
        return min;
    }

    public double getBioCondMin(String bioCondName) {
        double min = 1000000;
        for (OmicsData tscData : getBioConditionHashMaps().get(bioCondName).getOmicsData()) {
            if (tscData instanceof ExpressionData) {
                ExpressionData data = (ExpressionData) tscData;
                double minData = data.getMin();
                if (min > minData)
                    min = minData;
            } else if (tscData instanceof ExpressionMatrix || tscData instanceof ProteomicsData) {
                ExpressionMatrix data = (ExpressionMatrix) tscData;
                double minData = ExpressionMatrixStat.min(data, data.getGenomeViewerColumnIndex());
                if (min > minData)
                    min = minData;
            } else if (tscData instanceof NGS) {
                NGS seqData = (NGS) tscData;
                ExpressionData dataset = seqData.getDatasets().get(track.getChromosomeID());
                double minData = dataset.getMin();
                if (min > minData)
                    min = minData;
            } else {
                double minData = -9;
                if (min > minData)
                    min = minData;
            }
        }
        min = Double.parseDouble(String.format("%.2f", min).replaceFirst(",", "."));
        return min;
    }

    /*
     * ************************************************************* GETTERS and SETTERS
     * *************************************************************
     */

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public boolean[] getDisplay() {
        return display;
    }

    public void setDisplay(boolean[] display) {
        this.display = display;
    }

    public TreeMap<String, Color> getDataColors() {
        return dataColors;
    }

    public void setDataColors(TreeMap<String, Color> dataColors) {
        this.dataColors = dataColors;
    }

    public TreeMap<String, Float> getDataSize() {
        return dataSizes;
    }

    public LinkedHashMap<String, BioCondition> getBioConditionHashMaps() {
        return bioConditionHashMaps;
    }

    public boolean isDisplayAbsoluteValue() {
        return displayAbsoluteValue;
    }

    public void setDisplayAbsoluteValue(boolean displayAbsoluteValue) {
        this.displayAbsoluteValue = displayAbsoluteValue;
    }

    public void setBioConditionHashMaps(LinkedHashMap<String, BioCondition> bioConditionHashMaps) {
        this.bioConditionHashMaps = bioConditionHashMaps;
    }

    public ArrayList<String> getDataNOTDisplayed() {
        return dataNOTDisplayed;
    }

    public void setDataNOTDisplayed(ArrayList<String> dataNOTDisplayed) {
        this.dataNOTDisplayed = dataNOTDisplayed;
    }

    public TreeMap<String, Float> getDataSizes() {
        return dataSizes;
    }

}
