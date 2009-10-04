/* PTViewer	-	Interactive Viewer for Panoramic Images
   Copyright (C) 2000 - Helmut Dersch  der@fh-furtwangen.de
   
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
 * PTViewer 2.8
 * 
 * Based on version 2.5 by Helmut Dersch
 * Modified by Fulvio Senore (fsenore@ica-net.it)
 * 
 * Version 2.7L2:
 *  beta1 
 *   added parameter value quality=4 to activate Lanczos2 interpolator
 *   speeded up panning (bilinear only)
 *   fixed: the mouse cursor did not change to HAND when over a hotspot and using the Sun VM
 *   fixed: pressing the spacebar did not show hotspots until the pano was dragged
 *   fixed: a custom mouse cursor did not show up until the mouse was moved
 *  beta2
 *   now when panning very slowly the image does not move in strange ways any more
 *  beta3
 *   panning and zooming with the keyboard now uses bilinear instead of Lanczos2 
 *     so it's faster (thanks to Vladimir Simunic)
 *   now if using the Sun VM V1.4 or later ptviewer draws the panorama frames using
 *     hardware accelerated graphics. On Windows it is now faster than with the Microsoft VM
 *  beta4
 *   added new parameter "dynLoadROIs" (=true) to dynamically load a panorama sliced as ROIs
 *     depending on the current view direction 
 *   fixed: when diplaying a wait image with the Sun VM the background of the applet
 *     was not set to the background colour
 *   modified ptzoom.java in order to make it work with quality==4. At the moment the image is
 *     rendered with bilinear interpolation only
 *   added new parameter "hsEnableVisibleOnly" (default false). If set to true hotspots
 *     will be disabled is not visible
 *  beta5
 *   fixed: with the beta4 version, under certain circumstances, text hotspots did not
 *     use the bgcolor setting  
 *   small speedup in ptImageTo2DArray()
 *   added new parameter showToolbar (default false) if true shows an integrated toolbar
 *   added new parameter imgLoadFeedbak (default true): if set to false ptviewer will
 *     not give feedback of the image loading progress
 *   added new parameter toolbarImage: name of the image used to draw the toolbar
 *     default: Toolbar.gif file contained in the jar file
 *  beta6
 *    fixed: if the panorama was not fully spherical hotspots were displayed at a
 *      wrong y coordinate
 *    fixed: the toolbar sometimes incorrectly showed an OVER button when leaving the
 *      applet passing over a button. Now the toolbar handles also the mouseExit event 
 *    fixed: the toolbad did not update the HS button when using the 
 *      ptviewer.showHS() and ptviewer.hideHS() methods
 *    added a flag to avoid a lot of unnecessary paint()s while dynLoading ROIs
 *      now loading is much faster
 *    fixed: with some computers the zoom buttons did not work properly because
 *      ptviewer.paint() did not execute
 *    added a limit to maximum zoom speed with the toolbar to avoid too fast panning 
 *      with very fast computers
 *  beta7
 *    fixed: the previous fix to avoid unnecessary paint()s made it impossible to pan
 *      while dynLoading the pano
 *    fixed: calling moveTo() or gotoView() from a link while dynLoading ROIs caused
 *      ptviewer to keep panning until you clicked with the mouse. Now these functions 
 *      are disabled while dynLoading
 *    modified the gotoView(): now if the requested fov is too large to fit in the current
 *      pano, it is reduced. In previous versions this caused the function to do nothing.
 *      this function was also called by moveTo(), that had the same problem
 *    if the tilt parameters requests an impossible direction of view (outside the pano)
 *      now the the tilt angle is set to the maximum or minimum possible value. previous versions
 *      set it to 0
 *    if using the toolbar the hotspot's description is also written to the toolbar when
 *      the mouse moves over the hotspot
 *      previous versions only wrote it to the browser's status bar
 *    fixed bug: the viewer crashed if the parameters 
 *      specified a wait image and the integrated toolbar
 *  beta8
 *    added new parameter "autoTime". Used with the auto parameter, it sets the time in seconds
 *      for a full 360 degrees pano revolution
 *    added public method startAutoPan( pan_inc,  tilt_inc, zoom, autoTime ): the last
 *      parameter works like the autoTime applet parameter
 *    now the toolbar is painted before static hotspots, 
 *      so it is possible to draw shotspots over the toolbar
 *    added new parameter "toolbarDescr_x" to set the x coordinate (in pixels) of the
 *      hotspots description in the toolbar
 *    added new paramater "toolbarText_color" to set the color of the hotspots description 
 *      in the toolbar. The default is black. This value is overridden by the "c" parameter
 *      in the hotspot's definition (if any) 
 *    modified the PTViewerScript() function: now the last command can end with a ";"
 *      without causing an exception
 * 
 *
 * Version 2.7.1L2:
 * 
 *  beta1
 *    added new parameter "shsEnableVisibleOnly" (default false). If set to true static hotspots
 *      will be disabled is not visible
 *    fixed: when loading new panos with newPanoFromList() the toolbar did reset its
 *      properties (text color and position) to the default. Now it keeps the values
 *      set with <param> tags.
 *    fixed: when loading new panos with newPanoFromList() from a hotspot the hotspot
 *      button of the toolbar went out of sync
 *    fixed: when using the toolbar and the parameter "view_height" the toolbar moved up
 *      each time that a pano was loaded with newPanoFromList()
 *    changed: now the tiltmax parameter accepts negative values and the tiltmin parameter
 *      accepts positive ones (hard to believe, but somebody asked for it!)
 *    added two public methods: getPanoIsLoaded() and getFilename()
 * 
 *  beta2
 *    modified scaleImage() to avoid image shift wit some image sizes
 *    added parameter "popup_panning", originally added by David Buxo to his version of PTViewer
 *    Tore Meyer (Tore.Meyer@gmx.de) added optional parameter "autoTime" to moveTo() and moveFromTo() 
 * 
 * Version 2.8:
 * 
 *  beta1
 *    modified the values for the "quality" parameter:
 *      now 4 means nearest neighbour when panning, lanczos2 when steady,
 *          5 means bilinear when panning, lanczos2 when steady.
 *          6 means nn if panning fast, bil if panning slowly, lanczos2 if steady
 *      the new default is 6
 *    modified math_transform to optimize also nn interpolation
 *    rewritten math_transform() to enable interpolation of the geometric transform
 *      between lines. Now the transform is computed using longs instead of ints to
 *      achieve more resolution: zooming in a lot and panning slowly does not cause strange
 *      movements anymore
 *    Rik Littlefield (rj.littlefield@computer.org) has rewritten the image loading code:
 *      now it is much faster, expecially with larger images
 *      As a consequence the "maxarray" is no longer used by the applet. Using maxarray 
 *      will not cause errors, but it will not be used.
 * 
 *   beta2
 *    now dynamic loading of ROIs is faster. This is more visible once the images are
 *      cached in the local computer. No more drawing on invisible ROIs, drawing uses bilinear
 *      to be faster.
 *    fixed bug: when using nn interpolator and a rectangular hotspot the hotspot was not
 *      painted correctly while panning
 *    added new parameter "mouseSensitivity", it is a decimal number, default = 1
 *      if mouseSensitivity < 1 panning will be slower
 *      if mouseSensitivity > 1 panning will be faster
 *    added new parameter "mouseQ6Threshold" used only if quality=6. It is a decimal number, default = 1
 *      mouseQ6Threshold > 1 will require a larger mouse movement to switch from bilinear to nn
 *      mouseQ6Threshold < 1 will require a smaller mouse movement to switch from bilinear to nn
 *    fixed: when loading a pano that was not tall enough to fit in the current window
 *      with the current fov ptviewer reduced too much the fov value.
 *      Now that vale is reduced to the correct value that will no require vertical panning
 * 
 *   beta3
 *    added support for *.ptv and *.ptvref custom files
 * 
 *   beta4
 *    added new parameter "outOfMemoryURL": it is a link to a page to be opened in case of 
 *      out of memory error while loading the pano 
 *    some web servers (like IIS 6) by default do not serve files with unknown extensions, but they
 *      send a "file not found". To bypass this problem .ptv and .ptvref files can be renamed as follows:
 *        pano.ptvref ==> pano.ptvref.txt
 *        pano.ptv ==> pano.ptv.jpg
 *      the trailing extension does not need to be "txt" or "jpg" but they can be any
 *      extension known by the server
 *    added parameter "mousePanTime" used to limit the maximum speed when panning with
 *      the mouse. It works like "autoTime": it is the minimum time (in seconds) needed for a full
 *      360 degrees revolution
 *    fixed: the applet did not load images (like a wait screen) if they were packed in the jar file
 *  
 *   beta5
 *    Rober Bisland (R.Bilsland@Dial.pipex.com) added a new Javascript command, DrawSHSPopup(), 
 *      and new functionality that allows multiple static hotspots to be drawn, hidden and popped up at once.
 *    he also added a new parameter: "shsStopAutoPanOnClick" (default true). If set to false clicking 
 *      on a static hotspot will not stop an AutoPan.
 *    added support for encrypted .ptv files.
 *    Ercan Gigi (ercan.gigi@philips.com) added the "autoNumTurns" parameter which is used 
 *      to limit the number of full 360 degree turns when auto-panning is on.
 * 
 * 	 beta6
 *    changed the encryption keys format from String to byte[] to avoid problems with Mac
 *      and Linux when some bytes have negative values.
 *    now the "Y" key can be used to zoom out, like the "Z" key. This helps with some
 *      keyboard layouts (Germany).
 *    fixed: if an encrypted .ptv file contained a tile smaller than 2000 bytes it
 *      caused the viewer to crash.
 * 
 *   beta7
 *    fixed: when autopanning and quality=6 moving the mouse over the viewer caused
 *      the interpolator to switch to nearest neighbour reducing image quality
 *    added new parameter "horizonPosition" to specify the position of the horizon
 *      if it is not in the middle of the pano image
 *    pressing "o" will interactively decrease the value of the horizonPosition parameter
 *      with feedback in the status bar, pressong "O" will increase it. This is useful to quickly
 *      find the correct value for the parameter. Note that after pressing the key you will 
 *      have to pan in order to see the effect. Hotspots position will not be updated since
 *      it would require more work and this feature is not intended for production use.
 *    added paramether "authoringMode" (default = false). If set to true it will enable 
 *      authoring features that could have unexpected results for end users
 *      at the moment it will enable the "o" key. 
 *    added parameter "toolbarBoldText", default false. If set to true the hotspots description
 *      in the toolber will be written in bold
 *    added parameter "statusMessage": it can be used to specify a fixed text to be written
 *      in the status bar. The text is written everytime the user drags the mouse or presses an arrow key
 *      this parameter does not work with Firefox (for security reasons)
 *    added parameter "hsShowDescrInStatusBar" (default = true). If set to false 
 *      the applet will not show the hotspots' description in the browser's status
 *      bar when the mouse moves over a hotspot
 *    changed default mouse sensitivity because panning was too fast with modern computers
 * 	  added new features in static hotspots declaration: 
 *      "y" and "b" values can be negative: if negative they are computed from the bottom 
 *      of the viewer window and not from the top
 *      "x" and "a" values can be negative: if negative they are computed from the right 
 *      of the viewer window and not from the left
 *      "x" must compute to a positive number lower than "a"
 *      "y" must compute to a positive number lower than "b"
 * 
 *   beta8
 *    fixed: when using param "pano0" and so on, the handling of parameters between
 *      "{" and "}" was case sensitive while normal parameters processing was
 *      NOT case sensitive. Now this handling is case insensitive too.
 *    changed: in .ptv files handling, references to the web address hosting the image
 *      (used to decrypt an encrypted image)
 *      were done using getCodeBase(). Now they are done using getDocumentBase() so
 *      it should not be possible to create a page on a server that shows a file from
 *      a second server using the copy of ptviewer stored in the second server
 *      (image stealing).
 * 
 */


import java.applet.*;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

public class ptviewer extends Applet implements Runnable {

	public ptviewer() {
// FS+
		quality = 6;
		backBuffer = null;
		tlbObj = null;
		dynLoadROIs = false;
		loadingROI = false;
		imgLoadFeedback = true;
		outOfMemoryURL = null;
		statusMessage = null;
		showToolbar = false;
		hsShowDescrInStatusBar = true;
		hsEnableVisibleOnly = false;
		shsEnableVisibleOnly = false;
		shsStopAutoPanOnClick = true;
		popupPanning = false;
		lastMouseX = lastMouseY = -1;
		tlbImageName = null;
		org_vheight = 0;
		usingCustomFile = false;
// FS-
		inited = false;
		bgcolor = null;
		waittime = 0L;
		WaitDisplayed = false;
		view = null;
		dwait = null;
		frame = null;
		offImage = null;
		offGraphics = null;
		offwidth = 0;
		offheight = 0;
		source = null;
		awidth = 320;
		aheight = 200;
		vwidth = 0;
		vheight = 0;
		vset = false;
		vx = 0;
		vy = 0;
		pwidth = 0;
		pheight = 0;
		vdata = null;
		hs_vdata = null;
		pdata = null;
		show_pdata = true;
		ready = false;
		hsready = false;
		PanoIsLoaded = false;
		fatal = false;
		mouseInWindow = true;
		mouseInViewer = true;
		panning = false;
		dirty = true;
		showhs = false;
		showCoordinates = false;
		oldx = 0;
		oldy = 0;
		newx = 0;
		newy = 0;
		ptcursor = 0;
		yaw = 0.0D;
		hfov = 70D;
		hfov_min = 10.5D;
		hfov_max = 165D;
		pitch = 0.0D;
		pitch_max = 90D;
		pitch_min = -90D;
		yaw_max = 180D;
		yaw_min = -180D;
		MASS = 0.0D;
		oldspeedx = 0.0D;
		oldspeedy = 0.0D;
		autopan = 0.0D;
		autopanFrameTime = 0;
		autotilt = 0.0D;
		zoom = 1.0D;
		pan_steps = 20D;
		filename = null;
		inits = null;
		MouseOverHS = null;
		GetView = null;
		click_x = -1;
		click_y = -1;
		frames = 0L;
		lastframe = 0L;
		ptimer = 0L;
		loadPano = null;
		ptviewerScript = null;
		PTScript = null;
		PTViewer_Properties = null;
		loadAllRoi = true;
		CurrentPano = -1;
		sender = null;
		preloadthread = null;
		preload = null;
		order = null;
		antialias = false;
		scaledPanos = null;
		max_oversampling = 1.5D;
		im_maxarray = 0x80000;
		grid_bgcolor = 0xffffff;
		grid_fgcolor = 0;
		file_Cache = null;
		file_cachefiles = true;
		pb_color = Color.gray;
		pb_x = -1;
		pb_y = -1;
		pb_width = -1;
		pb_height = 10;
		percent = null;
		numshs = 0;
		curshs = -1;
		shotspots = null;
		atan_LU_HR = null;
		atan_LU = null;
		dist_e = 1.0D;
		PV_atan0_HR = 0;
		PV_pi_HR = 0;
		numroi = 0;
		sounds = null;
		applets = null;
		app_properties = null;
		hotspots = null;
		numhs = 0;
		curhs = -1;
		hs_image = null;
		horizonPosition = 50;
		authoringMode = false;
	}

	public ptviewer(int ai[][]) {
		quality = 6;
		dynLoadROIs = false;
		loadingROI = false;
		showToolbar = false;
		imgLoadFeedback = true;
		outOfMemoryURL = null;
		outOfMemoryURL = null;
		lastMouseX = lastMouseY = -1;
		hsShowDescrInStatusBar = true;
		hsEnableVisibleOnly = false;
		shsEnableVisibleOnly = false;
		shsStopAutoPanOnClick = true;
		popupPanning = false;
		tlbImageName = null;
		org_vheight = 0;
		usingCustomFile = false;
		ai = null;
		inited = false;
		bgcolor = null;
		waittime = 0L;
		WaitDisplayed = false;
		view = null;
		dwait = null;
		frame = null;
		offImage = null;
		backBuffer = null;
		tlbObj = null;
		offGraphics = null;
		offwidth = 0;
		offheight = 0;
		source = null;
		awidth = 320;
		aheight = 200;
		vwidth = 0;
		vheight = 0;
		vset = false;
		vx = 0;
		vy = 0;
		pwidth = 0;
		pheight = 0;
		vdata = null;
		hs_vdata = null;
		pdata = null;
		show_pdata = true;
		ready = false;
		hsready = false;
		PanoIsLoaded = false;
		fatal = false;
		mouseInWindow = true;
		mouseInViewer = true;
		panning = false;
		dirty = true;
		showhs = false;
		showCoordinates = false;
		oldx = 0;
		oldy = 0;
		newx = 0;
		newy = 0;
		ptcursor = 0;
		yaw = 0.0D;
		hfov = 70D;
		hfov_min = 10.5D;
		hfov_max = 165D;
		pitch = 0.0D;
		pitch_max = 90D;
		pitch_min = -90D;
		yaw_max = 180D;
		yaw_min = -180D;
		MASS = 0.0D;
		oldspeedx = 0.0D;
		oldspeedy = 0.0D;
		autopan = 0.0D;
		autopanFrameTime = 0;
		autotilt = 0.0D;
		zoom = 1.0D;
		pan_steps = 20D;
		filename = null;
		inits = null;
		MouseOverHS = null;
		GetView = null;
		click_x = -1;
		click_y = -1;
		frames = 0L;
		lastframe = 0L;
		ptimer = 0L;
		loadPano = null;
		ptviewerScript = null;
		PTScript = null;
		PTViewer_Properties = null;
		loadAllRoi = true;
		CurrentPano = -1;
		sender = null;
		preloadthread = null;
		preload = null;
		order = null;
		antialias = false;
		scaledPanos = null;
		max_oversampling = 1.5D;
		im_maxarray = 0x80000;
		grid_bgcolor = 0xffffff;
		grid_fgcolor = 0;
		file_Cache = null;
		file_cachefiles = true;
		pb_color = Color.gray;
		pb_x = -1;
		pb_y = -1;
		pb_width = -1;
		pb_height = 10;
		percent = null;
		numshs = 0;
		curshs = -1;
		shotspots = null;
		atan_LU_HR = null;
		atan_LU = null;
		PV_atan0_HR = 0;
		PV_pi_HR = 0;
		numroi = 0;
		sounds = null;
		applets = null;
		app_properties = null;
		hotspots = null;
		numhs = 0;
		curhs = -1;
		hs_image = null;
		pdata = ai;
		PanoIsLoaded = true;
		math_setLookUp(pdata);
		filename = "Pano";
		horizonPosition = 50;
		authoringMode = false;
	}

	void initialize() {
		numhs = 0;
		curhs = -1;
		curshs = -1;
		numroi = 0;
		loadAllRoi = true;
		yaw = 0.0D;
		hfov = 70D;
		hfov_min = 10.5D;
		hfov_max = 165D;
		pitch = 0.0D;
		pitch_max = 90D;
		pitch_min = -90D;
		yaw_max = 180D;
		yaw_min = -180D;
		autopan = 0.0D;
		autopanFrameTime = 0;
		autotilt = 0.0D;
		zoom = 1.0D;
		pwidth = 0;
		pheight = 0;
		stopPan();
		lastframe = 0L;
		dirty = true;
		showhs = false;
		showCoordinates = false;
		MouseOverHS = null;
		GetView = null;
		WaitDisplayed = false;
		pan_steps = 20D;
		order = null;
		horizonPosition = 50;
	}

	public void init() {
		fatal = false;
		preloadthread = null;
		preload = null;
		ptcursor = 0;
		file_init();
		math_init();
		// FS+
		useVolatileImage = canUseAcceleratedGraphic();
		lanczos2_init();
		if( useVolatileImage )
			vImgObj = new vimage( this );
		// FS-
		pb_init();
		app_init();
		snd_init();
		shs_init();
		hs_init();
		sender = new Hashtable();
		inited = true;
		repaint();
		byte abyte0[];
		if ((abyte0 = file_read("PTDefault.html", null)) != null)
			PTViewer_Properties = new String(abyte0);
		initialize();
		if (PTViewer_Properties != null)
			ReadParameters(PTViewer_Properties);
		ReadParameters(null);
		if (filename != null && filename.startsWith("ptviewer:")) {
			int i =
				Integer.parseInt(filename.substring(filename.indexOf(':') + 1));
			if (myGetParameter(null, "pano" + i) != null) {
				filename = null;
				ReadParameters(myGetParameter(null, "pano" + i));
			}
		}
	}

	public String getAppletInfo() {
		return "PTViewer version 2.8 - Based on 2.5 by Helmut Dersch - Modified by Fulvio Senore www.fsoft.it/panorama/ptviewer.htm";
	}

	public void start() {
		if (loadPano == null) {
			loadPano = new Thread(this);
			loadPano.start();
		}
	}

	public synchronized void stop() {
		stopThread(preloadthread);
		preloadthread = null;
		stopThread(loadPano);
		loadPano = null;
		stopAutoPan();
		stopPan();
		stopApplets(0);
		ready = false;
		hsready = false;
		vdata = null;
		hs_vdata = null;
		view = null;
/*
		if (!vset) {
			vwidth = 0;
			vheight = 0;
		}
		offImage = null;
		backBuffer = null;
//		tlbObj = null;
		if( tlbObj != null ) ((toolbar) tlbObj).setMessage( "" );
		scaledPanos = null;
*/
	}

	synchronized void PV_reset() {
		ready = false;
		hsready = false;
		hs_dispose();
		roi_dispose();
		PanoIsLoaded = false;
		filename = null;
		MouseOverHS = null;
		GetView = null;
		pb_reset();
		inits = null;
		order = null;
		System.gc();
	}

	public synchronized void destroy() {
		stopThread(ptviewerScript);
		ptviewerScript = null;
		PV_reset();
		if (sender != null) {
			sender.clear();
			sender = null;
		}
		vdata = null;
		hs_vdata = null;
		source = null;
		frame = null;
		view = null;
		dwait = null;
		pdata = null;
		math_dispose();
		shs_dispose();
		snd_dispose();
		System.gc();
	}
	
	// computes some parameters depending on the horizon position
	protected void CheckHorizonPosition() {
		deltaYHorizonPosition = (100 - 2*horizonPosition)*pheight/100;
		if (pheight != pwidth >> 1) {
			double d = ((double) pheight / (double) pwidth) * 180D;
			double deltaPitch = ((double) deltaYHorizonPosition/(double) pwidth)*180;
			pitch_min = pitch_min_org;
			pitch_max = pitch_max_org;
			if (pitch_max > (d - deltaPitch) )
				pitch_max = d - deltaPitch;
			if (pitch_min < (-d - deltaPitch) )
				pitch_min = -d - deltaPitch;
		}
	}

	public void run() {
		int k;
		try {
			// added a try block to catch out of memory error
			if (Thread.currentThread() == preloadthread && preload != null) {
				int i;
				k = getNumArgs(preload, ',');
				i = k;
				if (k > 0) {
					for (int j = 0; j < i; j++) {
						String s1;
						if ((s1 = getArg(j, preload, ',')) != null
								&& file_cachefiles && file_Cache != null
								&& file_Cache.get(s1) == null && s1 != filename)
							file_read(s1, null);
					}

				}
				return;
			}
			if (Thread.currentThread() == ptviewerScript) {
				if (PTScript != null)
					PTViewerScript(PTScript);
				return;
			}
			ResetCursor();
			if (!PanoIsLoaded) {
				show_pdata = true;
				if (filename == null)
					if (pwidth != 0)
						filename = "_PT_Grid";
					else
						show_pdata = false;
				if (filename != null && filename.toLowerCase().endsWith(".mov"))
					pdata = im_loadPano(null, pdata, pwidth, pheight);
				else {
					pdata = im_loadPano(filename, pdata, pwidth, pheight);
					if (showToolbar) {
						((toolbar) tlbObj).setBarPerc(0); // clears the progress
														  // bar
					}
				}
				System.gc();
			}
			if (pdata == null) {
				fatal = true;
				repaint();
				return;
			}
			if (filename != null && filename.toLowerCase().endsWith(".mov"))
				try {
					String s = " {file=" + filename + "} ";
					if (order != null)
						s = s + "{order=" + order + "} ";
					if (antialias) {
						s = s + "{antialias=true} ";
						s = s + "{oversampling=" + max_oversampling + "} ";
					}
					Applet applet;
					(applet = (Applet) Class.forName("ptmviewer")
							.getConstructor(
									new Class[] { Class.forName("ptviewer"),
											java.lang.String.class })
							.newInstance(new Object[] { this, s })).init();
					applet.start();
					System.gc();
				} catch (Exception _ex) {
				}
			pheight = pdata.length;
			pwidth = pdata[0].length;
			
			// check if the horizon is not in the middle of the image
			pitch_min_org = pitch_min;
			pitch_max_org = pitch_max;
			CheckHorizonPosition();

			if (hfov > yaw_max - yaw_min)
				hfov = yaw_max - yaw_min;
			if (!PanoIsLoaded)
				math_setLookUp(pdata);
			finishInit(PanoIsLoaded);
			if( statusMessage != null ) showStatus( statusMessage );
		} catch (OutOfMemoryError ex) {
			if (outOfMemoryURL != null) {
				// opens a page that should contain an explication of the out of memory problem
				JumpToLink( outOfMemoryURL, null );
			} else {
				throw ex;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void finishInit(boolean flag) {
		if (!flag)
			shs_setup();
		ready = true;
		requestFocus();
		ResetCursor();
		repaint();
		paint(getGraphics());
		if (loadAllRoi && !PanoIsLoaded) {
			if( dynLoadROIs )
				loadROI_dyn();
			else
				loadROI(0, numroi - 1);
		}
		if( !PanoIsLoaded && usingCustomFile ) {
			ptvf.loadTiles();
		}
		if (!PanoIsLoaded)
			hs_setup(pdata);
		hsready = true;
		PanoIsLoaded = true;
		if (autopan != 0.0D)
		{			
			// E.Gigi - 2005.06.12
			if (autoNumTurns != 0.0D)
				lastframe = frames + (int) (autoNumTurns*360/autopan);
			else
				lastframe = frames + 0x5f5e100L;
		}
		int i;
		if (inits != null)
			if ((i = inits.indexOf('*')) == -1)
				JumpToLink(inits, null);
			else
				JumpToLink(inits.substring(0, i), inits.substring(i + 1));
		// FS+
		// to be able to show hotspots before panning
		dirty = true;
		if( tlbObj != null ) ((toolbar) tlbObj).syncHSButton();		// the hs button remains pressed if we omit this line
		// to show a cursor different from default at startup
		if( ptcursor != 0 ) 
			setCursor(Cursor.getPredefinedCursor(ptcursor));
		// FS-
		repaint();
		SetupSounds();
		if (preload != null && preloadthread == null) {
			preloadthread = new Thread(this);
			try {
				preloadthread.setPriority(1);
			} catch (SecurityException _ex) {
			}
			preloadthread.start();
		}
	}

	public boolean mouseDown(Event event, int i, int j) {
		boolean performStopAutoPan;

		if( tlbObj != null ) ((toolbar) tlbObj).mouseDown( i, j );

		// status bar message
		if( statusMessage != null ) showStatus( statusMessage );
		
		lastMouseX = i;
		lastMouseY = j;
		if (i >= vx && i < vx + vwidth && j >= vy && j < vy + vheight) {
			if (lastframe > frames) {
				stopThread(ptviewerScript);
				ptviewerScript = null;
				performStopAutoPan = true;
				if (!shsStopAutoPanOnClick) {
					if (hsready) {
						if (curshs >= 0) {
							performStopAutoPan = false;
						}
					}
				}
				if (performStopAutoPan) {
					stopAutoPan();
				}
				oldx = i;
				oldy = j;
				return true;
			}
			if (showCoordinates) {
				showStatus(DisplayHSCoordinates(i - vx, j - vy));
				showCoordinates = false;
				return true;
			}
		}
		if (!panning && mouseInViewer) {
			oldx = i;
			oldy = j;
			if (curhs < 0) {
				panning = true;
				if (event.shiftDown())
					zoom = 0.970873786407767D;
				else if (event.controlDown())
					zoom = 1.03D;
				else
					zoom = 1.0D;
				repaint();
				PVSetCursor(i, j);
			}
		}
		newx = i;
		newy = j;
		return true;
	}

	public boolean mouseDrag(Event event, int i, int j) {

		if( tlbObj != null ) ((toolbar) tlbObj).mouseDrag( i, j );

		lastMouseX = i;
		lastMouseY = j;
		newx = i;
		newy = j;
		if (mouseInViewer) {
			panning = true;
			if (event.shiftDown())
				zoom = 0.970873786407767D;
			else if (event.controlDown())
				zoom = 1.03D;
			else
				zoom = 1.0D;
			ResetCursor();
		}
		repaint();
		return true;
	}

	public boolean mouseUp(Event event, int i, int j) {

		if( tlbObj != null ) ((toolbar) tlbObj).mouseUp( i, j );

		lastMouseX = i;
		lastMouseY = j;
		newx = i;
		newy = j;
		stopPan();
		zoom = 1.0D;
		if (hsready) {
			if (curshs >= 0) {
				for (int k = 0; k < numshs; k++)
					if (shs_active[k])
						gotoSHS(k);

			} else if (curhs >= 0) {
				gotoHS(curhs);
				for (int l = curhs + 1; l < numhs && curhs != -1; l++)
					if (hs_link[l] == curhs)
						gotoHS(l);

				if (curhs < 0)
					return true;
			}
			PVSetCursor(i, j);
			click_x = i;
			click_y = j;
		}
		return true;
	}

	public boolean mouseEnter(Event event, int i, int j) {
		lastMouseX = i;
		lastMouseY = j;
		mouseInWindow = true;
		mouseInViewer = is_inside_viewer(i, j);
		PVSetCursor(i, j);
		return true;
	}

	public boolean mouseExit(Event event, int i, int j) {

		if( tlbObj != null ) ((toolbar) tlbObj).mouseExit( i, j );

		lastMouseX = i;
		lastMouseY = j;
		mouseInWindow = mouseInViewer = false;
		stopPan();
		zoom = 1.0D;
		ResetCursor();
		return true;
	}

	public boolean keyDown(Event event, int i) {
		if (!ready)
			return true;
		switch (i) {
			default :
				break;
			case 1004 :
				if( statusMessage != null ) showStatus( statusMessage );
				keyPanning = true;
				panUp();
				break;

			case 1005 :
				if( statusMessage != null ) showStatus( statusMessage );
				keyPanning = true;
				panDown();
				break;

			case 1006 :
				if( statusMessage != null ) showStatus( statusMessage );
				keyPanning = true;
				panLeft();
				break;

			case 1007 :
				if( statusMessage != null ) showStatus( statusMessage );
				keyPanning = true;
				panRight();
				break;

			case 43 : // '+'
			case 46 : // '.'
			case 61 : // '='
			case 62 : // '>'
			case 65 : // 'A'
			case 97 : // 'a'
				keyPanning = true;
				ZoomIn();
				break;

			case 44 : // ','
			case 45 : // '-'
			case 60 : // '<'
			case 90 : // 'Z'
			case 95 : // '_'
			case 122 : // 'z'
			case 'y' :
			case 'Y' :
				keyPanning = true;
				ZoomOut();
				break;

			case 32 : // ' '
				toggleHS();
				break;

			case 73 : // 'I'
			case 105 : // 'i'
				showStatus(getAppletInfo());
				break;

			case 118 : // 'v'
				showStatus(
					"pan = "
						+ (double) (int) (yaw * 100D) / 100D
						+ "deg; tilt = "
						+ (double) (int) (pitch * 100D) / 100D
						+ "deg; fov = "
						+ (double) (int) (hfov * 100D) / 100D
						+ "deg");
				break;

			case 80 : // 'P'
			case 112 : // 'p'
				showStatus(m1());
				break;

			case 85 : // 'U'
			case 117 : // 'u'
				showStatus(getDocumentBase().toString());
				break;

			case 104 : // 'h'
				showCoordinates = true;
				showStatus("Click Mouse to display X/Y Coordinates");
				break;

			case 10 : // '\n'
				if (!hsready)
					break;
				if (curshs >= 0) {
					for (int j = 0; j < numshs; j++)
						if (shs_active[j])
							gotoSHS(j);

					break;
				}
				if (panning || curhs < 0)
					break;
				gotoHS(curhs);
				for (int k = curhs + 1; k < numhs && curhs != -1; k++)
					if (hs_link[k] == curhs)
						gotoHS(k);

				if (curhs < 0)
					return true;
				break;
				
			case 'O' :
				// moves the horizon up
				if( authoringMode && horizonPosition < 100 ) {
					horizonPosition++;
					CheckHorizonPosition();
					showStatus( "horizonPosition = " + horizonPosition );
					dirty = true;
					// the double call is needed to properly update che screen
					// I don't know why but if I call "gotoView( yaw, pitch, hfov )" thet is what I need the results are not correct
					gotoView( yaw + 1, pitch, hfov );
					gotoView( yaw - 1, pitch, hfov ); 
					repaint();
				}
				break;
			case 'o' :
				// moves the horizon down
				if( authoringMode && horizonPosition > 0 ) {
					horizonPosition--;
					CheckHorizonPosition();
					showStatus( "horizonPosition = " + horizonPosition );
					dirty = true;
					gotoView( yaw + 1, pitch, hfov );
					gotoView( yaw - 1, pitch, hfov );
					repaint();
				}
				break;
		}
		return true;
	}

	public boolean keyUp(Event event, int i) {
		if (!ready)
			return true;

		switch (i) {
			default :
				break;

			case 1004 :
				keyPanning = false;
				break;

			case 1005 :
				keyPanning = false;
				break;

			case 1006 :
				keyPanning = false;
				break;

			case 1007 :
				keyPanning = false;
				break;

			case 43 : // '+'
			case 46 : // '.'
			case 61 : // '='
			case 62 : // '>'
			case 65 : // 'A'
			case 97 : // 'a'

				keyPanning = false;
				break;

			case 44 : // ','
			case 45 : // '-'
			case 60 : // '<'
			case 90 : // 'Z'
			case 95 : // '_'
			case 122 : // 'z'

				keyPanning = false;
				break;
		}
		return true;
	}


	public boolean mouseMove(Event event, int i, int j) {
		lastMouseX = i;
		lastMouseY = j;
		mouseInViewer = is_inside_viewer(i, j);
		if (mouseInWindow) {
			newx = i;
			newy = j;
		}
		PVSetCursor(i, j);
		
		if( tlbObj != null ) ((toolbar) tlbObj).mouseMove( i, j );
		
		return true;
	}

	void PVSetCursor(int i, int j) {
		if (!mouseInWindow) {
			ResetCursor();
			return;
		}
		int k;
		if (!ready)
			k = -1;
		else
			k = OverStaticHotspot(i, j);
		if (k != curshs) {
			curshs = k;
			if (curshs >= 0) {
				try {
					setCursor(Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));
//					((Frame) getParent()).setCursor(12);
				} catch (Exception _ex) {
				}
				curhs = -1;
				repaint();
				return;
			}
			ResetCursor();
			repaint();
		}
		if (curshs < 0) {
			if ((panning && !popupPanning) || lastframe > frames || !mouseInViewer) {
				curhs = -1;
				ResetCursor();
				return;
			}
			int l;
			if (!hsready)
				l = -1;
			else
				l = OverHotspot(i - vx, j - vy);
			if (l != curhs) {
				curhs = l;
				if (curhs >= 0) {
					try {
						setCursor(Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));
//						((Frame) getParent()).setCursor(12);
						if (hsready) {
							if( hsShowDescrInStatusBar ) showStatus(hs_name[curhs]);
							if( tlbObj != null ) {
								if( hs_hc[curhs] != null )
									((toolbar) tlbObj).setMessage( hs_name[curhs], hs_hc[curhs] );
								else
									((toolbar) tlbObj).setMessage( hs_name[curhs] );
							} 
							hs_exec_popup(curhs);
							repaint();
							sendHS();
						}
						return;
					} catch (Exception _ex) {
					}
				} else {
					ResetCursor();
					repaint();
					if( hsShowDescrInStatusBar ) showStatus("");
					if( tlbObj != null ) ((toolbar) tlbObj).setMessage( "" );
					sendHS();
					return;
				}
			}
			if (curhs < 0)
				ResetCursor();
		}
	}

	void ResetCursor() {
		try {
			if (mouseInViewer) {
				if (!ready) {
					setCursor(Cursor.getPredefinedCursor( 3 ));
					return;
				}
				if (getCursor().getType() != ptcursor) {
					setCursor(Cursor.getPredefinedCursor( ptcursor ));
					return;
				}
			} else if (getCursor().getType() != 0) {
				setCursor(Cursor.getPredefinedCursor( 0 ));
				return;
			}
		} catch (Exception _ex) {
		}
	}
	
	
//	void ResetCursor() {
//		try {
//			if (mouseInViewer) {
//				if (!ready) {
//					((Frame) getParent()).setCursor(3);
//					return;
//				}
//				if (((Frame) getParent()).getCursorType() != ptcursor) {
//					((Frame) getParent()).setCursor(ptcursor);
//					return;
//				}
//			} else if (((Frame) getParent()).getCursorType() != 0) {
//				((Frame) getParent()).setCursor(0);
//				return;
//			}
//		} catch (Exception _ex) {
//		}
//	}


	void sendView() {
		if (GetView != null && ready && loadPano != null)
			executeJavascriptCommand(
				GetView + "(" + yaw + "," + pitch + "," + hfov + ")");
	}

	void sendHS() {
		if (MouseOverHS != null && ready && loadPano != null)
			executeJavascriptCommand(MouseOverHS + "(" + curhs + ")");
	}

	public void update(Graphics g) {
		paint(g);
	}

public synchronized void paint(Graphics g) {

		long t;
		t = System.currentTimeMillis();

		if( onlyPaintToolbar ) {
			if( showToolbar ) ((toolbar) tlbObj).paint( g );
			onlyPaintToolbar = false;
			paintDone = true;
			forceBilIntepolator = false;
			return;
		}
		
		// avoids unnecessary paints that slow down ROI loading
		if( loadingROI && dynLoadROIs && !panning ) return;
// System.out.println( "Paint");

		if (inited) {
			if (fatal) {
				setBackground(Color.red);
				g.clearRect(0, 0, getSize().width, getSize().height);
				return;
			}
			if (offImage == null) {
				awidth = getSize().width;
				aheight = getSize().height;
				if (!vset || offwidth == 0) {
					offwidth = getSize().width;
					offheight = getSize().height;
				}
				offImage = createImage(offwidth, offheight);
				offGraphics = offImage.getGraphics();
				
				// sets the size of the toolbar object
				if( showToolbar ) {
					int w, h;
					if( vwidth == 0 )
						w = offwidth;
					else
						w = vwidth;
					if( vheight == 0 )
						h = offheight;
					else
						h = vheight;
					((toolbar) tlbObj).setViewerSize( w, h, vx, vy );
				} 
			}
			if (!ready || System.currentTimeMillis() < ptimer) {
				if (dwait != null) {
					if (bgcolor != null && !WaitDisplayed) {
						  setBackground(bgcolor);
//						offGraphics.clearRect(0, 0, offwidth, offheight);
							Color curColor = offGraphics.getColor();
							offGraphics.setColor(bgcolor);
							offGraphics.fillRect(0, 0, offwidth, offheight);
							offGraphics.setColor(curColor);
					}
					if (!WaitDisplayed) {
						if (waittime != 0L)
							ptimer = System.currentTimeMillis() + waittime;
						WaitDisplayed = true;
					}
					offGraphics.drawImage(
						dwait,
						offwidth - dwait.getWidth(null) >> 1,
						offheight - dwait.getHeight(null) >> 1,
						this);
					if( imgLoadFeedback ) pb_draw(offGraphics, offwidth, offheight);
					if (percent != null && percent[0] > 0) 
						if( showToolbar ) ((toolbar) tlbObj).setBarPerc( percent[0] );
//					g.drawImage(offImage, 0, 0, this);
					// paints the toolbar
					if( showToolbar ) ((toolbar) tlbObj).paint( g );
					if (ready) {
						try {
							Thread.sleep(20L);
						} catch (InterruptedException _ex) {
							return;
						}
						repaint();
						return;
					}
				} else {
					if (bgcolor != null)
						setBackground(bgcolor);
//					g.clearRect(0, 0, getSize().width, getSize().height);
					if (percent != null && percent[0] > 0) {
//						if( imgLoadFeedback ) 
//						  g.drawString(
//								"Loading Image..." + percent[0] + "% complete",
//								30,
//								getSize().height >> 1);
						if( showToolbar ) ((toolbar) tlbObj).setBarPerc( percent[0] );
						if( showToolbar ) ((toolbar) tlbObj).paint( g );
						return;
					}
//					if( imgLoadFeedback ) g.drawString("Loading Image...", 30, getSize().height >> 1);
//					if( showToolbar ) ((toolbar) tlbObj).paint( g );
				}
				return;
			}
			if (vdata == null) {
				if (vwidth == 0)
					vwidth = getSize().width;
				if (vheight == 0) { 
					vheight = getSize().height;
				}
//				else {
//					vheight = org_vheight;
//				}
				if( showToolbar ) {
					// makes room for the toolbar
					vheight -= ((toolbar) tlbObj).getHeight();
				}
				if( math_fovy(hfov, vwidth, vheight) > pitch_max - pitch_min ) {
					// reduces hfov to fit in the current window
					for (;
					math_fovy(hfov, vwidth, vheight) > pitch_max - pitch_min;
					hfov /= 1.03D);
					hfov *= 1.03D;
					for (;
					math_fovy(hfov, vwidth, vheight) > pitch_max - pitch_min;
					hfov /= 1.001D);	// second step needed to have more precision
				}
				double d = math_fovy(hfov, vwidth, vheight) / 2D;
				if (pitch > pitch_max - d && pitch_max != 90D)
					pitch = pitch_max - d;	// sets the highest possible pitch instead of 0
//					pitch = 0.0D;
				if (pitch < pitch_min + d && pitch_min != -90D)
					pitch = pitch_min + d;	// sets the lowest possible pitch instead of 0
//					pitch = 0.0D;
				vdata = new int[vwidth * vheight];
				hs_vdata = new byte[vwidth * vheight];
				if (filename != null
					&& filename.toLowerCase().endsWith(".mov")) {
					for (int k = 0; k < hs_vdata.length; k++)
						hs_vdata[k] = 0;

				} else {
					for (int i1 = 0; i1 < hs_vdata.length; i1++)
						hs_vdata[i1] = -1;

				}
				dirty = true;
				source =
					new MemoryImageSource(vwidth, vheight, vdata, 0, vwidth);
				source.setAnimated(true);
				if (view == null)
					view = createImage(source);
				if (antialias && pdata != null) {
					scaledPanos = new Vector();
					scaledPanos.addElement(pdata);
					int ai2[][] = pdata;
					double d5 =
						hfov_max / ((double) vwidth * 360D * max_oversampling);
					for (int l1 = 0;
						ai2 != null && (double) ai2[0].length * d5 > 1.0D;
						l1++) {
						ai2 = im_halfsize(ai2);
						scaledPanos.addElement(ai2);
					}

				}
			}
			if (panning) {
				double d;
				double scale =
					(((0.00050000000000000001D * hfov) / 70D) * 320D)
						/ (double) vwidth;
				d = (newx - oldx) * mouseSensitivity;
				double speedx =
					((double) (0.3 * d * d)
						* (newx <= oldx ? -1D : 1.0D)
						+ MASS * oldspeedx)
						/ (1.0D + MASS);
				oldspeedx = speedx;
				d = (oldy - newy) * mouseSensitivity;
				double speedy =
					((double) (0.3 * d * d)
						* (oldy <= newy ? -1D : 1.0D)
						+ MASS * oldspeedy)
						/ (1.0D + MASS);
				oldspeedy = speedy;
				double deltaYaw = scale * speedx;
				double deltaPitch = scale * speedy;
				if( mousePanTime > 0 && lastPanningPaintTime > 0 ) {
					double deltaAngle = Math.sqrt( deltaYaw*deltaYaw + deltaPitch*deltaPitch );
					// computes the time needed for a full revolution with this deltaAngle
					double t360 = 360.0/deltaAngle*lastPanningPaintTime/1000.0;
					if( t360 < mousePanTime ) {
						// the user is panning too fast, we need to reduce the angle
						deltaYaw = deltaYaw*t360/mousePanTime;
						deltaPitch = deltaPitch*t360/mousePanTime;
					}
				}
				gotoView(yaw + deltaYaw, pitch + deltaPitch, hfov * zoom);
//				gotoView(yaw + scale * speedx * mouseSensitivity, pitch + scale * speedy * mouseSensitivity, hfov * zoom);
			}
			if (lastframe > frames)
				gotoView(yaw + autopan, pitch + autotilt, hfov * zoom);
			if (hsready && hs_drawWarpedImages(pdata, curhs, showhs))
				dirty = true;
			if (dirty) {
				for (int i = 0; i < vdata.length; i++)
					vdata[i] = 0;

				if (app_properties.size() == 6
					&& filename != null
					&& filename.toLowerCase().endsWith(".mov")) {
					int ai[] = get_cube_order((int) yaw, (int) pitch);
					for (int l = 0; l < 6; l++) {
						Applet applet2;
						if ((applet2 =
							(Applet) applets.get(
								app_properties.elementAt(ai[l])))
							!= null
							&& sender != null
							&& sender.get(applet2) != null) {
							String s1 = applet2.getAppletInfo();
							if (dirty && s1 != null && s1.equals("topFrame"))
								applet2.paint(null);
						}
					}

				} else {
					for (int j = 0; j < app_properties.size(); j++) {
						Applet applet1;
						if ((applet1 =
							(Applet) applets.get(app_properties.elementAt(j)))
							!= null
							&& sender != null
							&& sender.get(applet1) != null) {
							String s = applet1.getAppletInfo();
							if (dirty && s != null && s.equals("topFrame"))
								applet1.paint(null);
						}
					}

				}
				if (dirty && show_pdata ) {
					int ai1[][] = pdata;
					if (antialias && scaledPanos != null) {
						double d3 =
							hfov / ((double) vwidth * 360D * max_oversampling);
						int j1 = 0;
						for (int k1 = pdata[0].length;
							(double) k1 * d3 > 1.0D;
							k1 >>= 1)
							j1++;

						if (scaledPanos.elementAt(j1) != null) {
							ai1 = (int[][]) scaledPanos.elementAt(j1);
							math_updateLookUp(ai1[0].length);
						}
					}
					
					// these variables are only used if ptviewer is set to use Lanczos2
					// they are used to force using a faster interpolator while dynLoading ROIs
					boolean useBilinear = forceBilIntepolator;
					boolean useLanczos2 = !forceBilIntepolator;
					forceBilIntepolator = false;
					switch (quality) {
						default :
							break;

						case 0 : // '\0'
							math_extractview(
								ai1,
								vdata,
								hs_vdata,
								vwidth,
								hfov,
								yaw,
								pitch,
								false,
								false);
							dirty = false;
							break;

						case 1 : // '\001'
							if (panning || lastframe > frames) {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									false,
									false);
							} else {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									true,
									false);
								System.gc();
								dirty = false;
							}
							break;

						case 2 : // '\002'
							if (panning) {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									false,
									false);
							} else {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									true,
									false);
								System.gc();
								dirty = false;
							}
							break;

						case 3 : // '\003'
							math_extractview(
								ai1,
								vdata,
								hs_vdata,
								vwidth,
								hfov,
								yaw,
								pitch,
								true,
								false);
							dirty = false;
							break;
						// FS+
						case 4 : // nn for panning & autopanning, lanczos2 else
							if (panning || lastframe > frames || keyPanning) {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									false,
									false);
							} else {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									useBilinear,
									useLanczos2
									);
								System.gc();
								dirty = false;
							}
							break;
						case 5 : // bilinear for panning & autopanning, lanczos2 else
							if (panning || lastframe > frames || keyPanning) {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									true,
									false);
							} else {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									useBilinear,
									useLanczos2
									);
								System.gc();
								dirty = false;
							}
							break;
						case 6 : // nn for fast panning & autopanning, bilinear for slow panning & autopanning, lanczos2 else
							if (panning || lastframe > frames || keyPanning) {
								// decides if panning is fast or slow
								int FEW_PIXELS = 70;
								boolean fastPanning = false;
								if( panning ) {
									// only if panning with the mouse
									int deltaX = newx - oldx;
									int deltaY = newy - oldy;
									deltaX *= mouseSensitivity/mouseQ6Threshold;
									deltaY *= mouseSensitivity/mouseQ6Threshold;
									if( Math.abs(deltaX)*vwidth/1024 > FEW_PIXELS ) fastPanning = true;
									if( Math.abs(deltaY)*vheight/768 > FEW_PIXELS ) fastPanning = true;
								}
								
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									!fastPanning,
									false);
							} else {
								math_extractview(
									ai1,
									vdata,
									hs_vdata,
									vwidth,
									hfov,
									yaw,
									pitch,
									useBilinear,
									useLanczos2
									);
								System.gc();
								dirty = false;
							}
							break;
						// FS-
					}
				}
				hs_setCoordinates(
					vwidth,
					vheight,
					pwidth,
					pheight,
					yaw,
					pitch,
					hfov);
				sendView();
				frames++;
				source.newPixels();
			}
			if (panning || lastframe > frames)
				PVSetCursor(newx, newy);

			if( useVolatileImage ) {
				vImgObj.setSize(offwidth, offheight);
				vImgObj.drawAcceleratedFrame( g );
			}
			else
				drawFrame( g );
		}

		// notify the toolbar that paint() has finished
		if( tlbObj != null ) ((toolbar) tlbObj).notifyEndPaint();
		
		paintDone = true;	// sets a global flag for synchronization

		t = System.currentTimeMillis() - t;
		if( panning ) lastPanningPaintTime = t;
		
		if( lastframe > frames && autopanFrameTime > 0 ) {
			if( t < autopanFrameTime ) {
				// sleeps some time to avoid too fast autopanning
				try {
					Thread.sleep( Math.round( autopanFrameTime - t ));
				} catch (InterruptedException _ex) {
				}
			}
		}
		
//		System.out.println("Time in paint(): " + t + " ms");

	}

	// creates the backbuffer used to draw frames
	void createBackBuffer() {
		if( backBuffer == null ) {
				backBuffer = createImage(offwidth, offheight);
		}
	}

	// draws a panorama frame
	void drawFrame(Graphics g) {
		createBackBuffer();
		Graphics gBB = backBuffer.getGraphics();
		renderFrame(gBB);
		g.drawImage(backBuffer, 0, 0, this);
	}

	// renders a panorama frame
	void renderFrame(Graphics gBB) {
		gBB.drawImage(view, vx, vy, this);
		if (hsready)
			hs_draw(gBB, vx, vy, vwidth, vheight, curhs, showhs);
		if (frame != null)
		gBB.drawImage(
				frame,
				offwidth - frame.getWidth(null),
				offheight - frame.getHeight(null),
				this);

		// paints the toolbar
		if( showToolbar ) ((toolbar) tlbObj).paint( gBB );

		if (ready)
			shs_draw(gBB);
		Applet applet;
		for (Enumeration enumeration = sender.elements();
			enumeration.hasMoreElements();
			)
			try {
				if ((applet = (Applet) enumeration.nextElement())
					.getAppletInfo()
					!= "topFrame")
					applet.paint(gBB);
			} catch (Exception _ex) {
			}
			
	}


	// tests the java version to see if we can use the VolatileImage class
	boolean canUseAcceleratedGraphic() {
		boolean retVal;
		
		try {
			String s =  System.getProperty( "java.version" );
			retVal = (s.substring( 0, 3 ).compareTo("1.4") >= 0);
		}
		catch( Exception ex ) {
			retVal = false;		
		}
		return retVal;
	}


	// dynamically loads ROIs: loads first the ROIs that are nearest to the direction of view
	void loadROI_dyn(){
		boolean done;
		int nLoaded = 0;
		
		computeRoiYaw();
		computeRoiPitch();
		
		do {
			done = true;
			int iVisible = -1;		// index of the nearest visible roi
			int iNotVisible = -1;	// index of the nearest not visible roi
			double minDistVisible = 10000, minDistNotVisible = 10000;
			// looks for the ROI that is nearest to the direction of view
			for( int k = 0; k < numroi; k++ ){
				if( !roi_loaded[k] ) {
					done = false;
					double distX = Math.abs( yaw - roi_yaw[k] );
					if( distX > 180 ) distX = 360 - distX;
					double distY = Math.abs( pitch - roi_pitch[k] );
					double dist = Math.sqrt( distX*distX + distY*distY );
					// computes the nearest visible and not visible tile in order to load first all visible tiles
					if( isROIVisible(k) ) {
						if( dist < minDistVisible ) {
							minDistVisible = dist;
							iVisible = k;
						}
					}
					else {
						if( dist < minDistNotVisible ) {
							minDistNotVisible = dist;
							iNotVisible = k;
						}
					}
				}
			}
			// if there is a visible roi chooses it, else chooses an invisible one
			int i;
			if( iVisible >= 0 ) {
				i = iVisible;
			}
			else {
				i = iNotVisible;
			}

			if( i >= 0 ) {
				loadROI(i);
				nLoaded++;
				// updates the progress bar
				if( showToolbar ) ((toolbar) tlbObj).setBarPerc( nLoaded*100/numroi );
				
//				// repaints only if the loaded ROI is visible
				if( !isROIVisible(i) ) onlyPaintToolbar = true; 

				paintDone = false;
				forceBilIntepolator = true;		// to speed up ROI drawing
				repaint();		// we always call repaint() to draw the toolbar

				// stops execution until paint() is executed
				int counter = 0;	// emergency exit
				while( !paintDone && counter < 100 ) {
					try {
						Thread.sleep(10L);
					} catch (Exception _ex) {}
					counter++;
				}
//				System.out.println( "Loaded ROI " + i + " pan=" + roi_pan[i] + " pano_pan=" + yaw);
			}
		} while( !done );

		// clears the progress bar
		if( showToolbar ) ((toolbar) tlbObj).setBarPerc( 0 );
		dirty = true;
		repaint();

	}
	

	/*
	 * returns true if the roi is currently visible in ptviewer
	 */
	protected boolean isROIVisible( int nRoi ) {
		boolean visible = true;
		// distance in degrees between the center of the current view and the center of the loaded ROI
		double yawDist = Math.abs(yaw - roi_yaw[nRoi]);
		if( yawDist > 180 ) yawDist = 360 - yawDist;
		double pitchDist = Math.abs( pitch - roi_pitch[nRoi] );
		if( yawDist > (hfov + roi_wdeg[nRoi])/2 ) visible = false;
		if( visible ) {
			if( pitchDist > (math_fovy( hfov, vwidth, vheight) + roi_hdeg[nRoi])/2)
				visible = false;
		}
		return visible;
	}

	

	public void loadROI(int i, int j) {
		for (int k = i; k <= j; k++)
			loadROI(k);

	}

	//		public void loadROI(int i)
	//		{
	//				Image image;
	//				Image image1;
	//				if(i >= numroi || roi_loaded[i])
	//						break MISSING_BLOCK_LABEL_180;
	//				image1 = loadImage(roi_im[i]);
	//				image1;
	//				image = image1;
	//				JVM INSTR ifnull 180;
	//					 goto _L1 _L2
	//_L1:
	//				break MISSING_BLOCK_LABEL_32;
	//_L2:
	//				break MISSING_BLOCK_LABEL_180;
	//				ptinsertImage(pdata, roi_xp[i], roi_yp[i], image, (pheight + 99) / 100);
	//				if(hsready)
	//				{
	//						for(int j = 0; j < numhs; j++)
	//								if((hs_imode[j] & 4) > 0)
	//								{
	//										int k = (int)hs_up[j];
	//										int l = (int)hs_vp[j];
	//										int i1 = (int)hs_xp[j] - (k >> 1);
	//										int j1 = (int)hs_yp[j] - (l >> 1);
	//										im_extractRect(pdata, i1, j1, (int[])hs_him[j], k, 0, l, k, l);
	//								}
	//
	//				}
	//				roi_loaded[i] = true;
	//		}

	// copied from version 2.1
	public void loadROI(int i) {
		if (i < numroi && !roi_loaded[i]) {

			loadingROI = true;
		
			Image r = null;
			r = loadImage(roi_im[i]);
			if (r != null) {
				ptinsertImage(
					pdata,
					roi_xp[i],
					roi_yp[i],
					r,
					(pheight + 99) / 100);

				// Update warped hotspots
				if (hsready) {
					int k;
					for (k = 0; k < numhs; k++) {
						if ((hs_imode[k] & IMODE_WARP) > 0) { // warped hotspot
							int w = (int) hs_up[k];
							int h = (int) hs_vp[k];
							int xp = (int) hs_xp[k] - w / 2;
							int yp = (int) hs_yp[k] - h / 2;
							im_extractRect(
								pdata,
								xp,
								yp,
								(int[]) hs_him[k],
								w,
								0,
								h,
								w,
								h);
						}
					}
				}
				roi_loaded[i] = true;
				roi_w[i] = r.getWidth( null );
				roi_wdeg[i] = 1.0*roi_w[i]*360/pwidth;
				roi_hdeg[i] = 1.0*roi_h[i]*360/pwidth;
				r = null;
			}

			loadingROI = false;
		
			// sleeps some time in order to let ptviewer perform a paint()
//			try {
//				Thread.sleep(100L);
//			} catch (Exception _ex) {}
		}
	}

	// computes the yaw angle of the middle of each ROI image
	void computeRoiYaw() {
		for( int k = 0; k < numroi; k++ ) {
			roi_yaw[k] = 360.0*(roi_xp[k] + roi_w[k]/2)/pwidth;
			if( roi_yaw[k] > 360 ) roi_yaw[k] -= 360;
			roi_yaw[k] -= 180;
		}
	}
	
	
	// computes the pitch angle of the middle of each ROI image
	void computeRoiPitch() {
		for( int k = 0; k < numroi; k++ ) {
			// y coord of the middle of the image if the pano had a 2:1 size ratio
			int y = pwidth/4 - pheight/2 + roi_yp[k] + roi_h[k]/2;
			double t = 90.0 - 180.0*y/(pwidth/2);
			roi_pitch[k] = t;
		}
	}
	
	

	String DisplayHSCoordinates(int i, int j) {
		double ad[];
		(ad =
			math_view2pano(
				i,
				j,
				vwidth,
				vheight,
				pwidth,
				pheight - deltaYHorizonPosition,
				yaw,
				pitch,
				hfov))[0] =
			Math.rint((ad[0] * 100000D) / (double) pwidth) / 1000D;
		ad[1] = Math.rint((ad[1] * 100000D) / (double) pheight) / 1000D;
		return "X = " + ad[0] + "; Y = " + ad[1];
	}

	int OverHotspot(int i, int j) {
		if (!hsready || i < 0 || i >= vwidth || j < 0 || j >= vheight)
			return -1;
		
		if( hsEnableVisibleOnly && !showhs )
			return -1;
		
		int k = hs_vdata[j * vwidth + i] & 0xff;
		if (filename != null && filename.toLowerCase().endsWith(".mov"))
			if (k == 0)
				return -1;
			else
				return k - 1;
		if (k != 255 && k < numhs)
			return k;
		if (hs_image != null)
			return -1;
		for (int l = 0; l < numhs; l++)
			if (hs_visible[l]
				&& hs_mask[l] == null
				&& hs_link[l] == -1
				&& hs_up[l] == -200D
				&& hs_vp[l] == -200D
				&& i < hs_xv[l] + 12
				&& i > hs_xv[l] - 12
				&& j < hs_yv[l] + 12
				&& j > hs_yv[l] - 12)
				return l;

		return -1;
	}

	public void waitWhilePanning() {
		while (lastframe > frames)
			try {
				Thread.sleep(200L);
			} catch (Exception _ex) {
				return;
			}
	}

	public void ZoomIn() {
		gotoView(yaw, pitch, hfov / 1.03D);
	}

	public void ZoomOut() {
		gotoView(yaw, pitch, hfov * 1.03D);
	}

	public void panUp() {
		gotoView(yaw, pitch + hfov / pan_steps, hfov);
	}

	public void panDown() {
		gotoView(yaw, pitch - hfov / pan_steps, hfov);
	}

	public void panLeft() {
		gotoView(yaw - hfov / pan_steps, pitch, hfov);
	}

	public void panRight() {
		gotoView(yaw + hfov / pan_steps, pitch, hfov);
	}

	public void showHS() {
		showhs = true;
		if( showToolbar ) ((toolbar) tlbObj).syncHSButton();
		repaint();
	}

	public void hideHS() {
		showhs = false;
		if( showToolbar ) ((toolbar) tlbObj).syncHSButton();
		repaint();
	}

	public void toggleHS() {
		showhs = !showhs;
		
		if( hsEnableVisibleOnly ) {
			// we need to change the mouse cursor if we are over an hotspot
			int i = lastMouseX;
			int j = lastMouseY;
			mouseInViewer = is_inside_viewer(i, j);
			if (mouseInWindow) {
				newx = i;
				newy = j;
			}
			PVSetCursor(i, j);
		}
		
		if( showToolbar ) ((toolbar) tlbObj).toggleHSButton();
		repaint();
	}

	public boolean isVisibleHS() {
		return showhs;
	}

	public double pan() {
		return yaw;
	}

	public double tilt() {
		return pitch;
	}

	public double fov() {
		return hfov;
	}

	public void setQuality(int i) {
		if (i >= 0 && i <= MAX_QUALITY) {
			quality = i;
			dirty = true;
			repaint();
		}
	}
	
	public int getQuality() {
		return quality;
	}
        
 	/**
 	* Moves from a specific position to another position using a specified amount of frames
    * @param p0 Pan angle of starting view
    * @param p1 Pan angle of target view
    * @param t0 Tilt angle of starting view
    * @param t1 Tilt angle of target view
    * @param f0 Field of View angle of starting view
    * @param f1 Field of View of target view
    * @param nframes the number of frames
    */
	public void moveFromTo(
		double p0,
		double p1,
		double t0,
		double t1,
		double f0,
		double f1,
		int nframes) {
			
	//	// to avoid weird behaviour when this function is called while loading ROIs
	//	if( loadingROI && dynLoadROIs ) return; 
			
	//	double d6 = 0.0D;
	//	double d7 = (d3 - d2) / (double) i;
	//	double d8 = Math.pow(d5 / d4, 1.0D / (double) i);
	//	if (Math.abs(d1 - d) < 180D || yaw_max != 180D || yaw_min != -180D)
	//		d6 = (d1 - d) / (double) i;
	//	else if (d1 > d)
	//		d6 = (d1 - d - 360D) / (double) i;
	//	else if (d1 < d)
	//		d6 = ((d1 - d) + 360D) / (double) i;
	//	gotoView(d, d2, d4);
	//	lastframe = frames + (long) i;
	//	startAutoPan(d6, d7, d8);
		moveFromTo(p0, p1, t0, t1, f0, f1, nframes, 0);
	}

	public void moveFromTo(
		double p0,
		double p1,
		double t0,
		double t1,
		double f0,
		double f1,
		int nframes,
		double autoTime) {
			
		// to avoid weird behaviour when this function is called while loading ROIs
		if( loadingROI && dynLoadROIs ) return; 
			
		double dp = 0.0D;
		double dt = (t1 - t0) / (double) nframes;
		double z = Math.pow(f1 / f0, 1.0D / (double) nframes);
		if (Math.abs(p1 - p0) < 180D || yaw_max != 180D || yaw_min != -180D)
			dp = (p1 - p0) / (double) nframes;
		else if (p1 > p0)
			dp = (p1 - p0 - 360D) / (double) nframes;
		else if (p1 < p0)
			dp = ((p1 - p0) + 360D) / (double) nframes;
		gotoView(p0, t0, f0);
		lastframe = frames + (long) nframes;
		startAutoPan(dp, dt, z, autoTime);
	}

        
	public void moveTo(double pan, double tilt, double fov, int nframes) {
		moveFromTo(yaw, pan, pitch, tilt, hfov, fov, nframes, 0);
	}

	public void moveTo(double pan, double tilt, double fov, int nframes, double autoTime) {
		moveFromTo(yaw, pan, pitch, tilt, hfov, fov, nframes, autoTime);
	}

	/**
 	* Starts autopanning.
    * @param p Pan angle increment per frame
    * @param t Tilt angle increment per frame
    * @param z Field of View angle factor per frame
    */
	public void startAutoPan(double p, double t, double z) {
//		autopan = d;
//		autotilt = d1;
//		zoom = d2;
//		if (lastframe <= frames)
//			lastframe = frames + 0x5f5e100L;
//		repaint();
		startAutoPan( p, t, z, 0 );
	}

	public void startAutoPan(double p, double t, double z, double autoTime) {
		autopan = p;
		autotilt = t;
		zoom = z;
		if( autoTime != 0 )
			autopanFrameTime = ComputeAutoTimeFrame( autopan, autoTime );
		if (lastframe <= frames)
			lastframe = frames + 0x5f5e100L;
		repaint();
	}

	public void stopAutoPan() {
		lastframe = 0L;
		autopan = 0.0D;
		autopanFrameTime = 0;
		autotilt = 0.0D;
		zoom = 1.0D;
	}

	void stopPan() {
		panning = false;
		oldspeedx = 0.0D;
		oldspeedy = 0.0D;
	}

	public boolean getAutoPan() {
		return lastframe > frames;
	}

	public String getFilename() {
		return filename;
	}

	public boolean getPanoIsLoaded() {
		return PanoIsLoaded;
	}

	public void gotoView(double pan, double tilt, double fov) {

		// to avoid weird behaviour when this function is called while loading ROIs
//		if( loadingROI && dynLoadROIs ) return; 

		// reduces fov if it is too large for the vertical extension of this pano
		while (math_fovy(fov, vwidth, vheight) > pitch_max - pitch_min) {
			fov /= 1.03;
		}

		label0 : {
			if (pan == yaw && tilt == pitch && fov == hfov)
				return;
			for (; pan > 180D; pan -= 360D);
			for (; pan < -180D; pan += 360D);
			double f = math_fovy(fov, vwidth, vheight) / 2D;
			if (tilt > pitch_max - f && pitch_max != 90D)
				tilt = pitch_max - f;
			else if (tilt > pitch_max)
				tilt = pitch_max;
			else if (tilt < pitch_min + f && pitch_min != -90D)
				tilt = pitch_min + f;
			else if (tilt < pitch_min)
				tilt = pitch_min;
			if (yaw_max != 180D || yaw_min != -180D) {
				// check left edge
				double xl =
					math_view2pano(
						0,
						pitch <= 0.0D ? vheight - 1 : 0,
						vwidth,
						vheight,
						pwidth,
						pheight,
						pan,
						tilt,
						fov)[0];
				double xr =
					math_view2pano(
						vwidth - 1,
						pitch <= 0.0D ? vheight - 1 : 0,
						vwidth,
						vheight,
						pwidth,
						pheight,
						pan,
						tilt,
						fov)[0];
				if (math_view2pano(vwidth - 1,
					pitch <= 0.0D ? vheight - 1 : 0,
					vwidth,
					vheight,
					pwidth,
					pheight,
					pan,
					tilt,
					fov)[0]
					- xl
					> ((yaw_max - yaw_min) / 360D) * (double) pwidth)
					break label0;
				if (xl < ((yaw_min + 180D) / 360D) * (double) pwidth) {
					if (lastframe > frames)
						autopan *= -1D;
					pan += yaw_min - ((xl / (double) pwidth) * 360D - 180D);
				}
				if (xr > ((yaw_max + 180D) / 360D) * (double) pwidth) {
					if (lastframe > frames)
						autopan *= -1D;
					pan -= (xr / (double) pwidth) * 360D - 180D - yaw_max;
				}
			}
			if (2D * f <= pitch_max - pitch_min
				&& fov <= hfov_max
				&& fov >= hfov_min
				&& fov <= yaw_max - yaw_min
				&& tilt <= pitch_max
				&& tilt >= pitch_min
				&& pan <= yaw_max
				&& pan >= yaw_min
				&& (pan != yaw || tilt != pitch || fov != hfov)) {
				yaw = pan;
				pitch = tilt;
				hfov = fov;
				dirty = true;
				repaint();
				return;
			}
			// If we reach this point, then there is no change
			// We have probably reached the end of an autopan
			stopAutoPan();
		}
	}

	public void gotoHS(int i) {
		if (i < 0 || i >= numhs) {
			return;
		} else {
			JumpToLink(hs_url[i], hs_target[i]);
			return;
		}
	}

	void gotoSHS(int i) {
		if (i < 0 || i >= numshs) {
			return;
		} else {
			JumpToLink(shs_url[i], shs_target[i]);
			return;
		}
	}

	void JumpToLink(String s, String s1) {
		if (s != null) {
			if (s.startsWith("ptviewer:")) {
				executePTViewerCommand(s.substring(s.indexOf(':') + 1));
				return;
			}
			if (s.startsWith("javascript:")) {
				executeJavascriptCommand(s.substring(s.indexOf(':') + 1));
				return;
			}
			URL url;
			try {
				url = new URL(getDocumentBase(), s);
			} catch (MalformedURLException _ex) {
				System.err.println("URL " + s + " ill-formed");
				return;
			}
			if (s1 == null) {
				getAppletContext().showDocument(url);
				return;
			}
			getAppletContext().showDocument(url, s1);
		}
	}

	public synchronized void newPanoFromList(
		int i,
		double d,
		double d1,
		double d2) {
		loadPanoFromList(i);
		yaw = d;
		pitch = d1;
		hfov = d2;
		repaint();
		start();
	}

	public synchronized void newPanoFromList(int i) {
		loadPanoFromList(i);
		repaint();
		start();
	}

	void loadPanoFromList(int i) {
		String s;
		if ((s = myGetParameter(null, "pano" + i)) != null) {
			stop();
			PV_reset();
			initialize();
			CurrentPano = i;
			if (PTViewer_Properties != null)
				ReadParameters(PTViewer_Properties);
			ReadParameters(s);
		}
	}

	public void newPano(String s) {
		stop();
		PV_reset();
		initialize();
		if (PTViewer_Properties != null)
			ReadParameters(PTViewer_Properties);
		ReadParameters(s);
//		if( dynLoadROIs ) computeRoiPan();
		repaint();
		start();
	}
	
//ƒpƒmƒ‰ƒ}‰æ‘œ‚ÌŒÂ•ÊŽw’è
	public void newPano(
	String s,
	double d,
	double d1,
	double d2) {
		stop();
		PV_reset();
		initialize();
		if (PTViewer_Properties != null)
			ReadParameters(PTViewer_Properties);
		ReadParameters(s);
//		if( dynLoadROIs ) computeRoiPan();
		yaw = d;
		pitch = d1;
		hfov = d2;
		repaint();
		start();
	}

	public void SetURL(String s) {
		newPano("{file=" + s + "}");
	}
	
	/*
	 * computes the value for	autopanFrameTime from the auto and autoTime parameters
	 */
	private double ComputeAutoTimeFrame( double auto, double autoTime ) {
		
		if( auto == 0 ) return 0;
		
		double nFrames = 360.0 / auto;
		double retVal = autoTime/nFrames*1000;
		return Math.abs(retVal);
	}

	void ReadParameters(String s) {
		String s1;
		if ((s1 = myGetParameter(s, "bgcolor")) != null)
			bgcolor = new Color(Integer.parseInt(s1, 16));
		if ((s1 = myGetParameter(s, "barcolor")) != null)
			pb_color = new Color(Integer.parseInt(s1, 16));
		if ((s1 = myGetParameter(s, "bar_x")) != null)
			pb_x = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "bar_y")) != null)
			pb_y = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "bar_width")) != null)
			pb_width = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "bar_height")) != null)
			pb_height = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "maxarray")) != null)
			im_maxarray = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "view_width")) != null) {
			vwidth = Integer.parseInt(s1);
			vset = true;
		}
		if ((s1 = myGetParameter(s, "view_height")) != null) {
			vheight = Integer.parseInt(s1);
			org_vheight = vheight;	// needed because if we use the toolbar we will change vheight each time that a pano is loaded
			vset = true;
		}
		else {
			if( org_vheight != 0 )
				vheight = org_vheight;
		}
		if ((s1 = myGetParameter(s, "view_x")) != null)
			vx = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "view_y")) != null)
			vy = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "preload")) != null)
			preload = s1;
		if ((s1 = myGetParameter(s, "cache")) != null
			&& s1.equalsIgnoreCase("false"))
			file_cachefiles = false;
		if ((s1 = myGetParameter(s, "cursor")) != null)
			if (s1.equalsIgnoreCase("CROSSHAIR"))
				ptcursor = 1;
			else if (s1.equalsIgnoreCase("MOVE"))
				ptcursor = 13;
		if ((s1 = myGetParameter(s, "grid_bgcolor")) != null)
			grid_bgcolor = Integer.parseInt(s1, 16);
		if ((s1 = myGetParameter(s, "grid_fgcolor")) != null)
			grid_fgcolor = Integer.parseInt(s1, 16);
		if ((s1 = myGetParameter(s, "mass")) != null)
			MASS = Double.valueOf(s1).doubleValue();
		if (myGetParameter(s, "antialias") != null)
			antialias = true;
		if ((s1 = myGetParameter(s, "quality")) != null) {
			quality = Integer.parseInt(s1);
			if (quality < 0)
				quality = 0;
			if (quality > MAX_QUALITY)
				quality = MAX_QUALITY;
		}
		if ((s1 = myGetParameter(s, "inits")) != null)
			inits = s1;
		double d;
//		if ((s1 = myGetParameter(s, "tiltmin")) != null
//			&& (d = Double.valueOf(s1).doubleValue()) > -90D
//			&& d < 0.0D)
//			pitch_min = d;
		// modified to allow positive values for tiltmin
		if ((s1 = myGetParameter(s, "tiltmin")) != null
			&& (d = Double.valueOf(s1).doubleValue()) > -90D )
			pitch_min = d;
//		if ((s1 = myGetParameter(s, "tiltmax")) != null
//			&& (d = Double.valueOf(s1).doubleValue()) < 90D
//			&& d > 0.0D)
//			pitch_max = d;
		// modified to allow negative values for tiltmax (hard to believe, but somebody needed it!)
		if ((s1 = myGetParameter(s, "tiltmax")) != null
			&& (d = Double.valueOf(s1).doubleValue()) < 90D )
			pitch_max = d;
		if ((s1 = myGetParameter(s, "tilt")) != null
			&& (d = Double.valueOf(s1).doubleValue()) >= pitch_min
			&& d <= pitch_max)
			pitch = d;
		if ((s1 = myGetParameter(s, "tilt")) != null
			&& (d = Double.valueOf(s1).doubleValue()) >= pitch_min
			&& d <= pitch_max)
			pitch = d;
		if ((s1 = myGetParameter(s, "panmax")) != null)
			yaw_max = Double.valueOf(s1).doubleValue();
		if ((s1 = myGetParameter(s, "panmin")) != null)
			yaw_min = Double.valueOf(s1).doubleValue();
		if ((s1 = myGetParameter(s, "pan")) != null
			&& (d = Double.valueOf(s1).doubleValue()) >= yaw_min
			&& d <= yaw_max)
			yaw = d;
		if ((s1 = myGetParameter(s, "fovmax")) != null
			&& (d = Double.valueOf(s1).doubleValue()) <= 165D)
			hfov_max = d <= yaw_max - yaw_min ? d : yaw_max - yaw_min;
		if ((s1 = myGetParameter(s, "fovmin")) != null)
			hfov_min = Double.valueOf(s1).doubleValue();
		if ((s1 = myGetParameter(s, "fov")) != null
			&& (d = Double.valueOf(s1).doubleValue()) <= hfov_max
			&& d >= hfov_min)
			hfov = d;

		// must be before the "wait" parameter: the update() function must find an existing toolbar
		if ((s1 = myGetParameter(s, "showToolbar")) != null
			&& s1.equalsIgnoreCase("true"))
			showToolbar = true;
		if ((s1 = myGetParameter(s, "toolbarImage")) != null) {
			tlbImageName = s1;
		}
		if( showToolbar && tlbObj == null ) tlbObj = new toolbar( this, tlbImageName );

		if( showToolbar ) {
			if ((s1 = myGetParameter(s, "toolbarDescr_x")) != null)
				((toolbar)tlbObj).setToolbarDescrX( Integer.parseInt(s1) );
			if ((s1 = myGetParameter(s, "toolbarDescr_color")) != null)
				((toolbar)tlbObj).SetTextColor( s1 );
			if ((s1 = myGetParameter(s, "toolbarBoldText")) != null
					&& s1.equalsIgnoreCase("true"))
				((toolbar)tlbObj).setMsgBold( true );
		}
		
		if ((s1 = myGetParameter(s, "mouseSensitivity")) != null)
			mouseSensitivity = Double.valueOf(s1).doubleValue();

		if ((s1 = myGetParameter(s, "mouseQ6Threshold")) != null)
			mouseQ6Threshold = Double.valueOf(s1).doubleValue();

		if ((s1 = myGetParameter(s, "mousePanTime")) != null)
			mousePanTime = Double.valueOf(s1).doubleValue();

		if ((s1 = myGetParameter(s, "wait")) != null) {
			dwait = null;
			dwait = loadImage(s1);
			update(getGraphics());
		}
		if ((s1 = myGetParameter(s, "auto")) != null)
			autopan = Double.valueOf(s1).doubleValue();
			
		if( autopan != 0 ) {
			if ((s1 = myGetParameter(s, "autoTime")) != null) {
				double autoTime = Double.valueOf(s1).doubleValue();
				autopanFrameTime = ComputeAutoTimeFrame( autopan, autoTime );
			}
			if ((s1 = myGetParameter(s, "autoNumTurns")) != null) {
				autoNumTurns = Double.valueOf(s1).doubleValue();
			}
		}
			
		if ((s1 = myGetParameter(s, "mousehs")) != null)
			MouseOverHS = s1;
		if ((s1 = myGetParameter(s, "getview")) != null)
			GetView = s1;
		if ((s1 = myGetParameter(s, "frame")) != null) {
			frame = null;
			frame = loadImage(s1);
		}
		if ((s1 = myGetParameter(s, "waittime")) != null)
			waittime = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "hsimage")) != null)
			hs_image = s1;
		if ((s1 = myGetParameter(s, "pwidth")) != null)
			pwidth = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "pheight")) != null)
			pheight = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "loadAllRoi")) != null
			&& s1.equalsIgnoreCase("false"))
			loadAllRoi = false;
		if ((s1 = myGetParameter(s, "file")) != null)
			filename = s1;
		if ((s1 = myGetParameter(s, "order")) != null)
			order = s1;
		if ((s1 = myGetParameter(s, "oversampling")) != null)
			max_oversampling = Double.valueOf(s1).doubleValue();
		for (int i = 0; i <= hotspots.size(); i++) {
			String s2;
			if ((s2 = myGetParameter(s, "hotspot" + i)) != null) {
				if (i < hotspots.size())
					hotspots.setSize(i);
				hotspots.addElement(s2);
			}
		}

		numroi = 0;
		int j1;
		for (j1 = 0; myGetParameter(s, "roi" + j1) != null; j1++);
		if (j1 > 0) {
			roi_allocate(j1);
			for (int j = 0; j < numroi; j++) {
				String s3;
				if ((s3 = myGetParameter(s, "roi" + j)) != null)
					ParseROILine(s3, j);
			}

		}
		for (int k = 0; k <= shotspots.size(); k++) {
			String s4;
			if ((s4 = myGetParameter(s, "shotspot" + k)) != null) {
				if (k < shotspots.size())
					shotspots.setSize(k);
				shotspots.addElement(s4);
			}
		}

		for (int l = 0; l <= sounds.size(); l++) {
			String s5;
			if ((s5 = myGetParameter(s, "sound" + l)) != null) {
				if (l < sounds.size())
					sounds.setSize(l);
				sounds.addElement(s5);
			}
		}

		for (int i1 = 0; i1 <= app_properties.size(); i1++) {
			String s6;
			if ((s6 = myGetParameter(s, "applet" + i1)) != null) {
				if (i1 < app_properties.size()) {
					stopApplets(i1);
					app_properties.setSize(i1);
				}
				app_properties.addElement(s6);
			}
		}

		if ((s1 = myGetParameter(s, "dynLoadROIs")) != null
			&& s1.equalsIgnoreCase("true"))
			dynLoadROIs = true;

		if ((s1 = myGetParameter(s, "hsShowDescrInStatusBar")) != null
				&& s1.equalsIgnoreCase("false"))
				hsShowDescrInStatusBar = false;

		if ((s1 = myGetParameter(s, "hsEnableVisibleOnly")) != null
			&& s1.equalsIgnoreCase("true"))
			hsEnableVisibleOnly = true;

		if ((s1 = myGetParameter(s, "shsEnableVisibleOnly")) != null
				&& s1.equalsIgnoreCase("true"))
				shsEnableVisibleOnly = true;
				
		if ((s1 = myGetParameter(s, "shsStopAutoPanOnClick")) != null
				&& s1.equalsIgnoreCase("false"))
				shsStopAutoPanOnClick = false;

		if ((s1 = myGetParameter(s, "popup_panning")) != null
				&& s1.equalsIgnoreCase("true"))
				popupPanning = true;

		if ((s1 = myGetParameter(s, "imgLoadFeedback")) != null
				&& s1.equalsIgnoreCase("false"))
				imgLoadFeedback = false;
				
		if ((s1 = myGetParameter(s, "outOfMemoryURL")) != null) {
			outOfMemoryURL = s1;
		}

		if ((s1 = myGetParameter(s, "authoringMode")) != null
				&& s1.equalsIgnoreCase("true"))
			authoringMode = true;

		if ((s1 = myGetParameter(s, "horizonposition")) != null)
			horizonPosition = Integer.parseInt(s1);

		if ((s1 = myGetParameter(s, "statusMessage")) != null) {
			statusMessage = s1;
		}
	}


	void executeJavascriptCommand(String s) {
		if (s != null)
			try {
				Class class1;
				Object obj =
					(class1 = Class.forName("netscape.javascript.JSObject"))
						.getMethod(
							"getWindow",
							new Class[] { java.applet.Applet.class })
						.invoke(class1, new Object[] { this });
				class1.getMethod(
					"eval",
					new Class[] { java.lang.String.class }).invoke(
					obj,
					new Object[] { s });
				return;
			} catch (Exception _ex) {
			}
	}

	void executePTViewerCommand(String s) {
		stopThread(ptviewerScript);
		ptviewerScript = new Thread(this);
		PTScript = s;
		ptviewerScript.start();
	}

	void PTViewerScript(String s) {
		int i;
		String s2;
		if ((i = getNumArgs(s, ';')) > 0) {
			for (int j = 0; j < i; j++) {
				String s1;
				s2 = stripWhiteSpace(getArg(j, s, ';'));
				s1 = s2;
				if( s2 != null ) {
					if (s2.equals("loop()"))
						j = -1;
					else
						PTViewerCommand(s1);
				}
			}

		}
	}

	void PTViewerCommand(String s) {
		String parsedNumberRange;
		int argCount;
		String s1 = s.substring(s.indexOf('(') + 1, s.indexOf(')'));
		if (s.startsWith("ZoomIn")) {
			ZoomIn();
			return;
		}
		if (s.startsWith("ZoomOut")) {
			ZoomOut();
			return;
		}
		if (s.startsWith("panUp")) {
			panUp();
			return;
		}
		if (s.startsWith("panDown")) {
			panDown();
			return;
		}
		if (s.startsWith("panLeft")) {
			panLeft();
			return;
		}
		if (s.startsWith("panRight")) {
			panRight();
			return;
		}
		if (s.startsWith("showHS")) {
			showHS();
			return;
		}
		if (s.startsWith("hideHS")) {
			hideHS();
			return;
		}
		if (s.startsWith("toggleHS")) {
			toggleHS();
			return;
		}
		if (s.startsWith("gotoView")) {
			if (getNumArgs(s1) == 3) {
				gotoView(
					Double.valueOf(getArg(0, s1)).doubleValue(),
					Double.valueOf(getArg(1, s1)).doubleValue(),
					Double.valueOf(getArg(2, s1)).doubleValue());
				return;
			}
		} else if (s.startsWith("startAutoPan")) {
			if (getNumArgs(s1) == 3) {
				startAutoPan(
					Double.valueOf(getArg(0, s1)).doubleValue(),
					Double.valueOf(getArg(1, s1)).doubleValue(),
					Double.valueOf(getArg(2, s1)).doubleValue());
				return;
			}
			if (getNumArgs(s1) == 4) {
				startAutoPan(
					Double.valueOf(getArg(0, s1)).doubleValue(),
					Double.valueOf(getArg(1, s1)).doubleValue(),
					Double.valueOf(getArg(2, s1)).doubleValue(),
					Double.valueOf(getArg(3, s1)).doubleValue());
				return;
			}
		} else {
			if (s.startsWith("stopAutoPan")) {
				stopAutoPan();
				return;
			}
			if (s.startsWith("newPanoFromList")) {
				if (getNumArgs(s1) == 1) {
					newPanoFromList(Integer.parseInt(s1));
					return;
				}
				if (getNumArgs(s1) == 4) {
					newPanoFromList(
						Integer.parseInt(getArg(0, s1)),
						Double.valueOf(getArg(1, s1)).doubleValue(),
						Double.valueOf(getArg(2, s1)).doubleValue(),
						Double.valueOf(getArg(3, s1)).doubleValue());
					return;
				}
			} else {
				if (s.startsWith("newPano")) {
					newPano(s1);
					return;
				}
				if (s.startsWith("SetURL")) {
					SetURL(s1);
					return;
				}
				if (s.startsWith("PlaySound")) {
					PlaySound(Integer.parseInt(s1));
					return;
				}
				if (s.startsWith("moveFromTo")) {
					if (getNumArgs(s1) == 7) {
						moveFromTo(
							Double.valueOf(getArg(0, s1)).doubleValue(),
							Double.valueOf(getArg(1, s1)).doubleValue(),
							Double.valueOf(getArg(2, s1)).doubleValue(),
							Double.valueOf(getArg(3, s1)).doubleValue(),
							Double.valueOf(getArg(4, s1)).doubleValue(),
							Double.valueOf(getArg(5, s1)).doubleValue(),
							Integer.valueOf(getArg(6, s1)).intValue(),
							0D);
						return;
					}else if(getNumArgs(s1) == 8){
						moveFromTo(
							Double.valueOf(getArg(0, s1)).doubleValue(),
							Double.valueOf(getArg(1, s1)).doubleValue(),
							Double.valueOf(getArg(2, s1)).doubleValue(),
							Double.valueOf(getArg(3, s1)).doubleValue(),
							Double.valueOf(getArg(4, s1)).doubleValue(),
							Double.valueOf(getArg(5, s1)).doubleValue(),
							Integer.valueOf(getArg(6, s1)).intValue(),
							Double.valueOf(getArg(7, s1)).doubleValue());
						return;
					}
				} else if (s.startsWith("moveTo")) {
					if (getNumArgs(s1) == 4) {
						moveTo(
							Double.valueOf(getArg(0, s1)).doubleValue(),
							Double.valueOf(getArg(1, s1)).doubleValue(),
							Double.valueOf(getArg(2, s1)).doubleValue(),
							Integer.valueOf(getArg(3, s1)).intValue(),
							0D);
						return;
					} else if (getNumArgs(s1) == 5) {
						moveTo(
							Double.valueOf(getArg(0, s1)).doubleValue(),
							Double.valueOf(getArg(1, s1)).doubleValue(),
							Double.valueOf(getArg(2, s1)).doubleValue(),
							Integer.valueOf(getArg(3, s1)).intValue(),
							Double.valueOf(getArg(4, s1)).doubleValue());
						return;
					}
				} else {
					if (s.startsWith("DrawSHSImage")) {
						parsedNumberRange = parseNumberRange(s1);
						if ((argCount = getNumArgs(parsedNumberRange)) > 0) {
							for (int argLoop = 0; argLoop < argCount; argLoop++) {
								DrawSHSImage(Integer.parseInt(stripWhiteSpace(getArg(argLoop, parsedNumberRange))));
							}
						}
						return;
					}
					if (s.startsWith("DrawSHSPopup")) {
						parsedNumberRange = parseNumberRange(s1);
						if ((argCount = getNumArgs(parsedNumberRange)) > 0) {
							for (int argLoop = 0; argLoop < argCount; argLoop++) {
								DrawSHSPopup(Integer.parseInt(stripWhiteSpace(getArg(argLoop, parsedNumberRange))));
							}
						}
						return;
					}
					if (s.startsWith("HideSHSImage")) {
						parsedNumberRange = parseNumberRange(s1);
						if ((argCount = getNumArgs(parsedNumberRange)) > 0) {
							for (int argLoop = 0; argLoop < argCount; argLoop++) {
								HideSHSImage(Integer.parseInt(stripWhiteSpace(getArg(argLoop, parsedNumberRange))));
							}
						}
						return;
					}
					if (s.startsWith("DrawHSImage")) {
						DrawHSImage(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("HideHSImage")) {
						HideHSImage(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("ToggleHSImage")) {
						ToggleHSImage(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("ToggleSHSImage")) {
						ToggleSHSImage(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("waitWhilePanning")) {
						waitWhilePanning();
						return;
					}
					if (s.startsWith("startApplet")) {
						startApplet(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("stopApplet")) {
						stopApplet(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("loadROI"))
						if (getNumArgs(s1) == 2) {
							loadROI(
								Integer.valueOf(getArg(0, s1)).intValue(),
								Integer.valueOf(getArg(1, s1)).intValue());
							return;
						} else {
							loadROI(Integer.parseInt(s1));
							return;
						}
					if (s.startsWith("setQuality"))
						setQuality(Integer.parseInt(s1));
				}
			}
		}
	}

	public synchronized void DrawSHSImage(int i) {
		if (i >= 0 && i < numshs && shs_imode[i] != 2) {
			shs_imode[i] = 2;
			repaint();
		}
	}

	public synchronized void DrawSHSPopup(int i) {
		if (i >= 0 && i < numshs && shs_imode[i] != 1) {
			shs_imode[i] = 1;
			repaint();
		}
	}

	public synchronized void HideSHSImage(int i) {
		if (i >= 0 && i < numshs && shs_imode[i] != 0) {
			shs_imode[i] = 0;
			repaint();
		}
	}

	public synchronized void ToggleSHSImage(int i) {
		if (i >= 0 && i < numshs) {
			if (shs_imode[i] != 0) {
				HideSHSImage(i);
				return;
			}
			if (shs_imode[i] != 2)
				DrawSHSImage(i);
		}
	}

	public synchronized void DrawHSImage(int i) {
		if (i >= 0 && i < numhs && (hs_imode[i] & 2) == 0) {
			hs_imode[i] |= 2;
			repaint();
		}
	}

	public synchronized void HideHSImage(int i) {
		if (i >= 0 && i < numhs && (hs_imode[i] & 2) != 0) {
			hs_imode[i] &= -3;
			repaint();
		}
	}

	public synchronized void ToggleHSImage(int i) {
		if (i >= 0 && i < numhs) {
			if ((hs_imode[i] & 2) != 0) {
				HideHSImage(i);
				return;
			}
			if ((hs_imode[i] & 2) == 0)
				DrawHSImage(i);
		}
	}

	public double get_x() {
		double d = -1D;
		if (click_x >= 0 && click_y >= 0)
			d =
				((double) math_int_view2pano(click_x - vx,
					click_y - vy,
					vwidth,
					vheight,
					pwidth,
					pheight,
					yaw,
					pitch,
					hfov)[0]
					* 100D)
					/ (double) pwidth;
		return d;
	}

	public double get_y() {
		double d = -1D;
		if (click_x >= 0 && click_y >= 0)
			d =
				((double) math_int_view2pano(click_x - vx,
					click_y - vy,
					vwidth,
					vheight,
					pwidth,
					pheight,
					yaw,
					pitch,
					hfov)[1]
					* 100D)
					/ (double) pheight;
		click_x = -1;
		click_y = -1;
		return d;
	}

	public int getPanoNumber() {
		return CurrentPano;
	}

	public void startCommunicating(Applet applet) {
		synchronized (sender) {
			if (applet != null)
				sender.put(applet, applet);
			else
				sender.clear();
		}
		dirty = true;
		repaint();
	}

	public void stopCommunicating(Applet applet) {
		if (applet != null) {
			synchronized (sender) {
				sender.remove(applet);
			}
			dirty = true;
			repaint();
		}
	}

	public String parseNumberRange(String numberRange) {
		int argCount;
		int subArgCount;
		String singleNum;
		String fromNum;
		String toNum;
		String returnString;

		returnString = "";
		if ((argCount = getNumArgs(numberRange)) > 0) {
			for (int argLoop = 0; argLoop < argCount; argLoop++) {
				singleNum = stripWhiteSpace(getArg(argLoop, numberRange));
				subArgCount = getNumArgs(singleNum, '-');
				if (subArgCount == 1) {
					returnString = addAnotherArg(returnString, singleNum);
				} else if (subArgCount == 2) {
					fromNum = stripWhiteSpace(getArg(0, singleNum, '-'));
					toNum = stripWhiteSpace(getArg(1, singleNum, '-'));
					for (int subArgLoop = Integer.parseInt(fromNum); subArgLoop <= Integer.parseInt(toNum); subArgLoop++) {
						returnString = addAnotherArg(returnString, String.valueOf(subArgLoop));
					}
				}
			}
		}
		return returnString;
	}

	public String addAnotherArg(String currentArgs, String newArg) {
		if (currentArgs == "") {
			currentArgs = newArg;
		} else {
			currentArgs = currentArgs + "," + newArg;
		}
		return currentArgs;
	}

	private String m1() {
		String s;
		int i;
		if ((i = (s = getDocumentBase().getFile()).indexOf(':')) != -1
			&& i + 1 < s.length())
			return s.substring(i + 1);
		if ((i = s.indexOf('|')) != -1 && i + 1 < s.length())
			return s.substring(i + 1);
		else
			return s;
	}

	void stopThread(Thread thread) {
		if (thread != null && thread.isAlive())
			try {
				thread.checkAccess();
				thread.stop();
				return;
			} catch (SecurityException _ex) {
				thread.destroy();
			}
	}

	void ptinsertImage(int pd[][], int xi, int yi, Image im, int ntiles) {
		if (im != null) {
			new ImageTo2DIntArrayExtractor(pd, xi, yi, im).doit();
			dirty = true;
		}
	}
	
//	void ptinsertImage(int pd[][], int xi, int yi, Image im, int ntiles) {
//		if (im != null) {
//			int w = im.getWidth(null);
//			int h = im.getHeight(null);
//			if (ntiles > h)
//				ntiles = h;
//			int ht = ((h + ntiles) - 1) / ntiles;
//			int idata[] = new int[w * ht];
//			for (int i = 0; i < ntiles; i++) {
//				int sheight = ht + i * ht <= h ? ht : h - i * ht;
//				PixelGrabber pixelgrabber =
//					new PixelGrabber(im, 0, i * ht, w, sheight, idata, 0, w);
//				try {
//					pixelgrabber.grabPixels();
//				} catch (InterruptedException _ex) {
//					return;
//				}
//				im_insertRect(pd, xi, yi + i * ht, idata, w, 0, 0, w, sheight);
//				dirty = true;
//				Thread.yield();	// to allow panning
//			}
//
//		}
//	}

	boolean is_inside_viewer(int i, int j) {
		return i >= vx && j >= vy && i < vx + vwidth && j < vy + vheight;
	}

	int[] get_cube_order(int i, int j) {
		int ai[];
		(ai = new int[6])[0] = 0;
		ai[1] = 1;
		ai[2] = 2;
		ai[3] = 3;
		ai[4] = 4;
		ai[5] = 5;
		if (j > 45) {
			ai[0] = 4;
			switch (i / 45) {
				case 0 : // '\0'
					ai[1] = 2;
					ai[2] = 3;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = 5;
					break;

				case -1 :
					ai[1] = 2;
					ai[2] = 1;
					ai[3] = 3;
					ai[4] = 0;
					ai[5] = 5;
					break;

				case 1 : // '\001'
					ai[1] = 3;
					ai[2] = 2;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = 5;
					break;

				case 2 : // '\002'
					ai[1] = 3;
					ai[2] = 0;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = 5;
					break;

				case 3 : // '\003'
					ai[1] = 0;
					ai[2] = 3;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = 5;
					break;

				case -2 :
					ai[1] = 1;
					ai[2] = 0;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 5;
					break;

				case -3 :
					ai[1] = 1;
					ai[2] = 0;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 5;
					break;

				default :
					ai[1] = 0;
					ai[2] = 1;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 5;
					break;
			}
		} else if (j < -45) {
			ai[0] = 5;
			switch (i / 45) {
				case 0 : // '\0'
					ai[1] = 2;
					ai[2] = 3;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = 4;
					break;

				case -1 :
					ai[1] = 2;
					ai[2] = 1;
					ai[3] = 3;
					ai[4] = 0;
					ai[5] = 4;
					break;

				case 1 : // '\001'
					ai[1] = 3;
					ai[2] = 2;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = 4;
					break;

				case 2 : // '\002'
					ai[1] = 3;
					ai[2] = 0;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = 4;
					break;

				case 3 : // '\003'
					ai[1] = 0;
					ai[2] = 3;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = 4;
					break;

				case -2 :
					ai[1] = 1;
					ai[2] = 0;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 4;
					break;

				case -3 :
					ai[1] = 1;
					ai[2] = 0;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 4;
					break;

				default :
					ai[1] = 0;
					ai[2] = 1;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 4;
					break;
			}
		} else {
			switch (i / 45) {
				case 0 : // '\0'
					ai[0] = 2;
					ai[1] = 3;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case -1 :
					ai[0] = 2;
					ai[1] = 1;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 3;
					ai[4] = 0;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case 1 : // '\001'
					ai[0] = 3;
					ai[1] = 2;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case 2 : // '\002'
					ai[0] = 3;
					ai[1] = 0;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case 3 : // '\003'
					ai[0] = 0;
					ai[1] = 3;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case -2 :
					ai[0] = 1;
					ai[1] = 0;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case -3 :
					ai[0] = 1;
					ai[1] = 0;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				default :
					ai[0] = 0;
					ai[1] = 1;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;
			}
		}
		return ai;
	}

	public Image loadImage(String s) {
		Image image;
		byte readBuffer[];

		// first try to load the image from the jar file
		if ((image = readImageFromJAR(s)) != null ) {
			return image;
		}
		
		if ((readBuffer = file_read(s, null)) != null ) {
			if((image = bufferToImage(readBuffer)) != null)
				return image;
		}
		try {
			URL url = new URL(getDocumentBase(), s);
			Image image1 = getImage(url);
			MediaTracker mediatracker;
			(mediatracker = new MediaTracker(this)).addImage(image1, 0);
			mediatracker.waitForAll();
			if (image1 == null || image1.getWidth(null) <= 0)
				return null;
			else
				return image1;
		} catch (Exception _ex) {
			return null;
		}
	}

	Image loadImageProgress(String s) {
		percent[0] = 0;
		byte abyte0[];
		if ((abyte0 = file_read(s, percent)) != null) {
			Image image = bufferToImage(abyte0);
			percent[0] = 100;
			repaint();
			if (image != null)
				return image;
		}
		return loadImage(s);
	}

	Image bufferToImage(byte abyte0[]) {
		if (abyte0 == null)
			return null;
		Image image = Toolkit.getDefaultToolkit().createImage(abyte0);
		MediaTracker mediatracker;
		(mediatracker = new MediaTracker(this)).addImage(image, 0);
		try {
			mediatracker.waitForAll();
		} catch (InterruptedException _ex) {
			return null;
		}
		return image;
	}

	int[][] im_allocate_pano(int ai[][], int i, int j) {
		if (ai == null || ai.length != j || ai[0].length != i)
			try {
				return new int[j][i];
			} catch (Exception _ex) {
				return null;
			} else
			return ai;
	}

	void im_drawGrid(int ai[][], int i, int j) {
		int k3 = i | 0xff000000;
		int l3 = j | 0xff000000;
		if (ai != null) {
			int i4 = ai.length;
			int j4 = ai[0].length;
			for (int j1 = 0; j1 < i4; j1++) {
				for (int k = 0; k < j4; k++)
					ai[j1][k] = k3;

			}

			int k1 = 0;
			for (int k2 = (36 * i4) / j4; k2 >= 0; k2--) {
				int i3 = k1 + 1;
				for (int l = 0; l < j4; l++) {
					ai[k1][l] = l3;
					ai[i3][l] = l3;
				}

				if (k2 != 0)
					k1 += (i4 - 2 - k1) / k2;
			}

			int i1 = 0;
			for (int l2 = 36; l2 >= 0; l2--) {
				if (i1 == 0) {
					for (int l1 = 0; l1 < i4; l1++)
						ai[l1][i1] = l3;

				} else if (i1 >= j4 - 1) {
					i1 = j4 - 1;
					l2 = 0;
					for (int i2 = 0; i2 < i4; i2++)
						ai[i2][i1] = l3;

				} else {
					int j3 = i1 + 1;
					for (int j2 = 0; j2 < i4; j2++) {
						ai[j2][i1] = l3;
						ai[j2][j3] = l3;
					}

				}
				if (l2 != 0)
					i1 += (j4 - 1 - i1) / l2;
			}

		}
	}

	// Set alpha channel in rectangular region of
	// two dimensional array p  to 'alpha'
	void SetPAlpha(int x0, int y0, int x1, int y1, int alpha, int p[][]) {
		int hmask = (alpha << 24) + 0xffffff;
		int h = p.length;
		int w = p[0].length;
		int ymin;
		if ((ymin = Math.min(y0, y1)) < 0)
			ymin = 0;
		int ymax;
		if ((ymax = Math.max(y0, y1)) >= h)
			ymax = h - 1;
		if (x0 < 0)
			x0 = 0;
		if (x0 >= w)
			x0 = w - 1;
		if (x1 < 0)
			x1 = 0;
		if (x1 >= w)
			x1 = w - 1;
		if (x1 >= x0) {
			for (int y = ymin; y <= ymax; y++) {
				for (int x = x0; x <= x1; x++)
					p[y][x] &= hmask;

			}

			return;
		}
		for (int y = ymin; y <= ymax; y++) {
			for (int x = 0; x <= x1; x++)
				p[y][x] &= hmask;

			for (int x = x0; x < w; x++)
				p[y][x] &= hmask;

		}

	}

	// this version of the function does not cause strange shifts in the scaled image
	void scaleImage(int pd[][], int width, int height) {
		if (pd != null) {
			int ph = pd.length;
			int pw = pd[0].length;
			int scaleX = (4096 * width) / pw;
			int scaleY = (4096 * height) / ph;
			int shiftX = scaleX/2 - 2048;
			int shiftY = scaleY/2 - 2048;
			int w1 = width - 1;

			for (int y = ph - 1; y >= 0; y--) {
				int ys = y*scaleY + shiftY;
				int dy = (ys >> 4) & 0xff;
				int yd = ys >> 12; 
				int ys0, ys1;
				if (yd < 0)
					ys0 = ys1 = 0;
				else if (yd >= height - 1) {
					ys0 = ys1 = height - 1;
				} else {
					ys0 = yd++;
					ys1 = yd;
				}
				for (int x = pw - 1; x >= 0; x--) {
					int xs = x*scaleX + shiftX;
					int dx = (xs >> 4) & 0xff;
					xs >>= 12;
					int xs0;
					int xs1;
					if (xs < 0)
						xs0 = xs1 = 0;
					else if (xs >= w1) {
						xs0 = xs1 = w1;
					} else {
						xs0 = xs++;
						xs1 = xs;
					}
					pd[y][x] =
						bil(
							pd[ys0][xs0],
							pd[ys0][xs1],
							pd[ys1][xs0],
							pd[ys1][xs1],
							dx,
							dy);
				}

			}

		}
	}

	
	
//	 Scale pixel area to pwidth/pheight
//	 Use same procedure as Panorama Tools
//	void scaleImage(int pd[][], int width, int height) {
//		if (pd != null) {
//			int ph = pd.length;
//			int pw = pd[0].length;
//			int scale = (256 * width) / pw;
//			int w2 = (pw << 7) - 128;
//			int h2 = (ph << 7) - 128;
//			int sw2 = (width << 7) - 128;
//			int sh2 = (height << 7) - 128;
//			int w3 = (-w2 * width) / pw + sw2;
//			int w1 = width - 1;
//			for (int y = ph - 1; y >= 0; y--) {
//				int yd;
//				int dy = (yd = (((y << 8) - h2) * width) / pw + sh2) & 0xff;
//				int ys0;
//				int ys1;
//				if ((yd >>= 8) < 0)
//					ys0 = ys1 = 0;
//				else if (yd >= height - 1) {
//					ys0 = ys1 = height - 1;
//				} else {
//					ys0 = yd++;
//					ys1 = yd;
//				}
//				for (int x = pw - 1; x >= 0; x--) {
//					int xs;
//					int dx = (xs = x * scale + w3) & 0xff;
//					int xs0;
//					int xs1;
//					if ((xs >>= 8) < 0)
//						xs0 = xs1 = 0;
//					else if (xs >= w1) {
//						xs0 = xs1 = w1;
//					} else {
//						xs0 = xs++;
//						xs1 = xs;
//					}
//					pd[y][x] =
//						bil(
//							pd[ys0][xs0],
//							pd[ys0][xs1],
//							pd[ys1][xs0],
//							pd[ys1][xs1],
//							dx,
//							dy);
//				}
//
//			}
//
//		}
//	}

	void ptImageTo2DArray(int ai[][], Image image) {
		if (image == null || ai == null)
			return;
		new ImageTo2DIntArrayExtractor (ai, image).doit();
	}
	
//	void ptImageTo2DArray(int ai[][], Image image) {
//		if (image == null || ai == null)
//			return;
//		int i;
//		if ((i = image.getHeight(null)) * image.getWidth(null) > im_maxarray)
//			i = im_maxarray / image.getWidth(null);
//		int ai1[] = new int[i * image.getWidth(null)];
//		for (int j = 0; j < image.getHeight(null); j += i) {
//			int j1 =
//				i >= image.getHeight(null) - j ? image.getHeight(null) - j : i;
//			PixelGrabber pixelgrabber =
//				new PixelGrabber(
//					image,
//					0,
//					j,
//					image.getWidth(null),
//					j1,
//					ai1,
//					0,
//					image.getWidth(null));
//			try {
//				pixelgrabber.grabPixels();
//			} catch (InterruptedException _ex) {
//				return;
//			}
//			for (int i1 = 0; i1 < j1; i1++) {
//				int w = image.getWidth(null);
//				int k = i1 * w;
//				for (int l = 0; l < w; l++)
//					ai[i1 + j][l] = ai1[k + l] | 0xff000000;
////					System.arraycopy( ai1, k, ai[i1 + j], 0, w );
//			}
//		}
//		System.gc();
//	}

	void ptImageToAlpha(int ai[][], Image image) {
		if (image == null || ai == null)
			return;
		int i;
		if ((i = image.getHeight(null)) * image.getWidth(null) > im_maxarray)
			i = im_maxarray / image.getWidth(null);
		int ai1[] = new int[i * image.getWidth(null)];
		for (int k = 0; k < image.getHeight(null); k += i) {
			int k1 =
				i >= image.getHeight(null) - k ? image.getHeight(null) - k : i;
			PixelGrabber pixelgrabber =
				new PixelGrabber(
					image,
					0,
					k,
					image.getWidth(null),
					k1,
					ai1,
					0,
					image.getWidth(null));
			try {
				pixelgrabber.grabPixels();
			} catch (InterruptedException _ex) {
				return;
			}
			for (int j1 = 0; j1 < k1; j1++) {
				int l = j1 * image.getWidth(null);
				for (int i1 = 0; i1 < image.getWidth(null); i1++) {
					int j = ((ai1[l + i1] & 0xff) << 24) + 0xffffff;
					ai[j1 + k][i1] &= j;
				}

			}

		}

		System.gc();
	}

	void im_insertRect(
		int pd[][],
		int xd,
		int yd,
		int id[],
		int iwidth,
		int xs,
		int ys,
		int width,
		int height) {
		try {
			int y = 0;
			for (int yp = yd; y < height; yp++) {
				int x = 0;
				for (int idx = (ys + y) * iwidth + xs; x < width; idx++) {
					int px;
					if (((px = id[idx]) & 0xff000000) != 0) { //Non transparent

						int xp = x + xd;
						pd[yp][xp] = px & (pd[yp][xp] | 0xffffff);
					}
					x++;
				}

				y++;
			}

			return;
		} catch (Exception _ex) {
			System.out.println("Insert can't be fit into panorama");
		}
	}

	final void im_extractRect(
		int ai[][],
		int i,
		int j,
		int ai1[],
		int k,
		int l,
		int i1,
		int j1,
		int k1) {
		try {
			int i2 = 0;
			for (int j2 = j; i2 < k1; j2++) {
				int l1 = 0;
				for (int k2 = (i1 + i2) * k + l; l1 < j1; k2++) {
					ai1[k2] = pdata[j2][l1 + i] | 0xff000000;
					l1++;
				}

				i2++;
			}

			return;
		} catch (Exception _ex) {
			System.out.println("Invalid rectangle");
		}
	}

	// loads and parses a .ptvref file
	// returns the name of the preview file or null if there is no preview
	String loadPTVRefFile( String fname ) {
		
		byte[] buf = file_read( fname, null );
		if( buf == null ) {
			fatal = true;
			repaint();
			return null;
		}
		
		// extract the path to the file name to correctly locate the referenced files
		String path = "";
		int idx = fname.lastIndexOf('/');
		if( idx >= 0 ) {
			path = fname.substring( 0, idx + 1 );
		}
		
		ByteArrayInputStream ba = new ByteArrayInputStream( buf );
		InputStreamReader isr = new InputStreamReader( ba );
		BufferedReader br = new BufferedReader( isr );
		String previewName, s;
		
		try {
			// reads the preview name
			previewName = br.readLine();
			if( previewName.length() == 0 )
				previewName = null;
			else
				previewName = path + previewName;
			s = br.readLine();
			// pano width
			pwidth = Integer.valueOf(s).intValue();
			s = br.readLine();
			// pano height
			pheight = Integer.valueOf(s).intValue();
			s = br.readLine();
			// numbero of ROIs
			numroi = Integer.valueOf(s).intValue();
			roi_allocate( numroi );
			// reads the ROI lines
			int i = 0;
			while( (s = br.readLine()) != null ) {
				if( s.length() > 0 ) {
					ParseROILine( s, i );
				}
				if( i < numroi ) roi_im[i] = path + roi_im[i];	// adds the path
				i++;
			}
			dynLoadROIs = true;
			return previewName;
		}
		catch( Exception ex ) {
			return null;
		}
	}
	
	final int[][] im_loadPano(String fname, int pd[][], int pw, int ph) {
		ptvf = null;
		boolean showGrid;
		boolean isPTVREF, isPTV;	// flags to see if the file is a .ptvref or a .ptv
		
		isPTV = isPTVREF = false;

		if( fname != null ) {
			if( fname.toUpperCase().endsWith(".PTVREF") ) isPTVREF = true;
			if( fname.toUpperCase().endsWith(".PTV") ) isPTV = true;
			// sees if the filename is in the form:
			//  name.ptvref.txt  or  name.ptv.jpg
			// to bypass web servers that ignore unknows extensions
			if( fname.toUpperCase().indexOf(".PTVREF.") >= 0 ) isPTVREF = true;
			if( fname.toUpperCase().indexOf(".PTV.") >= 0 ) isPTV = true;
		}
		
		// if we are using a .ptvref file open and parse it
		if( isPTVREF ) {
			fname = loadPTVRefFile( fname );
			// pwidth and pheight are set by loadPTVRefFile() 
			pw = pwidth;
			ph = pheight;
		}
		
		// let's see if we are using a .ptv file
		if( isPTV ) { 
			usingCustomFile = true;
			ptvf = new PTVFile( this, fname );
			showGrid = !ptvf.hasPreview;
			pw = ptvf.pWidth;
			ph = ptvf.pHeight;
		}
		else {
			usingCustomFile = false;
			showGrid = (fname == null || fname.equals("_PT_Grid"));
		}

		if ( showGrid ) {
			if (pw == 0)
				pw = 100;	// dummy background
			
			// create grid panorama
			int p[][] = im_allocate_pano(pd, pw, ph != 0 ? ph : pw >> 1);
			im_drawGrid(p, grid_bgcolor, grid_fgcolor);
			return p;
		}

		Image pano;
		if( usingCustomFile ) {
			pano = ptvf.loadPreviewImage();
		}
		else {
			pano = loadImageProgress(fname);
		}
		if (pano == null)
			return null;

		// At this point we have a valid panorama image
	 	// Check size:
		
		if (pw > pano.getWidth(null)) {
			if (ph == 0)
				ph = pw >> 1;
		} else {
			pw = pano.getWidth(null);
			ph = pano.getHeight(null);
		}
		// Set up data array for panorama pixels
		int p[][];
		if ((p = im_allocate_pano(pd, pw, ph)) == null)
			return null;
		ptImageTo2DArray(p, pano);
		if (pw != pano.getWidth(null)) {
			scaleImage(p, pano.getWidth(null), pano.getHeight(null));
			if( dynLoadROIs ) {
				// this is a low resolution preview: do not draw it with Lanczos2
				forceBilIntepolator = true;
			}
		}
		return p;
	}

	int[][] im_halfsize(int ai[][]) {
		int i = ai.length;
		int j = ai[0].length;
		int k = i >> 1;
		int l = j >> 1;
		int ai1[][];
		if ((ai1 = new int[k][l]) == null)
			return null;
		int i1 = 0;
		int j1 = 0;
		for (int k1 = 1; i1 < k; k1 += 2) {
			int ai2[] = ai[j1];
			int ai3[] = ai[k1];
			int ai4[] = ai1[i1];
			int l1 = 0;
			int i2 = 0;
			for (int j2 = 1; l1 < l; j2 += 2) {
				ai4[l1] = im_pixelaverage(ai2[i2], ai2[j2], ai3[i2], ai3[j2]);
				l1++;
				i2 += 2;
			}

			i1++;
			j1 += 2;
		}

		return ai1;
	}

	byte[][] im_halfsize(byte abyte0[][]) {
		int i = abyte0.length;
		int j = abyte0[0].length;
		int k = i >> 1;
		int l = j >> 1;
		byte abyte1[][];
		if ((abyte1 = new byte[k][l]) == null)
			return null;
		int i1 = 0;
		for (int j1 = 0; i1 < k; j1 += 2) {
			byte abyte2[] = abyte0[j1];
			byte abyte3[] = abyte1[i1];
			int k1 = 0;
			for (int l1 = 0; k1 < l; l1 += 2) {
				abyte3[k1] = abyte2[l1];
				k1++;
			}

			i1++;
		}

		return abyte1;
	}

	static final int im_pixelaverage(int i, int j, int k, int l) {
		int i1;
		if ((i1 =
			(i >> 16 & 0xff)
				+ (j >> 16 & 0xff)
				+ (k >> 16 & 0xff)
				+ (l >> 16 & 0xff) >> 2)
			< 0)
			i1 = 0;
		if (i1 > 255)
			i1 = 255;
		int j1;
		if ((j1 =
			(i >> 8 & 0xff)
				+ (j >> 8 & 0xff)
				+ (k >> 8 & 0xff)
				+ (l >> 8 & 0xff) >> 2)
			< 0)
			j1 = 0;
		if (j1 > 255)
			j1 = 255;
		int k1;
		if ((k1 = (i & 0xff) + (j & 0xff) + (k & 0xff) + (l & 0xff) >> 2) < 0)
			k1 = 0;
		if (k1 > 255)
			k1 = 255;
		return (i & 0xff000000) + (i1 << 16) + (j1 << 8) + k1;
	}

	String resolveQuotes(String s) {
		if (s == null)
			return null;
		int j;
		if ((j = s.length()) < 6)
			return s;
		StringBuffer stringbuffer = new StringBuffer(0);
		int i;
		for (i = 0; i < j - 5; i++)
			if (s.substring(i, i + 6).equalsIgnoreCase("&quot;")) {
				stringbuffer.append('"');
				i += 5;
			} else {
				stringbuffer.append(s.charAt(i));
			}

		stringbuffer.append(s.substring(i, j));
		return stringbuffer.toString();
	}

	String stripWhiteSpace(String s) {
		if (s == null)
			return null;
		int i = 0;
		int j;
		int k = (j = s.length()) - 1;
		for (;
			i < j
				&& (s.charAt(i) == ' '
					|| s.charAt(i) == '\r'
					|| s.charAt(i) == '\n'
					|| s.charAt(i) == '\t');
			i++);
		if (i == j)
			return null;
		for (;
			k >= 0
				&& (s.charAt(k) == ' '
					|| s.charAt(k) == '\r'
					|| s.charAt(k) == '\n'
					|| s.charAt(k) == '\t');
			k--);
		if (k < 0 || k < i)
			return null;
		else
			return s.substring(i, k + 1);
	}

	Dimension string_textWindowSize(Graphics g, String s) {
		FontMetrics fontmetrics = g.getFontMetrics();
		int i = 0;
		int k = 1;
		int l = 0;
		int j;
		while ((j = s.indexOf('|', i)) != -1 && j < s.length() - 1) {
			int i1;
			if ((i1 = fontmetrics.stringWidth(s.substring(i, j))) > l)
				l = i1;
			k++;
			i = j + 1;
		}
		int j1;
		if ((j1 = fontmetrics.stringWidth(s.substring(i))) > l)
			l = j1;
		return new Dimension(
			l + 10,
			k * fontmetrics.getHeight() + (fontmetrics.getHeight() >> 1));
	}

	void string_drawTextWindow(
		Graphics g,
		int i,
		int j,
		Dimension dimension,
		Color color,
		String s,
		int k) {
		g.clearRect(i, j, dimension.width, dimension.height);
		if (color == null)
			g.setColor(Color.black);
		else
			g.setColor(color);
		FontMetrics fontmetrics = g.getFontMetrics();
		int l = 0;
		int j1 = 1;
		int i1;
		while ((i1 = s.indexOf('|', l)) != -1 && i1 < s.length() - 1) {
			g.drawString(
				s.substring(l, i1),
				i + 5,
				j + j1 * fontmetrics.getHeight());
			j1++;
			l = i1 + 1;
		}
		g.drawString(s.substring(l), i + 5, j + j1 * fontmetrics.getHeight());
		switch (k) {
			case 1 : // '\001'
				g.fillRect(i, (j + dimension.height) - 2, 2, 2);
				return;

			case 2 : // '\002'
				g.fillRect(i, j, 2, 2);
				return;

			case 3 : // '\003'
				g.fillRect(
					(i + dimension.width) - 2,
					(j + dimension.height) - 2,
					2,
					2);
				return;

			case 4 : // '\004'
				g.fillRect((i + dimension.width) - 2, j, 2, 2);
				break;
		}
	}

	//    public String myGetParameter(String s, String s1)
	//    {
	//        String s2;
	//        String s3;
	//        if(s != null)
	//            break MISSING_BLOCK_LABEL_20;
	//        s3 = resolveQuotes(getParameter(s1));
	//        s3;
	//        s2 = s3;
	//        JVM INSTR ifnull 33;
	//           goto _L1 _L2
	//_L1:
	//        break MISSING_BLOCK_LABEL_18;
	//_L2:
	//        break MISSING_BLOCK_LABEL_33;
	//        return s2;
	//        if((s2 = extractParameter(s, s1)) != null)
	//            return s2;
	//        return extractParameter(PTViewer_Properties, s1);
	//    }

	/** Read parameter values from a list of parameter tags.
	 * The list has the syntax <p>
	 *<CODE>{param1=value1} {param2=value2} {param3=value3}</CODE>
	 * @param p The list string.
	 * @param param The parameter name.
	 */
	// from version 2.1
	public String myGetParameter(String p, String param) {
		String r;

		if (p == null) {
			r = resolveQuotes(getParameter(param));
			if (r != null) {
				return r;
			}
		} else {
			r = extractParameter(p, param);
			if (r != null) {
				return r;
			}
		}

		return extractParameter(PTViewer_Properties, param);
	}

	String extractParameter(String s, String s1) {
		int j = 0;
		if (s == null || s1 == null)
			return null;
		int i;
		String s2;
		String s1u, s2u; // upper case versions of s1 and s2, used for case insensitivity
		s1u = s1.toUpperCase();
		while ((i = s.indexOf('{', j)) >= 0 && (j = s.indexOf('}', i)) >= 0) {
			s2 = stripWhiteSpace(s.substring(i + 1, j));
			s2u = s2.toUpperCase();
			if (s2u.startsWith(s1u + "=") )
				return resolveQuotes(
					stripWhiteSpace(s2.substring(s2.indexOf('=') + 1)));
		}
		return null;
	}

	int getNextWord(int i, String s, StringBuffer stringbuffer) {
		int j = i;
		int k = s.length();
		if (i >= k)
			return i;
		if (s.charAt(i) == '\'') {
			if (++i == k) {
				stringbuffer.setLength(0);
				return i;
			}
			j = i;
			for (; i < k && s.charAt(i) != '\''; i++);
			if (i < k) {
				stringbuffer.insert(0, s.substring(j, i));
				stringbuffer.setLength(s.substring(j, i).length());
			} else {
				stringbuffer.insert(0, s.substring(j));
				stringbuffer.setLength(s.substring(j).length());
			}
			return i;
		}
		if (s.charAt(i) == '$') {
			if (++i == k) {
				stringbuffer.setLength(0);
				return i;
			}
			char c = s.charAt(i);
			if (++i == k) {
				stringbuffer.setLength(0);
				return i;
			}
			j = i;
			for (; i < k && s.charAt(i) != c; i++);
			if (i < k) {
				stringbuffer.insert(0, s.substring(j, i));
				stringbuffer.setLength(s.substring(j, i).length());
			} else {
				stringbuffer.insert(0, s.substring(j));
				stringbuffer.setLength(s.substring(j).length());
			}
			return i;
		}
		for (;
			i < k
				&& s.charAt(i) != ' '
				&& s.charAt(i) != '\r'
				&& s.charAt(i) != '\n'
				&& s.charAt(i) != '\t';
			i++);
		if (i < k) {
			stringbuffer.insert(0, s.substring(j, i));
			stringbuffer.setLength(s.substring(j, i).length());
		} else {
			stringbuffer.insert(0, s.substring(j));
			stringbuffer.setLength(s.substring(j).length());
		}
		return i;
	}

	final String getArg(int i, String s, char c) {
		int k = 0;
		if (s == null)
			return null;
		for (int j = 0; j < i; j++) {
			if ((k = s.indexOf(c, k)) == -1)
				return null;
			k++;
		}

		int l;
		if ((l = s.indexOf(c, k)) == -1)
			return s.substring(k);
		else
			return s.substring(k, l);
	}

	final String getArg(int i, String s) {
		return getArg(i, s, ',');
	}

	final int getNumArgs(String s) {
		return getNumArgs(s, ',');
	}

	final int getNumArgs(String s, char c) {
		int j = 0;
		if (s == null)
			return 0;
		int i;
		for (i = 1;(j = s.indexOf(c, j)) != -1; i++)
			j++;

		return i;
	}

	void file_init() {
		file_cachefiles = true;
		file_Cache = new Hashtable();
	}

	void file_dispose() {
		if (file_Cache != null) {
			file_Cache.clear();
			file_Cache = null;
		}
	}

	// reads an image from the jar file containing the applet
	// returns null if not found
	Image readImageFromJAR( String name ) {
		byte readBuffer[];
		Image im;
		
		try {
			MediaTracker m = new MediaTracker( this );
			InputStream is = getClass().getResourceAsStream( name );
			if( is == null ) return null;
			readBuffer = new byte[is.available()];
			is.read( readBuffer );
			im = Toolkit.getDefaultToolkit().createImage(readBuffer);
			m.addImage( im, 0 );
			m.waitForAll();
		}
		catch( Exception e ) {
			im = null;
		}
			return im;
	}
	
	
	byte[] file_read(String name, int progress[]) {
		byte readBuffer[];
		if ((readBuffer = (byte[]) file_Cache.get(name)) != null) {
			if (progress != null) {
				progress[0] = 80;
				repaint();
			}
			return readBuffer;
		}
		try {
			URLConnection urlconnection;
			(
				urlconnection =
					(new URL(getDocumentBase(), name))
						.openConnection())
						.setUseCaches(
				true);
			int i;
			try {
				i = urlconnection.getContentLength();
			} catch (Exception _ex) {
				i = 0;
			}
			InputStream inputstream = urlconnection.getInputStream();
			readBuffer = file_read(inputstream, i, progress);
			inputstream.close();
			if (readBuffer != null) {
				m3(readBuffer, name);
				if (file_cachefiles)
					synchronized (file_Cache) {
						file_Cache.put(name, readBuffer);
					}
				return readBuffer;
			}
		} catch (Exception _ex) {
		}
		try {
			URLConnection urlconnection1;
			(
				urlconnection1 =
					(new URL(getCodeBase(), name)).openConnection()).setUseCaches(
				true);
			int j;
			try {
				j = urlconnection1.getContentLength();
			} catch (Exception _ex) {
				j = 0;
			}
			InputStream inputstream1 = urlconnection1.getInputStream();
			readBuffer = file_read(inputstream1, j, progress);
			inputstream1.close();
			if (readBuffer != null) {
				m3(readBuffer, name);
				if (file_cachefiles)
					synchronized (file_Cache) {
						file_Cache.put(name, readBuffer);
					}
				return readBuffer;
			}
		} catch (Exception _ex) {
		}
		try {
			InputStream inputstream2;
			if ((inputstream2 =
				Class.forName("ptviewer").getResourceAsStream(name))
				!= null) {
				readBuffer = file_read(inputstream2, 0, null);
				inputstream2.close();
			}
			if (readBuffer != null) {
				m3(readBuffer, name);
				if (file_cachefiles)
					synchronized (file_Cache) {
						file_Cache.put(name, readBuffer);
					}
				return readBuffer;
			}
		} catch (Exception _ex) {
		}
		return null;
	}

	byte[] file_read(InputStream is, int fsize, int progress[]) {
		int j = 0;
		int l = 0;
		int i1 = fsize <= 0 ? 50000 : fsize / 10 + 1;
		byte abyte0[] = new byte[fsize <= 0 ? 50000 : fsize];
		try {
			while (l != -1) {
				int k = 0;
				if (abyte0.length < j + i1) {
					byte abyte1[] = new byte[j + i1];
					System.arraycopy(abyte0, 0, abyte1, 0, j);
					abyte0 = abyte1;
				}
				while (k < i1
					&& (l = is.read(abyte0, j, i1 - k)) != -1) {
					k += l;
					j += l;
					if (fsize > 0 && progress != null) {
						progress[0] = ((800 * j) / fsize + 5) / 10;
						if (progress[0] > 100)
							progress[0] = 100;
						repaint();
					}
				}
			}
			if (abyte0.length > j) {
				byte abyte2[] = new byte[j];
				System.arraycopy(abyte0, 0, abyte2, 0, j);
				abyte0 = abyte2;
			}
		} catch (Exception _ex) {
			return null;
		}
		return abyte0;
	}

	private void m2(byte abyte0[], byte abyte1[]) {
		int i = 0;
		for (int k = 0; i < abyte0.length; k++) {
			if (k >= abyte1.length)
				k = 0;
			abyte0[i] ^= abyte1[k];
			i++;
		}

		int ai[] =
			{
				1,
				20,
				3,
				18,
				0,
				17,
				14,
				11,
				22,
				19,
				2,
				5,
				7,
				6,
				13,
				4,
				21,
				8,
				10,
				9,
				12,
				15,
				16 };
		int i1 = abyte0.length - ai.length;
		byte abyte2[] = new byte[ai.length];
		for (int j = 0; j < i1; j += ai.length) {
			System.arraycopy(abyte0, j, abyte2, 0, ai.length);
			for (int l = 0; l < ai.length; l++)
				abyte0[l + j] = abyte2[ai[l]];

		}

	}

	private void m3(byte abyte0[], String s) {
		if (abyte0 == null || s == null)
			return;
		int i;
		if ((i = s.lastIndexOf('.')) < 0 || i + 1 >= s.length())
			return;
		byte abyte1[] =
			{
				122,
				1,
				12,
				-78,
				-99,
				-33,
				-50,
				17,
				88,
				90,
				-117,
				119,
				30,
				20,
				10,
				33,
				27,
				114,
				121,
				3,
				-11,
				51,
				97,
				-59,
				-32,
				-28,
				0,
				83,
				37,
				43,
				-67,
				17,
				32,
				31,
				70,
				-70,
				-10,
				-39,
				-33,
				2,
				55,
				59,
				-88 };
		if (s.substring(i + 1).equalsIgnoreCase("jpa")) {
			m2(abyte0, abyte1);
			return;
		}
		if (s.substring(i + 1).equalsIgnoreCase("jpb")) {
			byte abyte2[] = m1().getBytes();
			byte abyte4[] = new byte[abyte1.length + abyte2.length];
			System.arraycopy(abyte1, 0, abyte4, 0, abyte1.length);
			System.arraycopy(abyte2, 0, abyte4, abyte1.length, abyte2.length);
			m2(abyte0, abyte4);
			return;
		}
		if (s.substring(i + 1).equalsIgnoreCase("jpc")) {
			byte abyte3[] = getDocumentBase().toString().getBytes();
			byte abyte5[] = new byte[abyte1.length + abyte3.length];
			System.arraycopy(abyte1, 0, abyte5, 0, abyte1.length);
			System.arraycopy(abyte3, 0, abyte5, abyte1.length, abyte3.length);
			m2(abyte0, abyte5);
		}
	}

	void pb_reset() {
		percent[0] = 0;
	}

	void pb_init() {
		percent = new int[1];
		percent[0] = 0;
	}

	void pb_draw(Graphics g, int i, int j) {
		if (pb_x == -1)
			pb_x = i >> 2;
		if (pb_y == -1)
			pb_y = j * 3 >> 2;
		if (pb_width == -1)
			pb_width = i >> 1;
		int k = 0;
		if (percent != null)
			k = percent[0];
		g.setColor(pb_color);
		g.fillRect(pb_x, pb_y, (pb_width * k) / 100, pb_height);
	}

	void shs_init() {
		shotspots = new Vector();
	}

	void shs_setup() {
		if (shotspots.size() > 0) {
			shs_allocate(shotspots.size());
			for (int i = 0; i < numshs; i++)
				ParseStaticHotspotLine((String) shotspots.elementAt(i), i);

		}
	}

	void shs_allocate(int i) {
		try {
			shs_x1 = new int[i];
			shs_x2 = new int[i];
			shs_y1 = new int[i];
			shs_y2 = new int[i];
			shs_url = new String[i];
			shs_target = new String[i];
			shs_him = new Object[i];
			shs_imode = new int[i];
			shs_active = new boolean[i];
			numshs = i;
			return;
		} catch (Exception _ex) {
			numshs = 0;
		}
	}

	void shs_dispose() {
		for (int i = 0; i < numshs; i++)
			if (shs_him[i] != null)
				shs_him[i] = null;

		numshs = 0;
	}

	void ParseStaticHotspotLine(String s, int i) {
		int j = 0;
		int k = s.length();
		StringBuffer stringbuffer = new StringBuffer();
		shs_x1[i] = 0;
		shs_x2[i] = 0;
		shs_y1[i] = 0;
		shs_y2[i] = 0;
		shs_url[i] = null;
		shs_target[i] = null;
		shs_him[i] = null;
		shs_imode[i] = 0;
		shs_active[i] = false;
		while (j < k)
			switch (s.charAt(j++)) {
				case 99 : // 'c'
				case 100 : // 'd'
				case 101 : // 'e'
				case 102 : // 'f'
				case 103 : // 'g'
				case 104 : // 'h'
				case 106 : // 'j'
				case 107 : // 'k'
				case 108 : // 'l'
				case 109 : // 'm'
				case 110 : // 'n'
				case 111 : // 'o'
				case 114 : // 'r'
				case 115 : // 's'
				case 118 : // 'v'
				case 119 : // 'w'
				default :
					break;

				case 120 : // 'x'
					j = getNextWord(j, s, stringbuffer);
					shs_x1[i] = Integer.parseInt(stringbuffer.toString());
					if( shs_x1[i] < 0 )
						shs_x1[i] += (vwidth == 0 ? getSize().width : vwidth);
					break;

				case 121 : // 'y'
					j = getNextWord(j, s, stringbuffer);
					shs_y1[i] = Integer.parseInt(stringbuffer.toString());
					if( shs_y1[i] < 0 )
						shs_y1[i] += (vheight == 0 ? getSize().height : vheight);
					break;

				case 97 : // 'a'
					j = getNextWord(j, s, stringbuffer);
					shs_x2[i] = Integer.parseInt(stringbuffer.toString());
					if( shs_x2[i] < 0 )
						shs_x2[i] += (vwidth == 0 ? getSize().width : vwidth);
					break;

				case 98 : // 'b'
					j = getNextWord(j, s, stringbuffer);
					shs_y2[i] = Integer.parseInt(stringbuffer.toString());
					if( shs_y2[i] < 0 )
						shs_y2[i] += (vheight == 0 ? getSize().height : vheight);
					break;

				case 117 : // 'u'
					j = getNextWord(j, s, stringbuffer);
					shs_url[i] = stringbuffer.toString();
					break;

				case 116 : // 't'
					j = getNextWord(j, s, stringbuffer);
					shs_target[i] = stringbuffer.toString();
					break;

				case 112 : // 'p'
					shs_imode[i] = 1;
					break;

				case 113 : // 'q'
					shs_imode[i] = 2;
					break;

				case 105 : // 'i'
					j = getNextWord(j, s, stringbuffer);
					if (stringbuffer.toString().startsWith("ptviewer:")
						|| stringbuffer.toString().startsWith("javascript:"))
						shs_him[i] = stringbuffer.toString();
					else
						shs_him[i] = loadImage(stringbuffer.toString());
					break;
			}
	}

	final void shs_draw(Graphics g) {
		for (int i = 0; i < numshs; i++)
			if (shs_him[i] != null) {
				if (((shs_imode[i] & 2) > 0
					|| shs_active[i]
					&& (shs_imode[i] & 1) > 0)
					&& (shs_him[i] instanceof Image))
					g.drawImage((Image) shs_him[i], shs_x1[i], shs_y1[i], this);
				if ((shs_him[i] instanceof String) && shs_active[i])
					JumpToLink((String) shs_him[i], null);
			}

	}

	final int OverStaticHotspot(int i, int j) {
		int l = -1;
		for (int k = 0; k < numshs; k++)
			if (shs_url[k] != null
				&& i >= shs_x1[k]
				&& i <= shs_x2[k]
				&& (j >= shs_y1[k]
					&& j <= shs_y2[k]
					|| j >= shs_y2[k]
					&& j <= shs_y1[k])) {
				if( shs_imode[k] == 0 && shsEnableVisibleOnly ) {
					shs_active[k] = false;
				}
				else {
					shs_active[k] = true;
					if (k > l)
						l = k;
				}
			} else {
				shs_active[k] = false;
			}

		return l;
	}

//	final int OverStaticHotspot(int i, int j) {
//		int l = -1;
//		for (int k = 0; k < numshs; k++)
//			if (shs_url[k] != null
//				&& i >= shs_x1[k]
//				&& i <= shs_x2[k]
//				&& (j >= shs_y1[k]
//					&& j <= shs_y2[k]
//					|| j >= shs_y2[k]
//					&& j <= shs_y1[k])) {
//				shs_active[k] = true;
//				if (k > l)
//					l = k;
//			} else {
//				shs_active[k] = false;
//			}
//
//		return l;
//	}
//
	void math_init() {
		mt = new double[3][3];
		mi = new long[3][3];
	}

	void math_dispose() {
		atan_LU_HR = null;
		sqrt_LU = null;
		mt = null;
		mi = null;
	}

	final void math_setLookUp(int ai[][]) {
		if (ai != null) {
			if (atan_LU_HR == null) {
				atan_LU_HR = new int[NATAN + 1];
				atan_LU = new double[NATAN + 1];
				sqrt_LU = new int[NSQRT + 1];
//				double d1 = 0.000244140625D;
				double d1 = 1.0 / (double) NSQRT;
				double d = 0.0D;
				for (int i = 0; i < NSQRT;) {
					sqrt_LU[i] = (int) (Math.sqrt(1.0D + d * d) * NSQRT);
					i++;
					d += d1;
				}

				sqrt_LU[NSQRT] = (int) (Math.sqrt(2D) * NSQRT);
//				d1 = 0.000244140625D;
				d1 = 1.0 / (double) NATAN;
				d = 0.0D;
				for (int j = 0; j < NATAN + 1;) {
					if (j < NATAN)
						atan_LU[j] = Math.atan(d / (1.0D - d)) * 256D;
					else
						atan_LU[j] = 402.12385965949352D;
					j++;
					d += d1;
				}

			}
			math_updateLookUp(ai[0].length);
		}
	}

	final void math_updateLookUp(int i) {
		int j = i << 6;
		if (PV_atan0_HR != j) {
			dist_e = (double) i / 6.2831853071795862D;
			PV_atan0_HR = j;
			PV_pi_HR = 128 * i;
			for (int k = 0; k < NATAN + 1; k++)
				atan_LU_HR[k] = (int) (dist_e * atan_LU[k] + 0.5D);

		}
	}

	final void SetMatrix(double d, double d1, double ad[][], int i) {
		double ad1[][] = new double[3][3];
		double ad2[][] = new double[3][3];
		ad1[0][0] = 1.0D;
		ad1[0][1] = 0.0D;
		ad1[0][2] = 0.0D;
		ad1[1][0] = 0.0D;
		ad1[1][1] = Math.cos(d);
		ad1[1][2] = Math.sin(d);
		ad1[2][0] = 0.0D;
		ad1[2][1] = -ad1[1][2];
		ad1[2][2] = ad1[1][1];
		ad2[0][0] = Math.cos(d1);
		ad2[0][1] = 0.0D;
		ad2[0][2] = -Math.sin(d1);
		ad2[1][0] = 0.0D;
		ad2[1][1] = 1.0D;
		ad2[1][2] = 0.0D;
		ad2[2][0] = -ad2[0][2];
		ad2[2][1] = 0.0D;
		ad2[2][2] = ad2[0][0];
		if (i == 1) {
			matrix_matrix_mult(ad1, ad2, ad);
			return;
		} else {
			matrix_matrix_mult(ad2, ad1, ad);
			return;
		}
	}

	final void matrix_matrix_mult(
		double ad[][],
		double ad1[][],
		double ad2[][]) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				ad2[i][j] =
					ad[i][0] * ad1[0][j]
						+ ad[i][1] * ad1[1][j]
						+ ad[i][2] * ad1[2][j];

		}

	}

	final int PV_atan2_HR(int pi, int pj)
	{
			long i = pi;
			long j = pj;
			int index;
			if(j > 0)
					if(i > 0)
							return atan_LU_HR[(int)((NATAN * i) / (j + i))];
					else
							return -atan_LU_HR[(int) ((NATAN * -i) / (j - i))];
			if(j == 0)
					if(i > 0)
							return PV_atan0_HR;
					else
							return -PV_atan0_HR;
			if(i < 0) {
				index = (int) ((NATAN * i) / (j + i));
				return atan_LU_HR[index] - PV_pi_HR;
//				return atan_LU_HR[(int) ((NATAN * i) / (j + i))] - PV_pi_HR;
			}
			else
					return -atan_LU_HR[(int) ((NATAN * -i) / (j - i))] + PV_pi_HR;
	}

	
	final int PV_sqrt(int pi, int pj) {
		long i = pi;
		long j = pj;
		if (i > j)
			return (int) (i * sqrt_LU[(int) ((j << NSQRT_SHIFT) / i)] >> NSQRT_SHIFT);
		if (j == 0)
			return 0;
		else
			return (int) (j * sqrt_LU[(int) ((i << NSQRT_SHIFT) / j)] >> NSQRT_SHIFT);
	}

	static final int bil(int p00, int p01, int p10, int p11, int dx, int dy) {
		int k1 = 255 - dx;
		int l1 = 255 - dy;
		int i2 = k1 * l1;
		int j2 = dy * k1;
		int k2 = dx * dy;
		int l2 = dx * l1;
		int i3 =
			i2 * (p00 >> 16 & 0xff)
				+ l2 * (p01 >> 16 & 0xff)
				+ j2 * (p10 >> 16 & 0xff)
				+ k2 * (p11 >> 16 & 0xff) & 0xff0000;
		int j3 =
			i2 * (p00 >> 8 & 0xff)
				+ l2 * (p01 >> 8 & 0xff)
				+ j2 * (p10 >> 8 & 0xff)
				+ k2 * (p11 >> 8 & 0xff) >> 16;
		int k3 =
			i2 * (p00 & 0xff)
				+ l2 * (p01 & 0xff)
				+ j2 * (p10 & 0xff)
				+ k2 * (p11 & 0xff) >> 16;
		return i3 + (j3 << 8) + k3 + 0xff000000;
	}

	final void math_extractview(
		int pd[][],
		int v[],
		byte hv[],
		int vw,
		double fov,
		double pan,
		double tilt,
		boolean bilinear,
		boolean lanczos2) {
			
		if(lanczos2) {
			double prev_view_scale = view_scale;
			lanczos2_compute_view_scale();
			if (view_scale != prev_view_scale)
				lanczos2_compute_weights(view_scale);
		}

		math_set_int_matrix(fov, pan, tilt, vw);
		math_transform(
			pd,
			pd[0].length,
			pd.length + deltaYHorizonPosition,
			v,
			hv,
			vw,
			v.length / vw,
			bilinear,
			lanczos2);
	}

	final void math_set_int_matrix(double fov, double pan, double tilt, int vw) {
		double a = (fov * 2D * 3.1415926535897931D) / 360D; // field of view in rad
		double p = (double) vw / (2D * Math.tan(a / 2D));
		SetMatrix(
			(tilt * 2D * 3.1415926535897931D) / 360D,
			(pan * 2D * 3.1415926535897931D) / 360D,
			mt,
			1);
		mt[0][0] /= p;
		mt[0][1] /= p;
		mt[0][2] /= p;
		mt[1][0] /= p;
		mt[1][1] /= p;
		mt[1][2] /= p;
		double ta =
			a <= 0.29999999999999999D ? 436906.66666666669D : 131072D / a;
		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 3; k++) 
				mi[j][k] = (long) (ta * mt[j][k] * MI_MULT + 0.5D);
//			mi[j][k] = (int) (ta * mt[j][k] * MI_MULT + 0.5D);
//			mi[j][k] = (int) (ta * mt[j][k] + 0.5D);

		}

	}

	/*
	 * if bilinear == true use bilinear interpolation
	 * if lanczos2 == true use lanczos2 interpolation
	 * if bilinear == false && lanczos2 == false use nearest neighbour interpolation
	 */
	final void math_transform(
		int pd[][],
		int pw,
		int ph,
		int v[],
		byte hv[],
		int vw,
		int vh,
		boolean bilinear,
		boolean lanczos2) {

		// flag: use nearest neighbour interpolation
		boolean nn = (!bilinear && !lanczos2);

		boolean firstTime;	// flag
		int itmp;	// temporary variable used as a loop index
		
		int mix = pw - 1;
		int miy = ph - deltaYHorizonPosition - 1;
		int w2 = vw - 1 >> 1;
		int h2 = vh >> 1;
		int sw2 = pw >> 1;
		int sh2 = ph >> 1;
		int x_min = -w2;
		int x_max = vw - w2;
		int y_min = -h2;
		int y_max = vh - h2;
		int cy = 0;

		int xs_org, ys_org;	// used for lanczos2 interpolation
		int l24 = 0;
		int pd_0[] = pd[0];
		int pd_1[] = pd[1];
		long m0 = mi[1][0] * y_min + mi[2][0];
		long m1 = mi[1][1] * y_min + mi[2][1];
		long m2 = mi[1][2] * y_min + mi[2][2];
		long mi_00 = mi[0][0];
		long mi_02 = mi[0][2];
		double vfov_2 = math_fovy(hfov, vw, vh) / 2D;

		// number of points to be computed with linear interpolation
		// between two correctly computed points along the x-axis
		int N_POINTS_INTERP_X = vw/20;
//System.out.println("Max view: " + (pitch + vfov_2) );

//		if (pitch + vfov_2 > 45D || pitch - vfov_2 < -45D) N_POINTS_INTERP_X = vw/30; 
//		if (pitch + vfov_2 > 50D || pitch - vfov_2 < -50D) N_POINTS_INTERP_X = vw/40; 
		if (pitch + vfov_2 > 65D || pitch - vfov_2 < -65D) N_POINTS_INTERP_X = vw/35; 
		if (pitch + vfov_2 > 70D || pitch - vfov_2 < -70D) N_POINTS_INTERP_X = vw/50; 
		if (pitch + vfov_2 > 80D || pitch - vfov_2 < -80D) N_POINTS_INTERP_X = vw/200; 
		int N_POINTS_INTERP_X_P1 = N_POINTS_INTERP_X + 1;

		// number of rows to be computed with linear interpolation
		// between two correctly computed rows along the y-axis
		int N_POINTS_INTERP_Y;
		int N_POINTS_INTERP_Y_P1;
		
		///////////////////////////////////////////////////
		// the standard settings cause artifacts at the poles, so we disable interpolation 
		// between rows when we draw the poles
		//
		// since correctly drawing the poles requires very few interpolated points in each row
		// we will interpolate on a larger distance between rows when we are far away from the poles
		// in order to speed up computation
		//
		// so if a pole is in the viewer window things will go this way, considering rows
		// from top to bottom:
		//  - the first rows are very far from the pole and they will be drawn with double
		//    y interpolation to speed up things
		//  - then some rows are nearer to the pole and will be drawn with standard y interpolation
		//  - now we draw the pole without y interpolation
		//  - then we draw some lines with standard y interpolation
		//  - the last lines are farther from the pole and will be drawn with double
		//    y interpolation
		//
		// first row to draw without y-interpolation (default none)
		int N_ROW_NO_INTERP_MIN = y_max + 100;
		// last row to draw without y-interpolation (default none)
		int N_ROW_NO_INTERP_MAX = N_ROW_NO_INTERP_MIN;
		
		// last row of the upper part of the window to draw with double y-interpolation (default none)
		// we will use double distance from row 0 to this row
		int N_ROW_DOUBLE_INTERP_LOW = y_min - 100;
		// first row of the lower part of the window to draw with double y-interpolation (default none)
		// we will use double distance from this row to the last one
		int N_ROW_DOUBLE_INTERP_HIGH = y_max + 100;
		
		if( vfov_2 > 10 ) { // only if not zooming in too much...
			// we consider critical the zone at +/- 5 degrees from the poles
			if (pitch + vfov_2 > 87.5 || pitch - vfov_2 < -87.5) {
				if( pitch > 0 ) {
					// looking upwards
					N_ROW_NO_INTERP_MIN = y_min + (int) ((y_max - y_min)*
							                    (1 - (92.5 - (pitch - vfov_2))/(2*vfov_2)));
					N_ROW_NO_INTERP_MAX = y_min + (int) ((y_max - y_min)*
		                                        (1 - (87.5 - (pitch - vfov_2))/(2*vfov_2)));
				}
				else {
					N_ROW_NO_INTERP_MIN = y_min + (int) ((y_max - y_min)*
		                    					(1 - (-87.5 - (pitch - vfov_2))/(2*vfov_2)));
					N_ROW_NO_INTERP_MAX = y_min + (int) ((y_max - y_min)*
												(1 - (-92.5 - (pitch - vfov_2))/(2*vfov_2)));
					
				}
			}
			// we draw with double y-interpolation the zone outside +/- 10 degrees from the poles
			double angle = 10;
			if (pitch + vfov_2 > 90 - angle || pitch - vfov_2 < -90 + angle) {
				if( pitch > 0 ) {
					// looking upwards
					N_ROW_DOUBLE_INTERP_LOW = y_min + (int) ((y_max - y_min)*
							                    (1 - (90 + angle - (pitch - vfov_2))/(2*vfov_2)));
					N_ROW_DOUBLE_INTERP_HIGH = y_min + (int) ((y_max - y_min)*
		                                        (1 - (90 - angle - (pitch - vfov_2))/(2*vfov_2)));
				}
				else {
					N_ROW_DOUBLE_INTERP_LOW = y_min + (int) ((y_max - y_min)*
		                    					(1 - (-90 + angle - (pitch - vfov_2))/(2*vfov_2)));
					N_ROW_DOUBLE_INTERP_HIGH = y_min + (int) ((y_max - y_min)*
												(1 - (-90 - angle - (pitch - vfov_2))/(2*vfov_2)));
					
				}
			}
//			System.out.println( "Min " + N_ROW_NO_INTERP_MIN + "       Max " + N_ROW_NO_INTERP_MAX );
//			System.out.println( "Low " + N_ROW_DOUBLE_INTERP_LOW + "       High " + N_ROW_DOUBLE_INTERP_HIGH );
		}
		///////////////////////////////////////////////////////////
		
		// data used for interpolation between rows:
		// size of the arrays used to store row values
		int ROWS_INT_SIZE = vw / N_POINTS_INTERP_X + 4;	// just to be safe...
		// coordinates of vertices in the upper computed row
		int[] row_xold = new int[ROWS_INT_SIZE];
		int[] row_yold = new int[ROWS_INT_SIZE];
		// coordinates of vertices in the lower computed row
		int[] row_xnew = new int[ROWS_INT_SIZE];
		int[] row_ynew = new int[ROWS_INT_SIZE];
		// difference between each interpolated line
		int[] row_xdelta = new int[ROWS_INT_SIZE];
		int[] row_ydelta = new int[ROWS_INT_SIZE];
		// used when drawing a line, contains the interpolted values every N_POINTS_INTERP_P1 pixels
		int[] row_xcurrent = new int[ROWS_INT_SIZE];
		int[] row_ycurrent = new int[ROWS_INT_SIZE];
		
		// shifted widh of the panorama
		int pw_shifted = (pw << 8);
		int pw_shifted_2 = pw_shifted / 2;
		int pw_shifted_3 = pw_shifted / 3;

		// used for linear interpolation 
		int x_old;
		int y_old;

		firstTime = true;
		long v0 = m0 + x_min*mi_00;
		long v1 = m1;
		long v2 = m2 + x_min*mi_02;

		N_POINTS_INTERP_Y = N_POINTS_INTERP_X;
		N_POINTS_INTERP_Y_P1 = N_POINTS_INTERP_Y + 1;
		int nPtsInterpXOrg = N_POINTS_INTERP_X;	// stores the original value for future reference
		
		for (int y = y_min; y < y_max;) {
			int idx;
			int x_center, y_center, x_tmp;

			idx = cy;
			
			// if we are drawing one of the poles we disable interpolation between rows
			// to avoid artifacts
			if( (y + N_POINTS_INTERP_Y_P1 > N_ROW_NO_INTERP_MIN) &&
				(y < N_ROW_NO_INTERP_MAX) ) {
				N_POINTS_INTERP_Y = 0;
				if( N_POINTS_INTERP_X != nPtsInterpXOrg ) {
					N_POINTS_INTERP_X = nPtsInterpXOrg;
					firstTime = true;   // to recompute the arrays
				}
			}
			else {
				if( (y + N_POINTS_INTERP_Y_P1 < N_ROW_DOUBLE_INTERP_LOW) ||
					(y > N_ROW_DOUBLE_INTERP_HIGH) ) {
					// we are farther from the pole so we compute more rows with interpolation
					N_POINTS_INTERP_Y = nPtsInterpXOrg * 4;
					// since we are far from the poles we can interpolate between more pixels
					if( N_POINTS_INTERP_X != nPtsInterpXOrg * 4 ) {
						N_POINTS_INTERP_X = nPtsInterpXOrg * 4;
						firstTime = true;   // to recompute the arrays
					}
				} else {
					N_POINTS_INTERP_Y = N_POINTS_INTERP_X;
				}
			}
			N_POINTS_INTERP_Y_P1 = N_POINTS_INTERP_Y + 1;
			N_POINTS_INTERP_X_P1 = N_POINTS_INTERP_X;
//System.out.println( "y = " + y + "  " + N_POINTS_INTERP_Y );			
			
			if( !firstTime ) {
				// row_old[] = row_new[]
				for( itmp = 0; itmp < ROWS_INT_SIZE; itmp++ ) {
					row_xold[itmp] = row_xnew[itmp];
					row_yold[itmp] = row_ynew[itmp];
				}
				m0 += mi[1][0] * N_POINTS_INTERP_Y_P1;
				m1 += mi[1][1] * N_POINTS_INTERP_Y_P1;
				m2 += mi[1][2] * N_POINTS_INTERP_Y_P1;
			}

			// computes row_new[]
			v0 = m0 + x_min*mi_00;
			v1 = m1;
			v2 = m2 + x_min*mi_02;
			int irow = 0;	  // index in the row_*[] arrays
			int curx = x_min;  // x position of the current pixel in the viewer window
			row_xnew[irow] = PV_atan2_HR( (int) v0 >> MI_SHIFT, (int) v2 >> MI_SHIFT);
			row_ynew[irow] = PV_atan2_HR( (int) v1 >> MI_SHIFT, PV_sqrt( (int) Math.abs(v2 >> MI_SHIFT), (int) Math.abs(v0 >> MI_SHIFT)));
//if(firstTime){
//	System.out.println( "row_xnew[0], row_ynew[0]" + row_xnew[irow] + "  " + row_ynew[irow] );
//	System.out.println( "v0, v2 " + (int)(v0 >> MI_SHIFT) + "  " + (int)(v2 >> MI_SHIFT) );
//	System.out.println( PV_pi_HR );
//}
			while( curx <= x_max ) {
				v0 += mi_00 * N_POINTS_INTERP_X_P1;
				v2 += mi_02 * N_POINTS_INTERP_X_P1;
				
				curx += N_POINTS_INTERP_X_P1;
				irow++;
				row_xnew[irow] = PV_atan2_HR( (int) v0 >> MI_SHIFT, (int) v2 >> MI_SHIFT);
				row_ynew[irow] = PV_atan2_HR( (int) v1 >> MI_SHIFT, PV_sqrt( (int) Math.abs(v2 >> MI_SHIFT), (int) Math.abs(v0 >> MI_SHIFT)));
			}
			
			if( firstTime ) {
				// the first time only computes the first row and loops: that computation should be done before the loop
				// but I didn't like the idea of duplicating so much code so I arranged the code in such a way
				firstTime = false;
				continue;
			}

			// computes row_delta[], the difference between each row
			for( itmp = 0; itmp < ROWS_INT_SIZE; itmp++ ) {
				if ((row_xnew[itmp] < -pw_shifted_3) && (row_xold[itmp] > pw_shifted_3))
					row_xdelta[itmp] =
						(row_xnew[itmp] + pw_shifted - row_xold[itmp]) / (N_POINTS_INTERP_Y_P1);
				else {
					if ((row_xnew[itmp] > pw_shifted_3) && (row_xold[itmp] < -pw_shifted_3))
						row_xdelta[itmp] =
							(row_xnew[itmp] - pw_shifted - row_xold[itmp]) / (N_POINTS_INTERP_Y_P1);
					else
						row_xdelta[itmp] = (row_xnew[itmp] - row_xold[itmp]) / (N_POINTS_INTERP_Y_P1);
				}
				row_ydelta[itmp] = (row_ynew[itmp] - row_yold[itmp]) / N_POINTS_INTERP_Y_P1;
			}
			
			// row_current[] contains the values for the current row
			for( itmp = 0; itmp < ROWS_INT_SIZE; itmp++ ) {
				row_xcurrent[itmp] = row_xold[itmp];
				row_ycurrent[itmp] = row_yold[itmp];
			}
			
			// now draws a set of lines
			for( int ky = 0; ky < N_POINTS_INTERP_Y_P1; ky++) {
				
				if( y >= y_max ) break;
				
				irow = 0;
				x_old = row_xcurrent[irow];
				y_old = row_ycurrent[irow];
			
				for (int x = x_min + 1; x <= x_max;) {
					v0 += mi_00 * N_POINTS_INTERP_X_P1;
					v2 += mi_02 * N_POINTS_INTERP_X_P1;
					irow++;
					// determines the next point: it will interpolate between the new and old point
					int x_new = row_xcurrent[irow];
					int y_new = row_ycurrent[irow];
	
					int delta_x;
					if ((x_new < -pw_shifted_3) && (x_old > pw_shifted_3))
						delta_x =
							(x_new + pw_shifted - x_old) / (N_POINTS_INTERP_X_P1);
					else {
						if ((x_new > pw_shifted_3) && (x_old < -pw_shifted_3))
							delta_x =
								(x_new - pw_shifted - x_old) / (N_POINTS_INTERP_X_P1);
						else
							delta_x = (x_new - x_old) / (N_POINTS_INTERP_X_P1);
					}
					int delta_y = (y_new - y_old) / (N_POINTS_INTERP_X_P1);
	
					// now computes the intermediate points with linear interpolation
					int cur_x = x_old;
					int cur_y = y_old;
					for (int kk = 0; kk < N_POINTS_INTERP_X_P1; kk++) {
						if (x > x_max)
							break;
						if (cur_x >= pw_shifted_2)
							cur_x -= pw_shifted;
						if (cur_x < -pw_shifted_2)
							cur_x += pw_shifted;
						cur_y += delta_y;
						int dx = cur_x & 0xff;
						int dy = cur_y & 0xff;
						int xs = (cur_x >> 8) + sw2;
						int ys;
						int v_idx = v[idx];
	
						// used for nn interpolation
						ys_org = (cur_y >> 8) + sh2 - deltaYHorizonPosition;
						int[] pd_row = null;
						int row_index, col_index;
						if( nn ) {
							if( dy < 128 )
								row_index = ys_org;
							else
								row_index = ys_org + 1;
							if( row_index < 0 ) row_index = 0;
							if( row_index > miy ) row_index = miy;
							pd_row = pd[row_index];
						}
						if (v_idx == 0 ) {
							// draws the pixel
							xs_org = xs;
							if (v_idx == 0) {
								if(nn) {
									if( dx < 128 ) 
										col_index = xs_org; 
									else 
										col_index = xs_org + 1;
									if( col_index < 0 ) col_index = 0;
									if( col_index > mix ) col_index = mix;
									int pxl = pd_row[col_index];
									v[idx] = pxl | 0xff000000;
									hv[idx] = (byte) (pxl >> 24);
								}
								else {
									int px00;
									int px01;
									int px10;
									int px11;
									if ((ys = ys_org) == l24
										&& xs >= 0
										&& xs < mix) {
										px00 = pd_0[xs];
										px10 = pd_1[xs++];
										px01 = pd_0[xs];
										px11 = pd_1[xs];
									} else if (
										ys >= 0 && ys < miy && xs >= 0 && xs < mix) {
										l24 = ys;
										pd_0 = pd[ys];
										pd_1 = pd[ys + 1];
										px00 = pd_0[xs];
										px10 = pd_1[xs++];
										px01 = pd_0[xs];
										px11 = pd_1[xs];
									} else {
										if (ys < 0) {
											pd_0 = pd[0];
											l24 = 0;
										} else if (ys > miy) {
											pd_0 = pd[miy];
											l24 = miy;
										} else {
											pd_0 = pd[ys];
											l24 = ys;
										}
										if (++ys < 0)
											pd_1 = pd[0];
										else if (ys > miy)
											pd_1 = pd[miy];
										else
											pd_1 = pd[ys];
										if (xs < 0) {
											px00 = pd_0[mix];
											px10 = pd_1[mix];
										} else if (xs > mix) {
											px00 = pd_0[0];
											px10 = pd_1[0];
										} else {
											px00 = pd_0[xs];
											px10 = pd_1[xs];
										}
										if (++xs < 0) {
											px01 = pd_0[mix];
											px11 = pd_1[mix];
										} else if (xs > mix) {
											px01 = pd_0[0];
											px11 = pd_1[0];
										} else {
											px01 = pd_0[xs];
											px11 = pd_1[xs];
										}
									}
									if(lanczos2)
										v[idx] = lanczos2_interp_pixel( pd, pw, ph - deltaYHorizonPosition, xs_org, ys_org, dx, dy);
									else
										v[idx] = bil(px00, px01, px10, px11, dx, dy);
									hv[idx] = (byte) (px00 >> 24);
								}
							}
						}
						idx++;
						x++;
						cur_x += delta_x;
					}
					x_old = x_new;
					y_old = y_new;
				}

				// computes the next line using interpolation at the rows level
				for( itmp = 0; itmp < ROWS_INT_SIZE; itmp++ ) {
					row_xcurrent[itmp] += row_xdelta[itmp];
					row_ycurrent[itmp] += row_ydelta[itmp];
				}
				
				y++;
				cy += vw;
			}
		}
	}


	
	final double[] math_view2pano(
		int i,
		int j,
		int k,
		int l,
		int i1,
		int j1,
		double d,
		double d1,
		double d2) {
		double d8 = (double) i1 / 6.2831853071795862D;
		double d3 = (d2 * 2D * 3.1415926535897931D) / 360D;
		double d4 = (int) ((double) k / (2D * Math.tan(d3 / 2D)) + 0.5D);
		SetMatrix(
			(d1 * 2D * 3.1415926535897931D) / 360D,
			(d * 2D * 3.1415926535897931D) / 360D,
			mt,
			1);
		i -= k >> 1;
		j -= l >> 1;
		double d5 =
			mt[0][0] * (double) i + mt[1][0] * (double) j + mt[2][0] * d4;
		double d6 =
			mt[0][1] * (double) i + mt[1][1] * (double) j + mt[2][1] * d4;
		double d7 =
			mt[0][2] * (double) i + mt[1][2] * (double) j + mt[2][2] * d4;
		double ad[];
		(ad = new double[2])[0] = d8 * Math.atan2(d5, d7) + (double) i1 / 2D;
		ad[1] =
			d8 * Math.atan2(d6, Math.sqrt(d7 * d7 + d5 * d5))
				+ (double) j1 / 2D;
		return ad;
	}

	final int[] math_int_view2pano(
		int i,
		int j,
		int k,
		int l,
		int i1,
		int j1,
		double d,
		double d1,
		double d2) {
		double ad[];
		if ((ad = math_view2pano(i, j, k, l, i1, j1, d, d1, d2))[0] < 0.0D)
			ad[0] = 0.0D;
		if (ad[0] >= (double) i1)
			ad[0] = i1 - 1;
		if (ad[1] < 0.0D)
			ad[1] = 0.0D;
		if (ad[1] >= (double) j1)
			ad[1] = j1 - 1;
		int ai[];
		(ai = new int[2])[0] = (int) ad[0];
		ai[1] = (int) ad[1];
		return ai;
	}

	// calculate vertical field of view
  final double math_fovy(double hFov, int vw, int vh) {
		return (360.0 / Math.PI)
			* Math.atan(
				((double) vh / (double) vw)
					* Math.tan(((hFov / 2D) * Math.PI) / 180D));
	}

	static final boolean math_odd(int i) {
		int j = i / 2;
		return 2 * j != i;
	}

	void roi_allocate(int i) {
		try {
			roi_im = new String[i];
			roi_xp = new int[i];
			roi_yp = new int[i];
			roi_loaded = new boolean[i];
			roi_yaw = new double[i];
			roi_pitch = new double[i];
			roi_w = new int[i];
			roi_h = new int[i];
			roi_wdeg = new double[i];
			roi_hdeg = new double[i];
			numroi = i;
			return;
		} catch (Exception _ex) {
			numroi = 0;
		}
	}

	void roi_dispose() {
		for (int i = 0; i < numroi; i++)
			roi_im[i] = null;

		roi_im = null;
		roi_xp = null;
		roi_yp = null;
		roi_loaded = null;
		roi_yaw = null;
		roi_pitch = null;
		roi_w = null;
		roi_h = null;
		roi_wdeg = null;
		roi_hdeg = null;
		numroi = 0;
	}

	void ParseROILine(String s, int i) {
		int j = 0;
		int k = s.length();
		StringBuffer stringbuffer = new StringBuffer();
		roi_im[i] = null;
		roi_xp[i] = 0;
		roi_yp[i] = 0;
		roi_w[i] = 0;
		roi_h[i] = 0;
		roi_wdeg[i] = 0;
		roi_hdeg[i] = 0;
		roi_loaded[i] = false;
		while (j < k)
			switch (s.charAt(j++)) {
			case 120: // 'x'
				j = getNextWord(j, s, stringbuffer);
				roi_xp[i] = Integer.parseInt(stringbuffer.toString());
				break;

			case 121: // 'y'
				j = getNextWord(j, s, stringbuffer);
				roi_yp[i] = Integer.parseInt(stringbuffer.toString());
				break;

			case 105: // 'i'
				j = getNextWord(j, s, stringbuffer);
				roi_im[i] = stringbuffer.toString();
				break;

			case 'w': // image width
				j = getNextWord(j, s, stringbuffer);
				roi_w[i] = Integer.parseInt(stringbuffer.toString());
				break;

			case 'h': // image height
				j = getNextWord(j, s, stringbuffer);
				roi_h[i] = Integer.parseInt(stringbuffer.toString());
				break;
			}
	}

	void snd_init() {
		sounds = new Vector();
	}

	void snd_dispose() {
		sounds.removeAllElements();
	}

	public synchronized void PlaySound(int i) {
		if (i < sounds.size()
			&& sounds.elementAt(i) != null
			&& (sounds.elementAt(i) instanceof AudioClip))
			 ((AudioClip) sounds.elementAt(i)).play();
	}

	void SetupSounds() {
		for (int i = 0; i < sounds.size(); i++)
			if (sounds.elementAt(i) != null
				&& (sounds.elementAt(i) instanceof String)) {
				String s = (String) sounds.elementAt(i);
				try {
					URL url = new URL(getDocumentBase(), s);
					sounds.setElementAt(getAudioClip(url), i);
				} catch (Exception _ex) {
					try {
						URL url1 = Class.forName("ptviewer").getResource(s);
						sounds.setElementAt(getAudioClip(url1), i);
					} catch (Exception _ex2) {
						sounds.setElementAt(null, i);
					}
				}
			}

	}

	void app_init() {
		applets = new Hashtable();
		app_properties = new Vector();
	}

	public void startApplet(int i) {
		if (i < 0
			|| app_properties == null
			|| i >= app_properties.size()
			|| app_properties.elementAt(i) == null)
			return;
		if (applets.get(app_properties.elementAt(i)) != null)
			stopApplet(i);
		String s2;
		try {
			String s;
			s2 = myGetParameter((String) app_properties.elementAt(i), "code");
			s = s2;
			Applet applet =
				(Applet) Class
					.forName(s2.substring(0, s.lastIndexOf(".class")))
					.getConstructor(
						new Class[] {
							Class.forName("ptviewer"),
							java.lang.String.class })
					.newInstance(
						new Object[] { this, app_properties.elementAt(i)});
			applets.put(app_properties.elementAt(i), applet);
			applet.init();
			applet.start();
			return;
		} catch (Exception _ex) {
		}
		String s3;
		try {
			String s1;
			s3 = myGetParameter((String) app_properties.elementAt(i), "code");
			s1 = s3;
			Applet applet1 =
				(Applet) Class
					.forName(s3.substring(0, s1.lastIndexOf(".class")))
					.getConstructor(new Class[0])
					.newInstance(new Object[0]);
			applets.put(app_properties.elementAt(i), applet1);
			AppletStub appletstub =
				(AppletStub) Class
					.forName("ptstub")
					.getConstructor(
						new Class[] {
							Class.forName("ptviewer"),
							java.lang.String.class })
					.newInstance(
						new Object[] { this, app_properties.elementAt(i)});
			applet1.setStub(appletstub);
			applet1.init();
			applet1.start();
			return;
		} catch (Exception _ex) {
			return;
		}
	}

	public void stopApplet(int i) {
		if (i < 0
			|| app_properties == null
			|| i >= app_properties.size()
			|| app_properties.elementAt(i) == null)
			return;
		Applet applet;
		if ((applet = (Applet) applets.get(app_properties.elementAt(i)))
			!= null) {
			applet.stop();
			applets.remove(app_properties.elementAt(i));
		}
	}

	void stopApplets(int i) {
		for (int j = i; j < app_properties.size(); j++)
			stopApplet(j);

	}

	void hs_init() {
		hotspots = new Vector();
	}

	void hs_allocate(int i) {
		try {
			hs_xp = new double[i];
			hs_yp = new double[i];
			hs_up = new double[i];
			hs_vp = new double[i];
			hs_xv = new int[i];
			hs_yv = new int[i];
			hs_hc = new Color[i];
			hs_name = new String[i];
			hs_url = new String[i];
			hs_target = new String[i];
			hs_him = new Object[i];
			hs_visible = new boolean[i];
			hs_imode = new int[i];
			hs_mask = new String[i];
			hs_link = new int[i];
			numhs = i;
			return;
		} catch (Exception _ex) {
			numhs = 0;
		}
	}

	void hs_dispose() {
		for (int i = 0; i < numhs; i++) {
			if (hs_him[i] != null)
				hs_him[i] = null;
			hs_hc[i] = null;
			hs_name[i] = null;
			hs_url[i] = null;
			hs_target[i] = null;
			hs_mask[i] = null;
		}

		numhs = 0;
		hotspots.removeAllElements();
		hs_xp = null;
		hs_yp = null;
		hs_up = null;
		hs_vp = null;
		hs_xv = null;
		hs_yv = null;
		hs_hc = null;
		hs_name = null;
		hs_url = null;
		hs_him = null;
		hs_visible = null;
		hs_target = null;
		hs_mask = null;
		hs_imode = null;
		hs_link = null;
		hs_image = null;
	}

	void ParseHotspotLine(String s, int i) {
		int j = 0;
		int k = s.length();
		StringBuffer stringbuffer = new StringBuffer();
		hs_xp[i] = 0.0D;
		hs_yp[i] = 0.0D;
		hs_up[i] = -200D;
		hs_vp[i] = -200D;
		hs_xv[i] = 0;
		hs_yv[i] = 0;
		hs_hc[i] = null;
		hs_name[i] = null;
		hs_url[i] = null;
		hs_target[i] = null;
		hs_him[i] = null;
		hs_visible[i] = false;
		hs_imode[i] = 0;
		hs_mask[i] = null;
		hs_link[i] = -1;
		while (j < k)
			switch (s.charAt(j++)) {
				case 120 : // 'x'
					j = getNextWord(j, s, stringbuffer);
					hs_xp[i] =
						Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 88 : // 'X'
					j = getNextWord(j, s, stringbuffer);
					hs_xp[i] =
						-Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 121 : // 'y'
					j = getNextWord(j, s, stringbuffer);
					hs_yp[i] =
						Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 89 : // 'Y'
					j = getNextWord(j, s, stringbuffer);
					hs_yp[i] =
						-Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 97 : // 'a'
					j = getNextWord(j, s, stringbuffer);
					hs_up[i] =
						Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 65 : // 'A'
					j = getNextWord(j, s, stringbuffer);
					hs_up[i] =
						-Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 98 : // 'b'
					j = getNextWord(j, s, stringbuffer);
					hs_vp[i] =
						Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 66 : // 'B'
					j = getNextWord(j, s, stringbuffer);
					hs_vp[i] =
						-Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 99 : // 'c'
					j = getNextWord(j, s, stringbuffer);
					hs_hc[i] =
						new Color(
							Integer.parseInt(stringbuffer.toString(), 16));
					break;

				case 110 : // 'n'
					j = getNextWord(j, s, stringbuffer);
					hs_name[i] = stringbuffer.toString();
					break;

				case 109 : // 'm'
					j = getNextWord(j, s, stringbuffer);
					hs_mask[i] = stringbuffer.toString();
					break;

				case 112 : // 'p'
					hs_imode[i] |= 1;
					break;

				case 113 : // 'q'
					hs_imode[i] |= 2;
					break;

				case 119 : // 'w'
					hs_imode[i] |= 4;
					break;

				case 101 : // 'e'
					hs_imode[i] |= 0x10;
					break;

				case 117 : // 'u'
					j = getNextWord(j, s, stringbuffer);
					hs_url[i] = stringbuffer.toString();
					break;

				case 105 : // 'i'
					j = getNextWord(j, s, stringbuffer);
					hs_him[i] = stringbuffer.toString();
					break;

				case 116 : // 't'
					j = getNextWord(j, s, stringbuffer);
					hs_target[i] = stringbuffer.toString();
					break;
			}
	}

	void hs_read() {
		if (hotspots.size() != 0) {
			hs_allocate(hotspots.size());
			for (int i = 0; i < numhs; i++)
				ParseHotspotLine((String) hotspots.elementAt(i), i);

			hs_setLinkedHotspots();
		}
	}

	//    void hs_setup(int ai[][])
	//    {
	//        int i;
	//        int j;
	//        if(ai == null)
	//            break MISSING_BLOCK_LABEL_1322;
	//        i = ai.length;
	//        j = ai[0].length;
	//        hs_read();
	//        for(int k = 0; k < numhs; k++)
	//        {
	//            String s;
	//            if(hs_him[k] != null && (hs_imode[k] & 0x10) == 0 && !(s = (String)hs_him[k]).startsWith("ptviewer:") && !s.startsWith("javascript:"))
	//                hs_him[k] = loadImage(s);
	//        }
	//
	//        hs_rel2abs(j, i);
	//        if(hs_image != null)
	//            hs_image = loadImage((String)hs_image);
	//        if(hs_image == null || !(hs_image instanceof Image) || j != ((Image)hs_image).getWidth(null) || i != ((Image)hs_image).getHeight(null)) goto _L2; else goto _L1
	//_L1:
	//        ptImageToAlpha(ai, (Image)hs_image);
	//          goto _L3
	//_L2:
	//        int l = 0;
	//          goto _L4
	//_L7:
	//        Image image;
	//        Image image2;
	//        if(hs_link[l] != -1)
	//            continue; /* Loop/switch isn't completed */
	//        if(hs_up[l] != -200D && hs_vp[l] != -200D)
	//        {
	//            SetPAlpha((int)hs_xp[l], (int)hs_yp[l], (int)hs_up[l], (int)hs_vp[l], l, ai);
	//            if(hs_up[l] >= hs_xp[l])
	//            {
	//                hs_xp[l] += (hs_up[l] - hs_xp[l]) / 2D;
	//                hs_up[l] = hs_up[l] - hs_xp[l];
	//            } else
	//            {
	//                hs_xp[l] += ((hs_up[l] + (double)j) - hs_xp[l]) / 2D;
	//                hs_up[l] = (hs_up[l] + (double)j) - hs_xp[l];
	//            }
	//            hs_yp[l] = (hs_yp[l] + hs_vp[l]) / 2D;
	//            hs_vp[l] = Math.abs(hs_yp[l] - hs_vp[l]);
	//            continue; /* Loop/switch isn't completed */
	//        }
	//        if((hs_imode[l] & 4) > 0 && hs_him[l] != null && (hs_him[l] instanceof Image) && hs_mask[l] == null)
	//        {
	//            hs_up[l] = ((Image)hs_him[l]).getWidth(null);
	//            hs_vp[l] = ((Image)hs_him[l]).getHeight(null);
	//            SetPAlpha((int)(hs_xp[l] - hs_up[l] / 2D), (int)(hs_yp[l] - hs_vp[l] / 2D), (int)(hs_xp[l] + hs_up[l] / 2D), (int)(hs_yp[l] + hs_vp[l] / 2D), l, ai);
	//            continue; /* Loop/switch isn't completed */
	//        }
	//        if(hs_mask[l] == null)
	//            continue; /* Loop/switch isn't completed */
	//        image2 = loadImage(hs_mask[l]);
	//        image2;
	//        image = image2;
	//        JVM INSTR ifnull 928;
	//           goto _L5 _L6
	//_L5:
	//        break MISSING_BLOCK_LABEL_665;
	//_L6:
	//        continue; /* Loop/switch isn't completed */
	//        int ai1[] = new int[image.getWidth(null) * image.getHeight(null)];
	//        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, image.getWidth(null), image.getHeight(null), ai1, 0, image.getWidth(null));
	//        try
	//        {
	//            pixelgrabber.grabPixels();
	//        }
	//        catch(InterruptedException _ex)
	//        {
	//            continue; /* Loop/switch isn't completed */
	//        }
	//        int j2 = (int)hs_yp[l];
	//        int j3 = (l << 24) + 0xffffff;
	//        int l3 = 0;
	//        for(int l1 = 0; l1 < image.getHeight(null) && j2 < i; j2++)
	//        {
	//            int i2 = l1 * image.getWidth(null);
	//            int k1 = 0;
	//            for(int l2 = (int)hs_xp[l]; k1 < image.getWidth(null) && l2 < j; l2++)
	//            {
	//                if((ai1[i2 + k1] & 0xffffff) == 0xffffff)
	//                {
	//                    ai[j2][l2] &= j3;
	//                    l3++;
	//                }
	//                k1++;
	//            }
	//
	//            l1++;
	//        }
	//
	//        hs_yp[l] += image.getHeight(null) >> 1;
	//        hs_xp[l] += image.getWidth(null) >> 1;
	//        hs_up[l] = image.getWidth(null);
	//        hs_vp[l] = image.getHeight(null);
	//        l++;
	//_L4:
	//        if(l < numhs && l < 255) goto _L7; else goto _L3
	//_L3:
	//        int j4;
	//        for(int i1 = 0; i1 < numhs; i1++)
	//            if(hs_link[i1] != -1)
	//            {
	//                hs_xp[i1] = hs_xp[hs_link[i1]];
	//                hs_yp[i1] = hs_yp[hs_link[i1]];
	//                hs_up[i1] = hs_up[hs_link[i1]];
	//                hs_vp[i1] = hs_vp[hs_link[i1]];
	//            }
	//
	//        for(int j1 = 0; j1 < numhs; j1++)
	//        {
	//            if((hs_imode[j1] & 4) <= 0 || hs_him[j1] == null || !(hs_him[j1] instanceof Image))
	//                continue;
	//            Image image1;
	//            int k2 = (image1 = (Image)hs_him[j1]).getWidth(null);
	//            int i3 = image1.getHeight(null);
	//            int k3 = (int)hs_xp[j1] - (k2 >> 1);
	//            int i4 = (int)hs_yp[j1] - (i3 >> 1);
	//            if(k3 >= 0 && i4 >= 0 && k2 + k3 <= j && i3 + i4 <= i)
	//            {
	//                j4 = k2 * i3;
	//                int ai2[] = new int[j4 + j4];
	//                PixelGrabber pixelgrabber1 = new PixelGrabber(image1, 0, 0, k2, i3, ai2, 0, k2);
	//                try
	//                {
	//                    pixelgrabber1.grabPixels();
	//                }
	//                catch(InterruptedException _ex)
	//                {
	//                    continue;
	//                }
	//                im_extractRect(ai, k3, i4, ai2, k2, 0, i3, k2, i3);
	//                hs_him[j1] = ai2;
	//                hs_up[j1] = k2;
	//                hs_vp[j1] = i3;
	//            } else
	//            {
	//                System.out.println("Image for Hotspot No " + j1 + " outside main panorama");
	//            }
	//        }
	//
	//    }

	// from version 2.1
	void hs_setup(int[][] pd) {
		if (pd == null) {
			return;
		}
		int ph = pd.length, pw = pd[0].length;
		PixelGrabber pg;
		int i, x, y, cy;

		hs_read();
		
		int[] tdata;

		// Load Hotspotimages, if not done

		for (i = 0; i < numhs; i++) {
			if (hs_him[i] != null && ((hs_imode[i] & IMODE_TEXT) == 0)) {
				String s = (String) hs_him[i];

				if (!(s.startsWith("ptviewer:")
					|| s.startsWith("javascript:"))) {
					hs_him[i] = loadImage(s);
				}
			}
		}

		hs_rel2abs(pw, ph);

		// Process global hotspot image

		if (hs_image != null) {
			hs_image = loadImage((String) hs_image);
		}
		if (hs_image != null
			&& hs_image instanceof Image
			&& pw == ((Image) hs_image).getWidth(null)
			&& ph == ((Image) hs_image).getHeight(null)) {
			ptImageToAlpha(pd, (Image) hs_image);
		} else {
			// Set hotspot masks

			for (i = 0; i < numhs && i < 255; i++) { // only 255 indices
				if (hs_link[i] == -1) { // Linked Hotspots don't get masks
					if (hs_up[i] != NO_UV && hs_vp[i] != NO_UV) {
						SetPAlpha(
							(int) hs_xp[i],
							(int) hs_yp[i],
							(int) hs_up[i],
							(int) hs_vp[i],
							i,
							pd);
						if (hs_up[i] >= hs_xp[i]) {
							hs_xp[i] += (hs_up[i] - hs_xp[i]) / 2;
							hs_up[i] = hs_up[i] - hs_xp[i];
						} else {
							hs_xp[i] += (hs_up[i] + pw - hs_xp[i]) / 2;
							hs_up[i] = hs_up[i] + pw - hs_xp[i];
						}
						hs_yp[i] = (hs_yp[i] + hs_vp[i]) / 2;
						hs_vp[i] = Math.abs(hs_yp[i] - hs_vp[i]);
					} else if (
						(hs_imode[i] & IMODE_WARP) > 0
							&& (hs_him[i] != null)
							&& hs_him[i] instanceof Image
							&& hs_mask[i] == null) { // warped image without mask
						hs_up[i] = ((Image) hs_him[i]).getWidth(null);
						hs_vp[i] = ((Image) hs_him[i]).getHeight(null);
						SetPAlpha(
							(int) (hs_xp[i] - hs_up[i] / 2.0),
							(int) (hs_yp[i] - hs_vp[i] / 2.0),
							(int) (hs_xp[i] + hs_up[i] / 2.0),
							(int) (hs_yp[i] + hs_vp[i] / 2.0),
							i,
							pd);
					} else if (hs_mask[i] != null) {

						Image mim = loadImage(hs_mask[i]);
						if (mim != null) {
							tdata =
								new int[mim.getWidth(null)
									* mim.getHeight(null)];
							pg =
								new PixelGrabber(
									mim,
									0,
									0,
									mim.getWidth(null),
									mim.getHeight(null),
									tdata,
									0,
									mim.getWidth(null));
							try {
								pg.grabPixels();
							} catch (InterruptedException e) {
								continue;
							}

							int hs_y = (int) hs_yp[i], hs_x = (int) hs_xp[i];
							int hmask = (i << 24) + 0x00ffffff;
							int k = 0;

							for (y = 0;
								y < mim.getHeight(null) && hs_y < ph;
								y++, hs_y++) {
								cy = y * mim.getWidth(null);
								for (x = 0, hs_x = (int) hs_xp[i];
									x < mim.getWidth(null) && hs_x < pw;
									x++, hs_x++) {
									if ((tdata[cy + x] & 0x00ffffff)
										== 0x00ffffff) {
										// inside mask
										pd[hs_y][hs_x] &= hmask;
										k++;
									}
								}
							}
							hs_yp[i] += mim.getHeight(null) / 2;
							hs_xp[i] += mim.getWidth(null) / 2;
							hs_up[i] = mim.getWidth(null); // width
							hs_vp[i] = mim.getHeight(null); // height
							mim = null;
							tdata = null;
						}
					}
				}
			}
		}

		for (i = 0; i < numhs; i++) {
			if (hs_link[i] != -1) {
				hs_xp[i] = hs_xp[hs_link[i]];
				hs_yp[i] = hs_yp[hs_link[i]];
				hs_up[i] = hs_up[hs_link[i]];
				hs_vp[i] = hs_vp[hs_link[i]];
			}
		}

		// Get and set pixel data for warped hotspots

		for (i = 0; i < numhs; i++) {
			if ((hs_imode[i] & IMODE_WARP) > 0 && hs_him[i] != null) {
				if (hs_him[i] instanceof Image) {
					Image p = (Image) hs_him[i];

					int w = p.getWidth(null);
					int h = p.getHeight(null);
					int xp = (int) hs_xp[i] - w / 2;
					int yp = (int) hs_yp[i] - h / 2;

					// System.out.println( xp + " " +yp + " " +w+" "+h);

					if (xp >= 0 && yp >= 0 && w + xp <= pw && h + yp <= ph) {
						int[] buf = new int[w * h * 2];
						pg = new PixelGrabber(p, 0, 0, w, h, buf, 0, w);
						try {
							pg.grabPixels();
						} catch (InterruptedException e) {
							continue;
						}

						im_extractRect(pd, xp, yp, buf, w, 0, h, w, h);
						hs_him[i] = buf;
						hs_up[i] = w;
						hs_vp[i] = h;
					} else {
						System.out.println(
							"Image for Hotspot No "
								+ i
								+ " outside main panorama");
					}
				}

			}
		}

	}

	boolean hs_drawWarpedImages(int ai[][], int i, boolean flag) {
		boolean flag1 = false;
		if (ai == null)
			return false;
		for (int j = 0; j < numhs; j++)
			if ((hs_imode[j] & 4) > 0
				&& hs_him[j] != null
				&& (hs_him[j] instanceof int[])) {
				int k = (int) hs_up[j];
				int l = (int) hs_vp[j];
				int i1 = (int) hs_xp[j] - (k >> 1);
				int j1 = (int) hs_yp[j] - (l >> 1);
				if (flag
					|| (hs_imode[j] & 2) > 0
					|| j == i
					&& (hs_imode[j] & 1) > 0
					|| i >= 0
					&& hs_link[j] == i
					&& (hs_imode[j] & 1) > 0) {
					if ((hs_imode[j] & 8) == 0) {
						im_insertRect(
							ai,
							i1,
							j1,
							(int[]) hs_him[j],
							k,
							0,
							0,
							k,
							l);
						hs_imode[j] |= 8;
						flag1 = true;
					}
				} else if ((hs_imode[j] & 8) > 0) {
					im_insertRect(ai, i1, j1, (int[]) hs_him[j], k, 0, l, k, l);
					hs_imode[j] &= -9;
					flag1 = true;
				}
			}

		return flag1;
	}

	void hs_rel2abs(int i, int j) {
		for (int k = 0; k < numhs; k++) {
			if (hs_xp[k] < 0.0D) {
				hs_xp[k] = (-hs_xp[k] * (double) i) / 100D;
				if (hs_xp[k] >= (double) i)
					hs_xp[k] = i - 1;
			}
			if (hs_yp[k] < 0.0D) {
				hs_yp[k] = (-hs_yp[k] * (double) j) / 100D;
				if (hs_yp[k] >= (double) j)
					hs_yp[k] = j - 1;
			}
			if (hs_up[k] < 0.0D && hs_up[k] != -200D) {
				hs_up[k] = (-hs_up[k] * (double) i) / 100D;
				if (hs_up[k] >= (double) i)
					hs_up[k] = i - 1;
			}
			if (hs_vp[k] < 0.0D && hs_vp[k] != -200D) {
				hs_vp[k] = (-hs_vp[k] * (double) j) / 100D;
				if (hs_vp[k] >= (double) j)
					hs_vp[k] = j - 1;
			}
		}

	}

	void hs_draw(
		Graphics g,
		int off_x,
		int off_y,
		int width,
		int height,
		int chs,
		boolean shs) {
		for (int i = 0; i < numhs; i++)
			if (hs_visible[i]
				&& (shs
					|| (hs_imode[i] & 2) > 0
					|| i == chs
					&& (hs_imode[i] & 1) > 0
					|| chs >= 0
					&& hs_link[i] == chs
					&& (hs_imode[i] & 1) > 0))
				if (hs_him[i] == null) {
					if (hs_hc[i] == null)
						g.setColor(Color.red);
					else
						g.setColor(hs_hc[i]);
					g.drawOval(
						(hs_xv[i] - 10) + off_x,
						(hs_yv[i] - 10) + off_y,
						20,
						20);
					g.fillOval(
						(hs_xv[i] - 5) + off_x,
						(hs_yv[i] - 5) + off_y,
						10,
						10);
				} else if (hs_him[i] instanceof Image) {
					Image image = (Image) hs_him[i];
					g.drawImage(
						image,
						(hs_xv[i] - (image.getWidth(null) >> 1)) + off_x,
						(hs_yv[i] - (image.getHeight(null) >> 1)) + off_y,
						this);
				} else if (
					(hs_imode[i] & 0x10) > 0
						&& (hs_him[i] instanceof String)) {
					String s = (String) hs_him[i];
					Dimension dimension = string_textWindowSize(g, s);
					if (hs_xv[i] >= 0
						&& hs_xv[i] < width
						&& hs_yv[i] >= 0
						&& hs_yv[i] < height) {
						int k1 = 0;
						int l1 = 0;
						byte byte0 = 0;
						if (hs_xv[i] + dimension.width < width) {
							if (hs_yv[i] - dimension.height > 0) {
								k1 = hs_xv[i];
								l1 = hs_yv[i] - dimension.height;
								byte0 = 1;
							} else if (hs_yv[i] + dimension.height < width) {
								k1 = hs_xv[i];
								l1 = hs_yv[i];
								byte0 = 2;
							}
						} else if (hs_xv[i] - dimension.width >= 0)
							if (hs_yv[i] - dimension.height > 0) {
								k1 = hs_xv[i] - dimension.width;
								l1 = hs_yv[i] - dimension.height;
								byte0 = 3;
							} else if (hs_yv[i] + dimension.height < width) {
								k1 = hs_xv[i] - dimension.width;
								l1 = hs_yv[i];
								byte0 = 4;
							}
						if (byte0 != 0)
							string_drawTextWindow(
								g,
								k1 + off_x,
								l1 + off_y,
								dimension,
								hs_hc[i],
								s,
								byte0);
					}
				}

	}

	final void hs_exec_popup(int i) {
		for (int j = 0; j < numhs; j++)
			if (hs_visible[j]
				&& hs_him[j] != null
				&& (j == i || i >= 0 && hs_link[j] == i)
				&& (hs_him[j] instanceof String)
				&& (hs_imode[j] & 0x10) == 0)
				JumpToLink((String) hs_him[j], null);

	}

	final void hs_setLinkedHotspots() {
		for (int i = 0; i < numhs; i++) {
			for (int j = i + 1; j < numhs; j++)
				if (hs_xp[i] == hs_xp[j]
					&& hs_yp[i] == hs_yp[j]
					&& hs_link[i] == -1)
					hs_link[j] = i;

		}

	}

	final void hs_setCoordinates(
		int vw,
		int vh,
		int pw,
		int ph,
		double pan,
		double tilt,
		double fov) {
			
		// deltaY is the height of missing upper part of the panorama if this pano is not fully spherical
		//   it is == 0 if the tilt angle goes from -90 to + 90 
		int deltaY = (pw/2 - ph)/2;
		if( deltaY < 0 ) deltaY = 0; 

		int sw2 = pw >> 1;
		int sh2 = (ph >> 1) + deltaY;
		double mt[][] = new double[3][3];
		double a = (fov * 2D * 3.1415926535897931D) / 360D; // field of view in rad
		double p = (double) vw / (2D * Math.tan(a / 2D));
		SetMatrix(
			(-tilt * 2D * 3.1415926535897931D) / 360D,
			(-pan * 2D * 3.1415926535897931D) / 360D,
			mt,
			0);
		double v0;
		for (int i = 0; i < numhs; i++) {
			double x = hs_xp[i] - (double) sw2;
			double y = (double) (pheight + 2*deltaY) - (hs_yp[i] + deltaY + deltaYHorizonPosition/2 - (double) sh2);
			double theta = (x / (double) sw2) * 3.1415926535897931D;
			double phi = ((y / (double) sh2) * 3.1415926535897931D) / 2D;
			double v2;
			if (Math.abs(theta) > 1.5707963267948966D)
				v2 = 1.0D;
			else
				v2 = -1D;
			double d5;
			v0 = v2 * Math.tan(theta);
			d5 = v0;
			double v1 = Math.sqrt(v0 * v0 + v2 * v2) * Math.tan(phi);
			x = mt[0][0] * d5 + mt[1][0] * v1 + mt[2][0] * v2;
			y = mt[0][1] * d5 + mt[1][1] * v1 + mt[2][1] * v2;
			double z = mt[0][2] * d5 + mt[1][2] * v1 + mt[2][2] * v2;
			hs_xv[i] = (int) ((x * p) / z + (double) vw / 2D);
			hs_yv[i] = (int) ((y * p) / z + (double) vh / 2D);
			int hs_vis_hor = 12;
			int hs_vis_ver	 = 12;
			if (hs_him[i] != null && (hs_him[i] instanceof Image)) {
				hs_vis_hor = ((Image) hs_him[i]).getWidth(null) >> 1;
				hs_vis_ver = ((Image) hs_him[i]).getHeight(null) >> 1;
			} else if (
				hs_him[i] != null
					&& (hs_him[i] instanceof String)
					&& (hs_imode[i] & 0x10) > 0) {
				hs_vis_hor = 100;
				hs_vis_ver = 100;
			} else if (hs_up[i] != -200D && hs_vp[i] != -200D) {
				hs_vis_hor = 100;
				hs_vis_ver = 100;
			}
			if (hs_xv[i] >= -hs_vis_hor
				&& hs_xv[i] < vwidth + hs_vis_hor
				&& hs_yv[i] >= -hs_vis_ver
				&& hs_yv[i] < vheight + hs_vis_ver
				&& z < 0.0D)
				hs_visible[i] = true;
			else
				hs_visible[i] = false;
		}

	}

	static final boolean debug = false;
	static final double HFOV_MIN = 10.5D;
	static final double HFOV_MAX = 165D;
	static final long TIME_PER_FRAME = 10L;
	static final long ETERNITY = 0x5f5e100L;
	int quality;
	boolean inited;
	Color bgcolor;
	long waittime;
	boolean WaitDisplayed;
	Image view;
	Image dwait;
	Image frame;
	Image offImage;
	Graphics offGraphics;
	int offwidth;
	int offheight;
	MemoryImageSource source;
	int awidth;
	int aheight;
	public int vwidth;
	public int vheight;
	boolean vset;
	int vx;
	int vy;
	int pwidth;
	int pheight;
	int vdata[];
	byte hs_vdata[];
	int pdata[][];
	boolean show_pdata;
	boolean ready;
	boolean hsready;
	boolean PanoIsLoaded;
	boolean fatal;
	boolean mouseInWindow;
	boolean mouseInViewer;
	boolean panning;
	boolean keyPanning;		// true if we are panning or zooming with the keyboard
	public boolean dirty;
	boolean showhs;
	boolean showCoordinates;
	int oldx;
	int oldy;
	int newx;
	int newy;
	int ptcursor;
	public double yaw;
	public double hfov;
	public double hfov_min;
	public double hfov_max;
	public double pitch;
	public double pitch_max;
	public double pitch_min;
	
	// original values kept as a reference
	public double pitch_max_org;
	public double pitch_min_org;
	
	public double yaw_max;
	public double yaw_min;
	double MASS;
	double oldspeedx;
	double oldspeedy;
	double autopan;
	double autotilt;
	
	// time interval in milliseconds between frames when autopanning
	// if == 0 it is ignored
	double autopanFrameTime;

	// E.Gigi - 2005.06.12
	//   number of 360 degree turns before auto-panning stops (fractions are allowed)
	//   ignored if 0
	double autoNumTurns;
	
	double zoom;
	public double pan_steps;
	String filename;
	String inits;
	String MouseOverHS;
	String GetView;
	int click_x;
	int click_y;
	long frames;
	long lastframe;
	long ptimer;
	Thread loadPano;
	Thread ptviewerScript;
	String PTScript;
	String PTViewer_Properties;
	boolean loadAllRoi;
	int CurrentPano;
	Hashtable sender;
	Thread preloadthread;
	String preload;
	String order;
	boolean antialias;
	Vector scaledPanos;
	double max_oversampling;
	int im_maxarray;
	int grid_bgcolor;
	int grid_fgcolor;
	Hashtable file_Cache;
	boolean file_cachefiles;
	Color pb_color;
	int pb_x;
	int pb_y;
	int pb_width;
	int pb_height;
	int percent[];
	int numshs;
	int curshs;
	int shs_x1[];
	int shs_x2[];
	int shs_y1[];
	int shs_y2[];
	String shs_url[];
	String shs_target[];
	Object shs_him[];
	boolean shs_active[];
	int shs_imode[];		// 0 - normal, 1 - popup, 2 - always visible
	Vector shotspots;
	int atan_LU_HR[];
	int sqrt_LU[];
	double atan_LU[];
	int PV_atan0_HR;
	int PV_pi_HR;

//	static final int NATAN = 4096;
//	static final int NSQRT = 4096;
//	static final int NSQRT_SHIFT = 12;	// NSQRT = 2^NSQRT_SHIFT
//	static final int NATAN = 16384;
//	static final int NSQRT = 16384;
//	static final int NSQRT_SHIFT = 14;	// NSQRT = 2^NSQRT_SHIFT
	static final int NATAN = 65536;
	static final int NSQRT = 65536;
	static final int NSQRT_SHIFT = 16;	// NSQRT = 2^NSQRT_SHIFT
	
	// multiplier and corresponding shift
	// used to increase the resolution of the values
	//   in the integer transformation matrix mi[][]
//	static final int MI_MULT = 64;
//	static final int MI_SHIFT = 6;
	static final int MI_MULT = 4096;
	static final int MI_SHIFT = 12;
	
	// a message to be written in the browser's status bar
	protected String statusMessage; 
	
	// true if we can use an accelerated VolatileImage
	private boolean useVolatileImage;
	// back buffer used to draw the panorama frames
	public Image backBuffer;
	// vimage object used to handle accelerated graphics
	vimage vImgObj;
	
	// if true shows the toolbar
	boolean showToolbar;
	
	// image used to draw the toolbar
	String tlbImageName;
	
	// toolbar object
	Object tlbObj;
	boolean onlyPaintToolbar = false;
	
	// original value if view_height set as a parameter
	// needed because the toolbar changes the value of vheight
	int org_vheight;
	
	// false if we don't want img loading feedback
	protected boolean imgLoadFeedback;
	
	// link to an URL to open in case of out of memory error while loading the pano
	String outOfMemoryURL;
	
	private double mt[][];
	private long mi[][];
	private double dist_e;
	int numroi;
	String roi_im[];
	int roi_xp[];
	int roi_yp[];
	boolean roi_loaded[];

	// true if we are using a custom format pano file (see PTVFile.java)
	boolean usingCustomFile;
	// object that handles the custom file format
	PTVFile ptvf;
	
	// yaw angle of the center of each ROI image
	// used only if the applet is created with the "dynLoadROIs" parameter
	double roi_yaw[];
	// pitch angle of the center of each ROI image
	double roi_pitch[];
	// size of each ROI image
	int roi_w[], roi_h[];
	// width in degrees of each ROI image
	double roi_wdeg[];
	// height in degrees of each ROI image
	double roi_hdeg[];
	boolean dynLoadROIs;	// true if the applet is created with the "dynLoadROIs" parameter
	boolean loadingROI;	// true while we are loading a ROI - used to avoid a lot of unnecessary paints
	// set to true at the end of paint(). Used for synchronization while dynamically loading ROIs
	boolean paintDone = true;
	// last position of the mouse cursor
	int lastMouseX, lastMouseY;
	// changes sensitivity to mouse panning: 
	// default 1.0 : no change
	// values < 1 : slower panning
	// values > 1 : faster panning
	double mouseSensitivity = 1.0;
	// only used for quality=6, default = 1.0
	// values > 1 will require a larger mouse movement to switch from bil to nn
	// values < 1 will require a smaller mouse movement to switch from bil to nn
	double mouseQ6Threshold = 1.0;
	
	// if set to true the next paint will be forced to use the bilinear interpolator 
	// instead of Lanczos2. Used to speed up dynLoading ROIs
	boolean forceBilIntepolator = false;
	
	// if false do not show the hotspots' description in the status bar
	boolean hsShowDescrInStatusBar;
	
	boolean hsEnableVisibleOnly;	// if true hotspots will be enabled only if visible
	boolean shsEnableVisibleOnly;	// if true static hotspots will be enabled only if visible
	boolean shsStopAutoPanOnClick;	// if false the autopan will not stop when clicking a static hotspot
	
	boolean popupPanning;			// if true hotspots will pop up also when panning with the mouse
	
	long lastPanningPaintTime = -1;	// time taken by the last paint while panning
	double mousePanTime = 0;		// minimum time for a full revolution, ignored if == 0
	
	// vertical position of the horizon, computed as the distance of the horizon from the
	// top of the image as a % value
	int horizonPosition;
	
	// this variable is <> 0 if horizonPosition is <> 50 (default value)
	// it is the number of padding pixels that we should add to the pano image
	// in order to have the horizon in the middle
	// it is > 0 if we need to add space at the top of the image
	// it is < 0 if we need to add space at the bottom of the image
	int deltaYHorizonPosition;
	
	// true if we want to use the applet in authoring mode
	// it enables the "o" key
	boolean authoringMode;
	
	Vector sounds;
	Hashtable applets;
	Vector app_properties;
	Vector hotspots;
	int numhs;
	int curhs;
	Object hs_image;
	double hs_xp[];
	double hs_yp[];
	double hs_up[];
	double hs_vp[];
	int hs_xv[];
	int hs_yv[];
	Color hs_hc[];
	String hs_name[];
	String hs_url[];
	String hs_target[];
	Object hs_him[];
	String hs_mask[];
	boolean hs_visible[];
	int hs_imode[];
	int hs_link[];
	static final double NO_UV = -200D;
	static final int HSIZE = 12;
	static final int IMODE_NORMAL = 0;
	static final int IMODE_POPUP = 1;
	static final int IMODE_ALWAYS = 2;
	static final int IMODE_WARP = 4;
	static final int IMODE_WHS = 8;
	static final int IMODE_TEXT = 16;

	/////////////////////////////////////////////
	// start of Lanczos2 interpolation stuff
	/////////////////////////////////////////////

	// number of subdivisions of the x-axis unity
	//  static int UNIT_XSAMPLES = 1024;
	static int UNIT_XSAMPLES = 256;
	// number of subdivisions of the y-axis unity
	static int UNIT_YSAMPLES = 1024;
	// number of bits to shift to return to the 0-255 range
	// corresponds to the division by (UNIT_YSAMPLES*UNIT_YSAMPLES)
	static int SHIFT_Y = 20;

	// maximum number of weights used to interpolate one pixel
	static int MAX_WEIGHTS = 20;

	// maximum value for the quality parameter
	static int MAX_QUALITY = 6;

	// lookup table
	static int lanczos2_LU[];
	// lookup table for the interpolation weights
	static int lanczos2_weights_LU[][];

	// number of points on each side for an enlarged image
	static int lanczos2_n_points_base = 2;

	// number of points actually used on each side, changes with view_scale
	int lanczos2_n_points;

	// temporary arrays used during interpolation
	int aR[], aG[], aB[];

	// current wiewing scale:
	// < 1: pano image is reduced
	// > 1: pano image is enlarged
	double view_scale;

	double sinc(double x) {
		double PI = 3.14159265358979;

		if (x == 0.0)
			return 1.0;
		else
			return Math.sin(PI * x) / (PI * x);
	}

	void lanczos2_init() {
		double x, dx;
		int k;

		// sets up the lookup table

		lanczos2_LU = new int[UNIT_XSAMPLES * 2 + 1];
		x = 0.0;
		dx = 1.0 / UNIT_XSAMPLES;
		for (k = 0; k <= UNIT_XSAMPLES * 2; k++) {
			lanczos2_LU[k] =
				(int) (sinc(x) * sinc(x / 2.0) * UNIT_YSAMPLES + 0.5);
			x += dx;
		}

		// allocates the weights lookup table
		// the values are set up by lanczos2_compute_weights()
		lanczos2_weights_LU = new int[UNIT_XSAMPLES + 1][MAX_WEIGHTS];

		// allocates temporary buffers
		aR = new int[MAX_WEIGHTS];
		aG = new int[MAX_WEIGHTS];
		aB = new int[MAX_WEIGHTS];
	}

	// computes the weiths for interpolating pixels
	// the weights change with view_scale
	void lanczos2_compute_weights(double pscale) {
		double s, corr;

		if (pscale > 1.0)
			pscale = 1.0;
		if (pscale >= 1.0)
			lanczos2_n_points = lanczos2_n_points_base;
		else
			lanczos2_n_points = (int) (lanczos2_n_points_base / pscale);

		// sets up the lookup table for the interpolation weights
		for (int j = 0; j <= UNIT_XSAMPLES; j++) {
			// computes the weights for this x value
			int k;
			s = 0;
			int i = j + UNIT_XSAMPLES * (lanczos2_n_points - 1);
			for (k = 0; k < lanczos2_n_points; k++) {
				lanczos2_weights_LU[j][k] =
					lanczos2_LU[(int) (i * pscale + 0.5)];
				s += lanczos2_weights_LU[j][k];
				i -= UNIT_XSAMPLES;
			}
			i = -i;
			for (; k < lanczos2_n_points * 2; k++) {
				lanczos2_weights_LU[j][k] =
					lanczos2_LU[(int) (i * pscale + 0.5)];
				s += lanczos2_weights_LU[j][k];
				i += UNIT_XSAMPLES;
			}
			// normalizes weights so that the sum == UNIT_YSAMPLES
			corr = UNIT_YSAMPLES / s;
			for (k = 0; k < lanczos2_n_points * 2; k++) {
				lanczos2_weights_LU[j][k] =
					(int) (lanczos2_weights_LU[j][k] * corr);
			}
		}
	}

	void lanczos2_compute_view_scale() {
		double wDT;

		wDT = hfov * pwidth / 360.0;
		view_scale = vwidth / wDT;
	}

	// interpolates one pixel
	final int lanczos2_interp_pixel(
		int[][] pd,
		int pw,
		int ph,
		int xs,
		int ys,
		int dx,
		int dy) {
		int tmpR, tmpG, tmpB;
		int itl, jtl;
		int ki, kj;
		int i, j;
		int np2, rgb;

		// cordinates of the top-left pixel to be used
		jtl = (xs) - lanczos2_n_points + 1;
		itl = (ys) - lanczos2_n_points + 1;

		// computes the index for the weights lookup table
		int iw = dx;

		// interpolates each row in the x-axis direction
		np2 = lanczos2_n_points * 2;
		//    np2 = lanczos2_n_points << 1;
		i = itl;
		for (ki = 0; ki < np2; ki++) {
			tmpR = tmpG = tmpB = 0;
			j = jtl;
			for (kj = 0; kj < np2; kj++) {
				int r, g, b;
				int i2, j2;

				// checks for out-of-bounds pixels
				i2 = i;
				j2 = j;
				if (i2 < 0)
					i2 = -i2 - 1;
				if (i2 >= ph)
					i2 = ph - (i2 - ph) - 1;
				if (j2 < 0)
					j2 = -j2 - 1;
				if (j2 >= pw)
					j2 = pw - (j2 - pw) - 1;

				rgb = pd[i2][j2];

				r = (rgb >> 16) & 0xff;
				g = (rgb >> 8) & 0xff;
				b = (rgb >> 0) & 0xff;

				int w = lanczos2_weights_LU[iw][kj];

				tmpR += r * w;
				tmpG += g * w;
				tmpB += b * w;
				//		tmpR = tmpR + r*w;
				//		tmpG = tmpG + g*w;
				//		tmpB = tmpB + b*w;

				j++;
			}
			// stores the result for the current row
			aR[ki] = tmpR;
			aG[ki] = tmpG;
			aB[ki] = tmpB;

			i++;
		}

		// computes the index for the weights lookup table
		iw = dy;

		// final interpolation in the y-axis direction
		tmpR = tmpG = tmpB = 0;
		for (ki = 0; ki < np2; ki++) {
			int w = lanczos2_weights_LU[iw][ki];
			tmpR += aR[ki] * w;
			tmpG += aG[ki] * w;
			tmpB += aB[ki] * w;
		}

		tmpR >>= SHIFT_Y;
		tmpG >>= SHIFT_Y;
		tmpB >>= SHIFT_Y;

		if (tmpR > 255)
			tmpR = 255;
		else {
			if (tmpR < 0)
				tmpR = 0;
		}
		if (tmpG > 255)
			tmpG = 255;
		else {
			if (tmpG < 0)
				tmpG = 0;
		}
		if (tmpB > 255)
			tmpB = 255;
		else {
			if (tmpB < 0)
				tmpB = 0;
		}

		return (tmpR << 16) + (tmpG << 8) + tmpB + 0xff000000;
	}


	/////////////////////////////////////////////
	// end of Lanczos2 interpolation stuff
	/////////////////////////////////////////////

}
