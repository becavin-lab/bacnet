package awt.table.gui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.utils.FileUtils;

public class CytoscapeExportDialog extends TitleAreaDialog {

	private ExpressionMatrix matrix;
	private List listColumn;
	
	private Shell shell;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public CytoscapeExportDialog(Shell parentShell,ExpressionMatrix matrix) {
		super(parentShell);
		this.matrix = matrix;
		this.shell = parentShell;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Cytoscape nodes export");
		setMessage("Select the column to export to cytoscape");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		listColumn = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_listColumn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_listColumn.widthHint = 215;
		gd_listColumn.heightHint = 332;
		listColumn.setLayoutData(gd_listColumn);
		
		for(String header : matrix.getHeaders()){
			listColumn.add(header);
		}
		for(String header : matrix.getHeaderAnnotation()){
			listColumn.add(header);
		}
		return area;
	}
	
	@Override
	public void okPressed(){
		if(listColumn.getSelection().length!=0){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
		     fd.setText("Save the genes attributes to: ");
		     fd.setFilterPath(Database.getInstance().getPATH());
		     //fd.setFileName("");
		     String[] filterExt = {"*.noa","*.*" };
		     fd.setFilterExtensions(filterExt);
		     String fileName = fd.open();
			if(fileName!=null){
				int i=0;
				for(String colName : listColumn.getSelection()){
					String nodeExpression = colName+"\n";
					if(colName.equals("COG")){
						for(String rowName : GenomeNCBITools.loadEgdeGenome().getLocusTagCodingList(true)){
							if(matrix.getRowNames().keySet().contains(rowName)){
								if(matrix.getHeaders().contains(colName)){
									nodeExpression += rowName+" = "+matrix.getValue(rowName, colName)+"\n";
								}else{
									String annotation = matrix.getValueAnnotation(rowName, colName);
									if(colName.equals("COG")){
										if(annotation.split(";").length>1) nodeExpression += rowName+" = Multi-COG\n";
										else nodeExpression += rowName+" = "+annotation+"\n";
									}else nodeExpression += rowName+" = "+annotation+"\n";
								}
							}else{
								if(matrix.getHeaders().contains(colName)){
									nodeExpression += rowName+" = "+0+"\n";
								}else{
									nodeExpression += rowName+" = Not in the list\n";
								}
								
							}
							
						}
					}else{
						for(String rowName : matrix.getRowNames().keySet()){
							if(matrix.getHeaders().contains(colName)){
								nodeExpression+= rowName+" = "+matrix.getValue(rowName, colName)+"\n";
							}else{
								nodeExpression+= rowName+" = "+matrix.getValueAnnotation(rowName, colName)+"\n";
							}
						}
					}
					String fileNameTemp = FileUtils.removeExtension(fileName)+"_"+i+".noa";
					FileUtils.saveText(nodeExpression, fileNameTemp);
					i++;
				}
			}
		}
		this.close();
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
		return new Point(457, 578);
	}
}
