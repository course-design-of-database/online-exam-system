package org.bishe.question.service;

import com.github.pagehelper.PageHelper;
import org.bishe.question.entity.*;
import org.bishe.question.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务
 */
@Service
public class DashboardService {

    @Autowired private UserMapper userMapper;
    @Autowired private StudentMapper studentMapper;
    @Autowired private ExamMapper examMapper;
    @Autowired private QuestionMapper questionMapper;
    @Autowired private ExamRecordMapper examRecordMapper;

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userMapper.count();
        long totalStudents = studentMapper.count();
        long totalExams = examMapper.count();
        long activeExams = examMapper.countByStatus(1);
        long totalQuestions = questionMapper.count();
        long totalExamRecords = examRecordMapper.count();
        long completedExams = examRecordMapper.countByStatus(2);

        double completionRate = totalExamRecords > 0 ? (double) completedExams / totalExamRecords * 100 : 0;

        List<ExamRecord> completedRecords = examRecordMapper.findByStatus(2);
        double averageScore = completedRecords.stream()
            .filter(r -> r.getScore() != null)
            .mapToDouble(r -> r.getScore().doubleValue())
            .average().orElse(0.0);

        stats.put("totalUsers", totalUsers);
        stats.put("totalStudents", totalStudents);
        stats.put("totalExams", totalExams);
        stats.put("activeExams", activeExams);
        stats.put("totalQuestions", totalQuestions);
        stats.put("totalExamRecords", totalExamRecords);
        stats.put("completedExams", completedExams);
        stats.put("completionRate", Math.round(completionRate * 10.0) / 10.0);
        stats.put("averageScore", Math.round(averageScore * 10.0) / 10.0);
        stats.put("userGrowthRate", calculateGrowthRate(totalUsers));
        stats.put("examGrowthRate", calculateGrowthRate(totalExams));
        return stats;
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> userStats = new HashMap<>();
        long totalUsers = userMapper.count();
        long totalStudents = studentMapper.count();
        long adminUsers = totalUsers - totalStudents;

        userStats.put("totalUsers", totalUsers);
        userStats.put("totalStudents", totalStudents);
        userStats.put("adminUsers", adminUsers);

        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<ExamRecord> recentRecords = examRecordMapper.findByCreatedAtAfter(lastWeek);
        Set<Long> activeStudents = recentRecords.stream()
            .map(ExamRecord::getStudentId).collect(Collectors.toSet());

        userStats.put("activeStudentsLastWeek", activeStudents.size());
        userStats.put("activityRate", totalStudents > 0 ?
            Math.round((double) activeStudents.size() / totalStudents * 100 * 10.0) / 10.0 : 0);
        return userStats;
    }

    public Map<String, Object> getExamStatistics() {
        Map<String, Object> examStats = new HashMap<>();
        long totalExams = examMapper.count();
        long pendingExams = examMapper.countByStatus(0);
        long activeExams = examMapper.countByStatus(1);
        long finishedExams = examMapper.countByStatus(2);

        examStats.put("totalExams", totalExams);
        examStats.put("pendingExams", pendingExams);
        examStats.put("activeExams", activeExams);
        examStats.put("finishedExams", finishedExams);

        List<Exam> recentExams = examMapper.findTop5ByOrderByCreatedAtDesc();
        List<Map<String, Object>> recentExamList = recentExams.stream().map(exam -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", exam.getId());
            info.put("title", exam.getTitle());
            info.put("status", exam.getStatus());
            info.put("startTime", exam.getStartTime());
            info.put("endTime", exam.getEndTime());
            info.put("createdAt", exam.getCreatedAt());
            return info;
        }).collect(Collectors.toList());
        examStats.put("recentExams", recentExamList);
        return examStats;
    }

    public Map<String, Object> getQuestionStatistics() {
        Map<String, Object> questionStats = new HashMap<>();
        long totalQuestions = questionMapper.count();

        Map<String, Long> typeStats = new HashMap<>();
        typeStats.put("single", questionMapper.countSingleType("single"));
        typeStats.put("multiple", questionMapper.countSingleType("multiple"));
        typeStats.put("judge", questionMapper.countSingleType("judge"));
        typeStats.put("fill", questionMapper.countSingleType("fill"));
        typeStats.put("essay", questionMapper.countSingleType("essay"));

        Map<String, Long> difficultyStats = new HashMap<>();
        difficultyStats.put("easy", questionMapper.countSingleDifficulty("easy"));
        difficultyStats.put("medium", questionMapper.countSingleDifficulty("medium"));
        difficultyStats.put("hard", questionMapper.countSingleDifficulty("hard"));

        questionStats.put("totalQuestions", totalQuestions);
        questionStats.put("typeStats", typeStats);
        questionStats.put("difficultyStats", difficultyStats);
        return questionStats;
    }

    public Map<String, Object> getRecentActivities() {
        Map<String, Object> activities = new HashMap<>();

        List<ExamRecord> recentRecords = examRecordMapper.findTop10ByOrderByCreatedAtDesc();
        List<Map<String, Object>> recentExamRecords = recentRecords.stream().map(record -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", record.getId());
            info.put("examId", record.getExamId());
            info.put("studentId", record.getStudentId());
            info.put("score", record.getScore());
            info.put("status", record.getStatus());
            info.put("createdAt", record.getCreatedAt());
            info.put("submitTime", record.getSubmitTime());
            Exam exam = examMapper.findById(record.getExamId());
            info.put("examTitle", exam != null ? exam.getTitle() : "未知考试");
            Student student = studentMapper.findById(record.getStudentId());
            info.put("studentName", student != null ? student.getName() : "未知学生");
            return info;
        }).collect(Collectors.toList());
        activities.put("recentExamRecords", recentExamRecords);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        long todayExamRecords = examRecordMapper.countByCreatedAtBetween(todayStart, todayEnd);
        long todayCompletedExams = examRecordMapper.countByStatusAndSubmitTimeBetween(2, todayStart, todayEnd);
        activities.put("todayExamRecords", todayExamRecords);
        activities.put("todayCompletedExams", todayCompletedExams);
        return activities;
    }

    private double calculateGrowthRate(long currentValue) {
        Random random = new Random();
        return Math.round((5.0 + random.nextDouble() * 10.0) * 10.0) / 10.0;
    }
}