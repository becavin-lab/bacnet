package bacnet.sequenceTools;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import bacnet.Database;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequence.Srna.TypeSrna;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.ArrayUtils;
import bacnet.utils.RWTUtils;

public class SrnaSummaryView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5574893108478572129L;

	public static final String ID = "bacnet.SrnaSummaryView"; //$NON-NLS-1$

	private String[][] array = new String[0][0];
	private String url = "";
	private TypeSrna displayType = TypeSrna.Srna;

	private ScrolledComposite scrolledComposite;
	private Composite composite;
	private Button btnAsrnas;
	private Button btnCisRegs;
	private Button btnSrnas;
	private Composite composite_1;
	private Table table;

	private Label lblXxSrnas;
	private Label lblImage;
	private Label lblSelectAType;

	@Inject
	private EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	private Composite composite_2;
	private Button btnDownloadtext;
	private Composite composite_3;
	private Label label;

	@Inject
	public SrnaSummaryView() {

	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));

		lblSelectAType = new Label(composite, SWT.NONE);
		lblSelectAType.setText("Select a type of non coding RNA");

		btnSrnas = new Button(composite, SWT.NONE);
		btnSrnas.setText("Small RNA");
		btnSrnas.addSelectionListener(this);
		btnAsrnas = new Button(composite, SWT.NONE);
		btnAsrnas.setText("Antisense RNA");
		btnAsrnas.addSelectionListener(this);
		btnCisRegs = new Button(composite, SWT.NONE);
		btnCisRegs.setText("CisReg RNA");
		btnCisRegs.addSelectionListener(this);
		scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		composite_1 = new Composite(scrolledComposite, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));

		lblXxSrnas = new Label(composite_1, SWT.BORDER | SWT.CENTER);
		lblXxSrnas.setFont(SWTResourceManager.getTitleFont());
		lblXxSrnas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblXxSrnas.setText("XX small RNAs are available. Select one to access its available information");
		lblXxSrnas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		lblImage = new Label(composite_1, SWT.NONE);
		GridData gd_lblImage = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
		gd_lblImage.widthHint = 300;
		gd_lblImage.heightHint = 300;
		lblImage.setLayoutData(gd_lblImage);
		lblImage.setImage(SWTResourceManager.getImage(Database.getANNOTATIONDATA_PATH() + "/SrnaCircularGenome.png"));

		composite_3 = new Composite(composite_1, SWT.NONE);
		composite_3.setLayout(new GridLayout(3, false));

		Label lblClickOnA = new Label(composite_3, SWT.NONE);
		lblClickOnA.setText("Click on a row to display non-coding RNA properties");
		lblClickOnA.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		label = new Label(composite_3, SWT.NONE);
		label.setText("");

		composite_2 = new Composite(composite_3, SWT.NONE);
		composite_2.setLayout(new GridLayout(2, false));

		btnDownloadtext = new Button(composite_2, SWT.NONE);
		btnDownloadtext.setToolTipText("Download the small non-coding RNA table");
		btnDownloadtext.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
		btnDownloadtext.addSelectionListener(this);
		Label lblDownloadTheSmall = new Label(composite_2, SWT.NONE);
		lblDownloadTheSmall.setText("Download the small non-coding RNA table");
		lblDownloadTheSmall.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));

		table = new Table(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addSelectionListener(this);
		scrolledComposite.setContent(composite_1);
		scrolledComposite.setMinSize(composite_1.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		updateSummary();

		createActions();
	}

	private void updateSummary() {
		if (displayType == TypeSrna.Srna) {
			array = TabDelimitedTableReader.read(Srna.PATHTABLE_Srna);
			url = Srna.PATHFigure_Srna;
			lblXxSrnas.setText((array.length - 1) + " small RNAs");
		} else if (displayType == TypeSrna.ASrna) {
			array = TabDelimitedTableReader.read(Srna.PATHTABLE_ASrna);
			url = Srna.PATHFigure_ASrna;
			lblXxSrnas.setText((array.length - 1) + " antisense RNAs");
		} else {
			array = TabDelimitedTableReader.read(Srna.PATHTABLE_CISReg);
			url = Srna.PATHFigure_CISReg;
			lblXxSrnas.setText((array.length - 1) + " cis regulatory RNAs");
		}
		lblImage.setImage(SWTResourceManager.getImage(url));

		table.removeAll();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		RWTUtils.setMarkup(table);
		for (int i = 0; i < array[0].length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(array[0][i]);
			column.setAlignment(SWT.LEFT);
		}

		HashMap<String, String> sRNARefToLink = TabDelimitedTableReader.readHashMap(Srna.PATHTABLE_SrnaReference);

		for (int i = 1; i < array.length; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			for (int j = 0; j < array[0].length; j++) {
				if (array[0][j].equals("First described in") || array[0][j].equals("Also described in")) {
					String text = RWTUtils.setSrnaLink(array[i][j], sRNARefToLink);
					item.setText(j, text);
				} else {
					item.setText(j, array[i][j]);
				}
			}
		}
		for (int i = 0; i < array[0].length; i++) {
			table.getColumn(i).pack();
		}
		table.update();
		table.redraw();

	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnSrnas) {
			displayType = TypeSrna.Srna;
			updateSummary();
		} else if (e.getSource() == btnAsrnas) {
			displayType = TypeSrna.ASrna;
			updateSummary();
		} else if (e.getSource() == btnCisRegs) {
			displayType = TypeSrna.CisReg;
			updateSummary();
		} else if (e.getSource() == table) {
			String selectSrna = array[table.getSelectionIndex() + 1][1];
			System.out.println("Select: " + selectSrna);
			Sequence seq = Genome.loadEgdeGenome().getElement(selectSrna);
			if (seq != null) {
				SrnaView.displaySrna((Srna) seq, partService);
			}
		} else if (e.getSource() == btnDownloadtext) {
			String[][] array = new String[0][0];
			if (displayType == TypeSrna.Srna) {
				array = TabDelimitedTableReader.read(Srna.PATHTABLE_Srna);
			} else if (displayType == TypeSrna.ASrna) {
				array = TabDelimitedTableReader.read(Srna.PATHTABLE_ASrna);
			} else {
				array = TabDelimitedTableReader.read(Srna.PATHTABLE_CISReg);
			}
			String arrayRep = ArrayUtils.toString(array);
			String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(array);
			SaveFileUtils.saveTextFile("Listeria_" + displayType + "_Table.txt", arrayRep, true, "", arrayRepHTML,
					partService, shell);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public String[][] getArray() {
		return array;
	}

	public void setArray(String[][] array) {
		this.array = array;
	}

}
