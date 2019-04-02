package awt.table;

import Jama.Matrix;
import bacnet.utils.VectorUtils;


/**
 * Public class with general matrix manipulation utilities, not specific to PROJECTNAME.
 * 
 * @author Christophe Becavin
 */

public class MatrixUtils {
	
	/**
	 *  Extract a Row from a Matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 * @param index
	 *              row index for extraction
	 * 
	 * @return a double[] containing the row
	 */
	public static double[] getRow(Matrix matrix,int index){
		double[] vector=new double[matrix.getColumnDimension()];
		for(int i=0;i<matrix.getColumnDimension();i++){
			vector[i]=matrix.get(index,i);
		}
		return vector;
	}
	/**
	 *  Extract a Column from a Matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 * @param index
	 *              column index for extraction
	 * 
	 * @return a double[] containing the column
	 */
	public static double[] getColumn(Matrix matrix,int index){
		double[] vector=new double[matrix.getRowDimension()];
		for(int i=0;i<matrix.getRowDimension();i++){
			vector[i]=matrix.get(i,index);
		}
		return vector;
	}
	/**
	 * @method sum(matrix,index)
	 *  			return the sum of all value in the matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 * @return sum
	 *              a double
	 */ 
	public static double sum(Matrix matrix){
		double sum=0;
		for(int i=0;i<matrix.getRowDimension();i++){
			sum+=VectorUtils.sum(MatrixUtils.getRow(matrix,i));
		}
		return sum;
	}
	/**
	 * @method min(matrix)
	 *  			return minimumValue of a matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 * @return min
	 *              a double
	 */ 
	public static double min(Matrix matrix){
		double min=matrix.get(0,0);
		for(int i=0;i<matrix.getRowDimension();i++){
			double mintemp=VectorUtils.min(MatrixUtils.getColumn(matrix, i));
			if(min>mintemp){
				min=mintemp;
			}
		}
		return min;
	}
	/**
	 * @method max(matrix)
	 *  			return minimumValue of a matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 * @return max
	 *              a double
	 */ 
	public static double max(Matrix matrix){
		double max=matrix.get(0,0);
		for(int i=0;i<matrix.getRowDimension();i++){
			double maxtemp=VectorUtils.max(MatrixUtils.getColumn(matrix, i));
			if(max<maxtemp){
				max=maxtemp;
			}
		}
		return max;
	}
	/**
	 * @method zeroRow(matrix,index)
	 *  			Put zero in the index Row of a Matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 * @param index
	 *              row index
	 */ 
	public static void zeroRow(Matrix matrix,int index){
		for(int i=0;i<matrix.getColumnDimension();i++){
			matrix.set(i,index,0);
		}
	}
	/**
	 * @method zeroColumn(matrix,index)
	 *  Put zero in the index Column of a Matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 * @param index
	 *              row index
	 */ 
	public static void zeroColumn(Matrix matrix,int index){
		for(int i=0;i<matrix.getColumnDimension();i++){
			matrix.set(index,i,0);
		}
	}
	/**
	 *  Center all the row from the Matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 *  
	 * @return the centered matrix
	 */
	public static Matrix centerRow(Matrix matrix){
		Matrix tempMatrix=new Matrix(matrix.getRowDimension(),matrix.getColumnDimension());
		// loop on all the row
		for(int i=0;i<matrix.getRowDimension();i++){
			// find mean
			double[] row=getRow(matrix,i);
			double mean=VectorUtils.mean(row);
			for(int j=0;j<matrix.getColumnDimension();j++){
				tempMatrix.set(i, j, matrix.get(i,j)-mean);
			}
		}
		return tempMatrix;
	}
	/**
	 *  Center all the column from the Matrix
	 * @param matrix
	 *              a Matrix or double[][]
	 *  
	 * @return the centered matrix
	 */
	public static Matrix centerColumn(Matrix matrix){
		Matrix tempMatrix=new Matrix(matrix.getRowDimension(),matrix.getColumnDimension());
		// loop on all the column
		for(int i=0;i<matrix.getColumnDimension();i++){
			// find mean
			double[] column=getColumn(matrix,i);
			double mean=VectorUtils.mean(column);
			for(int j=0;j<matrix.getRowDimension();j++){
				tempMatrix.set(j,i, matrix.get(j,i)-mean);
			}
		}
		return tempMatrix;
	}
	public static Matrix diagonalTimes(double[] diagonal,Matrix matrix){
		// X'=MX so we have m_ii on each row : 
		Matrix returnMatrix=new Matrix(matrix.getRowDimension(),matrix.getColumnDimension());
		for(int i=0;i<matrix.getRowDimension();i++){
			for(int k=0;k<matrix.getColumnDimension();k++){
				returnMatrix.set(i,k,matrix.get(i,k)*diagonal[i]);
			}
		}
		return returnMatrix;
	}
	public static Matrix diagonalTimes(Matrix matrix,double[] diagonal){
		// X'=XM so we have m_ii on each column i : 
		Matrix returnMatrix=new Matrix(matrix.getRowDimension(),matrix.getColumnDimension());
		for(int i=0;i<matrix.getColumnDimension();i++){
			for(int k=0;k<matrix.getRowDimension();k++){
				returnMatrix.set(k,i,matrix.get(k,i)*diagonal[i]);
			}
		}
		return returnMatrix;
	}
	
	
	/**
	 *  Calcul of euclidean distance Matrix
	 * @param matrix
	 * 			a double[][] or matrix
	 * @return distance matrix
	 * 			double[][]
	 */
	public static double[][] euclideanDistance(Matrix matrix){
		double[][] distance=new double[matrix.getRowDimension()][matrix.getRowDimension()];
		for(int i = 0 ; i < distance.length; i++){
			for(int j = 0 ; j < distance.length; j++){
				distance[i][j] = VectorUtils.euclideanDistance(MatrixUtils.getRow(matrix, i), MatrixUtils.getRow(matrix, j));
				distance[j][i]=distance[i][j];
			}
		}
		return distance;
	}
	public static double[][] euclideanDistance(double[][] matrix){
		double[][] distance=new double[matrix.length][matrix.length];
		for(int i = 0 ; i < distance.length; i++){
			for(int j = 0 ; j < distance.length; j++){
				distance[i][j] = VectorUtils.euclideanDistance(matrix[i], matrix[j]);
				distance[j][i]=distance[i][j];
			}
		}
		return distance;
	}
	
	public static void updateEuclideanDistance(double[][] matrix,double[][] distance){
		for(int i = 0 ; i < distance.length; i++){
			for(int j = 0 ; j < distance.length; j++){
				distance[i][j] = VectorUtils.euclideanDistance(matrix[i], matrix[j]);
				distance[j][i] = distance[i][j];
			}
		}
	}
	
	/**
	 *  Calcul of the Inner Product Matrix
	 * @param matrix
	 * 			a double[][] or matrix
	 * @return innerProduct Matrix
	 * 			double[][] or matrix
	 */
	public static Matrix innerProduct(Matrix matrix){
		Matrix T=matrix.transpose();
		Matrix innerProduct = matrix.times(T);
		return innerProduct;
	}
	/**
	 *  Calcul of the Outer Product Matrix
	 * @param matrix
	 * 			a double[][] or matrix
	 * @return innerProduct Matrix
	 * 			double[][] or matrix
	 */
	public static Matrix outerProduct(Matrix matrix){
		Matrix T=matrix.transpose();
		Matrix outerProduct = T.times(matrix);
		return outerProduct;
	}
	
}
