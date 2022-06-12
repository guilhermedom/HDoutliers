package outliers;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import java.lang.Math;

import common.Instance;
import common.Table;

public class MCA {
	private Table data;
	
	private RealMatrix U;
	private RealMatrix V;
	private RealMatrix S;
	private RealMatrix F;
	private RealMatrix D_r;
	private RealMatrix D_c;
	
	private double[] s;
	private double[] E;
	private int rank;
	
	public MCA(Table data) {
		this.data = data;
	}
	
	public Table makeContinuousRepresentation(List<Integer> columns) {
		for (int i = 0; i < columns.size(); i++) {
			findEigen(columns.get(i));
			double[] continuousColumn = getFactorScores(1.0, 0);
			
			for (int j = 0; j < continuousColumn.length; j++) {
				this.data.getInstance(j).setAt(columns.get(i), continuousColumn[j]);
			}
		}
		return this.data;
	}
	
	public double[] getFactorScores() {
		return getFactorScores(1.0, 0);
	}
	
	public double[] getFactorScores(double percent, int cut) {
		if (cut != 0) {
			cut = this.rank < cut ? this.rank : cut;
		}
		
		double sum = 0.0;
		double[] cumsum = new double[E.length];
		for (int i = 0; i < E.length; i++) {
			sum += i;
			cumsum[i] = sum;
		}
		sum *= percent;
		
		int k = 0;
		for (int i = 0; i < E.length; i++) {
			if (cumsum[i] >= sum) {
				k = i + 1;
			}
		}
				
		int numberOfColumns = cut != 0 ? cut : k;
//		this.S = this.S.getSubMatrix(0, this.data.getNumRows() - 2, 0, numberOfColumns);
//		double[][] S_matrix = this.U.getData();
//		System.out.println(S_matrix.length);
//		System.out.println(S_matrix[0].length);
//		for (int j = 0; j < S_matrix.length; j++) {
//			for (int ji = 0; ji < S_matrix[0].length; ji++) {
//				System.out.print(S_matrix[j][ji] + " ");
//			}
//			System.out.println();
//		}
		
		this.F = this.D_r.multiply(this.U).multiply(this.S);
		this.D_r = null;
		this.U = null;
		this.S = null;
		System.gc();
		
		return this.F.getColumn(0);
	}
	
	public void findEigen(int column) {
		double[][] dummyCodes = processColumn(column);
		
		RealMatrix X = MatrixUtils.createRealMatrix(dummyCodes);
		
		double S = Math.pow(X.getFrobeniusNorm(), 2);
		RealMatrix Z = X.scalarMultiply(1 / S);
		X = null;
		
		double[] rArray = new double[Z.getRowDimension()];
		for (int j = 0; j < Z.getRowDimension(); j++) {
			rArray[j] = Z.getRowVector(j).getL1Norm();
		}
		RealVector r = MatrixUtils.createRealVector(rArray);
		
		double[] cArray = new double[Z.getColumnDimension()];
		for (int j = 0; j < Z.getColumnDimension(); j++) {
			cArray[j] = Z.getColumnVector(j).getL1Norm();
		}
		RealVector c = MatrixUtils.createRealVector(cArray);
		
		RealMatrix Z_c = Z.subtract(r.outerProduct(c));
		Z = null;
		
		double[] rDiagArray = r.toArray();
		rDiagArray = arrayPow(rDiagArray, 0.5);
		rDiagArray = invertArray(rDiagArray);
		this.D_r = MatrixUtils.createRealDiagonalMatrix(rDiagArray);
		
		double[] cDiagArray = c.toArray();
		cDiagArray = arrayPow(cDiagArray, 0.5);
		cDiagArray = invertArray(cDiagArray);
		this.D_c = MatrixUtils.createRealDiagonalMatrix(cDiagArray);
							
		RealMatrix SVDInput = D_r.multiply(Z_c).multiply(D_c);
		this.D_c = null;
		Z_c = null;
		
		SingularValueDecomposition svd = new SingularValueDecomposition(SVDInput);
		this.U = svd.getU();
		//this.V = svd.getV();
		this.S = svd.getS();
		this.s = svd.getSingularValues();
		svd = null;
		
		this.E = arrayPow(this.s, 2);
		this.rank = E.length;
	}
	
	public double[][] processColumn(int column) {
		Double[] instanceValues = new Double[this.data.getNumRows()];

		for (int j = 0; j < this.data.getNumRows(); j++) {
			instanceValues[j] = this.data.getAt(j, column);
//			System.out.println(instanceValues[j]);
		}
		Set<Double> categs = new LinkedHashSet<Double>(Arrays.asList(instanceValues));
		Double[] categs_d = categs.toArray(new Double[0]);
//		for (int j = 0; j < categs_d.length; j++) {
//			System.out.println(categs_d[j]);
//		}
//		System.out.println();
				
		double[][] dummyCodes = new double[this.data.getNumRows()][categs_d.length];
		
		for (int j = 0; j < this.data.getNumRows(); j++) {
			for (int k = 0; k < categs_d.length; k++) {
				if (instanceValues[j].equals(categs_d[k])) {
					dummyCodes[j][k] = 1.0;
					break;
				}
			}
		}
		
		
//		for (int j = 0; j < dummyCodes.length; j++) {
//			for (int k = 0; k < dummyCodes[0].length; k++) {
//				System.out.print(dummyCodes[j][k] + " ");
//			}
//			System.out.println();
//		}
		
		return dummyCodes;
	}
	
	public double[] arrayPow(double[] array, double exp) {
		double[] powArray = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			powArray[i] = Math.pow(array[i], exp);
		}
		return powArray;
	}
	
	public double[] invertArray(double[] array) {
		double[] invertedArray = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			invertedArray[i] = 1 / array[i];
		}
		return invertedArray;
	}
}
