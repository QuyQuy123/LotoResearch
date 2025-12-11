package com.example.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lottery_daily_results",
        indexes = @Index(columnList = "drawDate")) // Index ngày để query cho nhanh
public class LotteryDailyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate drawDate; // Ngày quay thưởng (VD: 2023-10-25)

    @Column(length = 10)
    private String region; // "MB", "MN", "MT"

    @Column(length = 10)
    private String specialPrizeRaw; // Lưu full giải đặc biệt (VD: "58924") để bắt "Đề"

    // Quan hệ 1-N: Một ngày có danh sách các con lô đã về
    @OneToMany(mappedBy = "dailyResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LotoDigit> lotoDigits;

    // Getters, Setters, Constructors...
}