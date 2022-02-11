package bacnet.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.genome.parsers.gff.FeatureI;
import org.biojava3.genome.parsers.gff.FeatureList;
import org.biojava3.genome.parsers.gff.GFF3Reader;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Genome;
import bacnet.utils.FileUtils;

/**
 * Read GFF files to create a {@link Genome} object
 * 
 *
 * @author Christophe Bécavin
 *
 */
public class GFFNCBIReader {

    public static String EXTENSION = ".gff";

    public static String[] GFF_ATTRIBUTES = {"Note", "note", "product", "protein_id", "Dbxref", "transl_table",
            "ratt_ortholog", "orthologous_to", "Derives_from", "with", "ID_V1", "old_locus_tag", "gene_biotype"};

    /**
     * Read a GFF and for each Gene associate all the corresponding information
     * 
     * @param root
     * @param chromoID
     * @param chromosome
     * @throws IOException
     */
    public static void addAnnotationFromGFF(File file, LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes)
            throws IOException {
        /*
         * Read the appropriate .gff file/ we get only .gff (i.e. annotation data) files in the folder root
         */
        String fileName = FileUtils.removeExtension(file.getAbsolutePath()) + ".gff";
        FeatureList listGenes = GFF3Reader.read(fileName);

        /*
         * We create a HashMap which will regroup features to a same gene or RNA In the the GFF file,
         * features are order by position, so we need to go from one gene to another, considering everything
         * between a gene (or RNA) and another gene (or RNA) as a feature of the former.
         * 
         */
        LinkedHashMap<String, ArrayList<FeatureI>> listLocusTag = new LinkedHashMap<String, ArrayList<FeatureI>>();
        // we get all genes
        for (int j = 0; j < listGenes.size(); j++) {
            FeatureI featureGene = listGenes.get(j);
            // get the genes
            if (featureGene.type().equals("gene") || featureGene.type().equals("RNA")
                    || featureGene.type().equals("pseudogene")) {
                String locus_tag = listGenes.get(j).getAttribute("locus_tag");
                //System.out.println("locus: "+locus_tag);
                if (locus_tag == null) { // if locus_tag attribute was not found, take ID information
                    locus_tag = listGenes.get(j).getAttribute("ID");
                    // if(locus_tag.split(":").length>1) locus_tag = locus_tag.split(":")[1];
                }
                if (locus_tag == null) { // if locus_tag attribute was not found, take ID information
                    locus_tag = listGenes.get(j).getAttribute("gene_id");
                    // if(locus_tag.split(":").length>1) locus_tag = locus_tag.split(":")[1];
                }
                ArrayList<FeatureI> listFeatures = new ArrayList<FeatureI>();
                listFeatures.add(featureGene);
                boolean stop = false;
                for (int k = (j + 1); k < listGenes.size() && !stop; k++) {
                    FeatureI featureCDS = listGenes.get(k);
                    if (featureCDS.type().equals("gene") || featureCDS.type().equals("RNA")
                            || featureCDS.type().equals("pseudogene")) {
                        stop = true;
                    } else {
                        listFeatures.add(featureCDS);
                        j = k;
                    }
                }
                listLocusTag.put(locus_tag, listFeatures);
            }
        }
        addSequences(chromosomes, listLocusTag);
    }

    /**
     * Add all the genome element found in listLocusTag to the Chromosome<br>
     * Go through all locustag and create a <code>DNASequence</code> for each
     * 
     * @param chromosome current chromosome
     * @param listLocusTag <code>LinkedHashMap(String,ArrayList(FeatureI))</code> linking gene name to
     *        the list of Features related to it.
     */
    private static void addSequences(LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes,
            HashMap<String, ArrayList<FeatureI>> listLocusTag) {

        for (String chromo : chromosomes.keySet()) {
            System.out.println(chromo);
        }

        for (String locusTag : listLocusTag.keySet()) {
            /*
             * Init Sequence
             */
            DNASequence sequence = new DNASequence();
            sequence.setAccession(new AccessionID(locusTag));
            // System.out.println(locusTag);
            /*
             * Go through all features of the sequence
             */
            ArrayList<FeatureI> listFeatures = listLocusTag.get(locusTag);

            /*
             * If only Gene, pseudogene or RNA is available
             */
            if (listFeatures.size() == 1) {
                FeatureI feature = listFeatures.get(0);
                String chromoID = feature.seqname();
                if (chromosomes.containsKey(chromoID)) {
                    if (feature.type().equals("gene")) {
                        ChromosomeBacteriaSequence chromosome = chromosomes.get(chromoID);
                        sequence.setBioBegin(feature.location().bioStart());
                        sequence.setBioEnd(feature.location().bioEnd());

                        String ret = "Gene \t " + feature.location().bioStart() + " .. " + feature.location().bioEnd()
                                + "\n";
                        if (feature.hasAttribute("ID")) {
                            String[] text = feature.getAttribute("ID").split(":");
                            if (text.length > 1) {
                                ret += "\t Gene : " + text[1] + "\n";
                                chromosome.getLocusTagToGeneNameMap().put(locusTag, text[1]);
                                chromosome.getGeneNameToLocusTagMap().put(text[1], locusTag);
                            }
                        }

                        /*
                         * If no CDS information is available : add it like it a CDS information!
                         */
                        ret += " \t Strand : " + feature.location().bioStrand() + "\n";
                        for (String attribute : GFF_ATTRIBUTES) {
                            if (feature.hasAttribute(attribute))
                                ret += "\t " + attribute + " : " + feature.getAttribute(attribute) + "\n";
                        }
                        // System.out.println(ret);
                        sequence.addNote(ret);
                        // System.out.println("Add Coding: "+locusTag);
                        chromosome.getCodingSequenceHashMap().put(locusTag, sequence);
                    } else if (feature.type().contains("RNA")) {
                        ChromosomeBacteriaSequence chromosome = chromosomes.get(chromoID);
                        sequence.setBioBegin(feature.location().bioStart());
                        sequence.setBioEnd(feature.location().bioEnd());

                        String ret = feature.type() + " \t " + feature.location().bioStart() + " .. "
                                + feature.location().bioEnd() + "\n";
                        /*
                         * If no CDS information is available : add it like it a CDS information!
                         */
                        ret += " \t Strand : " + feature.location().bioStrand() + "\n";
                        for (String attribute : GFF_ATTRIBUTES) {
                            if (feature.hasAttribute(attribute))
                                ret += "\t " + attribute + " : " + feature.getAttribute(attribute) + "\n";
                        }
                        // System.out.println(ret);
                        sequence.addNote(ret);
                        // System.out.println("Add Non-Coding: "+locusTag);
                        chromosome.getNoncodingSequenceHashMap().put(locusTag, sequence);
                    }
                }
            } else {
                /*
                 * Multiple features available
                 */
                for (FeatureI feature : listFeatures) {
                	System.out.println("feature type: "+feature.type());


                    /*
                     * Create the genome element
                     */
                    String chromoID = feature.seqname();
                	System.out.println("chromoID: "+chromoID);
                	for (String text : chromosomes.keySet()) {
                    	System.out.println("chromosome accession: "+text);

                	}
                    // System.out.println("search chromoID: "+chromoID);
                    if (chromosomes.containsKey(chromoID)) {
                    	System.out.println("chromoID : "+chromoID);
                        ChromosomeBacteriaSequence chromosome = chromosomes.get(chromoID);
                        /*
                         * Add the different information available
                         */
                        if (feature.type().equals("gene")) { // Add coding sequence
                        	System.out.println("feature type gene: "+feature.type());

                            sequence.setBioBegin(feature.location().bioStart());
                            sequence.setBioEnd(feature.location().bioEnd());

                            // System.out.println(sequence);
                            if (feature.hasAttribute("gene")) { // if gene name is available
                                String geneName = feature.getAttribute("gene");
                                chromosome.getLocusTagToGeneNameMap().put(locusTag, geneName);
                                chromosome.getGeneNameToLocusTagMap().put(geneName, locusTag);
                            }
                            String ret = feature.type() + " \t " + feature.location().bioStart() + " .. "
                                    + feature.location().bioEnd() + "\n";
                            for (String attribute : GFF_ATTRIBUTES) {
                                if (feature.hasAttribute(attribute)) {
                                    ret += "\t " + attribute + " : " + feature.getAttribute(attribute) + "\n";
                                }
                            }
                            sequence.addNote(ret);
                        } else if (feature.type().equals("pseudogene")) { // Add coding sequence
                            sequence.setBioBegin(feature.location().bioStart());
                            sequence.setBioEnd(feature.location().bioEnd());
                        } else if (feature.type().contains("mRNA")) { // Add non coding sequence
                            sequence.setBioBegin(feature.location().bioStart());
                            sequence.setBioEnd(feature.location().bioEnd());
                            String ret = feature.type() + " \t " + feature.location().bioStart() + " .. "
                                    + feature.location().bioEnd() + "\n";
                            ret += " \t Strand : " + feature.location().bioStrand() + "\n";
                            String geneName = feature.getAttribute("Name");
                            chromosome.getLocusTagToGeneNameMap().put(locusTag, geneName);
                            chromosome.getGeneNameToLocusTagMap().put(geneName, locusTag);

                            for (String attribute : GFF_ATTRIBUTES) {
                                if (feature.hasAttribute(attribute))
                                    ret += "\t " + attribute + " : " + feature.getAttribute(attribute) + "\n";
                            }
                            // System.out.println("Add Coding: "+locusTag);
                            sequence.addNote(ret);
                            chromosome.getCodingSequenceHashMap().put(locusTag, sequence);
                        } else if (feature.type().contains("RNA")) { // Add non coding sequence
                            sequence.setBioBegin(feature.location().bioStart());
                            sequence.setBioEnd(feature.location().bioEnd());
                            String ret = feature.type() + " \t " + feature.location().bioStart() + " .. "
                                    + feature.location().bioEnd() + "\n";
                            ret += " \t Strand : " + feature.location().bioStrand() + "\n";
                            for (String attribute : GFF_ATTRIBUTES) {
                                if (feature.hasAttribute(attribute))
                                    ret += "\t " + attribute + " : " + feature.getAttribute(attribute) + "\n";
                            }
                            // System.out.println("Add Non-coding: "+locusTag);
                            sequence.addNote(ret);
                            chromosome.getNoncodingSequenceHashMap().put(locusTag, sequence);

                        } else if (feature.type().equals("CDS")) { // Only for coding sequence
                            // then we copy only CDS feature we have for this locus_tag
                            String ret = "CDS \t " + feature.location().bioStart() + " .. "
                                    + feature.location().bioEnd() + "\n";
                            if (feature.hasAttribute("ID")) {
                                String[] text = feature.getAttribute("ID").split(":");
                                if (text.length > 1) {
                                    ret += "\t Gene : " + text[1] + "\n";
                                    chromosome.getLocusTagToGeneNameMap().put(locusTag, text[1]);
                                    chromosome.getGeneNameToLocusTagMap().put(text[1], locusTag);
                                }
                            }
                            if (feature.hasAttribute("gene")) {
                                String text = feature.getAttribute("gene");
                                ret += "\t Gene : " + text + "\n";
                                chromosome.getLocusTagToGeneNameMap().put(locusTag, text);
                                chromosome.getGeneNameToLocusTagMap().put(text, locusTag);
                            }
                            ret += " \t Strand : " + feature.location().bioStrand() + "\n";
                            for (String attribute : GFF_ATTRIBUTES) {
                                if (feature.hasAttribute(attribute))
                                    ret += "\t " + attribute + " : " + feature.getAttribute(attribute) + "\n";
                            }
                            /*
                             * Change position of the sequence according to CDS position
                             */
                            if (feature.location().bioStrand() == '+') {
                                if (feature.location().bioStart() != sequence.getBioBegin()) {
                                    sequence.setBioBegin(feature.location().bioStart());
                                }
                            } else {
                                if (feature.location().bioEnd() != sequence.getBioEnd()) {
                                    sequence.setBioEnd(feature.location().bioEnd());
                                }

                            }
                            sequence.addNote(ret);
                            // System.out.println("Add Coding: "+locusTag);
                            chromosome.getCodingSequenceHashMap().put(locusTag, sequence);
                        } else if (feature.type().equals("exon") && feature.hasAttribute("gbkey")) { // sometimes RNA
                                                                                                     // features are
                            // defined inside exon
                            if (feature.getAttribute("gbkey").equals("rRNA")
                                    || feature.getAttribute("gbkey").equals("tRNA")) {
                                String ret = feature.type() + " \t " + feature.location().bioStart() + " .. "
                                        + feature.location().bioEnd() + "\n";
                                ret += " \t Strand : " + feature.location().bioStrand() + "\n";
                                for (String attribute : GFF_ATTRIBUTES) {
                                    if (feature.hasAttribute(attribute))
                                        ret += "\t " + attribute + " : " + feature.getAttribute(attribute) + "\n";
                                }
                                sequence.addNote(ret);
                                // System.out.println("Add Non-coding: "+locusTag);
                                chromosome.getNoncodingSequenceHashMap().put(locusTag, sequence);
                            }
                        } else if (feature.type().equals("misc_feature") || feature.type().equals("sequence_feature")) {
                            // if supplementary information are found, add it avoiding duplicates
                            if (feature.hasAttribute("note")) {
                                String ret = "Feature_1 : " + feature.location().bioStart() + ".."
                                        + feature.location().bioEnd() + " (" + feature.location().bioStrand() + ") -- "
                                        + feature.getAttribute("note");
                                if (!sequence.getNotesList().contains(ret)) {
                                    sequence.addNote(ret);
                                }
                            }
                            if (feature.hasAttribute("Note")) {
                                String ret = "Feature_1 : " + feature.location().bioStart() + ".."
                                        + feature.location().bioEnd() + " (" + feature.location().bioStrand() + ") -- "
                                        + feature.getAttribute("Note");
                                if (!sequence.getNotesList().contains(ret)) {
                                    sequence.addNote(ret);
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Finish parsing all genes");
    }

    @SuppressWarnings("unused")
    private static boolean comparePosition(DNASequence sequence, FeatureI feature) {
        int beginSeq = sequence.getBioBegin();
        int endSeq = sequence.getBioEnd();
        int beginFeat = Math.abs(feature.location().getBegin());
        int endFeat = Math.abs(feature.location().getEnd());
        if (feature.location().isNegative()) {
            beginSeq = sequence.getBioEnd();
            endSeq = sequence.getBioBegin();
        }
        int approx = 20;
        // we look if beginFeat is around beginSeq +-approx
        if (Math.abs(beginSeq - beginFeat) < approx) {
            // we look if endFeat is around endSeq +-approx
            if (Math.abs(endSeq - endFeat) < approx) {
                return true;
            }
        }
        return false;
    }

}
