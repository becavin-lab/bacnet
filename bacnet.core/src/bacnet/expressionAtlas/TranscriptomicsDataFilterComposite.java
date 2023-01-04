package bacnet.expressionAtlas;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.rap.rwt.RWT;
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
    private Button btnTiling;
    private Button btnTemp28;
    private Button btnTemp37;
    private Button btnTempOther;
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
            lblGenome.setText("Reference strain");
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
            btnGeneExpression.setText("Array");

            //btnTiling = new Button(composite, SWT.CHECK);
            //btnTiling.setText("Tiling");

            //btnTss = new Button(composite, SWT.CHECK);
            //btnTss.setText("TSS");

            //btnTermSeq = new Button(composite, SWT.CHECK);
            //btnTermSeq.setText("TermSeq");

            btnRnaseq = new Button(composite, SWT.CHECK);
            btnRnaseq.setText("RNA-Seq");

            //btnRiboSeq = new Button(composite, SWT.CHECK);
            //btnRiboSeq.setText("RiboSeq");

            btnGeneExpression.addSelectionListener(this);
            //btnTiling.addSelectionListener(this);
            //btnTss.addSelectionListener(this);
            btnRnaseq.addSelectionListener(this);
            //btnRiboSeq.addSelectionListener(this);
            //btnTermSeq.addSelectionListener(this);
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
        btnInCellulo.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        btnInCellulo.setText("<i>in cellulo</i>");
        btnInCellulo.addSelectionListener(this);
        
        btnBlood = new Button(composite_1_1, SWT.CHECK);
        btnBlood.setText("Blood");
        btnBlood.addSelectionListener(this);
        
        btnInVivo = new Button(composite_1_1, SWT.CHECK);
        btnInVivo.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        btnInVivo.setText("<i>in vivo</i>");
        btnInVivo.addSelectionListener(this);
        
        btnOtherMedia = new Button(composite_1_1, SWT.CHECK);
        btnOtherMedia.setText("Other Media");
        btnOtherMedia.addSelectionListener(this);
        
        new Label(this, SWT.NONE);

        Label lblGrowthPhases = new Label(this, SWT.NONE);
        lblGrowthPhases.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblGrowthPhases.setText("Growth Phase");

        Composite composite_2 = new Composite(this, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite_2.setLayout(new GridLayout(1, false));

        btnExponential = new Button(composite_2, SWT.CHECK);
        btnExponential.setText("Logarithmic");

        btnStationnary = new Button(composite_2, SWT.CHECK);
        btnStationnary.setText("Stationary");

        btnExponential.addSelectionListener(this);
        btnStationnary.addSelectionListener(this);

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
        allButtons.add(btnGeneExpression);
        //allButtons.add(btnTiling);
        //allButtons.add(btnTss);
        //allButtons.add(btnTermSeq);
        allButtons.add(btnRnaseq);
        //allButtons.add(btnRiboSeq);
        allButtons.add(btnExponential);
        allButtons.add(btnStationnary);
        allButtons.add(btnBlood);
        allButtons.add(btnDMEM);
        allButtons.add(btnBCS);
        allButtons.add(btnLB);
        allButtons.add(btnMinimalMedia);
        allButtons.add(btnInVivo);
        allButtons.add(btnInCellulo);
        allButtons.add(btnOtherMedia);
        allButtons.add(btnTemp28);
        allButtons.add(btnTemp37);
        allButtons.add(btnTempOther);

        

    }

    /**
     * From finalarrayBioCondition update the text for every Checkbox
     */
    public void updateInfo() {

        /*
         * For each category count the number of data available
         */
        int geneExpr = 0;
        //int tss = 0;
        int rnaSeq = 0;
        int expo = 0;
        int stat = 0;
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

        
        for (String[] row : view.getBioCondsToDisplay()) {
            // Data type
            String dataType = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Type")];
            if (dataType.contains("GeneExpression"))
                geneExpr++;
            if (dataType.contains("GeneExpr"))
                geneExpr++;
            if (dataType.contains("Tiling")) {
			}
            //if (dataType.contains("TSS"))
                //tss++;
            if (dataType.contains("RiboSeq")) {
			}
            if (dataType.contains("TermSeq")) {
			}
            if (dataType.contains("RNASeq"))
                rnaSeq++;

            // Unpublished data
            String reference = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Bibliographical reference")];
            if (reference.contains("Unpublished")) {
			}

            // temperature
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
            
         // Broth type
            String broth = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
            if (broth.contains("BCS"))
                BCS++;
            if (broth.contains("LB ")||broth.equals("LB"))
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
            if (broth.contains("THP-1")|| broth.contains("J774") || broth.contains("P388")
            	|| broth.contains("Monocyte")|| broth.contains("Neutrophil"))
                inCellulo++;
            if (!(broth.contains("BCS") || broth.contains("Minimal Media")||broth.contains("M9")||broth.contains("M63")||broth.contains("LB ")||broth.equals("LB")
            		||broth.contains("TMH")||broth.contains("PMH")||broth.contains("Plasma")
            		|| broth.contains("Blood")||broth.contains("DMEM")||broth.contains("RPMI")||broth.contains("Brown Norway")
            		|| broth.contains("C57BL")|| broth.contains("Xenopsylla")
            		|| broth.contains("Galleria")|| broth.contains("BALB")|| broth.contains("OF1")
            		|| broth.contains("FVB")|| broth.contains("Ictalurus")||broth.contains("THP-1")|| broth.contains("J774") || broth.contains("P388")
            		|| broth.contains("Monocyte")|| broth.contains("Neutrophil"))) {
            	mediaOther++;
            }
            
         // growth type
            String growth = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Growth")];
            if (growth.contains("Lag")) {
			}
            if (growth.contains("Log"))
                expo++;
            if (growth.contains("Stat"))
                stat++;
            if (growth.contains("survival")) {
			}
            if (growth.contains("Death")) {
			}
            if (growth.contains("Regrowth")) {
			}   
        }

        /*
         * Update checkbox text with the number of data available for each category
         */
        btnGeneExpression.setText("Array (" + geneExpr + ")");
        //btnTiling.setText("Tiling (" + tiling + ")");
        //btnTss.setText("TSS (" + tss + ")");
        //btnTermSeq.setText("TermSeq (" + termseq + ")");
        btnRnaseq.setText("RNA-Seq (" + rnaSeq + ")");
        //btnRiboSeq.setText("RiboSeq (" + riboseq + ")");
        
        btnTemp28.setText("25°C to 28°C (" + temp28 + ")");
        btnTemp37.setText("37°C (" + temp37 + ")");
        btnTempOther.setText("Other (" + tempOther + ")");
        btnBlood.setText("Blood (" + blood + ")");
        btnDMEM.setText("DMEM/RPMI (" + dmem + ")");
        btnBCS.setText("BCS (" + BCS + ")");
        btnLB.setText("LB (" + lb + ")");
        btnMinimalMedia.setText("Chemically Defined Media (" + mm + ")");
        btnInVivo.setText("<i>in vivo</i> (" + inVivo + ")");
        btnInCellulo.setText("<i>in cellulo</i> (" + inCellulo + ")");
        btnOtherMedia.setText("Other Media (" + mediaOther + ")");
        btnExponential.setText("Logarithmic (" + expo + ")");
        btnStationnary.setText("Stationnary (" + stat + ")");

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
        genome = genome.replaceFirst("L. innocua", "Listeria innocua");
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
        /*
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
        */
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
        /*
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
         */
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }


        /*
         * Update with Mutant
         */
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;

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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
        if (btnLB.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (info.contains("LB ")||info.equals("LB")) {
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
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
        if (btnOtherMedia.getSelection()) {
            selected = true;
            for (String[] row : view.getBioCondsToDisplay()) {
                String info = row[ArrayUtils.findColumn(view.getBioCondsArray(), "Media")];
                if (!(info.contains("BCS") || info.contains("Minimal Media")||info.contains("M9")||info.contains("M63")||info.contains("LB ")||info.equals("LB")
                		||info.contains("TMH")||info.contains("PMH")||info.contains("Plasma")
                		|| info.contains("Blood")||info.contains("DMEM")||info.contains("RPMI")||info.contains("Brown Norway")
                		|| info.contains("C57BL")|| info.contains("Xenopsylla")
                		|| info.contains("Galleria")|| info.contains("BALB")|| info.contains("OF1")
                		|| info.contains("FVB") || info.contains("Ictalurus")|| info.contains("THP-1")|| info.contains("J774") || info.contains("P388")
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
        
        if (selected) {
            view.getBioCondsToDisplay().clear();
            for (String[] row : bioCondsToDisplayTemp)
                view.getBioCondsToDisplay().add(row);
        }
        
        bioCondsToDisplayTemp = new ArrayList<>();
        selected = false;
        
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
        genome = genome.replaceFirst("L. innocua", "Listeria innocua");
        if (!genome.equals("All")) {
            statesParameters.put(NavigationManagement.COMBO + "1",
                    comboGenome.getItem(comboGenome.getSelectionIndex()));
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

    public Button getTemp28() {
        return btnTemp28;
    }

    public void setBtnTemp28(Button btnTemp28) {
        this.btnTemp28 = btnTemp28;
    }

    public Button getTemp37() {
        return btnTemp37;
    }

    public void setBtnTemp37(Button btnTemp37) {
        this.btnTemp37 = btnTemp37;
    }
    
    public Button getTempOther() {
        return btnTempOther;
    }

    public void setBtnTempOther(Button btnTempOther) {
        this.btnTempOther = btnTempOther;
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
