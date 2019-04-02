package bacnet.expressionAtlas.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.expdesign.Experiment;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.Filter;
import bacnet.utils.XMLUtils;

/**
 * A ComparisonAtlas is related to a Comparison object and gives the list of genome elements over, under and not differentially expressed 
 * @author UIBC
 *
 */
public class ComparisonAtlas implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3461420201661554414L;

	public static String PATH = Database.getDATA_PATH()+"BioConditionAtlas"+File.separator;
	public static String PATH_COMP = Database.getDATA_PATH()+"ComparisonData"+File.separator;

	private String name = "";

	private final ArrayList<String> overExpressed = new ArrayList<>();
	private final ArrayList<String> underExpressed = new ArrayList<>();
	private final ArrayList<String> excludeRows = new ArrayList<>();

	/*
	 * Arguments used only for the creation of the database
	 */
	private String[] overGenes = new String[0];
	private String[] underGenes = new String[0];
	private String[] notDiffExpressedGenes = new String[0];

	private String[] overSrnas = new String[0];
	private String[] underSrnas = new String[0];
	private String[] notDiffExpressedSrnas = new String[0];

	private String[] overASrnas = new String[0];
	private String[] underASrnas = new String[0];
	private String[] notDiffExpressedASrnas = new String[0];

	private String[] overCisRegs = new String[0];
	private String[] underCisRegs = new String[0];
	private String[] notDiffExpressedCisRegs = new String[0];





	public ComparisonAtlas(){

	}

	/**
	 * For a specific sequence update the list of BioCondition in which the genome element is over-expressed, under-expressed or not differently expressed<br>
	 * The update will be dependent of the value of the input filter = cutoff for LogFC and p-value
	 * @param seq
	 * @param filter object encapsulated cutoff values: cutOff1 = logFC , cutOff2 = pvalue
	 * @param stat if the statistical value should be taken, into account or not
	 */

	/**
	 * For a specific biological condition comparison update the list of genome elements in which the genome element is over-expressed, under-expressed or not differently expressed<br>
	 * The update will be dependent of the value of the input filter = cutoff for LogFC and p-value
	 * @param logFCMatrix
	 * @param excludeColumns
	 * @param comparisonName
	 * @param filter
	 */
	public ComparisonAtlas(ExpressionMatrix logFCMatrix, ArrayList<String> excludeColumns,String comparisonName,Filter filter){
		//System.out.println("comparison atlas");

		//System.out.println("Apply Cut-off:"+filter.getCutOff1()+"  "+filter.getCutOff2());
		System.out.println(comparisonName);
		if(comparisonName.equals("All Columns")){
			for(String header : logFCMatrix.getHeaders()){
				if(!excludeColumns.contains(header)){
					for(String genomeElement : logFCMatrix.getRowNames().keySet()){
						if(!underExpressed.contains(genomeElement) && !overExpressed.contains(genomeElement)){
							double logFC = logFCMatrix.getValue(genomeElement, header);
							if(logFC <= -filter.getCutOff1()){
								// Under-expressed
								underExpressed.add(genomeElement);
								if(excludeRows.contains(genomeElement)){
									excludeRows.remove(genomeElement);
								}
							}else if(logFC>filter.getCutOff1()){
								// Over-expressed
								overExpressed.add(genomeElement);
								if(excludeRows.contains(genomeElement)){
									excludeRows.remove(genomeElement);
								}
							}else{
								//System.out.println("logfc: "+logFC+" "+genomeElement);
								excludeRows.add(genomeElement);
							}
						}
				}
				}	
			}
		}else{
			if(logFCMatrix.getHeaders().contains(comparisonName)){
				for(String genomeElement : logFCMatrix.getRowNames().keySet()){
					if(!excludeRows.contains(genomeElement)){
						double logFC = logFCMatrix.getValue(genomeElement, comparisonName);
						if(logFC <= -filter.getCutOff1()){
							// under-expressed
							underExpressed.add(genomeElement);
						}else if(logFC>filter.getCutOff1()){
							// over-expressed
							overExpressed.add(genomeElement);
						}else{
							
							excludeRows.add(genomeElement);
						}
					}
				}
			}
		}
		
	}

	/**
	 * For each comparison create a <code>Comparison</code> object containing the list of all differentially expressed genes, sRNAs, asRNAs, and cisRegs<br><br>
	 * 
	 * WANRING: OrganizeDatabase.organizeComparisonsData(exp) as to be run first!!!
	 * @param exp
	 */
	public static void createAll(Experiment exp){
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for(BioCondition bioCondTemp : exp.getBioConditions()){

			final BioCondition bioCond = bioCondTemp;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					ComparisonAtlas element = new ComparisonAtlas();
					element.setName(bioCond.getName());
					for(String comparison : bioCond.getComparisonNames()){
						String path = PATH_COMP+comparison+File.separator;
						/*
						 * Genes
						 */
						ArrayList<String> listOver = TabDelimitedTableReader.readList(path+comparison+"_Gene_GEonly_Over_List.txt");
						ArrayList<String> listUnder = TabDelimitedTableReader.readList(path+comparison+"_Gene_GEonly_Under_List.txt");
						element.setOverGenes(listOver.toArray(new String[0]));
						element.setUnderGenes(listUnder.toArray(new String[0]));
						ArrayList<String> genes = TabDelimitedTableReader.readList(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDE_GENE"));
						ArrayList<String> notdiff = new ArrayList<>();
						for(String gene : genes){
							if(!listOver.contains(gene) && !listUnder.contains(gene)){
								notdiff.add(gene);
							}
						}
						element.setNotDiffExpressedGenes(notdiff.toArray(new String[0]));

						/*
						 * Srnas
						 */
						listOver = TabDelimitedTableReader.readList(path+comparison+"_Srna_Over_List.txt");
						listUnder = TabDelimitedTableReader.readList(path+comparison+"_Srna_Under_List.txt");
						element.setOverSrnas(listOver.toArray(new String[0]));
						element.setUnderSrnas(listUnder.toArray(new String[0]));
						genes = TabDelimitedTableReader.readList(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDe_SRNA"));
						notdiff = new ArrayList<>();
						for(String gene : genes){
							if(!listOver.contains(gene) && !listUnder.contains(gene)){
								notdiff.add(gene);
							}
						}
						element.setNotDiffExpressedSrnas(notdiff.toArray(new String[0]));

						/*
						 * ASrnas
						 */
						listOver = TabDelimitedTableReader.readList(path+comparison+"_ASrna_Over_List.txt");
						listUnder = TabDelimitedTableReader.readList(path+comparison+"_ASrna_Under_List.txt");
						element.setOverASrnas(listOver.toArray(new String[0]));
						element.setUnderASrnas(listUnder.toArray(new String[0]));
						genes = TabDelimitedTableReader.readList(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDe_ASRNA"));
						notdiff = new ArrayList<>();
						for(String gene : genes){
							if(!listOver.contains(gene) && !listUnder.contains(gene)){
								notdiff.add(gene);
							}
						}
						element.setNotDiffExpressedASrnas(notdiff.toArray(new String[0]));

						/*
						 * CisRegs
						 */
						listOver = TabDelimitedTableReader.readList(path+comparison+"_CisReg_Over_List.txt");
						listUnder = TabDelimitedTableReader.readList(path+comparison+"_CisReg_Under_List.txt");
						element.setOverCisRegs(listOver.toArray(new String[0]));
						element.setUnderCisRegs(listUnder.toArray(new String[0]));
						genes = TabDelimitedTableReader.readList(Database.getDATA_PATH() + Database.getInstance().getDatabaseFeatures().get("EGDE_CISREG"));
						notdiff = new ArrayList<>();
						for(String gene : genes){
							if(!listOver.contains(gene) && !listUnder.contains(gene)){
								notdiff.add(gene);
							}
						}
						element.setNotDiffExpressedCisRegs(notdiff.toArray(new String[0]));
						element.save(PATH+comparison);
						XMLUtils.encodeToFile(element, PATH+comparison+".xml");
					}
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
		System.err.println("All ComparisonAtlas have been created");
	}


	/*
	 * **********************************
	 * Serialization
	 * **********************************
	 */

	/**
	 * Read compressed, serialized data with a FileInputStream.
	 * Uncompress that data with a GZIPInputStream.
	 * Deserialize the vector of lines with a ObjectInputStream.
	 * Replace current data with new data, and redraw everything.
	 */
	public static ComparisonAtlas load(String fileName) {
		try {
			// Create necessary input streams
			FileInputStream fis = new FileInputStream(fileName); // Read from file
			GZIPInputStream gzis = new GZIPInputStream(fis);     // Uncompress
			ObjectInputStream in = new ObjectInputStream(gzis);  // Read objects
			// Read in an object.  It should be a vector of scribbles
			ComparisonAtlas seq = (ComparisonAtlas)in.readObject();
			in.close();
			return seq;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}                    // Close the stream.

	}


	/**
	 * Serialize the vector of lines with an ObjectOutputStream.
	 * Compress the serialized objects with a GZIPOutputStream.
	 * Write the compressed, serialized data to a file with a FileOutputStream.
	 * Don't forget to flush and close the stream.
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
			out.writeObject(this);      	// Write the entire Vector of scribbles
			out.flush();                 		// Always flush the output.
			out.close();                 		// And close the stream.
		}
		// Print out exceptions.  We should really display them in a dialog...
		catch (IOException e) { System.out.println(e); }
	}


	/*
	 * ************************
	 *     GETTER AND SETTERS
	 * ************************
	 */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getOverGenes() {
		return overGenes;
	}


	public void setOverGenes(String[] overGenes) {
		this.overGenes = overGenes;
	}


	public String[] getUnderGenes() {
		return underGenes;
	}


	public void setUnderGenes(String[] underGenes) {
		this.underGenes = underGenes;
	}


	public String[] getNotDiffExpressedGenes() {
		return notDiffExpressedGenes;
	}


	public void setNotDiffExpressedGenes(String[] notDiffExpressedGenes) {
		this.notDiffExpressedGenes = notDiffExpressedGenes;
	}


	public String[] getOverSrnas() {
		return overSrnas;
	}


	public void setOverSrnas(String[] overSrnas) {
		this.overSrnas = overSrnas;
	}


	public String[] getUnderSrnas() {
		return underSrnas;
	}


	public void setUnderSrnas(String[] underSrnas) {
		this.underSrnas = underSrnas;
	}


	public String[] getNotDiffExpressedSrnas() {
		return notDiffExpressedSrnas;
	}


	public void setNotDiffExpressedSrnas(String[] notDiffExpressedSrnas) {
		this.notDiffExpressedSrnas = notDiffExpressedSrnas;
	}


	public String[] getOverASrnas() {
		return overASrnas;
	}


	public void setOverASrnas(String[] overASrnas) {
		this.overASrnas = overASrnas;
	}


	public String[] getUnderASrnas() {
		return underASrnas;
	}


	public void setUnderASrnas(String[] underASrnas) {
		this.underASrnas = underASrnas;
	}


	public String[] getNotDiffExpressedASrnas() {
		return notDiffExpressedASrnas;
	}


	public void setNotDiffExpressedASrnas(String[] notDiffExpressedASrnas) {
		this.notDiffExpressedASrnas = notDiffExpressedASrnas;
	}

	public String[] getOverCisRegs() {
		return overCisRegs;
	}

	public void setOverCisRegs(String[] overCisRegs) {
		this.overCisRegs = overCisRegs;
	}

	public String[] getUnderCisRegs() {
		return underCisRegs;
	}

	public void setUnderCisRegs(String[] underCisRegs) {
		this.underCisRegs = underCisRegs;
	}

	public String[] getNotDiffExpressedCisRegs() {
		return notDiffExpressedCisRegs;
	}

	public void setNotDiffExpressedCisRegs(String[] notDiffExpressedCisRegs) {
		this.notDiffExpressedCisRegs = notDiffExpressedCisRegs;
	}


	public ArrayList<String> getExcludeRows() {
		return excludeRows;
	}

	public ArrayList<String> getOverExpressed() {
		return overExpressed;
	}

	public ArrayList<String> getUnderExpressed() {
		return underExpressed;
	}

}
