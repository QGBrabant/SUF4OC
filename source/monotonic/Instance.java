/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

import java.util.Arrays;
import tuples.TupleImpl;
import chains.Chain;
import orders.OrderTools;
import orders.PartiallyOrderable;

/**
 *
 * @author qgbrabant
 */
public class Instance implements PartiallyOrderable {

    private final Chain[] domain;
    private final Chain codomain;
    private final int[] premisses;
    private final TupleImpl features; // This contains the same thing as premisses. Just a quickfix for a bad optimization.
    private final int conclusion;
    private Instance inverse ;

    public Instance(int[] premisses, int conclusion, Chain[] domain, Chain codomain) {
        this.premisses = (int[]) Arrays.copyOf(premisses, premisses.length);
        this.conclusion = conclusion;
        this.domain = domain;
        this.codomain = codomain;
        this.features = new TupleImpl(premisses);
    }

    public int getArity() {
        return this.premisses.length;
    }

    public int getFeature(int i) {
        return this.premisses[i];
    }

    public int getLabel() {
        return this.conclusion;
    }

    public int[] copyOfFeatures() {
        return Arrays.copyOf(this.premisses, this.premisses.length);
    }

    public Chain[] getDomain() {
        return this.domain;
    }

    public Chain getCodomain() {
        return this.codomain;
    }

    public int getLeftHandSize() {
        int res = 0;
        for (int r : premisses) {
            if (r != 0) {
                res++;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        String res = "(";
        for (int mbt : this.premisses) {
            res += mbt + ",";
        }
        return res + ")-> " + conclusion;
    }

    @Override
    public boolean equals(Object o) {
        Instance ltr = (Instance) o;
        for (int i = 0; i < this.getArity(); i++) {
            if (this.premisses[i] != ltr.premisses[i]) {
                return false;
            }
        }
        if (this.conclusion != ltr.conclusion) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Arrays.hashCode(this.premisses);
        hash = 79 * hash + this.conclusion;
        return hash;
    }

    public TupleImpl getFeatures() {
        return this.features;
    }

    public Instance copy() {
        return new Instance(premisses, conclusion, domain, codomain);
    }

    @Override
    public Integer relation(Object o) {
        Instance other = (Instance) o;
        Integer rel = other.relation(this.premisses);
        return OrderTools.productOfRelations(rel, OrderTools.relation(this.conclusion, other.conclusion));
    }

    public static class InstanceMaker {

        private final Chain[] domain;
        private final Chain codomain;

        public InstanceMaker(Chain[] domain, Chain codomain) {
            this.domain = domain;
            this.codomain = codomain;
        }

        public Instance makeInstance(int... arg) {
            assert arg.length == domain.length + 1;
            int[] features = new int[domain.length];
            for (int i = 0; i < domain.length; i++) {
                features[i] = arg[i];
            }
            return new Instance(features, arg[arg.length - 1], domain, codomain);
        }
    }

    public Integer relation(int[] x) {
        return OrderTools.componentWiseRelation(this.premisses, x);
    }
    
    public Instance inverse(){
        if(inverse == null){
            Chain[] newdomain = new Chain[this.domain.length];
            for(int i = 0; i < newdomain.length; i ++){
                newdomain[i] = this.domain[i].inverse();
            }
            Chain newcodomain = this.codomain.inverse();
            int[] newpremisses = new int[this.premisses.length];
            for(int i = 0; i < newdomain.length; i ++){
                newpremisses[i] = this.domain[i].inv(this.premisses[i]);
            }
            int newconclusion = this.codomain.inv(this.conclusion);
            this.inverse = new Instance(newpremisses,newconclusion,newdomain,newcodomain);
            this.inverse.inverse = this;
        }
        return this.inverse;
    }

    String niceString(boolean rejection) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < this.domain.length; i++) {
            if (this.features.get(i) > 0) {
                res.append("x");
                res.append(i);
                res.append(this.domain[i].getOrderSymbol());
                res.append(this.domain[i].getName(this.getFeature(i)));
                if(("x <= "+this.domain[i].getName(this.getFeature(i))).length() < 7){
                    res.append("\t");
                }
                res.append("\t");
            }
        }
        res.append("==>\t");
        res.append("y");
        if (rejection) {
            res.append(" <= ");
        } else {
            res.append(" >= ");
        }
        res.append(this.codomain.getName(this.conclusion));
        return res.toString();
    }
}
