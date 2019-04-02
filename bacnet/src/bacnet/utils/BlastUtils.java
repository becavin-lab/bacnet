package bacnet.utils;

import org.eclipse.e4.ui.workbench.modeling.EPartService;

import bacnet.Database;
import bacnet.views.InternalBrowser;

public class BlastUtils {

    public static void openBlastN(String sequence, EPartService partService) {
        String os = System.getProperty("os.name");
        String path = "";
        if (os.equals("Windows 10") || os.equals("Mac OS X")) {
            path = Database.getBLAST_PATH();
        } else {
            path = "/srv/www/Blast/";
        }
        String textHTML = FileUtils.readText(path + "BlastN.html");
        textHTML = textHTML.replace("__sequence__", sequence);
        // textHTML =
        // textHTML.replaceAll("./BlastN_files",Database.getBLAST_PATH()+"/BlastN_files");
        FileUtils.saveText(textHTML, path + "BlastN_temp.html");
        String url = "";
        if (os.equals("Windows 10") || os.equals("Mac OS X")) {
            url = "file://" + path + "BlastN_temp.html";
        } else {
            url = "http://listeriomics01.hosting.pasteur.fr/Blast/BlastN_temp.html";
        }
        InternalBrowser.openURL(url, "Blast Nucleotide Sequence", partService);

    }

    public static void openBlastP(String sequence, EPartService partService) {
        String os = System.getProperty("os.name");
        String path = "";
        if (os.equals("Windows 10") || os.equals("Mac OS X")) {
            path = Database.getBLAST_PATH();
        } else {
            path = "/srv/www/Blast/";
        }
        String textHTML = FileUtils.readText(path + "BlastP.html");
        textHTML = textHTML.replace("__sequence__", sequence);
        // textHTML =
        // textHTML.replaceAll("./BlastN_files",Database.getBLAST_PATH()+"/BlastN_files");
        FileUtils.saveText(textHTML, path + "BlastP_temp.html");
        String url = "";
        if (os.equals("Windows 10") || os.equals("Mac OS X")) {
            url = "file://" + path + "BlastP_temp.html";
        } else {
            url = "http://listeriomics01.hosting.pasteur.fr/Blast/BlastP_temp.html";
        }
        InternalBrowser.openURL(url, "Blast Protein Sequence", partService);
    }

}
