/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that contains useful functions that make use of the interfaces defined in the package
 * @author qgbrabant
 */
public abstract class OrderTools {
    
    public static Integer relation(int i, int j){
        if(i==j) return 0;
        if(i<j) return 1;
        return -1;
    }
    
    /**
     * Returns a list that respect the partial order given as a parameter, which contains every elements of the set.
     * @param <T>
     * @param order
     * @return a list of elements
     */
    public static <T> List<T> totalOrdering(Order<T> order, Set<T> set){
        List<T> list = new ArrayList<>();
        Integer r;
        for(T e : set){
            int i = 0;
            while(i < list.size()){
                r = order.relation(e,list.get(i));
                if(r != null && r > 0){
                    break;
                }
                i++;
            }
            list.add(i,e);
        }
        return list;
    }
    
    /**
     * Returns a list that respect the order of the poset given as a parameter and contains each of its elements.
     * @param <T>
     * @param poset
     * @return 
     */
    public static <T> List<T> totalOrdering(Poset<T> poset){
        return totalOrdering(poset,poset);
    }
    
    public static Integer productOfRelations(Integer r1, Integer r2){
        if (r1 == null || r2 == null) return null;
        if (r1 == 0) return r2;
        if (r2 == 0) return r1;
        if (!Objects.equals(r1, r2)) return null;
        return r2;
    }
    
    public static Integer componentWiseRelation(int[] x, int[] y){
        Integer res = 0;
        int r;
        for(int i = 0; i < x.length && res != null; i ++){
            r = 0;
            if(x[i] < y[i]){
                r=1;
            }else if (x[i] > y[i]){
                r=-1;
            }
            res = productOfRelations(res, r);
        }
        return res;
    }
    
    
    public static int randomInt(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max+1);
    } 
}
