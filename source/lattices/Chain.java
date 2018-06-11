/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lattices;

import orders.Poset;
import lattices.impls.Rank;

/**
 *
 * @author qgbrabant
 */
public interface Chain extends Poset<Rank>, DistributiveLattice<Rank>{
    public Rank get(int i);
    //public Chain inverse();
}
