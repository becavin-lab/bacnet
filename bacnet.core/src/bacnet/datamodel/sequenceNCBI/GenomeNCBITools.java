package bacnet.datamodel.sequenceNCBI;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.compound.DNACompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.io.DNASequenceCreator;
import org.biojava3.core.sequence.io.FastaReader;
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.annotation.COGannotation;
import bacnet.datamodel.annotation.CodonUsage;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

public class GenomeNCBITools {

    public static String PATH_NCBI_BacGenome = File.separator + "Volumes" + File.separator + "USBHUB" + File.separator
            + "GenomeNCBI" + File.separator + "AllBacteria" + File.separator;

    /**
     * load and return the chromosome corresponding to an accession
     * 
     * @param accession
     * @return
     * @throws Exception
     */
    public static DNASequence loadCorrespondingChromosome(String accession) throws Exception {
        for (String genomeName : GenomeNCBITools.getAvailableGenome()) {
            String path = getPATH() + genomeName;
            File file = new File(path);

            File[] files = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".fna"))
                        return true;
                    return false;
                }
            });
            for (File fileChromosome : files) {
                String fileChromosomeName = FileUtils.removeExtensionAndPath(fileChromosome.getAbsolutePath());
                if (accession.equals(fileChromosomeName)) {
                    FileInputStream inStream = new FileInputStream(fileChromosome.getAbsolutePath());
                    FastaReader<DNASequence, NucleotideCompound> fastaReader =
                            new FastaReader<DNASequence, NucleotideCompound>(inStream,
                                    new NCBIFastaHeaderParser<DNASequence, NucleotideCompound>(),
                                    new DNASequenceCreator(DNACompoundSet.getDNACompoundSet()));
                    LinkedHashMap<String, DNASequence> genomeSequences = fastaReader.process();
                    System.out.println("Chromosome found! Number of sequence read: " + genomeSequences.size());
                    DNASequence chromosome = genomeSequences.get(genomeSequences.keySet().iterator().next());
                    return chromosome;
                }
                // For refSeq genome we need to delete sometimes NZ_ to have the accession
                if (fileChromosomeName.contains("NZ_")) {
                    fileChromosomeName = fileChromosomeName.substring(3, fileChromosomeName.length());
                }
                if (accession.equals(fileChromosomeName)) {
                    FileInputStream inStream = new FileInputStream(fileChromosome.getAbsolutePath());
                    FastaReader<DNASequence, NucleotideCompound> fastaReader =
                            new FastaReader<DNASequence, NucleotideCompound>(inStream,
                                    new NCBIFastaHeaderParser<DNASequence, NucleotideCompound>(),
                                    new DNASequenceCreator(DNACompoundSet.getDNACompoundSet()));
                    LinkedHashMap<String, DNASequence> genomeSequences = fastaReader.process();
                    System.out.println("Chromosome found! Number of sequence read: " + genomeSequences.size());
                    DNASequence chromosome = genomeSequences.get(genomeSequences.keySet().iterator().next());
                    return chromosome;
                }
            }
        }
        return null;
    }

    /**
     * Find in ModelProvider if Egde Genome is already loaded If not search EGDE_NAME in GENOMEDIR
     * 
     * 
     * @return
     * @throws Exception
     */
    public static GenomeNCBI loadEgdeGenome() {
        return loadGenome(Genome.EGDE_NAME);
    }

    /**
     * Find in ModelProvider if Genome Accession is already loaded If not search load it
     * 
     * @return
     * @throws Exception
     */
    public static GenomeNCBI loadGenome(String genomeAccession) {
        return loadGenome(genomeAccession, getPATH(), true, true);
    }

    /**
     * Load/return a genome given by a genomeAccession ID<br>
     * <br>
     * 
     * If keepInMemory is true : Find in ModelProvider if Genome Accession is already loaded if not load
     * it <br>
     * If keepInMemory is false : Only load the Genome without saving it <br>
     * <br>
     * If annotation is true : Load annotation information of the genome <br>
     * If anotation is false : Only load the sequence of the genome
     * 
     * @param genomeAccession
     * @param keepInMemory
     * @param annotation
     * @return
     */
    public static GenomeNCBI loadGenome(String genomeAccession, String path, boolean keepInMemory, boolean annotation) {
        if (keepInMemory) {
            TreeMap<String, GenomeNCBI> genomes = Database.getInstance().getGenomesNCBI();
            GenomeNCBI genome = new GenomeNCBI();
            if (genomes.size() == 0) {
                try {
                    System.out.println(path + File.separator + genomeAccession);
                    genome = new GenomeNCBI(path + File.separator + genomeAccession, annotation);
                    /*
                     * We found that some GeneName have the same locustag: prfA (lmo0200) prfA (lmo2543) this function
                     * detect that kind of cases
                     */
                    // curateGenomeFindingDoublonGeneName(genome);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Database.getInstance().getGenomesNCBI().put(genome.getSpecies(), genome);
            } else {
                genome = genomes.get(genomeAccession);
                if (genome == null) { // if this genome has not been loaded -> load it
                    try {
                        genome = new GenomeNCBI(path + File.separator + genomeAccession, annotation);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Database.getInstance().getGenomesNCBI().put(genome.getSpecies(), genome);
                }
            }
            return genome;
        } else {
            try {
                return new GenomeNCBI(path + File.separator + genomeAccession, annotation);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static String getPATH() {
        String os = System.getProperty("os.arch");
        if (os.equals("amd64"))
            return PATH_NCBI_BacGenome;
        else
            return PATH_NCBI_BacGenome;
    }
    // /**
    // * Load all data on Egde Annotation
    // * @throws Exception
    // */
    // public static void loadEgdeAnnotation() throws Exception{
    // loadEgdeGenome();
    // SrnaList.getEgdeSrnaList();
    // ASrna.getEgdeASrnaList();
    // Operon.getEgdeOperonList();
    // }

    /**
     * Return an array containing all available Genomes
     * 
     * @return
     */
    private static String[] getAvailableGenome(String fileName) {
        File path = new File(fileName);
        if (path.isDirectory()) {
            // return only subdirectories
            File[] files = path.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (!file.getAbsolutePath().contains(".svn") && file.isDirectory()
                            && !file.getAbsolutePath().contains("RfamData"))
                        return true;
                    return false;
                };
            });
            String[] genomeList = new String[files.length];
            for (int i = 0; i < genomeList.length; i++) {
                genomeList[i] =
                        files[i].getAbsolutePath().substring(path.getAbsolutePath().length() + File.separator.length());
            }
            return genomeList;
        } else {
            // setGenomeDirectory();
            // getAvailableGenome();
            return null;
        }
    }

    public static String[] getAvailableGenome() {
        return getAvailableGenome(getPATH());
    }

    public static ArrayList<String> getAvailableGenome(boolean list) {
        String[] genomes = getAvailableGenome(getPATH());
        ArrayList<String> genomesL = new ArrayList<String>();
        for (String genome : genomes)
            genomesL.add(genome);
        return genomesL;
    }

    /**
     * Return a List of all Listeria genomes available
     * 
     * @return
     */
    public static ArrayList<String> getListeriaGenomes() {
        String[] genomes = GenomeNCBITools.getAvailableGenome();
        ArrayList<String> genomesList = new ArrayList<String>();
        for (String genome : genomes) {
            if (genome.contains("Listeria"))
                genomesList.add(genome);
        }
        return genomesList;
    }

    /**
     * Return a List of all Listeria Monocytogenes genomes available
     * 
     * @return
     */
    public static ArrayList<String> getListeriaMonoGenome() {
        String[] genomes = GenomeNCBITools.getAvailableGenome();
        ArrayList<String> genomesList = new ArrayList<String>();
        for (String genome : genomes) {
            if (genome.contains("Listeria_monocytogenes"))
                genomesList.add(genome);
        }
        return genomesList;
    }

    /**
     * Return a List of all Listeria NOT Monocytogenes genomes available
     * 
     * @return
     */
    public static ArrayList<String> getListeriaNonMonoGenome() {
        String[] genomes = GenomeNCBITools.getAvailableGenome();
        ArrayList<String> genomesList = new ArrayList<String>();
        ArrayList<String> genomesList2 = new ArrayList<String>();
        for (String genome : genomes) {
            if (genome.contains("Listeria"))
                genomesList.add(genome);
        }
        for (String genome : genomesList) {
            if (!genome.contains("Listeria_monocytogenes"))
                genomesList2.add(genome);
        }
        return genomesList2;
    }

    /**
     * Return a list of all NON Listeria genomes
     * 
     * @return
     */
    public static ArrayList<String> getNotListeriaGenome() {
        String[] genomes = GenomeNCBITools.getAvailableGenome();
        ArrayList<String> genomesList = new ArrayList<String>();
        for (String genome : genomes) {
            if (!genome.contains("Listeria"))
                genomesList.add(genome);
        }
        return genomesList;
    }

    /**
     * Return a list of all NON Listeria genomes
     * 
     * @return
     */
    public static ArrayList<String> getBacillusGenome() {
        ArrayList<String> genomes = getNotListeriaGenome();
        ArrayList<String> genomesList = new ArrayList<String>();
        for (String genome : genomes) {
            if (genome.contains("Bacillus"))
                genomesList.add(genome);
        }
        return genomesList;
    }

    /**
     * Return a list of all NON Listeria genomes
     * 
     * @return
     */
    public static ArrayList<String> getNotListeriaAndBacillusGenome() {
        ArrayList<String> genomes = getNotListeriaGenome();
        ArrayList<String> genomesList = new ArrayList<String>();
        for (String genome : genomes) {
            if (!genome.contains("Bacillus"))
                genomesList.add(genome);
        }
        return genomesList;
    }

    /**
     * Extract all complete genomes from genomesList
     * 
     * @param genomesList
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getCompleteGenome(ArrayList<String> genomesList) throws IOException {
        ArrayList<String> newGenomesList = new ArrayList<String>();
        for (String genome : genomesList) {
            if (isCompleteGenome(genome))
                newGenomesList.add(genome);
        }
        return newGenomesList;
    }

    /**
     * Extract all WGS (Whole Genome Shotgun) from genomesList
     * 
     * @param genomesList
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getWGS(ArrayList<String> genomesList) throws IOException {
        ArrayList<String> newGenomesList = new ArrayList<String>();
        for (String genome : genomesList) {
            if (!isCompleteGenome(genome))
                newGenomesList.add(genome);
        }
        return newGenomesList;
    }

    // /**
    // * Test if the Genome directory has been given, and open a dialog to update it
    // in case
    // * @return fileName = path of the genome directory
    // */
    // public static String getGenomeDirectory(){
    // String fileName =
    // Activator.getDefault().getPreferenceStore().getString(GENOME_DIR)+File.separator;
    // ScopedPreferenceStore store =
    // (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
    // IEclipsePreferences[] list = store.getPreferenceNodes(false);
    // //System.out.println(store.);
    // File path = new File(fileName);
    // // test if the file exists
    // if(path.exists()){
    // // test if it corresponds to a directory
    // if(path.isDirectory()){
    // return fileName;
    // }else setGenomeDirectory();
    // }else setGenomeDirectory();
    //
    // return
    // (Activator.getDefault().getPreferenceStore().getString(GENOME_DIR)+File.separator);
    // }

    // /**
    // * Open a dialog to select Genome Directory
    // */
    // public static void setGenomeDirectory(){
    // DirectoryDialog fd = new
    // DirectoryDialog(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
    // SWT.OPEN);
    // fd.setText("Choose a Genome Path");
    // fd.setFilterPath(Project.PATH);
    // String fileName = fd.open();
    //
    // if(fileName!=null){
    // System.out.println("New Genome Path: " + fileName);
    // Activator.getDefault().getPreferenceStore().setValue(GENOME_DIR,fileName);
    // }
    // }

    public static void codonUsageEGDe() {
        CodonUsage codonUsage = new CodonUsage("EGD-e");
        GenomeNCBI genome = GenomeNCBITools.loadEgdeGenome();
        ArrayList<DNASequence> seqs = genome.getCodingSequencesList(true);
        String[][] result = new String[seqs.size()][2];
        for (int i = 0; i < seqs.size(); i++) {
            DNASequence seq = seqs.get(i);
            result[i][0] = seq.getAccession().toString();
            result[i][1] = codonUsage.calculate(seq.getSequenceAsString()) + "";
        }

        TabDelimitedTableReader.save(result, "D:/yo.txt");

    }

    /**
     * Summarize an ExpressionMatrix by regrouping the different genes (rows) by their COG functional
     * categories <br>
     * Averages on all the genes for each category are calculated.
     * 
     * @param matrix
     * @return
     */
    public static ExpressionMatrix getCOGExpression(ExpressionMatrix matrix) {
        GenomeNCBI genome;
        try {
            genome = GenomeNCBITools.loadEgdeGenome();
            HashMap<String, ArrayList<String>> classification = COGannotation.getCogClassification(matrix, genome);
            ExpressionMatrix dataset = new ExpressionMatrix();
            dataset.setFirstRowName("COG Id");
            dataset.setHeaders(matrix.getHeaders());
            dataset.setValues(classification.size(), matrix.getNumberColumn());
            dataset.getHeaderAnnotation().add("COG description");
            dataset.setAnnotations(new String[classification.size()][1]);
            int i = 0;
            for (String cog : classification.keySet()) {
                dataset.getRowNames().put(cog, i);
                ArrayList<String> genes = classification.get(cog);
                double[] rowMean = new double[matrix.getNumberColumn()];
                for (String gene : genes) {
                    int index = matrix.getRowNames().get(gene);
                    for (int j = 0; j < matrix.getNumberColumn(); j++) {
                        rowMean[j] += matrix.getValue(index, j);
                    }
                }
                for (int j = 0; j < matrix.getNumberColumn(); j++) {
                    rowMean[j] = rowMean[j] / genes.size();
                    dataset.setValue(rowMean[j], i, j);
                }
                dataset.getAnnotations()[i][0] = COGannotation.getCOGDescription(cog);
                i++;
            }
            return dataset;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }

   

    /**
     * Detect if the genome is a Complete genome or a WGS using DATABASES_LIST_PATH table
     * 
     * @param genome
     * @return
     * @throws IOException
     */
    public static boolean isCompleteGenome(String genome) throws IOException {
        // String[][] genomesTable = TabDelimitedTableReader.read(new
        // File(DATABASES_LIST_PATH));
        // for(int i=0;i<genomesTable.length;i++){
        // if(genome.equals(genomesTable[i][0])){
        // return Boolean.parseBoolean(genomesTable[i][3]);
        // }
        // }
        return false;
    }

    /**
     * Check if two different locustag are not associated to the same GeneName
     * 
     * @param genome
     */
    public static void curateGenomeFindingDoublonGeneName(GenomeNCBI genome) {
        ArrayList<DNASequence> listGenes = genome.getCodingSequencesList(true);
        for (int i = 0; i < listGenes.size(); i++) {
            for (int j = i + 1; j < listGenes.size(); j++) {
                String geneName = genome.getGeneName(listGenes.get(i).getAccession().toString());
                String geneName2 = genome.getGeneName(listGenes.get(j).getAccession().toString());
                if (geneName.equals(geneName2)) {
                    System.out.println(listGenes.get(i).getParentSequence().getAccession().toString() + "\t" + geneName
                            + "\t" + listGenes.get(i).getAccession().toString() + "\t" + geneName2 + "\t"
                            + listGenes.get(j).getAccession().toString() + "\t");
                }
            }
        }
        System.err.println("Correction Gene Name ");
        if (genome.getSpecies().equals(Genome.EGDE_NAME)) {
            String[][] correctGeneName = TabDelimitedTableReader.read(Annotation.PATH_CORRECTGENE);
            for (int i = 1; i < correctGeneName.length; i++) {
                String geneName = correctGeneName[i][0];
                String goodLocus = correctGeneName[i][3];
                String previousLocus = correctGeneName[i][1];
                String otherLocus = correctGeneName[i][2];
                genome.getFirstChromosome().getLocusTagToGeneNameMap().remove(previousLocus);
                genome.getFirstChromosome().getLocusTagToGeneNameMap().remove(otherLocus);
                if (!goodLocus.equals("")) {
                    System.err.println(geneName + "  " + goodLocus);
                    genome.getFirstChromosome().getGeneNameToLocusTagMap().put(geneName, goodLocus);
                    genome.getFirstChromosome().getLocusTagToGeneNameMap().put(goodLocus, geneName);
                }
            }

            System.err.println("Correction of Gene name done!");
            for (int i = 0; i < listGenes.size(); i++) {
                for (int j = i + 1; j < listGenes.size(); j++) {
                    String geneName = genome.getGeneName(listGenes.get(i).getAccession().toString());
                    String geneName2 = genome.getGeneName(listGenes.get(j).getAccession().toString());
                    if (geneName.equals(geneName2)) {
                        System.out
                                .println("Corrected: " + listGenes.get(i).getParentSequence().getAccession().toString()
                                        + "\t" + geneName + "\t" + listGenes.get(i).getAccession().toString() + "\t"
                                        + geneName2 + "\t" + listGenes.get(j).getAccession().toString() + "\t");
                    }
                }
            }
        }
    }

}
