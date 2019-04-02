package bacnet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator, BundleListener {
    // The plug-in ID
    public static final String PLUGIN_ID = "bacnet"; //$NON-NLS-1$

    private static BundleContext context;

    // The shared instance
    private static Activator plugin;

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework. BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        plugin = this;
        // System.out.println("Starting bacnet Bundle Listener - " +
        // context.hashCode());
        context.addBundleListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        // System.out.println("Stopping bacnet Bundle Listener - " +
        // context.hashCode());
        context.removeBundleListener(this);
    }

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
        String symbolicName = event.getBundle().getSymbolicName();
        String type = typeAsString(event);
        // System.out.println("BundleChanged bacnet: " + symbolicName + ", event.type: "
        // + type);
    }

}
