import java.util.List;
import java.util.ArrayList;

public class Node {
	
	// Variable for terminal nodes
	public boolean terminal;
	public String label;
	
	// Variable for nominal/numeric nodes	
	public String attribute;
	public List<Node> children;
		
	public int negativeInstances;
	public int positiveInstances;
	
	// Variables for nominal nodes only (value for parent attribute)
	public String value;
	
	// Variables for numeric nodes only (splitValue for parent attribute)
	public boolean numeric;
	public boolean leq;
	public double splitValue;
	
	public Node(String descriptor, boolean terminal) {
		// All other variables presumed null/default
		this.terminal = terminal;
		if (terminal) {
			this.label = descriptor;
			children = null;
		} else {
			this.attribute = descriptor;
			children = new ArrayList<Node>();
		}
	}
	
	public void countInstances(List<Instance> instances) {
		negativeInstances = 0;
		positiveInstances = 0;
		for (Instance instance : instances) {
			if (instance.label.equals("positive"))
				positiveInstances++;
			else if (instance.label.equals("negative"))
				negativeInstances++;
		}
	}
	
	public void addChild(Node child) {
		children.add(child);
	}
	
	// Prints the entire subtree  of the node
	public void print(int depth) {
		if (terminal) {
			System.out.print(": " + label);
		} else {
			for (Node child : children) {
				System.out.print("\n");
				for (int i = 0; i < depth; i++)
					System.out.print("|\t");
				System.out.print(attribute);
				if (numeric) {
					if (child.leq)
						System.out.print(" <= ");
					else
						System.out.print(" > ");
					System.out.print(splitValue);
					// System.out.print(" [" + child.negativeInstances + " " + child.positiveInstances + "]");
					child.print(depth + 1);
				} else {
					System.out.print(" = " + child.value);
					// System.out.print(" [" + child.negativeInstances + " " + child.positiveInstances + "]");
					child.print(depth + 1);
				}
				
			}
			
			
		}
	}
	
}
