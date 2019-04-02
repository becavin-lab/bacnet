package awt.table.wizards;

import java.util.ArrayList;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import bacnet.table.core.ColorMapperRCP.Type;
import bacnet.table.core.ColorMapperRCPList;

public class ColorMapperRCPWizard extends Wizard {

	private ArrayList<ColorMapperRCPWizardPage> wizardPages = new ArrayList<ColorMapperRCPWizardPage>();
	private ColorMapperRCPList colorMapper = new ColorMapperRCPList();
	private ColorMapperRCPList colorMapperPrevious = new ColorMapperRCPList();

	private Shell shell;

	public ColorMapperRCPWizard(ColorMapperRCPList colorMapper,Shell shell) {
		setWindowTitle("Color Mapper");
		this.shell = shell;
		this.colorMapper = colorMapper;
		colorMapperPrevious = colorMapper.clone();
	}

	@Override
	public void addPages() {
		for(Type type : colorMapper.getColorMappers().keySet()){
			ColorMapperRCPWizardPage wizardPage = new ColorMapperRCPWizardPage(colorMapper.getColorMappers().get(type),shell);
			addPage(wizardPage);
			wizardPages.add(wizardPage);
		}
	}

	@Override
	public boolean performFinish() {
		for(IWizardPage pageTemp : this.getPages()){
			ColorMapperRCPWizardPage page = (ColorMapperRCPWizardPage)pageTemp;
			colorMapper.getColorMappers().put(page.getColorMapper().getType(),page.getColorMapper());			
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

	public ColorMapperRCPList getColorMapper(){
		return colorMapper;
	}

}
