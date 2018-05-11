package com.leo.google.utility;

import java.io.File;

/**
 * Accepts only mp3 files.
 * @author Parabola
 */
public class FileFilter extends javax.swing.filechooser.FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		return f.getName().endsWith(".mp3");
	}

	@Override
	public String getDescription() {
		return "mp3";
	}
}
