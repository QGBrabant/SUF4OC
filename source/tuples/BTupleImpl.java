/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tuples;

import static java.lang.Math.random;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author qgbrabant
 */
public class BTupleImpl extends TupleImpl{
    public BTupleImpl(int[] bin){
        super(bin);
    }
    
    public int[] getBin(){
        int[] t = new int[this.size()];
        for(int i = 0; i < this.size(); i++){
            t[i] = this.get(i);
        }
        return t;
    }
    
    public Integer lexicographicRelation(BTupleImpl x){
        for(int i = 0; i < this.size() ; i ++){
            if(this.get(i) == 1 && x.get(i)==0){
                return -1;
            }else if (this.get(i)==0 && x.get(i)==1){
                return 1;
            }
        }
        return 0;
    }
    
    public static BTupleImpl randomBTuple(int d){
        int[] bins = new int[d];
        for(int i = 0; i < d ; i++){
            bins[i] = random() < 0.5 ? 0 : 1 ;
        }
        return new BTupleImpl(bins);
    }
    
    public Object join(Object o) {
        BTupleImpl x = (BTupleImpl) o;
        assert x.size() == this.size();

        int[] bin = new int[x.size()];
        for (int i = 0; i < bin.length; i++) {
            bin[i] = Math.max(x.get(i), this.get(i));
        }
        return new BTupleImpl(bin);
    }
    
    public Object meet(Object o) {
        BTupleImpl x = (BTupleImpl) o;
        assert x.size() == this.size();
        
        int[] bin = new int[x.size()];
        for (int i = 0; i < bin.length; i++) {
            bin[i] = Math.min(x.get(i), this.get(i));
        }
        return new BTupleImpl(bin);
    }
    
    public static <T extends BTupleImpl> boolean updateLowerBounds(Set<T> lbs, T e){
        Iterator<T> it = lbs.iterator();
        BTupleImpl bt;
        Integer r;
        while(it.hasNext()){
            bt = it.next();
            r = e.relation(bt);
            if(r != null){
                if(r == 1){
                    it.remove();
                }else if ( r <= 0 ) {
                    return false;
                }
            }
        }
        lbs.add(e);
        return true;
    }
    
    public static <T extends BTupleImpl> boolean updateUpperBounds(Set<T> ubs, T e){
        Iterator<T> it = ubs.iterator();
        BTupleImpl bt;
        Integer r;
        while(it.hasNext()){
            bt = (BTupleImpl) it.next();
            r = e.relation(bt);
            if(r != null){
                if(r == -1){
                    it.remove();
                }else if ( r >= 0 ) {
                    return false;
                }
            }
        }
        ubs.add(e);
        return true;
    }

    public Integer relation(Object o) {
        if(!(o instanceof BTupleImpl)){
            assert false;
            return null;
        }
        BTupleImpl x = (BTupleImpl) o;
        Integer rel = 0;
        for(int i = 0; i < this.size() ; i ++){
            if(this.get(i) == 1 && x.get(i) == 0){
                if(rel == 1){
                    return null;
                }else{
                    rel = -1;
                }
            }else if (this.get(i) == 0 && x.get(i) == 1){
                if(rel == -1){
                    return null;
                }else{
                    rel = 1;
                }
            }
        }
        return rel;
    }

    public int getNumberOfOnes() {
        int res = 0;
        for(int b : this.array){
            if(b==1) res ++;
        }
        return res;
    }

    public int getNumberOfZeros() {
        return this.array.length - this.getNumberOfOnes();
    }
}
