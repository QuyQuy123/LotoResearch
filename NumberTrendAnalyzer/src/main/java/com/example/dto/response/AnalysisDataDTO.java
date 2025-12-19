package com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisDataDTO {
    private List<AnalysisRowDTO> rows;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisRowDTO {
        private String date; // Format: dd-MM-yyyy
        private Integer dauDB; // Đầu ĐB (2 số đầu của giải đặc biệt)
        private Integer db; // ĐB (2 số cuối của giải đặc biệt, 00-99)
        private Integer dauG1; // Đầu G1 (2 số đầu của giải 1)
        private Integer g1; // G1 (2 số cuối của giải 1, 00-99)
        private Integer dauDBMatch; // 1 nếu trong khoảng, 0 nếu không
        private Integer dbMatch; // 1 nếu trong khoảng, 0 nếu không
        private Integer dauG1Match; // 1 nếu trong khoảng, 0 nếu không
        private Integer g1Match; // 1 nếu trong khoảng, 0 nếu không
    }
}

