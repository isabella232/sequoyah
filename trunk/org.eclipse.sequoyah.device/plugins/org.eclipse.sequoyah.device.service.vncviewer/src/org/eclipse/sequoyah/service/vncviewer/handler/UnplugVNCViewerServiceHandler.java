package org.eclipse.tml.service.vncviewer.handler;

import java.io.IOException;

import org.eclipse.tml.framework.device.model.IInstance;
import org.eclipse.tml.framework.device.model.handler.IServiceHandler;
import org.eclipse.tml.framework.device.model.handler.ServiceHandler;
import org.eclipse.tml.protocol.PluginProtocolActionDelegate;
import org.eclipse.tml.vncviewer.vncviews.views.VNCViewerView;

public class UnplugVNCViewerServiceHandler extends ServiceHandler {

	public UnplugVNCViewerServiceHandler() {

		
		//VNCViewerView.stop();
		//VNCViewerView.

	}

	@Override
	public IServiceHandler newInstance() {
		return new UnplugVNCViewerServiceHandler();
	}

	@Override
	public void runService(IInstance instance) {
		// TODO Auto-generated method stub
		
			
		VNCViewerView.stop();
		

		try {
			PluginProtocolActionDelegate.stopProtocol(VNCViewerView.protocol);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

	@Override
	public void updatingService(IInstance instance) {
		// TODO Auto-generated method stub

	}

}
