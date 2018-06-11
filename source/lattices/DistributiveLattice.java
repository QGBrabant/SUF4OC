/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lattices;

import java.util.List;
import orders.JoinComputable;
import orders.MeetComputable;

/**
 *
 * @author qgbrabant
 * @param <T> the type of the elements of the DistributiveLattive
 */
public interface DistributiveLattice<T extends MeetComputable & JoinComputable> extends Lattice<T> {

    /**
     * Useful for for implementing the algorithms of "Interpolation of partial
     * functions by lattice polynomial functions: a polynomial time algorithm".
     *
     *
     * @param i
     * @return s_t^+ (Lemma 1)
     */
    public T getStPlus(int i);

    /**
     * Useful for for implementing the algorithms of "Interpolation of partial
     * functions by lattice polynomial functions: a polynomial time algorithm".
     *
     *
     * @param i
     * @return s_t^- (Lemma 1)
     */
    public T getStMinus(int i);

    /**
     * 
     * @return the set of join-irreducible elements of the lattice
     */
    public List<T> getJointIrreducibles();
}
