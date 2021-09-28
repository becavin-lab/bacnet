package bacnet.datamodel.sequenceNCBI;

import java.util.ArrayList;

import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.DNASequence;
import bacnet.datamodel.annotation.COGannotation;
import bacnet.datamodel.annotation.GlaserFCannotation;
import bacnet.datamodel.sequence.Srna;
import bacnet.utils.ArrayUtils;
import bacnet.utils.ListUtils;
import bacnet.utils.StringColor;
import bacnet.utils.VectorUtils;

/**
 * 
 * List of tools useful to extract information hidden in the NoteList of a DNASequence
 * 
 * 
 * @author Chris
 *
 */
public class GeneNCBITools {

    public static String[] displayedAttributes = {"CDS", "mRNA", "Strand : ", "Gene : ", "note : ", "protein_id : ","protein_Id : ",
            "product : ", "COG : ", "Gene length : ", "Protein length : ", "Molecular mass ", "Operon : "};

    /**
     * Search an element in the Note part of a DNASequence
     * 
     * @param seq DNASequence to search in
     * @param attribute name of the attribute to search
     * @return value of the attribute ; empty string if the attribute does not exists
     */
    public static String searchElement(DNASequence seq, String attribute) {
        for (String notess : seq.getNotesList()) {
            String[] notes = notess.split("\n");
            for (String note : notes) {
                if (note.contains(attribute)) {
                	return note.replaceAll(attribute, "").replaceAll("\t", "").trim();
                }
            }
        }
        return "";
    }
    
    /**
     * Search an element in the Note part of a DNASequence in a sectio of the note :
     * Section = gene, CDS, etc...
     * 
     * @param seq DNASequence to search in
     * @param attribute name of the attribute to search
     * @return value of the attribute ; empty string if the attribute does not exists
     */
    public static String searchElement(DNASequence seq, String attribute, String section) {
        for (String notess : seq.getNotesList()) {
        	if(notess.startsWith(section)) {
        		String[] notes = notess.split("\n");
	            for (String note : notes) {
	                if (note.contains(attribute)) {
	                	return note.replaceAll(attribute, "").replaceAll("\t", "").trim();
	                }
	            }
        	}
        }
        return "";
    }

    public static String getStrand(DNASequence seq) {
        return searchElement(seq, "Strand : ");
    }

    public static String getGene(DNASequence seq) {
        return searchElement(seq, "Gene : ");
    }

    public static String getType(DNASequence seq) {
        return searchElement(seq, "Type : ");
    }

    public static String getNote(DNASequence seq) {
        return searchElement(seq, "note : ");
    }

    /**
     * Search for protein-id in the Note section of Gene<br>
     * Search first : protein_id attribute<br>
     * Search then : protein_Id attribute <br>
     * Finally in CDS section of Note search for: "Dbxref :" and "Genbank:" protein id
     * 
     * @param seq
     * @return
     */
    public static String getProteinID(DNASequence seq) {
    	String resultSearch = searchElement(seq, "protein_id : ");
    	if(resultSearch.equals("")) {
    		resultSearch = searchElement(seq, "protein_Id : ");
    		if(resultSearch.equals("")) {
    	    	if(searchElement(seq, "gene_biotype : ").equals("protein_coding")) {
    	    		resultSearch = searchElement(seq, "Dbxref : ", "CDS");
	    			if(!resultSearch.equals("")) {
	    				for(String resultTemp : resultSearch.split(",")) {
	    					if(resultTemp.startsWith("Genbank")) {
	    						return resultTemp.replaceFirst("Genbank:","").trim();
	    					}
	    				}
	    			}
	        	}
    		}else {
    			return resultSearch;
    		}
    	}
    	return resultSearch;
    }
    
    public static String getOldLocusTag(DNASequence seq) {
        return searchElement(seq, "old_locus_tag : ");
    }
    
    public static String getProduct(DNASequence seq) {
        return searchElement(seq, "product : ");
    }

    public static String getDbxref(DNASequence seq) {
        return searchElement(seq, "db_xref : ");
    }

    public static String getTranstable(DNASequence seq) {
        return searchElement(seq, "trans_table : ");
    }

    public static String getCOG(DNASequence seq) {
        return searchElement(seq, "COG : ");
    }

    public static String getGlaserFC(DNASequence seq) {
        return searchElement(seq, "GlaserFunctionalCategory : ");
    }

    public static String getLength(DNASequence seq) {
        return searchElement(seq, "Gene length : ");
    }

    public static String getLengthProt(DNASequence seq) {
        return seq.getRNASequence().getProteinSequence().getLength() + "";
    }

    public static String getMolMass(DNASequence seq) {
        return searchElement(seq, "Molecular mass : ");
    }

    public static String getIsoelectricPoint(DNASequence seq) {
        return searchElement(seq, "Isoelectric point : ");
    }

    public static String getOperon(DNASequence seq) {
        return searchElement(seq, "Operon : ");
    }

    public static String getCodonUsage(DNASequence seq) {
        return searchElement(seq, "CodonUsage : ");
    }

    public static String getRASTinfo(DNASequence seq) {
        return searchElement(seq, "description RAST : ");
    }

    // public static String getSignatures(DNASequence seq){
    // //seq.getParentSequence()
    // ChromosomeBacteriaSequence chromo = (ChromosomeBacteriaSequence)
    // seq.getParentSequence();
    // seq.getAccession().toString();
    // if(chromo.getSigMap().containsKey(seq.getAccession().toString())){
    // String[] signatures = chromo.getSigMap().get(seq.getAccession().toString());
    // String ret="";
    // if(signatures!=null){
    // for(String sig : signatures){
    // if(!sig.contains("Lmo_GeneExpr genes")){
    // ret+=sig+", ";
    // }
    // }
    // }
    // return ret;
    // }else{
    // return "";
    // }
    // }

    /**
     * Display the info contain in a coding DNASequence
     * 
     * @param coding
     * @return
     */
    public static String toStringCodingInfo(DNASequence coding) {
        String ret = "Gene Locus : " + coding.getAccession().toString() + "\n";
        ret += "begin: " + coding.getBioBegin() + "\n";
        ret += "end: " + coding.getBioEnd() + "\n";
        ret += "Protein: " + coding.getDescription() + "\n";
        ret += "RASTinfo: " + GeneNCBITools.getRASTinfo(coding) + "\n\n";

        ret += "COG: " + COGannotation.getCOGDescription(GeneNCBITools.getCOG(coding)) + "\n";
        ret += "Functional Categories (Glaser et al. 2001): "
                + GlaserFCannotation.getGlaserFCDescription(GeneNCBITools.getGlaserFC(coding)) + "\n";
        // ret+= "Signatures: "+GeneTools.getSignatures(coding)+"\n\n";

        ret += "Source: " + coding.getSource() + "\n";
        for (String note : coding.getNotesList()) {
            if (note.contains("GlaserFunctionalCategory :") || note.contains("COG :")
                    || note.contains("description RAST :"))
                ; // don't display note
            else
                ret += note + "\n";
        }

        ret += "\nSequence information Nucleotide: \n";
        ret += coding.getOriginalHeader() + "\n";
        ret += coding.getSequenceAsString() + "\n";
        ret += "\nSequence information AminoAcid: \n";
        ret += coding.getOriginalHeader() + "\n";
        ret += coding.getRNASequence().getProteinSequence().getSequenceAsString();
        return ret;
    }

    /**
     * Display info contain in a non coding DNASequence
     * 
     * @param coding
     * @return
     */
    public static String toStringNonCodingInfo(DNASequence coding) {
        String ret = "Gene Locus : " + coding.getAccession().toString() + "\n";
        ret += "begin: " + coding.getBioBegin() + "\n";
        ret += "end: " + coding.getBioEnd() + "\n";
        ret += "Protein: " + coding.getDescription() + "\n";
        // display Protein ID;
        if (coding.getDescription().equals("Pseudo-gene or RefSeq provisional")) {
            ret += "Source: " + coding.getSource() + "\n\n";
            for (String note : coding.getNotesList())
                ret += note + "\n";
        } else {
            ret += "Type: " + coding.getNotesList().get(0) + "\n";
            coding.getNotesList().remove(0);
            ret += "Source: " + coding.getSource() + "\n\n";
            for (String note : coding.getNotesList())
                ret += note + "\n";

            ret += "Sequence information : \n";
            ret += coding.getOriginalHeader() + "\n";
            ret += coding.getSequenceAsString() + "\n";
        }

        return ret;
    }

    /**
     * Read a sequence and separate the codon by a space <br>
     * This method return teh sequence as a StringColor, so codon might be colorized after
     * 
     * @param seq
     * @return
     */
    public static StringColor codonDisplay(String seq) {
        StringColor sequence = new StringColor();
        String seqSTR = seq;
        String ret = "";
        int k = 0;
        for (int i = 0; i < seqSTR.length(); i++) {
            if (k == 3) {
                ret += " " + seqSTR.charAt(i);
                k = 1;
            } else {
                ret += seqSTR.charAt(i);
                k++;
            }
        }
        sequence.add(ret);
        return sequence;
    }

    /**
     * Calculates the distance between the two center fo gene1 and gene2<br>
     * If this distance is under scale, in absolute value, and both gene are on the same strand, True is
     * return
     * 
     * @param gene1
     * @param gene2
     * @param scale
     * @return true if |diffCenter|<scale and gene1.strand = gene2.strand
     */
    public static boolean isEqual(DNASequence gene1, DNASequence gene2, int scale) {
        int center1 = (gene1.getBioBegin() + gene1.getBioEnd()) / 2;
        int center2 = (gene2.getBioBegin() + gene2.getBioEnd()) / 2;
        if (Math.abs(center1 - center2) < scale) {
            // System.out.println(GeneTools.getStrand(gene2));
            if (GeneNCBITools.getStrand(gene1).equals(GeneNCBITools.getStrand(gene2))) {
                return true;
            } else
                return false;
        } else
            return false;
    }

    /**
     * Calculate the distance between the two center of gene1 and gene2
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static double diffCenter(DNASequence gene1, DNASequence gene2) {
        int center1 = (gene1.getBioBegin() + gene1.getBioEnd()) / 2;
        int center2 = (gene2.getBioBegin() + gene2.getBioEnd()) / 2;
        return Math.abs(center1 - center2);
    }

    /**
     * Determine that to gene have the same end if their ending piosition are the same +- scale bp
     * 
     * @param gene1
     * @param gene2
     * @param scale
     * @return
     */
    public static boolean sameEnd(DNASequence gene1, DNASequence gene2, int scale) {
        int end1 = gene1.getBioEnd();
        int end2 = gene2.getBioEnd();
        if (Math.abs(end1 - end2) < scale) {
            // System.out.println(GeneTools.getStrand(gene2));
            if (GeneNCBITools.getStrand(gene1).equals(GeneNCBITools.getStrand(gene2))) {
                return true;
            } else
                return false;
        } else
            return false;
    }

    /**
     * Calculate difference in end position
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static double diffEndPosition(DNASequence gene1, DNASequence gene2) {
        int end1 = gene1.getBioEnd();
        int end2 = gene2.getBioEnd();
        return Math.abs(end1 - end2);
    }

    /**
     * Calculate difference in begin position
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static double diffBeginPosition(DNASequence gene1, DNASequence gene2) {
        int begin1 = gene1.getBioBegin();
        int begin2 = gene2.getBioBegin();
        return Math.abs(begin1 - begin2);
    }

    /**
     * Calculate the overlapping size of two genes <br>
     * First, find the gene upstream to the other<br>
     * Then, as we got gene1.begin < gene2.begin, overlap = gene1.end - gene2.begin<br>
     * overlap can be negative, it will mean that we have no overlap (gene1.end < gene2.begin) <br>
     * or overlap can be higher than gene2.length, in that case overlap = gene2.length
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static int overlap(DNASequence gene1, DNASequence gene2) {
        int begin1 = gene1.getBioBegin();
        int end1 = gene1.getBioEnd();
        int begin2 = gene2.getBioBegin();
        int end2 = gene2.getBioEnd();

        // be sure that for both gene begin < end
        if (begin1 > end1) {
            int temp = end1;
            end1 = begin1;
            begin1 = temp;
        }
        if (begin2 > end2) {
            int temp = end2;
            end2 = begin2;
            begin2 = temp;
        }

        // found gene upstream to the other
        int overlap = -1000000;
        if (begin1 < begin2) {
            overlap = end1 - begin2;
            if (overlap > 0 && gene2.getLength() < overlap)
                overlap = gene2.getLength();
        } else {
            overlap = end2 - begin1;
            if (overlap > 0 && gene1.getLength() < overlap)
                overlap = gene1.getLength();
        }
        return overlap;
    }

    /**
     * Decide if two genes are overlapping <br>
     * First, calculate the overlapping size of two genes, finding the gene upstream to the other<br>
     * If overlap is negative it will mean that we have no overlap (gene1.end < gene2.begin) <br>
     * 
     * @param gene1
     * @param gene2
     * @return
     */
    public static boolean isOverlap(DNASequence gene1, DNASequence gene2) {
        int overlap = overlap(gene1, gene2);
        if (overlap > 0) {
            return true;
        } else
            return false;
    }

    /**
     * Convert an Srna into a DNASequence
     * 
     * @param sRNA
     * @return
     */
    public static DNASequence convert(Srna sRNA) {
        DNASequence seq = new DNASequence(sRNA.getSequence());
        seq.setAccession(new AccessionID(sRNA.getName()));
        int begin = sRNA.getBegin();
        int end = sRNA.getEnd();
        // with sRNA we have always begin < end, so if strand=false we need to invert it
        // to get begin > end
        if (!sRNA.isStrand()) {
            int temp = begin;
            begin = end;
            end = temp;
        }
        seq.setBioBegin(begin);
        seq.setBioEnd(end);
        return seq;
    }
}
