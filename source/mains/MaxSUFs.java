/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mains;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import miscellaneous.Context;
import miscellaneous.Misc;
import miscellaneous.ResultStack;
import miscellaneous.Tuple;
import miscellaneous.TupleImpl;
import lattices.Chain;
import lattices.impls.Rank;
import lattices.impls.ShortChain;
import ordinalclassification.InstanceList;
import ordinalclassification.OCTable;
import ordinalclassification.OCTools;
import rules.DataTools;
import static rules.DataTools.testAccuracy;
import rules.Instance;
import rules.RuleSetOnChains;
import rules.SRTools;
import rules.SSProcesses;
import rules.SugenoUtility;

/**
 *
 * @author qgbrabant
 */
public class MaxSUFs {

    public static void analyzeMonotonicity(int nbFolds, int nbRuns) throws IOException {
        List<InstanceList<Rank, Chain>> datasets = DataTools.VCDomLEMBenchmark();
        InstanceList<Rank, Chain> D;

        double[] ratios = new double[datasets.size()];
        double[] compBefore = new double[datasets.size()];
        double[] compAfter = new double[datasets.size()];

        for (int k = 0; k < nbRuns; k++) {
            for (int i = 0; i < datasets.size(); i++) {
                D = datasets.get(i);

                List<InstanceList<Rank, Chain>> pieces = new ArrayList<>(D.getPieces(nbFolds));
                InstanceList<Rank, Chain> train;
                InstanceList<Rank, Chain> test;
                InstanceList<Rank, Chain> reduced;

                for (int f = 0; f < nbFolds; f++) {
                    test = pieces.get(0);
                    pieces.remove(0);
                    train = new InstanceList<>(pieces);
                    pieces.add(test);
                    reduced = SSProcesses.monotonize(train);
                    ratios[i] += ((double) reduced.size()) / train.size();
                    compBefore[i] += ((double) train.comparabilityDegree());
                    compAfter[i] += ((double) reduced.comparabilityDegree());
                }
            }
        }
        for (int i = 0; i < datasets.size(); i++) {
            ratios[i] = 1. - (ratios[i] / (nbRuns * nbFolds));
            compBefore[i] = (compBefore[i] / (nbRuns * nbFolds));
            compAfter[i] = (compAfter[i] / (nbRuns * nbFolds));
        }

        System.out.println(Arrays.toString(ratios));
        System.out.println(Arrays.toString(compBefore));
        System.out.println(Arrays.toString(compAfter));

    }

    public static void testGreedySUCoveringOfRandomData() {
        for (int d = 2; d <= 8; d++) {
            System.out.print("& " + d);
            for (int h = 2; h <= 8; h++) {

                Chain[] domain = new Chain[d];
                for (int i = 0; i < d; i++) {
                    domain[i] = new ShortChain(h);
                }
                Chain codomain = new ShortChain(h);
                int m = (int) Math.ceil(Math.pow(h, d) * 0.05);
                OCTable<Rank, Chain> dataset = OCTools.randomMonotonicDataset(m, domain, codomain);

                List<SugenoUtility> covering = greedySUCoveringOfMonotonicData(dataset);
                RuleSetOnChains inferedRuleSet = new RuleSetOnChains(domain, codomain, dataset);
                System.out.print(" & " + dataset.size() + "," + inferedRuleSet.size() + "," + covering.size());
                for (Instance<Rank, Chain> row : dataset) {
                    Rank truc = codomain.getBottom();
                    for (SugenoUtility sug : covering) {
                        truc = codomain.join(truc, sug.getOutput(row.getFeatures()));
                    }
                    assert codomain.relation(truc, row.getLabel()) == 0 : row.getFeatures() + " , " + row.getLabel() + " , " + truc + "\n" + dataset + "\n" + covering;
                }
            }
            System.out.print("\\\\ \n");
        }
    }

    public static Map<Integer, Integer> distributionOfMaxSUFSize(int n, int h, int nbloops) {
        Chain[] domain = new Chain[n];
        for (int i = 0; i < n; i++) {
            domain[i] = new ShortChain(h);
        }
        Chain codomain = new ShortChain(h);
        int m = (int) Math.ceil(Math.pow(h, n) * 0.05);
        OCTable<Rank, Chain> dataset = OCTools.randomMonotonicDataset(m, domain, codomain);

        Map<Integer, Integer> dist = new HashMap<>();

        for (int i = 0; i < nbloops; i++) {
            List<SugenoUtility> covering = greedySUCoveringOfMonotonicData(dataset);
            dist.put(covering.size(), dist.getOrDefault(covering.size(), 0) + 1);
        }

        return dist;
    }

    public static List<SugenoUtility> greedySUCoveringOfMonotonicData(OCTable<Rank, Chain> dataset) {
        Chain[] domain = dataset.getDomain();
        Chain codomain = dataset.getCodomain();
        List<SugenoUtility> covering = new ArrayList<>();
        boolean covered;

        List<Instance<Rank, Chain>> instanceList = new ArrayList<>(dataset);
        Collections.shuffle(instanceList);

        for (Instance<Rank, Chain> example : instanceList) {
            covered = false;
            for (SugenoUtility su : covering) {
                if (su.coverExample(example, dataset)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                covering.add(new SugenoUtility(domain, codomain));
                covered = covering.get(covering.size() - 1).coverExample(example, dataset);
            }
            assert covered;
        }
        return covering;
    }

    public static void IPMU_test_ruleset_to_SS(int nbRun) {
        int nbRules;
        int nbSUF;
        for (int d = 2; d <= 8; d++) {
            System.out.print("& " + d);
            for (int h = 2; h <= 8; h++) {
                nbRules = 0;
                nbSUF = 0;
                for (int j = 0; j < nbRun; j++) {
                    Chain[] domain = new Chain[d];
                    for (int i = 0; i < d; i++) {
                        domain[i] = new ShortChain(h);
                    }
                    Chain codomain = new ShortChain(h);
                    int m = (int) Math.ceil(Math.pow(h, d) * 0.05);
                    OCTable<Rank, Chain> dataset = OCTools.randomMonotonicDataset(m, domain, codomain);
                    RuleSetOnChains ruleSet = new RuleSetOnChains(domain, codomain, dataset);

                    Set<SugenoUtility> covering = IPMU_ruleSetToSS(domain, codomain, ruleSet);

                    nbRules += ruleSet.size();
                    nbSUF += covering.size();
                }

                nbRules = ((int) Math.round(((double) nbRules) / nbRun));
                nbSUF = ((int) Math.round(((double) nbSUF) / nbRun));

                System.out.print(" && " + nbRules + "/" + nbSUF);

            }
            System.out.print("\\\\ \n");
        }
    }

    public static Set<SugenoUtility> IPMU_ruleSetToSS(Chain[] domain, Chain codomain, Set<Instance<Rank, Chain>> ruleSet) {
        SugenoUtility su;

        //Initialize the list of Sugeno utilities with one utility
        Set<SugenoUtility> covering = new HashSet<>();
        covering.add(new SugenoUtility(domain, codomain));

        //A list just for understanding
        List<Instance<Rank, Chain>> listForU = new ArrayList<>();

        //Try to cover each rule of the set
        for (Instance<Rank, Chain> r : ruleSet) {
            listForU.add(r);

            //Try to cover the rule with each Sugeno utility, until one accept it
            boolean covered = false;
            for (SugenoUtility S : covering) {
                covered = S.coverRule(r);
                if (covered) {
                    assert S.getOutput(new TupleImpl<>(r.copyOfFeatures())).equals(r.getLabel()) : new TupleImpl<>(r.copyOfFeatures()) + " , " + r.getLabel() + " , " + S.getOutput(new TupleImpl<>(r.copyOfFeatures())) + "\n" + listForU + "\n" + covering;
                    break;
                }
            }
            //If no SU accepted the rule, create a new SU and add it to the set
            //then add the rule to the new SU
            if (!covered) {
                su = new SugenoUtility(domain, codomain);
                covering.add(su);
                covered = su.coverRule(r);
                assert covered : r;
            }
        }

        for (Instance<Rank, Chain> r : ruleSet) {
            Rank truc = codomain.getBottom();
            for (SugenoUtility sug : covering) {
                truc = codomain.join(truc, sug.getOutput(new TupleImpl<>(r.copyOfFeatures())));
                assert codomain.relation(sug.getOutput(new TupleImpl<>(r.copyOfFeatures())), r.getLabel()) >= 0 : new TupleImpl<>(r.copyOfFeatures()) + " , " + r.getLabel() + " , " + sug.getOutput(new TupleImpl<>(r.copyOfFeatures())) + "\n" + ruleSet + "\n" + sug;
            }
            assert codomain.relation(truc, r.getLabel()) == 0 : new TupleImpl<>(r.copyOfFeatures()) + " , " + r.getLabel() + " , " + truc + "\n" + ruleSet + "\n" + covering;
        }

        return covering;
    }

    /**
     * Contains several models and their evaluated performance (typically: each
     * model obtained via a tenfold cross-validation).
     */
    public static class Result implements Serializable {

        public double rho;
        List<Collection<SugenoUtility>> coverages;
        List<Double> accuracies;

        public Result() {
            this.coverages = new ArrayList<>();
            this.accuracies = new ArrayList<>();
        }

        public String toString() {
            String res = "rho=" + Misc.round(rho, 2) + "\n";
            res += this.getMeanAccuracy() + "(" + Misc.round(getAverageCoverageSize(), 1) + "," + Misc.round(getSummedCapacityComplexity(), 2) + ")\n";

            return res;
        }

        public double getMeanAccuracy() {
            return this.accuracies
                    .stream()
                    .reduce(0., (x, y) -> x + y)
                    / this.accuracies.size();
        }

        public double getAverageCoverageSize() {
            return this.coverages
                    .stream()
                    .map(x -> (double)x.size())
                    .reduce(0., (x, y) -> x + y)
                    / this.coverages.size();
        }

        public double getSummedCapacityComplexity() {
            double c = 0.;
            int nbSUF = 0;

            for (Collection<SugenoUtility> coverage : this.coverages) {
                nbSUF++;
                for (SugenoUtility su : coverage) {
                    c += su.getCapacityComplexity();
                }
            }

            return c / nbSUF;
        }

        public double getAverageRuleSetComplexity() {
            double res = 0.;
            Set<Instance<Rank, Chain>> rules;
            for (Collection<SugenoUtility> coverage : coverages) {
                for (SugenoUtility su : coverage) {
                    rules = su.toRules();
                    for (Instance<Rank, Chain> r : rules) {
                        res += SRTools.LHSSize(r);
                    }
                }

            }

            res /= coverages.size();
            return res;
        }

        public double getAverageRuleLength() {
            double res = 0.;
            int div = 0;
            Set<Instance<Rank, Chain>> rules;
            for (Collection<SugenoUtility> coverage : coverages) {
                for (SugenoUtility su : coverage) {
                    rules = su.toRules();
                    div += rules.size();
                    for (Instance<Rank, Chain> r : rules) {
                        res += SRTools.LHSSize(r);
                    }
                }

            }

            res /= div;
            return res;
        }

        public List<Double> getAccumulativeRuleLength() {
            List<Double> res = new ArrayList<>();
            int div = 0;
            Set<Instance<Rank, Chain>> rules;
            for (Collection<SugenoUtility> coverage : coverages) {
                for (SugenoUtility su : coverage) {
                    rules = su.toRules();
                    div += rules.size();
                    for (Instance<Rank, Chain> r : rules) {
                        for (int i = 0; i < SRTools.LHSSize(r); i++) {
                            if (i >= res.size()) {
                                res.add(0.);
                            }
                            res.set(i, res.get(i) + 1);
                        }
                    }
                }
            }
            res.add(0.);
            for (int i = 0; i < res.size(); i++) {
                res.set(i, res.get(i) / div);
            }
            return res;
        }

        public double getAverageRuleSetSize() {
            return this.coverages
                    .stream()
                    .flatMap(Collection::stream)
                    .map(su -> (double)su.toRules().size())
                    .reduce(0., (x, y) -> x + y)
                    /this.coverages.size();
        }

        public Result merge(Result r) {
            assert this.rho == r.rho;

            Result res = new Result();
            res.rho = r.rho;
            res.coverages = new ArrayList<>(this.coverages);
            res.coverages.addAll(r.coverages);
            res.accuracies = new ArrayList<>(this.accuracies);
            res.accuracies.addAll(r.accuracies);

            return res;
        }
    }

    public static Result[] tenfoldMaxSUFLearing(
            InstanceList<Rank, Chain> dataset,
            BiFunction<InstanceList<Rank, Chain>, Double[], Collection<SugenoUtility>> process)
            throws IOException {
        return tenfoldMaxSUFLearing(dataset, process, false);
    }

    public static Result[] tenfoldMaxSUFLearing(
            InstanceList<Rank, Chain> dataset,
            BiFunction<InstanceList<Rank, Chain>, Double[], Collection<SugenoUtility>> process,
            boolean silent)
            throws IOException {

        double rhoStart = 0.95;
        double rhoEnd = 1.0;
        double rhoStep = 0.01;

        return tenfoldMaxSUFLearing(dataset, process, silent, rhoStart, rhoEnd, rhoStep);

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
    public static Result[] tenfoldMaxSUFLearing(
            InstanceList<Rank, Chain> dataset,
            BiFunction<InstanceList<Rank, Chain>, Double[], Collection<SugenoUtility>> process,
            boolean silent,
            double rhoStart,
            double rhoEnd,
            double rhoStep)
            throws IOException {

        int nbColumns = (int) Math.ceil(Math.abs((rhoEnd - rhoStart) / rhoStep)) + 1;

        Result[] resTab = new Result[nbColumns];

        double[] rhoTab = new double[nbColumns];

        double rho = rhoStart;
        for (int i = 0; i < nbColumns; i++) {
            rhoTab[i] = rho;
            rho += rhoStep;
        }

        List<InstanceList<Rank, Chain>> pieces = new ArrayList<>(dataset.getPieces(10));
        Collection<SugenoUtility> coverage;
        InstanceList<Rank, Chain> train;
        InstanceList<Rank, Chain> test;

        for (int j = 0; j < rhoTab.length; j++) {
            resTab[j] = new Result();
            resTab[j].rho = rhoTab[j];

            for (int f = 0; f < 10; f++) {
                test = pieces.get(0);
                pieces.remove(0);
                train = new InstanceList<>(pieces);
                pieces.add(test);

                coverage = process.apply(train, new Double[]{rhoTab[j]});

                resTab[j].coverages.add(coverage);
                resTab[j].accuracies.add(testAccuracy(coverage, test, true));

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

                    if (resTab[i][j].getMeanAccuracy() >= res.getMeanAccuracy()
                            && score <= score2) {
                        it.remove();
                    } else if (resTab[i][j].getMeanAccuracy() <= res.getMeanAccuracy()
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
            return new Double(eval.apply(x)).compareTo(eval.apply(y));
        });
        return list;
    }

    public static void evaluateMaxSUFLearner(
            List<InstanceList<Rank, Chain>> datasets,
            BiFunction<InstanceList<Rank, Chain>, Double[], Collection<SugenoUtility>> process,
            int numberOfRun) throws IOException {
        evaluateMaxSUFLearner(datasets, process, numberOfRun, 1.0, 1.0, -0.01);
    }

    public static void evaluateMaxSUFLearner(
            List<InstanceList<Rank, Chain>> datasets,
            BiFunction<InstanceList<Rank, Chain>, Double[], Collection<SugenoUtility>> process,
            int numberOfRun,
            double rhoStart,
            double rhoEnd,
            double rhoStep)
            throws IOException {

        Result[] tabRes;
        for (InstanceList<Rank, Chain> D : datasets) {
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
                tabRes = tenfoldMaxSUFLearing(D, process, true, rhoStart, rhoEnd, rhoStep);
                for (int j = 0; j < tabRes.length; j++) {
                    Context.<Result>addResult(tabRes[j], "IPMU_" + D.getName(), (double) j);
                }
            }
            System.out.println("");
            //Context.save();
            displayResults("IPMU_" + D.getName(), 1, rhoStart == rhoEnd);
        }
    }

    public static void displayResults(String name, int d, boolean simple) {
        Function<Result, Double> a = (res) -> res.getMeanAccuracy();
        Function<Result, Double> rho = (res) -> res.rho * 10;
        Function<Result, Double> e = (res) -> res.getAverageCoverageSize();
        Function<Result, Double> f = (res) -> res.getSummedCapacityComplexity();
        Function<Result, Double> g = (res) -> res.getAverageRuleSetComplexity();
        Function<Result, Double> h = (res) -> res.getAverageRuleSetSize();
        Function<Result, Double> l = (res) -> res.getAverageRuleLength();
        Function<Result, List<Double>> m = (res) -> res.getAccumulativeRuleLength();
        if (simple) {
            ResultStack<Result> res = Context.<Result>getResultPack(name, d).getResult(new TupleImpl<>(0.));
            System.out.println("Average accuracy: "+Misc.round(res.getMeanValue(a),3));
            System.out.println("Average number of SUFs: "+Misc.round(res.getMeanValue(e),1));
            System.out.println("Average number of rules: "+Misc.round(res.getMeanValue(h),1));
            System.out.println("Average average rule length: "+Misc.round(res.getMeanValue(l),1));
            System.out.println("Average accumulative rule length (length:ratio_of_rules_at_most_size_L):");
        } else {
            System.out.println("Best tradeoffs depending on the value of rho:");
            System.out.println("Minimal rho, maximal accuracy");

            displayBestTradeoffs(name, d, a, true, rho, false);
            System.out.println();

            System.out.println("Minimal number of SUFs, maximal accuracy");
            displayBestTradeoffs(name, d, a, true, e, false);
            System.out.println();

            /*System.out.println("Capacity complexity");
            displayBestTradeoffs(name, d, a, true, f, false);
            System.out.println();

            System.out.println("Rule set complexity");
            displayBestTradeoffs(name, d, a, true, g, false);
            System.out.println();*/
            System.out.println("Minimal number of rules, maximal accuracy");
            displayBestTradeoffs(name, d, a, true, h, false);
            System.out.println();

            System.out.println("Minimal average rule length, minimal accuracy");
            displayBestTradeoffs(name, d, a, true, l, false);
            System.out.println();

            System.out.println("Average accumulative rule length (for the best accuracy) (length:ratio_of_rules_at most_size_L):");
        }
        List<Entry<Tuple<Double>, ResultStack<Result>>> list;

        list = new ArrayList<>(
                Context
                        .<Result>getResultPack(name, d)
                        .getParetoOptima(a, true, a, true));

        List<Double> accumulativeLengths
                = Collections.max(
                        list, ((x, y) -> {
                            return x.getValue().getMeanValue(a).compareTo(y.getValue().getMeanValue(a));
                        })).getValue().getMeanValueRow(m);

        for (int i = 0; i < accumulativeLengths.size(); i++) {
            System.out.print(i + ":" + Misc.round((1 - accumulativeLengths.get(i)),3)+" ");
        }
        System.out.println();

        /*for (Entry<Tuple<Double>, ResultStack<Result>> entry : Context.<Result>getResultPack(name, d).getParetoOptima(a, true, a, true)) {
            System.out.println("std (" + entry.getKey() + ") : " + entry.getValue().getStandardDeviation(a));
        }*/
    }

    public static void displayBestTradeoffs(String name, int d, Function<Result, Double> f, boolean b1, Function<Result, Double> g, boolean b2) {
        List<Entry<Tuple<Double>, ResultStack<Result>>> list;
        list = new ArrayList<>(
                Context
                        .<Result>getResultPack(name, d)
                        .getParetoOptima(f, b1, g, b2));
        Collections.sort(list, ((x, y) -> {
            return x.getValue().getMeanValue(g).compareTo(y.getValue().getMeanValue(g));
        }));
        list.stream().forEach(
                (x) -> {
                    System.out.print("(" + Misc.round(x.getValue().getMeanValue(g), 1) + "," + Misc.round(x.getValue().getMeanValue(f), 3) + ") ");
                }
        );
    }

    public static void testIPMU() throws IOException {
        List<InstanceList<Rank, Chain>> datasets = DataTools.VCDomLEMBenchmark();

        evaluateMaxSUFLearner(datasets, SSProcesses::PROCESS_1, 10);
    }

}
