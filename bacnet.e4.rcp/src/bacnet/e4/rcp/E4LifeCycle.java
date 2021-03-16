package bacnet.e4.rcp;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;

import bacnet.Database;
import bacnet.Database.TypeProject;

/**
 * This is a stub implementation containing e4 LifeCycle annotated methods.<br />
 * There is a corresponding entry in <em>plugin.xml</em> (under the
 * <em>org.eclipse.core.runtime.products' extension point</em>) that references
 * this class.
 **/
@SuppressWarnings("restriction")
public class E4LifeCycle {

	@PostContextCreate
	void postContextCreate(IApplicationContext appContext, Display display) {
		
		Database.getInstance().setTypeProject(TypeProject.Listeriomics);
		
		System.out.println("Run "+Database.getProjectName());
		
		//boolean open = openDialog(shell);
		boolean open = true;
		if (open){
			System.out.println("Open perspective");
			Database.getInstance().setPATH();			
			Database.getInstance().initDatabase(display.getActiveShell());
			
			// close the static splash screen
			appContext.applicationRunning();
		
		}else{
			// close the application
			System.exit(-1);
		}
	}
	
	@ProcessAdditions
	void ProcessAdditions(){
	
	}
		
	@PreSave
	void preSave(IEclipseContext workbenchContext) {
	}

	@ProcessAdditions
	void processAdditions(IEclipseContext workbenchContext) {
	}

	@ProcessRemovals
	void processRemovals(IEclipseContext workbenchContext) {
	}
}
