package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LotteryDataDTO {
    private String date;
    private String dayOfWeek;
    private List<String> codes; // Mã ĐB (có thể null)
    private String specialPrize;
    private String prize1;
    private List<String> prize2;
    private List<String> prize3;
    private List<String> prize4;
    private List<String> prize5;
    private List<String> prize6;
    private List<String> prize7;
}

