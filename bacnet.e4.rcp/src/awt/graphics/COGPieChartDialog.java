package awt.graphics;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.FileUtils;

public class COGPieChartDialog extends TitleAreaDialog implements SelectionListener{
	private Label lblLoadMatrices;
	private Label lblLoadLists;
	private Button btnMatrices;
	private Button btnLists;
	private Button btnLoadMatrices;
	private Button btnLoadLists;
	
	private boolean isMatrices = true;
	private ArrayList<String> fileNames;
	private StyledText styledText;

	private Shell shell;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public COGPieChartDialog(Shell parentShell) {
		super(parentShell);
		this.shell = parentShell;
		GenomeNCBITools.loadEgdeGenome();
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Load lists of genes or matrices of expression, and claasify them by COGs properties");
		setTitle("COG pie chart tool");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		
		btnLists = new Button(container, SWT.RADIO);
		btnLists.setText("Open lists of gene");
		
		btnLoadLists = new Button(container, SWT.NONE);
		btnLoadLists.setText("Load");
		btnLoadLists.addSelectionListener(this);
		
		lblLoadLists = new Label(container, SWT.NONE);
		GridData gd_lblLoadLists = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_lblLoadLists.widthHint = 153;
		lblLoadLists.setLayoutData(gd_lblLoadLists);
		new Label(container, SWT.NONE);
		
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		btnMatrices = new Button(container, SWT.RADIO);
		btnMatrices.setText("Open matrices of expression");
		
		btnLoadMatrices = new Button(container, SWT.NONE);
		btnLoadMatrices.setText("Load");
		btnLoadMatrices.addSelectionListener(this);
		
		lblLoadMatrices = new Label(container, SWT.NONE);
		GridData gd_lblLoadMatrices = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_lblLoadMatrices.widthHint = 155;
		lblLoadMatrices.setLayoutData(gd_lblLoadMatrices);
		
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		styledText = new StyledText(container, SWT.BORDER | SWT.WRAP);
		styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		styledText.setDoubleClickEnabled(false);
		styledText.setEnabled(false);
		styledText.setEditable(false);
		styledText.setText("COGs: Phylogenetic classification of proteins encoded in complete genomes. Clusters of Orthologous Groups of proteins (COGs) were delineated by comparing protein sequences encoded in complete genomes, representing major phylogenetic lineages. Each COG consists of individual proteins or groups of paralogs from at least 3 lineages and thus corresponds to an ancient conserved domain. Science 1997 Oct 24;278(5338):631-7, BMC Bioinformatics 2003 Sep 11;4(1):41.");
		GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		gd_styledText.heightHint = 128;
		gd_styledText.widthHint = 174;
		styledText.setLayoutData(gd_styledText);

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(395, 419);
	}

	@Override
	public void okPressed(){
		ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
		for(String fileName : fileNames){
			if(isMatrices){
					ExpressionMatrix matrix = ExpressionMatrix.loadTab(fileName, false);
//					HashMap<String, ArrayList<String>> cogs = COGannotation.getCogClassification(matrix,genome.getCogAnnot());
//					ExpressionMatrix cog = COGannotation.getPieChartData(cogs);
//					cog.setInfo(FileUtils.removeExtensionAndPath(fileName));
//					matrices.add(cog);
				
			}else{
					ArrayList<String> list = TabDelimitedTableReader.readList(fileName);
//					HashMap<String, ArrayList<String>> cogs = COGannotation.getCogClassification(list,genome.getCogAnnot());
//					ExpressionMatrix cog = COGannotation.getPieChartData(cogs);
//					cog.setInfo(FileUtils.removeExtensionAndPath(fileName));
//					matrices.add(cog);
				
				
			}
		}
		//ModelProvider.INSTANCE.setMatrix(matrices);
		this.close();
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnLoadMatrices){
			FileDialog fd = new FileDialog(shell, SWT.OPEN |SWT.MULTI);
			fd.setText("Open expression matrices: ");
			fd.setFilterPath(Database.getInstance().getPATH());
		    	String[] filterExt = {"*.*" };
		    	fd.setFilterExtensions(filterExt);
		    	String path = fd.open();
		    	String[] fileNames = fd.getFileNames();
		    	path = FileUtils.getPath(path);
			try {
				ArrayList<String> fileNamesTemp = new ArrayList<String>();
				for(String fileName : fileNames){
					fileNamesTemp.add(path+fileName);
				}
				this.fileNames = fileNamesTemp;
				lblLoadMatrices.setText("Matrices selected");
				lblLoadLists.setText("");
				btnMatrices.setSelection(true);
				btnLists.setSelection(false);
				isMatrices = true;
			} catch (Exception e1) {
				System.err.println("No data selected");
			}
		}else if(e.getSource()==btnLoadLists){
			FileDialog fd = new FileDialog(shell, SWT.OPEN |SWT.MULTI);
			fd.setText("Open lists of gene: ");
			fd.setFilterPath(Database.getInstance().getPATH());
		    	String[] filterExt = {"*.*" };
		    	fd.setFilterExtensions(filterExt);
		    	String path = fd.open();
		    	String[] fileNames = fd.getFileNames();
		    	path = FileUtils.getPath(path);
			try {
				ArrayList<String> fileNamesTemp = new ArrayList<String>();
				for(String fileName : fileNames){
					fileNamesTemp.add(path+fileName);
				}
				this.fileNames = fileNamesTemp;
				lblLoadMatrices.setText("");
				lblLoadLists.setText("Lists selected");
				btnMatrices.setSelection(false);
				btnLists.setSelection(true);
				isMatrices = false;
			} catch (Exception e1) {
				System.err.println("No data selected");
			}
		}
		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
