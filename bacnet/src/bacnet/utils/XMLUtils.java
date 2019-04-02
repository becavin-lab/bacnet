package bacnet.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public final class XMLUtils {

    private XMLUtils() {}

    /**
     * Serialisation d'un objet dans un fichier
     * 
     * @param object objet a serialiser
     * @param filename chemin du fichier
     */
    public static void encodeToFile(Object object, String fileName) {
        // ouverture de l'encodeur vers le fichier
        XMLEncoder encoder;
        try {
            encoder = new XMLEncoder(new FileOutputStream(fileName));
            // serialisation de l'objet
            encoder.writeObject(object);
            encoder.flush();
            encoder.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Deserialisation d'un objet depuis un fichier
     * 
     * @param filename chemin du fichier
     */
    public static Object decodeFromFile(String fileName) {
        Object object = null;
        try {
            // ouverture de decodeur
            XMLDecoder decoder = new XMLDecoder(new FileInputStream(fileName));
            // deserialisation de l'objet
            object = decoder.readObject();
            decoder.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return object;
    }

}
