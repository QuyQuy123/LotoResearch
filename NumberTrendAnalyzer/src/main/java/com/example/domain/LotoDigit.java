package com.example.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "loto_digits", indexes = @Index(columnList = "value"))
public class LotoDigit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- GIỮ NGUYÊN CÁI CŨ (Để code dự đoán hiện tại không bị lỗi) ---
    @Column(nullable = false)
    private Integer value; // 2 số cuối (00-99)

    // --- THÊM CÁI MỚI (Để lưu full số) ---
    @Column(length = 10)
    private String fullNumber; // Lưu "52668", "0245"...

    @Column(length = 20)
    private String prizeName; // Giai_DB, Giai_1...

    @ManyToOne
    @JoinColumn(name = "daily_result_id", nullable = false)
    private LotteryDailyResult dailyResult;

    // ... các getter/setter khác
}