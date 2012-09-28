package com.tobedevoured.naether.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.tobedevoured.naether.api.Naether;
import com.tobedevoured.naether.api.NaetherService;
import com.tobedevoured.naether.impl.NaetherImpl;

public class Activator implements NaetherService, BundleActivator {

    public void start(BundleContext context) throws Exception {
    	System.out.println("start");
    }
    
    public void stop(BundleContext context) throws Exception {
    	System.out.println("stop");
    }

	public Naether createInstance() {
		return new NaetherImpl();
	}

}
