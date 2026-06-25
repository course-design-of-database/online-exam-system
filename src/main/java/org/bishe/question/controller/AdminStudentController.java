package org.bishe.question.controller;

import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.ApiResponse;
import org.bishe.question.entity.Student;
import org.bishe.question.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/students")
@CrossOrigin(origins = "*")
public class AdminStudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public ApiResponse<PageInfo<Student>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageInfo<Student> students = studentService.findAllStudents(page, size, sortBy, sortDir);
            return ApiResponse.success("获取学生列表成功", students);
        } catch (Exception e) {
            return ApiResponse.error("获取学生列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ApiResponse<PageInfo<Student>> searchStudents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageInfo<Student> students = studentService.searchStudents(keyword, page, size, sortBy, sortDir);
            return ApiResponse.success("搜索学生成功", students);
        } catch (Exception e) {
            return ApiResponse.error("搜索学生失败: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ApiResponse<PageInfo<Student>> getStudentsByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageInfo<Student> students = studentService.findStudentsByStatus(status, page, size, sortBy, sortDir);
            return ApiResponse.success("获取学生列表成功", students);
        } catch (Exception e) {
            return ApiResponse.error("获取学生列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/class/{className}")
    public ApiResponse<PageInfo<Student>> getStudentsByClass(
            @PathVariable String className,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageInfo<Student> students = studentService.findStudentsByClassName(className, page, size, sortBy, sortDir);
            return ApiResponse.success("获取班级学生列表成功", students);
        } catch (Exception e) {
            return ApiResponse.error("获取班级学生列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/major/{major}")
    public ApiResponse<PageInfo<Student>> getStudentsByMajor(
            @PathVariable String major,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageInfo<Student> students = studentService.findStudentsByMajor(major, page, size, sortBy, sortDir);
            return ApiResponse.success("获取专业学生列表成功", students);
        } catch (Exception e) {
            return ApiResponse.error("获取专业学生列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Student> getStudentById(@PathVariable Long id) {
        try {
            Student student = studentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("学生不存在"));
            return ApiResponse.success("获取学生详情成功", student);
        } catch (Exception e) {
            return ApiResponse.error("获取学生详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Student> updateStudentStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer status = request.get("status");
            if (status == null) return ApiResponse.error("状态参数不能为空");
            Student student = studentService.updateStudentStatus(id, status);
            return ApiResponse.success("更新学生状态成功", student);
        } catch (Exception e) {
            return ApiResponse.error("更新学生状态失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ApiResponse.success("删除学生成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除学生失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    public ApiResponse<Void> deleteStudents(@RequestBody List<Long> studentIds) {
        try {
            studentService.deleteStudents(studentIds);
            return ApiResponse.success("批量删除学生成功", null);
        } catch (Exception e) {
            return ApiResponse.error("批量删除学生失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ApiResponse<StudentService.StudentStatistics> getStudentStatistics() {
        try {
            StudentService.StudentStatistics statistics = studentService.getStudentStatistics();
            return ApiResponse.success("获取学生统计信息成功", statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取学生统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/classes")
    public ApiResponse<List<String>> getAllClasses() {
        try {
            List<String> classes = studentService.findDistinctClassNames();
            return ApiResponse.success("获取班级列表成功", classes);
        } catch (Exception e) {
            return ApiResponse.error("获取班级列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/majors")
    public ApiResponse<List<String>> getAllMajors() {
        try {
            List<String> majors = studentService.findDistinctMajors();
            return ApiResponse.success("获取专业列表成功", majors);
        } catch (Exception e) {
            return ApiResponse.error("获取专业列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/grades")
    public ApiResponse<List<String>> getAllGrades() {
        try {
            List<String> grades = studentService.findDistinctGrades();
            return ApiResponse.success("获取年级列表成功", grades);
        } catch (Exception e) {
            return ApiResponse.error("获取年级列表失败: " + e.getMessage());
        }
    }
}