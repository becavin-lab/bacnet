package bacnet.datamodel.annotation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequence.NcRNA;
import bacnet.datamodel.sequence.Operon;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Sequence.SeqType;
import bacnet.datamodel.sequence.Srna;

public class Annotation implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6308650364233188961L;

    /**
     * All supplementary information taken from Toledo-Arana et al., Nature 2009
     */
    public static String EGDE_SUPPTABLE =
            GenomeNCBI.PATH_ANNOTATION + "/EGDeSuppAnnot.txt";
    /**
     * Some EGD-e genes have same name, this file is used to correct gene name
     */
    public static String PATH_CORRECTGENE = GenomeNCBI.PATH_ANNOTATION + "EGDeCorrectGeneName.txt";
    
    /**
     * All supplementary information taken from Glaser et al., Science 2001
     */
    public static String EGDE_ANNOTATION =
            GenomeNCBI.PATH_ANNOTATION + "/EGDeAnnotation.txt";
    public static String MOUSE_ANNOTTABLE =
            Database.getInstance().getPath() + "Analysis/Egd-e Annotation/Annot-Affy-Mouse-MoGene1st.txt";

    public static String[] HEADER = {"Name", "begin", "end", "length", "strand", "type", "details", "synonim"};

    /**
     * Array containing all elements ordered by their begin position
     */
    private String[][] annotation = new String[0][0];
    /**
     * Corresponding Genome
     */
    private String genome = Genome.EGDE_NAME;
    /**
     * Corresponding chromosome
     */
    private String chromosomeID = "";
    /**
     * Number of element type
     */
    private int nbElementType = 0;

    public Annotation() {}

    public Annotation(String[][] annotation) {
        this.annotation = annotation;
    }

    /**
     * Get the lists of elements between beginDraw and endDraw
     * 
     * @param chromosome
     * @param beginDraw
     * @param endDraw
     * @return
     */
    public ArrayList<Sequence> getElements(Chromosome chromosome, int beginDraw, int endDraw) {
        ArrayList<Sequence> sequences = new ArrayList<Sequence>();
        boolean stop = false;
        for (int i = 1; i < annotation.length && !stop; i++) {
            int begin = (int) (Double.parseDouble(annotation[i][getBeginColumn()]));
            int end = (int) (Double.parseDouble(annotation[i][getEndColumn()]));
            /*
             * if the element has its end or its begin between beginDraw and endDraw, we draw it Meaning either
             * : beginDraw < begin < endDraw or : beginDraw < end < endDraw or : begin < beginDraw < endDraw <
             * end
             */
            if ((beginDraw < begin && begin < endDraw) || (beginDraw < end && end < endDraw)
                    || (begin < beginDraw && endDraw < end)) {
                String name = annotation[i][getNameColumn()];
                Sequence sequence = chromosome.getAllElements().get(name);
                sequences.add(sequence);
                /*
                 * When begin is higher than endDraw no need to go further
                 */
            } else if (begin > endDraw) {
                stop = true;
            }
        }

        /*
         * Reorganize list to be sure to display first the ncRNA and the genes and finally sRNAs
         */
        ArrayList<Sequence> tempSequences = new ArrayList<>();
        for (Sequence seq : sequences) {
            if (seq.getType() != SeqType.Srna && seq.getType() != SeqType.ASrna) {
                tempSequences.add(seq);
            }
        }
        for (Sequence seq : sequences) {
            if (seq.getType() == SeqType.Srna || seq.getType() == SeqType.ASrna) {
                tempSequences.add(seq);
            }
        }
        sequences = tempSequences;
        return sequences;
    }

    /**
     * Return the first element for which<br>
     * begin < bpPosition < end
     * 
     * @param bpPosition
     * @return name of the element
     */
    public String getElementATbp(Chromosome chromosome, int bpPosition) {
        /*
         * By supposing an equal repartition of every genome element we try to find quickly a close element
         * near to the bpPosition
         */
        int chromoLength = chromosome.getLength();
        int numberElements = chromosome.getAllElements().size();
        double ratio = (double) chromoLength / (double) numberElements;
        int approxIndex = (int) (bpPosition / ratio);
        int beginGenomeElement = chromoLength;
        while (beginGenomeElement > bpPosition) {
            approxIndex--;
            // System.out.println(approxIndex);
            if (approxIndex == 0 || approxIndex == -1)
                approxIndex = 1;
            beginGenomeElement = (int) (Double.parseDouble(annotation[approxIndex][getBeginColumn()]));
            // System.out.println("index: "+approxIndex);
        }
        /*
         * Now from the genome element at index (approxIndex-2), search the closest genome element which is
         * in the bpPosition
         */
        if (approxIndex > 2)
            approxIndex = approxIndex - 2;
        boolean stop = false;
        for (int i = approxIndex; i < annotation.length && !stop; i++) {
            int begin = (int) (Double.parseDouble(annotation[i][getBeginColumn()]));
            int end = (int) (Double.parseDouble(annotation[i][getEndColumn()]));
            if (begin < bpPosition && bpPosition < end) {
                return annotation[i][getNameColumn()];
            }
        }
        return null;
    }

    /**
     * Return the first element for which<br>
     * begin < bpPosition < end
     * 
     * @param bpPosition
     * @return Sequence
     */
    public Sequence getElementInfoATbp(Chromosome chromosome, int bpPosition) {
        /*
         * By supposing an equal repartition of every genome element we try to find quickly a close element
         * near to the bpPosition
         */
        int chromoLength = chromosome.getLength();
        int numberElements = chromosome.getAllElements().size();
        double ratio = (double) chromoLength / (double) numberElements;
        int approxIndex = (int) (bpPosition / ratio);
        int beginGenomeElement = chromoLength;
        while (beginGenomeElement > bpPosition) {
            approxIndex--;
            // System.out.println(approxIndex);
            if (approxIndex == 0)
                approxIndex = 1;
            beginGenomeElement = (int) (Double.parseDouble(annotation[approxIndex][getBeginColumn()]));
            // System.out.println("index: "+approxIndex);
        }

        /*
         * Now from the genome element at index (approxIndex-2), search the closest genome element which is
         * in the bpPosition
         */
        if (approxIndex > 2)
            approxIndex = approxIndex - 2;
        boolean stop = false;
        for (int i = approxIndex; i < annotation.length && !stop; i++) {
            int begin = (int) (Double.parseDouble(annotation[i][getBeginColumn()]));
            int end = (int) (Double.parseDouble(annotation[i][getEndColumn()]));
            if (begin < bpPosition && bpPosition < end) {
                String name = annotation[i][getNameColumn()];
                Sequence seq = chromosome.getAllElements().get(name);
                if (!(seq instanceof Operon))
                    return seq;
            }
        }
        return null;
    }

    /**
     * Return the column index corresponding to Name
     * 
     * @return
     */
    public static int getNameColumn() {
        return 0;
    }

    /**
     * Return the column index corresponding to Begin
     * 
     * @return
     */
    public static int getBeginColumn() {
        return 1;
    }

    /**
     * Return the column index corresponding to End
     * 
     * @return
     */
    public static int getEndColumn() {
        return 2;
    }

    /**
     * Return the column index corresponding to Type
     * 
     * @return
     */
    public static int getTypeColumn() {
        return 5;
    }

    /**
     * Return a matrix containing different information on each element of the genome first chromosome
     * 
     * @param genome
     * @param elements
     * @return
     */
    public static ExpressionMatrix getAnnotationMatrix(Genome genome, ArrayList<String> elements) {
        ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
        for (Chromosome chromo : genome.getChromosomes().values()) {
            ExpressionMatrix matrix =
                    ExpressionMatrix.arrayToExpressionMatrix(chromo.getAnnotation().getAnnotation(), true);
            matrices.add(matrix);
        }
        ExpressionMatrix matrix = ExpressionMatrix.merge(matrices, false);
        matrix.getHeaderAnnotation().clear();
        matrix = matrix.getSubMatrixRow(elements);
        return Annotation.addAnnotation(matrix, genome);
    }

    /**
     * Return a matrix containing different information on each element of the genome first chromosome
     * 
     * @param genome
     * @return
     */
    public static ExpressionMatrix getAnnotationMatrix(Genome genome) {
        ArrayList<ExpressionMatrix> matrices = new ArrayList<>();
        for (Chromosome chromo : genome.getChromosomes().values()) {
            ExpressionMatrix matrix =
                    ExpressionMatrix.arrayToExpressionMatrix(chromo.getAnnotation().getAnnotation(), true);
            matrices.add(matrix);
        }
        ExpressionMatrix matrix = ExpressionMatrix.merge(matrices, false);
        matrix.getHeaderAnnotation().clear();
        return Annotation.addAnnotation(matrix, genome);
    }

    /**
     * Extract a String[][] with all gene annotation information
     * 
     * @param genome
     * @param elements
     * @return
     */
    public static String[][] getAnnotationGenes(Genome genome, ArrayList<String> elements) {
        String[] headers = {"Locus tag", "Strand", "Chr", "Begin", "End", "Length (bp)", "Length (aa)", "Gene Name",
                "Start codon", "Operon", "Product", "Description", "RAST info.", "COG"};
        String[][] annotation = new String[elements.size() + 1][headers.length];
        annotation[0] = headers;
        for (int i = 0; i < elements.size(); i++) {
            Gene gene = (Gene) genome.getElement(elements.get(i));
            int index = i + 1;
            annotation[index][0] = gene.getName();
            int previousSize = 1;
            annotation[index][previousSize + 0] = gene.getStrand() + "";
            annotation[index][previousSize + 1] = gene.getChromosomeID() + "";
            annotation[index][previousSize + 2] = gene.getBegin() + "";
            annotation[index][previousSize + 3] = gene.getEnd() + "";
            annotation[index][previousSize + 4] = gene.getLength() + "";
            annotation[index][previousSize + 5] = gene.getLengthAA() + "";
            annotation[index][previousSize + 6] = gene.getGeneName();
            annotation[index][previousSize + 7] = gene.getSequence().substring(0, 3);
            annotation[index][previousSize + 8] = gene.getOperon();
            annotation[index][previousSize + 9] =
                    gene.getProduct().replace('\n', ' ').replace('\t', ' ').replace(';', ' ').replaceAll("	", "");
            annotation[index][previousSize + 10] =
                    gene.getComment().replace('\n', ' ').replace('\t', ' ').replace(';', ' ').replaceAll("	", "");
            annotation[index][previousSize + 11] =
                    gene.getRASTinfo().replace('\n', ' ').replace('\t', ' ').replace(';', ' ').replaceAll("	", "");
            annotation[index][previousSize + 12] = gene.getCog();
        }
        return annotation;
    }

    /**
     * Add annotation information to an ExpressionMatrix
     * 
     * @param exprMatrix
     * @return
     */
    public static ExpressionMatrix quickAddAnnotation(ExpressionMatrix exprMatrix, Genome genome) {
        try {
            int previousSize = exprMatrix.getHeaderAnnotation().size();
            String[] headers = {"Strand", "Info", "Description", "Note", "Supp Note", "Operon", "COG",
                    "Functional category (Glaser et al.)", "Signatures"};
            for (String header : headers) {
                exprMatrix.getHeaderAnnotation().add(header);
            }

            // we will add a table of annotation cvontaining info on the gene, sRNA, asRNA
            String[][] annotation = new String[exprMatrix.getNumberRow()][exprMatrix.getHeaderAnnotation().size()];
            for (int i = 0; i < annotation.length; i++) {
                for (int j = 0; j < previousSize; j++) {
                    annotation[i][j] = exprMatrix.getAnnotations()[i][j];
                }
                for (int j = previousSize; j < annotation[0].length; j++) {
                    annotation[i][j] = "";
                }
            }

            // add to ExpressionLMatrix
            exprMatrix.setAnnotations(annotation);

            for (String locus : exprMatrix.getRowNames().keySet()) {
                int index = exprMatrix.getRowNames().get(locus);
                // if it is a Gene
                Sequence seq = genome.getElement(locus);
                if (seq != null) {
                    // System.out.println(locus);
                    if (seq.getType() == SeqType.Gene) {
                        Gene gene = (Gene) seq;
                        annotation[index][previousSize + 0] = gene.getStrand() + "";
                        annotation[index][previousSize + 1] = gene.getGeneName();
                        annotation[index][previousSize + 2] = gene.getComment();
                        annotation[index][previousSize + 3] = gene.getProduct();
                        annotation[index][previousSize + 4] = gene.getRASTinfo();
                        annotation[index][previousSize + 5] = gene.getOperon();
                        annotation[index][previousSize + 6] = gene.getCog();
                        annotation[index][previousSize + 7] = gene.getFeature("GlaserFunctionalCategory");
                        annotation[index][previousSize + 8] = gene.getSignaturesToString();
                    } else if (seq.getType() == SeqType.Srna) {
                        Srna sRNA = (Srna) seq;
                        annotation[index][previousSize + 0] = sRNA.getStrand() + "";
                        annotation[index][previousSize + 1] = sRNA.getLength() + " bp";
                        annotation[index][previousSize + 2] = sRNA.getTypeSrna() + "";
                        annotation[index][previousSize + 3] = sRNA.getRef();
                        annotation[index][previousSize + 5] = sRNA.getBegin() + "";
                        annotation[index][previousSize + 6] = sRNA.getEnd() + "";
                        annotation[index][previousSize + 7] = sRNA.getSynonymsText();
                        annotation[index][previousSize + 8] = sRNA.getFoundInText();
                    } else if (seq.getType() == SeqType.Operon) {
                        Operon operon = (Operon) seq;
                        annotation[index][previousSize + 0] = operon.getStrand() + "";
                        annotation[index][previousSize + 1] = operon.getGenes().size() + " genes";
                        annotation[index][previousSize + 2] = operon.toStringGenes();
                        annotation[index][previousSize + 3] = operon.getRef();
                    } else if (seq.getType() == SeqType.NcRNA) {
                        NcRNA ncRNA = (NcRNA) seq;
                        annotation[index][0] = ncRNA.getStrand() + "";
                        annotation[index][1] = ncRNA.getLength() + " bp";
                        annotation[index][2] = ncRNA.getTypeNcRNA() + "";
                        annotation[index][3] = ncRNA.getProduct();
                        annotation[index][4] = ncRNA.getComment();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Cannot add annotation to the ExpressionMatrix");
        }
        return exprMatrix;
    }

    /**
     * Add annotation information to an ExpressionMatrix
     * 
     * @param exprMatrix
     * @return
     */
    public static ExpressionMatrix addAnnotation(ExpressionMatrix exprMatrix, Genome genome) {
        try {
            int previousSize = exprMatrix.getHeaderAnnotation().size();
            String[] headers = {"Strand", "Begin", "End", "Length (bp)", "Info", "Description", "Note", "Supp Note",
                    "Operon", "COG", "StartCodon", "SD energy"};
            for (String header : headers) {
                exprMatrix.getHeaderAnnotation().add(header);
            }

            // we will add a table of annotation containing info on the gene, sRNA, asRNA
            String[][] annotation = new String[exprMatrix.getNumberRow()][exprMatrix.getHeaderAnnotation().size()];
            for (int i = 0; i < annotation.length; i++) {
                for (int j = 0; j < previousSize; j++) {
                    annotation[i][j] = exprMatrix.getAnnotations()[i][j];
                }
                for (int j = previousSize; j < annotation[0].length; j++) {
                    annotation[i][j] = "";
                }
            }

            // add to ExpressionLMatrix
            exprMatrix.setAnnotations(annotation);

            for (String locus : exprMatrix.getRowNames().keySet()) {
                int index = exprMatrix.getRowNames().get(locus);
                // if it is a Gene
                Sequence seq = genome.getElement(locus);
                if (seq != null) {
                    // System.out.println(locus);
                    if (seq.getType() == SeqType.Gene) {
                        Gene gene = (Gene) seq;
                        String geneName = "";
                                
                        // add phage information in the gene name if Listeria EGD-e
                        if(genome.getSpecies().equals(Genome.EGDE_NAME)) {
                            geneName = gene.getGeneName();
                            int geneId = Integer.parseInt(gene.getName().replaceAll("lmo", ""));
                            if (113 <= geneId && geneId <= 123) {
                                geneName = "lma operon - " + geneName;
                            }
                            if (2270 <= geneId && geneId <= 2333) {
                                geneName = "A118 phage - " + geneName;
                            }
                        }
                        // write line
                        annotation[index][previousSize + 0] = gene.getStrand() + "";
                        annotation[index][previousSize + 1] = gene.getBegin() + "";
                        annotation[index][previousSize + 2] = gene.getEnd() + "";
                        annotation[index][previousSize + 3] = gene.getLength() + "";
                        annotation[index][previousSize + 4] = geneName;
                        annotation[index][previousSize + 5] = gene.getComment();
                        annotation[index][previousSize + 6] = gene.getProduct();
                        annotation[index][previousSize + 7] = gene.getRASTinfo();
                        annotation[index][previousSize + 8] = gene.getOperon();
                        annotation[index][previousSize + 9] = gene.getCog();
                        annotation[index][previousSize + 10] = gene.getSequence().substring(0, 3);
                        // double energy = UNAfold.hybridRNA(gene.getSDSequence(),gene.getName(),
                        // Sequence.ANTI_SD_SEQ,"anti-SD",false);
                        annotation[index][previousSize + 11] = " ";
                    } else if (seq.getType() == SeqType.Srna) {
                        Srna sRNA = (Srna) seq;
                        annotation[index][previousSize + 0] = sRNA.getStrand() + "";
                        annotation[index][previousSize + 1] = sRNA.getBegin() + "";
                        annotation[index][previousSize + 2] = sRNA.getEnd() + "";
                        annotation[index][previousSize + 3] = sRNA.getLength() + "";
                        annotation[index][previousSize + 4] = sRNA.getTypeSrna() + "";
                        annotation[index][previousSize + 5] = sRNA.getRef();
                        annotation[index][previousSize + 6] = sRNA.getSynonymsText();
                        annotation[index][previousSize + 7] = sRNA.getFoundInText();
                        annotation[index][previousSize + 8] = sRNA.getComment();
                        annotation[index][previousSize + 9] = "";
                    } else if (seq.getType() == SeqType.Operon) {
                        Operon operon = (Operon) seq;
                        annotation[index][previousSize + 0] = operon.getStrand() + "";
                        annotation[index][previousSize + 1] = operon.getBegin() + "";
                        annotation[index][previousSize + 2] = operon.getEnd() + "";
                        annotation[index][previousSize + 3] = operon.getLength() + "";
                        annotation[index][previousSize + 4] = operon.getGenes().size() + " genes";
                        annotation[index][previousSize + 5] = operon.toStringGenes();
                        annotation[index][previousSize + 6] = operon.getRef();
                    } else if (seq.getType() == SeqType.NcRNA) {
                        NcRNA ncRNA = (NcRNA) seq;
                        annotation[index][previousSize + 0] = ncRNA.getStrand() + "";
                        annotation[index][previousSize + 1] = ncRNA.getBegin() + "";
                        annotation[index][previousSize + 2] = ncRNA.getEnd() + "";
                        annotation[index][previousSize + 3] = ncRNA.getLength() + "";
                        annotation[index][previousSize + 4] = ncRNA.getTypeNcRNA() + "";
                        annotation[index][previousSize + 5] = ncRNA.getProduct();
                        annotation[index][previousSize + 6] = ncRNA.getComment();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Cannot add annotation to the ExpressionMatrix");
        }
        return exprMatrix;
    }

    /**
     * Add annotation information to an ExpressionMatrix
     * 
     * @param exprMatrix
     * @return
     */
    public static ExpressionMatrix addAnnotationLite(ExpressionMatrix exprMatrix, Genome genome) {
        try {
            int previousSize = exprMatrix.getHeaderAnnotation().size();
            String[] headers = {"Begin", "End", "Length", "Strand", "Description", "Note", "Operon"};
            for (String header : headers) {
                exprMatrix.getHeaderAnnotation().add(header);
            }

            // we will add a table of annotation cvontaining info on the gene, sRNA, asRNA
            String[][] annotation = new String[exprMatrix.getNumberRow()][exprMatrix.getHeaderAnnotation().size()];
            for (int i = 0; i < annotation.length; i++) {
                for (int j = 0; j < previousSize; j++) {
                    annotation[i][j] = exprMatrix.getAnnotations()[i][j];
                }
                for (int j = previousSize; j < annotation[0].length; j++) {
                    annotation[i][j] = "";
                }
            }

            // add to ExpressionLMatrix
            exprMatrix.setAnnotations(annotation);

            for (String locus : exprMatrix.getRowNames().keySet()) {
                int index = exprMatrix.getRowNames().get(locus);
                // if it is a Gene
                Sequence seq = genome.getElement(locus);
                if (seq != null) {
                    // System.out.println(locus);
                    if (seq.getType() == SeqType.Gene) {
                        Gene gene = (Gene) seq;
                        annotation[index][previousSize + 0] = gene.getBegin() + "";
                        annotation[index][previousSize + 1] = gene.getEnd() + "";
                        annotation[index][previousSize + 2] = gene.getLength() + "";
                        annotation[index][previousSize + 3] = gene.getStrand() + "";
                        annotation[index][previousSize + 4] = gene.getGeneName();
                        annotation[index][previousSize + 5] = gene.getProduct();
                        annotation[index][previousSize + 6] = gene.getOperon();
                    } else if (seq.getType() == SeqType.Srna) {
                        Srna sRNA = (Srna) seq;
                        annotation[index][previousSize + 0] = sRNA.getBegin() + "";
                        annotation[index][previousSize + 1] = sRNA.getEnd() + "";
                        annotation[index][previousSize + 2] = sRNA.getLength() + "";
                        annotation[index][previousSize + 3] = sRNA.getStrand() + "";
                        annotation[index][previousSize + 4] = sRNA.getRef();
                        annotation[index][previousSize + 5] = sRNA.getSynonymsText();
                        annotation[index][previousSize + 6] = "";
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Cannot add annotation to the ExpressionMatrix");
        }
        return exprMatrix;
    }

    /**
     * Add annotation information to an ExpressionMatrix
     * 
     * @param exprMatrix
     * @return
     */
    public static ExpressionMatrix addAnnotationMultiChromosome(ExpressionMatrix exprMatrix, Genome genome) {
        try {
            int previousSize = exprMatrix.getHeaderAnnotation().size();
            String[] headers = {"Chromo", "Begin", "End", "Strand", "Length (bp)", "Type", "Product"};
            for (String header : headers) {
                exprMatrix.getHeaderAnnotation().add(header);
            }

            // we will add a table of annotation cvontaining info on the gene, sRNA, asRNA
            String[][] annotation = new String[exprMatrix.getNumberRow()][exprMatrix.getHeaderAnnotation().size()];
            for (int i = 0; i < annotation.length; i++) {
                for (int j = 0; j < previousSize; j++) {
                    annotation[i][j] = exprMatrix.getAnnotations()[i][j];
                }
                for (int j = previousSize; j < annotation[0].length; j++) {
                    annotation[i][j] = "";
                }
            }

            // add to ExpressionLMatrix
            exprMatrix.setAnnotations(annotation);

            for (String locus : exprMatrix.getRowNames().keySet()) {
                int index = exprMatrix.getRowNames().get(locus);
                // if it is a Gene
                Sequence seq = genome.getElement(locus);
                if (seq != null) {
                    annotation[index][previousSize + 0] = "chr" + seq.getChromosome().getChromosomeNumber() + "";
                    annotation[index][previousSize + 1] = seq.getBegin() + "";
                    annotation[index][previousSize + 2] = seq.getEnd() + "";
                    annotation[index][previousSize + 3] = seq.getStrand() + "";
                    annotation[index][previousSize + 4] = seq.getLength() + "";
                    annotation[index][previousSize + 5] = seq.getType() + "";
                    // System.out.println(locus);
                    if (seq.getType() == SeqType.Gene) {
                        Gene gene = (Gene) seq;
                        annotation[index][previousSize + 6] = gene.getProduct();

                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Cannot add annotation to the ExpressionMatrix");
        }
        return exprMatrix;
    }

    /*
     * ********************************** Serialization **********************************
     */

    /**
     * Read compressed, serialized data with a FileInputStream. Uncompress that data with a
     * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream. Replace current data
     * with new data, and redraw everything.
     */
    public static Annotation load(String fileName) {
        try {
            // Create necessary input streams
            FileInputStream fis = new FileInputStream(fileName); // Read from file
            GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
            ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
            // Read in an object. It should be a vector of scribbles
            Annotation seq = (Annotation) in.readObject();
            in.close();
            return seq;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } // Close the stream.

    }

    /**
     * Serialize the vector of lines with an ObjectOutputStream. Compress the serialized objects with a
     * GZIPOutputStream. Write the compressed, serialized data to a file with a FileOutputStream. Don't
     * forget to flush and close the stream.
     */
    public void save(String fileName) {
        try {
            // Create the necessary output streams to save the scribble.
            FileOutputStream fos = new FileOutputStream(fileName);
            // Save to file
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            // Compressed
            ObjectOutputStream out = new ObjectOutputStream(gzos);
            // Save objects
            out.writeObject(this); // Write the entire Vector of scribbles
            out.flush(); // Always flush the output.
            out.close(); // And close the stream.
        }
        // Print out exceptions. We should really display them in a dialog...
        catch (IOException e) {
            System.out.println(e);
        }

    }

    /*
     ***********************************************************************
     * Getter and Setters ********************************************************************
     */
    public String[][] getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String[][] annotation) {
        this.annotation = annotation;
    }

    public String getGenome() {
        return genome;
    }

    public void setGenome(String genome) {
        this.genome = genome;
    }

    public String getChromosomeID() {
        return chromosomeID;
    }

    public void setChromosomeID(String chromosomeID) {
        this.chromosomeID = chromosomeID;
    }

    public int getNbElementType() {
        return nbElementType;
    }

    public void setNbElementType(int nbElementType) {
        this.nbElementType = nbElementType;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
