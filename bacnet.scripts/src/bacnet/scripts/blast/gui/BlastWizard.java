package bacnet.scripts.blast.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.biojava3.core.exceptions.CompoundNotFoundError;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import bacnet.Database;
import bacnet.scripts.blast.BlastResultView;
import bacnet.swt.ResourceManager;
import bacnet.utils.FileUtils;

public class BlastWizard extends Wizard {

	private BlastQueryPage page;
	private BlastGenomeSelectionPage page2;
	// private BlastParametersPage page3;

	private Shell shell;
	private EPartService partService;

	public BlastWizard(Shell parentShell, EPartService partService) {
		this.shell = parentShell;
		this.partService = partService;
		setWindowTitle("Blast wizard");
		page = new BlastQueryPage(shell);
		page2 = new BlastGenomeSelectionPage();
		// page3 = new BlastParametersPage();
	}

	@Override
	public void addPages() {
		addPage(page);
		addPage(page2);
		// addPage(page3);
	}

	@Override
	public boolean performFinish() {
		// read the query sequence and test if it is a fasta file
		String fileName = Database.getTEMP_PATH() + "tempSeq.txt";

		FileUtils.saveText(page.getTextQuery().getText(), fileName);
		try {
			int type = 0;
//			if(page.btnProteinBlast.getSelection()) type=1;
//			if(page.btnBlastx.getSelection()) type = 2;
			runBlast(type);
			ResourceManager.openView(partService, BlastResultView.ID, BlastResultView.ID + Math.random());

		} catch (CompoundNotFoundError e1) {
			System.err.println("The query sequence cannot be read");
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public void runBlast(int typeT) {
		final int type = typeT;
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(false, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) {
					// get all genomes selected and make a fusion of them in Blast database format
					ArrayList<String> genomes = page2.getSelectedGenomes();
//					if(genomes.size()>0){
//						monitor.beginTask("Run Blast",genomes.size());
//						String title = "currentExp";
//						String query = Database.getTEMP_PATH()+"tempSeq.txt";
//						// get output format
//						BlastOutputTYPE outType = BlastOutputTYPE.ASN;
//						switch (page.cmbFileFormat.getSelectionIndex()) {
//							case 0:  outType = BlastOutputTYPE.ASN;							
//								break;
//							case 1: outType = BlastOutputTYPE.XML; break;
//							default:
//								break;
//						}
//						String blastResult = Database.getTEMP_PATH()+"resultBlast"+BlastOutput.fileExtension(outType);
//						String tempDatabase = Database.getTEMP_PATH()+"tempBlastDatabase";
//						ArrayList<String> databases = new ArrayList<String>();
//						for(String genome:genomes){
//							databases.add(GenomeNCBITools.getPATH()+genome+File.separator+genome);
//						}
//						System.out.println("Number of genomes "+genomes);
//						try {
//							//Blast.createFusionBlastDatabase(databases, title, tempDatabase,true);
//							String blast = Blast.blastN;
//							if(type==1) blast = Blast.blastP;
//							if(type==2) blast = Blast.blastX;
//							System.out.println(blast);
//							String[] args = {blast,"-query","\""+query+"\"","-db","\""+tempDatabase+"\"","-out","\""+blastResult+"\"","-outfmt",outType.ordinal()+""};
//							//String[] args = {Blast.blastN,"-query","\""+query+"\"","-db","\"D:/blast/wgs.52\"","-out","\""+blastResult+"\""};		
//							CMD.runProcess(args, true);
//							System.out.println("finish");
//						}
//						catch (NumberFormatException e) {
//							System.err.println("Cannot create database for this blast operation");
//						}
//						catch (IOException e) {
//							System.err.println("Cannot create database for this blast operation");
//						}
//					}
				}
			});
		} catch (InvocationTargetException e) {
			System.err.println("Problem in BLast");
		} catch (InterruptedException e) {
			System.err.println("Problem in BLast");
		}
	}

}
