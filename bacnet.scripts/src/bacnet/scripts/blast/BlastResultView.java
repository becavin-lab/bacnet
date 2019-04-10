package bacnet.scripts.blast;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;

public class BlastResultView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3579398886352775140L;
	public static final String ID = "ListTranscript.BlastResultView";
	private Button btnSave;
	private Label lblSave;
	private Browser browser;
	private Combo combo;
	private Button btnRefreshDisplay;

	private String archiveFile = Blast.getBLAST_RESULT_PATH() + BlastOutput.fileExtension(BlastOutputTYPE.ASN);
	private Button btnLoad;
	private Button btnExport;

	@Inject
	EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	public BlastResultView() {

	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		browser = new Browser(parent, SWT.NONE);
		// browser.setJavascriptEnabled(true);
		GridData gd_browser = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_browser.heightHint = 1000;
		gd_browser.widthHint = 1000;
		browser.setLayoutData(gd_browser);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));

		btnRefreshDisplay = new Button(composite, SWT.NONE);
		btnRefreshDisplay.setText("Refresh display");
		btnRefreshDisplay.addSelectionListener(this);
		combo = new Combo(composite, SWT.NONE);
		combo.setItems(BlastOutput.outputName);
		combo.select(0);

		btnExport = new Button(composite, SWT.NONE);
		btnExport.setText("export");
		btnExport.addSelectionListener(this);
		new Label(composite, SWT.NONE);
		btnLoad = new Button(composite, SWT.NONE);
		btnLoad.setText("Load Blast archive");
		btnLoad.addSelectionListener(this);

		btnSave = new Button(composite, SWT.NONE);
		btnSave.setText("Save Blast archive");
		btnSave.addSelectionListener(this);
		new Label(composite, SWT.NONE);

		lblSave = new Label(composite, SWT.NONE);

		loadBlastData();

	}

	public void loadBlastData() {
//		final boolean html = true;
//	     final BlastOutputTYPE type = BlastOutput.BlastOutputTYPE.values()[combo.getSelectionIndex()];
//	     ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
//		try {
//			dialog.run(false, true, new IRunnableWithProgress() {
//				@Override
//				public void run(IProgressMonitor monitor) {
//					monitor.beginTask("Creating Blast result file",0);
//					String out = BlastOutput.convertOuput(archiveFile, Blast.getBLAST_RESULT_PATH()+"-out", html, type);
//					browser.setUrl(out);
//					
//				}
//			});
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
//		if(e.getSource()==btnSave){
//			FileDialog fd = new FileDialog(shell, SWT.SAVE);
//		     fd.setText("Save Blast archive results: ");
//		     fd.setFilterPath(Database.getInstance().getPATH());
//		     String[] filterExt = {"*.asn","*.*" };
//			fd.setFilterExtensions(filterExt);
//		     String fileName = fd.open();
//			try {
//				if(fileName!=""){
//					// delete balise
//					FileUtils.saveText(FileUtils.readText(archiveFile),fileName);
//					lblSave.setText("Saved in: "+fileName);
//					archiveFile = fileName;
//				}
//			}catch (Exception e1) {
//				System.out.println("Cannot save this file");
//				lblSave.setText("Cannot save this file");
//			}		
//		}else if(e.getSource()==btnLoad){
//			FileDialog fd = new FileDialog(shell, SWT.OPEN);
//		     fd.setText("Open Blast archive results: ");
//		     fd.setFilterPath(Database.getInstance().getPATH());
//		     String[] filterExt = {"*.asn","*.*" };
//			fd.setFilterExtensions(filterExt);
//		     String fileName = fd.open();
//			try {
//				if(fileName!=""){
//					archiveFile = fileName;
//					lblSave.setText("Load in: "+fileName);
//				}
//			}catch (Exception e1) {
//				System.out.println("Cannot save this file");
//				lblSave.setText("Cannot save this file");
//			}		
//		}else if(e.getSource()==btnRefreshDisplay){
//			loadBlastData();
//		}else if(e.getSource()==btnExport){
//			FileDialog fd = new FileDialog(shell, SWT.SAVE);
//		     fd.setText("Save Blast results: ");
//		     fd.setFilterPath(Database.getInstance().getPATH());
//		     BlastOutputTYPE type = BlastOutput.BlastOutputTYPE.values()[combo.getSelectionIndex()];
//		     String[] filterExt = {BlastOutput.fileExtension(type),"*.*" };
//			fd.setFilterExtensions(filterExt);
//		     String fileName = fd.open();
//			try {
//				if(fileName!=""){
//					// delete balise if it is not a
////					FileUtils.saveText(browser.getText().replaceAll("<[^>]*>", ""), fileName);
////					lblSave.setText("Saved in: "+fileName);
//				}
//			}catch (Exception e1) {
//				System.out.println("Cannot save this file");
//				lblSave.setText("Cannot save this file");
//			}		
//		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
