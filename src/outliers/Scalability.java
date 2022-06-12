package outliers;

import java.util.ArrayList;
import java.util.List;

import common.Instance;
import common.Table;

public class Scalability {

	public static void main(String[] args) throws Exception {

		Table[] datasets = {
			Table.readCSV("http_1.0.csv", ",", 3, true, true),
			Table.readCSV("http_2.0.csv", ",", 3, true, true),
			Table.readCSV("http_5.0.csv", ",", 3, true, true),
			Table.readCSV("http_10.0.csv", ",", 3, true, true),
			Table.readCSV("http_15.0.csv", ",", 3, true, true),
			Table.readCSV("http_25.0.csv", ",", 3, true, true),
			Table.readCSV("http_50.0.csv", ",", 3, true, true),
			Table.readCSV("http_75.0.csv", ",", 3, true, true),
			Table.readCSV("http_100.0.csv", ",", 3, true, true)
		};
		
		int k = 10;
        for (int data_ind = 0; data_ind < datasets.length; data_ind++) {
			boolean transform = true;
            double runtime = 0.0;
            double[] runtime_list = new double[k];
            int numRowsSample = datasets[data_ind].getNumRows();
            for (int i = 0; i < k; i++) {
            	long start = System.currentTimeMillis();
        		MCA dt = new MCA(datasets[data_ind]);
        		
        		boolean[] datatypes = datasets[data_ind].getDatatypes();
        		List<Integer> catList = new ArrayList<Integer>();
        		for (int j = 0; j < datatypes.length; j++) {
        			if (datatypes[j] == false) {
        				catList.add(j);
        			}
        		}
        		Table data = transform ? dt.makeContinuousRepresentation(catList) : datasets[data_ind];
        		HDoutliers detector = new HDoutliers();
        		List<List<Instance>> members = detector.getHDmembers(data, 568000, 0);
        		List<Instance> outliers = detector.getHDoutliers(data, members, 0.33, false);
        		
        		long end = System.currentTimeMillis();

        		double time = (end - start) / 1000.0;
        		runtime += time;
        		runtime_list[i] = time;
            }
            runtime /= k;
            double stdRuntime = getStd(runtime_list);
            message(datasets[data_ind].getName(), numRowsSample, runtime, stdRuntime);
		}
	}
	
	private static void message(String dataset, int numRows, double runtime, double stdRuntime) {
		String outputFormat= "%-15s\tn=%d\tRuntime=%f\tStd Runtime=%f";
		String outputString = String.format(outputFormat, dataset, numRows, runtime, stdRuntime);
		System.out.println(outputString);
	}
	
	private static double getStd(double numArray[])
    {
        double sum = 0.0, standardDeviation = 0.0;

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum / numArray.length;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / numArray.length);
    }
}