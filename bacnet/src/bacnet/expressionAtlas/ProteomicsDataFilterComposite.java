package bacnet.expressionAtlas;

import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import bacnet.Database;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Genome;
import bacnet.raprcp.NavigationManagement;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.ArrayUtils;

public class ProteomicsDataFilterComposite extends org.eclipse.swt.widgets.Composite implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -4827398812522159453L;
    
    private Button btnCytoplasm;
    private Button btnSecretome;
    private Button btnMembrane;
    private Button btnBhiBroth;
    private Button btnMinimalMediaBroth;
    private Button btnMouseMacrophagesCells;
    private Button btnHumanBloodCells;
    private Button btnExponential;
    private Button btnStationnary;
    private Button btnCellWall;
    private Button btnLbBroth;
    private Text textSearch;
    private Combo comboGenome;
    private Button btnUnpublished;
    private ProteomicsView view;
    private ArrayList<Button> allButtons = new ArrayList<>();

    public ProteomicsDataFilterComposite(org.eclipse.swt.widgets.Composite parent, int style, ProteomicsView view) {
        super(parent, style);
        this.setLayout(new GridLayout(1, false));
        this.view = view;

        Label lblSearch = new Label(this, SWT.NONE);
        lblSearch.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblSearch.setText("Search");

        textSearch = new Text(this, SWT.BORDER);
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
                    // System.out.println("Search for "+textSearch.getText());
                    updateView();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Label label_6 = new Label(this, SWT.NONE);
        {
            Label lblGenome = new Label(this, SWT.NONE);
            lblGenome.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
            lblGenome.setText("Genome");
        }
        {
            comboGenome = new Combo(this, SWT.NONE);
            GridData gd_comboGenome = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
            gd_comboGenome.widthHint = 270;
            comboGenome.setLayoutData(gd_comboGenome);
            comboGenome.addSelectionListener(this);
        }
        {
            Label label_1 = new Label(this, SWT.NONE);
        }
        {
            Label lblNewLabel_2 = new Label(this, SWT.NONE);
            lblNewLabel_2.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
            lblNewLabel_2.setText("Localization");
        }
        {
            Composite composite = new Composite(this, SWT.NONE);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            composite.setLayout(new GridLayout(1, false));

            btnCytoplasm = new Button(composite, SWT.CHECK);
            btnCytoplasm.setText("Cytoplasm");

            btnCellWall = new Button(composite, SWT.CHECK);
            btnCellWall.setText("Cell Wall");

            btnMembrane = new Button(composite, SWT.CHECK);
            btnMembrane.setText("Membrane");

            btnSecretome = new Button(composite, SWT.CHECK);
            btnSecretome.setText("Secretome");

            btnUnpublished = new Button(composite, SWT.CHECK);
            btnUnpublished.setText("Unpublished");

            btnCytoplasm.addSelectionListener(this);
            btnCellWall.addSelectionListener(this);
            btnMembrane.addSelectionListener(this);
            btnSecretome.addSelectionListener(this);

        }

        Label label_2 = new Label(this, SWT.NONE);

        Label lblGrowth = new Label(this, SWT.NONE);
        lblGrowth.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowth.setText("Growth Medium");

        Composite composite_1_1 = new Composite(this, SWT.NONE);
        composite_1_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_1_1.setLayout(new GridLayout(1, false));

        btnBhiBroth = new Button(composite_1_1, SWT.CHECK);
        btnBhiBroth.setText("BHI Broth");

        btnBhiBroth.addSelectionListener(this);

        btnLbBroth = new Button(composite_1_1, SWT.CHECK);
        btnLbBroth.setText("LB Broth");
        btnLbBroth.addSelectionListener(this);

        btnMinimalMediaBroth = new Button(composite_1_1, SWT.CHECK);
        btnMinimalMediaBroth.setText("Minimal Media Broth");
        btnMinimalMediaBroth.addSelectionListener(this);

        btnHumanBloodCells = new Button(composite_1_1, SWT.CHECK);
        btnHumanBloodCells.setText("Human blood cells");

        btnMouseMacrophagesCells = new Button(composite_1_1, SWT.CHECK);
        btnMouseMacrophagesCells.setText("Mouse macrophages cells");
        btnHumanBloodCells.addSelectionListener(this);
        btnMouseMacrophagesCells.addSelectionListener(this);

        new Label(this, SWT.NONE);

        Label lblGrowthPhases = new Label(this, SWT.NONE);
        lblGrowthPhases.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowthPhases.setText("Growth phases");

        Composite composite_2 = new Composite(this, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        btnExponential = new Button(composite_2, SWT.CHECK);
        btnExponential.setText("Exponential");

        btnStationnary = new Button(composite_2, SWT.CHECK);
        btnStationnary.setText("Stationnary");
        btnExponential.addSelectionListener(this);
        btnStationnary.addSelectionListener(this);

        new Label(this, SWT.NONE);

        allButtons.add(btnCytoplasm);
        allButtons.add(btnCellWall);
        allButtons.add(btnMembrane);
        allButtons.add(btnSecretome);
        allButtons.add(btnHumanBloodCells);
        allButtons.add(btnMouseMacrophagesCells);
        allButtons.add(btnBhiBroth);
        allButtons.add(btnLbBroth);
        allButtons.add(btnMinimalMediaBroth);
        allButtons.add(btnExponential);
        allButtons.add(btnStationnary);

        initComboGenome();

    }

    private void initComboGenome() {
        /**
         * Update combo genome
         */
        comboGenome.add("All");
        ArrayList<String> genomes = BioCondition.getProteomeGenomes();
        for (String genome : genomes) {
            genome = genome.replaceFirst("Listeria monocytogenes", "L. mono.");
            comboGenome.add(genome);
        }
        comboGenome.select(0);
    }

    /**
     * From finalarrayBioCondition update the text for every Checkbox
     */
    public void updateInfo() {

        /*
         * For each category count the number of data available
         */
        int geneExpr = 0;
        int tiling = 0;
        int tss = 0;
        int rnaSeq = 0;
        int humanblood = 0;
        int mousemacro = 0;
        int bhi = 0;
        int lb = 0;
        int mm = 0;
        int expo = 0;
        int stat = 0;
        int unpublished = 0;
        for (String[] row : view.getBioCondsToDisplay()) {
            // Data type
            String dataType = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
            if (dataType.contains("Cytoplasm"))
                geneExpr++;
            if (dataType.contains("Cell wall"))
                tiling++;
            if (dataType.contains("Membrane"))
                tss++;
            if (dataType.contains("Secretome"))
                rnaSeq++;

            // Unpublished data
            String reference = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Reference")];
            if (reference.contains("Unpublished"))
                unpublished++;

            // intracellular type
            String intracellular = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
            if (intracellular.contains("Human blood cells"))
                humanblood++;
            if (intracellular.contains("Mouse macrophages cells"))
                mousemacro++;

            // Broth type
            String broth = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
            if (broth.contains("BHI"))
                bhi++;
            if (broth.contains("LB Broth"))
                lb++;
            if (broth.contains("Minimal Media Broth"))
                mm++;

            // growth type
            String growth = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
            if (growth.contains("Exp phase"))
                expo++;
            if (growth.contains("Stationary"))
                stat++;

        }

        /*
         * Update checkbox text with the number of data available for each category
         */
        btnCytoplasm.setText("Cytoplasm (" + geneExpr + ")");
        btnCellWall.setText("Cell Wall (" + tiling + ")");
        btnMembrane.setText("Membrane (" + tss + ")");
        btnSecretome.setText("Secretome (" + rnaSeq + ")");
        btnHumanBloodCells.setText("Human blood cells (" + humanblood + ")");
        btnMouseMacrophagesCells.setText("Mouse macrophages cells (" + mousemacro + ")");
        btnBhiBroth.setText("BHI Broth (" + bhi + ")");
        btnLbBroth.setText("LB Broth (" + lb + ")");
        btnMinimalMediaBroth.setText("Minimal Media Broth (" + mm + ")");
        btnExponential.setText("Exponential (" + expo + ")");
        btnStationnary.setText("Stationnary (" + stat + ")");
        if (Database.getInstance().getProjectName() != Database.UIBCLISTERIOMICS_PROJECT) {
            btnUnpublished.dispose();
        } else {
            btnUnpublished.setText("Unpublished (" + unpublished + ")");
        }

    }

    public void updateBioConditionList() {
        view.getBioCondsToDisplay().clear();

        /*
         * Update with search text
         */
        if (!textSearch.getText().equals("")) {
            for (String[] row : view.getBioConds()) {
                boolean found = false;
                for (String cell : row) {
                    if (cell.contains(textSearch.getText())) {
                        found = true;
                    }
                }
                if (found) {
                    view.getBioCondsToDisplay().add(row);
                }
            }
        } else {
            for (String[] row : view.getBioConds()) {
                view.getBioCondsToDisplay().add(row);
            }
        }

        /*
         * Update with genome information
         */
        String genome = comboGenome.getText().replaceFirst("L. mono.", "Listeria monocytogenes");
        if (!genome.equals("All")) {
            ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<>();
            for (String[] row : view.getBioCondsToDisplay()) {
                String genomeRow = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Strain array")];
                if (genomeRow.equals(genome)) {
                    bioCondsToDisplayTemp.add(row);
                }
            }
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }

        /*
         * Update with Data type
         */
        ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<>();
        boolean selected = false;
        if (btnCytoplasm.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
                if (info.contains("Cytoplasm")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnCellWall.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
                if (info.contains("Cell wall")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMembrane.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
                if (info.contains("Membrane")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnSecretome.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
                if (info.contains("Secretome")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }

        /*
         * Reference section
         */
        if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
            selected = false;
            if (btnUnpublished.getSelection()) {
                selected = true;
                for (String[] row : view.getBioCondsToDisplay()) {
                    String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Reference")];
                    if (info.contains("Unpublished (Cossart lab)")) {
                        if (!bioCondsToDisplayTemp.contains(row))
                            bioCondsToDisplayTemp.add(row);
                    }
                }

            }
            if (selected) {
                view.getBioCondsToDisplay().clear();
                for (String[] row : bioCondsToDisplayTemp)
                    view.getBioCondsToDisplay().add(row);
            }
        }

        /*
         * Update with intracellular Media type
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        if (btnHumanBloodCells.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("Human blood cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMouseMacrophagesCells.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("Mouse macrophages cells")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnBhiBroth.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("BHI")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnLbBroth.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("LB Broth")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMinimalMediaBroth.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("Minimal Media Broth")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }

        /*
         * Update with Growth type
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        if (btnExponential.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
                if (info.contains("Exp phase")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnStationnary.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
                if (info.contains("Stationary")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }

        if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
            bioCondsToDisplayTemp = new ArrayList<String[]>();
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Reference")];
                if (info.contains("Unpublished (Cossart lab)")) {
                    //
                } else {
                    bioCondsToDisplayTemp.add(row);
                }
            }
            view.setBioCondsToDisplay(bioCondsToDisplayTemp);
        }
    }

    public void pushState() {
        HashMap<String, String> statesParameters = new HashMap<>();

        /*
         * Update with search text
         */
        if (!textSearch.getText().equals("")) {
            statesParameters.put(NavigationManagement.SEARCH, textSearch.getText());
        }

        /*
         * Update with genome information
         */
        String genome = comboGenome.getText().replaceFirst("L. mono.", "Listeria monocytogenes");
        if (!genome.equals("All")) {
            statesParameters.put(NavigationManagement.COMBO, comboGenome.getItem(comboGenome.getSelectionIndex()));
        }

        int buttonPressed = 1;
        for (Button button : allButtons) {
            if (button.getSelection()) {
                String state = button.getText();
                state = state.substring(0, state.indexOf('(') - 1);
                statesParameters.put(NavigationManagement.BUTTON + buttonPressed, state);
                buttonPressed++;
            }
        }

        NavigationManagement.pushStateView(ProteomicsView.ID, statesParameters);

    }

    public void updateView() {
        updateBioConditionList();
        pushState();
        view.updateBioConditionTable();
        updateInfo();
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        updateView();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    public Combo getComboGenome() {
        return comboGenome;
    }

    public void setComboGenome(Combo comboGenome) {
        this.comboGenome = comboGenome;
    }

    public ArrayList<Button> getAllButtons() {
        return allButtons;
    }

    public void setAllButtons(ArrayList<Button> allButtons) {
        this.allButtons = allButtons;
    }

    public Text getTextSearch() {
        return textSearch;
    }

    public void setTextSearch(Text textSearch) {
        this.textSearch = textSearch;
    }

}
