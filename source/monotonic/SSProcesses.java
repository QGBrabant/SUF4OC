/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import chains.Chain;
import orders.OrderTools;
import tuples.TupleImpl;
import orders.Order;
import orders.PartiallyOrderable;
import orders.MinSet;

public class SSProcesses {
    ////////////////////////////////
    //                            //
    // MAX-SUF LEARNING PROCESSES //
    //                            //
    ////////////////////////////////
    
    public static MaxSUF SUF_COVERING(RuleSet R) {
        SugenoUtility su;
        Chain[] domain =R.getDomain();
        Chain codomain = R.getCoDomain();
        InstanceList listR = new InstanceList(domain,codomain);
        listR.addAll(R);
        Collections.shuffle(listR);

        //Initialize the list of Sugeno utilities with one utility
        MaxSUF covering = new MaxSUF();
        covering.add(new SugenoUtility(domain, codomain));

        //A list just for understanding
        List<Instance> listForU = new ArrayList<>();

        //Try to cover each rule of the set
        for (Instance r : listR) {
            listForU.add(r);

            //Try to cover the rule with each Sugeno utility, until one accept it
            boolean covered = false;
            for (SugenoUtility S : covering) {
                covered = S.coverRule(r, R);
                if (covered) {
                    assert S.apply(new TupleImpl(r.copyOfFeatures())) == r.getLabel() : new TupleImpl(r.copyOfFeatures()) + " , " + r.getLabel() + " , " + S.apply(new TupleImpl(r.copyOfFeatures())) + "\n" + listForU + "\n" + covering;
                    break;
                }
            }
            //If no SU accepted the rule, create a new SU and add it to the set
            //then add the rule to the new SU
            if (!covered) {
                su = new SugenoUtility(domain, codomain);
                covering.add(su);
                covered = su.coverRule(r, R);
                assert covered : "assert:"+R+"\n"+r;
            }
            Collections.sort(covering,
                    (su1, su2) -> {
                        return new Integer(su1.getAddedExamples().size()).compareTo(su2.getAddedExamples().size());
                    });
        }

        /*for (Instance r : listR) {
            int truc = 0;
            for (SugenoUtility sug : covering) {
                truc = Math.max(truc, sug.apply(new TupleImpl(r.copyOfFeatures())));
                assert sug.apply(new TupleImpl(r.copyOfFeatures())) <= r.getLabel() : new TupleImpl(r.copyOfFeatures()) + " , " + r.getLabel() + " , " + sug.apply(new TupleImpl(r.copyOfFeatures())) + "\n" + listR + "\n" + sug;
            }
            assert truc == r.getLabel() : new TupleImpl(r.copyOfFeatures()) + " , " + r.getLabel() + " , " + truc + "\n" + listR + "\n" + covering;
        }*/

        return covering;
    }
    
     public static MaxSUF SUF_INTERPOLATION_2(InstanceList dataset) {
        //InstanceList d1 = optimalRelabeling(dataset);
        InstanceList d2 = pruneCriteria(dataset.reduction(), dataset).reduction();
        Collections.shuffle(d2);
        MaxSUF sus = flexerpolate(d2, dataset);

        return sus;
    }
    
    public static MaxSUF SUF_INTERPOLATION_1(InstanceList dataset) {
        //InstanceList d1 = optimalRelabeling(dataset);
        InstanceList d1 = dataset.reduction();
        Collections.shuffle(d1);
        MaxSUF sus = flexerpolate(d1, dataset);

        return sus;
    }
    
    public static MaxSUF PROCESS_1(InstanceList dataset, Double[] params) {
        double rho = params[0];

        InstanceList d1 = optimalRelabeling(dataset);

        MaxSUF sus = SUF_INTERPOLATION_1(d1);

        if (rho <= 1) {
            sus = findBestSUSubsetGreedy(sus, d1, rho);
        }
        for (SugenoUtility su : sus) {
            su.clean();
        }
        return sus;
    }
    
    public static MaxSUF PROCESS_2(InstanceList dataset, Double[] params) {
        double rho = params[0];
        
        InstanceList d1 = optimalRelabeling(dataset);

        MaxSUF sus = SUF_INTERPOLATION_2(d1);

        if (rho <= 1) {
            sus = findBestSUSubsetGreedy(sus, d1, rho);
        }
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
    
    public static RuleSet twoSidedRuleLearning(InstanceList dataset){
        InstanceList d = optimalRelabeling(dataset);
        InstanceList dInv = d.inverse();
        
        RuleSet selectSet = new RuleSet(pruneCriteria(d.reduction(), d).reduction());
        RuleSet rejectSet = new RuleSet(pruneCriteria(dInv.reduction(), dInv).reduction());
        rejectSet.setRejection(true);
        
        return selectSet.size() > rejectSet.size() ? selectSet : rejectSet;
    }
    
    public static RuleSet SRL(InstanceList dataset) {
        InstanceList d1 = optimalRelabeling(dataset);
        InstanceList d2 = pruneCriteria(d1.reduction(), d1).reduction();

        return new RuleSet(d2);
    }
    
    public static Classifier LAMBDA_RULE_SET(InstanceList dataset) {
        InstanceList d1 = optimalRelabeling(dataset);

        return new RuleSet(d1);
    }

    //////////////////////////
    //                      //
    // RULE-SETS COMPLETION //
    //                      //
    //////////////////////////


    private static RuleSet analogicalCompletion(InstanceList rules, InstanceList dataset) {
        Chain[] domain = rules.getDomain();
        Chain codomain = rules.getCodomain();

        RuleSet res = new RuleSet(rules);
        rules = rules.getEmptyShell();
        rules.addAll(res);

        int arity = rules.getDomain().length;

        int[] effect = new int[arity];
        int[] newCondition = new int[arity];

        Instance candidate;
        System.out.println("Nb rules "+rules.size());
        for(Instance r1 : rules){
            for(Instance r2 : rules){
                
                if(r1.getLabel() != r2.getLabel()){

                    Arrays.fill(effect, -1);
                    for (int k = 0; k < arity; k++) {
                        if ((r1.getLabel() < r2.getLabel()) == (r1.getFeature(k) < r2.getFeature(k))) {
                            effect[k] = r2.getFeature(k);
                        }
                    }

                    for(Instance r3 : rules){
                        candidate = null;
                        
                        boolean getTheFuckOut = false;
                        for (int k = 0; k < arity && !getTheFuckOut; k++) {
                            if(effect[k] == -1 && ((r3.getFeature(k) <= r1.getFeature(k) && (r1.getLabel() > r2.getLabel())))){
                                getTheFuckOut = true;
                            }
                            if(r3.getFeature(k) >= r1.getFeature(k) && r1.getLabel() < r2.getLabel()){
                                getTheFuckOut = true;
                            }
                            newCondition[k] = effect[k] == -1 ? r3.getFeature(k) : effect[k];
                        }
                        if (!getTheFuckOut
                                && (r1.getLabel() < r2.getLabel() && r3.getLabel() >= r1.getLabel() && r3.getLabel() < r2.getLabel())
                                || (r1.getLabel() > r2.getLabel() && r3.getLabel() <= r1.getLabel() && r3.getLabel() > r2.getLabel())) {
                            candidate = new Instance(newCondition, r2.getLabel(), domain, codomain);
                        }

                        if (candidate != null && ruleDoesNotOverrates(candidate, dataset)) {
                            res.add(candidate);
                        }
                    }
                }
            }
        }
        System.out.println("We tried so hard, and got so faaaar: "+res.size());
        return res;
    }

    public static boolean ruleDoesNotOverrates(Instance rule, InstanceList dataset) {
        for (Instance example : dataset) {
            if (rule.getLabel() > example.getLabel() && SRTools.satisfies(rule, example.getFeatures())) {
                return false;
            }
        }
        return true;

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
        Instance row;

        public Example(Instance row) {
            this.row = row;
            this.criteria = new Criterion[row.getDomain().length];
            for (int i = 0; i < this.criteria.length; i++) {
                this.criteria[i] = new Criterion(i);
            }
        }
    }

    /**
     * Initialize a list of Examples from the data. Both parameters can be the
     * same object.
     *
     * @param rules set of instance the set of Example is build from.
     * @param dataset set of instances for calculating criteria usage.
     * @return the list of Examples with criteria usage of each instance.
     */
    private static List<Example> buildExamples(InstanceList rules, InstanceList dataset) {
        Chain[] domain = rules.getDomain();
        Chain codomain = rules.getCodomain();


        /* Make an Example out of each row
        For each example, count the number of time each criterion is useful for keeping monotonicity of the dataset */
        List<Example> examples = new ArrayList<>();
        for (Instance row : rules) {
            examples.add(new Example(row));
        }

        Integer r;
        Integer r2;
        for (Example e : examples) {
            for (Instance instance : dataset) {
                r = OrderTools.relation(e.row.getLabel(), instance.getLabel());

                if (r != 0) {
                    for (int k = 0; k < domain.length; k++) {
                        /*if (OrderTools.relation(
                                e.row.getFeature(k),
                                instance.getFeature(k))
                                .equals(r)) {
                            e.criteria[k].useCount++;
                        }*/
                        r2 = OrderTools.relation(
                                e.row.getFeature(k),
                                instance.getFeature(k));
                        
                        if((r >= 0 && r2 >= 0) || (r < 0 && r2 < 0)){
                            e.criteria[k].useCount++;
                        }
                    }
                }
            }
        }
        return examples;
    }
    

    private static List<Example> buildExamples2(InstanceList rules, InstanceList dataset) {
        Chain[] domain = rules.getDomain();
        Chain codomain = rules.getCodomain();


        /* Make an Example out of each row
        For each example, count the number of time each criterion is useful for keeping monotonicity of the dataset */
        List<Example> examples = new ArrayList<>();
        for (Instance row : rules) {
            examples.add(new Example(row));
        }

        Integer r;
        Integer r2;
        for (Example e : examples) {
            for (Instance instance : rules) {
                r = OrderTools.relation(e.row.getLabel(), instance.getLabel());

                //if (r <= 0) {
                    for (int k = 0; k < domain.length; k++) {
                        r2 = OrderTools.relation(
                                e.row.getFeature(k),
                                instance.getFeature(k));
                        
                        if((r <= 0 && r2 > 0) || (r >= 0 && r2 < 0)){
                            e.criteria[k].useCount++;
                        }
                    }
                //}
            }
        }
        return examples;
    }

    /**
     * Compute a set of rules which is identical to the first parameter, with
     * some criteria set to 0. The pruning of criteria is constrained by the
     * second parameter: the resulting set of rules is constrained
     *
     * @param rules
     * @param dataset
     * @return
     */
    public static InstanceList pruneCriteria(InstanceList rules, InstanceList dataset) {
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

        InstanceList res = dataset.getEmptyShell();
        int[] input;
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
                for (Instance r : dataset) {
                    if (e.row.getLabel() > r.getLabel()) {
                        ok = false;
                        for (Couple c2 : list) {
                            if (c1 != c2) {
                                if (e.row.getFeature(c2.i) > r.getFeature(c2.i)) {
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
                    input[c1.i] = 0;
                }
            }

            res.add(new Instance(input, e.row.getLabel(), dataset.getDomain(), dataset.getCodomain()));
        }

        //ouiSystem.out.println("Removal count: "+removalCount);
        return res;

    }

    //////////////////////////////////
    //                              //
    // MONOTONIZERS IMPLEMENTATIONS //
    //                              //
    //////////////////////////////////
    //OPTIMAL RELABELING START/////////////////////////////////////////////////
    public static InstanceList optimalRelabeling(InstanceList d) {
        /**
         * Nodes that contain instances to relabelize
         */
        class Node implements PartiallyOrderable {

            int[] x;
            MinSet<Node> successors; //direct successors in the graph
            boolean open;
            int linksToSource;
            int linksToSink;
            int[] counter;
            Node lastSeen; //for optimizing graph construction

            Node(int[] x, Order<Node> order) {
                this.x = x;
                this.successors = new MinSet<>(order);
                open = true;
                if (x != null) {
                    counter = new int[d.getCodomain().size()];
                    Arrays.fill(counter, 0);
                }
                lastSeen = null;
            }

            void fillUpperSet(Set<Node> upperSet) {
                if (upperSet.add(this)) {
                    for (Node n : this.successors) {
                        n.fillUpperSet(upperSet);
                    }
                }
            }

            public void setLinks(int i) {
                this.linksToSource = 0;
                this.linksToSink = 0;
                for (int j = 0; j < this.counter.length; j++) {
                    if (j <= i) {
                        linksToSink += counter[j];
                    } else {
                        linksToSource += counter[j];
                    }
                }
                open = true;
            }

            public boolean search() {
                if (!this.open) {
                    return false;
                }
                if (this.linksToSink > 0) {
                    this.linksToSink--;
                    return true;
                }
                for (Node succ : this.successors) {
                    if (succ.search()) {
                        return true;
                    }
                }
                open = false;
                return false;
            }

            @Override
            public String toString() {
                return Arrays.toString(x) + " -> " + Arrays.toString(counter);
            }

            @Override
            public boolean equals(Object o) {
                Integer rel = ((Node) o).relation(this);
                return rel != null && rel == 0;
            }

            @Override
            public Integer relation(Object o) {
                return OrderTools.componentWiseRelation(this.x, ((Node) o).x);
            }

            public void addSuccessor(Node n) {
                this.lastSeen = n;
                boolean add = this.successors.add(n);
                if (!add) {
                    for (Node s : successors) {
                        if (n != s.lastSeen) {
                            s.addSuccessor(n);
                        }
                    }
                }
            }
        }

        InstanceList monotoneData = d.getEmptyShell();

        Map<TupleImpl, Node> nodes = new HashMap<>();
        Order<Node> order = new Order.VanillaImpl<>();
        Node node;
        TupleImpl tuple;
        for (Instance inst : d) {
            tuple = new TupleImpl(inst.copyOfFeatures());
            node = nodes.get(tuple);
            if (node == null) {
                node = new Node(inst.copyOfFeatures(), order);
                nodes.put(tuple, node);
            }
            node.counter[inst.getLabel()]++;
        }

        List<Node> list = new ArrayList<>(nodes.values());
        Node n1;
        Node n2;
        Integer rel;
        for (int i = 0; i < list.size(); i++) {
            n1 = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                n2 = list.get(j);
                rel = n1.relation(n2);
                if (rel != null) {
                    assert rel != 0;
                    if (rel > 0) {
                        n1.successors.add(n2);
                    } else {
                        n2.successors.add(n1);
                    }
                }
            }
        }

        Set<Node> X = new HashSet<>(nodes.values());
        Set<Node> MUS = new HashSet<>();

        for (int l = 0; l < d.getCodomain().size(); l++) {
            int label = l;

            if (label != d.getCodomain().size() - 1) {
                for (Node n : X) {
                    n.setLinks(label);
                }

                for (Node n : X) {
                    while (n.linksToSource > 0 && n.search()) {
                        n.linksToSource--;
                    }
                }
                MUS.clear();
                for (Node n : X) {
                    if (n.linksToSource > 0) {
                        n.fillUpperSet(MUS);
                    }
                }
                X.removeAll(MUS);
            }

            for (Node n : X) {
                for (int i = 0; i < d.getCodomain().size(); i++) {
                    for (int j = 0; j < n.counter[i]; j++) {
                        monotoneData.add(new Instance(n.x, label, d.getDomain(), d.getCodomain()));
                    }
                }
            }
            X = new HashSet<>(MUS);
        }
        
        return monotoneData;

    }

    ///////////////////////////////////////////////////OPTIMAL RELABELING END//
    public static InstanceList monotonize2(InstanceList d) {
        // This local class represents a node in the incompatibility graph
        class Node {

            Instance row;
            Set<Node> neighbors;

            Node(Instance row) {
                this.row = row;
                this.neighbors = new HashSet<>();
            }

            boolean compatibleWith(Node other) {
                return SRTools.areCompatible(this.row, other.row);
            }

            double incompatiblityScore(double[][] clashWeight) {
                double res = 0;
                for (Node n : this.neighbors) {
                    res += clashWeight[this.row.getLabel()][n.row.getLabel()];
                }

                return res;
            }

            @Override
            public String toString() {
                return this.row.toString();
            }
        }

        List<Node> nodes = new ArrayList<>();
        Map<Integer, Integer> classSizes = new HashMap<>();

        for (Instance r1 : d) {
            nodes.add(new Node(r1));
            classSizes.put(r1.getLabel(), classSizes.getOrDefault(r1.getLabel(), 0) + 1);
        }
        //System.out.println(classSizes);

        double[][] clashWeight = new double[d.getCodomain().size()][d.getCodomain().size()];
        double div;
        for (int i = 0; i < d.getCodomain().size(); i++) {
            for (int j = 0; j < d.getCodomain().size(); j++) {
                clashWeight[i][j] = 0;
                div = 0;
                if (i < j) {
                    for (int k = i; k < j; k++) {
                        clashWeight[i][j] += classSizes.getOrDefault(k, 0);
                    }
                    for (int k = j; k < d.getCodomain().size(); k++) {
                        div += classSizes.getOrDefault(k, 0);
                    }
                }
                if (i > j) {
                    for (int k = i; k > j; k--) {
                        clashWeight[i][j] += classSizes.getOrDefault(k, 0);
                    }

                    for (int k = j; k >= 0; k--) {
                        div += classSizes.getOrDefault(k, 0);
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

    public static InstanceList monotonize(InstanceList d) {
        // This local class represents a node in the incompatibility graph
        class Node {

            Instance row;
            Set<Node> neighbors;

            Node(Instance row) {
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

        for (Instance r1 : d) {
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

    public static InstanceList monotonizeWithRatio(InstanceList d) {
        // This local class represents a node in the incompatibility graph
        class Node {

            Instance row;
            Set<Node> neighbors;
            int base;

            Node(Instance row) {
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
                    rel = OrderTools.productOfRelations(rel, OrderTools.relation(this.row.getFeature(i), other.row.getFeature(i)));
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

        for (Instance r1 : d) {
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
    public static MaxSUF flexerpolate(InstanceList d, InstanceList restrictions) {
        MaxSUF coverage = new MaxSUF();
        boolean covered;

        for (Instance example : d) {
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

    public static MaxSUF pruneSUs(MaxSUF coverage, InstanceList dataset, Double minCoverageSize) {
        List<SugenoUtility> candidates = new ArrayList<>(coverage);

        int[] goodOnes = new int[coverage.size()];
        int[] lowerOnes = new int[coverage.size()];
        int[] higherOnes = new int[coverage.size()];

        int prediction;
        int real;
        Chain codomain = dataset.getCodomain();
        Integer r;

        for (Instance row : dataset) {
            for (int i = 0; i < coverage.size(); i++) {
                prediction = candidates.get(i).apply(row.getFeatures());
                real = row.getLabel();
                r = OrderTools.relation(prediction, real);
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

    private static boolean acceptToMove(boolean add, double delta, double err1, double err2) {
        if (add) {
            return false;
        } else {
            return (err2 / err1) >= delta;
        }
    }

    public static MaxSUF findBestSUSubsetGreedy(MaxSUF coverage, InstanceList dataset, double rho) {

        MaxSUF inside = new MaxSUF(coverage);

        /*System.out.println("WOW!!");
        for(SugenoUtility su : inside){
            System.out.println(su);
        }*/
        double error = 1. - DataTools.MER(inside, dataset);
        double challengerScore;

        SugenoUtility su;

        boolean goOn = true;
        int loopSize;
        while (goOn) {
            goOn = false;
            loopSize = inside.size();
            for (int i = 0; i < loopSize; i++) {
                su = inside.get(0);
                inside.remove(0);

                challengerScore = 1. - DataTools.MER(inside, dataset);
                assert challengerScore <= error;
                //System.out.println(error+","+challengerScore+" ... "+rho);
                if (acceptToMove(false, rho, error, challengerScore)) {
                    error = challengerScore;
                    goOn = true;
                } else {
                    inside.add(su);
                }
            }
        }

        return inside;
    }

    public static MaxSUF findBestSUSubsetSophisticated(MaxSUF coverage, InstanceList dataset, double delta) {

        //System.out.println("coucou");
        MaxSUF inside = new MaxSUF(coverage);
        MaxSUF outside = new MaxSUF(coverage);

        double accuracy = DataTools.MAE(inside, dataset);
        double challengerScore;

        SugenoUtility su;

        boolean goOn = true;
        int loopSize;
        while (goOn) {
            //System.out.println(inside.size());
            goOn = false;
            loopSize = inside.size();
            for (int i = 0; i < loopSize; i++) {
                su = inside.get(0);
                inside.remove(0);
                challengerScore = DataTools.MAE(inside, dataset);
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
                su = outside.get(0);
                outside.remove(0);
                inside.add(su);
                challengerScore = DataTools.MAE(inside, dataset);
                //if(accuracyB > accuracyA){
                if (acceptToMove(true, delta, accuracy, challengerScore)) {
                    accuracy = challengerScore;
                    goOn = true;
                    //System.out.println(inside.size());
                } else {
                    inside.remove(inside.size() - 1);
                    outside.add(su);

                }
            }
        }

        return inside;
    }
}
