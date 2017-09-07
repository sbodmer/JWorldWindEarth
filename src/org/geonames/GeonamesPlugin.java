/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geonames;

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
public class GeonamesPlugin implements WWEGeocodePlugin {

    static final String DEFAULT_URL = "http://api.geonames.org";

    TinyFactory factory = null;
    App app = null;
    WorldWindow ww = null;
    
    DocumentBuilderFactory docBuilder = null;

    public GeonamesPlugin(TinyFactory factory, WorldWindow ww) {
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

        try {
            //------------------------------------------------------------------
            //--- OSM POIs
            //------------------------------------------------------------------
            String s = DEFAULT_URL + "/findNearbyPOIsOSM?";
            s += "username=lsimedia";
            s += "&lat=" + pos.latitude.degrees + "&lng=" + pos.longitude.degrees;
            s += "&maxRows=4";
            s += "&lang=" + Locale.getDefault().getLanguage();
            
            URL url = new URL(s);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(20000);
            con.setRequestProperty("User-Agent", "WorldWindEarth (http://github.com, sbodmer@lsi-media.ch)");

            DocumentBuilder builder = docBuilder.newDocumentBuilder();
            Document doc = builder.parse(con.getInputStream());
            con.disconnect();

            NodeList nl = doc.getElementsByTagName("poi");
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);

                Result r = new Result(this);
                String general = "";
                NodeList nl2 = e.getChildNodes();
                for (int j = 0; j < nl2.getLength(); j++) {
                    String nn = nl2.item(j).getNodeName();
                    if (nn.equals("name")) {
                        if (nl2.item(j).getFirstChild() != null) {
                            general += "" + nl2.item(j).getFirstChild().getNodeValue();
                            
                        } else {
                            general += "N/A";
                        }

                    } else if (nn.equals("typeClass")) {
                        // general += " - "+nl2.item(j).getFirstChild().getNodeValue()+" ";

                    } else if (nn.equals("typeName")) {
                        general += " - " + nl2.item(j).getFirstChild().getNodeValue() + " ";

                    } else if (nn.equals("lat")) {
                        r.setLatitude(Double.parseDouble(nl2.item(j).getFirstChild().getNodeValue().replace(',', '.')));

                    } else if (nn.equals("lng")) {
                        r.setLongitude(Double.parseDouble(nl2.item(j).getFirstChild().getNodeValue().replace(',', '.')));

                    }
                }
                r.setSummary(general);
                list.add(r);

            }

            //------------------------------------------------------------------
            //--- Find nearby
            //------------------------------------------------------------------
            s = DEFAULT_URL + "/findNearby?";
            s += "username=lsimedia";
            s += "&lat=" + pos.latitude.degrees + "&lng=" + pos.longitude.degrees;
            s += "&maxRows=4";
            
            url = new URL(s);
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(20000);
            con.setRequestProperty("User-Agent", "WorldWindEarth (https://github.com/sbodmer/WorldWindEarth)");

            //--- Parse (create here, because multi threaded access)
            doc = builder.parse(con.getInputStream());
            con.disconnect();

            nl = doc.getElementsByTagName("geoname");
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);

                Result r = new Result(this);
                String general = "";
                NodeList nl2 = e.getChildNodes();
                for (int j = 0; j < nl2.getLength(); j++) {
                    String nn = nl2.item(j).getNodeName();
                    if (nn.equals("name")) {
                        if (nl2.item(j).getFirstChild() != null) {
                            general += "" + nl2.item(j).getFirstChild().getNodeValue();

                        } else {
                            general += "N/A";
                        }

                    } else if (nn.equals("typeClass")) {
                        // general += " - "+nl2.item(j).getFirstChild().getNodeValue()+" ";

                    } else if (nn.equals("typeName")) {
                        general += " - " + nl2.item(j).getFirstChild().getNodeValue() + " ";

                    } else if (nn.equals("lat")) {
                        r.setLatitude(Double.parseDouble(nl2.item(j).getFirstChild().getNodeValue().replace(',', '.')));

                    } else if (nn.equals("lng")) {
                        r.setLongitude(Double.parseDouble(nl2.item(j).getFirstChild().getNodeValue().replace(',', '.')));

                    }
                }
                r.setSummary(general);
                
                // r.setElevation(ww.getView().getGlobe().getElevation(Angle.fromDegrees(r.latitude), Angle.fromDegrees(r.longitude)));
                // System.out.println("ELE:"+r.elevation);
                list.add(r);

            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return list;
    }

    /**
     * Not supported
     * 
     * @param house
     * @param street
     * @param city
     * @param country
     * @return 
     */
    @Override
    public ArrayList<Result> geocode(String house, String street, String zip, String city, String country) {
        ArrayList<Result> list = new ArrayList<>();
        try {
            DocumentBuilder builder = docBuilder.newDocumentBuilder();

            String s = DEFAULT_URL + "/search?";
            s += "format=xml";
            s += "&username=lsimedia";
            String q = "";
            if (!house.equals("")) q += "" + house + " ";
            if (!street.equals("")) q += "" + street + " ";
            if (!zip.equals("")) q += "" + zip + " ";
            if (!city.equals("")) q += "" + city + " ";
            s += "&q=" + URLEncoder.encode(q.trim(), "UTF-8");
            s += "&lang=" + Locale.getDefault().getLanguage();
            
            URL url = new URL(s);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(20000);
            con.setRequestProperty("User-Agent", "WorldWindEarth (support@lsi-media.ch)");

            //--- Parse (create builder, because multi threaded access)
            Document doc = builder.parse(con.getInputStream());
            con.disconnect();

            NodeList nl = doc.getElementsByTagName("geoname");
            for (int i = 0;i < nl.getLength();i++) {
                Element p = (Element) nl.item(i);
                
                Result r = new Result(this);

                String general = "";
                NodeList nl2 = p.getChildNodes();
                for (int j = 0;j < nl2.getLength();j++) {
                    String nn = nl2.item(j).getNodeName();
                    if (nn.equals("name")) {
                        if (nl2.item(j).getFirstChild() != null) {
                            general += "" + nl2.item(j).getFirstChild().getNodeValue();
                            
                        } else {
                            general += "N/A";
                        }

                    } else if (nn.equals("countryCode")) {
                        general += " - "+nl2.item(j).getFirstChild().getNodeValue();
                        r.setCountry(nl2.item(j).getFirstChild().getNodeValue());

                    } else if (nn.equals("lat")) {
                        r.setLatitude(Double.parseDouble(nl2.item(j).getFirstChild().getNodeValue().replace(',', '.')));

                    } else if (nn.equals("lng")) {
                        r.setLongitude(Double.parseDouble(nl2.item(j).getFirstChild().getNodeValue().replace(',', '.')));

                    }
                }
                // entry.addTag("Street", nl2.item(j).getFirstChild().getNodeValue());
                // entry.addTag("City", nl2.item(j).getFirstChild().getNodeValue());entry.setTag("State", nl2.item(j).getFirstChild().getNodeValue());
                // entry.addTag("Zip", nl2.item(j).getFirstChild().getNodeValue());
                r.setSummary(general);
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
