/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import chains.Chain;
import tuples.BTupleImpl;
import tuples.TupleImpl;

/**
 *
 * @author qgbrabant
 */
public class SugenoUtility implements Serializable, Classifier {

    final private int arity;
    final private Chain[] domain;
    final private Chain codomain;
    private final Node bottom;
    private final Node top;

    private int[][] phis;
    private Set<Node> nodes;
    private List<Instance> added;

    private List<Instance> addedExamples;
    private List<Instance> rejectedExamples;

    public SugenoUtility(Chain[] domain, Chain codomain) {
        this.domain = Arrays.copyOf(domain, domain.length);
        this.codomain = codomain;
        this.arity = domain.length;
        this.nodes = new HashSet<>();

        int[] bin = new int[this.arity];
        Arrays.fill(bin, 0);
        this.bottom = new Node(bin, 0, this.codomain);
        this.nodes.add(this.bottom);

        Arrays.fill(bin, 1);
        this.top = new Node(bin, codomain.size()-1, this.codomain);
        this.nodes.add(this.top);

        this.phis = new int[arity][];
        for (int i = 0; i < this.domain.length; i++) {
            phis[i] = new int[this.domain[i].size()];
            Arrays.fill(phis[i], 0);
            phis[i][this.domain[i].size() - 1] = this.codomain.size()-1;
        }
        this.added = new ArrayList<>();
        this.addedExamples = new ArrayList<>();
        this.rejectedExamples = new ArrayList<>();
    }

    public boolean coverExample(Instance example, Collection<Instance> restrictions) {
        int[][] oldPhis = this.copyOfPhis();
        Set<Node> oldNodes = this.copyOfNodes();

        Instance rule
                = new Instance(example.copyOfFeatures(), example.getLabel(), example.getDomain(), example.getCodomain());

        this.updateWithRule(rule);

        //Test if it does contradict the dataset
        for (Instance row : restrictions) {
            if(example.getLabel() > row.getLabel()){
                if (this.apply(row.getFeatures()) > row.getLabel()) {
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

        public int val;
        public Chain domain;

        public Node(int[] bin, int val, Chain domain) {
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
            StringBuilder sb = new StringBuilder("{");
            boolean atLeastOne = false;
            for(int i = 0; i < this.size() ; i ++){
                if(this.get(i)> 0){
                    if(atLeastOne){
                        sb.append(",");
                    }
                    sb.append(i);
                    atLeastOne = true;
                }
            }
            sb.append("}->");
            sb.append(this.val);
            return sb.toString();
        }
    }

    private void fillMin(Node n) {
        n.val = 0;
        for (Node n2 : this.nodes) {
            Integer r = n.relation(n2);
            if (r != null) {
                if (r == 0) {
                    n.val = n2.val;
                    break;
                }
                if (r < 0) {
                    n.val = Math.max(n.val, n2.val);
                }
            }
        }
    }

    private void increaseNode(Node cn, int x) {
        for (Node cn2 : this.nodes) {
            Integer r = cn2.relation(cn);
            if (r != null && r <= 0) {
                cn2.val = Math.max(cn2.val, x);
            }
        }
    }

    private int[][] copyOfPhis() {
        int[][] oldPhis = this.phis;
        this.phis = new int[this.arity][];
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
    
    private void updateWithRule2(Instance rule) {
        int[] bin = new int[this.domain.length];
        for (int i = 0; i < this.arity; i++) {
            if (rule.getFeature(i) > 0) {
                bin[i] = 1;
                
            } else {
                bin[i] = 0;
            }
            for (int j = 0; j < this.phis[i].length; j++) {
                    //System.out.println(example);
                    //assert example.getInput().get(i) != null : example;
                    if (j >= rule.getFeature(i)) {
                        this.phis[i][j] = Math.max(this.phis[i][j], rule.getLabel());
                    }
                }
        }
        /*Node nodeA = new Node(bin, 0, this.codomain);
        fillMin(nodeA);
        this.nodes.add(nodeA);

        this.increaseNode(nodeA, rule.getLabel());*/
    }

    private void updateWithRule(Instance rule) {
        int[] bin = new int[this.domain.length];
        for (int i = 0; i < this.arity; i++) {
            if (rule.getFeature(i) > 0) {
                bin[i] = 1;
                for (int j = 0; j < this.phis[i].length; j++) {
                    //System.out.println(example);
                    //assert example.getInput().get(i) != null : example;
                    if (j >= rule.getFeature(i)) {
                        this.phis[i][j] = Math.max(this.phis[i][j], rule.getLabel());
                    }
                }
            } else {
                bin[i] = 0;
            }
        }
        Node nodeA = new Node(bin, 0, this.codomain);
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
     * @param R
     * @return
     */
    public boolean coverRule(Instance r, RuleSet R) {
        if(r.getLabel() == 0){
            return true;
        }

        int[][] oldPhis = this.copyOfPhis();

        Set<Node> oldNodes = this.copyOfNodes();

        this.updateWithRule(r);

        RuleSet R3 = new RuleSet(R);
        RuleSet R2 =this.toRules();
        R2.removeAll(R);
        
        boolean ok = true;
        
        for(Instance rule : R2){
            if(R3.add(rule)){
                ok = false;
            }
        }
        if (!ok) {
            this.phis = oldPhis;
            this.nodes = oldNodes;
            this.rejectedExamples.add(r);
            return false;
        }
        this.addedExamples.add(r);
        return true;
    }

    public int apply(TupleImpl input) {
        //Apply the local utility functions on the input tuple
        int[] tab = new int[arity];
        for (int i = 0; i < arity; i++) {
            tab[i] = this.phis[i][input.get(i)];
        }
        return getMinOutput(tab);

    }
    
    public int apply(int[] input) {
        //Apply the local utility functions on the input tuple
        int[] tab = new int[arity];
        for (int i = 0; i < arity; i++) {
            tab[i] = this.phis[i][input[i]];
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

                    if (r1 > 0 && (list.get(i).val >= list.get(j).val)) {
                        list.remove(j);
                        j--;
                    } else if (r1 < 0 && (list.get(i).val <= list.get(j).val)) {
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
    private int getMinOutput(int[] input) {
        int result = 0;
        int threshold;
        for (Node cn : this.nodes) {
            threshold = cn.val;
            for (int i = 0; i < input.length && (threshold> result) ; i++) {
                if (cn.get(i) == 1) {
                    threshold = Math.min(threshold, input[i]);
                }
            }
            result = Math.max(result, threshold);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb2;
        StringBuilder sb = new StringBuilder("<<\nFocal sets:\n");
        for(Node node : this.nodes){
            sb.append(node).append(", ");
        }
        sb.append("\nQualitative normalization functions:\n________");
        for(int i = 0 ; i < this.codomain.size(); i++){
            sb.append(this.codomain.getName(i));
            sb.append("_______________");
        }
        sb.append("\n");
        for(int i = 0 ; i < this.domain.length ; i ++){
            //sb.append(Arrays.toString(this.phis[i])).append("\n");
            sb.append("phi").append(i).append(":\t");
            int m = this.phis[i][0];
            sb2 = new StringBuilder(this.domain[i].getName(0));
            boolean dash = false;
            for(int j = 1; j < this.phis[i].length ; j ++){
                if(this.phis[i][j] > m){
                    if(dash){
                        sb2.append("-").append(this.domain[i].getName(j-1));
                    }
                    m++;
                    if(sb2.length()< 8){
                        sb2.append("\t");
                    }
                    sb2.append("\t");
                    sb.append(sb2);
                    sb2 = new StringBuilder();
                    while(m != this.phis[i][j]){
                        m ++ ;
                        sb.append("X\t\t");
                    }
                    sb2.append(this.domain[i].getName(j));
                    dash = false;
                }else{
                    dash = true;
                }
            }
            if(dash){
                sb2.append("-").append(this.domain[i].getName(this.domain[i].size()-1));
            }
            sb.append(sb2);
            sb.append("\n");
        }
        sb.append(">>");
        return sb.toString();
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

    public List<Instance> getAddedExamples() {
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

    public RuleSet toRules() {
        //A max set ensures that there will be no redundant rules
        RuleSet res = new RuleSet(this.domain,this.codomain);

        int[] premisses;
        boolean complete;

        // For each node
        for (Node n : this.nodes) {
            for (int y = 1; y <= n.val; y++) { //start from 1 because rules with conclusion 0 are trivial
                premisses = new int[this.arity];
                Arrays.fill(premisses,-1);

                complete = true;

                // For each attribute
                for (int i = 0; i < this.arity; i++) {
                    // If the attribute belongs to the focal set represented by the node
                    if (n.get(i) == 1) {
                        // Look at possible values of the attribute in ascending order
                        for (int j = 0; j < this.phis[i].length; j++) {
                            // Keep the first value that is map to something greater than y
                            if (phis[i][j] >= y) {
                                premisses[i] = j;
                                break;
                            }
                        }
                        // We did not find a proper value for the attribute, then the rule cannot be made, break
                        if (premisses[i] == -1) {
                            complete = false;
                            break;
                        }
                    } else {
                        premisses[i] = 0;
                    }
                }
                if (complete) {
                    res.add(new Instance(premisses, y, domain, codomain));
                }
            }
        }
        return res;

    }

}
