package bacnet.datamodel.sequence;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

public class Gene extends Sequence {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7176949538355147984L;

	private String oldLocusTag = "";
	private String newLocusTag = "";
	private String geneName = "";
	private String product = "";
	private String protein_id = "";
	private int lengthAA = -1;
	private String operon = "";
	private String cog = "";
	private double molMass = 0;
	private boolean pseudogene = false;

	public Gene() {
		super();
	}

	public Gene(String name, int from, int to, char strand) {
		super(name, from, to, strand);
		setType(SeqType.Gene);
		lengthAA = (this.getLength() / 3) - 1; // need to remove stop codon in the calcul of gene length in amino acid
	}

	/**
	 * Return the gene downstream on the + strand
	 * 
	 * @param gene
	 * @return
	 */
	public Gene getGeneDownstream() {
		int index = this.getChromosome().getGeneNameList().indexOf(this.getName());
		if (index != -1) {
			Gene geneDonwstream = this.getChromosome().getGenes()
					.get(this.getChromosome().getGeneNameList().get(index + 1));
			return geneDonwstream;
		}
		return null;
	}

	/**
	 * Return the gene upstream on the + strand
	 * 
	 * @param gene
	 * @return
	 */
	public Gene getGeneUpstream() {
		int index = this.getChromosome().getGeneNameList().indexOf(this.getName());
		if (index != -1) {
			Gene geneUpstream = this.getChromosome().getGenes()
					.get(this.getChromosome().getGeneNameList().get(index - 1));
			return geneUpstream;
		}
		return null;
	}

	/**
	 * Load a serialized Gene
	 * 
	 * @param fileName
	 * @return
	 */
	public static Gene load(String fileName) {
		try {
			// Create necessary input streams
			FileInputStream fis = new FileInputStream(fileName); // Read from file
			GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
			ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
			// Read in an object. It should be a vector of scribbles
			Gene seq = (Gene) in.readObject();
			in.close();
			return seq;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} // Close the stream.
	}

	/**
	 * Return a String containing getProduct() and getComment() information
	 * 
	 * @return
	 */
	public String getInfo() {
		String info = "";
		if (!this.getGeneName().equals("-")) {
			info += this.getGeneName() + ", ";
		}
		if (!this.getProduct().equals("")) {
			info += getProduct();
			if (!this.getComment().equals("")) {
				info += ", " + getComment();
			}
		} else {
			if (!this.getComment().equals("")) {
				info += getComment();
			}
		}
		return info;
	}

	public String getRASTinfo() {
		return this.getFeature("RAST_Product");
	}

	/*
	 * ******************************************************************* Getters
	 * and Setters
	 * *******************************************************************
	 */
	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getProtein_id() {
		return protein_id;
	}

	public void setProtein_id(String protein_id) {
		this.protein_id = protein_id;
	}

	public int getLengthAA() {
		return lengthAA;
	}

	public void setLengthAA(int lengthAA) {
		this.lengthAA = lengthAA;
	}

	public String getOperon() {
		return operon;
	}

	public void setOperon(String operon) {
		this.operon = operon;
	}

	public String getCog() {
		return cog;
	}

	public void setCog(String cog) {
		this.cog = cog;
	}

	public double getMolMass() {
		return molMass;
	}

	public void setMolMass(double molMass) {
		this.molMass = molMass;
	}

	public String getOldLocusTag() {
		return oldLocusTag;
	}

	public void setOldLocusTag(String oldLocusTag) {
		this.oldLocusTag = oldLocusTag;
	}

	public String getNewLocusTag() {
		return newLocusTag;
	}

	public void setNewLocusTag(String newLocusTag) {
		this.newLocusTag = newLocusTag;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isPseudogene() {
		return pseudogene;
	}

	public void setPseudogene(boolean pseudogene) {
		this.pseudogene = pseudogene;
	}
}
