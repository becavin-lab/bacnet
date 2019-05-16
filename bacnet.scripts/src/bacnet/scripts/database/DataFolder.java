package bacnet.scripts.database;

import java.io.File;
import java.util.ArrayList;
import bacnet.Database;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.utils.FileUtils;

public class DataFolder {

    public static void createMissingFolders() {

        System.out.println("Test if Database folders exists");
        ArrayList<String> folders = new ArrayList<>();
        folders.add(Database.getDATA_PATH());
        folders.add(Database.getBIOCONDITION_PATH());
        folders.add(Database.getTRANSCRIPTOMES_PATH());
        folders.add(Database.getPROTEOMES_PATH());
        folders.add(Database.getMULTIOMICS_PATH());
        folders.add(Database.getNETWORK_PATH());
        folders.add(Database.getBLAST_PATH());
        folders.add(Database.getGENOMES_PATH());
        folders.add(Database.getANNOTATIONDATA_PATH());
        folders.add(Database.getSIGNATURES_PATH());
        folders.add(OmicsData.PATH_STREAMING);
        for (String path : folders) {
            if (!FileUtils.exists(path)) {
                System.out.println("Create folder: " + path);
                File file = new File(path);
                file.mkdir();
            }
        }

        System.out.println("Test if folders for database creation exists");
        folders.clear();
        folders.add(OmicsData.PATH_RAW);
        folders.add(OmicsData.PATH_NORM);
        folders.add(OmicsData.PATH_COMPARISONS);
        folders.add(OmicsData.PATH_GENEXPR_RAW);
        folders.add(OmicsData.PATH_GENEXPR_COMPLETE);
        folders.add(OmicsData.PATH_GENEXPR_NORM);
        folders.add(OmicsData.PATH_TILING_RAW);
        folders.add(OmicsData.PATH_TILING_NORM);
        folders.add(OmicsData.PATH_NGS_RAW);
        folders.add(OmicsData.PATH_NGS_NORM);
        folders.add(OmicsData.PATH_EXPR_NORM);
        folders.add(OmicsData.PATH_EXPR_RAW);
        
        folders.add(OmicsData.PATH_PROTEOMICS_RAW);
        folders.add(OmicsData.PATH_PROTEOMICS_NORM);

        folders.add(GenomeNCBI.PATH_RAW);
        folders.add(GenomeNCBI.PATH_GENOMES);
        folders.add(GenomeNCBI.PATH_HOMOLOGS);
        folders.add(GenomeNCBI.PATH_ANNOTATION);
        folders.add(GenomeNCBI.PATH_TEMP);
        for (String path : folders) {
            if (!FileUtils.exists(path)) {
                System.out.println("Create folder: " + path);
                File file = new File(path);
                file.mkdir();
            }
        }

    }

}
