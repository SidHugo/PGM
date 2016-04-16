import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by Sid_Hugo on 13.04.2016.
 */
public class MainClass {
    public static void main(String args[]) {
        if(args.length!=2) {
            System.out.println("Usage: java -jar PGM.jar <graphFile> <queryFile>");
            return;
        }
        ArrayList<Node> nodes=Parser.parse(args[0]);
        Set<Pair<String, String>> conditions=Parser.parseConditions(args[1]);
        ArrayList<String> outputs=Parser.parseOutputs(args[1]);
        Factor resultingFactor=PGMExecutor.executeWithUniversalNode(nodes, conditions, outputs);
        System.out.println("-----UNIVERSAL NODE METHOD-----");
        printFactor(resultingFactor);
        resultingFactor=PGMExecutor.executeWithMultiply(nodes, conditions, outputs);
        System.out.println("-----MULTIPLY METHOD-----");
        printFactor(resultingFactor);
        resultingFactor=PGMExecutor.executeWithThreads(nodes, conditions, outputs.get(0));
        System.out.println("-----THREAD METHOD-----");
        printFactor(resultingFactor);
    }

    private static void printFactor(Factor resultingFactor) {
        for(Map.Entry<Set<Pair<String, String>>, Double> entry:resultingFactor.getPotentials().entrySet()) {
            System.out.println("====================================");
            System.out.println("Potential for states:");
            for(Pair<String, String> pair:entry.getKey()) {
                System.out.println("\t"+pair.getKey()+"\t->\t"+pair.getValue());
            }
            System.out.println("POTENTIAL: "+entry.getValue());
        }
    }
}
