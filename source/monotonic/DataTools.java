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
import static java.lang.Double.parseDouble;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import chains.ChainBuilder;
import chains.ChainBuilder;
import chains.Chain;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import orders.OrderTools;

/**
 *
 * @author qgbrabant
 */
public class DataTools {

    public static List<InstanceList> datasetsFromFolder(String dir) throws IOException {
        List<InstanceList> datasets = new ArrayList<>();
        File d = new File(dir);
        assert d.isDirectory() : d.toPath();
        for (File file : d.listFiles()) {
            System.out.println(file.toPath());
            if (file.isFile()) {
                datasets.add(DataTools.extractDataset(file.getPath()));
                datasets.get(datasets.size() - 1).setName(file.getName());
            }
        }
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

    public static InstanceList extractDataset(String f) throws FileNotFoundException, IOException {

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
            if (attributeTypes.get(i).equals("NUMERICAL") || attributeTypes.get(i).equals("NUMERICAL_REVERSED")) {
                builders[i] = new ChainBuilder();
            }
        }
        String[] tokens;

        while (line != null) {
            tokens = line.split(",");
            for (int i = 0; i < n + 1; i++) {
                if (attributeTypes.get(i).equals("NUMERICAL") || attributeTypes.get(i).equals("NUMERICAL_REVERSED")) {
                    builders[i].add(tokens[i]);
                }
            }
            line = fileReader.readLine();
        }

        Chain[] domain = new Chain[n];
        String type;
        for (int i = 0; i < n; i++) {
            type = attributeTypes.get(i);
            switch (type) {
                case "NUMERICAL":
                    domain[i] = builders[i].buildChain(true);
                    break;
                case "NUMERICAL_REVERSED":
                    domain[i] = builders[i].buildChain(false);
                    break;
                default:
                    type = type.replaceAll(" |\\[|]", "");
                    tokens = type.split(",");
                    domain[i] = new Chain(tokens, ">=");
                    break;
            }
        }

        Chain codomain;
        type = attributeTypes.get(n);
        switch (type) {
            case "NUMERICAL":
                codomain = builders[n].buildChain(true);
                break;
            case "NUMERICAL_REVERSED":
                codomain = builders[n].buildChain(false);
                break;
            default:
                type = type.replaceAll(" |\\[|]", "");
                tokens = type.split(",");
                codomain = new Chain(tokens, ">=");
                break;
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
                x[i] = domain[i].getByName(tokens[i]);
                assert tokens[i].equals(domain[i].getName(x[i])) : tokens[i] + " / " + domain[i].getName(x[i]);
            }
            y = codomain.getByName(tokens[n]);

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
