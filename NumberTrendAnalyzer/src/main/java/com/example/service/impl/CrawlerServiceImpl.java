package com.example.service.impl;


import com.example.service.CrawlerService;
import com.example.domain.LotteryDailyResult;
import com.example.domain.LotoDigit;
import com.example.dto.LotteryDataDTO;
import com.example.repository.LotteryDailyResultRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CrawlerServiceImpl implements CrawlerService {

    @Autowired
    private LotteryDailyResultRepository resultRepo;

    public String crawlAndSaveData(LocalDate date) {
        // 1. Kiểm tra tồn tại
        if (resultRepo.existsByDrawDate(date)) {
            return "Dữ liệu ngày " + date + " đã tồn tại!";
        }

        String dateStr = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // URL Minh Ngọc cực kỳ chuẩn: kqsx.net.vn hoặc minhngoc.net.vn
        // URL: https://www.minhngoc.net.vn/ket-qua-xo-so/mien-bac/11-12-2025.html
        String url = "https://www.minhngoc.net.vn/ket-qua-xo-so/mien-bac/" + dateStr + ".html";

        try {
            // 2. Kết nối (Minh Ngọc rất lành, ít chặn bot)
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(15000)
                    .get();

            // Nếu tiêu đề báo lỗi hoặc không tìm thấy ngày
            if (doc.title().contains("404") || !doc.text().contains(dateStr.replace("-", "/"))) {
                return "Lỗi: Không tìm thấy trang kết quả cho ngày " + dateStr;
            }

            // 3. Khởi tạo Entity
            LotteryDailyResult dailyResult = new LotteryDailyResult();
            dailyResult.setDrawDate(date);
            dailyResult.setRegion("MB");
            List<LotoDigit> digits = new ArrayList<>();

            // 4. Bóc tách dữ liệu (Minh Ngọc dùng Class rất rõ ràng cho từng giải)
            // Bảng kết quả nằm trong div class="box_kqxs"
            Element boxKqxs = doc.selectFirst(".box_kqxs");
            if (boxKqxs == null) boxKqxs = doc.selectFirst(".bkqt"); // Dự phòng class cũ

            if (boxKqxs == null) return "Lỗi: Không tìm thấy bảng .box_kqxs hoặc .bkqt";

            // Lấy giải ĐB (Class: giai_db hoặc giaidb)
            String db = getPrizeText(boxKqxs, ".giai_db, .giaidb");
            if (db.isEmpty()) return "Lỗi: Chưa có KQ giải ĐB (Có thể chưa đến giờ quay)";

            dailyResult.setSpecialPrizeRaw(db);
            digits.add(createDigit(db, "Giai_DB", dailyResult));

            // Lấy các giải khác (Hàm tách chuỗi bên dưới xử lý việc 1 giải có nhiều số)
            parseAndAddDigits(boxKqxs, ".giai_nhat, .giai1", "Giai_1", dailyResult, digits);
            parseAndAddDigits(boxKqxs, ".giai_nhi, .giai2", "Giai_2", dailyResult, digits);
            parseAndAddDigits(boxKqxs, ".giai_ba, .giai3", "Giai_3", dailyResult, digits);
            parseAndAddDigits(boxKqxs, ".giai_tu, .giai4", "Giai_4", dailyResult, digits);
            parseAndAddDigits(boxKqxs, ".giai_nam, .giai5", "Giai_5", dailyResult, digits);
            parseAndAddDigits(boxKqxs, ".giai_sau, .giai6", "Giai_6", dailyResult, digits);
            parseAndAddDigits(boxKqxs, ".giai_bay, .giai7", "Giai_7", dailyResult, digits);

            // 5. Lưu DB
            dailyResult.setLotoDigits(digits);
            resultRepo.save(dailyResult);

            return "Thành công: " + dateStr + " (Nguồn: Minh Ngọc)";

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi ngày " + dateStr + ": " + e.getMessage();
        }
    }

    // --- Các hàm phụ trợ ---

    // Lấy text từ selector (VD: lấy "52668")
    private String getPrizeText(Element container, String cssSelector) {
        Elements els = container.select(cssSelector);
        if (els.isEmpty()) return "";
        return els.text().trim();
    }

    // Tách chuỗi giải thưởng thành các số và thêm vào list
    // VD: Giải 3 là "12345 67890 54321" -> Tách thành 3 số
    private void parseAndAddDigits(Element container, String cssSelector, String prizeName,
                                   LotteryDailyResult dailyResult, List<LotoDigit> digits) {
        String rawText = getPrizeText(container, cssSelector);
        if (rawText.isEmpty()) return;

        // Minh Ngọc ngăn cách số bằng dấu cách hoặc " - "
        String[] nums = rawText.split("[\\s\\-]+");

        for (String num : nums) {
            if (num.trim().length() > 0) {
                digits.add(createDigit(num.trim(), prizeName, dailyResult));
            }
        }
    }

    // Tạo đối tượng LotoDigit
    private LotoDigit createDigit(String rawVal, String prizeName, LotteryDailyResult dailyResult) {
        LotoDigit d = new LotoDigit();
        d.setPrizeName(prizeName);
        d.setDailyResult(dailyResult);

        // 1. Clean số (chỉ giữ ký tự số, bỏ chữ SR, dấu cách...)
        String cleanNum = rawVal.replaceAll("[^0-9]", "");

        // 2. LƯU SỐ ĐẦY ĐỦ (NEW)
        // Lưu nguyên bản "52668" hoặc "08" vào cột fullNumber
        d.setFullNumber(cleanNum);

        // 3. Vẫn tính và lưu 2 số cuối (OLD)

        if (cleanNum.length() >= 2) {
            try {
                String last2 = cleanNum.substring(cleanNum.length() - 2);
                d.setValue(Integer.parseInt(last2)); // Lưu 68
            } catch (Exception e) {
                // Ignore lỗi
            }
        } else if (cleanNum.length() == 1) {
            // Trường hợp hãn hữu số chỉ có 1 chữ số (ít gặp ở XSMB)
            d.setValue(Integer.parseInt(cleanNum));
        }

        return d;
    }

    @Override
    public void crawlRange(LocalDate from, LocalDate to) {
        // Giữ nguyên logic lặp cũ của bạn
        LocalDate current = from;
        while (!current.isAfter(to)) {
            System.out.println(crawlAndSaveData(current));
            current = current.plusDays(1);
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }
    }

    @Override
    public LotteryDataDTO getLotteryDataByDate(LocalDate date) {
        return resultRepo.findByDrawDate(date)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    @Override
    public String autoUpdateFromLastDate() {
        // Lấy ngày cuối cùng trong database
        LocalDate lastDate = resultRepo.findLatestDrawDate().orElse(null);
        LocalDate today = LocalDate.now();
        
        if (lastDate == null) {
            return "Không có dữ liệu trong database. Vui lòng cập nhật dữ liệu ban đầu.";
        }
        
        // Nếu ngày cuối cùng là hôm nay hoặc sau hôm nay, không cần cập nhật
        if (!lastDate.isBefore(today)) {
            return "Dữ liệu đã được cập nhật đến hôm nay (" + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").";
        }
        
        // Bắt đầu từ ngày tiếp theo ngày cuối cùng
        LocalDate startDate = lastDate.plusDays(1);
        int totalDays = 0;
        int successDays = 0;
        int skippedDays = 0;
        
        // Cập nhật từ startDate đến today
        LocalDate current = startDate;
        while (!current.isAfter(today)) {
            totalDays++;
            String result = crawlAndSaveData(current);
            if (result.contains("Thành công")) {
                successDays++;
            } else if (result.contains("đã tồn tại")) {
                skippedDays++;
            }
            current = current.plusDays(1);
            try { 
                Thread.sleep(500); // Delay để tránh spam request
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return String.format("Hoàn thành! Đã cập nhật %d ngày (Thành công: %d, Đã tồn tại: %d, Lỗi: %d). Từ %s đến %s.", 
                totalDays, successDays, skippedDays, totalDays - successDays - skippedDays,
                startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    // Convert từ Entity sang DTO
    private LotteryDataDTO convertToDTO(LotteryDailyResult result) {
        LotteryDataDTO dto = new LotteryDataDTO();
        
        // Format ngày: dd/MM/yyyy
        dto.setDate(result.getDrawDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // Lấy thứ trong tuần (Java: 1=Monday, 7=Sunday)
        // Convert sang format Việt Nam: Thứ 2=2, Thứ 3=3, ..., Thứ 7=7, Chủ nhật=8
        int dayOfWeekJava = result.getDrawDate().getDayOfWeek().getValue();
        int dayOfWeekVN = (dayOfWeekJava == 7) ? 8 : dayOfWeekJava + 1; // Chủ nhật = 8, các thứ khác +1
        dto.setDayOfWeek(String.valueOf(dayOfWeekVN));
        
        // Giải Đặc Biệt
        dto.setSpecialPrize(result.getSpecialPrizeRaw() != null ? result.getSpecialPrizeRaw() : "");
        
        // Group các số theo giải
        List<LotoDigit> digits = result.getLotoDigits();
        if (digits != null && !digits.isEmpty()) {
            // Lấy full number từ database
            List<String> prize1List = getPrizeNumbers(digits, "Giai_1", result.getSpecialPrizeRaw());
            dto.setPrize1(prize1List.isEmpty() ? "" : prize1List.get(0));
            dto.setPrize2(getPrizeNumbers(digits, "Giai_2", result.getSpecialPrizeRaw()));
            dto.setPrize3(getPrizeNumbers(digits, "Giai_3", result.getSpecialPrizeRaw()));
            dto.setPrize4(getPrizeNumbers(digits, "Giai_4", result.getSpecialPrizeRaw()));
            dto.setPrize5(getPrizeNumbers(digits, "Giai_5", result.getSpecialPrizeRaw()));
            dto.setPrize6(getPrizeNumbers(digits, "Giai_6", result.getSpecialPrizeRaw()));
            dto.setPrize7(getPrizeNumbers(digits, "Giai_7", result.getSpecialPrizeRaw()));
        } else {
            dto.setPrize1("");
            dto.setPrize2(new ArrayList<>());
            dto.setPrize3(new ArrayList<>());
            dto.setPrize4(new ArrayList<>());
            dto.setPrize5(new ArrayList<>());
            dto.setPrize6(new ArrayList<>());
            dto.setPrize7(new ArrayList<>());
        }
        
        // Mã ĐB - có thể null
        dto.setCodes(null);
        
        return dto;
    }

    // Lấy danh sách số của một giải
    private List<String> getPrizeNumbers(List<LotoDigit> digits, String prizeName, String specialPrizeRaw) {
        return digits.stream()
                .filter(d -> prizeName.equals(d.getPrizeName()))
                .map(d -> {
                    // Ưu tiên dùng fullNumber nếu có
                    if (d.getFullNumber() != null && !d.getFullNumber().isEmpty()) {
                        return d.getFullNumber();
                    }
                    // Fallback: dùng specialPrizeRaw cho giải ĐB
                    if (specialPrizeRaw != null && !specialPrizeRaw.isEmpty() && prizeName.equals("Giai_DB")) {
                        return specialPrizeRaw;
                    }
                    // Cuối cùng: format 2 số cuối (không chính xác nhưng để demo)
                    return String.format("%02d", d.getValue());
                })
                .collect(Collectors.toList());
    }
}



