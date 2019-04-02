package bacnet.genomeBrowser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.proteomics.NTerm;

public class CompositeNTermInfo extends Composite {

    private final Label lblPosition;
    private final Label lblSize;
    private final Label lblType;
    private final Label lblModifv;
    private final Label lblSpectrav;
    private final Text txtSequence;
    private final Label lblMaxquant;
    private Label lblOverlap;

    public CompositeNTermInfo(Composite parent, int style) {
        super(parent, style);
        GridData gd_cmpNTermInfo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        this.setLayoutData(gd_cmpNTermInfo);
        this.setLayout(new GridLayout(1, false));

        lblModifv = new Label(this, SWT.NONE);
        GridData gd_lblModifv = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblModifv.widthHint = 62;
        lblModifv.setLayoutData(gd_lblModifv);
        lblModifv.setText("modifv");

        lblPosition = new Label(this, SWT.NONE);
        GridData gd_lblPosition = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
        gd_lblPosition.widthHint = 72;
        lblPosition.setLayoutData(gd_lblPosition);
        lblPosition.setText("begin");

        lblSize = new Label(this, SWT.NONE);
        lblSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblSize.setText("size");

        lblType = new Label(this, SWT.NONE);
        lblType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        lblType.setText("Type");

        lblOverlap = new Label(this, SWT.NONE);
        lblOverlap.setText("Overlap");

        lblSpectrav = new Label(this, SWT.NONE);
        lblSpectrav.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblSpectrav.setText("spectrav");

        lblMaxquant = new Label(this, SWT.NONE);
        lblMaxquant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblMaxquant.setText("MaxQuant:");

        txtSequence = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
        txtSequence.setText("sequence");
        GridData gd_txtSequence = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_txtSequence.heightHint = 30;
        txtSequence.setLayoutData(gd_txtSequence);
    }

    /**
     * Set all Labels for NTerm info
     */
    public void setNTermInfo(NTerm nTerm, NTermData massSpecData) {
        lblModifv.setText(nTerm.getTypeModif() + " - " + nTerm.getName());
        lblMaxquant.setText("MaxQuant: " + nTerm.getMaxQuant());
        lblPosition.setText(nTerm.getBegin() + " .. " + nTerm.getEnd() + "  (" + nTerm.getStrand() + ")");
        lblSize.setText((nTerm.getLength() / 3) + " aa");
        lblType.setText(nTerm.getTypeOverlap());
        lblOverlap.setText(nTerm.getOverlap());
        lblSpectrav.setText(
                "Spectr # " + nTerm.getSpectra() + " Thr " + nTerm.getThreshold() + " Score " + nTerm.getScore());
        txtSequence.setText(nTerm.getSequenceMap());
    }

}
