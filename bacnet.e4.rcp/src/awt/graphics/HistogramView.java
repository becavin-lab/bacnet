package awt.graphics;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JScrollPane;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import awt.table.ModelProviderRCP;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.ResourceManager;

public class HistogramView implements SelectionListener{

	public static String ID = "bacnet.rcp.HistogramView";

	private ExpressionMatrix matrix;
	private ArrayList<String> includeColumn = new ArrayList<String>();
	private HistogramDataset dataset;
	private JFreeChart chart;
	private Rectangle2D bounds;
	private Button btnSavePNG;
	private LegendTitle legendTitle;
	private static int BINS_NUMBER = 100;
	private ChartPanel chartPanel;
	private Button btnSelectColumn;
	private Button btnDisplayLegend;
	private Text textBinNB;
	private Button btnRefresh;

	@Inject
	EPartService partService;
	
	@ Inject
	@ Named (IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	
	@Inject
	public HistogramView() {
		// TODO Auto-generated constructor stub
	}
	
	
	@PostConstruct
	public void createPartControl(Composite parent) {

		matrix = ModelProviderRCP.INSTANCE.getMatrix();
		selectColumn();
		setData();

		String plotTitle = "Histogram"; 
		String xaxis = "number";
		String yaxis = "Expression"; 
		PlotOrientation orientation = PlotOrientation.VERTICAL; 
		boolean show = true;    // include legend
		boolean toolTips = true;    // tooltips
		boolean urls = false;         // urls
		chart = ChartFactory.createHistogram( plotTitle, xaxis, yaxis,dataset, orientation, show, toolTips, urls);


		parent.setLayout(new GridLayout(1, false));

		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayout(new GridLayout(7, false));

		btnSelectColumn = new Button(composite_1, SWT.NONE);
		btnSelectColumn.setText("Select column");
		btnSelectColumn.addSelectionListener(this);

		btnDisplayLegend = new Button(composite_1, SWT.CHECK);
		btnDisplayLegend.setSelection(true);
		btnDisplayLegend.setText("Display legend");
		btnDisplayLegend.addSelectionListener(this);

		btnRefresh = new Button(composite_1, SWT.NONE);
		btnRefresh.setToolTipText("Refresh display");
		btnRefresh.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/update.bmp"));
		btnRefresh.addSelectionListener(this);

		Label lblBins = new Label(composite_1, SWT.NONE);
		lblBins.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBins.setText("Bins");

		textBinNB = new Text(composite_1, SWT.BORDER);
		textBinNB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textBinNB.setText(BINS_NUMBER+"  ");
		new Label(composite_1, SWT.NONE);
		btnSavePNG = new Button(composite_1, SWT.NONE);
		btnSavePNG.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/png.bmp"));
		btnSavePNG.addSelectionListener(this);

		Composite composite = new Composite(parent, SWT.EMBEDDED);
		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_composite_1.heightHint = 700;
		gd_composite_1.widthHint = 1000;

		composite.setLayoutData(gd_composite_1);

		chartPanel = new ChartPanel(chart);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setZoomAroundAnchor(true);
		bounds = chartPanel.getBounds();
		Frame frame = SWT_AWT.new_Frame(composite);
		Panel panel = new Panel(new BorderLayout()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3507348834435906150L;

			public void update(java.awt.Graphics g) {
				/* Do not erase the background */
				paint(g);
			}
		};
		JScrollPane jScrollPane = new JScrollPane(chartPanel);
		panel.add(jScrollPane, BorderLayout.CENTER);
		frame.add(panel);
		try {
			System.setProperty("sun.awt.noerasebackground", "true");
		} catch (NoSuchMethodError error) {
		}   

		legendTitle = chart.getLegend();

	}

	private void selectColumn(){
		includeColumn.clear();
		for(String header : matrix.getHeaders() ){
			includeColumn.add(header);
		}
	}

	private void refresh(){
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setDataset(dataset);
		chartPanel.repaint();
	}

	private void setData(){
		dataset = new HistogramDataset();
		dataset.setType(HistogramType.FREQUENCY);
		for(int i=0;i<matrix.getNumberColumn();i++){
			if(includeColumn.contains(matrix.getHeader(i))){
				dataset.addSeries(matrix.getHeader(i),matrix.getColumn(i),BINS_NUMBER);
			}
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnSelectColumn){
			selectColumn();
			setData();
			refresh();
		}else if(e.getSource()==btnDisplayLegend){
			if(btnDisplayLegend.getSelection()){
				chart.addLegend(legendTitle);
				chartPanel.repaint();
			}else{
				chart.removeLegend();
				chartPanel.repaint();
			}
		}else if(e.getSource()==btnRefresh){
			try{
				int bins = Integer.parseInt(textBinNB.getText().trim());
				BINS_NUMBER = bins;
				setData();
				refresh();
			}catch(Exception e1){
				System.out.println("Not a number, bin number is not changed");
			}
			refresh();
		}else if(e.getSource()==btnSavePNG){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the image to: ");
			//fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
			fd.setFileName(Database.getInstance().getPATH()+".png");
			String[] filterExt = {"*.png","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				BufferedImage tamponSauvegarde = new BufferedImage(chartPanel.getWidth(),chartPanel.getHeight(), BufferedImage.TYPE_INT_RGB); 
				Graphics g = tamponSauvegarde.getGraphics();
				chartPanel.paint(g); 
				ImageIO.write(tamponSauvegarde, "PNG", new File(fileName));
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public static void displayMatrix(ExpressionMatrix matrix,String viewName,EPartService partService){
		ModelProviderRCP.INSTANCE.setMatrix(matrix);
		String id = HistogramView.ID+Math.random();
		ResourceManager.openView(partService, HistogramView.ID, id);
		MPart part = partService.findPart(id);
		part.setLabel(viewName);
	}
}
