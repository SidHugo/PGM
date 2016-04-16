import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Sid_Hugo on 10.04.2016.
 */
public class Node {
    private String Name;
    private ArrayList<Node> parents=new ArrayList<>();
    private ArrayList<Node> children=new ArrayList<>();
    private Factor factor=new Factor();

    public Node(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public ArrayList<String> getStates() {
        return factor.getStates();
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

    public void addParent(Node parent) {
        parents.add(parent);
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public void addPotential(Set<Pair<String, String>> key, double value) {
        factor.addPotentisal(key,value);
    }

    public Double getPotential(Set<Pair<String, String>> key) {
        return factor.getPotential(key);
    }

//    public void addPotentialTogether(Set<Pair<String, String>> key, double value) {
//        factor.addPotentialsTogether(key, value);
//    }

//    public Double getPotentialTogether(Set<Pair<String, String>> key) {
//        return factor.getPotentialTogether(key);
//    }

    public Map<Set<Pair<String, String>>,Double> getPotentials() {
        return factor.getPotentials();
    }

//    public Map<Set<Pair<String, String>>,Double> getPotentialsTogether() {
//        return factor.getPotentialsTogether();
//    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    public void setStates(ArrayList<String> states) {
        factor.setStates(states);
    }
}
