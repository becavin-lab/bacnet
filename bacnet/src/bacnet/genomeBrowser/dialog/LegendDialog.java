package bacnet.genomeBrowser.dialog;

import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.GeneExpression;
import bacnet.datamodel.dataset.NGS;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.ProteomicsData;
import bacnet.datamodel.dataset.Tiling;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.genomeBrowser.core.Track;
import bacnet.swt.ResourceManager;
import bacnet.utils.BasicColor;
import bacnet.utils.StringColor;

public class LegendDialog extends TitleAreaDialog implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3331773901037609884L;
	private final Track track;
	private ScrolledComposite scrolledComposite;
	private Table tableData;
	private Table tableBioCondition;
	private final Image imageTSS;
	private final Image imageRNASeq;
	private final Image imageTSSTilingGeneExpr;
	private final Image imageTilingGeneExpr;
	private final Image imageGeneExpr;
	private final Image imageChecked;
	private final Image imageUnchecked;
	private Button btnRemoveSelectedBiological;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public LegendDialog(Shell parentShell, Track track) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE);
		this.track = track;

		imageChecked = ResourceManager.getPluginImage("bacnet", "icons/checked.bmp");
		imageUnchecked = ResourceManager.getPluginImage("bacnet", "icons/unchecked.bmp");
		imageTSS = ResourceManager.getPluginImage("bacnet", "icons/tss.bmp");
		imageRNASeq = ResourceManager.getPluginImage("bacnet", "icons/rnaSeq.bmp");
		imageTSSTilingGeneExpr = ResourceManager.getPluginImage("bacnet", "icons/tssTilingGeneExpr.bmp");
		imageTilingGeneExpr = ResourceManager.getPluginImage("bacnet", "icons/TilingGeneExpr.bmp");
		imageGeneExpr = ResourceManager.getPluginImage("bacnet", "icons/GeneExpr.bmp");

	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Select biological condition and remove them from display, or click on a specific data to hide it ");
		setTitle("Legend and Data management");
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Composite composite_1 = new Composite(container, SWT.BORDER);
		composite_1.setLayout(new GridLayout(1, false));

		Label lblLegendAnnotation = new Label(composite_1, SWT.CENTER);
		lblLegendAnnotation.setText("Genome elements");

		Composite composite = new Composite(composite_1, SWT.NONE);
		composite.setLayout(new GridLayout(6, false));

		Label label = new Label(composite, SWT.BORDER);
		label.setBackground(BasicColor.REDLIGHT_GENE);
		label.setText("                ");

		Label lblGene = new Label(composite, SWT.NONE);
		lblGene.setText("Gene (+)");

		Label label_1 = new Label(composite, SWT.BORDER);
		label_1.setText("                ");
		label_1.setBackground(BasicColor.BLUELIGHT_GENE);

		Label lblGene_1 = new Label(composite, SWT.NONE);
		lblGene_1.setText("Gene (-)");

		Label label_3 = new Label(composite, SWT.BORDER);
		label_3.setText("                ");
		label_3.setBackground(BasicColor.ORANGE);

		Label lblOperons = new Label(composite, SWT.NONE);
		lblOperons.setText("Operon");

		Label label_6 = new Label(composite, SWT.BORDER);
		label_6.setText("                ");
		label_6.setBackground(BasicColor.LIGHT_NCRNA);

		Label lblNcrna = new Label(composite, SWT.NONE);
		lblNcrna.setText("tRNA and rRNA");

		Label label_7 = new Label(composite, SWT.BORDER);
		label_7.setText("                ");
		label_7.setBackground(BasicColor.LIGHTBLUE);

		Label lblPredictedTerminators = new Label(composite, SWT.NONE);
		lblPredictedTerminators.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblPredictedTerminators.setText("Predicted terminator");
		new Label(composite, SWT.NONE);

		Label label_4 = new Label(composite, SWT.BORDER);
		label_4.setText("                ");
		label_4.setBackground(BasicColor.LIGHT_SRNA);

		Label lblSmallRna = new Label(composite, SWT.NONE);
		lblSmallRna.setText("Small RNA");

		Label label_5 = new Label(composite, SWT.BORDER);
		label_5.setText("                ");
		label_5.setBackground(BasicColor.LIGHT_ASRNA);

		Label lblAsrna = new Label(composite, SWT.NONE);
		lblAsrna.setText("Antisense RNA");

		Label label_2 = new Label(composite, SWT.NONE);
		label_2.setText("                ");
		label_2.setBackground(BasicColor.LIGHT_CISREG);

		Label lblRiboswitch = new Label(composite, SWT.NONE);
		lblRiboswitch.setText("Riboswitch");

		Label lblBiologicalConditionDisplayed = new Label(container, SWT.NONE);
		lblBiologicalConditionDisplayed.setText("Biological Condition displayed");

		tableBioCondition = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd_tableBioCondition = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tableBioCondition.heightHint = 400;
		tableBioCondition.setLayoutData(gd_tableBioCondition);
		tableBioCondition.setHeaderVisible(true);
		tableBioCondition.setLinesVisible(true);

		btnRemoveSelectedBiological = new Button(container, SWT.NONE);
		btnRemoveSelectedBiological.setText("Remove selected biological condition");
		btnRemoveSelectedBiological.addSelectionListener(this);

		new Label(container, SWT.NONE);

		Label lblDataDisplayed = new Label(container, SWT.NONE);
		lblDataDisplayed.setText("Data displayed");

		tableData = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableData.setLinesVisible(true);
		tableData.setHeaderVisible(true);
		GridData gd_tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tableData.heightHint = 500;
		tableData.setLayoutData(gd_tableData);
		tableData.addSelectionListener(this);

		setBioCondition();
		setLegend();
		return container;
	}

	public void setBioCondition() {
		/*
		 * Prepare Legend in String Color file
		 */
		StringColor text = new StringColor();
		Color colorBackground = BasicColor.WHITE;
		for (BioCondition bioCondTemp : track.getDatas().getBioConditionHashMaps().values()) {
			if (bioCondTemp.getName().contains(" vs ")) {
				/*
				 * First bioCondition
				 */
				BioCondition bioCondition1 = BioCondition
						.getBioCondition(BioCondition.parseName(bioCondTemp.getName())[0]);
				String row = " " + "\t";
				row += bioCondition1.getName() + "\t";
				String typeDatacontained = bioCondition1.getTypeDataContained().toString().replace('[', ' ')
						.replace(']', ' ');
				if (bioCondition1.getTypeDataContained().size() == 0)
					typeDatacontained = "GeneExpr";
				if (typeDatacontained.contains("ExpressionMatrix"))
					typeDatacontained = typeDatacontained.replaceAll("ExpressionMatrix", "GeneExpr");
				typeDatacontained = typeDatacontained.replaceAll("GeneExpr", "GeneExpression");
				row += typeDatacontained.trim() + "\t";
				row += bioCondition1.getDate() + "\t";
				row += bioCondition1.getGrowth().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondition1.getTime() + "\t";
				row += bioCondition1.getTemperature().toString().replace('[', ' ').replace(']', ' ').replace('C', ' ')
						.trim() + "\t";
				row += bioCondition1.getLocalization() + "\t";
				row += bioCondition1.getMutant().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondition1.getMedia().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondition1.getMediaGrowthProperties().toString().replace('[', ' ').replace(']', ' ').trim()
						+ "\t";
				row += bioCondition1.getGenomeUsed() + "\t";
				row += bioCondition1.getGenomeName() + "\t";
				row += bioCondition1.getReference() + "\t";
				row += bioCondition1.getArrayExpressId() + "\t";
				row += bioCondition1.getArrayExpressTechnoId();
				text.addB(row + "\n", colorBackground);
				/*
				 * Second bioCondition
				 */
				BioCondition bioCondition2 = BioCondition
						.getBioCondition(BioCondition.parseName(bioCondTemp.getName())[1]);
				row = "vs" + "\t";
				row += bioCondition2.getName() + "\t";
				typeDatacontained = bioCondition2.getTypeDataContained().toString().replace('[', ' ').replace(']', ' ');
				if (bioCondition2.getTypeDataContained().size() == 0)
					typeDatacontained = "GeneExpr";
				if (typeDatacontained.contains("ExpressionMatrix"))
					typeDatacontained = typeDatacontained.replaceAll("ExpressionMatrix", "GeneExpr");
				typeDatacontained = typeDatacontained.replaceAll("GeneExpr", "GeneExpression");
				row += typeDatacontained.trim() + "\t";
				row += bioCondition2.getDate() + "\t";
				row += bioCondition2.getGrowth().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondition2.getTime() + "\t";
				row += bioCondition2.getTemperature().toString().replace('[', ' ').replace(']', ' ').replace('C', ' ')
						.trim() + "\t";
				row += bioCondition2.getLocalization() + "\t";
				row += bioCondition2.getMutant().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondition2.getMedia().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondition2.getMediaGrowthProperties().toString().replace('[', ' ').replace(']', ' ').trim()
						+ "\t";
				row += bioCondition2.getGenomeUsed() + "\t";
				row += bioCondition2.getGenomeName() + "\t";
				row += bioCondition2.getReference() + "\t";
				row += bioCondition2.getArrayExpressId() + "\t";
				row += bioCondition2.getArrayExpressTechnoId();
				text.addB(row + "\n", colorBackground);
			} else {
				String row = " " + "\t";
				row += bioCondTemp.getName() + "\t";
				String typeDatacontained = bioCondTemp.getTypeDataContained().toString().replace('[', ' ').replace(']',
						' ');
				if (bioCondTemp.getTypeDataContained().size() == 0)
					typeDatacontained = "GeneExpr";
				if (typeDatacontained.contains("ExpressionMatrix"))
					typeDatacontained = typeDatacontained.replaceAll("ExpressionMatrix", "GeneExpr");
				typeDatacontained = typeDatacontained.replaceAll("GeneExpr", "GeneExpression");
				row += typeDatacontained.trim() + "\t";
				row += bioCondTemp.getDate() + "\t";
				row += bioCondTemp.getGrowth().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondTemp.getTime() + "\t";
				row += bioCondTemp.getTemperature().toString().replace('[', ' ').replace(']', ' ').replace('C', ' ')
						.trim() + "\t";
				row += bioCondTemp.getLocalization() + "\t";
				row += bioCondTemp.getMutant().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondTemp.getMedia().toString().replace('[', ' ').replace(']', ' ').trim() + "\t";
				row += bioCondTemp.getMediaGrowthProperties().toString().replace('[', ' ').replace(']', ' ').trim()
						+ "\t";
				row += bioCondTemp.getGenomeUsed() + "\t";
				row += bioCondTemp.getGenomeName() + "\t";
				row += bioCondTemp.getReference() + "\t";
				row += bioCondTemp.getArrayExpressId() + "\t";
				row += bioCondTemp.getArrayExpressTechnoId();
				text.addB(row + "\n", colorBackground);
			}
			if (colorBackground.equals(BasicColor.WHITE))
				colorBackground = BasicColor.LIGHTGREY;
			else
				colorBackground = BasicColor.WHITE;
		}

		/*
		 * Display Legend
		 */
		tableBioCondition.removeAll();
		if (text.str.length() != 0) {
			String[] lines = text.str.split("\n");
			int k = 0;
			tableBioCondition.setHeaderVisible(true);
			String[] titles = { " ", "BioCondition", "Type", "Date", "Growth", "TimePoint", "C", "Localization",
					"Mutant", "Media", "MediaGrowthProperties", "Strain used", "Strain array", "Reference",
					"ArrayExpressId", "ArrayExpressTechnoID" };
			for (int i = 0; i < titles.length; i++) {
				TableColumn column = new TableColumn(tableBioCondition, SWT.NONE);
				column.setText(titles[i]);
				column.setAlignment(SWT.CENTER);
			}
			for (String line : lines) {
				TableItem item = new TableItem(tableBioCondition, SWT.NONE);
				for (int i = 0; i < line.split("\t").length; i++) {
					item.setText(i, line.split("\t")[i]);
					item.setBackground(i, text.colorBackground[k]);
				}
				k = k + (line.length() + 1);
			}
			for (int i = 0; i < titles.length; i++) {
				tableBioCondition.getColumn(i).pack();
			}
			tableBioCondition.update();
			tableBioCondition.redraw();
		}

	}

	public void setLegend() {
		/*
		 * Prepare Legend in String Color file
		 */
		StringColor text = new StringColor();
		Color colorBackground = BasicColor.WHITE;
		for (BioCondition bioCondition : track.getDatas().getBioConditionHashMaps().values()) {
			for (OmicsData tscData : bioCondition.getOmicsData()) {
				String display = "Display";
				if (track.getDatas().getDataNOTDisplayed().contains(tscData.getName()))
					display = "NoDisplay";
				String type = "";
				if (tscData instanceof Tiling) {
					if (tscData.getName().contains("+"))
						type = "Tiling +";
					else
						type = "Tiling -";
				} else if (tscData instanceof GeneExpression) {
					type = "GeneExpression";
				} else if (tscData instanceof NGS) {
					if (tscData.getName().contains("+") || tscData.getName().contains("_f")) {
						type = "RNASeq +";
					} else if (tscData.getName().contains("-") || tscData.getName().contains("_r")) {
						type = "RNASeq -";
					} else
						type = "RNASeq no";
				} else if (tscData instanceof NTermData) {
					type = "NTerm";
				} else if (tscData instanceof ProteomicsData) {
					type = "Proteome";
				} else if (tscData instanceof ExpressionMatrix) {
					type = "ExpressionMatrix";
				}
				String info = display + "\t" + type + "\t" + tscData.getName() + "\t" + bioCondition.getName();
				text.add(info + "\n", track.getDatas().getDataColors().get(tscData.getName()), colorBackground);
			}
			if (colorBackground.equals(BasicColor.WHITE))
				colorBackground = BasicColor.LIGHTGREY;
			else
				colorBackground = BasicColor.WHITE;
		}

		/*
		 * Display Legend
		 */
		tableData.removeAll();
		if (text.str.length() != 0) {
			String[] lines = text.str.split("\n");
			int k = 0;
			tableData.setHeaderVisible(true);
			String[] titles = { "Display", "TypeData", "Color", "Data Name", "Biological Condition" };
			for (int i = 0; i < titles.length; i++) {
				TableColumn column = new TableColumn(tableData, SWT.NONE);
				column.setText(titles[i]);
				column.setAlignment(SWT.CENTER);
			}
			for (String line : lines) {
				TableItem item = new TableItem(tableData, SWT.NONE);
				item.setText(0, "");
				item.setBackground(0, text.colorBackground[k]);
				if (line.split("\t")[0].equals("Display"))
					item.setImage(0, imageChecked);
				else if (line.split("\t")[0].equals("NoDisplay"))
					item.setImage(0, imageUnchecked);
				item.setText(1, line.split("\t")[1]);
				item.setBackground(1, text.colorBackground[k]);
				item.setText(2, "");
				item.setBackground(2, text.colorForeground[k]);
				String dataName = line.split("\t")[2];
				if (Database.getInstance().getProjectName() == Database.CRISPRGO_PROJECT) {
					if (dataName.equals("dCas9_induction_f"))
						dataName = "+1 strand";
					if (dataName.equals("dCas9_induction_r"))
						dataName = "-1 strand";
				}
				item.setText(3, dataName);
				item.setBackground(3, text.colorBackground[k]);
				item.setText(4, line.split("\t")[3]);
				item.setBackground(4, text.colorBackground[k]);
				k = k + (line.length() + 1);
			}
			for (int i = 0; i < titles.length; i++) {
				tableData.getColumn(i).pack();
			}
			tableData.update();
			tableData.redraw();
		}

	}

	@Override
	public void okPressed() {
		track.getDatas().getDataNOTDisplayed().clear();
		for (TableItem item : tableData.getItems()) {
			Image checked = item.getImage(0);
			if (checked.equals(imageUnchecked)) {
				System.out.println("add " + item.getText(3));
				track.getDatas().getDataNOTDisplayed().add(item.getText(3));
				System.out.println();
			}
		}

		this.close();
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
		return new Point(949, 899);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == tableData) {
			int index = tableData.getSelectionIndex();
			if (tableData.getItem(index).getImage().equals(imageChecked)) {
				tableData.getItem(index).setImage(0, imageUnchecked);
			} else if (tableData.getItem(index).getImage().equals(imageUnchecked)) {
				tableData.getItem(index).setImage(0, imageChecked);
			}
			tableData.update();
			tableData.redraw();
		} else if (e.getSource() == btnRemoveSelectedBiological) {
			TreeSet<String> removeBioCondition = new TreeSet<String>();
			for (int index : tableBioCondition.getSelectionIndices()) {
				TableItem item = tableBioCondition.getItem(index);
				if (item.getText(0).equals("vs")) {
					String bioCond1 = tableBioCondition.getItem(index - 1).getText(1);
					String bioCond2 = tableBioCondition.getItem(index).getText(1);
					removeBioCondition.add(bioCond1 + " vs " + bioCond2);
				} else if ((index + 1) < tableBioCondition.getItemCount()) {
					if (tableBioCondition.getItem(index + 1).getText(0).equals("vs")) {
						String bioCond1 = tableBioCondition.getItem(index).getText(1);
						String bioCond2 = tableBioCondition.getItem(index + 1).getText(1);
						removeBioCondition.add(bioCond1 + " vs " + bioCond2);
					} else {
						String bioCond = tableBioCondition.getItem(index).getText(1);
						removeBioCondition.add(bioCond);
					}
				} else {
					String bioCond = tableBioCondition.getItem(index).getText(1);
					removeBioCondition.add(bioCond);
				}
			}
			for (String bioCondName : removeBioCondition) {
				track.getDatas().getBioConditionHashMaps().remove(bioCondName);
			}
			setBioCondition();
			setLegend();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public ScrolledComposite getScrolledComposite() {
		return scrolledComposite;
	}

	public void setScrolledComposite(ScrolledComposite scrolledComposite) {
		this.scrolledComposite = scrolledComposite;
	}

	public Table getTableBioCondition() {
		return tableData;
	}

	public void setTableBioCondition(Table tableBioCondition) {
		this.tableData = tableBioCondition;
	}

	public Table getTable() {
		return tableBioCondition;
	}

	public void setTable(Table table) {
		this.tableBioCondition = table;
	}

	public Track getTrack() {
		return track;
	}

	public Image getImageTSS() {
		return imageTSS;
	}

	public Image getImageRNASeq() {
		return imageRNASeq;
	}

	public Image getImageTSSTilingGeneExpr() {
		return imageTSSTilingGeneExpr;
	}

	public Image getImageTilingGeneExpr() {
		return imageTilingGeneExpr;
	}

	public Image getImageGeneExpr() {
		return imageGeneExpr;
	}

	public Image getImageChecked() {
		return imageChecked;
	}

	public Image getImageUnchecked() {
		return imageUnchecked;
	}
}
