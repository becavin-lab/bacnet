package bacnet.scripts.listeriomics;

import java.util.TreeSet;

import bacnet.datamodel.dataset.ExpressionData;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.dataset.OmicsData.TypeData;
import bacnet.datamodel.expdesign.BioCondition;
import bacnet.datamodel.proteomics.NTerm;
import bacnet.scripts.database.TranscriptomesCreation;

public class MainListeriomics {

	public static String PATH_TSSasRNA = ExpressionData.PATH_NGS_RAW + "asRNA-TSS-EGD-e/";
	public static String PATH_TSS_EGDe = ExpressionData.PATH_NGS_RAW + "TSS_data_EGDe.txt";
	public static String PATH_TSS_Innocua = ExpressionData.PATH_NGS_RAW + "TSS_data_Innocua.txt";
	public static String[] TSS_ARRAY = { "37C", "sigB", "prfA", "30C", "minusO2", "Stat" };
	public static String PATH_TermSeq_EGDe = ExpressionData.PATH_NGS_RAW + "TermSeq_data_EGDe.txt";
	public static String[] TermSeq_ARRAY = { "37C" };

	/**
	 * This method in an example of all methods which should be run to create your
	 * multi-omics website <br>
	 * It is never run !<br>
	 * <br>
	 * <li>All supplementary annotation files created
	 * <li>Genomes creation
	 * <li>Phylogenomics figures and otrholog search
	 * <li>BioCondition creation
	 * <li>Comparisons creation
	 * <li>Downloard and create ArrayExpress files
	 * <li>Process all Transcriptome files
	 * <li>Process all Prteomics files
	 * <li>Create Expression and Protein Atlas
	 * <li>Create co-expression network
	 * <li>Create summary files once all database elements have been created
	 * 
	 */
	public static void run() {

		/*
		 * Prepare genomics data
		 */
		// SrnaListeriomics.run();
		// GenomesListeriomics.run();
		// PhylogenyListeriomics.run();
		// Signature.convertAll();
		// RegulatorRegion.run();

		/*
		 * create biocondition, can be done in bacnet.e4.setup interface
		 */

//        BioConditionCreation.addExperimentFromTable(Database.getInstance().getBioConditionsArrayPath());
//        BioConditionCreation.createSummaryTranscriptomesTable();
//        BioConditionCreation.createSummaryProteomesTable();
//        
//        /*
//         * Create Comparisons, can be done in bacnet.e4.setup interface
//         */
//        BioConditionCreation.addComparisonFromTable(Database.getInstance().getExperimentComparisonTablePath());
//        BioConditionCreation.createSummaryTranscriptomesComparisonsTable();

		/*
		 * Prepare arrayexpress data, TSS, TermSeq and RiboSeq
		 */
		// ArrayExpress.run();
		// TSSNTermRiboSeqListeriomics.run();
		/*
		 * Convert all transcriptome files, can be done in bacnet.e4.setup interface
		 */

		/*
		 * Deal with unpublished datasetsssssss
		 */

		String logs = "";
//        ArrayList<String> bioconds = new ArrayList<>();
//        bioconds.add("Innocua_37C_Weizmann");
//        TranscriptomesCreation.addTranscriptomeToDatabase(bioconds, logs);
		TranscriptomesCreation.addTranscriptomeToDatabase(BioCondition.getAllBioConditionNames(), logs);
		// System.out.println(logs);
//        /*
//         * Prepare Proteomics data, can be done in bacnet.e4.setup interface
//         */
//        ProteomicsListeriomics.run();
//
//        /*
//         * Create co-expression network
//         */
//        SystemsBiologyListeriomics.run();
//
//
//        /*
//         * Create Summary tables and figures
//         */
//        SummaryListeriomics.run();
//
//        System.out.println("Listeriomics Done!");
	}

	/**
	 * Run after loading of workbench (use when display are needed)
	 */
	public static void runPostInit() {

		GenomesListeriomics.runPostInit();
		/*
		 * Display circular genome view for circos graph
		 */
		SystemsBiologyListeriomics.runPostInit();

		/*
		 * Summarize different table for the pubication
		 */
		SummaryListeriomics.run();
	}

	/**
	 * Create a matrix from TIS data for which each gene has its corresponding
	 * number of spectra
	 */
	public static void createMatrixFromTISData() {
		for (BioCondition bioCondition : BioCondition.getAllBioConditions()) {
			if (bioCondition.getTypeDataContained().contains(TypeData.NTerm)) {
				TreeSet<String> geneFound = new TreeSet<>();
				NTermData massSpec = NTermData.load(bioCondition.getName());
				System.out.println(bioCondition.getName());
				System.out.println(massSpec.getName());
				for (NTerm nTerm : massSpec.getNTerms()) {
					if (nTerm.getTypeOverlap().contains("aTIS") || nTerm.getTypeOverlap().contains("annotated TIS")) {
						geneFound.add(nTerm.getOverlap().split(":")[0].trim());
						System.out.println(nTerm.getOverlap().split(":")[0].trim());
					}
				}

				ExpressionMatrix matrix = new ExpressionMatrix("VALUE", geneFound.size());
				int i = 0;
				for (String gene : geneFound) {
					matrix.getRowNames().put(gene, i);
					i++;
				}
				for (NTerm nTerm : massSpec.getNTerms()) {
					if (nTerm.getTypeOverlap().contains("aTIS") || nTerm.getTypeOverlap().contains("annotated TIS")) {
						String gene = nTerm.getOverlap().split(":")[0].trim();
						double value = nTerm.getSpectra();
						matrix.setValue(matrix.getValue(gene, "VALUE") + value, gene, "VALUE");
					}
				}
				matrix.saveTab(OmicsData.PATH_PROTEOMICS_RAW + massSpec.getName() + ".txt", "Probes");
			}
		}
	}

}
