package com.example.service.impl;

import com.example.domain.PredictionLog;
import com.example.dto.response.DashboardStatsDTO;
import com.example.repository.LotteryDailyResultRepository;
import com.example.repository.LotoDigitRepository;
import com.example.repository.PredictionLogRepository;
import com.example.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Autowired
    private LotteryDailyResultRepository resultRepository;
    
    @Autowired
    private LotoDigitRepository lotoDigitRepository;
    
    @Autowired
    private PredictionLogRepository predictionLogRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // 1. Tổng số ngày có dữ liệu
        long totalDays = resultRepository.count();
        stats.setTotalDays(totalDays);
        
        // 2. Ngày cập nhật gần nhất
        Optional<LocalDate> lastUpdateDateOpt = resultRepository.findLatestDrawDate();
        
        if (lastUpdateDateOpt.isPresent()) {
            stats.setLastUpdateDate(lastUpdateDateOpt.get().format(DATE_FORMATTER));
        } else {
            stats.setLastUpdateDate("Chưa có dữ liệu");
        }
        
        // 3. Top Lô Gan (các số lâu chưa về)
        LocalDate today = LocalDate.now();
        List<DashboardStatsDTO.LotoGanDTO> topLoGan = new ArrayList<>();
        
        // Lấy tất cả các số lô đã từng xuất hiện
        List<Integer> allNumbers = lotoDigitRepository.findAllDistinctNumbers();
        
        for (Integer number : allNumbers) {
            LocalDate lastAppearance = lotoDigitRepository.findLastAppearanceDate(number);
            if (lastAppearance != null) {
                long daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastAppearance, today);
                
                DashboardStatsDTO.LotoGanDTO gan = new DashboardStatsDTO.LotoGanDTO();
                gan.setNumber(number);
                gan.setDaysSinceLastAppearance(daysSince);
                gan.setLastAppearanceDate(lastAppearance.format(DATE_FORMATTER));
                topLoGan.add(gan);
            }
        }
        
        // Sắp xếp theo số ngày chưa về (lâu nhất trước)
        topLoGan.sort(Comparator.comparing(DashboardStatsDTO.LotoGanDTO::getDaysSinceLastAppearance).reversed());
        
        // Lấy top 10
        stats.setTopLoGan(topLoGan.stream().limit(10).collect(Collectors.toList()));
        
        // 4. Top Lô Hot (về nhiều nhất trong 30 ngày)
        LocalDate thirtyDaysAgo = today.minusDays(30);
        List<DashboardStatsDTO.LotoHotDTO> topLoHot = new ArrayList<>();
        
        for (Integer number : allNumbers) {
            Long frequency = lotoDigitRepository.countAppearancesInRange(number, thirtyDaysAgo);
            if (frequency != null && frequency > 0) {
                DashboardStatsDTO.LotoHotDTO hot = new DashboardStatsDTO.LotoHotDTO();
                hot.setNumber(number);
                hot.setFrequency(frequency);
                topLoHot.add(hot);
            }
        }
        
        // Sắp xếp theo tần suất (nhiều nhất trước)
        topLoHot.sort(Comparator.comparing(DashboardStatsDTO.LotoHotDTO::getFrequency).reversed());
        
        // Lấy top 5
        stats.setTopLoHot(topLoHot.stream().limit(5).collect(Collectors.toList()));
        
        // 5. Dự báo nhanh
        Optional<PredictionLog> latestPrediction = predictionLogRepository.findLatestPrediction(today);
        if (latestPrediction.isPresent()) {
            PredictionLog prediction = latestPrediction.get();
            DashboardStatsDTO.QuickForecastDTO forecast = new DashboardStatsDTO.QuickForecastDTO();
            forecast.setRangeStart(prediction.getRangeStart());
            forecast.setRangeEnd(prediction.getRangeEnd());
            forecast.setConfidenceScore(prediction.getConfidenceScore());
            forecast.setAlgorithmUsed(prediction.getAlgorithmUsed());
            stats.setQuickForecast(forecast);
        } else {
            // Nếu chưa có dự đoán, tạo một dự đoán mặc định
            DashboardStatsDTO.QuickForecastDTO forecast = new DashboardStatsDTO.QuickForecastDTO();
            forecast.setRangeStart(60);
            forecast.setRangeEnd(80);
            forecast.setConfidenceScore(0.75);
            forecast.setAlgorithmUsed("Frequency Analysis");
            stats.setQuickForecast(forecast);
        }
        
        return stats;
    }
}

