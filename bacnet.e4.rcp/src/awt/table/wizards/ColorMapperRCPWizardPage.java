package awt.table.wizards;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JRootPane;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

import bacnet.Database;
import bacnet.swt.ResourceManager;
import bacnet.table.core.ColorMapperRCP;
import bacnet.table.core.ColorMapperRCP.Type;

public class ColorMapperRCPWizardPage extends WizardPage implements Listener{

	private Text textMin;
	private Text textMax;
	private ColorMapperRCP colorMapper;
	private ColorMapperRCP previousColorMapper;

	private ChartPanel chartPanel;


	public String text = "";
	private double sliderPrecision = 100;
	private Text lblsliderCenter;
	private Slider sliderLeft;
	private Slider sliderRight;
	private Canvas canvas;
	private Text lblsliderMidLeft;
	private Text lblsliderMidRight;
	private Button btnMinData;
	private Button btnMaxData;
	private Button btnSelectMinColor;
	private Button btnSelectMidleftColor;
	private Button btnSelectCenterColor;
	private Button btnSelectMidRightColor;
	private Button btnSelectMaxColor;
	private Label lblBoundleft;
	private Label lblBoundRight;
	private Button btnRestoreDefault;
	private Slider sliderCenter;
	private Button btnSelectHeaderColor;
	private Button btnTextColor;
	private Button btnTextHeaderColor;
	private Button btnPngExport;
	private Button btnSVGexport;
	private Button btnFont;
	private Button btnFontNumber;
	private Button btnLoadColorMapper;
	private Button btnSaveColorMapper;
	
	private Shell shell;
	
	public ColorMapperRCPWizardPage(Shell shell) {
		super("wizardPage");
		this.shell = shell;
		setTitle("Color mapper");
		setDescription("Select color to display");
	}

	public ColorMapperRCPWizardPage(ColorMapperRCP colorMapper,Shell shell) {
		super("wizardPage");
		this.shell = shell;
		String type = colorMapper.getType().toString();
		if(colorMapper.getType() == Type.OTHER) type = "other type of data";
		setTitle("Color mapper for "+type);
		setDescription("Select color to display");
		this.colorMapper = colorMapper;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);

		canvas = new Canvas(container, SWT.NONE);
		canvas.setBounds(42, 10, 468, 57);
		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = canvas.getClientArea(); 
				int height = clientArea.height;
				int width = clientArea.width;
				updateColorCanvas(e.gc, width, height,colorMapper);
			}
		});

		sliderCenter = new Slider(container, SWT.NONE);
		sliderCenter.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				colorMapper.setCenterPos(sliderToValuePos(sliderCenter.getSelection()));
				lblsliderCenter.setText(String.valueOf(colorMapper.getCenterPos()));
				canvas.redraw();
			}
		});
		sliderCenter.setBounds(24, 96, 501, 17);
		sliderCenter.setIncrement(1);

		sliderLeft = new Slider(container, SWT.NONE);
		sliderLeft.setBounds(24, 132, 501, 17);
		sliderLeft.setIncrement(1);
		sliderLeft.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				colorMapper.setMidLeftPos(sliderToValuePos(sliderLeft.getSelection()));
				lblsliderMidLeft.setText(String.valueOf(colorMapper.getMidLeftPos()));
				canvas.redraw();
			}
		});

		sliderRight = new Slider(container, SWT.NONE);
		sliderRight.setBounds(24, 172, 501, 17);
		sliderRight.setIncrement(1);
		sliderRight.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				colorMapper.setMidRightPos(sliderToValuePos(sliderRight.getSelection()));
				lblsliderMidRight.setText(String.valueOf(colorMapper.getMidRightPos()));
				canvas.redraw();
			}
		});

		textMin = new Text(container, SWT.BORDER);
		textMin.setBounds(120, 202, 76, 21);
		textMin.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try{
					double min = Double.valueOf(textMin.getText());
					setMin(min);
					canvas.redraw();
				}catch(Exception exe){	
				}
			}
		});
		textMax = new Text(container, SWT.BORDER);
		textMax.setBounds(420, 202, 76, 21);
		textMax.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try{
					double max = Double.valueOf(textMax.getText());
					setMax(max);
					canvas.redraw();
				}catch(Exception exe){	
				}
			}
		});

		lblBoundleft = new Label(container, SWT.NONE);
		lblBoundleft.setBounds(42, 73, 55, 15);

		lblBoundRight = new Label(container, SWT.NONE);
		lblBoundRight.setBounds(506, 73, 55, 15);

		lblsliderCenter = new Text(container, SWT.NONE);
		lblsliderCenter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try{
					colorMapper.setCenterPos(Double.valueOf(lblsliderCenter.getText()));
					setSelectionSlider();
					canvas.redraw();
				}catch(Exception exe){	
				}
			}
		});
		lblsliderCenter.setBounds(531, 98, 55, 15);

		lblsliderMidLeft = new Text(container, SWT.NONE);
		lblsliderMidLeft.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try{
					colorMapper.setMidLeftPos(Double.valueOf(lblsliderMidLeft.getText()));
					setSelectionSlider();
					canvas.redraw();
				}catch(Exception exe){	
				}
			}
		});
		lblsliderMidLeft.setBounds(531, 132, 55, 15);

		lblsliderMidRight = new Text(container, SWT.NONE);
		lblsliderMidRight.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try{
					colorMapper.setMidRightPos(Double.valueOf(lblsliderMidRight.getText()));
					setSelectionSlider();
					canvas.redraw();
				}catch(Exception exe){	
				}
			}
		});
		lblsliderMidRight.setBounds(531, 172, 55, 15);

		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setBounds(10, 205, 104, 15);
		lblNewLabel.setText("Choose Min value");

		Label lblChooseMaxValue = new Label(container, SWT.NONE);
		lblChooseMaxValue.setText("Choose Max value");
		lblChooseMaxValue.setBounds(310, 205, 104, 15);

		btnMinData = new Button(container, SWT.NONE);
		btnMinData.setBounds(202, 200, 75, 25);
		btnMinData.setText("Real min");
		btnMinData.addListener(SWT.Selection, this);

		btnMaxData = new Button(container, SWT.NONE);
		btnMaxData.setText("Real max");
		btnMaxData.setBounds(502, 200, 75, 25);
		btnMaxData.addListener(SWT.Selection, this);

		Label lblStatInfo = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblStatInfo.setText("Stat Info");
		lblStatInfo.setBounds(10, 303, 730, 2);

		Label lblMean = new Label(container, SWT.NONE);
		lblMean.setBounds(579, 325, 129, 15);
		lblMean.setText("Mean : "+colorMapper.getMean());

		Label lblMedian = new Label(container, SWT.NONE);
		lblMedian.setText("Median : "+colorMapper.getMedian());
		lblMedian.setBounds(579, 360, 129, 15);

		Label lblVariance = new Label(container, SWT.NONE);
		lblVariance.setText("Variance : "+colorMapper.getVar());
		lblVariance.setBounds(579, 425, 134, 15);

		Label lblStatDeviation = new Label(container, SWT.NONE);
		lblStatDeviation.setText("Stat deviation : "+colorMapper.getDeviation());
		lblStatDeviation.setBounds(579, 456, 134, 15);

		Label lblNbElement = new Label(container, SWT.NONE);
		lblNbElement.setText("Nb Element : "+colorMapper.getNbElements());
		lblNbElement.setBounds(579, 393, 148, 15);

		Label labelMin = new Label(container, SWT.NONE);
		labelMin.setText("Min : "+colorMapper.getMin());
		labelMin.setBounds(579, 488, 134, 15);

		Label labelMax = new Label(container, SWT.NONE);
		labelMax.setText("Max : "+colorMapper.getMax());
		labelMax.setBounds(579, 518, 134, 15);

		btnSelectMinColor = new Button(container, SWT.NONE);
		btnSelectMinColor.setBounds(643, 10, 97, 25);
		btnSelectMinColor.setText("Min color");
		btnSelectMinColor.addListener(SWT.Selection, this);

		btnSelectMidleftColor = new Button(container, SWT.NONE);
		btnSelectMidleftColor.setText("Mid-Left color");
		btnSelectMidleftColor.setBounds(620, 52, 120, 25);
		btnSelectMidleftColor.addListener(SWT.Selection, this);

		btnSelectCenterColor = new Button(container, SWT.NONE);
		btnSelectCenterColor.setText("Center color");
		btnSelectCenterColor.setBounds(629, 93, 111, 25);
		btnSelectCenterColor.addListener(SWT.Selection, this);

		btnSelectMidRightColor = new Button(container, SWT.NONE);
		btnSelectMidRightColor.setText("Mid-Right color");
		btnSelectMidRightColor.setBounds(615, 145, 125, 25);
		btnSelectMidRightColor.addListener(SWT.Selection, this);

		btnSelectMaxColor = new Button(container, SWT.NONE);
		btnSelectMaxColor.setText("Max color");
		btnSelectMaxColor.setBounds(645, 198, 97, 25);
		btnSelectMaxColor.addListener(SWT.Selection, this);

		Composite composite = new Composite(container, SWT.EMBEDDED);
		composite.setBounds(40, 325, 510, 220);

		Frame frame_1 = SWT_AWT.new_Frame(composite);

		Panel panel = new Panel();
		frame_1.add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JRootPane rootPane = new JRootPane();
		panel.add(rootPane);
		JFreeChart chart = createChart(createDataset());
		chartPanel = new ChartPanel(chart);
		JPanel jpanel = new JPanel();
		rootPane.getContentPane().add(chartPanel, BorderLayout.CENTER);

		rootPane.setBounds(62, 325, 459, 193);

		btnRestoreDefault = new Button(container, SWT.NONE);
		btnRestoreDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				colorMapper.setDefault();
				setMin(colorMapper.getMin());
				setMax(colorMapper.getMax());
				setSelectionSlider();
				textMin.setText(String.valueOf(colorMapper.getMin()));
				textMax.setText(String.valueOf(colorMapper.getMax()));
				lblsliderCenter.setText(String.valueOf(colorMapper.getCenterPos()));
				lblsliderMidLeft.setText(String.valueOf(colorMapper.getMidLeftPos()));
				lblsliderMidRight.setText(String.valueOf(colorMapper.getMidRightPos()));
				canvas.redraw();
			}
		});
		btnRestoreDefault.setBounds(10, 256, 60, 25);
		btnRestoreDefault.setText("Default");

		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(10, 238, 730, 2);

		btnSelectHeaderColor = new Button(container, SWT.NONE);
		btnSelectHeaderColor.setBounds(536, 256, 87, 25);
		btnSelectHeaderColor.setText("Header color");

		btnTextColor = new Button(container, SWT.NONE);
		btnTextColor.setBounds(443, 256, 87, 25);
		btnTextColor.setText("Text color");

		btnTextHeaderColor = new Button(container, SWT.NONE);
		btnTextHeaderColor.setBounds(629, 256, 111, 25);
		btnTextHeaderColor.setText("Text header color");

		btnPngExport = new Button(container, SWT.NONE);
		btnPngExport.setToolTipText("Save in PNG");
		btnPngExport.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/png.bmp"));
		btnPngExport.setBounds(182, 256, 46, 25);

		btnSVGexport = new Button(container, SWT.NONE);
		btnSVGexport.setToolTipText("Save in SVG format (Illustrator)");
		btnSVGexport.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/svg.bmp"));
		btnSVGexport.setBounds(234, 256, 40, 25);

		btnFont = new Button(container, SWT.NONE);
		btnFont.setBounds(278, 256, 55, 25);
		btnFont.setText("Font text");

		btnFontNumber = new Button(container, SWT.NONE);
		btnFontNumber.setBounds(339, 256, 75, 25);
		btnFontNumber.setText("Font number");

		btnLoadColorMapper = new Button(container, SWT.NONE);
		btnLoadColorMapper.setToolTipText("Load a ColorMapper");
		btnLoadColorMapper.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/colLoad.bmp"));
		btnLoadColorMapper.setBounds(81, 256, 46, 25);

		btnSaveColorMapper = new Button(container, SWT.NONE);
		btnSaveColorMapper.setToolTipText("Save the ColorMapper");
		btnSaveColorMapper.setImage(ResourceManager.getPluginImage("bacnet.rcp", "icons/fileIO/colSave.bmp"));
		btnSaveColorMapper.setBounds(130, 256, 46, 25);
		btnLoadColorMapper.addListener(SWT.Selection, this);
		btnSaveColorMapper.addListener(SWT.Selection, this);
		btnFont.addListener(SWT.Selection, this);
		btnFontNumber.addListener(SWT.Selection, this);
		btnPngExport.addListener(SWT.Selection, this);
		btnSVGexport.addListener(SWT.Selection, this);
		btnTextColor.addListener(SWT.Selection, this);
		btnTextHeaderColor.addListener(SWT.Selection, this);
		btnSelectHeaderColor.addListener(SWT.Selection, this);



		previousColorMapper = colorMapper.clone();

		setInfo();
	}



	public void setInfo(){
		textMin.setText(colorMapper.getMinPos()+"");
		textMax.setText(colorMapper.getMaxPos()+"");
		lblBoundleft.setText(String.valueOf(colorMapper.getMinPos()));
		lblBoundRight.setText(String.valueOf(colorMapper.getMaxPos()));
		lblsliderCenter.setText(String.valueOf(colorMapper.getCenterPos()));
		lblsliderMidLeft.setText(String.valueOf(colorMapper.getMidLeftPos()));
		lblsliderMidRight.setText(String.valueOf(colorMapper.getMidRightPos()));
		setMin(colorMapper.getMinPos());
		setMax(colorMapper.getMaxPos());
	}

	private IntervalXYDataset createDataset() {
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries("values", colorMapper.getValues(), 100, colorMapper.getMin(), colorMapper.getMax());
		return dataset;     
	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            dataset.
	 * 
	 * @return A chart.
	 */
	private JFreeChart createChart(IntervalXYDataset dataset) {

		JFreeChart chart = ChartFactory.createHistogram(
				"", 
				null, 
				null, 
				dataset, 
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false
				);
		chart.setBorderVisible(false);

		chart.getXYPlot().setForegroundAlpha(0.75f);
		return chart;

	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		if(event.widget == btnSelectMinColor){
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Min Color");
			colorDiag.setRGB(colorMapper.getMinColor().getRGB());
			colorMapper.setMinColor(new Color(colorMapper.display,colorDiag.open()));
			canvas.redraw();
		}else if(event.widget == btnSelectMidleftColor){
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set MidLeft Color");
			colorDiag.setRGB(colorMapper.getMidLeftColor().getRGB());
			colorMapper.setMidLeftColor(new Color(colorMapper.display,colorDiag.open()));
			canvas.redraw();
		}else if(event.widget == btnSelectCenterColor){
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Center Color");
			colorDiag.setRGB(colorMapper.getCenterColor().getRGB());
			colorMapper.setCenterColor(new Color(colorMapper.display,colorDiag.open()));
			canvas.redraw();
		}else if(event.widget == btnSelectMidRightColor){
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set MidRight Color");
			colorDiag.setRGB(colorMapper.getMidRightColor().getRGB());
			colorMapper.setMidRightColor(new Color(colorMapper.display,colorDiag.open()));
			canvas.redraw();
		}else if(event.widget == btnSelectMaxColor){
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Max Color");
			colorDiag.setRGB(colorMapper.getMaxColor().getRGB());
			colorMapper.setMaxColor(new Color(colorMapper.display,colorDiag.open()));
			canvas.redraw();
		}else if(event.widget == btnSelectHeaderColor){
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Header Color");
			colorDiag.setRGB(colorMapper.getHeaderColor().getRGB());
			colorMapper.setHeaderColor(new Color(colorMapper.display,colorDiag.open()));
		}else if(event.widget == btnTextHeaderColor){
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Color of Header's text");
			colorDiag.setRGB(colorMapper.getTextHeaderColor().getRGB());
			colorMapper.setTextHeaderColor(new Color(colorMapper.display,colorDiag.open()));
		}else if(event.widget == btnTextColor){
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Text Color");
			colorDiag.setRGB(colorMapper.getTextColor().getRGB());
			colorMapper.setTextColor(new Color(colorMapper.display,colorDiag.open()));
		}else if(event.widget == btnMinData){
			textMin.setText(String.valueOf(colorMapper.getMin()));
			canvas.redraw();
		}else if(event.widget == btnMaxData){
			textMax.setText(String.valueOf(colorMapper.getMax()));
			canvas.redraw();
		}else if(event.widget == btnFont){
			FontDialog dialog = new FontDialog(shell);
			FontData font = new FontData(colorMapper.getFontString().getFontName(), colorMapper.getFontString().getSize(), colorMapper.getFontString().getStyle());
			FontData[] fonts = {font};
			dialog.setFontList(fonts);
			FontData fontData = dialog.open();
			java.awt.Font awtFont = new java.awt.Font(fontData.getName(), fontData.getStyle(), fontData.getHeight());
			colorMapper.setFontString(awtFont);
		}else if(event.widget == btnFontNumber){
			FontDialog dialog = new FontDialog(shell);
			FontData font = new FontData(colorMapper.getFontDouble().getFontName(), colorMapper.getFontDouble().getSize(), colorMapper.getFontDouble().getStyle());
			FontData[] fonts = {font};
			dialog.setFontList(fonts);
			FontData fontData = dialog.open();
			java.awt.Font awtFont = new java.awt.Font(fontData.getName(), fontData.getStyle(), fontData.getHeight());
			colorMapper.setFontDouble(awtFont);
		}else if(event.widget == btnLoadColorMapper){
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setText("Load the colormapper: ");
			fd.setFilterPath(Database.getInstance().getPATH());
			String[] filterExt = {"*.col","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				ColorMapperRCP colorMapp = ColorMapperRCP.load(fileName,shell);
				colorMapp.setValues(this.colorMapper.getValues());
				colorMapp.setType(this.colorMapper.getType());
				colorMapp.setStat();
				this.colorMapper = colorMapp;
				setInfo();
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}else if(event.widget == btnSaveColorMapper){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the image to: ");
			fd.setFilterPath(Database.getInstance().getPATH());
			String[] filterExt = {"*.col","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				colorMapper.save(fileName);
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}else if(event.widget == btnPngExport){
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Save the image to: ");
			fd.setFilterPath(Database.getInstance().getPATH());
			String[] filterExt = {"*.png","*.*" };
			fd.setFilterExtensions(filterExt);
			String fileName = fd.open();
			try {
				Image drawable = new Image(canvas.getDisplay(), canvas.getBounds());
				GC gc = new GC(drawable);
				canvas.print(gc);
				ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] {drawable.getImageData()};
				loader.save(fileName, SWT.IMAGE_PNG);
				drawable.dispose();
				gc.dispose();
			} catch (Exception ex) {
				System.out.println("Cannot save the image");
			}
		}else if(event.widget == btnSVGexport){
			//			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			//		     fd.setText("Save the image to: ");
			//		     fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.PROJECT_DIR));
			//		     String[] filterExt = {"*.svg","*.*" };
			//		     fd.setFilterExtensions(filterExt);
			//		     String fileName = fd.open();
			//			try {
			//				ImageExportUtils.exportAWTasSVG(chartPanel, fileName);
			//			} catch (Exception ex) {
			//				System.out.println("Cannot save the image");
			//			}
		}
	}



	public static void updateColorCanvas(GC gc,int width, int height,ColorMapperRCP colorMapper){
		// first we set all the values
		TreeMap<Double,Color> valuesMap = new TreeMap<Double,Color>();
		valuesMap.put(colorMapper.getMinPos(), colorMapper.getMinColor());
		valuesMap.put(colorMapper.getMidLeftPos(), colorMapper.getMidLeftColor());
		valuesMap.put(colorMapper.getCenterPos(), colorMapper.getCenterColor());
		valuesMap.put(colorMapper.getMidRightPos(), colorMapper.getMidRightColor());
		valuesMap.put(colorMapper.getMaxPos(), colorMapper.getMaxColor());
		Iterator<Double> itr = valuesMap.keySet().iterator();
		double previousValue = itr.next();
		while(itr.hasNext()){
			double nextValue = itr.next();
			gc.setForeground(valuesMap.get(previousValue));
			gc.setBackground(valuesMap.get(nextValue));
			gc.fillGradientRectangle(valueToCanvasPos(previousValue, width,colorMapper), 0, valueToCanvasSize(previousValue, nextValue, width,colorMapper),height,false);
			previousValue = nextValue;			
		}
	}

	private void setMin(double min){
		colorMapper.setMinPos(min);
		lblBoundleft.setText(String.valueOf(min));
		sliderCenter.setMinimum(valueToSliderPos(colorMapper.getMinPos()));
		sliderLeft.setMinimum(valueToSliderPos(colorMapper.getMinPos()));
		sliderRight.setMinimum(valueToSliderPos(colorMapper.getMinPos()));
		setSelectionSlider();
	}

	private void setMax(double max){
		colorMapper.setMaxPos(max);
		lblBoundRight.setText(String.valueOf(max));
		sliderCenter.setMaximum(valueToSliderPos(colorMapper.getMaxPos()));
		sliderLeft.setMaximum(valueToSliderPos(colorMapper.getMaxPos()));
		sliderRight.setMaximum(valueToSliderPos(colorMapper.getMaxPos()));
		setSelectionSlider();
	}

	private static int valueToCanvasPos(double value,int width,ColorMapperRCP colorMapper){
		return (int)((width/(colorMapper.getMaxPos()-colorMapper.getMinPos()))*(value-colorMapper.getMin()));
	}
	private static int valueToCanvasSize(double value1,double value2, int width,ColorMapperRCP colorMapper){
		int size = valueToCanvasPos(value2, width,colorMapper)-valueToCanvasPos(value1, width,colorMapper);
		return size;
	}
	private int valueToSliderPos(double value){
		return (int)((value-colorMapper.getMinPos())*sliderPrecision);
	}
	private double sliderToValuePos(int pos){
		double value = pos/sliderPrecision + colorMapper.getMinPos();	
		return value;
	}
	private void setSelectionSlider(){
		sliderCenter.setSelection(valueToSliderPos(colorMapper.getCenterPos()));
		sliderLeft.setSelection(valueToSliderPos(colorMapper.getMidLeftPos()));
		sliderRight.setSelection(valueToSliderPos(colorMapper.getMidRightPos()));
	}

	public void performCancel(){
		colorMapper = previousColorMapper;
	}
	public ColorMapperRCP getColorMapper(){
		return colorMapper;
	}
}
