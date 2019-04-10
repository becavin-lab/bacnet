package bacnet.views;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;

public class InitView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2416453633652904396L;
	public static final String ID = "bacnet.InitView"; //$NON-NLS-1$
	private Button btnBHI37;
	private Button btnSrnas;
	private Label lblTzest;
	private Button btnStat;
	private Button btnIntracellular;

	@Inject
	private EPartService partService;

	@Inject
	public InitView() {

		/**
		 * Uncomment to add Password !!!!!!!!!!
		 */
		// openDialog();
	}

	// public boolean openDialog(){
	// InputDialog dialog = new
	// InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
	// "Type password",
	// "Need to type a password to enter website", "", null);
	// if(dialog.open()==0){
	// //System.out.println(dialog.getValue());
	// if(dialog.getValue().equals("BecChris")){
	// System.out.println("Congratulation you can enter the gate!");
	// return true;
	// }else{
	// openDialog();
	// }
	// }else{
	// openDialog();
	// }
	// return false;
	// }

	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		FontData fontData = new FontData("Arial", 35, SWT.BOLD);

		fontData = new FontData("Arial", 12, SWT.BOLD);

		Composite composite_6 = new Composite(composite, SWT.BORDER);
		composite_6.setLayout(new GridLayout(1, false));
		composite_6.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_6.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_8 = new Composite(composite_6, SWT.NONE);
		composite_8.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		composite_8.setLayout(new GridLayout(3, false));
		composite_8.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_1 = new Composite(composite_8, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		composite_1.setSize(480, 109);
		composite_1.setLayout(new GridLayout(1, false));
		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnBHI37 = new Button(composite_1, SWT.NONE);
		GridData gd_btnBHI37 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnBHI37.heightHint = 150;
		btnBHI37.setLayoutData(gd_btnBHI37);
		btnBHI37.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/ExpBHI37C.png"));
		btnBHI37.addSelectionListener(this);

		Label lblExpressionAtlas = new Label(composite_1, SWT.NONE);
		lblExpressionAtlas.setText("BHI 37C - Exponential phase");
		// lblExpressionAtlas.setFont(new
		// Font(PlatformUI.getWorkbench().getDisplay(),fontData));
		lblExpressionAtlas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_7 = new Composite(composite_8, SWT.NONE);
		composite_7.setLayout(new GridLayout(1, false));
		composite_7.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnStat = new Button(composite_7, SWT.NONE);
		GridData gd_btnStat = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnStat.heightHint = 150;
		btnStat.setLayoutData(gd_btnStat);
		btnStat.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/StatBHI.png"));
		btnStat.addSelectionListener(this);

		Label lblStationnaryPhaseData = new Label(composite_7, SWT.NONE);
		lblStationnaryPhaseData.setText("BHI 37C - Stationary phase");
		lblStationnaryPhaseData.setFont(SWTResourceManager.getTitleFont());
		lblStationnaryPhaseData.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_9 = new Composite(composite_8, SWT.NONE);
		composite_9.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_9.setLayout(new GridLayout(1, false));
		composite_9.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnIntracellular = new Button(composite_9, SWT.NONE);
		GridData gd_btnIntracellular = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnIntracellular.heightHint = 150;
		btnIntracellular.setLayoutData(gd_btnIntracellular);
		btnIntracellular.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/Intracellular.png"));
		btnIntracellular.addSelectionListener(this);

		Label lblIntracellularMouse = new Label(composite_9, SWT.NONE);
		lblIntracellularMouse.setText("Intracellular - Mouse macrophages");
		lblIntracellularMouse.setFont(SWTResourceManager.getTitleFont());
		lblIntracellularMouse.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Composite composite_4 = new Composite(composite_8, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_4.setSize(242, 79);
		composite_4.setLayout(new GridLayout(1, false));
		composite_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		btnSrnas = new Button(composite_4, SWT.NONE);
		GridData gd_btnSrnas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnSrnas.heightHint = 150;
		btnSrnas.setLayoutData(gd_btnSrnas);
		btnSrnas.setImage(ResourceManager.getPluginImage("bacnet", "icons/InitPage/sRNAs.png"));
		btnSrnas.addSelectionListener(this);
		Label lblEgdeSmallRnas = new Label(composite_4, SWT.NONE);
		lblEgdeSmallRnas.setText("EGD-e small RNAs information");
		// lblEgdeSmallRnas.setFont(new
		// Font(PlatformUI.getWorkbench().getDisplay(),fontData));
		lblEgdeSmallRnas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		Label lblPosition = new Label(composite_4, SWT.NONE);
		lblPosition.setText("Position, structure, references.");
		lblPosition.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		// fontData = new FontData("Arial", 12, SWT.BOLD);

		Composite composite_10 = new Composite(composite_8, SWT.BORDER);
		GridData gd_composite_10 = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
		gd_composite_10.widthHint = 300;
		gd_composite_10.heightHint = 100;
		composite_10.setLayoutData(gd_composite_10);
		composite_10.setLayout(new GridLayout(5, false));

		Label lblLastUpdate = new Label(composite_10, SWT.NONE);
		lblLastUpdate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
		lblLastUpdate.setText("Last update: 2014-11-06");

		Label lblPasteur = new Label(composite_10, SWT.NONE);
		lblPasteur.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPasteur.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/Pasteur.png"));
		new Label(composite_10, SWT.NONE);

		Label lblAnr = new Label(composite_10, SWT.NONE);
		lblAnr.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/Logo ANR.png"));

		Label lblBacnet = new Label(composite_10, SWT.NONE);
		lblBacnet.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/investissement-davenir.png"));

		Label lblErc = new Label(composite_10, SWT.NONE);
		lblErc.setImage(ResourceManager.getPluginImage("bacnet", "icons/logos/erc-logo-small.png"));
		new Label(composite_8, SWT.NONE);
		fontData = new FontData("Arial", 16, SWT.BOLD);

		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	}

	@Override
	public void widgetSelected(SelectionEvent e) {

		if (e.getSource() == btnSrnas) {

			// page.showView(SrnaSummaryView.ID);
		} else if (e.getSource() == btnBHI37) {
			GenomeTranscriptomeView.displayBHI37View(partService);
			System.out.println("View opened");
		} else if (e.getSource() == btnStat) {
			GenomeTranscriptomeView.displayStat37View(partService);
		} else if (e.getSource() == btnIntracellular) {
			GenomeTranscriptomeView.displayIntracellularMacrophagesView(partService);
		}

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
