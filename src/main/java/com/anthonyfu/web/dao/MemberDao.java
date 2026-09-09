package com.anthonyfu.web.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.anthonyfu.web.model.Member;

@Repository
public class MemberDao {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<Map<String, Object>> list(Member member) {

		List<Object> args = new ArrayList<>();
		String sql = " SELECT * FROM anthonyfu_learning.member ";

		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, args.toArray());

		if (list != null && list.size() > 0) {
			return list;
		} else {
			return null;
		}
		
	}

}
