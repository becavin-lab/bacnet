package bacnet.utils;

import java.io.File;

/**
 * Methods link to image mlanipulation with ImageMagick
 * 
 * @author christophebecavin
 *
 */
public class ImageMagick {

    /**
     * Need to convert path from local use or server utilization
     * 
     * @return
     */
    public static String getConvertPATH() {

        String os = System.getProperty("os.name");
        System.out.println("OS: " + os);
        @SuppressWarnings("unused")
        String path = "/Data" + File.separator;
        if (os.equals("Mac OS X"))
            return "/usr/local/Cellar/imagemagick/6.9.7-3/bin/convert";
        else {
            return "/usr/bin/convert";
        }

    }
}
