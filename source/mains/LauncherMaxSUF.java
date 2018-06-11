/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mains;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lattices.Chain;
import lattices.impls.Rank;
import ordinalclassification.InstanceList;
import rules.DataTools;
import rules.SSProcesses;

/**
 *
 * @author qgbrabant
 */
public class LauncherMaxSUF {

    private static final Map<String, String> options = new HashMap<>();
    private static final List<String> argList = new ArrayList<>();

    public static String getOption(String optName) {
        return LauncherMaxSUF.options.get(optName);
    }

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].length() < 2) {
                        throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                    } else {
                        if (args.length - 1 == i) {
                            throw new IllegalArgumentException("Expected argument after: " + args[i]);
                        }
                        options.put(args[i], args[i + 1]);
                        i++;
                    }
                    break;
                default:
                    argList.add(args[i]);
                    break;
            }
        }

        if (argList.size() == 0) {
            System.out.println("Specify an argument.");
            System.exit(0);
        }

        switch (argList.get(0)) {
            case "classification":
                ordinalClassificationMaxSUF();
                break;
            default:
                System.out.println("The first argument is incorrect. See readme.md for help.");
        }
    }

    public static boolean argumentPassed(String arg) {
        for (String a : argList) {
            if (arg.equals(a)) {
                return true;
            }
        }
        return false;
    }

    public static void ordinalClassificationMaxSUF() throws IOException {
        String datasetPath = options.get("-data");
        int nbruns = 1;
        double rhoStart = 0.95;
        double rhoEnd = 1.0;
        double rhoStep = 0.1;

        if (options.get("-nbruns") != null) {
            nbruns = Integer.parseInt(options.get("-nbruns"));
        }

        if (argumentPassed("nonparametric")) {
            rhoStart = 1.0;
        } else {
            if (options.get("-rhoStart") != null) {
                rhoStart = Double.parseDouble(options.get("-rhoStart"));
            }
            if (options.get("-rhoEnd") != null) {
                rhoEnd = Double.parseDouble(options.get("-rhoEnd"));
            }
            if (options.get("-rhoStep") != null) {
                rhoStep = Double.parseDouble(options.get("-rhoStep"));
            }
        }

        if (datasetPath == null) {
            System.out.println("Specify the path of the file containing the data (option -data).");
            System.exit(0);
        }

        List<InstanceList<Rank, Chain>> datasets;

        if (datasetPath.equals("VCDomLEMbenchmark")) {
            datasets = DataTools.VCDomLEMBenchmark();
        } else {
            datasets = new ArrayList<>();
            datasets.add(DataTools.extractStandardDataset(
                    datasetPath));
            datasets.get(datasets.size() - 1).setName(datasetPath);
        }
        MaxSUFs.evaluateMaxSUFLearner(datasets, SSProcesses::PROCESS_1, nbruns, rhoStart, rhoEnd, rhoStep);
    }
}
