package bacnet.reader;

import java.util.ArrayList;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.NcRNA;

public class GBKReader {

    public static void createGenBankFile(Genome genome, String fileName) {
        ArrayList<String> results = new ArrayList<>();
        results.add("LOCUS       PRJEB4153            " + genome.getChromosomes().get(0).getLength()
                + " bp    DNA     circular CON 13-JUN-2013");
        results.add("DEFINITION  Listeria monocytogenes EGD, complete genome.");
        results.add("ACCESSION   PRJEB4153");
        results.add("VERSION     PRJEB4153  ERP003412");

        results.add("FEATURES             Location/Qualifiers");
        results.add(addSpaces(5) + "source" + addSpaces(10) + "1.." + genome.getChromosomes().get(0).getLength());
        results.add(addSpaces(21) + "/organism=\"" + genome.getSpecies() + "\"");
        results.add(addSpaces(21) + "/mol_type=\"genomic DNA\"");
        results.add(addSpaces(21) + "/strain=\" \"");
        results.add(addSpaces(21) + "/db_xref=\" \"");

        for (String locus : genome.getChromosomes().get(0).getGenes().keySet()) {
            // String locus = "lmo0001";
            Gene gene = genome.getChromosomes().get(0).getGenes().get(locus);
            locus = locus.replaceFirst("lmpc", "LMON_");

            String position = gene.getBegin() + ".." + gene.getEnd();
            if (!gene.isStrand())
                position = "complement(" + position + ")";
            results.add(addSpaces(5) + "gene" + addSpaces(12) + position);
            if (!gene.getGeneName().equals("")) {
                results.add(addSpaces(21) + "/gene=\"" + gene.getGeneName() + "\"");
            }
            results.add(addSpaces(21) + "/locus_tag=\"" + locus + "\"");
            results.add(addSpaces(5) + "CDS" + addSpaces(13) + position);
            if (!gene.getGeneName().equals(""))
                results.add(addSpaces(21) + "/gene=\"" + gene.getGeneName() + "\"");
            results.add(addSpaces(21) + "/locus_tag=\"" + locus + "\"");
            results.add(addSpaces(21) + "/codon_start=1");
            results.add(addSpaces(21) + "/transl_table=11");
            if (!gene.getProduct().equals("")) {
                String product = gene.getProduct();
                if (product.indexOf('"') != -1) {
                    product = product.replaceAll("\"", "");
                    System.out.println();
                }
                results.add(addSpaces(21) + "/product=\"" + product + "\"");
                results.add(addSpaces(21) + "/note=\"" + product + "\"");
            }
            // results.add(addSpaces(21)+"/note=\""+gene.getComment()+"\"");
            // results.add(addSpaces(21)+"/translation=\""+gene.getSequenceAA()+"\"");
        }

        for (String locus : genome.getChromosomes().get(0).getNcRNAs().keySet()) {
            NcRNA rna = genome.getChromosomes().get(0).getNcRNAs().get(locus);

            locus = locus.replaceFirst("lmpcr", "LMONR_");
            locus = locus.replaceFirst("lmpct", "LMONT_");

            String position = rna.getBegin() + ".." + rna.getEnd();
            if (!rna.isStrand())
                position = "complement(" + position + ")";
            results.add(addSpaces(5) + "gene" + addSpaces(12) + position);
            results.add(addSpaces(21) + "/locus_tag=\"" + locus + "\"");
            results.add(addSpaces(5) + rna.getTypeNcRNA() + addSpaces(12) + position);
            results.add(addSpaces(21) + "/locus_tag=\"" + locus + "\"");
            if (!rna.getProduct().equals("")) {
                String product = rna.getProduct();
                if (product.indexOf('"') != -1) {
                    product = product.replaceAll("\"", "");
                    System.out.println();
                }
                results.add(addSpaces(21) + "/product=\"" + product + "\"");
            }
        }
        results.add("ORIGIN");
        for (String line : formatSequence(genome))
            results.add(line);

        results.add("//");
        TabDelimitedTableReader.saveList(results, fileName);
    }

    public static ArrayList<String> formatSequence(Genome genome) {
        ArrayList<String> sequences = new ArrayList<>();
        // int nbA =
        // genome.getChromosomes().get(0).countCompounds(DNACompoundSet.getDNACompoundSet().getCompoundForString("A"));
        // int nbC =
        // genome.getChromosomes().get(0).countCompounds(DNACompoundSet.getDNACompoundSet().getCompoundForString("C"));
        // int nbG =
        // genome.getChromosomes().get(0).countCompounds(DNACompoundSet.getDNACompoundSet().getCompoundForString("G"));
        // int nbT =
        // genome.getChromosomes().get(0).countCompounds(DNACompoundSet.getDNACompoundSet().getCompoundForString("T"));
        // String firstLine = "SQ Sequence
        // "+genome.getChromosomes().get(0).getLength()+" BP; "+nbA+" A; "+nbC+" C;
        // "+nbG+" G; "+nbT+" T; 0 other;";
        // sequences.add(firstLine);

        String seq = genome.getChromosomes().get(0).getSequenceAsString();
        for (int i = 0; i < seq.length(); i = i + 60) {
            String line = "        " + (i + 1) + " ";
            if (seq.length() - i < 60) {
                line += seq.substring(i, seq.length()).toLowerCase();
            } else {
                line += seq.substring(i, i + 10).toLowerCase() + " ";
                line += seq.substring(i + 10, i + 20).toLowerCase() + " ";
                line += seq.substring(i + 20, i + 30).toLowerCase() + " ";
                line += seq.substring(i + 30, i + 40).toLowerCase() + " ";
                line += seq.substring(i + 40, i + 50).toLowerCase() + " ";
                line += seq.substring(i + 50, i + 60).toLowerCase() + " ";
                // System.out.println(seq.substring(i,i+60));
            }
            System.out.println(line);
            sequences.add(line);
        }
        return sequences;
    }

    /**
     * Return a String containing the appropriate number of spaces " "
     * 
     * @param number
     * @return
     */
    private static String addSpaces(int number) {
        String spaces = "";
        for (int i = 0; i < number; i++)
            spaces += " ";
        return spaces;
    }
}
