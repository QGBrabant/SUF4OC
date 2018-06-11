/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordinalclassification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import orders.Poset;
import rules.Instance;

/**
 * @param <T> the kind of 
 * @param <O>
 */
public class OCTable<T, O extends Poset<T>> implements Set<Instance<T,O>> {

    private final O[] domain;
    private final O codomain;
    private Set<InstanceWrap> innerSet;
    
    
    private class InstanceWrap {
        Instance<T,O> instance;
        
        public InstanceWrap(Instance inst){
            instance = inst;
        }
        
        @Override
        public boolean equals(Object o){
            return ((InstanceWrap)o).instance.getFeatures().equals(this.instance.getFeatures()) ;
        }

        @Override
        public int hashCode() {
            int hash = 8;
            hash = 89 * hash + Arrays.deepHashCode(this.instance.copyOfFeatures());
            return hash;
        }
    }

    public OCTable(O[] domain, O codomain) {
        super();
        this.innerSet = new HashSet<>();
        this.domain = domain;
        this.codomain = codomain;
    }
    
    public OCTable(InstanceList<T,O> dataset){
        super();
        this.innerSet = new HashSet<>();
        this.domain = dataset.getDomain();
        this.codomain = dataset.getCodomain();
        for(Instance<T,O> instance : dataset){
            this.add(instance);
        }
    }


    public O[] getDomain() {
        return domain;
    }

    public O getCodomain() {
        return codomain;
    }
    
    @Override
    public boolean add(Instance<T,O> inst){
        return this.innerSet.add(new InstanceWrap(inst.copy()));
    }

    
    public List<OCTable<T,O>> getPieces(int n){
        List<OCTable<T,O>> res = new ArrayList<>();
        int i = 0;
        List<Instance<T,O>> list = new ArrayList<>(this);
        Collections.shuffle(list);
        for(Instance<T,O> r : list){
            if( (i % (this.size()/n)) == 0){
                res.add(new OCTable<>(this.getDomain(),this.getCodomain()));
            }
            res.get(res.size()-1).add(r);
            i++;
        }
        
        return res;
    }

    @Override
    public int size() {
        return this.innerSet.size();
    }

    @Override
    public boolean isEmpty() {
        return this.innerSet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if(o instanceof Instance){
            return this.innerSet.contains(new InstanceWrap((Instance)o));
        }
        return false;
    }

    @Override
    public Iterator<Instance<T, O>> iterator() {
        return new Iterator<Instance<T, O>>() {
            Iterator<InstanceWrap> it = innerSet.iterator();
            
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Instance<T, O> next() {
                return it.next().instance;
            }
        };
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean remove(Object o) {
        if(o instanceof Instance){
            return this.innerSet.remove(new InstanceWrap((Instance)o));
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object x : c){
            if(!this.contains(x)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Instance<T, O>> c) {
        boolean res = false;
        for(Instance<T,O> inst : c){
            res |= this.add(inst);
        }
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean res = false;
        for(Object o : c){
            res |= this.remove(o);
        }
        return res;
    }

    @Override
    public void clear() {
        this.innerSet.clear();
    }
}
