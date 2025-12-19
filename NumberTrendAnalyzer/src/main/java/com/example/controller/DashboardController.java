package com.example.controller;

import com.example.dto.response.DashboardStatsDTO;
import com.example.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    /**
     * Lấy thống kê tổng quan cho dashboard
     * GET /api/dashboard/stats
     * GET /api/dashboard/stats?month=2025-11 (format: yyyy-MM)
     * GET /api/dashboard/stats?loGanMonth=2025-11&loHotMonth=2025-11
     * GET /api/dashboard/stats?algorithm=Frequency Analysis&rangeSize=20
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
            @RequestParam(value = "month", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(value = "loGanMonth", required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth loGanMonth,
            @RequestParam(value = "loHotMonth", required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth loHotMonth,
            @RequestParam(value = "algorithm", required = false) String algorithm,
            @RequestParam(value = "rangeSize", required = false) Integer rangeSize) {
        
        DashboardStatsDTO stats;
        // Nếu có month thì dùng cho cả hai, nếu không thì dùng loGanMonth và loHotMonth riêng
        if (month != null) {
            stats = dashboardService.getDashboardStats(month, month, algorithm, rangeSize);
        } else {
            stats = dashboardService.getDashboardStats(loGanMonth, loHotMonth, algorithm, rangeSize);
        }
        return ResponseEntity.ok(stats);
    }
}

