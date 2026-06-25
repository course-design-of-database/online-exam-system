package org.bishe.question.controller;

import org.bishe.question.dto.ApiResponse;
import org.bishe.question.dto.WrongQuestionDTO;
import org.bishe.question.service.WrongQuestionService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 错题本控制器
 * 提供学生错题管理API接口
 */
@RestController
@RequestMapping("/v1/student")
@CrossOrigin(origins = "*")
public class WrongQuestionController {

    @Autowired
    private WrongQuestionService wrongQuestionService;

    /**
     * 获取学生错题列表（分页 + 掌握状态筛选）
     */
    @GetMapping("/wrong-questions")
    public ApiResponse<PageInfo<WrongQuestionDTO>> getWrongQuestions(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer mastered) {
        try {
            PageInfo<WrongQuestionDTO> result = wrongQuestionService.getWrongQuestions(studentId, page, size, mastered);
            return ApiResponse.success("获取错题列表成功", result);
        } catch (Exception e) {
            return ApiResponse.error("获取错题列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生错题统计
     */
    @GetMapping("/wrong-questions/stats")
    public ApiResponse<Map<String, Object>> getWrongQuestionStats(@RequestParam Long studentId) {
        try {
            Map<String, Object> stats = wrongQuestionService.getWrongQuestionStats(studentId);
            return ApiResponse.success("获取错题统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error("获取错题统计失败: " + e.getMessage());
        }
    }

    /**
     * 切换掌握状态（已掌握 ↔ 未掌握）
     */
    @PutMapping("/wrong-questions/{id}/master")
    public ApiResponse<String> toggleMastered(@PathVariable Long id) {
        try {
            wrongQuestionService.toggleMastered(id);
            return ApiResponse.success("操作成功", "掌握状态已切换");
        } catch (Exception e) {
            return ApiResponse.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 删除错题记录
     */
    @DeleteMapping("/wrong-questions/{id}")
    public ApiResponse<String> deleteWrongQuestion(@PathVariable Long id) {
        try {
            wrongQuestionService.deleteWrongQuestion(id);
            return ApiResponse.success("删除成功", "错题记录已删除");
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}
