/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import miscellaneous.Tuple;
import lattices.Chain;
import lattices.impls.BTupleImpl;
import orders.impls.MaxSet;
import lattices.impls.Rank;

/**
 *
 * @author qgbrabant
 */
public class SugenoUtility implements Serializable {

    final private int arity;
    final private Chain[] domain;
    final private Chain codomain;
    private final Node bottom;
    private final Node top;

    private Rank[][] phis;
    private Set<Node> nodes;
    private List<Instance<Rank,Chain>> added;

    private List<Instance<Rank, Chain>> addedExamples;
    private List<Instance<Rank, Chain>> rejectedExamples;

    public SugenoUtility(Chain[] domain, Chain codomain) {
        this.domain = Arrays.copyOf(domain, domain.length);
        this.codomain = codomain;
        this.arity = domain.length;
        this.nodes = new HashSet<>();

        Boolean[] bin = new Boolean[this.arity];
        Arrays.fill(bin, false);
        this.bottom = new Node(bin, codomain.getBottom(), this.codomain);
        this.nodes.add(this.bottom);

        Arrays.fill(bin, true);
        this.top = new Node(bin, codomain.getTop(), this.codomain);
        this.nodes.add(this.top);

        this.phis = new Rank[arity][];
        for (int i = 0; i < this.domain.length; i++) {
            phis[i] = new Rank[this.domain[i].size()];
            Arrays.fill(phis[i], this.codomain.getBottom());
            phis[i][this.domain[i].size() - 1] = this.codomain.getTop();
        }
        this.added = new ArrayList<>();
        this.addedExamples = new ArrayList<>();
        this.rejectedExamples = new ArrayList<>();
    }

    public boolean coverExample(Instance<Rank, Chain> example, Collection<Instance<Rank, Chain>> restrictions) {
        Rank[][] oldPhis = this.copyOfPhis();
        Set<Node> oldNodes = this.copyOfNodes();

        LinearInstance rule
                = new LinearInstance(example.copyOfFeatures(), example.getLabel(), example.getDomain(), example.getCodomain());

        this.updateWithRule(rule);

        //Test if it does contradict the dataset
        for (Instance<Rank, Chain> row : restrictions) {
            if(example.getLabel().toInt() > row.getLabel().toInt()){
                if (this.getOutput(row.getFeatures()).toInt() > row.getLabel().toInt()) {
                    this.phis = oldPhis;
                    this.nodes = oldNodes;
                    this.rejectedExamples.add(example);
                    return false;
                }
            }
        }

        this.addedExamples.add(example);
        return true;

    }

    private class Node extends BTupleImpl {

        public Rank val;
        public Chain domain;

        public Node(Boolean[] bin, Rank val, Chain domain) {
            super(bin);
            this.val = val;
            this.domain = domain;
        }

        public Node(Node n) {
            super(n.toArray());
            this.val = n.val;
            this.domain = n.domain;
        }

        @Override
        public boolean equals(Object o) {
            Node cn = (Node) o;
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i) != cn.get(i)) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            String s = super.toString();
            s += "-<" + this.val + ">";
            return s;
        }
    }

    private void fillMin(Node n) {
        n.val = this.codomain.getBottom();
        for (Node n2 : this.nodes) {
            Integer r = n.relation(n2);
            if (r != null) {
                if (r == 0) {
                    n.val = n2.val;
                    break;
                }
                if (r < 0) {
                    n.val = this.codomain.join(n.val, n2.val);
                }
            }
        }
    }

    private void increaseNode(Node cn, Rank x) {
        for (Node cn2 : this.nodes) {
            Integer r = cn2.relation(cn);
            if (r != null && r <= 0) {
                cn2.val = this.codomain.join(cn2.val, x);
            }
        }
    }

    private Rank[][] copyOfPhis() {
        Rank[][] oldPhis = this.phis;
        this.phis = new Rank[this.arity][];
        for (int i = 0; i < this.arity; i++) {
            this.phis[i] = Arrays.copyOf(oldPhis[i], oldPhis[i].length);
        }

        return oldPhis;
    }

    private HashSet<Node> copyOfNodes() {
        HashSet<Node> oldNodes = new HashSet<>();
        for (Node n : this.nodes) {
            oldNodes.add(new Node(n));
        }
        return oldNodes;
    }

    private void updateWithRule(Instance<Rank,Chain> rule) {
        Boolean[] bin = new Boolean[this.domain.length];
        for (int i = 0; i < this.arity; i++) {
            if (!this.domain[i].getBottom().equals(rule.getFeature(i))) {
                bin[i] = true;
                for (int j = 0; j < this.phis[i].length; j++) {
                    //System.out.println(example);
                    //assert example.getInput().get(i) != null : example;
                    if (j >= rule.getFeature(i).toInt()) {
                        this.phis[i][j] = this.codomain.join(this.phis[i][j], rule.getLabel());
                    }
                }
            } else {
                bin[i] = false;
            }
        }
        Node nodeA = new Node(bin, this.codomain.getBottom(), this.codomain);
        fillMin(nodeA);
        this.nodes.add(nodeA);

        this.increaseNode(nodeA, rule.getLabel());
    }

    /**
     * Try to cover the rule. If possible, the SugenoUtility is updated and the
     * function return true. Else, nothing happens and the function returns
     * false.
     *
     * @param r
     * @return
     */
    public boolean coverRule(Instance<Rank,Chain> r) {
        if(r.getLabel().equals(r.getCodomain().getBottom())){
            return true;
        }
        MaxSet<Instance<Rank,Chain>> ruleSet = this.toRules();

        Rank[][] oldPhis = this.copyOfPhis();

        Set<Node> oldNodes = this.copyOfNodes();

        this.updateWithRule(r);

        //Test whether R U {r} is equivalent to the current SUF
        ruleSet.add(r);
        MaxSet<Instance<Rank,Chain>> ruleSet2 = this.toRules();

        if (!ruleSet.equals(ruleSet2)) {
            /*System.out.println("Not equal :");
            System.out.println(r);
            System.out.println(ruleSet);
            System.out.println(ruleSet2);*/
            this.phis = oldPhis;
            this.nodes = oldNodes;
            //this.rejectedExamples.add(example);
            return false;
        }

        //Test if it does contradict the dataset
        /*for (OCRow<Rung, Chain> row : restrictions) {
            if (this.getOutput(row.getInput()).toInt() > row.getOutput().toInt()) {
                this.phis = oldPhis;
                this.nodes = oldNodes;
                this.rejectedExamples.add(example);
                return false;
            }
        }*/
        //this..add(example);
        return true;
    }

    public Rank getOutput(Tuple<Rank> input) {
        //Apply the local utility functions on the input tuple
        Rank[] tab = new Rank[arity];
        for (int i = 0; i < arity; i++) {
            tab[i] = this.phis[i][input.get(i).toInt()];
        }
        return getMinOutput(tab);

    }

    /**
     * Removes redundant nodes.
     */
    public void clean() {
        List<Node> list = new ArrayList<>(this.nodes);
        int i = 0;
        int j;
        Integer r1;
        Integer r2;
        while (i < list.size()) {
            j = i + 1;
            while (j < list.size()) {
                r1 = list.get(i).relation(list.get(j));
                if (r1 != null) {

                    assert r1 != 0;
                    r2 = list.get(i).val.relation(list.get(j).val);

                    if (r1 > 0 && r2 <= 0) {
                        list.remove(j);
                        j--;
                    } else if (r1 < 0 && r2 >= 0) {
                        list.remove(i);
                        i--;
                        break;
                    }
                }
                j++;
            }
            i++;
        }
        this.nodes.clear();
        this.nodes.addAll(list);
    }

    //Result is a variable for border effects on the computed result, allowing coordination between the branches of recursion.
    private Rank getMinOutput(Rank[] input) {
        int d = this.codomain.getDimensionality();
        Rank result = this.codomain.getBottom();
        Rank threshold;
        for (Node cn : this.nodes) {
            threshold = cn.val;
            for (int i = 0; i < input.length && (threshold.toInt()> result.toInt()) ; i++) {
                if (cn.get(i)) {
                    threshold = this.codomain.meet(threshold, input[i]);
                }
            }
            result = this.codomain.join(result, threshold);
        }
        return result;
    }

    @Override
    public String toString() {
        String s = "\n<<\n" + this.nodes + "\n";
        for (Rank[] tab : this.phis) {
            s += "[";
            for (Rank m : tab) {
                s += m + ",";
            }
            s += "]\n";
        }
        s += this.added + "\n";
        s += this.addedExamples + "\n";
        s += this.rejectedExamples + "\n";
        s += ">>\n";
        return s;
    }

    public String getSummary() {
        String res = "Capacity: ";
        int[] counts = new int[this.arity];
        int n;
        for (Node node : this.nodes) {
            n = node.getNumberOfOnes() - 1;
            if (n > 0) {
                counts[n]++;
            }
        }
        for (int i = 0; i < this.arity; i++) {
            if (counts[i] != 0) {
                res += i + ":" + counts[i] + ", ";
            }
        }
        return res;
    }

    public List<Instance<Rank, Chain>> getAddedExamples() {
        return addedExamples;
    }

    public int getCapacityComplexity() {
        int c = 0;
        int dc;
        for (Node node : this.nodes) {
            dc = node.getNumberOfOnes();
            if (dc < this.arity) {
                c += dc;
            }
        }
        return c;
    }

    public MaxSet<Instance<Rank,Chain>> toRules() {
        //A max set ensures that there will be no redundant rules
        MaxSet<Instance<Rank,Chain>> res = new MaxSet<>(SRTools.order);

        Rank[] premisses;
        boolean complete;

        // For each node
        for (Node n : this.nodes) {
            for (int y = 1; y <= n.val.toInt(); y++) { //start from 1 because rules with conclusion 0 are trivial
                premisses = new Rank[this.arity];

                complete = true;

                // For each attribute
                for (int i = 0; i < this.arity; i++) {
                    // If the attribute belongs to the focal set represented by the node
                    if (n.get(i)) {
                        // Look at possible values of the attribute in ascending order
                        for (int j = 0; j < this.phis[i].length; j++) {
                            // Keep the first value that is map to something greater than y
                            if (phis[i][j].toInt() >= y) {
                                premisses[i] = this.domain[i].get(j);
                                break;
                            }
                        }
                        // We did not find a proper value for the attribute, then the rule cannot be made, break
                        if (premisses[i] == null) {
                            complete = false;
                            break;
                        }
                    } else {
                        premisses[i] = this.domain[i].getBottom();
                    }
                }
                if (complete) {
                    res.add(new LinearInstance(premisses, this.codomain.get(y), domain, codomain));
                }
            }
        }
        return res;

    }

}
