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
import bacnet.raprcp.NavigationManagement;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.ArrayUtils;

public class TranscriptomicsDataFilterComposite extends org.eclipse.swt.widgets.Composite implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 7804472924985182424L;
    private Button btnGeneExpression;
    private Button btnRnaseq;
    private Button btnTss;
    private Button btnRiboSeq;
    private Button btnTermSeq;
    private Button btnLagPhase;
    private Button btnExponential;
    private Button btnStationnary;
    private Button btnSurvival;
    private Button btnDeath;
    private Button btnRegrowth;
    private Button btnTiling;
    private Combo comboMutant;
    private Button btnChooseOneMutant;
    private Button btnNoneMutant;
    private Button btnAllMutant;
    private Text textSearch;
    private Combo comboGenome;
    private Button btnUnpublished;
    private ArrayList<Button> allButtons = new ArrayList<>();
    private TranscriptomicsView view;

    public TranscriptomicsDataFilterComposite(org.eclipse.swt.widgets.Composite parent, int style,
            TranscriptomicsView view) {
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

        new Label(this, SWT.NONE);
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
            new Label(this, SWT.NONE);
        }
        {
            Label lblNewLabel_2 = new Label(this, SWT.NONE);
            lblNewLabel_2.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
            lblNewLabel_2.setText("Data type");
        }
        {
            Composite composite = new Composite(this, SWT.NONE);
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

            btnGeneExpression.addSelectionListener(this);
            btnTiling.addSelectionListener(this);
            btnTss.addSelectionListener(this);
            btnRnaseq.addSelectionListener(this);
            btnRiboSeq.addSelectionListener(this);
            btnTermSeq.addSelectionListener(this);
        }

        Composite composite_1 = new Composite(this, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_1.setLayout(new GridLayout(1, false));

        btnUnpublished = new Button(composite_1, SWT.CHECK);
        btnUnpublished.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnUnpublished.setText("Unpublished (0)");
        btnUnpublished.addSelectionListener(this);

        
        new Label(this, SWT.NONE);

        Label lblMutantgenesAnd = new Label(this, SWT.NONE);
        lblMutantgenesAnd.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblMutantgenesAnd.setText("Mutant (Gene and sRNA)");

        Composite composite_6 = new Composite(this, SWT.NONE);
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

        new Label(this, SWT.NONE);

        Label lblGrowthPhases = new Label(this, SWT.NONE);
        lblGrowthPhases.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowthPhases.setText("Growth phases");

        Composite composite_2 = new Composite(this, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        btnLagPhase = new Button(composite_2, SWT.CHECK);
        btnLagPhase.setText("Lag");

        btnExponential = new Button(composite_2, SWT.CHECK);
        btnExponential.setText("Exponential");

        btnStationnary = new Button(composite_2, SWT.CHECK);
        btnStationnary.setText("Stationary");

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

        getComboGenome().add("All");
        ArrayList<String> genomes = BioCondition.getTranscriptomesGenomes();
        for (String genome : genomes) {
            if (Database.getInstance().getSpecies().equals("Listeria")) {
                genome = genome.replaceFirst("Listeria monocytogenes", "L. mono.");
                genome = genome.replaceFirst("Listeria innocua", "L. innocua");
            }
            getComboGenome().add(genome);
        }
        getComboGenome().select(0);
        allButtons.add(btnLagPhase);
        allButtons.add(btnExponential);
        allButtons.add(btnStationnary);
        allButtons.add(btnSurvival);
        allButtons.add(btnDeath);
        allButtons.add(btnRegrowth);
        allButtons.add(btnGeneExpression);
        allButtons.add(btnTiling);
        allButtons.add(btnTss);
        allButtons.add(btnTermSeq);
        allButtons.add(btnRnaseq);
        allButtons.add(btnRiboSeq);

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
        int termseq = 0;
        int riboseq = 0;
        int rnaSeq = 0;
        int lag = 0;
        int expo = 0;
        int stat = 0;
        int surival = 0;
        int death = 0;
        int regrowth = 0;
        int unpublished = 0;
        for (String[] row : view.getBioCondsToDisplay()) {
            // Data type
            String dataType = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Type")];
            if (dataType.contains("GeneExpression"))
                geneExpr++;
            if (dataType.contains("GeneExpr"))
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
            String reference = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Reference")];
            if (reference.contains("Unpublished"))
                unpublished++;

            // growth type
            String growth = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
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
        btnTermSeq.setText("TermSeq (" + termseq + ")");
        btnRnaseq.setText("RNASeq (" + rnaSeq + ")");
        btnRiboSeq.setText("RiboSeq (" + riboseq + ")");
        if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
            btnUnpublished.dispose();
        } else {
            btnUnpublished.setText("Unpublished (" + unpublished + ")");
        }
        btnLagPhase.setText("Lag (" + lag + ")");
        btnExponential.setText("Exponential (" + expo + ")");
        btnStationnary.setText("Stationnary (" + stat + ")");
        btnSurvival.setText("Survival (" + surival + ")");
        btnDeath.setText("Death (" + death + ")");
        btnRegrowth.setText("Regrowth (" + regrowth + ")");

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
        genome = genome.replaceFirst("L. innocua", "Listeria innocua");
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
        if (btnGeneExpression.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Type")];
                if (info.contains("GeneExpression")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
                if (info.contains("GeneExpr")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTiling.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Type")];
                if (info.contains("Tiling")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTss.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Type")];
                if (info.contains("TSS")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTermSeq.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Type")];
                if (info.contains("TermSeq")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnRnaseq.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Type")];
                if (info.contains("RNASeq")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnRiboSeq.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Type")];
                if (info.contains("RiboSeq")) {
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }

        /*
         * Update with Broth type
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }

        /*
         * Update with Mutant
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        if (btnNoneMutant.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Mutant")];
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
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Mutant")];
                if (info.contains(mutantSelected)) {
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
        if (btnLagPhase.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
                if (info.contains("Lag")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
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
        if (btnSurvival.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
                if (info.contains("survival")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnDeath.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
                if (info.contains("Death")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnRegrowth.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
                if (info.contains("Regrowth")) {
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
        genome = genome.replaceFirst("L. innocua", "Listeria innocua");
        if (!genome.equals("All")) {
            statesParameters.put(NavigationManagement.COMBO + "1",
                    comboGenome.getItem(comboGenome.getSelectionIndex()));
        }

        if (btnChooseOneMutant.getSelection()) {
            String mutantSelected = comboMutant.getItem(comboMutant.getSelectionIndex());
            if (mutantSelected.indexOf('(') != -1)
                mutantSelected = mutantSelected.substring(0, mutantSelected.indexOf('(')).trim();
            statesParameters.put(NavigationManagement.COMBO + "2", mutantSelected);
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
        NavigationManagement.pushStateView(TranscriptomicsView.ID, statesParameters);
    }

    public void updateView() {
        updateBioConditionList();
        pushState();
        view.updateBioConditionTable();
        updateInfo();
        redraw();
    }

    public Combo getComboMutant() {
        return comboMutant;
    }

    public void setComboMutant(Combo comboMutant) {
        this.comboMutant = comboMutant;
    }

    public Combo getComboGenome() {
        return comboGenome;
    }

    public void setComboGenome(Combo comboGenome) {
        this.comboGenome = comboGenome;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
    	updateView();  	
    	
    	
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    public Button getBtnGeneExpression() {
        return btnGeneExpression;
    }

    public void setBtnGeneExpression(Button btnGeneExpression) {
        this.btnGeneExpression = btnGeneExpression;
    }

    public Button getBtnRnaseq() {
        return btnRnaseq;
    }

    public void setBtnRnaseq(Button btnRnaseq) {
        this.btnRnaseq = btnRnaseq;
    }

    public Button getBtnTss() {
        return btnTss;
    }

    public void setBtnTss(Button btnTss) {
        this.btnTss = btnTss;
    }

    public Button getBtnRiboSeq() {
        return btnRiboSeq;
    }

    public void setBtnRiboSeq(Button btnRiboSeq) {
        this.btnRiboSeq = btnRiboSeq;
    }

    public Button getBtnTermSeq() {
        return btnTermSeq;
    }

    public void setBtnTermSeq(Button btnTermSeq) {
        this.btnTermSeq = btnTermSeq;
    }

    

    public Button getBtnLagPhase() {
        return btnLagPhase;
    }

    public void setBtnLagPhase(Button btnLagPhase) {
        this.btnLagPhase = btnLagPhase;
    }

    public Button getBtnExponential() {
        return btnExponential;
    }

    public void setBtnExponential(Button btnExponential) {
        this.btnExponential = btnExponential;
    }

    public Button getBtnStationnary() {
        return btnStationnary;
    }

    public void setBtnStationnary(Button btnStationnary) {
        this.btnStationnary = btnStationnary;
    }

    public Button getBtnSurvival() {
        return btnSurvival;
    }

    public void setBtnSurvival(Button btnSurvival) {
        this.btnSurvival = btnSurvival;
    }

    public Button getBtnDeath() {
        return btnDeath;
    }

    public void setBtnDeath(Button btnDeath) {
        this.btnDeath = btnDeath;
    }

    public Button getBtnRegrowth() {
        return btnRegrowth;
    }

    public void setBtnRegrowth(Button btnRegrowth) {
        this.btnRegrowth = btnRegrowth;
    }

    public Button getBtnTiling() {
        return btnTiling;
    }

    public void setBtnTiling(Button btnTiling) {
        this.btnTiling = btnTiling;
    }

    public Button getBtnChooseOneMutant() {
        return btnChooseOneMutant;
    }

    public void setBtnChooseOneMutant(Button btnChooseOneMutant) {
        this.btnChooseOneMutant = btnChooseOneMutant;
    }

    public Button getBtnNoneMutant() {
        return btnNoneMutant;
    }

    public void setBtnNoneMutant(Button btnNoneMutant) {
        this.btnNoneMutant = btnNoneMutant;
    }

    public Button getBtnAllMutant() {
        return btnAllMutant;
    }

    public void setBtnAllMutant(Button btnAllMutant) {
        this.btnAllMutant = btnAllMutant;
    }

    public Text getTextSearch() {
        return textSearch;
    }

    public void setTextSearch(Text textSearch) {
        this.textSearch = textSearch;
    }

    public Button getBtnUnpublished() {
        return btnUnpublished;
    }

    public void setBtnUnpublished(Button btnUnpublished) {
        this.btnUnpublished = btnUnpublished;
    }

    public ArrayList<Button> getAllButtons() {
        return allButtons;
    }

    public void setAllButtons(ArrayList<Button> allButtons) {
        this.allButtons = allButtons;
    }

}
