/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.microsoft.virtualearth;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author sbodmer
 */
public class VirtualearthLayer extends BasicMercatorTiledImageLayer {
    /**
     * The counter for the different servers (sequencial)
     */
    static int current = 0;
    
    public VirtualearthLayer() {
        super(makeLevels());
        
        setDrawTileIDs(true);
        
    }

    @Override
    public String toString() {
        return "Microsoft Virtualearth";
    }

    //**************************************************************************
    //*** Private
    //**************************************************************************
    private static class URLBuilder implements TileUrlBuilder {

        @Override
        public URL getURL(Tile tile, String imageFormat) throws MalformedURLException {
            String s = tile.getLevel().getService();
            //--- Find the current server to use
            int i1 = s.indexOf('[');
            if (i1 != -1) {
                int i2 = s.indexOf(']');
                String sub = s.substring(i1 + 1, i2);
                int l = sub.length();
                current++;
                if (current >= sub.length())
                    current = 0;
                char c = sub.charAt(current);
                s = s.replaceAll("\\[" + sub + "\\]", "" + c);
            }
            int x = tile.getColumn();
            int y = tile.getRow();
            int z = tile.getLevelNumber();
            //--- WW origin in bottom left (virtual earth is top left)
            int maxT = (int) Math.pow(2, z+1);
            s += "/a"+ xy2quad(x,maxT-y-1,z+1)+".jpg?g=1";
            // System.out.println("S:"+s);
            
            return new URL(s);

        }
    }

    private static LevelSet makeLevels() {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Microsoft/Virtualearth");
        params.setValue(AVKey.SERVICE, "http://a[123].ortho.tiles.virtualearth.net/tiles");
        params.setValue(AVKey.DATASET_NAME, "a");
        params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
        params.setValue(AVKey.NUM_LEVELS, 19);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(90d), Angle.fromDegrees(180d)));
        params.setValue(AVKey.SECTOR, MercatorSector.fromSector(Sector.FULL_SPHERE)); //new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new VirtualearthLayer.URLBuilder());

        return new LevelSet(params);
    }
    
    
    
    /**
     * Return the quad string for the passed x,y for the given zoom
     *
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    public static String xy2quad(int x, int y, int zoom) {
        StringBuilder quadKey = new StringBuilder();
        for (int i = zoom; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((x & mask) != 0) {
                digit++;
            }
            if ((y & mask) != 0) {
                digit++;
                digit++;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();

    }

    /**
     * Return the x,y and the zoom for the passed quad
     * @param quad
     * @param zoom
     * @return 
     */
    public static int[] quad2xy(String quad) {
        int xyz[] = { 0,0,0};
        xyz[2] = quad.length();
        for (int i = xyz[2]; i > 0; i--) {
            int mask = 1 << (i - 1);
            switch (quad.charAt(xyz[2] - i)) {
                case '0':
                    break;

                case '1':
                    xyz[0] |= mask;
                    break;

                case '2':
                    xyz[1] |= mask;
                    break;

                case '3':
                    xyz[0] |= mask;
                    xyz[1] |= mask;
                    break;

                    
            }
        }
        return xyz;
    }
    
    public static void main(String arg[]) {
        int x = 0;
        int y = 0;
        int z = 1;
        int maxT = (int) Math.pow(2, z);
        String q = xy2quad(x, maxT-y-1, z);
        System.out.println("M:"+maxT+" X:"+x+" Y:"+y+" => "+q);
    }
}
