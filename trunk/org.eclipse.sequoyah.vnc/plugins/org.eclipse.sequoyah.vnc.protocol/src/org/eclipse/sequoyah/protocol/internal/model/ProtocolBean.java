/********************************************************************************
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Fabio Rigo
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.tml.protocol.internal.model;

import org.eclipse.tml.protocol.lib.IProtocolImplementer;

/**
 * DESCRIPTION: This class represents a bean that holds data retrieved from the
 * ProtocolImplementer extensions. <br>
 * 
 * RESPONSIBILITY: Store and provide data regarding a protocol implementer.<br>
 * 
 * COLABORATORS: None.<br>
 * 
 * USAGE: The framework sets the data according to user extension declarations.
 * Use the getter methods to retrieve that data.<br>
 * 
 */
public class ProtocolBean {

	// Element fields
	private String protocolId;
	private String parentProtocol;
	private boolean isBigEndianProtocol;
	private IProtocolImplementer protocolImplementerSeed;
	private int serverPort;

	/*
	 * Setters section
	 */
	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}

	public void setParentProtocol(String parentProtocol) {
		this.parentProtocol = parentProtocol;
	}

	public void setBigEndianProtocol(boolean isBigEndianProtocol) {
		this.isBigEndianProtocol = isBigEndianProtocol;
	}

	public void setProtocolImplementerSeed(
			IProtocolImplementer protocolImplementerSeed) {
		this.protocolImplementerSeed = protocolImplementerSeed;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	/*
	 * Getters section
	 */
	public String getProtocolId() {
		return protocolId;
	}

	public String getParentProtocol() {
		return parentProtocol;
	}

	public boolean isBigEndianProtocol() {
		return isBigEndianProtocol;
	}

	public IProtocolImplementer getProtocolImplementer() {

		// Use reflection to guarantee that every time the method is invoked
		// a new instance of the class will be created to return to the user.
		// The "seed" object (the one created by the extension framework) is
		// kept intact
		Class<? extends IProtocolImplementer> classObj = protocolImplementerSeed
				.getClass();
		IProtocolImplementer newInstance = null;
		try {
			newInstance = classObj.newInstance();
		} catch (Exception e) {
			// TODO This is a temporary exception handling
			e.printStackTrace();
		}

		return newInstance;
	}

	public int getServerPort() {
		return serverPort;
	}
}
