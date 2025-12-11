package com.example.service;

import com.example.dto.LotteryDataDTO;

import java.time.LocalDate;

public interface CrawlerService {
    /**
     * Cào dữ liệu xổ số cho một ngày cụ thể
     * @param date Ngày cần cào dữ liệu
     * @return Thông báo kết quả
     */
    String crawlAndSaveData(LocalDate date);

    /**
     * Cào dữ liệu xổ số cho một khoảng thời gian
     * @param fromDate Ngày bắt đầu
     * @param toDate Ngày kết thúc
     */
    void crawlRange(LocalDate fromDate, LocalDate toDate);

    /**
     * Lấy dữ liệu xổ số từ database theo ngày
     * @param date Ngày cần lấy dữ liệu
     * @return DTO chứa dữ liệu xổ số, null nếu không tìm thấy
     */
    LotteryDataDTO getLotteryDataByDate(LocalDate date);
}
