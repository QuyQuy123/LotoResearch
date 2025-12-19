package com.example.service;

import com.example.dto.response.DashboardStatsDTO;

import java.time.YearMonth;

public interface DashboardService {
    /**
     * Lấy tất cả thống kê cho dashboard
     */
    DashboardStatsDTO getDashboardStats();
    
    /**
     * Lấy thống kê cho dashboard với filter theo tháng
     * @param yearMonth Tháng cần lọc (null = tất cả thời gian)
     */
    DashboardStatsDTO getDashboardStats(YearMonth yearMonth);
    
    /**
     * Lấy thống kê cho dashboard với filter riêng cho Lô Gan và Lô Hot
     * @param loGanMonth Tháng filter cho Lô Gan (null = tất cả thời gian)
     * @param loHotMonth Tháng filter cho Lô Hot (null = 30 ngày gần nhất)
     */
    DashboardStatsDTO getDashboardStats(YearMonth loGanMonth, YearMonth loHotMonth);
    
    /**
     * Lấy thống kê cho dashboard với filter riêng cho Lô Gan, Lô Hot và tùy chọn dự báo
     * @param loGanMonth Tháng filter cho Lô Gan (null = tất cả thời gian)
     * @param loHotMonth Tháng filter cho Lô Hot (null = 30 ngày gần nhất)
     * @param algorithm Thuật toán dự đoán (null = mặc định)
     * @param rangeSize Kích thước khoảng dự đoán (10-60, null = mặc định 20)
     */
    DashboardStatsDTO getDashboardStats(YearMonth loGanMonth, YearMonth loHotMonth, String algorithm, Integer rangeSize);
}

