package awt.table;

import java.util.ArrayList;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.scripts.core.vennDiagram.VennDiagram;
import bacnet.utils.StringColor;


public enum ModelProviderRCP {
	INSTANCE;

	/**
	 * For venn diagram tool
	 */
	private VennDiagram vennDiagram = new VennDiagram();
	
	/**
	 * For Sequence tools need to exchange a sequence
	 */
	String sequence = "";
	
	/**
	 * For multi alignment a list of DNASequence is used
	 */
	ArrayList<DNASequence> dnaSequences = new ArrayList<DNASequence>();
	
	ExpressionMatrix matrix = new ExpressionMatrix();
	
	/**
	 * For multi alignment a list of ProteinSequence is used
	 */
	ArrayList<ProteinSequence> proteinSequences = new ArrayList<ProteinSequence>();
	
	/**
	 * StringColor are String embedded with color information, useful for sequence display
	 */
	StringColor textColor = new StringColor();
	
	private ModelProviderRCP() {
	}

	public VennDiagram getVennDiagram() {
		return vennDiagram;
	}

	public void setVennDiagram(VennDiagram vennDiagram) {
		this.vennDiagram = vennDiagram;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public ArrayList<DNASequence> getDnaSequences() {
		return dnaSequences;
	}

	public void setDnaSequences(ArrayList<DNASequence> dnaSequences) {
		this.dnaSequences = dnaSequences;
	}

	public ArrayList<ProteinSequence> getProteinSequences() {
		return proteinSequences;
	}

	public void setProteinSequences(ArrayList<ProteinSequence> proteinSequences) {
		this.proteinSequences = proteinSequences;
	}

	public StringColor getTextColor() {
		return textColor;
	}

	public void setTextColor(StringColor textColor) {
		this.textColor = textColor;
	}

	public ExpressionMatrix getMatrix() {
		return matrix;
	}

	public void setMatrix(ExpressionMatrix matrix) {
		this.matrix = matrix;
	}
	
	
}
