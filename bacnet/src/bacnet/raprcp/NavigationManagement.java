package bacnet.raprcp;

import java.util.HashMap;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.rap.rwt.client.service.UrlLauncher;

import bacnet.Database;
import bacnet.expressionAtlas.HeatMapMultiOmicsView;
import bacnet.expressionAtlas.HeatMapProteomicsView;
import bacnet.expressionAtlas.HeatMapTranscriptomicsView;
import bacnet.expressionAtlas.ProteomicsView;
import bacnet.expressionAtlas.TranscriptomicsView;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.genomeBrowser.NTerminomicsView;
import bacnet.sequenceTools.AnnotationView;
import bacnet.sequenceTools.GeneView;
import bacnet.sequenceTools.GenomicsView;
import bacnet.sequenceTools.SrnaView;
import bacnet.utils.FileUtils;
import bacnet.views.CoExprNetworkView;

public class NavigationManagement {

	public static String SEARCH = "search";
	public static String BUTTON = "button";
	public static String COMBO = "combo";
	public static String CUTOFF = "cutoff";
	public static String LIST = "list";
	public static String LISTNOTINCLUDE = "list2";
	public static String ROW = "row";
	public static String COL = "col";
	public static String ITEM = "item";
	public static String CHROMO = "chromo";
	public static String GENOME = "genome";
	public static String GENE = "gene";
	public static String STYLE = "style";

	public static String CoExprID = "bacnet.e4.rap.CoExprNetworkView";

	/**
	 * Show the help page in an external browser
	 * 
	 * @param url
	 */
	public static void openURLInExternalBrowser(String url, EPartService partService) {
		/*
		 * For eclipse.rap
		 */
		UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
		launcher.openURL(url);
		/*
		 * For eclipse.rcp
		 */
		// Program.launch(url);
	}

	/**
	 * Register service navigation management to be able to use Previous and Next
	 * buttons
	 * 
	 * @param partService
	 */
	public static void registerServiceAndNavigationTab(EPartService partService) {
		/*
		 * For eclipse.rap
		 */
		BrowserNavigation service = RWT.getClient().getService(BrowserNavigation.class);
		service.removeBrowserNavigationListener(Database.getInstance().getNavigationListener());
		service.addBrowserNavigationListener(new BrowserNavigationListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5934778327076792918L;

			@Override
			public void navigated(BrowserNavigationEvent event) {
				String sourceID = event.getSource().getClass().toString();
				String stateID = event.getState();
				// System.out.println("source: "+sourceID+" state: "+stateID);
				/*
				 * Parse State
				 */
				String viewID = stateID;
				if (stateID.contains("?")) {
					viewID = stateID.substring(0, stateID.indexOf('?'));
				}
				Database.getInstance().setCurrentState(stateID);
				MPart part = partService.findPart(viewID);
				if (part != null) {
					partService.showPart(part, PartState.ACTIVATE);
				}
			}
		});

		/*
		 * for eclipse.rcp
		 */
	}

	/**
	 * Return the current URL of your RAP project
	 */
	public static String getURL() {

		/*
		 * for eclise.rap
		 */
		return RWT.getRequest().getRequestURL().toString();

		/*
		 * for eclipse.rcp
		 */
		// return "";
	}

	/**
	 * Push state puttin viewId and state parameters <br>
	 * stateName = viewID + key + "=" + parameters.get(key) + ";"
	 * 
	 * @param viewID
	 * @param parameters
	 */
	public static void pushStateView(String viewID, HashMap<String, String> parameters) {
		/*
		 * for eclipse.rap
		 */
		String stateName = viewID;
		if (parameters.size() > 0)
			stateName += '?';
		for (String key : parameters.keySet()) {
			stateName += key + "=" + parameters.get(key) + ";";
		}
		BrowserNavigation service = RWT.getClient().getService(BrowserNavigation.class);
		service.pushState(stateName, Database.getInstance().getProjectName());

		/*
		 * for eclipse.rcp
		 */
	}

	public static void pushStateView(String viewID) {
		pushStateView(viewID, new HashMap<>());
	}

	/**
	 * Parse init URL and display the correct View with the correct view parameters
	 * 
	 * @param partService
	 */
	public static void parseInitURL(EPartService partService) {
		String state = Database.getInstance().getCurrentState();
		state = FileUtils.cleanStringFromHex(state);
		// System.out.println("Access url: "+getURL());
		// System.out.println("Access door: "+Database.getInstance().getCurrentState());
		if (!state.equals("")) {
			if (state.contains("?")) {
				String viewId = state.substring(0, state.indexOf('?'));
				HashMap<String, String> parameters = parseStateParemeters(state.substring(state.indexOf('?') + 1));
				if (viewId.equals(GenomicsView.ID)) {
					GenomicsView.displayGenomeView(partService, parameters);
				} else if (viewId.equals(TranscriptomicsView.ID)) {
					TranscriptomicsView.displayTranscriptomicsView(partService, parameters);
				} else if (viewId.equals(ProteomicsView.ID)) {
					ProteomicsView.displayProteomicsView(partService, parameters);
				} else if (viewId.equals(CoExprNetworkView.ID)) {
					CoExprNetworkView.displayCoExpNetworkView(partService, parameters);
				} else if (viewId.equals(NTerminomicsView.ID)) {
					NTerminomicsView.displayNTerminomicsView(partService, parameters);
				} else if (viewId.contains(AnnotationView.ID)) {
					AnnotationView.displayAnnotationView(partService, parameters);
				} else if (viewId.contains(GeneView.ID)) {
					GeneView.displayGeneView(partService, parameters);
				} else if (viewId.contains(SrnaView.ID)) {
					SrnaView.displaySrnaView(partService, parameters);
				} else if (viewId.contains(HeatMapMultiOmicsView.ID)) {
					HeatMapMultiOmicsView.displayOmicsView(partService, parameters);
				} else if (viewId.contains(HeatMapTranscriptomicsView.ID)) {
					HeatMapTranscriptomicsView.displayHeatMapTranscriptomicsView(partService, parameters);
				} else if (viewId.contains(HeatMapProteomicsView.ID)) {
					HeatMapProteomicsView.displayHeatMapProteomicsView(partService, parameters);
				} else if (viewId.contains(GenomeTranscriptomeView.ID)) {
					GenomeTranscriptomeView.displayGenomeTranscriptomesView(partService, parameters);
				}
			} else {
				if (state.contains("-")) { // Multi instance view without parameters
					if (state.contains(GeneView.ID)) {
						GeneView.openGeneView(partService);
					}
				} else {
					partService.showPart(state, PartState.ACTIVATE);
				}
			}
		}
	}

	public static HashMap<String, String> parseStateParemeters(String state) {
		HashMap<String, String> parameters = new HashMap<>();
		for (String params : state.split(";")) {
			System.out.println(params.split("=")[0] + " - " + params.split("=")[1]);
			parameters.put(params.split("=")[0], params.split("=")[1]);
		}
		return parameters;
	}
}
