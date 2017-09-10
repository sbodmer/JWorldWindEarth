/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmbuildings;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.formats.geojson.GeoJSONDoc;
import gov.nasa.worldwind.formats.geojson.GeoJSONFeature;
import gov.nasa.worldwind.formats.geojson.GeoJSONFeatureCollection;
import gov.nasa.worldwind.formats.geojson.GeoJSONGeometry;
import gov.nasa.worldwind.formats.geojson.GeoJSONGeometryCollection;
import gov.nasa.worldwind.formats.geojson.GeoJSONMultiLineString;
import gov.nasa.worldwind.formats.geojson.GeoJSONMultiPoint;
import gov.nasa.worldwind.formats.geojson.GeoJSONMultiPolygon;
import gov.nasa.worldwind.formats.geojson.GeoJSONObject;
import gov.nasa.worldwind.formats.geojson.GeoJSONPoint;
import gov.nasa.worldwind.formats.geojson.GeoJSONPolygon;
import gov.nasa.worldwind.formats.geojson.GeoJSONPositionArray;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Sphere;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWMath;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.worldwindearth.WWE;

/**
 * The renderable of the GeoJSONDoc tile for OSM Buildings
 *
 * Multiple buildings are stored here
 *
 * @author sbodmer
 */
public class OSMBuildingsRenderable implements Renderable, PreRenderable, Disposable {

    /**
     * The tile comment (like the file/url of the tile)
     */
    public static String AVKEY_OSMBUILDING_COMMENT = "org.osmbuildings.Comment";

    /**
     * The feature id which is the parent of the renderable
     */
    public static String AVKEY_OSMBUILDING_FEATURE_ID = "org.osmbuildings.featureId";

    /**
     * The polygin has inner bounds (so texture map should be different)
     *
     */
    public static String AVKEY_OSMBUILDING_HAS_INNER_BOUNDS = "org.osmbuildings.hasInnerBounds";

    protected static final Map<String, String> COLORS = new HashMap<String, String>();

    protected static Random random = new Random();

    // protected PickSupport pickSupport = new PickSupport();
    /**
     * Default height of buildings
     */
    protected double defaultHeight = 10;

    /**
     * Some comment (like the x,y value of the tile), useful for debug
     */
    protected String comment = "";
    protected ShapeAttributes defaultAttrs = null;

    /**
     * Listener to check some values before the rendering
     */
    protected OSMBuildingsTileListener listener = null;

    /**
     * The list of renderable for this GeoJSON object (PointPlacemarks,
     * ExtrudePolygon, ...)
     */
    private List<Renderable> renderables = new ArrayList<Renderable>();

    /**
     * The list of the osm ids rendered in this object (to avoid multiple
     * rendering from other tile)
     */
    private ArrayList<String> ids = new ArrayList<>();

    static {
        COLORS.put("lightbrown", "#ac6b25");
        COLORS.put("yellowbrown", "#bb9613");
        COLORS.put("darkbrown", "#654321");
        COLORS.put("redbrown", "#a52a2a");
        COLORS.put("cream", "#ffffcc");
        COLORS.put("brickred", "#cb4154");
        COLORS.put("bluegreen", "#0d98ba");

    }

    /**
     * If the height is 0, then no building are extruded
     *
     * @param doc
     * @param defaultHeight
     * @param opacity
     */
    public OSMBuildingsRenderable(final GeoJSONDoc doc, final double defaultHeight, final ShapeAttributes defaultAttrs, final String comment, final OSMBuildingsTileListener listener) {
        this.defaultHeight = defaultHeight;
        this.defaultAttrs = defaultAttrs;
        this.comment = comment;
        this.listener = listener;

        // --- Prepare the renderable
        if (doc.getRootObject() instanceof GeoJSONObject) {
            GeoJSONObject obj = (GeoJSONObject) doc.getRootObject();
            prepare(obj);

        } else if (doc.getRootObject() instanceof Object[]) {
            for (Object o : (Object[]) doc.getRootObject()) {
                if (o instanceof GeoJSONObject) {
                    prepare((GeoJSONObject) o);

                }
            }

        } else {
            // ---
        }
    }

    @Override
    public String toString() {
        return "Contains " + renderables.size() + " elements to render (" + comment + ")";
    }

    // **************************************************************************
    // *** API
    // **************************************************************************
    public void clear() {
        renderables.clear();
        ids.clear();
    }

    /**
     * Return the original list of rendered ids
     *
     * @return
     */
    public ArrayList<String> getIds() {
        return ids;
    }

    /**
     * Set the opacity of the current generated renderables dynamically<p>
     *
     * @param opacity
     */
    public void setOpacity(double opacity) {
        defaultAttrs.setInteriorOpacity(opacity);

        for (Renderable renderable : renderables) {
            if (renderable instanceof Polygon) {
                Polygon polygon = (Polygon) renderable;
                polygon.getAttributes().setInteriorOpacity(opacity);
                // polygon.getAttributes().setOutlineOpacity(opacity);

            } else if (renderable instanceof ExtrudedPolygon) {
                ExtrudedPolygon polygon = (ExtrudedPolygon) renderable;
                polygon.getAttributes().setInteriorOpacity(opacity);
                // polygon.getAttributes().setOutlineOpacity(opacity);
                polygon.getSideAttributes().setInteriorOpacity(opacity);
                // polygon.getSideAttributes().setOutlineOpacity(opacity);

            } else {
                // System.out.println("setOpacity not handled on :" + renderable);
            }
        }

    }

    /**
     * Dynamically changes the current outlines
     *
     * @param draw
     */
    public void setDrawOutline(boolean draw) {
        defaultAttrs.setDrawOutline(draw);

        for (Renderable renderable : renderables) {
            if (renderable instanceof Polygon) {
                Polygon polygon = (Polygon) renderable;
                polygon.getAttributes().setDrawOutline(draw);

            } else if (renderable instanceof ExtrudedPolygon) {
                ExtrudedPolygon polygon = (ExtrudedPolygon) renderable;
                polygon.getAttributes().setDrawOutline(draw);
                polygon.getSideAttributes().setDrawOutline(draw);

            } else {
                // System.out.println("setOpacity not handled on :" + renderable);
            }
        }

    }

    /**
     * Apply the bing roof image to the renderable objects (the passed sector in
     * the current tile in world coordinate to use for texture mapping)
     * <p>
     *
     * @param tex
     * @param s
     */
    public void applyRoofTexture(BufferedImage tex, Sector sector) {
        for (Renderable renderable : renderables) {
            if (renderable instanceof Polygon) {
                Polygon polygon = (Polygon) renderable;
                // polygon.getAttributes().setDrawOutline(draw);

            } else if (renderable instanceof ExtrudedPolygon) {
                ExtrudedPolygon polygon = (ExtrudedPolygon) renderable;
                boolean innerBounds = (boolean) polygon.getValue(AVKEY_OSMBUILDING_HAS_INNER_BOUNDS);
                if (innerBounds) {
                    //--- Well inner bounds exists, no idea what to do with the roof

                } else {
                    Iterator<LatLon> it = (Iterator<LatLon>) polygon.getOuterBoundary().iterator();
                    double dlat = sector.getDeltaLatDegrees();
                    double dlon = sector.getDeltaLonDegrees();
                    ArrayList<Float> texCoord = new ArrayList<>();
                    int cnt = 0;
                    ShapeAttributes att = polygon.getCapAttributes();
                    att.setImageSource(tex);
                    while (it.hasNext()) {
                        LatLon ll = it.next();
                        double t = (ll.getLatitude().degrees - sector.getMinLatitude().degrees) / dlat;
                        double s = (ll.getLongitude().degrees - sector.getMinLongitude().degrees) / dlon;
                        texCoord.add((float) s);
                        texCoord.add((float) t);
                        cnt++;
                    }
                    float tc[] = new float[texCoord.size()];
                    for (int i = 0; i < tc.length; i++) tc[i] = texCoord.get(i);
                    polygon.setCapImageSource(tex, tc, cnt);
                }

            } else {
                //---
            }
        }
    }

    // **************************************************************************
    // *** Renderable
    // **************************************************************************
    @Override
    public void render(DrawContext dc) {
        for (Renderable r : renderables) {
            r.render(dc);
        }

    }

    @Override
    public void preRender(DrawContext dc) {
        for (Renderable r : renderables) {
            if (r instanceof PreRenderable)
                ((PreRenderable) r).preRender(dc);
        }
    }

    // **************************************************************************
    // *** Disposable
    // **************************************************************************
    @Override
    public void dispose() {
        for (Renderable r : renderables) {
            if (r instanceof Disposable)
                ((Disposable) r).dispose();
        }
        renderables.clear();
    }

    // **************************************************************************
    // *** Private
    // **************************************************************************
    /**
     * Create the basic shape for the rendering<p>
     *
     * Check if the feature id is already rendered, if so, do nothing
     *
     * @param object
     */
    public void prepare(GeoJSONObject object) {

        if (object.isGeometry()) {
            fill(null, object.asGeometry(), null);

        } else if (object.isFeature()) {
            GeoJSONFeature f = object.asFeature();
            String id = f.getValue("id").toString();
            boolean r = listener.osmBuildingsProduceRenderableForId(id);
            if (r) {
                fill(f, f.getGeometry(), f.getProperties());
                ids.add(id);
            }

        } else if (object.isFeatureCollection()) {
            GeoJSONFeatureCollection c = object.asFeatureCollection();
            for (GeoJSONFeature f : c.getFeatures()) {
                String id = f.getValue("id").toString();
                boolean r = listener.osmBuildingsProduceRenderableForId(id);
                if (r) {
                    fill(f, f.getGeometry(), f.getProperties());
                    ids.add(id);
                }

            }
        }
    }

    /**
     * The feature and properties could be null
     *
     * @param f
     * @param geom
     * @param properties
     */
    protected void fill(GeoJSONFeature f, GeoJSONGeometry geom, AVList properties) {
        if (geom.isPoint()) {
            GeoJSONPoint pt = geom.asPoint();
            PointPlacemarkAttributes pa = new PointPlacemarkAttributes();
            fillRenderablePoint(f, pt, pt.getPosition(), pa, properties);

        } else if (geom.isMultiPoint()) {
            GeoJSONMultiPoint mp = geom.asMultiPoint();
            PointPlacemarkAttributes pa = new PointPlacemarkAttributes();
            for (int i = 0; i < mp.getPointCount(); i++) {
                fillRenderablePoint(f, mp.asPoint(), mp.getPosition(i), pa, properties);
            }

        } else if (geom.isLineString()) {
            String msg = Logging.getMessage("Geometry rendering of line not supported");
            Logging.logger().warning(msg);
            // this.addRenderableForLineString(geom.asLineString(), layer, properties);

        } else if (geom.isMultiLineString()) {
            GeoJSONMultiLineString ms = geom.asMultiLineString();
            BasicShapeAttributes sa = new BasicShapeAttributes();
            sa.copy(defaultAttrs);
            fillShapeAttribute(sa, properties);
            for (GeoJSONPositionArray coords : ms.getCoordinates()) {
                fillRenderablePolyline(f, geom, coords, sa, properties);
            }

        } else if (geom.isPolygon()) {
            GeoJSONPolygon poly = geom.asPolygon();
            BasicShapeAttributes sa = new BasicShapeAttributes();
            sa.copy(defaultAttrs);
            fillShapeAttribute(sa, properties);
            // dumpAVList(properties);
            fillRenderablePolygon(f, poly, poly.getExteriorRing(), poly.getInteriorRings(), sa, properties);

        } else if (geom.isMultiPolygon()) {
            GeoJSONMultiPolygon mpoly = geom.asMultiPolygon();
            BasicShapeAttributes sa = new BasicShapeAttributes();
            sa.copy(defaultAttrs);
            fillShapeAttribute(sa, properties);
            for (int i = 0; i < mpoly.getPolygonCount(); i++) {
                fillRenderablePolygon(f, mpoly.asPolygon(), mpoly.getExteriorRing(i), mpoly.getInteriorRings(i), sa, properties);
            }

        } else if (geom.isGeometryCollection()) {
            GeoJSONGeometryCollection c = geom.asGeometryCollection();
            GeoJSONGeometry geos[] = c.getGeometries();
            for (int i = 0; i < geos.length; i++) {
                fill(f, geos[i], properties);
            }

        } else {
            String msg = Logging.getMessage("Geometry not supported");
            Logging.logger().warning(msg);
        }
    }

    /**
     * Create a PointPlacemark
     *
     * @param properties
     */
    protected void fillRenderablePoint(GeoJSONFeature parent, GeoJSONPoint owner, Position pos, PointPlacemarkAttributes attrs, AVList properties) {
        PointPlacemark p = new PointPlacemark(pos);
        p.setAttributes(attrs);
        if (pos.getAltitude() != 0) {
            p.setAltitudeMode(WorldWind.ABSOLUTE);
            p.setLineEnabled(true);

        } else {
            p.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        }

        if (properties != null) p.setValue(AVKey.PROPERTIES, properties);
        p.setValue(AVKEY_OSMBUILDING_COMMENT, comment);
        p.setValue(AVKEY_OSMBUILDING_FEATURE_ID, parent != null ? parent.getValue("id") : "");
        renderables.add(p);
    }

    /**
     * Prepare the polygon with color if present, the points are always on the
     * same plane
     * <p>
     * If the polygon has an altitude, do not extrude it
     * <p>
     * <p>
     * If the polygon properties has a height use it for extrude
     * <p>
     * <p>
     * If the the default height > 0 , then extrude the polygon if no other
     * height is found
     * <p>
     *
     * @param owner
     * @param outerBoundary
     * @param innerBoundaries
     * @param attrs
     * @param properties
     */
    protected void fillRenderablePolygon(GeoJSONFeature parent, GeoJSONPolygon owner, Iterable<? extends Position> outerBoundary, Iterable<? extends Position>[] innerBoundaries, ShapeAttributes attrs, AVList properties) {
        if (hasNonzeroAltitude(outerBoundary)) {
            // --- It's a ploygon with height (not a flat foot print)
            Polygon poly = new Polygon(outerBoundary);
            poly.setValue(AVKEY_OSMBUILDING_HAS_INNER_BOUNDS, false);
            poly.setAttributes(attrs);
            if (innerBoundaries != null) {
                for (Iterable<? extends Position> iter : innerBoundaries) poly.addInnerBoundary(iter);
                poly.setValue(AVKEY_OSMBUILDING_HAS_INNER_BOUNDS, true);
            }

            if (properties != null) poly.setValue(AVKey.PROPERTIES, properties);
            poly.setValue(AVKEY_OSMBUILDING_COMMENT, comment);
            poly.setValue(AVKEY_OSMBUILDING_FEATURE_ID, parent != null ? parent.getValue("id") : "");
            renderables.add(poly);

        } else if (defaultHeight > 0) {
            // --- The polygon should be a volume
            double height = 0;
            double minHeight = 0;
            double levels = 0;
            double minLevels = 0;
            String roofColor = "gray";
            String roofShape = "flat";
            double roofHeight = 0;
            String roofMaterial = "concrete";
            String roofOrientation = "along";
            String roofDirection = "";
            if (properties != null) {
                if (properties.getValue("height") != null) height = (Double) properties.getValue("height");
                if (properties.getValue("levels") != null) levels = (Double) properties.getValue("levels");
                if (properties.getValue("minHeight") != null) minHeight = (Double) properties.getValue("minHeight");
                // --- Roof
                if (properties.getValue("roofColor") != null) roofColor = (String) properties.getValue("roofColor");
                if (properties.getValue("roofShape") != null) roofShape = (String) properties.getValue("roofShape");
                if (properties.getValue("roofHeight") != null) roofHeight = (Double) properties.getValue("roofHeight");
                if (properties.getValue("roofMaterial") != null) roofMaterial = (String) properties.getValue("roofMaterial");
                if (properties.getValue("roofOrientation") != null) roofOrientation = (String) properties.getValue("roofOrientation");
                if (properties.getValue("roofDirection") != null) roofDirection = (String) properties.getValue("roofDirection");

            }
            if (roofColor == null) roofColor = "gray";

            // --- Check if height is correct
            if (height <= 0) {
                // --- Sometimes level are set, but no height
                // --- Consider 1 level to be 4 meters
                height = (levels == 0 ? defaultHeight : levels * 4);
                minHeight = 0;

            } else if (minHeight >= height) {
                // height = minHeight + 1;
                minHeight = 0;
            }

            // ShapeAttributes at = new BasicShapeAttributes();
            // attrs.setInteriorMaterial(Material.WHITE);
            // at.setOutlineOpacity(0.5);
            // attrs.setInteriorOpacity(1);
            // at.setOutlineMaterial(Material.GREEN);
            // at.setOutlineWidth(2);
            // attrs.setDrawOutline(false);
            // attrs.setDrawInterior(true);
            // attrs.setEnableLighting(true);
            // --- Roof cap
            ShapeAttributes ra = new BasicShapeAttributes();
            ra.copy(attrs);
            ra.setInteriorMaterial(new Material(stringToColor(roofColor)));
            ra.setOutlineMaterial(new Material(stringToColor(roofColor)));
            // ra.setOutlineMaterial(Material.RED);
            // ra.setInteriorOpacity(roofMaterial.equals("glass") ? 0.7 : 1.0d);
            // ra.setOutlineOpacity(roofMaterial.equals("glass") ? 0.7 : 1.0d);
            ra.setDrawInterior(true);
            ra.setEnableLighting(true);
            ra.setDrawOutline(false);
            ra.setEnableAntialiasing(true);

            // --- If levels, try some texture on it
            /*
            BufferedImage tex = null;
            if (levels > 0) {
                tex = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = (Graphics2D) tex.getGraphics();
                int floor = 100 / (int) (height / levels);

                g2.setColor(new Color(255, 0, 00));
                for (int i = 0; i < levels; i++) {
                    g2.fillRect(10, (floor * i) + 1, 80, floor - 2);
                }
                System.out.println("LEVELS:" + levels);
                // ra.setImageSource(tex);
            }
             */
            // --- Walls with default cap (flat roof)
            ExtrudedPolygon box = new ExtrudedPolygon(height);
            box.setValue(AVKEY_OSMBUILDING_HAS_INNER_BOUNDS, false);
            box.setAltitudeMode(WorldWind.CONSTANT);
            // box.setAttributes(attrs);
            box.setSideAttributes(attrs);
            box.setCapAttributes(ra);
            // box.setCapImageSource(roofShape, , 0);
            box.setVisible(true);
            box.setOuterBoundary(outerBoundary);
            box.setBaseDepth(-minHeight); // --- negative value will push the base up instead of below
            if (innerBoundaries != null) {
                for (Iterable<? extends Position> iter : innerBoundaries) {
                    box.addInnerBoundary(iter);
                }
                box.setValue(AVKEY_OSMBUILDING_HAS_INNER_BOUNDS, true);
            }

            //--- FOR DEBUG, DISPLAY ONLY NOT FLAT ROOF
            // if (roofShape.equals("pyramid") && (minHeight > 100)) renderables.add(box);
            renderables.add(box);

            //--- Handle roof
            if (roofShape.equals("pyramid") || roofShape.equals("pyramidal")) {
                // System.out.println("ROOF is " + roofShape + " HEIGHT:" + height + " MIN:" + minHeight);
                roofHeight = roofHeight == 0 ? 2 : roofHeight;

                // --- Pyramid, first and last corner is the same
                ArrayList<Position> corners = new ArrayList<>();
                Iterator<? extends Position> it = outerBoundary.iterator();
                double lats = 0.0;
                double lons = 0.0;
                while (it.hasNext()) {
                    Position p = it.next();
                    lats += p.latitude.degrees;
                    lons += p.longitude.degrees;
                    corners.add(p);
                }

                //--- Take median values for the center of the pyramid
                double centerLats = lats / corners.size();
                double centerLon = lons / corners.size();
                Position center = Position.fromDegrees(centerLats, centerLon, roofHeight);

                for (int i = 0; i < corners.size(); i++) {
                    if (i == corners.size() - 1) break;

                    Position p = corners.get(i);
                    ArrayList<Position> list = new ArrayList<>();
                    list.add(corners.get(i));
                    list.add(corners.get(i + 1));
                    list.add(center);
                    Polygon polygon = new Polygon(list);
                    polygon.setAttributes(ra);
                    polygon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                    polygon.move(Position.fromDegrees(0, 0, height));

                    if (properties != null) polygon.setValue(AVKey.PROPERTIES, properties);
                    polygon.setValue(AVKEY_OSMBUILDING_COMMENT, comment);
                    polygon.setValue(AVKEY_OSMBUILDING_FEATURE_ID, parent != null ? parent.getValue("id") : "");
                    renderables.add(polygon);
                }

                // for (int i=0;i<corners.size();i++) System.out.println("["+i+"] "+corners.get(i));

                /*
                 * Polygon roof = new Polygon(outerBoundary);
                 * Position ref = outerBoundary.iterator().next();
                 * Position nref = Position.fromDegrees(ref.getLatitude().degrees,ref.getLongitude().degrees, height); roof.setReferencePosition(nref); roof.setAttributes(ra); renderables.add(roof);
                 */
            } else if (roofShape.equals("gabled")) {
                roofHeight = roofHeight == 0 ? 2 : roofHeight;

            } else if (roofShape.equals("hipped")) {

            } else if (roofShape.equals("flat")) {

            } else if (roofShape.equals("dome")) {
                roofHeight = (roofHeight == 0 ? 2 : roofHeight);

                //--- Find corners
                Iterator<? extends Position> it = outerBoundary.iterator();
                double minLat = 90.0;
                double minLon = 180.0;
                double maxLat = -90.0;
                double maxLon = -180.0;
                while (it.hasNext()) {
                    Position p = it.next();
                    double lat = p.latitude.degrees;
                    double lon = p.longitude.degrees;
                    if (lat < minLat) minLat = lat;
                    if (lat > maxLat) maxLat = lat;
                    if (lon < minLon) minLon = lon;
                    if (lon > maxLon) maxLon = lon;

                }
                double dLat = maxLat - minLat;
                double dLon = maxLon - minLon;
                double centerLat = minLat + (dLat / 2);
                double centerLon = minLon + (dLon / 2);
                double dX = WWE.getDistance(minLat, minLon, minLat, maxLon);
                double dY = WWE.getDistance(minLat, minLon, maxLat, minLon);
                Position center = Position.fromDegrees(centerLat, centerLon);
                Ellipsoid dome = new Ellipsoid(center, dX/2, roofHeight, dY/2);
                dome.setAttributes(ra);
                dome.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                dome.move(Position.fromDegrees(0, 0, height));
                if (properties != null) dome.setValue(AVKey.PROPERTIES, properties);
                dome.setValue(AVKEY_OSMBUILDING_COMMENT, comment);
                dome.setValue(AVKEY_OSMBUILDING_FEATURE_ID, parent != null ? parent.getValue("id") : "");
                renderables.add(dome);
                
            } else if (roofShape.equals("round")) {
                /*
                roofHeight = roofHeight == 0 ? 2 : roofHeight;
                Iterable<? extends LatLon> iter = box.getOuterBoundary();
                
                System.out.println("BOX:"+box);
                Extent ext = box.getExtent();
                if (ext != null) {
                    System.out.println("EXTENT:" + ext);
                }
                 */
                // Cylinder c = new Cylinder();

            } else {
                // System.out.println("ROOF is " + roofShape);
            }

            if (properties != null) box.setValue(AVKey.PROPERTIES, properties);
            box.setValue(AVKEY_OSMBUILDING_COMMENT, comment);
            box.setValue(AVKEY_OSMBUILDING_FEATURE_ID, parent != null ? parent.getValue("id") : "");

        } else {
            //--- Display the foot print
            SurfacePolygon poly = new SurfacePolygon(attrs, outerBoundary);
            if (innerBoundaries != null) {
                for (Iterable<? extends Position> iter : innerBoundaries) {
                    poly.addInnerBoundary(iter);
                }
            }

            if (properties != null) poly.setValue(AVKey.PROPERTIES, properties);
            poly.setValue(AVKEY_OSMBUILDING_COMMENT, comment);
            poly.setValue(AVKEY_OSMBUILDING_FEATURE_ID, parent != null ? parent.getValue("id") : "");
            renderables.add(poly);
        }
    }

    protected void fillRenderablePolyline(GeoJSONFeature parent, GeoJSONGeometry owner, Iterable<? extends Position> positions, ShapeAttributes attrs, AVList properties) {
        if (hasNonzeroAltitude(positions)) {
            Path p = new Path();
            p.setPositions(positions);
            p.setAltitudeMode(WorldWind.ABSOLUTE);
            p.setAttributes(attrs);

            if (properties != null) p.setValue(AVKey.PROPERTIES, properties);
            p.setValue(AVKEY_OSMBUILDING_COMMENT, comment);
            p.setValue(AVKEY_OSMBUILDING_FEATURE_ID, parent != null ? parent.getValue("id") : "");
            renderables.add(p);

        } else {
            SurfacePolyline sp = new SurfacePolyline(attrs, positions);
            if (properties != null) sp.setValue(AVKey.PROPERTIES, properties);
            sp.setValue(AVKEY_OSMBUILDING_COMMENT, comment);
            sp.setValue(AVKEY_OSMBUILDING_FEATURE_ID, parent != null ? parent.getValue("id") : "");
            renderables.add(sp);

        }
    }

    /**
     * Check if a position has an altitude (!= 0)
     * <p>
     *
     * @param positions
     *
     * @return
     */
    protected static boolean hasNonzeroAltitude(Iterable<? extends Position> positions) {
        for (Position pos : positions) {
            if (pos.getAltitude() != 0)
                return true;
        }
        return false;
    }

    protected void dumpAVList(AVList av) {
        if (av == null) return;
        Set<Map.Entry<String, Object>> set = av.getEntries();
        Iterator<Map.Entry<String, Object>> it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> e = it.next();
            System.out.println("" + e.getKey() + "=" + e.getValue().toString() + " " + e.getValue().getClass().getName());
        }
    }

    /**
     * Will fill the passed shape attribute with the properties
     *
     * <PRE>
     * "color" will be processed (if none found, WHITE is used)
     * </PRE>
     *
     * @param sa
     * @param properties
     */
    private void fillShapeAttribute(ShapeAttributes sa, AVList properties) {
        //--- Use a default random gray scale
        int r = random.nextInt(16);
        String v = "#";
        for (int i = 0; i < 6; i++) v += Integer.toHexString(r);
        if (properties != null) {
            v = properties.getStringValue("color");
            String mat = properties.getStringValue("material");
            if (mat == null) mat = "concrete";
            if (mat.equals("glass")) sa.setDrawOutline(true);
        }
        if (v == null) v = "#bbbbbb";   //--
        sa.setInteriorMaterial(new Material(stringToColor(v)));
        /*
        sa.setInteriorOpacity(opacity);
        sa.setOutlineMaterial(Material.GRAY);
        // sa.setOutlineOpacity(opacity);
        sa.setDrawInterior(true);
        sa.setDrawOutline(true);

        sa.setEnableLighting(true);
        sa.setEnableAntialiasing(true);
         */
        // sa.setOutlineMaterial(new Material(stringToColor(v)));
    }

    /**
     * Convert hex value via javafx helpers class
     *
     * @param value
     * @return
     */
    private Color stringToColor(String value) {
        if (value == null) return Color.LIGHT_GRAY;

        // Clean name for FX
        value = value.replace(" ", "").replace("-", "").replace("_", "").toLowerCase();

        // In COLORS not handled by FX ? (yet)
        String hex = COLORS.get(value);
        if (hex != null) return Color.decode(hex);

        // Get the color using FX (the most complete set)
        try {
            javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.valueOf(value);
            if (fxColor == null) {
                System.out.println("Color not found:" + value);
                return Color.LIGHT_GRAY;
            }

            return new Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue(), (float) fxColor.getOpacity());

        } catch (Exception e) {
            System.out.println("Color not found:" + value);
            return Color.LIGHT_GRAY;
        }
    }

    public static final void main(String args[]) {
        double l1 = -50;
        double l2 = -100;
        System.out.println("" + l1 + ">" + l2 + " = " + (l1 > l2));
    }
}
