package bacnet.datamodel.sequence;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import bacnet.Database;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.TabDelimitedTableReader;

public class Srna extends Sequence {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7964059340593114897L;

	/**
	 * Path for Srna raw data
	 */
	public static String PATH = GenomeNCBI.PATH_ANNOTATION + "/Srna/";
	/**
	 * Path for serialized Srna before transferring data to Database
	 */
	public static String PATHSerialize = PATH + "sRNASerialize" + File.separator;
	/**
	 * Path for Srna XML serialization for control
	 */
	public static String PATHXML = PATH + "sRNAXML" + File.separator;
	/**
	 * Path for XML serialization for 10403S (Which have a specific annotation)
	 */
	public static String PATHXML_10403S = PATH + "sRNAXML10403S" + File.separator;

	public static String PATH_SEC_STRUCTURE = Database.getANNOTATIONDATA_PATH() + "/SrnaSecondStructure/";
	public static String PATH_CONSERVATION = Database.getANNOTATIONDATA_PATH() + "SrnaConservation.txt";
	public static String PATHTABLE_Srna = Database.getANNOTATIONDATA_PATH() + "SrnaTable.txt";
	public static String PATHTABLE_ASrna = Database.getANNOTATIONDATA_PATH() + "ASrnaTable.txt";
	public static String PATHTABLE_CISReg = Database.getANNOTATIONDATA_PATH() + "CisRegTable.txt";
	public static String PATHFigure_Srna = Database.getANNOTATIONDATA_PATH() + "SrnaCircularGenome.png";
	public static String PATHFigure_ASrna = Database.getANNOTATIONDATA_PATH() + "ASrnaCircularGenome.png";
	public static String PATHFigure_CISReg = Database.getANNOTATIONDATA_PATH() + "CisRegCircularGenome.png";
	public static String PATHTABLE_SrnaReference = Database.getANNOTATIONDATA_PATH() + "sRNAReference.txt";

	private ArrayList<String> foundIn = new ArrayList<String>();
	private TypeSrna typeSrna = TypeSrna.Srna;

	public enum TypeSrna {
		Srna, CisReg, ASrna
	}

	public Srna() {
		setType(SeqType.Srna);
	}

	public Srna(String name, int from, int to) {
		this.setName(name);
		this.setEnd(to);
		this.setBegin(from);
		setType(SeqType.Srna);
	}

	public Srna(String name, int from, int to, char strand) {
		super(name, from, to, strand);
		setType(SeqType.Srna);
	}

	public boolean isEqual(Srna sRNA, int scale) {
		int center1 = (getBegin() + getEnd()) / 2;
		int center2 = (sRNA.getBegin() + sRNA.getEnd()) / 2;
		if (Math.abs(center1 - center2) < scale) {
			if (sRNA.isStrand() == isStrand()) {
				if (sRNA.getTypeSrna() == this.getTypeSrna())
					return true;
				else
					return false;
			} else
				return false;
			// return true;
		} else
			return false;
	}

	public static Srna load(String fileName) {
		try {
			// Create necessary input streams
			FileInputStream fis = new FileInputStream(fileName); // Read from file
			GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
			ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
			// Read in an object. It should be a vector of scribbles
			Srna seq = (Srna) in.readObject();
			in.close();
			return seq;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} // Close the stream.

	}

	public String getRfamID() {
		String rfam = this.getFeatures().get("Rfam ID (Rfam 2012)");
		return rfam;
	}

	/**
	 * Classify Srna par IDs and save it in PATH_LISTSRNA
	 */
	public static void setSrnaOrder() {
		TreeSet<Integer> sRnasId = new TreeSet<Integer>();
		for (Srna sRNA : Genome.loadEgdeGenome().getFirstChromosome().getsRNAs().values()) {
			System.out.println(sRNA.getName() + "  -  " + sRNA.getId());
			sRnasId.add(Integer.parseInt(sRNA.getId()));
		}

		ArrayList<String> sRNANames = new ArrayList<String>();
		for (Integer idint : sRnasId) {
			String id = String.valueOf(idint);
			for (Srna sRNA : Genome.loadEgdeGenome().getFirstChromosome().getsRNAs().values()) {
				if (id.equals(sRNA.getId())) {
					sRNANames.add(sRNA.getName());
				}
			}
		}

		TabDelimitedTableReader.saveList(sRNANames,
				Database.getANNOTATIONDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDe_SRNA"));
	}

	/**
	 * Return an ArrayList containing Srna names in the proper order<br>
	 * The list is load from PATH_LISTSRNA
	 * 
	 * @return
	 */
	public static ArrayList<String> getSrnaOrder() {
		return TabDelimitedTableReader.readList(Database.getInstance().getDatabaseFeatures().get("EGDe_SRNA"));
	}

	/**
	 * Return an ordered list of all Srna in EGD-e
	 * 
	 * @return
	 */
	public static ArrayList<Srna> getEGDeSrnas() {
		Genome genome = Genome.loadEgdeGenome();
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		for (String sRNAName : getSrnaOrder()) {
			sRNAs.add(genome.getChromosomes().get(0).getsRNAs().get(sRNAName));
		}
		return sRNAs;
	}

	/**
	 * Return a list of all Srna in EGD-c
	 * 
	 * @return
	 */
	public static ArrayList<Srna> getEGDcSrnas() {
		Genome genome = Genome.loadGenome("FINAL_" + Genome.EGDC_NAME);
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		for (String sRNAName : genome.getChromosomes().get(0).getsRNAs().keySet()) {
			sRNAs.add(genome.getChromosomes().get(0).getsRNAs().get(sRNAName));
		}
		return sRNAs;
	}

	/**
	 * Return a list of all Srna in 10403S
	 * 
	 * @return
	 */
	public static ArrayList<Srna> get10403SSrnas() {
		Genome genome = Genome.loadGenome("FINAL_" + Genome.DP10403S_NAME);
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		for (String sRNAName : genome.getChromosomes().get(0).getsRNAs().keySet()) {
			sRNAs.add(genome.getChromosomes().get(0).getsRNAs().get(sRNAName));
		}
		return sRNAs;
	}

	/**
	 * Return a list of all Srna, CisReg and ASrna in EGD-e
	 * 
	 * @return
	 */
	public static ArrayList<Srna> getEGDeALLSrnas() {
		Genome genome = Genome.loadEgdeGenome();
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		for (String sRNAName : getSrnaOrder()) {
			sRNAs.add(genome.getChromosomes().get(0).getsRNAs().get(sRNAName));
		}
		for (String name : genome.getChromosomes().get(0).getCisRegs().keySet()) {
			sRNAs.add(genome.getChromosomes().get(0).getCisRegs().get(name));
		}
		for (String name : genome.getChromosomes().get(0).getAsRNAs().keySet()) {
			sRNAs.add(genome.getChromosomes().get(0).getAsRNAs().get(name));
		}

		return sRNAs;
	}

	/**
	 * Extract to a GFF file the list of Srna from EGD-e
	 */
	public static void extractAllSrnaToGff() {
		ArrayList<String> results = new ArrayList<>();
		Genome genome = Genome.loadEgdeGenome();
		for (Srna sRNA : genome.getChromosomes().get(0).getsRNAs().values()) {
			String text = "NC_003210.1\tListeriomics\t";
			text += sRNA.getTypeSrna() + "\t" + sRNA.getBegin() + "\t" + sRNA.getEnd() + "\t.\t" + sRNA.getStrand()
					+ "\t.\t";
			text += "locus_tag=" + sRNA.getName() + ";note=" + sRNA.getFoundInText();
			results.add(text);
			System.out.println(text);
		}
		for (Srna sRNA : genome.getChromosomes().get(0).getAsRNAs().values()) {
			String text = "NC_003210.1\tListeriomics\t";
			text += sRNA.getTypeSrna() + "\t" + sRNA.getBegin() + "\t" + sRNA.getEnd() + "\t.\t" + sRNA.getStrand()
					+ "\t.\t";
			text += "locus_tag=" + sRNA.getName() + ";note=" + sRNA.getFoundInText();
			results.add(text);
			System.out.println(text);
		}
		for (Srna sRNA : genome.getChromosomes().get(0).getCisRegs().values()) {
			String text = "NC_003210.1\tListeriomics\t";
			text += sRNA.getTypeSrna() + "\t" + sRNA.getBegin() + "\t" + sRNA.getEnd() + "\t.\t" + sRNA.getStrand()
					+ "\t.\t";
			text += "locus_tag=" + sRNA.getName() + ";note=" + sRNA.getFoundInText();
			results.add(text);
			System.out.println(text);
		}
		TabDelimitedTableReader.saveList(results, "D:/sRNA.txt");
	}

	/*
	 * *****************************************************************************
	 * *********** SETTER and GETTER
	 */
	public ArrayList<String> getFoundIn() {
		return foundIn;
	}

	public String getFoundInText() {
		String ret = "";
		for (String temp : getFoundIn()) {
			ret += temp + ";";
		}
		return ret;
	}

	public void setFoundIn(ArrayList<String> foundIn) {
		this.foundIn = foundIn;
	}

	public TypeSrna getTypeSrna() {
		return typeSrna;
	}

	public void setTypeSrna(TypeSrna typeSrna) {
		this.typeSrna = typeSrna;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
