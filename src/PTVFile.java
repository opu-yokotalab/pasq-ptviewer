/*
 * this class handles a *.ptv file
 */

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.*;

public class PTVFile {

	// bits used in the flags
	static final int MASK_CRYPT_SIMPLE = 1;		// crypt with a simple xor schema
	static final int MASK_ALLOW_LOCALVIEW = 2;		// allow viewing from "file" protocol even if the pano is crypted 
	
	// fixed encryption key in string format (easier to write)
	static final String fixedKeyString = "rW.F)H8Yr6q4p2RA2F&G)8d9kKrE6B3Z9{ek}W2RwMfSg;yRuHw43956v:n57";
	
	// fixed encryption key in byte[] format
	byte[] fixedKey;
	
	// domain encryption key stored in the .ptv file
	byte[] domainKey;
	
	// domain from which ptviewer has been loaded in string format
	String currentDomainString;
	
	// domain from which ptviewer has been loaded in byte[] format, used as encryption key
	byte[] currentDomain;
	
	// current version of the file
	static final int VERSION = 1;
	
	// name of the file to read
	String fileName;
	
	// this class stores the data of each directory entry
	protected class DirectoryEntry {

		// row number of this tile
		int nRow;
		// column number of this tile
		int nCol;
		// size of this tile
		int width, height;
		// x and y position of the top left corner of this image in the panoramic image
		int xPosInPano, yPosInPano;
		// offset of this image in the file
		int offset;
		// size of this compressed image in the file
		int size;
		// true if this image has already been loaded in ptviewer
		boolean loaded;
		// yaw angle of the center of each image
		double yaw;
		// pitch angle of the center of each image
		double pitch;
		// width of the tile in degrees
		double wDeg;
		// height of the tile in degrees
		double hDeg;
		
		public DirectoryEntry() {
			loaded = false;
		}

	}
	
	// reference to the ptviewer object
	protected ptviewer ptv;
	
	// array of directory entries for this file
	protected DirectoryEntry tileData[];
	
	// low res preview data
	protected DirectoryEntry previewData;
	
	// number of tile directory entries (preview excluded)
	protected int nTiles;
	
	// size of the panoramic image
	protected int pWidth, pHeight;
	
	// size of directory header
	protected int dirHeaderSize;
	
	// size of a directory entry
	protected int dirEntrySize;
	
	// offset of the directory
	protected int dirOffset;
	
	// flags
	protected int bitFlags;
	
	// number of rows and columns in which the panoramic image is divided
	protected int nRows, nCols;
	
	// flag: true if there is a preview image
	protected boolean hasPreview;
	
	// this buffer will contain the whole .ptv file if we are not
	// dynamically loading tiles
	protected byte[] fullImageBuffer;
	
	// flag: true if we are using partial loading, false if we load the whole file
	// in a single request
	protected boolean usePartialGet;
	
	// flag: true if we are using the Microsoft VM that cannont handle partial gets
	// within an applet with a high level interface
	protected boolean usingMSVM;
	
	// used to perform partial http gets if we are running in the Microsoft VM 
	HttpGetReader hgr = null;
	
	public PTVFile( ptviewer ptv, String fileName ) {
		this.ptv = ptv;
		this.fileName = fileName;
		fullImageBuffer = null;
		
		usingMSVM = (System.getProperty("java.vendor").toLowerCase().indexOf("microsoft") >= 0);
		if(usingMSVM) {
			System.out.println( "PTViewer: running in the Microsoft VM");
			hgr = new HttpGetReader( ptv );
		}
		
		// converts the fixed key from String to byte[] format
		fixedKey = new byte[fixedKeyString.length()];
		char[] ca = fixedKeyString.toCharArray();
		for( int k = 0; k < fixedKeyString.length(); k++ ) {
			fixedKey[k] = (byte) ca[k];
		}

		// loads the file header
		loadHeaderData();
	}
	
	/*
	 * returns the preview Image
	 */
	public Image loadPreviewImage() {
		byte[] buf = null;
		
		if( !hasPreview ) return null;
		
		if( usePartialGet ) {
			buf = doPartialGet( previewData.offset, previewData.size );
			if( buf == null ) {
				// partial get failed
				usePartialGet = false;
				System.out.println( "PTViewer: abandoned dynamic loading" );
				loadWholeFile();
			}
		}
			
		if( !usePartialGet ) {
			buf = new byte[previewData.size];
			System.arraycopy( fullImageBuffer, previewData.offset, buf, 0, previewData.size );
		}
		if( (bitFlags & MASK_CRYPT_SIMPLE) != 0 ) {
			if( currentDomainString.substring(0, 5).equalsIgnoreCase("file:") ) {
				if( (bitFlags & MASK_ALLOW_LOCALVIEW) != 0 ) {
					cryptImage( buf, domainKey );
				}
				else {
					cryptImage( buf, currentDomain );
				}
			}
			else {
				cryptImage( buf, currentDomain );
			}
		}
		Image img = ptv.bufferToImage( buf );
		return img;
	}
	
	/*
	 * Loads the whole file without partial gets, using ptviewer's code
	 */
	protected void loadWholeFile() {
		System.out.println( "PTViewer: loading whole file");
		ptv.percent[0] = 0;
		fullImageBuffer = ptv.file_read( fileName, ptv.percent );
		if( fullImageBuffer == null ) {
			System.out.println( "PTViewer: unable to load panorama file: probably file not found");
			ptv.fatal = true;
			ptv.repaint();
		}
		extractHeaderData( fullImageBuffer );
		extractDirectory( fullImageBuffer );
	}
	
	/*
	 * Loads the header data from the file
	 */
	protected void loadHeaderData() {
		byte[] buf;
		
		if( ptv.getDocumentBase().toString().toLowerCase().startsWith("file:") ) {
			// we are reading a local file
			usePartialGet = false;
			System.out.println( "PTViewer: reading a local image file");
		} else {
			// try a partial get
			usePartialGet = true;
			buf = doPartialGet( 0, 2000 );
			if( buf == null ) {
				// partial get did not work
				System.out.println( "PTViewer: abandoned dynamic loading" );
				usePartialGet = false;
			}
			else {
				// partial get worked
				extractHeaderData( buf );
				extractDirectory( buf );
			}
		}
		if( !usePartialGet ) {
			// load the whole image file
			loadWholeFile();
		}
	}
	
	/*
	 * Gets the specified range of bytes from the file
	 * returns null in case of problems
	 */
	protected byte[] doPartialGet( int start, int nBytes ) {
		String sRange;
		byte[] buffer;
		int responseCode;
		
		if( usingMSVM ) return hgr.doPartialGet( fileName, start, nBytes );
		
		try {
			URL url = new URL( ptv.getDocumentBase(), fileName );
			HttpURLConnection Connection = (HttpURLConnection) url.openConnection();
			sRange = "bytes=" + start + "-" + (start + nBytes - 1);
//			System.out.println( sRange );
			Connection.setRequestProperty("Range",sRange);
			Connection.connect();
//			System.out.println("Content-length: " + Connection.getContentLength());
			responseCode = Connection.getResponseCode();
//			System.out.println("ResponseCode " + responseCode);
			InputStream input = Connection.getInputStream();
			buffer = new byte[nBytes];

			// checks the response values
            if( responseCode != 206 ) {
            	System.out.println( "PTViewer: unexpected response code: " + responseCode );
            	return null;
            }
			
            ByteArrayOutputStream ba;
   			ba = new ByteArrayOutputStream();
            		
    		int tmpLen;
    		byte[] tmpBuf = new byte[4096];
    		while ((tmpLen = input.read(tmpBuf)) >= 0) {
    			ba.write( tmpBuf, 0, tmpLen );
            }
    		ba.close();
    		buffer = ba.toByteArray();

            int len = buffer.length;
			if( len != nBytes ) {
            	System.out.println( "PTViewer: number of returned bytes does not match. Requested: " + nBytes + "   Returned: " + len );
            	return null;
            }

			return buffer;
		}
		catch( Exception ex ) {
			System.out.println( ex.toString());
			return null;
		}

	}

	
	
	/*
	 * Returns a tile Image
	 */
	protected Image loadTileImage( DirectoryEntry de ) {
		byte[] buf = null;
		
		if( usePartialGet ) {
			buf = doPartialGet( de.offset, de.size );
			if( buf == null ) {
				// partial get failed
				usePartialGet = false;
				System.out.println( "PTViewer: abandoned dynamic loading" );
				loadWholeFile();
			}
		}
		if( !usePartialGet ) {
			buf = new byte[de.size];
			System.arraycopy( fullImageBuffer, de.offset, buf, 0, de.size );
		}		
		if( (bitFlags & MASK_CRYPT_SIMPLE) != 0 ) {
			if( currentDomainString.substring(0, 5).equalsIgnoreCase("file:") ) {
				if( (bitFlags & MASK_ALLOW_LOCALVIEW) != 0 ) {
					cryptImage( buf, domainKey );
				}
				else {
					cryptImage( buf, currentDomain );
				}
			}
			else {
				cryptImage( buf, currentDomain );
			}
		}
		Image img = ptv.bufferToImage( buf );
		return img;
	}
	
	/*
	 * Loads a tile in ptviewer
	 */
	protected void loadTileInPTViewer( DirectoryEntry de ) {
		
		if( de.loaded ) return;
		
		ptv.loadingROI = true;
		
		Image r = null;
		r = loadTileImage( de );
		if (r != null) {
			ptv.ptinsertImage(
					ptv.pdata,
				de.xPosInPano,
				de.yPosInPano,
				r,
				(ptv.pheight + 99) / 100);

			// Update warped hotspots
			if (ptv.hsready) {
				int k;
				for (k = 0; k < ptv.numhs; k++) {
					if ((ptv.hs_imode[k] & ptviewer.IMODE_WARP) > 0) { // warped hotspot
						int w = (int) ptv.hs_up[k];
						int h = (int) ptv.hs_vp[k];
						int xp = (int) ptv.hs_xp[k] - w / 2;
						int yp = (int) ptv.hs_yp[k] - h / 2;
						ptv.im_extractRect(
								ptv.pdata,
							xp,
							yp,
							(int[]) ptv.hs_him[k],
							w,
							0,
							h,
							w,
							h);
					}
				}
			}
			de.loaded = true;
			r = null;
		}
		ptv.loadingROI = false;
	}
	
	/*
	 * Dynamically loads the tiles directly in ptviewer
	 */
	public void loadTiles() {
		boolean done;
		int nLoaded = 0;

		computeYawAngle();
		computePitchAngle();
		
		do {
			done = true;
			int iVisible = -1;		// index of the nearest visible tile
			int iNotVisible = -1;	// index of the nearest not visible tile
			double minDistVisible = 10000, minDistNotVisible = 10000;
			// looks for the tile that is nearer to the direction of view
			for( int k = 0; k < nTiles; k++ ){
				if( !tileData[k].loaded ) {
					done = false;
					double distX = Math.abs( ptv.yaw - tileData[k].yaw );
					if( distX > 180 ) distX = 360 - distX;
					double distY = Math.abs( ptv.pitch - tileData[k].pitch );
					double dist = Math.sqrt( distX*distX + distY*distY );
					// computes the nearest visible and not visible tile in order to load first all visible tiles
					if( isTileVisible(k) ) {
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
			
			// if there is a visible tile chooses it, else chooses an unvisible one
			int i;
			if( iVisible >= 0 ) {
				i = iVisible;
			}
			else {
				i = iNotVisible;
			}
			
			if( i >= 0 ) {
				loadTileInPTViewer( tileData[i] );
				nLoaded++;
				// updates the progress bar
				if( ptv.showToolbar ) ((toolbar) ptv.tlbObj).setBarPerc( nLoaded*100/nTiles );
				
				// repaints only if the loaded tile is visible
				if( !isTileVisible(i) ) ptv.onlyPaintToolbar = true; 

				ptv.paintDone = false;
				ptv.forceBilIntepolator = true;		// to speed up ROI drawing
				ptv.repaint();		// we always call repaint() to draw the toolbar

				// stops execution until paint() is executed
				int counter = 0;	// emergency exit
				while( !ptv.paintDone && counter < 100 ) {
					try {
						Thread.sleep(10L);
					} catch (Exception _ex) {}
					counter++;
				}
//				System.out.println( "Loaded tile " + i + " pan=" + roi_pan[i] + " pano_pan=" + yaw);
			}
		} while( !done );
	
		// clears the progress bar
		if( ptv.showToolbar ) ((toolbar) ptv.tlbObj).setBarPerc( 0 );
		ptv.dirty = true;
		ptv.repaint();
}
	
	/*
	 * returns true if the tile is currently visible in ptviewer
	 */
	protected boolean isTileVisible( int nTile ) {
		boolean visible = true;
		// distance in degrees between the center of the current view and the center of the loaded ROI
		double yawDist = Math.abs(ptv.yaw - tileData[nTile].yaw);
		if( yawDist > 180 ) yawDist = 360 - yawDist;
		double pitchDist = Math.abs( ptv.pitch - tileData[nTile].pitch );
		if( yawDist > (ptv.hfov + tileData[nTile].wDeg)/2 ) visible = false;
		if( visible ) {
			if( pitchDist > (ptv.math_fovy( ptv.hfov, ptv.vwidth, ptv.vheight) + tileData[nTile].hDeg)/2)
				visible = false;
		}
		return visible;
	}
	
	/*
	 * Computes the yaw angle of the center of each tile
	 */
	protected void computeYawAngle() {
		for( int k = 0; k < nTiles; k++ ) {
			double p = 360.0*(tileData[k].xPosInPano + tileData[k].width/2)/pWidth;
			if( p > 360 ) p -= 360;
			p -= 180;
			tileData[k].yaw = p;
		}
	}
	
	/*
	 * Computes the pitch angle of the center of each tile
	 */
	protected void computePitchAngle() {
		for( int k = 0; k < nTiles; k++ ) {
			// y coord of the middle of the image if the pano had a 2:1 size ratio
			int y = pWidth/4 - pHeight/2 + tileData[k].yPosInPano + tileData[k].height/2;
			double t = 90.0 - 180.0*y/(pWidth/2);
			tileData[k].pitch = t;
		}
	}
	
	/*
	 * Extracts the header data from a buffer containing the first bytes of the file
	 */
	protected void extractHeaderData( byte[] buf ) {
		
		// check signature
		if( buf[0] == 'P' && buf[1] == 'V' ) {
			System.out.println( "PTViewer: This is not a valid .ptv file");
			ptv.fatal = true;
			ptv.repaint();
		}
		
		// check version number
		int vers = extractInt2( buf, 2 );
		if( vers != 1 ) {
			System.out.println( "PTViewer: unknown version for .ptv file");
			ptv.fatal = true;
			ptv.repaint();
		}
		
		pWidth = extractInt4( buf, 4 );
		pHeight = extractInt4( buf, 8 );
		dirHeaderSize = extractInt4( buf, 12 );
		dirEntrySize = extractInt4( buf, 16 );
		dirOffset = extractInt4( buf, 20 );
		bitFlags = extractInt4( buf, 24 );
		
		// if the file is encrypted extracts the encryption key
		domainKey = null;
		currentDomainString = "";
		if( (bitFlags & MASK_CRYPT_SIMPLE) != 0 ) {
			int lKey = extractInt2( buf, 28 );
			if( (bitFlags & MASK_ALLOW_LOCALVIEW) != 0 ) {
				// loads the key from the file
				byte[] tmp = new byte[lKey];
				for( int k = 0; k < lKey; k++ ) {
					tmp[k] = buf[30 + k];
				}
				// decrypts it
				byte[] key = cryptComputeFixedKey( lKey );
				domainKey = cryptByteArray( tmp, key );
			}
			else {
				// creates a dummy key, since it will not be used
				domainKey = new byte[] { 3, 4, 5, 6, 7, 8, 9 };
			}
			domainKey = cryptResizeKey( domainKey, 2000 );
			int nCh = lKey;
			if( ptv.getDocumentBase().toString().length() < nCh - 2 )
				nCh = ptv.getDocumentBase().toString().length();
			currentDomainString = ptv.getDocumentBase().toString().substring( 0, nCh - 2 );
			currentDomainString = currentDomainString.toLowerCase();
			currentDomain = cryptCreateDomainKey( currentDomainString );
			currentDomain = cryptResizeKey( currentDomain, 2000 );
		}
	}

	/*
	 * Extracts the directory data from a buffer containing the first bytes of the file
	 * ExtractHeaderData() must have been already called
	 */
	protected void extractDirectory( byte[] buf ) {
		
		int off = dirOffset;
		nRows = extractInt2( buf, off );
		off += 2;
		nCols = extractInt2( buf, off );
		off += 2;
		int itmp = extractInt2( buf, off );
		hasPreview = (itmp != 0);
		
		// allocates the array of directory entries
		nTiles = nRows*nCols;
		tileData = new DirectoryEntry[nTiles];
		if( hasPreview )
			previewData = new DirectoryEntry();
		else
			previewData = null;
		
		// have we read enough bytes?
		// if not we load what we need
		int neededBytes = dirOffset + dirHeaderSize;
		if( hasPreview ) neededBytes += dirEntrySize;
		neededBytes += dirEntrySize * nTiles;
		if( neededBytes > buf.length ) {
			if( usePartialGet ) {
				buf = doPartialGet( 0, neededBytes + 100 );
			}
		}
		
		// reads the directory entries
		off = dirOffset + dirHeaderSize;
		if( hasPreview ) {
			extractDirectoryEntry( buf, off, previewData );
			off += dirEntrySize;
		}
		for( int k = 0; k < nTiles; k++ ) {
			tileData[k] = new DirectoryEntry();
			extractDirectoryEntry( buf, off, tileData[k] );
			off += dirEntrySize;
		}
	}
	
	/*
	 * Extracts one directory entry from a buffer containing the first bytes of the file.
	 * stores the data in "de"
	 */
	protected void extractDirectoryEntry( byte[] buf, int offset, DirectoryEntry de ) {
		de.nRow = extractInt2( buf, offset );
		de.nCol = extractInt2( buf, offset + 2 );
		de.width = extractInt4( buf, offset + 4 );
		de.height = extractInt4( buf, offset + 8 );
		de.xPosInPano = extractInt4( buf, offset + 12 );
		de.yPosInPano = extractInt4( buf, offset + 16 );
		de.offset  = extractInt4( buf, offset + 20 );
		de.size = extractInt4( buf, offset + 24 );
		de.wDeg = 1.0*de.width*360/pWidth;
		de.hDeg = 1.0*de.height*360/pWidth;
	}
	
	/*
	 * Extracts a 2byte integer value from the buffer at the specified offset
	 */
	protected int extractInt2( byte[] buf, int offset ) {
		return unsignedByte2Int(buf[offset]) + (unsignedByte2Int(buf[offset + 1]) << 8);
	}

	/*
	 * Extracts a 4byte integer value from the buffer at the specified offset
	 */
	protected int extractInt4( byte[] buf, int offset ) {
		return unsignedByte2Int(buf[offset]) + (unsignedByte2Int(buf[offset + 1]) << 8) + (unsignedByte2Int(buf[offset + 2]) << 16) + (unsignedByte2Int(buf[offset + 3]) << 24);
	}
	
	/*
	 * Converts an unsigned byte to an integer value
	 */
	protected int unsignedByte2Int( byte b ) {
		return (int) b & 0xFF;
	}

	/*
	 * returns a fixed key of the desired length
	 */
	protected byte[] cryptComputeFixedKey( int lKey ) {
		int lFixed = fixedKey.length;
		byte[] retVal = new byte[lKey];
		int k;
		int j = 0;	// pointer to the output string
		
		// repeats fixedKey if needed
		while( lKey > lFixed ) {
			for( k = 0; k < lFixed; k++, j++ ) {
				retVal[j] = fixedKey[k];
			}
			lKey -= lFixed;
		}

		// takes only the first bytes of fixedKey
		for( k = 0; k < lKey; k++, j++ ) {
			retVal[j] = fixedKey[k];
		}
		
		return retVal;
	}
	
	/*
	 * returns a string of length lKey repeating many times the content of orgKey
	 */
	protected byte[] cryptResizeKey( byte[] orgKey, int lKey ) {
		int lOrg = orgKey.length;
		byte[] retVal = new byte[lKey];
		int k;
		int j = 0;	// pointer to the output string
		
		// repeats orgKey if needed
		while( lKey > lOrg ) {
			for( k = 0; k < lOrg; k++, j++ ) {
				retVal[j] = orgKey[k];
			}
			lKey -= lOrg;
		}

		// takes only the first bytes of orgKey
		for( k = 0; k < lKey; k++, j++ ) {
			retVal[j] = orgKey[k];
		}
		
		return retVal;
	}
	
	/*
	 * crypts an image stored as a byte array
	 */
	protected void cryptImage( byte[] img, byte[] key ) {
		int nBytes = key.length;
		if( img.length < nBytes ) {
			nBytes = img.length;
		}
		for( int k = 0; k < nBytes; k++ ) {
			img[k] ^= key[k];
		}
	}

	/*
	 * crypts a byte array
	 */
	protected byte[] cryptByteArray( byte[] s, byte[] key ) {
		byte[] retVal = new byte[s.length];
		for( int k = 0; k < key.length; k++ ) {
			retVal[k] = (byte) (s[k] ^ key[k]);
		}
		return retVal;
	}

	/*
	 * computes a hash value for the given string
	 */
	protected short cryptHashString( String s ) {
		int a, b, i, retVal;
		
		retVal = 0;
		a = 19;
		b = 17;
		for( i = 0; i < s.length(); i++ ) {
			retVal = retVal*a + s.charAt(i);
			retVal = retVal & 0x3FFF;
			a = a * b;
			a = a & 0x3FFF;
		}
		return (short) (retVal & 0xFFFF);
	}
	
	
	
	/*
	 * converts a short to the first 2 bytes of a byte array
	 */
	protected void cryptShort2Str( short i, byte[] ba ) {
		byte bh, bl;
		String s;
		
		bl = (byte) (i & 0xFF);
		bh = (byte) ((i & 0xFF00)/256);
		ba[0] = bh;
		ba[1] = bl;
		return;
	}	
		
	/*
	 * creates a key from the http domain
	 */
	protected byte[] cryptCreateDomainKey( String domain ) {
		byte[] retVal;
		String s;
		short i;
		
		retVal = new byte[domain.length() + 2];
		// computes a hash of the string domain
		i = cryptHashString( domain );
		// copies it to the first 2 bytes of the result
		cryptShort2Str( i, retVal );
		// appends the domain to the result
		char[] ba = domain.toCharArray();
		for( int k = 0; k < ba.length; k++ ) {
			retVal[k + 2] = (byte) ba[k];
		}
		return retVal;
	}
	
}
