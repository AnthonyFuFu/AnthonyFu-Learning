package com.anthonyfu.web.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anthonyfu.web.dao.MemberDao;
import com.anthonyfu.web.model.Member;

@Service
public class MemberService {
	
	@Autowired
	private MemberDao memberDao;

	public List<Map<String, Object>> list(Member member) {
		return memberDao.list(member);
	}
	
}
