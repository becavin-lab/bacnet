package bacnet.datamodel.dataset;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.proteomics.NTerm.TypeModif;
import bacnet.datamodel.proteomics.TIS;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;

public class NTermData extends OmicsData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 497243595006646884L;

    public static final String[] TYPE_OVERLAPS =
            {"annotated TIS", "multiple TIS", "updated TIS", "internal TIS", "new TIS", "uncategorized"};

    /**
     * Link NTerm name to <code>NTerm</code><br>
     * This correspond to the list of <code>NTerm</code>
     */
    private HashMap<String, NTerm> elements = new HashMap<String, NTerm>();

    /**
     * Link peptideSequence to <code>ArrayList(NTerm)</code><br>
     * This correspond to the list of <code>NTerm</code> having same peptideSequence
     */
    private HashMap<String, ArrayList<NTerm>> peptides = new HashMap<String, ArrayList<NTerm>>();

    /**
     * Link NTerm position (coded as a <code>String</code> to <code>ArrayList(NTerm)</code><br>
     * This correspond to the list of mapped sequence which are equivalent to other NTerm but with
     * different modification type
     */
    private HashMap<String, ArrayList<NTerm>> modifications = new HashMap<String, ArrayList<NTerm>>();
    /**
     * Link lmo genes to NTerm overlapping it<br>
     * NTerm with no association to an lmo are associated to "unknown" key
     */
    private HashMap<String, ArrayList<NTerm>> genes = new HashMap<String, ArrayList<NTerm>>();
    /**
     * Link gene to they corresponding aTIS if it exists
     */
    private HashMap<String, ArrayList<NTerm>> aTISMap = new HashMap<String, ArrayList<NTerm>>();
    /**
     * Link gene to a list of all <code>TypeModif</code> encountered for aTIS
     */
    private HashMap<String, ArrayList<TypeModif>> geneModification = new HashMap<String, ArrayList<TypeModif>>();
    /**
     * Link <code>Nterm</code> to a list of all <code>Nterm</code> which might be related to it (e.g.:
     * almost same sequence but proteolyse cut it)
     */
    private HashMap<NTerm, TIS> tisMap = new HashMap<NTerm, TIS>();

    private ArrayList<TIS> tisList = new ArrayList<TIS>();

    /**
     * List of Overlap Type used for TIS classification = aTIS, internal TIS, etc...
     */
    private ArrayList<String> typeOverlaps = new ArrayList<>();

    /**
     * An <code>Annotation</code> object to order NTerm by position in the Genome
     */
    private Annotation annotation;

    public NTermData() {
        setType(TypeData.NTerm);
    }

    public NTermData(String name) {
        setType(TypeData.NTerm);
        setName(name);
    }

    public NTermData(String name, HashMap<String, NTerm> elements) {
        setType(TypeData.NTerm);
        setName(name);
        setElements(elements);
    }

    /**
     * Add a NTerm in elements HashMap:<br>
     * key = nTerm.getName()<br>
     * value = nTerm
     * 
     * @param nTerm
     */
    public void addElement(NTerm nTerm) {
        this.getElements().put(nTerm.getName(), nTerm);
    }

    /**
     * Regroup in different HashMap the NTerm<br>
     * Fill:<br>
     * <li>HashMap<String, ArrayList<NTerm>> peptides is to make a simple HashMap between NTerm sequence
     * and a list of corresponding <code>NTerm</code>
     * <li>HashMap<String, ArrayList<NTerm>> modifications links Nterm position to a list of
     * corresponding <code>NTerm</code>
     * 
     */
    public void createHashMap() {
        peptides = new HashMap<String, ArrayList<NTerm>>();
        modifications = new HashMap<String, ArrayList<NTerm>>();
        for (NTerm nTerm : elements.values()) {
            String peptideSequence = nTerm.getSequencePeptide();
            if (peptides.containsKey(peptideSequence)) {
                peptides.get(peptideSequence).add(nTerm);
            } else {
                ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
                nTerms.add(nTerm);
                peptides.put(peptideSequence, nTerms);
            }
        }
        for (NTerm nTerm : elements.values()) {
            String position = nTerm.getBegin() + "," + nTerm.getEnd() + "," + nTerm.getStrand();
            if (modifications.containsKey(position)) {
                modifications.get(position).add(nTerm);
            } else {
                ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
                nTerms.add(nTerm);
                modifications.put(position, nTerms);
            }
        }
        System.out.println("HashMaps->  elements: " + elements.size() + " peptides: " + peptides.size()
                + " modifications: " + modifications.size());
    }

    /**
     * Regroup NTerm using genes of EGD-e<br>
     * Each EGD-e gene will be associated to a list of NTerm overlapping it<br>
     * Fill: HashMap<String, ArrayList<NTerm>>() aTISMap, HashMap<String, ArrayList<NTerm>>() genes,
     * HashMap<String, ArrayList<TypeModif>>() geneModification
     */
    public void createGeneHashMap() {
        aTISMap = new HashMap<String, ArrayList<NTerm>>();
        genes = new HashMap<String, ArrayList<NTerm>>();
        geneModification = new HashMap<String, ArrayList<TypeModif>>();
        Genome genome = Genome.loadEgdeGenome();
        for (Gene gene : genome.getFirstChromosome().getGenes().values()) {
            String locus = gene.getName();
            for (NTerm nTerm : elements.values()) {
                if (nTerm.getOverlap().contains(locus)) {
                    if (genes.containsKey(locus)) {
                        genes.get(locus).add(nTerm);
                    } else {
                        ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
                        nTerms.add(nTerm);
                        genes.put(locus, nTerms);
                    }
                    if (nTerm.getTypeOverlap() == "aTIS") {
                        if (aTISMap.containsKey(locus)) {
                            aTISMap.get(locus).add(nTerm);
                        } else {
                            ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
                            nTerms.add(nTerm);
                            aTISMap.put(locus, nTerms);
                        }
                    }
                }
            }

            if (aTISMap.containsKey(locus)) {
                TreeSet<TypeModif> modifs = new TreeSet<NTerm.TypeModif>();
                for (NTerm nTerm : aTISMap.get(locus)) {
                    modifs.add(nTerm.getTypeModif());
                }
                ArrayList<TypeModif> modifsArray = new ArrayList<NTerm.TypeModif>();
                for (TypeModif modif : modifs)
                    modifsArray.add(modif);
                geneModification.put(locus, modifsArray);
            }

        }

        System.out.println("HashMaps->  elements: " + elements.size() + " genes: " + genes.size()
                + " geneModification: " + geneModification.size() + " aTISMap: " + aTISMap.size());
    }

    /**
     * Regroup peptides which overlap -> the assumption is that they come from the same protein but were
     * created from proteolyse
     */
    public void createProteolyseMap() {
        tisList = new ArrayList<TIS>();
        tisMap = new HashMap<NTerm, TIS>();
        int i = 0;
        ArrayList<String> nTermsAlreadyUsed = new ArrayList<String>();
        ArrayList<NTerm> allNterms = getNTerms();
        ArrayList<NTerm> allNterms2 = getNTerms();
        for (NTerm nTerm : allNterms) {
            if (!nTermsAlreadyUsed.contains(nTerm.getName())) {
                i++;
                if (i % 200 == 0)
                    System.out.println("peptide: " + i + " - " + nTerm.getName());
                // nTermsAlreadyUsed.add(nTerm);
                ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
                for (NTerm nTermTemp : allNterms2) {
                    if (!nTermsAlreadyUsed.contains(nTermTemp.getName())) {
                        if (!nTerm.getName().equals(nTermTemp.getName()) && Sequence.isOverlap(nTerm, nTermTemp)) {
                            nTerms.add(nTermTemp);
                            nTermsAlreadyUsed.add(nTermTemp.getName());
                        }
                    }
                }

                nTerms.add(nTerm);
                TIS tis = new TIS();
                tis.setName("TIS_" + i);
                tis.setnTerms(nTerms);
                tis.findModifs();
                tis.findOverlaps();
                tis.findRefSequence();
                tis.findNTermRef();
                this.getTisList().add(tis);
                for (NTerm nTermTemp : nTerms) {
                    this.getTisMap().put(nTermTemp, tis);
                }
            }
        }
        System.out.println("HashMaps->  elements: " + elements.size() + " proteolyse: " + tisMap.size());
    }

    /**
     * Go through <code>proteolyses</code> map, and find the list of NTerm which might be associated to
     * an input <code>NTerm</code><br>
     * Warning: <code>ArrayList</code> in return contains <code>key NTerm</code> of
     * <code>proteolyses</code> but not the input <code>nTerm</code>
     * 
     * @param nTerm
     * @return
     */
    public TIS getProteolyseAssociated(NTerm nTerm) {
        if (this.getTisMap().containsKey(nTerm)) {
            return this.getTisMap().get(nTerm);
        }
        return new TIS();
    }

    /**
     * Create an <code>Annotation</code> Table from the list of <code>Nterm</code>
     */
    public void createAnnotation() {
        String[][] annot = new String[getElements().size() + 1][Annotation.HEADER.length];
        for (int j = 0; j < Annotation.HEADER.length; j++) {
            annot[0][j] = Annotation.HEADER[j];
        }
        int i = 1;
        for (String name : getElements().keySet()) {
            NTerm seq = getElements().get(name);
            annot[i][0] = seq.getName();
            annot[i][1] = seq.getBegin() + "";
            annot[i][2] = seq.getEnd() + "";
            annot[i][3] = seq.getLength() + "";
            annot[i][4] = seq.getStrand() + "";
            annot[i][5] = seq.getTypeModif() + "";
            // typeSrna and synonim
            annot[i][6] = "";
            annot[i][7] = "";
            i++;
        }
        System.out.println("Annotation array created");
        ExpressionMatrix annotMatrix = ExpressionMatrix.arrayToExpressionMatrix(annot, true);
        // annotMatrix.saveTab(path+File.separator+"AnnotationTemp.txt", "Elements");
        System.out.println("Sort annotation");
        /*
         * LONGER STEP : SORTING
         */
        annotMatrix = annotMatrix.sort(1, true);

        // annotMatrix.saveTab("D:/PasteurSVN/N-Term SVN/Results/NTerm-Genome
        // order.txt", "NTerm");
        Annotation annotation = new Annotation(annotMatrix.toArray("NTerm"));
        annotation.setGenome(Genome.EGDE_NAME);
        annotation.setChromosomeID("0");
        this.setAnnotation(annotation);
    }

    /**
     * Return a list of all NTerms available
     * 
     * @return
     */
    public ArrayList<NTerm> getNTerms() {
        ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
        for (NTerm nTerm : getElements().values()) {
            nTerms.add(nTerm);
        }
        return nTerms;
    }

    /**
     * Return a HashMap of <code>NTerm</code> with <code>nTerm.getTypeOverlap() == typeOverlap</code>
     * 
     * @return
     */
    public HashMap<String, NTerm> getTypeOfTIS(String typeOverlap) {
        HashMap<String, NTerm> nTermElements = new HashMap<String, NTerm>();
        for (NTerm nTerm : getElements().values()) {
            if (nTerm.getTypeOverlap().equals(typeOverlap)) {
                nTermElements.put(nTerm.getName(), nTerm);
            }
        }
        return nTermElements;
    }

    /**
     * Get the lists of NTerm between beginDraw and endDraw
     * 
     * @param chromosome
     * @param beginDraw
     * @param endDraw
     * @return
     */
    public ArrayList<NTerm> getElementsToDisplay(int beginDraw, int endDraw, String nTermTypeOverlap) {
        ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
        NTerm nTerm = findPreviousNterm(beginDraw, "All");
        boolean found = false;
        while (!found) {
            if (nTermTypeOverlap.equals("All") && !nTerm.getTypeOverlap().equals("uncategorized")) {
                nTerms.add(nTerm);
            } else if (nTerm.getTypeOverlap().contains(nTermTypeOverlap)) {
                nTerms.add(nTerm);
            }
            nTerm = getNextNterm(nTerm, "All");
            if (nTerm == null) {
                found = true;
            } else if (nTerm.getBegin() > endDraw) {
                found = true;

            }

        }
        // for(NTerm nTerm : getElements().values()){
        // int begin = nTerm.getBegin();
        // int end = nTerm.getEnd();
        //
        // /*
        // * if the element has its end or its begin between beginDraw and endDraw, we
        // draw it
        // * Meaning either : beginDraw < begin < endDraw
        // * or : beginDraw < end < endDraw
        // * or : begin < beginDraw < endDraw < end
        // */
        // if((beginDraw<begin && begin<endDraw) || (beginDraw<end && end<endDraw) ||
        // (begin<beginDraw &&
        // endDraw<end) ){
        // nTerms.add(nTerm);
        // }
        // }
        return nTerms;
    }

    /**
     * Return the list of from a List of NTerm
     * 
     * @param nTerms
     * @return
     */
    public ArrayList<TIS> getTIStoDisplay(ArrayList<NTerm> nTerms) {
        ArrayList<TIS> tisList = new ArrayList<>();
        for (NTerm nTerm : nTerms) {
            TIS tis = this.getTisMap().get(nTerm);
            if (!tisList.contains(tis) && tis != null) {
                tisList.add(tis);
            }
        }
        return tisList;
    }

    /**
     * Reorder the Nterm to display AcD3 first, then Ace, and For.
     * 
     * @param nTermDisplay
     * @return
     */
    public ArrayList<NTerm> reorderModif(ArrayList<NTerm> nTermDisplay, NTerm nTermHighlight) {
        ArrayList<NTerm> nTerms = new ArrayList<NTerm>();
        for (NTerm nTerm : nTermDisplay) {
            if (nTerm.getTypeModif() == TypeModif.AcD3) {
                if (nTermHighlight != null && !nTerm.getName().equals(nTermHighlight.getName())) {
                    nTerms.add(nTerm);
                }
            }
        }
        for (NTerm nTerm : nTermDisplay) {
            if (nTerm.getTypeModif() == TypeModif.Ace) {
                if (nTermHighlight != null && !nTerm.getName().equals(nTermHighlight.getName())) {
                    nTerms.add(nTerm);
                }
            }
        }
        for (NTerm nTerm : nTermDisplay) {
            if (nTerm.getTypeModif() == TypeModif.For) {
                if (nTermHighlight != null && !nTerm.getName().equals(nTermHighlight.getName())) {
                    nTerms.add(nTerm);
                }
            }
        }
        /*
         * Add finally the NTerm highlighted
         */
        if (nTermHighlight != null) {
            nTerms.add(nTermHighlight);
        }

        return nTerms;
    }

    /**
     * Given a position in the genome, give the NTerm just upstream to it
     * 
     * @param bpPosition
     * @param typEOverlap type of NTerm to display
     * @return
     */
    public NTerm findSameNterm(int bpPosition, String typeOverlap) {
        String[][] annot = getAnnotation().getAnnotation();
        int k = -1;
        for (int i = 1; i < annot.length; i++) {
            int end = (int) (Double.parseDouble(annot[i][2]));
            if (end > bpPosition) {
                k = i;
                String nTermName = annot[k][0];
                NTerm nTerm = this.getElements().get(nTermName);
                // System.out.println(nTermName+" "+this.getElements().get(nTermName).getEnd()+"
                // "+bpPosition);
                if (typeOverlap.equals("All") && !nTerm.getTypeOverlap().equals("uncategorized")) {
                    return nTerm;
                } else if (nTerm.getTypeOverlap().contains(typeOverlap)) {
                    return nTerm;
                }
            }
        }
        String nTermName = annot[1][0];
        // System.out.println(nTermName+" "+this.getElements().get(nTermName).getEnd()+"
        // "+bpPosition);
        return this.getElements().get(nTermName);
    }

    /**
     * Given a NTerm, find the previous one in the Annotation
     * 
     * @param nTerm
     * @param typEOverlap type of NTerm to display
     * @return
     */
    public NTerm getPreviousNterm(NTerm nTerm, String typeOverlap) {
        String[][] annot = getAnnotation().getAnnotation();
        for (int i = 1; i < annot.length; i++) {
            if (annot[i][0].equals(nTerm.getName())) {
                int k = i;
                while (k > 1) {
                    k--;
                    String nTermName = annot[k][0];
                    NTerm nTermNew = this.getElements().get(nTermName);
                    if (typeOverlap.equals("All") && !nTermNew.getTypeOverlap().equals("uncategorized")) {
                        return nTermNew;
                    } else if (nTermNew.getTypeOverlap().contains(typeOverlap)) {
                        return nTermNew;
                    }
                }
                /*
                 * If no Nterm has been found we go in the opposite direction and the search the NEXT NTerm
                 */
                k = i;
                while (k < (annot.length - 1)) {
                    k++;
                    String nTermName = annot[k][0];
                    NTerm nTermNew = this.getElements().get(nTermName);
                    if (typeOverlap.equals("All") && !nTermNew.getTypeOverlap().equals("uncategorized")) {
                        return nTermNew;
                    } else if (nTermNew.getTypeOverlap().contains(typeOverlap)) {
                        return nTermNew;
                    }
                }
            }
        }
        return nTerm;
    }

    /**
     * Given a position in the genome, give the NTerm just upstream to it
     * 
     * @param bpPosition
     * @param typEOverlap type of NTerm to display
     * @return
     */
    public NTerm findPreviousNterm(int bpPosition, String typeOverlap) {
        String[][] annot = getAnnotation().getAnnotation();
        int k = -1;
        for (int i = 1; i < annot.length; i++) {
            int end = (int) (Double.parseDouble(annot[i][2]));
            if (end > bpPosition) {
                k = i - 1;
                if (k == 0)
                    k = 1;
                String nTermName = annot[k][0];
                NTerm nTerm = this.getElements().get(nTermName);
                // System.out.println(nTermName+" "+this.getElements().get(nTermName).getEnd()+"
                // "+bpPosition);
                if (typeOverlap.equals("All") && !nTerm.getTypeOverlap().equals("uncategorized")) {
                    return nTerm;
                } else if (nTerm.getTypeOverlap().contains(typeOverlap)) {
                    return nTerm;
                }
            }
        }
        String nTermName = annot[1][0];
        // System.out.println(nTermName+" "+this.getElements().get(nTermName).getEnd()+"
        // "+bpPosition);
        return this.getElements().get(nTermName);
    }

    /**
     * Given a position in the genome, give the NTerm just downstream to it
     * 
     * @param bpPosition
     * @param typEOverlap type of NTerm to display
     * @return
     */
    public NTerm findNextNterm(int bpPosition, String typeOverlap) {
        String[][] annot = getAnnotation().getAnnotation();
        int k = -1;
        for (int i = 1; i < annot.length; i++) {
            int begin = (int) (Double.parseDouble(annot[i][1]));
            if (begin > bpPosition) {
                k = i - 1;
                String nTermName = annot[k][0];
                NTerm nTerm = this.getElements().get(nTermName);
                // System.out.println(nTermName+" "+this.getElements().get(nTermName).getEnd()+"
                // "+bpPosition);
                if (typeOverlap.equals("All") && !nTerm.getTypeOverlap().equals("uncategorized")) {
                    return nTerm;
                } else if (nTerm.getTypeOverlap().contains(typeOverlap)) {
                    return nTerm;
                }
            }
        }
        String nTermName = annot[k][0];
        // System.out.println(nTermName+"
        // "+this.getElements().get(nTermName).getBegin()+" "+bpPosition);
        return this.getElements().get(nTermName);
    }

    /**
     * Given a NTerm, find the next one in the Annotation
     * 
     * @param nTerm
     * @param typEOverlap type of NTerm to display
     * @return
     */
    public NTerm getNextNterm(NTerm nTerm, String typeOverlap) {
        String[][] annot = getAnnotation().getAnnotation();
        for (int i = 1; i < annot.length; i++) {
            if (annot[i][0].equals(nTerm.getName())) {
                while (i < (annot.length - 1)) {
                    i++;
                    String nTermName = annot[i][0];
                    NTerm nTermNew = this.getElements().get(nTermName);
                    if (typeOverlap.equals("All") && !nTermNew.getTypeOverlap().equals("uncategorized")) {
                        return nTermNew;
                    } else if (nTermNew.getTypeOverlap().contains(typeOverlap)) {
                        return nTermNew;
                    }
                }
            }
        }
        return nTerm;
    }

    /**
     * Sysout some statistics about the number of aTIS, dTISM, dTIS, and uTIS
     */
    public void getStat() {
        int[] numberTypeOverlap = new int[this.getTypeOverlaps().size()];
        int i = 0;
        for (String typeOverlap : this.getTypeOverlaps()) {
            for (NTerm nTerm : this.getElements().values()) {
                String type = nTerm.getTypeOverlap();
                if (type.equals(typeOverlap)) {
                    numberTypeOverlap[i] = numberTypeOverlap[i] + 1;
                }
            }
            i++;
        }
        int NH2nb = 0;
        int ACD3Mnb = 0;
        int Acenb = 0;
        int Fornb = 0;
        for (NTerm nTerm : this.getElements().values()) {
            TypeModif type = nTerm.getTypeModif();
            if (type == TypeModif.NH2) {
                NH2nb++;
            } else if (type == TypeModif.AcD3) {
                ACD3Mnb++;
            } else if (type == TypeModif.Ace) {
                Acenb++;
            } else if (type == TypeModif.For) {
                Fornb++;
            }
        }
        String result = "NTerm: " + this.getElements().values().size();
        i = 0;
        for (String typeOverlap : this.getTypeOverlaps()) {
            result += " " + typeOverlap + ": " + numberTypeOverlap[i];
            i++;
        }
        System.out.println(result);
        System.out.println("NTerm: " + this.getElements().values().size() + " AcD3: " + ACD3Mnb + " For: " + Fornb
                + " Ace: " + Acenb + " NH2: " + NH2nb);
    }

    /*
     * ************************************
     * 
     * Load and save methods
     * 
     * ************************************
     */
    /**
     * Load NTermData from PATH_STREAMING+ this.getName() +EXTENSION
     */
    public void load() {
        NTermData nTermDataTemp = NTermData.load(this.getName()); // Load NTerm
        this.setAnnotation(nTermDataTemp.getAnnotation());
        this.setaTISMap(nTermDataTemp.getaTISMap());
        this.setElements(nTermDataTemp.getElements());
        this.setGeneModification(nTermDataTemp.getGeneModification());
        this.setGenes(nTermDataTemp.getGenes());
        this.setModifications(nTermDataTemp.getModifications());
        this.setPeptides(nTermDataTemp.getPeptides());
        this.setTisList(nTermDataTemp.getTisList());
        this.setTisMap(nTermDataTemp.getTisMap());
        this.setTypeOverlaps(nTermDataTemp.getTypeOverlaps());
    }

    /**
     * 
     * Load in: TranscriptomeData.PATH_STREAMING + nTermExpName + TranscriptomeData.EXTENSION
     * 
     * Read compressed, serialized data with a FileInputStream. Uncompress that data with a
     * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream. Replace current data
     * with new data, and redraw everything.
     */
    public static NTermData load(String nTermExpName) {
        try {
            // Create necessary input streams
            FileInputStream fis = new FileInputStream(PATH_STREAMING + nTermExpName + EXTENSION); // Read from file
            GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
            ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
            // Read in an object. It should be a vector of scribbles
            NTermData ntermexp = (NTermData) in.readObject();
            in.close();

            return ntermexp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } // Close the stream.

    }

    /**
     * 
     * Save in: TranscriptomeData.PATH_STREAMING + this.getName() + TranscriptomeData.EXTENSION
     * 
     * Serialize the vector of lines with an ObjectOutputStream. Compress the serialized objects with a
     * GZIPOutputStream. Write the compressed, serialized data to a file with a FileOutputStream. Don't
     * forget to flush and close the stream.
     */
    public void save() {
        try {
            // Create the necessary output streams to save the scribble.
            FileOutputStream fos = new FileOutputStream(PATH_STREAMING + this.getName() + EXTENSION);
            // Save to file
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            // Compressed
            ObjectOutputStream out = new ObjectOutputStream(gzos);
            // Save objects
            out.writeObject(this); // Write the entire Vector of scribbles
            out.flush(); // Always flush the output.
            out.close(); // And close the stream.
            System.out.println("MassSpec data saved: " + PATH_STREAMING + this.getName() + EXTENSION);
        }
        // Print out exceptions. We should really display them in a dialog...
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Link NTerm name to <code>NTerm</code><br>
     * This correspond to the list of <code>NTerm</code>
     */
    public HashMap<String, NTerm> getElements() {
        return elements;
    }

    /**
     * Link NTerm name to <code>NTerm</code><br>
     * This correspond to the list of <code>NTerm</code>
     */
    public void setElements(HashMap<String, NTerm> elements) {
        this.elements = elements;
    }

    /**
     * Link peptideSequence to <code>ArrayList(NTerm)</code><br>
     * This correspond to the list of <code>NTerm</code> having same peptideSequence
     */
    public HashMap<String, ArrayList<NTerm>> getPeptides() {
        return peptides;
    }

    /**
     * Link peptideSequence to <code>ArrayList(NTerm)</code><br>
     * This correspond to the list of <code>NTerm</code> having same peptideSequence
     */
    public void setPeptides(HashMap<String, ArrayList<NTerm>> peptides) {
        this.peptides = peptides;
    }

    public HashMap<String, ArrayList<NTerm>> getModifications() {
        return modifications;
    }

    public void setModifications(HashMap<String, ArrayList<NTerm>> modifications) {
        this.modifications = modifications;
    }

    public HashMap<String, ArrayList<NTerm>> getGenes() {
        return genes;
    }

    public void setGenes(HashMap<String, ArrayList<NTerm>> genes) {
        this.genes = genes;
    }

    public HashMap<String, ArrayList<TypeModif>> getGeneModification() {
        return geneModification;
    }

    public void setGeneModification(HashMap<String, ArrayList<TypeModif>> geneModification) {
        this.geneModification = geneModification;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public void setaTISMap(HashMap<String, ArrayList<NTerm>> aTISMap) {
        this.aTISMap = aTISMap;
    }

    public HashMap<String, ArrayList<NTerm>> getaTISMap() {
        return aTISMap;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public ArrayList<String> getTypeOverlaps() {
        return typeOverlaps;
    }

    public void setTypeOverlaps(ArrayList<String> typeOverlaps) {
        this.typeOverlaps = typeOverlaps;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public HashMap<NTerm, TIS> getTisMap() {
        return tisMap;
    }

    public void setTisMap(HashMap<NTerm, TIS> tisMap) {
        this.tisMap = tisMap;
    }

    public ArrayList<TIS> getTisList() {
        return tisList;
    }

    public void setTisList(ArrayList<TIS> tisList) {
        this.tisList = tisList;
    }

}
