package outliers;

import common.AUC;
import common.Table;
import common.Instance;

import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length != 6) {
			System.out.println("Parameters: dataset_path label_column maxrows radius alpha transform");
			System.out.println("dataset_path is a string to the dataset path.");
			System.out.println("label_column = -1 means no labels.");
			System.out.println("maxrows is the maximum number of rows to use.");
			System.out.println("radius is the radius used by the kNN step.");
			System.out.println("alpha is an outlier threshold in between 0 and 1.");
			System.out.println("transform is 1 to transform categorical data to numeric or 0"
					+ " to leave as it is.");
			System.exit(1);
		}
		
		// read parameters
		String filename = args[0];
		int labelCol = Integer.valueOf(args[1]); // -1 means no label
		int maxrows = Integer.valueOf(args[2]); // Must be greater than 1
		double radius = Double.valueOf(args[3]);
		double alpha = Double.valueOf(args[4]);
		boolean transform = Integer.valueOf(args[5]) == 1;
		
		// read dataset
		Table dataset = Table.readCSV(filename, ",", labelCol, true, true);
		
		long start = System.currentTimeMillis();
		MCA dt = new MCA(dataset);
		
		boolean[] datatypes = dataset.getDatatypes();
		List<Integer> catList = new ArrayList<Integer>();
		for (int i = 0; i < datatypes.length; i++) {
			if (datatypes[i] == false) {
				catList.add(i);
			}
		}
		Table data = transform ? dt.makeContinuousRepresentation(catList) : dataset;
		HDoutliers detector = new HDoutliers();
		List<List<Instance>> members = detector.getHDmembers(data, maxrows, radius);
		//System.out.println(members.size() + "\n");
		List<Instance> outliers = detector.getHDoutliers(data, members, alpha, false);
		System.out.println(outliers.size() + "\n");

		long end = System.currentTimeMillis();
		
		double time = (end - start) / 1000.0;
		System.out.println(String.format("runtime: %.8f", time));
		
//		for (int i = 0; i < data.getNumRows(); i++) {
//			System.out.println(data.getInstance(i).toString());
//		}
				
//		double alpha_step = 0.05;
//		double alpha_max = 0.5;
//		//while (alpha < 0.06) {
//    		long start = System.currentTimeMillis();
//			HDoutliers detector = new HDoutliers();
//			List<List<Instance>> members = detector.getHDmembers(data, maxrows, radius);
//			//System.out.println(members.size() + "\n");
//			List<Instance> outliers = detector.getHDoutliers(data, members, alpha, false);
//			long end = System.currentTimeMillis();
//			System.out.println(outliers.size());
//			alpha += alpha_step;
		//}
		
//		for (int i = 0; i < data.getNumRows(); i++) {
//			System.out.print((i + 1) + ": ");
//			if (outliers.contains(data.getInstance(i))) {
//				System.out.println("1");
//			} else {
//				System.out.println("0");
//			}
//		}
		
		// run the algorithm and compute runtime
		//long start = System.currentTimeMillis();
		//HySortOD hsod = new HySortOD(b, strategy);		
		//double[] yPred = hsod.score(dataset);
		//long end = System.currentTimeMillis();
		
		//double time = (end - start) / 1000.0;
		//System.out.println(String.format("runtime: %.8f", time));
		
		// calculate the auc score if possible
//		if (dataset.hasLabel()) {
//			int[] yTrue = dataset.getLabels();
//			//double auc = AUC.measure(yTrue, yPred);
//			//System.out.println(String.format("rocauc : %.8f", auc));
//		}
		

		// report the outlierness score for each instance if specified
		/*if (reportOutput) {
			if (dataset.hasLabel()) {
				int[] yTrue = dataset.getLabels();
				for (int i = 0; i < yTrue.length; i++) {
					//System.out.println(String.format("%d,%.4f", yTrue[i], yPred[i]));
				}
			} else {
				//for (int i = 0; i < yPred.length; i++) {
					//System.out.println(String.format("%.4f", yPred[i]));
				//}
			}
		}*/
	}
}
