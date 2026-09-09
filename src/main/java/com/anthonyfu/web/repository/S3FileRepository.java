package com.anthonyfu.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.anthonyfu.web.dto.S3FileDto;
import com.anthonyfu.web.model.S3File;

@Repository
public interface S3FileRepository extends JpaRepository<S3File, String> {

    @Query("SELECT new com.anthonyfu.web.dto.S3FileDto(s.serialID, s.fPath, s.fSaveName, s.fSize, s.accessStart, s.accessEnd, s.accessUser, s.virtualPath) FROM S3File s")
    List<S3FileDto> findAllProjectedBy();
}
