/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author qgbrabant
 */
public class SimulatedAnnealing<S> implements Function<S,S>{
    public double startingTemperature = 1.;
    public Function<Double,Boolean> stopFunction = (x) -> x <= 0.;
    public Function<Double,Double> coolingFunction = (T) -> T * 0.9;
    public Function<S,S> mutationFunction;
    public Function<S,Double> lossFunction;
    public BiFunction<Double,Double,Boolean> acceptationFunction;

    @Override
    public S apply(S t) {
        double T = this.startingTemperature;
        S state = t;
        S state2;
        double error = this.lossFunction.apply(state);
        double error2;
        
        while(!this.stopFunction.apply(T)){
            state2 = this.mutationFunction.apply(state);
            error2 = this.lossFunction.apply(state2);
            if(this.acceptationFunction.apply(error,error2)){
                state = state2;
                error = error2;
            }
            T = this.coolingFunction.apply(T);
        }
        
        return state;
    }

}
