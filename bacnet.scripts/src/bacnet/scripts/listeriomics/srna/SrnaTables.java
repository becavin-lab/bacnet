package bacnet.scripts.listeriomics.srna;

import java.io.File;
import java.util.ArrayList;

import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequence.Srna.TypeSrna;
import bacnet.reader.TabDelimitedTableReader;

/**
 * Include a list of Methods to read every Table containing Small RNAs in
 * Listeria<br>
 * For each it reads the <code>Table</code> and create a <code>List</code> of
 * <code>Srna</code>
 * 
 * @author UIBC
 *
 */
public class SrnaTables {

	// public static String PATH =
	// Database.getPATH()+"Genome"+File.separator+"sRNA"+File.separator+"RawData
	// Tables"+File.separator;
	public static String PATH = GenomeNCBI.PATH_ANNOTATION + "/Srna/RawDataTables" + File.separator;

	/**
	 * Read the different tables from Toledo-Arana publication and create a list of
	 * sRNAs from the different tables
	 * 
	 * @return
	 * 
	 */
	public static ArrayList<Srna> getToledoInfo() {
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		String[][] array = TabDelimitedTableReader.read(PATH + "sRNA Toledo 2009.txt");
		System.err.println("Toledo sRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][1], Integer.parseInt(array[i][2]), Integer.parseInt(array[i][3]),
					array[i][4].charAt(0));
			sRNA.getFoundIn().add("Toledo-Arana et al. 2009");
			sRNA.setTypeSrna(TypeSrna.Srna);
			sRNA.setId(array[i][0]);
			sRNA.getFeatures().put("feature (Toledo-Arana et al. 2009)", array[i][5]);
			sRNA.getFeatures().put("note (Toledo-Arana et al. 2009)", array[i][6]);
			sRNA.getFeatures().put("ref (Toledo-Arana et al. 2009)", array[i][7]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "asRNA Toledo 2009.txt");
		System.err.println("Toledo asRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]));
			sRNA.setTypeSrna(TypeSrna.ASrna);
			sRNA.getFoundIn().add("Toledo-Arana et al. 2009");
			sRNA.setId(array[i][0]);
			sRNA.setLength(sRNA.getEnd());
			sRNA.setStrand(array[i][3].charAt(0));
			if (!sRNA.isStrand())
				sRNA.setEnd(sRNA.getBegin() - sRNA.getLength());
			else
				sRNA.setEnd(sRNA.getBegin() + sRNA.getLength());
			sRNA.getFeatures().put("Transcript (Toledo-Arana et al. 2009)", array[i][4]);
			sRNA.getFeatures().put("Min transcript coverage (Toledo-Arana et al. 2009)", array[i][5]);
			sRNA.getFeatures().put("note (Toledo-Arana et al. 2009)", array[i][6]);
			sRNA.getFeatures().put("Figure (Toledo-Arana et al. 2009)", array[i][7]);
			sRNA.getFeatures().put("Type (Toledo-Arana et al. 2009)", array[i][8]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "cisReg Toledo 2009.txt");
		System.err.println("Toledo cisReg " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][1], Integer.parseInt(array[i][2]), Integer.parseInt(array[i][3]),
					array[i][4].charAt(0));
			sRNA.setTypeSrna(TypeSrna.CisReg);
			sRNA.getFoundIn().add("Toledo-Arana et al. 2009");
			sRNA.setId(array[i][0]);
			if (sRNA.getEnd() == OmicsData.MISSING_VALUE)
				sRNA.setLength(OmicsData.MISSING_VALUE);
			else
				sRNA.setLength(Math.abs(sRNA.getBegin() - sRNA.getEnd()));
			sRNA.getFeatures().put("feature (Toledo-Arana et al. 2009)", array[i][5]);
			sRNA.getFeatures().put("note (Toledo-Arana et al. 2009)", array[i][6]);
			sRNA.getFeatures().put("ref (Toledo-Arana et al. 2009)", array[i][7]);
			sRNAs.add(sRNA);
		}
		System.err.println("Toledo, read " + sRNAs.size() + " elements");
		return sRNAs;
	}

	/**
	 * Read the different tables from Oliver et al. 2009 publication and create a
	 * list of sRNAs from the different tables
	 * 
	 * @return
	 * 
	 */
	public static ArrayList<Srna> getOliverInfo() {
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		String[][] array = TabDelimitedTableReader.read(PATH + "sRNA Oliver 2009-1.txt");
		System.err.println("Oliver sRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][6], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]), '+');
			sRNA.setTypeSrna(TypeSrna.Srna);
			sRNA.setId(array[i][0]);
			sRNA.getFoundIn().add("Oliver et al. 2009");
			sRNA.setGenomeName("Listeria_monocytogenes_10403S_pseudoChromosome");
			sRNA.getFeatures().put("10403S Average GEI (Oliver et al. 2009)", array[i][3]);
			sRNA.getFeatures().put("Delta_sigB Average GEI (Oliver et al. 2009)", array[i][4]);
			sRNA.getFeatures().put("FC range (10403S/Delta_sigB) (Oliver et al. 2009)", array[i][5]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "sRNA Oliver 2009-2.txt");
		System.err.println("Oliver sRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]), '+');
			sRNA.setTypeSrna(TypeSrna.Srna);
			sRNA.setId(array[i][0]);
			sRNA.getFoundIn().add("Oliver et al. 2009");
			sRNA.setGenomeName("Listeria_monocytogenes_10403S_pseudoChromosome");
			sRNA.setLength(Math.abs(sRNA.getBegin() - sRNA.getEnd()));
			sRNA.getFeatures().put("Comment", array[i][3]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "cisReg Oliver 2009-1.txt");
		System.err.println("Oliver cisReg " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]), '+');
			sRNA.setTypeSrna(TypeSrna.CisReg);
			sRNA.getFoundIn().add("Oliver et al. 2009");
			sRNA.setGenomeName("Listeria_monocytogenes_10403S_pseudoChromosome");
			sRNA.getFeatures().put("10403S Average GEI (Oliver et al. 2009)", array[i][3]);
			sRNA.getFeatures().put("Delta_sigB Average GEI (Oliver et al. 2009)", array[i][4]);
			sRNA.getFeatures().put("FC range (10403S/Delta_sigB) (Oliver et al. 2009)", array[i][5]);
			sRNAs.add(sRNA);
		}
		System.err.println("Oliver, read " + sRNAs.size() + " elements");
		return sRNAs;
	}

	/**
	 * Read the different tables from Oliver et al. 2009 publication and create a
	 * list of sRNAs from the different tables
	 * 
	 * @return
	 * 
	 */
	public static ArrayList<Srna> getOliverInfoEGDePosition() {
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		String[][] array = TabDelimitedTableReader.read(PATH + "sRNA Oliver 2009-1 EGDe position.txt");
		System.err.println("Oliver sRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]),
					array[i][3].charAt(0));
			sRNA.setTypeSrna(TypeSrna.Srna);
			sRNA.setId(array[i][0]);
			sRNA.getFoundIn().add("Oliver et al. 2009");
			sRNA.getFeatures().put("10403S Average GEI (Oliver et al. 2009)", array[i][4]);
			sRNA.getFeatures().put("Delta_sigB Average GEI (Oliver et al. 2009)", array[i][5]);
			sRNA.getFeatures().put("FC range (10403S/Delta_sigB) (Oliver et al. 2009)", array[i][6]);
			sRNAs.add(sRNA);
		}
		System.err.println("Oliver, read " + sRNAs.size() + " elements");
		return sRNAs;
	}

	/**
	 * Read the different tables from Mraheil et al. 2011 publication and create a
	 * list of sRNAs from the different tables
	 * 
	 * @return
	 * 
	 */
	public static ArrayList<Srna> getMraheilInfo() {
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		String[][] array = TabDelimitedTableReader.read(PATH + "sRNA Mraheil 2011.txt");
		System.err.println("Mraheil sRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][1], Integer.parseInt(array[i][2]), Integer.parseInt(array[i][3]),
					array[i][4].charAt(0));
			sRNA.setTypeSrna(TypeSrna.Srna);
			sRNA.getFoundIn().add("Mraheil et al. 2011");
			sRNA.setId(array[i][0]);
			sRNA.getFeatures().put("Nb read Intracellular (Mraheil et al. 2011)", array[i][5]);
			sRNA.getFeatures().put("Nb read Extracellular (Mraheil et al. 2011)", array[i][6]);
			if (!array[i][9].equals("")) {
				sRNA.getFeatures().put("Promoter Name (Mraheil et al. 2011)", array[i][9]);
				sRNA.getFeatures().put("Promoter in Intracellular (Mraheil et al. 2011)", array[i][7]);
				sRNA.getFeatures().put("Promoter in Extracellular (Mraheil et al. 2011)", array[i][8]);
			}
			sRNA.getFeatures().put("Terminator in Intracellular (Mraheil et al. 2011)", array[i][10]);
			sRNA.getFeatures().put("Terminator in Extracellular (Mraheil et al. 2011)", array[i][11]);
			sRNA.getFeatures().put("Rfam Name (Mraheil et al. 2011)", array[i][12]);
			sRNA.getFeatures().put("New (Mraheil et al. 2011)", array[i][13]);
			sRNA.getFeatures().put("note (Mraheil et al. 2011)", array[i][14]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "asRNA Mraheil 2011.txt");
		System.err.println("Mraheil asRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][1], Integer.parseInt(array[i][2]), Integer.parseInt(array[i][3]),
					array[i][4].charAt(0));
			sRNA.setTypeSrna(TypeSrna.ASrna);
			sRNA.getFoundIn().add("Mraheil et al. 2011");
			sRNA.setId(array[i][0]);
			sRNA.getFeatures().put("Nb read Intracellular (Mraheil et al. 2011)", array[i][5]);
			sRNA.getFeatures().put("Nb read Extracellular (Mraheil et al. 2011)", array[i][6]);
			if (!array[i][9].equals("")) {
				sRNA.getFeatures().put("Promoter Name (Mraheil et al. 2011)", array[i][9]);
				sRNA.getFeatures().put("Promoter in Intracellular (Mraheil et al. 2011)", array[i][7]);
				sRNA.getFeatures().put("Promoter in Extracellular (Mraheil et al. 2011)", array[i][8]);
			}
			sRNA.getFeatures().put("Terminator in Intracellular (Mraheil et al. 2011)", array[i][10]);
			sRNA.getFeatures().put("Terminator in Extracellular (Mraheil et al. 2011)", array[i][11]);
			sRNA.getFeatures().put("Rfam Name (Mraheil et al. 2011)", array[i][12]);
			sRNA.getFeatures().put("note (Mraheil et al. 2011)", array[i][13]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "cisReg Mraheil 2011.txt");
		System.err.println("Mraheil cisReg " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][1], Integer.parseInt(array[i][2]), Integer.parseInt(array[i][3]),
					array[i][4].charAt(0));
			sRNA.setTypeSrna(TypeSrna.CisReg);
			sRNA.getFoundIn().add("Mraheil et al. 2011");
			sRNA.setId(array[i][0]);
			sRNA.getFeatures().put("Nb read Intracellular (Mraheil et al. 2011)", array[i][5]);
			sRNA.getFeatures().put("Nb read Extracellular (Mraheil et al. 2011)", array[i][6]);
			if (!array[i][9].equals("")) {
				sRNA.getFeatures().put("Promoter Name (Mraheil et al. 2011)", array[i][9]);
				sRNA.getFeatures().put("Promoter in Intracellular (Mraheil et al. 2011)", array[i][7]);
				sRNA.getFeatures().put("Promoter in Extracellular (Mraheil et al. 2011)", array[i][8]);
			}
			sRNA.getFeatures().put("Terminator in Intracellular (Mraheil et al. 2011)", array[i][10]);
			sRNA.getFeatures().put("Terminator in Extracellular (Mraheil et al. 2011)", array[i][11]);
			sRNA.getFeatures().put("Rfam Name (Mraheil et al. 2011)", array[i][12]);
			sRNAs.add(sRNA);
		}
		System.err.println("Mraheil, read " + sRNAs.size() + " elements");
		return sRNAs;
	}

	/**
	 * Read the different tables from Jeff summary table in 2011 and create a list
	 * of sRNAs from the different tables
	 * 
	 * @return
	 * 
	 */
	public static ArrayList<Srna> getJeffInfo() {
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		String[][] array = TabDelimitedTableReader.read(PATH + "sRNA Jeff 2012.txt");
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]),
					array[i][3].charAt(0));
			sRNA.setTypeSrna(TypeSrna.Srna);
			sRNA.getFoundIn().add("Mellin et al. 2012");
			sRNA.getFeatures().put("ref (Mellin et al. 2012)", array[i][4]);
			sRNA.getFeatures().put("note (Mellin et al. 2012)", array[i][5]);
			sRNA.getFeatures().put("note Mellin (Mellin et al. 2012)", array[i][6]);
			sRNA.getFeatures().put("NB verification (Mellin et al. 2012)", array[i][7]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "cisReg Jeff 2012.txt");
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]),
					array[i][3].charAt(0));
			sRNA.setTypeSrna(TypeSrna.CisReg);
			sRNA.getFoundIn().add("Mellin et al. 2012");
			sRNA.getFeatures().put("feature coordinates begin (Mellin et al. 2012)", array[i][4]);
			sRNA.getFeatures().put("feature coordinates end (Mellin et al. 2012)", array[i][5]);
			sRNA.getFeatures().put("comment Mellin (Mellin et al. 2012)", array[i][6]);
			sRNAs.add(sRNA);
		}
		System.err.println("Jeff, read " + sRNAs.size() + " elements");
		return sRNAs;
	}

	/**
	 * Read the different tables from Rfam 2011 data and create a list of sRNAs from
	 * the different tables
	 * 
	 * @return
	 * 
	 */
	public static ArrayList<Srna> getRfamInfo() {
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		String[][] array = TabDelimitedTableReader.read(PATH + "sRNA Rfam 2012.txt");
		System.err.println("Rfam sRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]),
					array[i][3].charAt(0));
			sRNA.setTypeSrna(TypeSrna.Srna);
			sRNA.getFoundIn().add("Rfam 2012");
			sRNA.getFeatures().put("Bits score (Rfam 2012)", array[i][4]);
			sRNA.getFeatures().put("Rfam ID (Rfam 2012)", array[i][5]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "cisReg Rfam 2012.txt");
		System.err.println("Rfam cisReg " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]),
					array[i][3].charAt(0));
			sRNA.setTypeSrna(TypeSrna.CisReg);
			sRNA.getFoundIn().add("Rfam 2012");
			sRNA.getFeatures().put("Bits score (Rfam 2012)", array[i][4]);
			sRNA.getFeatures().put("Rfam ID (Rfam 2012)", array[i][5]);
			sRNAs.add(sRNA);
		}
		System.err.println("Rfam, read " + sRNAs.size() + " elements");
		return sRNAs;
	}

	/**
	 * Read the table from Johansson 2009 data and create a list of sRNAs from the
	 * different tables
	 * 
	 * @return
	 * 
	 */
	public static ArrayList<Srna> getJohanssonInfo() {
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		String[][] array = TabDelimitedTableReader.read(PATH + "cisReg Johansson 2009.txt");
		System.err.println("Rfam cisReg " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]),
					array[i][3].charAt(0));
			sRNA.setTypeSrna(TypeSrna.CisReg);
			sRNA.getFoundIn().add("Johansson 2009");
			sRNA.getFeatures().put("prfA regulated (Johansson 2009)", array[i][4]);
			sRNA.getFeatures().put("Half-life (Johansson 2009)", array[i][5]);
			sRNAs.add(sRNA);
		}
		System.err.println("Johansson, read " + sRNAs.size() + " elements");
		return sRNAs;
	}

	/**
	 * Read the different tables from Wurtezel, Sesto et al. 2012 publication and
	 * create a list of sRNAs from the different tables
	 * 
	 * @return
	 * 
	 */
	public static ArrayList<Srna> getWurtzelInfo() {
		ArrayList<Srna> sRNAs = new ArrayList<Srna>();
		String[][] array = TabDelimitedTableReader.read(PATH + "sRNA Wurtzel 2012.txt");
		System.err.println("wurtzel sRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), Integer.parseInt(array[i][2]),
					array[i][3].charAt(0));
			sRNA.setTypeSrna(TypeSrna.Srna);
			sRNA.getFoundIn().add("Wurtzel et al. 2012");
			sRNA.getFeatures().put("Inocua presence (Wurtzel et al. 2012)", array[i][4]);
			sRNA.getFeatures().put("note (Wurtzel et al. 2012)", array[i][5]);
			sRNA.getFeatures().put("ref (Wurtzel et al. 2012)", array[i][6]);
			sRNAs.add(sRNA);
		}
		array = TabDelimitedTableReader.read(PATH + "asRNA Wurtzel 2012.txt");
		System.err.println("Wurtzel asRNA " + array.length);
		for (int i = 1; i < array.length; i++) {
			Srna sRNA = new Srna(array[i][0], Integer.parseInt(array[i][1]), OmicsData.MISSING_VALUE,
					array[i][2].charAt(0));
			sRNA.setTypeSrna(TypeSrna.ASrna);
			sRNA.getFoundIn().add("Wurtzel et al. 2012");
			sRNA.setLength(OmicsData.MISSING_VALUE);
			sRNA.getFeatures().put("Gene from (Wurtzel et al. 2012)", array[i][3]);
			sRNA.getFeatures().put("Gene to (Wurtzel et al. 2012)", array[i][4]);
			sRNA.getFeatures().put("Gene strand (Wurtzel et al. 2012)", array[i][5]);
			sRNA.getFeatures().put("Gene Locus (Wurtzel et al. 2012)", array[i][6]);
			sRNA.getFeatures().put("Product (Wurtzel et al. 2012)", array[i][7]);
			sRNA.getFeatures().put("Expressed? (Wurtzel et al. 2012)", array[i][8]);
			sRNA.getFeatures().put("L.inocua locus (Wurtzel et al. 2012)", array[i][9]);
			sRNA.getFeatures().put("Already known (Wurtzel et al. 2012)", array[i][10]);
			sRNA.getFeatures().put("note (Wurtzel et al. 2012)", array[i][11]);
			sRNAs.add(sRNA);
		}
		System.err.println("Wurtzel, read " + sRNAs.size() + " elements");
		return sRNAs;
	}

}
