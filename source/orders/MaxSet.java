/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orders;

import java.util.HashSet;
import java.util.Iterator;
import monotonic.Instance;
import orders.Order;

/**
 *
 * @author qgbrabant
 */
public class MaxSet<T> extends HashSet<T>{
    public final Order<T> order;

    public MaxSet(Order<T> p) {
        super();
        this.order = p;
    }
    
    public MaxSet(MaxSet<T> set){
        super();
        for(T x : set){
            super.add(x);
        }
        this.order = set.order;        
    }
    
    @Override
    public boolean add(T x){
        Iterator<T> i = this.iterator();
        while (i.hasNext()) {
            T y = i.next();
            Integer r = order.relation(x, y);
            if (r != null) {
                if (r >= 0) {
                    return false;
                }
                if (r == -1) {
                    i.remove();
                }
            }
        }
        super.add(x);
        return true;
    }

}
