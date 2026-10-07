package com.anthonyfu.web.service;

import java.sql.Timestamp;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.anthonyfu.web.dto.S3FileDto;
import com.anthonyfu.web.model.S3FileEntity;
import com.anthonyfu.web.model.S3FileUploadHistoryEntity;
import com.anthonyfu.web.util.FileUtil;

@Service
public class BuildDtoService {
	
	public S3FileEntity buildS3File(String bucketName, String s3Key, S3FileDto dto, Long fileSize) {
		S3FileEntity entity = new S3FileEntity();
		entity.setObjectKey(s3Key);
		entity.setBucketName(bucketName);
		entity.setFileName(dto.getFSaveName().toUpperCase(Locale.ROOT));
		entity.setFileSize(fileSize);
		entity.setPcbNo(FileUtil.parsePcbNo(dto.getFSaveName()));
		entity.setPcbVersion(FileUtil.parsePcbVersion(dto.getFSaveName()));
		entity.setFileVersion("1"); // 舊系統搬移資料，目前指定為第 1 版
		entity.setCreateBy(dto.getAccessUser());
		entity.setUpdateBy(dto.getAccessUser());
		if (dto.getAccessEnd() != null) {
			Timestamp accessEndTimestamp = Timestamp.valueOf(dto.getAccessEnd());
			entity.setCreateDt(accessEndTimestamp);
			entity.setUpdateDt(accessEndTimestamp);
		}
		return entity;
	}

	public S3FileUploadHistoryEntity buildS3FileUploadHistory(String bucketName, String s3Key, S3FileDto dto, Long fileSize) {

	    S3FileUploadHistoryEntity history = new S3FileUploadHistoryEntity();
	    history.setObjectKey(s3Key);
	    history.setBucketName(bucketName);
	    history.setFileName(dto.getFSaveName().toUpperCase(Locale.ROOT));
	    history.setFileSize(fileSize);
	    history.setPcbNo(FileUtil.parsePcbNo(dto.getFSaveName()));
	    history.setPcbVersion(FileUtil.parsePcbVersion(dto.getFSaveName()));
	    history.setFileVersion("1"); // 舊系統搬移資料，目前指定為第 1 版
	    history.setUpdateBy(dto.getAccessUser());
	    if (dto.getAccessEnd() != null) {
	        Timestamp accessEndTimestamp = Timestamp.valueOf(dto.getAccessEnd());
	        history.setUpdateDt(accessEndTimestamp);
	    } else {
	        history.setUpdateDt(new Timestamp(System.currentTimeMillis()));
	    }
	    return history;
	}
	
}
