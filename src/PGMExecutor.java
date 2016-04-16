import javafx.util.Pair;

import java.util.*;

/**
 * Created by Sid_Hugo on 11.04.2016.
 */
public class PGMExecutor {
    public static Factor executeWithUniversalNode(ArrayList<Node> nodes, Collection<Pair<String, String>> conditions,
                                                ArrayList<String> output) {
        Node universalNode = makeUniversalNode(nodes);
        ArrayList<String> interests = getInterests(conditions, output);
        return universalNode.getFactor().marginalize(interests).select(conditions, interests).normalize();

    }

    private static ArrayList<String> getInterests(Collection<Pair<String, String>> conditions, ArrayList<String> output) {
        ArrayList<String> interests=new ArrayList<>();
        for(Pair<String, String> pair:conditions) {
            interests.add(pair.getKey());
        }
        interests.addAll(output);
        return interests;
    }

    private static Node makeUniversalNode(ArrayList<Node> nodes) {
        // make open and close lists
        ArrayList<Node> openList=new ArrayList<>();
        ArrayList<Node> closeList=new ArrayList<>();
        // find roots
        // add them to open list
        for(Node n: nodes)
            if(n.getParents().size()==0)
                openList.add(n);

        // make universalLeaf
        Node universalNode=new Node("universalLeaf");
        universalNode.setFactor(new Factor("universalLeaf", "universalState"));

        // while
        while(true) {
            if(openList.size()==0) {
                return universalNode;
            }
            Node currentNode=null;
            // foreach in open list
            for(Node n:openList) {
                // get from open list
                currentNode=n;
                for(Node parent:n.getParents()) {
                    if(!closeList.contains(parent)) {
                        currentNode=null;
                        break;
                    }
                }
                if(currentNode!=null)
                    break;
            }
            universalNode.setFactor(Factor.multiply(currentNode.getFactor(), universalNode.getFactor()));
            openList.remove(currentNode);
            closeList.add(currentNode);
            for(Node n:currentNode.getChildren()) {
                if(!openList.contains(n))
                    openList.add(n);
            }
        }
    }

    public static Factor executeWithMultiply(ArrayList<Node> nodes, Collection<Pair<String, String>> conditions,
                                                  ArrayList<String> output) {
        ArrayList<String> interests=getInterests(conditions, output);
        return walkGraphWithMultiply(nodes, conditions, interests);
    }

    private static Factor walkGraphWithMultiply(ArrayList<Node> nodes, Collection<Pair<String, String>> conditions,
                                                ArrayList<String> interests) {
        // make open and close lists
        ArrayList<Node> openList=new ArrayList<>();
        ArrayList<Node> closeList=new ArrayList<>();
        Set<Node> parentsToMarginalize=new HashSet<>();
        // find roots
        // add them to open list
        for(Node n: nodes)
            if(n.getParents().size()==0)
                openList.add(n);

        // make universalFactor
        Factor resultFactor=new Factor("111", "111");

        // while
        while(true) {
            if(openList.size()==0) {
                //universalNode.getFactor().normalizePotentials();
                return resultFactor.marginalize(interests).select(conditions, interests).normalize();
            }
            Node currentNode=null;
            // foreach in open list
            for(Node n:openList) {
                // get from open list
                currentNode=n;
                for(Node parent:n.getParents()) {
                    if(!closeList.contains(parent)) {
                        currentNode=null;
                        break;
                    }
                }
                if(currentNode!=null)
                    break;
            }
            resultFactor=Factor.multiply(resultFactor, currentNode.getFactor());
            openList.remove(currentNode);
            closeList.add(currentNode);
            for(Node n:currentNode.getChildren()) {
                if(!openList.contains(n))
                    openList.add(n);
            }
            parentsToMarginalize.add(currentNode);

            Iterator<Node> iterator=parentsToMarginalize.iterator();
            while(iterator.hasNext()) {
                Node parent=iterator.next();
                if(!interests.contains(parent.getName())) {
                    if (closeList.containsAll(parent.getChildren())) {
                        resultFactor=resultFactor.marginalizeExept(Arrays.asList(parent.getName()));
                        iterator.remove();
                    }
                }
            }
        }
    }

    public static Factor executeWithThreads(ArrayList<Node> nodes, Collection<Pair<String, String>> conditions,
                                             String output) {
        ArrayList<String> interests = new ArrayList<>();
        interests.add(output);
        for(Pair<String, String> pair:conditions) {
            interests.add(pair.getKey());
        }
        Set<Node> observables=new HashSet<>();
        Node outputNode=null;
        for(Node node:nodes) {
            for(Pair<String, String> pair:conditions) {
                if(node.getName().equals(pair.getKey()))
                    observables.add(node);
            }
            if (node.getName().equals(output)) {
                outputNode=node;
            }
        }
        Set<Node> activatedNodes=getActivatedNodes(observables, outputNode);
        return walkThroughActivated(activatedNodes, interests, conditions);

    }

    private static Factor walkThroughActivated(Set<Node> activatedNodes, ArrayList<String> interests, Collection<Pair<String, String>> conditions) {
        // make open and close lists
        ArrayList<Node> openList=new ArrayList<>();
        ArrayList<Node> closeList=new ArrayList<>();
        Set<Node> parentsToMarginalize=new HashSet<>();
        // find roots
        // add them to open list
        for(Node n: activatedNodes) {
            boolean isRoot=true;
            for(Node parent:n.getParents()) {
                if (activatedNodes.contains(parent)) {
                    isRoot=false;
                    break;
                }
            }
            if(isRoot)
                openList.add(n);
        }

        // make universalFactor
        Factor resultFactor=new Factor("111", "111");

        // while
        while(true) {
            if(openList.size()==0) {
                return resultFactor.marginalize(interests).select(conditions, interests).normalize();
            }
            Node currentNode=null;
            // foreach in open list
            for(Node n:openList) {
                // get from open list
                currentNode=n;
                for(Node parent:n.getParents()) {
                    if(!closeList.contains(parent) && activatedNodes.contains(parent)) {
                        currentNode=null;
                        break;
                    }
                }
                if(currentNode!=null)
                    break;
            }
            resultFactor=Factor.multiply(resultFactor, currentNode.getFactor());
            openList.remove(currentNode);
            closeList.add(currentNode);
            for(Node n:currentNode.getChildren()) {
                if(!openList.contains(n))
                    openList.add(n);
            }
            parentsToMarginalize.add(currentNode);

            Iterator<Node> iterator=parentsToMarginalize.iterator();
            while(iterator.hasNext()) {
                Node parent=iterator.next();
                if(!interests.contains(parent.getName())) {
                    if (closeList.containsAll(parent.getChildren())) {
                        resultFactor=resultFactor.marginalizeExept(Arrays.asList(parent.getName()));
                        iterator.remove();
                    }
                }
            }
        }
    }

    private static Set<Node> getActivatedNodes(Set<Node> observables, Node outputNode) {
        Set<Node> activatedNodes=new HashSet<>();
        Set<Node> parentsToAnalize=new HashSet<>();
        Set<Node> childrenToAnalize=new HashSet<>();
        Set<Node> closeAsParent=new HashSet<>();
        Set<Node> closeAsChild=new HashSet<>();
        activatedNodes.add(outputNode);
        closeAsChild.add(outputNode);
        closeAsParent.add(outputNode);
        activatedNodes.addAll(outputNode.getParents());
        activatedNodes.addAll(outputNode.getChildren());
        parentsToAnalize.addAll(outputNode.getParents());
        childrenToAnalize.addAll(outputNode.getChildren());

        while (parentsToAnalize.size()>0 || childrenToAnalize.size()>0) {
            ArrayList<Node> childrenToAdd=new ArrayList<>();
            ArrayList<Node> parentsToAdd=new ArrayList<>();
            for (Node child : childrenToAnalize) {
                if (!observables.contains(child)) {
                    activatedNodes.addAll(child.getChildren());
                    for(Node tryChild:child.getChildren()) {
                        if(!closeAsChild.contains(tryChild) && !childrenToAnalize.contains(tryChild))
                            childrenToAdd.add(tryChild);
                    }
                } else {
                    activatedNodes.addAll(child.getParents());
                    for(Node tryParent:child.getParents()) {
                        if(!closeAsParent.contains(tryParent) && !parentsToAnalize.contains(tryParent))
                            parentsToAdd.add(tryParent);
                    }
                }
                closeAsChild.add(child);
            }
            childrenToAnalize.clear();
            parentsToAnalize.addAll(parentsToAdd);
            parentsToAdd.clear();
            for (Node parent : parentsToAnalize) {
                if (!observables.contains(parent)) {
                    activatedNodes.addAll(parent.getParents());
                    activatedNodes.addAll(parent.getChildren());
//
                    for(Node tryChild:parent.getChildren()) {
                        if(!closeAsChild.contains(tryChild) && !childrenToAnalize.contains(tryChild))
                            childrenToAdd.add(tryChild);
                    }
                    for(Node tryParent:parent.getParents()) {
                        if(!closeAsParent.contains(tryParent) && !parentsToAnalize.contains(tryParent))
                            parentsToAdd.add(tryParent);
                    }
                } else {
                    activatedNodes.addAll(parent.getChildren());
                    for(Node tryChild:parent.getChildren()) {
                        if(!closeAsChild.contains(tryChild) && !childrenToAnalize.contains(tryChild))
                            childrenToAdd.add(tryChild);
                    }
                }
                closeAsParent.add(parent);
            }
            parentsToAnalize.clear();
            parentsToAnalize.addAll(parentsToAdd);
            childrenToAnalize.addAll(childrenToAdd);

            parentsToAdd.clear();
            childrenToAdd.clear();
        }
        return activatedNodes;
    }
}
