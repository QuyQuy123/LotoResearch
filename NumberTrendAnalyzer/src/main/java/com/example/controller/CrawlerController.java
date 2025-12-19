package com.example.controller;

import com.example.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/crawl")
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    // API 1: Cào 1 ngày cụ thể
    // Gọi: GET /api/crawl/single?date=11-12-2025
    @GetMapping("/single")
    public String crawlDate(@RequestParam("date") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date) {
        return crawlerService.crawlAndSaveData(date);
    }

    // API 2: Cào cả năm (Dùng để nạp dữ liệu lần đầu)
    // Gọi: GET /api/crawl/range?from=01-01-2023&to=31-12-2023
    @GetMapping("/range")
    public String crawlRange(
            @RequestParam("from") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
            @RequestParam("to") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to) {

        // Chạy ngầm (Async) để không bắt người dùng chờ
        new Thread(() -> crawlerService.crawlRange(from, to)).start();

        return "Đang bắt đầu cào dữ liệu từ " + from + " đến " + to + ". Hãy kiểm tra console log.";
    }
    
    // API 3: Tự động cập nhật từ ngày cuối cùng đến hôm nay
    // Gọi: GET /api/crawl/auto-update
    @GetMapping("/auto-update")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> autoUpdate() {
        try {
            String result = crawlerService.autoUpdateFromLastDate();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi cập nhật dữ liệu: " + e.getMessage());
        }
    }
}