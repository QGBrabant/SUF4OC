/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tuples;

import tuples.Tuple;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author qgbrabant
 */
public class TupleImpl implements Tuple{
    protected final int[] array;
    protected int H;
    protected String rep;
    
    public TupleImpl(int... a){
        super();
        this.array= Arrays.copyOf(a, a.length);

        this.precomputeH();
        this.precomputeRep();
    }
    
    protected void precomputeH(){
        H = java.util.Arrays.hashCode(this.array);
    }
    
    protected void precomputeRep(){
        String s = "(";
        for (int array1 : this.array) {
            s += array1 + ",";
        }
        s +=")";
        this.rep = s;
    }
    
    @Override
    public int get(int i){
        return this.array[i];
    }
    
    public int size(){
        return this.array.length;
    }
    
    @Override
    public final int hashCode(){
        return H;
    }
    
    @Override
    public boolean equals(Object o){
        if(this.hashCode()==o.hashCode()){
            if(o instanceof TupleImpl){
                for(int i = 0; i < this.array.length ; i++){
                    assert o != null;
                    if(this.array[i] != ((TupleImpl)o).get(i)){
                        //System.out.println("HERE "+this+" and "+((Tuple)o));
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString(){
        return rep;
    }
    
    @Override
    public int[] toArray(){
        return Arrays.copyOf(this.array,this.array.length);
    }
}
