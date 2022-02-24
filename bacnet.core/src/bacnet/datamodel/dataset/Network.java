package bacnet.datamodel.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import bacnet.Database;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.utils.FileUtils;
import bacnet.utils.VectorUtils;

/**
 * Network object for co-expression network analysis and visualisation
 * @author christophebecavin
 *
 */
public class Network extends OmicsData {

    public static double CORR_CUTOFF = 0.85;
    public static String CIRCOS_BACK_PATH = Database.getANNOTATIONDATA_PATH() + "CircosBackground.svg";
    /**
     * 
     */
    private static final long serialVersionUID = 991824259100059499L;

    /**
     * List of vertices = genome elements + their attributes = genome position, type, annotation
     */
    private HashMap<String, String> vertices = new HashMap<>();

    /**
     * An hashMap which map each vertex (=genome element) to the correlated vertex.<br>
     * Vertex -> HashMap<Vetrex,PearsonCorrelation>
     */
    private HashMap<String, HashMap<String, Double>> edges = new HashMap<>();

    public Network() {

    }

    /**
     * Given a list of vertice and a cutoff, filter the network
     * 
     * @param verticesDisplay
     * @param cutoff
     * @return
     */
    public Network filterNetwork(ArrayList<String> verticesDisplay, double cutoff) {
        Network filteredNetwork = new Network();
        filteredNetwork.setVertices(this.getVertices());
        for (String vertice : verticesDisplay) {
            if (this.getEdges().containsKey(vertice)) {
                HashMap<String, Double> edgesTemp = this.getEdges().get(vertice);
                HashMap<String, Double> newEdges = new HashMap<>();
                for (String targetVertice : edgesTemp.keySet()) {
                    double correlation = edgesTemp.get(targetVertice);
                    if (Math.abs(correlation) > cutoff) {
                        newEdges.put(targetVertice, correlation);
                        /*
                         * Insert also other vertices linked to target edge
                         */
                        /*
                        if (this.getEdges().containsKey(targetVertice)) {
                            HashMap<String, Double> edgesTempTarget = this.getEdges().get(targetVertice);
                            HashMap<String, Double> newEdgesTarget = new HashMap<>();
                            for (String targetVerticeTarget : edgesTempTarget.keySet()) {
                                double correlationTarget = edgesTempTarget.get(targetVerticeTarget);
                                if (Math.abs(correlationTarget) > cutoff) {
                                    newEdgesTarget.put(targetVerticeTarget, correlationTarget);
                                }
                            }
                            filteredNetwork.getEdges().put(targetVertice, newEdgesTarget);
                        }*/
                    }
                }
                filteredNetwork.getEdges().put(vertice, newEdges);
            }
        }
        return filteredNetwork;
    }

    /**
     * Export to ArrayList to send to d3.js viewer
     * 
     * @return
     */
    public ArrayList<String> toArrayList() {
        ArrayList<String> networkList = new ArrayList<>();
        networkList.add("from\tweight\tto");
        for (String vertice : this.getEdges().keySet()) {
            HashMap<String, Double> edgesTemp = this.getEdges().get(vertice);
            for (String targetVertice : edgesTemp.keySet()) {
                String row = vertice + this.getVertices().get(vertice) + "\t" + edgesTemp.get(targetVertice) + "\t"
                        + targetVertice + this.getVertices().get(targetVertice);
                networkList.add(row);
            }
        }
        return networkList;
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("from\tweight\tto\n");
    	for (String vertice : this.getEdges().keySet()) {
            HashMap<String, Double> edgesTemp = this.getEdges().get(vertice);
            for (String targetVertice : edgesTemp.keySet()) {
            	sb.append(""+ vertice + this.getVertices().get(vertice)+ "\t"+edgesTemp.get(targetVertice) + "\t"
                        + targetVertice + this.getVertices().get(targetVertice)+"\n");
            }
        }
    	return sb.toString();
    }
            
       
    /**
     * Export to String[][] to download
     * 
     * @return
     */
    public String[][] toArray() {
    	String[][] networkList = new String[this.getEdges().keySet().size()][3];
    	networkList[0][0] = "from";
    	networkList[0][1] = "weigth";
    	networkList[0][2] = "to";

        for (String vertice : this.getEdges().keySet()) {
        	int i = 1;
            HashMap<String, Double> edgesTemp = this.getEdges().get(vertice);
            for (String targetVertice : edgesTemp.keySet()) {
            	networkList[i][0] = vertice + this.getVertices().get(vertice);
            	networkList[i][1] = edgesTemp.get(targetVertice).toString();
            	networkList[i][2] = targetVertice + this.getVertices().get(targetVertice);
            	i = i+1;
            }
        }
        return networkList;
    }

    /**
     * Save stat and values in two separate files <br>
     * Saving is done via DataStream to allow streaming reading
     * 
     * @throws IOException
     */
    public void save(String fileName) {
        // save stat data
        try {
            // Create the necessary output streams to save the scribble.
            FileOutputStream fos = new FileOutputStream(fileName);
            // Save to file
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            // Compressed
            ObjectOutputStream out = new ObjectOutputStream(gzos);
            // Save objects
            out.writeObject(this); // Write the entire Vector of scribbles
            out.flush(); // Always flush the output.
            out.close(); // And close the stream.
        }
        // Print out exceptions. We should really display them in a dialog...
        catch (IOException e) {
            System.out.println(e);
        }

    }

    /**
     * Load information of the data
     * 
     * @param fileName Path for the file "Info.data"
     * @throws IOException
     */
    public static Network load(String fileName) {
    	
    	TreeMap<String, Network> networks = Database.getInstance().getNetworks();
        Network network = new Network();
        if (networks.size() == 0) {
            try {
            	// Create necessary input streams
                FileInputStream fis = new FileInputStream(fileName); // Read from file
                GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
                ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
                // Read in an object. It should be a vector of scribbles
                network = (Network) in.readObject();
                in.close();
                //setName(network.getName());
                //setNote(network.getNote());
                //setType(network.getType());
                //setDate(network.getDate());
                //setBioCondName(network.getBioCondName());
                //setEdges(network.getEdges());
                //setVertices(network.getVertices());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Database.getInstance().getNetworks().put(fileName, network);
        } else {
            // System.out.println("Genome already loaded");
            network = networks.get(fileName);
            if (network == null) { // if this genome has not been loaded -> load it
            	 try {
                 	// Create necessary input streams
                     FileInputStream fis = new FileInputStream(fileName); // Read from file
                     GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
                     ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
                     // Read in an object. It should be a vector of scribbles
                     network = (Network) in.readObject();
                     in.close();
                     //setName(network.getName());
                     //setNote(network.getNote());
                     //setType(network.getType());
                     //setDate(network.getDate());
                     //setBioCondName(network.getBioCondName());
                     //setEdges(network.getEdges());
                     //setVertices(network.getVertices());
                 } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 Database.getInstance().getNetworks().put(fileName, network);
            }
        }
        return network;

    }

    /**
     * Calculate all pearson correlation and put it in a list of network interactions<br>
     * Remove correlation CORR_CUTOFF<br>
     * Nedd to load: Database.COEXPR_NETWORK_TRANSCRIPTOMES_PATH+"_Temp_"+genome.getSpecies()
     * 
     * @param genome
     */
    public static void getCoExpressionGlobalMatrix(Genome genome) {
        ArrayList<String> networkList = new ArrayList<>();
        ExpressionMatrix coExpressionMatrix = ExpressionMatrix
                .load(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_Temp_" + genome.getSpecies());
        ArrayList<String> genomeElements = coExpressionMatrix.getRowNamesToList();
        Network network = new Network();
        network.setName(FileUtils.removeExtensionAndPath(
                Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_Temp_" + genome.getSpecies()));
        HashMap<String, HashMap<String, Double>> edges = network.getEdges();
        HashMap<String, String> vertices = new HashMap<>();
        int count = 0;
        // for(int k=0;k<50;k++){
        for (int k = 0; k < genomeElements.size(); k++) {
            String gene1Name = genomeElements.get(k);
//            System.out.println(gene1Name);
            // if(gene1Name.contains("lmo0200")){
            // for(int j=k+1;j<50;j++){
            for (int j = 0; j < genomeElements.size(); j++) {
                if (j != k) {
                    String gene2Name = genomeElements.get(j);
                    double[] vector1 = coExpressionMatrix.getRow(gene1Name);
                    double[] vector2 = coExpressionMatrix.getRow(gene2Name);
                    double pearsonCorrelation = VectorUtils.pearsonCorrelation(vector1, vector2);
                    if (Math.abs(pearsonCorrelation) > CORR_CUTOFF) {
                        count++;
                        Sequence seq1 = genome.getElement(gene1Name);
                        Sequence seq2 = genome.getElement(gene2Name);
                        // geneA(type;pos) coeff geneB(type;pos)"
                        String type1 = seq1.getType().toString();
                        String product1 = "";
                        String product2 = "";
                        if (type1.equals("Gene")) {
                            if (seq1.isStrand())
                                type1 = "Gene+";
                            else
                                type1 = "Gene-";
                            Gene gene1 = (Gene) seq1;
                            product1 = gene1.getProduct();
                            if (!gene1.getGeneName().equals("")) {
                                product1 = gene1.getGeneName() + " - " + gene1.getProduct();
                            }
                        }
                        String type2 = seq2.getType().toString();
                        if (type2.equals("Gene")) {
                            if (seq2.isStrand())
                                type2 = "Gene+";
                            else
                                type2 = "Gene-";
                            Gene gene1 = (Gene) seq2;
                            product2 = gene1.getProduct();
                            if (!gene1.getGeneName().equals("")) {
                                product2 = gene1.getGeneName() + " - " + gene1.getProduct();
                            }
                        }
                        if (seq1 instanceof Srna) {
                            Srna sRNA = (Srna) seq1;
                            type1 = sRNA.getTypeSrna().toString();
                            product1 = sRNA.getRef();
                        }
                        if (seq2 instanceof Srna) {
                            Srna sRNA = (Srna) seq2;
                            type2 = sRNA.getTypeSrna().toString();
                            product2 = sRNA.getRef();
                        }

                        String row = gene1Name + "(" + type1 + ";" + seq1.getBegin() + "){" + product1 + "}" + "\t"
                                + pearsonCorrelation + "\t" + gene2Name + "(" + type2 + ";" + seq2.getBegin() + "){"
                                + product2 + "}";
                        if(count%1000==0) {
                        	System.out.println("Edge: "+count+ " - "+row);
                        }
                        networkList.add(row);
                        vertices.put(gene1Name, "(" + type1 + ";" + seq1.getBegin() + "){" + product1 + "}");
                        vertices.put(gene2Name, "(" + type2 + ";" + seq2.getBegin() + "){" + product2 + "}");
                        if (edges.containsKey(gene1Name)) {
                            HashMap<String, Double> edgesTemp = edges.get(gene1Name);
                            edgesTemp.put(gene2Name, pearsonCorrelation);
                        } else {
                            HashMap<String, Double> edgesTemp = new HashMap<String, Double>();
                            edgesTemp.put(gene2Name, pearsonCorrelation);
                            edges.put(gene1Name, edgesTemp);
                        }
                    }
                }
            }
            // }
        }
        network.setEdges(edges);
        network.setVertices(vertices);
        System.out.println("Found: " + count + " edges");
        //TabDelimitedTableReader.saveList(networkList,
        //        Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies() + ".txt");
        network.save(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());
//        ArrayList<String> result = new ArrayList<>();
//        Network networkNew = new Network();
//        networkNew.load(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genome.getSpecies());
//        for (String vecteur : networkNew.getVertices().keySet()) {
//            result.add(vecteur + "\t" + networkNew.getVertices().get(vecteur));
//            System.out.println(vecteur + "\t" + networkNew.getVertices().get(vecteur));
//        }
//        TabDelimitedTableReader.saveList(result, Database.getInstance().getPath() + "nodesNew.txt");
//        result.clear();
//        for (String node : networkNew.getEdges().keySet()) {
//            for (String node2 : networkNew.getEdges().get(node).keySet()) {
//                result.add(node + "\t" + networkNew.getVertices().get(node) + "\t"
//                        + networkNew.getEdges().get(node).get(node2) + "\t" + node2 + "\t"
//                        + networkNew.getVertices().get(node2));
//                System.out
//                        .println("Node: " + node + " - " + networkNew.getEdges().get(node).get(node2) + " - " + node2);
//            }
//        }
//        TabDelimitedTableReader.saveList(result, Database.getInstance().getPath() + "aretesNew.txt");

    }

    /**
     * An hashMap which map each vertex (=genome element) to the correlated vertex.<br>
     * Vertex -> HashMap<Vetrex,PearsonCorrelation>
     */
    public HashMap<String, HashMap<String, Double>> getEdges() {
        return edges;
    }

    /**
     * An hashMap which map each vertex (=genome element) to the correlated vertex.<br>
     * Vertex -> HashMap<Vetrex,PearsonCorrelation>
     */
    public void setEdges(HashMap<String, HashMap<String, Double>> edges) {
        this.edges = edges;
    }

    /**
     * List of vertices = genome elements + their attributes = genome position, type, annotation
     */
    public HashMap<String, String> getVertices() {
        return vertices;
    }

    /**
     * List of vertices = genome elements + their attributes = genome position, type, annotation
     */
    public void setVertices(HashMap<String, String> vertices) {
        this.vertices = vertices;
    }

}
