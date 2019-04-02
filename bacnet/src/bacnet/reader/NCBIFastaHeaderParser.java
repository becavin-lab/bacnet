package bacnet.reader;

import java.util.ArrayList;
import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.DataSource;
import org.biojava3.core.sequence.TaxonomyID;
import org.biojava3.core.sequence.io.template.FastaHeaderParserInterface;
import org.biojava3.core.sequence.template.AbstractSequence;
import org.biojava3.core.sequence.template.AbstractSequence.AnnotationType;
import org.biojava3.core.sequence.template.Compound;

/**
 * My own implementation of a FastaHeaderParserInterface to parse Fasta files from NCBI ftp
 *
 * NCBI fna (genome and plasmids) gi|gi-number|ref|accession|name gi|16802048|ref|NC_003210.1|
 * Listeria monocytogenes EGD-e, complete genome NCBI ffn (coding) ref|refGenome
 * gi-number|:start-end name ref|NC_003210.1|:318-1673 chromosomal replication initiation protein
 * [Listeria monocytogenes EGD-e] NCBI frn (non-coding) ref|refGenome gi-number|:start-end| name|
 * [locus-tag] ref|NC_003210|:82705-82777|Lys tRNA| [locus_tag=lmot01] NCBI faa (proteins)
 * gi|gi-number|ref|accession|name gi|16802049|ref|NP_463534.1| chromosomal replication initiation
 * protein [Listeria monocytogenes EGD-e]
 *
 * @author Christophe Becavin
 */
public class NCBIFastaHeaderParser<S extends AbstractSequence<C>, C extends Compound>
        implements FastaHeaderParserInterface<S, C> {

    /**
     * Parse out the components where some have a | and others do not
     * 
     * @param header
     * @return
     */
    public String[] getHeaderValues(String header) {
        String[] data = new String[0];
        ArrayList<String> values = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < header.length(); i++) {
            if (header.charAt(i) == '|') {
                values.add(sb.toString());
                sb = new StringBuffer();
            } else if (i == header.length() - 1) {
                sb.append(header.charAt(i));
                values.add(sb.toString());
            } else {
                sb.append(header.charAt(i));
            }

            data = new String[values.size()];
            values.toArray(data);
        }
        return data;
    }

    /**
     * Parse the header and set the values in the sequence
     * 
     * @param header
     * @param sequence
     */
    @Override
    public void parseHeader(String header, S sequence) {
        // uniptrot
        // tr|Q0TET7|Q0TET7_ECOL5 Putative uncharacterized protein OS=Escherichia coli
        // O6:K15:H31 (strain 536 / UPEC) GN=ECP_2553 PE=4 SV=1
        sequence.setOriginalHeader(header);
        String[] data = getHeaderValues(header);
        sequence.setAnnotationType(AnnotationType.CURATED);
        sequence.setSource("NCBI");
        if (data.length == 1) {
            /*
             * Parse fasta header like that: >NZ_HG813249.1 Listeria monocytogenes 6179 chromosome, complete
             * genome By searching if it is starting by a sequence ID like NZ, CP, or NC
             */
            String[] tempString = data[0].split(" ");
            boolean found = false;
            String[] seqIDs = {"NZ", "CP", "NC", "NW", "HG"};
            for (String seqId : seqIDs) {
                if (tempString[0].contains(seqId)) {
                    found = true;
                }
            }
            if (found) {
                sequence.setAccession(new AccessionID(tempString[0], DataSource.NCBI));
                String description = data[0].replaceFirst(tempString[0], "").trim();
                sequence.setDescription(description);
                System.out.println("chromo:" + description);
                System.out.println("chromoID:" + tempString[0]);
            } else {
                sequence.setAccession(new AccessionID(data[0]));
                sequence.setDescription(data[0]);
                System.out.println("chromo and ID:" + data[0]);
            }
        } else if (data[0].equalsIgnoreCase("gi")) {

            DataSource giSource = DataSource.NCBI;
            sequence.setAccession(new AccessionID(data[3].trim(), giSource));
            // System.out.println(data[4].trim());
            String[] data2 = data[4].split(" ");
            String[] data3 = data2[0].split("-");
            /*
             * If it is a .fna or .faa
             */
            if (data3.length == 1) {
                sequence.setDescription(data[4].trim());
                if (data[4].trim().indexOf("[") != -1) {
                    // We have a name of protein so we delete the last part of the name
                    String name = data[4].substring(0, data[4].indexOf("[")).trim();
                    sequence.setDescription(name);
                }
                /*
                 * Then it means it's a .ffn (gene list)
                 */
            } else {
                try {
                    sequence.setBioEnd(Integer.valueOf(data3[1].trim()));
                } catch (Exception e) {
                    // sometimes "," appears, because two positions are provided
                    sequence.setBioEnd(Integer.valueOf(data3[1].split(",")[0].trim()));
                }

                if (data3[0].charAt(1) == 'c') {
                    sequence.setBioBegin(Integer.valueOf(data3[0].substring(2).trim()));
                } else
                    sequence.setBioBegin(Integer.valueOf(data3[0].substring(1).trim()));
            }
        } else if (data[0].equalsIgnoreCase("ref")) {
            sequence.setAccession(new AccessionID(data[1].trim(), DataSource.NCBI));
            // parse position (:bioBegin: BioEnd) ( c=antisense )
            String[] data2 = data[2].split(" ");
            String[] data3 = data2[0].split("-");
            try {
                sequence.setBioEnd(Integer.valueOf(data3[1].trim()));
            } catch (Exception e) {
                // sometimes "," appears, because two positions are provided
                sequence.setBioEnd(Integer.valueOf(data3[1].split(",")[0].trim()));
            }

            if (data3[0].charAt(1) == 'c') {
                sequence.setBioBegin(Integer.valueOf(data3[0].substring(2).trim()));
            } else
                sequence.setBioBegin(Integer.valueOf(data3[0].substring(1).trim()));

            // parse Name
            String species = "";
            if (data.length == 3) { // we have .ffn file (coding sequences)
                sequence.addNote("mRNA");
                // System.out.println(data[2]);
                int pos = data[2].indexOf(" ");
                if (pos != -1) {
                    String longName = data[2].substring(pos).trim();
                    String name = longName.substring(0, longName.indexOf("[")).trim();
                    sequence.setDescription(name);
                    species = longName.substring(longName.indexOf("[") + 1, longName.indexOf("]")).trim();
                    sequence.setTaxonomy(new TaxonomyID(species, DataSource.NCBI));
                } else {
                    sequence.setDescription("null");
                    sequence.setTaxonomy(new TaxonomyID(species, DataSource.NCBI));
                }
            } else if (data.length == 5) { // we have a NCBI.frn file (non-coding sequence)
                sequence.setDescription(data[3].trim());
                sequence.addNote("ncRNA" + data[4]);
            } else {
                System.out.println("This type of header is not supported for accurate parsing");
            }
        } else {
            sequence.setAccession(new AccessionID(data[0])); // avoid the common problem of picking up all the comments
                                                             // original header in getOriginalHeader
        }

    }

}
