package bacnet.scripts;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * OSGI Activator of bacnet.scripts
 * @author christophebecavin
 *
 */
public class Activator implements BundleActivator, BundleListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "bacnet.scripts"; //$NON-NLS-1$

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		// System.out.println("Starting bacnet.scripts Bundle Listener - " +
		// context.hashCode());
		context.addBundleListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		// System.out.println("Stopping bacnet.scripts Bundle Listener - " +
		// context.hashCode());
		context.removeBundleListener(this);
	}

	/**
	 * Print the type of activation for each bundle
	 * <br> Use it only if needed for debugging. Commented otherwise
	 * @param event
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String typeAsString(BundleEvent event) {
		if (event == null) {
			return "null";
		}
		int type = event.getType();
		switch (type) {
		case BundleEvent.INSTALLED:
			return "INSTALLED";
		case BundleEvent.LAZY_ACTIVATION:
			return "LAZY_ACTIVATION";
		case BundleEvent.RESOLVED:
			return "RESOLVED";
		case BundleEvent.STARTED:
			return "STARTED";
		case BundleEvent.STARTING:
			return "Starting";
		case BundleEvent.STOPPED:
			return "STOPPED";
		case BundleEvent.UNINSTALLED:
			return "UNINSTALLED";
		case BundleEvent.UNRESOLVED:
			return "UNRESOLVED";
		case BundleEvent.UPDATED:
			return "UPDATED";
		default:
			return "unknown event type: " + type;
		}
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		//String symbolicName = event.getBundle().getSymbolicName();
		//String type = typeAsString(event);
		// System.out.println("BundleChanged bacnet.scripts: " + symbolicName + ",
		// event.type: " + type);
	}

}
