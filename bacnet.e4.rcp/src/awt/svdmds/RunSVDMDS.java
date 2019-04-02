package awt.svdmds;

import org.eclipse.e4.ui.workbench.modeling.EPartService;

import awt.graphics.SVDMDSView;
import awt.table.ModelProviderRCP;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.swt.ResourceManager;

public class RunSVDMDS {

	public static int MAX_PROBES_FOR_PROBES_REPR = 5001; 
	
	public static void run(ExpressionMatrix matrix, EPartService partService){
		ExpressionMatrix matrix2 = matrix.transpose();
		MDSThread thread = new MDSThread(matrix);
		thread.run();
		matrix = ModelProviderRCP.INSTANCE.getMatrix();
		ResourceManager.openView(partService, SVDMDSView.ID, SVDMDSView.ID+Math.random());	
	}

}
