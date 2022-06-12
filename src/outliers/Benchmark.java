package outliers;

import java.util.ArrayList;
import java.util.List;

import common.Instance;
import common.Table;

public class Benchmark {

	public static void main(String[] args) throws Exception {

		Table[] datasets = {
			Table.readCSV("parkinson.csv", ",", 22, true, true),
			Table.readCSV("glass.csv", ",", 9, true, true),
			Table.readCSV("ionosphere0.csv", ",", 33, true, true),
			Table.readCSV("breastw0.csv", ",", 9, true, true),
			Table.readCSV("pima.csv", ",", 8, true, true),
			Table.readCSV("thyroid.csv", ",", 6, true, true),
			Table.readCSV("satimage-2.csv", ",", 36, true, true),
			Table.readCSV("mammography.csv", ",", 6, true, true),
			Table.readCSV("shuttle0.csv", ",", 9, true, true),
			Table.readCSV("http.csv", ",", 3, true, true),
            Table.readCSV("australian0.csv", ",", 14, true, true),
			Table.readCSV("heart0.csv", ",", 13, true, true),
			Table.readCSV("cmc0.csv", ",", 9, true, true),
			Table.readCSV("hepatitis.csv", ",", 20, true, true),
			Table.readCSV("ecoli.csv", ",", 7, true, true),
			Table.readCSV("adult0.csv", ",", 14, true, true),
			Table.readCSV("crx0.csv", ",", 15, true, true),
			Table.readCSV("german0.csv", ",", 20, true, true),
			Table.readCSV("tae0.csv", ",", 5, true, true),
			Table.readCSV("anneal0.csv", ",", 10, true, true),
			Table.readCSV("lymphography.csv", ",", 18, true, true),
			Table.readCSV("car0.csv", ",", 6, true, true),
			Table.readCSV("nursery.csv", ",", 8, true, true),
		};
		
        double[] bestAlphas = {
    		0.12, 0.05, 0.49, 0.41, 0.05, 0.05, 0.05, 0.20, 0.49, 0.33,
    		0.49, 0.30, 0.05, 0.20, 0.33, 0.49, 0.47, 0.49, 0.21, 0.43, 0.09, 0.05, 0.05
        };
		
        int k = 10;
        for (int data_ind = 21; data_ind < datasets.length; data_ind++) {
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
        		List<List<Instance>> members = detector.getHDmembers(data, 100000, 0);
        		List<Instance> outliers = detector.getHDoutliers(data, members, bestAlphas[data_ind], false);
        		
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