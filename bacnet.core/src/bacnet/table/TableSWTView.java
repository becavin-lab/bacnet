package bacnet.table;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.swt.ResourceManager;

public class TableSWTView {

    public static final String ID = "bacnet.TableSWTView"; //$NON-NLS-1$

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    private TableSWTComposite tableComposite;

    @Inject
    public TableSWTView() {}

    /**
     * Create contents of the view part.
     * 
     * @param parent
     */
    @PostConstruct
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        tableComposite = new TableSWTComposite(parent, SWT.NONE, shell, partService);
        tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    }

    public void setData(ExpressionMatrix matrix, Genome genome) {
        tableComposite.initData(matrix, genome);
        tableComposite.update();
    }

    /**
     * Open a HeatMapTranscriptomicsView using EPartService and an id
     * 
     * @param partService
     * @param id = identifier of the view created
     */
    public static void openView(EPartService partService, String id) {
        MPart part = partService.createPart(TableSWTView.ID);
        part.setElementId(id);
        partService.showPart(part, PartState.ACTIVATE);
    }

    /**
     * Static function which open an HeatMapView and display corresponding ExpressionMatrix
     * 
     * @param matrix
     * @param viewName
     * @throws PartInitException
     */
    public static void displayMatrix(ExpressionMatrix matrix, Genome genome, String viewName, EPartService partService) {
        displayMatrix(matrix, genome, viewName, false, "", partService);
    }

    public static void displayMatrix(ExpressionMatrix matrix, Genome genome, String viewName, boolean saveSVG, String fileName,
            EPartService partService) {
        String id = TableSWTView.ID + Math.random();
        // initiate view
        ResourceManager.openView(partService, TableSWTView.ID, id);
        // update data
        MPart part = partService.findPart(id);
        TableSWTView view = (TableSWTView) part.getObject();
        view.setData(matrix, genome);
    }

    public TableSWTComposite getTableComposite() {
        return tableComposite;
    }

    public void setTableComposite(TableSWTComposite tableComposite) {
        this.tableComposite = tableComposite;
    }

}
