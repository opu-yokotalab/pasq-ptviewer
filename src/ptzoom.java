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

// modified SetView(): it did not work with quality=4

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.*;
import java.util.Vector;

public class ptzoom extends Applet
    implements Runnable
{

    public ptzoom()
    {
        PTViewer = "ptviewer";
        pv = null;
        loadImages = null;
        filename = "image";
        maskname = null;
        idata = null;
        hsdata = null;
        cpitch = 0.0D;
        cyaw = 0.0D;
        croll = 0.0D;
        chfov = 50D;
        showSHS = -1;
        progress = false;
        zoom_image = null;
        hsimage = null;
        ready = false;
        max_oversampling = 8D;
        position = 0;
        antialias = false;
        scaledImages = null;
        scaledHSImages = null;
    }

    public ptzoom(ptviewer ptviewer1, String s)
    {
        PTViewer = "ptviewer";
        pv = null;
        loadImages = null;
        filename = "image";
        maskname = null;
        idata = null;
        hsdata = null;
        cpitch = 0.0D;
        cyaw = 0.0D;
        croll = 0.0D;
        chfov = 50D;
        showSHS = -1;
        progress = false;
        zoom_image = null;
        hsimage = null;
        ready = false;
        max_oversampling = 8D;
        position = 0;
        antialias = false;
        scaledImages = null;
        scaledHSImages = null;
        pv = ptviewer1;
        setStub(new ptstub(pv, s));
    }

    public ptzoom(ptviewer ptviewer1, Image image, byte abyte0[], String s)
    {
        PTViewer = "ptviewer";
        pv = null;
        loadImages = null;
        filename = "image";
        maskname = null;
        idata = null;
        hsdata = null;
        cpitch = 0.0D;
        cyaw = 0.0D;
        croll = 0.0D;
        chfov = 50D;
        showSHS = -1;
        progress = false;
        zoom_image = null;
        hsimage = null;
        ready = false;
        max_oversampling = 8D;
        position = 0;
        antialias = false;
        scaledImages = null;
        scaledHSImages = null;
        pv = ptviewer1;
        zoom_image = image;
        if(abyte0 != null && abyte0.length == image.getWidth(null) * image.getHeight(null))
        {
            hsdata = new byte[image.getHeight(null)][image.getWidth(null)];
            for(int i = 0; i < image.getHeight(null); i++)
                System.arraycopy(abyte0, i * image.getWidth(null), hsdata[i], 0, image.getWidth(null));

        }
        setStub(new ptstub(pv, s));
    }

    public ptzoom(ptviewer ptviewer1, int ai[][], double d, double d1, double d2, double d3)
    {
        PTViewer = "ptviewer";
        pv = null;
        loadImages = null;
        filename = "image";
        maskname = null;
        idata = null;
        hsdata = null;
        cpitch = 0.0D;
        cyaw = 0.0D;
        croll = 0.0D;
        chfov = 50D;
        showSHS = -1;
        progress = false;
        zoom_image = null;
        hsimage = null;
        ready = false;
        max_oversampling = 8D;
        position = 0;
        antialias = false;
        scaledImages = null;
        scaledHSImages = null;
        pv = ptviewer1;
        chfov = d;
        cyaw = d1;
        cpitch = d2;
        croll = d3;
        width = ai[0].length;
        height = ai.length;
        hsdata = new byte[height][width];
        if(hsimage == null)
        {
            for(int i = 0; i < height; i++)
            {
                for(int j = 0; j < width; j++)
                    hsdata[i][j] = -1;

            }

        }
        idata = ai;
        setStub(new ptstub(pv, ""));
        max_oversampling = 1000D;
    }

    public void init()
    {
        f1 = new double[3][3];
        f2 = new double[3][3];
        String s;
        if((s = getParameter("PTViewer")) != null)
            PTViewer = s;
        if((s = getParameter("file")) != null)
            filename = s;
        if((s = getParameter("mask")) != null)
            maskname = s;
        if((s = getParameter("hsimage")) != null)
            hsimage = s;
        if((s = getParameter("fov")) != null)
            chfov = Double.valueOf(s).doubleValue();
        if((s = getParameter("pan")) != null)
            cyaw = Double.valueOf(s).doubleValue();
        if((s = getParameter("tilt")) != null)
            cpitch = Double.valueOf(s).doubleValue();
        if((s = getParameter("rot")) != null)
            croll = Double.valueOf(s).doubleValue();
        if((s = getParameter("showSHS")) != null)
            showSHS = Integer.parseInt(s);
        if((s = getParameter("progress")) != null && s.equalsIgnoreCase("true"))
            progress = true;
        if(getParameter("antialias") != null)
        {
            antialias = true;
            max_oversampling = 1.5D;
        }
        if((s = getParameter("oversampling")) != null)
            max_oversampling = Double.valueOf(s).doubleValue();
    }

    public void start()
    {
        while(pv == null) 
            try
            {
                pv = (ptviewer)getAppletContext().getApplet(PTViewer);
            }
            catch(Exception _ex)
            {
                try
                {
                    Thread.sleep(2000L);
                }
                catch(InterruptedException _ex2)
                {
                    return;
                }
            }
        if(pv != null)
        {
            pv.startCommunicating(this);
            if(idata == null)
            {
                if(zoom_image == null)
                {
                    if(loadImages == null)
                    {
                        loadImages = new Thread(this);
                        loadImages.start();
                        return;
                    }
                } else
                {
                    load_zoom_image();
                    return;
                }
            } else
            {
                SetDefMatrix();
                pv.dirty = true;
                pv.repaint();
                ready = true;
            }
        }
    }

    public synchronized void set(double d, double d1, double d2, double d3, int ai[][])
    {
        chfov = d;
        cyaw = d1;
        cpitch = d2;
        croll = d3;
        if(ai != idata)
        {
            width = ai[0].length;
            height = ai.length;
            if(hsdata[0].length != width || hsdata.length != height)
            {
                hsdata = new byte[height][width];
                for(int i = 0; i < height; i++)
                {
                    for(int j = 0; j < width; j++)
                        hsdata[i][j] = -1;

                }

            }
            idata = ai;
        }
        SetDefMatrix();
        pv.dirty = true;
        pv.repaint();
    }

    public void set(double d, double d1, double d2, double d3, int ai[][], int i)
    {
        position = i;
        set(d, d1, d2, d3, ai);
    }

    public void stop()
    {
        if(pv != null)
        {
            if(loadImages != null)
            {
                pv.stopThread(loadImages);
                loadImages = null;
            }
            if(showSHS >= 0 && showSHS < pv.numshs)
                pv.shs_imode[showSHS] = 0;
            pv.stopCommunicating(this);
        }
        scaledImages = null;
        scaledHSImages = null;
    }

    public void run()
    {
        load_zoom_image();
    }

    void load_zoom_image()
    {
        Image image;
        if(zoom_image == null)
        {
            if(progress)
            {
                pv.ready = false;
                pv.percent[0] = 0;
                pv.repaint();
                image = pv.loadImageProgress(filename);
            } else
            {
                image = pv.loadImage(filename);
            }
        } else
        {
            image = zoom_image;
        }
        if(image != null)
        {
            width = image.getWidth(null);
            height = image.getHeight(null);
            int ai[][];
            try
            {
                ai = new int[height][width];
            }
            catch(Exception _ex)
            {
                return;
            }
            pv.ptImageTo2DArray(ai, image);
            if(maskname != null && (image = pv.loadImage(maskname)) != null && image.getWidth(null) == width && image.getHeight(null) == height)
                pv.ptImageToAlpha(ai, image);
            if(hsdata == null)
            {
                hsdata = new byte[height][width];
                Image image1;
                if(hsimage == null)
                {
                    if(pv.filename != null && pv.filename.toLowerCase().endsWith(".mov"))
                    {
                        for(int i = 0; i < height; i++)
                        {
                            for(int k = 0; k < width; k++)
                                hsdata[i][k] = 0;

                        }

                    } else
                    {
                        for(int j = 0; j < height; j++)
                        {
                            for(int l = 0; l < width; l++)
                                hsdata[j][l] = -1;

                        }

                    }
                } else
                if((image1 = pv.loadImage(hsimage)) != null && image1.getWidth(null) == width && image1.getHeight(null) == height)
                {
                    int ai1[][] = new int[height][width];
                    pv.ptImageTo2DArray(ai1, image1);
                    for(int i1 = 0; i1 < height; i1++)
                    {
                        for(int j1 = 0; j1 < width; j1++)
                            hsdata[i1][j1] = (byte)(ai1[i1][j1] >> 24);

                    }

                    System.gc();
                }
            }
            SetDefMatrix();
            idata = ai;
            pv.dirty = true;
            if(showSHS >= 0 && showSHS < pv.numshs)
                pv.shs_imode[showSHS] = 2;
            if(progress)
                pv.ready = true;
            pv.repaint();
            ready = true;
        }
    }

    public void paint(Graphics g)
    {
        if(pv == null || idata == null || pv.vdata == null)
            return;
        if(antialias && scaledImages == null)
        {
            scaledImages = new Vector();
            scaledImages.addElement(idata);
            int ai[][] = idata;
            double d = pv.hfov_max / ((double)pv.vwidth * chfov * max_oversampling);
            for(int i = 0; ai != null && (double)ai[0].length * d > 1.0D; i++)
            {
                ai = pv.im_halfsize(ai);
                scaledImages.addElement(ai);
            }

            scaledHSImages = new Vector();
            scaledHSImages.addElement(hsdata);
            for(byte abyte1[][] = hsdata; abyte1 != null && (double)abyte1[0].length * d > 1.0D;)
            {
                abyte1 = pv.im_halfsize(abyte1);
                scaledHSImages.addElement(abyte1);
            }

        }
        if(pv.dirty && (antialias || pv.hfov * (double)width < max_oversampling * chfov * (double)pv.vwidth))
        {
            int ai1[][] = idata;
            byte abyte0[][] = hsdata;
            if(antialias && scaledImages != null)
            {
                double d1 = pv.hfov / ((double)pv.vwidth * chfov * max_oversampling);
                int j = 0;
                for(int k = idata[0].length; (double)k * d1 > 1.0D; k >>= 1)
                    j++;

                if(scaledImages.elementAt(j) != null)
                {
                    ai1 = (int[][])scaledImages.elementAt(j);
                    abyte0 = (byte[][])scaledHSImages.elementAt(j);
                }
            }
            switch(pv.quality)
            {
            default:
                break;

            case 0: // '\0'
                pv.dirty = !SetView(ai1, abyte0, false);
                return;

            case 1: // '\001'
                if(pv.panning || pv.lastframe > pv.frames)
                {
                    SetView(ai1, abyte0, false);
                    return;
                } else
                {
                    pv.dirty = !SetView(ai1, abyte0, true);
                    return;
                }

            case 2: // '\002'
                if(pv.panning)
                {
                    SetView(ai1, abyte0, false);
                    return;
                } else
                {
                    pv.dirty = !SetView(ai1, abyte0, true);
                    return;
                }

            case 3: // '\003'
            case 4:	// if ptviewer has quality == 4 this image will viewed with bilinear, but it will work, at least
                pv.dirty = !SetView(ai1, abyte0, true);
                break;
            }
        }
    }

	// return true if the whole viewport is/has been rendered	
    boolean SetView(int ai[][], byte abyte0[][], boolean bilinear)
    {
        boolean rename = true;
        int k5 = ai[0].length;
        int l5 = ai.length;
        SetMatrix((pv.pitch * 2D * 3.1415926535897931D) / 360D, ((pv.yaw - cyaw) * 2D * 3.1415926535897931D) / 360D, 1);
        if(position == 1)
            ShiftCubeFace(1.5707963267948966D);
        else
        if(position == 2)
            ShiftCubeFace(-1.5707963267948966D);
        double d1 = (double)pv.vwidth / (2D * Math.tan((pv.hfov * 3.1415926535897931D) / 180D / 2D));
        double d2 = (double)k5 / (2D * Math.tan((chfov * 3.1415926535897931D) / 180D / 2D));
        for(int i2 = 0; i2 < 3; i2++)
            f1[2][i2] *= d1;

        for(int j2 = 0; j2 < 3; j2++)
        {
            for(int k2 = 0; k2 < 2; k2++)
                f1[j2][k2] *= d2;

        }

        double d;
        for(d = 256D; (d1 * d1 * d) / 256D > 10000000D; d /= 2D);
        int j3 = (int)(d * f1[0][0] + 0.5D);
        int k3 = (int)(d * f1[0][1] + 0.5D);
        int l3 = (int)(d * f1[0][2] + 0.5D);
        int i4 = (int)(d * f1[1][0] + 0.5D);
        int j4 = (int)(d * f1[1][1] + 0.5D);
        int k4 = (int)(d * f1[1][2] + 0.5D);
        int l4 = (int)(d * f1[2][0] + 0.5D);
        int i5 = (int)(d * f1[2][1] + 0.5D);
        int j5 = (int)(d * f1[2][2] + 0.5D);
        int w2 = pv.vwidth - 1 >> 1;
        int h2 = pv.vheight >> 1;
        int sw2 = (k5 << 7) + 64;
        int sh2 = (l5 << 7) + 64;
        int h1 = l5 - 1;
        int w1 = k5 - 1;
        int l12 = 0;
        int ai1[] = ai[0];
        int ai2[] = ai[1];
        byte abyte1[] = abyte0[0];
        if(bilinear)
        {
            int k = 0;
            int i9 = -h2 * k4 + j5 + 128;
            int k9 = -h2 * i4 + l4;
            for(int i10 = -h2 * j4 + i5; k < pv.vheight; i10 += j4)
            {
                int k7 = pv.vwidth * k;
                int i = 0;
                int k8 = k7;
                int k10 = -w2 * l3 + i9;
                int i11 = -w2 * j3 + k9;
                int k11 = -w2 * k3 + i10;
                while(i < pv.vwidth) 
                {
                    int l2;
                    if(pv.vdata[k8] == 0)
                        if((l2 = k10 + i * l3 >> 8) <= 0)
                        {
                            rename = false;
                        } else
                        {
                            int xs = (i11 + i * j3) / l2 + sw2;
                            int ys = (k11 + i * k3) / l2 + sh2;
                            int dx = xs & 0xff;
                            xs >>= 8;
                            int dy = ys & 0xff;
                            int i13;
                            if((ys >>= 8) == l12 && xs >= 0 && xs < w1)
                            {
                                if(((i13 = ai1[xs]) & 0xff000000) != 0)
                                {
                                    pv.hs_vdata[k8] = abyte1[xs];
                                    int i14 = ai2[xs++];
                                    int k13 = ai1[xs];
                                    int k14 = ai2[xs];
                                    pv.vdata[k8] = ptviewer.bil(i13, k13, i14, k14, dx, dy);
                                } else
                                {
                                    rename = false;
                                }
                            } else
                            if(xs >= 0 && ys >= 0 && ys < h1 && xs < w1)
                            {
                                l12 = ys;
                                abyte1 = abyte0[ys];
                                ai1 = ai[ys++];
                                ai2 = ai[ys];
                                int j13;
                                if(((j13 = ai1[xs]) & 0xff000000) != 0)
                                {
                                    pv.hs_vdata[k8] = abyte1[xs];
                                    int j14 = ai2[xs++];
                                    int l13 = ai1[xs];
                                    int l14 = ai2[xs];
                                    pv.vdata[k8] = ptviewer.bil(j13, l13, j14, l14, dx, dy);
                                } else
                                {
                                    rename = false;
                                }
                            } else
                            {
                                if(ys == -1)
                                    ys = 0;
                                else
                                if(ys == l5)
                                    ys = h1;
                                if(xs == -1)
                                    xs = 0;
                                else
                                if(xs == k5)
                                    xs = w1;
                                int i12;
                                if(xs >= 0 && xs < k5 && ys >= 0 && ys < l5 && ((i12 = ai[ys][xs]) & 0xff000000) != 0)
                                {
                                    pv.vdata[k8] = i12;
                                    pv.hs_vdata[k8] = abyte0[ys][xs];
                                } else
                                {
                                    rename = false;
                                }
                            }
                        }
                    i++;
                    k8++;
                }
                k++;
                i9 += k4;
                k9 += i4;
            }

        } else
        {
            sw2 += 128;
            sh2 += 128;
            int l = 0;
            int j9 = -h2 * k4 + j5 + 128;
            int l9 = -h2 * i4 + l4;
            for(int j10 = -h2 * j4 + i5; l < pv.vheight; j10 += j4)
            {
                int l7 = pv.vwidth * l;
                int j = 0;
                int l8 = l7;
                int l10 = -w2 * l3 + j9;
                int j11 = -w2 * j3 + l9;
                int l11 = -w2 * k3 + j10;
                while(j < pv.vwidth) 
                {
                    int i3;
                    if(pv.vdata[l8] == 0)
                        if((i3 = l10 + j * l3 >> 8) <= 0)
                        {
                            rename = false;
                        } else
                        {
                            int j1 = (j11 + j * j3) / i3 + sw2 >> 8;
                            int l1 = (l11 + j * k3) / i3 + sh2 >> 8;
                            int j12;
                            if(j1 >= 0 && j1 < k5 && l1 >= 0 && l1 < l5 && ((j12 = ai[l1][j1]) & 0xff000000) != 0)
                            {
                                pv.vdata[l8] = j12;
                            } else
                            {
                                if(j1 == -1)
                                    j1 = 0;
                                else
                                if(j1 == k5)
                                    j1 = w1;
                                if(l1 == -1)
                                    l1 = 0;
                                else
                                if(l1 == l5)
                                    l1 = h1;
                                int k12;
                                if(j1 >= 0 && j1 < k5 && l1 >= 0 && l1 < l5 && ((k12 = ai[l1][j1]) & 0xff000000) != 0)
                                    pv.vdata[l8] = k12;
                                else
                                    rename = false;
                            }
                        }
                    j++;
                    l8++;
                }
                l++;
                j9 += k4;
                l9 += i4;
            }

        }
        return rename;
    }

    void SetMatrix(double d, double d1, int i)
    {
        double ad[][] = new double[3][3];
        double ad1[][] = new double[3][3];
        double ad2[][] = new double[3][3];
        ad[0][0] = 1.0D;
        ad[0][1] = 0.0D;
        ad[0][2] = 0.0D;
        ad[1][0] = 0.0D;
        ad[1][1] = Math.cos(d);
        ad[1][2] = Math.sin(d);
        ad[2][0] = 0.0D;
        ad[2][1] = -ad[1][2];
        ad[2][2] = ad[1][1];
        ad1[0][0] = Math.cos(d1);
        ad1[0][1] = 0.0D;
        ad1[0][2] = -Math.sin(d1);
        ad1[1][0] = 0.0D;
        ad1[1][1] = 1.0D;
        ad1[1][2] = 0.0D;
        ad1[2][0] = -ad1[0][2];
        ad1[2][1] = 0.0D;
        ad1[2][2] = ad1[0][0];
        if(i == 1)
            pv.matrix_matrix_mult(ad, ad1, ad2);
        else
            pv.matrix_matrix_mult(ad1, ad, ad2);
        pv.matrix_matrix_mult(ad2, f2, f1);
    }

    void SetDefMatrix()
    {
        double ad[][] = new double[3][3];
        double ad1[][] = new double[3][3];
        double d = (-cpitch * 2D * 3.1415926535897931D) / 360D;
        double d1 = (-croll * 2D * 3.1415926535897931D) / 360D;
        ad[0][0] = 1.0D;
        ad[0][1] = 0.0D;
        ad[0][2] = 0.0D;
        ad[1][0] = 0.0D;
        ad[1][1] = Math.cos(d);
        ad[1][2] = Math.sin(d);
        ad[2][0] = 0.0D;
        ad[2][1] = -ad[1][2];
        ad[2][2] = ad[1][1];
        ad1[0][0] = Math.cos(d1);
        ad1[0][1] = Math.sin(d1);
        ad1[0][2] = 0.0D;
        ad1[1][0] = -ad1[0][1];
        ad1[1][1] = ad1[0][0];
        ad1[1][2] = 0.0D;
        ad1[2][0] = 0.0D;
        ad1[2][1] = 0.0D;
        ad1[2][2] = 1.0D;
        pv.matrix_matrix_mult(ad1, ad, f2);
    }

    public String getAppletInfo()
    {
        return "topFrame";
    }

    void ShiftCubeFace(double d)
    {
        double ad[][] = new double[3][3];
        double ad1[][];
        (ad1 = new double[3][3])[0][0] = Math.cos(d);
        ad1[0][1] = 0.0D;
        ad1[0][2] = -Math.sin(d);
        ad1[1][0] = 0.0D;
        ad1[1][1] = 1.0D;
        ad1[1][2] = 0.0D;
        ad1[2][0] = -ad1[0][2];
        ad1[2][1] = 0.0D;
        ad1[2][2] = ad1[0][0];
        pv.matrix_matrix_mult(f1, ad1, ad);
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
                f1[i][j] = ad[i][j];

        }

    }

    String PTViewer;
    ptviewer pv;
    Thread loadImages;
    String filename;
    String maskname;
    int width;
    int height;
    int idata[][];
    byte hsdata[][];
    private double f1[][];
    private double f2[][];
    double cpitch;
    double cyaw;
    double croll;
    double chfov;
    int showSHS;
    boolean progress;
    Image zoom_image;
    String hsimage;
    boolean ready;
    double max_oversampling;
    int position;
    boolean antialias;
    Vector scaledImages;
    Vector scaledHSImages;
}