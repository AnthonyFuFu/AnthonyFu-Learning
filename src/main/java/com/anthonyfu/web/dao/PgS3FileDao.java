package com.anthonyfu.web.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.anthonyfu.web.model.S3FileEntity;
import com.anthonyfu.web.model.S3FileUploadHistoryEntity;

@Repository
public class PgS3FileDao {

    @Autowired
    @Qualifier("pgJdbcTemplate")
    private JdbcTemplate pgJdbcTemplate;
    
    /**
     * s3_file：
     * object_key 存在則更新，不存在則新增
     */
    public void saveS3File(S3FileEntity entity) {

        String checkSql =
                "SELECT COUNT(*) " +
                "FROM pcbaprddata.s3_file " +
                "WHERE object_key = ?";

		Integer count = pgJdbcTemplate.queryForObject(checkSql, Integer.class, entity.getObjectKey());

        if (count != null && count > 0) {
            updateS3File(entity);
        } else {
            insertS3File(entity);
        }
    }

    private void updateS3File(S3FileEntity entity) {

        String updateSql =
                "UPDATE pcbaprddata.s3_file SET " +
                "bucket_name = ?, " +
                "file_name = ?, " +
                "file_size = ?, " +
                "pcb_no = ?, " +
                "pcb_version = ?, " +
                "file_version = ?, " +
                "create_by = ?, " +
                "create_dt = ?, " +
                "update_by = ?, " +
                "update_dt = ? " +
                "WHERE object_key = ?";

        pgJdbcTemplate.update(
                updateSql,
                entity.getBucketName(),
                entity.getFileName(),
                entity.getFileSize(),
                entity.getPcbNo(),
                entity.getPcbVersion(),
                entity.getFileVersion(),
                entity.getCreateBy(),
                entity.getCreateDt(),
                entity.getUpdateBy(),
                entity.getUpdateDt(),
                entity.getObjectKey()
        );

		System.out.println("🔄 s3_file 已更新: " + entity.getObjectKey());
    }

    private void insertS3File(S3FileEntity entity) {

        String insertSql =
                "INSERT INTO pcbaprddata.s3_file " +
                "(seq_no, object_key, bucket_name, file_name, file_size, " +
                " pcb_no, pcb_version, file_version, " +
                " create_by, create_dt, update_by, update_dt) " +
                "VALUES (" +
                " nextval('pcbaprddata.s3_file_generate_id_seq'), " +
                " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                ")";

        pgJdbcTemplate.update(
                insertSql,
                entity.getObjectKey(),
                entity.getBucketName(),
                entity.getFileName(),
                entity.getFileSize(),
                entity.getPcbNo(),
                entity.getPcbVersion(),
                entity.getFileVersion(),
                entity.getCreateBy(),
                entity.getCreateDt(),
                entity.getUpdateBy(),
                entity.getUpdateDt()
        );

		System.out.println("✅ s3_file 新增成功: " + entity.getObjectKey());
    }

    /**
     * 新增 s3_file_upload_history 紀錄
     *
     * 歷史表通常每次上傳都新增一筆，不做 UPDATE。
     */
	public void insertUploadHistory(S3FileUploadHistoryEntity entity) {

        String insertSql =
                "INSERT INTO pcbaprddata.s3_file_upload_history " +
                "(seq_no, object_key, bucket_name, file_name, file_size, " +
                " pcb_no, pcb_version, file_version, update_by, update_dt) " +
                "VALUES (" +
                " nextval('pcbaprddata.s3_file_upload_history_generate_id_seq'), " +
                " ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                ")";

        pgJdbcTemplate.update(
                insertSql,
                entity.getObjectKey(),
                entity.getBucketName(),
                entity.getFileName(),
                entity.getFileSize(),
                entity.getPcbNo(),
                entity.getPcbVersion(),
                entity.getFileVersion(),
                entity.getUpdateBy(),
                entity.getUpdateDt()
        );

		System.out.println("✅ s3_file_upload_history 新增成功: " + entity.getObjectKey());
    }
}
