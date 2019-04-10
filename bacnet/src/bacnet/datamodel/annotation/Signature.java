package bacnet.datamodel.annotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import bacnet.Database;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

public class Signature implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5588137900648373339L;

	public static String EXTENSION = ".sig";

	private String ID = "";
	private String name = "";
	private String type = "";
	private String genome = "";
	private String description = "";
	private String reference = "";
	private int size;
	private ArrayList<String> elements = new ArrayList<String>();

	public Signature() {
	}

	/**
	 * Load serialized signature
	 * 
	 * @param signatureName
	 * @return
	 */
	public static Signature getSignatureFromName(String signatureName) {
		TreeMap<String, String> signaturesNametoID = Database.getInstance().getSignaturesNametoID();

		// get ID
		String signatureID = signaturesNametoID.get(signatureName);

		// get signature
		Signature signature = Signature.load(Database.getSIGNATURES_PATH() + signatureID);
		if (signature == null) {
			System.err.println("Cannot find signature ID: " + signatureID + " name: " + signatureName);
			return null;
		} else
			return signature;
	}

	/**
	 * Load <code>TreeMap<String,String> signaturesNametoID</code> from text file in
	 * <code>LIST_PATH</code><br>
	 * Used for initiating DATABASE.INSTANCE data provider
	 */
	public static TreeMap<String, String> loadSignaturesNametoID(String signaturePath) {
		String[][] array = TabDelimitedTableReader.read(signaturePath);
		TreeMap<String, String> signatures = new TreeMap<>();
		for (int i = 0; i < array.length; i++) {
			signatures.put(array[i][0], array[i][1]);
		}
		return signatures;
	}

	/**
	 * Create a mapping between gene to a signature <br>
	 * All signatures from the folder Database.getSIGNATURES() are used
	 * 
	 * @param genome
	 * @return
	 */
	public static HashMap<String, String[]> getGeneToSigMap(ChromosomeBacteriaSequence chromo) {
		TreeMap<String, Signature> signatures = getSignaturesFromTextFiles();
		HashMap<String, String[]> sigMap = new HashMap<String, String[]>();
		ArrayList<String> listSignatures = new ArrayList<String>();
		for (String locus : chromo.getCodingSequenceHashMap().keySet()) {
			listSignatures.clear();
			for (String signatureID : signatures.keySet()) {
				Signature signature = signatures.get(signatureID);
				if (signature.getElements().contains(locus)) {
					// if found in this signature we add it to listSignatures
					listSignatures.add(signatureID);
				}
			}
			String[] signaturesArray = listSignatures.toArray(new String[0]);
			sigMap.put(locus, signaturesArray);
		}
		return sigMap;
	}

	/*
	 * ********************************** Load and Save in text file
	 * **********************************
	 */

	/**
	 * Read all signatures in txt format from the folder Database.getSIGNATURES()
	 * 
	 * @return
	 */
	public static TreeMap<String, Signature> getSignaturesFromTextFiles() {
		TreeMap<String, Signature> signatures = new TreeMap<>();
		File path = new File(Database.getSIGNATURES_PATH());
		File[] files = path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".txt"))
					return true;
				return false;
			}
		});
		for (File file : files) {
			Signature signature = loadText(file.getAbsolutePath());
			signatures.put(FileUtils.removeExtensionAndPath(file.getAbsolutePath()), signature);
		}

		// create Map from signature.Name to signature.ID
		TreeMap<String, String> signaturesNametoID = Database.getInstance().getSignaturesNametoID();
		if (signaturesNametoID.size() == 0) {
			for (String ID : signatures.keySet()) {
				signaturesNametoID.put(signatures.get(ID).getName(), ID);
			}
		}
		return signatures;
	}

	/**
	 * Read a formatted text file, and create the corresponding signature object
	 * <br>
	 * Each field of this text file, has to be separated by "#"
	 * 
	 * @param fileName
	 * @return
	 */
	public static Signature loadText(String fileName) {
		Signature signature = new Signature();
		signature.ID = FileUtils.removeExtensionAndPath(fileName);
		String[] lines = FileUtils.readText(fileName).split("#");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.contains("Name=")) {
				String temp = line.replaceFirst("Name=", "").trim();
				signature.setName(temp);
			} else if (line.contains("Genome=")) {
				String temp = line.replaceFirst("Genome=", "").trim();
				signature.setGenome(temp);
			} else if (line.contains("Reference=")) {
				signature.setReference(line.replaceFirst("Reference=", "").trim());
			} else if (line.contains("Description=")) {
				String temp = line.replaceFirst("Description=", "").trim().replaceAll("\n", " ");
				signature.setDescription(temp);
			} else if (line.contains("List=")) {
				String[] elementsArray = line.replaceFirst("List=", "").trim().split("\n");
				ArrayList<String> elements = new ArrayList<String>();
				for (String element : elementsArray) {
					String tempGene = element.split(" ")[0].trim();
					if (!tempGene.equals(""))
						elements.add(tempGene);
				}
				signature.setElements(elements);
				signature.setSize(elements.size());
			}

		}
		return signature;
	}

	/**
	 * Save Signature in a readable text file
	 * 
	 * @param fileName
	 */
	public void saveText(String fileName) {
		String text = "#Name=" + this.getName() + "\n";
		text += "#Genome=" + this.getGenome() + "\n";
		text += "#Reference=" + this.getReference() + "\n";
		text += "#Description=" + this.getDescription() + "\n";
		text += "#List=\n";
		for (String element : elements) {
			text += element + "\n";
		}
		FileUtils.saveText(text, fileName);
	}

	/*
	 * ********************************** Serialization
	 * **********************************
	 */
	/**
	 * Read all <code>Signature</code> in text files, and serialize them in
	 * Database.getSIGNATURES()\*<br>
	 * Save an array with the HashMap between Signature name and ID
	 */
	public static void convertAll() {
		ArrayList<Signature> signatures = new ArrayList<Signature>();
		File path = new File(Database.getSIGNATURES_PATH());
		File[] files = path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".txt"))
					return true;
				return false;
			}
		});
		for (File file : files) {
			Signature signature = loadText(file.getAbsolutePath());
			signatures.add(signature);
		}
		ArrayList<String> signaturesArray = new ArrayList<>();
		for (Signature sig : signatures) {
			String pathSig = Database.getSIGNATURES_PATH();
			File file = new File(pathSig);
			if (file.exists()) {
				pathSig += sig.ID;
				System.out.println(pathSig);
				sig.save(pathSig);
				signaturesArray.add(sig.getName() + "\t" + sig.getID());
			} else {
				file.mkdir();
				pathSig += sig.ID;
				System.out.println(pathSig);
				sig.save(pathSig);
				signaturesArray.add(sig.getName() + "\t" + sig.getID());
			}
		}
		TabDelimitedTableReader.saveList(signaturesArray,
				Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("SIGNATURES"));
	}

	// /**
	// * Read all Serialized <code>Signature</code> add them in the different
	// <code>Sequence</code>
	// contained in <code>Genome</code>
	// */
	// public static void addToGenome(String genomeName){
	// Genome genome = Genome.loadGenome(genomeName);
	// String pathName =
	// Database.getGENOMES()+genomeName+File.separator+"Signatures"+File.separator;
	// File path = new File(pathName);
	// File[] files = path.listFiles(new FilenameFilter() {
	// public boolean accept(File dir, String name) {
	// if(name.endsWith(".svn")) return false;
	// return true;
	// }
	// });
	// for(File file : files){
	// Signature signature = load(file.getAbsolutePath());
	// System.out.println(signature.getID() + signature.getName() +
	// signature.getType());
	//
	// for(String element : signature.getElements()){
	// //System.out.println(element);
	// Sequence seq = genome.getElement(element);
	// seq.getSignatures().add(signature.getID());
	// seq.save(Database.getGENOMES()+genomeName+File.separator+"Sequences"+File.separator+seq.getName());
	// }
	// }
	// }
	/**
	 * Read compressed, serialized data with a FileInputStream. Uncompress that data
	 * with a GZIPInputStream. Deserialize the vector of lines with a
	 * ObjectInputStream. Replace current data with new data, and redraw everything.
	 */
	public static Signature load(String fileName) {
		try {
			// Create necessary input streams
			FileInputStream fis = new FileInputStream(fileName); // Read from file
			GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
			ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
			// Read in an object. It should be a vector of scribbles
			Signature sign = (Signature) in.readObject();
			in.close();
			return sign;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} // Close the stream.

	}

	/**
	 * Serialize the vector of lines with an ObjectOutputStream. Compress the
	 * serialized objects with a GZIPOutputStream. Write the compressed, serialized
	 * data to a file with a FileOutputStream. Don't forget to flush and close the
	 * stream.
	 */
	public void save(String fileName) {
		try {
			// Create the necessary output streams to save the scribble.
			FileOutputStream fos = new FileOutputStream(fileName);
			// Save to file
			GZIPOutputStream gzos = new GZIPOutputStream(fos);
			// Compressed
			ObjectOutputStream out = new ObjectOutputStream(gzos);
			// Save objects
			out.writeObject(this); // Write the entire Vector of scribbles
			out.flush(); // Always flush the output.
			out.close(); // And close the stream.
		}
		// Print out exceptions. We should really display them in a dialog...
		catch (IOException e) {
			System.out.println(e);
		}
	}

	/*
	 * Getter and setters
	 */

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGenome() {
		return genome;
	}

	public void setGenome(String genome) {
		this.genome = genome;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public ArrayList<String> getElements() {
		return elements;
	}

	public void setElements(ArrayList<String> elements) {
		this.elements = elements;
	}

}
