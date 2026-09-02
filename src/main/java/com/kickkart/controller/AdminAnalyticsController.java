package com.kickkart.controller;

import com.kickkart.dto.AnalyticsDto;
import com.kickkart.dto.ApiResponse;
import com.kickkart.service.AdminAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<AnalyticsDto>> getDailyAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        AnalyticsDto dto = adminAnalyticsService.getDailyAnalytics(date);
        return ResponseEntity.ok(ApiResponse.success("Daily analytics retrieved", dto));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<AnalyticsDto>> getMonthlyAnalytics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        if (year == null) year = now.getYear();
        if (month == null) month = now.getMonthValue();
        AnalyticsDto dto = adminAnalyticsService.getMonthlyAnalytics(year, month);
        return ResponseEntity.ok(ApiResponse.success("Monthly analytics retrieved", dto));
    }

    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<AnalyticsDto>> getYearlyAnalytics(
            @RequestParam(required = false) Integer year) {
        if (year == null) year = LocalDate.now().getYear();
        AnalyticsDto dto = adminAnalyticsService.getYearlyAnalytics(year);
        return ResponseEntity.ok(ApiResponse.success("Yearly analytics retrieved", dto));
    }

    @GetMapping("/overall")
    public ResponseEntity<ApiResponse<AnalyticsDto>> getOverallAnalytics() {
        AnalyticsDto dto = adminAnalyticsService.getOverallAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Overall business analytics retrieved", dto));
    }
}
