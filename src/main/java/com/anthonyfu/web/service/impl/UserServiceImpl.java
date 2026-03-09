package com.anthonyfu.web.service.impl;

import org.springframework.stereotype.Service;

import com.anthonyfu.web.aspect.LogExecutionTime;
import com.anthonyfu.web.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Override
    public String getUserInfo(String userId) {
        System.out.println("Getting user info for: " + userId);
        return "User info for " + userId;
    }

	@Override
    public void saveUser(String userData) {
        System.out.println("Saving user data: " + userData);
    }

	@Override
    @LogExecutionTime
    public String getUserDetails(String productId) {
        return "User details for " + productId;
    }

}
