/**
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
 * which accompanies this distribution, and is available
 * at the URL "http://www.eclipse.org/legal/epl-v10.html".
 *
 * Contributors:
 * 	David Dubrow
 *
 */

package org.eclipse.mtj.pulsar.core;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.mtj.internal.provisional.pulsar.core.ISDKRepositoryProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
/**
 *
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.mtj.pulsar.core"; //$NON-NLS-1$

	// The extension point id for sdk provider
	private static final String SDK_REPOSITORY_PROVIDER_EXTENSION = PLUGIN_ID + ".sdkRepositoryProvider"; //$NON-NLS-1$

	private Collection<ISDKRepositoryProvider> providers;
	
	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Return the SDK repository provider extensions
	 * 
	 * @return Collection
	 */
	public Collection<ISDKRepositoryProvider> getSDKRepositoryProviders() {
		if (providers == null)
			readSDKRepositoryProviderExtensions();
		
		return providers;
	}
	
	/**
	 * Reads the SDK repository providers extensions into an internal collection
	 */
	private void readSDKRepositoryProviderExtensions() {
		providers = new ArrayList<ISDKRepositoryProvider>();
		IConfigurationElement[] config = 
			Platform.getExtensionRegistry().getConfigurationElementsFor(SDK_REPOSITORY_PROVIDER_EXTENSION);
		for (IConfigurationElement element : config) {
			try {
				Object extension = element.createExecutableExtension("class"); //$NON-NLS-1$
				providers.add((ISDKRepositoryProvider) extension);
			} 
			catch (Exception e) {
				logError("Could not create extension", e); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Return an error IStatus.
	 * 
	 * @param message String
	 * @param t Throwable
	 * @return IStatus
	 */
	public static IStatus makeErrorStatus(String message, Throwable t) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message, t);
	}
	
	/**
	 * Log an error.
	 * 
	 * @param message String
	 * @param t Throwable
	 */
	public static void logError(String message, Throwable t) {
		plugin.getLog().log(makeErrorStatus(message, t));
	}

	/**
	 * Return the BundleContext for this bundle.
	 * 
	 * @return BundleContext
	 */
	public static BundleContext getContext() {
		return plugin.getBundle().getBundleContext();
	}
}
