package gov.nasa.wms;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.WWXML;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.tinyrcp.App;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.worldwindearth.WWEPlugin;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.components.SVGIcon;

/**
 *
 * @author sbodmer
 */
public class JWMSWWEFactory extends JPanel implements WWEFactory, ActionListener, MouseListener {

    ResourceBundle bundle = null;
    App app = null;

    /**
     * The shared list of server
     */
    DefaultComboBoxModel<WMSServer> list = new DefaultComboBoxModel<>();

    /**
     * Creates new form JGridsLMFactory
     */
    public JWMSWWEFactory() {
        bundle = ResourceBundle.getBundle("gov.nasa.wms.WMS");

        initComponents();

    }

    //***************************************************************************
    //*** WWELayerfactory
    //***************************************************************************
    @Override
    public String getFactoryCategory() {
        return WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER;
    }

    @Override
    public String getFactoryFamily() {
        return PLUGIN_FAMILY_WORLDWIND_LAYER_WMS;
    }

    @Override
    public void initialize(App app) {
        this.app = app;

        BT_Add.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/add.svg"));
        BT_Up.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/up.svg"));
        BT_Down.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/down.svg"));
        BT_Edit.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/edit.svg"));
        BT_Delete.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/trash.svg"));
        
        BT_Add.addActionListener(this);
        BT_Delete.addActionListener(this);
        BT_Edit.addActionListener(this);
        BT_Down.addActionListener(this);
        BT_Up.addActionListener(this);

        BT_Reset.addActionListener(this);

        TB_Servers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TB_Servers.addMouseListener(this);
    }

    @Override
    public JComponent getFactoryConfigComponent() {
        return this;
    }

    @Override
    public void configure(Element config) {
        //--- Populate with the one is the configuration first
        if (config != null) {
            DefaultTableModel model = (DefaultTableModel) TB_Servers.getModel();
            NodeList nl = config.getChildNodes();
            for (int i = 0;i < nl.getLength();i++) {
                if (nl.item(i).getNodeName().equals("Server")) {
                    Element e = (Element) nl.item(i);
                    try {
                        WMSServer wms = new WMSServer(e.getAttribute("title"), new URI(e.getFirstChild().getNodeValue()));
                        list.addElement(wms);

                        //--- Add to table too
                        model.addRow(new Object[]{wms.getTitle(), wms.getApi()});

                    } catch (URISyntaxException ex) {
                        //---

                    }

                }
            }
        }

        //--- If list is empty, populate with default list
        if (list.getSize() == 0) {
            resetServerList();

        }
    }

    @Override
    public void store(Element config) {
        if (config == null) return;

        //--- Store the list of server
        for (int i = 0;i < list.getSize();i++) {
            WMSServer wms = list.getElementAt(i);
            Element e = config.getOwnerDocument().createElement("Server");
            e.setAttribute("title", wms.getTitle().replace('&',' '));
            e.appendChild(e.getOwnerDocument().createTextNode(wms.getApi().toString()));
            config.appendChild(e);
        }
    }

    @Override
    public Icon getFactoryIcon(int size) {
        return LB_Name.getIcon();
    }

    @Override
    public String getFactoryName() {
        return LB_Name.getText();
    }

    @Override
    public String getFactoryDescription() {
        return LB_Description.getText();
    }

    /**
     * The pass argument is the WorldWindow
     *
     * @param arg
     * @return
     */
    @Override
    public WWEPlugin newPlugin(Object arg) {
        return new JWMSWWEPlugin(this, (WorldWindow) arg, list);

    }

    @Override
    public void destroy() {
        //---
    }

    @Override
    public Object getProperty(String property) {
        return null;
    }

    public boolean doesFactorySupport(Object obj) {
        if (obj != null) return obj.toString().equals(WWEFactory.PLANET_EARTH);
        return false;
    }
    
    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("reset")) {
            resetServerList();

        } else if (e.getActionCommand().equals("delete")) {
            DefaultTableModel model = (DefaultTableModel) TB_Servers.getModel();
            int index = TB_Servers.getSelectedRow();
            if (index == -1) return;

            WMSServer wms = list.getElementAt(index);
            list.removeElement(wms);
            model.removeRow(index);

        } else if (e.getActionCommand().equals("edit")) {
            int index = TB_Servers.getSelectedRow();
            if (index == -1) return;

            edit(index);

        } else if (e.getActionCommand().equals("add")) {
            int index = TB_Servers.getSelectedRow();
            if (index == -1) index = 0;
            add(index);

        } else if (e.getActionCommand().equals("down")) {
            int index = TB_Servers.getSelectedRow();
            if (index >= list.getSize() - 1) return;

            DefaultTableModel model = (DefaultTableModel) TB_Servers.getModel();

            WMSServer src = list.getElementAt(index);
            list.removeElement(src);
            model.removeRow(index);

            list.insertElementAt(src, index + 1);
            model.insertRow(index + 1, new Object[]{src.getTitle(), src.getApi()});

            //--- Select row again
            TB_Servers.getSelectionModel().addSelectionInterval(index + 1, index + 1);

        } else if (e.getActionCommand().equals("up")) {
            int index = TB_Servers.getSelectedRow();
            if (index <= 0) return;

            DefaultTableModel model = (DefaultTableModel) TB_Servers.getModel();

            WMSServer src = list.getElementAt(index);
            list.removeElement(src);
            model.removeRow(index);

            list.insertElementAt(src, index - 1);
            model.insertRow(index - 1, new Object[]{src.getTitle(), src.getApi()});

            //--- Select row again
            TB_Servers.getSelectionModel().addSelectionInterval(index - 1, index - 1);

        }
    }

    //**************************************************************************
    //*** MouseListener
    //**************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == TB_Servers) {
            if (e.getClickCount() >= 2) {
                int index = TB_Servers.getSelectedRow();
                if (index == -1) return;

                edit(index);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //---
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //---
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //---
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //---
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LB_Name = new javax.swing.JLabel();
        LB_Description = new javax.swing.JLabel();
        TAB_Main = new javax.swing.JTabbedPane();
        PN_Servers = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TB_Servers = new javax.swing.JTable();
        TB_Tools = new javax.swing.JToolBar();
        BT_Add = new javax.swing.JButton();
        BT_Up = new javax.swing.JButton();
        BT_Down = new javax.swing.JButton();
        BT_Edit = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        BT_Delete = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        BT_Reset = new javax.swing.JButton();

        LB_Name.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nasa/wms/Resources/Icons/22x22/wms.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gov/nasa/wms/WMS"); // NOI18N
        LB_Name.setText(bundle.getString("factory_name")); // NOI18N

        LB_Description.setText("<html><body>\nWorldwind generic WMS servers\n</body></html>");
        LB_Description.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        setLayout(new java.awt.BorderLayout());

        PN_Servers.setLayout(new java.awt.BorderLayout());

        TB_Servers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title", "URL"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(TB_Servers);

        PN_Servers.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        TB_Tools.setBorder(null);
        TB_Tools.setFloatable(false);

        BT_Add.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Add.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/add.png"))); // NOI18N
        BT_Add.setToolTipText("add");
        BT_Add.setActionCommand("add");
        BT_Add.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_Add);

        BT_Up.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Up.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/up.png"))); // NOI18N
        BT_Up.setToolTipText("move up");
        BT_Up.setActionCommand("up");
        BT_Up.setFocusable(false);
        BT_Up.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Up.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Up.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_Tools.add(BT_Up);

        BT_Down.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Down.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/down.png"))); // NOI18N
        BT_Down.setToolTipText("move down");
        BT_Down.setActionCommand("down");
        BT_Down.setFocusable(false);
        BT_Down.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Down.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Down.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_Tools.add(BT_Down);

        BT_Edit.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_Edit.setToolTipText("edit");
        BT_Edit.setActionCommand("edit");
        BT_Edit.setFocusable(false);
        BT_Edit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Edit.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Edit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_Tools.add(BT_Edit);
        TB_Tools.add(jSeparator9);

        BT_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_Delete.setToolTipText("delete");
        BT_Delete.setActionCommand("delete");
        BT_Delete.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_Delete);

        PN_Servers.add(TB_Tools, java.awt.BorderLayout.PAGE_START);

        TAB_Main.addTab("Servers", PN_Servers);

        add(TAB_Main, java.awt.BorderLayout.CENTER);

        BT_Reset.setText("Reset to default fixed list");
        BT_Reset.setActionCommand("reset");
        jPanel1.add(BT_Reset);

        add(jPanel1, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton BT_Add;
    public javax.swing.JButton BT_Delete;
    public javax.swing.JButton BT_Down;
    public javax.swing.JButton BT_Edit;
    public javax.swing.JButton BT_Reset;
    public javax.swing.JButton BT_Up;
    public javax.swing.JLabel LB_Description;
    public javax.swing.JLabel LB_Name;
    public javax.swing.JPanel PN_Servers;
    public javax.swing.JTabbedPane TAB_Main;
    public javax.swing.JTable TB_Servers;
    public javax.swing.JToolBar TB_Tools;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JToolBar.Separator jSeparator9;
    // End of variables declaration//GEN-END:variables

    private void resetServerList() {
        //--- Clear shared list
        list.removeAllElements();

        //--- Clear table
        DefaultTableModel model = (DefaultTableModel) TB_Servers.getModel();
        model.setRowCount(0);

        //--- Load fixed list from external resource
        Document doc = WWXML.openDocumentURL(getClass().getResource("/gov/nasa/wms/Resources/Config/Servers.xml"));
        Element root = doc.getDocumentElement();
        NodeList nl = root.getChildNodes();
        for (int i = 0;i < nl.getLength();i++) {
            if (nl.item(i).getNodeName().equals("Server")) {
                try {
                    Element e = (Element) nl.item(i);
                    WMSServer wms = new WMSServer(e.getAttribute("title"), new URI(e.getFirstChild().getNodeValue()));
                    list.addElement(wms);

                    //--- Add to table too
                    model.addRow(new Object[]{wms.getTitle(), wms.getApi()});

                } catch (URISyntaxException ex) {
                    //---

                }
            }
        }
    }

    /**
     * Open input dialog to ask for wms server modification
     */
    private void edit(int index) {
        WMSServer wms = list.getElementAt(index);
        
        String title = JOptionPane.showInputDialog(getTopLevelAncestor(), "Title", wms.getTitle());
        if (title == null) return;
        wms.setTitle(title);
        TB_Servers.setValueAt(wms.getTitle(), index, 0);
        
        String url = JOptionPane.showInputDialog(getTopLevelAncestor(), "URL", wms.getApi());
        if (url != null) {
            try {
                wms.setApi(new URI(url));
                TB_Servers.setValueAt(wms.getApi(), index, 1);
        
            } catch (URISyntaxException ex) {
                JOptionPane.showMessageDialog(getTopLevelAncestor(), "" + ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void add(int index) {
        String title = JOptionPane.showInputDialog(getTopLevelAncestor(), "Title", "WMS");
        if (title == null) return;

        String url = JOptionPane.showInputDialog(getTopLevelAncestor(), "URL", "http://");
        if (url != null) {
            try {
                URI api = new URI(url);
                WMSServer wms = new WMSServer(title, api);
                list.insertElementAt(wms, index);
                DefaultTableModel model = (DefaultTableModel) TB_Servers.getModel();

                model.insertRow(index, new Object[]{wms.getTitle(), wms.getApi()});

            } catch (URISyntaxException ex) {
                JOptionPane.showMessageDialog(getTopLevelAncestor(), "" + ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
