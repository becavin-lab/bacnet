package awt.table.gui;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import bacnet.table.core.Filter;
import bacnet.table.core.FilterList;

public class FilterExpressionMatrixDialog extends TitleAreaDialog implements SelectionListener{

	private FilterList filters;
	private Text txtDescriptionFilter;
	private List listFilter;
	private Button btnCreateNew;
	private Button btnEdit;
	private Button btnRemove;
	
	private Shell shell;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public FilterExpressionMatrixDialog(FilterList filters, Shell parentShell) {
		super(parentShell);
		this.shell = parentShell;
		this.filters = filters;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Filters applied to the table");
		setMessage("Add or remove different filters");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblListOfFilters = new Label(container, SWT.NONE);
		lblListOfFilters.setText("List of filters");
		new Label(container, SWT.NONE);
		
		Label lblFilterDescription = new Label(container, SWT.NONE);
		lblFilterDescription.setText("Filter description");
		
		listFilter = new List(container, SWT.BORDER | SWT.H_SCROLL);
		GridData gd_listFilter = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_listFilter.heightHint = 312;
		gd_listFilter.widthHint = 182;
		listFilter.setLayoutData(gd_listFilter);
		listFilter.addSelectionListener(this);
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		btnCreateNew = new Button(composite, SWT.NONE);
		btnCreateNew.setText("Create new");
		btnCreateNew.addSelectionListener(this);
		btnEdit = new Button(composite, SWT.NONE);
		btnEdit.setText("Edit");
		btnEdit.addSelectionListener(this);
		btnRemove = new Button(composite, SWT.NONE);
		btnRemove.setText("Remove");
		btnRemove.addSelectionListener(this);
		txtDescriptionFilter = new Text(container, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.CANCEL);
		GridData gd_txtDescriptionFilter = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_txtDescriptionFilter.heightHint = 217;
		txtDescriptionFilter.setLayoutData(gd_txtDescriptionFilter);

		listFilter.setItems(filters.getFilters().keySet().toArray(new String[0]));
		
		return area;
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

	@Override
	public void okPressed(){
		filters.updateExclude();
		this.close();
	}
	
	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(594, 529);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnCreateNew){
			Filter filter = new Filter(filters.getMatrix());
			FilterDialog dialog = new FilterDialog(filter, shell);
			int ok = dialog.open();
			if(ok == Status.OK){
				filters.getFilters().put(filter.getName(), filter);
				listFilter.add(filter.getName());
			}
		}else if(e.getSource()==btnEdit){
			if(listFilter.getSelection().length!=0){
				Filter filter = filters.getFilters().get(listFilter.getSelection()[0]);
				Filter filterClone = filter.clone();
				FilterDialog dialog = new FilterDialog(filterClone, shell);
				int ok = dialog.open();
				if(ok == Status.OK){
					filters.getFilters().remove(filter.getName());
					filters.getFilters().put(filterClone.getName(), filterClone);
					listFilter.setItems(filters.getFilters().keySet().toArray(new String[0]));
				}
			}
		}else if(e.getSource()==btnRemove){
			if(listFilter.getSelection().length!=0){
				Filter filter = filters.getFilters().get(listFilter.getSelection()[0]);
				filters.getFilters().remove(filter.getName());
				listFilter.setItems(filters.getFilters().keySet().toArray(new String[0]));
			}
			
		}else if(e.getSource()==listFilter){
			if(listFilter.getSelection().length!=0){
				Filter filter = filters.getFilters().get(listFilter.getSelection()[0]);
				txtDescriptionFilter.setText(filter.toString());
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
