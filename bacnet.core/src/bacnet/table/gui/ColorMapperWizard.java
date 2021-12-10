package bacnet.table.gui;

import java.util.ArrayList;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import bacnet.table.core.ColorMapper.TypeMapper;
import bacnet.table.core.ColorMapperList;

public class ColorMapperWizard extends Wizard {

    private ArrayList<ColorMapperWizardPage> wizardPages = new ArrayList<ColorMapperWizardPage>();
    private ColorMapperList colorMapper = new ColorMapperList();
    private ColorMapperList colorMapperPrevious = new ColorMapperList();

    private Shell shell;

    public ColorMapperWizard(ColorMapperList colorMapper, Shell shell) {
        setWindowTitle("Color Mapper settings");
        this.shell = shell;
        this.colorMapper = colorMapper;
        colorMapperPrevious = colorMapper.clone();
    }

    @Override
    public void addPages() {
        for (TypeMapper type : colorMapper.getColorMappers().keySet()) {
            ColorMapperWizardPage wizardPage =
                    new ColorMapperWizardPage(colorMapper.getColorMappers().get(type), shell);
            addPage(wizardPage);
            wizardPages.add(wizardPage);
        }
    }

    @Override
    public boolean performFinish() {
        for (IWizardPage pageTemp : this.getPages()) {
            ColorMapperWizardPage page = (ColorMapperWizardPage) pageTemp;
            colorMapper.getColorMappers().put(page.getColorMapper().getType(), page.getColorMapper());
        }
        dispose();
        return true;
    }

    @Override
    public boolean performCancel() {
        colorMapper = colorMapperPrevious;
        this.dispose();
        return true;
    }

    public ColorMapperList getColorMapper() {
        return colorMapper;
    }

}
