package awt.graphics;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import awt.table.ImageExportUtilsAWT;
import awt.table.ModelProviderRCP;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.ResourceManager;

public class SVDMDSView implements SelectionListener{

	public static final String ID = "bacnet.rcp.SVDMDSView";
	
	private static int MISSING_VALUE = -15;
	
	private JRootPane rootPane;
	
	private ScatterPlot2DJPanel scatterplotPanel;
	private ExpressionMatrix matrixFC;
	private ExpressionMatrix matrixExpr;
	
	private String annotation = "";
	
	private Button btnDisplayMutants;
	private Button btnDisplayGenomeElements;
	private Button btnPNGExport;
	private Label lblInfo;
	private Button btnSVGexport;
	
	@Inject
	EPartService partService;
	
	@ Inject
	@ Named (IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	
	@Inject
	public SVDMDSView() {
		// mutant matrix
		matrixExpr = ModelProviderRCP.INSTANCE.getMatrix();
	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		Composite cmpBtn = new Composite(composite, SWT.NONE);
		cmpBtn.setLayout(new GridLayout(4, false));
		
		btnDisplayMutants = new Button(cmpBtn, SWT.NONE);
		btnDisplayMutants.setText("Display mutants");
		btnDisplayMutants.addSelectionListener(this);
		btnDisplayGenomeElements = new Button(cmpBtn, SWT.NONE);
		btnDisplayGenomeElements.setText("Display genome elements");
		
		btnPNGExport = new Button(cmpBtn, SWT.NONE);
		btnPNGExport.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/png.bmp"));
		btnPNGExport.addSelectionListener(this);
		
		btnSVGexport = new Button(cmpBtn, SWT.NONE);
		btnSVGexport.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/svg.bmp"));
		btnSVGexport.addSelectionListener(this);
		btnDisplayGenomeElements.addSelectionListener(this);
		
		lblInfo = new Label(composite, SWT.NONE);
		lblInfo.setText("zqdzdzqdqdz");
		
		Composite composite_1 = new Composite(composite, SWT.EMBEDDED);
		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_composite_1.heightHint = 1000;
		gd_composite_1.widthHint = 1000;
		composite_1.setLayoutData(gd_composite_1);
		
		Frame frame_1 = SWT_AWT.new_Frame(composite_1);
		
		Panel panel = new Panel();
		frame_1.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		rootPane = new JRootPane();
		panel.add(rootPane);
		
		
		// set panel
		scatterplotPanel = new ScatterPlot2DJPanel(matrixFC,false);
		rootPane.getContentPane().add(scatterplotPanel, BorderLayout.CENTER);
		
		setInfo(false);
	
		
	     try {
	          System.setProperty("sun.awt.noerasebackground", "true");
	     } catch (NoSuchMethodError error) {
	     }
	}
	

	private void setInfo(boolean bcRepresentation){
		if(bcRepresentation){
			String text=" Representation of mutants in 2D (using SVD-MDS algorithm in correspondence space)\n";
			text+=" Stress: "+matrixExpr.getNote().split(";")[0]+" (begin "+matrixExpr.getNote().split(";")[1]+")";
			lblInfo.setText(text);
		}else{
			String text=" Representation of genome elements in 2D (using SVD-MDS algorithm in correspondence space)\n";
			text+=" Stress: "+matrixFC.getNote().split(";")[0]+" (begin "+matrixFC.getNote().split(";")[1]+")";
			lblInfo.setText(text);
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==btnDisplayMutants){
			scatterplotPanel.setData(matrixExpr,true);
			setInfo(true);
		}else if(e.getSource()==btnDisplayGenomeElements){
			scatterplotPanel.setData(matrixFC,false);
			setInfo(true);
		}else if(e.getSource()==btnPNGExport){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
		     fd.setText("Save the image to: ");
		     fd.setFilterPath(Database.getInstance().getPATH());
		     String[] filterExt = {"*.png","*.*" };
		     fd.setFilterExtensions(filterExt);
		     String fileName = fd.open();
			try {
				BufferedImage tamponSauvegarde = new BufferedImage(scatterplotPanel.getWidth(),scatterplotPanel.getHeight(), BufferedImage.TYPE_INT_RGB); 
				Graphics g = tamponSauvegarde.getGraphics();
				scatterplotPanel.paint(g); 
				ImageIO.write(tamponSauvegarde, "PNG", new File(fileName));
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}else if(e.getSource()==btnSVGexport){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
		     fd.setText("Save the image to: ");
		     fd.setFilterPath(Database.getInstance().getPATH());
		     String[] filterExt = {"*.svg","*.*" };
		     fd.setFilterExtensions(filterExt);
		     String fileName = fd.open();
			try {
				ImageExportUtilsAWT.exportAWTasSVG(scatterplotPanel, fileName);
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
