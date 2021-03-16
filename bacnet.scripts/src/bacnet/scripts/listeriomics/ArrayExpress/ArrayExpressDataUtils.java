package bacnet.scripts.listeriomics.ArrayExpress;

import java.io.File;
import java.util.ArrayList;
import bacnet.Database;
import bacnet.reader.TabDelimitedTableReader;

public class ArrayExpressDataUtils {

    public static String PATH = Database.getInstance().getPath() + "ArrayExpress" + File.separator;

    /**
     * Read the SDRF table and search for the "Array Design REF" column
     * 
     * @param dataID
     * @return return first value in "Array Design REF" column, empty String otherwise
     */
    public static String getArrayDesignRef(String dataID) {
        return getColumn(getTableSDRF(dataID), "Array Design REF");
    }

    /**
     * Read the SDRF table and search for the corresponding column
     * 
     * @param dataID example: E-MTAB-1800
     * @param columnHeader name of the column
     * @return return first value in "Array Design REF" column, empty String otherwise
     */
    public static String getColumn(String[][] sdrf, String columnHeader) {
        for (int j = 0; j < sdrf[0].length; j++) {
            if (sdrf[0][j].equals(columnHeader)) {
                return sdrf[1][j];
            }
        }
        return "";

    }

    /**
     * Return column Index of the column named columnHeader
     * 
     * @param dataID
     * @param columnHeader
     * @return
     */
    public static int getColumnIndex(String[][] sdrf, String columnHeader) {
        for (int j = 0; j < sdrf[0].length; j++) {
            if (sdrf[0][j].equals(columnHeader)) {
                return j;
            }
        }

        /*
         * If not found, try to search if at least one column contain the element
         */
        for (int j = 0; j < sdrf[0].length; j++) {
            if (sdrf[0][j].contains(columnHeader)) {
                return j;
            }
            if (sdrf[0][j].contains(columnHeader.toLowerCase())) {
                return j;
            }
            if (sdrf[0][j].contains(columnHeader.toUpperCase())) {
                return j;
            }
        }
        return -1;

    }
    // public static String getPlatform(String dataID){
    //
    // }
    //
    // public static String getStrain(String dataID){
    //
    // }
    //
    // public static String getTitle(String dataID){
    //
    // }

    /**
     * Get path of IDF file for a specific dataID (ex: E-MTAB-1800)
     * 
     * @param dataID
     * @return
     */
    public static String getIDFPath(String dataID) {
        return PATH + dataID + File.separator + dataID + ".idf.txt";
    }

    /**
     * Get path of SDRF file for a specific dataID (ex: E-MTAB-1800)
     * 
     * @param dataID
     * @return
     */
    public static String getSDRFPath(String dataID) {
        return PATH + dataID + File.separator + dataID + ".sdrf.txt";
    }

    /**
     * Get path of curated SDRF file for a specific dataID (ex: E-MTAB-1800)
     * 
     * @param dataID
     * @return
     */
    public static String getSDRFCuratedPath(String dataID) {
        return PATH + dataID + File.separator + dataID + "-curated.sdrf.txt";
    }

    /**
     * Read DATA_TABLe and return the list of dataset IDs
     * 
     * @return
     */
    public static ArrayList<String> getListData() {
        String[][] array = TabDelimitedTableReader.read(ArrayExpressListeriomics.DATA_TABLE);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i < array.length; i++) {
            list.add(array[i][1]);
        }
        return list;
    }

    /**
     * Return table found in DATA_TABLE
     * 
     * @return
     */
    public static String[][] getTableSDRF(String dataID) {
        return TabDelimitedTableReader.read(getSDRFPath(dataID));
    }

}
