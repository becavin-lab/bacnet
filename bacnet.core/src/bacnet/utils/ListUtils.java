package bacnet.utils;

import java.util.ArrayList;
import java.util.TreeSet;

public class ListUtils {

    /**
     * Return a String version of the list
     * 
     * @param list
     * @param separator include between each element of the list
     * @return
     */
    public static String toString(ArrayList<String> list, String separator) {
        String row = "";
        for (String temp : list) {
            row += temp + separator;
        }
        return row;
    }

    /**
     * Return a String version of the TreeSet
     * 
     * @param set
     * @param separator include between each element of the list
     * @return
     */
    public static String toString(TreeSet<String> set, String separator) {
        String row = "";
        for (String temp : set) {
            row += temp + separator;
        }
        return row;
    }

    /**
     * Return the invert list of List1 inside the total universe
     * 
     * @param list
     * @param universe
     * @return
     */
    public static ArrayList<String> inverse(ArrayList<String> list, ArrayList<String> universe) {
        ArrayList<String> inverse = new ArrayList<>();
        for (String element : universe) {
            if (!list.contains(element)) {
                inverse.add(element);
            }
        }
        return inverse;
    }

    /**
     * Calculate union of two lists L1 and L2 <br>
     * 
     * @param list1
     * @param list2
     * @return L1 U L2
     */
    public static ArrayList<String> union(ArrayList<String> list1, ArrayList<String> list2) {
        TreeSet<String> setFinal = new TreeSet<String>();
        for (String element : list1)
            setFinal.add(element);
        for (String element : list2)
            setFinal.add(element);

        ArrayList<String> listFinal = new ArrayList<String>();
        for (String element : setFinal)
            listFinal.add(element);
        return listFinal;
    }

    /**
     * Calculate the union of a list of lists
     * 
     * @param lists
     * @return a list containing all the elements from the different lists
     */
    public static ArrayList<String> union(ArrayList<ArrayList<String>> lists) {
        ArrayList<String> listFinal = new ArrayList<String>();
        for (String element : lists.get(0))
            listFinal.add(element);
        for (int i = 1; i < lists.size(); i++) {
            listFinal = union(listFinal, lists.get(i));
        }
        return listFinal;
    }

    /**
     * Calculate intersection of two lists L1 and L2 <br>
     * 
     * @param list1
     * @param list2
     * @return L1 inter L2
     */
    public static ArrayList<String> intersect(ArrayList<String> list1, ArrayList<String> list2) {
        TreeSet<String> setFinal = new TreeSet<String>();
        for (String element : list1) {
            if (list2.contains(element)) {
                setFinal.add(element);
            }
        }

        ArrayList<String> listFinal = new ArrayList<String>();
        for (String element : setFinal)
            listFinal.add(element);
        return listFinal;
    }

    /**
     * Calculate the intersection of a list of lists
     * 
     * @param lists
     * @return a list containing all the elements contained in the different lists
     */
    public static ArrayList<String> intersect(ArrayList<ArrayList<String>> lists) {
        ArrayList<String> listFinal = new ArrayList<String>();
        for (String element : lists.get(0))
            listFinal.add(element);
        for (int i = 1; i < lists.size(); i++) {
            listFinal = intersect(listFinal, lists.get(i));
        }
        return listFinal;
    }

    /**
     * Calculate symmetrical difference of two lists L1 and L2 <br>
     * 
     * @param list1
     * @param list2
     * @return L1 Delta L2 = (L1 U L2) - (L1 inter L2)
     */
    public static ArrayList<String> symDifference(ArrayList<String> list1, ArrayList<String> list2) {
        ArrayList<String> listIntersect = intersect(list1, list2);
        TreeSet<String> setFinal = new TreeSet<String>();
        for (String element : list1) {
            if (!listIntersect.contains(element)) {
                setFinal.add(element);
            }
        }
        for (String element : list2) {
            if (!listIntersect.contains(element)) {
                setFinal.add(element);
            }
        }

        ArrayList<String> listFinal = new ArrayList<String>();
        for (String element : setFinal)
            listFinal.add(element);
        return listFinal;
    }

    /**
     * Calculate the relative complement of two lists L1 and L2 <br>
     * 
     * @param list1
     * @param list2
     * @return L1 \ L2 = L1 - (L1 inter L2)
     */
    public static ArrayList<String> relativeComplement(ArrayList<String> list1, ArrayList<String> list2) {
        TreeSet<String> setFinal = new TreeSet<String>();
        for (String element : list1) {
            if (!list2.contains(element)) {
                setFinal.add(element);
            }
        }

        ArrayList<String> listFinal = new ArrayList<String>();
        for (String element : setFinal)
            listFinal.add(element);
        return listFinal;
    }

    /**
     * From a list of String extract the longest common subsequence
     * 
     * @param list
     * @return
     */
    public static String longestCommonSub(ArrayList<String> list) {
        String lcs = "";
        if (list.size() == 0)
            return "";
        else if (list.size() == 1)
            return list.get(0);
        else if (list.size() == 2)
            return longestCommonSub(list.get(0), list.get(1));
        else {
            int i = 1;
            lcs = list.get(0);
            while (i < list.size()) {
                lcs = longestCommonSub(lcs, list.get(i));
                i++;
            }
            return lcs;
        }
    }

    /**
     * The longest common subsequence (or LCS) of groups A and B is the longest group of elements from A
     * and B that are common between the two groups and in the same order in each group.<br>
     * For example, the sequences "1234" and "1224533324" have an LCS of "1234"<br>
     * <code>Code extract from http://rosettacode.org/wiki/Rosetta_Code</code>
     * 
     * @param a
     * @param b
     * @return
     */
    public static String longestCommonSub(String a, String b) {
        int[][] lengths = new int[a.length() + 1][b.length() + 1];

        // row 0 and column 0 are initialized to 0 already

        for (int i = 0; i < a.length(); i++) {
            for (int j = 0; j < b.length(); j++) {
                if (a.charAt(i) == b.charAt(j)) {
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                } else {
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);
                }
            }
        }
        // read the substring out from the matrix
        StringBuffer sb = new StringBuffer();
        for (int x = a.length(), y = b.length(); x != 0 && y != 0;) {
            if (lengths[x][y] == lengths[x - 1][y])
                x--;
            else if (lengths[x][y] == lengths[x][y - 1])
                y--;
            else {
                assert a.charAt(x - 1) == b.charAt(y - 1);
                sb.append(a.charAt(x - 1));
                x--;
                y--;
            }
        }

        return sb.reverse().toString();
    }

    /**
     * From a list of String extract the sequence regrouping all String<br>
     * CAREFULL: All the String have to come from the same String. This method is designed to find this
     * "Reference String"
     * 
     * @param list
     * @return
     */
    public static String unionOFString(ArrayList<String> list) {
        String union = longestCommonSub(list);
        String lcs = union;
        for (String temp : list) {
            if (temp.contains(lcs)) {
                union = unionOFString(temp, union);
            }
        }

        /*
         * Verify
         */
        for (String temp : list) {
            if (!union.contains(temp)) {
                return "false";
            }
        }
        return union;
    }

    /**
     * Complete a <code>String</code>, by adding elements from another <code>String</code><br>
     * CAREFULL: Both <code>String</code> should share a common sequence
     * 
     * @param a
     * @param lcs
     * @return complete String
     */
    public static String unionOFString(String a, String b) {
        String lcs = longestCommonSub(a, b);
        if (a.equals(lcs))
            return b;
        if (b.equals(lcs))
            return a;

        String union = "";
        String first = a.replaceFirst(lcs, "");
        String second = b.replaceFirst(lcs, "");
        if (a.indexOf(lcs) == 0) {
            first = b.replaceFirst(lcs, "");
            second = a.replaceFirst(lcs, "");
        }

        union = first + lcs + second;
        return union;
    }

}
