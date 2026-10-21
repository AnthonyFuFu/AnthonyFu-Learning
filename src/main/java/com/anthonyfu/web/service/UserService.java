package com.anthonyfu.web.service;

import org.springframework.stereotype.Service;

import com.anthonyfu.web.aspect.LogExecutionTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    public String getUserInfo(String userId) {
    	log.info("Getting user info for: " + userId);
        return "User info for " + userId;
    }

    public void saveUser(String userData) {
    	log.info("Saving user data: " + userData);
    }

    @LogExecutionTime
    public String getUserDetails(String productId) {
        return "User details for " + productId;
    }
}
