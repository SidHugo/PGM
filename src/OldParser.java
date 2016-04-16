import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sid_Hugo on 12.04.2016.
 */
public class OldParser {
    public static ArrayList<Node> parse(String filePath) {
        ArrayList<Node> result=new ArrayList<>();
        InputStream in;
        BufferedReader reader;
        try {
            in = Files.newInputStream(Paths.get(filePath));
            reader=new BufferedReader(new InputStreamReader(in));
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }

        parseNodes(reader, result);

        return result;
    }

    private static void parseNodes(BufferedReader reader, ArrayList<Node> result) {
        String line=null;
        boolean inBlock=false;
        String blockHeader[]=null;
        Node currentNode=null;
        try {
            while((line=reader.readLine())!=null) {
                if(line.equals(""))
                    continue;
                if(!inBlock) {
                    blockHeader=line.split(" ");
                    line=reader.readLine();
                    inBlock=true;
                    if(blockHeader[0].equals("node")) {
                        currentNode=new Node(blockHeader[1]);
                        result.add(currentNode);
                    } else if (blockHeader[0].startsWith("potential")) {
                        //SET PARENTS AND CHILDS

                        // DEBUG IF THERE IS NO PARENTS
                        currentNode=findNodeByName(blockHeader[2],result);
                        for(int i=4; i<blockHeader.length-1; ++i) {
                            Node parent=findNodeByName(blockHeader[i], result);
                            currentNode.addParent(parent);
                            parent.addChild(currentNode);
                        }
                    }
                } else {
                    if(!line.equals("}")) {
                        switch (blockHeader[0]) {
                            case "net":
                                break;
                            case "node":
                                line=line.replace("\t","");
                                if(line.startsWith("states")) {
                                    currentNode.setStates(parseStates(line));
                                }
                                break;
                            case "potential":
                                line=line.replaceFirst("\t","");
                                if(line.startsWith("data")){
                                    parsePotentials(currentNode, reader, line);
                                }
                                break;
                        }
                    } else {
                        inBlock=false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Node findNodeByName(String name, ArrayList<Node> nodes) {
        for(Node n: nodes)
            if(n.getName().equals(name))
                return n;
        return null;
    }

    private static ArrayList<String> parseStates(String line) {
        String statesString[]=line.substring(line.indexOf("(")+1, line.indexOf(")")).split(" ");
        ArrayList<String> states=new ArrayList<>();
        for(int i=0; i<statesString.length; ++i) {
            statesString[i]=statesString[i].replace("\"", "");
            states.add(statesString[i]);
        }
        return states;
    }

    private static void parsePotentials(Node currentNode, BufferedReader reader, String line) {
        ArrayList<Node> parents=currentNode.getParents();
        int parentsCount=parents.size();
        int parentStateCount[]=new int[parentsCount];
        int counters[]=new int[parentsCount];
        for(int i=0; i<parentsCount; ++i) {
            parentStateCount[i]=parents.get(i).getStates().size();
            counters[i]=0;
        }

        while(true) {
            // parse line
            line=line.substring(line.indexOf("("));
            line=line.replace("(", "");
            line=line.replaceFirst("\t", "");
            String doubleStrings[]=line.split("\t");

            // fill data
            // while
            //      makeHalfKey
            //      for range (currentNode.stateSize)
            //          add(key+firstState, double)
            //          add(key+secondState, double)
            //      incrementParents
            //while (true) {
            Set parentKey = new HashSet<>();
            for (int parentId = 0; parentId < parentsCount; ++parentId) {
                parentKey.add(parents.get(parentId).getStates().get(counters[parentId]));
            }
            for (int stateCounter = 0; stateCounter < currentNode.getStates().size(); ++stateCounter) {
                Set key=new HashSet<>();
                key.add(currentNode.getStates().get(stateCounter));
                key.addAll(parentKey);
                currentNode.addPotential(key, Double.valueOf(doubleStrings[stateCounter]));
            }
            // increment counters
            boolean exitFlag = true;
            for (int parentId = parentsCount - 1; parentId >= 0; --parentId) {
                if (counters[parentId] < parentStateCount[parentId] - 1) {
                    counters[parentId]++;
                    exitFlag = false;
                    break;
                } else {
                    counters[parentId] = 0;
                }
            }
            if (exitFlag)
                break;
            //}

            // get new line
            try {
                line=reader.readLine();
                if(line.equals("}"))
                    return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
