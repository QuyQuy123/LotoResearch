package com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    // Tổng số ngày có dữ liệu
    private Long totalDays;
    
    // Ngày cập nhật gần nhất
    private String lastUpdateDate;
    
    // Top Lô Gan (các số lâu chưa về)
    private List<LotoGanDTO> topLoGan;
    
    // Top Lô Hot (về nhiều nhất trong 30 ngày)
    private List<LotoHotDTO> topLoHot;
    
    // Dự báo nhanh
    private QuickForecastDTO quickForecast;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LotoGanDTO {
        private Integer number; // Số lô (00-99)
        private Long daysSinceLastAppearance; // Số ngày chưa về
        private String lastAppearanceDate; // Ngày về gần nhất
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LotoHotDTO {
        private Integer number; // Số lô (00-99)
        private Long frequency; // Số lần xuất hiện trong 30 ngày
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickForecastDTO {
        private Integer rangeStart; // Khoảng dự đoán bắt đầu
        private Integer rangeEnd; // Khoảng dự đoán kết thúc
        private Double confidenceScore; // Độ tin cậy (0-1)
        private String algorithmUsed; // Thuật toán sử dụng
    }
}

