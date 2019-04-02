package awt.table;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.utils.FileUtils;

public class FusionExpressionMatrixHandler {

	@ Inject
	EPartService partService;

	@Execute
	public void execute(IWorkbench workbench,@Named (IServiceConstants.ACTIVE_SHELL) Shell shell) {
		FileDialog fd = new FileDialog(shell, SWT.OPEN |SWT.MULTI);
		fd.setText("Open an expression Matrix: ");
		fd.setFilterPath(Database.getInstance().getPATH());
		String[] filterExt = {"*.txt","*.*" };
		fd.setFilterExtensions(filterExt);
		String path = fd.open();
		String[] fileNames = fd.getFileNames();
		path = FileUtils.getPath(path);
		ArrayList<ExpressionMatrix> matrices = new ArrayList<ExpressionMatrix>();
		for(String fileName : fileNames){
			ExpressionMatrix matrix = ExpressionMatrix.loadTab(path+fileName,false);
			matrices.add(matrix);	
		}
		ExpressionMatrix matrix = ExpressionMatrix.fusion(matrices, false);
		matrix.saveTab(Database.getInstance().getPATH()+"FusionOfMatrices.txt", "GenomeElements");
		TableAWTView.displayMatrix(matrix, "Fusion of different matrices",partService);
	}
}
