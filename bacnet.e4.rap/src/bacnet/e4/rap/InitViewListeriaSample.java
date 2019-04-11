package bacnet.e4.rap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import bacnet.Database;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Genome.OpenGenomesThread;
import bacnet.expressionAtlas.ProteomicsView;
import bacnet.expressionAtlas.TranscriptomicsView;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.raprcp.NavigationManagement;
import bacnet.sequenceTools.GeneView;
import bacnet.sequenceTools.GenomicsView;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.FileUtils;

public class InitViewListeriaSample implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3252983689419871498L;

	public static final String ID = "bacnet.ListeriaSample"; //$NON-NLS-1$

	/**
	 * Indicates if we focus the view, so we can pushState navigation
	 */
	private boolean focused = false;
	private Button btnBHI37;
	private Button btnLoadData;
	private Button btnSubmitData;
	private Button btnGeneView;
	private Button btnAccessWiki;
	private Link linkUIBC;
	private Link linkPubli;
	private Link linkHUB;
	private Button btnTranscriptomics;
	private Button btnGenomics;
	private Button btnProteomics;
	private Link linkLicenceField;

	@Inject
	EPartService partService;

	@Inject
	EModelService modelService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	public InitViewListeriaSample() {
	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		/**
		 * Uncomment to add Password !!!!!!!!!!
		 */
		if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
			openPasswordDialog();
		}
		focused = true;
		parent.setLayout(new GridLayout(1, false));
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Composite composite_Intro = new Composite(composite, SWT.NONE);
		composite_Intro.setLayout(new GridLayout(2, false));
		GridData gd_composite_Intro = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		gd_composite_Intro.widthHint = 850;
		composite_Intro.setLayoutData(gd_composite_Intro);
		// composite_Intro.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		Label lblListeriomicsIsSo = new Label(composite_Intro, SWT.WRAP);
		lblListeriomicsIsSo.setAlignment(SWT.CENTER);
		// lblListeriomicsIsSo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblListeriomicsIsSo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
		// lblListeriomicsIsSo.setData(RWT.MARKUP_ENABLED, Boolean.TRUE );
		// lblListeriomicsIsSo.setText("Systems biology of the model pathogen
		// <i>Listeria</i>");
		// lblListeriomicsIsSo.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
		lblListeriomicsIsSo.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/LogoListeriomics.png"));
		new Label(composite_Intro, SWT.NONE);
		Label lblNewLabel_1 = new Label(composite_Intro, SWT.WRAP);
		lblNewLabel_1.setAlignment(SWT.CENTER);
		GridData gd_lblNewLabel_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_1.widthHint = 700;
		lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
		lblNewLabel_1.setText(
				"Listeriomics integrates all complete genomes, transcriptomes and proteomes published for <i>Listeria</i> "
						+ "species to date. It allows navigating among all these datasets with enriched metadata in a user-friendly format. "
						+ "Use Listeriomics for deciphering regulatory mechanisms of your genome element of interest.\r");
		// lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblNewLabel_1.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		lblNewLabel_1.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		Composite composite_11 = new Composite(composite, SWT.BORDER);
		GridData gd_composite_11 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_composite_11.widthHint = 850;
		composite_11.setLayoutData(gd_composite_11);
		composite_11.setLayout(new GridLayout(4, false));
		composite_11.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_12 = new Composite(composite_11, SWT.NONE);
		composite_12.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
		composite_12.setLayout(new GridLayout(1, false));
		composite_12.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		Label lblFastAccessTo = new Label(composite_12, SWT.WRAP);
		GridData gd_lblFastAccessTo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblFastAccessTo.widthHint = 140;
		lblFastAccessTo.setLayoutData(gd_lblFastAccessTo);
		lblFastAccessTo.setAlignment(SWT.CENTER);
		lblFastAccessTo.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
		lblFastAccessTo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblFastAccessTo.setText("Multi-omics views");
		Label lblImage = new Label(composite_12, SWT.BORDER);
		lblImage.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/genomeView.png"));

		Composite composite_9 = new Composite(composite_11, SWT.NONE);
		GridData gd_composite_9 = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		gd_composite_9.heightHint = 180;
		composite_9.setLayoutData(gd_composite_9);
		composite_9.setLayout(new GridLayout(1, false));
		composite_9.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnBHI37 = new Button(composite_9, SWT.BORDER);
		btnBHI37.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnBHI37.addSelectionListener(this);
		btnBHI37.setText("Exponential phase");
		btnBHI37.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));

		Label lblIntracellularMouse = new Label(composite_9, SWT.WRAP);
		lblIntracellularMouse.setAlignment(SWT.CENTER);
		GridData gd_lblIntracellularMouse = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblIntracellularMouse.widthHint = 200;
		lblIntracellularMouse.setLayoutData(gd_lblIntracellularMouse);
		lblIntracellularMouse.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		lblIntracellularMouse.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblIntracellularMouse.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		lblIntracellularMouse.setText("Browse RNA-seq, tiling arrays, transcription start sites (TSS), "
				+ "transcription termination sites (TermSeq),"
				+ " and Ribosome Profiling (RiboSeq) of <i>Listeria monocytogenes</i> EGD-e strain grown in BHI at 37°C to exponential phase");
		gd_composite_11.widthHint = 850;
		gd_composite_9.heightHint = 180;
		gd_lblIntracellularMouse.widthHint = 200;
		Composite composite_14 = new Composite(composite_11, SWT.NONE);
		composite_14.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		composite_14.setLayout(new GridLayout(1, false));
		composite_14.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		Label lblAccessToInformation = new Label(composite_14, SWT.WRAP);
		lblAccessToInformation.setAlignment(SWT.CENTER);
		GridData gd_lblAccessToInformation = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblAccessToInformation.widthHint = 140;
		lblAccessToInformation.setLayoutData(gd_lblAccessToInformation);
		lblAccessToInformation.setText("Genes and small non-coding RNAs");
		lblAccessToInformation.setFont(SWTResourceManager.getTitleFont());
		lblAccessToInformation.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		Label lblImage_1 = new Label(composite_14, SWT.BORDER);
		lblImage_1.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/sRNAs.png"));

		Composite composite_15 = new Composite(composite_11, SWT.NONE);
		GridData gd_composite_15 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_15.widthHint = 234;
		composite_15.setLayoutData(gd_composite_15);
		composite_15.setLayout(new GridLayout(1, false));
		composite_15.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		btnGeneView = new Button(composite_15, SWT.BORDER);
		btnGeneView.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		// btnGeneView.setImage(ResourceManager.getPluginImage("bacnet",
		// "icons/InitPage/SystemsBio.png"));
		btnGeneView.addSelectionListener(this);
		btnGeneView.setText("Genes information");
		btnGeneView.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));

		Label lblinfo = new Label(composite_15, SWT.WRAP);
		lblinfo.setAlignment(SWT.CENTER);
		lblinfo.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		lblinfo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		GridData gd_lblinfo = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblinfo.widthHint = 200;
		lblinfo.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		lblinfo.setLayoutData(gd_lblinfo);
		lblinfo.setText("Access to all information about <i>Listeria</i> species genes: "
				+ "functional annotation, gene conservation, synteny, expression atlas and protein atlas. "
				+ "The expression atlas extracts the transcriptomics datasets in which a gene is differently expressed."
				+ " The protein atlas shows in which proteomics datasets a protein has been observed.");

		Composite composite_6 = new Composite(composite, SWT.BORDER);
		GridData gd_composite_6 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_composite_6.widthHint = 850;
		composite_6.setLayoutData(gd_composite_6);
		composite_6.setLayout(new GridLayout(4, false));
		composite_6.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		Composite composite_8 = new Composite(composite_6, SWT.NONE);
		composite_8.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		composite_8.setLayout(new GridLayout(1, false));
		composite_8.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		Label lblBrowseOmicsDatasets = new Label(composite_8, SWT.WRAP);
		lblBrowseOmicsDatasets.setAlignment(SWT.CENTER);
		GridData gd_lblBrowseOmicsDatasets = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblBrowseOmicsDatasets.widthHint = 140;
		lblBrowseOmicsDatasets.setLayoutData(gd_lblBrowseOmicsDatasets);
		lblBrowseOmicsDatasets.setText("Browse omics datasets");
		lblBrowseOmicsDatasets.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblBrowseOmicsDatasets.setFont(SWTResourceManager.getTitleFont());

		Label lblImage_3 = new Label(composite_8, SWT.BORDER);
		lblImage_3.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/heatmap.png"));
		Composite composite_10 = new Composite(composite_6, SWT.NONE);
		GridData gd_composite_10 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_composite_10.heightHint = 180;
		composite_10.setLayoutData(gd_composite_10);
		composite_10.setLayout(new GridLayout(1, false));
		composite_10.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		btnGenomics = new Button(composite_10, SWT.BORDER);
		btnGenomics.setText("Genomics browser");
		btnGenomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnGenomics.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
		btnGenomics.addSelectionListener(this);
		Label lblGoThroughAll = new Label(composite_10, SWT.WRAP);
		lblGoThroughAll.setAlignment(SWT.CENTER);
		GridData gd_lblGoThroughAll = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblGoThroughAll.widthHint = 200;
		lblGoThroughAll.setLayoutData(gd_lblGoThroughAll);
		lblGoThroughAll.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		lblGoThroughAll.setText("Browse all 83 <i>Listeria</i> complete genomes available on Listeriomics."
				+ " Visualize strain relationship in a phylogenomic tree. Access to all their annotated genome elements.");
		lblGoThroughAll.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		lblGoThroughAll.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_21 = new Composite(composite_6, SWT.NONE);
		GridData gd_composite_21 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_composite_21.heightHint = 180;
		composite_21.setLayoutData(gd_composite_21);
		composite_21.setLayout(new GridLayout(1, false));
		composite_21.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		btnTranscriptomics = new Button(composite_21, SWT.BORDER);
		btnTranscriptomics.setText("Transcriptomics browser");
		btnTranscriptomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnTranscriptomics.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
		btnTranscriptomics.addSelectionListener(this);
		Label lblGoThroughAll_1 = new Label(composite_21, SWT.WRAP);
		lblGoThroughAll_1.setAlignment(SWT.CENTER);
		GridData gd_lblGoThroughAll_1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblGoThroughAll_1.widthHint = 200;
		lblGoThroughAll_1.setLayoutData(gd_lblGoThroughAll_1);
		lblGoThroughAll_1.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		lblGoThroughAll_1
				.setText("Browse all 426 <i>Listeria</i> species transcriptomics datasets available on Listeriomics. "
						+ "Visualize them on the genome browser. Extract differently expressed genome elements and display their fold changes in a heatmap viewer.");
		lblGoThroughAll_1.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		lblGoThroughAll_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_22 = new Composite(composite_6, SWT.NONE);
		GridData gd_composite_22 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_composite_22.heightHint = 180;
		composite_22.setLayoutData(gd_composite_22);
		composite_22.setLayout(new GridLayout(1, false));
		composite_22.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		btnProteomics = new Button(composite_22, SWT.BORDER);
		btnProteomics.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnProteomics.setText("Proteomics browser");
		btnProteomics.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
		btnProteomics.addSelectionListener(this);
		Label lblGoThroughAll_2 = new Label(composite_22, SWT.WRAP);
		lblGoThroughAll_2.setAlignment(SWT.CENTER);
		GridData gd_lblGoThroughAll_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblGoThroughAll_2.widthHint = 200;
		lblGoThroughAll_2.setLayoutData(gd_lblGoThroughAll_2);
		lblGoThroughAll_2.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		lblGoThroughAll_2
				.setText("Browse all 76 <i>Listeria</i> species proteomics datasets available on Listeriomics. "
						+ "Visualize them on the genome browser. Display protein detection patterns for each datasets in a heatmap viewer.");
		lblGoThroughAll_2.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		lblGoThroughAll_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_16 = new Composite(composite, SWT.BORDER);
		GridData gd_composite_16 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_composite_16.widthHint = 850;
		composite_16.setLayoutData(gd_composite_16);
		composite_16.setLayout(new GridLayout(4, false));
		composite_16.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		Composite composite_17 = new Composite(composite_16, SWT.NONE);
		composite_17.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 2));
		composite_17.setLayout(new GridLayout(1, false));
		composite_17.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		Label lblListeriomicsSpecificTools = new Label(composite_17, SWT.WRAP);
		lblListeriomicsSpecificTools.setAlignment(SWT.CENTER);
		GridData gd_lblListeriomicsSpecificTools = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblListeriomicsSpecificTools.widthHint = 140;
		lblListeriomicsSpecificTools.setLayoutData(gd_lblListeriomicsSpecificTools);
		lblListeriomicsSpecificTools.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblListeriomicsSpecificTools.setText("Listeriomics specific tools");
		lblListeriomicsSpecificTools.setFont(SWTResourceManager.getTitleFont(SWT.BOLD));
		Label lblImage_2 = new Label(composite_17, SWT.BORDER);
		lblImage_2.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/SysBio.png"));
		Composite composite_20 = new Composite(composite_16, SWT.NONE);
		composite_20.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
		composite_20.setLayout(new GridLayout(1, false));
		composite_20.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		btnAccessWiki = new Button(composite_20, SWT.BORDER);
		btnAccessWiki.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnAccessWiki.setText("Access Listeriomics wiki");
		btnAccessWiki.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));
		btnAccessWiki.addSelectionListener(this);
		Label lblGoToThe = new Label(composite_20, SWT.WRAP);
		lblGoToThe.setAlignment(SWT.CENTER);
		lblGoToThe.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		GridData gd_lblGoToThe = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblGoToThe.widthHint = 200;
		lblGoToThe.setLayoutData(gd_lblGoToThe);
		lblGoToThe.setText(
				"Go to the Listeriomics wiki page for tutorials and description of the different tools included in Listeriomics. "
						+ "Be careful, it might not display if you disallow your internet browser to display pop-up webpage.");
		lblGoToThe.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_18 = new Composite(composite_16, SWT.NONE);
		composite_18.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		composite_18.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		composite_18.setLayout(new GridLayout(1, false));
		btnLoadData = new Button(composite_18, SWT.BORDER | SWT.CENTER);
		btnLoadData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnLoadData.setText(" Load data selection");
		btnLoadData.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txtload.bmp"));
		btnLoadData.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));

		Label lblLoadAPrevious = new Label(composite_18, SWT.WRAP);
		lblLoadAPrevious.setAlignment(SWT.CENTER);
		lblLoadAPrevious.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		GridData gd_lblLoadAPrevious = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblLoadAPrevious.widthHint = 200;
		lblLoadAPrevious.setLayoutData(gd_lblLoadAPrevious);
		lblLoadAPrevious.setText("Load a previous genome viewer visualization saved in .gview file");
		lblLoadAPrevious.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		btnLoadData.addSelectionListener(this);

		Composite composite_5 = new Composite(composite_16, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		composite_5.setLayout(new GridLayout(1, false));
		composite_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		btnSubmitData = new Button(composite_5, SWT.BORDER | SWT.CENTER);
		btnSubmitData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnSubmitData.setText("Submit to Listeriomics");
		btnSubmitData.setFont(SWTResourceManager.getTitleFont(SWT.NORMAL));

		Label lblContactUsIf = new Label(composite_5, SWT.WRAP);
		GridData gd_lblContactUsIf = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblContactUsIf.widthHint = 200;
		lblContactUsIf.setLayoutData(gd_lblContactUsIf);
		lblContactUsIf.setAlignment(SWT.CENTER);
		lblContactUsIf.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		lblContactUsIf.setText(
				"Contact us if you have a recently published \"omics\" dataset you want to be integrated to Listeriomics");
		lblContactUsIf.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		btnSubmitData.addSelectionListener(this);
		new Label(composite_16, SWT.NONE);
		new Label(composite_16, SWT.NONE);
		new Label(composite_16, SWT.NONE);

		Composite composite_19 = new Composite(composite, SWT.NONE);
		composite_19.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		composite_19.setLayout(new GridLayout(1, false));

		Label lblLastUpdate = new Label(composite_19, SWT.NONE);
		lblLastUpdate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
		lblLastUpdate.setText("Last update: July 2017");

		linkPubli = new Link(composite_19, SWT.NONE);
		linkPubli.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		linkPubli.setText("<a>Cite Listeriomics</a>");
		linkPubli.addSelectionListener(this);

		Link link = new Link(composite_19, SWT.NONE);
		link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		link.setText("Contact us: <a>listeriomics AT pasteur DOT fr</a>");

		new Label(composite_19, SWT.NONE);

		Label lblImagelicence = new Label(composite_19, SWT.CENTER);
		lblImagelicence.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblImagelicence.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/cccommons.png"));

		Label lblThisWorkIs = new Label(composite_19, SWT.NONE);
		lblThisWorkIs.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblThisWorkIs.setAlignment(SWT.CENTER);
		lblThisWorkIs.setText("This work is licensed under");
		linkLicenceField = new Link(composite_19, SWT.NONE);
		linkLicenceField.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		linkLicenceField.setText("<a>Creative Commons Attribution 4.0 International License</a>");
		linkLicenceField.addSelectionListener(this);

		new Label(composite_19, SWT.NONE);

		Label lblCredits = new Label(composite_19, SWT.NONE);
		lblCredits.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblCredits.setAlignment(SWT.CENTER);
		lblCredits.setText("Credits");
		linkUIBC = new Link(composite_19, SWT.NONE);
		linkUIBC.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		linkUIBC.setText("<a>Unité des Intéractions Bactéries-Cellules, Institut Pasteur, Paris</a>");
		linkUIBC.addSelectionListener(this);
		linkHUB = new Link(composite_19, SWT.NONE);
		linkHUB.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		linkHUB.setText("<a>Bioinformatics and Biostatistics HUB, Institut Pasteur, Paris</a>");
		linkHUB.addSelectionListener(this);

		Label lblBacnet_1 = new Label(composite_19, SWT.NONE);
		lblBacnet_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblBacnet_1.setText("BACNET 10-BINF-02-01");

		Composite composite_3 = new Composite(composite_19, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_3.setLayout(new GridLayout(4, false));

		Label lblPasteur = new Label(composite_3, SWT.NONE);
		lblPasteur.setSize(0, 15);
		lblPasteur.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPasteur.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/Pasteur.png"));

		Label lblInra = new Label(composite_3, SWT.NONE);
		lblInra.setSize(0, 15);
		lblInra.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblInra.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/INRA.png"));
		Label lblAnr = new Label(composite_3, SWT.NONE);
		lblAnr.setSize(0, 15);
		lblAnr.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/Logo ANR.png"));

		Label lblBacnet = new Label(composite_3, SWT.NONE);
		lblBacnet.setSize(0, 15);
		lblBacnet.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/investissement-davenir.png"));

		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		pushState();

	}

	public boolean openPasswordDialog() {
		InputDialog dialog = new InputDialog(shell, "Type password", "Need to type a password to enter website", "",
				null);
		if (dialog.open() == 0) {
			// System.out.println(dialog.getValue());
			String password = "EGDe";
			if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
				password = "BecChris";
			}

			if (dialog.getValue().equals(password)) {
				System.out.println("Congratulation you can enter the gate!");
				return true;
			} else {
				openPasswordDialog();
			}
		} else {
			openPasswordDialog();
		}
		return false;
	}

	@Focus
	public void onFocus() {
		if (!focused) {
			pushState();
			focused = true;
		} else {
			focused = false;
		}
	}

	/**
	 * Push genome, chromosome, gene and Tabitem state
	 */
	public void pushState() {
		NavigationManagement.pushStateView(InitViewListeriaSample.ID);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnSubmitData) {
			partService.showPart(SubmitDataView.ID, PartState.ACTIVATE);
			NavigationManagement.pushStateView(SubmitDataView.ID);
		} else if (e.getSource() == btnBHI37) {
			GenomeTranscriptomeView.displayBHI37View(partService);
		} else if (e.getSource() == btnProteomics) {
			try {
				ArrayList<String> genomeNames = new ArrayList<>();
				genomeNames.add(Genome.EGDE_NAME);
				OpenGenomesThread thread = new OpenGenomesThread(genomeNames);
				new ProgressMonitorDialog(this.shell).run(true, false, thread);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			partService.showPart(ProteomicsView.ID, PartState.ACTIVATE);
			NavigationManagement.pushStateView(ProteomicsView.ID);
		} else if (e.getSource() == btnGenomics) {
			partService.showPart(GenomicsView.ID, PartState.ACTIVATE);
			NavigationManagement.pushStateView(GenomicsView.ID);
		} else if (e.getSource() == btnTranscriptomics) {
			try {
				ArrayList<String> genomeNames = new ArrayList<>();
				genomeNames.add(Genome.EGDE_NAME);
				OpenGenomesThread thread = new OpenGenomesThread(genomeNames);
				new ProgressMonitorDialog(this.shell).run(true, false, thread);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			partService.showPart(TranscriptomicsView.ID, PartState.ACTIVATE);
			NavigationManagement.pushStateView(TranscriptomicsView.ID);
		} else if (e.getSource() == btnAccessWiki) {
			UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
			launcher.openURL("https://listeriomics.pasteur.fr/WikiListeriomics/index.php/Summary");
		} else if (e.getSource() == btnGeneView) {
			GeneView.openGeneView(partService);
		} else if (e.getSource() == linkPubli) {
			String url = "http://msystems.asm.org/content/2/2/e00186-16";
			NavigationManagement.openURLInExternalBrowser(url, partService);
		} else if (e.getSource() == linkUIBC) {
			String url = "https://research.pasteur.fr/en/team/bacteria-cell-interactions/";
			NavigationManagement.openURLInExternalBrowser(url, partService);
		} else if (e.getSource() == linkHUB) {
			String url = "https://research.pasteur.fr/en/team/bioinformatics-and-biostatistics-hub/";
			NavigationManagement.openURLInExternalBrowser(url, partService);
		} else if (e.getSource() == linkLicenceField) {
			String url = "http://creativecommons.org/licenses/by/4.0/";
			NavigationManagement.openURLInExternalBrowser(url, partService);
		} else if (e.getSource() == btnLoadData) {
			try {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open .gview file of your previous session");
				String fileName = fd.open();
				if (fileName != null) {
					String textTrack = FileUtils.readText(fileName);
					GenomeTranscriptomeView.displayBioConditionsFromText(partService, textTrack);
				}
			} catch (Exception e1) {
				System.out.println("Cannot read the list of data");
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}
}
