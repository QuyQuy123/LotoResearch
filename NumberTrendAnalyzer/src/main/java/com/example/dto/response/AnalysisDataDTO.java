package com.example.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisDataDTO {
    private List<AnalysisRowDTO> rows;
    private List<EmptyStatsDTO> emptyStats; // Thống kê rỗng
    private Integer totalPages; // Tổng số trang
    private Integer currentPage; // Trang hiện tại
    private Long totalElements; // Tổng số phần tử
    
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
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmptyStatsDTO {
        private String columnName; // Tên cột: "Đầu ĐB", "ĐB", "Đầu G1", "G1"
        private String range; // Khoảng lọc: "51-99"
        private List<EmptyCountDTO> counts; // Danh sách số lần xuất hiện rỗng
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EmptyCountDTO {
            private Integer emptyLength; // Độ dài rỗng (3, 4, 5, 6, ...)
            private Integer count; // Số lần xuất hiện
        }
    }
}

