package awt.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import awt.graphics.BoxPlotView;
import awt.graphics.HistogramView;
import awt.graphics.ScatterPlotView;
import awt.svdmds.RunSVDMDS;
import awt.table.gui.CytoscapeExportDialog;
import awt.table.gui.FilterExpressionMatrixDialog;
import awt.table.gui.HeatMapColorEditor;
import awt.table.gui.HeatMapDoubleRenderer;
import awt.table.gui.HeatMapStringRenderer;
import awt.table.gui.HeatMapTableModel;
import awt.table.gui.HideExpressionMatrixDialog;
import awt.table.wizards.ColorMapperRCPWizard;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.swt.ResourceManager;
import bacnet.table.core.ColorMapper;
import bacnet.table.core.ColorMapperRCPList;
import bacnet.table.core.FilterList;
import bacnet.utils.FileUtils;
import bacnet.utils.TableHTMLExport;

public class TableAWTView implements SelectionListener{

	public static final String ID = "bacnet.rcp.HeatMapViewAWT"; //$NON-NLS-1$

	// data 
	private ColorMapperRCPList colorMapperList = new ColorMapperRCPList();
	private ExpressionMatrix matrix;
	private FilterList filters;
	private TreeSet<String> excludeRow = new TreeSet<String>();
	private TreeSet<String> excludeColumn = new TreeSet<String>();
	private TreeSet<String> hideDialogExcludeRow = new TreeSet<String>();
	private TreeSet<String> hideDialogExcludeColumn = new TreeSet<String>();

	// GUI 
	private Button btnColorProperties;
	private JTable table;
	private JScrollPane jScrollPane;
	private Button btnDisplayValues;
	private Button btnExportToImage;
	private Text textWidth;
	private Text textHeight;
	private Scale scaleCellHeight;
	private Scale scaleCellWidth;
	private Text searchText;
	private TableRowSorter<HeatMapTableModel> sorter;
	private Composite composite;
	private Button btnFileExport;
	private Button btnDisplayHeatmap;
	private Button btnHide;
	private Composite composite_7;
	private Button btnFilter;
	private Button btnCog;
	private Button btnBoxPlot;
	private Button btnHistogram;
	private Button btnDisplaygenome;

	// GUI properties
	private int cellWidth = 60;
	private int cellHeight = 30;
	//private int textSize = 12;
	private Composite composite_5;
	private Button btnScatterPlot;
	private Button btnExportSVG;

	// only for representing heatmap sRNA2011
	private boolean inocua = false;
	private int type = -1;
	private Button btnVolcanoPlot;
	private Composite composite_3;
	private Label lblSize;
	private Button btnSigSave;
	private Button btnCytoscapeExport;
	private Button btnHtmlexport;

	
	@Inject
	EPartService partService;
	
	@ Inject
	@ Named (IServiceConstants.ACTIVE_SHELL)
	private Shell shell;	
	
	@Inject
	public TableAWTView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));

		composite_5 = new Composite(composite_1, SWT.NONE);
		composite_5.setLayout(new GridLayout(5, false));

		btnFileExport = new Button(composite_5, SWT.NONE);
		btnFileExport.setToolTipText("Save table in txt file");
		btnFileExport.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/txt.bmp"));
		btnFileExport.setBounds(0, 0, 75, 25);
		btnFileExport.addSelectionListener(this);

		btnExportToImage = new Button(composite_5, SWT.NONE);
		btnExportToImage.setToolTipText("Save table in PNG image");
		btnExportToImage.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/png.bmp"));
		btnExportToImage.setSize(95, 25);
		btnExportToImage.addSelectionListener(this);

		btnExportSVG = new Button(composite_5, SWT.NONE);
		btnExportSVG.setToolTipText("Save table in SVG vector image (Illustrator)");
		btnExportSVG.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/svg.bmp"));
		btnExportSVG.addSelectionListener(this);

		btnSigSave = new Button(composite_5, SWT.NONE);
		btnSigSave.setToolTipText("Save the list of genes in the first column as a Signature file");
		btnSigSave.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/sigSave.bmp"));
		btnSigSave.addSelectionListener(this);

		btnDisplayValues = new Button(composite_5, SWT.CHECK);
		btnDisplayValues.setSelection(true);
		btnDisplayValues.setToolTipText("Show/hide values");
		btnDisplayValues.setSize(95, 16);
		btnDisplayValues.setText("Values");
		//Set up renderer and editor for the Favorite Color column.
		btnDisplayValues.addSelectionListener(this);

		btnCog = new Button(composite_5, SWT.NONE);
		btnCog.setToolTipText("Display COGs expression matrix");
		btnCog.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/COG.bmp"));
		btnCog.addSelectionListener(this);

		btnVolcanoPlot = new Button(composite_5, SWT.NONE);
		btnVolcanoPlot.setToolTipText("VolcanoPlot");
		btnVolcanoPlot.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/volcanoplot.bmp"));
		btnVolcanoPlot.addSelectionListener(this);
		btnColorProperties = new Button(composite_5, SWT.NONE);
		btnColorProperties.setToolTipText("Set color mapper");
		btnColorProperties.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/colorMapper.bmp"));
		btnColorProperties.addSelectionListener(this);

		btnCytoscapeExport = new Button(composite_5, SWT.NONE);
		btnCytoscapeExport.setToolTipText("Save the information into a cytoscape file, to represent them into a network");
		btnCytoscapeExport.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/cytoscape.bmp"));
		btnCytoscapeExport.addSelectionListener(this);

		//	     TableColumn col = table.getColumnModel().getColumn(table.getColumnCount()-1);
		//		col.setPreferredWidth(200);
		//		cellWidth = 170;




		btnDisplayHeatmap = new Button(composite_5, SWT.CHECK);
		btnDisplayHeatmap.setToolTipText("Show/hide heatmap");
		btnDisplayHeatmap.setSelection(true);
		btnDisplayHeatmap.setSize(297, 468);
		btnDisplayHeatmap.setText("HeatMap");
		btnDisplayHeatmap.addSelectionListener(this);

		btnBoxPlot = new Button(composite_5, SWT.NONE);
		btnBoxPlot.setToolTipText("Display boxplot");
		btnBoxPlot.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/boxplotView.bmp"));
		btnBoxPlot.addSelectionListener(this);
		btnHistogram = new Button(composite_5, SWT.NONE);
		btnHistogram.setToolTipText("Display histogram");
		btnHistogram.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/histoView.bmp"));
		btnHistogram.addSelectionListener(this);
		btnScatterPlot = new Button(composite_5, SWT.NONE);
		btnScatterPlot.setToolTipText("2D representation (SVDMDS)");
		btnScatterPlot.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/scatter.bmp"));
		btnScatterPlot.addSelectionListener(this);

		btnHtmlexport = new Button(composite_5, SWT.NONE);
		btnHtmlexport.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/htmlSave.bmp"));
		btnHtmlexport.addSelectionListener(this);
		btnDisplaygenome = new Button(composite_5, SWT.NONE);
		btnDisplaygenome.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/genome/genomeViewer.bmp"));
		btnDisplaygenome.addSelectionListener(this);
		Composite composite_2 = new Composite(container, SWT.NONE);
		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_composite_2.widthHint = 1000;
		composite_2.setLayoutData(gd_composite_2);
		composite_2.setLayout(new GridLayout(1, false));
		{

			composite_7 = new Composite(composite_2, SWT.NONE);
			composite_7.setLayout(new GridLayout(2, false));

			composite_3 = new Composite(composite_7, SWT.NONE);
			composite_3.setLayout(new GridLayout(2, false));

			Label lblCellWidth = new Label(composite_3, SWT.NONE);
			lblCellWidth.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblCellWidth.setText("Cell width");

			textWidth = new Text(composite_3, SWT.BORDER);
			textWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Composite composite_4_1 = new Composite(composite_7, SWT.NONE);
			composite_4_1.setLayout(new GridLayout(2, false));

			Label lblCellHeight = new Label(composite_4_1, SWT.NONE);
			lblCellHeight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblCellHeight.setText("Cell height");

			textHeight = new Text(composite_4_1, SWT.BORDER);
			textHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			textHeight.addSelectionListener(this);
			textHeight.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ke) {
					try{
						cellHeight = Integer.parseInt(textHeight.getText().trim());
						scaleCellHeight.setSelection(cellHeight);
						setMargin();
						table.setRowHeight(scaleCellHeight.getSelection());
						filterTable();
					}catch(Exception e1){

					}
				}
			});
			textWidth.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ke) {
					try{
						cellWidth = Integer.parseInt(textWidth.getText().trim());
						scaleCellWidth.setSelection(cellWidth);
						setMargin();
						filterTable();	
						table.repaint();
					}catch(Exception e1){

					}
				}
			});
			scaleCellWidth = new Scale(composite_7, SWT.NONE);
			GridData gd_scaleCellWidth = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_scaleCellWidth.widthHint = 125;
			scaleCellWidth.setLayoutData(gd_scaleCellWidth);
			scaleCellWidth.setSize(170, 42);
			scaleCellWidth.setToolTipText("Cell width");
			scaleCellWidth.setMaximum(250);
			scaleCellWidth.setMinimum(1);
			scaleCellWidth.setSelection(cellWidth);
			scaleCellWidth.addSelectionListener(this);

			scaleCellHeight = new Scale(composite_7, SWT.NONE);
			GridData gd_scaleCellHeight = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_scaleCellHeight.widthHint = 121;
			scaleCellHeight.setLayoutData(gd_scaleCellHeight);
			scaleCellHeight.setSize(170, 42);
			scaleCellHeight.setMinimum(1);
			scaleCellHeight.setSelection(cellHeight);
			scaleCellHeight.setToolTipText("Cell height");
			scaleCellHeight.addSelectionListener(this);
		}

		//		ExpressionMatrix stochast = ExpressionMatrix.createStochasticMatrix(10, 5);
		//	     matrices = new ArrayList<ExpressionMatrix>();
		//	     matrices.add(stochast);

		matrix = ModelProviderRCP.INSTANCE.getMatrix();
		colorMapperList = new ColorMapperRCPList(matrix,shell);
		filters = new FilterList(matrix);

		HeatMapTableModel model = new HeatMapTableModel();

		System.out.println("Display type "+type);
		model.setHeatMapTableModel(matrix,type);
		model.setColorMapper(colorMapperList);

		table = new JTable(model);
		sorter = new TableRowSorter<HeatMapTableModel>(model);
		//table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setColumnSelectionAllowed(true);

		table.setDefaultRenderer(Double.class,new HeatMapDoubleRenderer(btnDisplayValues.getSelection(),btnDisplayHeatmap.getSelection(),colorMapperList,inocua));
		table.setDefaultRenderer(String.class,new HeatMapStringRenderer(colorMapperList));
		table.setDefaultEditor(Color.class,new HeatMapColorEditor());
		table.setRowSorter(sorter);
		table.setRowHeight(cellHeight);
		scaleCellWidth.setSelection(cellWidth);

		btnHide = new Button(composite_7, SWT.NONE);
		btnHide.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnHide.setText("Hide Row/Column");
		btnHide.addSelectionListener(this);

		Composite composite_4 = new Composite(composite_7, SWT.NONE);
		composite_4.setLayout(new GridLayout(3, false));

		btnFilter = new Button(composite_4, SWT.NONE);
		btnFilter.setToolTipText("Create different filters");
		GridData gd_btnFilter = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnFilter.widthHint = 50;
		btnFilter.setLayoutData(gd_btnFilter);
		btnFilter.setText("Filters");
		btnFilter.addSelectionListener(this);

		searchText = new Text(composite_4, SWT.BORDER | SWT.SEARCH);
		GridData gd_searchText = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_searchText.widthHint = 71;
		searchText.setLayoutData(gd_searchText);

		lblSize = new Label(composite_4, SWT.NONE);
		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				//searchIncludeRow.clear();
				RowFilter<HeatMapTableModel,Integer> rowFilter = new RowFilter<HeatMapTableModel,Integer>() {
					public boolean include(Entry<? extends HeatMapTableModel, ? extends Integer> entry) {
						HeatMapTableModel model = entry.getModel();
						String rowName = (String) model.getValueAt(entry.getIdentifier(), 0);
						for(int i=0;i<model.getColumnCount();i++){
							String value = String.valueOf(model.getValueAt(entry.getIdentifier(), i));
							if(value.contains(searchText.getText())){
								if(excludeRow.contains(rowName)){
									return false;
								}
								return true;
							}
						}
						// if searchText has not been found
						return false;
					}
				};
				sorter.setRowFilter(rowFilter);
			}
		});
		for(int i=0;i<table.getColumnCount();i++){
			TableColumn col = table.getColumnModel().getColumn(i);
			col.setResizable(true);
			if(i!=(table.getColumnCount()-1)) col.setPreferredWidth(cellWidth);
		}
		//	     for(int i=0;i<table.getRowCount();i++){
		//	     	searchIncludeRow.add(String.valueOf(model.getValueAt(i, 0)));
		//	     }

		//Create the scroll pane and add the table to it.
		jScrollPane = new JScrollPane(table);
		composite = new Composite(container, SWT.EMBEDDED);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, true, true, 3, 2);
		gd_composite.heightHint = 800;
		gd_composite.widthHint = 1600;
		composite.setLayoutData(gd_composite);
		Frame frame = SWT_AWT.new_Frame(composite);
		Panel panel = new Panel(new BorderLayout()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7131774662613327539L;

			public void update(java.awt.Graphics g) {
				/* Do not erase the background */
				paint(g);
			}
		};
		panel.add(jScrollPane, BorderLayout.CENTER);
		frame.add(panel);


		try {
			System.setProperty("sun.awt.noerasebackground", "true");
		} catch (NoSuchMethodError error) {
		}

		/* Create and setting up frame */
		updateLblInfo();
		setMargin();

		filterTable();
		filterTable();

	}

	private void updateLblInfo(){
		textWidth.setText(cellWidth+"    ");
		textHeight.setText(cellHeight+"   ");
		int nbColumn = matrix.getHeaders().size() + matrix.getHeaderAnnotation().size() - excludeColumn.size();
		int nbRow = matrix.getNumberRow() - excludeRow.size();
		lblSize.setText("Nb row: "+nbRow+" Nb column: "+nbColumn);
	}

	private void filterTable(){

		// filter Row
		RowFilter<HeatMapTableModel,Integer> rowFilter = new RowFilter<HeatMapTableModel,Integer>() {
			public boolean include(Entry<? extends HeatMapTableModel, ? extends Integer> entry) {
				HeatMapTableModel model = entry.getModel();
				String rowName = (String) model.getValueAt(entry.getIdentifier(), 0);
				//System.out.println(entry.getIdentifier());
				if(excludeRow.contains(rowName)){
					return false;
				}else return true;
			}
		};
		sorter.setRowFilter(rowFilter);

		// filter Column
		for(int i=0;i<table.getColumnCount();i++){
			TableColumn col = table.getColumnModel().getColumn(i);
			if(excludeColumn.contains(col.getHeaderValue())){
				col.setPreferredWidth(0);
				col.setMaxWidth(0);
				col.setMinWidth(0);
			}else{
				col.setPreferredWidth(scaleCellWidth.getSelection());
				col.setMaxWidth(2000);
				col.setMinWidth(0);
			}
		}
	}

	private void updateExcludeElements(){
		excludeColumn.clear();
		excludeRow.clear();

		for(String col : filters.getExcludeColumn()) excludeColumn.add(col);
		for(String row : filters.getExcludeRow()) excludeRow.add(row);
		for(String col : hideDialogExcludeColumn) excludeColumn.add(col);
		for(String row : hideDialogExcludeRow) excludeRow.add(row);

	}

	private void setMargin(){
		if(scaleCellWidth.getSelection()<10 || scaleCellHeight.getSelection()<15 || type==0){
			table.setIntercellSpacing(new Dimension(0, 0));

		}else{
			table.setIntercellSpacing(new Dimension(1, 1));
		}
		//		table.setIntercellSpacing(new Dimension(1, 1));
	}

	private ExpressionMatrix getSubMatrix(){
		ArrayList<String> includeRow = new ArrayList<String>();
		ArrayList<String> includeColumn = new ArrayList<String>();

		for(String rowName : matrix.getRowNamesToList()){
			if(!excludeRow.contains(rowName)) includeRow.add(rowName);
		}
		for(String col : matrix.getHeaders()){
			if(!excludeColumn.contains(col)) includeColumn.add(col);
		}
		for(String col : matrix.getHeaderAnnotation()){
			if(!excludeColumn.contains(col)) includeColumn.add(col);
		}

		return matrix.getSubMatrix(includeRow, includeColumn);
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnColorProperties){
			ColorMapperRCPWizard wizard = new ColorMapperRCPWizard(colorMapperList,shell);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			int dia = dialog.open();
			if(dia==0){
				colorMapperList = wizard.getColorMapper();
				table.repaint();
				updateLblInfo();
			}
		}else if(e.getSource()==btnDisplayValues || e.getSource()==btnDisplayHeatmap){
			table.setDefaultRenderer(Double.class,new HeatMapDoubleRenderer(btnDisplayValues.getSelection(),btnDisplayHeatmap.getSelection(),colorMapperList,inocua));
			table.repaint();
		}else if(e.getSource()==btnScatterPlot){
			ExpressionMatrix matrix = getSubMatrix();
			matrix = matrix.getMissingValuesFreeMatrix();
			RunSVDMDS.run(matrix,partService);
		}else if(e.getSource()==btnVolcanoPlot){
			ExpressionMatrix matrix = getSubMatrix();
			matrix = matrix.getMissingValuesFreeMatrix();
			ModelProviderRCP.INSTANCE.setMatrix(matrix);
			String id = ScatterPlotView.ID+Math.random(); 
			ResourceManager.openView(partService, ScatterPlotView.ID, id);
		}else if(e.getSource()==btnBoxPlot){
			BoxPlotView.displayMatrix(matrix, matrix.getName(),partService);
			
		}else if(e.getSource()==btnHistogram){
			HistogramView.displayMatrix(matrix, matrix.getName(),partService);
		}else if(e.getSource()==btnHide){
			HideExpressionMatrixDialog dialog = new HideExpressionMatrixDialog(matrix, hideDialogExcludeRow, hideDialogExcludeColumn,shell,partService);
			int ok = dialog.open();
			if(ok == Status.OK){
				updateExcludeElements();
				updateLblInfo();
				filterTable();
				// For an obscure reason, filterTable has to be run two times if we want the col size change to be effective.
				filterTable();  //
			}
		}else if(e.getSource()==btnFilter){
			FilterExpressionMatrixDialog dialog = new FilterExpressionMatrixDialog(filters, shell);
			int ok = dialog.open();
			if(ok == Status.OK){
				updateExcludeElements();
				updateLblInfo();
				filterTable();
				// For an obscure reason, filterTable has to be run two times if we want the col size change to be effective.
				filterTable();  //
			}
		}else if(e.getSource()==scaleCellHeight){
			setMargin();
			table.setRowHeight(scaleCellHeight.getSelection());
			textHeight.setText(scaleCellHeight.getSelection()+"");
			cellHeight = scaleCellHeight.getSelection();
			filterTable();
		}else if(e.getSource()==scaleCellWidth){
			setMargin();
			cellWidth = scaleCellWidth.getSelection();
			textWidth.setText(cellWidth+"");
			filterTable();	
			table.repaint();
		}else if(e.getSource()==btnCog){
			ExpressionMatrix cogExpression = GenomeNCBITools.getCOGExpression(matrix);
			TableAWTView.displayMatrix(cogExpression, "COG expression",partService);
		}else if(e.getSource()==btnDisplaygenome){
			//GenomeViewDisplayHandler.DisplayExpressionMatrix(getSubMatrix());
		}else if(e.getSource()==btnFileExport){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the table to: ");
			//fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(HEATMAP_View_Pref));
			fd.setFileName(Database.getInstance().getPATH()+FileUtils.removeExtension(matrix.getName())+".txt");
			String[] filterExt = {"*.txt","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				getSubMatrix().saveTab(fileName,matrix.getFirstRowName());
			} catch (Exception ex) {
				System.out.println("Cannot save the table");
			}
		}else if(e.getSource()==btnExportToImage){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the image to: ");
			//fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
			fd.setFileName(Database.getInstance().getPATH()+FileUtils.removeExtension(matrix.getName())+".png");
			String[] filterExt = {"*.png","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				BufferedImage tamponSauvegarde = new BufferedImage(table.getWidth(),table.getHeight(), BufferedImage.TYPE_INT_RGB); 
				Graphics g = tamponSauvegarde.getGraphics();
				table.paint(g); 
				ImageIO.write(tamponSauvegarde, "PNG", new File(fileName));
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}else if(e.getSource()==btnHtmlexport){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the image to: ");
			//fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
			fd.setFileName(Database.getInstance().getPATH()+FileUtils.removeExtension(matrix.getName())+".png");
			String[] filterExt = {"*.png","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			if(fileName!=null){
				TableHTMLExport.exportInHTML(table, fileName);
			}
		}else if(e.getSource()==btnExportSVG){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the SVG image to: ");
			//fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()+getPartName()+".svg"));
			fd.setFileName(Database.getInstance().getPATH()+FileUtils.removeExtension(matrix.getName())+".svg");
			String[] filterExt = {"*.svg","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				ImageExportUtilsAWT.exportAWTasSVG(table,fileName);
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}else if(e.getSource()==btnSigSave){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the list of genes to: ");
			//fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()+getPartName()+".svg"));
			fd.setFileName(Database.getInstance().getPATH()+FileUtils.removeExtension(matrix.getName())+".txt");
			String[] filterExt = {"*.txt","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				String text = "";
				if(matrix.isOrdered()){
					for(String rowName : matrix.getOrderedRowNames()){
						if(!excludeRow.contains(rowName)) text+=rowName+"\n";
					}
				}else{
					for(String rowName : matrix.getRowNamesToList()){
						if(!excludeRow.contains(rowName)) text+=rowName+"\n";
					}
				}
				FileUtils.saveText(text, fileName);
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}else if(e.getSource()==btnCytoscapeExport){
			CytoscapeExportDialog dialog = new CytoscapeExportDialog(shell, getSubMatrix());
			dialog.open();
		}
	}

	public void saveSVG(String fileName){
		try {
			ImageExportUtilsAWT.exportAWTasSVG(table,fileName);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Static function which open an HeatMapView and display corresponding ExpressionMatrix
	 * @param matrix
	 * @param viewName
	 * @throws PartInitException
	 */
	public static void displayMatrix(ExpressionMatrix matrix, String viewName,EPartService partService){
		displayMatrix(matrix, viewName,false,"",partService);
	}

	public static void displayMatrix(ExpressionMatrix matrix, String viewName,boolean saveSVG,String fileName,EPartService partService){
		ModelProviderRCP.INSTANCE.setMatrix(matrix);
		String id = TableAWTView.ID+Math.random();
		ResourceManager.openView(partService, TableAWTView.ID, id);
		MPart part = partService.findPart(id);
		TableAWTView view = (TableAWTView) part.getObject();
		part.setLabel(viewName);
		if(saveSVG){
			view.saveSVG(fileName);
		}
		
	}

	public static void displayMatrix(ExpressionMatrix matrix, String viewName, ColorMapper colorMapper,EPartService partService){
		matrix.setName(-2+"");
		displayMatrix(matrix, viewName,false,"",partService);
	}

}
