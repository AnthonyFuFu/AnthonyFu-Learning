package com.anthonyfu.web.util;

import static com.anthonyfu.web.constant.CommonConstant.YEAR_LIST;
import java.util.Locale;

import com.anthonyfu.web.constant.FileTypeConstant;

public class FileUtil {

	// 根據檔名規則判定 S3 Prefix (資料夾分類)
	public static String getFolderPrefix(String filename) {
	    if (filename == null || filename.isBlank()) {
	        return "GERBER/";
	    }
	    String lowerName = filename.trim().toLowerCase(Locale.ROOT);
	    if (FileTypeConstant.FAB_LIST.stream().anyMatch(lowerName::endsWith)) {
	        return "FAB/";
	    } else if (FileTypeConstant.CAD_LIST.stream().anyMatch(lowerName::contains)) {
	        return "CAD/";
	    } else if (FileTypeConstant.ODB_LIST.stream().anyMatch(lowerName::contains)) {
	        return "ODB/";
	    } else if (FileTypeConstant.PANEL_LIST.stream().anyMatch(lowerName::contains)) {
	        return "PANEL/";
	    } else if (FileTypeConstant.SMD_LIST.stream().anyMatch(lowerName::contains)) {
	        return "SMD/";
	    } else if (FileTypeConstant.SPEC_LIST.stream().anyMatch(lowerName::contains)) {
	        return "SPEC/";
	    } else {
	    	return "GERBER/";
	    }
	}

	private static final String VERSION_SEPARATOR_REGEX = "[_.\\-\\s]+";

	public static String parsePcbNo(String fileName) {

		if (fileName == null || fileName.isBlank()) {
			return null;
		}

		String fileNameTrimmed = fileName.trim();
		int firstHyphenIndex = fileNameTrimmed.indexOf("-");

		// 沒有 "-"，或 "-" 位於檔名第一個字元，視為格式不正確
		if (firstHyphenIndex <= 0) {
			return null;
		}

		String pcbNo = fileNameTrimmed.substring(0, firstHyphenIndex).trim();

		return pcbNo.isEmpty() ? null : pcbNo.toUpperCase(Locale.ROOT);
	}

	public static String parsePcbVersion(String fileName) {

		if (fileName == null || fileName.isBlank()) {
			return null;
		}

		String fileNameTrimmed = fileName.trim();
		int firstHyphenIndex = fileNameTrimmed.indexOf("-");

		// 沒有 "-"，或 "-" 後面沒有內容，視為格式不正確
		if (firstHyphenIndex < 0 || firstHyphenIndex == fileNameTrimmed.length() - 1) {
			return null;
		}

		String versionPart = fileNameTrimmed.substring(firstHyphenIndex + 1).trim();

		if (versionPart.isEmpty()) {
			return null;
		}

		versionPart = versionPart.replaceFirst("^[_.\\-\\s]+", "");

		if (versionPart.isEmpty()) {
			return null;
		}

		String pcbVersion = versionPart.split(VERSION_SEPARATOR_REGEX, 2)[0].trim().toUpperCase(Locale.ROOT);

		return pcbVersion.isEmpty() ? null : pcbVersion;
	}

	public static String getYear(String virtualPath) {
		if (virtualPath == null || virtualPath.isBlank()) {
			return null;
		}
		String path = virtualPath.trim();
		// 移除最後多餘的反斜線
		while (path.endsWith("\\")) {
			path = path.substring(0, path.length() - 1);
		}
		int lastBackslashIndex = path.lastIndexOf('\\');
		if (lastBackslashIndex < 0 || lastBackslashIndex == path.length() - 1) {
			return null;
		}

		String year = path.substring(lastBackslashIndex + 1).trim();
		if (YEAR_LIST.contains(year)) {
			return year;
        }
		return null;
	}

}
