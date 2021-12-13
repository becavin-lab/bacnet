package bacnet.views;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import bacnet.Database;
import bacnet.datamodel.dataset.Network;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.expressionAtlas.core.SelectGenomeElementDialog;
import bacnet.raprcp.NavigationManagement;
import bacnet.raprcp.SaveFileUtils;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;
import bacnet.utils.BasicColor;
import bacnet.utils.HTMLUtils;

public class CoExprNetworkView implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -4429010895850129342L;

    public static final String ID = "bacnet.CoExprNetworkView";
    public static final String GRAPH_NAME = "Graph visualization";
    public static final String CIRCOS_NAME = "Radial visualization";

    /**
     * Indicates if we focus the view, so we can pushState navigation
     */
    private boolean focused = false;

    private Browser browserGraph;
    private Button btnSelectGenomeElements;
    private Table tableGenes;
    private Composite composite;
    private Text textCutOff;
    private ArrayList<String> listGenomeElements = new ArrayList<>();
    private Genome genome;
    private TabFolder tabFolder;
    private TabItem tbtmGraphVisualization;
    //private TabItem tbtmRadialNetwork;
    //private Browser browserRadial;
    private Button btnCorrPlus;
    private Button btnCorrMinus;

    private String genomeName;
    private Network generalNetwork;
    private Network filteredNetwork;
    private Text txtSearch;
    private Button btnHelp;

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;
    private Combo comboGenome;

    @Inject
    public CoExprNetworkView() {
        // TODO Auto-generated constructor stub
    }

    @PostConstruct
    public void createPartControl(Composite parent) {
        focused = true;
        parent.setLayout(new GridLayout(2, false));
        
        Composite compositeGenome = new Composite(parent, SWT.NONE);
        compositeGenome.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        compositeGenome.setLayout(new GridLayout(7, false));

        Label lblSelectAGenome = new Label(compositeGenome, SWT.NONE);
        lblSelectAGenome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSelectAGenome.setText("Select genome");

        comboGenome = new Combo(compositeGenome, SWT.NONE);
        GridData gd_comboGenome = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_comboGenome.widthHint = 220;
        comboGenome.setLayoutData(gd_comboGenome);
        comboGenome.addSelectionListener(this);
        
        
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);
        new Label(compositeGenome, SWT.NONE);

       
        
        Composite compositeGeneselection = new Composite(parent, SWT.BORDER);
        compositeGeneselection.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2));
        compositeGeneselection.setLayout(new GridLayout(1, false));

        Label lblSearch = new Label(compositeGeneselection, SWT.NONE);
        lblSearch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblSearch.setText("Search");
        lblSearch.setForeground(BasicColor.GREY);
        txtSearch = new Text(compositeGeneselection, SWT.BORDER);
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtSearch.addKeyListener(new KeyListener() {
            /**
             * 
             */
            private static final long serialVersionUID = -8644318212515933515L;

            @Override
            public void keyReleased(KeyEvent e) {
                if (txtSearch.getText().equals("")) {
                    updateGenomeInfo();
                }
                if (e.keyCode == 16777296 || e.keyCode == 13) {
                    System.out.println("Search for " + txtSearch.getText());
                    ArrayList<String> searchResults = search(txtSearch.getText());
                    if (searchResults.size() != 0) {
                        listGenomeElements.clear();
                        for (String geneName : searchResults)
                            listGenomeElements.add(geneName);
                        tableGenes.removeAll();
                        tableGenes.setItemCount(listGenomeElements.size());
                        tableGenes.update();
                        tableGenes.select(listGenomeElements.indexOf(searchResults.get(0)));
                        updateCoExpressionFigure();
                    }
                }

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }

        });
        btnSelectGenomeElements = new Button(compositeGeneselection, SWT.NONE);
        btnSelectGenomeElements.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnSelectGenomeElements.setText("Select genomic elements");
        btnSelectGenomeElements.addSelectionListener(this);

        tableGenes = new Table(compositeGeneselection, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
        tableGenes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
        tableGenes.addListener(SWT.SetData, new Listener() {
            private static final long serialVersionUID = 6744063943372593076L;

            @Override
            public void handleEvent(Event event) {
                TableItem item = (TableItem) event.item;
                int index = event.index;
                item.setText(listGenomeElements.get(index));
            }
        });
        tableGenes.addSelectionListener(this);

        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(5, false));

        Label lblDisplayCorrelation = new Label(composite, SWT.NONE);
        lblDisplayCorrelation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDisplayCorrelation.setText("Display links for two genomic elements having a correlation higher than ");

        btnCorrMinus = new Button(composite, SWT.NONE);
        btnCorrMinus.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomOUT.bmp"));
        btnCorrMinus.addSelectionListener(this);
        textCutOff = new Text(composite, SWT.BORDER);
        textCutOff.setText("0.98");
        textCutOff.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnCorrPlus = new Button(composite, SWT.NONE);
        btnCorrPlus.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/genome/zoomIN.bmp"));

        btnHelp = new Button(composite, SWT.NONE);
        btnHelp.setToolTipText("How to use Co-expression network panel ?");
        btnHelp.setImage(ResourceManager.getPluginImage("bacnet.core", "icons/help.png"));
        btnHelp.addSelectionListener(this);
        btnCorrPlus.addSelectionListener(this);

        tabFolder = new TabFolder(parent, SWT.NONE);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        tbtmGraphVisualization = new TabItem(tabFolder, SWT.NONE);
        tbtmGraphVisualization.setText(GRAPH_NAME);
        browserGraph = new Browser(tabFolder, SWT.BORDER);
        tbtmGraphVisualization.setControl(browserGraph);

//        tbtmRadialNetwork = new TabItem(tabFolder, SWT.NONE);
//        tbtmRadialNetwork.setText("Circular visualization");
//        browserRadial = new Browser(tabFolder, SWT.NONE);
//        tbtmRadialNetwork.setControl(browserRadial);

        tabFolder.addSelectionListener(new SelectionAdapter() {
            /**
             * 
             */
            private static final long serialVersionUID = 241821321620953447L;

            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                updateCoExpressionFigure();
            }
        });

        /*
         * Register the background SVG
         */
        //SaveFileUtils.registerTextFile(svgName, new File(Network.CIRCOS_BACK_PATH));


	    setData();
	}
	
	/**
	 * Set all starting variables
	 */
	private void setData() {
		
		/*
		 * Load available genomes for co--expression network
		 */
		String[][] genomes = TabDelimitedTableReader.read(Database.getInstance().getCoExprNetworkArrayPath());
		for (int i=1;i<genomes.length;i++) {
            comboGenome.add(genomes[i][0]);
        }		
		comboGenome.select(0);
		genomeName = comboGenome.getItem(0);
		
		/*
		 * Load network
		 */
		updateNetwork();
	}
	
	/**
	 * Load co-expression network file
	 */
	private void updateNetwork() {
		try {
            InitNetworkThread thread = new InitNetworkThread(this);
            new ProgressMonitorDialog(shell).run(true, false, thread);
            updateGenomeInfo();
            updateCoExpressionFigure();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
		
	}


    public static class InitNetworkThread implements IRunnableWithProgress {

        CoExprNetworkView view;

        public InitNetworkThread(CoExprNetworkView view) {
            this.view = view;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask("Display Co-expression network", 3);
            monitor.worked(1);
            view.initComboGenome();
            monitor.subTask("Load Co-expression network");
            monitor.worked(1);
            view.loadNetwork();
            monitor.subTask("Filter and display network");
            monitor.worked(1);
            monitor.done();
        }
    }

    public void initComboGenome() {
    	genome = Genome.loadGenome(genomeName);
    }

    /**
     * Load the general Network
     */
    public void loadNetwork() {
        generalNetwork = new Network();
        generalNetwork.load(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genomeName);
        System.out.println("Network loaded");
    }

    /**
     * Update all lists of genes after a genome selection, or opening of the view
     */
    private void updateGenomeInfo() {
        listGenomeElements = new ArrayList<>();
        Genome genome = Genome.loadGenome(genomeName);
        for (String gene : genome.getGeneNames()) {
            listGenomeElements.add(gene);
        }
        for (String srna : genome.getsRNAs().keySet()) {
            listGenomeElements.add(srna);
        }
        for (String cireg : genome.getCisRegs().keySet()) {
            listGenomeElements.add(cireg);
        }
        for (String asrna : genome.getAsRNAs().keySet()) {
            listGenomeElements.add(asrna);
        }

        tableGenes.removeAll();
        tableGenes.setItemCount(listGenomeElements.size());
        tableGenes.select(0);
        tableGenes.update();
    }

    /**
     * Using genome elements selected and current cut-off. FILTER network
     */
    public void filterNetwork() {
        /*
         * cut-off
         */
        double cutoff = Double.parseDouble(textCutOff.getText());
        /*
         * List selection
         */
        ArrayList<String> genomeElements = new ArrayList<>();
        for (TableItem item : tableGenes.getSelection()) {
            genomeElements.add(item.getText(0));
        }
        filteredNetwork = generalNetwork.filterNetwork(genomeElements, cutoff);
        pushState();
    }

    @Focus
    public void onFocus() {
        if (!focused) {
            pushState();
            focused = true;
        } else {
            focused = false;
        }
    }

    /**
     * Push Naviagtion state
     */
    private void pushState() {
        HashMap<String, String> stateParameters = new HashMap<>();
        /*
         * cut-off
         */
        stateParameters.put(NavigationManagement.CUTOFF, textCutOff.getText());
        /*
         * List selection
         */
        String list = "";
        for (TableItem item : tableGenes.getSelection()) {
            list += item.getText(0) + ":";
        }
        if (!list.equals("")) {
            stateParameters.put(NavigationManagement.LIST, list);
        }
        /*
         * Tab folder
         */
        Item item = tabFolder.getItem(tabFolder.getSelectionIndex());
        if (item.getText().contains("Circular")) {
            stateParameters.put(NavigationManagement.ITEM, "Circular");
        }
        NavigationManagement.pushStateView(CoExprNetworkView.ID, stateParameters);
    }

    private void updateCoExpressionFigure() {
        try {
            /*
             * Register the network data in corr.txt
             */
            File file = File.createTempFile("corr" + Math.random(), ".txt");
            String dataPath = file.getAbsolutePath();
            // System.out.println("Corr data - "+dataPath);
            filterNetwork();
            ArrayList<String> networkList = filteredNetwork.toArrayList();
            TabDelimitedTableReader.saveList(networkList, dataPath);

            /*
             * Get the Tab open and refresh appropriate view
             */
            // System.out.println("display:"+tabFolder.getItem(tabFolder.getSelectionIndex()).getText());
            if (tabFolder.getItem(tabFolder.getSelectionIndex()).getText().equals(GRAPH_NAME)) {
                /*
                 * Update Graph.html
                 */
                String html = SaveFileUtils.modifyHTMLwithFile(dataPath, HTMLUtils.NETWORK);
                browserGraph.setText(html);
                browserGraph.redraw();
            } else {
                /*
                 * Update circos.html
                 */
//                String html = SaveFileUtils.modifyHTMLwithFile(dataPath, HTMLUtils.CIRCOS);
//                String fileNameResource = SaveFileUtils.registerTextFile(svgName, new File(dataPath));
//                System.out.println("Add: " + fileNameResource + " to " + HTMLUtils.CIRCOS);
//                html = html.replaceFirst("_Background", fileNameResource);
//                browserRadial.setText(html);
//                browserRadial.redraw();
            }
            file.delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Search a text file in the annotation<br>
     * <li>Search if it is a position and go
     * <li>Search if it is contains in <code>getChromosome().getAllElements().keySet()</code>
     * <li>Search if it is contains in <code>gene.getName()</code><br>
     * All the search are case insensitive using <code>text.toUpperCase();</code>
     * 
     * @param text
     * @return
     */
    public ArrayList<String> search(String text) {
        Chromosome chromosome = genome.getFirstChromosome();
        ArrayList<String> searchResult = new ArrayList<>();
        try {
            /*
             * If a base pair position has been wrote, it will reach it directly
             */
            int position = Integer.parseInt(text);
            Sequence sequenceTemp = chromosome.getAnnotation().getElementInfoATbp(chromosome, position);
            if (sequenceTemp instanceof Gene) {
                searchResult.add(sequenceTemp.getName());
            }
            if (sequenceTemp instanceof Srna) {
                searchResult.add(sequenceTemp.getName());
            }
        } catch (Exception e) {
            /*
             * go through chromosome and search for a Gene with this name
             */
            text = text.toUpperCase();
            for (Gene gene : chromosome.getGenes().values()) {
                String geneName = gene.getName();
                String geneNameTemp = geneName.toUpperCase();
                if (geneNameTemp.contains(text)) {
                    if (!searchResult.contains(gene.getName())) {
                        searchResult.add(gene.getName());
                    }
                }
                geneName = gene.getGeneName();
                geneNameTemp = geneName.toUpperCase();
                if (geneNameTemp.contains(text)) {
                    if (!searchResult.contains(gene.getName())) {
                        searchResult.add(gene.getName());
                    }
                }
                String geneInfo = gene.getProduct() + "  -  " + gene.getProtein_id() + " - " + gene.getComment() + " - "
                        + gene.getFeaturesText();
                String geneInfoTemp = geneInfo.toUpperCase();
                if (geneInfoTemp.contains(text)) {
                    if (!searchResult.contains(gene.getName())) {
                        searchResult.add(gene.getName());
                    }
                }
            }

            /*
             * go through chromosome and search for a Gene with this name
             */
            if (genome.getSpecies().equals(Genome.EGDE_NAME)) {
                ArrayList<String> listSrnaTemp = new ArrayList<>();
                for (String rna : Database.getInstance().getsRNAListEGDe())
                    listSrnaTemp.add(rna);
                for (String rna : Database.getInstance().getAsRNAListEGDe())
                    listSrnaTemp.add(rna);
                for (String rna : Database.getInstance().getCisRegRNAListEGDe())
                    listSrnaTemp.add(rna);

                /*
                 * Go through all Srna and search information in the anniotation of each gene
                 */
                text = text.toUpperCase();
                for (String name : listSrnaTemp) {
                    Srna sRNA = (Srna) genome.getElement(name);
                    if (name.contains(text)) {
                        if (!searchResult.contains(sRNA.getName())) {
                            searchResult.add(sRNA.getName());
                        }
                    }
                    String sRNAInfo = sRNA.getFoundInText() + "  -  " + sRNA.getSynonym() + " - " + sRNA.getTypeSrna()
                            + " - " + sRNA.getFeaturesText();
                    String sRNAInfoTemp = sRNAInfo.toUpperCase();
                    if (sRNAInfoTemp.contains(text)) {
                        if (!searchResult.contains(sRNA.getName())) {
                            searchResult.add(sRNA.getName());
                        }
                    }
                }
            }
        }

        // for(String geneName : searchResult){
        // System.out.println(geneName);
        // }
        return searchResult;
    }

    
    
    
    /**
     * Display the view with saved parameters
     * 
     * @param gene
     * @param partService
     */
    public static void displayCoExpNetworkView(EPartService partService, HashMap<String, String> parameters) {
        partService.showPart(ID, PartState.ACTIVATE);
        // update data
        MPart part = partService.findPart(ID);
        NavigationManagement.pushStateView(ID, parameters);
        CoExprNetworkView view = (CoExprNetworkView) part.getObject();
        for (String stateId : parameters.keySet()) {
            String stateValue = parameters.get(stateId);
            if (stateId.equals(NavigationManagement.ITEM)) {
                //view.getTabFolder().setSelection(view.getTabFolder().indexOf(view.getTbtmRadialNetwork()));
            }
            if (stateId.contains(NavigationManagement.CUTOFF)) {
                view.getTextCutOff().setText(stateValue);
            }
            if (stateId.contains(NavigationManagement.LIST)) {
                String[] genes = stateValue.split(":");
                ArrayList<String> listGenes = new ArrayList<>();
                for (String gene : genes) {
                    listGenes.add(gene);
                }
                view.tableGenes.deselectAll();
                for (int i = 0; i < view.getTableGenes().getItemCount(); i++) {
                    TableItem item = view.getTableGenes().getItem(i);
                    if (listGenes.contains(item.getText())) {
                        view.getTableGenes().select(i);
                    }
                }
            }
        }
        view.updateCoExpressionFigure();
    }
    
    /**
     * The comboGenome contains modified genome name so we need this method to get selected element<br>
     * a '*' is add to genome name when a transcriptome data is available
     */
    public String getGenomeSelected() {
        if (comboGenome.isDisposed()) {
            return Genome.EGDE_NAME;
        } else {
            String genome = comboGenome.getItem(comboGenome.getSelectionIndex());
            return genome;
        }
    }
    

    @Override
    public void widgetSelected(SelectionEvent e) {
    	if (e.getSource() == comboGenome) {
            genomeName = getGenomeSelected();
            initComboGenome();
            updateNetwork();
        } else if (e.getSource() == btnCorrMinus) {
            double cutoff = Double.parseDouble(textCutOff.getText());
            if (cutoff > Network.CORR_CUTOFF) {
                cutoff = cutoff - 0.01;
                String cutoffString = cutoff + "";
                if (cutoffString.length() >= 4)
                    cutoffString = cutoffString.substring(0, 4);
                textCutOff.setText(cutoffString);
                updateCoExpressionFigure();
            }
        } else if (e.getSource() == btnCorrPlus) {
            double cutoff = Double.parseDouble(textCutOff.getText());
            cutoff = cutoff + 0.01;
            String cutoffString = cutoff + "";
            if (cutoffString.length() >= 4)
                cutoffString = cutoffString.substring(0, 4);
            textCutOff.setText(cutoffString);
            updateCoExpressionFigure();
        } else if (e.getSource() == tableGenes) {
            updateCoExpressionFigure();
        } else if (e.getSource() == btnSelectGenomeElements) {
            System.out.println("select genome elements");
            TreeSet<String> includeElements = new TreeSet<>();
            TreeSet<String> excludeElements = new TreeSet<>();
            SelectGenomeElementDialog dialog =
                    new SelectGenomeElementDialog(shell, partService, includeElements, excludeElements, genome);
            if (dialog.open() == 0) {
                listGenomeElements.clear();
                for (String row : includeElements) {
                    listGenomeElements.add(row);
                }
                tableGenes.removeAll();
                tableGenes.setItemCount(listGenomeElements.size());
                tableGenes.update();
                for (String row : includeElements) {
                    tableGenes.select(listGenomeElements.indexOf(row));
                }
                updateCoExpressionFigure();
            }
        } else if (e.getSource() == btnHelp) {
            HelpPage.helpNetworkView(partService);
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    public Network getGeneralNetwork() {
        return generalNetwork;
    }

    public void setGeneralNetwork(Network generalNetwork) {
        this.generalNetwork = generalNetwork;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genomeName) {
        this.genomeName = genomeName;
    }

    public Network getFilteredNetwork() {
        return filteredNetwork;
    }

    public void setFilteredNetwork(Network filteredNetwork) {
        this.filteredNetwork = filteredNetwork;
    }

    public Genome getGenome() {
        return genome;
    }

    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public Table getTableGenes() {
        return tableGenes;
    }

    public void setTableGenes(Table tableGenes) {
        this.tableGenes = tableGenes;
    }

    public Text getTextCutOff() {
        return textCutOff;
    }

    public void setTextCutOff(Text textCutOff) {
        this.textCutOff = textCutOff;
    }

    public TabItem getTbtmGraphVisualization() {
        return tbtmGraphVisualization;
    }

    public void setTbtmGraphVisualization(TabItem tbtmGraphVisualization) {
        this.tbtmGraphVisualization = tbtmGraphVisualization;
    }

//    public TabItem getTbtmRadialNetwork() {
//        return tbtmRadialNetwork;
//    }
//
//    public void setTbtmRadialNetwork(TabItem tbtmRadialNetwork) {
//        this.tbtmRadialNetwork = tbtmRadialNetwork;
//    }

    public TabFolder getTabFolder() {
        return tabFolder;
    }

    public void setTabFolder(TabFolder tabFolder) {
        this.tabFolder = tabFolder;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

}
