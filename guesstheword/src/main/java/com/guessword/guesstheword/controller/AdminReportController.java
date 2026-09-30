package com.guessword.guesstheword.controller;

import com.guessword.guesstheword.dto.DailyReportResponse;
import com.guessword.guesstheword.dto.UserReportResponse;
import com.guessword.guesstheword.service.AdminReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {
    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping("/daily")
    public DailyReportResponse getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return adminReportService.getDailyReport(date);
    }

    @GetMapping("/user/{userId}")
    public UserReportResponse getUserReport(@PathVariable Long userId) {
        return adminReportService.getUserReport(userId);
    }
}
