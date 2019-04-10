
package bacnet.e4.rap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import bacnet.genomeBrowser.CRISPRGuideView;
import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.raprcp.NavigationManagement;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.views.CRISPRPredictView;

public class InitViewCRISPR implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5364834050007950311L;

	public static final String ID = "bacnet.CRISPROmics"; //$NON-NLS-1$

	private Button btnLoadCrisprSeq;
	private Button btnPredictCrisprGuide;
	private Button btnTableGuide;
	private Link linkUIBC;
	private Link linkPubli;
	private Link linkHUB;
	private Link linkLicenceField;

	private ScrolledComposite scrolledComposite;

	@Inject
	EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	public InitViewCRISPR() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {

		parent.setLayout(new GridLayout(1, false));
		scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Composite composite_8 = new Composite(composite, SWT.NONE);
		composite_8.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_8.setLayout(new GridLayout(2, false));
		composite_8.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_1 = new Composite(composite_8, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		composite_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 3));
		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("CRISPRbrowser - CRISPR design tools for bacteria");
		lblNewLabel.setFont(SWTResourceManager.getTitleFont());
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Label lblDescription = new Label(composite_1, SWT.WRAP);
		GridData gd_lblDescription = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblDescription.widthHint = 400;
		gd_lblDescription.heightHint = 150;
		lblDescription.setLayoutData(gd_lblDescription);
		lblDescription.setText("We performed a CRISPR-dCas9 screen using a pool of ~92,000 sgRNAs in "
				+ "E. coli MG1655 during growth in rich medium for 17 generations. During this experiment, "
				+ "guides that reduce the cell fitness, for instance by blocking the expression of essential "
				+ "genes, are depleted from the library. For each guide, a log2-transformed fold change value"
				+ " (log2FC) represents the enrichment or depletion over the course of the experiment.");
		lblDescription.setFont(SWTResourceManager.getBodyFont(SWT.NORMAL));
		lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_7 = new Composite(composite_8, SWT.NONE);
		composite_7.setLayout(new GridLayout(1, false));
		composite_7.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnLoadCrisprSeq = new Button(composite_7, SWT.NONE);
		btnLoadCrisprSeq.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/crispromics.png"));
		btnLoadCrisprSeq.addSelectionListener(this);

		Label lblStationnaryPhaseData = new Label(composite_7, SWT.NONE);
		lblStationnaryPhaseData.setAlignment(SWT.CENTER);
		lblStationnaryPhaseData.setText("Omics viewer : CRISPR screen in E. coli K-12");
		lblStationnaryPhaseData.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
		lblStationnaryPhaseData.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_18 = new Composite(composite_8, SWT.NONE);
		composite_18.setLayout(new GridLayout(1, false));
		composite_18.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnTableGuide = new Button(composite_18, SWT.NONE);
		btnTableGuide.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/crispr-table.png"));
		btnTableGuide.addSelectionListener(this);

		Label lblGuideTable = new Label(composite_18, SWT.NONE);
		lblGuideTable.setAlignment(SWT.CENTER);
		lblGuideTable.setText("CRISPR guide table");
		lblGuideTable.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
		lblGuideTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_17 = new Composite(composite_8, SWT.NONE);
		composite_17.setLayout(new GridLayout(1, false));
		composite_17.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnPredictCrisprGuide = new Button(composite_17, SWT.NONE);
		btnPredictCrisprGuide
				.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/crispr-prediction.png"));
		btnPredictCrisprGuide.addSelectionListener(this);

		Label lblPredict = new Label(composite_17, SWT.NONE);
		lblPredict.setAlignment(SWT.CENTER);
		lblPredict.setText("CRISPR guide prediction tool");
		lblPredict.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
		lblPredict.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		// fontData = new FontData("Arial", 12, SWT.BOLD);

		Composite composite_10 = new Composite(composite, SWT.NONE);
		composite_10.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true, 2, 1));
		composite_10.setLayout(new GridLayout(1, false));

		Label lblLastUpdate = new Label(composite_10, SWT.NONE);
		lblLastUpdate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblLastUpdate.setText("Last update: 2018-10-16");

		linkPubli = new Link(composite_10, SWT.NONE);
		linkPubli.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		linkPubli.setText("<a>Cite CRISPRbrowser</a>");
		linkPubli.addSelectionListener(this);

		Link link = new Link(composite_10, SWT.NONE);
		link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		link.setText("Contact us: <a>david.bikard AT pasteur DOT fr</a>");

		Label label = new Label(composite_10, SWT.NONE);

		Label lblCredits = new Label(composite_10, SWT.NONE);
		lblCredits.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblCredits.setAlignment(SWT.CENTER);
		lblCredits.setText("Credits");
		linkUIBC = new Link(composite_10, SWT.NONE);
		linkUIBC.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		linkUIBC.setText("<a>Synthetic Biology, Junior Group, Institut Pasteur, Paris</a>");
		linkUIBC.addSelectionListener(this);
		linkHUB = new Link(composite_10, SWT.NONE);
		linkHUB.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		linkHUB.setText("<a>Bioinformatics and Biostatistics HUB, Institut Pasteur, Paris</a>");
		linkHUB.addSelectionListener(this);

		Composite composite_3 = new Composite(composite_10, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_3.setLayout(new GridLayout(4, false));

		Label lblPasteur = new Label(composite_3, SWT.NONE);
		lblPasteur.setSize(0, 15);
		lblPasteur.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPasteur.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/c3bi.png"));

		Label lblAnr = new Label(composite_3, SWT.NONE);
		lblAnr.setSize(0, 15);
		lblAnr.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/erc_logo.png"));
		new Label(composite_3, SWT.NONE);
		new Label(composite_3, SWT.NONE);

		Label label_1 = new Label(composite_10, SWT.NONE);

		Label lblImagelicence = new Label(composite_10, SWT.CENTER);
		lblImagelicence.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblImagelicence.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/cccommons.png"));

		Label lblThisWorkIs = new Label(composite_10, SWT.NONE);
		lblThisWorkIs.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblThisWorkIs.setAlignment(SWT.CENTER);
		lblThisWorkIs.setText("This work is licensed under");
		linkLicenceField = new Link(composite_10, SWT.NONE);
		linkLicenceField.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		linkLicenceField.setText("<a>Creative Commons Attribution 4.0 International License</a>");
		linkLicenceField.addSelectionListener(this);
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		composite_17.dispose();

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnLoadCrisprSeq) {
			GenomeTranscriptomeView.displayCRISPROmics(partService);
		} else if (e.getSource() == btnTableGuide) {
			partService.showPart(CRISPRGuideView.ID, PartState.ACTIVATE);
			NavigationManagement.pushStateView(CRISPRGuideView.ID);
		} else if (e.getSource() == btnPredictCrisprGuide) {
			MPart part = partService.createPart(CRISPRPredictView.ID);
			partService.showPart(part, PartState.ACTIVATE);
		} else if (e.getSource() == linkPubli) {
			String url = "https://www.ncbi.nlm.nih.gov/pubmed/30403660";
			NavigationManagement.openURLInExternalBrowser(url, partService);
		} else if (e.getSource() == linkUIBC) {
			String url = "https://research.pasteur.fr/en/team/synthetic-biology/";
			NavigationManagement.openURLInExternalBrowser(url, partService);
		} else if (e.getSource() == linkHUB) {
			String url = "https://research.pasteur.fr/en/team/bioinformatics-and-biostatistics-hub/";
			NavigationManagement.openURLInExternalBrowser(url, partService);
		} else if (e.getSource() == linkLicenceField) {
			String url = "http://creativecommons.org/licenses/by/4.0/";
			NavigationManagement.openURLInExternalBrowser(url, partService);
		}

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
