package bacnet.scripts.phylogeny;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.forester.phylogeny.Phylogeny;

import bacnet.Database;

/**
 * Handler to manage button for displaying phylogeny in forester
 * @author christophebecavin
 *
 */
@Deprecated
public class DisplayPhylogenyHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

//		// Creating a new rooted tree with two external nodes.
		final Phylogeny phy = PhylogenyTools.getNCBIGenomePhylogeny();
		Phylogeny[] phys = { phy };
//	        // Displaying the newly created tree with Archaeopteryx.
		org.forester.archaeopteryx.Archaeopteryx.createApplication(phys,
				Database.getANALYSIS_PATH() + "/Egd-e Annotation/_aptx_configuration_file.txt",
				"Phylogeny with forester");
		// GenomeFolderTools.createRfamVSNCBIGenomeTable();
		// GenomeFolderTools.addIMGtoRfamVSNCBIGenomeTable();
		// PhylogenyJeff.displayJeffPhylogeny();

		return null;
	}

}
