/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.wms;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import java.awt.image.BufferedImage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author sbodmer
 */
public class WrappedWMSTiledImageLayer extends WMSTiledImageLayer {

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
}
