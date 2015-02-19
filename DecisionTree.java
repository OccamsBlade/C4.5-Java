import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class DecisionTree {
	
	// Minimum number of instances to create decision node
	public int minInstances;
	
	// Stores information on classification feature
	public Attribute classAttribute;
	public String class1;
	public String class2;
	
	// Stores root node of learned tree
	public Node treeRoot; 
	
	// Stores split values when selecting numeric attribute as best
	public double splitValue;
	public double bestSplitValue;
	
	public DecisionTree(int minInstances) {
		this.minInstances = minInstances;
	}
	
	public void printTree() {
		treeRoot.print(0);
	}
	
	// Data set will be modified by this method, it must be either passed as copy or reconstructed
	public void train(DataSet dataSet) {
		Iterator<Attribute> iterator = dataSet.attributes.iterator();
		while (iterator.hasNext()) {
			classAttribute = iterator.next();
			if (classAttribute.label) {
				iterator.remove();
				break;
			}
		}
		class1 = classAttribute.values.get(0);
		class2 = classAttribute.values.get(1);
		treeRoot = decisionTreeLearning(dataSet.instances, dataSet.attributes, dataSet.instances);
		printTree();
	}

	// Tests a set of instances, prints results
	public void test(List<Instance> instances) {
		System.out.println("\n\nListed as: <predicted class> <actual class>");
		int correct = 0;
		for (Instance instance : instances) {
			String predicted = classify(instance, treeRoot);
			System.out.println(predicted + " " + instance.label);
			if (predicted.equals(instance.label)) 
				correct++;	
		}
		System.out.println("\nCorrect: " + correct);
		System.out.println("Total: " + instances.size());
		double hitRate = (correct * 1.0) / (instances.size() * 1.0);
		System.out.println("Hit Rate: " + (hitRate * 100) + "%");
	}
	
	// Classifies an instance (requires that a tree is already built)
	private String classify(Instance instance, Node node) {
		if (node.terminal)
			return node.label;
		if (node.numeric) {
			double instanceValue = Double.parseDouble(instance.values.get(node.attribute));
			if (instanceValue <= node.splitValue)
				return classify(instance, node.children.get(0));
			else return classify(instance, node.children.get(1));
		} else {
			String value = instance.values.get(node.attribute);
			for (Node child : node.children)
				if (value.equals(child.value))
					return classify(instance, child);
		}
		return null;
	}
	
	// Primary learning function
	private Node decisionTreeLearning(List<Instance> instances, 
			List<Attribute> attributes, List<Instance> parentInstances) {
		if (instances.isEmpty())
			return new Node(pluralityValue(parentInstances), true);
		if (instances.size() < minInstances)
			return new Node(pluralityValue(instances), true);
		if (unanimousLabel(instances))
			return new Node(instances.get(0).label, true);
		
		// Returns null if no more features / positive gain features
		Attribute bestAttribute = bestAttribute(instances, attributes);
		if (bestAttribute == null) 
			return new Node(pluralityValue(instances), true);
		Node root = new Node(bestAttribute.name, false);
		root.countInstances(instances);

		// Partition instances based on best attribute
		LinkedHashMap<String, List<Instance>> subsets = new LinkedHashMap<String, List<Instance>>();
		double currentSplitValue = bestSplitValue;
		if (bestAttribute.numeric) {
			root.numeric = true;
			root.splitValue = currentSplitValue;
			List<Instance> leq = new ArrayList<Instance>();
			List<Instance> gtr = new ArrayList<Instance>();
			for (Instance instance : instances) {
				double value = Double.parseDouble(instance.values.get(bestAttribute.name));
				if (value <= currentSplitValue)
					leq.add(instance);
				else
					gtr.add(instance);
			}
			subsets.put("leq", leq);
			subsets.put("gtr", gtr);
		} else {
			for (String value : bestAttribute.values)
				subsets.put(value, new ArrayList<Instance>());
			for (Instance instance : instances) {
				List<Instance> subset = subsets.get(instance.values.get(bestAttribute.name));
				subset.add(instance);
			}
		}
		
		// Iterate through possible values of bestAttribute
		for(String key: subsets.keySet()) {
			// Copy attributes list and remove bestAttribute from copy
			List<Attribute> remainingAttributes = new ArrayList<Attribute>();
			remainingAttributes.addAll(attributes);
			if (!bestAttribute.numeric)
				remainingAttributes.remove(bestAttribute);
			// Add child to subtree of root
			Node child = decisionTreeLearning(subsets.get(key), remainingAttributes, instances);
			if (bestAttribute.numeric) {
				//child.numeric = true;
				//child.splitValue = currentSplitValue;
				if (key.equals("leq"))
					child.leq = true;
				else
					child.leq = false;
			} else
				child.value = key;
			root.addChild(child);
		}
		return root;
	}
	
	/*
	 * Returns best attribute for given instances, remaining attributes
	 * Returns null if no attribute has positive information gain
	 * Returns null if no candidate splits remaining
	 */
	private Attribute bestAttribute(List<Instance> instances, List<Attribute> attributes) {
		double maxGain = Double.MIN_VALUE;
		Attribute best = null;
		for (Attribute attribute : attributes) {
			double gain = gain(instances, attribute);
			if (gain > maxGain) {
				maxGain = gain;
				best = attribute;
				// bestSplitValue gets split value of numeric attribute with highest gain
				bestSplitValue = splitValue;
			}
		}
		if (maxGain <= 0.0)
			return null;
		return best;
	}
	
	// Calculates (maximum) information gain over instance for the attribute
	private double gain(List<Instance> instances, Attribute attribute) {
		if (instances.isEmpty())
			return 0.0;
		if (!attribute.numeric)
			return entropy(instances) - conditionalEntropy(instances, attribute);
		else {
			// Get list of possible split points
			TreeSet<Double> values = new TreeSet<Double>();
			for (Instance instance : instances)
				values.add(Double.parseDouble(instance.values.get(attribute.name)));
			List<Double> candidateSplits = new ArrayList<Double>();
			Iterator<Double> num1 = values.iterator();
			Iterator<Double> num2 = values.iterator();
			if (num2.hasNext())
				num2.next();
			while (num2.hasNext())
				candidateSplits.add((num1.next() + num2.next()) / 2);
			// Find maximum gain from among possible split points
			double maxGain = Double.MIN_VALUE;
			splitValue = Double.MIN_VALUE;
			for (Double split : candidateSplits) {
				double gain = entropy(instances) - conditionalEntropy(instances, attribute, split);
				if (gain > maxGain) {
					maxGain = gain;
					splitValue = split;
				}
			}
			return maxGain;	
		}
	}
	// Returns non-conditional entropy of set of instances
	private double entropy(List<Instance> instances) {
		if (instances.isEmpty())
			return 0.0;
		// Obtain number of instances in each class
		int total = instances.size();
		int label1count = 0;
		for (Instance instance : instances)
			if (instance.label.equals(class1))
				label1count++;
		int label2count = total - label1count;
		// Calculate entropy
		double prLabel1 = (label1count * 1.0) / (total * 1.0);
		double prLabel2 = (label2count * 1.0) / (total * 1.0);
		return -(prLabel1 * logBase2(prLabel1) + prLabel2 * logBase2(prLabel2));
	}
	// Conditional entropy of instances given nominal attribute
	private double conditionalEntropy(List<Instance> instances, Attribute attribute) {
		if (instances.isEmpty())
			return 0.0;
		// Create list of instances for each possible value of attribute
		LinkedHashMap<String, List<Instance>> subsets = new LinkedHashMap<String, List<Instance>>();
		for (String value : attribute.values)
			subsets.put(value, new ArrayList<Instance>());
		for (Instance instance : instances) {
			List<Instance> subset = subsets.get(instance.values.get(attribute.name));
			subset.add(instance);
		}
		// Compute entropy
		double entropy = 0.0;
		for(List<Instance> subset : subsets.values()) {
			double probability = (subset.size() * 1.0) / (instances.size() * 1.0);
			entropy += probability * entropy(subset);
		}
		return entropy;
	}
	// Conditional entropy of instances given nominal attribute, split value
	private double conditionalEntropy(List<Instance> instances, Attribute attribute, double split) {
		if (instances.isEmpty())
			return 0.0;
		// Divide the list based on the split point
		List<Instance> leq = new ArrayList<Instance>();
		List<Instance> gtr = new ArrayList<Instance>();
		for (Instance instance : instances) {
			double value = Double.parseDouble(instance.values.get(attribute.name));
			if (value <= split)
				leq.add(instance);
			else
				gtr.add(instance);
		}
		double prLeq = (leq.size() * 1.0) / (instances.size() * 1.0);
		double prGtr = (gtr.size() * 1.0) / (instances.size() * 1.0);
		return prLeq * entropy(leq) + prGtr * entropy(gtr);
	}
	private double logBase2(double x) {
		if (x == 0.0)
			return 0.0;
		return (Math.log(x) / Math.log(2.0));
	}
	
 	// Returns the label of majority of instances, resolves ties with label of first instance
	private String pluralityValue(List<Instance> instances) {
		int firstCount = 0;
		for (Instance instance : instances) 
			if (instance.label.equals(class1))
				firstCount++;
		if (firstCount >= instances.size() - firstCount)
			return class1;
		else return class2;
	}
	// Returns true if all instances have the same class label
	private boolean unanimousLabel(List<Instance> instances) {
		String label = instances.get(0).label;
		for (Instance instance : instances)
			if (!instance.label.equals(label))
				return false;
		return true;
	}

}
