/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mains;

import java.io.IOException;
import java.util.function.Function;
import static mains.Launcher.getDatasets;
import miscellaneous.Context;
import miscellaneous.Experiments;
import monotonic.Classifier;
import monotonic.InstanceList;
import monotonic.SSProcesses;

/**
 *
 * @author qgbrabant
 */
public class TS {

    public static void main(String[] args) throws IOException {
        Launcher.parseArguments(args);

        int nbruns = 1;
        int nbfolds = 10;

        if (Context.getOption("-nbruns") != null) {
            nbruns = Integer.parseInt(Context.getOption("-nbruns"));
        }
        if (Context.getOption("-nbfolds") != null) {
            nbfolds = Integer.parseInt(Context.getOption("-nbfolds"));
        }

        Function<InstanceList, Classifier> learner = SSProcesses::twoSidedRuleLearning;

        if (Context.getArgList().contains("-validation")) {
            Experiments.evaluateLearner(getDatasets(), learner, nbruns, nbfolds);
        } else {
            Experiments.learnRuleSet(getDatasets(), learner, false);
        }

    }
}
