package bacnet.datamodel.sequenceNCBI;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.TreeSet;
import org.biojava3.core.sequence.DNASequence;
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.NcRNA;
import bacnet.datamodel.sequence.Operon;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Sequence.SeqType;
import bacnet.datamodel.sequence.Srna;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

/**
 * Static methods for converting GenomeNCBI to Genome<br>
 * Genome is used for fast loading of all genomic information we gathered
 * 
 * @author UIBC
 *
 */
public class GenomeConversion {

    /**
     * Method for Genome in the GenomeNCBIFolder<br>
     * <br>
     * 
     * Convert a GenomeNCBI into a Genome object -> convert each ChromosomeBacteriaSequence into
     * Chromosome:<br>
     * <it> each chromosome sequence are the same
     * 
     * <it> coding and noncoding DNASequence are convert into Gene and NcRNA and serialized <it> sRNA
     * are leaved unchanged <it> Operons, terminator and other elements are serialized <it> Annotation
     * is created, saved in a table (only for verification) and serialized
     * 
     * @param genomeNCBI
     * @param genomeName
     */
    public static Genome run(String genomeName) {
        // read GenomeNCBI
        GenomeNCBI genomeNCBI = GenomeNCBITools.loadGenome(genomeName);

        // create folder and copy .fna in the right folder
        String pathNCBI = GenomeNCBITools.getPATH() + genomeName;

        // run conversion
        return run(genomeNCBI, pathNCBI, genomeName);
    }

    /**
     * Method for Genome already load<br>
     * <br>
     * 
     * Convert a GenomeNCBI into a Genome object -> convert each ChromosomeBacteriaSequence into
     * Chromosome:<br>
     * <it> each chromosome sequence are the same
     * 
     * <it> coding and noncoding DNASequence are convert into Gene and NcRNA and serialized <it> sRNA
     * are leaved unchanged <it> Operons, terminator and other elements are serialized <it> Annotation
     * is created, saved in a table (only for verification) and serialized
     * 
     * @param genomeNCBI already load GenomeNCBI
     * @param pathGenome path where .fna files are
     * @param genomeName final genome name
     */
    public static Genome run(GenomeNCBI genomeNCBI, String pathGenome, String genomeName) {
        String genomePath = Database.getGENOMES_PATH() + genomeName;
        return run(genomeNCBI, pathGenome, genomeName, genomePath);
    }

    /**
     * Method for Genome already load<br>
     * <br>
     * 
     * Convert a GenomeNCBI into a Genome object -> convert each ChromosomeBacteriaSequence into
     * Chromosome:<br>
     * <it> each chromosome sequence are the same
     * 
     * <it> coding and noncoding DNASequence are convert into Gene and NcRNA and serialized <it> sRNA
     * are leaved unchanged <it> Operons, terminator and other elements are serialized <it> Annotation
     * is created, saved in a table (only for verification) and serialized
     * 
     * @param genomeNCBI already load GenomeNCBI
     * @param pathGenome path where .fna files are
     * @param genomeName final genome name
     * @param genomePath should have genomePath = Project.getGENOMES()+genomeName;
     */
    public static Genome run(GenomeNCBI genomeNCBI, String pathGenome, String genomeName, String genomePath) {

        // create folder and copy .fna in the right folder
        createFolderCopyFNA(genomePath, pathGenome);

        // create Genome with no annotation
        Genome genome = new Genome();
        try {
            genome = new Genome(genomePath, false);

            // create a folder to put all Sequence serialized
            String pathSequences = genomePath + File.separator + "Sequences" + File.separator;
            File file = new File(pathSequences);
            if (!file.exists()) {
                file.mkdir();
            }

            // convertGenes
            convertGenes(genome, genomeNCBI, pathSequences);
            // convert ncRNA
            convertNcRNA(genome, genomeNCBI, pathSequences);
            // copy SRNA, serialize operons and terminator
            serializeOtherElements(genome, genomeNCBI, pathSequences);
            // put all elements in a LinkedHashMap<String, Sequence> allElements to create
            // Annotation
            updateAllElements(genome);
            // create Annotation
            createAnnotation(genome, genomePath);

            System.out.println(genome.getSpecies());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Database.getInstance().getGenomes().put(genomeName, genome);
        return genome;
    }

    /**
     * Go through all chromosomes and all Coding region of GenomeNCBI<br>
     * Convert all DNASequence into Gene<br>
     * Add the result to Genome
     * 
     * @param genome
     * @param genomeNCBI
     */
    private static void convertGenes(Genome genome, GenomeNCBI genomeNCBI, String pathSequences) {
        String[][] correctGenesArray = new String[0][0];
        if (genome.getSpecies().equals(Genome.EGDE_NAME)) {
            correctGenesArray = TabDelimitedTableReader.read(Annotation.PATH_CORRECTGENE);
        }

        for (ChromosomeBacteriaSequence chromosomeNCBI : genomeNCBI.getChromosomes().values()) {
            for (String key : chromosomeNCBI.getCodingSequenceHashMap().keySet()) {
                DNASequence dnaSeq = chromosomeNCBI.getCodingSequenceHashMap().get(key);
                Gene gene = GenomeConversionElement.convertDNASequenceGene(dnaSeq);
                if (genome.getSpecies().equals(Genome.EGDE_NAME)) {
                    correctGeneName(gene, correctGenesArray);
                }
                gene.setGenomeName(genome.getSpecies());
                gene.setChromosomeID(chromosomeNCBI.getAccession().toString());
                genome.getChromosomes().get(chromosomeNCBI.getAccession().toString()).getGenes().put(gene.getName(),
                        gene);
                if(!gene.getProtein_id().equals("")) {
                	genome.getChromosomes().get(chromosomeNCBI.getAccession().toString()).getProteinIDTolocusTag().put(gene.getProtein_id(), gene.getName());
                }
                gene.save(pathSequences + gene.getName());
                // for(String note:dnaSeq.getNotesList()){
                // System.out.println(note);
                // }
                // for(String attrib:gene.getFeatures().keySet()){
                // System.out.println("CONVERT::"+attrib+" : "+gene.getFeatures().get(attrib));
                // }
            }
        }
    }

    /**
     * Some Gene name are associated to multiple lmo locus, with this function we delete gene name if
     * necessary
     * 
     * @param correctGenesArray
     */
    private static void correctGeneName(Gene gene, String[][] correctGenesArray) {
        for (int k = 1; k < correctGenesArray.length; k++) {
            String geneName = correctGenesArray[k][0];
            String goodLocus = correctGenesArray[k][3];
            if (geneName.equals(gene.getGeneName())) {
                if (!gene.getName().equals(goodLocus)) {
                    // locus and GeneName should not be associated so we delete GeneName
                    gene.setGeneName("");
                }
            }

            // In case GeneName is not already associated to this locus, we do it
            if (goodLocus.equals(gene.getName())) {
                gene.setGeneName(geneName);
            }
        }
    }

    /**
     * Go through all chromosomes and all NonCoding regions of GenomeNCBI<br>
     * Convert all DNASequence into Gene<br>
     * Add the result to Genome
     * 
     * @param genome
     * @param genomeNCBI
     */
    private static void convertNcRNA(Genome genome, GenomeNCBI genomeNCBI, String pathSequences) {
        for (ChromosomeBacteriaSequence chromosomeNCBI : genomeNCBI.getChromosomes().values()) {
            for (String key : chromosomeNCBI.getNoncodingSequenceHashMap().keySet()) {
                DNASequence dnaSeq = chromosomeNCBI.getNoncodingSequenceHashMap().get(key);
                NcRNA ncRNA = GenomeConversionElement.convertDNASequenceNcRNA(dnaSeq);
                ncRNA.setGenomeName(genome.getSpecies());
                ncRNA.setChromosomeID(chromosomeNCBI.getAccession().toString());
                genome.getChromosomes().get(chromosomeNCBI.getAccession().toString()).getNcRNAs().put(ncRNA.getName(),
                        ncRNA);
                ncRNA.save(pathSequences + ncRNA.getName());
                // for(String note:dnaSeq.getNotesList()){
                // System.out.println(note);
                // }
                // for(String attrib:ncRNA.getFeatures().keySet()){
                // System.err.println(attrib+"m m"+ncRNA.getFeatures().get(attrib));
                // }
            }
        }
    }

    /**
     * Get all elements except Coding and NonCoding serialize them, and put them in Chromosome
     * 
     * @param genome
     * @param genomeNCBI
     * @param pathSequences
     */
    private static void serializeOtherElements(Genome genome, GenomeNCBI genomeNCBI, String pathSequences) {
        for (ChromosomeBacteriaSequence chromosomeNCBI : genomeNCBI.getChromosomes().values()) {
            Chromosome chromo = genome.getChromosomes().get(chromosomeNCBI.getAccession().toString());
            String accession = chromosomeNCBI.getAccession().toString();
            for (Srna sRNA : chromosomeNCBI.getsRNAs()) {
                sRNA.setGenomeName(genome.getSpecies());
                sRNA.setChromosomeID(accession);
                sRNA.save(pathSequences + sRNA.getName());
                chromo.getsRNAs().put(sRNA.getName(), sRNA);
            }
            for (Srna asRNA : chromosomeNCBI.getAsRNAs()) {
                asRNA.setGenomeName(genome.getSpecies());
                asRNA.setChromosomeID(accession);
                asRNA.save(pathSequences + asRNA.getName());
                chromo.getAsRNAs().put(asRNA.getName(), asRNA);
            }
            for (Srna cisRegs : chromosomeNCBI.getCisRegs()) {
                cisRegs.setGenomeName(genome.getSpecies());
                cisRegs.setChromosomeID(accession);
                cisRegs.save(pathSequences + cisRegs.getName());
                chromo.getCisRegs().put(cisRegs.getName(), cisRegs);
            }
            for (Operon operon : chromosomeNCBI.getOperons()) {
                operon.setGenomeName(genome.getSpecies());
                operon.setChromosomeID(accession);
                operon.save(pathSequences + operon.getName());
                chromo.getOperons().put(operon.getName(), operon);
            }
            for (Sequence sequence : chromosomeNCBI.getElements()) {
                sequence.setGenomeName(genome.getSpecies());
                sequence.setChromosomeID(accession);
                sequence.save(pathSequences + sequence.getName());
                chromo.getElements().put(sequence.getName(), sequence);
            }
        }

    }

    /**
     * Add all elements found in the Chromosome to LinkedHashMap<String, Sequence> allElements
     * 
     * @param genome
     */
    public static void updateAllElements(Genome genome) {
        for (Chromosome chromo : genome.getChromosomes().values()) {
            for (String key : chromo.getGenes().keySet()) {
                Sequence seq = chromo.getGenes().get(key);
                chromo.getAllElements().put(seq.getName(), seq);
            }
            for (String key : chromo.getGenesAlternative().keySet()) {
                Sequence seq = chromo.getGenesAlternative().get(key);
                chromo.getAllElements().put(seq.getName(), seq);
            }
            for (String key : chromo.getNcRNAs().keySet()) {
                Sequence seq = chromo.getNcRNAs().get(key);
                chromo.getAllElements().put(seq.getName(), seq);
            }
            for (String key : chromo.getsRNAs().keySet()) {
                Sequence seq = chromo.getsRNAs().get(key);
                chromo.getAllElements().put(seq.getName(), seq);
            }
            for (String key : chromo.getCisRegs().keySet()) {
                Sequence seq = chromo.getCisRegs().get(key);
                chromo.getAllElements().put(seq.getName(), seq);
            }
            for (String key : chromo.getAsRNAs().keySet()) {
                Sequence seq = chromo.getAsRNAs().get(key);
                chromo.getAllElements().put(seq.getName(), seq);
            }
            for (String key : chromo.getOperons().keySet()) {
                Sequence seq = chromo.getOperons().get(key);
                chromo.getAllElements().put(seq.getName(), seq);
            }
            for (String key : chromo.getElements().keySet()) {
                Sequence seq = chromo.getElements().get(key);
                chromo.getAllElements().put(seq.getName(), seq);
            }
            System.err.println("Chromo " + chromo.getAccession().toString() + " has " + chromo.getAllElements().size()
                    + " elements");
        }

    }

    /**
     * Create a Table with all elements of the Genome, which will be used for element search and display
     * on the GenomeViewer
     * 
     * @param genome
     * @param path
     */
    public static void createAnnotation(Genome genome, String path) {
        for (Chromosome chromo : genome.getChromosomes().values()) {
            String[][] annot = new String[chromo.getAllElements().size() + 1][Annotation.HEADER.length];
            for (int j = 0; j < Annotation.HEADER.length; j++) {
                annot[0][j] = Annotation.HEADER[j];
            }
            int i = 1;
            TreeSet<Sequence.SeqType> types = new TreeSet<Sequence.SeqType>();
            for (String name : chromo.getAllElements().keySet()) {
                Sequence seq = chromo.getAllElements().get(name);
                annot[i][0] = seq.getName();
                annot[i][1] = seq.getBegin() + "";
                annot[i][2] = seq.getEnd() + "";
                annot[i][3] = seq.getLength() + "";
                annot[i][4] = seq.getStrand() + "";
                annot[i][5] = seq.getType() + "";
                // typeSrna and synonim
                annot[i][6] = "";
                annot[i][7] = "";
                switch (seq.getType()) {
                    case Gene:
                        annot[i][7] = ((Gene) seq).getGeneName();
                        types.add(SeqType.Gene);
                        break;
                    case NcRNA:
                        annot[i][6] = ((NcRNA) seq).getTypeNcRNA() + "";
                        types.add(SeqType.NcRNA);
                        break;
                    case Operon:
                        annot[i][6] = ((Operon) seq).getGenes().size() + " genes";
                        types.add(SeqType.Operon);
                        break;
                    case Srna:
                        annot[i][6] = ((Srna) seq).getTypeSrna() + "";
                        annot[i][6] = ((Srna) seq).getRef();
                        types.add(SeqType.Srna);
                        break;
                    case terminator:
                        annot[i][7] = seq.getComment();
                        types.add(SeqType.terminator);
                        break;
                    case unknown:
                        annot[i][6] = seq.getComment();
                        types.add(SeqType.unknown);
                        break;
                    case ASrna:
                        annot[i][6] = ((Srna) seq).getTypeSrna() + "";
                        annot[i][6] = ((Srna) seq).getRef();
                        types.add(SeqType.Srna);
                        break;
                    default:
                        break;
                }
                i++;
            }
            if (annot.length == 1) {
                annot = new String[2][Annotation.HEADER.length];
                for (int j = 0; j < Annotation.HEADER.length; j++) {
                    annot[0][j] = Annotation.HEADER[j];
                }
                i = 1;
                annot[i][0] = "No gene";
                annot[i][1] = 1 + "";
                annot[i][2] = 10 + "";
                annot[i][3] = 10 + "";
                annot[i][4] = "+";
                annot[i][5] = "unknown";
                // typeSrna and synonim
                annot[i][6] = "";
                annot[i][7] = "";
            }

            System.out.println("Annotation array created");

            ExpressionMatrix annotMatrix = ExpressionMatrix.arrayToExpressionMatrix(annot, true);
            // annotMatrix.saveTab(path+File.separator+"AnnotationTemp.txt", "Elements");
            System.out.println("Sort annotation");
            /*
             * LONGER STEP : SORTING
             */
            annotMatrix = annotMatrix.sort(1, true);

            String accession = chromo.getChromosomeID();
            annotMatrix.saveTab(path + File.separator + accession + "_Annotation.txt", "Elements");
            Annotation annotation = new Annotation(annotMatrix.toArray("Elements"));
            annotation.setGenome(genome.getSpecies());
            annotation.setChromosomeID(accession);
            annotation.setNbElementType(types.size());
            annotation.save(path + File.separator + accession + "_Annotation");
        }

    }

    /**
     * Create a folder for each Genome, and copy all .fna in it
     * 
     * @param path
     * @param pathNCBI
     */
    public static void createFolderCopyFNA(String path, String pathNCBI) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(pathNCBI);
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".fna"))
                    return true;
                return false;
            }
        });
        for (int i = 0; i < files.length; i++) {
            String accession = FileUtils.removePath(files[i].getAbsolutePath());
            System.out.println(accession);
            try {
                FileUtils.copy(files[i].getAbsolutePath(), path + File.separator + accession);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        files = file.listFiles(new FilenameFilter() {
        @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".faa"))
                    return true;
                return false;
            }
        });
        for (int i = 0; i < files.length; i++) {
            String accession = FileUtils.removePath(files[i].getAbsolutePath());
            System.out.println(accession);
            try {
                FileUtils.copy(files[i].getAbsolutePath(), path + File.separator + accession);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
