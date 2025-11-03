package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {
	public final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;

	}

	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}

	@PostMapping("/signup")
	public String signup(@ModelAttribute @Validated SignupForm signupForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			HttpServletRequest httpServletRequest,
			Model model) {
		// メールアドレスが登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
		if (userService.isEmailRegisterd(signupForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}

		if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "既に登録済みのパスワードです。");
			bindingResult.addError(fieldError);
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute("signupForm", signupForm);

			return "auth/signup";
		}

		userService.createUser(signupForm);
		redirectAttributes.addFlashAttribute("successMessage",
				"会員登録が完了しました。");

		return "redirect:/";
	}

}
