package bacnet.datamodel.expdesign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.ProteomicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;

/**
 * This class is used for the different bioconds declared in the experiment
 * 
 * It contains the biocond name, and all the related data.
 * 
 * IMPORTANT, MAJ 04102011 : WT data are only used as indication on experimental design, in all
 * analysis if we want WT we need to add it in the Experiment
 */
public class BioCondition implements Serializable {

    private static final long serialVersionUID = -8545052328831545661L;

    /**
     * BioConditoon separator for comparisons<br>
     * equal to " vs "
     */
    public static String SEPARATOR = "_vs_";

    /**
     * Name of the BioCondition
     */
    private String name = "";
    /**
     * Reference genome ID to used for this BioCondition
     */
    private String genomeName = "";
    /**
     * Actual strain used to create this BioCondition, might be different from genomeName
     */
    private String genomeUsed = "";
    /**
     * Type of bioCondition : TSS only, TIS only, RNASeq + Tiling + GeneExpr, RNASeq only ...
     */
    private TreeSet<TypeData> typeDataContained = new TreeSet<>();
    /**
     * Date of creation or data of publication
     */
    private String date = "";
    /**
     * For experiment from Cossart lab, ID of the experiment
     */
    private int experienceNb = -1;
    /**
     * True if this BioCondition is a wild-type strain, to be used as a reference condition
     */
    private boolean wildType = false;
    /**
     * Different comment
     */
    private String comment = "";
    /**
     * All Listeria GeneExpression arrays associated to this BioCondition
     */
    private ArrayList<GeneExpression> geneExprs = new ArrayList<GeneExpression>();
    /**
     * All Listeria Tiling arrays associated to this BioCondition
     */
    private ArrayList<Tiling> tilings = new ArrayList<Tiling>();
    /**
     * All RNASeq data associated to this BioCondition
     */
    private ArrayList<NGS> ngss = new ArrayList<NGS>();

    /**
     * All NTermData data associated to this BioCondition
     */
    private ArrayList<NTermData> nTerms = new ArrayList<NTermData>();
    /**
     * All ProteomicsData data associated to this BioCondition
     */
    private ArrayList<ProteomicsData> proteomes = new ArrayList<ProteomicsData>();
    /**
     * All data in <code>ExpressioMatrix</code> associated to this BioCondition
     */
    private ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
    /**
     * Data associated to this BioCondition to use in the case of timepoint experiment, or same mutant
     * in different condition
     */
    private ArrayList<String> linkedBioCondition = new ArrayList<>();
    /**
     * ArrayExpressId if this data is public
     */
    private String arrayExpressId = "";
    /**
     * Name of the study from which the data is extracted
     */
    private String studyName = "";
    /**
     * ArrayExpressId of the technology used
     */
    private String arrayExpressTechnoId = "";
    /**
     * If it is a mutant, the locus is indicated (example: prfA, lmo0200 or rliA), leave empty otherwise
     */
    private ArrayList<String> mutant = new ArrayList<>();
    /**
     * Growth conditions are specified here
     */
    private ArrayList<String> growth = new ArrayList<>();
    /**
     * Growth media is specified here
     */
    private ArrayList<String> media = new ArrayList<>();
    /**
     * Temperature is specified here
     */
    private String temperature = "";
    /**
     * time point if necessary
     */
    private String time = "";
    /**
     * Article in which data is published
     */
    private String reference = "";
    /**
     * Quantity/properties of drugs added to the media if specified: quantity of glucose, type of Broth
     */
    private ArrayList<String> mediaGrowthProperties = new ArrayList<>();
    /**
     * If it is a proteome, this parameters tells you where in the bacteria cell the proteins have been
     * extracted
     */
    private ArrayList<String> localization = new ArrayList<>();
    /**
     * List of Data for which we have comparison data: This Biocondition vs BioCondComparison
     */
    private ArrayList<String> comparisons = new ArrayList<>();
    /**
     * List of Data for which the BioCondition is the reference bioCodntion for the comparison:
     * BioCondComparison vs This Biocondition
     */
    private ArrayList<String> antiComparisons = new ArrayList<>();
    
    private String ENAProject = "";
    private String GEOProject = "";
    private String GEOPlatform = "";
    private String sequencingPlatform = "";
    private String massSpectrometer = "";
    private String prideID = "";

    /**
     * True if no data are associated to this BioCondition<br>
     * It is used principaly to indicate that the BioCondition is a reference condition for another
     * BioCondition, and thus no data are available for it, as the data are in the other BioCondition
     * 
     */
    private boolean noData = false;

    public BioCondition() {}

    public BioCondition(String bioCondName) {
        this.name = bioCondName;
    }

    public boolean containTranscriptomes() {
        boolean found = false;
        for (TypeData dataType : this.getTypeDataContained()) {
            if (dataType == TypeData.DNASeq)
                found = true;
            if (dataType == TypeData.ExpressionMatrix)
                found = true;
            if (dataType == TypeData.GeneExpr)
                found = true;
            if (dataType == TypeData.RiboSeq)
                found = true;
            if (dataType == TypeData.RNASeq)
                found = true;
            if (dataType == TypeData.TermSeq)
                found = true;
            if (dataType == TypeData.Tiling)
                found = true;
            if (dataType == TypeData.TSS)
                found = true;
        }
        return found;
    }
    
    public boolean containRNASeq() {
        boolean found = false;
        for (TypeData dataType : this.getTypeDataContained()) {
            if (dataType == TypeData.RNASeq)
                found = true;
        }
        return found;
    }
    public boolean containProteomes() {
        boolean found = false;
        for (TypeData dataType : this.getTypeDataContained()) {
            if (dataType == TypeData.Proteome)
                found = true;
        }
        return found;
    }

    /**
     * Put all ExpressionData in a list
     * 
     * @return
     */
    public ArrayList<ExpressionData> getExpressionData() {
        ArrayList<ExpressionData> datas = new ArrayList<ExpressionData>();
        for (GeneExpression expr : geneExprs) {
            datas.add(expr);
        }
        for (Tiling expr : tilings) {
            datas.add(expr);
        }
        for (NGS expr : ngss) {
            for (ExpressionData dataset : expr.getDatasets().values()) {
                datas.add(dataset);
            }
        }
        return datas;
    }

    /**
     * Put all TranscriptomeData in a list
     * 
     * @return
     */
    public ArrayList<OmicsData> getTranscriptomesData() {
        ArrayList<OmicsData> datas = new ArrayList<OmicsData>();
        for (GeneExpression expr : geneExprs) {
            datas.add(expr);
        }
        for (Tiling expr : tilings) {
            datas.add(expr);
        }
        for (NGS expr : ngss) {
            datas.add(expr);
        }

        for (ExpressionMatrix matrix : matrices) {
            datas.add(matrix);
        }
        return datas;
    }

    /**
     * Get all genomes associated to a transcriptomes dataset
     * 
     * @return
     */
    public static ArrayList<String> getTranscriptomesGenomes() {
        TreeSet<String> setGenomes = new TreeSet<String>();
        for (BioCondition bioCond : getAllBioConditions()) {
            if (bioCond.containTranscriptomes()) {
                setGenomes.add(bioCond.getGenomeName());
            }
        }
        ArrayList<String> listGenomes = new ArrayList<>();
        for (String genome : setGenomes) {
            listGenomes.add(genome);
        }

        return listGenomes;
    }
    /**
     * Get all genomes associated to a transcriptomes dataset
     * 
     * @return
     */
    public static ArrayList<String> getRNASeqGenomes() {
        TreeSet<String> setGenomes = new TreeSet<String>();
        for (BioCondition bioCond : getAllBioConditions()) {
            if (bioCond.containRNASeq()) {
                setGenomes.add(bioCond.getGenomeName());
            }
        }
        ArrayList<String> listGenomes = new ArrayList<>();
        for (String genome : setGenomes) {
            listGenomes.add(genome);
        }

        return listGenomes;
    }

    /**
     * Put all OmicsData in a list
     * 
     * @return
     */
    public ArrayList<OmicsData> getOmicsData() {
        ArrayList<OmicsData> datas = new ArrayList<OmicsData>();
        for (ExpressionData data : getExpressionData()) {
            datas.add(data);
        }
        for (ExpressionMatrix matrix : matrices) {
            datas.add(matrix);
        }
        for (NTermData data : nTerms) {
            datas.add(data);
        }
        for (ProteomicsData data : proteomes) {
            datas.add(data);
        }
        return datas;
    }

    /**
     * Put all OmicsData in a list
     * 
     * @return
     */
    public ArrayList<OmicsData> getProteomicsData() {
        ArrayList<OmicsData> datas = new ArrayList<OmicsData>();
        for (NTermData data : nTerms) {
            datas.add(data);
        }
        for (ProteomicsData data : proteomes) {
            datas.add(data);
        }
        return datas;
    }

    /**
     * Get all genomes associated to a proteomics dataset
     * 
     * @return
     */
    
    public static ArrayList<String> getProteomeGenomes() {
        TreeSet<String> setGenomes = new TreeSet<String>();
        for (BioCondition bioCond : getAllBioConditions()) {
            if (bioCond.containProteomes()) {
                setGenomes.add(bioCond.getGenomeName());
            }
        }
        ArrayList<String> listGenomes = new ArrayList<>();
        for (String genome : setGenomes) {
            listGenomes.add(genome);
        }
        return listGenomes;
    }

    /**
     * Get every names of the Comparison which can be found:<br>
     * <li>Tiling arrays
     * <li>Gene Expression arrays
     * <li>ExpressionMatrix
     * 
     * @return a list of all the data
     */
    public ArrayList<String> getComparisonDataNames() {
        ArrayList<String> dataNames = new ArrayList<String>();
        for (String compName : getComparisons()) {
            if (getTilings().size() != 0) {
                String compData = this.getName() + ".+.gr" + SEPARATOR + compName + ".+.gr";
                dataNames.add(compData);
                compData = this.getName() + ".-.gr" + SEPARATOR + compName + ".-.gr";
                dataNames.add(compData);
            }

            if (getGeneExprs().size() != 0) {
                String compData = this.getName() + ".ge" + SEPARATOR + compName + ".ge";
                dataNames.add(compData);
            }
            // if(getMatrices().size()!=0){
            String compData = this.getName() + SEPARATOR + compName;
            dataNames.add(compData);
            // }
        }
        return dataNames;
    }

    /**
     * Return corresponding Genome given by this.genomeName<br>
     * Load it if necessary
     * 
     * @return
     */
    public Genome getGenome() {
        return Genome.loadGenome(getGenomeName());
    }

    /**
     * Return a list of all Comparisons: bioCondName vs comparisons.get(i)
     * 
     * @return
     */
    public ArrayList<String> getComparisonNames() {
        ArrayList<String> comparisonNames = new ArrayList<>();
        for (String comp : this.getComparisons()) {
            comparisonNames.add(this.getName() + SEPARATOR + comp);
        }
        return comparisonNames;
    }

    /**
     * Create a BioCondition containing the different data for comparing this BioCondition to bioCond2
     * 
     * @param bioCond2
     * @return
     * @throws IOException
     */
    public BioCondition compare(BioCondition bioCond2, boolean calcData) {
        // System.out.println("Create: "+this.getName()+" vs "+bioCond2.getName());
        /*
         * Create BioCondition
         */
        BioCondition bioConditionCompare = new BioCondition(this.getName() + SEPARATOR + bioCond2.getName());
        bioConditionCompare.setDate(date);
        bioConditionCompare.setArrayExpressId(arrayExpressId);
        bioConditionCompare.setComment(comment);
        bioConditionCompare.setArrayExpressTechnoId(arrayExpressTechnoId);
        bioConditionCompare.setGenomeName(genomeName);
        bioConditionCompare.setGenomeUsed(genomeUsed);
        bioConditionCompare.setGrowth(growth);
        bioConditionCompare.setLocalization(localization);
        bioConditionCompare.setMedia(media);
        bioConditionCompare.setMediaGrowthProperties(mediaGrowthProperties);
        bioConditionCompare.setMutant(mutant);
        bioConditionCompare.setReference(reference);
        bioConditionCompare.setStudyName(studyName);
        bioConditionCompare.setTemperature(temperature);
        bioConditionCompare.setTime(time);
        bioConditionCompare.setWildType(isWildType());

        /*
         * Add Omics Data
         */
        for (int i = 0; i < this.getGeneExprs().size(); i++) {
            GeneExpression data1 = this.getGeneExprs().get(i);
            GeneExpression data2 = bioCond2.getGeneExprs().get(i);
            GeneExpression compData = data1.compare(data2, calcData);
            bioConditionCompare.getGeneExprs().add(compData);
            bioConditionCompare.getTypeDataContained().add(TypeData.GeneExpr);
        }
        for (int i = 0; i < this.getTilings().size(); i++) {
            Tiling data1 = this.getTilings().get(i);
            Tiling data2 = bioCond2.getTilings().get(i);
            Tiling compData = data1.compare(data2, calcData);
            bioConditionCompare.getTilings().add(compData);
            bioConditionCompare.getTypeDataContained().add(TypeData.Tiling);
        }
        
        
        for (int i = 0; i < this.getNGSSeqs().size(); i++) {
            NGS data1 = this.getNGSSeqs().get(i);
            NGS data2 = bioCond2.getNGSSeqs().get(i);
            System.out.println("in compare: "+ data1.getBioCondName() + " + " + data2.getBioCondName());
            NGS compData = data1.compare(data2, calcData);
            bioConditionCompare.getNGSSeqs().add(compData);
            bioConditionCompare.getTypeDataContained().add(TypeData.ExpressionMatrix);

        } 
        
        /*
         * Proteomics data comparison if they exists
         */
        File file = new File(OmicsData.PATH_STREAMING + bioConditionCompare.getName() + ProteomicsData.EXTENSION);
        System.out.println("in compare add proteomics: "+ file.getName());

        if (file.exists()) {
            System.out.println("in compare add proteomics if: "+ file.getName());

            ProteomicsData matrix = new ProteomicsData();
            matrix.setName(bioConditionCompare.getName());
            bioConditionCompare.getProteomes().add(matrix);
            bioConditionCompare.getTypeDataContained().add(TypeData.Proteome);
        }

        /*
         * Expression data comparison if they exists
         */
        file = new File(OmicsData.PATH_STREAMING + bioConditionCompare.getName() + OmicsData.EXTENSION);
        System.out.println("in compare add matrix: "+ file.getName());

        if (file.exists()) {
            System.out.println("in compare add matrix if: "+ file.getName());

            ExpressionMatrix matrix = new ExpressionMatrix();
            matrix.setName(bioConditionCompare.getName());
            bioConditionCompare.getMatrices().add(matrix);
            bioConditionCompare.getTypeDataContained().add(TypeData.ExpressionMatrix);
        }

        return bioConditionCompare;
    }

    /**
     * From a biological condition name load the corresponding BioCondition object
     * 
     * @param bioCondName
     * @return
     */
    @SuppressWarnings("static-access")
    public static BioCondition getBioCondition(String bioCondName) {
        // test if the file exists
        File file = new File(Database.getInstance().getBIOCONDITION_PATH() + bioCondName);
        if (file.exists()) {
            BioCondition bioCond = BioCondition.load(Database.getInstance().getBIOCONDITION_PATH() + bioCondName);
            return bioCond;
        } else {
            System.err.println("BioCondition: " + bioCondName + " does not exists");
            return null;
        }
    }

    /**
     * From a biological condition name, get from the ModelProvider the corresponding BioCondition
     * object<br>
     * If not available it will load them for the hard drive
     * 
     * @param bioCondName
     * @param true if you want to get BioCondition from pre-loaded BioCondition
     * @return
     */
    public static BioCondition getBioCondition(String bioCondName, boolean modelProvider) {
        BioCondition bioCond = Experiment.getGeneralExp().getBioConds().get(bioCondName);
        if (bioCond != null) {
            return bioCond;
        } else {
            return getBioCondition(bioCondName);
        }
    }

    /**
     * Parse a Comparison name, extracting leftBioCondition and rightBiocondition
     * 
     * @param name
     * @return {leftBioCond,rightBioCond}
     */
    public static String[] parseName(String name) {
        String[] compElement = name.split(SEPARATOR);
        String leftBioCond = compElement[0].trim();
        String rightBioCond = compElement[1].trim();
        String[] bioConds = {leftBioCond, rightBioCond};
        return bioConds;
    }

    /**
     * Get all available <code>BioCondition</code> names by reading the text file
     * <code>Experiment.INCLUDE_COND_PATH</code>
     * 
     * @return
     */
    public static ArrayList<String> getAllBioConditionNames() {
        ArrayList<String> listBioConds =
                TabDelimitedTableReader.readList(Database.getInstance().getBioConditionsArrayPath());
        // Remove duplicates
        TreeSet<String> setBioConds = new TreeSet<>();
        for (String bioCond : listBioConds) {
            setBioConds.add(bioCond);
        }
        listBioConds.clear();
        for (String bioCond : setBioConds) {
            listBioConds.add(bioCond);
        }
        listBioConds.remove("BioCondName");
        return listBioConds;
    }

    /**
     * Return all BioCondition available
     * 
     * @return
     */
    public static ArrayList<BioCondition> getAllBioConditions() {
        ArrayList<String> list = getAllBioConditionNames();
        ArrayList<BioCondition> bioConds = new ArrayList<BioCondition>();
        for (String name : list) {
            bioConds.add(BioCondition.getBioCondition(name));
        }
        return bioConds;
    }

    /**
     * Return all BioCondition available
     * 
     * @return
     */
    public static ArrayList<BioCondition> getAllPublishedBioConditions() {
        ArrayList<String> list = getAllBioConditionNames();
        ArrayList<BioCondition> bioConds = new ArrayList<BioCondition>();
        for (String name : list) {
            BioCondition bioCond = BioCondition.getBioCondition(name);
            if (!bioCond.getReference().contains("Unpublished")) {
                bioConds.add(bioCond);
            }
        }
        return bioConds;
    }

    /**
     * Return all BioCondition available
     * 
     * @return
     */
    public static ArrayList<BioCondition> getAllUnPublishedBioConditions() {
        ArrayList<String> list = getAllBioConditionNames();
        ArrayList<BioCondition> bioConds = new ArrayList<BioCondition>();
        for (String name : list) {
            BioCondition bioCond = BioCondition.getBioCondition(name);
            if (bioCond.getReference().contains("Unpublished")) {
                bioConds.add(bioCond);
            }
        }
        return bioConds;
    }

    /*
     * ************************************************************** Load and Save
     * **************************************************************
     */
    /**
     * Read compressed, serialized data with a FileInputStream. Uncompress that data with a
     * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream. Replace current data
     * with new data, and redraw everything.
     */
    public static BioCondition load(String fileName) {
        try {
            // Create necessary input streams
            FileInputStream fis = new FileInputStream(fileName); // Read from file
            GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
            ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
            // Read in an object. It should be a vector of scribbles
            BioCondition bioCond = (BioCondition) in.readObject();
            in.close(); // Close the stream.
            return bioCond;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * Prompt the user for a filename, and save the scribble in that file. Serialize the vector of lines
     * with an ObjectOutputStream. Compress the serialized objects with a GZIPOutputStream. Write the
     * compressed, serialized data to a file with a FileOutputStream. Don't forget to flush and close
     * the stream.
     */
    public void save(String fileName) {
        // Create a file dialog to query the user for a filename.
        if (fileName != null) { // If user didn't click "Cancel".
            try {
                // Create the necessary output streams to save the scribble.
                FileOutputStream fos = new FileOutputStream(fileName);
                // Save to file
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                // Compressed
                ObjectOutputStream out = new ObjectOutputStream(gzos);
                // Save objects
                out.writeObject(this); // Write the entire Vector of scribbles
                out.flush(); // Always flush the output.
                out.close(); // And close the stream.
            }
            // Print out exceptions. We should really display them in a dialog...
            catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    /*
     * *****************************************************
     * 
     * Getters and Setters
     * 
     * *****************************************************
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genomeName) {
        this.genomeName = genomeName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getExperienceNb() {
        return experienceNb;
    }

    public void setExperienceNb(int experienceNb) {
        this.experienceNb = experienceNb;
    }

    public boolean isWildType() {
        return wildType;
    }

    public void setWildType(boolean wildType) {
        this.wildType = wildType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ArrayList<GeneExpression> getGeneExprs() {
        return geneExprs;
    }

    public void setGeneExprs(ArrayList<GeneExpression> geneExprs) {
        this.geneExprs = geneExprs;
    }

    public ArrayList<Tiling> getTilings() {
        return tilings;
    }

    public void setTilings(ArrayList<Tiling> tilings) {
        this.tilings = tilings;
    }

    public ArrayList<ExpressionMatrix> getMatrices() {
        return matrices;
    }

    public void setMatrices(ArrayList<ExpressionMatrix> matrices) {
        this.matrices = matrices;
    }

    public String getArrayExpressId() {
        return arrayExpressId;
    }

    public void setArrayExpressId(String arrayExpressId) {
        this.arrayExpressId = arrayExpressId;
    }

    public String getArrayExpressTechnoId() {
        return arrayExpressTechnoId;
    }

    public boolean isNoData() {
        return noData;
    }

    public void setNoData(boolean noData) {
        this.noData = noData;
    }

    public void setArrayExpressTechnoId(String arrayExpressTechnoId) {
        this.arrayExpressTechnoId = arrayExpressTechnoId;
    }

    public String getGenomeUsed() {
        return genomeUsed;
    }

    public void setGenomeUsed(String genomeUsed) {
        this.genomeUsed = genomeUsed;
    }

    public ArrayList<String> getMutant() {
        return mutant;
    }

    public void setMutant(ArrayList<String> mutant) {
        this.mutant = mutant;
    }

    public ArrayList<String> getMediaGrowthProperties() {
        return mediaGrowthProperties;
    }

    public void setMediaGrowthProperties(ArrayList<String> mediaGrowthProperties) {
        this.mediaGrowthProperties = mediaGrowthProperties;
    }

    public ArrayList<String> getGrowth() {
        return growth;
    }

    public void setGrowth(ArrayList<String> growth) {
        this.growth = growth;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ArrayList<String> getMedia() {
        return media;
    }

    public void setMedia(ArrayList<String> media) {
        this.media = media;
    }

    public ArrayList<String> getLinkedBioCondition() {
        return linkedBioCondition;
    }

    public void setLinkedBioCondition(ArrayList<String> linkedBioCondition) {
        this.linkedBioCondition = linkedBioCondition;
    }

    public ArrayList<String> getComparisons() {
        return comparisons;
    }

    public void setComparisons(ArrayList<String> comparisons) {
        this.comparisons = comparisons;
    }

    public TreeSet<TypeData> getTypeDataContained() {
        return typeDataContained;
    }

    public ArrayList<NTermData> getnTerms() {
        return nTerms;
    }

    public ArrayList<NGS> getNGSSeqs() {
        return ngss;
    }

    // public ArrayList<NewRNASeq> getNewRnaSeqs() {
    // return newRnaSeqs;
    // }
    //
    // public void setNewRnaSeqs(ArrayList<NewRNASeq> newRnaSeqs) {
    // this.newRnaSeqs = newRnaSeqs;
    // }

    public void setNGSSeqs(ArrayList<NGS> rnaSeqs) {
        this.ngss = rnaSeqs;
    }

    public void setnTerms(ArrayList<NTermData> nTerms) {
        this.nTerms = nTerms;
    }

    public ArrayList<ProteomicsData> getProteomes() {
        return proteomes;
    }

    public void setProteomes(ArrayList<ProteomicsData> proteomes) {
        this.proteomes = proteomes;
    }

    public void setTypeDataContained(TreeSet<TypeData> typeDataContained) {
        this.typeDataContained = typeDataContained;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public ArrayList<String> getAntiComparisons() {
        return antiComparisons;
    }

    public void setAntiComparisons(ArrayList<String> antiComparisons) {
        this.antiComparisons = antiComparisons;
    }

    public ArrayList<String> getLocalization() {
        return localization;
    }

    public void setLocalization(ArrayList<String> localization) {
        this.localization = localization;
    }
        
    public String getENAProject() {
        return ENAProject;
    }

    public void setENAProject(String ENAProject) {
        this.ENAProject = ENAProject;
    }

    public String getGEOProject() {
        return GEOProject;
    }

    public void setGEOProject(String GEOProject) {
        this.GEOProject = GEOProject;
    }

    public String getGEOPlatform() {
        return GEOPlatform;
    }

    public void setGEOPlatform(String GEOPlatform) {
        this.GEOPlatform = GEOPlatform;
    }

    public String getSequencingPlatform() {
        return sequencingPlatform;
    }

    public void setSequencingPlatform(String sequencingPlatform) {
        this.sequencingPlatform = sequencingPlatform;
    }

    public String getMassSpectrometer() {
        return massSpectrometer;
    }

    public void setMassSpectrometer(String massSpectrometer) {
        this.massSpectrometer = massSpectrometer;
    }
    
    public String getPrideID() {
        return prideID;
    }

    public void setPrideID(String prideID) {
        this.prideID = prideID;
    }
    
}

    