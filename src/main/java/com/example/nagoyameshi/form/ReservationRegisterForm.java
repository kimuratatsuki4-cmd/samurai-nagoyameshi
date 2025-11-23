package com.example.nagoyameshi.form;

import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationRegisterForm {
    
    // 予約日
    @NotNull(message = "予約日を選択してください。")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;
    
    // 予約時間
    @NotNull(message = "時間を選択してください。")
    // タイムゾーンやサマータイムの問題を避けるため、通常はHH:mmを指定しますが、仕様に合わせHH:mm:ssとします
    @DateTimeFormat(pattern = "HH:mm:ss") 
    private LocalTime reservationTime;
    
    // 予約人数
    @NotNull(message = "人数を選択してください。")
    @Range(min = 1, max = 50, message = "予約人数は1人以上50人以下で選択してください。")
    private Integer numberOfPeople;
    
    // 備考：店舗IDはフォームには含めず、Controllerのパス変数（@PathVariable）で受け取るため、ここでは定義しません。
}