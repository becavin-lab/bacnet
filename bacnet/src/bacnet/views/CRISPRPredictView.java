package bacnet.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.table.core.TableViewerComparator;
import bacnet.utils.ArrayUtils;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class CRISPRPredictView implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 4263354745027835111L;

    public static final String ID = "bacnet.CRISPRPredictView";

    public String SCRIPT_PATH = "/Users/cbecavin/Documents/BACNET/CRISPRGo";
    public String SCRIPT_SERVER = "/srv/data/CRISPRGo/python";
    public String PYTHON_PATH = "/Users/cbecavin/anaconda/envs/py36/bin";
    public String PYTHON_SERVER = "/srv/data/CRISPRGo/python";
    public String SINGULARITY_PATH = "/srv/data/CRISPRGo/python/crispr_predict.img";
    public String MODEL_CAS9 = "Cas9 E.Coli.";
    public String DEFAULT_MODEL = "seqReduc_data1217";

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;
    private TableViewer tableViewer;
    private Table table;
    @SuppressWarnings("unused")
    private Button btnUploadFastaFile;
    private Button btnRunPredicition;
    private ArrayList<String> columnNames = new ArrayList<>();
    private Button btnRunTest;
    private Combo combo;
    private Text text;
    private Button btnTxt;
    private String[][] results;

    @Inject
    public CRISPRPredictView() {}

    /**
     * Create contents of the view part.
     */
    @SuppressWarnings("unused")
    @PostConstruct
    public void createControls(Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Composite composite_2 = new Composite(composite, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        Label lblSummaryText = new Label(composite_2, SWT.WRAP);
        GridData gd_lblSummaryText = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
        gd_lblSummaryText.widthHint = 100;
        gd_lblSummaryText.heightHint = 100;
        lblSummaryText.setLayoutData(gd_lblSummaryText);
        lblSummaryText.setText(
                "This tool provides predictions of the ability of guide RNAs to efficiently direct Cas9 cleavage in E. coli. The predictions are made using a neural network trained on data generated in a genome-wide screen of Cas9 cleavage activity in the chromosome of E. coli strain MG1655.");

        Label lblEnterYourFasta = new Label(composite_2, SWT.NONE);
        lblEnterYourFasta.setText("Enter your sequence here (>50bp)");

        text = new Text(composite_2, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
        GridData gd_text = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
        gd_text.heightHint = 300;
        gd_text.widthHint = 237;
        text.setLayoutData(gd_text);

        Label lblNewLabel = new Label(composite_2, SWT.NONE);

        Label lblChooseModel = new Label(composite_2, SWT.NONE);
        lblChooseModel.setText("Choose model");

        combo = new Combo(composite_2, SWT.NONE);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        combo.add(MODEL_CAS9);
        combo.select(0);
        Label label = new Label(composite_2, SWT.NONE);

        btnRunPredicition = new Button(composite_2, SWT.NONE);
        btnRunPredicition.setText("Run prediction");
        btnRunPredicition.addSelectionListener(this);

        btnRunTest = new Button(composite_2, SWT.NONE);
        btnRunTest.setText("Run prediction (on test data)");
        btnRunTest.addSelectionListener(this);

        Composite composite_1 = new Composite(composite, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        composite_1.setLayout(new GridLayout(1, false));

        Composite composite_3 = new Composite(composite_1, SWT.NONE);
        composite_3.setLayout(new GridLayout(2, false));

        btnTxt = new Button(composite_3, SWT.NONE);
        btnTxt.setImage(ResourceManager.getPluginImage("bacnet", "icons/fileIO/txt.bmp"));
        btnTxt.addSelectionListener(this);
        btnTxt.setToolTipText("Save to Tab separated text format");
        new Label(composite_3, SWT.NONE);

        // Label lblClickOneTime = new Label(composite_3, SWT.NONE);
        // lblClickOneTime.setSize(150, 14);
        // lblClickOneTime.setText("Double click to display sequence");
        // lblClickOneTime.setFont(SWTResourceManager.getBodyFont(10,SWT.NORMAL));
        tableViewer = new TableViewer(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_table.heightHint = 106;
        gd_table.widthHint = 178;
        table = tableViewer.getTable();
        table.setLayoutData(gd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        // tableViewer.addDoubleClickListener(new IDoubleClickListener() {
        //
        // @Override
        // public void doubleClick(DoubleClickEvent event) {
        // String position =
        // table.getItem(table.getSelectionIndex()).getText(columnNames.indexOf("pos"));
        // System.out.println("Select: "+position);
        // int pos = Integer.parseInt(position);
        // GenomeTranscriptomeView.displayCRISPROmics(partService, pos);
        // }
        // });
        scrolledComposite.setContent(composite);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void updateTable(String[][] results) {
        table.removeAll();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableViewerComparator comparator = new TableViewerComparator();
        tableViewer.setComparator(comparator);
        String[] titles = {"Target", "Pos", "sgRNA activity", "Quartile"};
        for (int i = 0; i < titles.length; i++) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(titles[i]);
            column.setAlignment(SWT.LEFT);
            // column.addSelectionListener(getSelectionAdapter(column, i, comparator));
            columnNames.add(titles[i]);
        }

        // System.out.println(seq.getFeaturesText());
        for (int i = 1; i < results.length; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            for (int j = 0; j < results[0].length; j++) {
                item.setText(j, results[i][j]);
            }
        }
        for (int i = 0; i < titles.length; i++) {
            table.getColumn(i).pack();
        }

        table.update();
        table.redraw();

    }

    // private SelectionAdapter getSelectionAdapter(final TableColumn column,final
    // int index,
    // TableViewerComparator comparator) {
    // SelectionAdapter selectionAdapter = new SelectionAdapter() {
    // /**
    // *
    // */
    // private static final long serialVersionUID = -6997325110066606691L;
    //
    // @Override
    // public void widgetSelected(SelectionEvent e) {
    // comparator.setColumn(index);
    // int dir = comparator.getDirection();
    // System.out.println("yo "+column+" index "+index);
    // table.update();
    // table.redraw();
    // System.out.println("test");
    // tableViewer.refresh();
    // }
    // };
    // return selectionAdapter;
    // }

    @PreDestroy
    public void dispose() {}

    @Focus
    public void setFocus() {
        // TODO Set the focus to control
    }

    private void runPrediction(String path, String fasta, String model, String output) {
        String os = System.getProperty("os.name");
        String execProcess = "";
        if (os.equals("Mac OS X")) {
            execProcess = "python " + path + "/prediction_cas_prod.py" + " -p " + SCRIPT_PATH + " -f " + fasta + " -m "
                    + model + " -o " + output;
        } else {
            execProcess = "/usr/local/bin/singularity run " + SINGULARITY_PATH + " -f " + fasta + " -m " + model
                    + " -o " + output;
        }
        try {
            File tempPNGFile = File.createTempFile("python", "Process.sh");
            String bash_process = "#!/bin/bash\nexport PATH=\"" + PYTHON_PATH
                    + ":$PATH\"\npython --version\necho Run script\n" + execProcess;
            FileUtils.saveText(bash_process, tempPNGFile.getAbsolutePath());
            System.out.println(tempPNGFile);
            String[] command = {"sh", tempPNGFile.getAbsolutePath()};
            CMD.runProcess(command, true);
            if (os.equals("Mac OS X")) {
                results = TabDelimitedTableReader.read(output);
            } else {
                results = TabDelimitedTableReader.read("/root/" + output);

            }
            updateTable(results);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        String os = System.getProperty("os.name");
        if (e.getSource() == btnTxt) {
            String arrayRep = ArrayUtils.toString(results);
            String arrayRepHTML = TabDelimitedTableReader.getHTMLVersion(results);
            SaveFileUtils.saveTextFile("CRISPR_Prediction_guide.txt", arrayRep, true, "", arrayRepHTML, partService,
                    shell);
        } else if (e.getSource() == btnRunPredicition) {
            if (os.equals("Mac OS X")) {
                try {
                    File fasta_file = File.createTempFile("Fasta", ".fasta");
                    FileUtils.saveText(">Seq\n" + text.getText(), fasta_file.getAbsolutePath());
                    // String model = combo.getItem(combo.getSelectionIndex());
                    String model = DEFAULT_MODEL;
                    File output = File.createTempFile("Output", ".txt");
                    runPrediction(SCRIPT_PATH, fasta_file.getAbsolutePath(), model, output.getAbsolutePath());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else {
                String path = SCRIPT_PATH;
                String fasta_file = "Fasta_" + Math.random() + ".fasta";
                String fasta_seq = ">Seq\n" + text.getText();
                // String model = combo.getItem(combo.getSelectionIndex());
                String model = DEFAULT_MODEL;
                FileUtils.saveText(fasta_seq, "/root/" + fasta_file);
                String output = "Output_predict_" + Math.random() + ".txt";
                runPrediction(path, fasta_file, model, output);
            }
        } else if (e.getSource() == btnRunTest) {
            // System.out.println("OS: "+os);
            if (os.equals("Mac OS X")) {
                try {
                    String fasta_file = SCRIPT_PATH + "/example/myfasta.fasta";
                    // String model = combo.getItem(combo.getSelectionIndex());
                    String model = DEFAULT_MODEL;
                    File output = File.createTempFile("Output", ".txt");
                    runPrediction(SCRIPT_PATH, fasta_file, model, output.getAbsolutePath());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else {
                String path = SCRIPT_PATH;
                String fasta = "/opt/CRISPRGo/example/myfasta.fasta";
                String model = DEFAULT_MODEL;
                String output = "Output_predict_" + Math.random() + ".txt";
                runPrediction(path, fasta, model, output);
            }

            String filename = "/Users/cbecavin/Documents/BACNET/CRISPRGo/example/mySeq.txt";
            if (!os.equals("Mac OS X")) {
                filename = PYTHON_SERVER + "/example/mySeq.txt";
            }
            String sequence = FileUtils.readText(filename);
            text.setText(sequence);
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }
}
