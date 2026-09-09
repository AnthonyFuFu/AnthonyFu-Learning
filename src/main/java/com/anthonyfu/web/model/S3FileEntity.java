package com.anthonyfu.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "s3_file", schema = "pcbaprddata" , catalog = "plmaws")
@Entity
//@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class S3FileEntity extends Auditable<String> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "s3_file_generate_id_seq")
    @SequenceGenerator(name = "s3_file_generate_id_seq", sequenceName = "pcbaprddata.s3_file_generate_id_seq", allocationSize = 1)
    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    // S3上檔案實際的名稱包含folder
    @Column(name = "object_key", length = 255)
    private String objectKey;

    // S3 Bucket 名稱
    @Column(name = "bucket_name", length = 255)
    private String bucketName;

    // 檔案名稱
    @Column(name = "file_name", length = 255)
    private String fileName;

    // 檔案大小
    @Column(name = "file_size")
    private Long fileSize;
    
    // 版號
    @Column(name = "pcb_no", length = 50)
    private String pcbNo;
    
    // 版本
    @Column(name = "pcb_version", length = 50)
    private String pcbVersion;
    
    // 修訂版本
    @Column(name = "file_version", length = 50)
    private String fileVersion;
    
}
