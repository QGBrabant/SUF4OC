/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordinalclassification;

import java.util.Arrays;
import miscellaneous.TupleImpl;
import lattices.Chain;
import lattices.impls.Rank;
import rules.Instance;
import rules.LinearInstance;

/**
 *
 * @author qgbrabant
 */
public abstract class OCTools {

    /**
     * Generate a random monotonic dataset with m rows
     *
     * @param m
     * @param domain
     * @param codomain
     * @return
     */
    public static OCTable randomMonotonicDataset(int m, Chain[] domain, Chain codomain) {
        OCTable<Rank, Chain> dataset = new OCTable(domain, codomain);

        Rank[] premisses = new Rank[domain.length];
        for (int i = 0; i < domain.length; i++) {
            premisses[i] = domain[i].getTop();
        }
        dataset.add(new LinearInstance(premisses, codomain.getTop(), domain, codomain));
        for (int i = 0; i < domain.length; i++) {
            premisses[i] = domain[i].getBottom();
        }
        dataset.add(new LinearInstance(premisses, codomain.getBottom(), domain, codomain));
        //System.out.println(dataset);
        int j = 0;
        Integer r;
        boolean add;
        while (j < m) {
            premisses = new Rank[domain.length];
            for (int i = 0; i < domain.length; i++) {
                premisses[i] = domain[i].randomValue();
            }

            add = true;
            //Initialize min and max
            Rank min = codomain.getBottom();
            Rank max = codomain.getTop();
            //If the tuple does not satisfy the premisses of one rule, deacrease the max
            for (Instance<Rank, Chain> row : dataset) {
                //compute the relation between the input and the candidate
                r = 0;
                for (int i = 0; i < premisses.length; i++) {
                    if (row.getFeature(i).toInt() < premisses[i].toInt()) {
                        if (r == 1) {
                            r = null;
                            break;
                        } else {
                            r = -1;
                        }
                    } else if (row.getFeature(i).toInt() > premisses[i].toInt()) {
                        if (r == -1) {
                            r = null;
                            break;
                        } else {
                            r = 1;
                        }
                    }
                }
                if (r != null) {
                    if (r == 0) {
                        add = false;
                        break;
                    }
                    if (r > 0) {
                        //System.out.println(input+" & "+new TupleImpl<ManicheanBTuple>(premisses));
                        max = codomain.meet(max, row.getLabel());
                    }
                    if (r < 0) {
                        min = codomain.join(min, row.getLabel());
                    }
                }
            }
            if (add) {
                dataset.add(new LinearInstance(premisses, codomain.randomValueBetween(min, max), domain, codomain));
                j++;
            }
        }
        //System.out.println(dataset);
        return dataset;
    }

    
}
