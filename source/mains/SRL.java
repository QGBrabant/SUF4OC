/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mains;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import static mains.Launcher.analyze_relabeling;
import static mains.Launcher.getDatasets;
import static mains.Launcher.interpolation;
import static mains.Launcher.monotonicClassificationMaxSUF;
import static mains.Launcher.simpleRuleLearning;
import static mains.Launcher.translation;
import miscellaneous.Context;
import miscellaneous.Experiments;
import monotonic.Classifier;
import monotonic.InstanceList;
import monotonic.SSProcesses;

/**
 *
 * @author qgbrabant
 */
public class SRL {
    
    public static void main(String[] args) throws IOException {
        Launcher.parseArguments(args);
        
        int nbruns = 1;

        boolean rejectionRules = false;

        if (Context.getArgList().contains("-rejection")) {
            rejectionRules = true;
        }
        if (Context.getOption("-nbruns") != null) {
            nbruns = Integer.parseInt(Context.getOption("-nbruns"));
        }
        
        Function<InstanceList,Classifier> learner =  SSProcesses::SRL;
        if (Context.getArgList().contains("-longrules")) {
            learner = SSProcesses::LAMBDA_RULE_SET;
        }
        
        if(Context.getArgList().contains("-validation")){
            Experiments.evaluateLearner(getDatasets(rejectionRules), learner, nbruns);
        }else{
            Experiments.learnRuleSet(getDatasets(rejectionRules), learner,rejectionRules);
        }
        
    }
}
