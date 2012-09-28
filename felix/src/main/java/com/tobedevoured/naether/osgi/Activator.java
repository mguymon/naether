package com.tobedevoured.naether.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
    	System.out.println("start");
    }

    public void stop(BundleContext context) throws Exception {
    	System.out.println("stop");
    }

}
