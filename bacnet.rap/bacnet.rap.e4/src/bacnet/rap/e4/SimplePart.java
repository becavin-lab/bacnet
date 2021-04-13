package bacnet.rap.e4;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SimplePart {

    @PostConstruct
    void init(MPart part, Composite parent) {
       parent.setLayout(new GridLayout());
       Label label = new Label(parent, SWT.NONE);
       label.setText("Hello e4 World");

    }

}
