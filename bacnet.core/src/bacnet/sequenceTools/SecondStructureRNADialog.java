package bacnet.sequenceTools;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import bacnet.swt.SWTResourceManager;

public class SecondStructureRNADialog extends Dialog {

    /**
     * 
     */
    private static final long serialVersionUID = -8357071260167781918L;
    private String fileName = "";

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public SecondStructureRNADialog(Shell parentShell, String fileName) {
        super(parentShell);
        setShellStyle(SWT.BORDER | SWT.RESIZE);
        this.fileName = fileName;
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        ScrolledComposite scrolledComposite =
                new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Label lblImage = new Label(composite, SWT.NONE);
        lblImage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        lblImage.setSize(10, 10);
        lblImage.setText("Image");

        lblImage.setImage(SWTResourceManager.getImage(fileName));
        scrolledComposite.setContent(composite);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        return container;
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

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(798, 503);
    }

}
