package com.anthonyfu.web.service;

public interface UserService {

	public String getUserInfo(String userId);

	public void saveUser(String userData);
	
	public String getUserDetails(String userId);

}
