package bacnet.expressionAtlas.core;

import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import bacnet.datamodel.annotation.Signature;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.views.InternalBrowser;

/**
 * Dialog which allow specific selection of genes, smallRNAs and cisRegs
 * 
 * @author christophebecavin
 *
 */
public class SelectGenomeElementDialog extends TitleAreaDialog implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3481072975746172059L;
	private Button btnDeselRNA;
	private Button btnSelectRNA;
	private Button btnDeselGene;
	private Button btnSelectGene;
	private Button btnDeselAS;
	private Button btnSelectAS;
	private Table tableASRNAs;
	private Table tableSRNAs;
	private Table tableGenes;
	private Button btnLoad;
	private Button btnSave;

	private ArrayList<String> listGenes = new ArrayList<>();
	private ArrayList<String> listsRNAs = new ArrayList<>();
	private ArrayList<String> listasRNAs = new ArrayList<>();
	private ArrayList<String> listcisRegs = new ArrayList<>();
	public boolean rFamAnnot = false;

	private TreeSet<String> includeElements = new TreeSet<String>();
	private TreeSet<String> excludedElements = new TreeSet<String>();
	private Button btnSelectSpecificList;
	private Composite compositeButton;
	private ScrolledComposite scrolledComposite;
	private Composite composite;

	private Shell shell;
	private EPartService partService;

	/**
	 * Create the wizard.
	 */
	public SelectGenomeElementDialog(Shell shell, EPartService partService, TreeSet<String> includeElements,
			TreeSet<String> excludedElements, Genome genome) {
		super(shell);
		this.shell = shell;
		this.partService = partService;
		setShellStyle(SWT.DIALOG_TRIM);
		this.includeElements = includeElements;
		this.excludedElements = excludedElements;
		listGenes = new ArrayList<>();
		for (String gene : genome.getGeneNames()) {
			listGenes.add(gene);
		}
		listsRNAs = new ArrayList<>();
		for (String sRNA : genome.getsRNAs().keySet()) {
			listsRNAs.add(sRNA);
		}
		listasRNAs = new ArrayList<>();
		for (String asRNA : genome.getAsRNAs().keySet()) {
			listasRNAs.add(asRNA);
		}
		listcisRegs = new ArrayList<>();
		for (String asRNA : genome.getCisRegs().keySet()) {
			listcisRegs.add(asRNA);
		}
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@Override
	public Control createDialogArea(Composite parent) {
		setTitle("Select Element to display");
		setMessage("Select element of the genome to display. You can load and save your selection");
		Composite container = (Composite) super.createDialogArea(parent);

		container.setLayout(new GridLayout(1, false));

		scrolledComposite = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		Composite compositeGene = new Composite(composite, SWT.NONE);
		compositeGene.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeGene.setSize(181, 421);
		compositeGene.setLayout(new GridLayout(1, false));

		Composite composite_2 = new Composite(compositeGene, SWT.NONE);
		composite_2.setLayout(new GridLayout(3, false));

		Label lblGenes = new Label(composite_2, SWT.NONE);
		lblGenes.setText("Genes");

		btnSelectGene = new Button(composite_2, SWT.NONE);
		btnSelectGene.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/select.bmp"));
		btnSelectGene.setToolTipText("Select all genes");

		btnDeselGene = new Button(composite_2, SWT.NONE);
		btnDeselGene.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/deselect.bmp"));
		btnDeselGene.setToolTipText("Deselect all genes");

		tableGenes = new Table(compositeGene, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.VIRTUAL);
		tableGenes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite compositeSrna = new Composite(composite, SWT.NONE);
		compositeSrna.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeSrna.setLayout(new GridLayout(1, false));

		Composite composite_3 = new Composite(compositeSrna, SWT.NONE);
		composite_3.setLayout(new GridLayout(3, false));

		Label lblSrna = new Label(composite_3, SWT.NONE);
		lblSrna.setText("sRNA");

		btnSelectRNA = new Button(composite_3, SWT.NONE);
		btnSelectRNA.setToolTipText("Select all sRNA");
		btnSelectRNA.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/select.bmp"));

		btnDeselRNA = new Button(composite_3, SWT.NONE);
		btnDeselRNA.setToolTipText("Deselect all sRNA");
		btnDeselRNA.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/deselect.bmp"));

		tableSRNAs = new Table(compositeSrna, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.VIRTUAL);
		tableSRNAs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite compositeASrna = new Composite(composite, SWT.NONE);
		compositeASrna.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeASrna.setLayout(new GridLayout(1, false));

		Composite composite_5 = new Composite(compositeASrna, SWT.NONE);
		composite_5.setLayout(new GridLayout(3, false));

		Label lblAsrna = new Label(composite_5, SWT.NONE);
		lblAsrna.setText("asRNA");

		btnSelectAS = new Button(composite_5, SWT.NONE);
		btnSelectAS.setToolTipText("Select all asRNA");
		btnSelectAS.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/select.bmp"));

		btnDeselAS = new Button(composite_5, SWT.NONE);
		btnDeselAS.setToolTipText("Deselect all asRNA");
		btnDeselAS.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/deselect.bmp"));

		tableASRNAs = new Table(compositeASrna, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.VIRTUAL);
		tableASRNAs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		compositeButton = new Composite(composite, SWT.NONE);
		compositeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
		compositeButton.setLayout(new GridLayout(3, false));

		btnLoad = new Button(compositeButton, SWT.NONE);
		btnLoad.setToolTipText("Load a list of genome elements");
		btnLoad.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/sigLoad.bmp"));
		btnLoad.setText("Load");

		btnSave = new Button(compositeButton, SWT.NONE);
		btnSave.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/sigSave.bmp"));
		btnSave.setToolTipText("Save your list of genome elements selected");
		btnSave.setText("Save");

		btnSelectSpecificList = new Button(compositeButton, SWT.NONE);
		btnSelectSpecificList.setText("Use list of genes with common functions");
		btnSelectSpecificList.addSelectionListener(this);
		btnSave.addSelectionListener(this);
		btnLoad.addSelectionListener(this);
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// add Listener
		btnDeselAS.addSelectionListener(this);
		btnSelectAS.addSelectionListener(this);
		btnDeselRNA.addSelectionListener(this);
		btnSelectRNA.addSelectionListener(this);
		btnDeselGene.addSelectionListener(this);
		btnSelectGene.addSelectionListener(this);

		initData();

		return container;
	}

	private void initData() {
		// Fill tables only with the number of data needed
		tableGenes.addListener(SWT.SetData, new Listener() {
			private static final long serialVersionUID = 6744063943372593076L;

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(listGenes.get(index));
			}
		});
		tableGenes.setItemCount(listGenes.size());
		// Fill tables only with the number of data needed
		tableSRNAs.addListener(SWT.SetData, new Listener() {
			private static final long serialVersionUID = 8429132651820570430L;

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(listsRNAs.get(index));
			}
		});
		tableSRNAs.setItemCount(listsRNAs.size());
		// Fill tables only with the number of data needed
		tableASRNAs.addListener(SWT.SetData, new Listener() {
			private static final long serialVersionUID = -7858199108743075609L;

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(listasRNAs.get(index));
			}
		});
		tableASRNAs.setItemCount(listasRNAs.size());
	}

	/**
	 * When Ok is pressed, update the different genome elements sleected
	 * 
	 * @return
	 */
	@Override
	public void okPressed() {
		try {
			includeElements.clear();
			for (TableItem element : tableGenes.getSelection())
				includeElements.add(element.getText());
			for (TableItem element : tableSRNAs.getSelection())
				includeElements.add(element.getText());
			for (TableItem element : tableASRNAs.getSelection())
				includeElements.add(element.getText());

			excludedElements.clear();
			/*
			 * Exclude all genome elements
			 */
			for (String element : listGenes) {
				excludedElements.add(element);
			}
			for (String element : listsRNAs) {
				excludedElements.add(element);
			}
			for (String element : listasRNAs) {
				excludedElements.add(element);
			}
			for (String element : listcisRegs) {
				excludedElements.add(element);
			}
			/*
			 * Remove only the ones selected
			 */
			for (String element : includeElements) {
				excludedElements.remove(element);
			}

			this.close();
		} catch (Exception e1) {
			System.err.println("Cannot parse the cutOff");
			this.close();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnSelectGene) {
			tableGenes.selectAll();
		} else if (e.getSource() == btnSelectRNA) {
			tableSRNAs.selectAll();
		} else if (e.getSource() == btnSelectAS) {
			tableASRNAs.selectAll();
		} else if (e.getSource() == btnDeselGene) {
			tableGenes.deselectAll();
		} else if (e.getSource() == btnDeselRNA) {
			tableSRNAs.deselectAll();
		} else if (e.getSource() == btnDeselAS) {
			tableASRNAs.deselectAll();
		} else if (e.getSource() == btnLoad) {
			try {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open a signature (list of genome elements)");
				String fileName = fd.open();
				if (fileName != null) {
					ArrayList<String> signature = TabDelimitedTableReader.readList(fileName);
					tableGenes.deselectAll();
					tableASRNAs.deselectAll();
					tableSRNAs.deselectAll();
					for (int i = 0; i < signature.size(); i++) {
						String element = signature.get(i);
						// System.out.println(element);
						// search in Gene list
						int index = listGenes.indexOf(element);
						if (index != -1) {
							tableGenes.select(index);
						}
						index = listsRNAs.indexOf(element);
						if (index != -1) {
							tableSRNAs.select(index);
						}
						index = listasRNAs.indexOf(element);
						if (index != -1) {
							tableASRNAs.select(index);
						}
					}
				}
			} catch (Exception e1) {
				System.out.println("Cannot read the signature");
			}
		} else if (e.getSource() == btnSave) {
			try {
				includeElements.clear();
				for (TableItem element : tableGenes.getSelection())
					includeElements.add(element.getText());
				for (TableItem element : tableSRNAs.getSelection())
					includeElements.add(element.getText());
				for (TableItem element : tableASRNAs.getSelection())
					includeElements.add(element.getText());
				ArrayList<String> signature = new ArrayList<>();
				for (String include : includeElements) {
					signature.add(include);
				}
				InternalBrowser.openList(signature, "Signature", partService);
			} catch (Exception e1) {
				System.out.println("Cannot save the signature");

			}
		} else if (e.getSource() == btnSelectSpecificList) {
			SignatureSelectionDialog dialog = new SignatureSelectionDialog(shell);
			if (dialog.open() == 0) {
				Signature signature = dialog.getSignature();
				System.out.println(signature.getName());
				tableGenes.deselectAll();
				tableASRNAs.deselectAll();
				tableSRNAs.deselectAll();
				for (int i = 0; i < signature.getSize(); i++) {
					String element = signature.getElements().get(i);
					// System.out.println(element);
					// search in Gene list
					int index = listGenes.indexOf(element);
					if (index != -1) {
						tableGenes.select(index);
					}
					index = listsRNAs.indexOf(element);
					if (index != -1) {
						tableSRNAs.select(index);
					}
					index = listasRNAs.indexOf(element);
					if (index != -1) {
						tableASRNAs.select(index);
					}
				}
			}

		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
