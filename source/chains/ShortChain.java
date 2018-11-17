/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chains;

import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import chains.Chain;

/**
 *
 * @author qgbrabant
 */


public class ShortChain implements Chain{
    private String[] elementNames;
    
    public ShortChain(int size){
        this.elementNames = new String[size];
        for(int i = 0 ; i < size ; i ++){
            this.elementNames[i] = ""+i;
        }
    }
    
    public ShortChain(String[] names){
        this.elementNames = Arrays.copyOf(names, names.length);
    }
 
    public int getTop() {
        return this.elementNames.length-1;
    }
    

    public String getName(int i){
        return this.elementNames[i];
    }


    public int randomValue() {
         return ThreadLocalRandom.current().nextInt(0, this.elementNames.length);
    }

    /*@Override
    public Chain inverse() {
        int[] inverseTab = new int[this.elements.length];
        for(int i = 0; i < this.elements.length ; i++){
            inverseTab[inverseTab.length- 1 - i] = new int(this.elements[i].size(),this.elements[i].size() - this.elements[i].toInt());
            assert inverseTab[inverseTab.length- 1 - i].toInt() >= 0;
        }
        ShortChain res = new ShortChain(this.size());
        res.elements = inverseTab;
        
        System.out.println(Arrays.toString(this.elements));
        System.out.println(Arrays.toString(res.elements));
        
        return res;
    }*/

    @Override
    public int size() {
        return elementNames.length;
    }
    
}
