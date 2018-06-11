/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rules;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import miscellaneous.TupleImpl;
import lattices.Chain;
import lattices.impls.Rank;
import orders.OTools;

/**
 *
 * @author qgbrabant
 */
public class LinearInstance implements Instance<Rank, Chain>, Serializable {
    private final Chain[] domain;
    private final Chain codomain;
    private final Rank[] premisses;
    private final TupleImpl<Rank> features; // This contains the same thing as premisses. Just a quickfix for a bad optimization.
    private final Rank conclusion;


    public LinearInstance(Rank[] premisses, Rank conclusion, Chain[] domain, Chain codomain) {
        this.premisses = (Rank[]) Arrays.<Rank>copyOf(premisses, premisses.length);
        this.conclusion = conclusion;
        this.domain = domain;
        this.codomain = codomain;
        this.features = new TupleImpl<>(premisses);
    }

    @Override
    public int getArity() {
        return this.premisses.length;
    }

    @Override
    public Rank getFeature(int i) {
        return this.premisses[i];
    }

    @Override
    public Rank getLabel() {
        return this.conclusion;
    }
    
    

    @Override
    public Rank[] copyOfFeatures() {
        return Arrays.copyOf(this.premisses,this.premisses.length);
    }

    @Override
    public Chain[] getDomain() {
        return this.domain;
    }
    
    @Override
    public Chain getCodomain() {
        return this.codomain;
    }
    
    public int getLeftHandSize(){
        int res = 0;
        for(Rank r : premisses){
            if(r.toInt() != 0){
                res ++;
            }
        }
        return res;
    }
    
    @Override
    public String toString(){
        String res = "";
        for(Rank mbt : this.premisses){
            res += mbt+", ";
        }
        return res+" -> "+conclusion;
    }
    
    @Override
    public boolean equals(Object o) {
        LinearInstance ltr = (LinearInstance) o;
        for (int i = 0; i < this.getArity(); i++) {
            if (!this.premisses[i].equals(ltr.premisses[i])) {
                return false;
            }
        }
        if (!this.conclusion.equals( ltr.conclusion)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Arrays.deepHashCode(this.premisses);
        hash = 79 * hash + Objects.hashCode(this.conclusion);
        return hash;
    }

    @Override
    public TupleImpl<Rank> getFeatures() {
        return this.features;
    }

    @Override
    public Instance<Rank, Chain> copy() {
        return new LinearInstance(premisses, conclusion, domain, codomain);
    }
    
    public static class InstanceMaker {
            private final Chain[] domain;
            private final Chain codomain;
            public InstanceMaker(Chain[] domain, Chain codomain){
                this.domain = domain;
                this.codomain= codomain;
            }
            
            public LinearInstance makeInstance(int... arg) {
                assert arg.length == domain.length+1;
                Rank[] features = new Rank[domain.length];
                for(int i = 0; i < domain.length ; i ++){
                    features[i] = domain[i].get(arg[i]);
                }
                return new LinearInstance(features,codomain.get(arg[arg.length-1]),domain,codomain);
            }
        }
    
    public Integer relation(Rank[] x){
        return OTools.componentWiseOrdering(this.premisses,x);
    }
}
