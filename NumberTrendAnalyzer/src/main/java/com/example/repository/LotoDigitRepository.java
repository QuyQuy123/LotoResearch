package com.example.repository;

import com.example.domain.LotoDigit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LotoDigitRepository extends JpaRepository<LotoDigit, Long> {
    
    /**
     * Tìm ngày xuất hiện gần nhất của một số lô
     */
    @Query("SELECT MAX(ld.dailyResult.drawDate) FROM LotoDigit ld WHERE ld.value = :number")
    LocalDate findLastAppearanceDate(@Param("number") Integer number);
    
    /**
     * Đếm số lần xuất hiện của một số lô trong khoảng thời gian
     */
    @Query("SELECT COUNT(DISTINCT ld.dailyResult.drawDate) FROM LotoDigit ld " +
           "WHERE ld.value = :number AND ld.dailyResult.drawDate >= :fromDate")
    Long countAppearancesInRange(@Param("number") Integer number, @Param("fromDate") LocalDate fromDate);
    
    /**
     * Lấy danh sách các số lô đã xuất hiện (distinct)
     */
    @Query("SELECT DISTINCT ld.value FROM LotoDigit ld ORDER BY ld.value")
    List<Integer> findAllDistinctNumbers();
    
    /**
     * Tìm các số lô và số ngày chưa về (từ ngày hiện tại)
     */
    @Query("SELECT ld.value, MAX(ld.dailyResult.drawDate) FROM LotoDigit ld " +
           "GROUP BY ld.value " +
           "ORDER BY MAX(ld.dailyResult.drawDate) ASC")
    List<Object[]> findNumbersWithLastAppearance();
}

