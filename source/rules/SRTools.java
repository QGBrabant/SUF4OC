/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rules;

import java.util.List;
import miscellaneous.Tuple;
import lattices.Chain;
import orders.Order;
import lattices.impls.Rank;
import ordinalclassification.OCTable;

/**
 *
 * @author qgbrabant
 */
public class SRTools {

    /**
     * The rule is satisfied iff each parameter is greater than or equal to the
     * value specified in the corresponding premiss.
     *
     * @param rule
     * @param params
     * @return
     */
    public static boolean satisfies(Instance<Rank, Chain> rule, Rank[] params) {
        assert params.length == rule.getArity() : params.length + " , " + rule.getArity();
        for (int i = 0; i < params.length; i++) {
            if (params[i].relation(rule.getFeature(i)) > 0) {
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
    public static boolean satisfies(Instance<Rank, Chain> rule, List<Rank> params) {
        assert params.size() == rule.getArity() : params.size() + " , " + rule.getArity();
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).relation(rule.getFeature(i)) > 0) {
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
    public static boolean satisfies(Instance<Rank, Chain> rule, Tuple<Rank> params) {
        assert params.size() == rule.getArity() : params.size() + " , " + rule.getArity();
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).relation(rule.getFeature(i)) > 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean areCompatible(OCTable<Rank, Chain> table, LinearInstance rule) {
        for (Instance<Rank, Chain> row : table) {
            if (satisfies(rule, row.getFeatures()) && rule.getLabel().toInt() > row.getLabel().toInt()) {
                return false;
            }
        }
        return true;
    }

    public static boolean areCompatible(Instance<Rank, Chain> r1, Instance<Rank, Chain> r2) {
        return ( (!satisfies(r1, r2.getFeatures())) || r1.getLabel().toInt() <= r2.getLabel().toInt())
                &&
               ( (!satisfies(r2, r1.getFeatures())) || r2.getLabel().toInt() <= r1.getLabel().toInt());
    }

    public static LinearInstance max(Instance<Rank, Chain> r1, Instance<Rank, Chain> r2) {
        Rank[] premisses = new Rank[r1.getDomain().length];
        Rank conclusion;
        for (int i = 0; i < r1.getDomain().length; i++) {
            if (r1.getDomain()[i].relation(r1.getFeature(i), r2.getFeature(i)) >= 0) {
                premisses[i] = r1.getFeature(i);
            } else {
                premisses[i] = r2.getFeature(i);
            }

        }
        if (r1.getCodomain().relation(r1.getLabel(), r2.getLabel()) <= 0) {
            conclusion = r1.getLabel();
        } else {
            conclusion = r2.getLabel();
        }

        return new LinearInstance(premisses, conclusion, r1.getDomain(), r1.getCodomain());
    }
    
    /**
     * Returns the number of criteria that are active in the rule.
     * @param rule
     * @return 
     */
    public static int LHSSize(Instance<Rank,Chain> rule){
        int res = 0;
        for(int i = 0; i < rule.getArity() ; i++){
            if(! rule.getFeature(i).equals(rule.getDomain()[i].getBottom())){
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
    public static Integer relation(Instance<Rank, Chain> r1, Instance<Rank, Chain> r2) {
        int r = 0;
        for (int i = 0; i < r1.getArity(); i++) {
            if (r1.getFeature(i).toInt() < r2.getFeature(i).toInt()) {
                if (r == 1) {
                    return null;
                } else {
                    r = -1;
                }
            } else if (r1.getFeature(i).toInt() > r2.getFeature(i).toInt()) {
                if (r == -1) {
                    return null;
                } else {
                    r = 1;
                }
            }
        }
        if (r1.getLabel().toInt() > r2.getLabel().toInt()) {
            if (r == 1) {
                return null;
            } else {
                r = -1;
            }
        } else if (r1.getLabel().toInt() < r2.getLabel().toInt()) {
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
            return SRTools.relation((Instance<Rank, Chain>) x, (Instance<Rank, Chain>) y);
        }

        @Override
        public Object randomValue() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

}
