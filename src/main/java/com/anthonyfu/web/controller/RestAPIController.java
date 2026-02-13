package com.anthonyfu.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anthonyfu.web.model.Member;
import com.anthonyfu.web.service.MemberService;

@RestController
public class RestAPIController {

	@Autowired
	private MemberService memberService;

	// 前後端分離 寫法，測試網址 http://localhost:8080/restAPI/member/list
	@GetMapping("/restAPI/member/list")
	public List<Map<String, Object>> list() {
	    return memberService.list(new Member());
	}
}
