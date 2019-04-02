package bacnet.genomeBrowser.dialog;

import java.util.ArrayList;
import java.util.TreeSet;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.genomeBrowser.core.Track;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.BioConditionComparator;
import bacnet.utils.ArrayUtils;

public class AddTranscriptomicsDataDialog extends TitleAreaDialog implements SelectionListener {
    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            return element.toString();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2962428673771724278L;
    private Track track;
    private BioConditionComparator comparatorBioCondition;
    private ArrayList<String> columnNames = new ArrayList<>();

    /**
     * Array used to init data
     */
    private String[][] bioCondsArray;
    /**
     * List used to init data
     */
    private ArrayList<String[]> bioConds;
    /**
     * Array which will be displayed
     */
    private ArrayList<String[]> bioCondsToDisplay;

    private final Image imageTSS;
    private final Image imageRNASeq;
    private final Image imageTSSTilingGeneExpr;
    private final Image imageTilingGeneExpr;
    private final Image imageGeneExpr;
    private Button btnGeneExpression;
    private Button btnRnaseq;
    private Button btnTss;
    private Button btnTermSeq;
    private Button btnRiboSeq;
    private Button btnBhiBroth;
    private Button btnMinimalMediaBroth;
    private Button btnTrypticSoyBroth;
    private Button btnCacoCells;
    private Button btnTurkeyDeliMeat;
    private Button btnPorcineBileCells;
    private Button btnMouseSpleenCells;
    private Button btnMouseMacrophagesCells;
    private Button btnMouseIntestineCells;
    private Button btnHumanBloodCells;
    private Button btnLagPhase;
    private Button btnExponential;
    private Button btnStationnary;
    private Button btnSurvival;
    private Button btnDeath;
    private Button btnRegrowth;
    private Button btnTiling;
    private Button btnLbBroth;
    private Combo comboMutant;
    private Button btnChooseOneMutant;
    private Button btnNoneMutant;
    private Button btnAllMutant;
    private Text textSearch;
    private Combo comboGenome;
    private TableViewer tableBioCondition;
    private Button btnUnpublished;

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public AddTranscriptomicsDataDialog(Shell parentShell, Track track) {
        super(parentShell);
        setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE);
        this.track = track;

        imageTSS = ResourceManager.getPluginImage("bacnet", "icons/tss.bmp");
        imageRNASeq = ResourceManager.getPluginImage("bacnet", "icons/rnaSeq.bmp");
        imageTSSTilingGeneExpr = ResourceManager.getPluginImage("bacnet", "icons/tssTilingGeneExpr.bmp");
        imageTilingGeneExpr = ResourceManager.getPluginImage("bacnet", "icons/TilingGeneExpr.bmp");
        imageGeneExpr = ResourceManager.getPluginImage("bacnet", "icons/GeneExpr.bmp");
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage("The list below shows you the different type of transcriptomics data available for display");
        setTitle("Add transcriptomics data");
        Composite area = (Composite) super.createDialogArea(parent);

        Composite compositeGeneral = new Composite(area, SWT.NONE);
        compositeGeneral.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        compositeGeneral.setLayout(new GridLayout(2, false));

        ScrolledComposite scrolledComposite =
                new ScrolledComposite(compositeGeneral, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 2);
        gd_scrolledComposite.widthHint = 300;
        scrolledComposite.setLayoutData(gd_scrolledComposite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite composite_3 = new Composite(scrolledComposite, SWT.NONE);
        composite_3.setLayout(new GridLayout(1, false));

        Label lblSearch = new Label(composite_3, SWT.NONE);
        lblSearch.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblSearch.setText("Search");

        textSearch = new Text(composite_3, SWT.BORDER);
        GridData gd_textSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_textSearch.widthHint = 250;
        textSearch.setLayoutData(gd_textSearch);
        textSearch.addKeyListener(new KeyListener() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == 16777296 || e.keyCode == 13) {
                    System.out.println("Search for " + textSearch.getText());
                    bioCondsToDisplay.clear();
                    for (String[] row : bioConds) {
                        boolean found = false;
                        for (String cell : row) {
                            if (cell.contains(textSearch.getText())) {
                                found = true;
                            }
                        }
                        if (found) {
                            bioCondsToDisplay.add(row);
                        }
                    }
                    updateInfo();
                    updateBioConditionTable();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Label label_6 = new Label(composite_3, SWT.NONE);
        {
            Label lblGenome = new Label(composite_3, SWT.NONE);
            lblGenome.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
            lblGenome.setText("Genome");
        }
        {
            comboGenome = new Combo(composite_3, SWT.NONE);
            GridData gd_comboGenome = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
            gd_comboGenome.widthHint = 270;
            comboGenome.setLayoutData(gd_comboGenome);
            comboGenome.addSelectionListener(this);
        }
        {
            Label label_1 = new Label(composite_3, SWT.NONE);
        }
        {
            Label lblNewLabel_2 = new Label(composite_3, SWT.NONE);
            lblNewLabel_2.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
            lblNewLabel_2.setText("Data type");
        }
        {
            Composite composite = new Composite(composite_3, SWT.NONE);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            composite.setLayout(new GridLayout(1, false));

            btnGeneExpression = new Button(composite, SWT.CHECK);
            btnGeneExpression.setText("Gene Expression");

            btnTiling = new Button(composite, SWT.CHECK);
            btnTiling.setText("Tiling");

            btnTss = new Button(composite, SWT.CHECK);
            btnTss.setText("TSS");

            btnTermSeq = new Button(composite, SWT.CHECK);
            btnTermSeq.setText("TermSeq");

            btnRnaseq = new Button(composite, SWT.CHECK);
            btnRnaseq.setText("RNA-Seq");

            btnRiboSeq = new Button(composite, SWT.CHECK);
            btnRiboSeq.setText("RiboSeq");

            btnUnpublished = new Button(composite, SWT.CHECK);
            btnUnpublished.setText("Unpublished");

            btnGeneExpression.addSelectionListener(this);
            btnTiling.addSelectionListener(this);
            btnTss.addSelectionListener(this);
            btnRnaseq.addSelectionListener(this);

        }

        Label label_2 = new Label(composite_3, SWT.NONE);

        Label lblIntracellularGrowth = new Label(composite_3, SWT.NONE);
        lblIntracellularGrowth.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblIntracellularGrowth.setText("Intracellular growth");

        Composite composite_1_1 = new Composite(composite_3, SWT.NONE);
        composite_1_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_1_1.setLayout(new GridLayout(1, false));

        btnCacoCells = new Button(composite_1_1, SWT.CHECK);
        btnCacoCells.setText("Caco-2 cells");

        btnHumanBloodCells = new Button(composite_1_1, SWT.CHECK);
        btnHumanBloodCells.setText("Human blood cells");

        btnMouseIntestineCells = new Button(composite_1_1, SWT.CHECK);
        btnMouseIntestineCells.setText("Mouse intestine cells");

        btnMouseMacrophagesCells = new Button(composite_1_1, SWT.CHECK);
        btnMouseMacrophagesCells.setText("Mouse macrophages cells");

        btnMouseSpleenCells = new Button(composite_1_1, SWT.CHECK);
        btnMouseSpleenCells.setText("Mouse spleen cells");

        btnPorcineBileCells = new Button(composite_1_1, SWT.CHECK);
        btnPorcineBileCells.setText("Porcine Bile cells");

        btnTurkeyDeliMeat = new Button(composite_1_1, SWT.CHECK);
        btnTurkeyDeliMeat.setText("Turkey Deli Meat cells");

        btnCacoCells.addSelectionListener(this);
        btnHumanBloodCells.addSelectionListener(this);
        btnMouseIntestineCells.addSelectionListener(this);
        btnMouseMacrophagesCells.addSelectionListener(this);
        btnMouseSpleenCells.addSelectionListener(this);
        btnPorcineBileCells.addSelectionListener(this);
        btnTurkeyDeliMeat.addSelectionListener(this);

        Label label_1_1 = new Label(composite_3, SWT.NONE);

        Label lblBroth = new Label(composite_3, SWT.NONE);
        lblBroth.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblBroth.setText("Broth");

        Composite composite_4 = new Composite(composite_3, SWT.NONE);
        composite_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_4.setLayout(new GridLayout(1, false));

        btnBhiBroth = new Button(composite_4, SWT.CHECK);
        btnBhiBroth.setText("BHI Broth");

        btnTrypticSoyBroth = new Button(composite_4, SWT.CHECK);
        btnTrypticSoyBroth.setText("Tryptic Soy Broth");

        btnLbBroth = new Button(composite_4, SWT.CHECK);
        btnLbBroth.setText("LB Broth");

        btnMinimalMediaBroth = new Button(composite_4, SWT.CHECK);
        btnMinimalMediaBroth.setText("Minimal Media Broth");

        btnBhiBroth.addSelectionListener(this);
        btnTrypticSoyBroth.addSelectionListener(this);
        btnLbBroth.addSelectionListener(this);
        btnMinimalMediaBroth.addSelectionListener(this);

        Label label_4 = new Label(composite_3, SWT.NONE);

        Label lblMutantgenesAnd = new Label(composite_3, SWT.NONE);
        lblMutantgenesAnd.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblMutantgenesAnd.setText("Mutant (Genes and sRNA)");

        Composite composite_6 = new Composite(composite_3, SWT.NONE);
        composite_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_6.setLayout(new GridLayout(1, false));

        btnAllMutant = new Button(composite_6, SWT.RADIO);
        btnAllMutant.setSelection(true);
        btnAllMutant.setText("All");

        btnNoneMutant = new Button(composite_6, SWT.RADIO);
        btnNoneMutant.setText("None");

        btnChooseOneMutant = new Button(composite_6, SWT.RADIO);
        btnChooseOneMutant.setText("Choose One");

        comboMutant = new Combo(composite_6, SWT.NONE);
        GridData gd_comboMutant = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gd_comboMutant.widthHint = 200;
        comboMutant.setLayoutData(gd_comboMutant);
        comboMutant.addSelectionListener(this);

        btnAllMutant.addSelectionListener(this);
        btnNoneMutant.addSelectionListener(this);
        btnChooseOneMutant.addSelectionListener(this);

        Label label_5 = new Label(composite_3, SWT.NONE);

        Label lblGrowthPhases = new Label(composite_3, SWT.NONE);
        lblGrowthPhases.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowthPhases.setText("Growth phases");

        Composite composite_2 = new Composite(composite_3, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        btnLagPhase = new Button(composite_2, SWT.CHECK);
        btnLagPhase.setText("Lag");

        btnExponential = new Button(composite_2, SWT.CHECK);
        btnExponential.setText("Exponential");

        btnStationnary = new Button(composite_2, SWT.CHECK);
        btnStationnary.setText("Stationnary");

        btnSurvival = new Button(composite_2, SWT.CHECK);
        btnSurvival.setText("Survival");

        btnDeath = new Button(composite_2, SWT.CHECK);
        btnDeath.setText("Death");

        btnRegrowth = new Button(composite_2, SWT.CHECK);
        btnRegrowth.setText("Regrowth");

        btnLagPhase.addSelectionListener(this);
        btnExponential.addSelectionListener(this);
        btnStationnary.addSelectionListener(this);
        btnSurvival.addSelectionListener(this);
        btnDeath.addSelectionListener(this);
        btnRegrowth.addSelectionListener(this);

        Label label_3 = new Label(composite_3, SWT.NONE);

        scrolledComposite.setContent(composite_3);
        scrolledComposite.setMinSize(composite_3.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        tableBioCondition = new TableViewer(compositeGeneral, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        Table table = tableBioCondition.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        tableBioCondition.setLabelProvider(new TableLabelProvider());

        {
            Composite composite_1 = new Composite(compositeGeneral, SWT.BORDER);
            composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            composite_1.setLayout(new GridLayout(8, false));

            Label label_9 = new Label(composite_1, SWT.NONE);
            label_9.setImage(ResourceManager.getPluginImage("bacnet", "icons/geneexpr.bmp"));
            Label lblGeneExpressionArray = new Label(composite_1, SWT.NONE);
            lblGeneExpressionArray.setText("Gene expression array");

            Label lblNewLabel = new Label(composite_1, SWT.NONE);
            lblNewLabel.setImage(ResourceManager.getPluginImage("bacnet", "icons/tiling.bmp"));
            Label lblTilingArrayAvailable = new Label(composite_1, SWT.NONE);
            lblTilingArrayAvailable.setText("Tiling array");

            Label lblNewLabel_1 = new Label(composite_1, SWT.NONE);
            lblNewLabel_1.setImage(ResourceManager.getPluginImage("bacnet", "icons/tss.bmp"));
            Label lblTssAvailable = new Label(composite_1, SWT.NONE);
            lblTssAvailable.setText("TSS");

            Label label = new Label(composite_1, SWT.NONE);
            label.setImage(ResourceManager.getPluginImage("bacnet", "icons/rnaSeq.bmp"));

            Label lblRnaseq = new Label(composite_1, SWT.NONE);
            lblRnaseq.setText("RNASeq");
        }

        setData();
        updateInfo();
        updateBioConditionList();
        updateBioConditionTable();
        return area;
    }

    /**
     * Set all starting variables
     */
    private void setData() {
        /*
         * Add genomes
         */
        comboGenome.add(track.getGenomeName());
        comboGenome.select(0);
        /*
         * Load Table
         */
        bioCondsArray = TabDelimitedTableReader.read(Database.getInstance().getTranscriptomesArrayPath());
        bioConds = TabDelimitedTableReader.readList(Database.getInstance().getTranscriptomesArrayPath(), true, true);
        bioCondsToDisplay =
                TabDelimitedTableReader.readList(Database.getInstance().getTranscriptomesArrayPath(), true, true);
        if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
            ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<String[]>();
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Reference")];
                if (info.contains("Unpublished (Cossart lab)")) {
                    //
                } else {
                    bioCondsToDisplayTemp.add(row);
                }
            }
            bioCondsToDisplay = bioCondsToDisplayTemp;
        }
        bioConds.remove(0);
        bioCondsToDisplay.remove(0);
        updateBioConditionTable();
        /*
         * Fill combos
         */
        TreeSet<String> mutantSet = new TreeSet<>();
        for (String[] rows : bioConds) {
            String mutants = rows[ArrayUtils.findColumn(bioCondsArray, "Mutant")];
            for (String mutant : mutants.split(",")) {
                if (!mutant.equals(""))
                    mutantSet.add(mutant);
            }
        }
        for (String mutant : mutantSet) {
            comboMutant.add(mutant);
        }
        comboMutant.select(0);
    }

    /**
     * From finalarrayBioCondition update the text for every Checkbox
     */
    private void updateInfo() {
        /*
         * For each category count the number of data available
         */
        int geneExpr = 0;
        int tiling = 0;
        int tss = 0;
        int riboseq = 0;
        int termseq = 0;
        int rnaSeq = 0;
        int caco2 = 0;
        int humanblood = 0;
        int mouseintest = 0;
        int mousemacro = 0;
        int mousespleen = 0;
        int porcine = 0;
        int turkey = 0;
        int bhi = 0;
        int trypticsoy = 0;
        int lb = 0;
        int mm = 0;
        int lag = 0;
        int expo = 0;
        int stat = 0;
        int surival = 0;
        int death = 0;
        int regrowth = 0;
        int unpublished = 0;
        for (String[] row : bioCondsToDisplay) {
            // Data type
            String dataType = row[ArrayUtils.findColumn(bioCondsArray, "Type")];
            if (dataType.contains("GeneExpression"))
                geneExpr++;
            if (dataType.contains("Tiling"))
                tiling++;
            if (dataType.contains("TSS"))
                tss++;
            if (dataType.contains("RiboSeq"))
                riboseq++;
            if (dataType.contains("TermSeq"))
                termseq++;
            if (dataType.contains("RNASeq"))
                rnaSeq++;

            // Unpublished data
            String reference = row[ArrayUtils.findColumn(bioCondsArray, "Reference")];
            if (reference.contains("Unpublished"))
                unpublished++;

            // intracellular type
            String intracellular = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
            if (intracellular.contains("Caco-2 cells"))
                caco2++;
            if (intracellular.contains("Human blood cells"))
                humanblood++;
            if (intracellular.contains("Mouse intestine cells"))
                mouseintest++;
            if (intracellular.contains("Mouse macrophages cells"))
                mousemacro++;
            if (intracellular.contains("Mouse spleen cells"))
                mousespleen++;
            if (intracellular.contains("Porcine Bile cells"))
                porcine++;
            if (intracellular.contains("Turkey Deli Meat cells"))
                turkey++;

            // Broth type
            String broth = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
            if (broth.contains("BHI"))
                bhi++;
            if (broth.contains("Tryptic Soy Broth"))
                trypticsoy++;
            if (broth.contains("LB Broth"))
                lb++;
            if (broth.contains("Minimal Media Broth"))
                mm++;

            // growth type
            String growth = row[ArrayUtils.findColumn(bioCondsArray, "Growth")];
            if (growth.contains("Lag"))
                lag++;
            if (growth.contains("Exp phase"))
                expo++;
            if (growth.contains("Stationary"))
                stat++;
            if (growth.contains("survival"))
                surival++;
            if (growth.contains("Death"))
                death++;
            if (growth.contains("Regrowth"))
                regrowth++;

        }

        /*
         * Update checkbox text with the number of data available for each category
         */
        btnGeneExpression.setText("Gene Expression (" + geneExpr + ")");
        btnTiling.setText("Tiling (" + tiling + ")");
        btnTss.setText("TSS (" + tss + ")");
        btnRnaseq.setText("RNASeq (" + rnaSeq + ")");
        if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
            btnUnpublished.dispose();
        } else {
            btnUnpublished.setText("Unpublished (" + unpublished + ")");
        }
        btnCacoCells.setText("Caco-2 cells (" + caco2 + ")");
        btnHumanBloodCells.setText("Human blood cells (" + humanblood + ")");
        btnMouseIntestineCells.setText("Mouse intestine cells (" + mouseintest + ")");
        btnMouseMacrophagesCells.setText("Mouse macrophages cells (" + mousemacro + ")");
        btnMouseSpleenCells.setText("Mouse spleen cells (" + mousespleen + ")");
        btnPorcineBileCells.setText("Porcine Bile cells (" + porcine + ")");
        btnTurkeyDeliMeat.setText("Turkey Deli Meat cells (" + turkey + ")");
        btnBhiBroth.setText("BHI Broth (" + bhi + ")");
        btnTrypticSoyBroth.setText("Tryptic Soy Broth (" + trypticsoy + ")");
        btnLbBroth.setText("LB Broth (" + lb + ")");
        btnMinimalMediaBroth.setText("Minimal Media Broth (" + mm + ")");
        btnLagPhase.setText("Lag (" + lag + ")");
        btnExponential.setText("Exponential (" + expo + ")");
        btnStationnary.setText("Stationnary (" + stat + ")");
        btnSurvival.setText("Survival (" + surival + ")");
        btnDeath.setText("Death (" + death + ")");
        btnRegrowth.setText("Regrowth (" + regrowth + ")");

        /*
         * Disable checkbox if no correspondign data is present
         */
        // if(geneExpr==0) btnGeneExpression.setEnabled(false);
        // else btnGeneExpression.setEnabled(true);
        // if(tiling==0) btnTiling.setEnabled(false);
        // else btnTiling.setEnabled(true);
        // if(tss==0) btnTss.setEnabled(false);
        // else btnTss.setEnabled(true);
        // if(rnaSeq==0) btnRnaseq.setEnabled(false);
        // else btnRnaseq.setEnabled(true);
        // if(caco2==0) btnCacoCells.setEnabled(false);
        // else btnCacoCells.setEnabled(true);
        // if(humanblood==0) btnHumanBloodCells.setEnabled(false);
        // else btnHumanBloodCells.setEnabled(true);
        // if(mouseintest==0) btnMouseIntestineCells.setEnabled(false);
        // else btnMouseIntestineCells.setEnabled(true);
        // if(mousemacro==0) btnMouseMacrophagesCells.setEnabled(false);
        // else btnMouseMacrophagesCells.setEnabled(true);
        // if(mousespleen==0) btnMouseSpleenCells.setEnabled(false);
        // else btnMouseSpleenCells.setEnabled(true);
        // if(porcine==0) btnPorcineBileCells.setEnabled(false);
        // else btnPorcineBileCells.setEnabled(true);
        // if(turkey==0) btnTurkeyDeliMeat.setEnabled(false);
        // else btnTurkeyDeliMeat.setEnabled(true);
        // if(bhi==0) btnBhiBroth.setEnabled(false);
        // else btnBhiBroth.setEnabled(true);
        // if(trypticsoy==0) btnTrypticSoyBroth.setEnabled(false);
        // else btnTrypticSoyBroth.setEnabled(true);
        // if(lb==0) btnLbBroth.setEnabled(false);
        // else btnLbBroth.setEnabled(true);
        // if(mm==0) btnMinimalMediaBroth.setEnabled(false);
        // else btnMinimalMediaBroth.setEnabled(true);
        // if(lag==0) btnLagPhase.setEnabled(false);
        // else btnLagPhase.setEnabled(true);
        // if(expo==0) btnExponential.setEnabled(false);
        // else btnExponential.setEnabled(true);
        // if(stat==0) btnStationnary.setEnabled(false);
        // else btnStationnary.setEnabled(true);
        // if(surival==0) btnSurvival.setEnabled(false);
        // else btnSurvival.setEnabled(true);
        // if(death==0) btnDeath.setEnabled(false);
        // else btnDeath.setEnabled(true);
        // if(regrowth==0) btnRegrowth.setEnabled(false);
        // else btnRegrowth.setEnabled(true);

    }

    private void updateBioConditionList() {
        bioCondsToDisplay.clear();
        /*
         * Update with search text
         */
        if (!textSearch.getText().equals("")) {
            for (String[] row : bioConds) {
                boolean found = false;
                for (String cell : row) {
                    if (cell.contains(textSearch.getText())) {
                        found = true;
                    }
                }
                if (found) {
                    bioCondsToDisplay.add(row);
                }
            }
        } else {
            for (String[] row : bioConds) {
                bioCondsToDisplay.add(row);
            }
        }

        /*
         * Update with genome information
         */
        String genome = comboGenome.getText().replaceFirst("L. mono.", "Listeria monocytogenes");
        System.out.println("genome selected: " + genome);
        if (!genome.equals("All")) {
            ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<>();
            for (String[] row : bioCondsToDisplay) {
                String genomeRow = row[ArrayUtils.findColumn(bioCondsArray, "Strain array")];
                if (genomeRow.equals(genome)) {
                    bioCondsToDisplayTemp.add(row);
                }
            }
            bioCondsToDisplay.clear();
            for (String[] row : bioCondsToDisplayTemp)
                bioCondsToDisplay.add(row);
        }

        /*
         * Update with Data type
         */
        ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<>();
        boolean selected = false;
        if (btnGeneExpression.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Type")];
                if (info.contains("GeneExpression")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTiling.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Type")];
                if (info.contains("Tiling")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTss.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Type")];
                if (info.contains("TSS")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnRnaseq.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Type")];
                if (info.contains("RNASeq")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }

        if (selected) {
            bioCondsToDisplay.clear();
            for (String[] row : bioCondsToDisplayTemp)
                bioCondsToDisplay.add(row);
        }

        /*
         * Reference section
         */
        if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
            selected = false;
            if (btnUnpublished.getSelection()) {
                selected = true;
                for (String[] row : bioCondsToDisplay) {
                    String info = row[ArrayUtils.findColumn(bioCondsArray, "Reference")];
                    if (info.contains("Unpublished (Cossart lab)")) {
                        if (!bioCondsToDisplayTemp.contains(row))
                            bioCondsToDisplayTemp.add(row);
                    }
                }

            }
            if (selected) {
                bioCondsToDisplay.clear();
                for (String[] row : bioCondsToDisplayTemp)
                    bioCondsToDisplay.add(row);
            }
        }
        /*
         * Update with intracellular Media type
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        if (btnCacoCells.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Caco-2 cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnHumanBloodCells.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Human blood cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMouseIntestineCells.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Mouse intestine cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMouseMacrophagesCells.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Mouse macrophages cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMouseSpleenCells.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Mouse spleen cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnPorcineBileCells.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Porcine Bile cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTurkeyDeliMeat.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Turkey Deli Meat cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (selected) {
            bioCondsToDisplay.clear();
            for (String[] row : bioCondsToDisplayTemp)
                bioCondsToDisplay.add(row);
        }

        /*
         * Update with Broth type
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        if (btnBhiBroth.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("BHI")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTrypticSoyBroth.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Tryptic Soy Broth")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnLbBroth.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("LB Broth")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMinimalMediaBroth.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Minimal Media Broth")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (selected) {
            bioCondsToDisplay.clear();
            for (String[] row : bioCondsToDisplayTemp)
                bioCondsToDisplay.add(row);
        }

        /*
         * Update with Mutant
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        if (btnNoneMutant.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Mutant")];
                if (info.equals("")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        } else if (btnChooseOneMutant.getSelection()) {
            selected = true;
            String mutantSelected = comboMutant.getItem(comboMutant.getSelectionIndex());
            if (mutantSelected.indexOf('(') != -1)
                mutantSelected = mutantSelected.substring(0, mutantSelected.indexOf('(')).trim();
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Mutant")];
                if (info.contains(mutantSelected)) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (selected) {
            bioCondsToDisplay.clear();
            for (String[] row : bioCondsToDisplayTemp)
                bioCondsToDisplay.add(row);
        }

        /*
         * Update with Growth type
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        if (btnLagPhase.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Growth")];
                if (info.contains("Lag")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnExponential.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Growth")];
                if (info.contains("Exp phase")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnStationnary.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Growth")];
                if (info.contains("Stationary")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnSurvival.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Growth")];
                if (info.contains("survival")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnDeath.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Growth")];
                if (info.contains("Death")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnRegrowth.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Growth")];
                if (info.contains("Regrowth")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (selected) {
            bioCondsToDisplay.clear();
            for (String[] row : bioCondsToDisplayTemp)
                bioCondsToDisplay.add(row);
        }

        if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
            bioCondsToDisplayTemp = new ArrayList<String[]>();
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Reference")];
                if (info.contains("Unpublished (Cossart lab)")) {
                    //
                } else {
                    bioCondsToDisplayTemp.add(row);
                }
            }
            bioCondsToDisplay = bioCondsToDisplayTemp;
        }

    }

    /**
     * Update Table for Biological Condition
     * 
     * @param bioConds
     */
    private void updateBioConditionTable() {

        setColumnNames();
        for (TableColumn col : tableBioCondition.getTable().getColumns()) {
            col.dispose();
        }
        tableBioCondition.setContentProvider(new ArrayContentProvider());

        createColumns();

        tableBioCondition.getTable().setHeaderVisible(true);
        tableBioCondition.getTable().setLinesVisible(true);
        /*
         * Remove first row
         */
        tableBioCondition.setInput(bioCondsToDisplay);
        comparatorBioCondition = new BioConditionComparator(columnNames);
        tableBioCondition.setComparator(comparatorBioCondition);
        for (int i = 0; i < tableBioCondition.getTable().getColumnCount(); i++) {
            tableBioCondition.getTable().getColumn(i).pack();
        }
    }

    /**
     * Add the name of the columns
     */
    private void setColumnNames() {
        columnNames = new ArrayList<>();
        for (String title : bioCondsArray[0]) {
            columnNames.add(title);
        }
    }

    private TableViewerColumn createTableViewerColumn(String title, final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(tableBioCondition, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setResizable(true);
        column.setMoveable(true);
        column.addSelectionListener(getSelectionAdapter(column, colNumber));
        return viewerColumn;
    }

    private void createColumns() {
        for (int i = 0; i < bioConds.get(0).length; i++) {
            final int k = i;
            TableViewerColumn col2 = createTableViewerColumn(bioCondsArray[0][i], i);
            col2.setLabelProvider(new CellLabelProvider() {
                private static final long serialVersionUID = -2853434015639244753L;

                @Override
                public void update(ViewerCell cell) {
                    // TODO Auto-generated method stub

                }

            });
            col2.setLabelProvider(new ColumnLabelProvider() {
                /**
                 * 
                 */
                private static final long serialVersionUID = -55186278705378176L;

                @Override
                public String getText(Object element) {
                    String[] bioCond = (String[]) element;
                    if (k != 1) {
                        return bioCond[k];
                    } else {
                        return "";
                    }
                }

                @Override
                public Image getImage(Object element) {
                    String[] bioCond = (String[]) element;
                    if (k == 1) {
                        String typeDataContained = bioCond[k];
                        if (typeDataContained.contains(TypeData.TSS + "")
                                && typeDataContained.contains(TypeData.GeneExpr + "")) {
                            return imageTSSTilingGeneExpr;
                        } else if (typeDataContained.contains(TypeData.TSS + "")) {
                            return imageTSS;
                        } else if (typeDataContained.contains(TypeData.RNASeq + "")) {
                            return imageRNASeq;
                        } else if (typeDataContained.contains(TypeData.Tiling + "")) {
                            return imageTilingGeneExpr;
                        } else {
                            return imageGeneExpr;
                        }
                    } else
                        return null;
                }

            });
            col2.getColumn().pack();

            // }

        }

    }

    private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            /**
             * 
             */
            private static final long serialVersionUID = -6997325110066606691L;

            @Override
            public void widgetSelected(SelectionEvent e) {
                comparatorBioCondition.setColumn(index);
                int dir = comparatorBioCondition.getDirection();
                tableBioCondition.getTable().setSortDirection(dir);
                tableBioCondition.getTable().setSortColumn(column);
                tableBioCondition.refresh();
            }
        };
        return selectionAdapter;
    }

    @Override
    public void okPressed() {
        try {
            for (Item item : tableBioCondition.getTable().getSelection()) {
                String bioCondName = item.getText();
                track.getDatas().addBioCondition(bioCondName);
            }

            /*
             * Init data
             */
            track.getDatas().setDataColors();
            track.getDatas().setDataSizes();

            this.close();
        } catch (Exception e1) {
            this.close();
        }
    }

    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Ok", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(900, 700);
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        updateBioConditionList();
        updateBioConditionTable();
        updateInfo();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }
}
