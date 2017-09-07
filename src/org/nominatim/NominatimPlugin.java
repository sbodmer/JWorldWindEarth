/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nominatim;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.JComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.worldwindearth.geocode.Result;
import org.worldwindearth.geocode.WWEGeocodePlugin;

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
    public ArrayList<Result> reverse(Position pos) {
        ArrayList<Result> list = new ArrayList<>();

        //--- Fetch for the last level
        int zooms[] = {18};
        for (int k = 0; k < zooms.length; k++) {
            int zoom = zooms[k];
            try {
                String s = DEFAULT_URL + "/reverse?";
                s += "format=xml";
                s += "&lat=" + pos.latitude.degrees + "&lon=" + pos.longitude.degrees;
                s += "&zoom=" + zoom + "&addressdetails=1";
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
                Result r = new Result(this);

                NodeList nl = c.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    String nn = nl.item(i).getNodeName();
                    if (nn.equals("result")) {
                        Element e = (Element) nl.item(i);

                        r.setSummary(e.getFirstChild().getNodeValue());
                        r.setLatitude(Double.parseDouble(e.getAttribute("lat")));
                        r.setLongitude(Double.parseDouble(e.getAttribute("lon")));

                    } else if (nn.equals("addressparts")) {
                        Element e = (Element) nl.item(i);
                        NodeList nl2 = e.getChildNodes();
                        for (int j = 0; j < nl2.getLength(); j++) {
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

    @Override
    public ArrayList<Result> geocode(String house, String street, String zip, String city, String country) {
        ArrayList<Result> list = new ArrayList<>();
        try {
            String s = DEFAULT_URL + "/search?";
            s += "format=xml";
            // s += "&q=";
            // String q = "";
            // if (!street.equals("")) s += "&street="+URLEncoder.encode(house+" "+street, "UTF-8");
            // if (!city.equals("")) s += "&city=" + URLEncoder.encode(city,"UTF-8");
            // if (!zip.equals("")) s += "&postalcode=" + URLEncoder.encode(zip,"UTF-8");
            String q = " "+house+" "+street+" "+zip+" "+city;
            s += "&q=" + URLEncoder.encode(q.trim(), "UTF-8");
            s += "&addressdetails=1";
            if (country.equals("")) country = "ch";
            s += "&countrycodes=" + country.toLowerCase();
            s += "&limit=5";

            // System.out.println("S:"+s);
            URL url = new URL(s);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(20000);
            con.setRequestProperty("User-Agent", "WorldWindEarth (support@lsi-media.ch)");

            //--- Parse (create builder, because multi threaded access)
            DocumentBuilder builder = docBuilder.newDocumentBuilder();
            Document doc = builder.parse(con.getInputStream());

            NodeList nl = doc.getElementsByTagName("place");
            for (int i = 0; i < nl.getLength(); i++) {
                Element p = (Element) nl.item(i);
                
                Result r = new Result(this);
                
                String summary = "";
                // r.setSummary(p.getAttribute("display_name"));
                r.setLatitude(Double.parseDouble(p.getAttribute("lat").replace(',', '.')));
                r.setLongitude(Double.parseDouble(p.getAttribute("lon").replace(',', '.')));

                NodeList nl2 = p.getChildNodes();
                for (int j = 0; j < nl2.getLength(); j++) {
                    String nn = nl2.item(j).getNodeName();
                    if (nn.equals("house")) {
                        r.setHouse(nl2.item(j).getFirstChild().getNodeValue());

                    } else if (nn.equals("road")) {
                        r.setStreet(nl2.item(j).getFirstChild().getNodeValue());

                    } else if (nn.equals("tram_stop")) {
                        //---
                        
                    } else if (nn.equals("cycleway")) {
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

                    }
                }
                summary = r.zip+" "+r.city+", "+r.house+" "+r.street;
                r.setSummary(summary);
                list.add(r);

            }

        } catch (Exception ex) {
            ex.printStackTrace();

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
