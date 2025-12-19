package com.example.service;

import java.time.LocalDate;

import com.example.dto.response.AnalysisDataDTO;

public interface AnalysisService {
    /**
     * Lấy dữ liệu phân tích từ ngày bắt đầu với phân trang
     * @param fromDate Ngày bắt đầu
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số phần tử mỗi trang
     * @param analysisType Loại phân tích: "50-50" hoặc "even-odd"
     * @param dauDBStart Số bắt đầu cho Đầu ĐB (chỉ dùng cho 50-50)
     * @param dauDBEnd Số kết thúc cho Đầu ĐB (chỉ dùng cho 50-50)
     * @param dbStart Số bắt đầu cho ĐB (chỉ dùng cho 50-50)
     * @param dbEnd Số kết thúc cho ĐB (chỉ dùng cho 50-50)
     * @param dauG1Start Số bắt đầu cho Đầu G1 (chỉ dùng cho 50-50)
     * @param dauG1End Số kết thúc cho Đầu G1 (chỉ dùng cho 50-50)
     * @param g1Start Số bắt đầu cho G1 (chỉ dùng cho 50-50)
     * @param g1End Số kết thúc cho G1 (chỉ dùng cho 50-50)
     * @return DTO chứa dữ liệu phân tích
     */
    AnalysisDataDTO getAnalysisData(
            LocalDate fromDate,
            int page,
            int size,
            String analysisType,
            Integer dauDBStart, Integer dauDBEnd,
            Integer dbStart, Integer dbEnd,
            Integer dauG1Start, Integer dauG1End,
            Integer g1Start, Integer g1End
    );
}

