/********************************************************************************
 * Copyright (c) 2008 Motorola Inc. and Other. All rights reserved
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Julia Martinez Perdigueiro (Eldorado Research Institute) 
 * [244805] - Improvements on Instance view  
 *
 * Contributors:
 * Yu-Fen Kuo (MontaVista)  - [236476] - provide a generic device type
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [259243] - image in the wizards
 ********************************************************************************/

package org.eclipse.tml.framework.device.ui.view.model;

import org.eclipse.tml.framework.device.model.IDeviceType;


public class ViewerDeviceNode extends ViewerAbstractNode
{
    /*
     * The Device
     */
    private IDeviceType device;

    public ViewerDeviceNode(IDeviceType device)
    {
        super(null);
        this.device = device;
    }

    public String toString()
    {
        return getDeviceName();
    }
    
    public String getDeviceName()
    {
        return device.getLabel();
    }
    
    public IDeviceType getDevice()
    {
        return device;
    }
}
