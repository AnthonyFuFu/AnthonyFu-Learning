package com.anthonyfu.web.batch.s3File;

import java.util.Locale;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.anthonyfu.web.model.S3File;
import com.anthonyfu.web.model.S3FileDto;
import com.anthonyfu.web.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3FileProcessor implements ItemProcessor<S3FileDto, S3File> {

	@Override
	public S3File process(S3FileDto csv) throws Exception {

		// 先整理資料欄位
		String serialID = CommonUtil.toNull(csv.getSerialID());
		String nodeType = CommonUtil.toNull(csv.getNodeType());
		String fPath = CommonUtil.toNull(csv.getFPath());
		String fName = CommonUtil.toNull(csv.getFName());
		String fSaveName = CommonUtil.toNull(csv.getFSaveName());

		// f_type 優先使用原始欄位。 如果 f_type 為空，則從 f_save_name 取得副檔名。
		String fType = getFileType(csv.getFType(), fSaveName);

		/*
		 * 如果無法取得 f_type， 直接略過這筆資料，不交給 ItemWriter。
		 * Spring Batch 收到 null 時， 會將該筆資料視為 filtered item。
		 */
		if (fType == null) {
			log.warn("Skip file record because f_type cannot be determined. "
					+ "serial_id=[{}], original_f_type=[{}], "
					+ "f_save_name=[{}], f_name=[{}], f_path=[{}]", serialID, csv.getFType(), fSaveName, fName, fPath);

			return null;
		}

		/*
		 * 只檢查其他資料表 NOT NULL 欄位。
		 * f_type 已經在上面處理完成， 因此這裡不再呼叫 validateRequiredField("f_type", fType)。
		 */
		validateRequiredField("serial_id", serialID);
		validateRequiredField("node_type", nodeType);
		validateRequiredField("f_path", fPath);
		validateRequiredField("f_name", fName);

		// 建立 Entity
		S3File entity = new S3File();
		entity.setSerialID(serialID);
		entity.setNodeType(nodeType);
		entity.setFType(fType);
		entity.setFPath(fPath);
		entity.setFName(fName);
		entity.setFSaveName(fSaveName);
		entity.setFSize(CommonUtil.parseLong(csv.getFSize()));
		entity.setRefID(CommonUtil.toNull(csv.getRefID()));
		entity.setAccessStart(CommonUtil.parseDateTime(csv.getAccessStart()));
		entity.setStartMile(CommonUtil.parseBigDecimal(csv.getStartMile()));
		entity.setAccessEnd(CommonUtil.parseDateTime(csv.getAccessEnd()));
		entity.setEndMile(CommonUtil.parseBigDecimal(csv.getEndMile()));
		entity.setAccessType(CommonUtil.toNull(csv.getAccessType()));
		entity.setAccessUser(CommonUtil.toNull(csv.getAccessUser()));
		entity.setOverWriteID(CommonUtil.toNull(csv.getOverWriteID()));
		entity.setReleaseUser(CommonUtil.toNull(csv.getReleaseUser()));
		entity.setReleaseUserName(CommonUtil.toNull(csv.getReleaseUserName()));
		entity.setReleaseTime(CommonUtil.parseDateTime(csv.getReleaseTime()));
		entity.setHisRefID(CommonUtil.toNull(csv.getHisRefID()));
		entity.setVirtualPath(CommonUtil.toNull(csv.getVirtualPath()));
		entity.setSource(CommonUtil.toNull(csv.getSource()));

//		log.info("Import file record: serial_id={}, f_type={}, f_save_name={}", entity.getSerialID(), entity.getFType(), entity.getFSaveName());

		return entity;
	}

	/**
	 * 取得檔案類型。
	 * 判斷順序：
	 * 1. 優先使用原始 f_type 2. f_type 為空時，從 f_save_name 取得副檔名
	 * 範例： FAZ -> faz .FAZ -> faz report.FAZ -> faz report.v1.final.FAZ -> faz
	 */
	private String getFileType(String originalFileType, String saveName) {

		// 優先使用原始 f_type
		String fileType = normalizeFileType(originalFileType);

		if (fileType != null) {
			return fileType;
		}

		// f_type 為空時， 從 f_save_name 取得副檔名
		String extension = extractFileExtension(saveName);
		if (extension != null) {
//			log.debug("f_type is empty, use extension from f_save_name: {}", extension);
		}

		return extension;
	}

	/**
	 * 從檔名取得副檔名。
	 * 使用最後一個 "."， 因此可以處理多個 "." 的檔名。
	 * 範例：report.v1.final.FAZ -> faz test.data.ppt -> ppt
	 */
	private String extractFileExtension(String fileName) {
		String normalizedFileName = CommonUtil.toNull(fileName);
		if (normalizedFileName == null) {
			return null;
		}

		/*
		 * 如果內容包含路徑， 只取最後一段檔案名稱。
		 * Windows： \\server\folder\report.final.FAZ
		 * Linux： /server/folder/report.final.FAZ
		 */
		int lastWindowsSlashIndex = normalizedFileName.lastIndexOf('\\');
		int lastLinuxSlashIndex = normalizedFileName.lastIndexOf('/');
		int lastSlashIndex = Math.max(lastWindowsSlashIndex, lastLinuxSlashIndex);
		String pureFileName = normalizedFileName.substring(lastSlashIndex + 1);

		// 取得最後一個 "." 的位置
		int lastDotIndex = pureFileName.lastIndexOf('.');

		// 以下狀況視為沒有副檔名： README file. .gitignore
		if (lastDotIndex <= 0) {
			return null;
		}
		if (lastDotIndex >= pureFileName.length() - 1) {
			return null;
		}
		String extension = pureFileName.substring(lastDotIndex + 1);
		return normalizeFileType(extension);
	}

	// 統一副檔名格式。.FAZ -> faz FAZ -> faz Faz -> faz
	private String normalizeFileType(String fileType) {
		String normalizedType = CommonUtil.toNull(fileType);
		if (normalizedType == null) {
			return null;
		}
		// 移除前面的 "."。
		while (normalizedType.startsWith(".")) {
			normalizedType = normalizedType.substring(1);
		}
		normalizedType = normalizedType.trim();
		if (normalizedType.isEmpty()) {
			return null;
		}

		return normalizedType.toLowerCase(Locale.ROOT);
	}

	// 檢查資料表 NOT NULL 欄位。
	private void validateRequiredField(String fieldName, String value) {
		if (value == null) {
			throw new IllegalArgumentException("必要欄位不可為空：" + fieldName);
		}
	}

}
