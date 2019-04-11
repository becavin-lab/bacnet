package bacnet.scripts.listeriomics;

import org.biojava3.core.sequence.Strand;
import bacnet.datamodel.sequence.Genome;

public class RegulatorRegion {

    public static void run() {

        Genome genome = Genome.loadEgdeGenome();

        System.out.println(genome.getFirstChromosome().getSequenceAsString(1399100, 1399365, Strand.POSITIVE));

        // ArrayList<String> finalResult = new ArrayList<>();
        // ArrayList<Sequence> sequences = new ArrayList<>();
        // for(Sequence seq : genome.getFirstChromosome().getAllElements().values()){
        // if(seq.getType()==SeqType.Gene || seq.getType()==SeqType.Srna ||
        // seq.getType()==SeqType.ASrna ||
        // seq.getType()==SeqType.NcRNA){
        // sequences.add(seq);
        // }
        // }
        //
        // int distRegulatory = 500;
        // for(Sequence seq : sequences){
        // int begin = seq.getBegin()-distRegulatory;
        // int end = seq.getBegin()+10;
        // Strand strand = Strand.POSITIVE;
        // if(!seq.isStrand()){
        // begin = seq.getEnd()-10;
        // end = seq.getEnd()+distRegulatory;
        // strand = Strand.NEGATIVE;
        // }
        // if(begin<1) begin = 1;
        // if(end>genome.getFirstChromosome().getLength()){
        // end = genome.getFirstChromosome().getLength()-1;
        // }
        //
        // finalResult.add(">Reg-"+seq.getName()+"|"+begin+"--"+end+"("+strand+")");
        // finalResult.add(genome.getFirstChromosome().getSequenceAsString(begin,end,strand));
        // if(seq instanceof Srna){
        // Srna sRNA = (Srna) seq;
        // System.out.println(sRNA.getName()+" "+sRNA.getSequence().length());
        // finalResult.add(">"+sRNA.getName());
        // finalResult.add(sRNA.getSequence());
        //
        // }
        //
        // }
        //
        // TabDelimitedTableReader.saveList(finalResult,
        // Project.getEGDeGENOMEDATA_PATH()+"EGDeRegulatoryRegions.fasta");
        //

        /**
         * MEME run: meme codY_list_motif_seq.txt -dna -oc . -nostatus -time 18000 -maxsize 60000 -mod zoops
         * -nmotifs 3 -minw 6 -maxw 50 -revcomp
         * 
         * 
         */

    }

}
