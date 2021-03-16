package awt.svdmds;


import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import awt.table.MatrixUtils;
import bacnet.utils.ArrayUtils;
import bacnet.utils.VectorUtils;

/**
 * Singular value decomposition class
 *   The SVD will solve :   X=USV^t
 *  
 * @param SVDData
 * 		data for your tool
 * @param dataMatrix
 * 		a double[][] for the input data
 * @param type
 * 		if type==0 perform a full SVD, if type==1 just center and reoriente (normal PCA)
 * @author Christophe Becavin
 */

public class SVDTool {
	
	public double[][] Xnew;
	
	public double[][] eigenMatrix;
	public double[][] eigenMatrixY;
	
	
	public double[] singularValue;
	public double[] inertie;
	
	private double[] qualityControl;
	
	public static enum Metric{
		COV,CORR,CORRESP;
	}
	
	public double[] performSVD(Matrix X){
		
		qualityControl=new double[2];
		eigenMatrix=new double[0][0];
		Matrix product;
		Matrix U,V,S,Sinv;
		
		
//		GEOStressTool stressN=new GEOStressTool();
//		stressN.initiateReferenceMatrix(MatrixUtils.euclideanDistance(X));
//		System.out.println("before "+stressN.KruskalStress(MatrixUtils.euclideanDistance(X.getArray())));
		//X=MatrixUtils.centerRow(X);
//		System.out.println("middle "+stressN.KruskalStress(MatrixUtils.euclideanDistance(X.getArray())));
		
		// First we need to center our data
		//X=MatrixUtils.centerColumn(X);
//		System.out.println("ned "+stressN.KruskalStress(MatrixUtils.euclideanDistance(X.getArray())));
		
		Matrix tempS=new Matrix(0,0);
		Matrix tempU=new Matrix(0,0);
		Matrix tempV=new Matrix(0,0);
		// if we have a low number of row compare to number of column we perform SVD using innerproduct
		// Else we perform SVD using outerProduct
		if(X.getRowDimension()<X.getColumnDimension()){
			//  We calculate the innerProduct
			product = MatrixUtils.innerProduct(X);
			// And then we find the singularvalue and vector 
			// innerproduct is smaller then outer so we use it to find the eigen-value and vector.
			EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(product);
			tempS = eigenvalueDecomposition.getD();
			tempU = eigenvalueDecomposition.getV();
		}else{
			///////////  SVD through outerproduct
			product = MatrixUtils.outerProduct(X);
			
			// And then we find the singularvalue and vector 
			// innerproduct is smaller then outer so we use it to find the eigen-value and vector.
			EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(product);
			tempS = eigenvalueDecomposition.getD();
			tempV = eigenvalueDecomposition.getV();
		}
		
		// how much singular value are close to zero : what the real number of dimension, we use and calculate inertie to decide
		int count=0;
		double weight=0;
		double[] tempInertie=new double[tempS.getRowDimension()];
		for(int i=0;i<tempS.getRowDimension();i++){
			tempInertie[i]=Math.sqrt(Math.abs(tempS.get(tempS.getRowDimension()-1-i,tempS.getRowDimension()-1-i)));
			weight+=tempInertie[i];
		}	
		for(int i=0;i<tempInertie.length;i++){
			if((tempInertie[i]=tempInertie[i]/weight)<0.00001){
				count++;
			}
		}
		ArrayUtils.displayMatrix("tempInertie :",tempInertie);
		
		int nbOfDim=tempS.getRowDimension()-count;
		// X = USVt
		U=new Matrix(X.getRowDimension(),nbOfDim);
		S=new Matrix(nbOfDim,nbOfDim);
		Sinv=new Matrix(nbOfDim,nbOfDim);
		V=new Matrix(X.getColumnDimension(),nbOfDim);
				
		// tout est dans l'ordre decroissant donc il faut retrier tout dans l'ordre croissant
		singularValue=new double[S.getRowDimension()];
		inertie=new double[S.getRowDimension()];
		for(int i=0;i<S.getRowDimension();i++){
			singularValue[i]=Math.sqrt(tempS.get((tempS.getRowDimension()-1)-i,(tempS.getRowDimension()-1)-i));
			inertie[i]=tempInertie[i];
			S.set(i, i, singularValue[i]);
			Sinv.set(i, i, 1/singularValue[i]);
		}
		
		if(X.getRowDimension()<X.getColumnDimension()){			
			for(int i=0;i<nbOfDim;i++){
				for(int j=0;j<U.getRowDimension();j++){
					U.set(j, i, tempU.get(j,(tempU.getColumnDimension()-1)-i));
				}
			}
			// Outer product give me U, I find V
			//Matrix tempV=U.times(Sinv);
			V=(X.transpose()).times(U.times(Sinv));			
		}else{
			for(int i=0;i<nbOfDim;i++){
				for(int j=0;j<X.getColumnDimension();j++){
					V.set(j, i, tempV.get(j,(tempV.getColumnDimension()-1)-i));
				}
			}
			// Outer product give me V, I find U	
			U=X.times(V.times(Sinv));			
		}
		
		// new X=XV
		Xnew=(X.times(V)).getArray();
		// eigen Matrix
		eigenMatrix=V.getArray();
		eigenMatrixY=U.getArray();
		
		qualityControl(X,U,V,S);
		System.out.println("Reduction :"+X.getRowDimension()+"x"+X.getColumnDimension()+" ---> " + Xnew.length+"x"+Xnew[0].length); 
		return qualityControl;
		
	}

	
	public void qualityControl(Matrix X,Matrix U,Matrix V,Matrix S){
		
		// verification 1 : Can we reobtain X
		Matrix Xverif=(U.times(S)).times(V.transpose());
		MDSStressTool geoStress=new MDSStressTool();
		geoStress.initiateReferenceMatrix(X.getArray());
		qualityControl[0]=geoStress.kruskalStress(Xverif.getArray());
		System.out.println(" SVD quality control 1 : " + qualityControl[0]);
		
		// verification 2 : Can we reobtain S
		
		Matrix Sverif=((U.transpose()).times(X)).times(V);
		geoStress.initiateReferenceMatrix(S.getArray());
		qualityControl[1]=geoStress.kruskalStress(Sverif.getArray());
		System.out.println(" SVD quality control 2 : " + qualityControl[1]);
		
		//System.out.println("moyenne de chaque colonne");
		//for(int i=0;i<((Xnew).length);i++){
		//	System.out.println("col"+i+" = "+VectorUtils.mean(Xnew[i]));
		//}
	}
	
	public static Matrix rescaleXCorrelation(Matrix X){
		// Correlation rescaling :  x_ij/delta(j)
		double[] variance=new double[X.getColumnDimension()];
		for(int i=0;i<variance.length;i++){
			variance[i]=1/VectorUtils.deviation(MatrixUtils.getColumn(X, i));
		}
		return MatrixUtils.diagonalTimes(X, variance);
				
	}
	
	public static Matrix rescaleYCorrelation(Matrix X){
		Matrix Y=X.transpose();
		double[] variance=new double[Y.getColumnDimension()];
		for(int i=0;i<variance.length;i++){
			variance[i]=1/VectorUtils.deviation(MatrixUtils.getRow(X, i));
		}
		return MatrixUtils.diagonalTimes(Y, variance);
	}
	
	public static Matrix rescaleXCorrespondence(Matrix X){
		
		double W=MatrixUtils.sum(X);
		
		double[] firstRescalingMatrix=new double[X.getRowDimension()];
		for(int i=0;i<firstRescalingMatrix.length;i++){
			firstRescalingMatrix[i]=1/Math.sqrt(W*VectorUtils.sum(MatrixUtils.getRow(X,i)));
		}
		X=MatrixUtils.diagonalTimes(firstRescalingMatrix, X);
		
		double[] secondRescalingMatrix=new double[X.getColumnDimension()];
		for(int i=0;i<secondRescalingMatrix.length;i++){
			secondRescalingMatrix[i]=1/VectorUtils.sum(MatrixUtils.getColumn(X,i));
		}
		X=MatrixUtils.diagonalTimes(X,secondRescalingMatrix);	
		return X;
	}
	
	public static Matrix rescaleYCorrespondence(Matrix X){
		
		double W=MatrixUtils.sum(X);
		
		Matrix Y=X.transpose();
		double[] firstRescalingMatrix=new double[Y.getRowDimension()];
		for(int i=0;i<firstRescalingMatrix.length;i++){
			firstRescalingMatrix[i]=1/Math.sqrt(W*VectorUtils.sum(MatrixUtils.getRow(Y,i)));
		}
		X=MatrixUtils.diagonalTimes(firstRescalingMatrix, Y);
		
		double[] secondRescalingMatrix=new double[Y.getColumnDimension()];
		for(int i=0;i<secondRescalingMatrix.length;i++){
			secondRescalingMatrix[i]=1/VectorUtils.sum(MatrixUtils.getColumn(Y,i));
		}
		X=MatrixUtils.diagonalTimes(Y,secondRescalingMatrix);	
		return X;
	}
	
	
	
	public double[][] centerReorient(double[][] positionMatrix){
		Matrix X=new Matrix(positionMatrix);
		
		// first we center the matrix
		X=MatrixUtils.centerColumn(X);
		
		//  We calculate the outerProduct
		Matrix outerProduct = MatrixUtils.outerProduct(X);
		
		// And then we find the singularvalue and vector 
		// innerproduct is smaller then outer so we use it to find the eigen-value and vector.
		EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(outerProduct);
		Matrix tempU = eigenvalueDecomposition.getV();
		Matrix tempS = eigenvalueDecomposition.getD();
		
		// we calculate inertia and reorganise it in the right order
		int count=0;
		double weight=0;
		inertie=new double[tempS.getRowDimension()];
		for(int i=0;i<tempS.getRowDimension();i++){
			inertie[i]=Math.sqrt(Math.abs(tempS.get(tempS.getRowDimension()-1-i,tempS.getRowDimension()-1-i)));
			weight+=inertie[i];
		}	
		for(int i=0;i<inertie.length;i++){
			inertie[i]=inertie[i]/weight;
			count++;
		}
		
		Matrix U=new Matrix(tempU.getRowDimension(),tempU.getColumnDimension());
		for(int i=0;i<U.getColumnDimension();i++){
			for(int j=0;j<U.getRowDimension();j++){
				U.set(j, i, tempU.get(j,tempU.getColumnDimension()-1-i));
			}
		}
		
		X=X.times(U);
		
		return X.getArray();
	}
	
}
