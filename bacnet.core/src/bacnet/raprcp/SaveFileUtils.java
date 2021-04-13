package bacnet.raprcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.swt.widgets.Shell;
import bacnet.utils.FileUtils;
import bacnet.utils.HTMLUtils;
import bacnet.views.InternalBrowser;

/**
 * Utils methods for managinf files. <br>
 * SOME PARTS NEED TO BE COMMENTED FOR RAP OR RCP TO WORK
 * 
 * @author christophebecavin
 *
 */
public class SaveFileUtils {

    /**
     * Save a text file
     * 
     * @param fileName name of the file you want to give
     * @param textToSave text you want to save
     * @param viewFile true if one want to vizualize file in a browser
     * @param title of the browser opened
     * @param textToDisplay text to display on the browser
     * @param partService
     * @param shell
     */
    public static void saveTextFile(String fileName, String textToSave, boolean viewFile, String title,
            String textToDisplay, EPartService partService, Shell shell) {

        /*
         * IF eclipse.rap
         */
        String url = DownloadServiceHandler.getDownloadUrl(fileName, textToSave, partService);
        textToDisplay = "<a href=\"" + url + "\">Click here to download " + fileName
                + "</a><br><br><b>File preview</b><hr>" + textToDisplay;
        if (viewFile) {
            InternalBrowser.openText(textToDisplay, title, partService);
        } else {
            NavigationManagement.openURLInExternalBrowser(url, partService);
        }

        /*
         * If eclipse.rcp
         */
        // FileDialog fd = new FileDialog(shell, SWT.SAVE);
        // fd.setText("Save "+fileName+" to: ");
        // fd.setFileName(fileName);
        // String extension = FileUtils.getExtension(fileName);
        // String[] filterExt = {"*"+extension,"*.*" };
        // fd.setFilterExtensions(filterExt);
        // String fileNameSave = fd.open();
        // try {
        // String text = "";
        // FileUtils.saveText(text, fileNameSave);
        // } catch (Exception ex) {
        // System.out.println("Cannot save the image");
        // }

    }

    /**
     * Same as saveTextFile but with no Preview
     * 
     * @param fileName
     * @param textToSave
     * @param title
     * @param partService
     * @param shell
     */
    public static void saveFile(String fileName, File fileToSave, String title, EPartService partService, Shell shell) {
        /*
         * IF eclipse.rap
         */
        String url = DownloadServiceHandler.getDownloadUrl(fileName, fileToSave, partService);
        String textToDisplay = "<a href=\"" + url + "\">Click here to download " + fileName
                + "</a><br><br><b>No file preview available</b><hr>";
        InternalBrowser.openText(textToDisplay, title, partService);

        /*
         * If eclipse.rcp
         */
        // FileDialog fd = new FileDialog(shell, SWT.SAVE);
        // fd.setText("Save "+fileName+" to: ");
        // fd.setFileName(fileName);
        // String extension = FileUtils.getExtension(fileName);
        // String[] filterExt = {"*"+extension,"*.*" };
        // fd.setFilterExtensions(filterExt);
        // String fileNameSave = fd.open();
        // try {
        // FileUtils.copy(fileToSave.getAbsolutePath(), fileNameSave);
        // } catch (Exception ex) {
        // System.out.println("Cannot save the image");
        // }

    }

    /**
     * Replace "_FileName" by fileName in the htmlFile found in bacnet/html/ folder
     * 
     * @param fileName to replace in the html file; htmlText.replaceFirst("_FileName",
     *        fileNameResource);
     * @param htmlFile to read in bacnet/html/ folder
     * @return
     */
    public static String modifyHTMLwithFile(String fileName, String htmlFile) {
        String dataName = FileUtils.removePath(fileName);
        String fileNameResource = registerTextFile(dataName, new File(fileName));
        System.out.println("Add: " + fileNameResource + " to " + htmlFile);
        String htmlText = HTMLUtils.getPluginTextFile("bacnet", "html/" + htmlFile);
        
        htmlText = htmlText.replaceFirst("_FileName", fileNameResource);
        return htmlText;
    }

    /**
     * Register a text file in RWT platform
     * 
     * @param fileNameResource
     * @param dataName
     * @param tempDataFile
     * @return
     */
    public static String registerTextFile(String dataName, File tempDataFile) {
        /*
         * For eclipse.rap
         */
        if (!RWT.getApplicationContext().getResourceManager().isRegistered(dataName)) {
            try {
                RWT.getApplicationContext().getResourceManager().register(dataName, new FileInputStream(tempDataFile));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println(RWT.getApplicationContext().getResourceManager().getLocation(dataName));
        String fileNameResource = "../" + dataName;
        return fileNameResource;

        /*
         * Fro eclipse.rcp
         */
        // return tempDataFile;
    }

}
