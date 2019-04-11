package bacnet.e4.rap.setup;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.rap.e4.E4ApplicationConfig;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.ExceptionHandler;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.swt.widgets.Shell;

public class BasicApplication implements ApplicationConfiguration {

	// public static String projectName = Database.YERSINIOMICS_PROJECT;
	// public static String projectName = Database.LEISHOMICS_PROJECT;
	// public static String projectName = Database.CRISPRGO_PROJECT;
	// public static String projectName = Database.LISTERIOMICS_PROJECT;
	public static String projectName = "ListeriomicsSample";
	// public static String projectName = "Encode";

	public static String MAIN_ENTRYPOINT = "/";

	@Inject
	EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	public void configure(Application application) {
		// System.out.println(System.getProperty(CONFIGURATION_PARAM));

		Map<String, String> properties = new HashMap<String, String>();
		String title = "Bacnet setup website";

		properties.put(WebClient.PAGE_TITLE, title);
		// WebClient.
		properties.put(WebClient.THEME_ID, RWT.DEFAULT_THEME_ID);
		properties.put(WebClient.PAGE_OVERFLOW, "scroll");

		BacnetE4PointFactory entryPointFactory = new BacnetE4PointFactory(
				E4ApplicationConfig.create("platform:/plugin/bacnet.e4.rap.setup/Application.e4xmi"));
		application.addEntryPoint(MAIN_ENTRYPOINT, entryPointFactory, properties);
		application.setOperationMode(OperationMode.SWT_COMPATIBILITY);
		application.setExceptionHandler(new ExceptionHandler() {
			@Override
			public void handleException(Throwable exception) {
				System.out.println("Error : " + exception.getMessage());
			}
		});
	}

	@SuppressWarnings("unused")
	private static ResourceLoader createResourceLoader(final String resourceName) {
		return new ResourceLoader() {
			@Override
			public InputStream getResourceAsStream(String resourceName) throws IOException {
				return getClass().getClassLoader().getResourceAsStream(resourceName);
			}
		};
	}

}
