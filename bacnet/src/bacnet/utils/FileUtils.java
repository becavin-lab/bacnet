package bacnet.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.biojava3.core.sequence.io.BufferedReaderBytesRead;

/**
 * Utils for file manipulation. Mostly override from org.apache.commons.io
 * 
 * @author christophebecavin
 *
 */
public class FileUtils {

    /**
     * copy file with path=input to file with path=output
     * 
     * @param input
     * @param output
     * @throws IOException
     */
    public static void copy(String input, String output) throws IOException {
        org.apache.commons.io.FileUtils.copyFile(new File(input), new File(output));
    }

    /**
     * Load text from fileName
     * 
     * @param fileName
     * @return
     */
    public static String readText(String fileName) {
        try {
            FileInputStream fi = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(fi);
            BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

            String line = br.readLine();
            String text = "";
            // Read the lines and put them in ArrayList
            while (line != null) {
                text += line + "\n";
                // System.out.println(line);
                line = br.readLine();
            }
            br.close();
            isr.close();
            // If stream was created from File object then we need to close it
            if (fi != null) {
                fi.close();
            }
            // System.out.println("Text load from: "+fileName);
            return text;

        } catch (IOException e) {
            System.out.println("Error when reading the file : " + fileName + " - " + e);
            return null;
        }
    }

    /**
     * Save text from "text" into fileName
     * 
     * @param text
     * @param fileName
     * @param append
     * @param addComment
     * @return
     */
    public static File saveText(String text, String fileName, boolean append, boolean addComment) {
        try {
            FileWriter fileW = new FileWriter(fileName, append);
            BufferedWriter bufferW = new BufferedWriter(fileW);

            // comment
            if (addComment) {
                bufferW.write("#" + " Generated: " + (new Date()).toString());
                bufferW.newLine();
                bufferW.newLine();
            }
            String[] texts = text.split("\\r?\\n");
            for (String line : texts) {
                bufferW.write(line);
                bufferW.newLine();
            }
            bufferW.close();
            fileW.close();
            // System.out.println("Text saved in: "+fileName);
            return new File(fileName);

        } catch (IOException e) {
            System.out.println("Error when writing to the file : " + fileName + " - " + e);
            return null;
        }
    }

    public static File saveText(String text, String fileName) {
        return saveText(text, fileName, false, false);
    }

    /**
     * Given a query sequence, find its different occurences on a database sequence
     * 
     * @param query String to search
     * @param database String in which the search is performed
     * @return
     */
    public static ArrayList<Integer> searchPosition(String query, String database) {
        int len = query.length();
        @SuppressWarnings("unused")
        int result = 0;
        ArrayList<Integer> positions = new ArrayList<Integer>();
        if (len > 0) {
            int start = database.indexOf(query);
            while (start != -1) {
                positions.add(start);
                result++;
                start = database.indexOf(query, start + 1);
            }
        }
        // System.out.println(result);
        return positions;
    }

    /**
     * Remove extension of a fileName, by removing everything after the last '.'
     * 
     * @param s
     * @return
     */
    public static String removeExtension(String s) {
        int extensionIndex = s.lastIndexOf(".");
        if (extensionIndex == -1)
            return s;

        return s.substring(0, extensionIndex);
    }

    /**
     * Get the extension in a fileName
     * 
     * @param s
     * @return
     */
    public static String getExtension(String s) {
        int extensionIndex = s.lastIndexOf(".");
        if (extensionIndex == -1)
            return "no extension";

        return s.substring(extensionIndex, s.length());
    }

    /**
     * Remove extension of a filePath and the complete path
     * 
     * @param s
     * @return
     */
    public static String removePath(String s) {

        String separator = System.getProperty("file.separator");
        String filename = "";

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            lastSeparatorIndex = s.lastIndexOf("/");
            if (lastSeparatorIndex == -1) {
                filename = s;
            } else {
                filename = s.substring(lastSeparatorIndex + 1);
            }
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }
        return filename;
    }

    /**
     * Get only the folder where the file is contained
     * 
     * @param s
     * @return
     */
    public static String getPath(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(0, lastSeparatorIndex + 1);
        }

        return filename;
    }

    /**
     * Remove extension of a filePath and the complete path
     * 
     * @param s
     * @return
     */
    public static String removeExtensionAndPath(String s) {

        s = removePath(s);
        s = removeExtension(s);

        return s;
    }

    public static void extractGZIP(String source, String fileName) throws FileNotFoundException, IOException {
        // Open the compressed file
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(source));
        OutputStream out = new FileOutputStream(fileName);

        // Transfer bytes from the compressed file to the output file
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        // Close the file and stream
        in.close();
        out.close();
    }

    /**
     * Unziped a file in the same directory
     * 
     * @param source
     * 
     */
    public static void unZIP(String fileName) {
        try {
            ZipFile zipFile = new ZipFile(fileName);
            String path = FileUtils.getPath(fileName);
            Enumeration<?> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                String name = zipEntry.getName();
                long size = zipEntry.getSize();
                long compressedSize = zipEntry.getCompressedSize();
                System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", name, size, compressedSize);

                File file = new File(path + name);
                if (name.endsWith("/")) {
                    file.mkdirs();
                    continue;
                }

                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
                is.close();
                fos.close();

            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getFileBytes(File file) throws IOException {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1)
                ous.write(buffer, 0, read);
        } finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
                // swallow, since not that important
            }
            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
                // swallow, since not that important
            }
        }
        return ous.toByteArray();
    }

    /**
     * Search hexadecimal code in a String indicated by %<br>
     * Replace them by they ASCII representation
     * 
     * @param query
     * @return
     */
    public static String cleanStringFromHex(String query) {
        int index = query.indexOf('%');
        while (index != -1) {
            String hex = query.substring(index + 1, index + 3);
            // System.out.println(hex);
            query = query.replaceAll("%" + hex, convertHexToString(hex));
            index = query.indexOf('%');
        }
        // System.out.println(query);
        return query;
    }

    /**
     * Test if a file exists or not
     * 
     * @param path
     * @return
     */
    public static boolean exists(String path) {
        File file = new File(path);
        if (file.exists())
            return true;
        else
            return false;
    }

    /**
     * Convert an hexadecimal ASCII to a String
     * 
     * @param hex
     * @return
     */
    public static String convertHexToString(String hex) {
        if (hex.startsWith(" "))
            return hex;
        if (hex.equals("5C"))
            return "";
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        // 49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            // grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            // convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            // convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        // System.out.println("Decimal : " + temp.toString());

        return sb.toString();
    }

}
