package awt.svdmds;



public class MDSPhysicalTool{
	
	private final double[][] force;
	private final double[][] vitesse;
	public final double[][] positionTempMatrix;
	public double[][] positionMatrix;
	public double[][] distanceMatrix;
	public MDSStressTool geoStress;
	
	public double stressMinTemp;
	public double K=1;
	public double F=0.1;
	
	
	// Constant for the physic algorithm
	private static double MASSE=5;
	private static double DELTA_T=0.002;
	private static double CARRE_DELTA_T=Math.pow(DELTA_T,2);
	
	//  Quality of the algorithm
	private static int NUMBER_OF_STEP_TO_WAIT = 100;
	private static double PRECISION_MAX=Math.pow(10,-4);
	private static double PRECISION_MIN=Math.pow(10,-17);
	
	public MDSPhysicalTool(double[][] positionMatrix,double[][] distanceMatrix,MDSStressTool geoStress){
		this.positionMatrix = positionMatrix;
		this.distanceMatrix = distanceMatrix;
		this.geoStress = geoStress;		
		
		positionTempMatrix=new double[positionMatrix.length][positionMatrix[0].length];
		for(int i=0;i<positionMatrix.length;i++){
			for(int j=0;j<positionMatrix[0].length;j++){
				positionTempMatrix[i][j]=positionMatrix[i][j];
			}
		}
		
		
		force=new double[positionMatrix.length][positionMatrix[0].length];
		vitesse=new double[positionMatrix.length][positionMatrix[0].length];
		
		// init the value of stress
		//geoStress.saveStress(distanceMatrix);
		stressMinTemp=geoStress.getCurrentStress();
		NUMBER_OF_STEP_TO_WAIT =100000/force.length;
		System.out.println("NUMBER_OF_STEP_TO_WAIT: "+NUMBER_OF_STEP_TO_WAIT);
	}
	

	public void oneStepOfPhysicalSimulation(){
/*
		 * We run the physics on all points (except the first fourth)
		 * it begins with pause clicked
		 * stop will end all, and fixed the minimal stress position
		 */
		
		//System.out.println("Calc force");
		calcForce();
		//MatrixUtils.displayMatrix("diffDistance : ",MatrixUtils.addition(1, distanceMatrixInit, -1, distanceMatrix));
		//System.out.println("update pos");
		updatePosition();
		
	}

	/**
	 * Calculation of force[currentPoint]
	 * We sum on all the point from 0 to currentPoint-1
	 * 
	 * @param positionMatrix
	 * @param distanceMatrix
	 * @param geoStress
	 */
	private void calcForce(){
		double result = 0;
		for(int i=0;i<this.getForce().length;i++){
			for(int j=0;j<this.getForce().length;j++){
				if(i!=j){
					//calc F_spring
					result=forceAndPotential(this.getDistanceMatrix()[i][j],this.getGeoStress().getReferenceMatrix()[i][j]);
					// Sum_force = Sum ( F_spring + F_frottement )
					for(int k=0;k<force[0].length;k++){	
						force[i][k]+=result*(positionMatrix[i][k]-positionMatrix[j][k])-F*vitesse[i][k];
						//System.out.println("i: "+i+" j: "+j+" k: "+k+" force "+force[i][k]);
					}
				}	
			}
		}
	}

	/**
	 * Position and velocity are calculated with Verlet algorithm
		 * See Wikipedia
	 *
	 * We update all the point
	 * 
	 * We also calculate Cinetic energy
	 * @param positionMatrix
	 */
	private void updatePosition(){
		double tempPos;
		for(int i=0;i<this.getPositionMatrix().length;i++){
			for(int k=0;k<this.getPositionMatrix()[0].length;k++){
				tempPos=this.getPositionMatrix()[i][k];
				MASSE = 5;
				this.getPositionMatrix()[i][k]=2*this.getPositionMatrix()[i][k]-this.getPositionTempMatrix()[i][k]+ this.getForce()[i][k]*CARRE_DELTA_T/MASSE;
				this.getVitesse()[i][k]=(this.getPositionMatrix()[i][k]-this.getPositionTempMatrix()[i][k])/(2*DELTA_T);
				this.getPositionTempMatrix()[i][k]=tempPos;
			}
		}
	}
	
	private double forceAndPotential(double d,double d_ij){
		//double[] result=new double[2];
		// We use a String potential
		double result = - K*(d-d_ij);  // Force
		//result[1]=0.5*getK()*(d-d_ij)*(d-d_ij); //Energy
		return result;
	}
	
	
	
	public boolean systemIsInEquilibrium(int timer,double currentStress){	
		// Test of the gradient
		if(Double.isNaN(currentStress)){
			return true;
		}
		
		// If the gradient is bellow precision we wait 100=numberOfStepToWait step, in case there's a bigger gradient if the system is run
		if(Math.abs(stressMinTemp-currentStress)<PRECISION_MAX){  // we are on the plateau
			if(timer<NUMBER_OF_STEP_TO_WAIT ){
				// the case when stressMin is very near stress : diff = 0
				if(Math.abs(stressMinTemp-currentStress)<PRECISION_MIN){
					timer=0;
					if(stressMinTemp<0.6) return true;
					else return false;
					
				}
				timer++;
				return false;
				
			}
			else {
				System.out.println("fin stepwait machin");
				timer=0;
				return true;
			}
		}
		else {
			if(stressMinTemp-currentStress>0){
				// the stress is decreasing so we continue the physic
				timer=0;
				stressMinTemp=currentStress;
				return false;
			}
			else {
				// the stress is increasing so we stop the physic
				//System.out.println("increasing stress" + timer + " diff "+Math.abs(stressMinTemp-stress));
				System.out.println("          Stress increase");
				timer=0;
				if(currentStress<0.5) return true;
				else return false;
			}
		}
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


	public double getStressMinTemp() {
		return stressMinTemp;
	}


	public void setStressMinTemp(double stressMinTemp) {
		this.stressMinTemp = stressMinTemp;
	}


	public double getK() {
		return K;
	}


	public void setK(double k) {
		K = k;
	}


	public double getF() {
		return F;
	}


	public void setF(double f) {
		F = f;
	}


	public double[][] getForce() {
		return force;
	}


	public double[][] getVitesse() {
		return vitesse;
	}


	public double[][] getPositionTempMatrix() {
		return positionTempMatrix;
	}
	
}
