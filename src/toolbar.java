/* PTViewer	-	Interactive Viewer for Panoramic Images
   
   toolbar.java: toolbar module
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

import java.awt.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.PixelGrabber;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class toolbar implements Runnable {

	// reference to the ptviewer object
	protected ptviewer ptv;
	
	// size of the toolbar
	protected int width, height = 23;
	
	// coordinates of the upper-left corner of the toolbar
	protected int xTlb, yTlb;
	
	// image of the toolbar, ready to be drawn in the Graphic context
	protected Image tlbImage;
	protected Graphics tlbGraph; // graphic context
	
	// contains the images of the various buttons
	protected Image buttons[];
	
	// indexes in the buttons[] array
	protected int BTN_PLUS = 0;
	protected int BTN_PLUS_OVER = 1;
	protected int BTN_PLUS_PRESSED = 2;
	protected int BTN_MINUS = 3;
	protected int BTN_MINUS_OVER = 4;
	protected int BTN_MINUS_PRESSED = 5;
	protected int BTN_HS = 6;
	protected int BTN_HS_OVER = 7;
	protected int BTN_HS_PRESSED = 8;
	protected int BTN_HS_PRESSED_OVER = 9;
	protected int BTN_HS_DISABLED = 10;
	
	// number of image buttons
	protected 	int N_BUTTONS = 11;

	// size of buttons
	protected int W_BUTTON = 27;
	protected int H_BUTTON = 19;
	
	// x offset of the first button in the toolbar
	protected int X_OFFSET = 1;
	// y offset of each button in the toolbar
	protected int Y_OFFSET = 2;
	
	// offset of the various statuses of each button
	protected int BTN_STATUS_NORMAL = 0;
	protected int BTN_STATUS_OVER = 1;
	protected int BTN_STATUS_PRESSED = 2;
	protected int BTN_STATUS_PRESSED_OVER = 3;
	protected int BTN_STATUS_DISABLED = 4;
	
	// current status of each button
	protected int curStatusPlus = BTN_STATUS_NORMAL;
	protected int curStatusMinus = BTN_STATUS_NORMAL;
	protected int curStatusHS = BTN_STATUS_DISABLED;
	protected boolean isHSButtonPressed = false;
	
	// last button pressed in a mousedown event: 0=plus, 1=minus and so on -1=no button
	protected boolean justPressedHSButton = false;
	
	private Thread zoomThread = null;
	
	// color of the progress bar
	protected Color barColor;			// progress bar color
	protected Color borderColor;	// color of the border of the toolbar
	protected Color bgColor;			// background color
	protected Color textColor;		// color of the text displayed in the toolbar
	protected Color textDefaultColor;		// default text color
	
	// position and size of the progress bar
	protected int BAR_X_OFFSET = 89;
	protected int BAR_Y_OFFSET = 7;
	protected int BAR_HEIGHT = 9; 
	protected int barX, barY;
	protected int barTotWidth;	// width at 100%, will be determined at runtime
	protected int barPerc = 0;	// current percent for the progress bar
	
	// flag, used in the run() method to make sure that ptviewer.paint() has been called
	protected boolean paintFinished = true;
	
	// message to draw in the toolbar
	protected String msg;
	
	// font to use when drawing msg
	Font msgFont;
	
	// coordinates for writing the message
	int xMessage, yMessage;
	
	// if true the message will be written in bold
	boolean msgBold;
	
	/*
	 * constructor: receives a reference to the ptviewer applet and the size of the viewer
	 *   window (it will include the toolbar)
	 * the second parameter is the image strip to be used to draw the toolbar
	 *   if == null the image strip will be loaded from the jar file
	 */
	public toolbar( ptviewer ptv, String imgStripName ) {
			
			this.ptv = ptv;
			msg = "";
			msgFont = null;
			msgBold = false;
			
			textDefaultColor = Color.black;
			textColor = textDefaultColor;
			
			// loads the button images
			buttons = new Image[N_BUTTONS];
			loadButtonImages( imgStripName );
			
			xMessage = BAR_X_OFFSET;
	}

	// creates the font to use for drawing messages
	protected void createFont( Graphics g ) {
		boolean fontFits = false;
		Font curFont = g.getFont();
		int maxCharHeight = 19;
		int minFontSize = 5;

		Font font = g.getFont();
		int size = font.getSize();
		String name = font.getName();
		int style = font.getStyle();
		if( msgBold ) style = Font.BOLD;
		g.setFont(font = new Font(name, style, size));
		FontMetrics fontMetrics = g.getFontMetrics();

		while (!fontFits) {
			if (fontMetrics.getHeight() <= maxCharHeight) {
				fontFits = true;
			} else {
				if (size <= minFontSize) {
					fontFits = true;
				} else {
					g.setFont(font = new Font(name, style, --size));
					fontMetrics = g.getFontMetrics();
				}
			}
		}
		
		// computes the writing y coordinate
		yMessage = yTlb + height - (height - fontMetrics.getAscent())/2 - 2;

		msgFont = g.getFont();
		g.setFont( curFont );
		
	}
	
	// sets a message to be drawn in the toolbar, at the right of the buttons
	// sets the color to its default value
	public void setMessage( String msg ) {
		this.msg = msg;
		this.textColor = this.textDefaultColor;
	}
	
	// sets a message to be drawn in the toolbar, at the right of the buttons
	// sets the color to the received parameter
	public void setMessage( String msg, Color textColor ) {
		this.msg = msg;
		this.textColor = textColor;
	}
	
	// repeats zooming in or out while the button is pressed
	public void run() {
		
		int ptvQuality = ptv.getQuality();
		if( ptvQuality > 3 ) ptv.setQuality(3);	// changes to bilinear to speed up zooming
		
		Thread myThread = Thread.currentThread();
		while( zoomThread == myThread ) {

			long t;
			t = System.currentTimeMillis();

			paintFinished = false;
			if( myThread.getName().equals("zoomin") )
				ptv.ZoomIn();			
			if( myThread.getName().equals("zoomout") )
				ptv.ZoomOut();
			Thread.yield();
			
			// waits for the ptviewer.paint() method to execute
			// paintFinished is changed in toolbar.paint()
			while( !paintFinished ) {
				try{ Thread.sleep( 20 ); }
				catch( Exception e ) {}
			}
			
			// waits for a minimum time to avoid too fast zooming
			t = System.currentTimeMillis() - t;
			if( t < 50 ) {
				try{ Thread.sleep( 50 - t ); }
				catch( Exception e ) {}
			}
		}
		
		if( ptvQuality > 3 ) {
			// redraws the last frame with high quality
			ptv.setQuality(ptvQuality);
			ptv.repaint();
		}
		
	}
	
	// called from ptviewer to inform that the paint() method has terminated
	public void notifyEndPaint() {
		paintFinished = true;
	}
	
	// informs the toobar about the viewer size
	public void setViewerSize( int wViewer, int hViewer, int wx, int wy ) {
		
		// computes size and position of the toolbar
		width = wViewer;
		xTlb = wx;
		yTlb = hViewer - height + wy;		
		
		// position of the progress bar
		barX = BAR_X_OFFSET;
		barY = BAR_Y_OFFSET;
		
		// compute width of the progress bar
		barTotWidth = width - BAR_X_OFFSET - 9;

		// creates the toolbar image and its context
		tlbImage = ptv.createImage( width, height );
		tlbGraph = tlbImage.getGraphics();
			
	}

	// sets the value for the progress bar ( <= 0 or >= 100 do not draw bar)
	public void setBarPerc( int perc ) {
		barPerc = perc;
	}
	
	// sets the x coordinate for the hotspots description
	public void setToolbarDescrX( int x ) {
		if( x > 0 ) xMessage = x;
	}

	public void paint( Graphics g ) {
		if( msgFont == null ) createFont( g );
		drawToolbar();
		drawProgressBar();
		g.drawImage( tlbImage, xTlb, yTlb, ptv );
		
		// draws the message
		if( msg != "" ) {
			Color curColor = g.getColor();
			g.setColor( textColor );
			Font curFont = g.getFont();
			g.setFont( msgFont );
			g.drawString( msg, xMessage, yMessage );
			g.setFont( curFont );
			g.setColor( curColor );
		}
	}
	
	// returns the height of the toolbar
	public int getHeight() {
		return height;
	}
	
	// toggles the hotspots button image (called by ptviewer)
	public void toggleHSButton() {
		if( ptv.numhs == 0 ) return;
		if( isHSButtonPressed ) {
			if( curStatusHS == BTN_STATUS_PRESSED_OVER)
				curStatusHS = BTN_STATUS_OVER;
			else
				curStatusHS = BTN_STATUS_NORMAL;
		}
		else {
			if( curStatusHS == BTN_STATUS_OVER)
				curStatusHS = BTN_STATUS_PRESSED_OVER;
			else
				curStatusHS = BTN_STATUS_PRESSED;
		}
		isHSButtonPressed = !isHSButtonPressed;
	}
	
	// synchronizes the HS button with ptviewer's status
	public void syncHSButton() {
		if( ptv.numhs == 0 ) return;
		if( ptv.showhs ) {
			isHSButtonPressed = true;
			if( curStatusHS == BTN_STATUS_OVER || curStatusHS == BTN_STATUS_PRESSED_OVER )
				curStatusHS = BTN_STATUS_PRESSED_OVER;
			else
				curStatusHS = BTN_STATUS_PRESSED;
		}
		else {
			isHSButtonPressed = false;
			if( curStatusHS == BTN_STATUS_OVER || curStatusHS == BTN_STATUS_PRESSED_OVER )
				curStatusHS = BTN_STATUS_OVER;
			else
				curStatusHS = BTN_STATUS_NORMAL;
		}
	}
	
	// handles the mouse movement inside the toolbar
	// the coordinates are in referred to ptviewer
	public void mouseMove( int i, int j ) {

		// plus button
		if( overPlusButton(i, j) ) {
			if( curStatusPlus != BTN_STATUS_OVER ) {
				curStatusPlus = BTN_STATUS_OVER;
				ptv.repaint();
			}
		}
		else {
			if( curStatusPlus != BTN_STATUS_NORMAL ) {
				curStatusPlus = BTN_STATUS_NORMAL;
				ptv.repaint();
			}
		}

		// minus button
		if( overMinusButton(i, j) ) {
			if( curStatusMinus != BTN_STATUS_OVER ) {
				curStatusMinus = BTN_STATUS_OVER;
				ptv.repaint();
			}
		}
		else {
			if( curStatusMinus != BTN_STATUS_NORMAL ) {
				curStatusMinus = BTN_STATUS_NORMAL;
				ptv.repaint();
			}
		}

		if( ptv.numhs > 0 ) {
			// hotspots button
			if( overHSButton(i, j) ) {
				if( !isHSButtonPressed && curStatusHS != BTN_STATUS_OVER ) {
					curStatusHS = BTN_STATUS_OVER;
					ptv.repaint();
				}
				else if ( isHSButtonPressed && curStatusHS != BTN_STATUS_PRESSED_OVER ) {
					curStatusHS = BTN_STATUS_PRESSED_OVER;
					ptv.repaint();
				}
			}
			else {
				if( !isHSButtonPressed && curStatusHS != BTN_STATUS_NORMAL ) {
					curStatusHS = BTN_STATUS_NORMAL;
					ptv.repaint();
				}
				else if ( isHSButtonPressed && curStatusHS != BTN_STATUS_PRESSED ) {
					curStatusHS = BTN_STATUS_PRESSED;
					ptv.repaint();
				}
			}
		}
	}

	public void mouseDown( int i, int j ) {
		
		justPressedHSButton = false;
		
		if( overPlusButton(i, j) ) {
			justPressedHSButton = false;
			curStatusPlus = BTN_STATUS_PRESSED;
			zoomThread = new Thread( this, "zoomin" );
			zoomThread.start();
		}

		if( overMinusButton(i, j) ) {
			justPressedHSButton = false;
			curStatusMinus = BTN_STATUS_PRESSED;
			zoomThread = new Thread( this, "zoomout" );
			zoomThread.start();
		}

		if( ptv.numhs > 0 ) {
			if( overHSButton(i, j) ) {
				justPressedHSButton = !isHSButtonPressed;
				if( !isHSButtonPressed ) {
					curStatusHS = BTN_STATUS_PRESSED_OVER;
					isHSButtonPressed = !isHSButtonPressed;
					ptv.showHS();
				}
			}
		}
	}
	
	public void mouseUp( int i, int j ) {
		if( overPlusButton(i, j) ) {
			curStatusPlus = BTN_STATUS_OVER;
			zoomThread = null;
		}

		if( overMinusButton(i, j) ) {
			curStatusMinus = BTN_STATUS_OVER;
			zoomThread = null;
		}

		if( ptv.numhs > 0 ) {
			if( overHSButton(i, j) ) {
				if( isHSButtonPressed && !justPressedHSButton ) {
					curStatusHS = BTN_STATUS_OVER;
					isHSButtonPressed = !isHSButtonPressed;
					ptv.hideHS();
				}
			}
		}
	}
	
	public void mouseDrag( int i, int j ) {
		if( !overPlusButton(i, j) ) {
			if( curStatusPlus != BTN_STATUS_NORMAL ) {
				curStatusPlus = BTN_STATUS_NORMAL;
				zoomThread = null;	// stop zooming (in case we are zooming)
				ptv.repaint();
			}
		}

		if( !overMinusButton(i, j) ) {
			if( curStatusMinus != BTN_STATUS_NORMAL ) {
				curStatusMinus = BTN_STATUS_NORMAL;
				zoomThread = null;	// stop zooming (in case we are zooming)
				ptv.repaint();
			}
		}
	}
	
	public void mouseExit( int i, int j ) {
		curStatusPlus = BTN_STATUS_NORMAL;
		curStatusMinus = BTN_STATUS_NORMAL;
		if( ptv.numhs > 0 ) {
			if( isHSButtonPressed )
				curStatusHS = BTN_STATUS_PRESSED;
			else
				curStatusHS = BTN_STATUS_NORMAL;
		}
		ptv.repaint();
	}
	
	// sets the default text color
	public void SetTextColor( String txtColor ) {
		try {
			textDefaultColor = new Color( Integer.parseInt( txtColor, 16 ));
		}
		catch (Exception e) {}
	}

	// returns true if the mouse is over the plus button
	protected boolean overPlusButton( int i, int j ) {
		return overButtonN( i, j, 0 );

//		if( i < xTlb + X_OFFSET + 1 ) return false;
//		if( i > xTlb + X_OFFSET + W_BUTTON - 2 ) return false;
//		if( j < yTlb + Y_OFFSET + 1 ) return false;
//		if( j > yTlb + Y_OFFSET + H_BUTTON - 2 ) return false;
//		return true;
	}
	
	// returns true if the mouse is over the minus button
	protected boolean overMinusButton( int i, int j ) {

		return overButtonN( i, j, 1 );
//		if( i < xTlb + X_OFFSET + 1 + W_BUTTON ) return false;
//		if( i > xTlb + X_OFFSET + W_BUTTON - 2 + W_BUTTON ) return false;
//		if( j < yTlb + Y_OFFSET + 1 ) return false;
//		if( j > yTlb + Y_OFFSET + H_BUTTON - 2 ) return false;
//		return true;
	}
	
	// returns true if the mouse is over the hotspots button
	protected boolean overHSButton( int i, int j ) {

		return overButtonN( i, j, 2 );
//		if( i < xTlb + X_OFFSET + 1 + W_BUTTON*2 ) return false;
//		if( i > xTlb + X_OFFSET + W_BUTTON - 2 + W_BUTTON*2 ) return false;
//		if( j < yTlb + Y_OFFSET + 1 ) return false;
//		if( j > yTlb + Y_OFFSET + H_BUTTON - 2 ) return false;
//		return true;
	}
	
	// returns true if the mouse is over a button ( 0 = plus, 1 = minus, 2 = HS )
	protected boolean overButtonN( int i, int j, int nButton ) {
		if( i < xTlb + X_OFFSET + 1 + W_BUTTON*nButton ) return false;
		if( i > xTlb + X_OFFSET + W_BUTTON - 2 + W_BUTTON*nButton ) return false;
		if( j < yTlb + Y_OFFSET + 1 ) return false;
		if( j > yTlb + Y_OFFSET + H_BUTTON - 2 ) return false;
		return true;
	}
	
	// loads the button images from a single file
	protected void loadButtonImages(String imgStripName) {
		Image imgStrip;
		
//		Image imgStrip = ptv.loadImage( "toolbar.gif" );
		if( imgStripName == null ) {
			// loads the image strip from the jar file
			try {
				MediaTracker m = new MediaTracker( ptv );
				InputStream is = getClass().getResourceAsStream( "Toolbar.gif");
				BufferedInputStream bis = new BufferedInputStream( is );
				byte[] byBuf = new byte[10000];
				int byteRead = bis.read( byBuf, 0, 10000 );
				imgStrip = Toolkit.getDefaultToolkit().createImage(byBuf);
				m.addImage( imgStrip, 0 );
				m.waitForAll();
			}
			catch( Exception e ) {
				imgStrip = null;
			}
		} else {
			// load the file
			imgStrip = ptv.loadImage( imgStripName );
		}
		
		for( int i = 0; i < N_BUTTONS; i++ ) {
			ImageFilter crop = new CropImageFilter( 1 + i*W_BUTTON, 2, W_BUTTON, H_BUTTON );
			buttons[i] = ptv.createImage( new FilteredImageSource( imgStrip.getSource(), crop ));
			ptv.prepareImage( buttons[i], ptv );
		}
		
		extractColors( imgStrip );
	}
	
	// draws a button in the toolbar image at the specified position
	protected void drawButton( int btnIndex, int x, int y ) {
		tlbGraph.drawImage( buttons[btnIndex], x, y, ptv );
	}

	// draws a plus button with the specified status
	protected void drawPlusButton(int btnStatus) {
		int i = btnStatus;
		drawButton( i, X_OFFSET, Y_OFFSET );
		curStatusPlus = btnStatus;
	}

	// draws a minus button with the specified status
	protected void drawMinusButton(int btnStatus) {
		int i = btnStatus + 3;
		drawButton( i, X_OFFSET + W_BUTTON + 1, Y_OFFSET );
		curStatusMinus = btnStatus;
	}

	// draws an hotspot button with the specified status
	protected void drawHSButton(int btnStatus) {
		int i = btnStatus + 6;
		drawButton( i, X_OFFSET + W_BUTTON*2 + 1, Y_OFFSET );
		curStatusHS = btnStatus;
	}

	// draws the image of the toolbar with the current button settings
	protected void drawToolbar() {
				
		// draws the rectangle
		tlbGraph.setColor( borderColor );
		tlbGraph.drawRect( 0, 0, width - 1, height - 1 );
		tlbGraph.setColor( bgColor );
		tlbGraph.fillRect( 1, 1, width - 2, height - 2 );
		
		// draws the buttons
		drawPlusButton( curStatusPlus );
		drawMinusButton( curStatusMinus );

		if( ptv.numhs > 0 && curStatusHS == BTN_STATUS_DISABLED ) curStatusHS = BTN_STATUS_NORMAL;
		drawHSButton( curStatusHS );
		
	}

	// reads the border, progress bar and background color from the toolbar image
	protected void extractColors( Image imgStrip ) {
		int[] pixel = new int[1];
		int r, g, b;
		
		PixelGrabber pg = new PixelGrabber( imgStrip, 0, 0, 1, 1, pixel, 0, 1 );
		try {
			pg.grabPixels();
			r = (pixel[0] >> 16) & 0xff;
			g = (pixel[0] >>  8) & 0xff;
			b = (pixel[0]      ) & 0xff;
			borderColor = new Color( r, g, b );
		}
		catch( Exception e ) {
			borderColor = new Color( 0, 0, 0 );		// in case of error defaults to black
		}
		
		pg = new PixelGrabber( imgStrip, 306, 11, 1, 1, pixel, 0, 1 );
		try {
			pg.grabPixels();
			r = (pixel[0] >> 16) & 0xff;
			g = (pixel[0] >>  8) & 0xff;
			b = (pixel[0]      ) & 0xff;
			barColor = new Color( r, g, b );
		}
		catch( Exception e ) {
			barColor = new Color( 0, 60, 116 );		// default in case of error
		}

		pg = new PixelGrabber( imgStrip, 298, 11, 1, 1, pixel, 0, 1 );
		try {
			pg.grabPixels();
			r = (pixel[0] >> 16) & 0xff;
			g = (pixel[0] >>  8) & 0xff;
			b = (pixel[0]      ) & 0xff;
			bgColor = new Color( r, g, b );
		}
		catch( Exception e ) {
			bgColor = new Color( 236, 233, 216 );		// default in case of error
		}

	}
	
	// draws the progress bar
	protected void drawProgressBar() {
		if( barPerc <= 0 || barPerc > 100 ) return;
		int barWidth = barTotWidth * barPerc / 100;
		tlbGraph.setColor( barColor );
		tlbGraph.fillRect( barX, barY, barWidth, BAR_HEIGHT );
	}
	
	// sets the bold flag for the toolbar text
	public void setMsgBold( boolean isBold ) {
		msgBold = isBold;
	}
	
}

	