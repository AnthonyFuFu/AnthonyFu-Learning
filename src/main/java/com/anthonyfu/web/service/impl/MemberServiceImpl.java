package com.anthonyfu.web.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anthonyfu.web.dao.MemberDao;
import com.anthonyfu.web.model.Member;
import com.anthonyfu.web.service.MemberService;

@Service
public class MemberServiceImpl implements MemberService {
	@Autowired
	 private MemberDao memberDao;
	 @Override
	 public List<Map<String, Object>> list(Member member) {
	  return memberDao.list(member);
	 }
}
