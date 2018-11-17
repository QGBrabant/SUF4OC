/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tuples;

/**
 *
 * @author qgbrabant
 */
public class BoxBTuple extends BTupleImpl {
    private int content;

    public BoxBTuple(int[] bin, int c) {
        super(bin);
        this.content =c;
    }
    
    public BoxBTuple(int[] bin) {
        super(bin);
    }
    
    public void setContent(int c){
        this.content = c;
    }
    
    public int getContent(){
        return content;
    }
}
