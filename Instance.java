import java.util.LinkedHashMap;

// Holds data for a training instance
public class Instance {
	
	public final String label;
	public final LinkedHashMap<String, String> values;
	
	public Instance(LinkedHashMap<String, String> values, String label) {
		this.label = label;
		this.values = values;
	}
	
	// Developer tool: print instance
	public void print() {
		System.out.println(label + ": " + values);
	}
	

}
