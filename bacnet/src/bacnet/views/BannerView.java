package bacnet.views;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import bacnet.swt.ResourceManager;
import bacnet.utils.BasicColor;

public class BannerView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3803453895078156914L;
	public static final String ID = "bacnet.BannerView"; //$NON-NLS-1$
	private ToolItem tltmProteomics;
	private ToolItem tltmTranscriptomics;
	private ToolItem tltmGenomics;
	/**
	 * In this Canvas we will draw the Banner of the website/software
	 */
	private Canvas canvas;
	private ToolItem tltmAbout;

	@Inject
	public BannerView() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new GridLayout(2, false));

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
				Image image = ResourceManager.getPluginImage("bacnet", "icons/ToolBar/LogoListeriomics.png");
				int xPosition = event.x + 20;
				int yPosition = event.y + event.height / 2 - image.getBounds().height / 2;
				event.gc.drawImage(image, xPosition, yPosition);
			}
		});

		ToolBar toolBar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);
		tltmProteomics = new ToolItem(toolBar, SWT.NONE);
		tltmProteomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/Proteomics.png"));
		tltmProteomics.addSelectionListener(this);
		tltmTranscriptomics = new ToolItem(toolBar, SWT.NONE);
		tltmTranscriptomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/Transcriptomics.png"));
		tltmTranscriptomics.addSelectionListener(this);
		tltmGenomics = new ToolItem(toolBar, SWT.NONE);
		tltmGenomics.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/genomics.png"));
		tltmGenomics.addSelectionListener(this);

		ToolItem toolItem_3 = new ToolItem(toolBar, SWT.SEPARATOR);

		tltmAbout = new ToolItem(toolBar, SWT.NONE);
		tltmAbout.setImage(ResourceManager.getPluginImage("bacnet", "icons/ToolBar/howto.png"));
		tltmAbout.addSelectionListener(this);

		createActions();
		initializeToolBar();
		initializeMenu();
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		// if(e.getSource()==tltmProteomics){
		// MPart part = partService.createPart(ProteomicsView.ID);
		// partService.showPart(part, PartState.ACTIVATE);
		//// ProteomicsView.displayProteomicsView();
		// }else if(e.getSource()==tltmGenomics){
		// page.showView(GenomicsView.ID);
		// }else if(e.getSource()==tltmTranscriptomics){
		// page.showView(TranscriptomicsView.ID);
		// }else if(e.getSource()==tltmAbout){
		// try {
		// PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new
		// URL("http://wiki.listeriomics.com/Main_Page"));
		// } catch (PartInitException ef) {
		// ef.printStackTrace();
		// } catch (MalformedURLException ef) {
		// ef.printStackTrace();
		// }
		// }
		// } catch (PartInitException e1) {
		// e1.printStackTrace();
		// }
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}
}
