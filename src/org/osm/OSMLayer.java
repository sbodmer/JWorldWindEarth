/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osm;

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
import java.util.Random;

/**
 *
 * @author sbodmer
 */
public class OSMLayer extends BasicMercatorTiledImageLayer {
    static Random random = new Random();
    
    public OSMLayer() {
        super(makeLevels());
        
        setDrawTileIDs(false);
        
    }

    @Override
    public String toString() {
        return "Openstreetmap";
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
                char c = sub.charAt(random.nextInt(sub.length()));
                s = s.replaceAll("\\[" + sub + "\\]", "" + c);
            }
            int x = tile.getColumn();
            int y = tile.getRow();
            int z = tile.getLevelNumber();
            int maxT = (int) Math.pow(2, z);
            s += "/"+z+"/"+ x+"/"+(maxT-y-1)+""+tile.getFormatSuffix();
            // System.out.println("S:"+s);
            
            return new URL(s);

        }
    }

    private static LevelSet makeLevels() {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/OSM");
        params.setValue(AVKey.SERVICE, "http://[abc].tile.openstreetmap.org");
        params.setValue(AVKey.DATASET_NAME, "a");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, 23);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(180d), Angle.fromDegrees(360d)));
        params.setValue(AVKey.SECTOR, MercatorSector.fromSector(Sector.FULL_SPHERE)); //new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new OSMLayer.URLBuilder());
        
        return new LevelSet(params);
    }
    
}
