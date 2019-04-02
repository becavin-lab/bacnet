package awt.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import awt.table.ImageExportUtilsAWT;
import awt.table.ModelProviderRCP;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.ResourceManager;
import bacnet.utils.VectorUtils;

public class StatScatterView implements SelectionListener{

	public static final String ID = "bacnet.rcp.StatScatterView"; //$NON-NLS-1$

	private DefaultXYDataset dataset = new DefaultXYDataset();
	private ChartPanel chartPanel;
	private JFreeChart chart;
	private Panel panel;
	private Button btnSaveSVG;
	private Button btnSavePNG;

	@Inject
	EPartService partService;
	
	@ Inject
	@ Named (IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	
	@Inject
	public StatScatterView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		try {
			setData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NumberAxis xAxis = new NumberAxis("COGs");
		NumberAxis yAxis = new NumberAxis("LogFC absolute values (each dot is a gene in one bio condition)");
		XYDotRenderer renderer = new XYDotRenderer();
		renderer.setDotWidth(3);
		renderer.setDotHeight(3);
		renderer.setSeriesItemLabelsVisible(0, true);
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		// set last series = mean serie has black
		plot.getRenderer().setSeriesPaint(dataset.getSeriesCount()-1, Color.BLACK);

		chart = new JFreeChart("XY scatter plot", new Font("SansSerif", Font.BOLD, 14),plot,true);
		parent.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(parent, SWT.EMBEDDED);
		composite.setLayout(new GridLayout(1, false));
		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_composite_1.heightHint = 1000;
		gd_composite_1.widthHint = 1500;
		composite.setLayoutData(gd_composite_1);

		chartPanel = new ChartPanel(chart);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setZoomAroundAnchor(true);

		{	
			Composite composite_1 = new Composite(composite, SWT.NONE);
			composite_1.setLayout(new GridLayout(2, false));
			composite_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			
			btnSavePNG = new Button(composite_1, SWT.NONE);
			btnSavePNG.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/png.bmp"));
			btnSavePNG.addSelectionListener(this);
			btnSaveSVG = new Button(composite_1, SWT.NONE);
			btnSaveSVG.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/svg.bmp"));
			btnSaveSVG.addSelectionListener(this);
		}
		
		{
			Composite composite_1 = new Composite(composite,  SWT.EMBEDDED | SWT.FILL);
			GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
			gd_composite_2.heightHint = 900;
			gd_composite_1.widthHint = 1500;
			composite_1.setLayoutData(gd_composite_1);
			Frame frame = SWT_AWT.new_Frame(composite_1);
			panel = new Panel(new BorderLayout()) {
			/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

			public void update(java.awt.Graphics g) {
				/* Do not erase the background */
				paint(g);
			}
			};
			JScrollPane jScrollPane = new JScrollPane(chartPanel);
			panel.add(jScrollPane, BorderLayout.CENTER);
			frame.add(panel);
		}
		
		

		try {
			System.setProperty("sun.awt.noerasebackground", "true");
		} catch (NoSuchMethodError error) {
		}  
	}

	private void setData() throws IOException{
		dataset = new DefaultXYDataset();
		ExpressionMatrix matrix = ModelProviderRCP.INSTANCE.getMatrix();
		// one series for each column, and one for all the median
		double[][] dataMean = new double[2][matrix.getNumberColumn()*11];
		int m=0;
		for(int j=0;j<matrix.getNumberColumn();j++){
			String header = matrix.getHeader(j);
			double[][] data = new double[2][VectorUtils.deleteMissingValue(matrix.getColumn(header)).length];
			int i=0;
			for(String rowName : matrix.getRowNames().keySet()){
				double value = matrix.getValue(rowName, header);
				if(value != ExpressionMatrix.MISSING_VALUE){
					data[0][i] = j*0.50;
					data[1][i] = matrix.getValue(rowName,header);
					i++;
				}
			}
			double[] cleanRow = VectorUtils.deleteMissingValue(matrix.getColumn(j));
			dataset.addSeries(header,data);
			//dataset.addSeries(header+" ("+cleanRow.length+")",data);

			double median = 0;
			double firstQuantile = 0;
			double lastQuantile = 0;
			if(cleanRow.length!=0){
				median = VectorUtils.median(cleanRow);
//				double[] tenquantile = VectorUtils.quantiles(cleanRow, 10);
//
//				firstQuantile = tenquantile[1];
//				lastQuantile = tenquantile[tenquantile.length-2];
//				System.out.println(header+"\t"+ VectorUtils.min(cleanRow)+"\t"+firstQuantile+"\t"+median+"\t"+lastQuantile+"\t"+VectorUtils.max(cleanRow));
			}
			// plot 33=3*11 points to represent the mean,firstquantile, lastquantile as a line
			for(int k=0;k<11;k++){
				dataMean[0][m] = j*0.50+(k-5)*0.01;
				dataMean[1][m] = median;
				m++;
			}
			//	    	for(int k=0;k<11;k++){
			//	    		dataMean[0][m] = j*0.50+(k-5)*0.01;
			//	    		dataMean[1][m] = firstQuantile;
			//	    		m++;
			//	    	}
			//	    	for(int k=0;k<11;k++){
			//	    		dataMean[0][m] = j*0.50+(k-5)*0.01;
			//	    		dataMean[1][m] = lastQuantile;
			//	    		m++;
			//	    	}
		}
		dataset.addSeries("median",dataMean);



	}

	public static void displayMatrix(ExpressionMatrix matrix, String viewName,EPartService partService){
		ModelProviderRCP.INSTANCE.setMatrix(matrix);
		String id = StatScatterView.ID+Math.random();
		ResourceManager.openView(partService, StatScatterView.ID, id);
		MPart part = partService.findPart(id);
		part.setLabel(viewName);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnSavePNG){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the image to: ");
			//fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
			//fd.setFileName(Project.getANALYSIS_PATH()+FileUtils.removeExtension(getPartName())+".png");
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
		}else if(e.getSource()==btnSaveSVG){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the image to: ");
			//fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
			//fd.setFileName(Project.getANALYSIS_PATH()+FileUtils.removeExtension(getPartName())+".svg");
			String[] filterExt = {"*.svg","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				ImageExportUtilsAWT.exportAWTasSVG(chartPanel, fileName);
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
