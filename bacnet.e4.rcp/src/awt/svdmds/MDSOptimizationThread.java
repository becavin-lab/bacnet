package awt.svdmds;

import java.util.Calendar;

import awt.table.MatrixUtils;






public class MDSOptimizationThread extends Thread{
	
	private double[][] bestPositionMatrix;
	private double bestStress=1;
	public int NUMBER_OF_TRANSIENT_STEP=1000;
	
	private boolean finish = false;
	
	private double[][] positionMatrix;
	private double[][] distanceMatrix;
	private MDSStressTool geoStress;
	
	
	public MDSOptimizationThread(double[][] positionMatrixInput, MDSStressTool geoStressInput){
		this.positionMatrix = positionMatrixInput;
		this.distanceMatrix = MatrixUtils.euclideanDistance(positionMatrix);
		this.geoStress = geoStressInput;
	}

	@Override
	public void run(){
		performGeo();
	}
	
	public void performGeo(){
		
						
		bestStress=1000000000;
		bestPositionMatrix=new double[positionMatrix.length][positionMatrix[0].length];
		
		// We initiate all our matrix
		MDSPhysicalTool geoPhysicalTool=new MDSPhysicalTool(positionMatrix,distanceMatrix,geoStress);
		geoPhysicalTool.K=geoPhysicalTool.K;
		geoPhysicalTool.F=geoPhysicalTool.K * 0.1;
		
		System.out.println("dim "+positionMatrix[0].length+" stress au depart: "+geoPhysicalTool.stressMinTemp);
		double initialStress=geoPhysicalTool.stressMinTemp;
			
		
			
		int time=0;		
		boolean equilibre = false;
		// We continue if we are in auto mode, and stop has not be clicked
		System.out.println("time: "+time);	
		
		
		/*
		 *  First we do some step for free
		 *  to run the modeling
		 */
		for(time=0;time<NUMBER_OF_TRANSIENT_STEP;time++){
			System.out.println("time: "+time);	
			//System.out.println(positionMatrix[0].length);
			Calendar cal = Calendar.getInstance();
	    	//System.out.println("timephys:"+ cal.getTime());
			geoPhysicalTool.oneStepOfPhysicalSimulation();
			//System.out.println("timedist:"+ cal.getTime());
			MatrixUtils.updateEuclideanDistance(positionMatrix, distanceMatrix);
			//System.out.println("calcstress:"+ cal.getTime());
			this.getGeoStress().saveStress(distanceMatrix);
			//System.out.println("timestress:"+ cal.getTime());
			
			if(this.getGeoStress().getCurrentStress() < bestStress){
				// then we save the bestMatrix
				for(int m=0;m<this.getPositionMatrix().length;m++){
					for(int n=0;n<this.getPositionMatrix()[0].length;n++){
						this.getBestPositionMatrix()[m][n]=this.getPositionMatrix()[m][n];
					}
				}
				bestStress=geoStress.getCurrentStress();
			}
			//System.out.println("time:"+ cal.getTime());
			
			if(Double.isNaN(geoStress.getCurrentStress())){
				this.interrupt();
			}
			
			
		}
		
		// then we search for the plateau
		while(!equilibre){
			System.out.println("time: "+time);	
			geoPhysicalTool.oneStepOfPhysicalSimulation();
			MatrixUtils.updateEuclideanDistance(positionMatrix, distanceMatrix);
			geoStress.saveStress(distanceMatrix);
			equilibre = geoPhysicalTool.systemIsInEquilibrium(time, this.getGeoStress().getCurrentStress() );
			
			
			if( this.getGeoStress().getCurrentStress() < bestStress){
				// then we save the bestMatrix
				for(int m=0;m<this.getPositionMatrix().length;m++){
					for(int n=0;n<this.getPositionMatrix()[0].length;n++){
						this.getBestPositionMatrix()[m][n]=this.getPositionMatrix()[m][n];
					}
				}
				bestStress=this.getGeoStress().getCurrentStress();
			}
			if(Double.isNaN(this.getGeoStress().getCurrentStress() )){
				this.interrupt();
			}
			time++;	
		}
		
		System.out.println(time);
		
		
		for(int m=0;m<positionMatrix.length;m++){
			for(int n=0;n<positionMatrix[0].length;n++){
				positionMatrix[m][n]=bestPositionMatrix[m][n];
			}
		}
		System.out.println("     Diff= "+(initialStress-bestStress));
		System.out.println("Final Stress: "+bestStress);
			
		finish = true;
	}

	public double[][] getBestPositionMatrix() {
		return bestPositionMatrix;
	}

	public void setBestPositionMatrix(double[][] bestPositionMatrix) {
		this.bestPositionMatrix = bestPositionMatrix;
	}

	public double getBestStress() {
		return bestStress;
	}

	public void setBestStress(double bestStress) {
		this.bestStress = bestStress;
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public double[][] getPositionMatrix() {
		return positionMatrix;
	}

	public void setPositionMatrix(double[][] positionMatrix) {
		this.positionMatrix = positionMatrix;
	}

	public double[][] getDistanceMatrix() {
		return distanceMatrix;
	}

	public void setDistanceMatrix(double[][] distanceMatrix) {
		this.distanceMatrix = distanceMatrix;
	}

	public MDSStressTool getGeoStress() {
		return geoStress;
	}

	public void setGeoStress(MDSStressTool geoStress) {
		this.geoStress = geoStress;
	}



	

}
