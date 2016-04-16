import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Sid_Hugo on 12.04.2016.
 */
public class OldNode {
    private String Name;
    private ArrayList<String> states=new ArrayList<>();
    private ArrayList<Node> parents=new ArrayList<>();
    private ArrayList<Node> children=new ArrayList<>();
    //private Map<String, ArrayList<Float>> potentials;
    private Map<Set<String>, Double> potentials=new HashMap<>();
    private Map<Set<String>, Double> potentialsTogether=new HashMap<>();

    public OldNode(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public ArrayList<String> getStates() {
        return states;
    }

    public ArrayList<Node> getParents() {
        return parents;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setStates(ArrayList<String> states) {
        this.states = states;
    }

    public void addParent(Node parent) {
        parents.add(parent);

    }

    public void addChild(Node child) {
        children.add(child);
    }

    public void addPotential(Set<String> key, double value) {
        potentials.put(key,value);
    }
    public Double getPotential(Set<String> key) {
        return potentials.get(key);
    }
    public void addPotentialTogether(Set<String> key, double value) {
        potentialsTogether.put(key, value);
    }
    public Double getPotentialTogether(Set<String> key) {
        return potentialsTogether.get(key);
    }

    public Map<Set<String>, Double> getPotentials() {
        return potentials;
    }

    public Map<Set<String>, Double> getPotentialsTogether() {
        return potentialsTogether;
    }
}
