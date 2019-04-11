package bacnet.e4.rap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import bacnet.Database;
import bacnet.expressionAtlas.ProteomicsView;
import bacnet.expressionAtlas.TranscriptomicsView;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.sequenceTools.GenomicsView;
import bacnet.swt.ResourceManager;
import bacnet.utils.BasicColor;

public class BannerView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8818752955596747659L;

	public static String ID = "bacnet.e4.rap.BannerView";

	private ToolItem tltmProteomics;
	private ToolItem tltmTranscriptomics;
	private ToolItem tltmGenomics;
	private ToolItem tltmGitlab;
	private ToolItem tltmAbout;

	/**
	 * In this Canvas we will draw the Banner of the website/software
	 */
	private Canvas canvas;
	private ToolBar toolBar;

	@Inject
	EPartService partService;

	@Inject
	EModelService modelService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	private ToolItem tltmPrintScreen;

	@Inject
	public BannerView() {
		// System.out.println("initbanner");
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {

		SessionControl.initBacnetApp(partService, modelService, shell);
		String appName = Database.getInstance().getProjectName();

		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new GridLayout(3, false));

		canvas = new Canvas(container, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		canvas.setLayout(new GridLayout(1, false));
		/*
		 * Paint the Banner of the software/website
		 */
		canvas.addPaintListener(new PaintListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1241919382522062960L;

			@Override
			public void paintControl(PaintEvent event) {
				event.gc.setBackground(BasicColor.WHITE);
				event.gc.setForeground(BasicColor.LIGHTGREY);
				event.gc.fillGradientRectangle(0, 0, event.width, event.height, false);
				// event.gc.setBackground(BasicColor.WHITE);
				// event.gc.setForeground(BasicColor.BANNER_BACKGROUND);
				// event.gc.fillGradientRectangle(0, event.height/2, event.width,
				// event.height/2, true);
				Image image = ResourceManager.getPluginImage("bacnet", Database.getInstance().getLogo());
				int xPosition = event.x + 20;
				int yPosition = event.y + event.height / 2 - image.getBounds().height / 2;
				event.gc.drawImage(image, xPosition, yPosition);
			}
		});

		toolBar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);

		tltmGenomics = new ToolItem(toolBar, SWT.NONE);
		tltmGenomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/genomics.png"));
		tltmGenomics.addSelectionListener(this);
		tltmTranscriptomics = new ToolItem(toolBar, SWT.NONE);
		tltmTranscriptomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/Transcriptomics.png"));
		tltmTranscriptomics.addSelectionListener(this);
		tltmProteomics = new ToolItem(toolBar, SWT.NONE);
		tltmProteomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/Proteomics.png"));
		tltmProteomics.addSelectionListener(this);

		if (appName.equals(Database.BACNET)) {
			tltmGitlab = new ToolItem(toolBar, SWT.NONE);
			tltmGitlab.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/gitlab.png"));
			tltmGitlab.addSelectionListener(this);
			tltmGitlab.setToolTipText("Access source code on GitLab.pasteur.fr");

		}

		new ToolItem(toolBar, SWT.SEPARATOR);

		tltmAbout = new ToolItem(toolBar, SWT.NONE);
		tltmAbout.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/howto.png"));
		tltmAbout.addSelectionListener(this);

		tltmPrintScreen = new ToolItem(toolBar, SWT.NONE);
		tltmPrintScreen.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/printscreen.png"));
		tltmPrintScreen.addSelectionListener(this);

		if (appName.equals(Database.CRISPRGO_PROJECT) || appName.equals(Database.BACNET)) {
			tltmGenomics.dispose();
			tltmTranscriptomics.dispose();
			tltmProteomics.dispose();
		}

		System.out.println("BannerView loaded");

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == tltmProteomics) {
			partService.showPart(ProteomicsView.ID, PartState.ACTIVATE);
			NavigationManagement.pushStateView(ProteomicsView.ID);
		} else if (e.getSource() == tltmGenomics) {
			partService.showPart(GenomicsView.ID, PartState.ACTIVATE);
			NavigationManagement.pushStateView(GenomicsView.ID);
		} else if (e.getSource() == tltmTranscriptomics) {
			partService.showPart(TranscriptomicsView.ID, PartState.ACTIVATE);
			NavigationManagement.pushStateView(TranscriptomicsView.ID);
		} else if (e.getSource() == tltmPrintScreen) {
			SaveFileUtils.saveControltoPNG("body", Database.getInstance().getProjectName(), shell);
		} else if (e.getSource() == tltmAbout) {
			UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
			launcher.openURL("https://gitlab.pasteur.fr/bacnet/Bacnet-public/wikis/home");
		} else if (e.getSource() == tltmGitlab) {
			UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
			launcher.openURL("https://gitlab.pasteur.fr/bacnet/Bacnet-public");
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public void setToolBar(ToolBar toolBar) {
		this.toolBar = toolBar;
	}
}
