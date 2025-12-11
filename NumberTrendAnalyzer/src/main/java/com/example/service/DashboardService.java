package com.example.service;

import com.example.dto.response.DashboardStatsDTO;

public interface DashboardService {
    /**
     * Lấy tất cả thống kê cho dashboard
     */
    DashboardStatsDTO getDashboardStats();
}

