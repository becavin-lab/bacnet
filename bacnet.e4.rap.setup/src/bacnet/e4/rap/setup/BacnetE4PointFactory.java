package bacnet.e4.rap.setup;

import org.eclipse.rap.e4.E4ApplicationConfig;
import org.eclipse.rap.e4.E4EntryPointFactory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;

import bacnet.Database;

public class BacnetE4PointFactory extends E4EntryPointFactory {

	public BacnetE4PointFactory(E4ApplicationConfig config) {
		super(config);
	}

	@Override
	public EntryPoint create() {
		EntryPoint wrapped = super.create();
		return new EntryPoint() {
			public int createUI() {
				BrowserNavigation service = RWT.getClient().getService(BrowserNavigation.class);
				BrowserNavigationListener listener = new BrowserNavigationListener() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -5943067476931651570L;

					@Override
					public void navigated(BrowserNavigationEvent event) {
						Database.getInstance().setCurrentState(event.getState());
						System.out.println("InitState: " + event.getState());
					}
				};
				Database.getInstance().setNavigationListener(listener);
				service.addBrowserNavigationListener(listener);
				return wrapped.createUI();
			}
		};
	}
}
