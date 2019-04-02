package awt.svdmds;





public class ReductionThread {
	
	public double[][] positionMatrix;
	public MDSStressTool mdsStress;
	
	public ReductionThread(double[][] positionMatrix,MDSStressTool geoStress){
			this.positionMatrix = positionMatrix;
			this.mdsStress = geoStress;
	}
	

	public int run(){
		
		// Then we run Least square scaling
		System.out.println("Least square scaling...");
		MDSOptimizationThread geoLeastSquareTool = new MDSOptimizationThread(positionMatrix, mdsStress);
		int numberOfDimRepr=2;
		
		///////////////////////////// real algorithm  ////////////////
		double[][] pos = null;
		geoLeastSquareTool.performGeo();   // MDS, MD-MDS in automode
		pos=geoLeastSquareTool.getPositionMatrix();
		
		System.out.println("dim pos: "+pos[0].length);
			
	
		// We calculate derivative stress and fill geoData with stress
		mdsStress.derivativeStressCalcul();
		mdsStress.secondDerivativeStressCalcul();
		
		

		return 0;
	}
		
	
}
