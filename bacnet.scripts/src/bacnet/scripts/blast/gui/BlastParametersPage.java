package bacnet.scripts.blast.gui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import bacnet.swt.SWTResourceManager;

public class BlastParametersPage extends WizardPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3655573289402306938L;

	/**
	 * Create the wizard.
	 */
	public BlastParametersPage() {
		super("wizardPage");
		setTitle("Algorithm parameters");
		setDescription("Select the different parameters of the algorithm");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(2, false));

		Label lblGeneralParameters = new Label(container, SWT.NONE);
		lblGeneralParameters.setText("General Parameters");
		new Label(container, SWT.NONE);

		Label lblMaxTargetSequences = new Label(container, SWT.NONE);
		lblMaxTargetSequences.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMaxTargetSequences.setText("Max target sequences");

		Combo combo = new Combo(container, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label lblSelectTheMaximyum = new Label(container, SWT.NONE);
		lblSelectTheMaximyum.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		lblSelectTheMaximyum.setText("Select the maximum number of aligned sequences to display ");

		Label lblShortQueris = new Label(container, SWT.NONE);
		lblShortQueris.setText("Short queries");

		Button btnAutomaticallyAdjustParameters = new Button(container, SWT.CHECK);
		btnAutomaticallyAdjustParameters.setText("Automatically adjust parameters for short input sequences");

		Label lblExpectThreshold = new Label(container, SWT.NONE);
		lblExpectThreshold.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblExpectThreshold.setText("Expect threshold");

		Combo combo_1 = new Combo(container, SWT.NONE);
		combo_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblWordSize = new Label(container, SWT.NONE);
		lblWordSize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblWordSize.setText("Word size");

		Combo combo_2 = new Combo(container, SWT.NONE);
		combo_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMaxMatchesIn = new Label(container, SWT.NONE);
		lblMaxMatchesIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMaxMatchesIn.setText("Max matches in a query range");

		Combo combo_3 = new Combo(container, SWT.NONE);
		combo_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Label lblScoringParameters = new Label(container, SWT.NONE);
		lblScoringParameters.setText("Scoring parameters");
		new Label(container, SWT.NONE);

		Label lblMatchmismatchScores = new Label(container, SWT.NONE);
		lblMatchmismatchScores.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMatchmismatchScores.setText("Match/Mismatch Scores");

		Combo combo_4 = new Combo(container, SWT.NONE);
		combo_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblGapCosts = new Label(container, SWT.NONE);
		lblGapCosts.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblGapCosts.setText("Gap Costs");

		Combo combo_5 = new Combo(container, SWT.NONE);
		combo_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Label lblFiltersAndMasking = new Label(container, SWT.NONE);
		lblFiltersAndMasking.setText("Filters and Masking");
		new Label(container, SWT.NONE);

		Label lblFilter = new Label(container, SWT.NONE);
		lblFilter.setText("Filter");

		Button btnCheckButton = new Button(container, SWT.CHECK);
		btnCheckButton.setText("Low complexity regions");
	}

}
