package com.anthonyfu.web.model;

import lombok.Data;

@Data
public class S3FileDto {

    private String serialID;
    private String nodeType;
    private String fType;
    private String fPath;
    private String fName;
    private String fSaveName;
    private String fSize;
    private String refID;
    private String accessStart;
    private String startMile;
    private String accessEnd;
    private String endMile;
    private String accessType;
    private String accessUser;
    private String overWriteID;
    private String releaseUser;
    private String releaseUserName;
    private String releaseTime;
    private String hisRefID;
    private String virtualPath;
    private String source;
    
}
