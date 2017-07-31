/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.wms;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSCapabilityInformation;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.wms.Capabilities;
import gov.nasa.worldwind.wms.CapabilitiesRequest;
import java.net.URI;
import java.net.URL;

/**
 * Simple WMS container
 *
 * @author sbodmer
 */
public class WMSServer implements Runnable {

    String name = "";
    URI api = null;
    WMSCapabilities caps = null;
    WMSServerListener listener = null;
    
    public WMSServer(String name, URI api, WMSServerListener listener) {
        this.name = name;
        this.api = api;
        this.listener = listener;
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    @Override
    public String toString() {
        return name;
    }

    public URI getApi() {
        return api;
    }

    /**
     * Returned the fetched capabilities or null if not yet fetched
     * 
     * @return 
     */
    public WMSCapabilities getCapabilities() {
        return caps;
    }
    /**
     * Fetch the capabilities in i't onw thread, use listener to receive feed-back
     */
    public void fetch() {
        Thread t = new Thread(this);
        t.start();
    }
    @Override
    public void run() {
        //--- Fetch the capabilities
        if (listener != null) listener.wmsCapabilitiesLoading(this);
        try {
            // AVListImpl av = new AVListImpl();
            // av.setValue(AVKey.SERVICE_NAME, "OGC:WMS");
            // av.setValue(AVKey.GET_CAPABILITIES_URL, api);
            // CapabilitiesRequest request = new CapabilitiesRequest(uri, "WMS");
            // System.out.println("URI:"+request.getUri());
            // cap = Capabilities.retrieve(api, "WMS", 60000, 60000);
            
            caps = WMSCapabilities.retrieve(api);
            caps.parse();
            
            // System.out.println("CAP:" + (cap == null ? "NULL" : "NOT NULL"));
            // System.out.println("FORMAT:" + cap.getImageFormats());
            // WMSCapabilityInformation info = cap.
            // System.out.println("CAP:"+cap.getRequestURL("GetCapabilities", "1.1.1", "GET"));
            // WMSCapabilityInformation info = cap.getCapabilityInformation();
            // System.out.println("INFO:" + info.getImageFormats());
            if (listener != null) listener.wmsCapabilitiesLoaded(this, caps);
            
        } catch (Exception ex) {
            if (listener != null) listener.wmsCapabilitiesFailed(this, ex.getMessage());
        }
        
    }
    
    public interface WMSServerListener {
        public void wmsCapabilitiesLoading(WMSServer wms);
        public void wmsCapabilitiesLoaded(WMSServer wms, WMSCapabilities cap);
        public void wmsCapabilitiesFailed(WMSServer wms, String message);
        
    }
    
    public static void main(String args[]) {
        /*
        AVListImpl av = new AVListImpl();
        av.setValue(AVKey.NUM_LEVELS, 19);
        av.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, LatLon.fromDegrees(90, 180));
        av.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        av.setValue(AVKey.TILE_WIDTH, 256);
        av.setValue(AVKey.TILE_HEIGHT, 256);
        av.setValue(AVKey.FORMAT_SUFFIX, ".png");
        av.setValue(AVKey.DATA_CACHE_NAME, "WMS");
        av.setValue(AVKey.DATASET_NAME, "generic");
        LevelSet.SectorResolution[] sectorLimits = {
            new LevelSet.SectorResolution(Sector.FULL_SPHERE, 19)
        };
        av.setValue(AVKey.SECTOR_RESOLUTION_LIMITS, sectorLimits);
         */

        try {
            String api = "http://wms.geo.admin.ch/";
            AVListImpl av = new AVListImpl();
            av.setValue(AVKey.SERVICE_NAME, "OGC:WMS");
            av.setValue(AVKey.GET_CAPABILITIES_URL, api);
            // this.setParms(AVKey.R"REQUEST", "GetCapabilities");
            // this.setParam("VERSION", "1.3.0");
            // URL url = DataConfigurationUtils.getOGCGetCapabilitiesURL(av);
            // System.out.println("URL:" + url);
            // WMSCapabilities cap = WMSCapabilities.retrieve(url.toURI());
            URI uri = new URI(api);
            // CapabilitiesRequest request = new CapabilitiesRequest(uri, "WMS");
            // System.out.println("URI:"+request.getUri());
            Capabilities cap = Capabilities.retrieve(uri, "WMS", 60000, 60000);
            System.out.println("CAP:"+cap);
            System.out.println("PERSON:"+cap.getContactPerson());
            // WMSCapabilities wms = new WMSCapabilities(request);
            // System.out.println("CAP:" + (cap == null ? "NULL" : "NOT NULL"));
            // System.out.println("FORMAT:" + cap.getImageFormats());

            // System.out.println("CAP:"+cap.getRequestURL("GetCapabilities", "1.1.1", "GET"));
            // WMSCapabilityInformation info = cap.getCapabilityInformation();
            // System.out.println("INFO:" + info.getImageFormats());
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
