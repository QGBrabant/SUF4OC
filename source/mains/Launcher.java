/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mains;

import miscellaneous.Experiments;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import miscellaneous.Context;
import monotonic.InstanceList;
import monotonic.DataTools;
import monotonic.SSProcesses;

/**
 *
 * @author qgbrabant
 */
public class Launcher {

    private static String action = null;

    private static final List<String> ACTION_NAMES = Arrays.asList(new String[]{"rlsuf", "srl", "relabeling", "translation", "interpolation"});

    private static final List<String> OPTION_NAMES
            = Arrays.asList(new String[]{"-nbruns", "-rhostart", "-rhoend", "-rhostep", "-data"});

    public static void parseArguments(String[] args){
        for (int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].length() < 2) {
                        throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                    } else {

                        if (OPTION_NAMES.contains(args[i].toLowerCase())) {
                            if (args.length - 1 == i) {
                                throw new IllegalArgumentException("Expected argument after: " + args[i]);
                            }
                            System.out.println(args[i].toLowerCase() + ":" + args[i + 1].toLowerCase());
                            Context.setOption(args[i].toLowerCase(), args[i + 1]);
                            i++;
                        } else {
                            Context.addArgument(args[i].toLowerCase());
                            System.out.println(args[i].toLowerCase());
                        }
                    }
                    break;
                default:
                    if (ACTION_NAMES.contains(args[i].toLowerCase())) {
                        action = args[i].toLowerCase();
                    }
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        parseArguments(args);

        if (action == null) {
            System.out.println("Put one of these arguments : " + ACTION_NAMES.toString());
            System.exit(0);
        }
        switch (action) {
            case "rlsuf":
                monotonicClassificationMaxSUF();
                break;
            case "srl":
                simpleRuleLearning();
                break;
            case "relabeling":
                analyze_relabeling();
                break;
            case "interpolation":
                interpolation();
                break;
            case "translation":
                translation();
                break;
            default:
                System.out.println("The first argument is incorrect. See readme.md for help.");
        }
    }

    public static List<InstanceList> getDatasets(boolean rejectionRules) throws IOException {
        String datasetPath = Context.getOption("-data");
        if (datasetPath == null) {
            System.out.println("Specify the path of the file containing the data (option -data).");
            System.exit(0);
        }
        List<InstanceList> datasets;
        if (datasetPath.toLowerCase().equals("vcdomlembenchmark")) {
            datasets = DataTools.VCDomLEMBenchmark(rejectionRules);
        } else if (datasetPath.toLowerCase().equals("calligraphy")) {
            datasets = DataTools.calligraphyBenchmark(rejectionRules);
        } else {
            datasets = new ArrayList<>();
            datasets.add(DataTools.extractDataset(
                    datasetPath, rejectionRules));
            datasets.get(datasets.size() - 1).setName(datasetPath);
        }

        return datasets;
    }

    public static void translation() throws IOException {
        System.out.println("Translation");
        boolean rejectionRules = false;

        if (Context.getArgList().contains("-rejection")) {
            rejectionRules = true;
        }

        Experiments.rulesTranslation(getDatasets(rejectionRules));
    }

    public static void interpolation() throws IOException {
        boolean rejectionRules = false;
        int nbruns = 10;

        if (Context.getOption("-nbruns") != null) {
            nbruns = Integer.parseInt(Context.getOption("-nbruns"));
        }
        if (Context.getArgList().contains("-rejection")) {
            rejectionRules = true;
        }

        Experiments.SUFinterpolationBenchmark(getDatasets(rejectionRules),!Context.getArgList().contains("-longrules"));

    }

    public static void monotonicClassificationMaxSUF() throws IOException {
        System.out.println("===== RL-SUF =====");
        int nbruns = 1;
        double rhoStart = 1.01;
        double rhoEnd = 1.01;
        double rhoStep = 0.01;
        boolean rejectionRules = false;
        
        
        if (Context.getArgList().contains("-rejection")) {
            rejectionRules = true;
        }
        if (Context.getOption("-nbruns") != null) {
            nbruns = Integer.parseInt(Context.getOption("-nbruns"));
        }

        if (Context.getOption("-rhoStart") != null) {
            rhoStart = Double.parseDouble(Context.getOption("-rhoStart"));
        }
        if (Context.getOption("-rhoEnd") != null) {
            rhoEnd = Double.parseDouble(Context.getOption("-rhoEnd"));
        }
        if (Context.getOption("-rhoStep") != null) {
            rhoStep = Double.parseDouble(Context.getOption("-rhoStep"));
        }
        if (Context.getArgList().contains("-longrules")) {
            Experiments.evaluateMaxSUFLearner(getDatasets(rejectionRules), SSProcesses::PROCESS_1, nbruns, rhoStart, rhoEnd, rhoStep);
        }else{
            Experiments.evaluateMaxSUFLearner(getDatasets(rejectionRules), SSProcesses::PROCESS_2, nbruns, rhoStart, rhoEnd, rhoStep);
        }
    }

    public static void simpleRuleLearning() throws IOException {
        System.out.println("===== SRL =====");
        int nbruns = 1;

        boolean rejectionRules = false;

        if (Context.getArgList().contains("-rejection")) {
            rejectionRules = true;
        }
        if (Context.getOption("-nbruns") != null) {
            nbruns = Integer.parseInt(Context.getOption("-nbruns"));
        }
        if (Context.getArgList().contains("-longrules")) {
            Experiments.evaluateLearner(getDatasets(rejectionRules), SSProcesses::LAMBDA_RULE_SET, nbruns);
        }else{
            Experiments.evaluateLearner(getDatasets(rejectionRules), SSProcesses::SRL, nbruns); 
        }
    }

    public static void analyze_relabeling() throws IOException {

        int nbruns = 1;

        boolean rejectionRules = false;

        if (Context.getOption("-nbruns") != null) {
            nbruns = Integer.parseInt(Context.getOption("-nbruns"));
        }

        Experiments.analyzeRelabeling(getDatasets(rejectionRules), nbruns);
    }

}
