package bacnet.raprcp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rap.rwt.service.ServiceManager;

/**
 * Specific tools for downloading files from eclipse.rap instance NEED TO BE COMMENTED FOR
 * Eclipse.RCP to work !!!
 * 
 * @author cbecavin
 *
 */
public class DownloadServiceHandler implements ServiceHandler {

    private String fileName;
    private String text = "";
    private File file = null;

    public DownloadServiceHandler(String fileName, String text) {
        this.fileName = fileName;
        this.text = text;
    }

    public DownloadServiceHandler(String fileName, File file) {
        this.fileName = fileName;
        this.file = file;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("Run DL service");
        // Which file to download? String or File
        // Get the file content
        byte[] download = new byte[0];
        if (file != null) {
            Path path = Paths.get(file.getAbsolutePath());
            download = Files.readAllBytes(path);
        } else {
            download = this.getText().getBytes();
        }
        // Send the file in the response
        response.setContentType("application/octet-stream");
        response.setContentLength(download.length);
        String contentDisposition = "attachment; filename=\"" + this.getFileName() + "\"";
        response.setHeader("Content-Disposition", contentDisposition);
        response.getOutputStream().write(download);
    }

    public static String getDownloadUrl(String fileName, String textToSave, EPartService partService) {
        ServiceManager manager = RWT.getServiceManager();
        ServiceHandler handler = new DownloadServiceHandler(fileName, textToSave);
        double random = Math.random();
        manager.registerServiceHandler("dlServiceHandler_" + fileName + "_" + random, handler);
        StringBuilder url = new StringBuilder();
        url.append(RWT.getServiceManager().getServiceHandlerUrl("dlServiceHandler_" + fileName + "_" + random));
        url.append('&').append("filename").append('=').append(fileName);
        System.out.println(url.toString());
        return url.toString();
    }

    public static String getDownloadUrl(String fileName, File fileToSave, EPartService partService) {
        ServiceManager manager = RWT.getServiceManager();
        ServiceHandler handler = new DownloadServiceHandler(fileName, fileToSave);
        double random = Math.random();
        manager.registerServiceHandler("dlServiceHandler_" + fileName + "_" + random, handler);
        StringBuilder url = new StringBuilder();
        url.append(RWT.getServiceManager().getServiceHandlerUrl("dlServiceHandler_" + fileName + "_" + random));
        url.append('&').append("filename").append('=').append(fileName);
        System.out.println(url.toString());
        return url.toString();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
