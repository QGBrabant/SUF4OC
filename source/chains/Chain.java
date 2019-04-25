/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chains;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author qgbrabant
 */


public class Chain {
    private final String orderSymbol;
    private String[] elementNames;
    private Chain inverse;
    private Map<String,Integer> lex;
    
    public Chain(int size){
        this.elementNames = new String[size];
        for(int i = 0 ; i < size ; i ++){
            this.elementNames[i] = ""+i;
        }
        this.orderSymbol = ">=";
        makeLex();
    }
    
    public Chain(String[] names, String orderSymbol){
        this.elementNames = Arrays.copyOf(names, names.length);
        this.orderSymbol = orderSymbol;
        makeLex();
    }
    
    public Chain(List names, String orderSymbol){
        this.elementNames = new String[names.size()];
        for(int i = 0; i < names.size() ; i ++){
            elementNames[i] = names.get(i).toString();
        }
        this.orderSymbol = orderSymbol;
        makeLex();
    }
    
    private void makeLex(){
        this.lex = new HashMap<>();
        for(int i = 0; i < this.elementNames.length ; i ++){
            this.lex.put(this.elementNames[i], i);
        }
    }
    
 
    public int getTop() {
        return this.elementNames.length-1;
    }
    

    public String getName(int i){
        return this.elementNames[i];
    }

    public String getOrderSymbol(){
        return this.orderSymbol;
    }

    public int randomValue() {
         return ThreadLocalRandom.current().nextInt(0, this.elementNames.length);
    }

    private void computeInverse() {
        String[] inverseNames = new String[this.size()];
        for(int i = 0; i < this.size() ; i++){
            inverseNames[this.inv(i)] = this.elementNames[i];
        }
        this.inverse = new Chain(inverseNames, "<=");
        this.inverse.inverse = this;
    }
    
    public Chain inverse(){
        if(this.inverse == null){
            this.computeInverse();
        }
        return this.inverse;
    }
    
    public int inv(int x){
        return this.size() - 1 - x;
    }

    public int size() {
        return elementNames.length;
    }

    public int getByName(String elementName){
        assert this.lex.get(elementName) != null : elementName;
        return this.lex.get(elementName);
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(orderSymbol + " [");
        for(String s : this.elementNames){
            sb.append(s);
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
    
}
