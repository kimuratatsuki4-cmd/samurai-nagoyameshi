package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.form.CompanyEditForm;
import com.example.nagoyameshi.service.CompanyService;

@Controller
@RequestMapping("/admin/company")
public class AdminCompanyController {
	private final CompanyService companyService;

	public AdminCompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@GetMapping
	public String index(Model model) {
		// データベースから会社情報を取得
		Company company = companyService.findFirstCompanyByOrderByIdDesc();
		model.addAttribute("company", company);

		// admin/company/index.html を表示
		return "admin/company/index";
	}

	// 2. 会社概要の編集フォームページを表示する
	// URL: GET /admin/company/edit
	@GetMapping("/edit")
	public String edit(Model model) {
		Company company = companyService.findFirstCompanyByOrderByIdDesc();
		// 編集フォームに既存のデータをセット
		CompanyEditForm companyEditForm = new CompanyEditForm(
				company.getName(),
				company.getPostalCode(),
				company.getAddress(),
				company.getRepresentative(),
				company.getEstablishmentDate(),
				company.getCapital(),
				company.getBusiness(),
				company.getNumberOfEmployees());

		model.addAttribute("companyEditForm", companyEditForm);
		// admin/company/edit.html を表示
		return "admin/company/edit";
	}

	// 3. フォームから送信された会社概要の内容でデータベースを更新する
	// URL: POST /admin/company/update
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated CompanyEditForm companyEditForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {

		// バリデーションエラーチェック
		if (bindingResult.hasErrors()) {
			model.addAttribute("companyEditForm", companyEditForm);
			return "admin/company/edit";
		}
		Company company = companyService.findFirstCompanyByOrderByIdDesc();
		// Service層を呼び出して更新処理を実行
		companyService.updateCompany(company, companyEditForm);
		// 成功メッセージを設定し、一覧ページへリダイレクト
		redirectAttributes.addFlashAttribute("successMessage", "会社概要を編集しました。");

		// 更新後、GET /admin/company にリダイレクト
		return "redirect:/admin/company";
	}
}
