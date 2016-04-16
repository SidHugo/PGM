
import javafx.util.Pair;
import sun.text.resources.cldr.zh.FormatData_zh;

import java.util.*;

/**
 * Created by Sid_Hugo on 12.04.2016.
 */
public class Factor {
    private ArrayList<String> states=new ArrayList<>();
    Set<String> columnNames=new HashSet<>();
    Map<Set<Pair<String, String>>,Double> potentials=new HashMap<>();

    public Factor(){}

    public Factor(String column, String state) {
        Set<Pair<String, String>> key=new HashSet<>();
        key.add(new Pair<>(column, state));
        addPotentisal(key, 1);
        ArrayList<String> states=new ArrayList<>();
        states.add(state);
        setStates(states);
    }

    public Factor(ArrayList<String> states, Set<String> columnNames, Map<Set<Pair<String, String>>, Double> potentials) {
        this.states = states;
        this.columnNames = columnNames;
        this.potentials = potentials;
    }

    public void addColumnName(String name) {
        columnNames.add(name);
    }

    public void removeColumnName(String name) {
        columnNames.remove(name);
    }

    public Set<String> getColumnNames() {
        return columnNames;
    }

    public boolean containsColumnName(String name) {
        return columnNames.contains(name);
    }

    public void addPotentisal(Set<Pair<String, String>> key, double value) {
        potentials.put(key,value);
        for(Pair<String, String> pair:key) {
            columnNames.add(pair.getKey());
        }
    }

    public Double getPotential(Set<Pair<String, String>> key) {
        return potentials.get(key);
    }

    public boolean potentialsContainsKey(Set<Pair<String, String>> key) {
        return potentials.containsKey(key);
    }

    public Map<Set<Pair<String, String>>, Double> getPotentials() {
        return potentials;
    }

    public boolean potentialsContainsNode(String node) {
        for(Map.Entry<Set<Pair<String, String>>,Double> entry:potentials.entrySet()) {
            for(Pair<String, String> pair:entry.getKey()) {
                if(pair.getKey().equals(node)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean potentialsContainsState(String state) {
        for(Map.Entry<Set<Pair<String, String>>,Double> entry:potentials.entrySet()) {
            for(Pair<String, String> pair:entry.getKey()) {
                if(pair.getValue().equals(state)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setStates(ArrayList<String> states) {
        this.states = states;
    }

    public Factor normalize() {
        double sum=0;
        for(Map.Entry<Set<Pair<String, String>>,Double> entry:potentials.entrySet()) {
            sum+=entry.getValue();
        }
        Map<Set<Pair<String, String>>, Double> newPotentials=new HashMap<>();
        if(sum!=0) {
            for(Map.Entry<Set<Pair<String, String>>,Double> entry:potentials.entrySet()) {
                newPotentials.put(entry.getKey(), entry.getValue()/sum);
            }
        }
        return new Factor(states, columnNames, newPotentials);
    }

    public Factor marginalizeExept(Collection<String> exepts) {
        Map<Set<Pair<String, String>>,Double> newPotentials=new HashMap<>();
        for(Map.Entry<Set<Pair<String, String>>,Double> entry:potentials.entrySet()) {
            Set<Pair<String, String>> newKey=new HashSet<>(entry.getKey());
            Set<Pair<String, String>> keysToRemove=new HashSet<>();
            for(Pair<String, String> pair:newKey) {
                for(String exept:exepts) {
                    if(pair.getKey().equals(exept)) {
                        keysToRemove.add(pair);
                        break;
                    }
                }
            }
            newKey.removeAll(keysToRemove);
            if(newPotentials.get(newKey)!=null) {
                newPotentials.put(newKey, newPotentials.get(newKey)+entry.getValue());
            } else {
                newPotentials.put(newKey, entry.getValue());
            }
        }
        Set<String> newColumnNames=new HashSet<>(columnNames);
        newColumnNames.removeAll(exepts);
        if(newPotentials.size()==0)
            return new Factor("111", "111");
        return new Factor(states, newColumnNames, newPotentials);
    }

    public Factor marginalize(Collection<String> interests) {
        Map<Set<Pair<String, String>>,Double> newPotentials=new HashMap<>();
        ArrayList<String> columnNamesToRemove=new ArrayList<>();
        for(Map.Entry<Set<Pair<String, String>>,Double> entry:potentials.entrySet()) {
            Set<Pair<String, String>> newKey=new HashSet<>(entry.getKey());
            Set<Pair<String, String>> keysToRemove=new HashSet<>();
            for(Pair<String, String> pair:newKey) {
                if(!interests.contains(pair.getKey())) {
                    keysToRemove.add(pair);
                    columnNamesToRemove.add(pair.getKey());
                }
            }
            newKey.removeAll(keysToRemove);
            if(newPotentials.get(newKey)!=null) {
                newPotentials.put(newKey, newPotentials.get(newKey)+entry.getValue());
            } else {
                newPotentials.put(newKey, entry.getValue());
            }
        }
        Set<String> newColumnNames=new HashSet<>(columnNames);
        newColumnNames.removeAll(columnNamesToRemove);
        if(newPotentials.size()==0)
            return new Factor("111", "111");
        return new Factor(states, newColumnNames, newPotentials);
    }

    public Factor marginalizeSafeState(Collection<String> interests) {
        Map<Set<Pair<String, String>>,Double> newPotentials=new HashMap<>();
        ArrayList<String> columnNamesToRemove=new ArrayList<>();
        for(Map.Entry<Set<Pair<String, String>>,Double> entry:potentials.entrySet()) {
            Set<Pair<String, String>> newKey=entry.getKey();
            Set<Pair<String, String>> keysToRemove=new HashSet<>();
            for(Pair<String, String> pair:newKey) {
                if(!interests.contains(pair.getKey()) && !states.contains(pair.getKey())) {
                    keysToRemove.add(pair);
                    columnNamesToRemove.add(pair.getKey());
                }
            }
            newKey.removeAll(keysToRemove);
            if(newPotentials.get(newKey)!=null) {
                newPotentials.put(newKey, newPotentials.get(newKey)+entry.getValue());
            } else {
                newPotentials.put(newKey, entry.getValue());
            }
        }
        Set<String> newColumnNames=new HashSet<>(columnNames);
        newColumnNames.removeAll(columnNamesToRemove);
        if(newPotentials.size()==0)
            return new Factor("111", "111");
        return new Factor(states, newColumnNames, newPotentials);
    }

    public Factor select(Collection<Pair<String, String>> condition, ArrayList<String> interests) {
        Map<Set<Pair<String, String>>,Double> newPotentials=new HashMap<>();

        // get conditions that could be in this factor
        ArrayList<Pair<String, String>> matchingConditions=new ArrayList<>();
        for(Pair<String, String> pair:condition) {
            if(columnNames.contains(pair.getKey()))
                matchingConditions.add(pair);
        }
        // if  we got nothing matching - return empty factor (?)
        if(matchingConditions.size()==0) {
            for (String interest : interests) {
                if (columnNames.contains(interest))
                    return new Factor(states, columnNames, potentials);
            }
            return new Factor("111", "111");
        }

        for(Map.Entry<Set<Pair<String, String>>,Double> entry:potentials.entrySet()) {
            if(entry.getKey().containsAll(matchingConditions))
                newPotentials.put(entry.getKey(), entry.getValue());
        }
        if(newPotentials.size()==0)
            return new Factor("111", "111");
        return new Factor(states, columnNames, newPotentials);
    }

    public static Factor multiply(Factor left, Factor right) {
        Factor newFactor=new Factor();
        // find shared columns
        ArrayList<String> sharedColumns=new ArrayList<>();
        for(String leftColumnName:left.getColumnNames()) {
            for(String rightColumnName:right.getColumnNames()) {
                if(leftColumnName.equals(rightColumnName))
                    sharedColumns.add(leftColumnName);
            }
        }

        // check shared columns. if they are equal - multilpy and put
        if(sharedColumns.size()>0) {
            // for each left row
            for (Map.Entry<Set<Pair<String, String>>, Double> leftEntry : left.getPotentials().entrySet()) {
                // for each right row
                for (Map.Entry<Set<Pair<String, String>>, Double> rightEntry : right.getPotentials().entrySet()) {
                    Set<Pair<String, String>> leftKey = leftEntry.getKey();
                    Set<Pair<String, String>> rightKey = rightEntry.getKey();
                    int pairsLeftToFind=sharedColumns.size();
                    // for each left pair of column name and state
                    for (Pair<String, String> leftPair : leftKey) {
                        // select only shared pairs
                        if (sharedColumns.contains(leftPair.getKey())) {
                            boolean equalPair = false;
                            // for each right pair of column name and state
                            for (Pair<String, String> rightPair : rightKey) {
                                // if appropriate column found
                                if (rightPair.getKey().equals(leftPair.getKey())) {
                                    // if states are equal
                                    if (rightPair.getValue().equals(leftPair.getValue())) {
                                        equalPair = true;
                                        pairsLeftToFind--;
                                        break;
                                    }
                                    // if states are not equal
                                    else {
                                        equalPair = false;
                                        break;
                                    }
                                }
                            }
                            // if we didn't find equal state for equal columns
                            if (!equalPair)
                                break;

                        }
                    }
                    if(pairsLeftToFind==0) {
                        // add new key and value to resulting factor
                        Set<Pair<String, String>> newKey = new HashSet<>();
                        newKey.addAll(leftKey);
                        newKey.addAll(rightKey);

                        newFactor.addPotentisal(newKey, leftEntry.getValue() * rightEntry.getValue());
                    }
                }
            }
        } else {
            for (Map.Entry<Set<Pair<String, String>>, Double> leftEntry : left.getPotentials().entrySet()) {
                // for each right row
                for (Map.Entry<Set<Pair<String, String>>, Double> rightEntry : right.getPotentials().entrySet()) {
                    Set<Pair<String, String>> newKey = new HashSet<>(leftEntry.getKey());
                    newKey.addAll(rightEntry.getKey());
                    newFactor.addPotentisal(newKey, leftEntry.getValue()*rightEntry.getValue());
                }
            }
        }
        return newFactor;
    }

    public ArrayList<String> getStates() {
        return states;
    }
}
