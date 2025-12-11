package com.example.controller;

import com.example.dto.LotteryDataDTO;
import com.example.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/lottery")
@CrossOrigin(origins = "*") // Cho phép frontend gọi API
public class LotteryController {

    @Autowired
    private CrawlerService crawlerService;

    /**
     * Lấy dữ liệu xổ số theo ngày
     * GET /api/lottery?date=2025-12-11
     */
    @GetMapping
    public ResponseEntity<LotteryDataDTO> getLotteryData(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        LotteryDataDTO data = crawlerService.getLotteryDataByDate(date);
        
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(data);
    }
}

