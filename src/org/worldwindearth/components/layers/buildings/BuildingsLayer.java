/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components.layers.buildings;

import org.osmbuildings.*;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import osm.map.worldwind.gl.obj.ObjRenderable;

/**
 * Always assume a zoom level of 15
 *
 * @author sbodmer
 */
public class BuildingsLayer extends RenderableLayer implements ActionListener, BuildingsTileListener {

    /**
     * Root of the cache folder, the md5 of the url is added to the path
     */
    public static final String CACHE_FOLDER = "Earth" + File.separatorChar + "Buildings";

    protected ExtrudedPolygon box = null;

    /**
     * The last viewport center postion in world coordinates
     */
    protected Position center = null;

    /**
     * The worldwindow for this layer
     */
    protected WorldWindow ww = null;

    protected SurfaceCircle cursor = null;

    /**
     * The key is "{level};{x};{y}"
     */
    protected HashMap<String, BuildingsTile> buildings = new HashMap<>();
    protected BuildingsTileListener listener = null;

    protected File cacheFolder = null;

    protected int maxTiles = 9;
    protected boolean drawProcessingBox = true;
    protected boolean draggable = false;
    protected int cols = 3;
    protected int rows = 3;
    protected String provider = "file:///usr/share/worldwindearth/buildings/${Z}/${X}/${Y}/buildings.xml";
    protected int minLevel = 15;
    protected int maxLevel = 15;
    protected int lastLevel = 15;

    javax.swing.Timer timer = null;

    public BuildingsLayer() {
        super();

        //--- Prepare the screen credits
        // ScreenCredit sc = new ScreenCreditImage("Buildings", getClass().getResource("/org/worldwindearthbuildings/Resources/Icons/32x32/Buildings.png"));
        // sc.setLink("http://www.osmbuildings.org");
        //  sc.setOpacity(1);
        // setScreenCredit(sc);

        //--- Default to 30 days
        setExpiryTime(30L * 24L * 60L * 60L * 1000L);

        //--- House keeping timer (check if building are not resolved in specific delay)
        timer = new javax.swing.Timer(30000, this);
        timer.start();
    }

    //**************************************************************************
    //*** API
    //*************************************************************************
    public void setBuildingTileListener(BuildingsTileListener listener) {
        this.listener = listener;
    }
    
    /**
     * Warpper for the protected method
     * @param sc 
     */
    public void setScreenCreditImage(ScreenCredit sc) {
        setScreenCredit(sc);
    }
    
    /**
     * To fetch the tile at the given location
     *
     * User 15 as the zoom
     *
     * If the tile could be fetched/produced, null will be returned The method
     * could be long to return (max 60s)
     *
     * @param lat
     * @param lon
     * @return
     */
    public ArrayList<Renderable> fetchTileAt(double lat, double lon, int zoom) {

        int x = lon2x(lon, zoom);
        int y = lat2y(lat, zoom);
        String key = x + "x" + y + "@" + zoom;
        BuildingsTile t = buildings.get(key);
        if (t == null) {
            t = new BuildingsTile(zoom,
                    x,
                    y,
                    this,
                    center,
                    getDataFileStore(),
                    isNetworkRetrievalEnabled(),
                    getExpiryTime(),
                    draggable,
                    provider);
            buildings.put(key, t);
            t.start();
        }
        /*
        try {
            int cnt = 0;
            while (t.isLoaded() == false) {
                Thread.sleep(1000);
                cnt++;
                if (cnt >= 60) break;
            }
            OSMBuildingsRenderable re = t.getRenderable();
            if (re != null) return re.getRenderables();
            
        } catch (InterruptedException ex) {
            //---
            
        }
         */
        return null;

    }

    public void clearTiles() {
        if (listener != null) {
            Iterator<BuildingsTile> it = buildings.values().iterator();
            while (it.hasNext()) {
                BuildingsTile t = it.next();
                if (t.isAlive()) t.interrupt();
                listener.buildingRemoved(t);
            }
        }
        buildings.clear();
        removeAllRenderables();

    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public String getProvider() {
        return provider;
    }

    public void setDrawProcessingBox(boolean draw) {
        this.drawProcessingBox = draw;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
        /*
        for (Renderable r : renderables) {
            if (r instanceof OSMBuildingsRenderable) {
                OSMBuildingsRenderable or = (OSMBuildingsRenderable) r;
                ((OSMBuildingsRenderable) r).setDragEnabled(draggable);
            }
        }
         */
    }

    public void setMaxTiles(int maxTiles) {
        this.maxTiles = maxTiles;
    }

    /**
     * Set the building resolution grid (rows)
     *
     * @param rows
     */
    public void setRows(int rows) {
        this.rows = Math.abs(rows);
        if (maxTiles < (this.rows * this.cols)) maxTiles = this.rows * this.cols;
    }

    /**
     * Set the building resolution grid (cols)
     *
     * @param cols
     */
    public void setCols(int cols) {
        this.cols = Math.abs(cols);
        if (maxTiles < (this.rows * this.cols)) maxTiles = this.rows * this.cols;
    }

    public static int lon2x(double lon, int z) {
        return (int) (Math.floor((lon + 180.0) / 360.0 * Math.pow(2.0, z)));
    }

    public static int lat2y(double lat, int z) {
        return (int) (Math.floor((1.0 - Math.log(Math.tan(lat * Math.PI / 180.0) + 1.0 / Math.cos(lat * Math.PI / 180.0)) / Math.PI) / 2.0 * Math.pow(2.0, z)));
    }

    public static double x2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double y2lat(int y, int z) {
        double n = Math.PI - 2.0 * Math.PI * y / Math.pow(2.0, z);
        return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    }

    public static int w2level(double w) {
        for (int i = 0; i <= 18; i++) {
            int tiles = (int) Math.pow(2, i);
            double oneTile = 360d / tiles;
            if (oneTile <= w) return (i + 2);
        }
        return 18;

    }

    /**
     * Return the quad string for the passed x,y for the given zoom
     *
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    public static String xy2quad(int x, int y, int zoom) {
        StringBuilder quadKey = new StringBuilder();
        for (int i = zoom; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((x & mask) != 0) {
                digit++;
            }
            if ((y & mask) != 0) {
                digit++;
                digit++;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();

    }

    /**
     * Return the x,y and the zoom for the passed quad
     *
     * @param quad
     * @param zoom
     * @return
     */
    public static int[] quad2xy(String quad) {
        int xyz[] = {0, 0, 0};
        xyz[2] = quad.length();
        for (int i = xyz[2]; i > 0; i--) {
            int mask = 1 << (i - 1);
            switch (quad.charAt(xyz[2] - i)) {
                case '0':
                    break;

                case '1':
                    xyz[0] |= mask;
                    break;

                case '2':
                    xyz[1] |= mask;
                    break;

                case '3':
                    xyz[0] |= mask;
                    xyz[1] |= mask;
                    break;

            }
        }
        return xyz;
    }

    //**************************************************************************
    //*** AbstractLayer
    //**************************************************************************
    @Override
    public void setOpacity(double opacity) {
        super.setOpacity(opacity);

        for (Renderable r : renderables) {
            if (r instanceof OSMBuildingsRenderable) {
                OSMBuildingsRenderable or = (OSMBuildingsRenderable) r;
                ((OSMBuildingsRenderable) r).setOpacity(opacity);
            }
        }
    }

    @Override
    public void dispose() {
        timer.stop();
        clearTiles();
        super.dispose();
    }

    @Override
    public boolean isLayerInView(DrawContext dc) {
        ScreenCredit sc = getScreenCredit();
        if (sc != null) dc.addScreenCredit(sc);

        return true;
    }

    /**
     * Fetch the buldings data for the center of the viewport
     *
     * @param dc
     */
    @Override
    public void doRender(DrawContext dc) {
        super.doRender(dc);
        center = dc.getViewportCenterPosition();
        //--- If Fly view, the center is the eye (no center postion available)
        if (dc.getView() instanceof BasicFlyView) {
            center = dc.getView().getCurrentEyePosition();
        }

        
        
    }

    @Override
    public void doPreRender(DrawContext dc) {
        Iterator<BuildingsTile> it = buildings.values().iterator();
        while (it.hasNext()) {
            BuildingsTile t = it.next();
            if (t.isLoaded()) {
                //--- For each building, load the .obj file
                ArrayList<Building> list = t.getBuildings();
                for (int i = 0; i < list.size(); i++) {
                    Building b = list.get(i);
                    if (b.getRenderable() == null) {
                        if (b.getFile().getName().toLowerCase().endsWith(".obj")) {
                            Position pos = Position.fromDegrees(b.getLatitude(), b.getLongitude(), b.getAltitude());
                            ObjRenderable r = new ObjRenderable(pos, b.getFile().getPath(), true, false, null);
                            b.setRenderable(r);
                            r.setPosition(pos);
                            r.setVisible(b.isVisible());
                            r.setAzimuth(b.getAzimuth());
                            r.setElevation(b.getElevation());
                            r.setRoll(b.getRoll());
                            r.setSize(b.getScale());
                            addRenderable(b.getRenderable());
                            
                        }
                    }

                }

            }

        }

        super.doPreRender(dc);
    }

    //**************************************************************************
    //*** MessageListener
    //**************************************************************************
    /**
     * If view stopped,find the buidling tile to fetch at the center of the
     * viewport
     *
     * @param msg
     */
    @Override
    public void onMessage(Message msg) {
        // System.out.println("onMessage:" + msg.getName() + " when:" + msg.getWhen() + " source:" + msg.getSource());

        if (View.VIEW_STOPPED.equals(msg.getName()) && (center != null)) {
            if (ww == null) {
                ww = (WorldWindow) msg.getSource();
                // ww.addSelectListener(this);
            }

            //--- Find current view tile level
            int zoom = 15;
            try {
                //--- Is there a better way ?
                Rectangle r = ww.getView().getViewport();
                Position bl = ww.getView().computePositionFromScreenPoint(r.x, r.y / 2);
                Position br = ww.getView().computePositionFromScreenPoint(r.width, r.y / 2);
                double w = br.getLongitude().degrees - bl.getLongitude().degrees;
                zoom = w2level(w);

            } catch (NullPointerException ex) {
                //---
            }

            // System.out.println("Zoom:" + zoom);
            if (zoom < minLevel) zoom = minLevel;
            if (zoom > maxLevel) zoom = maxLevel;
            if (lastLevel != zoom) clearTiles();
            lastLevel = zoom;
            int x = lon2x(center.getLongitude().degrees, zoom);
            int y = lat2y(center.getLatitude().degrees, zoom);
            // System.out.println("X=" + x + ", Y=" + y);

            //--- Take the total of x tile
            x = x - (rows / 2);
            y = y - (rows / 2);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    //--- Check if max tiles are reached, if so, remove the oldest one
                    if (buildings.size() > maxTiles) {
                        Iterator<BuildingsTile> it = buildings.values().iterator();
                        BuildingsTile oldest = null;
                        while (it.hasNext()) {
                            BuildingsTile t = it.next();
                            if (oldest == null) oldest = t;
                            if (t.getLastUsed() < oldest.getLastUsed()) oldest = t;
                        }
                        ArrayList<Building> list = oldest.getBuildings();
                        for (int k = 0; k < list.size(); k++) {
                            Renderable rend = list.get(k).getRenderable();
                            if (rend != null) removeRenderable(rend);
                        }
                        buildings.remove(oldest.toString());
                        if (listener != null) listener.buildingRemoved(oldest);
                    }

                    String key = (x + i) + "x" + (y + j) + "@" + zoom;
                    BuildingsTile t = buildings.get(key);
                    if (t == null) {
                        t = new BuildingsTile(zoom,
                                (x + i),
                                (y + j),
                                this,
                                center,
                                getDataFileStore(),
                                isNetworkRetrievalEnabled(),
                                getExpiryTime(),
                                draggable,
                                provider);
                        buildings.put(key, t);
                        t.start();
                    }
                    t.tick();
                }
            }
        }
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            long now = System.currentTimeMillis();
            ArrayList<BuildingsTile> tooLong = new ArrayList<>();
            Iterator<String> it = buildings.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                BuildingsTile t = buildings.get(key);

                if (t.isLoaded() == false) {
                    long load = t.getFetchedTimestamp();
                    long diff = now - load;
                    // System.out.println("["+key+"]="+load+", diff="+diff+" renderable=false");
                    // System.out.println("DIFF:"+diff+" (load:"+load+" now:"+now+")");
                    if (diff > 30000) {
                        //--- Loading too long, consider failed
                        tooLong.add(t);
                    }
                }

            }
            for (int i = 0; i < tooLong.size(); i++) {
                BuildingsTile t = tooLong.get(i);
                t.interrupt();
                buildingLoadingFailed(t, "No failed message received after 30s, force to failed loading...");
            }
        }
    }

    //**************************************************************************
    //*** BuildingTileListener
    //**************************************************************************
    @Override
    public void buildingLoaded(BuildingsTile btile) {
        // System.out.println("BUILDINGTILE loaded:" + btile);
        if (listener != null) listener.buildingLoaded(btile);

    }

    @Override
    public void buildingLoading(BuildingsTile btile, long current, long total) {
        // System.out.println("BUILDINGTILE loading:" + btile + " (" + current + "/" + total);
        if (listener != null) listener.buildingLoading(btile, current, total);
    }

    @Override
    public void buildingLoadingFailed(BuildingsTile btile, String reason) {
        // System.out.println("BUILDINGTILE loading failed:" + btile + " " + reason);
        if (listener != null) listener.buildingLoadingFailed(btile, reason);
    }

    @Override
    public void buildingRemoved(BuildingsTile btile) {
        // System.out.println("BUILDINGTILE loading failed:" + btile + " " + reason);
        if (listener != null) listener.buildingRemoved(btile);
    }

    //**************************************************************************
    //*** Debug
    //**************************************************************************
    public static void main(String args[]) {
        // double lat = 52.20276987984823d;
        double lat = 46.1935;
        double lon = 6.129;
        // double lat = 0;

        int level = 15;
        int maxY = 1 << level;
        int maxX = 1 << level;
        double oneY = 180d / maxY;

        //--- https://en.wikipedia.org/wiki/Web_Mercator
        /*
        System.out.println("MAX:" + maxY);
        System.out.println("ONE:" + oneY);
        int y = (int) ((maxY * (lat + 90d)) / 180d);

        System.out.println("Y  :" + y);
        System.out.println("D  :" + (maxY - y));

        double plat = Math.log(Math.tan((Math.PI/4)+(lat/2)));
        System.out.println("PLAT:"+plat);
         */
        //--- Cols (x)
        double rx = Math.toRadians(lon);
        double x = 128 / Math.PI * maxX * (rx + Math.PI);
        System.out.println("COLS:" + (x / 256));

        //--- Rows
        double ry = Math.toRadians(lat);
        double tl = Math.tan(Math.PI / 4d + ry / 2d);
        double y = 128 / Math.PI * maxY * (Math.PI - Math.log(tl));
        System.out.println("ROWS:" + (y / 256));
    }

}
