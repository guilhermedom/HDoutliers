package outliers;

import common.Table;
import common.Instance;
import java.util.ArrayList;
import java.lang.Math;
import java.util.stream.IntStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
//import weka.classifiers.Classifier;
//import weka.classifiers.lazy.IBk;
//import weka.core.Attribute;
//import weka.core.DenseInstance;
//import weka.core.Instances;

public class HDoutliers {
	private double radius;
	private double alpha;
	private boolean transform;
	private Table data;
	
	//private ArrayList<Integer> cl;
	//private ArrayList<Integer> U;
	//private int m;
	
	public HDoutliers() {
		radius = 0;
		alpha = 0.05;
		transform = true;
	}
		
	public List<List<Instance>> getHDmembers(Table data, int maxrows, double radius) throws Exception {
	    //data = as.matrix(data)
	    int n = data.getNumRows();
	    int p = data.getNumCols();
	    List<List<Instance>> members = new ArrayList<List<Instance>>();
	    
	    if (radius == 0) {
	        radius = 0.1 / (Math.pow(Math.log(n), (1 / (double) p)));
	    }
	    
	    if (n <= maxrows) {
//	    	List<Integer> uniqueIndexes = data.findUniqueIndexes();
//	    	for (int i = 0; i < uniqueIndexes.size(); i++) {
//	    		List<Instance> memberList = new ArrayList<Instance>();
//	    		memberList.add(data.getInstance(uniqueIndexes.get(i)));
//	    		members.add(memberList);
//	    	}
	    	members = data.defineMemberLists();
	    	this.data = null;
	    	System.gc();
	    }
	    else {
//	        members = Collections.nCopies(n, -1);
//	        members.set(0, 1);
	    	for (int i = 0; i < n; i++) {
	    		List<Instance> memberList = new ArrayList<Instance>();
	    		//memberList.add(uniqueIndexes.get(i));
	    		members.add(memberList);
	    	}
	    	members.get(0).add(data.getInstance(0));

	    	List<Instance> exemplars = new ArrayList<Instance>();
	        exemplars.add(data.getInstance(0));
	        
	        ArrayList<weka.core.Attribute> attributes = new ArrayList<weka.core.Attribute>();
	        for (String attr : data.getAttributes()) {
	        	attributes.add(new weka.core.Attribute(attr));
	        }
	        weka.core.Instances wekaDataset = new weka.core.Instances("dataset", attributes, n);
	        wekaDataset.add(new weka.core.DenseInstance(1.0, data.getInstance(0).getValues()));
	        
	        for (int i = 1; i < n; i++) {
	        	if (i < 11) {
	        		System.out.println(exemplars.toString());
	        	}
	            weka.core.DenseInstance newWekaInstance = new weka.core.DenseInstance(1.0, data.getInstance(i).getValues());
	            
//	            System.out.println(wekaDataset.numInstances());
	            wekaDataset.add(newWekaInstance);
	            newWekaInstance.setDataset(wekaDataset);
//	            System.out.println(wekaDataset.numInstances());
		        weka.core.neighboursearch.KDTree kdt = new weka.core.neighboursearch.KDTree();
		        kdt.setInstances(wekaDataset);
//            	System.out.println(newWekaInstance.toStringNoWeight());
	            Instance m = new Instance(i, kdt.nearestNeighbour(wekaDataset.lastInstance()).toDoubleArray());
//            	System.out.println(wekaDataset.toString());
	            double[] d = kdt.getDistances();
//	            System.out.println(d[0]);
	            if (d[0] < radius) {
		            wekaDataset.delete(wekaDataset.numInstances() - 1);
//	            	for (int j = 0; j < exemplars.size(); j++) {
//	            		System.out.println(exemplars.get(j).toString());
//	            	}
//	            	System.out.println(exemplars.size());
	                int l = exemplars.indexOf(m);
//	                System.out.println(m.toString());
//	                System.out.println(l);
	    	        members.get(l).add(m);
	                continue;
	            }
//	            System.out.println(d[0]);
	            exemplars.add(data.getInstance(i));
	            members.get(i).add(data.getInstance(i));
	            
//	        	wekaDataset.add(newWekaInstance);
	        	//newWekaInstance.setDataset(wekaDataset);
	        }
	        for (int i = 0; i < exemplars.size(); i++) {
//	    		System.out.println(exemplars.get(i).getId());
	    	}
	    }
//	    System.out.println(radius);
	    members.removeIf(List::isEmpty);
	    
//	    for (int i = 0; i < members.size(); i++) {
//    		System.out.println(members.get(i).toString());
//    	}
	    
	    return members;
	}
	
	public List<Instance> getHDoutliers(Table data, List<List<Instance>> memberLists, double alpha, boolean transform) throws Exception {
	    //data = transform ? dataTrans(data) : data;
		
		
	    //if (any(is.na(data))) stop("missing values not allowed")

	    //exemplars <- sapply(memberLists, function(x) x[[1]])
	    List<Instance> exemplars = new ArrayList<Instance>();
	    
	    ArrayList<weka.core.Attribute> attributes = new ArrayList<weka.core.Attribute>();
        for (String attr : data.getAttributes()) {
        	attributes.add(new weka.core.Attribute(attr));
        }
        weka.core.Instances wekaDataset = new weka.core.Instances("exemplars_dataset", attributes, data.getNumRows());

	    for (int i = 0; i < memberLists.size(); i++) {
	    	exemplars.add(memberLists.get(i).get(0));
	    	
        	wekaDataset.add(new weka.core.DenseInstance(1.0, memberLists.get(i).get(0).getValues()));
	    }
	    weka.core.neighboursearch.KDTree kdt = new weka.core.neighboursearch.KDTree();
	    kdt.setInstances(wekaDataset);

	    List<Double> distances = new ArrayList<Double>();
	    
	    for (int i = 0; i < memberLists.size(); i++) {
	        //Instance m = new Instance(i, kdt.nearestNeighbour(wekaDataset.instance(i)).toDoubleArray());
	    	kdt.nearestNeighbour(wekaDataset.instance(i)).toDoubleArray();
	        double[] d = kdt.getDistances();
	        distances.add(d[0]);
	    }
	    kdt = null;
	    wekaDataset = null;
	    System.gc();
//	    System.out.println(distances.size());

	    //double[] d = knn.dist(data[exemplars, ], k = 1);
	    int n = distances.size();
	    
	    List<Double> sortedDistances = new ArrayList<Double>();
	    for (int i = 0; i < n; i++) {
	    	sortedDistances.add(distances.get(i));
	    }
	    
	    Collections.sort(sortedDistances);
	    List<Double> gaps = consecutiveDifferences(sortedDistances);
	    double n4 = Math.max(Math.min(50, (int) Math.floor(n / 4)), 2);
	    //J <- 1:n4
	    int start = Math.max((int) Math.floor(n / 2), 1);
	    
	    double[] ghat = new double[n];
	    double sum;
	    for (int i = start; i < n; i++) {
	    	sum = 0.0;
	    	for (int j = 1; j <= n4; j++) {
	    		sum += (j / n4) * gaps.get(i - j + 1);
	    	}
	    	ghat[i] = sum;
	    }
	    
	    //for (i in start:n) ghat[i] <- sum((J/n4) * gaps[i - J + 1])
	    
	    
	    
	    double logAlpha = Math.log(1 / alpha);
	    double bound = Double.POSITIVE_INFINITY;
	    for (int i = start; i < n; i++) {
	        if (gaps.get(i) > logAlpha * ghat[i]) {
	            bound = sortedDistances.get(i - 1);
	            break;
	        }
	    }
//	    for (int i = start; i < sortedDistances.size(); i++) {
//	    	System.out.println(sortedDistances.get(i));
//	    }
//	    System.out.println(bound);
//	    System.out.println(sortedDistances.size());
	    
	    List<Instance> ex = new ArrayList<Instance>();
	    for (int i = 0; i < distances.size(); i++) {
	    	if (distances.get(i) > bound) {
	    		ex.add(exemplars.get(i));
	    	}
	    }
	    
//	    for (int i = 0; i < ex.size(); i++) {
//	    	System.out.println(ex.get(i));
//	    }
	    
	    List<Instance> out = new ArrayList<Instance>();
	    for (int i = 0; i < memberLists.size(); i++) {
	    	for (int j = 0; j < ex.size(); j++) {
		    	if (memberLists.get(i).get(0).equals(ex.get(j))) {
		    		out.addAll(memberLists.get(i));
		    	}
	    	}
	    }
	    
//	    for (int i = 0; i < out.size(); i++) {
//	    	System.out.println(out.get(i).toString());
//	    }
	    
//	    ex = exemplars[which(d > bound)]
//	    mem1 <- sapply(memberLists, function(x) x[1])
//	    out <- unlist(memberLists[match(ex, mem1)])
//	    names(out) <- NULL
	    return out;
	}
	
	static List<Double> consecutiveDifferences(List<Double> arr) {
	    List<Double> diff = new ArrayList<Double>();
	    diff.add(0.0);
	    for (int i = 1; i < arr.size(); i++) {
	        diff.add(arr.get(i) - arr.get(i - 1));
	    }
	    return diff;
	}
}
