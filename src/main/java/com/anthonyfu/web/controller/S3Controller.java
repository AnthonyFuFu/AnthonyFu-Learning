package com.anthonyfu.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anthonyfu.web.service.S3FileService;

@RestController
@RequestMapping(value = "/s3")
public class S3Controller {

	@Autowired
	public S3FileService s3FileService;
	
    @GetMapping({"/addS3File", "/list"})
    public String addS3File() {
        s3FileService.addS3File();
        return "執行完成，請查看 Console Log！"; // 加上回傳字串，瀏覽器才不會呈現空白頁
    }
}
