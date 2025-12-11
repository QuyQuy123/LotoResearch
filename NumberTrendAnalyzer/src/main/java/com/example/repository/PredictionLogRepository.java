package com.example.repository;

import com.example.domain.PredictionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PredictionLogRepository extends JpaRepository<PredictionLog, Long> {
    
    /**
     * Lấy dự đoán mới nhất cho ngày mai
     */
    @Query("SELECT p FROM PredictionLog p WHERE p.targetDate >= :today ORDER BY p.predictionDate DESC")
    Optional<PredictionLog> findLatestPrediction(@Param("today") LocalDate today);
}

