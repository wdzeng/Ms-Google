package com.leo.google;

import com.leo.google.utility.Downloader;
import com.leo.google.utility.FileFilter;
import com.leo.google.utility.FileSaver;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * An application that helps download google sound from google's server.
 * @author Parabola
 */
@SuppressWarnings("serial")
public class Application extends JFrame implements ActionListener {

	private static final File TMP_FOLDER = new File(System.getProperty("java.io.tmpdir"));

	/**
	 * Copy a file using high-efficient file channel (NIO).
	 * @param source source location
	 * @param dest   destination location
	 * @return true if task is completed
	 */
	private static boolean copyFile(File source, File dest) {
		try (FileChannel sourceChannel = new FileInputStream(source).getChannel() ; FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
			destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Queires if this text contains effective character to be played.
	 * @param text text to be checked
	 * @return true if this text is effective
	 */
	private static boolean isTextValid(String text) {
		return !text.replaceAll("\\s", "").isEmpty();
	}

	/**
	 * Replaces all invalid character in a string to another, so as to get a valid file name.
	 * @param str string to be modified
	 * @return a legal file name
	 */
	private static String toValidFileName(String str) {
		return str.replaceAll("[:\\*\\\\\\/\\?\\\"\\|]", "");
	}

	private JTextArea input;
	private JCheckBox autoDownload;
	private JButton listen;
	private JButton download;
	private File dfDownloadLoc;
	private JLabel hint;

	public Application() {
		super("Google小姐產生器");
		createGUI();

		pack();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				//Before closing application, delete all tmp files
				deleteTmpFiles();
				System.exit(0);
			}
		});
	}



	private void createGUI() {

		//Set buttons and check box
		listen = new JButton("試聽");
		listen.addActionListener(this);
		download = new JButton("下載");
		download.addActionListener(this);
		autoDownload = new JCheckBox("啟用預設下載位置");
		autoDownload.addActionListener(this);
		JPanel btnPanel = new JPanel();
		btnPanel.add(listen);
		btnPanel.add(download);
		btnPanel.add(autoDownload);
		hint = new JLabel("作者: 雙曲線");
		JPanel south = new JPanel(new BorderLayout());
		south.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		south.add(hint, BorderLayout.CENTER);
		south.add(btnPanel, BorderLayout.EAST);

		//Set text area
		input = new JTextArea("輸入文字");
		input.setLineWrap(true);
		input.setWrapStyleWord(true);
		JScrollPane scr = new JScrollPane(input);
		scr.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		scr.setPreferredSize(new Dimension(400, 150));

		//Add components
		add(scr, BorderLayout.CENTER);
		add(south, BorderLayout.SOUTH);
	}

	/**
	 * Deletes all the temporary files in the temp folder.
	 */
	private void deleteTmpFiles() {
		File[] files = TMP_FOLDER.listFiles();
		Arrays.stream(files).filter(file -> file.getName().startsWith("leoleoleo")).forEach(file -> file.delete());
	}

	private void download() {
		String text = input.getText();
		if (!isTextValid(text)) {
			return;
		}

		File dest;
		if (dfDownloadLoc == null) {
			dest = selectDir();
			if (dest == null) {
				return;
			}
			if (!dest.getName().endsWith(".mp3")) {
				dest = new File(dest.getParent(), dest.getName() + ".mp3");
			}
		}
		else {
			dest = new File(dfDownloadLoc, getDefaultFileName());
		}
		if (dest.exists()) {
			dest.delete();
		}


		File tempSource = getTempFile();
		if (tempSource.exists()) {
			if (!copyFile(tempSource, dest)) {
				hint.setText("發生未知異常");
			}
		}
		else {
			try {
				Downloader.download(text, true, dest);
			}
			catch (IOException e) {
				hint.setText("網路異常");
				e.printStackTrace();
				return;
			}
		}

		String hintText = text.trim();
		if (hintText.length() > 4) {
			hintText = hintText.substring(0, 3) + "...";
		}
		hint.setText("下載完成: " + hintText);
	}

	private String getDefaultFileName() {
		String text = input.getText();
		return "leoleoleo" + toValidFileName(text) + ".mp3";
	}

	private File getTempFile() {
		return new File(TMP_FOLDER, getDefaultFileName());
	}

	private void listen() {
		String text = input.getText();
		if (!isTextValid(text)) {
			return;
		}

		File temp = getTempFile();
		if (!temp.exists()) {
			temp.getParentFile().mkdirs();
			try {
				Downloader.download(text, true, temp);
			}
			catch (IOException e) {
				hint.setText("網路異常");
				e.printStackTrace();
				return;
			}
		}

		try {
			new Player(new FileInputStream(temp)).play();
		}
		catch (JavaLayerException | FileNotFoundException e) {
			//Never happen
			e.printStackTrace();
			hint.setText("發生未知異常");
		}
	}

	private File selectDir() {
		JFileChooser chooser = new FileSaver();
		chooser.setFileFilter(new FileFilter());
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("選擇下載位置");
		int option = chooser.showSaveDialog(this);
		if (option == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		else {
			return null;
		}
	}

	private void setDefaultDownloadLocation() {
		if (!autoDownload.isSelected()) {
			dfDownloadLoc = null;
			return;
		}

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("選擇預設下載位置");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			dfDownloadLoc = chooser.getCurrentDirectory();
		}
		else {
			autoDownload.setSelected(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == download) {
			new Thread(() -> download()).start();
		}
		else if (source == listen) {
			new Thread(() -> listen()).start();
		}
		else {
			setDefaultDownloadLocation();
		}
	}



	public static void main(String[] args) {

		new Application();
	}
}
