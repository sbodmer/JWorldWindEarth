/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components.layers.buildings;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.osmbuildings.OSMBuildingsLayer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents the content of the buildings.xml file
 *
 * List of models, paths
 *
 * The building.xml if fetched, then for each model, the data will be fetched
 *
 * @author sbodmer
 */
public class BuildingsTile extends Thread {

    static java.security.MessageDigest md = null;

    int x = 0;
    int y = 0;
    int level = 15;
    boolean draggable = false;
    String provider = "";   //--- Base URL provider

    /**
     * The fetching URL
     */
    URL url = null;

    /**
     * The counter for the different servers
     */
    static int current = 0;

    Position center = null;
    FileStore store = null;
    boolean retrieveRemoteData = true;
    long expireDate = 0;
    String cachePath = "";

    /**
     * The loading timestamp
     */
    long ts = 0;
    long currentLoaded = -1;
    long totalToLoad = -1;
    String progress = "";

    /**
     * The start of the fetching process
     */
    long fetchTs = 0;

    BuildingsTileListener listener = null;

    /**
     * The loaded models
     */
    Renderable renderable = null;
    boolean loaded = false;
    String error = null;

    /**
     * The tile bounding box
     */
    ExtrudedPolygon tile = null;

    LatLon bl = null;   //--- Bottom left
    LatLon tl = null;   //--- Top left
    LatLon tr = null;   //--- Top right
    LatLon br = null;   //--- Bottom right

    //--- The list of buildings on this tile
    ArrayList<Building> buildings = new ArrayList<>();

    static {
        try {
            md = java.security.MessageDigest.getInstance("MD5");

        } catch (java.security.NoSuchAlgorithmException ex) {
            ex.printStackTrace();

        }
    }

    public BuildingsTile(int level, int x, int y, BuildingsTileListener listener, Position center, FileStore store, boolean retrieveRemoteData, long expireDate, boolean draggable, String provider) {
        this.x = x;
        this.y = y;
        this.level = level;
        this.center = center;
        this.listener = listener;
        this.store = store;
        this.retrieveRemoteData = retrieveRemoteData;
        this.expireDate = expireDate;
        this.draggable = draggable;
        this.provider = provider;

        //--- Find the hash of the provider string to determine the cache folder
        byte[] array = md.digest(provider.getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
        }
        String hash = sb.toString();

        //--- Produce the building.xml local cache path
        cachePath = BuildingsLayer.CACHE_FOLDER + File.separatorChar + hash + File.separatorChar + level + File.separatorChar + x + File.separatorChar + y + File.separatorChar + "buildings.xml";

        BufferedImage tex = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) tex.getGraphics();
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, 256, 256);
        g2.setColor(Color.WHITE);
        g2.drawString("" + x + "x" + y, 30, 30);

        //--- Create the surface box for tile information
        tile = new ExtrudedPolygon();
        List<LatLon> list = new ArrayList<LatLon>();

        double lat1 = OSMBuildingsLayer.y2lat(y + 1, level);
        double lon1 = OSMBuildingsLayer.x2lon(x, level);
        double lat2 = OSMBuildingsLayer.y2lat(y, level);
        double lon2 = OSMBuildingsLayer.x2lon(x + 1, level);
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
        cap.setDrawInterior(true);
        cap.setInteriorOpacity(0.1d);
        cap.setEnableLighting(false);
        cap.setOutlineMaterial(Material.BLACK);
        cap.setOutlineWidth(1d);
        cap.setInteriorMaterial(Material.GREEN);
        cap.setDrawInterior(true);
        cap.setDrawOutline(true);
        tile.setCapAttributes(cap);

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    /**
     * Return the source array of the list of parsed building
     * @return 
     */
    public ArrayList<Building> getBuildings() {
        return buildings;
    }
    
    @Override
    public String toString() {
        return "" + level + "x" + x + "x" + y;
    }

    public URL getURL() {
        return url;
    }

    public long getCurrentLoadedProgress() {
        return currentLoaded;
    }

    public long getTotalToLoad() {
        return totalToLoad;
    }

    /**
     * Progress string
     *
     * @return
     */
    public String getProgress() {
        return progress;
    }

    /**
     * Return the tile footprint as a surface on the ground
     *
     * @return
     */
    public Renderable getTileSurfaceRenderable() {
        return tile;
    }

    /**
     * Tick tile usage
     */
    public void tick() {
        ts = System.currentTimeMillis();
    }

    public long getLastUsed() {
        return ts;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Return null if no error
     */
    public String hasError() {
        return error;
    }

    /**
     * Returns the start of the tile loading (fetch call)
     *
     * @return
     */
    public long getFetchedTimestamp() {
        return fetchTs;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        error = "Too long, interrupted";
    }

    /**
     * Thread
     */
    @Override
    public void run() {
        fetchTs = System.currentTimeMillis();
        try {
            url = new URL(getBuildingUrl(level, x, y));
            // System.out.println("FETCHING " + level + "x" + x + "x" + y + " URL:" + url);
            if (listener != null) listener.buildingLoading(this, -1, -1);
            progress = "Loading building.xml";

            //--- Check if cached version is too old
            URL data = store.findFile(cachePath, false);
            File f = null;
            if (data != null) {
                long now = System.currentTimeMillis();
                f = new File(data.toURI());
                if (f.lastModified() < now - expireDate) {
                    f.delete();
                    f = null;
                }
            }

            //--- Download the building.xml
            if (f == null) {
                f = store.newFile(cachePath);
                URLConnection con = url.openConnection();
                int len = con.getContentLength();
                // System.out.println("DOWNLOADING LENGTH:" + len);
                FileOutputStream fout = new FileOutputStream(f);
                InputStream in = con.getInputStream();
                byte b[] = new byte[65535];
                while (true) {
                    int read = in.read(b);
                    if (read == -1) break;
                    fout.write(b, 0, read);
                }
                fout.close();
                in.close();

            }

            // System.out.println("BUILDING:" + f.length() + " PATH:" + f.getPath());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);
            Element root = doc.getDocumentElement();    //--- Buildings root tag
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeName().equals("Model")) {
                    Element e = (Element) n;
                    String modelUrl = e.getAttribute("url");
                    NodeList nl2 = e.getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        Node n2 = nl2.item(j);
                        if (n2.getNodeName().equals("Path")) {
                            Element e2 = (Element) n2;
                            String path = e2.getAttribute("file");
                            File file = new File(f.getParent(), path);
                            Building b = new Building(file, modelUrl);
                            NodeList nl3 = e2.getChildNodes();
                            for (int k = 0; k < nl3.getLength(); k++) {
                                Node n3 = nl3.item(k);
                                if (n3.getNodeName().equals("Coordinates")) {
                                    Element e3 = (Element) n3;
                                    try {
                                        double lat = Double.parseDouble(e3.getAttribute("latitude"));
                                        double lon = Double.parseDouble(e3.getAttribute("longitude"));
                                        double alt = Double.parseDouble(e3.getAttribute("altitude"));
                                        double azi = Double.parseDouble(e3.getAttribute("azimuth"));
                                        double ele = Double.parseDouble(e3.getAttribute("elevation"));
                                        double roll = Double.parseDouble(e3.getAttribute("roll"));
                                        double scale = Double.parseDouble(e3.getAttribute("scale"));
                                        b.setLatitude(lat);
                                        b.setLongitude(lon);
                                        b.setAltitude(alt);
                                        b.setAzimuth(azi);
                                        b.setElevation(ele);
                                        b.setRoll(roll);
                                        b.setScale(scale);
                                        
                                    } catch (NumberFormatException ex) {
                                        //---
                                        ex.printStackTrace();
                                    }
                                }
                            }
                            buildings.add(b);

                        }
                    }
                }
            }

            for (int i = 0; i < buildings.size(); i++) {
                Building b = buildings.get(i);
                File file = b.getFile();
                if (!file.exists()) {
                    //--- Download the model
                    URL m = new URL(b.getModelUrl());
                    String fileName = m.getFile().substring(m.getFile().lastIndexOf('/') + 1);
                    File t = new File(f.getParentFile(), fileName);
                    // System.out.println("DOWNLOADING FROM " + m + " TO " + t.getPath());
                    URLConnection con = m.openConnection();
                    progress = "Downloading...";
                    totalToLoad = con.getContentLength();
                    // System.out.println("DOWNLOADING LENGTH:" + totalToLoad);
                    currentLoaded = 0;
                    progress = "Downloading " + fileName;
                    FileOutputStream fout = new FileOutputStream(t);
                    InputStream in = con.getInputStream();
                    byte buffer[] = new byte[65535];
                    while (true) {
                        int read = in.read(buffer);
                        if (read == -1) break;
                        currentLoaded += read;
                        fout.write(buffer, 0, read);
                    }
                    fout.close();
                    in.close();
                    if (t.getName().toLowerCase().endsWith(".zip")) {
                        progress = "Unzipping";
                        //--- Unzip the files
                        File destDir = t.getParentFile();
                        ZipInputStream zis = new ZipInputStream(new FileInputStream(t));
                        ZipEntry zipEntry = zis.getNextEntry();
                        while (zipEntry != null) {
                            File newFile = new File(destDir, zipEntry.getName());
                            progress = "Unzipping "+zipEntry.getName();
                            // System.out.println("NEWFILE:" + newFile);
                            if (zipEntry.isDirectory()) {
                                newFile.mkdirs();

                            } else {
                                FileOutputStream fos = new FileOutputStream(newFile);
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                                fos.close();

                            }
                            zipEntry = zis.getNextEntry();
                        }
                        zis.closeEntry();
                        zis.close();

                        t.delete();
                    }
                }
                if (file.exists()) {
                    // System.out.println("BUILDING FOUND:" + b.getFile() + "("+b.getFile().exists()+") MODEL:" + b.getModelUrl());
                    
                }
            }
            loaded = true;
            if (listener != null) listener.buildingLoaded(this);
            
        } catch (Exception ex) {
            error = ex.getMessage();
            if (listener != null) listener.buildingLoadingFailed(this, ex.getMessage());
            progress = ex.getMessage();
        }
        
    }

    //**************************************************************************
    //*** Private
    //**************************************************************************
    /**
     * Replace the ${Z},${X},${Y} with resolved values
     *
     * @return
     */
    private String getBuildingUrl(int z, int x, int y) {
        String s = provider.replaceAll("\\$\\{Z\\}", "" + level);
        s = s.replaceAll("\\$\\{X\\}", "" + x);
        s = s.replaceAll("\\$\\{Y\\}", "" + y);
        int i1 = s.indexOf('[');
        if (i1 != -1) {
            int i2 = s.indexOf(']');
            String sub = s.substring(i1 + 1, i2);
            int l = sub.length();
            current++;
            if (current >= sub.length())
                current = 0;
            char c = sub.charAt(current);
            s = s.replaceAll("\\[" + sub + "\\]", "" + c);
        }
        // System.out.println("S:" + s);
        return s;

    }

    /*
    private class LocalBuildingLoader implements Runnable, RetrievalPostProcessor {

        String path = null;
        BuildingTile ti = null;

        private LocalBuildingLoader(String file, BuildingTile ti) {
            this.path = file;
            this.ti = ti;

        }

        @Override
        public void run() {
            fetchTs = System.currentTimeMillis();
            // System.out.println(">RUN ["+ti.x+"x"+ti.y+"]=>"+path);
            try {
                URL data = store.findFile(path, false);

                //--- Load the building.xml and parse it
                // GeoJSONDoc doc = new GeoJSONDoc(data);
                // doc.parse();

                // renderable = new OSMBuildingsRenderable(doc, defaultHeight, draggable, defaultAttrs, data.toString(), listener, tile );
                if (listener != null) listener.buildingLoaded(ti);

                loaded = true;
                
            } catch (NullPointerException ex) {
                //--- File is no more in local storage ?
                if (listener != null) listener.buildingLoadingFailed(ti, "Local buildings.xml file could not be found : " + cachePath);

            
            } catch (Exception ex) {
                //--- Other problems
                if (listener != null) listener.buildingLoadingFailed(ti, "Local building.xml file could not be loaded : " + ex.getMessage());

            }
            // System.out.println("<RUN");
            fetchTs = 0;
        }

        //**********************************************************************
        //*** RetrievalPostProcessor
        //**********************************************************************
        @Override
        public ByteBuffer run(Retriever retriever) {
            fetchTs = System.currentTimeMillis();
            // System.out.println(">RUN ["+ti.x+"x"+ti.y+"]=>"+path);
            HTTPRetriever hr = (HTTPRetriever) retriever;
            try {
                if (hr.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    byte b[] = hr.getBuffer().array();
                    if (b.length == 0) return null;

                    //--- Store to cache file
                    File f = store.newFile(path);
                    FileOutputStream fout = new FileOutputStream(f);
                    //--- The buffer contains trailling 0000, so convert to string to remove it
                    //--- Why is that, no idea ???
                    String tmp = new String(b, "UTF-8").trim();
                    fout.write(tmp.getBytes("UTF-8"));
                    fout.close();

                    //--- Load the data
                    // GeoJSONDoc doc = new GeoJSONDoc(f.toURI().toURL());
                    // doc.parse();

                    // renderable = new OSMBuildingsRenderable(doc, defaultHeight, draggable, defaultAttrs, f.toString(), listener, tile);
                    if (listener != null) listener.buildingLoaded(ti);

                    
                    loaded = true;
                    
                } else {
                    //--- Wrong http response
                    if (listener != null)
                        listener.buildingLoadingFailed(ti, "buildings.xml file could not be found, wrong http response : " + hr.getResponseCode() + " " + hr.getResponseMessage());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                //--- Failed
                if (listener != null)
                    listener.buildingLoadingFailed(ti, "building.xml file could not be found : " + ex.getMessage());
            }
            fetchTs = 0;
            return hr.getBuffer();
        }

        
    }
     */
}
