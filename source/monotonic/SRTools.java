/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

import java.util.List;
import tuples.Tuple;
import chains.Chain;
import orders.Order;
import monotonic.MonotonicTable;

/**
 *
 * @author qgbrabant
 */
public abstract class SRTools {

    /**
     * The rule is satisfied iff each parameter is greater than or equal to the
     * value specified in the corresponding premiss.
     *
     * @param rule
     * @param params
     * @return
     */
    public static boolean satisfies(Instance rule, int[] params) {
        assert params.length == rule.getArity() : params.length + " , " + rule.getArity();
        for (int i = 0; i < params.length; i++) {
            if (params[i] < rule.getFeature(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The rule is satisfied iff each parameter is greater than or equal to the
     * value specified in the corresponding premiss.
     *
     * @param rule
     * @param params
     * @return
     */
    public static boolean satisfies(Instance rule, Tuple params) {
        assert params.size() == rule.getArity() : params.size() + " , " + rule.getArity();
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) < rule.getFeature(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean areCompatible(MonotonicTable table, Instance rule) {
        for (Instance row : table) {
            if (satisfies(rule, row.getFeatures()) && rule.getLabel() > row.getLabel()) {
                return false;
            }
        }
        return true;
    }

    public static boolean areCompatible(Instance r1, Instance r2) {
        return ( (!satisfies(r1, r2.getFeatures())) || r1.getLabel() <= r2.getLabel())
                &&
               ( (!satisfies(r2, r1.getFeatures())) || r2.getLabel() <= r1.getLabel());
    }

    public static Instance max(Instance r1, Instance r2) {
        int[] premisses = new int[r1.getDomain().length];
        int conclusion;
        for (int i = 0; i < r1.getDomain().length; i++) {
            if (r1.getFeature(i) <= r2.getFeature(i)) {
                premisses[i] = r1.getFeature(i);
            } else {
                premisses[i] = r2.getFeature(i);
            }

        }
        if (r1.getLabel() >= r2.getLabel()) {
            conclusion = r1.getLabel();
        } else {
            conclusion = r2.getLabel();
        }

        return new Instance(premisses, conclusion, r1.getDomain(), r1.getCodomain());
    }
    
    /**
     * Returns the number of criteria that are active in the rule.
     * @param rule
     * @return 
     */
    public static int LHSSize(Instance rule){
        int res = 0;
        for(int i = 0; i < rule.getArity() ; i++){
            if(rule.getFeature(i) > 0){
                res ++;
            }
        }
        return res;
    }
    

    /**
     * A rule is "bigger" than another if the premisses are smaller (pair-wisely
     * speaking) and the conclusion is bigger. 1: param is bigger -1: param is
     * smaller 0: param is equal null: param is not comparable to this
     *
     * @param r1
     * @param r2
     * @return
     */
    public static Integer relation(Instance r1, Instance r2) {
        int r = 0;
        for (int i = 0; i < r1.getArity(); i++) {
            if (r1.getFeature(i) < r2.getFeature(i)) {
                if (r == 1) {
                    return null;
                } else {
                    r = -1;
                }
            } else if (r1.getFeature(i) > r2.getFeature(i)) {
                if (r == -1) {
                    return null;
                } else {
                    r = 1;
                }
            }
        }
        if (r1.getLabel() > r2.getLabel()) {
            if (r == 1) {
                return null;
            } else {
                r = -1;
            }
        } else if (r1.getLabel() < r2.getLabel()) {
            if (r == -1) {
                return null;
            } else {
                r = 1;
            }
        }
        return r;
    }
    
    
    
    
    /**
     * The default order over selection rules.
     */
    public static Order order = new Order() {
        @Override
        public Integer relation(Object x, Object y) {
            return SRTools.relation((Instance) x, (Instance) y);
        }

        @Override
        public Object randomValue() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

}
