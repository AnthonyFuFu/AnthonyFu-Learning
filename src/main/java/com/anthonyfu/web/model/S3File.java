package com.anthonyfu.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "s3_file")
//@Table(name = "origin_s3_file")
@NoArgsConstructor
@AllArgsConstructor
public class S3File {

    @Id
    @Column(name = "serial_id", length = 64, nullable = false)
    private String serialID;

    @Column(name = "node_type", length = 5, nullable = false)
    private String nodeType;

    @Column(name = "f_type", length = 20, nullable = false)
    private String fType;

    @Column(name = "f_path", length = 500, nullable = false)
    private String fPath;

    @Column(name = "f_name", length = 255, nullable = false)
    private String fName;

    @Column(name = "f_save_name", length = 255)
    private String fSaveName;

    @Column(name = "f_size")
    private Long fSize;

    @Column(name = "ref_id", length = 64)
    private String refID;

    @Column(name = "access_start")
    private LocalDateTime accessStart;

    @Column(name = "start_mile", precision = 18, scale = 3)
    private BigDecimal startMile;

    @Column(name = "access_end")
    private LocalDateTime accessEnd;

    @Column(name = "end_mile", precision = 18, scale = 3)
    private BigDecimal endMile;

    @Column(name = "access_type", length = 20)
    private String accessType;

    @Column(name = "access_user", length = 50)
    private String accessUser;

    @Column(name = "over_write_id", length = 64)
    private String overWriteID;

    @Column(name = "release_user", length = 50)
    private String releaseUser;

    @Column(name = "release_user_name", length = 100)
    private String releaseUserName;

    @Column(name = "release_time")
    private LocalDateTime releaseTime;

    @Column(name = "his_ref_id", length = 64)
    private String hisRefID;

    @Column(name = "virtual_path", length = 500)
    private String virtualPath;

    @Column(name = "source", length = 1)
    private String source;
}
