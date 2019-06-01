/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.models;

/**
 *
 * @author sbodmer
 */
public interface ModelLoaderListener {
    public void modelLoading(String file, String message);
    public void modelLoaded(String file, String message);
    public void modelLoadingFailed(String file, String message);
}
