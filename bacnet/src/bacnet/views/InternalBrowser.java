package bacnet.views;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import bacnet.raprcp.NavigationManagement;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.swt.ResourceManager;

public class InternalBrowser {

	public static final String ID = "bacnet.InternalBrowser"; //$NON-NLS-1$
	private Browser browser;
	private Link link;
	private String downloadLink = "";
	private String fileName = "";

	@Inject
	EPartService partService;

	@Inject
	public InternalBrowser() {
	}

	@PostConstruct
	public void createPartControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		link = new Link(composite, SWT.NONE);
		link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		link.setText("Select whole table (Crtl-A or Apple-A) and copy-paste it into Excel-like softwares");
		link.addListener(SWT.Selection, new Listener() {
			/**
			* 
			*/
			private static final long serialVersionUID = -4741739227220194568L;

			public void handleEvent(Event event) {
				String url = event.text;
				System.out.println("Selection: " + url);
				NavigationManagement.openURLInExternalBrowser(url, partService);
				System.out.println("Downloaded: " + url);
			}
		});
		browser = new Browser(composite, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}

	public void loadHTML(String text) {
		browser.setText(text);
		browser.redraw();
	}

	public void loadURL(String url) {
		browser.setUrl(url);
		browser.redraw();
	}

	public void setTitle(String text) {
		link.setText(text);
	}

	/**
	 * Open an HTML version of the String array
	 * 
	 * @param array
	 * @param title
	 */
	public static void openTable(String[][] array, String title, EPartService partService) {
		String id = InternalBrowser.ID + Math.random();
		// initiate view
		ResourceManager.openView(partService, InternalBrowser.ID, id);
		// update data
		MPart part = partService.findPart(id);
		InternalBrowser view = (InternalBrowser) part.getObject();

		String html = TabDelimitedTableReader.getInHTML(array, title);
		view.loadHTML(html);
		view.setTitle(title);

	}

	/**
	 * Open an HTML version of the String ArrayList
	 * 
	 * @param array
	 * @param title
	 */
	public static void openList(ArrayList<String> list, String title, EPartService partService) {
		String id = InternalBrowser.ID + Math.random();
		// initiate view
		ResourceManager.openView(partService, InternalBrowser.ID, id);
		// update data
		MPart part = partService.findPart(id);
		InternalBrowser view = (InternalBrowser) part.getObject();

		String html = TabDelimitedTableReader.getInHTML(list, title);
		view.loadHTML(html);
		view.setTitle(title);

	}

	/**
	 * Open an HTML version of the String
	 * 
	 * @param array
	 * @param title
	 */
	public static void openText(String text, String title, EPartService partService) {
		String id = InternalBrowser.ID + Math.random();
		// initiate view
		ResourceManager.openView(partService, InternalBrowser.ID, id);
		// update data
		MPart part = partService.findPart(id);
		InternalBrowser view = (InternalBrowser) part.getObject();
		view.loadHTML(text);
		view.setTitle(title);
		System.out.println(title);
	}

	/**
	 * Open an HTML file
	 * 
	 * @param htmlText textfile with HTML code
	 * @param title
	 */
	public static void openHTML(String htmlText, String title, EPartService partService) {
		String id = InternalBrowser.ID + Math.random();
		// initiate view
		ResourceManager.openView(partService, InternalBrowser.ID, id);
		// update data
		MPart part = partService.findPart(id);
		InternalBrowser view = (InternalBrowser) part.getObject();

		view.loadHTML(htmlText);
		view.setTitle(title);

	}

	/**
	 * Open an HTML file
	 * 
	 * @param htmlText textfile with HTML code
	 * @param title
	 */
	public static MPart openURL(String url, String title, EPartService partService) {
		String id = InternalBrowser.ID + Math.random();
		// initiate view
		ResourceManager.openView(partService, InternalBrowser.ID, id);
		// update data
		MPart part = partService.findPart(id);
		InternalBrowser view = (InternalBrowser) part.getObject();

		view.loadURL(url);
		view.setTitle(title);
		return part;
	}

	public String getDownloadLink() {
		return downloadLink;
	}

	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
