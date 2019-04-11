package bacnet.scripts.blast.gui;

import java.lang.reflect.InvocationTargetException;
import org.biojava3.core.exceptions.CompoundNotFoundError;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import bacnet.Database;
import bacnet.scripts.blast.BlastResultView;
import bacnet.swt.ResourceManager;
import bacnet.utils.FileUtils;

public class BlastWizard extends Wizard {

    private BlastQueryPage page;
    private BlastGenomeSelectionPage page2;
    // private BlastParametersPage page3;

    private Shell shell;
    private EPartService partService;

    public BlastWizard(Shell parentShell, EPartService partService) {
        this.shell = parentShell;
        this.partService = partService;
        setWindowTitle("Blast wizard");
        page = new BlastQueryPage(shell);
        page2 = new BlastGenomeSelectionPage();
        // page3 = new BlastParametersPage();
    }

    @Override
    public void addPages() {
        addPage(page);
        addPage(page2);
        // addPage(page3);
    }

    @Override
    public boolean performFinish() {
        // read the query sequence and test if it is a fasta file
        String fileName = Database.getTEMP_PATH() + "tempSeq.txt";

        FileUtils.saveText(page.getTextQuery().getText(), fileName);
        try {
            int type = 0;
            // if(page.btnProteinBlast.getSelection()) type=1;
            // if(page.btnBlastx.getSelection()) type = 2;
            runBlast(type);
            ResourceManager.openView(partService, BlastResultView.ID, BlastResultView.ID + Math.random());

        } catch (CompoundNotFoundError e1) {
            System.err.println("The query sequence cannot be read");
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    public void runBlast(int typeT) {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(false, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) {
                    page2.getSelectedGenomes();
                }
            });
        } catch (InvocationTargetException e) {
            System.err.println("Problem in BLast");
        } catch (InterruptedException e) {
            System.err.println("Problem in BLast");
        }
    }

}
