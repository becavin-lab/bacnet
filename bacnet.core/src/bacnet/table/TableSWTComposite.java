package bacnet.table;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.widgets.Text;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.ColorMapper;
import bacnet.table.core.ColorMapperList;
import bacnet.table.core.FilterList;
import bacnet.table.core.MatrixSize;
import bacnet.table.core.TableViewerComparator;
import bacnet.table.gui.ColorMapperWizard;
import bacnet.table.gui.FilterExpressionMatrixDialog;
import bacnet.table.gui.HideExpressionMatrixDialog;
import bacnet.table.gui.TableFilter;
import bacnet.utils.BasicColor;

public class TableSWTComposite extends Composite implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -5019287630134430710L;
    private Text searchText;
    private Table table;
    private Label labelRow;
    private Button btnSavepng;
    private Button btnSaveText;

    /*
     * data
     */
    private ExpressionMatrix matrix;
    private ExpressionMatrix matrixDisplayed;
    private Genome genome;

    private ArrayList<String> columnNames = new ArrayList<>();
    private ColorMapperList colorMapperList = new ColorMapperList();
    private MatrixSize matrixSize = new MatrixSize();
    private FilterList filters;
    private TableFilter searchFilter;

    private boolean displayValues = true;
    private ArrayList<String> excludeRow = new ArrayList<String>();
    private ArrayList<String> excludeColumn = new ArrayList<String>();
    private ArrayList<String> hideDialogExcludeRow = new ArrayList<String>();
    private ArrayList<String> hideDialogExcludeColumn = new ArrayList<String>();

    private TableViewer tableViewer;
    private TableViewerComparator comparator;
    private Label labelColumn;
    private Button btnHide;
    private Button btnColormapper;
    private Button btnDisplayValues;
    private Composite composite;
    private Button btnZoomIn;
    private Button btnZoomOut;
    private Composite composite_4;
    private Label lblX;
    private Button btnFilter;
    private Label lblSearch;
    private Label labelVoid;

    private EPartService partService;
    private Shell shell;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public TableSWTComposite(Composite parent, int style, Shell shell, EPartService partService) {
        super(parent, SWT.BORDER);
        this.partService = partService;
        this.shell = shell;
        setLayout(new GridLayout(1, false));

        Composite compositeTable = new Composite(this, SWT.NONE);
        compositeTable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        compositeTable.setLayout(new GridLayout(3, false));

        Composite composite_1 = new Composite(compositeTable, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        composite_1.setLayout(new GridLayout(2, false));

        Composite composite_2 = new Composite(composite_1, SWT.BORDER);
        composite_2.setLayout(new GridLayout(4, false));
        btnHide = new Button(composite_2, SWT.NONE);
        btnHide.setToolTipText("Select row or column to hide");
        btnHide.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/hideData.bmp"));
        btnHide.addSelectionListener(this);

        btnFilter = new Button(composite_2, SWT.NONE);
        GridData gd_btnFilter = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnFilter.widthHint = 50;
        btnFilter.setLayoutData(gd_btnFilter);
        btnFilter.setToolTipText("Create different filters");
        btnFilter.setText("Filters");
        btnFilter.addSelectionListener(this);

        lblSearch = new Label(composite_2, SWT.NONE);
        lblSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSearch.setText("Search: ");

        searchText = new Text(composite_2, SWT.BORDER | SWT.SEARCH);
        GridData gd_searchText = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_searchText.widthHint = 71;
        searchText.setLayoutData(gd_searchText);
        // New to support the search
        searchText.addKeyListener(new KeyAdapter() {
            /**
             * 
             */
            private static final long serialVersionUID = -6052956098916936559L;

            public void keyReleased(KeyEvent ke) {
                searchFilter.setSearchText(searchText.getText());
                tableViewer.refresh();
            }

        });

        composite = new Composite(composite_1, SWT.BORDER);
        composite.setLayout(new GridLayout(4, false));

        btnZoomIn = new Button(composite, SWT.NONE);
        btnZoomIn.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomIN.bmp"));
        btnZoomIn.addSelectionListener(this);
        btnZoomOut = new Button(composite, SWT.NONE);
        btnZoomOut.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomOUT.bmp"));
        btnZoomOut.addSelectionListener(this);
        btnColormapper = new Button(composite, SWT.NONE);
        btnColormapper.setToolTipText("Change colors");
        btnColormapper.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/colorMapper.bmp"));

        btnDisplayValues = new Button(composite, SWT.CHECK);
        btnDisplayValues.setSelection(true);
        btnDisplayValues.setText("Display values");
        btnDisplayValues.addSelectionListener(this);
        btnColormapper.addSelectionListener(this);

        composite_4 = new Composite(composite_1, SWT.BORDER);
        composite_4.setLayout(new GridLayout(6, false));

        btnSaveText = new Button(composite_4, SWT.NONE);
        btnSaveText.setToolTipText("Save to Tab separated text format");
        btnSaveText.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/txt.bmp"));
        btnSaveText.addSelectionListener(this);

        btnSavepng = new Button(composite_4, SWT.NONE);
        btnSavepng.setToolTipText("Save to image (.png)");
        btnSavepng.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/fileIO/png.bmp"));
        btnSavepng.addSelectionListener(this);

        new Label(composite_4, SWT.NONE);

        labelRow = new Label(composite_4, SWT.NONE);
        GridData gd_labelRow = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_labelRow.widthHint = 40;
        labelRow.setLayoutData(gd_labelRow);
        labelRow.setText("00000");

        lblX = new Label(composite_4, SWT.NONE);
        lblX.setText(" x ");

        labelColumn = new Label(composite_4, SWT.NONE);
        GridData gd_labelColumn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_labelColumn.widthHint = 40;
        labelColumn.setLayoutData(gd_labelColumn);
        labelColumn.setText("000");

        labelVoid = new Label(composite_1, SWT.NONE);
        labelVoid.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        labelVoid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        tableViewer = new TableViewer(this, SWT.BORDER | SWT.HIDE_SELECTION | SWT.VIRTUAL);
        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        new Label(this, SWT.NONE);

    }

    /**
     * Init data and filter lists
     * 
     * @param matrix
     */
    public void initData(ExpressionMatrix matrix, Genome genome) {
        this.matrix = matrix;
        this.genome = genome;
        excludeRow = new ArrayList<String>();
        excludeColumn = new ArrayList<String>();
        searchFilter = new TableFilter();
        tableViewer.addFilter(searchFilter);
        colorMapperList = new ColorMapperList(this.matrix, shell);
        filters = new FilterList(this.matrix);
        if (matrix.getNumberRow() != 0) {
            updateInfo();
            table.setVisible(true);
            labelVoid.setText("");
        } else {
            labelVoid.setText("No Element to Display");
            table.setVisible(false);
        }
    }

    /**
     * Update all viewers and labels
     */
    public void updateInfo() {
        matrixDisplayed = getSubMatrix();
        int nbRow = matrixDisplayed.getNumberRow();
        if (nbRow == 0) {
            table.setVisible(false);
            labelVoid.setText("No Element to Display");
        } else {
            setColumnNames();
            int nbColumn = matrixDisplayed.getNumberColumnWithAnnotation();
            labelRow.setText("" + nbRow);
            labelColumn.setText("" + nbColumn);

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
            if (header.contains(" vs ")) {
                String newHeader = header.split(" vs ")[0];
                columnNames.add(newHeader);
            } else {
                columnNames.add(header);
            }
        }

    }

    private void createColumns() {

        // TableColumn[] columns = ;
        // ArrayList<String> colName = new ArrayList<String>();
        // for(String coll:matrix.getHeaders()) System.out.println(coll);
        //

        // First column is for the rownames
        TableViewerColumn col = createTableViewerColumn("", matrixSize.getWidthRowNameDefault(), 0);
        col.setLabelProvider(new ColumnLabelProvider() {
            /**
             * 
             */
            private static final long serialVersionUID = 1088733089521320381L;

            @Override
            public String getText(Object element) {
                String[] row = (String[]) element;
                return row[0];
            }

            @Override
            public Font getFont(Object element) {
                ColorMapper colorMapper = colorMapperList.getCorrespondingMapper(matrixDisplayed.getHeader(0));
                return colorMapper.getFontRowName();
            }

            @Override
            public String getToolTipText(Object element) {
                String[] row = (String[]) element;
                Genome genome = Genome.loadEgdeGenome();
                Sequence seq = genome.getElement((String) row[0]);
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

        // create data columns
        for (int i = 0; i < matrixDisplayed.getHeaders().size(); i++) {
            // if(!colName.contains(matrix.getHeader(i))){
            final int k = i + 1;
            TableViewerColumn col2 = createTableViewerColumn(columnNames.get(i), matrixSize.getWidthDefault(), k);
            col.getColumn().setToolTipText(matrix.getHeader(k - 1));
            col2.setLabelProvider(new CellLabelProvider() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 6650938475051546454L;

                @Override
                public void update(ViewerCell cell) {
                    // TODO Auto-generated method stub
                }
            });
            col2.setLabelProvider(new ColumnLabelProvider() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 3427293599471487650L;

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

            // }

        }

        // create annotation columns
        for (int i = 0; i < matrixDisplayed.getHeaderAnnotation().size(); i++) {
            // if(!colName.contains(matrix.getHeader(i))){
            final int k = i + matrixDisplayed.getHeaders().size() + 1;
            TableViewerColumn col3 = createTableViewerColumn(matrix.getHeaderAnnotation().get(i),
                    matrixSize.getWidthRowNameDefault(), k);
            col3.setLabelProvider(new ColumnLabelProvider() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 6200747667379741077L;

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
            // }
        }
    }

    private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
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
            private static final long serialVersionUID = -5608066016814727966L;

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

    private void updateExcludeElements() {
        excludeColumn.clear();
        excludeRow.clear();

        for (String col : filters.getExcludeColumn())
            excludeColumn.add(col);
        for (String row : filters.getExcludeRow())
            excludeRow.add(row);
        for (String col : hideDialogExcludeColumn)
            excludeColumn.add(col);
        for (String row : hideDialogExcludeRow)
            excludeRow.add(row);

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
                // File file = File.createTempFile("Matrix-"+matrix.firstRowName, ".txt");
                // getSubMatrix().saveTab(file.getAbsolutePath(),matrix.firstRowName);
                // FileUtils.saveFile(file);
            } else if (e.getSource() == btnSavepng) {
                // File file = File.createTempFile("Matrix-", ".png");
                // System.out.println(file.getAbsolutePath());
                // ImageExportUtilsSWT.saveSVGtoJPG("", "", 0,0,0);
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
                // System.out.println("in row:"+(int)(height*factor));
                // System.out.println("in column:"+(int)(table.getColumn(1).getWidth()));
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
                // System.out.println("out row:"+(int)(height*factor));
                // System.out.println("out column:"+(int)(table.getColumn(1).getWidth()));
                tableViewer.refresh();
            } else if (e.getSource() == btnDisplayValues) {
                displayValues = btnDisplayValues.getSelection();
                tableViewer.refresh();
            } else if (e.getSource() == btnHide) {
                HideExpressionMatrixDialog dialog = new HideExpressionMatrixDialog(matrix, genome, hideDialogExcludeRow,
                        hideDialogExcludeColumn, shell, partService);
                int ok = dialog.open();
                if (ok == Status.OK) {
                    updateExcludeElements();
                    updateInfo();
                }
            } else if (e.getSource() == btnFilter) {
                FilterExpressionMatrixDialog dialog = new FilterExpressionMatrixDialog(filters, shell);
                int ok = dialog.open();
                if (ok == Status.OK) {
                    updateExcludeElements();
                    updateInfo();
                    tableViewer.refresh();
                }
            }
        } catch (Exception e1) {

        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

}
