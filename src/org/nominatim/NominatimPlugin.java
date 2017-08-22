/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nominatim;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.JComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.worldwindearth.geocode.Geocode;
import org.worldwindearth.geocode.Reverse;
import org.worldwindearth.geocode.WWEGeocodePlugin;
import org.xml.sax.SAXException;

/**
 *
 * @author sbodmer
 */
public class NominatimPlugin implements WWEGeocodePlugin {
    static final String DEFAULT_URL = "http://nominatim.openstreetmap.org";
    
    TinyFactory factory = null;
    App app = null;
    WorldWindow ww = null;
    
    DocumentBuilderFactory docBuilder = null;
    
    public NominatimPlugin(TinyFactory factory, WorldWindow ww) {
        this.factory = factory;
        
        try {
            docBuilder = DocumentBuilderFactory.newInstance();
            // builder = factory.newDocumentBuilder();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    //**************************************************************************
    //*** WWEGeocodePlugin
    //**************************************************************************
    /**
     * Open http connection to remote service
     * 
     * @param pos
     * @return 
     */
    @Override
    public ArrayList<Reverse> reverse(Position pos) {
        ArrayList<Reverse> list = new ArrayList<>();
        
        //--- Fetch for the last level
        int zooms[] = { 18 };
        for (int k=0;k<zooms.length;k++) {
            int zoom = zooms[k];
            try {
                String s = DEFAULT_URL + "/reverse?";
                s += "format=xml";
                s += "&lat=" + pos.latitude.degrees + "&lon=" + pos.longitude.degrees;
                s += "&zoom="+zoom+"&addressdetails=1";
                s += "&accept-language=" + Locale.getDefault().getLanguage();
                
                URL url = new URL(s);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(20000);
                con.setRequestProperty("User-Agent", "WorldWindEarth (https://github.com/sbodmer/WorldWindEarth)");

                //--- Parse (create here, because multi threaded access)
                DocumentBuilder builder = docBuilder.newDocumentBuilder();
                Document doc = builder.parse(con.getInputStream());
                // System.out.println("DOC:"+doc);

                Element c = (Element) doc.getElementsByTagName("reversegeocode").item(0);
                Reverse r = new Reverse(this);
                
                NodeList nl = c.getChildNodes();
                for (int i = 0;i < nl.getLength();i++) {
                    String nn = nl.item(i).getNodeName();
                    if (nn.equals("result")) {
                        Element e = (Element) nl.item(i);

                        r.setSummary(e.getFirstChild().getNodeValue());
                        r.setLatitude(Double.parseDouble(e.getAttribute("lat")));
                        r.setLongitude(Double.parseDouble(e.getAttribute("lon")));
                        
                    } else if (nn.equals("addressparts")) {
                        Element e = (Element) nl.item(i);
                        NodeList nl2 = e.getChildNodes();
                        for (int j = 0;j < nl2.getLength();j++) {
                            nn = nl2.item(j).getNodeName();
                            if (nn.equals("house_number")) {
                                r.setHouse(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("road")) {
                                r.setStreet(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("village")) {
                                r.setCity(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("town")) {
                                r.setCity(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("city")) {
                                r.setCity(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("county")) {
                                r.setState(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("state")) {
                                r.setState(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("country")) {
                                r.setCountry(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("postcode")) {
                                r.setZip(nl2.item(j).getFirstChild().getNodeValue());

                            } else if (nn.equals("pedestrian")) {
                                //--- For POI, use it as address
                                r.setStreet(nl2.item(j).getFirstChild().getNodeValue());
                            }
                        }
                    }
                }
                //--- No elevation, let the elevation be defined in the display process
                // r.setElevation(ww.getView().getGlobe().getElevation(Angle.fromDegrees(r.latitude), Angle.fromDegrees(r.longitude)));
                // System.out.println("ELE:"+r.elevation);
                list.add(r);

            } catch (Exception ex) {
                ex.printStackTrace();

            }
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
