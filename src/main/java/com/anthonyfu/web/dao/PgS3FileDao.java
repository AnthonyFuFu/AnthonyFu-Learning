package com.anthonyfu.web.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.anthonyfu.web.model.S3FileEntity;
import com.anthonyfu.web.model.S3FileUploadHistoryEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PgS3FileDao {

    @Autowired
    @Qualifier("pgJdbcTemplate")
    private JdbcTemplate pgJdbcTemplate;
    
    // s3_file：object_key 存在則更新，不存在則新增
    public void saveS3File(S3FileEntity entity) {
    	
        String checkSql = "SELECT COUNT(*) FROM pcbaprddata.s3_file WHERE object_key = ?";
        
		Integer count = pgJdbcTemplate.queryForObject(checkSql, Integer.class, entity.getObjectKey());
        if (count != null && count > 0) {
            updateS3File(entity);
        } else {
            insertS3File(entity);
        }
    }

    // s3_file_upload_history：object_key 存在則更新，不存在則新增
	public void saveUploadHistory(S3FileUploadHistoryEntity entity) {
		
        String checkSql = "SELECT COUNT(*) FROM pcbaprddata.s3_file_upload_history WHERE object_key = ?";
        
		Integer count = pgJdbcTemplate.queryForObject(checkSql, Integer.class, entity.getObjectKey());
        if (count != null && count > 0) {
            updateUploadHistory(entity);
        } else {
            insertUploadHistory(entity);
        }
    }
	
	// 確認 s3_file 與 s3_file_upload_history 是否都有 object_key 且每個欄位都有值
    public boolean isFileDataComplete(String objectKey) {
        return isS3FileComplete(objectKey) && isUploadHistoryComplete(objectKey);
    }
    
    // 更新既有的 s3file 紀錄
    private void updateS3File(S3FileEntity entity) {

        String sql = """
                UPDATE pcbaprddata.s3_file
                SET bucket_name = ?, file_name = ?, file_size = ?, pcb_no = ?, pcb_version = ?, file_version = ?, create_by = ?, create_dt = ?, update_by = ?, update_dt = ? 
                WHERE object_key = ?
                """;

		pgJdbcTemplate.update(sql, entity.getBucketName(), entity.getFileName(), entity.getFileSize(),
				entity.getPcbNo(), entity.getPcbVersion(), entity.getFileVersion(), entity.getCreateBy(),
				entity.getCreateDt(), entity.getUpdateBy(), entity.getUpdateDt(), entity.getObjectKey());

		log.info("🔄 s3_file 已更新: " + entity.getObjectKey());
    }

    // 新增 s3file 紀錄
    private void insertS3File(S3FileEntity entity) {

        String sql = """
                INSERT INTO pcbaprddata.s3_file 
                (seq_no, object_key, bucket_name, file_name, file_size, pcb_no, pcb_version, file_version, create_by, create_dt, update_by, update_dt) 
                VALUES (nextval('pcbaprddata.s3_file_generate_id_seq'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

		pgJdbcTemplate.update(sql, entity.getObjectKey(), entity.getBucketName(), entity.getFileName(),
				entity.getFileSize(), entity.getPcbNo(), entity.getPcbVersion(), entity.getFileVersion(),
				entity.getCreateBy(), entity.getCreateDt(), entity.getUpdateBy(), entity.getUpdateDt());

		log.info("✅ s3_file 新增成功: " + entity.getObjectKey());
    }
    
    // 更新既有的 History 紀錄
	private void updateUploadHistory(S3FileUploadHistoryEntity entity) {
		
        String sql = """
                UPDATE pcbaprddata.s3_file_upload_history
                SET bucket_name = ?, file_name = ?, file_size = ?, pcb_no = ?, pcb_version = ?, file_version = ?, update_by = ?, update_dt = ? 
                WHERE object_key = ?
                """;

		pgJdbcTemplate.update(sql, entity.getBucketName(), entity.getFileName(), entity.getFileSize(),
				entity.getPcbNo(), entity.getPcbVersion(), entity.getFileVersion(), entity.getUpdateBy(),
				entity.getUpdateDt(), entity.getObjectKey());

		log.info("🔄 s3_file_upload_history 已更新: " + entity.getObjectKey());
    }
    
    // 新增 History 紀錄
	private void insertUploadHistory(S3FileUploadHistoryEntity entity) {

        String sql = """
                INSERT INTO pcbaprddata.s3_file_upload_history
                (seq_no, object_key, bucket_name, file_name, file_size, pcb_no, pcb_version, file_version, update_by, update_dt)
                VALUES (nextval('pcbaprddata.s3_file_upload_history_generate_id_seq'), ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

		pgJdbcTemplate.update(sql, entity.getObjectKey(), entity.getBucketName(), entity.getFileName(),
				entity.getFileSize(), entity.getPcbNo(), entity.getPcbVersion(), entity.getFileVersion(),
				entity.getUpdateBy(), entity.getUpdateDt());

		log.info("✅ s3_file_upload_history 新增成功: " + entity.getObjectKey());
    }
	
    // 檢查 s3_file 是否有完整資料
    public boolean isS3FileComplete(String objectKey) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM pcbaprddata.s3_file
                WHERE object_key = ?
                  AND NULLIF(TRIM(object_key), '') IS NOT NULL
                  AND NULLIF(TRIM(bucket_name), '') IS NOT NULL
                  AND NULLIF(TRIM(file_name), '') IS NOT NULL
                  AND file_size IS NOT NULL
                  AND NULLIF(TRIM(pcb_no), '') IS NOT NULL
                  AND NULLIF(TRIM(pcb_version), '') IS NOT NULL
                  AND NULLIF(TRIM(file_version), '') IS NOT NULL
                  AND NULLIF(TRIM(create_by), '') IS NOT NULL
                  AND create_dt IS NOT NULL
                  AND NULLIF(TRIM(update_by), '') IS NOT NULL
                  AND update_dt IS NOT NULL
            )
            """;

		Boolean result = pgJdbcTemplate.queryForObject(sql, Boolean.class, objectKey);
        return Boolean.TRUE.equals(result);
    }

    // 檢查 s3_file_upload_history 是否有完整資料
    public boolean isUploadHistoryComplete(String objectKey) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM pcbaprddata.s3_file_upload_history
                WHERE object_key = ?
                  AND NULLIF(TRIM(object_key), '') IS NOT NULL
                  AND NULLIF(TRIM(bucket_name), '') IS NOT NULL
                  AND NULLIF(TRIM(file_name), '') IS NOT NULL
                  AND file_size IS NOT NULL
                  AND NULLIF(TRIM(pcb_no), '') IS NOT NULL
                  AND NULLIF(TRIM(pcb_version), '') IS NOT NULL
                  AND NULLIF(TRIM(file_version), '') IS NOT NULL
                  AND NULLIF(TRIM(update_by), '') IS NOT NULL
                  AND update_dt IS NOT NULL
            )
            """;
        
		Boolean result = pgJdbcTemplate.queryForObject(sql, Boolean.class, objectKey);
        return Boolean.TRUE.equals(result);
    }
    
}
