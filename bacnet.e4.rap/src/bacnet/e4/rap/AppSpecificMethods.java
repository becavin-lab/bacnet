package bacnet.e4.rap;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import bacnet.Database;

/***
 * All the methods which are specific to a webpage
 * 
 *
 * @author Christophe Bécavin
 *
 */
public class AppSpecificMethods {

    public static void modifyApp(String viewID, EPartService partService) {
        String appName = Database.getInstance().getProjectName();
        if (appName.equals("CRISPRGo")) {
            if (viewID.equals(BannerView.ID)) {
                MPart part = partService.findPart(viewID);
                @SuppressWarnings("unused")
                BannerView view = (BannerView) part.getObject();
                // System.out.println(view.getClass());
                // view.getToolBar().dispose();
            }

        }
    }

    /**
     * Push states to change url
     */
    public boolean openPasswordDialog(Shell shell) {
        InputDialog dialog =
                new InputDialog(shell, "Type password", "Need to type a password to enter website", "", null);
        if (dialog.open() == 0) {
            // System.out.println(dialog.getValue());
            String password = "";
            if (Database.getInstance().getProjectName() == Database.UIBCLISTERIOMICS_PROJECT) {
                password = "";
            } else if (Database.getInstance().getProjectName() == Database.LEISHOMICS_PROJECT) {
                password = "";
            }

            if (dialog.getValue().equals(password)) {
                System.out.println("Congratulation you can enter the gate!");
                return true;
            } else {
                openPasswordDialog(shell);
            }
        } else {
            openPasswordDialog(shell);
        }
        return false;
    }
}
