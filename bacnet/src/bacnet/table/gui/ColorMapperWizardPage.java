package bacnet.table.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.ColorMapper;
import bacnet.table.core.ColorMapper.TypeMapper;
import bacnet.utils.BasicColor;

public class ColorMapperWizardPage extends WizardPage implements Listener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3829149015717789437L;
	private Text textMin;
	private Text textMax;
	private ColorMapper colorMapper;
	private ColorMapper previousColorMapper;

	private Shell shell;

	public String text = "";
	private Text lblsliderCenter;
	private Canvas canvas;
	private Text lblsliderMidLeft;
	private Text lblsliderMidRight;
	private Button btnSelectMinColor;
	private Button btnSelectMidleftColor;
	private Button btnSelectCenterColor;
	private Button btnSelectMidRightColor;
	private Button btnSelectMaxColor;
	private Label lblBoundleft;
	private Label lblBoundRight;
	private Button btnSelectHeaderColor;
	private Button btnNumberTextColor;
	private Button btnTextHeaderColor;
	private Button btnFont;
	private Button btnFontNumber;
	private Button btnLoadColorMapper;
	private Button btnSaveColorMapper;
	private Composite composite_8;
	private Composite composite_9;
	private Composite composite_10;
	private Composite composite_11;
	private Composite composite_12;
	private Composite composite;
	private Label lblSetColor;
	private Label label_4;
	private Label label_9;
	private Label label_10;
	private Label label_11;
	private Composite composite_13;
	private Label lblMin_1;
	private Label lblMidleft;
	private Label lblCenter;
	private Label lblMidright;
	private Label lblMax;

	/**
	 * Create the wizard.
	 * 
	 * @wbp.parser.constructor
	 */
	public ColorMapperWizardPage(Shell shell) {
		super("wizardPage");
		this.shell = shell;
		setTitle("Color mapper");
		setDescription("Select color to display");
	}

	public ColorMapperWizardPage(ColorMapper colorMapper, Shell shell) {
		super("wizardPage");
		this.shell = shell;
		String type = colorMapper.getType().toString();
		if (colorMapper.getType() == TypeMapper.OTHER)
			type = "";
		setTitle("Color mapper for " + type);
		setDescription("Select color to display");
		this.colorMapper = colorMapper;
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));

		composite_11 = new Composite(container, SWT.BORDER);
		composite_11.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_11.setLayout(new GridLayout(2, false));

		composite_13 = new Composite(composite_11, SWT.NONE);
		composite_13.setLayout(new GridLayout(23, false));
		GridData gd_composite_13 = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_composite_13.widthHint = 600;
		composite_13.setLayoutData(gd_composite_13);

		lblMin_1 = new Label(composite_13, SWT.NONE);
		lblMin_1.setText("Min");
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);

		lblMidleft = new Label(composite_13, SWT.NONE);
		lblMidleft.setText("Mid-left");
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);

		lblCenter = new Label(composite_13, SWT.NONE);
		lblCenter.setText("Center");
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);

		lblMidright = new Label(composite_13, SWT.NONE);
		lblMidright.setText("Mid-right");
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);
		new Label(composite_13, SWT.NONE);

		lblMax = new Label(composite_13, SWT.NONE);
		lblMax.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblMax.setText("Max");

		canvas = new Canvas(composite_11, SWT.BORDER);
		GridData gd_canvas = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
		gd_canvas.widthHint = 610;
		canvas.setLayoutData(gd_canvas);

		lblBoundleft = new Label(composite_11, SWT.NONE);

		lblBoundRight = new Label(composite_11, SWT.RIGHT);
		lblBoundRight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		canvas.addPaintListener(new PaintListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 527459191862753134L;

			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = canvas.getClientArea();
				int height = clientArea.height;
				int width = clientArea.width;
				updateColorCanvas(e.gc, width, height, colorMapper);
			}
		});

		Composite composite_7 = new Composite(container, SWT.NONE);
		composite_7.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_7.setLayout(new GridLayout(3, false));
		DecimalFormat twoDForm = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
		Label lblMean = new Label(composite_7, SWT.NONE);
		lblMean.setText("Mean : " + twoDForm.format(colorMapper.getMean()));

		Label lblNbElement = new Label(composite_7, SWT.NONE);
		lblNbElement.setText("Nb Element : " + colorMapper.getNbElements());

		Label labelMin = new Label(composite_7, SWT.NONE);
		labelMin.setText("Min : " + twoDForm.format(colorMapper.getMin()));

		Label lblMedian = new Label(composite_7, SWT.NONE);
		lblMedian.setText("Median : " + twoDForm.format(colorMapper.getMedian()));

		Label lblStatDeviation = new Label(composite_7, SWT.NONE);
		lblStatDeviation.setText("Stat deviation : " + twoDForm.format(colorMapper.getDeviation()));

		Label labelMax = new Label(composite_7, SWT.NONE);
		labelMax.setText("Max : " + twoDForm.format(colorMapper.getMax()));

		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_label.widthHint = 378;
		label.setLayoutData(gd_label);

		Label label_1 = new Label(container, SWT.NONE);

		composite_8 = new Composite(container, SWT.NONE);
		composite_8.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
		composite_8.setLayout(new GridLayout(2, false));

		composite_9 = new Composite(composite_8, SWT.BORDER);
		composite_9.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		composite_9.setLayout(new GridLayout(1, false));

		Label lblSetColorsProperties = new Label(composite_9, SWT.NONE);
		lblSetColorsProperties.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblSetColorsProperties.setText("Set colors properties here");

		Composite composite_1 = new Composite(composite_9, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_1.setLayout(new GridLayout(4, false));

		Label lblMin = new Label(composite_1, SWT.NONE);
		lblMin.setText("Set min value ");
		lblMin.setForeground(BasicColor.GREY);

		textMin = new Text(composite_1, SWT.BORDER);

		label_4 = new Label(composite_1, SWT.NONE);
		label_4.setText("and select ");
		label_4.setForeground(SWTResourceManager.getColor(127, 127, 127));

		btnSelectMinColor = new Button(composite_1, SWT.NONE);
		btnSelectMinColor.setText("Min color");
		btnSelectMinColor.addListener(SWT.Selection, this);

		Composite composite_2 = new Composite(composite_9, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_2.setLayout(new GridLayout(4, false));

		Label lblSetMidleftValue = new Label(composite_2, SWT.NONE);
		lblSetMidleftValue.setText("Set mid-left value ");
		lblSetMidleftValue.setForeground(SWTResourceManager.getColor(127, 127, 127));

		lblsliderMidLeft = new Text(composite_2, SWT.BORDER);
		lblsliderMidLeft.addModifyListener(new ModifyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 7366589214793857868L;

			public void modifyText(ModifyEvent e) {
				try {
					colorMapper.setMidLeftPos(Double.valueOf(lblsliderMidLeft.getText()));
					setSelectionSlider();
					canvas.redraw();
				} catch (Exception exe) {
				}
			}
		});

		Label label_3 = new Label(composite_2, SWT.NONE);
		label_3.setText("and select ");
		label_3.setForeground(SWTResourceManager.getColor(127, 127, 127));

		btnSelectMidleftColor = new Button(composite_2, SWT.NONE);
		btnSelectMidleftColor.setText("Mid-Left color");
		btnSelectMidleftColor.addListener(SWT.Selection, this);

		Composite composite_3 = new Composite(composite_9, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_3.setLayout(new GridLayout(4, false));

		Label lblSetCenterValue = new Label(composite_3, SWT.NONE);
		lblSetCenterValue.setText("Set center value ");
		lblSetCenterValue.setForeground(SWTResourceManager.getColor(127, 127, 127));
		lblSetCenterValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		lblsliderCenter = new Text(composite_3, SWT.BORDER);
		lblsliderCenter.addModifyListener(new ModifyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8605749110386390250L;

			public void modifyText(ModifyEvent e) {
				try {
					colorMapper.setCenterPos(Double.valueOf(lblsliderCenter.getText()));
					setSelectionSlider();
					canvas.redraw();
				} catch (Exception exe) {
				}
			}
		});

		label_9 = new Label(composite_3, SWT.NONE);
		label_9.setText("and select ");
		label_9.setForeground(SWTResourceManager.getColor(127, 127, 127));

		btnSelectCenterColor = new Button(composite_3, SWT.NONE);
		btnSelectCenterColor.setText("Center color");
		btnSelectCenterColor.addListener(SWT.Selection, this);

		Composite composite_4 = new Composite(composite_9, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_4.setLayout(new GridLayout(4, false));

		Label lblSetMidrightValue = new Label(composite_4, SWT.NONE);
		lblSetMidrightValue.setText("Set mid-right value ");
		lblSetMidrightValue.setForeground(SWTResourceManager.getColor(127, 127, 127));
		lblSetMidrightValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		lblsliderMidRight = new Text(composite_4, SWT.BORDER);
		lblsliderMidRight.addModifyListener(new ModifyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1503951672152947834L;

			public void modifyText(ModifyEvent e) {
				try {
					colorMapper.setMidRightPos(Double.valueOf(lblsliderMidRight.getText()));
					setSelectionSlider();
					canvas.redraw();
				} catch (Exception exe) {
				}
			}
		});

		label_10 = new Label(composite_4, SWT.NONE);
		label_10.setText("and select ");
		label_10.setForeground(SWTResourceManager.getColor(127, 127, 127));

		btnSelectMidRightColor = new Button(composite_4, SWT.NONE);
		btnSelectMidRightColor.setText("Mid-Right color");
		btnSelectMidRightColor.addListener(SWT.Selection, this);

		Composite composite_5 = new Composite(composite_9, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_5.setLayout(new GridLayout(4, false));

		Label lblSetMaxValue = new Label(composite_5, SWT.NONE);
		lblSetMaxValue.setText("Set max value ");
		lblSetMaxValue.setForeground(SWTResourceManager.getColor(127, 127, 127));
		lblSetMaxValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		textMax = new Text(composite_5, SWT.BORDER);
		textMax.addModifyListener(new ModifyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3491949949142450877L;

			public void modifyText(ModifyEvent e) {
				try {
					double max = Double.valueOf(textMax.getText());
					setMax(max);
					canvas.redraw();
				} catch (Exception exe) {
				}
			}
		});

		label_11 = new Label(composite_5, SWT.NONE);
		label_11.setText("and select ");
		label_11.setForeground(SWTResourceManager.getColor(127, 127, 127));

		btnSelectMaxColor = new Button(composite_5, SWT.NONE);
		btnSelectMaxColor.setText("Max color");
		btnSelectMaxColor.addListener(SWT.Selection, this);

		composite = new Composite(composite_9, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		lblSetColor = new Label(composite, SWT.NONE);
		lblSetColor.setText("Set color of the first column");
		lblSetColor.setForeground(SWTResourceManager.getColor(127, 127, 127));

		btnSelectHeaderColor = new Button(composite, SWT.NONE);
		btnSelectHeaderColor.setText("Text cell color");
		btnSelectHeaderColor.addListener(SWT.Selection, this);
		textMin.addModifyListener(new ModifyListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6304499516543871347L;

			public void modifyText(ModifyEvent e) {
				try {
					double min = Double.valueOf(textMin.getText());
					setMin(min);
					canvas.redraw();
				} catch (Exception exe) {
				}
			}
		});

		composite_10 = new Composite(composite_8, SWT.BORDER);
		composite_10.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1));
		composite_10.setLayout(new GridLayout(1, false));

		Label lblSetTextProperties = new Label(composite_10, SWT.NONE);
		lblSetTextProperties.setText("Set text properties (color and font)");

		Composite composite_6 = new Composite(composite_10, SWT.NONE);
		composite_6.setLayout(new GridLayout(2, false));

		btnTextHeaderColor = new Button(composite_6, SWT.NONE);
		btnTextHeaderColor.setText("Set text color");

		Label lblNewLabel = new Label(composite_6, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
		lblNewLabel.setText("of column containing text");
		lblNewLabel.setForeground(BasicColor.GREY);

		btnFont = new Button(composite_6, SWT.NONE);
		btnFont.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnFont.setText("Set Font");
		btnFont.addListener(SWT.Selection, this);
		btnTextHeaderColor.addListener(SWT.Selection, this);

		Composite composite_14 = new Composite(composite_10, SWT.NONE);
		composite_14.setLayout(new GridLayout(2, false));

		btnNumberTextColor = new Button(composite_14, SWT.NONE);
		btnNumberTextColor.setText("Set text color");

		Label lblOfColumnContaining = new Label(composite_14, SWT.NONE);
		lblOfColumnContaining.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
		lblOfColumnContaining.setText("of column containing values");
		lblOfColumnContaining.setForeground(BasicColor.GREY);

		btnFontNumber = new Button(composite_14, SWT.NONE);
		btnFontNumber.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnFontNumber.setText("Set Font");
		btnFontNumber.addListener(SWT.Selection, this);
		btnNumberTextColor.addListener(SWT.Selection, this);

		composite_12 = new Composite(composite_8, SWT.NONE);
		composite_12.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		composite_12.setLayout(new GridLayout(2, false));

		btnLoadColorMapper = new Button(composite_12, SWT.NONE);
		btnLoadColorMapper.setText("Load");
		btnLoadColorMapper.setToolTipText("Load a ColorMapper");
		btnLoadColorMapper.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/colLoad.bmp"));

		btnSaveColorMapper = new Button(composite_12, SWT.NONE);
		btnSaveColorMapper.setText("Save");
		btnSaveColorMapper.setToolTipText("Save the ColorMapper");
		btnSaveColorMapper.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/colSave.bmp"));
		btnSaveColorMapper.addListener(SWT.Selection, this);
		btnLoadColorMapper.addListener(SWT.Selection, this);

		previousColorMapper = colorMapper.clone();

		setInfo();
	}

	public void setInfo() {
		// display only two digits
		DecimalFormat twoDForm = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
		textMin.setText(twoDForm.format(colorMapper.getMinPos()));
		textMax.setText(twoDForm.format(colorMapper.getMaxPos()));
		lblBoundleft.setText(twoDForm.format(colorMapper.getMinPos()));
		lblBoundRight.setText(twoDForm.format(colorMapper.getMaxPos()));
		lblsliderCenter.setText(twoDForm.format(colorMapper.getCenterPos()));
		lblsliderMidLeft.setText(twoDForm.format(colorMapper.getMidLeftPos()));
		lblsliderMidRight.setText(twoDForm.format(colorMapper.getMidRightPos()));
		setMin(colorMapper.getMinPos());
		setMax(colorMapper.getMaxPos());
	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		if (event.widget == btnSelectMinColor) {
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Min Color");
			colorDiag.setRGB(colorMapper.getMinColor().getRGB());
			colorMapper.setMinColor(new Color(colorMapper.display, colorDiag.open()));
			canvas.redraw();
		} else if (event.widget == btnSelectMidleftColor) {
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set MidLeft Color");
			colorDiag.setRGB(colorMapper.getMidLeftColor().getRGB());
			colorMapper.setMidLeftColor(new Color(colorMapper.display, colorDiag.open()));
			canvas.redraw();
		} else if (event.widget == btnSelectCenterColor) {
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Center Color");
			colorDiag.setRGB(colorMapper.getCenterColor().getRGB());
			colorMapper.setCenterColor(new Color(colorMapper.display, colorDiag.open()));
			canvas.redraw();
		} else if (event.widget == btnSelectMidRightColor) {
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set MidRight Color");
			colorDiag.setRGB(colorMapper.getMidRightColor().getRGB());
			colorMapper.setMidRightColor(new Color(colorMapper.display, colorDiag.open()));
			canvas.redraw();
		} else if (event.widget == btnSelectMaxColor) {
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Max Color");
			colorDiag.setRGB(colorMapper.getMaxColor().getRGB());
			colorMapper.setMaxColor(new Color(colorMapper.display, colorDiag.open()));
			canvas.redraw();
		} else if (event.widget == btnSelectHeaderColor) {
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Header Color");
			colorDiag.setRGB(colorMapper.getRowNameCellColor().getRGB());
			colorMapper.setRowNameCellColor(new Color(colorMapper.display, colorDiag.open()));
		} else if (event.widget == btnTextHeaderColor) {
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Color of Header's text");
			colorDiag.setRGB(colorMapper.getRowNameTextColor().getRGB());
			colorMapper.setRowNameTextColor(new Color(colorMapper.display, colorDiag.open()));
		} else if (event.widget == btnNumberTextColor) {
			ColorDialog colorDiag = new ColorDialog(shell);
			colorDiag.setText("Set Text Color");
			colorDiag.setRGB(colorMapper.getTextColor().getRGB());
			colorMapper.setTextColor(new Color(colorMapper.display, colorDiag.open()));
		} else if (event.widget == btnFont) {
			FontDialog dialog = new FontDialog(shell);
			FontData[] fonts = colorMapper.getFontText().getFontData();
			dialog.setFontList(fonts);
			FontData fontData = dialog.open();
			colorMapper.setFontText(new Font(shell.getDisplay(), fontData));
		} else if (event.widget == btnFontNumber) {
			FontDialog dialog = new FontDialog(shell);
			FontData[] fonts = colorMapper.getFontDouble().getFontData();
			dialog.setFontList(fonts);
			FontData fontData = dialog.open();
			colorMapper.setFontDouble(new Font(shell.getDisplay(), fontData));
		} else if (event.widget == btnLoadColorMapper) {
			// FileDialog fd = new FileDialog(shell, SWT.OPEN);
			// fd.setText("Load the colormapper: ");
			// fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
			// String[] filterExt = {"*.col","*.*" };
			// fd.setFilterExtensions(filterExt);
			// String fileName = fd.open();
			// try {
			// ColorMapper colorMapp = ColorMapper.load(fileName);
			// colorMapp.setValues(this.colorMapper.getValues());
			// colorMapp.setType(this.colorMapper.getType());
			// colorMapp.setStat();
			// this.colorMapper = colorMapp;
			// setInfo();
			// } catch (Exception ex) {
			// System.out.println("Cannot save the image");
			// }
		} else if (event.widget == btnSaveColorMapper) {
			// FileDialog fd = new FileDialog(shell, SWT.SAVE);
			// fd.setText("Save the image to: ");
			// fd.setFilterPath(Activator.getDefault().getPreferenceStore().getString(Project.getANALYSIS_PATH()));
			// String[] filterExt = {"*.col","*.*" };
			// fd.setFilterExtensions(filterExt);
			// String fileName = fd.open();
			// try {
			// colorMapper.save(fileName);
			// } catch (Exception ex) {
			// System.out.println("Cannot save the image");
			// }
		}
	}

	public static void updateColorCanvas(GC gc, int width, int height, ColorMapper colorMapper) {
		// first we set all the values
		TreeMap<Double, Color> valuesMap = new TreeMap<Double, Color>();
		valuesMap.put(colorMapper.getMinPos(), colorMapper.getMinColor());
		gc.drawString("Min", valueToCanvasPos(colorMapper.getMinPos(), width, colorMapper), 0);
		valuesMap.put(colorMapper.getMidLeftPos(), colorMapper.getMidLeftColor());
		gc.drawString("Mid-Left", valueToCanvasPos(colorMapper.getMidLeftPos(), width, colorMapper), 0);
		valuesMap.put(colorMapper.getCenterPos(), colorMapper.getCenterColor());
		gc.drawString("Center", valueToCanvasPos(colorMapper.getCenterPos(), width, colorMapper), 0);
		valuesMap.put(colorMapper.getMidRightPos(), colorMapper.getMidRightColor());
		gc.drawString("Mid-Right", valueToCanvasPos(colorMapper.getMidRightPos(), width, colorMapper), 0);
		valuesMap.put(colorMapper.getMaxPos(), colorMapper.getMaxColor());
		gc.drawString("Max", valueToCanvasPos(colorMapper.getMaxPos(), width, colorMapper), 0);
		Iterator<Double> itr = valuesMap.keySet().iterator();
		double previousValue = itr.next();
		while (itr.hasNext()) {
			double nextValue = itr.next();
			gc.setForeground(valuesMap.get(previousValue));
			gc.setBackground(valuesMap.get(nextValue));
			gc.fillGradientRectangle(valueToCanvasPos(previousValue, width, colorMapper), 0,
					valueToCanvasSize(previousValue, nextValue, width, colorMapper), height, false);
			previousValue = nextValue;
		}
		gc.setForeground(BasicColor.BLACK);
		gc.drawLine(valueToCanvasPos(colorMapper.getMinPos(), width, colorMapper), 0,
				valueToCanvasPos(colorMapper.getMinPos(), width, colorMapper), height);
		gc.setLineWidth(3);
		gc.drawLine(valueToCanvasPos(colorMapper.getMidLeftPos(), width, colorMapper), 0,
				valueToCanvasPos(colorMapper.getMidLeftPos(), width, colorMapper), height);
		gc.drawLine(valueToCanvasPos(colorMapper.getCenterPos(), width, colorMapper), 0,
				valueToCanvasPos(colorMapper.getCenterPos(), width, colorMapper), height);
		gc.drawLine(valueToCanvasPos(colorMapper.getMidRightPos(), width, colorMapper), 0,
				valueToCanvasPos(colorMapper.getMidRightPos(), width, colorMapper), height);
		gc.drawLine(valueToCanvasPos(colorMapper.getMaxPos(), width, colorMapper), 0,
				valueToCanvasPos(colorMapper.getMaxPos(), width, colorMapper), height);

	}

	private void setMin(double min) {
		colorMapper.setMinPos(min);
		DecimalFormat twoDForm = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
		lblBoundleft.setText(twoDForm.format(min));
		setSelectionSlider();
	}

	private void setMax(double max) {
		colorMapper.setMaxPos(max);
		DecimalFormat twoDForm = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
		lblBoundRight.setText(twoDForm.format(max));
		setSelectionSlider();
	}

	/**
	 * Give the position in the canvas of a specific value, depending of ColorMapper
	 * parameters
	 * 
	 * @param value
	 * @param width
	 * @param colorMapper
	 * @return
	 */
	private static int valueToCanvasPos(double value, int width, ColorMapper colorMapper) {
		return (int) ((width / (colorMapper.getMaxPos() - colorMapper.getMinPos())) * (value - colorMapper.getMin()));
	}

	private static int valueToCanvasSize(double value1, double value2, int width, ColorMapper colorMapper) {
		int size = valueToCanvasPos(value2, width, colorMapper) - valueToCanvasPos(value1, width, colorMapper);
		return size;
	}

	private void setSelectionSlider() {
	}

	public void performCancel() {
		colorMapper = previousColorMapper;
	}

	public ColorMapper getColorMapper() {
		return colorMapper;
	}
}
