/*
 * Copyright 2015-Present Entando S.r.l. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.entando.entando.aps.system.services.storage;

import com.agiletec.aps.system.exception.ApsSystemException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class LocalStorageManager implements IStorageManager {

	private static final Logger logger = LoggerFactory.getLogger(LocalStorageManager.class);

	private String baseURL;
	private String baseDiskRoot;
	private String protectedBaseDiskRoot;
	private String protectedBaseURL;
	private String allowedEditExtensions;


	public void init() throws Exception {
		logger.debug("{} ready", this.getClass().getName());
	}

	@Override
	public void saveFile(String subPath, boolean isProtectedResource, InputStream is) throws ApsSystemException, IOException {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		FileOutputStream outStream = null;
		try {
			File dir = new File(fullPath).getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			byte[] buffer = new byte[1024];
			int length;
			outStream = new FileOutputStream(fullPath);
			while ((length = is.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
				outStream.flush();
			}
		} catch (Throwable t) {
			logger.error("Error on saving file", t);
			throw new ApsSystemException("Error on saving file", t);
		} finally {
			if (null != outStream) {
				outStream.close();
			}
			if (null != is) {
				is.close();
			}
		}
	}

	@Override
	public boolean deleteFile(String subPath, boolean isProtectedResource) {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		File file = new File(fullPath);
		if (file.exists()) {
			return file.delete();
		}
		return false;
	}

	@Override
	public void createDirectory(String subPath, boolean isProtectedResource) {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		File dir = new File(fullPath);
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
	}

	@Override
	public void deleteDirectory(String subPath, boolean isProtectedResource) {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		File dir = new File(fullPath);
		this.delete(dir);
	}

	private boolean delete(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				String[] filesName = file.list();
				if (filesName != null) {
					for (String s : filesName) {
						File fileToDelete = new File(file.getAbsoluteFile() + File.separator + s);
						this.delete(fileToDelete);
					}
				}
				boolean deleted = file.delete();

				if(!deleted) {
					logger.warn("Failed to delete  file {}", file.getAbsolutePath());
				}
			} else {
				return file.delete();
			}
		}
		return false;
	}

	@Override
	public InputStream getStream(String subPath, boolean isProtectedResource) throws ApsSystemException {
		try {
			subPath = (null == subPath) ? "" : subPath;
			String fullPath = this.createFullPath(subPath, isProtectedResource);
			File file = new File(fullPath);
			if (file.exists() && !file.isDirectory()) {
				return new FileInputStream(file);
			}
		} catch (Throwable t) {
			logger.error("Error extracting stream", t);
			throw new ApsSystemException("Error extracting stream", t);
		}
		return null;
	}

	@Override
	public boolean exists(String subPath, boolean isProtectedResource) {
		File file = this.getFile(subPath, isProtectedResource);
		return file.exists();
	}

	protected File getFile(String subPath, boolean isProtectedResource) {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		return new File(fullPath);
	}

	@Override
	public String getResourceUrl(String subPath, boolean isProtectedResource) {
		subPath = (null == subPath) ? "" : subPath;
		String baseUrl = (!isProtectedResource) ? this.getBaseURL() : this.getProtectedBaseURL();
		return this.createPath(baseUrl, subPath, true);
	}

	@Override
	public String readFile(String subPath, boolean isProtectedResource) throws ApsSystemException {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		File file = new File(fullPath);
		try {
			return FileUtils.readFileToString(file, CharEncoding.UTF_8);
		} catch (Throwable t) {
			logger.error("Error reading File with path {}", subPath, t);
			throw new ApsSystemException("Error reading file", t);
		}
	}

	@Override
	public void editFile(String subPath, boolean isProtectedResource, InputStream is) throws ApsSystemException {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		String tempFilePath = null;
		try {
			File oldFile = new File(fullPath);
			if (oldFile.exists()) {
				String tempDir = System.getProperty("java.io.tmpdir");
				tempFilePath = tempDir + File.separator + subPath;
				FileUtils.copyFile(oldFile, new File(tempFilePath));
			}
			this.saveFile(subPath, isProtectedResource, is);
		} catch (Throwable t) {
			try {
				if (null != tempFilePath) {
					FileUtils.moveFile(new File(tempFilePath), new File(fullPath));
				}
			} catch (Throwable tr) {
				logger.error("Error restoring File from path {} to path", tempFilePath, fullPath, tr);
			}
			logger.error("Error writing File with path {}", subPath, t);
			throw new ApsSystemException("Error editing file", t);
		} finally {
			if (null != tempFilePath) {
				boolean deleted = new File(tempFilePath).delete();

				if(!deleted) {
					logger.warn("Failed to delete  file {}", tempFilePath);
				}
			}
		}
	}

	@Override
	public String[] list(String subPath, boolean isProtectedResource) {
		return this.list(subPath, isProtectedResource, null);
	}

	@Override
	public String[] listDirectory(String subPath, boolean isProtectedResource) {
		return this.list(subPath, isProtectedResource, true);
	}

	@Override
	public String[] listFile(String subPath, boolean isProtectedResource) {
		return this.list(subPath, isProtectedResource, false);
	}

	private String[] list(String subPath, boolean isProtectedResource, Boolean searchDirectory) {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		File directory = new File(fullPath);
		if (directory.exists() && directory.isDirectory()) {
			String[] objects = new String[]{};
			String folder = fullPath;
			if (!folder.endsWith("/")) {
				folder += "/";
			}
			String[] contents = directory.list();
			if (null == searchDirectory) {
				objects = contents;
			} else {
				if (contents != null) {
					for (String string : contents) {
						File file = new File(folder + string);
						if ((file.isDirectory() && searchDirectory) || (!file.isDirectory() && !searchDirectory)) {
							objects = this.addChild(string, objects);
						}
					}
				}
			}
			Arrays.sort(Objects.requireNonNull(objects));
			return objects;
		}
		return null;
	}

	protected String[] addChild(String stringToAdd, String[] objects) {
		int len = objects.length;
		String[] newArray = new String[len + 1];
		System.arraycopy(objects, 0, newArray, 0, len);
		newArray[len] = stringToAdd;
		return newArray;
	}

	private String createFullPath(String subPath, boolean isProtectedResource) {
		subPath = (null == subPath) ? "" : subPath;
		String diskRoot = (!isProtectedResource) ? this.getBaseDiskRoot() : this.getProtectedBaseDiskRoot();
		return this.createPath(diskRoot, subPath, false);
	}

	private String createPath(String basePath, String subPath, boolean isUrlPath) {
		subPath = (null == subPath) ? "" : subPath;
		String separator = (isUrlPath) ? "/" : File.separator;
		boolean baseEndWithSlash = basePath.endsWith(separator);
		boolean subPathStartWithSlash = subPath.startsWith(separator);
		if ((baseEndWithSlash && !subPathStartWithSlash) || (!baseEndWithSlash && subPathStartWithSlash)) {
			return basePath + subPath;
		} else if (!baseEndWithSlash && !subPathStartWithSlash) {
			return basePath + separator + subPath;
		} else {
			String base = basePath.substring(0, basePath.length() - File.separator.length());
			return base + subPath;
		}
	}

	@Override
	public BasicFileAttributeView[] listAttributes(String subPath, boolean isProtectedResource) {
		return this.listAttributes(subPath, isProtectedResource, null);
	}

	@Override
	public BasicFileAttributeView[] listDirectoryAttributes(String subPath, boolean isProtectedResource) {
		return this.listAttributes(subPath, isProtectedResource, true);
	}

	@Override
	public BasicFileAttributeView[] listFileAttributes(String subPath, boolean isProtectedResource) {
		return this.listAttributes(subPath, isProtectedResource, false);
	}

	private BasicFileAttributeView[] listAttributes(String subPath, boolean isProtectedResource, Boolean searchDirectory) {
		subPath = (null == subPath) ? "" : subPath;
		String fullPath = this.createFullPath(subPath, isProtectedResource);
		File directory = new File(fullPath);
		if (directory.exists() && directory.isDirectory()) {
			BasicFileAttributeView[] objects = new BasicFileAttributeView[]{};
			String folder = fullPath;
			if (!folder.endsWith("/")) {
				folder += "/";
			}
			String[] contents = directory.list();
			for (String string : Objects.requireNonNull(contents)) {
				File file = new File(folder + string);
				if (null == searchDirectory || (file.isDirectory() && searchDirectory) || (!file.isDirectory() && !searchDirectory)) {
					BasicFileAttributeView bfav = new BasicFileAttributeView(file);
					objects = this.addChild(bfav, objects);
				}
			}
			Arrays.sort(objects);
			return objects;
		}
		return null;
	}

	protected BasicFileAttributeView[] addChild(BasicFileAttributeView elementToAdd, BasicFileAttributeView[] objects) {
		int len = objects.length;
		BasicFileAttributeView[] newArray = new BasicFileAttributeView[len + 1];
		System.arraycopy(objects, 0, newArray, 0, len);
		newArray[len] = elementToAdd;
		return newArray;
	}

	@Override
	public BasicFileAttributeView getAttributes(String subPath, boolean isProtectedResource) {
		File file = this.getFile(subPath, isProtectedResource);
		if (!file.exists()) {
			return null;
		} else {
			return new BasicFileAttributeView(file);
		}
	}

	protected String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	protected String getBaseDiskRoot() {
		return baseDiskRoot;
	}

	public void setBaseDiskRoot(String baseDiskRoot) {
		this.baseDiskRoot = baseDiskRoot;
	}

	protected String getProtectedBaseDiskRoot() {
		return protectedBaseDiskRoot;
	}

	public void setProtectedBaseDiskRoot(String protBaseDiskRoot) {
		this.protectedBaseDiskRoot = protBaseDiskRoot;
	}

	protected String getProtectedBaseURL() {
		return protectedBaseURL;
	}

	public void setProtectedBaseURL(String protBaseURL) {
		this.protectedBaseURL = protBaseURL;
	}

	public void setAllowedEditExtensions(String allowedEditExtensions) {
		this.allowedEditExtensions = allowedEditExtensions;
	}

	public String getAllowedEditExtensions() {
		return allowedEditExtensions;
	}
}
