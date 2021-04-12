package bacnet.rap.e4;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.e4.E4ApplicationConfig;
import org.eclipse.rap.e4.E4EntryPointFactory;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.client.WebClient;


public class E4Application implements ApplicationConfiguration {

    private final static String E4XMI = "platform:/plugin/bacnet.rap.e4/Application.e4xmi";

    public void configure(Application application) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(WebClient.PAGE_TITLE, "Hello e4 RAP");
        E4ApplicationConfig config = E4ApplicationConfig.create(E4XMI);
        E4EntryPointFactory entryPointFactory = new E4EntryPointFactory(config);
        application.addEntryPoint("/e4", entryPointFactory, properties);
        application.setOperationMode( OperationMode.SWT_COMPATIBILITY );
    }

}
