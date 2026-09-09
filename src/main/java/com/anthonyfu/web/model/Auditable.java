package com.anthonyfu.web.model;

import java.sql.Timestamp;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
//@EntityListeners(AuditingEntityListener.class)
public class Auditable<U> {
    @CreatedBy
    @Column(name = "create_by")
    private U createBy;

    @CreatedDate
    @Column(name = "create_dt")
    private Timestamp createDt;

    @LastModifiedBy
    @Column(name = "update_by")
    private U updateBy;

    @LastModifiedDate
    @Column(name = "update_dt")
    private Timestamp updateDt;

}
