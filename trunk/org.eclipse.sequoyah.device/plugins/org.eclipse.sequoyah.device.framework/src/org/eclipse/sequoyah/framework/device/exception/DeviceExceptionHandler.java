
/**Copyright (c) 2007 Motorola Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Fabio Fantato (Motorola)
 * 
 * Contributors:
 * name (company) - description.
 ********************************************************************************/

package org.eclipse.tml.framework.device.exception;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tml.common.utilities.exception.ExceptionHandler;
import org.eclipse.tml.common.utilities.exception.TmLException;
import org.eclipse.tml.framework.device.DevicePlugin;

public class DeviceExceptionHandler extends ExceptionHandler {
	

	public static TmLException exception(IStatus status) {
		return new TmLException(new DeviceExceptionStatus(status));
	}
	
	public static TmLException exception(int severity, int code, String message, Throwable exception) {
		return new TmLException(new DeviceExceptionStatus(severity, DevicePlugin.PLUGIN_ID, code, message, exception));
	}

	public static TmLException exception(int code){
		return new TmLException(new DeviceExceptionStatus(code,DevicePlugin.PLUGIN_ID,null,null));
	}

	public static TmLException exception(int code,Throwable exception) {
		return new TmLException(new DeviceExceptionStatus(code,DevicePlugin.PLUGIN_ID,exception));
	}
	
	public static TmLException exception(int code,Object[] args,Throwable exception) {
		return new TmLException(new DeviceExceptionStatus(code,DevicePlugin.PLUGIN_ID,args,exception));		
	}
	
}
