/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import chains.Chain;
import orders.OrderTools;
import orders.Order;
import orders.Poset;
import orders.MaxSet;

/**
 *
 * @author qgbrabant
 * @param <int>
 * @param <Chain>
 */
public class InstanceList extends ArrayList<Instance> implements Serializable {

    private final Chain[] domain;
    private final Chain codomain;
    private String name = "unnamed";
    private boolean hasID = false;


    public InstanceList(Chain[] domain, Chain codomain) {
        this.domain = domain;
        this.codomain = codomain;
    }
    
    public InstanceList(Collection<Chain> domain, Chain codomain) {
        this.domain = (Chain[]) domain.toArray();
        this.codomain = codomain;
    }

    public InstanceList(InstanceList d) {
        this(d.domain, d.codomain);
        this.addAll(d);
    }

    public InstanceList(InstanceList... datasets) {
        this(datasets[0].domain, datasets[0].codomain);
        for (InstanceList d : datasets) {
            this.addAll(d);
        }
    }

    public InstanceList(List<InstanceList> datasets) {
        this(datasets.get(0).domain, datasets.get(0).codomain);
        for (InstanceList d : datasets) {
            this.addAll(d);
        }
    }

    public Chain[] getDomain() {
        return domain;
    }

    public Chain getCodomain() {
        return codomain;
    }

    public List<InstanceList> getPieces(int n) {
        List<InstanceList> res = new ArrayList<>();
        int i = 0;
        List<Instance> list = new ArrayList<>(this);

        for (int j = 0; j < n; j++) {
            res.add(new InstanceList(this.getDomain(), this.getCodomain()));
        }

        Collections.shuffle(list);
        for (Instance r : list) {
            res.get(i % n).add(r.copy());
            i++;
        }

        return res;
    }
    


    public InstanceList getEmptyShell() {
        InstanceList res = new InstanceList(this.domain, this.codomain);
        return res;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double comparabilityDegree() {
        List<Instance> list = new ArrayList<>(this);
        double res = 0.;
        Integer rel;
        int k;
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                rel = 0;
                k = -1;
                while (rel != null && ++k < this.domain.length) {
                    rel = OrderTools.productOfRelations(rel, OrderTools.relation(list.get(i).getFeature(k), list.get(j).getFeature(k)));
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
    
    public InstanceList reduction(){
        MaxSet<Instance> ms = new MaxSet<>(new Order.VanillaImpl<>());
        ms.addAll(this);
        InstanceList res = this.getEmptyShell();
        res.addAll(ms);
        return res;
    }

}
