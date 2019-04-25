/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miscellaneous;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import chains.Chain;
import chains.Chain;
import java.text.DecimalFormat;
import monotonic.Classifier;
import monotonic.InstanceList;
import monotonic.DataTools;
import monotonic.MaxSUF;
import monotonic.RuleSet;
import monotonic.SSProcesses;
import static monotonic.SSProcesses.flexerpolate;
import monotonic.SugenoUtility;

/**
 *
 * @author qgbrabant
 */
public class Experiments {

    public static void analyzeRelabeling(List<InstanceList> datasets, int nbRuns) throws IOException {
        InstanceList D;
        RuleSet D2;
        double[] MERs = new double[datasets.size()];
        double[] MAEs = new double[datasets.size()];
        Arrays.fill(MERs, 0.);
        Arrays.fill(MAEs, 0.);

        for (int k = 0; k < nbRuns; k++) {
            for (int i = 0; i < datasets.size(); i++) {
                D = datasets.get(i);
                D2 = new RuleSet(SSProcesses.optimalRelabeling(D));
                MERs[i] += DataTools.MER(D2, D);
                MAEs[i] += DataTools.MAE(D2, D);
            }
        }
        for (int i = 0; i < datasets.size(); i++) {
            MERs[i] /= nbRuns;
            MAEs[i] /= nbRuns;
        }

        System.out.println("MER: " + Arrays.toString(MERs));
        System.out.println("MAE: " + Arrays.toString(MAEs));

    }

    public static void SUFinterpolationBenchmark(List<InstanceList> datasets, boolean shortrules) {
        int nbLoops = 1000;
        for (InstanceList dataset : datasets) {
            List<Double> scores = new ArrayList<>();
            dataset = SSProcesses.optimalRelabeling(dataset);
            double nbInstances = 0.;
            double nbRules = 0.;
            double nbSUFs = 0.;
            double std = 0.;
            int min = Integer.MAX_VALUE;
            InstanceList Rlist = dataset.reduction();
            for (int i = 0; i < nbLoops; i++) {

                InstanceList Rlist2;
                Collections.shuffle(Rlist);
                if(shortrules){
                    Rlist2 = SSProcesses.pruneCriteria(Rlist, dataset);
                }else{
                    Rlist2 = Rlist;
                }
                MaxSUF covering = flexerpolate(Rlist2, dataset);

                nbInstances += dataset.size();
                nbRules += Rlist2.size();
                nbSUFs += covering.size();
                scores.add((double) covering.size());

                if (covering.size() < min) {
                    min = covering.size();
                }
            }
            final double mean = scores.stream().reduce(0., (x, y) -> x + y) / nbLoops;
            std += Math.sqrt(scores
                    .stream()
                    .map(x -> (x - mean) * (x - mean))
                    .reduce(0., (x, y) -> x + y)
                    / nbLoops);
            nbInstances /= nbLoops;
            nbRules /= nbLoops;
            nbSUFs /= nbLoops;

            DecimalFormat df = new DecimalFormat("#.##");
            System.out.print(" & " + ((int) nbInstances) + "," + ((int) nbRules) + "," + ((int) nbSUFs) + "(" + min + ")" + " $\\pm$ "
                    + df.format(std));
        }
    }

    public static void rulesTranslation(List<InstanceList> datasets) {

        int nbLoops = 1000;
        for (InstanceList dataset : datasets) {
            List<Double> scores = new ArrayList<>();
            dataset = SSProcesses.optimalRelabeling(dataset);
            RuleSet R = (RuleSet) SSProcesses.SRL(dataset);
            R.addCeiling();
            double nbInstances = 0.;
            double nbRules = 0.;
            double nbSUFs = 0.;
            double std = 0.;
            int min = Integer.MAX_VALUE;

            for (int i = 0; i < nbLoops; i++) {

                List<SugenoUtility> covering = SSProcesses.SUF_COVERING(R);

                nbInstances += dataset.size();
                nbRules += R.size();
                nbSUFs += covering.size();
                scores.add((double) covering.size());

                if (covering.size() < min) {
                    min = covering.size();
                }
                /*
                        for (Instance row : dataset) {
                            int truc = 0;
                            for (SugenoUtility sug : covering) {
                                truc = Math.max(truc, sug.apply(row.getFeatures()));
                            }
                            assert truc == row.getLabel() : row.getFeatures() + " , " + row.getLabel() + " , " + truc + "\n" + dataset + "\n" + covering;
                        }*/
            }
            //System.out.println(scores);
            final double mean = scores.stream().reduce(0., (x, y) -> x + y) / nbLoops;
            std += Math.sqrt(scores
                    .stream()
                    .map(x -> (x - mean) * (x - mean))
                    .reduce(0., (x, y) -> x + y)
                    / nbLoops);
            nbInstances /= nbLoops;
            nbRules /= nbLoops;
            nbSUFs /= nbLoops;

            DecimalFormat df = new DecimalFormat("#.##");
            System.out.print(" & " + ((int) nbInstances) + "," + ((int) nbRules) + "," + ((int) nbSUFs) + "(" + min + ")" + " $\\pm$ "
                    + df.format(std));
        }

    }

    public static void SUFinterpolationRandomData(int nbruns, Function<InstanceList, MaxSUF> interpolator) {
        for (int d = 2; d <= 6; d++) {
            System.out.print("& " + d);
            for (int h = 2; h <= 6; h++) {
                double nbInstances = 0.;
                double nbRules = 0.;
                double nbSUFs = 0.;
                double std = 0.;

                for (int k = 0; k < nbruns; k++) {

                    Chain[] domain = new Chain[d];
                    for (int i = 0; i < d; i++) {
                        domain[i] = new Chain(h);
                    }
                    Chain codomain = new Chain(h);
                    int m = (int) Math.ceil(Math.pow(h, d) * 0.05);
                    InstanceList dataset = DataTools.randomMonotonicDataset(m, domain, codomain);

                    List<Double> scores = new ArrayList<>();

                    for (int i = 0; i < 100; i++) {
                        Collections.shuffle(dataset);
                        List<SugenoUtility> covering = interpolator.apply(dataset);
                        RuleSet inferedRuleSet = new RuleSet(domain, codomain, dataset);

                        nbInstances += dataset.size();
                        nbRules += inferedRuleSet.size();
                        nbSUFs += covering.size();
                        scores.add((double) covering.size());
                        /*
                        for (Instance row : dataset) {
                            int truc = 0;
                            for (SugenoUtility sug : covering) {
                                truc = Math.max(truc, sug.apply(row.getFeatures()));
                            }
                            assert truc == row.getLabel() : row.getFeatures() + " , " + row.getLabel() + " , " + truc + "\n" + dataset + "\n" + covering;
                        }*/
                    }
                    //System.out.println(scores);
                    final double mean = scores.stream().reduce(0., (x, y) -> x + y) / 100.;
                    std += Math.sqrt(scores
                            .stream()
                            .map(x -> (x - mean) * (x - mean))
                            .reduce(0., (x, y) -> x + y)
                            / 100.);
                }
                nbInstances /= nbruns * 100;
                nbRules /= nbruns * 100;
                nbSUFs /= nbruns * 100;
                std /= nbruns;

                DecimalFormat df = new DecimalFormat("#.##");
                System.out.print(" & " + ((int) nbInstances) + "," + ((int) nbRules) + "," + ((int) nbSUFs) + " $\\pm$ "
                        + df.format(std));
            }
            System.out.print("\\\\ \n");
        }
    }

    public static Result[] tenfoldMaxSUFLearing(
            InstanceList dataset,
            BiFunction<InstanceList, Double[], MaxSUF> process)
            throws IOException {
        return tenfoldMaxSUFLearing(dataset, process, false);
    }

    public static Result[] tenfoldMaxSUFLearing(
            InstanceList dataset,
            BiFunction<InstanceList, Double[], MaxSUF> process,
            boolean silent)
            throws IOException {

        double rhoStart = 0.95;
        double rhoEnd = 1.0;
        double rhoStep = 0.01;

        return tenfoldMaxSUFLearning(dataset, process, silent, rhoStart, rhoEnd, rhoStep);

    }

    /**
     * Learns and evaluate the accuracy of a max-SUF on data, using tenfold
     * cross validation.
     *
     * @param dataset
     * @param process a bi-function that outputs the max-SUF learned from the
     * data
     * @param silent hides some information display if set to true
     * @param rhoStart first value of the rho parameter
     * @param rhoEnd last value of the rho parameter
     * @param rhoStep amount of change between two values of the rho parameter
     * in two successive tests
     * @return
     * @throws IOException
     */
    public static Result[] tenfoldMaxSUFLearning(
            InstanceList dataset,
            BiFunction<InstanceList, Double[], MaxSUF> process,
            boolean silent,
            double rhoStart,
            double rhoEnd,
            double rhoStep)
            throws IOException {

        int nbColumns = (int) Math.ceil(Math.abs((rhoEnd - rhoStart) / rhoStep));
        if (nbColumns == 0) {
            nbColumns = 1;
        }
        Result[] resTab = new Result[nbColumns];

        double[] rhoTab = new double[nbColumns];

        double rho = rhoStart;
        for (int i = 0; i < nbColumns; i++) {
            rhoTab[i] = rho;
            rho += rhoStep;
        }

        List<InstanceList> pieces = new ArrayList<>(dataset.getPieces(10));
        MaxSUF coverage;
        InstanceList train;
        InstanceList test;

        for (int j = 0; j < rhoTab.length; j++) {
            resTab[j] = new Result();
            resTab[j].rho = rhoTab[j];

            for (int f = 0; f < 10; f++) {
                test = pieces.get(0);
                pieces.remove(0);
                train = new InstanceList(pieces);
                pieces.add(test);

                coverage = process.apply(train, new Double[]{rhoTab[j]});
                resTab[j].coverages.add(coverage);
                resTab[j].MAE_scores.add(DataTools.MAE(coverage, test));
                resTab[j].MER_scores.add(DataTools.MER(coverage, test));

            }
        }

        return resTab;
    }

    public static List<Result> extractParetoFront(Result[][] resTab, Function<Result, Double> eval) {
        Set<Result> paretoFront = new HashSet<>();
        Result res;
        boolean maximal;
        double score;
        double score2;

        for (int i = 0; i < resTab.length; i++) {
            for (int j = 0; j < resTab[i].length; j++) {
                Iterator<Result> it = paretoFront.iterator();
                maximal = true;

                score = eval.apply(resTab[i][j]);

                while (it.hasNext() && maximal) {
                    res = it.next();

                    score2 = eval.apply(res);

                    if (resTab[i][j].getMeanMAE() <= res.getMeanMAE()
                            && score <= score2) {
                        it.remove();
                    } else if (resTab[i][j].getMeanMAE() >= res.getMeanMAE()
                            && score >= score2) {
                        maximal = false;
                    }
                }
                if (maximal) {
                    paretoFront.add(resTab[i][j]);
                }
            }
        }

        List<Result> list = new ArrayList<>(paretoFront);

        Collections.sort(list, (x, y) -> {
            return eval.apply(x).compareTo(eval.apply(y));
        });
        return list;
    }

    public static void evaluateMaxSUFLearner(
            List<InstanceList> datasets,
            BiFunction<InstanceList, Double[], MaxSUF> process,
            int numberOfRun) throws IOException {
        evaluateMaxSUFLearner(datasets, process, numberOfRun, 1.0, 1.0, -0.01);
    }

    public static void evaluateMaxSUFLearner(
            List<InstanceList> datasets,
            BiFunction<InstanceList, Double[], MaxSUF> process,
            int numberOfRun,
            double rhoStart,
            double rhoEnd,
            double rhoStep)
            throws IOException {

        Result[] tabRes;
        for (InstanceList D : datasets) {
            Context.startNewExperiment();
            System.out.println("==================================");
            System.out.println(D.datasetInformation());
            if (rhoStart != rhoEnd) {
                System.out.println("rho from " + rhoStart + " to " + rhoEnd + " (steps of " + rhoStep + ")");
            } else {
                System.out.println("rho=" + rhoStart);
            }
            System.out.print("Run:");
            for (int k = 0; k < numberOfRun; k++) {
                System.out.print(" " + (k + 1) + "/" + numberOfRun);
                tabRes = tenfoldMaxSUFLearning(D, process, true, rhoStart, rhoEnd, rhoStep);
                for (int j = 0; j < tabRes.length; j++) {
                    Context.addResult(D.getName() + j, tabRes[j]);
                }
            }
            System.out.println("");

            displayResultsMaxSUFs(D.getName(), 1, rhoStart == rhoEnd, numberOfRun > 1);
        }
    }

    public static void displayResultsMaxSUFs(String name, int d, boolean simple, boolean  metaStd) {
        System.out.println("Results depending on the rho parameter value:");

        List<Function<Result, Double>> funcs = new ArrayList<>();
        funcs.add(ResultWatcher::rho);
        funcs.add(ResultWatcher::MER);
        funcs.add(ResultWatcher::stdMER);
        funcs.add(ResultWatcher::MAE);
        funcs.add(ResultWatcher::stdMAE);
        funcs.add(ResultWatcher::nbSUF);
        funcs.add(ResultWatcher::nbRules);
        funcs.add(ResultWatcher::avgRuleLength);
            
            Context.display(
                    funcs,
                    new String[]{"rho"," MER", "(over folds) std", "MAE", "(over folds) std", "nb SUFs", "nb rules", "rule lengths"},
                    new int[]{2, 3, 4, 3, 4, 1, 1, 1},
                    new boolean[]{false, metaStd, false, metaStd, false, false, false, false});
        if (simple) {
            System.out.println("Rule length cumulative distribution (length:ratio_of_rules_at_most_size_L):");
        } else {
            System.out.println("Rule length cumulative distribution (for the best MAE) (length:ratio_of_rules_at_most_size_L):");
        }
        displayRuleLengthDistrib();

    }

    public static void displayRuleLengthDistrib() {
        List<Entry<String, ResultStack>> list;

        list = new ArrayList<>(
                Context.getParetoOptima(ResultWatcher::MAE, false, ResultWatcher::MAE, false));
        assert !list.isEmpty();
        List<Double> lengths
                = Collections.min(
                        list, ((x, y) -> {
                            return x.getValue().getMeanValue(ResultWatcher::MAE).compareTo(y.getValue().getMeanValue(ResultWatcher::MAE));
                        })).getValue().getMeanValueRow(ResultWatcher::ruleLenghtDistrib);

        for (int i = 0; i < lengths.size(); i++) {
            System.out.print(i + ":" + Misc.round(lengths.get(i), 3) + " ");
        }
        System.out.println();
    }

    public static void displayBestTradeoffs(String name, int d, Function<Result, Double> f, boolean b1, Function<Result, Double> g, boolean b2) {
        List<Entry<String, ResultStack>> list;
        list = new ArrayList<>(
                Context.getParetoOptima(f, b1, g, b2));
        Collections.sort(list, ((x, y) -> {
            return x.getValue().getMeanValue(g).compareTo(y.getValue().getMeanValue(g));
        }));
        list.stream().forEach(
                (x) -> {
                    System.out.print("(" + Misc.round(x.getValue().getMeanValue(g), 2) + "," + Misc.round(x.getValue().getMeanValue(f), 3) + ") ");
                }
        );
    }

    public static void evaluateLearner(
            List<InstanceList> datasets,
            Function<InstanceList, Classifier> process,
            int numberOfRun,
            int nbfolds)
            throws IOException {

        Result res;
        for (InstanceList D : datasets) {
            Context.startNewExperiment();
            System.out.println("==================================");
            System.out.println(D.datasetInformation());
            System.out.print("Run:");
            for (int k = 0; k < numberOfRun; k++) {
                System.out.print(" " + (k + 1) + "/" + numberOfRun);
                res = manyFoldEvaluation(D, process, nbfolds, true);
                Context.addResult(D.getName(), res);

            }
            System.out.println("");

            List<Function<Result, Double>> funcs = new ArrayList<>();
            //funcs.add(ResultWatcher::rho);
            funcs.add(ResultWatcher::MER);
            funcs.add(ResultWatcher::stdMER);
            funcs.add(ResultWatcher::MAE);
            funcs.add(ResultWatcher::stdMAE);
            funcs.add(ResultWatcher::nbRules);
            funcs.add(ResultWatcher::avgRuleLength);
            
            boolean metaStd = numberOfRun > 1;
            
            Context.display(
                    funcs,
                    new String[]{"MER", "(over folds) std", "MAE", "(over folds) std", "nb rules", "rule lengths"},
                    new int[]{3, 4, 3, 4, 1, 1},
                    new boolean[]{metaStd, false, metaStd, false, false, false});
            System.out.println("Rule length cumulative distribution (max-length:ratio):");
            displayRuleLengthDistrib();
        }
    }

    public static Result manyFoldEvaluation(
            InstanceList dataset,
            Function<InstanceList, Classifier> process,
            int nbfolds,
            boolean silent)
            throws IOException {

        Result res = new Result();

        List<InstanceList> pieces = new ArrayList<>(dataset.getPieces(nbfolds));
        Classifier R;
        InstanceList train;
        InstanceList test;

        for (int f = 0; f < nbfolds; f++) {
            test = pieces.get(0);
            pieces.remove(0);
            train = new InstanceList(pieces);
            pieces.add(test);

            R = process.apply(train);
            
            if(R.isRejection()){
                test = test.inverse();
            }

            res.MAE_scores.add(DataTools.MAE(R, test));
            res.MER_scores.add(DataTools.MER(R, test));
            res.coverages.add(R);
        }

        return res;
    }

    public static void learnRuleSet(
            List<InstanceList> datasets,
            Function<InstanceList, Classifier> process,
            boolean rejectionRules)
            throws IOException {

        for (InstanceList D : datasets) {
            Context.startNewExperiment();
            System.out.println("==================================");
            System.out.println(D.datasetInformation()+"\n");

            RuleSet R = (RuleSet) process.apply(D);
            
            System.out.println(R.niceString(rejectionRules));
        }
    }
    
    public static void learnMaxSUF(
            List<InstanceList> datasets,
            BiFunction<InstanceList, Double[], MaxSUF> process,
            double rho,
            boolean rejectionRules)
            throws IOException {
        Double[] params = new Double[]{rho};
        for (InstanceList D : datasets) {
            Context.startNewExperiment();
            System.out.println("==================================");
            System.out.println(D.datasetInformation()+"\n");

            MaxSUF res = process.apply(D, params);
            
            System.out.println(res);
        }
    }
    
}
