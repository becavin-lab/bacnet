package bacnet.handler;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.table.TableSWTView;
import bacnet.utils.FileUtils;

public class LoadExpressionMatrixHandler {

	@Inject
	EPartService partService;

	@Execute
	public void execute(IWorkbench workbench, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		fd.setText("Open an expression Matrix: ");
		String path = fd.open();
		if (path != null) {
			File file = new File(path);
			if (file.exists()) {
				String[] fileNames = fd.getFileNames();
				path = FileUtils.getPath(path);

				for (String fileName : fileNames) {
					fileName = FileUtils.removePath(fileName);
					System.out.println(path + fileName);
					ExpressionMatrix matrix = ExpressionMatrix.loadTab(path + fileName, true);
					System.out.println("Loaded:" + path + fileName);
					TableSWTView.displayMatrix(matrix, fileName, partService);

				}
			}
		}

	}

}
