package bacnet.scripts.core.vennDiagram;

import java.util.ArrayList;
import java.util.TreeMap;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ListUtils;
import bacnet.utils.MathUtils;

/**
 * Class for creating sets necessary in Venn diagrams<br>
 * Providing a list of sets (through <code>ArrayList<String></code>) it will find intersections,
 * unions and differences between all sets.
 * 
 * @author Christophe Becavin
 *
 */
public class VennDiagram {

    private int nbSet = 0;
    private TreeMap<String, ArrayList<String>> sets = new TreeMap<String, ArrayList<String>>();
    private ArrayList<String> setNames = new ArrayList<String>();
    private ArrayList<String> subsetNames = new ArrayList<String>();
    private TreeMap<String, ArrayList<String>> subsets = new TreeMap<String, ArrayList<String>>();

    public VennDiagram() {}

    public VennDiagram(TreeMap<String, ArrayList<String>> sets) {
        this.sets = sets;
        nbSet = sets.size();
    }

    public void addSet(String name, ArrayList<String> set) {
        sets.put(name, set);
        setNames.add(name);
        nbSet = sets.size();
    }

    public void compute() {
        subsetNames = new ArrayList<String>();
        subsets = new TreeMap<String, ArrayList<String>>();
        for (int i = 0; i < nbSet; i++) {
            ArrayList<ArrayList<String>> subsets = combinator(setNames, i + 1);
            for (int l = 0; l < subsets.size(); l++) {
                ArrayList<String> subset = subsets.get(l);
                // calculate the intersection within all the elements of subset
                String name = "(" + subset.get(0);
                ArrayList<ArrayList<String>> setsIntersection = new ArrayList<ArrayList<String>>();
                setsIntersection.add(sets.get(subset.get(0)));
                for (int j = 1; j < subset.size(); j++) {
                    name += " n " + subset.get(j);
                    setsIntersection.add(sets.get(subset.get(j)));
                }
                name += ")";
                ArrayList<String> intersect = ListUtils.intersect(setsIntersection);

                // calculate the union of all the element not in subset
                ArrayList<ArrayList<String>> setsUnion = new ArrayList<ArrayList<String>>();
                ArrayList<String> relatComplement = ListUtils.relativeComplement(setNames, subset);
                if (relatComplement.size() != 0) {
                    name += " - (";
                    name += relatComplement.get(0);
                    setsUnion.add(sets.get(relatComplement.get(0)));
                    for (int j = 1; j < relatComplement.size(); j++) {
                        name += " u " + relatComplement.get(j);
                        setsUnion.add(sets.get(relatComplement.get(j)));
                    }
                    name += ")";
                    ArrayList<String> union = ListUtils.union(setsUnion);

                    // calculate the relative complement between inter \ union
                    ArrayList<String> finalSubset = ListUtils.relativeComplement(intersect, union);
                    this.subsets.put(name, finalSubset);
                    subsetNames.add(name);
                    name += " - size: " + finalSubset.size();
                    System.out.println(name);
                } else {
                    this.subsets.put(name, intersect);
                    subsetNames.add(name);
                    name += " - size: " + intersect.size();
                    System.out.println(name);
                }
            }
        }

        // perform union of all sets
        ArrayList<String> union = new ArrayList<String>();
        ArrayList<String> set = sets.get(sets.firstKey());
        for (String elt : set)
            union.add(elt);
        String name = "";
        for (String setName : sets.keySet()) {
            if (!setName.equals(sets.lastKey()))
                name += setName + " u ";
            else
                name += setName;
            union = ListUtils.union(union, sets.get(setName));
        }

        subsetNames.add(name);
        this.subsets.put(name, union);
        name += " - size: " + union.size();
        System.out.println(name);

        System.out.println("finish computing Venn diagram");
    }

    /**
     * Create all combinations of k elements of list1 <br>
     * 
     * @param list1
     * @param k number of elements of the subsets
     * @return list of subsets
     */
    public static ArrayList<ArrayList<String>> combinator(ArrayList<String> list1, int k) {
        ArrayList<ArrayList<String>> finalSubsets = new ArrayList<ArrayList<String>>();
        int[] kIndices = new int[k];
        CombinationGenerator x = new CombinationGenerator(list1.size(), k);
        while (x.hasNext()) {
            ArrayList<String> subset = new ArrayList<String>();
            kIndices = x.getNext();
            for (int i = 0; i < kIndices.length; i++) {
                subset.add(list1.get(kIndices[i]));
            }
            finalSubsets.add(subset);
        }

        System.err.println("Should have " + MathUtils.C_nk(list1.size(), k) + " subsets, nb of subsets found: "
                + finalSubsets.size());
        return finalSubsets;
    }

    public String[][] convertToVennMasterDataList() {
        ArrayList<String[]> vennMasterLists = new ArrayList<String[]>();
        ArrayList<String> total = computeTotalList();
        for (String element : total) {
            for (String setName : sets.keySet()) {
                if (sets.get(setName).contains(element)) {
                    String[] values = {element, setName};
                    vennMasterLists.add(values);
                }
            }
        }

        String[][] array = new String[vennMasterLists.size()][2];
        for (int i = 0; i < vennMasterLists.size(); i++) {
            array[i][0] = vennMasterLists.get(i)[0];
            array[i][1] = vennMasterLists.get(i)[1];
        }
        return array;
    }

    public ArrayList<String> computeTotalList() {
        ArrayList<String> totalList = new ArrayList<String>();
        for (String setName : sets.keySet()) {
            totalList = ListUtils.union(totalList, sets.get(setName));
        }

        return totalList;
    }

    public void save(String path) {
        for (int i = 0; i < subsets.size(); i++) {
            @SuppressWarnings("unlikely-arg-type")
            ArrayList<String> subset = subsets.get(i);
            TabDelimitedTableReader.saveList(subset, path + subsetNames.get(i) + ".txt");
        }
    }

    /*
     * ***************************************************** Getters and Setters
     * 
     * *****************************************************
     */

    public int getNbSet() {
        return nbSet;
    }

    public void setNbSet(int nbSet) {
        this.nbSet = nbSet;
    }

    public ArrayList<String> getSetNames() {
        return setNames;
    }

    public void setSetNames(ArrayList<String> setNames) {
        this.setNames = setNames;
    }

    public TreeMap<String, ArrayList<String>> getSets() {
        return sets;
    }

    public void setSets(TreeMap<String, ArrayList<String>> sets) {
        this.sets = sets;
    }

    public ArrayList<String> getSubsetNames() {
        return subsetNames;
    }

    public void setSubsetNames(ArrayList<String> subsetNames) {
        this.subsetNames = subsetNames;
    }

    public TreeMap<String, ArrayList<String>> getSubsets() {
        return subsets;
    }

    public void setSubsets(TreeMap<String, ArrayList<String>> subsets) {
        this.subsets = subsets;
    }

}
