package com.fitnessclub.membershipportal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kiểm tra kết nối DB và dữ liệu app đang thấy (để so sánh với MySQL Workbench).
 * Mở: http://localhost:8081/api/debug/db-check
 */
@RestController
@RequestMapping("/api/debug")
public class DbCheckController {

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    public DbCheckController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db-check")
    public Map<String, Object> dbCheck() {
        Map<String, Object> out = new HashMap<>();
        // URL có thể chứa password – chỉ hiển thị phần host/database
        String safeUrl = datasourceUrl != null && datasourceUrl.contains("//") 
            ? datasourceUrl.replaceFirst(":[^:@]+@", ":****@") 
            : datasourceUrl;
        out.put("datasourceUrl", safeUrl);
        out.put("message", "So sánh enrollmentIds với kết quả trong MySQL Workbench: SELECT id FROM membership_enrollments ORDER BY id DESC;");

        try {
            List<Integer> ids = jdbcTemplate.query(
                "SELECT id FROM membership_enrollments ORDER BY id DESC LIMIT 15",
                (rs, rowNum) -> rs.getInt("id"));
            out.put("enrollmentIds_from_app", ids);
            out.put("ok", true);
        } catch (Exception e) {
            out.put("ok", false);
            out.put("error", e.getMessage());
        }
        return out;
    }
}
