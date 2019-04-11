package bacnet.scripts.listeriomics.nterm;

import java.util.ArrayList;
import org.biojava3.core.sequence.Strand;
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;

public class NTermTranscriptome {

    public static void getTranscriptomes() {
        String fileName = Database.getInstance().getPath() + "ListTranscriptomes.txt";
        System.out.println(fileName);
        ArrayList<String> listBioCond = TabDelimitedTableReader.readList(fileName);
        ExpressionMatrix matrix = ExpressionMatrix
                .load(Database.getLOGFC_MATRIX_TRANSCRIPTOMES_PATH() + "_" + Genome.EGDE_NAME + "_PRIVATE");
        ArrayList<BioCondition> bioConds = new ArrayList<>();
        for (String name : listBioCond) {
            bioConds.add(BioCondition.getBioCondition(name));
        }
        ArrayList<String> comps = new ArrayList<>();
        for (BioCondition bioCond : bioConds) {
            for (String comp : bioCond.getComparisons()) {
                comps.add(bioCond.getName() + " vs " + comp);
                System.out.println(bioCond.getName() + " vs " + comp);
            }
        }

        matrix = matrix.getSubMatrixColumn(comps);
        Annotation.addAnnotation(matrix, Genome.loadEgdeGenome());
        matrix.saveTab(Database.getInstance().getPath() + "Rli42Transcriptomes.txt", "Gene");
    }

    public static void getRegulatoryRegion() {
        ArrayList<String> listSeqNames =
                TabDelimitedTableReader.readList(Database.getInstance().getPath() + "H2O2Transcriptomes_PosReg.txt");
        ArrayList<String> finalResult = new ArrayList<>();
        ArrayList<Sequence> sequences = new ArrayList<>();
        for (String seqName : listSeqNames) {
            Gene gene = Genome.loadEgdeGenome().getFirstChromosome().getGenes().get(seqName);
            sequences.add(gene);
        }

        int distRegulatory = 500;
        for (Sequence seq : sequences) {
            int begin = seq.getBegin() - distRegulatory;
            int end = seq.getBegin() + 10;
            Strand strand = Strand.POSITIVE;
            if (!seq.isStrand()) {
                begin = seq.getEnd() - 10;
                end = seq.getEnd() + distRegulatory;
                strand = Strand.NEGATIVE;
            }
            if (begin < 1)
                begin = 1;
            if (end > Genome.loadEgdeGenome().getFirstChromosome().getLength()) {
                end = Genome.loadEgdeGenome().getFirstChromosome().getLength() - 1;
            }

            finalResult.add(">" + seq.getName() + "|" + begin + "--" + end + "(" + strand + ")");
            finalResult.add(Genome.loadEgdeGenome().getFirstChromosome().getSequenceAsString(begin, end, strand));

        }

        TabDelimitedTableReader.saveList(finalResult,
                Database.getInstance().getPath() + "H2O2Transcriptomes_PosReg.fasta");
    }
}
