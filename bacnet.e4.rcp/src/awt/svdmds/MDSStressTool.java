package awt.svdmds;

import java.util.ArrayList;
import java.util.List;

import bacnet.datamodel.dataset.ExpressionMatrix;



public class MDSStressTool {
	
	/**
	 *   @param  referenceMatrix
	 *         Matrix=double[][] used for kruskal stress calculation
	 *         
	 *   @param inertia
	 *   	 Vector of inertia which may be corelate to stress decreasing
	 *         
	 *   @param  stressEvolution_all
	 *         A set of list :    _sIndex is for time
	 *         				_ is for stress
	 *         				_Derivative is for derivative stress
	 *         				_Inertia is the value of inertia of the current deleted dimension
	 *   @param currentStress
	 *   	Value of stress updated by kruskal and productStress methods
	 *   
	 */
	
	
	private double[][] referenceMatrix;

	private List<Double> stressEvolution = new ArrayList<Double>();
	private List<Integer> stressEvolutionIndex = new ArrayList<Integer>();
	private List<Double> stressEvolutionInertia = new ArrayList<Double>();
	private List<Double> stressEvolutionDerivative = new ArrayList<Double>();
	private List<Double> stressEvolutionSecondDerivative = new ArrayList<Double>();
	
//	private double[] inertia;
//	private int inertiaPosition=0;
//	private int inertiaStressPosition=0;
//	public List stressEvolutionProbe = new ArrayList();
//	private double currentStressProbe=-1;
////	private double[][] transfoMatrix;
//	private double[][] refProbeMatrix;
//	private double distanceProbeSum=0;
	
	private double currentStress=-1;
	private double scaleStress=-1;
	
	
	public void initiateReferenceMatrix(double[][] initMatrix){
		referenceMatrix=initMatrix.clone();
		scaleStress=0;
		for(int i = 0 ; i < referenceMatrix.length; i ++){
			for(int j = 0 ; j < referenceMatrix[0].length ; j ++){
				scaleStress += Math.pow(referenceMatrix[i][j],2);
			}
		}
	}
//	public void initiateInertia(double[] initInertia){
//		inertia=initInertia;
//	}
	// when we delete a new dimension we change our position in the inertia vector
//	public void newDimension(){
//		inertiaPosition++;
//		inertiaStressPosition++;
//	}
//	public void newDeletion(){
//		inertiaStressPosition++;
//	}
	
	public void saveStress(double[][] matrix){
		kruskalStress(matrix);
		//System.out.println(currentStress);
		stressEvolution.add(Double.valueOf(currentStress));
		stressEvolutionIndex.add(Integer.valueOf(stressEvolution.size()));
//		stressEvolutionInertia.add(Double.valueOf(inertia[inertia.length-1-inertiaPosition]*100));
//		if(currentStressProbe==-1){
//			stressEvolutionProbe.add(currentStressProbe);	
//		}
	}

	
	public double kruskalStress(double[][] matrix){
		double stress=0;
		for(int i = 0 ; i < referenceMatrix.length; i ++){
			for(int j = 0 ; j < referenceMatrix[0].length ; j ++){
				stress += (matrix[i][j]-referenceMatrix[i][j]) * (matrix[i][j]-referenceMatrix[i][j]);
			}
		}
		currentStress=Math.sqrt(stress/scaleStress);
		return currentStress;	
	}
	
	public double ProductStress(double[][] productMatrix){
		double stress=0;
		double scale = 0;
		for(int i = 0 ; i < referenceMatrix.length; i ++){
			for(int j = 0 ; j < i ; j ++){
				stress += (productMatrix[i][j]-referenceMatrix[i][j])*(productMatrix[i][j]-referenceMatrix[i][j]);
				scale += referenceMatrix[i][j]*referenceMatrix[i][j];
			}
		}
		currentStress=Math.sqrt(stress/scale);
		return currentStress;	
	}
	
	public ExpressionMatrix saveInATable(){
		ExpressionMatrix stress = new ExpressionMatrix();
		
		String[] temp = {"Index","Stress","DStress","DDStress"};
		for(String header:temp) stress.getHeaders().add(header);
		
		double[][]	values = new double[stressEvolutionIndex.size()][3];
		for(int i=0;i<stressEvolutionIndex.size();i++){
			values[i][1]=stressEvolution.get(i);
			values[i][2]=stressEvolutionDerivative.get(i);
			values[i][3]=stressEvolutionSecondDerivative.get(i);
			stress.getRowNames().put(stressEvolutionIndex.get(i)+"", i);
		}
		stress.setValues(values);
		
		return stress;
	}
	

	
	/*
	 * we calculate the derivative function of the stress
	 * 
	 * deriv =  f(i+1)-f(i-1) / 2
	 * 
	 */ 
	
	public void derivativeStressCalcul(){
		stressEvolutionDerivative.add(Double.valueOf(0.0));
		// First position is set, we set the other n-1 positions
		double derivative=0;
		for(int i=1;i<stressEvolution.size()-1;i++){
			derivative=stressEvolution.get(i+1).doubleValue()-stressEvolution.get(i-1).doubleValue();
			derivative=derivative/2;
			stressEvolutionDerivative.add(Double.valueOf(derivative));
		}
		// For last value we use a different calcul : deriv=f(i)-f(i-1)
		derivative=stressEvolution.get(stressEvolution.size()-1).doubleValue()-stressEvolution.get(stressEvolution.size()-1).doubleValue();
		stressEvolutionDerivative.add(Double.valueOf(derivative));
	}
	
	public void secondDerivativeStressCalcul(){
		stressEvolutionSecondDerivative.add(Double.valueOf(0.0));
		// First position is set, we set the other n-1 positions
		double derivative=0;
		for(int i=1;i<stressEvolutionDerivative.size()-1;i++){
			derivative=stressEvolutionDerivative.get(i+1).doubleValue()-stressEvolutionDerivative.get(i-1).doubleValue();
			derivative=derivative/2;
			stressEvolutionSecondDerivative.add(Double.valueOf(derivative));
		}
		// For last value we use a different calcul : deriv=f(i)-f(i-1)
		derivative=stressEvolutionDerivative.get(stressEvolutionDerivative.size()-1).doubleValue()-stressEvolutionDerivative.get(stressEvolutionDerivative.size()-1).doubleValue();
		stressEvolutionSecondDerivative.add(Double.valueOf(derivative));
	}

	public double[][] getReferenceMatrix() {
		return referenceMatrix;
	}

	public void setReferenceMatrix(double[][] referenceMatrix) {
		this.referenceMatrix = referenceMatrix;
	}

	public List<Double> getStressEvolution() {
		return stressEvolution;
	}

	public void setStressEvolution(List<Double> stressEvolution) {
		this.stressEvolution = stressEvolution;
	}

	public List<Integer> getStressEvolutionIndex() {
		return stressEvolutionIndex;
	}

	public void setStressEvolutionIndex(List<Integer> stressEvolutionIndex) {
		this.stressEvolutionIndex = stressEvolutionIndex;
	}

	public List<Double> getStressEvolutionInertia() {
		return stressEvolutionInertia;
	}

	public void setStressEvolutionInertia(List<Double> stressEvolutionInertia) {
		this.stressEvolutionInertia = stressEvolutionInertia;
	}

	public List<Double> getStressEvolutionDerivative() {
		return stressEvolutionDerivative;
	}

	public void setStressEvolutionDerivative(List<Double> stressEvolutionDerivative) {
		this.stressEvolutionDerivative = stressEvolutionDerivative;
	}

	public List<Double> getStressEvolutionSecondDerivative() {
		return stressEvolutionSecondDerivative;
	}

	public void setStressEvolutionSecondDerivative(
			List<Double> stressEvolutionSecondDerivative) {
		this.stressEvolutionSecondDerivative = stressEvolutionSecondDerivative;
	}

	public double getCurrentStress() {
		return currentStress;
	}

	public void setCurrentStress(double currentStress) {
		this.currentStress = currentStress;
	}

	public double getScaleStress() {
		return scaleStress;
	}

	public void setScaleStress(double scaleStress) {
		this.scaleStress = scaleStress;
	}
	
	////////////////////////////////////
	//////    Probe stress calcul
	////////////////////////////////////
	
//	public void initiateRefProbeMatrix(double[][] probeMatrix){
//		refProbeMatrix=probeMatrix;
//		distanceProbeSum=0;
//		for(int i=0;i<refProbeMatrix.length;i++){
//			for(int j=0;j<i;j++){
//				for(int k=0;k<refProbeMatrix[0].length;k++){
//					distanceProbeSum+=Math.pow(refProbeMatrix[i][k]-refProbeMatrix[j][k],2);
//				}
//			}
//		}
//		System.out.println("scale: " +distanceProbeSum);
//	}
//	
//	
//	public void kruskalStressProbe(double[][] matrix){
//		// calcul of the new matrix X = refProbeMatrix.times(matrix)
//		double[][] Xnew=new double[refProbeMatrix.length][matrix[0].length];
//		for(int i=0;i<Xnew.length;i++){
//			for(int j=0;j<Xnew[0].length;j++){
//				double temp=0;
//				for(int k=0;k<Xnew[0].length;k++){
//					temp+=refProbeMatrix[i][k]*matrix[k][j];
//				}
//				Xnew[i][j]=temp;
//			}
//		}
//		
//		/*
//		 * we want to compare distance in Xnew to distance in refProbeMatrix
//		 * We can't save distance matrix because they are too big
//		 * so we calculate stress term by term, without distance matrix saving
//		 */ 
//		currentStressProbe=0;
//		for(int i =0;i<Xnew.length;i++){
//			for(int j=0;j<i;j++){
//				// calcul of d_ij
//				double dij=0;
//				for(int k=0;k<Xnew[0].length;k++){
//					dij+=Math.pow(Xnew[i][k]-Xnew[j][k],2);
//				}
//				dij=Math.sqrt(dij);
//				// calcul of delta_ij
//				double deltaij=0;
//				for(int k=0;k<refProbeMatrix[0].length;k++){
//					deltaij+=Math.pow(refProbeMatrix[i][k]-refProbeMatrix[j][k],2);
//				}
//				deltaij=Math.sqrt(deltaij);
//				// compare both value
//				currentStressProbe+=Math.pow(dij-deltaij, 2);
//			}
//		}
//		
//		// final result is now
//		currentStressProbe=currentStressProbe/distanceProbeSum;	
//		System.out.println("scale: " +currentStressProbe);
//	}

}
