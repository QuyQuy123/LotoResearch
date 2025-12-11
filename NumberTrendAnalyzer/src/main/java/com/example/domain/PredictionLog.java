package com.example.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "prediction_logs")
public class PredictionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate predictionDate; // Ngày thực hiện dự đoán
    private LocalDate targetDate;     // Dự đoán cho ngày nào (thường là predictionDate + 1)

    // Khoảng dự đoán
    private Integer rangeStart; // VD: 60
    private Integer rangeEnd;   // VD: 90

    private Double confidenceScore; // Độ tin cậy (VD: 0.85 tức là 85%)

    private String algorithmUsed; // Tên thuật toán dùng (VD: "Markov", "Frequency")

    // Sau khi có kết quả thực tế, cập nhật trường này
    private Boolean isCorrect; // True nếu kết quả thực tế rơi đúng vào khoảng này

    // Getters, Setters...
}