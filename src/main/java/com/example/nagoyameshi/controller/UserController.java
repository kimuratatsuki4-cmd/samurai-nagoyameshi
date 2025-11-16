package com.example.nagoyameshi.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 会員情報ページを表示します。
	 * GET /user
	 */
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		// 現在ログイン中のユーザーのUserエンティティを取得
		User user = userDetailsImpl.getUser();
		model.addAttribute("user", user);

		return "user/index";
	}

	/**
	 * 会員情報編集ページを表示します。
	 * GET /user/edit
	 */
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		// 現在ログイン中のユーザーのUserエンティティを取得
		User user = userDetailsImpl.getUser();

		// LocalDate型からString型への変換用フォーマッター（"yyyyMMdd"形式）
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

		// Userエンティティの値をUserEditFormにセット
		UserEditForm userEditForm = new UserEditForm(
				user.getName(),
				user.getFurigana(),
				user.getPostalCode(),
				user.getAddress(),
				user.getPhoneNumber(),

				// birthdayの型変換処理
				// User.birthdayがnullでなければフォーマットし、nullであればnullをセットする
				user.getBirthday() != null ? user.getBirthday().format(formatter) : null,

				user.getOccupation(),
				user.getEmail());

		model.addAttribute("userEditForm", userEditForm);

		return "user/edit";
	}

	/**
	 * 会員情報を更新します。
	 * POST /user/update
	 */
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated UserEditForm userEditForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			Model model) {

		// 現在ログイン中のユーザーのUserエンティティを取得
		User user = userDetailsImpl.getUser();

		// カスタムバリデーション
		// 1. メールアドレスが変更されたかチェック
		// 2. 変更されたメールアドレスが既にデータベースに登録済みかチェック
		if (userService.isEmailChanged(userEditForm, user)
				&& userService.isEmailRegisterd(userEditForm.getEmail())) {

			// 既に登録済みのメールアドレスが入力された場合、BindingResultにエラーを追加
			FieldError fieldError = new FieldError(
					bindingResult.getObjectName(),
					"email",
					"既に使用されているメールアドレスです。");
			bindingResult.addError(fieldError);
		}

		if (bindingResult.hasErrors()) {
			return "user/edit";
		}

		// エラーが存在しない場合、会員情報を更新
		userService.updateUser(user, userEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");

		// 会員情報ページにリダイレクト
		return "redirect:/user";
	}
}