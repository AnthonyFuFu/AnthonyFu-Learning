package com.anthonyfu.web.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3FileDto {
    private String serialID;
    private String fPath;
    private String fSaveName;
    private Long fSize;
    private LocalDateTime  accessStart;
    private LocalDateTime  accessEnd;
    private String accessUser;
    private String virtualPath;
}
