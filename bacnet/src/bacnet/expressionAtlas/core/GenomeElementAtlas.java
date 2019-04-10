package bacnet.expressionAtlas.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.datamodel.sequence.Sequence;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.Filter;

public class GenomeElementAtlas implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6277013816841128158L;

	public static String PATH = Database.getDATA_PATH() + "GenomeElementAtlas" + File.separator;
	public static double DEFAULT_LOGFC_CUTOFF = 1.5;

	private String name = "";

	private TreeSet<String> overBioConds = new TreeSet<>();
	private TreeSet<String> underBioConds = new TreeSet<>();
	private TreeSet<String> notDiffExpresseds = new TreeSet<>();

	public GenomeElementAtlas() {
	}

	/**
	 * For a specific sequence update the list of BioCondition in which the genome
	 * element is over-expressed, under-expressed or not differently expressed<br>
	 * The update will be dependent of the value of the input filter = cutoff for
	 * LogFC and p-value
	 * 
	 * @param seq
	 * @param filter object encapsulated cutoff values: cutOff1 = logFC , cutOff2 =
	 *               pvalue
	 * @param stat   if the statistical value should be taken, into account or not
	 */
	public GenomeElementAtlas(Sequence seq, Filter filter) {
		// System.out.println("genome element atlas");
		// System.out.println("Apply cut-off:"+filter.getCutOff1()+"
		// "+filter.getCutOff2());
		System.out.println(seq.getGenomeName());
		ExpressionMatrix logFCMatrix = Database.getInstance().getLogFCTranscriptomesTable(seq.getGenomeName());
		String genomeElement = seq.getName();
		if (logFCMatrix.getRowNames().containsKey(genomeElement)) {
			for (String bioCondName : logFCMatrix.getHeaders()) {
				double logFC = logFCMatrix.getValue(genomeElement, bioCondName);
				if (logFC <= -filter.getCutOff1()) {
					// under-expressed
					underBioConds.add(bioCondName);
				} else if (logFC > filter.getCutOff1()) {
					// over-expressed
					overBioConds.add(bioCondName);
				} else {
					notDiffExpresseds.add(bioCondName);
				}
			}
		}
	}

	/**
	 * Read all <code>Comparison</code> results and extract which gene is over and
	 * under expressed in it<br>
	 * For each gene, sRNA,asRNA, and cisReg, create a list of all
	 * <code>BioCondition</code> where it is over, under, and not differentially
	 * expressed<br>
	 * <br>
	 * 
	 * WANRING: OrganizeDatabase.organizeComparisonsData(exp) as to be run first!!!
	 * 
	 * @param expTemp
	 */
	public static void createAll(Experiment expTemp) {
		final Experiment exp = expTemp;
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (String geneTemp : TabDelimitedTableReader
				.readList(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDE_GENE"))) {
			final String gene = geneTemp;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TreeSet<String> overBioCondsList = new TreeSet<>();
					TreeSet<String> underBioCondsList = new TreeSet<>();
					TreeSet<String> notDiffExpressedsList = new TreeSet<>();
					for (BioCondition bioCond : exp.getBioConditions()) {
						for (String comparison : bioCond.getComparisonNames()) {
							String path = ComparisonAtlas.PATH_COMP + comparison + File.separator;
							ArrayList<String> listGeneOver = TabDelimitedTableReader
									.readList(path + comparison + "_Gene_GEonly_Over_List.txt");
							ArrayList<String> listGeneUnder = TabDelimitedTableReader
									.readList(path + comparison + "_Gene_GEonly_Under_List.txt");
							if (listGeneOver.contains(gene))
								overBioCondsList.add(comparison);
							else if (listGeneUnder.contains(gene))
								underBioCondsList.add(comparison);
							else
								notDiffExpressedsList.add(comparison);
						}
					}
					GenomeElementAtlas element = new GenomeElementAtlas();
					element.setName(gene);
					element.setOverBioConds(overBioCondsList);
					element.setUnderBioConds(underBioCondsList);
					element.setNotDiffExpresseds(notDiffExpressedsList);
					element.save(PATH + gene);

//					try {
//						XMLUtils.encodeToFile(element, PATH+gene+".xml");
//					} catch (FileNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			};
			executor.execute(runnable);
		}

		for (String sRNATemp : TabDelimitedTableReader
				.readList(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDe_SRNA"))) {
			final String sRNA = sRNATemp;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TreeSet<String> overBioCondsList = new TreeSet<>();
					TreeSet<String> underBioCondsList = new TreeSet<>();
					TreeSet<String> notDiffExpressedsList = new TreeSet<>();
					for (BioCondition bioCond : exp.getBioConditions()) {
						for (String comparison : bioCond.getComparisonNames()) {
							String path = ComparisonAtlas.PATH_COMP + comparison + File.separator;
							ArrayList<String> listGeneOver = TabDelimitedTableReader
									.readList(path + comparison + "_Srna_Over_List.txt");
							ArrayList<String> listGeneUnder = TabDelimitedTableReader
									.readList(path + comparison + "_Srna_Under_List.txt");
							if (listGeneOver.contains(sRNA))
								overBioCondsList.add(comparison);
							else if (listGeneUnder.contains(sRNA))
								underBioCondsList.add(comparison);
							else
								notDiffExpressedsList.add(comparison);
						}
					}
					GenomeElementAtlas element = new GenomeElementAtlas();
					element.setName(sRNA);
					element.setOverBioConds(overBioCondsList);
					element.setUnderBioConds(underBioCondsList);
					element.setNotDiffExpresseds(notDiffExpressedsList);
					element.save(PATH + sRNA);
				}
			};
			executor.execute(runnable);
		}

		for (String asRNATemp : TabDelimitedTableReader
				.readList(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDe_ASRNA"))) {
			final String asRNA = asRNATemp;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					System.out.println(asRNA);
					TreeSet<String> overBioCondsList = new TreeSet<>();
					TreeSet<String> underBioCondsList = new TreeSet<>();
					TreeSet<String> notDiffExpressedsList = new TreeSet<>();
					for (BioCondition bioCond : exp.getBioConditions()) {
						for (String comparison : bioCond.getComparisonNames()) {
							String path = ComparisonAtlas.PATH_COMP + comparison + File.separator;
							ArrayList<String> listGeneOver = TabDelimitedTableReader
									.readList(path + comparison + "_ASrna_Over_List.txt");
							ArrayList<String> listGeneUnder = TabDelimitedTableReader
									.readList(path + comparison + "_ASrna_Under_List.txt");
							if (listGeneOver.contains(asRNA))
								overBioCondsList.add(comparison);
							else if (listGeneUnder.contains(asRNA))
								underBioCondsList.add(comparison);
							else
								notDiffExpressedsList.add(comparison);
						}
					}
					GenomeElementAtlas element = new GenomeElementAtlas();
					element.setName(asRNA);
					element.setOverBioConds(overBioCondsList);
					element.setUnderBioConds(underBioCondsList);
					element.setNotDiffExpresseds(notDiffExpressedsList);
					element.save(PATH + asRNA);
				}
			};
			executor.execute(runnable);
		}

		for (String cisRegTemp : TabDelimitedTableReader
				.readList(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDE_CISREG"))) {
			final String cisReg = cisRegTemp;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					System.out.println(cisReg);
					TreeSet<String> overBioCondsList = new TreeSet<>();
					TreeSet<String> underBioCondsList = new TreeSet<>();
					TreeSet<String> notDiffExpressedsList = new TreeSet<>();
					for (BioCondition bioCond : exp.getBioConditions()) {
						for (String comparison : bioCond.getComparisonNames()) {
							String path = ComparisonAtlas.PATH_COMP + comparison + File.separator;
							ArrayList<String> listGeneOver = TabDelimitedTableReader
									.readList(path + comparison + "_CisReg_Over_List.txt");
							ArrayList<String> listGeneUnder = TabDelimitedTableReader
									.readList(path + comparison + "_CisReg_Under_List.txt");
							if (listGeneOver.contains(cisReg))
								overBioCondsList.add(comparison);
							else if (listGeneUnder.contains(cisReg))
								underBioCondsList.add(comparison);
							else
								notDiffExpressedsList.add(comparison);
						}
					}
					GenomeElementAtlas element = new GenomeElementAtlas();
					element.setName(cisReg);
					element.setOverBioConds(overBioCondsList);
					element.setUnderBioConds(underBioCondsList);
					element.setNotDiffExpresseds(notDiffExpressedsList);
					element.save(PATH + cisReg);
				}
			};
			executor.execute(runnable);
		}

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception");
		}
		System.err.println("All GenomeElementAtlas have been created");

	}
	/*
	 * ********************************** Serialization
	 * **********************************
	 */

	/**
	 * Read compressed, serialized data with a FileInputStream. Uncompress that data
	 * with a GZIPInputStream. Deserialize the vector of lines with a
	 * ObjectInputStream. Replace current data with new data, and redraw everything.
	 */
	public static GenomeElementAtlas load(String fileName) {
		try {
			// Create necessary input streams
			FileInputStream fis = new FileInputStream(fileName); // Read from file
			GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
			ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
			// Read in an object. It should be a vector of scribbles
			GenomeElementAtlas seq = (GenomeElementAtlas) in.readObject();
			in.close();
			return seq;
		} catch (Exception e) {
			System.err.println("Cannot read GenomeElementAtlas");
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
	 * ************************ GETTER AND SETTERS ************************
	 */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TreeSet<String> getOverBioConds() {
		return overBioConds;
	}

	public void setOverBioConds(TreeSet<String> overBioConds) {
		this.overBioConds = overBioConds;
	}

	public TreeSet<String> getUnderBioConds() {
		return underBioConds;
	}

	public void setUnderBioConds(TreeSet<String> underBioConds) {
		this.underBioConds = underBioConds;
	}

	public TreeSet<String> getNotDiffExpresseds() {
		return notDiffExpresseds;
	}

	public void setNotDiffExpresseds(TreeSet<String> notDiffExpresseds) {
		this.notDiffExpresseds = notDiffExpresseds;
	}

}
