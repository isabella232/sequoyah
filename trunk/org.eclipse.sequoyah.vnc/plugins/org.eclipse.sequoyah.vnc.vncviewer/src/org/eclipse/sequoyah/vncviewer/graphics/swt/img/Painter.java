/********************************************************************************
 * Copyright (c) 2007 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Daniel Franco (Motorola)
 *
 * Contributors:
 * Eugene Melekhov (Montavista) - Bug [227793] - Implementation of the several encodings, performance enhancement etc
 * Daniel Barboza Franco (Eldorado Research Institute) -  [243167] - Zoom mechanism not working properly 
 ********************************************************************************/

package org.eclipse.tml.vncviewer.graphics.swt.img;

import java.io.DataInputStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.tml.vncviewer.graphics.IPainterContext;
import org.eclipse.tml.vncviewer.graphics.swt.ISWTPainter;
import org.eclipse.tml.vncviewer.network.AbstractVNCPainter;
import org.eclipse.tml.vncviewer.network.PixelFormat;
import org.eclipse.tml.vncviewer.network.RectHeader;

/**
 * This class renders the screen sent by a VNC Server using 
 * the SWT Image object
 */
public class Painter extends AbstractVNCPainter implements ISWTPainter {

	protected Image image; 

	protected ImageData imgData;
	
	protected GC imageGC;

	protected PaletteData paletteData;
	
	protected SWTRemoteDisplayImg parent;
	
	public Painter(SWTRemoteDisplayImg parent) {
		super();
		this.parent = parent;
	}

	public void setPixelFormat(PixelFormat pixelFormat) {
		super.setPixelFormat(pixelFormat);
		paletteData = new PaletteData(pixelFormat.getRedMax()<<pixelFormat.getRedShift(), pixelFormat.getGreenMax()<<pixelFormat.getGreenShift(), pixelFormat.getBlueMax()<<pixelFormat.getBlueShift());		
	}

	public void setSize(int width, int height){
		super.setSize(width, height);
		dispose();
		image = new Image(parent.getDisplay(), width, height);
		imageGC = new GC(image);
		imgData = new ImageData(width, height, pixelFormat.getDepth(), paletteData);
	}
	
	protected void fillRect(int pixel, int x, int y, int width, int height) {
		Color color = new Color(image.getDevice(), paletteData.getRGB(pixel));
		imageGC.setBackground(color);
		
		int [] pixels;
		pixels = new int[width];
		
		for (int i=0; i<width; i++ ) {
			pixels[i] = pixel; 
		}

		for (int j=0; j<height; j++) {
			imgData.setPixels(x, y+j, width, pixels, 0);	
		}

		color.dispose();
	}

	protected void setPixels(int x, int y, int width, int[] pixels, int startIndex) {
		//We do not need this feature in this implementation
	}

	protected void setPixels(int x, int y, int width, int height,
			int[] pixels, int start) {
		
		
		for (int i=0; i<height; i++) {
			imgData.setPixels(x, y+i, width, pixels, width*i);	
		}
		
	}
	
	public void updateRectangle(int x1, int y1, int x2, int y2) {
		
		double zoom = parent.getZoomFactor();
		
		int a, b, r, s;
		a = (int) (x1 * zoom);
		b = (int) (y1 * zoom);
		r = (int) ((x2-x1) * zoom);
		s = (int) ((y2-y1) * zoom);
		
		// Cover rounded values
		a--;
		b--;
		r++;
		s++;
		
		parent.redrawScreen(a, b, r, s);
		
	}

	@Override
	protected IPainterContext getPainterContext() {
		return new IPainterContext() {

			public void fillRect(int pixel, int x, int y, int width, int height) {
				Painter.this.fillRect(pixel, x, y, width, height);
			}

			public PixelFormat getPixelFormat() {
				return Painter.this.getPixelFormat();
			}

			public int getBytesPerPixel() {
				return bytesPerPixel;
			}

			public void processRectangle(RectHeader rh, DataInputStream in) throws Exception{
				Painter.this.processRectangle(rh, in);
			}

			public int readPixel(DataInputStream is) throws Exception {
				return Painter.this.readPixel(is);
			}

			public int readPixel(DataInputStream is, int bytesPerPixel) throws Exception {
				return Painter.this.readPixel(is, bytesPerPixel);
			}

			public void setPixels(int x, int y, int width, int height,
					int[] pixels, int start) {
				Painter.this.setPixels(x, y, width, height, pixels, start);
			}

			public int[] readpixels(DataInputStream is, int w, int h) {
				return  Painter.this.readPixels(is, w, h);
			}
		};
	}

	public void dispose() {
		if (image != null) {
			image.dispose();
			image = null;
		}
		if (imageGC != null) {
			imageGC.dispose();
			imageGC = null;
		}
	}

	public ImageData getImageData() {
		return imgData;
	}
	
	
}
