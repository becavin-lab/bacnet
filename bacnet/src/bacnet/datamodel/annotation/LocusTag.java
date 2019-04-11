package bacnet.datamodel.annotation;

import java.io.File;
import java.util.ArrayList;

import bacnet.Database;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.TabDelimitedTableReader;

/**
 * Since the update of RefSeq database all the locustag have been changed <br>
 * Here is some methods to deal with this change
 * 
 * @author cbecavin
 *
 */
public class LocusTag {

	public static String NewLocusTagArray = Database.getANNOTATIONDATA_PATH() + "NewLocusTagChange.txt";
	public static String OldLocusTagArray = Database.getANNOTATIONDATA_PATH() + "OldLocusTagChange.txt";

	/*
	 * 
	 */
	public static void addLocusChange() {
		/*
		 * Look at the current GFF files and add old locustag if found
		 */
		ArrayList<String> cannotFind = new ArrayList<>();
		String[][] array = TabDelimitedTableReader.read(OldLocusTagArray);
		for (int i = 0; i < array.length; i++) {
			String genomeName = array[i][0];
			String locusTag = array[i][1];
			String oldLocusTag = array[i][2];
			Genome genome = Genome.loadGenome(genomeName);
			// System.out.println("ey"+locusTag);
			Gene gene = (Gene) genome.getElement(locusTag);
			if (gene != null) {
				gene.setOldLocusTag(oldLocusTag);
				String pathSequences = Database.getGENOMES_PATH() + genomeName + File.separator + "Sequences"
						+ File.separator + gene.getName();
				// System.out.println(pathSequences);
				gene.save(pathSequences);

			} else {
				gene = (Gene) genome.getElement(oldLocusTag);
				if (gene != null) {
				} else {
					System.err.println("Cannot found: " + genome + " " + locusTag);
					cannotFind.add(genomeName + "\t" + oldLocusTag + "\t" + locusTag + "\t");
				}
			}
		}
		TabDelimitedTableReader.saveList(cannotFind,
				Database.getANNOTATIONDATA_PATH() + "CannotFindOldLocusTagChange.txt");

		/*
		 * Look at the new GFF locustag names and add the new names if found
		 */
		array = TabDelimitedTableReader.read(NewLocusTagArray);
		cannotFind.clear();
		for (int i = 0; i < array.length; i++) {
			String genomeName = array[i][0];
			String newlocusTag = array[i][1];
			String locusTag = array[i][2];
			Genome genome = Genome.loadGenome(genomeName);
			// System.out.println("ey"+locusTag);
			Gene gene = (Gene) genome.getElement(locusTag);
			if (gene != null) {
				gene.setNewLocusTag(newlocusTag);
				String pathSequences = Database.getGENOMES_PATH() + genomeName + File.separator + "Sequences"
						+ File.separator + gene.getName();
				// System.out.println(pathSequences);
				gene.save(pathSequences);
			} else {
				gene = (Gene) genome.getElement(newlocusTag);
				if (gene != null) {

				} else {
					System.err.println("Cannot found: " + genome + " " + locusTag);
					cannotFind.add(genomeName + "\t" + newlocusTag + "\t" + locusTag + "\t");
				}
			}
		}
		TabDelimitedTableReader.saveList(cannotFind,
				Database.getANNOTATIONDATA_PATH() + "CannotFindNewLocusTagChange.txt");
	}

	/**
	 * Create a HashMap with all "old_locus_tag" and "locus_tag" from the current
	 * GFF folder Listeriomics/NCBIGenomes/
	 */
	public static void parseOldLocusTagChange() {
		String PATH_GENOME = GenomeNCBI.PATH_GENOMES;
		ArrayList<String> listLocusTag = new ArrayList<>();
		for (String genomeName : Genome.getAvailableGenomes()) {
			File path = new File(PATH_GENOME + genomeName);
			System.out.println(PATH_GENOME + genomeName);
			for (File file : path.listFiles()) {
				if (file.getAbsolutePath().endsWith(".gff")) {
					ArrayList<String> gffList = TabDelimitedTableReader.readList(file.getAbsolutePath(), true);
					for (String line : gffList) {
						if (line.contains("gene") && line.contains(";locus_tag=") && line.contains(";old_locus_tag=")) {
							line = line.replaceAll("\"", "");
							String[] allFeatures = line.split(";");
							String locusTag = "";
							String oldLocusTag = "";
							for (String feature : allFeatures) {
								if (feature.startsWith("locus_tag")) {
									locusTag = feature.replaceFirst("locus_tag=", "");
								}
								if (feature.startsWith("old_locus_tag")) {
									oldLocusTag = feature.replaceFirst("old_locus_tag=", "");
								}
							}

							if (!locusTag.equals("")) {
								// System.out.println(l+" --> "+locusTag);
								listLocusTag.add(genomeName + "\t" + locusTag + "\t" + oldLocusTag);
								// listLocusTag.add(genomeName+"\t"+locusTag);
							}
							// System.out.println(line);
						}
					}
				}
			}
		}
		TabDelimitedTableReader.saveList(listLocusTag, OldLocusTagArray);
	}

	/**
	 * Create a HashMap with all "old_locus_tag" and "locus_tag" from the new GFF
	 * folder Listeriomics/NCBIGenomesNew/
	 */
	public static void parseNewLocusTagChange() {
		ArrayList<String> listLocusTag = new ArrayList<>();
		for (String genomeName : Genome.getAvailableGenomes()) {
			File path = new File(GenomeNCBI.PATH_GENOMES_NEW + genomeName);
			System.out.println(GenomeNCBI.PATH_GENOMES_NEW + genomeName);
			for (File file : path.listFiles()) {
				if (file.getAbsolutePath().endsWith(".gff")) {
					ArrayList<String> gffList = TabDelimitedTableReader.readList(file.getAbsolutePath(), true);
					for (String line : gffList) {
						if (line.contains("gene") && line.contains(";locus_tag=") && line.contains(";old_locus_tag=")) {
							line = line.replaceAll("\"", "");
							String[] allFeatures = line.split(";");
							String locusTag = "";
							String oldLocusTag = "";
							for (String feature : allFeatures) {
								if (feature.startsWith("locus_tag")) {
									locusTag = feature.replaceFirst("locus_tag=", "");
								}
								if (feature.startsWith("old_locus_tag")) {
									oldLocusTag = feature.replaceFirst("old_locus_tag=", "");
								}
							}

							if (!locusTag.equals("")) {
								// System.out.println(l+" --> "+locusTag);
								listLocusTag.add(genomeName + "\t" + locusTag + "\t" + oldLocusTag);
								// listLocusTag.add(genomeName+"\t"+locusTag);
							}
							// System.out.println(line);
						}
					}
				}
			}
		}
		TabDelimitedTableReader.saveList(listLocusTag, NewLocusTagArray);
	}

}
