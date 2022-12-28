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
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.raprcp.NavigationManagement;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.ArrayUtils;

public class ProteomicsDataFilterComposite extends org.eclipse.swt.widgets.Composite implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -4827398812522159453L;
   
    private Button btnPellet;
    private Button btnCytoplasm;
    private Button btnPeriplasm;
    private Button btnSupernatant;
    private Button btnMembrane;
    private Button btnTemp28;
    private Button btnTemp37;
    private Button btnTempOther;
    private Button btnExponential;
    private Button btnStationnary;
    private Button btnBCS;
    private Button btnMinimalMedia;
    private Button btnDMEM;
    private Button btnBlood;
    private Button btnLB;
    private Button btnInVivo;
    private Button btnInCellulo;
    private Button btnOtherMedia;
    private Combo comboMutant;
    private Button btnChooseOneMutant;
    private Button btnNoneMutant;
    private Button btnAllMutant;
    private Text textSearch;
    private Combo comboGenome;
    //private Button btnUnpublished;
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

        new Label(this, SWT.NONE);
        {
            Label lblGenome = new Label(this, SWT.NONE);
            lblGenome.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
            lblGenome.setText("Reference strain");
        }
        {
            comboGenome = new Combo(this, SWT.NONE);
            GridData gd_comboGenome = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
            gd_comboGenome.widthHint = 270;
            comboGenome.setLayoutData(gd_comboGenome);
            comboGenome.addSelectionListener(this);
        }
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
        {
            new Label(this, SWT.NONE);
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

            btnPellet.addSelectionListener(this);
            btnCytoplasm.addSelectionListener(this);
            btnPeriplasm.addSelectionListener(this);
            btnMembrane.addSelectionListener(this);
            btnSupernatant.addSelectionListener(this);

        }
        
        new Label(this, SWT.NONE);

        Label lblTemp = new Label(this, SWT.NONE);
        lblTemp.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblTemp.setText("Temperature");

        Composite composite_21 = new Composite(this, SWT.NONE);
        composite_21.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_21.setLayout(new GridLayout(1, false));

        btnTemp28 = new Button(composite_21, SWT.CHECK);
        btnTemp28.setText("25°C to 28°C");

        btnTemp37 = new Button(composite_21, SWT.CHECK);
        btnTemp37.setText("37°");

        btnTempOther = new Button(composite_21, SWT.CHECK);
        btnTempOther.setText("Other");

        btnTemp28.addSelectionListener(this);
        btnTemp37.addSelectionListener(this);
        btnTempOther.addSelectionListener(this);

        new Label(this, SWT.NONE);

        Label lblGrowth = new Label(this, SWT.NONE);
        lblGrowth.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowth.setText("Growth Medium");

        Composite composite_1_1 = new Composite(this, SWT.NONE);
        composite_1_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_1_1.setLayout(new GridLayout(1, false));

        btnBCS = new Button(composite_1_1, SWT.CHECK);
        btnBCS.setText("BCS");
        btnBCS.addSelectionListener(this);

        btnLB = new Button(composite_1_1, SWT.CHECK);
        btnLB.setText("LB");
        btnLB.addSelectionListener(this);

        btnMinimalMedia = new Button(composite_1_1, SWT.CHECK);
        btnMinimalMedia.setText("Chemically Defined Media");
        btnMinimalMedia.addSelectionListener(this);

        btnDMEM = new Button(composite_1_1, SWT.CHECK);
        btnDMEM.setText("DMEM/RPMI");
        btnDMEM.addSelectionListener(this);
        
        btnInCellulo = new Button(composite_1_1, SWT.CHECK);
        btnInCellulo.setText("in cellulo");
        btnInCellulo.addSelectionListener(this);
        
        btnBlood = new Button(composite_1_1, SWT.CHECK);
        btnBlood.setText("Blood");
        btnBlood.addSelectionListener(this);
        
        btnInVivo = new Button(composite_1_1, SWT.CHECK);
        btnInVivo.setText("in vivo");
        btnInVivo.addSelectionListener(this);
        
        btnOtherMedia = new Button(composite_1_1, SWT.CHECK);
        btnOtherMedia.setText("Other Media");
        btnOtherMedia.addSelectionListener(this);

        new Label(this, SWT.NONE);

        Label lblGrowthPhases = new Label(this, SWT.NONE);
        lblGrowthPhases.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowthPhases.setText("Growth Phases");

        Composite composite_2 = new Composite(this, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        btnExponential = new Button(composite_2, SWT.CHECK);
        btnExponential.setText("Logarithmic");
        btnExponential.addSelectionListener(this);

        btnStationnary = new Button(composite_2, SWT.CHECK);
        btnStationnary.setText("Stationnary");
        btnStationnary.addSelectionListener(this);

        new Label(this, SWT.NONE);

        allButtons.add(btnPellet);
        allButtons.add(btnCytoplasm);
        allButtons.add(btnPeriplasm);
        allButtons.add(btnMembrane);
        allButtons.add(btnSupernatant);
        allButtons.add(btnBlood);
        allButtons.add(btnDMEM);
        allButtons.add(btnBCS);
        allButtons.add(btnBCS);
        allButtons.add(btnMinimalMedia);
        allButtons.add(btnInVivo);
        allButtons.add(btnInCellulo);
        allButtons.add(btnOtherMedia);
        allButtons.add(btnExponential);
        allButtons.add(btnStationnary);
        allButtons.add(btnTemp28);
        allButtons.add(btnTemp37);
        allButtons.add(btnTempOther);
        
        initComboGenome();

    }
    
    //not used
    /*
    public ProteomicsDataFilterComposite(org.eclipse.swt.widgets.Composite parent, int style, ProteomicsExpressionView view) {
        super(parent, style);
        this.setLayout(new GridLayout(1, false));
        //this.view = view;

        Label lblSearch = new Label(this, SWT.NONE);
        lblSearch.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblSearch.setText("Search");

        textSearch = new Text(this, SWT.BORDER);
        GridData gd_textSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_textSearch.widthHint = 250;
        textSearch.setLayoutData(gd_textSearch);
        textSearch.addKeyListener(new org.eclipse.swt.events.KeyListener() {

            
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
            lblNewLabel_2.setText("Localization");
        }
        {
            Composite composite = new Composite(this, SWT.NONE);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            composite.setLayout(new GridLayout(1, false));

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

        new Label(this, SWT.NONE);

        Label lblGrowth = new Label(this, SWT.NONE);
        lblGrowth.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowth.setText("Growth Medium");

        Composite composite_1_1 = new Composite(this, SWT.NONE);
        composite_1_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_1_1.setLayout(new GridLayout(1, false));

        btnBCSinfo = new Button(composite_1_1, SWT.CHECK);
        btnBCSinfo.setText("BCS");
        btnBCSinfo.addSelectionListener(this);

        btnLbinfo = new Button(composite_1_1, SWT.CHECK);
        btnLbinfo.setText("LB");
        btnLbinfo.addSelectionListener(this);

        btnMinimalMediainfo = new Button(composite_1_1, SWT.CHECK);
        btnMinimalMediainfo.setText("Chemically Defined Media");
        btnMinimalMediainfo.addSelectionListener(this);

        btnBlood = new Button(composite_1_1, SWT.CHECK);
        btnBlood.setText("Blood");
        btnBlood.addSelectionListener(this);

        btnDMEM = new Button(composite_1_1, SWT.CHECK);
        btnDMEM.setText("DMEM");
        btnDMEM.addSelectionListener(this);
        
        btnOtherMedia = new Button(composite_1_1, SWT.CHECK);
        btnOtherMedia.setText("Other Media");
        btnOtherMedia.addSelectionListener(this);

        new Label(this, SWT.NONE);

        Label lblGrowthPhases = new Label(this, SWT.NONE);
        lblGrowthPhases.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowthPhases.setText("Growth phases");

        Composite composite_2 = new Composite(this, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        btnExponential = new Button(composite_2, SWT.CHECK);
        btnExponential.setText("Logarithmic");

        btnStationnary = new Button(composite_2, SWT.CHECK);
        btnStationnary.setText("Stationnary");
        btnExponential.addSelectionListener(this);
        btnStationnary.addSelectionListener(this);

        new Label(this, SWT.NONE);

        allButtons.add(btnCytoplasm);
        allButtons.add(btnPeriplasm);
        allButtons.add(btnMembrane);
        allButtons.add(btnSupernatant);
        allButtons.add(btnBlood);
        allButtons.add(btnDMEM);
        allButtons.add(btnBCSinfo);
        allButtons.add(btnLbinfo);
        allButtons.add(btnMinimalMediainfo);
        allButtons.add(btnExponential);
        allButtons.add(btnStationnary);
        allButtons.add(btnOtherMedia);

        initComboGenome();

    }
*/
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
        int pell = 0;
        int cyto = 0;
        int peri = 0;
        int memb = 0;
        int sup = 0;
        int temp28 = 0;
        int temp37 = 0;
        int tempOther = 0;
        int blood = 0;
        int dmem = 0;
        int inVivo = 0;
        int inCellulo = 0;
        int BCS = 0;
        int lb = 0;
        int mm = 0;
        int mediaOther = 0;
        int expo = 0;
        int stat = 0;
        
        //int unpublished = 0;
        for (String[] row : view.getBioCondsToDisplay()) {
            // Data type
            String dataType = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
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

            // Temperature
            String temperature = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Temp.")];
            if (temperature.contains("28") || temperature.contains("26") || temperature.contains("25")|| temperature.contains("37")) {
            	 if (temperature.contains("37"))
                     temp37++;
                 if (temperature.contains("28") || temperature.contains("26") || temperature.contains("25")) {
                     temp28++;
     			}
			} else {
                tempOther++;
			}
            
            // broth type
            String broth = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
            if (broth.contains("BCS"))
                BCS++;
            if (broth.contains("LB"))
                lb++;
            if (broth.contains("Minimal Media")||broth.contains("M9")||broth.contains("M63")||broth.contains("TMH")||broth.contains("PMH"))
                mm++;
            if (broth.contains("Plasma") || broth.contains("Blood"))
                blood++;
            if (broth.contains("DMEM")||broth.contains("RPMI"))
                dmem++;
            if (broth.contains("Brown Norway")|| broth.contains("C57BL")|| broth.contains("Xenopsylla")
            		|| broth.contains("Galleria")|| broth.contains("BALB")|| broth.contains("OF1")
            		|| broth.contains("FVB")|| broth.contains("Ictalurus"))
                inVivo++;
            if (broth.contains("THP-1")|| broth.contains("J774")|| broth.contains("P388")
            	|| broth.contains("Monocyte")|| broth.contains("Neutrophil"))
                inCellulo++;
            if (!(broth.contains("BCS") || broth.contains("Minimal Media")||broth.contains("M9")||broth.contains("M63")||broth.contains("LB")
            		||broth.contains("TMH")||broth.contains("PMH")||broth.contains("Plasma")
            		|| broth.contains("Blood")||broth.contains("DMEM")||broth.contains("RPMI")||broth.contains("Brown Norway")
            		|| broth.contains("C57BL")|| broth.contains("Xenopsylla")
            		|| broth.contains("Galleria")|| broth.contains("BALB")|| broth.contains("OF1")
            		|| broth.contains("FVB")|| broth.contains("Ictalurus")||broth.contains("THP-1")|| broth.contains("J774")|| broth.contains("P388")
            		|| broth.contains("Monocyte")|| broth.contains("Neutrophil"))) {
            	mediaOther++;
            }

            // growth type
            String growth = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
            if (growth.contains("Log"))
                expo++;
            if (growth.contains("Stat"))
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
        btnTemp28.setText("25°C to 28°C (" + temp28 + ")");
        btnTemp37.setText("37°C (" + temp37 + ")");
        btnTempOther.setText("Other (" + tempOther + ")");
        btnBlood.setText("Blood (" + blood + ")");
        btnDMEM.setText("DMEM/RPMI (" + dmem + ")");
        btnInVivo.setText("in vivo (" + inVivo + ")");
        btnInCellulo.setText("in cellulo (" + inCellulo + ")");
        btnBCS.setText("BCS (" + BCS + ")");
        btnLB.setText("LB (" + lb + ")");
        btnMinimalMedia.setText("Chemically Defined Media (" + mm + ")");
        btnOtherMedia.setText("Other Media (" + mediaOther + ")");
        btnExponential.setText("Logarithmic Phase (" + expo + ")");
        btnStationnary.setText("Stationnary Phase (" + stat + ")");

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
                    if (cell.toLowerCase().contains(textSearch.getText().toLowerCase())) {
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
                String genomeRow = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Reference strain")];
                if (genomeRow.equals(genome)) {
                    bioCondsToDisplayTemp.add(row);
                }
            }
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }

        /*
         * Update with Mutant
         */
        ArrayList<String[]> bioCondsToDisplayTemp = new ArrayList<>();
        boolean selected = false;

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
         * Update with localization
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        

        if (btnPellet.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
                if (info.contains("Whole Cell") || info.contains("Bacterial Pellet")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
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
        if (btnPeriplasm.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
                if (info.contains("Periplasm")) {
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
        if (btnSupernatant.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Localization")];
                if (info.contains("Supernatant")) {
                    if (!bioCondsToDisplayTemp.contains(row)) {
                        bioCondsToDisplayTemp.add(row);
                        
                    }
                }
            }
        }
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        /*
         * Update with temperature
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
        if (btnTemp28.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Temp.")];
                if (info.contains("25") || info.contains("26") || info.contains("28")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTemp37.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Temp.")];
                if (info.contains("37")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnTempOther.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Temp.")];
                if (!(info.contains("25") || info.contains("26") || info.contains("28")||info.contains("37"))) {
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
         * Update with Media type
         */
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
        if (btnBlood.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("Plasma") || info.contains("Blood")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnDMEM.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("DMEM")||info.contains("RPMI")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnBCS.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("BCS")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnLB.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("LB")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnMinimalMedia.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("Minimal Media") ||info.contains("M9")||info.contains("M63")||info.contains("TMH")||info.contains("PMH")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        
        if (btnInVivo.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("Brown Norway")|| info.contains("C57BL")|| info.contains("Xenopsylla")
                		|| info.contains("Galleria")|| info.contains("BALB")|| info.contains("OF1")
                		|| info.contains("FVB")|| info.contains("Ictalurus")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        
        if (btnInCellulo.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("THP-1")|| info.contains("J774")|| info.contains("P388")|| info.contains("Monocyte")|| info.contains("Neutrophil")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        
        if (btnOtherMedia.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (!(info.contains("BCS") || info.contains("Minimal Media")||info.contains("M9")||info.contains("M63")||info.contains("LB")
                		||info.contains("TMH")||info.contains("PMH")||info.contains("Plasma")
                		|| info.contains("Blood")||info.contains("DMEM")||info.contains("RPMI")||info.contains("Brown Norway")
                		|| info.contains("C57BL")|| info.contains("Xenopsylla")
                		|| info.contains("Galleria")|| info.contains("BALB")|| info.contains("OF1")
                		|| info.contains("FVB")|| info.contains("Ictalurus") || info.contains("THP-1")|| info.contains("J774")|| info.contains("P388")
                		|| info.contains("Monocyte")|| info.contains("Neutrophil"))) {
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
                if (info.contains("Log")) {
                    if (!bioCondsToDisplayTemp.contains(row))
                        bioCondsToDisplayTemp.add(row);
                }
            }
        }
        if (btnStationnary.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
                if (info.contains("Stat")) {
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
        if (btnChooseOneMutant.getSelection()) {
            String mutantSelected = comboMutant.getItem(comboMutant.getSelectionIndex());
            if (mutantSelected.indexOf('(') != -1) {
                mutantSelected = mutantSelected.substring(0, mutantSelected.indexOf('(')).trim();}
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

    public ArrayList<Button> getAllButtons() {
        return allButtons;
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
