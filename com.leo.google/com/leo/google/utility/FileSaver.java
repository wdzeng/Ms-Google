package com.leo.google.utility;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * A custom file saver helps prevent illegal file name.
 * @author Parabola
 */
@SuppressWarnings("serial")
public class FileSaver extends JFileChooser {

	@Override
	public void approveSelection() {
		super.approveSelection();
		File f = getSelectedFile();
		if (!f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, "檔名不合法", "錯誤", JOptionPane.ERROR_MESSAGE);
			return;
		}
		f.delete();
	}
}
