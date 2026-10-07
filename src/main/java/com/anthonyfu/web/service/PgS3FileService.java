package com.anthonyfu.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthonyfu.web.dao.PgS3FileDao;
import com.anthonyfu.web.model.S3FileEntity;
import com.anthonyfu.web.model.S3FileUploadHistoryEntity;

@Service
public class PgS3FileService {

	@Autowired
	private PgS3FileDao pgS3FileDao;
	
	// 確認 DB 兩張表的資料是否都完整
    public boolean isFileDataComplete(String objectKey) {
        return pgS3FileDao.isFileDataComplete(objectKey);
    }
    
	// 儲存目前檔案資料
	@Transactional
	public void saveRecord(S3FileEntity entity) {
		pgS3FileDao.saveS3File(entity);
	}
	
	// 新增上傳歷史紀錄
	@Transactional
	public void saveUploadHistory(S3FileUploadHistoryEntity entity) {
		pgS3FileDao.saveUploadHistory(entity);
	}
	
	// 同時儲存目前檔案資料與上傳歷史紀錄
	@Transactional
	public void saveFileAndUploadHistory(S3FileEntity fileEntity, S3FileUploadHistoryEntity historyEntity) {
		pgS3FileDao.saveS3File(fileEntity);
		pgS3FileDao.saveUploadHistory(historyEntity);
	}
	
}
