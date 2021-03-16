package bacnet.datamodel.annotation;

import bacnet.Database;

/**
 * All data and methods for Subcellular compartment information are done here
 * 
 * @author UIBC
 *
 */
public class SubCellCompartment {

    public enum TypeCompartment {
        aCM, CW, CP, EM, CM, iCM, CS, PC
    }

    public static String[] COMPARTMENT_NAMES = {"anchored to CM", "Cell wall", "Cytoplasm", "Extracellular milieu",
            "Cytoplasmic Membrane", "integral to CM", "Cell surface", "Protein complex"};

    public static String LOCALIZATION_PATH = Database.getANNOTATIONDATA_PATH() + "RenierPredictionPlosOne2012.txt";
    public static String LOCALIZATION_LEGEND_PATH =
            Database.getANNOTATIONDATA_PATH() + "RenierPredictionPlosOne2012Legend.txt";

}
