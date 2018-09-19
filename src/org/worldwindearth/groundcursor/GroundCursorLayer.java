package org.worldwindearth.groundcursor;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GroundCursorLayer extends RenderableLayer {

    SurfaceCircle cursor = null;

    /**
     * The tile bounding box
     */
    ExtrudedPolygon tile = null;
    LatLon bl = null;   //--- Bottom left
    LatLon tl = null;   //--- Top left
    LatLon tr = null;   //--- Top right
    LatLon br = null;   //--- Bottom right

    /**
     * The last viewport center postion in world coordinates
     */
    Position center = null;
    int level = 15; //--- Fixed level
    boolean dynamic = false;
    boolean drawTileInfo = false;

    //--- Current x,y tile
    int cx = 0;
    int cy = 0;

    /**
     * Sets fog range/density according to view altitude
     */
    public GroundCursorLayer() {

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

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public void setLevel(int level) {
        this.level = level;
        //--- Force to draw tile
        cx = -1;
        cy = -1;
    }

    public void setDynamicLevel(boolean dynamic) {
        this.dynamic = dynamic;
        cx = -1;
        cy = -1;
    }

    public void setDrawTileInfo(boolean drawTileInfo) {
        this.drawTileInfo = drawTileInfo;
        //--- Force to create the tile
        cx = -1;
        cy = -1;
    }

    //**************************************************************************
    //*** Layer
    //**************************************************************************
    @Override
    public void doRender(DrawContext dc) {
        center = dc.getViewportCenterPosition();
        if (dc.getView() instanceof BasicFlyView) center = Position.ZERO;

        //--- Move cursor to center of viewport
        if (center != null) cursor.moveTo(new Position(center, 0));

        if (drawTileInfo) {
            int zoom = level;
            if (dynamic) {
                /*
                Sector s = dc.getVisibleSector();
                Rectangle2D r = s.toRectangleDegrees();
                // System.out.println("R:" + r);
                double width = r.getWidth();
                double height = r.getHeight();
                 */

                try {
                    Rectangle r = dc.getView().getViewport();
                    Position bl = dc.getView().computePositionFromScreenPoint((r.x/2)-128, (r.y/2)-128);
                    Position br = dc.getView().computePositionFromScreenPoint(256, (r.y/2)-128);
                    double w = br.getLongitude().degrees - bl.getLongitude().degrees;
                    zoom = w2level(w);
                    
                } catch (NullPointerException ex) {

                }
            }
            int x = lon2x(center.getLongitude().degrees, zoom);
            int y = lat2y(center.getLatitude().degrees, zoom);
            if ((cx != x)
                    || (cy != y)) {
                if (tile != null) removeRenderable(tile);
                tile = prepareTileInfo(zoom);
                addRenderable(tile);

            }

        } else {
            if (tile != null) removeRenderable(tile);
            tile = null;
        }

        super.doRender(dc);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    //**************************************************************************
    //*** Private
    //**************************************************************************
    private ExtrudedPolygon prepareTileInfo(int zoom) {
        //--- Create the surface box for tile information
        ExtrudedPolygon tile = new ExtrudedPolygon();
        List<LatLon> list = new ArrayList<LatLon>();
        int x = lon2x(center.getLongitude().degrees, zoom);
        int y = lat2y(center.getLatitude().degrees, zoom);

        double lat1 = y2lat(y + 1, zoom);
        double lon1 = x2lon(x, zoom);
        double lat2 = y2lat(y, zoom);
        double lon2 = x2lon(x + 1, zoom);
        bl = LatLon.fromDegrees(lat1, lon1);       //--- Bottom left
        tl = LatLon.fromDegrees(lat2, lon1);     //--- Top left
        tr = LatLon.fromDegrees(lat2, lon2);  //--- Top right
        br = LatLon.fromDegrees(lat1, lon2);     //--- Bottom right
        list.add(bl);
        list.add(tl);
        list.add(tr);
        list.add(br);
        list.add(bl);
        tile.setOuterBoundary(list);
        tile.setVisible(true);
        tile.setHeight(100d);
        tile.setAltitudeMode(WorldWind.CONSTANT);
        tile.setEnableCap(true);

        ShapeAttributes att = new BasicShapeAttributes();
        att.setInteriorOpacity(0.1d);
        att.setEnableLighting(false);
        att.setOutlineMaterial(Material.BLACK);
        att.setOutlineWidth(1d);
        att.setInteriorMaterial(Material.GREEN);
        att.setDrawInterior(true);
        att.setDrawOutline(false);
        tile.setSideAttributes(att);

        ShapeAttributes cap = new BasicShapeAttributes();
        cap.setInteriorOpacity(0.1d);
        cap.setEnableLighting(false);
        cap.setOutlineMaterial(Material.BLACK);
        cap.setOutlineWidth(1d);
        cap.setInteriorMaterial(Material.GREEN);
        cap.setDrawInterior(true);
        cap.setDrawOutline(true);
        // tile.setCapAttributes(cap);
        BufferedImage tex = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) tex.getGraphics();
        Color t = new Color(0, 255, 0, 128);
        g2.setColor(t);
        g2.fillRect(0, 0, 256, 256);
        g2.setColor(Color.WHITE);
        g2.drawString("" + zoom + "x" + x + "x" + y, 30, 30);
        g2.drawString("Lat TL " + tl.latitude, 30, 60);
        g2.drawString("Lon TL " + tl.longitude, 30, 80);
        g2.drawString("Lat BR " + br.latitude, 30, 100);
        g2.drawString("Lon BR " + br.longitude, 30, 120);
        float[] texCoords = new float[]{0, 0, 1, 0, 1, 1, 0, 1};
        tile.setCapImageSource(tex, texCoords, 4);
        // tile.setCapAttributes(cap);

        cx = x;
        cy = y;
        return tile;
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
     * Convert the passed width (in decimal degrees) to the tile level
     *
     * @param w
     * @return
     */
    public static int w2level(double w) {
        for (int i = 0; i <= 18; i++) {
            int tiles = (int) Math.pow(2, i);
            double oneTile = 360d / tiles;
            if (oneTile <= w) return i;
        }
        return 18;

    }
}
