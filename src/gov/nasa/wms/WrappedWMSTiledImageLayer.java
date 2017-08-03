/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.wms;

import com.jogamp.opengl.util.texture.TextureData;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.BulkRetrievalListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.BulkRetrievalThread;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import java.awt.image.BufferedImage;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author sbodmer
 */
public class WrappedWMSTiledImageLayer extends WMSTiledImageLayer {

    static final AVListImpl INITIAL_PARAMS = new AVListImpl();

    /**
     * The wrapped wms layer, all call will be forwared to this one<p>
     *
     * The layer is mutable, so it can be changed during the life of this
     * wrapped<p>
     *
     */
    WMSTiledImageLayer wmsLayer = null;

    //--- The initial dummy params
    static {
        INITIAL_PARAMS.setValue(AVKey.NUM_LEVELS, 3);
        INITIAL_PARAMS.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, LatLon.fromDegrees(90, 180));
        INITIAL_PARAMS.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        INITIAL_PARAMS.setValue(AVKey.TILE_WIDTH, 256);
        INITIAL_PARAMS.setValue(AVKey.TILE_HEIGHT, 256);
        INITIAL_PARAMS.setValue(AVKey.FORMAT_SUFFIX, ".png");
        INITIAL_PARAMS.setValue(AVKey.DATA_CACHE_NAME, "WMS");
        INITIAL_PARAMS.setValue(AVKey.DATASET_NAME, "generic");
        LevelSet.SectorResolution[] sectorLimits = {
            new LevelSet.SectorResolution(Sector.FULL_SPHERE, 1),
            new LevelSet.SectorResolution(Sector.FULL_SPHERE, 2)
        };

        INITIAL_PARAMS.setValue(AVKey.SECTOR_RESOLUTION_LIMITS, sectorLimits);
    }

    public WrappedWMSTiledImageLayer() {
        super(INITIAL_PARAMS);
    }

    public WrappedWMSTiledImageLayer(AVList params) {
        super(params);
    }

    public WrappedWMSTiledImageLayer(Document dom, AVList params) {
        this(dom.getDocumentElement(), params);
    }

    public WrappedWMSTiledImageLayer(Element domElement, AVList params) {
        this(wmsGetParamsFromDocument(domElement, params));
    }

    public WrappedWMSTiledImageLayer(WMSCapabilities caps, AVList params) {
        this(wmsGetParamsFromCapsDoc(caps, params));
    }

    public WrappedWMSTiledImageLayer(String stateInXml) {
        this(wmsRestorableStateToParams(stateInXml));
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public void set(WMSTiledImageLayer wmsLayer) {
        this.wmsLayer = wmsLayer;
    }

    /**
     * The passed layer will be filled with the results
     * @param wms
     * @param params 
     */
    public void set(WMSServer wms, AVList params) {
        params = wmsGetParamsFromCapsDoc(wms.getCapabilities(), params);
        Iterator<Entry<String, Object>> it = params.getEntries().iterator();
        while (it.hasNext()) {
            Entry<String, Object> e = it.next();
            System.out.println("" + e.getKey() + " = " + e.getValue());
            // setValue(e.getKey(), e.getValue());
        }
       
        // wmsLayer = new WMSTiledImageLayer(params);
        
    }

    //**************************************************************************
    //*** WMSTiledImageLayer
    //**************************************************************************
    @Override
    public void getRestorableStateForAVPair(String key, Object value, RestorableSupport rs, RestorableSupport.StateObject context) {
        super.getRestorableStateForAVPair(key, value, rs, context);

    }

    @Override
    public BufferedImage composeImageForSector(Sector sector, int canvasWidth, int canvasHeight, double aspectRatio, int levelNumber, String mimeType, boolean abortOnError, BufferedImage image, int timeout) throws Exception {
        return super.composeImageForSector(sector, canvasWidth, canvasHeight, aspectRatio, levelNumber, mimeType, abortOnError, image, timeout);
    }

    @Override
    protected Document createConfigurationDocument(AVList params) {
        return super.createConfigurationDocument(params);

    }

    //**************************************************************************
    //*** BasicTilesImageLayer
    //**************************************************************************
    @Override
    protected boolean loadTexture(TextureTile tile, java.net.URL textureURL) {
        // System.out.println("loadTexture(" + tile + ") " + textureURL + ")");
        return super.loadTexture(tile, textureURL);
    }

    @Override
    protected void requestTexture(DrawContext dc, TextureTile tile) {
        // System.out.println("requestTexture(" + tile + ")");
        super.requestTexture(dc, tile);
    }

    @Override
    protected RequestTask createRequestTask(TextureTile tile) {
        // System.out.println("createRequestTask(" + tile + ")");
        return super.createRequestTask(tile);
    }

    @Override
    protected TextureData readTexture(java.net.URL url, String textureFormat, boolean useMipMaps) {
        // System.out.println("readTexture(" + url + ") " + textureFormat);
        return super.readTexture(url, textureFormat, useMipMaps);
    }

    @Override
    public BulkRetrievalThread makeLocal(Sector sector, double resolution, BulkRetrievalListener listener) {
        // System.out.println("makeLocal(" + sector + ") " + resolution);
        return super.makeLocal(sector, resolution, null, listener);
    }

    @Override
    protected void retrieveTexture(TextureTile tile, DownloadPostProcessor postProcessor) {
        //System.out.println("retrieveTexture(" + tile + ")");
        super.retrieveTexture(tile, postProcessor);
    }

    @Override
    protected DownloadPostProcessor createDownloadPostProcessor(TextureTile tile) {
        // System.out.println("createDownloadPostProcessor(" + tile + ")");
        return super.createDownloadPostProcessor(tile);
    }

    @Override
    protected String retrieveResources() {
        // System.out.println("retreiveResources()");
        return super.retrieveResources();

    }

    //**************************************************************************
    //*** TiledImageLayer
    //**************************************************************************
    @Override
    public LevelSet getLevels() {
        // System.out.println("getLevels()");
        return super.getLevels();
    }

    @Override
    public String getTextureFormat() {
        // System.out.println("getTextureFormat()");
        return super.getTextureFormat();
    }

    public List<String> getAvailableImageFormats() {
        // System.out.println("getAvailableImageFormats");
        return super.getAvailableImageFormats();
    }

    protected BufferedImage requestImage(TextureTile tile, String mimeType) throws URISyntaxException, InterruptedIOException, MalformedURLException {
        // System.out.println("requestImage(" + tile + ") " + mimeType);
        return super.requestImage(tile, mimeType);
    }
}
