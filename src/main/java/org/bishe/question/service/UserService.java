package org.bishe.question.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.LoginRequest;
import org.bishe.question.dto.LoginResponse;
import org.bishe.question.entity.User;
import org.bishe.question.mapper.UserMapper;
import org.bishe.question.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest loginRequest) {
        System.out.println("=== 登录请求开始 ===");
        System.out.println("请求用户名: " + loginRequest.getUsername());
        System.out.println("请求密码: " + loginRequest.getPassword());

        if (!StringUtils.hasText(loginRequest.getUsername()) ||
            !StringUtils.hasText(loginRequest.getPassword())) {
            System.out.println("参数验证失败: 用户名或密码为空");
            throw new RuntimeException("用户名和密码不能为空");
        }

        System.out.println("正在查找用户: " + loginRequest.getUsername());
        User user = userMapper.findByUsernameOrEmail(
            loginRequest.getUsername(), loginRequest.getUsername());

        if (user == null) {
            System.out.println("用户查找失败: 用户不存在");
            throw new RuntimeException("用户不存在");
        }

        System.out.println("找到用户: " + user.getUsername());
        System.out.println("数据库中的密码: " + user.getPassword());
        System.out.println("用户状态: " + user.getStatus());

        if (user.getStatus() == null || user.getStatus() != 1) {
            System.out.println("用户状态检查失败: 用户已被禁用");
            throw new RuntimeException("用户已被禁用");
        }

        System.out.println("密码比较: 输入[" + loginRequest.getPassword() + "] vs 数据库[" + user.getPassword() + "]");
        System.out.println("密码是否相等: " + loginRequest.getPassword().equals(user.getPassword()));
        if (!loginRequest.getPassword().equals(user.getPassword())) {
            System.out.println("密码验证失败: 密码不匹配");
            throw new RuntimeException("密码错误");
        }

        System.out.println("密码验证成功");

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.update(user);

        LoginResponse response = new LoginResponse(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getEmail()
        );
        response.setAvatar(user.getAvatar());

        String token = generateToken(user.getId());
        response.setToken(token);

        return response;
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.findById(id));
    }

    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private String generateToken(Long userId) {
        return "token_" + userId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    // ==================== 管理员功能 ====================

    public PageInfo<User> findAllUsers(int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<User> list = userMapper.findAll();
        return new PageInfo<>(list);
    }

    public PageInfo<User> searchUsers(String keyword, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<User> list = userMapper.findByKeyword(keyword);
        return new PageInfo<>(list);
    }

    public PageInfo<User> findUsersByStatus(Integer status, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<User> list = userMapper.findByStatus(status);
        return new PageInfo<>(list);
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userMapper.count();
        stats.put("totalUsers", totalUsers);

        long activeUsers = userMapper.countByStatus(1);
        long inactiveUsers = userMapper.countByStatus(0);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);

        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<User> recentActiveUsers = userMapper.findRecentlyActiveUsers(lastWeek);
        stats.put("recentActiveUsers", (long) recentActiveUsers.size());

        List<User> neverLoggedInUsers = userMapper.findNeverLoggedInUsers();
        stats.put("neverLoggedInUsers", (long) neverLoggedInUsers.size());

        return stats;
    }

    public User updateUserStatus(Long userId, Integer status) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.update(user);
        return user;
    }

    public PageInfo<User> getRecentlyActiveUsers(int days, int page, int size) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        PageHelper.startPage(page + 1, size, "last_login_time desc");
        List<User> list = userMapper.findRecentlyActiveUsers(since);
        return new PageInfo<>(list);
    }

    public PageInfo<User> getNeverLoggedInUsers(int page, int size) {
        PageHelper.startPage(page + 1, size, "created_time desc");
        List<User> list = userMapper.findNeverLoggedInUsers();
        return new PageInfo<>(list);
    }

    public PageInfo<User> findUsersByCreatedTime(LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        PageHelper.startPage(page + 1, size, "created_time desc");
        List<User> list = userMapper.findByCreatedTimeBetween(startTime, endTime);
        return new PageInfo<>(list);
    }
}