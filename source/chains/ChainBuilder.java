/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is for building Chains by using double values.
 * Step 1: create a new Chain builder
 Step 2: add all double values that you want in your chain
 Step 3: call buildChain, which provide a ChainBuilder.Lexicon.
 
 ChainBuilder.Lexicon contains a Chain and a Map<Double,Rank>,
 * and allows to get the element of the Chain that corresponds to a double (provided this double was added during step 2).
 * @author qgbrabant
 */
public class ChainBuilder{

    private Set<String> values;

    public ChainBuilder() {
        this.values = new HashSet<>();
    }

    public boolean add(String value) {
        return this.values.add(value);
    }
    

    public Chain buildChain(){
        return this.buildChain(true);
    }
    
    public Chain buildChain(boolean ascending) {

        List<String> list = new ArrayList<>(this.values);
        Collections.sort(list, (x,y) -> {return Double.compare(Double.parseDouble(x), Double.parseDouble(y));});
        
        Chain chain = new Chain(list, ">=");

        return ascending ? chain : chain.inverse();
    }


}
