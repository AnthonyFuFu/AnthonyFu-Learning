package com.anthonyfu.web.service;

import java.util.List;
import java.util.Map;

import com.anthonyfu.web.model.Member;

public interface MemberService {
	 public List<Map<String, Object>> list(Member member);
}
