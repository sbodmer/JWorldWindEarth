package org.worldwindearth.trek;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class GPX {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    File file = null;
    String name = "";
    String desc = "";
    
    ArrayList<WptType> wpt = new ArrayList<>();
    ArrayList<RteType> rte = new ArrayList<>();
    ArrayList<TrkType> trk = new ArrayList<>();
    
    //--- Null Consructore
    public GPX() {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

    }

    public GPX(File f) {
        this();
        importFile(f);
    }
    
    public String toString() {
        return name+ " (wpt="+wpt.size()+",rte="+rte.size()+",trk="+trk.size()+")";
    }
    
    /**
     * Returne linked file or null if none
     * 
     * @return 
     */
    public File getFile() {
        return file;
    }
    
    public void importFile(File f) {
        this.file = f;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new FileInputStream(f));

            //--- Don't care if not gps, parse it anyway
            NodeList nl = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeName().equals("metadata")) {
                    Element e = (Element) n;
                    NodeList nl2 = e.getChildNodes();
                    for (int j=0;j<nl2.getLength();j++) {
                        Node n2 = nl2.item(j);
                        if (n2.getNodeName().equals("name")) {
                            Element e2 = (Element) n2;
                            name = (e2.getFirstChild() != null ? e2.getFirstChild().getNodeValue() : "");
                            
                        } else if (n2.getNodeName().equals("desc")) {
                            
                        }
                    }
 
                } else if (n.getNodeName().equals("wpt")) {
                    //--- Waypoints
                    Element e = (Element) n;
                    try {
                        WptType w = parseWptType(e);
                        wpt.add(w);

                    } catch (Exception ex) {
                        //--- Parsing error
                        ex.printStackTrace();

                    }

                } else if (n.getNodeName().equals("rte")) {
                    Element e = (Element) n;
                    RteType r = new RteType();

                    NodeList nl2 = e.getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        Node n2 = nl2.item(j);
                        if (n2.getNodeName().equals("name")) {
                            Element e2 = (Element) n2;
                            r.name = (e2.getFirstChild() != null ? e2.getFirstChild().getNodeValue() : "");
                            
                        } else if (n2.getNodeName().equals("rtept")) {
                            Element e2 = (Element) n2;
                            try {
                                WptType w = parseWptType(e2);
                                r.rtept.add(w);
                                
                            } catch(Exception ex) {
                                ex.printStackTrace();
                                
                            }
                        }
                    }

                } else if (n.getNodeName().equals("trk")) {
                    Element e = (Element) n;
                    TrkType t = new TrkType();
                    trk.add(t);
                    
                    NodeList nl2 = e.getChildNodes();
                    for (int j=0;j<nl2.getLength();j++) {
                        Node n2 = nl2.item(j);
                        if (n2.getNodeName().equals("name")) {
                            Element e2 = (Element) n2;
                            t.name = (e2.getFirstChild() != null ? e2.getFirstChild().getNodeValue() : "");
                            
                        } else if (n2.getNodeName().equals("trkseg")) {
                            Element e2 = (Element) n2;
                            TrkSeg seg = new TrkSeg();
                            t.trkseg.add(seg);
                            
                            NodeList nl3 = e2.getChildNodes();
                            for (int k=0;k<nl3.getLength();k++) {
                                Node n3 = nl3.item(k);
                                if (n3.getNodeName().equals("trkpt")) {
                                    Element e3 = (Element) n3;
                                    try {
                                        WptType w = parseWptType(e3);
                                        seg.trkpt.add(w);
                                        
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        
                                    }
                                    
                                } else if (n3.getNodeName().equals("extensions")) {
                                    //--- Don't care
                                }
                            }
                        }
                    }
                    
                } else if (n.getNodeName().equals("extensions")) {
                    //--- Don't care
                }
            }
            

        } catch (Exception ex) {
            ex.printStackTrace();
           
        }

    }

    private WptType parseWptType(Element e) {
        WptType w = new WptType(Double.parseDouble(e.getAttribute("lat")), Double.parseDouble(e.getAttribute("lon")));
        
        //--- Parse the non mandatory nodes
        NodeList nl2 = e.getChildNodes();
        for (int j = 0; j < nl2.getLength(); j++) {
            Node n2 = nl2.item(j);
            try {
                if (n2.getNodeName().equals("ele")) {
                    Element e2 = (Element) n2;
                    w.ele = Double.parseDouble(e2.getFirstChild().getNodeValue());

                } else if (n2.getNodeName().equals("time")) {
                    Element e2 = (Element) n2;
                    String s = e2.getFirstChild().getNodeValue().replaceAll("\\+0([0-9]){1}\\:00", "+0$100");
                    w.time = sdf.parse(s);

                } else if (n2.getNodeName().equals("name")) {
                    Element e2 = (Element) n2;
                    w.name = e2.getFirstChild().getNodeValue();

                }

            } catch (Exception ex) {
                //---Parsing error
                ex.printStackTrace();

            }
        }
        return w;
    }

}
