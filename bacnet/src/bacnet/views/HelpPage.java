package bacnet.views;

import org.eclipse.e4.ui.workbench.modeling.EPartService;

import bacnet.raprcp.NavigationManagement;

// import org.eclipse.rap.rwt.RWT;
// import org.eclipse.rap.rwt.client.service.UrlLauncher;

public class HelpPage {

	private static String URL_HELP = "https://listeriomics.pasteur.fr/WikiListeriomics/";

	public static void helpSubmit(EPartService partService) {
		String url = URL_HELP + "index.php/Submit_Data";
		showHelp(url, partService);
	}

	public static void helpGeneView(EPartService partService) {
		String url = URL_HELP + "index.php/Gene_tools";
		showHelp(url, partService);
	}

	public static void helpSrnaView(EPartService partService) {
		String url = URL_HELP + "index.php/Small_RNA_tools";
		showHelp(url, partService);
	}

	public static void helpGenomeView(EPartService partService) {
		String url = URL_HELP + "index.php/Genomic_tools";
		showHelp(url, partService);
	}

	public static void helpTrancriptomicView(EPartService partService) {
		String url = URL_HELP + "index.php/Transcriptomic_tools";
		showHelp(url, partService);
	}

	public static void helpProteomicView(EPartService partService) {
		String url = URL_HELP + "index.php/Proteomic_tools";
		showHelp(url, partService);
	}

	public static void helpGenomeViewer(EPartService partService) {
		String url = URL_HELP + "index.php/Genome_viewer";
		showHelp(url, partService);
	}

	public static void helpTranscriptHeatMapViewer(EPartService partService) {
		String url = URL_HELP + "index.php/Transcriptomic_HeatMap_viewer";
		showHelp(url, partService);
	}

	public static void helpProteomHeatMapViewer(EPartService partService) {
		String url = URL_HELP + "index.php/Proteomic_HeatMap_viewer";
		showHelp(url, partService);
	}

	public static void helpNetworkView(EPartService partService) {
		String url = URL_HELP + "index.php/Co-Expression_network_tools";
		showHelp(url, partService);
	}

	/**
	 * Show the help page in an external browser
	 * 
	 * @param url
	 */
	public static void showHelp(String url, EPartService partService) {
		NavigationManagement.openURLInExternalBrowser(url, partService);
	}
}
