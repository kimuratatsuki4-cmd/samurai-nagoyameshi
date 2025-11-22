package com.example.nagoyameshi.form;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRegisterForm {

    /** 評価 (1～5) */
    @NotNull(message = "評価を選択してください。")
    @Range(min = 1, max = 5, message = "評価は1～5のいずれかを選択してください。")
    private Integer score;

    /** 感想 */
    @NotBlank(message = "感想を入力してください。")
    @Size(max = 300, message = "感想は300文字以内で入力してください。")
    private String content;
}