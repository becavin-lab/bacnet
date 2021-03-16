package awt.svdmds;

import awt.table.MatrixUtils;
import awt.table.ModelProviderRCP;
import bacnet.datamodel.dataset.ExpressionMatrix;



public class MDSThread extends Thread {

	private ExpressionMatrix matrix;

	public MDSThread(ExpressionMatrix matrix){
		this.matrix = matrix;
	}
	
	public void run(){
		
		double[][]  positionMatrix = matrix.getValues();
		System.out.println("run: SVD-MDS");
		
		InitThread initThread = new InitThread(positionMatrix);
		initThread.run();
		positionMatrix = initThread.positionMatrix;
		
		
        initThread.geoStress.saveStress(MatrixUtils.euclideanDistance(positionMatrix));
		double beginthread = initThread.geoStress.getCurrentStress();
        System.out.println("Begin stress: "+beginthread);
						
		ReductionThread reducThread = new ReductionThread(positionMatrix, initThread.geoStress);
		int time = reducThread.run();
			//reducThread.geoStress.saveStress(MatrixUtils.euclideanDistance(reducThread.positionMatrix));
		System.out.println("End stress: "+reducThread.mdsStress.getCurrentStress());
		System.out.println("SVD-MDS finished in "+time+" steps");
		
		//ArrayUtils.displayMatrix("dd", positionMatrix);
		matrix.setValues(positionMatrix);
		matrix.setNote(initThread.geoStress.getCurrentStress()+";"+beginthread);
		matrix.getHeaders().clear();
		matrix.addHeader("Axe1");
		matrix.addHeader("Axe2");
		
		ModelProviderRCP.INSTANCE.setMatrix(matrix);
		
		System.out.println("finish");
		
	}
	
	
}
