package bacnet.utils;

import java.io.File;

public class ImageMagick {

    public static String getConvertPATH() {

        String os = System.getProperty("os.name");
        System.out.println("OS: " + os);
        String path = "/Data" + File.separator;
        if (os.equals("Mac OS X"))
            return "/usr/local/Cellar/imagemagick/6.9.7-3/bin/convert";
        else {
            return "/usr/bin/convert";
        }

    }
}
