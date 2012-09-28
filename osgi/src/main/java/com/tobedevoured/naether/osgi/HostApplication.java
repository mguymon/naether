package com.tobedevoured.naether.osgi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;

public class HostApplication {
    private HostActivator m_activator = null;
    private Map m_lookupMap = new HashMap();
    private Felix m_felix = null;
    private ServiceTracker m_tracker = null;

    public HostApplication() {
    	// Initialize the map for the property lookup service.
        m_lookupMap.put("name1", "value1");

        m_lookupMap.put("name2", "value2");
        m_lookupMap.put("name3", "value3");
        m_lookupMap.put("name4", "value4");
    	
        // Create a configuration property map.
        Map configMap = new HashMap();
        
        // Export the host provided service  and command interface package.
        configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
            "com.tobedevourd.naether.osgi.lookup; version=1.0.0, com.tobedevourd.naether.osgi.command; version=1.0.0");
        
        
     // Create host activator;
        m_activator = new HostActivator(m_lookupMap);
        List list = new ArrayList();
        list.add(m_activator);
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);
        
        
        try {
            // Now create an instance of the framework with
            // our configuration properties.
            m_felix = new Felix(configMap);
            // Now start Felix instance.
            m_felix.start();
        } catch (Exception ex) {
            System.err.println("Could not create framework: " + ex);
            ex.printStackTrace();
        }
        
        m_tracker = new ServiceTracker( m_activator.getContext(), Command.class.getName(), null);
        m_tracker.open();
        
    }
    
    public static void main(String[] args) {
    	HostApplication app = new HostApplication();
    	
    	app.execute("test", "Test" );
    	
    	app.shutdownApplication();
    }
    
    public boolean execute(String name, String commandline) {
    	System.out.println(" w00t " );
        // See if any of the currently tracked command services
        // match the specified command name, if so then execute it.
        Object[] services = m_tracker.getServices();
        for (int i = 0; (services != null) && (i < services.length); i++) {
            try {
                if (((Command) services[i]).getName().equals(name)) {
                    return ((Command) services[i]).execute(commandline);
                }
            } catch (Exception ex) {
                // Since the services returned by the tracker could become
                // invalid at any moment, we will catch all exceptions, log
                // a message, and then ignore faulty services.
                System.err.println(ex);
            }
        }
        return false;
    }

    public Bundle[] getInstalledBundles() {
        // Use the system bundle activator to gain external
        // access to the set of installed bundles.
        return m_activator.getBundles();
    }

    public void shutdownApplication() {
        // Shut down the felix framework when stopping the
        // host application.
        try {
			m_felix.stop();
		} catch (BundleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {
			m_felix.waitForStop(0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}