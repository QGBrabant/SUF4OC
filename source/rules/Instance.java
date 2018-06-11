package rules;

import java.util.Objects;
import miscellaneous.Tuple;
import miscellaneous.TupleImpl;
import lattices.Chain;
import orders.Order;
import orders.PartiallyOrderable;
import lattices.impls.Rank;

/**
 * @author qgbrabant
 */
public interface Instance<T, O extends Order<T>> {    
    public int getArity();
    public T[] copyOfFeatures();
    public TupleImpl<T> getFeatures();
    public T getFeature(int i);
    public T getLabel();
    public Instance<T,O> copy();
    
    public O[] getDomain();
    public O getCodomain();
    public Integer relation(T[] x);

}
