package bacnet.datamodel.sequenceNCBI;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import org.biojava3.core.sequence.DNASequence;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.NcRNA;
import bacnet.datamodel.sequence.NcRNA.TypeNcRNA;
import bacnet.datamodel.sequence.Operon;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

public class GenomeConversionElement {

    /**
     * From a DNASequence object, create Sequence<br>
     * Mainly used in GenomeConversion methods
     * 
     * @param dnaSeq
     * @return
     */
    public static Sequence convertDNASequenceGeneral(DNASequence dnaSeq) {
        Sequence seq = new Sequence(dnaSeq.getAccession().toString(), dnaSeq.getBioBegin(), dnaSeq.getBioEnd(),
                GeneNCBITools.getStrand(dnaSeq).charAt(0));
        seq.setComment(GeneNCBITools.getNote(dnaSeq));
        seq.setRef(dnaSeq.getSource());
        return seq;
    }

    /**
     * From a DNASequence make a Gene object
     * 
     * @param dnaSeq
     * @return
     */
    public static Gene convertDNASequenceGene(DNASequence dnaSeq) {
        // System.out.println("Convert: "+dnaSeq.getAccession().toString());
        // if(dnaSeq.getAccession().toString().equals("lin2958")){
        // System.out.println();
        // }
        Gene gene = new Gene(dnaSeq.getAccession().toString(), dnaSeq.getBioBegin(), dnaSeq.getBioEnd(),
                GeneNCBITools.getStrand(dnaSeq).charAt(0));
        gene.setComment(FileUtils.cleanStringFromHex(GeneNCBITools.getNote(dnaSeq)));
        gene.setRef(dnaSeq.getSource());
        gene.setGeneName(FileUtils.cleanStringFromHex(GeneNCBITools.getGene(dnaSeq)));
        gene.setOperon(GeneNCBITools.getOperon(dnaSeq));
        gene.setProduct(FileUtils.cleanStringFromHex(GeneNCBITools.getProduct(dnaSeq)));
        gene.setProtein_id(GeneNCBITools.getProteinID(dnaSeq));
        gene.setCog(GeneNCBITools.getCOG(dnaSeq));
        gene.setOldLocusTag(GeneNCBITools.getOldLocusTag(dnaSeq));

        // gene.setLengthAA(gene.getSequenceAA().length());

        // CodonUsage codonUsage = new CodonUsage("EGD-e");
        // gene.getFeatures().put("CodonUsage",codonUsage.calculate(gene.getSequence())+"");

        // add remaining features
        String[] displayedFeatures = GeneNCBITools.displayedAttributes;
        int i = 1;
        for (String notess : dnaSeq.getNotesList()) {
            String[] notes = notess.split("\n");
            for (String note : notes) {
                note = FileUtils.cleanStringFromHex(note.trim());
                boolean found = false;
                for (String feat : displayedFeatures) {
                    if (note.contains(feat)) {
                        found = true;
                    }
                }
                if (!found) {
                    int separation = note.indexOf(":");
                    // System.out.println(note);
                    if (separation != -1) {
                        String attribute = note.substring(0, separation - 1).trim();
                        String value = note.substring(separation + 1, note.length()).trim();
                        if (attribute.contains("Feature_") && gene.getFeatures().containsKey("Feature_" + i)) {
                            i++;
                            attribute = "Feature_" + i;
                        }
                        gene.getFeatures().put(attribute, value);
                    } else {
                        gene.getFeatures().put("Misc", note);
                    }
                }
            }
        }

        if (gene.getFeatures().containsKey("pseudo")) {
            gene.setPseudogene(true);
        }

        return gene;
    }

    /**
     * From a DNASequence make a Gene object
     * 
     * @param dnaSeq
     * @return
     */
    public static NcRNA convertDNASequenceNcRNA(DNASequence dnaSeq) {
        NcRNA ncRNA = new NcRNA(dnaSeq.getAccession().toString(), dnaSeq.getBioBegin(), dnaSeq.getBioEnd(),
                GeneNCBITools.getStrand(dnaSeq).charAt(0));
        ncRNA.setComment(FileUtils.cleanStringFromHex(GeneNCBITools.getNote(dnaSeq)));
        ncRNA.setRef(dnaSeq.getSource());
        ncRNA.setProduct(GeneNCBITools.getProduct(dnaSeq));
        // add remaining features
        for (String notess : dnaSeq.getNotesList()) {
            String[] notes = notess.split("\n");
            for (String note : notes) {
                note = FileUtils.cleanStringFromHex(note.trim());
                if (note.contains("ribosomal RNA")) {
                    ncRNA.setTypeNcRNA(TypeNcRNA.rRNA);
                }
            }
        }
        return ncRNA;
    }

    public static Operon convertDNASequenceOperon(String[] row, GenomeNCBI genome) {
        Operon operon = new Operon();
        int i = 0;
        operon.setBegin(Integer.parseInt(row[i]));
        i++;
        operon.setEnd(Integer.parseInt(row[i]));
        operon.setLength(operon.getEnd() - operon.getBegin() + 1);
        i++;
        operon.setStrand(row[i].toCharArray()[0]);
        // if strand = false invert from and to

        i++;
        i++;
        String note = row[i];
        operon.setName(note.split("->")[0].trim());
        note = note.split("->")[1].trim();
        String[] genesS = note.split(" ");
        // search cooresponding genes
        for (String gene : genesS) {
            DNASequence seq = genome.getGeneFromName(gene);
            operon.getGenes().add(seq.getAccession().toString());
        }
        return operon;
    }

    public static ArrayList<Operon> getEgdeOperonList() throws Exception {
        GenomeNCBI genome = GenomeNCBITools.loadEgdeGenome();
        ArrayList<Operon> operons = genome.getFirstChromosome().getOperons();
        if (operons.size() == 0) {
            File file = new File(Annotation.EGDE_SUPPTABLE);
            if (file.exists()) {
                String[][] suppData = TabDelimitedTableReader.read(Annotation.EGDE_SUPPTABLE);
                for (int i = 0; i < suppData.length; i++) {
                    // System.out.println(name);
                    if (suppData[i][3].contains("Operon")) {
                        Operon operon = convertDNASequenceOperon(ArrayUtils.getRow(suppData, i), genome);
                        operons.add(operon);
                    }
                }
                System.out.println("Egde operons list read");
                genome.getFirstChromosome().setOperons(operons);
                return operons;
            }
        }
        return operons;
    }

    public static TreeMap<String, Operon> getEgdeOperonMap() throws Exception {
        TreeMap<String, Operon> operons = new TreeMap<String, Operon>();
        ArrayList<Operon> operonList = getEgdeOperonList();
        for (Operon operon : operonList) {
            operons.put(operon.getName(), operon);
        }
        return operons;
    }

    public static void getGeneToOperon() {
        GenomeNCBI genome = GenomeNCBITools.loadEgdeGenome();
        HashMap<String, ArrayList<String>> geneToOperon = new HashMap<String, ArrayList<String>>();
        for (DNASequence sequence : genome.getCodingSequencesList(true)) {
            geneToOperon.put(sequence.getAccession().toString(), new ArrayList<String>());
        }

        try {
            for (Operon operon : getEgdeOperonList()) {
                for (String sequence : operon.getGenes()) {
                    if (geneToOperon.containsKey(sequence)) {
                        geneToOperon.get(sequence).add(operon.getName());
                    }
                }
            }

            String[][] result = new String[geneToOperon.size()][2];
            int i = 0;
            for (String gene : geneToOperon.keySet()) {
                result[i][0] = gene;
                String ret = "";
                for (String operon : geneToOperon.get(gene)) {
                    ret += operon + " ";
                }
                ret.trim();
                result[i][1] = ret;
                i++;
            }

            TabDelimitedTableReader.save(result, "D:/operon.txt");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
