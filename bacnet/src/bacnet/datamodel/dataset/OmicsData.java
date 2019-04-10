package bacnet.datamodel.dataset;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import bacnet.Database;

/**
 * Abstract Class describing data related to omics data <br>
 * 
 * @author Chris
 *
 */
public class OmicsData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3093089920089151952L;

	/**
	 * Path for all streaming datasets = serialized data for BACNET platform
	 */
	public static String PATH_STREAMING = Database.getDATA_PATH() + "/StreamingData" + File.separator;
	/**
	 * Folder for all raw datasets
	 */
	public static String PATH_RAW = Database.getInstance().getPath() + "/RawData" + File.separator;
	/**
	 * Folder for all normalized datasets
	 */
	public static String PATH_NORM = Database.getInstance().getPath() + "/NormData" + File.separator;
	/**
	 * Folder for saving all comparisons datasets
	 */
	public static String PATH_COMPARISONS = Database.getInstance().getPath() + "/Comparisons" + File.separator;
	/**
	 * Folder for all ExpressionMatrix RAW datasets
	 */
	public static String PATH_EXPR_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "ExprMatrix"
			+ File.separator;
	/**
	 * Folder for all ExpressionMatrix RAW datasets
	 */
	public static String PATH_EXPR_NORM = Database.getInstance().getPath() + "/NormData" + File.separator + "ExprMatrix"
			+ File.separator;
	/**
	 * Folder for all ExpressionMatrix NORM datasets
	 */
	public static String PATH_GENEXPR_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "GeneExpr"
			+ File.separator;
	/**
	 * Folder for all GeneExpression COMPLETE datasets
	 */
	public static String PATH_GENEXPR_COMPLETE = Database.getInstance().getPath() + "/NormData" + File.separator
			+ "GeneExprComplete" + File.separator;
	/**
	 * Folder for all GeneExpression NORM datasets
	 */
	public static String PATH_GENEXPR_NORM = Database.getInstance().getPath() + "/NormData" + File.separator
			+ "GeneExpr" + File.separator;
	/**
	 * Folder for all Tiling RAW datasets
	 */
	public static String PATH_TILING_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "Tiling"
			+ File.separator;
	/**
	 * Folder for all Tiling NORM datasets
	 */
	public static String PATH_TILING_NORM = Database.getInstance().getPath() + "/NormData" + File.separator + "Tiling"
			+ File.separator;
	/**
	 * Folder for all RNASeq RAW datasets
	 */
	public static String PATH_NGS_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "NGS"
			+ File.separator;
	/**
	 * Folder for all RNASeq NORM datasets
	 */
	public static String PATH_NGS_NORM = Database.getInstance().getPath() + "/NormData" + File.separator + "NGS"
			+ File.separator;
	/**
	 * Folder for all Proteome RAW datasets
	 */
	public static String PATH_PROTEOMICS_RAW = Database.getInstance().getPath() + "/RawData" + File.separator
			+ "Proteome" + File.separator;
	/**
	 * Folder for all Proteome NORM datasets
	 */
	public static String PATH_PROTEOMICS_NORM = Database.getInstance().getPath() + "/NormData" + File.separator
			+ "Proteome" + File.separator;

	/**
	 * Name used for "median" data created from a set of reference condition
	 */
	public static final String GENERAL_WT_NAME = "EGDe_37C";
	/**
	 * Name of the Transcriptome data used as a reference data for comparisons
	 */
	public static final String GENERAL_WT_DATA = "EGDe_030510";
	/**
	 * Extension name of every OmicsData serialized
	 */
	public static final String EXTENSION = ".data";
	/**
	 * Every Found missing values are replaced by this number
	 */
	public static int MISSING_VALUE = -1000000;

	/**
	 * Enum referencing the different type of OmicsData possible:
	 * <li>GeneExpr
	 * <li>Tiling
	 * <li>RNASeq
	 * <li>TSS (Transcription Initiation Site)
	 * <li>NTerm (Translation Initiation Site)
	 * <li>Proteome (Matrix of protein found)
	 * <li>MassSpec (List of peptides found by mass spectrometry)
	 * <li>ExpressionMatrix (General matrix format)
	 * <li>unknown
	 * 
	 * @author christophebecavin
	 *
	 */
	public enum TypeData {
		GeneExpr, Tiling, RNASeq, DNASeq, TSS, TermSeq, RiboSeq, NTerm, Proteome, ExpressionMatrix, unknown
	}

	public enum ColNames {
		VALUE, FC, LOGFC, PVALUE, PADJ, GenomeElements
	}

	/**
	 * The BioCondition to which its related
	 */
	private String bioCondName = "";
	private String name = "";
	private String date = "01/01/2011";
	private String note = "";
	private TypeData type = TypeData.unknown;
	/**
	 * List of rawData from which it has been created
	 */
	private ArrayList<String> rawDatas = new ArrayList<String>();

	public OmicsData() {
	}

	public String getBioCondName() {
		return bioCondName;
	}

	public void setBioCondName(String bioCondName) {
		this.bioCondName = bioCondName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public TypeData getType() {
		return type;
	}

	public void setType(TypeData type) {
		this.type = type;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static String getExtension() {
		return EXTENSION;
	}

	public ArrayList<String> getRawDatas() {
		return rawDatas;
	}

	public void setRawDatas(ArrayList<String> rawDatas) {
		this.rawDatas = rawDatas;
	}

	/**
	 * Reinit static variables of OmicsData when database path is changed
	 */
	public static void initStaticVariables() {
		PATH_STREAMING = Database.getInstance().getDATA_PATH() + "/StreamingData" + File.separator;
		PATH_RAW = Database.getInstance().getPath() + "/RawData" + File.separator;
		PATH_NORM = Database.getInstance().getPath() + "/NormData" + File.separator;
		PATH_COMPARISONS = Database.getInstance().getPath() + "/Comparisons" + File.separator;
		PATH_EXPR_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "ExprMatrix" + File.separator;
		PATH_EXPR_NORM = Database.getInstance().getPath() + "/NormData" + File.separator + "ExprMatrix"
				+ File.separator;
		PATH_GENEXPR_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "GeneExpr" + File.separator;
		PATH_GENEXPR_COMPLETE = Database.getInstance().getPath() + "/NormData" + File.separator + "GeneExprComplete"
				+ File.separator;
		PATH_GENEXPR_NORM = Database.getInstance().getPath() + "/NormData" + File.separator + "GeneExpr"
				+ File.separator;
		PATH_TILING_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "Tiling" + File.separator;
		PATH_TILING_NORM = Database.getInstance().getPath() + "/NormData" + File.separator + "Tiling" + File.separator;
		PATH_NGS_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "NGS" + File.separator;
		PATH_NGS_NORM = Database.getInstance().getPath() + "/NormData" + File.separator + "NGS" + File.separator;
		PATH_PROTEOMICS_RAW = Database.getInstance().getPath() + "/RawData" + File.separator + "Proteome"
				+ File.separator;
		PATH_PROTEOMICS_NORM = Database.getInstance().getPath() + "/NormData" + File.separator + "Proteome"
				+ File.separator;

	}

}
