package bacnet.e4.rap;

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

import bacnet.Database;

public class BasicApplication implements ApplicationConfiguration {

    //public static String projectName = Database.BACNET;
    //public static String projectName = Database.YERSINIOMICS_PROJECT;
    //public static String projectName = Database.URY_YERSINIOMICS_PROJECT;  
    //public static String projectName = "ListeriomicsSample";
    public static String projectName = Database.YERSINIOMICS_PROJECT;
    // public static String projectName = "CRISPRGo";
    // public static String projectName = "Leishomics";
    public static String MAIN_ENTRYPOINT = "/";

    @Inject
    EPartService partService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    public void configure(Application application) {
    	System.out.println("Start Application");
        System.out.println("prop: " + System.getProperty("enableFrameworkControls")+" - "+System.getProperty(CONFIGURATION_PARAM));
    	
        Map<String, String> properties = new HashMap<String, String>();
        String title = "";
        String googleId = "1";
        if (projectName == Database.LISTERIOMICS_PROJECT) {
            title = "Listeriomics website";
            googleId = "UA-80335618-1";
        } else if (projectName == Database.BACNET) {
            title = "Build your own omics website with BACNET";
            googleId = "UA-80335618-6";
        } else if (projectName == Database.UIBCLISTERIOMICS_PROJECT) {
            title = "UIBC private listeriomics website";
            googleId = "UA-80335618-4";
        } else if (projectName == Database.LEISHOMICS_PROJECT) {
            title = "Leishomics website";
            googleId = "UA-80335618-2";
        } else if (projectName == Database.CRISPRGO_PROJECT) {
            title = "CRISPRBrowser - CRISPR design tools for bacteria";
            // title = "CRISPRGo - CRISPR design tools for bacteria";
            googleId = "UA-129705822-1";
        } else if (projectName == Database.YERSINIOMICS_PROJECT) {
            title = "Yersiniomics website";
            googleId = "G-0HQQJHGD66";
        } else if (projectName == Database.URY_YERSINIOMICS_PROJECT) {
            title = "Yersiniomics website";
            googleId = "UA-80335618-7";
        }
        application.addResource("theme/favicon.png", createResourceLoader("theme/favicon.png"));
        properties.put(WebClient.FAVICON, "theme/favicon.png");
        properties.put(WebClient.PAGE_TITLE, title);
        // WebClient.
        String webpage_head = "";
        // String hotjar = "(function(h,o,t,j,a,r){\n" +
        // " h.hj=h.hj||function(){(h.hj.q=h.hj.q||[]).push(arguments)};\n" +
        // " h._hjSettings={hjid:638461,hjsv:5};\n" +
        // " a=o.getElementsByTagName('head')[0];\n" +
        // " r=o.createElement('script');r.async=1;\n" +
        // " r.src=t+h._hjSettings.hjid+j+h._hjSettings.hjsv;\n" +
        // " a.appendChild(r);\n" +
        // " })(window,document,'//static.hotjar.com/c/hotjar-','.js?sv=');";
        webpage_head = "<meta name=\"description\" content=\"Yersiniomics integrates complete genomes, transcriptomes and proteomes published for Yersinia species.\"\n";
        webpage_head +="<!-- Google tag (gtag.js) -->\n" + 
        "<script async src=\"https://www.googletagmanager.com/gtag/js?id="+googleId+"\"></script>\n" + 
        "<script>\n" + 
        "  window.dataLayer = window.dataLayer || [];\n" + 
        "  function gtag(){dataLayer.push(arguments);}\n" + 
        "  gtag('js', new Date());\n" + 
        "\n" + 
        "  gtag('config', '"+googleId+"');\n" + 
        "</script>\n" ;
        webpage_head += "\n<script src=\"https://html2canvas.hertzen.com/dist/html2canvas.min.js\"></script>";
        webpage_head += "\n<script  src=\"https://code.jquery.com/jquery-3.3.1.min.js\"></script>";
        properties.put(WebClient.HEAD_HTML, webpage_head);
        application.addStyleSheet(RWT.DEFAULT_THEME_ID, "theme/bacnet.rap.css");
        properties.put(WebClient.THEME_ID, RWT.DEFAULT_THEME_ID);
        properties.put(WebClient.PAGE_OVERFLOW, "scroll");

        BacnetE4PointFactory entryPointFactory = new BacnetE4PointFactory(
                E4ApplicationConfig.create("platform:/plugin/bacnet.e4.rap/Application.e4xmi"));
        application.addEntryPoint(MAIN_ENTRYPOINT, entryPointFactory, properties);
        application.setOperationMode(OperationMode.SWT_COMPATIBILITY);
        application.setExceptionHandler(new ExceptionHandler() {
            @Override
            public void handleException(Throwable exception) {
                System.out.println("Error : " + exception.getMessage());
            }
            
        });
        System.out.println("Application started");
    }

    public static ResourceLoader createResourceLoader(final String resourceName) {
        return new ResourceLoader() {
            @Override
            public InputStream getResourceAsStream(String resourceName) throws IOException {
                return getClass().getClassLoader().getResourceAsStream(resourceName);
            }
        };
    }

}
