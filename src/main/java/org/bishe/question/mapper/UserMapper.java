package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.bishe.question.entity.User;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {
    User findById(Long id);
    List<User> findAll();
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
    long count();

    User findByUsername(String username);
    User findByEmail(String email);
    User findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByStatus(@Param("status") Integer status);

    // These will be used with PageHelper
    List<User> findByKeyword(@Param("keyword") String keyword);
    long countByStatus(@Param("status") Integer status);
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);
    List<User> findNeverLoggedInUsers();
    List<User> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
