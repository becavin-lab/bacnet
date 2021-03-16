package bacnet.scripts.arrayexpress;

import java.io.File;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import bacnet.reader.TabDelimitedTableReader;

/**
 * List of methods to download all ArrayExpress needed for Listeriomics website
 * 
 * @author UIBC
 *
 */
public class ArrayExpressDataImport {

    public static String FTP_Path = "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/";

    /**
     * Download every related experiment from ArrayExpress
     */
    public static void run() {
        // get the URL of our idf to parse
        try {

            // urlPath = "http://www.ebi.ac.uk/arrayexpress/files/";
            String[][] expDesign = TabDelimitedTableReader.read(ArrayExpress.DATA_TABLE);
            for (int i = 1; i < expDesign.length; i++) {
                String accession = expDesign[i][1];
                File pathFile = new File(ArrayExpress.ARRAYEXPRESS_PATH + accession + File.separator);
                if (!pathFile.exists()) {
                    System.out.println("Create folder: " + pathFile.getAbsolutePath());
                    pathFile.mkdir();
                    downloadIDF(accession);
                    downloadSDRF(accession);
                    downloadProcessedFile(accession);
                    downloadRawFile(accession);
                    downloadAdditional(accession);
                    unzipFile(accession);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    /**
     * Unzip processed files
     * 
     * @param accession
     */
    public static void unzipFile(String accession) {
        String fileName = ArrayExpress.ARRAYEXPRESS_PATH + accession + File.separator + accession + ".processed.1.zip";
        bacnet.utils.FileUtils.unZIP(fileName);
    }

    public static void downloadIDF(String accession) {
        downloadFile(accession, ".idf.txt");
    }

    public static void downloadSDRF(String accession) {
        downloadFile(accession, ".sdrf.txt");
    }

    public static void downloadRawFile(String accession) {
        downloadFile(accession, ".raw.1.zip");
    }

    public static void downloadProcessedFile(String accession) {
        downloadFile(accession, ".processed.1.zip");
    }

    public static void downloadAdditional(String accession) {
        downloadFile(accession, ".additional.1.zip");
    }

    /**
     * Given an accession and an extension type:
     * <li>check if the file does not already exist in the current folder
     * <li>Download it if not
     * <li>Print out a message if no file has been downloaded
     * 
     * @param accession
     * @param extension
     */
    public static void downloadFile(String accession, String extension) {
        String id = accession.split("-")[1];
        String fileName = ArrayExpress.ARRAYEXPRESS_PATH + accession + File.separator + accession + extension;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                // System.out.println("Already exist: "+fileName);
            } else {
                URL idfURL = new URL(FTP_Path + "/" + id + "/" + accession + "/" + accession + extension);
                System.out.println("Download: " + idfURL + " to " + fileName);
                FileUtils.copyURLToFile(idfURL, new File(fileName));
            }
        } catch (Exception e) {
            System.out.println(accession + extension);
        }
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File missing: " + fileName);
        }
    }

}
