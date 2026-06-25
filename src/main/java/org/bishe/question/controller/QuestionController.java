package org.bishe.question.controller;

import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.ApiResponse;
import org.bishe.question.dto.QuestionRequest;
import org.bishe.question.dto.QuestionResponse;
import org.bishe.question.entity.Question;
import org.bishe.question.service.ExcelExportService;
import org.bishe.question.service.ExcelImportService;
import org.bishe.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Workbook;

import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class QuestionController {

    @Autowired private QuestionService questionService;
    @Autowired private ExcelExportService excelExportService;
    @Autowired private ExcelImportService excelImportService;

    @PostMapping
    public ApiResponse<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest questionRequest) {
        try {
            Long createdBy = 1L;
            QuestionResponse response = questionService.createQuestion(questionRequest, createdBy);
            return ApiResponse.success("题目创建成功", response);
        } catch (Exception e) {
            return ApiResponse.error("创建题目失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionResponse> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest questionRequest) {
        try {
            return ApiResponse.success("题目更新成功", questionService.updateQuestion(id, questionRequest));
        } catch (Exception e) {
            return ApiResponse.error("更新题目失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error("删除题目失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionResponse> getQuestionById(@PathVariable Long id) {
        try {
            return ApiResponse.success("获取题目详情成功", questionService.getQuestionById(id));
        } catch (Exception e) {
            return ApiResponse.error("获取题目详情失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<PageInfo<QuestionResponse>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Question.QuestionType type,
            @RequestParam(required = false) Question.Difficulty difficulty) {
        try {
            PageInfo<QuestionResponse> response = questionService.getQuestions(page, size, keyword, type, difficulty);
            return ApiResponse.success("查询题目成功", response);
        } catch (Exception e) {
            return ApiResponse.error("查询题目失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ApiResponse<Object> getQuestionStats() {
        try {
            return ApiResponse.success("获取统计信息成功", questionService.getQuestionStats());
        } catch (Exception e) {
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Question.QuestionType type,
            @RequestParam(required = false) Question.Difficulty difficulty) {
        try {
            List<Question> questions = questionService.getQuestionsForExport(keyword, type, difficulty);
            byte[] excelData = excelExportService.exportQuestionsToExcel(questions);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "questions.xlsx");
            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            Workbook workbook = excelImportService.generateTemplate();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            byte[] bytes = outputStream.toByteArray();
            outputStream.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "question_import_template.xlsx");
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importQuestions(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ApiResponse.error("请选择要上传的文件");
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")))
                return ApiResponse.error("请上传Excel文件(.xlsx或.xls格式)");
            Map<String, Object> result = excelImportService.importQuestionsFromExcel(file);
            return ApiResponse.success("导入完成", result);
        } catch (IOException e) {
            return ApiResponse.error("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("题目管理服务运行正常", "OK");
    }
}