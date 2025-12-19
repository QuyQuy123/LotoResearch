package com.example.service.impl;

import com.example.domain.LotteryDailyResult;
import com.example.domain.LotoDigit;
import com.example.dto.response.AnalysisDataDTO;
import com.example.repository.LotteryDailyResultRepository;
import com.example.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisServiceImpl implements AnalysisService {
    
    @Autowired
    private LotteryDailyResultRepository resultRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    @Override
    public AnalysisDataDTO getAnalysisData(
            LocalDate fromDate,
            Integer dauDBStart, Integer dauDBEnd,
            Integer dbStart, Integer dbEnd,
            Integer dauG1Start, Integer dauG1End,
            Integer g1Start, Integer g1End
    ) {
        // Lấy tất cả kết quả từ fromDate trở đi, sắp xếp theo ngày
        List<LotteryDailyResult> results = resultRepository.findAllFromDate(fromDate);
        
        List<AnalysisDataDTO.AnalysisRowDTO> rows = new ArrayList<>();
        
        for (LotteryDailyResult result : results) {
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
                rows.add(row);
                continue;
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
                        row.setDauDB(Integer.parseInt(dauDBStr));
                        // Kiểm tra match
                        if (dauDBStart != null && dauDBEnd != null) {
                            int dauDB = Integer.parseInt(dauDBStr);
                            row.setDauDBMatch((dauDB >= dauDBStart && dauDB <= dauDBEnd) ? 1 : 0);
                        } else {
                            row.setDauDBMatch(0);
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
                if (dbStart != null && dbEnd != null) {
                    row.setDbMatch((dbDigit.getValue() >= dbStart && dbDigit.getValue() <= dbEnd) ? 1 : 0);
                } else {
                    row.setDbMatch(0);
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
                        row.setDauG1(Integer.parseInt(dauG1Str));
                        // Kiểm tra match
                        if (dauG1Start != null && dauG1End != null) {
                            int dauG1 = Integer.parseInt(dauG1Str);
                            row.setDauG1Match((dauG1 >= dauG1Start && dauG1 <= dauG1End) ? 1 : 0);
                        } else {
                            row.setDauG1Match(0);
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
                if (g1Start != null && g1End != null) {
                    row.setG1Match((g1Digit.getValue() >= g1Start && g1Digit.getValue() <= g1End) ? 1 : 0);
                } else {
                    row.setG1Match(0);
                }
            } else {
                row.setDauG1(null);
                row.setG1(null);
                row.setDauG1Match(0);
                row.setG1Match(0);
            }
            
            rows.add(row);
        }
        
        AnalysisDataDTO dto = new AnalysisDataDTO();
        dto.setRows(rows);
        return dto;
    }
}

