package bacnet.expressionAtlas;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.expressionAtlas.core.SelectGenomeElementDialog;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.ColorMapper;
import bacnet.table.core.ColorMapperList;
import bacnet.table.core.MatrixSize;
import bacnet.table.core.TableViewerComparator;
import bacnet.table.gui.ColorMapperWizard;
import bacnet.table.gui.TableFilter;
import bacnet.utils.ArrayUtils;
import bacnet.utils.BasicColor;
import bacnet.utils.RWTUtils;
import bacnet.views.HelpPage;

/**
 * The Composite in which is displayed the heatmap
 * 
 * @author christophebecavin
 *
 */
public class TableCompositeHeatMap extends Composite implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -5898609236718602892L;

    private final Table table;
    private final Label labelRow;
    private final Button btnSaveText;
    private String genomeName;

    /*
     * data
     */
    private ExpressionMatrix matrix;
    private ExpressionMatrix matrixDisplayed;

    /**
     * Boolean describing if it is a transcriptomics or proteomic heatmap viewer
     */
    private boolean transcriptomics = true;

    private ArrayList<String> columnNames = new ArrayList<>();
    private ColorMapperList colorMapperList = new ColorMapperList();
    private final MatrixSize matrixSize = new MatrixSize();
    private TableFilter searchFilter;
    private boolean packColumns = false;
    private boolean displayValues = true;
    private ArrayList<String> excludeRow = new ArrayList<String>();
    private ArrayList<String> excludeColumn = new ArrayList<String>();

    private final TableViewer tableViewer;
    private TableViewerComparator comparator;
    private final Label labelColumn;
    private final Button btnHide;
    private final Button btnColormapper;
    private final Button btnDisplayValues;
    private final Composite composite;
    private final Button btnZoomIn;
    private final Button btnZoomOut;
    private final Composite composite_4;
    private final Label lblX;
    private Button btnHelp;
    private Shell shell;
    private EPartService partService;


    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     * @param genomeName name of the genome used here
     * @param packColumns true if columns of the table need to be packed
     */
    public TableCompositeHeatMap(Composite parent, int style, String genomeName, boolean packColumns,
            EPartService partService, Shell shell) {
        super(parent, SWT.BORDER);
        this.partService = partService;
        this.shell = shell;
        this.genomeName = genomeName;
        this.packColumns = packColumns;
        setLayout(new GridLayout(1, false));

        Composite compositeTable = new Composite(this, SWT.NONE);
        compositeTable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        compositeTable.setLayout(new GridLayout(2, false));
        compositeTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        composite_4 = new Composite(compositeTable, SWT.BORDER);
        composite_4.setLayout(new GridLayout(3, false));
        composite_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        labelRow = new Label(composite_4, SWT.NONE);
        labelRow.setAlignment(SWT.CENTER);
        GridData gd_labelRow = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_labelRow.widthHint = 67;
        labelRow.setLayoutData(gd_labelRow);
        labelRow.setText("00000");
        labelRow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        lblX = new Label(composite_4, SWT.NONE);
        lblX.setText("  x  ");
        lblX.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        labelColumn = new Label(composite_4, SWT.NONE);
        labelColumn.setAlignment(SWT.CENTER);
        GridData gd_labelColumn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_labelColumn.widthHint = 63;
        labelColumn.setLayoutData(gd_labelColumn);
        labelColumn.setText("000");
        labelColumn.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        composite = new Composite(compositeTable, SWT.BORDER);
        composite.setLayout(new GridLayout(7, false));
        composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        btnHide = new Button(composite, SWT.NONE);
        btnHide.setText("Select genome features");
        btnHide.setToolTipText("Select specific genome elements");
        btnHide.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/hideData.bmp"));
        btnHide.addSelectionListener(this);

        btnZoomIn = new Button(composite, SWT.NONE);
        btnZoomIn.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomIN.bmp"));
        btnZoomIn.addSelectionListener(this);
        btnZoomOut = new Button(composite, SWT.NONE);
        btnZoomOut.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomOUT.bmp"));
        btnZoomOut.addSelectionListener(this);
        btnColormapper = new Button(composite, SWT.NONE);
        btnColormapper.setToolTipText("Change colors");
        btnColormapper.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/colorMapper.bmp"));
        btnColormapper.addSelectionListener(this);

        btnDisplayValues = new Button(composite, SWT.CHECK);
        btnDisplayValues.setSelection(true);
        btnDisplayValues.setText("Display values");
        btnDisplayValues.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        btnDisplayValues.addSelectionListener(this);

        btnSaveText = new Button(composite, SWT.NONE);
        btnSaveText.setToolTipText("Save to Tab separated text format");
        btnSaveText.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/txt.bmp"));

        btnHelp = new Button(composite, SWT.NONE);
        btnHelp.setToolTipText("How to use HeatMap viewer ?");
        btnHelp.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/help.png"));
        btnHelp.addSelectionListener(this);
        btnSaveText.addSelectionListener(this);

        tableViewer = new TableViewer(this, SWT.BORDER | SWT.HIDE_SELECTION | SWT.VIRTUAL);
        tableViewer.setUseHashlookup(true);
        table = tableViewer.getTable();
        RWTUtils.setPreloadedItems(table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    }

    /**
     * Init data and filter lists
     * 
     * @param matrix
     */
    public void initData(ExpressionMatrix matrix) {
        this.matrix = matrix;
        excludeRow = new ArrayList<String>();
        excludeColumn = new ArrayList<String>();
        searchFilter = new TableFilter();
        tableViewer.addFilter(searchFilter);
        colorMapperList = new ColorMapperList(this.matrix, shell);
        if (matrix.getNumberRow() != 0) {
            table.setVisible(true);
        } else {
            labelRow.setText("No Element");
            labelColumn.setText("Display");
            table.setVisible(false);
        }
    }

    /**
     * Update all viewers and labels
     */
    public void updateInfo() {
        tableViewer.addFilter(searchFilter);
        colorMapperList = new ColorMapperList(this.matrix, shell);
        matrixDisplayed = getSubMatrix();
        int nbRow = matrixDisplayed.getNumberRow();
        if (nbRow == 0) {
            table.setVisible(false);
            labelRow.setText("Nothing");
            lblX.setText(" to ");
            labelColumn.setText("display");
        } else {
            table.setVisible(true);
            setColumnNames();
            int nbColumn = matrixDisplayed.getNumberColumnWithAnnotation();
            labelRow.setText("" + nbRow + " rows");
            lblX.setText(" X ");
            labelColumn.setText("" + nbColumn + " columns");

            for (TableColumn col : table.getColumns()) {
                col.dispose();
            }
            tableViewer.setContentProvider(new ArrayContentProvider());

            createColumns();

            tableViewer.setInput(matrixDisplayed.toArray("", false, true));
            tableViewer.setData("matrix", matrixDisplayed);
            comparator = new TableViewerComparator();
            tableViewer.setComparator(comparator);
            table.setData("org.eclipse.rap.rwt.customItemHeight", matrixSize.getHeightDefault());
        }

    }

    /**
     * For comparisons delete the second part of the header and keep only the BioCondition name
     */
    private void setColumnNames() {
        columnNames = new ArrayList<>();
        for (String header : matrixDisplayed.getHeaders()) {
            columnNames.add(header);
        }
    }

    private void createColumns() {

        // TableColumn[] columns = ;
        // ArrayList<String> colName = new ArrayList<String>();
        // for(String coll:matrix.getHeaders()) System.out.println(coll);
        //

        // First column is for the rownames
        TableViewerColumn col = createTableViewerColumn("Genome   locus", matrixSize.getWidthRowNameDefault(), 0);
        col.setLabelProvider(new ColumnLabelProvider() {
            /**
             * 
             */
            private static final long serialVersionUID = 3787720984608282634L;

            @Override
            public String getText(Object element) {
                String[] row = (String[]) element;
                return row[0];
            }

            @Override
            public String getToolTipText(Object element) {
                String[] row = (String[]) element;
                Genome genome = Genome.loadEgdeGenome();
                Sequence seq = genome.getElement(row[0]);
                if (seq != null) {
                    String ret = seq.getBegin() + "--" + seq.getEnd() + " (" + seq.getStrand() + ")";
                    return ret;
                }
                return "";

            }

            @Override
            public Point getToolTipShift(Object object) {
                return new Point(5, 5);
            }

            @Override
            public Color getBackground(Object element) {
                ColorMapper colorMapper = colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(0));
                return colorMapper.getRowNameCellColor();
            }

            @Override
            public Color getForeground(Object element) {
                ColorMapper colorMapper = colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(0));
                return colorMapper.getRowNameTextColor();
            }

            @Override
            public int getToolTipDisplayDelayTime(Object object) {
                return 100; // msec
            }

            @Override
            public int getToolTipTimeDisplayed(Object object) {
                return 5000; // msec
            }
        });
        col.getColumn().pack();

        // create data columns
        for (int i = 0; i < matrixDisplayed.getHeaders().size(); i++) {
            // if(!colName.contains(matrix.getHeader(i))){
            final int k = i + 1;
            TableViewerColumn col2 = createTableViewerColumn(columnNames.get(i), matrixSize.getWidthDefault(), k);
            col2.getColumn().setToolTipText(matrixDisplayed.getHeader(k - 1));
            col2.setLabelProvider(new ColumnLabelProvider() {
                /**
                 * 
                 */
                private static final long serialVersionUID = -55186278705378176L;

                @Override
                public String getText(Object element) {
                    if (displayValues) {
                        String[] row = (String[]) element;
                        if (!row[k].equals("")) {
                            double value = Double.parseDouble(row[k]);
                            // display only two digits
                            DecimalFormat twoDForm = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
                            value = Double.valueOf(twoDForm.format(value));
                            return value + "";
                        } else
                            return "";
                    }
                    return "";

                }

                @Override
                public Font getFont(Object element) {
                    ColorMapper colorMapper = colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(k - 1));
                    return colorMapper.getFontDouble();
                }

                @Override
                public Color getBackground(Object element) {
                    String[] row = (String[]) element;
                    if (!row[k].equals("")) {
                        double value = Double.parseDouble(row[k]);
                        ColorMapper colorMapper =
                                colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(k - 1));
                        return colorMapper.parseColor(value);
                    } else {
                        return BasicColor.WHITE;
                    }

                }

                @Override
                public Color getForeground(Object element) {
                    ColorMapper colorMapper = colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(k - 1));
                    return colorMapper.getTextColor();
                }

                @Override
                public String getToolTipText(Object element) {
                    String[] row = (String[]) element;
                    String ret = "row: " + row[0] + "\n column: " + matrixDisplayed.getHeader(k - 1) + "\n";
                    System.out.println("ret:" + ret);
                    if (!row[k].equals("")) {
                        double value = Double.parseDouble(row[k]);
                        ret += "value: " + value;
                    }

                    return ret;
                }

                @Override
                public Point getToolTipShift(Object object) {
                    return new Point(5, 5);
                }

                @Override
                public int getToolTipDisplayDelayTime(Object object) {
                    return 100; // msec
                }

                @Override
                public int getToolTipTimeDisplayed(Object object) {
                    return 5000; // msec
                }
            });
            if (packColumns) {
                col2.getColumn().pack();
            }
        }

        // create annotation columns
        for (int i = 0; i < matrixDisplayed.getHeaderAnnotation().size(); i++) {
            // if(!colName.contains(matrix.getHeader(i))){
            final int k = i + matrixDisplayed.getHeaders().size() + 1;
            TableViewerColumn col3 = createTableViewerColumn(matrix.getHeaderAnnotation().get(i),
                    matrixSize.getWidthRowNameDefault(), k);
            col3.getColumn().setToolTipText(
                    matrixDisplayed.getHeaderAnnotation().get(k - 1 - matrixDisplayed.getHeaders().size()));
            col3.setLabelProvider(new ColumnLabelProvider() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 2281817255142354364L;

                @Override
                public String getText(Object element) {
                    String[] row = (String[]) element;
                    return row[k];
                }

                @Override
                public org.eclipse.swt.graphics.Font getFont(Object element) {
                    ColorMapper colorMapper = colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(0));
                    return colorMapper.getFontText();
                }

                @Override
                public String getToolTipText(Object element) {
                    String[] row = (String[]) element;
                    String ret = "row: " + row[0] + "\n column: "
                            + matrixDisplayed.getHeaderAnnotation().get(k - 1 - matrixDisplayed.getHeaders().size())
                            + "\n" + row[k];
                    return ret;
                }

                @Override
                public Point getToolTipShift(Object object) {
                    return new Point(5, 5);
                }

                @Override
                public Color getBackground(Object element) {
                    ColorMapper colorMapper = colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(0));
                    return colorMapper.getRowNameCellColor();
                }

                @Override
                public Color getForeground(Object element) {
                    ColorMapper colorMapper = colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(0));
                    return colorMapper.getRowNameTextColor();
                }

                @Override
                public int getToolTipDisplayDelayTime(Object object) {
                    return 100; // msec
                }

                @Override
                public int getToolTipTimeDisplayed(Object object) {
                    return 5000; // msec
                }
            });
            if (packColumns) {
                col3.getColumn().pack();
            }
        }

    }

    private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.FILL);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        column.addSelectionListener(getSelectionAdapter(column, colNumber));
        return viewerColumn;
    }

    private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            /**
             * 
             */
            private static final long serialVersionUID = -6997325110066606691L;

            @Override
            public void widgetSelected(SelectionEvent e) {
                comparator.setColumn(index);
                int dir = comparator.getDirection();
                tableViewer.getTable().setSortDirection(dir);
                tableViewer.getTable().setSortColumn(column);
                tableViewer.refresh();
            }
        };
        return selectionAdapter;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    /**
     * Use excludeRow and excludColumn lists to create the new matrix to display
     * 
     * @return
     */
    private ExpressionMatrix getSubMatrix() {
        ArrayList<String> includeRow = new ArrayList<String>();
        ArrayList<String> includeColumn = new ArrayList<String>();

        for (String rowName : matrix.getRowNames().keySet()) {
            if (!excludeRow.contains(rowName))
                includeRow.add(rowName);
        }
        for (String col : matrix.getHeaders()) {
            if (!excludeColumn.contains(col))
                includeColumn.add(col);
        }
        for (String col : matrix.getHeaderAnnotation()) {
            if (!excludeColumn.contains(col))
                includeColumn.add(col);
        }

        return matrix.getSubMatrix(includeRow, includeColumn);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        try {
            if (e.getSource() == btnSaveText) {
                matrixDisplayed = getSubMatrix();
                String[][] array = matrixDisplayed.toArray(matrixDisplayed.getFirstRowName());
                String arrayRep = ArrayUtils.toString(array);
                String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(array);
                SaveFileUtils.saveTextFile("LogFc-Table.txt", arrayRep, true, "", arrayRepHTML, partService, shell);
            } else if (e.getSource() == btnColormapper) {
                ColorMapperWizard wizard = new ColorMapperWizard(colorMapperList, shell);
                WizardDialog dialog = new WizardDialog(shell, wizard);
                int dia = dialog.open();
                if (dia == 0) {
                    colorMapperList = wizard.getColorMapper();
                    tableViewer.refresh();
                }
            } else if (e.getSource() == btnZoomIn) {
                int height = (int) table.getData("org.eclipse.rap.rwt.customItemHeight");
                float factor = 1.25f;
                if (height < 3)
                    factor = 2;
                table.setData("org.eclipse.rap.rwt.customItemHeight", (int) (height * factor));
                for (int i = 1; i < table.getColumnCount(); i++) {
                    TableColumn column = table.getColumn(i);
                    column.setWidth((int) (column.getWidth() * factor));
                }
                matrixSize.setWidthDefault((int) (matrixSize.getWidthDefault() * factor));
                tableViewer.refresh();
            } else if (e.getSource() == btnZoomOut) {
                int height = (int) table.getData("org.eclipse.rap.rwt.customItemHeight");
                float factor = 0.75f;
                if (height == 1)
                    factor = 1;
                table.setData("org.eclipse.rap.rwt.customItemHeight", (int) (height * factor));
                for (int i = 1; i < table.getColumnCount(); i++) {
                    TableColumn column = table.getColumn(i);
                    column.setWidth((int) (column.getWidth() * factor));
                }
                matrixSize.setWidthDefault((int) (matrixSize.getWidthDefault() * factor));
                tableViewer.refresh();
            } else if (e.getSource() == btnDisplayValues) {
                displayValues = btnDisplayValues.getSelection();
                tableViewer.refresh();
            } else if (e.getSource() == btnHide) {
                TreeSet<String> includeElements = new TreeSet<>();
                TreeSet<String> excludeElements = new TreeSet<>();
                SelectGenomeElementDialog dialog =
                        new SelectGenomeElementDialog(shell, partService, includeElements, excludeElements, Genome.loadGenome(genomeName));
                if (dialog.open() == 0) {
                    excludeRow.clear();
                    for (String row : excludeElements) {
                        excludeRow.add(row);
                    }
                    updateInfo();
                }
            } else if (e.getSource() == btnHelp) {
                if (this.isTranscriptomics()) {
                    HelpPage.helpTranscriptHeatMapViewer(partService);
                } else {
                    HelpPage.helpProteomHeatMapViewer(partService);
                }
            }
        } catch (Exception e1) {

        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    public ExpressionMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(ExpressionMatrix matrix) {
        this.matrix = matrix;
    }

    public ArrayList<String> getExcludeRow() {
        return excludeRow;
    }

    public void setExcludeRow(ArrayList<String> excludeRow) {
        this.excludeRow = excludeRow;
    }

    public ArrayList<String> getExcludeColumn() {
        return excludeColumn;
    }

    public void setExcludeColumn(ArrayList<String> excludeColumn) {
        this.excludeColumn = excludeColumn;
    }

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genomeName) {
        this.genomeName = genomeName;
    }

    public ExpressionMatrix getMatrixDisplayed() {
        return matrixDisplayed;
    }

    public void setMatrixDisplayed(ExpressionMatrix matrixDisplayed) {
        this.matrixDisplayed = matrixDisplayed;
    }

    public ColorMapperList getColorMapperList() {
        return colorMapperList;
    }

    public void setColorMapperList(ColorMapperList colorMapperList) {
        this.colorMapperList = colorMapperList;
    }

    public boolean isTranscriptomics() {
        return transcriptomics;
    }

    public void setTranscriptomics(boolean transcriptomics) {
        this.transcriptomics = transcriptomics;
    }

}
