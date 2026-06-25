package org.bishe.question.util;

/**
 * SQL工具类
 */
public class SqlUtils {

    /**
     * 将驼峰命名转换为下划线命名
     * e.g. createdTime -> created_time, studentNumber -> student_number
     */
    public static String camelToSnake(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 在大写字母前插入下划线，然后全转小写
        String result = input.replaceAll("([A-Z])", "_$1").toLowerCase();
        // 处理连续大写的情况（如 XMLHttpRequest -> xml_http_request）
        return result;
    }
}
