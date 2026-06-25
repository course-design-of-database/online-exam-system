package org.bishe.question.controller;

import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.ApiResponse;
import org.bishe.question.entity.User;
import org.bishe.question.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<PageInfo<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created_time") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageInfo<User> users = userService.findAllUsers(page, size, sortBy, sortDir);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ApiResponse<PageInfo<User>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created_time") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageInfo<User> users = userService.searchUsers(keyword, page, size, sortBy, sortDir);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error("搜索用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ApiResponse<PageInfo<User>> getUsersByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created_time") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageInfo<User> users = userService.findUsersByStatus(status, page, size, sortBy, sortDir);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> statistics = userService.getUserStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        try {
            return userService.findById(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("用户不存在"));
        } catch (Exception e) {
            return ApiResponse.error("获取用户详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ApiResponse<User> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            User updatedUser = userService.updateUserStatus(id, status);
            return ApiResponse.success(updatedUser);
        } catch (Exception e) {
            return ApiResponse.error("更新用户状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/recent-active")
    public ApiResponse<PageInfo<User>> getRecentlyActiveUsers(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageInfo<User> users = userService.getRecentlyActiveUsers(days, page, size);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error("获取最近活跃用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/never-logged-in")
    public ApiResponse<PageInfo<User>> getNeverLoggedInUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageInfo<User> users = userService.getNeverLoggedInUsers(page, size);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error("获取从未登录用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/created-between")
    public ApiResponse<PageInfo<User>> getUsersByCreatedTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageInfo<User> users = userService.findUsersByCreatedTime(startTime, endTime, page, size);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }
}