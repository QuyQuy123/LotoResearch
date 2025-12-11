package com.example.repository;

import com.example.domain.LotteryDailyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
}
