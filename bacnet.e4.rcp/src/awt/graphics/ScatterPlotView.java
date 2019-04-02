package awt.graphics;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import awt.table.ModelProviderRCP;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.ResourceManager;

public class ScatterPlotView implements SelectionListener{

	public static final String ID = "ListTranscript.ScatterPlotView";

	private DefaultXYDataset dataset = new DefaultXYDataset();
	private Combo cmbXaxis;
	private Combo cmbYaxis;
	private ChartPanel chartPanel;
	private JFreeChart chart;
	private Panel panel;
	private Button btnSavePNG;
	private Button btnXaxisLog;
	private Button btnYaxisLog;

	@Inject
	EPartService partService;
	
	@ Inject
	@ Named (IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	
	@Inject
	public ScatterPlotView() {
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void createPartControl(Composite parent) {

		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayout(new GridLayout(3, false));

		Label lblChoosDataFor = new Label(composite_1, SWT.NONE);
		GridData gd_lblChoosDataFor = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblChoosDataFor.widthHint = 182;
		lblChoosDataFor.setLayoutData(gd_lblChoosDataFor);
		lblChoosDataFor.setText("Choos data for xAxis");

		Label lblChooseDataFor = new Label(composite_1, SWT.NONE);
		GridData gd_lblChooseDataFor = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblChooseDataFor.widthHint = 186;
		lblChooseDataFor.setLayoutData(gd_lblChooseDataFor);
		lblChooseDataFor.setText("Choose data for Yaxis");
		new Label(composite_1, SWT.NONE);

		cmbXaxis = new Combo(composite_1, SWT.NONE);
		cmbXaxis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbYaxis = new Combo(composite_1, SWT.NONE);
		cmbYaxis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));



		Button btnRefresh = new Button(composite_1, SWT.NONE);
		btnRefresh.setImage(ResourceManager.getPluginImage("bacnet", "icons/genome/update.bmp"));
		btnRefresh.setToolTipText("Refresh volcano plot");

		btnRefresh.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("refresh");
				setData();
				XYPlot plot = (XYPlot)chart.getPlot();
				plot.setDataset(dataset);
				NumberAxis xAxis = new NumberAxis("Position");
				NumberAxis yAxis = new NumberAxis("Median");
				ExpressionMatrix matrix = ModelProviderRCP.INSTANCE.getMatrix();
				xAxis = new NumberAxis(matrix.getHeader(cmbXaxis.getSelectionIndex()));
				yAxis = new NumberAxis(matrix.getHeader(cmbYaxis.getSelectionIndex()));

				plot.setDomainAxis(xAxis);
				plot.setRangeAxis(yAxis);
				chartPanel.repaint();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		btnXaxisLog = new Button(composite_1, SWT.CHECK);
		btnXaxisLog.setText("X-axis log10");

		btnYaxisLog = new Button(composite_1, SWT.CHECK);
		btnYaxisLog.setText("Y-axis log10");

		btnSavePNG = new Button(composite_1, SWT.NONE);
		btnSavePNG.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/png.bmp"));
		btnSavePNG.addSelectionListener(this);

		NumberAxis xAxis = new NumberAxis("Position");
		NumberAxis yAxis = new NumberAxis("Median");

		ExpressionMatrix matrix = ModelProviderRCP.INSTANCE.getMatrix();
		for(String colName : matrix.getHeaders()){
			cmbXaxis.add(colName);
			cmbYaxis.add(colName);
		}
		xAxis = new NumberAxis(matrix.getHeader(0));
		yAxis = new NumberAxis(matrix.getHeader(1));
		cmbXaxis.select(0);
		if(matrix.getNumberColumn()>1) cmbYaxis.select(1);
		else cmbYaxis.select(0);

		setData();
		XYDotRenderer renderer = new XYDotRenderer();
		renderer.setDotWidth(3);
		renderer.setDotHeight(3);
		renderer.setSeriesItemLabelsVisible(0, true);
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

		chart = new JFreeChart("XY scatter plot", new Font("SansSerif", Font.BOLD, 14),plot,false);
		parent.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(parent, SWT.EMBEDDED);
		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_composite_1.heightHint = 700;
		gd_composite_1.widthHint = 1000;
		composite.setLayoutData(gd_composite_1);

		chartPanel = new ChartPanel(chart);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setZoomAroundAnchor(true);


		Frame frame = SWT_AWT.new_Frame(composite);
		panel = new Panel(new BorderLayout()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6619407857641544326L;

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
	}

	private void setData(){
		dataset = new DefaultXYDataset();
		ExpressionMatrix matrix = ModelProviderRCP.INSTANCE.getMatrix();

		double[][] data = new double[2][matrix.getNumberRow()];
		for(String rowName : matrix.getRowNames().keySet()){
			data[0][matrix.getRowNames().get(rowName)] = matrix.getValue(rowName, matrix.getHeader(0));
			//if(btnXaxisLog.getSelection()) data[0][matrix.getRowNames().get(rowName)] = Math.log10(1 - matrix.getValue(rowName, xAxisCol));
			data[1][matrix.getRowNames().get(rowName)] = matrix.getValue(rowName, matrix.getHeader(1));
			//if(btnXaxisLog.getSelection()) data[1][matrix.getRowNames().get(rowName)] = Math.log10(1 - matrix.getValue(rowName, yAxisCol));
		}
		dataset.addSeries(matrix.getNote(),data);


	}

	public void savePNG(String fileName){
		try {
			BufferedImage tamponSauvegarde = new BufferedImage(panel.getWidth(),panel.getHeight(), BufferedImage.TYPE_INT_RGB); 
			Graphics g = tamponSauvegarde.getGraphics();
			panel.paint(g); 
			ImageIO.write(tamponSauvegarde, "PNG", new File(fileName));
		} catch (Exception ex) {
			System.out.println("Cannot save the image");
		}
	}

	/**
	 * Static function which open an HeatMapView and display corresponding ArrayList<ExpressionMatrix>
	 * @param matrices
	 * @param viewName
	 * @throws PartInitException 
	 */
	public static void displayMatrix(ExpressionMatrix matrix, String viewName,EPartService partService){
		ModelProviderRCP.INSTANCE.setMatrix(matrix);
		String id = ScatterPlotView.ID+Math.random();
		ResourceManager.openView(partService, ScatterPlotView.ID, id);
		MPart part = partService.findPart(id);
		part.setLabel(viewName);
	}

	public static void displayMatrix(ExpressionMatrix matrix, String viewName,String fileName,EPartService partService){
		ModelProviderRCP.INSTANCE.setMatrix(matrix);
		String id = ScatterPlotView.ID+Math.random();
		ResourceManager.openView(partService, ScatterPlotView.ID, id);
		MPart part = partService.findPart(id);
		part.setLabel(viewName);
		ScatterPlotView view = (ScatterPlotView) part.getObject();
		view.savePNG(fileName);
	}



	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnSavePNG){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the image to: ");
			fd.setFileName(Database.getInstance().getPATH()+".png");
			String[] filterExt = {"*.png","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			savePNG(fileName);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}
}
