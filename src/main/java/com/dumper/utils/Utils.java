package com.dumper.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.engine.jdbc.NonContextualLobCreator;

/**
 * 
 * @author ksalnis
 *
 */
public class Utils {

	private static final String ZIP_EXTENSION = "zip";
	private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

	/**
	 * Zips whole directory
	 * 
	 * @param dirToCompress
	 * @param zipOutputFile
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void zipDirectory(String dirToCompress, String zipOutputFile)
			throws IOException, FileNotFoundException {
		ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(zipOutputFile));
		for (File file : new File(dirToCompress).listFiles()) {
			if (!file.getName().contains(ZIP_EXTENSION)) {
				ZipEntry entry = new ZipEntry(file.getName());
				zipFile.putNextEntry(entry);
				FileInputStream in = new FileInputStream(
						String.format("%s%s%s", dirToCompress, File.separator, file.getName()));
				IOUtils.copy(in, zipFile);
				IOUtils.closeQuietly(in);
			}
		}
		IOUtils.closeQuietly(zipFile);
	}

	/**
	 * Zips one file
	 * 
	 * @param fileToCompress
	 * @param zipOutputFile
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void zipFile(String fileToCompress, String zipOutputFile) throws IOException, FileNotFoundException {
		FileOutputStream fileOutputStream = new FileOutputStream(zipOutputFile);
		ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
		ZipEntry zipEntry = new ZipEntry(new File(fileToCompress).getName());
		zipOutputStream.putNextEntry(zipEntry);
		FileInputStream fileInputStream = new FileInputStream(fileToCompress);

		byte[] buf = new byte[1024];
		int bytesRead;
		while ((bytesRead = fileInputStream.read(buf)) > 0) {
			zipOutputStream.write(buf, 0, bytesRead);
		}
		IOUtils.closeQuietly(zipOutputStream);
		IOUtils.closeQuietly(fileOutputStream);
		IOUtils.closeQuietly(fileInputStream);
	}

	/**
	 * Removes html tags from the string
	 * 
	 * @param textWithHtml
	 * @return
	 */
	public static String removeHtmlTags(String textWithHtml) {
		if (textWithHtml == null || textWithHtml.length() == 0) {
			return textWithHtml;
		}

		Matcher m = REMOVE_TAGS.matcher(textWithHtml);
		return m.replaceAll("");
	}
	
	/**
	 * Escapes HTML
	 * 
	 * @param html
	 * @return
	 */
	public static String escapeHtml(String html){
		return StringEscapeUtils.escapeHtml4(html);
	}
	
	/**
	 * Unescape HTML
	 * 
	 * @param html
	 * @return
	 */
	public static String unescapeHtml(String html) {
		return StringEscapeUtils.unescapeHtml4(html);
	}

	/**
	 * Returns current time stamp
	 * 
	 * @return
	 */
	public static Date getCurrentTimestamp() {
		Calendar c = Calendar.getInstance(TimeZone.getDefault());
		return c.getTime();
	}

	/**
	 * Remove pre tags
	 * 
	 * @param threadDump
	 * @return
	 */
	public static String removePreTags(String threadDump) {
		return threadDump.replaceAll("<pre class=\"pre-threads\">", "").replaceAll("</pre>","");
	}
	
	/**
	 * Add pre tags
	 * 
	 * @param threadDump
	 * @return
	 */
	public static String addPreTags(String threadDump) {
		return "<pre class=\"pre-threads\">" + threadDump + "</pre>";
	}
	
	/**
	 * Writes thread dump into file
	 * 
	 * @param fileName
	 * @param minDepth
	 * @param filterRegex
	 */
	public static void writeThreadDumpToFile(String fileName, String fileStream) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(fileStream.toString());
		} catch (IOException e) {
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Creates clob object
	 * 
	 * @param reader
	 * @param length
	 * @return
	 */
	public static Clob createClob(Reader reader, int length) {
		return NonContextualLobCreator.INSTANCE.wrap(NonContextualLobCreator.INSTANCE.createClob(reader, length));
	}

	/**
	 * Deletes phd file of IBM JVM if exists
	 * 
	 * @param folder
	 * @param filePattern
	 */
	public static void deletePhdFile(File folder, String filePattern) {
		Collection<File> files = FileUtils.listFiles(folder, new String[] { "phd" }, false);
		filePattern = String.format(".*%s.*", filePattern);
		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
			File file = iterator.next();
			if (file.toString().matches(filePattern)) {
				FileUtils.deleteQuietly(file);
			}
		}
	}

	/**
	 * Generated truncated phd file name
	 * 
	 * @return
	 */
	public static String generatePhdFilePattern() {
		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHm");
			Date date = new Date();
			String timeStamp = dateFormat.format(date).toString();
			timeStamp = timeStamp.substring(0, timeStamp.length() - 1);
			return String.format("heapdump.%s", timeStamp);

		} catch (StringIndexOutOfBoundsException e) {
			throw new StringIndexOutOfBoundsException("Cannot substring string " + e);
		}
	}

	/**
	 * Returns full phd file path with given pattern
	 * 
	 * @param folder
	 * @param filePattern
	 * @return
	 */
	public static String matchPhdFileName(File folder, String filePattern) {
		Collection<File> files = FileUtils.listFiles(folder, new String[] { "phd" }, false);
		filePattern = String.format(".*%s.*", filePattern);
		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
			File file = iterator.next();
			if (file.toString().matches(filePattern)) {
				return (String) file.getAbsolutePath();
			}
		}
		return null;
	}

}