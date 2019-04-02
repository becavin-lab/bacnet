package awt.graphics;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JPanel;
import javax.swing.JRootPane;

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
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import bacnet.Database;
import bacnet.datamodel.annotation.COGannotation;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.ResourceManager;

public class PieChartView implements SelectionListener{
	public static final String ID = "bacnet.rcp.PieChartView";
	private Button btnSavePNG;
	private JRootPane rootPane;
	private Button btnSaveSVG;
	private JPanel panelPie;
	private ArrayList<JFreeChart> charts;

	@Inject
	EPartService partService;
	
	@ Inject
	@ Named (IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	
	@Inject
	public PieChartView() {
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		
		btnSavePNG = new Button(composite_1, SWT.NONE);
		btnSavePNG.setImage(ResourceManager.getPluginImage("ListTranscript", "icons/fileIO/png.bmp"));
		btnSavePNG.addSelectionListener(this);
		btnSaveSVG = new Button(composite_1, SWT.NONE);
		btnSaveSVG.setToolTipText("Save in SVG file");
		btnSaveSVG.setImage(ResourceManager.getPluginImage("ListTranscript", "icons/fileIO/svg.bmp"));
		btnSaveSVG.addSelectionListener(this);
		
		Composite composite = new Composite(parent, SWT.EMBEDDED);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_composite.heightHint = 800;
		gd_composite.widthHint = 700;
		composite.setLayoutData(gd_composite);
		
		Frame frame = SWT_AWT.new_Frame(composite);
		
		Panel panel2 = new Panel();
		frame.add(panel2);
		panel2.setLayout(new BorderLayout(0, 0));
		
		rootPane = new JRootPane();
		panel2.add(rootPane);
		try {
			getData();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void getData() throws IOException{
//		ArrayList<ExpressionMatrix> matrices = ModelProvider.INSTANCE.getMatrixListToDisplay();
//		charts = new ArrayList<JFreeChart>();
//		panelPie = new JPanel(new java.awt.GridLayout(3, 2));
//		JScrollPane jScrollPane = new JScrollPane(panelPie);
//		rootPane.getContentPane().add(jScrollPane, BorderLayout.CENTER);
//		for(ExpressionMatrix matrix: matrices){
//			JFreeChart chart = ChartFactory.createPieChart(matrix.getInfo(), createDataset(matrix), false, false, false);
//			
//			PiePlot plot = (PiePlot) chart.getPlot();
//			plot.setCircular(true);
//			int i=0;
//			for(String cogID : COGannotation.getCOGMap().keySet()){
//				plot.setSectionPaint(COGannotation.getCOGDescription(cogID), ColorMapper.swtColorToAwt(BasicColor.getColors(i)));
//				//plot.setSectionPaint(cogID, ColorMapper.swtColorToAwt(BasicColor.getColors(i)));
//				i++;
//			}
//			charts.add(chart);
//			ChartPanel chartPanel = new ChartPanel(chart);
//			//chartPanel.setPreferredSize(new Dimension(300, 300));
//			panelPie.add(chartPanel);
//		}
        //JFreeChart chart3 = ChartFactory.createPieChart3D("Chart 3", dataset, false, false, false);
        //PiePlot3D plot3 = (PiePlot3D) chart3.getPlot();
        
        
	}
	
	private static PieDataset createDataset(ExpressionMatrix matrix) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for(String rowName : matrix.getRowNames().keySet()){
        	//dataset.setValue(rowName, matrix.getValue(rowName, matrix.getHeader(0)));
        	dataset.setValue(COGannotation.getCOGDescription(rowName), matrix.getValue(rowName, matrix.getHeader(0)));
        }
        return dataset;        
    }

	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnSavePNG) {
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
		     fd.setText("Save the image to: ");
		     //fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
		     fd.setFileName(Database.getInstance().getPATH()+".png");
		     String[] filterExt = {"*.png","*.*" };
		     fd.setFilterExtensions(filterExt);
		     String fileName = fd.open();
			try {
				BufferedImage tamponSauvegarde = new BufferedImage(panelPie.getWidth(),panelPie.getHeight(), BufferedImage.TYPE_INT_RGB); 
				Graphics g = tamponSauvegarde.getGraphics();
				panelPie.paint(g); 
				ImageIO.write(tamponSauvegarde, "PNG", new File(fileName));
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}else if(e.getSource()==btnSaveSVG) {
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
		     fd.setText("Save the SVG image to: ");
		     //fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
		     fd.setFileName(Database.getInstance().getPATH()+".svg");
		     String[] filterExt = {"*.svg","*.*" };
		     fd.setFilterExtensions(filterExt);
		     String fileName = fd.open();
			try {
				for(JFreeChart chart : charts){
					//ImageExportUtils.exportJFreeChartAsSVG(chart, panelPie.getBounds(), fileName);
				}
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
