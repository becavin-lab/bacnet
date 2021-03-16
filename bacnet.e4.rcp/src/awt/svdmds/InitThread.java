package awt.svdmds;

import Jama.Matrix;
import awt.svdmds.SVDTool.Metric;
import awt.table.MatrixUtils;
import bacnet.utils.ArrayUtils;

public class InitThread {
	
	public double[][] positionMatrix;
	public MDSStressTool geoStress;
	public SVDTool.Metric metric = Metric.CORR;
	public int nbOfProbes = 0;
	public int nbOfPoints = 0;
	
	public int numberOfDimRepr = 2;
	
	
	public InitThread(double[][] positionMatrix){
		this.positionMatrix = positionMatrix;
		nbOfPoints = positionMatrix.length;
		nbOfProbes = positionMatrix[0].length;
	}
	
	public void run() {
			
		///////////////////////////////////
		// Get or create the inputMatrix //
		///////////////////////////////////
		
		System.out.println("nbOfPoints: " + nbOfPoints);
		System.out.println("nbOfProbes: " + nbOfProbes);
		
			
		
		// /////////////////////
		// ////// First part : reduction from n to n-1 with SVD
		// /////////////////////
		
		System.out.println("Performing SVD...");

		SVDData svdData = new SVDData();
		SVDTool svdTool = new SVDTool();
		// Stress initialisation
		geoStress = new MDSStressTool();

		Matrix X = new Matrix(positionMatrix);
		
		switch (metric) {
			case COV: // Covariance part
				X = MatrixUtils.centerColumn(X);
				break;
			case CORR: // Correlation part
				X = MatrixUtils.centerColumn(X);
				X = SVDTool.rescaleXCorrelation(X);
				break;
			case CORRESP: // // Correspondence part
				X = SVDTool.rescaleXCorrespondence(X);
				break;
		}
		
		// initiate reference matrix for stress
		geoStress.initiateReferenceMatrix(MatrixUtils.euclideanDistance(X));
		
		
		// We ran SVD
		svdTool.performSVD(X);
		double[] inertie = svdTool.inertie;
		svdData.singularValue = svdTool.singularValue;
		positionMatrix = svdTool.Xnew;

//		geoStress.initiateInertia(inertie);
//		geoStress.saveStress(MatrixUtils.euclideanDistance(positionMatrix));

		// We rescale data to make it more accurate for MDS
		positionMatrix = ArrayUtils.rescaleData(positionMatrix, 6);
				
		// test and Save stress
//		geoStress.initiateInertia(inertie);
//		geoStress.saveStress(MatrixUtils.euclideanDistance(positionMatrix));

		
		
		// /////////////////////
		// ////// Second part : reduction from n-1 to 3 & 2
		// /////////////////////

		// Euclidean distance calculus for initiate stress calcul
		geoStress.initiateReferenceMatrix(MatrixUtils.euclideanDistance(positionMatrix));
		
		System.out.println("AfterRescale and update of reference matrix " + geoStress.kruskalStress(MatrixUtils.euclideanDistance(positionMatrix)));
		
		int ndim=positionMatrix[0].length;	
		ndim=ndim-numberOfDimRepr;
		for(int i=0;i<ndim;i++){
			positionMatrix = ArrayUtils.eliminateColumn(positionMatrix);
		}
			
		// we center the data
		positionMatrix = ArrayUtils.centerColumn(positionMatrix);



	}
	
}
