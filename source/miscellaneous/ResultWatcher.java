/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miscellaneous;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import monotonic.Instance;
import monotonic.MaxSUF;
import monotonic.RuleSet;
import monotonic.SRTools;
import monotonic.SugenoUtility;

/**
 *
 * @author qgbrabant
 */
public abstract class ResultWatcher {

    public static Double MER(Result res) {
        return res.getMeanMER();
    }

    public static Double MAE(Result res) {
        return res.getMeanMAE();
    }
    
    public static Double stdMER(Result res) {
        return res.getStdMER();
    }

    public static Double stdMAE(Result res) {
        return res.getStdMAE();
    }

    public static Double rho(Result res) {
        return res.rho;
    }

    public static Double nbSUF(Result res) {
        if (res.coverages.isEmpty()) {
            return -1.;
        }
        return ((Result<MaxSUF>) res).coverages
                .stream()
                .map(x -> (double) x.size())
                .reduce(0., (x, y) -> x + y)
                / res.coverages.size();
    }

    public static Double nbRules(Result res) {
        if (res.coverages.isEmpty()) {
            return -1.;
        }
        if (res.coverages.get(0) instanceof MaxSUF) {
            return ((Result<MaxSUF>) res).coverages
                    .stream()
                    .flatMap(Collection::stream)
                    .map(su -> (double) su.toRules().size())
                    .reduce(0., (x, y) -> x + y)
                    / res.coverages.size();
        } else if (res.coverages.get(0) instanceof RuleSet) {
            return ((Result<RuleSet>) res).coverages
                    .stream()
                    .map(R -> (double) R.size())
                    .reduce(0., (x, y) -> x + y)
                    / res.coverages.size();
        }
        return -1.;
    }

    public static Double avgRuleLength(Result res) {
        if (res.coverages.isEmpty()) {
            return -1.;
        }
        double avg = 0.;
        int div = 0;
        if (res.coverages.get(0) instanceof MaxSUF) {

            Set<Instance> rules;
            for (Collection<SugenoUtility> coverage : ((Result<MaxSUF>) res).coverages) {
                for (SugenoUtility su : coverage) {
                    rules = su.toRules();
                    div += rules.size();
                    for (Instance r : rules) {
                        avg += SRTools.LHSSize(r);
                    }
                }
            }
        } else if (res.coverages.get(0) instanceof RuleSet) {
            for (RuleSet coverage : ((Result<RuleSet>) res).coverages) {
                for (Instance r : coverage) {
                    avg += r.getLeftHandSize();
                    div++;
                }
            }
        }
        avg /= div;
        return avg;
    }

    public static List<Double> ruleLenghtDistrib(Result res) {
        List<Double> list = new ArrayList<>();
        int div = 0;
        Set<Instance> rules;
        if (res.coverages.get(0) instanceof MaxSUF) {
            for (Collection<SugenoUtility> coverage : ((Result<MaxSUF>) res).coverages) {
                for (SugenoUtility su : coverage) {
                    rules = su.toRules();
                    div += rules.size();
                    for (Instance r : rules) {
                        int i = SRTools.LHSSize(r);
                        for(int j = list.size(); j<= i ; j++){
                                list.add(0.);
                        }
                        list.set(i, list.get(i) + 1);

                    }
                }
            }
        } else if (res.coverages.get(0) instanceof RuleSet) {
            for (RuleSet coverage : ((Result<RuleSet>) res).coverages) {
                rules = coverage;
                div += rules.size();
                for (Instance r : rules) {
                    int i = SRTools.LHSSize(r);

                    for(int j = list.size(); j<= i ; j++){
                        list.add(0.);
                    }
                    list.set(i, list.get(i) + 1);
                }
            }
        }
        list.add(0.);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i) / div);
        }
        return list;
    }
}
