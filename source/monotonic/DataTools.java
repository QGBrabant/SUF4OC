/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monotonic;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import chains.Chain;
import chains.ChainBuilder;
import chains.RealChainBuilder;
import chains.ShortChain;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import orders.OrderTools;

/**
 *
 * @author qgbrabant
 */
public class DataTools {

    
    public static List<InstanceList> VCDomLEMBenchmark(boolean flipScales) throws IOException {
        List<InstanceList> datasets = new ArrayList<>();
        String folder = "datasets";

        datasets.add(DataTools.extractDataset(
                folder + "/breast-cancer_nm.data", flipScales));
        datasets.get(datasets.size() - 1).setName("Breast cancer - c");

        datasets.add(DataTools.extractDataset(
                folder + "/breast-cancer-wisconsin.data", flipScales));
        datasets.get(datasets.size() - 1).setName("Breast cancer - wisconsin");

        datasets.add(DataTools.extractDataset(
                folder + "/car.data", flipScales));
        datasets.get(datasets.size() - 1).setName("Car");

        datasets.add(DataTools.extractDataset(
                folder + "/cpu4classes.data", flipScales));
        datasets.get(datasets.size() - 1).setName("CPU");

        datasets.add(DataTools.extractDataset(
                folder + "/bank-g.data", flipScales));
        datasets.get(datasets.size() - 1).setName("bank-g");

        datasets.add(DataTools.extractDataset(
                folder + "/fame.data", flipScales));
        datasets.get(datasets.size() - 1).setName("fame");

        datasets.add(DataTools.extractDataset(
                folder + "/denbosch.data", flipScales));
        datasets.get(datasets.size() - 1).setName("denbosch");

        datasets.add(DataTools.extractDataset(
                folder + "/ERA.data", flipScales));
        datasets.get(datasets.size() - 1).setName("ERA");

        datasets.add(DataTools.extractDataset(
                folder + "/ESL.data", flipScales));
        datasets.get(datasets.size() - 1).setName("ESL");

        datasets.add(DataTools.extractDataset(
                folder + "/LEV.data", flipScales));
        datasets.get(datasets.size() - 1).setName("LEV");

        datasets.add(DataTools.extractDataset(
                folder + "/SWD.data", flipScales));
        datasets.get(datasets.size() - 1).setName("SWD");

        datasets.add(DataTools.extractDataset(
                folder + "/windsor.data", flipScales));
        datasets.get(datasets.size() - 1).setName("windsor");

        return datasets;
    }

    public static List<InstanceList> calligraphyBenchmark(boolean flipScales) throws IOException {
        List<InstanceList> datasets = new ArrayList<>();
        String folder = "../calligraphy";

        datasets.add(DataTools.extractDataset(
                folder + "/error_calli_whole_V.txt", flipScales));
        datasets.get(datasets.size() - 1).setName("calligaphy errors, whole exercise (V)");

        datasets.add(DataTools.extractDataset(
                folder + "/error_calli_whole_R.txt", flipScales));
        datasets.get(datasets.size() - 1).setName("calligaphy errors, whole exercise (R)");

        datasets.add(DataTools.extractDataset(
                folder + "/error_calli_whole_L.txt", flipScales));
        datasets.get(datasets.size() - 1).setName("calligaphy errors, whole exercise (L)");

        return datasets;
    }

    public static void printArffFile(InstanceList dataset, String folder) throws IOException {
        printArffFile(dataset, folder, dataset.getName() + ".arff");
    }

    public static void printArffFile(InstanceList dataset, String folder, String fileName) throws IOException {
        File f = new File(folder + fileName);
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
        for (Instance inst : dataset) {
            for (int i = 0; i < dataset.getDomain().length; i++) {
                output1.write(inst.getFeature(i) + ",");
            }
            output1.write(inst.getLabel() + "\n");
        }
        output1.close();

    }

    public static InstanceList extractBreastCancerDataset(File f, boolean flipScales)
            throws FileNotFoundException, IOException {

        Chain codomain = new ShortChain(2);
        Chain c = new ShortChain(10);
        Chain[] domain = {c, c, c, c, c, c, c, c, c};

        HashMap<String, Integer> lexicon = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            if (flipScales) {
                lexicon.put("" + i, 10 - i);
            } else {
                lexicon.put("" + i, i - 1);
            }
        }

        InstanceList d = new InstanceList(domain, codomain);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            String line; //One line of the parsed file
            String[] tokens; //The line in splitted form
            int[] x = new int[9];
            boolean bullshit;
            while ((line = fileReader.readLine()) != null) {
                bullshit = false;
                tokens = line.split(",");
                for (int i = 1; i <= 9; i++) {
                    if (lexicon.get(tokens[i]) == null) {
                        bullshit = true;
                    } else {
                        x[i - 1] = lexicon.get(tokens[i]);
                    }
                }
                if (!bullshit) {
                    if (tokens[10].equals("2")) {
                        if (flipScales) {
                            d.add(new Instance(x, 1, domain, codomain));
                        } else {
                            d.add(new Instance(x, 0, domain, codomain));
                        }
                    } else {
                        if (flipScales) {
                            d.add(new Instance(x, 0, domain, codomain));
                        } else {
                            d.add(new Instance(x, 1, domain, codomain));
                        }
                    }
                }
            }
        } //One line of the parsed file

        return d;
    }

   
  

    public static InstanceList extractDataset(String f, boolean flipScales)
            throws FileNotFoundException, IOException {

        // Step 1: Parse the types of attributes 
        
        List<String> attributeTypes = new ArrayList<>();

        BufferedReader fileReader = new BufferedReader(new FileReader(f));

        String line = fileReader.readLine();
        while (!line.replaceAll(" ", "").toUpperCase().equals("ATTRIBUTES:")) {
            line = fileReader.readLine();
        }
        line = fileReader.readLine();
        while (!line.toUpperCase().replaceAll(" ", "").equals("DATA:")) {
            if (line.toUpperCase().replaceAll(" ", "").equals("NUMERICAL") || line.toUpperCase().replaceAll(" ", "").equals("NUMERICAL_REVERSED")) {
                attributeTypes.add(line.toUpperCase().replaceAll(" ", ""));
            } else {
                attributeTypes.add(line);
            }
            line = fileReader.readLine();
        }
        line = fileReader.readLine();

        int n = attributeTypes.size() - 1;
        
        // Step 2: Create the chains that form the domain and the codomain

        ChainBuilder[] builders = new ChainBuilder[n + 1];

        for (int i = 0; i < n + 1; i++) {
            if (attributeTypes.get(i).equals("NUMERICAL")) {
                builders[i] = new RealChainBuilder(!flipScales);
            } else if (attributeTypes.get(i).equals("NUMERICAL_REVERSED")) {
                builders[i] = new RealChainBuilder(flipScales);
            }
        }

        String[] tokens;

        while (line != null) {
            tokens = line.split(",");
            for (int i = 0; i < n + 1; i++) {
                if (attributeTypes.get(i).equals("NUMERICAL") || attributeTypes.get(i).equals("NUMERICAL_REVERSED")) {
                    builders[i].add(parseDouble(tokens[i]));
                }
                
            }
            line = fileReader.readLine();
        }
        ChainBuilder.Lexicon[] lexs = new ChainBuilder.Lexicon[n + 1];
        Map<String, Integer>[] maps = new HashMap[n + 1];
        Chain[] domain = new Chain[n];
        String type;
        for (int i = 0; i < n; i++) {
            type = attributeTypes.get(i);
            if (type.equals("NUMERICAL") || type.equals("NUMERICAL_REVERSED")) {
                lexs[i] = builders[i].buildChain();
                domain[i] = lexs[i].getChain();
            } else {
                maps[i] = new HashMap<>();
                type = type.replaceAll(" |\\[|]", "");
                tokens = type.split(",");

                domain[i] = new ShortChain(tokens);
                for (int k = 0; k < tokens.length; k++) {
                    maps[i].put(tokens[k], flipScales ?  k : tokens.length - 1 - k);
                }
            }
        }

        Chain codomain;
        type = attributeTypes.get(n);
        if (type.equals("NUMERICAL") || type.equals("NUMERICAL_REVERSED")) {
            lexs[n] = builders[n].buildChain();
            codomain = lexs[n].getChain();
        } else {
            maps[n] = new HashMap<>();
            type = type.replaceAll(" |\\[|]", "");
            tokens = type.split(",");
            codomain = new ShortChain(tokens);
            for (int k = 0; k < tokens.length; k++) {
                maps[n].put(tokens[k], flipScales ? k : tokens.length - 1 - k);
            }
        }
        
        // Step 3 : extract rows
        
        InstanceList res = new InstanceList(domain, codomain);

        fileReader = new BufferedReader(new FileReader(f));
        int[] x = new int[n];

        line = fileReader.readLine();
        while (!line.equals("DATA:")) {
            line = fileReader.readLine();
        }
        line = fileReader.readLine();
        int y;
        while (line != null) {
            tokens = line.split(",");
            for (int i = 0; i < n; i++) {
                type = attributeTypes.get(i);

                if (type.equals("NUMERICAL") || type.equals("NUMERICAL_REVERSED")) {
                    x[i] = lexs[i].get(parseDouble(tokens[i]));
                } else {
                    x[i] = maps[i].get(tokens[i]);
                }
            }

            if (attributeTypes.get(n).equals("NUMERICAL") || attributeTypes.get(n).equals("NUMERICAL_REVERSED")) {
                y = lexs[n].get(parseDouble(tokens[n]));
            } else {
                y = maps[n].get(tokens[n]);
            }
            res.add(new Instance(x, y, domain, codomain));

            line = fileReader.readLine();
        }

        return res;
    }

    public static double MAE(Classifier model, InstanceList dataset) {
        int sum = 0;
        for (Instance row : dataset) {
            sum += Math.abs(row.getLabel() - model.apply(row.getFeatures()));
        }
        return ((double) sum) / dataset.size();
    }

    public static double MER(Classifier model, InstanceList dataset) {
        int sum = 0;
        for (Instance row : dataset) {
            if (row.getLabel() != model.apply(row.getFeatures())) {
                sum += 1;
            }
        }
        return ((double) sum) / dataset.size();
    }

    /////////////////////////////
    //                         //
    // DATASET META-INDICATORS //
    //                         //
    /////////////////////////////
    public static int countAntiMonotonicPairs(InstanceList dataset, Set<Integer> forbidden) {
        int res = 0;
        int n = dataset.getDomain().length;
        List<Instance> list = new ArrayList<>(dataset);
        Instance row;
        Instance row2;
        Chain L = dataset.getCodomain();
        boolean antimonotonic;
        Integer r;
        for (int i = 0; i < list.size(); i++) {
            row = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                row2 = list.get(j);
                r = OrderTools.relation(row.getLabel(), row2.getLabel());
                if (r != 0) {
                    antimonotonic = true;
                    for (int k = 0; k < n && antimonotonic; k++) {
                        if (!forbidden.contains(k)) {
                            if (OrderTools.relation(row.getFeature(k), row2.getFeature(k)) == r) {
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

    public static int countAntiMonotonicPairs(InstanceList dataset) {
        return countAntiMonotonicPairs(dataset, new HashSet<>());
    }

    public static int countAntiMonotonicPairs(InstanceList dataset, int forbidden) {
        HashSet<Integer> s = new HashSet<>();
        s.add(forbidden);
        return countAntiMonotonicPairs(dataset, s);
    }

    /**
     * Generate a random monotonic dataset with m rows
     *
     * @param m
     * @param domain
     * @param codomain
     * @return
     */
    public static InstanceList randomMonotonicDataset(int m, Chain[] domain, Chain codomain) {
        InstanceList dataset = new InstanceList(domain, codomain);

        int[] premisses = new int[domain.length];
        for (int i = 0; i < domain.length; i++) {
            premisses[i] = domain[i].size() - 1;
        }
        dataset.add(new Instance(premisses, codomain.size() - 1, domain, codomain));
        for (int i = 0; i < domain.length; i++) {
            premisses[i] = 0;
        }
        dataset.add(new Instance(premisses, 0, domain, codomain));

        int j = 0;
        Integer r;
        boolean add;
        while (j < m) {
            premisses = new int[domain.length];
            for (int i = 0; i < domain.length; i++) {
                premisses[i] = domain[i].randomValue();
            }

            add = true;
            //Initialize min and max
            int min = 0;
            int max = codomain.size() - 1;
            //If the tuple does not satisfy the premisses of one rule, deacrease the max
            for (Instance row : dataset) {
                //compute the relation between the input and the candidate
                r = 0;
                for (int i = 0; i < premisses.length; i++) {
                    if (row.getFeature(i) < premisses[i]) {
                        if (r == 1) {
                            r = null;
                            break;
                        } else {
                            r = -1;
                        }
                    } else if (row.getFeature(i) > premisses[i]) {
                        if (r == -1) {
                            r = null;
                            break;
                        } else {
                            r = 1;
                        }
                    }
                }
                if (r != null) {
                    if (r == 0) {
                        add = false;
                        break;
                    }
                    if (r > 0) {
                        max = Math.min(max, row.getLabel());
                    }
                    if (r < 0) {
                        min = Math.max(min, row.getLabel());
                    }
                }
            }
            if (add) {
                dataset.add(new Instance(premisses, ThreadLocalRandom.current().nextInt(min, max + 1), domain, codomain));
                j++;
            }
        }

        return dataset;
    }
}
