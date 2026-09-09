package com.anthonyfu.web.service;

import org.springframework.stereotype.Service;

import com.anthonyfu.web.aspect.LogExecutionTime;

@Service
public class UserService {

    public String getUserInfo(String userId) {
        System.out.println("Getting user info for: " + userId);
        return "User info for " + userId;
    }

    public void saveUser(String userData) {
        System.out.println("Saving user data: " + userData);
    }

    @LogExecutionTime
    public String getUserDetails(String productId) {
        return "User details for " + productId;
    }
}
