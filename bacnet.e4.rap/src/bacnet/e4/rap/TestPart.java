package bacnet.e4.rap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import bacnet.Database;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.ColNames;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.dataset.ProteomicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.NcRNA;
import bacnet.expressionAtlas.core.GenomeElementAtlas;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.utils.Filter;
import bacnet.utils.VectorUtils;

public class TestPart {

    @Inject
    EPartService partService;

    @Inject
    EModelService modelService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;
    
    @Inject
    public TestPart() {}

    /**
     * Create contents of the view part.
     */
    @PostConstruct
    public void createControls(Composite parent) {

        /*
         * Run Scripts here!
         */
        // BioCondition bioCond = BioCondition.getBioCondition("EGDe_TIS_Final");
        
        System.out.println("Finished script!");

    }

    @PreDestroy
    public void dispose() {}

    @Focus
    public void setFocus() {
        // TODO Set the focus to control
    }

    /**
     * Ran when the BannerView is opened
     */
    public static void runTests() {
        
        /*
         * PUT YOUR SCRIPT HERE!
         */
        
        
        /*
         * PUT YOUR SCRIPT HERE!
         */
        
        /*
         * Get list of genomes
         */
//        ArrayList<String> genomes = Genome.getAvailableGenomes();
//        for(String genome : genomes) {
//            System.out.println(genome);
//        }
        
        /*
         * Get list of bioconditions
         */
//        ArrayList<BioCondition> bioconditions = BioCondition.getAllBioConditions();
//        for(BioCondition biocond : bioconditions) {
//            System.out.println(biocond.getName() +" "+ biocond.getGenomeName() +" "+ biocond.getTypeDataContained());
//        }
        
        /*
         * Get list transcriptomics data
         */
//        ArrayList<BioCondition> bioconditions = BioCondition.getAllBioConditions();
//        for(BioCondition biocond : bioconditions) {
//            for(OmicsData omics : biocond.getTranscriptomesData()){
//                System.out.println(biocond.getGenomeName() + " " + omics.getName());
//            }
//        }
        
        
        /*
         * Get list of proteomics data
         */
//        ArrayList<BioCondition> bioconditions = BioCondition.getAllBioConditions();
//        for(BioCondition biocond : bioconditions) {
//            for(OmicsData omics : biocond.getProteomicsData()){
//                System.out.println(biocond.getGenomeName() + " " + omics.getName());
//            }
//        }
        
        /*
         * get list of genes and ncRNA in a genome (ex: Listeria monocytogenes EGD-e)
         */
//        // get genome
//        String genomeName = Genome.EGDE_NAME;
//        System.out.println("Open: "+genomeName);
//        Genome genome = Genome.loadGenome(genomeName);
//        // get list of genes and print all
//        LinkedHashMap<String, Gene> genes = genome.getGenes();
//        for(String geneName : genes.keySet()) {
//            Gene gene = genes.get(geneName);
//            System.out.println(gene + " ("+gene.getGeneName()+")");
//        }
//        // get list of ncRNA
//        LinkedHashMap<String, NcRNA> ncrnas = genome.getNcRNAs();
//        for(String ncrnaName : ncrnas.keySet()) {
//            NcRNA ncrna = ncrnas.get(ncrnaName);
//            System.out.println(ncrna + " ("+ncrna.getGeneName()+")");
//        }
        
        
        /*
         * Search for a gene and print available information (ex: lmo0200 in L. mono. EGD-e)
         */
//        String geneName = "lmo0200";
//        String genomeName = Genome.EGDE_NAME;
//        Genome genome = Genome.loadGenome(genomeName);
//        // search gene
//        Gene gene = genome.getGeneFromName(geneName);
//        // print general information
//        System.out.println(gene + " " + gene.getInfo());

        /*
         * Search for a gene and print its sequence (nucleotide and amino acid)
         */
//        String geneName = "lmo0200";
//        String genomeName = Genome.EGDE_NAME;
//        Genome genome = Genome.loadGenome(genomeName);
//        // search gene
//        Gene gene = genome.getGeneFromName(geneName);
//        // print gene nucleotide sequence
//        System.out.println(gene.getSequence());
//        // print gene amino acid sequence
//        System.out.println(gene.getSequenceAA());

        
        /*
         * Get in which transcriptome and proteome a gene is differently expressed
         */
//        String geneName = "lmo0200";
//        String genomeName = Genome.EGDE_NAME;
//        Genome genome = Genome.loadGenome(genomeName);
//        // search gene
//        Gene gene = genome.getGeneFromName(geneName);
//        
//        // get transcriptomics datasets
//        double cutoffLogFC = GenomeElementAtlas.DEFAULT_LOGFC_CUTOFF;
//        Filter filter = new Filter();
//        filter.setCutOff1(cutoffLogFC);
//        GenomeElementAtlas atlas = new GenomeElementAtlas(gene, filter);
//        System.out.println("Over expressed in " + atlas.getOverBioConds().size() + " transcriptomic datasets");
//        for(String bioCond : atlas.getOverBioConds()) {
//            System.out.println(bioCond);
//        }
//        System.out.println("Under expressed in " + atlas.getUnderBioConds().size() + " transcriptomic datasets");
//        // Print datasets over expressed
//        for(String bioCond : atlas.getUnderBioConds()) {
//            System.out.println(bioCond);     
//        }
//        System.out.println("Not diff expressed in " + atlas.getNotDiffExpresseds().size() + " transcriptomic datasets");
//        for(String bioCond : atlas.getNotDiffExpresseds()) {
//            System.out.println(bioCond); 
//        }
//        
//        // get proteomics datasets 
//        ExpressionMatrix exprProteomesMatrix = Database.getInstance().getExprProteomesTable(genomeName);
//        System.out.println(gene.getName());
//        if (exprProteomesMatrix.getRowNames().containsKey(gene.getName())) {
//            for (String header : exprProteomesMatrix.getHeaders()) {
//                double value = exprProteomesMatrix.getValue(gene.getName(), header);
//                if (value > 0) {
//                    System.out.println("Found in " + header + " proteome");
//                }
//            }
//        }
        
        /*
         * Show expression of gene in different omics dataset
         */
//        String geneName = "lmo0196";
//        String genomeName = Genome.EGDE_NAME;
//        Genome genome = Genome.loadGenome(genomeName);
//        // search gene
//        Gene gene = genome.getGeneFromName(geneName);
//        
//        //Select some datasets
//        ArrayList<String> bioConditions = new ArrayList<>();
//        bioConditions.add("BHI_2014_EGDe"); // RNASeq data
//        bioConditions.add("EGDe_280212"); // Tiling array data
//        bioConditions.add("Extracellular_BHI_2011_EGDe"); // proteomics data
//        
//        /*
//         * Load all datasets (Load absolute expression values)
//         * Modified from: bacnet.genomeBrowser.core.DataTrack.loadData()
//         */
//        ArrayList<OmicsData> omics = new ArrayList<OmicsData>();
//        for (String bioCondName : bioConditions) {
//            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
//            for (Tiling tiling : bioCond.getTilings()) {
//                if (!tiling.isInfoRead()) {
//                    tiling.read();
//                    omics.add(tiling);
//                }
//            }
//            for (OmicsData tscData : bioCond.getOmicsData()) {
//                if (tscData.getType() == TypeData.RNASeq) {
//                    ExpressionData rnaseq = (ExpressionData) tscData;
//                    rnaseq.load();
//                    omics.add(rnaseq);
//                }
//            }
//            for (ProteomicsData proteome : bioCond.getProteomes()) {
//                if (!proteome.isLoaded()) {
//                    proteome.load();
//                    omics.add(proteome);
//                }
//            }
//        }
//        
//        // Get expression of the gene in each omics dataset
//        for(OmicsData omic : omics) {
//            if(omic instanceof ExpressionData) {
//                if(omic.getType() == TypeData.RNASeq) {
//                    ExpressionData ngs = (ExpressionData) omic;
//                    double[] values = ngs.read(gene.getBegin(), gene.getEnd());
//                    double value = VectorUtils.mean(values);
//                    System.out.println("RNA expression of " +gene.getName() + " = "+ ngs.getName()+ " " + value);
//                }else if(omic.getType() == TypeData.Tiling) {
//                    Tiling tiling = (Tiling) omic;
//                    double[] values = tiling.get(gene.getBegin(), gene.getEnd(), true);
//                    double value = VectorUtils.mean(values);
//                    System.out.println("Tiling expression of " +gene.getName() + " = "+ tiling.getName()+ " " + value);
//                }
//            }else if(omic instanceof ProteomicsData) {
//                ProteomicsData proteome = (ProteomicsData) omic;
//                double value = proteome.getValue(gene.getName(), ""+ColNames.VALUE);
//                System.out.println("Proteome expression of " +gene.getName() + " = " + proteome.getName()+ " " + value);
//            }
//        }
        
        
        /*
         * Correlate expression of multi-omics dataset for all genes
         */
//        String genomeName = Genome.EGDE_NAME;
//        Genome genome = Genome.loadGenome(genomeName);
//        
//        //Select some datasets
//        ArrayList<String> bioConditions = new ArrayList<>();
//        bioConditions.add("BHI_2014_EGDe"); // RNASeq data
//        bioConditions.add("EGDe_280212"); // Tiling array data
//        bioConditions.add("Extracellular_BHI_2011_EGDe"); // proteomics data
//        
//        /*
//         * Load all datasets (Load absolute expression values)
//         * Modified from: bacnet.genomeBrowser.core.DataTrack.loadData()
//         */
//        ArrayList<OmicsData> omics = new ArrayList<OmicsData>();
//        for (String bioCondName : bioConditions) {
//            BioCondition bioCond = BioCondition.getBioCondition(bioCondName);
//            for (Tiling tiling : bioCond.getTilings()) {
//                if (!tiling.isInfoRead()) {
//                    tiling.read();
//                    omics.add(tiling);
//                }
//            }
//            for (OmicsData tscData : bioCond.getOmicsData()) {
//                if (tscData.getType() == TypeData.RNASeq) {
//                    ExpressionData rnaseq = (ExpressionData) tscData;
//                    rnaseq.load();
//                    omics.add(rnaseq);
//                }
//            }
//            for (ProteomicsData proteome : bioCond.getProteomes()) {
//                if (!proteome.isLoaded()) {
//                    proteome.load();
//                    omics.add(proteome);
//                }
//            }
//        }
//        
//        
//        // Get expression of the genes in each omics dataset
//        HashMap<String, double[]> dataTOvalue = new HashMap<String, double[]>();
//        for(OmicsData omic : omics) {
//            System.out.println("Get expression value from "+omic.getName());
//            double[] valueOmic = new double[genome.getGeneNames().size()];
//            int k = 0;
//            for(String geneName : genome.getGeneNames()) {
//                Gene gene = genome.getGeneFromName(geneName);
//                if(omic instanceof ExpressionData) {
//                    if(omic.getType() == TypeData.RNASeq) {
//                        ExpressionData ngs = (ExpressionData) omic;
//                        double[] values = ngs.read(gene.getBegin(), gene.getEnd());
//                        double value = VectorUtils.mean(values);
//                        valueOmic[k] = value;
//                    }else if(omic.getType() == TypeData.Tiling) {
//                        Tiling tiling = (Tiling) omic;
//                        double[] values = tiling.get(gene.getBegin(), gene.getEnd(), true);
//                        double value = VectorUtils.mean(values);
//                        valueOmic[k] = value;
//                    }
//                }else if(omic instanceof ProteomicsData) {
//                    ProteomicsData proteome = (ProteomicsData) omic;
//                    double value = proteome.getValue(gene.getName(), ""+ColNames.VALUE);
//                    valueOmic[k] = value;
//                }
//                k++;
//            }
//            dataTOvalue.put(omic.getName(), valueOmic);
//        }
//        
//        // calculate correlation
//        System.out.println(dataTOvalue.size());
//        for(int i=0;i<dataTOvalue.size();i++) {
//            String data1 = (String) dataTOvalue.keySet().toArray()[i];
//            for(int j=i+1;j<dataTOvalue.size();j++) {
//                String data2 = (String) dataTOvalue.keySet().toArray()[j];
//                double pearson = VectorUtils.pearsonCorrelation(dataTOvalue.get(data1), dataTOvalue.get(data2));
//                System.out.println("Pearson correlation " + data1 + " vs " + data2 + " = "+pearson);
//            }
//        }
        
        /*
         * Load a tab-delimited table and add annotation
         */
//        ExpressionMatrix matrix = ExpressionMatrix.loadTab("/Users/user/exprMatrix.txt", true);
//        matrix = Annotation.addAnnotation(matrix, Genome.loadEgdeGenome());
//        matrix.saveTab("/Users/user/exprMatrix_Annot.txt","Locustag");

        System.out.println("Finished init scripts!");

    }
    
    /**
     * Ran after the ListeriomicsSample page has open
     */
    public static void runPostTests(EPartService partService) {
        
        /*
         * Open GenomeViewer with preloaded datasets
         * copied from : bacnet.genomeBrowser.GenomeTranscriptomeView.displayBHI37View(EPartService partService)
         */
//        ArrayList<String> bioConditions = new ArrayList<>();
//        bioConditions.add("EGDe_280212");
//        bioConditions.add("BHI_2014_EGDe");
//        bioConditions.add("EGDe_37C_TSS");
//        bioConditions.add("EGDe_37C_RiboSeq");
//        bioConditions.add("EGDe_37C_TermSeq");
//        GenomeTranscriptomeView.displayGenomeElementAndBioConditions(partService, Genome.EGDE_NAME, bioConditions, "");
//                
                
    }

}
