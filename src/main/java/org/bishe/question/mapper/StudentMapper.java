package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.bishe.question.entity.Student;
import java.util.List;

@Mapper
public interface StudentMapper {
    Student findById(Long id);
    List<Student> findAll();
    int insert(Student student);
    int update(Student student);
    int deleteById(Long id);
    void deleteAllById(@Param("ids") List<Long> ids);
    long count();
    boolean existsById(Long id);

    Student findByStudentNumber(String studentNumber);
    Student findByEmail(String email);
    Student findByStudentNumberOrEmail(@Param("studentNumber") String studentNumber, @Param("email") String email);
    boolean existsByStudentNumber(String studentNumber);
    boolean existsByEmail(String email);
    List<Student> findByStatus(@Param("status") Integer status);
    List<Student> findByClassName(String className);
    List<Student> findByMajor(String major);
    List<Student> findByGrade(String grade);
    List<Student> findByNameContaining(@Param("name") String name);

    List<Student> findByKeyword(@Param("keyword") String keyword);
    long countByStatus(@Param("status") Integer status);
    long countByClassName(String className);
    long countByMajor(String major);
    List<String> findDistinctClassNames();
    List<String> findDistinctMajors();
    List<String> findDistinctGrades();
}
