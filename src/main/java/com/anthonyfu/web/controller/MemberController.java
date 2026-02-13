package com.anthonyfu.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.anthonyfu.web.model.Member;
import com.anthonyfu.web.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/member")
@SessionAttributes("memberSession")
public class MemberController {
	
	@Autowired
	private MemberService memberService;

	// 前後端不分離 寫法，測試網址 http://localhost:8080/member/list 
	@RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String list(Model model, HttpServletRequest pRequest, HttpServletResponse pResponse) throws Exception {
		Member memberData = new Member();
		List<Map<String, Object>> memberList = memberService.list(memberData);
		model.addAttribute("memberList", memberList);
		return "member/list";
	}

}
