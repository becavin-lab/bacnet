package bacnet.datamodel.proteomics;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.Strand;
import bacnet.datamodel.annotation.CodonUsage;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.proteomics.NTerm.MappingFrame;
import bacnet.datamodel.proteomics.NTerm.TypeModif;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Codon;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Sequence.SeqType;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import bacnet.utils.UNAfold;

/**
 * Transform excel table describing all N-Terminal peptides found in Listeria<br>
 * <li>Find the position of each peptide in Listeria genome
 * <li>create a NGS data from it
 * 
 * @author christophebecavin
 *
 */
public class NTermCreateData {

    public static String PATH_RAW_DATA = NTermUtils.getPATH() + "Raw Data" + File.separator;
    public static String PATH_RESULTS = NTermUtils.getPATH() + "Results" + File.separator;
    /**
     * List of column names when PATHARRYA contains only one experiment
     */
    public static String[] RAWARRAY_COLUMN =
            {"Peptide", "position", "modified_sequence", "# spectra", "max score", "max trh", "soluble", "insoluble"};
    /**
     * List of column names when PATHARRYA contains multiple experiments
     */
    public static String[] RAWARRAY_MULTICOLUMN =
            {"Peptide", "position", "modified_sequence", "# spectra", "max score", "max trh", "soluble", "insoluble",
                    "# spectra trypsin soluble 1", "# spectra trypsin insoluble 1", "# spectra trypsin soluble 2",
                    "# spectra trypsin insoluble 2", "# spectra gluC soluble 2", "# spectra GluC insoluble 2"};
    /**
     * List of column names for the final curated table of mapping
     */
    public static String[] CURATEDARRAY_CURATED = {"Name", "Modif Sequence", "Type", "Position", "Map found",
            "Map sequence", "Verified", "# spectra", "max score", "max trh", "soluble", "insoluble",
            "# spectra trypsin soluble 1", "# spectra trypsin insoluble 1", "# spectra trypsin soluble 2",
            "# spectra trypsin insoluble 2", "# spectra gluC soluble 2", "# spectra GluC insoluble 2"};

    private String nameRawData = "";
    private String nameNTermData = "";
    private Chromosome chromosome = Genome.loadEgdeGenome().getFirstChromosome();
    private String[][] nTermArray = new String[0][0];
    private String genomeAA = "";
    private String genomeAACompl = "";
    private String genomeAAMinus = "";
    private String genomeAAMinusCompl = "";
    private String genomeAAPlus = "";
    private String genomeAAPlusCompl = "";

    /**
     * Initialize a void NTermCreateData object and run <code>initializeGenomeSequence()</code> to have
     * the genome on the six frames
     */
    public NTermCreateData(Genome genome) {
        initializeGenomeSequence(genome);
    }

    public static void run(String nameRawData, String nameNTermData) {
        new NTermCreateData(nameRawData, nameNTermData);
    }

    /**
     * Create table summarizing the data<br>
     * Curate the table by adding different information<br>
     * Create streaming data for the viewer<br>
     * 
     * Run Mapping and creation of summary tables<br>
     * mapPeptide();<br>
     * curateTable();<br>
     * <br>
     * Create all NTerm from the table<br>
     * createNTermExperiment();<br>
     * NTermCreation.findStart(nameNTermData);<br>
     * NTermCreation.findTSS(nameNTermData);<br>
     * NTermCreation.findCodonUsage(nameNTermData);<br>
     * NTermCreation.findSDSequence(nameNTermData);<br>
     * <br>
     * Complete NTermExp by creating all HashMaps<br>
     * NTermData nTermExp = NTermData.load(nameNTermData);<br>
     * nTermExp.createHashMap();<br>
     * nTermExp.createGeneHashMap();<br>
     * nTermExp.createProteolyseMap();<br>
     * nTermExp.createAnnotation();<br>
     * nTermExp.save();<br>
     * 
     */
    public NTermCreateData(String nameRawData, String nameNTermData) {
        this.nameRawData = nameRawData;
        this.nameNTermData = nameNTermData;
        File file = new File(PATH_RESULTS + "Mapping\\");
        file.mkdir();
        modifyPathArray();
        /*
         * Test first if all peptide are unique in PATH_ARRAY
         */
        ArrayList<String> list = TabDelimitedTableReader.readList(nameRawData);
        TreeSet<String> set = new TreeSet<String>();
        int count = 0;
        for (String temp : list) {
            if (set.contains(temp)) {
                System.err.println("Duplicate: " + temp);
                count++;
            } else
                set.add(temp);
        }
        if (count == 0)
            System.err.println("No duplicate peptides in the RawDataTable");
        else
            System.err.println("Found " + count + " duplicate peptides in the RawDataTable");

        /*
         * Run Mapping and creation of summary tables
         */
        mapPeptide();
        System.out.println("Peptide mapped");
        curateTable();
        /*
         * Create all NTerm from the table
         */
        createNTermData();
        System.out.println("Add different information to NTermData");
        findStart(nameNTermData);
        findTSS(nameNTermData);
        findCodonUsage(nameNTermData);
        findSDSequence(nameNTermData);
        /*
         * Complete NTermData by creating all HashMaps
         */
        System.out.println("Creating HashMaps");
        NTermData nTermExp = NTermData.load(nameNTermData);
        nTermExp.createHashMap();
        nTermExp.createProteolyseMap();
        nTermExp.createAnnotation();
        nTermExp.save();

    }

    /**
     * Create a BioCondition containing only one Nterm
     * 
     * @param nTermName name of the NTerm data
     * @return
     */
    public static BioCondition createBioCondition(String nTermName) {
        BioCondition bioCond = new BioCondition(nTermName);
        bioCond.setGenomeName(Genome.EGDE_NAME);
        bioCond.setGenomeUsed(Genome.EGDE_NAME);
        NTermData massSpec = new NTermData();
        massSpec.setName(nTermName);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        massSpec.setDate(dateFormat.format(date));
        massSpec.setType(bacnet.datamodel.dataset.OmicsData.TypeData.NTerm);
        bioCond.getnTerms().add(massSpec);
        return bioCond;
    }

    /**
     * Search if the first amino acid or the amino acid before is a StartCodon
     * 
     * @param nameNTermData
     */
    public static void findStart(String nameNTermData) {
        System.out.println("Search Start codon");
        NTermData nTermExp = NTermData.load(nameNTermData);
        Chromosome chromosome = Genome.loadEgdeGenome().getFirstChromosome();

        /*
         * Search if the first peptide or the peptide before is a StartCodon
         */
        for (NTerm nTerm : nTermExp.getElements().values()) {
            String sequence = "";
            if (nTerm.isStrand())
                sequence = chromosome.getSequenceAsString(nTerm.getBegin() - 3, nTerm.getBegin() + 5, Strand.POSITIVE);
            else
                sequence = chromosome.getSequenceAsString(nTerm.getEnd() - 5, nTerm.getEnd() + 3, Strand.NEGATIVE);

            String firstcodon = sequence.substring(0, 3);
            String secondCodon = sequence.substring(3, 6);
            String thirdCodon = sequence.substring(6);

            String startCodon = "";
            if (Codon.isStart(firstcodon))
                startCodon += Codon.startCodon(firstcodon) + "-Start-1; ";
            if (Codon.isStart(secondCodon))
                startCodon += Codon.startCodon(secondCodon) + "-Start; ";
            if (Codon.isStart(thirdCodon))
                startCodon += Codon.startCodon(thirdCodon) + "-Start+1";
            nTerm.setStartCode(startCodon);
            nTerm.setPreviousCodon(firstcodon + "-" + Codon.startCodon(firstcodon));
            nTerm.setStartCodon(secondCodon + "-" + Codon.startCodon(secondCodon));
            nTerm.setNextCodon(thirdCodon + "-" + Codon.startCodon(thirdCodon));

        }
        nTermExp.save();

    }

    /**
     * Search for upstream and downstream TSS for each <code>NTerm</code><br>
     * It uses EGDe_Complete_TSS data for this search
     * 
     * @param nameNTermData
     */
    public static void findTSS(String nameNTermData) {
        System.out.println("Search TSS");
        NTermData nTermExp = NTermData.load(nameNTermData);

        /*
         * For each TIS positions search the closest TSS
         */
        BioCondition bioCond = BioCondition.getBioCondition("EGDe_Complete_TSS");
        NGS tssNGSPlus = bioCond.getNGSSeqs().get(0);
        NGS tssNGSMinus = bioCond.getNGSSeqs().get(1);
        // String[][] tssArray = TabDelimitedTableReader.read(ExpressionData.PATH_NGS_RAW +
        // "TSS_data_EGDe.txt");
        tssNGSPlus.read();
        tssNGSMinus.read();
        for (NTerm nTerm : nTermExp.getElements().values()) {
            /*
             * get sequence and search overlapping surrounding TSS in EGD-e
             */
            // if (nTerm.isStrand()) {
            // /*
            // * Search TSS upstream
            // */
            // int k = 0;
            // while (tssNGSPlus.getValues()[nTerm.getBegin() - k] == 0) {
            // // System.out.println(tssNGSPlus.getValues()[peptide.getBegin()-k]);
            // k++;
            // }
            // // found TSS at position nTerm.getBegin()-k
            // for (int i = 1; i < tssArray.length; i++) {
            // int positionTSS = Integer.parseInt(tssArray[i][0]);
            // // nTerm.setTssUptype((nTerm.getBegin()-k+1)+"");
            // if (positionTSS == (nTerm.getBegin() - k + 1))
            // nTerm.setTssUptype(tssArray[i][2] + "-" + (nTerm.getBegin() - k + 1));
            // }
            // nTerm.setTssUpDistance(k);
            // nTerm.setTssUpCoverage((int) Math.rint(Math.pow(2, tssNGSPlus.getValues()[nTerm.getBegin() -
            // k])));
            // /*
            // * Search TSS downstream
            // */
            // k = 0;
            // while ((nTerm.getBegin() + k) < tssNGSPlus.getLength()
            // && tssNGSPlus.getValues()[nTerm.getBegin() + k] == 0) {
            // // System.out.println(tssNGSPlus.getValues()[peptide.getBegin()-k]);
            // k++;
            // }
            // if ((nTerm.getBegin() + k) < tssNGSPlus.getLength()) {
            // // found TSS at position nTerm.getBegin()+k
            // for (int i = 1; i < tssArray.length; i++) {
            // int positionTSS = Integer.parseInt(tssArray[i][0]);
            // // nTerm.setTssDowntype((nTerm.getBegin()+k+1)+"");
            // if (positionTSS == (nTerm.getBegin() + k + 1))
            // nTerm.setTssDowntype(tssArray[i][2] + "-" + (nTerm.getBegin() + k + 1));
            // }
            // nTerm.setTssDownDistance(k);
            // nTerm.setTssDownCoverage(
            // (int) Math.rint(Math.pow(2, tssNGSPlus.getValues()[nTerm.getBegin() + k])));
            // }
            // } else {
            // /*
            // * Search TSS upstream
            // */
            // int k = 0;
            // while (tssNGSMinus.getValues()[nTerm.getEnd() + k] == 0) {
            // // System.out.println((peptide.getEnd()+k)+"
            // // "+tssNGSMinus.getValues()[peptide.getEnd()+k]);
            // k++;
            // }
            // for (int i = 1; i < tssArray.length; i++) {
            // int positionTSS = Integer.parseInt(tssArray[i][0]);
            // // nTerm.setTssUptype((nTerm.getEnd()+k+1)+"");
            // if (positionTSS == (nTerm.getEnd() + k + 1)) {
            // nTerm.setTssUptype(tssArray[i][2] + "-" + (nTerm.getBegin() + k + 1));
            // }
            // }
            // nTerm.setTssUpDistance(k);
            // nTerm.setTssUpCoverage((int) Math.rint(Math.pow(2, tssNGSMinus.getValues()[nTerm.getEnd() +
            // k])));
            // /*
            // * Search TSS downstream
            // */
            // k = 0;
            // while ((nTerm.getEnd() - k) >= 0 && tssNGSMinus.getValues()[nTerm.getEnd() - k] == 0) {
            // // System.out.println(tssNGSPlus.getValues()[peptide.getBegin()-k]);
            // k++;
            // // System.out.println((nTerm.getEnd()-k)+" : "+tssNGSMinus.getValues().length);
            // }
            // for (int i = 1; i < tssArray.length; i++) {
            // int positionTSS = Integer.parseInt(tssArray[i][0]);
            // // nTerm.setTssDowntype((nTerm.getEnd()-k+1)+"");
            // if (positionTSS == (nTerm.getEnd() - k + 1))
            // nTerm.setTssDowntype(tssArray[i][2] + "-" + (nTerm.getEnd() - k + 1));
            // }
            // if ((nTerm.getEnd() - k) >= 0) {
            // nTerm.setTssDownDistance(k);
            // nTerm.setTssDownCoverage((int) Math.rint(Math.pow(2, tssNGSMinus.getValues()[nTerm.getEnd() -
            // k])));
            // }
            // }
            // System.out.println(nTerm.getName());
        }
        nTermExp.save();
    }

    /**
     * Get Codon usage of the peptide
     * 
     * @param nameNTermData
     */
    public static void findCodonUsage(String nameNTermData) {
        System.out.println("Search Codon Usage");
        NTermData nTermExp = NTermData.load(nameNTermData);

        /*
         * Calculate codon Usage
         */
        CodonUsage codonUsage = new CodonUsage("EGD-e");
        for (NTerm nTerm : nTermExp.getElements().values()) {
            nTerm.setCodonUsage(codonUsage.calculate(nTerm.getSequence()));
            // System.out.println(nTerm.getCodonUsage());
        }

        nTermExp.save();
    }

    /**
     * For each <code>NTerm</code> calculates anti Shine-Dalgarno sequence affinity in the region -20bp
     * before peptide on the genome
     * 
     * @param nameNTermData
     */
    public static void findSDSequence(String nameNTermData) {
        System.out.println("Search Shine-Dalgarno energy");
        NTermData nTermExp = NTermData.load(nameNTermData);

        /*
         * Calculate anti-sd sequence binding freeEnergy
         */
        int i = 0;
        for (NTerm nTerm : nTermExp.getElements().values()) {
            double energy =
                    UNAfold.hybridRNA(nTerm.getSDSequence(), nTerm.getName(), Sequence.ANTI_SD_SEQ, "anti-SD", false);
            nTerm.setAntiSDBindingFreeEnergy(energy);
            if (i % 500 == 0)
                System.out.println("peptide: " + i + " - " + nTerm.getName() + "\t" + energy);
            i++;
        }

        nTermExp.save();
    }

    /**
     * Add MaxQuant information for each peptide <br>
     * This function is used only when multiple files are available
     * 
     * @param NTermData
     * @param fileName
     * @param multipleFiles
     */
    public static void addMaXQuantInfo(NTermData nTermData, String fileName, boolean multipleFiles) {
        String finalFileName = fileName + "_trypsin_soluble_1.txt";
        addMaXQuantInfo(nTermData, finalFileName, 0);
        finalFileName = fileName + "_trypsin_insoluble_1.txt";
        addMaXQuantInfo(nTermData, finalFileName, 1);
        finalFileName = fileName + "_trypsin_soluble_2.txt";
        addMaXQuantInfo(nTermData, finalFileName, 2);
        finalFileName = fileName + "_trypsin_insoluble_2.txt";
        addMaXQuantInfo(nTermData, finalFileName, 3);
        finalFileName = fileName + "_GluC_soluble_2.txt";
        addMaXQuantInfo(nTermData, finalFileName, 4);
        finalFileName = fileName + "_GluC_insoluble_2.txt";
        addMaXQuantInfo(nTermData, finalFileName, 5);

    }

    public static void addMaXQuantInfo(NTermData NTermData, String fileName, int indexTable) {
        System.out.println("Load MaxQuant data");
        String[][] maxQuantTable = TabDelimitedTableReader.read(fileName);
        for (int i = 1; i < maxQuantTable.length; i++) {
            String modification = maxQuantTable[i][1];
            if (modification.equals("Acetyl (N-term)")) {
                maxQuantTable[i][1] = TypeModif.Ace + "";
            } else if (modification.equals("AcetylD3 (N-term)")) {
                maxQuantTable[i][1] = TypeModif.AcD3 + "";
            } else if (modification.equals("Formyl (N-term)")) {
                maxQuantTable[i][1] = TypeModif.For + "";
            } else if (modification.equals("Gln->pyro-Glu")) {
                maxQuantTable[i][1] = TypeModif.PyroGlu + "";
            } else {
                maxQuantTable[i][1] = TypeModif.NH2 + "";
            }
        }
        System.out.println("Add MaxQuant info table: " + fileName);
        // ArrayList<String> results = new ArrayList<>();
        // results.add("Name\tSequence\tMaxQuant\tModif\tGroup\tOverlap");
        for (NTerm nTerm : NTermData.getNTerms()) {
            for (int i = 1; i < maxQuantTable.length; i++) {
                String sequenceMaxQuant = maxQuantTable[i][0];
                TypeModif modifMaxQuant = TypeModif.valueOf(maxQuantTable[i][1]);
                if (nTerm.getSequencePeptide().equals(sequenceMaxQuant)) {
                    if (nTerm.getTypeModif() == modifMaxQuant) {
                        nTerm.setMaxQuant("FOUND");
                        nTerm.getMaxQuantum()[indexTable] = "FOUND";
                    }
                }
            }
            // results.add(nTerm.getName()+"\t"+nTerm.getSequencePeptide()+"\t"+nTerm.getMaxQuant()+"\t"+nTerm.getTypeModif()+"\t"+nTerm.getTypeOverlap()+"\t"+nTerm.getOverlap());
        }
        // TabDelimitedTableReader.saveList(results,
        // PATH_RESULTS+NTermData.getName()+"_MaxQuant_Summary.txt");
        NTermData.save();
        System.out.println("MaxQuant info succesfully added");
    }

    /**
     * Compare to peptides from MaxQuant software analysis<br>
     * For each NTerm search if the same peptide and the same modif has been found in MaxQuant analysis.
     * 
     * @param fileName
     */
    public static void addMaXQuantInfo(NTermData NTermData, String fileName) {
        System.out.println("Load MaxQuant data");
        String[][] maxQuantTable = TabDelimitedTableReader.read(fileName);
        for (int i = 1; i < maxQuantTable.length; i++) {
            String modification = maxQuantTable[i][1];
            if (modification.equals("Acetyl (N-term)")) {
                maxQuantTable[i][1] = TypeModif.Ace + "";
            } else if (modification.equals("AcetylD3 (N-term)")) {
                maxQuantTable[i][1] = TypeModif.AcD3 + "";
            } else if (modification.equals("Formyl (N-term)")) {
                maxQuantTable[i][1] = TypeModif.For + "";
            } else if (modification.equals("Gln->pyro-Glu")) {
                maxQuantTable[i][1] = TypeModif.PyroGlu + "";
            } else {
                maxQuantTable[i][1] = TypeModif.NH2 + "";
            }
        }
        System.out.println("Add MaxQuant info table");
        ArrayList<String> results = new ArrayList<>();
        results.add("Name\tSequence\tMaxQuant\tModif\tGroup\tOverlap");
        for (NTerm nTerm : NTermData.getNTerms()) {
            for (int i = 1; i < maxQuantTable.length; i++) {
                String sequenceMaxQuant = maxQuantTable[i][0];
                TypeModif modifMaxQuant = TypeModif.valueOf(maxQuantTable[i][1]);
                if (nTerm.getSequencePeptide().equals(sequenceMaxQuant)) {
                    if (nTerm.getTypeModif() == modifMaxQuant) {
                        nTerm.setMaxQuant("FOUND");
                    }
                }
            }
            results.add(nTerm.getName() + "\t" + nTerm.getSequencePeptide() + "\t" + nTerm.getMaxQuant() + "\t"
                    + nTerm.getTypeModif() + "\t" + nTerm.getTypeOverlap() + "\t" + nTerm.getOverlap());
        }
        TabDelimitedTableReader.saveList(results, PATH_RESULTS + NTermData.getName() + "_MaxQuant_Summary.txt");
        NTermData.save();
        System.out.println("MaxQuant info succesfully added");
    }

    /**
     * On each of the 6 frame genome, map the different peptide
     */
    private void mapPeptide() {

        initializeGenomeSequence(Genome.loadEgdeGenome());
        nTermArray = TabDelimitedTableReader.read(nameRawData);
        ArrayList<String> notMappeds = new ArrayList<String>();
        int countNotMapped = 0;
        int countM = 0;
        int countLV = 0;
        int countAmbiguous = 0;
        int countNotM = 0;
        int duplicates = 0;
        System.out.println("Mapping peptides");
        for (int i = 1; i < nTermArray.length; i++) {
            if (i % 500 == 0)
                System.out.println("peptide: " + i);
            String sequence = nTermArray[i][ArrayUtils.findColumn(nTermArray, "modified_sequence")].split("-")[1];
            // System.out.println("seq: "+sequence);
            /*
             * We map all the sequences We search for the PERFECT match using FileUtils.searchPosition(sequence,
             * genomeAA);
             */
            ArrayList<Sequence> sequences = mapSequence(sequence);
            int sequencesSize = sequences.size();

            // count number of mapped peptide starting with M
            if (sequencesSize != 0) {
                if (sequence.charAt(0) == 'M') {
                    countM++;
                } else {
                    countNotM++;
                }
            }

            /*
             * There is an ambiguity when the peptide start with a M It can be a L->M, I->M or V->M We remove
             * this ambiguity by mapping peptides without the M and searching for not AUG start codon just
             * before
             */
            if (sequence.charAt(0) == 'M') {
                sequence = sequence.substring(1);
                /*
                 * Remap but without the M this time
                 */
                ArrayList<Sequence> sequencesTemp = mapSequence(sequence);
                boolean foundLV = false;
                for (Sequence seq : sequencesTemp) {
                    /*
                     * Get map sequence + the first codon (depending of the strand we do -3bp or +3bp)
                     */
                    Sequence realSeq = new Sequence(seq.getName(), seq.getBegin() - 3, seq.getEnd(), seq.getStrand());
                    if (!seq.isStrand())
                        realSeq = new Sequence(seq.getName(), seq.getBegin(), seq.getEnd() + 3, seq.getStrand());
                    realSeq.getFeatures().put("frame", seq.getFeature("frame"));
                    String codon = Codon.getFirstCodon(realSeq);
                    /*
                     * Get only map sequence where first codon before is a TTG (L) or GTG (V)
                     */
                    if (Codon.isStart(codon) && !Codon.startCodon(codon).equals("M")) {
                        sequences.add(realSeq);
                        foundLV = true;
                    }
                }
                /*
                 * If not AUG start codon were found
                 */
                if (foundLV) {
                    if (sequencesSize == 0) {
                        /*
                         * The sequence was not mapped before as sequencesSize==0 But its mapped now as foundLV = true
                         * 
                         * So the sequence is now mapped with a non AUG start codon It's counted as: Non AUG start codon
                         */
                        countLV++;
                    } else {
                        /*
                         * The sequence map with a M and with a non AUG start codon It's counted as: Ambiguous
                         */
                        countAmbiguous++;
                    }
                }
            }

            /*
             * Count peptide still not mapped
             */
            if (sequences.size() == 0) {
                countNotMapped++;
                String notMapped = nTermArray[i][ArrayUtils.findColumn(nTermArray, "Peptide")] + "\t"
                        + nTermArray[i][ArrayUtils.findColumn(nTermArray, "modified_sequence")] + "\t"
                        + nTermArray[i][ArrayUtils.findColumn(nTermArray, "modified_sequence")].split("-")[1];
                notMappeds.add(notMapped);
            }

            /*
             * Create a String of all mapping positions
             */
            String posString = "";
            for (Sequence sequenceTemp : sequences) {
                String newSequence = sequenceTemp.getBegin() + "," + sequenceTemp.getEnd() + ","
                        + sequenceTemp.getStrand() + "," + sequenceTemp.getFeature("frame") + ";";
                if (!posString.contains(newSequence))
                    posString += newSequence;
                if (!sequenceTemp.getFeatures().containsKey("frame"))
                    System.err.println("No frame info " + sequenceTemp.getName() + " " + posString);
            }

            /*
             * Find if there was duplicate mapping
             */
            if (posString.split(";").length > 1) {
                duplicates++;
                // System.out.println(posString.split(";").length);
            }
            nTermArray[i][1] = posString;
            // System.out.println(posString);

        }
        TabDelimitedTableReader.save(nTermArray,
                PATH_RESULTS + "Mapping\\" + FileUtils.removeExtensionAndPath(nameRawData) + "_Mapped.txt");
        TabDelimitedTableReader.saveList(notMappeds,
                PATH_RAW_DATA + "NotMapped_" + FileUtils.removeExtensionAndPath(nameRawData) + ".txt");

        /*
         * Create summary file for mapping
         */
        Date date = new Date();
        String ret = "SummaryMapping " + date.toString() + "\tnumber\n";
        ret += "Total peptide\t" + (nTermArray.length - 1) + "\n";
        ret += "Not mapped\t" + countNotMapped + "\n";
        ret += "Total mapped\t" + ((nTermArray.length - 1) - countNotMapped) + "\n";
        ret += "Peptide mapped in duplicate\t" + duplicates + "\n";
        ret += "Mapped without M\t" + countNotM + "\n";
        ret += "Mapped with M\t" + countM + "\n";
        ret += "    subset Mapped with M and non AUG start codon\t" + countAmbiguous + "\n";
        ret += "Mapped with a non AUG start codon\t" + countLV + "\n";
        System.out.println(ret);
        FileUtils.saveText(ret, PATH_RESULTS + "Mapping\\" + "SummaryMapping_"
                + FileUtils.removeExtensionAndPath(nameRawData) + ".txt");

    }

    /**
     * Map an amino acide sequence to 6 frame Genomes<br>
     * From initializeGenomeSequence() we get all 6 possible amino acids sequence of the genome, and map
     * the peptide<br>
     * The different mapping coordinates are transformed into nucleotide coordinates (= genome
     * coordinates)
     * 
     * @param sequence amino acid sequence to map
     */
    private ArrayList<Sequence> mapSequence(String sequence) {
        ArrayList<Sequence> sequences = new ArrayList<Sequence>();
        int k = 0;
        ArrayList<Integer> positions = FileUtils.searchPosition(sequence, genomeAA);
        for (int position : positions) {
            int bpPosition = (position) * 3 + 1;
            k++;
            Sequence seq = new Sequence(k + "", bpPosition, (bpPosition + (sequence.length() * 3)) - 1, '+');
            seq.getFeatures().put("frame", MappingFrame.plus + "");
            sequences.add(seq);
        }
        positions = FileUtils.searchPosition(sequence, genomeAACompl);
        for (int position : positions) {
            int bpPosition = chromosome.getLength() - position * 3;
            k++;
            Sequence seq = new Sequence(k + "", bpPosition, (bpPosition - (sequence.length() * 3)) + 1, '-');
            seq.getFeatures().put("frame", MappingFrame.minus + "");
            sequences.add(seq);
        }
        positions = FileUtils.searchPosition(sequence, genomeAAMinus);
        for (int position : positions) {
            int bpPosition = ((position) * 3 + 1) - 1;
            k++;
            Sequence seq = new Sequence(k + "", bpPosition, (bpPosition + (sequence.length() * 3)) - 1, '+');
            seq.getFeatures().put("frame", MappingFrame.plusM + "");
            sequences.add(seq);
        }
        positions = FileUtils.searchPosition(sequence, genomeAAMinusCompl);
        for (int position : positions) {
            int bpPosition = (chromosome.getLength() - position * 3) - 1;
            k++;
            Sequence seq = new Sequence(k + "", bpPosition, (bpPosition - (sequence.length() * 3)) + 1, '-');
            seq.getFeatures().put("frame", MappingFrame.minusM + "");
            sequences.add(seq);
        }
        positions = FileUtils.searchPosition(sequence, genomeAAPlus);
        for (int position : positions) {
            int bpPosition = (position * 3 + 1) + 1;
            k++;
            Sequence seq = new Sequence(k + "", bpPosition, (bpPosition + (sequence.length() * 3)) - 1, '+');
            seq.getFeatures().put("frame", MappingFrame.plusP + "");
            sequences.add(seq);
        }
        positions = FileUtils.searchPosition(sequence, genomeAAPlusCompl);
        for (int position : positions) {
            int bpPosition = (chromosome.getLength() - position * 3) + 1;
            k++;
            Sequence seq = new Sequence(k + "", bpPosition, (bpPosition - (sequence.length() * 3)) + 1, '-');
            seq.getFeatures().put("frame", MappingFrame.minusP + "");
            sequences.add(seq);
        }
        return sequences;
    }

    /**
     * Modify result table, and verify if map peptides are well mapped<br>
     * For each peptide we extract position found, get the AA sequence from it, and check if it is the
     * same than peptide sequence
     */
    private void curateTable() {
        nTermArray = TabDelimitedTableReader
                .read(PATH_RESULTS + "/Mapping/" + FileUtils.removeExtensionAndPath(nameRawData) + "_Mapped.txt");
        String[][] finalArray = new String[nTermArray.length][CURATEDARRAY_CURATED.length];
        for (int j = 0; j < CURATEDARRAY_CURATED.length; j++) {
            finalArray[0][j] = CURATEDARRAY_CURATED[j];
        }

        // PATHARRAY_COLUMN = {"Peptide","position","modified_sequence","# spectra","max
        // score","max
        // trh","soluble","insoluble"};
        //
        // ArrayUtils.findColumn(nTermArray, "Peptide")

        for (int i = 1; i < nTermArray.length; i++) {
            finalArray[i][ArrayUtils.findColumn(finalArray, "Name")] =
                    nTermArray[i][ArrayUtils.findColumn(nTermArray, "Peptide")];
            String modifSeq = nTermArray[i][ArrayUtils.findColumn(nTermArray, "modified_sequence")];
            finalArray[i][ArrayUtils.findColumn(finalArray, "Modif Sequence")] = modifSeq;
            String type = nTermArray[i][ArrayUtils.findColumn(nTermArray, "modified_sequence")].split("-")[0]; // type
                                                                                                               // of
                                                                                                               // modif
            finalArray[i][ArrayUtils.findColumn(finalArray, "Type")] = type;
            String positions = nTermArray[i][ArrayUtils.findColumn(nTermArray, "position")];
            finalArray[i][ArrayUtils.findColumn(finalArray, "Position")] = positions;
            finalArray[i][ArrayUtils.findColumn(finalArray, "Map found")] = positions.split(";").length + "";
            finalArray[i][ArrayUtils.findColumn(finalArray, "Map sequence")] = "";
            finalArray[i][ArrayUtils.findColumn(finalArray, "Verified")] = "";
            finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra")] =
                    nTermArray[i][ArrayUtils.findColumn(nTermArray, "# spectra")];
            finalArray[i][ArrayUtils.findColumn(finalArray, "max score")] =
                    nTermArray[i][ArrayUtils.findColumn(nTermArray, "max score")];
            finalArray[i][ArrayUtils.findColumn(finalArray, "max trh")] =
                    nTermArray[i][ArrayUtils.findColumn(nTermArray, "max trh")];
            finalArray[i][ArrayUtils.findColumn(finalArray, "soluble")] =
                    nTermArray[i][ArrayUtils.findColumn(nTermArray, "soluble")];
            finalArray[i][ArrayUtils.findColumn(finalArray, "insoluble")] =
                    nTermArray[i][ArrayUtils.findColumn(nTermArray, "insoluble")];

            if (ArrayUtils.findColumn(nTermArray, "# spectra trypsin soluble 1") == -1) { // if only one experiment is
                                                                                          // contain in the Raw data
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra trypsin soluble 1")] = "0";
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra trypsin insoluble 1")] = "0";
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra trypsin soluble 2")] = "0";
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra trypsin insoluble 2")] = "0";
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra gluC soluble 2")] = "0";
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra GluC insoluble 2")] = "0";
            } else { // multiple experiments contained in this data
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra trypsin soluble 1")] =
                        nTermArray[i][ArrayUtils.findColumn(nTermArray, "# spectra trypsin soluble 1")];
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra trypsin insoluble 1")] =
                        nTermArray[i][ArrayUtils.findColumn(nTermArray, "# spectra trypsin insoluble 1")];
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra trypsin soluble 2")] =
                        nTermArray[i][ArrayUtils.findColumn(nTermArray, "# spectra trypsin soluble 2")];
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra trypsin insoluble 2")] =
                        nTermArray[i][ArrayUtils.findColumn(nTermArray, "# spectra trypsin insoluble 2")];
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra gluC soluble 2")] =
                        nTermArray[i][ArrayUtils.findColumn(nTermArray, "# spectra gluC soluble 2")];
                finalArray[i][ArrayUtils.findColumn(finalArray, "# spectra GluC insoluble 2")] =
                        nTermArray[i][ArrayUtils.findColumn(nTermArray, "# spectra GluC insoluble 2")];
            }

            /*
             * Get sequences directly from the genome to verify the mapping
             */
            String sequencesAA = "";
            String verified = "";
            for (String position : positions.split(";")) {
                if (!position.equals("")) {
                    Sequence seq = new Sequence(nTermArray[i][0], Integer.parseInt(position.split(",")[0]),
                            Integer.parseInt(position.split(",")[1]), position.split(",")[2].charAt(0));
                    Strand strand = Strand.POSITIVE;
                    if (!seq.isStrand())
                        strand = Strand.NEGATIVE;
                    String sequenceBP = chromosome.getSequenceAsString(seq.getBegin(), seq.getEnd(), strand);
                    DNASequence seqDNA = new DNASequence(sequenceBP);
                    String sequenceAA = seqDNA.getRNASequence().getProteinSequence().getSequenceAsString();
                    sequencesAA += sequenceAA + ";";
                    String modifSequenceAA = type + "-" + sequenceAA + "-COOH";
                    if (modifSequenceAA.equals(modifSeq)) {
                        verified += "YES;";
                    } else
                        verified += "NO;";
                }
            }
            finalArray[i][ArrayUtils.findColumn(finalArray, "Map sequence")] = sequencesAA;
            finalArray[i][ArrayUtils.findColumn(finalArray, "Verified")] = verified;
        }
        TabDelimitedTableReader.save(finalArray,
                PATH_RESULTS + "/Mapping/" + FileUtils.removeExtensionAndPath(nameRawData) + "_Curated.txt");
    }

    /**
     * Create an <code>NTermExperiment</code> from PATH_ARRAY+"_Curated.txt"
     */
    private void createNTermData() {
        System.out.println("Verify and create NTermData");
        chromosome = Genome.loadEgdeGenome().getFirstChromosome();
        NTermData NTermData = new NTermData(nameNTermData);
        NTermData.setBioCondName(nameNTermData);
        // {"Name","Modif Sequence","Type","Position","Map found","Map
        // sequence","Verified","# spectra","max score","max
        // trh","soluble","insoluble","# spectra trypsin soluble 1","# spectra trypsin
        // insoluble 1","# spectra trypsin soluble 2","# spectra trypsin insoluble 2","#
        // spectra gluC soluble 2","# spectra GluC insoluble 2"};

        /*
         * Fill values
         */
        String[][] peptideArray = TabDelimitedTableReader
                .read(PATH_RESULTS + "/Mapping/" + FileUtils.removeExtensionAndPath(nameRawData) + "_Curated.txt");
        for (int i = 1; i < peptideArray.length; i++) {
            String[] positions = peptideArray[i][ArrayUtils.findColumn(peptideArray, "Position")].split(";");
            String type = peptideArray[i][ArrayUtils.findColumn(peptideArray, "Type")];
            String name = peptideArray[i][ArrayUtils.findColumn(peptideArray, "Name")];
            String sequenceModif = peptideArray[i][ArrayUtils.findColumn(peptideArray, "Modif Sequence")];
            String sequence = sequenceModif.split("-")[1];
            int spectra = Integer.parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "# spectra")]);
            int score = Integer.parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "max score")]);
            int threshold = Integer.parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "max trh")]);
            /*
             * Put all spectrum in memory
             */
            int[] spectrum = new int[6];
            spectrum[0] = Integer
                    .parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "# spectra trypsin soluble 1")]);
            spectrum[1] = Integer
                    .parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "# spectra trypsin insoluble 1")]);
            spectrum[2] = Integer
                    .parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "# spectra trypsin soluble 2")]);
            spectrum[3] = Integer
                    .parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "# spectra trypsin insoluble 2")]);
            spectrum[4] =
                    Integer.parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "# spectra gluC soluble 2")]);
            spectrum[5] = Integer
                    .parseInt(peptideArray[i][ArrayUtils.findColumn(peptideArray, "# spectra GluC insoluble 2")]);

            boolean soluble = false;
            if (peptideArray[i][ArrayUtils.findColumn(peptideArray, "soluble")].equals("TRUE"))
                soluble = true;
            boolean insoluble = false;
            if (peptideArray[i][ArrayUtils.findColumn(peptideArray, "insoluble")].equals("TRUE"))
                insoluble = true;
            int k = 1;
            for (String position : positions) {
                String[] temp = position.split(",");
                // System.out.println(position+" "+temp.length);
                if (temp.length > 1) {
                    char strand = '+';
                    if (temp[2].equals("-"))
                        strand = '-';
                    int begin = Integer.parseInt(temp[0]);
                    int end = Integer.parseInt(temp[1]);
                    String peptideName = name;
                    if (positions.length > 1) {
                        peptideName = name + "_" + k;
                    }
                    NTerm nTerm = new NTerm(peptideName, begin, end, strand);
                    nTerm.setTypeModif(TypeModif.valueOf(type));
                    nTerm.setType(SeqType.unknown);
                    nTerm.setSoluble(soluble);
                    nTerm.setInsoluble(insoluble);
                    nTerm.setSpectra(spectra);
                    nTerm.setSpectrum(spectrum);
                    nTerm.setScore(score);
                    nTerm.setThreshold(threshold);
                    nTerm.setModifSequence(sequenceModif);
                    nTerm.setSequencePeptide(sequence);
                    nTerm.setSequenceMap(
                            peptideArray[i][ArrayUtils.findColumn(peptideArray, "Map sequence")].split(";")[k - 1]);
                    nTerm.setMappingFrame(MappingFrame.valueOf(temp[3]));
                    // Add duplicate mapped sequence here
                    ArrayList<String> duplicates = new ArrayList<>();
                    if (positions.length > 1) {
                        for (int j = 0; j < positions.length; j++) {
                            String duplicate = name + "_" + (j + 1);
                            if (!duplicate.equals(peptideName))
                                duplicates.add(duplicate);
                        }
                        // System.out.println();
                    }
                    nTerm.setDuplicates(duplicates);
                    NTermData.addElement(nTerm);

                    // System.out.println(("length: "+nTerm.getLength()+" "+nTerm.getLength()%3));
                    // System.out.println(("Seq: "+nTerm.getSequenceAA()+"
                    // "+nTerm.getSequencePeptide()));
                }
                k++;
            }
        }

        NTermData.save();
        System.out.println("Saved: " + NTermData.getElements().size() + " peptides");
    }

    /**
     * Get all 6 possible amino acids sequence of the genome<br>
     * <br>
     * Initialize:<br>
     * <li>genomeAA - In frame sequence
     * <li>genomeAACompl - In frame sequence complementary
     * <li>genomeAAMinus - Minus -1 bp sequence
     * <li>genomeAAMinusCompl - Minus -1 bp sequence complementary
     * <li>genomeAAPlus - Plus 1 bp sequence
     * <li>genomeAAPlusCompl - Plus 1 bp sequence complementary <br>
     * <br>
     * Can save these genomes in NTermUtils.getPATH()+"Genome aminoacid mapping/"
     * 
     * @param genome to read
     */
    private void initializeGenomeSequence(Genome genome) {
        /*
         * In frame sequences
         */
        this.setChromosome(genome.getFirstChromosome());
        genomeAA = chromosome.getRNASequence().getProteinSequence().getSequenceAsString();
        String seqComplement = chromosome.getReverseComplement().getSequenceAsString();
        DNASequence chromoComplement = new DNASequence(seqComplement);
        genomeAACompl = chromoComplement.getRNASequence().getProteinSequence().getSequenceAsString();

        /*
         * Minus -1 bp sequences
         */
        String seqCromosome = chromosome.getSequenceAsString();
        String seqMinus =
                seqCromosome.charAt(seqCromosome.length() - 1) + seqCromosome.substring(0, seqCromosome.length() - 1);
        DNASequence seq = new DNASequence(seqMinus);
        genomeAAMinus = seq.getRNASequence().getProteinSequence().getSequenceAsString();
        seqComplement = seq.getReverseComplement().getSequenceAsString();
        seq = new DNASequence(seqComplement);
        genomeAAMinusCompl = seq.getRNASequence().getProteinSequence().getSequenceAsString();

        /*
         * Plus 1 bp sequences
         */
        String seqPlus = seqCromosome.substring(1, seqCromosome.length()) + seqCromosome.charAt(0);
        seq = new DNASequence(seqPlus);
        genomeAAPlus = seq.getRNASequence().getProteinSequence().getSequenceAsString();
        seqComplement = seq.getReverseComplement().getSequenceAsString();
        seq = new DNASequence(seqComplement);
        genomeAAPlusCompl = seq.getRNASequence().getProteinSequence().getSequenceAsString();
        System.out.println("Genome created for mapping: " + genomeAA.length() + " " + genomeAACompl.length() + " "
                + genomeAAPlusCompl.length() + " " + genomeAAMinus.length() + " " + genomeAAMinusCompl.length() + " "
                + genomeAAPlus.length());

        // bacnet.core.utils.data.FileUtils.saveText(genomeAA,
        // NTermUtils.getPATH()+"Genome aminoacid
        // mapping/"+"GenomeAA.txt");
        // bacnet.core.utils.data.FileUtils.saveText(genomeAACompl,
        // NTermUtils.getPATH()+"Genome aminoacid
        // mapping/"+"GenomeAACompl.txt");
        // bacnet.core.utils.data.FileUtils.saveText(genomeAAMinus,
        // NTermUtils.getPATH()+"Genome aminoacid
        // mapping/"+"GenomeAAMinus.txt");
        // bacnet.core.utils.data.FileUtils.saveText(genomeAAMinusCompl,
        // NTermUtils.getPATH()+"Genome
        // aminoacid mapping/"+"GenomeAAMinusCompl.txt");
        // bacnet.core.utils.data.FileUtils.saveText(genomeAAPlus,
        // NTermUtils.getPATH()+"Genome aminoacid
        // mapping/"+"GenomeAAPlus.txt");
        // bacnet.core.utils.data.FileUtils.saveText(genomeAAPlusCompl,
        // NTermUtils.getPATH()+"Genome
        // aminoacid mapping/"+"GenomeAAPlusCompl.txt");

    }

    /**
     * Check PATHARRAY and had columns if needed<br>
     * <br>
     * 
     * Two types of array are possible:
     * <li>one with only one experiment (contains only one column with "# spectra")
     * <li>one with only multiple experiments (contains many columns containing "# spectra") <br>
     * <br>
     * For The first type:<br>
     * Check if PATHARRAY table is complete = has 8 columns, first column is the list of peptide,
     * soluble and insoluble contains "TRUE" and "FALSE" and no "yes"<br>
     * <li>Change "TRUE" and "FALSE" to "yes" and "no"
     * <li>Remove line with NH2-free peptide <br>
     * <br>
     * For the second type:<br>
     * 
     * 
     */
    public void modifyPathArray() {
        ArrayList<String> list = new ArrayList<String>();
        String[][] array = TabDelimitedTableReader.read(nameRawData);
        int index = ArrayUtils.findColumn(array, "# spectra trypsin soluble 1");
        if (index == -1) { // only one experiment in the array
            System.out.println("Found only one spectrum");
            if (array[0].length != RAWARRAY_COLUMN.length) {
                String header = "Peptide\tposition\t";
                for (int j = 0; j < array[0].length; j++) {
                    header += array[0][j] + "\t";
                }
                list.add(header);
                for (int i = 1; i < array.length; i++) {
                    if (!array[i][0].contains("NH2-")) {
                        String row = "peptide-" + i + "\t\t";
                        for (int j = 0; j < array[0].length; j++) {
                            String value = array[i][j];
                            if (value.equals("yes"))
                                value = "TRUE";
                            if (value.equals(""))
                                value = "FALSE";
                            row += value + "\t";
                        }
                        list.add(row);
                    }
                }
                TabDelimitedTableReader.saveList(list, nameRawData);
            }
        } else { // multiple experiments in the array
                 // {"Peptide","position","modified_sequence","# spectra","max score","max
                 // trh","soluble","insoluble","# spectra trypsin soluble 1","# spectra trypsin
                 // insoluble 1","# spectra trypsin soluble 2","# spectra trypsin insoluble 2","#
                 // spectra gluC soluble 2","# spectra GluC insoluble 2"};
            System.out.println("Found multiple spectra");
            if (array[0].length != RAWARRAY_MULTICOLUMN.length) {
                String header = "";
                for (int j = 0; j < RAWARRAY_MULTICOLUMN.length; j++) {
                    header += RAWARRAY_MULTICOLUMN[j] + "\t";
                }
                list.add(header);
                for (int i = 1; i < array.length; i++) {
                    if (!array[i][0].contains("NH2-")) {
                        String row = "peptide-" + i + "\t\t";
                        for (int j = 0; j < array[0].length; j++) {
                            String value = array[i][j];
                            if (value.equals("yes"))
                                value = "TRUE";
                            if (value.equals(""))
                                value = "FALSE";
                            row += value + "\t";
                        }
                        list.add(row);

                    }
                }
                TabDelimitedTableReader.saveList(list, nameRawData);
            }
        }
    }

    public String getNameRawData() {
        return nameRawData;
    }

    public void setNameRawData(String nameRawData) {
        this.nameRawData = nameRawData;
    }

    public String getNameNTermData() {
        return nameNTermData;
    }

    public void setNameNTermData(String nameNTermData) {
        this.nameNTermData = nameNTermData;
    }

    public String getGenomeAA() {
        return genomeAA;
    }

    public void setGenomeAA(String genomeAA) {
        this.genomeAA = genomeAA;
    }

    public String getGenomeAACompl() {
        return genomeAACompl;
    }

    public void setGenomeAACompl(String genomeAACompl) {
        this.genomeAACompl = genomeAACompl;
    }

    public String getGenomeAAMinus() {
        return genomeAAMinus;
    }

    public void setGenomeAAMinus(String genomeAAMinus) {
        this.genomeAAMinus = genomeAAMinus;
    }

    public String getGenomeAAMinusCompl() {
        return genomeAAMinusCompl;
    }

    public void setGenomeAAMinusCompl(String genomeAAMinusCompl) {
        this.genomeAAMinusCompl = genomeAAMinusCompl;
    }

    public String getGenomeAAPlus() {
        return genomeAAPlus;
    }

    public void setGenomeAAPlus(String genomeAAPlus) {
        this.genomeAAPlus = genomeAAPlus;
    }

    public String getGenomeAAPlusCompl() {
        return genomeAAPlusCompl;
    }

    public void setGenomeAAPlusCompl(String genomeAAPlusCompl) {
        this.genomeAAPlusCompl = genomeAAPlusCompl;
    }

    public Chromosome getChromosome() {
        return chromosome;
    }

    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

}
