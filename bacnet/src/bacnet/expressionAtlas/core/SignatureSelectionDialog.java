package bacnet.expressionAtlas.core;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import bacnet.Database;
import bacnet.datamodel.annotation.Signature;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;

public class SignatureSelectionDialog extends TitleAreaDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1941320621284175887L;
	private Text txtDescription;
	private Label lblGenome;
	private List listGenes;
	private Table tableSignature;
	private Label lblSize;
	private Label lblID;
	private Label lblName;
	private Label lblRef;
	
	private Label lblDescription;
	private Label lblGenesInclude;
	private Composite composite_1;
	private Label lblSelectYourList;
	
	private final ArrayList<String> signatures = new ArrayList<>();
	private Signature signature;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SignatureSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Gene signature selection");
		setMessage("We provide here different group of genes published in the litterature. Select one to look at it in the HeatMap Viewer.");
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		
		composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_1.setLayout(new GridLayout(2, false));
		
		lblSelectYourList = new Label(composite_1, SWT.NONE);
		lblSelectYourList.setText("Select a list of genes :");
		
		Composite composite = new Composite(composite_1, SWT.BORDER);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 2));
		composite.setLayout(new GridLayout(1, false));
		
		lblName = new Label(composite, SWT.NONE);
		GridData gd_lblName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblName.widthHint = 235;
		lblName.setLayoutData(gd_lblName);
		lblName.setText("New Label");
		
		lblID = new Label(composite, SWT.NONE);
		GridData gd_lblID = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblID.widthHint = 236;
		lblID.setLayoutData(gd_lblID);
		lblID.setText("New Label");
		
		lblGenome = new Label(composite, SWT.WRAP);
		GridData gd_lblGenome = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblGenome.heightHint = 22;
		gd_lblGenome.widthHint = 238;
		lblGenome.setLayoutData(gd_lblGenome);
		lblGenome.setText("New Label");
		
		lblRef = new Label(composite, SWT.WRAP);
		GridData gd_lblRef = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblRef.heightHint = 50;
		gd_lblRef.widthHint = 200;
		lblRef.setLayoutData(gd_lblRef);
		lblRef.setText("New Label");
		
		lblSize = new Label(composite, SWT.NONE);
		GridData gd_lblSize = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblSize.widthHint = 174;
		lblSize.setLayoutData(gd_lblSize);
		lblSize.setText("New Label");
		
		lblDescription = new Label(composite, SWT.NONE);
		GridData gd_lblDescription = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblDescription.widthHint = 129;
		lblDescription.setLayoutData(gd_lblDescription);
		lblDescription.setText("Description:");
		
		txtDescription = new Text(composite, SWT.BORDER | SWT.WRAP);
		GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtDescription.heightHint = 99;
		gd_txtDescription.widthHint = 225;
		txtDescription.setLayoutData(gd_txtDescription);
		
		lblGenesInclude = new Label(composite, SWT.NONE);
		lblGenesInclude.setText("Genes include:");
		
		listGenes = new List(composite, SWT.BORDER | SWT.V_SCROLL);
		GridData gd_listGenes = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_listGenes.widthHint = 172;
		listGenes.setLayoutData(gd_listGenes);
		
		tableSignature = new Table(composite_1, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.VIRTUAL);
		tableSignature.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableSignature.addSelectionListener(new SelectionListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public void widgetSelected(SelectionEvent e) {
				signature = Signature.getSignatureFromName(tableSignature.getSelection()[0].getText());
				updateInfo(signature);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		initData();
		
		return container;
	}

	private void initData(){
		for(String signature : Database.getInstance().getSignaturesNametoID().keySet()){
			signatures.add(signature);
		}
		
		tableSignature.addListener(SWT.SetData, new Listener() {
			private static final long serialVersionUID = 6744063943372593076L;

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem)event.item;
				int index = event.index;
				item.setText(signatures.get(index));
			}
		});
		tableSignature.setItemCount(signatures.size());
		
		signature = Signature.getSignatureFromName(signatures.get(0));
		signature = Signature.getSignatureFromName("PrfA regulon");
		updateInfo(signature);
	}
	
	
	private void updateInfo(Signature signature){
		lblName.setText("Name: "+signature.getName());
		lblGenome.setText("Genome: "+signature.getGenome());
		lblID.setText("file: "+signature.getID()+".txt");
		lblRef.setText("Ref: "+signature.getReference());
		lblSize.setText("size: "+signature.getSize()+"");
		txtDescription.setText(signature.getDescription());
		Genome genome = Genome.loadGenome(signature.getGenome());
		String[] items = new String[signature.getSize()];
		for(int i=0;i<items.length;i++){
			String ret = signature.getElements().get(i);
			Gene gene = genome.getGeneFromName(ret);
			if(gene!=null){
				String geneName = gene.getGeneName();
				items[i] = ret;
				if(!geneName.equals("-")){
					items[i] = ret + " ("+geneName+")";
				}
			}
			
			
		}
		listGenes.setItems(items);
	}
	
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Ok", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(575, 701);
	}

	public Signature getSignature() {
		return signature;
	}

	public void setSignature(Signature signature) {
		this.signature = signature;
	}

}
