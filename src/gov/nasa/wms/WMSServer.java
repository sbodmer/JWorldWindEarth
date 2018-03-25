/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.wms;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.wms.Capabilities;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;
import java.net.URI;
import java.util.List;

/**
 * Simple WMS container
 *
 * @author sbodmer
 */
public class WMSServer {

    String title = "";
    URI api = null;
    WMSCapabilities caps = null;
    
    public WMSServer(String title, URI api) {
        this.title = title;
        this.api = api;
        
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    @Override
    public String toString() {
        return title;
    }

    public URI getApi() {
        return api;
    }

    public void setApi(URI api) {
        this.api = api;
    } 
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
     * Fetch the capabilities in it's own thread, use listener to receive
     * feed-back
     * @param listener
     */
    public void fetch(WMSServerListener listener, String defaultLayers) {
        Fetch t = new Fetch(this, listener, defaultLayers);
        t.start();
    }

    public interface WMSServerListener {

        public void wmsCapabilitiesLoading(WMSServer wms);

        public void wmsCapabilitiesLoaded(WMSServer wms, WMSCapabilities cap, String defaultLayers);

        public void wmsCapabilitiesFailed(WMSServer wms, String message);

    }

    //**************************************************************************
    //*** Private
    //**************************************************************************
    private class Fetch extends Thread {

        WMSServer server = null;
        String defaultLayers = "";
        WMSServerListener listener = null;

        private Fetch(WMSServer server, WMSServerListener listener, String defaultLayers) {
            super("Fetch_"+server.getTitle());
            this.server = server;
            this.listener = listener;
            this.defaultLayers = defaultLayers;
        }

        @Override
        public void run() {
            //--- Fetch the capabilities
            if (listener != null) listener.wmsCapabilitiesLoading(server);
            try {
                // AVListImpl av = new AVListImpl();
                // av.setValue(AVKey.SERVICE_NAME, "OGC:WMS");
                // av.setValue(AVKey.GET_CAPABILITIES_URL, api);
                // CapabilitiesRequest request = new CapabilitiesRequest(uri, "WMS");
                // System.out.println("URI:"+request.getUri());
                // cap = Capabilities.retrieve(api, "WMS", 60000, 60000);

                caps = WMSCapabilities.retrieve(api);
                caps.parse();

                
                /*
                
                */
                
                // System.out.println("CAP:" + (cap == null ? "NULL" : "NOT NULL"));
                // System.out.println("FORMAT:" + cap.getImageFormats());
                // WMSCapabilityInformation info = cap.
                // System.out.println("CAP:"+cap.getRequestURL("GetCapabilities", "1.1.1", "GET"));
                // WMSCapabilityInformation info = cap.getCapabilityInformation();
                // System.out.println("INFO:" + info.getImageFormats());
                if (listener != null) listener.wmsCapabilitiesLoaded(server, caps, defaultLayers);

            } catch (Exception ex) {
                if (listener != null) listener.wmsCapabilitiesFailed(server, ex.getMessage());
            }
        }
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
            System.out.println("CAP:" + cap);
            System.out.println("PERSON:" + cap.getContactPerson());
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
