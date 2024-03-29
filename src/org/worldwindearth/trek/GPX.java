package org.worldwindearth.trek;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * Representation of an .gpx file
 *
 * @author sbodmer
 */
public class GPX {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    File file = null;
    String name = "";
    String desc = "";

    ArrayList<WptType> wpt = new ArrayList<>();
    ArrayList<RteType> rte = new ArrayList<>();
    ArrayList<TrkType> trk = new ArrayList<>();

    public GPX(File f) {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        loadFile(f);
    }

    public String toString() {
        return name + " (wpt=" + wpt.size() + ",rte=" + rte.size() + ",trk=" + trk.size() + ")";
    }

    /**
     * Will generate the .gpx xml structure
     *
     * @param builder
     */
    public void produce(StringBuilder b) {
        b.append("<?xml version=\"1.0\"?>\n");
        b.append("<gpx version=\"1.1\" creator=\"Generated by jworldwindearth\">\n");
        b.append("<metadata>\n");
        b.append(" <name>" + name + "</name>\n");
        b.append(" <time>" + sdf.format(new Date()) + "</time>\n");
        b.append("</metadata>\n");
        for (int i = 0; i < wpt.size(); i++) {
            WptType w = wpt.get(i);
            b.append("<wpt lat=\"" + w.lat + "\" lon=\"" + w.lon + "\">\n");
            b.append(" <ele>" + w.ele + "</ele>\n");
            b.append(" <name>" + w.name + "</name>\n");
            b.append("</wpt>\n");
        }
        for (int i = 0; i < rte.size(); i++) {
            RteType r = rte.get(i);
            b.append("<rte>\n");
            b.append(" <name>" + r.name + "</name>\n");
            for (int j = 0; j < r.rtept.size(); j++) {
                WptType w = r.rtept.get(j);
                b.append(" <rtept lat=\"" + w.lat + "\" lon=\"" + w.lon + "\">\n");
                b.append("  <ele>" + w.ele + "</ele>\n");
                b.append("  <name>" + w.name + "</name>\n");
                b.append(" </rtept>\n");
            }
            b.append("</rte>\n");
        }
        for (int i = 0; i < trk.size(); i++) {
            TrkType t = trk.get(i);
            b.append("<trk>\n");
            b.append(" <name>" + t.name + "</name>\n");
            for (int j = 0; j < t.trkseg.size(); j++) {
                TrkSeg s = t.trkseg.get(j);
                b.append(" <trkseg>\n");
                for (int k = 0; k < s.trkpt.size(); k++) {
                    WptType w = s.trkpt.get(k);
                    b.append(" <trkpt lat=\"" + w.lat + "\" lon=\"" + w.lon + "\">\n");
                    b.append("  <ele>" + w.ele + "</ele>\n");
                    b.append("  <time>"+sdf.format(w.time)+"</time>\n");
                    b.append(" </trkpt>\n");
                }
                b.append(" </trkseg>\n");
            }
            b.append("</trk>\n");
        }
        b.append("</gpx>");
    }

    /**
     * Return linked file or null if none
     *
     * @return
     */
    public File getReferenceFile() {
        return file;
    }

    public File setReferenceFile(File f) {
        return file;
    }
    
    /**
     * Nullify the cached renderable (cones, paths)
     */
    public void clearRenderables() {
        for (int i = 0; i < wpt.size(); i++) wpt.get(i).cone = null;
        for (int i = 0; i < rte.size(); i++) {
            RteType r = rte.get(i);
            r.path = null;
            for (int j = 0; j < r.rtept.size(); j++) r.rtept.get(j).cylinder = null;
        }
        for (int i = 0; i < trk.size(); i++) {
            TrkType t = trk.get(i);
            for (int j = 0; j < t.trkseg.size(); j++) t.trkseg.get(j).path = null;
        }
    }

    public void loadFile(File f) {
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
                    for (int j = 0; j < nl2.getLength(); j++) {
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
                    rte.add(r);
                    
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

                            } catch (Exception ex) {
                                ex.printStackTrace();

                            }
                        }
                    }

                } else if (n.getNodeName().equals("trk")) {
                    Element e = (Element) n;
                    TrkType t = new TrkType();
                    trk.add(t);

                    int segments = 0;
                    NodeList nl2 = e.getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        Node n2 = nl2.item(j);
                        if (n2.getNodeName().equals("name")) {
                            Element e2 = (Element) n2;
                            t.name = (e2.getFirstChild() != null ? e2.getFirstChild().getNodeValue() : "");

                        } else if (n2.getNodeName().equals("trkseg")) {
                            Element e2 = (Element) n2;
                            segments++;
                            TrkSeg seg = new TrkSeg(segments);
                            t.trkseg.add(seg);

                            NodeList nl3 = e2.getChildNodes();
                            for (int k = 0; k < nl3.getLength(); k++) {
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
            // ex.printStackTrace();

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
