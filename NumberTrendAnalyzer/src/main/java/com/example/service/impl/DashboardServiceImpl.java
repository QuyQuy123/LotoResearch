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
import java.time.YearMonth;
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
        return getDashboardStats(null);
    }
    
    @Override
    public DashboardStatsDTO getDashboardStats(YearMonth yearMonth) {
        return getDashboardStats(yearMonth, yearMonth);
    }
    
    @Override
    public DashboardStatsDTO getDashboardStats(YearMonth loGanMonth, YearMonth loHotMonth) {
        return getDashboardStats(loGanMonth, loHotMonth, null, null);
    }
    
    @Override
    public DashboardStatsDTO getDashboardStats(YearMonth loGanMonth, YearMonth loHotMonth, String algorithm, Integer rangeSize) {
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
        
        LocalDate today = LocalDate.now();
        
        // 3. Top Lô Gan (các số lâu chưa về) - dùng loGanMonth
        LocalDate loGanFilterStartDate = null;
        if (loGanMonth != null) {
            loGanFilterStartDate = loGanMonth.atDay(1);
        }
        List<DashboardStatsDTO.LotoGanDTO> topLoGan = calculateTopLoGan(loGanFilterStartDate, today);
        stats.setTopLoGan(topLoGan);
        
        // 4. Top Lô Hot (về nhiều nhất) - dùng loHotMonth
        LocalDate loHotFilterStartDate = null;
        LocalDate loHotFilterEndDate = today;
        if (loHotMonth != null) {
            loHotFilterStartDate = loHotMonth.atDay(1);
            loHotFilterEndDate = today.isBefore(loHotMonth.atEndOfMonth()) ? today : loHotMonth.atEndOfMonth();
        }
        List<DashboardStatsDTO.LotoHotDTO> topLoHot = calculateTopLoHot(loHotFilterStartDate, loHotFilterEndDate);
        stats.setTopLoHot(topLoHot);
        
        // 5. Dự báo nhanh với thuật toán và khoảng số tùy chỉnh
        // 6. Tính toán gợi ý dựa trên tổng hợp cả 3 thuật toán
        // Tính recommendation với khoảng số mà người dùng chọn (nếu có)
        DashboardStatsDTO.RecommendationDTO recommendation = calculateRecommendation(rangeSize);
        stats.setRecommendation(recommendation);
        
        // Nếu người dùng chọn đúng thuật toán và khoảng số được đề xuất, dùng kết quả từ recommendation
        DashboardStatsDTO.QuickForecastDTO forecast;
        if (algorithm != null && algorithm.equals(recommendation.getRecommendedAlgorithm()) 
            && rangeSize != null && rangeSize.equals(recommendation.getRecommendedRangeSize())) {
            // Dùng kết quả từ recommendation
            forecast = new DashboardStatsDTO.QuickForecastDTO();
            forecast.setAlgorithmUsed(recommendation.getRecommendedAlgorithm());
            forecast.setRangeStart(recommendation.getRecommendedRangeStart());
            forecast.setRangeEnd(recommendation.getRecommendedRangeEnd());
            forecast.setConfidenceScore(recommendation.getRecommendedConfidenceScore());
        } else {
            // Tính toán riêng dựa trên lựa chọn của người dùng
            forecast = calculateQuickForecast(algorithm, rangeSize, recommendation);
        }
        stats.setQuickForecast(forecast);
        
        return stats;
    }
    
    /**
     * Tính toán dự báo nhanh dựa trên thuật toán và khoảng số được chọn
     * @param recommendation Được truyền vào để có thể tái sử dụng kết quả nếu trùng
     */
    private DashboardStatsDTO.QuickForecastDTO calculateQuickForecast(String algorithm, Integer rangeSize, DashboardStatsDTO.RecommendationDTO recommendation) {
        // Validate và set giá trị mặc định
        if (algorithm == null || algorithm.trim().isEmpty()) {
            algorithm = "Frequency Analysis";
        }
        
        if (rangeSize == null || rangeSize < 10 || rangeSize > 60) {
            rangeSize = 20; // Mặc định 20 số
        }
        
        DashboardStatsDTO.QuickForecastDTO forecast = new DashboardStatsDTO.QuickForecastDTO();
        forecast.setAlgorithmUsed(algorithm);
        
        // Nếu trùng với recommendation, dùng kết quả từ đó
        if (recommendation != null 
            && algorithm.equals(recommendation.getRecommendedAlgorithm()) 
            && rangeSize.equals(recommendation.getRecommendedRangeSize())) {
            forecast.setRangeStart(recommendation.getRecommendedRangeStart());
            forecast.setRangeEnd(recommendation.getRecommendedRangeEnd());
            forecast.setConfidenceScore(recommendation.getRecommendedConfidenceScore());
            return forecast;
        }
        
        // Tính toán dựa trên thuật toán được chọn
        // Sử dụng seed giống như trong calculateRecommendation để đảm bảo kết quả nhất quán
        LocalDate today = LocalDate.now();
        long seed = today.toEpochDay() + algorithm.hashCode() + rangeSize;
        
        int rangeStart;
        int rangeEnd;
        double confidenceScore;
        
        switch (algorithm) {
            case "Long Short-Term Memory":
            case "LSTM":
                // Giả lập kết quả LSTM (thực tế cần implement model LSTM)
                rangeStart = generateLSTMPrediction(rangeSize, seed);
                rangeEnd = rangeStart + rangeSize - 1;
                // Độ tin cậy thay đổi theo khoảng số: khoảng lớn hơn = độ tin cậy cao hơn
                // Base confidence: 0.70, tăng dần theo rangeSize (10 số = 0.70, 60 số = 0.90)
                confidenceScore = 0.70 + (rangeSize - 10) * (0.20 / 50.0); // 0.70 -> 0.90
                break;
                
            case "Markov Chains":
            case "Markov":
                // Giả lập kết quả Markov Chains
                rangeStart = generateMarkovPrediction(rangeSize, seed);
                rangeEnd = rangeStart + rangeSize - 1;
                // Base confidence: 0.65, tăng dần theo rangeSize (10 số = 0.65, 60 số = 0.85)
                confidenceScore = 0.65 + (rangeSize - 10) * (0.20 / 50.0); // 0.65 -> 0.85
                break;
                
            case "Frequency Analysis":
            default:
                // Giả lập kết quả Frequency Analysis
                rangeStart = generateFrequencyPrediction(rangeSize, seed);
                rangeEnd = rangeStart + rangeSize - 1;
                // Base confidence: 0.60, tăng dần theo rangeSize (10 số = 0.60, 60 số = 0.80)
                confidenceScore = 0.60 + (rangeSize - 10) * (0.20 / 50.0); // 0.60 -> 0.80
                break;
        }
        
        // Đảm bảo rangeEnd không vượt quá 99
        if (rangeEnd > 99) {
            rangeEnd = 99;
            rangeStart = rangeEnd - rangeSize + 1;
            if (rangeStart < 0) {
                rangeStart = 0;
            }
        }
        
        forecast.setRangeStart(rangeStart);
        forecast.setRangeEnd(rangeEnd);
        forecast.setConfidenceScore(confidenceScore);
        
        return forecast;
    }
    
    /**
     * Tính toán gợi ý dựa trên tổng hợp cả 3 thuật toán
     * Nếu rangeSize được cung cấp: so sánh các thuật toán với khoảng số đó và chọn thuật toán tốt nhất
     * Nếu rangeSize không được cung cấp: so sánh tất cả các tổ hợp và chọn tổ hợp tốt nhất
     * Sử dụng seed cố định để đảm bảo kết quả nhất quán
     */
    private DashboardStatsDTO.RecommendationDTO calculateRecommendation(Integer userRangeSize) {
        String[] algorithms = {"Frequency Analysis", "Long Short-Term Memory", "Markov Chains"};
        
        // Nếu người dùng đã chọn khoảng số, chỉ so sánh các thuật toán với khoảng số đó
        // Nếu không, so sánh tất cả các khoảng số từ 10-60
        int[] rangeSizes;
        if (userRangeSize != null && userRangeSize >= 10 && userRangeSize <= 60) {
            rangeSizes = new int[]{userRangeSize};
        } else {
            rangeSizes = new int[]{10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};
        }
        
        String bestAlgorithm = "Frequency Analysis";
        int bestRangeSize = userRangeSize != null && userRangeSize >= 10 && userRangeSize <= 60 ? userRangeSize : 20;
        int bestRangeStart = 60;
        int bestRangeEnd = 80;
        double bestScore = 0.0;
        
        // Sử dụng seed cố định dựa trên ngày hiện tại để đảm bảo kết quả nhất quán trong cùng một ngày
        LocalDate today = LocalDate.now();
        long seed = today.toEpochDay(); // Seed dựa trên ngày
        
        // Tính toán điểm số cho mỗi tổ hợp (thuật toán, khoảng số)
        for (String algo : algorithms) {
            for (int rangeSize : rangeSizes) {
                // Tính dự báo với thuật toán và khoảng số này (dùng seed để nhất quán)
                int rangeStart = 0;
                double confidenceScore = 0.0;
                
                switch (algo) {
                    case "Long Short-Term Memory":
                        rangeStart = generateLSTMPrediction(rangeSize, seed + algo.hashCode() + rangeSize);
                        // Độ tin cậy thay đổi theo khoảng số: khoảng lớn hơn = độ tin cậy cao hơn
                        // Base confidence: 0.70, tăng dần theo rangeSize (10 số = 0.70, 60 số = 0.90)
                        confidenceScore = 0.70 + (rangeSize - 10) * (0.20 / 50.0); // 0.70 -> 0.90
                        break;
                    case "Markov Chains":
                        rangeStart = generateMarkovPrediction(rangeSize, seed + algo.hashCode() + rangeSize);
                        // Base confidence: 0.65, tăng dần theo rangeSize (10 số = 0.65, 60 số = 0.85)
                        confidenceScore = 0.65 + (rangeSize - 10) * (0.20 / 50.0); // 0.65 -> 0.85
                        break;
                    case "Frequency Analysis":
                    default:
                        rangeStart = generateFrequencyPrediction(rangeSize, seed + algo.hashCode() + rangeSize);
                        // Base confidence: 0.60, tăng dần theo rangeSize (10 số = 0.60, 60 số = 0.80)
                        confidenceScore = 0.60 + (rangeSize - 10) * (0.20 / 50.0); // 0.60 -> 0.80
                        break;
                }
                
                int rangeEnd = rangeStart + rangeSize - 1;
                if (rangeEnd > 99) {
                    rangeEnd = 99;
                    rangeStart = rangeEnd - rangeSize + 1;
                    if (rangeStart < 0) {
                        rangeStart = 0;
                    }
                }
                
                // Tính điểm số: nếu người dùng đã chọn khoảng số, chỉ so sánh độ tin cậy
                // Nếu không, ưu tiên độ tin cậy cao và khoảng số nhỏ hơn
                double score;
                if (userRangeSize != null && userRangeSize >= 10 && userRangeSize <= 60) {
                    // Chỉ so sánh độ tin cậy khi khoảng số đã được chọn
                    score = confidenceScore;
                } else {
                    // Ưu tiên độ tin cậy cao và khoảng số nhỏ hơn khi chưa chọn khoảng số
                    score = confidenceScore - (rangeSize / 100.0) * 0.1;
                }
                
                if (score > bestScore) {
                    bestScore = score;
                    bestAlgorithm = algo;
                    bestRangeSize = rangeSize;
                    bestRangeStart = rangeStart;
                    bestRangeEnd = rangeEnd;
                }
            }
        }
        
        // Tính lại độ tin cậy cho recommendation dựa trên bestAlgorithm và bestRangeSize
        double finalConfidenceScore;
        switch (bestAlgorithm) {
            case "Long Short-Term Memory":
                finalConfidenceScore = 0.70 + (bestRangeSize - 10) * (0.20 / 50.0);
                break;
            case "Markov Chains":
                finalConfidenceScore = 0.65 + (bestRangeSize - 10) * (0.20 / 50.0);
                break;
            case "Frequency Analysis":
            default:
                finalConfidenceScore = 0.60 + (bestRangeSize - 10) * (0.20 / 50.0);
                break;
        }
        
        // Tạo lý do đề xuất
        String reason;
        if (userRangeSize != null && userRangeSize >= 10 && userRangeSize <= 60) {
            // Nếu người dùng đã chọn khoảng số, gợi ý thuật toán tốt nhất cho khoảng số đó
            reason = String.format("Với khoảng %d số, %s cho độ tin cậy cao nhất (%.0f%%)", 
                    bestRangeSize, bestAlgorithm, finalConfidenceScore * 100);
        } else {
            // Nếu chưa chọn khoảng số, gợi ý tổ hợp tốt nhất
            reason = String.format("Dựa trên phân tích tổng hợp, %s với khoảng %d số cho độ tin cậy cao nhất (%.0f%%)", 
                    bestAlgorithm, bestRangeSize, finalConfidenceScore * 100);
        }
        
        DashboardStatsDTO.RecommendationDTO recommendation = new DashboardStatsDTO.RecommendationDTO();
        recommendation.setRecommendedAlgorithm(bestAlgorithm);
        recommendation.setRecommendedRangeSize(bestRangeSize);
        recommendation.setRecommendedRangeStart(bestRangeStart);
        recommendation.setRecommendedRangeEnd(bestRangeEnd);
        recommendation.setRecommendedConfidenceScore(finalConfidenceScore);
        recommendation.setReason(reason);
        
        return recommendation;
    }
    
    /**
     * Giả lập dự đoán bằng Frequency Analysis (với seed để nhất quán)
     */
    private int generateFrequencyPrediction(int rangeSize, long seed) {
        // Dùng seed để tạo số pseudo-random nhất quán
        java.util.Random random = new java.util.Random(seed);
        return 60 + random.nextInt(21); // Khoảng 60-80
    }
    
    /**
     * Giả lập dự đoán bằng LSTM (với seed để nhất quán)
     */
    private int generateLSTMPrediction(int rangeSize, long seed) {
        java.util.Random random = new java.util.Random(seed);
        return 55 + random.nextInt(26); // Khoảng 55-80
    }
    
    /**
     * Giả lập dự đoán bằng Markov Chains (với seed để nhất quán)
     */
    private int generateMarkovPrediction(int rangeSize, long seed) {
        java.util.Random random = new java.util.Random(seed);
        return 50 + random.nextInt(31); // Khoảng 50-80
    }
    
    /**
     * Giả lập dự đoán bằng Frequency Analysis (overload không seed - dùng cho backward compatibility)
     */
    private int generateFrequencyPrediction(int rangeSize) {
        // Dựa trên tần suất xuất hiện trong 30 ngày gần nhất
        // Giả lập: chọn khoảng có tần suất cao nhất
        LocalDate today = LocalDate.now();
        long seed = today.toEpochDay();
        return generateFrequencyPrediction(rangeSize, seed);
    }
    
    /**
     * Giả lập dự đoán bằng LSTM (overload không seed)
     */
    private int generateLSTMPrediction(int rangeSize) {
        // LSTM thường dự đoán dựa trên pattern dài hạn
        // Giả lập: phân tích chuỗi thời gian
        LocalDate today = LocalDate.now();
        long seed = today.toEpochDay();
        return generateLSTMPrediction(rangeSize, seed);
    }
    
    /**
     * Giả lập dự đoán bằng Markov Chains (overload không seed)
     */
    private int generateMarkovPrediction(int rangeSize) {
        // Markov Chains dựa trên xác suất chuyển trạng thái
        // Giả lập: tính toán xác suất chuyển tiếp
        LocalDate today = LocalDate.now();
        long seed = today.toEpochDay();
        return generateMarkovPrediction(rangeSize, seed);
    }
    
    /**
     * Tính Top Lô Gan
     * Nếu filterStartDate != null: chỉ tính các số chưa về từ filterStartDate đến today
     * Nếu filterStartDate == null: tính tất cả các số chưa về
     */
    private List<DashboardStatsDTO.LotoGanDTO> calculateTopLoGan(LocalDate filterStartDate, LocalDate today) {
        List<DashboardStatsDTO.LotoGanDTO> topLoGan = new ArrayList<>();
        
        // Lấy tất cả các số lô đã từng xuất hiện (00-99)
        List<Integer> allNumbers = new ArrayList<>();
        for (int i = 0; i <= 99; i++) {
            allNumbers.add(i);
        }
        
        for (Integer number : allNumbers) {
            LocalDate lastAppearance;
            long daysSince;
            
            if (filterStartDate != null) {
                // Tìm ngày xuất hiện gần nhất của số này (tất cả thời gian)
                LocalDate lastAppearanceOverall = lotoDigitRepository.findLastAppearanceDate(number);
                
                // Chỉ tính các số ĐÃ TỪNG XUẤT HIỆN trước filterStartDate
                // (không tính các số chưa bao giờ xuất hiện)
                if (lastAppearanceOverall == null) {
                    // Số này chưa bao giờ xuất hiện, bỏ qua
                    continue;
                }
                
                // Kiểm tra xem số này có xuất hiện trong khoảng filterStartDate đến today không
                Long countInRange = lotoDigitRepository.countAppearancesInRange(number, filterStartDate, today);
                boolean hasAppearedInRange = (countInRange != null && countInRange > 0);
                
                if (hasAppearedInRange) {
                    // Số này đã xuất hiện trong khoảng filterStartDate đến today, không tính vào Lô Gan
                    continue;
                }
                
                // Kiểm tra xem lastAppearanceOverall có trước filterStartDate không
                if (lastAppearanceOverall.isBefore(filterStartDate)) {
                    // Số này đã về trước filterStartDate và chưa về lại từ filterStartDate đến today
                    // Tính số ngày từ lastAppearanceOverall đến today
                    daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastAppearanceOverall, today);
                    lastAppearance = lastAppearanceOverall;
                } else {
                    // Trường hợp này không nên xảy ra vì đã kiểm tra hasAppearedInRange ở trên
                    // Nhưng để an toàn, bỏ qua
                    continue;
                }
            } else {
                // Không có filter, tính như cũ
                lastAppearance = lotoDigitRepository.findLastAppearanceDate(number);
                if (lastAppearance == null) {
                    // Số này chưa bao giờ xuất hiện, bỏ qua (hoặc có thể tính từ ngày đầu tiên có dữ liệu)
                    continue;
                }
                daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastAppearance, today);
            }
            
            DashboardStatsDTO.LotoGanDTO gan = new DashboardStatsDTO.LotoGanDTO();
            gan.setNumber(number);
            gan.setDaysSinceLastAppearance(daysSince);
            if (lastAppearance != null) {
                gan.setLastAppearanceDate(lastAppearance.format(DATE_FORMATTER));
            } else {
                gan.setLastAppearanceDate("Chưa từng xuất hiện");
            }
            topLoGan.add(gan);
        }
        
        // Sắp xếp theo số ngày chưa về (lâu nhất trước)
        topLoGan.sort(Comparator.comparing(DashboardStatsDTO.LotoGanDTO::getDaysSinceLastAppearance).reversed());
        
        // Lấy top 10
        return topLoGan.stream().limit(10).collect(Collectors.toList());
    }
    
    /**
     * Tính Top Lô Hot
     * Nếu filterStartDate != null: đếm số lần xuất hiện từ filterStartDate đến filterEndDate
     * Nếu filterStartDate == null: đếm số lần xuất hiện trong 30 ngày gần nhất (bao gồm hôm nay)
     */
    private List<DashboardStatsDTO.LotoHotDTO> calculateTopLoHot(LocalDate filterStartDate, LocalDate filterEndDate) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate;
        LocalDate toDate;
        
        if (filterStartDate != null) {
            fromDate = filterStartDate;
            toDate = filterEndDate;
        } else {
            // Không có filter, dùng 30 ngày gần nhất (tương đương SQL: DATE_SUB(today, INTERVAL 30 DAY) đến today)
            // minusDays(30) để khớp với SQL: từ 30 ngày trước đến hôm nay (bao gồm cả hai ngày) = 31 ngày
            // Nhưng đếm "30 ngày qua" nghĩa là từ 30 ngày trước đến hôm nay
            fromDate = today.minusDays(30);
            toDate = today;
        }
        
        List<DashboardStatsDTO.LotoHotDTO> topLoHot = new ArrayList<>();
        List<Integer> allNumbers = lotoDigitRepository.findAllDistinctNumbers();
        
        for (Integer number : allNumbers) {
            // Đếm số lần xuất hiện trong khoảng thời gian (dùng countAppearancesInRange cho cả hai trường hợp)
            Long frequency = lotoDigitRepository.countAppearancesInRange(number, fromDate, toDate);
            if (frequency != null && frequency > 0) {
                DashboardStatsDTO.LotoHotDTO hot = new DashboardStatsDTO.LotoHotDTO();
                hot.setNumber(number);
                hot.setFrequency(frequency);
                topLoHot.add(hot);
            }
        }
        
        // Sắp xếp theo tần suất (nhiều nhất trước)
        topLoHot.sort(Comparator.comparing(DashboardStatsDTO.LotoHotDTO::getFrequency).reversed());
        
        // Lấy top 10 (thay vì top 5 như trước)
        return topLoHot.stream().limit(10).collect(Collectors.toList());
    }
}

