/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.yahoo.search;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.poi.PointOfInterest;
import gov.nasa.worldwind.poi.YahooGazetteer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.w3c.dom.Element;
import org.worldwindearth.geocode.Result;
import org.worldwindearth.geocode.WWEGazetteerPlugin;

/**
 *
 * @author sbodmer
 */
public class YahooGazetteerPlugin implements WWEGazetteerPlugin {
    TinyFactory factory = null;
    App app = null;
    WorldWindow ww = null;
    
    YahooGazetteer yahoo = null;
    
    public YahooGazetteerPlugin(TinyFactory factory, WorldWindow ww) {
        this.factory = factory;
        
        yahoo = new YahooGazetteer();
            
    }
    
    
    //**************************************************************************
    //*** WWEGazetteerPlugin
    //**************************************************************************
    /**
     * Open http connection to remote service
     * 
     * @param string
     * @param pos
     * @return 
     */
    @Override
    public ArrayList<Result> findPlaces(String string) {
        ArrayList<Result> list = new ArrayList<>();
        
        List<PointOfInterest> pois = yahoo.findPlaces(string);
        
        for (PointOfInterest p: pois) {
            Result r = new Result(this);
            LatLon latlon = p.getLatlon();
            r.setLatitude(latlon.latitude.degrees);
            r.setLongitude(latlon.longitude.degrees);
            r.setSummary(p.toString());
            list.add(r);
        }
        
        return list;
    }
    
    //**************************************************************************
    //*** TinyPlugin
    //**************************************************************************
    @Override
    public TinyFactory getPluginFactory() {
        return factory;
    }

    @Override
    public String getPluginName() {
        return factory.getFactoryName();
    }

    @Override
    public void setPluginName(String string) {
        //--- Not supported
    }

    @Override
    public JComponent getVisualComponent() {
        return null;
    }

    @Override
    public JComponent getConfigComponent() {
        return null;
    }

    @Override
    public Object doAction(String string, Object o, Object o1) {
        return null;
    }

    /**
     * The passed object is the world window
     * 
     * @param app
     * @param o 
     */
    @Override
    public void setup(App app, Object obj) {
        this.app = app;
        ww = (WorldWindow) obj;
    }

    @Override
    public void configure(Element elmnt) {
        //---
    }

    @Override
    public void cleanup() {
        //---
    }

    @Override
    public void saveConfig(Element elmnt) {
        //---
    }

    @Override
    public Object getProperty(String string) {
        return null;
    }

    @Override
    public void setProperty(String string, Object o) {
        //---
    }

    
    
}
