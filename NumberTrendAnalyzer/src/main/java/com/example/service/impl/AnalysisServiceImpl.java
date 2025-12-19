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
        boolean isPrime = "prime".equals(analysisType);
        boolean isDivide3 = "divide-3".equals(analysisType);
        
        if (isEvenOdd) {
            // Với even-odd, tính toán cho tất cả các cột
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu ĐB", 
                    row -> row.getDauDBMatch(), null, null, "Chẵn/Lẻ"));
            statsList.add(calculateEmptyStatsForColumn(rows, "ĐB", 
                    row -> row.getDbMatch(), null, null, "Chẵn/Lẻ"));
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu G1", 
                    row -> row.getDauG1Match(), null, null, "Chẵn/Lẻ"));
            statsList.add(calculateEmptyStatsForColumn(rows, "G1", 
                    row -> row.getG1Match(), null, null, "Chẵn/Lẻ"));
        } else if (isPrime) {
            // Với prime, tính toán cho tất cả các cột
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu ĐB", 
                    row -> row.getDauDBMatch(), null, null, "Số nguyên tố"));
            statsList.add(calculateEmptyStatsForColumn(rows, "ĐB", 
                    row -> row.getDbMatch(), null, null, "Số nguyên tố"));
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu G1", 
                    row -> row.getDauG1Match(), null, null, "Số nguyên tố"));
            statsList.add(calculateEmptyStatsForColumn(rows, "G1", 
                    row -> row.getG1Match(), null, null, "Số nguyên tố"));
        } else if (isDivide3) {
            // Với divide-3, tính toán riêng cho từng loại dư (dư 0, dư 1, dư 2)
            // Đầu ĐB
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu ĐB", 
                    row -> row.getDauDBMatch() == 0 ? 1 : 0, null, null, "Dư 0"));
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu ĐB", 
                    row -> row.getDauDBMatch() == 1 ? 1 : 0, null, null, "Dư 1"));
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu ĐB", 
                    row -> row.getDauDBMatch() == 2 ? 1 : 0, null, null, "Dư 2"));
            // ĐB
            statsList.add(calculateEmptyStatsForColumn(rows, "ĐB", 
                    row -> row.getDbMatch() == 0 ? 1 : 0, null, null, "Dư 0"));
            statsList.add(calculateEmptyStatsForColumn(rows, "ĐB", 
                    row -> row.getDbMatch() == 1 ? 1 : 0, null, null, "Dư 1"));
            statsList.add(calculateEmptyStatsForColumn(rows, "ĐB", 
                    row -> row.getDbMatch() == 2 ? 1 : 0, null, null, "Dư 2"));
            // Đầu G1
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu G1", 
                    row -> row.getDauG1Match() == 0 ? 1 : 0, null, null, "Dư 0"));
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu G1", 
                    row -> row.getDauG1Match() == 1 ? 1 : 0, null, null, "Dư 1"));
            statsList.add(calculateEmptyStatsForColumn(rows, "Đầu G1", 
                    row -> row.getDauG1Match() == 2 ? 1 : 0, null, null, "Dư 2"));
            // G1
            statsList.add(calculateEmptyStatsForColumn(rows, "G1", 
                    row -> row.getG1Match() == 0 ? 1 : 0, null, null, "Dư 0"));
            statsList.add(calculateEmptyStatsForColumn(rows, "G1", 
                    row -> row.getG1Match() == 1 ? 1 : 0, null, null, "Dư 1"));
            statsList.add(calculateEmptyStatsForColumn(rows, "G1", 
                    row -> row.getG1Match() == 2 ? 1 : 0, null, null, "Dư 2"));
        } else {
            // Với 50-50, chỉ tính toán nếu có filter
            if (dauDBStart != null && dauDBEnd != null) {
                statsList.add(calculateEmptyStatsForColumn(rows, "Đầu ĐB", 
                        row -> row.getDauDBMatch(), dauDBStart, dauDBEnd, null));
            }
            
            if (dbStart != null && dbEnd != null) {
                statsList.add(calculateEmptyStatsForColumn(rows, "ĐB", 
                        row -> row.getDbMatch(), dbStart, dbEnd, null));
            }
            
            if (dauG1Start != null && dauG1End != null) {
                statsList.add(calculateEmptyStatsForColumn(rows, "Đầu G1", 
                        row -> row.getDauG1Match(), dauG1Start, dauG1End, null));
            }
            
            if (g1Start != null && g1End != null) {
                statsList.add(calculateEmptyStatsForColumn(rows, "G1", 
                        row -> row.getG1Match(), g1Start, g1End, null));
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
        boolean isPrime = "prime".equals(analysisType);
        boolean isDivide3 = "divide-3".equals(analysisType);
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
                    } else if (isPrime) {
                        // Số nguyên tố = 1, Không phải số nguyên tố = 0
                        row.setDauDBMatch(isPrimeNumber(dauDB) ? 1 : 0);
                    } else if (isDivide3) {
                        // Chia 3: dư 0 = 0, dư 1 = 1, dư 2 = 2
                        int remainder = dauDB % 3;
                        row.setDauDBMatch(remainder < 0 ? remainder + 3 : remainder);
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
            int dbValue = dbDigit.getValue();
            if (isEvenOdd) {
                // Chẵn = 0, Lẻ = 1
                row.setDbMatch((dbValue % 2 == 0) ? 0 : 1);
            } else if (isPrime) {
                // Số nguyên tố = 1, Không phải số nguyên tố = 0
                row.setDbMatch(isPrimeNumber(dbValue) ? 1 : 0);
            } else if (isDivide3) {
                // Chia 3: dư 0 = 0, dư 1 = 1, dư 2 = 2
                int remainder = dbValue % 3;
                row.setDbMatch(remainder < 0 ? remainder + 3 : remainder);
            } else {
                // Logic 50-50: kiểm tra trong khoảng
                if (dbStart != null && dbEnd != null) {
                    row.setDbMatch((dbValue >= dbStart && dbValue <= dbEnd) ? 1 : 0);
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
                    } else if (isPrime) {
                        // Số nguyên tố = 1, Không phải số nguyên tố = 0
                        row.setDauG1Match(isPrimeNumber(dauG1) ? 1 : 0);
                    } else if (isDivide3) {
                        // Chia 3: dư 0 = 0, dư 1 = 1, dư 2 = 2
                        int remainder = dauG1 % 3;
                        row.setDauG1Match(remainder < 0 ? remainder + 3 : remainder);
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
            int g1Value = g1Digit.getValue();
            if (isEvenOdd) {
                // Chẵn = 0, Lẻ = 1
                row.setG1Match((g1Value % 2 == 0) ? 0 : 1);
            } else if (isPrime) {
                // Số nguyên tố = 1, Không phải số nguyên tố = 0
                row.setG1Match(isPrimeNumber(g1Value) ? 1 : 0);
            } else if (isDivide3) {
                // Chia 3: dư 0 = 0, dư 1 = 1, dư 2 = 2
                int remainder = g1Value % 3;
                row.setG1Match(remainder < 0 ? remainder + 3 : remainder);
            } else {
                // Logic 50-50: kiểm tra trong khoảng
                if (g1Start != null && g1End != null) {
                    row.setG1Match((g1Value >= g1Start && g1Value <= g1End) ? 1 : 0);
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
            Integer rangeEnd,
            String defaultRangeLabel
    ) {
        // Xác định range string
        String rangeStr;
        if (rangeStart != null && rangeEnd != null) {
            rangeStr = rangeStart + "-" + rangeEnd;
        } else if (defaultRangeLabel != null) {
            rangeStr = defaultRangeLabel;
        } else {
            rangeStr = "Tất cả";
        }
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
    
    /**
     * Kiểm tra xem một số có phải là số nguyên tố không
     * @param n Số cần kiểm tra (0-99)
     * @return true nếu là số nguyên tố, false nếu không
     */
    private boolean isPrimeNumber(int n) {
        // Số nhỏ hơn 2 không phải số nguyên tố
        if (n < 2) {
            return false;
        }
        // 2 là số nguyên tố
        if (n == 2) {
            return true;
        }
        // Số chẵn lớn hơn 2 không phải số nguyên tố
        if (n % 2 == 0) {
            return false;
        }
        // Kiểm tra các số lẻ từ 3 đến sqrt(n)
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}

