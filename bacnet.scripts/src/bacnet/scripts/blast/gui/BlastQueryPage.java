package bacnet.scripts.blast.gui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class BlastQueryPage extends WizardPage implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5725807534323834458L;
	private Text textQuery;
	private Button btnLoad;
	private Label lblLoadFile;
	private Label lblSave;
	private Button btnSave;
	public Combo cmbFileFormat;

	/**
	 * Create the wizard.
	 */
	public BlastQueryPage(Shell parentShell) {
		super("wizardPage");
		setTitle("Enter query sequence");
		setDescription("Provide a query nucleotide sequence");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		setControl(container);
		container.setLayout(new GridLayout(1, false));

		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setText("Enter FASTA sequence(s)");

		textQuery = new Text(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP);
		textQuery.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));

		btnLoad = new Button(composite_1, SWT.NONE);
		btnLoad.setText("Load");
		btnLoad.addSelectionListener(this);

		lblLoadFile = new Label(composite_1, SWT.NONE);
		GridData gd_lblLoadFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblLoadFile.widthHint = 430;
		lblLoadFile.setLayoutData(gd_lblLoadFile);

		btnSave = new Button(composite_1, SWT.NONE);
		btnSave.setText("Save");
		btnSave.addSelectionListener(this);

		lblSave = new Label(composite_1, SWT.NONE);
		GridData gd_lblSave = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblSave.widthHint = 400;
		lblSave.setLayoutData(gd_lblSave);

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label lblExportFormat = new Label(composite, SWT.NONE);
		lblExportFormat.setText("Export format");

		cmbFileFormat = new Combo(composite, SWT.NONE);
		cmbFileFormat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbFileFormat.add("ASN");
		cmbFileFormat.add("XML");
		cmbFileFormat.select(0);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
//		if(e.getSource()==btnLoad){
//			FileDialog fd = new FileDialog(shell, SWT.OPEN);
//		     fd.setText("Open a query Fasta file: ");
//		     fd.setFilterPath(Database.getInstance().getPATH());
//		     String fileName = fd.open();
//			try {
//				if(fileName!=""){
//					lblLoadFile.setText("Load: "+fileName);
//					textQuery.setText(FileUtils.readText(fileName));
//				}
//			}catch (Exception e1) {
//				System.out.println("Cannot read this file");
//				lblLoadFile.setText("Cannot load this file");
//			}
//		}else if(e.getSource()==btnSave){
//			FileDialog fd = new FileDialog(shell, SWT.SAVE);
//		     fd.setText("Save the query Fasta file: ");
//		     fd.setFilterPath(Database.getInstance().getPATH());
//		     String fileName = fd.open();
//			try {
//				if(fileName!=""){
//					FileUtils.saveText(textQuery.getText(), fileName);
//					lblSave.setText("Saved in: "+fileName);
//				}
//			}catch (Exception e1) {
//				System.out.println("Cannot save this file");
//				lblSave.setText("Cannot save this file");
//			}		
//		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public Text getTextQuery() {
		return textQuery;
	}

	public void setTextQuery(Text textQuery) {
		this.textQuery = textQuery;
	}

}
