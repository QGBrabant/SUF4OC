/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Double.parseDouble;
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.deepToString;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import miscellaneous.Misc;
import lattices.Chain;
import orders.Order;
import lattices.impls.ChainBuilder;
import orders.impls.MaxSet;
import lattices.impls.RangeChainBuilder;
import lattices.impls.Rank;
import lattices.impls.RealChainBuilder;
import lattices.impls.ShortChain;
import ordinalclassification.InstanceList;

/**
 *
 * @author qgbrabant
 */
public class DataTools {
    
    /*public static List<InstanceList<Rank, Chain>> invertedSlowinskiBenchmark() throws IOException {
        List<InstanceList<Rank, Chain>> datasets =  new ArrayList<>();
        for ( InstanceList<Rank,Chain> d : VCDomLEMBenchmark()){
            datasets.add(inverseDataset(d));
        }
        return datasets;
    }*/
    
    public static List<InstanceList<Rank, Chain>> VCDomLEMBenchmark() throws IOException {
        List<InstanceList<Rank, Chain>> datasets = new ArrayList<>();
        String folder = "/home/qgbrabant/MiscProg/data/monotonic/slowinski";
        
        datasets.add(DataTools.extractStandardDataset(
                folder+"/breast-cancer_nm.data"));
        datasets.get(datasets.size() - 1).setName("Breast cancer - c");

        datasets.add(DataTools.extractBreastCancerDataset(
                new File(folder+"/breast-cancer-wisconsin.data")));
        datasets.get(datasets.size() - 1).setName("Breast cancer - wisconsin");

        datasets.add(DataTools.extractCarDataset(
                new File(folder+"/car.data")));
        datasets.get(datasets.size() - 1).setName("Car");

        datasets.add(DataTools.extractDataset(
                folder+"/cpu4classes.data", ","));
        datasets.get(datasets.size() - 1).setName("CPU");

        datasets.add(DataTools.extractDataset(
               folder+"/bank-g.data", ","));
        datasets.get(datasets.size() - 1).setName("bank-g");

        datasets.add(DataTools.extractDataset(
                folder+"/fame.data", ","));
        datasets.get(datasets.size() - 1).setName("fame");

        datasets.add(DataTools.extractStandardDataset(
                folder+"/denbosch.data"));
        datasets.get(datasets.size() - 1).setName("denbosch");

        datasets.add(DataTools.extractStandardDataset(
                folder+"/ERA.data"));
        datasets.get(datasets.size() - 1).setName("ERA");

        datasets.add(DataTools.extractStandardDataset(
                folder+"/ESL.data"));
        datasets.get(datasets.size() - 1).setName("ESL");

        datasets.add(DataTools.extractStandardDataset(
                folder+"/LEV.data"));
        datasets.get(datasets.size() - 1).setName("LEV");

        datasets.add(DataTools.extractStandardDataset(
                folder+"/SWD.data"));
        datasets.get(datasets.size() - 1).setName("SWD");

        datasets.add(DataTools.extractStandardDataset(
                folder+"/windsor.data"));
        datasets.get(datasets.size() - 1).setName("windsor");

        return datasets;
    }
    
    public static void printArffFile(InstanceList<Rank,Chain> dataset, String folder) throws IOException{
        printArffFile(dataset, folder, dataset.getName()+".arff");
    }
    
    public static void printArffFile(InstanceList<Rank,Chain> dataset, String folder, String fileName) throws IOException{
        File f = new File(folder+fileName);
        f.createNewFile();
        BufferedWriter output1 = new BufferedWriter(new FileWriter(f));
        output1.write("@relation " + dataset.getName() + "\n \n");
        int x = 1;
        for (Chain c : dataset.getDomain()) {
            output1.write("@attribute x" + x + " {");
            for (int i = 0; i < c.size() - 1; i++) {
                output1.write(i + ",");
            }
            output1.write((c.size() - 1) + "}\n");
            x++;
        }
        output1.write("@attribute out1 {");
        for (int i = 0; i < dataset.getCodomain().size() - 1; i++) {
            output1.write(i + ",");
        }
        output1.write((dataset.getCodomain().size() - 1) + "}\n");

        output1.write("\n\n@data\n");
        for (Instance<Rank, Chain> inst : dataset) {
            for (int i = 0; i < dataset.getDomain().length; i++) {
                output1.write(inst.getFeature(i).toInt() + ",");
            }
            output1.write(inst.getLabel().toInt() + "\n");
        }
        output1.close();
        
    }

    public static InstanceList<Rank, Chain> extractBreastCancerDataset(File f)
            throws FileNotFoundException, IOException {

        Chain codomain = new ShortChain(2);
        Chain c = new ShortChain(10);
        Chain[] domain = {c, c, c, c, c, c, c, c, c};

        HashMap<String, Rank> lexicon = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            lexicon.put("" + i, c.get(i - 1));
        }

        InstanceList<Rank, Chain> d = new InstanceList<>(domain, codomain);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            String line; //One line of the parsed file
            String[] tokens; //The line in splitted form
            Rank[] x = new Rank[9];
            boolean bullshit;
            while ((line = fileReader.readLine()) != null) {
                bullshit = false;
                tokens = line.split(",");
                for (int i = 1; i <= 9; i++) {
                    x[i - 1] = lexicon.get(tokens[i]);
                    if (lexicon.get(tokens[i]) == null) {
                        bullshit = true;
                    }
                }
                if (!bullshit) {
                    if (tokens[10].equals("2")) {
                        d.add(new LinearInstance(x, codomain.get(0),domain,codomain));
                    } else {
                        d.add(new LinearInstance(x, codomain.get(1),domain,codomain));
                    }
                }
            }
        } //One line of the parsed file

        
        
        return d;
    }

    public static InstanceList<Rank, Chain> extractTripadvisorDataset(File f)
            throws FileNotFoundException, IOException {

        Chain c = new ShortChain(5);
        Chain[] domain = {c, c, c, c, c, c, c};

        HashMap<String, Rank> lexicon = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            lexicon.put("" + i, c.get(i - 1));
        }

        InstanceList<Rank, Chain> d = new InstanceList<>(domain, c);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            String line; //One line of the parsed file
            String[] tokens; //The line in splitted form
            Rank[] x = new Rank[7];
            boolean bullshit;
            while ((line = fileReader.readLine()) != null) {
                bullshit = false;
                tokens = line.split(",");
                for (int i = 0; i < 7; i++) {
                    x[i] = lexicon.get(tokens[i]);
                    if (x[i] == null) {
                        bullshit = true;
                    }
                }
                if (!bullshit) {
                    if (!tokens[7].equals("-1")) {
                        d.add(new LinearInstance(x, lexicon.get(tokens[7]),domain,c));
                    }
                }
            }
        } //One line of the parsed file //One line of the parsed file
        
        return d;
    }

    public static InstanceList<Rank, Chain> extractCarDataset(File f)
            throws FileNotFoundException, IOException {

        Chain[] domain = {new ShortChain(4),
            new ShortChain(4),
            new ShortChain(4),
            new ShortChain(3),
            new ShortChain(3),
            new ShortChain(3)};

        Chain codomain = new ShortChain(4);

        HashMap<String, Integer> lex1 = new HashMap<>();
        lex1.put("vhigh", 0);
        lex1.put("high", 1);
        lex1.put("med", 2);
        lex1.put("low", 3);

        HashMap<String, Integer> lex2 = new HashMap<>();
        lex2.put("2", 0);
        lex2.put("3", 1);
        lex2.put("4", 2);
        lex2.put("5more", 3);

        HashMap<String, Integer> lex3 = new HashMap<>();
        lex3.put("2", 0);
        lex3.put("4", 1);
        lex3.put("more", 2);

        HashMap<String, Integer> lex4 = new HashMap<>();
        lex4.put("small", 0);
        lex4.put("med", 1);
        lex4.put("big", 2);

        HashMap<String, Integer> lex5 = new HashMap<>();
        lex5.put("low", 0);
        lex5.put("med", 1);
        lex5.put("high", 2);

        HashMap<String, Integer> lex6 = new HashMap<>();
        lex6.put("unacc", 0);
        lex6.put("acc", 1);
        lex6.put("good", 2);
        lex6.put("vgood", 3);

        List<HashMap<String, Integer>> lexicons = new ArrayList<>();
        lexicons.add(lex1);
        lexicons.add(lex1);
        lexicons.add(lex2);
        lexicons.add(lex3);
        lexicons.add(lex4);
        lexicons.add(lex5);
        lexicons.add(lex6);

        InstanceList<Rank, Chain> d = new InstanceList<>(domain, codomain);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            String line; //One line of the parsed file
            String[] tokens; //The line in splitted form
            Rank[] x = new Rank[6];

            while ((line = fileReader.readLine()) != null) {

                tokens = line.split(",");
                for (int i = 0; i < 6; i++) {
                    x[i] = domain[i].get(lexicons.get(i).get(tokens[i]));
                }
                d.add(new LinearInstance(x, codomain.get(lexicons.get(6).get(tokens[6])),domain,codomain));
            }
        }

        return d;
    }

    public static InstanceList<Rank, Chain> extractCPUDataset(File f)
            throws FileNotFoundException, IOException {

        ChainBuilder[] builders = new ChainBuilder[7];

        builders[0] = new RealChainBuilder(false);
        for (int i = 1; i <= 6; i++) {
            builders[i] = new RealChainBuilder();
        }

        List<Double> thresholds = new ArrayList<>();
        thresholds.add(21.);
        thresholds.add(101.);
        thresholds.add(201.);
        thresholds.add(301.);
        thresholds.add(401.);
        thresholds.add(501.);
        thresholds.add(601.);

        ChainBuilder classesBuiler = new RangeChainBuilder(thresholds);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            String line; //One line of the parsed file
            String[] tokens; //The line in splitted form

            while ((line = fileReader.readLine()) != null) {

                tokens = line.split(",");
                for (int i = 0; i <= 6; i++) {
                    builders[i].add(parseDouble(tokens[i + 2]));
                }
            }
        }

        ChainBuilder.Lexicon[] lexs = new ChainBuilder.Lexicon[7];

        for (int i = 0; i <= 6; i++) {
            lexs[i] = builders[i].buildChain();
        }

        Chain[] domain = new Chain[6];
        for (int i = 0; i < 6; i++) {
            domain[i] = lexs[i].getChain();
        }
        Chain codomain = lexs[6].getChain();
        ChainBuilder.Lexicon classLex = classesBuiler.buildChain();

        InstanceList<Rank, Chain> res = new InstanceList<>(domain, codomain);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            String line; //One line of the parsed file
            String[] tokens; //The line in splitted form
            Rank[] x = new Rank[6];

            while ((line = fileReader.readLine()) != null) {

                tokens = line.split(",");
                for (int i = 0; i < 6; i++) {
                    x[i] = lexs[i].get(parseDouble(tokens[i + 2]));
                }
                res.add(new LinearInstance(x, lexs[6].get(parseDouble(tokens[8])),domain,codomain));
                res.putInClassFilter(lexs[6].get(parseDouble(tokens[8])),
                        classLex.get(parseDouble(tokens[8])));
            }
        }
        
        return res;
    }


    public static void tripAdvisorSample(File f) throws FileNotFoundException, IOException {

        File translation = new File("/home/qgbrabant/MiscProg/NetBeansProjects/DiscreteMerge/data/supersugeno/tripAdvisorSample.data");
        try (PrintWriter writer = new PrintWriter(new FileWriter(translation));BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            String line; //One line of the parsed file
            String[] tokens; //The line in splitted form
            Rank[] x = new Rank[7];
            boolean bullshit;
            while ((line = fileReader.readLine()) != null) {
                bullshit = false;
                tokens = line.split(",");
                for (int i = 0; i < 8; i++) {
                    if (!(tokens[i].equals("1") || tokens[i].equals("2") || tokens[i].equals("3") || tokens[i].equals("4") || tokens[i].equals("5"))) {
                        bullshit = true;
                    }
                }
                if (!bullshit && random() < 0.01) {
                    writer.append(line + "\n");
                }
            }
        }
    }

    public static InstanceList<Rank, Chain> extractStandardDataset(String f)
            throws FileNotFoundException, IOException {
        return extractStandardDataset(f, ",", false);
    }

    public static InstanceList<Rank, Chain> extractStandardDataset(String f, String separator, boolean hasID)
            throws FileNotFoundException, IOException {

        //Count the number of values per line, and create as many ChainBuilders as necessary
        BufferedReader fileReader = new BufferedReader(new FileReader(f));
        int n = fileReader.readLine().split(",").length - 1;
        
        ChainBuilder[] builders = new ChainBuilder[n + 1];

        for (int i = 0; i < n + 1; i++) {
            builders[i] = new RealChainBuilder();
        }

        //read the file once to fill the chain builders with values
        fileReader = new BufferedReader(new FileReader(f));

        String line; //One line of the parsed file
        String[] tokens; //The line in splitted form

        while ((line = fileReader.readLine()) != null) {

            tokens = line.split(separator);
            for (int i = 0; i < n + 1; i++) {
                builders[i].add(parseDouble(tokens[i]));
            }
        }

        ChainBuilder.Lexicon[] lexs = new ChainBuilder.Lexicon[n + 1];

        for (int i = 0; i < n + 1; i++) {
            lexs[i] = builders[i].buildChain();
        }

        Chain[] domain = new Chain[hasID ? n-1 : n];
        for (int i = hasID ? 1 : 0 ; i < n; i++) {
            domain[hasID ? i - 1 : i] = lexs[i].getChain();
        }
        Chain codomain = lexs[n].getChain();

        InstanceList<Rank, Chain> res = new InstanceList<>(domain, codomain);

        fileReader = new BufferedReader(new FileReader(f));
        Rank[] x = new Rank[domain.length];

        while ((line = fileReader.readLine()) != null) {
            tokens = line.split(separator);
            for (int i = hasID ? 1 : 0; i < n; i++) {
                x[hasID ? i - 1 : i] = lexs[i].get(parseDouble(tokens[i]));
            }
            res.add(new LinearInstance(x, lexs[n].get(parseDouble(tokens[n])),domain,codomain));
        }

        return res;
    }

    public static InstanceList<Rank, Chain> extractDataset(String f, String separator)
            throws FileNotFoundException, IOException {

        String[] token;

        //Count the number of values per line, and create as many ChainBuilders as necessary
        BufferedReader fileReader = new BufferedReader(new FileReader(f));
        token = fileReader.readLine().split(",");
        int n = token.length - 1;

        ChainBuilder[] builders = new ChainBuilder[n + 1];

        for (int i = 0; i < n + 1; i++) {
            if (token[i].equals("cost")) {
                builders[i] = new RealChainBuilder(false);
            } else {
                builders[i] = new RealChainBuilder();
            }
        }

        //read the file once to fill the chain builders with values
        fileReader = new BufferedReader(new FileReader(f));

        String line; //One line of the parsed file
        String[] tokens; //The line in splitted form

        line = fileReader.readLine();
        if (line.contains("gain") || line.contains("cost")) {
            line = fileReader.readLine();
        }
        while (line != null) {
            //System.out.println(line);
            tokens = line.split(separator);
            for (int i = 0; i < n + 1; i++) {
                builders[i].add(parseDouble(tokens[i]));
            }

            line = fileReader.readLine();
        }

        ChainBuilder.Lexicon[] lexs = new ChainBuilder.Lexicon[n + 1];

        for (int i = 0; i < n + 1; i++) {
            lexs[i] = builders[i].buildChain();
        }

        Chain[] domain = new Chain[n];
        for (int i = 0; i < n; i++) {
            domain[i] = lexs[i].getChain();
        }
        Chain codomain = lexs[n].getChain();

        InstanceList<Rank, Chain> res = new InstanceList<>(domain, codomain);

        fileReader = new BufferedReader(new FileReader(f));
        Rank[] x = new Rank[n];

        line = fileReader.readLine();
        if (line.contains("gain") || line.contains("cost")) {
            line = fileReader.readLine();
        }
        while (line != null) {
            tokens = line.split(separator);
            for (int i = 0; i < n; i++) {
                x[i] = lexs[i].get(parseDouble(tokens[i]));
            }
            res.add(new LinearInstance(x, lexs[n].get(parseDouble(tokens[n])),domain,codomain));

            line = fileReader.readLine();
        }

        return res;
    }

    public static double testAccuracy(Collection<SugenoUtility> model, InstanceList<Rank, Chain> dataset, boolean useClassFilter) {
        int goodOnes = 0;
        Rank output;
        Rank y;
        for (Instance<Rank, Chain> row : dataset) {
            output = dataset.getCodomain().getBottom();
            for (SugenoUtility su : model) {
                output = dataset.getCodomain().join(output, su.getOutput(row.getFeatures()));
            }

            y = row.getLabel();

            if (useClassFilter) {
                /*if(useClassFilter){
                    System.out.println(y+" -filter-> "+dataset.getClass(y));
                }*/
                y = dataset.getClass(y);
                output = dataset.getClass(output);

            }
            if (output.equals(y)) {
                goodOnes++;
            }
        }
        return ((double) goodOnes) / dataset.size();
    }

    public static Set<Instance<Rank,Chain>> superSugenoToRules(Collection<SugenoUtility> SUs) {
        MaxSet<Instance<Rank,Chain>> rules = new MaxSet<>(new Order.VanillaImpl());
        for (SugenoUtility su : SUs) {
            rules.addAll(su.toRules());
        }
        return rules;
    }

    /////////////////////////////
    //                         //
    // DATASET META-INDICATORS //
    //                         //
    /////////////////////////////
    public static double[][][] thresholdCorrelations(InstanceList<Rank, Chain> dataset) {
        double[][] meansX = new double[dataset.getDomain().length][];
        double[] meansY = new double[dataset.getCodomain().size()];

        double[][][] correlations = new double[dataset.getDomain().length][][];
        for (int i = 0; i < dataset.getDomain().length; i++) {
            meansX[i] = new double[dataset.getDomain()[i].size()];
            correlations[i] = new double[dataset.getDomain()[i].size()][dataset.getCodomain().size()];
        }

        int heightX;
        int heightY;

        for (Instance<Rank, Chain> row : dataset) {
            for (int i = 0; i < meansX.length; i++) {
                heightX = row.getFeature(i).toInt();
                for (int j = 0; j <= heightX; j++) {
                    meansX[i][j]++;
                }
            }
            heightY = row.getLabel().toInt();
            for (int j = 0; j <= heightY; j++) {
                meansY[j]++;
            }
        }
        for (int i = 0; i < meansX.length; i++) {
            for (int j = 0; j < meansX[i].length; j++) {
                meansX[i][j] /= dataset.size();
            }
        }
        for (int j = 0; j < meansY.length; j++) {
            meansY[j] /= dataset.size();
        }

        double x;
        double y;

        for (Instance<Rank, Chain> row : dataset) {
            for (int i = 0; i < meansX.length; i++) {

                heightX = row.getFeature(i).toInt();
                heightY = row.getLabel().toInt();

                for (int j = 0; j < meansX[i].length; j++) {
                    for (int k = 0; k < meansY.length; k++) {

                        x = (j >= heightX) ? 1 : 0;
                        y = (k >= heightY) ? 1 : 0;

                        correlations[i][j][k] += (x - meansX[i][j]) * (y - meansY[k]);

                    }
                }
            }
        }
        for (int i = 0; i < meansX.length; i++) {
            for (int j = 0; j < meansX[i].length; j++) {
                for (int k = 0; k < meansY.length; k++) {
                    correlations[i][j][k] /= dataset.size();
                }
            }
        }

        for (int k = 0; k < meansY.length; k++) {
            for (int i = 0; i < meansX.length; i++) {
                for (int j = 0; j < meansX[i].length; j++) {

                    System.out.print(Misc.round(correlations[i][j][k], 2) + ", ");
                }
                System.out.println("");
            }
            System.out.println("");
            System.out.println("");
        }

        return correlations;
    }

    public static double[] classCount(InstanceList<Rank, Chain> dataset) {
        int height;
        double[] res = new double[dataset.getCodomain().size()];
        for (Instance<Rank, Chain> row : dataset) {
            height = row.getLabel().toInt();
            for (int i = 0; i <= height; i++) {
                res[i]++;
            }
        }
        return res;
    }

    public static double[][][] thresholdConfidence(InstanceList<Rank, Chain> dataset) {
        double[][] countX = new double[dataset.getDomain().length][];
        double[][][] confidences = new double[dataset.getDomain().length][][];

        for (int i = 0; i < dataset.getDomain().length; i++) {
            countX[i] = new double[dataset.getDomain()[i].size()];
            confidences[i] = new double[dataset.getDomain()[i].size()][dataset.getCodomain().size()];
        }

        int heightX;
        int heightY;

        for (Instance<Rank, Chain> row : dataset) {

            for (int i = 0; i < countX.length; i++) {
                heightX = row.getFeature(i).toInt();

                for (int j = 0; j <= heightX; j++) {
                    countX[i][j]++;
                }
            }
        }

        double x;
        double y;

        for (Instance<Rank, Chain> row : dataset) {
            heightY = row.getLabel().toInt();
            for (int i = 0; i < countX.length; i++) {

                heightX = row.getFeature(i).toInt();

                for (int j = 0; j <= heightX; j++) {
                    for (int k = 0; k <= heightY; k++) {
                        confidences[i][j][k]++;
                    }
                }
            }
        }
        for (int i = 0; i < countX.length; i++) {
            for (int j = 0; j < countX[i].length; j++) {
                for (int k = 0; k < dataset.getCodomain().size(); k++) {
                    confidences[i][j][k] /= countX[i][j];
                }
            }
        }

        return confidences;
    }

    public static int countAntiMonotonicPairs(InstanceList<Rank, Chain> dataset, Set<Integer> forbidden) {
        int res = 0;
        int n = dataset.getDomain().length;
        List<Instance> list = new ArrayList<>(dataset);
        Instance<Rank, Chain> row;
        Instance<Rank, Chain> row2;
        Chain L = dataset.getCodomain();
        boolean antimonotonic;
        Integer r;
        for (int i = 0; i < list.size(); i++) {
            row = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                row2 = list.get(j);
                r = L.relation(row.getLabel(), row2.getLabel());
                if (r != 0) {
                    antimonotonic = true;
                    for (int k = 0; k < n && antimonotonic; k++) {
                        if (!forbidden.contains(k)) {
                            if (dataset.getDomain()[k].relation(row.getFeature(k), row2.getFeature(k)) == r) {
                                antimonotonic = false;
                            }
                        }
                    }
                    if (antimonotonic) {
                        res++;
                    }
                }
            }
        }
        return res;
    }

    public static int countAntiMonotonicPairs(InstanceList<Rank, Chain> dataset) {
        return countAntiMonotonicPairs(dataset, new HashSet<>());
    }
    
    public static int countAntiMonotonicPairs(InstanceList<Rank, Chain> dataset, int forbidden) {
        HashSet<Integer> s = new HashSet<>();
        s.add(forbidden);
        return countAntiMonotonicPairs(dataset, s);
    }
    
    /*public static InstanceList<Rank, Chain> inverseDataset(InstanceList<Rank, Chain> d) {
        Chain[] newDom = Arrays.copyOf(d.getDomain(), d.getDomain().length);
        for (int i = 0; i < d.getDomain().length; i++) {
            newDom[i] = d.getDomain()[i].inverse();
        }
        InstanceList<Rank, Chain> res = new InstanceList<>(newDom, d.getCodomain().inverse());
        Instance<Rank,Chain> newInst;
        Rank[] premisses = new Rank[d.getDomain().length];
        for (Instance<Rank, Chain> inst : d) {
            for (int i = 0; i < d.getDomain().length; i++) {
                premisses[i] = newDom[i].get(d.getDomain()[i].size() - 1 - inst.getFeature(i).toInt());
            }
            newInst = new LinearInstance(premisses, res.getCodomain().get(res.getCodomain().size() - 1 - inst.getLabel().toInt()), newDom, res.getCodomain());
            res.add(newInst);
            System.out.println("--");
            System.out.println(inst);
            System.out.println(newInst);
        }
        
        res.setName(d.getName()+" (inverted)");
        return res;
    }*/
}
