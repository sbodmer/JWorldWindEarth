/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.worldwindearth.trek;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author sbodmer
 */
public class WptTypeTransferable implements Transferable {
    public static DataFlavor wptTypeDataFlavor = new DataFlavor(WptTypeTransferable.class, DataFlavor.javaJVMLocalObjectMimeType);

	ArrayList<WptType> v = new ArrayList<>();
	String info = null;

	public WptTypeTransferable() {
		super();
	}
	//**************************************************************************
	//*** Transferrable
	//**************************************************************************

	public void add(WptType obj) {
		v.add(obj);
	}

	public boolean remove(WptType obj) {
		return v.remove(obj);
	}

	public WptType remove(int i) {
		return v.remove(i);
	}

	/**
	 * Some additional info (like "cut" or "copy" for clipboard operation)
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	public String getInfo() {
		return info;
	}

	public int size() {
		return v.size();
	}

	public void clear() {
		v.clear();
	}

	public WptType get(int i) {
		return v.get(i);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor dataflavors[] = new DataFlavor[1];
		dataflavors[0] = wptTypeDataFlavor;
		return dataflavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.isMimeTypeEqual(wptTypeDataFlavor.getMimeType())) {
			return true;

		} else {
			return false;
		}
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.isMimeTypeEqual(wptTypeDataFlavor.getMimeType())) {
			return this;

		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}    
}
