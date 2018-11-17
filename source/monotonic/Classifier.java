/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

import tuples.TupleImpl;

/**
 *
 * @author qgbrabant
 */
public interface Classifier {
    public int apply(int[] x); 
    public int apply(TupleImpl x); 
}
