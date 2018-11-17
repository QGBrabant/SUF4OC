/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miscellaneous;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import miscellaneous.Misc;
import monotonic.Classifier;

/**
 * Contains several models and their evaluated performance.
 *
 * @param <C>
 */
public class Result<C extends Classifier> implements Serializable {

    public double rho;
    List<C> coverages;
    List<Double> MER_scores;
    List<Double> MAE_scores;

    public Result() {
        this.coverages = new ArrayList<>();
        this.MER_scores = new ArrayList<>();
        this.MAE_scores = new ArrayList<>();
    }

    public String toString() {
        String res = "rho=" + Misc.round(rho, 2) + "\n";
        res += this.getMeanMAE() + ")\n";

        return res;
    }

    public double getMeanMER() {
        return this.MER_scores
                .stream()
                .reduce(0., (x, y) -> x + y)
                / this.MER_scores.size();
    }
    
    public double getStdMER() {
        double mean = this.getMeanMER();
        return Math.sqrt(this.MER_scores
                .stream()
                .reduce(0., (x, y) -> x + (y - mean)*(y - mean))
                / this.MER_scores.size());
    }
    

    public double getMeanMAE() {
        return this.MAE_scores
                .stream()
                .reduce(0., (x, y) -> x + y)
                / this.MAE_scores.size();
    }
    
    public double getStdMAE() {
        double mean = this.getMeanMAE();
        return Math.sqrt(this.MAE_scores
                .stream()
                .reduce(0., (x, y) -> x + (y - mean)*(y - mean))
                / this.MAE_scores.size());
    }

    public Result merge(Result r) {
        assert this.rho == r.rho;

        Result res = new Result();
        res.rho = r.rho;
        res.coverages = new ArrayList<>(this.coverages);
        res.coverages.addAll(r.coverages);
        res.MAE_scores = new ArrayList<>(this.MAE_scores);
        res.MAE_scores.addAll(r.MAE_scores);
        res.MER_scores = new ArrayList<>(this.MER_scores);
        res.MER_scores.addAll(r.MER_scores);
        return res;
    }

    
}
