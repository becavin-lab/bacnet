package bacnet.e4.rcp.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import bacnet.Database;
import bacnet.Leishmania;
import bacnet.datamodel.annotation.Annotation;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.scripts.listeriomics.MainListeriomics;

public class TestPart {

	@Inject
	EPartService partService;
	
	@ Inject
	@ Named (IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	
	/**
	 * Run a test before initilization of the GUI
	 * (run in ApplicationWorkbenchAdvisor.initialize() )
	 */
	@Inject
	public TestPart(){
		try {

			
//			MultiSequenceBlastProtein.run("D:/PasteurNoSVN/Alex/SmallORF.txt", true,1);
//			MultiSequenceBlastProtein.run("D:/PasteurNoSVN/Alex/ORF.txt", false,0.0001);
			
//			NatalieBacteriocin.run();
//			Genome genome = Genome.loadGenome("Listeria monocytogenes 6179");
//			Chromosome chromo = genome.getChromosomes().get(0);
			
			ExpressionMatrix matrix = ExpressionMatrix.loadTab(Database.getInstance().getDATA_PATH()+"Ama and Proma DNA.txt",true);
			matrix = Annotation.addAnnotationMultiChromosome(matrix, Genome.loadGenome(Leishmania.Donovani_NAME));
			matrix.saveTab(Database.getInstance().getDATA_PATH()+"Ama and Proma DNA annot.txt", "Locus");
			
			
//			MainListeriomics.run();

//			VesicleMain.run();
			
//			SDProfile.run();		
			
//			SabrinaScripts.run();
	
//			DramsiStreptoRepeats.run();

//			NTermMain.run();

			
			System.out.println("Init test finished");
			
		}catch (Exception e) {
			System.err.println("Test crashed!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Run a test after initialization of the GUI
	 * (run in TestHandler )
	 */
	@PostConstruct
	public void createPart(){
//		SearchPeptideHuman.searchAllPeptides();
//		Rickettsia.searchallRickA();
//		PrfAUtils.searchAllPrfA();
		System.out.println("yo");
		MainListeriomics.runPostInit();
		
//		NTermMain.run();
//		ExpressionMatrix matrix = ExpressionMatrix.loadTab(Project.getPATH()+"NoSVN/miRNA Cristel/table 06102014/table.txt", true);
//		TableAWTView.displayMatrix(matrix, "table");
		
//		TableSWTView.displayMatrix(ExpressionMatrix.loadTab(Project.getTEMP_PATH()+"GeneExprHouseKeeping_QC.txt", true), "GeneExprHouseKeeping_QC");
//		TableSWTView.displayMatrix(ExpressionMatrix.loadTab(Project.getTEMP_PATH()+"TilingHouseKeeping_QC.txt", true), "TilingHouseKeeping_QC");
//		TableSWTView.displayMatrix(ExpressionMatrix.loadTab(Project.getTEMP_PATH()+"GeneExprHouseKeeping_WT_QC.txt", true), "GeneExprHouseKeeping_WT_QC");
//		TableSWTView.displayMatrix(ExpressionMatrix.loadTab(Project.getTEMP_PATH()+"TilingHouseKeeping_WT_QC.txt", true), "TilingHouseKeeping_WT_QC");
	}
	
}
           