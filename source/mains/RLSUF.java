/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mains;

import java.io.IOException;
import java.util.function.BiFunction;
import static mains.Launcher.getDatasets;
import miscellaneous.Context;
import miscellaneous.Experiments;
import monotonic.InstanceList;
import monotonic.MaxSUF;
import monotonic.SSProcesses;

/**
 *
 * @author qgbrabant
 */
public class RLSUF {

    public static void main(String[] args) throws IOException {
        Launcher.parseArguments(args);

        int nbruns = 1;

        double rhoStart = 1.01;
        double rhoEnd = 1.01;
        double rhoStep = 0.01;
        boolean rejectionRules = false;

        if (Context.getOption("-rho") != null) {
            rhoStart = Double.parseDouble(Context.getOption("-rho"));
            rhoEnd = rhoStart;
        }

        if (Context.getArgList().contains("-rejection")) {
            rejectionRules = true;
        }

        BiFunction<InstanceList, Double[], MaxSUF> learner = SSProcesses::PROCESS_2;
        if (Context.getArgList().contains("-longrules")) {
            learner = SSProcesses::PROCESS_1;
        }

        if (Context.getArgList().contains("-validation")) {
            if (Context.getOption("-nbruns") != null) {
                nbruns = Integer.parseInt(Context.getOption("-nbruns"));
            }

            if (Context.getOption("-rhoStart") != null) {
                rhoStart = Double.parseDouble(Context.getOption("-rhoStart"));
            }
            if (Context.getOption("-rhoEnd") != null) {
                rhoEnd = Double.parseDouble(Context.getOption("-rhoEnd"));
            }
            
            if(rhoEnd < rhoStart){
                double swap = rhoEnd;
                rhoEnd = rhoStart;
                rhoStart = swap;
            }
            if (Context.getOption("-rhoStep") != null) {
                rhoStep = Double.parseDouble(Context.getOption("-rhoStep"));
            }
            Experiments.evaluateMaxSUFLearner(getDatasets(), learner, nbruns, rhoStart, rhoEnd, rhoStep);
        } else {
            Experiments.learnMaxSUF(getDatasets(), learner, rhoStart, rejectionRules);
        }

    }
}
