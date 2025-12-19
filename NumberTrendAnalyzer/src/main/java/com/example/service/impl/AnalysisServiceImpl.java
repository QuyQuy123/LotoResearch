package com.example.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.domain.LotoDigit;
import com.example.domain.LotteryDailyResult;
import com.example.dto.response.AnalysisDataDTO;
import com.example.repository.LotteryDailyResultRepository;
import com.example.service.AnalysisService;

@Service
public class AnalysisServiceImpl implements AnalysisService {
    
    @Autowired
    private LotteryDailyResultRepository resultRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    @Override
    public AnalysisDataDTO getAnalysisData(
            LocalDate fromDate,
            int page,
            int size,
            String analysisType,
            Integer dauDBStart, Integer dauDBEnd,
            Integer dbStart, Integer dbEnd,
            Integer dauG1Start, Integer dauG1End,
            Integer g1Start, Integer g1End
    ) {
        // Lấy tất cả kết quả từ fromDate trở đi, sắp xếp theo ngày
        List<LotteryDailyResult> allResults = resultRepository.findAllFromDate(fromDate);
        
        // Tính toán phân trang
        long totalElements = allResults.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, allResults.size());
        
        // Lấy dữ liệu cho trang hiện tại
        List<LotteryDailyResult> results = new ArrayList<>();
        if (startIndex < allResults.size()) {
            results = allResults.subList(startIndex, endIndex);
        }
        
        List<AnalysisDataDTO.AnalysisRowDTO> rows = new ArrayList<>();
        
        for (LotteryDailyResult result : results) {
            AnalysisDataDTO.AnalysisRowDTO row = createAnalysisRow(result, analysisType, dauDBStart, dauDBEnd, 
                    dbStart, dbEnd, dauG1Start, dauG1End, g1Start, g1End);
            rows.add(row);
        }
        
        AnalysisDataDTO dto = new AnalysisDataDTO();
        dto.setRows(rows);
        dto.setTotalPages(totalPages);
        dto.setCurrentPage(page);
        dto.setTotalElements(totalElements);
        
        // Tính toán thống kê rỗng (dùng toàn bộ dữ liệu, không chỉ trang hiện tại)
        List<AnalysisDataDTO.AnalysisRowDTO> allRows = new ArrayList<>();
        for (LotteryDailyResult result : allResults) {
            AnalysisDataDTO.AnalysisRowDTO row = createAnalysisRow(result, analysisType, dauDBStart, dauDBEnd, 
                    dbStart, dbEnd, dauG1Start, dauG1End, g1Start, g1End);
            allRows.add(row);
        }
        
        List<AnalysisDataDTO.EmptyStatsDTO> emptyStats = calculateEmptyStats(
                allRows, analysisType, dauDBStart, dauDBEnd, dbStart, dbEnd, dauG1Start, dauG1End, g1Start, g1End
        );
        dto.setEmptyStats(emptyStats);
        
        return dto;
    }
    
    /**
     * Tính toán thống kê rỗng (chuỗi match liên tiếp)
     */
    private List<AnalysisDataDTO.EmptyStatsDTO> calculateEmptyStats(
            List<AnalysisDataDTO.AnalysisRowDTO> rows,
            String analysisType,
            Integer dauDBStart, Integer dauDBEnd,
            Integer dbStart, Integer dbEnd,
            Integer dauG1Start, Integer dauG1End,
            Integer g1Start, Integer g1End
    ) {
        List<AnalysisDataDTO.EmptyStatsDTO> statsList = new ArrayList<>();
        boolean isEvenOdd = "even-odd".equals(analysisType);
        
        if (isEvenOdd) {
            // Với even-odd, tính toán cho tất cả các cột (không cần filter)
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu ĐB", 
                    row -> row.getDauDBMatch(), null, null));
            statsList.add(calculateEmptyStatsForColumn(rows, "ĐB", 
                    row -> row.getDbMatch(), null, null));
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu G1", 
                    row -> row.getDauG1Match(), null, null));
            statsList.add(calculateEmptyStatsForColumn(rows, "G1", 
                    row -> row.getG1Match(), null, null));
        } else {
            // Với 50-50, chỉ tính toán nếu có filter
            if (dauDBStart != null && dauDBEnd != null) {
                statsList.add(calculateEmptyStatsForColumn(rows, "Đầu ĐB", 
                        row -> row.getDauDBMatch(), dauDBStart, dauDBEnd));
            }
            
            if (dbStart != null && dbEnd != null) {
                statsList.add(calculateEmptyStatsForColumn(rows, "ĐB", 
                        row -> row.getDbMatch(), dbStart, dbEnd));
            }
            
            if (dauG1Start != null && dauG1End != null) {
                statsList.add(calculateEmptyStatsForColumn(rows, "Đầu G1", 
                        row -> row.getDauG1Match(), dauG1Start, dauG1End));
            }
            
            if (g1Start != null && g1End != null) {
                statsList.add(calculateEmptyStatsForColumn(rows, "G1", 
                        row -> row.getG1Match(), g1Start, g1End));
            }
        }
        
        return statsList;
    }
    
    /**
     * Tạo AnalysisRowDTO từ LotteryDailyResult
     */
    private AnalysisDataDTO.AnalysisRowDTO createAnalysisRow(
            LotteryDailyResult result,
            String analysisType,
            Integer dauDBStart, Integer dauDBEnd,
            Integer dbStart, Integer dbEnd,
            Integer dauG1Start, Integer dauG1End,
            Integer g1Start, Integer g1End
    ) {
        boolean isEvenOdd = "even-odd".equals(analysisType);
        AnalysisDataDTO.AnalysisRowDTO row = new AnalysisDataDTO.AnalysisRowDTO();
        row.setDate(result.getDrawDate().format(DATE_FORMATTER));
        
        // Lấy các số từ kết quả
        List<LotoDigit> digits = result.getLotoDigits();
        if (digits == null || digits.isEmpty()) {
            // Nếu không có dữ liệu, set null và match = 0
            row.setDauDB(null);
            row.setDb(null);
            row.setDauG1(null);
            row.setG1(null);
            row.setDauDBMatch(0);
            row.setDbMatch(0);
            row.setDauG1Match(0);
            row.setG1Match(0);
            return row;
        }
        
        // Tìm giải ĐB (Giai_DB)
        LotoDigit dbDigit = digits.stream()
                .filter(d -> "Giai_DB".equals(d.getPrizeName()))
                .findFirst()
                .orElse(null);
        
        if (dbDigit != null) {
            // Đầu ĐB: lấy 2 số đầu từ fullNumber hoặc specialPrizeRaw
            String dbFullNumber = dbDigit.getFullNumber();
            if (dbFullNumber == null || dbFullNumber.isEmpty()) {
                dbFullNumber = result.getSpecialPrizeRaw();
            }
            if (dbFullNumber != null && dbFullNumber.length() >= 2) {
                try {
                    String dauDBStr = dbFullNumber.substring(0, 2);
                    int dauDB = Integer.parseInt(dauDBStr);
                    row.setDauDB(dauDB);
                    // Kiểm tra match
                    if (isEvenOdd) {
                        // Chẵn = 0, Lẻ = 1
                        row.setDauDBMatch((dauDB % 2 == 0) ? 0 : 1);
                    } else {
                        // Logic 50-50: kiểm tra trong khoảng
                        if (dauDBStart != null && dauDBEnd != null) {
                            row.setDauDBMatch((dauDB >= dauDBStart && dauDB <= dauDBEnd) ? 1 : 0);
                        } else {
                            row.setDauDBMatch(0);
                        }
                    }
                } catch (NumberFormatException e) {
                    row.setDauDB(null);
                    row.setDauDBMatch(0);
                }
            } else {
                row.setDauDB(null);
                row.setDauDBMatch(0);
            }
            
            // ĐB: lấy 2 số cuối (value field)
            row.setDb(dbDigit.getValue());
            if (isEvenOdd) {
                // Chẵn = 0, Lẻ = 1
                row.setDbMatch((dbDigit.getValue() % 2 == 0) ? 0 : 1);
            } else {
                // Logic 50-50: kiểm tra trong khoảng
                if (dbStart != null && dbEnd != null) {
                    row.setDbMatch((dbDigit.getValue() >= dbStart && dbDigit.getValue() <= dbEnd) ? 1 : 0);
                } else {
                    row.setDbMatch(0);
                }
            }
        } else {
            row.setDauDB(null);
            row.setDb(null);
            row.setDauDBMatch(0);
            row.setDbMatch(0);
        }
        
        // Tìm giải 1 (Giai_1)
        LotoDigit g1Digit = digits.stream()
                .filter(d -> "Giai_1".equals(d.getPrizeName()))
                .findFirst()
                .orElse(null);
        
        if (g1Digit != null) {
            // Đầu G1: lấy 2 số đầu từ fullNumber
            String g1FullNumber = g1Digit.getFullNumber();
            if (g1FullNumber != null && g1FullNumber.length() >= 2) {
                try {
                    String dauG1Str = g1FullNumber.substring(0, 2);
                    int dauG1 = Integer.parseInt(dauG1Str);
                    row.setDauG1(dauG1);
                    // Kiểm tra match
                    if (isEvenOdd) {
                        // Chẵn = 0, Lẻ = 1
                        row.setDauG1Match((dauG1 % 2 == 0) ? 0 : 1);
                    } else {
                        // Logic 50-50: kiểm tra trong khoảng
                        if (dauG1Start != null && dauG1End != null) {
                            row.setDauG1Match((dauG1 >= dauG1Start && dauG1 <= dauG1End) ? 1 : 0);
                        } else {
                            row.setDauG1Match(0);
                        }
                    }
                } catch (NumberFormatException e) {
                    row.setDauG1(null);
                    row.setDauG1Match(0);
                }
            } else {
                row.setDauG1(null);
                row.setDauG1Match(0);
            }
            
            // G1: lấy 2 số cuối (value field)
            row.setG1(g1Digit.getValue());
            if (isEvenOdd) {
                // Chẵn = 0, Lẻ = 1
                row.setG1Match((g1Digit.getValue() % 2 == 0) ? 0 : 1);
            } else {
                // Logic 50-50: kiểm tra trong khoảng
                if (g1Start != null && g1End != null) {
                    row.setG1Match((g1Digit.getValue() >= g1Start && g1Digit.getValue() <= g1End) ? 1 : 0);
                } else {
                    row.setG1Match(0);
                }
            }
        } else {
            row.setDauG1(null);
            row.setG1(null);
            row.setDauG1Match(0);
            row.setG1Match(0);
        }
        
        return row;
    }
    
    /**
     * Tính toán thống kê rỗng cho một cột cụ thể
     */
    private AnalysisDataDTO.EmptyStatsDTO calculateEmptyStatsForColumn(
            List<AnalysisDataDTO.AnalysisRowDTO> rows,
            String columnName,
            java.util.function.Function<AnalysisDataDTO.AnalysisRowDTO, Integer> matchGetter,
            Integer rangeStart,
            Integer rangeEnd
    ) {
        // Với even-odd, range sẽ là khoảng số nếu có, hoặc "Chẵn/Lẻ" nếu không có
        String rangeStr = (rangeStart != null && rangeEnd != null) 
                ? (rangeStart + "-" + rangeEnd) 
                : "Chẵn/Lẻ";
        // Đếm số lần xuất hiện của mỗi độ dài rỗng
        Map<Integer, Integer> emptyCountMap = new HashMap<>();
        
        int currentEmptyLength = 0;
        
        for (AnalysisDataDTO.AnalysisRowDTO row : rows) {
            Integer match = matchGetter.apply(row);
            if (match != null && match == 1) {
                // Nếu match = 1, tăng độ dài rỗng hiện tại
                currentEmptyLength++;
            } else {
                // Nếu match = 0 hoặc null, kết thúc chuỗi rỗng
                if (currentEmptyLength >= 3) {
                    // Chỉ đếm các chuỗi rỗng từ 3 trở lên
                    emptyCountMap.put(currentEmptyLength, 
                            emptyCountMap.getOrDefault(currentEmptyLength, 0) + 1);
                }
                currentEmptyLength = 0;
            }
        }
        
        // Xử lý trường hợp chuỗi rỗng kéo dài đến cuối danh sách
        if (currentEmptyLength >= 3) {
            emptyCountMap.put(currentEmptyLength, 
                    emptyCountMap.getOrDefault(currentEmptyLength, 0) + 1);
        }
        
        // Chuyển đổi Map thành List và sắp xếp theo độ dài rỗng
        List<AnalysisDataDTO.EmptyStatsDTO.EmptyCountDTO> counts = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : emptyCountMap.entrySet()) {
            AnalysisDataDTO.EmptyStatsDTO.EmptyCountDTO countDTO = 
                    new AnalysisDataDTO.EmptyStatsDTO.EmptyCountDTO();
            countDTO.setEmptyLength(entry.getKey());
            countDTO.setCount(entry.getValue());
            counts.add(countDTO);
        }
        
        // Sắp xếp theo độ dài rỗng tăng dần
        counts.sort((a, b) -> Integer.compare(a.getEmptyLength(), b.getEmptyLength()));
        
        AnalysisDataDTO.EmptyStatsDTO stats = new AnalysisDataDTO.EmptyStatsDTO();
        stats.setColumnName(columnName);
        stats.setRange(rangeStr);
        stats.setCounts(counts);
        
        return stats;
    }
}

