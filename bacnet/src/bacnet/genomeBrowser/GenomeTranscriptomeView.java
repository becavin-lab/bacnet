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
import org.eclipse.jface.dialogs.InputDialog;
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
        track = new Track(genome, firstChromoID);
        int i = 1;
        for (String bioCondName : bioCondNames) {
            /*
             * Add and load BioCondition
             */
            monitor.subTask("Loading datasets " + i + "/" + bioCondNames.size() + " : " + bioCondName);
            track.getDatas().addBioCondition(bioCondName);
            monitor.worked(1);
            i++;
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
            ArrayList<String> comparisons, String genomeElement) {
        String id = GenomeTranscriptomeView.ID + "-" + String.valueOf(Math.random() * 1000).substring(0, 3);
        // initiate view
        ResourceManager.openView(partService, GenomeTranscriptomeView.ID, id);
        // update data
        MPart part = partService.findPart(id);
        NavigationManagement.pushStateView(id, new HashMap<>());
        GenomeTranscriptomeView view = (GenomeTranscriptomeView) part.getObject();
        view.getTracksComposite().setParentViewId(id);
        ArrayList<String> bioConds = new ArrayList<>();
        for (String comparison : comparisons) {
            if (comparison.contains(BioCondition.SEPARATOR)) {
                bioConds.add(comparison.split(BioCondition.SEPARATOR)[0]);
            } else
                bioConds.add(comparison);
        }
        // Create your new ProgressMonitorDialog with a IRunnableWithProgress
        try {
            IRunnableWithProgress thread =
                    new OpenBioConditionAndGenomeElementThread(view, genome, bioConds, genomeElement);
            new ProgressMonitorDialog(view.shell).run(true, false, thread);
            /*
             * Init Composite
             */
            view.getTracksComposite().setTrack(view.getTrack());
            if (!genomeElement.equals("")) {
                view.getTracksComposite().search(genomeElement);
                try {
                    int position = Integer.parseInt(genomeElement);
                } catch (Exception e) {
                    view.getTracksComposite().displaySpecificRegion(genomeElement);
                }
            }
        } catch (InvocationTargetException ex) {
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
        ArrayList<String> bioCondtions = new ArrayList<>();
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
            if (bioCondName.contains(" vs ")) {
                bioConds.add(bioCondName.split(" vs ")[0]);
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
            int sizeProcess = 1 + bioConditions.size();
            // Tell the user what you are doing
            monitor.beginTask("Loading datasets for Genome Viewer", sizeProcess);

            // Optionally add subtasks
            monitor.subTask("Loading genome: " + genome);
            monitor.worked(1);
            view.setData(genome, bioConditions);
            monitor.worked(1);
            view.updateView(monitor);
            // You are done
            monitor.done();
        }

    }

    /**
     * Load the minimal number of data for the simplest proteomics view
     */
    public static void displayBHI37View(EPartService partService) {
        ArrayList<String> bioConditions = new ArrayList<>();
        bioConditions.add("EGDe_270407");
        bioConditions.add("BHI_2014_EGDe");
        bioConditions.add("EGDe_37C_TSS");
        bioConditions.add("EGDe_37C_RiboSeq");
        bioConditions.add("EGDe_37C_TermSeq");
        // bioConditions.add("EGDe_37C_Weizmann");
        // bioConditions.add("Stat_2009_EGDe");

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
        GenomeTranscriptomeView view = displayGenomeElementAndBioConditions(partService, Genome.Donovani_NAME, bioConditions, "");
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
