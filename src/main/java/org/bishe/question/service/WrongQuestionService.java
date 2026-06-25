package org.bishe.question.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.WrongQuestionDTO;
import org.bishe.question.entity.WrongQuestion;
import org.bishe.question.mapper.WrongQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 错题本服务类
 */
@Service
@Transactional
public class WrongQuestionService {

    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;

    /**
     * 分页获取错题列表
     */
    @Transactional(readOnly = true)
    public PageInfo<WrongQuestionDTO> getWrongQuestions(Long studentId, int page, int size, Integer mastered) {
        PageHelper.startPage(page + 1, size, "updated_time desc");
        List<WrongQuestion> list = wrongQuestionMapper.findByStudentIdWithPagination(studentId, mastered);
        PageInfo<WrongQuestion> pageInfo = new PageInfo<>(list);
        List<WrongQuestionDTO> dtoList = pageInfo.getList().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageInfo<WrongQuestionDTO> result = new PageInfo<>();
        result.setList(dtoList);
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        return result;
    }

    /**
     * 获取错题统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getWrongQuestionStats(Long studentId) {
        long total = wrongQuestionMapper.countByStudentId(studentId);
        long mastered = wrongQuestionMapper.countByStudentIdAndMastered(studentId, 1);
        long notMastered = total - mastered;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("mastered", mastered);
        stats.put("notMastered", notMastered);
        stats.put("masteryRate", total > 0 ? Math.round((double) mastered / total * 10000.0) / 100.0 : 0);
        return stats;
    }

    /**
     * 切换掌握状态
     */
    public void toggleMastered(Long id) {
        WrongQuestion wq = wrongQuestionMapper.findById(id);
        if (wq == null) throw new RuntimeException("错题记录不存在");
        int newStatus = wq.getMastered() == 1 ? 0 : 1;
        wrongQuestionMapper.markMastered(wq.getStudentId(), wq.getQuestionId(), newStatus);
    }

    /**
     * 删除错题记录
     */
    public void deleteWrongQuestion(Long id) {
        if (wrongQuestionMapper.findById(id) == null)
            throw new RuntimeException("错题记录不存在");
        wrongQuestionMapper.deleteById(id);
    }

    /**
     * 记录错题（考试提交时调用）
     * 存在则累加错误次数，不存在则新增
     */
    public void recordWrongAnswer(Long studentId, Long questionId, Long examId, String wrongAnswer) {
        WrongQuestion existing = wrongQuestionMapper.findByStudentIdAndQuestionId(studentId, questionId);

        WrongQuestion wq = new WrongQuestion();
        wq.setStudentId(studentId);
        wq.setQuestionId(questionId);
        wq.setExamId(examId);
        wq.setLastWrongAnswer(wrongAnswer);
        wq.setMastered(0); // 又做错了，重置掌握状态

        if (existing != null) {
            wq.setId(existing.getId());
            wq.setWrongCount(existing.getWrongCount() + 1);
            wrongQuestionMapper.update(wq);
        } else {
            wq.setWrongCount(1);
            wrongQuestionMapper.insert(wq);
        }
    }

    /**
     * 当学生答对一道题时，如果之前有错题记录，自动标记为已掌握
     */
    public void autoMarkMastered(Long studentId, Long questionId) {
        WrongQuestion existing = wrongQuestionMapper.findByStudentIdAndQuestionId(studentId, questionId);
        if (existing != null && existing.getMastered() == 0) {
            wrongQuestionMapper.markMastered(studentId, questionId, 1);
        }
    }

    private WrongQuestionDTO convertToDTO(WrongQuestion wq) {
        WrongQuestionDTO dto = new WrongQuestionDTO();
        dto.setId(wq.getId());
        dto.setQuestionId(wq.getQuestionId());
        dto.setQuestionTitle(wq.getQuestionTitle());
        dto.setQuestionContent(wq.getQuestionContent());
        dto.setQuestionType(wq.getQuestionType());
        dto.setQuestionDifficulty(wq.getQuestionDifficulty());
        dto.setQuestionOptions(wq.getQuestionOptions());
        dto.setCorrectAnswers(wq.getCorrectAnswers());
        dto.setCorrectAnswer(wq.getCorrectAnswer());
        dto.setQuestionTags(wq.getQuestionTags());
        dto.setWrongCount(wq.getWrongCount());
        dto.setMastered(wq.getMastered());
        dto.setLastWrongAnswer(wq.getLastWrongAnswer());
        return dto;
    }
}
