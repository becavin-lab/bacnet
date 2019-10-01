package bacnet.sequenceTools;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.Strand;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import bacnet.Database;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Genome;
import bacnet.raprcp.SaveFileUtils;
import bacnet.swt.SWTResourceManager;

public class SequenceDisplayDialog extends Dialog implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3418095326728894710L;
    private Text textBegin;
    private Text textEnd;
    private Text textSequence;

    private Genome genome;
    private String chromoId = "";
    private String seqFasta = "";
    private boolean nucleotide = true;
    private int begin = -1;
    private int end = -1;
    private Strand strand = Strand.POSITIVE;
    private Combo comboGenome;
    private Combo cmbChromo;
    private Button btnNucleotideSequence;
    private Button btnAminoAcidSequence;
    private Button btnStrandplus;
    private Button btnStrandMinus;
    private Label lblInfo;
    private Button btnSaveToFasta;
    
    private Shell shell;
    private EPartService partService;

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public SequenceDisplayDialog(Shell parentShell, EPartService partService, Genome genome, String chromoId,
            boolean nucleotide, int begin, int end, Strand strand) {
        super(parentShell);
        this.shell = parentShell;
        this.partService = partService;
        this.genome = genome;
        this.chromoId = chromoId;
        this.nucleotide = nucleotide;
        this.begin = begin;
        this.end = end;
        this.strand = strand;
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();
        gridLayout.numColumns = 2;

        Label lblDisplay = new Label(container, SWT.NONE);
        lblDisplay.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDisplay.setText("Display");

        Composite composite_1 = new Composite(container, SWT.NONE);
        composite_1.setLayout(new GridLayout(2, false));

        btnNucleotideSequence = new Button(composite_1, SWT.RADIO);
        btnNucleotideSequence.setSelection(true);
        btnNucleotideSequence.setText("Nucleotide sequence");
        btnNucleotideSequence.addSelectionListener(this);
        btnAminoAcidSequence = new Button(composite_1, SWT.RADIO);
        btnAminoAcidSequence.setText("Amino acid sequence");
        btnAminoAcidSequence.addSelectionListener(this);

        Label lblGenome = new Label(container, SWT.NONE);
        lblGenome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblGenome.setText("Genome");

        comboGenome = new Combo(container, SWT.NONE);
        comboGenome.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboGenome.addSelectionListener(this);
        Label lblChromosome = new Label(container, SWT.NONE);
        lblChromosome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblChromosome.setText("Chromosome");

        cmbChromo = new Combo(container, SWT.NONE);
        cmbChromo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        cmbChromo.addSelectionListener(this);
        Label lblBegin = new Label(container, SWT.NONE);
        lblBegin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblBegin.setText("Begin");

        textBegin = new Text(container, SWT.BORDER);
        textBegin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textBegin.addSelectionListener(this);
        textBegin.addKeyListener(new KeyListener() {
            /**
             * 
             */
            private static final long serialVersionUID = -2395446561922797310L;

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                updateView();
            }
        });

        Label lblEnd = new Label(container, SWT.NONE);
        lblEnd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblEnd.setText("End");

        textEnd = new Text(container, SWT.BORDER);
        textEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textEnd.addSelectionListener(this);
        textEnd.addKeyListener(new KeyListener() {
            /**
             * 
             */
            private static final long serialVersionUID = -9021058189305495978L;

            @Override
            public void keyReleased(KeyEvent e) {
                updateView();
            }

            @Override
            public void keyPressed(KeyEvent e) {}
        });

        Label lblStrand = new Label(container, SWT.NONE);
        lblStrand.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblStrand.setText("Strand");

        Composite composite_2 = new Composite(container, SWT.NONE);
        composite_2.setLayout(new GridLayout(2, false));

        btnStrandplus = new Button(composite_2, SWT.RADIO);
        btnStrandplus.setSelection(true);
        btnStrandplus.setText("+");
        btnStrandplus.addSelectionListener(this);
        btnStrandMinus = new Button(composite_2, SWT.RADIO);
        btnStrandMinus.setText("-");
        btnStrandMinus.addSelectionListener(this);

        lblInfo = new Label(container, SWT.NONE);
        lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        lblInfo.setText("Display n nucleotides");

        textSequence = new Text(container, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.CANCEL | SWT.MULTI);
        GridData gd_textSequence = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_textSequence.heightHint = 175;
        textSequence.setLayoutData(gd_textSequence);
        textSequence.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        textSequence.setFont(SWTResourceManager.getFont("Courrier", 14, SWT.NORMAL, false, false));

        new Label(container, SWT.NONE);

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        composite.setLayout(new GridLayout(2, false));

        btnSaveToFasta = new Button(composite, SWT.NONE);
        btnSaveToFasta.setText("Save to Fasta file");
        btnSaveToFasta.addSelectionListener(this);
        
        initView();
        updateView();
        return container;
    }

    public void initView() {
        /*
         * Update comboGenome
         */
        for (String genomeTemp : Database.getInstance().getGenomeList()) {
            comboGenome.add(genomeTemp);
        }
        int index = -1;
        for (int i = 0; i < comboGenome.getItems().length; i++) {
            if (comboGenome.getItem(i).contains(genome.getSpecies())) {
                index = i;
            }
        }
        comboGenome.select(index);

        /*
         * Update comboChromo
         */
        index = -1;
        int i = 0;
        for (String chromoIDTemp : genome.getChromosomes().keySet()) {
            Chromosome chromo = genome.getChromosomes().get(chromoIDTemp);
            cmbChromo.add(chromo.getChromosomeNumber() + " - " + chromoIDTemp);
            if (chromoId.equals(chromoIDTemp)) {
                index = i;
            }
            i++;
        }
        cmbChromo.select(index);

        /*
         * Update typeSequence buttons
         */
        if (nucleotide) {
            btnNucleotideSequence.setSelection(true);
            btnAminoAcidSequence.setSelection(false);
        } else {
            btnAminoAcidSequence.setSelection(true);
            btnNucleotideSequence.setSelection(false);
        }
        /*
         * Set position info
         */
        textBegin.setText(begin + "");
        textEnd.setText(end + "");

        /*
         * Set strand
         */
        if (strand == Strand.POSITIVE) {
            btnStrandplus.setSelection(true);
            btnStrandMinus.setSelection(false);
        } else {
            btnStrandplus.setSelection(false);
            btnStrandMinus.setSelection(true);
        }
    }

    private void initComboChromo() {
        cmbChromo.removeAll();
        for (String chromoIDTemp : genome.getChromosomes().keySet()) {
            Chromosome chromo = genome.getChromosomes().get(chromoIDTemp);
            cmbChromo.add(chromo.getChromosomeNumber() + " - " + chromoIDTemp);
        }
        chromoId = genome.getFirstChromosome().getChromosomeID();
        cmbChromo.select(0);
    }

    public void updateView() {
        // get genome
        chromoId = cmbChromo.getItem(cmbChromo.getSelectionIndex()).split(" - ")[1];

        // set strand
        if (btnStrandplus.getSelection()) {
            this.setStrand(Strand.POSITIVE);
        } else {
            this.setStrand(Strand.NEGATIVE);
        }

        try {
            this.setBegin(Integer.parseInt(textBegin.getText().trim()));
            this.setEnd(Integer.parseInt(textEnd.getText().trim()));
            if (begin > end) {
                textSequence.setText("WARNING: \"begin\" should be lower than \"end\"");
            } else if (begin < 1) {
                textSequence.setText("WARNING: \"begin\" should be higher than 1");
            } else if (end >= genome.getChromosomes().get(chromoId).getLength()) {
                textSequence.setText(
                        "WARNING: \"end\" should be lower than " + genome.getChromosomes().get(chromoId).getLength());
            } else if (btnNucleotideSequence.getSelection()) {
                String sequence = genome.getChromosomes().get(chromoId).getSequenceAsString(begin, end, strand);
                String strandText = "+";
                if (strand == Strand.NEGATIVE)
                    strandText = "-";
                seqFasta = ">" + genome.getSpecies().replaceAll(" ", "_") + "|" + chromoId + "|" + begin + "--" + end
                        + "(" + strandText + ")|DNA\n";
                seqFasta += sequence;
                textSequence.setText(seqFasta);
                lblInfo.setText("Display " + sequence.length() + " nucleotides");
                this.setNucleotide(true);
            } else {
                String sequence = genome.getChromosomes().get(chromoId).getSequenceAsString(begin, end, strand);
                DNASequence seqNucleotide = new DNASequence(sequence);
                ProteinSequence protein = seqNucleotide.getRNASequence().getProteinSequence();
                String strandText = "+";
                if (strand == Strand.NEGATIVE)
                    strandText = "-";
                seqFasta = ">" + genome.getSpecies().replaceAll(" ", "_") + "|" + chromoId + "|" + begin + "--" + end
                        + "(" + strandText + ")|AA\n";
                seqFasta += protein.getSequenceAsString();
                textSequence.setText(seqFasta);

                lblInfo.setText("Display " + protein.getLength() + " amino acids");
                this.setNucleotide(false);
            }
        } catch (Exception e) {
            System.out.println("Cannot parse begin and end");
            textSequence.setText("WARNING: Cannot parse \"begin\" or \"end\", only integers are allowed");
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

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnAminoAcidSequence) {
            updateView();
        } else if (e.getSource() == btnNucleotideSequence) {
            updateView();
        } else if (e.getSource() == btnStrandplus) {
            updateView();
        } else if (e.getSource() == btnStrandMinus) {
            updateView();
        } else if (e.getSource() == comboGenome) {
            String genomeName = comboGenome.getItem(comboGenome.getSelectionIndex());
            this.genome = Genome.loadGenome(genomeName);
            initComboChromo();
            updateView();
        } else if (e.getSource() == cmbChromo) {
            updateView();
        } else if (e.getSource() == btnSaveToFasta) {
            SaveFileUtils.saveTextFile("SavedSequence.fa", textSequence.getText(), true, "SavedSequence.fa",
                    textSequence.getText(), partService, shell);
        } 
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(468, 609);
    }

    public Genome getGenome() {
        return genome;
    }

    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public String getChromoId() {
        return chromoId;
    }

    public void setChromoId(String chromoId) {
        this.chromoId = chromoId;
    }

    public boolean isNucleotide() {
        return nucleotide;
    }

    public void setNucleotide(boolean nucleotide) {
        this.nucleotide = nucleotide;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Combo getCmbGenome() {
        return comboGenome;
    }

    public void setCmbGenome(Combo cmbGenome) {
        this.comboGenome = cmbGenome;
    }

    public Combo getCmbChromo() {
        return cmbChromo;
    }

    public void setCmbChromo(Combo cmbChromo) {
        this.cmbChromo = cmbChromo;
    }

    public Strand getStrand() {
        return strand;
    }

    public void setStrand(Strand strand) {
        this.strand = strand;
    }

}
