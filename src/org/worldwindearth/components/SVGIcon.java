/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.worldwindearth.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;
import java.awt.Color;
import java.net.URL;

/**
 *
 * @author sbodmer
 */
public class SVGIcon {
    public static Color LIGHT = new Color(52, 73, 94);
    public static Color DARK = new Color(186, 184, 182);
    public static Color FROM = new Color(0, 0, 0);
    
    static ColorFilter cf = new ColorFilter();
     
    static {
        cf.add(FROM, LIGHT, DARK);
        
    }

    /**
     * The path is the resource path in the jar (ex:
     * "Resources/Icons/Common/unknown.svg), if not found the unknown icon is
     * used
     *
     * @param size
     * @param path
     * @return
     */
    public static FlatSVGIcon newIcon(int size, String path) {
        try {
            URL url = SVGIcon.class.getResource(path);
            FlatSVGIcon icon = new FlatSVGIcon(url);
            icon.setColorFilter(cf);
            return icon.derive(size, size);
            

        } catch (Exception ex) {
            ex.printStackTrace();
            

        }
        return null;
    }
    
}
