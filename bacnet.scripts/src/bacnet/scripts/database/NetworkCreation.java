package bacnet.scripts.database;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.Network;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.core.Expression;
import bacnet.utils.FileUtils;

public class NetworkCreation {

    /**
     * Create Database.getInstance().getCoExprNetworkArrayPath() with all co-expression network available <br>
     * Then for each genome available create a file with list of datasets available for co-expression network creation. this will be saved in
     * Database.getLISTDATA_COEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genomeName + ".txt"
     */
    public static String createCoExprTable(String logs) {
        ArrayList<String> coExprTable = new ArrayList<String>();
        coExprTable.add("Genome\tNetworkName\tNbData");
        for (String genomeName : Genome.getAvailableGenomes()) {
            TreeSet<String> list = new TreeSet<String>();
            for (BioCondition bioCond : BioCondition.getAllBioConditions()) {
                if (bioCond.getGenomeName().equals(genomeName)) {
                    if (bioCond.getTypeDataContained().contains(TypeData.Tiling)) {
                        list.add(bioCond.getName() + "[" + TypeData.Tiling + "]");
                    }
                    if (bioCond.getTypeDataContained().contains(TypeData.RNASeq)) {
                        String name = bioCond.getName();
                        if (name.contains("Long") || name.contains("Short") || name.contains("Medium")
                                || name.contains("BHI_2011_EGDe")) {
                            System.out.println("Not included");
                        } else {
                            list.add(bioCond.getName() + "[" + TypeData.RNASeq + "]");
                        }
                    }
                }
            }
            if (list.size() > 4) {
            	System.out.println("Found: "+list.size()+" datasets for network construction for "+ genomeName);
                String path_list = Database.getLISTDATA_COEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genomeName + ".txt";
                logs += "Load dataset list : " + path_list +"\n";
                TabDelimitedTableReader.saveTreeSet(list,path_list);
                String row = genomeName + "\t" + FileUtils.removeExtensionAndPath(path_list) + "\t" + list.size();
                coExprTable.add(row);
            } else {
                logs += "CoExpression Network will not be created for " + genomeName + " because not enough transcriptomes datasets are available\n";
            }
        }
        TabDelimitedTableReader.saveList(coExprTable, Database.getInstance().getCoExprNetworkArrayPath());
        return logs;
    }

    
    /**
     * Create Co-Expression network for all absolute expression dataset of a specific genome
     * @param networks
     * @param logs
     * @return
     */
    public static String createCoExpressionNetwork(ArrayList<String> networks, String logs) {
        for(String genomeName : networks) {
            logs += "Load expression matrix for " + genomeName;
            ExpressionMatrix expression =
                    ExpressionMatrix.loadTab(Expression.PATH_ALLDataType + "_" + genomeName + ".excel", false);
            expression = expression.getSubMatrixColumn(TabDelimitedTableReader
                    .readList(Database.getLISTDATA_COEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_" + genomeName + ".txt"));
            expression.save(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_Temp_" + genomeName);
            expression.saveTab(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_Temp_" + genomeName + ".excel",
                    "GenomeElements");
//    
            /*
             * Compute Network
             */
            System.out.println("Compute network");
            logs += "Compute network for " + genomeName;
            Network.getCoExpressionGlobalMatrix(Genome.loadGenome(genomeName));
            
            
            /*
             * Remove temporary file
             */
            File file = new File(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_Temp_" + genomeName);
            file.delete();
            file = new File(Database.getCOEXPR_NETWORK_TRANSCRIPTOMES_PATH() + "_Temp_" + genomeName + ".excel");
            file.delete();
            
            
            /*
             * Create circular genome figure for CircosPlot 
             */
            //@Deprecated
            //GenomesCreation.createCircularGenomeView(Genome.loadGenome(genomeName), genomeName);
        }
        return logs;
    }

}

