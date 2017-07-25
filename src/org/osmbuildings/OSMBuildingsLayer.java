/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmbuildings;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * Always assume a zoom level of 15
 *
 * @author sbodmer
 */
public class OSMBuildingsLayer extends RenderableLayer implements OSMBuildingsTileListener, ActionListener,
        SelectListener {

    public static final String CACHE_FOLDER = "Earth" + File.separatorChar + "OSMBuildings";

    public static final int ZOOM = 15;

    public static final double maxX = Math.pow(2, ZOOM);
    public static final double maxY = Math.pow(2, ZOOM);

    // LatLon center = null;
    // SurfacePolygon carpet = null;
    ExtrudedPolygon box = null;

    /**
     * The last viewport center postion in world coordinates
     */
    Position center = null;

    /**
     * The worldwindow for this layer
     */
    WorldWindow ww = null;

    SurfaceCircle cursor = null;

    /**
     * The key is "{level};{x};{y}"
     */
    HashMap<String, OSMBuildingsTile> buildings = new HashMap<String, OSMBuildingsTile>();

    File cacheFolder = null;

    int maxTiles = 10;
    double defaultHeight = 10;
    boolean drawProcessingBox = true;
    boolean drawOutline = false;
    boolean applyRoofTextures = false;

    // Cylinder c = null;
    public OSMBuildingsLayer() {
        super();

        //--- Prepare the screen credits
        ScreenCredit sc = new ScreenCreditImage("OSM Buildings", getClass().getResource("/org/osmbuildings/Resources/Icons/32x32/osmbuildings.png"));
        sc.setLink("http://www.osmbuildings.org");
        sc.setOpacity(1);
        setScreenCredit(sc);

        //--- Default to 30 days
        setExpiryTime(30L * 24L * 60L * 60L * 1000L);

        // System.out.println("ROOT:"+cacheRoot);
        /*
        List<FileStoreDataSet> dataSets = FileStoreDataSet.getDataSets(cacheRoot);
        for (FileStoreDataSet fileStoreDataSet : dataSets) {
            String cacheName = fileStoreDataSet.getName();
            if (cacheName.contains(sourceName)) {
                fileStoreDataSet.delete(false);
                break;
            }
        }
         */
 /*
        ShapeAttributes a4 = new BasicShapeAttributes();
        a4.setInteriorOpacity(1);
        a4.setEnableLighting(true);
        a4.setOutlineMaterial(Material.RED);
        // a4.setOutlineWidth(2d);
        a4.setDrawInterior(true);
        a4.setDrawOutline(false);
         */
 /*
        c = new Cylinder(new Position(LatLon.fromDegrees(0,0),0), 100, 10);
        c.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        c.setAttributes(a4);
        c.setVisible(true);
        addRenderable(c);
         */
 /*
        ArrayList<Position> poss = new ArrayList<>();
        poss.add(Position.fromDegrees(0, 0));
        poss.add(Position.fromDegrees(oneTileX, 0));
        poss.add(Position.fromDegrees(oneTileX, oneTileY));
        poss.add(Position.fromDegrees(0, oneTileY));
        
        carpet = new SurfacePolygon(a4, poss);
        addRenderable(carpet);
         */
 /*
        ShapeAttributes at = new BasicShapeAttributes();
        at.setInteriorMaterial(Material.WHITE);
        // at.setOutlineOpacity(0.5);
        at.setInteriorOpacity(1);
        // at.setOutlineMaterial(Material.GREEN);
        // at.setOutlineWidth(2);
        // at.setDrawOutline(true);
        at.setDrawInterior(true);
        at.setEnableLighting(true);

        ShapeAttributes cap = new BasicShapeAttributes();
        cap.setInteriorMaterial(Material.GRAY);
        cap.setInteriorOpacity(1);
        cap.setDrawInterior(true);
        cap.setEnableLighting(true);

        ArrayList<Position> poss = new ArrayList<>();
        poss.add(Position.fromDegrees(0, 0));
        poss.add(Position.fromDegrees(0, oneTileY));
        poss.add(Position.fromDegrees(oneTileX, oneTileY));
        poss.add(Position.fromDegrees(oneTileX, 0));

        box = new ExtrudedPolygon(10d);
        box.setAltitudeMode(WorldWind.CONSTANT);
        box.setAttributes(at);
        box.setSideAttributes(at);
        box.setCapAttributes(cap);
        box.setVisible(true);
        box.setOuterBoundary(poss);

        addRenderable(box);
         */
 /*
        //--- Prepare the cursor
        ShapeAttributes a1 = new BasicShapeAttributes();
        a1.setInteriorMaterial(Material.GREEN);
        a1.setInteriorOpacity(0.5);
        a1.setEnableLighting(false);
        a1.setOutlineMaterial(Material.BLACK);
        a1.setOutlineWidth(2d);
        a1.setDrawInterior(true);
        a1.setDrawOutline(true);
        cursor = new SurfaceCircle(a1, LatLon.ZERO, 20d);
        cursor.setVisible(true);
        addRenderable(cursor);
         */
    }

    //**************************************************************************
    //*** API
    //*************************************************************************
    public void setDefaultBuildingHeight(double defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    public void clearTiles() {
        Iterator<OSMBuildingsTile> it = buildings.values().iterator();
        while (it.hasNext()) {
                OSMBuildingsTile tile = it.next();
                removeRenderable(tile.getTileSurfaceRenderable());
                //--- Sometime the renerable is not yet received before clearing it
                if (tile.getRenderable() != null) removeRenderable(tile.getRenderable());
                
                
        }
        buildings.clear();
    }

    public void setDrawProcessingBox(boolean draw) {
        this.drawProcessingBox = draw;
    }

    public void setDrawOutline(boolean drawOutline) {
        this.drawOutline = drawOutline;
        for (Renderable r : renderables) {
            if (r instanceof OSMBuildingsRenderable) {
                OSMBuildingsRenderable or = (OSMBuildingsRenderable) r;
                ((OSMBuildingsRenderable) r).setDrawOutline(drawOutline);
            }
        }
    }

    public void setApplyRoofTextures(boolean applyRoofTextures) {
        this.applyRoofTextures = applyRoofTextures;
        
        clearTiles();
    }

    public void setMaxTiles(int maxTiles) {
        this.maxTiles = maxTiles;
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
        clearTiles();
        super.dispose();
    }

    @Override
    public boolean isLayerInView(DrawContext dc) {
        dc.addScreenCredit(getScreenCredit());

        return true;
    }

    /**
     * Fetch the buldings data for the center of the viewport
     *
     * @param dc
     */
    @Override
    public void doRender(DrawContext dc) {
        center = dc.getViewportCenterPosition();
        //--- Move cursor to center of viewport
        // if (center != null) cursor.moveTo(new Position(center, 0));
        super.doRender(dc);
    }

    @Override
    public void doPreRender(DrawContext dc) {
        // System.out.println("doPreRender:" + dc);
        // SectorGeometryList gl = dc.getSurfaceGeometry();
        // Sector s = dc.getVisibleSector();
        // System.out.println("Sector:" + s);
        // LatLon poss[] = s.getCorners();
        // Position center = dc.getViewportCenterPosition();
        // fr.getRight().

        // ArrayList<Position> poss = new ArrayList<>();
        /*
        poss.add(Position.fromDegrees(, 0));
        poss.add(Position.fromDegrees(0, oneTileY));
        poss.add(Position.fromDegrees(oneTileX, oneTileY));
        poss.add(Position.fromDegrees(oneTileX, 0));
         */
        // box.setOuterBoundary(Arrays.asList(s.getCorners()));
        // box.moveTo(new Position(center, 0));
        // Vec4 c = dc.getView().getCenterPoint();
        // System.out.println("CENTER:" + center.getLatitude().degrees + "," + center.getLongitude().degrees);
        // System.out.println("Lat:"+c.getY()+" Lon:"+c.getX()+" Alt:"+c.z);
        /*
        Cylinder c = new Cylinder(Position.fromRadians(center.getLatitude(), oneTileX, oneTileX), maxX, maxX)
        BasicShapeAttributes sa = new BasicShapeAttributes();
        SurfacePolyline sp = new SurfacePolyline(sa, s.asList());
        addRenderable(sp);
         */
        super.doPreRender(dc);
    }

    //**************************************************************************
    //*** MessageListener
    //**************************************************************************
    /**
     * If view stopped,find the osmbuidling tile to fetch at the center of the
     * viewport
     *
     * @param msg
     */
    @Override
    public void onMessage(Message msg) {
        System.out.println("onMessage:" + msg.getName() + " when:" + msg.getWhen() + " source:" + msg.getSource());

        if (View.VIEW_STOPPED.equals(msg.getName()) && (center != null)) {
            if (ww == null) {
                ww = (WorldWindow) msg.getSource();
                // ww.addSelectListener(this);
            }
            /*
            double rx = center.getLongitude().radians;
            double dx = 128 / Math.PI * maxX * (rx + Math.PI);
            int x = (int) dx / 256;

            //--- Rows
            double ry = center.getLatitude().radians;
            double tl = Math.tan(Math.PI / 4d + ry / 2d);
            double dy = 128 / Math.PI * maxY * (Math.PI - Math.log(tl));
            int y = (int) dy / 256;
             */
            int x = lon2x(center.getLongitude().degrees, ZOOM);
            int y = lat2y(center.getLatitude().degrees, ZOOM);
            // System.out.println("X=" + x + ", Y=" + y);

            //--- Take the total of 9 tile
            x--;
            y--;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    //--- Check if max tiles are reached, if so, remove the oldest one
                    if (buildings.size() > maxTiles) {
                        Iterator<OSMBuildingsTile> it = buildings.values().iterator();
                        OSMBuildingsTile oldest = null;
                        while (it.hasNext()) {
                            OSMBuildingsTile t = it.next();
                            if (oldest == null) oldest = t;
                            if (t.getLastUsed() < oldest.getLastUsed()) oldest = t;
                        }
                        Renderable rend = oldest.getRenderable();
                        if (rend != null) removeRenderable(rend);
                        removeRenderable(oldest.getTileSurfaceRenderable());
                        buildings.remove(oldest.toString());
                    }

                    String key = (x + i) + "x" + (y + j) + "@" + ZOOM;
                    OSMBuildingsTile t = buildings.get(key);
                    if (t == null) {
                        t = new OSMBuildingsTile(ZOOM, (x + i), (y + j), this, center, getDataFileStore(), isNetworkRetrievalEnabled(), getExpiryTime(), defaultHeight, applyRoofTextures, produceDefaultShapeAttribute());
                        buildings.put(key, t);
                        t.fetch();
                    }
                    t.tick();
                }
            }
        }
    }

    //**************************************************************************
    //*** OSMBuildingsTileListener
    //**************************************************************************
    @Override
    public void osmBuildingsLoaded(OSMBuildingsTile btile) {
        removeRenderable(btile.getTileSurfaceRenderable());
        addRenderable(btile.getRenderable());
    }

    @Override
    public void osmBuildingsLoading(OSMBuildingsTile btile) {
        //--- Loading in progress, display tile shadow
        if (drawProcessingBox) addRenderable(btile.getTileSurfaceRenderable());
        ww.redrawNow();
    }

    @Override
    public void osmBuildingsLoadingFailed(OSMBuildingsTile btile, String reason) {
        // System.out.println("LOADING FAILED:" + btile+" reason:"+reason);
        removeRenderable(btile.getTileSurfaceRenderable());
        Logging.logger().log(Level.WARNING, "OSMBuildingsLayer.osmBuildingsLoadingFailed for tile " + btile.toString(), new Object[]{reason});
        buildings.remove(btile);

    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        //--- Nothing at the moment
    }

    //**************************************************************************
    //*** SelectListener
    //**************************************************************************
    @Override
    public void selected(SelectEvent event) {
        // System.out.println("EVENT:"+event);
        if (event.isLeftDoubleClick()) {
            System.out.println("Double click on " + event.getTopPickedObject());
        }
    }

    //**************************************************************************
    //*** Private
    //**************************************************************************
    private ShapeAttributes produceDefaultShapeAttribute() {
        BasicShapeAttributes defaultAttrs = new BasicShapeAttributes();
        defaultAttrs.setInteriorMaterial(Material.LIGHT_GRAY);
        defaultAttrs.setInteriorOpacity(getOpacity());
        defaultAttrs.setOutlineMaterial(Material.GRAY);
        // sa.setOutlineOpacity(opacity);
        defaultAttrs.setDrawInterior(true);
        defaultAttrs.setDrawOutline(drawOutline);

        defaultAttrs.setEnableLighting(true);
        defaultAttrs.setEnableAntialiasing(true);
        return defaultAttrs;
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
