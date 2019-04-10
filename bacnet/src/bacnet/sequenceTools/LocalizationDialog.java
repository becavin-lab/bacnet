package bacnet.sequenceTools;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.utils.RWTUtils;

public class LocalizationDialog extends Dialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8150068655518237507L;

	private String[][] arrayGeneToLocalization;
	private Sequence sequence;
	private String[][] bioCondsArray;
	private Genome genome;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public LocalizationDialog(Shell parentShell, String[][] arrayGeneToLocalization, Sequence sequence,
			String[][] bioCondsArray, Genome genome) {
		super(parentShell);
		this.arrayGeneToLocalization = arrayGeneToLocalization;
		this.sequence = sequence;
		this.bioCondsArray = bioCondsArray;
		this.genome = genome;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		Label lblPredictedSubcellularLocalization = new Label(container, SWT.WRAP);
		GridData gd_lblPredictedSubcellularLocalization = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblPredictedSubcellularLocalization.widthHint = 200;
		gd_lblPredictedSubcellularLocalization.heightHint = 55;
		lblPredictedSubcellularLocalization.setLayoutData(gd_lblPredictedSubcellularLocalization);
		RWTUtils.setMarkup(lblPredictedSubcellularLocalization);
		lblPredictedSubcellularLocalization
				.setText("Predicted subcellular localization of L. mono. EGD-e proteins (<a href='"
						+ RWTUtils.getPubMedLink("22912771") + "' target='_blank'>Renier et al., Plos One 2012</a>)");

		TableViewer tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		Table tableLocalization = tableViewer.getTable();
		GridData gd_tableLocalization = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		gd_tableLocalization.heightHint = 100;
		gd_tableLocalization.widthHint = 200;
		tableLocalization.setLayoutData(gd_tableLocalization);

		Browser browserLocalization = new Browser(container, SWT.NONE);
		GridData gd_browserLocalization = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_browserLocalization.widthHint = 184;
		gd_browserLocalization.heightHint = 250;
		browserLocalization.setLayoutData(gd_browserLocalization);

		GeneViewLocalizationTools.loadLocalizationFigure(browserLocalization, arrayGeneToLocalization, sequence,
				bioCondsArray, tableLocalization, genome);

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
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
		return new Point(686, 415);
	}

}
