/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.wms;

import java.net.URI;

/**
 * Simple WMS container
 * 
 * @author sbodmer
 */
public class WMSServer {
    String name = "";
    URI api = null;
    
    public WMSServer(String name, URI api) {
        this.name = name;
        this.api = api;
    }
    
    //**************************************************************************
    //*** API
    //**************************************************************************
    @Override
    public String toString() {
        return name;
    }
    
    public URI getApi() {
        return api;
    }
    
}
