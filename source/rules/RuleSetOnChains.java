/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rules;

import aggregation.AggregationFunction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import miscellaneous.Tuple;
import lattices.Chain;
import orders.impls.MaxSet;
import lattices.impls.Rank;
import lattices.impls.ShortChain;
import ordinalclassification.InstanceList;
import ordinalclassification.OCTable;

/**
 *
 * @author qgbrabant
 */
public class RuleSetOnChains extends MaxSet<Instance<Rank,Chain>> implements AggregationFunction<Rank>{

    private Chain[] domain;
    private Chain codomain;

    public RuleSetOnChains(Chain[] domain, Chain codomain) {
        super(SRTools.order);
        this.domain = domain;
        this.codomain = codomain;
    }
    
    public RuleSetOnChains(InstanceList<Rank,Chain> D){
        super(SRTools.order);
        this.domain = D.getDomain();
        this.codomain = D.getCodomain();
        this.addAll(D);
    }

    public RuleSetOnChains(File f) throws IOException {
        super(SRTools.order);

        BufferedReader fileReader = new BufferedReader(new FileReader(f));

        List<Map<String, Rank>> dictionnaries = new ArrayList<>();
        List<Chain> chainList = new ArrayList<>();
        Map<String, Integer> attributeNames = new HashMap<>();

        //Read until "[ATTRIBUTES]"
        String line = fileReader.readLine();
        while (!(line.equals("[ATTRIBUTES]"))) {
            line = fileReader.readLine();
        }
        line = fileReader.readLine();

        //Read each attribute line
        //The decision attribute must be in the end
        String[] valueNames;
        Map<String, Rank> dict;
        Chain scale;
        int attributeIndex = 0;
        while (line.length() > 0 && line.charAt(0) == '+') {
            System.out.println(line);
            //Cut with : to serperate the name from the rest, keep the name
            attributeNames.put(line.split(":")[0].substring(2), attributeIndex);
            line = line.split(":")[1];
            //assume that we have a chain like [1,2,3..], then "gain"
            //ignore nominal attributes
            if (!line.contains("(nominal)")) {

                //take the [ ] part
                line = line.split(" \\[")[1];
                line = line.split("]")[0];
                //split with ","
                valueNames = line.split(", ");
                //create chain of the right length
                scale = new ShortChain(valueNames.length);
                chainList.add(scale);
                //map each string value from the splitted [ ] part to an element of the chain (in increasing order)
                dict = new HashMap<>();
                dictionnaries.add(dict);
                for (int i = 0; i < valueNames.length; i++) {
                    dict.put(valueNames[i], scale.get(i));
                }

                attributeIndex++;
            }
            line = fileReader.readLine();
        }

        //Read until "decision:"
        while (!line.contains("decision:")) {
            line = fileReader.readLine();
        }
        String targetName = line.split(": ")[1];
        //Put the decision attribute in this.codomain
        this.codomain = chainList.get(chainList.size() - 1);
        //Put the other in this.domain
        this.domain = new Chain[chainList.size() - 1];
        for (int i = 0; i < this.domain.length; i++) {
            this.domain[i] = chainList.get(i);
        }

        //Read #Certain at least rules
        while (!line.contains("#Certain at least rules")) {
            line = fileReader.readLine();
        }
        line = fileReader.readLine();
        //Until "#Certain at most rules", do:
        String[] rightLeft;
        String[] conditions;
        Rank[] premisses;
        Rank conclusion;
        while (!line.contains("#Certain at most rules")) {
            System.out.println(line);
            //remove number "i: " and the " |..." in the end
            line = line.split(" \\|")[0].split(": ")[1];
            System.out.println(line);
            //split from ") => ("
            rightLeft = line.split(" => ");

            //Do stuff
            conditions = rightLeft[0].split(" & ");
            premisses = new Rank[this.domain.length];
            for (int i = 0; i < this.domain.length; i++) {
                premisses[i] = this.domain[i].getBottom();
            }
            for (String c : conditions) {
                c = c.substring(1, c.length() - 1);
                attributeIndex = attributeNames.get(c.split(" >= ")[0]);
                premisses[attributeIndex] = dictionnaries.get(attributeIndex).get(c.split(" >= ")[1]);
            }
            
            String c = rightLeft[1].substring(1, rightLeft[1].length() - 1).split(" >= ")[1];
            System.out.println(c);
            conclusion = dictionnaries.get(this.domain.length).get(c);
            assert conclusion != null;
            //add a new rule 
            System.out.println(new LinearInstance(premisses, conclusion, this.domain, this.codomain));
            this.add(new LinearInstance(premisses, conclusion, this.domain, this.codomain));

            line = fileReader.readLine();
        }
        
        System.out.println(this);

        fileReader.close();
    }

    /**
     * Constructor that infer a minimal set of rules from the dataset parameter,
     * which is assumed to be composed of monotonic examples.
     *
     * @param domain
     * @param codomain
     * @param dataset
     */
    public RuleSetOnChains(Chain[] domain, Chain codomain, OCTable<Rank,Chain> dataset) {
        this(domain, codomain);
        
        Tuple<Rank> prem;
        Rank[] premTab;
        Rank conc;
        LinearInstance r;
        
        for (Instance<Rank,Chain> row : dataset) {
            prem = row.getFeatures();
            premTab = prem.toArray();
            conc = row.getLabel();
            r = new LinearInstance(premTab, conc, this.domain, this.codomain);
            this.add(r);
        }
    }
    
    public Chain[] getDomain(){
        return this.domain;
    }
    
    public Chain getDomainSlice(int i) {
        return this.domain[i];
    }

    public Chain getCoDomain() {
        return this.codomain;
    }

    

    @Override
    public int getArity() {
        return this.domain.length;
    }

    @Override
    public Rank apply(List<Rank> params) {
        Rank res = this.codomain.getBottom();
        for (Instance<Rank,Chain> ltr : this) {
            if (SRTools.satisfies(ltr,params)) {
                res = codomain.join(res, ltr.getLabel());
            }
        }
        return res;
    }
    
    public Rank apply(Rank[] params) {
        Rank res = this.codomain.getBottom();
        for (Instance<Rank,Chain> ltr : this) {
            if (SRTools.satisfies(ltr,params)) {
                res = codomain.join(res, ltr.getLabel());
            }
        }
        return res;
    }

}
