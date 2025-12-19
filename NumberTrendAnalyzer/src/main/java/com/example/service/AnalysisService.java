package com.example.service;

import com.example.dto.response.AnalysisDataDTO;

import java.time.LocalDate;

public interface AnalysisService {
    /**
     * Lấy dữ liệu phân tích từ ngày bắt đầu
     * @param fromDate Ngày bắt đầu
     * @param dauDBStart Số bắt đầu cho Đầu ĐB
     * @param dauDBEnd Số kết thúc cho Đầu ĐB
     * @param dbStart Số bắt đầu cho ĐB
     * @param dbEnd Số kết thúc cho ĐB
     * @param dauG1Start Số bắt đầu cho Đầu G1
     * @param dauG1End Số kết thúc cho Đầu G1
     * @param g1Start Số bắt đầu cho G1
     * @param g1End Số kết thúc cho G1
     * @return DTO chứa dữ liệu phân tích
     */
    AnalysisDataDTO getAnalysisData(
            LocalDate fromDate,
            Integer dauDBStart, Integer dauDBEnd,
            Integer dbStart, Integer dbEnd,
            Integer dauG1Start, Integer dauG1End,
            Integer g1Start, Integer g1End
    );
}

