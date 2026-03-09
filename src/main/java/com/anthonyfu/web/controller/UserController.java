package com.anthonyfu.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anthonyfu.web.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 獲取用戶信息
     * GET /api/users/{userId}
     * http://localhost:8080/api/users/1
     */
    @GetMapping("/{userId}")
    public ResponseEntity<String> getUserInfo(@PathVariable String userId) {
        String userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 創建/保存用戶
     * POST /api/users
     * curl -X POST http://localhost:8080/api/users \   -H "Content-Type: application/json" \   -d '{"name":"Anthony Fu","email":"anthony@example.com"}'
     */
    @PostMapping
    public ResponseEntity<String> saveUser(@RequestBody String userData) {
        userService.saveUser(userData);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body("User created successfully");
    }

    /**
     * 獲取產品詳情 (應用了 LogExecutionTime 切面)
     * GET /api/users/user/{userId}
     * http://localhost:8080/api/users/user/1
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<String> getUserDetails(@PathVariable String userId) {
        String productDetails = userService.getUserDetails(userId);
        return ResponseEntity.ok(productDetails);
    }

    /**
     * 錯誤處理示例
     * 當發生異常時，AOP 的 @AfterThrowing 將被觸發
     * http://localhost:8080/api/users/error-demo
     */
    @GetMapping("/error-demo")
    public ResponseEntity<String> errorDemo() {
        throw new RuntimeException("This is a demonstration error for AOP");
    }
}