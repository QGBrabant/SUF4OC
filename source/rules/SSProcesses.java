/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rules;

import aggregation.AggregationFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lattices.Chain;
import orders.OTools;
import lattices.impls.Rank;
import ordinalclassification.InstanceList;
import ordinalclassification.OCTable;

/**
 *
 * @author qgbrabant
 */
public class SSProcesses {
    ////////////////////////////////
    //                            //
    // MAX-SUF LEARNING PROCESSES //
    //                            //
    ////////////////////////////////


    public static Collection<SugenoUtility> PROCESS_1(InstanceList<Rank, Chain> dataset, Double[] params) {
        double rho = params[0];

        InstanceList<Rank, Chain> d1 = monotonize(dataset);
        InstanceList<Rank, Chain> d2 = pruneCriteria(d1,d1);

        Collection<SugenoUtility> sus = flexerpolate(d2, d1);
        sus = findBestSUSubsetGreedy(sus, d1, rho);
        for (SugenoUtility su : sus) {
            su.clean();
        }
        return sus;
    }
    
    /////////////////////////////////
    //                             //
    // RULE-SET LEARNING PROCESSES //
    //                             //
    /////////////////////////////////
    
    public static AggregationFunction<Rank> RULE_LEARNING_PROCESS(InstanceList<Rank, Chain> dataset, Double[] params) {
        InstanceList<Rank, Chain> d1 = monotonize(dataset);
        InstanceList<Rank, Chain> d2 = pruneCriteria(d1,d1);
        
        return new RuleSetOnChains(d1);
    }

    /////////////////////////
    //                     //
    // RULE-SETS REDUCTION //
    //                     //
    /////////////////////////
    
    /**
     * Something that does not really works.
     * @param R
     * @param table
     * @return 
     */
    public static InstanceList<Rank, Chain> mergeRules(InstanceList<Rank, Chain> R, OCTable<Rank, Chain> table) {

        List<LinearInstance> listR = new ArrayList<>();
        LinearInstance r1;
        LinearInstance r2;
        Set<LinearInstance> newRules = new HashSet(R);

        LinearInstance candidate;
        while (!(newRules.isEmpty())) {
            for (LinearInstance r : newRules) {
                listR.add(r);
            }
            Collections.shuffle(listR);
            newRules = new HashSet<>();
            int i = -1;
            int j;
            while (++i < listR.size()) {
                r1 = listR.get(i);
                j = i;
                while (++j < listR.size()) {
                    r2 = listR.get(j);
                    if (!r1.equals(r2)) {
                        candidate = SRTools.max(r1, r2);
                        if (SRTools.areCompatible(table, candidate)) {
                            newRules.add(candidate);
                            listR.remove(j);
                            listR.remove(i);
                            i--;
                            j = listR.size();
                        }
                    }
                }
            }
        }

        InstanceList<Rank, Chain> res = new InstanceList<>(R.getDomain(), R.getCodomain());
        res.addAll(listR);

        return res;
    }

    //////////////////////
    //                  //
    // CRITERIA PRUNORS //
    //                  //
    //////////////////////
    /*A criterion belongs to exactly one Example*/
    private static class Criterion {

        int id;
        int useCount; //the number of time the criterion is used to "justify" the difference of outcomes between the example and another

        public Criterion(int id) {
            this.id = id;
            useCount = 0;
        }
    }
    /* An example is an instance with a Criterion array, where the "usage" of each Criterion is evaluated */
    private static class Example {

        Criterion[] criteria;
        Instance<Rank, Chain> row;

        public Example(Instance<Rank, Chain> row) {
            this.row = row;
            this.criteria = new Criterion[row.getDomain().length];
            for (int i = 0; i < this.criteria.length; i++) {
                this.criteria[i] = new Criterion(i);
            }
        }
    }
    
    /**
     * Initialize a list of Examples from the data.
     * Both parameters can be the same object.
     * @param rules set of instance the set of Example is build from.
     * @param dataset set of instances for calculating criteria usage.
     * @return the list of Examples with criteria usage of each instance.
     */

    private static List<Example> buildExamples(InstanceList<Rank, Chain> rules, InstanceList<Rank, Chain> dataset) {
        Chain[] domain = rules.getDomain();
        Chain codomain = rules.getCodomain();


        /* Make an Example out of each row
        For each example, count the number of time each criterion is useful for keeping monotonicity of the dataset */
        List<Example> examples = new ArrayList<>();
        for (Instance<Rank, Chain> row : rules) {
            examples.add(new Example(row));
        }

        Integer r;

        for (Example e : examples) {
            for (Instance<Rank, Chain> instance : dataset) {
                r = codomain.relation(e.row.getLabel(), instance.getLabel());

                if (r != 0) {
                    for (int k = 0; k < domain.length; k++) {
                        if (domain[k]
                                .relation(
                                        e.row.getFeature(k),
                                        instance.getFeature(k))
                                .equals(r)) {
                            e.criteria[k].useCount++;
                        }
                    }
                }
            }
        }
        return examples;
    }
    
    /**
     * Compute a set of rules which is identical to the first parameter, with some criteria set to 0.
     * The pruning of criteria is constrained by the second parameter:
     * the resulting set of rules is constrained 
     * @param rules
     * @param dataset
     * @return 
     */
    public static InstanceList<Rank, Chain> pruneCriteria(InstanceList<Rank, Chain> rules, InstanceList<Rank, Chain> dataset) {
        class Couple {

            int i, count;

            Couple(int i, int c) {
                this.i = i;
                this.count = c;
            }
        }

        Chain[] domain = dataset.getDomain();
        Chain codomain = dataset.getCodomain();

        List<Example> examples = buildExamples(rules, dataset);
        boolean ok;

        InstanceList<Rank, Chain> res = dataset.getEmptyShell();
        Rank[] input;
        for (Example e : examples) {
            input = Arrays.copyOf(e.row.copyOfFeatures(), dataset.getDomain().length);
            int sum = 0;
            List<Couple> list = new ArrayList<>();
            for (int k = 0; k < domain.length; k++) {
                sum += e.criteria[k].useCount;
                list.add(new Couple(k, e.criteria[k].useCount));
            }

            Collections.sort(list, (x, y) -> {
                return new Integer(x.count).compareTo(y.count);
            });

            int i = 0;
            ok = true;
            Couple c1;
            Iterator<Couple> it = list.iterator();

            while (it.hasNext()) {
                c1 = it.next();
                for (Instance<Rank, Chain> r : dataset) {
                    if (e.row.getLabel().toInt() > r.getLabel().toInt()) {
                        ok = false;
                        for (Couple c2 : list) {
                            if (c1 != c2) {
                                if (e.row.getFeature(c2.i).toInt() > r.getFeature(c2.i).toInt()) {
                                    ok = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!ok) {
                        break;
                    }
                }
                if (ok) {
                    it.remove();
                    input[c1.i] = dataset.getDomain()[c1.i].getBottom();
                }
            }

            res.add(new LinearInstance(input, e.row.getLabel(), dataset.getDomain(), dataset.getCodomain()));
        }

        //ouiSystem.out.println("Removal count: "+removalCount);
        return res;

    }

    //////////////////////////////////
    //                              //
    // MONOTONIZERS IMPLEMENTATIONS //
    //                              //
    //////////////////////////////////
    
    public static InstanceList<Rank, Chain> monotonize2(InstanceList<Rank, Chain> d) {
        // This local class represents a node in the incompatibility graph
        class Node {

            Instance<Rank, Chain> row;
            Set<Node> neighbors;

            Node(Instance<Rank, Chain> row) {
                this.row = row;
                this.neighbors = new HashSet<>();
            }

            boolean compatibleWith(Node other) {
                return SRTools.areCompatible(this.row, other.row);
            }
            
            double incompatiblityScore(double[][] clashWeight){
                double res = 0;
                for(Node n : this.neighbors){
                    res += clashWeight[this.row.getLabel().toInt()][n.row.getLabel().toInt()];
                }
                
                return res;
            }

            @Override
            public String toString() {
                return this.row.toString();
            }
        }

        List<Node> nodes = new ArrayList<>();
        Map<Rank,Integer> classSizes = new HashMap<>();
        
        
        
        
        for (Instance<Rank, Chain> r1 : d) {
            nodes.add(new Node(r1));
            classSizes.put(r1.getLabel(), classSizes.getOrDefault(r1.getLabel(), 0)+1);
        }
        //System.out.println(classSizes);
        
        double[][] clashWeight = new double[d.getCodomain().size()][d.getCodomain().size()];
        double div;
        for(int i = 0; i < d.getCodomain().size(); i++){
            for(int j = 0; j < d.getCodomain().size(); j++){
                clashWeight[i][j] = 0;
                div = 0;
                if(i < j){
                    for(int k = i; k < j; k ++){
                        clashWeight[i][j] += classSizes.getOrDefault(d.getCodomain().get(k),0);
                    }
                    for(int k = j; k < d.getCodomain().size(); k ++){
                        div += classSizes.getOrDefault(d.getCodomain().get(k),0);
                    }
                }
                if(i > j){
                    for(int k = i; k > j; k --){
                        clashWeight[i][j] += classSizes.getOrDefault(d.getCodomain().get(k),0);
                    }
                    
                    for(int k = j; k >= 0; k --){
                        div += classSizes.getOrDefault(d.getCodomain().get(k),0);
                    }
                }
                //clashWeight[i][j] /= div; //classSizes.get(d.getCodomain().get(j));
                //clashWeight[i][j] = 1. / clashWeight[i][j];
            }
        }
        //System.out.println(Arrays.deepToString(clashWeight));

        // For each pair of nodes {a,b},
        // if they are incompatible then
        // add a to the neighborhood of b and b to the neighborhood of a
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (!nodes.get(i).compatibleWith((nodes.get(j)))) {
                    nodes.get(i).neighbors.add(nodes.get(j));
                    nodes.get(j).neighbors.add(nodes.get(i));
                    //System.out.println("Incompatible :");
                    //System.out.println(nodes.get(i));
                    //System.out.println(nodes.get(j));
                }
            }
        }

        // Sort the nodes by descending size of neighborhood
        Collections.sort(nodes,
                (n1, n2) -> {
                    return new Double(n2.incompatiblityScore(clashWeight)).compareTo(n1.incompatiblityScore(clashWeight));
                });
        
        // For each node in the list :
        // If the node has at least 1 neighbor, remove it and update neighborhoods
        // keep the list sorted
        // do it until everyone is alone
        // can be optimized I guess, but whatever
        Node node = nodes.get(0);
        while (!node.neighbors.isEmpty()) {
            //nodes.stream().forEach(n -> {System.out.print(n.neighbors.size()+",");});
            //System.out.println("\n");
            for (Node n : node.neighbors) {
                n.neighbors.remove(node);
            }

            Collections.sort(nodes,
                    (n1, n2) -> {
                        return new Integer(n2.neighbors.size()).compareTo(n1.neighbors.size());
                    });

            nodes.remove(0);
            node = nodes.get(0);
        }
        //System.out.println(d.size()+"rows to "+nodes.size());

        InstanceList res = d.getEmptyShell();
        for (Node n : nodes) {
            res.add(n.row);
        }
        return res;
    }
    
    public static InstanceList<Rank, Chain> monotonize(InstanceList<Rank, Chain> d) {
        // This local class represents a node in the incompatibility graph
        class Node {

            Instance<Rank, Chain> row;
            Set<Node> neighbors;

            Node(Instance<Rank, Chain> row) {
                this.row = row;
                this.neighbors = new HashSet<>();
            }

            boolean compatibleWith(Node other) {
                return SRTools.areCompatible(this.row, other.row);
            }

            @Override
            public String toString() {
                return this.row.toString();
            }
        }

        List<Node> nodes = new ArrayList<>();

        for (Instance<Rank, Chain> r1 : d) {
            nodes.add(new Node(r1));
        }

        // For each pair of nodes {a,b},
        // if they are incompatible then
        // add a to the neighborhood of b and b to the neighborhood of a
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (!nodes.get(i).compatibleWith((nodes.get(j)))) {
                    nodes.get(i).neighbors.add(nodes.get(j));
                    nodes.get(j).neighbors.add(nodes.get(i));
                    //System.out.println("Incompatible :");
                    //System.out.println(nodes.get(i));
                    //System.out.println(nodes.get(j));
                }
            }
        }

        // Sort the nodes by descending size of neighborhood
        Collections.sort(nodes,
                (n1, n2) -> {
                    return new Integer(n2.neighbors.size()).compareTo(n1.neighbors.size());
                });

        // For each node in the list :
        // If the node has at least 1 neighbor, remove it and update neighborhoods
        // keep the list sorted
        // do it until everyone is alone
        // can be optimized I guess, but whatever
        Node node = nodes.get(0);
        while (!node.neighbors.isEmpty()) {
            //nodes.stream().forEach(n -> {System.out.print(n.neighbors.size()+",");});
            //System.out.println("\n");
            for (Node n : node.neighbors) {
                n.neighbors.remove(node);
            }

            Collections.sort(nodes,
                    (n1, n2) -> {
                        return new Integer(n2.neighbors.size()).compareTo(n1.neighbors.size());
                    });

            nodes.remove(0);
            node = nodes.get(0);
        }
        //System.out.println(d.size()+"rows to "+nodes.size());

        InstanceList res = d.getEmptyShell();
        for (Node n : nodes) {
            res.add(n.row);
        }
        return res;
    }

    public static InstanceList<Rank, Chain> monotonizeWithRatio(InstanceList<Rank, Chain> d) {
        // This local class represents a node in the incompatibility graph
        class Node {

            Instance<Rank, Chain> row;
            Set<Node> neighbors;
            int base;

            Node(Instance<Rank, Chain> row) {
                this.row = row;
                this.neighbors = new HashSet<>();
                this.base = 0;
            }

            boolean isCompatibleWith(Node other) {
                return SRTools.areCompatible(this.row, other.row);
            }

            boolean isComparableWith(Node other) {
                Integer rel = 0;
                for (int i = 0; i < this.row.getArity(); i++) {
                    rel = OTools.cartesianProductOfRelations(rel, d.getDomain()[i].relation(this.row.getFeature(i), other.row.getFeature(i)));
                }
                return rel != null;
            }

            double clashRatio() {
                return ((double) this.neighbors.size()) / base;
            }

            @Override
            public String toString() {
                return this.row.toString();
            }
        }

        List<Node> nodes = new ArrayList<>();

        for (Instance<Rank, Chain> r1 : d) {
            nodes.add(new Node(r1));
        }

        // For each pair of nodes {a,b},
        // if they are incompatible then
        // add a to the neighborhood of b and b to the neighborhood of a
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (nodes.get(i).isComparableWith(nodes.get(j))) {
                    nodes.get(i).base++;
                    nodes.get(j).base++;
                }
                if (!nodes.get(i).isCompatibleWith((nodes.get(j)))) {
                    nodes.get(i).neighbors.add(nodes.get(j));
                    nodes.get(j).neighbors.add(nodes.get(i));
                }
            }
        }

        // Sort the nodes by descending size of neighborhood
        Collections.sort(nodes,
                (n1, n2) -> new Double(n2.clashRatio()).compareTo(n1.clashRatio())
        );

        // For each node in the list :
        // If the node has at least 1 neighbor, remove it and update neighborhoods
        // keep the list sorted
        // do it until everyone is alone
        // can be optimized I guess, but whatever
        Node node = nodes.get(0);
        while (!node.neighbors.isEmpty()) {
            //nodes.stream().forEach(n -> {System.out.print(n.neighbors.size()+",");});
            //System.out.println("\n");
            for (Node n : node.neighbors) {
                n.neighbors.remove(node);
                n.base--;
            }

            Collections.sort(nodes,
                    (n1, n2) -> new Double(n2.clashRatio()).compareTo(n1.clashRatio())
            );

            nodes.remove(0);
            node = nodes.get(0);
        }
        //System.out.println(d.size()+"rows to "+nodes.size());

        InstanceList res = d.getEmptyShell();
        for (Node n : nodes) {
            res.add(n.row);
        }

        return res;
    }

    ////////////////////////////////////
    //                                //
    // FLEXERPOLATORS IMPLEMENTATIONS //
    //                                //
    //////////////////////////////////// 
    public static Collection<SugenoUtility> flexerpolate(InstanceList<Rank, Chain> d, InstanceList<Rank, Chain> restrictions) {
        List<SugenoUtility> coverage = new ArrayList<>();
        boolean covered;

        for (Instance<Rank, Chain> example : d) {
            covered = false;
            for (SugenoUtility su : coverage) {
                if (su.coverExample(example, restrictions)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                coverage.add(new SugenoUtility(d.getDomain(), d.getCodomain()));
                covered = coverage.get(coverage.size() - 1).coverExample(example, restrictions.getEmptyShell());
                if (!covered) {
                    coverage.remove(coverage.size() - 1);
                }
            }

            Collections.sort(coverage,
                    (SugenoUtility s1, SugenoUtility s2) -> {
                        return new Integer(s2.getAddedExamples().size()).compareTo(s1.getAddedExamples().size());
                    });
            //Collections.shuffle(coverage);
        }

        return coverage;
    }

    public static Collection<SugenoUtility> pruneSUs(Collection<SugenoUtility> coverage, InstanceList<Rank, Chain> dataset, Double minCoverageSize) {
        List<SugenoUtility> candidates = new ArrayList<>(coverage);

        int[] goodOnes = new int[coverage.size()];
        int[] lowerOnes = new int[coverage.size()];
        int[] higherOnes = new int[coverage.size()];

        Rank prediction;
        Rank real;
        Chain codomain = dataset.getCodomain();
        Integer r;

        for (Instance<Rank, Chain> row : dataset) {
            for (int i = 0; i < coverage.size(); i++) {
                prediction = candidates.get(i).getOutput(row.getFeatures());
                real = row.getLabel();
                r = codomain.relation(prediction, real);
                if (r == 0) {
                    goodOnes[i]++;
                } else if (r < 0) {
                    higherOnes[i]++;
                } else {
                    lowerOnes[i]++;
                }
            }
        }

        System.out.println("\t" + Arrays.toString(goodOnes));
        System.out.println("\t" + Arrays.toString(lowerOnes));
        System.out.println("\t" + Arrays.toString(higherOnes));
        return coverage;
    }

    private static boolean acceptToMove(boolean add, double delta, double acc1, double acc2) {
        if (add) {
            return false;
        } else {
            return (acc2 / acc1) > delta;
        }
    }

    /**
     * Might be wrong. Might not be faster either.
     *
     * @param coverage
     * @param dataset
     * @param delta
     * @return
     */
    public static Collection<SugenoUtility> findBestSUSubsetGreedyWithArrays(Collection<SugenoUtility> coverage, InstanceList<Rank, Chain> dataset, double delta) {
        Set<SugenoUtility> inside = new HashSet<>(coverage);
        Chain L = dataset.getCodomain();

        List<Instance<Rank, Chain>> rows = new ArrayList<>(dataset);
        int[] tooHigh = new int[rows.size()]; //Array for counting the number of SUF that evaluate each row too high
        int[] good = new int[rows.size()]; //Array for counting the number of SUF that evaluate each row just right
        Map<SugenoUtility, int[]> mapTH = new HashMap<>();
        Map<SugenoUtility, int[]> mapG = new HashMap<>();

        int relation;
        int[] th;
        int[] g;

        for (SugenoUtility su : coverage) {
            th = new int[dataset.size()];
            g = new int[dataset.size()];
            mapTH.put(su, th);
            mapG.put(su, g);
            for (int i = 0; i < rows.size(); i++) {
                relation = L.relation(su.getOutput(rows.get(i).getFeatures()), rows.get(i).getLabel());
                if (relation == 0) {
                    good[i]++;
                    g[i]++;
                } else if (relation == -1) {
                    tooHigh[i]++;
                    th[i]++;
                }
            }
        }

        SugenoUtility su;
        double accuracyA = 0.;
        double accuracyB;

        for (int i = 0; i < rows.size(); i++) {
            if (good[i] > 0 && tooHigh[i] > 0) {
                accuracyA++;
            }
        }
        accuracyA /= rows.size();

        boolean goOn = true;
        while (goOn) {
            goOn = false;

            Iterator<SugenoUtility> it = inside.iterator();
            while (it.hasNext()) {
                su = it.next();
                g = mapG.get(su);
                th = mapTH.get(su);

                accuracyB = 0.;
                for (int i = 0; i < rows.size(); i++) {
                    if (good[i] - g[i] > 0 && tooHigh[i] - th[i] > 0) {
                        accuracyB++;
                    }
                }
                accuracyB /= rows.size();

                if (accuracyB >= accuracyA * delta) {
                    it.remove();
                    for (int i = 0; i < rows.size(); i++) {
                        good[i] -= g[i];
                        tooHigh[i] -= th[i];
                        goOn = true;
                    }
                }
            }
        }

        return inside;
    }

    public static Collection<SugenoUtility> findBestSUSubsetGreedy(Collection<SugenoUtility> coverage, InstanceList<Rank, Chain> dataset, double rho) {
        LinkedList<SugenoUtility> inside = new LinkedList<>(coverage);

        double accuracy = DataTools.testAccuracy(inside, dataset, false);
        double challengerScore;

        SugenoUtility su;

        boolean goOn = true;
        int loopSize;
        while (goOn) {
            goOn = false;
            loopSize = inside.size();
            for (int i = 0; i < loopSize; i++) {
                su = inside.remove();
                challengerScore = DataTools.testAccuracy(inside, dataset, false);
                if (acceptToMove(false, rho, accuracy, challengerScore)) {
                    accuracy = challengerScore;
                    goOn = true;
                } else {
                    inside.add(su);
                }
            }
        }

        return inside;
    }

    public static Collection<SugenoUtility> findBestSUSubsetSophisticated(Collection<SugenoUtility> coverage, InstanceList<Rank, Chain> dataset, double delta) {

        //System.out.println("coucou");
        LinkedList<SugenoUtility> inside = new LinkedList<>(coverage);
        LinkedList<SugenoUtility> outside = new LinkedList<>(coverage);

        double accuracy = DataTools.testAccuracy(inside, dataset, false);
        double challengerScore;

        SugenoUtility su;

        boolean goOn = true;
        int loopSize;
        while (goOn) {
            //System.out.println(inside.size());
            goOn = false;
            loopSize = inside.size();
            for (int i = 0; i < loopSize; i++) {
                su = inside.remove();
                challengerScore = DataTools.testAccuracy(inside, dataset, false);
                //if(accuracyB >= accuracyA){
                if (acceptToMove(false, delta, accuracy, challengerScore)) {
                    accuracy = challengerScore;
                    goOn = true;
                    outside.add(su);
                    //System.out.println(inside.size());
                } else {
                    inside.add(su);
                }
            }

            loopSize = outside.size();
            for (int i = 0; i < loopSize; i++) {
                su = outside.remove();
                inside.add(su);
                challengerScore = DataTools.testAccuracy(inside, dataset, false);
                //if(accuracyB > accuracyA){
                if (acceptToMove(true, delta, accuracy, challengerScore)) {
                    accuracy = challengerScore;
                    goOn = true;
                    //System.out.println(inside.size());
                } else {
                    inside.removeLast();
                    outside.add(su);

                }
            }
        }

        return inside;
    }
}
