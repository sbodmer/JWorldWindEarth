/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.trek;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Path.PositionColors;
import gov.nasa.worldwind.util.BasicDragger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.TimeZone;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.worldwindearth.WWEPlugin;
import org.worldwindearth.components.SVGIcon;

/**
 * Stars dome
 *
 * @author sbodmer
 */
public class JTrekWWEPlugin extends JPanel implements WWEPlugin, ActionListener, ListSelectionListener, ItemListener, SelectListener, ClipboardOwner {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    NumberFormat nf = NumberFormat.getInstance();

    App app = null;
    TinyFactory factory = null;
    WorldWindow ww = null;
    JDesktopPane jdesktop = null;

    TraceLayer layer = new TraceLayer();

    File lastDir = new File(System.getProperty("user.home"));
    javax.swing.Timer timer = null;

    JElevations jele = new JElevations();

    BasicDragger dragger = null;
    GPX selected = null;

    Clipboard clip = new Clipboard("Trek");

    /**
     *
     * @param factory
     */
    public JTrekWWEPlugin(TinyFactory factory, WorldWindow ww) {
        super();
        this.factory = factory;
        this.ww = ww;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);
        initComponents();

        TB_GPX.getSelectionModel().addListSelectionListener(this);

        TB_Waypoints.getSelectionModel().addListSelectionListener(this);

        TB_Track.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(64);
        TB_Track.getSelectionModel().addListSelectionListener(this);

        PN_GPX.setVisible(false);
        PN_Elevations.add(jele, BorderLayout.CENTER);

        dragger = new BasicDragger(ww);

        timer = new javax.swing.Timer(1000, this);

    }

    //**************************************************************************
    //*** Plugin
    //**************************************************************************
    @Override
    public String getPluginName() {
        return layer.getName();
    }

    @Override
    public TinyFactory getPluginFactory() {
        return factory;
    }

    @Override
    public void setup(App app, Object arg) {
        this.app = app;
        this.jdesktop = (JDesktopPane) arg;

        BT_New.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/add.svg"));
        BT_Edit.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/edit.svg"));
        BT_Delete.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/trash.svg"));
        BT_Import.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/import.svg"));
        BT_Export.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/save.svg"));
        
        BT_EditRoutePoint.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/edit.svg"));
        BT_CopyRoutePoints.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/copy.svg"));
        BT_PasteRoutePoints.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/paste.svg"));
        BT_DeleteRoutePoint.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/trash.svg"));

        BT_DeleteRoute.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/trash.svg"));
        BT_EditRoute.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/edit.svg"));
        BT_NewRoute.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/add.svg"));

        BT_EditWaypoint.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/edit.svg"));
        BT_DeleteWaypoint.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/trash.svg"));
        
        BT_EditTrack.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/edit.svg"));
        
        BT_Import.addActionListener(this);
        BT_Export.addActionListener(this);
        BT_Delete.addActionListener(this);
        BT_Edit.addActionListener(this);
        BT_New.addActionListener(this);

        BT_DeleteWaypoint.addActionListener(this);
        BT_EditWaypoint.addActionListener(this);

        BT_NewRoute.addActionListener(this);
        BT_EditRoute.addActionListener(this);
        BT_DeleteRoute.addActionListener(this);
        BT_EditRoutePoint.addActionListener(this);
        BT_DeleteRoutePoint.addActionListener(this);
        BT_CopyRoutePoints.addActionListener(this);
        BT_PasteRoutePoints.addActionListener(this);

        BT_EditTrack.addActionListener(this);

        CB_Extrude.addActionListener(this);

        ww.addSelectListener(dragger);
        ww.addSelectListener(this);

        layer.setName("Trek");
        timer.start();
    }

    @Override
    public void cleanup() {
        ww.removeSelectListener(this);
        ww.removeSelectListener(dragger);

        layer.dispose();
        timer.stop();
    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;

        config.setAttribute("extrude", "" + CB_Extrude.isSelected());

        //--- Store the file reference
        for (int i = 0; i < TB_GPX.getRowCount(); i++) {
            try {
                GPX g = (GPX) TB_GPX.getValueAt(i, 0);
                Element e = config.getOwnerDocument().createElement("GPX");
                e.setAttribute("file", g.file.getPath());
                StringBuilder b = new StringBuilder();
                g.produce(b);
                e.appendChild(config.getOwnerDocument().createTextNode(Base64.getEncoder().encodeToString(b.toString().getBytes("UTF-8"))));
                config.appendChild(e);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Object getProperty(String property) {
        return null;
    }

    @Override
    public void setProperty(String property, Object value) {
        //---
    }

    @Override
    public Object doAction(String action, Object argument, Object subject) {
        return null;
    }

    @Override
    public void setPluginName(String name) {
        layer.setName(name);
    }

    @Override
    public JComponent getVisualComponent() {
        return null;
    }

    @Override
    public JComponent getConfigComponent() {
        return this;
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;

        DefaultTableModel model = (DefaultTableModel) TB_GPX.getModel();
        CB_Extrude.setSelected(config.getAttribute("extrude").equals("true"));
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("GPX")) {
                Element e = (Element) n;
                File f = new File(e.getAttribute("file"));

                //--- Dump the body as temp file to load it
                try {
                    String body = new String(Base64.getDecoder().decode(e.getFirstChild().getNodeValue()), "UTF-8");
                    File tmp = File.createTempFile("GPX", ".gpx");
                    System.out.println("(I) Saving temporary gpx file to " + tmp.getPath());
                    FileOutputStream fout = new FileOutputStream(tmp);
                    fout.write(body.getBytes("UTF-8"));
                    fout.close();

                    GPX g = new GPX(tmp);
                    Object obj[] = {g};
                    model.addRow(obj);
                    tmp.delete();

                } catch (Exception ex) {
                    ex.printStackTrace();

                }

            }
        }
        //--- Clear selection
        TB_GPX.getSelectionModel().clearSelection();
    }

    //**************************************************************************
    //*** WorldWindLayerPlugin
    //**************************************************************************
    @Override
    public Layer getLayer() {
        return layer;
    }

    @Override
    public boolean hasLayerButton() {
        return true;
    }

    @Override
    public void layerMouseClicked(MouseEvent e, Position pos) {
        if (e.getClickCount() >= 2) {

            if (selected == null) return;

            //--- Ceck if a gpx file is selected
            if (TAB_Item.getSelectedIndex() == 0) {
                //--- Waypoints
                //--- Create a new waypoint
                WptType w = new WptType(pos.latitude.degrees, pos.longitude.degrees);
                w.ele = pos.elevation;
                w.name = "Waypoint";
                Object obj[] = {w, w.lat, w.lon, w.ele};
                DefaultTableModel model = (DefaultTableModel) TB_Waypoints.getModel();
                model.addRow(obj);
                selected.wpt.add(w);

                //--- Display the waypoint cone
                layer.addRenderable(w.generateCone());

            } else if (TAB_Item.getSelectedIndex() == 1) {
                //--- Route
                //--- Create a new route point if selected
                if (CMB_Route.getItemCount() > 0) {
                    RteType r = (RteType) CMB_Route.getSelectedItem();
                    WptType w = new WptType(pos.latitude.degrees, pos.longitude.degrees);
                    w.ele = pos.elevation;
                    w.name = "Route";
                    r.rtept.add(w);

                    //--- Display the route path
                    layer.clearRoutes();
                    fillRoute(r);
                }

            }

        }

    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            //--- Display current track length
            if (CMB_Tracks.getItemCount() > 0) {
                TrkType t = (TrkType) CMB_Tracks.getSelectedItem();
                int total = 0;
                for (int j = 0; j < t.trkseg.size(); j++) {
                    TrkSeg seg = t.trkseg.get(j);
                    if (seg.path != null) total += seg.path.getLength();
                }
                LB_TrackPoints.setText(TB_Track.getRowCount() + " points, length=" + nf.format(total) + "m, high=" + jele.getHighest() + "m, low=" + jele.getLowest() + "m, diff=" + jele.getDiff() + "m");
            }

        } else if (e.getActionCommand().equals("import")) {
            JFileChooser jf = new JFileChooser(lastDir);
            jf.setMultiSelectionEnabled(false);
            jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jf.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().toLowerCase().endsWith(".gpx")) return true;
                    if (f.isDirectory()) return true;
                    return false;

                }

                @Override
                public String getDescription() {
                    return ".gpx files";
                }

            });
            int rep = jf.showOpenDialog(getTopLevelAncestor());
            if (rep == JFileChooser.APPROVE_OPTION) {
                File f = jf.getSelectedFile();
                lastDir = f.getParentFile();

                GPX g = new GPX(f);
                Object obj[] = {g};
                DefaultTableModel model = (DefaultTableModel) TB_GPX.getModel();
                model.addRow(obj);
            }

        } else if (e.getActionCommand().equals("export")) {
            int row = TB_GPX.getSelectedRow();
            if (row == -1) return;

            GPX g = (GPX) TB_GPX.getValueAt(row, 0);

            JFileChooser jf = new JFileChooser(lastDir);
            jf.setMultiSelectionEnabled(false);
            jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jf.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().toLowerCase().endsWith(".gpx")) return true;
                    if (f.isDirectory()) return true;
                    return false;

                }

                @Override
                public String getDescription() {
                    return ".gpx files";
                }

            });
            int rep = jf.showSaveDialog(getTopLevelAncestor());
            if (rep == JFileChooser.APPROVE_OPTION) {
                StringBuilder b = new StringBuilder();
                g.produce(b);

                try {
                    File f = jf.getSelectedFile();

                    FileOutputStream fout = new FileOutputStream(f);
                    fout.write(b.toString().getBytes("UTF-8"));
                    fout.close();

                    lastDir = f.getParentFile();
                    g.setReferenceFile(f);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

                }

            }

        } else if (e.getActionCommand().equals("delete")) {
            int row = TB_GPX.getSelectedRow();
            if (row == -1) return;

            GPX g = (GPX) TB_GPX.getValueAt(row, 0);
            for (int i = 0; i < g.wpt.size(); i++) {
                WptType w = g.wpt.get(i);
                if (w.cone != null) layer.removeRenderable(w.cone);
            }
            for (int i = 0; i < g.rte.size(); i++) {
                RteType r = g.rte.get(i);
                for (int j = 0; j < r.rtept.size(); j++) layer.removeRenderable(r.rtept.get(j).cylinder);
                layer.removeRoute(r.path);
            }

            for (int i = 0; i < g.trk.size(); i++) {
                TrkType t = g.trk.get(i);
                for (int j = 0; j < t.trkseg.size(); j++) layer.removeTrack(t.trkseg.get(j).path);
            }
            DefaultTableModel model = (DefaultTableModel) TB_GPX.getModel();
            model.removeRow(row);
            selected = null;
            PN_GPX.setVisible(false);

        } else if (e.getActionCommand().equals("edit")) {
            int row = TB_GPX.getSelectedRow();
            if (row == -1) return;

            GPX g = (GPX) TB_GPX.getValueAt(row, 0);
            String name = JOptionPane.showInputDialog("Name", g.name);
            if (name != null) g.name = name;
            DefaultTableModel model = (DefaultTableModel) TB_GPX.getModel();
            model.fireTableCellUpdated(row, 0);
            LB_GPX.setText(name);

        } else if (e.getActionCommand().equals("new")) {
            DefaultTableModel model = (DefaultTableModel) TB_GPX.getModel();
            String name = JOptionPane.showInputDialog("Name", "New");
            if (name == null) return;

            try {
                File tmp = File.createTempFile("GPX", ".gpx");
                GPX g = new GPX(tmp);
                g.name = name;

                Object obj[] = {g};
                model.addRow(obj);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(getTopLevelAncestor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

            }
        } else if (e.getActionCommand().equals("deleteWaypoint")) {
            int row = TB_Waypoints.getSelectedRow();
            if (row == -1) return;

            DefaultTableModel model = (DefaultTableModel) TB_Waypoints.getModel();
            WptType w = (WptType) model.getValueAt(row, 0);
            layer.removeRenderable(w.cone);
            model.removeRow(row);

        } else if (e.getActionCommand().equals("editWaypoint")) {
            int row = TB_Waypoints.getSelectedRow();
            if (row == -1) return;

            DefaultTableModel model = (DefaultTableModel) TB_Waypoints.getModel();
            WptType w = (WptType) model.getValueAt(row, 0);
            String name = JOptionPane.showInputDialog("Name", w.name);
            if (name != null) w.name = name;
            model.fireTableCellUpdated(row, 0);

        } else if (e.getActionCommand().equals("newRoute")) {
            String name = JOptionPane.showInputDialog(getTopLevelAncestor(), "Name");
            if (name != null) {
                RteType r = new RteType();
                r.name = name;
                selected.rte.add(r);
                CMB_Route.addItem(r);
            }

        } else if (e.getActionCommand().equals("editRoutePoint")) {
            int row = TB_Routes.getSelectedRow();
            if (row == -1) return;

            DefaultTableModel model = (DefaultTableModel) TB_Routes.getModel();
            WptType w = (WptType) model.getValueAt(row, 0);

            String name = JOptionPane.showInputDialog(getTopLevelAncestor(), "Name", w.name);
            if (name != null) w.name = name;
            model.fireTableCellUpdated(row, 0);

        } else if (e.getActionCommand().equals("deleteRoutePoint")) {
            int row = TB_Routes.getSelectedRow();
            if (row == -1) return;

            DefaultTableModel model = (DefaultTableModel) TB_Routes.getModel();
            WptType w = (WptType) model.getValueAt(row, 0);
            RteType r = (RteType) CMB_Route.getSelectedItem();
            r.rtept.remove(w);

            layer.removeRenderable(w.cylinder);
            layer.clearRoutes();
            fillRoute(r);

        } else if (e.getActionCommand().equals("copyRoutePoints")) {
            int rows[] = TB_Routes.getSelectedRows();
            if (rows.length == 0) return;

            WptTypeTransferable tr = new WptTypeTransferable();
            for (int i = 0; i < rows.length; i++) tr.add((WptType) TB_Routes.getValueAt(rows[i], 0));
            clip.setContents(tr, this);

        } else if (e.getActionCommand().equals("pasteRoutePoints")) {
            Transferable tr = clip.getContents(null);
            if (tr != null) {
                if (tr.isDataFlavorSupported(WptTypeTransferable.wptTypeDataFlavor)) {
                    try {
                        WptTypeTransferable wpt = (WptTypeTransferable) tr.getTransferData(WptTypeTransferable.wptTypeDataFlavor);
                        DefaultTableModel model = (DefaultTableModel) TB_Routes.getModel();
                        RteType r = (RteType) CMB_Route.getSelectedItem();
                        for (int i=0;i<wpt.size();i++) {
                            WptType w = wpt.get(i);
                            Object objs[] = { w, w.lat, w.lon, w.ele };
                            model.addRow(objs);
                            r.rtept.add(w);
                            
                        }
                        layer.clearRoutes();
                        fillRoute(r);
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }

        } else if (e.getActionCommand().equals("editTrack")) {
            TrkType t = (TrkType) CMB_Tracks.getSelectedItem();

            String name = JOptionPane.showInputDialog(getTopLevelAncestor(), "Name", t.name);
            if (name != null) t.name = name;
            CMB_Tracks.repaint();

        } else if (e.getActionCommand().equals("extrudeTrack")) {
            //--- Switch extrdue for all segements tracks of the selected gpx
            for (int i = 0; i < CMB_Tracks.getItemCount(); i++) {
                TrkType t = CMB_Tracks.getItemAt(i);
                for (int j = 0; j < t.trkseg.size(); j++) {
                    TrkSeg s = t.trkseg.get(j);
                    if (s.path != null) {
                        if (CB_Extrude.isSelected()) {
                            s.path.setExtrude(true);
                            s.path.setSurfacePath(false);

                        } else {
                            s.path.setExtrude(false);
                            s.path.setSurfacePath(true);
                        }
                    }
                }
            }
            ww.redraw();
        }
    }

    //**************************************************************************
    //*** ListSelectionListener
    //**************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        // if (e.getValueIsAdjusting() == true) return;
        if (e.getSource() == TB_GPX.getSelectionModel()) {
            if (e.getValueIsAdjusting()) return;

            //--- Clear old selected
            if (selected != null) selected.clearRenderables();

            int row = TB_GPX.getSelectedRow();
            if (row == -1) {
                PN_GPX.setVisible(false);
                selected = null;
                layer.clearTracks();
                layer.clearRoutes();
                layer.removeAllRenderables();

            } else {
                selected = (GPX) TB_GPX.getValueAt(row, 0);
                fill(selected);

                LB_GPX.setText(selected.name);
                LB_GPX.setToolTipText(selected.getReferenceFile().getPath());
                PN_GPX.setVisible(true);
            }
            
        } else if (e.getSource() == TB_Waypoints.getSelectionModel()) {
            int row = TB_Waypoints.getSelectedRow();
            if (row != -1) {
                WptType w = (WptType) TB_Waypoints.getValueAt(row, 0);
                try {
                    View v = ww.getView();
                    Position ref = Position.fromDegrees(w.lat, w.lon, w.ele);
                    v.goTo(ref, w.ele + 50);

                } catch (Exception ex) {
                    //---
                }
            }

        } else if (e.getSource() == TB_Track.getSelectionModel()) {
            int row = TB_Track.getSelectedRow();
            if (row != -1) {
                WptType w = (WptType) TB_Track.getValueAt(row, 1);
                try {
                    View v = ww.getView();
                    Position ref = Position.fromDegrees(w.lat, w.lon, w.ele);
                    v.goTo(ref, w.ele + 50);
                    ww.redraw();

                } catch (Exception ex) {
                    //---
                }
            }
            jele.highlight(row);
        }
    }

    //**************************************************************************
    //*** ItemListener
    //**************************************************************************
    @Override
    public void itemStateChanged(ItemEvent e) {
        // System.out.println("EVENT:" + e);
        if (e.getStateChange() == ItemEvent.DESELECTED) return;

        if (e.getSource() == CMB_Route) {
            RteType r = (RteType) CMB_Route.getSelectedItem();
            layer.clearRoutes();
            fillRoute(r);

        } else if (e.getSource() == CMB_Tracks) {
            TrkType t = (TrkType) CMB_Tracks.getSelectedItem();
            layer.clearTracks();
            fillTrack(t);

        }
    }

    //**************************************************************************
    //*** SelectListener
    //**************************************************************************
    @Override
    public void selected(SelectEvent event) {
        if (event.isDragEnd()) {
            Object obj = event.getTopObject();
            if (obj instanceof Cone) {
                //--- Find if it's one of the waypoints
                DefaultTableModel model = (DefaultTableModel) TB_Waypoints.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    WptType w = (WptType) model.getValueAt(i, 0);
                    if (w.cone == obj) {
                        Position center = w.cone.getCenterPosition();
                        // System.out.println("FOUND THE CON :" + center);
                        w.lat = center.latitude.degrees;
                        w.lon = center.longitude.degrees;
                        w.ele = ww.getModel().getGlobe().getElevation(center.latitude, center.longitude);

                        model.setValueAt(w.lat, i, 1);
                        model.setValueAt(w.lon, i, 2);
                        model.setValueAt(w.ele, i, 3);
                    }
                }
            }
            if (obj instanceof Cylinder) {
                DefaultTableModel model = (DefaultTableModel) TB_Routes.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    WptType w = (WptType) model.getValueAt(i, 0);
                    if (w.cylinder == obj) {
                        Position center = w.cylinder.getCenterPosition();
                        // System.out.println("FOUND THE CYLINDER :" + center);
                        w.lat = center.latitude.degrees;
                        w.lon = center.longitude.degrees;
                        w.ele = ww.getModel().getGlobe().getElevation(center.latitude, center.longitude);

                        model.setValueAt(w.lat, i, 1);
                        model.setValueAt(w.lon, i, 2);
                        model.setValueAt(w.ele, i, 3);

                        RteType r = (RteType) CMB_Route.getSelectedItem();

                        //--- Display the route path
                        layer.clearRoutes();
                        fillRoute(r);
                    }

                }

            }

        }
    }

    //*************************************************************************
    //*** Clipboard owner Listener
    //*************************************************************************
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //---
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        BT_New = new javax.swing.JButton();
        BT_Edit = new javax.swing.JButton();
        BT_Import = new javax.swing.JButton();
        BT_Export = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        BT_Delete = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        TB_GPX = new javax.swing.JTable();
        PN_GPX = new javax.swing.JPanel();
        TAB_Item = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        TB_Waypoints = new javax.swing.JTable();
        jToolBar4 = new javax.swing.JToolBar();
        BT_EditWaypoint = new javax.swing.JButton();
        BT_DeleteWaypoint = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        CMB_Route = new javax.swing.JComboBox<>();
        BT_NewRoute = new javax.swing.JButton();
        BT_EditRoute = new javax.swing.JButton();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        BT_DeleteRoute = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        TB_Routes = new javax.swing.JTable();
        jToolBar5 = new javax.swing.JToolBar();
        BT_EditRoutePoint = new javax.swing.JButton();
        BT_CopyRoutePoints = new javax.swing.JButton();
        BT_PasteRoutePoints = new javax.swing.JButton();
        BT_DeleteRoutePoint = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        CMB_Tracks = new javax.swing.JComboBox<>();
        BT_EditTrack = new javax.swing.JButton();
        CB_Extrude = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        TB_Track = new javax.swing.JTable();
        PN_TrackDetails = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        LB_TrackPoints = new javax.swing.JLabel();
        PN_Elevations = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        LB_GPX = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setPreferredSize(new java.awt.Dimension(456, 200));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);

        BT_New.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_New.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/add.png"))); // NOI18N
        BT_New.setToolTipText("New gpx");
        BT_New.setActionCommand("new");
        BT_New.setFocusable(false);
        BT_New.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_New.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_New.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_New);

        BT_Edit.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_Edit.setToolTipText("rename gpx");
        BT_Edit.setActionCommand("edit");
        BT_Edit.setFocusable(false);
        BT_Edit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Edit.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Edit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Edit);

        BT_Import.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Import.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/load.png"))); // NOI18N
        BT_Import.setToolTipText("import");
        BT_Import.setActionCommand("import");
        BT_Import.setFocusable(false);
        BT_Import.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Import.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Import.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Import);

        BT_Export.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Export.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/save.png"))); // NOI18N
        BT_Export.setToolTipText("Export");
        BT_Export.setActionCommand("export");
        BT_Export.setFocusable(false);
        BT_Export.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Export.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Export.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Export);
        jToolBar1.add(jSeparator9);

        BT_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_Delete.setToolTipText("delete layer");
        BT_Delete.setActionCommand("delete");
        BT_Delete.setFocusable(false);
        BT_Delete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Delete.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Delete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Delete);

        jPanel3.add(jToolBar1, java.awt.BorderLayout.NORTH);

        TB_GPX.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "GPX"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(TB_GPX);

        jPanel3.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        PN_GPX.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        TB_Waypoints.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Name", "Lat", "Lon", "Elevation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(TB_Waypoints);

        jPanel2.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jToolBar4.setRollover(true);

        BT_EditWaypoint.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_EditWaypoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_EditWaypoint.setToolTipText("rename gpx");
        BT_EditWaypoint.setActionCommand("editWaypoint");
        BT_EditWaypoint.setFocusable(false);
        BT_EditWaypoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_EditWaypoint.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_EditWaypoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar4.add(BT_EditWaypoint);

        BT_DeleteWaypoint.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_DeleteWaypoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_DeleteWaypoint.setToolTipText("Delete route");
        BT_DeleteWaypoint.setActionCommand("deleteWaypoint");
        BT_DeleteWaypoint.setFocusable(false);
        BT_DeleteWaypoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_DeleteWaypoint.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_DeleteWaypoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar4.add(BT_DeleteWaypoint);

        jPanel2.add(jToolBar4, java.awt.BorderLayout.NORTH);

        jLabel1.setText("Double click on terrain to create new waypoint");
        jPanel7.add(jLabel1);

        jPanel2.add(jPanel7, java.awt.BorderLayout.PAGE_END);

        TAB_Item.addTab("Waypoints", jPanel2);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jToolBar2.setRollover(true);

        jToolBar2.add(CMB_Route);

        BT_NewRoute.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_NewRoute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/add.png"))); // NOI18N
        BT_NewRoute.setToolTipText("new route");
        BT_NewRoute.setActionCommand("newRoute");
        BT_NewRoute.setFocusable(false);
        BT_NewRoute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_NewRoute.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_NewRoute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(BT_NewRoute);

        BT_EditRoute.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_EditRoute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_EditRoute.setToolTipText("rename gpx");
        BT_EditRoute.setActionCommand("edit");
        BT_EditRoute.setFocusable(false);
        BT_EditRoute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_EditRoute.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_EditRoute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(BT_EditRoute);
        jToolBar2.add(jSeparator10);

        BT_DeleteRoute.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_DeleteRoute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_DeleteRoute.setToolTipText("Delete route");
        BT_DeleteRoute.setActionCommand("deleteRoute");
        BT_DeleteRoute.setFocusable(false);
        BT_DeleteRoute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_DeleteRoute.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_DeleteRoute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(BT_DeleteRoute);

        jPanel5.add(jToolBar2, java.awt.BorderLayout.NORTH);

        jPanel9.setLayout(new java.awt.BorderLayout());

        TB_Routes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Lat", "Lon", "Elevation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(TB_Routes);

        jPanel9.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jToolBar5.setRollover(true);

        BT_EditRoutePoint.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_EditRoutePoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_EditRoutePoint.setToolTipText("rename point");
        BT_EditRoutePoint.setActionCommand("editRoutePoint");
        BT_EditRoutePoint.setFocusable(false);
        BT_EditRoutePoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_EditRoutePoint.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_EditRoutePoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar5.add(BT_EditRoutePoint);

        BT_CopyRoutePoints.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_CopyRoutePoints.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_CopyRoutePoints.setToolTipText("Copy route points");
        BT_CopyRoutePoints.setActionCommand("copyRoutePoints");
        BT_CopyRoutePoints.setFocusable(false);
        BT_CopyRoutePoints.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_CopyRoutePoints.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_CopyRoutePoints.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar5.add(BT_CopyRoutePoints);

        BT_PasteRoutePoints.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_PasteRoutePoints.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_PasteRoutePoints.setToolTipText("Paste route points");
        BT_PasteRoutePoints.setActionCommand("pasteRoutePoints");
        BT_PasteRoutePoints.setFocusable(false);
        BT_PasteRoutePoints.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_PasteRoutePoints.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_PasteRoutePoints.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar5.add(BT_PasteRoutePoints);

        BT_DeleteRoutePoint.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_DeleteRoutePoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_DeleteRoutePoint.setToolTipText("Delete route point");
        BT_DeleteRoutePoint.setActionCommand("deleteRoutePoint");
        BT_DeleteRoutePoint.setFocusable(false);
        BT_DeleteRoutePoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_DeleteRoutePoint.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_DeleteRoutePoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar5.add(BT_DeleteRoutePoint);

        jPanel9.add(jToolBar5, java.awt.BorderLayout.NORTH);

        jPanel5.add(jPanel9, java.awt.BorderLayout.CENTER);

        jLabel2.setText("Double click on terrain to add point");
        jPanel8.add(jLabel2);

        jPanel5.add(jPanel8, java.awt.BorderLayout.PAGE_END);

        TAB_Item.addTab("Routes", jPanel5);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jToolBar3.setRollover(true);

        jToolBar3.add(CMB_Tracks);

        BT_EditTrack.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_EditTrack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_EditTrack.setToolTipText("rename gpx");
        BT_EditTrack.setActionCommand("editTrack");
        BT_EditTrack.setFocusable(false);
        BT_EditTrack.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_EditTrack.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_EditTrack.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(BT_EditTrack);

        CB_Extrude.setText("Extrude");
        CB_Extrude.setActionCommand("extrudeTrack");
        jToolBar3.add(CB_Extrude);

        jPanel6.add(jToolBar3, java.awt.BorderLayout.NORTH);

        TB_Track.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Segment", "Time", "Lat", "Lon", "Elevation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(TB_Track);

        jPanel6.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        PN_TrackDetails.setPreferredSize(new java.awt.Dimension(22, 200));
        PN_TrackDetails.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        LB_TrackPoints.setText("...");
        jPanel4.add(LB_TrackPoints);

        PN_TrackDetails.add(jPanel4, java.awt.BorderLayout.PAGE_END);

        PN_Elevations.setBackground(java.awt.Color.white);
        PN_Elevations.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        PN_Elevations.setLayout(new java.awt.BorderLayout());
        PN_TrackDetails.add(PN_Elevations, java.awt.BorderLayout.CENTER);

        jPanel6.add(PN_TrackDetails, java.awt.BorderLayout.SOUTH);

        TAB_Item.addTab("Tracks", jPanel6);

        PN_GPX.add(TAB_Item, java.awt.BorderLayout.CENTER);

        jPanel11.setMaximumSize(new java.awt.Dimension(32767, 24));
        jPanel11.setOpaque(false);
        jPanel11.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        LB_GPX.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LB_GPX.setText("...");
        jPanel11.add(LB_GPX);

        PN_GPX.add(jPanel11, java.awt.BorderLayout.NORTH);

        jPanel1.add(PN_GPX, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_CopyRoutePoints;
    private javax.swing.JButton BT_Delete;
    private javax.swing.JButton BT_DeleteRoute;
    private javax.swing.JButton BT_DeleteRoutePoint;
    private javax.swing.JButton BT_DeleteWaypoint;
    private javax.swing.JButton BT_Edit;
    private javax.swing.JButton BT_EditRoute;
    private javax.swing.JButton BT_EditRoutePoint;
    private javax.swing.JButton BT_EditTrack;
    private javax.swing.JButton BT_EditWaypoint;
    private javax.swing.JButton BT_Export;
    private javax.swing.JButton BT_Import;
    private javax.swing.JButton BT_New;
    private javax.swing.JButton BT_NewRoute;
    private javax.swing.JButton BT_PasteRoutePoints;
    private javax.swing.JCheckBox CB_Extrude;
    private javax.swing.JComboBox<RteType> CMB_Route;
    private javax.swing.JComboBox<TrkType> CMB_Tracks;
    private javax.swing.JLabel LB_GPX;
    private javax.swing.JLabel LB_TrackPoints;
    private javax.swing.JPanel PN_Elevations;
    private javax.swing.JPanel PN_GPX;
    private javax.swing.JPanel PN_TrackDetails;
    private javax.swing.JTabbedPane TAB_Item;
    private javax.swing.JTable TB_GPX;
    private javax.swing.JTable TB_Routes;
    private javax.swing.JTable TB_Track;
    private javax.swing.JTable TB_Waypoints;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JToolBar jToolBar5;
    // End of variables declaration//GEN-END:variables

    /**
     * Fill waypoints, routes, track table
     *
     * @param g
     */
    private void fill(GPX g) {
        //--- Clear all renderables
        layer.clearTracks();
        layer.clearRoutes();
        layer.removeAllRenderables();

        //--- Populate with new renderables
        //--- Waypoints
        DefaultTableModel model = (DefaultTableModel) TB_Waypoints.getModel();
        model.setRowCount(0);
        for (int i = 0; i < g.wpt.size(); i++) {
            WptType w = g.wpt.get(i);
            Object obj[] = {w, w.lat, w.lon, w.ele};
            model.addRow(obj);

            if (w.cone != null) layer.removeRenderable(w.cone);
            layer.addRenderable(w.generateCone());

        }

        //--- Routes
        model = (DefaultTableModel) TB_Routes.getModel();
        model.setRowCount(0);
        CMB_Route.removeItemListener(this);
        CMB_Route.removeAllItems();
        for (int i = 0; i < g.rte.size(); i++) {
            RteType r = g.rte.get(i);
            CMB_Route.addItem(r);
            //--- Display first route found
            if (i == 0) fillRoute(r);
        }
        CMB_Route.addItemListener(this);

        //--- Tracks
        model = (DefaultTableModel) TB_Track.getModel();
        model.setRowCount(0);
        CMB_Tracks.removeItemListener(this);
        CMB_Tracks.removeAllItems();
        for (int i = 0; i < g.trk.size(); i++) {
            TrkType r = g.trk.get(i);
            CMB_Tracks.addItem(r);
            //--- Display first one found
            if (i == 0) fillTrack(r);
        }
        CMB_Tracks.addItemListener(this);

    }

    public void fillRoute(RteType r) {
        DefaultTableModel model = (DefaultTableModel) TB_Routes.getModel();
        model.setRowCount(0);

        if (r.path != null) layer.removeRoute(r.path);

        ArrayList<Position> positions = new ArrayList<>();
        for (int i = 0; i < r.rtept.size(); i++) {
            WptType w = r.rtept.get(i);
            Object obj[] = {w, w.lat, w.lon, w.ele};
            model.addRow(obj);

            Position ref = Position.fromDegrees(w.lat, w.lon, w.ele);
            positions.add(ref);

            //--- Display point, removed first to be coherente with states
            //--- and to avoid to add two times
            if (w.cylinder != null) layer.removeRenderable(w.cylinder);
            layer.addRenderable(w.generateCylinder());

        }

        //--- Prepare paths
        Path p = new Path();
        p.setFollowTerrain(true);
        p.setShowPositions(true);
        p.setPositionColors(new PositionColors() {
            @Override
            public Color getColor(Position position, int ordinal) {
                return Color.RED;
            }
        });
        p.setDragEnabled(false);
        p.setSurfacePath(true);
        p.setPositions(positions);
        r.path = p;
        layer.addRoute(p);

    }

    public void fillTrack(TrkType t) {
        DefaultTableModel model = (DefaultTableModel) TB_Track.getModel();
        model.setRowCount(0);

        ArrayList<Position> positions = new ArrayList<>();
        ArrayList<Double> elevations = new ArrayList<>();
        for (int i = 0; i < t.trkseg.size(); i++) {
            TrkSeg s = t.trkseg.get(i);
            for (int j = 0; j < s.trkpt.size(); j++) {
                WptType w = s.trkpt.get(j);
                Object obj[] = {s, w, w.lat, w.lon, w.ele};
                model.addRow(obj);

                Position ref = Position.fromDegrees(w.lat, w.lon, w.ele);
                positions.add(ref);

                elevations.add(ref.elevation);
            }
            if (s.path == null) {
                //--- Prepare paths
                Path p = new Path();
                p.setFollowTerrain(true);
                p.setShowPositions(true);
                p.setPositionColors(new PositionColors() {
                    @Override
                    public Color getColor(Position position, int ordinal) {
                        //--- Find the selected tracked point
                        int index = TB_Track.getSelectedRow();
                        if (index == ordinal) return Color.RED;
                        return null;
                    }
                });
                p.setDragEnabled(false);
                if (CB_Extrude.isSelected()) {
                    p.setExtrude(true);

                } else {
                    p.setSurfacePath(true);

                }
                // p.setHighlighted(true);
                p.setPositions(positions);
                s.path = p;
                layer.addTrack(p);

            }
        }
        //--- Elevations for all segment
        jele.setElevations(elevations);
    }

}
