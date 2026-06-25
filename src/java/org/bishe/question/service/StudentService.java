package org.bishe.question.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.StudentLoginRequest;
import org.bishe.question.dto.StudentLoginResponse;
import org.bishe.question.entity.Student;
import org.bishe.question.mapper.StudentMapper;
import org.bishe.question.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 学生服务类
 */
@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public StudentLoginResponse login(StudentLoginRequest loginRequest) {
        System.out.println("=== 学生登录请求开始 ===");
        System.out.println("请求学号: " + loginRequest.getStudentNumber());
        System.out.println("请求密码: " + loginRequest.getPassword());

        if (!StringUtils.hasText(loginRequest.getStudentNumber()) ||
            !StringUtils.hasText(loginRequest.getPassword())) {
            System.out.println("参数验证失败: 学号或密码为空");
            throw new RuntimeException("学号和密码不能为空");
        }

        System.out.println("正在查找学生: " + loginRequest.getStudentNumber());
        Student student = studentMapper.findByStudentNumberOrEmail(
            loginRequest.getStudentNumber(), loginRequest.getStudentNumber());

        if (student == null) {
            System.out.println("学生查找失败: 学生不存在");
            throw new RuntimeException("学生不存在");
        }

        System.out.println("找到学生: " + student.getStudentNumber());
        System.out.println("数据库中的密码: " + student.getPassword());
        System.out.println("学生状态: " + student.getStatus());

        if (student.getStatus() == null || student.getStatus() != 1) {
            System.out.println("学生状态检查失败: 学生已被禁用");
            throw new RuntimeException("学生账号已被禁用");
        }

        System.out.println("密码验证: 输入[" + loginRequest.getPassword() + "] vs 数据库[" + student.getPassword() + "]");

        boolean passwordMatches = false;
        boolean isBcryptHash = student.getPassword() != null && student.getPassword().startsWith("$2a$");

        if (isBcryptHash) {
            // BCrypt 哈希密码：使用 BCrypt 验证
            passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), student.getPassword());
            System.out.println("BCrypt密码验证结果: " + passwordMatches);
        } else {
            // 明文密码（兼容旧数据）：直接比对
            passwordMatches = loginRequest.getPassword().equals(student.getPassword());
            System.out.println("明文密码比对结果: " + passwordMatches);

            // 明文匹配成功后，自动升级为 BCrypt 哈希
            if (passwordMatches) {
                System.out.println("明文密码匹配成功，自动升级为BCrypt哈希...");
                student.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
                System.out.println("密码已升级为: " + student.getPassword());
            }
        }

        System.out.println("密码是否匹配: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println("密码验证失败: 密码不匹配");
            throw new RuntimeException("密码错误");
        }

        System.out.println("密码验证成功");

        student.setUpdatedAt(LocalDateTime.now());
        studentMapper.update(student);

        StudentLoginResponse response = new StudentLoginResponse(
            student.getId(),
            student.getStudentNumber(),
            student.getName(),
            generateToken(student.getId())
        );
        response.setEmail(student.getEmail());
        response.setPhone(student.getPhone());
        response.setClassName(student.getClassName());
        response.setMajor(student.getMajor());
        response.setGrade(student.getGrade());

        System.out.println("学生登录成功: " + response.getStudentNumber());
        return response;
    }

    public Optional<Student> findByStudentNumber(String studentNumber) {
        return Optional.ofNullable(studentMapper.findByStudentNumber(studentNumber));
    }

    public Optional<Student> findById(Long id) {
        return Optional.ofNullable(studentMapper.findById(id));
    }

    public boolean existsByStudentNumber(String studentNumber) {
        return studentMapper.existsByStudentNumber(studentNumber);
    }

    public boolean existsByEmail(String email) {
        return studentMapper.existsByEmail(email);
    }

    public List<Student> findByClassName(String className) {
        return studentMapper.findByClassName(className);
    }

    public List<Student> findByMajor(String major) {
        return studentMapper.findByMajor(major);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private String generateToken(Long studentId) {
        return "student_token_" + studentId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    // ==================== 管理员功能 ====================

    public PageInfo<Student> findAllStudents(int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Student> list = studentMapper.findAll();
        return new PageInfo<>(list);
    }

    public PageInfo<Student> searchStudents(String keyword, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Student> list = studentMapper.findByKeyword(keyword);
        return new PageInfo<>(list);
    }

    public PageInfo<Student> findStudentsByStatus(Integer status, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Student> list = studentMapper.findByStatus(status);
        return new PageInfo<>(list);
    }

    public PageInfo<Student> findStudentsByClassName(String className, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Student> list = studentMapper.findByClassName(className);
        return new PageInfo<>(list);
    }

    public PageInfo<Student> findStudentsByMajor(String major, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Student> list = studentMapper.findByMajor(major);
        return new PageInfo<>(list);
    }

    public List<Student> findAllStudents() {
        return studentMapper.findAll();
    }

    public Student updateStudentStatus(Long studentId, Integer status) {
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        student.setStatus(status);
        student.setUpdatedAt(LocalDateTime.now());
        studentMapper.update(student);
        return student;
    }

    public void deleteStudent(Long studentId) {
        if (!studentMapper.existsById(studentId)) {
            throw new RuntimeException("学生不存在");
        }
        studentMapper.deleteById(studentId);
    }

    public void deleteStudents(List<Long> studentIds) {
        studentMapper.deleteAllById(studentIds);
    }

    public StudentStatistics getStudentStatistics() {
        long totalStudents = studentMapper.count();
        long activeStudents = studentMapper.countByStatus(1);
        long inactiveStudents = studentMapper.countByStatus(0);
        return new StudentStatistics(totalStudents, activeStudents, inactiveStudents);
    }

    public List<String> findDistinctClassNames() {
        return studentMapper.findDistinctClassNames();
    }

    public List<String> findDistinctMajors() {
        return studentMapper.findDistinctMajors();
    }

    public List<String> findDistinctGrades() {
        return studentMapper.findDistinctGrades();
    }

    public static class StudentStatistics {
        private long totalStudents;
        private long activeStudents;
        private long inactiveStudents;

        public StudentStatistics(long totalStudents, long activeStudents, long inactiveStudents) {
            this.totalStudents = totalStudents;
            this.activeStudents = activeStudents;
            this.inactiveStudents = inactiveStudents;
        }

        public long getTotalStudents() { return totalStudents; }
        public long getActiveStudents() { return activeStudents; }
        public long getInactiveStudents() { return inactiveStudents; }
    }
}