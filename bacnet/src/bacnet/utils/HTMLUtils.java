package bacnet.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.biojava3.core.sequence.io.BufferedReaderBytesRead;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import bacnet.swt.ResourceManager;

/**
 * List of methods to manage D3js visualization in BACNET
 * 
 * @author Christophe Bécavin
 *
 */
public class HTMLUtils {

    public static String SVG = "SVGDisplay.html";
    public static String NETWORK = "network.html";
    public static String CIRCOS = "circos.html";
    public static String SCATTER = "scatterplot.html";

    /**
     * Returns an {@link String} based on a {@link Bundle} and resource entry path.
     * 
     * @param symbolicName the symbolic name of the {@link Bundle}.
     * @param path the path of the resource entry.
     * @return the {@link String} representation of the html stored in the file at the specified path.
     */
    public static String getPluginTextFile(String symbolicName, String path) {
        try {
            URL url = ResourceManager.getPluginFileURL(symbolicName, path);
            if (url != null) {
                return getPluginHTMLFromUrl(url);
            }
        } catch (Throwable e) {
            // Ignore any exceptions
        }
        return null;
    }

    /**
     * Returns an {@link Image} based on given {@link URL}.
     */
    private static String getPluginHTMLFromUrl(URL url) {
        try {
            @SuppressWarnings("unused")
            String key = url.toExternalForm();

            InputStream stream;
            stream = url.openStream();

            try {
                InputStreamReader isr = new InputStreamReader(stream);
                BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

                String line = br.readLine();
                String html = "";
                // Read the lines and put them in ArrayList
                while (line != null) {
                    html += line + "\n";
                    // System.out.println(line);
                    line = br.readLine();
                }
                br.close();
                isr.close();
                // If stream was created from File object then we need to close it
                if (stream != null) {
                    stream.close();
                }
                // System.out.println("Text load from: "+fileName);
                return html;
            } catch (IOException e) {
                System.out.println("Error when writing to the file : " + url + " - " + e);
                return null;
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return null;
    }

}
