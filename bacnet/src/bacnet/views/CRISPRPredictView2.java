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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import bacnet.genomeBrowser.GenomeTranscriptomeView;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class CRISPRPredictView2 implements SelectionListener {

    public static final String ID = "bacnet.CRISPRPredictView";

    public String SCRIPT_PATH = "/Users/cbecavin/Documents/BACNET/CRISPROmics";
    public String PYTHON_PATH = "/Users/cbecavin/anaconda/envs/py36/bin";
    public String PYTHON_SERVER = "/srv/data/CRISPROmics/python";
    public String SINGULARITY_PATH = "/srv/data/CRISPROmics/python/crispr_predict.img";

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;
    private TableViewer tableViewer;
    private Table table;
    private Button btnUploadFastaFile;
    private Button btnRunPredicition;
    private ArrayList<String> columnNames = new ArrayList<>();
    private Button btnRunTest;
    private Combo combo;
    private Text text;

    @Inject
    public CRISPRPredictView2() {}

    /**
     * Create contents of the view part.
     */
    @PostConstruct
    public void createControls(Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Composite composite_3 = new Composite(composite, SWT.NONE);
        composite_3.setLayout(new GridLayout(1, false));
        composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        composite_3.setBackground(ResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Label lblCrisprGuidePrediction = new Label(composite_3, SWT.NONE);
        lblCrisprGuidePrediction.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        lblCrisprGuidePrediction.setSize(228, 18);
        lblCrisprGuidePrediction.setText("CRISPR guide prediction tool");
        lblCrisprGuidePrediction.setBackground(ResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblCrisprGuidePrediction.setFont(ResourceManager.getTitleFont());

        Label lblNewLabel_1 = new Label(composite, SWT.NONE);
        lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        Composite composite_2 = new Composite(composite, SWT.NONE);
        composite_2.setLayout(new GridLayout(1, false));

        Label lblEnterYourFasta = new Label(composite_2, SWT.NONE);
        lblEnterYourFasta.setText("Enter your fasta sequence here");

        text = new Text(composite_2, SWT.BORDER | SWT.H_SCROLL | SWT.CANCEL | SWT.MULTI);
        GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_text.heightHint = 110;
        gd_text.widthHint = 237;
        text.setLayoutData(gd_text);

        btnUploadFastaFile = new Button(composite_2, SWT.NONE);
        btnUploadFastaFile.setText("Upload fasta file");
        btnUploadFastaFile.addSelectionListener(this);

        Label lblNewLabel = new Label(composite_2, SWT.NONE);

        Label lblChooseModel = new Label(composite_2, SWT.NONE);
        lblChooseModel.setText("Choose model");

        combo = new Combo(composite_2, SWT.NONE);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        combo.add("Cas9SeqOnly");
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
        Label lblClickOneTime = new Label(composite_1, SWT.NONE);
        lblClickOneTime.setText("Double click to acces genome position on Genome Viewer");
        lblClickOneTime.setFont(SWTResourceManager.getBodyFont(10, SWT.NORMAL));
        tableViewer = new TableViewer(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_table.heightHint = 106;
        gd_table.widthHint = 178;
        table = tableViewer.getTable();
        table.setLayoutData(gd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                String position = table.getItem(table.getSelectionIndex()).getText(columnNames.indexOf("pos"));
                System.out.println("Select: " + position);
                int pos = Integer.parseInt(position);
                GenomeTranscriptomeView.displayCRISPROmics(partService, pos);
            }
        });
        scrolledComposite.setContent(composite);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void updateTable(String[][] results) {
        table.removeAll();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        String[] titles = {"target", "pos", "fitness", "quartile"};
        for (int i = 0; i < titles.length; i++) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(titles[i]);
            column.setAlignment(SWT.LEFT);
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

    @PreDestroy
    public void dispose() {}

    @Focus
    public void setFocus() {
        // TODO Set the focus to control
    }

    private void runPrediction(String path, String fasta, String model, String output) {
        String os = System.getProperty("os.name");
        // System.out.println("OS: "+os);
        if (os.equals("Mac OS X")) {
            String prediction_cas = SCRIPT_PATH + "/prediction_cas_prod.py";
            String execProcess =
                    "python " + prediction_cas + " -p " + path + " -f " + fasta + " -m " + model + " -o " + output;
            // execProcess = "source activate py36";
            try {
                File tempPNGFile = File.createTempFile("python", "Process.sh");
                String bash_process = "#!/bin/bash\nexport PATH=\"" + PYTHON_PATH
                        + ":$PATH\"\npython --version\necho Run script\n" + execProcess;
                FileUtils.saveText(bash_process, tempPNGFile.getAbsolutePath());
                System.out.println(tempPNGFile);
                String[] command = {"sh", tempPNGFile.getAbsolutePath()};
                CMD.runProcess(command, true);
                String[][] results = TabDelimitedTableReader.read(output);
                updateTable(results);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // Run on server
            String prediction_cas = PYTHON_SERVER + "/prediction_cas_prod.py";
            String execProcess = "/usr/local/bin/singularity run " + SINGULARITY_PATH + " -f " + fasta + " -m " + model
                    + " -o " + output;
            try {
                File tempPNGFile = File.createTempFile("python", "Process.sh");
                String bash_process = "#!/bin/bash\nid\necho $HOME\necho $PATH\npwd\necho Run script\n" + execProcess;
                FileUtils.saveText(bash_process, tempPNGFile.getAbsolutePath());
                System.out.println(tempPNGFile);
                String[] command = {"sh", tempPNGFile.getAbsolutePath()};
                CMD.runProcess(command, true);
                String[][] results = TabDelimitedTableReader.read(output);
                updateTable(results);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnUploadFastaFile) {
            try {
                FileDialog fd = new FileDialog(shell, SWT.OPEN);
                fd.setText("Open fasta file");
                String fileName = fd.open();
                if (fileName != null) {
                    String fasta_text = FileUtils.readText(fileName);
                    text.setText(fasta_text);
                }
            } catch (Exception e1) {
                System.out.println("Cannot read the fasta file");
            }
        } else if (e.getSource() == btnRunPredicition) {
            String os = System.getProperty("os.name");
            // System.out.println("OS: "+os);
            if (os.equals("Mac OS X")) {
                try {
                    File fasta_file = File.createTempFile("Fasta", ".fasta");
                    FileUtils.saveText(text.getText(), fasta_file.getAbsolutePath());
                    String model = combo.getItem(combo.getSelectionIndex());
                    File output = File.createTempFile("Output", ".txt");
                    runPrediction(SCRIPT_PATH, fasta_file.getAbsolutePath(), model, output.getAbsolutePath());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else {
                String path = SCRIPT_PATH;
                String fasta_file = "Fasta_" + Math.random() + ".fasta";
                String fasta_seq = text.getText();
                String model = combo.getItem(combo.getSelectionIndex());
                FileUtils.saveText(fasta_seq, "/root/" + fasta_file);
                String output = "Output_predict_" + Math.random() + ".txt";
                runPrediction(path, fasta_file, model, output);
            }
        } else if (e.getSource() == btnRunTest) {
            String os = System.getProperty("os.name");
            // System.out.println("OS: "+os);
            if (os.equals("Mac OS X")) {
                try {
                    String fasta_file = SCRIPT_PATH + "/example/myfasta.txt";
                    String model = combo.getItem(combo.getSelectionIndex());
                    File output = File.createTempFile("Output", ".txt");
                    runPrediction(SCRIPT_PATH, fasta_file, model, output.getAbsolutePath());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else {
                String path = SCRIPT_PATH;
                String fasta = "myfasta.txt";
                String model = "Cas9SeqOnly";
                String output = "Output_predict_" + Math.random() + ".txt";
                runPrediction(path, fasta, model, output);
            }
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }
}
