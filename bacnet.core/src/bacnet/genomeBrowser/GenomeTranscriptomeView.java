package bacnet.genomeBrowser;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.datamodel.sequence.Genome;
import bacnet.genomeBrowser.core.Track;
import bacnet.genomeBrowser.core.Track.DisplayType;
import bacnet.genomeBrowser.tracksGUI.TracksComposite;
import bacnet.raprcp.NavigationManagement;
import bacnet.swt.ResourceManager;

/**
 * Display on the genome viewer: expression of specific bioCond and the log of fold change
 * 
 * @author UIBC
 * 
 */
public class GenomeTranscriptomeView {

    public static final String ID = "bacnet.GenomeTranscriptomeView"; //$NON-NLS-1$

    private boolean focused = false;

    private TracksComposite tracksComposite;
    private Track track;

    private ArrayList<String> bioCondNames;
    private Genome genome;

    @Inject
    EPartService partService;

    @Inject
    MPart part;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    private Composite container;

    @Inject
    public GenomeTranscriptomeView() {

    }

    /**
     * Create contents of the view part.
     * 
     * @param parent
     */
    @PostConstruct
    public void createPartControl(Composite parent) {
        focused = true;
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        {
            tracksComposite = new TracksComposite(container, SWT.BORDER, true, partService, shell);
            tracksComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
        }

        System.out.println("Part created");

    }

    @Focus
    public void onFocus() {
        if (!focused) {
            tracksComposite.pushState();
            focused = true;
        } else {
            focused = false;
        }
    }

    @PreDestroy
    public void preDestroy() {
        System.out.println(this.part.getTags());
        System.out.println("Destroy " + ID + " " + this);

    }

    /**
     * set the Data for the genomeViewer
     * 
     * @param genomeName
     * @param bioCondNames
     */
    public void setData(String genomeName, ArrayList<String> bioCondNames) {
        this.bioCondNames = bioCondNames;
        this.genome = Genome.loadGenome(genomeName);
    }

    public void updateView(IProgressMonitor monitor) {
        /*
         * Load Experiment and init Track
         */
        String firstChromoID = genome.getFirstChromosome().getChromosomeID();
        //System.out.println("updateView: "+ firstChromoID);

        track = new Track(genome, firstChromoID);
        int i = 1;
        for (String bioCondName : bioCondNames) {
        	
            /*
             * Add and load BioCondition
             */
        	//System.out.println("biocondname: " + bioCondName);
        	if (bioCondName.contains("_vs_")) {
                String bioCond1 = BioCondition.parseName(bioCondName)[0];
                String bioCond2 = BioCondition.parseName(bioCondName)[1];

                monitor.subTask("Loading datasets " + i + "/" + bioCondNames.size() + " : " + bioCondName);
                track.getDatas().addBioCondition(bioCond1);
                track.getDatas().addBioCondition(bioCond2);
                monitor.worked(1);
                i++;
        	} else {
        		monitor.subTask("Loading datasets " + i + "/" + bioCondNames.size() + " : " + bioCondName);
                track.getDatas().addBioCondition(bioCondName);
                monitor.worked(1);
                i++;
        	}
            
        }
        track.setDisplayType(DisplayType.BIOCOND);
        // filterData(bioCondNames);
        /*
         * Init data
         */
        track.getDatas().setDataColors();
        track.getDatas().setDataSizes();

    }

    public void updateView() {
        /*
         * Load Experiment and init Track
         */
        System.out.println("update");
        String firstChromoID = genome.getFirstChromosome().getChromosomeID();
        track = new Track(genome, firstChromoID);
        for (String bioCondName : bioCondNames) {
            /*
             * Add and load BioCondition
             */
            track.getDatas().addBioCondition(bioCondName);
        }
        track.setDisplayType(DisplayType.BIOCOND);
        // filterData(bioCondNames);
        /*
         * Init data
         */
        track.getDatas().setDataColors();
        track.getDatas().setDataSizes();
        /*
         * Init Composite
         */

        tracksComposite.setTrack(track);
    }

    /**
     * We filter data, and add all comparisons from the data
     */
    public void filterData(ArrayList<String> bioConds) {
        track.getDatas().getDataNOTDisplayed().clear();
        for (String bioCondName : bioCondNames) {
            BioCondition bioCond = BioCondition.getBioCondition(bioCondName, true);
            /*
             * Filter all absolute expression data for GeneExpression and Tiling
             */
            for (OmicsData data : bioCond.getOmicsData()) {
                if (data.getType() == TypeData.GeneExpr || data.getType() == TypeData.Tiling
                        || data.getType() == TypeData.ExpressionMatrix) {
                    if (data.getName().contains("EGDe_37C_Mean"))
                        ;
                    else
                        track.getDatas().getDataNOTDisplayed().add(data.getName());
                }
            }

        }

    }

    /**
     * Open a GenomeView given a genome and list of biological conditions
     * 
     * @param partService
     * @param genome
     * @param bioConditions
     */
    public static GenomeTranscriptomeView displayGenomeElementAndBioConditions(EPartService partService, String genome,
            ArrayList<String> bioConditionsSelected, String genomeElement) {
        String id = GenomeTranscriptomeView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
        // initiate view
        ResourceManager.openView(partService, GenomeTranscriptomeView.ID, id);
        // update data
        MPart part = partService.findPart(id);
        NavigationManagement.pushStateView(id, new HashMap<>());
        GenomeTranscriptomeView view = (GenomeTranscriptomeView) part.getObject();
        view.getTracksComposite().setParentViewId(id);
    	System.out.println("displayGenomeElementAndBioConditions"+ bioConditionsSelected);

        // Create your new ProgressMonitorDialog with a IRunnableWithProgress
        try {
        	System.out.println("in try 1");

            IRunnableWithProgress thread =
                    new OpenBioConditionAndGenomeElementThread(view, genome, bioConditionsSelected, genomeElement);
            System.out.println("after thread 1");
            new ProgressMonitorDialog(view.shell).run(true, false, thread);
            /*
             * Init Composite
             */
        	System.out.println("before getTracksComposite ");

            view.getTracksComposite().setTrack(view.getTrack());
            System.out.println("after view");

            if (!genomeElement.equals("")) {
            	System.out.println("genomeElement: "+ genomeElement);
                view.getTracksComposite().search(genomeElement);
                try {
                	System.out.println("in try 2");
                    @SuppressWarnings("unused")
                    int position = Integer.parseInt(genomeElement);
                } catch (Exception e) {
                	System.out.println("in catch 2");

                    view.getTracksComposite().displaySpecificRegion(genomeElement);
                }
            }
        } catch (InvocationTargetException ex) {
        	System.out.println("in catch 1");

            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return view;
    }

    public static void displayGenomeTranscriptomesView(EPartService partService, HashMap<String, String> parameters) {
        /*
         * Parse state parameters
         */
        String genomeName = parameters.get(NavigationManagement.GENOME);
        String chromoID = parameters.get(NavigationManagement.CHROMO);
        String[] bionconditionsVect = parameters.get(NavigationManagement.LIST).split(":");
        ArrayList<String> bioConditions = new ArrayList<>();
        for (String biocond : bionconditionsVect) {
            bioConditions.add(biocond);
        }
        ArrayList<String> notIncluded = new ArrayList<>();
        if (parameters.containsKey(NavigationManagement.LISTNOTINCLUDE)) {
            String[] genes = parameters.get(NavigationManagement.LISTNOTINCLUDE).split(":");
            for (String gene : genes) {
                notIncluded.add(gene);
            }
        }
        String[] styleParams = parameters.get(NavigationManagement.STYLE).split(":");

        /*
         * Transform state parameters into .gview file and load the genome viewer
         */
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String fileText = "#Data visualization saved: " + dateFormat.format(date) + "\n";
        fileText += "#BioCondition" + "\n";
        for (String bioCondition : bioConditions) {
            fileText += bioCondition + "\n";
        }
        fileText += "#DataNotDisplayed" + "\n";
        for (String data : notIncluded) {
            fileText += data + "\n";
        }
        fileText += "#Genome" + "\n";
        fileText += genomeName + "\n" + chromoID + "\n";
        fileText += "#Style" + "\n";
        fileText += styleParams[0] + "\n";
        fileText += "AbsoluteValueDisplayed=" + styleParams[1] + "\n";
        fileText += "position=" + styleParams[2] + "\n";
        fileText += "zoom horizontal=" + styleParams[3] + "\n";
        fileText += "display sequence=" + styleParams[4] + "\n";

        /*
         * Display view
         */
        displayBioConditionsFromText(partService, fileText);

    }

    /**
     * Open a GenomeView given a genome and text file from .gview file
     * 
     * @param partService
     * @param genome
     * @param bioConditions
     */
    public static void displayBioConditionsFromText(EPartService partService, String textTrack) {
        String id = GenomeTranscriptomeView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
        // initiate view
        ResourceManager.openView(partService, GenomeTranscriptomeView.ID, id);
        // update data
        MPart part = partService.findPart(id);
        GenomeTranscriptomeView view = (GenomeTranscriptomeView) part.getObject();
        view.getTracksComposite().setParentViewId(id);

        /*
         * Parse file
         */
        String[] parsedTrack = textTrack.split("#");
        // for(int i=0;i<parsedTrack.length;i++){
        // System.out.println(i+" - "+parsedTrack[i]);
        // }

        /*
         * BioCondition
         */
        ArrayList<String> bioCondNames = new ArrayList<String>();
        for (String bioCondName : parsedTrack[2].split("\n")) {
            if (!bioCondName.equals("BioCondition") && !bioCondName.equals("")) {
                bioCondNames.add(bioCondName);
            }
        }
        /*
         * DataNotDisplayed
         */
        ArrayList<String> dataNotDisplayed = new ArrayList<String>();
        for (String data : parsedTrack[3].split("\n")) {
            if (!data.equals("DataNotDisplayed") && !data.equals("")) {
                dataNotDisplayed.add(data);
            }
        }
        /*
         * Genome
         */
        String genomeName = parsedTrack[4].split("\n")[1];
        String chromoID = parsedTrack[4].split("\n")[2];

        /*
         * Style
         */
        DisplayType displayType = DisplayType.valueOf(parsedTrack[5].split("\n")[1]);
        boolean absoluteValueDisplayed = Boolean.parseBoolean(parsedTrack[5].split("\n")[2].split("=")[1]);
        int sliderPosition = Integer.parseInt(parsedTrack[5].split("\n")[3].split("=")[1]);
        int zoomPosition = Integer.parseInt(parsedTrack[5].split("\n")[4].split("=")[1]);
        boolean displaySequence = Boolean.parseBoolean(parsedTrack[5].split("\n")[5].split("=")[1]);

        /*
         * End of text parsing
         */

        /*
         * Create view
         */
        ArrayList<String> bioConds = new ArrayList<>();
        for (String bioCondName : bioCondNames) {
            if (bioCondName.contains("_vs_")) {
                bioConds.add(bioCondName.split("_vs_")[0]);
            } else
                bioConds.add(bioCondName);
        }
        // Create your new ProgressMonitorDialog with a IRunnableWithProgress
        try {
            IRunnableWithProgress thread = new OpenBioConditionAndGenomeElementThread(view, genomeName, bioConds, "");
            new ProgressMonitorDialog(view.shell).run(true, false, thread);
            /*
             * Init Composite
             */
            Track track = view.getTrack();
            track.setChromosomeID(chromoID);
            track.getDatas().setDisplayAbsoluteValue(absoluteValueDisplayed);
            if (!absoluteValueDisplayed) {
                track.getDatas().absoluteTOrelativeValue();
            }
            track.setDisplayType(displayType);
            track.getDatas().setDataNOTDisplayed(dataNotDisplayed);
            track.getDatas().setDataColors();
            track.getDatas().setDataSizes();
            track.moveHorizontally(sliderPosition);
            track.setDisplaySequence(displaySequence);
            if (track.isDisplaySequence()) {
                while (track.getZoom().getZoomPosition() != track.getZoom().getZoomNumber()) {
                    track.zoom(true);
                }
            }
            int zoomDifference = track.getZoom().getZoomPosition() - zoomPosition;
            // System.out.println("zoomdiff: "+zoomDifference);
            for (int i = 0; i < Math.abs(zoomDifference); i++) {
                track.zoom(zoomDifference < 0);
            }
            // System.out.println("zoom: "+track.getZoom().getZoomPosition());

            view.getTracksComposite().setTrack(track);
            view.getTracksComposite().update();

        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static class OpenBioConditionAndGenomeElementThread implements IRunnableWithProgress {
        private String genome;
        private ArrayList<String> bioConditions = new ArrayList<>();
        @SuppressWarnings("unused")
        private String genomeElement;
        private GenomeTranscriptomeView view;

        public OpenBioConditionAndGenomeElementThread(GenomeTranscriptomeView view, String genome,
                ArrayList<String> bioConditions, String genomeElement) {
            this.genome = genome;
            this.view = view;
            this.bioConditions = bioConditions;
            this.genomeElement = genomeElement;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            //System.out.println("run");

        	int sizeProcess = 1 + bioConditions.size();
            // Tell the user what you are doing
            monitor.beginTask("Loading datasets for Genome Viewer", sizeProcess);
            //System.out.println("run2");

            // Optionally add subtasks
            monitor.subTask("Loading genome: " + genome);
            //System.out.println("run3");

            monitor.worked(1);
            //System.out.println("run4");

            view.setData(genome, bioConditions);
            //System.out.println("run5: " +bioConditions);

            monitor.worked(1);
            //System.out.println("run6");

            view.updateView(monitor);
            //System.out.println("run7");

            // You are done
            monitor.done();
            //System.out.println("monitor.done()");

        }

    }

    /**
     * Load the minimal number of data for the simplest proteomics view
     */
    public static void displayBHI37View(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("EGDe_280212");
        bioConditions.add("BHI_2014_EGDe");
        bioConditions.add("EGDe_37C_TSS");
        bioConditions.add("EGDe_37C_RiboSeq");
        bioConditions.add("EGDe_37C_TermSeq");

        displayGenomeElementAndBioConditions(partService, Genome.EGDE_NAME, bioConditions, "");
    }

    /**
     * Load the minimal number of data for the simplest proteomics view
     */
    public static void displayNTerminomics(EPartService partService, String peptideID) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("BHI_2014_EGDe");
        bioConditions.add("EGDe_37C_TSS");
        bioConditions.add("EGDe_37C_TermSeq");
        bioConditions.add("EGDe_37C_RiboSeq");
        bioConditions.add(NTerm.NTERM_PROJECT_BHI);
        displayGenomeElementAndBioConditions(partService, Genome.EGDE_NAME, bioConditions, peptideID);
    }

    public static void displayStat37View(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("EGDe_Stat");
        bioConditions.add("Stat_2009_EGDe");
        // bioConditions.add("StatPhase_10403S");
        displayGenomeElementAndBioConditions(partService, Genome.EGDE_NAME, bioConditions, "");
    }

    public static void displayIntracellularMacrophagesView(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Macrophages_2014_EGDe");
        bioConditions.add("BHI_2014_EGDe");
        if (Database.getInstance().getProjectName() == Database.LISTERIOMICS_PROJECT) {
            bioConditions.add("EGDe_Complete_TSS");
        }
        displayGenomeElementAndBioConditions(partService, Genome.EGDE_NAME, bioConditions, "");
    }

    
    /**
     * Display omics data for Yersinia pestis CO92
     * @param partService
     */
    public static void displayYersiCO92(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Pestis_CO92_WT_37C_Planktonic_TMH_Glucose_NextSeq500_2019");
        bioConditions.add("Pestis_CO92_WT_37C_Planktonic_TMH_Glycerol_NextSeq500_2019");
        bioConditions.add("Pestis_CO92_WT_37C_Biofilm_TMH_Glucose_NextSeq500_2019");
        bioConditions.add("Pestis_CO92_WT_37C_Biofilm_TMH_Glycerol_NextSeq500_2019");
        bioConditions.add("Pestis_CO92_Mutant_crp_37C_Planktonic_TMH_Glucose_NextSeq500_2019");
        bioConditions.add("Pestis_CO92_Mutant_crp_37C_Biofilm_TMH_Glucose_NextSeq500_2019");
        bioConditions.add("Pestis_CO92_WT_37C_BHI_HiSeq2000_2016");
        bioConditions.add("Pestis_CO92_WT_37C_Intracellular_HiSeq2000_2016");
        bioConditions.add("Pestis_CO92_WT_37C_Extracellular_HiSeq2000_2016");
        bioConditions.add("Pestis_CO92_25C_solidLBH_QExactiveHF_2020");
        bioConditions.add("Pestis_CO92_37C_M9_preculture25C_QExactiveHF_2020");
        bioConditions.add("Pestis_CO92_37C_M9_preculture37C_QExactiveHF_2020");
        bioConditions.add("Pestis_CO92_37C_Plasma_preculture25C_QExactiveHF_2020");
        bioConditions.add("Pestis_CO92_37C_Plasma_preculture37C_QExactiveHF_2020");
        bioConditions.add("Pestis_CO92_37C_solidLBH_QExactiveHF_2020");
        displayGenomeElementAndBioConditions(partService, "Yersinia pestis CO92", bioConditions, "");
    }
    
    /**
     * Display omics data for Yersinia pestis KIM
     * @param partService
     */
    public static void displayYersiKIM(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Pestis_KIM5_28C_1h30_Log_J774A1_JCVI_2010");
        bioConditions.add("Pestis_KIM5_28C_4h_Log_Control_JCVI_2010");
        bioConditions.add("Pestis_KIM5_28C_4h_Log_J774A1_JCVI_2010");
        bioConditions.add("Pestis_KIM5_28C_8h_Log_J774A1_JCVI_2010");

        displayGenomeElementAndBioConditions(partService, "Yersinia pestis KIM10+", bioConditions, "");
    }
    
    /**
     * Display omics data for Yersinia  pseudotuberculosis IP32953
     * @param partService
     */
    public static void displayYersiIP32953(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Pseudotuberculosis_IP32953_WT_25C_Log_LB_HiSeq2500_2016");
        bioConditions.add("Pseudotuberculosis_IP32953_WT_25C_Stat_LB_HiSeq2500_2016");
        bioConditions.add("Pseudotuberculosis_IP32953_WT_37C_Log_LB_HiSeq2500_2016");
        bioConditions.add("Pseudotuberculosis_IP32953_WT_37C_Stat_LB_HiSeq2500_2016");
        bioConditions.add("Pseudotuberculosis_IP32953_WT_37C_PeyerPatches_HiSeq2500_2016");

        displayGenomeElementAndBioConditions(partService, "Yersinia pseudotuberculosis IP32953", bioConditions, "");
    }
    
    /**
     * Display omics data for Yersinia pseudotuberculosis YPIII
     * @param partService
     */
    public static void displayYersiYPIII(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Pseudotuberculosis_YPIII_Mutant_crp_25C_Stat_Agilent_2014");
        bioConditions.add("Pseudotuberculosis_YPIII_Mutant_crp_25C_Log_Agilent_2014");
        bioConditions.add("Pseudotuberculosis_YPIII_Mutant_csrA_25C_Log_Agilent_2014");
        bioConditions.add("Pseudotuberculosis_YPIII_Mutant_rovA_25C_Log_Agilent_2014");
        bioConditions.add("Pseudotuberculosis_YPIII_25_Log_Anaerobic_Agilent_2014");
        bioConditions.add("Pseudotuberculosis_YPIII_25_Stat_Anaerobic_Agilent_2014");

        displayGenomeElementAndBioConditions(partService, "Yersinia pseudotuberculosis YPIII", bioConditions, "");
    }
    
    /**
     * Display omics data for Yersinia enterocolitica Y11
     * @param partService
     */
    public static void displayYersiY11(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Enterocolitica_647176_Mutant_ybeY_22C_Log_IHS2000_2014");
        bioConditions.add("Enterocolitica_647176_Mutant_ybeY_37C_Log_IHS2000_2014");
        bioConditions.add("Enterocolitica_647176_WT_22C_Log_IHS2000_2014");
        bioConditions.add("Enterocolitica_647176_WT_37C_Log_IHS2000_2014");
        bioConditions.add("Enterocolitica_647176_Mutant_hfq_22C_Log_IHS2000_2015");
        bioConditions.add("Enterocolitica_647176_Mutant_hfq_37C_Log_IHS2000_2015");
        bioConditions.add("Enterocolitica_647176_Mutant_OAntigen_22C_Log_IHS2000_2015");
        bioConditions.add("Enterocolitica_647176_Mutant_rfaH_22C_Log_IHS2000_2015");
        bioConditions.add("Enterocolitica_647176_Mutant_rfaH_37C_Log_IHS2000_2015");
        bioConditions.add("Enterocolitica_647176_WT_22C_Log_IHS2000_2015");
        bioConditions.add("Enterocolitica_647176_WT_37C_Log_IHS2000_2015");
        displayGenomeElementAndBioConditions(partService, "Yersinia enterocolitica Y11", bioConditions, "");
    }
    
    /**
     * Display omics data for Yersinia enterocolitica 8081
     * @param partService
     */
    public static void displayYersi8081(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Enterocolitica_8081_Mutant_yenR_26-37C_5h_SGUL_2008");
        bioConditions.add("Enterocolitica_8081_Mutant_yenR_OverExpr_ytxR_26-37C_5h_SGUL_2008");
        bioConditions.add("Enterocolitica_8081_Mutant_yenR_ytxR_26C_SGUL_2008");
        bioConditions.add("Enterocolitica_8081_Mutant_yenR_ytxR_OverExpr_ytxR_26C_SGUL_2008");
        displayGenomeElementAndBioConditions(partService, "Yersinia enterocolitica 8081", bioConditions, "");
    }
    /**
     * Display omics data for Yersinia ruckeri CSF007
     * @param partService
     */
    public static void displayYersiCSF007(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Ruckeri_CSF007-82_WT_NextSeq500_2019");
        bioConditions.add("Ruckeri_CSF007-82_Mutant_rcsB_NextSeq500_2019");
        displayGenomeElementAndBioConditions(partService, "Yersinia ruckeri QMA0440", bioConditions, "");
    }
    
    /**
     * Load the minimal number of data for Amastigote versus Promastigote view
     */
    public static void displayLeishmaniaAmavsPro(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Amastigote_t0_DNA");
        bioConditions.add("Amastigote_t0_Proteome");
        bioConditions.add("Amastigote_t0_RNA");
        bioConditions.add("Promastigote_passage_2_DNA");
        bioConditions.add("Promastigote_passage_2_Proteome");
        bioConditions.add("Promastigote_passage_2_RNA");
        GenomeTranscriptomeView view =
                displayGenomeElementAndBioConditions(partService, Genome.Donovani_NAME, bioConditions, "");
        view.getTracksComposite().getCompositeAddData().dispose();
        view.getTracksComposite().getCompositeTypeDisplay().dispose();

    }

    /**
     * Load the minimal number of data for Amastigote versus Promastigote view
     */
    public static void displayLeishmaniaLeisheild(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("infDono_CH33p2_P2_DNA");
        bioConditions.add("infDono_CH33p5A_P5rep1_DNA");
        bioConditions.add("infDono_CH34p2_P2_DNA");
        bioConditions.add("infDono_CH34p5A_P5rep1_DNA");
        bioConditions.add("infMon1_ZK27p2_P2_DNA");
        bioConditions.add("infMon1_ZK27p51_P5rep1_DNA");
        bioConditions.add("infMon1_ZK47p2_P2_DNA");
        bioConditions.add("infMon1_ZK47p51_P5rep1_DNA");
        bioConditions.add("infantum_02A_P2_DNA");
        bioConditions.add("infantum_03A_P5_DNA");
        bioConditions.add("infantum_LLM1345p2_P2_DNA");
        bioConditions.add("infantum_LLM1345p5_P5_DNA");
        bioConditions.add("infantum_LLM1356p2_P2_DNA");
        bioConditions.add("infantum_LLM1356p5_P5_DNA");
        displayGenomeElementAndBioConditions(partService, "Leishmania infantum JPCM5 Leisheild", bioConditions, "");
    }

    /**
     * Load the minimal number of data for Amastigote versus Promastigote view
     */
    public static void displayCRISPROmics(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("dCas9_induction");
        displayGenomeElementAndBioConditions(partService, Genome.ECOLI_NAME, bioConditions, "");
    }

    /**
     * Open a GenomeView given a genome and list of biological conditions
     * 
     * @param partService
     * @param genome
     * @param bioConditions
     */
    public static void displayCRISPROmics(EPartService partService, int position) {
        String id = GenomeTranscriptomeView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
        // initiate view
        ResourceManager.openView(partService, GenomeTranscriptomeView.ID, id);
        // update data
        MPart part = partService.findPart(id);
        NavigationManagement.pushStateView(id, new HashMap<>());
        GenomeTranscriptomeView view = (GenomeTranscriptomeView) part.getObject();
        view.getTracksComposite().setParentViewId(id);
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("dCas9_induction");
        // Create your new ProgressMonitorDialog with a IRunnableWithProgress
        try {
            IRunnableWithProgress thread =
                    new OpenBioConditionAndGenomeElementThread(view, Genome.ECOLI_NAME, bioConditions, "");
            new ProgressMonitorDialog(view.shell).run(true, false, thread);
            view.getTracksComposite().setTrack(view.getTrack());
            view.getTracksComposite().getTrack().zoom(true);
            view.getTracksComposite().getTrack().zoom(true);
            view.getTracksComposite().getTrack().zoom(true);
            view.getTracksComposite().getTrack().zoom(true);
            view.getTracksComposite().getTrack().zoom(true);
            view.getTracksComposite().getTrack().zoom(true);
            view.getTracksComposite().getTrack().zoom(true);
            view.getTracksComposite().moveHorizontally(position);
            view.getTracksComposite().getCanvasData().redraw();
            view.getTracksComposite().getCanvasGenome().redraw();
            view.getTracksComposite().setHorizontalBarProperties();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Load the minimal number of data for the simplest proteomics view
     */
    public static GenomeTranscriptomeView displayTestView(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("Macrophages_2014_EGDe");
        bioConditions.add("EGDe_37C_Mean");
        bioConditions.add("EGDe_minusO2");
        bioConditions.add("EGDe_IntestL");
        bioConditions.add("LB_2014_EGDe");
        bioConditions.add("EGDe_Complete_TSS");
        bioConditions.add("sigB_3C_Agilent_2012");

        String id = GenomeTranscriptomeView.ID + Math.random();
        // initiate view
        ResourceManager.openView(partService, GenomeTranscriptomeView.ID, id);
        // update data
        MPart part = partService.findPart(id);
        GenomeTranscriptomeView view = (GenomeTranscriptomeView) part.getObject();

        view.setData(Genome.EGDE_NAME, bioConditions);
        view.updateView();
        return view;
    }

    public TracksComposite getTracksComposite() {
        return tracksComposite;
    }

    public void setTracksComposite(TracksComposite tracksComposite) {
        this.tracksComposite = tracksComposite;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public Composite getContainer() {
        return container;
    }

    public void setContainer(Composite container) {
        this.container = container;
    }

}
