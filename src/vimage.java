/* PTViewer	-	Interactive Viewer for Panoramic Images
   
   vimage.java: VolatileImage module
   Copyright (C) 2004 Fulvio Senore fsenore@ica-net.it
   
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  */

/*------------------------------------------------------------*/

/*
 * Handles the drawing on an accelerated frame using
 * a VolatileImage
 * 
 * I need a separate class because ptviewer must run in VMs that do
 * not support the VolatileImage class
 */

import java.awt.image.VolatileImage;
import java.awt.*;

public class vimage {

	ptviewer ptv;
	int w, h;	// size of the image
	
	public vimage( ptviewer ptv ) {
		this.ptv = ptv;
	}
	
	public void setSize( int w, int h ) {
		this.w = w;
		this.h = h;
	}
	
	// creates the backbuffer used to draw frames
	void createBackBuffer() {
		if( ptv.backBuffer == null ) {
				ptv.backBuffer = ptv.createVolatileImage(w, h);
		}
	}

	// recreates the backbuffer if it has been lost
	void recreateBackBuffer() {
		if (ptv.backBuffer != null) {
			ptv.backBuffer.flush();
			ptv.backBuffer = null;
		}
		createBackBuffer();
	}

	// draws a frame using an accelerated back buffer
	void drawAcceleratedFrame(Graphics g) {
		createBackBuffer();
		do {
				// First, we validate the back buffer
				int valCode = ((VolatileImage) ptv.backBuffer).validate(ptv.getGraphicsConfiguration());
				if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
					recreateBackBuffer();
				}
				// Now we've handled validation, get on with the rendering

				Graphics gBB = ptv.backBuffer.getGraphics();
				ptv.renderFrame(gBB);
				g.drawImage(ptv.backBuffer, 0, 0, ptv);

				// Now we are done; or are we?  Check contentsLost() and loop as necessary
		} while (((VolatileImage) ptv.backBuffer).contentsLost());
	}

}
