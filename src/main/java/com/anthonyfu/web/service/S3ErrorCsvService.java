package com.anthonyfu.web.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anthonyfu.web.model.S3FileEntity;
import com.anthonyfu.web.model.S3FileUploadHistoryEntity;

@Service
public class S3ErrorCsvService {

    /**
     * 記錄 s3_file 寫入失敗資料
     */
    private final List<S3FileEntity> s3ErrorList =
            Collections.synchronizedList(new ArrayList<>());

    /**
     * 記錄 s3_file_upload_history 寫入失敗資料
     */
    private final List<S3FileUploadHistoryEntity> s3HistoryErrorList =
            Collections.synchronizedList(new ArrayList<>());

    /**
     * 預設輸出到 src/main/resources
     */
    @Value("${app.error-dir}")
    private String errorDirectory;

    private static final String S3_ERROR_FILE_NAME = "S3Error.csv";
    private static final String S3_HISTORY_ERROR_FILE_NAME = "S3HistoryError.csv";

    /**
     * 記錄 s3_file 失敗資料
     */
    public synchronized void recordS3Error(S3FileEntity entity) {
        if (entity == null) {
            return;
        }

        s3ErrorList.add(entity);
        appendS3ErrorCsv(entity);
    }

    /**
     * 記錄 s3_file_upload_history 失敗資料
     */
    public synchronized void recordS3HistoryError(S3FileUploadHistoryEntity entity) {
        if (entity == null) {
            return;
        }

        s3HistoryErrorList.add(entity);
        appendS3HistoryErrorCsv(entity);
    }

    /**
     * 同時記錄 s3_file 與 s3_file_upload_history 失敗資料
     */
    public synchronized void recordBoth(
            S3FileEntity fileEntity,
            S3FileUploadHistoryEntity historyEntity) {

        recordS3Error(fileEntity);
        recordS3HistoryError(historyEntity);
    }

    public List<S3FileEntity> getS3ErrorList() {
        return new ArrayList<>(s3ErrorList);
    }

    public List<S3FileUploadHistoryEntity> getS3HistoryErrorList() {
        return new ArrayList<>(s3HistoryErrorList);
    }

    private void appendS3ErrorCsv(S3FileEntity entity) {
        String header =
                "object_key,bucket_name,file_name,file_size,"
                + "pcb_no,pcb_version,file_version,"
                + "create_by,create_dt,update_by,update_dt";

        String content =
                csv(entity.getObjectKey()) + ","
                + csv(entity.getBucketName()) + ","
                + csv(entity.getFileName()) + ","
                + csv(entity.getFileSize()) + ","
                + csv(entity.getPcbNo()) + ","
                + csv(entity.getPcbVersion()) + ","
                + csv(entity.getFileVersion()) + ","
                + csv(entity.getCreateBy()) + ","
                + csv(entity.getCreateDt()) + ","
                + csv(entity.getUpdateBy()) + ","
                + csv(entity.getUpdateDt());

        appendCsv(S3_ERROR_FILE_NAME, header, content);
    }

    private void appendS3HistoryErrorCsv(
            S3FileUploadHistoryEntity entity) {

        String header =
                "object_key,bucket_name,file_name,file_size,"
                + "pcb_no,pcb_version,file_version,"
                + "update_by,update_dt";

        String content =
                csv(entity.getObjectKey()) + ","
                + csv(entity.getBucketName()) + ","
                + csv(entity.getFileName()) + ","
                + csv(entity.getFileSize()) + ","
                + csv(entity.getPcbNo()) + ","
                + csv(entity.getPcbVersion()) + ","
                + csv(entity.getFileVersion()) + ","
                + csv(entity.getUpdateBy()) + ","
                + csv(entity.getUpdateDt());

        appendCsv(S3_HISTORY_ERROR_FILE_NAME, header, content);
    }

    private void appendCsv(
            String fileName,
            String header,
            String content) {

        try {
            Path directoryPath = Paths.get(errorDirectory);
            Files.createDirectories(directoryPath);

            Path filePath = directoryPath.resolve(fileName);

            boolean fileExists = Files.exists(filePath);
            boolean fileHasContent =
                    fileExists && Files.size(filePath) > 0;

            if (!fileHasContent) {
                Files.writeString(
                        filePath,
                        header + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            }

            Files.writeString(
                    filePath,
                    content + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            System.out.println("⚠️ 錯誤資料已寫入 CSV：" + filePath);

        } catch (IOException e) {
            /*
             * CSV 寫入失敗不能再拋出，否則會蓋掉原始錯誤資訊。
             */
            System.err.println("❌ CSV 錯誤紀錄寫入失敗：" + fileName);
            e.printStackTrace();
        }
    }

    /**
     * CSV 欄位處理：
     * 逗號、雙引號、換行都需要用雙引號包起來。
     */
    private String csv(Object value) {
        if (value == null) {
            return "";
        }

        String text;

        if (value instanceof Timestamp) {
            text = value.toString();
        } else {
            text = String.valueOf(value);
        }

        text = text.replace("\"", "\"\"");

        if (text.contains(",")
                || text.contains("\"")
                || text.contains("\n")
                || text.contains("\r")) {
            return "\"" + text + "\"";
        }

        return text;
    }
}
