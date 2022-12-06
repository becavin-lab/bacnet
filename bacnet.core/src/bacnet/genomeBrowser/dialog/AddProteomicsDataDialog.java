package bacnet.genomeBrowser.dialog;

import java.util.ArrayList;
import java.util.HashMap;
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
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.genomeBrowser.core.Track;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.table.core.BioConditionComparator;
import bacnet.utils.ArrayUtils;

public class AddProteomicsDataDialog extends TitleAreaDialog implements SelectionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -8202496409867913492L;

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        /**
         * 
         */
        private static final long serialVersionUID = -8364757130025429726L;

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            return element.toString();
        }
    }

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
    @SuppressWarnings("unused") // loaded after
    private final Image imageRNASeq;
    @SuppressWarnings("unused") // loaded after
    private final Image imageTSSTilingGeneExpr;
    @SuppressWarnings("unused") // loaded after
    private final Image imageTilingGeneExpr;
    private final Image imageGeneExpr;
    private Text textSearch;
    private Combo comboGenome;
    private TableViewer tableBioCondition;
    private Label lblTssAvailable;
    private Label lblTSSImage;
    
    private Button btnPellet;
    private Button btnCytoplasm;
    private Button btnSupernatant;
    private Button btnMembrane;
    private Button btnBCSBroth;
    private Button btnMinimalMediaBroth;
    private Button btnDMEM;
    private Button btnHumanPlasma;
    private Button btnExponential;
    private Button btnStationnary;
    private Button btnPeriplasm;
    private Button btnLbBroth;

    private Combo comboMutant;
    private Button btnChooseOneMutant;
    private Button btnNoneMutant;
    private Button btnAllMutant;

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public AddProteomicsDataDialog(Shell parentShell, Track track) {
        super(parentShell);
        setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE);
        this.track = track;

        imageTSS = ResourceManager.getPluginImage("bacnet.core", "icons/tss.bmp");
        imageRNASeq = ResourceManager.getPluginImage("bacnet.core", "icons/rnaSeq.bmp");
        imageTSSTilingGeneExpr = ResourceManager.getPluginImage("bacnet.core", "icons/tssTilingGeneExpr.bmp");
        imageTilingGeneExpr = ResourceManager.getPluginImage("bacnet.core", "icons/TilingGeneExpr.bmp");
        imageGeneExpr = ResourceManager.getPluginImage("bacnet.core", "icons/GeneExpr.bmp");
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @SuppressWarnings("unused")
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage("The list below shows you the different type of proteomics data available for display");
        setTitle("Add Proteomics data");
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
        textSearch.addKeyListener(new org.eclipse.swt.events.KeyListener() {

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
        {
            Label lblNewLabel_2 = new Label(composite_3, SWT.NONE);
            lblNewLabel_2.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
            lblNewLabel_2.setText("Localization");
        }
        {
            Composite composite = new Composite(composite_3, SWT.NONE);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            composite.setLayout(new GridLayout(1, false));

            btnPellet = new Button(composite, SWT.CHECK);
            btnPellet.setText("Whole Cell");

            btnCytoplasm = new Button(composite, SWT.CHECK);
            btnCytoplasm.setText("Cytoplasm");

            btnPeriplasm = new Button(composite, SWT.CHECK);
            btnPeriplasm.setText("Periplasm");

            btnMembrane = new Button(composite, SWT.CHECK);
            btnMembrane.setText("Membrane");

            btnSupernatant = new Button(composite, SWT.CHECK);
            btnSupernatant.setText("Supernatant");

            btnCytoplasm.addSelectionListener(this);
            btnPeriplasm.addSelectionListener(this);
            btnMembrane.addSelectionListener(this);
            btnSupernatant.addSelectionListener(this);

        }

        new Label(composite_3, SWT.NONE);

        Label lblGrowth = new Label(composite_3, SWT.NONE);
        lblGrowth.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowth.setText("Growth Medium");

        Composite composite_1_1 = new Composite(composite_3, SWT.NONE);
        composite_1_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_1_1.setLayout(new GridLayout(1, false));

        btnBCSBroth = new Button(composite_1_1, SWT.CHECK);
        btnBCSBroth.setText("BCS");
        btnBCSBroth.addSelectionListener(this);

        btnLbBroth = new Button(composite_1_1, SWT.CHECK);
        btnLbBroth.setText("LB Broth");
        btnLbBroth.addSelectionListener(this);

        btnMinimalMediaBroth = new Button(composite_1_1, SWT.CHECK);
        btnMinimalMediaBroth.setText("Chemically Defined Media");
        btnMinimalMediaBroth.addSelectionListener(this);

        btnHumanPlasma = new Button(composite_1_1, SWT.CHECK);
        btnHumanPlasma.setText("Human Plasma");
        btnHumanPlasma.addSelectionListener(this);

        btnDMEM = new Button(composite_1_1, SWT.CHECK);
        btnDMEM.setText("DMEM");
        btnDMEM.addSelectionListener(this);

        Label label_5 = new Label(composite_3, SWT.NONE);

        Label lblGrowthPhases = new Label(composite_3, SWT.NONE);
        lblGrowthPhases.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowthPhases.setText("Growth phases");

        Composite composite_2 = new Composite(composite_3, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        btnExponential = new Button(composite_2, SWT.CHECK);
        btnExponential.setText("Logarithmic");
        btnExponential.addSelectionListener(this);

        btnStationnary = new Button(composite_2, SWT.CHECK);
        btnStationnary.setText("Stationnary");
        btnStationnary.addSelectionListener(this);

        
        Label label_3 = new Label(composite_3, SWT.NONE);

        scrolledComposite.setContent(composite_3);
        scrolledComposite.setMinSize(composite_3.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        new Label(composite_3, SWT.NONE);

        scrolledComposite.setContent(composite_3);
        scrolledComposite.setMinSize(composite_3.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        tableBioCondition = new TableViewer(compositeGeneral, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        Table table = tableBioCondition.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        tableBioCondition.setLabelProvider(new TableLabelProvider());
/*
        {
            Composite composite_1 = new Composite(compositeGeneral, SWT.BORDER);
            composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            composite_1.setLayout(new GridLayout(4, false));

            Label lblNewLabel = new Label(composite_1, SWT.NONE);
            lblNewLabel.setImage(imageGeneExpr);
            Label lblTilingArrayAvailable = new Label(composite_1, SWT.NONE);
            lblTilingArrayAvailable.setText("Mass Spectometry");
            lblTSSImage = new Label(composite_1, SWT.NONE);
            lblTSSImage.setImage(imageTSS);
            lblTssAvailable = new Label(composite_1, SWT.NONE);
            lblTssAvailable.setText("TIS");

        }
        */
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
        bioCondsArray = TabDelimitedTableReader.read(Database.getInstance().getProteomesArrayPath());
        bioConds = TabDelimitedTableReader.readList(Database.getInstance().getProteomesArrayPath(), true, true);
        bioCondsToDisplay =
                TabDelimitedTableReader.readList(Database.getInstance().getProteomesArrayPath(), true, true);
        if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
            ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<String[]>();
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Bibliographical reference")];
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
            	 if (!mutant.equals("")) {
                     mutantSet.add(mutant);

            	 }
            	/* vestige de Listeriomics
                if (!mutant.equals("")) {
                    mutant = mutant.trim();
                    Genome genome = Genome.loadEgdeGenome();
                    Sequence seq = genome.getElement(mutant);
                    if (seq != null && seq instanceof Gene) {
                        Gene gene = (Gene) seq;
                        String text = gene.getName();
                        if (!gene.getGeneName().equals(""))
                            text += " (" + gene.getGeneName() + ")";
                        mutantSet.add(text);
                    } else {
                        mutantSet.add(mutant.trim());
                    }
                }*/
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
    	  int pell = 0;
    	  int cyto = 0;
          int peri = 0;
          int memb = 0;
          int sup = 0;
          int humanblood = 0;
          int dmem = 0;
          int BCS = 0;
          int lb = 0;
          int mm = 0;
          int expo = 0;
          int stat = 0;

        for (String[] row : bioCondsToDisplay) {
            // Data type
            String dataType = row[ArrayUtils.findColumn(bioCondsArray, "Localization")];
            if (dataType.contains("Whole Cell") || dataType.contains("Bacterial Pellet"))
                pell++;
            if (dataType.contains("Cytoplasm"))
                cyto++;
            if (dataType.contains("Periplasm"))
                peri++;
            if (dataType.contains("Membrane"))
                memb++;
            if (dataType.contains("Supernatant"))
                sup++;
/*
            // Unpublished data
            String reference = row[ArrayUtils.findColumn(bioCondsArray, "Bibliographical reference")];
            if (reference.contains("Unpublished"))
                unpublished++;
*/

            // Broth type
            String broth = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
            if (broth.contains("BCS"))
                BCS++;
            if (broth.contains("LB"))
                lb++;
            if (broth.contains("Minimal Media")||broth.contains("M9")||broth.contains("TMH")||broth.contains("PMH"))
                mm++;
            if (broth.contains("Human Plasma"))
                humanblood++;
            if (broth.contains("DMEM"))
                dmem++;

            // growth type
            String growth = row[ArrayUtils.findColumn(bioCondsArray, "Growth")];
            if (growth.contains("Logarithmic"))
                expo++;
            if (growth.contains("Stationary"))
                stat++;

        }

        /*
         * Update checkbox text with the number of data available for each category
         */
        btnPellet.setText("Whole Cell (" + pell + ")");
        btnCytoplasm.setText("Cytoplasm (" + cyto + ")");
        btnPeriplasm.setText("Periplasm (" + peri + ")");
        btnMembrane.setText("Membrane (" + memb + ")");
        btnSupernatant.setText("Supernatant (" + sup + ")");
        btnHumanPlasma.setText("Human Plasma (" + humanblood + ")");
        btnDMEM.setText("DMEM (" + dmem + ")");
        btnBCSBroth.setText("BCS (" + BCS + ")");
        btnLbBroth.setText("LB (" + lb + ")");
        btnMinimalMediaBroth.setText("Chemically Defined Media (" + mm + ")");
        btnExponential.setText("Logarithmic Phase (" + expo + ")");
        btnStationnary.setText("Stationnary Phase (" + stat + ")");
        /*
        if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
            btnUnpublished.dispose();
            lblTssAvailable.dispose();
            lblTSSImage.dispose();
        } else {
            btnUnpublished.setText("Unpublished (" + unpublished + ")");
        }*/

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
        if (!genome.equals("All")) {
            ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<>();
            for (String[] row : bioCondsToDisplay) {
                String genomeRow = row[ArrayUtils.findColumn(bioCondsArray, "Reference strain")];
                if (genomeRow.equals(genome)) {
                    bioCondsToDisplayTemp.add(row);
                }
            }
            bioCondsToDisplay.clear();
            for (String[] row : bioCondsToDisplayTemp)
                bioCondsToDisplay.add(row);
        }

        /*
         * Update with Mutant
         */
        ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<>();
        boolean selected = false;

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
         * Update with Localization type
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        if (btnPellet.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Localization")];
                if (info.contains("Whole Cell") || info.contains("Bacterial Pellet")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnCytoplasm.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Localization")];
                if (info.contains("Cytoplasm")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnPeriplasm.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Localization")];
                if (info.contains("Periplasm")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMembrane.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Localization")];
                if (info.contains("Membrane")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnSupernatant.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Localization")];
                if (info.contains("Supernatant")) {
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
        /*
        if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
            selected = false;
            if (btnUnpublished.getSelection()) {
                selected = true;
                for (String[] row : bioCondsToDisplay) {
                    String info = row[ArrayUtils.findColumn(bioCondsArray, "Bibliographical reference")];
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
*/
        /*
         * Update with media type
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
        if (btnHumanPlasma.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Human Plasma")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnDMEM.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("DMEM")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnBCSBroth.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("BCS")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnLbBroth.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("LB")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMinimalMediaBroth.getSelection()) {
            selected = true;
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Media")];
                if (info.contains("Minimal Media")||info.contains("M9")||info.contains("TMH")||info.contains("PMH")) {
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
        if (selected) {
            bioCondsToDisplay.clear();
            for (String[] row : bioCondsToDisplayTemp)
                bioCondsToDisplay.add(row);
        }

        if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
            bioCondsToDisplayTemp = new ArrayList<String[]>();
            for (String[] row : bioCondsToDisplay) {
                String info = row[ArrayUtils.findColumn(bioCondsArray, "Bibliographical reference")];
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
                    if (k != columnNames.indexOf("Type")) {
                        return bioCond[k];
                    } else {
                        return "";
                    }
                }

                @Override
                public Image getImage(Object element) {
                    String[] bioCond = (String[]) element;
                    if (k == columnNames.indexOf("Type")) {
                        String typeDataContained = bioCond[k];
                        if (typeDataContained.contains(TypeData.NTerm + "")) {
                            return imageTSS;
                        } else if (!typeDataContained.contains(TypeData.Proteome + "")) {
                            return imageGeneExpr;
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

    /**
     * Retreive the name of the BioCond selected in tableBioCondition
     * 
     * @return
     */
    @SuppressWarnings("unused")
    private HashMap<String, ArrayList<String>> getSelectedBioConditions() {
        HashMap<String, ArrayList<String>> genomeToBioConds = new HashMap<>();
        for (int index : tableBioCondition.getTable().getSelectionIndices()) {
            String bioCondName = tableBioCondition.getTable().getItem(index).getText(columnNames.indexOf("Data Name"));
            BioCondition bioCondition = BioCondition.getBioCondition(bioCondName);
            String genome = bioCondition.getGenomeName();
            if (genomeToBioConds.containsKey(genome)) {
                genomeToBioConds.get(genome).add(bioCondName);
            } else {
                ArrayList<String> bioCondNames = new ArrayList<>();
                bioCondNames.add(bioCondName);
                genomeToBioConds.put(genome, bioCondNames);
            }
        }
        return genomeToBioConds;
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
        return new Point(990, 899);
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
