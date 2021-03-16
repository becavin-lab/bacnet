package awt.graphics;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
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
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import awt.table.ModelProviderRCP;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.ResourceManager;

public class BoxPlotView implements SelectionListener{

	public static String ID = "bacnet.rcp.BoxPlotView";
	
	private ExpressionMatrix matrix;
	private ArrayList<String> includeColumn = new ArrayList<String>();
	private DefaultBoxAndWhiskerCategoryDataset dataset;
	private Button btnSavePNG;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	private LegendTitle legendTitle;
	private Button btnSelectData;
	private Button btnDisplayLegend;
	
	@Inject
	EPartService partService;
	
	@ Inject
	@ Named (IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	
	@Inject
	public BoxPlotView() {
		// TODO Auto-generated constructor stub
	}
	

	@PostConstruct
	public void createPartControl(Composite parent) {
		
		matrix = ModelProviderRCP.INSTANCE.getMatrix();
		selectColumn();
		setData();
		
		CategoryAxis xAxis = new CategoryAxis("Expression");
	     NumberAxis yAxis = new NumberAxis("Quantity");
	     yAxis.setAutoRangeIncludesZero(false);
	     BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
	     renderer.setSeriesVisible(0, true);
	     //renderer.setFillBox(false);
	     CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
	     
	     chart = new JFreeChart("Box Plot view", new Font("SansSerif", Font.BOLD, 14),plot,true);
	     parent.setLayout(new GridLayout(1, false));
		
		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayout(new GridLayout(3, false));
		
		btnSelectData = new Button(composite_1, SWT.NONE);
		btnSelectData.setText("Select data");
		btnSelectData.addSelectionListener(this);
		
		btnDisplayLegend = new Button(composite_1, SWT.CHECK);
		btnDisplayLegend.setSelection(true);
		btnDisplayLegend.setText("Display legend");
		btnDisplayLegend.addSelectionListener(this);
		btnSavePNG = new Button(composite_1, SWT.NONE);
		btnSavePNG.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/png.bmp"));
		btnSavePNG.addSelectionListener(this);
			
		Composite composite = new Composite(parent, SWT.EMBEDDED);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_1.heightHint = 700;
		gd_composite_1.widthHint = 1000;
		composite.setLayoutData(gd_composite_1);
			
		chartPanel = new ChartPanel(chart);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setZoomAroundAnchor(true);
		
		Frame frame = SWT_AWT.new_Frame(composite);
		Panel panel = new Panel(new BorderLayout()) {
		    	/**
			 * 
			 */
			private static final long serialVersionUID = 7396617097080952057L;

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
		CategoryPlot plot = (CategoryPlot)chart.getPlot();
		plot.setDataset(dataset);
		chartPanel.repaint();
	}
	
	private void setData(){
		dataset = new DefaultBoxAndWhiskerCategoryDataset();
	     for(int i=0;i<matrix.getNumberColumn();i++){
	     	if(includeColumn.contains(matrix.getHeader(i))){
	     		ArrayList<Double> list = new ArrayList<Double>();
		     	double[] values = matrix.getColumn(i);
		     	for(Double value:values) list.add(value);
		     	dataset.add(list, matrix.getHeader(i), "Expression");
	     	}
	     }   
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnSelectData){
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
		}else if(e.getSource()==btnSavePNG){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
		     fd.setText("Save the image to: ");
		     //fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
		     fd.setFileName(Database.getInstance().getPATH());
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
		String id = BoxPlotView.ID+Math.random();
		ResourceManager.openView(partService, BoxPlotView.ID, id);
		MPart part = partService.findPart(id);
		part.setLabel(viewName);
	}
}
