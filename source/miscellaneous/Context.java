/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miscellaneous;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This class is a singleton that contains informations and options about the
 * process that is running. This is also the class to use for saving results.
 *
 * @author qgbrabant
 */
public class Context {

    private final Map<String, String> options;
    private final List<String> argList;
    private final Map<String, ResultStack> results;

    private Context() {
        options = new HashMap<>();
        argList = new ArrayList<>();
        results = new HashMap<>();
    }

    private static class RSHolder {

        private final static Context INSTANCE = new Context();
    }

    private static Context getInstance() {
        return RSHolder.INSTANCE;
    }

    /**
     * Returns the value of an option. Null if the option wasn't specified.
     *
     * @param key the name of the option (including "-").
     * @return the value of the option.
     */
    public static String getOption(String key) {
        return getInstance().options.get(key.toLowerCase());
    }

    /**
     * Returns the value of an option. If the option, wasn't specified, the
     * default value is returned.
     *
     * @param key the name of the option (including "-").
     * @param defaultValue
     * @return the value of the option.
     */
    public static String getOption(String key, String defaultValue) {
        return getInstance().options.get(key.toLowerCase()) == null ? defaultValue : getInstance().options.get(key.toLowerCase());
    }

    /**
     * Parses the value of an option and returns it as a list of Doubles. The
     * String is split using "," as separator.
     *
     * @param optionName
     * @param defaultValue
     * @return
     */
    public static List<Double> getDoubleListOption(String optionName, List<Double> defaultValue) {
        String val = getInstance().options.get(optionName);
        if (val == null) {
            return defaultValue;
        } else {
            List<Double> res = new ArrayList<>();
            String[] split = val.split(",");
            for (String s : split) {
                res.add(Double.parseDouble(s));
            }
            return res;
        }
    }

    /**
     * Parses the value of an option and returns it as a list of Integers. The
     * String is split using "," as separator.
     *
     * @param optionName
     * @param defaultValue
     * @return
     */
    public static List<Integer> getIntegerListOption(String optionName, List<Integer> defaultValue) {
        String val = getInstance().options.get(optionName);
        if (val == null) {
            return defaultValue;
        } else {
            List<Integer> res = new ArrayList<>();
            String[] split = val.split(",");
            for (String s : split) {
                res.add(Integer.parseInt(s));
            }
            return res;
        }
    }

    /**
     * Specifies the value of an option.
     *
     * @param key option name
     * @param value
     */
    public static void setOption(String key, String value) {
        getInstance().options.put(key.toLowerCase(), value);
    }

    /**
     * Returns a reference to the list of arguments (this is not a copy of the
     * original list, so you can add and remove values, if you dare).
     *
     * @return
     */
    public static List<String> getArgList() {
        return getInstance().argList;
    }

    public static void addArgument(String arg) {
        getInstance().argList.add(arg.toLowerCase());
    }

    public static void addResult(String k, Result res) {
        ResultStack stack = getInstance().results.get(k);
        if (stack == null) {
            stack = new ResultStack();
            getInstance().results.put(k, stack);
        }
        stack.addValue(res);
    }

    public static ResultStack getResultStack(String k) {
        return getInstance().results.get(k);
    }

    public static void displayAllTradeoffs(Function<Result, Double> f, boolean b1, Function<Result, Double> g, boolean b2) {
        List<Map.Entry<String, ResultStack>> list;
        list = new ArrayList<>(
                getInstance().results.entrySet());
        Collections.sort(list, ((x, y) -> {
            return x.getValue().getMeanValue(g).compareTo(y.getValue().getMeanValue(g));
        }));
        list.stream().forEach(
                (x) -> {
                    System.out.print("(" + Misc.round(x.getValue().getMeanValue(g), 2) + "," + Misc.round(x.getValue().getMeanValue(f), 3) + ") ");
                }
        );
    }

    public static void display(List<Function<Result, Double>> funcs, String[] names, int[] precisions, boolean[] stds) {
        int maxlength = 15;
        for (int i = 0; i < names.length; i++) {
            if (names[i].length() > maxlength) {
                maxlength = names[i].length();
            }
        }

        List<ResultStack> list;
        list = new ArrayList<>(getInstance().results.values());

        Collections.sort(list, ((x, y) -> {
            return x.getMeanValue(funcs.get(0)).compareTo(y.getMeanValue(funcs.get(0)));
        }));

        for (int i = 0; i < funcs.size(); i++) {
            System.out.print(new String(new char[maxlength - names[i].length()]).replace("\0", " ") + names[i] + ": \t");
            for (ResultStack x : list) {
                System.out.print(Misc.round(x.getMeanValue(funcs.get(i)), precisions[i]) + "\t");
            }
            System.out.println("");
            if (stds[i]) {
                i++;
                System.out.print(new String(new char[maxlength - names[i].length()]).replace("\0", " ") + names[i] + ": \t");
                for (ResultStack x : list) {
                    System.out.print(Misc.round(x.getMeanValue(funcs.get(i)), precisions[i]) + "\t");
                }
                System.out.println("");
                System.out.print(new String(new char[maxlength - 15]).replace("\0", " ") + "(over runs) std:\t");
                for (ResultStack x : list) {
                    System.out.print(Misc.round(x.getStandardDeviation(funcs.get(i-1)), precisions[i-1]) + "\t");
                }
                System.out.println("");
            }
        }
    }

    public static Set<Map.Entry<String, ResultStack>> getParetoOptima(Function<Result, Double> eval1, boolean maximize1, Function<Result, Double> eval2, boolean maximize2) {
        Set<Map.Entry<String, ResultStack>> paretoz = new HashSet<>();
        Map.Entry<String, ResultStack> p;
        boolean maximal;
        double scoreA1;
        double scoreA2;
        double scoreB1;
        double scoreB2;

        Iterator<Map.Entry<String, ResultStack>> it;

        for (Map.Entry<String, ResultStack> candidate : getInstance().results.entrySet()) {
            maximal = true;

            scoreA1 = candidate.getValue().getMeanValue(eval1);
            if (!maximize1) {
                scoreA1 *= -1;
            }
            scoreA2 = candidate.getValue().getMeanValue(eval2);
            if (!maximize2) {
                scoreA2 *= -1;
            }

            it = paretoz.iterator();

            while (it.hasNext() && maximal) {
                p = it.next();

                scoreB1 = p.getValue().getMeanValue(eval1);
                if (!maximize1) {
                    scoreB1 *= -1;
                }
                scoreB2 = p.getValue().getMeanValue(eval2);
                if (!maximize2) {
                    scoreB2 *= -1;
                }

                if (!(scoreA1 == scoreB1 && scoreA2 == scoreB2)) {
                    if (scoreA1 >= scoreB1 && scoreA2 >= scoreB2) {
                        it.remove();
                    }
                }

                if (scoreA1 <= scoreB1 && scoreA2 <= scoreB2) {
                    maximal = false;
                }
            }
            if (maximal) {
                paretoz.add(candidate);
            }
        }

        return paretoz;
    }

    public static void startNewExperiment() {
        getInstance().results.clear();
    }
}
