/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordinalclassification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import orders.OTools;
import orders.Poset;
import rules.Instance;

/**
 *
 * @author qgbrabant
 * @param <T>
 * @param <O>
 */
public class InstanceList<T, O extends Poset<T>> extends ArrayList<Instance<T,O>> implements Serializable {

    private final O[] domain;
    private final O codomain;
    private final Map<T, T> classesFilter;
    private String name = "unnamed";
    private boolean hasID = false;


    public InstanceList(O[] domain, O codomain) {
        this.domain = domain;
        this.codomain = codomain;
        this.classesFilter = new HashMap<>();
    }
    
    public InstanceList(Collection<O> domain, O codomain) {
        this.domain = (O[]) domain.toArray();
        this.codomain = codomain;
        this.classesFilter = new HashMap<>();
    }

    public InstanceList(InstanceList<T, O> d) {
        this(d.domain, d.codomain);
        this.classesFilter.putAll(d.classesFilter);
        this.addAll(d);
    }

    public InstanceList(InstanceList<T, O>... datasets) {
        this(datasets[0].domain, datasets[0].codomain);
        this.classesFilter.putAll(datasets[0].classesFilter);
        for (InstanceList<T, O> d : datasets) {
            this.addAll(d);
        }
    }

    public InstanceList(List<InstanceList<T, O>> datasets) {
        this(datasets.get(0).domain, datasets.get(0).codomain);
        this.classesFilter.putAll(datasets.get(0).classesFilter);
        for (InstanceList<T, O> d : datasets) {
            this.addAll(d);
        }
    }

    public O[] getDomain() {
        return domain;
    }

    public O getCodomain() {
        return codomain;
    }

    public List<InstanceList<T, O>> getPieces(int n) {
        List<InstanceList<T, O>> res = new ArrayList<>();
        int i = 0;
        List<Instance<T,O>> list = new ArrayList<>(this);

        for (int j = 0; j < n; j++) {
            res.add(new InstanceList<>(this.getDomain(), this.getCodomain()));
            res.get(res.size() - 1).classesFilter.putAll(classesFilter);
        }

        Collections.shuffle(list);
        for (Instance<T,O> r : list) {
            res.get(i % n).add(r.copy());
            i++;
        }

        return res;
    }

    public void putInClassFilter(T key, T value) {
        this.classesFilter.put(key, value);
    }

    public T getClass(T x) {
        if (this.classesFilter.size() > 0) {
            return this.classesFilter.get(x);
        } else {
            return x;
        }
    }

    public InstanceList<T, O> getEmptyShell() {
        InstanceList<T, O> res = new InstanceList<>(this.domain, this.codomain);
        res.classesFilter.putAll(this.classesFilter);
        return res;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double comparabilityDegree() {
        List<Instance<T, O>> list = new ArrayList<>(this);
        double res = 0.;
        Integer rel;
        int k;
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                rel = 0;
                k = -1;
                while (rel != null && ++k < this.domain.length) {
                    rel = OTools.cartesianProductOfRelations(rel, this.domain[k].relation(list.get(i).getFeature(k), list.get(j).getFeature(k)));
                }
                if (rel != null) {
                    res++;
                }
            }
        }
        return (res * 2 + this.size()) / (this.size() * this.size());
    }

    public boolean hasID() {
        return hasID;
    }

    public void setHasID(boolean hasID) {
        this.hasID = hasID;
    }
    
    public String datasetInformation(){
        return "Dataset: "+this.getName()+"\n"+
        this.getDomain().length+" features\n"+
        this.getCodomain().size()+" classes\n"+
        this.size()+" rows";
    }

}
