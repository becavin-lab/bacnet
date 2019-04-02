package bacnet.e4.rap.setup;

import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;
import org.eclipse.swt.widgets.Shell;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.Network;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Srna;
import bacnet.expressionAtlas.HeatMapProteomicsView;
import bacnet.expressionAtlas.HeatMapTranscriptomicsView;
import bacnet.expressionAtlas.ProteomicsView;
import bacnet.expressionAtlas.TranscriptomicsView;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.genomeBrowser.core.Track;
import bacnet.genomeBrowser.tracksGUI.TracksComposite;
import bacnet.sequenceTools.GeneView;
import bacnet.sequenceTools.GenomicsView;
import bacnet.sequenceTools.SrnaSummaryView;
import bacnet.sequenceTools.SrnaView;
import bacnet.table.core.ColorMapperList;
import bacnet.views.CoExprNetworkView;

public class SessionControl {



    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    Shell shell;

    public SessionControl() {}

    /**
     * Performed all initialisation necessary at the running of the webapp<br>
     * Is runned in BannerView.createPartControl()
     * 
     * @param partService
     * @param modelService
     * @param shell
     */
    public static void initBacnetApp(EPartService partService, EModelService modelService, Shell shell) {
        Database database = Database.getInstance();
        database.setProjectName(BasicApplication.projectName);
        database.initDatabase(shell);
    }

    public void testApplicationContext(EPartService partService) {
        String sessionInfo = "";
        sessionInfo = RWT.getApplicationContext().toString() + "\n";
        sessionInfo += RWT.getClient().toString() + "\n";
        sessionInfo += RWT.getUISession().toString() + "\n";
        Genome genome = Genome.loadEgdeGenome();
        sessionInfo += genome.toString();
        // sessionInfo += RWT.getUISession().getHttpSession().toString()+"\n";
        // System.out.println(sessionInfo);

        MessageDialog.openInformation(shell, "Session info", sessionInfo);
    }

    public static void setExitConfirmation() {
        ExitConfirmation service = RWT.getClient().getService(ExitConfirmation.class);
        service.setMessage("Do you really want to leave Listeriomics ?");
    }

    public static void registerClosingUIsession(EPartService partService, EModelService modelService, Shell shell) {


        RWT.getUISession().addUISessionListener(new UISessionListener() {
            /**
             * 
             */
            private static final long serialVersionUID = 4774888041292630588L;

            public void beforeDestroy(UISessionEvent event) {

                for (MPart part : partService.getParts()) {
                    // System.out.println(part.getElementId());
                    if (part.getObject() != null) {
                        // System.out.println("Cleaning - "+part);
                        // System.out.println(part.getObject());
                        if (part.getElementId().contains("GenomeTranscriptomeView")) {
                            GenomeTranscriptomeView view = (GenomeTranscriptomeView) part.getObject();
                            TracksComposite trackComposite = null;
                            view.setTracksComposite(trackComposite);
                            Track track = view.getTrack();
                            track.getChromosome().clearChromosome();
                            track.getDatas().getBioConditionHashMaps().clear();
                            track.getDatas().setDisplay(new boolean[0]);
                            view.setTrack(track);
                            // partService.hidePart(part, true);
                        } else if (part.getElementId().contains("CoExprNetworkView")) {
                            CoExprNetworkView view = (CoExprNetworkView) part.getObject();
                            view.setGenome(null);
                            view.setGeneralNetwork(new Network());
                            view.setFilteredNetwork(new Network());
                        } else if (part.getElementId().contains("GenomicsView")) {
                            GenomicsView view = (GenomicsView) part.getObject();
                            view.setBioCondsArray(new String[0][0]);
                            view.setBioCondsToDisplay(new ArrayList<>());
                            view.setColumnNames(new ArrayList<>());
                        } else if (part.getElementId().contains("bacnet.TranscriptomicsView")) {
                            TranscriptomicsView view = (TranscriptomicsView) part.getObject();
                            view.setBioConds(new ArrayList<>());
                            view.setBioCondsArray(new String[0][0]);
                            view.setBioCondsToDisplay(new ArrayList<>());
                            view.setColumnNames(new ArrayList<>());
                        } else if (part.getElementId().contains("bacnet.ProteomicsView")) {
                            ProteomicsView view = (ProteomicsView) part.getObject();
                            view.setBioConds(new ArrayList<>());
                            view.setBioCondsArray(new String[0][0]);
                            view.setBioCondsToDisplay(new ArrayList<>());
                            view.setColumnNames(new ArrayList<>());
                        } else if (part.getElementId().contains("SrnaView")) {
                            SrnaView view = (SrnaView) part.getObject();
                            view.setArrayDataList(new String[0][0]);
                            view.setSeq(new Srna());
                            view.setListSrnas(new ArrayList<>());
                        } else if (part.getElementId().contains("GeneView")) {
                            GeneView view = (GeneView) part.getObject();
                            view.setArrayDataList(new String[0][0]);
                            view.setArrayGeneToLocalization(new String[0][0]);
                            view.setArrayProteomeList(new String[0][0]);
                            view.setBioConds(new ArrayList<>());
                            view.setBioCondsArray(new String[0][0]);
                            view.setBioCondsToDisplay(new ArrayList<>());
                            view.getGenome().clearGenome();
                            view.setArrayGeneToLocalization(new String[0][0]);
                        } else if (part.getElementId().contains("SrnaSummaryView")) {
                            SrnaSummaryView view = (SrnaSummaryView) part.getObject();
                            view.setArray(new String[0][0]);
                        } else if (part.getElementId().contains("bacnet.HeatMapTranscriptomicsView")) {
                            HeatMapTranscriptomicsView view = (HeatMapTranscriptomicsView) part.getObject();
                            view.getTableComposite().setMatrix(new ExpressionMatrix());
                            view.getTableComposite().setMatrixDisplayed(new ExpressionMatrix());
                            view.getTableComposite().getExcludeColumn().clear();
                            view.getTableComposite().getExcludeRow().clear();
                            view.getTableComposite().getColumnNames().clear();
                            view.getTableComposite().setColorMapperList(new ColorMapperList());
                        } else if (part.getElementId().contains("bacnet.HeatMapProteomicsView")) {
                            HeatMapProteomicsView view = (HeatMapProteomicsView) part.getObject();
                            view.getTableComposite().setMatrix(new ExpressionMatrix());
                            view.getTableComposite().setMatrixDisplayed(new ExpressionMatrix());
                            view.getTableComposite().getExcludeColumn().clear();
                            view.getTableComposite().getExcludeRow().clear();
                            view.getTableComposite().getColumnNames().clear();
                            view.getTableComposite().setColorMapperList(new ColorMapperList());
                        }
                    }
                }
                System.out.println("Session closing");
                // System.out.println("Singleton ID: "+SingletonUtil.getSessionInstance(Database.class));
                Database database = Database.getInstance();
                database.cleanUpDatabase();
                // System.out.println(database.toString());

            }
        });
    }

    public static void closeGenomeViewer(EPartService partService) {
        for (MPart part : partService.getParts()) {
            System.out.println(part.getContributionURI());
            if (part.getElementId().contains("GenomeTranscriptomeView")) {
                GenomeTranscriptomeView view = (GenomeTranscriptomeView) part.getObject();
                // Track track = view.getTrack();
                // System.out.println("Track : "+track);
                // track = new Track();
                // track = null;

                partService.hidePart(part, true);
                System.out.println("Genome closed");
            }
        }
    }

}
