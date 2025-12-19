package com.example.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.response.AnalysisDataDTO;
import com.example.service.AnalysisService;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {
    
    @Autowired
    private AnalysisService analysisService;
    
    /**
     * Lấy dữ liệu phân tích với phân trang
     * GET /api/analysis?fromDate=2025-01-25&page=0&size=30&analysisType=50-50&dauDBStart=36&dauDBEnd=96&dbStart=36&dbEnd=95&dauG1Start=29&dauG1End=68&g1Start=29&g1End=8
     * GET /api/analysis?fromDate=2025-01-25&page=0&size=30&analysisType=even-odd
     */
    @GetMapping
    public ResponseEntity<AnalysisDataDTO> getAnalysisData(
            @RequestParam(value = "fromDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "30") int size,
            @RequestParam(value = "analysisType", defaultValue = "50-50") String analysisType,
            @RequestParam(value = "dauDBStart", required = false) Integer dauDBStart,
            @RequestParam(value = "dauDBEnd", required = false) Integer dauDBEnd,
            @RequestParam(value = "dbStart", required = false) Integer dbStart,
            @RequestParam(value = "dbEnd", required = false) Integer dbEnd,
            @RequestParam(value = "dauG1Start", required = false) Integer dauG1Start,
            @RequestParam(value = "dauG1End", required = false) Integer dauG1End,
            @RequestParam(value = "g1Start", required = false) Integer g1Start,
            @RequestParam(value = "g1End", required = false) Integer g1End) {
        
        // Nếu không có fromDate, mặc định lấy từ 30 ngày trước
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(30);
        }
        
        // Validate page và size
        if (page < 0) page = 0;
        if (size < 1) size = 30;
        if (size > 100) size = 100; // Giới hạn tối đa 100 phần tử mỗi trang
        
        AnalysisDataDTO data = analysisService.getAnalysisData(
                fromDate,
                page,
                size,
                analysisType,
                dauDBStart, dauDBEnd,
                dbStart, dbEnd,
                dauG1Start, dauG1End,
                g1Start, g1End
        );
        
        return ResponseEntity.ok(data);
    }
}

