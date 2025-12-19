package com.example.repository;

import com.example.domain.LotteryDailyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LotteryDailyResultRepository extends JpaRepository<LotteryDailyResult, Long> {
    /**
     * Kiểm tra xem đã có dữ liệu cho ngày này chưa
     */
    boolean existsByDrawDate(LocalDate drawDate);

    /**
     * Tìm kết quả theo ngày
     */
    Optional<LotteryDailyResult> findByDrawDate(LocalDate drawDate);
    
    /**
     * Lấy ngày cập nhật gần nhất
     */
    @Query("SELECT MAX(r.drawDate) FROM LotteryDailyResult r")
    Optional<LocalDate> findLatestDrawDate();
    
    /**
     * Lấy tất cả kết quả từ ngày cụ thể trở đi, sắp xếp theo ngày
     */
    @Query("SELECT r FROM LotteryDailyResult r LEFT JOIN FETCH r.lotoDigits WHERE r.drawDate >= :fromDate ORDER BY r.drawDate")
    List<LotteryDailyResult> findAllFromDate(LocalDate fromDate);
}
