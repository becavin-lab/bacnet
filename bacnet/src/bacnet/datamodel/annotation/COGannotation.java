package bacnet.datamodel.annotation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;

public class COGannotation {

    public static String COGMAP = Database.getInstance().getPath() + "Analysis/Egd-e Annotation/COG_info.txt";

    /**
     * Return the COG description corresponding to a specific COGid
     * 
     * @param cogID
     * @return
     */
    public static String getCOGDescription(String cogID) {
        try {
            TreeMap<String, String> cogMap = getCOGMap();
            if (cogID.length() == 1) {
                String cogDescription = cogMap.get(cogID);
                return cogDescription;
            } else {
                String cogDescription = "";
                for (int i = 0; i < cogID.length(); i++) {
                    cogDescription += cogMap.get(cogID.charAt(i) + "") + "; ";
                }
                return cogDescription;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Return a map from COG ID to COG description
     * 
     * @return
     * @throws IOException
     */
    public static TreeMap<String, String> getCOGMap() throws IOException {
        TreeMap<String, String> cogMap = new TreeMap<String, String>();
        String fileToOpen = COGMAP;
        File file = new File(fileToOpen);
        if (file.exists()) {
            String[][] table = TabDelimitedTableReader.read(file);
            // ArrayUtils.displayMatrix("jh", table);
            for (int i = 1; i < table.length; i++) {
                cogMap.put(table[i][0], table[i][1]);
            }
            System.out.println("COG annotation read");
            // System.out.println("Egde sRNA list read");
            return cogMap;
        }
        return cogMap;
    }

    /**
     * Return a map between cogID and the number of genes associated to this cog in EGD-e
     * 
     * @return
     * @throws IOException
     */
    public static TreeMap<String, Integer> getCOGNumberEGDe() {
        TreeMap<String, Integer> cogNumber = new TreeMap<String, Integer>();
        String fileToOpen = COGMAP;
        File file = new File(fileToOpen);
        if (file.exists()) {
            String[][] table = TabDelimitedTableReader.read(file);
            // ArrayUtils.displayMatrix("jh", table);
            for (int i = 1; i < table.length; i++) {
                cogNumber.put(table[i][0], Integer.parseInt(table[i][2]));
            }
        }
        return cogNumber;
    }

    /**
     * Return a map between cogID and the number of genes associated to this cog in EGD-e
     * 
     * @return
     * @throws IOException
     */
    public static void createCOGNumberEGDe() {
        String fileToOpen = COGMAP;
        String[][] table = TabDelimitedTableReader.read(fileToOpen);
        String[] column = new String[table.length];
        column[0] = "Number";
        Genome genome = Genome.loadEgdeGenome();
        for (int i = 1; i < table.length; i++) {
            String cogName = table[i][1];
            System.out.println(cogName);
            int count = 0;
            for (Gene gene : genome.getChromosomes().get(0).getGenes().values()) {
                System.out.println(gene.getCog());
                if (cogName.equals(gene.getCog())) {
                    count++;
                }
            }
            column[i] = count + "";

        }

        table = ArrayUtils.addColumn(table, column);
        TabDelimitedTableReader.save(table, COGMAP);

    }

    /**
     * Transform the result of getCogClassification() method into an ExpressionMatrix which can be load
     * by PieChartView for display
     * 
     * @param classification
     * @return
     */
    public static ExpressionMatrix getPieChartData(HashMap<String, ArrayList<String>> classification) {
        ExpressionMatrix dataset = new ExpressionMatrix("COGType", classification.size());
        int totalSize = 0;
        for (String cog : classification.keySet()) {
            totalSize += classification.get(cog).size();
        }
        int i = 0;
        for (String cog : classification.keySet()) {
            dataset.getRowNames().put(cog, i);
            double ratio = (double) classification.get(cog).size() / (double) totalSize;
            System.out.println(ratio * 100);
            dataset.setValue(ratio * 100, i, 0);
            i++;
        }
        return dataset;
    }

    public static HashMap<String, ArrayList<String>> getCogClassification(ExpressionMatrix matrix, GenomeNCBI genome) {
        // TODO Auto-generated method stub
        return null;
    }

}
