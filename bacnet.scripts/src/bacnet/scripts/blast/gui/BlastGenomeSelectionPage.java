package bacnet.scripts.blast.gui;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.swt.SWTResourceManager;

public class BlastGenomeSelectionPage extends WizardPage implements SelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5405627500164523922L;
	private List listMono;
	private List listNonMono;
	private List listBac;
	private List listMono2;
	private List listNonMono2;
	private List listBac2;
	private List listOther;
	private Button btnNonMono;
	private Button btnOther2;
	private Button btnOther;
	private Button btnBac;
	private Button btnBac2;
	private Button btnNonMono2;
	private Button btnMono2;
	private Button btnMono;
	private List listOther2;
	private Composite composite;
	private Label label;
	private Composite composite_1;
	private Label label_1;

	/**
	 * Create the wizard.
	 */
	public BlastGenomeSelectionPage() {
		super("wizardPage");
		setTitle("Select search genomes");
		setDescription("Choose on which genome you want to blast your query");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		try {
			Composite container = new Composite(parent, SWT.NULL);

			setControl(container);
			container.setLayout(new GridLayout(4, false));

			Composite composite_3 = new Composite(container, SWT.NONE);
			composite_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			composite_3.setBackground(SWTResourceManager.getColor(153, 204, 102));
			composite_3.setLayout(new GridLayout(1, false));

			Label lblCompleteGenomicSequence = new Label(composite_3, SWT.NONE);
			lblCompleteGenomicSequence.setBackground(SWTResourceManager.getColor(153, 204, 102));
			lblCompleteGenomicSequence.setText("Completed Genomic Sequences");

			Composite composite_4 = new Composite(container, SWT.NONE);
			composite_4.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			composite_4.setBackground(SWTResourceManager.getColor(255, 255, 204));
			composite_4.setLayout(new GridLayout(1, false));

			Label lblNewLabel = new Label(composite_4, SWT.NONE);
			lblNewLabel.setBackground(SWTResourceManager.getColor(255, 255, 204));
			lblNewLabel.setText("Whole Genome Shotgun");

			composite = new Composite(container, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			composite.setBackground(SWTResourceManager.getColor(153, 204, 102));
			composite.setLayout(new GridLayout(1, false));

			label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			label.setText("Completed Genomic Sequences");
			label.setBackground(SWTResourceManager.getColor(153, 204, 102));

			composite_1 = new Composite(container, SWT.NONE);
			composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			composite_1.setBackground(SWTResourceManager.getColor(255, 255, 204));
			composite_1.setLayout(new GridLayout(1, false));

			label_1 = new Label(composite_1, SWT.NONE);
			label_1.setText("Whole Genome Shotgun");
			label_1.setBackground(SWTResourceManager.getColor(255, 255, 204));

			Label lblListeriaMonocytogenes = new Label(container, SWT.CENTER);
			lblListeriaMonocytogenes.setAlignment(SWT.CENTER);
			GridData gd_lblListeriaMonocytogenes = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
			gd_lblListeriaMonocytogenes.widthHint = 405;
			lblListeriaMonocytogenes.setLayoutData(gd_lblListeriaMonocytogenes);
			lblListeriaMonocytogenes.setText("Listeria Monocytogenes");

			Label lblBacillusStrains = new Label(container, SWT.NONE);
			lblBacillusStrains.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
			lblBacillusStrains.setText("Bacillus strains");

			btnMono = new Button(container, SWT.CHECK);
			btnMono.setText("Select all");
			btnMono.addSelectionListener(this);
			btnMono2 = new Button(container, SWT.CHECK);
			btnMono2.setText("Select all");
			btnMono2.addSelectionListener(this);

			btnBac = new Button(container, SWT.CHECK);
			btnBac.setText("Select all");
			btnBac.addSelectionListener(this);
			btnBac2 = new Button(container, SWT.CHECK);
			btnBac2.setText("Select all");
			btnBac2.addSelectionListener(this);
			listMono = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_listMono = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_listMono.heightHint = 145;
			gd_listMono.widthHint = 289;
			listMono.setLayoutData(gd_listMono);

			listMono2 = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_listMono2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_listMono2.heightHint = 145;
			gd_listMono2.widthHint = 289;
			listMono2.setLayoutData(gd_listMono2);
			listMono2.setItems(GenomeNCBITools.getWGS(GenomeNCBITools.getListeriaMonoGenome()).toArray(new String[0]));
			listBac = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			listBac.setItems(
					GenomeNCBITools.getCompleteGenome(GenomeNCBITools.getBacillusGenome()).toArray(new String[0]));
			GridData gd_listBac = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_listBac.heightHint = 131;
			gd_listBac.widthHint = 289;
			listBac.setLayoutData(gd_listBac);

			listBac2 = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_listBac2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_listBac2.heightHint = 131;
			gd_listBac2.widthHint = 289;
			listBac2.setLayoutData(gd_listBac2);
			listBac2.setItems(GenomeNCBITools.getWGS(GenomeNCBITools.getBacillusGenome()).toArray(new String[0]));

			Label lblOtherListeriaStrain = new Label(container, SWT.NONE);
			lblOtherListeriaStrain.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
			lblOtherListeriaStrain.setText("Other Listeria strain");

			Label lblOtherBacteria = new Label(container, SWT.NONE);
			lblOtherBacteria.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
			lblOtherBacteria.setText("Other Bacteria");

			btnNonMono = new Button(container, SWT.CHECK);
			btnNonMono.setText("Select all");
			btnNonMono.addSelectionListener(this);
			btnNonMono2 = new Button(container, SWT.CHECK);
			btnNonMono2.setText("Select all");
			btnNonMono2.addSelectionListener(this);

			btnOther = new Button(container, SWT.CHECK);
			btnOther.setText("Select all");
			btnOther.addSelectionListener(this);
			btnOther2 = new Button(container, SWT.CHECK);
			btnOther2.setText("Select all");
			btnOther2.addSelectionListener(this);
			listNonMono = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			listNonMono.setItems(GenomeNCBITools.getCompleteGenome(GenomeNCBITools.getListeriaNonMonoGenome())
					.toArray(new String[0]));
			GridData gd_listNonMono = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_listNonMono.heightHint = 131;
			gd_listNonMono.widthHint = 288;
			listNonMono.setLayoutData(gd_listNonMono);

			listNonMono2 = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_listNonMono2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_listNonMono2.heightHint = 130;
			gd_listNonMono2.widthHint = 288;
			listNonMono2.setLayoutData(gd_listNonMono2);
			listNonMono2.setItems(
					GenomeNCBITools.getWGS(GenomeNCBITools.getListeriaNonMonoGenome()).toArray(new String[0]));
			listOther = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_listOther = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_listOther.heightHint = 168;
			gd_listOther.widthHint = 289;
			listOther.setLayoutData(gd_listOther);
			listOther.setItems(new String[] {});
			listOther.setItems(GenomeNCBITools.getCompleteGenome(GenomeNCBITools.getNotListeriaAndBacillusGenome())
					.toArray(new String[0]));

			listOther2 = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_listOther2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_listOther2.widthHint = 289;
			gd_listOther2.heightHint = 172;
			listOther2.setLayoutData(gd_listOther2);
			listOther2.setItems(new String[] {});
			listOther2.setItems(
					GenomeNCBITools.getWGS(GenomeNCBITools.getNotListeriaAndBacillusGenome()).toArray(new String[0]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get all genomes which have been selected
	 * 
	 * @return ArrayList of selected genomes
	 */
	public ArrayList<String> getSelectedGenomes() {
		ArrayList<String> genomes = new ArrayList<String>();
		for (String genome : listMono.getSelection())
			genomes.add(genome);
		for (String genome : listMono2.getSelection())
			genomes.add(genome);
		for (String genome : listNonMono.getSelection())
			genomes.add(genome);
		for (String genome : listNonMono2.getSelection())
			genomes.add(genome);
		for (String genome : listBac.getSelection())
			genomes.add(genome);
		for (String genome : listBac2.getSelection())
			genomes.add(genome);
		for (String genome : listOther.getSelection())
			genomes.add(genome);
		for (String genome : listOther2.getSelection())
			genomes.add(genome);

		return genomes;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnMono) {
			if (btnMono.getSelection())
				listMono.selectAll();
			else
				listMono.deselectAll();
		} else if (e.getSource() == btnMono2) {
			if (btnMono2.getSelection())
				listMono2.selectAll();
			else
				listMono2.deselectAll();
		} else if (e.getSource() == btnNonMono) {
			if (btnNonMono.getSelection())
				listNonMono.selectAll();
			else
				listNonMono.deselectAll();
		} else if (e.getSource() == btnNonMono2) {
			if (btnNonMono2.getSelection())
				listNonMono2.selectAll();
			else
				listNonMono2.deselectAll();
		} else if (e.getSource() == btnBac) {
			if (btnBac.getSelection())
				listBac.selectAll();
			else
				listBac.deselectAll();
		} else if (e.getSource() == btnBac2) {
			if (btnBac2.getSelection())
				listBac2.selectAll();
			else
				listBac2.deselectAll();
		} else if (e.getSource() == btnOther) {
			if (btnOther.getSelection())
				listOther.selectAll();
			else
				listOther.deselectAll();
		} else if (e.getSource() == btnOther2) {
			if (btnOther2.getSelection())
				listOther2.selectAll();
			else
				listOther2.deselectAll();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
