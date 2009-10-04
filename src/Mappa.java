import java.applet.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
//import java.awt.event.*;
//import java.util.Enumeration;
 
public final class Mappa extends Applet implements Runnable {

    private float direzione, ultimaDirezione;
    private Thread timerThread;
    private Image imgMappa, imgCerchio;
    private Color coloreFreccia;
    private int xCentro, yCentro;  // punto da cui fare partire la freccia
    private int offsetAngolo;

    private String nomeImmagine( String nomeParametro ) {
        String s = getParameter( nomeParametro );
        if( s == null ) s = "";
        return s;
    }

    public void init() {

        int i;
        String s;

        imgMappa = getImage( getDocumentBase(), nomeImmagine("MAPPA") );
        
//        imgCerchio = getImage( getDocumentBase(), "Cerchio.gif" );
        // carica l'immagine del file .Jar
		try {
			MediaTracker m = new MediaTracker( this );
			InputStream is = getClass().getResourceAsStream( "Cerchio.gif");
			BufferedInputStream bis = new BufferedInputStream( is );
			byte[] byBuf = new byte[10000];
			int byteRead = bis.read( byBuf, 0, 10000 );
			imgCerchio = Toolkit.getDefaultToolkit().createImage(byBuf);
			m.addImage( imgCerchio, 0 );
			m.waitForAll();
		}
		catch( Exception e ) {
			imgCerchio = null;
		}


        // punto di partenza della freccia: stringa del tipo "12,45" (x e y)
        s = getParameter( "BASEFRECCIA" );
        if( s == null ) s = "100,100";
        i = s.indexOf( ',' );
        if( i == -1 ) {
            xCentro = 100;
            yCentro = 100;
        }
        else {
            xCentro = Integer.parseInt(s.substring(0, i));
            yCentro = Integer.parseInt(s.substring(i + 1));
        }

        // angolo iniziale da aggiungere a quanto ritornato dal viewer (0 = nord)
        s = getParameter( "OFFSET" );
        if( s == null ) s = "0";
        offsetAngolo = Integer.parseInt( s );

//        coloreFreccia = new Color( 63, 59, 240 );
        coloreFreccia = new Color( 255, 64, 64 );

        ultimaDirezione = -1;

    }

    public void start() {
        if (timerThread == null) {
            timerThread = new Thread(this);
        }
        timerThread.start();
    }

    public void stop() {
        timerThread = null;
    }

    public void run() {

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        Thread currentThread = Thread.currentThread();

        while (currentThread == timerThread) {
            if( direzione != ultimaDirezione ) {
                repaint();
                ultimaDirezione = direzione;
            }
            try {
                Thread.sleep( 1000 );
            } catch (InterruptedException e) {
                break;
            }
            chiediDirezione();
        }
    }


    // chiede la direzione corrente alla panoramica
    private void chiediDirezione() {

        try {
	        Applet receiver = null;
	        String receiverName = "Panoramica";
	        receiver = getAppletContext().getApplet(receiverName);
	        if (receiver != null) {
	            direzione = (float) ((ptviewer)receiver).pan();
	            direzione += 180;
	            if( direzione > 360 ) direzione -= 360;
	        };
        } catch( Exception e ) {}            
    }

    private void disegnaFreccia( Graphics g ) {
	int x1, y1, x2, y2, r;
        double angolo, seno, coseno;
	int deltaX1, deltaX2, deltaY1, deltaY2;


        x1 = xCentro;
        y1 = yCentro;

        angolo = (direzione + offsetAngolo)*Math.PI/180;    // converte in radianti
        seno = Math.sin( angolo );
        coseno = Math.cos( angolo );

        r = 30;	// raggio
        x2 = (int) (x1 + r*seno);
        y2 = (int) (y1 - r*coseno);

        r = 7;	// raggio iniziale
        x1 = (int) (x1 + r*seno);
        y1 = (int) (y1 - r*coseno);

        g.setColor( coloreFreccia );
//        g.drawLine( x1, y1, x2, y2 );
//System.out.println( x1 + "  " + y1 );
//System.out.println( x2 + "  " + y2 );
        // disegna un poligono per ingrossare l'inizio della freccia
        if( (coseno > 0.38) || (coseno < -0.92) ) {
            deltaX1 = -1;
            deltaX2 = 1;
        }
        else {
           if( coseno > -0.38 ) {
              deltaX1 = 0;
              deltaX2 = 0;
           }
           else {
              deltaX1 = 1;
              deltaX2 = -1;
           }
        }

        if( (seno > 0.38) || (seno < -0.92) ) {
            deltaY1 = -1;
            deltaY2 = 1;
        }
        else {
           if( seno > -0.38 ) {
              deltaY1 = 0;
              deltaY2 = 0;
           }
           else {
              deltaY1 = 1;
              deltaY2 = -1;
           }
        }

        r = 30;
        x2 = (int) (xCentro + r*seno);
        y2 = (int) (yCentro - r*coseno);

        Polygon poli = new Polygon();
//        poli.addPoint( x1 + deltaX1*2, y1 + deltaY1*2 );
//        poli.addPoint( x1 + deltaX2*2, y1 + deltaY2*2 );
        poli.addPoint( x1 + deltaX1, y1 + deltaY1 );
        poli.addPoint( x1 + deltaX2, y1 + deltaY2 );
        poli.addPoint( x2 + deltaX2, y2 + deltaY2 );
        poli.addPoint( x2 + deltaX1, y2 + deltaY1 );
        g.fillPolygon( poli );

/*
System.out.println( (x1 + deltaX1*2) + "  " + (y1 + deltaY1*2) );
System.out.println( (x1 + deltaX2*2) + "  " + (y1 + deltaY2*2) );
System.out.println( (x2 + deltaX2) + "  " + (y2 + deltaY2) );
System.out.println( (x2 + deltaX1) + "  " + (y2 + deltaY1) + "\n" );
*/
/*        r = 20;	// raggio
        x2 = (int) (xCentro + r*Math.sin( angolo ));
        y2 = (int) (yCentro - r*Math.cos( angolo ));
        g.drawLine( x1 + 1, y1 + 1, x2 + 1, y2 + 1 );
        g.drawLine( x1 + 1, y1 - 1, x2 + 1, y2 - 1 );
        g.drawLine( x1 - 1, y1 + 1, x2 - 1, y2 + 1 );
        g.drawLine( x1 - 1, y1 - 1, x2 - 1, y2 - 1 );
*/

/*        poli.addPoint( x2, y2 );

        angolo = (direzione + offsetAngolo + 90)*Math.PI/180;
        r = 2;
        x2 = (int) (x1 + r*Math.sin( angolo ));
        y2 = (int) (y1 - r*Math.cos( angolo ));
        poli.addPoint( x2, y2 );

        angolo = (direzione + offsetAngolo - 90)*Math.PI/180;
        r = 2;
        x2 = (int) (x1 + r*Math.sin( angolo ));
        y2 = (int) (y1 - r*Math.cos( angolo ));
        poli.addPoint( x2, y2 );

        g.fillPolygon( poli );
        g.fillOval( x1 - 2, y1 - 2, 5, 5 );
*/
    }

    
    private void disegnaCerchio( Graphics g ) {
    	int x, y, w, h, k;
    	
    	x = xCentro - 8 - 1;
    	y = yCentro - 8;
    	g.drawImage( imgCerchio, x, y, this );
    }

    private void disegnaContorno( Graphics g ) {
    	g.setColor( new Color( 0, 0, 0 ) );
    	g.drawLine( 0, 0, 200, 0 );
    	g.drawLine( 200, 0, 200, 300 );
    	g.drawLine( 200, 300, 0, 300 );
    	g.drawLine( 0, 300, 0, 0 );
    }

    public void paint(Graphics g) {
        update( g );
    }


    public void update(Graphics g) {

        g.drawImage( imgMappa, 0, 0, this );        

        disegnaCerchio( g );
        disegnaFreccia( g );
        disegnaContorno( g );

    }
}

