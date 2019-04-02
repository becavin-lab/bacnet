package bacnet.e4.rap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
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
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.utils.BasicColor;

public class SynTView implements SelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -4429010895850129342L;

    public static final String ID = "bacnet.e4.rap.SynTView";
    public static final String GRAPH_NAME = "Graph visualization";
    public static final String CIRCOS_NAME = "Radial visualization";


    private Browser browserGraph;
    private Button btnSelectGenomeElements;
    private Table tableGenes;
    private ArrayList<String> listGenomeElements = new ArrayList<>();
    private Genome genome;
    private Composite compositeTitle;

    private final String svgName = "CircosBackground.svg";
    private String genomeName;
    private Network generalNetwork;
    private Network filteredNetwork;
    private Label lblSearch;
    private Text txtSearch;
    private Button btnHelp;

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    public SynTView() {
        // TODO Auto-generated constructor stub
    }

    @PostConstruct
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(2, false));

        compositeTitle = new Composite(parent, SWT.NONE);
        compositeTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
        compositeTitle.setLayout(new GridLayout(2, false));

        Label lblXxSrnas = new Label(compositeTitle, SWT.BORDER | SWT.CENTER);
        lblXxSrnas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblXxSrnas.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
        lblXxSrnas.setText("SynTView");
        lblXxSrnas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        btnHelp = new Button(compositeTitle, SWT.NONE);
        btnHelp.setToolTipText("How to use Co-expression network panel ?");
        btnHelp.setImage(ResourceManager.getPluginImage("bacnet", "icons/help.png"));
        btnHelp.addSelectionListener(this);

        Composite compositeGeneselection = new Composite(parent, SWT.BORDER);
        compositeGeneselection.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2));
        compositeGeneselection.setLayout(new GridLayout(1, false));

        lblSearch = new Label(compositeGeneselection, SWT.NONE);
        lblSearch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblSearch.setText("Search");
        lblSearch.setForeground(BasicColor.GREY);
        txtSearch = new Text(compositeGeneselection, SWT.BORDER);
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnSelectGenomeElements = new Button(compositeGeneselection, SWT.NONE);
        btnSelectGenomeElements.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnSelectGenomeElements.setText("Select genome elements");
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

        browserGraph = new Browser(parent, SWT.BORDER);
        browserGraph.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        browserGraph.addProgressListener(new ProgressListener() {

            @Override
            public void completed(ProgressEvent event) {
                System.out.println("Load completed");
            }

            @Override
            public void changed(ProgressEvent event) {
                // System.out.println("Change performed "+event.);
            }
        });

        /*
         * Register the background SVG
         */
        try {
            System.out.println(svgName + "  -   " + Network.CIRCOS_BACK_PATH);
            if (!RWT.getApplicationContext().getResourceManager().isRegistered(svgName)) {
                RWT.getApplicationContext().getResourceManager().register(svgName,
                        new FileInputStream(Network.CIRCOS_BACK_PATH));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        updateCoExpression();
    }



    private void updateCoExpression() {
        /*
         * Working path
         */

        /*
         * Get the Tab open and refresh appropriate view
         */
        // System.out.println("display:"+tabFolder.getItem(tabFolder.getSelectionIndex()).getText());
        /*
         * Update Graph.html
         */
        // String path = InternalBrowser.getPath()+"TestJS.html";
        String pathGraphHTML = "http://listeriomics01.hosting.pasteur.fr/SynTView/flash/indexG2.html";
        // URL url = new URL(pathGraphHTML)
        // String htmlTextGraph = FileUtils.readText(pathGraphHTML);
        // browserGraph.setText(htmlTextGraph);
        browserGraph.setUrl(pathGraphHTML);
        browserGraph.redraw();


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

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnSelectGenomeElements) {
            System.out.println("Run script");
            try {
                // System.out.println(browserGraph.evaluate("search()"));
                // browserGraph.evaluate("search()");
                // browserGraph.evaluate("test()");
                // browserGraph.evaluate("test()");
                browserGraph.evaluate("search('lmo0200')");
                // System.out.println("execute(\"search(\"lmo0200\")");
                // browserGraph.update();
            } catch (SWTException eawd) {
                System.out.println(eawd.getMessage());
            }
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

}
