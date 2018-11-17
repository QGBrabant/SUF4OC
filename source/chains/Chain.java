/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chains;

import orders.Poset;

/**
 *
 * @author qgbrabant
 */
public interface Chain {
    public String getName(int i);
    public int size();
    public int randomValue();
    //public Chain inverse();
}
