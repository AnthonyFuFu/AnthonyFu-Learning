package com.anthonyfu.web.dao;

import java.util.List;
import java.util.Map;

import com.anthonyfu.web.model.Member;

public interface MemberDao {
	 public List<Map<String, Object>> list(Member member);

}
