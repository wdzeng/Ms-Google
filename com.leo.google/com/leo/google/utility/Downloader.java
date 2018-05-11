package com.leo.google.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * A class helps download google sound from google's server.
 * @author Parabola
 */
public final class Downloader {

	private static final String URL_FORMAT = "http://translate.google.com/translate_tts?ie=UTF-8&total=1&idx=0&textlen=%d&client=tw-ob&q=%s&tl=zh-CN&ttsspeed=%s";

	private static InputStream getInputStream(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
		conn.connect();
		return conn.getInputStream();
	}

	private static String getURLAddress(String text, boolean hSpeed) {
		try {
			String code = URLEncoder.encode(text, "UTF-8");
			String speed = hSpeed ? "1" : "0.24";
			return String.format(URL_FORMAT, text.length(), code, speed);
		}
		catch (UnsupportedEncodingException neverHappen) {
			throw new InternalError(neverHappen);
		}
	}

	/**
	 * Downloads a google sound (mp3) to a specific location
	 * @param text text of sound
	 * @param fast true indicates a higher speaking speed
	 * @param location location to download the file
	 * @return true, always
	 * @throws IOException if I/O error occurs
	 */
	public static boolean download(String text, boolean fast, File location) throws IOException {
		byte[] buff = new byte[1024];
		int len;
		try (FileOutputStream out = new FileOutputStream(location) ; InputStream in = getInputStream(getURL(text, fast))) {
			while ((len = in.read(buff)) != -1) {
				out.write(buff, 0, len);
			}
		}
		return true;
	}

	/**
	 * Gets the corresponding url to a given text.
	 * @param text text of sound
	 * @param fast true indicates a higher speaking speed
	 * @return an url
	 */
	public static URL getURL(String text, boolean fast) {
		try {
			return new URL(getURLAddress(text, fast));
		}
		catch (MalformedURLException e) {
			//Never happen
			throw new InternalError(e);
		}
	}

	//No constructors
	private Downloader() {
	}
}
