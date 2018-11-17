/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

import java.util.ArrayList;
import java.util.Collection;
import tuples.TupleImpl;

/**
 *
 * @author qgbrabant
 */
public class MaxSUF extends ArrayList<SugenoUtility> implements Classifier{
    public MaxSUF(){
        super();
    }
    
    public MaxSUF(Collection<SugenoUtility> c){
        super(c);
    }
    
    @Override
    public int apply(int[] x) {
        int output = 0;
        for (SugenoUtility su : this) {
            output = Math.max(output, su.apply(x));
        }

        return output;
    }

    @Override
    public int apply(TupleImpl x) {
        int output = 0;
        for (SugenoUtility su : this) {
            output = Math.max(output, su.apply(x));
        }

        return output;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        for(SugenoUtility su: this){
            sb.append(su.toString());
        }
        sb.append("}");
        return sb.toString();
    }
    
}
